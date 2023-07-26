/*
 * Ki-Tax: System for the management of external childcare subsidies
 * Copyright (C) 2017 City of Bern Switzerland
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package ch.dvbern.ebegu.tests.validations;

import java.time.LocalDate;

import javax.annotation.Nonnull;
import javax.validation.ConstraintValidatorContext;

import org.easymock.EasyMockExtension;
import org.easymock.Mock;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import ch.dvbern.ebegu.entities.Betreuung;
import ch.dvbern.ebegu.entities.Fachstelle;
import ch.dvbern.ebegu.entities.KindContainer;
import ch.dvbern.ebegu.entities.PensumFachstelle;
import ch.dvbern.ebegu.enums.IntegrationTyp;
import ch.dvbern.ebegu.test.TestDataUtil;
import ch.dvbern.ebegu.types.DateRange;
import ch.dvbern.ebegu.validators.CheckPensumFachstellenOverlappingValidator;

/**
 * Test fuer {@link ch.dvbern.ebegu.validators.CheckPensumFachstellenOverlappingValidator}
 */
@ExtendWith(EasyMockExtension.class)
public class CheckFachstellenPetreuungspensumOverlappingValidatorTest {

	private CheckPensumFachstellenOverlappingValidator validator;
	@Mock
	private ConstraintValidatorContext contextMock;

	private static final int BASIS_JAHR = 2023;
	private static final int BASIS_JAHR_PLUS = 2024;
	private static final LocalDate AUGUST_FIRST = LocalDate.of(BASIS_JAHR, 8, 1);
	private static final LocalDate OCTOBER_31 = LocalDate.of(BASIS_JAHR, 10, 31);
	private static final LocalDate NOV_FIRST = LocalDate.of(BASIS_JAHR, 11, 1);
	private static final LocalDate JAN_FIRST = LocalDate.of(BASIS_JAHR_PLUS, 1, 1);
	private static final LocalDate FEB_FIRST = LocalDate.of(BASIS_JAHR_PLUS, 2, 1);
	private static final LocalDate JUL_31 = LocalDate.of(BASIS_JAHR_PLUS, 7, 31);

	@BeforeEach
	public void setUp() {
		this.validator = new CheckPensumFachstellenOverlappingValidator();
	}

	@Test
	public void testCheckBetreuungspensumTwoDatesOverlapping() {
		DateRange augTilJan = new DateRange(AUGUST_FIRST, JAN_FIRST);
		DateRange augTilOct = new DateRange(AUGUST_FIRST, OCTOBER_31);
		KindContainer kindContainer = createBetreuungWithFachstellenFachstellen(augTilJan, augTilOct); //overlapping
		Assertions.assertFalse(validator.isValid(kindContainer, contextMock));
	}

	@Test
	public void testCheckBetreuungspensumTwoDatesOneDayOverlapping() {
		DateRange augTilOct = new DateRange(AUGUST_FIRST, OCTOBER_31);
		DateRange octTilJan = new DateRange(OCTOBER_31, JAN_FIRST);
		KindContainer kindContainer = createBetreuungWithFachstellenFachstellen(augTilOct, octTilJan); //overlapping
		Assertions.assertFalse(validator.isValid(kindContainer, contextMock));
	}

	@Test
	public void testCheckBetreuungspensumThreeDatesOneOverlapping() {
		DateRange augTilJan = new DateRange(AUGUST_FIRST, OCTOBER_31);
		DateRange augTilOct = new DateRange(NOV_FIRST, JAN_FIRST);
		DateRange octTilJan = new DateRange(OCTOBER_31, JAN_FIRST);
		KindContainer kindContainer = createBetreuungWithFachstellenFachstellen(augTilJan, augTilOct, octTilJan); //overlapping
		Assertions.assertFalse(validator.isValid(kindContainer, contextMock));
	}

	@Test
	public void testCheckBetreuungspensumEmptyNotOverlapping() {
		KindContainer kindContainer = createBetreuungWithFachstellenFachstellen(); //overlapping
		Assertions.assertTrue(validator.isValid(kindContainer, contextMock));
	}

	@Test
	public void testCheckPensumFachstellenTwoDatesNotOverlapping() {
		DateRange augTilOct = new DateRange(AUGUST_FIRST, OCTOBER_31);
		DateRange novTilJan = new DateRange(NOV_FIRST, JAN_FIRST);
		KindContainer kindContainer = createBetreuungWithFachstellenFachstellen(augTilOct, novTilJan); // not overlapping
		Assertions.assertTrue(validator.isValid(kindContainer, contextMock));
	}

	@Test
	public void testCheckPensumFachstellenThreeDatesNotOverlapping() {
		DateRange augTilOct = new DateRange(AUGUST_FIRST, OCTOBER_31);
		DateRange novTilJan = new DateRange(NOV_FIRST, JAN_FIRST);
		DateRange febTilJul = new DateRange(FEB_FIRST, JUL_31);
		KindContainer kindContainer = createBetreuungWithFachstellenFachstellen(augTilOct, novTilJan, febTilJul); // not overlapping
		Assertions.assertTrue(validator.isValid(kindContainer, contextMock));
	}

	@Nonnull
	private KindContainer createBetreuungWithFachstellenFachstellen(DateRange ... gueltigkeiten) {
		Betreuung betreuung = TestDataUtil.createDefaultBetreuung();
		final KindContainer kindContainer = betreuung.getKind();
		kindContainer.getKindJA().getPensumFachstelle().clear();

		for (DateRange gueltigkeit : gueltigkeiten) {
			final PensumFachstelle pensumFachstelle = createPensumFachstelle(gueltigkeit);
			kindContainer.getKindJA().getPensumFachstelle().add(pensumFachstelle);
		}


		return kindContainer;
	}

	@Nonnull
	private static PensumFachstelle createPensumFachstelle(DateRange gueltigkeit) {
		final PensumFachstelle pensumFachstelle = new PensumFachstelle();
		pensumFachstelle.setFachstelle(new Fachstelle());
		pensumFachstelle.setPensum(40);
		pensumFachstelle.setIntegrationTyp(IntegrationTyp.SOZIALE_INTEGRATION);
		pensumFachstelle.setGueltigkeit(gueltigkeit);
		return pensumFachstelle;
	}
}
