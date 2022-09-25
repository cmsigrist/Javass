package main.java.jass;

import main.java.Preconditions;

/**
 * The Score of the game
 *
 * @author Kilian Schneiter (287496)
 * @author Capucine Berger(269503)
 */

public final class Score {

    public static final Score INITIAL = new Score(0L);

    private long pkScore;

    private Score(long pkScore) {
        this.pkScore = pkScore;
    }

    /**
     * Builds the Score given a packed score
     *
     * @param packed a packed score
     * @return a new Score containing the packed score
     * @throws IllegalArgumentException if the packed score is not valid using PackedScore.isValid
     */
    public static Score ofPacked(long packed) {
        Preconditions.checkArgument(PackedScore.isValid(packed));
        return new Score(packed);
    }

    /**
     * @return long representing the score of both teams
     */
    public long packed() {
        return pkScore;
    }

    /**
     * @param t team ID of the team
     * @return number of tricks won by the team
     */
    public int turnTricks(TeamId t) {
        return PackedScore.turnTricks(pkScore, t);
    }

    /**
     * @param t team ID of the team
     * @return number of points won by the team during the turn
     */
    public int turnPoints(TeamId t) {
        return PackedScore.turnPoints(pkScore, t);
    }

    /**
     * @param t team ID of the team
     * @return number of points won by the team during the game
     */
    public int gamePoints(TeamId t) {
        return PackedScore.gamePoints(pkScore, t);
    }

    /**
     * @param t team ID of the team
     * @return number of points won by the team in total
     */
    public int totalPoints(TeamId t) {
        return PackedScore.totalPoints(pkScore, t);
    }

    /**
     * Updates the number of tricks and points of the turn and adds additional match points if the number of tricks won
     * is equal to 9.
     *
     * @param winningteam team ID of the winning team
     * @param trickPoints points to add to turn points
     * @return updated Score
     * @throws IllegalArgumentException if the trick points to add are negative
     */
    public Score withAdditionalTrick(TeamId winningteam, int trickPoints) {
        Preconditions.checkArgument(trickPoints >= 0);
        return new Score(PackedScore.withAdditionalTrick(pkScore, winningteam, trickPoints));
    }

    /**
     * Prepares the pkScore for a new turn : reset turn points and trick points to zero and transfer the points won in the game points
     *
     * @return updated Score
     */
    public Score nextTurn() {
        return new Score(PackedScore.nextTurn(pkScore));
    }

    @Override
    public boolean equals(Object thatO){
        return (thatO instanceof Score) && (((Score) thatO).packed() == pkScore);
    }

    @Override
    public int hashCode() {
        return Long.hashCode(pkScore);
    }

    @Override
    public String toString() {
        return PackedScore.toString(pkScore);
    }
}
