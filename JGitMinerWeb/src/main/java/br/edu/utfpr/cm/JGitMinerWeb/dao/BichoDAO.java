package br.edu.utfpr.cm.JGitMinerWeb.dao;

import static br.edu.utfpr.cm.JGitMinerWeb.dao.QueryUtils.filterByIssues;
import br.edu.utfpr.cm.minerador.services.matrix.model.Commenter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Rodrigo T. Kuroda
 */
public class BichoDAO {

    // filters
    private final String FILTER_BY_MAX_FILES_IN_COMMIT;
    private final String FIXED_ISSUES_ONLY;

    // queries
    private final String COUNT_FILES_PER_COMMITS;
    private final String SELECT_ISSUES_BY_FIXED_DATE;
    private final String SELECT_COMMENTERS_BY_ISSUE_ORDER_BY_SUBMIT;
    private final String SELECT_COMMENTERS;

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

        FIXED_ISSUES_ONLY
                = " AND i.resolution = 'Fixed'"
                + " AND c.field = 'Resolution'"
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

        SELECT_COMMENTERS_BY_ISSUE_ORDER_BY_SUBMIT
                = QueryUtils.getQueryForDatabase("SELECT p.id, p.name, p.email"
                        + "  FROM {0}_issues.comments c"
                        + "  JOIN {0}_issues.people p ON p.id = c.submitted_by"
                        + " WHERE c.issue_id = ?"
                        + " ORDER BY c.submitted_on ASC", repository);

        SELECT_COMMENTERS
                = QueryUtils.getQueryForDatabase("SELECT p.id, p.name, p.email"
                        + "  FROM {0}_issues.comments c"
                        + "  JOIN {0}_issues.issues i ON i.id = c.issue_id"
                        + "  JOIN {0}_issues.people p ON p.id = c.submitted_by"
                        + " WHERE 1 = 1", repository);
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

    public List<Commenter> selectCommentersByIssueId(Integer issue) {
        List<Object[]> rawFilesPath
                = dao.selectNativeWithParams(SELECT_COMMENTERS_BY_ISSUE_ORDER_BY_SUBMIT,
                        new Object[]{issue});

        List<Commenter> files = new ArrayList<>();
        for (Object[] row : rawFilesPath) {
            Commenter commenter = new Commenter((Integer) row[0], (String) row[1], (String) row[2]);
            files.add(commenter);
        }

        return files;
    }

    public List<Commenter> selectCommentersByIssueId(Collection<Integer> issue) {
        StringBuilder sql = new StringBuilder(SELECT_COMMENTERS);

        filterByIssues(issue, sql);

        sql.append(" ORDER BY c.submitted_on ASC");

        List<Object[]> rawFilesPath
                = dao.selectNativeWithParams(sql.toString(), new Object[0]);

        List<Commenter> files = new ArrayList<>();
        for (Object[] row : rawFilesPath) {
            Commenter commenter = new Commenter((Integer) row[0], (String) row[1], (String) row[2]);
            files.add(commenter);
        }

        return files;
    }
}
