package main.java.gui;

import main.java.jass.Card;
import main.java.jass.PlayerId;
import main.java.jass.Trick;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableMap;

/**
 * A Trick represented in a graphical context. It will be used the show the cards, the player names and the trump
 * on screen.
 *
 * @author Kilian Schneiter (287496)
 * @author Capucine Berger(269503)
 */

public final class TrickBean {
    private final ObjectProperty<Card.Color> trump;
    private final MapProperty<PlayerId, Card> trick;
    private final ObjectProperty<PlayerId> winningPlayer;

    /**
     * Creates a new TrickBean and initialises all the properties.
     */
    public TrickBean() {
        trump = new SimpleObjectProperty<>();
        trick = new SimpleMapProperty<>();
        winningPlayer = new SimpleObjectProperty<>();
    }

    /**
     * Getter of the trump color property.
     *
     * @return the property containing the current trump color
     */
    public ReadOnlyObjectProperty<Card.Color> trumpProperty() {
        return trump;
    }

    /**
     * Sets a new value to the property which contains the color of the trump.
     *
     * @param trumpColor the new color of the trump
     */
    public void setTrump(Card.Color trumpColor) {
        trump.set(trumpColor);
    }

    /**
     * Getter of the trick observable map.
     *
     * @return the ObservableMap containing the trick
     */
    public ObservableMap<PlayerId, Card> trick() {
        return FXCollections.unmodifiableObservableMap(trick);
    }

    /**
     * Sets a new value to the property containing the trick, by assigning each card played to the player who
     * played it. In addition, it updates the value of the current winning player of the trick
     *
     * @param newTrick the new Trick that is going to be set
     */
    public void setTrick(Trick newTrick) {
        ObservableMap<PlayerId, Card> trickMap = FXCollections.observableHashMap();

        if (!newTrick.isEmpty()) {
            for (int i = 0; i < newTrick.size(); i++) {
                trickMap.put(newTrick.player(i), newTrick.card(i));
            }
            winningPlayer.set(newTrick.winningPlayer());
        } else {
            winningPlayer.set(null);
        }

        trick.set(FXCollections.unmodifiableObservableMap(trickMap));
    }

    /**
     * Getter of the winning player property.
     *
     * @return the property containing the current winning player
     */
    public ReadOnlyObjectProperty<PlayerId> winningPlayerProperty() {
        return winningPlayer;
    }
}
