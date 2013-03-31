/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package br.edu.utfpr.cm.JGitMinerWeb.pojo;

import java.io.Serializable;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.Lob;
import javax.persistence.Table;

/**
 *
 * @author douglas
 */
@Entity
@Table(name = "node")
@Inheritance(strategy = InheritanceType.JOINED)
public abstract class EntityNode implements InterfaceEntity, Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Lob
    private String line;

    public EntityNode() {
    }

    public EntityNode(Object line) {
        this.line = line + "";
    }

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public void setId(Long id) {
        this.id = id;
    }

    public String getLine() {
        return line;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (id != null ? id.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        if (object instanceof EntityNode) {
            EntityNode other = (EntityNode) object;
            if (this.id != null && other.id != null // verifica se nao é nulo
                    && this.id.equals(other.id)) { // verifica se id são iguais
                return true;
            }
        }
        return false;
    }

    @Override
    public String toString() {
        return line;
    }
}
