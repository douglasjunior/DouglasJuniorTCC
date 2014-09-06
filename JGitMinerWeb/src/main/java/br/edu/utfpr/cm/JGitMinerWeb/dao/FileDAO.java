
package br.edu.utfpr.cm.JGitMinerWeb.dao;

import br.edu.utfpr.cm.JGitMinerWeb.model.miner.EntityRepository;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 *
 * @author Rodrigo T. Kuroda
 */
public class FileDAO {

    public static final String SELECT_COUNT_PULL_REQUEST_WHERE_FILE_IS_IN
            = "SELECT count(distinct(pul.id)) "
            + " FROM gitcommitfile fil, "
            + "      gitpullrequest_gitrepositorycommit prc, "
            + "      gitpullrequest pul "
            + " WHERE fil.repositorycommit_id = prc.repositorycommits_id "
            + "   AND pul.id = prc.entitypullrequest_id "
            + "   AND pul.repository_id = ? "
            + "   AND fil.filename = ? ";

    private static final String FILTER_BY_PULL_REQUEST_CREATION_DATE
            = " AND pul.createdat BETWEEN ? AND ? ";

    private static final String FILTER_BY_BEFORE_PULL_REQUEST_CREATION_DATE
            = " AND pul.createdat <= ? ";

    private static final String FILTER_BY_AFTER_PULL_REQUEST_CREATION_DATE
            = " AND pul.createdat >= ? ";

    private static final String FILTER_BY_MAX_NUMBER_FILES_IN_COMMIT
            = " AND (SELECT count(distinct(cf.filename)) FROM gitcommitfile cf WHERE cf.repositorycommit_id = prc.repositorycommits_id) <= ? ";

    private static final String FILTER_BY_MIN_NUMBER_FILES_IN_COMMIT
            = " AND (SELECT count(distinct(cf.filename)) FROM gitcommitfile cf WHERE cf.repositorycommit_id = prc.repositorycommits_id) >= ? ";

    private static final String MERGED_PULL_REQUEST_ONLY = " AND pul.mergedat IS NOT NULL ";

    private static final String COUNT_DISTINCT_COMMITERS_BY_FILE_NAME
            = "SELECT count(distinct(u.email)) FROM "
            + "  gitpullrequest pul, gitissue i,"
            + "  gitcommitfile fil, "
            + "  gitpullrequest_gitrepositorycommit prc,"
            + "  gitrepositorycommit rc,"
            + "  gitcommit c,"
            + "  gitcommituser u"
            + " WHERE pul.issue_id = i.id"
            + "   AND prc.entitypullrequest_id = pul.id"
            + "   AND prc.repositorycommits_id = rc.id"
            + "   AND fil.repositorycommit_id = rc.id"
            + "   AND rc.commit_id = c.id"
            + "   AND c.committer_id = u.id"
            + "   AND pul.repository_id = ? "
            + "   AND fil.filename = ?";

    private static final String COUNT_COMMITS_BY_FILE_NAME
            = "SELECT count(distinct(rc.id)) FROM "
            + "  gitpullrequest pul, gitissue i,"
            + "  gitcommitfile fil, "
            + "  gitpullrequest_gitrepositorycommit prc,"
            + "  gitrepositorycommit rc,"
            + "  gitcommit c,"
            + "  gitcommituser u"
            + " WHERE pul.issue_id = i.id"
            + "   AND prc.entitypullrequest_id = pul.id"
            + "   AND prc.repositorycommits_id = rc.id"
            + "   AND fil.repositorycommit_id = rc.id"
            + "   AND rc.commit_id = c.id"
            + "   AND c.committer_id = u.id"
            + "   AND pul.repository_id = ? "
            + "   AND fil.filename = ?";

    private static final String SUM_CODE_CHURN_BY_FILE_NAME
            = "SELECT coalesce(sum(fil.additions), 0) AS additions, "
            + "       coalesce(sum(fil.deletions), 0) AS deletions, "
            + "       coalesce(sum(fil.changes), 0) AS changes FROM "
            + "  gitpullrequest pul, gitissue i, "
            + "  gitcommitfile fil, "
            + "  gitpullrequest_gitrepositorycommit prc,"
            + "  gitrepositorycommit rc,"
            + "  gitcommit c,"
            + "  gitcommituser u"
            + " WHERE pul.issue_id = i.id"
            + "   AND prc.entitypullrequest_id = pul.id"
            + "   AND prc.repositorycommits_id = rc.id"
            + "   AND fil.repositorycommit_id = rc.id"
            + "   AND rc.commit_id = c.id"
            + "   AND c.committer_id = u.id"
            + "   AND pul.repository_id = ? "
            + "   AND fil.filename = ?";

    private static final String FILTER_BY_USER_EMAIL_OR_NAME
            = " AND (u.email = ? OR u.name = ?)";

    private static final String SELECT_SUM_OF_CHANGES_OF_FILE_BY_DATE
            = "SELECT sum(fil.changes) AS codeChurn"
            + "  FROM gitpullrequest pul,"
            + "       gitcommitfile fil,"
            + "       gitpullrequest_gitrepositorycommit prc"
            + " WHERE prc.entitypullrequest_id = pul.id"
            + "   AND fil.repositorycommit_id = prc.repositorycommits_id"
            + "   AND pul.mergedat IS NOT NULL"
            + "   AND pul.repository_id = ?"
            + "   AND fil.filename = ?"
            + "   AND pul.createdat between ? AND ?";

    private final GenericDao dao;

    public FileDAO(GenericDao dao) {
        this.dao = dao;
    }

    // Pull Requests ///////////////////////////////////////////////////////////
    public long calculeNumberOfPullRequestWhereFileIsIn(EntityRepository repository,
            String filename, Date beginDate, Date endDate) {
        return calculeNumberOfPullRequestWhereFileIsIn(repository, filename,
                beginDate, endDate, 0, 0, true);
    }

    public long calculeNumberOfPullRequestWhereFileIsIn(EntityRepository repository,
            String filename, Date beginDate, Date endDate,
            int minFilesPerCommit, int maxFilesPerCommit) {
        return calculeNumberOfPullRequestWhereFileIsIn(repository, filename,
                beginDate, endDate,
                minFilesPerCommit, maxFilesPerCommit,
                true);
    }

    public long calculeNumberOfPullRequestWhereFileIsIn(EntityRepository repository,
            String filename, Date beginDate, Date endDate,
            int minFilesPerCommit, int maxFilesPerCommit, boolean onlyMergeds) {
        List<Object> selectParams = new ArrayList<>();

        StringBuilder sql = new StringBuilder();
        sql.append(SELECT_COUNT_PULL_REQUEST_WHERE_FILE_IS_IN);

        selectParams.add(repository.getId());
        selectParams.add(filename);

        if (beginDate != null && endDate != null) {
            sql.append(FILTER_BY_PULL_REQUEST_CREATION_DATE);
            selectParams.add(beginDate);
            selectParams.add(endDate);
        }

        if (onlyMergeds) {
            sql.append(MERGED_PULL_REQUEST_ONLY);
        }

        if (minFilesPerCommit > 0) {
            sql.append(FILTER_BY_MIN_NUMBER_FILES_IN_COMMIT);
            selectParams.add(minFilesPerCommit);
        }

        if (maxFilesPerCommit > 0) {
            sql.append(FILTER_BY_MAX_NUMBER_FILES_IN_COMMIT);
            selectParams.add(maxFilesPerCommit);
        }

        Long count = (Long) dao.selectNativeOneWithParams(sql.toString(), selectParams.toArray());

        return count != null ? count : 0l;
    }

    // COMMITTERS //////////////////////////////////////////////////////////////
    public long countDistinctCommittersByFilename(EntityRepository repository,
            String filename, Date beginDate, Date endDate) {
        return countDistinctCommittersByFilename(repository, filename, null,
                beginDate, endDate, 0, 0, true);
    }

    public long countDistinctCommittersByFilename(EntityRepository repository,
            String filename, String user, Date beginDate, Date endDate) {
        return countDistinctCommittersByFilename(repository, filename, user,
                beginDate, endDate, 0, 0, true);
    }

    public long countDistinctCommittersByFilename(EntityRepository repository,
            String filename, String user, Date beginDate, Date endDate,
            int minFilesPerCommit, int maxFilesPerCommit, boolean onlyMergeds) {
        List<Object> selectParams = new ArrayList<>();

        StringBuilder sql = new StringBuilder();
        sql.append(COUNT_DISTINCT_COMMITERS_BY_FILE_NAME);

        selectParams.add(repository.getId());
        selectParams.add(filename);

        if (beginDate != null && endDate != null) {
            sql.append(FILTER_BY_PULL_REQUEST_CREATION_DATE);
            selectParams.add(beginDate);
            selectParams.add(endDate);
        }

        if (onlyMergeds) {
            sql.append(MERGED_PULL_REQUEST_ONLY);
        }

        if (minFilesPerCommit > 0) {
            sql.append(FILTER_BY_MIN_NUMBER_FILES_IN_COMMIT);
            selectParams.add(minFilesPerCommit);
        }

        if (maxFilesPerCommit > 0) {
            sql.append(FILTER_BY_MAX_NUMBER_FILES_IN_COMMIT);
            selectParams.add(maxFilesPerCommit);
        }

        if (user != null) {
            sql.append(FILTER_BY_USER_EMAIL_OR_NAME);
            selectParams.add(user);
        }

        Long count = (Long) dao.selectNativeOneWithParams(sql.toString(), selectParams.toArray());

        return count != null ? count : 0l;
    }

    // COMMITS /////////////////////////////////////////////////////////////////
    public long countCommitsByFilename(EntityRepository repository,
            String filename, Date beginDate, Date endDate) {
        return countCommitsByFilename(repository, filename, null, beginDate, endDate, 0, 0, true);
    }

    public long countCommitsByFilename(EntityRepository repository,
            String filename, String user, Date beginDate, Date endDate) {
        return countCommitsByFilename(repository, filename, user, beginDate, endDate, 0, 0, true);
    }

    public long countCommitsByFilename(EntityRepository repository,
            String filename, String user, Date beginDate, Date endDate,
            int minFilesPerCommit, int maxFilesPerCommit) {
        return countCommitsByFilename(repository, filename, user,
                beginDate, endDate,
                minFilesPerCommit, maxFilesPerCommit, true);
    }

    public long countCommitsByFilename(EntityRepository repository,
            String filename, String user, Date beginDate, Date endDate,
            int minFilesPerCommit, int maxFilesPerCommit, boolean onlyMergeds) {
        List<Object> selectParams = new ArrayList<>();

        StringBuilder sql = new StringBuilder();
        sql.append(COUNT_COMMITS_BY_FILE_NAME);

        selectParams.add(repository.getId());
        selectParams.add(filename);

        if (beginDate != null && endDate != null) {
            sql.append(FILTER_BY_PULL_REQUEST_CREATION_DATE);
            selectParams.add(beginDate);
            selectParams.add(endDate);
        }

        if (onlyMergeds) {
            sql.append(MERGED_PULL_REQUEST_ONLY);
        }

        if (minFilesPerCommit > 0) {
            sql.append(FILTER_BY_MIN_NUMBER_FILES_IN_COMMIT);
            selectParams.add(minFilesPerCommit);
        }

        if (maxFilesPerCommit > 0) {
            sql.append(FILTER_BY_MAX_NUMBER_FILES_IN_COMMIT);
            selectParams.add(maxFilesPerCommit);
        }

        if (user != null) {
            sql.append(FILTER_BY_USER_EMAIL_OR_NAME);
            selectParams.add(user);
        }

        Long count = (Long) dao.selectNativeOneWithParams(sql.toString(), selectParams.toArray());

        return count != null ? count : 0l;
    }

    // CORE CHURN //////////////////////////////////////////////////////////////
    public AuxCodeChurn sumCodeChurnByFilename(EntityRepository repository,
            String filename, Date beginDate, Date endDate) {
        return sumCodeChurnByFilename(repository, filename, null, beginDate, endDate, 0, 0, true);
    }

    public AuxCodeChurn sumCodeChurnByFilename(EntityRepository repository,
            String filename, String user, Date beginDate, Date endDate) {
        return sumCodeChurnByFilename(repository, filename, user, beginDate, endDate, 0, 0, true);
    }

    public AuxCodeChurn sumCodeChurnByFilename(EntityRepository repository,
            String filename, String user, Date beginDate, Date endDate,
            int minFilesPerCommit, int maxFilesPerCommit) {
        return sumCodeChurnByFilename(repository, filename, user,
                beginDate, endDate,
                minFilesPerCommit, maxFilesPerCommit, true);
    }

    public AuxCodeChurn sumCodeChurnByFilename(EntityRepository repository,
            String fileName, String user, Date beginDate, Date endDate,
            int minFilesPerCommit, int maxFilesPerCommit, boolean onlyMergeds) {
        List<Object> selectParams = new ArrayList<>();

        StringBuilder sql = new StringBuilder();
        sql.append(SUM_CODE_CHURN_BY_FILE_NAME);

        selectParams.add(repository.getId());
        selectParams.add(fileName);

        if (beginDate != null) {
            sql.append(FILTER_BY_AFTER_PULL_REQUEST_CREATION_DATE);
            selectParams.add(beginDate);
        }

        if (endDate != null) {
            sql.append(FILTER_BY_BEFORE_PULL_REQUEST_CREATION_DATE);
            selectParams.add(endDate);
        }

        if (onlyMergeds) {
            sql.append(MERGED_PULL_REQUEST_ONLY);
        }

        if (minFilesPerCommit > 0) {
            sql.append(FILTER_BY_MIN_NUMBER_FILES_IN_COMMIT);
            selectParams.add(minFilesPerCommit);
        }

        if (maxFilesPerCommit > 0) {
            sql.append(FILTER_BY_MAX_NUMBER_FILES_IN_COMMIT);
            selectParams.add(maxFilesPerCommit);
        }

        if (user != null) {
            sql.append(FILTER_BY_USER_EMAIL_OR_NAME);
            selectParams.add(user);
            selectParams.add(user);
        }

        List<Object[]> sum = dao.selectNativeWithParams(sql.toString(),
                selectParams.toArray());

        return new AuxCodeChurn(fileName,
                (Long) sum.get(0)[0], (Long) sum.get(0)[1], (Long) sum.get(0)[2]);
    }

    public long calculeCodeChurn(EntityRepository repository, String fileName, Date beginDate, Date endDate) {
        Object[] bdObjects = new Object[]{
            repository.getId(),
            fileName,
            beginDate,
            endDate
        };

        Long sum = dao.selectNativeOneWithParams(
                SELECT_SUM_OF_CHANGES_OF_FILE_BY_DATE, bdObjects);

        return sum;
    }
}
