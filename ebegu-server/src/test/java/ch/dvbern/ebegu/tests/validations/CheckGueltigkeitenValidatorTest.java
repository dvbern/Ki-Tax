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
import java.util.HashSet;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.validation.ConstraintViolation;

import ch.dvbern.ebegu.entities.Betreuung;
import ch.dvbern.ebegu.entities.BetreuungspensumContainer;
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.test.TestDataUtil;
import ch.dvbern.ebegu.validators.dateranges.CheckGueltigkeiten;
import ch.dvbern.ebegu.validators.dateranges.CheckGueltigkeitenValidator;
import org.junit.jupiter.api.Test;

import static ch.dvbern.ebegu.tests.util.validation.ViolationMatchers.violatesAnnotation;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.not;

/**
 * Test fuer {@link CheckGueltigkeitenValidator}
 */
public class CheckGueltigkeitenValidatorTest extends AbstractValidatorTest {

	@Test
	public void testCheckBetreuungspensumDatesOverlapping() {
		Betreuung betreuung = createBetreuungWithOverlappedDates(true); //overlapping
		Set<ConstraintViolation<Betreuung>> violations = validate(betreuung);

		assertThat(violations, violatesAnnotation(CheckGueltigkeiten.class));
	}

	@Test
	public void testCheckBetreuungspensumDatesNotOverlapping() {
		Betreuung betreuung = createBetreuungWithOverlappedDates(false); // not overlapping
		Set<ConstraintViolation<Betreuung>> violations = validate(betreuung);

		assertThat(violations, not(violatesAnnotation(CheckGueltigkeiten.class)));
	}

	@Nonnull
	private Betreuung createBetreuungWithOverlappedDates(boolean overlapping) {
		Gesuch gesuch = TestDataUtil.createDefaultGesuch();
		gesuch.setGesuchsperiode(TestDataUtil.createGesuchsperiode1718());
		Betreuung betreuung = TestDataUtil.createDefaultBetreuung();
		betreuung.getKind().setGesuch(gesuch); // Aktuell nur in 1 Richtung verknuepft
		Set<BetreuungspensumContainer> containerSet = new HashSet<>();

		BetreuungspensumContainer betPensContainer = TestDataUtil.createBetPensContainer(betreuung);
		betPensContainer.setBetreuungspensumGS(null); //wir wollen nur JA container testen
		betPensContainer.getBetreuungspensumJA().getGueltigkeit().setGueltigAb(LocalDate.of(2000, 10, 10));
		betPensContainer.getBetreuungspensumJA().getGueltigkeit().setGueltigBis(LocalDate.of(2005, 10, 10));
		containerSet.add(betPensContainer);

		BetreuungspensumContainer betPensContainer2 = TestDataUtil.createBetPensContainer(betreuung);
		betPensContainer2.setBetreuungspensumGS(null);
		betPensContainer2.getBetreuungspensumJA().getGueltigkeit().setGueltigAb(overlapping ? LocalDate.of(2003, 10, 10) : LocalDate.of(2006, 10, 10));
		betPensContainer2.getBetreuungspensumJA().getGueltigkeit().setGueltigBis(LocalDate.of(2008, 10, 10));
		containerSet.add(betPensContainer2);

		betreuung.setBetreuungspensumContainers(containerSet);

		return betreuung;
	}
}
