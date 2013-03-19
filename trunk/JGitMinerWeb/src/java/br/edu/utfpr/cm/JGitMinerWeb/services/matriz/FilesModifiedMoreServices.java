/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package br.edu.utfpr.cm.JGitMinerWeb.services.matriz;

import br.edu.utfpr.cm.JGitMinerWeb.dao.GenericDao;
import br.edu.utfpr.cm.JGitMinerWeb.pojo.matriz.EntityMatrizNode;
import br.edu.utfpr.cm.JGitMinerWeb.pojo.miner.EntityRepository;
import br.edu.utfpr.cm.JGitMinerWeb.services.matriz.auxiliary.AuxFileCount;
import br.edu.utfpr.cm.JGitMinerWeb.util.JsfUtil;
import br.edu.utfpr.cm.JGitMinerWeb.util.Util;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
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
        } else {
            auxs = getFilesByDate();
        }

        orderByCount(auxs);

        int total = auxs.size();
        int percent = getPercent();

        int cut = percent * total / 100;

        System.out.println("total:" + total);
        System.out.println("percent:" + percent);
        System.out.println("cut:" + cut);

        List<EntityMatrizNode> nodes = new ArrayList<>();

        for (AuxFileCount aux : auxs.subList(0, cut)) {
            nodes.add(new EntityMatrizNode(aux.getFileName(), "", aux.getCount()));
        }

        setNodes(nodes);

        System.out.println("Results: " + nodes.size());
    }

    private List<AuxFileCount> getFilesByMilestone() {
        String jpql = "SELECT NEW " + AuxFileCount.class.getName() + "(f.filename, COUNT(f.filename)) "
                + "FROM EntityPullRequest p JOIN p.issue i JOIN p.repositoryCommits rc JOIN rc.files f "
                + "WHERE p.repository = :repo "
                + "AND i.milestone.number >= :milestoneNumber "
                + "AND f.filename LIKE :prefix "
                + "AND f.filename LIKE :suffix "
                + "GROUP BY f.filename ";

        System.out.println(jpql);

        List<AuxFileCount> auxs = dao.selectWithParams(jpql,
                new String[]{
                    "repo",
                    "milestoneNumber",
                    "prefix",
                    "suffix"
                }, new Object[]{
                    getRepository(),
                    getMilestoneNumber(),
                    getPrefixFile(),
                    getSuffixFile()
                });
        return auxs;
    }

    private List<AuxFileCount> getFilesByDate() {
        String jpql = "SELECT NEW " + AuxFileCount.class.getName() + "(f.filename, COUNT(f.filename)) "
                + "FROM EntityRepositoryCommit rc JOIN rc.files f "
                + "WHERE rc.repository = :repo "
                + "AND rc.commit.committer.dateCommitUser >= :beginDate "
                + "AND rc.commit.committer.dateCommitUser <= :endDate "
                + "AND f.filename LIKE :prefix "
                + "AND f.filename LIKE :suffix "
                + "GROUP BY f.filename ";

        System.out.println(jpql);

        List<AuxFileCount> auxs = dao.selectWithParams(jpql,
                new String[]{
                    "repo",
                    "beginDate",
                    "endDate",
                    "prefix",
                    "suffix"
                }, new Object[]{
                    getRepository(),
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
    public String convertToCSV(Collection<EntityMatrizNode> nodes) {
        StringBuilder sb = new StringBuilder("file;count\n");
        for (EntityMatrizNode node : nodes) {
            sb.append(node.getFrom()).append(JsfUtil.TOKEN_SEPARATOR);
            sb.append(Util.tratarDoubleParaString(node.getWeight(), 0)).append("\n");
        }
        return sb.toString();
    }
}
