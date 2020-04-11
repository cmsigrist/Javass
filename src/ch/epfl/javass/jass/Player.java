package ch.epfl.javass.jass;

import java.util.Map;

/**
 * The interface of a player
 *
 * @author Kilian Schneiter (287496)
 * @author Capucine Berger(269503)
 */

public interface Player {
     abstract Card cardToPlay(TurnState state, CardSet hand);

     default void setPlayers(PlayerId ownId, Map<PlayerId, String> playerNames) {}
     default void updateHand(CardSet newHand) {}
     default void setTrump(Card.Color trump) {}
     default void updateTrick(Trick newTrick) {}
     default void updateScore(Score score) {}
     default void setWinningTeam(TeamId winningTeam) {}
}
