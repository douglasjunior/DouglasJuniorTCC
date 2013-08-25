/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package br.edu.utfpr.cm.JGitMinerWeb.services.matriz;

import br.edu.utfpr.cm.JGitMinerWeb.dao.GenericDao;
import br.edu.utfpr.cm.JGitMinerWeb.pojo.miner.EntityRepository;
import br.edu.utfpr.cm.JGitMinerWeb.services.matriz.auxiliary.AuxUserUserFile;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 *
 * @author douglas
 */
public class UserModifySameFileInDateServices extends AbstractMatrizServices {

    public UserModifySameFileInDateServices(GenericDao dao) {
        super(dao);
    }

    public UserModifySameFileInDateServices(GenericDao dao, EntityRepository repository, Map params) {
        super(dao, repository, params);
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

        String jpql = "SELECT DISTINCT NEW " + AuxUserUserFile.class.getName() + "(rc.committer.login, rc.commit.committer.email, rc2.committer.login, rc2.commit.committer.email, f.filename) "
                + "FROM "
                + "EntityRepositoryCommit rc JOIN rc.files f,  "
                + "EntityRepositoryCommit rc2 JOIN rc2.files f2 "
                + "WHERE "
                + "rc.repository = :repo AND "
                + "rc2.repository = :repo AND "
                + "rc.commit.committer.dateCommitUser BETWEEN :beginDate AND :endDate AND "
                + "rc2.commit.committer.dateCommitUser BETWEEN :beginDate AND :endDate AND "
                + "f.filename LIKE :prefix AND "
                + "f.filename LIKE :suffix AND "
                + "f2.filename LIKE :prefix AND "
                + "f2.filename LIKE :suffix AND "
                + "f.filename = f2.filename AND "
                + "rc.commit.committer.email <> rc2.commit.committer.email ";

        System.out.println(jpql);

        String[] bdParams = new String[]{
            "repo",
            "prefix",
            "suffix",
            "beginDate",
            "endDate"
        };
        Object[] bdObjects = new Object[]{
            getRepository(),
            getPrefixFile(),
            getSuffixFile(),
            getBeginDate(),
            getEndDate()
        };

        List<AuxUserUserFile> result = dao.selectWithParams(jpql, bdParams, bdObjects);

        System.out.println("Result: " + result.size());

        result = removeDuplicade(result);

        System.out.println("Result distinct: " + result.size());

        addToEntityMatrizNodeList(result);
    }

    @Override
    public String getHeadCSV() {
        return "user;user2;file";
    }

    private List<AuxUserUserFile> removeDuplicade(List<AuxUserUserFile> result) {
        List<AuxUserUserFile> newResult = new ArrayList<>();
        for (AuxUserUserFile aux : result) {
            if (!newResult.contains(aux)) {
                newResult.add(aux);
            }
        }
        return newResult;
    }
}
