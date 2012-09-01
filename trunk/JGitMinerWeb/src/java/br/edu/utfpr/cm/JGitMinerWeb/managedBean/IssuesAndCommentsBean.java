package br.edu.utfpr.cm.JGitMinerWeb.managedBean;

import br.edu.utfpr.cm.JGitMinerWeb.dao.CommentDao;
import br.edu.utfpr.cm.JGitMinerWeb.dao.IssueDao;
import br.edu.utfpr.cm.JGitMinerWeb.pojo.EntityIssue;
import br.edu.utfpr.cm.JGitMinerWeb.pojo.EntityRepository;
import br.edu.utfpr.cm.JGitMinerWeb.services.IssueServices;
import br.edu.utfpr.cm.JGitMinerWeb.services.RepositoryServices;
import br.edu.utfpr.cm.JGitMinerWeb.util.out;
import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import javax.ejb.EJB;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;
import org.eclipse.egit.github.core.Issue;
import org.eclipse.egit.github.core.Repository;

@ManagedBean(name = "issuesAndCommentsBean")
@SessionScoped
public class IssuesAndCommentsBean implements Serializable {

    @EJB
    private IssueDao issueDao;
    @EJB
    private CommentDao commentDao;
    private EntityRepository repositoryToMiner;
    private boolean minerOpenIssues;
    private boolean minerClosedIssues;
    private boolean minerComments;
    private boolean initialized;
    private Integer progress;
    private String message;
    private boolean canceled;
    Thread process;

    public IssuesAndCommentsBean() {
        initialized = false;
        canceled = false;
    }

    public boolean isInitialized() {
        return initialized;
    }

    public boolean isMinerComments() {
        return minerComments;
    }

    public void setMinerComments(boolean minerComments) {
        this.minerComments = minerComments;
    }

    public boolean isMinerClosedIssues() {
        return minerClosedIssues;
    }

    public void setMinerClosedIssues(boolean minerClosedIssues) {
        this.minerClosedIssues = minerClosedIssues;
    }

    public boolean isMinerOpenIssues() {
        return minerOpenIssues;
    }

    public void setMinerOpenIssues(boolean minerOpenIssues) {
        this.minerOpenIssues = minerOpenIssues;
    }

    public EntityRepository getRepositoryToMiner() {
        return repositoryToMiner;
    }

    public void setRepositoryToMiner(EntityRepository repositoryToMiner) {
        this.repositoryToMiner = repositoryToMiner;
    }

    public String getLastLog() {
        return out.getLastLog();
    }

    public String getLog() {
        return out.getLog();
    }

    public void start() {

        out.resetLog();
        initialized = false;
        canceled = false;
        System.out.println(repositoryToMiner + "");

        out.printLog("Repositorio: " + repositoryToMiner);
        out.printLog("minerOpenIssues: " + minerOpenIssues);
        out.printLog("minerClosedIssues: " + minerClosedIssues);
        out.printLog("minerComments: " + minerComments);
        out.printLog("");

        if (repositoryToMiner == null) {
            message = "Erro: Escolha o repositorio desejado.";
            out.printLog(message);
            progress = new Integer(100);
            initialized = false;
        } else if (minerOpenIssues || minerClosedIssues) {
            progress = new Integer(0);
            initialized = true;

            out.printLog("########### PROCESSO DE MINERAÇÃO INICIADO! ##############\n");

            process = new Thread() {

                @Override
                public void run() {
                    try {
                        progress = new Integer(1);

                        Repository gitRepo = RepositoryServices.getGitRepository(repositoryToMiner.getOwnerLogin(), repositoryToMiner.getName());

                        List<Issue> issues = IssueServices.getGitIssuesFromRepository(gitRepo, minerOpenIssues, minerClosedIssues);

                        progress = new Integer(10);

                        int i = 0;
                        while (!canceled && i < issues.size()) {
                            Issue gitIssue = issues.get(i);
                            EntityIssue issue = null;
                            try {
                                issue = IssueServices.createEntity(gitIssue);
                                issue.setRepository(repositoryToMiner);
                                if (issue.getId() == null || issue.getId().equals(new Long(0))) {
                                    issueDao.insert(issue);
                                } else {
                                    issueDao.edit(issue);
                                }
                                out.printLog("Isseu gravada com sucesso: " + issue.getTitle() + " - ID: " + issue.getIdIssue());
                            } catch (Exception ex) {
                                ex.printStackTrace();
                                out.printLog("## Erro ao gravar Issue: " + issue.getTitle() + " - ID: " + issue.getIdIssue() + " Descrição: " + ex.toString());
                            }

                            calcularProgresso(i, issues.size());
                            i++;
                        }

                        if (canceled) {
                            out.printLog("Processo de mineração interrompido.\n");
                        }

                        progress = new Integer(100);
                        initialized = false;
                        message = "Mineração finalizada.";
                        out.printLog(message + "\n");
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        message = "Erro ocorrido, consulte o registo de Log para mais informações.";
                        progress = new Integer(100);
                        initialized = false;
                        out.printLog(ex.toString() + "\n" + Arrays.toString(ex.getStackTrace()) + "\n");
                    }
                }
            };
            process.start();
        } else {
            message = "Erro: É preciso selecionar a mineração de isssues.";
            out.printLog(message);
            progress = new Integer(100);
            initialized = false;
        }
    }

    public void cancel() {
        out.printLog("Pedido de cancelamento enviado.\n");
        canceled = true;
        process.interrupt();
        System.gc();
    }

    public Integer getProgress() {
        System.out.println("## getProgres: " + progress + '\n');
        if (progress != null && progress > 100) {
            progress = new Integer(100);
        }
        return progress;
    }

    public void onComplete() {
        out.printLog("onComplete" + '\n');
        initialized = false;
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, message, message));
    }

    private void calcularProgresso(int i, int size) {
        double iDb = i;
        double sizeDb = size;
        double complete = 90;

        double prog = (iDb / sizeDb * complete) + 10;

        progress = new Integer((int) prog);
    }
}
