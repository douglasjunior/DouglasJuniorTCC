/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package br.edu.utfpr.cm.JGitMinerWeb.services.miner;

import br.edu.utfpr.cm.JGitMinerWeb.dao.GenericDao;
import br.edu.utfpr.cm.JGitMinerWeb.pojo.miner.EntityCommitStats;
import br.edu.utfpr.cm.JGitMinerWeb.pojo.miner.EntityRepositoryCommit;
import java.io.Serializable;
import java.util.Date;
import java.util.List;
import org.eclipse.egit.github.core.CommitStats;

/**
 *
 * @author Douglas
 */
public class CommitStatsServices implements Serializable {

    public static EntityCommitStats createEntity(CommitStats gitStats, EntityRepositoryCommit repoCommit, GenericDao dao) {
        if (gitStats == null) {
            return null;
        }

        EntityCommitStats stats = new EntityCommitStats();

        stats.setMineredAt(new Date());
        stats.setAdditions(gitStats.getAdditions());
        stats.setDeletions(gitStats.getDeletions());
        stats.setTotal(gitStats.getTotal());

        dao.insert(stats);

        return stats;
    }

    private static EntityCommitStats getStatsByRepoCommit(EntityRepositoryCommit repoCommit, GenericDao dao) {
        List<EntityCommitStats> stats = dao.executeNamedQueryComParametros("CommitStats.findByRepositoryCommit", new String[]{"repositoryCommit"}, new Object[]{repoCommit});
        if (!stats.isEmpty()) {
            return stats.get(0);
        }
        return null;
    }
}
