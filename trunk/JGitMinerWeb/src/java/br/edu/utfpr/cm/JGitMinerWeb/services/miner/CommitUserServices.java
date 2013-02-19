/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package br.edu.utfpr.cm.JGitMinerWeb.services.miner;

import br.edu.utfpr.cm.JGitMinerWeb.dao.GenericDao;
import br.edu.utfpr.cm.JGitMinerWeb.pojo.miner.EntityCommitUser;
import java.io.Serializable;
import java.util.Date;
import java.util.List;
import org.eclipse.egit.github.core.CommitUser;

/**
 *
 * @author Douglas
 */
public class CommitUserServices implements Serializable {

    public static EntityCommitUser createEntity(CommitUser gitCommitUser, GenericDao dao) {
        if (gitCommitUser == null) {
            return null;
        }

        EntityCommitUser commitUser = getCommitUserByEmail(gitCommitUser.getEmail(), dao);

        if (commitUser == null) {
            commitUser = new EntityCommitUser();

            commitUser.setMineredAt(new Date());
            commitUser.setDateCommitUser(gitCommitUser.getDate());
            commitUser.setEmail(gitCommitUser.getEmail());
            commitUser.setName(gitCommitUser.getName());

            dao.insert(commitUser);
        }

        return commitUser;
    }

    private static EntityCommitUser getCommitUserByEmail(String email, GenericDao dao) {
        List<EntityCommitUser> users = dao.executeNamedQueryComParametros("CommitUser.findByEmail", new String[]{"email"}, new Object[]{email}, true);
        if (!users.isEmpty()) {
            return users.get(0);
        }
        return null;
    }
}
