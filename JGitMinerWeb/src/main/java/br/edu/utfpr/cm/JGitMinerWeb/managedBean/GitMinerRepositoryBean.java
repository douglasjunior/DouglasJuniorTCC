package br.edu.utfpr.cm.JGitMinerWeb.managedBean;

import br.edu.utfpr.cm.JGitMinerWeb.dao.GenericDao;
import br.edu.utfpr.cm.JGitMinerWeb.model.miner.EntityRepository;
import br.edu.utfpr.cm.JGitMinerWeb.services.miner.AuthServices;
import br.edu.utfpr.cm.JGitMinerWeb.services.miner.RepositoryServices;
import br.edu.utfpr.cm.JGitMinerWeb.util.JsfUtil;
import java.io.Serializable;
import java.util.List;
import javax.ejb.EJB;
import javax.enterprise.context.RequestScoped;
import javax.inject.Named;
import org.eclipse.egit.github.core.Repository;
import org.eclipse.egit.github.core.service.RepositoryService;

@Named
@RequestScoped
public class GitMinerRepositoryBean implements Serializable {

    @EJB
    private GenericDao dao;
    private EntityRepository repository;
    private EntityRepository repositorySelected;
    private String repositoryName;
    private String repositoryOwnerLogin;
    private String repositoryUrl;

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

    public String getRepositoryUrl() {
        return repositoryUrl;
    }

    public void setRepositoryUrl(String repositoryUrl) {
        this.repositoryUrl = repositoryUrl;
    }

    public void goMiner() {
        System.out.println("gotMiner Repository");
        try {
            if (this.repositoryUrl != null && !this.repositoryUrl.isEmpty()) {
                parseRepositoryUrl();
            } 
            
            if (this.repositoryName == null || this.repositoryName.isEmpty()
                    || this.repositoryOwnerLogin == null || this.repositoryOwnerLogin.isEmpty()) {
                throw new RuntimeException("Informe o nome e login do repositorio desejado, ou a URL para a página do GitHub.");
            }

            Repository gitRepository = new RepositoryService(AuthServices.getGitHubClient()).getRepository(this.repositoryOwnerLogin, this.repositoryName);

            System.err.println("Repositório: " + gitRepository.getName() + " | " + gitRepository.getOwner().getLogin() + " | " + gitRepository.getCreatedAt() + " | " + gitRepository.getHtmlUrl());

            repository = RepositoryServices.createEntity(gitRepository, dao, true);

            JsfUtil.addSuccessMessage("Repositorio salvo com sucesso.");
        } catch (Exception e) {
            e.printStackTrace();
            JsfUtil.addErrorMessage("Erro ao salvar Repositorio.<br />Descrição: " + e.getMessage());
        }
    }

    public List<EntityRepository> getAllRepositories() {
        return dao.selectAll(EntityRepository.class);
    }

    public List<EntityRepository> getRepositoriesPrimaryMiner() {
        return dao.executeNamedQuery("Repository.findByPrimaryMiner");
    }

    private void parseRepositoryUrl() {
        this.repositoryUrl = this.repositoryUrl.toLowerCase().trim();
        String[] tokens = this.repositoryUrl.split("github.com/");
        tokens = tokens[1].split("/");
        this.repositoryOwnerLogin = tokens[0];
        this.repositoryName = tokens[1];
    }
}
