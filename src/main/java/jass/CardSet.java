package main.java.jass;

import main.java.Preconditions;

import java.util.*;

/**
 * A set of card
 *
 * @author Kilian Schneiter (287496)
 * @author Capucine Berger(269503)
 */

public final class CardSet {
    public static final CardSet EMPTY = new CardSet(0L);
    public static final CardSet ALL_CARDS =
            new CardSet(0b0000000111111111_0000000111111111_0000000111111111_0000000111111111L);
    private final long pkCardSet;

    /*
     * Private builder of the class
     *
     * @param pkCardSet the set of packed cards
     */
    private CardSet(long pkCardSet) {
        this.pkCardSet = pkCardSet;
    }

    /**
     * Given a List<Cards> creates a new CardSet
     *
     * @param cards a list of Cards
     * @return a CardSet containing all the Cards in the list
     */
    public static CardSet of(List<Card> cards) {
        long packed = 0;
        for(Card c : cards) {
            packed += PackedCardSet.singleton(c.packed());
        }

        return new CardSet(packed);
    }

    /**
     * Given a packedCardSet creates a new CardSet
     *
     * @param packed a PackedCardSet
     * @return a new CardSet with the parameter packed as PackedCardSet
     * @throws IllegalArgumentException if packed is not valid
     */
    public static CardSet ofPacked(long packed) {
        Preconditions.checkArgument(PackedCardSet.isValid(packed));
        return new CardSet(packed);
    }

    /**
     * @return packed version of a set of cards
     */
    public long packed() {
        return pkCardSet;
    }

    /**
     * Check if the CardSet if empty
     *
     * @return true if the CardSet is empty
     */
    public boolean isEmpty() {
        return PackedCardSet.isEmpty(pkCardSet);
    }

    /**
     * Gives the size of @this CardSet
     *
     * @return the size of the CardSet
     */
    public int size() {
        return PackedCardSet.size(pkCardSet);
    }

    /**
     * Return the card at the given index in the CardSet
     *
     * @param index the index of the card to get
     * @return a Card, the card at the given index
     */
    public Card get(int index) {
        return Card.ofPacked(PackedCardSet.get(pkCardSet, index));
    }

    /**
     * Adds a card into the CardSet
     *
     * @param card a Card to add
     * @return a new CardSEt containing the given Card
     */
    public CardSet add(Card card) {
        return new CardSet(PackedCardSet.add(pkCardSet, card.packed()));
    }

    /**
     * Removes a card from the CardSet
     *
     * @param card the Card to remove from the CardSet
     * @return a new CardSet without the given Card
     */
    public CardSet remove(Card card) {
        return new CardSet(PackedCardSet.remove(pkCardSet, card.packed()));
    }

    /**
     * Check if the CardSet contains the Card
     *
     * @param card a Card
     * @return true if the Card is in the CardSet
     */
    public boolean contains(Card card) {
        return PackedCardSet.contains(pkCardSet, card.packed());
    }

    /**
     * Gives a new CardSet with all the cards that weren't in @this CardSet
     *
     * @return a new CardSet, the complement of the CardSet
     */
    public CardSet complement() {
        return new CardSet(PackedCardSet.complement(pkCardSet));
    }

    /**
     * Computes the union of @this CardSet and @that CardSet
     *
     * @param that the other CardSet
     * @return a new CardSet containing the cards that in either one of the CardSet
     */
    public CardSet union(CardSet that) {
        return new CardSet(PackedCardSet.union(pkCardSet, that.packed()));
    }

    /**
     * Computes the intersection of @this CardSet with @that CardSet
     *
     * @param that the other CardSet
     * @return a new CardSet, containing only the cards that are in both CardSet
     */
    public CardSet intersection(CardSet that) {
        return new CardSet(PackedCardSet.intersection(pkCardSet, that.packed()));
    }

    /**
     * Computes the difference of @this CardSet with @that CardSet
     *
     * @param that the other CardSet
     * @return a new CardSet without all the cards that were in the other CardSet
     */
    public CardSet difference(CardSet that) {
        return new CardSet(PackedCardSet.difference(pkCardSet, that.packed()));
    }

    /**
     * Gives all the cards in the given colour
     *
     * @param color the colour of the subset
     * @return a CardSet containing all the cards of a given colour
     */
    public CardSet subsetOfColor(Card.Color color) {
        return new CardSet(PackedCardSet.subsetOfColor(pkCardSet, color));
    }

    @Override
    public String toString() {
        return PackedCardSet.toString(pkCardSet);
    }

    @Override
    public boolean equals(Object thatO) {
        return thatO instanceof CardSet && ((CardSet) thatO).packed() == pkCardSet;
    }

    @Override
    public int hashCode() {
        return Long.hashCode(pkCardSet);
    }


}
