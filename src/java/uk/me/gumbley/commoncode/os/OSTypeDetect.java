package uk.me.gumbley.commoncode.os;

public final class OSTypeDetect {
    private static OSTypeDetect myInstance = null;
    private OSType myOSType;
    private boolean bWindows;
    private OSTypeDetect() {
        super();
        myOSType = getOS();
        bWindows = getWindows();
    }
    public static synchronized OSTypeDetect getInstance() {
        if (myInstance == null) {
            myInstance = new OSTypeDetect();
        }
        return myInstance;
    }
    public enum OSType {
        Windows, Linux, MacOSX, Solaris
    }
    public OSType getOSType() {
        return myOSType;
    }
    private OSType getOS() {
        String osName = System.getProperty("os.name");
        if (osName.equals("Windows 2000") || osName.equals("Windows XP") || osName.startsWith("Windows")) { // let's guess about Longhorn and others...
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
        String osName = System.getProperty("os.name");
        return (osName.equals("Windows 2000") || osName.equals("Windows XP") || osName.startsWith("Windows")); // let's guess about Longhorn and others...        
    }
    public boolean isWindows() {
        return bWindows;
    }

}
