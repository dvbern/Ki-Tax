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

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.validation.Configuration;
import javax.validation.Validation;
import javax.validation.ValidatorFactory;

import ch.dvbern.ebegu.entities.Betreuung;
import ch.dvbern.ebegu.entities.BetreuungspensumContainer;
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.enums.BetreuungsangebotTyp;
import ch.dvbern.ebegu.test.TestDataUtil;
import ch.dvbern.ebegu.validators.CheckBetreuungspensum;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import static ch.dvbern.ebegu.tests.util.ValidationTestHelper.assertNotViolated;
import static ch.dvbern.ebegu.tests.util.ValidationTestHelper.assertViolated;

/**
 * Tests der den Validator fuer die Werte in Betreuungspensum checkt
 */
public class CheckBetreuungspensumValidatorTest {

	private ValidatorFactory customFactory;

	@Before
	public void setUp() {
		// see https://docs.jboss.org/hibernate/validator/5.2/reference/en-US/html/chapter-bootstrapping.html#_constraintvalidatorfactory
		Configuration<?> config = Validation.byDefaultProvider().configure();
		config.constraintValidatorFactory(new ValidationTestConstraintValidatorFactory(null, null));
		this.customFactory = config.buildValidatorFactory();
	}

	@Test
	public void testKitaGSWrongValue() {
		Betreuung betreuung = createBetreuung(BetreuungsangebotTyp.KITA, BigDecimal.valueOf(9), BigDecimal.valueOf(9));
		// Das passiert weil wir nur den ersten falschen Werten checken. Deswegen als wir den Fehler in betreuungspensumGS finden, checken
		// wir nicht weiter und betreuungspensumJA wirft keine Violation
		assertViolated(CheckBetreuungspensum.class, betreuung, customFactory, "betreuungspensumContainers[0].betreuungspensumGS.pensum");
		assertNotViolated(CheckBetreuungspensum.class, betreuung, customFactory, "betreuungspensumContainers[0].betreuungspensumJA.pensum");
	}

	@Test
	public void testKitaJAWrongValue() {
		Betreuung betreuung = createBetreuung(BetreuungsangebotTyp.KITA, BigDecimal.TEN, BigDecimal.valueOf(9));
		// Jetzt ist betreuungspensumGS richtig und wir finden den Fehler in betreuungspensumJA
		assertNotViolated(CheckBetreuungspensum.class, betreuung, customFactory, "betreuungspensumContainers[0].betreuungspensumGS.pensum");
		assertViolated(CheckBetreuungspensum.class, betreuung, customFactory, "betreuungspensumContainers[0].betreuungspensumJA.pensum");
	}

	@Test
	public void testKitaRightValues() {
		Betreuung betreuung = createBetreuung(BetreuungsangebotTyp.KITA, BigDecimal.TEN, BigDecimal.TEN);
		assertNotViolated(CheckBetreuungspensum.class, betreuung, customFactory, "betreuungspensumContainers[0].betreuungspensumGS.pensum");
		assertNotViolated(CheckBetreuungspensum.class, betreuung, customFactory, "betreuungspensumContainers[0].betreuungspensumJA.pensum");
	}

	@Test
	public void testTageselternGSWrongValue() {
		Betreuung betreuung = createBetreuung(BetreuungsangebotTyp.TAGESFAMILIEN, BigDecimal.valueOf(19), BigDecimal.valueOf(19));
		// Das passiert weil wir nur den ersten falschen Werten checken. Deswegen als wir den Fehler in betreuungspensumGS finden, checken
		// wir nicht weiter und betreuungspensumJA wirft keine Violation
		assertViolated(CheckBetreuungspensum.class, betreuung, customFactory, "betreuungspensumContainers[0].betreuungspensumGS.pensum");
		assertNotViolated(CheckBetreuungspensum.class, betreuung, customFactory, "betreuungspensumContainers[0].betreuungspensumJA.pensum");
	}

	@Test
	public void testTageselternJAWrongValue() {
		Betreuung betreuung = createBetreuung(BetreuungsangebotTyp.TAGESFAMILIEN, BigDecimal.valueOf(20), BigDecimal.valueOf(19));
		// Jetzt ist betreuungspensumGS richtig und wir finden den Fehler in betreuungspensumJA
		assertNotViolated(CheckBetreuungspensum.class, betreuung, customFactory, "betreuungspensumContainers[0].betreuungspensumGS.pensum");
		assertViolated(CheckBetreuungspensum.class, betreuung, customFactory, "betreuungspensumContainers[0].betreuungspensumJA.pensum");
	}

	@Test
	public void testTageselternRightValues() {
		Betreuung betreuung = createBetreuung(BetreuungsangebotTyp.TAGESFAMILIEN, BigDecimal.valueOf(20), BigDecimal.valueOf(20));
		assertNotViolated(CheckBetreuungspensum.class, betreuung, customFactory, "betreuungspensumContainers[0].betreuungspensumGS.pensum");
		assertNotViolated(CheckBetreuungspensum.class, betreuung, customFactory, "betreuungspensumContainers[0].betreuungspensumJA.pensum");
	}

	/**
	 * Fuer Tagesschule gibt es keinen Minimalwert
	 */
	@Test
	public void testTagesschule() {
		Betreuung betreuung = createBetreuung(BetreuungsangebotTyp.TAGESSCHULE, BigDecimal.ONE, BigDecimal.ONE);
		assertNotViolated(CheckBetreuungspensum.class, betreuung, customFactory, "betreuungspensumContainers[0].betreuungspensumGS.pensum");
		assertNotViolated(CheckBetreuungspensum.class, betreuung, customFactory, "betreuungspensumContainers[0].betreuungspensumJA.pensum");
	}

	@Test
	public void testSeveralBetreuungspensumContainers() {
		Betreuung betreuung = createBetreuung(BetreuungsangebotTyp.KITA, BigDecimal.valueOf(10), BigDecimal.valueOf(10));

		BetreuungspensumContainer betPensContainer = TestDataUtil.createBetPensContainer(betreuung);
		Assert.assertNotNull(betPensContainer.getBetreuungspensumGS());
		betPensContainer.getBetreuungspensumGS().setPensum(BigDecimal.valueOf(9));
		betPensContainer.getBetreuungspensumJA().setPensum(BigDecimal.valueOf(10));
		betreuung.getBetreuungspensumContainers().add(betPensContainer);

		//es ist ein Set. Daher muessen wir den Index finden
		//es ist etwas problematisch dass wir den Index dem Client uebergeben wenn es eigentlich auf dem Server ein Set (und nicht eine Liste ist). Der Index wird durch die Sortierung gegeben
		int i = 0;
		for (BetreuungspensumContainer betreuungspensumContainer : betreuung.getBetreuungspensumContainers()) {
			if (betreuungspensumContainer.equals(betPensContainer)) {
				break;
			}
			i++;
		}

		assertViolated(CheckBetreuungspensum.class, betreuung, customFactory, "betreuungspensumContainers[" + i + "].betreuungspensumGS.pensum");
		assertNotViolated(CheckBetreuungspensum.class, betreuung, customFactory, "betreuungspensumContainers[" + i + "].betreuungspensumJA.pensum");
	}

	// HELP METHODS

	/**
	 * creates a Betreeung with {@link BetreuungspensumContainer} gs and ja with the specified Pensum
	 */
	@Nonnull
	private Betreuung createBetreuung(BetreuungsangebotTyp betreuungsangebotTyp, BigDecimal pensumGS, BigDecimal pensumJA) {
		Gesuch gesuch = TestDataUtil.createDefaultGesuch();
		gesuch.setGesuchsperiode(TestDataUtil.createGesuchsperiode1718());
		Betreuung betreuung = TestDataUtil.createDefaultBetreuung();
		betreuung.getKind().setGesuch(gesuch); // Aktuell nur in 1 Richtung verknuepft
		betreuung.getInstitutionStammdaten().setBetreuungsangebotTyp(betreuungsangebotTyp);
		BetreuungspensumContainer betPensContainer = TestDataUtil.createBetPensContainer(betreuung);
		Set<BetreuungspensumContainer> containerSet = new HashSet<>();
		containerSet.add(betPensContainer);
		Assert.assertNotNull(betPensContainer.getBetreuungspensumGS());
		betPensContainer.getBetreuungspensumGS().setPensum(pensumGS);
		betPensContainer.getBetreuungspensumJA().setPensum(pensumJA);
		betreuung.setBetreuungspensumContainers(containerSet);
		return betreuung;
	}
}
