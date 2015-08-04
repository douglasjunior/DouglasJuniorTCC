package br.edu.utfpr.cm.minerador.services.matrix;

import br.edu.utfpr.cm.JGitMinerWeb.dao.BichoDAO;
import br.edu.utfpr.cm.JGitMinerWeb.dao.BichoFileDAO;
import br.edu.utfpr.cm.JGitMinerWeb.dao.BichoPairFileDAO;
import br.edu.utfpr.cm.JGitMinerWeb.dao.GenericBichoDAO;
import br.edu.utfpr.cm.JGitMinerWeb.model.matrix.EntityMatrix;
import br.edu.utfpr.cm.JGitMinerWeb.util.OutLog;
import br.edu.utfpr.cm.JGitMinerWeb.util.Util;
import br.edu.utfpr.cm.minerador.services.matrix.model.FilePair;
import br.edu.utfpr.cm.minerador.services.matrix.model.FilePairApriori;
import br.edu.utfpr.cm.minerador.services.matrix.model.FilePairAprioriOutput;
import br.edu.utfpr.cm.minerador.services.metric.Cacher;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 
 * @author Rodrigo Kuroda
 */
public class BichoPairOfFileInFixVersionServices extends AbstractBichoMatrixServices {

    public BichoPairOfFileInFixVersionServices() {
        super(null, null);
    }

    public BichoPairOfFileInFixVersionServices(GenericBichoDAO dao, OutLog out) {
        super(dao, out);
    }

    public BichoPairOfFileInFixVersionServices(GenericBichoDAO dao, String repository, List<EntityMatrix> matricesToSave, Map<Object, Object> params, OutLog out) {
        super(dao, repository, matricesToSave, params, out);
    }

    private Integer getMaxFilesPerCommit() {
        return Util.stringToInteger(params.get("maxFilesPerCommit") + "");
    }

    private Integer getMinFilesPerCommit() {
        return Util.stringToInteger(params.get("minFilesPerCommit") + "");
    }

    private boolean isOnlyFixed() {
        return "true".equalsIgnoreCase(params.get("mergedOnly") + "");
    }

    public List<String> getFilesToIgnore() {
        return getStringLinesParam("filesToIgnore", true, false);
    }

    public List<String> getFilesToConsiders() {
        return getStringLinesParam("filesToConsiders", true, false);
    }

    public String getVersion() {
        return getStringParam("version");
    }

    public String getFutureVersion() {
        return getStringParam("futureVersion");
    }

    @Override
    public void run() {
        System.out.println(params);

        if (getRepository() == null) {
            throw new IllegalArgumentException("Parameter repository must be informed.");
        }

        log("\n --------------- "
                + new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new Date())
                + "\n --------------- \n");

        String version = getVersion();
        String futureVersion = getFutureVersion();
//        Set<FilePath> allDistinctFiles = new HashSet<>();

//        Pattern fileToConsiders = null;
//        if (getFilesToConsiders() != null && !getFilesToConsiders().isEmpty()) {
//            fileToConsiders = MatcherUtils.createExtensionIncludeMatcher(getFilesToConsiders());
//        }

        BichoDAO bichoDAO = new BichoDAO(dao, getRepository(), getMaxFilesPerCommit());
        BichoFileDAO bichoFileDAO = new BichoFileDAO(dao, getRepository(), getMaxFilesPerCommit());
        BichoPairFileDAO bichoPairFileDAO = new BichoPairFileDAO(dao, getRepository(), getMaxFilesPerCommit());
        Cacher cacher = new Cacher(bichoFileDAO);

        out.printLog("Maximum files per commit: " + getMaxFilesPerCommit());
        out.printLog("Minimum files per commit: " + getMinFilesPerCommit());

        final Map<FilePair, FilePairAprioriOutput> pairFiles = new HashMap<>();
        identifyFilePairs(pairFiles, bichoDAO, version, bichoFileDAO);

        out.printLog("Result: " + pairFiles.size());

        if (futureVersion != null) {
            out.printLog("Counting future defects...");
            final int total = pairFiles.keySet().size();
            int progressCountFutureDefects = 0;
            for (FilePair fileFile : pairFiles.keySet()) {
                if (++progressCountFutureDefects % 100 == 0
                        || progressCountFutureDefects == total) {
                    System.out.println(progressCountFutureDefects + "/" + total);
                }
                List<Integer> futureDefectIssues = bichoPairFileDAO.selectIssues(
                        fileFile.getFile1(), fileFile.getFile2(),
                        futureVersion, "Bug");
                pairFiles.get(fileFile).addFutureDefectIssuesId(futureDefectIssues);
            }
        }

        Set<Integer> allConsideredIssues = new HashSet<>();
        for (Map.Entry<FilePair, FilePairAprioriOutput> entrySet : pairFiles.entrySet()) {
            FilePairAprioriOutput value = entrySet.getValue();
            allConsideredIssues.addAll(value.getIssuesId());
        }
        // calculando o apriori
        out.printLog("Calculing apriori...");
        out.printLog("Issues considered in version " + version + ": " + allConsideredIssues.size());
        int totalApriori = pairFiles.size();
        int countApriori = 0;

        final List<FilePairAprioriOutput> pairFileList = new ArrayList<>();

        for (FilePair fileFile : pairFiles.keySet()) {
            if (++countApriori % 100 == 0
                    || countApriori == totalApriori) {
                System.out.println(countApriori + "/" + totalApriori);
            }

            Long file1Issues = cacher.calculeNumberOfIssues(fileFile.getFile1(), version);
            Long file2Issues = cacher.calculeNumberOfIssues(fileFile.getFile2(), version);

            FilePairAprioriOutput filePairOutput = pairFiles.get(fileFile);

            FilePairApriori apriori = new FilePairApriori(file1Issues, file2Issues,
                    filePairOutput.getIssuesIdWeight(), allConsideredIssues.size());

            fileFile.orderFilePairByConfidence(apriori);
            filePairOutput.setFilePairApriori(apriori);

            pairFileList.add(filePairOutput);
        }
        orderByFilePairConfidenceAndSupport(pairFileList);

        EntityMatrix matrix = new EntityMatrix();
        matrix.setNodes(objectsToNodes(pairFileList, FilePairAprioriOutput.getToStringHeader()));
        matricesToSave.add(matrix);

        saveTop25Matrix(pairFileList);
    }

    private void saveTop25Matrix(List<FilePairAprioriOutput> pairFileList) {
        // 25 arquivos distintos com maior confianÃ§a entre o par (coluna da esquerda)
        final Set<FilePairAprioriOutput> distinctFileOfFilePairWithHigherConfidence = new LinkedHashSet<>(25);
        final List<FilePairAprioriOutput> nodesTop25 = new ArrayList<>();
        for (FilePairAprioriOutput node : pairFileList) {
            distinctFileOfFilePairWithHigherConfidence.add(node);
            nodesTop25.add(node);
            if (distinctFileOfFilePairWithHigherConfidence.size() >= 25) {
                break;
            }
        }

        Set<Integer> totalIssues = new HashSet<>();
        Set<Integer> totalCommits = new HashSet<>();
        Set<Integer> totalCommitsFile1 = new HashSet<>();
        Set<Integer> totalCommitsFile2 = new HashSet<>();
        Set<Integer> totalDefects = new HashSet<>();
        Set<Integer> totalFutureDefects = new HashSet<>();

        Set<String> allFiles = new HashSet<>();
        Set<String> allJavaFiles = new HashSet<>();
        Set<String> allXmlFiles = new HashSet<>();
        Set<String> allOtherFiles = new HashSet<>();

        for (FilePairAprioriOutput node : nodesTop25) {
            totalIssues.addAll(node.getIssuesId());
            totalCommits.addAll(node.getCommitsId());

            totalCommitsFile1.addAll(node.getCommitsFile1Id());
            totalCommitsFile2.addAll(node.getCommitsFile2Id());

            totalFutureDefects.addAll(node.getFutureDefectIssuesId());
            totalDefects.add(node.getCommitsIdWeight());

            final String file1 = node.getFilePair().getFile1();

            allFiles.add(file1);
            if (file1.endsWith(".java")) {
                allJavaFiles.add(file1);
            } else if (file1.endsWith(".xml")) {
                allXmlFiles.add(file1);
            } else {
                allOtherFiles.add(file1);
            }

            final String file2 = node.getFilePair().getFile2();

            allFiles.add(file2);
            if (file2.endsWith(".java")) {
                allJavaFiles.add(file2);
            } else if (file2.endsWith(".xml")) {
                allXmlFiles.add(file2);
            } else {
                allOtherFiles.add(file2);
            }
        }

        FilePairAprioriOutput summary = new FilePairAprioriOutput(new FilePair("Summary", String.valueOf(allFiles.size())));
        summary.addIssueId(totalIssues.size());
        summary.addCommitId(totalCommits.size());
        summary.addCommitFile1Id(totalCommitsFile1.size());
        summary.addCommitFile2Id(totalCommitsFile2.size());
        summary.addDefectIssueId(totalDefects.size());
        summary.addFutureDefectIssuesId(totalFutureDefects.size());

        log("\n\n" + getRepository() + " " + getVersion() + " top 25 \n"
                + "Number of files: " + allFiles.size() + "\n"
                + "Number of files (JAVA): " + allJavaFiles.size() + "\n"
                + "Number of files (XML): " + allXmlFiles.size() + "\n"
                + "Number of files (Others): " + allOtherFiles.size() + "\n"
                + "Number of commits file 1: " + totalCommitsFile1.size() + "\n"
                + "Number of commits file 2: " + totalCommitsFile2.size() + "\n"
                + "Number of issues: " + totalIssues.size() + "\n"
                + "Number of commits: " + totalCommits.size() + "\n"
                + "Number of defect issues: " + totalDefects.size() + "\n"
                + "Number of future defect issues: " + totalFutureDefects.size() + "\n\n"
        );

        EntityMatrix top25 = new EntityMatrix();
        top25.setNodes(objectsToNodes(nodesTop25, FilePairAprioriOutput.getToStringHeader()));
        top25.setAdditionalFilename("top 25");
        matricesToSave.add(top25);
    }
}
