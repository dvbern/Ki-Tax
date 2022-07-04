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
import ch.dvbern.ebegu.entities.EinkommensverschlechterungContainer;
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
 * Wechsel von 1 auf 2. Mit vorheriger EKV, stattgegeben auch nach Heirat
 */
@SuppressWarnings("PMD.ClassNamingConventions")
public class Testfall_ASIV_05 extends AbstractASIVTestfall {

	private static final BigDecimal EINKOMMEN_GS1 = MathUtil.DEFAULT.from(70000);

	public Testfall_ASIV_05(
			Gesuchsperiode gesuchsperiode,
			boolean betreuungenBestaetigt,
			Gemeinde gemeinde,
			InstitutionStammdatenBuilder institutionStammdatenBuilder) {
		super(gesuchsperiode, betreuungenBestaetigt, gemeinde, institutionStammdatenBuilder);
	}

	@Override
	public Gesuch fillInGesuch() {
		return createErstgesuch();
	}

	public Gesuch createErstgesuch() {
		// Gesuch, Gesuchsteller
		Gesuch erstgesuch = createAlleinerziehend();
		GesuchstellerContainer gesuchsteller1 = createGesuchstellerContainer();
		erstgesuch.setGesuchsteller1(gesuchsteller1);
		// Erwerbspensum
		ErwerbspensumContainer erwerbspensum = createErwerbspensum(100);
		gesuchsteller1.addErwerbspensumContainer(erwerbspensum);
		// Kinder
		KindContainer kind = createKind(Geschlecht.MAENNLICH, "ASIV", "Kind", LocalDate.of(2014, Month.APRIL, 13), Kinderabzug.GANZER_ABZUG, true);
		kind.setGesuch(erstgesuch);
		erstgesuch.getKindContainers().add(kind);
		// Kita Brünnen
		Betreuung betreuungKitaBruennen = createBetreuung(institutionStammdatenBuilder.getIdInstitutionStammdatenBruennen(), true);
		betreuungKitaBruennen.setKind(kind);
		kind.getBetreuungen().add(betreuungKitaBruennen);
		BetreuungspensumContainer betreuungspensumKitaBruennen = createBetreuungspensum(100, LocalDate.of(gesuchsperiode.getBasisJahrPlus1(), Month.AUGUST, 1), LocalDate.of(gesuchsperiode.getBasisJahrPlus2(), Month.JULY, 31));
		betreuungspensumKitaBruennen.setBetreuung(betreuungKitaBruennen);
		betreuungKitaBruennen.getBetreuungspensumContainers().add(betreuungspensumKitaBruennen);
		// Finanzielle Situation
		FinanzielleSituationContainer finanzielleSituationContainer = createFinanzielleSituationContainer();
		finanzielleSituationContainer.getFinanzielleSituationJA().setNettolohn(EINKOMMEN_GS1);
		finanzielleSituationContainer.setGesuchsteller(gesuchsteller1);
		gesuchsteller1.setFinanzielleSituationContainer(finanzielleSituationContainer);

		// LU
		TestFaelleUtil.fillInFinSitLuZero(finanzielleSituationContainer);
		assert finanzielleSituationContainer.getFinanzielleSituationJA().getSelbstdeklaration() != null;
		finanzielleSituationContainer.getFinanzielleSituationJA().getSelbstdeklaration().setEinkunftErwerb(EINKOMMEN_GS1);

		// SO
		TestFaelleUtil.fillInFinSitSoZero(finanzielleSituationContainer);

		// Einkommensverschlechterug
		EinkommensverschlechterungContainer ekvContainer = createEinkommensverschlechterungContainer(erstgesuch, true, false);
		ekvContainer.getEkvJABasisJahrPlus1().setNettolohn(MathUtil.DEFAULT.from(49000));
		gesuchsteller1.setEinkommensverschlechterungContainer(ekvContainer);
		return erstgesuch;
	}

	@Override
	public Gesuch createMutation(Gesuch erstgesuch) {
		// Gesuch, Gesuchsteller
		Gesuch mutation = createVerheiratet(erstgesuch, LocalDate.of(gesuchsperiode.getBasisJahrPlus2(), Month.JANUARY, 15));
		GesuchstellerContainer gesuchsteller2 = createGesuchstellerContainer();
		mutation.setGesuchsteller2(gesuchsteller2);
		// Erwerbspensum
		ErwerbspensumContainer erwerbspensum = createErwerbspensum(100);
		gesuchsteller2.addErwerbspensumContainer(erwerbspensum);
		// Finanzielle Situation
		FinanzielleSituationContainer finanzielleSituationContainerGS2 = createFinanzielleSituationContainer();
		finanzielleSituationContainerGS2.getFinanzielleSituationJA().setNettolohn(MathUtil.DEFAULT.from(30000));
		finanzielleSituationContainerGS2.setGesuchsteller(gesuchsteller2);
		gesuchsteller2.setFinanzielleSituationContainer(finanzielleSituationContainerGS2);
		// Einkommensverschlechterug
		EinkommensverschlechterungContainer ekvContainerGS2 = createEinkommensverschlechterungContainer(true, false);
		ekvContainerGS2.getEkvJABasisJahrPlus1().setNettolohn(MathUtil.DEFAULT.from(30000));
		gesuchsteller2.setEinkommensverschlechterungContainer(ekvContainerGS2);
		// LU
		TestFaelleUtil.fillInFinSitLuZero(finanzielleSituationContainerGS2);
		assert finanzielleSituationContainerGS2.getFinanzielleSituationJA().getSelbstdeklaration() != null;
		finanzielleSituationContainerGS2.getFinanzielleSituationJA().getSelbstdeklaration().setEinkunftErwerb(MathUtil.DEFAULT.from(30000));
		// SO
		TestFaelleUtil.fillInFinSitSoZero(finanzielleSituationContainerGS2);
		return mutation;
	}

	@Override
	public String getNachname() {
		return "ASIV_5";
	}

	@Override
	public String getVorname() {
		return "Testfall 5";
	}
}
