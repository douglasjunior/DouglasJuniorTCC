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

    // order
    private final String ORDER_BY_FIX_DATE;

    // limiting
    private final String LIMIT_OFFSET = " LIMIT ? OFFSET ?";

    // queries
    private final String COUNT_FILES_PER_COMMITS;
    private final String SELECT_ALL_FIXED_ISSUES;
    private final String SELECT_ALL_FIXED_ISSUES_LIMIT_OFFSET;
    private final String SELECT_ISSUES_BY_FIXED_DATE;
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

        ORDER_BY_FIX_DATE
                = " ORDER BY i.fixed_on";

        COUNT_FILES_PER_COMMITS = QueryUtils.getQueryForDatabase("SELECT s.id, s.num_files"
                + "  FROM {0}_issues.issues_scmlog i2s"
                + "  JOIN {0}_issues.issues i ON i.id = i2s.issue_id"
                + "  JOIN {0}_issues.changes c ON c.issue_id = i.id"
                + "  JOIN {0}_vcs.scmlog s ON s.id = i2s.scmlog_id"
                + "   AND s.num_files > 0", repository)
                + FIXED_ISSUES_ONLY
                + FILTER_BY_MAX_FILES_IN_COMMIT;

        SELECT_ALL_FIXED_ISSUES
                = QueryUtils.getQueryForDatabase("SELECT i.id, i.type, s.id"
                        + "  FROM {0}_issues.issues_scmlog i2s"
                        + "  JOIN {0}_issues.issues i ON i.id = i2s.issue_id"
                        + "  JOIN {0}_issues.changes c ON c.issue_id = i.id"
                        + "  JOIN {0}_vcs.scmlog s ON s.id = i2s.scmlog_id"
                        + " WHERE s.date > i.submitted_on"
                        + "   AND s.num_files > 0", repository)
                + FIXED_ISSUES_ONLY
                + FILTER_BY_MAX_FILES_IN_COMMIT;

        SELECT_ALL_FIXED_ISSUES_LIMIT_OFFSET
                = QueryUtils.getQueryForDatabase("SELECT DISTINCT(i.id) "
                        + "    FROM {0}_issues.issues i "
                        + "    JOIN {0}_issues.issues_scmlog i2s ON i2s.issue_id = i.id"
                        + "    JOIN {0}_vcs.scmlog s ON s.id = i2s.scmlog_id"
                        + "   WHERE i.fixed_on IS NOT NULL"
                        + " WHERE s.date > i.submitted_on"
                        + "   AND s.num_files > 0", repository)
                + FIXED_ISSUES_ONLY
                + FILTER_BY_MAX_FILES_IN_COMMIT
                + ORDER_BY_FIX_DATE
                + "   LIMIT ? OFFSET ?";

        SELECT_ISSUES_BY_FIXED_DATE
                = QueryUtils.getQueryForDatabase("SELECT i.id, s.id"
                        + "  FROM {0}_issues.issues_scmlog i2s"
                        + "  JOIN {0}_issues.issues i ON i.id = i2s.issue_id"
                        + "  JOIN {0}_issues.changes c ON c.issue_id = i.id"
                        + "  JOIN {0}_vcs.scmlog s ON s.id = i2s.scmlog_id"
                        + " WHERE c.changed_on BETWEEN ? AND ?"
                        + "   AND s.date > i.submitted_on"
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
                        + "   AND s.num_files > 0", repository)
                + FIXED_ISSUES_ONLY
                + FILTER_BY_MAX_FILES_IN_COMMIT;

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
                        + "   AND com2.date > i.submitted_on", repository)
                + FILTER_BY_MAX_FILES_IN_COMMIT;

        SELECT_COMMIT_AND_FILES_BY_ISSUE
                = QueryUtils.getQueryForDatabase(
                        "SELECT DISTINCT com.commit_id, com.file_path, p.id, p.name, p.email"
                        + "  FROM {0}_issues.issues i"
                        + "  JOIN {0}_issues.changes c ON c.issue_id = i.id"
                        + "  JOIN {0}_issues.issues_scmlog i2s ON i2s.issue_id = i.id"
                        + "  JOIN {0}_vcs.scmlog s ON s.id = i2s.scmlog_id"
                        + "  JOIN {0}.commits com ON com.commit_id = i2s.scmlog_id"
                        + "  JOIN {0}_vcs.people p ON p.id = com.committer_id"
                        + " WHERE 1 = 1", repository)
                + FILTER_BY_ISSUE_ID
                + FILTER_BY_MAX_FILES_IN_COMMIT;

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

    public List<Map<Issue, List<Integer>>> selectAllIssuesAndTypeSubdividedBy(Integer size) {
        List<Object> selectParams = new ArrayList<>();

        StringBuilder sql = new StringBuilder();
        sql.append(SELECT_ALL_FIXED_ISSUES);

        sql.append(ORDER_BY_FIX_DATE);

        Integer offset = 0;
        sql.append(LIMIT_OFFSET);
        selectParams.add(size);
        selectParams.add(offset);

        final Object[] params = selectParams.toArray();
        List<Map<Issue, List<Integer>>> dividedIssuesCommits = new ArrayList<>();
        List<Object[]> rawIssues = dao.selectNativeWithParams(sql.toString(), params);

        while (!rawIssues.isEmpty() && rawIssues.get(0)[0] != null) {
            Map<Issue, List<Integer>> issuesCommits = new LinkedHashMap<>();
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
            dividedIssuesCommits.add(issuesCommits);

            offset += size;
            params[1] = offset;
            rawIssues = dao.selectNativeWithParams(sql.toString(), params);
        }
        return dividedIssuesCommits;
    }

    public List<Integer> selectIssuesAndType(Integer size, Integer index) {
        List<Object> selectParams = new ArrayList<>();

        StringBuilder sql = new StringBuilder();
        sql.append(SELECT_ALL_FIXED_ISSUES_LIMIT_OFFSET);

        Integer offset = size * index;
        selectParams.add(size);
        selectParams.add(offset);

        final Object[] params = selectParams.toArray();
        List<Integer> issues = dao.selectNativeWithParams(sql.toString(), params);

        return issues;
    }

    public Map<Issue, List<Integer>> selectIssuesAndType(String version) {
        List<Object> selectParams = new ArrayList<>();

        StringBuilder sql = new StringBuilder();
        sql.append(SELECT_ISSUES_AND_TYPE_BY_FIXED_MAJOR_VERSION);

        selectParams.add(version);

        List<Object[]> rawIssues = dao.selectNativeWithParams(sql.toString(), selectParams.toArray());

        Map<Issue, List<Integer>> issuesCommits = new LinkedHashMap<>(rawIssues.size());
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

            Committer committer = new Committer(committerId, committerName, committerEmail);

            Commit commit = new Commit(commitId, committer);

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
}
