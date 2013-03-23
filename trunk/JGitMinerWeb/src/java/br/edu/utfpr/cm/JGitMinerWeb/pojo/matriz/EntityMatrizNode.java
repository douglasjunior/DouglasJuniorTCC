/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package br.edu.utfpr.cm.JGitMinerWeb.pojo.matriz;

import br.edu.utfpr.cm.JGitMinerWeb.pojo.miner.InterfaceEntity;
import java.io.Serializable;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

/**
 *
 * @author douglas
 */
@Entity
@Table(name = "matrizNode")
public class EntityMatrizNode implements InterfaceEntity, Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Lob
    private String line;
    @ManyToOne(fetch = FetchType.LAZY)
    private EntityMatriz matriz;

    public EntityMatrizNode() {
    }

    public EntityMatrizNode(Object line) {
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

    public EntityMatriz getMatriz() {
        return matriz;
    }

    public void setMatriz(EntityMatriz matriz) {
        this.matriz = matriz;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (id != null ? id.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        if (object instanceof EntityMatrizNode) {

            EntityMatrizNode other = (EntityMatrizNode) object;
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
