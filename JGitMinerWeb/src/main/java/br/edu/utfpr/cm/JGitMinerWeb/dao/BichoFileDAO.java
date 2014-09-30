package br.edu.utfpr.cm.JGitMinerWeb.dao;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 *
 * @author Rodrigo T. Kuroda
 */
public class BichoFileDAO {

    public static final String SELECT_COUNT_COMMITS_WHERE_FILE_IS_IN
            = "SELECT COUNT(DISTINCT(is.scmlog_id)) "
            + "  FROM {0}_vcs.files fil"
            + "  JOIN {0}_vcs.actions a ON a.file_id = fil.id"
            + "  JOIN {0}_issues.issues_scmlog is ON is.scmlog_id = a.commit_id"
            + "  JOIN {0}_issues.issues i ON i.id = is.issue_id"
            + " WHERE fil.file_name = ? ";

    private static final String FILTER_BY_ISSUE_CREATION_DATE
            = " AND i.date BETWEEN ? AND ? ";

    private static final String FILTER_BY_BEFORE_ISSUE_CREATION_DATE
            = " AND i.date <= ? ";

    private static final String FILTER_BY_AFTER_ISSUE_CREATION_DATE
            = " AND i.date >= ? ";

    private static final String FILTER_BY_MAX_NUMBER_FILES_IN_COMMIT
            = " AND (SELECT COUNT(1)"
            + "        FROM {0}_vcs.files cfil"
            + "        JOIN {0}_vcs.actions ca ON ca.file_id = cfil.id"
            + "        JOIN {0}_vcs.scmlog cs ON cs.id = ca.commit_id"
            + "       WHERE cs.id = s.id) <= ? ";

    private static final String FILTER_BY_MIN_NUMBER_FILES_IN_COMMIT
            = " AND (SELECT COUNT(1)"
            + "        FROM {0}_vcs.files cfil"
            + "        JOIN {0}_vcs.actions ca ON ca.file_id = cfil.id"
            + "        JOIN {0}_vcs.scmlog cs ON cs.id = ca.commit_id"
            + "       WHERE cs.id = s.id) >= ? ";

    private static final String FILTER_BY_MIN_ISSUE_COMMENTS
            = " AND (SELECT COUNT(1)"
            + "        FROM {0}_issues.comments ic2"
            + "       WHERE ic2.issue_id = i.id) > 1";

    private static final String FIXED_ISSUES_ONLY = " AND i.status = 'Fixed' ";

    private static final String COUNT_DISTINCT_COMMITERS_BY_FILE_NAME
            = "SELECT COUNT(DISTINCT(p.name))"
            + "  FROM {0}_vcs.files fil"
            + "  JOIN {0}_vcs.actions a ON a.file_id = fil.id"
            + "  JOIN {0}_vcs.scmlog s ON s.id = a.commit_id"
            + "  JOIN {0}_vcs.people p ON p.id = s.committer_id"
            + "  JOIN {0}_issues.issues_scmlog i2s ON i2s.scmlog_id = a.commit_id"
            + "  JOIN {0}_issues.issues i ON i.id = i2s.issue_id"
            + " WHERE fil.file_name = ?";

    private static final String COUNT_COMMITS_BY_FILE_NAME
            = "SELECT COUNT(s.id)"
            + "  FROM {0}_vcs.files fil"
            + "  JOIN {0}_vcs.actions a ON a.file_id = fil.id"
            + "  JOIN {0}_vcs.scmlog s ON s.id = a.commit_id"
            + " WHERE fil.file_name = ?";

//    private static final String SUM_CODE_CHURN_BY_FILE_NAME
//            = "SELECT coalesce(sum(fil.additions), 0) AS additions, "
//            + "       coalesce(sum(fil.deletions), 0) AS deletions, "
//            + "       coalesce(sum(fil.changes), 0) AS changes FROM "
//            + "  gitpullrequest pul, gitissue i, "
//            + "  gitcommitfile fil, "
//            + "  gitpullrequest_gitrepositorycommit prc,"
//            + "  gitrepositorycommit rc,"
//            + "  gitcommit c,"
//            + "  gitcommituser u"
//            + " WHERE pul.issue_id = i.id"
//            + "   AND prc.entitypullrequest_id = pul.id"
//            + "   AND prc.repositorycommits_id = rc.id"
//            + "   AND fil.repositorycommit_id = rc.id"
//            + "   AND rc.commit_id = c.id"
//            + "   AND c.committer_id = u.id"
//            + "   AND pul.repository_id = ? "
//            + "   AND fil.filename = ?";

    private static final String FILTER_BY_USER_EMAIL_OR_NAME
            = " AND (p.email = ? OR p.name = ?)";

//    private static final String SELECT_SUM_OF_CHANGES_OF_FILE_BY_DATE
//            = "SELECT sum(fil.changes) AS codeChurn"
//            + "  FROM gitpullrequest pul,"
//            + "       gitcommitfile fil,"
//            + "       gitpullrequest_gitrepositorycommit prc"
//            + " WHERE prc.entitypullrequest_id = pul.id"
//            + "   AND fil.repositorycommit_id = prc.repositorycommits_id"
//            + "   AND pul.mergedat IS NOT NULL"
//            + "   AND pul.repository_id = ?"
//            + "   AND fil.filename = ?"
//            + "   AND pul.createdat between ? AND ?";

    private final GenericBichoDAO dao;

    public BichoFileDAO(GenericBichoDAO dao) {
        this.dao = dao;
    }

    // Pull Requests ///////////////////////////////////////////////////////////
    public long calculeNumberOfPullRequestWhereFileIsIn(String repository,
            String filename, Date beginDate, Date endDate) {
        return calculeNumberOfPullRequestWhereFileIsIn(repository, filename,
                beginDate, endDate, 0, 0, true);
    }

    public long calculeNumberOfPullRequestWhereFileIsIn(String repository,
            String filename, Date beginDate, Date endDate,
            int minFilesPerCommit, int maxFilesPerCommit) {
        return calculeNumberOfPullRequestWhereFileIsIn(repository, filename,
                beginDate, endDate,
                minFilesPerCommit, maxFilesPerCommit,
                true);
    }

    public long calculeNumberOfPullRequestWhereFileIsIn(String repository,
            String filename, Date beginDate, Date endDate,
            int minFilesPerCommit, int maxFilesPerCommit, boolean onlyFixed) {
        List<Object> selectParams = new ArrayList<>();

        StringBuilder sql = new StringBuilder();
        sql.append(getQueryForDatabase(SELECT_COUNT_COMMITS_WHERE_FILE_IS_IN, repository));
        sql.append(FILTER_BY_MIN_ISSUE_COMMENTS);
        selectParams.add(filename);

        if (beginDate != null && endDate != null) { // from begin to end
            sql.append(FILTER_BY_ISSUE_CREATION_DATE);
            selectParams.add(beginDate);
            selectParams.add(endDate);
        } else if (beginDate != null) { // from begin
            sql.append(FILTER_BY_AFTER_ISSUE_CREATION_DATE);
            selectParams.add(beginDate);
        } else if (endDate != null) { // until end
            sql.append(FILTER_BY_BEFORE_ISSUE_CREATION_DATE);
            selectParams.add(endDate);
        }

        if (onlyFixed) {
            sql.append(FIXED_ISSUES_ONLY);
        }

        if (minFilesPerCommit > 0) {
            sql.append(FILTER_BY_MIN_NUMBER_FILES_IN_COMMIT);
            selectParams.add(minFilesPerCommit);
        }

        if (maxFilesPerCommit > 0) {
            sql.append(FILTER_BY_MAX_NUMBER_FILES_IN_COMMIT);
            selectParams.add(maxFilesPerCommit);
        }

        Long count = (Long) dao.selectNativeOneWithParams(
                getQueryForDatabase(sql.toString(), repository),
                selectParams.toArray());

        return count != null ? count : 0l;
    }

    private static String getQueryForDatabase(String query, String repository) {
        return MessageFormat.format(query, new Object[]{repository});
    }

    // COMMITTERS //////////////////////////////////////////////////////////////
    public long countDistinctCommittersByFilename(String repository,
            String filename, Date beginDate, Date endDate) {
        return countDistinctCommittersByFilename(repository, filename, null,
                beginDate, endDate, 0, 0, true);
    }

    public long countDistinctCommittersByFilename(String repository,
            String filename, String user, Date beginDate, Date endDate) {
        return countDistinctCommittersByFilename(repository, filename, user,
                beginDate, endDate, 0, 0, true);
    }

    public long countDistinctCommittersByFilename(String repository,
            String filename, String user, Date beginDate, Date endDate,
            int minFilesPerCommit, int maxFilesPerCommit, boolean onlyFixed) {
        List<Object> selectParams = new ArrayList<>();

        StringBuilder sql = new StringBuilder();
        sql.append(getQueryForDatabase(COUNT_DISTINCT_COMMITERS_BY_FILE_NAME, repository));

        selectParams.add(maxFilesPerCommit);
        selectParams.add(filename);

        if (beginDate != null && endDate != null) { // from begin to end
            sql.append(FILTER_BY_ISSUE_CREATION_DATE);
            selectParams.add(beginDate);
            selectParams.add(endDate);
        } else if (beginDate != null) { // from begin
            sql.append(FILTER_BY_AFTER_ISSUE_CREATION_DATE);
            selectParams.add(beginDate);
        } else if (endDate != null) { // until end
            sql.append(FILTER_BY_BEFORE_ISSUE_CREATION_DATE);
            selectParams.add(endDate);
        }

        if (onlyFixed) {
            sql.append(FIXED_ISSUES_ONLY);
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
    public long countCommitsByFilename(String repository,
            String filename, Date beginDate, Date endDate) {
        return countCommitsByFilename(repository, filename, null, beginDate, endDate, 0, 0, true);
    }

    public long countCommitsByFilename(String repository,
            String filename, String user, Date beginDate, Date endDate) {
        return countCommitsByFilename(repository, filename, user, beginDate, endDate, 0, 0, true);
    }

    public long countCommitsByFilename(String repository,
            String filename, String user, Date beginDate, Date endDate,
            int minFilesPerCommit, int maxFilesPerCommit) {
        return countCommitsByFilename(repository, filename, user,
                beginDate, endDate,
                minFilesPerCommit, maxFilesPerCommit, true);
    }

    public long countCommitsByFilename(String repository,
            String filename, String user, Date beginDate, Date endDate,
            int minFilesPerCommit, int maxFilesPerCommit, boolean onlyFixed) {
        List<Object> selectParams = new ArrayList<>();

        StringBuilder sql = new StringBuilder();
        sql.append(COUNT_COMMITS_BY_FILE_NAME);

        selectParams.add(repository);
        selectParams.add(filename);

        if (beginDate != null && endDate != null) { // from begin to end
            sql.append(FILTER_BY_ISSUE_CREATION_DATE);
            selectParams.add(beginDate);
            selectParams.add(endDate);
        } else if (beginDate != null) { // from begin
            sql.append(FILTER_BY_AFTER_ISSUE_CREATION_DATE);
            selectParams.add(beginDate);
        } else if (endDate != null) { // until end
            sql.append(FILTER_BY_BEFORE_ISSUE_CREATION_DATE);
            selectParams.add(endDate);
        }

        if (onlyFixed) {
            sql.append(FIXED_ISSUES_ONLY);
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
//    public AuxCodeChurn sumCodeChurnByFilename(String repository,
//            String filename, Date beginDate, Date endDate) {
//        return sumCodeChurnByFilename(repository, filename, null, beginDate, endDate, 0, 0, true);
//    }
//
//    public AuxCodeChurn sumCodeChurnByFilename(String repository,
//            String filename, String user, Date beginDate, Date endDate) {
//        return sumCodeChurnByFilename(repository, filename, user, beginDate, endDate, 0, 0, true);
//    }
//
//    public AuxCodeChurn sumCodeChurnByFilename(String repository,
//            String filename, String user, Date beginDate, Date endDate,
//            int minFilesPerCommit, int maxFilesPerCommit) {
//        return sumCodeChurnByFilename(repository, filename, user,
//                beginDate, endDate,
//                minFilesPerCommit, maxFilesPerCommit, true);
//    }
//
//    public AuxCodeChurn sumCodeChurnByFilename(String repository,
//            String fileName, String user, Date beginDate, Date endDate,
//            int minFilesPerCommit, int maxFilesPerCommit, boolean onlyFixed) {
//        List<Object> selectParams = new ArrayList<>();
//
//        StringBuilder sql = new StringBuilder();
//        sql.append(SUM_CODE_CHURN_BY_FILE_NAME);
//
//        selectParams.add(repository);
//        selectParams.add(fileName);
//
//        if (beginDate != null) {
//            sql.append(FILTER_BY_AFTER_PULL_REQUEST_CREATION_DATE);
//            selectParams.add(beginDate);
//        }
//
//        if (endDate != null) {
//            sql.append(FILTER_BY_BEFORE_PULL_REQUEST_CREATION_DATE);
//            selectParams.add(endDate);
//        }
//
//        if (onlyFixed) {
//            sql.append(FIXED_ISSUES_ONLY);
//        }
//
//        if (minFilesPerCommit > 0) {
//            sql.append(FILTER_BY_MIN_NUMBER_FILES_IN_COMMIT);
//            selectParams.add(minFilesPerCommit);
//        }
//
//        if (maxFilesPerCommit > 0) {
//            sql.append(FILTER_BY_MAX_NUMBER_FILES_IN_COMMIT);
//            selectParams.add(maxFilesPerCommit);
//        }
//
//        if (user != null) {
//            sql.append(FILTER_BY_USER_EMAIL_OR_NAME);
//            selectParams.add(user);
//            selectParams.add(user);
//        }
//
//        List<Object[]> sum = dao.selectNativeWithParams(sql.toString(),
//                selectParams.toArray());
//
//        return new AuxCodeChurn(fileName,
//                (Long) sum.get(0)[0], (Long) sum.get(0)[1], (Long) sum.get(0)[2]);
//    }
//
//    public long calculeCodeChurn(String repository, String fileName, Date beginDate, Date endDate) {
//        Object[] bdObjects = new Object[]{
//            repository,
//            fileName,
//            beginDate,
//            endDate
//        };
//
//        Long sum = dao.selectNativeOneWithParams(
//                SELECT_SUM_OF_CHANGES_OF_FILE_BY_DATE, bdObjects);
//
//        return sum;
//    }
}
