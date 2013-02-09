/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package br.edu.utfpr.cm.JGitMinerWeb.services.matriz;

import br.edu.utfpr.cm.JGitMinerWeb.dao.GenericDao;
import br.edu.utfpr.cm.JGitMinerWeb.pojo.matriz.EntityMatrizRecord;
import br.edu.utfpr.cm.JGitMinerWeb.pojo.miner.EntityIssue;
import br.edu.utfpr.cm.JGitMinerWeb.pojo.miner.EntityRepository;
import br.edu.utfpr.cm.JGitMinerWeb.pojo.miner.EntityUser;
import br.edu.utfpr.cm.JGitMinerWeb.util.JsfUtil;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

/**
 *
 * @author douglas
 */
public class UserCommentInIssueMatrizServices extends MatrizServices {

    public UserCommentInIssueMatrizServices(GenericDao dao) {
        super(dao);
    }

    public UserCommentInIssueMatrizServices(GenericDao dao, EntityRepository repository, Map params) {
        super(dao, repository, params);
        System.out.println(params);
    }

    public Date getBegin() {
        Date begin = getDateParam("begin");
        if (begin == null) {
            try {
                return new SimpleDateFormat("MM/dd/yyyy").parse("01/01/1970");
            } catch (ParseException ex) {
                ex.printStackTrace();
            }
        }
        return begin;
    }

    public Date getEnd() {
        Date end = getDateParam("end");
        if (end == null) {
            try {
                return new SimpleDateFormat("MM/dd/yyyy").parse("01/01/2999");
            } catch (ParseException ex) {
                ex.printStackTrace();
            }
        }
        return end;
    }

    @Override
    public void run() {
        if (repository == null) {
            throw new IllegalArgumentException("Parâmetro Repository não pode ser nulo.");
        }

        String jpql = "SELECT NEW br.edu.utfpr.cm.JGitMinerWeb.pojo.matriz.EntityMatrizRecord(\"" + EntityUser.class.getName() + "\", u.id,\"" + EntityIssue.class.getName() + "\" , i.id, count(u.id))"
                + "FROM EntityIssue i JOIN i.comments c JOIN c.user u "
                + "WHERE i.repository = :repo "
                + "AND i.createdAt >= :dataInicial "
                + "AND i.createdAt <= :dataFinal "
                + "GROUP BY i.id, u.id ";
        //      + "ORDER BY u.id, i.id";

        System.out.println(jpql);

        records = dao.selectWithParams(jpql, new String[]{"repo", "dataInicial", "dataFinal"}, new Object[]{getRepository(), getBegin(), getEnd()});

        System.out.println("Results: " + records.size());
    }

    @Override
    public String convertToCSV() {
        StringBuilder sb = new StringBuilder();
        for (EntityMatrizRecord record : records) {
            EntityUser user = dao.findByID(record.getIdX(), record.getClassX());
            EntityIssue issue = dao.findByID(record.getIdY(), record.getClassY());
            sb.append(user.getLogin()).append(JsfUtil.TOKEN_SEPARATOR).append(issue.getNumber()).append(JsfUtil.TOKEN_SEPARATOR).append(record.getAmount()).append("\n");
        }
        return sb.toString();
    }
}
