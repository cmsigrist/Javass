package ch.epfl.javass.jass;

import java.util.*;
/**
 * A game of Jass
 *
 * @author Kilian Schneiter (287496)
 * @author Capucine Berger(269503)
 */

public final class JassGame {

    private final Random shuffleRng;
    private final Random trumpRng;
    private final Map<PlayerId, Player> players;
    private final Map<PlayerId, String> playerNames;

    private TurnState turnState;
    private Map<PlayerId, CardSet> handOfCard;

    private int turnNumber = 0;
    private PlayerId firstPlayerOfTheGame;

    private final List<Card> DECK;
    private final int NUMBER_OF_CARDS_IN_THE_DECK = 36;
    private final Card SEVEN_OF_DIAMOND = Card.of(Card.Color.DIAMOND, Card.Rank.SEVEN);


    /**
     * Building a new JassGame
     *
     * @param rngSeed the seed of the sequence
     * @param players the map of the players
     * @param playerNames the map of the name of the player
     */
    public JassGame(long rngSeed, Map<PlayerId, Player> players, Map<PlayerId, String> playerNames) {
        Random rng = new Random(rngSeed);
        this.shuffleRng = new Random(rng.nextLong());
        this.trumpRng = new Random(rng.nextLong());
        DECK  = new ArrayList<>();

        for (int i = 0; i < NUMBER_OF_CARDS_IN_THE_DECK; i++) {
            DECK.add(CardSet.ALL_CARDS.get(i));
        }

        this.players = Collections.unmodifiableMap(new EnumMap<>(players));
        this.playerNames = Collections.unmodifiableMap(new EnumMap<>(playerNames));

        turnState = TurnState.ofPackedComponents(0L, PackedCardSet.ALL_CARDS, PackedTrick.EMPTY_CARD_TRICK);
    }

    /**
     * Check if the game is over (one of the team's totalScore is greater than 1000
     *
     * @return true
     */
    public boolean isGameOver() {
        return turnState.score().totalPoints(TeamId.TEAM_1) >= Jass.WINNING_POINTS
                || turnState.score().totalPoints(TeamId.TEAM_2) >= Jass.WINNING_POINTS;
    }

    /**
     * Finishes a trick and sets everything up for the next
     */
    public void advanceToEndOfNextTrick() {
        if(turnState.trick().isFull()) {
            turnState = turnState.withTrickCollected();
        }

        PlayerId firstPlayerOfTheTrick;

        if(turnState.trick().index() == 0) {
            handOfCard = shuffleAndDeal();

            if(turnState.score().equals(Score.INITIAL)) {
                firstPlayerOfTheGame = firstPlayer();
                for(PlayerId p : PlayerId.ALL) {
                    players.get(p).setPlayers(p, playerNames);
                }
            }
            firstPlayerOfTheTrick = PlayerId.ALL.get((firstPlayerOfTheGame.ordinal()
                    + turnNumber) % PlayerId.COUNT);
            turnState = setANewTurn(firstPlayerOfTheTrick);
        } else {
            firstPlayerOfTheTrick = turnState.trick().player(0);
        }

        updateTurnState();
        completeTrick(firstPlayerOfTheTrick);

        if(turnState.trick().isLast()) {
            if(!isGameOver()) {
                turnState = TurnState.initial(turnState.trick().trump(), endOfTurnScore(),
                        turnState.trick().player(0));
            }
            turnState = TurnState.ofPackedComponents(turnState.packedScore(), PackedCardSet.ALL_CARDS,
                    PackedTrick.EMPTY_CARD_TRICK);
            turnNumber++;
        }

        if (turnState.score().totalPoints(TeamId.TEAM_1) >= Jass.WINNING_POINTS) {
            updateScoreForEachPlayer();
            setWinningTeamForEachPlayer(TeamId.TEAM_1);
        } else if (turnState.score().totalPoints(TeamId.TEAM_2) >= Jass.WINNING_POINTS){
            updateScoreForEachPlayer();
            setWinningTeamForEachPlayer(TeamId.TEAM_2);
        }
    }

    /*
     * Shuffles the deck of cards and deal to each player his hand of card
     *
     * @return a map where each PlayerID has a CardSet
     */
    private Map<PlayerId, CardSet> shuffleAndDeal() {
        Map<PlayerId, CardSet> handOfCard = new HashMap<>();
        List<Card> shuffledDeck = new ArrayList<>();
        for (int i = 0; i <NUMBER_OF_CARDS_IN_THE_DECK; i++) {
            shuffledDeck.add(DECK.get(i));
        }

        Collections.shuffle(shuffledDeck, shuffleRng);
        for(int i = 0; i < PlayerId.COUNT; i++) {
            handOfCard.put(PlayerId.ALL.get(i), CardSet.of(shuffledDeck.subList(i * Jass.HAND_SIZE,
                    (i * Jass.HAND_SIZE) + Jass.HAND_SIZE)));
        }
        return handOfCard;
    }

    private Card.Color setTrump() {
        return Card.Color.ALL.get(trumpRng.nextInt(Card.Color.COUNT));
    }

    private PlayerId firstPlayer() {
        int index = 0;
        for(PlayerId p : PlayerId.ALL) {
            if (handOfCard.get(p).contains(SEVEN_OF_DIAMOND)) {
                index = p.ordinal();
            }
        }
        return PlayerId.ALL.get(index);
    }

    private TurnState setANewTurn(PlayerId firstPlayerOfTheTrick) {
        Card.Color trumpColor = setTrump();

        for(PlayerId p : PlayerId.ALL) {
            players.get(p).setTrump(trumpColor);
        }

        return TurnState.initial(trumpColor, turnState.score(), firstPlayerOfTheTrick);
    }

    private Score endOfTurnScore() {
        assert PackedScore.isValid(turnState.packedScore());
        Score score = Score.ofPacked(PackedScore.withAdditionalTrick(turnState.packedScore(),
                TeamId.ALL.get(turnState.trick().winningPlayer().ordinal() % TeamId.COUNT),
                turnState.trick().points()));
        return score.nextTurn();
    }

    private void updateTurnState() {
        updateHandForEachPlayer();
        updateScoreForEachPlayer();
        updateTrickForEachPlayer();
    }

    private void setWinningTeamForEachPlayer(TeamId winningTeam) {
        for(PlayerId p : PlayerId.ALL) {
            players.get(p).setWinningTeam(winningTeam);
        }
    }

    private void updateTrickForEachPlayer() {
        for(PlayerId p : PlayerId.ALL) {
            players.get(p).updateTrick(turnState.trick());
        }
    }

    private void updateScoreForEachPlayer() {
        for(PlayerId p : PlayerId.ALL) {
            players.get(p).updateScore(turnState.score());
        }
    }

    private void updateHandForEachPlayer() {
        for(PlayerId p : PlayerId.ALL) {
            players.get(p).updateHand(handOfCard.get(p));
        }
    }

    private void completeTrick(PlayerId firstToPlay) {
        for(int i = 0; i < PlayerId.COUNT; i ++) {

            PlayerId currentPlayer = PlayerId.ALL.get((firstToPlay.ordinal() + i) % PlayerId.COUNT);

            Player player = players.get(currentPlayer);
            CardSet hand = handOfCard.get(currentPlayer);
            Card cardToPlay = player.cardToPlay(turnState, hand);
            handOfCard.replace(currentPlayer, CardSet.ofPacked(
                    PackedCardSet.remove(handOfCard.get(currentPlayer).packed(), cardToPlay.packed())));
            players.get(currentPlayer).updateHand(handOfCard.get(currentPlayer));

            turnState = turnState.withNewCardPlayed(cardToPlay);
            updateTrickForEachPlayer();
        }
    }
}
