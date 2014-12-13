package br.edu.utfpr.cm.minerador.services.matrix;

import br.edu.utfpr.cm.JGitMinerWeb.dao.GenericBichoDAO;
import br.edu.utfpr.cm.JGitMinerWeb.model.matrix.EntityMatrix;
import br.edu.utfpr.cm.JGitMinerWeb.model.matrix.EntityMatrixNode;
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
public abstract class AbstractBichoMatrixServices extends AbstractBichoServices {

    private final String repository;
    protected final List<EntityMatrix> matricesToSave;

    public AbstractBichoMatrixServices(GenericBichoDAO dao, OutLog out) {
        super(dao, out);
        this.repository = null;
        this.matricesToSave = null;
    }

    public AbstractBichoMatrixServices(GenericBichoDAO dao, String repository, List<EntityMatrix> matricesToSave, Map params, OutLog out) {
        super(dao, params, out);
        this.repository = repository;
        this.matricesToSave = matricesToSave;
    }

    public String getRepository() {
        return repository;
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

    protected static List<EntityMatrixNode> objectsToNodes(Collection<? extends Object> list) {
        List<EntityMatrixNode> nodes = new ArrayList<>();
        for (Object value : list) {
            nodes.add(new EntityMatrixNode(value.toString()));
        }
        return nodes;
    }
}
