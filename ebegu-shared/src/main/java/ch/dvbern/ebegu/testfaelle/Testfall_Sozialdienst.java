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

import javax.annotation.Nonnull;

import ch.dvbern.ebegu.entities.Adresse;
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
import ch.dvbern.ebegu.entities.sozialdienst.Sozialdienst;
import ch.dvbern.ebegu.entities.sozialdienst.SozialdienstFall;
import ch.dvbern.ebegu.enums.Geschlecht;
import ch.dvbern.ebegu.enums.Kinderabzug;
import ch.dvbern.ebegu.enums.SozialdienstFallStatus;
import ch.dvbern.ebegu.util.MathUtil;

/**
 * http://localhost:8080/ebegu/api/v1/testfaelle/testfall/1
 * https://ebegu.dvbern.ch/ebegu/api/v1/testfaelle/testfall/1
 */
public class Testfall_Sozialdienst extends AbstractTestfall {

	private final Sozialdienst sozialdienst;

	public Testfall_Sozialdienst(
		Gesuchsperiode gesuchsperiode,
		Collection<InstitutionStammdaten> institutionStammdatenList,
		boolean betreuungenBestaetigt,
		Gemeinde gemeinde,
		@Nonnull Sozialdienst sozialdienst
	) {
		super(gesuchsperiode, institutionStammdatenList, betreuungenBestaetigt, gemeinde);
		this.sozialdienst = sozialdienst;
	}

	@Override
	public Gesuch fillInGesuch() {
		// Gesuch, Gesuchsteller
		gesuch = createAlleinerziehend();

		// Sozialdienst
		SozialdienstFall sozialdienstFall = new SozialdienstFall();
		sozialdienstFall.setSozialdienst(sozialdienst);
		sozialdienstFall.setName("Wälti");
		sozialdienstFall.setVorname("Simon");
		sozialdienstFall.setStatus(SozialdienstFallStatus.AKTIV);
		sozialdienstFall.setGeburtsdatum(LocalDate.of(2014, Month.APRIL, 13));

		Adresse adresse = new Adresse();
		adresse.setStrasse("Teststrasse");
		adresse.setHausnummer("1");
		adresse.setPlz("3000");
		adresse.setOrt("Bern");
		sozialdienstFall.setAdresse(adresse);

		gesuch.getFall().setSozialdienstFall(sozialdienstFall);

		// Gesuchsteller
		GesuchstellerContainer gesuchsteller1 = createGesuchstellerContainer();
		gesuch.setGesuchsteller1(gesuchsteller1);
		// Erwerbspensum
		ErwerbspensumContainer erwerbspensum = createErwerbspensum(80);
		gesuchsteller1.addErwerbspensumContainer(erwerbspensum);
		// Kinder
		KindContainer kind = createKind(Geschlecht.MAENNLICH, "Wälti", "Simon", LocalDate.of(2014, Month.APRIL, 13), Kinderabzug.GANZER_ABZUG, true);
		kind.setGesuch(gesuch);
		gesuch.getKindContainers().add(kind);
		// Betreuungen
		// Kita Weissenstein
		Betreuung betreuungKitaAaregg = createBetreuung(ID_INSTITUTION_STAMMDATEN_WEISSENSTEIN_KITA, betreuungenBestaetigt);
		betreuungKitaAaregg.setKind(kind);
		kind.getBetreuungen().add(betreuungKitaAaregg);
		BetreuungspensumContainer betreuungspensumKitaAaregg = createBetreuungspensum(80, LocalDate.of(gesuchsperiode.getBasisJahrPlus1(), Month.AUGUST, 1), LocalDate.of(gesuchsperiode.getBasisJahrPlus2(), Month.JANUARY, 31));
		betreuungspensumKitaAaregg.setBetreuung(betreuungKitaAaregg);
		betreuungKitaAaregg.getBetreuungspensumContainers().add(betreuungspensumKitaAaregg);
		// Kita Brünnen
		Betreuung betreuungKitaBruennen = createBetreuung(ID_INSTITUTION_STAMMDATEN_BRUENNEN_KITA, betreuungenBestaetigt);
		betreuungKitaBruennen.setBetreuungNummer(2);
		betreuungKitaBruennen.setKind(kind);
		kind.getBetreuungen().add(betreuungKitaBruennen);
		BetreuungspensumContainer betreuungspensumKitaBruennen = createBetreuungspensum(40, LocalDate.of(gesuchsperiode.getBasisJahrPlus2(), Month.FEBRUARY, 1), LocalDate.of(gesuchsperiode.getBasisJahrPlus2(), Month.JULY, 31));
		betreuungspensumKitaBruennen.setBetreuung(betreuungKitaBruennen);
		betreuungKitaBruennen.getBetreuungspensumContainers().add(betreuungspensumKitaBruennen);
		// Finanzielle Situation
		FinanzielleSituationContainer finanzielleSituationContainer = createFinanzielleSituationContainer();
		finanzielleSituationContainer.getFinanzielleSituationJA().setNettolohn(MathUtil.DEFAULT.from(53265));
		finanzielleSituationContainer.getFinanzielleSituationJA().setBruttovermoegen(Objects.requireNonNull(MathUtil.DEFAULT.from(12147)));
		finanzielleSituationContainer.setGesuchsteller(gesuchsteller1);
		gesuchsteller1.setFinanzielleSituationContainer(finanzielleSituationContainer);

		createEmptyEKVInfoContainer(gesuch);

		return gesuch;
	}

	@Override
	public String getNachname() {
		return "Wälti";
	}

	@Override
	public String getVorname() {
		return "Dagmar";
	}

}
