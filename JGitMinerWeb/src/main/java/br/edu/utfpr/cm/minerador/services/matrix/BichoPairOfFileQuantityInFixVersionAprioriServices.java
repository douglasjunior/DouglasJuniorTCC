package br.edu.utfpr.cm.minerador.services.matrix;

import br.edu.utfpr.cm.JGitMinerWeb.dao.BichoDAO;
import br.edu.utfpr.cm.JGitMinerWeb.dao.BichoFileDAO;
import br.edu.utfpr.cm.JGitMinerWeb.dao.BichoPairFileDAO;
import br.edu.utfpr.cm.JGitMinerWeb.dao.GenericBichoDAO;
import br.edu.utfpr.cm.JGitMinerWeb.model.matrix.EntityMatrix;
import br.edu.utfpr.cm.JGitMinerWeb.model.matrix.EntityMatrixNode;
import br.edu.utfpr.cm.JGitMinerWeb.util.OutLog;
import br.edu.utfpr.cm.JGitMinerWeb.util.Util;
import br.edu.utfpr.cm.minerador.services.matrix.model.FilePair;
import br.edu.utfpr.cm.minerador.services.matrix.model.FilePairApriori;
import br.edu.utfpr.cm.minerador.services.matrix.model.FilePairAprioriOutput;
import br.edu.utfpr.cm.minerador.services.metric.Cacher;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Report 1 - Projects Pair of File per Version
 *
 * @author Rodrigo Kuroda
 */
public class BichoPairOfFileQuantityInFixVersionAprioriServices extends AbstractBichoMatrixServices {

    public BichoPairOfFileQuantityInFixVersionAprioriServices() {
        super(null, null);
    }

    public BichoPairOfFileQuantityInFixVersionAprioriServices(GenericBichoDAO dao, OutLog out) {
        super(dao, out);
    }

    public BichoPairOfFileQuantityInFixVersionAprioriServices(GenericBichoDAO dao, String repository, List<EntityMatrix> matricesToSave, Map<Object, Object> params, OutLog out) {
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

        final int minOccurrencesInOneVersion = 1;

        log("\n --------------- "
                + new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new Date())
                + "\n --------------- \n");
        for (String project : getSelectedProjects()) {
            BichoDAO bichoDAO = new BichoDAO(dao, project, getMaxFilesPerCommit());
            BichoFileDAO bichoFileDAO = new BichoFileDAO(dao, project, getMaxFilesPerCommit());
            BichoPairFileDAO bichoPairFileDAO = new BichoPairFileDAO(dao, project, getMaxFilesPerCommit());

            final Map<FilePair, Integer[]> pairFilesOccurrencesPerVersion = new HashMap<>();
            final List<String> fixVersionOrdered = bichoDAO.selectFixVersionOrdered();
            final int totalFixVersion = fixVersionOrdered.size();

            for (String version : fixVersionOrdered) {
                out.printLog("Maximum files per commit: " + getMaxFilesPerCommit());
                out.printLog("Minimum files per commit: " + getMinFilesPerCommit());
                Cacher cacher = new Cacher(bichoFileDAO);

                final Map<FilePair, FilePairAprioriOutput> pairFiles = new HashMap<>();
                identifyFilePairs(pairFiles, bichoDAO, version, bichoFileDAO);

                out.printLog("Result: " + pairFiles.size());

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

                for (Map.Entry<FilePair, FilePairAprioriOutput> entrySet1 : pairFiles.entrySet()) {
                    final FilePair key = entrySet1.getKey();
                    final int occurrences = entrySet1.getValue().getIssuesId().size();
                    final int indexOfVersion = fixVersionOrdered.indexOf(version);

                    if (pairFilesOccurrencesPerVersion.containsKey(key)) {
                        final Integer[] quantity = pairFilesOccurrencesPerVersion.get(key);
                        if (quantity[indexOfVersion] == null) {
                            quantity[indexOfVersion] = occurrences;
                        } else {
                            quantity[indexOfVersion] += occurrences;
                        }
                    } else {
                        final Integer[] quantity = new Integer[totalFixVersion];
                        quantity[indexOfVersion] = occurrences;
                        pairFilesOccurrencesPerVersion.put(key, quantity);
                    }
                }

    //            EntityMatrix matrix = new EntityMatrix();
                //            matrix.setNodes(objectsToNodes(pairFileList, FilePairAprioriOutput.getToStringHeaderAprioriOnly()));
                //            matrix.setNodes(objectsToNodes(pairFilesOccurrencesPerVersion, fixVersionOrdered));
                //            matricesToSave.add(matrix);
            }

            Map<FilePair, Integer[]> filteredOccurrencesPerVersion = new HashMap<>();
            if (minOccurrencesInOneVersion > 1) {
                nextPairFile:
                for (Map.Entry<FilePair, Integer[]> entrySet : pairFilesOccurrencesPerVersion.entrySet()) {
                    FilePair key = entrySet.getKey();
                    Integer[] occurrencesPerVesion = entrySet.getValue();
                    for (Integer occurrences : occurrencesPerVesion) {
                        if (occurrences != null && occurrences >= minOccurrencesInOneVersion) {
                            filteredOccurrencesPerVersion.put(key, occurrencesPerVesion);
                            continue nextPairFile;
                        }
                    }
                }
            } else {
                filteredOccurrencesPerVersion = pairFilesOccurrencesPerVersion;
            }

            EntityMatrix matrix = new EntityMatrix();
            //        matrix.setNodes(objectsToNodes(pairFileList, FilePairAprioriOutput.getToStringHeaderAprioriOnly()));
            matrix.setNodes(objectsToNodes(filteredOccurrencesPerVersion, fixVersionOrdered));
            matrix.setRepository(project);
            matrix.getParams().put("filename", "cochanges per version");
            matrix.getParams().put("minIssueInAtLeastOneVersion", minOccurrencesInOneVersion);
            matricesToSave.add(matrix);
        }
    }

    protected static List<EntityMatrixNode> objectsToNodes(final Map<FilePair, Integer[]> list, final List<String> versions) {
        List<EntityMatrixNode> nodes = new ArrayList<>();
        StringBuilder header = new StringBuilder("file1;file2");
        for (String version : versions) {
            header.append(";").append(version);
        }
        nodes.add(new EntityMatrixNode(header.toString()));

        for (Map.Entry<FilePair, Integer[]> entrySet : list.entrySet()) {
            FilePair filePair = entrySet.getKey();
            Integer[] value = entrySet.getValue();
            StringBuilder row = new StringBuilder(filePair.toString());
            for (Integer ocurrencesQuantity : value) {
                row.append(ocurrencesQuantity == null ? 0 : ocurrencesQuantity).append(";");
            }
            nodes.add(new EntityMatrixNode(row.toString()));
        }
        return nodes;
    }

    // TODO parameterize
    private List<String> getSelectedProjects() {
        return Arrays.asList(new String[]{
            "camel",
            "cassandra",
            "cloudstack",
            "cxf",
            "derby",
            "hadoop",
            "hbase",
            "hive",
            "lucene",
            "solr",});
    }
}
