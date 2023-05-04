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

package ch.dvbern.ebegu.util;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Comparator;

import ch.dvbern.ebegu.entities.AbstractPlatz;
import ch.dvbern.ebegu.entities.Betreuung;

public class BetreuungComparatorAppenzell implements Comparator<AbstractPlatz>, Serializable {

	private static final long serialVersionUID = 3590090321514756785L;

	@Override
	public int compare(AbstractPlatz platz1, AbstractPlatz platz2) {
		// Reihenfolge ist nur fuer Betreuungen relevant für Restanspruch, daher werden nur Betreuungen verglichen.
		// Anmeldungen Tagesschule beliben immer am gleichen Ort in Relation zu Betreuungen
		if (!(platz1 instanceof Betreuung && platz2 instanceof Betreuung)) {
			return 0;
		}

		Betreuung betreuung1 = (Betreuung) platz1;
		Betreuung betreuung2 = (Betreuung) platz2;

		BigDecimal durchschnittlicheVollkosten1 = calculateDurchschnittlicheVollkosten(betreuung1);
		BigDecimal durchschnittlicheVollkosten2 = calculateDurchschnittlicheVollkosten(betreuung2);

		//wenn die durchschnittlichen Vollkosten gleich hoch sind, wird nach der Berner Reger verglichen
		if (durchschnittlicheVollkosten1.compareTo(durchschnittlicheVollkosten2) == 0) {
			return new BetreuungComparatorBern().compare(platz1, platz2);
		}

		return durchschnittlicheVollkosten2.compareTo(durchschnittlicheVollkosten1);
	}

	private BigDecimal calculateDurchschnittlicheVollkosten(Betreuung betreuung) {
		BigDecimal anzahlBetreuungsStunden =  betreuung.getBetreuungspensumContainers().stream()
				.map(betreuungspensumContainer -> betreuungspensumContainer.getBetreuungspensumJA().getPensum())
				.reduce(BigDecimal.ZERO, BigDecimal::add);

		if (MathUtil.isZero(anzahlBetreuungsStunden)) {
			return BigDecimal.ZERO;
		}

		BigDecimal totalVollkosten = betreuung.getBetreuungspensumContainers().stream()
				.map(betreuungspensumContainer -> betreuungspensumContainer.getBetreuungspensumJA().getMonatlicheBetreuungskosten())
				.reduce(BigDecimal.ZERO, BigDecimal::add);


		return MathUtil.EXACT.divide(totalVollkosten, anzahlBetreuungsStunden);
	}
}
