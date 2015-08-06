package br.edu.utfpr.cm.minerador.services.metric;

import br.edu.utfpr.cm.JGitMinerWeb.dao.GenericBichoDAO;
import br.edu.utfpr.cm.JGitMinerWeb.dao.GenericDao;
import br.edu.utfpr.cm.JGitMinerWeb.model.matrix.EntityMatrix;
import br.edu.utfpr.cm.JGitMinerWeb.model.matrix.EntityMatrixNode;
import br.edu.utfpr.cm.JGitMinerWeb.model.metric.EntityMetric;
import br.edu.utfpr.cm.JGitMinerWeb.model.metric.EntityMetricNode;
import br.edu.utfpr.cm.JGitMinerWeb.util.OutLog;
import br.edu.utfpr.cm.minerador.services.AbstractBichoServices;
import br.edu.utfpr.cm.minerador.services.metric.model.FilePair;
import br.edu.utfpr.cm.minerador.services.util.MatrixUtils;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author douglas
 */
public abstract class AbstractBichoMetricServices extends AbstractBichoServices {

    protected final EntityMatrix matrix;

    public AbstractBichoMetricServices() {
        matrix = null;
    }

    public AbstractBichoMetricServices(GenericBichoDAO dao, GenericDao genericDao, EntityMatrix matrix, Map<Object, Object> params, OutLog out) {
        super(dao, genericDao, params, out);
        this.matrix = matrix;
    }

    public EntityMatrix getMatrix() {
        return matrix;
    }

    protected void saveMetrics(EntityMetric entityMetric, Class<?> serviceClass) {
        out.printLog("\nSalvando métricas com " + entityMetric.getNodes().size() + " registros. Parametros: " + entityMetric.getParams());

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
        genericDao.insert(entityMetric);

        out.printLog("\nSalvamento dos dados concluído!");
    }

    @Override
    public abstract void run();

    protected static List<EntityMetricNode> objectsToNodes(Collection<?> list, String header) {
        List<EntityMetricNode> nodes = new ArrayList<>();
        nodes.add(new EntityMetricNode(header));
        for (Object value : list) {
            nodes.add(new EntityMetricNode(value.toString()));
        }
        return nodes;
    }

    protected static List<EntityMetricNode> objectsToNodes(Object value, String header) {
        List<EntityMetricNode> nodes = new ArrayList<>();
        nodes.add(new EntityMetricNode(header));
        nodes.add(new EntityMetricNode(value.toString()));
        return nodes;
    }

    /**
     * @return List the names of "classes services" available matrices allowed for this metric.
     */
    public abstract List<String> getAvailableMatricesPermitted();

    protected Set<FilePair> getFilePairsFromMatrix(EntityMatrixNode get, List<EntityMatrixNode> matrixNodes, Map<String, Integer> headerIndexesMap) {
        // 25 arquivos distintos com maior confiança entre o par (coluna da esquerda)
        final int file1Index = headerIndexesMap.get("file1");
        final int file2Index = headerIndexesMap.get("file2");

        final Set<FilePair> distinctFileOfFilePairWithHigherConfidence = new LinkedHashSet<>();
        final List<EntityMatrixNode> nodes = new ArrayList<>();
        for (EntityMatrixNode node : matrixNodes) {
            final String[] lineValues = MatrixUtils.separateValues(node);

            FilePair filePair = new FilePair(lineValues[file1Index], lineValues[file2Index]);

            distinctFileOfFilePairWithHigherConfidence.add(filePair);
            nodes.add(node);
        }

        return distinctFileOfFilePairWithHigherConfidence;
    }
}
