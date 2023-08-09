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

import javax.annotation.Nonnull;
import javax.validation.Configuration;
import javax.validation.Validation;
import javax.validation.ValidatorFactory;

import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.entities.KindContainer;
import ch.dvbern.ebegu.entities.PensumFachstelle;
import ch.dvbern.ebegu.enums.IntegrationTyp;
import ch.dvbern.ebegu.test.TestDataUtil;
import ch.dvbern.ebegu.validators.CheckPensumFachstelle;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;

import static ch.dvbern.ebegu.tests.util.ValidationTestHelper.assertNotViolated;
import static ch.dvbern.ebegu.tests.util.ValidationTestHelper.assertViolated;

/**
 * Tests for CheckPensumFachstelle
 * SOZIALE_INTEGRATION range from 20 to 60
 * SPRACHLICHE_INTEGRATION range from 40 to 40
 */
public class CheckPensumFachstelleValidatorTest {

	private ValidatorFactory customFactory;
	private static final int SOZ_PENSUM_OK = 40;
	private static final int SOZ_PENSUM_TOO_LOW = 0;
	private static final int SOZ_PENSUM_TOO_HIGH = 80;
	private static final int SPRACH_PENSUM_TOO_HIGH = 41;
	private static final int SPRACH_PENSUM_TOO_LOW = 39;
	private static final int SPRACH_PENSUM_OK = 40;

	@Before
	public void setUp() {
		Configuration<?> config = Validation.byDefaultProvider().configure();
		final ValidationTestConstraintValidatorFactory constraintValidatorFactory =
			new ValidationTestConstraintValidatorFactory();
		config.constraintValidatorFactory(constraintValidatorFactory);
		this.customFactory = config.buildValidatorFactory();
	}

	@Test
	public void testKindWithoutPensumFachstelle() {
		KindContainer kind = createKindWithoutPensumFachstelle();

		assertNotViolated(CheckPensumFachstelle.class, kind, customFactory,
			"pensumFachstelle.pensum");
	}

	@Test
	public void testSozialPensumTooLow() {
		KindContainer kind = createKindWithPensumFachstelle(
			IntegrationTyp.SOZIALE_INTEGRATION, SOZ_PENSUM_TOO_LOW);

		assertViolated(CheckPensumFachstelle.class, kind, customFactory,
			"pensumFachstelle.pensum");
	}

	@Test
	public void testSozialPensumOkSozialPensumTooLow() {
		KindContainer kind = createKindWithPensumFachstelle(
			IntegrationTyp.SOZIALE_INTEGRATION, SOZ_PENSUM_TOO_LOW);

		kind.getKindJA()
			.getPensumFachstelle()
			.add(createPensumFachstelleWithPensum(IntegrationTyp.SOZIALE_INTEGRATION, SOZ_PENSUM_OK));

		assertViolated(CheckPensumFachstelle.class, kind, customFactory,
			"pensumFachstelle.pensum");
	}

	@Test
	public void testSozialPensumTooHigh() {
		KindContainer kind = createKindWithPensumFachstelle(
			IntegrationTyp.SOZIALE_INTEGRATION, SOZ_PENSUM_TOO_HIGH);

		assertViolated(CheckPensumFachstelle.class, kind, customFactory,
			"pensumFachstelle.pensum");
	}

	@Test
	public void testSozialPensumOkSozialPensumTooHigh() {
		KindContainer kind = createKindWithPensumFachstelle(
			IntegrationTyp.SOZIALE_INTEGRATION, SOZ_PENSUM_TOO_HIGH);

		kind.getKindJA()
			.getPensumFachstelle()
			.add(createPensumFachstelleWithPensum(IntegrationTyp.SOZIALE_INTEGRATION, SOZ_PENSUM_OK));

		assertViolated(CheckPensumFachstelle.class, kind, customFactory,
			"pensumFachstelle.pensum");
	}

	@Test
	public void testSozialPensumMax() {
		KindContainer kind = createKindWithPensumFachstelle(
			IntegrationTyp.SOZIALE_INTEGRATION, 60);

		assertNotViolated(CheckPensumFachstelle.class, kind, customFactory,
			"pensumFachstelle.pensum");
	}

	@Test
	public void testSozialPensumMin() {
		KindContainer kind = createKindWithPensumFachstelle(
			IntegrationTyp.SOZIALE_INTEGRATION, 20);

		assertNotViolated(CheckPensumFachstelle.class, kind, customFactory,
			"pensumFachstelle.pensum");
	}

	@Test
	public void testSozialPensumInRange() {
		KindContainer kind = createKindWithPensumFachstelle(
			IntegrationTyp.SOZIALE_INTEGRATION, 40);

		assertNotViolated(CheckPensumFachstelle.class, kind, customFactory,
			"pensumFachstelle.pensum");
	}

	@Test
	public void testTwoSozialPensumInRange() {
		KindContainer kind = createKindWithPensumFachstelle(
			IntegrationTyp.SOZIALE_INTEGRATION, SOZ_PENSUM_OK);

		kind.getKindJA()
			.getPensumFachstelle()
			.add(createPensumFachstelleWithPensum(IntegrationTyp.SOZIALE_INTEGRATION, SOZ_PENSUM_OK));

		assertNotViolated(CheckPensumFachstelle.class, kind, customFactory,
			"pensumFachstelle.pensum");
	}

	@Test
	public void testSprachlichPensumTooLow() {
		KindContainer kind = createKindWithPensumFachstelle(
			IntegrationTyp.SPRACHLICHE_INTEGRATION, SPRACH_PENSUM_TOO_LOW);

		assertViolated(CheckPensumFachstelle.class, kind, customFactory,
			"pensumFachstelle.pensum");
	}

	@Test
	public void testSprachlichPensumOkSprachlichPensumTooLow() {
		KindContainer kind = createKindWithPensumFachstelle(
			IntegrationTyp.SPRACHLICHE_INTEGRATION, SPRACH_PENSUM_TOO_LOW);
		kind.getKindJA()
			.getPensumFachstelle()
			.add(createPensumFachstelleWithPensum(IntegrationTyp.SPRACHLICHE_INTEGRATION, SPRACH_PENSUM_OK));

		assertViolated(CheckPensumFachstelle.class, kind, customFactory,
			"pensumFachstelle.pensum");
	}

	@Test
	public void testSprachlichPensumTooHigh() {
		KindContainer kind = createKindWithPensumFachstelle(
			IntegrationTyp.SPRACHLICHE_INTEGRATION, SPRACH_PENSUM_TOO_HIGH);

		assertViolated(CheckPensumFachstelle.class, kind, customFactory,
			"pensumFachstelle.pensum");
	}

	@Test
	public void testSprachlichPensumOkSprachlichPensumTooHigh() {
		KindContainer kind = createKindWithPensumFachstelle(
			IntegrationTyp.SPRACHLICHE_INTEGRATION, SPRACH_PENSUM_TOO_HIGH);

		kind.getKindJA()
			.getPensumFachstelle()
			.add(createPensumFachstelleWithPensum(IntegrationTyp.SPRACHLICHE_INTEGRATION, SPRACH_PENSUM_TOO_HIGH));

		assertViolated(CheckPensumFachstelle.class, kind, customFactory,
			"pensumFachstelle.pensum");
	}

	@Test
	public void testSprachlichPensumInRange() {
		KindContainer kind = createKindWithPensumFachstelle(
			IntegrationTyp.SPRACHLICHE_INTEGRATION, 40);

		assertNotViolated(CheckPensumFachstelle.class, kind, customFactory,
			"pensumFachstelle.pensum");
	}

	@Test
	public void testTwoSprachlichPensumInRange() {
		KindContainer kind = createKindWithPensumFachstelle(
			IntegrationTyp.SPRACHLICHE_INTEGRATION, SPRACH_PENSUM_OK);

		assertNotViolated(CheckPensumFachstelle.class, kind, customFactory,
			"pensumFachstelle.pensum");
	}

	// HELP METHODS

	@Nonnull
	private KindContainer createKindWithPensumFachstelle(@Nonnull IntegrationTyp integrationTyp, @Nonnull Integer pensum) {
		Gesuch gesuch = TestDataUtil.createDefaultGesuch();
		gesuch.setGesuchsperiode(TestDataUtil.createGesuchsperiode1718());

		KindContainer kind = TestDataUtil.createDefaultKindContainer();
		kind.setGesuch(gesuch);

		MatcherAssert.assertThat(kind.getKindJA().getPensumFachstelle().size(), Matchers.is(1));
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
