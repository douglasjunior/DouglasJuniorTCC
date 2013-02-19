/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package br.edu.utfpr.cm.JGitMinerWeb.services.miner;

import br.edu.utfpr.cm.JGitMinerWeb.dao.GenericDao;
import br.edu.utfpr.cm.JGitMinerWeb.pojo.miner.EntityTeam;
import br.edu.utfpr.cm.JGitMinerWeb.util.OutLog;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import org.eclipse.egit.github.core.Repository;
import org.eclipse.egit.github.core.Team;
import org.eclipse.egit.github.core.service.TeamService;

/**
 *
 * @author douglas
 */
public class TeamServices implements Serializable {

    public static List<Team> getGitTeamsFromRepository(Repository gitRepo, OutLog out) {
        List<Team> teams = new ArrayList<Team>();
        try {
            out.printLog("Baixando Teams...\n");
            teams.addAll(new TeamService(AuthServices.getGitHubCliente()).getTeams(gitRepo));
            out.printLog(teams.size() + " Teams baixados no total!");
        } catch (Exception ex) {
            ex.printStackTrace();
            out.printLog(teams.size() + " Teams baixadaos no total! Erro: " + ex.toString());
        }
        return teams;
    }

    public static EntityTeam createEntity(Team gitTeam, GenericDao dao) {
        if (gitTeam == null) {
            return null;
        }

        EntityTeam team = getTeamByTeamID(gitTeam.getId(), dao);

        if (team == null) {
            team = new EntityTeam();
        }

        team.setIdTeam(gitTeam.getId());
        team.setMembersCount(gitTeam.getMembersCount());
        team.setName(gitTeam.getName());
        team.setPermission(gitTeam.getPermission());
        team.setReposCount(gitTeam.getReposCount());
        team.setUrl(gitTeam.getUrl());

        if (team.getId() == null || team.getId().equals(new Long(0))) {
            dao.insert(team);
        } else {
            dao.edit(team);
        }

        return team;
    }

    private static EntityTeam getTeamByTeamID(int idTeam, GenericDao dao) {
        List<EntityTeam> teams = dao.executeNamedQueryComParametros("Team.findByTeamID", new String[]{"idTeam"}, new Object[]{idTeam}, true);
        if (!teams.isEmpty()) {
            return teams.get(0);
        }
        return null;
    }
}
