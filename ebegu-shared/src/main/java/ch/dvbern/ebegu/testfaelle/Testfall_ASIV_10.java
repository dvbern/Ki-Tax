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
import ch.dvbern.ebegu.testfaelle.institutionStammdatenBuilder.InstitutionStammdatenBuilder;
import ch.dvbern.ebegu.util.MathUtil;

/**
 * Wechsel von 2 auf 1. Mit nachheriger EKV, nach der Trennung (GS2 nicht mehr relevant)
 */
@SuppressWarnings("PMD.ClassNamingConventions")
public class Testfall_ASIV_10 extends AbstractASIVTestfall {

	private static final BigDecimal EINKOMMEN_GS1 = MathUtil.DEFAULT.from(70000);
	private static final BigDecimal EINKOMMEN_GS2 = MathUtil.DEFAULT.from(30000);

	public Testfall_ASIV_10(
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
		Gesuch erstgesuch = createVerheiratet();
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
		KindContainer kind = createKind(Geschlecht.MAENNLICH, "ASIV", "Kind", LocalDate.of(2014, Month.APRIL, 13), Kinderabzug.GANZER_ABZUG, true);
		kind.setGesuch(erstgesuch);
		erstgesuch.getKindContainers().add(kind);
		// Kita Brünnen
		Betreuung betreuungKitaBruennen = createBetreuung(institutionStammdatenBuilder.getIdInstitutionStammdatenBruennen(), true);
		betreuungKitaBruennen.setKind(kind);
		kind.getBetreuungen().add(betreuungKitaBruennen);
		BetreuungspensumContainer betreuungspensumKitaBruennen = createBetreuungspensum(
			100,
			LocalDate.of(gesuchsperiode.getBasisJahrPlus1(), Month.AUGUST, 15),
			LocalDate.of(gesuchsperiode.getBasisJahrPlus2(), Month.JULY, 31)
		);
		betreuungspensumKitaBruennen.setBetreuung(betreuungKitaBruennen);
		betreuungKitaBruennen.getBetreuungspensumContainers().add(betreuungspensumKitaBruennen);
		// Finanzielle Situation
		FinanzielleSituationContainer finanzielleSituationContainer = createFinanzielleSituationContainer(BigDecimal.ZERO, EINKOMMEN_GS1);
		finanzielleSituationContainer.setGesuchsteller(gesuchsteller1);
		gesuchsteller1.setFinanzielleSituationContainer(finanzielleSituationContainer);

		FinanzielleSituationContainer finanzielleSituationGS2 = createFinanzielleSituationContainer(BigDecimal.ZERO, EINKOMMEN_GS2);
		finanzielleSituationGS2.setGesuchsteller(gesuchsteller2);
		gesuchsteller2.setFinanzielleSituationContainer(finanzielleSituationGS2);

		createEmptyEKVInfoContainer(gesuch);

		return erstgesuch;
	}

	@Override
	public Gesuch createMutation(Gesuch erstgesuch) {
		Gesuch mutation = createAlleinerziehend(erstgesuch, LocalDate.of(gesuchsperiode.getBasisJahrPlus2(), Month.JANUARY, 15));

		// Einkommensverschlechterug
		EinkommensverschlechterungContainer ekvContainer = createEinkommensverschlechterungContainer(erstgesuch, false, true);
		ekvContainer.getEkvJABasisJahrPlus2().setNettolohn(MathUtil.DEFAULT.from(50000));
		Objects.requireNonNull(mutation.getGesuchsteller1());
		mutation.getGesuchsteller1().setEinkommensverschlechterungContainer(ekvContainer);
		return mutation;
	}

	@Override
	public String getNachname() {
		return "ASIV_10";
	}

	@Override
	public String getVorname() {
		return "Testfall 10";
	}
}
