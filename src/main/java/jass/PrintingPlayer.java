package main.java.jass;

import java.util.Map;

/**
 * A printed Player, prints all the useful information during a game
 *
 * @author Kilian Schneiter (287496)
 * @author Capucine Berger(269503)
 */

public final class PrintingPlayer implements Player{
        private final Player underlyingPlayer;

        private static final int NUMBER_OF_PLAYERS = 4;

        public PrintingPlayer(Player underlyingPlayer) {
            this.underlyingPlayer = underlyingPlayer;
        }

        @Override
        public Card cardToPlay(TurnState state, CardSet hand) {
            System.out.print("C'est à moi de jouer... Je joue : ");
            Card c = underlyingPlayer.cardToPlay(state, hand);
            System.out.println(c);
            return c;
        }

        @Override
        public void setPlayers(PlayerId ownId, Map<PlayerId, String> playerNames) {
            System.out.println("Les joueurs sont : ");
            for(int i = 0; i < NUMBER_OF_PLAYERS; i++) {
                if(PlayerId.ALL.get(i) == ownId) {
                    System.out.println("\tPlayer_" + (i + 1) + " (moi)");
                } else {
                    System.out.println("\tPlayer_" + (i + 1));
                }
            }
        }

        @Override
        public void updateHand(CardSet newHand) {
            System.out.println("Ma nouvelle main: " + newHand.toString());
        }

        @Override
        public void setTrump(Card.Color trump) {
            System.out.println("Atout: " + trump.toString());
        }

        @Override
        public void updateTrick(Trick newTrick) {
            System.out.println("Pli " + newTrick.index() + ", commencé par " +  getPlayer(newTrick.packed()) + " : " + newTrick.toString());
        }

        @Override
        public void updateScore(Score score) {
            System.out.println("Score: " + score.toString());
        }

        @Override
        public void setWinningTeam(TeamId winningTeam) {
            System.out.println("Equipe gagnante: " + winningTeam.toString());
        }

        private static PlayerId getPlayer(int pkTrick) {
            return PackedTrick.player(pkTrick, 0);
        }

}
