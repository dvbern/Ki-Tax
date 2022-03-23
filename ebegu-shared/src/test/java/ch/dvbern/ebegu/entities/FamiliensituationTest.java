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

import ch.dvbern.ebegu.enums.EnumFamilienstatus;
import ch.dvbern.ebegu.enums.EnumGesuchstellerKardinalitaet;
import org.junit.Assert;
import org.junit.Test;

public class FamiliensituationTest {

	@Test
	public void hasSecondGesuchstellerBevorFKJV() {
		Familiensituation familiensituation = new Familiensituation();
		familiensituation.setFkjvFamSit(false);
		familiensituation.setFamilienstatus(EnumFamilienstatus.ALLEINERZIEHEND);

		//ALLEINERZIEHEND
		LocalDate referenzDatum = LocalDate.of(2021, 8,1);
		Assert.assertEquals(false, familiensituation.hasSecondGesuchsteller(referenzDatum));

		//VERHEIRATET
		familiensituation.setFamilienstatus(EnumFamilienstatus.VERHEIRATET);
		Assert.assertEquals(true, familiensituation.hasSecondGesuchsteller(referenzDatum));

		//KONKUBINAT
		familiensituation.setFamilienstatus(EnumFamilienstatus.KONKUBINAT);
		Assert.assertEquals(true, familiensituation.hasSecondGesuchsteller(referenzDatum));

		//KONKUBINAT_KEIN_KIND
		familiensituation.setFamilienstatus(EnumFamilienstatus.KONKUBINAT_KEIN_KIND);
		familiensituation.setMinDauerKonkubinat(2);
		familiensituation.setStartKonkubinat(referenzDatum);

		Assert.assertEquals(false, familiensituation.hasSecondGesuchsteller(referenzDatum));

		LocalDate startKonkubinat = LocalDate.of(2019, 7,1);
		familiensituation.setStartKonkubinat(startKonkubinat);
		Assert.assertEquals(true, familiensituation.hasSecondGesuchsteller(referenzDatum));
	}

	@Test
	public void hasSecondGesuchstellerAfterFKJV() {
		Familiensituation familiensituation = new Familiensituation();
		familiensituation.setFkjvFamSit(true);
		LocalDate referenzDatum = LocalDate.of(2021, 8,1);

		//PFLEGEFAMILIE
		familiensituation.setFamilienstatus(EnumFamilienstatus.PFLEGEFAMILIE);
		familiensituation.setGesuchstellerKardinalitaet(EnumGesuchstellerKardinalitaet.ZU_ZWEIT);
		Assert.assertEquals(true, familiensituation.hasSecondGesuchsteller(referenzDatum));

		familiensituation.setGesuchstellerKardinalitaet(EnumGesuchstellerKardinalitaet.ALLEINE);
		Assert.assertEquals(false, familiensituation.hasSecondGesuchsteller(referenzDatum));

		//VERHEIRATET
		familiensituation.setFamilienstatus(EnumFamilienstatus.VERHEIRATET);
		Assert.assertEquals(true, familiensituation.hasSecondGesuchsteller(referenzDatum));

		//KONKUBINAT
		familiensituation.setFamilienstatus(EnumFamilienstatus.KONKUBINAT);
		Assert.assertEquals(true, familiensituation.hasSecondGesuchsteller(referenzDatum));

		//KONKUBINAT_KEIN_KIND
		familiensituation.setFamilienstatus(EnumFamilienstatus.KONKUBINAT_KEIN_KIND);
		familiensituation.setMinDauerKonkubinat(2);
		familiensituation.setStartKonkubinat(referenzDatum);

		Assert.assertEquals(false, familiensituation.hasSecondGesuchsteller(referenzDatum));

		LocalDate startKonkubinat = LocalDate.of(2019, 7,1);
		familiensituation.setStartKonkubinat(startKonkubinat);
		Assert.assertEquals(true, familiensituation.hasSecondGesuchsteller(referenzDatum));
	}

	@Test
	public void hasSecondGesuchstellerFKJVAlleinerziehendTest() {
		Familiensituation familiensituation = new Familiensituation();
		familiensituation.setFkjvFamSit(true);
		familiensituation.setFamilienstatus(EnumFamilienstatus.ALLEINERZIEHEND);
		familiensituation.setGesuchstellerKardinalitaet(EnumGesuchstellerKardinalitaet.ALLEINE);
		familiensituation.setGeteilteObhut(true);
		LocalDate referenzDatum = LocalDate.of(2021, 8,1);

		//GETEILTE OBHUT
		Assert.assertFalse(familiensituation.hasSecondGesuchsteller(referenzDatum));
		familiensituation.setGesuchstellerKardinalitaet(EnumGesuchstellerKardinalitaet.ZU_ZWEIT);
		Assert.assertTrue(familiensituation.hasSecondGesuchsteller(referenzDatum));

		//NICHT GETEILTE OBHUT ABER UNTERHALTSVEREINBARUNG
		familiensituation.setGeteilteObhut(false);
		familiensituation.setGesuchstellerKardinalitaet(null);
		familiensituation.setUnterhaltsvereinbarung(true);
		Assert.assertFalse(familiensituation.hasSecondGesuchsteller(referenzDatum));

		//NICHT GETEILTE OBHUT UND KEINE UNTERHALTSVEREINBARUNG
		familiensituation.setUnterhaltsvereinbarung(false);
		Assert.assertTrue(familiensituation.hasSecondGesuchsteller(referenzDatum));
	}
}
