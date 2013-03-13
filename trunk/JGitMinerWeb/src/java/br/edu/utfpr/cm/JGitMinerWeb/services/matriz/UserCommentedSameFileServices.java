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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author douglas
 */
public class UserCommentedSameFileServices extends AbstractMatrizServices {

    public UserCommentedSameFileServices(GenericDao dao) {
        super(dao);
    }

    public UserCommentedSameFileServices(GenericDao dao, EntityRepository repository, Map params) {
        super(dao, repository, params);
    }

    public Long getBeginPullRequestNumber() {
        String idPull = params.get("beginPull") + "";
        return Util.tratarStringParaLong(idPull);
    }

    public Long getEndPullRequestNumber() {
        String idPull = params.get("endPull") + "";
        return Util.tratarStringParaLong(idPull);
    }

    private String getPrefixFile() {
        return params.get("prefixFile") + "%";
    }

    private String getSuffixFile() {
        return "%" + params.get("suffixFile");
    }

    private int getMilestoneNumber() {
        String mileNumber = params.get("milestoneNumber") + "";
        return Util.tratarStringParaInt(mileNumber);
    }

    @Override
    public void run() {
        System.out.println(params);

        if (getRepository() == null) {
            throw new IllegalArgumentException("Parâmetro Repository não pode ser nulo.");
        }

        int mileNumber = getMilestoneNumber();

        String jpql = "SELECT DISTINCT NEW " + AuxUserFilePull.class.getName() + "(rc.committer.login, c.committer.email, p.number, f.filename) "
                + "FROM "
                + "EntityPullRequest p JOIN p.issue i JOIN i.milestone m JOIN p.repositoryCommits rc JOIN rc.files f JOIN rc.commit c "
                + "WHERE "
                + "p.repository = :repo AND "
                + (mileNumber > 0 ? "m.number = :milestoneNumber AND " : "")
                + (getBeginPullRequestNumber() > 0 ? "p.number >= :beginPull AND " : "")
                + (getEndPullRequestNumber() > 0 ? "p.number <= :endPull AND " : "")
                + "f.filename LIKE :prefixFile AND "
                + "f.filename LIKE :suffixFile";

        // INICIO QUERY
        //query é feita por intervalos para evitar estouro de RAM e CPU
        int queryCount = 0;
        int offset = 1;
        final int limit = 10000;

        System.out.println(jpql);

        List<AuxUserFilePull> query = new ArrayList<>();

        do {
            List result = dao.selectWithParams(jpql,
                    new String[]{
                        "repo",
                        getBeginPullRequestNumber() > 0 ? "beginPull" : "#none#",
                        getEndPullRequestNumber() > 0 ? "endPull" : "#none#",
                        "prefixFile",
                        "suffixFile",
                        mileNumber > 0 ? "milestoneNumber" : "#none#"
                    },
                    new Object[]{
                        getRepository(),
                        getBeginPullRequestNumber(),
                        getEndPullRequestNumber(),
                        getPrefixFile(),
                        getSuffixFile(),
                        mileNumber
                    },
                    offset, limit);
            query.addAll(result);
            offset += limit;
            queryCount = result.size();
            System.out.println("query: " + query.size());
        } while (queryCount >= limit);

        // FIM QUERY

        List<EntityMatrizNode> nodes = new ArrayList<>();
        Set<AuxUserUserPullFile> controls = new HashSet<>(); // lista pata controle de repetição

        for (int i = 0; i < query.size(); i++) {
            System.out.println(i + "/" + query.size());
            AuxUserFilePull aux = query.get(i);
            for (int j = i + 1; j < query.size(); j++) {
                AuxUserFilePull aux2 = query.get(j);
                if (aux.getPullNumber().equals(aux2.getPullNumber())) { // se o pull é igual 
                    if (!aux.getUserIdentity().equals(aux2.getUserIdentity())) {// se o user é diferente
                        if (aux.getFileName().equals(aux.getFileName())) { // se o file é igual  
                            // então eles mexeram no mesmo arquivo no mesmo pull request
                            AuxUserUserPullFile control = new AuxUserUserPullFile(aux.getUserIdentity(), aux2.getUserIdentity(), aux.getPullNumber(), aux.getFileName());
                            // verifica se já foi gravado registo semelhante, se não foi então grave
                            if (controls.add(control)) {
                                incrementNode(nodes, new EntityMatrizNode(control.getUserIdentity(), control.getUserIdentity2(), 1));
                            }
                        }
                    }
                }
            }
        }
        System.out.println("Nodes: " + nodes.size());
        setNodes(nodes);
    }

    @Override
    public String convertToCSV(Collection<EntityMatrizNode> nodes) {
        StringBuilder sb = new StringBuilder("user;user2;file\n");
        for (EntityMatrizNode node : nodes) {
            sb.append(node.getFrom()).append(JsfUtil.TOKEN_SEPARATOR);
            sb.append(node.getTo()).append(JsfUtil.TOKEN_SEPARATOR);
            sb.append(Util.tratarDoubleParaString(node.getWeight(), 0)).append("\n");
        }
        return sb.toString();
    }
}
