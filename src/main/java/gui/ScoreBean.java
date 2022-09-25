package main.java.gui;

import main.java.jass.TeamId;
import javafx.beans.property.*;

/**
 * A Score represented in a graphical context. It will be used the show the scores of both teams on screen.
 *
 * @author Kilian Schneiter (287496)
 * @author Capucine Berger(269503)
 */

public final class ScoreBean {
    private final IntegerProperty turnPoints1;
    private final IntegerProperty gamePoints1;
    private final IntegerProperty totalPoints1;
    private final IntegerProperty turnPoints2;
    private final IntegerProperty gamePoints2;
    private final IntegerProperty totalPoints2;
    private final ObjectProperty<TeamId> winningTeam;

    /**
     * Creates a new ScoreBean that initialises all the properties.
     */
    public ScoreBean() {
        turnPoints1 = new SimpleIntegerProperty();
        gamePoints1 = new SimpleIntegerProperty();
        totalPoints1 = new SimpleIntegerProperty();
        turnPoints2 = new SimpleIntegerProperty();
        gamePoints2 = new SimpleIntegerProperty();
        totalPoints2 = new SimpleIntegerProperty();
        winningTeam = new SimpleObjectProperty<>();
    }

    /**
     * Getter of the turn points property.
     *
     * @param team the TeamId of the team we want to get the property
     * @return the property containing the turn points of the given TeamId
     */
    public ReadOnlyIntegerProperty turnPointsProperty(TeamId team) {
        if (team == TeamId.TEAM_1)
            return turnPoints1;
        else
            return turnPoints2;
    }

    /**
     * Sets a new value to the turnPoints of the property of a given team.
     *
     * @param team the TeamId of the team we want to set the points
     * @param newTurnPoints the new turn points of the team
     */
    public void setTurnPoints(TeamId team, int newTurnPoints) {
        if (team == TeamId.TEAM_1)
            turnPoints1.setValue(newTurnPoints);
        else
            turnPoints2.setValue(newTurnPoints);
    }

    /**
     * Getter of the game points property.
     *
     * @param team the TeamId of the team we want to get the property
     * @return the property containing the game points of the given TeamId
     */
    public ReadOnlyIntegerProperty gamePointsProperty(TeamId team) {
        if (team == TeamId.TEAM_1)
            return gamePoints1;
        else
            return gamePoints2;
    }

    /**
     * Sets a new value to the gamePoints of the property of a given team.
     *
     * @param team the TeamId of the team we want to set the points
     * @param newGamePoints the new game points of the team
     */
    public void setGamePoints(TeamId team, int newGamePoints) {
        if (team == TeamId.TEAM_1)
            gamePoints1.setValue(newGamePoints);
        else
            gamePoints2.setValue(newGamePoints);
    }

    /**
     * Getter of the total points property.
     *
     * @param team the TeamId of the team we want to get the property
     * @return the property containing the total points of the given TeamId
     */
    public ReadOnlyIntegerProperty totalPointsProperty(TeamId team) {
        if (team == TeamId.TEAM_1)
            return totalPoints1;
        else
            return totalPoints2;
    }

    /**
     * Sets a new value to the totalPoints of the property of a given team.
     *
     * @param team the TeamId of the team we want to set the points
     * @param newTotalPoints the new total points of the team
     */
    public void setTotalPoints(TeamId team, int newTotalPoints) {
        if (team == TeamId.TEAM_1)
            totalPoints1.setValue(newTotalPoints);
        else
            totalPoints2.setValue(newTotalPoints);
    }

    /**
     * Getter of the winning team property.
     *
     * @return the property containing the winning team
     */
    public ReadOnlyObjectProperty<TeamId> winningTeamProperty() {
        return winningTeam;
    }

    /**
     * Sets a new value to the property containing the winning team
     *
     * @param wTeam the TeamId of the new winning team
     */
    public void setWinningTeam(TeamId wTeam) {
        winningTeam.setValue(wTeam);
    }
}
