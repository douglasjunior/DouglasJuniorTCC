/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package br.edu.utfpr.cm.JGitMinerWeb.util.matriz;

/**
 *
 * @author jteodoro
 */
public interface NodeConnection<T, K> {

    public double getWeight();

    public void setWeight(double weight);

    public T getFrom();

    public K getTo();

    public void incWeight();
}
