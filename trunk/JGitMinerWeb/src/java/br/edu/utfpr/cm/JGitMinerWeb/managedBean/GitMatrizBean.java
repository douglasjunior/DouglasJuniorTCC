/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package br.edu.utfpr.cm.JGitMinerWeb.managedBean;

import br.edu.utfpr.cm.JGitMinerWeb.dao.GenericDao;
import br.edu.utfpr.cm.JGitMinerWeb.pojo.matriz.EntityMatriz;
import br.edu.utfpr.cm.JGitMinerWeb.pojo.miner.EntityRepository;
import br.edu.utfpr.cm.JGitMinerWeb.services.matriz.MatrizServices;
import br.edu.utfpr.cm.JGitMinerWeb.services.matriz.UserCommentInIssueMatrizServices;
import br.edu.utfpr.cm.JGitMinerWeb.util.JsfUtil;
import br.edu.utfpr.cm.JGitMinerWeb.util.OutLog;
import java.io.Serializable;
import java.util.Date;
import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;

/**
 *
 * @author douglas
 */
@ManagedBean(name = "gitMatrizBean")
@SessionScoped
public class GitMatrizBean implements Serializable {


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
    public GitMatrizBean() {
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
        final EntityMatriz entityNet = new EntityMatriz();
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

        entityNet.setLog(out.getLog());
        dao.edit(entityNet);

        if (repository == null) {
            message = "Erro: Escolha o repositorio desejado.";
            out.printLog(message);
            progress = new Integer(0);
            initialized = false;
            fail = true;
            entityNet.setLog(out.getLog());
            dao.edit(entityNet);
        } else {
            initialized = true;
            progress = new Integer(10);
            entityNet.setRepository(repository);

            final MatrizServices netServices = new UserCommentInIssueMatrizServices(dao, repository, beginDate, endDate);

            process = new Thread(netServices) {
                @Override
                public void run() {
                    try {
                        out.setCurrentProcess("Iniciando consulta ao banco de dados.");
                        super.run();
                        out.printLog("Consulta ao banco de dados concluída!");
                        progress = new Integer(50);
                        out.printLog("");
                        out.setCurrentProcess("Iniciando processamento dos dados coletados.");
                        entityNet.setRecords(netServices.getRecords());
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
                    entityNet.setLog(out.getLog());
                    entityNet.setStoped(new Date());
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
}
