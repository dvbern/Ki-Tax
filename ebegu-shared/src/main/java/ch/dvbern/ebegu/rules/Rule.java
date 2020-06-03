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

package ch.dvbern.ebegu.rules;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;

import ch.dvbern.ebegu.entities.AbstractPlatz;
import ch.dvbern.ebegu.entities.Einstellung;
import ch.dvbern.ebegu.entities.VerfuegungZeitabschnitt;
import ch.dvbern.ebegu.enums.EinstellungKey;
import ch.dvbern.ebegu.types.DateRange;

/**
 * Interface für alle Berechnungs-Regeln in Ki-Tax.
 */
public interface Rule {

	/**
	 * @return Datum von dem an die Regel gilt
	 */
	@Nonnull
	LocalDate validFrom();

	/**
	 * @return Datum bis zu dem die Regel gilt
	 */
	@Nonnull
	LocalDate validTo();

	/**
	 * @return DateRange, in welchem die Regel gilt
	 */
	@Nonnull
	DateRange validityPeriod();

	/**
	 * @return true, wenn die Regel *irgendwann* in diesem Zeitraum gueltig ist
	 */
	boolean isValid(@Nonnull DateRange dateRange);

	/**
	 * @return den {@link RuleType} Enumwert dieser Regel
	 */
	@Nonnull
	RuleType getRuleType();

	/**
	 * @return einzigartiger Key fuer diese Regel
	 */
	@Nonnull
	RuleKey getRuleKey();

	/**
	 * Diese Methode fuehrt die eigentliche Berechnung durch die von der Regel abgebildet wird
	 *
	 * @param platz Betreuung oder Anmeldung
	 * @param zeitabschnitte Die Zeitabschnitte die bereits ermittelt wurden
	 * @return gemergete Liste von bestehenden und neu berechneten Zeitabschnitten
	 */
	@Nonnull
	List<VerfuegungZeitabschnitt> calculate(@Nonnull AbstractPlatz platz, @Nonnull List<VerfuegungZeitabschnitt> zeitabschnitte);

	/**
	 * Gibt zurueck, ob die Regel fuer die Berechnung der Familiensituation (Fam-Groesse, Einkommen, Abzug fuer Fam-Groesse)
	 * relevant ist
	 */
	boolean isRelevantForFamiliensituation();

	/**
	 * Entscheidet aufrund der Einstellungen, ob eine Regel fuer eine Gemeinde benoetigt wird.
	 * z.B: ErwerbspensumZuschlag ist von der Gemeinde nicht ueberschrieben worden -> die
	 * Regel ErwerbspensumGemeindeCalcRule wird nicht benoetigt.
	 */
	boolean isRelevantForGemeinde(@Nonnull Map<EinstellungKey, Einstellung> einstellungMap);
}
