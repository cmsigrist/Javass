package ch.epfl.javass.bits;

import ch.epfl.javass.Preconditions;

/**
 * A class to manipulate Longs
 *
 * @author Kilian Schneiter (287496)
 * @author Capucine Berger(269503)
 */

public final class Bits64 {

    private Bits64() {}

    /**
     * Method that fills the indexes between start and start + size with ones, the rest are zeros
     *
     * @param start is where in the bits 'size' one will be put
     * @param size is the number of 1's
     * @return a bit filled with one's between start and start + size and zero's otherwise
     * @throws IllegalArgumentException if start or size are negative or greater than Integer.SIZE or if start + size
     *         is greater than Integer.SIZE
     */
    public static long mask(int start, int size) {
        checkStartAndSize(start, size);

        if (size == Long.SIZE) {
            return ~0L;
        }
        return ((1L << size) - 1L) << (start);
    }

    /**
     * Extract the 'size' lsb of a bit
     *
     * @param bits the bit to extract from
     * @param start is the index where the method start to extract the lsb
     * @param size is the number of lsb extracted
     * @return an int
     * @throws IllegalArgumentException if start or size are negative or greater than Integer.SIZE or if start + size
     *         is greater than Integer.SIZE
     */
    public static long extract(long bits, int start, int size) {
        checkStartAndSize(start, size);

        return (bits << (Long.SIZE - (start + size)) >>> (Long.SIZE - size));
    }

    /**
     * Pack the arguments into an int, v1 represents the lsb and v2 the following bits
     *
     * @param v1 an int that fills the lsb
     * @param s1 the size of v1
     * @param v2 an int that fills the bits following the 's1' lsb
     * @param s2 the size of v2
     * @return a packed int
     * @throws IllegalArgumentException if s1 + s2 exceeds Integer.SIZE bits
     */
    public static long pack(long v1, int s1, long v2, int s2) {
        checkArgument(v1, s1);
        checkArgument(v2, s2);
        Preconditions.checkArgument(s1 + s2 <= Long.SIZE);

        return (v2 << s1) | v1;
    }
    /*
     * Private method to check the validity of the index (if it's inside of the bounds or not),
     * and the range of the value must be strictly inferior to 2^31
     *
     * @param value, an int that must be between 0 and 2^31
     * @param size, the size must in between 0 and 31 bits
     * @throws IllegalArgumentException if the above condition are not respected
     *
     */
    private static void checkArgument(long value, int size) {
        Preconditions.checkArgument(size >= 0);
        Preconditions.checkArgument(size < Long.SIZE);
        Preconditions.checkArgument(value <= Long.MAX_VALUE - 1);
        Preconditions.checkArgument(value >>> size == 0);
    }

    /*
     * Checks if the index of the starting bit and the size of the bit string are correct
     *
     * @param start the starting index
     * @param size the size of the bit string
     */
    private static void checkStartAndSize(int start, int size) {
        Preconditions.checkArgument(start >= 0);
        Preconditions.checkArgument(start <= Long.SIZE);
        Preconditions.checkArgument(size >= 0);
        Preconditions.checkArgument(size <= Long.SIZE);
        Preconditions.checkArgument(size + start <= Long.SIZE);
    }
}
