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

    // commons query fragment
    private final String FROM_TABLE;
    private final String WHERE;

    // filters
    private final String FILTER_BY_ISSUE;

    private final String FILTER_BY_ISSUE_FIX_MAJOR_VERSION;
    private final String FILTER_BY_BEFORE_ISSUE_FIX_MAJOR_VERSION;

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
    private final String FILTER_BY_JAVA_OR_XML_EXTENSION_AND_IS_NOT_TEST;

    // complete queries (commons fragment + specific fragments for query)
    private final String COUNT_DISTINCT_COMMITERS_BY_FILE_NAME;

    private final String COUNT_COMMITS_BY_FILE_NAME;

    private final String COUNT_ISSUES_BY_FILENAME;

    private final String SUM_CODE_CHURN_BY_FILE_NAME;

    private final String SELECT_FILES_PATH_BY_COMMIT_ID;

    private final String SELECT_COMMITTERS_OF_FILE;

    private final String COUNT_ISSUE_REOPENED_TIMES;

    private final GenericBichoDAO dao;

    public BichoFileDAO(GenericBichoDAO dao, String repository, Integer maxFilePerCommit) {
        this.dao = dao;

        FROM_TABLE
                = "  FROM {0}_issues.issues i"
                + "  JOIN {0}_issues.changes c ON c.issue_id = i.id"
                + "  JOIN {0}_issues.issues_scmlog i2s ON i2s.issue_id = i.id"
                + "  JOIN {0}_vcs.scmlog s ON s.id = i2s.scmlog_id"
                + "  JOIN {0}.commits com ON com.commit_id = i2s.scmlog_id";

        WHERE = " WHERE com.file_path = ?"
                + "   AND com.date > i.submitted_on";

        FILTER_BY_ISSUE
                = " AND i.id = ? ";

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
                        + " WHERE ifvo.version_order <= "
                        + "(SELECT ifvo2.version_order"
                        + "   FROM {0}_issues.issues_fix_version_order ifvo2"
                        + "  WHERE ifvo2.major_fix_version = ?))", repository);

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
                = " AND i.resolution = \"Fixed\""
                + " AND c.field = \"Resolution\""
                + " AND c.new_value = i.resolution";

        FILTER_BY_LIKE_FILE_NAME
                = " AND com.file_path LIKE ?";
        FILTER_BY_JAVA_EXTENSION
                = " AND com.file_path LIKE \"%.java\"";
        FILTER_BY_XML_EXTENSION
                = " AND com.file_path LIKE \"%.xml\"";
        FILTER_BY_JAVA_OR_XML_EXTENSION
                = " AND (com.file_path LIKE \"%.java\""
                + " OR com.file_path LIKE \"%.xml\")";
        FILTER_BY_JAVA_OR_XML_EXTENSION_AND_IS_NOT_TEST
                = " AND (com.file_path LIKE \"%.java\""
                + " OR com.file_path LIKE \"%.xml\")"
                + " AND com.file_path NOT LIKE \"%Test.java\"";

        COUNT_ISSUES_BY_FILENAME
                = QueryUtils.getQueryForDatabase(
                        "SELECT COUNT(DISTINCT(i.id))"
                        + FROM_TABLE
                        + WHERE, repository)
                + FILTER_BY_MAX_FILES_IN_COMMIT;

        COUNT_DISTINCT_COMMITERS_BY_FILE_NAME
                = QueryUtils.getQueryForDatabase(
                        "SELECT COUNT(DISTINCT(p.name))"
                        + FROM_TABLE
                        + "  JOIN {0}_vcs.people p ON p.id = s.committer_id"
                        + WHERE, repository)
                + FILTER_BY_MAX_FILES_IN_COMMIT;

        COUNT_COMMITS_BY_FILE_NAME
                = QueryUtils.getQueryForDatabase(
                        "SELECT COUNT(DISTINCT(s.id))"
                        + FROM_TABLE
                        + "  JOIN {0}_vcs.people p ON p.id = s.committer_id"
                        + WHERE, repository)
                + FILTER_BY_MAX_FILES_IN_COMMIT
                + FIXED_ISSUES_ONLY;

        SUM_CODE_CHURN_BY_FILE_NAME
                = QueryUtils.getQueryForDatabase(
                        "SELECT COALESCE(SUM(com.added_lines), 0),"
                        + "       COALESCE(SUM(com.removed_lines), 0)"
                        + FROM_TABLE
                        + "  JOIN {0}_vcs.people p ON p.id = s.committer_id"
                        + WHERE, repository)
                + FILTER_BY_MAX_FILES_IN_COMMIT;

        SELECT_FILES_PATH_BY_COMMIT_ID
                = QueryUtils.getQueryForDatabase("SELECT com.file_id, com.file_path"
                        + "  FROM {0}.commits com"
                        + "  JOIN {0}_vcs.scmlog s ON s.id = com.commit_id"
                        + " WHERE com.commit_id = ?", repository)
                + FILTER_BY_MAX_FILES_IN_COMMIT
                + FILTER_BY_JAVA_OR_XML_EXTENSION_AND_IS_NOT_TEST;

        SELECT_COMMITTERS_OF_FILE
                = QueryUtils.getQueryForDatabase(
                        "SELECT DISTINCT(p.user_id)"
                        + FROM_TABLE
                        + "  JOIN {0}_vcs.people p ON p.id = s.committer_id"
                        + WHERE, repository)
                + FILTER_BY_MAX_FILES_IN_COMMIT;

        COUNT_ISSUE_REOPENED_TIMES
                = QueryUtils.getQueryForDatabase(
                        "SELECT COALESCE(COUNT(1), 0)"
                        + "  FROM {0}_issues.changes c"
                        + " WHERE c.new_value = ?"
                        + "   AND c.field = ?"
                        + "   AND c.issue_id = ?", repository);
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
        sql.append(COUNT_ISSUES_BY_FILENAME);
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

    public long calculeNumberOfIssues(
            String filename, String version) {
        List<Object> selectParams = new ArrayList<>();

        StringBuilder sql = new StringBuilder();
        sql.append(COUNT_ISSUES_BY_FILENAME);
        selectParams.add(filename);

        if (version != null) { // from begin to end
            sql.append(FILTER_BY_ISSUE_FIX_MAJOR_VERSION);
            selectParams.add(version);
        }

        sql.append(FIXED_ISSUES_ONLY);

        Long count = (Long) dao.selectNativeOneWithParams(
                sql.toString(),
                selectParams.toArray());

        return count != null ? count : 0l;
    }

    // COMMITTERS //////////////////////////////////////////////////////////////
    public long countDistinctCommittersByFilename(
            String filename, Date beginDate, Date endDate) {
        return countDistinctCommittersByFilename(filename,
                beginDate, endDate, true);
    }

    public long countDistinctCommittersByFilename(
            String filename, Date beginDate, Date endDate, boolean onlyFixed) {
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

        Long count = (Long) dao.selectNativeOneWithParams(sql.toString(), selectParams.toArray());

        return count != null ? count : 0l;
    }

    // COMMITS /////////////////////////////////////////////////////////////////
    public long countCommitsByFilename(
            String filename, Date beginDate, Date endDate, Collection<Integer> issues) {
        return countCommitsByFilename(filename, null, beginDate, endDate, issues);
    }

    public long countCommitsByFilename(
            String filename, String user, Date beginDate, Date endDate, Collection<Integer> issues) {
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

        if (user != null) {
            sql.append(FILTER_BY_USER_NAME);
            selectParams.add(user);
        }

        filterByIssues(issues, sql);

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

    public AuxCodeChurn sumCodeChurnByFilename(
            String filename, String version) {
        return sumCodeChurnByFilename(filename, null, version, null, true);
    }

    public AuxCodeChurn sumCodeChurnByFilename(
            String filename, String user, String version) {
        return sumCodeChurnByFilename(filename, user, version, null, true);
    }

    public AuxCodeChurn sumCodeChurnByFilename(
            String filename, String version, Collection<Integer> issues) {
        return sumCodeChurnByFilename(filename, null, version, issues, true);
    }

    public AuxCodeChurn sumCodeChurnByFilename(
            String filename, String user, String version, Collection<Integer> issues) {
        return sumCodeChurnByFilename(filename, user, version, issues, true);
    }

    public AuxCodeChurn sumCodeChurnByFilename(
            String fileName, String user, String version,
            Collection<Integer> issues, boolean onlyFixed) {
        List<Object> selectParams = new ArrayList<>();

        StringBuilder sql = new StringBuilder();
        sql.append(SUM_CODE_CHURN_BY_FILE_NAME);

        selectParams.add(fileName);

        sql.append(FILTER_BY_ISSUE_FIX_MAJOR_VERSION);
        selectParams.add(version);

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

    public AuxCodeChurn sumCummulativeCodeChurnByFilename(
            String filename, String version, Collection<Integer> issues) {
        return sumCodeChurnByFilename(filename, null, version, issues, true);
    }

    public AuxCodeChurn sumCummulativeCodeChurnByFilename(
            String filename, String user, String version, Collection<Integer> issues) {
        return sumCodeChurnByFilename(filename, user, version, issues, true);
    }

    public AuxCodeChurn sumCummulativeCodeChurnByFilename(
            String fileName, String user, String version,
            Collection<Integer> issues, boolean onlyFixed) {
        List<Object> selectParams = new ArrayList<>();

        StringBuilder sql = new StringBuilder();
        sql.append(SUM_CODE_CHURN_BY_FILE_NAME);

        selectParams.add(fileName);

        sql.append(FILTER_BY_BEFORE_ISSUE_FIX_MAJOR_VERSION);
        selectParams.add(version);

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
                = dao.selectNativeWithParams(SELECT_FILES_PATH_BY_COMMIT_ID, new Object[]{commitId});

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

    public long calculeIssueReopenedTimes(Integer issue) {
        Long count = (Long) dao.selectNativeOneWithParams(COUNT_ISSUE_REOPENED_TIMES, new Object[]{"Reopened", "Status", issue});

        return count;
    }
}
