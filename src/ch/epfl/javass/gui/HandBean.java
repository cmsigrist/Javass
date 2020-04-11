package ch.epfl.javass.gui;

import ch.epfl.javass.jass.Card;
import ch.epfl.javass.jass.CardSet;
import ch.epfl.javass.jass.Jass;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableSet;

/**
 * A class that represents the hand in a graphical context. It is used to show the hand of the GraphicalPlayer
 * on screen.
 *
 * @author Kilian Schneiter (287496)
 * @author Capucine Berger(269503)
 */

public final class HandBean {
    private final ListProperty<Card> hand;
    private final SetProperty<Card> playableCards;

    /**
     * Creates a new empty HandBean.
     */
    public HandBean() {
        hand = new SimpleListProperty<>();
        playableCards = new SimpleSetProperty<>();
    }

    /**
     * Getter of the hand.
     *
     * @return the observable list containing all the cards of the hand
     */
    public ObservableList<Card> hand() {
        return FXCollections.unmodifiableObservableList(hand);
    }

    /**
     * Sets the hand a new value. If the size of the CardSet newHand is equal to 9, changes all the cards of the hand.
     * If the size of the CardSet newHand is smaller than 9, checks for the card which was removed and sets its
     * value in the list to null. Using this method, we are sure that the set is always of size 9.
     *
     * @param newHand the CardSet containing the new hand
     */
    public void setHand(CardSet newHand) {
        ObservableList<Card> handList = FXCollections.observableArrayList(hand);

        if (newHand.size() == Jass.HAND_SIZE) {
            handList.clear();
            for (int i = 0; i < Jass.HAND_SIZE; i++) {
                handList.add(newHand.get(i));
            }
        } else {
            for (Card card : handList) {
                if (card != null) {
                    if (!newHand.contains(card))
                        handList.set(handList.indexOf(card), null);
                }
            }
        }

        hand.set(FXCollections.unmodifiableObservableList(handList));
    }

    /**
     * Getter of the playable cards.
     *
     * @return the observable set containing the Set of playable cards
     */
    public ObservableSet<Card> playableCards() {
        return FXCollections.unmodifiableObservableSet(playableCards);
    }

    /**
     * Sets playableCard a new set of cards.
     *
     * @param newPlayableCards the CardSet containing the new playable cards
     */
    public void setPlayableCards(CardSet newPlayableCards) {
        ObservableSet<Card> playableCardsList = FXCollections.observableSet();

        for (int i = 0; i < newPlayableCards.size(); i++) {
            playableCardsList.add(newPlayableCards.get(i));
        }

        playableCards.set(FXCollections.unmodifiableObservableSet(playableCardsList));
    }
}
