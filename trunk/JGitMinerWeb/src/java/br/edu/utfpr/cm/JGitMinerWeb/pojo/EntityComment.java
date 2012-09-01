/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package br.edu.utfpr.cm.JGitMinerWeb.pojo;

import br.edu.utfpr.cm.JGitMinerWeb.services.CommentServices;
import java.io.Serializable;
import java.util.Date;
import javax.persistence.*;
import org.eclipse.egit.github.core.Comment;

/**
 *
 * @author Douglas
 */
@Entity
@Table(name = "gitComment")
@NamedQueries({
    @NamedQuery(name = "Comment.findByIdComment", query = "SELECT c FROM EntityComment c WHERE c.idComment = :idComment")
})
public class EntityComment implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Temporal(javax.persistence.TemporalType.TIMESTAMP)
    private Date mineredAt;
    @Temporal(javax.persistence.TemporalType.TIMESTAMP)
    private Date createdAt;
    @Temporal(javax.persistence.TemporalType.TIMESTAMP)
    private Date updatedAt;
    @Column(columnDefinition = "text")
    private String body;
    @Column(columnDefinition = "text")
    private String bodyHtml;
    @Column(columnDefinition = "text")
    private String bodyText;
    @Column(unique = true)
    private long idComment;
    private String url;
    @ManyToOne(cascade = CascadeType.ALL)
    private EntityUser user;

    public EntityComment() {
        mineredAt = new Date();
    }

    public EntityComment(Date createdAt, Date updatedAt, String body, String bodyHtml, String bodyText, long idComment, String url, EntityUser user) {
        this();
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.body = body;
        this.bodyHtml = bodyHtml;
        this.bodyText = bodyText;
        this.idComment = idComment;
        this.url = url;
        this.user = user;
    }

    public EntityComment(Comment coment) {
        this();
        this.createdAt = coment.getCreatedAt();
        this.updatedAt = coment.getUpdatedAt();
        this.body = coment.getBody();
        this.bodyHtml = coment.getBodyHtml();
        this.bodyText = coment.getBodyText();
        this.idComment = coment.getId();
        this.url = coment.getUrl();
        this.user = EntityUser.createUser(coment.getUser());
    }

    public static EntityComment createComment(Comment gitComment) {
                EntityComment entityComment = null;
        if (gitComment != null) {
            entityComment = CommentServices.getCommentByIdComment(gitComment.getId());
            if (entityComment == null) {
                entityComment = CommentServices.insertComment(gitComment);
                System.out.println("############# CRIOU NOVO COMMENT " + entityComment.getIdComment() + " | " + entityComment.getUrl() + " #############");
            } else {
                System.out.println("############### PEGOU O COMMENT " + entityComment.getIdComment() + " | " + entityComment.getUrl() + " ##############");
            }
        }
        return entityComment;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getBodyHtml() {
        return bodyHtml;
    }

    public void setBodyHtml(String bodyHtml) {
        this.bodyHtml = bodyHtml;
    }

    public String getBodyText() {
        return bodyText;
    }

    public void setBodyText(String bodyText) {
        this.bodyText = bodyText;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public long getIdComment() {
        return idComment;
    }

    public void setIdComment(long idComment) {
        this.idComment = idComment;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public EntityUser getUser() {
        return user;
    }

    public void setUser(EntityUser user) {
        this.user = user;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (id != null ? id.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof EntityComment)) {
            return false;
        }
        EntityComment other = (EntityComment) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "br.edu.utfpr.cm.JGitMiner.pojo.EntityComment[ id=" + id + " ]";
    }
}
