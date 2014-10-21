package br.edu.utfpr.cm.JGitMinerWeb.dao;

import br.edu.utfpr.cm.JGitMinerWeb.model.miner.EntityComment;
import br.edu.utfpr.cm.JGitMinerWeb.services.metric.auxiliary.AuxWordiness;
import java.math.BigDecimal;
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

    // filters
    private final String FILTER_BY_MAX_FILES_IN_COMMIT;

    private final String FILTER_BY_ISSUE_CREATION_DATE;

    private final String FILTER_BY_BEFORE_ISSUE_CREATION_DATE;

    private final String FILTER_BY_AFTER_ISSUE_CREATION_DATE;

    private final String FIXED_ISSUES_ONLY;

    private final String FILTER_BY_ISSUES_THAT_HAS_AT_LEAST_ONE_COMMENT;

    // commits
    private final String COUNT_ISSUES_BY_FILE_NAME;

    private final String LIST_COMMENTS_OF_FILE_PAIR_BY_DATE;

    private final String SELECT_ISSUES_COMMENTS_OF_FILE_PAIR_BY_DATE;

    private final String COUNT_COMMENTS_OF_FILE_PAIR_BY_DATE;

    private final String SUM_CHANGES_OF_FILE_PAIR_BY_DATE;

    private final String SUM_ADD_DEL_LINES_OF_FILE_PAIR_BY_DATE;

    private final String SELECT_COMMITTERS_OF_PAIR_FILE;

    private final String COUNT_PAIR_FILE_COMMITS;

    private final String COUNT_PAIR_FILE_COMMITTERS;

    private final String COUNT_ISSUES;

    private final String FILTER_BY_USER_NAME;

    private final String SELECT_RELEASE_MIN_MAX_DATE_CREATION;

    private final GenericBichoDAO dao;

    public BichoPairFileDAO(GenericBichoDAO dao, String repository, Integer maxFilePerCommit) {
        this.dao = dao;

        if (repository == null
                || maxFilePerCommit == null) {
            throw new IllegalArgumentException("The parameters 'repository' and 'maxFilePerCommit' can not be null.");
        }

        // filters
        FILTER_BY_MAX_FILES_IN_COMMIT
                = QueryUtils.getQueryForDatabase(
                        " AND (SELECT COUNT(1)"
                        + "        FROM {0}_vcs.files cfil"
                        + "        JOIN {0}_vcs.actions ca ON ca.file_id = cfil.id"
                        + "        JOIN {0}_vcs.scmlog cs ON cs.id = ca.commit_id"
                        + "       WHERE cs.id = s.id) <= " + maxFilePerCommit, repository);

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
                        "AND (SELECT COUNT(1)"
                        + "        FROM {0}_issues.comments ic2"
                        + "       WHERE ic2.issue_id = i.id) > 0", repository);

        FILTER_BY_USER_NAME
                = " AND (p.name IS NOT NULL AND "
                + "      p2.name IS NOT NULL AND "
                + "      p.name = ? AND "
                + "      p2.name = ?)";

        // commit
        COUNT_ISSUES
                = QueryUtils.getQueryForDatabase(
                        "SELECT DISTINCT(i.id)"
                        + "  FROM {0}_vcs.scmlog s"
                        + "  JOIN {0}_issues.issues_scmlog i2s ON i2s.scmlog_id = s.id"
                        + "  JOIN {0}_issues.issues i ON i.id = i2s.issue_id"
                        + "  JOIN {0}_vcs.actions a ON a.commit_id = s.id"
                        + "  JOIN {0}_vcs.files fil ON fil.id = a.file_id"
                        + "  JOIN {0}_vcs.file_links fill ON fill.file_id = fil.id"
                        + "  JOIN {0}_vcs.actions a2 ON a2.commit_id = s.id"
                        + "  JOIN {0}_vcs.files fil2 ON fil2.id = a2.file_id AND a.file_id <> a2.file_id"
                        + "  JOIN {0}_vcs.file_links fill2 ON fill2.file_id = fil2.id"
                        + " WHERE 1 = 1", repository)
                + FILTER_BY_MAX_FILES_IN_COMMIT;

        COUNT_ISSUES_BY_FILE_NAME
                = QueryUtils.getQueryForDatabase(
                        "SELECT DISTINCT(i.id)"
                        + "  FROM {0}_vcs.scmlog s"
                        + "  JOIN {0}_issues.issues_scmlog i2s ON i2s.scmlog_id = s.id"
                        + "  JOIN {0}_issues.issues i ON i.id = i2s.issue_id"
                        + "  JOIN {0}_vcs.actions a ON a.commit_id = s.id"
                        + "  JOIN {0}_vcs.files fil ON fil.id = a.file_id"
                        + "  JOIN {0}_vcs.file_links fill ON fill.file_id = fil.id"
                        + "  JOIN {0}_vcs.actions a2 ON a2.commit_id = s.id"
                        + "  JOIN {0}_vcs.files fil2 ON fil2.id = a2.file_id AND a.file_id <> a2.file_id"
                        + "  JOIN {0}_vcs.file_links fill2 ON fill2.file_id = fil2.id"
                        + " WHERE fill.file_path = ?"
                        + "   AND fill2.file_path = ?", repository)
                + FILTER_BY_MAX_FILES_IN_COMMIT;

        LIST_COMMENTS_OF_FILE_PAIR_BY_DATE
                = QueryUtils.getQueryForDatabase(
                        "SELECT c.text"
                        + "  FROM {0}_vcs.scmlog s"
                        + "  JOIN {0}_issues.issues_scmlog i2s ON i2s.scmlog_id = s.id"
                        + "  JOIN {0}_issues.issues i ON i.id = i2s.issue_id"
                        + "  JOIN {0}_issues.comments c ON c.issue_id = i.id"
                        + "  JOIN {0}_vcs.actions a ON a.commit_id = s.id"
                        + "  JOIN {0}_vcs.files fil ON fil.id = a.file_id"
                        + "  JOIN {0}_vcs.file_links fill ON fill.file_id = fil.id"
                        + "  JOIN {0}_vcs.actions a2 ON a2.commit_id = s.id"
                        + "  JOIN {0}_vcs.files fil2 ON fil2.id = a2.file_id AND a.file_id <> a2.file_id"
                        + "  JOIN {0}_vcs.file_links fill2 ON fill2.file_id = fil2.id"
                        + " WHERE fill.file_path = ?"
                        + "   AND fill2.file_path = ?", repository)
                + FILTER_BY_MAX_FILES_IN_COMMIT;

        SELECT_ISSUES_COMMENTS_OF_FILE_PAIR_BY_DATE
                = QueryUtils.getQueryForDatabase(
                        "SELECT i.id, i.issue, i.description, c.text"
                        + "  FROM {0}_vcs.scmlog s"
                        + "  JOIN {0}_issues.issues_scmlog i2s ON i2s.scmlog_id = s.id"
                        + "  JOIN {0}_issues.issues i ON i.id = i2s.issue_id"
                        + "  JOIN {0}_issues.comments c ON c.issue_id = i.id"
                        + "  JOIN {0}_vcs.actions a ON a.commit_id = s.id"
                        + "  JOIN {0}_vcs.files fil ON fil.id = a.file_id"
                        + "  JOIN {0}_vcs.file_links fill ON fill.file_id = fil.id"
                        + "  JOIN {0}_vcs.actions a2 ON a2.commit_id = s.id"
                        + "  JOIN {0}_vcs.files fil2 ON fil2.id = a2.file_id AND a.file_id <> a2.file_id"
                        + "  JOIN {0}_vcs.file_links fill2 ON fill2.file_id = fil2.id"
                        + " WHERE fill.file_path = ?"
                        + "   AND fill2.file_path = ?", repository)
                + FILTER_BY_MAX_FILES_IN_COMMIT;

        COUNT_COMMENTS_OF_FILE_PAIR_BY_DATE
                = QueryUtils.getQueryForDatabase(
                        "SELECT COUNT(DISTINCT(c.id))"
                        + "  FROM {0}_vcs.scmlog s"
                        + "  JOIN {0}_issues.issues_scmlog i2s ON i2s.scmlog_id = s.id"
                        + "  JOIN {0}_issues.issues i ON i.id = i2s.issue_id"
                        + "  JOIN {0}_issues.comments c ON c.issue_id = i.id"
                        + "  JOIN {0}_vcs.actions a ON a.commit_id = s.id"
                        + "  JOIN {0}_vcs.files fil ON fil.id = a.file_id"
                        + "  JOIN {0}_vcs.file_links fill ON fill.file_id = fil.id"
                        + "  JOIN {0}_vcs.actions a2 ON a2.commit_id = s.id"
                        + "  JOIN {0}_vcs.files fil2 ON fil2.id = a2.file_id AND a.file_id <> a2.file_id"
                        + "  JOIN {0}_vcs.file_links fill2 ON fill2.file_id = fil2.id"
                        + " WHERE fill.file_path = ?"
                    + "   AND fill2.file_path = ?", repository)
                + FILTER_BY_MAX_FILES_IN_COMMIT
                + FILTER_BY_ISSUE_CREATION_DATE;

        SUM_CHANGES_OF_FILE_PAIR_BY_DATE
                = QueryUtils.getQueryForDatabase(
                        "SELECT COALESCE((SUM(filcl.added) + SUM(filcl.removed) "
                        + "             + SUM(filcl2.added) + SUM(filcl2.removed), 0)"
                        + "  FROM {0}_vcs.scmlog s"
                        + "  JOIN {0}_issues.issues_scmlog i2s ON i2s.scmlog_id = s.id"
                        + "  JOIN {0}_issues.issues i ON i.id = i2s.issue_id"
                        + "  JOIN {0}_vcs.actions a ON a.commit_id = s.id"
                        + "  JOIN {0}_vcs.files fil ON fil.id = a.file_id"
                        + "  JOIN {0}_vcs.file_links fill ON fill.file_id = fil.id"
                        + "  JOIN {0}_vcs.actions a2 ON a2.commit_id = s.id"
                        + "  JOIN {0}_vcs.files fil2 ON fil2.id = a2.file_id AND a2.file_id <> a.file_id"
                        + "  JOIN {0}_vcs.file_links fill2 ON fill2.file_id = fil2.id"
                        + "  JOIN {0}_vcs.commits_files_lines filcl ON filcl.file_id = fil.id AND filcl.commit_id = s.id"
                        + "  JOIN {0}_vcs.commits_files_lines filcl2 ON filcl2.file_id = fil2.id AND filcl2.commit_id = s.id"
                        + " WHERE fill.file_path = ?"
                        + "   AND fill2.file_path = ?", repository)
                + FILTER_BY_MAX_FILES_IN_COMMIT;

        SUM_ADD_DEL_LINES_OF_FILE_PAIR_BY_DATE
                = QueryUtils.getQueryForDatabase(
                        "SELECT COALESCE((SUM(filcl.added) + SUM(filcl.added)), 0), "
                        + "      COALESCE((SUM(filcl.removed) + SUM(filcl2.removed)), 0),"
                        + "      COALESCE((SUM(filcl.added) + SUM(filcl.removed) + SUM(filcl2.added) + SUM(filcl2.removed)), 0)"
                        + "  FROM {0}_vcs.scmlog s"
                        + "  JOIN {0}_issues.issues_scmlog i2s ON i2s.scmlog_id = s.id"
                        + "  JOIN {0}_issues.issues i ON i.id = i2s.issue_id"
                        + "  JOIN {0}_vcs.actions a ON a.commit_id = s.id"
                        + "  JOIN {0}_vcs.files fil ON fil.id = a.file_id"
                        + "  JOIN {0}_vcs.file_links fill ON fill.file_id = fil.id"
                        + "  JOIN {0}_vcs.actions a2 ON a2.commit_id = s.id"
                        + "  JOIN {0}_vcs.files fil2 ON fil2.id = a2.file_id AND a2.file_id <> a.file_id"
                        + "  JOIN {0}_vcs.file_links fill2 ON fill2.file_id = fil2.id"
                        + "  JOIN {0}_vcs.commits_files_lines filcl ON filcl.file_id = fil.id AND filcl.commit_id = s.id"
                        + "  JOIN {0}_vcs.commits_files_lines filcl2 ON filcl2.file_id = fil2.id AND filcl2.commit_id = s.id"
                        + " WHERE fill.file_path = ?"
                        + "   AND fill2.file_path = ?", repository)
                + FILTER_BY_MAX_FILES_IN_COMMIT
                + FILTER_BY_ISSUE_CREATION_DATE;

        SELECT_COMMITTERS_OF_PAIR_FILE
                = QueryUtils.getQueryForDatabase(
                        "SELECT DISTINCT(p.name)"
                        + "  FROM {0}_vcs.scmlog s"
                        + "  JOIN {0}_vcs.people p ON p.id = s.committer_id"
                        + "  JOIN {0}_issues.issues_scmlog i2s ON i2s.scmlog_id = s.id"
                        + "  JOIN {0}_issues.issues i ON i.id = i2s.issue_id"
                        + "  JOIN {0}_vcs.actions a ON a.commit_id = s.id"
                        + "  JOIN {0}_vcs.files fil ON fil.id = a.file_id"
                        + "  JOIN {0}_vcs.file_links fill ON fill.file_id = fil.id"
                        + "  JOIN {0}_vcs.actions a2 ON a2.commit_id = s.id"
                        + "  JOIN {0}_vcs.files fil2 ON fil2.id = a2.file_id AND a2.file_id <> a.file_id"
                        + "  JOIN {0}_vcs.file_links fill2 ON fill2.file_id = fil2.id"
                        + " WHERE fill.file_path = ?"
                        + "   AND fill2.file_path = ?", repository)
                + FILTER_BY_MAX_FILES_IN_COMMIT;

        COUNT_PAIR_FILE_COMMITS
                = QueryUtils.getQueryForDatabase(
                        "SELECT COUNT(DISTINCT(s.id))"
                        + "  FROM {0}_vcs.scmlog s"
                        + "  JOIN {0}_vcs.actions a ON a.commit_id = s.id"
                        + "  JOIN {0}_vcs.files fil ON fil.id = a.file_id"
                        + "  JOIN {0}_vcs.file_links fill ON fill.file_id = fil.id"
                        + "  JOIN {0}_issues.issues_scmlog i2s ON i2s.scmlog_id = a.commit_id"
                        + "  JOIN {0}_issues.issues i ON i.id = i2s.issue_id"
                        + "  JOIN {0}_vcs.actions a2 ON a2.commit_id = s.id"
                        + "  JOIN {0}_vcs.files fil2 ON fil2.id = a2.file_id AND a2.file_id <> a.file_id"
                        + "  JOIN {0}_vcs.file_links fill2 ON fill2.file_id = fil2.id"
                        + " WHERE fill.file_path = ?"
                        + "   AND fill2.file_path = ?", repository)
                + FILTER_BY_MAX_FILES_IN_COMMIT;

        COUNT_PAIR_FILE_COMMITTERS
                = QueryUtils.getQueryForDatabase(
                        "SELECT COUNT(DISTINCT(p.name))"
                        + "  FROM {0}_vcs.scmlog s"
                        + "  JOIN {0}_issues.issues_scmlog i2s ON i2s.scmlog_id = s.id"
                        + "  JOIN {0}_issues.issues i ON i.id = i2s.issue_id"
                        + "  JOIN {0}_issues.comments c ON c.issue_id = i.id"
                        + "  JOIN {0}_issues.people p ON p.id = c.submitted_by"
                        + "  JOIN {0}_vcs.actions a ON a.commit_id = s.id"
                        + "  JOIN {0}_vcs.files fil ON fil.id = a.file_id"
                        + "  JOIN {0}_vcs.file_links fill ON fill.file_id = fil.id"
                        + "  JOIN {0}_vcs.actions a2 ON a2.commit_id = s.id"
                        + "  JOIN {0}_vcs.files fil2 ON fil2.id = a2.file_id AND a2.file_id <> a.file_id"
                        + "  JOIN {0}_vcs.file_links fill2 ON fill2.file_id = fil2.id"
                        + " WHERE fill.file_path = ?"
                        + "   AND fill2.file_path = ?", repository)
                + FILTER_BY_MAX_FILES_IN_COMMIT;

        SELECT_RELEASE_MIN_MAX_DATE_CREATION
                = QueryUtils.getQueryForDatabase(
                        "SELECT MIN(s.date), MAX(s.date)"
                        + "  FROM {0}_vcs.scmlog s"
                        + "  JOIN {0}_issues.issues_scmlog i2s ON i2s.scmlog_id = s.id"
                        + "  JOIN {0}_issues.issues i ON i.id = i2s.issue_id"
                        + "  JOIN {0}_vcs.actions a ON a.commit_id = s.id"
                        + "  JOIN {0}_vcs.files fil ON fil.id = a.file_id"
                        + "  JOIN {0}_vcs.file_links fill ON fill.file_id = fil.id"
                        + "  JOIN {0}_vcs.actions a2 ON a2.commit_id = s.id"
                        + "  JOIN {0}_vcs.files fil2 ON fil2.id = a2.file_id AND a2.file_id <> a.file_id"
                        + "  JOIN {0}_vcs.file_links fill2 ON fill2.file_id = fil2.id"
                        + " WHERE fill.file_path = ?"
                        + "   AND fill2.file_path = ?", repository)
                + FILTER_BY_MAX_FILES_IN_COMMIT;
    }

    public long calculeNumberOfIssues(Date beginDate, Date endDate, boolean onlyFixed) {

        List<Object> selectParams = new ArrayList<>();

        StringBuilder sql = new StringBuilder();
        sql.append(COUNT_ISSUES);

        if (onlyFixed) {
            sql.append(FIXED_ISSUES_ONLY);
        }

        sql.append(FILTER_BY_ISSUE_CREATION_DATE);
        selectParams.add(beginDate);
        selectParams.add(endDate);

        Long count = (Long) dao.selectNativeOneWithParams(sql.toString(),
                selectParams.toArray());

        return count != null ? count : 0l;
    }

    public long calculeNumberOfIssues(
            String file, String file2, Date beginDate, Date endDate, boolean onlyFixed) {

        if (file == null || file2 == null) {
            throw new IllegalArgumentException("The file and file2 parameters can not be null.");
        }

        List<Object> selectParams = new ArrayList<>();

        StringBuilder sql = new StringBuilder();
        sql.append(COUNT_ISSUES_BY_FILE_NAME);
        selectParams.add(file);
        selectParams.add(file2);

        if (onlyFixed) {
            sql.append(FIXED_ISSUES_ONLY);
        }

        sql.append(FILTER_BY_ISSUE_CREATION_DATE);
        selectParams.add(beginDate);
        selectParams.add(endDate);

        Long count = (Long) dao.selectNativeOneWithParams(sql.toString(),
                selectParams.toArray());

        return count != null ? count : 0l;
    }

    public long calculeComments(
            String file, String file2, Date beginDate, Date endDate, boolean onlyFixed) {

        if (file == null || file2 == null) {
            throw new IllegalArgumentException("The file and file2 parameters can not be null.");
        }

        List<Object> selectParams = new ArrayList<>();

        StringBuilder sql = new StringBuilder();
        sql.append(COUNT_COMMENTS_OF_FILE_PAIR_BY_DATE);

        if (onlyFixed) {
            sql.append(FIXED_ISSUES_ONLY);
        }

        selectParams.add(file);
        selectParams.add(file2);
        selectParams.add(beginDate);
        selectParams.add(endDate);

        Long count = dao.selectNativeOneWithParams(sql.toString(), selectParams.toArray());

        return count != null ? count : 0l;
    }

    public Long calculeCodeChurn(
            String fileName, String fileName2, Date beginDate, Date endDate) {

        Object[] bdObjects = new Object[]{
            fileName,
            fileName2,
            beginDate,
            endDate
        };

        BigDecimal sum = dao.selectNativeOneWithParams(SUM_CHANGES_OF_FILE_PAIR_BY_DATE, bdObjects);

        return sum.longValue();
    }

    public AuxCodeChurn calculeCodeChurnAddDelChange(
            String fileName, String fileName2, Date beginDate, Date endDate) {

        Object[] params = new Object[]{
            fileName,
            fileName2,
            beginDate,
            endDate
        };

        List<Object[]> sum = dao.selectNativeWithParams(
                SUM_ADD_DEL_LINES_OF_FILE_PAIR_BY_DATE, params);

        Long additions = sum.get(0)[0] == null ? 0l : ((BigDecimal) sum.get(0)[0]).longValue();
        Long deletions = sum.get(0)[1] == null ? 0l : ((BigDecimal) sum.get(0)[1]).longValue();
        Long changes = sum.get(0)[2] == null ? 0l : ((BigDecimal) sum.get(0)[2]).longValue();

        return new AuxCodeChurn(fileName, fileName2,
                additions, deletions, changes);
    }

    public Set<AuxUser> selectCommitters(
            String file, String file2, Date beginDate, Date endDate) {
        return selectCommitters(file, file2, beginDate, endDate, true);
    }

    public Set<AuxUser> selectCommitters(
            String file, String file2, Date beginDate, Date endDate, boolean onlyFixed) {

        List<Object> selectParams = new ArrayList<>();
        if (file == null || file2 == null) {
            throw new IllegalArgumentException("Pair file could not be null");
        }

        StringBuilder sql = new StringBuilder();
        sql.append(SELECT_COMMITTERS_OF_PAIR_FILE);

        selectParams.add(file);
        selectParams.add(file2);

        if (onlyFixed) {
            sql.append(FIXED_ISSUES_ONLY);
        }

        if (beginDate != null) {
            sql.append(FILTER_BY_AFTER_ISSUE_CREATION_DATE);
            selectParams.add(beginDate);
        }

        if (endDate != null) {
            sql.append(FILTER_BY_BEFORE_ISSUE_CREATION_DATE);
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

    public long calculeCommits(String file, String file2) {
        return calculeCommits(file, file2, null, null, null, true);
    }

    public long calculeCommits(String file, String file2, Date beginDate, Date endDate) {
        return calculeCommits(file, file2, null, beginDate, endDate, true);
    }

    public long calculeCommits(String file, String file2, Date beginDate, Date endDate, boolean onlyFixed) {
        return calculeCommits(file, file2, null, beginDate, endDate, onlyFixed);
    }

    public long calculeCommits(String file, String file2, String user, Date beginDate, Date endDate) {
        return calculeCommits(file, file2, user, beginDate, endDate, true);
    }

    public long calculeCommits(
            String file, String file2, String user, Date beginDate, Date endDate, boolean onlyFixed) {
        List<Object> selectParams = new ArrayList<>();

        if (file == null || file2 == null) {
            throw new IllegalArgumentException("Pair file could not be null");
        }

        StringBuilder sql = new StringBuilder();
        sql.append(COUNT_PAIR_FILE_COMMITS);

        selectParams.add(file);
        selectParams.add(file2);

        if (onlyFixed) {
            sql.append(FIXED_ISSUES_ONLY);
        }

        if (beginDate != null && endDate != null) {
            sql.append(FILTER_BY_ISSUE_CREATION_DATE);
            selectParams.add(beginDate);
            selectParams.add(endDate);
        }

        if (user != null) {
            sql.append(FILTER_BY_USER_NAME);
            selectParams.add(user); // commiter name of file 1
            selectParams.add(user); // commiter name of file 2
        }

        Long count = dao.selectNativeOneWithParams(sql.toString(), selectParams.toArray());

        return count != null ? count : 0l;
    }

    public long calculeCommitters(
            String file, String file2) {
        return calculeCommitters(file, file2, null, null, true);
    }

    public long calculeCommitters(
            String file, String file2, Date beginDate, Date endDate) {
        return calculeCommitters(file, file2, beginDate, endDate, true);
    }

    public long calculeCommitters(
            String file, String file2, Date beginDate, Date endDate, boolean onlyFixed) {
        List<Object> selectParams = new ArrayList<>();

        if (file == null || file2 == null) {
            throw new IllegalArgumentException("Pair file could not be null");
        }

        StringBuilder sql = new StringBuilder();
        sql.append(COUNT_PAIR_FILE_COMMITTERS);

        selectParams.add(file);
        selectParams.add(file2);

        if (onlyFixed) {
            sql.append(FIXED_ISSUES_ONLY);
        }

        if (beginDate != null) {
            sql.append(FILTER_BY_AFTER_ISSUE_CREATION_DATE);
            selectParams.add(beginDate);
        }

        if (endDate != null) {
            sql.append(FILTER_BY_BEFORE_ISSUE_CREATION_DATE);
            selectParams.add(endDate);
        }

        Long count = dao.selectNativeOneWithParams(sql.toString(), selectParams.toArray());

        return count != null ? count : 0l;
    }

    public Collection<AuxWordiness> listIssues(String file, String file2, Date beginDate, Date endDate, boolean onlyMergeds) {
        List<Object> selectParams = new ArrayList<>();

        StringBuilder sql = new StringBuilder();
        sql.append(SELECT_ISSUES_COMMENTS_OF_FILE_PAIR_BY_DATE);

        if (onlyMergeds) {
            sql.append(FIXED_ISSUES_ONLY);
        }

        selectParams.add(file);
        selectParams.add(file2);
        selectParams.add(beginDate);
        selectParams.add(endDate);

        List<Object[]> rawIssues = dao.selectNativeWithParams(sql.toString(), selectParams.toArray());
        
        Map<Integer, AuxWordiness> cache = new HashMap<>(rawIssues.size());
        for (Object[] objects : rawIssues) {
            Integer issueNumber = (Integer) objects[0];

            if (cache.containsKey(issueNumber)) {
                cache.get(issueNumber).addComment((String) objects[3]);
            } else {
                List<String> comments = new ArrayList<>();
                comments.add((String) objects[3]);
                AuxWordiness issue = new AuxWordiness(
                        issueNumber,
                        (String) objects[1],
                        (String) objects[2],
                        comments);
                cache.put(issueNumber, issue);
            }
        }

        return cache.values();
    }

    public List<EntityComment> listComments( String file, String file2, Date beginDate, Date endDate, boolean onlyFixed) {
        if (file == null || file2 == null) {
            throw new IllegalArgumentException("The file and file2 parameters can not be null.");
        }

        List<Object> selectParams = new ArrayList<>();

        StringBuilder sql = new StringBuilder();
        sql.append(LIST_COMMENTS_OF_FILE_PAIR_BY_DATE);

        if (onlyFixed) {
            sql.append(FIXED_ISSUES_ONLY);
        }

        selectParams.add(file);
        selectParams.add(file2);
        selectParams.add(beginDate);
        selectParams.add(endDate);

        List<EntityComment> comments = dao.selectNativeWithParams(sql.toString(), selectParams.toArray());

        return comments;
    }

    public int calculePairFileDaysAge( String file, String file2, Date beginDate, Date endDate, boolean onlyFixed) {
        if (file == null || file2 == null) {
            throw new IllegalArgumentException("The file and file2 parameters can not be null.");
        }

        List<Object> selectParams = new ArrayList<>();

        StringBuilder sql = new StringBuilder();
        sql.append(SELECT_RELEASE_MIN_MAX_DATE_CREATION);

        if (onlyFixed) {
            sql.append(FIXED_ISSUES_ONLY);
        }

        selectParams.add(file);
        selectParams.add(file2);

        if (beginDate != null) {
            sql.append(FILTER_BY_AFTER_ISSUE_CREATION_DATE);
            selectParams.add(beginDate);
        }

        if (endDate != null) {
            sql.append(FILTER_BY_BEFORE_ISSUE_CREATION_DATE);
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
}
