package org.devzendo.commoncode.string;

import org.devzendo.commoncode.string.HexDump;
import org.junit.Assert;
import org.junit.Test;



/**
 * Tests for hex dump code
 * @author matt
 *
 */
public final class HexDumpTest {
    
    /**
     * 
     */
    @Test(expected = IllegalArgumentException.class)
    public void testOddHex2Bytes() {
        HexDump.hex2bytes("012");
    }
    
    /**
     * 
     */
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
}
