package main.java.jass;

import main.java.Preconditions;

import java.util.*;

/**
 * A Player using the MCTS algorithm
 *
 * @author Kilian Schneiter (287496)
 * @author Capucine Berger(269503)
 */

public final class MctsPlayer implements Player {

    private final PlayerId mctsPlayer;
    private final SplittableRandom splittableRandom;
    private final int iterations;
    private List<Node> tree = new ArrayList<>();

    private static final int MAX_SCORE = 157;
    private static final int TURN_SIZE = 9;

    /**
     * Builds a new MctsPlayer with the given argument
     *
     * @param ownId the PlayerId of the MctsPlayer
     * @param rngSeed the seed
     * @param iterations the number of iterations the algorithms will run before choosing which card to play
     */
    public MctsPlayer(PlayerId ownId, long rngSeed, int iterations) {
        Preconditions.checkArgument(iterations >= TURN_SIZE);

        mctsPlayer = ownId;
        SplittableRandom rng = new SplittableRandom(rngSeed);
        this.splittableRandom = new SplittableRandom(rng.nextLong());
        this.iterations = iterations;
    }

    /**
     * Given the turnState and the cards in the hand of the MctsPlayer, chooses which is the best card to play
     *
     * @param state the state of the game
     * @param hand the hand of the MctsPlayer
     * @return a Card (the best card to play)
     */
    @Override
    public Card cardToPlay(TurnState state, CardSet hand) {
        if (hand.size() == 1) {
            tree.clear();
            return hand.get(0);
        } else {
            Node padre = new Node(state, updatedUnusedCards(state, hand));
            tree.add(padre);

            for (int i = 0; i < iterations - 1; i++) {
                List<Node> path = pathForBestNode();
                Node recursiveParent = path.get(path.size() - 1);
                CardSet updatedHand = hand.intersection(recursiveParent.turnState.unplayedCards());
                Card cardToBePlayed = null;

                if (!recursiveParent.unUsedCards.isEmpty()) {
                    cardToBePlayed = getRandomCardToPlay(recursiveParent.turnState, recursiveParent.unUsedCards);
                }

                TurnState updatedState = recursiveParent.turnState;

                if (cardToBePlayed != null) {
                    updatedHand = updatedHand.remove(cardToBePlayed);
                    updatedState = updatedState.withNewCardPlayedAndTrickCollected(cardToBePlayed);
                }

                Node child = new Node(updatedState, updatedUnusedCards(updatedState, updatedHand));
                child.totalScore = getFinalScore(updatedState, updatedHand).turnPoints(currentPlayerTeam(updatedState));
                updateAllScores(addNewNode(child, path));
            }
            CardSet nodeCards = padre.children[padre.getIndexOfBestSon(0)].getNodeCards();
            CardSet padreCards = padre.getNodeCards();
            tree.clear();
            return nodeCards.difference(padreCards).get(0);
        }
    }

    /*
     * Returns a card that can be played in the state of a turn, considering who is playing
     *
     * @param state the current TurnState
     * @param playableCards the CardSet of the cards that can be played by other players
     * @param hand the CardSet containing the hand of the MctsPlayer
     * @return the Card that can be played
     */
    private Card getCardToPlay(TurnState state, CardSet playableCards, CardSet hand) {
        Card cardToBePlayed;
        if (state.nextPlayer() != mctsPlayer) {
            if (!playableCards.isEmpty()) {
                cardToBePlayed = getRandomCardToPlay(state, playableCards);
            } else {
                throw new IllegalStateException("No cards left to play");
            }
        } else {
            cardToBePlayed = getRandomCardToPlay(state, hand);
        }

        return cardToBePlayed;
    }

    /*
     * @param state the current TurnState
     * @return the team of the player that is currently playing
     */
    private TeamId currentPlayerTeam(TurnState state) {
        return PlayerId.ALL.get((state.nextPlayer().ordinal() + 3) % PlayerId.COUNT).team();
    }

    /*
     * Finishes randomly the the turn and returns the Score of both teams
     *
     * @param turnState the current TurnState
     * @param hand the CardSet containing the hand of the MctsPlayer
     * @return the resulting Score
     */
    private Score getFinalScore(TurnState turnState, CardSet hand) {
        TurnState copy = TurnState.ofPackedComponents(turnState.packedScore(), turnState.packedUnplayedCards(), turnState.packedTrick());

        while(!copy.unplayedCards().isEmpty()) {
            if (copy.trick().isFull()) {
                copy = copy.withTrickCollected();
            }
            Card cardToPlay = getCardToPlay(copy, getPlayableCards(copy, hand), hand);
            copy = copy.withNewCardPlayed(cardToPlay);
            hand = hand.remove(cardToPlay);
        }

        return copy.withTrickCollected().score();
    }

    /*
     * Finds the best Node to get a child and returns the path to it, from the top of the tree
     *
     * @return a List containing the Nodes we went through to get to the best Node
     */
    private List<Node> pathForBestNode() {
        Node bestNode = tree.get(0);
        List<Node> path = new ArrayList<>();
        path.add(bestNode);

        while(bestNode.unUsedCards.isEmpty() && !bestNode.turnState.trick().isLast()) {
            bestNode = bestNode.children[bestNode.getIndexOfBestSon(Node.C)];
            path.add(bestNode);
        }

        return path;
    }

    /*
     * Returns a random Card in a CardSet, considering a TurnState.
     * Or null if there is no cards left to play in the turn.
     *
     * @param turnState the current TurnState
     * @param cardSet the CardSet in which we want to get a Card
     * @return the random Card to be played
     */
    private Card getRandomCardToPlay(TurnState turnState, CardSet cardSet) {
        int bound = turnState.trick().playableCards(cardSet).size();

        if (bound > 0)
            return turnState.trick().playableCards(cardSet).get(splittableRandom.nextInt(bound));
        else
            return null;
    }

    /*
     * Adds a new Node at the end of the path
     *
     * @param nodeChild the Node to be added
     * @param path the List of Nodes that we went through to get the best one
     * @return a List of Node containing the path with the child added
     */
    private List<Node> addNewNode(Node nodeChild, List<Node> path) {
        path.get(path.size() - 1).addChild(nodeChild);

        for (Node n : path) {
            n.numberOfTurnsSimulated++;
        }
        path.add(nodeChild);

        return path;
    }

    /*
     * Returns the cards that can be played by other players (not Mcts)
     *
     * @param turnState the current TurnState
     * @param hand the CardSet containing the hand of the MctsPlayer
     * @return the CardSet containing the cards that can be played by other players
     */
    private CardSet getPlayableCards(TurnState turnState, CardSet hand) {
        return turnState.unplayedCards().difference(hand);
    }

    /* Updates all the scores of a list of Node
     *
     * @param nList the List of all Nodes that we want to update the score
     */
    private void updateAllScores(List<Node> nList) {
        for(int i = nList.size() - 2 ; i >= 0; i--) {
            nList.get(i).updateScore(nList.get(nList.size() - 1));
        }
    }

    /*
     * Returns the unUsedCards for a new Node
     *
     * @param state the current TurnState
     * @param hand the CardSet containing the hand of the MctsPlayer
     * @return a CardSet containing all the cards that can be played right after this Node
     */
    private CardSet updatedUnusedCards(TurnState state, CardSet hand) {
        CardSet possibleCards;

        if (state.nextPlayer() == mctsPlayer) {
            possibleCards = state.trick().playableCards(hand);
        } else {
            possibleCards = getPlayableCards(state, hand);
        }

        return possibleCards;
    }

    private static final class Node {

        private final TurnState turnState;
        private final Node[] children;
        private CardSet unUsedCards;
        private int totalScore; // S(n)
        private int numberOfTurnsSimulated; // N(n)

        private static final int C = 40;
        private static final int MAX_CHILDREN_SIZE = Jass.HAND_SIZE * PlayerId.COUNT;

        private Node(TurnState turnState, CardSet unUsedCards) {
            this.turnState = turnState;
            this.unUsedCards = unUsedCards;
            children = new Node[MAX_CHILDREN_SIZE];
            totalScore = 0;
            numberOfTurnsSimulated = 1;
        }

        /*
         * Adds a child to a Node
         *
         * @param child the node to add
         */
        private void addChild (Node child) {
            boolean childAdded = false;
            for (int i = 0; i < children.length; i++) {
                if (children[i] == null && !childAdded) {
                    children[i] = child;
                    CardSet cardToDelete = child.getNodeCards().difference(getNodeCards());
                    unUsedCards = unUsedCards.difference(cardToDelete);
                    childAdded = true;
                }
            }
        }

        /*
         * Computes the value of V(s)
         *
         * @param index the index of the child
         * @param constant the constant
         * @return double, the value of V(s)
         */
        private double valueOfV (int index, int constant) {
            if (children[index] == null) {
                return 0;
            } else {
                if (children[index].numberOfTurnsSimulated > 0)
                    return ((double)children[index].totalScore / (double)children[index].numberOfTurnsSimulated) +
                            constant * Math.sqrt(2 * Math.log((double)numberOfTurnsSimulated)
                                    / (double)children[index].numberOfTurnsSimulated);
                else
                    return Double.POSITIVE_INFINITY;
            }
        }

        /*
         * Computed the best Node to add a new Node to
         *
         * @param c the constant, usually 40 for this project
         * @return the index of the best node
         */
        private int getIndexOfBestSon(int c) {
            int index = 0;
            double maxValueOfV = 0;

            for (int i = 0; i < children.length; i++) {
                if (valueOfV(i, c) > maxValueOfV) {
                    maxValueOfV = valueOfV(i, c);
                    index = i;
                }
            }

            return index;
        }

        /*
         * Checks if two Nodes are from the same team
         *
         * @return true if they are of the same team
         */
        private boolean areNodesFromTheSameTeam(Node node) {
            return turnState.trick().player(turnState.trick().size()) == node.turnState.trick().player(node.turnState.trick().size());
        }

        /*
         * Updates, according to the team, the score of the Node, by adding the score of its child
         *
         * @param child the child of the Node
         */
        private void updateScore(Node child) {
            if (areNodesFromTheSameTeam(child)) {
                totalScore += child.totalScore;
            } else {
                totalScore += MAX_SCORE - child.totalScore;
            }
        }

        /*
         * Get the cards in the Node
         */
        private CardSet getNodeCards() {
            return CardSet.ALL_CARDS.difference(turnState.unplayedCards());
        }
    }
}
