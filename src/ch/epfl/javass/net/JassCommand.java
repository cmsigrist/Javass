package ch.epfl.javass.net;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * An Enum made for messages between the server and the client
 *
 * @author Kilian Schneiter (287496)
 * @author Capucine Berger(269503)
 */

public enum JassCommand {
    PLRS,
    TRMP,
    HAND,
    TRCK,
    CARD,
    SCOR,
    WINR;

    public final static List<JassCommand> ALL = Collections.unmodifiableList(Arrays.asList(values()));
    public final static int COUNT = 7;

}
