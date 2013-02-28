/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package br.edu.utfpr.cm.JGitMinerWeb.pojo.matriz;

import br.edu.utfpr.cm.JGitMinerWeb.pojo.miner.InterfaceEntity;
import java.io.Serializable;
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
@Table(name = "matrizRecord")
public class EntityMatrizRecord implements InterfaceEntity, Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String classX;
    private String valueX;
    private String classY;
    private String valueY;
    private String classZ;
    private String valueZ;
    @ManyToOne
    private EntityMatriz matriz;

    public EntityMatrizRecord() {
    }

    public EntityMatrizRecord(Object classX, Object valueX, Object classY, Object valueY, Object classZ, Object valueZ) {
        this.classX = classX + "";
        this.valueX = valueX + "";
        this.classY = classY + "";
        this.valueY = valueY + "";
        this.classZ = classZ + "";
        this.valueZ = valueZ + "";
    }

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public void setId(Long id) {
        this.id = id;
    }

    public String getClassX() {
        return classX;
    }

    public void setClassX(String classX) {
        this.classX = classX;
    }

    public String getValueX() {
        return valueX;
    }

    public void setValueX(String valueX) {
        this.valueX = valueX;
    }

    public String getClassY() {
        return classY;
    }

    public void setClassY(String classY) {
        this.classY = classY;
    }

    public String getValueY() {
        return valueY;
    }

    public void setValueY(String valueY) {
        this.valueY = valueY;
    }

    public String getClassZ() {
        return classZ;
    }

    public void setClassZ(String classZ) {
        this.classZ = classZ;
    }

    public String getValueZ() {
        return valueZ;
    }

    public void setValueZ(String valueZ) {
        this.valueZ = valueZ;
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
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof EntityMatrizRecord)) {
            return false;
        }
        EntityMatrizRecord other = (EntityMatrizRecord) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return classX + "\t\t" + valueX + "\t\t" + classY + "\t\t" + valueY + "\t\t" + classZ + "\t\t" + valueZ;
    }
}
