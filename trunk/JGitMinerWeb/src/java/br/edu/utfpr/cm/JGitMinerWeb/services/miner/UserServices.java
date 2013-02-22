/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package br.edu.utfpr.cm.JGitMinerWeb.services.miner;

import br.edu.utfpr.cm.JGitMinerWeb.dao.GenericDao;
import br.edu.utfpr.cm.JGitMinerWeb.pojo.miner.EntityUser;
import br.edu.utfpr.cm.JGitMinerWeb.util.OutLog;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.eclipse.egit.github.core.Repository;
import org.eclipse.egit.github.core.User;
import org.eclipse.egit.github.core.service.CollaboratorService;
import org.eclipse.egit.github.core.service.WatcherService;

/**
 *
 * @author Douglas
 */
public class UserServices implements Serializable {

    private static EntityUser getUserByLogin(String login, GenericDao dao) {
        List<EntityUser> users = dao.executeNamedQueryComParametros("User.findByLogin", new String[]{"login"}, new Object[]{login}, true);
        if (!users.isEmpty()) {
            return users.get(0);
        }
        return null;
    }

    public static EntityUser createEntity(User gitUser, GenericDao dao, boolean firstMiner) {
        if (gitUser == null) {
            return null;
        }

        EntityUser user = getUserByLogin(gitUser.getLogin(), dao);

        if (user == null) {
            user = new EntityUser();
        }

        user.setMineredAt(new Date());
        user.setGravatarId(gitUser.getGravatarId());
        user.setIdUser(gitUser.getId());
        user.setLogin(gitUser.getLogin());
        user.setUrl(gitUser.getUrl());

        if (firstMiner) {
            user.setCreatedAt(gitUser.getCreatedAt());
            user.setCollaborators(gitUser.getCollaborators());
            user.setDiskUsage(gitUser.getDiskUsage());
            user.setFollowers(gitUser.getFollowers());
            user.setFollowing(gitUser.getFollowing());
            user.setOwnedPrivateRepos(gitUser.getOwnedPrivateRepos());
            user.setPrivateGists(gitUser.getPrivateGists());
            user.setPublicGists(gitUser.getPublicGists());
            user.setPublicRepos(gitUser.getPublicRepos());
            user.setTotalPrivateRepos(gitUser.getTotalPrivateRepos());
            user.setAvatarUrl(gitUser.getBlog());
            user.setCompany(gitUser.getCompany());
            user.setEmail(gitUser.getEmail());
            user.setHtmlUrl(gitUser.getHtmlUrl());
            user.setLocation(gitUser.getLocation());
            user.setName(gitUser.getName());
            user.setType(gitUser.getType());
        }


        if (user.getId() == null || user.getId().equals(new Long(0))) {
            dao.insert(user);
        } else {
            dao.edit(user);
        }

        return user;
    }

    public static List<User> getGitCollaboratorsFromRepository(Repository gitRepo, OutLog out) throws Exception {
        List<User> users = new ArrayList<User>();
        try {
            out.printLog("Baixando Collaborators...\n");
            users.addAll(new CollaboratorService(AuthServices.getGitHubCliente()).getCollaborators(gitRepo));
            out.printLog(users.size() + " Collaborators baixados!");
        } catch (Exception ex) {
            ex.printStackTrace();
            out.printLog("Erro: " + ex.toString());
        }
        return users;
    }

    public static List<User> getGitWatchersFromRepository(Repository gitRepo, OutLog out) throws Exception {
        out.printLog("Baixando Watchers...\n");
        List<User> users = new WatcherService(AuthServices.getGitHubCliente()).getWatchers(gitRepo);
        out.printLog(users.size() + " Watchers baixados!");
        return users;
    }
}
