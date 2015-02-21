package br.edu.utfpr.cm.JGitMinerWeb.dao;

import static br.edu.utfpr.cm.JGitMinerWeb.dao.QueryUtils.filterByIssues;
import br.edu.utfpr.cm.minerador.services.matrix.model.Commenter;
import br.edu.utfpr.cm.minerador.services.matrix.model.Issue;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
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

    // queries
    private final String COUNT_FILES_PER_COMMITS;
    private final String SELECT_ISSUES_BY_FIXED_DATE;
    private final String SELECT_ISSUES_AND_TYPE_BY_FIXED_MAJOR_VERSION;
    private final String SELECT_ISSUES_AND_TYPE_BY_FIXED_DATE;
    private final String SELECT_COMMENTERS_BY_ISSUE_ORDER_BY_SUBMIT;
    private final String SELECT_COMMENTERS;
    private final String SELECT_FIX_VERSION_ORDERED;
    private final String COUNT_ISSUES_TYPES_BEGIN;
    private final String COUNT_ISSUES_TYPES_END;
    private final String COUNT_ISSUES;

    private final GenericBichoDAO dao;

    public BichoDAO(GenericBichoDAO dao, String repository) {
        this.dao = dao;

        if (repository == null) {
            throw new IllegalArgumentException("The parameters 'repository' and 'maxFilePerCommit' can not be null.");
        }

        // filters
        FILTER_BY_MAX_FILES_IN_COMMIT
                = QueryUtils.getQueryForDatabase(
                        " AND s.num_files <= ?", repository);

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

        COUNT_FILES_PER_COMMITS = QueryUtils.getQueryForDatabase("SELECT s.id, s.num_files"
                + "  FROM {0}_issues.issues_scmlog i2s"
                + "  JOIN {0}_issues.issues i ON i.id = i2s.issue_id"
                + "  JOIN {0}_issues.changes c ON c.issue_id = i.id"
                + "  JOIN {0}_vcs.scmlog s ON s.id = i2s.scmlog_id"
                + "   AND s.num_files > 0", repository)
                + FIXED_ISSUES_ONLY;

        SELECT_ISSUES_BY_FIXED_DATE
                = QueryUtils.getQueryForDatabase("SELECT i.id, s.id"
                        + "  FROM {0}_issues.issues_scmlog i2s"
                        + "  JOIN {0}_issues.issues i ON i.id = i2s.issue_id"
                        + "  JOIN {0}_issues.changes c ON c.issue_id = i.id"
                        + "  JOIN {0}_vcs.scmlog s ON s.id = i2s.scmlog_id"
                        + " WHERE c.changed_on BETWEEN ? AND ?"
                        + "   AND s.date > i.submitted_on"
                        + "   AND s.num_files > 0", repository)
                + FIXED_ISSUES_ONLY;

        SELECT_ISSUES_AND_TYPE_BY_FIXED_DATE
                = QueryUtils.getQueryForDatabase("SELECT i.id, i.type, s.id"
                        + "  FROM {0}_issues.issues_scmlog i2s"
                        + "  JOIN {0}_issues.issues i ON i.id = i2s.issue_id"
                        + "  JOIN {0}_issues.changes c ON c.issue_id = i.id"
                        + "  JOIN {0}_vcs.scmlog s ON s.id = i2s.scmlog_id"
                        + " WHERE c.changed_on BETWEEN ? AND ?"
                        + "   AND s.date > i.submitted_on"
                        + "   AND s.num_files > 0", repository)
                + FIXED_ISSUES_ONLY;

        SELECT_ISSUES_AND_TYPE_BY_FIXED_MAJOR_VERSION
                = QueryUtils.getQueryForDatabase("SELECT DISTINCT i.id, i.type, s.id"
                        + "  FROM {0}_issues.issues_scmlog i2s"
                        + "  JOIN {0}_issues.issues i ON i.id = i2s.issue_id"
                        + "  JOIN {0}_issues.issues_fix_version ifv ON ifv.issue_id = i2s.issue_id"
                        + "  JOIN {0}_issues.changes c ON c.issue_id = i.id"
                        + "  JOIN {0}_vcs.scmlog s ON s.id = i2s.scmlog_id"
                        + " WHERE ifv.major_fix_version = ?"
                        + "   AND s.date > i.submitted_on"
                        + "   AND s.num_files > 0", repository)
                + FIXED_ISSUES_ONLY;

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
                        + "   AND com2.date > i.submitted_on", repository)
                + FILTER_BY_MAX_FILES_IN_COMMIT;

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

    public Map<Integer, List<Integer>> selectIssues(Date beginDate, Date endDate, Integer maxFilesPerCommit) {
        List<Object> selectParams = new ArrayList<>();

        StringBuilder sql = new StringBuilder();
        sql.append(SELECT_ISSUES_BY_FIXED_DATE);

        selectParams.add(beginDate);
        selectParams.add(endDate);

        if (maxFilesPerCommit > 0) {
            sql.append(FILTER_BY_MAX_FILES_IN_COMMIT);
            selectParams.add(maxFilesPerCommit);
        }

        List<Object[]> rawIssues = dao.selectNativeWithParams(sql.toString(), selectParams.toArray());

        Map<Integer, List<Integer>> issuesCommits = new HashMap<>();
        for (Object[] issueCommit : rawIssues) {
            Integer issue = (Integer) issueCommit[0];
            Integer commit = (Integer) issueCommit[1];

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

    public Map<Issue, List<Integer>> selectIssuesAndType(String version, Integer maxFilesPerCommit) {
        List<Object> selectParams = new ArrayList<>();

        StringBuilder sql = new StringBuilder();
        sql.append(SELECT_ISSUES_AND_TYPE_BY_FIXED_MAJOR_VERSION);

        selectParams.add(version);

        if (maxFilesPerCommit > 0) {
            sql.append(FILTER_BY_MAX_FILES_IN_COMMIT);
            selectParams.add(maxFilesPerCommit);
        }

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

    public Map<Issue, List<Integer>> selectIssuesAndType(Date beginDate, Date endDate, Integer maxFilesPerCommit) {
        List<Object> selectParams = new ArrayList<>();

        StringBuilder sql = new StringBuilder();
        sql.append(SELECT_ISSUES_AND_TYPE_BY_FIXED_DATE);

        selectParams.add(beginDate);
        selectParams.add(endDate);

        if (maxFilesPerCommit > 0) {
            sql.append(FILTER_BY_MAX_FILES_IN_COMMIT);
            selectParams.add(maxFilesPerCommit);
        }

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

    public List<Commenter> selectCommentersByIssueId(Integer issue) {
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

    public List<Commenter> selectCommentersByIssueId(Collection<Integer> issue) {
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
}
