package br.edu.utfpr.cm.JGitMinerWeb.dao;

import static br.edu.utfpr.cm.JGitMinerWeb.dao.QueryUtils.filterByIssues;
import br.edu.utfpr.cm.minerador.services.matrix.model.FilePath;
import br.edu.utfpr.cm.minerador.services.metric.committer.Committer;
import br.edu.utfpr.cm.minerador.services.metric.model.CodeChurn;
import br.edu.utfpr.cm.minerador.services.metric.model.Commit;
import br.edu.utfpr.cm.minerador.services.metric.model.File;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.joda.time.Days;
import org.joda.time.LocalDate;

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
    private final String FILTER_BY_BEFORE_ISSUE_FIX_MAJOR_VERSION_INCLUSIVE;
    private final String FILTER_BY_BEFORE_ISSUE_FIX_MAJOR_VERSION_EXCLUSIVE;
    private final String FILTER_BY_BEFORE_ISSUE_FIX_DATE_OF_ISSUE_ID;

    private final String FILTER_BY_COMMIT;

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

    private final String ORDER_BY_SUBMITTED_ON;

    // complete queries (commons fragment + specific fragments for query)
    private final String COUNT_DISTINCT_COMMITERS_BY_FILE_NAME;

    private final String COUNT_COMMITS_BY_FILE_NAME;

    private final String COUNT_ISSUES_BY_FILENAME;

    private final String SUM_ADD_AND_DEL_LINES_BY_FILE_NAME;

    private final String SELECT_ISSUES_BY_FIX_MAJOR_VERSION;

    private final String SELECT_FILES_PATH_BY_COMMIT_ID;

    private final String SELECT_COMMITTERS_OF_FILE_BY_DATE;
    private final String SELECT_COMMITTERS_OF_FILE_BY_FIX_MAJOR_VERSION;
    private final String COUNT_COMMITTERS_OF_FILE;

    private final String COUNT_ISSUE_REOPENED_TIMES;

    private final String COUNT_ISSUES_TYPES;
    private final String SELECT_RELEASE_MIN_MAX_COMMIT_DATE;

    private final String SELECT_LAST_COMMITTER_BEFORE_ISSUE;

    private final String SELECT_COMMIT_AND_FILES_BY_FILENAME_AND_ISSUE;
    private final String FILTER_BY_ISSUE_ID;

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
        FILTER_BY_BEFORE_ISSUE_FIX_MAJOR_VERSION_INCLUSIVE
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

        // avoid join, because has poor performance in this case
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

        FILTER_BY_BEFORE_ISSUE_FIX_DATE_OF_ISSUE_ID
                = QueryUtils.getQueryForDatabase(
                        " AND c.changed_on <="
                        + "     (SELECT MAX(c2.changed_on)"
                        + "        FROM {0}_issues.changes c2"
                        + "       WHERE c2.issue_id = ?"
                        + "         AND c2.field = \"Resolution\""
                        + "         AND c2.new_value = \"Fixed\")", repository);

        FILTER_BY_COMMIT
                = " AND s.id = ?";

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

        FILTER_BY_ISSUE_ID
                = " AND i.id = ?";

        ORDER_BY_SUBMITTED_ON
                = " ORDER BY i.submitted_on";

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

        SUM_ADD_AND_DEL_LINES_BY_FILE_NAME
                = QueryUtils.getQueryForDatabase(
                        "SELECT COALESCE(SUM(com.added_lines), 0),"
                        + "       COALESCE(SUM(com.removed_lines), 0)"
                        + FROM_TABLE
                        + "  JOIN {0}_vcs.people p ON p.id = s.committer_id"
                        + WHERE, repository)
                + FILTER_BY_MAX_FILES_IN_COMMIT;

        SELECT_ISSUES_BY_FIX_MAJOR_VERSION
                = QueryUtils.getQueryForDatabase(
                        "SELECT DISTINCT i.id"
                        + FROM_TABLE
                        + WHERE, repository)
                + FILTER_BY_MAX_FILES_IN_COMMIT
                + FILTER_BY_ISSUE_FIX_MAJOR_VERSION
                + FIXED_ISSUES_ONLY;

        SELECT_FILES_PATH_BY_COMMIT_ID
                = QueryUtils.getQueryForDatabase(
                        "SELECT com.file_id, com.file_path"
                        + "  FROM {0}.commits com"
                        + "  JOIN {0}_vcs.scmlog s ON s.id = com.commit_id"
                        + " WHERE com.commit_id = ?", repository)
                + FILTER_BY_MAX_FILES_IN_COMMIT
                + FILTER_BY_JAVA_OR_XML_EXTENSION_AND_IS_NOT_TEST;

        SELECT_COMMITTERS_OF_FILE_BY_DATE
                = QueryUtils.getQueryForDatabase(
                        "SELECT DISTINCT(p.name)"
                        + FROM_TABLE
                        + "  JOIN {0}_vcs.people p ON p.id = s.committer_id"
                        + WHERE, repository)
                + FILTER_BY_MAX_FILES_IN_COMMIT;

        SELECT_COMMITTERS_OF_FILE_BY_FIX_MAJOR_VERSION
                = QueryUtils.getQueryForDatabase(
                        "SELECT DISTINCT p.id, p.name, p.email"
                        + FROM_TABLE
                        + "  JOIN {0}_vcs.people p ON p.id = s.committer_id"
                        + WHERE, repository)
                + FILTER_BY_MAX_FILES_IN_COMMIT
                + FILTER_BY_ISSUE_FIX_MAJOR_VERSION;

        COUNT_COMMITTERS_OF_FILE
                = QueryUtils.getQueryForDatabase(
                        "SELECT COUNT(DISTINCT(p.name))"
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

        COUNT_ISSUES_TYPES
                = QueryUtils.getQueryForDatabase(""
                        + "SELECT COALESCE(COUNT(DISTINCT(i.id)), 0) AS count, i.type"
                        + FROM_TABLE
                        + WHERE
                        + FILTER_BY_MAX_FILES_IN_COMMIT
                        + FILTER_BY_ISSUE_FIX_MAJOR_VERSION
                        + " GROUP BY i.type", repository);

        SELECT_RELEASE_MIN_MAX_COMMIT_DATE
                = QueryUtils.getQueryForDatabase(
                        "SELECT MIN(com.date), MAX(com.date)"
                        + FROM_TABLE
                        + WHERE, repository)
                + FILTER_BY_MAX_FILES_IN_COMMIT;

        SELECT_LAST_COMMITTER_BEFORE_ISSUE
                = QueryUtils.getQueryForDatabase(
                        "SELECT DISTINCT p.id, p.name, p.email"
                        + FROM_TABLE
                        + "  JOIN {0}_vcs.people p ON p.id = s.committer_id"
                        + WHERE
                        + FIXED_ISSUES_ONLY
                        + FILTER_BY_BEFORE_ISSUE_FIX_MAJOR_VERSION_INCLUSIVE
                        + " AND s.date < "
                        + "     (SELECT MAX(s2.date) FROM {0}_vcs.scmlog s2"
                        + "       WHERE s2.id = ?)"
                        + " ORDER BY s.date DESC LIMIT 1", repository);

        SELECT_COMMIT_AND_FILES_BY_FILENAME_AND_ISSUE
                = QueryUtils.getQueryForDatabase(
                        "SELECT DISTINCT com.commit_id, com.file_path, p.id, p.name, p.email"
                        + FROM_TABLE
                        + "  JOIN {0}_vcs.people p ON p.id = s.committer_id"
                        + WHERE, repository)
                + FILTER_BY_ISSUE_ID
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
    public CodeChurn sumCodeChurnByFilename(
            String filename, Date beginDate, Date endDate) {
        return sumCodeChurnByFilename(filename, null, beginDate, endDate, null, true);
    }

    public CodeChurn sumCodeChurnByFilename(
            String filename, String user, Date beginDate, Date endDate) {
        return sumCodeChurnByFilename(filename, user, beginDate, endDate, null, true);
    }

    public CodeChurn sumCodeChurnByFilename(
            String filename, Date beginDate, Date endDate, Collection<Integer> issues) {
        return sumCodeChurnByFilename(filename, null, beginDate, endDate, issues, true);
    }

    public CodeChurn sumCodeChurnByFilename(
            String filename, String user, Date beginDate, Date endDate, Collection<Integer> issues) {
        return sumCodeChurnByFilename(filename, user, beginDate, endDate, issues, true);
    }

    public CodeChurn sumCodeChurnByFilename(
            String fileName, String user, Date beginDate, Date endDate,
            Collection<Integer> issues, boolean onlyFixed) {
        List<Object> selectParams = new ArrayList<>();

        StringBuilder sql = new StringBuilder();
        sql.append(SUM_ADD_AND_DEL_LINES_BY_FILE_NAME);

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

        return new CodeChurn(fileName,
                ((BigDecimal) sum.get(0)[0]).longValue(), ((BigDecimal) sum.get(0)[1]).longValue());
    }

    public CodeChurn sumCodeChurnByFilename(
            String filename, String version) {
        return sumCodeChurnByFilename(filename, null, version, null, true);
    }

    public CodeChurn sumCodeChurnByFilename(
            String filename, String user, String version) {
        return sumCodeChurnByFilename(filename, user, version, null, true);
    }

    public CodeChurn sumCodeChurnByFilename(
            String filename, String version, Collection<Integer> issues) {
        return sumCodeChurnByFilename(filename, null, version, issues, true);
    }

    public CodeChurn sumCodeChurnByFilename(
            String filename, String user, String version, Collection<Integer> issues) {
        return sumCodeChurnByFilename(filename, user, version, issues, true);
    }

    public CodeChurn sumCodeChurnByFilename(
            String fileName, String user, String version,
            Collection<Integer> issues, boolean onlyFixed) {
        List<Object> selectParams = new ArrayList<>();

        StringBuilder sql = new StringBuilder();
        sql.append(SUM_ADD_AND_DEL_LINES_BY_FILE_NAME);

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

        return new CodeChurn(fileName,
                ((BigDecimal) sum.get(0)[0]).longValue(), ((BigDecimal) sum.get(0)[1]).longValue());
    }

    public CodeChurn sumCummulativeCodeChurnByFilename(
            String filename, String version, Collection<Integer> issues) {
        return sumCodeChurnByFilename(filename, null, version, issues, true);
    }

    public CodeChurn sumCummulativeCodeChurnByFilename(
            String filename, String user, String version, Collection<Integer> issues) {
        return sumCodeChurnByFilename(filename, user, version, issues, true);
    }

    public CodeChurn sumCummulativeCodeChurnByFilename(
            String fileName, String user, String version,
            Collection<Integer> issues, boolean onlyFixed) {
        List<Object> selectParams = new ArrayList<>();

        StringBuilder sql = new StringBuilder();
        sql.append(SUM_ADD_AND_DEL_LINES_BY_FILE_NAME);

        selectParams.add(fileName);

        sql.append(FILTER_BY_BEFORE_ISSUE_FIX_MAJOR_VERSION_INCLUSIVE);
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

        return new CodeChurn(fileName,
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
        sql.append(SELECT_COMMITTERS_OF_FILE_BY_DATE);

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

    public Set<Committer> selectCommitters(String file, String fixVersion) {

        List<Object> selectParams = new ArrayList<>();
        StringBuilder sql = new StringBuilder();
        sql.append(SELECT_COMMITTERS_OF_FILE_BY_FIX_MAJOR_VERSION);
        sql.append(FIXED_ISSUES_ONLY);
        selectParams.add(file);
        selectParams.add(fixVersion);

        List<Object[]> committers = dao.selectNativeWithParams(
                sql.toString(), selectParams.toArray());

        Set<Committer> commitersList = new HashSet<>(committers.size());
        for (Object row[] : committers) {
            Committer committer = null;
            if (row != null) {
                Integer committerId = (Integer) row[0];
                String committerName = (String) row[1];
                String committerEmail = (String) row[2];

                committer = new Committer(committerId, committerName, committerEmail);
            }
            commitersList.add(committer);
        }

        return commitersList;
    }

    public Long calculeCommitters(
            String file, String fixVersion) {
        return calculeCommitters(file, null, fixVersion);
    }

    public Long calculeCommitters(
            String file, Integer issue, String fixVersion) {

        List<Object> selectParams = new ArrayList<>();

        StringBuilder sql = new StringBuilder();

        sql.append(COUNT_COMMITTERS_OF_FILE);
        selectParams.add(file);

        sql.append(FILTER_BY_ISSUE_FIX_MAJOR_VERSION);
        selectParams.add(fixVersion);

        sql.append(FIXED_ISSUES_ONLY);

        if (issue != null) {
            sql.append(FILTER_BY_ISSUE);
            selectParams.add(issue);
        }

        Long committers = dao.selectNativeOneWithParams(
                sql.toString(), selectParams.toArray());

        return committers;
    }

    public Long calculeCummulativeCommitters(
            String file, Integer issue, String fixVersion) {

        List<Object> selectParams = new ArrayList<>();

        StringBuilder sql = new StringBuilder();

        sql.append(COUNT_COMMITTERS_OF_FILE);
        selectParams.add(file);

        sql.append(FILTER_BY_BEFORE_ISSUE_FIX_MAJOR_VERSION_EXCLUSIVE);
        selectParams.add(fixVersion);

        sql.append(FIXED_ISSUES_ONLY);

        Long committers = dao.selectNativeOneWithParams(
                sql.toString(), selectParams.toArray());

        return committers;
    }

    public long calculeIssueReopenedTimes(Integer issue) {
        Long count = (Long) dao.selectNativeOneWithParams(COUNT_ISSUE_REOPENED_TIMES, new Object[]{"Reopened", "Status", issue});

        return count;
    }

    public Map<String, Long> calculeNumberOfIssuesGroupedByType(String filename, String version) {
        List<Object[]> rawFilesPath
                = dao.selectNativeWithParams(COUNT_ISSUES_TYPES, new Object[]{filename, version});

        Map<String, Long> result = new HashMap<>(rawFilesPath.size());
        for (Object[] row : rawFilesPath) {
            Long count = (Long) row[0];
            String type = (String) row[1];
            result.put(type, count);
        }
        return result;
    }

    public CodeChurn calculeAddDelChanges(String filename, Integer issue, String version) {
        return calculeAddDelChanges(filename, issue, null, version);
    }

    public CodeChurn calculeAddDelChanges(String filename, Integer issue, Integer commit, String version) {
        List<Object> selectParams = new ArrayList<>();

        StringBuilder sql = new StringBuilder();

        sql.append(SUM_ADD_AND_DEL_LINES_BY_FILE_NAME);
        selectParams.add(filename);

        sql.append(FILTER_BY_ISSUE_FIX_MAJOR_VERSION);
        selectParams.add(version);

        if (commit != null) {
            sql.append(FILTER_BY_COMMIT);
            selectParams.add(commit);
        }

        if (issue != null) {
            sql.append(FILTER_BY_ISSUE);
            selectParams.add(issue);
        }

        sql.append(FIXED_ISSUES_ONLY);

        List<Object[]> sum = dao.selectNativeWithParams(sql.toString(),
                selectParams.toArray());
        Long additions = sum.get(0)[0] == null ? 0l : ((BigDecimal) sum.get(0)[0]).longValue();
        Long deletions = sum.get(0)[1] == null ? 0l : ((BigDecimal) sum.get(0)[1]).longValue();

        return new CodeChurn(filename, additions, deletions);
    }

    public List<Integer> selectIssues(String filename, String fixVersion) {
        List<Object> selectParams = new ArrayList<>();

        StringBuilder sql = new StringBuilder();
        sql.append(SELECT_ISSUES_BY_FIX_MAJOR_VERSION);
        selectParams.add(filename);
        selectParams.add(fixVersion);

        sql.append(ORDER_BY_SUBMITTED_ON);

        List<Integer> issues = dao.selectNativeWithParams(sql.toString(), selectParams.toArray());

        return issues;
    }

    public long calculeCummulativeCommits(String file, String fixVersion) {
        List<Object> selectParams = new ArrayList<>();

        StringBuilder sql = new StringBuilder();
        sql.append(COUNT_COMMITS_BY_FILE_NAME);

        selectParams.add(file);

        sql.append(FILTER_BY_BEFORE_ISSUE_FIX_MAJOR_VERSION_EXCLUSIVE);
        selectParams.add(fixVersion);

        Long count = dao.selectNativeOneWithParams(sql.toString(), selectParams.toArray());

        return count != null ? count : 0l;
    }

    public long calculeCommits(String file, String fixVersion) {
        return calculeCommitsByFixVersion(file, null, null, fixVersion, null);
    }

    public long calculeCommits(String file, Integer issue, String fixVersion) {
        return calculeCommitsByFixVersion(file, null, issue, fixVersion, null);
    }

    public long calculeCommits(String file, String fixVersion,
            Collection<Integer> issues) {
        return calculeCommitsByFixVersion(file, null, null, fixVersion, issues);
    }

    public long calculeCommits(String file, String user, String fixVersion) {
        return calculeCommitsByFixVersion(file, user, null, fixVersion, null);
    }

    public long calculeCommits(String file, String user,
            String fixVersion, Collection<Integer> issues) {
        return calculeCommitsByFixVersion(file, user, null, fixVersion, issues);
    }

    public long calculeCommitsByFixVersion(
            String file, String user, Integer issue, String fixVersion,
            Collection<Integer> issues) {
        List<Object> selectParams = new ArrayList<>();

        StringBuilder sql = new StringBuilder();

        sql.append(COUNT_COMMITS_BY_FILE_NAME);
        selectParams.add(file);

        sql.append(FILTER_BY_ISSUE_FIX_MAJOR_VERSION);
        selectParams.add(fixVersion);

        sql.append(FIXED_ISSUES_ONLY);

        if (issue != null) {
            sql.append(FILTER_BY_ISSUE);
            selectParams.add(issue);
        }

        if (user != null) {
            sql.append(FILTER_BY_USER_NAME);
            selectParams.add(user);
        }

        filterByIssues(issues, sql);

        Long count = dao.selectNativeOneWithParams(sql.toString(), selectParams.toArray());

        return count != null ? count : 0l;
    }

    public int calculeFileAgeInDays(String filename, String fixVersion) {
        return calculeFileAgeInDays(filename, null, fixVersion);
    }

    public int calculeFileAgeInDays(String filename, Integer issue, String fixVersion) {
        List<Object> selectParams = new ArrayList<>();

        StringBuilder sql = new StringBuilder();
        sql.append(SELECT_RELEASE_MIN_MAX_COMMIT_DATE);
        sql.append(FIXED_ISSUES_ONLY);

        selectParams.add(filename);

        sql.append(FILTER_BY_ISSUE_FIX_MAJOR_VERSION);
        selectParams.add(fixVersion);

        if (issue != null) {
            sql.append(FILTER_BY_BEFORE_ISSUE_FIX_DATE_OF_ISSUE_ID);
            selectParams.add(issue);
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

    public int calculeTotalFileAgeInDays(String filename, String fixVersion) {
        return calculeTotalFileAgeInDays(filename, null, fixVersion);
    }

    public int calculeTotalFileAgeInDays(String filename, Integer issue, String fixVersion) {
        List<Object> selectParams = new ArrayList<>();

        StringBuilder sql = new StringBuilder();
        sql.append(SELECT_RELEASE_MIN_MAX_COMMIT_DATE);
        sql.append(FIXED_ISSUES_ONLY);

        selectParams.add(filename);

        sql.append(FILTER_BY_BEFORE_ISSUE_FIX_MAJOR_VERSION_EXCLUSIVE);
        selectParams.add(fixVersion);

        if (issue != null) {
            sql.append(FILTER_BY_BEFORE_ISSUE_FIX_DATE_OF_ISSUE_ID);
            selectParams.add(issue);
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

    public Committer selectLastCommitter(String filename, Commit commit, String fixVersion) {

        Object[] row
                = dao.selectNativeOneWithParams(SELECT_LAST_COMMITTER_BEFORE_ISSUE,
                        new Object[]{filename, fixVersion, commit.getId()});

        Committer committer = null;
        if (row != null) {
            Integer committerId = (Integer) row[0];
            String committerName = (String) row[1];
            String committerEmail = (String) row[2];

            committer = new Committer(committerId, committerName, committerEmail);
        }

        return committer;
    }

    public Set<Commit> selectFilesAndCommitByFileAndIssue(String filename, Integer issue) {
        List<Object[]> rawFilesPath
                = dao.selectNativeWithParams(SELECT_COMMIT_AND_FILES_BY_FILENAME_AND_ISSUE, new Object[]{filename, issue});

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

}
