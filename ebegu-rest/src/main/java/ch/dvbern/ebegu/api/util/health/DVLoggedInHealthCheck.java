/*
 * Copyright (C) 2019 DV Bern AG, Switzerland
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
package ch.dvbern.ebegu.api.util.health;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.NavigableSet;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentMap;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import ch.dvbern.ebegu.services.AuthService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.eclipse.microprofile.health.Health;
import org.eclipse.microprofile.health.HealthCheckResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * checks how many users were active within timeframe and tracks this through time
 */
@ApplicationScoped
@Health
public class DVLoggedInHealthCheck implements org.eclipse.microprofile.health.HealthCheck {

	private static final int ACTIVE_WITHIN_TIMEFRAME_IN_SECONDS = 1800; //30 min
	static final int MAX_TIMESERIES_ENTRIES = 50;
	static final int TIME_SERIES_ENTRY_COOLDOWN_IN_MINUTES = 15;
	public static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");
	private Cache<LocalDateTime, Collection<String>> userNameCache;

	private LocalDateTime lastTimeSeriesEntry = LocalDateTime.MIN;

	private static final Logger LOG = LoggerFactory.getLogger(DVLoggedInHealthCheck.class.getSimpleName());

	private AuthService authService;

	@Inject
	public DVLoggedInHealthCheck(AuthService authService) {
		this.authService = authService;
		initCache();
	}

	public void initCache() {
		// create a cache that has limits in size and weight to ensure it does not eat too much mem
		userNameCache = CacheBuilder.newBuilder()
			.<LocalDateTime, Collection<String>>weigher((key, values) -> values.size())
			.maximumWeight(100000) // cache a maximum of 100 000 usernames
			.build();

	}

	@Override
	public HealthCheckResponse call() {

		Collection<String> usersActive = findAndRecordActiveUsers();

		return HealthCheckResponse.builder().name("dv-logged-in-check")
			.withData("activeUsersNum", usersActive.size())
			.withData("checkedTimeframeInSec", ACTIVE_WITHIN_TIMEFRAME_IN_SECONDS)
			.withData("users", toStringArray(usersActive))
			.withData("activeAtTime", getLastEntryTimeSeriesJson(MAX_TIMESERIES_ENTRIES))
			.state(true)
			.build();
	}

	Collection<String> findAndRecordActiveUsers() {
		Collection<String> usersActive = authService.findActiveSince(ACTIVE_WITHIN_TIMEFRAME_IN_SECONDS);
		createTimeSeriesEntry(LocalDateTime.now(),usersActive);
		return usersActive;
	}

	void createTimeSeriesEntry(LocalDateTime currTime, Collection<String> usersActive) {

		LocalDateTime earliestAllowedNextEntryTime = lastTimeSeriesEntry.plusMinutes(TIME_SERIES_ENTRY_COOLDOWN_IN_MINUTES);

		if (currTime.isEqual(earliestAllowedNextEntryTime) || currTime.isAfter(earliestAllowedNextEntryTime)) {
			userNameCache.put(currTime, usersActive);
			lastTimeSeriesEntry = currTime;
		} else{
			LOG.trace("Did not put entriy into TimeSeries for logged-in users over time because there was already"
				+ " an entry within the last 15 minutes");
		}
	}

	/**
	 *
	 * @return the last N entries in the time series for logged-in users over time as a json object in the format
	 * {"2018-04-10T03:34" : 76, "2018-04-10T03:13" : 98}
	 */
	 String getLastEntryTimeSeriesJson(int maxNumOfDesiredEntries) {

		Map<String, Integer> resutlMap = new LinkedHashMap<>();
		NavigableSet<LocalDateTime> localDateTimes = new TreeSet<>(userNameCache.asMap().keySet());
		 Iterator<LocalDateTime> iterator = localDateTimes.descendingIterator();
		while (iterator.hasNext() && resutlMap.size() < maxNumOfDesiredEntries) {
			LocalDateTime next = iterator.next();
			Collection<String> users = userNameCache.getIfPresent(next);
			String timestamp = TIME_FORMATTER.format(next);
			resutlMap.put(timestamp, users != null ? users.size() : 0);
		}

		return convertMapToJson(resutlMap);

	}

	 String toStringArray(Collection<String> users) {
		if (users != null && !users.isEmpty()) {
			ObjectMapper objectMapper = new ObjectMapper();
			try {
				return objectMapper.writeValueAsString(users);
			} catch (JsonProcessingException e) {
				LOG.error("Error creating healthcheck response, could not transform list of users to json array");
			}
		}
		return "[]";

	}

	String convertMapToJson(Map<String, Integer> resutlMap) {
		ObjectMapper objectMapper = new ObjectMapper();
		try {
			return objectMapper.writeValueAsString(resutlMap);
		} catch (JsonProcessingException e) {
			LOG.error("Error creating healthcheck response, could not transform list of users over time to json object");
		}
		return null;
	}
}
