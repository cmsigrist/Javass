package ch.epfl.javass;

/**
 * Preconditions
 *
 * @author Kilian Schneiter (287496)
 * @author Capucine Berger(269503)
 */

public final class Preconditions {

    private Preconditions() {}

    /**
     * If the condition is false, throw an IllegalArgumentException
     *
     *  @param b the condition that needs to be checked
     *  @throws IllegalArgumentException if b is false
     */
    public static void checkArgument(boolean b) {
        if(!b) {
            throw new IllegalArgumentException();
        }
    }

    /**
     * Throw an Exception if the argument is negative or greater or equal to the size,
     * else returns the index
     *
     * @param index the index that we want to verify
     * @param size the maximum size that the index can have
     * @returns the index if it is in the interval
     * @throws IndexOutOfBoundsException if the index is not correct
     *
     */
    public static int checkIndex(int index, int size) {
        if(index < 0 || index >= size) {
            throw new IndexOutOfBoundsException();
        } else {
            return index;
        }
    }

}
