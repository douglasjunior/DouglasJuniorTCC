package br.edu.utfpr.cm.minerador.services.metric;

import br.edu.utfpr.cm.JGitMinerWeb.dao.GenericBichoDAO;
import br.edu.utfpr.cm.JGitMinerWeb.model.matrix.EntityMatrix;
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

    private final EntityMatrix matrix;

    public AbstractBichoMetricServices(GenericBichoDAO dao, OutLog out) {
        super(dao, out);
        this.matrix = null;
    }

    public AbstractBichoMetricServices(GenericBichoDAO dao, EntityMatrix matrix, Map params, OutLog out) {
        super(dao, params, out);
        this.matrix = matrix;
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

    protected void addToEntityMetricNodeList(Collection list) {
        if (nodes == null) {
            nodes = new ArrayList<>();
        }
        for (Object obj : list) {
            nodes.add(new EntityMetricNode(obj));
        }
    }

    public List<EntityMetricNode> getMetricNodes() {
        return (List) super.getNodes();
    }

    /**
     * @return List the names of "classes services" available matrices allowed for this metric.
     */
    public abstract List<String> getAvailableMatricesPermitted();
}
