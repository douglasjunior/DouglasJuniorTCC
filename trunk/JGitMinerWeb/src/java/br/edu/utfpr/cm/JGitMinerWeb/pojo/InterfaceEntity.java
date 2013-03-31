/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package br.edu.utfpr.cm.JGitMinerWeb.pojo;

import java.io.Serializable;

/**
 *
 * @author douglas
 */
public interface InterfaceEntity extends Serializable {

    public Long getId();

    public void setId(Long id);

    @Override
    public String toString();

    @Override
    public boolean equals(Object obj);
}
