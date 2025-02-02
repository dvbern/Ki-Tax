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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Month;

import ch.dvbern.ebegu.entities.Betreuung;
import ch.dvbern.ebegu.entities.BetreuungspensumContainer;
import ch.dvbern.ebegu.entities.ErwerbspensumContainer;
import ch.dvbern.ebegu.entities.FinanzielleSituationContainer;
import ch.dvbern.ebegu.entities.Gemeinde;
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.entities.Gesuchsperiode;
import ch.dvbern.ebegu.entities.GesuchstellerContainer;
import ch.dvbern.ebegu.entities.KindContainer;
import ch.dvbern.ebegu.enums.Geschlecht;
import ch.dvbern.ebegu.enums.Kinderabzug;
import ch.dvbern.ebegu.testfaelle.institutionStammdatenBuilder.InstitutionStammdatenBuilder;
import ch.dvbern.ebegu.util.MathUtil;

/**
 * http://localhost:8080/ebegu/api/v1/testfaelle/testfall/5
 * https://ebegu.dvbern.ch/ebegu/api/v1/testfaelle/testfall/5
 */
@SuppressWarnings("PMD.ClassNamingConventions")
public class Testfall07_MeierMeret extends AbstractTestfall {

	private static final String FAMILIENNAME = "Meier";

	private static final BigDecimal VERMOEGEN_GS1 = MathUtil.DEFAULT.from(15321.00);
	private static final BigDecimal VERMOEGEN_GS2 = MathUtil.DEFAULT.from(25496);
	private static final BigDecimal EINKOMMEN_GS1 = MathUtil.DEFAULT.from(5691.00);
	private static final BigDecimal EINKOMMEN_GS2 = MathUtil.DEFAULT.from(102498.00);

	public Testfall07_MeierMeret(
			Gesuchsperiode gesuchsperiode,
			boolean betreuungenBestaetigt,
			Gemeinde gemeinde, InstitutionStammdatenBuilder institutionStammdatenBuilder) {
		super(gesuchsperiode, betreuungenBestaetigt, gemeinde, institutionStammdatenBuilder);
	}

	@Override
	public Gesuch fillInGesuch() {
		// Gesuch, Gesuchsteller
		gesuch = createVerheiratet();
		GesuchstellerContainer gesuchsteller1 = createGesuchstellerContainer(FAMILIENNAME, "Meret", 1);
		gesuch.setGesuchsteller1(gesuchsteller1);
		GesuchstellerContainer gesuchsteller2 = createGesuchstellerContainer(FAMILIENNAME, "Jan", 2);
		gesuch.setGesuchsteller2(gesuchsteller2);
		// Erwerbspensum
		ErwerbspensumContainer erwerbspensumGS1 = createErwerbspensum(70);
		gesuchsteller1.addErwerbspensumContainer(erwerbspensumGS1);
		ErwerbspensumContainer erwerbspensumGS2 = createErwerbspensum(60);
		gesuchsteller2.addErwerbspensumContainer(erwerbspensumGS2);
		// Kinder
		KindContainer kind1 = createKind(Geschlecht.WEIBLICH, FAMILIENNAME, "Tanja", LocalDate.of(2013, Month.JANUARY, 29), Kinderabzug.GANZER_ABZUG, true);
		kind1.setGesuch(gesuch);
		gesuch.getKindContainers().add(kind1);

		// Betreuungen
		// Kind 1: Kita Weissenstein
		Betreuung betreuungTagiAaregg = createBetreuung(institutionStammdatenBuilder.getIdInstitutionStammdatenWeissenstein(), betreuungenBestaetigt);
		betreuungTagiAaregg.setKind(kind1);
		kind1.getBetreuungen().add(betreuungTagiAaregg);
		// 50%
		BetreuungspensumContainer betreuungspensumTagiAaregg1 = createBetreuungspensum(50, LocalDate.of(gesuchsperiode.getBasisJahrPlus1(), Month.AUGUST, 1), LocalDate.of(gesuchsperiode.getBasisJahrPlus1(), Month.DECEMBER, 31));
		betreuungspensumTagiAaregg1.setBetreuung(betreuungTagiAaregg);
		betreuungTagiAaregg.getBetreuungspensumContainers().add(betreuungspensumTagiAaregg1);
		// 60%
		BetreuungspensumContainer betreuungspensumTagiAaregg2 = createBetreuungspensum(60, LocalDate.of(gesuchsperiode.getBasisJahrPlus2(), Month.JANUARY, 1), LocalDate.of(gesuchsperiode.getBasisJahrPlus2(), Month.JULY, 31));
		betreuungspensumTagiAaregg2.setBetreuung(betreuungTagiAaregg);
		betreuungTagiAaregg.getBetreuungspensumContainers().add(betreuungspensumTagiAaregg2);

		// Finanzielle Situation
		FinanzielleSituationContainer finanzielleSituationGS1 = createFinanzielleSituationContainer(VERMOEGEN_GS1, EINKOMMEN_GS1);
		finanzielleSituationGS1.setGesuchsteller(gesuchsteller1);
		gesuchsteller1.setFinanzielleSituationContainer(finanzielleSituationGS1);

		FinanzielleSituationContainer finanzielleSituationGS2 = createFinanzielleSituationContainer(VERMOEGEN_GS2, EINKOMMEN_GS2);
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
		return "Meret";
	}
}
