/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package br.edu.utfpr.cm.JGitMinerWeb.services.matriz;

import br.edu.utfpr.cm.JGitMinerWeb.dao.GenericDao;
import br.edu.utfpr.cm.JGitMinerWeb.model.matriz.EntityMatrizNode;
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
public abstract class AbstractMatrizServices extends AbstractServices {

    private final EntityRepository repository;

    public AbstractMatrizServices(GenericDao dao, OutLog out) {
        super(dao, out);
        this.repository = null;
    }

    public AbstractMatrizServices(GenericDao dao, EntityRepository repository, Map params, OutLog out) {
        super(dao, params, out);
        this.repository = repository;
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

    protected void addToEntityMatrizNodeList(List list) {
        if (nodes == null) {
            nodes = new ArrayList<>();
        }
        for (Object obj : list) {
            nodes.add(new EntityMatrizNode(obj));
        }
    }

    public List<EntityMatrizNode> getMatrizNodes() {
        return (List) super.getNodes();
    }
}
