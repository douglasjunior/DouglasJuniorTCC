package br.edu.utfpr.cm.JGitMinerWeb.dao;

import br.edu.utfpr.cm.JGitMinerWeb.model.miner.EntityCommit;
import br.edu.utfpr.cm.JGitMinerWeb.model.miner.EntityCommitFile;
import br.edu.utfpr.cm.JGitMinerWeb.model.miner.EntityCommitUser;
import br.edu.utfpr.cm.JGitMinerWeb.model.miner.EntityRepository;
import br.edu.utfpr.cm.JGitMinerWeb.model.miner.EntityRepositoryCommit;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 *
 * @author Rodrigo T. Kuroda
 */
public class PairFileDAO {

    private static final String CALCULE_SUM_UPDATES_OF_TWO_FILE
            = "SELECT count(1) FROM " + EntityRepositoryCommit.class.getSimpleName() + " rc "
            + "JOIN " + EntityCommit.class.getSimpleName() + " c ON rc.commit = c "
            + "JOIN " + EntityCommitUser.class.getSimpleName() + " u ON c.committer = u "
            + "JOIN " + EntityCommitFile.class.getSimpleName() + " f ON f.repositoryCommit = rc "
            + "WHERE "
            + "rc.repository = :repo AND "
            + "u.dateCommitUser BETWEEN :beginDate AND :endDate AND "
            + "(f.filename = :fileName OR f.filename = :fileName2)";

    private static final String[] CALCULE_SUM_UPDATES_OF_TWO_FILE_PARAMS = new String[]{
        "repo", "fileName", "fileName2", "beginDate", "endDate"
    };
    
    private static final String CALCULE_UPDATES = 
            "SELECT COUNT(rc) FROM EntityRepositoryCommit rc "
            + "WHERE "
            + "rc.repository = :repo AND "
            + "rc.commit.committer.dateCommitUser BETWEEN :beginDate AND :endDate AND "
            + "EXISTS (SELECT f FROM EntityCommitFile f WHERE f.repositoryCommit = rc AND f.filename = :fileName) AND "
            + "EXISTS (SELECT f2 FROM EntityCommitFile f2 WHERE f2.repositoryCommit = rc AND f2.filename = :fileName2)";

    private static final String[] CALCULE_UPDATES_PARAMS = new String[]{
        "repo", "fileName", "fileName2", "beginDate", "endDate"
    };

    private GenericDao dao;

    public PairFileDAO(GenericDao dao) {
        this.dao = dao;
    }

    public Long calculeSumUpdatesOfTwoFile(EntityRepository repository, 
            String fileName, String fileName2, 
            Date beginDate, Date endDate) {
        if (beginDate == null || endDate == null) {
            return 0l;
        }

        Object[] queryParams = new Object[]{
            repository,
            fileName,
            fileName2,
            beginDate,
            endDate
        };

        return dao.selectOneWithParams(CALCULE_SUM_UPDATES_OF_TWO_FILE, CALCULE_SUM_UPDATES_OF_TWO_FILE_PARAMS, queryParams);
    }

    public Long calculeUpdates(EntityRepository repository, 
            String fileName, String fileName2, 
            Date beginDate, Date endDate) {
        if (beginDate == null || endDate == null) {
            return 0l;
        }
        
        Object[] queryParams = new Object[]{
            repository,
            fileName,
            fileName2,
            beginDate,
            endDate
        };

        return dao.selectOneWithParams(CALCULE_UPDATES, CALCULE_UPDATES_PARAMS, queryParams);
    }
    
    public long calculeNumberOfPullRequest(EntityRepository repository, String file, String file2, Date beginDate, Date endDate) {
        List selectParams = new ArrayList(); 
        
        String jpql = " SELECT count(pul.*) "
                + " FROM gitpullrequest pul "
                + " where pul.repository_id = ? "
                + "   and pul.createdat between ? and ? ";
        
        selectParams.add(repository.getId());
        selectParams.add(beginDate);
        selectParams.add(endDate);
        
        if (file != null) {
            jpql += "  and exists  "
                    + "  ( select f.* "
                    + "  from gitpullrequest_gitrepositorycommit r, "
                    + "       gitcommitfile f  "
                    + "  where r.entitypullrequest_id = pul.id "
                    + "    and f.repositorycommit_id = r.repositorycommits_id "
                    + "    and f.filename = ? ) ";
            selectParams.add(file);
        }

        if (file2 != null) {
            jpql += "  and exists  "
                    + "  ( select f2.*  "
                    + "  from gitpullrequest_gitrepositorycommit r2, "
                    + "       gitcommitfile f2  "
                    + "  where r2.entitypullrequest_id = pul.id "
                    + "    and f2.repositorycommit_id = r2.repositorycommits_id "
                    + "    and f2.filename = ? ) ";
            selectParams.add(file2);
        }

         Long count = dao.selectNativeOneWithParams(jpql, selectParams.toArray());

        return count != null ? count : 0l;
    }
}
