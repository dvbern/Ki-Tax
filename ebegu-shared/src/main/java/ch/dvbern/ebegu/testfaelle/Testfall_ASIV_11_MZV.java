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
import ch.dvbern.ebegu.util.MathUtil;

/**
 * Wechsel von 2 auf 1. Mit nachheriger EKV, nach der Trennung (GS2 nicht mehr relevant)
 */
@SuppressWarnings("PMD.ClassNamingConventions")
public class Testfall_ASIV_11_MZV extends AbstractASIVTestfall {

	private static final BigDecimal EINKOMMEN_GS1 = MathUtil.DEFAULT.from(20000);
	private static final BigDecimal EINKOMMEN_GS2 = MathUtil.DEFAULT.from(30000);

	private boolean initWithMZV;

	public Testfall_ASIV_11_MZV(
			Gesuchsperiode gesuchsperiode,
			boolean betreuungenBestaetigt,
			Gemeinde gemeinde,
			boolean initWithMZV, InstitutionStammdatenBuilder institutionStammdatenBuilder) {
		super(gesuchsperiode, betreuungenBestaetigt, gemeinde, institutionStammdatenBuilder);
		this.initWithMZV = initWithMZV;
	}

	@Override
	public Gesuch fillInGesuch() {
		return createErstgesuch();
	}

	public Gesuch createErstgesuch() {
		// Gesuch, Gesuchsteller
		Gesuch erstgesuch = createVerheiratetMitMVZ();
		GesuchstellerContainer gesuchsteller1 = createGesuchstellerContainer();
		erstgesuch.setGesuchsteller1(gesuchsteller1);
		GesuchstellerContainer gesuchsteller2 = createGesuchstellerContainer();
		erstgesuch.setGesuchsteller2(gesuchsteller2);
		// Erwerbspensum
		ErwerbspensumContainer erwerbspensum = createErwerbspensum(100);
		gesuchsteller1.addErwerbspensumContainer(erwerbspensum);
		ErwerbspensumContainer erwerbspensumGS2 = createErwerbspensum(100);
		gesuchsteller2.addErwerbspensumContainer(erwerbspensumGS2);
		// Kinder
		KindContainer kind = createKind(Geschlecht.MAENNLICH,
			"ASIV",
			"Kind",
			LocalDate.of(2014, Month.APRIL, 13),
			Kinderabzug.GANZER_ABZUG,
			true);
		kind.setGesuch(erstgesuch);
		erstgesuch.getKindContainers().add(kind);
		// Kita Brünnen
		Betreuung betreuungKitaBruennen =
			createBetreuung(institutionStammdatenBuilder.getIdInstitutionStammdatenBruennen(), betreuungenBestaetigt);
		betreuungKitaBruennen.setKind(kind);
		kind.getBetreuungen().add(betreuungKitaBruennen);
		BetreuungspensumContainer betreuungspensumKitaBruennen = createBetreuungspensum(
			BigDecimal.valueOf(100),
			LocalDate.of(gesuchsperiode.getBasisJahrPlus1(), Month.AUGUST, 1),
			LocalDate.of(gesuchsperiode.getBasisJahrPlus2(), Month.JULY, 31),
			initWithMZV ? BigDecimal.TEN : BigDecimal.ZERO,
			initWithMZV ? BigDecimal.TEN : BigDecimal.ZERO,
			initWithMZV ? BigDecimal.TEN : BigDecimal.ZERO,
			initWithMZV ? BigDecimal.ONE : BigDecimal.ZERO
		);
		betreuungspensumKitaBruennen.setBetreuung(betreuungKitaBruennen);
		betreuungKitaBruennen.getBetreuungspensumContainers().add(betreuungspensumKitaBruennen);
		// Finanzielle Situation
		FinanzielleSituationContainer finanzielleSituationContainer = createFinanzielleSituationContainer();
		finanzielleSituationContainer.getFinanzielleSituationJA().setNettolohn(MathUtil.DEFAULT.from(EINKOMMEN_GS1));
		finanzielleSituationContainer.setGesuchsteller(gesuchsteller1);
		gesuchsteller1.setFinanzielleSituationContainer(finanzielleSituationContainer);

		FinanzielleSituationContainer finanzielleSituationGS2 = createFinanzielleSituationContainer();
		finanzielleSituationGS2.getFinanzielleSituationJA().setNettolohn(MathUtil.DEFAULT.from(EINKOMMEN_GS2));
		finanzielleSituationGS2.setGesuchsteller(gesuchsteller2);
		gesuchsteller2.setFinanzielleSituationContainer(finanzielleSituationGS2);

		// LU
		TestFaelleUtil.fillInFinSitLuZero(finanzielleSituationContainer);
		assert finanzielleSituationContainer.getFinanzielleSituationJA().getSelbstdeklaration() != null;
		finanzielleSituationContainer.getFinanzielleSituationJA().getSelbstdeklaration().setEinkunftErwerb(MathUtil.DEFAULT.from(EINKOMMEN_GS1));

		// SO
		TestFaelleUtil.fillInFinSitSoZero(finanzielleSituationContainer);
		TestFaelleUtil.fillInFinSitSoZero(finanzielleSituationGS2);

		createEmptyEKVInfoContainer(gesuch);

		return erstgesuch;
	}

	@Override
	public Gesuch createMutation(Gesuch erstgesuch) {
		// add MZV wenn noetig
		if (!initWithMZV) {
			KindContainer kind = erstgesuch.getKindContainers().iterator().next();
			Betreuung betreuungKitaBruennen = kind.getBetreuungen().iterator().next();
			BetreuungspensumContainer betreuungspensumKitaBruennen =
				betreuungKitaBruennen.getBetreuungspensumContainers().iterator().next();
			betreuungspensumKitaBruennen.getBetreuungspensumJA().setTarifProHauptmahlzeit(BigDecimal.TEN);
			betreuungspensumKitaBruennen.getBetreuungspensumJA().setTarifProNebenmahlzeit(BigDecimal.TEN);
			betreuungspensumKitaBruennen.getBetreuungspensumJA().setMonatlicheHauptmahlzeiten(BigDecimal.TEN);
			betreuungspensumKitaBruennen.getBetreuungspensumJA().setMonatlicheNebenmahlzeiten(BigDecimal.ONE);
		}
		return erstgesuch;
	}

	@Override
	public String getNachname() {
		return "ASIV_11";
	}

	@Override
	public String getVorname() {
		return "Testfall 11";
	}
}
