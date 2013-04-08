/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package br.edu.utfpr.cm.JGitMinerWeb.managedBean;

import br.edu.utfpr.cm.JGitMinerWeb.converter.ClassConverter;
import br.edu.utfpr.cm.JGitMinerWeb.dao.GenericDao;
import br.edu.utfpr.cm.JGitMinerWeb.pojo.matriz.EntityMatriz;
import br.edu.utfpr.cm.JGitMinerWeb.pojo.metric.EntityMetric;
import br.edu.utfpr.cm.JGitMinerWeb.pojo.metric.EntityMetricNode;
import br.edu.utfpr.cm.JGitMinerWeb.services.metric.AbstractMetricServices;
import br.edu.utfpr.cm.JGitMinerWeb.util.JsfUtil;
import br.edu.utfpr.cm.JGitMinerWeb.util.OutLog;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;

/**
 *
 * @author douglas
 */
@ManagedBean(name = "gitMetricBean")
@SessionScoped
public class GitMetricBean implements Serializable {


    /*
     * 
     */
    @EJB
    private GenericDao dao;
    private OutLog out;
    private EntityMatriz matriz;
    private String matrizId;
    private Class serviceClass;
    private Map params;
    private String message;
    private Thread process;
    private Integer progress;
    private boolean initialized;
    private boolean fail;
    private boolean canceled;

    /**
     * Creates a new instance of GitNet
     */
    public GitMetricBean() {
        out = new OutLog();
        params = new HashMap();
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
        return matrizId;
    }

    public void setRepositoryId(String repositoryId) {
        this.matrizId = repositoryId;
    }

    public Class getServiceClass() {
        return serviceClass;
    }

    public void setServiceClass(Class serviceClass) {
        this.serviceClass = serviceClass;
    }

    public Map getParamValue() {
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
        out.resetLog();
        initialized = false;
        canceled = false;
        fail = false;
        progress = new Integer(0);

        matriz = getMatrizSelected();
        params.putAll(matriz.getParams());

        out.printLog("Geração da rede iniciada!");
        out.printLog("");
        out.printLog("Params: " + params);
        out.printLog("Class Service: " + serviceClass);
        out.printLog("Matriz: " + matriz);
        out.printLog("");


        if (matriz == null || serviceClass == null) {
            message = "Erro: Escolha a Matriz e o Service desejado.";
            out.printLog(message);
            progress = new Integer(0);
            initialized = false;
            fail = true;
        } else {
            final EntityMetric entityMetric = new EntityMetric();
            dao.insert(entityMetric);
            entityMetric.setParams(params);
            entityMetric.setMatriz(matriz + "");
            entityMetric.setLog(out.getLog().toString());
            entityMetric.setClassServicesName(serviceClass.getName());
            dao.edit(entityMetric);

            initialized = true;
            progress = new Integer(10);

            final AbstractMetricServices services = createMetricServiceInstance();

            process = new Thread(services) {
                @Override
                public void run() {
                    try {
                        if (!canceled) {
                            out.setCurrentProcess("Iniciando coleta dos dados para geração da metrica.");
                            super.run();
                            out.printLog(services.getNodes().size() + " Registros coletados!");
                        }
                        progress = new Integer(50);
                        out.printLog("");
                        if (!canceled) {
                            out.setCurrentProcess("Iniciando salvamento dos dados gerados.");
                            saveNodes(entityMetric, services.getMetricNodes());
                            entityMetric.setComplete(true);
                            dao.edit(entityMetric);
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
                        progress = new Integer(100);
                        initialized = false;
                        entityMetric.setLog(out.getLog().toString());
                        entityMetric.setStoped(new Date());
                        dao.edit(entityMetric);
                        params.clear();
                        System.gc();
                    }
                }
            };
            process.start();
        }
    }

    private void saveNodes(EntityMetric metric, List<EntityMetricNode> nodes) {
        int j = 0;
        for (Iterator<EntityMetricNode> it = nodes.iterator(); it.hasNext();) {
            EntityMetricNode node = it.next();
            node.setMetric(metric);
            dao.insert(node);
            it.remove();
            j++;
            if (j >= 1000) {
                dao.clearCache(false);
                j = 0;
            }
        }
    }

    public void cancel() {
        if (initialized) {
            out.printLog("Pedido de cancelamento enviado.\n");
            canceled = true;
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
        progress = new Integer(0);
        if (fail) {
            JsfUtil.addErrorMessage(message);
        } else {
            JsfUtil.addSuccessMessage(message);
        }
    }

    public List<Class> getServicesClasses() {
        List<Class> metricsServices = null;
        try {
            /*
             * Filtra as matrizes que podem ser utilizadas nesta metrica
             */
            // pega a matriz selecionada
            matriz = getMatrizSelected();
            if (matriz != null) {
                // pegas as classes do pacote de metricas
                metricsServices = JsfUtil.getClasses(AbstractMetricServices.class.getPackage().getName(), Arrays.asList(AbstractMetricServices.class.getSimpleName()));
                // faz uma iteração percorrendo cada classe
                for (Iterator<Class> itMetricService = metricsServices.iterator(); itMetricService.hasNext();) {
                    Class metricService = itMetricService.next();
                    // pegas as matrizes disponíveis para esta metrica
                    List<String> avaliableMatricesServices = ((AbstractMetricServices) metricService.getConstructor(GenericDao.class).newInstance(dao)).getAvailableMatricesPermitted();
                    // verifica se a matriz selecionada está entre as disponíveis
                    if (!avaliableMatricesServices.contains(matriz.getClassServicesName())) {
                        itMetricService.remove();
                    }
                }
            } else {
                metricsServices = new ArrayList<>();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return metricsServices;
    }

    private AbstractMetricServices createMetricServiceInstance() {
        try {
            return (AbstractMetricServices) serviceClass.getConstructor(GenericDao.class, EntityMatriz.class, Map.class).newInstance(dao, matriz, params);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    public ClassConverter getConverterClass() {
        return new ClassConverter();
    }

    private EntityMatriz getMatrizSelected() {
        return dao.findByID(matrizId, EntityMatriz.class);
    }
}
