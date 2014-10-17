package br.edu.utfpr.cm.JGitMinerWeb.dao;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 *
 * @author Rodrigo T. Kuroda
 */
public class BichoFileDAO {

    // filters
    private final String FILTER_BY_ISSUE_CREATION_DATE;

    private final String FILTER_BY_BEFORE_ISSUE_CREATION_DATE;

    private final String FILTER_BY_AFTER_ISSUE_CREATION_DATE;

    private final String FILTER_BY_MAX_FILES_IN_COMMIT;

    private final String FILTER_BY_MIN_FILES_IN_COMMIT;

    private final String FILTER_BY_MIN_ISSUE_COMMENTS;

    private final String FILTER_BY_USER_NAME;

    private final String FIXED_ISSUES_ONLY;

    private final String FILTER_BY_ISSUES_THAT_HAS_AT_LEAST_ONE_COMMENT;

    // queries
    private final String COUNT_DISTINCT_COMMITERS_BY_FILE_NAME;

    private final String COUNT_COMMITS_BY_FILE_NAME;

    private final String COUNT_ISSUES;

    private final String SUM_CODE_CHURN_BY_FILE_NAME;

    private final String SUM_CHANGES_OF_FILE;

    private final GenericBichoDAO dao;

    public BichoFileDAO(GenericBichoDAO dao, String repository) {
        this.dao = dao;

        FILTER_BY_ISSUE_CREATION_DATE
                = " AND i.date BETWEEN ? AND ? ";

        FILTER_BY_BEFORE_ISSUE_CREATION_DATE
                = " AND i.date <= ? ";

        FILTER_BY_AFTER_ISSUE_CREATION_DATE
                = " AND i.date >= ? ";

        FILTER_BY_MAX_FILES_IN_COMMIT
                = QueryUtils.getQueryForDatabase(" AND (SELECT COUNT(1)"
                        + "        FROM {0}_vcs.files cfil"
                + "        JOIN {0}_vcs.actions ca ON ca.file_id = cfil.id"
                + "        JOIN {0}_vcs.scmlog cs ON cs.id = ca.commit_id"
                + "       WHERE cs.id = s.id) <= ? ", repository);

        FILTER_BY_MIN_FILES_IN_COMMIT
                = QueryUtils.getQueryForDatabase(" AND (SELECT COUNT(1)"
                        + "        FROM {0}_vcs.files cfil"
                + "        JOIN {0}_vcs.actions ca ON ca.file_id = cfil.id"
                + "        JOIN {0}_vcs.scmlog cs ON cs.id = ca.commit_id"
                + "       WHERE cs.id = s.id) >= ? ", repository);

        FILTER_BY_MIN_ISSUE_COMMENTS
                = QueryUtils.getQueryForDatabase(
                        " AND (SELECT COUNT(1)"
                + "        FROM {0}_issues.comments ic2"
                + "       WHERE ic2.issue_id = i.id) > 0", repository);

        FILTER_BY_ISSUES_THAT_HAS_AT_LEAST_ONE_COMMENT
                = QueryUtils.getQueryForDatabase(" AND (SELECT COUNT(1)"
                        + "        FROM {0}_issues.comments ic2"
                        + "       WHERE ic2.issue_id = i.id) > 0", repository);

        FILTER_BY_USER_NAME
                = " AND (p.name IS NOT NULL AND p2.name IS NOT NULL AND p.name = ? AND p2.name = ?)";

        FIXED_ISSUES_ONLY = " AND i.status = 'Fixed' ";

        COUNT_ISSUES
                = QueryUtils.getQueryForDatabase("SELECT DISTINCT(i.id)"
                        + "  FROM {0}_vcs.scmlog s"
                + "  JOIN {0}_issues.issues_scmlog i2s ON i2s.scmlog_id = s.id"
                + "  JOIN {0}_issues.issues i ON i.id = i2s.issue_id"
                + "  JOIN {0}_vcs.actions a ON a.commit_id = s.id"
                + "  JOIN {0}_vcs.files fil ON fil.id = a.file_id"
                + "  JOIN {0}_vcs.file_links fill ON fill.file_id = fil.id"
                        + " WHERE fil.file_name = ?", repository);

        COUNT_DISTINCT_COMMITERS_BY_FILE_NAME
                = QueryUtils.getQueryForDatabase("SELECT COUNT(DISTINCT(p.name))"
                        + "  FROM {0}_vcs.files fil"
                + "  JOIN {0}_vcs.actions a ON a.file_id = fil.id"
                + "  JOIN {0}_vcs.scmlog s ON s.id = a.commit_id"
                + "  JOIN {0}_vcs.people p ON p.id = s.committer_id"
                + "  JOIN {0}_issues.issues_scmlog i2s ON i2s.scmlog_id = a.commit_id"
                + "  JOIN {0}_issues.issues i ON i.id = i2s.issue_id"
                + " WHERE fil.file_name = ?", repository);

        COUNT_COMMITS_BY_FILE_NAME
                = QueryUtils.getQueryForDatabase("SELECT COUNT(s.id)"
                        + "  FROM {0}_vcs.files fil"
                + "  JOIN {0}_vcs.actions a ON a.file_id = fil.id"
                + "  JOIN {0}_vcs.scmlog s ON s.id = a.commit_id"
                + " WHERE fil.file_name = ?", repository);

        SUM_CODE_CHURN_BY_FILE_NAME
                = QueryUtils.getQueryForDatabase("SELECT (SUM(filcl.added)),"
                        + "       (SUM(filcl.removed))"
                + "  FROM {0}_vcs.scmlog s"
                + "  JOIN {0}_issues.issues_scmlog i2s ON i2s.scmlog_id = s.id"
                + "  JOIN {0}_issues.issues i ON i.id = i2s.issue_id"
                + "  JOIN {0}_vcs.actions a ON a.commit_id = s.id"
                + "  JOIN {0}_vcs.files fil ON fil.id = a.file_id"
                + "  JOIN {0}_vcs.file_links fill ON fill.file_id = fil.id"
                + "  JOIN {0}_vcs.commits_files_lines filcl ON filcl.file_id = fil.id AND filcl.commit_id = s.id"
                        + " WHERE fil.file_name = ?", repository);

        SUM_CHANGES_OF_FILE
                = QueryUtils.getQueryForDatabase("SELECT (SUM(filcl.added) + SUM(filcl.removed))"
                        + "  FROM {0}_vcs.scmlog s"
                + "  JOIN {0}_issues.issues_scmlog i2s ON i2s.scmlog_id = s.id"
                + "  JOIN {0}_issues.issues i ON i.id = i2s.issue_id"
                + "  JOIN {0}_vcs.actions a ON a.commit_id = s.id"
                + "  JOIN {0}_vcs.files fil ON fil.id = a.file_id"
                + "  JOIN {0}_vcs.file_links fill ON fill.file_id = fil.id"
                + "  JOIN {0}_vcs.commits_files_lines filcl ON filcl.file_id = fil.id AND filcl.commit_id = s.id"
                        + " WHERE fil.file_name = ?", repository);
    }

    // Issues //////////////////////////////////////////////////////////////////
    public long calculeNumberOfIssues(
            String filename, Date beginDate, Date endDate) {
        return calculeNumberOfIssues(filename,
                beginDate, endDate, 0, 0, true);
    }

    public long calculeNumberOfIssues(
            String filename, Date beginDate, Date endDate, boolean onlyFixed) {
        return calculeNumberOfIssues(filename,
                beginDate, endDate, 0, 0, onlyFixed);
    }

    public long calculeNumberOfIssues(
            String filename, Date beginDate, Date endDate,
            int minFilesPerCommit, int maxFilesPerCommit) {
        return calculeNumberOfIssues(filename,
                beginDate, endDate,
                minFilesPerCommit, maxFilesPerCommit,
                true);
    }

    public long calculeNumberOfIssues(
            String filename, Date beginDate, Date endDate,
            int minFilesPerCommit, int maxFilesPerCommit, boolean onlyFixed) {
        List<Object> selectParams = new ArrayList<>();

        StringBuilder sql = new StringBuilder();
        sql.append(COUNT_ISSUES);
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
            sql.append(FILTER_BY_MIN_FILES_IN_COMMIT);
            selectParams.add(minFilesPerCommit);
        }

        if (maxFilesPerCommit > 0) {
            sql.append(FILTER_BY_MAX_FILES_IN_COMMIT);
            selectParams.add(maxFilesPerCommit);
        }

        Long count = (Long) dao.selectNativeOneWithParams(
                sql.toString(),
                selectParams.toArray());

        return count != null ? count : 0l;
    }

    // COMMITTERS //////////////////////////////////////////////////////////////
    public long countDistinctCommittersByFilename(
            String filename, Date beginDate, Date endDate) {
        return countDistinctCommittersByFilename(filename, null,
                beginDate, endDate, 0, 0, true);
    }

    public long countDistinctCommittersByFilename(
            String filename, String user, Date beginDate, Date endDate) {
        return countDistinctCommittersByFilename(filename, user,
                beginDate, endDate, 0, 0, true);
    }

    public long countDistinctCommittersByFilename(
            String filename, String user, Date beginDate, Date endDate,
            int minFilesPerCommit, int maxFilesPerCommit, boolean onlyFixed) {
        List<Object> selectParams = new ArrayList<>();

        StringBuilder sql = new StringBuilder();
        sql.append(COUNT_DISTINCT_COMMITERS_BY_FILE_NAME);

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
            sql.append(FILTER_BY_MIN_FILES_IN_COMMIT);
            selectParams.add(minFilesPerCommit);
        }

        if (maxFilesPerCommit > 0) {
            sql.append(FILTER_BY_MAX_FILES_IN_COMMIT);
            selectParams.add(maxFilesPerCommit);
        }

        if (user != null) {
            sql.append(FILTER_BY_USER_NAME);
            selectParams.add(user);
        }

        Long count = (Long) dao.selectNativeOneWithParams(sql.toString(), selectParams.toArray());

        return count != null ? count : 0l;
    }

    // COMMITS /////////////////////////////////////////////////////////////////
    public long countCommitsByFilename(
            String filename, Date beginDate, Date endDate) {
        return countCommitsByFilename(filename, null, beginDate, endDate, 0, 0, true);
    }

    public long countCommitsByFilename(
            String filename, String user, Date beginDate, Date endDate) {
        return countCommitsByFilename(filename, user, beginDate, endDate, 0, 0, true);
    }

    public long countCommitsByFilename(
            String filename, String user, Date beginDate, Date endDate,
            int minFilesPerCommit, int maxFilesPerCommit) {
        return countCommitsByFilename(filename, user,
                beginDate, endDate,
                minFilesPerCommit, maxFilesPerCommit, true);
    }

    public long countCommitsByFilename(
            String filename, String user, Date beginDate, Date endDate,
            int minFilesPerCommit, int maxFilesPerCommit, boolean onlyFixed) {
        List<Object> selectParams = new ArrayList<>();

        StringBuilder sql = new StringBuilder();
        sql.append(COUNT_COMMITS_BY_FILE_NAME);

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
            sql.append(FILTER_BY_MIN_FILES_IN_COMMIT);
            selectParams.add(minFilesPerCommit);
        }

        if (maxFilesPerCommit > 0) {
            sql.append(FILTER_BY_MAX_FILES_IN_COMMIT);
            selectParams.add(maxFilesPerCommit);
        }

        if (user != null) {
            sql.append(FILTER_BY_USER_NAME);
            selectParams.add(user);
        }

        Long count = (Long) dao.selectNativeOneWithParams(sql.toString(), selectParams.toArray());

        return count != null ? count : 0l;
    }

    // CORE CHURN //////////////////////////////////////////////////////////////
    public AuxCodeChurn sumCodeChurnByFilename(
            String filename, Date beginDate, Date endDate) {
        return sumCodeChurnByFilename(filename, null, beginDate, endDate, 0, 0, true);
    }

    public AuxCodeChurn sumCodeChurnByFilename(
            String filename, String user, Date beginDate, Date endDate) {
        return sumCodeChurnByFilename(filename, user, beginDate, endDate, 0, 0, true);
    }

    public AuxCodeChurn sumCodeChurnByFilename(
            String filename, String user, Date beginDate, Date endDate,
            int minFilesPerCommit, int maxFilesPerCommit) {
        return sumCodeChurnByFilename(filename, user,
                beginDate, endDate,
                minFilesPerCommit, maxFilesPerCommit, true);
    }

    public AuxCodeChurn sumCodeChurnByFilename(
            String fileName, String user, Date beginDate, Date endDate,
            int minFilesPerCommit, int maxFilesPerCommit, boolean onlyFixed) {
        List<Object> selectParams = new ArrayList<>();

        StringBuilder sql = new StringBuilder();
        sql.append(SUM_CODE_CHURN_BY_FILE_NAME);

        selectParams.add(fileName);

        if (beginDate != null) {
            sql.append(FILTER_BY_AFTER_ISSUE_CREATION_DATE);
            selectParams.add(beginDate);
        }

        if (endDate != null) {
            sql.append(FILTER_BY_BEFORE_ISSUE_CREATION_DATE);
            selectParams.add(endDate);
        }

        if (onlyFixed) {
            sql.append(FIXED_ISSUES_ONLY);
        }

        if (minFilesPerCommit > 0) {
            sql.append(FILTER_BY_MIN_FILES_IN_COMMIT);
            selectParams.add(minFilesPerCommit);
        }

        if (maxFilesPerCommit > 0) {
            sql.append(FILTER_BY_MAX_FILES_IN_COMMIT);
            selectParams.add(maxFilesPerCommit);
        }

        if (user != null) {
            sql.append(FILTER_BY_USER_NAME);
            selectParams.add(user);
            selectParams.add(user);
        }

        List<Object[]> sum = dao.selectNativeWithParams(sql.toString(),
                selectParams.toArray());

        return new AuxCodeChurn(fileName,
                (Long) sum.get(0)[0], (Long) sum.get(0)[1], (Long) sum.get(0)[2]);
    }

    public long calculeCodeChurn(String fileName, Date beginDate, Date endDate) {
        Object[] bdObjects = new Object[]{
            fileName,
            beginDate,
            endDate
        };

        Long sum = dao.selectNativeOneWithParams(SUM_CHANGES_OF_FILE, bdObjects);

        return sum;
    }
}
