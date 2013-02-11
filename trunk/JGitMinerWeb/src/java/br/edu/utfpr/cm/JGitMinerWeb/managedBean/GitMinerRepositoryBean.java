/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package br.edu.utfpr.cm.JGitMinerWeb.managedBean;

import br.edu.utfpr.cm.JGitMinerWeb.dao.GenericDao;
import br.edu.utfpr.cm.JGitMinerWeb.pojo.miner.EntityRepository;
import br.edu.utfpr.cm.JGitMinerWeb.services.miner.AuthServices;
import br.edu.utfpr.cm.JGitMinerWeb.services.miner.RepositoryServices;
import br.edu.utfpr.cm.JGitMinerWeb.util.JsfUtil;
import java.io.Serializable;
import java.util.List;
import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.RequestScoped;
import org.eclipse.egit.github.core.Repository;
import org.eclipse.egit.github.core.service.RepositoryService;

@ManagedBean(name = "gitMinerRepositoryBean")
@RequestScoped
public class GitMinerRepositoryBean implements Serializable {

    @EJB
    private GenericDao dao;
    private EntityRepository repository;
    private EntityRepository repositorySelected;
    private String repositoryName;
    private String repositoryOwnerLogin;

    public GitMinerRepositoryBean() {
        repository = new EntityRepository();
        repositorySelected = new EntityRepository();
    }

    public EntityRepository getRepository() {
        return repository;
    }

    public void setRepository(EntityRepository repository) {
        this.repository = repository;
    }

    public EntityRepository getRepositorySelected() {
        return repositorySelected;
    }

    public void setRepositorySelected(EntityRepository repositorySelected) {
        this.repositorySelected = repositorySelected;
    }

    public String getRepositoryName() {
        return repositoryName;
    }

    public void setRepositoryName(String repositoryName) {
        this.repositoryName = repositoryName;
    }

    public String getRepositoryOwner() {
        return repositoryOwnerLogin;
    }

    public void setRepositoryOwner(String repositoryOwner) {
        this.repositoryOwnerLogin = repositoryOwner;
    }

    public void goMiner() {
        System.out.println("gotMiner Repository");
        if (this.repositoryName == null || this.repositoryName.isEmpty()
                || this.repositoryOwnerLogin == null || this.repositoryOwnerLogin.isEmpty()) {
            JsfUtil.addErrorMessage("Informe o nome do repositorio desejado.");
        } else {
            try {
                Repository gitRepository = new RepositoryService(AuthServices.getGitHubCliente()).getRepository(this.repositoryOwnerLogin, this.repositoryName);

                System.err.println("Repositório: " + gitRepository.getName() + " | " + gitRepository.getOwner().getLogin() + " | " + gitRepository.getCreatedAt() + " | " + gitRepository.getHtmlUrl());

                repository = RepositoryServices.createEntity(gitRepository, dao, true);

                JsfUtil.addSuccessMessage("Repositorio salvo com sucesso.");
            } catch (Exception e) {
                e.printStackTrace();
                JsfUtil.addErrorMessage("Erro ao salvar Repositorio.<br />Descrição: " + e.getMessage());
            }
        }
    }

    public List<EntityRepository> getAllRepositories() {
        return dao.selectAll(EntityRepository.class);
    }

    public List<EntityRepository> getRepositoriesPrimaryMiner() {
        return dao.executeNamedQuery("Repository.findByPrimaryMiner");
    }
}
