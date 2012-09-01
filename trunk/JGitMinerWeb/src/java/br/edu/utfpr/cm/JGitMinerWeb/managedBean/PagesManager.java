package br.edu.utfpr.cm.JGitMinerWeb.managedBean;

import java.io.Serializable;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;

@ManagedBean(name = "pagesManager")
@SessionScoped
public class PagesManager implements Serializable {

    private String header = "./pages/header.xhtml";
    private String menu = "./pages/menu.xhtml";
    private String body = "./pages/home.xhtml";
    private String footer = "./pages/footer.xhtml";

    public PagesManager() {
    }

    public String getBody() {
        return body;
    }

    public String getFooter() {
        return footer;
    }

    public String getHeader() {
        return header;
    }

    public String getMenu() {
        return menu;
    }

    public String home() {
        this.body = "./pages/home.xhtml";
        return "refreshPage";
    }

    public String minerRepository() {
        this.body = "./pages/miner/repository.xhtml";
        return "refreshPage";
    }

    public String minerIssuesAndComments() {
        this.body = "./pages/miner/issuesAndComments.xhtml";
        return "refreshPage";
    }

    public String minerPullRequests() {
        this.body = "./pages/miner/pullRequests.xhtml";
        return "refreshPage";
    }
}
