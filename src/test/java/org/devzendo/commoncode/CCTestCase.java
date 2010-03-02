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

package org.devzendo.commoncode;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import org.junit.Before;

/**
 * Base class for commoncode test classes - initialise logging
 * 
 * @author matt
 *
 */
public abstract class CCTestCase {
    private static final Logger LOGGER = Logger.getLogger(CCTestCase.class);
    private static boolean bInitialised = false;
    
    /**
     * Initialise log4j
     * @throws Exception on failure
     */
    @Before
    public final void setUpLogging() throws Exception {
        if (!bInitialised) {
            bInitialised = true;
            BasicConfigurator.resetConfiguration();
            BasicConfigurator.configure();
            LOGGER.debug("Initialising logging");
        }
    }
    
    /**
     * @return the Logger for the base class
     */
    protected abstract Logger getLogger();
}
