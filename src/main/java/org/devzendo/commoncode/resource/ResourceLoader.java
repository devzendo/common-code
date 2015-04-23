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

package org.devzendo.commoncode.resource;

import java.awt.Image;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Properties;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;

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
        if (resourceAsStream == null) {
            LOGGER.warn("Could not open resource '" + resourceName + "'");
            return;
        }
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
    
    /**
     * Obtain the URL for the named resource
     * @param resourceName the name of the resource, from the classpath
     * @return the URL
     */
    public static URL getResourceURL(final String resourceName) {
        final URL url = Thread.currentThread().
            getContextClassLoader().
            getResource(resourceName);
        return url;
    }

    /**
     *  Loads an ImageIcon from a resource.
     *  @param resourceName the name of the icon resource, from the classpath
     *  @return an ImageIcon, or null if the path was invalid. 
     */
    public static ImageIcon createResourceImageIcon(final String resourceName) {
        final java.net.URL imgURL = ResourceLoader.getResourceURL(resourceName);
        if (imgURL != null) {
            return new ImageIcon(imgURL);
        } else {
            LOGGER.warn("Couldn't find file: " + resourceName);
            return null;
        }
    }
    
    /**
     *  Loads an Image from a resource.
     *  @param resourceName the name of the icon resource, from the classpath
     *  @return an Image, or null if the path was invalid. 
     */
    public static Image readImageResource(final String resourceName) {
        try {
            final InputStream resourceAsStream = getResourceInputStream(resourceName);
            if (resourceAsStream != null) {
                try {
                    return ImageIO.read(resourceAsStream);
                } finally {
                    resourceAsStream.close();
                }
            } else {
                LOGGER.warn("Couldn't find file: " + resourceName);
                return null;
            }
        } catch (final IOException e) {
            LOGGER.warn("Couldn't read image " + resourceName + ": " + e.getMessage(), e);
            return null;
        }
    }

    /**
     * Loads a properties file from a resource.
     * @param resourceName the name of the properties resource, from the classpath
     * @return a Properties, or null if the path was invalid, or the properties
     * could not be read.
     */
    public static Properties readPropertiesResource(final String resourceName) {
        final Properties properties = new Properties();
        try {
            final InputStream resourceAsStream = getResourceInputStream(resourceName);
            if (resourceAsStream != null) {
                try {
                    properties.load(resourceAsStream);
                    return properties;
                } finally {
                    resourceAsStream.close();
                }
            } else {
                LOGGER.warn("Couldn't find file: " + resourceName);
                return null;
            }
        } catch (final IOException e) {
            LOGGER.warn("Couldn't read properties " + resourceName + ": " + e.getMessage(), e);
            return null;
        }
    }
}
