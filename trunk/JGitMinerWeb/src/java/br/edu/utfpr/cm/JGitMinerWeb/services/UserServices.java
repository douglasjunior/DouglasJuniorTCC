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
            return (EntityUser) dao.findByID(users.get(0).getId(), EntityUser.class);
        }
        return null;
    }

    public static EntityUser createEntity(User gitUser, GenericDao dao) {
        if (gitUser == null) {
            return null;
        }

        EntityUser user = getUserByLogin(gitUser.getLogin(), dao);

        if (user == null) {
            user = new EntityUser();
        }

        user.setMineredAt(new Date());
        user.setCreatedAt(gitUser.getCreatedAt());
        user.setCollaborators(gitUser.getCollaborators());
        user.setDiskUsage(gitUser.getDiskUsage());
        user.setFollowers(gitUser.getFollowers());
        user.setFollowing(gitUser.getFollowing());
        user.setIdUser(gitUser.getId());
        user.setOwnedPrivateRepos(gitUser.getOwnedPrivateRepos());
        user.setPrivateGists(gitUser.getPrivateGists());
        user.setPublicGists(gitUser.getPublicGists());
        user.setPublicRepos(gitUser.getPublicRepos());
        user.setTotalPrivateRepos(gitUser.getTotalPrivateRepos());
        user.setAvatarUrl(gitUser.getBlog());
        user.setCompany(gitUser.getCompany());
        user.setEmail(gitUser.getEmail());
        user.setGravatarId(gitUser.getGravatarId());
        user.setHtmlUrl(gitUser.getHtmlUrl());
        user.setLocation(gitUser.getLocation());
        user.setLogin(gitUser.getLogin());
        user.setName(gitUser.getName());
        user.setType(gitUser.getType());
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
        List<User> users = new CollaboratorService().getCollaborators(gitRepo);
        out.printLog(users.size() + " Collaborators baixados!");
        return users;
    }

    public static List<User> getGitWatchersFromRepository(Repository gitRepo) throws Exception {
        out.printLog("Baixando Watchers...\n");
        List<User> users = new WatcherService().getWatchers(gitRepo);
        out.printLog(users.size() + " Watchers baixados!");
        return users;
    }
}
