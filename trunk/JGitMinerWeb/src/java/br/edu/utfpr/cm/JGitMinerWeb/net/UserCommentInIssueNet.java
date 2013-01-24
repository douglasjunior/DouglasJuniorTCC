/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package br.edu.utfpr.cm.JGitMinerWeb.net;

import br.edu.utfpr.cm.JGitMinerWeb.dao.GenericDao;
import br.edu.utfpr.cm.JGitMinerWeb.edge.AbstractEdge;
import br.edu.utfpr.cm.JGitMinerWeb.pojo.EntityRepository;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author douglas
 */
public class UserCommentInIssueNet extends AbstractNet {

    public UserCommentInIssueNet(EntityRepository repository, Date begin, Date end, GenericDao dao) {
        super(repository, begin, end, dao);
    }

    @Override
    public Date getBegin() {
        if (super.getBegin() == null) {
            try {
                return new SimpleDateFormat("dd/MM/yyyy").parse("01/01/1970");
            } catch (ParseException ex) {
                ex.printStackTrace();
            }
        }
        return super.getBegin();
    }

    @Override
    public Date getEnd() {
        if (super.getEnd() == null) {
            try {
                return new SimpleDateFormat("dd/MM/yyyy").parse("01/01/2999");
            } catch (ParseException ex) {
                ex.printStackTrace();
            }
        }
        return super.getEnd();
    }

    @Override
    public void run() {
        if (repository == null) {
            throw new IllegalArgumentException("Parâmetro Repository não pode ser nulo.");
        }

        String jpql = "SELECT NEW br.edu.utfpr.cm.JGitMinerWeb.edge.UserCommentInIssueEdge(i, u, count(u))"
                + "FROM EntityIssue i JOIN i.comments c JOIN c.user u "
                + "WHERE i.repository = :repo "
                + "AND i.createdAt >= :dataInicial "
                + "AND i.createdAt <= :dataFinal "
                + "GROUP BY i, u "
                + "ORDER BY u.login, i.number";

        System.out.println(jpql);

        net = dao.selectWithParams(jpql, new String[]{"repo", "dataInicial", "dataFinal"}, new Object[]{getRepository(), getBegin(), getEnd()});

        System.out.println("Results: " + net.size());
    }
}
