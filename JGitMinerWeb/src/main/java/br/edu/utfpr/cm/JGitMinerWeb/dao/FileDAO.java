
package br.edu.utfpr.cm.JGitMinerWeb.dao;

import br.edu.utfpr.cm.JGitMinerWeb.model.miner.EntityRepository;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 *
 * @author Rodrigo T. Kuroda
 */
public class FileDAO {

    public static final String SELECT_COUNT_PULL_REQUEST_WHERE_FILE_IS_IN
            = "SELECT count(distinct(pul.id)) "
            + " FROM gitcommitfile fil, "
            + "      gitpullrequest_gitrepositorycommit prc, "
            + "      gitpullrequest pul "
            + " WHERE fil.repositorycommit_id = prc.repositorycommits_id "
            + "   AND pul.id = prc.entitypullrequest_id "
            + "   AND pul.mergedat IS NOT NULL "
            + "   AND pul.repository_id = ? "
            + "   AND fil.filename = ? "
            + "   AND pul.createdat BETWEEN ? AND ? ";

    private static final String FILTER_BY_MAX_NUMBER_FILES_IN_COMMIT
            = "   AND (SELECT count(1) FROM gitcommitfile cf WHERE cf.repositorycommit_id = prc.repositorycommits_id) <= ? ";

    private static final String FILTER_BY_MIN_NUMBER_FILES_IN_COMMIT
            = "   AND (SELECT count(1) FROM gitcommitfile cf WHERE cf.repositorycommit_id = prc.repositorycommits_id) >= ? ";

    private static final String MERGED_PULL_REQUEST_ONLY = " AND pul.mergedat IS NOT NULL ";

    private final GenericDao dao;

    public FileDAO(GenericDao dao) {
        this.dao = dao;
    }

    public long calculeNumberOfPullRequestWhereFileIsIn(EntityRepository repository,
            String filename, Date beginDate, Date endDate,
            int minFilesPerCommit, int maxFilesPerCommit, boolean onlyMergeds) {
        List<Object> selectParams = new ArrayList<>();

        StringBuilder sql = new StringBuilder();
        sql.append(SELECT_COUNT_PULL_REQUEST_WHERE_FILE_IS_IN);

        selectParams.add(repository.getId());
        selectParams.add(filename);
        selectParams.add(beginDate);
        selectParams.add(endDate);

        if (onlyMergeds) {
            sql.append(MERGED_PULL_REQUEST_ONLY);
        }

        if (minFilesPerCommit > 0) {
            sql.append(FILTER_BY_MIN_NUMBER_FILES_IN_COMMIT);
            selectParams.add(minFilesPerCommit);
        }

        if (maxFilesPerCommit > 0) {
            sql.append(FILTER_BY_MAX_NUMBER_FILES_IN_COMMIT);
            selectParams.add(maxFilesPerCommit);
        }

        Long count = (Long) dao.selectNativeOneWithParams(sql.toString(), selectParams.toArray());

        return count != null ? count : 0l;
    }
}
