/*
 * Copyright (C) 2020 DV Bern AG, Switzerland
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

package ch.dvbern.ebegu.util;

import java.math.BigDecimal;
import java.time.LocalDate;

import javax.annotation.Nonnull;

import ch.dvbern.ebegu.entities.Gemeinde;

/**
 * Kapselung aller Parameter, welche für die BG-Berechnung aller Angebote benötigt werden.
 * Diese müssen aus den EbeguParametern gelesen werden.
 */
public final class KitaxUebergangsloesungParameter {

	private BigDecimal oeffnungsstundenKita = MathUtil.DEFAULT.from(11.5); 	// TODO KITAX woher? Achtung: Der Test beruht auf diesen Daten!
	private BigDecimal oeffnungstageKita = MathUtil.DEFAULT.from(240); 		// TODO KITAX woher? Achtung: Der Test beruht auf diesen Daten!

	private BigDecimal beitragKantonProTag = MathUtil.DEFAULT.from(111.15);
	private BigDecimal beitragStadtProTagJahr = MathUtil.DEFAULT.from(8.00);

	private BigDecimal maxTageKita = MathUtil.DEFAULT.from(244);
	private BigDecimal maxStundenProTagKita = MathUtil.DEFAULT.from(11.5);

	private BigDecimal kostenProStundeMaximalKitaTagi = MathUtil.DEFAULT.from(12.35);
	private BigDecimal kostenProStundeMaximalTageseltern = MathUtil.DEFAULT.from(9.49);
	private BigDecimal kostenProStundeMinimal = MathUtil.DEFAULT.from(0.79);

	private BigDecimal maxMassgebendesEinkommen = MathUtil.DEFAULT.from(160000);
	private BigDecimal minMassgebendesEinkommen = MathUtil.DEFAULT.from(43000);

	private int minEWP = 10; // Gilt fuer alle Schulstufen. Zuschlaege/Rundungen werden im Korrekturmodus gemacht

	private BigDecimal babyFaktor = MathUtil.DEFAULT.from(1.5);

	private LocalDate stadtBernAsivStartDate = null;
	private boolean isStadtBernAsivConfiguered = false;


	public KitaxUebergangsloesungParameter(@Nonnull LocalDate stadtBernAsivStartDate, boolean isStadtBernAsivConfiguered) {
		this.stadtBernAsivStartDate = stadtBernAsivStartDate;
		this.isStadtBernAsivConfiguered = isStadtBernAsivConfiguered;
	}

	public BigDecimal getOeffnungsstundenKita() {
		return oeffnungsstundenKita;
	}

	public BigDecimal getOeffnungstageKita() {
		return oeffnungstageKita;
	}

	public BigDecimal getBeitragKantonProTag() {
		return beitragKantonProTag;
	}

	public BigDecimal getBeitragStadtProTagJahr() {
		return beitragStadtProTagJahr;
	}

	public BigDecimal getMaxTageKita() {
		return maxTageKita;
	}

	public BigDecimal getMaxStundenProTagKita() {
		return maxStundenProTagKita;
	}

	public BigDecimal getKostenProStundeMaximalKitaTagi() {
		return kostenProStundeMaximalKitaTagi;
	}

	public BigDecimal getKostenProStundeMaximalTageseltern() {
		return kostenProStundeMaximalTageseltern;
	}

	public BigDecimal getKostenProStundeMinimal() {
		return kostenProStundeMinimal;
	}

	public BigDecimal getMaxMassgebendesEinkommen() {
		return maxMassgebendesEinkommen;
	}

	public BigDecimal getMinMassgebendesEinkommen() {
		return minMassgebendesEinkommen;
	}

	public BigDecimal getBabyFaktor() {
		return babyFaktor;
	}

	public int getMinEWP() {
		return minEWP;
	}

	public void setMinEWP(int minEWP) {
		this.minEWP = minEWP;
	}

	public LocalDate getStadtBernAsivStartDate() {
		return stadtBernAsivStartDate;
	}

	public void setStadtBernAsivStartDate(LocalDate stadtBernAsivStartDate) {
		this.stadtBernAsivStartDate = stadtBernAsivStartDate;
	}

	public boolean isStadtBernAsivConfiguered() {
		return isStadtBernAsivConfiguered;
	}

	public void setStadtBernAsivConfiguered(boolean stadtBernAsivConfiguered) {
		isStadtBernAsivConfiguered = stadtBernAsivConfiguered;
	}

	public boolean isGemeindeWithKitaxUebergangsloesung(@Nonnull Gemeinde gemeinde) {
		// Zum Testen behandeln wir Paris wie Bern
		long bfsNummer = gemeinde.getBfsNummer();
		return bfsNummer == 351 || bfsNummer == 99998;
	}
}
