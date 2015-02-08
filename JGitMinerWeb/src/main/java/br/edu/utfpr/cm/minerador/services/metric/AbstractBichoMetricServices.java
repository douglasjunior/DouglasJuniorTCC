package br.edu.utfpr.cm.minerador.services.metric;

import br.edu.utfpr.cm.JGitMinerWeb.dao.GenericBichoDAO;
import br.edu.utfpr.cm.JGitMinerWeb.dao.GenericDao;
import br.edu.utfpr.cm.JGitMinerWeb.model.matrix.EntityMatrix;
import br.edu.utfpr.cm.JGitMinerWeb.model.metric.EntityMetric;
import br.edu.utfpr.cm.JGitMinerWeb.model.metric.EntityMetricNode;
import br.edu.utfpr.cm.JGitMinerWeb.util.OutLog;
import br.edu.utfpr.cm.minerador.services.AbstractBichoServices;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;

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

    protected void saveMetrics(EntityMetric entityMetric) {
        out.printLog("\nSalvando métricas com " + entityMetric.getNodes().size() + " registros. Parametros: " + entityMetric.getParams());

        params.put("additionalFilename", entityMetric.getAdditionalFilename());
        entityMetric.getParams().putAll(params);
        entityMetric.setMatrix(matrix.toString());
        entityMetric.setClassServicesName(BichoPairFilePerIssueMetricsInFixVersionServices.class.getName());
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

    /**
     * Name of columns separated by ";".
     *
     * @return column1;column2;column3;...
     */
    @Override
    public abstract String getHeadCSV();

    protected static List<EntityMetricNode> objectsToNodes(Collection<?> list) {
        List<EntityMetricNode> nodes = new ArrayList<>();
        for (Object value : list) {
            nodes.add(new EntityMetricNode(value.toString()));
        }
        return nodes;
    }

    /**
     * @return List the names of "classes services" available matrices allowed for this metric.
     */
    public abstract List<String> getAvailableMatricesPermitted();
}
