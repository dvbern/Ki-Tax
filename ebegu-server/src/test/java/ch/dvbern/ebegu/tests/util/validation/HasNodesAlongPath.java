/*
 * Copyright © 2019 DV Bern AG, Switzerland
 *
 * Das vorliegende Dokument, einschliesslich aller seiner Teile, ist urheberrechtlich
 * geschützt. Jede Verwertung ist ohne Zustimmung der DV Bern AG unzulässig. Dies gilt
 * insbesondere für Vervielfältigungen, die Einspeicherung und Verarbeitung in
 * elektronischer Form. Wird das Dokument einem Kunden im Rahmen der Projektarbeit zur
 * Ansicht übergeben, ist jede weitere Verteilung durch den Kunden an Dritte untersagt.
 */

package ch.dvbern.ebegu.tests.util.validation;

import java.util.List;

import javax.annotation.Nonnull;
import javax.validation.Path;
import javax.validation.Path.Node;

import org.apache.commons.lang3.StringUtils;
import org.hamcrest.Description;
import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.hamcrest.collection.IsIterableContainingInOrder;

import static org.hamcrest.CoreMatchers.equalTo;

/**
 * @see
 * <a href="https://github.com/testinfected/hamcrest-matchers/tree/master/validation-matchers/src/main/java/org/testinfected/hamcrest/validation">source</a>
 */
public class HasNodesAlongPath extends TypeSafeMatcher<Path> {

	@Nonnull
	private final List<Matcher<? super Node>> nodeMatchers;

	public HasNodesAlongPath(@Nonnull List<Matcher<? super Node>> nodeMatchers) {
		this.nodeMatchers = nodeMatchers;
	}

	@Override
	protected boolean matchesSafely(@Nonnull Path path) {
		return IsIterableContainingInOrder.contains(nodeMatchers).matches(path);
	}

	@Override
	public void describeTo(@Nonnull Description description) {
		description.appendList(StringUtils.EMPTY, "->", StringUtils.EMPTY, nodeMatchers);
	}

	@Nonnull
	public static HasNodesAlongPath path(@Nonnull String expression) {
		return new HasNodesAlongPath(List.of(nodeWithName(expression)));
	}

	@Nonnull
	public static Matcher<? super Node> nodeWithName(@Nonnull String name) {
		return nodeWithName(equalTo(name.isEmpty() ? null : name));
	}

	@Nonnull
	public static Matcher<? super Node> nodeWithName(@Nonnull Matcher<? super String> nameMatcher) {
		return new FeatureMatcher<Node, String>(nameMatcher, StringUtils.EMPTY, StringUtils.EMPTY) {
			@Override
			protected String featureValueOf(Node actual) {
				return actual.getName();
			}
		};
	}
}
