package uk.me.gumbley.commoncode.executor;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import org.apache.log4j.Logger;

public abstract class Executor {
    private static Logger myLogger = Logger.getLogger(Executor.class);

    private String[] myArguments;

    private Process myProcess;

    private ArrayList<String> myOtherLines;

    private OtherReader myOtherReaderThread;

    private BufferedReader myReader;
    private boolean bUseStdErr;


    private BufferedWriter myWriter;
    
    private int myExitValue;

    public Executor(final String[] args) {
        super();
        if (args.length == 0) {
            throw new IllegalArgumentException("Cannot execute anything with an empty array");
        }
        myArguments = args;
        init();
    }

    public Executor(final String cmd) {
        super();
        myArguments = new String[] {cmd};
        init();
    }

    private void init() {
        if (myLogger.isDebugEnabled()) {
            StringBuilder sb = new StringBuilder();
            sb.append(this.getClass().getSimpleName());
            sb.append(" [");
            for (String arg : myArguments) {
                sb.append(arg);
                sb.append(',');
            }
            sb.deleteCharAt(sb.length()-1);
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

    public String[] getArguments() {
        return myArguments;
    }

    class OtherReader extends Thread {
        InputStream is;

        OtherReader(InputStream is) {
            this.is = is;
            setName(myArguments[0] + " Std Err Reader");
        }

        public void run() {
            String l = null;
            try {
                BufferedReader br = new BufferedReader(new InputStreamReader(is));
                while ((l = br.readLine()) != null) {
                    myOtherLines.add(l);
                }
            } catch (IOException ioe) {
                myLogger.warn("Failed to read standard error: " + ioe.getMessage());
            }
        }
    }

    /**
     * After execution, obtain the lines sent to the Standard Error channel, or,
     * if useStdErr has been called, those lines sent to Standard Output.
     * @return
     */
    public ArrayList getOtherLines() {
        return myOtherLines;
    }

    /**
     * Obtain a Reader suitable for reading data from the Standard Output
     * channel of the process (or if useStdErr is called, Standard Error)
     * @return
     */
    protected BufferedReader getReader() {
        return myReader;
    }

    /**
     * Obtain a Writer suitable for sending data to the Standard Input channel
     * of the process
     * @return
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
            StringBuilder sb = new StringBuilder();
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
        Runtime rt = Runtime.getRuntime();
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
     * @return
     * @throws IOException
     */
    protected int executeAndWaitFor() throws IOException {
        try {
            myExitValue = execute().waitFor();
            close();
            myLogger.debug("Process " + myArguments[0] + " returned with exit code " + myExitValue);
            return myExitValue;
        } catch (InterruptedException e) {
            myLogger.warn("Interrupted waiting for " + myArguments[0] + ": " + e.getMessage());
            return -1;
        }
    }

    protected void setExitValue(int exitValue) {
        myExitValue = exitValue;
    }
    
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
            } catch (InterruptedException e) {
                myLogger.warn("Interrupted waiting for Standard Error Reader thread: " + e.getMessage());
            }
        }
        if (myProcess != null) {
            myProcess.destroy();
        }
        if (myReader != null) {
            try {
                myReader.close();
            } catch (IOException ioe) {
                myLogger.warn("Could not close reader for " + myArguments[0] + ": " + ioe.getMessage());
            }
        }
    }
    
    /**
     * Make sue we tidy up
     */
    public void finalize() throws Throwable {
        close();
        super.finalize();
    }
}
