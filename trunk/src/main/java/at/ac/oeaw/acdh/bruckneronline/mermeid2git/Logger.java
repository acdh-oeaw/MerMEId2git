package at.ac.oeaw.acdh.bruckneronline.mermeid2git;

import java.io.File;
import java.io.IOException;
import java.lang.Thread.UncaughtExceptionHandler;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;

/*
 * TODO : refactor into reusables
 */
/**
 * 
 * @author mcupak
 *
 */
public class Logger extends java.util.logging.Logger {

	private final Formatter logFormatter;
	
	public Logger() {
		super("", null);
		
		// init logging
		for (Handler h : getHandlers()) {	// remove default handlers
			removeHandler(h);
		}

		this.logFormatter = new LogFormatter();
		
		// always log into console
		addHandler(setupHandler(new ConsoleHandler()));
		
		// log all uncaught exceptions
		Thread.setDefaultUncaughtExceptionHandler(new GeneralExceptionHandler());
		System.setProperty("sun.awt.exception.handler", GeneralExceptionHandler.class.getName());	// for EDT exceptions
	}
	
	private Handler setupHandler(Handler h) {
		h.setLevel(Level.ALL);
		h.setFormatter(logFormatter);
		return h;
	}
	
	public void logInto(File logFile, int limit, int count, boolean append) {
		try {
			addHandler(setupHandler(new FileHandler(logFile.getAbsolutePath(), limit, count, append)));

		} catch (SecurityException | IOException e) {
			log(Level.WARNING, "exception while adding file logger handler", e);
		}

	}
	
	public void close() {
		for (Handler h : getHandlers()) {
			h.close();
		}
	}

	private class GeneralExceptionHandler implements UncaughtExceptionHandler {
		
		@SuppressWarnings("unused")
		public void handle(Throwable thrown) {	// for EDT exceptions
			uncaughtException(Thread.currentThread(), thrown);
		}
	
		@Override
		public void uncaughtException(Thread t, Throwable e) {
			log(Level.WARNING, "uncaught exception occured in Thread '" + t.getName() + "' with id '" + t.getId() + "'", e);
		}
	}
}
