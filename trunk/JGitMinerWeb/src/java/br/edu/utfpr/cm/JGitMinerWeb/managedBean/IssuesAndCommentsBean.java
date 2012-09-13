package br.edu.utfpr.cm.JGitMinerWeb.managedBean;

import br.edu.utfpr.cm.JGitMinerWeb.dao.CommentDao;
import br.edu.utfpr.cm.JGitMinerWeb.dao.IssueDao;
import br.edu.utfpr.cm.JGitMinerWeb.dao.MinerDao;
import br.edu.utfpr.cm.JGitMinerWeb.dao.UserDao;
import br.edu.utfpr.cm.JGitMinerWeb.pojo.*;
import br.edu.utfpr.cm.JGitMinerWeb.services.CommentServices;
import br.edu.utfpr.cm.JGitMinerWeb.services.IssueServices;
import br.edu.utfpr.cm.JGitMinerWeb.services.RepositoryServices;
import br.edu.utfpr.cm.JGitMinerWeb.services.UserServices;
import br.edu.utfpr.cm.JGitMinerWeb.util.out;
import java.io.Serializable;
import java.util.Date;
import java.util.List;
import javax.ejb.EJB;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;
import org.eclipse.egit.github.core.Comment;
import org.eclipse.egit.github.core.Issue;
import org.eclipse.egit.github.core.Repository;
import org.eclipse.egit.github.core.User;
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
    @EJB
    private MinerDao minerDao;
    private EntityRepository repositoryToMiner;
    private boolean minerOpenIssues;
    private boolean minerClosedIssues;
    private boolean minerComments;
    private boolean minerCollaborators;
    private boolean minerWatchers;
    private boolean minerPullRequests;
    private boolean minerTeams;
    private boolean initialized;
    private Integer progress;
    private Integer subProgress;
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

    public boolean isMinerCollaborators() {
        return minerCollaborators;
    }

    public void setMinerCollaborators(boolean minerCollaborators) {
        this.minerCollaborators = minerCollaborators;
    }

    public boolean isMinerPullRequests() {
        return minerPullRequests;
    }

    public void setMinerPullRequests(boolean minerPullRequests) {
        this.minerPullRequests = minerPullRequests;
    }

    public boolean isMinerTeams() {
        return minerTeams;
    }

    public void setMinerTeams(boolean minerTeams) {
        this.minerTeams = minerTeams;
    }

    public boolean isMinerWatchers() {
        return minerWatchers;
    }

    public void setMinerWatchers(boolean minerWatchers) {
        this.minerWatchers = minerWatchers;
    }

    public EntityRepository getRepositoryToMiner() {
        return repositoryToMiner;
    }

    public void setRepositoryToMiner(EntityRepository repositoryToMiner) {
        this.repositoryToMiner = repositoryToMiner;
    }

    public String getCurrentProcess() {
        return out.getCurrentProcess();
    }

    public String getLog() {
        return out.getLog();
    }

    public void start() {
        final EntityMiner mineration = new EntityMiner();
        minerDao.insert(mineration);

        out.resetLog();
        initialized = false;
        canceled = false;

        out.printLog("Repositorio: " + repositoryToMiner);
        out.printLog("minerOpenIssues: " + minerOpenIssues);
        out.printLog("minerClosedIssues: " + minerClosedIssues);
        out.printLog("minerComments: " + minerComments);
        out.printLog("minerCollaborators: " + minerCollaborators);
        out.printLog("minerWatchers: " + minerWatchers);
        out.printLog("minerPullRequests: " + minerPullRequests);
        out.printLog("minerTeams: " + minerTeams);
        out.printLog("");

        if (repositoryToMiner == null) {
            message = "Erro: Escolha o repositorio desejado.";
            out.printLog(message);
            progress = new Integer(0);
            subProgress = new Integer(0);
            initialized = false;
            mineration.setMinerLog(out.getLog());
            minerDao.edit(mineration);
            return;
        }
        process = new Thread() {

            @Override
            public void run() {

                out.printLog("########### PROCESSO DE MINERAÇÃO INICIADO! ##############\n");

                try {
                    initialized = true;
                    progress = new Integer(1);
                    Repository gitRepo = RepositoryServices.getGitRepository(repositoryToMiner.getOwner().getLogin(), repositoryToMiner.getName());
                    progress = new Integer(10);
                    if (minerOpenIssues || minerClosedIssues) {
                        subProgress = new Integer(0);
                        out.setCurrentProcess("Mirerando issues...\n");
                        List<Issue> issues = IssueServices.getGitIssuesFromRepository(gitRepo, minerOpenIssues, minerClosedIssues);
                        minerIssues(issues, gitRepo);
                        mineration.setMinerLog(out.getLog());
                        minerDao.edit(mineration);
                    }
                    progress = new Integer(30);
                    if (minerCollaborators) {
                        subProgress = new Integer(0);
                        out.setCurrentProcess("Mirerando collaborators...\n");
                        List<User> collaborators = UserServices.getGitCollaboratorsFromRepository(gitRepo);
                        minerCollaborators(collaborators);
                        mineration.setMinerLog(out.getLog());
                        minerDao.edit(mineration);
                    }
                    progress = new Integer(60);
                    if (minerWatchers) {
                        subProgress = new Integer(0);
                        out.setCurrentProcess("Mirerando watchers...\n");
                        List<User> wacthers = UserServices.getGitWatchersFromRepository(gitRepo);
                        minerWatchers(wacthers);
                        mineration.setMinerLog(out.getLog());
                        minerDao.edit(mineration);
                    }
                    if (canceled) {
                        out.printLog("Processo de mineração interrompido.\n");
                    }
                    mineration.setMinerSucess(true);
                    mineration.setMinerStop(new Date());
                    message = "Mineração finalizada.";
                } catch (Exception ex) {
                    mineration.setMinerSucess(false);
                    mineration.setMinerStop(new Date());
                    ex.printStackTrace();
                    message = "Mineração abortada:\n" + ex.toString();
                }
                out.setCurrentProcess(message);
                progress = new Integer(100);
                initialized = false;
                out.printLog(message + "\n");
                mineration.setMinerLog(out.getLog());
                minerDao.edit(mineration);
            }
        };
        process.start();
    }

    public void cancel() {
        out.printLog("Pedido de cancelamento enviado.\n");
        canceled = true;
        process.interrupt();
        System.gc();
    }

    public Integer getProgress() {
        if (progress != null && progress > 100) {
            progress = new Integer(100);
        }
        return progress;
    }

    public Integer getSubProgress() {
        if (subProgress != null && subProgress > 100) {
            subProgress = new Integer(100);
        }
        return subProgress;
    }

    public void onComplete() {
        out.printLog("onComplete" + '\n');
        initialized = false;
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, message, message));
    }

    private void minerIssues(List<Issue> issues, Repository gitRepo) throws Exception {
        int i = 0;
        calculeSubProgress(i, issues.size());
        while (!canceled && i < issues.size()) {
            Issue gitIssue = issues.get(i);
            EntityIssue issue = minerIssue(gitIssue);
            if (issue != null && minerComments) {
                minerComments(issue, gitRepo);
            }
            issue.setRepository(repositoryToMiner);
            issueDao.edit(issue);
            i++;
            calculeSubProgress(i, issues.size());
        }
    }

    private EntityIssue minerIssue(Issue gitIssue) {
        EntityIssue issue = null;
        try {
            issue = IssueServices.createEntity(gitIssue, issueDao, userDao);
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

    private void minerCollaborators(List<User> collaborators) {
        int i = 0;
        calculeSubProgress(i, collaborators.size());
        while (!canceled && i < collaborators.size()) {
            User gitCollab = collaborators.get(i);
            EntityUser colab = minerCollaborator(gitCollab);
            colab.addCollaboratedRepository(repositoryToMiner);
            userDao.edit(colab);
            i++;
            calculeSubProgress(i, collaborators.size());
        }
    }

    private EntityUser minerCollaborator(User gitCollab) {
        EntityUser colab = null;
        try {
            colab = UserServices.createEntity(gitCollab, userDao);
            out.printLog("Collaborator gravado com sucesso: " + gitCollab.getLogin() + " - ID: " + gitCollab.getId());
        } catch (Exception ex) {
            ex.printStackTrace();
            out.printLog("## Erro ao gravar Collaborator: " + gitCollab.getLogin() + " - ID: " + gitCollab.getId() + " Descrição: " + ex.toString());
        }
        return colab;
    }

    private void minerWatchers(List<User> watchers) {
        int i = 0;
        calculeSubProgress(i, watchers.size());
        while (!canceled && i < watchers.size()) {
            User gitWatcher = watchers.get(i);
            EntityUser watcher = minerWatcher(gitWatcher);
            watcher.addWatchedRepository(repositoryToMiner);
            userDao.edit(watcher);
            i++;
            calculeSubProgress(i, watchers.size());
        }
    }

    private EntityUser minerWatcher(User gitWatcher) {
        EntityUser watcher = null;
        try {
            watcher = UserServices.createEntity(gitWatcher, userDao);
            out.printLog("Watcher gravado com sucesso: " + gitWatcher.getLogin() + " - ID: " + gitWatcher.getId());
        } catch (Exception ex) {
            ex.printStackTrace();
            out.printLog("## Erro ao gravar Watcher: " + gitWatcher.getLogin() + " - ID: " + gitWatcher.getId() + " Descrição: " + ex.toString());
        }
        return watcher;
    }

    public void teste() {
        EntityIssue issue = new EntityIssue();
        issue.setIdIssue(234234234);
        issue.setUserIssue(userDao.findByID(new Long(12)));
        issueDao.insert(issue);
        System.out.println("Gravou issue: " + issue);
    }

    private void calculeSubProgress(double i, double size) {
        double subProg = (i / size) * 100;
        subProgress = new Integer((int) subProg);
    }
}
