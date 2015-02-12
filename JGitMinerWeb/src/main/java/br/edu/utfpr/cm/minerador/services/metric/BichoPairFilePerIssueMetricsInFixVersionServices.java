package br.edu.utfpr.cm.minerador.services.metric;

import br.edu.utfpr.cm.JGitMinerWeb.dao.AuxCodeChurn;
import br.edu.utfpr.cm.JGitMinerWeb.dao.AuxUser;
import br.edu.utfpr.cm.JGitMinerWeb.dao.BichoDAO;
import br.edu.utfpr.cm.JGitMinerWeb.dao.BichoFileDAO;
import br.edu.utfpr.cm.JGitMinerWeb.dao.BichoPairFileDAO;
import br.edu.utfpr.cm.JGitMinerWeb.dao.GenericBichoDAO;
import br.edu.utfpr.cm.JGitMinerWeb.dao.GenericDao;
import br.edu.utfpr.cm.JGitMinerWeb.model.matrix.EntityMatrix;
import br.edu.utfpr.cm.JGitMinerWeb.model.matrix.EntityMatrixNode;
import br.edu.utfpr.cm.JGitMinerWeb.model.metric.EntityMetric;
import br.edu.utfpr.cm.JGitMinerWeb.services.matrix.auxiliary.AuxFileFile;
import br.edu.utfpr.cm.JGitMinerWeb.services.matrix.auxiliary.AuxFileFilePull;
import br.edu.utfpr.cm.JGitMinerWeb.services.matrix.auxiliary.AuxUserUserDirectional;
import br.edu.utfpr.cm.JGitMinerWeb.services.metric.auxiliary.AuxFileFileIssueMetrics;
import br.edu.utfpr.cm.JGitMinerWeb.services.metric.auxiliary.IssueMetrics;
import br.edu.utfpr.cm.JGitMinerWeb.services.metric.global.GlobalMeasure;
import br.edu.utfpr.cm.JGitMinerWeb.services.metric.global.GlobalMeasureCalculator;
import br.edu.utfpr.cm.JGitMinerWeb.util.DescriptiveStatisticsHelper;
import br.edu.utfpr.cm.JGitMinerWeb.util.JsfUtil;
import br.edu.utfpr.cm.JGitMinerWeb.util.JungExport;
import br.edu.utfpr.cm.JGitMinerWeb.util.OutLog;
import br.edu.utfpr.cm.JGitMinerWeb.util.PairUtils;
import br.edu.utfpr.cm.JGitMinerWeb.util.PathUtils;
import br.edu.utfpr.cm.minerador.services.matrix.BichoPairOfFileInFixVersionServices;
import br.edu.utfpr.cm.minerador.services.matrix.model.Commenter;
import br.edu.utfpr.cm.minerador.services.matrix.model.FilePairApriori;
import static br.edu.utfpr.cm.minerador.services.metric.AbstractBichoMetricServices.objectsToNodes;
import edu.uci.ics.jung.graph.DirectedSparseGraph;
import edu.uci.ics.jung.graph.util.EdgeType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.commons.lang3.BooleanUtils;

/**
 *
 * @author Rodrigo T. Kuroda
 */
public class BichoPairFilePerIssueMetricsInFixVersionServices extends AbstractBichoMetricServices {

    private String repository;

    public BichoPairFilePerIssueMetricsInFixVersionServices() {
        super();
    }

    public BichoPairFilePerIssueMetricsInFixVersionServices(GenericBichoDAO dao, GenericDao genericDao, EntityMatrix matrix, Map<Object, Object> params, OutLog out) {
        super(dao, genericDao, matrix, params, out);
    }

    private Integer getIntervalOfMonths() {
        return getIntegerParam("intervalOfMonths");
    }

    private String getVersion() {
        return getStringParam("version");
    }

    public String getFutureVersion() {
        return getStringParam("futureVersion");
    }

    @Override
    public void run() {
        repository = getRepository();
        final Date started = new Date();

        final String fixVersion = getVersion();
        final String futureVersion = getFutureVersion();

        // file; file2; issueWeigth; issues; commitsWeight; commmits
        out.printLog("Iniciado cálculo da métrica de matriz com " + getMatrix().getNodes().size() + " nodes. Parametros: " + params);

        final Map<AuxFileFilePull, Set<AuxUser>> committersPairFile = new HashMap<>();
        final Map<Integer, Set<Commenter>> devCommentersPairFile = new HashMap<>();
        final Map<Integer, Set<Commenter>> commentersPairFile = new HashMap<>();
        final Map<AuxFileFilePull, Set<Integer>> issuesPairFileMap = new HashMap<>();
        final Map<AuxFileFilePull, Set<Integer>> commitsPairFile = new HashMap<>();
        final Map<AuxFileFile, Set<Integer>> allPairFileIssuesMap = new HashMap<>();
        final Map<AuxFileFile, Integer> futureDefectsPairFile = new HashMap<>();
        final Map<String, Integer> edgesWeigth = new HashMap<>();
        final Map<Integer, Set<String>> distinctFilesPerIssueMap = new HashMap<>();
        final Set<Integer> noCommenters = new HashSet<>();

        // rede de comunicação global, com todos pares de arquivos
        DirectedSparseGraph<String, String> globalGraph = new DirectedSparseGraph<>();

        // rede de comunicação de cada par de arquivo
        final Map<Integer, DirectedSparseGraph<String, String>> pairFileNetwork = new HashMap<>();

        int countIgnored = 0;
        final int maxFilePerCommit = 20;
        final BichoDAO bichoDAO = new BichoDAO(dao, repository);
        final BichoFileDAO bichoFileDAO = new BichoFileDAO(dao, repository, maxFilePerCommit);
        final BichoPairFileDAO bichoPairFileDAO = new BichoPairFileDAO(dao, repository, maxFilePerCommit);
        final Long issuesSize = bichoPairFileDAO
                .calculeNumberOfIssues(fixVersion, true);

        System.out.println("Number of all pull requests: " + issuesSize);

        // construindo a rede de comunicação para cada par de arquivo (desenvolvedores que comentaram)
        final int nodesSize = getMatrix().getNodes().size();
        int count = 0;
        final Set<AuxFileFilePull> pairFilesSet = new HashSet<>();
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

            final Integer defectsWeight = Integer.valueOf(columns[6]);
            final Set<Integer> defects = toIntegerList(columns[7]);

            final Integer futureDefectsWeight = Integer.valueOf(columns[8]);
            final Set<Integer> futureDefects = toIntegerList(columns[9]);


            if (issues.isEmpty()) {
                out.printLog("No issues for pair file " + filename1 + ";" + filename2);
            }

            AuxFileFile fileFile = new AuxFileFile(filename1, filename2);
            if (allPairFileIssuesMap.containsKey(fileFile)) {
                allPairFileIssuesMap.get(fileFile).addAll(issues);
            } else {
                allPairFileIssuesMap.put(fileFile, issues);
            }

            for (Integer issue : issues) {
                AuxFileFilePull pairFileIssue = new AuxFileFilePull(filename1, filename2, issue);
                pairFilesSet.add(pairFileIssue);

                if (distinctFilesPerIssueMap.containsKey(issue)) {
                    final Set<String> hashSet = distinctFilesPerIssueMap.get(issue);
                    hashSet.add(filename1);
                    hashSet.add(filename2);
                } else {
                    final Set<String> hashSet = new HashSet<>();
                    distinctFilesPerIssueMap.put(issue, hashSet);
                    hashSet.add(filename1);
                    hashSet.add(filename2);
                }

                if (commitsPairFile.containsKey(pairFileIssue)) {
                    commitsPairFile.get(pairFileIssue).addAll(commits);
                } else {
                    commitsPairFile.put(pairFileIssue, commits);
                }

                if (issuesPairFileMap.containsKey(pairFileIssue)) {
                    issuesPairFileMap.get(pairFileIssue).addAll(issues);
                } else {
                    issuesPairFileMap.put(pairFileIssue, issues);
                }

                futureDefectsPairFile.put(pairFileIssue.getFileFile(), futureDefectsWeight);

                // TODO optimize querying at matrix generation
                // Find the pair files committers
                Set<AuxUser> pairFileCommitters = bichoPairFileDAO.selectCommitters(issues, filename1, filename2);

                /**
                 * Extract all distinct developer that commit a pair of file
                 */
                if (committersPairFile.containsKey(pairFileIssue)) {
                    Set<AuxUser> commiters = committersPairFile.get(pairFileIssue);
                    commiters.addAll(pairFileCommitters);
                } else {
                    committersPairFile.put(pairFileIssue, pairFileCommitters);
                }

                List<Commenter> commenters = bichoDAO.selectCommentersByIssueId(issue);

                /**
                 * Extract all distinct commenter of issue that pair of file was
                 * committed
                 */
                if (commentersPairFile.containsKey(issue)) {
                    commentersPairFile.get(issue).addAll(commenters);
                } else {
                    commentersPairFile.put(issue, new HashSet<>(commenters));
                }

                // Commenters that are developer too (have same name)
                Set<Commenter> devCommenters = new HashSet<>();
                for (Commenter commenter : commenters) {
                    if (commenter.isDev()) {
                        devCommenters.add(commenter);
                    }
                }
                if (devCommentersPairFile.containsKey(issue)) {
                    devCommentersPairFile.get(issue).addAll(devCommenters);
                } else {
                    devCommentersPairFile.put(issue, devCommenters);
                }

                if (commenters.isEmpty()) {
                    out.printLog("No commenters for issues " + Arrays.toString(issues.toArray()) + " pair file " + filename1 + ";" + filename2);
                    noCommenters.addAll(issues);
                } else if (commenters.size() == 1) {
                    DirectedSparseGraph<String, String> graphMulti
                            = new DirectedSparseGraph<>();
                    graphMulti.addVertex(commenters.get(0).getName());
                    pairFileNetwork.put(issue, graphMulti);
                } else {
                    Map<AuxUserUserDirectional, AuxUserUserDirectional> pairCommenters
                            = PairUtils.pairCommenters(commenters);

                    if (pairCommenters.isEmpty()) {
                        DirectedSparseGraph<String, String> graphMulti
                                = new DirectedSparseGraph<>();
                        graphMulti.addVertex(commenters.get(0).getName());
                        pairFileNetwork.put(issue, graphMulti);
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
                        if (pairFileNetwork.containsKey(issue)) {
                            pairFileNetwork.get(issue)
                                    .addEdge(pairUser.toStringDirectional(), pairUser.getUser(), pairUser.getUser2(), EdgeType.DIRECTED);
                        } else {
                            DirectedSparseGraph<String, String> graphMulti = new DirectedSparseGraph<>();
                            graphMulti.addEdge(pairUser.toStringDirectional(), pairUser.getUser(), pairUser.getUser2(), EdgeType.DIRECTED);
                            pairFileNetwork.put(issue, graphMulti);
                        }
                    }
                }
            }
        }
        out.printLog("No commenters for issues " + Arrays.toString(noCommenters.toArray()));

        JungExport.exportToImage(globalGraph, "C:/Users/a562273/Desktop/networks/",
                repository + " " + fixVersion);

        out.printLog("Número de pares de arquivos ignoradoa: " + countIgnored);

        out.printLog("Número de autores de comentários (commenters): " + globalGraph.getVertexCount());
        out.printLog("Número de pares de arquivos (committers): " + committersPairFile.size());
        out.printLog("Número de pares de arquivos (issues): " + issuesPairFileMap.size());
        out.printLog("Número de pares de arquivos (commenters): " + commentersPairFile.size());
        out.printLog("Número de pares de arquivos distintos: " + pairFilesSet.size());
        out.printLog("Iniciando cálculo das métricas.");

        Set<AuxFileFileIssueMetrics> fileFileMetrics = new HashSet<>();
        out.printLog("Calculando metricas SNA...");

        GlobalMeasure global = GlobalMeasureCalculator.calcule(globalGraph);
        out.printLog("Global measures: " + global.toString());

        // number of pull requests in date interval
        Long numberAllIssues = issuesSize;
        // cache for optimization number of pull requests where file is in,
        // reducing access to database
        Cacher cacher = new Cacher(bichoFileDAO, bichoPairFileDAO);

        out.printLog("Calculando somas, máximas, médias, updates, code churn e apriori para cada par de arquivos...");
        count = 0;
        final int size = committersPairFile.entrySet().size();
        out.printLog("Número de pares de arquivos: " + commentersPairFile.keySet().size());
        for (AuxFileFilePull fileFile : pairFilesSet) {
            if (count++ % 10 == 0 || count == size) {
                System.out.println(count + "/" + size);
            }
            final Integer issue = fileFile.getPullNumber();
            final Set<Commenter> devsCommentters = commentersPairFile.get(issue);

            // pair file network
            final DirectedSparseGraph<String, String> issueGraph = pairFileNetwork.get(issue);
            final NetworkMetricsCalculator networkMetrics = cacher.calculeNetworkMetrics(issue, issueGraph, edgesWeigth, devsCommentters);

            final Integer distinctCommentersCount = devsCommentters.size();

            // Commit-based metrics ////////////////////////////////////////////
            final Set<Integer> fileFileIssues = issuesPairFileMap.get(fileFile);
            final Set<Integer> allPairFileIssues = allPairFileIssuesMap.get(fileFile.getFileFile());

//            final Map<String, Long> issuesTypesCount = bichoDAO.countIssuesTypes(fileFileIssues);

            final long changes = cacher.calculeFileCodeChurn(
                    fileFile.getFileName(), fixVersion).getChanges();
            final long changes2 = cacher.calculeFileCodeChurn(
                    fileFile.getFileName2(), fixVersion).getChanges();

            final Set<AuxUser> devsCommitters = bichoPairFileDAO.selectCommitters(
                    fileFile.getFileName(), fileFile.getFileName2(), fixVersion, fileFileIssues);

            final DescriptiveStatisticsHelper devCommitsStatistics = new DescriptiveStatisticsHelper();
            final DescriptiveStatisticsHelper ownershipStatistics = new DescriptiveStatisticsHelper();
            long minorContributors = 0l, majorContributors = 0l;
            Double ownerExperience = 0.0d, ownerExperience2 = 0.0d,
                    cummulativeOwnerExperience = 0.0d, cummulativeOwnerExperience2 = 0.0d;

            final long committers = devsCommitters.size();
            final long devCommenters = devCommentersPairFile.get(issue).size();
            final long distinctCommitters = cacher.calculeCummulativeCommitters(
                    fileFile.getFileName(), fileFile.getFileName2(), fixVersion);

            final Long commits = bichoPairFileDAO.calculeCommits(fileFile.getFileName(), fileFile.getFileName2(),
                    fixVersion, fileFileIssues);

            final long totalCommits = cacher.calculeCummulativeCommits(fileFile.getFileName(), fileFile.getFileName2(),
                    fixVersion);

            for (AuxUser devCommitter : devsCommitters) {
                Long devCommits = bichoPairFileDAO.calculeCommits(fileFile.getFileName(), fileFile.getFileName2(), devCommitter.getUser(),
                        fixVersion, fileFileIssues);
                devCommitsStatistics.addValue(devCommits);

                Double ownership = devCommits.doubleValue() / commits.doubleValue();
                ownershipStatistics.addValue(ownership);

                if (ownership <= 0.05) { // menor ou igual que 5% = minor
                    minorContributors++;
                } else { // maior que 5% = major
                    majorContributors++;
                }

                // Calculing OEXP of each file
                Double experience = cacher.calculeDevFileExperience(changes,
                        fileFile.getFileName(), devCommitter.getUser(), fixVersion, fileFileIssues);
                ownerExperience = Math.max(experience, ownerExperience);

                Double experience2 = cacher.calculeDevFileExperience(changes2,
                        fileFile.getFileName2(), devCommitter.getUser(), fixVersion, fileFileIssues);
                ownerExperience2 = Math.max(experience2, ownerExperience2);

                // Calculing OWN
                final long cummulativeChanges = cacher.calculeFileCummulativeCodeChurn(
                        fileFile.getFileName(), fixVersion, fileFileIssues).getChanges();
                final long cummulativeChanges2 = cacher.calculeFileCummulativeCodeChurn(
                        fileFile.getFileName2(), fixVersion, fileFileIssues).getChanges();

                Double cumulativeExperience = cacher.calculeCummulativeDevFileExperience(cummulativeChanges,
                        fileFile.getFileName(), devCommitter.getUser(), fixVersion, fileFileIssues);
                cummulativeOwnerExperience = Math.max(cummulativeOwnerExperience, cumulativeExperience);

                Double cumulativeExperience2 = cacher.calculeCummulativeDevFileExperience(cummulativeChanges2,
                        fileFile.getFileName2(), devCommitter.getUser(), fixVersion, fileFileIssues);
                cummulativeOwnerExperience2 = Math.max(cummulativeOwnerExperience2, cumulativeExperience2);

            }

//            double majorContributorsRate = (double) majorContributors / (double) committers; // % de major
//            double minorContributorsRate = (double) minorContributors / (double) committers; // % de minor
            final long updates = fileFileIssues.size();
//                    bichoPairFileDAO.calculeNumberOfIssues(
//                    fileFile.getFileName(), fileFile.getFileName2(),
//                    beginDate, endDate, true);

            final long futureUpdates = cacher.calculeFutureNumberOfIssues(
                    fileFile.getFileFile(), futureVersion);

            // list all issues and its comments
            final IssueMetrics issueMetrics = cacher.calculeIssueMetrics(issue);

            final long codeChurn = cacher.calculeFileCodeChurn(
                    fileFile.getFileName(), fixVersion).getChanges();
            final long codeChurn2 = cacher.calculeFileCodeChurn(
                    fileFile.getFileName2(), fixVersion).getChanges();

            final AuxCodeChurn pairFileCodeChurn = cacher.calculeCummulativeCodeChurnAddDelChange(
                    fileFile.getFileName2(), fileFile.getFileName(), issue, allPairFileIssues, fixVersion);

            final double codeChurnAvg = (codeChurn + codeChurn2) / 2.0d;

            // pair file age in release interval (days)
            final int ageRelease = bichoPairFileDAO.calculePairFileDaysAge(fileFile.getFileName(), fileFile.getFileName2(), fixVersion, true);

            // pair file age in total until final date (days)
            final int ageTotal = bichoPairFileDAO.calculeTotalPairFileDaysAge(fileFile.getFileName(), fileFile.getFileName2(), fixVersion, true);

            final boolean samePackage = PathUtils.isSameFullPath(fileFile.getFileName(), fileFile.getFileName2());

            final boolean sameOwnership;
            if (committers == 1l) {
                final AuxUser pastCommitter = bichoPairFileDAO.selectLastCommitter(
                        fileFile.getFileName(), fileFile.getFileName2(), issue);
                if (pastCommitter.getUser() == null
                        || pastCommitter.getUser().equals(devsCommitters.iterator().next().getUser())) {
                    sameOwnership = true;
                } else {
                    sameOwnership = false;
                }
            } else {
                sameOwnership = false;
            }

            final long issueReopenedTimes = cacher.calculeIssueReopenedTimes(issue);

            final AuxFileFileIssueMetrics auxFileFileMetrics = new AuxFileFileIssueMetrics(
                    fileFile.getFileName(), fileFile.getFileName2(), issueMetrics,
                    issueReopenedTimes,
                    BooleanUtils.toInteger(samePackage),
                    BooleanUtils.toInteger(sameOwnership),
                    // barycenterSum, barycenterAvg, barycenterMax,
                    networkMetrics.getBetweennessSum(), networkMetrics.getBetweennessMean(), networkMetrics.getBetweennessMedian(), networkMetrics.getBetweennessMax(),
                    networkMetrics.getClosenessSum(), networkMetrics.getClosenessMean(), networkMetrics.getClosenessMedian(), networkMetrics.getClosenessMax(),
                    networkMetrics.getDegreeSum(), networkMetrics.getDegreeMean(), networkMetrics.getDegreeMedian(), networkMetrics.getDegreeMax(),
                    // eigenvectorSum, eigenvectorAvg, eigenvectorMax,

                    networkMetrics.getEgoBetweennessSum(), networkMetrics.getEgoBetweennessMean(), networkMetrics.getEgoBetweennessMedian(), networkMetrics.getEgoBetweennessMax(),
                    networkMetrics.getEgoSizeSum(), networkMetrics.getEgoSizeMean(), networkMetrics.getEgoSizeMedian(), networkMetrics.getEgoSizeMax(),
                    networkMetrics.getEgoTiesSum(), networkMetrics.getEgoTiesMean(), networkMetrics.getEgoTiesMedian(), networkMetrics.getEgoTiesMax(),
                    // egoPairsSum, egoPairsAvg, egoPairsMax,
                    networkMetrics.getEgoDensitySum(), networkMetrics.getEgoDensityMean(), networkMetrics.getEgoDensityMedian(), networkMetrics.getEgoDensityMax(),
                    networkMetrics.getEfficiencySum(), networkMetrics.getEfficiencyMean(), networkMetrics.getEfficiencyMedian(), networkMetrics.getEfficiencyMax(),
                    networkMetrics.getEffectiveSizeSum(), networkMetrics.getEffectiveSizeMean(), networkMetrics.getEffectiveSizeMedian(), networkMetrics.getEffectiveSizeMax(),
                    networkMetrics.getConstraintSum(), networkMetrics.getConstraintMean(), networkMetrics.getConstraintMedian(), networkMetrics.getConstraintMax(),
                    networkMetrics.getHierarchySum(), networkMetrics.getHierarchyMean(), networkMetrics.getHierarchyMedian(), networkMetrics.getHierarchyMax(),
                    networkMetrics.getGlobalSize(), networkMetrics.getGlobalTies(),
                    networkMetrics.getGlobalDensity(), networkMetrics.getGlobalDiameter(),
                    devCommitsStatistics.getSum(), devCommitsStatistics.getMean(), devCommitsStatistics.getMedian(), devCommitsStatistics.getMax(),
                    ownershipStatistics.getSum(), ownershipStatistics.getMean(), ownershipStatistics.getMedian(), ownershipStatistics.getMax(),
                    majorContributors, minorContributors,
                    ownerExperience, ownerExperience2,
                    cummulativeOwnerExperience, cummulativeOwnerExperience2,
                    distinctFilesPerIssueMap.get(issue).size(),
                    committers, distinctCommitters, commits, totalCommits, devCommenters,
                    distinctCommentersCount, issueMetrics.getNumberOfComments(), issueMetrics.getWordiness(),
                    codeChurn, codeChurn2, codeChurnAvg,
                    pairFileCodeChurn.getAdditionsNormalized(), pairFileCodeChurn.getDeletionsNormalized(), pairFileCodeChurn.getChanges(),
                    // rigidityFile1, rigidityFile2, rigidityPairFile,
//                    issuesTypesCount.get("Improvement"), issuesTypesCount.get("Bug"),
                    ageRelease, ageTotal, updates, futureUpdates, futureDefectsPairFile.get(fileFile.getFileFile())
            );

            // apriori /////////////////////////////////////////////////////////
            final Long file1Issues = cacher.calculeNumberOfIssues(auxFileFileMetrics.getFile(), fixVersion);
            final Long file2Issues = cacher.calculeNumberOfIssues(auxFileFileMetrics.getFile2(), fixVersion);

            auxFileFileMetrics.addMetrics(file1Issues, file2Issues, numberAllIssues);

            final FilePairApriori apriori = new FilePairApriori(file1Issues, file2Issues, updates, numberAllIssues);
            auxFileFileMetrics.setFilePairApriori(apriori);
            auxFileFileMetrics.addMetrics(
                    apriori.getSupportFile(), apriori.getSupportFile2(), apriori.getSupportFilePair(),
                    apriori.getConfidence(), apriori.getConfidence2(),
                    apriori.getLift(),
                    apriori.getConviction(), apriori.getConviction2()
            );

            auxFileFileMetrics.changeToRisky();
            fileFileMetrics.add(auxFileFileMetrics);
        }

        out.printLog("Número de pares de arquivos: " + fileFileMetrics.size());

        EntityMetric metrics = new EntityMetric();
        metrics.setNodes(objectsToNodes(fileFileMetrics));
        saveMetrics(metrics);

        List<AuxFileFileIssueMetrics> metricsList = new ArrayList<>(fileFileMetrics);
        Map<String, Set<AuxFileFileIssueMetrics>> top25 = getTop25(metricsList);

        // separa o top 10 em A + qualquerarquivo
        int rank = 1;
        for (Map.Entry<String, Set<AuxFileFileIssueMetrics>> top25EntrySet : top25.entrySet()) {
            final String combineWithFile = top25EntrySet.getKey();
            final Set<AuxFileFileIssueMetrics> changedWithA = top25EntrySet.getValue();
            final Set<AuxFileFileIssueMetrics> allCombinedFilesChangedWithA = new LinkedHashSet<>();

            // issues onde pelo menos um dos pares identificados mudou
            final Set<Integer> issuesWhereChangedAtLeatsOneTime = new HashSet<>();

            for (AuxFileFileIssueMetrics filePairChangedMetrics : changedWithA) {
                issuesWhereChangedAtLeatsOneTime.add(filePairChangedMetrics.getIssue());
            }

            int progress = 0, totalProgress = changedWithA.size() * issuesWhereChangedAtLeatsOneTime.size();

            for (Integer issue : issuesWhereChangedAtLeatsOneTime) {
                for (AuxFileFileIssueMetrics filePairChangedMetrics : changedWithA) {
                    if (progress++ % 10 == 0 || progress == totalProgress) {
                        System.out.println("Rank " + rank + " - " + progress + "/" + totalProgress);
                    }

                    final IssueMetrics issueMetrics = cacher.calculeIssueMetrics(issue);
                    AuxFileFileIssueMetrics combined = new AuxFileFileIssueMetrics(filePairChangedMetrics.getFile(), filePairChangedMetrics.getFile2(), issueMetrics);

                    if (!changedWithA.contains(combined)) {

                        final boolean samePackage = PathUtils.isSameFullPath(combined.getFile(), combined.getFile2());
                        final Set<Commenter> devsCommentters = commentersPairFile.get(issue);

                        // pair file network
                        final DirectedSparseGraph<String, String> issueGraph = pairFileNetwork.get(issue);
                        final NetworkMetricsCalculator networkMetrics = cacher.calculeNetworkMetrics(issue, issueGraph, edgesWeigth, devsCommentters);

                        final long issueReopenedTimes = cacher.calculeIssueReopenedTimes(issue);
                        combined.addMetrics(
                                // issueReopened
                                issueReopenedTimes,
                                // samePackage, sameOwnership
                                BooleanUtils.toInteger(samePackage), 0,
                                // barycenterSum, barycenterAvg, barycenterMax,
                                networkMetrics.getBetweennessSum(), networkMetrics.getBetweennessMean(), networkMetrics.getBetweennessMedian(), networkMetrics.getBetweennessMax(),
                                networkMetrics.getClosenessSum(), networkMetrics.getClosenessMean(), networkMetrics.getClosenessMedian(), networkMetrics.getClosenessMax(),
                                networkMetrics.getDegreeSum(), networkMetrics.getDegreeMean(), networkMetrics.getDegreeMedian(), networkMetrics.getDegreeMax(),
                                // eigenvectorSum, eigenvectorAvg, eigenvectorMax,

                                networkMetrics.getEgoBetweennessSum(), networkMetrics.getEgoBetweennessMean(), networkMetrics.getEgoBetweennessMedian(), networkMetrics.getEgoBetweennessMax(),
                                networkMetrics.getEgoSizeSum(), networkMetrics.getEgoSizeMean(), networkMetrics.getEgoSizeMedian(), networkMetrics.getEgoSizeMax(),
                                networkMetrics.getEgoTiesSum(), networkMetrics.getEgoTiesMean(), networkMetrics.getEgoTiesMedian(), networkMetrics.getEgoTiesMax(),
                                // egoPairsSum, egoPairsAvg, egoPairsMax,
                                networkMetrics.getEgoDensitySum(), networkMetrics.getEgoDensityMean(), networkMetrics.getEgoDensityMedian(), networkMetrics.getEgoDensityMax(),
                                networkMetrics.getEfficiencySum(), networkMetrics.getEfficiencyMean(), networkMetrics.getEfficiencyMedian(), networkMetrics.getEfficiencyMax(),
                                networkMetrics.getEffectiveSizeSum(), networkMetrics.getEffectiveSizeMean(), networkMetrics.getEffectiveSizeMedian(), networkMetrics.getEffectiveSizeMax(),
                                networkMetrics.getConstraintSum(), networkMetrics.getConstraintMean(), networkMetrics.getConstraintMedian(), networkMetrics.getConstraintMax(),
                                networkMetrics.getHierarchySum(), networkMetrics.getHierarchyMean(), networkMetrics.getHierarchyMedian(), networkMetrics.getHierarchyMax(),
                                networkMetrics.getGlobalSize(), networkMetrics.getGlobalTies(),
                                networkMetrics.getGlobalDensity(), networkMetrics.getGlobalDiameter());

                        final int numCommenters = commentersPairFile.get(issue).size();

                        final long totalCommitters = cacher.calculeCummulativeCommitters(
                                combined.getFile(), combined.getFile2(), fixVersion);
                        final long totalCommits = cacher.calculeCummulativeCommits(combined.getFile(), combined.getFile2(),
                                fixVersion);
                        // TODO da para usar o cache que vem da matrix
                        //                        final long futureUpdates = cacher.calculeFutureNumberOfIssues(
                        //                                filePairTop.getFile(), filePairTop.getFile2(), futureVersion);
                        final Map<String, Long> futureIssuesTypes = cacher.calculeFutureNumberOfIssuesWithType(
                                combined.getFile(), combined.getFile2(), futureVersion);
                        final long futureDefects;
                        if (futureIssuesTypes == null || !futureIssuesTypes.containsKey("Bug")) {
                            futureDefects = 0;
                        } else {
                            futureDefects = futureIssuesTypes.get("Bug");
                        }

                        long futureUpdates = 0;
                        for (Map.Entry<String, Long> entrySet : futureIssuesTypes.entrySet()) {
                            futureUpdates += entrySet.getValue();
                        }

                        Collection<Integer> allPairFileIssues = allPairFileIssuesMap.get(new AuxFileFile(combined.getFile(), combined.getFile2()));

                        final AuxCodeChurn pairFileCodeChurn = bichoPairFileDAO.calculeCummulativeCodeChurnAddDelChange(
                                combined.getFile(), combined.getFile2(), issue, allPairFileIssues, fixVersion);

                        combined.addMetrics(
                                // devCommitsStatistics.getSum(), devCommitsStatistics.getMean(), devCommitsStatistics.getMedian(), devCommitsStatistics.getMax(),
                                0, 0, 0, 0,
                                // ownershipStatistics.getSum(), ownershipStatistics.getMean(), ownershipStatistics.getMedian(), ownershipStatistics.getMax(),
                                0, 0, 0, 0,
                                // majorContributors, minorContributors,
                                0, 0,
                                // ownerExperience, ownerExperience2,
                                0, 0,
                                // cummulativeOwnerExperience, cummulativeOwnerExperience2,
                                0, 0,
                                // numFiles
                                distinctFilesPerIssueMap.get(issue).size(),
                                // committers, totalCommitters, commits, totalCommits, devCommenters,
                                0, totalCommitters, 0, totalCommits, 0,
                                // distinctCommentersCount, issuesAndComments.getNumberOfComments(), issuesAndComments.getWordiness(),
                                numCommenters, issueMetrics.getNumberOfComments(), issueMetrics.getWordiness(),
                                // codeChurn, codeChurn2, codeChurnAvg,
                                0, 0, 0,
                                // pairFileCodeChurn.getAdditionsNormalized(), pairFileCodeChurn.getDeletionsNormalized(), pairFileCodeChurn.getChanges()
                                pairFileCodeChurn.getAdditionsNormalized(), pairFileCodeChurn.getDeletionsNormalized(), pairFileCodeChurn.getChanges(),
                                // ageRelease, ageTotal
                                0, 0,
                                // updates, futureUpdates, futureDefects,
                                0, futureUpdates, futureDefects,
                                // file1Issues, file2Issues, numberAllIssues
                                0, 0, 0
                        );
                        combined.addMetrics(
                                // support1, support2, pair support
                                0, 0, 0,
                                // confidence1, confidence2
                                0, 0,
                                // lift
                                0,
                                // conviction1, conviction2
                                0, 0
                        );
                        allCombinedFilesChangedWithA.add(combined);
                    } else {
                        allCombinedFilesChangedWithA.add(filePairChangedMetrics);
                    }

                }
            }

            EntityMetric metrics3 = new EntityMetric();
            metrics3.setNodes(objectsToNodes(allCombinedFilesChangedWithA));
            metrics3.setAdditionalFilename(rank++ + " file changed with " + combineWithFile);
            saveMetrics(metrics3);
        }
    }

    private Map<String, Set<AuxFileFileIssueMetrics>> getTop25(final List<AuxFileFileIssueMetrics> pairFileList) {
        // order by number of defects (lower priority)
        orderByNumberOfDefects(pairFileList);
        // order by support (higher priority)
        orderByFilePairSupport(pairFileList);

        // 25 arquivos distintos com maior confiança entre o par (coluna da esquerda)
        final Set<String> distinctFileOfFilePairWithHigherConfidence = new LinkedHashSet<>(25);
        final List<AuxFileFileIssueMetrics> top25Metrics = new ArrayList<>(25);
        for (AuxFileFileIssueMetrics filePair : pairFileList) {
            top25Metrics.add(filePair);
            distinctFileOfFilePairWithHigherConfidence.add(filePair.getFile());
            if (distinctFileOfFilePairWithHigherConfidence.size() >= 25) {
                break;
            }
        }
        // salvando a matriz com o top par de arquivos
        EntityMetric metrics = new EntityMetric();
        metrics.setNodes(objectsToNodes(top25Metrics));
        metrics.setAdditionalFilename("top 25");
        saveMetrics(metrics);

        return getAllFilesChangedWithTop25(distinctFileOfFilePairWithHigherConfidence, pairFileList);
    }

    private Map<String, Set<AuxFileFileIssueMetrics>> getAllFilesChangedWithTop25(final Set<String> distinctFileOfFilePairWithHigherConfidence, final List<AuxFileFileIssueMetrics> pairFileList) {
        Map<String, Set<AuxFileFileIssueMetrics>> top25 = new LinkedHashMap<>(25);
        for (String file : distinctFileOfFilePairWithHigherConfidence) {
            final Set<AuxFileFileIssueMetrics> filesChangedWithA = new LinkedHashSet<>();
            
            for (AuxFileFileIssueMetrics pairFile : pairFileList) {
                if (pairFile.getFile().equals(file)) {
                    filesChangedWithA.add(pairFile);
                }
            }
            top25.put(file, filesChangedWithA);
        }
        return top25;
    }

    private void orderByFilePairSupport(final List<AuxFileFileIssueMetrics> pairFileList) {
        Collections.sort(pairFileList, new Comparator<AuxFileFileIssueMetrics>() {

            @Override
            public int compare(AuxFileFileIssueMetrics o1, AuxFileFileIssueMetrics o2) {
                FilePairApriori apriori1 = o1.getFilePairApriori();
                FilePairApriori apriori2 = o2.getFilePairApriori();
                if (apriori1.getSupportFilePair() > apriori2.getSupportFilePair()) {
                    return -1;
                } else if (apriori1.getSupportFilePair() < apriori2.getSupportFilePair()) {
                    return 1;
                }
                return 0;
            }
        });
    }

    private void orderByNumberOfDefects(final List<AuxFileFileIssueMetrics> pairFileList) {
        Collections.sort(pairFileList, new Comparator<AuxFileFileIssueMetrics>() {

            @Override
            public int compare(AuxFileFileIssueMetrics o1, AuxFileFileIssueMetrics o2) {
                final int defectIssuesIdWeight1 = o1.getFutureDefectIssuesIdWeight();
                final int defectIssuesIdWeight2 = o2.getFutureDefectIssuesIdWeight();
                if (defectIssuesIdWeight1 > defectIssuesIdWeight2) {
                    return -1;
                } else if (defectIssuesIdWeight1 < defectIssuesIdWeight2) {
                    return 1;
                }
                return 0;
            }
        });
    }

    @Override
    public String getHeadCSV() {
        return "file;file2;issue;"
                + "issueType;issuePriority;issueAssignedTo;issueSubmittedBy;"
                + "issueWatchers;issueReopened;"
                + "samePackage;sameOwnership;" // arquivos do par são do mesmo pacote = 1, caso contrário 0
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
                + "numFiles;"
                + "committers;" // committers na release
                + "totalCommitters;" // ddev, committers desde o começo até a data final da relese
                + "commits;" // commits do par de arquivos na release
                + "totalCommits;" // todos commits do par de arquivos
                + "devCommenters;" // número de autores de comentários que são desenvolvedores
                + "commenters;comments;wordiness;"
                + "codeChurn;codeChurn2;codeChurnAvg;"
                + "add;del;changes;"
                // + "rigidityFile1;rigidityFile2;rigidityPairFile;"
                // + "taskImprovement;taskDefect;"
                + "ageRelease;ageTotal;"
                + "updates;futureUpdates;futureDefects;"
                + "fileIssues;file2Issues;allIssues;"
                + "supportFile;supportFile2;supportPairFile;confidence;confidence2;lift;conviction;conviction2;changed";
    }

    @Override
    public List<String> getAvailableMatricesPermitted() {
        return Arrays.asList(BichoPairOfFileInFixVersionServices.class.getName());
    }

    private String getRepository() {
        return getMatrix().getRepository();
    }

    private Set<Integer> toIntegerList(String value) {
        String values[] = value.split(",");
        Set<Integer> list = new HashSet<>(values.length);
        for (String integerValue : values) {
            if (!integerValue.isEmpty()) {
                list.add(Integer.valueOf(integerValue));
            }
        }
        return list;
    }
}
