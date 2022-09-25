package main.java.jass;

import main.java.Preconditions;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * A card of the game
 *
 * @author Kilian Schneiter (287496)
 * @author Capucine Berger(269503)
 */

public final class Card {

    final private int pkCard;

    /**
     * An enum containing all the possible colours a set of card has
     */
    public enum Color {
        SPADE,
        HEART,
        DIAMOND,
        CLUB;

        public final static List<Color> ALL = Collections.unmodifiableList(Arrays.asList(values()));
        public final static int COUNT = 4;

        public String toString() {
            Color c = Card.Color.valueOf(this.name());
            switch (c) {
                case SPADE: return "\u2660";
                case HEART: return "\u2661";
                case DIAMOND: return "\u2662";
                default: return  "\u2663";
            }
        }
    }

    /**
     * An enum containing all the rank of a set of card
     */
    public enum Rank {
        SIX,
        SEVEN,
        EIGHT,
        NINE,
        TEN,
        JACK,
        QUEEN,
        KING,
        ACE;

        public final static List<Rank> ALL =  Collections.unmodifiableList(
                Arrays.asList(values()));
        public final static int COUNT = 9;

        /**
         * Assign the ordinal to all the rank given the fact that they are of trump colour
         * @return the ordinal of the rank
         */
        public int trumpOrdinal() {
            Rank r = Card.Rank.valueOf(this.name());
            switch(r) {
                case SIX: return 0;
                case SEVEN: return 1;
                case EIGHT: return 2;
                case NINE: return 7;
                case TEN: return 3;
                case JACK: return 8;
                case QUEEN: return 4;
                case KING: return 5;
                default: return 6;
            }
        }

        public String toString() {
            Rank r = Card.Rank.valueOf(this.name());
            switch(r) {
                case SIX: return "6";
                case SEVEN: return "7";
                case EIGHT: return "8";
                case NINE: return "9";
                case TEN: return "10";
                case JACK: return "J";
                case QUEEN: return "Q";
                case KING: return "K";
                default: return "A";
            }
        }
    }

    /**
     * Takes a packed card as an argument and assigns it to the class attribute
     */
    private Card(int p) {
         pkCard = p;
    }

    /**
     * Returns a card with its colour and rank
     *
     * @param c the colour f the card
     * @param r the rank of the card
     * @return the Card with its colour and rank in a packed card
     */
    public static Card of(Color c, Rank r) {
        return new Card (PackedCard.pack(c, r));
    }

    /**
     * Returns a card with its colour and rank
     *
     * @param packed the int of a packed card
     * @return the Card with its colour and rank in a packed card
     * @throws IllegalArgumentException if the packed card is not valid
     */
    public static Card ofPacked(int packed) {
        Preconditions.checkArgument(PackedCard.isValid(packed));
        return new Card(packed);
    }

    /**
     * @return the packed form of @this card
     */
    public int packed() {
        return pkCard;
    }

    /**
     * @return the colour of the packed card
     */
    public Color color() {
        return PackedCard.color(pkCard);
    }

    /**
     * @return the rank of the packed card
     */
    public Rank rank() {
        return PackedCard.rank(pkCard);
    }

    /**
     * Compares @this card with @that using the method of PackedCard
     *
     * @param trump the colour of the trump
     * @param that the card to compare
     * @return true if @this is better than @that
     */
    public boolean isBetter(Color trump, Card that) {
        return PackedCard.isBetter(trump, pkCard, that.packed());
    }

    /**
     * Gives the number of points of @this card using the method of PackedCard
     *
     * @param trump the colour of the trump
     * @return the number of points
     */
    public int points(Color trump) {
        return PackedCard.points(trump, pkCard);
    }

    @Override
    public boolean equals(Object thatO){
        return thatO instanceof Card && ((Card) thatO).packed() == pkCard;
    }

    @Override
    public int hashCode() {
        return pkCard;
    }

    @Override
    public String toString() {
       return PackedCard.color(pkCard).toString() + PackedCard.rank(pkCard).toString();
    }
}