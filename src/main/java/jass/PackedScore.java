package main.java.jass;

import main.java.bits.Bits32;
import main.java.bits.Bits64;

import java.util.StringJoiner;

/**
 * A Score in packed format
 *
 * @author Kilian Schneiter (287496)
 * @author Capucine Berger(269503)
 */

public final class PackedScore {

    public static final long INITIAL = 0;

    private static final int TRICK_SIZE = 4;
    private static final int TRICK_START = 0;
    private static final int TURN_POINTS_SIZE = 9;
    private static final int TURN_POINTS_START = 4;
    private static final int GAME_POINTS_SIZE = 11;
    private static final int GAME_POINTS_START = 13;
    private static final int UNUSED_BIT_SIZE = 8;
    private static final int UNUSED_BIT_START = 24;
    private static final int TEAM_1_START = 0;
    private static final int TEAM_2_START = 32;

    private static final int RESET = 0;
    private static final int MAX_TURN_POINTS = 257;
    private static final int MAX_GAME_POINTS = 2000;

    /**
     * Checks if the pkScore is valid. The sum of the trick numbers of the two teams cannot be greater than 9, the sum of the turn points cannot be
     * greater than 257, and the sum of the game points cannot be greater than 2000. Moreover, all points cannot be negative.
     *
     * @param pkScore a long containing the packed score of the two teams
     * @return a boolean
     */
    public static boolean isValid(long pkScore) {
        long trickTeam1 = Bits64.extract(pkScore, TRICK_START + TEAM_1_START, TRICK_SIZE);
        long turnPointsTeam1 = Bits64.extract(pkScore, TURN_POINTS_START + TEAM_1_START, TURN_POINTS_SIZE);
        long gamePointsTeam1 = Bits64.extract(pkScore, GAME_POINTS_START + TEAM_1_START, GAME_POINTS_SIZE);
        long unusedBitTeam1 = Bits64.extract(pkScore, UNUSED_BIT_START + TEAM_1_START, UNUSED_BIT_SIZE);

        long trickTeam2 = Bits64.extract(pkScore, TRICK_START + TEAM_2_START, TRICK_SIZE);
        long turnPointsTeam2 = Bits64.extract(pkScore, TURN_POINTS_START + TEAM_2_START, TURN_POINTS_SIZE);
        long gamePointsTeam2 = Bits64.extract(pkScore, GAME_POINTS_START + TEAM_2_START, GAME_POINTS_SIZE);
        long unusedBitTeam2 = Bits64.extract(pkScore, UNUSED_BIT_START + TEAM_2_START, UNUSED_BIT_SIZE);

        //System.out.println(trickTeam1 + " " + trickTeam2);
        return (trickTeam1 + trickTeam2 <= Jass.TRICKS_PER_TURN
                && turnPointsTeam1 <= MAX_TURN_POINTS
                && turnPointsTeam2 <= MAX_TURN_POINTS
                && gamePointsTeam1 <= MAX_GAME_POINTS
                && gamePointsTeam2 <= MAX_GAME_POINTS
                && unusedBitTeam1 == 0
                && unusedBitTeam2 == 0);
    }

    /**
     * Packs all the points of both teams using Bits64.pack and packTeamScore
     *
     * @param turnTricks1 number of tricks won by team 1
     * @param turnPoints1 number of points won during the turn by team 1
     * @param gamePoints1 number of points won during the game by team 1
     * @param turnTricks2 number of tricks won by team 2
     * @param turnPoints2 number of points won during the turn by team 2
     * @param gamePoints2 number of points won during the game by team 2
     * @return a long containing the packed score of the two teams
     */
    public static long pack(int turnTricks1, int turnPoints1, int gamePoints1, int turnTricks2, int turnPoints2, int gamePoints2) {
        long pkTeam1Score = packTeamScore(turnTricks1, turnPoints1, gamePoints1);
        long pkTeam2Score = packTeamScore(turnTricks2, turnPoints2, gamePoints2);

        return Bits64.pack(pkTeam1Score, Integer.SIZE, pkTeam2Score, Integer.SIZE);
    }

    /**
     * Packs the score of a team given its number of trick, its number of points in the turn and its number of points in the game
     *
     * @param trick number of tricks won by the team
     * @param turn number of points won by the team during the turn
     * @param game number of point won by the team during the game
     * @return the packed score of the team
     */
    private static long packTeamScore(int trick, int turn, int game) {
        return Bits32.pack(trick, TRICK_SIZE, turn, TURN_POINTS_SIZE, game, GAME_POINTS_SIZE);
    }

    /**
     * Returns the number of tricks won by the given team
     *
     * @param pkScore packed Score of both teams
     * @param t the teamID of the team
     * @return the number of tricks won by the team
     */
    public static int turnTricks(long pkScore, TeamId t) {
        assert isValid(pkScore);

        if (t == TeamId.TEAM_1) {
            return (int) Bits64.extract(pkScore, TRICK_START + TEAM_1_START, TRICK_SIZE);
        } else {
            return (int) Bits64.extract(pkScore, TRICK_START + TEAM_2_START, TRICK_SIZE);
        }
    }

    /**
     * @param pkScore packed Score of both teams
     * @param t the teamID of the team
     * @return the number of points won by the team during the turn
     */
    public static int turnPoints(long pkScore, TeamId t) {
        assert isValid(pkScore);

        if (t == TeamId.TEAM_1) {
            return (int) Bits64.extract(pkScore, TURN_POINTS_START + TEAM_1_START, TURN_POINTS_SIZE);
        } else {
            return (int) Bits64.extract(pkScore, TURN_POINTS_START + TEAM_2_START, TURN_POINTS_SIZE);
        }

    }

    /**
     * @param pkScore packed Score of both teams
     * @param t the teamID of the team
     * @return the number of points won by the team during the game
     */
    public static int gamePoints(long pkScore, TeamId t) {
        assert isValid(pkScore);

        if (t == TeamId.TEAM_1) {
            return (int) Bits64.extract(pkScore, GAME_POINTS_START + TEAM_1_START, GAME_POINTS_SIZE);
        } else {
            return (int) Bits64.extract(pkScore, GAME_POINTS_START + TEAM_2_START, GAME_POINTS_SIZE );
        }
    }

    /**
     * @param pkScore packed Score of both teams
     * @param t teamID of the team
     * @return number of points won by the team during the game and the turn
     */
    public static int totalPoints(long pkScore, TeamId t) {
        assert isValid(pkScore);

        return turnPoints(pkScore, t) + gamePoints(pkScore, t);
    }

    /**
     * Updates the number of tricks and points of the turn and adds additional match points if the number of tricks won
     * is equal to 9.
     *
     * @param pkScore packed Score of both teams
     * @param winningTeam teamID of the winning team
     * @param trickPoints points to add to the turn points of the winning team
     * @return pkScore updated
     */
    public static long withAdditionalTrick(long pkScore, TeamId winningTeam, int trickPoints) {
        assert isValid(pkScore);

        if (turnTricks(pkScore, winningTeam) + 1 == Jass.TRICKS_PER_TURN) {
            return orderedScorePacked(pkScore, winningTeam, trickPoints, Jass.MATCH_ADDITIONAL_POINTS);
        } else {
            return orderedScorePacked(pkScore, winningTeam, trickPoints, RESET);
        }
    }

    /**
     * Prepare the pkScore for a new turn : reset turn points and trick points to zero and transfer the points won in the game points
     *
     * @param pkScore packed Score of both teams
     * @return pkScore updated
     */
    public static long nextTurn(long pkScore) {
        assert isValid(pkScore);

        int updatedTeam1Points = gamePoints(pkScore, TeamId.TEAM_1) + turnPoints(pkScore, TeamId.TEAM_1);
        int updatedTeam2Points = gamePoints(pkScore, TeamId.TEAM_2) + turnPoints(pkScore, TeamId.TEAM_2);

        return pack(RESET, RESET, updatedTeam1Points, RESET, RESET, updatedTeam2Points);
    }

    /**
     * @param pkScore packed Score of both teams
     * @return a String representing the Score
     */
    public static String toString(long pkScore) {
        assert isValid(pkScore);

        String scoreString1 = new StringJoiner(", ", "(", ")")
                .add(Integer.toString(turnTricks(pkScore, TeamId.TEAM_1)))
                .add(Integer.toString(turnPoints(pkScore, TeamId.TEAM_1)))
                .add(Integer.toString(gamePoints(pkScore, TeamId.TEAM_1)))
                .toString();

        String scoreString2 = new StringJoiner(", ", "(", ")")
                .add(Integer.toString(turnTricks(pkScore, TeamId.TEAM_2)))
                .add(Integer.toString(turnPoints(pkScore, TeamId.TEAM_2)))
                .add(Integer.toString(gamePoints(pkScore, TeamId.TEAM_2)))
                .toString();

        return scoreString1 + "\t/\t" + scoreString2;
    }

    /* Updates the score of the team given, in the entire packed score
     *
     * @param pkScore the entire PackedScore
     * @param team the TeamId of the team that won points
     * @param trickPoints the points that the team won in the trick
     * @param matchAdditionalPoints the bonus points that the team won (if there are any)
     * @return
     */
    private static long orderedScorePacked(long pkScore, TeamId team, int trickPoints, int matchAdditionalPoints) {
        assert isValid(pkScore);

        if (team == TeamId.TEAM_1) {
            return Bits64.pack(replaceTeamBits(pkScore, trickPoints + matchAdditionalPoints, team), Integer.SIZE,
                    extractScoreTeam(pkScore, TeamId.TEAM_2), Integer.SIZE);
        } else {
            return Bits64.pack(extractScoreTeam(pkScore, TeamId.TEAM_1), Integer.SIZE,
                    replaceTeamBits(pkScore, trickPoints + matchAdditionalPoints, team), Integer.SIZE);
        }
    }

    /* Replace the bits of the team given
     *
     * @param pkScore the entire PackedScore
     * @param updatedPoints the points that need to be updated of the team
     * @param t the TeamId of the team that needs to be updated
     * @return the updated PackedScore, containing only the updated team
     */
    private static long replaceTeamBits(long pkScore, int updatedPoints, TeamId t) {
        assert isValid(pkScore);

        int gamePoints = gamePoints(pkScore, t);
        int turnPoints = turnPoints(pkScore, t) + updatedPoints;
        int trickNb = turnTricks(pkScore, t);

        return packTeamScore(trickNb + 1, turnPoints, gamePoints);
    }

    private static int extractScoreTeam(long pkScore, TeamId t) {
        assert isValid(pkScore);

        if (t == TeamId.TEAM_1) {
            return (int) Bits64.extract(pkScore, TEAM_1_START, Integer.SIZE);
        } else {
            return (int) Bits64.extract(pkScore, TEAM_2_START, Integer.SIZE);
        }
    }
}
