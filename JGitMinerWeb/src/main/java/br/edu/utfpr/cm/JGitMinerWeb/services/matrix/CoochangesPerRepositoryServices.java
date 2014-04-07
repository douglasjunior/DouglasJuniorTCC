/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.edu.utfpr.cm.JGitMinerWeb.services.matrix;

import br.edu.utfpr.cm.JGitMinerWeb.dao.GenericDao;
import br.edu.utfpr.cm.JGitMinerWeb.model.matrix.EntityMatrix;
import br.edu.utfpr.cm.JGitMinerWeb.model.miner.EntityRepository;
import br.edu.utfpr.cm.JGitMinerWeb.util.OutLog;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Douglas
 */
public class CoochangesPerRepositoryServices extends AbstractMatrixServices {

    public CoochangesPerRepositoryServices(GenericDao dao, OutLog out) {
        super(dao, out);
    }

    public CoochangesPerRepositoryServices(GenericDao dao, EntityRepository repository, List<EntityMatrix> matricesToSave, Map params, OutLog out) {
        super(dao, repository, matricesToSave, params, out);
    }

    @Override
    public void run() {
        
        if (getRepository() == null) {
            throw new IllegalArgumentException("Parâmetro Repository não pode ser nulo.");
        }
        
        EntityMatrix matrix = new EntityMatrix();
        matricesToSave.add(matrix);
        
        
    }

    @Override
    public String getHeadCSV() {
        return "repository;period;coochange;testCoochange";
    }

}
