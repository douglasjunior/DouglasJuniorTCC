/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package br.edu.utfpr.cm.JGitMinerWeb.pojo.matriz;

import br.edu.utfpr.cm.JGitMinerWeb.pojo.miner.InterfaceEntity;
import br.edu.utfpr.cm.JGitMinerWeb.util.Util;
import br.edu.utfpr.cm.JGitMinerWeb.util.matriz.NodeConnection;
import java.io.Serializable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

/**
 *
 * @author douglas
 */
@Entity
@Table(name = "matrizNode")
public class EntityMatrizNode implements InterfaceEntity, Serializable, NodeConnection<String, String> {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "mFrom")
    private String from;
    @Column(name = "mTo")
    private String to;
    @Column(name = "mWeight")
    private double weight;
    @ManyToOne
    private EntityMatriz matriz;

    public EntityMatrizNode() {
    }

    public EntityMatrizNode(Object from, Object to, Object weight) {
        this.from = from + "";
        this.to = to + "";
        this.weight = Util.tratarStringParaDouble(weight + "");
    }

    public EntityMatrizNode(Object from, Object to) {
        this.from = from + "";
        this.to = to + "";
    }

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public void setId(Long id) {
        this.id = id;
    }

    @Override
    public double getWeight() {
        return weight;
    }

    @Override
    public void setWeight(double weight) {
        this.weight = weight;
    }

    public EntityMatriz getMatriz() {
        return matriz;
    }

    public void setMatriz(EntityMatriz matriz) {
        this.matriz = matriz;
    }

    @Override
    public String getFrom() {
        return from;
    }

    @Override
    public String getTo() {
        return to;
    }

    public void incWeight() {
        weight++;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (id != null ? id.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof EntityMatrizNode)) {
            return false;
        }
        EntityMatrizNode other = (EntityMatrizNode) object;
        if (this.id != null && other.id != null // verifica se nao é nulo
                && !this.id.equals(new Long(0)) // verifica se tem id
                && this.id.equals(other.id)) { // verifica se id são iguais
            return true;
        }
        if (this.from.equals(other.from) && this.to.equals(other.to)) {
            return true;
        }
        if (this.from.equals(other.to) && this.to.equals(other.from)) {
            return true;
        }
        return false;
    }

    @Override
    public String toString() {
        return from + "\t\t" + to + "\t\t" + weight;
    }
}
