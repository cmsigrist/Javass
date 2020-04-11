package ch.epfl.javass.jass;

import java.util.Map;

/**
 * A paced Player
 *
 * @author Kilian Schneiter (287496)
 * @author Capucine Berger(269503)
 */

public final class PacedPlayer  implements Player {

    private final Player underlyingPlayer;
    private final long minTime;

    /**
     * Builds a PacedPlayer
     *
     * @param underlyingPlayer the player
     * @param minTime the minimum amount of time before a player can put hsi card on the table
     */
    public PacedPlayer(Player underlyingPlayer, double minTime) {
        this.underlyingPlayer = underlyingPlayer;
        this.minTime =(long) minTime * 1000;
    }

    /**
     * Given a turnState and the hand of the PacedPlayer, returns which card he will play
     *
     * @param state the state of the game
     * @param hand the hand of the PacedPlayer
     * @return a Card
     */
    @Override
    public Card cardToPlay(TurnState state, CardSet hand) {
        long currentHour = System.currentTimeMillis();
        Card cardToPlay = underlyingPlayer.cardToPlay(state, hand);
        long currentTimeMillis = System.currentTimeMillis();
        if(currentTimeMillis - currentHour < minTime) {
            try {
                Thread.sleep(minTime - (currentTimeMillis - currentHour));
            } catch (InterruptedException e) { /* ignore */ }
        }
        return cardToPlay;
    }

    public void setPlayers(PlayerId ownId, Map<PlayerId, String> playerNames) {
        underlyingPlayer.setPlayers(ownId, playerNames);
    }

    /**
     * Updates the hand of the PacedPlayer
     *
     * @param newHand, the updated hand of the player
     */
    public void updateHand(CardSet newHand) {
        underlyingPlayer.updateHand(newHand);
    }

    /**
     * Sets the colour of the trump for the turn
     *
     * @param trump the colour of the trump
     */
    public void setTrump(Card.Color trump) {
        underlyingPlayer.setTrump(trump);
    }

    /**
     * Updates the current trick
     *
     * @param newTrick the updated trick
     */
    public void updateTrick(Trick newTrick) {
        underlyingPlayer.updateTrick(newTrick);
    }

    /**
     * Updates the score of the player
     *
     * @param score, the updated score
     */
    public void updateScore(Score score) {
        underlyingPlayer.updateScore(score);
    }

    /**
     * Sets the winning team of the game
     *
     * @param winningTeam the winning team
     */
    public void setWinningTeam(TeamId winningTeam) {
        underlyingPlayer.setWinningTeam(winningTeam);
    }


}
