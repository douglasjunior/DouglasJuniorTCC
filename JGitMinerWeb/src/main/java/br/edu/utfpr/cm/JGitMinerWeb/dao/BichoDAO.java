package br.edu.utfpr.cm.JGitMinerWeb.dao;

import br.edu.utfpr.cm.minerador.services.matrix.model.Commenter;
import java.util.ArrayList;
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
    private final String FILTER_BY_ISSUE_CREATION_DATE;
    private final String FILTER_BY_BEFORE_ISSUE_CREATION_DATE;
    private final String FILTER_BY_AFTER_ISSUE_CREATION_DATE;
    private final String FIXED_ISSUES_ONLY;
    private final String FILTER_BY_ISSUES_THAT_HAS_AT_LEAST_ONE_COMMENT;

    // queries
    private final String COUNT_FILES_PER_COMMITS;
    private final String SELECT_ISSUES_BY_CREATION_DATE;
    private final String SELECT_COMMENTERS_BY_ISSUE;

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

        FILTER_BY_ISSUE_CREATION_DATE
                = " AND i.submitted_on BETWEEN ? AND ?";

        FILTER_BY_BEFORE_ISSUE_CREATION_DATE
                = " AND i.submitted_on <= ?";

        FILTER_BY_AFTER_ISSUE_CREATION_DATE
                = " AND i.submitted_on >= ?";

        FIXED_ISSUES_ONLY
                = " AND i.resolution = 'Fixed'";

        FILTER_BY_ISSUES_THAT_HAS_AT_LEAST_ONE_COMMENT
                = QueryUtils.getQueryForDatabase(
                        "AND i.num_comments > 0", repository);

        COUNT_FILES_PER_COMMITS = QueryUtils.getQueryForDatabase("SELECT s.id, s.num_files"
                + "  FROM {0}_issues.issues_scmlog i2s"
                + "  JOIN {0}_issues.issues i ON i.id = i2s.issue_id"
                + "  JOIN {0}_vcs.scmlog s ON s.id = i2s.scmlog_id"
                + "   AND s.num_files > 0", repository);

        SELECT_ISSUES_BY_CREATION_DATE
                = QueryUtils.getQueryForDatabase("SELECT i.id, s.id"
                        + "  FROM {0}_issues.issues_scmlog i2s"
                        + "  JOIN {0}_issues.issues i ON i.id = i2s.issue_id"
                        + "  JOIN {0}_vcs.scmlog s ON s.id = i2s.scmlog_id"
                        + " WHERE i.submitted_on BETWEEN ? AND ?"
                        + "   AND s.num_files > 0", repository);

        SELECT_COMMENTERS_BY_ISSUE
                = QueryUtils.getQueryForDatabase("SELECT p.id, p.name, p.email"
                        + "  FROM {0}_issues.comments c"
                        + "  JOIN {0}_issues.people p ON p.id = c.submitted_by"
                        + " WHERE c.issue_id = ?"
                        + " ORDER BY c.submitted_on ASC", repository);
    }

    public Map<Integer, Integer> countFilesPerCommit(Date beginDate, Date endDate, boolean onlyFixed) {
        List<Object> selectParams = new ArrayList<>();

        StringBuilder sql = new StringBuilder();
        sql.append(COUNT_FILES_PER_COMMITS);

        if (onlyFixed) {
            sql.append(FIXED_ISSUES_ONLY);
        }

        selectParams.add(beginDate);
        selectParams.add(endDate);

        List<Object[]> rawFilesPerCommit = dao.selectNativeWithParams(sql.toString(), selectParams.toArray());
        
        Map<Integer, Integer> numFilesPerCommit = new HashMap<>(rawFilesPerCommit.size());
        for (Object[] objects : rawFilesPerCommit) {
            Integer commitId = (Integer) objects[0];
            Integer numFiles = (Integer) objects[1];
            numFilesPerCommit.put(commitId, numFiles);
        }

        return numFilesPerCommit;
    }

    public Map<Integer, List<Integer>> selectIssues(Date beginDate, Date endDate, Integer maxFilesPerCommit, boolean onlyFixed) {
        List<Object> selectParams = new ArrayList<>();

        StringBuilder sql = new StringBuilder();
        sql.append(SELECT_ISSUES_BY_CREATION_DATE);

        selectParams.add(beginDate);
        selectParams.add(endDate);

        if (onlyFixed) {
            sql.append(FIXED_ISSUES_ONLY);
        }

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
                = dao.selectNativeWithParams(SELECT_COMMENTERS_BY_ISSUE,
                        new Object[]{issue});

        List<Commenter> files = new ArrayList<>();
        for (Object[] row : rawFilesPath) {
            Commenter commenter = new Commenter((Integer) row[0], (String) row[1], (String) row[2]);
            files.add(commenter);
        }

        return files;
    }
}
