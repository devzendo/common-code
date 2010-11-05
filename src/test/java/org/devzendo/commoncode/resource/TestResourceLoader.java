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

import org.devzendo.commoncode.logging.LoggingUnittestHelper;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;



/**
 * Test the resource loader
 * @author matt
 *
 */
public final class TestResourceLoader {
    /**
     * 
     */
    @BeforeClass
    public static void setupLogging() {
        LoggingUnittestHelper.setupLogging();
    }

    /**
     * 
     */
    @Test
    public void canGetResource() {
        final StringBuilder sb = new StringBuilder();
        ResourceLoader.readResource(sb, "resourceloader.txt");
        Assert.assertEquals("this is a test", sb.toString());
    }
    
    /**
     * 
     */
    @Test
    public void resourceExists() {
        Assert.assertTrue(ResourceLoader.resourceExists("resourceloader.txt"));
        Assert.assertFalse(ResourceLoader.resourceExists("lordlucan.txt"));
    }
}
