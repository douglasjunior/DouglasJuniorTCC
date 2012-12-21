/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package br.edu.utfpr.cm.JGitMinerWeb.services;

import br.edu.utfpr.cm.JGitMinerWeb.dao.GenericDao;
import br.edu.utfpr.cm.JGitMinerWeb.pojo.EntityCommit;
import java.util.Date;
import java.util.List;
import org.eclipse.egit.github.core.Commit;

/**
 *
 * @author Douglas
 */
public class CommitServices {

    public static EntityCommit createEntity(Commit gitCommit, GenericDao dao) {
        if (gitCommit == null) {
            return null;
        }

        EntityCommit commit = getCommitByURL(gitCommit.getUrl(), dao);

        if (commit == null) {
            commit = new EntityCommit();
        }

        commit.setMineredAt(new Date());
        commit.setAuthor(CommitUserServices.createEntity(gitCommit.getAuthor(), dao));
        commit.setCommitter(CommitUserServices.createEntity(gitCommit.getCommitter(), dao));
        commit.setCommentCount(gitCommit.getCommentCount());
        commit.setMessage(gitCommit.getMessage());
        createParents(commit, gitCommit.getParents(), dao);
        commit.setSha(gitCommit.getSha());
        commit.setTree(null);
        commit.setUrl(gitCommit.getUrl());

        if (commit.getId() == null || commit.getId().equals(new Long(0))) {
            dao.insert(commit);
        } else {
            dao.edit(commit);
        }

        return commit;
    }

    public static EntityCommit getCommitByURL(String url, GenericDao dao) {
        List<EntityCommit> commits = dao.executeNamedQueryComParametros("Commit.findByURL", new String[]{"url"}, new Object[]{url});
        if (!commits.isEmpty()) {
            return commits.get(0);
        }
        return null;
    }

    private static void createParents(EntityCommit commit, List<Commit> gitParents, GenericDao dao) {
        if (gitParents != null) {
            for (Commit gitParent : gitParents) {
                commit.addParent(createEntity(gitParent, dao));
            }
        }


    }
}
