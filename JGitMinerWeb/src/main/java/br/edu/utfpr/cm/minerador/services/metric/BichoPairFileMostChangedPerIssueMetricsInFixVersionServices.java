package br.edu.utfpr.cm.minerador.services.metric;

import br.edu.utfpr.cm.JGitMinerWeb.dao.BichoDAO;
import br.edu.utfpr.cm.JGitMinerWeb.dao.BichoFileDAO;
import br.edu.utfpr.cm.JGitMinerWeb.dao.BichoPairFileDAO;
import br.edu.utfpr.cm.JGitMinerWeb.dao.GenericBichoDAO;
import br.edu.utfpr.cm.JGitMinerWeb.dao.GenericDao;
import br.edu.utfpr.cm.JGitMinerWeb.model.matrix.EntityMatrix;
import br.edu.utfpr.cm.JGitMinerWeb.model.matrix.EntityMatrixNode;
import br.edu.utfpr.cm.JGitMinerWeb.model.metric.EntityMetric;
import br.edu.utfpr.cm.JGitMinerWeb.services.matrix.auxiliary.AuxFileFile;
import br.edu.utfpr.cm.JGitMinerWeb.services.metric.auxiliary.AuxFileFileIssueMetrics;
import br.edu.utfpr.cm.JGitMinerWeb.util.OutLog;
import br.edu.utfpr.cm.minerador.services.matrix.BichoPairOfFileInFixVersionServices;
import br.edu.utfpr.cm.minerador.services.matrix.model.FilePairApriori;
import static br.edu.utfpr.cm.minerador.services.metric.AbstractBichoMetricServices.objectsToNodes;
import br.edu.utfpr.cm.minerador.services.metric.committer.Committer;
import br.edu.utfpr.cm.minerador.services.metric.committer.CommitterFileMetrics;
import br.edu.utfpr.cm.minerador.services.metric.committer.CommitterFileMetricsCalculator;
import br.edu.utfpr.cm.minerador.services.metric.committer.EmptyCommitterFileMetrics;
import br.edu.utfpr.cm.minerador.services.metric.model.CodeChurn;
import br.edu.utfpr.cm.minerador.services.metric.model.Commit;
import br.edu.utfpr.cm.minerador.services.metric.model.CommitMetrics;
import br.edu.utfpr.cm.minerador.services.metric.model.File;
import br.edu.utfpr.cm.minerador.services.metric.model.FileIssueMetrics;
import br.edu.utfpr.cm.minerador.services.metric.model.FilePair;
import br.edu.utfpr.cm.minerador.services.metric.model.IssueMetrics;
import br.edu.utfpr.cm.minerador.services.metric.socialnetwork.NetworkMetrics;
import br.edu.utfpr.cm.minerador.services.metric.socialnetwork.NetworkMetricsCalculator;
import br.edu.utfpr.cm.minerador.services.util.MatrixUtils;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
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
public class BichoPairFileMostChangedPerIssueMetricsInFixVersionServices extends AbstractBichoMetricServices {

    private String repository;

    public BichoPairFileMostChangedPerIssueMetricsInFixVersionServices() {
        super();
    }

    public BichoPairFileMostChangedPerIssueMetricsInFixVersionServices(GenericBichoDAO dao, GenericDao genericDao, EntityMatrix matrix, Map<Object, Object> params, OutLog out) {
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
        final String fixVersion = getVersion();
        final String futureVersion = getFutureVersion();

        out.printLog("Iniciado cálculo da métrica de matriz com " + getMatrix().getNodes().size() + " nodes. Parametros: " + params);

        final int maxFilePerCommit = 20;
        final BichoDAO bichoDAO = new BichoDAO(dao, repository, maxFilePerCommit);
        final BichoFileDAO bichoFileDAO = new BichoFileDAO(dao, repository, maxFilePerCommit);
        final BichoPairFileDAO bichoPairFileDAO = new BichoPairFileDAO(dao, repository, maxFilePerCommit);
        final Long issuesSize = bichoDAO
                .calculeNumberOfIssues(fixVersion, true);

        final String pastMajorVersion = bichoDAO.selectPastMajorVersion(fixVersion);

        System.out.println("Number of all pull requests: " + issuesSize);

        final Map<String, Integer> headerIndexesMap = MatrixUtils.extractHeaderIndexes(matrix);
        final List<EntityMatrixNode> matrixNodes = MatrixUtils.extractValues(matrix);

        // cache for optimization number of pull requests where file is in,
        // reducing access to database
        Cacher cacher = new Cacher(bichoFileDAO, bichoPairFileDAO);

        Set<FilePair> top25 = getTop25Matrix(matrixNodes, headerIndexesMap);
        // calcule committer experience for top 25 files
        for (FilePair filePair : top25) {
            final Set<Committer> fileCommittersInPreviousVersion
                    = bichoFileDAO.selectCommitters(filePair.getFileName(), fixVersion);
            for (Committer committer : fileCommittersInPreviousVersion) {

            }
        }

        CommitterFileMetricsCalculator committerFileMetricsCalculator = new CommitterFileMetricsCalculator(bichoFileDAO);

        // separa o top 10 em A + qualquerarquivo
        int rank = 1;
        for (FilePair filePair : top25) {
            final Set<FileIssueMetrics> allFileChanges = new LinkedHashSet<>();

            // par analisado
            final String filename = filePair.getFileName(); // arquivo principal
            final String filename2 = filePair.getFileName2();
            final File file = new File(filename);
            final File file2 = new File(filename2);

            final List<Integer> issueWhereFileChanged = bichoFileDAO.selectIssues(filename, fixVersion);
            int progress = 0, totalProgress = issueWhereFileChanged.size();

            for (Integer issue : issueWhereFileChanged) {

                if (progress++ % 100 == 0 || progress == totalProgress) {
                    System.out.println("Rank " + rank + " - " + progress + "/" + totalProgress);
                }

                final IssueMetrics issueMetrics = cacher.calculeIssueMetrics(issue);

                final Set<Commit> issueCommits = bichoDAO.selectFilesAndCommitByIssue(issue);

                for (Commit commitInIssue : issueCommits) {
                    Set<File> filesInCommit = commitInIssue.getFiles();

                    // metricas do arquivo com maior confiança, somente
                    final FileIssueMetrics fileIssueMetrics = new FileIssueMetrics(filename, filename2, commitInIssue, issueMetrics);

                    // TODO metrics do commit
                    final CommitMetrics commitMetrics = new CommitMetrics(commitInIssue);

                    final CommitterFileMetrics committerFileMetrics;
                    if (pastMajorVersion != null) {
                        committerFileMetrics = committerFileMetricsCalculator.calculeForVersion(file, commitInIssue.getCommiter(), pastMajorVersion);
                    } else {
                        committerFileMetrics = new EmptyCommitterFileMetrics();
                    }

                    fileIssueMetrics.setCommitMetrics(commitMetrics);
                    fileIssueMetrics.setCommitterFileMetrics(committerFileMetrics);

                    if (filesInCommit.contains(file2)) { // se houve commit do arquivo 2, então o par mudou
                        // muda a coluna changed para 1, indicando que o par analisado mudou nessa issue
                        fileIssueMetrics.changed();
                    }

                    // calculo das metricas de commit apenas para o primeiro arquivo
                    if (!allFileChanges.contains(fileIssueMetrics)) {
                        // pair file network
                        final NetworkMetrics networkMetrics
                                = new NetworkMetricsCalculator(issue, bichoDAO).getNetworkMetrics();

                        fileIssueMetrics.setNetworkMetrics(networkMetrics);

                        final long totalCommitters = bichoFileDAO.calculeCummulativeCommitters(filename, issue, fixVersion);
                        final long totalCommits = bichoFileDAO.calculeCummulativeCommits(filename, issue, fixVersion);

                        final Map<String, Long> futureIssuesTypes
                                = bichoFileDAO.calculeNumberOfIssuesGroupedByType(
                                        filename, futureVersion);
                        final long futureDefects;
                        if (!futureIssuesTypes.containsKey("Bug")) {
                            futureDefects = 0;
                        } else {
                            futureDefects = futureIssuesTypes.get("Bug");
                        }

                        long futureUpdates = 0;
                        for (Map.Entry<String, Long> entrySet : futureIssuesTypes.entrySet()) {
                            futureUpdates += entrySet.getValue();
                        }

                        final Committer lastCommitter = bichoFileDAO.selectLastCommitter(file.getFileName(), commitInIssue, fixVersion);
                        final boolean sameOwnership = commitInIssue.getCommiter().equals(lastCommitter);

                        final CodeChurn fileCodeChurn = bichoFileDAO.calculeAddDelChanges(filename, issue, commitInIssue.getId(), fixVersion);

                        // pair file age in release interval (days)
                        final int ageRelease = bichoFileDAO.calculeFileAgeInDays(filename, issue, fixVersion);

                        // pair file cummulative age: from first commit until previous (past) release
                        final int ageTotal = bichoFileDAO.calculeTotalFileAgeInDays(filename, issue, fixVersion);

                        fileIssueMetrics.addMetrics(
                                // majorContributors
                                0,
                                // ownerExperience,
                                0,
                                // cummulativeOwnerExperience
                                0,
                                // sameOwnership
                                BooleanUtils.toInteger(sameOwnership),
                                // number of distinct files in commit
                                filesInCommit.size(),
                                // committers, totalCommitters, commits, totalCommits,
                                totalCommitters, totalCommits,
                                // pairFileCodeChurn.getAdditionsNormalized(), pairFileCodeChurn.getDeletionsNormalized(), pairFileCodeChurn.getChanges()
                                fileCodeChurn.getAdditionsNormalized(), fileCodeChurn.getDeletionsNormalized(), fileCodeChurn.getChanges(),
                                // ageRelease, ageTotal
                                ageRelease, ageTotal,
                                // futureUpdates, futureDefects,
                                futureUpdates, futureDefects
                        );

                        allFileChanges.add(fileIssueMetrics);
                    }
                }
            }

            EntityMetric metrics3 = new EntityMetric();
            metrics3.setNodes(objectsToNodes(allFileChanges, FileIssueMetrics.HEADER));
            metrics3.setAdditionalFilename("rank " + rank++);
            saveMetrics(metrics3);
        }
    }

    private Set<FilePair> getTop25Matrix(final List<EntityMatrixNode> matrixNodes, final Map<String, Integer> header) {
        // order by number of defects (lower priority)
        MatrixUtils.orderByNumberOfDefects(matrixNodes, header);
        // order by support (higher priority)
        MatrixUtils.orderByFilePairSupport(matrixNodes, header);

        // 25 arquivos distintos com maior confiança entre o par (coluna da esquerda)
        final int file1Index = header.get("file1");
        final int file2Index = header.get("file2");

        final Set<FilePair> distinctFileOfFilePairWithHigherConfidence = new LinkedHashSet<>(25);
        for (EntityMatrixNode node : matrixNodes) {
            final String[] lineValues = MatrixUtils.separateValues(node);

            FilePair filePair = new FilePair(lineValues[file1Index], lineValues[file2Index]);

            distinctFileOfFilePairWithHigherConfidence.add(filePair);
            if (distinctFileOfFilePairWithHigherConfidence.size() >= 25) {
                break;
            }
        }

        return distinctFileOfFilePairWithHigherConfidence;
    }

    private Set<FilePair> getTop25(final List<AuxFileFileIssueMetrics> pairFileList) {
        // order by number of defects (lower priority)
        orderByNumberOfDefects(pairFileList);
        // order by support (higher priority)
        orderByFilePairSupport(pairFileList);

        // 25 arquivos distintos com maior confiança entre o par (coluna da esquerda)
        final Set<FilePair> distinctFileOfFilePairWithHigherConfidence = new LinkedHashSet<>(25);
        final List<AuxFileFileIssueMetrics> top25Metrics = new ArrayList<>(25);
        for (AuxFileFileIssueMetrics filePairMetrics : pairFileList) {
            top25Metrics.add(filePairMetrics);
            FilePair filePair = new FilePair(filePairMetrics.getFile(), filePairMetrics.getFile2());
            distinctFileOfFilePairWithHigherConfidence.add(filePair);
            if (distinctFileOfFilePairWithHigherConfidence.size() >= 25) {
                break;
            }
        }
        // salvando a matriz com o top par de arquivos
        EntityMetric metrics = new EntityMetric();
        metrics.setNodes(objectsToNodes(top25Metrics, getHeadCSV()));
        metrics.setAdditionalFilename("top 25");
        saveMetrics(metrics);

        return distinctFileOfFilePairWithHigherConfidence;
    }

    private Map<AuxFileFile, Set<Integer>> getAllTopFileChanges(Set<AuxFileFile> distinctFileOfFilePairWithHigherConfidence, List<AuxFileFileIssueMetrics> pairFileList) {
        Map<AuxFileFile, Set<Integer>> topFiles = new LinkedHashMap<>(distinctFileOfFilePairWithHigherConfidence.size());

        for (AuxFileFile fileFile : distinctFileOfFilePairWithHigherConfidence) {
            for (AuxFileFileIssueMetrics fileFileIssueMetrics : pairFileList) {
                // one file of pair is equals to first file (file with high confidence)?
                if (fileFileIssueMetrics.getFile().equals(fileFile.getFileName())
                        || fileFileIssueMetrics.getFile2().equals(fileFile.getFileName())) {
                    if (topFiles.containsKey(fileFile)) {
                        topFiles.get(fileFile).add(fileFileIssueMetrics.getIssue());
                    } else {
                        Set<Integer> changes = new LinkedHashSet<>();
                        changes.add(fileFileIssueMetrics.getIssue());
                        topFiles.put(fileFile, changes);
                    }
                }
            }
        }
        return topFiles;
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
