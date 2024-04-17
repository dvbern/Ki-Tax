/*
 * Copyright (C) 2021 DV Bern AG, Switzerland
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

package ch.dvbern.ebegu.entities;

import java.time.LocalDate;
import java.util.stream.Stream;

import ch.dvbern.ebegu.enums.EnumFamilienstatus;
import ch.dvbern.ebegu.enums.EnumGesuchstellerKardinalitaet;
import ch.dvbern.ebegu.enums.UnterhaltsvereinbarungAnswer;
import ch.dvbern.ebegu.types.DateRange;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

class FamiliensituationTest {

	@Test
	void hasSecondGesuchstellerBevorFKJV() {
		Familiensituation familiensituation = new Familiensituation();
		familiensituation.setFkjvFamSit(false);
		familiensituation.setFamilienstatus(EnumFamilienstatus.ALLEINERZIEHEND);

		//ALLEINERZIEHEND
		LocalDate referenzDatum = LocalDate.of(2021, 8, 1);
		Assertions.assertEquals(false, familiensituation.hasSecondGesuchsteller(referenzDatum));

		//VERHEIRATET
		familiensituation.setFamilienstatus(EnumFamilienstatus.VERHEIRATET);
		Assertions.assertEquals(true, familiensituation.hasSecondGesuchsteller(referenzDatum));

		//KONKUBINAT
		familiensituation.setFamilienstatus(EnumFamilienstatus.KONKUBINAT);
		Assertions.assertEquals(true, familiensituation.hasSecondGesuchsteller(referenzDatum));

		//KONKUBINAT_KEIN_KIND
		familiensituation.setFamilienstatus(EnumFamilienstatus.KONKUBINAT_KEIN_KIND);
		familiensituation.setMinDauerKonkubinat(2);
		familiensituation.setStartKonkubinat(referenzDatum);

		Assertions.assertEquals(false, familiensituation.hasSecondGesuchsteller(referenzDatum));

		LocalDate startKonkubinat = LocalDate.of(2019, 7, 1);
		familiensituation.setStartKonkubinat(startKonkubinat);
		Assertions.assertEquals(true, familiensituation.hasSecondGesuchsteller(referenzDatum));
	}

	@Test
	void hasSecondGesuchstellerAfterFKJV() {
		Familiensituation familiensituation = new Familiensituation();
		familiensituation.setFkjvFamSit(true);
		LocalDate referenzDatum = LocalDate.of(2021, 8, 1);

		//PFLEGEFAMILIE
		familiensituation.setFamilienstatus(EnumFamilienstatus.PFLEGEFAMILIE);
		familiensituation.setGesuchstellerKardinalitaet(EnumGesuchstellerKardinalitaet.ZU_ZWEIT);
		Assertions.assertEquals(true, familiensituation.hasSecondGesuchsteller(referenzDatum));

		familiensituation.setGesuchstellerKardinalitaet(EnumGesuchstellerKardinalitaet.ALLEINE);
		Assertions.assertEquals(false, familiensituation.hasSecondGesuchsteller(referenzDatum));

		//VERHEIRATET
		familiensituation.setFamilienstatus(EnumFamilienstatus.VERHEIRATET);
		Assertions.assertEquals(true, familiensituation.hasSecondGesuchsteller(referenzDatum));

		//KONKUBINAT
		familiensituation.setFamilienstatus(EnumFamilienstatus.KONKUBINAT);
		Assertions.assertEquals(true, familiensituation.hasSecondGesuchsteller(referenzDatum));

		//KONKUBINAT_KEIN_KIND
		familiensituation.setFamilienstatus(EnumFamilienstatus.KONKUBINAT_KEIN_KIND);
		familiensituation.setMinDauerKonkubinat(2);
		familiensituation.setStartKonkubinat(referenzDatum);

		Assertions.assertEquals(false, familiensituation.hasSecondGesuchsteller(referenzDatum));

		LocalDate startKonkubinat = LocalDate.of(2019, 7, 1);
		familiensituation.setStartKonkubinat(startKonkubinat);
		Assertions.assertEquals(true, familiensituation.hasSecondGesuchsteller(referenzDatum));
	}

	@Test
	void hasSecondGesuchstellerFKJVAlleinerziehendTest() {
		Familiensituation familiensituation = new Familiensituation();
		familiensituation.setFkjvFamSit(true);
		familiensituation.setFamilienstatus(EnumFamilienstatus.ALLEINERZIEHEND);
		familiensituation.setGesuchstellerKardinalitaet(EnumGesuchstellerKardinalitaet.ALLEINE);
		familiensituation.setGeteilteObhut(true);
		LocalDate referenzDatum = LocalDate.of(2021, 8, 1);

		//GETEILTE OBHUT
		Assertions.assertFalse(familiensituation.hasSecondGesuchsteller(referenzDatum));
		familiensituation.setGesuchstellerKardinalitaet(EnumGesuchstellerKardinalitaet.ZU_ZWEIT);
		Assertions.assertTrue(familiensituation.hasSecondGesuchsteller(referenzDatum));

		//NICHT GETEILTE OBHUT ABER UNTERHALTSVEREINBARUNG
		familiensituation.setGeteilteObhut(false);
		familiensituation.setGesuchstellerKardinalitaet(null);
		familiensituation.setUnterhaltsvereinbarung(UnterhaltsvereinbarungAnswer.JA_UNTERHALTSVEREINBARUNG);
		Assertions.assertFalse(familiensituation.hasSecondGesuchsteller(referenzDatum));

		//NICHT GETEILTE OBHUT UND KEINE UNTERHALTSVEREINBARUNG
		familiensituation.setUnterhaltsvereinbarung(UnterhaltsvereinbarungAnswer.NEIN_UNTERHALTSVEREINBARUNG);
		Assertions.assertTrue(familiensituation.hasSecondGesuchsteller(referenzDatum));
	}

	@ParameterizedTest
	@MethodSource("startKonkubinatSource")
	void shouldNotBeReachingMinDauerOfInGP2IfStartKonkubinatIs(LocalDate startKonkubinat) {
		Familiensituation familiensituation = new Familiensituation();
		Gesuchsperiode gesuchsperiode = new Gesuchsperiode();
		gesuchsperiode.setGueltigkeit(new DateRange(LocalDate.of(2024, 8, 1), LocalDate.of(2025, 7, 31)));
		familiensituation.setStartKonkubinat(startKonkubinat);
		assertThat(familiensituation.isKonkubinatReachingMinDauerIn(gesuchsperiode), is(false));
	}

	static Stream<LocalDate> startKonkubinatSource() {
		return Stream.of(
			LocalDate.of(2022, 7, 31), // older than 2 years
			LocalDate.of(2022, 8, 1), // two years before GP start
			LocalDate.of(2023, 1, 1), // two years before during GP
			LocalDate.of(2023, 7, 31), // 2 years before GP end
			LocalDate.of(2023, 8, 1) // Younger than 2 years efore GP end
		);
	}
}
