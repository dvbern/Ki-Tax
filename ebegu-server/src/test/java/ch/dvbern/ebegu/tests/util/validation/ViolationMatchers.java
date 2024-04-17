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

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.Collection;

import javax.annotation.Nonnull;
import javax.validation.ConstraintViolation;
import javax.validation.Path;
import javax.validation.metadata.ConstraintDescriptor;

import lombok.experimental.UtilityClass;
import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.hamcrest.collection.IsEmptyIterable;
import org.hamcrest.core.IsIterableContaining;

import static ch.dvbern.ebegu.tests.util.validation.HasNodesAlongPath.path;
import static com.spotify.hamcrest.pojo.IsPojo.pojo;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;

/**
 * A collection of hamcrest matchers to validate JSR-303
 * {@link ConstraintViolation}s.
 *
 * @see
 * <a href="https://github.com/testinfected/hamcrest-matchers/tree/master/validation-matchers/src/main/java/org/testinfected/hamcrest/validation">source</a>
 */
@UtilityClass
public final class ViolationMatchers {

	/**
	 * Checks that a collection of violations contains at least one violation
	 * and that each of the given matchers matches at least one of its elements.
	 */
	@SafeVarargs
	@SuppressWarnings("OverloadedVarargsMethod")
	@Nonnull
	public static <T> Matcher<Iterable<ConstraintViolation<T>>> violates(
		@Nonnull Matcher<? super ConstraintViolation<T>>... matchers) {

		return violates(Arrays.asList(matchers));
	}

	/**
	 * Checks that a collection of violations contains at least one violation
	 * and that each of the given matchers matches at least one of its elements.
	 */
	@Nonnull
	public static <T> Matcher<Iterable<ConstraintViolation<T>>> violates(
		@Nonnull Collection<Matcher<? super ConstraintViolation<T>>> matchers) {

		return Matchers.<Iterable<ConstraintViolation<T>>>
			both(ViolationMatchers.fails())
			.and(IsIterableContaining.hasItems(violation(matchers)));
	}

	/**
	 * Checks that validation fails, i.e. there is at least one constraint violation.
	 */
	@Nonnull
	public static <T> Matcher<Iterable<? extends ConstraintViolation<T>>> fails() {
		return not(ViolationMatchers.succeeds());
	}

	/**
	 * Checks that violation succeeds, i.e. that there is no constraint violation.
	 */
	@Nonnull
	public static <T> Matcher<Iterable<? extends ConstraintViolation<T>>> succeeds() {
		return IsEmptyIterable.emptyIterable();
	}

	@Nonnull
	public static <T> Matcher<Iterable<? extends ConstraintViolation<T>>> succeedsOn(@Nonnull String pathExpression) {
		return Matchers.everyItem(Matchers.not(ViolationMatchers.on(pathExpression)));
	}

	/**
	 * Checks that a violation satisfies a set of conditions.
	 */
	@Nonnull
	public static <T> Matcher<ConstraintViolation<T>> violation(
		@Nonnull Collection<Matcher<? super ConstraintViolation<T>>> matchers) {

		return allOf(matchers);
	}

	/**
	 * Checks that a violation occurs on a given property.
	 * <p>
	 * The property can be a nested property expression. For instance, expression {@code foo.bar} would
	 * check that the violation applies to the {@code bar} property of the object accessed
	 * by the {@code foo} property.
	 */
	@Nonnull
	public static <T> Matcher<ConstraintViolation<T>> on(@Nonnull String pathExpression) {
		return new FeatureMatcher<>(path(pathExpression), "on path", "path") {
			@Override
			protected Path featureValueOf(@Nonnull ConstraintViolation<T> actual) {
				return actual.getPropertyPath();
			}
		};
	}

	/**
	 * Checks that a violation's error message template contains a given string.
	 */
	@Nonnull
	public static <T> Matcher<ConstraintViolation<T>> withError(@Nonnull String messagePart) {
		return new FeatureMatcher<>(
			containsString(messagePart),
			"with message",
			"message") {
			@Override
			protected String featureValueOf(@Nonnull ConstraintViolation<T> actual) {
				return actual.getMessageTemplate();
			}
		};
	}

	/**
	 * Checks that a collection of violations contains at least one violation
	 * and that each of the given matchers matches at least one of its elements.
	 */
	@Nonnull
	public static <T> Matcher<Iterable<ConstraintViolation<T>>> violatesAnnotation(@Nonnull Class<?> annotation) {
		return violates(pojo(ConstraintViolation.class)
			.where(ConstraintViolation::getConstraintDescriptor, pojo(ConstraintDescriptor.class)
				.where(ConstraintDescriptor::getAnnotation, pojo(Annotation.class)
					.where(Annotation::annotationType, is(annotation)))));
	}
}
