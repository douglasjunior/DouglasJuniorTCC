package br.edu.utfpr.cm.JGitMinerWeb.managedBean;

import br.edu.utfpr.cm.JGitMinerWeb.converter.ClassConverter;
import br.edu.utfpr.cm.JGitMinerWeb.dao.GenericBichoDAO;
import br.edu.utfpr.cm.JGitMinerWeb.dao.GenericDao;
import br.edu.utfpr.cm.JGitMinerWeb.model.matrix.EntityMatrix;
import br.edu.utfpr.cm.JGitMinerWeb.model.matrix.EntityMatrixNode;
import br.edu.utfpr.cm.JGitMinerWeb.util.JsfUtil;
import br.edu.utfpr.cm.JGitMinerWeb.util.OutLog;
import br.edu.utfpr.cm.minerador.services.matrix.AbstractBichoMatrixServices;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.ejb.EJB;
import javax.enterprise.context.SessionScoped;
import javax.inject.Named;

/**
 *
 * @author Rodrigo T. Kuroda
 */
@Named
@SessionScoped
public class BichoMatrixQueueBean implements Serializable {

    private final OutLog out;
    private final List<Map<Object, Object>> paramsQueue;
    private final ExecutorService threadPool;

    @EJB
    private GenericBichoDAO dao;
    @EJB
    private GenericDao genericDao;

    private String repositoryId;
    private Class<?> serviceClass;
    private String message;
    private Integer progress;
    private boolean initialized;
    private boolean fail;
    private boolean canceled;
    private Map<Object, Object> params;

    /**
     * Creates a new instance of GitNet
     */
    public BichoMatrixQueueBean() {
        out = new OutLog();
        params = new HashMap<>();
        paramsQueue = new ArrayList<>();
        threadPool = Executors.newSingleThreadExecutor();
    }

    public boolean isFail() {
        return fail;
    }

    public boolean isCanceled() {
        return canceled;
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

    public boolean isInitialized() {
        return initialized;
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

    public void queue() {
        if (params.isEmpty()) {
            out.printLog("Params is empty.");
            return;
        }
        out.printLog("Queued params: " + params);
        paramsQueue.add(params);
        params = new HashMap<>();
    }

    public void showQueue() {
        out.printLog("Queued params: ");
        for (Map<Object, Object> queuedParams : paramsQueue) {
            out.printLog(queuedParams.toString());
        }
    }

    public void clearQueue() {
        paramsQueue.clear();
        out.printLog("Params queue cleared!");
    }

    public void startQueue() {
        out.resetLog();
        initialized = false;
        canceled = false;
        fail = false;
        progress = 0;

        out.printLog("Geração da rede iniciada!");
        out.printLog("");
        out.printLog("Params queue: " + paramsQueue);
        out.printLog("Class Service: " + serviceClass);
        out.printLog("Project: " + repositoryId);
        out.printLog("");

        if (repositoryId == null || serviceClass == null) {
            message = "Erro: Escolha o repositorio e o service desejado.";
            out.printLog(message);
            progress = 0;
            initialized = false;
            fail = true;
        } else {
            initialized = true;
            progress = 1;
            final int fraction = 100 / paramsQueue.size();
            for (final Map<Object, Object> params : paramsQueue) {
                
                out.resetLog();
                final List<EntityMatrix> matricesToSave = new ArrayList<>();

                final AbstractBichoMatrixServices netServices = createMatrixServiceInstance(matricesToSave, params);

                Thread process = new Thread(netServices) {

                    @Override
                    public void run() {
                        Date started = new Date();
                        try {
                            if (!canceled) {
                                out.setCurrentProcess("Iniciando coleta dos dados para geração da matriz.");

                                super.run();
                            }
                            progress += fraction / 2;
                            out.printLog("");
                            if (!canceled) {
                                out.setCurrentProcess("Iniciando salvamento dos dados gerados.");
                                genericDao.clearCache(false);
                                for (EntityMatrix entityMatrix : matricesToSave) {
                                    out.printLog("Salvando matriz com " + entityMatrix.getNodes().size() + " registros. Parametros: " + entityMatrix.getParams());
                                    entityMatrix.setStarted(started);
                                    entityMatrix.getParams().putAll(params);
                                    entityMatrix.setRepository(repositoryId);
                                    entityMatrix.setClassServicesName(serviceClass.getName());
                                    entityMatrix.setLog(out.getLog().toString());
                                    for (EntityMatrixNode node : entityMatrix.getNodes()) {
                                        node.setMatrix(entityMatrix);
                                    }
                                    entityMatrix.setStoped(new Date());
                                    entityMatrix.setComplete(true);
                                    // saving in jgitminer database
                                    genericDao.insert(entityMatrix);
                                    out.printLog("");
                                    genericDao.clearCache(true);
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
                            progress += fraction / 2;
                            initialized = false;
                            System.gc();
                        }
                    }
                };

                threadPool.submit(process);
            }
            progress = 100;
        }
    }

    public void cancel() {
        if (initialized) {
            out.printLog("Pedido de cancelamento enviado.\n");
            canceled = true;
            try {
                threadPool.shutdown();
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
            cls = JsfUtil.getClasses(AbstractBichoMatrixServices.class.getPackage().getName(), Arrays.asList(AbstractBichoMatrixServices.class.getSimpleName()));
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return cls;
    }

    private AbstractBichoMatrixServices createMatrixServiceInstance(List<EntityMatrix> matricesToSave, Map<Object, Object> params) {
        try {
            return (AbstractBichoMatrixServices) serviceClass.getConstructor(GenericBichoDAO.class, String.class, List.class, Map.class, OutLog.class).newInstance(dao, repositoryId, matricesToSave, params, out);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    public ClassConverter getConverterClass() {
        return new ClassConverter();
    }
}
