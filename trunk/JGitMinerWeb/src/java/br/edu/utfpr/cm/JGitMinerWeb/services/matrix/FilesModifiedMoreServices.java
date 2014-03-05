/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package br.edu.utfpr.cm.JGitMinerWeb.services.matrix;

import br.edu.utfpr.cm.JGitMinerWeb.dao.GenericDao;
import br.edu.utfpr.cm.JGitMinerWeb.model.matrix.EntityMatrix;
import br.edu.utfpr.cm.JGitMinerWeb.model.miner.EntityRepository;
import br.edu.utfpr.cm.JGitMinerWeb.services.matrix.auxiliary.AuxFileCountSum;
import br.edu.utfpr.cm.JGitMinerWeb.util.OutLog;
import br.edu.utfpr.cm.JGitMinerWeb.util.Util;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 *
 * @author douglas
 */
public class FilesModifiedMoreServices extends AbstractMatrixServices {

    public FilesModifiedMoreServices(GenericDao dao, OutLog out) {
        super(dao, out);
    }

    public FilesModifiedMoreServices(GenericDao dao, EntityRepository repository, List<EntityMatrix> matricesToSave, Map params, OutLog out) {
        super(dao, repository, matricesToSave, params, out);
    }

    private int getMilestoneNumber() {
        String mileNumber = params.get("milestoneNumber") + "";
        return Util.tratarStringParaInt(mileNumber);
    }

    private int getPercent() {
        String percent = params.get("percent") + "";
        return Util.tratarStringParaInt(percent);
    }

    private String getPrefixFile() {
        return params.get("prefixFile") + "%";
    }

    private String getSuffixFile() {
        return "%" + params.get("suffixFile");
    }

    @Override
    public void run() {
        if (getRepository() == null) {
            throw new IllegalArgumentException("Parâmetro Repository não pode ser nulo.");
        }

        List<AuxFileCountSum> auxs;

        if (getMilestoneNumber() != 0) {
            auxs = getFilesByMilestone();
        } else if (getBeginDate() != null
                && getEndDate() != null) {
            auxs = getFilesByDate();
        } else {
            throw new IllegalArgumentException("Informe o número do Milestone ou um Intervalo de datas.");
        }

        orderByCount(auxs);

        int total = auxs.size();
        int percent = getPercent();

        int cut = percent * total / 100;

        System.out.println("total:" + total);
        System.out.println("percent:" + percent);
        System.out.println("cut:" + cut);

        auxs = auxs.subList(0, cut);

        System.out.println("Results: " + auxs.size());

        EntityMatrix matrix = new EntityMatrix();
        matrix.setNodes(objectsToNodes(auxs));
        matricesToSave.add(matrix);
    }

    private List<AuxFileCountSum> getFilesByMilestone() {
        String jpql = "SELECT NEW " + AuxFileCountSum.class.getName() + "(f.filename, COUNT(f.filename)) "
                + "FROM EntityPullRequest p JOIN p.issue i JOIN p.repositoryCommits rc JOIN rc.files f "
                + (!getIssueLabels().isEmpty() ? "JOIN i.labels l " : "")
                + "WHERE p.repository = :repo "
                + "AND i.milestone.number >= :milestoneNumber "
                + (!getIssueLabels().isEmpty() ? "AND LOWER(l.name) IN :labels " : "")
                + "AND f.filename LIKE :prefix "
                + "AND f.filename LIKE :suffix "
                + "GROUP BY f.filename ";

        System.out.println(jpql);

        List<AuxFileCountSum> auxs = dao.selectWithParams(jpql,
                new String[]{
                    "repo",
                    "milestoneNumber",
                    !getIssueLabels().isEmpty() ? "labels" : "#none#",
                    "prefix",
                    "suffix"
                }, new Object[]{
                    getRepository(),
                    getMilestoneNumber(),
                    getIssueLabels(),
                    getPrefixFile(),
                    getSuffixFile()
                });
        return auxs;
    }

    private List<AuxFileCountSum> getFilesByDate() {
        String jpql = "SELECT NEW " + AuxFileCountSum.class.getName() + "(f.filename, COUNT(f.filename)) "
                + "FROM EntityPullRequest p JOIN p.issue i JOIN p.repositoryCommits rc JOIN rc.files f "
                + (!getIssueLabels().isEmpty() ? "JOIN i.labels l " : "")
                + "WHERE rc.repository = :repo "
                + (!getIssueLabels().isEmpty() ? "AND LOWER(l.name) IN :labels " : "")
                + "AND rc.commit.committer.dateCommitUser >= :beginDate "
                + "AND rc.commit.committer.dateCommitUser <= :endDate "
                + "AND f.filename LIKE :prefix "
                + "AND f.filename LIKE :suffix "
                + "GROUP BY f.filename ";

        System.out.println(jpql);

        List<AuxFileCountSum> auxs = dao.selectWithParams(jpql,
                new String[]{
                    "repo",
                    !getIssueLabels().isEmpty() ? "labels" : "#none#",
                    "beginDate",
                    "endDate",
                    "prefix",
                    "suffix"
                }, new Object[]{
                    getRepository(),
                    getIssueLabels(),
                    getBeginDate(),
                    getEndDate(),
                    getPrefixFile(),
                    getSuffixFile()
                });
        return auxs;
    }

    private void orderByCount(List<AuxFileCountSum> auxs) {
        Collections.sort(auxs, new Comparator() {
            @Override
            public int compare(Object o1, Object o2) {
                AuxFileCountSum v1 = (AuxFileCountSum) o1;
                AuxFileCountSum v2 = (AuxFileCountSum) o2;

                Comparable i1 = (Comparable) v1.getCount();
                Comparable i2 = (Comparable) v2.getCount();
                return i2.compareTo(i1);
            }
        });
    }

    @Override
    public String getHeadCSV() {
        return "file;count";
    }

    private List<String> getIssueLabels() {
        List<String> labels = new ArrayList<>();
        for (String label : (params.get("issueLabels") + "").split(";")) {
            label = label.trim();
            if (!label.isEmpty()) {
                labels.add(label);
            }
        }
        return labels;
    }
}
