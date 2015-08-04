package br.edu.utfpr.cm.JGitMinerWeb.dao;

import static br.edu.utfpr.cm.JGitMinerWeb.dao.QueryUtils.filterByIssues;
import br.edu.utfpr.cm.minerador.services.matrix.model.Commenter;
import br.edu.utfpr.cm.minerador.services.matrix.model.Issue;
import br.edu.utfpr.cm.minerador.services.metric.committer.Committer;
import br.edu.utfpr.cm.minerador.services.metric.model.Commit;
import br.edu.utfpr.cm.minerador.services.metric.model.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author Rodrigo T. Kuroda
 */
public class BichoDAO {

    // filters
    private final String FILTER_BY_MAX_FILES_IN_COMMIT;
    private final String FIXED_ISSUES_ONLY;
    private final String FILTER_BY_ISSUE_FIX_MAJOR_VERSION;
    private final String FILTER_BY_ISSUE_FIX_DATE;
    private final String FILTER_BY_ISSUE_ID;
    private final String FILTER_BY_ONLY_REOPENED_ISSUES;

    // order
    private final String ORDER_BY_FIX_DATE;
    private final String ORDER_BY_COMMIT_DATE;

    // limiting
    private final String LIMIT_OFFSET = " LIMIT ? OFFSET ?";

    // queries
    private final String COUNT_FILES_PER_COMMITS;
    private final String SELECT_ALL_FIXED_ISSUES;
    private final String SELECT_ALL_FIXED_ISSUES_LIMIT_OFFSET;
    private final String SELECT_ISSUES_BY_FIXED_DATE;
    private final String SELECT_ISSUES_AND_TYPE;
    private final String SELECT_ISSUES_AND_TYPE_BY_FIXED_MAJOR_VERSION;
    private final String SELECT_ISSUES_AND_TYPE_BY_FIXED_DATE;
    private final String SELECT_COMMENTERS_BY_ISSUE_ORDER_BY_SUBMIT;
    private final String SELECT_COMMENTERS;
    private final String SELECT_FIX_VERSION_ORDERED;
    private final String COUNT_ISSUES_TYPES_BEGIN;
    private final String COUNT_ISSUES_TYPES_END;
    private final String COUNT_ISSUES;
    private final String SELECT_COMMIT_AND_FILES_BY_ISSUE;
    private final String SELECT_PAST_MAJOR_VERSION;
    private final String SELECT_FUTURE_MAJOR_VERSION;
    private final String SELECT_ISSUE_REOPENED_AND_FIXED_DATE;

    private final String ISSUE_BY_LIMIT_OFFSET_ORDER_BY_FIX_DATE;

    private final GenericBichoDAO dao;

    public BichoDAO(GenericBichoDAO dao, String repository, Integer maxFilesPerCommit) {
        this.dao = dao;

        if (repository == null) {
            throw new IllegalArgumentException("The parameters 'repository' and 'maxFilePerCommit' can not be null.");
        }

        // filters
        FILTER_BY_MAX_FILES_IN_COMMIT
                = " AND s.num_files <= " + maxFilesPerCommit;

        // avoid join, because has poor performance in this case
        FILTER_BY_ISSUE_FIX_MAJOR_VERSION
                = QueryUtils.getQueryForDatabase(
                        " AND i.id IN ("
                        + " SELECT ifv.issue_id "
                        + "   FROM {0}_issues.issues_fix_version ifv "
                        + "  WHERE ifv.major_fix_version = ?)", repository);

        FILTER_BY_ISSUE_FIX_DATE
                = " AND c.changed_on BETWEEN ? AND ?";

        FIXED_ISSUES_ONLY
                = " AND i.resolution = \"Fixed\""
                + " AND c.field = \"Resolution\""
                + " AND c.new_value = i.resolution";

        FILTER_BY_ISSUE_ID
                = " AND i.id = ?";

        FILTER_BY_ONLY_REOPENED_ISSUES
                = " AND i.reopened_times > 0";

        ORDER_BY_FIX_DATE
                = " ORDER BY i.fixed_on ASC";

        ORDER_BY_COMMIT_DATE
                = " ORDER BY com.date ASC";

        COUNT_FILES_PER_COMMITS = QueryUtils.getQueryForDatabase("SELECT s.id, s.num_files"
                + "  FROM {0}_issues.issues_scmlog i2s"
                + "  JOIN {0}_issues.issues i ON i.id = i2s.issue_id"
                + "  JOIN {0}_issues.changes c ON c.issue_id = i.id"
                + "  JOIN {0}_vcs.scmlog s ON s.id = i2s.scmlog_id"
                + " WHERE s.date > i.submitted_on"
                + "   AND s.date < i.fixed_on"
                + "   AND s.num_files > 0", repository)
                + FIXED_ISSUES_ONLY
                + FILTER_BY_MAX_FILES_IN_COMMIT;

        ISSUE_BY_LIMIT_OFFSET_ORDER_BY_FIX_DATE
                = " INNER JOIN "
                + " (SELECT DISTINCT i2.id "
                + "    FROM {0}_issues.issues i2 "
                + "    JOIN {0}_issues.changes c2 ON c2.issue_id = i2.id"
                + "    JOIN {0}_issues.issues_scmlog i2s2 ON i2s2.issue_id = i2.id"
                + "    JOIN {0}_vcs.scmlog s2 ON s2.id = i2s2.scmlog_id"
                + "   WHERE i2.fixed_on IS NOT NULL"
                + "     AND s2.date > i2.submitted_on"
                + "     AND s2.date < i2.fixed_on"
                + "     AND i2.resolution = \"Fixed\""
                + "     AND c2.field = \"Resolution\""
                + "     AND c2.new_value = i2.resolution"
                + "     AND s2.num_files <= " + maxFilesPerCommit
                + "     AND s2.num_files > 0 "
                + "   ORDER BY i2.fixed_on "
                + "   LIMIT ? OFFSET ?) AS i3 ON i3.id = i.id";

        SELECT_ALL_FIXED_ISSUES
                = QueryUtils.getQueryForDatabase("SELECT DISTINCT i.id, i.type, i.fixed_on, s.id, s.date"
                        + "  FROM {0}_issues.issues_scmlog i2s"
                        + "  JOIN {0}_issues.issues i ON i.id = i2s.issue_id"
                        + "  JOIN {0}_vcs.scmlog s ON s.id = i2s.scmlog_id"
                        + ISSUE_BY_LIMIT_OFFSET_ORDER_BY_FIX_DATE
                        + " WHERE s.date > i.submitted_on"
                        + "   AND s.date < i.fixed_on"
                        + "   AND s.num_files > 0", repository);

        SELECT_ALL_FIXED_ISSUES_LIMIT_OFFSET
                = QueryUtils.getQueryForDatabase("SELECT DISTINCT(i.id) "
                        + "    FROM {0}_issues.issues i "
                        + "    JOIN {0}_issues.changes c ON c.issue_id = i.id"
                        + "    JOIN {0}_issues.issues_scmlog i2s ON i2s.issue_id = i.id"
                        + "    JOIN {0}_vcs.scmlog s ON s.id = i2s.scmlog_id"
                        + ISSUE_BY_LIMIT_OFFSET_ORDER_BY_FIX_DATE
                        + " WHERE s.date > i.submitted_on"
                        + "   AND s.date < i.fixed_on"
                        + "   AND s.num_files > 0", repository)
                + FIXED_ISSUES_ONLY
                + FILTER_BY_MAX_FILES_IN_COMMIT
                + ORDER_BY_FIX_DATE;

        SELECT_ISSUES_BY_FIXED_DATE
                = QueryUtils.getQueryForDatabase("SELECT i.id, s.id"
                        + "  FROM {0}_issues.issues_scmlog i2s"
                        + "  JOIN {0}_issues.issues i ON i.id = i2s.issue_id"
                        + "  JOIN {0}_issues.changes c ON c.issue_id = i.id"
                        + "  JOIN {0}_vcs.scmlog s ON s.id = i2s.scmlog_id"
                        + " WHERE c.changed_on BETWEEN ? AND ?"
                        + "   AND s.date > i.submitted_on"
                        + "   AND s.date < i.fixed_on"
                        + "   AND s.num_files > 0", repository)
                + FIXED_ISSUES_ONLY
                + FILTER_BY_MAX_FILES_IN_COMMIT;

        SELECT_ISSUES_AND_TYPE_BY_FIXED_DATE
                = QueryUtils.getQueryForDatabase("SELECT i.id, i.type, s.id"
                        + "  FROM {0}_issues.issues_scmlog i2s"
                        + "  JOIN {0}_issues.issues i ON i.id = i2s.issue_id"
                        + "  JOIN {0}_issues.changes c ON c.issue_id = i.id"
                        + "  JOIN {0}_vcs.scmlog s ON s.id = i2s.scmlog_id"
                        + " WHERE c.changed_on BETWEEN ? AND ?"
                        + "   AND s.date > i.submitted_on"
                        + "   AND s.date < i.fixed_on"
                        + "   AND s.num_files > 0", repository)
                + FIXED_ISSUES_ONLY
                + FILTER_BY_MAX_FILES_IN_COMMIT;

        SELECT_ISSUES_AND_TYPE
                = QueryUtils.getQueryForDatabase("SELECT DISTINCT i.id, i.type, i.fixed_on, s.id, s.date"
                        + "  FROM {0}_issues.issues_scmlog i2s"
                        + "  JOIN {0}_issues.issues i ON i.id = i2s.issue_id"
                        + "  JOIN {0}_issues.issues_fix_version ifv ON ifv.issue_id = i2s.issue_id"
                        + "  JOIN {0}_issues.changes c ON c.issue_id = i.id"
                        + "  JOIN {0}_vcs.scmlog s ON s.id = i2s.scmlog_id"
                        + " WHERE s.date > i.submitted_on"
                        + "   AND s.date < i.fixed_on"
                        + "   AND s.num_files > 0", repository)
                + FIXED_ISSUES_ONLY
                + FILTER_BY_MAX_FILES_IN_COMMIT;

        SELECT_ISSUES_AND_TYPE_BY_FIXED_MAJOR_VERSION
                = QueryUtils.getQueryForDatabase("SELECT DISTINCT i.id, i.type, i.fixed_on, s.id, s.date"
                        + "  FROM {0}_issues.issues_scmlog i2s"
                        + "  JOIN {0}_issues.issues i ON i.id = i2s.issue_id"
                        + "  JOIN {0}_issues.issues_fix_version ifv ON ifv.issue_id = i2s.issue_id"
                        + "  JOIN {0}_issues.changes c ON c.issue_id = i.id"
                        + "  JOIN {0}_vcs.scmlog s ON s.id = i2s.scmlog_id"
                        + " WHERE ifv.major_fix_version = ?"
                        + "   AND s.date > i.submitted_on"
                        + "   AND s.date < i.fixed_on"
                        + "   AND s.num_files > 0", repository)
                + FIXED_ISSUES_ONLY
                + FILTER_BY_MAX_FILES_IN_COMMIT;

        SELECT_COMMENTERS_BY_ISSUE_ORDER_BY_SUBMIT
                = QueryUtils.getQueryForDatabase("SELECT p.id, p.user_id, p.email, p.is_dev"
                        + "  FROM {0}_issues.comments c"
                        + "  JOIN {0}_issues.people p ON p.id = c.submitted_by"
                        + " WHERE c.issue_id = ?"
                        + " ORDER BY c.submitted_on ASC", repository);

        SELECT_COMMENTERS
                = QueryUtils.getQueryForDatabase("SELECT p.id, p.user_id, p.email, p.is_dev"
                        + "  FROM {0}_issues.comments c"
                        + "  JOIN {0}_issues.issues i ON i.id = c.issue_id"
                        + "  JOIN {0}_issues.people p ON p.id = c.submitted_by"
                        // ignore continuous integration user
                        + " WHERE UPPER(p.user_id) <> ?", repository);

        SELECT_FIX_VERSION_ORDERED
                = QueryUtils.getQueryForDatabase(
                        "SELECT ifvo.major_fix_version"
                        + " FROM {0}_issues.issues_fix_version_order ifvo"
                        + " ORDER BY ifvo.version_order", repository);

        COUNT_ISSUES_TYPES_BEGIN
                = QueryUtils.getQueryForDatabase("SELECT"
                        + "     (SELECT COALESCE(COUNT(1), 0)"
                        + "         FROM {0}_issues.issues i"
                        + "         WHERE i.type = i2.type ", repository);
        COUNT_ISSUES_TYPES_END
                = QueryUtils.getQueryForDatabase(") AS count,"
                        + "     i2.type"
                        + "  FROM {0}_issues.issues i2"
                        + " GROUP BY i2.type", repository);

        COUNT_ISSUES
                = QueryUtils.getQueryForDatabase(
                        "SELECT COUNT(DISTINCT(i.id))"
                        + "  FROM {0}_issues.issues i"
                        + "  JOIN {0}_issues.changes c ON c.issue_id = i.id"
                        + "  JOIN {0}_issues.issues_scmlog i2s ON i2s.issue_id = i.id"
                        + "  JOIN {0}_vcs.scmlog s ON s.id = i2s.scmlog_id"
                        + "  JOIN {0}_issues.issues_scmlog i2s2 ON i2s2.issue_id = i.id"
                        + "  JOIN {0}_vcs.scmlog s2 ON s2.id = i2s2.scmlog_id"
                        + "  JOIN {0}.commits com ON com.commit_id = i2s.scmlog_id"
                        + "  JOIN {0}.commits com2 ON com2.commit_id = i2s2.scmlog_id AND com.file_id <> com2.file_id"
                        + " WHERE com.date > i.submitted_on"
                        + "   AND com2.date > i.submitted_on"
                        + "   AND com.date < i.fixed_on"
                        + "   AND com2.date < i.fixed_on"
                        , repository)
                + FILTER_BY_MAX_FILES_IN_COMMIT;

        SELECT_COMMIT_AND_FILES_BY_ISSUE
                = QueryUtils.getQueryForDatabase(
                        "SELECT DISTINCT com.commit_id, com.file_path, p.id, p.name, p.email, com.date"
                        + "  FROM {0}_issues.issues i"
                        + "  JOIN {0}_issues.changes c ON c.issue_id = i.id"
                        + "  JOIN {0}_issues.issues_scmlog i2s ON i2s.issue_id = i.id"
                        + "  JOIN {0}_vcs.scmlog s ON s.id = i2s.scmlog_id"
                        + "  JOIN {0}.commits com ON com.commit_id = i2s.scmlog_id"
                        + "  JOIN {0}_vcs.people p ON p.id = com.committer_id"
                        + " WHERE 1 = 1", repository)
                + FILTER_BY_ISSUE_ID
                + FILTER_BY_MAX_FILES_IN_COMMIT
                + ORDER_BY_COMMIT_DATE;

        SELECT_PAST_MAJOR_VERSION
                = QueryUtils.getQueryForDatabase(
                        "SELECT ifvo.major_fix_version"
                        + "  FROM {0}_issues.issues_fix_version_order ifvo"
                        + " WHERE ifvo.version_order = "
                        + "((SELECT MIN(ifvo2.version_order)"
                        + "   FROM {0}_issues.issues_fix_version_order ifvo2"
                        + "  WHERE ifvo2.major_fix_version = ?) - 1)", repository);

        SELECT_FUTURE_MAJOR_VERSION
                = QueryUtils.getQueryForDatabase(
                        "SELECT ifvo.major_fix_version"
                        + "  FROM {0}_issues.issues_fix_version_order ifvo"
                        + " WHERE ifvo.version_order = "
                        + "((SELECT MAX(ifvo2.version_order)"
                        + "   FROM {0}_issues.issues_fix_version_order ifvo2"
                        + "  WHERE ifvo2.major_fix_version = ?) + 1)", repository);

        SELECT_ISSUE_REOPENED_AND_FIXED_DATE
                = QueryUtils.getQueryForDatabase(
                        "SELECT DISTINCT i.submitted_on, c.changed_on "
                        + "  FROM {0}_issues.issues i"
                        + "  JOIN {0}_issues.issues_ext_jira iej ON iej.issue_id = i.id"
                        + "  JOIN {0}_issues.changes c ON c.issue_id = i.id"
                        + "  JOIN {0}_issues.changes c2 ON c2.issue_id = iej.issue_id AND c2.changed_on = c.changed_on"
                        + " WHERE i.id = ?"
                        + "  AND ("
                        + "        (c.field = \"Resolution\" AND c.old_value = \"Fixed\" AND c.new_value = \"\" AND c2.field = \"Status\" AND"
                        + "        (c2.old_value = \"Resolved\" OR c2.old_value = \"Closed\") AND c2.new_value = \"Reopened\")"
                        + "      OR"
                        + "        (c.field = \"Resolution\" AND c.old_value = \"\" AND c.new_value = \"Fixed\" AND c2.field = \"Status\" AND "
                        // + "        (c2.old_value = \"Reopened\" OR c2.old_value = \"Open\") AND"
                        + "         (c2.new_value = \"Resolved\" OR c2.new_value = \"Closed\"))"
                        + "      )" // reopened date
                        + " ORDER BY c.changed_on", repository);

    }

    public long calculeNumberOfIssues(Date beginDate, Date endDate, boolean onlyFixed) {

        List<Object> selectParams = new ArrayList<>();

        StringBuilder sql = new StringBuilder();
        sql.append(COUNT_ISSUES);

        if (onlyFixed) {
            sql.append(FIXED_ISSUES_ONLY);
        }

        sql.append(FILTER_BY_ISSUE_FIX_DATE);
        selectParams.add(beginDate);
        selectParams.add(endDate);

        Long count = (Long) dao.selectNativeOneWithParams(sql.toString(),
                selectParams.toArray());

        return count != null ? count : 0l;
    }

    public long calculeNumberOfIssues(String version, boolean onlyFixed) {

        List<Object> selectParams = new ArrayList<>();

        StringBuilder sql = new StringBuilder();
        sql.append(COUNT_ISSUES);

        if (onlyFixed) {
            sql.append(FIXED_ISSUES_ONLY);
        }

        sql.append(FILTER_BY_ISSUE_FIX_MAJOR_VERSION);
        selectParams.add(version);

        Long count = (Long) dao.selectNativeOneWithParams(sql.toString(),
                selectParams.toArray());

        return count != null ? count : 0l;
    }

    public long calculeNumberOfIssues() {

        List<Object> selectParams = new ArrayList<>();

        StringBuilder sql = new StringBuilder();
        sql.append(COUNT_ISSUES);
        sql.append(FIXED_ISSUES_ONLY);

        Long count = (Long) dao.selectNativeOneWithParams(sql.toString(),
                selectParams.toArray());

        return count != null ? count : 0l;
    }

    public Map<Integer, Integer> countFilesPerCommit(Date beginDate, Date endDate) {
        List<Object> selectParams = new ArrayList<>();

        selectParams.add(beginDate);
        selectParams.add(endDate);

        List<Object[]> rawFilesPerCommit = dao.selectNativeWithParams(COUNT_FILES_PER_COMMITS, selectParams.toArray());

        Map<Integer, Integer> numFilesPerCommit = new HashMap<>(rawFilesPerCommit.size());
        for (Object[] objects : rawFilesPerCommit) {
            Integer commitId = (Integer) objects[0];
            Integer numFiles = (Integer) objects[1];
            numFilesPerCommit.put(commitId, numFiles);
        }

        return numFilesPerCommit;
    }

    public Map<Integer, Set<Integer>> selectIssues(Date beginDate, Date endDate) {
        List<Object> selectParams = new ArrayList<>();

        StringBuilder sql = new StringBuilder();
        sql.append(SELECT_ISSUES_BY_FIXED_DATE);

        selectParams.add(beginDate);
        selectParams.add(endDate);

        sql.append(ORDER_BY_FIX_DATE);

        List<Object[]> rawIssues = dao.selectNativeWithParams(sql.toString(), selectParams.toArray());

        Map<Integer, Set<Integer>> issuesCommits = new HashMap<>();
        for (Object[] issueCommit : rawIssues) {
            Integer issue = (Integer) issueCommit[0];
            Integer commit = (Integer) issueCommit[1];

            if (issuesCommits.containsKey(issue)) {
                issuesCommits.get(issue).add(commit);
            } else {
                Set<Integer> commits = new LinkedHashSet<>();
                commits.add(commit);
                issuesCommits.put(issue, commits);
            }
        }
        return issuesCommits;
    }

    public List<Map<Issue, List<Commit>>> selectAllIssuesAndTypeSubdividedBy(long size) {
        List<Object> selectParams = new ArrayList<>();

        StringBuilder sql = new StringBuilder();
        sql.append(SELECT_ALL_FIXED_ISSUES);

        long offset = 0;
        selectParams.add(size);
        selectParams.add(offset);

        final Object[] params = selectParams.toArray();
        List<Map<Issue, List<Commit>>> dividedIssuesCommits = new ArrayList<>();
        List<Object[]> rawIssues = dao.selectNativeWithParams(sql.toString(), params);

        while (!rawIssues.isEmpty() && rawIssues.get(0)[0] != null) {
            Map<Issue, List<Commit>> issuesCommits = rawIssuesAndCommitsToMap(rawIssues);
            dividedIssuesCommits.add(issuesCommits);

            System.out.println("Size " + issuesCommits.size() + " offset " + offset);
            offset += size;
            params[1] = offset;
            rawIssues = dao.selectNativeWithParams(sql.toString(), params);
        }
        System.out.println("Total divided by " + size + " = " + dividedIssuesCommits.size());
        return dividedIssuesCommits;
    }

    public Set<Integer> selectIssuesAndType(Integer size, Integer index) {
        List<Object> selectParams = new ArrayList<>();

        StringBuilder sql = new StringBuilder();
        sql.append(SELECT_ALL_FIXED_ISSUES_LIMIT_OFFSET);

        Integer offset = size * index;
        selectParams.add(size);
        selectParams.add(offset);

        final Object[] params = selectParams.toArray();
        List<Integer> issues = dao.selectNativeWithParams(sql.toString(), params);

        return new LinkedHashSet<>(issues);
    }

    public Map<Issue, List<Commit>> selectIssuesAndType(String version) {
        List<Object> selectParams = new ArrayList<>();

        StringBuilder sql = new StringBuilder();
        sql.append(SELECT_ISSUES_AND_TYPE_BY_FIXED_MAJOR_VERSION);

        selectParams.add(version);

        List<Object[]> rawIssues = dao.selectNativeWithParams(sql.toString(), selectParams.toArray());

        return rawIssuesAndCommitsToMap(rawIssues);
    }

    public Map<Issue, List<Commit>> selectIssuesAndType() {
        List<Object> selectParams = new ArrayList<>();

        StringBuilder sql = new StringBuilder();
        sql.append(SELECT_ISSUES_AND_TYPE);

        List<Object[]> rawIssues = dao.selectNativeWithParams(sql.toString(), selectParams.toArray());

        return rawIssuesAndCommitsToMap(rawIssues);
    }

    public Map<Issue, List<Commit>> selectReopenedIssuesAndType(String version) {
        List<Object> selectParams = new ArrayList<>();

        StringBuilder sql = new StringBuilder();
        sql.append(SELECT_ISSUES_AND_TYPE_BY_FIXED_MAJOR_VERSION);
        sql.append(FILTER_BY_ONLY_REOPENED_ISSUES);
        selectParams.add(version);

        List<Object[]> rawIssues = dao.selectNativeWithParams(sql.toString(), selectParams.toArray());

        return rawIssuesAndCommitsToMap(rawIssues);
    }

    private Map<Issue, List<Commit>> rawIssuesAndCommitsToMap(List<Object[]> rawIssues) {
        Map<Issue, List<Commit>> issuesCommits = new LinkedHashMap<>(rawIssues.size());
        for (Object[] issueCommit : rawIssues) {
            Integer issueId = (Integer) issueCommit[0];
            String issueType = (String) issueCommit[1];
            java.sql.Timestamp fixedOn = (java.sql.Timestamp) issueCommit[2];
            Integer commit = (Integer) issueCommit[3];
            Date commitDate = (java.sql.Timestamp) issueCommit[4];

            Issue issue = new Issue(issueId, issueType, fixedOn);
            if (issuesCommits.containsKey(issue)) {
                issuesCommits.get(issue).add(new Commit(commit, null, commitDate));
            } else {
                List<Commit> commits = new ArrayList<>();
                commits.add(new Commit(commit, null, commitDate));
                issuesCommits.put(issue, commits);
            }
        }
        return issuesCommits;
    }

    public Map<Issue, List<Integer>> selectIssuesAndType(Date beginDate, Date endDate) {
        List<Object> selectParams = new ArrayList<>();

        StringBuilder sql = new StringBuilder();
        sql.append(SELECT_ISSUES_AND_TYPE_BY_FIXED_DATE);

        selectParams.add(beginDate);
        selectParams.add(endDate);

        List<Object[]> rawIssues = dao.selectNativeWithParams(sql.toString(), selectParams.toArray());

        Map<Issue, List<Integer>> issuesCommits = new HashMap<>();
        for (Object[] issueCommit : rawIssues) {
            Integer issueId = (Integer) issueCommit[0];
            String issueType = (String) issueCommit[1];
            Integer commit = (Integer) issueCommit[2];

            Issue issue = new Issue(issueId, issueType);
            if (issuesCommits.containsKey(issue)) {
                issuesCommits.get(issue).add(commit);
            } else {
                List<Integer> commits = new ArrayList<>();
                commits.add(commit);
                issuesCommits.put(issue, commits);
            }
        }
        return issuesCommits;
    }

    public List<Commenter> selectCommentersByIssueOrderBySubmissionDate(Integer issue) {
        List<Object[]> rawFilesPath
                = dao.selectNativeWithParams(SELECT_COMMENTERS_BY_ISSUE_ORDER_BY_SUBMIT,
                        new Object[]{issue});

        List<Commenter> files = new ArrayList<>();
        for (Object[] row : rawFilesPath) {
            Integer id = (Integer) row[0];
            String userId = (String) row[1];
            String email = (String) row[2];
            boolean isDev = ((Integer) row[3]) == 1;
            Commenter commenter = new Commenter(id, userId, email, isDev);
            files.add(commenter);
        }

        return files;
    }

    public List<Commenter> selectCommentersByIssuesOrderBySubmissionDate(Collection<Integer> issue) {
        StringBuilder sql = new StringBuilder(SELECT_COMMENTERS);

        filterByIssues(issue, sql);

        sql.append(" ORDER BY c.submitted_on ASC");

        List<Object[]> rawFilesPath
                = dao.selectNativeWithParams(sql.toString(), new Object[]{"HUDSON"});

        List<Commenter> files = new ArrayList<>();
        for (Object[] row : rawFilesPath) {
            Integer id = (Integer) row[0];
            String userId = (String) row[1];
            String email = (String) row[2];
            boolean isDev = ((Integer) row[3]) == 1;
            Commenter commenter = new Commenter(id, userId, email, isDev);
            files.add(commenter);
        }

        return files;
    }

    public List<String> selectFixVersionOrdered() {
        List<String> versionsOrdered
                = dao.selectNativeWithParams(SELECT_FIX_VERSION_ORDERED, new Object[0]);

        return versionsOrdered;
    }

    public Map<String, Long> countIssuesTypes(Set<Integer> fileFileIssues) {
        StringBuilder sql = new StringBuilder(COUNT_ISSUES_TYPES_BEGIN);

        filterByIssues(fileFileIssues, sql);

        sql.append(COUNT_ISSUES_TYPES_END);
        List<Object[]> rawFilesPath
                = dao.selectNativeWithParams(sql.toString(), new Object[0]);

        Map<String, Long> result = new HashMap<>(rawFilesPath.size());
        for (Object[] row : rawFilesPath) {
            Long count = (Long) row[0];
            String type = (String) row[1];
            result.put(type, count);
        }
        return result;
    }

    public Set<Commit> selectFilesAndCommitByIssue(Integer issue) {
        List<Object[]> rawFilesPath
                = dao.selectNativeWithParams(SELECT_COMMIT_AND_FILES_BY_ISSUE, new Object[]{issue});

        Map<Commit, Commit> commits = new LinkedHashMap<>();

        for (Object[] row : rawFilesPath) {

            Integer commitId = (Integer) row[0];
            String fileName = (String) row[1];
            Integer committerId = (Integer) row[2];
            String committerName = (String) row[3];
            String committerEmail = (String) row[4];
            java.sql.Timestamp commitDate = (java.sql.Timestamp) row[5];

            Committer committer = new Committer(committerId, committerName, committerEmail);

            Commit commit = new Commit(commitId, committer, new Date(commitDate.getTime()));

            if (commits.containsKey(commit)) {
                commits.get(commit).getFiles().add(new File(fileName));
            } else {
                commit.getFiles().add(new File(fileName));
                commits.put(commit, commit);
            }
        }

        return commits.keySet();
    }

    public String selectPastMajorVersion(String version) {
        String pastMajorVersion
                = dao.selectNativeOneWithParams(SELECT_PAST_MAJOR_VERSION, new Object[]{version});
        return pastMajorVersion;
    }

    public String selectFutureMajorVersion(String version) {
        String futureMajorVersion
                = dao.selectNativeOneWithParams(SELECT_FUTURE_MAJOR_VERSION, new Object[]{version});
        return futureMajorVersion;
    }

    /**
     * Seleciona data de abertura e da 1a correcao + data de reabertura e
     * correcao para cada vez que a issue foi reaberta
     *
     * @param issue
     * @return
     */
    public List<Date[]> selectIssueOpenedPeriod(Integer issue) {

        List<Object[]> issueReopenedFixedTimestamp
                = dao.selectNativeWithParams(SELECT_ISSUE_REOPENED_AND_FIXED_DATE, new Object[]{issue});

        List<Date[]> issueReopenedFixedDate = new ArrayList<>();

        if (!issueReopenedFixedTimestamp.isEmpty()) {
            Iterator<Object[]> iterator = issueReopenedFixedTimestamp.iterator();
            Object[] timestamps = iterator.next();
            final long openedTimestamp = ((java.sql.Timestamp) timestamps[0]).getTime();
            final long firstFixedTimestamp = ((java.sql.Timestamp) timestamps[1]).getTime();
            final Date[] openedFixedDate = new Date[]{new Date(openedTimestamp), new Date(firstFixedTimestamp)};
            issueReopenedFixedDate.add(openedFixedDate);

            while (iterator.hasNext()) { // issue has reopened?
                final long reopenedTimestamp = ((java.sql.Timestamp) iterator.next()[1]).getTime();
                try {
                    final long fixedTimestamp = ((java.sql.Timestamp) iterator.next()[1]).getTime();
                    final Date[] reopenedFixedDate = new Date[]{new Date(reopenedTimestamp), new Date(fixedTimestamp)};
                    issueReopenedFixedDate.add(reopenedFixedDate);
                } catch (Exception e) {
                    System.out.println("Erro nas datas da Issue " + issue);
                }
            }
        }
        return issueReopenedFixedDate;
    }

    public List<String> listAllProjects() {
        return dao.selectNativeWithParams(
                "SELECT distinct(replace(replace(schema_name, '_vcs', ''), '_issues', '')) "
                + "  FROM information_schema.schemata "
                + " WHERE schema_name  LIKE '%_vcs'"
                + "    OR schema_name LIKE '%_issues'", new Object[]{});
    }
}
