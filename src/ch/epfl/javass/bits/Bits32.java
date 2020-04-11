package ch.epfl.javass.bits;

import ch.epfl.javass.Preconditions;

/**
 * A class to manipulate Integers
 *
 * @author Kilian Schneiter (287496)
 * @author Capucine Berger(269503)
 */

public final class Bits32 {

    private Bits32(){}

    /**
     * Method that fills the indexes between start and start + size with one's, the rest are zero's
     *
     * @param start the index of the starting bit
     * @param size the size that the mask needs to have
     * @return a bit filled with one's between start and start + size and zero's otherwise
     * @throws IllegalArgumentException if start or size are negative or greater than Integer.SIZE or if start + size
     *         is greater than Integer.SIZE
     */
    public static int mask(int start, int size) {
        checkStartAndSize(start, size);

        if (size == Integer.SIZE) {
            return ~0;
        }

        return ((1 << size) - 1) << (start);
    }

    /**
     * Extract the 'size' lsb of a bit
     *
     * @param bits the bit string
     * @param start is the index where the method start to extract the lsb
     * @param size is the number of lsb extracted
     * @return an int
     * @throws IllegalArgumentException if start or size are negative or greater than Integer.SIZE or if start + size
     *         is greater than Integer.SIZE
     */
    public static int extract(int bits, int start, int size) {
        checkStartAndSize(start, size);

        return (bits << (Integer.SIZE - (start + size)) >>> (Integer.SIZE - size));
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
    public static int pack(int v1, int s1, int v2, int s2) {
        checkArgument(v1, s1);
        checkArgument(v2, s2);
        Preconditions.checkArgument(s1 + s2 <= Integer.SIZE);

        return (extract(v2, 0, s2) << s1) | extract(v1, 0, s1);
    }

    /**
     * Pack the argument into an int
     *
     * @param v1 an int filling the 's1' first bits
     * @param s1 the size of v1
     * @param v2 an int occupying after v1 the 's2' next bits
     * @param s2 the size of v2
     * @param v3 an int occupying after v2 the 's3' next bits
     * @param s3 the size of v3
     * @return a packed int containing v1, v2 and v3, and of size v1 + v2 + v3
     * @throws IllegalArgumentException if s1 + s2 + s3 are greater than Interger.SIZE (= 32)
     */
    public static int pack(int v1, int s1, int v2, int s2, int v3, int s3) {
        checkArgument(v1, s1);
        checkArgument(v2, s2);
        checkArgument(v3, s3);
        Preconditions.checkArgument(s1 + s2 + s3 <= Integer.SIZE);

        return (extract(v3, 0, s3) << (s1 + s2) | extract(v2, 0, s2) << s1) | extract(v1, 0, s1);
    }

    /**
     * Pack the argument into an int
     *
     * @param v1 an int occupying the 's1' first bits
     * @param s1 the size of v1
     * @param v2 an int occupying after v1 the 's2' next bits
     * @param s2 the size of v2
     * @param v3 an int occupying after v2 the 's3' next bits
     * @param s3 the size of v3
     * @param v4 an int occupying after v3 the 's4' next bits
     * @param s4 the size of v4
     * @param v5 an int occupying after v4 the 's5' next bits
     * @param s5 the size of v5
     * @param v6 an int occupying after v5 the 's6' next bits
     * @param s6 the size of v6
     * @param v7 an int occupying after v6 the 's7' next bits
     * @param s7 the size of v7
     * @return a packed int containing all the int from v1 to v7 of size v1 + v2 + v3 + v4 + v5 + v6 + v7
     * @throws IllegalArgumentException if s1 + s2 + s3 + s4 + s5 + s6 + s7 are greater than Integer.SIZE(= 32)
     */
    public static int pack(int v1, int s1, int v2, int s2, int v3, int s3, int v4, int s4, int v5, int s5, int v6,
                           int s6, int v7, int s7) {
        checkArgument(v1, s1);
        checkArgument(v2, s2);
        checkArgument(v3, s3);
        checkArgument(v4, s4);
        checkArgument(v5, s5);
        checkArgument(v6, s6);
        checkArgument(v7, s7);
        Preconditions.checkArgument(s1 + s2 + s3 + s4 + s5 + s6 + s7 <= Integer.SIZE);

        return (extract(v7, 0, s7) << (s1 + s2 + s3 + s4 + s5 + s6) |
                extract(v6, 0, s6) << (s1 + s2 + s3 + s4 + s5) |
                extract(v5, 0, s5) << (s1 + s2 + s3 + s4) |
                extract(v4, 0, s4) << (s1 + s2 + s3) |
                extract(v3, 0, s3) << (s1 + s2) |
                extract(v2, 0, s2) << s1) |
                extract(v1, 0, s1);
    }

    /*
     * Private method to check the validity of the index (if it's inside of the bounds or not),
     * and the range of the value must be strictly inferior to 2^31
     *
     * @param value, an int that must be between 0 and 2^31
     * @param size, the size must in between 0 and 31 bits
     * @throws IllegalArgumentException if the above condition are not respected
     */
    private static void checkArgument(int value, int size) {
        Preconditions.checkArgument(size >= 0);
        Preconditions.checkArgument(size < Integer.SIZE);
        Preconditions.checkArgument(value <= Integer.MAX_VALUE - 1);
        Preconditions.checkArgument(value >>> size == 0);
    }

    /*
     * Checks if the starting index and the size of a bit string are correct
     *
     * @param start the starting index
     * @param size the size of the bit string
     */
    private static void checkStartAndSize(int start, int size) {
        Preconditions.checkArgument(start >= 0);
        Preconditions.checkArgument(start <= Integer.SIZE);
        Preconditions.checkArgument(size >= 0);
        Preconditions.checkArgument(size <= Integer.SIZE);
        Preconditions.checkArgument(size + start <= Integer.SIZE);
    }
}
