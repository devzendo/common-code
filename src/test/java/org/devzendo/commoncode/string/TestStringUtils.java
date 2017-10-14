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

import org.apache.log4j.Logger;
import org.devzendo.commoncode.logging.LoggingUnittestHelper;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.devzendo.commoncode.string.StringUtils.join;
import static org.devzendo.commoncode.string.StringUtils.translateByteUnits;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;


/**
 * Tests for String Utilities
 * 
 * @author matt
 *
 */
public final class TestStringUtils {
    private static final Logger LOGGER = Logger.getLogger(TestStringUtils.class);

    @BeforeClass
    public static void setupLogging() {
        LoggingUnittestHelper.setupLogging();
    }

    @Test
    public void byteUnitFormats() {
        assertThat(translateByteUnits(0), equalTo("   0B"));
        assertThat(translateByteUnits(1024 - 1), equalTo("1023B"));
        assertThat(translateByteUnits(1024), equalTo("  1.00KB"));
        assertThat(translateByteUnits(1048576 - 1), equalTo("1024.00KB"));
        assertThat(translateByteUnits(1048576), equalTo("  1.00MB"));
        assertThat(translateByteUnits(1073741824 - 1), equalTo("1024.00MB"));
        assertThat(translateByteUnits(1073741824), equalTo("  1.00GB"));
        assertThat(translateByteUnits(1099511627776L - 1), equalTo("1024.00GB"));
        assertThat(translateByteUnits(1099511627776L), equalTo("  1.00TB"));
        assertThat(translateByteUnits(1125899906842624L - 1), equalTo("1024.00TB"));
        assertThat(translateByteUnits(1125899906842624L), equalTo("  1.00PB"));
        assertThat(translateByteUnits(1152921504606846976L - 1), equalTo("1024.00PB"));
        assertThat(translateByteUnits(1152921504606846976L), equalTo("???.?xB"));
        // can't store a zettaabyte (or in new terms, zebibyte) in a Long.
        // zebibyte: 1180591620717411303424L
        // max long: 9223372036854775807L
    }

    /**
     * Join
     */
    @Test
    public void testJoin0() {
        LOGGER.info("start testJoin0");
        assertEquals("", join(new String[] {}, "; "));
        LOGGER.info("end testJoin0");
    }
    
    /**
     * Join
     */
    @Test
    public void testJoin1() {
        LOGGER.info("start testJoin1");
        assertEquals("XYZ", join(new String[] {"XYZ"}, "; "));
        LOGGER.info("end testJoin1");
    }
    
    /**
     * Join
     */
    @Test
    public void testJoin2() {
        LOGGER.info("start testJoin2");
        assertEquals("XYZ; ABC", join(new String[] {"XYZ", "ABC"}, "; "));
        LOGGER.info("end testJoin2");
    }
    
    /**
     * Join
     */
    @Test
    public void testJoin3() {
        LOGGER.info("start testJoin3");
        assertEquals("XYZ; ABC; DEF", join(new String[] {"XYZ", "ABC", "DEF"}, "; "));
        LOGGER.info("end testJoin3");
    }
    
    /**
     * Test pluralisation
     */
    @Test
    public void testPluralise() {
        assertEquals("Files", StringUtils.pluralise("File", 2));
        assertEquals("File", StringUtils.pluralise("File", 1));
    }
    
    /**
     * Test return of are or is
     */
    @Test
    public void testAreIs() {
        assertEquals("are", StringUtils.getAreIs(2));
        assertEquals("is", StringUtils.getAreIs(1));
    }
    
    /**
     * Tests ASCII conversion
     */
    @Test
    public void testStrToASCII() {
        final byte[] ascii = StringUtils.stringToASCII("ABC");
        assertEquals(3, ascii.length);
        assertEquals((byte) 65, ascii[0]);
        assertEquals((byte) 66, ascii[1]);
        assertEquals((byte) 67, ascii[2]);
    }
    
    /**
     * Test the masking of sensitive text
     */
    @Test
    public void maskString() {
        assertEquals("***", StringUtils.maskSensitiveText("abc"));
        assertEquals("****", StringUtils.maskSensitiveText("****"));
        assertEquals("", StringUtils.maskSensitiveText(""));
        assertEquals("", StringUtils.maskSensitiveText(null));
    }
}
