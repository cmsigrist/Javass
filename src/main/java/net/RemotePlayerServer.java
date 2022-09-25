package main.java.net;

import main.java.jass.*;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

import static java.nio.charset.StandardCharsets.US_ASCII;

/**
 * A class used to build a remote player server
 *
 * @author Kilian Schneiter (287496)
 * @author Capucine Berger(269503)
 */

public final class RemotePlayerServer {

    private final Player remotePlayerServer;
    private final ServerSocket s0;

    private boolean winnerIsSet = false;

    public final static int TCP_PORT = 5108;

    /*
     * It might be a bit overkill but better safe than sorry...
     */
    private final  int INDEX_JASS_COMMAND = 0;

    private final  int INDEX_TRUMP = 1;
    private final  int INDEX_HAND = 1;
    private final  int INDEX_TRICK = 1;
    private final  int INDEX_SCORE = 1;
    private final  int INDEX_WINNING_PLAYER = 1;

    private final  int INDEX_PLAYER_ID = 1;
    private final  int INDEX_PLAYERS_NAMES = 2;

    private final  int INDEX_TURN_STATE = 1;
    private final  int INDEX_TURN_STATE_SCORE = 0;
    private final  int INDEX_TURN_STATE_UNPLAYED_CARDS = 1;
    private final  int INDEX_TURNS_STATE_TRICK = 2;
    private final  int INDEX_CARD_SET = 2;


    /**
     * Build the player server
     *
     * @param ownId the id of the player using the server
     * @throws Exception
     */
    public RemotePlayerServer(Player ownId) throws Exception {
        remotePlayerServer = ownId;
        s0 = new ServerSocket(TCP_PORT);
    }

    /**
     * Runs the server
     */
    public void run() {
        try (Socket s = s0.accept();
             BufferedReader r =
                     new BufferedReader(
                             new InputStreamReader(s.getInputStream(),
                                     US_ASCII));
             BufferedWriter w =
                     new BufferedWriter(
                             new OutputStreamWriter(s.getOutputStream(),
                                     US_ASCII))) {

            while (!winnerIsSet) {
                String message = r.readLine();
                String[] splitSerializedMessage = StringSerializer.splitString(' ', message);
                JassCommand command = JassCommand.valueOf(splitSerializedMessage[INDEX_JASS_COMMAND]);
                response(command, splitSerializedMessage, w);
            }

        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /*
     * Convert the serialized message into a map, linking the PlayerId to a String.
     *
     * @param s the String to convert
     * @return a map associating the PlayerIds to their names
     */
    private Map<PlayerId, String> serializerToMap(String[] s) {
        Map<PlayerId, String> players = new HashMap<>();

        String[] names = StringSerializer.splitString(',', s[INDEX_PLAYERS_NAMES]);

        for(PlayerId p : PlayerId.ALL) {
            players.put(p, StringSerializer.deserializeString(names[p.ordinal()]));
        }

        return players;
    }

    /*
     * Convert the given String into a PlayerId
     *
     * @param s the String to convert
     * @return a PlayerId
     */
    private PlayerId playerId(String s) {
        return PlayerId.ALL.get(StringSerializer.deserializeInt(s));
    }

    /*
     * Convert the given String into a Score
     *
     * @param s the String to convert
     * @return a Score
     */
    private Score score(String s) {
        return Score.ofPacked(StringSerializer.deserializeLong(s));
    }

    /*
     * Convert the given String into a CardSet
     *
     * @param s the String to convert
     * @return a CardSet containing the cards that haven't been played yet
     */
    private CardSet unplayedCards(String s) {
        return CardSet.ofPacked(StringSerializer.deserializeLong(s));
    }

    /*
     * Convert the given String into a Trick
     *
     * @param s the String to convert
     * @return a Trick
     */
    private Trick trick(String s) {
        return Trick.ofPacked(StringSerializer.deserializeInt(s));
    }

    /*
     * Given a JassCommand, respond accordingly
     *
     * PLRS : sets the players for the game (the Id of the remotePlayerServer
     * is given at index 1 of the SplitMessage) and the names of the player are given at index 2,
     * starting with the Id of the remote player.
     *
     * TRMP : sets the colour of the trump for the game
     *
     * HAND : sets the hand of the player
     *
     * TRCK : updates the trick of the current game
     *
     * CARD : return the card to play, given the turnState at index 1 (the score, the unplayedCard
     * and the trick can be found in the serialized message) and the hand of cards of the remote player
     * at index 2 of the SplitMessage
     *
     * SCOR : updates the score of the game, the score is at index 2 of the SplitMessage
     *
     * WINR : sets the winningTeam, which is at index 1 of the SplitMessage
     *
     * @param command the given JassCommand
     * @param splitMessage a tab containing the original message split with ' '
     * @param w the BufferWriter
     */
    private void response(JassCommand command, String[] splitMessage, BufferedWriter w) {
        switch (command) {
            case PLRS:
                remotePlayerServer.setPlayers(playerId(splitMessage[INDEX_PLAYER_ID]),
                        serializerToMap(splitMessage));
                break;
            case TRMP:
                remotePlayerServer.setTrump(
                        Card.Color.ALL.get(
                                StringSerializer.deserializeInt(
                                        splitMessage[INDEX_TRUMP])));
                break;
            case HAND:
                remotePlayerServer.updateHand(
                        CardSet.ofPacked(
                                StringSerializer.deserializeLong(
                                        splitMessage[INDEX_HAND])));
                break;
            case TRCK:
                remotePlayerServer.updateTrick(trick(splitMessage[INDEX_TRICK]));
                break;
            case CARD:
                String[] turnState = StringSerializer.splitString(',', splitMessage[INDEX_TURN_STATE]);
                int pkCard = remotePlayerServer.cardToPlay(
                        TurnState.ofPackedComponents(
                                score(turnState[INDEX_TURN_STATE_SCORE]).packed(),
                                unplayedCards(turnState[INDEX_TURN_STATE_UNPLAYED_CARDS]).packed(),
                                trick(turnState[INDEX_TURNS_STATE_TRICK]).packed()),
                        CardSet.ofPacked(
                                StringSerializer.deserializeLong(
                                        splitMessage[INDEX_CARD_SET]))).packed();
                try {
                    w.write(StringSerializer.serializeInt(pkCard));
                    w.write("\n");
                    w.flush();
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
                break;
            case SCOR:
                remotePlayerServer.updateScore(score(splitMessage[INDEX_SCORE]));

                break;
            case WINR:
                winnerIsSet = true;
                remotePlayerServer.setWinningTeam(
                        TeamId.ALL.get(
                                StringSerializer.deserializeInt(
                                       splitMessage[INDEX_WINNING_PLAYER])));
                break;
        }
    }
}
