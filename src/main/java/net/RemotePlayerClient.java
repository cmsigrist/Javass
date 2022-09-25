package main.java.net;

import main.java.jass.*;

import java.io.*;
import java.net.Socket;
import java.util.Map;

import static java.nio.charset.StandardCharsets.US_ASCII;

/**
 * A class used to build a remote player client
 *
 * @author Kilian Schneiter (287496)
 * @author Capucine Berger(269503)
 */

public final class RemotePlayerClient implements Player, AutoCloseable {

    private final Socket socket;
    private final BufferedReader r;
    private final BufferedWriter w;
    private String message;

    /**
     * Build the player client
     *
     * @param localhost the IP address of the localhost
     */
    public RemotePlayerClient(String localhost) {
        // Try establishing connection
        try {
            socket = new Socket(localhost, RemotePlayerServer.TCP_PORT);
            this.r = new BufferedReader(
                        new InputStreamReader(socket.getInputStream(), US_ASCII));
            this.w = new BufferedWriter(
                        new OutputStreamWriter(socket.getOutputStream(), US_ASCII));

        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /**
     * Given the turnState and the hand, plays a card
     *
     * @param state the state of the turn
     * @param hand the hand of the client player
     * @return The Card the client must play
     */
    @Override
    public Card cardToPlay(TurnState state, CardSet hand) {
        message = StringSerializer.combineSerializedString(' ',
                JassCommand.CARD.name(),
                StringSerializer.combineSerializedString(',',
                        StringSerializer.serializeLong(state.packedScore()),
                        StringSerializer.serializeLong(state.packedUnplayedCards()),
                        StringSerializer.serializeInt(state.packedTrick())),
                StringSerializer.serializeLong(hand.packed()));
        send(message);

        int pkCard;
        try {
            pkCard = StringSerializer.deserializeInt(r.readLine());
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        } catch (Exception e) {
            throw new NumberFormatException();
        }
        return Card.ofPacked(pkCard);
    }

    /**
     * Sets all the players
     *
     * @param ownId the PlayerId of the client player
     * @param playerNames the name of all the players
     */
    @Override
    public void setPlayers(PlayerId ownId, Map<PlayerId, String> playerNames) {
        message = StringSerializer.combineString(',',
                playerNames.get(PlayerId.PLAYER_1),
                playerNames.get(PlayerId.PLAYER_2),
                playerNames.get(PlayerId.PLAYER_3),
                playerNames.get(PlayerId.PLAYER_4));
        message = StringSerializer.combineSerializedString(' ',
                JassCommand.PLRS.name(),
                StringSerializer.serializeInt(ownId.ordinal()), message);
        send(message);
    }

    /**
     * Updates the hand of the player
     *
     * @param newHand a CardSet containing new hand of the player
     */
    @Override
    public void updateHand(CardSet newHand) {
        message = StringSerializer.combineSerializedString(' ',
                JassCommand.HAND.name(),
                StringSerializer.serializeLong(newHand.packed()));
        send(message);
    }

    /**
     * Sets the trump for the turn
     *
     * @param trump the Color of the trump
     */
    @Override
    public void setTrump(Card.Color trump) {
        message = StringSerializer.combineSerializedString(' ',
                JassCommand.TRMP.name(),
                StringSerializer.serializeInt(trump.ordinal()));
        send(message);
    }

    /**
     * Updates the trick
     *
     * @param newTrick the new Trick
     */
    @Override
    public void updateTrick(Trick newTrick) {
        message = StringSerializer.combineSerializedString(' ',
                JassCommand.TRCK.name(),
                StringSerializer.serializeInt(newTrick.packed()));
        send(message);
    }

    /**
     * Updates the score of the game
     *
     * @param score the new Score of the game
     */
    @Override
    public void updateScore(Score score) {
        message = StringSerializer.combineSerializedString(' ',
                JassCommand.SCOR.name(),
                StringSerializer.serializeLong(score.packed()));
        send(message);
    }

    /**
     * Sets the winning team of the game
     *
     * @param winningTeam the TEamId of the winning team
     */
    @Override
    public void setWinningTeam(TeamId winningTeam) {
        message = StringSerializer.combineSerializedString(' ',
                JassCommand.WINR.name(),
                StringSerializer.serializeInt(winningTeam.ordinal()));
        send(message);
    }

    /**
     * Closes the Buffer Reader, writes and closes the socket
     *
     * @throws Exception
     */
    @Override
    public void close() throws Exception {
        r.close();
        w.close();
        socket.close();
    }

    /*
     * Sends the given String to the server
     *
     * @param message the String containing the message
     */
    private void send(String message) {
        try {
            w.write(message);
            w.write("\n");
            w.flush();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
