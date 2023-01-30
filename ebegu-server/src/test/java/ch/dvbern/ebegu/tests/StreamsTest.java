package ch.dvbern.ebegu.tests;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.Assert;
import org.junit.Test;

public class StreamsTest {

	private Streams streams = new Streams();

	@Test
	public void getTallestPerson() {
		Person tallestPerson = streams.getTallestPerson();
		Assert.assertEquals("Kevin", tallestPerson.name);
	}

	@Test
	public void getSmallestPerson() {
		Person smallestPerson = streams.getSmallestPerson();
		Assert.assertEquals("Lara", smallestPerson.name);
	}

/*	@Test
	public void getNumberOfPersonInList() {
		int number = streams.getNumberOfPersonInList();
		Assert.assertEquals(5, number);
	}*/

	@Test
	public void getTotalHightOfAllPerson() {
		int hight = streams.getTotalHightOfAllPerson();
		Assert.assertEquals(846, hight);
	}

	@Test
	public void getPersonSortedByName() {
		List<Person> sortedByName = streams.getPersonSortedByName();
		Assert.assertEquals("Anna", sortedByName.get(0).name);
		Assert.assertEquals("Kevin", sortedByName.get(1).name);
		Assert.assertEquals("Lara", sortedByName.get(2).name);
		Assert.assertEquals("Lukas", sortedByName.get(3).name);
		Assert.assertEquals("Marie", sortedByName.get(4).name);
	}

	@Test
	public void getNameList() {
		List<String> nameList = streams.getNameList();
		Assert.assertEquals(5, nameList.size());
		Assert.assertTrue(nameList.contains("Anna"));
		Assert.assertTrue(nameList.contains("Kevin"));
		Assert.assertTrue(nameList.contains("Lara"));
		Assert.assertTrue(nameList.contains("Lukas"));
		Assert.assertTrue(nameList.contains("Marie"));
	}

	@Test
	public void getPersonsTallerThan170() {
		List<String> nameList = streams.getPersonsTallerThan170().stream().map(person -> person.name).collect(Collectors.toList());
		Assert.assertEquals(3, nameList.size());
		Assert.assertTrue(nameList.contains("Marie"));
		Assert.assertTrue(nameList.contains("Kevin"));
		Assert.assertTrue(nameList.contains("Lukas"));
	}

	@Test
	public void getPersonTallerTahn170SortedByName() {
		List<Person> sortedByName = streams.getPersonTallerTahn170SortedByName();
		Assert.assertEquals("Kevin", sortedByName.get(0).name);
		Assert.assertEquals("Lukas", sortedByName.get(1).name);
		Assert.assertEquals("Marie", sortedByName.get(2).name);
	}

}


