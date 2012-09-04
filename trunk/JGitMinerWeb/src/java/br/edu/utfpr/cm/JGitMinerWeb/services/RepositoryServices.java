/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package br.edu.utfpr.cm.JGitMinerWeb.services;

import br.edu.utfpr.cm.JGitMinerWeb.dao.RepositoryDao;
import br.edu.utfpr.cm.JGitMinerWeb.dao.UserDao;
import br.edu.utfpr.cm.JGitMinerWeb.pojo.EntityRepository;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import org.eclipse.egit.github.core.Repository;
import org.eclipse.egit.github.core.service.RepositoryService;

/**
 *
 * @author Douglas
 */
public class RepositoryServices {

    private static RepositoryDao dao;

    private static EntityRepository getRepositoryByName(String name) {
        List<EntityRepository> users = dao.executeNamedQueryComParametros("Repository.findByName", new String[]{"name"}, new Object[]{name});
        if (!users.isEmpty()) {
            return (EntityRepository) dao.findByID(users.get(0).getId());
        }
        return null;
    }

    public static EntityRepository createEntity(Repository gitRepository, RepositoryDao repositoryDao, UserDao userDao) {
        if (gitRepository == null) {
            return null;
        }

        RepositoryServices.dao = repositoryDao;
        EntityRepository repo = getRepositoryByName(gitRepository.getName());

        if (repo == null) {
            repo = new EntityRepository();
        }

        repo.setMineredAt(new Date());
        repo.setFork(gitRepository.isFork());
        repo.setHasDownloads(gitRepository.isHasDownloads());
        repo.setHasIssues(gitRepository.isHasIssues());
        repo.setHasWiki(gitRepository.isHasWiki());
        repo.setIsPrivate(gitRepository.isPrivate());
        repo.setCreatedAt(gitRepository.getCreatedAt());
        repo.setPushedAt(gitRepository.getPushedAt());
        repo.setUpdatedAt(gitRepository.getUpdatedAt());
        repo.setForks(gitRepository.getForks());
        repo.setIdRepository(gitRepository.getId());
        repo.setOpenIssues(gitRepository.getOpenIssues());
        repo.setSizeRepository(gitRepository.getSize());
        repo.setWatchers(gitRepository.getWatchers());
        repo.setParent(RepositoryServices.createEntity(gitRepository.getParent(), repositoryDao, userDao));
        repo.setSource(RepositoryServices.createEntity(gitRepository.getSource(), repositoryDao, userDao));
        repo.setCloneUrl(gitRepository.getCloneUrl());
        repo.setDescription(gitRepository.getDescription());
        repo.setHomepage(gitRepository.getHomepage());
        repo.setGitUrl(gitRepository.getGitUrl());
        repo.setHtmlUrl(gitRepository.getHtmlUrl());
        repo.setLanguageRepository(gitRepository.getLanguage());
        repo.setMasterBranch(gitRepository.getMasterBranch());
        repo.setMirrorUrl(gitRepository.getMirrorUrl());
        repo.setName(gitRepository.getName());
        repo.setSshUrl(gitRepository.getSshUrl());
        repo.setSvnUrl(gitRepository.getSvnUrl());
        repo.setUrl(gitRepository.getUrl());
        repo.setOwner(UserServices.createEntity(gitRepository.getOwner(), userDao));

        return repo;
    }

    public static Repository getGitRepository(String ownerLogin, String repoName) throws IOException {
        return new RepositoryService().getRepository(ownerLogin, repoName);
    }
}
