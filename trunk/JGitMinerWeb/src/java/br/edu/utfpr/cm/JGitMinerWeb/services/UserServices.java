/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package br.edu.utfpr.cm.JGitMinerWeb.services;

import br.edu.utfpr.cm.JGitMinerWeb.dao.PersistenciaServices;
import br.edu.utfpr.cm.JGitMinerWeb.pojo.EntityUser;
import java.util.List;
import org.eclipse.egit.github.core.User;

/**
 *
 * @author Douglas
 */
public class UserServices {

    public static EntityUser getUserByLogin(String login) {
        List<EntityUser> users = PersistenciaServices.executeNamedQueryComParametros("User.findByLogin", new String[]{"login"}, new Object[]{login});
        if (!users.isEmpty()) {
            return (EntityUser) PersistenciaServices.buscaID(users.get(0).getClass(), users.get(0).getId() + "");
        }
        return null;
    }

    public static EntityUser insertUser(User gitUser) {
        EntityUser newUser = new EntityUser(gitUser);
        PersistenciaServices.insere(newUser);
        return newUser;
    }
}
