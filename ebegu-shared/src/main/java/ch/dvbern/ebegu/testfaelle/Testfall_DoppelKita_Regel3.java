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
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.entities.Gesuchsperiode;
import ch.dvbern.ebegu.entities.GesuchstellerContainer;
import ch.dvbern.ebegu.entities.KindContainer;
import ch.dvbern.ebegu.enums.Geschlecht;
import ch.dvbern.ebegu.enums.Kinderabzug;
import ch.dvbern.ebegu.util.MathUtil;

/**
 * Kind mit 2 Kitas. Anspruch 60%
 * Kita A: ab 01.08.2016 - 31.12.2016 40%, ab 01.01.2017 30%
 * Kita B: ab 01.08.2016 - 31.12.2016 40%, ab 01.01.2017 50%
 *
 * -> Regel 3
 * -> Kita A wird zuerst bedient, weil sie bei gleichem Beginn und gleichem Pensum zuerst eingegeben wurde, auch ab 1.1., wo Kita B höher wäre!
 */
public class Testfall_DoppelKita_Regel3 extends AbstractTestfall {

	private static final BigDecimal EINKOMMEN_GS1 = MathUtil.DEFAULT.from(53265);
	private static final BigDecimal VERMOEGEN_GS1 = MathUtil.DEFAULT.from(12147);

	public Testfall_DoppelKita_Regel3(
			Gesuchsperiode gesuchsperiode,
			boolean betreuungenBestaetigt,
			InstitutionStammdatenBuilder institutionStammdatenBuilder) {
		super(gesuchsperiode, betreuungenBestaetigt, institutionStammdatenBuilder);
	}

	public Testfall_DoppelKita_Regel3(
			Gesuchsperiode gesuchsperiode,
			InstitutionStammdatenBuilder institutionStammdatenBuilder) {
		super(gesuchsperiode, false, institutionStammdatenBuilder);
	}

	@Override
	public Gesuch fillInGesuch() {
		// Gesuch, Gesuchsteller
		Gesuch gesuch = createAlleinerziehend();
		GesuchstellerContainer gesuchsteller1 = createGesuchstellerContainer();
		gesuch.setGesuchsteller1(gesuchsteller1);
		// Erwerbspensum
		ErwerbspensumContainer erwerbspensum = createErwerbspensum(60);
		gesuchsteller1.addErwerbspensumContainer(erwerbspensum);
		// Kinder
		KindContainer kind = createKind(Geschlecht.MAENNLICH, "Doppelkita", "Dodi", LocalDate.of(2014, Month.APRIL, 13), Kinderabzug.GANZER_ABZUG, true);
		kind.setGesuch(gesuch);
		gesuch.getKindContainers().add(kind);
		// Betreuungen
		// Kita Weissenstein
		Betreuung betreuungKitaAaregg = createBetreuung(institutionStammdatenBuilder.getIdInstitutionStammdatenWeissenstein(), betreuungenBestaetigt);
		betreuungKitaAaregg.setBetreuungNummer(1);
		betreuungKitaAaregg.setKind(kind);
		kind.getBetreuungen().add(betreuungKitaAaregg);

		BetreuungspensumContainer betreuungspensumKitaAaregg = createBetreuungspensum(40, LocalDate.of(gesuchsperiode.getBasisJahrPlus1(), Month.AUGUST, 1), LocalDate.of(gesuchsperiode.getBasisJahrPlus1(), Month.DECEMBER, 31));
		betreuungspensumKitaAaregg.setBetreuung(betreuungKitaAaregg);
		betreuungKitaAaregg.getBetreuungspensumContainers().add(betreuungspensumKitaAaregg);

		BetreuungspensumContainer betreuungspensumKitaAaregg2 = createBetreuungspensum(30, LocalDate.of(gesuchsperiode.getBasisJahrPlus2(), Month.JANUARY, 1), LocalDate.of(gesuchsperiode.getBasisJahrPlus2(), Month.JULY, 31));
		betreuungspensumKitaAaregg2.setBetreuung(betreuungKitaAaregg);
		betreuungKitaAaregg.getBetreuungspensumContainers().add(betreuungspensumKitaAaregg2);

		// Kita Brünnen
		Betreuung betreuungKitaBruennen = createBetreuung(institutionStammdatenBuilder.getIdInstitutionStammdatenBruennen(), betreuungenBestaetigt);
		betreuungKitaBruennen.setBetreuungNummer(2);
		betreuungKitaBruennen.setKind(kind);
		kind.getBetreuungen().add(betreuungKitaBruennen);

		BetreuungspensumContainer betreuungspensumKitaBruennen = createBetreuungspensum(40, LocalDate.of(gesuchsperiode.getBasisJahrPlus1(), Month.AUGUST, 1), LocalDate.of(gesuchsperiode.getBasisJahrPlus1(), Month.DECEMBER, 31));
		betreuungspensumKitaBruennen.setBetreuung(betreuungKitaBruennen);
		betreuungKitaBruennen.getBetreuungspensumContainers().add(betreuungspensumKitaBruennen);

		BetreuungspensumContainer betreuungspensumKitaBruennen2 = createBetreuungspensum(50, LocalDate.of(gesuchsperiode.getBasisJahrPlus2(), Month.JANUARY, 1), LocalDate.of(gesuchsperiode.getBasisJahrPlus2(), Month.JULY, 31));
		betreuungspensumKitaBruennen2.setBetreuung(betreuungKitaBruennen);
		betreuungKitaBruennen.getBetreuungspensumContainers().add(betreuungspensumKitaBruennen2);

		// Finanzielle Situation
		FinanzielleSituationContainer finanzielleSituationContainer = createFinanzielleSituationContainer();
		finanzielleSituationContainer.getFinanzielleSituationJA().setNettolohn(MathUtil.DEFAULT.from(53265));
		finanzielleSituationContainer.getFinanzielleSituationJA().setBruttovermoegen(MathUtil.DEFAULT.from(12147));
		finanzielleSituationContainer.setGesuchsteller(gesuchsteller1);
		gesuchsteller1.setFinanzielleSituationContainer(finanzielleSituationContainer);

		// LU
		TestFaelleUtil.fillInFinSitLuZero(finanzielleSituationContainer);
		assert finanzielleSituationContainer.getFinanzielleSituationJA().getSelbstdeklaration() != null;
		finanzielleSituationContainer.getFinanzielleSituationJA().getSelbstdeklaration().setEinkunftErwerb(MathUtil.DEFAULT.from(EINKOMMEN_GS1));
		finanzielleSituationContainer.getFinanzielleSituationJA().getSelbstdeklaration().setVermoegen(MathUtil.DEFAULT.from(VERMOEGEN_GS1));

		// SO
		TestFaelleUtil.fillInFinSitSoZero(finanzielleSituationContainer);
		finanzielleSituationContainer.getFinanzielleSituationJA().setSteuerbaresVermoegen(MathUtil.DEFAULT.from(VERMOEGEN_GS1));

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
