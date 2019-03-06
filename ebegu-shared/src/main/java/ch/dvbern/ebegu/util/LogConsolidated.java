/*
 * Copyright (C) 2018 DV Bern AG, Switzerland
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package ch.dvbern.ebegu.util;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nullable;

import org.slf4j.Logger;

/**
 * This helper checks the origin of a logmessage (filename and line in sourcecode)
 * and consolidates repeated logmessages to one message if they occur within
 * a given timeframe
 */
@SuppressWarnings("NonFinalUtilityClass")
public class LogConsolidated {

	private static final Map<String, TimeAndCount> LAST_LOGGED_TIME = new HashMap<>();
	private static final String UNKNOWN = "?";

	private LogConsolidated() {
	}

	/**
	 * Logs given {@code message} to given {@code logger} as long as:
	 * <ul>
	 * <li>A message (from same class and line number) has not already been logged within the past {@code timeBetweenLogsSec}.</li>
	 * <li>The given {@code level} is active for given {@code logger}.</li>
	 * </ul>
	 * Note: If messages are skipped, they are counted. When {@code timeBetweenLogsSec} has passed, and a repeat message is logged,
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
				}
				//noinspection StringConcatenationMissingWhitespace
				log(logger,  "|x" + lastTimeAndCount.count + "| " + message, t);
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
				return ste.getFileName() + ':' + ste.getLineNumber();
			}
		}
		return UNKNOWN;
	}

	private static void log(Logger logger, String message, @Nullable Throwable t) {
		if (t == null) {
			logger.warn(message);
		} else {
			logger.warn(message, t);
		}
	}

	@SuppressWarnings("PackageVisibleField")
	private static class TimeAndCount {
		long time;
		int count;

		TimeAndCount() {
			this.time = System.currentTimeMillis();
			this.count = 0;
		}
	}
}
