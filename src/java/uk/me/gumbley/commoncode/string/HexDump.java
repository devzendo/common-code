package uk.me.gumbley.commoncode.string;

import java.nio.ByteBuffer;
import java.util.ArrayList;

/**
 * Hexadecimal routines
 * @author matt
 *
 */
public class HexDump {
    private HexDump() {
        super();
    }

    private static String HEXDIGS = "0123456789ABCDEF";
    public static String byte2hex(final byte b) {
        StringBuilder sb = new StringBuilder();
        appendHexByte(sb, b);
        return sb.toString();
    }
    public static String bytes2hex(final byte[] bs) {
        StringBuilder sb = new StringBuilder();
        for (int i=0; i<bs.length; i++) {
            appendHexByte(sb, bs[i]);
        }
        return sb.toString();
    }
    private static void appendHexByte(final StringBuilder sb, final byte b) {
        sb.append(HEXDIGS.charAt((b & 0xf0) >> 4));
        sb.append(HEXDIGS.charAt(b & 0x0f));
    }

    public static String long2hex(final long l) {
        long d = l;
        char[] buf = new char[16];
        long mask = 0x0f;
        for (int x = 15; x >= 0; x--) {
            buf[x] = HEXDIGS.charAt((int)(d & mask));
            d >>>= 4;
        }
        return new String(buf);
    }
    public static String int2hex(final int i) {
        int d = i;
        char[] buf = new char[8];
        long mask = 0x0f;
        for (int x = 7; x >= 0; x--) {
            buf[x] = HEXDIGS.charAt((int)(d & mask));
            d >>>= 4;
        }
        return new String(buf);
    }
    public static String short2hex(final short s) {
        short d = s;
        char[] buf = new char[4];
        long mask = 0x0f;
        for (int x = 3; x >= 0; x--) {
            buf[x] = HEXDIGS.charAt((int)(d & mask));
            d >>>= 4;
        }
        return new String(buf);
    }
    public static String[] hexDump(final byte[] buffer, final int startOffset, final int bufferLength) {
        int offset = startOffset;
        ArrayList < String > lines = new ArrayList < String > ();
        StringBuilder line = new StringBuilder(80);

        for (int i = 0; i < 78; i++) {
            line.append(' ');
        }

        int left = bufferLength - startOffset;
        int upto16;
        byte b;

        while (left > 0) {
            for (int i = 0; i < 78; i++) {
                line.setCharAt(i, ' ');
            }

            line.setCharAt(9, '|');
            line.setCharAt(59, '|');
            line.replace(0, 8, int2hex(offset));
            upto16 = (left > 16) ? 16 : left;

            for (int x = 0; x < upto16; x++) {
                b = buffer[offset + x];
                line.setCharAt(11 + (3 * x), HEXDIGS.charAt((b & 0xf0) >> 4));
                line.setCharAt(12 + (3 * x), HEXDIGS.charAt(b & 0x0f));
                line.setCharAt(61 + x, (b >= 32 && b <= 126) ? (char) b : '.');
            }

            lines.add(line.toString());
            offset += 16;
            left -= 16;
        }

        return lines.toArray(new String[0]);
    }

    public static String[] hexDump(final ByteBuffer buffer) {
        int len = buffer.remaining();
        byte[] buf = new byte[len];
        int startPosition = buffer.position();
        buffer.get(buf);
        String[] ret = hexDump(buf, 0, len);
        buffer.position(startPosition);
        return ret;
    }
    
    private static byte hex2byte(final String h) {
        return (byte) ((nibble2decimal(h.charAt(0)) << 4) | (nibble2decimal(h.charAt(1))));
    }
    private static byte nibble2decimal(final char n) {
        return (byte) ((n >= 0x30 && n <= 0x39) ? n - 0x30 :
            n - 'A' + 10);
    }
}
