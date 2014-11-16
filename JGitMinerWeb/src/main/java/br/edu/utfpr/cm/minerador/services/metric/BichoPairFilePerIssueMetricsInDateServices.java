package br.edu.utfpr.cm.minerador.services.metric;

import br.edu.utfpr.cm.JGitMinerWeb.dao.AuxCodeChurn;
import br.edu.utfpr.cm.JGitMinerWeb.dao.AuxUser;
import br.edu.utfpr.cm.JGitMinerWeb.dao.BichoDAO;
import br.edu.utfpr.cm.JGitMinerWeb.dao.BichoFileDAO;
import br.edu.utfpr.cm.JGitMinerWeb.dao.BichoPairFileDAO;
import br.edu.utfpr.cm.JGitMinerWeb.dao.GenericBichoDAO;
import br.edu.utfpr.cm.JGitMinerWeb.model.matrix.EntityMatrix;
import br.edu.utfpr.cm.JGitMinerWeb.model.matrix.EntityMatrixNode;
import br.edu.utfpr.cm.JGitMinerWeb.services.matrix.UserCommentedSamePairOfFileInAllDateServices;
import br.edu.utfpr.cm.JGitMinerWeb.services.matrix.auxiliary.AuxFileFilePull;
import br.edu.utfpr.cm.JGitMinerWeb.services.matrix.auxiliary.AuxUserUserDirectional;
import br.edu.utfpr.cm.JGitMinerWeb.services.metric.auxiliary.AuxFileFileIssueMetrics;
import br.edu.utfpr.cm.JGitMinerWeb.services.metric.auxiliary.AuxWordiness;
import br.edu.utfpr.cm.JGitMinerWeb.services.metric.centrality.BetweennessCalculator;
import br.edu.utfpr.cm.JGitMinerWeb.services.metric.centrality.ClosenessCalculator;
import br.edu.utfpr.cm.JGitMinerWeb.services.metric.centrality.DegreeCalculator;
import br.edu.utfpr.cm.JGitMinerWeb.services.metric.discussion.WordinessCalculator;
import br.edu.utfpr.cm.JGitMinerWeb.services.metric.ego.EgoMeasure;
import br.edu.utfpr.cm.JGitMinerWeb.services.metric.ego.EgoMeasureCalculator;
import br.edu.utfpr.cm.JGitMinerWeb.services.metric.global.GlobalMeasure;
import br.edu.utfpr.cm.JGitMinerWeb.services.metric.global.GlobalMeasureCalculator;
import br.edu.utfpr.cm.JGitMinerWeb.services.metric.structuralholes.StructuralHolesCalculator;
import br.edu.utfpr.cm.JGitMinerWeb.services.metric.structuralholes.StructuralHolesMeasure;
import br.edu.utfpr.cm.JGitMinerWeb.util.DescriptiveStatisticsHelper;
import br.edu.utfpr.cm.JGitMinerWeb.util.JsfUtil;
import br.edu.utfpr.cm.JGitMinerWeb.util.JungExport;
import br.edu.utfpr.cm.JGitMinerWeb.util.OutLog;
import br.edu.utfpr.cm.JGitMinerWeb.util.PairUtils;
import br.edu.utfpr.cm.JGitMinerWeb.util.PathUtils;
import br.edu.utfpr.cm.minerador.services.matrix.BichoPairOfFileInDateServices;
import br.edu.utfpr.cm.minerador.services.matrix.BichoUserCommentedSamePairOfFileOnIssueInDateServices;
import br.edu.utfpr.cm.minerador.services.matrix.model.Commenter;
import edu.uci.ics.jung.graph.DirectedSparseGraph;
import edu.uci.ics.jung.graph.util.EdgeType;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.commons.lang3.BooleanUtils;

/**
 * 
 * @author Rodrigo T. Kuroda
 */
public class BichoPairFilePerIssueMetricsInDateServices extends AbstractBichoMetricServices {

    private String repository;

    public BichoPairFilePerIssueMetricsInDateServices() {
        super(null, null);
    }

    public BichoPairFilePerIssueMetricsInDateServices(GenericBichoDAO dao, OutLog out) {
        super(dao, out);
    }

    public BichoPairFilePerIssueMetricsInDateServices(GenericBichoDAO dao, EntityMatrix matrix, Map params, OutLog out) {
        super(dao, matrix, params, out);
    }

    private Integer getIntervalOfMonths() {
        return getIntegerParam("intervalOfMonths");
    }

    private Date getBeginDate() {
        if (getMatrix().getClassServicesName().equals(UserCommentedSamePairOfFileInAllDateServices.class.getName())) {
            return getDateParam("matrixBeginDate");
        }
        return getDateParam("beginDate");
    }

    private Date getEndDate() {
        if (getMatrix().getClassServicesName().equals(UserCommentedSamePairOfFileInAllDateServices.class.getName())) {
            return getDateParam("matrixEndDate");
        }
        return getDateParam("endDate");
    }

    public Date getFutureBeginDate() {
        return getDateParam("futureBeginDate");
    }

    public Date getFutureEndDate() {
        return getDateParam("futureEndDate");
    }

    @Override
    public void run() {

        if (getMatrix() == null
                || !getAvailableMatricesPermitted().contains(getMatrix().getClassServicesName())) {
            throw new IllegalArgumentException("Selecione uma matriz gerada pelo Services: " + getAvailableMatricesPermitted());
        }

        repository = getRepository();

        if (repository == null) {
            throw new IllegalArgumentException("Não foi possível encontrar o repositório utilizado nesta matriz.");
        }

        Date futureBeginDate = getFutureBeginDate();
        Date futureEndDate = getFutureEndDate();
        
        Date beginDate = getBeginDate();
        Date endDate = getEndDate();

        if (futureBeginDate == null && futureEndDate == null) {
            if (getIntervalOfMonths() == null || getIntervalOfMonths() == 0) {
                throw new IllegalArgumentException("A matriz selecionada não possui parâmetro de Interval Of Months, informe Future Begin Date e Future End Date.");
            }
            futureBeginDate = (Date) getEndDate().clone();
            Calendar cal = Calendar.getInstance();
            cal.setTime((Date) futureBeginDate.clone());
            cal.add(Calendar.MONTH, getIntervalOfMonths());
            futureEndDate = cal.getTime();
        }

        params.put("futureBeginDate", futureBeginDate);
        params.put("futureEndDate", futureEndDate);

        // file; file2; issueWeigth; issues; commitsWeight; commmits
        out.printLog("Iniciado cálculo da métrica de matriz com " + getMatrix().getNodes().size() + " nodes. Parametros: " + params);

        out.printLog("Iniciando construção da rede.");

        final Map<AuxFileFilePull, Set<AuxUser>> committersPairFile = new HashMap<>();
        final Map<AuxFileFilePull, Set<Commenter>> commentersPairFile = new HashMap<>();
        final Map<AuxFileFilePull, Set<Integer>> issuesPairFile = new HashMap<>();
        final Map<AuxFileFilePull, Set<Integer>> commitsPairFile = new HashMap<>();
        final Map<String, Integer> edgesWeigth = new HashMap<>();
        
        // rede de comunicação global, com todos pares de arquivos
        DirectedSparseGraph<String, String> globalGraph = new DirectedSparseGraph<>();
        
        // rede de comunicação de cada par de arquivo
        Map<AuxFileFilePull, DirectedSparseGraph<String, String>> pairFileNetwork = new HashMap<>();
        
        int countIgnored = 0;
        final int maxFilePerCommit = 20;
        BichoDAO bichoDAO = new BichoDAO(dao, repository);
        BichoFileDAO bichoFileDAO = new BichoFileDAO(dao, repository, maxFilePerCommit);
        BichoPairFileDAO bichoPairFileDAO = new BichoPairFileDAO(dao, repository, maxFilePerCommit);
        Long issuesSize = bichoPairFileDAO
                .calculeNumberOfIssues(futureBeginDate, futureEndDate, true);
        
        System.out.println("Number of all pull requests: " + issuesSize);
        
        // construindo a rede de comunicação para cada par de arquivo (desenvolvedores que comentaram)
        int nodesSize = getMatrix().getNodes().size();
        int count = 0;
        Set<AuxFileFilePull> pairFilesSet = new HashSet<>();
        for (int i = 0; i < getMatrix().getNodes().size(); i++) {
            if (count++ % 100 == 0 || count == nodesSize) {
                System.out.println(count + "/" + nodesSize);
            }
            EntityMatrixNode node = getMatrix().getNodes().get(i);
            String[] columns = node.getLine().split(JsfUtil.TOKEN_SEPARATOR);

            final String filename1 = columns[0];
            final String filename2 = columns[1];

            final Integer issueWeight = Integer.valueOf(columns[2]);
            final Set<Integer> issues = toIntegerList(columns[3]);

            final Integer commitWeight = Integer.valueOf(columns[4]);
            final Set<Integer> commits = toIntegerList(columns[5]);

            if (issues.isEmpty()) {
                out.printLog("No issues for pair file " + filename1 + ";" + filename2);
            }
            // Group pair files by issue
            for (Integer issue : issues) {
                AuxFileFilePull pairFile = new AuxFileFilePull(filename1, filename2, issue);
                pairFilesSet.add(pairFile);

                if (commitsPairFile.containsKey(pairFile)) {
                    commitsPairFile.get(pairFile).addAll(commits);
                } else {
                    commitsPairFile.put(pairFile, commits);
                }

                // in this case (per issue), there is one issue
                if (issuesPairFile.containsKey(pairFile)) {
                    issuesPairFile.get(pairFile).add(issue);
                } else {
                    Set<Integer> issuesSet = new HashSet<>();
                    issuesSet.add(issue);
                    issuesPairFile.put(pairFile, issuesSet);
                }

                // TODO optimize querying at matrix generation
                // Find the pair files committers
                Set<AuxUser> pairFileCommitters = bichoPairFileDAO.selectCommitters(issue, filename1, filename2);

                /**
                 * Extract all distinct developer that commit a pair of file
                 */
                if (committersPairFile.containsKey(pairFile)) {
                    Set<AuxUser> commiters = committersPairFile.get(pairFile);
                    commiters.addAll(pairFileCommitters);
                } else {
                    committersPairFile.put(pairFile, pairFileCommitters);
                }

                List<Commenter> commenters = bichoDAO.selectCommentersByIssueId(issue);

                /**
                 * Extract all distinct commenter of issue that pair of file was
                 * committed
                 */
                if (commentersPairFile.containsKey(pairFile)) {
                    commentersPairFile.get(pairFile).addAll(commenters);
                } else {
                    commentersPairFile.put(pairFile, new HashSet<>(commenters));
                }

                if (commenters.isEmpty()) {
                    out.printLog("No commenters for issue " + issue + " pair file " + filename1 + ";" + filename2);
                } else if (commenters.size() == 1) {
                    DirectedSparseGraph<String, String> graphMulti
                            = new DirectedSparseGraph<>();
                    graphMulti.addVertex(commenters.get(0).getName());
                    pairFileNetwork.put(pairFile, graphMulti);
                } else {
                    Map<AuxUserUserDirectional, AuxUserUserDirectional> pairCommenters
                            = PairUtils.pairCommenters(commenters);

                    if (pairCommenters.isEmpty()) {
                        DirectedSparseGraph<String, String> graphMulti
                                = new DirectedSparseGraph<>();
                        graphMulti.addVertex(commenters.get(0).getName());
                        pairFileNetwork.put(pairFile, graphMulti);
                        continue;
                    }

                    for (AuxUserUserDirectional pairUser : pairCommenters.keySet()) {

                        // adiciona conforme o peso
                        //  String edgeName = pairFile.getFileName() + "-" + pairFile.getFileName2() + "-" + i;

                        /* Sum commit for each pair file that the pair devCommentter has commited. */
                        // user > user2 - directed edge
                        if (edgesWeigth.containsKey(pairUser.toStringDirectional())) {
                            // edgeName = user + user2
                            edgesWeigth.put(pairUser.toStringDirectional(), edgesWeigth.get(pairUser.toStringDirectional()) + pairUser.getWeigth());
                            //            // for undirectional globalGraph
                            //            } else if (edgesWeigth.containsKey(pairUser.toStringUser2AndUser())) {
                            //                // edgeName = user2 + user - undirected edge
                            //                edgesWeigth.put(pairUser.toStringUser2AndUser(), edgesWeigth.get(pairUser.toStringUser2AndUser()) + weight);
                        } else {
                            edgesWeigth.put(pairUser.toStringDirectional(), pairUser.getWeigth());
                        }

                        if (!globalGraph.containsVertex(pairUser.getUser())
                                || !globalGraph.containsVertex(pairUser.getUser2())
                                || !globalGraph.containsEdge(pairUser.toStringDirectional())) {
                            globalGraph.addEdge(pairUser.toStringDirectional(), pairUser.getUser(), pairUser.getUser2(), EdgeType.DIRECTED);
                        }

                        // check if network already created
                        if (pairFileNetwork.containsKey(pairFile)) {
                            pairFileNetwork.get(pairFile)
                                    .addEdge(pairUser.toStringDirectional(), pairUser.getUser(), pairUser.getUser2(), EdgeType.DIRECTED);
                        } else {
                            DirectedSparseGraph<String, String> graphMulti
                                    = new DirectedSparseGraph<>();
                            graphMulti.addEdge(pairUser.toStringDirectional(), pairUser.getUser(), pairUser.getUser2(), EdgeType.DIRECTED);
                            pairFileNetwork.put(pairFile, graphMulti);
                        }
                    }
                }
            }
        }
        SimpleDateFormat format = new SimpleDateFormat("dd.MM.yyyy");
        JungExport.exportToImage(globalGraph, "C:/Users/a562273/Desktop/networks/",
                repository + " Single " + format.format(beginDate) + " a " + format.format(endDate));
        
        out.printLog("Número de pares de arquivos ignoradoa: " + countIgnored);
        
        out.printLog("Número de autores de comentários (commenters): " + globalGraph.getVertexCount());
        out.printLog("Número de pares de arquivos (committers): " + committersPairFile.size());
        out.printLog("Número de pares de arquivos (issues): " + issuesPairFile.size());
        out.printLog("Número de pares de arquivos (commenters): " + commentersPairFile.size());
        out.printLog("Número de pares de arquivos distintos: " + pairFilesSet.size());
        out.printLog("Iniciando cálculo das métricas.");

        Set<AuxFileFileIssueMetrics> fileFileMetrics = new HashSet<>();
        
        out.printLog("Calculando metricas SNA...");

        GlobalMeasure global = GlobalMeasureCalculator.calcule(globalGraph);
        out.printLog("Global measures: " + global.toString());

        // number of pull requests in date interval
        Long numberOfAllPullrequestFuture = issuesSize;
        // cache for optimization number of pull requests where file is in,
        // reducing access to database
        Map<String, Long> issueFileMap = new HashMap<>();
        // cache for optimization file code churn (add, del, change),
        // reducing access to database
        Map<String, AuxCodeChurn> codeChurnRequestFileMap = new HashMap<>();
        Map<String, AuxCodeChurn> cummulativeCodeChurnRequestFileMap = new HashMap<>();
        // cache for optimization file commits made by user,
        // reducing access to database
        Map<String, AuxCodeChurn> fileUserCommitMap = new HashMap<>();

        out.printLog("Calculando somas, máximas, médias, updates, code churn e apriori para cada par de arquivos...");
        count = 0;
        final int size = committersPairFile.entrySet().size();
        out.printLog("Número de pares de arquivos: " + commentersPairFile.keySet().size());
        for (Map.Entry<AuxFileFilePull, Set<Commenter>> entry : commentersPairFile.entrySet()) {
            if (count++ % 10 == 0 || count == size) {
                System.out.println(count + "/" + size);
            }
            AuxFileFilePull fileFile = entry.getKey();
            Set<Commenter> devsCommentters = entry.getValue();

            // pair file network
            final DirectedSparseGraph<String, String> pairFileGraph = pairFileNetwork.get(fileFile);
            GlobalMeasure pairFileGlobal = GlobalMeasureCalculator.calcule(pairFileGraph);

            // Map<String, Double> barycenter = BarycenterCalculator.calcule(pairFileGraph, edgesWeigth);
            Map<String, Double> betweenness = BetweennessCalculator.calcule(pairFileGraph, edgesWeigth);
            Map<String, Double> closeness = ClosenessCalculator.calcule(pairFileGraph, edgesWeigth);
            Map<String, Integer> degree = DegreeCalculator.calcule(pairFileGraph);
            // Map<String, Double> eigenvector = EigenvectorCalculator.calcule(pairFileGraph, edgesWeigth);
            Map<String, EgoMeasure<String>> ego = EgoMeasureCalculator.calcule(pairFileGraph, edgesWeigth);
            Map<String, StructuralHolesMeasure<String>> structuralHoles = StructuralHolesCalculator.calcule(pairFileGraph, edgesWeigth);

//            DescriptiveStatisticsHelper barycenterStatistics = new DescriptiveStatisticsHelper(devsCommentters.isEmpty() ? 1 : devsCommentters.size());
            DescriptiveStatisticsHelper betweennessStatistics = new DescriptiveStatisticsHelper(devsCommentters.isEmpty() ? 1 : devsCommentters.size());
            DescriptiveStatisticsHelper closenessStatistics = new DescriptiveStatisticsHelper(devsCommentters.isEmpty() ? 1 : devsCommentters.size());
            DescriptiveStatisticsHelper degreeStatistics = new DescriptiveStatisticsHelper(devsCommentters.isEmpty() ? 1 : devsCommentters.size());
//            DescriptiveStatisticsHelper eigenvectorStatistics = new DescriptiveStatisticsHelper(devsCommentters.isEmpty() ? 1 : devsCommentters.size());
            DescriptiveStatisticsHelper egoBetweennessStatistics = new DescriptiveStatisticsHelper(devsCommentters.isEmpty() ? 1 : devsCommentters.size());
            DescriptiveStatisticsHelper egoSizeStatistics = new DescriptiveStatisticsHelper(devsCommentters.isEmpty() ? 1 : devsCommentters.size());
//            DescriptiveStatisticsHelper egoPairsStatistics = new DescriptiveStatisticsHelper(devsCommentters.isEmpty() ? 1 : devsCommentters.size());
            DescriptiveStatisticsHelper egoTiesStatistics = new DescriptiveStatisticsHelper(devsCommentters.isEmpty() ? 1 : devsCommentters.size());
            DescriptiveStatisticsHelper egoDensityStatistics = new DescriptiveStatisticsHelper(devsCommentters.isEmpty() ? 1 : devsCommentters.size());
            DescriptiveStatisticsHelper efficiencyStatistics = new DescriptiveStatisticsHelper(devsCommentters.isEmpty() ? 1 : devsCommentters.size());
            DescriptiveStatisticsHelper effectiveSizeStatistics = new DescriptiveStatisticsHelper(devsCommentters.isEmpty() ? 1 : devsCommentters.size());
            DescriptiveStatisticsHelper constraintStatistics = new DescriptiveStatisticsHelper(devsCommentters.isEmpty() ? 1 : devsCommentters.size());
            DescriptiveStatisticsHelper hierarchyStatistics = new DescriptiveStatisticsHelper(devsCommentters.isEmpty() ? 1 : devsCommentters.size());

            for (Commenter user : devsCommentters) {
                String commenter = user.getName();

//                barycenterStatistics.addValue(barycenter.get(commenter));
                betweennessStatistics.addValue(betweenness.get(commenter));
                closenessStatistics.addValue(closeness.get(commenter));
                degreeStatistics.addValue(degree.get(commenter));
//                eigenvectorStatistics.addValue(eigenvector.get(commenter));

                final EgoMeasure<String> egoMetrics = ego.get(commenter);
                egoBetweennessStatistics.addValue(egoMetrics.getBetweennessCentrality());
                egoSizeStatistics.addValue(egoMetrics.getSize());
//                egoPairsStatistics.addValue(ego.get(commenter).getPairs());
                egoTiesStatistics.addValue(egoMetrics.getTies());
                egoDensityStatistics.addValue(egoMetrics.getDensity());

                final StructuralHolesMeasure<String> structuralHolesMetric = structuralHoles.get(commenter);
                efficiencyStatistics.addValue(structuralHolesMetric.getEfficiency());
                effectiveSizeStatistics.addValue(structuralHolesMetric.getEffectiveSize());
                constraintStatistics.addValue(structuralHolesMetric.getConstraint());
                hierarchyStatistics.addValue(structuralHolesMetric.getHierarchy());
            }
            Integer distinctCommentersCount = devsCommentters.size();
            
            // Commit-based metrics ////////////////////////////////////////////
            final Set<Integer> fileFileIssues = issuesPairFile.get(fileFile);

            if (fileFileIssues == null || fileFileIssues.isEmpty()) {
                out.printLog("Empty issues for " + fileFile.toString());
            }

            final long changes = calculeFileCodeChurn(codeChurnRequestFileMap,
                    fileFile.getFileName(), bichoFileDAO, beginDate, endDate, null).getChanges();
            final long changes2 = calculeFileCodeChurn(codeChurnRequestFileMap,
                    fileFile.getFileName2(), bichoFileDAO, beginDate, endDate, null).getChanges();

            Set<AuxUser> devsCommitters = bichoPairFileDAO.selectCommitters(
                    fileFile.getFileName(), fileFile.getFileName2(), beginDate, endDate, fileFileIssues);

            DescriptiveStatisticsHelper devCommitsStatistics = new DescriptiveStatisticsHelper();
            DescriptiveStatisticsHelper ownershipStatistics = new DescriptiveStatisticsHelper();
            Long minorContributors = 0l, majorContributors = 0l;
            Double ownerExperience = 0.0d, ownerExperience2 = 0.0d,
                    cummulativeOwnerExperience = 0.0d, cummulativeOwnerExperience2 = 0.0d;

            long committers = devsCommitters.size();
            long distinctCommitters = bichoPairFileDAO.calculeCommitters(
                    fileFile.getFileName(), fileFile.getFileName2(), null, endDate, fileFileIssues);

            Long commits = bichoPairFileDAO.calculeCommits(fileFile.getFileName(), fileFile.getFileName2(),
                    beginDate, endDate, fileFileIssues);

            for (AuxUser devCommitter : devsCommitters) {
                Long devCommits = bichoPairFileDAO.calculeCommits(fileFile.getFileName(), fileFile.getFileName2(), devCommitter.getUser(),
                        beginDate, endDate, fileFileIssues);
                devCommitsStatistics.addValue(devCommits);

                Double ownership = devCommits.doubleValue() / commits.doubleValue();
                ownershipStatistics.addValue(ownership);

                if (ownership <= 0.05) { // menor ou igual que 5% = minor
                    minorContributors++;
                } else { // maior que 5% = major
                    majorContributors++;
                }

                // Calculing OEXP of each file
                Double experience = calculeDevFileExperience(changes, fileUserCommitMap,
                        fileFile.getFileName(), devCommitter.getUser(), bichoFileDAO, beginDate, endDate, fileFileIssues);
                ownerExperience = Math.max(experience, ownerExperience);

                Double experience2 = calculeDevFileExperience(changes2, fileUserCommitMap,
                        fileFile.getFileName2(), devCommitter.getUser(), bichoFileDAO, beginDate, endDate, fileFileIssues);
                ownerExperience2 = Math.max(experience2, ownerExperience2);

                // Calculing OWN
                final long cummulativeChanges = calculeFileCodeChurn(cummulativeCodeChurnRequestFileMap,
                        fileFile.getFileName(), bichoFileDAO, null, endDate, fileFileIssues).getChanges();
                final long cummulativeChanges2 = calculeFileCodeChurn(cummulativeCodeChurnRequestFileMap,
                        fileFile.getFileName2(), bichoFileDAO, null, endDate, fileFileIssues).getChanges();

                Double cumulativeExperience = calculeDevFileExperience(cummulativeChanges, fileUserCommitMap,
                        fileFile.getFileName(), devCommitter.getUser(), bichoFileDAO, null, endDate, fileFileIssues);
                cummulativeOwnerExperience = Math.max(cummulativeOwnerExperience, cumulativeExperience);

                Double cumulativeExperience2 = calculeDevFileExperience(cummulativeChanges2, fileUserCommitMap,
                        fileFile.getFileName2(), devCommitter.getUser(), bichoFileDAO, null, endDate, fileFileIssues);
                cummulativeOwnerExperience2 = Math.max(cummulativeOwnerExperience2, cumulativeExperience2);

            }

//            double majorContributorsRate = (double) majorContributors / (double) committers; // % de major
//            double minorContributorsRate = (double) minorContributors / (double) committers; // % de minor

            Long updates = (long) fileFileIssues.size();
//                    bichoPairFileDAO.calculeNumberOfIssues(
//                    fileFile.getFileName(), fileFile.getFileName2(),
//                    beginDate, endDate, true);

            Long futureUpdates;
            if (beginDate.equals(futureBeginDate) && endDate.equals(futureEndDate)) {
                futureUpdates = updates;
            } else {
                futureUpdates = bichoPairFileDAO.calculeNumberOfIssues(
                        fileFile.getFileName(), fileFile.getFileName2(),
                        futureBeginDate, futureEndDate, true);
            }

            // list all issues and its comments
            Collection<AuxWordiness> issuesAndComments
                    = bichoPairFileDAO.listIssues(fileFile.getFileName(), fileFile.getFileName2(), beginDate, endDate, fileFileIssues);

            long wordiness = 0;
            long commentsSum = 0;
            for (AuxWordiness auxWordiness : issuesAndComments) {
                wordiness += WordinessCalculator.calcule(auxWordiness);
                commentsSum += auxWordiness.getComments().size();
            }

            final long codeChurn = calculeFileCodeChurn(codeChurnRequestFileMap,
                    fileFile.getFileName(), bichoFileDAO, beginDate, endDate, null).getChanges();
            final long codeChurn2 = calculeFileCodeChurn(codeChurnRequestFileMap,
                    fileFile.getFileName2(), bichoFileDAO, beginDate, endDate, null).getChanges();

            AuxCodeChurn pairFileCodeChurn = bichoPairFileDAO.calculeCodeChurnAddDelChange(
                    fileFile.getFileName2(), fileFile.getFileName(),
                    beginDate, endDate, fileFileIssues);

            double codeChurnAvg = (codeChurn + codeChurn2) / 2.0d;

            // pair file age in release interval (days)
            int ageRelease = bichoPairFileDAO.calculePairFileDaysAge( fileFile.getFileName(), fileFile.getFileName2(), beginDate, endDate, true);

            // pair file age in total until final date (days)
            int ageTotal = bichoPairFileDAO.calculePairFileDaysAge( fileFile.getFileName(), fileFile.getFileName2(), null, endDate, true);

            boolean samePackage = PathUtils.isSameFullPath(fileFile.getFileName(), fileFile.getFileName2());

            AuxFileFileIssueMetrics auxFileFileMetrics = new AuxFileFileIssueMetrics(
                    fileFile.getFileName(), fileFile.getFileName2(),
                    fileFile.getPullNumber(),
                    BooleanUtils.toInteger(samePackage),
                    // barycenterSum, barycenterAvg, barycenterMax,
                    betweennessStatistics.getSum(), betweennessStatistics.getMean(), betweennessStatistics.getMedian(), betweennessStatistics.getMax(),
                    closenessStatistics.getSum(), closenessStatistics.getMean(), closenessStatistics.getMedian(), closenessStatistics.getMax(),
                    degreeStatistics.getSum(), degreeStatistics.getMean(), degreeStatistics.getMedian(), degreeStatistics.getMax(),
                    // eigenvectorSum, eigenvectorAvg, eigenvectorMax,

                    egoBetweennessStatistics.getSum(), egoBetweennessStatistics.getMean(), egoBetweennessStatistics.getMedian(), egoBetweennessStatistics.getMax(),
                    egoSizeStatistics.getSum(), egoSizeStatistics.getMean(), egoSizeStatistics.getMedian(), egoSizeStatistics.getMax(),
                    egoTiesStatistics.getSum(), egoTiesStatistics.getMean(), egoTiesStatistics.getMedian(), egoTiesStatistics.getMax(),
                    // egoPairsSum, egoPairsAvg, egoPairsMax,
                    egoDensityStatistics.getSum(), egoDensityStatistics.getMean(), egoDensityStatistics.getMedian(), egoDensityStatistics.getMax(),
                    efficiencyStatistics.getSum(), efficiencyStatistics.getMean(), efficiencyStatistics.getMedian(), efficiencyStatistics.getMax(),
                    effectiveSizeStatistics.getSum(), effectiveSizeStatistics.getMean(), effectiveSizeStatistics.getMedian(), effectiveSizeStatistics.getMax(),
                    constraintStatistics.getSum(), constraintStatistics.getMean(), constraintStatistics.getMedian(), constraintStatistics.getMax(),
                    hierarchyStatistics.getSum(), hierarchyStatistics.getMean(), hierarchyStatistics.getMedian(), hierarchyStatistics.getMax(),
                    pairFileGlobal.getSize(), pairFileGlobal.getTies(),
                    pairFileGlobal.getDensity(), pairFileGlobal.getDiameter(), 
                    devCommitsStatistics.getSum(), devCommitsStatistics.getMean(), devCommitsStatistics.getMedian(), devCommitsStatistics.getMax(),
                    ownershipStatistics.getSum(), ownershipStatistics.getMean(), ownershipStatistics.getMedian(), ownershipStatistics.getMax(),
                    majorContributors, minorContributors,
                    ownerExperience, ownerExperience2,
                    cummulativeOwnerExperience, cummulativeOwnerExperience2,
                    committers, distinctCommitters, commits,
                    distinctCommentersCount, commentsSum, wordiness,
                    codeChurn, codeChurn2, codeChurnAvg,
                    pairFileCodeChurn.getAdditionsNormalized(), pairFileCodeChurn.getDeletionsNormalized(), pairFileCodeChurn.getChanges(),
                    ageRelease, ageTotal, updates, futureUpdates
            );

            // apriori /////////////////////////////////////////////////////////
            Long fileNumberOfPullrequestOfPairFuture
                    = calculeNumberOfIssues(issueFileMap, auxFileFileMetrics.getFile(), bichoFileDAO, futureBeginDate, futureEndDate);

            Long file2NumberOfPullrequestOfPairFuture
                    = calculeNumberOfIssues(issueFileMap, auxFileFileMetrics.getFile2(), bichoFileDAO, futureBeginDate, futureEndDate);

            auxFileFileMetrics.addMetrics(fileNumberOfPullrequestOfPairFuture, file2NumberOfPullrequestOfPairFuture, numberOfAllPullrequestFuture);

            Double supportFile = fileNumberOfPullrequestOfPairFuture.doubleValue() / numberOfAllPullrequestFuture.doubleValue();
            Double supportFile2 = file2NumberOfPullrequestOfPairFuture.doubleValue() / numberOfAllPullrequestFuture.doubleValue();
            Double supportPairFile = futureUpdates.doubleValue() / numberOfAllPullrequestFuture.doubleValue();
            Double confidence = supportFile == 0 ? 0d : supportPairFile / supportFile;
            Double confidence2 = supportFile2 == 0 ? 0d : supportPairFile / supportFile2;
            Double lift = supportFile * supportFile2 == 0 ? 0d : supportPairFile / (supportFile * supportFile2);
            Double conviction = 1 - confidence == 0 ? 0d : (1 - supportFile) / (1 - confidence);
            Double conviction2 = 1 - confidence2 == 0 ? 0d : (1 - supportFile2) / (1 - confidence2);

            auxFileFileMetrics.addMetrics(
                    supportFile, supportFile2, supportPairFile,
                    confidence, confidence2,
                    lift,
                    conviction, conviction2
            );

            fileFileMetrics.add(auxFileFileMetrics);
        }

        out.printLog("Número de pares de arquivos: " + fileFileMetrics.size());
        addToEntityMetricNodeList(fileFileMetrics);
    }

    public Long calculeNumberOfIssues(Map<String, Long> issueFileMap, String fileName, BichoFileDAO fileDAO, Date futureBeginDate, Date futureEndDate) {
        Long fileNumberOfPullrequestOfPairFuture;
        if (issueFileMap.containsKey(fileName)) {
            fileNumberOfPullrequestOfPairFuture = issueFileMap.get(fileName);
        } else {
            fileNumberOfPullrequestOfPairFuture = fileDAO.calculeNumberOfIssues(fileName, futureBeginDate, futureEndDate, true);
            issueFileMap.put(fileName, fileNumberOfPullrequestOfPairFuture);
        }
        return fileNumberOfPullrequestOfPairFuture;
    }

    public AuxCodeChurn calculeFileCodeChurn(Map<String, AuxCodeChurn> codeChurnRequestFileMap,
            String fileName, BichoFileDAO fileDAO, Date beginDate, Date endDate, Collection<Integer> issues) {
        if (codeChurnRequestFileMap.containsKey(fileName)) { // cached
            return codeChurnRequestFileMap.get(fileName);
        } else {
            AuxCodeChurn sumCodeChurnFile = fileDAO.sumCodeChurnByFilename(fileName, beginDate, endDate, issues);
            codeChurnRequestFileMap.put(fileName, sumCodeChurnFile);
            return sumCodeChurnFile;
        }
    }

    public double calculeDevFileExperience(final Long changes, Map<String, AuxCodeChurn> fileUserCommitMap,
            String fileName, String user, BichoFileDAO fileDAO, Date beginDate, Date endDate, Collection<Integer> issues) {
        final long devChanges;
        if (fileUserCommitMap.containsKey(fileName)) { // cached
            AuxCodeChurn sumCodeChurnFile = fileUserCommitMap.get(fileName);
            devChanges = sumCodeChurnFile.getChanges();
        } else {
            AuxCodeChurn sumCodeChurnFile = fileDAO.sumCodeChurnByFilename(fileName, user, beginDate, endDate, issues);
            fileUserCommitMap.put(fileName, sumCodeChurnFile);
            devChanges = sumCodeChurnFile.getChanges();
        }

        return changes == 0 ? 0.0d : (double) devChanges / (double) changes;
    }

    @Override
    public String getHeadCSV() {
        return "file;file2;"
                + "issue;" // 1 issue da ocorrencia do par
                + "samePackage;" // arquivos do par são do mesmo pacote = 1, caso contrário 0
                // + "brcAvg;brcSum;brcMax;"
                + "btwSum;btwAvg;btwMdn;btwMax;"
                + "clsSum;clsAvg;clsMdn;clsMax;"
                + "dgrSum;dgrAvg;dgrMdn;dgrMax;"
                //+ "egvSum;egvAvg;egvMax;"
                + "egoBtwSum;egoBtwAvg;egoBtwMdn;egoBtwMax;"
                + "egoSizeSum;egoSizeAvg;egoSizeMdn;egoSizeMax;"
                + "egoTiesSum;egoTiesAvg;egoTiesMdn;egoTiesMax;"
                // + "egoPairsSum;egoPairsAvg;egoPairsMax;"
                + "egoDensitySum;egoDensityAvg;egoDensityMdn;egoDensityMax;"
                + "efficiencySum;efficiencyAvg;efficiencyMdn;efficiencyMax;"
                + "efvSizeSum;efvSizeAvg;efvSizeMdn;efvSizeMax;"
                + "constraintSum;constraintAvg;constraintMdn;constraintMax;"
                + "hierarchySum;hierarchyAvg;hierarchyMdn;hierarchyMax;"
                + "size;ties;density;diameter;"
                + "devCommitsSum;devCommitsAvg;devCommitsMdn;devCommitsMax;"
                + "ownershipSum;ownershipAvg;ownershipMdn;ownershipMax;"
                + "majorContributors;minorContributors;"
                + "oexp;oexp2;"
                + "own;own2;"
                + "adev;" // committers na relese
                + "ddev;" // committers desde o começo até a data final da relese
                + "commits;" // commits do par de arquivos
                + "commenters;comments;wordiness;"
                + "codeChurn;codeChurn2;codeChurnAvg;"
                + "add;del;changes;"
                + "ageRelease;ageTotal;"
                + "updates;futureUpdates;"
                + "fileChangeFuture;file2ChangeFuture;allPullrequestFuture;"
                + "supportFile;supportFile2;supportPairFile;confidence;confidence2;lift;conviction;conviction2";
    }

    @Override
    public List<String> getAvailableMatricesPermitted() {
        return Arrays.asList(BichoUserCommentedSamePairOfFileOnIssueInDateServices.class.getName(),
                BichoPairOfFileInDateServices.class.getName());
    }

    private String getRepository() {
        return getMatrix().getRepository();
    }

    private Set<Integer> toIntegerList(String value) {
        String values[] = value.split(",");
        Set<Integer> list = new HashSet<>(values.length);
        for (String integerValue : values) {
            list.add(Integer.valueOf(integerValue));
        }
        return list;
    }
}
