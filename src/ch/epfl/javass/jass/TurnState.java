package ch.epfl.javass.jass;

import ch.epfl.javass.Preconditions;

import static ch.epfl.javass.jass.PackedTrick.INVALID;
import static ch.epfl.javass.jass.PackedTrick.card;

/**
 * The state of a turn
 *
 * @author Kilian Schneiter (287496)
 * @author Capucine Berger(269503)
 */
public final class TurnState {
    private final long pkScore;
    private final int pkTrick;
    private final long pkUnplayedCards;

    private final static int COLOUR_SIZE = 2;
    private final static int FIRST_PLAYER_START = 28;
    private final static int MAX_CARDS_IN_A_TRICK = 4;


    private TurnState(long pkScore, int pkTrick, long pkUnplayedCards) {
        this.pkScore = pkScore;
        this.pkTrick = pkTrick;
        this.pkUnplayedCards = pkUnplayedCards;
    }

    /**
     * Initialise a new TurnState with the given argument
     *
     * @param trump the color of the trump
     * @param score the initial score
     * @param firstPlayer the first player to play
     * @return the initial state of a new turn
     */
    public static TurnState initial(Card.Color trump, Score score, PlayerId firstPlayer) {
        int packedTrick = (((trump.ordinal() << COLOUR_SIZE) + firstPlayer.ordinal()) << FIRST_PLAYER_START | PackedTrick.EMPTY_CARD_TRICK);
        return new TurnState(score.packed(),packedTrick , PackedCardSet.ALL_CARDS);
    }

    /**
     * @param pkScore a packed score
     * @param pkUnplayedCards a packed card set of all the cards that haven't been played yet
     * @param pkTrick a packed trick
     * @return a new TurnState with all the given arguments
     */
    public static TurnState ofPackedComponents(long pkScore, long pkUnplayedCards, int pkTrick) {
        Preconditions.checkArgument(PackedScore.isValid(pkScore));
        Preconditions.checkArgument(PackedCardSet.isValid(pkUnplayedCards));
        Preconditions.checkArgument(PackedTrick.isValid(pkTrick));

        return new TurnState(pkScore, pkTrick, pkUnplayedCards);
    }

    /**
     * @return the packed score of the TurnState
     */
    public long packedScore() {
        return pkScore;
    }

    /**
     * @return the packed card set of the unplayed cards of the TurnState
     */
    public long packedUnplayedCards() {
        return pkUnplayedCards;
    }

    /**
     * @return the packed trick of the TurnState
     */
    public int packedTrick() {
        return pkTrick;
    }

    /**
     * @return a Score built by the packed score of the TurnState
     */
    public Score score() {
        return Score.ofPacked(pkScore);
    }

    /**
     * @return a CardSet built by the packed unPlayedCardSet of the TurnState
     */
    public CardSet unplayedCards() {
        return CardSet.ofPacked(pkUnplayedCards);
    }

    /**
     * @return a Trick built by the packed trick of the TurnState
     */
    public Trick trick() {
        return Trick.ofPacked(pkTrick);
    }

    /**
     * @return true if it is the last trick of the turn, false otherwise
     */
    public boolean isTerminal() {
        return pkTrick == INVALID;
    }

    /**
     * @return the identity of the next player to play
     * @throws IllegalStateException if the trick is full
     */
    public PlayerId nextPlayer() {
        if (PackedTrick.isFull(pkTrick))
            throw new IllegalStateException();
        else
            return PlayerId.ALL.get((PackedTrick.player(pkTrick, 0).ordinal() + trick().size()) % PlayerId.COUNT);
    }

    /**
     * Add a card to the trick, and update the TurnState
     *
     * @param card the card to be added
     * @return an updated TurnState
     * @throws IllegalStateException if the trick is already full
     */
    public TurnState withNewCardPlayed(Card card) {
        if (PackedTrick.isFull(pkTrick))
            throw new IllegalStateException();
        else {
            return new TurnState(pkScore, trick().withAddedCard(card).packed(),
                    PackedCardSet.difference(pkUnplayedCards, PackedCardSet.singleton(card.packed())));
        }
    }

    /**
     * Collects the trick, and then updates the points and reset the trick
     *
     * @return an updated TurnState
     * @throws IllegalStateException if the trick is not full
     */
    public TurnState withTrickCollected() {
        if (!PackedTrick.isFull(pkTrick))
            throw new IllegalStateException();
        else {
            long packedScore = score().withAdditionalTrick(TeamId.ALL.get(trick().winningPlayer().ordinal() % TeamId.COUNT),
                    trick().points()).packed();
            int packedTrick = PackedTrick.nextEmpty(pkTrick);
            long packedCardSet = PackedCardSet.difference(pkUnplayedCards, cardsPlayed(pkTrick).packed());
            return new TurnState(packedScore,packedTrick ,packedCardSet);
        }
    }

    /**
     * Does the same thing as above, but check if the card to be added is the last one of the trick.
     * If so, the trick is collected, the points are updated and the trick is reset
     *
     * @param card the card to be added
     * @return an updated TurnState
     * @throws IllegalStateException if the trick is full
     */
    public TurnState withNewCardPlayedAndTrickCollected(Card card) {
        if (PackedTrick.isFull(pkTrick))
            throw new IllegalStateException();
        else {
            TurnState result = withNewCardPlayed(card);
            if (PackedTrick.size(pkTrick) == MAX_CARDS_IN_A_TRICK - 1) {
                return result.withTrickCollected();
            } else
                return result;
        }
    }

    /*
     * Return the CardSet of all the cards played during the trick
     * @param pkTrick a packed trick
     * @return a CardSet of all cards played during the trick
     */
    private static CardSet cardsPlayed(int pkTrick) {
        assert PackedTrick.isValid(pkTrick);

        long playedCardsDuringTheTrick = 0;
        for (int i = 0; i < PackedTrick.size(pkTrick); i++) {
            playedCardsDuringTheTrick = PackedCardSet.add(playedCardsDuringTheTrick, card(pkTrick, i));
        }

        return CardSet.ofPacked(playedCardsDuringTheTrick);
    }

    @Override
    public String toString() {
        String s = new StringBuilder()
                .append("Score: ").append(score())
                .append(" Trump colour: ").append(trick().trump())
                .append(" First player: ").append(trick().player(0))
                .append(" Index: ").append(trick().index())
                .append(" Cards: ").append(trick())
                .toString();
        return  s;
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof TurnState
                && score().equals(o)
                && trick().equals(o)
                && unplayedCards().equals(o);
    }
}
