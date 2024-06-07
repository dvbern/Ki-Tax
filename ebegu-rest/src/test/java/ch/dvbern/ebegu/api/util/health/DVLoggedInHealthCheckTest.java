package ch.dvbern.ebegu.api.util.health;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import ch.dvbern.ebegu.services.AuthService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.easymock.EasyMock;
import org.easymock.EasyMockRunner;
import org.easymock.Mock;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import static ch.dvbern.ebegu.api.util.health.DVLoggedInHealthCheck.MAX_TIMESERIES_ENTRIES;
import static ch.dvbern.ebegu.api.util.health.DVLoggedInHealthCheck.TIME_SERIES_ENTRY_COOLDOWN_IN_MINUTES;

@RunWith(EasyMockRunner.class)
public class DVLoggedInHealthCheckTest {
	@Mock
	private AuthService authServiceMock;

	@Test
	public void testReturnedValues() throws IOException {

		//setup
		Collection<String> userList = List.of("eberhard.gugler@mailinator.com", "jemand@irgendwo.com");
		EasyMock.expect(authServiceMock.findActiveSince(EasyMock.anyInt()))
			.andReturn(userList);
		EasyMock.replay(authServiceMock); // im replay modus wird bei aufrufen die definierte antwort zurueckgegeben

		DVLoggedInHealthCheck dvLoggedInHealthCheck = new DVLoggedInHealthCheck(authServiceMock);
		//to make the test more interesting add a bunch of old time series entries
		setuUpUserTimeSerieEntriesForTest(dvLoggedInHealthCheck);

		// actual test
		Collection<String> list = dvLoggedInHealthCheck.findAndRecordActiveUsers();
		String currentusersJson = dvLoggedInHealthCheck.toStringArray(list);
		Assert.assertEquals("[\"eberhard.gugler@mailinator.com\",\"jemand@irgendwo.com\"]", currentusersJson);
		String lastEntryTimeSeriesJson = dvLoggedInHealthCheck.getLastEntryTimeSeriesJson(MAX_TIMESERIES_ENTRIES);

		Assert.assertTrue(lastEntryTimeSeriesJson.startsWith("{"));
		Assert.assertTrue(lastEntryTimeSeriesJson.endsWith("}"));

		ObjectMapper objectMapper = new ObjectMapper();
		SortedMap<String, Integer> jsonAsMap = objectMapper.readValue(lastEntryTimeSeriesJson, TreeMap.class);
		Assert.assertEquals(MAX_TIMESERIES_ENTRIES, jsonAsMap.size());
		Integer lastNum = jsonAsMap.get(jsonAsMap.lastKey());
		Assert.assertEquals(userList.size(), (int) lastNum); //last entry is the "current active users" entry
		jsonAsMap.remove(jsonAsMap.lastKey());
		int i = 2; // -1 for the remoted element and -1 for the index offset
		for (String key : jsonAsMap.keySet()) {
			Integer numOfUsers = jsonAsMap.get(key);
			Assert.assertEquals(MAX_TIMESERIES_ENTRIES - i , (int) numOfUsers);
			i++;
		}
		//verify expected calls
		EasyMock.verify(authServiceMock);

	}
	@Test
	public void testNoOneLoggedIn() throws IOException {

		//setup
		Collection<String> userList = Collections.emptyList();
		EasyMock.expect(authServiceMock.findActiveSince(EasyMock.anyInt()))
			.andReturn(userList);
		EasyMock.replay(authServiceMock); // im replay modus wird bei aufrufen die definierte antwort zurueckgegeben

		DVLoggedInHealthCheck dvLoggedInHealthCheck = new DVLoggedInHealthCheck(authServiceMock);
		//to make the test more interesting add a bunch of old time series entries

		// actual test
		Collection<String> list = dvLoggedInHealthCheck.findAndRecordActiveUsers();
		String currentusersJson = dvLoggedInHealthCheck.toStringArray(list);
		Assert.assertEquals("[]", currentusersJson);
		String lastEntryTimeSeriesJson = dvLoggedInHealthCheck.getLastEntryTimeSeriesJson(MAX_TIMESERIES_ENTRIES);

		Assert.assertTrue(lastEntryTimeSeriesJson.startsWith("{"));
		Assert.assertTrue(lastEntryTimeSeriesJson.endsWith("}"));

		ObjectMapper objectMapper = new ObjectMapper();
		SortedMap<String, Integer> jsonAsMap = objectMapper.readValue(lastEntryTimeSeriesJson, TreeMap.class);
		Assert.assertEquals(1, jsonAsMap.size());
		Integer lastNum = jsonAsMap.get(jsonAsMap.lastKey());
		Assert.assertEquals(0, (int) lastNum); //lastt entry is the "current active users" entry which has no active user
		EasyMock.verify(authServiceMock);

	}

	private void setuUpUserTimeSerieEntriesForTest(DVLoggedInHealthCheck dvLoggedInHealthCheck) {
		LocalDateTime oldEntriesStarttime =
			LocalDateTime.now().minusMinutes((TIME_SERIES_ENTRY_COOLDOWN_IN_MINUTES) + 1);
		for (int i = 100; i > 0; i--) {
			//start with oldest entry and progress
			LocalDateTime oldEntryTime = oldEntriesStarttime.minusMinutes((TIME_SERIES_ENTRY_COOLDOWN_IN_MINUTES * i));
			Collection<String> users = IntStream.range(1, i)
				.mapToObj(i1 -> "user" + i1 + "@mailbucket.dvbern.ch")
				.collect(Collectors.toList());
			dvLoggedInHealthCheck.createTimeSeriesEntry(oldEntryTime, users);

		}
	}
}
