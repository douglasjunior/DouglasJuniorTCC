/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package br.edu.utfpr.cm.JGitMinerWeb.services.matriz.nodes;

import br.edu.utfpr.cm.JGitMinerWeb.util.Util;
import br.edu.utfpr.cm.JGitMinerWeb.util.matriz.NodeConnection;
import java.util.Objects;

/**
 *
 * @author douglas
 */
public class NodeGeneric implements NodeConnection<String, String> {

    private String from;
    private String to;
    private double weight;

    public NodeGeneric(Object from, Object to, Object weight) {
        this.from = from + "";
        this.to = to + "";
        this.weight = Util.tratarStringParaDouble(weight + "");
    }

    public NodeGeneric(Object from, Object to) {
        this(from, to, 1);
    }

    public NodeGeneric() {
    }

    @Override
    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    @Override
    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    @Override
    public double getWeight() {
        return weight;
    }

    @Override
    public void setWeight(double weight) {
        this.weight = weight;
    }

    @Override
    public void incWeight() {
        weight++;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj != null && obj instanceof NodeGeneric) {
            NodeGeneric other = (NodeGeneric) obj;
            if (this.from.equals(other.from)
                    && this.to.equals(other.to)) {
                return true;
            }
            if (this.from.equals(other.to)
                    && this.to.equals(other.from)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 97 * hash + Objects.hashCode(this.from);
        hash = 97 * hash + Objects.hashCode(this.to);
        return hash;


    }

    @Override
    public String toString() {
        return from + ";" + to + ";" + weight;
    }
}
