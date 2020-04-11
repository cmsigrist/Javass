package ch.epfl.javass;

import ch.epfl.javass.gui.GraphicalPlayerAdapter;
import ch.epfl.javass.net.RemotePlayerServer;
import javafx.application.Application;
import javafx.stage.Stage;

/**
 * The class used to launch a game of Jass for a remote player
 *
 * @author Kilian Schneiter (287496)
 * @author Capucine Berger(269503)
 */

public final class RemoteMain extends Application {

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
        RemotePlayerServer remote = new RemotePlayerServer(new GraphicalPlayerAdapter());

        Thread gameThread = new Thread(() -> {
            try {
                System.out.println("La partie commencera à la connexion du client...");
                remote.run();
            } catch (Exception e) {
                System.err.println("Praublaim taichnike de cheaunnexction (Aussi connu sous le nom de problème technique de connexion...)");
            }

        });
            gameThread.setDaemon(true);
            gameThread.start();
    }
}
