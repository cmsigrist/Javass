package main.java.net;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * A class used to serialize and deserialize Strings
 *
 * @author Kilian Schneiter (287496)
 * @author Capucine Berger(269503)
 */

public final class StringSerializer {
    private final static int BASE_16 = 16;

    private StringSerializer() {}

    /**
     * Serializes an integer into a String
     *
     * @param toSerialise the integer to serialize
     * @return a String representing the serialized integer in base 16
     */
    public static String serializeInt(int toSerialise) {
        return Integer.toUnsignedString(toSerialise, BASE_16);
    }

    /**
     * Deserializes the integer from the given String
     *
     * @param s the String  containing the int
     * @return an int
     */
    public static int deserializeInt(String s) {
        return Integer.parseUnsignedInt(s, BASE_16);
    }

    /**
     * Serializes the given long into a String
     *
     * @param toSerialise the long to serialize
     * @return a String representing the serialized long in base 16
     */
    public static String serializeLong(long toSerialise) {
        return Long.toUnsignedString(toSerialise, BASE_16);
    }

    /**
     * Deserializes the String into a long
     *
     * @param s the String to deserialize
     * @return the long represented by the String
     */
    public static long deserializeLong(String s) {
        return Long.parseUnsignedLong(s, BASE_16);
    }

    /**
     * Serializes a String using the encoder of the cass Base64
     *
     * @param s the String to serialize
     * @return the serialized String
     */
    public static String serializeString(String s) {
        return Base64.getEncoder().encodeToString(s.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Deserializes the given String given its array of byte
     *
     * @param b the array of byte
     * @return the decoded String
     */
    public static String deserializeString(byte[] b) {
        return new String(b);
    }

    /**
     * Deserializes the given String using the decoder of the class Base64
     *
     * @param s the encoded String
     * @return the decoded String
     */
    public static String deserializeString(String s) {
        return new String(Base64.getDecoder().decode(s.getBytes(StandardCharsets.UTF_8)));
    }

    /**
     * Combines the four Strings in their serialized form and separate them using the char given in the argument
     *
     * @param c the char used to separate the Strings
     * @param s1 the first String to combine
     * @param s2 the second String to combine
     * @param s3 the third String to combine
     * @param s4 the fourth String to combine
     * @return a String combining all the above Strings separated by the char c
     */
    public static String combineString(char c, String s1, String s2, String s3, String s4) {
        return String.join(Character.toString(c),
                serializeString(s1),
                serializeString(s2),
                serializeString(s3),
                serializeString(s4));
    }

    /**
     * Combines Spring that are already serialized using the char given in the argument to separate them
     *
     * @param c the char used to separate the Strings
     * @param s1 the first String to combine
     * @param s2 the second String to combine
     * @param s3 the third String to combine
     * @return a String combining all the above Strings separated by the char c
     */
    public static String combineSerializedString(char c, String s1, String s2, String s3) {
        return String.join(Character.toString(c), s1, s2, s3);
    }

    /**
     * Combines Spring that are already serialized using the char given in the argument to separate them
     *
     * @param c the char used to separate the Strings
     * @param s1 the first String to combine
     * @param s2 the second String to combine
     * @return a String combining all the above Strings separated by the char c
     */
    public static String combineSerializedString(char c, String s1, String s2) {
        return String.join(Character.toString(c), s1, s2);
    }

    /**
     * Splits the given String along the char c given in the argument
     *
     * @param c the char used to spit the Strings
     * @param s the String to split
     * @return an array containing all the String that were split
     */
    public static String[] splitString(char c, String s) {
        return s.split(Character.toString(c)) ;
    }
}
