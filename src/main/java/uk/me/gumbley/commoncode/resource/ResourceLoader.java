package uk.me.gumbley.commoncode.resource;

import java.io.IOException;
import java.io.InputStream;

import org.apache.log4j.Logger;

/**
 * Toolkit for helping with loading resources.
 * 
 * @author matt
 *
 */
public final class ResourceLoader {
    private static final Logger LOGGER = Logger.getLogger(ResourceLoader.class);
    /**
     * It's a toolkit; no instances
     */
    private ResourceLoader() {
        // no instances
    }

    /**
     * Read a resource into a StringBuilder
     * @param store the StringBuilder to be populated with the resource
     * @param resourceName the name of the resource, from the classpath 
     */
    public static void readResource(final StringBuilder store, final String resourceName) {
        final InputStream resourceAsStream = getResourceInputStream(resourceName);
        final int bufsize = 16384;
        final byte[] buf = new byte[bufsize];
        int nread;
        try {
            while ((nread = resourceAsStream.read(buf, 0, bufsize)) != -1) {
                final String block = new String(buf, 0, nread);
                store.append(block);
            }
        } catch (final IOException e) {
            LOGGER.warn("Could not read resource '" + resourceName + "': " + e.getMessage());
        } finally {
            try {
                resourceAsStream.close();
            } catch (final IOException ioe) {
            }
        }
    }
    
    /**
     * Read a resource into a String
     * @param resourceName the name of the resource, from the classpath
     * @return the resource contents
     */
    public static String readResource(final String resourceName) {
        final StringBuilder sb = new StringBuilder();
        readResource(sb, resourceName);
        return sb.toString();
    }

    /**
     * Obtain the InputStream for the named resource
     * @param resourceName the name of the resource, from the classpath
     * @return the InputStream
     */
    public static InputStream getResourceInputStream(final String resourceName) {
        final InputStream resourceAsStream = Thread.currentThread().
            getContextClassLoader().
            getResourceAsStream(resourceName);
        return resourceAsStream;
    }

    /**
     * Does the named resource exist?
     * @param resourceName the name of the resource, from the classpath
     * @return true iff it exists.
     */
    public static boolean resourceExists(final String resourceName) {
        return Thread.currentThread().
            getContextClassLoader().getResource(resourceName) != null;
    }
}
