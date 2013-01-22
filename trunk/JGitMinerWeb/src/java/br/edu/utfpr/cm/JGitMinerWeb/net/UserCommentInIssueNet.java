/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package br.edu.utfpr.cm.JGitMinerWeb.net;

import br.edu.utfpr.cm.JGitMinerWeb.edge.AbstractEdge;
import br.edu.utfpr.cm.JGitMinerWeb.pojo.EntityRepository;
import java.util.ArrayList;
import java.util.Date;

/**
 *
 * @author douglas
 */
public class UserCommentInIssueNet extends AbstractNet {

    public UserCommentInIssueNet(EntityRepository repository, Date begin, Date end) {
        super(repository, begin, end);
    }

    @Override
    public void run() {
        net = new ArrayList<AbstractEdge>();
    }
}
