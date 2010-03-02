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

package org.devzendo.commoncode.os;

/**
 * Operating System detection
 * @author matt
 *
 */
public final class OSTypeDetect {
    private static OSTypeDetect myInstance = null;
    private OSType myOSType;
    private boolean bWindows;
    private OSTypeDetect() {
        super();
        myOSType = getOS();
        bWindows = getWindows();
    }
    /**
     * Singleton that gives the current OS
     * @return the OS
     */
    public static synchronized OSTypeDetect getInstance() {
        if (myInstance == null) {
            myInstance = new OSTypeDetect();
        }
        return myInstance;
    }
    /**
     * The types of OS we detect
     */
    public enum OSType {
        /**
         * Any variant of Windows
         */
        Windows,
        
        /**
         * Any variant of Linux 
         */
        Linux,
        /**
         * Any version of Mac OS X
         */
        MacOSX,
        /**
         * Any variant of Solaris
         */
        Solaris
    }
    /**
     * @return the OS type
     */
    public OSType getOSType() {
        return myOSType;
    }
    private OSType getOS() {
        final String osName = System.getProperty("os.name");
        if (osName.equals("Windows 2000")
                || osName.equals("Windows XP")
                || osName.startsWith("Windows")) { 
            // let's guess about Vista and 7 and others...
            return OSType.Windows;
        } else if (osName.equals("SunOS")) {
            return OSType.Solaris;
        } else if (osName.equals("Linux")) {
            return OSType.Linux;
        } else if (osName.equals("Mac OS X")) {
            return OSType.MacOSX;
        }
        throw new RuntimeException("Unknown OS type");
    }
    private boolean getWindows() {
        final String osName = System.getProperty("os.name");
        return (osName.equals("Windows 2000")
                || osName.equals("Windows XP")
                || osName.startsWith("Windows")); // would they ever drop that brand?
        // let's guess about Vista and 7 and others...        
    }
    /**
     * @return true iff running on some MS Windows variant
     */
    public boolean isWindows() {
        return bWindows;
    }

    /**
     * Command line program to print the detected OS
     * @param args none
     */
    public static void main(final String[] args) {
        System.out.println(getInstance().getOSType());
    }
}
