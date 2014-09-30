package br.edu.utfpr.cm.JGitMinerWeb.managedBean;

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
        return dao.selectNativeWithParams(
                "SELECT distinct(replace(replace(schema_name, '_vcs', ''), '_issues', '')) "
                + "  FROM information_schema.schemata "
                + " WHERE schema_name  LIKE '%_vcs'"
                + "    OR schema_name LIKE '%_issues'", new Object[]{});
    }

}
