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

package ch.dvbern.ebegu.testfaelle;

import java.time.LocalDate;
import java.time.Month;
import java.util.Collection;

import ch.dvbern.ebegu.entities.Betreuung;
import ch.dvbern.ebegu.entities.BetreuungspensumContainer;
import ch.dvbern.ebegu.entities.ErwerbspensumContainer;
import ch.dvbern.ebegu.entities.FinanzielleSituationContainer;
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.entities.Gesuchsperiode;
import ch.dvbern.ebegu.entities.GesuchstellerContainer;
import ch.dvbern.ebegu.entities.InstitutionStammdaten;
import ch.dvbern.ebegu.entities.KindContainer;
import ch.dvbern.ebegu.enums.Geschlecht;
import ch.dvbern.ebegu.enums.Kinderabzug;
import ch.dvbern.ebegu.util.MathUtil;

/**
 * Kind mit 2 Kitas. Anspruch 60%
 * Kita A: ab 01.08.2016 - 31.12.2016 40%, ab 01.01.2017 50%
 * Kita B: ab 01.08.2016 - 31.12.2016 50%, ab 01.01.2017 40%
 *
 * -> Regel 1
 * -> Kita B wird zuerst bedient, weil sie bei gleichem Beginn das höhere Pensum hat, auch ab 1.1., wo Kita A höher wäre!
 */
public class Testfall_DoppelKita_Regel1 extends AbstractTestfall {

	public Testfall_DoppelKita_Regel1(Gesuchsperiode gesuchsperiode, Collection<InstitutionStammdaten> institutionStammdatenList,
		boolean betreuungenBestaetigt) {
		super(gesuchsperiode, institutionStammdatenList, betreuungenBestaetigt);
	}

	public Testfall_DoppelKita_Regel1(Gesuchsperiode gesuchsperiode, Collection<InstitutionStammdaten> institutionStammdatenList) {
		super(gesuchsperiode, institutionStammdatenList, false);
	}

	@Override
	public Gesuch fillInGesuch() {
		// Gesuch, Gesuchsteller
		Gesuch gesuch = createAlleinerziehend();
		GesuchstellerContainer gesuchsteller1 = createGesuchstellerContainer();
		gesuch.setGesuchsteller1(gesuchsteller1);
		// Erwerbspensum
		ErwerbspensumContainer erwerbspensum = createErwerbspensum(60, 0);
		gesuchsteller1.addErwerbspensumContainer(erwerbspensum);
		// Kinder
		KindContainer kind = createKind(Geschlecht.MAENNLICH, "Doppelkita", "Dodi", LocalDate.of(2014, Month.APRIL, 13), Kinderabzug.GANZER_ABZUG, true);
		kind.setGesuch(gesuch);
		gesuch.getKindContainers().add(kind);
		// Betreuungen
		// Kita Weissenstein
		Betreuung betreuungKitaAaregg = createBetreuung(ID_INSTITUTION_STAMMDATEN_WEISSENSTEIN_KITA, betreuungenBestaetigt);
		betreuungKitaAaregg.setBetreuungNummer(1);
		betreuungKitaAaregg.setKind(kind);
		kind.getBetreuungen().add(betreuungKitaAaregg);

		BetreuungspensumContainer betreuungspensumKitaAaregg = createBetreuungspensum(40, LocalDate.of(gesuchsperiode.getBasisJahrPlus1(), Month.AUGUST, 1), LocalDate.of(gesuchsperiode.getBasisJahrPlus1(), Month.DECEMBER, 31));
		betreuungspensumKitaAaregg.setBetreuung(betreuungKitaAaregg);
		betreuungKitaAaregg.getBetreuungspensumContainers().add(betreuungspensumKitaAaregg);

		BetreuungspensumContainer betreuungspensumKitaAaregg2 = createBetreuungspensum(50, LocalDate.of(gesuchsperiode.getBasisJahrPlus2(), Month.JANUARY, 1), LocalDate.of(gesuchsperiode.getBasisJahrPlus2(), Month.JULY, 31));
		betreuungspensumKitaAaregg2.setBetreuung(betreuungKitaAaregg);
		betreuungKitaAaregg.getBetreuungspensumContainers().add(betreuungspensumKitaAaregg2);

		// Kita Brünnen
		Betreuung betreuungKitaBruennen = createBetreuung(ID_INSTITUTION_STAMMDATEN_BRUENNEN_KITA, betreuungenBestaetigt);
		betreuungKitaBruennen.setBetreuungNummer(2);
		betreuungKitaBruennen.setKind(kind);
		kind.getBetreuungen().add(betreuungKitaBruennen);

		BetreuungspensumContainer betreuungspensumKitaBruennen = createBetreuungspensum(50, LocalDate.of(gesuchsperiode.getBasisJahrPlus1(), Month.AUGUST, 1), LocalDate.of(gesuchsperiode.getBasisJahrPlus1(), Month.DECEMBER, 31));
		betreuungspensumKitaBruennen.setBetreuung(betreuungKitaBruennen);
		betreuungKitaBruennen.getBetreuungspensumContainers().add(betreuungspensumKitaBruennen);

		BetreuungspensumContainer betreuungspensumKitaBruennen2 = createBetreuungspensum(40, LocalDate.of(gesuchsperiode.getBasisJahrPlus2(), Month.JANUARY, 1), LocalDate.of(gesuchsperiode.getBasisJahrPlus2(), Month.JULY, 31));
		betreuungspensumKitaBruennen2.setBetreuung(betreuungKitaBruennen);
		betreuungKitaBruennen.getBetreuungspensumContainers().add(betreuungspensumKitaBruennen2);
		// Finanzielle Situation
		FinanzielleSituationContainer finanzielleSituationContainer = createFinanzielleSituationContainer();
		finanzielleSituationContainer.getFinanzielleSituationJA().setNettolohn(MathUtil.DEFAULT.from(53265));
		finanzielleSituationContainer.getFinanzielleSituationJA().setBruttovermoegen(MathUtil.DEFAULT.from(12147));
		finanzielleSituationContainer.setGesuchsteller(gesuchsteller1);
		gesuchsteller1.setFinanzielleSituationContainer(finanzielleSituationContainer);

		createEmptyEKVInfoContainer(gesuch);

		return gesuch;
	}

	@Override
	public String getNachname() {
		return "Doppelkita";
	}

	@Override
	public String getVorname() {
		return "Doris";
	}
}
