package br.edu.utfpr.cm.minerador.services.metric;

import br.edu.utfpr.cm.JGitMinerWeb.dao.GenericBichoDAO;
import br.edu.utfpr.cm.JGitMinerWeb.model.matrix.EntityMatrix;
import br.edu.utfpr.cm.JGitMinerWeb.model.metric.EntityMetric;
import br.edu.utfpr.cm.JGitMinerWeb.model.metric.EntityMetricNode;
import br.edu.utfpr.cm.JGitMinerWeb.util.OutLog;
import br.edu.utfpr.cm.minerador.services.AbstractBichoServices;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 *
 * @author douglas
 */
public abstract class AbstractBichoMetricServices extends AbstractBichoServices {

    protected final EntityMatrix matrix;
    protected final List<EntityMetric> metricsToSave;

    public AbstractBichoMetricServices() {
        matrix = null;
        metricsToSave = null;
    }

    public AbstractBichoMetricServices(GenericBichoDAO dao, OutLog out) {
        super(dao, out);
        this.matrix = null;
        metricsToSave = null;
    }

    public AbstractBichoMetricServices(GenericBichoDAO dao, EntityMatrix matrix, Map<?, ?> params, OutLog out, List<EntityMetric> metricsToSave) {
        super(dao, params, out);
        this.matrix = matrix;
        this.metricsToSave = metricsToSave;
    }

    public EntityMatrix getMatrix() {
        return matrix;
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
