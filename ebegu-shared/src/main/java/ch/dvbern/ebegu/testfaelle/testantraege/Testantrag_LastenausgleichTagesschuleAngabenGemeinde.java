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
import ch.dvbern.ebegu.enums.gemeindeantrag.LastenausgleichTagesschuleAngabenGemeindeFormularStatus;
import ch.dvbern.ebegu.enums.gemeindeantrag.LastenausgleichTagesschuleAngabenGemeindeStatus;

public class Testantrag_LastenausgleichTagesschuleAngabenGemeinde {

	private final BigDecimal GELEISTETE_BETREUUNGSSTUNDEN_OHNE_BESONDERE_BEDUERFNISSE = new BigDecimal(5);
	private final BigDecimal DAVON_ZU_WENIGER_ALS_50_PROZENT_NORMLOHN = new BigDecimal(5);

	private LastenausgleichTagesschuleAngabenGemeinde angaben;

	public Testantrag_LastenausgleichTagesschuleAngabenGemeinde(
		BigDecimal institutionsBetreuungsstundenSum,
		LastenausgleichTagesschuleAngabenGemeindeStatus status) {
		this.angaben = new LastenausgleichTagesschuleAngabenGemeinde();
		if(status == LastenausgleichTagesschuleAngabenGemeindeStatus.IN_BEARBEITUNG_GEMEINDE) {
			this.angaben.setStatus(LastenausgleichTagesschuleAngabenGemeindeFormularStatus.IN_BEARBEITUNG);
		} else {
			this.angaben.setStatus(LastenausgleichTagesschuleAngabenGemeindeFormularStatus.ABGESCHLOSSEN);
		}
		// A
		this.angaben.setBedarfBeiElternAbgeklaert(true);
		this.angaben.setAngebotFuerFerienbetreuungVorhanden(true);
		this.angaben.setAngebotVerfuegbarFuerAlleSchulstufen(true);
		// B
		if(institutionsBetreuungsstundenSum.compareTo(BigDecimal.ZERO) == 0) {
			this.angaben.setGeleisteteBetreuungsstundenOhneBesondereBeduerfnisse(BigDecimal.ZERO);
			this.angaben.setGeleisteteBetreuungsstundenBesondereBeduerfnisse(BigDecimal.ZERO);
			this.angaben.setDavonStundenZuNormlohnWenigerAls50ProzentAusgebildete(BigDecimal.ZERO);
			this.angaben.setDavonStundenZuNormlohnMehrAls50ProzentAusgebildete(BigDecimal.ZERO);
		} else {
			this.angaben.setGeleisteteBetreuungsstundenOhneBesondereBeduerfnisse(
				GELEISTETE_BETREUUNGSSTUNDEN_OHNE_BESONDERE_BEDUERFNISSE);
			this.angaben.setGeleisteteBetreuungsstundenBesondereBeduerfnisse(
				institutionsBetreuungsstundenSum.subtract(GELEISTETE_BETREUUNGSSTUNDEN_OHNE_BESONDERE_BEDUERFNISSE)
			);
			this.angaben.setDavonStundenZuNormlohnWenigerAls50ProzentAusgebildete(DAVON_ZU_WENIGER_ALS_50_PROZENT_NORMLOHN);
			this.angaben.setDavonStundenZuNormlohnMehrAls50ProzentAusgebildete(institutionsBetreuungsstundenSum.subtract(
				DAVON_ZU_WENIGER_ALS_50_PROZENT_NORMLOHN));
		}
		this.angaben.setEinnahmenElterngebuehren(new BigDecimal(500));
		this.angaben.setTagesschuleTeilweiseGeschlossen(false);
		this.angaben.setErsteRateAusbezahlt(new BigDecimal(350));
		// C
		this.angaben.setGesamtKostenTagesschule(new BigDecimal(15000));
		this.angaben.setEinnnahmenVerpflegung(new BigDecimal(1500));
		this.angaben.setEinnahmenSubventionenDritter(new BigDecimal(1500));
		this.angaben.setUeberschussErzielt(false);
		// E
		this.angaben.setBetreuungsstundenDokumentiertUndUeberprueft(true);
		this.angaben.setElterngebuehrenGemaessVerordnungBerechnet(true);
		this.angaben.setEinkommenElternBelegt(true);
		this.angaben.setMaximalTarif(true);
		this.angaben.setMindestens50ProzentBetreuungszeitDurchAusgebildetesPersonal(true);
		this.angaben.setAusbildungenMitarbeitendeBelegt(true);
	}

	public LastenausgleichTagesschuleAngabenGemeinde getAngaben() {
		return angaben;
	}
}
