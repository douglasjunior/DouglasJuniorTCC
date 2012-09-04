package br.edu.utfpr.cm.JGitMinerWeb.managedBean;

import br.edu.utfpr.cm.JGitMinerWeb.dao.CommentDao;
import br.edu.utfpr.cm.JGitMinerWeb.dao.IssueDao;
import br.edu.utfpr.cm.JGitMinerWeb.dao.UserDao;
import br.edu.utfpr.cm.JGitMinerWeb.pojo.EntityComment;
import br.edu.utfpr.cm.JGitMinerWeb.pojo.EntityIssue;
import br.edu.utfpr.cm.JGitMinerWeb.pojo.EntityRepository;
import br.edu.utfpr.cm.JGitMinerWeb.services.CommentServices;
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
import org.eclipse.egit.github.core.Comment;
import org.eclipse.egit.github.core.Issue;
import org.eclipse.egit.github.core.Repository;
import org.eclipse.egit.github.core.service.IssueService;

@ManagedBean(name = "issuesAndCommentsBean")
@SessionScoped
public class IssuesAndCommentsBean implements Serializable {

    @EJB
    private IssueDao issueDao;
    @EJB
    private CommentDao commentDao;
    @EJB
    private UserDao userDao;
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

                        Repository gitRepo = RepositoryServices.getGitRepository(repositoryToMiner.getOwner().getLogin(), repositoryToMiner.getName());

                        List<Issue> issues = IssueServices.getGitIssuesFromRepository(gitRepo, minerOpenIssues, minerClosedIssues);

                        progress = new Integer(10);

                        minerIssues(issues, gitRepo);

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

    private void minerIssues(List<Issue> issues, Repository gitRepo) throws Exception {
        int i = 0;
        while (!canceled && i < issues.size()) {
            Issue gitIssue = issues.get(i);
            EntityIssue issue = minerIssue(gitIssue);
            if (issue != null && minerComments) {
                minerComments(issue, gitRepo);
            }
            issueDao.edit(issue);
            calcularProgresso(i, issues.size());
            i++;
        }
    }

    private EntityIssue minerIssue(Issue gitIssue) {
        EntityIssue issue = null;
        try {
            issue = IssueServices.createEntity(gitIssue, issueDao, userDao);
            issue.setRepository(repositoryToMiner);
            out.printLog("Isseu gravada com sucesso: " + gitIssue.getTitle() + " - ID: " + gitIssue.getId());
        } catch (Exception ex) {
            ex.printStackTrace();
            System.out.println("getAssignee: " + issue.getAssignee());
            System.out.println("getUserIssue: " + issue.getUserIssue());
            out.printLog("## Erro ao gravar Issue: " + gitIssue.getTitle() + " - ID: " + gitIssue.getId() + " Descrição: " + ex.toString());
        }
        return issue;
    }

    private void minerComments(EntityIssue issue, Repository gitRepo) throws Exception {
        out.printLog("");
        out.printLog("Baixando Comentários...");
        List<Comment> gitComments = new IssueService().getComments(gitRepo, issue.getNumber());
        out.printLog(gitComments.size() + " Comentários baixados.");
        int i = 0;
        while (!canceled && i < gitComments.size()) {
            Comment gitComment = gitComments.get(i);
            minerComment(gitComment, issue);
            i++;
        }
    }

    private void minerComment(Comment gitComment, EntityIssue issue) {
        try {
            EntityComment comment = CommentServices.createEntity(gitComment, commentDao, userDao);
            issue.addComment(comment);
            out.printLog("Comment gravado com sucesso: " + gitComment.getUrl() + " - ID: " + gitComment.getId());
        } catch (Exception ex) {
            ex.printStackTrace();
            out.printLog("## Erro ao gravar Comment: " + gitComment.getUrl() + " - ID: " + gitComment.getId() + " Descrição: " + ex.toString());
        }
    }

    public void teste() {
        EntityIssue issue = new EntityIssue();
        issue.setIdIssue(234234234);
        issue.setUserIssue(userDao.findByID(new Long(12)));
        issueDao.insert(issue);
        System.out.println("Gravou issue: " + issue);
    }
}
