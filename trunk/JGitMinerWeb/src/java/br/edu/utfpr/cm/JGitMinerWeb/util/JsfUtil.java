package br.edu.utfpr.cm.JGitMinerWeb.util;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.component.UIViewRoot;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

public class JsfUtil {

    public static void ensureAddErrorMessage(Exception ex, String defaultMsg) {
        String msg = ex.getLocalizedMessage();
        if (msg != null && msg.length() > 0) {
            addErrorMessage(msg);
        } else {
            addErrorMessage(defaultMsg);
        }
    }

    public static void addErrorMessages(List<String> messages) {
        for (String message : messages) {
            addErrorMessage(message);
        }
    }

    public static void addErrorMessage(String msg) {
        FacesMessage facesMsg = new FacesMessage(FacesMessage.SEVERITY_ERROR, msg, msg);
        FacesContext.getCurrentInstance().addMessage(null, facesMsg);
    }

    public static void addCustomMessage(FacesMessage.Severity severity, String msg) {
        FacesMessage facesMsg = new FacesMessage(severity, msg, msg);
        FacesContext.getCurrentInstance().addMessage(null, facesMsg);
    }

    public static void addSuccessMessage(String msg) {
        FacesMessage facesMsg = new FacesMessage(FacesMessage.SEVERITY_INFO, msg, msg);
        FacesContext.getCurrentInstance().addMessage(null, facesMsg);
    }

    /**
     * <p>Exibe uma mensagem para um componente que esteja dentro de um form<br>
     * ex: exibir um mensagem de erro de validação para um campo email.</p>
     *
     * @param msg - Mensagem que será exibida para o componente.
     * @param componentClientID - ID do componente em que a mensagem será
     * mostrada, esse ID deve estar no formato
     * "idDoFormulario:idDoComponente".<br> <p>Tanto o formulário quanto o
     * componente devem possuir um ID.</p>
     */
    public static void addMessageToComponentClientID(String summary, String msg, String componentClientID) {
        FacesMessage facesMsg = new FacesMessage(FacesMessage.SEVERITY_ERROR, summary, msg);
        FacesContext.getCurrentInstance().addMessage(componentClientID, facesMsg);
    }

    /**
     * <p>Exibe uma mensagem para um componente específico da aplicação<br> ex:
     * exibir um mensagem de erro de validação para um campo email.</p>
     *
     * @param msg - Mensagem a ser exibida para o componente especificado.
     * @param componentID - ID do componente em para o qual a mensagem será
     * exibida. <b>Não</b> precisa ser um ID no formato
     * "idDoFormulario:idDoComponente".<br> É necessário apenas indicar o ID do
     * componente que o método irá buscar em toda a árvore DOM.
     */
    public static void addMessageToComponentID(String summary, String msg, String componentID) {
        FacesMessage facesMsg = new FacesMessage(FacesMessage.SEVERITY_ERROR, summary, msg);
        UIComponent mybutton = findComponent(FacesContext.getCurrentInstance().getViewRoot(), componentID);
        FacesContext.getCurrentInstance().addMessage(mybutton.getClientId(FacesContext.getCurrentInstance()), facesMsg);
    }

    private static UIComponent findComponent(UIComponent parent, String id) {
        if (id.equals(parent.getId())) {
            return parent;
        }
        Iterator<UIComponent> kids = parent.getFacetsAndChildren();
        while (kids.hasNext()) {
            UIComponent kid = kids.next();
            UIComponent found = findComponent(kid, id);
            if (found != null) {
                return found;
            }
        }
        return null;
    }

    /**
     * Obtem o valor de um parametro da requisição a partir do seu nome
     * (geralmente o ID de um input ou outro componente HTML).
     *
     * @param key nome do parametro que se deseja obter o valor.
     * @return uma <code>String</code> contendo o valor do parâmetro referente
     * ao nome recebido.
     */
    public static String getRequestParameter(String key) {
        return FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get(key);
    }

    /**
     * <p>Obtem um objeto da requisição a partir do seu nome e do componente
     * onde ele está inserido.<br> ex: obter o objeto referente a linha
     * selecionada de uma dataTable ou de um item selecionado em um
     * selectOnMenu.<br> Para isso é utilizado o nome definido no atributo
     * <code>var</code> da tag jsf</p>
     *
     * @param requestParameterName nome do parametro que se deseja obter.
     * @param converter <code>Converter</code> referente a classe do objeto que
     * será obtido.
     * @param component componente do qual o objeto faz parte (ex: dataTable ou
     * selectOnMenu)
     * @return um objeto do tipo <code>Object</code> referente ao parametro
     * identificado pelo nome recebido.
     */
    public static Object getObjectFromRequestParameter(String requestParameterName, Converter converter, UIComponent component) {
        String theId = getRequestParameter(requestParameterName);
        return converter.getAsObject(FacesContext.getCurrentInstance(), component, theId);
    }

    /**
     * <p>Obtem um objeto da requisição a partir do seu nome.<br>
     *
     * @param requestParameterName nome do parametro que se deseja obter.
     * @return um objeto do tipo <code>Object</code> referente ao parametro
     * identificado pelo nome recebido.
     */
    public static Object getObjectFromRequestParameter(String requestParameterName) {
        HttpServletRequest request = (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();
        return request.getAttribute(requestParameterName);
    }

    /**
     * <p>Obtem um objeto referente a um atributo da sessão a partir do nome
     * desse atributo.</p>
     *
     * @param key nome do atributo que será obtido da sessao.
     * @return um objeto do tipo <code>Object</code> referente ao atributo
     * identificado pelo nome recebido como parametro.
     */
    public static Object getObjectFromSession(String key) {
        return FacesContext.getCurrentInstance().getExternalContext().getSessionMap().get(key);
    }

    public static void dispatch(String s) {
        try {
            FacesContext.getCurrentInstance().getExternalContext().dispatch(s);
        } catch (IOException ex) {
            Logger.getLogger(JsfUtil.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * <p>Redireciona para uma URL específica recebida como parametro
     *
     * @param url URL para a qual será redirecionada a página.
     */
    public static void redirect(String url) {
        try {
            FacesContext.getCurrentInstance().getExternalContext().redirect(url);
        } catch (IOException ex) {
            Logger.getLogger(JsfUtil.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static HttpSession getSession(Boolean create) {
        FacesContext fc = FacesContext.getCurrentInstance();
        HttpSession session = (HttpSession) fc.getExternalContext().getSession(create);
        return session;
    }

    /**
     * Pega o contexto atual da aplicação.
     *
     * @return o contexto atual da aplicação.
     * @see javax.faces.context.FacesContext
     */
    public static FacesContext getContext() {
        return FacesContext.getCurrentInstance();
    }

    /**
     * Encontra um componente a partir da visão principal.
     *
     * @param componentId id do componente a ser buscado.
     * @return retorna o componente com o id especificado caso ele exista ou
     * <code>null</code> caso contrário.
     */
    public static UIComponent findComponent(String componentId) {
        UIComponent component = UIViewRoot.getCurrentComponent(getContext());
        return component.findComponent(componentId);
    }
}