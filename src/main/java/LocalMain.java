package main.java;

import main.java.gui.GraphicalPlayerAdapter;
import main.java.jass.*;
import main.java.net.RemotePlayerClient;
import javafx.application.Application;
import javafx.stage.Stage;

import java.io.UncheckedIOException;
import java.util.*;

/**
 * The class containing the main program, used to launch a game of Jass
 *
 * @author Kilian Schneiter (287496)
 * @author Capucine Berger(269503)
 */

public final class LocalMain extends Application {
    private final int ARG_SIZE = 4;
    private final int ARG_SIZE_WITH_SEED = 5;

    private final int INDEX_TYPE = 0;
    private final int INDEX_NAME = 1;
    private final int S_INDEX_ITERATIONS = 2;
    private final int R_INDEX_HOST = 2;
    private final int INDEX_SEED = 4;

    private final int H_NUMBER_OF_ARG_BOUND = 3;
    private final int R_NUMBER_OF_ARG_BOUND = 4;
    private final int S_NUMBER_OF_ARG_BOUND = 4;

    private final int DEFAULT_ITERATIONS = 10000;
    private final int MIN_ITERATIONS = 10;

    private final int EXIT_STATUS = 1;

    private final int PACED_PLAYER_TIME = 2;
    private final int SLEEP_TIME = 2000;

    public static void main(String[] args) { launch(args); }

    /**
     * The main entry point for all JavaFX applications.
     * The start method is called after the init method has returned,
     * and after the system is ready for the application to begin running.
     *
     * <p>
     * NOTE: This method is called on the JavaFX Application Thread.
     * </p>
     *
     * @param primaryStage the primary stage for this application, onto which
     *                     the application scene can be set. The primary stage will be embedded in
     *                     the browser if the application was launched as an applet.
     *                     Applications may create other stages, if needed, but they will not be
     *                     primary stages and will not be embedded in the browser.
     */
    @Override public void start(Stage primaryStage) throws Exception {
        List<String> args = new ArrayList<>(getParameters().getRaw());

        if(args.size() != ARG_SIZE && args.size() != ARG_SIZE_WITH_SEED) {
            String help = "Utilisation: java main.java.LocalMain <j1>…<j4> [<graine>] où " +
                    "<jn> spécifie le joueur n, ainsi:\n" +
                    "\th:<nom>  un joueur humain nommé <nom>\n" +
                    "\ts:<nom>:<n_itérations>  un joueur simulé (algorithme MCTS) nommé <nom> " +
                    "qui itéreras <n_itérations> fois\n" +
                    "\tr:<nom>:<ip>  un joueur distant nommé <nom> accompagné de son adresse IP <ip>\n\n" +
                    "- <graine> spécifie la graine utilisée afin de générer des nombres aléatoires\n" +
                    "- Tous les argument entre <> sont optionnels, et la graine n'est pas obligatoire.";
            System.err.println(help);
            System.exit(EXIT_STATUS);
        }

        Random randomGenerator = new Random();

        try {
            if(args.size() == ARG_SIZE_WITH_SEED)
                randomGenerator = new Random(Long.parseLong(args.get(ARG_SIZE)));
        } catch (NumberFormatException e) {
            System.err.println("Erreur : la graine de nombre aléatoire est invalide : " + args.get(INDEX_SEED));
            System.exit(EXIT_STATUS);
        }
        long seedGame = randomGenerator.nextLong();

        Map<PlayerId, String> playerNames = new HashMap<>();
        Map<PlayerId, Player> players = new HashMap<>();

        playerNames.put(PlayerId.PLAYER_1, "Aline");
        playerNames.put(PlayerId.PLAYER_2, "Bastien");
        playerNames.put(PlayerId.PLAYER_3, "Colette");
        playerNames.put(PlayerId.PLAYER_4, "David");

        for(int i = 0; i < PlayerId.COUNT;  i++) {
            String[] splitString = args.get(i).split(":");
            PlayerId playerId = PlayerId.ALL.get(i);
            String playerType = splitString[INDEX_TYPE];

            boolean onlyTypeIsGiven = (splitString.length == 1);
            if (!onlyTypeIsGiven) {
                if (!splitString[INDEX_NAME].isEmpty())
                    playerNames.replace(playerId, splitString[INDEX_NAME]);
            }

            switch (playerType) {
                case "h":
                    checkPlayerArguments(splitString.length, H_NUMBER_OF_ARG_BOUND);
                    players.put(playerId, new GraphicalPlayerAdapter());
                    break;
                case "s":
                    checkPlayerArguments(splitString.length, S_NUMBER_OF_ARG_BOUND);
                    addSimulatedPlayer(splitString, players, playerId, randomGenerator);
                    break;
                case "r":
                    checkPlayerArguments(splitString.length, R_NUMBER_OF_ARG_BOUND);
                    addRemotePlayer(splitString, players, playerId);
                    break;
                default:
                    System.err.println("Erreur : spécification du joueur invalide : " + splitString[INDEX_TYPE]);
                    System.exit(EXIT_STATUS);
                    break;
            }
        }

        Thread gameThread = new Thread(() -> {
            JassGame g = new JassGame(seedGame, players, playerNames);
            while (! g.isGameOver()) {
                g.advanceToEndOfNextTrick();
                try { Thread.sleep(SLEEP_TIME); } catch (Exception e) {}
            }
        });

        gameThread.setDaemon(true);
        gameThread.start();
    }

    /*
     * Creates a simulated player and adds it to the game. If parseInt throws a NumberFormatException,
     * we catch it, stop the program and write in System.err. We do the same for checkArgument and
     * the IllegalArgumentException.
     *
     * @param splitString the given arguments specified in the run configuration
     * @param players the map containing the players and their player id
     * @param playerId the player id of the simulated player
     * @param random the random used to get a seed
     */
    private void addSimulatedPlayer(String[] splitString, Map<PlayerId, Player> players,
                                       PlayerId playerId, Random random) {
        try {
            int iterations = DEFAULT_ITERATIONS;
            boolean isIterationsGiven = splitString.length > 2;

            if(isIterationsGiven)
                iterations = Integer.parseInt(splitString[S_INDEX_ITERATIONS]);

            players.put(playerId, new PacedPlayer(new MctsPlayer(playerId, random.nextLong(),
                    iterations), PACED_PLAYER_TIME));
            Preconditions.checkArgument(iterations >= MIN_ITERATIONS);
        } catch (NumberFormatException e) {
            System.err.println("Erreur : Le nombre d'itérations n'est pas un entier valide : "
                    + splitString[S_INDEX_ITERATIONS]);
            System.exit(EXIT_STATUS);
        } catch (IllegalArgumentException a) {
            System.err.println("Erreur : Le nombre d'itérations est inférieur à " + MIN_ITERATIONS);
            System.exit(EXIT_STATUS);
        }
    }

    /*
     * Creates a remote player and adds it to the game. If the remote player cannot connect to the
     * client (it throws an UncheckedIOException), we catch the exception, stop the program and write
     * an error in System.err.
     *
     * @param splitString the given arguments specified in the run configuration
     * @param players the map containing the players and their player id
     * @param playerId the player id of the remote player
     */
    private void addRemotePlayer(String[] splitString, Map<PlayerId, Player> players,
                                    PlayerId playerId) {
        String host;

        if (!splitString[R_INDEX_HOST].isEmpty())
            host = splitString[R_INDEX_HOST];
        else
            host = "localhost";

        try {
            players.put(playerId, new RemotePlayerClient(host));
        } catch (UncheckedIOException e) {
            System.err.println("Erreur : problème de connexion au serveur");
            System.exit(EXIT_STATUS);
        }
    }
    /*
     * Checks that the number of arguments given to a player is smaller than the given bound
     *
     * @param size : the number fo arguments
     * @param bound : the upper bound
     */
    private void checkPlayerArguments(int size, int bound) {
        try {
            Preconditions.checkIndex(size, bound);
        } catch(IndexOutOfBoundsException e) {
            System.err.println("Erreur : le nombre d'argument du joueur doit être strictement inférieur à " + bound);
            System.exit(EXIT_STATUS);
        }
    }
}
