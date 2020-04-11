package ch.epfl.javass.gui;

import ch.epfl.javass.jass.*;

import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;

import static javafx.application.Platform.runLater;

/**
 * A class made to wait for the GraphicalPlayer to play, using an ArrayBlockingQueue and
 * runLater function. It tells the game when the GraphicalPlayer plays a card.
 *
 * @author Kilian Schneiter (287496)
 * @author Capucine Berger(269503)
 */

public final class GraphicalPlayerAdapter implements Player {
    private final ScoreBean scoreBean;
    private final TrickBean trickBean;
    private final HandBean handBean;
    private final ArrayBlockingQueue<Card> cardArrayBlockingQueue;

    /**
     * The constructor of the class GraphicalPlayerAdapter. It creates a new empty ScoreBean,
     * TrickBean and HandBean and initialises the ArrayBlockingQueue of length 1.
     */
    public GraphicalPlayerAdapter() {
        scoreBean = new ScoreBean();
        trickBean = new TrickBean();
        handBean = new HandBean();
        cardArrayBlockingQueue = new ArrayBlockingQueue<>(1);
    }

    /*
     * All the following methods are overrides of the interface player. They use the
     * runLater method to wait for the GraphicalPlayer to put the card in the
     * ArrayBlockingQueue before executing.
     */

    @Override
    public Card cardToPlay(TurnState state, CardSet hand) {
        runLater(() -> handBean.setPlayableCards(state.trick().playableCards(hand)));
        try {
            Card c = cardArrayBlockingQueue.take();
            handBean.setPlayableCards(CardSet.EMPTY);
            return c;
        } catch (InterruptedException e) {
            throw new Error(e);
        }
    }

    @Override
    public void setPlayers(PlayerId ownId, Map<PlayerId, String> playerNames) {
        GraphicalPlayer graphicalPlayer = new GraphicalPlayer(ownId, playerNames, scoreBean,
                trickBean, handBean, cardArrayBlockingQueue);
        runLater(() -> graphicalPlayer.createStage().show());
    }

    @Override
    public void updateHand(CardSet newHand) {
        runLater(() -> handBean.setHand(newHand));
    }

    @Override
    public void setTrump(Card.Color trump) {
        runLater(() -> trickBean.setTrump(trump));
    }

    @Override
    public void updateTrick(Trick newTrick) {
        runLater(() -> trickBean.setTrick(newTrick));
    }

    @Override
    public void updateScore(Score score) {
        runLater(() -> {
            for(TeamId id : TeamId.ALL) {
                scoreBean.setTurnPoints(id, score.turnPoints(id));
                scoreBean.setGamePoints(id, score.gamePoints(id));
                scoreBean.setTotalPoints(id, score.totalPoints(id));
            }
        });
    }

    @Override
    public void setWinningTeam(TeamId winningTeam) {
        runLater(() -> scoreBean.setWinningTeam(winningTeam));
    }
}
