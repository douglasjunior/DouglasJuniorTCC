/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package br.edu.utfpr.cm.JGitMinerWeb.managedBean;

import br.edu.utfpr.cm.JGitMinerWeb.dao.GenericDao;
import br.edu.utfpr.cm.JGitMinerWeb.edge.AbstractEdge;
import br.edu.utfpr.cm.JGitMinerWeb.net.UserCommentInIssueNet;
import br.edu.utfpr.cm.JGitMinerWeb.pojo.EntityNet;
import br.edu.utfpr.cm.JGitMinerWeb.pojo.EntityRepository;
import br.edu.utfpr.cm.JGitMinerWeb.util.JsfUtil;
import br.edu.utfpr.cm.JGitMinerWeb.util.OutLog;
import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.io.Serializable;
import java.util.Date;
import java.util.List;
import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;

/**
 *
 * @author douglas
 */
@ManagedBean(name = "gitNetBean")
@SessionScoped
public class GitNetBean implements Serializable {

    private final String STR_NET_FOR_DELETE = "netForDelete";
    /*
     * 
     */
    @EJB
    private GenericDao dao;
    private OutLog out;
    private EntityRepository repository;
    private String message;
    private Date beginDate;
    private Date endDate;
    private Thread process;
    private Integer progress;
    private boolean initialized;
    private boolean fail;
    private boolean canceled;

    /**
     * Creates a new instance of GitNet
     */
    public GitNetBean() {
        out = new OutLog();
    }

    public boolean isFail() {
        return fail;
    }

    public void setFail(boolean fail) {
        this.fail = fail;
    }

    public boolean isCanceled() {
        return canceled;
    }

    public void setCanceled(boolean canceled) {
        this.canceled = canceled;
    }

    public EntityRepository getRepository() {
        return repository;
    }

    public void setRepository(EntityRepository repository) {
        this.repository = repository;
    }

    public GenericDao getDao() {
        return dao;
    }

    public void setDao(GenericDao dao) {
        this.dao = dao;
    }

    public Date getBeginDate() {
        return beginDate;
    }

    public void setBeginDate(Date beginDate) {
        this.beginDate = beginDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public boolean isInitialized() {
        return initialized;
    }

    public void setInitialized(boolean initialized) {
        this.initialized = initialized;
    }

    public String getLog() {
        if (out.getLog().length() > 999999) {
            return out.getLog().substring(0, 999999);
        }
        return out.getLog();
    }

    public Integer getProgress() {
        if (fail) {
            progress = new Integer(100);
        } else if (progress == null) {
            progress = new Integer(0);
        } else if (progress > 100) {
            progress = new Integer(100);
        }
        System.out.println("progress: " + progress);
        return progress;
    }

    public void setProgress(Integer progress) {
        this.progress = progress;
    }

    public void start() {
        final EntityNet entityNet = new EntityNet();
        dao.insert(entityNet);
        out.resetLog();
        initialized = false;
        canceled = false;
        fail = false;
        progress = new Integer(0);

        out.printLog("Geração da rede iniciada!");
        out.printLog("");
        out.printLog("Begin Date: " + beginDate);
        out.printLog("End Date: " + endDate);
        out.printLog("Repository: " + repository);
        out.printLog("");

        entityNet.setNetLog(out.getLog());
        dao.edit(entityNet);

        if (repository == null) {
            message = "Erro: Escolha o repositorio desejado.";
            out.printLog(message);
            progress = new Integer(0);
            initialized = false;
            fail = true;
            entityNet.setNetLog(out.getLog());
            dao.edit(entityNet);
        } else {
            initialized = true;
            progress = new Integer(10);
            entityNet.setRepository(repository);

            final UserCommentInIssueNet net = new UserCommentInIssueNet(repository, beginDate, endDate, dao);

            process = new Thread(net) {
                @Override
                public void run() {
                    try {
                        out.setCurrentProcess("Iniciando consulta ao banco de dados.");
                        super.run();
                        out.printLog("Consulta ao banco de dados concluída!");
                        progress = new Integer(50);
                        out.printLog("");
                        out.setCurrentProcess("Iniciando processamento dos dados coletados.");
                        entityNet.setNetResult(convertResultToString(net.getNet()));
                        out.printLog("Processamento dos dados concluído!");
                        entityNet.setComplete(true);
                        dao.edit(entityNet);
                        message = "Geração da rede concluída.";
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        message = "Geração da rede abortada: " + ex.toString();
                        fail = true;
                    }
                    System.gc();
                    out.printLog("");
                    out.setCurrentProcess(message);
                    progress = new Integer(100);
                    initialized = false;
                    entityNet.setNetLog(out.getLog());
                    entityNet.setNetStop(new Date());
                    dao.edit(entityNet);
                }
            };
            process.start();
        }
    }

    public void cancel() {
        if (initialized) {
            out.printLog("Pedido de cancelamento enviado.\n");
            canceled = true;
            try {
                process.interrupt();
            } catch (Exception ex) {
            }
        }
    }

    public void onComplete() {
        out.printLog("onComplete" + '\n');
        initialized = false;
        progress = new Integer(0);
        if (fail) {
            JsfUtil.addErrorMessage(message);
        } else {
            JsfUtil.addSuccessMessage(message);
        }
    }

    private String convertResultToString(List<AbstractEdge> net) {
        StringBuilder sb = new StringBuilder();
        for (AbstractEdge edge : net) {
            sb.append(edge.getStringY()).append(JsfUtil.TOKEN_SEPARATOR).append(edge.getStringX()).append(JsfUtil.TOKEN_SEPARATOR).append(edge.getValue()).append("\n");
        }
        System.out.println("Saida: ##################################################### \n" + sb.toString());
        return sb.toString();
    }

    public List<EntityNet> getNets() {
        return dao.executeNamedQuery("Net.findAllTheLatest");
    }

    public void downloadCSV(EntityNet net) {
        try {
            String fileName = net.getRepository().getName() + "-" + net.getNetStart() + ".txt";

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            PrintWriter pw = new PrintWriter(baos);

            String[] linhas = net.getNetResult().split("\n");

            for (String linha : linhas) {
                pw.println(linha);
            }

            pw.flush();
            pw.close();

            JsfUtil.downloadFile(fileName, baos.toByteArray());
        } catch (Exception ex) {
            ex.printStackTrace();
            JsfUtil.addErrorMessage(ex.toString());
        }
    }

    public void deleteNetInSession() {
        try {
            EntityNet netForDelete = (EntityNet) JsfUtil.getObjectFromSession(STR_NET_FOR_DELETE);
            dao.remove(netForDelete);
        } catch (Exception ex) {
            ex.printStackTrace();
            JsfUtil.addErrorMessage(ex.toString());
        }
        removeNetFromSession();
    }

    public void removeNetFromSession() {
        JsfUtil.removeAttributeFromSession(STR_NET_FOR_DELETE);
    }

    public void addNetForDeleteInSession(EntityNet netForDelete) {
        JsfUtil.addAttributeInSession(STR_NET_FOR_DELETE, netForDelete);
    }
}
