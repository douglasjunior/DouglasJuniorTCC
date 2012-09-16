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

    private static StringBuilder log;
    private static String currentProcess;

    static {
        log = new StringBuilder();
    }

    public static String getCurrentProcess() {
        return currentProcess;
    }

    public static void setCurrentProcess(String currentProcess) {
        out.currentProcess = currentProcess;
        out.printLog(currentProcess);
    }

    public static String getLog() {
        return log.toString();
    }

    public static void printLog(String log) {
        out.log.insert(0, new Date() + ": " + log + "\n");
        System.out.println(log);
    }

    public static void resetLog() {
        out.log = new StringBuilder();
        out.currentProcess = "";
    }
}
