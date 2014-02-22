/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package br.edu.utfpr.cm.JGitMinerWeb.model.matriz;

import br.edu.utfpr.cm.JGitMinerWeb.model.EntityNode;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

/**
 *
 * @author douglas
 */
@Entity
@DiscriminatorValue(value = "EntityMatrizNode")
public class EntityMatrizNode extends EntityNode {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "matriz_id")
    private EntityMatriz matriz;

    public EntityMatrizNode() {
        super();
    }

    public EntityMatrizNode(Object line) {
        super(line);
    }

    public EntityMatriz getMatriz() {
        return matriz;
    }

    public void setMatriz(EntityMatriz matriz) {
        this.matriz = matriz;
    }
}
