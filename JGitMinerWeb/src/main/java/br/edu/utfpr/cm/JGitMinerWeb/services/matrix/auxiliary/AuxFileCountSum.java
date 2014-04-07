/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package br.edu.utfpr.cm.JGitMinerWeb.services.matrix.auxiliary;

import br.edu.utfpr.cm.JGitMinerWeb.util.Util;

/**
 *
 * @author douglas
 */
public class AuxFileCountSum {

    private long count;
    private long sum;
    private String fileName;

    public AuxFileCountSum(Object fileName, Object count, Object sum) {
        this.fileName = fileName + "";
        this.count = Util.tratarStringParaLong(count + "");
        this.sum = Util.tratarStringParaLong(sum + "");
    }

    public long getCount() {
        return count;
    }

    public void setCount(long count) {
        this.count = count;
    }

    public long getSum() {
        return sum;
    }

    public void setSum(long sum) {
        this.sum = sum;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    @Override
    public String toString() {
        return fileName + ";" + count;
    }
}
