package br.edu.utfpr.cm.JGitMinerWeb.managedBean;

import br.edu.utfpr.cm.JGitMinerWeb.converter.ClassConverter;
import br.edu.utfpr.cm.JGitMinerWeb.dao.GenericDao;
import br.edu.utfpr.cm.JGitMinerWeb.model.matrix.EntityMatrix;
import br.edu.utfpr.cm.JGitMinerWeb.model.metric.EntityMetric;
import br.edu.utfpr.cm.JGitMinerWeb.model.metric.EntityMetricNode;
import br.edu.utfpr.cm.JGitMinerWeb.services.metric.AbstractMetricServices;
import br.edu.utfpr.cm.JGitMinerWeb.util.JsfUtil;
import br.edu.utfpr.cm.JGitMinerWeb.util.OutLog;
import java.io.PrintWriter;
import java.io.Serializable;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
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
public class GitMetricBean implements Serializable {

    @EJB
    private GenericDao dao;
    private OutLog out;
    private EntityMatrix matrix;
    private String matrixId;
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
    public GitMetricBean() {
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

    public String getMatrixId() {
        return matrixId;
    }

    public void setMatrixId(String matrixId) {
        this.matrixId = matrixId;
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

        matrix = getMatrixSelected();
        params.putAll(matrix.getParams());

        out.printLog("Geração da rede iniciada!");
        out.printLog("");
        out.printLog("Params: " + params);
        out.printLog("Class Service: " + serviceClass);
        out.printLog("Matriz: " + matrix);
        out.printLog("");

        if (matrix == null || serviceClass == null) {
            message = "Erro: Escolha a Matriz e o Service desejado.";
            out.printLog(message);
            progress = 0;
            initialized = false;
            fail = true;
        } else {
            final EntityMetric entityMetric = new EntityMetric();
            dao.insert(entityMetric);
            entityMetric.setParams(params);
            entityMetric.setMatrix(matrix + "");
            entityMetric.setLog(out.getLog().toString());
            entityMetric.setClassServicesName(serviceClass.getName());
            dao.edit(entityMetric);

            initialized = true;
            progress = 10;

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
                        progress = 50;
                        out.printLog("");
                        if (!canceled) {
                            out.setCurrentProcess("Iniciando salvamento dos dados gerados.");
                            entityMetric.setNodes(services.getMetricNodes());
                            for (EntityMetricNode node : services.getMetricNodes()){
                                node.setMetric(entityMetric);
                            }
                            entityMetric.setComplete(true);
                            dao.edit(entityMetric);
                            out.printLog("Salvamento dos dados concluído!");
                        }
                        message = "Geração da matriz finalizada.";
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        StringWriter errors = new StringWriter();
                        ex.printStackTrace(new PrintWriter(errors));
                        message = "Geração da rede abortada, erro: " + errors.toString();
                        out.printLog(errors.toString());
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
        progress = 0;
        if (fail) {
            JsfUtil.addErrorMessage(message);
        } else {
            JsfUtil.addSuccessMessage(message);
        }
    }

    public String getMatrixParamsToString() {
        matrix = getMatrixSelected();
        if (matrix != null) {
            return matrix.getParams() + "";
        }
        return "";
    }

    public List<Class<?>> getServicesClasses() {
        List<Class<?>> metricsServices = null;
        try {
            /*
             * Filtra as matrizes que podem ser utilizadas nesta metrica
             */
            // pega a matriz selecionada
            matrix = getMatrixSelected();
            if (matrix != null) {
                // pegas as classes do pacote de metricas
                metricsServices = JsfUtil.getClasses(AbstractMetricServices.class.getPackage().getName(), Arrays.asList(AbstractMetricServices.class.getSimpleName()));
                // faz uma iteração percorrendo cada classe
                for (Iterator<Class<?>> itMetricService = metricsServices.iterator(); itMetricService.hasNext();) {
                    Class<?> metricService = itMetricService.next();
                    // pegas as matrizes disponíveis para esta metrica
                    List<String> avaliableMatricesServices = ((AbstractMetricServices) metricService.getConstructor(GenericDao.class, OutLog.class).newInstance(dao, out)).getAvailableMatricesPermitted();
                    // verifica se a matriz selecionada está entre as disponíveis
                    if (!avaliableMatricesServices.contains(matrix.getClassServicesName())) {
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
            return (AbstractMetricServices) serviceClass.getConstructor(GenericDao.class, EntityMatrix.class, Map.class, OutLog.class).newInstance(dao, matrix, params, out);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    public ClassConverter getConverterClass() {
        return new ClassConverter();
    }

    private EntityMatrix getMatrixSelected() {
        return dao.findByID(matrixId, EntityMatrix.class);
    }
}
