package br.edu.utfpr.cm.JGitMinerWeb.dao;

import static br.edu.utfpr.cm.JGitMinerWeb.dao.QueryUtils.filterByIssues;
import br.edu.utfpr.cm.minerador.services.matrix.model.FilePath;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 *
 * @author Rodrigo T. Kuroda
 */
public class BichoFileDAO {

    // filters
    private final String FILTER_BY_ISSUE;

    private final String FILTER_BY_ISSUE_FIX_DATE;

    private final String FILTER_BY_BEFORE_ISSUE_FIX_DATE;

    private final String FILTER_BY_AFTER_ISSUE_FIX_DATE;

    private final String FILTER_BY_MAX_FILES_IN_COMMIT;

    private final String FILTER_BY_MIN_FILES_IN_COMMIT;

    private final String FILTER_BY_MIN_ISSUE_COMMENTS;

    private final String FILTER_BY_USER_NAME;

    private final String FIXED_ISSUES_ONLY;

    private final String FILTER_BY_ISSUES_THAT_HAS_AT_LEAST_ONE_COMMENT;

    private final String FILTER_BY_LIKE_FILE_NAME;
    private final String FILTER_BY_JAVA_EXTENSION;
    private final String FILTER_BY_XML_EXTENSION;
    private final String FILTER_BY_JAVA_OR_XML_EXTENSION;

    // queries
    private final String COUNT_DISTINCT_COMMITERS_BY_FILE_NAME;

    private final String COUNT_COMMITS_BY_FILE_NAME;

    private final String COUNT_ISSUES;

    private final String SUM_CODE_CHURN_BY_FILE_NAME;

    private final String SELECT_FILES_PATH_BY_ISSUE;

    private final String SELECT_COMMITTERS_OF_FILE;

    private final GenericBichoDAO dao;

    public BichoFileDAO(GenericBichoDAO dao, String repository, Integer maxFilePerCommit) {
        this.dao = dao;

        FILTER_BY_ISSUE
                = " AND i.id = ? ";

        FILTER_BY_ISSUE_FIX_DATE
                = " AND c.changed_on BETWEEN ? AND ?";

        FILTER_BY_BEFORE_ISSUE_FIX_DATE
                = " AND c.changed_on <= ?";

        FILTER_BY_AFTER_ISSUE_FIX_DATE
                = " AND c.changed_on >= ?";

        FILTER_BY_MAX_FILES_IN_COMMIT
                = QueryUtils.getQueryForDatabase(
                        " AND s.num_files <= " + maxFilePerCommit, repository);

        FILTER_BY_MIN_FILES_IN_COMMIT
                = QueryUtils.getQueryForDatabase(
                        " AND s.num_files >= ? ", repository);

        FILTER_BY_MIN_ISSUE_COMMENTS
                = QueryUtils.getQueryForDatabase(
                        " AND i.num_comments >= ? ", repository);

        FILTER_BY_ISSUES_THAT_HAS_AT_LEAST_ONE_COMMENT
                = QueryUtils.getQueryForDatabase(
                        " AND i.num_comments > 0", repository);

        FILTER_BY_USER_NAME
                = " AND p.name = ?";

        FIXED_ISSUES_ONLY
                = " AND i.resolution = 'Fixed'"
                + " AND c.field = 'Resolution'"
                + " AND c.new_value = i.resolution";

        FILTER_BY_LIKE_FILE_NAME
                = " AND fill.file_path LIKE ?";
        FILTER_BY_JAVA_EXTENSION
                = " AND fill.file_path LIKE '%.java'";
        FILTER_BY_XML_EXTENSION
                = " AND fill.file_path LIKE '%.xml'";
        FILTER_BY_JAVA_OR_XML_EXTENSION
                = " AND (fill.file_path LIKE '%.java'"
                + " OR fill.file_path LIKE '%.xml')";

        COUNT_ISSUES
                = QueryUtils.getQueryForDatabase(
                        "SELECT COUNT(DISTINCT(i.id))"
                        + "  FROM {0}_vcs.scmlog s"
                        + "  JOIN {0}_issues.issues_scmlog i2s ON i2s.scmlog_id = s.id"
                        + "  JOIN {0}_issues.issues i ON i.id = i2s.issue_id"
                        + "  JOIN {0}_issues.changes c ON c.issue_id = i.id"
                        + "  JOIN {0}_vcs.actions a ON a.commit_id = s.id"
                        + "  JOIN {0}_vcs.files fil ON fil.id = a.file_id"
                        + "  JOIN {0}_vcs.file_links fill ON fill.file_id = fil.id AND fill.commit_id = "
                        + "       (SELECT MAX(afill.commit_id) " // last commit where file has introduced, because it can have more than one
                        + "          FROM aries_vcs.file_links afill "
                        + "         WHERE afill.commit_id <= s.id "
                        + "           AND afill.file_id = fil.id)"
                        + " WHERE fill.file_path = ?"
                        + "   AND s.date > i.submitted_on", repository)
                + FILTER_BY_MAX_FILES_IN_COMMIT;

        COUNT_DISTINCT_COMMITERS_BY_FILE_NAME
                = QueryUtils.getQueryForDatabase(
                        "SELECT COUNT(DISTINCT(p.name))"
                        + "  FROM {0}_vcs.files fil"
                        + "  JOIN {0}_vcs.actions a ON a.file_id = fil.id"
                        + "  JOIN {0}_vcs.scmlog s ON s.id = a.commit_id"
                        + "  JOIN {0}_vcs.people p ON p.id = s.committer_id"
                        + "  JOIN {0}_vcs.file_links fill ON fill.file_id = fil.id AND fill.commit_id = "
                        + "       (SELECT MAX(afill.commit_id) " // last commit where file has introduced, because it can have more than one
                        + "          FROM aries_vcs.file_links afill "
                        + "         WHERE afill.commit_id <= s.id "
                        + "           AND afill.file_id = fil.id)"
                        + "  JOIN {0}_issues.issues_scmlog i2s ON i2s.scmlog_id = a.commit_id"
                        + "  JOIN {0}_issues.issues i ON i.id = i2s.issue_id"
                        + "  JOIN {0}_issues.changes c ON c.issue_id = i.id"
                        + " WHERE fill.file_path = ?"
                        + "   AND s.date > i.submitted_on", repository)
                + FILTER_BY_MAX_FILES_IN_COMMIT;

        COUNT_COMMITS_BY_FILE_NAME
                = QueryUtils.getQueryForDatabase(
                        "SELECT COUNT(DISTINCT(s.id))"
                        + "  FROM {0}_vcs.files fil"
                        + "  JOIN {0}_vcs.actions a ON a.file_id = fil.id"
                        + "  JOIN {0}_vcs.scmlog s ON s.id = a.commit_id"
                        + "  JOIN {0}_vcs.people p ON p.id = s.committer_id"
                        + "  JOIN {0}_vcs.file_links fill ON fill.file_id = fil.id AND fill.commit_id = "
                        + "       (SELECT MAX(afill.commit_id) " // last commit where file has introduced, because it can have more than one
                        + "          FROM aries_vcs.file_links afill "
                        + "         WHERE afill.commit_id <= s.id "
                        + "           AND afill.file_id = fil.id)"
                        + "  JOIN {0}_issues.issues_scmlog i2s ON i2s.scmlog_id = a.commit_id"
                        + "  JOIN {0}_issues.issues i ON i.id = i2s.issue_id"
                        + "  JOIN {0}_issues.changes c ON c.issue_id = i.id"
                        + " WHERE fill.file_path = ?"
                        + "   AND s.date > i.submitted_on", repository)
                + FILTER_BY_MAX_FILES_IN_COMMIT;

        SUM_CODE_CHURN_BY_FILE_NAME
                = QueryUtils.getQueryForDatabase(
                        "SELECT COALESCE(SUM(filcl.added), 0),"
                        + "       COALESCE(SUM(filcl.removed), 0)"
                        + "  FROM {0}_vcs.scmlog s"
                        + "  JOIN {0}_vcs.people p ON p.id = s.committer_id"
                        + "  JOIN {0}_issues.issues_scmlog i2s ON i2s.scmlog_id = s.id"
                        + "  JOIN {0}_issues.issues i ON i.id = i2s.issue_id"
                        + "  JOIN {0}_issues.changes c ON c.issue_id = i.id"
                        + "  JOIN {0}_vcs.actions a ON a.commit_id = s.id"
                        + "  JOIN {0}_vcs.files fil ON fil.id = a.file_id"
                        + "  JOIN {0}_vcs.file_links fill ON fill.file_id = fil.id AND fill.commit_id = "
                        + "       (SELECT MAX(afill.commit_id) " // last commit where file has introduced, because it can have more than one
                        + "          FROM aries_vcs.file_links afill "
                        + "         WHERE afill.commit_id <= s.id "
                        + "           AND afill.file_id = fil.id)"
                        + "  JOIN {0}_vcs.commits_files_lines filcl ON filcl.commit = s.id AND filcl.path = fill.file_path"
                        + " WHERE fill.file_path = ?"
                        + "   AND s.date > i.submitted_on", repository)
                + FILTER_BY_MAX_FILES_IN_COMMIT;

        SELECT_FILES_PATH_BY_ISSUE
                = QueryUtils.getQueryForDatabase("SELECT fill.file_id, fill.file_path"
                        + "  FROM {0}_vcs.files fil"
                        + "  JOIN {0}_vcs.actions a ON a.file_id = fil.id"
                        + "  JOIN {0}_vcs.scmlog s ON s.id = a.commit_id"
                        + "  JOIN {0}_vcs.file_links fill ON fill.file_id = fil.id AND fill.commit_id = "
                        + "       (SELECT MAX(afill.commit_id) " // last commit where file has introduced, because it can have more than one
                        + "          FROM aries_vcs.file_links afill "
                        + "         WHERE afill.commit_id <= s.id "
                        + "           AND afill.file_id = fil.id)"
                        + " WHERE s.id = ?", repository)
                + FILTER_BY_MAX_FILES_IN_COMMIT
                + FILTER_BY_JAVA_OR_XML_EXTENSION;

        SELECT_COMMITTERS_OF_FILE
                = QueryUtils.getQueryForDatabase(
                        "SELECT DISTINCT(p.name)"
                        + "  FROM {0}_issues.issues i"
                        + "  JOIN {0}_issues.issues_scmlog i2s ON i2s.issue_id = i.id"
                        + "  JOIN {0}_issues.changes c ON c.issue_id = i.id"
                        + "  JOIN {0}_vcs.scmlog s ON s.id = i2s.scmlog_id"
                        + "  JOIN {0}_vcs.people p ON p.id = s.committer_id"
                        + "  JOIN {0}_vcs.actions a ON a.commit_id = s.id"
                        + "  JOIN {0}_vcs.files fil ON fil.id = a.file_id"
                        + "  JOIN {0}_vcs.file_links fill ON fill.file_id = fil.id AND fill.commit_id = "
                        + "       (SELECT MAX(afill.commit_id) " // last commit where file has introduced, because it can have more than one
                        + "          FROM aries_vcs.file_links afill "
                        + "         WHERE afill.commit_id <= s.id "
                        + "           AND afill.file_id = fil.id)"
                        + " WHERE fill.file_path = ?", repository)
                + FILTER_BY_MAX_FILES_IN_COMMIT;
    }

    // Issues //////////////////////////////////////////////////////////////////
    public long calculeNumberOfIssues(
            String filename, Date beginDate, Date endDate) {
        return calculeNumberOfIssues(filename,
                beginDate, endDate, true);
    }

    public long calculeNumberOfIssues(
            String filename, Date beginDate, Date endDate, boolean onlyFixed) {
        List<Object> selectParams = new ArrayList<>();

        StringBuilder sql = new StringBuilder();
        sql.append(COUNT_ISSUES);
        selectParams.add(filename);

        if (beginDate != null && endDate != null) { // from begin to end
            sql.append(FILTER_BY_ISSUE_FIX_DATE);
            selectParams.add(beginDate);
            selectParams.add(endDate);
        } else if (beginDate != null) { // from begin
            sql.append(FILTER_BY_AFTER_ISSUE_FIX_DATE);
            selectParams.add(beginDate);
        } else if (endDate != null) { // until end
            sql.append(FILTER_BY_BEFORE_ISSUE_FIX_DATE);
            selectParams.add(endDate);
        }

        if (onlyFixed) {
            sql.append(FIXED_ISSUES_ONLY);
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
                beginDate, endDate, true);
    }

    public long countDistinctCommittersByFilename(
            String filename, String user, Date beginDate, Date endDate) {
        return countDistinctCommittersByFilename(filename, user,
                beginDate, endDate, true);
    }

    public long countDistinctCommittersByFilename(
            String filename, String user, Date beginDate, Date endDate, boolean onlyFixed) {
        List<Object> selectParams = new ArrayList<>();

        StringBuilder sql = new StringBuilder();
        sql.append(COUNT_DISTINCT_COMMITERS_BY_FILE_NAME);

        selectParams.add(filename);

        if (beginDate != null && endDate != null) { // from begin to end
            sql.append(FILTER_BY_ISSUE_FIX_DATE);
            selectParams.add(beginDate);
            selectParams.add(endDate);
        } else if (beginDate != null) { // from begin
            sql.append(FILTER_BY_AFTER_ISSUE_FIX_DATE);
            selectParams.add(beginDate);
        } else if (endDate != null) { // until end
            sql.append(FILTER_BY_BEFORE_ISSUE_FIX_DATE);
            selectParams.add(endDate);
        }

        if (onlyFixed) {
            sql.append(FIXED_ISSUES_ONLY);
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
            sql.append(FILTER_BY_ISSUE_FIX_DATE);
            selectParams.add(beginDate);
            selectParams.add(endDate);
        } else if (beginDate != null) { // from begin
            sql.append(FILTER_BY_AFTER_ISSUE_FIX_DATE);
            selectParams.add(beginDate);
        } else if (endDate != null) { // until end
            sql.append(FILTER_BY_BEFORE_ISSUE_FIX_DATE);
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
        return sumCodeChurnByFilename(filename, null, beginDate, endDate, null, true);
    }

    public AuxCodeChurn sumCodeChurnByFilename(
            String filename, String user, Date beginDate, Date endDate) {
        return sumCodeChurnByFilename(filename, user, beginDate, endDate, null, true);
    }

    public AuxCodeChurn sumCodeChurnByFilename(
            String filename, Date beginDate, Date endDate, Collection<Integer> issues) {
        return sumCodeChurnByFilename(filename, null, beginDate, endDate, issues, true);
    }

    public AuxCodeChurn sumCodeChurnByFilename(
            String filename, String user, Date beginDate, Date endDate, Collection<Integer> issues) {
        return sumCodeChurnByFilename(filename, user, beginDate, endDate, issues, true);
    }

    public AuxCodeChurn sumCodeChurnByFilename(
            String fileName, String user, Date beginDate, Date endDate,
            Collection<Integer> issues, boolean onlyFixed) {
        List<Object> selectParams = new ArrayList<>();

        StringBuilder sql = new StringBuilder();
        sql.append(SUM_CODE_CHURN_BY_FILE_NAME);

        selectParams.add(fileName);

        if (beginDate != null) {
            sql.append(FILTER_BY_AFTER_ISSUE_FIX_DATE);
            selectParams.add(beginDate);
        }

        if (endDate != null) {
            sql.append(FILTER_BY_BEFORE_ISSUE_FIX_DATE);
            selectParams.add(endDate);
        }

        if (onlyFixed) {
            sql.append(FIXED_ISSUES_ONLY);
        }

        if (user != null) {
            sql.append(FILTER_BY_USER_NAME);
            selectParams.add(user);
        }

        filterByIssues(issues, sql);

        List<Object[]> sum = dao.selectNativeWithParams(sql.toString(),
                selectParams.toArray());

        return new AuxCodeChurn(fileName,
                ((BigDecimal) sum.get(0)[0]).longValue(), ((BigDecimal) sum.get(0)[1]).longValue());
    }

    public List<FilePath> selectFilesByCommitId(Integer commitId) {
// TODO comment for optimize performance
//        return selectFilesByCommitId(commitId, null);
//    }
//
//    public List<FilePath> selectFilesByCommitId(Integer commitId, String[] filesExtension) {
//        List<Object> selectParams = new ArrayList<>();
//
//        StringBuilder sql = new StringBuilder(SELECT_FILES_PATH_BY_ISSUE);

//        selectParams.add(commitId);
//
//        if (filesExtension != null) {
//            for (String extension : filesExtension) {
//                sql.append(FILTER_BY_LIKE_FILE_NAME);
//                selectParams.add("%" + extension);
//            }
//        }

        List<Object[]> rawFilesPath
//                = dao.selectNativeWithParams(sql.toString(), selectParams.toArray());
                = dao.selectNativeWithParams(SELECT_FILES_PATH_BY_ISSUE, new Object[]{commitId});

        List<FilePath> files = new ArrayList<>();
        for (Object[] row : rawFilesPath) {
            FilePath filePath = new FilePath(commitId, (Integer) row[0], (String) row[1]);
            files.add(filePath);
        }

        return files;
    }

    public Set<AuxUser> selectCommitters(
            String file, Date beginDate, Date endDate) {
        return selectCommitters(file, beginDate, endDate, true);
    }

    public Set<AuxUser> selectCommitters(
            String file, Date beginDate, Date endDate, boolean onlyFixed) {

        List<Object> selectParams = new ArrayList<>();
        if (file == null) {
            throw new IllegalArgumentException("Pair file could not be null");
        }

        StringBuilder sql = new StringBuilder();
        sql.append(SELECT_COMMITTERS_OF_FILE);

        selectParams.add(file);

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

        List<String> committers = dao.selectNativeWithParams(
                sql.toString(), selectParams.toArray());

        Set<AuxUser> commitersList = new HashSet<>(committers.size());
        for (String name : committers) {
            commitersList.add(new AuxUser(name));
        }

        return commitersList;
    }
}
