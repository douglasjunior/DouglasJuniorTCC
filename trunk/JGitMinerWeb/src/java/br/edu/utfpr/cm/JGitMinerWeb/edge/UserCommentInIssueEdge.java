/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package br.edu.utfpr.cm.JGitMinerWeb.edge;

import br.edu.utfpr.cm.JGitMinerWeb.pojo.EntityIssue;
import br.edu.utfpr.cm.JGitMinerWeb.pojo.EntityUser;

/**
 *
 * @author douglas
 */
public class UserCommentInIssueEdge extends AbstractEdge<EntityIssue, EntityUser, Long> {

    public UserCommentInIssueEdge(EntityIssue x, EntityUser y, Long value) {
        super(x, y, value);
    }

    @Override
    public String getStringX() {
        return getX().getNumber()+"";
    }

    @Override
    public String getStringY() {
        return getY().getLogin();
    } 
}
