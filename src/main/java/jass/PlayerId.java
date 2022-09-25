package main.java.jass;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * An enum containing the ID of the player
 *
 * @author Kilian Schneiter (287496)
 * @author Capucine Berger(269503)
 */

public enum PlayerId {

    PLAYER_1,
    PLAYER_2,
    PLAYER_3,
    PLAYER_4;

    public final static List<PlayerId> ALL = Collections.unmodifiableList(Arrays.asList(values()));
    public final static int COUNT = 4;

    /**
     * @return the TeamId corresponding to the PlayerId. Player 1 and 3 are in team 1, others in team 2
     */
    public TeamId team() {
        return (this == PLAYER_1 || this == PLAYER_3) ? TeamId.TEAM_1 : TeamId.TEAM_2;
    }
}
