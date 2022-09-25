package main.java.jass;

import java.util.Random;

/**
 * A random Player (plays card randomly)
 *
 * @author Kilian Schneiter (287496)
 * @author Capucine Berger(269503)
 */

public final class RandomPlayer implements Player {
        private final Random rng;

        public RandomPlayer(long rngSeed) {
            this.rng = new Random(rngSeed);
        }

        @Override
        public Card cardToPlay(TurnState state, CardSet hand) {
            CardSet playable = state.trick().playableCards(hand);
            return playable.get(rng.nextInt(playable.size()));
        }
}

