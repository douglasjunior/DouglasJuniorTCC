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

    private static final String CALCULE_UPDATES
            = "SELECT COUNT(rc) FROM EntityRepositoryCommit rc "
            + "WHERE "
            + "rc.repository = :repo AND "
            + "rc.commit.committer.dateCommitUser BETWEEN :beginDate AND :endDate AND "
            + "EXISTS (SELECT f FROM EntityCommitFile f WHERE f.repositoryCommit = rc AND f.filename = :fileName) AND "
            + "EXISTS (SELECT f2 FROM EntityCommitFile f2 WHERE f2.repositoryCommit = rc AND f2.filename = :fileName2)";

    private static final String[] CALCULE_UPDATES_PARAMS = new String[]{
        "repo", "fileName", "fileName2", "beginDate", "endDate"
    };

    private static final String SELECT_PULL_REQUEST_BY_DATE = "SELECT count(distinct(pr.id)) "
            + " FROM gitpullrequest pr "
            + " WHERE pr.repository_id = ? "
            + "   AND pr.createdat BETWEEN ? AND ? ";

    private static final String SELECT_PULL_REQUEST_AND_ISSUE_BY_DATE = "SELECT count(distinct(pr.id)) "
            + " FROM gitpullrequest pr, gitissue iss "
            + " WHERE pr.repository_id = ? "
            + "   AND iss.id = pr.issue_id "
            + "   AND iss.commentscount > 1 "
            + "   AND pr.createdat BETWEEN ? AND ? ";

    private static final String SELECT_PULL_REQUEST_BY_NUMBER = "SELECT count(1) "
            + " FROM gitpullrequest pr "
            + " WHERE pr.repository_id = ? "
            + "   AND pr.number BETWEEN ? AND ? ";

    private static final String MERGED_PULL_REQUEST_ONLY = " AND pr.mergedat IS NOT NULL ";

    private static final String EXISTS_FILE1_IN_PULL_REQUEST = " AND EXISTS "
            + "(SELECT 1 "
            + "FROM gitpullrequest_gitrepositorycommit r, "
            + "     gitcommitfile f "
            + "WHERE r.entitypullrequest_id = pr.id "
            + "  AND f.repositorycommit_id = r.repositorycommits_id "
            + "  AND f.filename = ?) ";

    private static final String EXISTS_FILE2_IN_PULL_REQUEST = " AND EXISTS "
            + "(SELECT 1  "
            + "FROM gitpullrequest_gitrepositorycommit r2, "
            + "     gitcommitfile f2 "
            + "WHERE r2.entitypullrequest_id = pr.id "
            + "  AND f2.repositorycommit_id = r2.repositorycommits_id "
            + "  AND f2.filename = ?) ";

    private static final String COUNT_COMMENTS_OF_FILE_PAIR_BY_DATE = "SELECT SUM(i.commentscount) "
            + "FROM gitpullrequest pr "
            + "  JOIN gitissue i ON i.id = pr.issue_id "
            + "WHERE pr.repository_id = ? "
            + "  AND pr.createdat BETWEEN ? AND ? ";

    private static final String COUNT_COMMENTS_OF_FILE_PAIR_BY_PULL_REQUEST_NUMBER = "SELECT SUM(i.commentscount) "
            + "FROM gitpullrequest pr "
            + "  JOIN gitissue i ON i.id = pr.issue_id "
            + "WHERE pr.repository_id = ? "
            + "  AND pr.number BETWEEN ? AND ? ";

    private static final String SELECT_SUM_OF_CHANGES_IN_FILE_BY_PULL_REQUEST_NUMBER = "SELECT SUM(ff.changes) "
            + "FROM "
            + "EntityPullRequest pp JOIN pp.repositoryCommits rc JOIN rc.files ff "
            + "WHERE "
            + "pp.repository = :repo AND "
            + "pp.number BETWEEN :beginNumber AND :endNumber AND "
            + "ff.filename = :fileName AND pp.mergedAt IS NOT NULL AND "
            + "EXISTS (SELECT p2 FROM EntityPullRequest p2 JOIN p2.repositoryCommits r2 JOIN r2.files f2 WHERE p2 = pp AND f2.filename = :fileName2) ";

    private static final String[] SELECT_SUM_OF_CHANGES_IN_FILE_BY_PULL_REQUEST_NUMBER_PARAMS = new String[]{
        "repo",
        "fileName",
        "fileName2",
        "beginNumber",
        "endNumber"
    };

    private static final String SELECT_SUM_OF_CHANGES_IN_FILE_BY_DATE = "SELECT SUM(ff.changes) "
            + "FROM "
            + "EntityPullRequest pp JOIN pp.repositoryCommits rc JOIN rc.files ff "
            + "WHERE "
            + "pp.repository = :repo AND "
            + "pp.createdAt BETWEEN :beginDate AND :endDate AND "
            + "ff.filename = :fileName AND pp.mergedAt IS NOT NULL AND "
            + "EXISTS (SELECT p2 FROM EntityPullRequest p2 JOIN p2.repositoryCommits r2 JOIN r2.files f2 WHERE p2 = pp AND f2.filename = :fileName2) ";

    private static final String[] SELECT_SUM_OF_CHANGES_IN_FILE_BY_DATE_PARAMS = new String[]{
        "repo",
        "fileName",
        "fileName2",
        "beginDate",
        "endDate"
    };

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

    public long calculeNumberOfPullRequest(EntityRepository repository, 
            String file, String file2, Date beginDate, Date endDate, boolean onlyMergeds) {
        List<Object> selectParams = new ArrayList<>();

        StringBuilder sql = new StringBuilder();
        sql.append(SELECT_PULL_REQUEST_BY_DATE);

        if (onlyMergeds) {
            sql.append(MERGED_PULL_REQUEST_ONLY);
        }

        selectParams.add(repository.getId());
        selectParams.add(beginDate);
        selectParams.add(endDate);

        if (file != null) {
            sql.append(EXISTS_FILE1_IN_PULL_REQUEST);
            selectParams.add(file);
        }

        if (file2 != null) {
            sql.append(EXISTS_FILE2_IN_PULL_REQUEST);
            selectParams.add(file2);
        }

        Long count = (Long) dao.selectNativeOneWithParams(sql.toString(), selectParams.toArray());

        return count != null ? count : 0l;
    }

    public long calculeNumberOfPullRequest(EntityRepository repository, 
            String file, String file2, Long beginNumber, Long endNumber, boolean onlyMergeds) {
        List<Object> selectParams = new ArrayList<>();

        StringBuilder sql = new StringBuilder();
        sql.append(SELECT_PULL_REQUEST_BY_NUMBER);

        if (onlyMergeds) {
            sql.append(MERGED_PULL_REQUEST_ONLY);
        }

        selectParams.add(repository.getId());
        selectParams.add(beginNumber);
        selectParams.add(endNumber);

        if (file != null) {
            sql.append(EXISTS_FILE1_IN_PULL_REQUEST);
            selectParams.add(file);
        }

        if (file2 != null) {
            sql.append(EXISTS_FILE2_IN_PULL_REQUEST);
            selectParams.add(file2);
        }

        Long count = dao.selectNativeOneWithParams(sql.toString(), selectParams.toArray());

        return count != null ? count : 0l;
    }

    public long calculeComments(EntityRepository repository, 
            String file, String file2, Date beginDate, Date endDate, boolean onlyMergeds) {
        List<Object> selectParams = new ArrayList<>();

        StringBuilder sql = new StringBuilder();
        sql.append(COUNT_COMMENTS_OF_FILE_PAIR_BY_DATE);

        if (onlyMergeds) {
            sql.append(MERGED_PULL_REQUEST_ONLY);
        }

        selectParams.add(repository.getId());
        selectParams.add(beginDate);
        selectParams.add(endDate);

        if (file != null) {
            sql.append(EXISTS_FILE1_IN_PULL_REQUEST);
            selectParams.add(file);
        }

        if (file2 != null) {
            sql.append(EXISTS_FILE2_IN_PULL_REQUEST);
            selectParams.add(file2);
        }

        Long count = dao.selectNativeOneWithParams(sql.toString(), selectParams.toArray());

        return count != null ? count : 0l;
    }

    public long calculeComments(EntityRepository repository, 
            String file, String file2, Long beginNumber, Long endNumber, boolean onlyMergeds) {
        List<Object> selectParams = new ArrayList<>();

        StringBuilder sql = new StringBuilder();
        sql.append(COUNT_COMMENTS_OF_FILE_PAIR_BY_PULL_REQUEST_NUMBER);

        if (onlyMergeds) {
            sql.append(MERGED_PULL_REQUEST_ONLY);
        }

        selectParams.add(repository.getId());
        selectParams.add(beginNumber);
        selectParams.add(endNumber);

        if (file != null) {
            sql.append(EXISTS_FILE1_IN_PULL_REQUEST);
            selectParams.add(file);
        }

        if (file2 != null) {
            sql.append(EXISTS_FILE2_IN_PULL_REQUEST);
            selectParams.add(file2);
        }

        Long count = dao.selectNativeOneWithParams(sql.toString(), selectParams.toArray());

        return count != null ? count : 0l;
    }

    public Long calculeCodeChurn(EntityRepository repository, 
            String fileName, String fileName2, Long beginNumber, Long endNumber) {

        Object[] bdObjects = new Object[]{
            repository,
            fileName,
            fileName2,
            beginNumber,
            endNumber
        };

        Long sum = dao.selectOneWithParams(
                SELECT_SUM_OF_CHANGES_IN_FILE_BY_PULL_REQUEST_NUMBER,
                SELECT_SUM_OF_CHANGES_IN_FILE_BY_PULL_REQUEST_NUMBER_PARAMS,
                bdObjects);

        return sum != null ? sum : 0l;
    }

    public Long calculeCodeChurn(EntityRepository repository, 
            String fileName, String fileName2, Date beginDate, Date endDate) {

        Object[] bdObjects = new Object[]{
            repository,
            fileName,
            fileName2,
            beginDate,
            endDate
        };

        Long sum = dao.selectOneWithParams(
                SELECT_SUM_OF_CHANGES_IN_FILE_BY_DATE,
                SELECT_SUM_OF_CHANGES_IN_FILE_BY_DATE_PARAMS, bdObjects);

        return sum != null ? sum : 0l;
    }
}
