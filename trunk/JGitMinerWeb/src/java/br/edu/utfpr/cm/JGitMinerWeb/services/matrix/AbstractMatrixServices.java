/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package br.edu.utfpr.cm.JGitMinerWeb.services.matrix;

import br.edu.utfpr.cm.JGitMinerWeb.dao.GenericDao;
import br.edu.utfpr.cm.JGitMinerWeb.model.matrix.EntityMatrix;
import br.edu.utfpr.cm.JGitMinerWeb.model.matrix.EntityMatrixNode;
import br.edu.utfpr.cm.JGitMinerWeb.model.miner.EntityRepository;
import br.edu.utfpr.cm.JGitMinerWeb.services.AbstractServices;
import br.edu.utfpr.cm.JGitMinerWeb.util.OutLog;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 *
 * @author douglas
 */
public abstract class AbstractMatrixServices extends AbstractServices {

    private final EntityRepository repository;
    protected final List<EntityMatrix> matricesToSave;

    public AbstractMatrixServices(GenericDao dao, OutLog out) {
        super(dao, out);
        this.repository = null;
        this.matricesToSave = null;
    }

    public AbstractMatrixServices(GenericDao dao, EntityRepository repository, List<EntityMatrix> matricesToSave, Map params, OutLog out) {
        super(dao, params, out);
        this.repository = repository;
        this.matricesToSave = matricesToSave;
    }

    public EntityRepository getRepository() {
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

    protected List<EntityMatrixNode> objectsToNodes(List list) {
        List<EntityMatrixNode> nodes = new ArrayList<>();
        for (Object obj : list) {
            nodes.add(new EntityMatrixNode(obj));
        }
        return nodes;
    }
}
