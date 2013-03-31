/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package br.edu.utfpr.cm.JGitMinerWeb.services.matriz;

import br.edu.utfpr.cm.JGitMinerWeb.dao.GenericDao;
import br.edu.utfpr.cm.JGitMinerWeb.pojo.matriz.EntityMatrizNode;
import br.edu.utfpr.cm.JGitMinerWeb.pojo.miner.EntityRepository;
import br.edu.utfpr.cm.JGitMinerWeb.services.AbstractServices;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 *
 * @author douglas
 */
public abstract class AbstractMatrizServices extends AbstractServices {

    private final EntityRepository repository;

    public AbstractMatrizServices(GenericDao dao) {
        super(dao);
        this.repository = null;
    }

    public AbstractMatrizServices(GenericDao dao, EntityRepository repository, Map params) {
        super(dao, params);
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
