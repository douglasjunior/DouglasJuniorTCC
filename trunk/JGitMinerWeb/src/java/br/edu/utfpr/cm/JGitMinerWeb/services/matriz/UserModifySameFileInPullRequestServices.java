/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package br.edu.utfpr.cm.JGitMinerWeb.services.matriz;

import br.edu.utfpr.cm.JGitMinerWeb.dao.GenericDao;
import br.edu.utfpr.cm.JGitMinerWeb.pojo.matriz.EntityMatrizNode;
import br.edu.utfpr.cm.JGitMinerWeb.pojo.miner.EntityRepository;
import br.edu.utfpr.cm.JGitMinerWeb.services.matriz.auxiliary.AuxUserFilePull;
import br.edu.utfpr.cm.JGitMinerWeb.services.matriz.auxiliary.AuxUserUserPullFile;
import br.edu.utfpr.cm.JGitMinerWeb.util.JsfUtil;
import br.edu.utfpr.cm.JGitMinerWeb.util.Util;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import org.eclipse.egit.github.core.service.CollaboratorService;

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
        } while (queryCount >= limit);

        System.out.println("query: " + query.size());
        // FIM QUERY


        List< EntityMatrizNode> nodes = new ArrayList<EntityMatrizNode>();

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
                        incrementNode(nodes, new EntityMatrizNode(control.getCommitUser().getEmail(), control.getCommitUser2().getEmail()));
                    }
                }
            }
        }
        setNodes(nodes);
    }

    private void incrementNode(List<EntityMatrizNode> nodes, EntityMatrizNode node) {
        int i = nodes.indexOf(node);
        if (i >= 0) {
            nodes.get(i).incWeight();
        } else {
            nodes.add(node);
        }
    }

    @Override
    public String convertToCSV(Collection<EntityMatrizNode> nodes) {
        StringBuilder sb = new StringBuilder("user;user2;file\n");
        for (EntityMatrizNode node : nodes) {
            sb.append(node.getFrom()).append(JsfUtil.TOKEN_SEPARATOR);
            sb.append(node.getTo()).append(JsfUtil.TOKEN_SEPARATOR);
            sb.append(node.getWeight()).append("\n");
        }
        return sb.toString();
    }
}
