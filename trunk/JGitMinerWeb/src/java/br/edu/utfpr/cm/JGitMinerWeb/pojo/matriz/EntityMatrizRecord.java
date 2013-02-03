/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package br.edu.utfpr.cm.JGitMinerWeb.pojo.matriz;

import java.io.Serializable;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 *
 * @author douglas
 */
@Entity
@Table(name = "matrizRecord")
public class EntityMatrizRecord implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String classX;
    private String idX;
    private String classY;
    private String idY;
    private String amount;

    public EntityMatrizRecord() {
    }

    public EntityMatrizRecord(Object classX, Object idX, Object classY, Object idY, Object amount) {
        this();
        this.classX = classX + "";
        this.idX = idX + "";
        this.classY = classY + "";
        this.idY = idY + "";
        this.amount = amount + "";
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getClassX() {
        return classX;
    }

    public void setClassX(String classX) {
        this.classX = classX;
    }

    public String getIdX() {
        return idX;
    }

    public void setIdX(String idX) {
        this.idX = idX;
    }

    public String getClassY() {
        return classY;
    }

    public void setClassY(String classY) {
        this.classY = classY;
    }

    public String getIdY() {
        return idY;
    }

    public void setIdY(String idY) {
        this.idY = idY;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
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
}
