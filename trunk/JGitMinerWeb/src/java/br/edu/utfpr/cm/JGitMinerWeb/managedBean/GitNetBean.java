/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package br.edu.utfpr.cm.JGitMinerWeb.managedBean;

import br.edu.utfpr.cm.JGitMinerWeb.pojo.EntityRepository;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;

/**
 *
 * @author douglas
 */
@ManagedBean(name = "gitNetBean")
@SessionScoped
public class GitNetBean {

    private boolean initialized;
    private boolean fail;
    private boolean canceled;
    private Integer progress;
    private EntityRepository repository;

    /**
     * Creates a new instance of GitNet
     */
    public GitNetBean() {
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

    public boolean isInitialized() {
        return initialized;
    }

    public void setInitialized(boolean initialized) {
        this.initialized = initialized;
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
    }

    public void cancel() {
    }

    public void onComplete() {
    }
}
