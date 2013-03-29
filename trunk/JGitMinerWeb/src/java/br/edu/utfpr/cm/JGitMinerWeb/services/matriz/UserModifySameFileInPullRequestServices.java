/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package br.edu.utfpr.cm.JGitMinerWeb.services.matriz;

import br.edu.utfpr.cm.JGitMinerWeb.dao.GenericDao;
import br.edu.utfpr.cm.JGitMinerWeb.pojo.miner.EntityRepository;
import br.edu.utfpr.cm.JGitMinerWeb.services.matriz.auxiliary.AuxUserFilePull;
import br.edu.utfpr.cm.JGitMinerWeb.services.matriz.auxiliary.AuxUserUserPullFile;
import br.edu.utfpr.cm.JGitMinerWeb.services.matriz.nodes.NodeGeneric;
import br.edu.utfpr.cm.JGitMinerWeb.util.Util;
import java.util.ArrayList;
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

        List<AuxUserFilePull> query;

        if (getMilestoneNumber() != 0) {
            query = getByMilestone();
        } else if (getBeginPullRequestNumber() != 0
                || getEndPullRequestNumber() != 0) {
            query = getByPullRequest();
        } else {
            throw new IllegalArgumentException("Informe o número do Milestone ou um Intervalo de Pull Requests.");
        }

        List<NodeGeneric> nodes = new ArrayList<>();
        List<AuxUserUserPullFile> controls = new ArrayList<>(); // lista pata controle de repetição

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
                            if (!controls.contains(control)) {
                                controls.add(control);
                                incrementNode(nodes, new NodeGeneric(control.getUserIdentity(), control.getUserIdentity2()));
                            }
                        }
                    }
                }
            }
        }
        System.out.println("Nodes: " + nodes.size());
        addToEntityMatrizNodeList(nodes);
    }

    @Override
    public String getHeadCSV() {
        return "user;user2;file";
    }

    private List<AuxUserFilePull> getByMilestone() {
        int mileNumber = getMilestoneNumber();

        String jpql = "SELECT DISTINCT NEW " + AuxUserFilePull.class.getName() + "(rc.committer.login, rc.commit.committer.email, p.number, f.filename) "
                + "FROM "
                + "EntityPullRequest p JOIN p.repositoryCommits rc JOIN rc.files f "
                + "WHERE "
                + "p.repository = :repo AND "
                + "p.issue.milestone.number = :milestoneNumber AND "
                + "f.filename LIKE :prefixFile AND "
                + "f.filename LIKE :suffixFile";

        return createQueryResult(jpql,
                new String[]{
                    "repo",
                    "prefixFile",
                    "suffixFile",
                    "milestoneNumber"
                },
                new Object[]{
                    getRepository(),
                    getPrefixFile(),
                    getSuffixFile(),
                    mileNumber
                });
    }

    private List<AuxUserFilePull> getByPullRequest() {
        String jpql = "SELECT DISTINCT NEW " + AuxUserFilePull.class.getName() + "(rc.committer.login, c.committer.email, p.number, f.filename) "
                + "FROM "
                + "EntityPullRequest p JOIN p.repositoryCommits rc JOIN rc.files f JOIN rc.commit c "
                + "WHERE "
                + "p.repository = :repo AND "
                + "p.number >= :beginPull AND "
                + "p.number <= :endPull AND "
                + "f.filename LIKE :prefixFile AND "
                + "f.filename LIKE :suffixFile";

        return createQueryResult(jpql,
                new String[]{
                    "repo",
                    "beginPull",
                    "endPull",
                    "prefixFile",
                    "suffixFile"
                },
                new Object[]{
                    getRepository(),
                    getBeginPullRequestNumber(),
                    getEndPullRequestNumber(),
                    getPrefixFile(),
                    getSuffixFile()
                });
    }

    private List createQueryResult(String jpql, String[] paras, Object[] objects) {
        //query é feita por intervalos para evitar estouro de RAM e CPU
        List<AuxUserFilePull> query = new ArrayList<>();
        int queryCount = 0;
        int offset = 1;
        final int limit = 10000;

        System.out.println(jpql);

        do {
            List result = dao.selectWithParams(jpql, paras, objects, offset, limit);
            query.addAll(result);
            offset += limit;
            queryCount = result.size();
            System.out.println("query: " + query.size());
        } while (queryCount >= limit);

        return query;
    }
}
