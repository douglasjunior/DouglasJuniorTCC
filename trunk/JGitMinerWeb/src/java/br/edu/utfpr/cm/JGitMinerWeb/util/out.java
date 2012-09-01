/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package br.edu.utfpr.cm.JGitMinerWeb.util;

import java.util.Date;

/**
 *
 * @author Douglas
 */
public class out {

    private static String log;
    private static String lastLog;

    public static String getLastLog() {
        return lastLog;
    }

    public static String getLog() {
        return log;
    }

    public static void printLog(String log) {
        out.lastLog = log;
        out.log = new Date() + ": " + log + "\n" + out.log;
        System.out.println(log);
    }

    public static void resetLog() {
        out.log = "";
        out.lastLog = "";
    }
}
