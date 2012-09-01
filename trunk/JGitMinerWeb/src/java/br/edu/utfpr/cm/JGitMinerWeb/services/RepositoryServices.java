/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package br.edu.utfpr.cm.JGitMinerWeb.services;

import br.edu.utfpr.cm.JGitMinerWeb.dao.PersistenciaServices;
import br.edu.utfpr.cm.JGitMinerWeb.pojo.EntityRepository;
import java.io.IOException;
import java.util.List;
import org.eclipse.egit.github.core.Repository;
import org.eclipse.egit.github.core.service.RepositoryService;

/**
 *
 * @author Douglas
 */
public class RepositoryServices {

    public static EntityRepository getRepositoryByName(String name) {
        List<EntityRepository> users = PersistenciaServices.executeNamedQueryComParametros("Repository.findByName", new String[]{"name"}, new Object[]{name});
        if (!users.isEmpty()) {
            return (EntityRepository) PersistenciaServices.buscaID(users.get(0).getClass(), users.get(0).getId() + "");
        }
        return null;
    }

    public static EntityRepository insertRepository(Repository gitRepository) {
        EntityRepository newRepository = new EntityRepository(gitRepository);
        PersistenciaServices.insere(newRepository);
        return newRepository;
    }

    public static Repository getGitRepository(String ownerLogin, String repoName) throws IOException {
        return new RepositoryService().getRepository(ownerLogin, repoName);
    }
}
