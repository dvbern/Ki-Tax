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
import java.util.Objects;

import ch.dvbern.ebegu.entities.Betreuung;
import ch.dvbern.ebegu.entities.BetreuungspensumContainer;
import ch.dvbern.ebegu.entities.ErwerbspensumContainer;
import ch.dvbern.ebegu.entities.FinanzielleSituationContainer;
import ch.dvbern.ebegu.entities.Gemeinde;
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.entities.Gesuchsperiode;
import ch.dvbern.ebegu.entities.GesuchstellerContainer;
import ch.dvbern.ebegu.entities.InstitutionStammdaten;
import ch.dvbern.ebegu.entities.KindContainer;
import ch.dvbern.ebegu.enums.BetreuungsangebotTyp;
import ch.dvbern.ebegu.enums.Geschlecht;
import ch.dvbern.ebegu.enums.Kinderabzug;
import ch.dvbern.ebegu.util.MathUtil;

/**
 * http://localhost:8080/ebegu/api/v1/testfaelle/testfall/2
 * https://ebegu.dvbern.ch/ebegu/api/v1/testfaelle/testfall/2
 */
public class Testfall02_FeutzYvonne extends AbstractTestfall {

	private static final String FAMILIENNAME = "Feutz";

	public Testfall02_FeutzYvonne(Gesuchsperiode gesuchsperiode, Collection<InstitutionStammdaten> institutionStammdatenList) {
		super(gesuchsperiode, institutionStammdatenList, false);
	}

	public Testfall02_FeutzYvonne(Gesuchsperiode gesuchsperiode, Collection<InstitutionStammdaten> institutionStammdatenList, boolean betreuungenBestaetigt, Gemeinde gemeinde) {
		super(gesuchsperiode, institutionStammdatenList, betreuungenBestaetigt, gemeinde);
	}

	@Override
	public Gesuch fillInGesuch() {
		// Gesuch, Gesuchsteller
		gesuch = createVerheiratet();
		GesuchstellerContainer gesuchsteller1 = createGesuchstellerContainer();
		gesuch.setGesuchsteller1(gesuchsteller1);
		GesuchstellerContainer gesuchsteller2 = createGesuchstellerContainer(FAMILIENNAME, "Tizian");
		gesuch.setGesuchsteller2(gesuchsteller2);
		// Erwerbspensum
		ErwerbspensumContainer erwerbspensumGS1 = createErwerbspensum(40, 0);
		gesuchsteller1.addErwerbspensumContainer(erwerbspensumGS1);
		ErwerbspensumContainer erwerbspensumGS2 = createErwerbspensum(100, 0);
		gesuchsteller2.addErwerbspensumContainer(erwerbspensumGS2);
		// Kinder
		KindContainer kind1 = createKind(Geschlecht.WEIBLICH, FAMILIENNAME, "Tamara", LocalDate.of(2009, Month.JULY, 11), Kinderabzug.GANZER_ABZUG, true);
		kind1.setGesuch(gesuch);
		kind1.setKindNummer(1);
		gesuch.getKindContainers().add(kind1);
		KindContainer kind2 = createKind(Geschlecht.MAENNLICH, FAMILIENNAME, "Leonard", LocalDate.of(2012, Month.NOVEMBER, 19), Kinderabzug.GANZER_ABZUG, true);
		kind2.setGesuch(gesuch);
		kind2.setKindNummer(2);
		gesuch.getKindContainers().add(kind2);

		// Betreuungen
		// Kind 1: Tagi Weissenstein
		Betreuung betreuungTagiAaregg = createBetreuung(BetreuungsangebotTyp.TAGI, ID_INSTITUTION_WEISSENSTEIN, betreuungenBestaetigt);
		betreuungTagiAaregg.setKind(kind1);
		kind1.getBetreuungen().add(betreuungTagiAaregg);
		BetreuungspensumContainer betreuungspensumTagiAaregg = createBetreuungspensum(60, LocalDate.of(gesuchsperiode.getBasisJahrPlus1(), Month.AUGUST, 1), LocalDate.of(gesuchsperiode.getBasisJahrPlus2(), Month.JULY, 31));
		betreuungspensumTagiAaregg.setBetreuung(betreuungTagiAaregg);
		betreuungTagiAaregg.getBetreuungspensumContainers().add(betreuungspensumTagiAaregg);
		// Kind 2: Kita Weissenstein
		Betreuung betreuungKitaAaregg = createBetreuung(BetreuungsangebotTyp.KITA, ID_INSTITUTION_WEISSENSTEIN, betreuungenBestaetigt);
		betreuungKitaAaregg.setKind(kind2);
		kind2.getBetreuungen().add(betreuungKitaAaregg);
		BetreuungspensumContainer betreuungspensumKitaAaregg = createBetreuungspensum(40, LocalDate.of(gesuchsperiode.getBasisJahrPlus1(), Month.AUGUST, 1), LocalDate.of(gesuchsperiode.getBasisJahrPlus2(), Month.JULY, 31));
		betreuungspensumKitaAaregg.setBetreuung(betreuungKitaAaregg);
		betreuungKitaAaregg.getBetreuungspensumContainers().add(betreuungspensumKitaAaregg);

		// Finanzielle Situation
		FinanzielleSituationContainer finanzielleSituationGS1 = createFinanzielleSituationContainer();
		finanzielleSituationGS1.getFinanzielleSituationJA().setNettolohn(MathUtil.DEFAULT.from(34022));
		finanzielleSituationGS1.getFinanzielleSituationJA().setBruttovermoegen(Objects.requireNonNull(MathUtil.DEFAULT.from(96054)));
		finanzielleSituationGS1.setGesuchsteller(gesuchsteller1);
		gesuchsteller1.setFinanzielleSituationContainer(finanzielleSituationGS1);

		FinanzielleSituationContainer finanzielleSituationGS2 = createFinanzielleSituationContainer();
		finanzielleSituationGS2.getFinanzielleSituationJA().setNettolohn(MathUtil.DEFAULT.from(98521));
		finanzielleSituationGS2.setGesuchsteller(gesuchsteller2);
		gesuchsteller2.setFinanzielleSituationContainer(finanzielleSituationGS2);

		createEmptyEKVInfoContainer(gesuch);

		return gesuch;
	}

	@Override
	public String getNachname() {
		return FAMILIENNAME;
	}

	@Override
	public String getVorname() {
		return "Yvonne";
	}
}
