package ch.dvbern.ebegu.util;

import java.util.HashMap;

import javax.annotation.Nullable;

import org.slf4j.Logger;

/**
 * This helper checks the origin of a logmessage and consolidates repeated logmessages to one message if they occur within
 * a given timeframe
 */
public class LogConsolidated {

	private static final HashMap<String, TimeAndCount> LAST_LOGGED_TIME = new HashMap<>();
	private static final String UNKNOWN = "?";

	/**
	 * Logs given <code>message</code> to given <code>logger</code> as long as:
	 * <ul>
	 * <li>A message (from same class and line number) has not already been logged within the past <code>timeBetweenLogsSec</code>.</li>
	 * <li>The given <code>level</code> is active for given <code>logger</code>.</li>
	 * </ul>
	 * Note: If messages are skipped, they are counted. When <code>timeBetweenLogsSec</code> has passed, and a repeat message is logged,
	 * the count will be displayed.
	 *
	 * @param logger Where to log.
	 * @param timeBetweenLogsSec seconds to wait between similar log messages.
	 * @param message The actual message to log.
	 * @param t Can be null. Will log stack trace if not null.
	 */
	public static void warning(Logger logger, long timeBetweenLogsSec, String message, @Nullable Throwable t) {

		String uniqueIdentifier = getFileAndLine();
		TimeAndCount lastTimeAndCount = LAST_LOGGED_TIME.get(uniqueIdentifier);
		if (lastTimeAndCount != null) {
			synchronized (LAST_LOGGED_TIME) {
				long now = System.currentTimeMillis();
				if (now - lastTimeAndCount.time < timeBetweenLogsSec * 1000) {
					lastTimeAndCount.count++;
					return;
				} else {
					log(logger,  "|x" + lastTimeAndCount.count + "| " + message, t);
				}
			}
		} else {
			log(logger,  message, t);
		}
		LAST_LOGGED_TIME.put(uniqueIdentifier, new TimeAndCount());
	}

	private static String getFileAndLine() {
		StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
		boolean enteredLogConsolidated = false;
		for (StackTraceElement ste : stackTrace) {
			if (ste.getClassName().equals(LogConsolidated.class.getName())) {
				enteredLogConsolidated = true;
			} else if (enteredLogConsolidated) {
				// We have now file/line before entering LogConsolidated.
				return ste.getFileName() + ":" + ste.getLineNumber();
			}
		}
		return UNKNOWN;
	}

	private static void log(Logger logger, String message, Throwable t) {
		if (t == null) {
			logger.warn(message);
		} else {
			logger.warn(message, t);
		}
	}

	private static class TimeAndCount {
		long time;
		int count;

		TimeAndCount() {
			this.time = System.currentTimeMillis();
			this.count = 0;
		}
	}
}
