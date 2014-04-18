package br.edu.utfpr.cm.JGitMinerWeb.services.metric;

import br.edu.utfpr.cm.JGitMinerWeb.dao.GenericDao;
import br.edu.utfpr.cm.JGitMinerWeb.model.matrix.EntityMatrix;
import br.edu.utfpr.cm.JGitMinerWeb.model.matrix.EntityMatrixNode;
import br.edu.utfpr.cm.JGitMinerWeb.model.miner.EntityCommit;
import br.edu.utfpr.cm.JGitMinerWeb.model.miner.EntityCommitFile;
import br.edu.utfpr.cm.JGitMinerWeb.model.miner.EntityCommitUser;
import br.edu.utfpr.cm.JGitMinerWeb.model.miner.EntityRepository;
import br.edu.utfpr.cm.JGitMinerWeb.model.miner.EntityRepositoryCommit;
import br.edu.utfpr.cm.JGitMinerWeb.services.matrix.UserCommentedSamePairOfFileInDateServices;
import br.edu.utfpr.cm.JGitMinerWeb.services.matrix.UserModifySamePairOfFileInDateServices;
import br.edu.utfpr.cm.JGitMinerWeb.services.matrix.auxiliary.AuxFileFile;
import br.edu.utfpr.cm.JGitMinerWeb.services.matrix.auxiliary.AuxUserUser;
import br.edu.utfpr.cm.JGitMinerWeb.services.metric.auxiliary.AuxFileFileMetrics;
import br.edu.utfpr.cm.JGitMinerWeb.services.metric.structuralholes.StructuralHoleMeasure;
import br.edu.utfpr.cm.JGitMinerWeb.services.metric.structuralholes.StructuralHolesCalculator;
import br.edu.utfpr.cm.JGitMinerWeb.util.JsfUtil;
import br.edu.utfpr.cm.JGitMinerWeb.util.OutLog;
import edu.uci.ics.jung.graph.UndirectedGraph;
import edu.uci.ics.jung.graph.UndirectedSparseGraph;
import edu.uci.ics.jung.graph.UndirectedSparseMultigraph;
import edu.uci.ics.jung.graph.util.EdgeType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Measure structural holes metrics based on graph of co-change with weighted edges.
 * The result of measure is about the edges. An edge represents a pair of files 
 * (i.e. two files has changed in same pull request), in a timeframe.
 * To calculate the metric, we sum the measure of the every vertex that 
 * connected with these pair of file.
 * 
 * @author Rodrigo T. Kuroda <rodrigokuroda at gmail dot com>
 */
public class CochangeStructuralHolesMeasureServices extends AbstractMetricServices {

    private EntityRepository repository;

    private static final String CALCULE_SUM_UPDATES_OF_TWO_FILE = 
            "SELECT count(1) FROM " + EntityRepositoryCommit.class.getSimpleName() + " rc "
            + "JOIN " + EntityCommit.class.getSimpleName() + " c ON rc.commit = c "
            + "JOIN " + EntityCommitUser.class.getSimpleName() + " u ON c.committer = u "
            + "JOIN " + EntityCommitFile.class.getSimpleName() + " f ON f.repositoryCommit = rc "
            + "WHERE "
            + "rc.repository = :repo AND "
            + "u.dateCommitUser BETWEEN :beginDate AND :endDate AND "
            + "(f.filename = :fileName OR f.filename = :fileName2)";

    private static final String[] CALCULE_SUM_UPDATES_OF_TWO_FILE_PARAMS = new String[]{
        "repo", "fileName", "fileName2", "beginDate", "endDate"
    };

    private static final String CALCULE_UPDATES = 
            "SELECT COUNT(rc) FROM EntityRepositoryCommit rc "
            + "WHERE "
            + "rc.repository = :repo AND "
            + "rc.commit.committer.dateCommitUser BETWEEN :beginDate AND :endDate AND "
            + "EXISTS (SELECT f FROM EntityCommitFile f WHERE f.repositoryCommit = rc AND f.filename = :fileName) AND "
            + "EXISTS (SELECT f2 FROM EntityCommitFile f2 WHERE f2.repositoryCommit = rc AND f2.filename = :fileName2)";

    private static final String[] CALCULE_UPDATES_PARAMS = new String[]{
        "repo", "fileName", "fileName2", "beginDate", "endDate"
    };

    
    public CochangeStructuralHolesMeasureServices(GenericDao dao, OutLog out) {
        super(dao, out);
    }

    public CochangeStructuralHolesMeasureServices(GenericDao dao, EntityMatrix matrix, Map params, OutLog out) {
        super(dao, matrix, params, out);
    }

    public Date getBeginDate() {
        Calendar beginDateCalendar = Calendar.getInstance();
        beginDateCalendar.setTime(getDateParam("beginDate"));
        beginDateCalendar.set(Calendar.HOUR_OF_DAY, 0);
        beginDateCalendar.set(Calendar.MINUTE, 0);
        beginDateCalendar.set(Calendar.SECOND, 0);
        beginDateCalendar.set(Calendar.MILLISECOND, 0);
        return beginDateCalendar.getTime();
    }

    public Date getEndDate() {
        Calendar endDateCalendar = Calendar.getInstance();
        endDateCalendar.setTime(getDateParam("endDate"));
        endDateCalendar.set(Calendar.HOUR_OF_DAY, 23);
        endDateCalendar.set(Calendar.MINUTE, 59);
        endDateCalendar.set(Calendar.SECOND, 59);
        endDateCalendar.set(Calendar.MILLISECOND, 999);
        return endDateCalendar.getTime();
    }

    @Override
    public void run() {
        System.out.println(params);

        System.out.println(getMatrix().getClassServicesName());
        
        Date beginDate = getBeginDate();
        Date endDate = getEndDate();
        Date futureBeginDate = getFutureBeginDate();
        Date futureEndDate = getFutureEndDate();

        if (getMatrix() == null
                || !getAvailableMatricesPermitted().contains(getMatrix().getClassServicesName())) {
            throw new IllegalArgumentException("Selecione uma matrix gerada pelo Services: " + getAvailableMatricesPermitted());
        }

        repository = getRepository();

        if (repository == null) {
            throw new IllegalArgumentException("Não foi possível encontrar o repositório utilizado nesta matrix.");
        }

        System.out.println("Selecionado matrix com " + getMatrix().getNodes().size() + " nodes.");
        String graphType = (String) params.get("graphType");
        final UndirectedGraph<String, String> graph;
        if ("multi".equals(graphType)) {
            graph = new UndirectedSparseMultigraph<>();
        } else {
            graph = new UndirectedSparseGraph<>();
        }
        
        final Map<String, Integer> edgeWeigth = new HashMap<>(getMatrix().getNodes().size());
        final Map<AuxFileFile, Set<String>> commitersPairFile = new HashMap<>();
        final Map<AuxFileFile, Long> pairFileCommitCount = new HashMap<>();
        
        final Set<String> distinctDevelopers = new HashSet<>();
        final Set<String> distinctFiles = new HashSet<>();
        
        Set<AuxFileFile> pairFiles = new HashSet<>();
        Map<String, AuxFileFile> edgeFiles = new HashMap<>();
        for (int i = 0; i < getMatrix().getNodes().size(); i++) {
            EntityMatrixNode node = getMatrix().getNodes().get(i);
            String[] columns = node.getLine().split(JsfUtil.TOKEN_SEPARATOR);
            
            AuxFileFile pairFile = new AuxFileFile(columns[1], columns[2]);
            AuxUserUser pairUser = new AuxUserUser(columns[0], columns[3]);
            
            // to count unique developers
            distinctDevelopers.add(pairUser.getUser());
            distinctDevelopers.add(pairUser.getUser2());
            
            /** 
             * Extract all distinct developer that commit a pair of file 
             */
            if (commitersPairFile.containsKey(pairFile)) {
                Set<String> commiters = commitersPairFile.get(pairFile);
                commiters.add(pairUser.getUser());
                commiters.add(pairUser.getUser2());
            } else {
                Set<String> commiters = new HashSet<>();
                commiters.add(pairUser.getUser());
                commiters.add(pairUser.getUser2());
                commitersPairFile.put(pairFile, commiters);
            }
            
            String edgeName;
            if (graph instanceof UndirectedSparseGraph) {
               edgeName = pairUser.toString();
            } else {
               edgeName = pairFile + "(" + i + ")";
            }
            
            // to count unique files
            distinctFiles.add(pairFile.getFileName());
            distinctFiles.add(pairFile.getFileName2());
            
            pairFiles.add(pairFile);
            edgeFiles.put(edgeName, pairFile);
            
            String weightedEdge = (String) params.get("weightedEdge");
            if (graph instanceof UndirectedSparseMultigraph) {
                /* On multi edge (parallel edges), always insert an edge */
                if ("false".equalsIgnoreCase(weightedEdge)) { 
                    // binary edge weight
                    edgeWeigth.put(edgeName, 1);
                } else {
                    edgeWeigth.put(edgeName, Integer.valueOf(columns[4]));
                }
            } else {
                if ("false".equalsIgnoreCase(weightedEdge)) { 
                    // binary edge weight
                    edgeWeigth.put(edgeName, 1);
                } else {
                    /* Sum commit for each pair file that the pair dev has commited. */
                    if (edgeWeigth.containsKey(pairUser.toStringUserAndUser2())) {
                        // edgeName = user + user2
                        edgeWeigth.put(pairUser.toStringUserAndUser2(), edgeWeigth.get(pairUser.toStringUserAndUser2()) + Integer.valueOf(columns[4]));
                    } else if (edgeWeigth.containsKey(pairUser.toStringUser2AndUser())) {
                        // edgeName = user2 + user
                        edgeWeigth.put(pairUser.toStringUser2AndUser(), edgeWeigth.get(pairUser.toStringUser2AndUser()) + Integer.valueOf(columns[4]));
                    } else {
                        edgeWeigth.put(edgeName, Integer.valueOf(columns[4]));
                    }
                }
            }
            
            if (!pairFileCommitCount.containsKey(pairFile)) {
                pairFileCommitCount.put(pairFile, 
                        calculeSumUpdatesOfTwoFile(pairFile.getFileName(), pairFile.getFileName2(), beginDate, endDate));
            }
            
            /** 
             * The co-change network is constructed as follow: 
             * If developer A and developer B commits a pair file PF, then they are connected in network.
             */
            if (!graph.containsVertex(pairUser.getUser()) 
                    || !graph.containsVertex(pairUser.getUser2()) 
                    || !graph.isNeighbor(pairUser.getUser(), pairUser.getUser2())) {
                graph.addEdge(edgeName, pairUser.getUser(), pairUser.getUser2(), EdgeType.UNDIRECTED);
            }
        }
        out.printLog("Weigth of edges:");
        for (Map.Entry<String, Integer> entry : edgeWeigth.entrySet()) {
            out.printLog(entry.getKey() + " " + entry.getValue());
        }
        out.printLog("End of weigth of edges.");

        /** 
         * Calculates structural holes metrics for each developer on the co-change network based.
         */
        Map<String, StructuralHoleMeasure<String>> metricsResult = 
                StructuralHolesCalculator.calculeStructuralHolesMetrics(graph, edgeWeigth);
        
        /** 
         * For each pair of file, calculate the sum, average, and max of each 
         * structural holes metrics of developer that commited the pair of file.
         */
        List<AuxFileFileMetrics> structuralHolesMetrics = new ArrayList<>(pairFiles.size());
        for (AuxFileFile pairFile : pairFiles) {
            double efficiencyMax = 0, efficiencyAvg, efficiencySum = 0;
            double effectiveSizeMax = 0, effectiveSizeAvg, effectiveSizeSum = 0;
            double constraintMax = 0, constraintAvg, constraintSum = 0;
            double hierarchyMax = 0, hierarchyAvg, hierarchySum = 0;
            long developers = 0;
            for (String commiter : commitersPairFile.get(pairFile)) {
                StructuralHoleMeasure<String> commiterMetric = metricsResult.get(commiter);
                double efficiency = commiterMetric.getEfficiency();
                double effectiveSize = commiterMetric.getEffectiveSize();
                double constraint = commiterMetric.getConstraint();
                double hierarchy = commiterMetric.getHierarchy();
                
                efficiencySum += efficiency;
                effectiveSizeSum += effectiveSize;
                constraintSum += constraint;
                hierarchySum += hierarchy;
                
                efficiencyMax = Math.max(efficiencyMax, efficiency);
                effectiveSizeMax = Math.max(effectiveSizeMax, effectiveSize);
                constraintMax = Math.max(constraintMax, constraint);
                hierarchyMax = Math.max(hierarchyMax, hierarchy);
                
                developers++;
            }
            efficiencyAvg = efficiencySum / developers;
            effectiveSizeAvg = effectiveSizeSum / developers;
            constraintAvg = constraintSum / developers;
            hierarchyAvg = hierarchySum / developers;
            
            Long updates = 
                    calculeUpdates(pairFile.getFileName(), pairFile.getFileName2(), 
                            beginDate, endDate);
            Long futureUpdates = 
                    calculeUpdates(pairFile.getFileName(), pairFile.getFileName2(), 
                            futureBeginDate, futureEndDate);
            
            structuralHolesMetrics.add(
                    new AuxFileFileMetrics(
                        pairFile.getFileName(), pairFile.getFileName2(),
                        efficiencySum, efficiencyAvg, efficiencyMax, 
                        effectiveSizeSum, effectiveSizeAvg, effectiveSizeMax,
                        constraintSum, constraintAvg, constraintMax,
                        hierarchySum, hierarchyAvg, hierarchyMax,
                        developers, pairFileCommitCount.get(pairFile), updates, futureUpdates));
        }
        
        addToEntityMetricNodeList(structuralHolesMetrics);
        
        out.printLog("Distinct developers: " + distinctDevelopers.size());
        out.printLog("Distinct files: " + distinctFiles.size());
    }

    @Override
    public String getHeadCSV() {
        return "file1;file2;"
                + "efficiencySum;efficiencyAvg;efficiencyMax;"
                + "effectiveSizeSum;effectiveSizeAvg;effectiveSizeMax;"
                + "constraintSum;constraintAvg;constraintMax;"
                + "hierarchySum;hierarchyAvg;hierarchyMax;"
                + "developers;commits;updates;futureUpdates"
                ;
    }

    @Override
    public List<String> getAvailableMatricesPermitted() {
        return Arrays.asList(
                UserModifySamePairOfFileInDateServices.class.getName(),
                UserCommentedSamePairOfFileInDateServices.class.getName()
        );
    }
    
    private Long calculeSumUpdatesOfTwoFile(String fileName, String fileName2, Date beginDate, Date endDate) {
        if (beginDate == null || endDate == null) {
            return 0l;
        }
        
        Object[] queryParams = new Object[]{
            repository,
            fileName,
            fileName2,
            beginDate,
            endDate
        };

        return dao.selectOneWithParams(CALCULE_SUM_UPDATES_OF_TWO_FILE, CALCULE_SUM_UPDATES_OF_TWO_FILE_PARAMS, queryParams);
    }
    
    private Long calculeUpdates(String fileName, String fileName2, Date beginDate, Date endDate) {
        if (beginDate == null || endDate == null) {
            return 0l;
        }
        
        Object[] queryParams = new Object[]{
            repository,
            fileName,
            fileName2,
            beginDate,
            endDate
        };

        return dao.selectOneWithParams(CALCULE_UPDATES, CALCULE_UPDATES_PARAMS, queryParams);
    }
    
    private Long calculeUpdatesForCommiterPair(String fileName, String fileName2, String commiter, String commiter2,
            Date beginDate, Date endDate) {
        if (beginDate == null || endDate == null) {
            return 0l;
        }
        String jpql = "SELECT COUNT(rc) "
                + "FROM "
                + "EntityRepositoryCommit rc "
                + "WHERE "
                + "rc.repository = :repo AND "
                + "rc.commit.committer.dateCommitUser BETWEEN :beginDate AND :endDate AND "
                + "EXISTS (SELECT f FROM EntityCommitFile f WHERE f.repositoryCommit = rc AND f.filename = :fileName) AND "
                + "EXISTS (SELECT f2 FROM EntityCommitFile f2 WHERE f2.repositoryCommit = rc AND f2.filename = :fileName2) AND "
                + "("
                  + "("
                    + "(rc.commit.commiter.email IS NOT NULL AND rc.commit.commiter.email = :commiter) OR "
                    + "(rc.commit.commiter.name IS NOT NULL AND rc.commit.commiter.name = :commiter) "
                  + ") OR (" 
                    + "(rc.commit.commiter.email IS NOT NULL AND rc.commit.commiter.email = :commiter2) OR "
                    + "(rc.commit.commiter.name IS NOT NULL AND rc.commit.commiter.name = :commiter2) "
                  + ")"
                + ")";

        String[] bdParams = new String[]{
            "repo",
            "fileName",
            "fileName2",
            "beginDate",
            "endDate",
            "commiter",
            "commiter2"
        };
        Object[] bdObjects = new Object[]{
            repository,
            fileName,
            fileName2,
            beginDate,
            endDate,
            commiter,
            commiter2
        };

        return dao.selectOneWithParams(jpql, bdParams, bdObjects);
    }
    
    public Date getFutureBeginDate() {
        Calendar futureBeginDateCalendar = Calendar.getInstance();
        futureBeginDateCalendar.setTime(getDateParam("futureBeginDate"));
        futureBeginDateCalendar.set(Calendar.HOUR_OF_DAY, 0);
        futureBeginDateCalendar.set(Calendar.MINUTE, 0);
        futureBeginDateCalendar.set(Calendar.SECOND, 0);
        futureBeginDateCalendar.set(Calendar.MILLISECOND, 0);
        return futureBeginDateCalendar.getTime();
    }

    public Date getFutureEndDate() {
        Calendar futureEndDateCalendar = Calendar.getInstance();
        futureEndDateCalendar.setTime(getDateParam("futureEndDate"));
        futureEndDateCalendar.set(Calendar.HOUR_OF_DAY, 23);
        futureEndDateCalendar.set(Calendar.MINUTE, 59);
        futureEndDateCalendar.set(Calendar.SECOND, 59);
        futureEndDateCalendar.set(Calendar.MILLISECOND, 999);
        return futureEndDateCalendar.getTime();
    }

    private EntityRepository getRepository() {
        String[] repoStr = getMatrix().getRepository().split("/");
        List<EntityRepository> repos = dao.executeNamedQueryWithParams(
                "Repository.findByNameAndOwner",
                new String[]{"login", "name"},
                new Object[]{repoStr[0], repoStr[1]});
        if (repos.size() == 1) {
            return repos.get(0);
        }
        return null;
    }
}
