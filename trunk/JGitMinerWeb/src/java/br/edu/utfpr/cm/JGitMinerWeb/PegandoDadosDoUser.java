/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package br.edu.utfpr.cm.JGitMinerWeb;

import br.edu.utfpr.cm.JGitMinerWeb.dao.PersistenciaServices;
import br.edu.utfpr.cm.JGitMinerWeb.pojo.EntityUser;
import java.io.IOException;
import java.util.List;
import org.eclipse.egit.github.core.User;
import org.eclipse.egit.github.core.service.UserService;

/**
 *
 * @author Douglas
 */
public class PegandoDadosDoUser {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws IOException {
        try {
            PersistenciaServices.dataBaseConnect(false);
        } catch (Exception ex) {
            ex.printStackTrace();
            System.exit(1);
        }
        List<EntityUser> users = PersistenciaServices.selecionaTodos(EntityUser.class, "idUser");

        System.err.println("Selecionados " + users.size() + " usuários no banco de dados para atualização.");

        UserService userService = new UserService();

        for (EntityUser entityUser : users) {
            if(entityUser.getType() != null && !entityUser.getType().isEmpty()){
                System.err.println("Usuário já atualizado ####################################");
                continue;
            }
            
            System.out.println("Iniciada aualização do usuário: " + entityUser.getLogin());
            User gitUser = userService.getUser(entityUser.getLogin());
  //          entityUser.updateData(gitUser);

            System.out.println("Atualizado usuário: " + entityUser.getLogin());
            PersistenciaServices.atualiza(entityUser);

            System.out.println("Concluída aualização do usuário: " + entityUser.getLogin());
            System.err.println("###########################################################");
        }

        System.out.println("terminou");
    }
}
