/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package br.edu.utfpr.cm.JGitMinerWeb.services;

import br.edu.utfpr.cm.JGitMinerWeb.dao.PersistenciaServices;
import br.edu.utfpr.cm.JGitMinerWeb.pojo.EntityComment;
import java.util.List;
import org.eclipse.egit.github.core.Comment;

/**
 *
 * @author Douglas
 */
public class CommentServices {

    public static EntityComment getCommentByIdComment(long idComment) {
        List<EntityComment> comments = PersistenciaServices.executeNamedQueryComParametros("Comment.findByIdComment", new String[]{"idComment"}, new Object[]{idComment});
        if (!comments.isEmpty()) {
            return (EntityComment) PersistenciaServices.buscaID(comments.get(0).getClass(), comments.get(0).getId() + "");
        }
        return null;
    }

    public static EntityComment createEntity(Comment gitComment) {
        if (gitComment == null) {
            return null;
        }

        EntityComment comment = new EntityComment();

        comment.setCreatedAt(gitComment.getCreatedAt());
        comment.setUpdatedAt(gitComment.getUpdatedAt());
        comment.setBody(gitComment.getBody());
        comment.setBodyHtml(gitComment.getBodyHtml());
        comment.setBodyText(gitComment.getBodyText());
        comment.setIdComment(gitComment.getId());
        comment.setUrl(gitComment.getUrl());
        //  comment.setUser(gitComent.getUser());

        return comment;
    }
}
