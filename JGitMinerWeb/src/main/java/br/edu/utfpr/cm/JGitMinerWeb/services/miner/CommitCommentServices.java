/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package br.edu.utfpr.cm.JGitMinerWeb.services.miner;

import br.edu.utfpr.cm.JGitMinerWeb.dao.GenericDao;
import br.edu.utfpr.cm.JGitMinerWeb.model.miner.EntityCommitComment;
import br.edu.utfpr.cm.JGitMinerWeb.model.miner.EntityRepositoryCommit;
import br.edu.utfpr.cm.JGitMinerWeb.util.JsfUtil;
import java.io.Serializable;
import java.util.Date;
import java.util.List;
import org.eclipse.egit.github.core.CommitComment;
import org.eclipse.egit.github.core.Repository;
import org.eclipse.egit.github.core.service.CommitService;

/**
 *
 * @author Douglas
 */
public class CommitCommentServices implements Serializable {

    public static EntityCommitComment createEntity(CommitComment gitComment, GenericDao dao) {
        if (gitComment == null) {
            return null;
        }

        EntityCommitComment comment = getCommitCommentByURL(gitComment.getId(), dao);

        if (comment == null) {
            comment = new EntityCommitComment();

            comment.setMineredAt(new Date());
            comment.setCommitId(gitComment.getCommitId());
            comment.setBody(JsfUtil.filterChar(gitComment.getBody()));
            comment.setBodyHtml(gitComment.getBodyHtml());
            comment.setBodyText(gitComment.getBodyText());
            comment.setCreatedAt(gitComment.getCreatedAt());
            comment.setIdComment(gitComment.getId());
            comment.setLine(gitComment.getLine());
            comment.setPathCommitComment(gitComment.getPath());
            comment.setPosition(gitComment.getPosition());
            comment.setUpdatedAt(gitComment.getUpdatedAt());
            comment.setUrl(gitComment.getUrl());
            comment.setUser(UserServices.createEntity(gitComment.getUser(), dao, false));

            dao.insert(comment);
        }

        return comment;
    }

    private static EntityCommitComment getCommitCommentByURL(Long idComment, GenericDao dao) {
        List<EntityCommitComment> comments = dao.executeNamedQueryWithParams("CommitComment.findByIdComment", new String[]{"idComment"}, new Object[]{idComment}, true);
        if (!comments.isEmpty()) {
            return comments.get(0);
        }
        return null;
    }

    public static List<CommitComment> getGitCommitComments(Repository gitRepo, EntityRepositoryCommit repoCommit) throws Exception {
        try {
            return new CommitService(AuthServices.getGitHubClient()).getComments(gitRepo, repoCommit.getSha());
        } catch (java.net.UnknownHostException ex) {
            ex.printStackTrace();
            Thread.sleep(100 * 1000);
            return getGitCommitComments(gitRepo, repoCommit);
        } catch (Exception ex) {
            ex.printStackTrace();
            if (ex instanceof java.net.UnknownHostException) {
                Thread.sleep(100 * 1000);
            }
            return getGitCommitComments(gitRepo, repoCommit);
        }
    }
}
