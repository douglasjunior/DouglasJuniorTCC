/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package br.edu.utfpr.cm.JGitMinerWeb.services.miner;

import br.edu.utfpr.cm.JGitMinerWeb.dao.GenericDao;
import br.edu.utfpr.cm.JGitMinerWeb.pojo.miner.EntityCommit;
import java.io.Serializable;
import java.util.Date;
import java.util.List;
import org.eclipse.egit.github.core.Commit;

/**
 *
 * @author Douglas
 */
public class CommitServices implements Serializable {

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
//        createParents(commit, gitCommit.getParents(), gitRepo, dao);
        commit.setSha(gitCommit.getSha());
        //  commit.setTree(TreeServices.createTreeEntity(gitCommit.getTree(), gitRepo, dao));
        commit.setUrl(gitCommit.getUrl());

        if (commit.getId() == null || commit.getId().equals(new Long(0))) {
            dao.insert(commit);
        } else {
            dao.edit(commit);
        }

        return commit;
    }

    public static EntityCommit getCommitByURL(String url, GenericDao dao) {
        List<EntityCommit> commits = dao.executeNamedQueryComParametros("Commit.findByURL", new String[]{"url"}, new Object[]{url}, true);
        if (!commits.isEmpty()) {
            return commits.get(0);
        }
        return null;
    }
//    private static void createParents(EntityCommit commit, List<Commit> gitParents, Repository gitRepo, GenericDao dao) {
//        if (gitParents != null) {
//            for (Commit gitParent : gitParents) {
//                commit.addParent(createEntity(gitParent, gitRepo, dao));
//            }
//        }
//    }
}
