/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package br.edu.utfpr.cm.JGitMinerWeb.services;

import br.edu.utfpr.cm.JGitMinerWeb.dao.GenericDao;
import br.edu.utfpr.cm.JGitMinerWeb.pojo.EntityIssue;
import br.edu.utfpr.cm.JGitMinerWeb.pojo.EntityIssueEvent;
import com.google.gson.reflect.TypeToken;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.eclipse.egit.github.core.IssueEvent;
import org.eclipse.egit.github.core.client.PageIterator;
import org.eclipse.egit.github.core.client.PagedRequest;

/**
 *
 * @author douglas
 */
public class IssueEventServices {

    public static List<IssueEvent> getEventsByIssue(EntityIssue issue, String ownerRepositoryLogin, String repositoryName) {
        // repos/:owner/:repo/issues/:issue_number/events

        PagedRequest<IssueEvent> request = new PagedRequest<IssueEvent>(1, 100);
        StringBuilder uri = new StringBuilder("/repos");
        uri.append('/').append(ownerRepositoryLogin);
        uri.append('/').append(repositoryName);
        uri.append("/issues");
        uri.append('/').append(issue.getNumber());
        uri.append("/events");
        //   uri.append('/').append(eventId);
        request.setUri(uri);
        // request.setType(IssueEvent.class);
        request.setType(new TypeToken<List<IssueEvent>>() {
        }.getType());
        PageIterator<IssueEvent> pageIterator = new PageIterator<IssueEvent>(request, AuthServices.getGitHubCliente());
        List<IssueEvent> elements = new ArrayList<IssueEvent>();
        try {
            while (pageIterator.hasNext()) {
                elements.addAll(pageIterator.next());
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return elements;
    }

    public static EntityIssueEvent createEntity(IssueEvent gitIssueEvent, GenericDao dao) {
        if (gitIssueEvent == null) {
            return null;
        }

        EntityIssueEvent issueEvent = getEventByIssueEventID(gitIssueEvent.getId(), dao);

        if (issueEvent == null) {
            issueEvent = new EntityIssueEvent();

            issueEvent.setMineredAt(new Date());
            issueEvent.setCreatedAt(gitIssueEvent.getCreatedAt());
            issueEvent.setActor(UserServices.createEntity(gitIssueEvent.getActor(), dao, false));
            issueEvent.setCommitId(gitIssueEvent.getCommitId());
            issueEvent.setEvent(gitIssueEvent.getEvent());
            issueEvent.setIdIssueEvent(gitIssueEvent.getId());
            issueEvent.setUrl(gitIssueEvent.getUrl());

            dao.insert(issueEvent);
        }

        return issueEvent;
    }

    private static EntityIssueEvent getEventByIssueEventID(long issueEventID, GenericDao dao) {
        List<EntityIssueEvent> events = dao.executeNamedQueryComParametros("IssueEvent.findByEventIssueID", new String[]{"idIssueEvent"}, new Object[]{issueEventID});
        if (!events.isEmpty()) {
            return events.get(0);
        }
        return null;
    }
}
