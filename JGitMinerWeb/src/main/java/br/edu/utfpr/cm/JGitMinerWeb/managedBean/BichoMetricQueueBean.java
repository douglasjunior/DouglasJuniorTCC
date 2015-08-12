package br.edu.utfpr.cm.JGitMinerWeb.managedBean;

import br.edu.utfpr.cm.JGitMinerWeb.converter.ClassConverter;
import br.edu.utfpr.cm.JGitMinerWeb.dao.BichoDAO;
import br.edu.utfpr.cm.JGitMinerWeb.dao.GenericBichoDAO;
import br.edu.utfpr.cm.JGitMinerWeb.dao.GenericDao;
import br.edu.utfpr.cm.JGitMinerWeb.model.matrix.EntityMatrix;
import br.edu.utfpr.cm.JGitMinerWeb.util.JsfUtil;
import br.edu.utfpr.cm.JGitMinerWeb.util.OutLog;
import br.edu.utfpr.cm.minerador.services.metric.AbstractBichoMetricServices;
import br.edu.utfpr.minerador.preprocessor.comparator.OrderEntityMatrixByIndex;
import br.edu.utfpr.minerador.preprocessor.comparator.OrderEntityMatrixByVersion;
import com.google.common.util.concurrent.AtomicDouble;
import java.io.PrintWriter;
import java.io.Serializable;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
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

    private final String LIST = "listMatrices";

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
        params = new LinkedHashMap<>();
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
        params = new LinkedHashMap<>();
    }

    public void queueAll() {
        List<EntityMatrix> matrices = (List<EntityMatrix>) JsfUtil.getObjectFromSession(LIST);
        Collections.sort(matrices, new OrderEntityMatrixByVersion());
        for (EntityMatrix matrix : matrices) {
            Map<Object, Object> params = new LinkedHashMap<>();
            params.put("matrix", matrix);
            if (matrix.getParams().get("version") != null) {
                params.put("version", matrix.getParams().get("version"));
            }
            if (matrix.getParams().get("futureVersion") != null) {
                params.put("futureVersion", matrix.getParams().get("futureVersion"));
            }
            if (matrix.getParams().get("index") != null) {
                params.put("index", matrix.getParams().get("index"));
            }
            if (matrix.getParams().get("quantity") != null) {
                params.put("quantity", matrix.getParams().get("quantity"));
            }
            out.printLog("Queued params: " + params);
            paramsQueue.add(params);
        }
        params = new LinkedHashMap<>();
    }

    public void queueAllForCurrentAndFutureVersion() {
        final List<EntityMatrix> matrices = (List<EntityMatrix>) JsfUtil.getObjectFromSession(LIST);
        final List<EntityMatrix> filteredMatrices = matrices.stream().filter(m -> !m.toString().contains("summary")).collect(Collectors.toList());
        Collections.sort(filteredMatrices, new OrderEntityMatrixByVersion());
        final BichoDAO bichoDAO = new BichoDAO(bichoDao, matrix.getRepository(), null);
        for (EntityMatrix matrix : filteredMatrices) {
            String project = (String) matrix.getParams().get("project");

            String version = (String) matrix.getParams().get("version");
            String aprioriFilter = (String) matrix.getParams().get("aprioriFilter");
            String futureVersion = bichoDAO.selectFutureMajorVersion(version);

            Map<Object, Object> trainParams = new LinkedHashMap<>();
            trainParams.put("matrix", matrix);
            trainParams.put("versionInAnalysis", version);
            trainParams.put("version", version);
            trainParams.put("filename", project + " " + version);
            trainParams.put("futureVersion", futureVersion);
            trainParams.put("additionalFilename", "train");
            trainParams.put("project", project);
            trainParams.put("aprioriFilter", aprioriFilter);

            out.printLog("Train params: " + trainParams);
            paramsQueue.add(trainParams);

            String futureFutureVersion = bichoDAO.selectFutureMajorVersion(version);

            Map<Object, Object> testParams = new LinkedHashMap<>();
            testParams.put("matrix", matrix);
            testParams.put("versionInAnalysis", version);
            testParams.put("version", futureVersion);
            testParams.put("filename", project + " " + futureVersion);
            testParams.put("futureVersion", futureFutureVersion);
            testParams.put("additionalFilename", "test");
            testParams.put("project", project);
            testParams.put("aprioriFilter", aprioriFilter);

            out.printLog("Test params: " + testParams);
            paramsQueue.add(testParams);
        }
        params = new LinkedHashMap<>();
    }

    public void queueForCurrentAndFutureVersion() {
        final BichoDAO bichoDAO = new BichoDAO(bichoDao, matrix.getRepository(), null);

        String project = (String) matrix.getParams().get("project");

        String version = (String) matrix.getParams().get("version");
        String aprioriFilter = (String) matrix.getParams().get("aprioriFilter");
        String futureVersion = bichoDAO.selectFutureMajorVersion(version);

        // organization in zip:
        // [filters]/[project][versionInAnalysis]/[rank]/[additionalFilename: train or test].csv
        Map<Object, Object> trainParams = new LinkedHashMap<>();
        trainParams.put("matrix", matrix);
        trainParams.put("versionInAnalysis", version);
        trainParams.put("version", version);
        trainParams.put("filename", project + " " + version);
        trainParams.put("futureVersion", futureVersion);
        trainParams.put("additionalFilename", "train");
        trainParams.put("project", project);
        trainParams.put("aprioriFilter", aprioriFilter);

        out.printLog("Train params: " + trainParams);
        paramsQueue.add(trainParams);

        String futureFutureVersion = bichoDAO.selectFutureMajorVersion(version);

        Map<Object, Object> testParams = new LinkedHashMap<>();
        testParams.put("matrix", matrix);
        // o arquivo de teste vai ficar junto com o arquivo de treino
        testParams.put("versionInAnalysis", version);
        testParams.put("version", futureVersion);
        testParams.put("filename", project + " " + futureVersion);
        testParams.put("futureVersion", futureFutureVersion);
        testParams.put("additionalFilename", "test");
        trainParams.put("project", project);
        trainParams.put("aprioriFilter", aprioriFilter);

        out.printLog("Test params: " + testParams);
        paramsQueue.add(testParams);
    }

    public void queueAllForAllVersion() {
        List<EntityMatrix> matrices = (List<EntityMatrix>) JsfUtil.getObjectFromSession(LIST);
        Collections.sort(matrices, new OrderEntityMatrixByVersion());
        List<String> versions = new BichoDAO(bichoDao, matrix.getRepository(), null).selectFixVersionOrdered();
        List<String> toRemove = new ArrayList<>();
        for (String version : versions) {
            if (version.endsWith("top 25")) {
                toRemove.add(version);
            }
        }
        versions.removeAll(toRemove);
        for (EntityMatrix matrix : matrices) {
            if (matrix.toString().endsWith("top 25")) {
                continue;
            }
            for (int i = 0; i < versions.size() - 1; i++) {
                Map<Object, Object> params = new LinkedHashMap<>();
                params.put("matrix", matrix);
                params.put("version", versions.get(i));//matrix.getParams().get("version"));
                params.put("filename", "v" + versions.get(i));//matrix.getParams().get("version"));
                params.put("futureVersion", versions.get(i + 1)); //matrix.getParams().get("futureVersion"));
                out.printLog("Queued params: " + params);
                paramsQueue.add(params);
            }
        }
        params = new LinkedHashMap<>();
        out.printLog("Queue size: " + paramsQueue.size());
    }

    public void queueForCurrentAndFutureIndex() {
        Integer index = (Integer) matrix.getParams().get("index");
        String aprioriFilter = (String) matrix.getParams().get("aprioriFilter");
        Integer futureIndex = index + 1;

        // organization in zip:
        // [filtros]/[projeto][indexInAnalysis]/[rank]/[train or test].csv
        Map<Object, Object> trainParams = createParams(matrix, index, index, futureIndex, aprioriFilter);

        out.printLog("Train params: " + trainParams);
        paramsQueue.add(trainParams);

        Integer futureFutureVersion = futureIndex + 1;

        Map<Object, Object> testParams = createParams(matrix, futureIndex, index, futureFutureVersion, aprioriFilter);

        out.printLog("Test params: " + testParams);
        paramsQueue.add(testParams);
    }

    public void queueAllForCurrentAndFutureIndex() {
        List<EntityMatrix> matrices = (List<EntityMatrix>) JsfUtil.getObjectFromSession(LIST);
        final List<EntityMatrix> filteredMatrices = matrices.stream().filter(m -> !m.toString().contains("summary")).collect(Collectors.toList());
        Collections.sort(filteredMatrices, new OrderEntityMatrixByIndex());

        for (EntityMatrix matrix : filteredMatrices) {
            Integer index = Integer.valueOf(matrix.getParams().get("index").toString());
            String aprioriFilter = (String) matrix.getParams().get("aprioriFilter");
            Integer futureIndex = index + 1;

            Map<Object, Object> trainParams = createParams(matrix, index, index, futureIndex, aprioriFilter);
            trainParams.put("additionalFilename", "train");

            out.printLog("Train params: " + trainParams);
            paramsQueue.add(trainParams);

            Integer futureFutureVersion = futureIndex + 1;

            Map<Object, Object> testParams = createParams(matrix, futureIndex, index, futureFutureVersion, aprioriFilter);
            testParams.put("additionalFilename", "test");

            out.printLog("Test params: " + testParams);
            paramsQueue.add(testParams);
        }
        params = new LinkedHashMap<>();

        out.printLog("Jobs: " + paramsQueue.size());
    }

    private Map<Object, Object> createParams(EntityMatrix matrix1, Integer index, Integer indexInAnalisys, Integer futureIndex, String aprioriFilter) {
        String project = matrix1.getParams().get("project").toString();
        Map<Object, Object> params = new LinkedHashMap<>();
        params.put("matrix", matrix1);
        params.put("indexInAnalysis", indexInAnalisys);
        params.put("index", index);
        params.put("filename", project + " " + index);
        params.put("futureIndex", futureIndex);
        params.put("project", project);
        params.put("aprioriFilter", aprioriFilter);
        params.put("groupsQuantity", matrix1.getParams().get("groupsQuantity"));
        return params;
    }

    public void queueAllForAllMatrixIndex() {
        List<EntityMatrix> matrices = (List<EntityMatrix>) JsfUtil.getObjectFromSession(LIST);
        Collections.sort(matrices, new OrderEntityMatrixByVersion());
        Set<Integer> indexes = new LinkedHashSet<>(matrices.size());
        for (EntityMatrix matrix : matrices) {
            if (matrix.toString().endsWith("top 25")) {
                continue;
            }

            indexes.add(Integer.valueOf(String.valueOf(matrix.getParams().get("index"))));
        }

        for (EntityMatrix matrix : matrices) {
            if (matrix.toString().endsWith("top 25")) {
                continue;
            }

            for (Integer index : indexes) {
                Map<Object, Object> params = new LinkedHashMap<>();
                params.put("matrix", matrix);
                params.put("index", index);//matrix.getParams().get("version"));
                params.put("filename", "v" + (index + 1));//matrix.getParams().get("version"));
                out.printLog("Queued params: " + params);
                paramsQueue.add(params);
            }
        }
        params = new LinkedHashMap<>();
    }

    public void showQueue() {
        out.printLog("Queued params: ");
        for (Map<Object, Object> queuedParams : paramsQueue) {
            out.printLog(queuedParams.toString());
        }
    }

    public void removeLastFromQueue() {
        out.printLog("Params removed: " + paramsQueue.remove(paramsQueue.size() - 1));
    }

    public void removeFirstFromQueue() {
        out.printLog("Params removed: " + paramsQueue.remove(0));
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
                final EntityMatrix matrix = (EntityMatrix) params.remove("matrix");
                for (Map.Entry<Object, Object> entrySet : matrix.getParams().entrySet()) {
                    Object key = entrySet.getKey();
                    Object value = entrySet.getValue();

                    if (!params.containsKey(key)) {
                        params.put(key, value);
                    }

                }

                out.resetLog();

                out.printLog("");
                out.printLog("Params: " + params);
                out.printLog("");

                final AbstractBichoMetricServices services = createMetricServiceInstance(matrix, params);

                Thread process = new Thread(services) {

                    @Override
                    public void run() {
                        try {
                            out.setCurrentProcess("Iniciando coleta dos dados para cálculo das métricas.");

                            super.run();

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
            params.put("version", matrix.getParams().get("version"));
            params.put("futureVersion", matrix.getParams().get("futureVersion"));
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

    private AbstractBichoMetricServices createMetricServiceInstance(EntityMatrix matrix, Map<Object, Object> params) {
        try {
            return (AbstractBichoMetricServices) serviceClass
                    .getConstructor(GenericBichoDAO.class, GenericDao.class, EntityMatrix.class, Map.class, OutLog.class)
                    .newInstance(bichoDao, dao, matrix, params, out);
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
