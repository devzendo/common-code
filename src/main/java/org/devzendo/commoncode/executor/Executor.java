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

package org.devzendo.commoncode.executor;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;

import org.apache.log4j.Logger;

/**
 * An Executor forms the basis for spawning processes and obtaining
 * their output and exit code.
 * 
 * @author matt
 *
 */
public abstract class Executor {
    private static Logger myLogger = Logger.getLogger(Executor.class);
    private final String[] myArguments;
    private Process myProcess;
    private ArrayList<String> myOtherLines;
    private OtherReader myOtherReaderThread;
    private BufferedReader myReader;
    private boolean bUseStdErr;
    private BufferedWriter myWriter;
    private int myExitValue;

    /**
     * Construct an Executor with an array of arguments 
     * @param args the arguments
     */
    public Executor(final String[] args) {
        super();
        if (args.length == 0) {
            throw new IllegalArgumentException("Cannot execute anything with an empty array");
        }
        myArguments = args;
        init();
    }

    /**
     * Construct an executor with a single argument
     * @param cmd the command to execute
     */
    public Executor(final String cmd) {
        super();
        myArguments = new String[] {cmd};
        init();
    }

    private void init() {
        if (myLogger.isDebugEnabled()) {
            final StringBuilder sb = new StringBuilder();
            sb.append(this.getClass().getSimpleName());
            sb.append(" [");
            for (String arg : myArguments) {
                sb.append(arg);
                sb.append(',');
            }
            sb.deleteCharAt(sb.length() - 1);
            sb.append("]");
            myLogger.debug(sb.toString());
        }
        myOtherLines = new ArrayList<String>();
        myOtherReaderThread = null;
        myExitValue = -1; // DID NOT EXIT
        bUseStdErr = false;
    }
    
    /**
     * By default, this Executor's getReader will return a Reader that can
     * be used to obtain the process's Standard Output, and getOtherLines() can
     * be used at process end to obtain all lines output to Standard Error.
     * Using useStdErr, getReader will return a Reader that can obtain the
     * process's Standard Error, and getOtherLines() will return all the Standard
     * Output. 
     *
     */
    public void useStdErr() {
        bUseStdErr = true;
    }

    /**
     * Obtain the supplied arguments
     * @return the supplied arguments
     */
    public String[] getArguments() {
        return myArguments;
    }

    /**
     * A thread that reads the stderr and adds to myOtherLines.
     *
     */
    class OtherReader extends Thread {
        private final InputStream mInputStream;

        /**
         * Construct a reading thread monitoring an InputStream
         * @param is the InputStream to monitor
         */
        OtherReader(final InputStream is) {
            this.mInputStream = is;
            setName(myArguments[0] + " Std Err Reader");
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void run() {
            try {
                String l = null;
                final BufferedReader br = new BufferedReader(new InputStreamReader(mInputStream));
                while ((l = br.readLine()) != null) {
                    myOtherLines.add(l);
                }
            } catch (final IOException ioe) {
                myLogger.warn("Failed to read standard error: " + ioe.getMessage());
            }
        }
    }

    /**
     * After execution, obtain the lines sent to the Standard Error channel, or,
     * if useStdErr has been called, those lines sent to Standard Output.
     * @return the stderr output
     */
    public ArrayList<String> getOtherLines() {
        return myOtherLines;
    }

    /**
     * Obtain a Reader suitable for reading data from the Standard Output
     * channel of the process (or if useStdErr is called, Standard Error)
     * @return the stdout reader
     */
    protected BufferedReader getReader() {
        return myReader;
    }

    /**
     * Obtain a Writer suitable for sending data to the Standard Input channel
     * of the process
     * @return the stdin writer
     */
    protected BufferedWriter getWriter() {
        return myWriter;
    }

    /**
     * Obtain the executing Process
     * @return the Process
     */
    protected Process getProcess() {
        return myProcess;
    }
    
    /**
     * Execute the process. After this, you can obtain a Reader and Writer
     * to control/observe the process.
     * @return the Process object
     * @throws IOException if the process cannot be executed.
     */
    protected Process execute() throws IOException {
        if (myLogger.isDebugEnabled()) {
            final StringBuilder sb = new StringBuilder();
            sb.append("Executing: ");
            for (String arg : myArguments) {
                sb.append(arg);
                sb.append(' ');
            }
            if (sb.charAt(sb.length() - 1) == ' ') {
                sb.deleteCharAt(sb.length() - 1);
            }
            myLogger.debug(sb.toString());
        }
        final Runtime rt = Runtime.getRuntime();
        myProcess = rt.exec(myArguments);
        if (bUseStdErr) {
            myReader = new BufferedReader(new InputStreamReader(myProcess.getErrorStream()));
            myOtherReaderThread = new OtherReader(myProcess.getInputStream());
        } else {
            myReader = new BufferedReader(new InputStreamReader(myProcess.getInputStream()));
            myOtherReaderThread = new OtherReader(myProcess.getErrorStream());
        }
        myOtherReaderThread.start();
        myWriter = new BufferedWriter(new OutputStreamWriter(myProcess.getOutputStream()));
        return myProcess;
    }

    /**
     * Execute the process, wait for it to finish, close it, and return the
     * exit code.
     * @return the exit code
     * @throws IOException on error
     */
    protected int executeAndWaitFor() throws IOException {
        try {
            myExitValue = execute().waitFor();
            close();
            myLogger.debug("Process " + myArguments[0] + " returned with exit code " + myExitValue);
            return myExitValue;
        } catch (final InterruptedException e) {
            myLogger.warn("Interrupted waiting for " + myArguments[0] + ": " + e.getMessage());
            return -1;
        }
    }

    /**
     * Set the exit code of the process
     * @param exitValue the exit code
     */
    protected void setExitValue(final int exitValue) {
        myExitValue = exitValue;
    }
    
    /**
     * @return the exit code of the process
     */
    public int getExitValue() {
        return myExitValue;
    }
    
    /**
     * Once finished, close this Executor
     *
     */
    public void close() {
        if (myOtherReaderThread != null) {
            try {
                myOtherReaderThread.join();
            } catch (final InterruptedException e) {
                myLogger.warn("Interrupted waiting for Standard Error Reader thread: " + e.getMessage());
            }
        }
        if (myProcess != null) {
            myProcess.destroy();
        }
        if (myReader != null) {
            try {
                myReader.close();
            } catch (final IOException ioe) {
                myLogger.warn("Could not close reader for " + myArguments[0] + ": " + ioe.getMessage());
            }
        }
    }
    
    /**
     * Make sue we tidy up
     * @throws Throwable on failure
     */
    @Override
    public void finalize() throws Throwable {
        close();
        super.finalize();
    }
}
