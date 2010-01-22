package org.devzendo.commoncode.gui;

import javax.swing.JTextArea;

import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.spi.LoggingEvent;
import org.apache.log4j.spi.ThrowableInformation;

/**
 * A log4j appender that appends events to the supplied JTextArea
 * @author matt
 *
 */
public class TextAreaAppender extends AppenderSkeleton {
    private final JTextArea myJTextArea;

    private int myTextAreaContentLength = 0;

    private volatile boolean bLoggingEnabled;

    private volatile boolean bScrollLock;

    /**
     * Construct a TextAreaAppender that appends events to the supplied JTextArea
     * @param textArea the JTextArea to log events in
     */
    public TextAreaAppender(final JTextArea textArea) {
        myJTextArea = textArea;
        bLoggingEnabled = false;
        bScrollLock = false;
    }

    /**
     * @see org.apache.log4j.AppenderSkeleton#append(org.apache.log4j.spi.LoggingEvent)
     * @param event the logging event to append to the text area
     */
    @Override
    protected  void append(final LoggingEvent event) {
        if (!bLoggingEnabled) {
            return;
        }
        final StringBuilder message = new StringBuilder();
        message.append(getLayout().format(event));
        final ThrowableInformation ti = event.getThrowableInformation();
        if (ti != null) {
            message.append("Throwable: " + ti.getThrowable().getClass().getName());
            final StackTraceElement[] ste = ti.getThrowable().getStackTrace();
            for (int i = 0; i < ste.length; i++) {
                message.append("   " + ste[i] + "\n");
            }
        }
        GUIUtils.invokeLaterOnEventThread(new Runnable() {
            public void run() {
                final String mS = message.toString();
                myJTextArea.append(mS);
                myTextAreaContentLength += mS.length();
                if (!bScrollLock) {
                    myJTextArea.setCaretPosition(myTextAreaContentLength);
                }
            }
        });
    }

    /**
     * @see org.apache.log4j.Appender#close()
     */
    public void close() {
    }

    /**
     * @see org.apache.log4j.Appender#requiresLayout()
     * @return true iff layout is required
     */
    public boolean requiresLayout() {
        return true;
    }

    /**
     * Sets the scroll lock, or clears it. When events are logged, the caret
     * will be automatically positioned at the end of the textarea, unless
     * the scroll lock is set.
     * @param scrollLockEnabled true to set the scroll lock, false to clear it.
     */
    public void setScrollLock(final boolean scrollLockEnabled) {
        bScrollLock = scrollLockEnabled;
    }


    /**
     * Enable log output, after initial GUI setup.
     *
     */
    public void enableLogging() {
        bLoggingEnabled = true;
    }
}
