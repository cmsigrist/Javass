package main.java.jass;

import main.java.bits.Bits32;

/**
 * Card represented in a packed format
 *
 * @author Kilian Schneiter (287496)
 * @author Capucine Berger(269503)
 */

public final class PackedCard {

    public static int INVALID = 0b111111;

    private static final int COLOUR_SIZE = 2;
    private static final int COLOUR_START = 4;
    private static final int RANK_SIZE = 4;
    private static final int RANK_START = 0;
    private static final int UPPER_BOUND_COLOUR = 3;
    private static final int UPPER_BOUND_RANK = 8;
    private static final int EMPTY_BITS_START = 6;

    private PackedCard() {}

    /**
     * Check whether the packed card is a valid or not (the rank is an int between 0 and 8, the colour between 0 and 3
     * and if the other bits are equal to zero
     *
     * @param pkCard a packed Card
     * @return true if the packed card is valid
     */
    public static boolean isValid(int pkCard) {
        int extractedBitColour = Bits32.extract(pkCard, COLOUR_START, COLOUR_SIZE);
        int extractedBitRank = Bits32.extract(pkCard, RANK_START, RANK_SIZE);

        return extractedBitColour >= 0
                && extractedBitColour <= UPPER_BOUND_COLOUR
                && extractedBitRank >= 0
                && extractedBitRank <= UPPER_BOUND_RANK
                && Bits32.extract(pkCard, EMPTY_BITS_START , Integer.SIZE - EMPTY_BITS_START) == 0;
    }

    /**
     * Packs a Card given its colour and rank
     *
     * @param c the colour of the card
     * @param r the rank of the card
     * @return a packed card
     */
    public static int pack(Card.Color c, Card.Rank r) {
        return Bits32.pack(r.ordinal(), RANK_SIZE, c.ordinal(), COLOUR_SIZE );
    }

    /**
     * Gives the colour of a card
     *
     * @param pkCard a packed Card
     * @return the colour of the card
     */
    public static Card.Color color(int pkCard) {
        assert isValid(pkCard);
        return Card.Color.ALL.get(Bits32.extract(pkCard, COLOUR_START, COLOUR_SIZE));
    }

    /**
     * Gives the rank of a card
     *
     * @param pkCard a packed Card
     * @return the rank of a card
     */

    public static Card.Rank rank(int pkCard) {
        assert isValid(pkCard);
        return Card.Rank.ALL.get(Bits32.extract(pkCard, RANK_START, RANK_SIZE));
    }

    /**
     * Compares two cards if they can be compared
     *
     * @param trump the colour of the trump card
     * @param pkCardL the first card
     * @param pkCardR the second card
     * @return true if the first card is better than the second
     */
    public static boolean isBetter(Card.Color trump, int pkCardL, int pkCardR) {
        assert isValid(pkCardL);
        assert isValid(pkCardR);
        if (color(pkCardL) == color(pkCardR)) {
            if (color(pkCardL) == trump) {
                return rank(pkCardL).trumpOrdinal() > rank(pkCardR).trumpOrdinal();
            } else {
                return rank(pkCardL).ordinal() > rank(pkCardR).ordinal();
            }
        } else {
            return color(pkCardL) == trump;
        }
    }

    /**
     * Gives the point according to the rank and the colour of the card (if it's a trump card or not)
     *
     * @param trump the colour of the trump card
     * @param pkCard a packed Card
     * @return the number of points of the card
     */
    public static int points(Card.Color trump, int pkCard) {
        assert isValid(pkCard);
        switch (rank(pkCard)) {
            case NINE:
                if (color(pkCard) == trump) {
                    return 14;
                } else {
                    return 0;
                }
            case TEN: return 10;
            case JACK:
                if (color(pkCard) == trump) {
                    return 20;
                } else {
                    return 2;
                }
            case QUEEN: return 3;
            case KING: return 4;
            case ACE: return 11;
            default: return 0;
        }
    }

    /**
     * Put into text the colour and the rank of the given card
     *
     * @param pkCard a packed Card
     * @return a String containing the colour and rank of the card
     */
    public static String toString(int pkCard) {
        assert isValid(pkCard);
        return color(pkCard).toString() + rank(pkCard).toString();
    }
}
