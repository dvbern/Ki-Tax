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

package ch.dvbern.ebegu.tests.validations;

import java.util.Set;

import javax.annotation.Nonnull;
import javax.validation.ConstraintViolation;

import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.entities.KindContainer;
import ch.dvbern.ebegu.entities.PensumFachstelle;
import ch.dvbern.ebegu.enums.IntegrationTyp;
import ch.dvbern.ebegu.test.TestDataUtil;
import ch.dvbern.ebegu.validators.CheckPensumFachstelle;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static ch.dvbern.ebegu.tests.util.validation.ViolationMatchers.violatesAnnotation;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.not;

/**
 * Tests for CheckPensumFachstelle
 * SOZIALE_INTEGRATION range from 20 to 60
 * SPRACHLICHE_INTEGRATION range from 40 to 40
 */
class CheckPensumFachstelleValidatorTest extends AbstractValidatorTest {

	private static final int SOZ_PENSUM_OK = 40;
	private static final int SOZ_PENSUM_TOO_LOW = 0;
	private static final int SOZ_PENSUM_TOO_HIGH = 80;
	private static final int SPRACH_PENSUM_TOO_HIGH = 41;
	private static final int SPRACH_PENSUM_TOO_LOW = 39;
	private static final int SPRACH_PENSUM_OK = 40;

	@Test
	void testKindWithoutPensumFachstelle() {
		KindContainer kind = createKindWithoutPensumFachstelle();

		Set<ConstraintViolation<KindContainer>> violations = validate(kind);

		assertThat(violations, not(violatesAnnotation(CheckPensumFachstelle.class)));
	}

	@Test
	void testSozialPensumTooLow() {
		KindContainer kind = createKindWithPensumFachstelle(
			IntegrationTyp.SOZIALE_INTEGRATION, SOZ_PENSUM_TOO_LOW);

		Set<ConstraintViolation<KindContainer>> violations = validate(kind);

		assertThat(violations, violatesAnnotation(CheckPensumFachstelle.class));
	}

	@Test
	void testSozialPensumOkSozialPensumTooLow() {
		KindContainer kind = createKindWithPensumFachstelle(
			IntegrationTyp.SOZIALE_INTEGRATION, SOZ_PENSUM_TOO_LOW);

		kind.getKindJA()
			.getPensumFachstelle()
			.add(createPensumFachstelleWithPensum(IntegrationTyp.SOZIALE_INTEGRATION, SOZ_PENSUM_OK));

		Set<ConstraintViolation<KindContainer>> violations = validate(kind);

		assertThat(violations, violatesAnnotation(CheckPensumFachstelle.class));
	}

	@Test
	void testSozialPensumTooHigh() {
		KindContainer kind = createKindWithPensumFachstelle(
			IntegrationTyp.SOZIALE_INTEGRATION, SOZ_PENSUM_TOO_HIGH);

		Set<ConstraintViolation<KindContainer>> violations = validate(kind);

		assertThat(violations, violatesAnnotation(CheckPensumFachstelle.class));
	}

	@Test
	void testSozialPensumOkSozialPensumTooHigh() {
		KindContainer kind = createKindWithPensumFachstelle(
			IntegrationTyp.SOZIALE_INTEGRATION, SOZ_PENSUM_TOO_HIGH);

		kind.getKindJA()
			.getPensumFachstelle()
			.add(createPensumFachstelleWithPensum(IntegrationTyp.SOZIALE_INTEGRATION, SOZ_PENSUM_OK));

		Set<ConstraintViolation<KindContainer>> violations = validate(kind);

		assertThat(violations, violatesAnnotation(CheckPensumFachstelle.class));
	}

	@ParameterizedTest
	@ValueSource(ints = {
		60, // max
		20, // min
		40 // in range
	})
	void testSozialPensumMax(int pensum) {
		KindContainer kind = createKindWithPensumFachstelle(IntegrationTyp.SOZIALE_INTEGRATION, pensum);

		Set<ConstraintViolation<KindContainer>> violations = validate(kind);

		assertThat(violations, not(violatesAnnotation(CheckPensumFachstelle.class)));
	}

	@Test
	void testTwoSozialPensumInRange() {
		KindContainer kind = createKindWithPensumFachstelle(
			IntegrationTyp.SOZIALE_INTEGRATION, SOZ_PENSUM_OK);

		kind.getKindJA()
			.getPensumFachstelle()
			.add(createPensumFachstelleWithPensum(IntegrationTyp.SOZIALE_INTEGRATION, SOZ_PENSUM_OK));

		Set<ConstraintViolation<KindContainer>> violations = validate(kind);

		assertThat(violations, not(violatesAnnotation(CheckPensumFachstelle.class)));
	}

	@Test
	void testSprachlichPensumTooLow() {
		KindContainer kind = createKindWithPensumFachstelle(
			IntegrationTyp.SPRACHLICHE_INTEGRATION, SPRACH_PENSUM_TOO_LOW);

		Set<ConstraintViolation<KindContainer>> violations = validate(kind);

		assertThat(violations, violatesAnnotation(CheckPensumFachstelle.class));
	}

	@Test
	void testSprachlichPensumOkSprachlichPensumTooLow() {
		KindContainer kind = createKindWithPensumFachstelle(
			IntegrationTyp.SPRACHLICHE_INTEGRATION, SPRACH_PENSUM_TOO_LOW);
		kind.getKindJA()
			.getPensumFachstelle()
			.add(createPensumFachstelleWithPensum(IntegrationTyp.SPRACHLICHE_INTEGRATION, SPRACH_PENSUM_OK));

		Set<ConstraintViolation<KindContainer>> violations = validate(kind);

		assertThat(violations, violatesAnnotation(CheckPensumFachstelle.class));
	}

	@Test
	void testSprachlichPensumTooHigh() {
		KindContainer kind = createKindWithPensumFachstelle(
			IntegrationTyp.SPRACHLICHE_INTEGRATION, SPRACH_PENSUM_TOO_HIGH);

		Set<ConstraintViolation<KindContainer>> violations = validate(kind);

		assertThat(violations, violatesAnnotation(CheckPensumFachstelle.class));
	}

	@Test
	void testSprachlichPensumOkSprachlichPensumTooHigh() {
		KindContainer kind = createKindWithPensumFachstelle(
			IntegrationTyp.SPRACHLICHE_INTEGRATION, SPRACH_PENSUM_TOO_HIGH);

		kind.getKindJA()
			.getPensumFachstelle()
			.add(createPensumFachstelleWithPensum(IntegrationTyp.SPRACHLICHE_INTEGRATION, SPRACH_PENSUM_TOO_HIGH));

		Set<ConstraintViolation<KindContainer>> violations = validate(kind);

		assertThat(violations, violatesAnnotation(CheckPensumFachstelle.class));
	}

	@Test
	void testSprachlichPensumInRange() {
		KindContainer kind = createKindWithPensumFachstelle(
			IntegrationTyp.SPRACHLICHE_INTEGRATION, 40);

		Set<ConstraintViolation<KindContainer>> violations = validate(kind);

		assertThat(violations, not(violatesAnnotation(CheckPensumFachstelle.class)));
	}

	@Test
	void testTwoSprachlichPensumInRange() {
		KindContainer kind = createKindWithPensumFachstelle(
			IntegrationTyp.SPRACHLICHE_INTEGRATION, SPRACH_PENSUM_OK);

		Set<ConstraintViolation<KindContainer>> violations = validate(kind);

		assertThat(violations, not(violatesAnnotation(CheckPensumFachstelle.class)));
	}

	// HELP METHODS

	@Nonnull
	private KindContainer createKindWithPensumFachstelle(@Nonnull IntegrationTyp integrationTyp, @Nonnull Integer pensum) {
		Gesuch gesuch = TestDataUtil.createDefaultGesuch();
		gesuch.setGesuchsperiode(TestDataUtil.createGesuchsperiode1718());

		KindContainer kind = TestDataUtil.createDefaultKindContainer();
		kind.setGesuch(gesuch);

		assertThat(kind.getKindJA().getPensumFachstelle().size(), Matchers.is(1));
		PensumFachstelle pensumFachstelle = kind.getKindJA().getPensumFachstelle().stream().findFirst().orElseThrow();
		pensumFachstelle.setIntegrationTyp(integrationTyp);
		pensumFachstelle.setPensum(pensum);

		return kind;
	}

	@Nonnull
	private KindContainer createKindWithoutPensumFachstelle() {
		KindContainer kind = TestDataUtil.createKindContainerWithoutFachstelle();

		return kind;
	}

	@Nonnull
	private static PensumFachstelle createPensumFachstelleWithPensum(IntegrationTyp integrationTyp, int pensum) {
		PensumFachstelle pensumFachstelle = new PensumFachstelle();
		pensumFachstelle.setPensum(pensum);
		pensumFachstelle.setIntegrationTyp(integrationTyp);
		return pensumFachstelle;
	}
}
