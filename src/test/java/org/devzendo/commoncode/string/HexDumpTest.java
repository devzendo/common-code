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
