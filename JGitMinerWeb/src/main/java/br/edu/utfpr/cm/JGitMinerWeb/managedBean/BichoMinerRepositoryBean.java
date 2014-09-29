package br.edu.utfpr.cm.JGitMinerWeb.managedBean;

import br.edu.utfpr.cm.JGitMinerWeb.dao.GenericBichoDAO;
import java.io.Serializable;
import java.util.List;
import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.RequestScoped;

@ManagedBean
@RequestScoped
public class BichoMinerRepositoryBean implements Serializable {

    @EJB
    private GenericBichoDAO dao;

    public List<String> getAllRepositories() {
        return dao.selectNativeWithParams(
                "SELECT distinct(replace(replace(schema_name, '_vcs', ''), '_issues', '')) "
                + "  FROM information_schema.schemata "
                + " WHERE schema_name  LIKE '%_vcs'"
                + "    OR schema_name LIKE '%_issues'", new Object[]{});
    }

}
