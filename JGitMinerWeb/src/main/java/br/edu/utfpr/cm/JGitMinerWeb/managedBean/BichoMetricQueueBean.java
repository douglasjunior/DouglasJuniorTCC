package br.edu.utfpr.cm.JGitMinerWeb.managedBean;

import br.edu.utfpr.cm.JGitMinerWeb.converter.ClassConverter;
import br.edu.utfpr.cm.JGitMinerWeb.dao.GenericBichoDAO;
import br.edu.utfpr.cm.JGitMinerWeb.dao.GenericDao;
import br.edu.utfpr.cm.JGitMinerWeb.model.matrix.EntityMatrix;
import br.edu.utfpr.cm.JGitMinerWeb.model.metric.EntityMetric;
import br.edu.utfpr.cm.JGitMinerWeb.model.metric.EntityMetricNode;
import br.edu.utfpr.cm.JGitMinerWeb.util.JsfUtil;
import br.edu.utfpr.cm.JGitMinerWeb.util.OutLog;
import br.edu.utfpr.cm.minerador.services.metric.AbstractBichoMetricServices;
import com.google.common.util.concurrent.AtomicDouble;
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
public class BichoMetricQueueBean implements Serializable {

    private final OutLog out;
    private final List<Map<Object, Object>> paramsQueue;
    private final ExecutorService threadPool;

    @EJB
    private GenericDao dao;
    @EJB
    private GenericBichoDAO bichoDao;
    private EntityMatrix matrix;
    private String matrixId;
    private Class<?> serviceClass;
    private String message;
    private AtomicDouble progress;
    private boolean initialized;
    private boolean fail;
    private boolean canceled;
    private Map<Object, Object> params;

    /**
     * Creates a new instance of GitNet
     */
    public BichoMetricQueueBean() {
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

    public boolean isInitialized() {
        return initialized;
    }

    public String getLog() {
        return out.getSingleLog();
    }

    public Integer getProgress() {
        if (fail) {
            progress = new AtomicDouble(100d);
        } else if (progress == null) {
            progress = new AtomicDouble(0d);
        } else if (progress.intValue() > 100) {
            progress.set(100d);
        }

        return progress.intValue();
    }

    public void queue() {
        if (matrix == null || matrix.getId() == null) {
            out.printLog("Matrix is not selected.");
            return;
        }
        if (params.isEmpty()) {
            out.printLog("Params is empty.");
            return;
        }
        params.put("matrix", matrix);
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
        progress = new AtomicDouble(0);

        out.printLog("Geração da rede iniciada!");
        out.printLog("");
        out.printLog("Params queue: " + paramsQueue);
        out.printLog("Class Service: " + serviceClass);
        out.printLog("");

        if (matrix == null || serviceClass == null) {
            message = "Erro: Escolha a Matriz e o Service desejado.";
            out.printLog(message);
            progress = new AtomicDouble(0);
            initialized = false;
            fail = true;
        } else {
            initialized = true;
            progress = new AtomicDouble(1);
            out.printLog("Queue size: " + paramsQueue.size());
            final double fraction = 99.0d / paramsQueue.size();
            for (final Map<Object, Object> params : paramsQueue) {
                final EntityMatrix matrix = (EntityMatrix) params.get("matrix");
                params.putAll(matrix.getParams());

                out.resetLog();
                final List<EntityMetric> metricsToSave = new ArrayList<>();

                out.printLog("");
                out.printLog("Params: " + params);
                out.printLog("");

                final AbstractBichoMetricServices services = createMetricServiceInstance(metricsToSave, matrix, params);

                Thread process = new Thread(services) {

                    @Override
                    public void run() {
                        Date started = new Date();
                        try {
                            out.setCurrentProcess("Iniciando coleta dos dados para cálculo das métricas.");

                            super.run();

                            out.printLog("");

                            out.setCurrentProcess("Iniciando salvamento dos dados gerados.");
                            for (EntityMetric entityMetric : metricsToSave) {
                                out.printLog("Salvando métricas com " + entityMetric.getNodes().size() + " registros. Parametros: " + entityMetric.getParams());
                                entityMetric.setStarted(started);
                                entityMetric.getParams().putAll(params);
                                entityMetric.setMatrix(matrix.toString());
                                entityMetric.setClassServicesName(serviceClass.getName());
                                entityMetric.setLog(out.getLog().toString());
                                for (EntityMetricNode node : entityMetric.getNodes()) {
                                    node.setMetric(entityMetric);
                                }
                                entityMetric.setStoped(new Date());
                                entityMetric.setComplete(true);
                                // saving in jgitminer database
                                dao.insert(entityMetric);
                                out.printLog("");
                                dao.clearCache(true);
                            }
                            out.printLog("Salvamento dos dados concluído!");

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
                            out.setCurrentProcess(message);
                            progress.addAndGet(fraction);
                            if (progress.intValue() >= 100) {
                                initialized = false;
                            }
                        }
                    }
                };

                threadPool.submit(process);
            }
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
        progress = new AtomicDouble(0);
        if (fail) {
            JsfUtil.addErrorMessage(message);
        } else {
            JsfUtil.addSuccessMessage(message);
        }
    }

    public String getMatrixParamsToString() {
        matrix = getMatrixSelected();
        if (matrix != null) {
            params.put("beginDate", matrix.getParams().get("beginDate"));
            params.put("endDate", matrix.getParams().get("endDate"));
            params.put("futureBeginDate", matrix.getParams().get("beginDate"));
            params.put("futureEndDate", matrix.getParams().get("endDate"));
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
                metricsServices = JsfUtil.getClasses(AbstractBichoMetricServices.class.getPackage().getName(), Arrays.asList(AbstractBichoMetricServices.class.getSimpleName()));
                // faz uma iteração percorrendo cada classe
                for (Iterator<Class<?>> itMetricService = metricsServices.iterator(); itMetricService.hasNext();) {
                    Class<?> metricService = itMetricService.next();
                    // pegas as matrizes disponíveis para esta metrica
                    List<String> avaliableMatricesServices = ((AbstractBichoMetricServices) metricService.getConstructor().newInstance()).getAvailableMatricesPermitted();
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

    private AbstractBichoMetricServices createMetricServiceInstance(List<EntityMetric> metricsToSave, EntityMatrix matrix, Map<Object, Object> params) {
        try {
            return (AbstractBichoMetricServices) serviceClass
                    .getConstructor(GenericBichoDAO.class, EntityMatrix.class, Map.class, OutLog.class, List.class)
                    .newInstance(bichoDao, matrix, params, out, metricsToSave);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    public ClassConverter getConverterClass() {
        return new ClassConverter();
    }

    private EntityMatrix getMatrixSelected() {
        return dao.findByID(matrixId, EntityMatrix.class);
    }
}
