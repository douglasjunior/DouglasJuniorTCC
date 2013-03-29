/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package br.edu.utfpr.cm.JGitMinerWeb.services.matriz;

import br.edu.utfpr.cm.JGitMinerWeb.dao.GenericDao;
import br.edu.utfpr.cm.JGitMinerWeb.pojo.miner.EntityRepository;
import br.edu.utfpr.cm.JGitMinerWeb.services.matriz.auxiliary.AuxFileCount;
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
public class FilesModifiedMoreServices extends AbstractMatrizServices {

    public FilesModifiedMoreServices(GenericDao dao) {
        super(dao);
    }

    public FilesModifiedMoreServices(GenericDao dao, EntityRepository repository, Map params) {
        super(dao, repository, params);
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

        List<AuxFileCount> auxs;

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

        addToEntityMatrizNodeList(auxs);
    }

    private List<AuxFileCount> getFilesByMilestone() {
        String jpql = "SELECT NEW " + AuxFileCount.class.getName() + "(f.filename, COUNT(f.filename)) "
                + "FROM EntityPullRequest p JOIN p.issue i JOIN p.repositoryCommits rc JOIN rc.files f "
                + (!getIssueLabels().isEmpty() ? "JOIN i.labels l " : "")
                + "WHERE p.repository = :repo "
                + "AND i.milestone.number >= :milestoneNumber "
                + (!getIssueLabels().isEmpty() ? "AND LOWER(l.name) IN :labels " : "")
                + "AND f.filename LIKE :prefix "
                + "AND f.filename LIKE :suffix "
                + "GROUP BY f.filename ";

        System.out.println(jpql);

        List<AuxFileCount> auxs = dao.selectWithParams(jpql,
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

    private List<AuxFileCount> getFilesByDate() {
        String jpql = "SELECT NEW " + AuxFileCount.class.getName() + "(f.filename, COUNT(f.filename)) "
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

        List<AuxFileCount> auxs = dao.selectWithParams(jpql,
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

    private void orderByCount(List<AuxFileCount> auxs) {
        Collections.sort(auxs, new Comparator() {
            @Override
            public int compare(Object o1, Object o2) {
                AuxFileCount v1 = (AuxFileCount) o1;
                AuxFileCount v2 = (AuxFileCount) o2;

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
