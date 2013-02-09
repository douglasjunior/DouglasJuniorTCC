/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package br.edu.utfpr.cm.JGitMinerWeb.managedBean;

import br.edu.utfpr.cm.JGitMinerWeb.dao.GenericDao;
import br.edu.utfpr.cm.JGitMinerWeb.pojo.matriz.EntityMatriz;
import br.edu.utfpr.cm.JGitMinerWeb.pojo.miner.EntityRepository;
import br.edu.utfpr.cm.JGitMinerWeb.services.matriz.MatrizServices;
import br.edu.utfpr.cm.JGitMinerWeb.util.JsfUtil;
import br.edu.utfpr.cm.JGitMinerWeb.util.OutLog;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;

/**
 *
 * @author douglas
 */
@ManagedBean(name = "gitMatrizBean")
@SessionScoped
public class GitMatrizBean implements Serializable {


    /*
     * 
     */
    @EJB
    private GenericDao dao;
    private OutLog out;
    private EntityRepository repository;
    private Class serviceClass;
    private Map params;
    private String message;
    private Thread process;
    private Integer progress;
    private boolean initialized;
    private boolean fail;
    private boolean canceled;

    /**
     * Creates a new instance of GitNet
     */
    public GitMatrizBean() {
        out = new OutLog();
        params = new HashMap();
    }

    public boolean isFail() {
        return fail;
    }

    public void setFail(boolean fail) {
        this.fail = fail;
    }

    public boolean isCanceled() {
        return canceled;
    }

    public void setCanceled(boolean canceled) {
        this.canceled = canceled;
    }

    public EntityRepository getRepository() {
        return repository;
    }

    public void setRepository(EntityRepository repository) {
        this.repository = repository;
    }

    public Class getServiceClass() {
        return serviceClass;
    }

    public void setServiceClass(Class serviceClass) {
        this.serviceClass = serviceClass;
    }

    public void addParamValue(Object value, String key) {
        System.out.println("value:" + value + " key:" + key);
        params.put(value, key);
    }

    public GenericDao getDao() {
        return dao;
    }

    public void setDao(GenericDao dao) {
        this.dao = dao;
    }

    public boolean isInitialized() {
        return initialized;
    }

    public void setInitialized(boolean initialized) {
        this.initialized = initialized;
    }

    public String getLog() {
        return out.getSingleLog();
    }

    public Integer getProgress() {
        if (fail) {
            progress = new Integer(100);
        } else if (progress == null) {
            progress = new Integer(0);
        } else if (progress > 100) {
            progress = new Integer(100);
        }
        System.out.println("progress: " + progress);
        return progress;
    }

    public void setProgress(Integer progress) {
        this.progress = progress;
    }

    public void start() {
        final EntityMatriz entityNet = new EntityMatriz();
        dao.insert(entityNet);
        out.resetLog();
        initialized = false;
        canceled = false;
        fail = false;
        progress = new Integer(0);

        out.printLog("Geração da rede iniciada!");
        out.printLog("");
        out.printLog("Params: " + params);
        out.printLog("Repository: " + repository);
        out.printLog("");

        entityNet.setLog(out.getLog().toString());
        dao.edit(entityNet);

        if (repository == null) {
            message = "Erro: Escolha o repositorio desejado.";
            out.printLog(message);
            progress = new Integer(0);
            initialized = false;
            fail = true;
            entityNet.setLog(out.getLog().toString());
            dao.edit(entityNet);
        } else {
            initialized = true;
            progress = new Integer(10);
            entityNet.setRepository(repository);

            final MatrizServices netServices = createMatrizServiceInstance();

            process = new Thread(netServices) {
                @Override
                public void run() {
                    try {
                        out.setCurrentProcess("Iniciando consulta ao banco de dados.");
                        super.run();
                        out.printLog("Consulta ao banco de dados concluída!");
                        progress = new Integer(50);
                        out.printLog("");
                        out.setCurrentProcess("Iniciando processamento dos dados coletados.");
                        entityNet.setRecords(netServices.getRecords());
                        out.printLog("Processamento dos dados concluído!");
                        entityNet.setComplete(true);
                        dao.edit(entityNet);
                        message = "Geração da rede concluída.";
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        message = "Geração da rede abortada: " + ex.toString();
                        fail = true;
                    }
                    System.gc();
                    out.printLog("");
                    out.setCurrentProcess(message);
                    progress = new Integer(100);
                    initialized = false;
                    entityNet.setLog(out.getLog().toString());
                    entityNet.setStoped(new Date());
                    dao.edit(entityNet);
                }
            };
            process.start();
        }
    }

    public void cancel() {
        if (initialized) {
            out.printLog("Pedido de cancelamento enviado.\n");
            canceled = true;
            try {
                process.interrupt();
            } catch (Exception ex) {
            }
        }
    }

    public void onComplete() {
        out.printLog("onComplete" + '\n');
        initialized = false;
        progress = new Integer(0);
        if (fail) {
            JsfUtil.addErrorMessage(message);
        } else {
            JsfUtil.addSuccessMessage(message);
        }
    }

    public List<Class> getServicesClasses() {
        List<Class> cls = null;
        try {
            cls = JsfUtil.getClasses("br.edu.utfpr.cm.JGitMinerWeb.services.matriz", Arrays.asList("MatrizServices"));
            for (Class cl : cls) {
                System.out.println(cl.getName());
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return cls;
    }

    private MatrizServices createMatrizServiceInstance() {
        try {
            return (MatrizServices) serviceClass.getConstructor(GenericDao.class, EntityRepository.class, Map.class).newInstance(dao, repository, JsfUtil.getContext().getExternalContext().getRequestParameterMap());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    // ######### CONVERTER ###########
    @FacesConverter(forClass = Class.class)
    public static class ClassConverter implements Converter {

        @Override
        public Object getAsObject(FacesContext facesContext, UIComponent component, String value) {
            if (value == null || value.length() == 0 || value.equals("null")) {
                return null;
            }
            System.out.println("VALUEEE: " + value);
            Class classValue = null;
            try {
                classValue = Class.forName(value);
            } catch (ClassNotFoundException ex) {
                ex.printStackTrace();
            }
            return classValue;
        }

        @Override
        public String getAsString(FacesContext facesContext, UIComponent component, Object object) {
            if (object == null) {
                return null;
            }
            if (object instanceof Class) {
                Class o = (Class) object;
                return o.getName();
            } else {
                throw new IllegalArgumentException("object " + object + " is of type " + object.getClass().getName() + "; expected type: " + Class.class.getName());
            }
        }
    }

    public ClassConverter getConverterClass() {
        return new ClassConverter();
    }
}
