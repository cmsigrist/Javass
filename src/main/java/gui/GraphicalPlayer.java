package main.java.gui;

import main.java.jass.Card;
import main.java.jass.Jass;
import main.java.jass.PlayerId;
import main.java.jass.TeamId;

import com.sun.javafx.collections.UnmodifiableObservableMap;
import javafx.beans.binding.*;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableMap;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.util.Map;
import java.util.StringJoiner;
import java.util.TreeMap;
import java.util.concurrent.ArrayBlockingQueue;

/**
 * A player that will have his own window of the game on a computer.
 *
 * @author Kilian Schneiter (287496)
 * @author Capucine Berger(269503)
 */

public final class GraphicalPlayer {
    private final PlayerId player;
    private final TreeMap<PlayerId, String> playerNames;
    private final ScoreBean scoreBean;
    private final TrickBean trickBean;
    private final HandBean handBean;
    private final ArrayBlockingQueue<Card> cardArrayBlockingQueue;

    private final int IMAGE_WIDTH_HAND = 160;
    private final int IMAGE_WIDTH_TRICK = 240;
    private final int TRICK_CARD_HEIGHT = 180;
    private final int TRICK_CARD_WIDTH = 120;
    private final int TRUMP_DIMENSION = 101;
    private final int HAND_CARD_HEIGHT = 120;
    private final int HAND_CARD_WIDTH = 80;
    private final int HALO_BLUR_RADIUS = 4;

    private final double PLAYABLE_OPACITY = 1d;
    private final double UNPLAYABLE_OPACITY = 0.2;
    private final double CARD_MARGIN = 5d;

    /**
     * The constructor of the class GraphicalPlayer.
     *
     * @param playerId the PlayerId of the player that is playing on screen
     * @param playerNames a map that assigns each player to his name
     * @param scoreBean the score bean that is used to show the score on the stage
     * @param trickBean the trick bean that is used to show the trick on the stage
     * @param handBean the hand bean that is used to show the hand on the stage
     * @param cardArrayBlockingQueue the ArrayBlockingQueue that transmits the card we want to play to the
     *                               GraphicalPlayerAdapter which waits for us to fill it
     */
    public GraphicalPlayer(PlayerId playerId, Map<PlayerId, String> playerNames, ScoreBean scoreBean,
                           TrickBean trickBean, HandBean handBean, ArrayBlockingQueue<Card> cardArrayBlockingQueue) {
        player = playerId;
        this.playerNames = new TreeMap<>(playerNames);
        this.scoreBean = scoreBean;
        this.trickBean = trickBean;
        this.handBean = handBean;
        this.cardArrayBlockingQueue = cardArrayBlockingQueue;
    }

    /**
     * Creates the Stage that will be showed on screen, by calling each's part method.
     *
     * @return the game stage that we see when we launch the program
     */
    public Stage createStage() {
        Stage gameStage = new Stage();
        gameStage.setTitle("Javass - " + playerNames.get(player));

        BorderPane borderPane = new BorderPane();
        borderPane.setTop(createScorePane());
        borderPane.setCenter(createTrickPane());
        borderPane.setBottom(createHandPane());

        StackPane stackPane = new StackPane(
                borderPane,
                createVictoryPane(TeamId.TEAM_1),
                createVictoryPane(TeamId.TEAM_2)
        );

        gameStage.setScene(new Scene(stackPane));
        gameStage.sizeToScene();

        return gameStage;
    }

    /*
     * Creates the Score pane that contains the names of the players of each team and their score.
     *
     * @return the GridPane that is used to show the scores
     */
    private GridPane createScorePane() {
        GridPane scorePane = new GridPane();

        for (TeamId id : TeamId.ALL) {
            String player1Name = playerNames.get(PlayerId.ALL.get(id.ordinal()));
            String player2Name = playerNames.get(PlayerId.ALL.get(id.ordinal() + TeamId.COUNT));
            Text teamNamesT = new Text(player1Name + " et " + player2Name + " : ");
            GridPane.setHalignment(teamNamesT, HPos.RIGHT);

            Text teamTurnPointsT = new Text();
            teamTurnPointsT.textProperty().bind(Bindings.convert(scoreBean.turnPointsProperty(id)));
            GridPane.setHalignment(teamTurnPointsT, HPos.RIGHT);

            StringProperty differenceP = new SimpleStringProperty();
            scoreBean.turnPointsProperty(id).addListener((l, x, y) -> {
                        if (y.intValue() - x.intValue() > 0)
                            differenceP.set(" (+" + (y.intValue() - x.intValue()) + ")");
                    });
            Text teamTrickPointsT = new Text();
            teamTrickPointsT.textProperty().bind(differenceP);

            Text totalT = new Text("/ Total : ");
            Text teamTotalPointsT = new Text();
            teamTotalPointsT.textProperty().bind(Bindings.convert(scoreBean.totalPointsProperty(id)));
            GridPane.setHalignment(teamTotalPointsT, HPos.RIGHT);

            scorePane.add(teamNamesT, 0, id.ordinal());
            scorePane.add(teamTurnPointsT, 1, id.ordinal());
            scorePane.add(teamTrickPointsT, 2, id.ordinal());
            scorePane.add(totalT, 3, id.ordinal());
            scorePane.add(teamTotalPointsT, 4, id.ordinal());
        }

        scorePane.setStyle("-fx-font: 16 Optima;"
                + "-fx-background-color: lightgray;" +
                "-fx-padding: 5px;" +
                "-fx-alignment: center;");

        return scorePane;
    }

    /*
     * Creates the trick part of the stage, with the 4 cards and the trump in the center.
     *
     * @return the GridPane that contains the trick pane
     */
    private GridPane createTrickPane() {
        GridPane trickPane = new GridPane();
        UnmodifiableObservableMap<Card, Image> cardImageUnmodifiableObservableMap = cardToImageMap(IMAGE_WIDTH_TRICK);

        for (PlayerId p : PlayerId.ALL) {
            ImageView cardI = new ImageView();

            ObjectBinding<Card> cardObjectBinding = Bindings.valueAt(trickBean.trick(), p);

            cardI.imageProperty().bind(Bindings.valueAt(cardImageUnmodifiableObservableMap, cardObjectBinding));
            cardI.setFitHeight(TRICK_CARD_HEIGHT);
            cardI.setFitWidth(TRICK_CARD_WIDTH);

            PlayerId eastPlayer = PlayerId.ALL.get((player.ordinal() + 1) % PlayerId.COUNT);
            PlayerId northPlayer = PlayerId.ALL.get((player.ordinal() + 2) % PlayerId.COUNT);
            PlayerId westPlayer = PlayerId.ALL.get((player.ordinal() + 3) % PlayerId.COUNT);

            if (p.equals(player))
                trickPane.add(createVBox(cardI, p), 1, 2, 1, 1);
            if (p.equals(eastPlayer))
                trickPane.add(createVBox(cardI, p), 2, 0, 1, 3);
            if (p.equals(northPlayer))
                trickPane.add(createVBox(cardI, p), 1, 0, 1, 1);
            if (p.equals(westPlayer))
                trickPane.add(createVBox(cardI, p), 0, 0, 1, 3);

        }

        ImageView trumpI = new ImageView();
        trumpI.imageProperty().bind(Bindings.valueAt(colourToImageMap(), trickBean.trumpProperty()));
        trumpI.setFitHeight(TRUMP_DIMENSION);
        trumpI.setFitWidth(TRUMP_DIMENSION);

        VBox trumpBox = new VBox(trumpI);
        trumpBox.setAlignment(Pos.CENTER);
        trickPane.add(trumpBox, 1, 1, 1, 1);

        trickPane.setStyle("-fx-background-color: whitesmoke;" +
                "-fx-padding: 5px;" +
                "-fx-border-width: 3px 0px;" +
                "-fx-border-style: solid;" +
                "-fx-border-color: gray;" +
                "-fx-alignment: center;");

        return trickPane;
    }

    /*
     * Creates the victory pane for a given TeamId, which shows the names of both players that won, the points that
     * they had and the points of the opposing team. This will be displayed only if one of the team won the game.
     *
     * @param teamId the id of the team
     * @return the border pane used to show the victory pane
     */
    private BorderPane createVictoryPane(TeamId teamId) {
        BorderPane victoryPane = new BorderPane();

        String player1 = playerNames.get(PlayerId.ALL.get(teamId.ordinal()));
        String player2 = playerNames.get(PlayerId.ALL.get(teamId.ordinal() + TeamId.COUNT));

        ReadOnlyIntegerProperty totalPoints1 = scoreBean.totalPointsProperty(teamId);
        ReadOnlyIntegerProperty totalPoints2 = scoreBean.totalPointsProperty(teamId.other());

        Text gamePointsT = new Text();
        String format = player1 + " et " + player2 + " ont gagné avec %d points contre %d.";
        gamePointsT.textProperty().bind(Bindings.format(format, totalPoints1, totalPoints2));

        victoryPane.setStyle("-fx-font: 16 Optima;" +
                "-fx-background-color: white;");

        victoryPane.visibleProperty().bind(scoreBean.winningTeamProperty().isEqualTo(teamId));
        victoryPane.setCenter(gamePointsT);

        return victoryPane;
    }

    /*
     * Creates the part that shows the cards the player has in his hand, at the bottom of the window. Cards are more
     * opaque if they can't be played or if it is not the turn of the player, and they disappear if they have been
     * played. If the card is playable and the player clicks on it, it is put in the cardArrayBlockingQueue to inform
     * the GraphicalPlayerAdapter.
     *
     * @return a Hbox that shows all the cards of the hand
     */
    private HBox createHandPane() {
        HBox handPane = new HBox();
        UnmodifiableObservableMap<Card, Image> handCardMap = cardToImageMap(IMAGE_WIDTH_HAND);

        for (int i = 0; i < Jass.HAND_SIZE; i++) {
            ImageView cardI = new ImageView();
            ObjectBinding<Card> cardObjectBinding = Bindings.valueAt(handBean.hand(), i);
            cardI.imageProperty().bind(Bindings.valueAt(handCardMap, cardObjectBinding));

            BooleanBinding isPlayable = Bindings.createBooleanBinding(() ->
                    handBean.playableCards().contains(cardObjectBinding.getValue()), handBean.hand(),
                    handBean.playableCards());
            cardI.disableProperty().bind(isPlayable.not());

            DoubleBinding opacityBinding = new When(isPlayable).then(PLAYABLE_OPACITY).otherwise(UNPLAYABLE_OPACITY);
            cardI.opacityProperty().bind(opacityBinding);

            cardI.setOnMouseClicked((e) -> {
                try {
                    cardArrayBlockingQueue.put(cardObjectBinding.get());
                } catch (InterruptedException exception) {
                    throw new Error(exception);
                }
            });

            cardI.setFitHeight(HAND_CARD_HEIGHT);
            cardI.setFitWidth(HAND_CARD_WIDTH);
            handPane.getChildren().add(cardI);
        }

        handPane.setStyle("-fx-background-color: lightgray;"
                + "-fx-spacing: 5px;"
                + "-fx-padding: 5px;");

        return handPane;
    }

    /*
     * Creates the VBox that contains the card and the name of the player (the card is under the name). If the card is
     * the current winning card of the trick, it is surrounded by a red halo. The VBox of the player playing on screen
     * is different : his name is under the card.
     *
     * @param cardI the ImageView corresponding to the card
     * @param player the PlayerId of the player that played this card
     * @return the VBox filled with what we want to show on screen
     */
    private VBox createVBox(ImageView cardI, PlayerId player) {
        Rectangle halo = new Rectangle(TRICK_CARD_WIDTH, TRICK_CARD_HEIGHT);
        halo.setStyle("-fx-arc-width: 20;"
                + "-fx-arc-height: 20;"
                + "-fx-fill: transparent;"
                + "-fx-stroke: lightpink;"
                + "-fx-stroke-width: 5;"
                + "-fx-opacity: 0.5;");
        halo.setEffect(new GaussianBlur(HALO_BLUR_RADIUS));
        halo.visibleProperty().bind(trickBean.winningPlayerProperty().isEqualTo(player));

        StackPane stackPane = new StackPane(halo, cardI);

        Text playerT = new Text(playerNames.get(player));
        playerT.setStyle("-fx-font: 14 Optima;");

        VBox cardP;
        if(player.equals(this.player))
            cardP = new VBox(stackPane, playerT);
        else
            cardP = new VBox(playerT, stackPane);
        cardP.setAlignment(Pos.CENTER);
        GridPane.setMargin(cardP, new Insets(CARD_MARGIN));

        return cardP;
    }

    /*
     * Creates an unmodifiable observable map that assigns each card to its image, considering the width.
     *
     * @param imageWidth the width of the images
     * @return the corresponding map
     */
    private UnmodifiableObservableMap<Card, Image> cardToImageMap(int imageWidth) {
        ObservableMap<Card, Image> cardImageObservableMap = FXCollections.observableHashMap();

        for(Card.Color c : Card.Color.ALL) {
            for (Card.Rank r : Card.Rank.ALL) {
                String cardPath = new StringJoiner("_", "resources/", ".png")
                        .add("card")
                        .add(Integer.toString(c.ordinal()))
                        .add(Integer.toString(r.ordinal()))
                        .add(Integer.toString(imageWidth))
                        .toString();
                cardImageObservableMap.put(Card.of(c, r), new Image(cardPath));
            }
        }

        return new UnmodifiableObservableMap<>(cardImageObservableMap);
    }

    /*
     * Creates an unmodifiable observable map that assigns each trump color to its image.
     *
     * @return the corresponding unmodifiable map
     */
    private UnmodifiableObservableMap<Card.Color, Image> colourToImageMap() {
        ObservableMap<Card.Color, Image> colorImageObservableMap = FXCollections.observableHashMap();

        for (Card.Color c : Card.Color.ALL) {
            String colourName = new StringJoiner("_", "/resources/", ".png")
                    .add("trump")
                    .add(Integer.toString(c.ordinal()))
                    .toString();
            colorImageObservableMap.put(c, new Image(colourName));
        }

        return new UnmodifiableObservableMap<>(colorImageObservableMap);
    }

}
