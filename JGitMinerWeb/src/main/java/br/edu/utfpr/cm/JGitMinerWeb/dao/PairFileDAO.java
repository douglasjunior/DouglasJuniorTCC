package br.edu.utfpr.cm.JGitMinerWeb.dao;

import br.edu.utfpr.cm.JGitMinerWeb.model.miner.EntityCommit;
import br.edu.utfpr.cm.JGitMinerWeb.model.miner.EntityCommitFile;
import br.edu.utfpr.cm.JGitMinerWeb.model.miner.EntityCommitUser;
import br.edu.utfpr.cm.JGitMinerWeb.model.miner.EntityRepository;
import br.edu.utfpr.cm.JGitMinerWeb.model.miner.EntityRepositoryCommit;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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

    private static final String SELECT_PULL_REQUEST_BY_DATE = "SELECT count(distinct(pul.id)) "
            + " FROM gitpullrequest pul "
            + " WHERE pul.repository_id = ? "
            + "   AND pul.createdat BETWEEN ? AND ? ";

    private static final String SELECT_PULL_REQUEST_AND_ISSUE_BY_DATE = "SELECT count(distinct(pul.id)) "
            + " FROM gitpullrequest pul, gitissue iss "
            + " WHERE pul.repository_id = ? "
            + "   AND iss.id = pul.issue_id "
            + "   AND iss.commentscount > 1 "
            + "   AND pul.createdat BETWEEN ? AND ? ";

    private static final String SELECT_PULL_REQUEST_BY_NUMBER = "SELECT count(1) "
            + " FROM gitpullrequest pul "
            + " WHERE pul.repository_id = ? "
            + "   AND pul.number BETWEEN ? AND ? ";

    private static final String FILTER_BY_PULL_REQUEST_CREATION_DATE
            = " AND pul.createdat BETWEEN ? AND ? ";

    private static final String FILTER_BY_BEFORE_PULL_REQUEST_CREATION_DATE
            = " AND pul.createdat <= ? ";

    private static final String FILTER_BY_AFTER_PULL_REQUEST_CREATION_DATE
            = " AND pul.createdat >= ? ";

    private static final String MERGED_PULL_REQUEST_ONLY
            = " AND pul.mergedat IS NOT NULL ";

    private static final String EXISTS_FILE1_IN_PULL_REQUEST = " AND EXISTS "
            + "(SELECT 1 "
            + "FROM gitpullrequest_gitrepositorycommit r, "
            + "     gitcommitfile f "
            + "WHERE r.entitypullrequest_id = pul.id "
            + "  AND f.repositorycommit_id = r.repositorycommits_id "
            + "  AND f.filename = ?) ";

    private static final String EXISTS_FILE2_IN_PULL_REQUEST = " AND EXISTS "
            + "(SELECT 1  "
            + "FROM gitpullrequest_gitrepositorycommit r2, "
            + "     gitcommitfile f2 "
            + "WHERE r2.entitypullrequest_id = pul.id "
            + "  AND f2.repositorycommit_id = r2.repositorycommits_id "
            + "  AND f2.filename = ?) ";

    private static final String COUNT_COMMENTS_OF_FILE_PAIR_BY_DATE = "SELECT SUM(i.commentscount) "
            + "FROM gitpullrequest pul "
            + "  JOIN gitissue i ON i.id = pul.issue_id "
            + "WHERE pul.repository_id = ? "
            + "  AND pul.createdat BETWEEN ? AND ? ";

    private static final String COUNT_COMMENTS_OF_FILE_PAIR_BY_PULL_REQUEST_NUMBER = "SELECT SUM(i.commentscount) "
            + "FROM gitpullrequest pul "
            + "  JOIN gitissue i ON i.id = pul.issue_id "
            + "WHERE pul.repository_id = ? "
            + "  AND pul.number BETWEEN ? AND ? ";

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

    private static final String SELECT_SUM_OF_ADD_DEL_CHANGES_IN_FILE_BY_DATE
            = "SELECT SUM(ff.additions), SUM(ff.deletions), SUM(ff.changes) "
            + "FROM "
            + "EntityPullRequest pp JOIN pp.repositoryCommits rc JOIN rc.files ff "
            + "WHERE "
            + "pp.repository = :repo AND "
            + "pp.createdAt BETWEEN :beginDate AND :endDate AND "
            + "ff.filename = :fileName AND pp.mergedAt IS NOT NULL AND "
            + "EXISTS (SELECT p2 FROM EntityPullRequest p2 JOIN p2.repositoryCommits r2 JOIN r2.files f2 WHERE p2 = pp AND f2.filename = :fileName2) ";

    private static final String[] SELECT_SUM_OF_ADD_DEL_CHANGES_IN_FILE_BY_DATE_PARAMS = new String[]{
        "repo",
        "fileName",
        "fileName2",
        "beginDate",
        "endDate"
    };


    private static final String SELECT_PAIR_FILE_COMMITTERS
            = "SELECT u.name, u.email FROM " // u ou u2 retornam a mesma coisa
            + "  gitpullrequest pul, gitissue i,"
            + "  gitcommitfile fil, gitcommitfile fil2, "
            + "  gitpullrequest_gitrepositorycommit prc,"
            + "  gitpullrequest_gitrepositorycommit prc2,"
            + "  gitrepositorycommit rc,"
            + "  gitrepositorycommit rc2,"
            + "  gitcommituser u,"
            + "  gitcommituser u2"
            + " WHERE pul.issue_id = i.id"
            + "   AND prc.entitypullrequest_id = pul.id"
            + "   AND fil.repositorycommit_id = prc.repositorycommits_id"
            + "   AND fil.repositorycommit_id = rc.id"
            + "   AND u.id = rc.committer_id"
            + "   AND prc2.entitypullrequest_id = pul.id"
            + "   AND fil2.repositorycommit_id = prc2.repositorycommits_id"
            + "   AND fil2.repositorycommit_id = rc2.id"
            + "   AND u2.id = rc2.committer_id"
            + "   AND fil.filename <> fil2.filename"
            + "   AND u.email = u2.email"
            + "   AND u.name = u2.name"
            + "   AND prc.entitypullrequest_id = prc2.entitypullrequest_id"
            + "   AND pul.repository_id = ?"
            + "   AND fil.filename = ?"
            + "   AND fil2.filename = ?";

    private static final String COUNT_PAIR_FILE_COMMITS
            = "SELECT count(distinct(prc.repositorycommits_id)) FROM "
            + "  gitpullrequest pul, gitissue i,"
            + "  gitcommitfile fil, gitcommitfile fil2, "
            + "  gitpullrequest_gitrepositorycommit prc,"
            + "  gitpullrequest_gitrepositorycommit prc2,"
            + "  gitrepositorycommit rc,"
            + "  gitrepositorycommit rc2,"
            + "  gitcommituser u,"
            + "  gitcommituser u2"
            + " WHERE pul.issue_id = i.id"
            + "   AND prc.entitypullrequest_id = pul.id"
            + "   AND fil.repositorycommit_id = prc.repositorycommits_id"
            + "   AND fil.repositorycommit_id = rc.id"
            + "   AND u.id = rc.committer_id"
            + "   AND prc2.entitypullrequest_id = pul.id"
            + "   AND fil2.repositorycommit_id = prc2.repositorycommits_id"
            + "   AND fil2.repositorycommit_id = rc2.id"
            + "   AND u2.id = rc2.committer_id"
            + "   AND fil.filename <> fil2.filename"
            + "   AND prc.entitypullrequest_id = prc2.entitypullrequest_id"
            + "   AND pul.repository_id = ?"
            + "   AND fil.filename = ?"
            + "   AND fil2.filename = ?";

    private static final String COUNT_PAIR_FILE_COMMITTERS
            = "SELECT count(distinct(coalesce(nullif(trim(u.email), ''), u.name))) FROM "
            + "  gitpullrequest pul, gitissue i,"
            + "  gitcommitfile fil, gitcommitfile fil2, "
            + "  gitpullrequest_gitrepositorycommit prc,"
            + "  gitrepositorycommit rc,"
            + "  gitcommituser u,"
            + "  gitpullrequest_gitrepositorycommit prc2,"
            + "  gitrepositorycommit rc2,"
            + "  gitcommituser u2"
            + " WHERE pul.issue_id = i.id"
            + "   AND prc.entitypullrequest_id = pul.id"
            + "   AND fil.repositorycommit_id = prc.repositorycommits_id"
            + "   AND fil.repositorycommit_id = rc.id"
            + "   AND rc.committer_id = u.id"
            + "   AND prc2.entitypullrequest_id = pul.id"
            + "   AND fil2.repositorycommit_id = prc2.repositorycommits_id"
            + "   AND fil2.repositorycommit_id = rc2.id"
            + "   AND rc2.committer_id = u2.id"
            + "   AND fil.filename <> fil2.filename"
            + "   AND prc.entitypullrequest_id = prc2.entitypullrequest_id"
            + "   AND pul.repository_id = ?"
            + "   AND fil.filename = ?"
            + "   AND fil2.filename = ?";

    private static final String FILTER_BY_USER_NAME_OR_EMAIL
            = "   AND ((u.email = ? AND u2.email = ?)"
            + "    OR (u.name = ? AND u2.name = ?))";

    private static final String SELECT_COMMITTERS_X_COMMITS_PER_ISSUE
            = "SELECT count(distinct(u.email)), count(distinct(rc.id)) FROM "
            + "	 gitpullrequest pul, gitissue i,"
            + "	 gitcommitfile fil, gitcommitfile fil2, "
            + "	 gitpullrequest_gitrepositorycommit prc,"
            + "	 gitrepositorycommit rc,"
            + "	 gitcommituser u,"
            + "	 gitpullrequest_gitrepositorycommit prc2,"
            + "	 gitrepositorycommit rc2,"
            + "	 gitcommituser u2"
            + " WHERE pul.issue_id = i.id"
            + "   AND prc.entitypullrequest_id = pul.id"
            + "   AND rc.id = prc.repositorycommits_id"
            + "   AND fil.repositorycommit_id = rc.id"
            + "   AND rc.committer_id = u.id"
            + "   AND prc2.entitypullrequest_id = pul.id"
            + "   AND rc2.id = prc2.repositorycommits_id"
            + "   AND fil2.repositorycommit_id = rc2.id"
            + "   AND rc2.committer_id = u2.id"
            + "   AND fil.filename <> fil2.filename"
            + "   AND prc.entitypullrequest_id = prc2.entitypullrequest_id"
            + "   AND pul.repository_id = ?"
            + MERGED_PULL_REQUEST_ONLY
            + FILTER_BY_PULL_REQUEST_CREATION_DATE
            + "   AND fil.filename = ?"
            + "   AND fil2.filename = ?"
            + " GROUP BY i.id";

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

    public AuxCodeChurn calculeCodeChurnAddDelChange(EntityRepository repository,
            String fileName, String fileName2, Date beginDate, Date endDate) {

        Object[] bdObjects = new Object[]{
            repository,
            fileName,
            fileName2,
            beginDate,
            endDate
        };

        List<Object[]> sum = dao.selectWithParams(
                SELECT_SUM_OF_ADD_DEL_CHANGES_IN_FILE_BY_DATE,
                SELECT_SUM_OF_ADD_DEL_CHANGES_IN_FILE_BY_DATE_PARAMS, bdObjects, Object[].class);

        return new AuxCodeChurn(fileName, fileName2,
                (Long) sum.get(0)[0], (Long) sum.get(0)[1], (Long) sum.get(0)[2]);
    }

    public Set<AuxUser> selectCommitters(EntityRepository repository,
            String file, String file2, Date beginDate, Date endDate) {
        return selectCommitters(repository, file, file2, beginDate, endDate, true);
    }

    public Set<AuxUser> selectCommitters(EntityRepository repository,
            String file, String file2, Date beginDate, Date endDate, boolean onlyMergeds) {

        List<Object> selectParams = new ArrayList<>();
        if (file == null || file2 == null) {
            throw new IllegalArgumentException("Pair file could not be null");
        }

        StringBuilder sql = new StringBuilder();
        sql.append(SELECT_PAIR_FILE_COMMITTERS);

        selectParams.add(repository.getId());
        selectParams.add(file);
        selectParams.add(file2);

        if (onlyMergeds) {
            sql.append(MERGED_PULL_REQUEST_ONLY);
        }

        if (beginDate != null) {
            sql.append(FILTER_BY_AFTER_PULL_REQUEST_CREATION_DATE);
            selectParams.add(beginDate);
        }

        if (endDate != null) {
            sql.append(FILTER_BY_BEFORE_PULL_REQUEST_CREATION_DATE);
            selectParams.add(endDate);
        }

        List<Object[]> committers = dao.selectNativeWithParams(
                sql.toString(), selectParams.toArray());

        Set<AuxUser> commitersList = new HashSet<>(committers.size());
        for (Object[] nameAndEmail : committers) {
            commitersList.add(new AuxUser((String) nameAndEmail[0],
                    (String) nameAndEmail[1]));
        }

        return commitersList;
    }

    public long calculeCommits(EntityRepository repository,
            String file, String file2) {
        return calculeCommits(repository, file, file2, null, null, null, true);
    }

    public long calculeCommits(EntityRepository repository,
            String file, String file2, Date beginDate, Date endDate) {
        return calculeCommits(repository, file, file2, null, beginDate, endDate, true);
    }

    public long calculeCommits(EntityRepository repository,
            String file, String file2, Date beginDate, Date endDate, boolean onlyMergeds) {
        return calculeCommits(repository, file, file2, null, beginDate, endDate, onlyMergeds);
    }

    public long calculeCommits(EntityRepository repository,
            String file, String file2, String user, Date beginDate, Date endDate) {
        return calculeCommits(repository, file, file2, user, beginDate, endDate, true);
    }

    public long calculeCommits(EntityRepository repository,
            String file, String file2, String user, Date beginDate, Date endDate, boolean onlyMergeds) {
        List<Object> selectParams = new ArrayList<>();

        if (file == null || file2 == null) {
            throw new IllegalArgumentException("Pair file could not be null");
        }

        StringBuilder sql = new StringBuilder();
        sql.append(COUNT_PAIR_FILE_COMMITS);

        selectParams.add(repository.getId());
        selectParams.add(file);
        selectParams.add(file2);

        if (onlyMergeds) {
            sql.append(MERGED_PULL_REQUEST_ONLY);
        }

        if (beginDate != null && endDate != null) {
            sql.append(FILTER_BY_PULL_REQUEST_CREATION_DATE);
            selectParams.add(beginDate);
            selectParams.add(endDate);
        }

        if (user != null) {
            sql.append(FILTER_BY_USER_NAME_OR_EMAIL);
            selectParams.add(user); // commiter email of file 1
            selectParams.add(user); // commiter email of file 2
            selectParams.add(user); // commiter name of file 1
            selectParams.add(user); // commiter name of file 2
        }

        Long count = dao.selectNativeOneWithParams(sql.toString(), selectParams.toArray());

        return count != null ? count : 0l;
    }

    public long calculeCommitters(EntityRepository repository,
            String file, String file2) {
        return calculeCommitters(repository, file, file2, null, null, true);
    }

    public long calculeCommitters(EntityRepository repository,
            String file, String file2, Date beginDate, Date endDate) {
        return calculeCommitters(repository, file, file2, beginDate, endDate, true);
    }

    public long calculeCommitters(EntityRepository repository,
            String file, String file2, Date beginDate, Date endDate, boolean onlyMergeds) {
        List<Object> selectParams = new ArrayList<>();

        if (file == null || file2 == null) {
            throw new IllegalArgumentException("Pair file could not be null");
        }

        StringBuilder sql = new StringBuilder();
        sql.append(COUNT_PAIR_FILE_COMMITTERS);

        selectParams.add(repository.getId());
        selectParams.add(file);
        selectParams.add(file2);

        if (onlyMergeds) {
            sql.append(MERGED_PULL_REQUEST_ONLY);
        }

        if (beginDate != null) {
            sql.append(FILTER_BY_AFTER_PULL_REQUEST_CREATION_DATE);
            selectParams.add(beginDate);
        }

        if (endDate != null) {
            sql.append(FILTER_BY_BEFORE_PULL_REQUEST_CREATION_DATE);
            selectParams.add(endDate);
        }

        Long count = dao.selectNativeOneWithParams(sql.toString(), selectParams.toArray());

        return count != null ? count : 0l;
    }

    public final long[][] calculeCommittersXCommits(EntityRepository repository,
            String file, String file2, Date beginDate, Date endDate) {
        List<Object> selectParams = new ArrayList<>();

        if (file == null || file2 == null) {
            throw new IllegalArgumentException("Pair file could not be null");
        }

        selectParams.add(repository.getId());
        selectParams.add(beginDate);
        selectParams.add(endDate);
        selectParams.add(file);
        selectParams.add(file2);

        List<Object[]> list = dao.selectNativeWithParams(SELECT_COMMITTERS_X_COMMITS_PER_ISSUE, selectParams.toArray());

        final long[][] matrix = new long[list.size()][2];
        for (int i = 0; i < list.size(); i++) {
            matrix[i][0] = (Long) list.get(i)[0]; // committers
            matrix[i][1] = (Long) list.get(i)[1]; // commits
        }

        return matrix;
    }
}
