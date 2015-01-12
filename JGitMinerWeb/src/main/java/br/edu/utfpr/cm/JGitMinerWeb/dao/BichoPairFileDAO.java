package br.edu.utfpr.cm.JGitMinerWeb.dao;

import static br.edu.utfpr.cm.JGitMinerWeb.dao.QueryUtils.filterByIssues;
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

    private final String FILTER_BY_ISSUE_FIX_DATE;

    private final String FILTER_BY_ISSUE_FIX_MAJOR_VERSION;
    private final String FILTER_BY_BEFORE_ISSUE_FIX_MAJOR_VERSION;

    private final String FILTER_BY_BEFORE_ISSUE_FIX_DATE;

    private final String FILTER_BY_AFTER_ISSUE_FIX_DATE;

    private final String FIXED_ISSUES_ONLY;

    private final String FILTER_BY_ISSUES_THAT_HAS_AT_LEAST_ONE_COMMENT;

    private final String FILTER_BY_USER_NAME;

    private final String FILTER_BY_ISSUE_ID;

    // commits
    private final String COUNT_ISSUES_BY_FILE_NAME;

    private final String LIST_COMMENTS_OF_FILE_PAIR_BY_DATE;

    private final String SELECT_ISSUES_OF_FILE_PAIR_BY_DATE;
    private final String SELECT_ISSUES_OF_FILE_PAIR_BY_DATE_AND_FIX_VERSION;

    private final String COUNT_COMMENTS_OF_FILE_PAIR_BY_DATE;

    private final String SUM_CHANGES_OF_FILE_PAIR_BY_DATE;

    private final String SUM_ADD_DEL_LINES_OF_FILE_PAIR_BY_DATE;
    private final String SUM_ADD_DEL_LINES_OF_FILE_PAIR_BY_DATE_AND_FIX_VERSION;

    private final String SELECT_COMMITTERS_OF_PAIR_FILE_BY_ISSUE_CREATION_DATE;

    private final String SELECT_COMMITTERS_OF_PAIR_FILE_BY_ISSUE_ID;

    private String SELECT_COMMITTERS_OF_PAIR_FILE;

    private final String COUNT_PAIR_FILE_COMMITS;

    private final String COUNT_PAIR_FILE_COMMITTERS;

    private final String COUNT_COMMENTERS_BY_ISSUE_ID;

    private final String COUNT_ISSUES;

    private final String SELECT_RELEASE_MIN_MAX_COMMIT_DATE;
    private final String SELECT_RELEASE_MIN_MAX_COMMIT_DATE_BY_FIX_VERSION;

    private final String SELECT_COMMENTS_BY_ISSUE_ID;

    private final String FILTER_BY_ISSUE_TYPE;

    private final String SELECT_ISSUES_BY_FILE_NAME;

    private final GenericBichoDAO dao;

    public BichoPairFileDAO(GenericBichoDAO dao, String repository, Integer maxFilePerCommit) {
        this.dao = dao;

        if (repository == null
                || maxFilePerCommit == null) {
            throw new IllegalArgumentException("The parameters 'repository' and 'maxFilePerCommit' can not be null.");
        }

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
                        + "SELECT ifvo.issue_id"
                        + "  FROM {0}_issues.issues_fix_version_order ifvo"
                        + " WHERE ifvo.version_order <= "
                        + "(SELECT ifvo2.version_order"
                        + "   FROM {0}_issues.issues_fix_version_order ifvo2"
                        + "  WHERE ifvo2.major_fix_version = ?))", repository);

        FILTER_BY_BEFORE_ISSUE_FIX_DATE
                = " AND c.changed_on <= ?";

        FILTER_BY_AFTER_ISSUE_FIX_DATE
                = " AND c.changed_on >= ?";

        FIXED_ISSUES_ONLY
                = " AND i.resolution = 'Fixed'"
                + " AND c.field = 'Resolution'"
                + " AND c.new_value = i.resolution";
        FILTER_BY_ISSUE_TYPE
                = " AND i.type = ?";

        FILTER_BY_ISSUES_THAT_HAS_AT_LEAST_ONE_COMMENT
                = "AND i.num_comments > 0";

        FILTER_BY_USER_NAME
                = " AND p.name = ?";

        FILTER_BY_ISSUE_ID
                = " AND i.id = ?";

        // commit
        COUNT_ISSUES
                = QueryUtils.getQueryForDatabase(
                        "SELECT COUNT(DISTINCT(i.id))"
                        + "  FROM {0}_issues.issues i"
                        + "  JOIN {0}_issues.changes c ON c.issue_id = i.id"
                        + "  JOIN {0}_issues.issues_scmlog i2s ON i2s.issue_id = i.id"
                        + "  JOIN {0}_vcs.scmlog s ON s.id = i2s.scmlog_id"
                        + "  JOIN {0}_issues.issues_scmlog i2s2 ON i2s2.issue_id = i.id"
                        + "  JOIN {0}_vcs.scmlog s2 ON s2.id = i2s2.scmlog_id"
                        + "  JOIN {0}_vcs.actions a ON a.commit_id = s.id"
                        + "  JOIN {0}_vcs.files fil ON fil.id = a.file_id"
                        + "  JOIN {0}_vcs.file_links fill ON fill.file_id = fil.id AND fill.commit_id = "
                        + "       (SELECT MAX(afill.commit_id) " // last commit where file has introduced, because it can have more than one
                        + "          FROM {0}_vcs.file_links afill "
                        + "         WHERE afill.commit_id <= s.id "
                        + "           AND afill.file_id = fil.id)"
                        + "  JOIN {0}_vcs.actions a2 ON a2.commit_id = s2.id"
                        + "  JOIN {0}_vcs.files fil2 ON fil2.id = a2.file_id AND a.file_id <> a2.file_id"
                        + "  JOIN {0}_vcs.file_links fill2 ON fill2.file_id = fil2.id AND fill2.commit_id = "
                        + "       (SELECT MAX(afill2.commit_id) " // last commit where file has introduced, because it can have more than one
                        + "          FROM {0}_vcs.file_links afill2 "
                        + "         WHERE afill2.commit_id <= s2.id "
                        + "           AND afill2.file_id = fil2.id)"
                        + " WHERE s.date > i.submitted_on"
                        + "   AND s2.date > i.submitted_on", repository)
                + FILTER_BY_MAX_FILES_IN_COMMIT;

        COUNT_ISSUES_BY_FILE_NAME
                = QueryUtils.getQueryForDatabase(
                        "SELECT COUNT(DISTINCT(i.id))"
                        + "  FROM {0}_issues.issues i"
                        + "  JOIN {0}_issues.changes c ON c.issue_id = i.id"
                        + "  JOIN {0}_issues.issues_scmlog i2s ON i2s.issue_id = i.id"
                        + "  JOIN {0}_vcs.scmlog s ON s.id = i2s.scmlog_id"
                        + "  JOIN {0}_issues.issues_scmlog i2s2 ON i2s2.issue_id = i.id"
                        + "  JOIN {0}_vcs.scmlog s2 ON s2.id = i2s2.scmlog_id"
                        + "  JOIN {0}_vcs.actions a ON a.commit_id = s.id"
                        + "  JOIN {0}_vcs.files fil ON fil.id = a.file_id"
                        + "  JOIN {0}_vcs.file_links fill ON fill.file_id = fil.id AND fill.commit_id = "
                        + "       (SELECT MAX(afill.commit_id) " // last commit where file has introduced, because it can have more than one
                        + "          FROM {0}_vcs.file_links afill "
                        + "         WHERE afill.commit_id <= s.id "
                        + "           AND afill.file_id = fil.id)"
                        + "  JOIN {0}_vcs.actions a2 ON a2.commit_id = s2.id"
                        + "  JOIN {0}_vcs.files fil2 ON fil2.id = a2.file_id AND a.file_id <> a2.file_id"
                        + "  JOIN {0}_vcs.file_links fill2 ON fill2.file_id = fil2.id AND fill2.commit_id = "
                        + "       (SELECT MAX(afill2.commit_id) " // last commit where file has introduced, because it can have more than one
                        + "          FROM {0}_vcs.file_links afill2 "
                        + "         WHERE afill2.commit_id <= s2.id "
                        + "           AND afill2.file_id = fil2.id)"
                        + " WHERE fill.file_path = ?"
                        + "   AND fill2.file_path = ? "
                        + "   AND s.date > i.submitted_on"
                        + "   AND s2.date > i.submitted_on", repository)
                + FILTER_BY_MAX_FILES_IN_COMMIT;

        SELECT_ISSUES_BY_FILE_NAME
                = QueryUtils.getQueryForDatabase(
                        "SELECT DISTINCT(i.id)"
                        + "  FROM {0}_issues.issues i"
                        + "  JOIN {0}_issues.changes c ON c.issue_id = i.id"
                        + "  JOIN {0}_issues.issues_scmlog i2s ON i2s.issue_id = i.id"
                        + "  JOIN {0}_vcs.scmlog s ON s.id = i2s.scmlog_id"
                        + "  JOIN {0}_issues.issues_scmlog i2s2 ON i2s2.issue_id = i.id"
                        + "  JOIN {0}_vcs.scmlog s2 ON s2.id = i2s2.scmlog_id"
                        + "  JOIN {0}_vcs.actions a ON a.commit_id = s.id"
                        + "  JOIN {0}_vcs.files fil ON fil.id = a.file_id"
                        + "  JOIN {0}_vcs.file_links fill ON fill.file_id = fil.id AND fill.commit_id = "
                        + "       (SELECT MAX(afill.commit_id) " // last commit where file has introduced, because it can have more than one
                        + "          FROM {0}_vcs.file_links afill "
                        + "         WHERE afill.commit_id <= s.id "
                        + "           AND afill.file_id = fil.id)"
                        + "  JOIN {0}_vcs.actions a2 ON a2.commit_id = s2.id"
                        + "  JOIN {0}_vcs.files fil2 ON fil2.id = a2.file_id AND a.file_id <> a2.file_id"
                        + "  JOIN {0}_vcs.file_links fill2 ON fill2.file_id = fil2.id AND fill2.commit_id = "
                        + "       (SELECT MAX(afill2.commit_id) " // last commit where file has introduced, because it can have more than one
                        + "          FROM {0}_vcs.file_links afill2 "
                        + "         WHERE afill2.commit_id <= s2.id "
                        + "           AND afill2.file_id = fil2.id)"
                        + " WHERE fill.file_path = ?"
                        + "   AND fill2.file_path = ? "
                        + "   AND s.date > i.submitted_on"
                        + "   AND s2.date > i.submitted_on", repository)
                + FILTER_BY_MAX_FILES_IN_COMMIT
                + FIXED_ISSUES_ONLY;

        LIST_COMMENTS_OF_FILE_PAIR_BY_DATE
                = QueryUtils.getQueryForDatabase(
                        "SELECT com.text"
                        + "  FROM {0}_issues.issues i"
                        + "  JOIN {0}_issues.issues_scmlog i2s ON i2s.issue_id = i.id"
                        + "  JOIN {0}_vcs.scmlog s ON s.id = i2s.scmlog_id"
                        + "  JOIN {0}_issues.issues_scmlog i2s2 ON i2s2.issue_id = i.id"
                        + "  JOIN {0}_vcs.scmlog s2 ON s2.id = i2s2.scmlog_id"
                        + "  JOIN {0}_issues.comments com ON com.issue_id = i.id"
                        + "  JOIN {0}_vcs.actions a ON a.commit_id = s.id"
                        + "  JOIN {0}_vcs.files fil ON fil.id = a.file_id"
                        + "  JOIN {0}_vcs.file_links fill ON fill.file_id = fil.id AND fill.commit_id = "
                        + "       (SELECT MAX(afill.commit_id) " // last commit where file has introduced, because it can have more than one
                        + "          FROM {0}_vcs.file_links afill "
                        + "         WHERE afill.commit_id <= s.id "
                        + "           AND afill.file_id = fil.id)"
                        + "  JOIN {0}_vcs.actions a2 ON a2.commit_id = s2.id"
                        + "  JOIN {0}_vcs.files fil2 ON fil2.id = a2.file_id AND a.file_id <> a2.file_id"
                        + "  JOIN {0}_vcs.file_links fill2 ON fill2.file_id = fil2.id AND fill2.commit_id = "
                        + "       (SELECT MAX(afill2.commit_id) " // last commit where file has introduced, because it can have more than one
                        + "          FROM {0}_vcs.file_links afill2 "
                        + "         WHERE afill2.commit_id <= s2.id "
                        + "           AND afill2.file_id = fil2.id)"
                        + " WHERE fill.file_path = ?"
                        + "   AND fill2.file_path = ?"
                        + "   AND s.date > i.submitted_on"
                        + "   AND s2.date > i.submitted_on", repository)
                + FILTER_BY_MAX_FILES_IN_COMMIT;

        SELECT_ISSUES_OF_FILE_PAIR_BY_DATE
                = QueryUtils.getQueryForDatabase(
                        "SELECT DISTINCT i.id, i.issue, i.description"
                        + "  FROM {0}_issues.issues i"
                        + "  JOIN {0}_issues.changes c ON c.issue_id = i.id"
                        + "  JOIN {0}_issues.issues_scmlog i2s ON i2s.issue_id = i.id"
                        + "  JOIN {0}_vcs.scmlog s ON s.id = i2s.scmlog_id"
                        + "  JOIN {0}_issues.issues_scmlog i2s2 ON i2s2.issue_id = i.id"
                        + "  JOIN {0}_vcs.scmlog s2 ON s2.id = i2s2.scmlog_id"
                        + "  JOIN {0}_vcs.actions a ON a.commit_id = s.id"
                        + "  JOIN {0}_vcs.files fil ON fil.id = a.file_id"
                        + "  JOIN {0}_vcs.file_links fill ON fill.file_id = fil.id AND fill.commit_id = "
                        + "       (SELECT MAX(afill.commit_id) " // last commit where file has introduced, because it can have more than one
                        + "          FROM {0}_vcs.file_links afill "
                        + "         WHERE afill.commit_id <= s.id "
                        + "           AND afill.file_id = fil.id)"
                        + "  JOIN {0}_vcs.actions a2 ON a2.commit_id = s2.id"
                        + "  JOIN {0}_vcs.files fil2 ON fil2.id = a2.file_id AND a.file_id <> a2.file_id"
                        + "  JOIN {0}_vcs.file_links fill2 ON fill2.file_id = fil2.id AND fill2.commit_id = "
                        + "       (SELECT MAX(afill2.commit_id) " // last commit where file has introduced, because it can have more than one
                        + "          FROM {0}_vcs.file_links afill2 "
                        + "         WHERE afill2.commit_id <= s2.id "
                        + "           AND afill2.file_id = fil2.id)"
                        + " WHERE fill.file_path = ?"
                        + "   AND fill2.file_path = ?"
                        + "   AND s.date > i.submitted_on"
                        + "   AND s2.date > i.submitted_on", repository)
                + FILTER_BY_MAX_FILES_IN_COMMIT
                + FILTER_BY_ISSUE_FIX_DATE
                + FIXED_ISSUES_ONLY;

        SELECT_ISSUES_OF_FILE_PAIR_BY_DATE_AND_FIX_VERSION
                = QueryUtils.getQueryForDatabase(
                        "SELECT DISTINCT i.id, i.issue, i.description"
                        + "  FROM {0}_issues.issues i"
                        + "  JOIN {0}_issues.changes c ON c.issue_id = i.id"
                        + "  JOIN {0}_issues.issues_scmlog i2s ON i2s.issue_id = i.id"
                        + "  JOIN {0}_vcs.scmlog s ON s.id = i2s.scmlog_id"
                        + "  JOIN {0}_issues.issues_scmlog i2s2 ON i2s2.issue_id = i.id"
                        + "  JOIN {0}_vcs.scmlog s2 ON s2.id = i2s2.scmlog_id"
                        + "  JOIN {0}_vcs.actions a ON a.commit_id = s.id"
                        + "  JOIN {0}_vcs.files fil ON fil.id = a.file_id"
                        + "  JOIN {0}_vcs.file_links fill ON fill.file_id = fil.id AND fill.commit_id = "
                        + "       (SELECT MAX(afill.commit_id) " // last commit where file has introduced, because it can have more than one
                        + "          FROM {0}_vcs.file_links afill "
                        + "         WHERE afill.commit_id <= s.id "
                        + "           AND afill.file_id = fil.id)"
                        + "  JOIN {0}_vcs.actions a2 ON a2.commit_id = s2.id"
                        + "  JOIN {0}_vcs.files fil2 ON fil2.id = a2.file_id AND a.file_id <> a2.file_id"
                        + "  JOIN {0}_vcs.file_links fill2 ON fill2.file_id = fil2.id AND fill2.commit_id = "
                        + "       (SELECT MAX(afill2.commit_id) " // last commit where file has introduced, because it can have more than one
                        + "          FROM {0}_vcs.file_links afill2 "
                        + "         WHERE afill2.commit_id <= s2.id "
                        + "           AND afill2.file_id = fil2.id)"
                        + " WHERE fill.file_path = ?"
                        + "   AND fill2.file_path = ?"
                        + "   AND s.date > i.submitted_on"
                        + "   AND s2.date > i.submitted_on", repository)
                + FILTER_BY_MAX_FILES_IN_COMMIT
                + FILTER_BY_ISSUE_FIX_MAJOR_VERSION
                + FIXED_ISSUES_ONLY;

        COUNT_COMMENTS_OF_FILE_PAIR_BY_DATE
                = QueryUtils.getQueryForDatabase(
                        "SELECT COUNT(DISTINCT(com.id))"
                        + "  FROM {0}_issues.issues i"
                        + "  JOIN {0}_issues.changes c ON c.issue_id = i.id"
                        + "  JOIN {0}_issues.issues_scmlog i2s ON i2s.issue_id = i.id"
                        + "  JOIN {0}_vcs.scmlog s ON s.id = i2s.scmlog_id"
                        + "  JOIN {0}_issues.issues_scmlog i2s2 ON i2s2.issue_id = i.id"
                        + "  JOIN {0}_vcs.scmlog s2 ON s2.id = i2s2.scmlog_id"
                        + "  JOIN {0}_issues.comments com ON com.issue_id = i.id"
                        + "  JOIN {0}_vcs.actions a ON a.commit_id = s.id"
                        + "  JOIN {0}_vcs.files fil ON fil.id = a.file_id"
                        + "  JOIN {0}_vcs.file_links fill ON fill.file_id = fil.id AND fill.commit_id = "
                        + "       (SELECT MAX(afill.commit_id) " // last commit where file has introduced, because it can have more than one
                        + "          FROM {0}_vcs.file_links afill "
                        + "         WHERE afill.commit_id <= s.id "
                        + "           AND afill.file_id = fil.id)"
                        + "  JOIN {0}_vcs.actions a2 ON a2.commit_id = s2.id"
                        + "  JOIN {0}_vcs.files fil2 ON fil2.id = a2.file_id AND a.file_id <> a2.file_id"
                        + "  JOIN {0}_vcs.file_links fill2 ON fill2.file_id = fil2.id AND fill2.commit_id = "
                        + "       (SELECT MAX(afill2.commit_id) " // last commit where file has introduced, because it can have more than one
                        + "          FROM {0}_vcs.file_links afill2 "
                        + "         WHERE afill2.commit_id <= s2.id "
                        + "           AND afill2.file_id = fil2.id)"
                        + " WHERE fill.file_path = ?"
                        + "   AND fill2.file_path = ?"
                        + "   AND s.date > i.submitted_on"
                        + "   AND s2.date > i.submitted_on", repository)
                + FILTER_BY_MAX_FILES_IN_COMMIT
                + FILTER_BY_ISSUE_FIX_DATE
                + FIXED_ISSUES_ONLY;

        SUM_CHANGES_OF_FILE_PAIR_BY_DATE
                = QueryUtils.getQueryForDatabase(
                        "SELECT COALESCE((SUM(filcl.added) + SUM(filcl.removed) "
                        + "             + SUM(filcl2.added) + SUM(filcl2.removed), 0)"
                        + "  FROM {0}_issues.issues i"
                        + "  JOIN {0}_issues.issues_scmlog i2s ON i2s.issue_id = i.id"
                        + "  JOIN {0}_vcs.scmlog s ON s.id = i2s.scmlog_id"
                        + "  JOIN {0}_issues.issues_scmlog i2s2 ON i2s2.issue_id = i.id"
                        + "  JOIN {0}_vcs.scmlog s2 ON s2.id = i2s2.scmlog_id"
                        + "  JOIN {0}_vcs.actions a ON a.commit_id = s.id"
                        + "  JOIN {0}_vcs.files fil ON fil.id = a.file_id"
                        + "  JOIN {0}_vcs.file_links fill ON fill.file_id = fil.id AND fill.commit_id = "
                        + "       (SELECT MAX(afill.commit_id) " // last commit where file has introduced, because it can have more than one
                        + "          FROM {0}_vcs.file_links afill "
                        + "         WHERE afill.commit_id <= s.id "
                        + "           AND afill.file_id = fil.id)"
                        + "  JOIN {0}_vcs.actions a2 ON a2.commit_id = s2.id"
                        + "  JOIN {0}_vcs.files fil2 ON fil2.id = a2.file_id AND a2.file_id <> a.file_id"
                        + "  JOIN {0}_vcs.file_links fill2 ON fill2.file_id = fil2.id AND fill2.commit_id = "
                        + "       (SELECT MAX(afill2.commit_id) " // last commit where file has introduced, because it can have more than one
                        + "          FROM {0}_vcs.file_links afill2 "
                        + "         WHERE afill2.commit_id <= s2.id "
                        + "           AND afill2.file_id = fil2.id)"
                        + "  JOIN {0}_vcs.commits_files_lines filcl ON filcl.commit = s.id AND filcl.path = fill.file_path"
                        + "  JOIN {0}_vcs.commits_files_lines filcl2 ON filcl2.commit = s.id AND filcl2.path = fill2.file_path"
                        + " WHERE fill.file_path = ?"
                        + "   AND fill2.file_path = ?"
                        + "   AND s.date > i.submitted_on"
                        + "   AND s2.date > i.submitted_on", repository)
                + FILTER_BY_MAX_FILES_IN_COMMIT;

        SUM_ADD_DEL_LINES_OF_FILE_PAIR_BY_DATE
                = QueryUtils.getQueryForDatabase(
                        "SELECT COALESCE(SUM(filcl.added), 0), "
                        + "      COALESCE(SUM(filcl.removed), 0)"
                        + "  FROM {0}_issues.issues i"
                        + "  JOIN {0}_issues.changes c ON c.issue_id = i.id"
                        + "  JOIN {0}_issues.issues_scmlog i2s ON i2s.issue_id = i.id"
                        + "  JOIN {0}_vcs.scmlog s ON s.id = i2s.scmlog_id"
                        + "  JOIN {0}_vcs.actions a ON a.commit_id = s.id"
                        + "  JOIN {0}_vcs.files fil ON fil.id = a.file_id"
                        + "  JOIN {0}_vcs.file_links fill ON fill.file_id = fil.id AND fill.commit_id = "
                        + "       (SELECT MAX(afill.commit_id) " // last commit where file has introduced, because it can have more than one
                        + "          FROM {0}_vcs.file_links afill "
                        + "         WHERE afill.commit_id <= s.id "
                        + "           AND afill.file_id = fil.id)"
                        + "  JOIN {0}_vcs.commits_files_lines filcl ON filcl.commit = s.id AND filcl.path = fill.file_path"
                        + " WHERE (fill.file_path = ?"
                        + "   OR fill.file_path = ?)"
                        + "   AND s.date > i.submitted_on", repository)
                + " AND s.num_files <= " + maxFilePerCommit
                + FILTER_BY_ISSUE_FIX_DATE
                + FIXED_ISSUES_ONLY;

        SUM_ADD_DEL_LINES_OF_FILE_PAIR_BY_DATE_AND_FIX_VERSION
                = QueryUtils.getQueryForDatabase(
                        "SELECT COALESCE(SUM(filcl.added), 0), "
                        + "      COALESCE(SUM(filcl.removed), 0)"
                        + "  FROM {0}_issues.issues i"
                        + "  JOIN {0}_issues.issues_fix_version ifv ON ifv.issue_id = i.id"
                        + "  JOIN {0}_issues.changes c ON c.issue_id = i.id"
                        + "  JOIN {0}_issues.issues_scmlog i2s ON i2s.issue_id = i.id"
                        + "  JOIN {0}_vcs.scmlog s ON s.id = i2s.scmlog_id"
                        + "  JOIN {0}_vcs.actions a ON a.commit_id = s.id"
                        + "  JOIN {0}_vcs.files fil ON fil.id = a.file_id"
                        + "  JOIN {0}_vcs.file_links fill ON fill.file_id = fil.id AND fill.commit_id = "
                        + "       (SELECT MAX(afill.commit_id) " // last commit where file has introduced, because it can have more than one
                        + "          FROM {0}_vcs.file_links afill "
                        + "         WHERE afill.commit_id <= s.id "
                        + "           AND afill.file_id = fil.id)"
                        + "  JOIN {0}_vcs.commits_files_lines filcl ON filcl.commit = s.id AND filcl.path = fill.file_path"
                        + " WHERE (fill.file_path = ?"
                        + "   OR fill.file_path = ?)"
                        + "   AND s.date > i.submitted_on", repository)
                + " AND s.num_files <= " + maxFilePerCommit
                + FILTER_BY_ISSUE_FIX_MAJOR_VERSION
                + FIXED_ISSUES_ONLY;

        SELECT_COMMITTERS_OF_PAIR_FILE_BY_ISSUE_CREATION_DATE
                = QueryUtils.getQueryForDatabase(
                        "SELECT DISTINCT(p.name)"
                        + "  FROM {0}_issues.issues i"
                        + "  JOIN {0}_issues.changes c ON c.issue_id = i.id"
                        + "  JOIN {0}_issues.issues_scmlog i2s ON i2s.issue_id = i.id"
                        + "  JOIN {0}_vcs.scmlog s ON s.id = i2s.scmlog_id"
                        + "  JOIN {0}_issues.issues_scmlog i2s2 ON i2s2.issue_id = i.id"
                        + "  JOIN {0}_vcs.scmlog s2 ON s2.id = i2s2.scmlog_id"
                        + "  JOIN {0}_vcs.people p ON p.id = s.committer_id"
                        + "  JOIN {0}_vcs.actions a ON a.commit_id = s.id"
                        + "  JOIN {0}_vcs.files fil ON fil.id = a.file_id"
                        + "  JOIN {0}_vcs.file_links fill ON fill.file_id = fil.id AND fill.commit_id = "
                        + "       (SELECT MAX(afill.commit_id) " // last commit where file has introduced, because it can have more than one
                        + "          FROM {0}_vcs.file_links afill "
                        + "         WHERE afill.commit_id <= s.id "
                        + "           AND afill.file_id = fil.id)"
                        + "  JOIN {0}_vcs.actions a2 ON a2.commit_id = s2.id"
                        + "  JOIN {0}_vcs.files fil2 ON fil2.id = a2.file_id AND a2.file_id <> a.file_id"
                        + "  JOIN {0}_vcs.file_links fill2 ON fill2.file_id = fil2.id AND fill2.commit_id = "
                        + "       (SELECT MAX(afill2.commit_id) " // last commit where file has introduced, because it can have more than one
                        + "          FROM {0}_vcs.file_links afill2 "
                        + "         WHERE afill2.commit_id <= s2.id "
                        + "           AND afill2.file_id = fil2.id)"
                        + " WHERE fill.file_path = ?"
                        + "   AND fill2.file_path = ?"
                        + "   AND s.date > i.submitted_on"
                        + "   AND s2.date > i.submitted_on", repository)
                + FILTER_BY_MAX_FILES_IN_COMMIT;

        SELECT_COMMITTERS_OF_PAIR_FILE_BY_ISSUE_ID
                = QueryUtils.getQueryForDatabase(
                        "SELECT DISTINCT(p.name)"
                        + "  FROM {0}_issues.issues i"
                        + "  JOIN {0}_issues.changes c ON c.issue_id = i.id"
                        + "  JOIN {0}_issues.issues_scmlog i2s ON i2s.issue_id = i.id"
                        + "  JOIN {0}_vcs.scmlog s ON s.id = i2s.scmlog_id"
                        + "  JOIN {0}_issues.issues_scmlog i2s2 ON i2s2.issue_id = i.id"
                        + "  JOIN {0}_vcs.scmlog s2 ON s2.id = i2s2.scmlog_id"
                        + "  JOIN {0}_vcs.people p ON p.id = s.committer_id"
                        + "  JOIN {0}_vcs.actions a ON a.commit_id = s.id"
                        + "  JOIN {0}_vcs.files fil ON fil.id = a.file_id"
                        + "  JOIN {0}_vcs.file_links fill ON fill.file_id = fil.id AND fill.commit_id = "
                        + "       (SELECT MAX(afill.commit_id) " // last commit where file has introduced, because it can have more than one
                        + "          FROM {0}_vcs.file_links afill "
                        + "         WHERE afill.commit_id <= s.id "
                        + "           AND afill.file_id = fil.id)"
                        + "  JOIN {0}_vcs.actions a2 ON a2.commit_id = s2.id"
                        + "  JOIN {0}_vcs.files fil2 ON fil2.id = a2.file_id AND a2.file_id <> a.file_id"
                        + "  JOIN {0}_vcs.file_links fill2 ON fill2.file_id = fil2.id AND fill2.commit_id = "
                        + "       (SELECT MAX(afill2.commit_id) " // last commit where file has introduced, because it can have more than one
                        + "          FROM {0}_vcs.file_links afill2 "
                        + "         WHERE afill2.commit_id <= s2.id "
                        + "           AND afill2.file_id = fil2.id)"
                        + " WHERE fill.file_path = ?"
                        + "   AND fill2.file_path = ?"
                        + "   AND s.date > i.submitted_on"
                        + "   AND s2.date > i.submitted_on", repository)
                + FILTER_BY_ISSUE_ID
                + FIXED_ISSUES_ONLY;

        SELECT_COMMITTERS_OF_PAIR_FILE
                = QueryUtils.getQueryForDatabase(
                        "SELECT DISTINCT(p.name)"
                        + "  FROM {0}_issues.issues i"
                        + "  JOIN {0}_issues.changes c ON c.issue_id = i.id"
                        + "  JOIN {0}_issues.issues_scmlog i2s ON i2s.issue_id = i.id"
                        + "  JOIN {0}_vcs.scmlog s ON s.id = i2s.scmlog_id"
                        + "  JOIN {0}_issues.issues_scmlog i2s2 ON i2s2.issue_id = i.id"
                        + "  JOIN {0}_vcs.scmlog s2 ON s2.id = i2s2.scmlog_id"
                        + "  JOIN {0}_vcs.people p ON p.id = s.committer_id"
                        + "  JOIN {0}_vcs.actions a ON a.commit_id = s.id"
                        + "  JOIN {0}_vcs.files fil ON fil.id = a.file_id"
                        + "  JOIN {0}_vcs.file_links fill ON fill.file_id = fil.id AND fill.commit_id = "
                        + "       (SELECT MAX(afill.commit_id) " // last commit where file has introduced, because it can have more than one
                        + "          FROM {0}_vcs.file_links afill "
                        + "         WHERE afill.commit_id <= s.id "
                        + "           AND afill.file_id = fil.id)"
                        + "  JOIN {0}_vcs.actions a2 ON a2.commit_id = s2.id"
                        + "  JOIN {0}_vcs.files fil2 ON fil2.id = a2.file_id AND a2.file_id <> a.file_id"
                        + "  JOIN {0}_vcs.file_links fill2 ON fill2.file_id = fil2.id AND fill2.commit_id = "
                        + "       (SELECT MAX(afill2.commit_id) " // last commit where file has introduced, because it can have more than one
                        + "          FROM {0}_vcs.file_links afill2 "
                        + "         WHERE afill2.commit_id <= s2.id "
                        + "           AND afill2.file_id = fil2.id)"
                        + " WHERE fill.file_path = ?"
                        + "   AND fill2.file_path = ?"
                        + "   AND s.date > i.submitted_on"
                        + "   AND s2.date > i.submitted_on", repository)
                + FIXED_ISSUES_ONLY;

        COUNT_PAIR_FILE_COMMITS
                = QueryUtils.getQueryForDatabase(
                        "SELECT COUNT(DISTINCT(s.id))"
                        + "  FROM {0}_issues.issues i"
                        + "  JOIN {0}_issues.changes c ON c.issue_id = i.id"
                        + "  JOIN {0}_issues.issues_scmlog i2s ON i2s.issue_id = i.id"
                        + "  JOIN {0}_vcs.scmlog s ON s.id = i2s.scmlog_id"
                        + "  JOIN {0}_issues.issues_scmlog i2s2 ON i2s2.issue_id = i.id"
                        + "  JOIN {0}_vcs.scmlog s2 ON s2.id = i2s2.scmlog_id"
                        + "  JOIN {0}_vcs.people p ON p.id = s.committer_id"
                        + "  JOIN {0}_vcs.actions a ON a.commit_id = s.id"
                        + "  JOIN {0}_vcs.files fil ON fil.id = a.file_id"
                        + "  JOIN {0}_vcs.file_links fill ON fill.file_id = fil.id AND fill.commit_id = "
                        + "       (SELECT MAX(afill.commit_id) " // last commit where file has introduced, because it can have more than one
                        + "          FROM {0}_vcs.file_links afill "
                        + "         WHERE afill.commit_id <= s.id "
                        + "           AND afill.file_id = fil.id)"
                        + "  JOIN {0}_vcs.actions a2 ON a2.commit_id = s2.id"
                        + "  JOIN {0}_vcs.files fil2 ON fil2.id = a2.file_id AND a2.file_id <> a.file_id"
                        + "  JOIN {0}_vcs.file_links fill2 ON fill2.file_id = fil2.id AND fill2.commit_id = "
                        + "       (SELECT MAX(afill2.commit_id) " // last commit where file has introduced, because it can have more than one
                        + "          FROM {0}_vcs.file_links afill2 "
                        + "         WHERE afill2.commit_id <= s2.id "
                        + "           AND afill2.file_id = fil2.id)"
                        + " WHERE fill.file_path = ?"
                        + "   AND fill2.file_path = ?"
                        + "   AND s.date > i.submitted_on"
                        + "   AND s2.date > i.submitted_on", repository)
                + FILTER_BY_MAX_FILES_IN_COMMIT;

        COUNT_PAIR_FILE_COMMITTERS
                = QueryUtils.getQueryForDatabase(
                        "SELECT COUNT(DISTINCT(p.name))"
                        + "  FROM {0}_issues.issues i"
                        + "  JOIN {0}_issues.changes c ON c.issue_id = i.id"
                        + "  JOIN {0}_issues.issues_scmlog i2s ON i2s.issue_id = i.id"
                        + "  JOIN {0}_vcs.scmlog s ON s.id = i2s.scmlog_id"
                        + "  JOIN {0}_issues.issues_scmlog i2s2 ON i2s2.issue_id = i.id"
                        + "  JOIN {0}_vcs.scmlog s2 ON s2.id = i2s2.scmlog_id"
                        + "  JOIN {0}_vcs.people p ON p.id = s.committer_id"
                        + "  JOIN {0}_vcs.actions a ON a.commit_id = s.id"
                        + "  JOIN {0}_vcs.files fil ON fil.id = a.file_id"
                        + "  JOIN {0}_vcs.file_links fill ON fill.file_id = fil.id AND fill.commit_id = "
                        + "       (SELECT MAX(afill.commit_id) " // last commit where file has introduced, because it can have more than one
                        + "          FROM {0}_vcs.file_links afill "
                        + "         WHERE afill.commit_id <= s.id "
                        + "           AND afill.file_id = fil.id)"
                        + "  JOIN {0}_vcs.actions a2 ON a2.commit_id = s2.id"
                        + "  JOIN {0}_vcs.files fil2 ON fil2.id = a2.file_id AND a2.file_id <> a.file_id"
                        + "  JOIN {0}_vcs.file_links fill2 ON fill2.file_id = fil2.id AND fill2.commit_id = "
                        + "       (SELECT MAX(afill2.commit_id) " // last commit where file has introduced, because it can have more than one
                        + "          FROM {0}_vcs.file_links afill2 "
                        + "         WHERE afill2.commit_id <= s2.id "
                        + "           AND afill2.file_id = fil2.id)"
                        + " WHERE fill.file_path = ?"
                        + "   AND fill2.file_path = ?"
                        + "   AND s.date > i.submitted_on"
                        + "   AND s2.date > i.submitted_on", repository)
                + FILTER_BY_MAX_FILES_IN_COMMIT;

        COUNT_COMMENTERS_BY_ISSUE_ID
                = QueryUtils.getQueryForDatabase(
                        "SELECT COUNT(DISTINCT(p.user_id))"
                        + "  FROM {0}_issues.issues i"
                        + "  JOIN {0}_issues.changes c ON c.issue_id = i.id"
                        + "  JOIN {0}_issues.comments com ON com.issue_id = i.id"
                        + "  JOIN {0}_issues.people p ON p.id = c.submitted_by"
                        + " WHERE i.id = ?", repository);

        SELECT_RELEASE_MIN_MAX_COMMIT_DATE
                = QueryUtils.getQueryForDatabase(
                        "SELECT MIN(s.date), MAX(s.date)"
                        + "  FROM {0}_issues.issues i"
                        + "  JOIN {0}_issues.changes c ON c.issue_id = i.id"
                        + "  JOIN {0}_issues.issues_scmlog i2s ON i2s.issue_id = i.id"
                        + "  JOIN {0}_vcs.scmlog s ON s.id = i2s.scmlog_id"
                        + "  JOIN {0}_issues.issues_scmlog i2s2 ON i2s2.issue_id = i.id"
                        + "  JOIN {0}_vcs.scmlog s2 ON s2.id = i2s2.scmlog_id"
                        + "  JOIN {0}_vcs.actions a ON a.commit_id = s.id"
                        + "  JOIN {0}_vcs.files fil ON fil.id = a.file_id"
                        + "  JOIN {0}_vcs.file_links fill ON fill.file_id = fil.id"
                        // neste caso não é necessário
                        // temos que considerar todos os arquivos, inclusive quando renomeados
                        //                        + " AND fill.commit_id = "
                        //                        + "       (SELECT MAX(afill.commit_id) " // last commit where file has introduced, because it can have more than one
                        //                        + "          FROM {0}_vcs.file_links afill "
                        //                        + "         WHERE afill.commit_id <= s.id "
                        //                        + "           AND afill.file_id = fil.id)"
                        + "  JOIN {0}_vcs.actions a2 ON a2.commit_id = s2.id"
                        + "  JOIN {0}_vcs.files fil2 ON fil2.id = a2.file_id AND a.file_id <> a2.file_id"
                        + "  JOIN {0}_vcs.file_links fill2 ON fill2.file_id = fil2.id"
                        // neste caso não é necessário
                        // temos que considerar todos os arquivos, inclusive quando renomeados
                        //                        + " AND fill2.commit_id = "
                        //                        + "       (SELECT MAX(afill2.commit_id) " // last commit where file has introduced, because it can have more than one
                        //                        + "          FROM {0}_vcs.file_links afill2 "
                        //                        + "         WHERE afill2.commit_id <= s2.id "
                        //                        + "           AND afill2.file_id = fil2.id)"
                        + " WHERE fill.file_path = ?"
                        + "   AND fill2.file_path = ?"
                        + "   AND s.date > i.submitted_on"
                        + "   AND s2.date > i.submitted_on", repository)
                + FILTER_BY_MAX_FILES_IN_COMMIT;

        SELECT_RELEASE_MIN_MAX_COMMIT_DATE_BY_FIX_VERSION
                = QueryUtils.getQueryForDatabase(
                        "SELECT MIN(s.date), MAX(s.date)"
                        + "  FROM {0}_issues.issues i"
                        + "  JOIN {0}_issues.changes c ON c.issue_id = i.id"
                        + "  JOIN {0}_issues.issues_scmlog i2s ON i2s.issue_id = i.id"
                        + "  JOIN {0}_vcs.scmlog s ON s.id = i2s.scmlog_id"
                        + "  JOIN {0}_issues.issues_scmlog i2s2 ON i2s2.issue_id = i.id"
                        + "  JOIN {0}_vcs.scmlog s2 ON s2.id = i2s2.scmlog_id"
                        + "  JOIN {0}_vcs.actions a ON a.commit_id = s.id"
                        + "  JOIN {0}_vcs.files fil ON fil.id = a.file_id"
                        + "  JOIN {0}_vcs.file_links fill ON fill.file_id = fil.id"
                        // neste caso não é necessário
                        // temos que considerar todos os arquivos, inclusive quando renomeados
                        //                        + " AND fill.commit_id = "
                        //                        + "       (SELECT MAX(afill.commit_id) " // last commit where file has introduced, because it can have more than one
                        //                        + "          FROM {0}_vcs.file_links afill "
                        //                        + "         WHERE afill.commit_id <= s.id "
                        //                        + "           AND afill.file_id = fil.id)"
                        + "  JOIN {0}_vcs.actions a2 ON a2.commit_id = s2.id"
                        + "  JOIN {0}_vcs.files fil2 ON fil2.id = a2.file_id AND a.file_id <> a2.file_id"
                        + "  JOIN {0}_vcs.file_links fill2 ON fill2.file_id = fil2.id"
                        // neste caso não é necessário
                        // temos que considerar todos os arquivos, inclusive quando renomeados
                        //                        + " AND fill2.commit_id = "
                        //                        + "       (SELECT MAX(afill2.commit_id) " // last commit where file has introduced, because it can have more than one
                        //                        + "          FROM {0}_vcs.file_links afill2 "
                        //                        + "         WHERE afill2.commit_id <= s2.id "
                        //                        + "           AND afill2.file_id = fil2.id)"
                        + " WHERE fill.file_path = ?"
                        + "   AND fill2.file_path = ?"
                        + "   AND s.date > i.submitted_on"
                        + "   AND s2.date > i.submitted_on", repository)
                + FILTER_BY_MAX_FILES_IN_COMMIT;

        SELECT_COMMENTS_BY_ISSUE_ID = QueryUtils.getQueryForDatabase(
                "SELECT com.text FROM {0}_issues.comments com WHERE com.issue_id = ?", repository);
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

    public long calculeNumberOfIssues(
            String file, String file2, Date beginDate, Date endDate, boolean onlyFixed) {
        return calculeNumberOfIssues(file, file2, beginDate, endDate, null, onlyFixed);
    }

    public long calculeNumberOfIssues(
            String file, String file2, Date beginDate, Date endDate, String type, boolean onlyFixed) {

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

        if (file == null || file2 == null) {
            throw new IllegalArgumentException("The file and file2 parameters can not be null.");
        }

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
        if (file == null || file2 == null) {
            throw new IllegalArgumentException("Pair file could not be null");
        }

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
        if (file == null || file2 == null) {
            throw new IllegalArgumentException("Pair file could not be null");
        }

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
        if (file == null || file2 == null) {
            throw new IllegalArgumentException("Pair file could not be null");
        }

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
        if (file == null || file2 == null) {
            throw new IllegalArgumentException("Pair file could not be null");
        }

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
        return calculeCommits(file, file2, null, fixVersion, null, true);
    }

    public long calculeCommits(String file, String file2, String fixVersion,
            Collection<Integer> issues) {
        return calculeCommits(file, file2, null, fixVersion, issues, true);
    }

    public long calculeCommits(String file, String file2, String fixVersion,
            boolean onlyFixed) {
        return calculeCommits(file, file2, null, fixVersion, null, onlyFixed);
    }

    public long calculeCommits(String file, String file2, String user, String fixVersion) {
        return calculeCommits(file, file2, user, fixVersion, null, true);
    }

    public long calculeCommits(String file, String file2, String user,
            String fixVersion, Collection<Integer> issues) {
        return calculeCommits(file, file2, user, fixVersion, issues, true);
    }

    public long calculeCommits(
            String file, String file2, String user, String fixVersion,
            Collection<Integer> issues, boolean onlyFixed) {
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

        sql.append(FILTER_BY_ISSUE_FIX_MAJOR_VERSION);
        selectParams.add(fixVersion);

        if (user != null) {
            sql.append(FILTER_BY_USER_NAME);
            selectParams.add(user);
        }

        filterByIssues(issues, sql);

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

        sql.append(FILTER_BY_ISSUE_FIX_MAJOR_VERSION);
        selectParams.add(fixVersion);

        filterByIssues(issues, sql);

        Long count = dao.selectNativeOneWithParams(sql.toString(), selectParams.toArray());

        return count != null ? count : 0l;
    }

    public long calculeCummulativeCommitters(
            String file, String file2, String fixVersion,
            Collection<Integer> issues) {
        return calculeCummulativeCommitters(file, file2, fixVersion, issues, true);
    }

    public long calculeCummulativeCommitters(
            String file, String file2, String fixVersion,
            Collection<Integer> issues, boolean onlyFixed) {
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

        sql.append(FILTER_BY_BEFORE_ISSUE_FIX_MAJOR_VERSION);
        selectParams.add(fixVersion);

        filterByIssues(issues, sql);

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

    public Collection<AuxWordiness> listIssues(String file, String file2,
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

        Map<Integer, AuxWordiness> cache = new HashMap<>(rawIssues.size());
        for (Object[] issue : rawIssues) {
            Integer issueNumber = (Integer) issue[0];

            List<String> comments = dao.selectNativeWithParams(SELECT_COMMENTS_BY_ISSUE_ID, new Object[]{issueNumber});
            AuxWordiness comment = new AuxWordiness(
                    issueNumber,
                    (String) issue[1],
                    (String) issue[2],
                    comments);
            cache.put(issueNumber, comment);
        }

        return cache.values();
    }

    public Collection<AuxWordiness> listIssues(String file, String file2,
            String fixVersion, Collection<Integer> issues) {
        List<Object> selectParams = new ArrayList<>();

        StringBuilder sql = new StringBuilder();
        sql.append(SELECT_ISSUES_OF_FILE_PAIR_BY_DATE_AND_FIX_VERSION);

        selectParams.add(file);
        selectParams.add(file2);
        selectParams.add(fixVersion);

        filterByIssues(issues, sql);

        List<Object[]> rawIssues = dao.selectNativeWithParams(sql.toString(), selectParams.toArray());

        Map<Integer, AuxWordiness> cache = new HashMap<>(rawIssues.size());
        for (Object[] issue : rawIssues) {
            Integer issueNumber = (Integer) issue[0];

            List<String> comments = dao.selectNativeWithParams(SELECT_COMMENTS_BY_ISSUE_ID, new Object[]{issueNumber});
            AuxWordiness comment = new AuxWordiness(
                    issueNumber,
                    (String) issue[1],
                    (String) issue[2],
                    comments);
            cache.put(issueNumber, comment);
        }

        return cache.values();
    }

    public List<EntityComment> listComments(String file, String file2, Date beginDate, Date endDate, boolean onlyFixed) {
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

    public int calculeTotalPairFileDaysAge(String file, String file2, Date endDate, boolean onlyFixed) {
        return calculePairFileDaysAge(file, file2, null, endDate, onlyFixed);
    }

    public int calculePairFileDaysAge(String file, String file2, Date beginDate, Date endDate, boolean onlyFixed) {
        if (file == null || file2 == null) {
            throw new IllegalArgumentException("The file and file2 parameters can not be null.");
        }

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

    public int calculeTotalPairFileDaysAge(String file, String file2, String fixVersion, boolean onlyFixed) {
        if (file == null || file2 == null) {
            throw new IllegalArgumentException("The file and file2 parameters can not be null.");
        }

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

    public int calculePairFileDaysAge(String file, String file2, String fixVersion, boolean onlyFixed) {
        if (file == null || file2 == null) {
            throw new IllegalArgumentException("The file and file2 parameters can not be null.");
        }

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
}
