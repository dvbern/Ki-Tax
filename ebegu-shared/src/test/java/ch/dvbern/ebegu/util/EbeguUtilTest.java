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

package ch.dvbern.ebegu.util;

import java.time.LocalDate;
import java.util.Set;
import java.util.TreeSet;

import ch.dvbern.ebegu.entities.Betreuung;
import ch.dvbern.ebegu.entities.Dossier;
import ch.dvbern.ebegu.entities.Fall;
import ch.dvbern.ebegu.entities.Familiensituation;
import ch.dvbern.ebegu.entities.FamiliensituationContainer;
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.entities.KindContainer;
import ch.dvbern.ebegu.entities.Mandant;
import ch.dvbern.ebegu.enums.AntragStatus;
import ch.dvbern.ebegu.enums.Betreuungsstatus;
import ch.dvbern.ebegu.enums.EnumFamilienstatus;
import ch.dvbern.ebegu.util.mandant.MandantIdentifier;
import org.junit.Assert;
import org.junit.Test;

/**
 * test fuer Ebeguutil
 */
public class EbeguUtilTest {

	@Test
	public void testFromOneGSToTwoGS_From2To1() {

		Familiensituation oldData = new Familiensituation();
		oldData.setFamilienstatus(EnumFamilienstatus.VERHEIRATET);
		Familiensituation newData = new Familiensituation();
		newData.setFamilienstatus(EnumFamilienstatus.ALLEINERZIEHEND);
		FamiliensituationContainer fsc = new FamiliensituationContainer();
		fsc.setFamiliensituationErstgesuch(oldData);
		fsc.setFamiliensituationJA(newData);

		Assert.assertFalse(EbeguUtil.fromOneGSToTwoGS(fsc, LocalDate.now()));
	}

	@Test
	public void testFromOneGSToTwoGS_From2To2() {
		Familiensituation oldData = new Familiensituation();
		oldData.setFamilienstatus(EnumFamilienstatus.VERHEIRATET);
		Familiensituation newData = new Familiensituation();
		newData.setFamilienstatus(EnumFamilienstatus.KONKUBINAT);

		FamiliensituationContainer fsc = new FamiliensituationContainer();
		fsc.setFamiliensituationErstgesuch(oldData);
		fsc.setFamiliensituationJA(newData);

		Assert.assertFalse(EbeguUtil.fromOneGSToTwoGS(fsc, LocalDate.now()));
	}

	@Test
	public void testFromOneGSToTwoGS_From1To1() {
		Familiensituation oldData = new Familiensituation();
		oldData.setFamilienstatus(EnumFamilienstatus.KONKUBINAT_KEIN_KIND);
		oldData.setStartKonkubinat(LocalDate.now());

		Familiensituation newData = new Familiensituation();
		newData.setFamilienstatus(EnumFamilienstatus.ALLEINERZIEHEND);

		FamiliensituationContainer fsc = new FamiliensituationContainer();
		fsc.setFamiliensituationErstgesuch(oldData);
		fsc.setFamiliensituationJA(newData);

		Assert.assertFalse(EbeguUtil.fromOneGSToTwoGS(fsc, LocalDate.now()));
	}

	@Test
	public void testFromOneGSToTwoGS_From1To2() {
		Familiensituation oldData = new Familiensituation();
		oldData.setFamilienstatus(EnumFamilienstatus.ALLEINERZIEHEND);
		Familiensituation newData = new Familiensituation();
		newData.setFamilienstatus(EnumFamilienstatus.VERHEIRATET);

		FamiliensituationContainer fsc = new FamiliensituationContainer();
		fsc.setFamiliensituationErstgesuch(oldData);
		fsc.setFamiliensituationJA(newData);

		Assert.assertTrue(EbeguUtil.fromOneGSToTwoGS(fsc, LocalDate.now()));
	}

	@Test
	public void testFromOneGSToTwoGS_nullFamilienstatus() {
		Familiensituation oldData = new Familiensituation();
		Familiensituation newData = new Familiensituation();

		FamiliensituationContainer fsc = new FamiliensituationContainer();
		fsc.setFamiliensituationErstgesuch(oldData);
		fsc.setFamiliensituationJA(newData);

		Assert.assertFalse(EbeguUtil.fromOneGSToTwoGS(fsc, LocalDate.now()));
	}

	@Test
	public void toFilename() {
		String filename = EbeguUtil.toFilename("Kita Beundenweg/Crèche Oeuches.pdf");
		Assert.assertNotNull(filename);
		Assert.assertEquals("Kita_Beundenweg_Crèche_Oeuches.pdf", filename);
	}

	@Test
	public void isErlaeuterungenZurVerfuegungMit0AnspruchRequiredTest() {
		Gesuch gesuch = prepareGesuchForErlaeuterungenZurVerguegungTests(MandantIdentifier.BERN);
		// 0 Anspruch sollte keine Erlaeterung sein:
		Assert.assertFalse(EbeguUtil.isErlaeuterungenZurVerfuegungRequired(gesuch));
	}
	@Test
	public void isErlaeuterungenZurVerfuegungMit0AnspruchRequiredFuerAppenzellTest() {
		Gesuch gesuch = prepareGesuchForErlaeuterungenZurVerguegungTests(MandantIdentifier.APPENZELL_AUSSERRHODEN);
		// 0 Anspruch aber beim Appenzell ist einer Ausnahme so es muss immer sein:
		Assert.assertTrue(EbeguUtil.isErlaeuterungenZurVerfuegungRequired(gesuch));
	}

	private Gesuch prepareGesuchForErlaeuterungenZurVerguegungTests(MandantIdentifier mandantIdentifier) {
		Mandant mandant = new Mandant();
		mandant.setMandantIdentifier(mandantIdentifier);
		Fall fall = new Fall();
		fall.setMandant(mandant);
		Dossier dossier = new Dossier();
		dossier.setFall(fall);
		Gesuch gesuch = new Gesuch();
		gesuch.setDossier(dossier);
		gesuch.setStatus(AntragStatus.VERFUEGT);
		Betreuung betreuung = new Betreuung();
		betreuung.setBetreuungsstatus(Betreuungsstatus.VERFUEGT);
		Set<Betreuung> betreuungen = new TreeSet<>();
		betreuungen.add(betreuung);
		KindContainer kindContainer = new KindContainer();
		kindContainer.setBetreuungen(betreuungen);
		gesuch.addKindContainer(kindContainer);
		return gesuch;
	}
}
