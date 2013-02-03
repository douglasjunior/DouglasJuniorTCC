/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package br.edu.utfpr.cm.JGitMinerWeb.services.matriz;

import br.edu.utfpr.cm.JGitMinerWeb.dao.GenericDao;
import br.edu.utfpr.cm.JGitMinerWeb.pojo.matriz.EntityMatrizRecord;
import br.edu.utfpr.cm.JGitMinerWeb.pojo.miner.EntityRepository;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author douglas
 */
public abstract class MatrizServices implements Runnable {

    protected GenericDao dao;
    protected EntityRepository repository;
    protected List<EntityMatrizRecord> records;

    public MatrizServices(GenericDao dao) {
        this.dao = dao;
    }

    public MatrizServices(GenericDao dao, EntityRepository repository) {
        this.dao = dao;
        this.repository = repository;
    }

    public EntityRepository getRepository() {
        return repository;
    }

    public List<EntityMatrizRecord> getRecords() {
        return records;
    }

    @Override
    public abstract void run();

    public abstract String convertToCSV();
    
    public static MatrizServices createInstance(GenericDao dao, String className){
        try {
            return (MatrizServices) Class.forName(className).getConstructor(dao.getClass()).newInstance(dao);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }
}
