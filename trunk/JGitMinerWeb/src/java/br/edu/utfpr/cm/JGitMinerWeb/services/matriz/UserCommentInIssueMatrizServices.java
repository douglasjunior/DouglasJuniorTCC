/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package br.edu.utfpr.cm.JGitMinerWeb.services.matriz;

import br.edu.utfpr.cm.JGitMinerWeb.dao.GenericDao;
import br.edu.utfpr.cm.JGitMinerWeb.pojo.matriz.EntityMatrizNode;
import br.edu.utfpr.cm.JGitMinerWeb.pojo.miner.EntityRepository;
import br.edu.utfpr.cm.JGitMinerWeb.util.JsfUtil;
import br.edu.utfpr.cm.JGitMinerWeb.util.Util;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 *
 * @author douglas
 */
public class UserCommentInIssueMatrizServices extends AbstractMatrizServices {

    public UserCommentInIssueMatrizServices(GenericDao dao) {
        super(dao);
    }

    public UserCommentInIssueMatrizServices(GenericDao dao, EntityRepository repository, Map params) {
        super(dao, repository, params);
        System.out.println(params);
    }

    public Date getBegin() {
        return getDateParam("beginDate");
    }

    public Date getEnd() {
        return getDateParam("endDate");
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

        String jpql = "SELECT NEW " + EntityMatrizNode.class.getName() + "(u.login, i.number, count(u.id)) "
                + "FROM EntityIssue i JOIN i.comments c JOIN c.user u "
                + "WHERE i.repository = :repo "
                + (getMilestoneNumber() != 0 ? "AND i.milestone.number >= :milestoneNumber " : "")
                + (getBegin() != null ? "AND i.createdAt >= :dataInicial " : "")
                + (getEnd() != null ? "AND i.createdAt <= :dataFinal " : "")
                + "GROUP BY u.login, i.number";

        System.out.println(jpql);

        List<EntityMatrizNode> nodes = dao.selectWithParams(jpql,
                new String[]{
                    "repo",
                    getMilestoneNumber() != 0 ? "milestoneNumber" : "#none#",
                    getBegin() != null ? "dataInicial" : "#none#",
                    getEnd() != null ? "dataFinal" : "#none#"
                }, new Object[]{
                    getRepository(),
                    getBegin(),
                    getEnd()
                });

        setNodes(nodes);

        System.out.println("Results: " + nodes.size());
    }

    @Override
    public String convertToCSV(Collection<EntityMatrizNode> nodes) {
        StringBuilder sb = new StringBuilder("user;issue;countComments\n");
        for (EntityMatrizNode node : nodes) {
            sb.append(node.getFrom()).append(JsfUtil.TOKEN_SEPARATOR);
            sb.append(node.getTo()).append(JsfUtil.TOKEN_SEPARATOR);
            sb.append(Util.tratarDoubleParaString(node.getWeight(), 0)).append("\n");
        }
        return sb.toString();
    }
}
