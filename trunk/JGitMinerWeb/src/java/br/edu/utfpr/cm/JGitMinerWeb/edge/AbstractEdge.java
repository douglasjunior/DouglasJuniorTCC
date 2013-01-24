/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package br.edu.utfpr.cm.JGitMinerWeb.edge;

/**
 *
 * @author douglas
 */
public abstract class AbstractEdge<X, Y, V> {

    private X x;
    private Y y;
    private V value;

    public AbstractEdge(X x, Y y, V value) {
        this.x = x;
        this.y = y;
        this.value = value;
    }

    public X getX() {
        return x;
    }

    public String getStringX() {
        return getX() + "";
    }

    public void setX(X x) {
        this.x = x;
    }

    public Y getY() {
        return y;
    }

    public String getStringY() {
        return getY() + "";
    }

    public void setY(Y y) {
        this.y = y;
    }

    public V getValue() {
        return value;
    }

    public String getStringValue() {
        return getValue() + "";
    }

    public void setValue(V value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return getStringX() + ";" + getStringY() + ";" + getStringValue();
    }
}
