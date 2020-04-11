package ch.epfl.javass.jass;
import java.util.StringJoiner;

/**
 * A CardSet in packed format
 *
 * @author Kilian Schneiter (287496)
 * @author Capucine Berger(269503)
 */

public final class PackedCardSet {

    public final static long EMPTY = 0L;
    public final static long ALL_CARDS = 0b0000000111111111_0000000111111111_0000000111111111_0000000111111111L;
    private final static int SIZE_SUBSET_OF_COLOUR = 16;

    private final static long[] trumpCardsHierarchy = {0b111111110L, 0b111111100L, 0b111111000L, 0b000100000L, 0b111101000L, EMPTY, 0b110101000L, 0b100101000L, 0b000101000L,
            0b111111110L << 16, 0b111111100L << 16, 0b111111000L << 16, 0b000100000L << 16, 0b111101000L << 16, EMPTY, 0b110101000L << 16, 0b100101000L << 16, 0b000101000L << 16,
            0b111111110L << 32, 0b111111100L << 32, 0b111111000L << 32, 0b000100000L << 32, 0b111101000L << 32, EMPTY, 0b110101000L << 32, 0b100101000L << 32, 0b000101000L << 32,
            0b111111110L << 48, 0b111111100L << 48, 0b111111000L << 48, 0b000100000L << 48, 0b111101000L << 48, EMPTY, 0b110101000L << 48, 0b100101000L << 48, 0b000101000L << 48};

    private static final long allCardsOfSameColor = 0b111111111L;

    private PackedCardSet() {}

    /**
     * Check if the given packed CardSet is valid
     *
     * @param pkCardSet the set of packed card
     * @return true if none of the 28 unused bits are ones
     */
    public static boolean isValid(long pkCardSet) {
        return (ALL_CARDS | pkCardSet) == ALL_CARDS;
    }

    /**
     * Given a trump card the method returns all the cards that are better
     *
     * @param pkCard a packed card
     * @return long containing all the cards that are better than the given trump card
     */
    public static long trumpAbove(int pkCard) {
        assert PackedCard.isValid(pkCard);
        return trumpCardsHierarchy[PackedCard.color(pkCard).ordinal() * Card.Rank.COUNT
                + PackedCard.rank(pkCard).ordinal()];
    }

    /**
     * Given a packed card, the method returns a set of card consisting only of the packed card
     *
     * @param pkCard a packed card
     * @return long containing a single card
     */
    public static long singleton(int pkCard) {
        assert PackedCard.isValid(pkCard);
        return (EMPTY + 1 << (PackedCard.color(pkCard).ordinal() * SIZE_SUBSET_OF_COLOUR + PackedCard.rank(pkCard).ordinal()));
    }

    /**
     * Check if the set of packed card is empty
     *
     * @param pkCardSet the set of packed Card
     * @return true if it is empty
     */
    public static boolean isEmpty(long pkCardSet) {
        return pkCardSet == EMPTY;
    }

    /**
     * Returns the number of cards of the set of card
     *
     * @param pkCardSet a set of packed cards
     * @return int, gives the number of cards in the set
     */
    public static int size(long pkCardSet) {
        return Long.bitCount(pkCardSet);
    }

    /**
     * Given a set of packed card and an index, returns the cards 'index' 1 contained in the set
     *
     * @param pkCardSet the set of packed cards
     * @param index the index of the cards, where index 0 is the least significant bit equal to 1
     * @return a packed card
     */
    public static int get(long pkCardSet, int index) {
        assert isValid(pkCardSet);

        long copy = pkCardSet;
        long oneBit = 0;

        for(int i = 0; i < index; i++) {
            oneBit = Long.lowestOneBit(copy);
            copy = copy - oneBit;
        }

        int position = Long.numberOfTrailingZeros(copy);
        int rank = position % SIZE_SUBSET_OF_COLOUR;
        return PackedCard.pack(Card.Color.ALL.get((position - (rank)) / SIZE_SUBSET_OF_COLOUR), Card.Rank.ALL.get(rank));
    }

    /**
     * Adds a card to the set of packed cards
     *
     * @param pkCardSet a set of packed cards
     * @param pkCard the card to add
     * @return long, the set of packed cards
     */
    public static long add(long pkCardSet, int pkCard) {
        assert isValid(pkCardSet);
        assert PackedCard.isValid(pkCard);

        return pkCardSet | singleton(pkCard);
    }

    /**
     * Removes a card from a set of card
     *
     * @param pkCardSet the set of cards from which a card is removed
     * @param pkCard the card to remove
     * @return long, the set of cards without the packed card
     */
    public static long remove(long pkCardSet, int pkCard) {
        assert isValid(pkCardSet);
        assert PackedCard.isValid(pkCard);
        if(contains(pkCardSet, pkCard)) {
            return pkCardSet ^ singleton(pkCard);
        } else {
            return pkCardSet;
        }
    }

    /**
     * Check if the packed set of card contains the given card
     *
     * @param pkCarSet the set of cards
     * @param pkCard a packed card
     * @return true if the packed card is in the set of packed cards
     */
    public static boolean contains (long pkCarSet, int pkCard) {
        return Long.bitCount(singleton(pkCard) & pkCarSet) == 1 ;
    }

    /**
     * Gives the complement of the set of packed card
     *
     * @param pkCardCardSet the set of packed cards
     * @return the complement of the set of packed cards
     */
    public static long complement (long pkCardCardSet) {
        return ALL_CARDS ^ pkCardCardSet;
    }

    /**
     * Computes the union of the two set of packed cards
     *
     * @param pkCardSet1 a set of packed cards
     * @param pkCardSet2 a set of packed cards
     * @return long, the union of the two sets of cards
     */
    public static long union (long pkCardSet1, long pkCardSet2) {
        return  pkCardSet1 | pkCardSet2;
    }

    /**
     * Computes the intersection of the two set of packed cards
     *
     * @param pkCardSet1 a set of packed cards
     * @param pkCardSet2 a set of packed cards
     * @return long, the intersection of the two sets of cards
     */
    public static long intersection(long pkCardSet1, long pkCardSet2) {
        return pkCardSet1 & pkCardSet2;
    }

    /**
     * Removes all the cards that are in the second set of packed cards from the first
     *
     * @param pkCardSet1 the set of packed from which are removed the cards of the second
     * @param pkCardSet2 the set of  packed containing all the cards to remove
     * @return long, the first set of packed cards without the second
     */
    public static long difference (long pkCardSet1, long pkCardSet2) {
        return (pkCardSet1 & ~pkCardSet2);
    }

    /**
     * Given a color, returns all the cards of this colour if they are in the set of packed cards
     *
     * @param pkCardSet the set of packed cards
     * @param color the colour to extract
     * @return long, all the cards of the same colour
     */
    public static long subsetOfColor (long pkCardSet, Card.Color color) {
        return pkCardSet & (allCardsOfSameColor << (color.ordinal() * SIZE_SUBSET_OF_COLOUR));
    }

    /**
     * @param pkCardSet a set of packed cards
     * @return String containing the rank and color of every card of the set
     */
    public static String toString(long pkCardSet) {
        StringJoiner stringJoiner = new StringJoiner(",", "{", "}");

        for(int i = 0; i < size(pkCardSet); i++) {
            stringJoiner.add(PackedCard.toString(get(pkCardSet, i)));
        }

        return stringJoiner.toString();
    }

}