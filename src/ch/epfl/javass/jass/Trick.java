package ch.epfl.javass.jass;

import ch.epfl.javass.Preconditions;

/**
 * Tricks of the game
 *
 * @author Kilian Schneiter (287496)
 * @author Capucine Berger(269503)
 */

public final class Trick {
    public static final Trick INVALID = new Trick(PackedTrick.INVALID);
    private final int pkTrick;

    private Trick(int pkTrick) {
        this.pkTrick = pkTrick;
    }

    /**
     * Creates a new empty trick with the given first player and trump
     *
     * @param trump the color of the trump of the trick
     * @param firstPlayer the PlayedId of the first player of the trick
     * @return an empty trick, with firstPlayer as first player and trump as trump color
     */
    public static Trick firstEmpty(Card.Color trump, PlayerId firstPlayer) {
        return new Trick(PackedTrick.firstEmpty(trump, firstPlayer));
    }

    /**
     * @param packed a packed trick
     * @return a new Trick representing the packed trick given in argument
     * @throws IllegalArgumentException if packed is not valid
     */
    public static Trick ofPacked(int packed) {
        Preconditions.checkArgument(PackedTrick.isValid(packed));

        return new Trick(packed);
    }

    /**
     * @return the packed version of the Trick
     */
    public int packed() {
        return pkTrick;
    }

    /**
     * Creates a new trick which empties all cards and add 1 to the index, and the winning player of the last trick begins it
     *
     * @return a new trick, that is updated
     * @throws IllegalStateException if the trick isn't full
     */
    public Trick nextEmpty() {
        if(!isFull())
            throw new IllegalStateException();
        else
            return new Trick(PackedTrick.nextEmpty(pkTrick));
    }

    /**
     * @return true if the trick is empty, and false otherwise
     */
    public boolean isEmpty() {
        return PackedTrick.isEmpty(pkTrick);
    }

    /**
     * @return true if the trick is full, and false otherwise
     */
    public boolean isFull() {
        return PackedTrick.isFull(pkTrick);
    }

    /**
     * @return true if the trick is the last one of the turn, and false otherwise
     */
    public boolean isLast() {
        return PackedTrick.isLast(pkTrick);
    }

    /**
     * @return the number of cards in the trick
     */
    public int size() {
        return PackedTrick.size(pkTrick);
    }

    /**
     * @return the trump color of the trick
     */
    public Card.Color trump() {
        return PackedTrick.trump(pkTrick);
    }

    /**
     * @return the index of the trick in the current turn
     */
    public int index() {
        return PackedTrick.index(pkTrick);
    }

    /**
     * Returns the player playing at an index in the trick
     *
     * @param index the index of the player we want to know the identity
     * @return the PlayerId of the player at the given index
     * @throws IndexOutOfBoundsException if the index is not in the interval [0, 4[
     */
    public PlayerId player(int index) {
        if (!(index >= 0 && index < PlayerId.COUNT))
            throw new IndexOutOfBoundsException();
        else
            return PackedTrick.player(pkTrick, index);
    }

    /**
     * Returns a card at an index in the trick
     *
     * @param index the index of the card
     * @return the Card at the given index in the trick
     * @throws IndexOutOfBoundsException if the index is not between 0 (included) and the number of cards in the current trick (excluded)
     */
    public Card card(int index) {
        if (!(index >= 0 && index < size()))
            throw new IndexOutOfBoundsException();
        else
            return Card.ofPacked(PackedTrick.card(pkTrick, index));
    }

    /**
     * Creates a new Trick containing the Card that we want to add
     *
     * @param c the card to be added in the trick
     * @return a new trick, with the card added
     * @throws IllegalArgumentException if the trick is full
     */
    public Trick withAddedCard(Card c) {
        Preconditions.checkArgument(!isFull());

        return new Trick(PackedTrick.withAddedCard(pkTrick, c.packed()));
    }

    /**
     * @return the color of the first card played in the trick
     * @throws IllegalStateException if the trick is empty
     */
    public Card.Color baseColor() {
        if (isEmpty())
            throw new IllegalStateException();
        else
            return PackedTrick.baseColor(pkTrick);
    }

    /**
     * Returns all the cards of the hand of a player that can be player
     *
     * @param hand a CardSet containing the hand of the player
     * @return a CardSet containing all the cards that are playable given the state of the trick
     * @throws IllegalStateException if the trick is full
     */
    public CardSet playableCards(CardSet hand) {
        if (isFull())
            throw new IllegalStateException();
        else
            return CardSet.ofPacked(PackedTrick.playableCards(pkTrick, hand.packed()));
    }

    /**
     * @return the points that can be won in the trick
     */
    public int points() {
        return PackedTrick.points(pkTrick);
    }

    /**
     * @return the PlayedId of the current winning player of the trick
     * @throws IllegalStateException if the trick is empty
     */
    public PlayerId winningPlayer() {
        if (isEmpty())
            throw new IllegalStateException();
        else
            return PackedTrick.winningPlayer(pkTrick);
    }

    @Override
    public boolean equals(Object that0) {
        return (that0 instanceof Trick) && (((Trick)that0).packed() == pkTrick);
    }

    @Override
    public int hashCode() {
        return pkTrick;
    }

    @Override
    public String toString() {
        return PackedTrick.toString(pkTrick);
    }
}
