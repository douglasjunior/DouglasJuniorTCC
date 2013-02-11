/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package br.edu.utfpr.cm.JGitMinerWeb.pojo.miner;

/**
 *
 * @author douglas
 */
public interface InterfaceEntity {

    public Long getId();

    public void setId(Long id);
    
    @Override
    public String toString();
    
    @Override
    public boolean equals(Object obj);
}
