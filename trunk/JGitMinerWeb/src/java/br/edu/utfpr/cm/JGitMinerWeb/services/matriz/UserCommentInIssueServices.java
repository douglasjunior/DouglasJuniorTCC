/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package br.edu.utfpr.cm.JGitMinerWeb.services.matriz;

import br.edu.utfpr.cm.JGitMinerWeb.dao.GenericDao;
import br.edu.utfpr.cm.JGitMinerWeb.pojo.miner.EntityRepository;
import br.edu.utfpr.cm.JGitMinerWeb.services.matriz.nodes.NodeGeneric;
import br.edu.utfpr.cm.JGitMinerWeb.util.Util;
import java.util.List;
import java.util.Map;

/**
 *
 * @author douglas
 */
public class UserCommentInIssueServices extends AbstractMatrizServices {

    public UserCommentInIssueServices(GenericDao dao) {
        super(dao);
    }

    public UserCommentInIssueServices(GenericDao dao, EntityRepository repository, Map params) {
        super(dao, repository, params);
        System.out.println(params);
    }

    private int getMilestoneNumber() {
        String mileNumber = params.get("milestoneNumber") + "";
        return Util.tratarStringParaInt(mileNumber);
    }

    @Override
    public void run() {
        if (getRepository() == null) {
            throw new IllegalArgumentException("Parâmetro Repository não pode ser nulo.");
        }

        List<NodeGeneric> nodes;
        
        if (getMilestoneNumber() != 0) {
            nodes = getFilesByMilestone();
        } else if (getBeginDate() != null
                && getEndDate() != null) {
            nodes = getFilesByDate();
        } else {
            throw new IllegalArgumentException("Informe o número do Milestone ou um Intervalo de datas.");
        }

        System.out.println("Nodes: " + nodes.size());

        addToEntityMatrizNodeList(nodes);
    }

    @Override
    public String getHeadCSV() {
        return "user;issue;countComments";
    }

    private List<NodeGeneric> getFilesByMilestone() {
        String jpql = "SELECT NEW " + NodeGeneric.class.getName() + "(u.login, i.number, count(u.id)) "
                + "FROM EntityIssue i JOIN i.comments c JOIN c.user u "
                + "WHERE i.repository = :repo "
                + "AND i.milestone.number >= :milestoneNumber "
                + "GROUP BY u.login, i.number";

        System.out.println(jpql);

        List<NodeGeneric> nodes = dao.selectWithParams(jpql,
                new String[]{
                    "repo",
                    "milestoneNumber"
                }, new Object[]{
                    getRepository(),
                    getMilestoneNumber()
                });
        return nodes;
    }

    private List<NodeGeneric> getFilesByDate() {
        String jpql = "SELECT NEW " + NodeGeneric.class.getName() + "(u.login, i.number, count(u.id)) "
                + "FROM EntityIssue i JOIN i.comments c JOIN c.user u "
                + "WHERE i.repository = :repo "
                + "AND i.createdAt >= :dataInicial "
                + "AND i.createdAt <= :dataFinal "
                + "GROUP BY u.login, i.number";

        System.out.println(jpql);

        List<NodeGeneric> nodes = dao.selectWithParams(jpql,
                new String[]{
                    "repo",
                    "dataInicial",
                    "dataFinal"
                }, new Object[]{
                    getRepository(),
                    getBeginDate(),
                    getEndDate()
                });
        return nodes;
    }
}
