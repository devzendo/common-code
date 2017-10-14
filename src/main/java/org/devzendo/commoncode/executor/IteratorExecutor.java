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

package org.devzendo.commoncode.executor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.Iterator;

/**
 * An IteratorExecutor executes a process and allows the output
 * to be retrieved using an Iterator.
 * @author matt
 *
 */
public class IteratorExecutor extends Executor implements Iterator<Object> {
    private static final Logger LOGGER = LoggerFactory.getLogger(IteratorExecutor.class);
    private boolean bFirst;
    private boolean bSkipBlankLines;
    private IOException myIOException;
    private BufferedReader myReader;
    private String myNextLine;
    
    /**
     * Create an IteratorExecutor that will execute the program with
     * parameters given in the String array
     * @param args an array of (program name, arg1, arg2, ... , argN) 
     */
    public IteratorExecutor(final String[] args) {
        super(args);
        init();
    }

    /**
     * Create an IteratorExecutor that will execute the given program with
     * no parameters.
     * @param cmd the name of a program 
     */
    public IteratorExecutor(final String cmd) {
        super(cmd);
        init();
    }

    private void init() {
        bFirst = true;
        myIOException = null;
        myReader = null;
        myNextLine = null;
        bSkipBlankLines = false;
    }
    
    /**
     * By default, this IteratorExecutor will return blank lines in calls to
     * next(). By calling skipBlankLines, blank lines will not be returned.
     *
     */
    public void skipBlankLines() {
        bSkipBlankLines = true;
    }

    /**
     * Is there a line from the program's output to obtain with next()?
     * <p>
     * The first time this is called, the process will start executing.
     * <p>
     * After this returns false, either the process has exited successfully,
     * and getExitValue() will return the exit value, or some IOException has
     * occurred, in which case getIOException() will return it.
     * <p>
     * You must either repeatedly call hasNext() until it returns false, or
     * if you wish to terminate early, you must call close().
     *
     * @return true if there is a line, false if there is not.
     */
    @Override
    public boolean hasNext() {
        if (myIOException != null) {
            return false;
        }
        if (bFirst) {
            bFirst = false;
            try {
                execute();
                myReader = getReader();
            } catch (final IOException e) {
                myIOException = e;
                LOGGER.warn("Could not execute " + getArguments()[0] + ": " + e.getMessage());
                return false;
            }
        }
        myNextLine = null;
        try {
            while ((myNextLine = myReader.readLine()) != null) {
                if (bSkipBlankLines && myNextLine.length() == 0) {
                    // Skip blank lines
                    continue;
                }
                return true;
            }
            // Has process finished? Obtain exit value...
            if (myNextLine == null) {
                try {
                    setExitValue(getProcess().waitFor());
                    LOGGER.debug("Exit code is " + getExitValue());
                } catch (final InterruptedException e) {
                    LOGGER.warn("Interrupted " + getArguments()[0] + " obtaining exit status");
                    // TODO now what?
                }
            }
        } catch (final IOException e) {
            myIOException = e;
            LOGGER.warn("Could not read " + getArguments()[0] + ": " + e.getMessage());
        }
        return false;
    }

    /**
     * If hasNext() has returned true, obtain the line with next().
     * @return the line from the program's output.
     */
    @Override
    public Object next() {
        return myNextLine;
    }

    /**
     * We don't remove things
     */
    @Override
    public void remove() {
        throw new UnsupportedOperationException();
    }

    /**
     * If hasNext() returns false, was there an IOException?
     * @return any IOException that was thrown
     */
    public IOException getIOException() {
        return myIOException;
    }
}
