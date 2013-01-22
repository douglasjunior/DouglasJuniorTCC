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
public class UserCommentInIssueEdge extends AbstractEdge<EntityIssue, EntityUser, Integer> {

    public UserCommentInIssueEdge(EntityIssue x, EntityUser y, Integer value) {
        super(x, y, value);
    }
}
