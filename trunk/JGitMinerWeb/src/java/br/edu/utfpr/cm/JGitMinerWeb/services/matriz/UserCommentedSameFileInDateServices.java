/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package br.edu.utfpr.cm.JGitMinerWeb.services.matriz;

import br.edu.utfpr.cm.JGitMinerWeb.dao.GenericDao;
import br.edu.utfpr.cm.JGitMinerWeb.pojo.miner.EntityRepository;
import br.edu.utfpr.cm.JGitMinerWeb.services.matriz.auxiliary.AuxUserUserFile;
import br.edu.utfpr.cm.JGitMinerWeb.services.matriz.auxiliary.AuxUserUserFileMilestone;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 *
 * @author douglas
 */
public class UserCommentedSameFileInDateServices extends AbstractMatrizServices {

    public UserCommentedSameFileInDateServices(GenericDao dao) {
        super(dao);
    }

    public UserCommentedSameFileInDateServices(GenericDao dao, EntityRepository repository, Map params) {
        super(dao, repository, params);
    }

    private String getPrefixFile() {
        return params.get("prefixFile") + "%";
    }

    private String getSuffixFile() {
        return "%" + params.get("suffixFile");
    }

    private Boolean isIncludeCommitComments() {
        return Boolean.parseBoolean(params.get("includeCommitComments") + "");
    }

    private Boolean isIncludeFileComments() {
        return Boolean.parseBoolean(params.get("includeFileComments") + "");
    }

    private Boolean isIncludeIssueComments() {
        return Boolean.parseBoolean(params.get("includeIssueComments") + "");
    }

    @Override
    public void run() {
        System.out.println(params);

        if (getRepository() == null) {
            throw new IllegalArgumentException("Parâmetro Repository não pode ser nulo.");
        }

        List<AuxUserUserFile> result;

        result = getByDate();

        System.out.println("Nodes: " + result.size());

        addToEntityMatrizNodeList(result);
    }

    private List<AuxUserUserFile> getByDate() {
        String jpql = "";
        List<AuxUserUserFile> result = new ArrayList<>();
        List<AuxUserUserFile> temp;

        String[] bdParams = new String[]{
            "repo",
            "beginDate",
            "endDate",
            "prefix",
            "suffix"
        };
        Object[] bdObjects = new Object[]{
            getRepository(),
            getBeginDate(),
            getEndDate(),
            getPrefixFile(),
            getSuffixFile()
        };

        if (isIncludeFileComments()) {
            jpql = "SELECT DISTINCT NEW " + AuxUserUserFile.class.getName() + "(cm.user.login, cm2.user.login, cm.pathCommitComment) "
                    + "FROM "
                    + "EntityCommitComment cm JOIN cm.repositoryCommit rc, "
                    + "EntityCommitComment cm2 JOIN cm2.repositoryCommit rc2 "
                    + "WHERE "
                    + "rc.repository = :repo AND "
                    + "rc2.repository = :repo AND "
                    + "rc.commit.committer.dateCommitUser BETWEEN :beginDate AND :endDate AND "
                    + "rc2.commit.committer.dateCommitUser BETWEEN :beginDate AND :endDate AND "
                    + "cm.pathCommitComment LIKE :prefix AND "
                    + "cm.pathCommitComment LIKE :suffix AND "
                    + "cm2.pathCommitComment LIKE :prefix AND "
                    + "cm2.pathCommitComment LIKE :suffix AND "
                    + "cm.pathCommitComment = cm2.pathCommitComment AND "
                    + "cm.user <> cm2.user ";

            System.out.println(jpql);

            temp = dao.selectWithParams(jpql,
                    bdParams,
                    bdObjects);

            System.out.println("Result temp: " + temp.size());

            addTempInResultAndRemoveDuplicate(temp, result);
        }

        if (isIncludeCommitComments()) {
            jpql = "SELECT DISTINCT NEW " + AuxUserUserFile.class.getName() + "(cm.user.login, cm2.user.login, f.filename) "
                    + "FROM "
                    + "EntityRepositoryCommit rc JOIN rc.comments cm JOIN rc.files f, "
                    + "EntityRepositoryCommit rc2 JOIN rc2.comments cm2 JOIN rc2.files f2 "
                    + "WHERE "
                    + "rc.repository = :repo AND "
                    + "rc2.repository = :repo AND "
                    + "rc.commit.committer.dateCommitUser BETWEEN :beginDate AND :endDate AND "
                    + "rc2.commit.committer.dateCommitUser BETWEEN :beginDate AND :endDate AND "
                    + "cm.pathCommitComment IS NULL AND "
                    + "cm2.pathCommitComment IS NULL AND "
                    + "f.filename LIKE :prefix AND "
                    + "f.filename LIKE :suffix AND "
                    + "f2.filename LIKE :prefix AND "
                    + "f2.filename LIKE :suffix AND "
                    + "f.filename = f2.filename AND "
                    + "cm.user <> cm2.user ";

            System.out.println(jpql);

            temp = dao.selectWithParams(jpql,
                    bdParams,
                    bdObjects);

            System.out.println("Result temp2: " + temp.size());

            addTempInResultAndRemoveDuplicate(temp, result);
        }

        if (isIncludeIssueComments()) {
            jpql = "SELECT DISTINCT NEW " + AuxUserUserFile.class.getName() + "(cm.user.login, cm2.user.login, f.filename) "
                    + "FROM "
                    + "EntityPullRequest p JOIN p.issue i JOIN i.comments cm JOIN p.repositoryCommits rc JOIN rc.files f, "
                    + "EntityPullRequest p2 JOIN p.issue i2 JOIN i2.comments cm2 JOIN p2.repositoryCommits rc2 JOIN rc2.files f2 "
                    + "WHERE "
                    + "p.repository = :repo AND "
                    + "p2.repository = :repo AND "
                    + "p.createdAt BETWEEN :beginDate AND :endDate AND "
                    + "p2.createdAt BETWEEN :beginDate AND :endDate AND "
                    + "f.filename = f2.filename AND "
                    + "cm.user <> cm2.user AND "
                    + "f.filename LIKE :prefix AND "
                    + "f.filename LIKE :suffix AND "
                    + "f2.filename LIKE :prefix AND "
                    + "f2.filename LIKE :suffix ";

            System.out.println(jpql);

            temp = dao.selectWithParams(jpql,
                    bdParams,
                    bdObjects);

            System.out.println("Result temp3: " + temp.size());

            addTempInResultAndRemoveDuplicate(temp, result);
        }

        System.out.println("Result: " + result.size());
        return result;
    }

    private void addTempInResultAndRemoveDuplicate(List<AuxUserUserFile> temp, List<AuxUserUserFile> result) {
        for (AuxUserUserFile aux : temp) {
            if (!result.contains(aux)) {
                result.add(aux);
            }
        }
    }

    @Override
    public String getHeadCSV() {
        return "user;user2;file";
    }
}
