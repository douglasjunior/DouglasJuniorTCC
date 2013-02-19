/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package br.edu.utfpr.cm.JGitMinerWeb.services.matriz;

import br.edu.utfpr.cm.JGitMinerWeb.dao.GenericDao;
import br.edu.utfpr.cm.JGitMinerWeb.pojo.matriz.EntityMatrizRecord;
import br.edu.utfpr.cm.JGitMinerWeb.pojo.miner.EntityCommitUser;
import br.edu.utfpr.cm.JGitMinerWeb.pojo.miner.EntityRepository;
import br.edu.utfpr.cm.JGitMinerWeb.pojo.miner.EntityUser;
import br.edu.utfpr.cm.JGitMinerWeb.services.matriz.auxiliary.AuxUserFilePull;
import br.edu.utfpr.cm.JGitMinerWeb.services.matriz.auxiliary.AuxUserUserPullFile;
import br.edu.utfpr.cm.JGitMinerWeb.util.JsfUtil;
import br.edu.utfpr.cm.JGitMinerWeb.util.Util;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 *
 * @author douglas
 */
public class UserModifySameFileInPullRequestServices extends MatrizServices {

    public UserModifySameFileInPullRequestServices(GenericDao dao) {
        super(dao);
    }

    public UserModifySameFileInPullRequestServices(GenericDao dao, EntityRepository repository, Map params) {
        super(dao, repository, params);
    }

//    public Date getBegin() {
//        Date begin = getDateParam("beginDate");
//        if (begin == null) {
//            try {
//                return new SimpleDateFormat("MM/dd/yyyy").parse("01/01/1970");
//            } catch (ParseException ex) {
//                ex.printStackTrace();
//            }
//        }
//        return begin;
//    }
//    public Date getEnd() {
//        Date end = getDateParam("endDate");
//        if (end == null) {
//            try {
//                return new SimpleDateFormat("MM/dd/yyyy").parse("01/01/2999");
//            } catch (ParseException ex) {
//                ex.printStackTrace();
//            }
//        }
//        return end;
//    }
    public Long getBeginPullRequestNumber() {
        String idPull = params.get("beginPull") + "";
        if (idPull == null || idPull.isEmpty()) {
            return 0l;
        }
        return Util.tratarStringParaLong(idPull);
    }

    public Long getEndPullRequestNumber() {
        String idPull = params.get("endPull") + "";
        if (idPull == null || idPull.isEmpty()) {
            return 99999999999999l;
        }
        return Util.tratarStringParaLong(idPull);
    }

    @Override
    public void run() {
        System.out.println(params);

        if (repository == null) {
            throw new IllegalArgumentException("Parâmetro Repository não pode ser nulo.");
        }

        List<EntityMatrizRecord> records = new ArrayList<EntityMatrizRecord>();

        String jpql = "SELECT NEW br.edu.utfpr.cm.JGitMinerWeb.services.matriz.auxiliary.AuxUserFilePull(uc, p, f.filename) "
                + "FROM "
                + "EntityPullRequest p JOIN p.repositoryCommits c JOIN c.commit cm JOIN cm.committer uc JOIN c.files f "
                + "WHERE "
                + "p.number >= :beginPull AND "
                + "p.number <= :endPull AND "
                + "p.repository = :repo "
                + "ORDER BY p.number";

        System.out.println(jpql);

        List<AuxUserFilePull> query = dao.selectWithParams(jpql, new String[]{"repo", "beginPull", "endPull"}, new Object[]{getRepository(), getBeginPullRequestNumber(), getEndPullRequestNumber()});

        System.out.println(getRepository() + " " + getBeginPullRequestNumber() + " " + getEndPullRequestNumber());

        System.out.println("query: " + query.size());

        List<AuxUserUserPullFile> controls = new ArrayList<AuxUserUserPullFile>(); // lista pata controle de repetição
        for (AuxUserFilePull aufp : query) {
            for (AuxUserFilePull aufp2 : query) {
                if (!aufp.getCommitUser().equals(aufp2.getCommitUser()) // se o user é diferente
                        && aufp.getPull().equals(aufp2.getPull()) // se o file é igual
                        && aufp.getFile().equals(aufp2.getFile())) { // e se o pull é igual
                    // então eles mexeram no mesmo arquivo no mesmo pull request
                    AuxUserUserPullFile control = new AuxUserUserPullFile(aufp.getCommitUser(), aufp2.getCommitUser(), aufp.getPull(), aufp.getFile());
                    // verifica se já foi gravado registo semelhante
                    if (!controls.contains(control)) {
                        // salva o controle
                        controls.add(control);
                        // aqui sim ele cria o registro a ser salvo
                        EntityMatrizRecord rec = new EntityMatrizRecord(EntityCommitUser.class, aufp.getCommitUser().getId(),
                                EntityCommitUser.class, aufp2.getCommitUser().getId(), aufp.getPull().getNumber(), aufp.getFile());
                         records.add(rec);
                    }
                }
            }
        }
        setRecords(records);
    }

    @Override
    public String convertToCSV(List<EntityMatrizRecord> records) {
        StringBuilder sb = new StringBuilder("user;user2;file\n");
        for (EntityMatrizRecord record : records) {
            EntityCommitUser user = dao.findByID(record.getValueX(), EntityCommitUser.class);
            EntityCommitUser user2 = dao.findByID(record.getValueY(), EntityCommitUser.class);
            sb.append(user.getEmail()).append(JsfUtil.TOKEN_SEPARATOR).append(user2.getEmail()).append(JsfUtil.TOKEN_SEPARATOR).append(record.getValueZ()).append("\n");
        }
        return sb.toString();
    }
}
