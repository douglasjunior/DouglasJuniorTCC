/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package br.edu.utfpr.cm.JGitMinerWeb.services;

import br.edu.utfpr.cm.JGitMinerWeb.dao.UserDao;
import br.edu.utfpr.cm.JGitMinerWeb.pojo.EntityUser;
import java.util.Date;
import java.util.List;
import org.eclipse.egit.github.core.User;

/**
 *
 * @author Douglas
 */
public class UserServices {

    private static UserDao dao;

    private static EntityUser getUserByLogin(String login) {
        List<EntityUser> users = dao.executeNamedQueryComParametros("User.findByLogin", new String[]{"login"}, new Object[]{login});
        if (!users.isEmpty()) {
            return dao.findByID(users.get(0).getId());
        }
        return null;
    }

    static EntityUser createEntity(User gitUser, UserDao userDao) {
        if (gitUser == null) {
            return null;
        }

        UserServices.dao = userDao;
        EntityUser user = getUserByLogin(gitUser.getLogin());

        if (user == null) {
            user = new EntityUser();
        } else {
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

        return user;
    }
}
