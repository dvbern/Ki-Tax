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

package ch.dvbern.ebegu.rechner.kitax;

import java.math.BigDecimal;

import ch.dvbern.ebegu.util.MathUtil;

/**
 * Kapselung aller Parameter, welche für die BG-Berechnung aller Angebote benötigt werden.
 * Diese müssen aus den EbeguParametern gelesen werden.
 */
public final class KitaxParameterDTO {

	private BigDecimal oeffnungsstundenKita = MathUtil.DEFAULT.from(9); 	// TODO KITAX woher?
	private BigDecimal oeffnungstageKita = MathUtil.DEFAULT.from(240); 		// TODO KITAX woher?

	private BigDecimal beitragKantonProTag = MathUtil.DEFAULT.from(111.15);
	private BigDecimal beitragStadtProTagJahr = MathUtil.DEFAULT.from(8.00);

	private BigDecimal maxTageKita = MathUtil.DEFAULT.from(244);

	private BigDecimal maxStundenProTagKita = MathUtil.DEFAULT.from(11.50);

	private BigDecimal kostenProStundeMaximalKitaTagi = MathUtil.DEFAULT.from(12.35);
	private BigDecimal kostenProStundeMaximalTageseltern = MathUtil.DEFAULT.from(9.49);
	private BigDecimal kostenProStundeMinimal = MathUtil.DEFAULT.from(0.79);

	private BigDecimal maxMassgebendesEinkommen = MathUtil.DEFAULT.from(160000);
	private BigDecimal minMassgebendesEinkommen = MathUtil.DEFAULT.from(43000);

	private BigDecimal babyFaktor = MathUtil.DEFAULT.from(1.5);


	public KitaxParameterDTO() {

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
}
