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
import java.util.Objects;

import ch.dvbern.ebegu.entities.Betreuung;
import ch.dvbern.ebegu.entities.BetreuungspensumContainer;
import ch.dvbern.ebegu.entities.ErwerbspensumContainer;
import ch.dvbern.ebegu.entities.FinanzielleSituationContainer;
import ch.dvbern.ebegu.entities.Gemeinde;
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.entities.Gesuchsperiode;
import ch.dvbern.ebegu.entities.GesuchstellerAdresse;
import ch.dvbern.ebegu.entities.GesuchstellerAdresseContainer;
import ch.dvbern.ebegu.entities.GesuchstellerContainer;
import ch.dvbern.ebegu.entities.KindContainer;
import ch.dvbern.ebegu.enums.Geschlecht;
import ch.dvbern.ebegu.enums.Kinderabzug;
import ch.dvbern.ebegu.types.DateRange;
import ch.dvbern.ebegu.util.Constants;
import ch.dvbern.ebegu.util.MathUtil;

/**
 * Dieser TestFall ist eine Kopie von Waelti Dagmar (im Status vom 10.11.2016) aber mit
 * - Wohnadresse NICHT in Bern
 * - Umzugsadresse ab 15.12.2016 in Bern
 * - Umzugsadresse ab 01.01.2017 NICHT in Bern
 * <p>
 * PS: Die Daten von Waelti Dagmar werden direkt kopiert anstatt die Methoden aufzurufen. Dieses dupliziert Code aber
 * macht einfacher, diesen Fall einzeln betrachten und verwalten zu koennen, anstatt vom Test von Waelti Dagmar abzuhaengen
 */
public class Testfall08_UmzugAusInAusBern extends AbstractTestfall {

	static final int VERMOEGEN = 12147;
	static final int EINKOMMEN = 53265;

	public Testfall08_UmzugAusInAusBern(
			Gesuchsperiode gesuchsperiode,
			boolean betreuungenBestaetigt,
			Gemeinde gemeinde, InstitutionStammdatenBuilder institutionStammdatenBuilder) {
		super(gesuchsperiode, betreuungenBestaetigt, gemeinde, institutionStammdatenBuilder);
	}

	@Override
	public Gesuch fillInGesuch() {
		// Gesuch, Gesuchsteller
		gesuch = createAlleinerziehend();
		GesuchstellerContainer gesuchsteller1 = createGesuchstellerContainer();
		gesuch.setGesuchsteller1(gesuchsteller1);

		//Wohnadresse NICHT in Bern: 01.01.1900 - 14.12.2017
		GesuchstellerAdresse gesuchstellerAdresseJA = gesuchsteller1.getAdressen().get(0).getGesuchstellerAdresseJA();
		Objects.requireNonNull(gesuchstellerAdresseJA);
		gesuchstellerAdresseJA.setNichtInGemeinde(true);
		final int gesuchsperiodeFirstYear = gesuchsperiode.getGueltigkeit().getGueltigAb().getYear();
		gesuchstellerAdresseJA
			.setGueltigkeit(new DateRange(Constants.START_OF_TIME, LocalDate.of(gesuchsperiodeFirstYear, 12, 14)));

		// Umzugsadresse am 15.12.2016 in Bern: 15.12.2017 - 31.12.2017
		GesuchstellerAdresseContainer umzugInBern = createWohnadresseContainer(gesuchsteller1);
		GesuchstellerAdresse umzugInBernAdresseJA = umzugInBern.getGesuchstellerAdresseJA();
		Objects.requireNonNull(umzugInBernAdresseJA);
		umzugInBernAdresseJA.setNichtInGemeinde(false);
		umzugInBernAdresseJA.setGueltigkeit(new DateRange(LocalDate.of(gesuchsperiodeFirstYear, 12, 15),
			LocalDate.of(gesuchsperiodeFirstYear, 12, 31)));
		gesuchsteller1.getAdressen().add(umzugInBern);

		// Umzugsadresse am 01.01.2017 NICHT in Bern -> Berechtigt bis Ende Monat (Ende Januar)
		GesuchstellerAdresseContainer umzugAusBern = createWohnadresseContainer(gesuchsteller1);
		GesuchstellerAdresse umzugAusBernAdresseJA = umzugAusBern.getGesuchstellerAdresseJA();
		Objects.requireNonNull(umzugAusBernAdresseJA);
		umzugAusBernAdresseJA.setNichtInGemeinde(true);
		umzugAusBernAdresseJA.setGueltigkeit(new DateRange(LocalDate.of(gesuchsperiodeFirstYear + 1, 1, 1), Constants.END_OF_TIME));
		gesuchsteller1.getAdressen().add(umzugAusBern);

		// Erwerbspensum
		ErwerbspensumContainer erwerbspensum = createErwerbspensum(80);
		gesuchsteller1.addErwerbspensumContainer(erwerbspensum);
		// Kinder
		KindContainer kind = createKind(Geschlecht.MAENNLICH, getNachname(), "Paco", LocalDate.of(2014, Month.APRIL, 13), Kinderabzug.GANZER_ABZUG, true);
		kind.setGesuch(gesuch);
		gesuch.getKindContainers().add(kind);
		// Betreuungen
		// Kita Weissenstein
		Betreuung betreuungKitaAaregg = createBetreuung(institutionStammdatenBuilder.getIdInstitutionStammdatenWeissenstein(), betreuungenBestaetigt);
		betreuungKitaAaregg.setKind(kind);
		kind.getBetreuungen().add(betreuungKitaAaregg);
		BetreuungspensumContainer betreuungspensumKitaAaregg = createBetreuungspensum(80, LocalDate.of(gesuchsperiode.getBasisJahrPlus1(), Month.AUGUST, 1), LocalDate.of(gesuchsperiode.getBasisJahrPlus2(), Month.JANUARY, 31));
		betreuungspensumKitaAaregg.setBetreuung(betreuungKitaAaregg);
		betreuungKitaAaregg.getBetreuungspensumContainers().add(betreuungspensumKitaAaregg);
		// Kita Brünnen
		Betreuung betreuungKitaBruennen = createBetreuung(institutionStammdatenBuilder.getIdInstitutionStammdatenBruennen(), betreuungenBestaetigt);
		betreuungKitaBruennen.setBetreuungNummer(2);
		betreuungKitaBruennen.setKind(kind);
		kind.getBetreuungen().add(betreuungKitaBruennen);
		BetreuungspensumContainer betreuungspensumKitaBruennen = createBetreuungspensum(40, LocalDate.of(gesuchsperiode.getBasisJahrPlus2(), Month.FEBRUARY, 1), LocalDate.of(gesuchsperiode.getBasisJahrPlus2(), Month.JULY, 31));
		betreuungspensumKitaBruennen.setBetreuung(betreuungKitaBruennen);
		betreuungKitaBruennen.getBetreuungspensumContainers().add(betreuungspensumKitaBruennen);
		// Finanzielle Situation
		FinanzielleSituationContainer finanzielleSituationContainer = createFinanzielleSituationContainer();
		finanzielleSituationContainer.getFinanzielleSituationJA().setNettolohn(MathUtil.DEFAULT.from(EINKOMMEN));
		finanzielleSituationContainer.getFinanzielleSituationJA().setBruttovermoegen(Objects.requireNonNull(MathUtil.DEFAULT.from(VERMOEGEN)));
		finanzielleSituationContainer.setGesuchsteller(gesuchsteller1);
		gesuchsteller1.setFinanzielleSituationContainer(finanzielleSituationContainer);

		// LU
		TestFaelleUtil.fillInFinSitLuZero(finanzielleSituationContainer);
		assert finanzielleSituationContainer.getFinanzielleSituationJA().getSelbstdeklaration() != null;
		finanzielleSituationContainer.getFinanzielleSituationJA().getSelbstdeklaration().setVermoegen(MathUtil.DEFAULT.from(VERMOEGEN));
		finanzielleSituationContainer.getFinanzielleSituationJA().getSelbstdeklaration().setEinkunftErwerb(MathUtil.DEFAULT.from(EINKOMMEN));

		// SO
		TestFaelleUtil.fillInFinSitSoZero(finanzielleSituationContainer);
		finanzielleSituationContainer.getFinanzielleSituationJA().setSteuerbaresVermoegen(MathUtil.DEFAULT.from(VERMOEGEN));

		createEmptyEKVInfoContainer(gesuch);

		return gesuch;
	}

	@Override
	public String getNachname() {
		return "Zügelmann";
	}

	@Override
	public String getVorname() {
		return "Manolo";
	}
}
