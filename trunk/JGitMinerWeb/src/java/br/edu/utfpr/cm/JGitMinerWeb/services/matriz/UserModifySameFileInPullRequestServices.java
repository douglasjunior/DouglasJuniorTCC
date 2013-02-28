/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package br.edu.utfpr.cm.JGitMinerWeb.services.matriz;

import br.edu.utfpr.cm.JGitMinerWeb.dao.GenericDao;
import br.edu.utfpr.cm.JGitMinerWeb.pojo.matriz.EntityMatrizRecord;
import br.edu.utfpr.cm.JGitMinerWeb.pojo.miner.EntityCommitUser;
import br.edu.utfpr.cm.JGitMinerWeb.pojo.miner.EntityRepository;
import br.edu.utfpr.cm.JGitMinerWeb.services.matriz.auxiliary.AuxUserFilePull;
import br.edu.utfpr.cm.JGitMinerWeb.services.matriz.auxiliary.AuxUserUserFile;
import br.edu.utfpr.cm.JGitMinerWeb.util.JsfUtil;
import br.edu.utfpr.cm.JGitMinerWeb.util.Util;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 *
 * @author douglas
 */
public class UserModifySameFileInPullRequestServices extends AbstractMatrizServices {

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

        String jpql = "SELECT DISTINCT NEW br.edu.utfpr.cm.JGitMinerWeb.services.matriz.auxiliary.AuxUserFilePull(uc, p, f.filename) "
                + "FROM "
                + "EntityPullRequest p JOIN p.repositoryCommits c JOIN c.commit cm JOIN cm.committer uc JOIN c.files f "
                + "WHERE "
                + "p.number >= :beginPull AND "
                + "p.number <= :endPull AND "
                + "p.repository = :repo AND "
                + "f.filename LIKE :prefixFile AND "
                + "f.filename LIKE :suffixFile "
                + "ORDER BY p.number ";

        // INICIO QUERY
        //query é feita por intervalos para evitar estouro de RAM e CPU
        int queryCount = 0;
        int offset = 1;
        final int limit = 10000;

        System.out.println(jpql);

        List<AuxUserFilePull> query = new ArrayList<AuxUserFilePull>();

        do {
            List result = dao.selectWithParams(jpql,
                    new String[]{"repo", "beginPull", "endPull", "prefixFile", "suffixFile"},
                    new Object[]{getRepository(), getBeginPullRequestNumber(), getEndPullRequestNumber(), getPrefixFile(), getSuffixFile()},
                    offset, limit);
            query.addAll(result);
            offset += limit;
            queryCount = result.size();
            System.out.println("query: " + query.size());
        } while (queryCount >= limit);

        // FIM QUERY


        List< EntityMatrizRecord> records = new ArrayList<EntityMatrizRecord>();

        List<AuxUserUserFile> controls = new ArrayList<AuxUserUserFile>(); // lista pata controle de repetição
        for (int i = 0; i < query.size(); i++) {
            AuxUserFilePull aufp = query.get(i);
            for (int j = i + 1; j < query.size(); j++) {
                AuxUserFilePull aufp2 = query.get(j);
                if (!aufp.getCommitUser().equals(aufp2.getCommitUser()) // se o user é diferente
                        && aufp.getPull().equals(aufp2.getPull()) // se o file é igual
                        && aufp.getFile().equals(aufp2.getFile())) { // e se o pull é igual
                    // então eles mexeram no mesmo arquivo no mesmo pull request
                    AuxUserUserFile control = new AuxUserUserFile(aufp.getCommitUser(), aufp2.getCommitUser(),  aufp.getFile());
                    // verifica se já foi gravado registo semelhante
                    if (controls.contains(control)) {
                        continue;
                    }
                    // salva o controle
                    controls.add(control);
                    // aqui sim ele cria o registro a ser salvo
                    EntityMatrizRecord rec = new EntityMatrizRecord(
                            EntityCommitUser.class, aufp.getCommitUser().getId(),
                            EntityCommitUser.class, aufp2.getCommitUser().getId(),
                            "", aufp.getFile());
                    records.add(rec);
                }
            }
            System.out.println(i + "/" + query.size());
        }
        setRecords(records);
    }

    @Override
    public String convertToCSV(Collection<EntityMatrizRecord> records) {
        StringBuilder sb = new StringBuilder("user;user2;file\n");
        for (EntityMatrizRecord record : records) {
            EntityCommitUser user = dao.findByID(record.getValueX(), EntityCommitUser.class);
            sb.append(user.getEmail()).append(JsfUtil.TOKEN_SEPARATOR);

            EntityCommitUser user2 = dao.findByID(record.getValueY(), EntityCommitUser.class);
            sb.append(user2.getEmail()).append(JsfUtil.TOKEN_SEPARATOR);

            sb.append(record.getValueZ()).append("\n");
        }
        return sb.toString();
    }
}
