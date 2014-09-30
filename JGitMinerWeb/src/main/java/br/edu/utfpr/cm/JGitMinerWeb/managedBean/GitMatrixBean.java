package br.edu.utfpr.cm.JGitMinerWeb.managedBean;

import br.edu.utfpr.cm.JGitMinerWeb.converter.ClassConverter;
import br.edu.utfpr.cm.JGitMinerWeb.dao.GenericDao;
import br.edu.utfpr.cm.JGitMinerWeb.model.matrix.EntityMatrix;
import br.edu.utfpr.cm.JGitMinerWeb.model.matrix.EntityMatrixNode;
import br.edu.utfpr.cm.JGitMinerWeb.model.miner.EntityRepository;
import br.edu.utfpr.cm.JGitMinerWeb.services.matrix.AbstractMatrixServices;
import br.edu.utfpr.cm.JGitMinerWeb.util.JsfUtil;
import br.edu.utfpr.cm.JGitMinerWeb.util.OutLog;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.ejb.EJB;
import javax.enterprise.context.SessionScoped;
import javax.inject.Named;

/**
 *
 * @author douglas
 */
@Named
@SessionScoped
public class GitMatrixBean implements Serializable {

    @EJB
    private GenericDao dao;
    private OutLog out;
    private EntityRepository repository;
    private String repositoryId;
    private Class<?> serviceClass;
    private Map<Object, Object> params;
    private String message;
    private Thread process;
    private Integer progress;
    private boolean initialized;
    private boolean fail;
    private boolean canceled;

    /**
     * Creates a new instance of GitNet
     */
    public GitMatrixBean() {
        out = new OutLog();
        params = new HashMap<>();
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

    public String getRepositoryId() {
        return repositoryId;
    }

    public void setRepositoryId(String repositoryId) {
        this.repositoryId = repositoryId;
    }

    public Class<?> getServiceClass() {
        return serviceClass;
    }

    public void setServiceClass(Class<?> serviceClass) {
        this.serviceClass = serviceClass;
    }

    public Map<Object, Object> getParamValue() {
        return params;
    }

    public GenericDao getDao() {
        return dao;
    }

    public void setDao(GenericDao dao) {
        this.dao = dao;
    }

    public boolean isInitialized() {
        return initialized;
    }

    public void setInitialized(boolean initialized) {
        this.initialized = initialized;
    }

    public String getLog() {
        return out.getSingleLog();
    }

    public Integer getProgress() {
        if (fail) {
            progress = 100;
        } else if (progress == null) {
            progress = 0;
        } else if (progress > 100) {
            progress = 100;
        }
        System.out.println("progress: " + progress);
        return progress;
    }

    public void setProgress(Integer progress) {
        this.progress = progress;
    }

    public void start() {
        out.resetLog();
        initialized = false;
        canceled = false;
        fail = false;
        progress = 0;

        repository = dao.findByID(repositoryId, EntityRepository.class);

        out.printLog("Geração da rede iniciada!");
        out.printLog("");
        out.printLog("Params: " + params);
        out.printLog("Class Service: " + serviceClass);
        out.printLog("Repository: " + repository);
        out.printLog("");

        if (repository == null || serviceClass == null) {
            message = "Erro: Escolha o repositorio e o service desejado.";
            out.printLog(message);
            progress = 0;
            initialized = false;
            fail = true;
        } else {
            initialized = true;
            progress = 10;

            final List<EntityMatrix> matricesToSave = new ArrayList<>();

            final AbstractMatrixServices netServices = createMatrixServiceInstance(matricesToSave);

            process = new Thread(netServices) {

                @Override
                public void run() {
                    Date started = new Date();
                    try {
                        if (!canceled) {
                            out.setCurrentProcess("Iniciando coleta dos dados para geração da matriz.");

                            super.run();
                        }
                        progress = 50;
                        out.printLog("");
                        if (!canceled) {
                            out.setCurrentProcess("Iniciando salvamento dos dados gerados.");
                            dao.clearCache(false);
                            for (EntityMatrix entityMatrix : matricesToSave) {
                                out.printLog("Salvando matriz com " + entityMatrix.getNodes().size() + " registros. Parametros: " + entityMatrix.getParams());
                                entityMatrix.setStarted(started);
                                entityMatrix.getParams().putAll(params);
                                entityMatrix.setRepository(repository + "");
                                entityMatrix.setClassServicesName(serviceClass.getName());
                                entityMatrix.setLog(out.getLog().toString());
                                for (EntityMatrixNode node : entityMatrix.getNodes()) {
                                    node.setMatrix(entityMatrix);
                                }
                                entityMatrix.setStoped(new Date());
                                entityMatrix.setComplete(true);
                                dao.insert(entityMatrix);
                                out.printLog("");
                                dao.clearCache(true);
                                System.gc();
                            }
                            out.printLog("Salvamento dos dados concluído!");
                        }
                        message = "Geração da matriz finalizada.";
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        message = "Geração da rede abortada, erro: " + ex.toString();
                        fail = true;
                    } finally {
                        out.printLog("");
                        if (canceled) {
                            out.setCurrentProcess("Geração da matriz abortada pelo usuário.");
                        } else {
                            out.setCurrentProcess(message);
                        }
                        progress = 100;
                        initialized = false;
                        params.clear();
                        System.gc();
                    }
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
                process.checkAccess();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            try {
                process.interrupt();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    public void onComplete() {
        out.printLog("onComplete" + '\n');
        initialized = false;
        progress = 0;
        if (fail) {
            JsfUtil.addErrorMessage(message);
        } else {
            JsfUtil.addSuccessMessage(message);
        }
    }

    public List<Class<?>> getServicesClasses() {
        List<Class<?>> cls = null;
        try {
            cls = JsfUtil.getClasses(AbstractMatrixServices.class.getPackage().getName(), Arrays.asList(AbstractMatrixServices.class.getSimpleName()));
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return cls;
    }

    private AbstractMatrixServices createMatrixServiceInstance(List<EntityMatrix> matricesToSave) {
        try {
            return (AbstractMatrixServices) serviceClass.getConstructor(GenericDao.class, EntityRepository.class, List.class, Map.class, OutLog.class).newInstance(dao, repository, matricesToSave, params, out);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    public ClassConverter getConverterClass() {
        return new ClassConverter();
    }
}
