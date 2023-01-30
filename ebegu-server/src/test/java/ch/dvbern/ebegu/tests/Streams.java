package ch.dvbern.ebegu.tests;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

public class Streams {

	private List<Person> personList = Arrays.asList(
		new Person("Anna", 161),
		new Person("Marie", 173),
		new Person("Lukas", 184),
		new Person("Kevin", 186),
		new Person("Lara", 142)
	);

	public Person getTallestPerson() {
		return personList
			.stream()
			.max(Comparator.comparing(person -> person.height))
			.get();
	}

	public Person getSmallestPerson() {
		return personList
			.stream()
			.min(Comparator.comparing(person -> person.height))
			.get();
	}

/*	public int getNumberOfPersonInList() {
		return 0;
	}*/

	public int getTotalHightOfAllPerson() {
		return personList
			.stream()
			.mapToInt(person -> person.height)
			.sum();
	}

	public List<Person> getPersonSortedByName() {
		return personList
			.stream()
			.sorted(Comparator.comparing(person -> person.name))
			.collect(Collectors.toList());
	}

	public List<String> getNameList() {
		return personList
			.stream()
			.map(person -> person.name)
			.collect(Collectors.toList());
	}

	public List<Person> getPersonsTallerThan170() {
		return personList
			.stream()
			.filter(person -> person.height > 170)
			.collect(Collectors.toList());
	}

	public List<Person> getPersonTallerTahn170SortedByName() {
		return personList
			.stream()
			.filter(person -> person.height > 170)
			.sorted(Comparator.comparing(person -> person.name))
			.collect(Collectors.toList());
	}
}

class Person implements Comparable<Person>{
	String name;
	int height;

	Person() {}

	Person(String name, int height) {
		this.name = name;
		this.height = height;
	}

	@Override
	public int compareTo(final Person o) {
		return 0;
	}
}


