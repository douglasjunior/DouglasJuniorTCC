/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package br.edu.utfpr.cm.JGitMinerWeb.managedBean;

import br.edu.utfpr.cm.JGitMinerWeb.dao.GenericDao;
import br.edu.utfpr.cm.JGitMinerWeb.pojo.EntityRepository;
import br.edu.utfpr.cm.JGitMinerWeb.services.RepositoryServices;
import br.edu.utfpr.cm.JGitMinerWeb.util.JsfUtil;
import java.util.List;
import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.RequestScoped;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;
import org.eclipse.egit.github.core.Repository;
import org.eclipse.egit.github.core.service.RepositoryService;

@ManagedBean(name = "repositoryBean")
@RequestScoped
public class RepositoryBean {

    @EJB
    private GenericDao dao;
    private EntityRepository repository;
    private EntityRepository repositorySelected;
    private String repositoryName;
    private String repositoryOwnerLogin;

    public RepositoryBean() {
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
        if (this.repositoryName == null || this.repositoryName.isEmpty()
                || this.repositoryOwnerLogin == null || this.repositoryOwnerLogin.isEmpty()) {
            JsfUtil.addErrorMessage("Informe o nome do repositorio desejado.", "");
        } else {
            try {


                Repository gitRepository = new RepositoryService().getRepository(this.repositoryOwnerLogin, this.repositoryName);

                System.err.println("Repositório: " + gitRepository.getName() + " | " + gitRepository.getOwner().getLogin() + " | " + gitRepository.getCreatedAt() + " | " + gitRepository.getHtmlUrl());

                repository = RepositoryServices.createEntity(gitRepository, dao);

                JsfUtil.addSuccessMessage("Repositorio salvo com sucesso.", "");
            } catch (Exception e) {
                e.printStackTrace();
                JsfUtil.addErrorMessage("Erro ao salvar Repositorio.<br />" + e.getMessage(), "");
            }
        }
    }

    public List<EntityRepository> getAllRepositories() {
        return dao.selectAll(EntityRepository.class);
    }

    @FacesConverter(forClass = EntityRepository.class)
    public static class RepositoryConverter implements Converter {

        @Override
        public Object getAsObject(FacesContext facesContext, UIComponent component, String value) {
            if (value == null || value.length() == 0 || value.equals("null")) {
                return null;
            }
            RepositoryBean bean = (RepositoryBean) facesContext.getApplication().getELResolver().
                    getValue(facesContext.getELContext(), null, "repositoryBean");
            return bean.dao.findByID(getKey(value), EntityRepository.class);
        }

        java.lang.Long getKey(String value) {
            java.lang.Long key;
            key = Long.valueOf(value);
            return key;
        }

        String getStringKey(java.lang.Long value) {
            StringBuilder sb = new StringBuilder();
            sb.append(value);
            return sb.toString();
        }

        @Override
        public String getAsString(FacesContext facesContext, UIComponent component, Object object) {
            if (object == null) {
                return null;
            }
            if (object instanceof EntityRepository) {
                EntityRepository o = (EntityRepository) object;
                return getStringKey(o.getId());
            } else {
                throw new IllegalArgumentException("object " + object + " is of type " + object.getClass().getName() + "; expected type: " + RepositoryBean.class.getName());
            }
        }
    }

    public RepositoryConverter getConverter() {
        return new RepositoryConverter();
    }
}
