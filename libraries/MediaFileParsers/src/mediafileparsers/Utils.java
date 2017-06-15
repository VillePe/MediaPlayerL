package mediafileparsers;

import java.io.BufferedInputStream;
import java.io.IOException;

/**
 * Created by Ville on 15.6.2017.
 */
public class Utils {

    /**
     * Reads one 32 bit long integer encoded in little-endian from given buffered input stream and saves each byte into given byte array.
     *
     * @param bInput
     * @param byteArray
     * @return
     * @throws IOException
     */
    public static long read32BitIntegerLE(BufferedInputStream bInput, int[] byteArray) throws IOException {
        int oneChar = 0;
        for (int i = 0; i < 4; i++) {

            // Read one byte
            oneChar = bInput.read();

            // If byte is -1 break the loop
            // TODO: This needs better implementation!
            if (oneChar == -1) {
                break;
            }

            // Take one byte to check that the reading was successful
            byteArray[i] = oneChar;
        }
        if (oneChar == -1) {
            return -1;
        }
        long integer = 0;

        // Combine the bytes into a single 32 bit integer
        for (int i = 0; i < 4 - 1; i++) {
            integer += byteArray[i] << 24 - (i * 8);
        }

        // Add the last byte into the result integer
        integer += byteArray[4 - 1];
        return integer;
    }

    /**
     * Reads one 32 bit long integer encoded in little-endian from given string
     *
     * @param s
     * @return
     */
    public static long read32BitIntegerLE(String s) {
        long integer = 0;

        // Combine the bytes into a single 32 bit integer
        for (int i = 0; i < 4 - 1; i++) {
            integer += s.charAt(i) << 24 - (i * 8);
//            System.out.println("Char = " + (int)s.charAt(i));
//            System.out.println("Integer = " + integer);
        }

        // Add the last byte into the result integer
        integer += s.charAt(4 - 1);
//        System.out.println("Char = " + (int)s.charAt(4 - 1));
//        System.out.println("Integer = " + integer);
        return integer;
    }

    /**
     * Reads one 32 bit long integer encoded in big-endian from given string
     *
     * @param s
     * @return
     */
    public static long read32BitIntegerBE(String s) {
        long integer = 0;

        // Combine the bytes into a single 32 bit integer
        for (int i = 3; i > 0; i--) {
            integer += s.charAt(i) << 24 - ((3 - i) * 8);
//            System.out.println("Char = " + (int) s.charAt(i));
//            System.out.println("Integer = " + integer);
        }

        // Add the first bit into the result integer
        integer += s.charAt(0);
//        System.out.println("Char = " + (int) s.charAt(0));
//        System.out.println("Integer = " + integer);
        return integer;
    }


}
