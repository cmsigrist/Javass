package main.java.jass;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * An enum containing all the ID of the teams
 *
 * @author Kilian Schneiter (287496)
 * @author Capucine Berger(269503)
 */

public enum TeamId {
    TEAM_1,
    TEAM_2;

    public final static List<TeamId> ALL = Collections.unmodifiableList(Arrays.asList(values()));
    public final static int COUNT = 2;

    /**
     * @return the opposing TeamId
     */
    public TeamId other() {
        return (this == TEAM_1) ? TEAM_2 : TEAM_1;
    }
}
