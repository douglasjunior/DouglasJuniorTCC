package br.edu.utfpr.cm.minerador.services.metric;

import br.edu.utfpr.cm.JGitMinerWeb.dao.AuxCodeChurn;
import br.edu.utfpr.cm.JGitMinerWeb.dao.AuxUser;
import br.edu.utfpr.cm.JGitMinerWeb.dao.BichoDAO;
import br.edu.utfpr.cm.JGitMinerWeb.dao.BichoFileDAO;
import br.edu.utfpr.cm.JGitMinerWeb.dao.BichoPairFileDAO;
import br.edu.utfpr.cm.JGitMinerWeb.dao.GenericBichoDAO;
import br.edu.utfpr.cm.JGitMinerWeb.model.matrix.EntityMatrixNode;
import br.edu.utfpr.cm.JGitMinerWeb.model.metric.EntityMetric;
import br.edu.utfpr.cm.JGitMinerWeb.services.matrix.auxiliary.AuxFileFile;
import br.edu.utfpr.cm.JGitMinerWeb.services.matrix.auxiliary.AuxUserUserDirectional;
import br.edu.utfpr.cm.JGitMinerWeb.services.metric.auxiliary.AuxFileFileMetrics;
import br.edu.utfpr.cm.JGitMinerWeb.services.metric.auxiliary.IssueMetrics;
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
import br.edu.utfpr.cm.minerador.services.matrix.BichoPairOfFileInFixVersionServices;
import br.edu.utfpr.cm.minerador.services.matrix.model.Commenter;
import br.edu.utfpr.cm.minerador.services.matrix.model.FilePairApriori;
import static br.edu.utfpr.cm.minerador.services.metric.AbstractBichoMetricServices.objectsToNodes;
import edu.uci.ics.jung.graph.DirectedSparseGraph;
import edu.uci.ics.jung.graph.util.EdgeType;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
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
public class BichoPairFileMetricsInFixVersionServices extends AbstractBichoMetricServices {

    private String repository;

    public BichoPairFileMetricsInFixVersionServices() {
        super(null, null);
    }

    public BichoPairFileMetricsInFixVersionServices(GenericBichoDAO dao, OutLog out) {
        super(dao, out);
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

        String fixVersion = getVersion();
        String futureVersion = getFutureVersion();

        // file; file2; issueWeigth; issues; commitsWeight; commmits
        out.printLog("Iniciado cálculo da métrica de matriz com " + getMatrix().getNodes().size() + " nodes. Parametros: " + params);

        final Map<AuxFileFile, Set<AuxUser>> committersPairFile = new HashMap<>();
        final Map<AuxFileFile, Set<Commenter>> commentersPairFile = new HashMap<>();
        final Map<AuxFileFile, Set<Commenter>> devCommentersPairFile = new HashMap<>();
        final Map<AuxFileFile, Set<Integer>> issuesPairFile = new HashMap<>();
        final Map<AuxFileFile, Set<Integer>> commitsPairFile = new HashMap<>();
        final Map<AuxFileFile, Integer> futureDefectsPairFile = new HashMap<>();
        final Map<String, Integer> edgesWeigth = new HashMap<>();
        final Set<Integer> noCommenters = new HashSet<>();

        // rede de comunicação global, com todos pares de arquivos
        DirectedSparseGraph<String, String> globalGraph = new DirectedSparseGraph<>();

        // rede de comunicação de cada par de arquivo
        Map<AuxFileFile, DirectedSparseGraph<String, String>> pairFileNetwork = new HashMap<>();

        int countIgnored = 0;
        final int maxFilePerCommit = 20;
        BichoDAO bichoDAO = new BichoDAO(dao, repository);
        BichoFileDAO bichoFileDAO = new BichoFileDAO(dao, repository, maxFilePerCommit);
        BichoPairFileDAO bichoPairFileDAO = new BichoPairFileDAO(dao, repository, maxFilePerCommit);
        Long issuesSize = bichoPairFileDAO
                .calculeNumberOfIssues(fixVersion, true);

        System.out.println("Number of all pull requests: " + issuesSize);

        // construindo a rede de comunicação para cada par de arquivo (desenvolvedores que comentaram)
        int nodesSize = getMatrix().getNodes().size();
        int count = 0;
        Set<AuxFileFile> pairFilesSet = new HashSet<>();
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

            AuxFileFile pairFile = new AuxFileFile(filename1, filename2);
            pairFilesSet.add(pairFile);

            if (commitsPairFile.containsKey(pairFile)) {
                commitsPairFile.get(pairFile).addAll(commits);
            } else {
                commitsPairFile.put(pairFile, commits);
            }

            if (issuesPairFile.containsKey(pairFile)) {
                issuesPairFile.get(pairFile).addAll(issues);
            } else {
                issuesPairFile.put(pairFile, issues);
            }

            futureDefectsPairFile.put(pairFile, futureDefectsWeight);

            // TODO optimize querying at matrix generation
            // Find the pair files committers
            Set<AuxUser> pairFileCommitters = bichoPairFileDAO.selectCommitters(issues, filename1, filename2);

            /**
             * Extract all distinct developer that commit a pair of file
             */
            if (committersPairFile.containsKey(pairFile)) {
                Set<AuxUser> commiters = committersPairFile.get(pairFile);
                commiters.addAll(pairFileCommitters);
            } else {
                committersPairFile.put(pairFile, pairFileCommitters);
            }

            List<Commenter> commenters = bichoDAO.selectCommentersByIssueId(issues);

            /**
             * Extract all distinct commenter of issue that pair of file was
             * committed
             */
            if (commentersPairFile.containsKey(pairFile)) {
                commentersPairFile.get(pairFile).addAll(commenters);
            } else {
                commentersPairFile.put(pairFile, new HashSet<>(commenters));
            }

            // Commenters that are developer too (have same name)
            Set<Commenter> devCommenters = new HashSet<>();
            for (Commenter commenter : commenters) {
                if (commenter.isDev()) {
                    devCommenters.add(commenter);
                }
            }
            if (devCommentersPairFile.containsKey(pairFile)) {
                devCommentersPairFile.get(pairFile).addAll(devCommenters);
            } else {
                devCommentersPairFile.put(pairFile, devCommenters);
            }

            if (commenters.isEmpty()) {
                out.printLog("No commenters for issues " + Arrays.toString(issues.toArray()) + " pair file " + filename1 + ";" + filename2);
                noCommenters.addAll(issues);
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
        out.printLog("No commenters for issues " + Arrays.toString(noCommenters.toArray()));

        SimpleDateFormat format = new SimpleDateFormat("dd.MM.yyyy");
        JungExport.exportToImage(globalGraph, "C:/Users/a562273/Desktop/networks/",
                repository + " " + fixVersion);

        out.printLog("Número de pares de arquivos ignoradoa: " + countIgnored);

        out.printLog("Número de autores de comentários (commenters): " + globalGraph.getVertexCount());
        out.printLog("Número de pares de arquivos (committers): " + committersPairFile.size());
        out.printLog("Número de pares de arquivos (issues): " + issuesPairFile.size());
        out.printLog("Número de pares de arquivos (commenters): " + commentersPairFile.size());
        out.printLog("Número de pares de arquivos distintos: " + pairFilesSet.size());
        out.printLog("Iniciando cálculo das métricas.");

        Set<AuxFileFileMetrics> fileFileMetrics = new HashSet<>();

        out.printLog("Calculando metricas SNA...");

        GlobalMeasure global = GlobalMeasureCalculator.calcule(globalGraph);
        out.printLog("Global measures: " + global.toString());

        // number of pull requests in date interval
        Long numberAllFutureIssues = issuesSize;
        // cache for optimization number of pull requests where file is in,
        // reducing access to database
        Cacher cacher = new Cacher(bichoFileDAO);

        out.printLog("Calculando somas, máximas, médias, updates, code churn e apriori para cada par de arquivos...");
        count = 0;
        final int size = committersPairFile.entrySet().size();
        out.printLog("Número de pares de arquivos: " + commentersPairFile.keySet().size());
        for (Map.Entry<AuxFileFile, Set<Commenter>> entry : commentersPairFile.entrySet()) {
            if (count++ % 10 == 0 || count == size) {
                System.out.println(count + "/" + size);
            }
            AuxFileFile fileFile = entry.getKey();
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

            final Map<String, Long> issuesTypesCount = bichoDAO.countIssuesTypes(fileFileIssues);

            if (fileFileIssues == null || fileFileIssues.isEmpty()) {
                out.printLog("Empty issues for " + fileFile.toString());
            }

            final long changes = cacher.calculeFileCodeChurn(
                    fileFile.getFileName(), fixVersion).getChanges();
            final long changes2 = cacher.calculeFileCodeChurn(
                    fileFile.getFileName2(), fixVersion).getChanges();

            Set<AuxUser> devsCommitters = bichoPairFileDAO.selectCommitters(
                    fileFile.getFileName(), fileFile.getFileName2(), fixVersion, fileFileIssues);

            DescriptiveStatisticsHelper devCommitsStatistics = new DescriptiveStatisticsHelper();
            DescriptiveStatisticsHelper ownershipStatistics = new DescriptiveStatisticsHelper();
            Long minorContributors = 0l, majorContributors = 0l;
            Double ownerExperience = 0.0d, ownerExperience2 = 0.0d,
                    cummulativeOwnerExperience = 0.0d, cummulativeOwnerExperience2 = 0.0d;

            long committers = devsCommitters.size();
            long devCommenters = devCommentersPairFile.get(fileFile).size();
            long distinctCommitters = bichoPairFileDAO.calculeCummulativeCommitters(
                    fileFile.getFileName(), fileFile.getFileName2(), fixVersion, fileFileIssues);

            Long commits = bichoPairFileDAO.calculeCommits(fileFile.getFileName(), fileFile.getFileName2(),
                    fixVersion, fileFileIssues);

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
            Long updates = (long) fileFileIssues.size();
//                    bichoPairFileDAO.calculeNumberOfIssues(
//                    fileFile.getFileName(), fileFile.getFileName2(),
//                    beginDate, endDate, true);

            Long futureUpdates = bichoPairFileDAO.calculeNumberOfIssues(
                    fileFile.getFileName(), fileFile.getFileName2(),
                    futureVersion);

            // list all issues and its comments
            Collection<IssueMetrics> issuesAndComments
                    = bichoPairFileDAO.listIssues(fileFile.getFileName(), fileFile.getFileName2(), fixVersion, fileFileIssues);

            long wordiness = 0;
            long commentsSum = 0;
            for (IssueMetrics auxWordiness : issuesAndComments) {
                wordiness += WordinessCalculator.calcule(auxWordiness);
                commentsSum += auxWordiness.getComments().size();
            }

            final long codeChurn = cacher.calculeFileCodeChurn(
                    fileFile.getFileName(), fixVersion).getChanges();
            final long codeChurn2 = cacher.calculeFileCodeChurn(
                    fileFile.getFileName2(), fixVersion).getChanges();

            AuxCodeChurn pairFileCodeChurn = bichoPairFileDAO.calculeCodeChurnAddDelChange(
                    fileFile.getFileName2(), fileFile.getFileName(),
                    fixVersion, fileFileIssues);

            double codeChurnAvg = (codeChurn + codeChurn2) / 2.0d;

            // pair file age in release interval (days)
            int ageRelease = bichoPairFileDAO.calculePairFileDaysAge(fileFile.getFileName(), fileFile.getFileName2(), fixVersion, true);

            // pair file age in total until final date (days)
            int ageTotal = bichoPairFileDAO.calculeTotalPairFileDaysAge(fileFile.getFileName(), fileFile.getFileName2(), fixVersion, true);

            boolean samePackage = PathUtils.isSameFullPath(fileFile.getFileName(), fileFile.getFileName2());

            AuxFileFileMetrics auxFileFileMetrics = new AuxFileFileMetrics(
                    fileFile.getFileName(), fileFile.getFileName2(),
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
                    committers, distinctCommitters, commits, devCommenters,
                    distinctCommentersCount, commentsSum, wordiness,
                    codeChurn, codeChurn2, codeChurnAvg,
                    pairFileCodeChurn.getAdditionsNormalized(), pairFileCodeChurn.getDeletionsNormalized(), pairFileCodeChurn.getChanges(),
                    // rigidityFile1, rigidityFile2, rigidityPairFile,
                    issuesTypesCount.get("Improvement"), issuesTypesCount.get("Bug"), futureDefectsPairFile.get(fileFile),
                    ageRelease, ageTotal, updates, futureUpdates
            );

            // apriori /////////////////////////////////////////////////////////
            Long file1FutureIssues = cacher.calculeNumberOfIssues(auxFileFileMetrics.getFile(), fixVersion);
            Long file2FutureIssues = cacher.calculeNumberOfIssues(auxFileFileMetrics.getFile2(), fixVersion);

            auxFileFileMetrics.addMetrics(file1FutureIssues, file2FutureIssues, numberAllFutureIssues);

            FilePairApriori apriori = new FilePairApriori(file1FutureIssues, file2FutureIssues, updates, numberAllFutureIssues);
            auxFileFileMetrics.setFilePairApriori(apriori);
            auxFileFileMetrics.addMetrics(
                    apriori.getSupportFile(), apriori.getSupportFile2(), apriori.getSupportFilePair(),
                    apriori.getConfidence(), apriori.getConfidence2(),
                    apriori.getLift(),
                    apriori.getConviction(), apriori.getConviction2()
            );

            fileFileMetrics.add(auxFileFileMetrics);
        }

        out.printLog("Número de pares de arquivos: " + fileFileMetrics.size());

        EntityMetric metrics = new EntityMetric();
        metrics.setNodes(objectsToNodes(fileFileMetrics));
        saveMetrics(metrics);

        List<AuxFileFileMetrics> metricsList = new ArrayList<>(fileFileMetrics);
        // salvando a matriz com o top 10 par de arquivos
        EntityMetric metrics2 = new EntityMetric();
        List<AuxFileFileMetrics> top10 = getTop10(metricsList);
        metrics2.setNodes(objectsToNodes(top10));
        metrics2.setAdditionalFilename(" top 10");
        saveMetrics(metrics2);

        // separa o top 10 em A + qualquerarquivo
        int rank = 0;
        for (AuxFileFileMetrics filePairTop : top10) {
            List<AuxFileFileMetrics> changedWithA = new ArrayList<>();
            for (AuxFileFileMetrics filePair : metricsList) {
                if (filePair.getFile()
                        .equals(filePairTop.getFile())
                        && !filePair.equals(filePairTop)) {
                    changedWithA.add(filePair);
                }
            }
            filePairTop.changeToRisky();
            changedWithA.add(0, filePairTop);
            EntityMetric metrics3 = new EntityMetric();
            metrics3.setNodes(objectsToNodes(changedWithA));
            rank++;
            metrics3.setAdditionalFilename(" " + rank + " file changed with " + filePairTop.getFile());
            saveMetrics(metrics3);
        }
    }

    private List<AuxFileFileMetrics> getTop10(final List<AuxFileFileMetrics> pairFileList) {
        // order by number of defects (lower priority)
        orderByNumberOfDefects(pairFileList);
        // order by support (higher priority)
        orderByFilePairSupport(pairFileList);

        int lastIndex = pairFileList.size() > 10 ? 10 : pairFileList.size();
        final List<AuxFileFileMetrics> top10 = pairFileList.subList(0, lastIndex);
        return top10;
    }

    private void orderByFilePairSupport(final List<AuxFileFileMetrics> pairFileList) {
        Collections.sort(pairFileList, new Comparator<AuxFileFileMetrics>() {

            @Override
            public int compare(AuxFileFileMetrics o1, AuxFileFileMetrics o2) {
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

    private void orderByNumberOfDefects(final List<AuxFileFileMetrics> pairFileList) {
        Collections.sort(pairFileList, new Comparator<AuxFileFileMetrics>() {

            @Override
            public int compare(AuxFileFileMetrics o1, AuxFileFileMetrics o2) {
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
        return "file;file2;"
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
                + "adev;" // committers na release
                + "ddev;" // committers desde o começo até a data final da relese
                + "commits;" // commits do par de arquivos
                + "devCommenters;" // número de autores de comentários que são desenvolvedores
                + "commenters;comments;wordiness;"
                + "codeChurn;codeChurn2;codeChurnAvg;"
                + "add;del;changes;"
                //                + "rigidityFile1;rigidityFile2;rigidityPairFile;"
                + "taskImprovement;taskDefect;futureDefects;"
                + "ageRelease;ageTotal;"
                + "updates;futureUpdates;"
                + "fileFutureIssues;file2FutureIssues;allFutureIssues;"
                + "supportFile;supportFile2;supportPairFile;confidence;confidence2;lift;conviction;conviction2;risk";
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
