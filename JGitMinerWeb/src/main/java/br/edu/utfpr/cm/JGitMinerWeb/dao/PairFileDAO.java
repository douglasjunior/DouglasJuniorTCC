package br.edu.utfpr.cm.JGitMinerWeb.dao;

import br.edu.utfpr.cm.JGitMinerWeb.model.miner.EntityComment;
import br.edu.utfpr.cm.JGitMinerWeb.model.miner.EntityRepository;
import br.edu.utfpr.cm.minerador.services.metric.model.IssueMetrics;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.joda.time.Days;
import org.joda.time.LocalDate;

/**
 *
 * @author Rodrigo T. Kuroda
 */
public class PairFileDAO {

    private static final String CALCULE_SUM_UPDATES_OF_TWO_FILE
            = "SELECT count(1)"
            + "  FROM gitrepositorycommit rc,"
            + "       gitcommit c,"
            + "       gitcommituser u,"
            + "       gitcommitfile fil"
            + " WHERE rc.commit_id = c.id"
            + "   AND c.committer_id = u.id"
            + "   AND fil.repositorycommit_id = rc.id"
            + "   AND rc.repository_id = ?"
            + "   AND u.datecommituser BETWEEN ? AND ?"
            + "   AND (fil.filename = ? OR fil.filename = ?)";

    private static final String CALCULE_UPDATES
            = "SELECT count(distinct(rc.id)) FROM "
            + "  gitcommitfile fil, gitcommitfile fil2, "
            + "  gitpullrequest_gitrepositorycommit prc,"
            + "  gitpullrequest_gitrepositorycommit prc2,"
            + "  gitrepositorycommit rc,"
            + "  gitrepositorycommit rc2,"
            + "  gitcommit c,"
            + "  gitcommit c2,"
            + "  gitcommituser u,"
            + "  gitcommituser u2"
            + " WHERE fil.repositorycommit_id = prc.repositorycommits_id"
            + "   AND fil.repositorycommit_id = rc.id"
            + "   AND rc.commit_id = c.id"
            + "   AND u.id = c.committer_id"
            + "   AND fil2.repositorycommit_id = prc2.repositorycommits_id"
            + "   AND fil2.repositorycommit_id = rc2.id"
            + "   AND rc2.commit_id = c2.id"
            + "   AND u2.id = c2.committer_id"
            + "   AND fil.filename <> fil2.filename"
            + "   AND prc.entitypullrequest_id = prc2.entitypullrequest_id"
            + "   AND rc.repository_id = ?"
            + "   AND u.datecommituser BETWEEN ? AND ?"
            + "   AND u2.datecommituser BETWEEN ? AND ?"
            + "   AND fil.filename = ?"
            + "   AND fil2.filename = ?";

    private static final String SELECT_PULL_REQUEST_BY_DATE = "SELECT count(distinct(pul.id)) "
            + " FROM gitpullrequest pul "
            + " WHERE pul.repository_id = ? "
            + "   AND pul.createdat BETWEEN ? AND ? ";

    private static final String SELECT_PULL_REQUEST_AND_ISSUE_BY_DATE = "SELECT count(distinct(pul.id)) "
            + " FROM gitpullrequest pul, gitissue iss "
            + " WHERE pul.repository_id = ? "
            + "   AND iss.id = pul.issue_id "
            + "   AND iss.commentscount > 1 "
            + "   AND pul.createdat BETWEEN ? AND ? ";

    private static final String SELECT_PULL_REQUEST_BY_NUMBER = "SELECT count(1) "
            + " FROM gitpullrequest pul "
            + " WHERE pul.repository_id = ? "
            + "   AND pul.number BETWEEN ? AND ? ";

    private static final String FILTER_BY_PULL_REQUEST_CREATION_DATE
            = " AND pul.createdat BETWEEN ? AND ? ";

    private static final String FILTER_BY_BEFORE_PULL_REQUEST_CREATION_DATE
            = " AND pul.createdat <= ? ";

    private static final String FILTER_BY_AFTER_PULL_REQUEST_CREATION_DATE
            = " AND pul.createdat >= ? ";

    private static final String MERGED_PULL_REQUEST_ONLY
            = " AND pul.mergedat IS NOT NULL ";

    private static final String EXISTS_FILE1_IN_PULL_REQUEST = " AND EXISTS "
            + "(SELECT 1 "
            + "FROM gitpullrequest_gitrepositorycommit r, "
            + "     gitcommitfile f "
            + "WHERE r.entitypullrequest_id = pul.id "
            + "  AND f.repositorycommit_id = r.repositorycommits_id "
            + "  AND f.filename = ?) ";

    private static final String EXISTS_FILE2_IN_PULL_REQUEST = " AND EXISTS "
            + "(SELECT 1  "
            + "FROM gitpullrequest_gitrepositorycommit r2, "
            + "     gitcommitfile f2 "
            + "WHERE r2.entitypullrequest_id = pul.id "
            + "  AND f2.repositorycommit_id = r2.repositorycommits_id "
            + "  AND f2.filename = ?) ";

    private static final String LIST_COMMENTS_OF_FILE_PAIR_BY_DATE = "SELECT * "
            + "FROM gitpullrequest pul "
            + "  JOIN gitissue i ON i.id = pul.issue_id "
            + "  JOIN gitcomment c ON c.issue_id = i.id "
            + "WHERE pul.repository_id = ? "
            + "  AND pul.createdat BETWEEN ? AND ? ";

    private static final String LIST_ISSUES_OF_FILE_PAIR_BY_DATE
            = "SELECT pul.number, i.url, i.body "
            + "  FROM gitpullrequest pul "
            + "  JOIN gitissue i ON i.id = pul.issue_id "
            + " WHERE pul.repository_id = ? "
            + "   AND i.commentsCount > 0 "
            + "   AND pul.createdat BETWEEN ? AND ? ";

    private static final String LIST_COMMENTS_BY_PULL_REQUEST_NUMBER
            = "SELECT c.body "
            + "  FROM gitcomment c "
            + "  JOIN gitissue i ON i.id = c.issue_id "
            + "  JOIN gitpullrequest pul ON i.id = pul.issue_id "
            + " WHERE pul.number = ?"
            + "   AND i.commentsCount > 0 ";

    private static final String COUNT_COMMENTS_OF_FILE_PAIR_BY_DATE = "SELECT SUM(i.commentscount) "
            + "FROM gitpullrequest pul "
            + "  JOIN gitissue i ON i.id = pul.issue_id "
            + "WHERE pul.repository_id = ? "
            + "  AND pul.createdat BETWEEN ? AND ? ";

    private static final String COUNT_COMMENTS_OF_FILE_PAIR_BY_PULL_REQUEST_NUMBER = "SELECT SUM(i.commentscount) "
            + "FROM gitpullrequest pul "
            + "  JOIN gitissue i ON i.id = pul.issue_id "
            + "WHERE pul.repository_id = ? "
            + "  AND pul.number BETWEEN ? AND ? ";

    private static final String SELECT_SUM_OF_CHANGES_OF_FILE_PAIR_BY_PULL_REQUEST_NUMBER
            = "SELECT (sum(fil.changes) + sum(fil2.changes)) AS codeChurn"
            + "  FROM gitpullrequest pul,"
            + "       gitcommitfile fil, gitcommitfile fil2,"
            + "       gitpullrequest_gitrepositorycommit prc,"
            + "       gitpullrequest_gitrepositorycommit prc2"
            + " WHERE prc.entitypullrequest_id = pul.id"
            + "   AND fil.repositorycommit_id = prc.repositorycommits_id"
            + "   AND prc2.entitypullrequest_id = pul.id"
            + "   AND fil2.repositorycommit_id = prc2.repositorycommits_id"
            + "   AND fil.filename <> fil2.filename"
            + "   AND prc.entitypullrequest_id = prc2.entitypullrequest_id"
            + "   AND pul.mergedat IS NOT NULL"
            + "   AND pul.repository_id = ?"
            + "   AND fil.filename = ?"
            + "   AND fil2.filename = ?"
            + "   AND pul.number between ? AND ?";

    private static final String SELECT_SUM_OF_CHANGES_OF_FILE_PAIR_BY_DATE
            = "SELECT (sum(fil.changes) + sum(fil2.changes)) AS codeChurn"
            + "  FROM gitpullrequest pul,"
            + "       gitcommitfile fil, gitcommitfile fil2,"
            + "       gitpullrequest_gitrepositorycommit prc,"
            + "       gitpullrequest_gitrepositorycommit prc2"
            + " WHERE prc.entitypullrequest_id = pul.id"
            + "   AND fil.repositorycommit_id = prc.repositorycommits_id"
            + "   AND prc2.entitypullrequest_id = pul.id"
            + "   AND fil2.repositorycommit_id = prc2.repositorycommits_id"
            + "   AND fil.filename <> fil2.filename"
            + "   AND prc.entitypullrequest_id = prc2.entitypullrequest_id"
            + "   AND pul.mergedat IS NOT NULL"
            + "   AND pul.repository_id = ?"
            + "   AND fil.filename = ?"
            + "   AND fil2.filename = ?"
            + "   AND pul.createdat between ? AND ?";

    private static final String SELECT_SUM_OF_ADD_DEL_CHANGES_OF_FILE_PAIR_BY_DATE
            = "SELECT (sum(fil.additions) + sum(fil2.additions)) AS additions,"
            + "       (sum(fil.deletions) + sum(fil2.deletions)) AS deletions,"
            + "       (sum(fil.changes) + sum(fil2.changes)) AS codeChurn"
            + "  FROM gitpullrequest pul,"
            + "       gitcommitfile fil, gitcommitfile fil2,"
            + "       gitpullrequest_gitrepositorycommit prc,"
            + "       gitpullrequest_gitrepositorycommit prc2"
            + " WHERE prc.entitypullrequest_id = pul.id"
            + "   AND fil.repositorycommit_id = prc.repositorycommits_id"
            + "   AND prc2.entitypullrequest_id = pul.id"
            + "   AND fil2.repositorycommit_id = prc2.repositorycommits_id"
            + "   AND fil.filename <> fil2.filename"
            + "   AND prc.entitypullrequest_id = prc2.entitypullrequest_id"
            + "   AND pul.mergedat IS NOT NULL"
            + "   AND pul.repository_id = ?"
            + "   AND fil.filename = ?"
            + "   AND fil2.filename = ?"
            + "   AND pul.createdat between ? AND ?";


    private static final String SELECT_PAIR_FILE_COMMITTERS
            = "SELECT u.name, u.email FROM " // u ou u2 retornam a mesma coisa
            + "  gitpullrequest pul, gitissue i,"
            + "  gitcommitfile fil, gitcommitfile fil2, "
            + "  gitpullrequest_gitrepositorycommit prc,"
            + "  gitpullrequest_gitrepositorycommit prc2,"
            + "  gitrepositorycommit rc,"
            + "  gitrepositorycommit rc2,"
            + "  gitcommit c,"
            + "  gitcommit c2,"
            + "  gitcommituser u,"
            + "  gitcommituser u2"
            + " WHERE pul.issue_id = i.id"
            + "   AND prc.entitypullrequest_id = pul.id"
            + "   AND fil.repositorycommit_id = prc.repositorycommits_id"
            + "   AND fil.repositorycommit_id = rc.id"
            + "   AND rc.commit_id = c.id"
            + "   AND u.id = c.committer_id"
            + "   AND prc2.entitypullrequest_id = pul.id"
            + "   AND fil2.repositorycommit_id = prc2.repositorycommits_id"
            + "   AND fil2.repositorycommit_id = rc2.id"
            + "   AND rc2.commit_id = c2.id"
            + "   AND u2.id = c2.committer_id"
            + "   AND fil.filename <> fil2.filename"
            + "   AND u.email = u2.email"
            + "   AND u.name = u2.name"
            + "   AND prc.entitypullrequest_id = prc2.entitypullrequest_id"
            + "   AND pul.repository_id = ?"
            + "   AND fil.filename = ?"
            + "   AND fil2.filename = ?";

    private static final String COUNT_PAIR_FILE_COMMITS
            = "SELECT count(distinct(prc.repositorycommits_id)) FROM "
            + "  gitpullrequest pul, gitissue i,"
            + "  gitcommitfile fil, gitcommitfile fil2, "
            + "  gitpullrequest_gitrepositorycommit prc,"
            + "  gitpullrequest_gitrepositorycommit prc2,"
            + "  gitrepositorycommit rc,"
            + "  gitrepositorycommit rc2,"
            + "  gitcommit c,"
            + "  gitcommit c2,"
            + "  gitcommituser u,"
            + "  gitcommituser u2"
            + " WHERE pul.issue_id = i.id"
            + "   AND prc.entitypullrequest_id = pul.id"
            + "   AND fil.repositorycommit_id = prc.repositorycommits_id"
            + "   AND fil.repositorycommit_id = rc.id"
            + "   AND rc.commit_id = c.id"
            + "   AND u.id = c.committer_id"
            + "   AND prc2.entitypullrequest_id = pul.id"
            + "   AND fil2.repositorycommit_id = prc2.repositorycommits_id"
            + "   AND fil2.repositorycommit_id = rc2.id"
            + "   AND rc2.commit_id = c2.id"
            + "   AND u2.id = c2.committer_id"
            + "   AND fil.filename <> fil2.filename"
            + "   AND prc.entitypullrequest_id = prc2.entitypullrequest_id"
            + "   AND pul.repository_id = ?"
            + "   AND fil.filename = ?"
            + "   AND fil2.filename = ?";

    private static final String COUNT_PAIR_FILE_COMMITTERS
            = "SELECT count(distinct(coalesce(nullif(trim(u.email), ''), u.name))) FROM "
            + "  gitpullrequest pul, gitissue i,"
            + "  gitcommitfile fil, gitcommitfile fil2, "
            + "  gitpullrequest_gitrepositorycommit prc,"
            + "  gitpullrequest_gitrepositorycommit prc2,"
            + "  gitrepositorycommit rc,"
            + "  gitrepositorycommit rc2,"
            + "  gitcommit c,"
            + "  gitcommit c2,"
            + "  gitcommituser u,"
            + "  gitcommituser u2"
            + " WHERE pul.issue_id = i.id"
            + "   AND prc.entitypullrequest_id = pul.id"
            + "   AND fil.repositorycommit_id = prc.repositorycommits_id"
            + "   AND fil.repositorycommit_id = rc.id"
            + "   AND rc.commit_id = c.id"
            + "   AND u.id = c.committer_id"
            + "   AND prc2.entitypullrequest_id = pul.id"
            + "   AND fil2.repositorycommit_id = prc2.repositorycommits_id"
            + "   AND fil2.repositorycommit_id = rc2.id"
            + "   AND rc2.commit_id = c2.id"
            + "   AND u2.id = c2.committer_id"
            + "   AND fil.filename <> fil2.filename"
            + "   AND prc.entitypullrequest_id = prc2.entitypullrequest_id"
            + "   AND pul.repository_id = ?"
            + "   AND fil.filename = ?"
            + "   AND fil2.filename = ?";

    private static final String FILTER_BY_USER_NAME_OR_EMAIL
            = "   AND ((u.email = ? AND u2.email = ?)"
            + "    OR (u.name = ? AND u2.name = ?))";

    private static final String SELECT_COMMITTERS_X_COMMITS_PER_ISSUE
            = "SELECT count(distinct(u.email)), count(distinct(rc.id)) FROM "
            + "	 gitpullrequest pul, gitissue i,"
            + "	 gitcommitfile fil, gitcommitfile fil2, "
            + "	 gitpullrequest_gitrepositorycommit prc,"
            + "	 gitpullrequest_gitrepositorycommit prc2,"
            + "	 gitrepositorycommit rc,"
            + "	 gitrepositorycommit rc2,"
            + "  gitcommit c,"
            + "  gitcommit c2,"
            + "	 gitcommituser u,"
            + "	 gitcommituser u2"
            + " WHERE pul.issue_id = i.id"
            + "   AND prc.entitypullrequest_id = pul.id"
            + "   AND rc.id = prc.repositorycommits_id"
            + "   AND fil.repositorycommit_id = rc.id"
            + "   AND rc.commit_id = c.id"
            + "   AND u.id = c.committer_id"
            + "   AND prc2.entitypullrequest_id = pul.id"
            + "   AND rc2.id = prc2.repositorycommits_id"
            + "   AND fil2.repositorycommit_id = rc2.id"
            + "   AND rc2.commit_id = c2.id"
            + "   AND u2.id = c2.committer_id"
            + "   AND fil.filename <> fil2.filename"
            + "   AND prc.entitypullrequest_id = prc2.entitypullrequest_id"
            + "   AND pul.repository_id = ?"
            + MERGED_PULL_REQUEST_ONLY
            + FILTER_BY_PULL_REQUEST_CREATION_DATE
            + "   AND fil.filename = ?"
            + "   AND fil2.filename = ?"
            + " GROUP BY i.id";

    private static final String SELECT_RELEASE_MIN_MAX_DATE_CREATION
            = "SELECT min(u.datecommituser), max(u.datecommituser) "
            + "  FROM gitpullrequest pul, gitissue i,"
            + "       gitcommitfile fil, gitcommitfile fil2, "
            + "       gitpullrequest_gitrepositorycommit prc,"
            + "       gitrepositorycommit rc,       "
            + "       gitcommit c,"
            + "       gitcommituser u,"
            + "       gitpullrequest_gitrepositorycommit prc2,"
            + "       gitrepositorycommit rc2,"
            + "       gitcommituser u2,"
            + "       gitcommit c2"
            + " WHERE pul.issue_id = i.id"
            + "   AND prc.entitypullrequest_id = pul.id"
            + "   AND rc.id = prc.repositorycommits_id"
            + "   AND fil.repositorycommit_id = rc.id"
            + "   AND rc.commit_id = c.id"
            + "   AND c.committer_id = u.id"
            + "   AND prc2.entitypullrequest_id = pul.id"
            + "   AND rc2.id = prc2.repositorycommits_id"
            + "   AND fil2.repositorycommit_id = rc2.id"
            + "   AND rc2.commit_id = c2.id"
            + "   AND c2.committer_id = u2.id"
            + "   AND fil.filename <> fil2.filename"
            + "   AND prc.entitypullrequest_id = prc2.entitypullrequest_id"
            + "   AND pul.repository_id = ?"
            + "   AND fil.filename = ?"
            + "   AND fil2.filename = ?";

    private static final String BEGIN_DATE
            = " AND pul.createdat >= ?";
    private static final String END_DATE
            = " AND pul.createdat <= ?";

    private final GenericDao dao;

    public PairFileDAO(GenericDao dao) {
        this.dao = dao;
    }

    public Long calculeSumUpdatesOfTwoFile(EntityRepository repository,
            String fileName, String fileName2,
            Date beginDate, Date endDate) {
        if (beginDate == null || endDate == null) {
            return 0l;
        }

        Object[] queryParams = new Object[]{
            repository,
            fileName,
            fileName2,
            beginDate,
            endDate
        };

        return dao.selectNativeOneWithParams(CALCULE_SUM_UPDATES_OF_TWO_FILE, queryParams);
    }

    public Long calculeUpdates(EntityRepository repository,
            String fileName, String fileName2,
            Date beginDate, Date endDate) {
        if (beginDate == null || endDate == null) {
            return 0l;
        }

        Object[] queryParams = new Object[]{
            repository,
            beginDate,
            endDate,
            beginDate,
            endDate,
            fileName,
            fileName2
        };

        return dao.selectNativeOneWithParams(CALCULE_UPDATES, queryParams);
    }

    public long calculeNumberOfPullRequest(EntityRepository repository, 
            String file, String file2, Date beginDate, Date endDate, boolean onlyMergeds) {
        List<Object> selectParams = new ArrayList<>();

        StringBuilder sql = new StringBuilder();
        sql.append(SELECT_PULL_REQUEST_BY_DATE);

        if (onlyMergeds) {
            sql.append(MERGED_PULL_REQUEST_ONLY);
        }

        selectParams.add(repository.getId());
        selectParams.add(beginDate);
        selectParams.add(endDate);

        if (file != null) {
            sql.append(EXISTS_FILE1_IN_PULL_REQUEST);
            selectParams.add(file);
        }

        if (file2 != null) {
            sql.append(EXISTS_FILE2_IN_PULL_REQUEST);
            selectParams.add(file2);
        }

        Long count = (Long) dao.selectNativeOneWithParams(sql.toString(), selectParams.toArray());

        return count != null ? count : 0l;
    }

    public long calculeNumberOfPullRequest(EntityRepository repository, 
            String file, String file2, Long beginNumber, Long endNumber, boolean onlyMergeds) {
        List<Object> selectParams = new ArrayList<>();

        StringBuilder sql = new StringBuilder();
        sql.append(SELECT_PULL_REQUEST_BY_NUMBER);

        if (onlyMergeds) {
            sql.append(MERGED_PULL_REQUEST_ONLY);
        }

        selectParams.add(repository.getId());
        selectParams.add(beginNumber);
        selectParams.add(endNumber);

        if (file != null) {
            sql.append(EXISTS_FILE1_IN_PULL_REQUEST);
            selectParams.add(file);
        }

        if (file2 != null) {
            sql.append(EXISTS_FILE2_IN_PULL_REQUEST);
            selectParams.add(file2);
        }

        Long count = dao.selectNativeOneWithParams(sql.toString(), selectParams.toArray());

        return count != null ? count : 0l;
    }

    public long calculeComments(EntityRepository repository, 
            String file, String file2, Date beginDate, Date endDate, boolean onlyMergeds) {
        List<Object> selectParams = new ArrayList<>();

        StringBuilder sql = new StringBuilder();
        sql.append(COUNT_COMMENTS_OF_FILE_PAIR_BY_DATE);

        if (onlyMergeds) {
            sql.append(MERGED_PULL_REQUEST_ONLY);
        }

        selectParams.add(repository.getId());
        selectParams.add(beginDate);
        selectParams.add(endDate);

        if (file != null) {
            sql.append(EXISTS_FILE1_IN_PULL_REQUEST);
            selectParams.add(file);
        }

        if (file2 != null) {
            sql.append(EXISTS_FILE2_IN_PULL_REQUEST);
            selectParams.add(file2);
        }

        Long count = dao.selectNativeOneWithParams(sql.toString(), selectParams.toArray());

        return count != null ? count : 0l;
    }

    public long calculeComments(EntityRepository repository, 
            String file, String file2, Long beginNumber, Long endNumber, boolean onlyMergeds) {
        List<Object> selectParams = new ArrayList<>();

        StringBuilder sql = new StringBuilder();
        sql.append(COUNT_COMMENTS_OF_FILE_PAIR_BY_PULL_REQUEST_NUMBER);

        if (onlyMergeds) {
            sql.append(MERGED_PULL_REQUEST_ONLY);
        }

        selectParams.add(repository.getId());
        selectParams.add(beginNumber);
        selectParams.add(endNumber);

        if (file != null) {
            sql.append(EXISTS_FILE1_IN_PULL_REQUEST);
            selectParams.add(file);
        }

        if (file2 != null) {
            sql.append(EXISTS_FILE2_IN_PULL_REQUEST);
            selectParams.add(file2);
        }

        Long count = dao.selectNativeOneWithParams(sql.toString(), selectParams.toArray());

        return count != null ? count : 0l;
    }

    public Long calculeCodeChurn(EntityRepository repository, 
            String fileName, String fileName2, Long beginNumber, Long endNumber) {

        Object[] bdObjects = new Object[]{
            repository.getId(),
            fileName,
            fileName2,
            beginNumber,
            endNumber
        };

        Long sum = dao.selectNativeOneWithParams(
                SELECT_SUM_OF_CHANGES_OF_FILE_PAIR_BY_PULL_REQUEST_NUMBER, bdObjects);

        return sum;
    }

    public Long calculeCodeChurn(EntityRepository repository, 
            String fileName, String fileName2, Date beginDate, Date endDate) {

        Object[] bdObjects = new Object[]{
            repository.getId(),
            fileName,
            fileName2,
            beginDate,
            endDate
        };

        Long sum = dao.selectNativeOneWithParams(
                SELECT_SUM_OF_CHANGES_OF_FILE_PAIR_BY_DATE, bdObjects);

        return sum;
    }

    public AuxCodeChurn calculeCodeChurnAddDelChange(EntityRepository repository,
            String fileName, String fileName2, Date beginDate, Date endDate) {

        Object[] params = new Object[]{
            repository.getId(),
            fileName,
            fileName2,
            beginDate,
            endDate
        };

        List<Object[]> sum = dao.selectNativeWithParams(
                SELECT_SUM_OF_ADD_DEL_CHANGES_OF_FILE_PAIR_BY_DATE, params);

        Long additions = sum.get(0)[0] == null ? 0 : (Long) sum.get(0)[0];
        Long deletions = sum.get(0)[1] == null ? 0 : (Long) sum.get(0)[1];
        Long changes = sum.get(0)[2] == null ? 0 : (Long) sum.get(0)[2];

        return new AuxCodeChurn(fileName, fileName2,
                additions, deletions, changes);
    }

    public Set<AuxUser> selectCommitters(EntityRepository repository,
            String file, String file2, Date beginDate, Date endDate) {
        return selectCommitters(repository, file, file2, beginDate, endDate, true);
    }

    public Set<AuxUser> selectCommitters(EntityRepository repository,
            String file, String file2, Date beginDate, Date endDate, boolean onlyMergeds) {

        List<Object> selectParams = new ArrayList<>();
        if (file == null || file2 == null) {
            throw new IllegalArgumentException("Pair file could not be null");
        }

        StringBuilder sql = new StringBuilder();
        sql.append(SELECT_PAIR_FILE_COMMITTERS);

        selectParams.add(repository.getId());
        selectParams.add(file);
        selectParams.add(file2);

        if (onlyMergeds) {
            sql.append(MERGED_PULL_REQUEST_ONLY);
        }

        if (beginDate != null) {
            sql.append(FILTER_BY_AFTER_PULL_REQUEST_CREATION_DATE);
            selectParams.add(beginDate);
        }

        if (endDate != null) {
            sql.append(FILTER_BY_BEFORE_PULL_REQUEST_CREATION_DATE);
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

    public long calculeCommits(EntityRepository repository,
            String file, String file2) {
        return calculeCommits(repository, file, file2, null, null, null, true);
    }

    public long calculeCommits(EntityRepository repository,
            String file, String file2, Date beginDate, Date endDate) {
        return calculeCommits(repository, file, file2, null, beginDate, endDate, true);
    }

    public long calculeCommits(EntityRepository repository,
            String file, String file2, Date beginDate, Date endDate, boolean onlyMergeds) {
        return calculeCommits(repository, file, file2, null, beginDate, endDate, onlyMergeds);
    }

    public long calculeCommits(EntityRepository repository,
            String file, String file2, String user, Date beginDate, Date endDate) {
        return calculeCommits(repository, file, file2, user, beginDate, endDate, true);
    }

    public long calculeCommits(EntityRepository repository,
            String file, String file2, String user, Date beginDate, Date endDate, boolean onlyMergeds) {
        List<Object> selectParams = new ArrayList<>();

        if (file == null || file2 == null) {
            throw new IllegalArgumentException("Pair file could not be null");
        }

        StringBuilder sql = new StringBuilder();
        sql.append(COUNT_PAIR_FILE_COMMITS);

        selectParams.add(repository.getId());
        selectParams.add(file);
        selectParams.add(file2);

        if (onlyMergeds) {
            sql.append(MERGED_PULL_REQUEST_ONLY);
        }

        if (beginDate != null && endDate != null) {
            sql.append(FILTER_BY_PULL_REQUEST_CREATION_DATE);
            selectParams.add(beginDate);
            selectParams.add(endDate);
        }

        if (user != null) {
            sql.append(FILTER_BY_USER_NAME_OR_EMAIL);
            selectParams.add(user); // commiter email of file 1
            selectParams.add(user); // commiter email of file 2
            selectParams.add(user); // commiter name of file 1
            selectParams.add(user); // commiter name of file 2
        }

        Long count = dao.selectNativeOneWithParams(sql.toString(), selectParams.toArray());

        return count != null ? count : 0l;
    }

    public long calculeCommitters(EntityRepository repository,
            String file, String file2) {
        return calculeCommitters(repository, file, file2, null, null, true);
    }

    public long calculeCommitters(EntityRepository repository,
            String file, String file2, Date beginDate, Date endDate) {
        return calculeCommitters(repository, file, file2, beginDate, endDate, true);
    }

    public long calculeCommitters(EntityRepository repository,
            String file, String file2, Date beginDate, Date endDate, boolean onlyMergeds) {
        List<Object> selectParams = new ArrayList<>();

        if (file == null || file2 == null) {
            throw new IllegalArgumentException("Pair file could not be null");
        }

        StringBuilder sql = new StringBuilder();
        sql.append(COUNT_PAIR_FILE_COMMITTERS);

        selectParams.add(repository.getId());
        selectParams.add(file);
        selectParams.add(file2);

        if (onlyMergeds) {
            sql.append(MERGED_PULL_REQUEST_ONLY);
        }

        if (beginDate != null) {
            sql.append(FILTER_BY_AFTER_PULL_REQUEST_CREATION_DATE);
            selectParams.add(beginDate);
        }

        if (endDate != null) {
            sql.append(FILTER_BY_BEFORE_PULL_REQUEST_CREATION_DATE);
            selectParams.add(endDate);
        }

        Long count = dao.selectNativeOneWithParams(sql.toString(), selectParams.toArray());

        return count != null ? count : 0l;
    }

    public final long[][] calculeCommittersXCommits(EntityRepository repository,
            String file, String file2, Date beginDate, Date endDate) {
        List<Object> selectParams = new ArrayList<>();

        if (file == null || file2 == null) {
            throw new IllegalArgumentException("Pair file could not be null");
        }

        selectParams.add(repository.getId());
        selectParams.add(beginDate);
        selectParams.add(endDate);
        selectParams.add(file);
        selectParams.add(file2);

        List<Object[]> list = dao.selectNativeWithParams(SELECT_COMMITTERS_X_COMMITS_PER_ISSUE, selectParams.toArray());

        final long[][] matrix = new long[list.size()][2];
        for (int i = 0; i < list.size(); i++) {
            matrix[i][0] = (Long) list.get(i)[0]; // committers
            matrix[i][1] = (Long) list.get(i)[1]; // commits
        }

        return matrix;
    }

    public List<IssueMetrics> listIssues(EntityRepository repository, String file, String file2, Date beginDate, Date endDate, boolean onlyMergeds) {
        List<Object> selectParams = new ArrayList<>();

        StringBuilder sql = new StringBuilder();
        sql.append(LIST_ISSUES_OF_FILE_PAIR_BY_DATE);

        if (onlyMergeds) {
            sql.append(MERGED_PULL_REQUEST_ONLY);
        }

        selectParams.add(repository.getId());
        selectParams.add(beginDate);
        selectParams.add(endDate);

        if (file != null) {
            sql.append(EXISTS_FILE1_IN_PULL_REQUEST);
            selectParams.add(file);
        }

        if (file2 != null) {
            sql.append(EXISTS_FILE2_IN_PULL_REQUEST);
            selectParams.add(file2);
        }

        List<Object[]> rawIssues = dao.selectNativeWithParams(sql.toString(), selectParams.toArray());
        List<IssueMetrics> issuesAndComments = new ArrayList<>(rawIssues.size());
        for (Object[] objects : rawIssues) {
            Integer issuedNumber = (Integer) objects[0];
            IssueMetrics issue = new IssueMetrics(
                    issuedNumber,
                    (String) objects[1],
                    (String) objects[2],
                    listComments(issuedNumber));


            issuesAndComments.add(issue);
        }

        return issuesAndComments;
    }

    private List<String> listComments(Integer pullRequestNumber) {

        StringBuilder sql = new StringBuilder();
        sql.append(LIST_COMMENTS_BY_PULL_REQUEST_NUMBER);

        List<String> comments = dao.selectNativeWithParams(sql.toString(), new Object[]{pullRequestNumber});

        return comments;
    }

    public List<EntityComment> listComments(EntityRepository repository, String file, String file2, Date beginDate, Date endDate, boolean onlyMergeds) {
        List<Object> selectParams = new ArrayList<>();

        StringBuilder sql = new StringBuilder();
        sql.append(LIST_COMMENTS_OF_FILE_PAIR_BY_DATE);

        if (onlyMergeds) {
            sql.append(MERGED_PULL_REQUEST_ONLY);
        }

        selectParams.add(repository.getId());
        selectParams.add(beginDate);
        selectParams.add(endDate);

        if (file != null) {
            sql.append(EXISTS_FILE1_IN_PULL_REQUEST);
            selectParams.add(file);
        }

        if (file2 != null) {
            sql.append(EXISTS_FILE2_IN_PULL_REQUEST);
            selectParams.add(file2);
        }

        List<EntityComment> comments = dao.selectNativeWithParams(sql.toString(), selectParams.toArray());

        return comments;
    }

    public int calculePairFileDaysAge(EntityRepository repository, String file, String file2, Date beginDate, Date endDate, boolean onlyMergeds) {
        if (file == null || file2 == null) {
            throw new IllegalArgumentException("The file and file2 parameters can not be null.");
        }

        List<Object> selectParams = new ArrayList<>();

        StringBuilder sql = new StringBuilder();
        sql.append(SELECT_RELEASE_MIN_MAX_DATE_CREATION);

        if (onlyMergeds) {
            sql.append(MERGED_PULL_REQUEST_ONLY);
        }

        selectParams.add(repository.getId());
        selectParams.add(file);
        selectParams.add(file2);

        if (beginDate != null) {
            sql.append(BEGIN_DATE);
            selectParams.add(beginDate);
        }

        if (endDate != null) {
            sql.append(END_DATE);
            selectParams.add(endDate);
        }

        List<Object[]> minMaxDateList = dao.selectNativeWithParams(sql.toString(), selectParams.toArray());
        Object[] minMaxDate = minMaxDateList.get(0);
        java.sql.Timestamp minDate = (java.sql.Timestamp) minMaxDate[0];
        java.sql.Timestamp maxDate = (java.sql.Timestamp) minMaxDate[1];

        LocalDate createdAt = new LocalDate(minDate.getTime());
        LocalDate finalDate = new LocalDate(maxDate.getTime());
        Days age = Days.daysBetween(createdAt, finalDate);

        return age.getDays();
    }
}
