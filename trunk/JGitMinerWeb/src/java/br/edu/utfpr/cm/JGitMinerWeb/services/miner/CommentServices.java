/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package br.edu.utfpr.cm.JGitMinerWeb.services.miner;

import br.edu.utfpr.cm.JGitMinerWeb.dao.GenericDao;
import br.edu.utfpr.cm.JGitMinerWeb.pojo.miner.EntityComment;
import br.edu.utfpr.cm.JGitMinerWeb.util.JsfUtil;
import java.io.Serializable;
import java.util.Date;
import java.util.List;
import org.eclipse.egit.github.core.Comment;
import org.eclipse.egit.github.core.Repository;
import org.eclipse.egit.github.core.service.IssueService;

/**
 *
 * @author Douglas
 */
public class CommentServices implements Serializable {

    public static EntityComment getCommentByIdComment(long idComment, GenericDao dao) {
        List<EntityComment> comments = dao.executeNamedQueryComParametros("Comment.findByIdComment", new String[]{"idComment"}, new Object[]{idComment}, true);
        if (!comments.isEmpty()) {
            return comments.get(0);
        }
        return null;
    }

    public static EntityComment createEntity(Comment gitComment, GenericDao dao) {
        if (gitComment == null) {
            return null;
        }

        EntityComment comment = getCommentByIdComment(gitComment.getId(), dao);

        if (comment == null) {
            comment = new EntityComment();

            comment.setMineredAt(new Date());
            comment.setCreatedAt(gitComment.getCreatedAt());
            comment.setUpdatedAt(gitComment.getUpdatedAt());
            comment.setBody(JsfUtil.filterChar(gitComment.getBody()));
            comment.setBodyHtml(gitComment.getBodyHtml());
            comment.setBodyText(gitComment.getBodyText());
            comment.setIdComment(gitComment.getId());
            comment.setUrl(gitComment.getUrl());
            comment.setUser(UserServices.createEntity(gitComment.getUser(), dao, false));

            dao.insert(comment);
        }

        return comment;
    }

    public static List<Comment> getGitCommentsByIssue(Repository gitRepo, Integer issueNumber) throws Exception {
        try {
            return new IssueService(AuthServices.getGitHubCliente()).getComments(gitRepo, issueNumber);
        } catch (Exception ex) {
            ex.printStackTrace();
            return getGitCommentsByIssue(gitRepo, issueNumber);
        }
    }
}
