/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package br.edu.utfpr.cm.JGitMinerWeb.services;

import br.edu.utfpr.cm.JGitMinerWeb.dao.CommentDao;
import br.edu.utfpr.cm.JGitMinerWeb.dao.UserDao;
import br.edu.utfpr.cm.JGitMinerWeb.pojo.EntityComment;
import java.util.Date;
import java.util.List;
import org.eclipse.egit.github.core.Comment;

/**
 *
 * @author Douglas
 */
public class CommentServices {
    
    private static CommentDao dao;
    
    public static EntityComment getCommentByIdComment(long idComment) {
        List<EntityComment> comments = dao.executeNamedQueryComParametros("Comment.findByIdComment", new String[]{"idComment"}, new Object[]{idComment});
        if (!comments.isEmpty()) {
            return dao.findByID(comments.get(0).getId());
        }
        return null;
    }
    
    public static EntityComment createEntity(Comment gitComment, CommentDao commentDao, UserDao userDao) {
        if (gitComment == null) {
            return null;
        }
        
        CommentServices.dao = commentDao;
        EntityComment comment = getCommentByIdComment(gitComment.getId());
        
        if (comment == null) {
            comment = new EntityComment();
        }
        
        comment.setMineredAt(new Date());
        comment.setCreatedAt(gitComment.getCreatedAt());
        comment.setUpdatedAt(gitComment.getUpdatedAt());
        comment.setBody(gitComment.getBody());
        comment.setBodyHtml(gitComment.getBodyHtml());
        comment.setBodyText(gitComment.getBodyText());
        comment.setIdComment(gitComment.getId());
        comment.setUrl(gitComment.getUrl());
        comment.setUser(UserServices.createEntity(gitComment.getUser(), userDao));
        
        if (comment.getId() == null || comment.getId().equals(new Long(0))) {
            commentDao.insert(comment);
        } else {
            commentDao.edit(comment);
        }
        
        return comment;
    }
}
