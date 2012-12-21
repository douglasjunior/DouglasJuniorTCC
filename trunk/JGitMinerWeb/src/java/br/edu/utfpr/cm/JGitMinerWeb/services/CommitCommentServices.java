/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package br.edu.utfpr.cm.JGitMinerWeb.services;

import br.edu.utfpr.cm.JGitMinerWeb.dao.GenericDao;
import br.edu.utfpr.cm.JGitMinerWeb.pojo.EntityCommitComment;
import java.util.Date;
import java.util.List;
import org.eclipse.egit.github.core.CommitComment;

/**
 *
 * @author Douglas
 */
public class CommitCommentServices {

    public static EntityCommitComment createEntity(CommitComment gitComment, GenericDao dao) {
        if (gitComment == null) {
            return null;
        }

        EntityCommitComment comment = getCommitCommentByURL(gitComment.getUrl(), dao);

        if (comment == null) {
            comment = new EntityCommitComment();
        }

        comment.setMineredAt(new Date());
        comment.setCommitId(gitComment.getCommitId());
        comment.setBody(gitComment.getBody());
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

        if (comment.getId() == null || comment.getId().equals(new Long(0))) {
            dao.insert(comment);
        } else {
            dao.edit(comment);
        }

        return comment;
    }

    private static EntityCommitComment getCommitCommentByURL(String url, GenericDao dao) {
        List<EntityCommitComment> comments = dao.executeNamedQueryComParametros("CommitComment.findByURL", new String[]{"url"}, new Object[]{url});
        if (!comments.isEmpty()) {
            return comments.get(0);
        }
        return null;
    }
}
