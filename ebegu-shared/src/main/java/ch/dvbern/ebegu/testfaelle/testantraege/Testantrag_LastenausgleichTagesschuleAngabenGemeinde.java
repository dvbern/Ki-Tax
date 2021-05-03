/*
 * Copyright (C) 2021 DV Bern AG, Switzerland
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package ch.dvbern.ebegu.testfaelle.testantraege;

import java.math.BigDecimal;

import ch.dvbern.ebegu.entities.gemeindeantrag.LastenausgleichTagesschuleAngabenGemeinde;

public class Testantrag_LastenausgleichTagesschuleAngabenGemeinde extends LastenausgleichTagesschuleAngabenGemeinde {

	private final BigDecimal GELEISTETE_BETREUUNGSSTUNDEN_OHNE_BESONDERE_BEDUERFNISSE = new BigDecimal(5);
	private final BigDecimal DAVON_ZU_WENIGER_ALS_50_PROZENT_NORMLOHN = new BigDecimal(5);

	public Testantrag_LastenausgleichTagesschuleAngabenGemeinde(BigDecimal institutionsBetreuungsstundenSum) {
		// A
		this.setBedarfBeiElternAbgeklaert(true);
		this.setAngebotFuerFerienbetreuungVorhanden(true);
		this.setAngebotVerfuegbarFuerAlleSchulstufen(true);
		// B
		this.setGeleisteteBetreuungsstundenOhneBesondereBeduerfnisse(
			GELEISTETE_BETREUUNGSSTUNDEN_OHNE_BESONDERE_BEDUERFNISSE);
		this.setGeleisteteBetreuungsstundenBesondereBeduerfnisse(
			institutionsBetreuungsstundenSum.subtract(GELEISTETE_BETREUUNGSSTUNDEN_OHNE_BESONDERE_BEDUERFNISSE)
		);
		this.setDavonStundenZuNormlohnWenigerAls50ProzentAusgebildete(DAVON_ZU_WENIGER_ALS_50_PROZENT_NORMLOHN);
		this.setDavonStundenZuNormlohnMehrAls50ProzentAusgebildete(institutionsBetreuungsstundenSum.subtract(
			DAVON_ZU_WENIGER_ALS_50_PROZENT_NORMLOHN));
		this.setEinnahmenElterngebuehren(new BigDecimal(1500));
		this.setTagesschuleTeilweiseGeschlossen(false);
		this.setErsteRateAusbezahlt(new BigDecimal(1000));
		// C
		this.setGesamtKostenTagesschule(new BigDecimal(15000));
		this.setEinnnahmenVerpflegung(new BigDecimal(1500));
		this.setEinnahmenSubventionenDritter(new BigDecimal(1500));
		this.setUeberschussErzielt(false);
		// E
		this.setBetreuungsstundenDokumentiertUndUeberprueft(true);
		this.setElterngebuehrenGemaessVerordnungBerechnet(true);
		this.setEinkommenElternBelegt(true);
		this.setMaximalTarif(true);
		this.setMindestens50ProzentBetreuungszeitDurchAusgebildetesPersonal(true);
		this.setAusbildungenMitarbeitendeBelegt(true);
	}
}
