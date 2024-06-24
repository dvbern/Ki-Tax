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
import java.util.HashSet;
import java.util.Set;

import ch.dvbern.ebegu.entities.Abwesenheit;
import ch.dvbern.ebegu.entities.AbwesenheitContainer;
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
import ch.dvbern.ebegu.types.DateRange;
import ch.dvbern.ebegu.util.MathUtil;

/**
 * Dieser TestFall ist eine Kopie von Waelti Dagmar (im Status vom 10.11.2016) aber mit einer Abwesenheit fuer das Kind Julio
 * und die KITA Weissenstein. Die Abwesenheit laeuft von 11.10.2016 bis 25.11.2016. Deshalb muss ab dem 03.10.2016 Volltarif eintretten
 */
@SuppressWarnings("PMD.ClassNamingConventions")
public class Testfall09_Abwesenheit extends AbstractTestfall {

	static final int VERMOEGEN = 12147;
	static final int EINKOMMEN = 53265;

	public Testfall09_Abwesenheit(
			Gesuchsperiode gesuchsperiode,
			boolean betreuungenBestaetigt,
			Gemeinde gemeinde, InstitutionStammdatenBuilder institutionStammdatenBuilder) {
		super(gesuchsperiode, betreuungenBestaetigt, gemeinde, institutionStammdatenBuilder);
	}

	@Override
	public Gesuch fillInGesuch() {
		// Gesuch, Gesuchsteller
		gesuch = createAlleinerziehend();
		GesuchstellerContainer gesuchsteller1 = createGesuchstellerContainer(1);
		gesuch.setGesuchsteller1(gesuchsteller1);

		// Erwerbspensum
		ErwerbspensumContainer erwerbspensum = createErwerbspensum(80);
		gesuchsteller1.addErwerbspensumContainer(erwerbspensum);
		// Kinder
		KindContainer kind = createKind(Geschlecht.MAENNLICH, getNachname(), "Julio", LocalDate.of(2014, Month.APRIL, 13), Kinderabzug.GANZER_ABZUG, true);
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
		// Abwesenheit
		Set<AbwesenheitContainer> abwesenheitContainerSet = new HashSet<>();
		AbwesenheitContainer abwesenheitContainer = new AbwesenheitContainer();
		abwesenheitContainer.setBetreuung(betreuungKitaAaregg);
		Abwesenheit abwesenheitJA = new Abwesenheit();
		abwesenheitJA.setGueltigkeit(new DateRange(LocalDate.of(gesuchsperiode.getBasisJahrPlus1(), Month.OCTOBER, 11), LocalDate.of(gesuchsperiode.getBasisJahrPlus1(), Month.NOVEMBER, 25)));
		abwesenheitContainer.setAbwesenheitJA(abwesenheitJA);
		abwesenheitContainerSet.add(abwesenheitContainer);
		betreuungKitaAaregg.setAbwesenheitContainers(abwesenheitContainerSet);

		// Kita Brünnen
		Betreuung betreuungKitaBruennen = createBetreuung(institutionStammdatenBuilder.getIdInstitutionStammdatenBruennen(), betreuungenBestaetigt);
		betreuungKitaBruennen.setBetreuungNummer(2);
		betreuungKitaBruennen.setKind(kind);
		kind.getBetreuungen().add(betreuungKitaBruennen);
		BetreuungspensumContainer betreuungspensumKitaBruennen = createBetreuungspensum(40, LocalDate.of(gesuchsperiode.getBasisJahrPlus2(), Month.FEBRUARY, 1), LocalDate.of(gesuchsperiode.getBasisJahrPlus2(), Month.JULY, 31));
		betreuungspensumKitaBruennen.setBetreuung(betreuungKitaBruennen);
		betreuungKitaBruennen.getBetreuungspensumContainers().add(betreuungspensumKitaBruennen);
		// Finanzielle Situation
		FinanzielleSituationContainer finanzielleSituationContainer =
				createFinanzielleSituationContainer(MathUtil.DEFAULT.from(VERMOEGEN), MathUtil.DEFAULT.from(EINKOMMEN));
		finanzielleSituationContainer.setGesuchsteller(gesuchsteller1);
		gesuchsteller1.setFinanzielleSituationContainer(finanzielleSituationContainer);

		createEmptyEKVInfoContainer(gesuch);

		return gesuch;
	}

	@Override
	public String getNachname() {
		return "Abwesend";
	}

	@Override
	public String getVorname() {
		return "Cindy";
	}

}
