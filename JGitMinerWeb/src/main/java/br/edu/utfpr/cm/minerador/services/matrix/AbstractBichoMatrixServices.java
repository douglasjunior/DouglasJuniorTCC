package br.edu.utfpr.cm.minerador.services.matrix;

import br.edu.utfpr.cm.JGitMinerWeb.dao.GenericBichoDAO;
import br.edu.utfpr.cm.JGitMinerWeb.model.matrix.EntityMatrix;
import br.edu.utfpr.cm.JGitMinerWeb.model.matrix.EntityMatrixNode;
import br.edu.utfpr.cm.JGitMinerWeb.util.OutLog;
import br.edu.utfpr.cm.minerador.services.AbstractBichoServices;
import br.edu.utfpr.cm.minerador.services.matrix.model.FilePair;
import br.edu.utfpr.cm.minerador.services.matrix.model.FilePairOutput;
import br.edu.utfpr.cm.minerador.services.matrix.model.FilePath;
import br.edu.utfpr.cm.minerador.services.matrix.model.Issue;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author douglas
 */
public abstract class AbstractBichoMatrixServices extends AbstractBichoServices {

    private final String repository;
    protected final List<EntityMatrix> matricesToSave;

    public AbstractBichoMatrixServices(GenericBichoDAO dao, OutLog out) {
        super(dao, out);
        this.repository = null;
        this.matricesToSave = null;
    }

    public AbstractBichoMatrixServices(GenericBichoDAO dao, String repository, List<EntityMatrix> matricesToSave, Map<Object, Object> params, OutLog out) {
        super(dao, params, out);
        this.repository = repository;
        this.matricesToSave = matricesToSave;
    }

    public String getRepository() {
        return repository;
    }

    @Override
    public abstract void run();

    protected static List<EntityMatrixNode> objectsToNodes(Collection<? extends Object> list, String header) {
        List<EntityMatrixNode> nodes = new ArrayList<>();
        nodes.add(new EntityMatrixNode(header));
        for (Object value : list) {
            nodes.add(new EntityMatrixNode(value.toString()));
        }
        return nodes;
    }

    protected void pairFiles(List<FilePath> commitedFiles, Map<FilePair, FilePairOutput> pairFiles, Issue issue, Set<Integer> allDefectIssues, Set<Integer> allConsideredCommits) {
        for (int i = 0; i < commitedFiles.size(); i++) {
            FilePath file1 = commitedFiles.get(i);
            for (int j = i + 1; j < commitedFiles.size(); j++) {
                FilePath file2 = commitedFiles.get(j);
                if (!file1.getFilePath().equals(file2.getFilePath())) {
                    FilePair filePair = new FilePair(file1.getFilePath(), file2.getFilePath());
                    FilePairOutput filePairOutput;

                    if (pairFiles.containsKey(filePair)) {
                        filePairOutput = pairFiles.get(filePair);
                    } else {
                        filePairOutput = new FilePairOutput(filePair);
                        pairFiles.put(filePair, filePairOutput);
                    }

                    filePairOutput.addIssueId(issue.getId());

                    if ("Bug".equals(issue.getType())) {
                        filePairOutput.addDefectIssueId(issue.getId());
                        allDefectIssues.add(issue.getId());
                    }

                    filePairOutput.addCommitId(file1.getCommitId());
                    filePairOutput.addCommitId(file2.getCommitId());

                    filePairOutput.addCommitFile1Id(file1.getCommitId());
                    filePairOutput.addCommitFile2Id(file2.getCommitId());

                    allConsideredCommits.add(file1.getCommitId());
                    allConsideredCommits.add(file2.getCommitId());
                }
            }
        }
    }

    protected void log(String log) {
        try {
            FileWriter w = new FileWriter(new java.io.File(System.getProperty("user.home") + "\\statistics.txt"), true);
            w.append(log);
            w.flush();
            w.close();
        } catch (Exception e) {
            System.err.println("Error to write log. " + e.getMessage());
        }
    }

}
