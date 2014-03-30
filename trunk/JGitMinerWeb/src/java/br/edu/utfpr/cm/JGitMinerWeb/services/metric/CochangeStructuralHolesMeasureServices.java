package br.edu.utfpr.cm.JGitMinerWeb.services.metric;

import br.edu.utfpr.cm.JGitMinerWeb.dao.GenericDao;
import br.edu.utfpr.cm.JGitMinerWeb.model.matrix.EntityMatrix;
import br.edu.utfpr.cm.JGitMinerWeb.model.matrix.EntityMatrixNode;
import br.edu.utfpr.cm.JGitMinerWeb.model.miner.EntityRepository;
import br.edu.utfpr.cm.JGitMinerWeb.services.matrix.UserModifySamePairOfFileInDateServices;
import br.edu.utfpr.cm.JGitMinerWeb.services.matrix.auxiliary.AuxFileFile;
import br.edu.utfpr.cm.JGitMinerWeb.services.metric.auxiliary.AuxFileFileMetrics;
import br.edu.utfpr.cm.JGitMinerWeb.services.metric.structuralholes.StructuralHoleMetric;
import br.edu.utfpr.cm.JGitMinerWeb.services.metric.structuralholes.StructuralHolesCalculator;
import br.edu.utfpr.cm.JGitMinerWeb.util.JsfUtil;
import br.edu.utfpr.cm.JGitMinerWeb.util.OutLog;
import edu.uci.ics.jung.graph.UndirectedSparseMultigraph;
import edu.uci.ics.jung.graph.util.EdgeType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author Rodrigo T. Kuroda
 */
public class CochangeStructuralHolesMeasureServices extends AbstractMetricServices {

    private EntityRepository repository;

    public CochangeStructuralHolesMeasureServices(GenericDao dao, OutLog out) {
        super(dao, out);
    }

    public CochangeStructuralHolesMeasureServices(GenericDao dao, EntityMatrix matrix, Map params, OutLog out) {
        super(dao, matrix, params, out);
    }

    public Date getBeginDate() {
        return getDateParam("beginDate");
    }

    public Date getEndDate() {
        return getDateParam("endDate");
    }

    @Override
    public void run() {
        System.out.println(params);

        System.out.println(getMatrix().getClassServicesName());

        if (getMatrix() == null
                || !getAvailableMatricesPermitted().contains(getMatrix().getClassServicesName())) {
            throw new IllegalArgumentException("Selecione uma matrix gerada pelo Services: " + getAvailableMatricesPermitted());
        }

        repository = getRepository();

        if (repository == null) {
            throw new IllegalArgumentException("Não foi possível encontrar o repositório utilizado nesta matrix.");
        }

        System.out.println("Selecionado matrix com " + getMatrix().getNodes().size() + " nodes.");
        
        final UndirectedSparseMultigraph<String, String> graphMulti = new UndirectedSparseMultigraph<>();
        
        final Map<String, Integer> edgeWeigth = new HashMap<>(getMatrix().getNodes().size());
        final Map<AuxFileFile, Set<String>> commitersPairFile = new HashMap<>();
        final Map<AuxFileFile, Integer> pairFileCommitCount = new HashMap<>();
        
        final Set<String> distinctDevelopers = new HashSet<>();
        final Set<String> distinctFiles = new HashSet<>();
        
        Set<AuxFileFile> pairFiles = new HashSet<>();
        Map<String, AuxFileFile> edgeFiles = new HashMap<>();
        for (int i = 0; i < getMatrix().getNodes().size(); i++) {
            EntityMatrixNode node = getMatrix().getNodes().get(i);
            String[] columns = node.getLine().split(JsfUtil.TOKEN_SEPARATOR);
            
            AuxFileFile pairFile = new AuxFileFile(columns[1], columns[2]);
            String edgeName = pairFile + "(" + i + ")";
            
            // to count unique files
            distinctFiles.add(pairFile.getFileName());
            distinctFiles.add(pairFile.getFileName2());
            
            pairFiles.add(pairFile);
            edgeFiles.put(edgeName, pairFile);
            edgeWeigth.put(edgeName, Integer.valueOf(columns[4]));
            
            if (pairFileCommitCount.containsKey(pairFile)) {
                pairFileCommitCount.put(pairFile, Integer.valueOf(columns[4]) + edgeWeigth.get(edgeName));
            } else {
                pairFileCommitCount.put(pairFile, Integer.valueOf(columns[4]));
            }
            
            String commiter1 = columns[0];
            String commiter2 = columns[3];
            
            // to count unique developers
            distinctDevelopers.add(commiter1);
            distinctDevelopers.add(commiter2);
            
            /** 
             * Extract all distinct developer that commit a pair of file 
             */
            if (commitersPairFile.containsKey(pairFile)) {
                Set<String> commiters = commitersPairFile.get(pairFile);
                commiters.add(commiter1);
                commiters.add(commiter2);
            } else {
                Set<String> commiters = new HashSet<>();
                commiters.add(commiter1);
                commiters.add(commiter2);
                commitersPairFile.put(pairFile, commiters);
            }
            /** 
             * The co-change network is constructed as follow: 
             * If developer A and developer B commits a pair file PF, then they are connected in network.
             */
            graphMulti.addEdge(edgeName, commiter1, commiter2, EdgeType.UNDIRECTED);
        }

        /** 
         * Calculates structural holes metrics for each developer on the co-change network based.
         */
        Map<String, StructuralHoleMetric<String>> metricsResult = 
                StructuralHolesCalculator.calculeStructuralHolesMetrics(graphMulti, edgeWeigth);
        
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
                StructuralHoleMetric<String> commiterMetric = metricsResult.get(commiter);
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
                            getBeginDate(), getEndDate());
            Long futureUpdates = 
                    calculeUpdates(pairFile.getFileName(), pairFile.getFileName2(), 
                            getFutureBeginDate(), getFutureEndDate());
            
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
                UserModifySamePairOfFileInDateServices.class.getName()
        );
    }
    
    private Long calculeUpdates(String fileName, String fileName2, Date beginDate, Date endDate) {
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
                + "EXISTS (SELECT f2 FROM EntityCommitFile f2 WHERE f2.repositoryCommit = rc AND f2.filename = :fileName2)";

        String[] bdParams = new String[]{
            "repo",
            "fileName",
            "fileName2",
            "beginDate",
            "endDate"
        };
        Object[] bdObjects = new Object[]{
            repository,
            fileName,
            fileName2,
            beginDate,
            endDate
        };

        return dao.selectOneWithParams(jpql, bdParams, bdObjects);
    }
    
    private Long calculeUpdates(String fileName, String fileName2, String commiter, String commiter2,
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
        return getDateParam("futureBeginDate");
    }

    public Date getFutureEndDate() {
        return getDateParam("futureEndDate");
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
