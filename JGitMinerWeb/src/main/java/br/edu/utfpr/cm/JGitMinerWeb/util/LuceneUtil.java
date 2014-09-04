/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.edu.utfpr.cm.JGitMinerWeb.util;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.core.StopAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.util.Version;

/**
 *
 * @author geazzy
 */
public final class LuceneUtil {

    //Remove os stopwords;
    public static List<String> tokenizeString(String linha) {
       
        Analyzer analyzer = new StopAnalyzer(Version.LUCENE_4_9);

        List<String> result = new ArrayList<>();

        try {
            TokenStream stream = analyzer.tokenStream(null, new StringReader(linha));
            stream.reset();
            while (stream.incrementToken()) {

                result.add(stream.getAttribute(CharTermAttribute.class).toString());

            }
        } catch (IOException e) {
            System.out.println(e.getMessage());

        }

        return result;
    }

}
