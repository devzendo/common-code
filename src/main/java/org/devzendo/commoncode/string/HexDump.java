/**
 * Copyright (C) 2008-2010 Matt Gumbley, DevZendo.org <http://devzendo.org>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.devzendo.commoncode.string;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

/**
 * Hexadecimal routines
 * @author matt
 *
 */
public final class HexDump {
    private HexDump() {
        super();
    }

    private static String hexDigits = "0123456789ABCDEF";
    
    /**
     * Convert a byte into its Hex String
     * @param b e.g. 127
     * @return "7F"
     */
    public static String byte2hex(final byte b) {
        final StringBuilder sb = new StringBuilder();
        appendHexByte(sb, b);
        return sb.toString();
    }
    
    /**
     * Convert a number of bytes into their Hex String
     * @param bs e.g. 127, 201
     * @return "7FC9"
     */
    public static String bytes2hex(final byte[] bs) {
        final StringBuilder sb = new StringBuilder();
        for (int i = 0; i < bs.length; i++) {
            appendHexByte(sb, bs[i]);
        }
        return sb.toString();
    }
    
    private static void appendHexByte(final StringBuilder sb, final byte b) {
        sb.append(hexDigits.charAt((b & 0xf0) >> 4));
        sb.append(hexDigits.charAt(b & 0x0f));
    }

    /**
     * Convert a long into its Hex String
     * @param l the long
     * @return a 16 char hex string
     */
    public static String long2hex(final long l) {
        long d = l;
        final char[] buf = new char[16];
        final long mask = 0x0f;
        for (int x = 15; x >= 0; x--) {
            buf[x] = hexDigits.charAt((int) (d & mask));
            d >>>= 4;
        }
        return new String(buf);
    }
    
    /**
     * Convert an int into its hex string
     * @param i the int
     * @return an 8 char hex string
     */
    public static String int2hex(final int i) {
        int d = i;
        final char[] buf = new char[8];
        final long mask = 0x0f;
        for (int x = 7; x >= 0; x--) {
            buf[x] = hexDigits.charAt((int) (d & mask));
            d >>>= 4;
        }
        return new String(buf);
    }
    
    /**
     * Convert a short into its hex string
     * @param s the short
     * @return a 4 char hex string
     */
    public static String short2hex(final short s) {
        short d = s;
        final char[] buf = new char[4];
        final long mask = 0x0f;
        for (int x = 3; x >= 0; x--) {
            buf[x] = hexDigits.charAt((int) (d & mask));
            d >>>= 4;
        }
        return new String(buf);
    }

    /**
     * Dump all of a byte array into a hex/ascii dump
     * @param buffer the byte array
     * @return an array of hex/ascii dump strings
     */
    public static String[] hexDump(final byte[] buffer) {
        return hexDump(buffer, 0, buffer.length);
    }

    /**
     * Dump some of a byte array into a hex/ascii dump
     * @param buffer the byte array
     * @param startOffset the position in the byte array to start the dump
     * @param bufferLength the number of bytes in the byte array to dump
     * @return an array of hex/ascii dump strings
     */
    public static String[] hexDump(final byte[] buffer, final int startOffset, final int bufferLength) {
        int offset = startOffset;
        final ArrayList < String > lines = new ArrayList < String > ();
        final StringBuilder line = new StringBuilder(80);

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
                line.setCharAt(11 + (3 * x), hexDigits.charAt((b & 0xf0) >> 4));
                line.setCharAt(12 + (3 * x), hexDigits.charAt(b & 0x0f));
                line.setCharAt(61 + x, (b >= 32 && b <= 126) ? (char) b : '.');
            }

            lines.add(line.toString());
            offset += 16;
            left -= 16;
        }

        return lines.toArray(new String[0]);
    }

    /**
     * Dump an entire ByteBuffer without affecting the position of the buffer
     * (it gets changed but restored by this routine)
     * @param buffer the buffer
     * @return the lines of hex/ascii dump
     */
    public static String[] hexDump(final ByteBuffer buffer) {
        final int len = buffer.remaining();
        final byte[] buf = new byte[len];
        final int startPosition = buffer.position();
        buffer.get(buf);
        final String[] ret = hexDump(buf, 0, len);
        buffer.position(startPosition);
        return ret;
    }
    
    /**
     * Convert the first two characters of a hex dump into a byte
     * @param h a string starting with 2 hex characters
     * @return the byte
     */
    public static byte hex2byte(final String h) {
        return (byte) ((nibble2decimal(h.charAt(0)) << 4) | (nibble2decimal(h.charAt(1))));
    }
    
    /**
     * Convert two hex characters into a byte
     * @param h the most significant nibble
     * @param l the least significant nibble
     * @return the byte
     */
    public static byte hex2byte(final char h, final char l) {
        return (byte) ((nibble2decimal(h) << 4) | (nibble2decimal(l)));
    }
    
    private static byte nibble2decimal(final char n) {
        return (byte) ((n >= 0x30 && n <= 0x39) ? n - 0x30 :
            n - 'A' + 10);
    }
    
    /**
     * Convert a hex dump string into the bytes it represents
     * @param hexdump a hexdump, as generated by bytes2hex, e.g. "4142"
     * @return its raw data e.g. the bytes 0x41, 0x42
     */
    public static byte[] hex2bytes(final String hexdump) {
        final int hexdumpLength = hexdump.length();
        if ((hexdumpLength & 0x01) == 0x01) {
            throw new IllegalArgumentException("Cannot decode an odd length hex dump into bytes");
        }
        final byte[] bytes = new byte[hexdumpLength >> 1];
        int j = 0;
        for (int i = 0; i < hexdumpLength; i += 2) {
            bytes[j++] = hex2byte(hexdump.charAt(i), hexdump.charAt(i + 1));
        }
        return bytes;
    }
    
    /**
     * Create a HEX|ASCII dump of a complete buffer.
     * 
     * @param buffer a buffer of bytes
     * @return a dump of the complete buffer starting at offset 0.
     * 
     */
    public static String[] asciiDump(final byte[] buffer) {
        return asciiDump(buffer, 0, buffer.length);
    }
    
    /**
     * Create a HEX|ASCII dump of part of a buffer, given a start within it.
     * 
     * @param buffer a buffer of bytes
     * @param startOffset the starting offset within the buffer to start the dump at
     * @return the dump of the buffer starting at the start offset
     */
    public static String[] asciiDump(final byte[] buffer, final int startOffset) {
        return asciiDump(buffer, startOffset, buffer.length);
    }
    
    /**
     * Create an ASCII dump of part of a buffer, given a start and length.
     * 
     * @param buffer a buffer of bytes
     * @param startOffset the starting offset within the buffer to start the dump at.
     * @param bufferLength the number of bytes to dump 
     * @return an ASCII dump
     */
    public static String[] asciiDump(final byte[] buffer, final int startOffset, 
            final int bufferLength) {
        int offset = startOffset;
        final List<String> lines = new ArrayList<String>();
        final StringBuffer line = new StringBuffer(80);
        for (int i = 0; i < 75; i++) {
            line.append(' ');
        }
        int left = bufferLength;
        int i, upto64, x;
        byte b;
        while (left > 0) {
            for (i = 0; i < 75; i++) {
                line.setCharAt(i, ' ');
            }
            line.setCharAt(9, '|');
            line.replace(0, 8, int2hex(offset));
            upto64 = (left > 64) ? 64 : left;
            for (x = 0; x < upto64; x++) {
                b = buffer[offset + x];
                line.setCharAt(11 + x, (b >= 32 && b < 127) ? (char) b : '.');
            }
            lines.add(line.toString());
            offset += 64;
            left -= 64;
        }
        return lines.toArray(new String[lines.size()]);
    }
}
