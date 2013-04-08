/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package br.edu.utfpr.cm.JGitMinerWeb.util;

import edu.uci.ics.jung.algorithms.layout.CircleLayout;
import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.visualization.VisualizationImageServer;
import edu.uci.ics.jung.visualization.decorators.ToStringLabeller;
import edu.uci.ics.jung.visualization.renderers.DefaultVertexLabelRenderer;
import edu.uci.ics.jung.visualization.renderers.Renderer;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Paint;
import java.awt.Point;
import java.awt.Stroke;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.text.DecimalFormat;
import java.text.Normalizer;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import javax.imageio.ImageIO;
import org.apache.commons.collections15.Transformer;

/**
 *
 * @author Douglas
 */
public class Util {

    private static SimpleDateFormat dateHoraFormatBr = new SimpleDateFormat("HH:mm");
    private static SimpleDateFormat dateDataFormatBr = new SimpleDateFormat("dd/MM/yyyy");
    private static DecimalFormat decimalFormat = new DecimalFormat();
    private static final String[] CARACTERES_INVALIDOS_DIRETORIO = {"/", "\\", ":", "*", "?", "\"", "<", ">", "|"};
    /// imagens

    // GUI ////////////////////////////////////////////////////////////
    /**
     * Verifica a validade de um e-mail.
     *
     * @param email String com E-mail a ser verificado.
     * @return True se e-mail válido. False caso contrário.
     */
    public static boolean validaEmail(String texto) {
        return Pattern.matches("^[\\w-]+(\\.[\\w-]+)*@([\\w-]+\\.)+[a-zA-Z]{2,7}$", texto);
    }

    /**
     * Verifica se um dado objeto está contido em um array.
     *
     * @param array Array no qual será realizada a busca pelo objeto.
     * @param procurado Objeto a procurar no array.
     * @return True se o array contém o objeto procurado. False caso contrário.
     */
    public static boolean arrayContains(Object[] array, Object procurado) {
        if (array != null) {
            for (Object object : array) {
                if (object == procurado) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Converte a data em String no formato dd/mm/aaaa para um objeto Date.
     *
     * @param data Data no formato dd/mm/aaaa.
     * @return Objeto Date contendo a data fornecida.
     */
    public static Date stringDataToDate(String data) {
        try {
            return dateDataFormatBr.parse(data);
        } catch (ParseException ex) {
            Logger.getLogger(Util.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    /**
     * Converte a data em object Date para String no formato dd/mm/aaaa.
     *
     * @param data Objeto data.
     * @return Data como String no formato dd/mm/aaaa.
     */
    public static String dateDataToString(Date data) {
        if (data != null) {
            return dateDataFormatBr.format(data);
        } else {
            return "  /  /    ";
        }
    }

    public static String dateDataToString(Date data, String formato) {
        if (data != null) {
            return new SimpleDateFormat(formato).format(data);
        } else {
            return "  /  /    ";
        }
    }

    /**
     * Converte a hora em String no formato hh:mm para um objeto Date.
     *
     * @param hora Hora no formato hh:mm.
     * @return Objeto Date contendo a hora fornecida.
     */
    public static Date stringHoraToDate(String hora) {
        try {
            return dateHoraFormatBr.parse(hora);
        } catch (ParseException ex) {
            Logger.getLogger(Util.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    /**
     * Converte a hora em object Date para String no formato hh:mm.
     *
     * @param hora Objeto Date.
     * @return Hora como String no formato hh:mm.
     */
    public static String dateHoraToString(Date hora) {
        if (hora != null) {
            return dateHoraFormatBr.format(hora);
        } else {
            return "  :  ";
        }
    }

    /**
     * Converte a data no formato aaaa-mm-dd (ISO) para o formato dd/mm/aaaa
     * (BR).
     *
     * @param data Data no formato aaaa-mm-dd (ISO).
     * @return Data no formato dd/mm/aaaa (BR).
     */
    public static String dateIsoToBr(String data) {
        // aaaa-mm-dd  >>  dd/mm/aaaa
        String[] d = data.split("-");
        return d[2] + "/" + d[1] + "/" + d[0];
    }

    /**
     * Converte a data no formato dd/mm/aaaa (BR) para o formato aaaa-mm-dd
     * (ISO).
     *
     * @param data Data no formato dd/mm/aaaa (BR).
     * @return Data no formato aaaa-mm-dd (ISO).
     */
    public static String dateBrToIso(String data) {
        // dd/mm/aaaa  >>  aaaa-mm-dd
        String[] d = data.split("/");
        return d[2] + "-" + d[1] + "-" + d[0];
    }

    /**
     * Converte tipo de dados 'String' para 'double'. Troca vírgula por ponto e
     * retira pontuação de milhagem se houver.
     *
     * @param valor String contendo valor a ser convertido.
     * @return Valor convertido no tipo 'double'.
     */
    public static Double tratarStringParaDouble(String valor) {
        if (valor == null
                || valor.length() == 0
                || !soContemNumeros(valor.replaceAll("\\.", "").replaceAll(",", ""))) {
            return 0.0;
        }
        try {
            valor = valor.replaceAll("\\.", "");
            valor = valor.replaceAll(",", "\\.");
            return Double.parseDouble(valor);
        } catch (Exception ex) {
            ex.printStackTrace();
            return 0.0;
        }
    }

    public static String tratarDoubleParaString(double valor) {
        return (valor + "").replace(".", ",");
    }

    /**
     * Converte tipo de Dados de 'double' para String.
     *
     * @param valor Número do tipo 'double'.
     * @param casasDecimais Quantidade de casas decimais desejadas no tipo
     * 'int'.
     * @return String contendo o valor convertido neste formato '####,##'.
     */
    public static String tratarDoubleParaString(double valor, int casasDecimais) {
        return tratarDoubleParaString(valor, casasDecimais, 0);
    }

    /**
     * Converte tipo de Dados de 'double' para String.
     *
     * @param valor Número do tipo 'double'.
     * @param casasDecimais Quantidade de casas decimais desejadas no tipo
     * 'int'.
     * @return String contendo o valor convertido neste formato '####,##'.
     */
    public static String tratarDoubleParaString(double valor, int casasDecimais, int zerosEsquerda) {
        String mascara = "";
        if (zerosEsquerda > 0) {
            int contador = 0;
            for (int i = 0; i < zerosEsquerda; i++) {
                mascara = "0" + mascara;
                contador++;
                if (contador == 3) {
                    mascara = "," + mascara;
                }
            }
        } else {
            mascara = "#,##0";
        }
        if (casasDecimais > 0) {
            mascara = mascara + ".";
            for (int i = 1; i <= casasDecimais; i++) {
                mascara = mascara + "0";
            }
        }
        decimalFormat.applyPattern(mascara);
        return decimalFormat.format(valor);
    }

    public static boolean dataValidaString(String data) {
        //String er = "^(([0-2]\\d|[3][0-1])\\/([0]\\d|[1][0-2])\\/[1-2][0-9]\\d{2})$";

        data = data.replaceAll("/", "");


        if (!soContemNumeros(data)) {
            return false;
        }

        int dia = Integer.parseInt(data.substring(0, 2));
        int mes = Integer.parseInt(data.substring(2, 4));
        int ano = Integer.parseInt(data.substring(4, 8));

        if (ano < 1940) {
            return false;
        }
        if (dia < 1 || dia > 31) {
            return false;
        }
        if (mes < 1 || mes > 12) {
            return false;
        }
        if ((mes == 4 || mes == 6 || mes == 9 || mes == 11) && (dia > 30)) {
            return false;
        }
        if (ano % 4 != 0 && mes == 02 && dia > 28) {
            return false;
        }
        if (ano % 4 == 0 && mes == 02 && dia > 29) {
            return false;
        }

        return true;
    }

    /**
     * Verifica se a String informada só contem números. Aceita números com
     * sinal negativo ou positivo
     *
     * @param texto String com o conteúdo desejado.
     * @return Retorna 'true' caso só contenha caracteres alfanuméricos ou
     * esteja em branco e 'false' caso contenha outro tipo de caractere.
     */
    public static boolean soContemNumeros(String texto) {
        if (texto.isEmpty()) {
            return true;
        }
        for (int i = 0; i < texto.toCharArray().length; i++) {
            char c = texto.toCharArray()[i];
            if (!(i == 0 && (c == '-' || c == '+'))) {
                if (!Character.isDigit(c)) {
                    return false;
                }
            }
        }
        return true;
    }

    public static String extrairSomenteNumeros(String texto) {
        String saida = "";
        for (char c : texto.toCharArray()) {
            if (Character.isDigit(c)) {
                saida += c;
            }
        }
        return saida;
    }

    public static String removerFormatacaoVazia(String texto) {
        if (extrairSomenteNumeros(texto).trim().isEmpty()) {
            return "";
        } else {
            return texto;
        }
    }

    public static String formatarString(String valor, String mascara) {

        String dado = "";
        // remove caracteres nao numericos
        for (int i = 0; i < valor.length(); i++) {
            char c = valor.charAt(i);
            if (Character.isDigit(c)) {
                dado += c;
            }
        }

        int indMascara = mascara.length();
        int indCampo = dado.length();

        for (; indCampo > 0 && indMascara > 0;) {
            if (mascara.charAt(--indMascara) == '#') {
                indCampo--;
            }
        }

        String saida = "";
        for (; indMascara < mascara.length(); indMascara++) {
            saida += ((mascara.charAt(indMascara) == '#') ? dado.charAt(indCampo++) : mascara.charAt(indMascara));
        }
        return saida;
    }

    public static int tratarStringParaInt(String numero) {
        if (numero == null
                || numero.isEmpty()
                || !soContemNumeros(numero)) {
            return 0;
        }
        return Integer.parseInt(numero);
    }

    public static String getStringDataPorExtenso(Date data) {
        String extenso = "";
        SimpleDateFormat format = new SimpleDateFormat();
        format.applyPattern("dd");
        extenso += format.format(data);
        extenso += " de ";
        format.applyPattern("MMMMM");
        extenso += format.format(data);
        extenso += " de ";
        format.applyPattern("yyyy");
        extenso += format.format(data);
        return extenso;
    }

    public static byte[] bytesFromFile(File file) {
        if (file != null) {
            try {
                FileInputStream is = new FileInputStream(file);
                ByteArrayOutputStream out = new ByteArrayOutputStream();

                byte[] buf = new byte[(int) file.length()];
                int r = is.read(buf);
                while (r != -1) {
                    out.write(buf, 0, r);
                    r = is.read(buf);
                }

                return out.toByteArray();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        return null;
    }

    public static String removerAcentos(String input) {
        input = Normalizer.normalize(input, Normalizer.Form.NFD);
        input = input.replaceAll("[^\\p{ASCII}]", "");
        return input;
    }

    public static String removerCaracteresNaoPermitidosEmDiretorios(String diretorio) {
        StringBuilder padrao = new StringBuilder();
        padrao.append("[^a-zA-Z0-9|");
        for (int i = 0; i < CARACTERES_INVALIDOS_DIRETORIO.length; i++) {
            padrao.append(CARACTERES_INVALIDOS_DIRETORIO[i]);
            if (i < CARACTERES_INVALIDOS_DIRETORIO.length - 1) {
                padrao.append("|");
            }
        }
        padrao.append("]");
        return (diretorio.replaceAll(padrao.toString(), " "));
    }

    public static long tratarStringParaLong(String numero) {
        if (numero == null
                || numero.isEmpty()
                || !soContemNumeros(numero)) {
            return 0;
        }
        return Long.parseLong(numero);
    }

    public static void convertGraphToImage(Graph grap) {
        try {
            Layout layout = new CircleLayout(grap);

            Dimension dime = new Dimension(grap.getEdgeCount() * 100, grap.getEdgeCount() * 100);

            VisualizationImageServer vv = new VisualizationImageServer(layout, dime);

            Transformer<String, Paint> vertexPaint = new Transformer<String, Paint>() {
                @Override
                public Paint transform(String i) {
                    return Color.BLUE;
                }
            };

            Transformer<String, Stroke> edgeStrokeTransformer = new Transformer<String, Stroke>() {
                @Override
                public Stroke transform(String s) {
                    Stroke edgeStroke = new BasicStroke(1.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER);
                    return edgeStroke;
                }
            };

            vv.getRenderContext().setVertexFillPaintTransformer(vertexPaint);
            vv.getRenderContext().setEdgeStrokeTransformer(edgeStrokeTransformer);
            vv.getRenderContext().setVertexLabelTransformer(new ToStringLabeller());
            vv.getRenderContext().setEdgeLabelTransformer(new ToStringLabeller());
            vv.getRenderContext().setVertexLabelRenderer(new DefaultVertexLabelRenderer(Color.yellow) {
                @Override
                public Font getFont() {
                    return new Font(Font.SERIF, 1, 30);
                }

                @Override
                public Color getForeground() {
                    return Color.YELLOW;
                }
            });
            vv.getRenderer().getVertexLabelRenderer().setPosition(Renderer.VertexLabel.Position.AUTO);

            BufferedImage bim = (BufferedImage) vv.getImage(new Point(), dime);

            File f = new File(Util.dateDataToString(new Date(), "dd-MM-yyyy_HH-mm") + "_imagem_teste.png");

            ImageIO.write(bim, "png", f);

            System.out.println("wrote image for " + f.getAbsolutePath());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static void addMapToProperties(Properties params, Map<String, String> params0) {
        if (params != null && params0 != null) {
            for (String key : params0.keySet()) {
                if (key != null && params0.get(key) != null) {
                    params.put(key, params0.get(key));
                }
            }
        }
    }
}
