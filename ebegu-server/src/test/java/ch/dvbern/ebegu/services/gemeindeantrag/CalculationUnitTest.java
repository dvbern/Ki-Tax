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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package ch.dvbern.ebegu.services.gemeindeantrag;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ch.dvbern.ebegu.entities.AnmeldungTagesschule;
import ch.dvbern.ebegu.entities.BelegungTagesschule;
import ch.dvbern.ebegu.entities.BelegungTagesschuleModul;
import ch.dvbern.ebegu.entities.Kind;
import ch.dvbern.ebegu.entities.KindContainer;
import ch.dvbern.ebegu.entities.ModulTagesschule;
import ch.dvbern.ebegu.entities.ModulTagesschuleGroup;
import ch.dvbern.ebegu.enums.BelegungTagesschuleModulIntervall;
import ch.dvbern.ebegu.enums.EinschulungTyp;
import org.junit.Assert;
import org.junit.Test;

public class CalculationUnitTest {

	@Test()
	public void testIsFruebetreuung() {
		ModulTagesschuleGroup group = new ModulTagesschuleGroup();

		group.setZeitVon(LocalTime.of(8,0));
		Assert.assertTrue(group.isFruehbetreuung());

		group.setZeitVon(LocalTime.of(11,29));
		Assert.assertTrue(group.isFruehbetreuung());

		group.setZeitVon(LocalTime.of(11, 30));
		Assert.assertFalse(group.isFruehbetreuung());
	}

	@Test()
	public void testIsMittagsbetreuung() {
		ModulTagesschuleGroup group = new ModulTagesschuleGroup();

		group.setZeitVon(LocalTime.of(11,30));
		Assert.assertTrue(group.isMittagsbetreuung());

		group.setZeitVon(LocalTime.of(13,14));
		Assert.assertTrue(group.isMittagsbetreuung());

		group.setZeitVon(LocalTime.of(13, 15));
		Assert.assertFalse(group.isMittagsbetreuung());
	}

	@Test()
	public void testNachmittagsbetreuung1() {
		ModulTagesschuleGroup group = new ModulTagesschuleGroup();

		group.setZeitVon(LocalTime.of(13,15));
		Assert.assertTrue(group.isNachmittagbetreuung1());

		group.setZeitVon(LocalTime.of(14,59));
		Assert.assertTrue(group.isNachmittagbetreuung1());

		group.setZeitVon(LocalTime.of(15, 0));
		Assert.assertFalse(group.isNachmittagbetreuung1());
	}

	@Test()
	public void testNachmittagsbetreuung2() {
		ModulTagesschuleGroup group = new ModulTagesschuleGroup();

		group.setZeitVon(LocalTime.of(15,0));
		Assert.assertTrue(group.isNachmittagbetreuung2());

		group.setZeitVon(LocalTime.of(20,0));
		Assert.assertTrue(group.isNachmittagbetreuung2());
	}

	@Test()
	public void testCountAnzahlKinder() {
		LastenausgleichTagesschuleAngabenInstitutionServiceBean latsService = new LastenausgleichTagesschuleAngabenInstitutionServiceBean();

		Kind kind1 = new Kind();
		kind1.setEinschulungTyp(EinschulungTyp.KLASSE1);
		KindContainer kindContainer1 = new KindContainer();
		kindContainer1.setKindJA(kind1);
		AnmeldungTagesschule anmeldung1 = new AnmeldungTagesschule();
		anmeldung1.setKind(kindContainer1);

		Kind kind2 = new Kind();
		kind2.setEinschulungTyp(EinschulungTyp.KINDERGARTEN1);
		KindContainer kindContainer2 = new KindContainer();
		kindContainer2.setKindJA(kind2);
		AnmeldungTagesschule anmeldung2 = new AnmeldungTagesschule();
		anmeldung2.setKind(kindContainer2);

		Kind kind3 = new Kind();
		kind3.setEinschulungTyp(EinschulungTyp.KINDERGARTEN2);
		KindContainer kindContainer3 = new KindContainer();
		kindContainer3.setKindJA(kind3);
		AnmeldungTagesschule anmeldung3 = new AnmeldungTagesschule();
		anmeldung3.setKind(kindContainer3);

		Kind kind4 = new Kind();
		kind4.setEinschulungTyp(EinschulungTyp.VORSCHULALTER);
		KindContainer kindContainer4 = new KindContainer();
		kindContainer4.setKindJA(kind4);
		AnmeldungTagesschule anmeldung4 = new AnmeldungTagesschule();
		anmeldung4.setKind(kindContainer4);

		Kind kind5 = new Kind();
		kind5.setEinschulungTyp(EinschulungTyp.KLASSE6);
		KindContainer kindContainer5 = new KindContainer();
		kindContainer5.setKindJA(kind5);
		AnmeldungTagesschule anmeldung5 = new AnmeldungTagesschule();
		anmeldung5.setKind(kindContainer5);

		List<AnmeldungTagesschule> anmeldungen = new ArrayList<>();
		anmeldungen.add(anmeldung1);
		anmeldungen.add(anmeldung2);
		anmeldungen.add(anmeldung3);
		anmeldungen.add(anmeldung4);
		anmeldungen.add(anmeldung5);

		Map<String, Integer> result = latsService.countAnzahlKinder(anmeldungen);
		Assert.assertEquals((int) result.get("vorschulalter"), 1);
		Assert.assertEquals((int) result.get("kindergarten"), 2);
		Assert.assertEquals((int) result.get("primarstufe"), 2);
		Assert.assertEquals((int) result.get("sekundarstufe"), 0);

	}

	@Test
	public void testCalculateDurchschnittKinderProTag() {
		LastenausgleichTagesschuleAngabenInstitutionServiceBean latsService = new LastenausgleichTagesschuleAngabenInstitutionServiceBean();

		ModulTagesschuleGroup group1 = new ModulTagesschuleGroup();
		group1.setZeitVon(LocalTime.of(8, 0));

		ModulTagesschuleGroup group2 = new ModulTagesschuleGroup();
		group2.setZeitVon(LocalTime.of(14, 0));

		ModulTagesschule modul1 = new ModulTagesschule();
		modul1.setModulTagesschuleGroup(group1);
		modul1.setWochentag(DayOfWeek.MONDAY);
		BelegungTagesschuleModul belegungTagesschuleModul1 = new BelegungTagesschuleModul();
		belegungTagesschuleModul1.setModulTagesschule(modul1);
		belegungTagesschuleModul1.setIntervall(BelegungTagesschuleModulIntervall.WOECHENTLICH);

		ModulTagesschule modul2 = new ModulTagesschule();
		modul2.setModulTagesschuleGroup(group1);
		modul2.setWochentag(DayOfWeek.TUESDAY);
		BelegungTagesschuleModul belegungTagesschuleModul2 = new BelegungTagesschuleModul();
		belegungTagesschuleModul2.setModulTagesschule(modul2);
		belegungTagesschuleModul2.setIntervall(BelegungTagesschuleModulIntervall.WOECHENTLICH);

		ModulTagesschule modul3 = new ModulTagesschule();
		modul3.setModulTagesschuleGroup(group1);
		modul3.setWochentag(DayOfWeek.WEDNESDAY);
		BelegungTagesschuleModul belegungTagesschuleModul3 = new BelegungTagesschuleModul();
		belegungTagesschuleModul3.setModulTagesschule(modul3);
		belegungTagesschuleModul3.setIntervall(BelegungTagesschuleModulIntervall.WOECHENTLICH);

		ModulTagesschule modul4 = new ModulTagesschule();
		modul4.setModulTagesschuleGroup(group1);
		modul4.setWochentag(DayOfWeek.THURSDAY);
		BelegungTagesschuleModul belegungTagesschuleModul4 = new BelegungTagesschuleModul();
		belegungTagesschuleModul4.setModulTagesschule(modul4);
		belegungTagesschuleModul4.setIntervall(BelegungTagesschuleModulIntervall.WOECHENTLICH);

		ModulTagesschule modul5 = new ModulTagesschule();
		modul5.setModulTagesschuleGroup(group1);
		modul5.setWochentag(DayOfWeek.FRIDAY);
		BelegungTagesschuleModul belegungTagesschuleModul5 = new BelegungTagesschuleModul();
		belegungTagesschuleModul5.setModulTagesschule(modul5);
		belegungTagesschuleModul5.setIntervall(BelegungTagesschuleModulIntervall.WOECHENTLICH);

		ModulTagesschule modul6 = new ModulTagesschule();
		modul6.setModulTagesschuleGroup(group2);
		modul6.setWochentag(DayOfWeek.MONDAY);
		BelegungTagesschuleModul belegungTagesschuleModul6 = new BelegungTagesschuleModul();
		belegungTagesschuleModul6.setModulTagesschule(modul6);
		belegungTagesschuleModul6.setIntervall(BelegungTagesschuleModulIntervall.WOECHENTLICH);

		ModulTagesschule modul7 = new ModulTagesschule();
		modul7.setModulTagesschuleGroup(group2);
		modul7.setWochentag(DayOfWeek.TUESDAY);
		BelegungTagesschuleModul belegungTagesschuleModul7 = new BelegungTagesschuleModul();
		belegungTagesschuleModul7.setModulTagesschule(modul7);
		belegungTagesschuleModul7.setIntervall(BelegungTagesschuleModulIntervall.WOECHENTLICH);

		ModulTagesschule modul8 = new ModulTagesschule();
		modul8.setModulTagesschuleGroup(group2);
		modul8.setWochentag(DayOfWeek.WEDNESDAY);
		BelegungTagesschuleModul belegungTagesschuleModul8 = new BelegungTagesschuleModul();
		belegungTagesschuleModul8.setModulTagesschule(modul8);
		belegungTagesschuleModul8.setIntervall(BelegungTagesschuleModulIntervall.ALLE_ZWEI_WOCHEN);

		ModulTagesschule modul9 = new ModulTagesschule();
		modul9.setModulTagesschuleGroup(group2);
		modul9.setWochentag(DayOfWeek.THURSDAY);
		BelegungTagesschuleModul belegungTagesschuleModul9 = new BelegungTagesschuleModul();
		belegungTagesschuleModul9.setModulTagesschule(modul9);
		belegungTagesschuleModul9.setIntervall(BelegungTagesschuleModulIntervall.ALLE_ZWEI_WOCHEN);

		ModulTagesschule modul10 = new ModulTagesschule();
		modul10.setModulTagesschuleGroup(group2);
		modul10.setWochentag(DayOfWeek.FRIDAY);
		BelegungTagesschuleModul belegungTagesschuleModul10 = new BelegungTagesschuleModul();
		belegungTagesschuleModul10.setModulTagesschule(modul10);
		belegungTagesschuleModul10.setIntervall(BelegungTagesschuleModulIntervall.ALLE_ZWEI_WOCHEN);

		Kind kind1 = new Kind();
		kind1.setEinschulungTyp(EinschulungTyp.KLASSE1);
		KindContainer kindContainer1 = new KindContainer();
		kindContainer1.setKindJA(kind1);
		AnmeldungTagesschule anmeldung1 = new AnmeldungTagesschule();
		anmeldung1.setKind(kindContainer1);
		BelegungTagesschule belegungTagesschule1 = new BelegungTagesschule();
		belegungTagesschule1.setBelegungTagesschuleModule(
			Set.of(
				belegungTagesschuleModul1,
				belegungTagesschuleModul2,
				belegungTagesschuleModul3,
				belegungTagesschuleModul4,
				belegungTagesschuleModul5,
				belegungTagesschuleModul6,
				belegungTagesschuleModul7
			)
		);
		anmeldung1.setBelegungTagesschule(belegungTagesschule1);

		Kind kind2 = new Kind();
		kind2.setEinschulungTyp(EinschulungTyp.KINDERGARTEN1);
		KindContainer kindContainer2 = new KindContainer();
		kindContainer2.setKindJA(kind2);
		AnmeldungTagesschule anmeldung2 = new AnmeldungTagesschule();
		anmeldung2.setKind(kindContainer2);
		BelegungTagesschule belegungTagesschule2 = new BelegungTagesschule();
		belegungTagesschule2.setBelegungTagesschuleModule(
			Set.of(
				belegungTagesschuleModul1,
				belegungTagesschuleModul2,
				belegungTagesschuleModul3,
				belegungTagesschuleModul8,
				belegungTagesschuleModul9,
				belegungTagesschuleModul10
			)
		);
		anmeldung2.setBelegungTagesschule(belegungTagesschule2);

		List<AnmeldungTagesschule> anmeldungen = new ArrayList<>();
		anmeldungen.add(anmeldung1);
		anmeldungen.add(anmeldung2);

		Map<String, BigDecimal> result = latsService.calculateDurchschnittKinderProTag(anmeldungen);
		// keine Mittagsbetreuung und keine Nachmittagsbetreuung2
		Assert.assertEquals(new BigDecimal("0.00"), (BigDecimal) result.get("mittagsbetreuung"));
		Assert.assertEquals(new BigDecimal("0.00"), (BigDecimal) result.get("nachmittagsbetreuung2"));
		// 8 Module in Kategorie Frühbetreuung in 5 Tagen
		Assert.assertEquals(new BigDecimal("1.60"), (BigDecimal) result.get("fruehbetreuung"));
		// 2 Wöchentliche Module und 3 zweiwöchentliche Module in Kategorie Nachmittagsbetreuung1 in 5 Tagen
		Assert.assertEquals(new BigDecimal("0.70"), (BigDecimal) result.get("nachmittagsbetreuung1"));
	}
}
