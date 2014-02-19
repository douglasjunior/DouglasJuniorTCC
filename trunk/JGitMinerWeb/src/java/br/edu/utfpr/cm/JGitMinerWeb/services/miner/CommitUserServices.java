/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package br.edu.utfpr.cm.JGitMinerWeb.services.miner;

import br.edu.utfpr.cm.JGitMinerWeb.dao.GenericDao;
import br.edu.utfpr.cm.JGitMinerWeb.model.miner.EntityCommitUser;
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

        EntityCommitUser commitUser = getCommitUserByEmailAndDate(gitCommitUser.getEmail(), gitCommitUser.getDate(), dao);

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

    private static EntityCommitUser getCommitUserByEmailAndDate(String email, Date date, GenericDao dao) {
        List<EntityCommitUser> users = dao.executeNamedQueryWithParams("CommitUser.findByEmailAndDate", new String[]{"email", "date"}, new Object[]{email, date}, true);
        if (!users.isEmpty()) {
            return users.get(0);
        }
        return null;
    }
}
