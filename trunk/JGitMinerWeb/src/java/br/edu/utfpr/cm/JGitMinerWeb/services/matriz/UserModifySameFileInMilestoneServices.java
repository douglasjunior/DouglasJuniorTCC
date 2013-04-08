/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package br.edu.utfpr.cm.JGitMinerWeb.services.matriz;

import br.edu.utfpr.cm.JGitMinerWeb.dao.GenericDao;
import br.edu.utfpr.cm.JGitMinerWeb.pojo.miner.EntityRepository;
import br.edu.utfpr.cm.JGitMinerWeb.services.matriz.auxiliary.AuxUserUserFile;
import br.edu.utfpr.cm.JGitMinerWeb.util.Util;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 *
 * @author douglas
 */
public class UserModifySameFileInMilestoneServices extends AbstractMatrizServices {

    public UserModifySameFileInMilestoneServices(GenericDao dao) {
        super(dao);
    }

    public UserModifySameFileInMilestoneServices(GenericDao dao, EntityRepository repository, Map params) {
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

    private List<String> getFilesName() {
        List<String> filesName = new ArrayList<>();
        for (String fileName : (params.get("filesName") + "").split("\n")) {
            fileName = fileName.trim();
            if (!fileName.isEmpty()) {
                filesName.add(fileName);
            }
        }
        return filesName;
    }

    private Integer getMilestoneNumber() {
        String mileNumber = params.get("milestoneNumber") + "";
        return Util.tratarStringParaInt(mileNumber);
    }

    @Override
    public void run() {
        System.out.println(params);

        if (getRepository() == null) {
            throw new IllegalArgumentException("Parâmetro Repository não pode ser nulo.");
        }

        List<AuxUserUserFile> result;

        if (getMilestoneNumber() > 0) {
            result = getByMilestoneNumber();
        } else {
            result = getByDate();
        }

        System.out.println("Nodes: " + result.size());

        addToEntityMatrizNodeList(result);
    }

    private List<AuxUserUserFile> getByMilestoneNumber() {
        String jpql = "SELECT DISTINCT NEW " + AuxUserUserFile.class.getName() + "(rc.committer.login, rc.commit.committer.email, rc2.committer.login, rc2.commit.committer.email, f.filename, p.issue.milestone) "
                + "FROM "
                + "EntityPullRequest p JOIN p.repositoryCommits rc JOIN rc.files f, "
                + "EntityPullRequest p2 JOIN p2.repositoryCommits rc2 JOIN rc2.files f2 "
                + "WHERE "
                + "p.repository = :repo AND "
                + "p2.repository = :repo AND "
                + "p.issue.milestone.number = :milestoneNumber AND "
                + "p2.issue.milestone.number = :milestoneNumber AND "
                + (!getFilesName().isEmpty() ? "f.filename IN :filesName AND " : "")
                + (!getFilesName().isEmpty() ? "f2.filename IN :filesName AND " : "")
                + "f.filename = f2.filename AND "
                + "rc.commit.committer.email <> rc2.commit.committer.email ";

        System.out.println(jpql);

        String[] bdParams = new String[]{
            "repo",
            !getFilesName().isEmpty() ? "filesName" : "#none#",
            "milestoneNumber"
        };
        Object[] bdObjects = new Object[]{
            getRepository(),
            getFilesName(),
            getMilestoneNumber()
        };

        System.out.println(jpql);

        List<AuxUserUserFile> result = dao.selectWithParams(jpql, bdParams, bdObjects);

        return removeDuplicade(result);
    }

    private List<AuxUserUserFile> getByDate() {
        final String select1 = "SELECT DISTINCT NEW " + AuxUserUserFile.class.getName() + "(rc.committer.login, rc.commit.committer.email, rc2.committer.login, rc2.commit.committer.email, f.filename, p.issue.milestone) ";

        final String select2 = "SELECT DISTINCT NEW " + AuxUserUserFile.class.getName() + "(rc.committer.login, rc.commit.committer.email, rc2.committer.login, rc2.commit.committer.email, f.filename) ";


        final String from = "FROM "
                + "EntityPullRequest p JOIN p.repositoryCommits rc JOIN rc.files f, "
                + "EntityPullRequest p2 JOIN p2.repositoryCommits rc2 JOIN rc2.files f2 "
                + "WHERE "
                + "p.repository = :repo AND "
                + "p2.repository = :repo AND ";

        final String where1 = "p.issue.milestone = p2.issue.milestone AND "
                + "rc.commit.committer.dateCommitUser BETWEEN :beginDate AND :endDate AND "
                + "rc2.commit.committer.dateCommitUser BETWEEN :beginDate AND :endDate AND "
                + (!getFilesName().isEmpty() ? "f.filename IN :filesName AND " : "")
                + (!getFilesName().isEmpty() ? "f2.filename IN :filesName AND " : "")
                + "f.filename = f2.filename AND "
                + "rc.commit.committer.email <> rc2.commit.committer.email ";

        final String where2 = "p.issue.milestone IS NULL AND "
                + "p2.issue.milestone IS NULL AND "
                + "rc.commit.committer.dateCommitUser BETWEEN :beginDate AND :endDate AND "
                + "rc2.commit.committer.dateCommitUser BETWEEN :beginDate AND :endDate AND "
                + (!getFilesName().isEmpty() ? "f.filename IN :filesName AND " : "")
                + (!getFilesName().isEmpty() ? "f2.filename IN :filesName AND " : "")
                + "f.filename = f2.filename AND "
                + "rc.commit.committer.email <> rc2.commit.committer.email ";

        String[] bdParams = new String[]{
            "repo",
            "beginDate",
            "endDate",
            !getFilesName().isEmpty() ? "filesName" : "#none#"
        };
        Object[] bdObjects = new Object[]{
            getRepository(),
            getBeginDate(),
            getEndDate(),
            getFilesName()
        };

        System.out.println(select1 + from + where1);

        List<AuxUserUserFile> result = dao.selectWithParams(select1 + from + where1, bdParams, bdObjects);

        System.out.println("result1: "+result.size());
        
        System.out.println(select2 + from + where2);

        result.addAll(dao.selectWithParams(select2 + from + where2, bdParams, bdObjects));

        System.out.println("result2: "+result.size());
        
        return removeDuplicade(result);
    }

    @Override
    public String getHeadCSV() {
        return "user;user2;file;milestone";
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
