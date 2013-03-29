/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package br.edu.utfpr.cm.JGitMinerWeb.pojo.metric;

import br.edu.utfpr.cm.JGitMinerWeb.pojo.EntityNode;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

/**
 *
 * @author douglas
 */
@Entity
@Table(name = "metricNode")
public class EntityMetricNode extends EntityNode {

    @ManyToOne(fetch = FetchType.LAZY)
    private EntityMetric metric;

    public EntityMetricNode() {
        super();
    }

    public EntityMetricNode(Object line) {
        super(line);
    }

    public EntityMetric getMetric() {
        return metric;
    }

    public void setMetric(EntityMetric metric) {
        this.metric = metric;
    }
}
