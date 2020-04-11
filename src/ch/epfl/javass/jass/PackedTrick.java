package ch.epfl.javass.jass;


import ch.epfl.javass.bits.Bits32;

import java.util.StringJoiner;

/**
 * A trick in packed format
 *
 * @author Kilian Schneiter (287496)
 * @author Capucine Berger(269503)
 */

public final class PackedTrick {

    public static final int INVALID = 0b11111111_11111111_11111111_11111111 ;
    public static final int EMPTY_CARD_TRICK = 0b111111_111111_111111_111111;

    private static final int MIN_INDEX = 0;
    private static final int MAX_INDEX = 8;
    private static final int MAX_CARDS_IN_TRICK = 4;
    private static final int SUBSET_OF_A_COLOR = 16;

    private static final int CARD_SIZE = 6;
    private static final int FIRST_CARD_START = 0;
    private static final int FIRST_PLAYER_SIZE = 2;
    private static final int FIRST_PLAYER_START = 28;
    private static final int INDEX_SIZE = 4;
    private static final int INDEX_START = 24;
    private static final int TRUMP_SIZE = 2;
    private static final int TRUMP_START = 30;

    /**
     * Checks if the packed trick of 32 bits is valid : the index (bit 24 to 27) must be between 0 and 8 included,
     * and all the cards (packs of 6 bits from bit 0 to 23) must be valid or invalid in a decreasing order of index.
     *
     * @param pkTrick an int representing a trick
     * @return a boolean
     */
    public static boolean isValid(int pkTrick) {
        int numberOfValidCards = 0;

        if(!(index(pkTrick) >= MIN_INDEX && index(pkTrick) <= MAX_INDEX))
            return false;

        for(int i = MAX_CARDS_IN_TRICK - 1; i >= 0; i--) {
            if(PackedCard.isValid(Bits32.extract(pkTrick, i * CARD_SIZE, CARD_SIZE)))
                numberOfValidCards++;
        }

        if(numberOfValidCards == MAX_CARDS_IN_TRICK)
            return true;
        else {
            int a = 0;
            for (int j = MAX_CARDS_IN_TRICK - 1; j >= numberOfValidCards; j--) {
                if ((Bits32.extract(pkTrick, j * CARD_SIZE, CARD_SIZE)) == PackedCard.INVALID)
                    a++;
            }

            return a == MAX_CARDS_IN_TRICK - numberOfValidCards;
        }
    }

    /**
     * Empties all cards and returns the trick with the given player and colour of the trump
     *
     * @param trump color of the trump
     * @param firstPlayer first player to play
     * @return a packed trick
     */
    public static int firstEmpty(Card.Color trump, PlayerId firstPlayer) {
        return (((trump.ordinal() << TRUMP_SIZE)
                + firstPlayer.ordinal()) << FIRST_PLAYER_START)
                | EMPTY_CARD_TRICK;
    }

    /**
     * Empties all cards and add 1 to the index, and the winning player of the last trick begins the new one
     *
     * @param pkTrick the last packed trick
     * @return an updated packed trick
     */
    public static int nextEmpty(int pkTrick) {
        assert isValid(pkTrick);

        if(isLast(pkTrick))
            return PackedTrick.INVALID;
        else {
            return ((((pkTrick >>> TRUMP_START) << FIRST_PLAYER_SIZE)
                    + player(pkTrick, indexOfWinningCard(pkTrick)).ordinal() << INDEX_SIZE)
                    + (index(pkTrick) + 1)) << INDEX_START
                    | EMPTY_CARD_TRICK;
        }
    }

    /**
     * @param pkTrick the pack trick
     * @return true if and only if the index of the trick is 8 (i.e the last one)
     */
    public static boolean isLast(int pkTrick) {
        assert isValid(pkTrick);

        return index(pkTrick) == MAX_INDEX;
    }

    /**
     * @param pkTrick a packed trick
     * @return true if the trick doesn't contain any cards
     */
    public static boolean isEmpty(int pkTrick) {
        assert isValid(pkTrick);

        return Bits32.extract(pkTrick, FIRST_CARD_START, CARD_SIZE) == PackedCard.INVALID;
    }

    /**
     * @param pkTrick a packed trick
     * @return true if the packed trick is full (all four cards have been played)
     */
    public static boolean isFull(int pkTrick) {
        assert isValid(pkTrick);

        return card(pkTrick, MAX_CARDS_IN_TRICK - 1) != PackedCard.INVALID;
    }

    /**
     * @param pkTrick packed trick
     * @return the number of cards that have been played in the current trick
     */
    public static int size(int pkTrick) {
        assert isValid(pkTrick);

        int size = 0;
        for(int i = 0; i < MAX_CARDS_IN_TRICK; i++) {
            if(PackedCard.isValid(card(pkTrick, i)))
                size++;
        }

        return size;
    }

    /**
     * @param pkTrick a packed trick
     * @return the colour of the trump of the current trick
     */
    public static Card.Color trump(int pkTrick) {
        assert isValid(pkTrick);

        return Card.Color.ALL.get(pkTrick >>> TRUMP_START);
    }

    /**
     * @param pkTrick a packed trick
     * @param index the position of the player in the trick
     * @return the playerId of the player at index index in the trick
     */
    public static PlayerId player(int pkTrick, int index) {
        assert isValid(pkTrick);

        return PlayerId.ALL.get((Bits32.extract(pkTrick, FIRST_PLAYER_START, FIRST_PLAYER_SIZE) + index) % PlayerId.COUNT);
    }

    /**
     * @param pkTrick a packed trick
     * @return the index of the trick
     */
    public static int index(int pkTrick) {
        //assert isValid(pkTrick);

        return Bits32.extract(pkTrick, INDEX_START, INDEX_SIZE);
    }

    /**
     * @param pkTrick a packed trick
     * @param index an index
     * @return the card at the given index
     */
    public static int card (int pkTrick, int index) {
        assert isValid(pkTrick);

        return Bits32.extract(pkTrick, index * CARD_SIZE, CARD_SIZE);
    }

    /**
     * @param pkTrick a packed trick
     * @param pkCard a packed card
     * @return the packed trick with the added card
     */
    public static int withAddedCard(int pkTrick, int pkCard) {
        assert isValid(pkTrick);
        assert PackedCard.isValid(pkCard);

        int numberOfInvalidCards = 0;
        for(int i = 0; i < MAX_CARDS_IN_TRICK; i++) {
            if (card(pkTrick, i) == PackedCard.INVALID)
                numberOfInvalidCards ++;
        }
        int complement = Bits32.extract(~(pkCard), FIRST_CARD_START, CARD_SIZE) << ((MAX_CARDS_IN_TRICK - numberOfInvalidCards) * CARD_SIZE);

        return pkTrick ^ complement;
    }

    /**
     * @param pkTrick a packed trick
     * @return the colour of the first played card of the trick
     */
    public static Card.Color baseColor(int pkTrick) {
        assert isValid(pkTrick);

        return PackedCard.color(card(pkTrick, FIRST_CARD_START));
    }

    /**
     * Returns all the cards that are playable, given a trick and a hand. All the numbers in comments are representing the different
     * cases
     *
     * @param pkTrick a packed trick
     * @param pkHand a packed set of cards, the cards in the hand of the player
     * @return a PackedCardSet containing the playable cards
     */
    public static long playableCards(int pkTrick, long pkHand) {
        assert isValid(pkTrick);
        assert PackedCardSet.isValid(pkHand);

        Card.Color trumpColor = trump(pkTrick);
        long subsetOfTrumpColor = PackedCardSet.subsetOfColor(pkHand, trumpColor);
        int winningCardOfTheTrick = card(pkTrick, indexOfWinningCard(pkTrick));
        //First card of the trick
        if (isEmpty(pkTrick)) {
            return pkHand;
        } else {
            long subsetOfTheRightColor = PackedCardSet.subsetOfColor(pkHand, PackedCard.color(card(pkTrick, FIRST_CARD_START)));
            //Can't follow on the first card
            if (PackedCardSet.isEmpty(subsetOfTheRightColor)) {
                //No trump in the hand (8), (9)
                if (PackedCardSet.isEmpty(subsetOfTrumpColor))
                    return pkHand;
                else {
                    long subsetOfTrumpAbove = PackedCardSet.intersection(subsetOfTrumpColor, PackedCardSet.trumpAbove(winningCardOfTheTrick));
                    //A trump has already been played
                    if (Card.ofPacked(winningCardOfTheTrick).color() == trumpColor) {
                        //Hand contains trumpAbove
                        if (setContainsCardAbove(subsetOfTrumpColor, PackedCardSet.trumpAbove(winningCardOfTheTrick))) {
                            //Hand contains other colours (10)
                            if (!PackedCardSet.isEmpty(PackedCardSet.difference(pkHand, subsetOfTrumpColor)))
                                return PackedCardSet.union(subsetOfTrumpAbove, PackedCardSet.difference(pkHand, subsetOfTrumpColor));
                                //(11)
                            else
                                return subsetOfTrumpAbove;
                        } else {
                            //(12)
                            if (!PackedCardSet.isEmpty(PackedCardSet.difference(pkHand, subsetOfTrumpColor)))
                                return PackedCardSet.difference(pkHand, subsetOfTrumpColor);
                                //(13)
                            else
                                return subsetOfTrumpColor;
                        }
                        //(14)
                    } else
                        return pkHand;
                }

            } else {
                long subsetOfTrumpAbove = PackedCardSet.intersection(subsetOfTrumpColor, PackedCardSet.trumpAbove(winningCardOfTheTrick));
                //To check if base colour is trumpColor and if only remaining card in trump is JACK (4)
                if (subsetOfTheRightColor == subsetOfTrumpColor) {
                    if (subsetOfTrumpColor == 1 << (trumpColor.ordinal() * SUBSET_OF_A_COLOR + Card.Rank.JACK.ordinal()))
                        return pkHand;
                    //Trump has already been played
                }
                if (Card.ofPacked(winningCardOfTheTrick).color() == trumpColor) {
                    //(3), (6)
                    if (setContainsCardAbove(subsetOfTrumpColor, PackedCardSet.trumpAbove(winningCardOfTheTrick)))
                        return PackedCardSet.union(subsetOfTrumpAbove, subsetOfTheRightColor);
                        //(2), (5)
                    else
                        return subsetOfTheRightColor;
                    //No trump has been played yet (7)
                } else
                    return PackedCardSet.union(subsetOfTrumpColor, subsetOfTheRightColor);
            }
        }
    }

    /**
     * @param pkTrick a packed trick
     * @return the number of points in the trick
     */
    public static int points(int pkTrick) {
        assert isValid(pkTrick);

        int points = 0;

        for (int i = 0; i < MAX_CARDS_IN_TRICK; i++) {
            Card card = Card.ofPacked(card(pkTrick, i));
            points += card.points(trump(pkTrick));
        }

        if (isLast(pkTrick)) {
            points += Jass.LAST_TRICK_ADDITIONAL_POINTS;
        }

        return points;
    }

    /**
     * @param pkTrick a packed trick
     * @return the playedId of the winning player
     */
    public static PlayerId winningPlayer(int pkTrick) {
        assert isValid(pkTrick);

        return player(pkTrick, indexOfWinningCard(pkTrick));
    }

    /**
     * @param pkTrick a packed trick
     * @return a string containing the colour and rank of all cards in the trick
     */
    public static String toString(int pkTrick) {
        assert isValid(pkTrick);

        StringJoiner stringJoiner = new StringJoiner(",", "{", "}");

        for (int i = 0; i < size(pkTrick); i++) {
            Card cardInTheTrick = Card.ofPacked(card(pkTrick, i));
            stringJoiner.add(cardInTheTrick.toString());
        }

        return stringJoiner.toString();
    }

    private static boolean setContainsCardAbove(long pkCardSet1, long pkCardSet2) {
        assert PackedCardSet.isValid(pkCardSet1);
        assert PackedCardSet.isValid(pkCardSet2);

        int numberOfCardsAbove = 0;
        for(int i = 0; i < PackedCardSet.size(pkCardSet2); i++) {
            if(PackedCardSet.contains(pkCardSet1, PackedCardSet.get(pkCardSet2, i)))
                numberOfCardsAbove ++;
        }

        return numberOfCardsAbove >= 1;
    }

    private static int indexOfWinningCard(int pkTrick) {
        assert isValid(pkTrick);

        int winningCard = card(pkTrick, 0);
        int index = 0;

        for (int i = 1; i < size(pkTrick); i++) {
            if (PackedCard.isBetter(Card.Color.ALL.get((trump(pkTrick)).ordinal()), card(pkTrick, i), winningCard)) {
                winningCard = card(pkTrick, i);
                index = i;
            }
        }

        return index;
    }
}
