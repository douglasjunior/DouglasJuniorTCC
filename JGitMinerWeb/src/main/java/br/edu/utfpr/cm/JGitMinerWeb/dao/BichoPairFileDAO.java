package br.edu.utfpr.cm.JGitMinerWeb.dao;

import static br.edu.utfpr.cm.JGitMinerWeb.dao.QueryUtils.filterByIssues;
import br.edu.utfpr.cm.minerador.services.metric.model.IssueMetrics;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.joda.time.Days;
import org.joda.time.LocalDate;

/**
 *
 * @author Rodrigo T. Kuroda
 */
public class BichoPairFileDAO {

    // commons query fragment
    private final String FROM_TABLE;
    private final String WHERE;

    // filters
    private final String FILTER_BY_MAX_FILES_IN_COMMIT;

    private final String FILTER_BY_ISSUE_FIX_DATE;

    private final String FILTER_BY_ISSUE_FIX_MAJOR_VERSION;
    private final String FILTER_BY_BEFORE_ISSUE_FIX_MAJOR_VERSION;
    private final String FILTER_BY_BEFORE_ISSUE_FIX_MAJOR_VERSION_EXCLUSIVE;

    private final String FILTER_BY_BEFORE_ISSUE_FIX_DATE;
    private final String FILTER_BY_BEFORE_ISSUE_FIX_DATE_OF_ISSUE_ID;

    private final String FILTER_BY_AFTER_ISSUE_FIX_DATE;

    private final String FIXED_ISSUES_ONLY;

    private final String FILTER_BY_ISSUES_THAT_HAS_AT_LEAST_ONE_COMMENT;

    private final String FILTER_BY_USER_NAME;

    private final String FILTER_BY_ISSUE_ID;

    private final String FILTER_BY_ISSUE_TYPE;

    private final String ORDER_BY_SUBMITTED_ON;

    // complete queries (commons fragment + specific fragments for query)
    private final String COUNT_ISSUES_BY_FILE_NAME;

    private final String SELECT_ISSUES_OF_FILE_PAIR_BY_DATE;
    private final String SELECT_ISSUES_OF_FILE_PAIR_BY_FIX_VERSION;
    private final String SELECT_ISSUES_BY_ISSUES_ID;

    private final String COUNT_COMMENTS_OF_FILE_PAIR_BY_DATE;

    private final String SUM_ADD_DEL_LINES_OF_FILE_PAIR_BY_DATE;
    private final String SUM_ADD_DEL_LINES_OF_FILE_PAIR_BY_FIX_VERSION;
    private final String SUM_CUMMULATIVE_ADD_DEL_LINES_OF_FILE_PAIR_BY_FIX_VERSION;

    private final String SELECT_COMMITTERS_OF_PAIR_FILE_BY_ISSUE_CREATION_DATE;

    private final String SELECT_COMMITTERS_OF_PAIR_FILE_BY_ISSUE_ID;
    private final String SELECT_LAST_COMMITTER_OF_PAIR_FILE;

    private final String SELECT_COMMITTERS_OF_PAIR_FILE;

    private final String COUNT_PAIR_FILE_COMMITS;

    private final String COUNT_PAIR_FILE_COMMITTERS;

    private final String COUNT_COMMENTERS_BY_ISSUE_ID;

    private final String SELECT_RELEASE_MIN_MAX_COMMIT_DATE;
    private final String SELECT_RELEASE_MIN_MAX_COMMIT_DATE_BY_FIX_VERSION;

    private final String SELECT_COMMENTS_BY_ISSUE_ID;

    private final String SELECT_ISSUES_BY_FILE_NAME;

    private final String COUNT_ISSUES_TYPES;

    private final GenericBichoDAO dao;

    public BichoPairFileDAO(GenericBichoDAO dao, String repository, Integer maxFilePerCommit) {
        this.dao = dao;

        if (repository == null
                || maxFilePerCommit == null) {
            throw new IllegalArgumentException("The parameters 'repository' and 'maxFilePerCommit' can not be null.");
        }

        FROM_TABLE
                = "  FROM {0}_issues.issues i"
                + "  JOIN {0}_issues.changes c ON c.issue_id = i.id"
                + "  JOIN {0}_issues.issues_scmlog i2s ON i2s.issue_id = i.id"
                + "  JOIN {0}_vcs.scmlog s ON s.id = i2s.scmlog_id"
                + "  JOIN {0}_issues.issues_scmlog i2s2 ON i2s2.issue_id = i.id"
                + "  JOIN {0}_vcs.scmlog s2 ON s2.id = i2s2.scmlog_id"
                + "  JOIN {0}.commits com ON com.commit_id = i2s.scmlog_id"
                + "  JOIN {0}.commits com2 ON com2.commit_id = i2s2.scmlog_id AND com.file_id <> com2.file_id";

        WHERE
                = " WHERE com.file_path = ?"
                + "   AND com2.file_path = ? "
                + "   AND com.date > i.submitted_on"
                + "   AND com2.date > i.submitted_on";
        // filters
        FILTER_BY_MAX_FILES_IN_COMMIT
                = " AND s.num_files <= " + maxFilePerCommit + " AND s2.num_files <= " + maxFilePerCommit;

        FILTER_BY_ISSUE_FIX_DATE
                = " AND c.changed_on BETWEEN ? AND ?";

        // avoid join, because has poor performance in this case
        FILTER_BY_ISSUE_FIX_MAJOR_VERSION
                = QueryUtils.getQueryForDatabase(
                        " AND i.id IN ("
                        + " SELECT ifv.issue_id "
                        + "   FROM {0}_issues.issues_fix_version ifv "
                        + "  WHERE ifv.major_fix_version = ?)", repository);

        // avoid join, because has poor performance in this case
        FILTER_BY_BEFORE_ISSUE_FIX_MAJOR_VERSION
                = QueryUtils.getQueryForDatabase(
                        " AND i.id IN ("
                        + " SELECT ifv.issue_id "
                        + "   FROM {0}_issues.issues_fix_version ifv "
                        + "  WHERE ifv.major_fix_version IN ("
                        + "SELECT ifvo.major_fix_version"
                        + "  FROM {0}_issues.issues_fix_version_order ifvo"
                        + " WHERE ifvo.version_order <= " // inclusive
                        + "(SELECT MAX(ifvo2.version_order)"
                        + "   FROM {0}_issues.issues_fix_version_order ifvo2"
                        + "  WHERE ifvo2.major_fix_version = ?)))", repository);

        FILTER_BY_BEFORE_ISSUE_FIX_MAJOR_VERSION_EXCLUSIVE
                = QueryUtils.getQueryForDatabase(
                        " AND i.id IN ("
                        + " SELECT ifv.issue_id "
                        + "   FROM {0}_issues.issues_fix_version ifv "
                        + "  WHERE ifv.major_fix_version IN ("
                        + "SELECT ifvo.major_fix_version"
                        + "  FROM {0}_issues.issues_fix_version_order ifvo"
                        + " WHERE ifvo.version_order < " // exclusive
                        + "(SELECT MIN(ifvo2.version_order)"
                        + "   FROM {0}_issues.issues_fix_version_order ifvo2"
                        + "  WHERE ifvo2.major_fix_version = ?)))", repository);

        FILTER_BY_BEFORE_ISSUE_FIX_DATE
                = " AND c.changed_on <= ?";

        FILTER_BY_AFTER_ISSUE_FIX_DATE
                = " AND c.changed_on >= ?";

        FIXED_ISSUES_ONLY
                = " AND i.resolution = \"Fixed\""
                + " AND c.field = \"Resolution\""
                + " AND c.new_value = i.resolution";

        FILTER_BY_BEFORE_ISSUE_FIX_DATE_OF_ISSUE_ID
                = QueryUtils.getQueryForDatabase(
                        " AND c.changed_on <="
                        + "     (SELECT MAX(c2.changed_on)"
                        + "        FROM {0}_issues.changes c2"
                        + "       WHERE c2.issue_id = ?"
                        + "         AND c2.field = \"Resolution\""
                        + "         AND c2.new_value = \"Fixed\")", repository);

        FILTER_BY_ISSUE_TYPE
                = " AND i.type = ?";

        FILTER_BY_ISSUES_THAT_HAS_AT_LEAST_ONE_COMMENT
                = "AND i.num_comments > 0";

        FILTER_BY_USER_NAME
                = " AND p.name = ?";

        FILTER_BY_ISSUE_ID
                = " AND i.id = ?";

        ORDER_BY_SUBMITTED_ON
                = " ORDER BY i.submitted_on";

        COUNT_ISSUES_BY_FILE_NAME
                = QueryUtils.getQueryForDatabase(
                        "SELECT COUNT(DISTINCT(i.id))"
                        + FROM_TABLE
                        + WHERE, repository)
                + FILTER_BY_MAX_FILES_IN_COMMIT;

        SELECT_ISSUES_BY_FILE_NAME
                = QueryUtils.getQueryForDatabase(
                        "SELECT DISTINCT(i.id)"
                        + FROM_TABLE
                        + WHERE, repository)
                + FILTER_BY_MAX_FILES_IN_COMMIT
                + FIXED_ISSUES_ONLY;

        SELECT_ISSUES_OF_FILE_PAIR_BY_DATE
                = QueryUtils.getQueryForDatabase(
                        "SELECT DISTINCT i.id, i.issue, i.description"
                        + FROM_TABLE
                        + WHERE, repository)
                + FILTER_BY_MAX_FILES_IN_COMMIT
                + FILTER_BY_ISSUE_FIX_DATE
                + FIXED_ISSUES_ONLY;

        SELECT_ISSUES_OF_FILE_PAIR_BY_FIX_VERSION
                = QueryUtils.getQueryForDatabase(
                        "SELECT DISTINCT i.id, i.issue, i.description"
                        + FROM_TABLE
                        + " WHERE com.file_path = ?"
                        + "   AND com2.file_path = ?"
                        + "   AND com.date > i.submitted_on"
                        + "   AND com2.date > i.submitted_on", repository)
                + FILTER_BY_MAX_FILES_IN_COMMIT
                + FILTER_BY_ISSUE_FIX_MAJOR_VERSION
                + FIXED_ISSUES_ONLY;

        SELECT_ISSUES_BY_ISSUES_ID
                = QueryUtils.getQueryForDatabase(
                        "SELECT DISTINCT i.id, iej.issue_key, i.issue, "
                        + "     i.description, i.type, i.priority, "
                        + "     assigned.user_id, submitted.user_id, "
                        + "     i.num_watchers, i.reopened_times,"
                        + "     i.num_commenters, i.num_dev_commenters,"
                        + "     i.submitted_on, MAX(c.changed_on)"
                        + "  FROM {0}_issues.issues i"
                        + "  JOIN {0}_issues.issues_ext_jira iej ON iej.issue_id = i.id"
                        + "  JOIN {0}_issues.changes c ON c.issue_id = i.id"
                        + "  JOIN {0}_issues.people assigned ON assigned.id = i.assigned_to"
                        + "  JOIN {0}_issues.people submitted ON submitted.id = i.submitted_by"
                        + " WHERE i.id = ?", repository)
                + FIXED_ISSUES_ONLY
                + ORDER_BY_SUBMITTED_ON;

        COUNT_COMMENTS_OF_FILE_PAIR_BY_DATE
                = QueryUtils.getQueryForDatabase(
                        "SELECT COUNT(DISTINCT(comments.id))"
                        + FROM_TABLE
                        + "  JOIN {0}_issues.comments comments ON comments.issue_id = i.id"
                        + WHERE, repository)
                + FILTER_BY_MAX_FILES_IN_COMMIT
                + FILTER_BY_ISSUE_FIX_DATE
                + FIXED_ISSUES_ONLY;

        SUM_ADD_DEL_LINES_OF_FILE_PAIR_BY_DATE
                = QueryUtils.getQueryForDatabase(""
                        + "SELECT COALESCE(SUM(com.added_lines), 0),"
                        + "       COALESCE(SUM(com.removed_lines), 0)"
                        + "  FROM {0}_vcs.commits_files_lines filcl"
                        + " WHERE (filcl.path = ?"
                        + "    OR filcl.path = ?)"
                        + "   AND filcl.commit IN ("
                        + "       SELECT DISTINCT s.id"
                        + "         FROM {0}_issues.issues i"
                        + "         JOIN {0}_issues.issues_fix_version ifv ON ifv.issue_id = i.id"
                        + "         JOIN {0}_issues.changes c ON c.issue_id = i.id"
                        + "         JOIN {0}_issues.issues_scmlog i2s ON i2s.issue_id = i.id"
                        + "         JOIN {0}_vcs.scmlog s ON s.id = i2s.scmlog_id"
                        + "         JOIN {0}.commits com ON com.commit_id = i2s.scmlog_id AND com.file_id <> com2.file_id"
                        + "      WHERE com.date > i.submitted_on"
                        + "        AND s.num_files <= " + maxFilePerCommit
                        + FILTER_BY_ISSUE_FIX_DATE
                        + FIXED_ISSUES_ONLY
                        + ")", repository);

        SUM_ADD_DEL_LINES_OF_FILE_PAIR_BY_FIX_VERSION
                = QueryUtils.getQueryForDatabase(""
                        + "SELECT COALESCE(SUM(filcl.added), 0),"
                        + "       COALESCE(SUM(filcl.removed), 0)"
                        + "  FROM {0}_vcs.commits_files_lines filcl"
                        + " WHERE (filcl.path = ? OR filcl.path = ?)"
                        + "   AND filcl.commit IN ("
                        + "       SELECT DISTINCT(s.id)"
                        + "         FROM {0}_issues.issues i"
                        + "         JOIN {0}_issues.issues_fix_version ifv ON ifv.issue_id = i.id"
                        + "         JOIN {0}_issues.changes c ON c.issue_id = i.id"
                        + "         JOIN {0}_issues.issues_scmlog i2s ON i2s.issue_id = i.id"
                        + "         JOIN {0}_vcs.scmlog s ON s.id = i2s.scmlog_id"
                        + "         JOIN {0}.commits com ON com.commit_id = i2s.scmlog_id"
                        + "      WHERE com.date > i.submitted_on"
                        + " AND s.num_files <= " + maxFilePerCommit
                        + FILTER_BY_ISSUE_FIX_MAJOR_VERSION
                        + FIXED_ISSUES_ONLY
                        + ")", repository);

        SUM_CUMMULATIVE_ADD_DEL_LINES_OF_FILE_PAIR_BY_FIX_VERSION
                = QueryUtils.getQueryForDatabase(""
                        + "SELECT COALESCE(SUM(com.added_lines), 0),"
                        + "       COALESCE(SUM(com.removed_lines), 0)"
                        + "  FROM {0}.commits com"
                        + " WHERE (com.file_path = ? OR com.file_path = ?)"
                        + "   AND com.commit_id IN ("
                        + "       SELECT DISTINCT(s.id)"
                        + "         FROM {0}_issues.issues i"
                        + "         JOIN {0}_issues.issues_fix_version ifv ON ifv.issue_id = i.id"
                        + "         JOIN {0}_issues.changes c ON c.issue_id = i.id"
                        + "         JOIN {0}_issues.issues_scmlog i2s ON i2s.issue_id = i.id"
                        + "         JOIN {0}_vcs.scmlog s ON s.id = i2s.scmlog_id"
                        + "      WHERE s.num_files <= " + maxFilePerCommit
                        + "        AND s.date > i.submitted_on"
                        + FILTER_BY_BEFORE_ISSUE_FIX_DATE_OF_ISSUE_ID
                        + FILTER_BY_ISSUE_FIX_MAJOR_VERSION
                        + FIXED_ISSUES_ONLY, repository)
                + "{0})";

        SELECT_COMMITTERS_OF_PAIR_FILE_BY_ISSUE_CREATION_DATE
                = QueryUtils.getQueryForDatabase(
                        "SELECT DISTINCT(p.name)"
                        + FROM_TABLE
                        + "  JOIN {0}_vcs.people p ON p.id = s.committer_id"
                        + WHERE, repository)
                + FILTER_BY_MAX_FILES_IN_COMMIT;

        SELECT_COMMITTERS_OF_PAIR_FILE_BY_ISSUE_ID
                = QueryUtils.getQueryForDatabase(
                        "SELECT DISTINCT(p.name)"
                        + FROM_TABLE
                        + "  JOIN {0}_vcs.people p ON p.id = s.committer_id"
                        + WHERE, repository)
                + FILTER_BY_ISSUE_ID
                + FIXED_ISSUES_ONLY;

        SELECT_LAST_COMMITTER_OF_PAIR_FILE
                = QueryUtils.getQueryForDatabase(
                        "SELECT p.name"
                        + FROM_TABLE
                        + "  JOIN {0}_vcs.people p ON p.id = s.committer_id"
                        + WHERE
                        + "   AND i.id <> ?", repository)
                + FILTER_BY_ISSUE_FIX_MAJOR_VERSION
                + FIXED_ISSUES_ONLY
                + " ORDER BY com.date DESC"
                + " LIMIT 1";

        SELECT_COMMITTERS_OF_PAIR_FILE
                = QueryUtils.getQueryForDatabase(
                        "SELECT DISTINCT(p.name)"
                        + FROM_TABLE
                        + "  JOIN {0}_vcs.people p ON p.id = s.committer_id"
                        + WHERE, repository)
                + FIXED_ISSUES_ONLY;

        COUNT_PAIR_FILE_COMMITS
                = QueryUtils.getQueryForDatabase(
                        "SELECT COUNT(DISTINCT(s.id))"
                        + FROM_TABLE
                        + "  JOIN {0}_vcs.people p ON p.id = s.committer_id"
                        + WHERE, repository)
                + FILTER_BY_MAX_FILES_IN_COMMIT;

        COUNT_PAIR_FILE_COMMITTERS
                = QueryUtils.getQueryForDatabase(
                        "SELECT COUNT(DISTINCT(COALESCE(p.name, p.email)))"
                        + FROM_TABLE
                        + "  JOIN {0}_vcs.people p ON p.id = s.committer_id"
                        + WHERE, repository)
                + FILTER_BY_MAX_FILES_IN_COMMIT;

        COUNT_COMMENTERS_BY_ISSUE_ID
                = QueryUtils.getQueryForDatabase(
                        "SELECT COUNT(DISTINCT(COALESCE(p.name, p.email)))"
                        + "  FROM {0}_issues.issues i"
                        + "  JOIN {0}_issues.changes c ON c.issue_id = i.id"
                        + "  JOIN {0}_issues.comments comments ON comments.issue_id = i.id"
                        + "  JOIN {0}_issues.people p ON p.id = c.submitted_by"
                        + " WHERE i.id = ?", repository);

        SELECT_RELEASE_MIN_MAX_COMMIT_DATE
                = QueryUtils.getQueryForDatabase(
                        "SELECT MIN(com.date), MAX(com.date)"
                        + FROM_TABLE
                        + WHERE, repository)
                + FILTER_BY_MAX_FILES_IN_COMMIT;

        SELECT_RELEASE_MIN_MAX_COMMIT_DATE_BY_FIX_VERSION
                = QueryUtils.getQueryForDatabase(
                        "SELECT MIN(com.date), MAX(com.date)"
                        + FROM_TABLE
                        + WHERE, repository)
                + FILTER_BY_MAX_FILES_IN_COMMIT;

        SELECT_COMMENTS_BY_ISSUE_ID = QueryUtils.getQueryForDatabase(
                "SELECT comments.text FROM {0}_issues.comments comments WHERE comments.issue_id = ?", repository);

        COUNT_ISSUES_TYPES
                = QueryUtils.getQueryForDatabase(""
                        + "SELECT COALESCE(COUNT(DISTINCT(i.id)), 0) AS count, i.type"
                        + FROM_TABLE
                        + WHERE
                        + FILTER_BY_MAX_FILES_IN_COMMIT
                        + FILTER_BY_ISSUE_FIX_MAJOR_VERSION
                        + " GROUP BY i.type", repository);
    }

    public long calculeNumberOfIssues(
            String file, String file2, Date beginDate, Date endDate, boolean onlyFixed) {
        return calculeNumberOfIssues(file, file2, beginDate, endDate, null, onlyFixed);
    }

    public long calculeNumberOfIssues(
            String file, String file2, Date beginDate, Date endDate, String type, boolean onlyFixed) {
        List<Object> selectParams = new ArrayList<>();

        StringBuilder sql = new StringBuilder();
        sql.append(COUNT_ISSUES_BY_FILE_NAME);
        selectParams.add(file);
        selectParams.add(file2);

        if (onlyFixed) {
            sql.append(FIXED_ISSUES_ONLY);
        }

        if (type != null) {
            sql.append(FILTER_BY_ISSUE_TYPE);
            selectParams.add(type);
        }

        sql.append(FILTER_BY_ISSUE_FIX_DATE);
        selectParams.add(beginDate);
        selectParams.add(endDate);

        Long count = (Long) dao.selectNativeOneWithParams(sql.toString(),
                selectParams.toArray());

        return count != null ? count : 0l;
    }

    public long calculeNumberOfIssues(
            String file, String file2, String fixVersion) {
        return calculeNumberOfIssues(file, file2, fixVersion, null);
    }

    public long calculeNumberOfIssues(
            String file, String file2, String fixVersion, String type) {

        List<Object> selectParams = new ArrayList<>();

        StringBuilder sql = new StringBuilder();
        sql.append(COUNT_ISSUES_BY_FILE_NAME);
        selectParams.add(file);
        selectParams.add(file2);
        sql.append(FIXED_ISSUES_ONLY);

        if (type != null) {
            sql.append(FILTER_BY_ISSUE_TYPE);
            selectParams.add(type);
        }

        sql.append(FILTER_BY_ISSUE_FIX_MAJOR_VERSION);
        selectParams.add(fixVersion);

        Long count = (Long) dao.selectNativeOneWithParams(sql.toString(),
                selectParams.toArray());

        return count != null ? count : 0l;
    }

    public List<Integer> selectIssues(
            String file, String file2, Date beginDate, Date endDate, String type) {

        List<Object> selectParams = new ArrayList<>();

        StringBuilder sql = new StringBuilder();
        sql.append(SELECT_ISSUES_BY_FILE_NAME);
        selectParams.add(file);
        selectParams.add(file2);

        if (type != null) {
            sql.append(FILTER_BY_ISSUE_TYPE);
            selectParams.add(type);
        }

        sql.append(FILTER_BY_ISSUE_FIX_DATE);
        selectParams.add(beginDate);
        selectParams.add(endDate);

        List<Integer> issues = dao.selectNativeWithParams(sql.toString(), selectParams.toArray());

        return issues;
    }

    public List<Integer> selectIssues(
            String file, String file2, String version, String type) {

        List<Object> selectParams = new ArrayList<>();

        StringBuilder sql = new StringBuilder();
        sql.append(SELECT_ISSUES_BY_FILE_NAME);
        selectParams.add(file);
        selectParams.add(file2);

        if (type != null) {
            sql.append(FILTER_BY_ISSUE_TYPE);
            selectParams.add(type);
        }

        sql.append(FILTER_BY_ISSUE_FIX_MAJOR_VERSION);
        selectParams.add(version);

        List<Integer> issues = dao.selectNativeWithParams(sql.toString(), selectParams.toArray());

        return issues;
    }

    public long calculeComments(
            String file, String file2, Date beginDate, Date endDate) {
        return calculeComments(null, file, file2, beginDate, endDate, null);
    }

    public long calculeComments(
            String file, String file2, Date beginDate, Date endDate, boolean onlyFixed) {
        return calculeComments(null, file, file2, beginDate, endDate, null);
    }

    public long calculeComments(
            String file, String file2, Date beginDate, Date endDate, Collection<Integer> issues) {
        return calculeComments(null, file, file2, beginDate, endDate, issues);
    }

    public long calculeComments(Integer issueId,
            String file, String file2, Date beginDate, Date endDate,
            Collection<Integer> issues) {

        List<Object> selectParams = new ArrayList<>();

        StringBuilder sql = new StringBuilder();
        sql.append(COUNT_COMMENTS_OF_FILE_PAIR_BY_DATE);

        selectParams.add(file);
        selectParams.add(file2);
        selectParams.add(beginDate);
        selectParams.add(endDate);

        if (issueId != null) {
            sql.append(FILTER_BY_ISSUE_ID);
            selectParams.add(issueId);
        }

        filterByIssues(issues, sql);

        Long count = dao.selectNativeOneWithParams(sql.toString(), selectParams.toArray());

        return count != null ? count : 0l;
    }

    public AuxCodeChurn calculeCodeChurnAddDelChange(
            String fileName, String fileName2, Date beginDate, Date endDate) {
        return calculeCodeChurnAddDelChange(fileName, fileName2, beginDate, endDate, null);
    }

    public AuxCodeChurn calculeCodeChurnAddDelChange(
            String fileName, String fileName2, Date beginDate, Date endDate, Collection<Integer> issues) {

        Object[] params = new Object[]{
            fileName,
            fileName2,
            beginDate,
            endDate
        };

        StringBuilder sql = new StringBuilder(SUM_ADD_DEL_LINES_OF_FILE_PAIR_BY_DATE);
        filterByIssues(issues, sql);

        List<Object[]> sum = dao.selectNativeWithParams(sql.toString(), params);

        Long additions = sum.get(0)[0] == null ? 0l : ((BigDecimal) sum.get(0)[0]).longValue();
        Long deletions = sum.get(0)[1] == null ? 0l : ((BigDecimal) sum.get(0)[1]).longValue();

        return new AuxCodeChurn(fileName, fileName2,
                additions, deletions);
    }

    public AuxCodeChurn calculeCodeChurnAddDelChange(
            String fileName, String fileName2, String fixVersion) {
        return calculeCodeChurnAddDelChange(fileName, fileName2, fixVersion, null);
    }

    public AuxCodeChurn calculeCodeChurnAddDelChange(
            String fileName, String fileName2, String fixVersion, Collection<Integer> issues) {

        Object[] params = new Object[]{
            fileName,
            fileName2,
            fixVersion
        };

        StringBuilder sql = new StringBuilder(SUM_ADD_DEL_LINES_OF_FILE_PAIR_BY_DATE);
        filterByIssues(issues, sql);

        List<Object[]> sum = dao.selectNativeWithParams(sql.toString(), params);

        Long additions = sum.get(0)[0] == null ? 0l : ((BigDecimal) sum.get(0)[0]).longValue();
        Long deletions = sum.get(0)[1] == null ? 0l : ((BigDecimal) sum.get(0)[1]).longValue();

        return new AuxCodeChurn(fileName, fileName2,
                additions, deletions);
    }

    public AuxCodeChurn calculeCummulativeCodeChurnAddDelChange(String fileName2, String fileName, Integer issue, Collection<Integer> issues, String fixVersion) {
        Object[] params = new Object[]{
            fileName,
            fileName2,
            issue,
            fixVersion
        };
        StringBuilder filterByIssues = new StringBuilder();
        filterByIssues(issues, filterByIssues);
        String sql = QueryUtils.getQueryForDatabase(SUM_CUMMULATIVE_ADD_DEL_LINES_OF_FILE_PAIR_BY_FIX_VERSION, filterByIssues.toString());
        List<Object[]> sum = dao.selectNativeWithParams(sql, params);

        Long additions = sum.get(0)[0] == null ? 0l : ((BigDecimal) sum.get(0)[0]).longValue();
        Long deletions = sum.get(0)[1] == null ? 0l : ((BigDecimal) sum.get(0)[1]).longValue();

        return new AuxCodeChurn(fileName, fileName2,
                additions, deletions);
    }

    public Set<AuxUser> selectCommitters(
            String file, String file2, Date beginDate, Date endDate) {
        return selectCommitters(file, file2, beginDate, endDate, null, true);
    }

    public Set<AuxUser> selectCommitters(
            String file, String file2, Date beginDate, Date endDate, Collection<Integer> issues) {
        return selectCommitters(file, file2, beginDate, endDate, issues, true);
    }

    public Set<AuxUser> selectCommitters(
            String file, String file2, Date beginDate, Date endDate,
            Collection<Integer> issues, boolean onlyFixed) {

        List<Object> selectParams = new ArrayList<>();
        StringBuilder sql = new StringBuilder();
        sql.append(SELECT_COMMITTERS_OF_PAIR_FILE_BY_ISSUE_CREATION_DATE);

        selectParams.add(file);
        selectParams.add(file2);

        if (onlyFixed) {
            sql.append(FIXED_ISSUES_ONLY);
        }

        if (beginDate != null) {
            sql.append(FILTER_BY_AFTER_ISSUE_FIX_DATE);
            selectParams.add(beginDate);
        }

        if (endDate != null) {
            sql.append(FILTER_BY_BEFORE_ISSUE_FIX_DATE);
            selectParams.add(endDate);
        }

        filterByIssues(issues, sql);

        List<String> committers = dao.selectNativeWithParams(
                sql.toString(), selectParams.toArray());

        Set<AuxUser> commitersList = new HashSet<>(committers.size());
        for (String name : committers) {
            commitersList.add(new AuxUser(name));
        }

        return commitersList;
    }

    public Set<AuxUser> selectCommitters(
            String file, String file2, String fixVersion) {
        return selectCommitters(file, file2, fixVersion, null, true);
    }

    public Set<AuxUser> selectCommitters(
            String file, String file2, String fixVersion, Collection<Integer> issues) {
        return selectCommitters(file, file2, fixVersion, issues, true);
    }

    public Set<AuxUser> selectCommitters(
            String file, String file2, String fixVersion,
            Collection<Integer> issues, boolean onlyFixed) {

        List<Object> selectParams = new ArrayList<>();

        StringBuilder sql = new StringBuilder();
        sql.append(SELECT_COMMITTERS_OF_PAIR_FILE_BY_ISSUE_CREATION_DATE);

        selectParams.add(file);
        selectParams.add(file2);

        if (onlyFixed) {
            sql.append(FIXED_ISSUES_ONLY);
        }

        sql.append(FILTER_BY_ISSUE_FIX_MAJOR_VERSION);
        selectParams.add(fixVersion);

        filterByIssues(issues, sql);

        List<String> committers = dao.selectNativeWithParams(
                sql.toString(), selectParams.toArray());

        Set<AuxUser> commitersList = new HashSet<>(committers.size());
        for (String name : committers) {
            commitersList.add(new AuxUser(name));
        }

        return commitersList;
    }

    public Set<AuxUser> selectCommitters(Integer issueId,
            String file, String file2) {

        List<Object> selectParams = new ArrayList<>();

        selectParams.add(file);
        selectParams.add(file2);
        selectParams.add(issueId);

        List<String> committers = dao.selectNativeWithParams(
                SELECT_COMMITTERS_OF_PAIR_FILE_BY_ISSUE_ID, selectParams.toArray());

        Set<AuxUser> commitersList = new HashSet<>(committers.size());
        for (String name : committers) {
            commitersList.add(new AuxUser(name));
        }

        return commitersList;
    }

    public Set<AuxUser> selectCommitters(Collection<Integer> issues,
            String file, String file2) {

        List<Object> selectParams = new ArrayList<>();

        selectParams.add(file);
        selectParams.add(file2);

        StringBuilder sql = new StringBuilder(SELECT_COMMITTERS_OF_PAIR_FILE);

        filterByIssues(issues, sql);

        List<String> committers = dao.selectNativeWithParams(
                sql.toString(), selectParams.toArray());

        Set<AuxUser> commitersList = new HashSet<>(committers.size());
        for (String name : committers) {
            commitersList.add(new AuxUser(name));
        }

        return commitersList;
    }

    public AuxUser selectLastCommitter(String file1,
            String file2, Integer issue) {

        List<Object> selectParams = new ArrayList<>();

        selectParams.add(file1);
        selectParams.add(file2);
        selectParams.add(issue);

        String committer = dao.selectNativeOneWithParams(
                SELECT_LAST_COMMITTER_OF_PAIR_FILE, selectParams.toArray());

        return new AuxUser(committer);
    }

    public long calculeCommits(String file, String file2) {
        return calculeCommits(file, file2, null, null, null, null, true);
    }

    public long calculeCommits(String file, String file2, Date beginDate, Date endDate) {
        return calculeCommits(file, file2, null, beginDate, endDate, null, true);
    }

    public long calculeCommits(String file, String file2, Date beginDate, Date endDate,
            Collection<Integer> issues) {
        return calculeCommits(file, file2, null, beginDate, endDate, issues, true);
    }

    public long calculeCommits(String file, String file2, Date beginDate, Date endDate,
            boolean onlyFixed) {
        return calculeCommits(file, file2, null, beginDate, endDate, null, onlyFixed);
    }

    public long calculeCommits(String file, String file2, String user, Date beginDate, Date endDate) {
        return calculeCommits(file, file2, user, beginDate, endDate, null, true);
    }

    public long calculeCommits(String file, String file2, String user,
            Date beginDate, Date endDate, Collection<Integer> issues) {
        return calculeCommits(file, file2, user, beginDate, endDate, issues, true);
    }

    public long calculeCommits(
            String file, String file2, String user, Date beginDate, Date endDate,
            Collection<Integer> issues, boolean onlyFixed) {
        List<Object> selectParams = new ArrayList<>();

        StringBuilder sql = new StringBuilder();
        sql.append(COUNT_PAIR_FILE_COMMITS);

        selectParams.add(file);
        selectParams.add(file2);

        if (onlyFixed) {
            sql.append(FIXED_ISSUES_ONLY);
        }

        if (beginDate != null && endDate != null) {
            sql.append(FILTER_BY_ISSUE_FIX_DATE);
            selectParams.add(beginDate);
            selectParams.add(endDate);
        }

        if (user != null) {
            sql.append(FILTER_BY_USER_NAME);
            selectParams.add(user);
        }

        filterByIssues(issues, sql);

        Long count = dao.selectNativeOneWithParams(sql.toString(), selectParams.toArray());

        return count != null ? count : 0l;
    }

    public long calculeCommits(String file, String file2, String fixVersion) {
        return calculeCommitsByFixVersion(file, file2, null, null, fixVersion, null, true);
    }

    public long calculeCommits(String file, String file2, Integer issue, String fixVersion) {
        return calculeCommitsByFixVersion(file, file2, null, issue, fixVersion, null, true);
    }

    public long calculeCommits(String file, String file2, String fixVersion,
            Collection<Integer> issues) {
        return calculeCommitsByFixVersion(file, file2, null, null, fixVersion, issues, true);
    }

    public long calculeCommits(String file, String file2, String fixVersion,
            boolean onlyFixed) {
        return calculeCommitsByFixVersion(file, file2, null, null, fixVersion, null, onlyFixed);
    }

    public long calculeCommits(String file, String file2, String user, String fixVersion) {
        return calculeCommitsByFixVersion(file, file2, user, null, fixVersion, null, true);
    }

    public long calculeCommits(String file, String file2, String user,
            String fixVersion, Collection<Integer> issues) {
        return calculeCommitsByFixVersion(file, file2, user, null, fixVersion, issues, true);
    }

    public long calculeCommitsByFixVersion(
            String file, String file2, String user, Integer issue, String fixVersion,
            Collection<Integer> issues, boolean onlyFixed) {
        List<Object> selectParams = new ArrayList<>();

        StringBuilder sql = new StringBuilder();
        sql.append(COUNT_PAIR_FILE_COMMITS);

        selectParams.add(file);
        selectParams.add(file2);

        if (onlyFixed) {
            sql.append(FIXED_ISSUES_ONLY);
        }

        sql.append(FILTER_BY_ISSUE_FIX_MAJOR_VERSION);
        selectParams.add(fixVersion);

        if (user != null) {
            sql.append(FILTER_BY_USER_NAME);
            selectParams.add(user);
        }

        filterByIssues(issues, sql);

        if (issue != null) {
            sql.append(FILTER_BY_BEFORE_ISSUE_FIX_DATE_OF_ISSUE_ID);
            selectParams.add(issue);
        }

        Long count = dao.selectNativeOneWithParams(sql.toString(), selectParams.toArray());

        return count != null ? count : 0l;
    }

    public long calculePastCommitsByFixVersion(
            String file, String file2, String fixVersion) {
        List<Object> selectParams = new ArrayList<>();

        StringBuilder sql = new StringBuilder();
        sql.append(COUNT_PAIR_FILE_COMMITS);

        selectParams.add(file);
        selectParams.add(file2);

        sql.append(FIXED_ISSUES_ONLY);

        sql.append(FILTER_BY_BEFORE_ISSUE_FIX_MAJOR_VERSION_EXCLUSIVE);
        selectParams.add(fixVersion);

        Long count = dao.selectNativeOneWithParams(sql.toString(), selectParams.toArray());

        return count != null ? count : 0l;
    }

    public long calculeCummulativeCommits(
            String file, String file2, String fixVersion) {
        List<Object> selectParams = new ArrayList<>();

        StringBuilder sql = new StringBuilder();
        sql.append(COUNT_PAIR_FILE_COMMITS);

        selectParams.add(file);
        selectParams.add(file2);

        sql.append(FIXED_ISSUES_ONLY);

        sql.append(FILTER_BY_BEFORE_ISSUE_FIX_MAJOR_VERSION);
        selectParams.add(fixVersion);

        Long count = dao.selectNativeOneWithParams(sql.toString(), selectParams.toArray());

        return count != null ? count : 0l;
    }

    public long calculeCommitters(
            String file, String file2) {
        return calculeCommitters(file, file2, null, null, null, true);
    }

    public long calculeCommitters(
            String file, String file2, Date beginDate, Date endDate) {
        return calculeCommitters(file, file2, beginDate, endDate, null, true);
    }

    public long calculeCommitters(
            String file, String file2, Collection<Integer> issues) {
        return calculeCommitters(file, file2, null, null, issues, true);
    }

    public long calculeCommitters(
            String file, String file2, Date beginDate, Date endDate,
            Collection<Integer> issues) {
        return calculeCommitters(file, file2, beginDate, endDate, issues, true);
    }

    public long calculeCommitters(
            String file, String file2, Date beginDate, Date endDate,
            Collection<Integer> issues, boolean onlyFixed) {
        List<Object> selectParams = new ArrayList<>();

        StringBuilder sql = new StringBuilder();
        sql.append(COUNT_PAIR_FILE_COMMITTERS);

        selectParams.add(file);
        selectParams.add(file2);

        if (onlyFixed) {
            sql.append(FIXED_ISSUES_ONLY);
        }

        if (beginDate != null) {
            sql.append(FILTER_BY_AFTER_ISSUE_FIX_DATE);
            selectParams.add(beginDate);
        }

        if (endDate != null) {
            sql.append(FILTER_BY_BEFORE_ISSUE_FIX_DATE);
            selectParams.add(endDate);
        }

        filterByIssues(issues, sql);

        Long count = dao.selectNativeOneWithParams(sql.toString(), selectParams.toArray());

        return count != null ? count : 0l;
    }

    public long calculeCommitters(
            String file, String file2, String fixVersion,
            Collection<Integer> issues, boolean onlyFixed) {
        List<Object> selectParams = new ArrayList<>();

        StringBuilder sql = new StringBuilder();
        sql.append(COUNT_PAIR_FILE_COMMITTERS);

        selectParams.add(file);
        selectParams.add(file2);

        if (onlyFixed) {
            sql.append(FIXED_ISSUES_ONLY);
        }

        sql.append(FILTER_BY_ISSUE_FIX_MAJOR_VERSION);
        selectParams.add(fixVersion);

        filterByIssues(issues, sql);

        Long count = dao.selectNativeOneWithParams(sql.toString(), selectParams.toArray());

        return count != null ? count : 0l;
    }

    public long calculeCummulativeCommitters(
            String file, String file2, String fixVersions) {
        return calculeCummulativeCommitters(file, file2, null, fixVersions, null);
    }

    public long calculeCummulativeCommitters(
            String file, String file2, Integer issue, String fixVersions) {
        return calculeCummulativeCommitters(file, file2, issue, fixVersions, null);
    }

    public long calculeCummulativeCommitters(
            String file, String file2, String fixVersion,
            Collection<Integer> issues) {
        return calculeCummulativeCommitters(file, file2, null, fixVersion, issues);
    }

    public long calculeCummulativeCommitters(
            String file, String file2, Integer issue, String fixVersion,
            Collection<Integer> issues) {
        List<Object> selectParams = new ArrayList<>();

        StringBuilder sql = new StringBuilder();
        sql.append(COUNT_PAIR_FILE_COMMITTERS);
        sql.append(FIXED_ISSUES_ONLY);
        sql.append(FILTER_BY_BEFORE_ISSUE_FIX_MAJOR_VERSION);

        selectParams.add(file);
        selectParams.add(file2);
        selectParams.add(fixVersion);

        if (issues != null) {
            filterByIssues(issues, sql);
        }

        if (issue != null) {
            sql.append(FILTER_BY_BEFORE_ISSUE_FIX_DATE_OF_ISSUE_ID);
            selectParams.add(issue);
        }

        Long count = dao.selectNativeOneWithParams(sql.toString(), selectParams.toArray());

        return count != null ? count : 0l;
    }

    public long calculePastCommitters(
            String file, String file2, String fixVersions) {
        return calculePastCommitters(file, file2, fixVersions, null);
    }

    public long calculePastCommitters(
            String file, String file2, String fixVersion,
            Collection<Integer> issues) {
        List<Object> selectParams = new ArrayList<>();

        StringBuilder sql = new StringBuilder();
        sql.append(COUNT_PAIR_FILE_COMMITTERS);
        sql.append(FIXED_ISSUES_ONLY);
        sql.append(FILTER_BY_BEFORE_ISSUE_FIX_MAJOR_VERSION_EXCLUSIVE);

        selectParams.add(file);
        selectParams.add(file2);
        selectParams.add(fixVersion);

        if (issues != null) {
            filterByIssues(issues, sql);
        }

        Long count = dao.selectNativeOneWithParams(sql.toString(), selectParams.toArray());

        return count != null ? count : 0l;
    }

    public long calculeCommitters(Integer issueId,
            String file, String file2) {
        return calculeCommitters(issueId, null, null, true);
    }

    public long calculeCommitters(Integer issueId,
            Date beginDate, Date endDate) {
        return calculeCommitters(issueId, beginDate, endDate, true);
    }

    public long calculeCommitters(Integer issueId,
            Date beginDate, Date endDate, boolean onlyFixed) {
        List<Object> selectParams = new ArrayList<>();

        StringBuilder sql = new StringBuilder();
        sql.append(COUNT_COMMENTERS_BY_ISSUE_ID);

        selectParams.add(issueId);

        if (onlyFixed) {
            sql.append(FIXED_ISSUES_ONLY);
        }

        if (beginDate != null) {
            sql.append(FILTER_BY_AFTER_ISSUE_FIX_DATE);
            selectParams.add(beginDate);
        }

        if (endDate != null) {
            sql.append(FILTER_BY_BEFORE_ISSUE_FIX_DATE);
            selectParams.add(endDate);
        }

        Long count = dao.selectNativeOneWithParams(sql.toString(), selectParams.toArray());

        return count != null ? count : 0l;
    }

    public Collection<IssueMetrics> listIssues(String file, String file2,
            Date beginDate, Date endDate, Collection<Integer> issues) {
        List<Object> selectParams = new ArrayList<>();

        StringBuilder sql = new StringBuilder();
        sql.append(SELECT_ISSUES_OF_FILE_PAIR_BY_DATE);

        selectParams.add(file);
        selectParams.add(file2);
        selectParams.add(beginDate);
        selectParams.add(endDate);

        filterByIssues(issues, sql);

        List<Object[]> rawIssues = dao.selectNativeWithParams(sql.toString(), selectParams.toArray());

        Map<Integer, IssueMetrics> cache = new HashMap<>(rawIssues.size());
        for (Object[] issue : rawIssues) {
            Integer issueNumber = (Integer) issue[0];

            List<String> comments = dao.selectNativeWithParams(SELECT_COMMENTS_BY_ISSUE_ID, new Object[]{issueNumber});
            IssueMetrics comment = new IssueMetrics(
                    issueNumber,
                    (String) issue[1],
                    (String) issue[2],
                    comments);
            cache.put(issueNumber, comment);
        }

        return cache.values();
    }

    public Collection<IssueMetrics> listIssues(String file, String file2,
            String fixVersion, Collection<Integer> issues) {
        List<Object> selectParams = new ArrayList<>();

        StringBuilder sql = new StringBuilder();
        sql.append(SELECT_ISSUES_OF_FILE_PAIR_BY_FIX_VERSION);

        selectParams.add(file);
        selectParams.add(file2);
        selectParams.add(fixVersion);

        filterByIssues(issues, sql);

        List<Object[]> rawIssues = dao.selectNativeWithParams(sql.toString(), selectParams.toArray());

        Map<Integer, IssueMetrics> cache = new HashMap<>(rawIssues.size());
        for (Object[] issue : rawIssues) {
            Integer issueNumber = (Integer) issue[0];

            List<String> comments = dao.selectNativeWithParams(SELECT_COMMENTS_BY_ISSUE_ID, new Object[]{issueNumber});
            IssueMetrics comment = new IssueMetrics(
                    issueNumber,
                    (String) issue[1],
                    (String) issue[2],
                    comments);
            cache.put(issueNumber, comment);
        }

        return cache.values();
    }

    public IssueMetrics listIssues(Integer issue) {
        Object[] rawIssues = dao.selectNativeOneWithParams(SELECT_ISSUES_BY_ISSUES_ID, new Object[]{issue});

        Integer issueNumber = (Integer) rawIssues[0];

        List<String> comments = dao.selectNativeWithParams(SELECT_COMMENTS_BY_ISSUE_ID, new Object[]{issueNumber});
        IssueMetrics issueWithComments = new IssueMetrics(
                issueNumber,
                (String) rawIssues[1], // iej.issue_key
                (String) rawIssues[2], // i.url
                (String) rawIssues[3], // i.body
                (String) rawIssues[4], // i.type
                (String) rawIssues[5], // i.priority
                (String) rawIssues[6], // assigned.user_id
                (String) rawIssues[7], // submitted.user_id
                (Integer) rawIssues[8], // i.num_watchers
                (Integer) rawIssues[9], // i.num_reopened
                comments,
                (Integer) rawIssues[10], // i.num_commenters
                (Integer) rawIssues[11], // i.num_dev_commenters
                (Timestamp) rawIssues[12], // i.submitted_on
                (Timestamp) rawIssues[13] // c.changed_on where value = "Fixed"
        );

        return issueWithComments;
    }

    public int calculeTotalPairFileDaysAge(String file, String file2, Date endDate, boolean onlyFixed) {
        return calculePairFileDaysAge(file, file2, null, endDate, onlyFixed);
    }

    public int calculePairFileDaysAge(String file, String file2, Date beginDate, Date endDate, boolean onlyFixed) {
        List<Object> selectParams = new ArrayList<>();

        StringBuilder sql = new StringBuilder();
        sql.append(SELECT_RELEASE_MIN_MAX_COMMIT_DATE);

        if (onlyFixed) {
            sql.append(FIXED_ISSUES_ONLY);
        }

        selectParams.add(file);
        selectParams.add(file2);

        if (beginDate != null) {
            sql.append(FILTER_BY_AFTER_ISSUE_FIX_DATE);
            selectParams.add(beginDate);
        }

        if (endDate != null) {
            sql.append(FILTER_BY_BEFORE_ISSUE_FIX_DATE);
            selectParams.add(endDate);
        }

        List<Object[]> minMaxDateList = dao.selectNativeWithParams(sql.toString(), selectParams.toArray());
        Object[] minMaxDate = minMaxDateList.get(0);

        if (minMaxDate[0] == null || minMaxDate[1] == null) {
            return 0;
        }

        java.sql.Timestamp minDate = (java.sql.Timestamp) minMaxDate[0];
        java.sql.Timestamp maxDate = (java.sql.Timestamp) minMaxDate[1];

        LocalDate createdAt = new LocalDate(minDate.getTime());
        LocalDate finalDate = new LocalDate(maxDate.getTime());
        Days age = Days.daysBetween(createdAt, finalDate);

        return age.getDays();
    }

    public int calculeTotalPairFilesAgeInDay(String file, String file2, String fixVersion, boolean onlyFixed) {
        List<Object> selectParams = new ArrayList<>();

        StringBuilder sql = new StringBuilder();
        sql.append(SELECT_RELEASE_MIN_MAX_COMMIT_DATE_BY_FIX_VERSION);

        if (onlyFixed) {
            sql.append(FIXED_ISSUES_ONLY);
        }

        selectParams.add(file);
        selectParams.add(file2);

        sql.append(FILTER_BY_BEFORE_ISSUE_FIX_MAJOR_VERSION);
        selectParams.add(fixVersion);

        List<Object[]> minMaxDateList = dao.selectNativeWithParams(sql.toString(), selectParams.toArray());
        Object[] minMaxDate = minMaxDateList.get(0);

        if (minMaxDate[0] == null || minMaxDate[1] == null) {
            return 0;
        }

        java.sql.Timestamp minDate = (java.sql.Timestamp) minMaxDate[0];
        java.sql.Timestamp maxDate = (java.sql.Timestamp) minMaxDate[1];

        LocalDate createdAt = new LocalDate(minDate.getTime());
        LocalDate finalDate = new LocalDate(maxDate.getTime());
        Days age = Days.daysBetween(createdAt, finalDate);

        return age.getDays();
    }

    public int calculePairFileAgeInDays(String file, String file2, String fixVersion, boolean onlyFixed) {
        List<Object> selectParams = new ArrayList<>();

        StringBuilder sql = new StringBuilder();
        sql.append(SELECT_RELEASE_MIN_MAX_COMMIT_DATE_BY_FIX_VERSION);

        if (onlyFixed) {
            sql.append(FIXED_ISSUES_ONLY);
        }

        selectParams.add(file);
        selectParams.add(file2);

        sql.append(FILTER_BY_ISSUE_FIX_MAJOR_VERSION);
        selectParams.add(fixVersion);

        List<Object[]> minMaxDateList = dao.selectNativeWithParams(sql.toString(), selectParams.toArray());
        Object[] minMaxDate = minMaxDateList.get(0);

        if (minMaxDate[0] == null || minMaxDate[1] == null) {
            return 0;
        }

        java.sql.Timestamp minDate = (java.sql.Timestamp) minMaxDate[0];
        java.sql.Timestamp maxDate = (java.sql.Timestamp) minMaxDate[1];

        LocalDate createdAt = new LocalDate(minDate.getTime());
        LocalDate finalDate = new LocalDate(maxDate.getTime());
        Days age = Days.daysBetween(createdAt, finalDate);

        return age.getDays();
    }

    public Map<String, Long> countIssuesTypes(String file1, String file2, String futureVersion) {
        List<Object> params = new ArrayList<>();
        params.add(file1);
        params.add(file2);
        params.add(futureVersion);

        List<Object[]> rawFilesPath
                = dao.selectNativeWithParams(COUNT_ISSUES_TYPES, params.toArray());

        Map<String, Long> result = new HashMap<>(rawFilesPath.size());
        for (Object[] row : rawFilesPath) {
            Long count = (Long) row[0];
            String type = (String) row[1];
            result.put(type, count);
        }
        return result;
    }
}
