package br.edu.utfpr.cm.JGitMinerWeb.managedBean;

import br.edu.utfpr.cm.JGitMinerWeb.services.miner.AuthServices;
import java.io.Serializable;
import javax.enterprise.context.SessionScoped;
import javax.inject.Named;

@Named
@SessionScoped
public class PagesManager implements Serializable {

    // static pages
    private static final String header = "./pages/header.xhtml";
    private static final String menu = "./pages/menu.xhtml";
    private static final String footer = "./pages/footer.xhtml";

    // dynamic page se
    private String body = "./pages/home.xhtml";

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

    public String minerOthers() {
        this.body = "./pages/miner/others.xhtml";
        return "refreshPage";
    }

    public String minerView() {
        this.body = "./pages/miner/view.xhtml";
        return "refreshPage";
    }

    public String matrixCreate() {
        this.body = "./pages/matrix/create.xhtml";
        return "refreshPage";
    }

    public String matrixCreateQueue() {
        this.body = "./pages/matrix/createQueue.xhtml";
        return "refreshPage";
    }

    public String bichoMatrixCreateQueue() {
        this.body = "./pages/matrix/bichoCreateQueue.xhtml";
        return "refreshPage";
    }

    public String matrixView() {
        this.body = "./pages/matrix/view.xhtml";
        return "refreshPage";
    }

    public String metricCreate() {
        this.body = "./pages/metric/create.xhtml";
        return "refreshPage";
    }

    public String metricCreateQueue() {
        this.body = "./pages/metric/createQueue.xhtml";
        return "refreshPage";
    }

    public String metricView() {
        this.body = "./pages/metric/view.xhtml";
        return "refreshPage";
    }

    public int getClientCount() {
        return AuthServices.getClientCount();
    }
}
