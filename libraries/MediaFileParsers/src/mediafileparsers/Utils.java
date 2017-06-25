package mediafileparsers;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.nio.charset.Charset;

/**
 * Created by Ville on 15.6.2017.
 */
public class Utils {

    /**
     * Reads one 32 bit long integer encoded in little-endian from given buffered input stream and saves each byte into given byte array.
     *
     * @param bInput    input stream to read the bytes from
     * @param byteArray array to store read bytes for later use
     * @return
     * @throws IOException can be thrown by bInput.read() method. See java.io.BufferedInputStream.read()
     */
    public static long read32BitIntegerBE(BufferedInputStream bInput, int[] byteArray) throws IOException {
        int oneChar = 0;
        for (int i = 0; i < 4; i++) {

            // Read one byte
            oneChar = bInput.read();

            // If byte is -1 break the loop
            // TODO: This needs better implementation!
            if (oneChar == -1) {
                break;
            }

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
    public static long read32BitIntegerBE(String s) {
        byte[] array = s.getBytes(Charset.forName("ASCII"));
        return read32BitIntegerBE(array);
//        long integer = 0;
//
//        // Combine the bytes into a single 32 bit integer
//        for (int i = 0; i < 4 - 1; i++) {
//            integer += s.charAt(i) << 24 - (i * 8);
////            System.out.println("Char = " + (int)s.charAt(i));
////            System.out.println("Integer = " + integer);
//        }
//
//        // Add the last byte into the result integer
//        integer += s.charAt(4 - 1);
////        System.out.println("Char = " + (int)s.charAt(4 - 1));
////        System.out.println("Integer = " + integer);
//        return integer;
    }

    /**
     * Reads one 32 bit long integer encoded in big-endian from given byte array
     *
     * @param byteArray
     * @return
     */
    public static long read32BitIntegerBE(byte[] byteArray) {
        long integer = 0;

        // Combine the bytes into a single 32 bit integer
        for (int i = 0; i < 4 - 1; i++) {
            integer += byteArray[i] << 24 - (i * 8);
        }

        // Add the last byte into the result integer
        integer += byteArray[3];
        return integer;
    }

    /**
     * Reads one 32 bit long integer encoded in big-endian from given string
     *
     * @param s
     * @return
     */
    public static long read32BitIntegerLE(String s) {
        byte[] sBytes = s.getBytes(Charset.forName("ASCII"));
        return read32BitIntegerLE(sBytes);
    }

    /**
     * Reads one 32 bit long integer encoded in big-endian from given byte array
     *
     * @param byteArray
     * @return
     */
    public static long read32BitIntegerLE(byte[] byteArray) {
        long integer = 0;

        // Combine the bytes into a single 32 bit integer
        for (int i = 3; i > 0; i--) {
            integer += byteArray[i] << 24 - ((3 - i) * 8);
        }

        // Add the first byte into the result integer
        integer += byteArray[0];
        return integer;
    }

    /**
     * Creates a 32 bit byte array from given integer value in big-endian. e.g. int 257 would translate to 0 0 1 1
     * @param value integer to be parsed to the array
     * @return byte array with four items
     */
    public static byte[] create32BitByteArrayBE(int value) {
        byte[] result = new byte[4];

        for (int i = 0; i < result.length; i++) {
            result[i] = (byte) (value >> 24 - (i * 8));
        }

        return result;
    }

    /**
     * Creates a 32 bit byte array from given integer value in little-endian. e.g. int 257 would translate to 1 1 0 0
     * @param value integer to be parsed to the array
     * @return byte array with four items
     */
    public static byte[] create32BitByteArrayLE(int value) {
        byte[] result = new byte[4];

        for (int i = 0; i < result.length; i++) {
            result[i] = (byte) (value >> 24 - ((3-i) * 8));
        }

        return result;
    }
}
