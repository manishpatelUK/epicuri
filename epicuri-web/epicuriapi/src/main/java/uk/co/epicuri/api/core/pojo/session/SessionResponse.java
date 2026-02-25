package uk.co.epicuri.api.core.pojo.session;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Manish on 23/06/2015.
 */
public class SessionResponse {
    private List<Session> sessions = new ArrayList<>();

    public List<Session> getSessions() {
        return sessions;
    }

    public void setSessions(List<Session> sessions) {
        this.sessions = sessions;
    }
}
