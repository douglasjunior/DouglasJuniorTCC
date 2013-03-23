/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package br.edu.utfpr.cm.JGitMinerWeb.services.matriz.auxiliary;

import br.edu.utfpr.cm.JGitMinerWeb.util.Util;

/**
 *
 * @author douglas
 */
public class AuxFileCount {

    private int count;
    private String fileName;

    public AuxFileCount(Object fileName, Object count) {
        this.fileName = fileName + "";
        this.count = Util.tratarStringParaInt(count + "");
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
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
