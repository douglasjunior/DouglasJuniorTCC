package br.edu.utfpr.cm.JGitMinerWeb.managedBean;

import br.edu.utfpr.cm.JGitMinerWeb.dao.BichoDAO;
import br.edu.utfpr.cm.JGitMinerWeb.dao.GenericBichoDAO;
import java.io.Serializable;
import java.util.List;
import javax.ejb.EJB;
import javax.enterprise.context.SessionScoped;
import javax.inject.Named;

@Named
@SessionScoped
public class BichoMinerRepositoryBean implements Serializable {

    @EJB
    private GenericBichoDAO dao;

    public List<String> getAllRepositories() {
        // TODO refactor
        return new BichoDAO(dao, "", 20).listAllProjects();
    }

}
