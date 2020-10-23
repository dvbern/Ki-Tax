/*
 * Copyright (C) 2020 DV Bern AG, Switzerland
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

package ch.dvbern.ebegu.tests.rules;

import java.time.LocalDate;
import java.time.Month;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import javax.annotation.Nonnull;

import ch.dvbern.ebegu.entities.Betreuung;
import ch.dvbern.ebegu.entities.Gemeinde;
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.entities.Gesuchsperiode;
import ch.dvbern.ebegu.entities.InstitutionStammdaten;
import ch.dvbern.ebegu.entities.KindContainer;
import ch.dvbern.ebegu.entities.Verfuegung;
import ch.dvbern.ebegu.entities.VerfuegungZeitabschnitt;
import ch.dvbern.ebegu.enums.AntragStatus;
import ch.dvbern.ebegu.enums.Betreuungsstatus;
import ch.dvbern.ebegu.enums.EinschulungTyp;
import ch.dvbern.ebegu.enums.MsgKey;
import ch.dvbern.ebegu.rechner.AbstractBGRechnerTest;
import ch.dvbern.ebegu.rechner.BGRechnerParameterDTO;
import ch.dvbern.ebegu.test.TestDataUtil;
import ch.dvbern.ebegu.testfaelle.Testfall02_FeutzYvonne;
import ch.dvbern.ebegu.util.Constants;
import ch.dvbern.ebegu.util.KitaxUebergangsloesungParameter;
import ch.dvbern.ebegu.util.KitaxUtil;
import ch.dvbern.ebegu.util.MathUtil;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * In Faellen mit mehreren Betreuungen fuer dasselbe Kind, wovon in einer Betreuung der Anspruch
 * manuell auf 0 gesetzt wird, in der anderen aber nicht, stimmt die Berechnung des Restanspruchs
 * nicht mehr.
 */
public class DoppelkitaMitManuellemAnspruchNullTest extends AbstractBGRechnerTest {

	private KitaxUebergangsloesungParameter kitaxUebergangsloesungParameter = TestDataUtil.geKitaxUebergangsloesungParameter();

	final Gesuchsperiode gesuchsperiode1718 = TestDataUtil.createGesuchsperiode1718();
	final Gemeinde paris = TestDataUtil.createGemeindeParis();
	final InstitutionStammdaten kita_1 = TestDataUtil.createInstitutionStammdatenKitaBruennen();
	final InstitutionStammdaten kita_2 = TestDataUtil.createInstitutionStammdatenKitaWeissenstein();

	private Gesuch gesuch;

	@Before
	public void setUp() {
		kitaxUebergangsloesungParameter.setStadtBernAsivStartDate(LocalDate.of(gesuchsperiode1718.getBasisJahrPlus2(), Month.JANUARY, 1));

		List<InstitutionStammdaten> institutionStammdatenList = new ArrayList<>();
		institutionStammdatenList.add(kita_1);
		institutionStammdatenList.add(kita_2);

		Testfall02_FeutzYvonne testfall = new Testfall02_FeutzYvonne(gesuchsperiode1718, institutionStammdatenList);

		testfall.createFall();
		testfall.createGesuch(LocalDate.of(2016, 7, 1));
		gesuch = testfall.fillInGesuch();
		TestDataUtil.calculateFinanzDaten(gesuch);
		gesuch.setGesuchsperiode(gesuchsperiode1718);
		gesuch.getDossier().setGemeinde(paris);

		// Der Einfachheit halber loeschen wir das zweite Kind
		final KindContainer kind = gesuch.getKindContainers().stream().findFirst().get();
		gesuch.setKindContainers(new HashSet<>());
		gesuch.getKindContainers().add(kind);
		Assert.assertEquals("Vor dem Test ist nur noch 1 Kind vorhanden", 1, gesuch.getKindContainers().size());
		// Und alle Betreuungen des ersten Kindes
		gesuch.getKindContainers().stream().iterator().next().setBetreuungen(new HashSet<>());
		Assert.assertTrue("Vor dem Test sollen keine Betreuungen vorhanden sein", gesuch.extractAllBetreuungen().isEmpty());
	}



	@Test
	public void normalFallUeberschneidungMitAnspruch() {
		// Betreuung 1: 40%, Anspruch 60% -> BG 40%, Restanspruch 20%
		addBetreuung(
			40,
			gesuchsperiode1718.getGueltigkeit().getGueltigAb(),
			gesuchsperiode1718.getGueltigkeit().getGueltigBis(),
			kita_1);
		// Betreuung 2: 30%, Restanspruch 20% -> BG 20%
		addBetreuung(
			30,
			gesuchsperiode1718.getGueltigkeit().getGueltigAb(),
			gesuchsperiode1718.getGueltigkeit().getGueltigBis(),
			kita_2
		);

		evaluator.evaluate(gesuch, getParameterToUse(), kitaxUebergangsloesungParameter, Constants.DEFAULT_LOCALE);
		assertResults(kita_1, 40, 60, 40, -1, false);
		assertResults(kita_2, 30, 20, 20, 20, true);

		// Die erste Kita verfuegen. Damit wird der Restangspruch fuer die zweite anders berechnet
		betreuungVerfuegen(kita_1);

		// Die Resultate muessen aber immer noch gleich sein
		evaluator.evaluate(gesuch, getParameterToUse(), kitaxUebergangsloesungParameter, Constants.DEFAULT_LOCALE);
		assertResults(kita_1, 40, 60, 40, -1, false);
		assertResults(kita_2, 30, 20, 20, 20, true);

		// Die zweite ebenfalls verfuegen
		betreuungVerfuegen(kita_2);
		gesuch.setStatus(AntragStatus.VERFUEGT);

		// Die Resultate muessen aber immer noch gleich sein
		evaluator.evaluate(gesuch, getParameterToUse(), kitaxUebergangsloesungParameter, Constants.DEFAULT_LOCALE);
		assertResults(kita_1, 40, 60, 40, -1, false);
		assertResults(kita_2, 30, 20, 20, 20, true);
	}

	@Test
	public void febrRechnerNichtUeberschneidend() {
		// Betreuung 1: 40%, Anspruch 60% -> BG 40%, beendet per Ende Oktober
		addBetreuung(
			40,
			gesuchsperiode1718.getGueltigkeit().getGueltigAb(),
			LocalDate.of(2017, Month.OCTOBER, 31),
			kita_1);
		// Betreuung 2: 30%, Anspruch 60% -> BG 30%, startet per Anfang November
		addBetreuung(
			30,
			LocalDate.of(2017, Month.NOVEMBER, 1),
			gesuchsperiode1718.getGueltigkeit().getGueltigBis(),
			kita_2
		);

		evaluator.evaluate(gesuch, getParameterToUse(), kitaxUebergangsloesungParameter, Constants.DEFAULT_LOCALE);

		assertResults(kita_1, gesuchsperiode1718.getGueltigkeit().getGueltigAb(),
			40, 60, 40, -1, false);
		assertResults(kita_2, gesuchsperiode1718.getGueltigkeit().getGueltigAb(),
			0, 0, 0, 20, false);
		assertResults(kita_1, LocalDate.of(2017, Month.NOVEMBER, 1),
			0, 0, 0, -1, false);
		assertResults(kita_2, LocalDate.of(2017, Month.NOVEMBER, 1),
			30, 60, 30, 60, false);

		// Die erste Kita verfuegen. Damit wird der Restangspruch fuer die zweite anders berechnet
		betreuungVerfuegen(kita_1);

		// Die Resultate muessen aber immer noch gleich sein
		evaluator.evaluate(gesuch, getParameterToUse(), kitaxUebergangsloesungParameter, Constants.DEFAULT_LOCALE);

		assertResults(kita_1, gesuchsperiode1718.getGueltigkeit().getGueltigAb(),
			40, 60, 40, -1, false);
		assertResults(kita_2, gesuchsperiode1718.getGueltigkeit().getGueltigAb(),
			0, 0, 0, 20, false);
		assertResults(kita_1, LocalDate.of(2017, Month.NOVEMBER, 1),
			0, 0, 0, -1, false);
		assertResults(kita_2, LocalDate.of(2017, Month.NOVEMBER, 1),
			30, 60, 30, -1, false);

		// Die zweite ebenfalls verfuegen
		betreuungVerfuegen(kita_2);
		gesuch.setStatus(AntragStatus.VERFUEGT);

		// Die Resultate muessen aber immer noch gleich sein
		evaluator.evaluate(gesuch, getParameterToUse(), kitaxUebergangsloesungParameter, Constants.DEFAULT_LOCALE);

		assertResults(kita_1, gesuchsperiode1718.getGueltigkeit().getGueltigAb(),
			40, 60, 40, -1, false);
		assertResults(kita_2, gesuchsperiode1718.getGueltigkeit().getGueltigAb(),
			0, 0, 0, 20, false);
		assertResults(kita_1, LocalDate.of(2017, Month.NOVEMBER, 1),
			0, 0, 0, -1, false);
		assertResults(kita_2, LocalDate.of(2017, Month.NOVEMBER, 1),
			30, 60, 30, -1, false);
	}

	private void addBetreuung(int betreuungspensum, @Nonnull LocalDate von, @Nonnull LocalDate bis, @Nonnull InstitutionStammdaten kita) {
		Betreuung betreuung = TestDataUtil.createDefaultBetreuung(betreuungspensum, von, bis);
		betreuung.setInstitutionStammdaten(kita);
		gesuch.getKindContainers().iterator().next().getBetreuungen().add(betreuung);
		betreuung.setKind(gesuch.getKindContainers().stream().findFirst().get());
		betreuung.initVorgaengerVerfuegungen(null, null);
	}

	@Nonnull
	private BGRechnerParameterDTO getParameterToUse() {
		final BGRechnerParameterDTO parameter = AbstractBGRechnerTest.getParameter();
		if (KitaxUtil.isGemeindeWithKitaxUebergangsloesung(gesuch.extractGemeinde())) {
			parameter.getGemeindeParameter().setGemeindeZusaetzlicherGutscheinEnabled(true);
			parameter.getGemeindeParameter().setGemeindeZusaetzlicherGutscheinBisUndMitSchulstufeKita(EinschulungTyp.KINDERGARTEN1);
			parameter.getGemeindeParameter().setGemeindeZusaetzlicherGutscheinBetragKita(MathUtil.DEFAULT.from(11));
		}
		return parameter;
	}

	private void assertResults(
		@Nonnull InstitutionStammdaten kita,
		int expectedBetreuungspensum,
		int expectedAnspruchspensum,
		int expectedBgPEnsum,
		int expectedRestFromPreviousBetreuung,
		boolean msgRestanspruchExpected
	) {
		// Egal welches Datum, wir gehen davon aus, dass alle Zeitabschnitte gleich sind
		this.assertResults(kita, gesuchsperiode1718.getGueltigkeit().getGueltigAb(),
			expectedBetreuungspensum, expectedAnspruchspensum, expectedBgPEnsum, expectedRestFromPreviousBetreuung, msgRestanspruchExpected);
	}

	private void assertResults(
		@Nonnull InstitutionStammdaten kita,
		@Nonnull LocalDate stichtag,
		int expectedBetreuungspensum,
		int expectedAnspruchspensum,
		int expectedBgPEnsum,
		int expectedRestFromPreviousBetreuung,
		boolean msgRestanspruchExpected
	) {
		for (KindContainer kindContainer : gesuch.getKindContainers()) {
			for (Betreuung betreuung : kindContainer.getBetreuungen()) {
				Verfuegung verfuegung = betreuung.getVerfuegungPreview();
				Assert.assertNotNull(verfuegung);
				if (betreuung.getInstitutionStammdaten().equals(kita)) {
					Assert.assertEquals(12, verfuegung.getZeitabschnitte().size());
					for (VerfuegungZeitabschnitt verfuegungZeitabschnitt : verfuegung.getZeitabschnitte()) {
						if (verfuegungZeitabschnitt.getGueltigkeit().contains(stichtag)) {
							assertZeitabschnitt(verfuegungZeitabschnitt, expectedBetreuungspensum, expectedAnspruchspensum, expectedBgPEnsum, expectedRestFromPreviousBetreuung);
							Assert.assertEquals(msgRestanspruchExpected, verfuegungZeitabschnitt.getBemerkungenList().containsMsgKey(MsgKey.RESTANSPRUCH_MSG));
						}
					}
				}
			}
		}
	}

	private void betreuungVerfuegen(@Nonnull InstitutionStammdaten kita) {
		gesuch.extractAllBetreuungen().forEach(betreuung -> {
			if (betreuung.getInstitutionStammdaten().equals(kita)) {
				betreuung.setBetreuungsstatus(Betreuungsstatus.VERFUEGT);
				final Verfuegung verfuegungPreview = betreuung.getVerfuegungPreview();
				Assert.assertNotNull(verfuegungPreview);
				betreuung.setVerfuegung(verfuegungPreview);
				verfuegungPreview.setBetreuung(betreuung);
			}
		});
	}
}
