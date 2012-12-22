/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package br.edu.utfpr.cm.JGitMinerWeb.services;

import br.edu.utfpr.cm.JGitMinerWeb.dao.GenericDao;
import br.edu.utfpr.cm.JGitMinerWeb.pojo.EntityUser;
import br.edu.utfpr.cm.JGitMinerWeb.util.out;
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
public class UserServices {

    private static EntityUser getUserByLogin(String login, GenericDao dao) {
        List<EntityUser> users = dao.executeNamedQueryComParametros("User.findByLogin", new String[]{"login"}, new Object[]{login});
        if (!users.isEmpty()) {
            return users.get(0);
        }
        return null;
    }

    public static EntityUser createEntity(User gitUser, GenericDao dao, boolean primaryMiner) {
        if (gitUser == null) {
            return null;
        }

        EntityUser user = getUserByLogin(gitUser.getLogin(), dao);
        if (user == null) {
            user = new EntityUser();
        }

        if (primaryMiner) {
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

        user.setMineredAt(new Date());
        user.setGravatarId(gitUser.getGravatarId());
        user.setIdUser(gitUser.getId());
        user.setLogin(gitUser.getLogin());
        user.setUrl(gitUser.getUrl());

        if (user.getId() == null || user.getId().equals(new Long(0))) {
            dao.insert(user);
        } else {
            dao.edit(user);
        }

        return user;
    }

    public static List<User> getGitCollaboratorsFromRepository(Repository gitRepo) throws Exception {
        out.printLog("Baixando Collaborators...\n");
        List<User> users = new CollaboratorService(AuthServices.getGitHubCliente()).getCollaborators(gitRepo);
        out.printLog(users.size() + " Collaborators baixados!");
        return users;
    }

    public static List<User> getGitWatchersFromRepository(Repository gitRepo) throws Exception {
        out.printLog("Baixando Watchers...\n");
        List<User> users = new WatcherService(AuthServices.getGitHubCliente()).getWatchers(gitRepo);
        out.printLog(users.size() + " Watchers baixados!");
        return users;
    }
}
