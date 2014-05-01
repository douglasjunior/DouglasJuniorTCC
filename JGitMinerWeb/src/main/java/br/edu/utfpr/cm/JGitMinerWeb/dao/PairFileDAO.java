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
    
    private static final String SELECT_PULL_REQUEST_BY_DATE = " SELECT count(1) "
            + " FROM gitpullrequest pul "
            + " where pul.repository_id = ? "
            + "   and pul.createdat between ? and ? ";
    
    private static final String SELECT_PULL_REQUEST_BY_NUMBER = " SELECT count(1) "
            + " FROM gitpullrequest pul "
            + " where pul.repository_id = ? "
            + "   and pul.number between ? and ? ";
    
    private static final String MERGED_PULL_REQUEST_ONLY = "  and pul.mergedat is not null ";
    
    private static final String EXISTS_FILE1_IN_PULL_REQUEST = "  and exists  "
            + "  ( select 1 "
            + "  from gitpullrequest_gitrepositorycommit r, "
            + "       gitcommitfile f  "
            + "  where r.entitypullrequest_id = pul.id "
            + "    and f.repositorycommit_id = r.repositorycommits_id "
            + "    and f.filename = ? ) ";
    
    private static final String EXISTS_FILE2_IN_PULL_REQUEST = "  and exists  "
            + "  ( select 1  "
            + "  from gitpullrequest_gitrepositorycommit r2, "
            + "       gitcommitfile f2  "
            + "  where r2.entitypullrequest_id = pul.id "
            + "    and f2.repositorycommit_id = r2.repositorycommits_id "
            + "    and f2.filename = ? ) ";

    private final GenericDao dao;

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
    
    public long calculeNumberOfPullRequest(EntityRepository repository, String file, String file2, Date beginDate, Date endDate, boolean onlyMergeds) {
        List selectParams = new ArrayList();

        String jpql = SELECT_PULL_REQUEST_BY_DATE;

        if (onlyMergeds) {
            jpql += MERGED_PULL_REQUEST_ONLY;
        }

        selectParams.add(repository.getId());
        selectParams.add(beginDate);
        selectParams.add(endDate);

        if (file != null) {
            jpql += EXISTS_FILE1_IN_PULL_REQUEST;
            selectParams.add(file);
        }

        if (file2 != null) {
            jpql += EXISTS_FILE2_IN_PULL_REQUEST;
            selectParams.add(file2);
        }

        Long count = (Long) dao.selectNativeOneWithParams(jpql, selectParams.toArray());

        return count != null ? count : 0l;
    }
    
    public long calculeNumberOfPullRequest(EntityRepository repository, String file, String file2, Long beginNumber, Long endNumber, boolean onlyMergeds) {
        List selectParams = new ArrayList();

        String jpql = SELECT_PULL_REQUEST_BY_NUMBER;

        if (onlyMergeds) {
            jpql += MERGED_PULL_REQUEST_ONLY;
        }

        selectParams.add(repository.getId());
        selectParams.add(beginNumber);
        selectParams.add(endNumber);

        if (file != null) {
            jpql += EXISTS_FILE1_IN_PULL_REQUEST;
            selectParams.add(file);
        }

        if (file2 != null) {
            jpql += EXISTS_FILE2_IN_PULL_REQUEST;
            selectParams.add(file2);
        }

        Long count = (Long) dao.selectNativeOneWithParams(jpql, selectParams.toArray());

        return count != null ? count : 0l;
    }
}
