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
import java.util.Objects;

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
 * http://localhost:8080/ebegu/api/v1/testfaelle/testfall/2
 * https://ebegu.dvbern.ch/ebegu/api/v1/testfaelle/testfall/2
 */
@SuppressWarnings("PMD.ClassNamingConventions")
public class Testfall02_FeutzYvonne extends AbstractTestfall {

	private static final String FAMILIENNAME = "Feutz";
	private static final BigDecimal VERMOEGEN_GS1 = MathUtil.DEFAULT.from(96054);
	private static final BigDecimal EINKOMMEN_GS1 = MathUtil.DEFAULT.from(34022);
	private static final BigDecimal EINKOMMEN_GS2 = MathUtil.DEFAULT.from(98521);

	public Testfall02_FeutzYvonne(
			Gesuchsperiode gesuchsperiode,
			InstitutionStammdatenBuilder institutionStammdatenBuilder) {
		super(gesuchsperiode, false, institutionStammdatenBuilder);
	}

	public Testfall02_FeutzYvonne(
			Gesuchsperiode gesuchsperiode,
			boolean betreuungenBestaetigt,
			Gemeinde gemeinde,
			InstitutionStammdatenBuilder institutionStammdatenBuilder) {
		super(gesuchsperiode, betreuungenBestaetigt, gemeinde, institutionStammdatenBuilder);
	}

	@Override
	public Gesuch fillInGesuch() {
		// Gesuch, Gesuchsteller
		gesuch = createVerheiratet();
		GesuchstellerContainer gesuchsteller1 = createGesuchstellerContainer();
		gesuchsteller1.getGesuchstellerJA().setGeschlecht(Geschlecht.WEIBLICH);
		gesuch.setGesuchsteller1(gesuchsteller1);
		GesuchstellerContainer gesuchsteller2 = createGesuchstellerContainer(FAMILIENNAME, "Tizian");
		gesuchsteller2.getGesuchstellerJA().setGeschlecht(Geschlecht.MAENNLICH);
		gesuch.setGesuchsteller2(gesuchsteller2);
		// Erwerbspensum
		ErwerbspensumContainer erwerbspensumGS1 = createErwerbspensum(40);
		gesuchsteller1.addErwerbspensumContainer(erwerbspensumGS1);
		ErwerbspensumContainer erwerbspensumGS2 = createErwerbspensum(100);
		gesuchsteller2.addErwerbspensumContainer(erwerbspensumGS2);
		// Kinder
		KindContainer kind1 = createKind(Geschlecht.WEIBLICH, FAMILIENNAME, "Tamara", LocalDate.of(2009, Month.JULY, 11), Kinderabzug.GANZER_ABZUG, true);
		kind1.setGesuch(gesuch);
		gesuch.getKindContainers().add(kind1);
		KindContainer kind2 = createKind(Geschlecht.MAENNLICH, FAMILIENNAME, "Leonard", LocalDate.of(2012, Month.NOVEMBER, 19), Kinderabzug.GANZER_ABZUG, true);
		kind2.setGesuch(gesuch);
		gesuch.getKindContainers().add(kind2);

		// Betreuungen
		// Kind 1: Kita Weissenstein
		Betreuung betreuungKitaWeissenstein = createBetreuung(institutionStammdatenBuilder.getIdInstitutionStammdatenWeissenstein(), betreuungenBestaetigt);
		betreuungKitaWeissenstein.setKind(kind1);
		kind1.getBetreuungen().add(betreuungKitaWeissenstein);
		BetreuungspensumContainer betreuungspensumTagiAaregg = createBetreuungspensum(60, LocalDate.of(gesuchsperiode.getBasisJahrPlus1(), Month.AUGUST, 1), LocalDate.of(gesuchsperiode.getBasisJahrPlus2(), Month.JULY, 31));
		betreuungspensumTagiAaregg.setBetreuung(betreuungKitaWeissenstein);
		betreuungKitaWeissenstein.getBetreuungspensumContainers().add(betreuungspensumTagiAaregg);
		// Kind 2: Kita Weissenstein
		Betreuung betreuungKitaAaregg = createBetreuung(institutionStammdatenBuilder.getIdInstitutionStammdatenWeissenstein(), betreuungenBestaetigt);
		betreuungKitaAaregg.setKind(kind2);
		kind2.getBetreuungen().add(betreuungKitaAaregg);
		BetreuungspensumContainer betreuungspensumKitaAaregg = createBetreuungspensum(40, LocalDate.of(gesuchsperiode.getBasisJahrPlus1(), Month.AUGUST, 1), LocalDate.of(gesuchsperiode.getBasisJahrPlus2(), Month.JULY, 31));
		betreuungspensumKitaAaregg.setBetreuung(betreuungKitaAaregg);
		betreuungKitaAaregg.getBetreuungspensumContainers().add(betreuungspensumKitaAaregg);

		// Finanzielle Situation
		FinanzielleSituationContainer finanzielleSituationGS1 = createFinanzielleSituationContainer();
		finanzielleSituationGS1.getFinanzielleSituationJA().setNettolohn(MathUtil.DEFAULT.from(EINKOMMEN_GS1));
		finanzielleSituationGS1.getFinanzielleSituationJA().setBruttovermoegen(Objects.requireNonNull(MathUtil.DEFAULT.from(VERMOEGEN_GS1)));
		finanzielleSituationGS1.setGesuchsteller(gesuchsteller1);
		gesuchsteller1.setFinanzielleSituationContainer(finanzielleSituationGS1);

		FinanzielleSituationContainer finanzielleSituationGS2 = createFinanzielleSituationContainer();
		finanzielleSituationGS2.getFinanzielleSituationJA().setNettolohn(MathUtil.DEFAULT.from(EINKOMMEN_GS2));
		finanzielleSituationGS2.setGesuchsteller(gesuchsteller2);
		gesuchsteller2.setFinanzielleSituationContainer(finanzielleSituationGS2);

		// LU
		TestFaelleUtil.fillInFinSitLuZero(finanzielleSituationGS1);
		assert finanzielleSituationGS1.getFinanzielleSituationJA().getSelbstdeklaration() != null;
		finanzielleSituationGS1.getFinanzielleSituationJA().getSelbstdeklaration().setVermoegen(MathUtil.DEFAULT.from(VERMOEGEN_GS1));
		finanzielleSituationGS1.getFinanzielleSituationJA().getSelbstdeklaration().setEinkunftErwerb(MathUtil.DEFAULT.from(EINKOMMEN_GS1));

		// SO
		TestFaelleUtil.fillInFinSitSoZero(finanzielleSituationGS1);
		finanzielleSituationGS1.getFinanzielleSituationJA().setSteuerbaresVermoegen(MathUtil.DEFAULT.from(VERMOEGEN_GS1));

		TestFaelleUtil.fillInFinSitSoZero(finanzielleSituationGS2);
		finanzielleSituationGS2.getFinanzielleSituationJA().setSteuerbaresVermoegen(MathUtil.DEFAULT.from(VERMOEGEN_GS1));

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
