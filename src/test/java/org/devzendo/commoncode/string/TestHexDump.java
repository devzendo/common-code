/**
 * Copyright (C) 2008-2010 Matt Gumbley, DevZendo.org http://devzendo.org
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

import org.assertj.core.api.Assertions;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.nio.ByteBuffer;

import static org.assertj.core.api.Assertions.assertThat;


/**
 * Tests for hex dump code
 * @author matt
 *
 */
public final class TestHexDump {

    public static final byte[] BUFFER = {'A', 'B', 'C', 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 127};
    public static final byte[] BUFFER_SINGLE_LINE = {'A', 'B', 'C', 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15};
    public static final byte[] BUFFER_SINGLE_BYTE = {'A'};

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void testOddHex2Bytes() {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Cannot decode an odd length hex dump into bytes");
        HexDump.hex2bytes("012");
    }

    @Test
    public void testByte2Hex() {
        assertThat(HexDump.byte2hex((byte) 0)).isEqualTo("00");
        assertThat(HexDump.byte2hex((byte) 201)).isEqualTo("C9");
        assertThat(HexDump.byte2hex((byte) 255)).isEqualTo("FF");
    }

    @Test
    public void testBytes2Hex() {
        assertThat(HexDump.bytes2hex(new byte[0])).isEqualTo("");
        assertThat(HexDump.bytes2hex(new byte[]{5})).isEqualTo("05");
        assertThat(HexDump.bytes2hex(new byte[]{5, 127, (byte) 255, 0})).isEqualTo("057FFF00");
    }

    @Test
    public void testHex2ByteNull() {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Cannot decode 'null' as a hex byte");
        HexDump.hex2byte(null);
    }

    @Test
    public void testHex2ByteEmptyString() {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Cannot decode '' as a hex byte");
        HexDump.hex2byte("");
    }

    @Test
    public void testHex2ByteSingleChar() {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Cannot decode 'A' as a hex byte");
        HexDump.hex2byte("A");
    }

    @Test
    public void testHex2ByteNonHexChars1() {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Cannot decode 'G' as a hex nibble");
        HexDump.hex2byte("G0");
    }

    @Test
    public void testHex2ByteNonHexChars2() {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Cannot decode 'G' as a hex nibble");
        HexDump.hex2byte("0G");
    }

    @Test
    public void testHex2ByteValidHexChars() {
        assertThat(HexDump.hex2byte("C9")).isEqualTo((byte) 0xC9);
    }

    @Test
    public void testHex2Bytes() {
        final byte[] bytes = HexDump.hex2bytes("00417f80ff");
        Assert.assertEquals(5, bytes.length);
        Assert.assertEquals(0x00, bytes[0]);
        Assert.assertEquals(0x41, bytes[1]);
        Assert.assertEquals(0x7f, bytes[2]);
        Assert.assertEquals((byte) 0x80, bytes[3]);
        Assert.assertEquals((byte) 0xff, bytes[4]);
    }

    @Test
    public void testLong2Hex() {
        assertThat(HexDump.long2hex(0))
                .isEqualTo("0000000000000000");
        assertThat(HexDump.long2hex(127))
                .isEqualTo("000000000000007F");
        assertThat(HexDump.long2hex(0x12345678ABCDEF01L))
                .isEqualTo("12345678ABCDEF01");
        assertThat(HexDump.long2hex(Long.MIN_VALUE))
                .isEqualTo("8000000000000000");
        assertThat(HexDump.long2hex(Long.MAX_VALUE))
                .isEqualTo("7FFFFFFFFFFFFFFF");
    }

    @Test
    public void testInt2Hex() {
        assertThat(HexDump.int2hex(0))
                .isEqualTo("00000000");
        assertThat(HexDump.int2hex(0x1234ABCD))
                .isEqualTo("1234ABCD");
        assertThat(HexDump.int2hex(Integer.MIN_VALUE))
                .isEqualTo("80000000");
        assertThat(HexDump.int2hex(Integer.MAX_VALUE))
                .isEqualTo("7FFFFFFF");
    }

    @Test
    public void testShort2Hex() {
        assertThat(HexDump.short2hex((short) 0))
                .isEqualTo("0000");
        assertThat(HexDump.short2hex((short) 0x12CD))
                .isEqualTo("12CD");
        assertThat(HexDump.short2hex(Short.MIN_VALUE))
                .isEqualTo("8000");
        assertThat(HexDump.short2hex(Short.MAX_VALUE))
                .isEqualTo("7FFF");
    }

    @Test
    public void testHexDumpCompleteByteArray() {
        final String[] singleByte = HexDump.hexDump(BUFFER_SINGLE_BYTE);
        assertThat(singleByte).hasSize(1);
        assertThat(singleByte[0]).isEqualTo("00000000 | 41                                              | A                ");

        final String[] lines = HexDump.hexDump(BUFFER);
        assertThat(lines).hasSize(2);
        assertThat(lines[0]).isEqualTo("00000000 | 41 42 43 03 04 05 06 07 08 09 0A 0B 0C 0D 0E 0F | ABC............. ");
        assertThat(lines[1]).isEqualTo("00000010 | 10 7F                                           | ..               ");

        final String[] singleLine = HexDump.hexDump(BUFFER_SINGLE_LINE);
        assertThat(singleLine).hasSize(1);
        assertThat(singleLine[0]).isEqualTo("00000000 | 41 42 43 03 04 05 06 07 08 09 0A 0B 0C 0D 0E 0F | ABC............. ");
    }

    @Test
    public void testHexDumpPartialByteArray() {
        final String[] single = HexDump.hexDump(BUFFER, 0, 1);
        assertThat(single).hasSize(1);
        assertThat(single[0]).isEqualTo("00000000 | 41                                              | A                ");

        final String[] start = HexDump.hexDump(BUFFER, 0, 5);
        assertThat(start).hasSize(1);
        assertThat(start[0]).isEqualTo("00000000 | 41 42 43 03 04                                  | ABC..            ");

        final String[] middle = HexDump.hexDump(BUFFER, 5, 5);
        assertThat(middle).hasSize(1);
        assertThat(middle[0]).isEqualTo("00000005 | 05 06 07 08 09                                  | .....            ");

        final String[] end = HexDump.hexDump(BUFFER, 13, 5);
        assertThat(end).hasSize(1);
        assertThat(end[0]).isEqualTo("0000000D | 0D 0E 0F 10 7F                                  | .....            ");
    }

    @Test
    public void testHexDumpPartialByteArrayTruncatesAnEndOfBuffer() {
        final String[] end = HexDump.hexDump(BUFFER, 13, 8);
        assertThat(end).hasSize(1);
        assertThat(end[0]).isEqualTo("0000000D | 0D 0E 0F 10 7F                                  | .....            ");
    }

    @Test
    public void testNullHexDump() {
        assertThat(HexDump.hexDump((byte[]) null)).hasSize(0);
        assertThat(HexDump.hexDump((byte[]) null, 0, 1)).hasSize(0);
        assertThat(HexDump.hexDump((ByteBuffer) null)).hasSize(0);
    }

    @Test
    public void testEmptyHexDump() {
        assertThat(HexDump.hexDump(new byte[0])).hasSize(0);
        assertThat(HexDump.hexDump(ByteBuffer.allocate(0))).hasSize(0);
    }

    @Test
    public void testAsciiDump() {
        final String in = "This is a test of the asciiDump routine\n";
        final byte[] inBytes = in.getBytes();

        final String[] start = HexDump.asciiDump(inBytes, 0, 8);
        assertThat(start).hasSize(1);
        assertThat(start[0]).isEqualTo("00000000 | This is                                                         ");

        final String[] middle = HexDump.asciiDump(inBytes, 5, 8);
        assertThat(middle).hasSize(1);
        assertThat(middle[0]).isEqualTo("00000005 | is a tes                                                        ");

        final String[] end = HexDump.asciiDump(inBytes, 32, 8);
        assertThat(end).hasSize(1);
        assertThat(end[0]).isEqualTo("00000020 | routine.                                                        ");
    }
    
    @Test
    public void testFullAsciiDump() {
        final String in = "1234567890123456789012345678901234567890123456789012345678901234";
        final byte[] inBytes = in.getBytes();
        final String[] asciiDump = HexDump.asciiDump(inBytes);
        Assert.assertEquals(1, asciiDump.length);
        Assert.assertEquals("00000000 | 1234567890123456789012345678901234567890123456789012345678901234", asciiDump[0]);
    }

    @Test
    public void testNullAsciiDump() {
        assertThat(HexDump.asciiDump((byte[]) null)).hasSize(0);
        assertThat(HexDump.asciiDump((byte[]) null, 0, 1)).hasSize(0);
    }

    @Test
    public void testEmptyAsciiDump() {
        assertThat(HexDump.asciiDump(new byte[0])).hasSize(0);
    }

    @Test
    public void testNibble2Decimal() {
        for (char i=Character.MIN_VALUE; i < Character.MAX_VALUE; i++) {
            boolean boom = false;
            try {
                HexDump.nibble2decimal(i);
            } catch (IllegalArgumentException iae) {
                boom = true;
            }
            boolean shouldConvertOk = ((i >= '0' && i <= '9') || (i >= 'a' && i <= 'f') || (i >= 'A' && i <= 'F'));
            assertThat(shouldConvertOk).isNotEqualTo(boom);
        }
    }

}
