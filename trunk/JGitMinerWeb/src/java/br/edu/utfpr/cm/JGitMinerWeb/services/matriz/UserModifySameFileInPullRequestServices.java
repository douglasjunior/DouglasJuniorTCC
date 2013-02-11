/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package br.edu.utfpr.cm.JGitMinerWeb.services.matriz;

import br.edu.utfpr.cm.JGitMinerWeb.dao.GenericDao;
import br.edu.utfpr.cm.JGitMinerWeb.pojo.matriz.EntityMatrizRecord;
import br.edu.utfpr.cm.JGitMinerWeb.pojo.miner.EntityRepository;
import br.edu.utfpr.cm.JGitMinerWeb.pojo.miner.EntityUser;
import br.edu.utfpr.cm.JGitMinerWeb.services.matriz.auxiliary.AuxUserFilePull;
import br.edu.utfpr.cm.JGitMinerWeb.services.matriz.auxiliary.AuxUserUserPullFile;
import br.edu.utfpr.cm.JGitMinerWeb.util.JsfUtil;
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
        return Long.parseLong(params.get("beginPull") + "");
    }

    public Long getEndPullRequestNumber() {
        return Long.parseLong(params.get("endPull") + "");
    }

    @Override
    public void run() {
        System.out.println(params);

        if (repository == null) {
            throw new IllegalArgumentException("Parâmetro Repository não pode ser nulo.");
        }

        List<EntityMatrizRecord> records = new ArrayList<EntityMatrizRecord>();

        String jpql = "SELECT NEW br.edu.utfpr.cm.JGitMinerWeb.services.matriz.auxiliary.AuxUserFilePull(u, p, f.filename) "
                + "FROM "
                + "EntityPullRequest p JOIN p.repositoryCommits c JOIN c.committer u JOIN c.files f "
                + "ORDER BY p.number";

        System.out.println(jpql);

        //   List<EntityMatrizRecord> records = dao.selectWithParams(jpql, new String[]{"repo", "beginPull", "endPull"}, new Object[]{getRepository(), getBeginPullRequestNumber(), getEndPullRequestNumber()});
        List<AuxUserFilePull> query = dao.selectWithParams(jpql, new String[]{}, new Object[]{});

        System.out.println("query: " + query.size());

        List<AuxUserUserPullFile> controls = new ArrayList<AuxUserUserPullFile>(); // lista pata controle de repetição
        for (AuxUserFilePull aufp : query) {
            for (AuxUserFilePull aufp2 : query) {
                if (!aufp.getUser().equals(aufp2.getUser()) // se o user é diferente
                        && aufp.getPull().equals(aufp2.getPull()) // se o file é igual
                        && aufp.getFile().equals(aufp2.getFile())) { // e se o pull é igual
                    // então eles mexeram no mesmo arquivo no mesmo pull request
                    AuxUserUserPullFile control = new AuxUserUserPullFile(aufp.getUser(), aufp2.getUser(), aufp.getPull(), aufp.getFile());
                    // verifica se já foi gravado registo semelhante
                    if (!controls.contains(control)) {
                        // salva o controle
                        controls.add(control);
                        // aqui sim ele cria o registro a ser salvo
                        records.add(new EntityMatrizRecord(EntityUser.class, aufp.getUser().getId(),
                                EntityUser.class, aufp2.getUser().getId(), aufp.getPull().getNumber(), aufp.getFile()));
                    }
                }
            }
        }

        setRecords(records);

        System.out.println("records: " + records.size());

        for (EntityMatrizRecord rec : records) {
            System.out.println(rec);
        }
    }

    @Override
    public String convertToCSV(List<EntityMatrizRecord> records) {
        StringBuilder sb = new StringBuilder("user;user2;file\n");
        for (EntityMatrizRecord record : records) {
            EntityUser user = dao.findByID(record.getValueX(), EntityUser.class);
            EntityUser user2 = dao.findByID(record.getValueY(), EntityUser.class);
            sb.append(user.getLogin()).append(JsfUtil.TOKEN_SEPARATOR).append(user2.getLogin()).append(JsfUtil.TOKEN_SEPARATOR).append(record.getValueZ()).append("\n");
        }
        return sb.toString();
    }
}
