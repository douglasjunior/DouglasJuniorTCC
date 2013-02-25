/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package br.edu.utfpr.cm.JGitMinerWeb.services.matriz;

import br.edu.utfpr.cm.JGitMinerWeb.dao.GenericDao;
import br.edu.utfpr.cm.JGitMinerWeb.pojo.matriz.EntityMatrizRecord;
import br.edu.utfpr.cm.JGitMinerWeb.pojo.miner.EntityCommitUser;
import br.edu.utfpr.cm.JGitMinerWeb.pojo.miner.EntityRepository;
import br.edu.utfpr.cm.JGitMinerWeb.services.matriz.auxiliary.AuxUserFileCount;
import br.edu.utfpr.cm.JGitMinerWeb.util.JsfUtil;
import br.edu.utfpr.cm.JGitMinerWeb.util.Util;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 *
 * @author douglas
 */
public class UserModifyFileInMilestoneServices extends AbstractMatrizServices {

    public UserModifyFileInMilestoneServices(GenericDao dao) {
        super(dao);
    }

    public UserModifyFileInMilestoneServices(GenericDao dao, EntityRepository repository, Map params) {
        super(dao, repository, params);
    }

    private int getMilestoneNumber() {
        String mileNumber = params.get("milestoneNumber") + "";
        return Util.tratarStringParaInt(mileNumber);
    }

    private String getPrefixFile() {
        return params.get("prefixFile") + "%";
    }

    private String getSuffixFile() {
        return "%" + params.get("suffixFile");
    }

    @Override
    public void run() {
        System.out.println(params);

        if (getRepository() == null) {
            throw new IllegalArgumentException("Parâmetro Repository não pode ser nulo.");
        }

        int mileNumber = getMilestoneNumber();

        if (mileNumber <= 0) {
            throw new IllegalArgumentException("Numero do Milestone inválido.");
        }

        String jpql = "SELECT NEW br.edu.utfpr.cm.JGitMinerWeb.services.matriz.auxiliary.AuxUserFileCount(cu, f.filename, COUNT(f.filename)) "
                + "FROM "
                + "EntityPullRequest p JOIN p.issue i JOIN i.milestone m JOIN p.repositoryCommits rc JOIN rc.files f JOIN rc.commit c JOIN c.committer cu "
                + "WHERE "
                + "p.repository = :repository AND "
                + "m.number = :milestoneNumber AND "
                + "f.filename LIKE :prefixFile AND "
                + "f.filename LIKE :suffixFile "
                + "GROUP BY cu, f.filename";

        System.out.println(jpql);

        List<AuxUserFileCount> query = dao.selectWithParams(jpql,
                new String[]{"repository", "milestoneNumber", "prefixFile", "suffixFile"},
                new Object[]{getRepository(), mileNumber, getPrefixFile(), getSuffixFile()});

        System.out.println("query: " + query.size());

        List<EntityMatrizRecord> records = new ArrayList<EntityMatrizRecord>();
        for (AuxUserFileCount aux : query) {
            EntityMatrizRecord rec = new EntityMatrizRecord(
                    aux.getCommitUser().getClass().getName(), aux.getCommitUser().getId(),
                    "", aux.getFileName(),
                    "", aux.getCount());
            records.add(rec);
        }
        setRecords(records);
    }

    @Override
    public String convertToCSV(List<EntityMatrizRecord> records) {
        StringBuilder sb = new StringBuilder("user;file;count\n");
        for (EntityMatrizRecord record : records) {
            EntityCommitUser user = dao.findByID(record.getValueX(), EntityCommitUser.class);
            sb.append(user.getEmail()).append(JsfUtil.TOKEN_SEPARATOR);
            sb.append(record.getValueY()).append(JsfUtil.TOKEN_SEPARATOR);
            sb.append(record.getValueZ()).append("\n");
        }
        return sb.toString();
    }
}
