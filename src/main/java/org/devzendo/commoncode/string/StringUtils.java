/*
 * Copyright (C) 2008-2017 Matt Gumbley, DevZendo.org http://devzendo.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.devzendo.commoncode.string;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Formatter;
import java.util.List;

/**
 * Toolkit for String utility methods.
 * 
 * @author matt
 *
 */
public final class StringUtils {
    private StringUtils() {
        // do not instantiate
    }

    /**
     * A Kilosomething
     */
    public static final long KILO = 1024L;

    /**
     * A Megasomething
     */
    public static final long MEGA = KILO * KILO;

    /**
     * A Gigasomething
     */
    public static final long GIGA = MEGA * KILO;

    /**
     * A Terasomething
     */
    public static final long TERA = GIGA * KILO;
    
    /**
     * A Petasomething 
     */
    public static final long PETA = TERA * KILO;
    
    /**
     * An Etasomething 
     */
    public static final long ETA = PETA * KILO;
    
    /**
     * Translate a number of bytes into an SI binary representation.
     * @param bytes the number of bytes, e.g. 1024
     * @return e.g. 1KB
     */
    public static String translateByteUnits(final long bytes) {
        //myLogger.debug("Converting " + bytes + " into byte units... ");
        final Formatter fmt = new Formatter();
        final double work = bytes;
        if (bytes < KILO) {
            //myLogger.debug("BYTES: " + Long.valueOf(bytes));
            fmt.format("%4dB", Long.valueOf(bytes));
        } else if (bytes < MEGA) {
            //myLogger.debug("KILOBYTES: " + Double.valueOf(work / KILO));
            fmt.format("%6.2fKB", Double.valueOf(work / KILO));
        } else if (bytes < GIGA) {
            fmt.format("%6.2fMB", Double.valueOf(work / MEGA));
        } else if (bytes < TERA) {
            fmt.format("%6.2fGB", Double.valueOf(work / GIGA));
        } else if (bytes < PETA) {
            fmt.format("%6.2fTB", Double.valueOf(work / TERA));
        } else if (bytes < ETA) {
            fmt.format("%6.2fPB", Double.valueOf(work / PETA));
        } else {
            fmt.format("???.?xB");
        }
        return fmt.toString();
    }

    /**
     * Remove all trailing slashes (directory separators). Directory separators
     * are platform-specific.
     * @param dirPath the original path e.g. /tmp/// or null (which causes an empty string to be returned)
     * @return the path with no trailing slashes e.g. /tmp. Never null; can be empty.
     */
    public static String unSlashTerminate(final String dirPath) {
        if (dirPath == null) {
            return "";
        }
        final StringBuilder sb = new StringBuilder(dirPath.trim());
        unSlashTerminate(sb);
        return sb.toString();
    }
    
    private static void unSlashTerminate(final StringBuilder sb) {
        while (sb.length() != 0 && sb.charAt(sb.length() - 1) == File.separatorChar) {
            sb.deleteCharAt(sb.length() - 1);
        }
    }
    
    /**
     * Ensure there is only one trailing slash (directory separator). Directory
     * separators are platform-specific.
     * @param dirPath the original path e.g. /tmp/// or /foo
     * @return the path with one trailing slash e.g. /tmp/ or /foo/
     */
    public static String slashTerminate(final String dirPath) {
        if (dirPath == null) {
            return File.separator;
        }
        final StringBuilder sb = new StringBuilder(dirPath.trim());
        unSlashTerminate(sb);
        sb.append(File.separatorChar);
        return sb.toString();
    }

    /**
     * Given a name, typically a set name, convert it into a sensible file
     * name, i.e. replace spaces and dots with underscores, trim. 
     * @param name the original name
     * @return the sensible version
     */
    public static String sensibilizeFileName(final String name) {
        if (name == null) {
            return "";
        }
        final StringBuilder sb = new StringBuilder(name.trim());
        for (int i = 0; i < sb.length(); i++) {
            if (sb.charAt(i) == ' ' || sb.charAt(i) == '.') {
                sb.setCharAt(i, '_');
            }
        }
        return sb.toString();
    }
    
    /**
     * How many ms in a second?
     */
    public static final long MS_IN_SEC = 1000;
    
    /**
     * How many ms in minute? 
     */
    public static final long MS_IN_MIN = MS_IN_SEC * 60;
    
    /**
     * How many ms in an hour? 
     */
    public static final long MS_IN_HOUR = MS_IN_MIN * 60;
    
    /**
     * How many ms in a day? 
     */
    public static final long MS_IN_DAY = MS_IN_HOUR * 24;
    
    /**
     * How many ms in a week? 
     */
    public static final long MS_IN_WEEK = MS_IN_DAY * 7;
    
    /**
     * Translate a number of milliseconds into a human-understandable
     * description of the time, i.e. in hours, mins, seconds, and ms.
     * @param ms the number of milliseconds
     * @return the time, stupid
     */
    public static String translateTimeDuration(final long ms) {
        long m = ms;
        long v = 0;
        final StringBuilder sb = new StringBuilder();
        if (m > MS_IN_WEEK) {
            v = m / MS_IN_WEEK;
            m %= MS_IN_WEEK;
            sb.append(v);
            sb.append("w ");
        }
        if (m > MS_IN_DAY) {
            v = m / MS_IN_DAY;
            m %= MS_IN_DAY;
            sb.append(v);
            sb.append("d ");
        }
        if (m > MS_IN_HOUR) {
            v = m / MS_IN_HOUR;
            m %= MS_IN_HOUR;
            sb.append(v);
            sb.append("h ");
        }
        if (m > MS_IN_MIN) {
            v = m / MS_IN_MIN;
            m %= MS_IN_MIN;
            sb.append(v);
            sb.append("m ");
        }
        if (m > MS_IN_SEC) {
            v = m / MS_IN_SEC;
            m %= MS_IN_SEC;
            sb.append(v);
            sb.append("s ");
        }
        if (m >= 0) {
            sb.append(m);
            sb.append("ms ");
        }
        if (sb.length() > 0 && sb.charAt(sb.length() - 1) == ' ') {
            sb.deleteCharAt(sb.length() - 1);
        }
        return sb.toString();
    }

    /**
     * Given a number of bytes, create a comma-ized version. e.g. given
     * 32768, return "32,768". or given 1123233223, return "1,123,233,223"
     * @param bytesTransferred a number
     * @return a comma-ized version
     */
    public static String translateCommaBytes(final long bytesTransferred) {
        final NumberFormat nf = NumberFormat.getNumberInstance();
        nf.setGroupingUsed(true);
        return nf.format(bytesTransferred);
    }

    /**
     * Translate a bandwidth figure.
     * @param dur How many ms the transfer took
     * @param bytesTransferred How many bytes were transferred
     * @return e.g. 2.3 MB/s
     */
    public static String translateBandwidth(final long dur, final long bytesTransferred) {
        final StringBuilder sb = new StringBuilder();
        final double elapsedSecsD = (dur) / 1000.0;
        final double xferRate = (bytesTransferred / MEGA) / elapsedSecsD;
        sb.append(translateByteUnits(bytesTransferred));
        sb.append(" (");
        sb.append(translateCommaBytes(bytesTransferred));
        sb.append(" byte");
        if (bytesTransferred != 1) {
            sb.append("s");
        }
        sb.append(") transferred in ");
        sb.append(translateTimeDuration(dur));
        sb.append(" (");
        final Formatter fmt = new Formatter();
        fmt.format("%.2f", Double.valueOf(xferRate));
        sb.append(fmt.toString());
        sb.append(" MB/s)");
        return sb.toString();
    }

    /**
     * Translate a fraction into a percentage
     * @param numerator e.g. 5
     * @param denominator e.g. 10
     * @return e.g. 50%
     */
    public static Object translatePercentage(final long numerator, final long denominator) {
        final double p = ((double) numerator / (double) denominator) * 100.0;
        return new Formatter().format("%3.2f%%", Double.valueOf(p));
    }


    /**
     * Join words together
     * @param words the words to join
     * @param inBetween what goes between
     * @return the joined up string
     */
    public static String join(final String[] words, final String inBetween) {
        return join(null, words, null, inBetween);
    }
    
    /**
     * Join words together
     * @param sb a StringBuilder to fill
     * @param start something to put at the start
     * @param words the words to join
     * @param end something to put at the end
     * @param inBetween what goes between each word
     */
    public static void join(final StringBuilder sb, final String start, final String[] words, final String end, final String inBetween) {
        if (start != null) {
            sb.append(start);
        }
        if (words.length != 0) {
            for (int i = 0; i < words.length - 1; i++) {
                sb.append(words[i]);
                sb.append(inBetween);
            }
            sb.append(words[words.length - 1]);
        }
        if (end != null) {
            sb.append(end);
        }
    }

    /**
     * Join words together
     * @param start something to put at the start
     * @param words the words to join
     * @param end something to put at the end
     * @param inBetween what goes between each word
     * @return the joined up string
     */
    public static String join(final String start, final String[] words, final String end, final String inBetween) {
        final StringBuilder sb = new StringBuilder();
        join(sb, start, words, end, inBetween);
        return sb.toString();
    }   

    /**
     * Join words together
     * @param words the words to join
     * @param inBetween what goes between each word
     * @return the joined up string
     */
    public static String join(final List<String> words, final String inBetween) {
        return join(words.toArray(new String[0]), inBetween);
    }

    /**
     * Given an array of objects, and an object-to-String function, return an
     * array produced by running each object through the function
     * @param objects an array of objects
     * @param func a MapStringFunction that does some object-to-object transform
     * @return the transformed objects, as Strings
     */
    public String[] map(final Object[] objects, final MapStringFunction func) {
        final ArrayList < String > list = new ArrayList < String >();
        for (final Object object : objects) {
            list.add(func.mapToString(object));
        }
        return list.toArray(new String[0]);
    }
    
    /**
     * A string mapping function
     * @author matt
     *
     */
    public interface MapStringFunction {
        /**
         * Map an Object to a String
         * @param object some object
         * @return the String that it maps to
         */
        String mapToString(final Object object);
    }

    /**
     * Given an array of objects, and an object-to-object function, return an
     * array produced by running each object through the function
     * @param objects an array of objects
     * @param func a MapFunction that does some object-to-object transform
     * @return the transformed objects
     */
    public Object[] map(final Object[] objects, final MapFunction func) {
        final ArrayList < Object > list = new ArrayList < Object >();
        for (final Object object : objects) {
            list.add(func.mapToObject(object));
        }
        return list.toArray(new String[0]);
    }
    /**
     * An Object mapping function
     * @author matt
     *
     */
    public interface MapFunction {
        /**
         * @param object an input Object
         * @return an output Object
         */
        Object mapToObject(final Object object);
    }

    /**
     * Generate a simplistic pluralisation of a word. Don't expect
     * linguistic correctness if you ask it to pluralise sheep, fish,
     * octopus. Dog/Dogs is as far as it goes.
     * 
     * @param word the word to pluralise, e.g. "file"
     * @param num how many of these things there are
     * @return the word, possibly with 's' added.
     */
    public static String pluralise(final String word, final int num) {
        return num == 1 ? word : (word + "s"); 
    }

    /**
     * Return the correct word, are or is, depending on some number,
     * e.g. there are 2, there is 1.  
     * @param num a number
     * @return are or is
     */
    public static String getAreIs(final int num) {
        return num == 1 ? "is" : "are";
    }

    /**
     * Encode the given string to an ASCII byte array
     * @param input the input string
     * @return the ASCII bytes
     */
    public static byte[] stringToASCII(final String input) {
        try {
            return input.getBytes("ASCII");
        } catch (final UnsupportedEncodingException e) {
            // when hell freezes over :-)
            return new byte[0];
        }
    }

    /**
     * Convert a string into a number os asterisks, as long as
     * the input, for the masking of sentivie information, e.g.
     * passwords
     * @param input the input string, can be null
     * @return the converted output, or an empty string - never
     * null
     */
    public static String maskSensitiveText(final String input) {
        if (input == null) {
            return "";
        }
        final StringBuilder sb = new StringBuilder();
        for (int i = 0; i < input.length(); i++) {
            sb.append("*");
        }
        return sb.toString();
    }
}
