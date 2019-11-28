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
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.validation.constraints.NotNull;

import ch.dvbern.ebegu.entities.AbstractPlatz;
import ch.dvbern.ebegu.entities.Abwesenheit;
import ch.dvbern.ebegu.entities.AbwesenheitContainer;
import ch.dvbern.ebegu.entities.Betreuung;
import ch.dvbern.ebegu.entities.VerfuegungZeitabschnitt;
import ch.dvbern.ebegu.types.DateRange;

import static java.util.Objects.requireNonNull;

/**
 * Regel für Abwesenheiten. Sie beachtet:
 * - Ab dem 31. Tag einer Abwesenheit (Krankheit oder Unfall des Kinds und bei Mutterschaft ausgeschlossen) entfällt der Gutschein.
 * Der Anspruch bleibt in dieser Zeit bestehen. D.h. ab dem 31. Tag einer Abwesenheit, wird den Eltern der Volltarif verrechnet.
 * - Hier wird mit Tagen und nicht mit Nettoarbeitstage gerechnet. D.h. eine Abwesenheit von 30 Tagen ist ok. Beim 31. Tag entfällt der Gutschein.
 * - Wann dieses Ereignis gemeldet wird, spielt keine Rolle.
 * Verweis 16.14.4
 */
public class AbwesenheitAbschnittRule extends AbstractAbschnittRule {

	private final Integer abwesenheitDaysLimit;

	public AbwesenheitAbschnittRule(
		@Nonnull DateRange validityPeriod,
		@Nonnull Integer abwesenheitDaysLimit,
		@Nonnull Locale locale
	) {
		super(RuleKey.ABWESENHEIT, RuleType.GRUNDREGEL_DATA, validityPeriod, locale);
		this.abwesenheitDaysLimit = abwesenheitDaysLimit;
	}

	/**
	 * Die Abwesenheiten der Betreuung werden zuerst nach gueltigkeit sortiert. Danach suchen wir die erste lange Abweseneheit und erstellen
	 * die 2 entsprechenden Zeitabschnitte. Alle anderen Abwesenheiten werden nicht beruecksichtigt
	 * Sollte es keine lange Abwesenheit geben, wird eine leere Liste zurueckgegeben
	 * Nur fuer Betreuungen die isAngebotJugendamtKleinkind
	 */
	@Nonnull
	@Override
	protected List<VerfuegungZeitabschnitt> createVerfuegungsZeitabschnitte(@Nonnull AbstractPlatz platz) {
		List<VerfuegungZeitabschnitt> resultlist = new ArrayList<>();
		if (requireNonNull(platz.getBetreuungsangebotTyp()).isAngebotJugendamtKleinkind()) {
			Betreuung betreuung = (Betreuung) platz;
			final List<AbwesenheitContainer> sortedAbwesenheiten = betreuung.getAbwesenheitContainers().stream().sorted().collect(Collectors.toList());
			for (final AbwesenheitContainer abwesenheit : sortedAbwesenheiten) {
				final Abwesenheit abwesenheitJA = abwesenheit.getAbwesenheitJA();
				if (abwesenheitJA != null && exceedsAbwesenheitTimeLimit(abwesenheitJA)) {
					LocalDate volltarifStart = calculateStartVolltarif(abwesenheitJA);
					LocalDate volltarifEnd = abwesenheitJA.getGueltigkeit().getGueltigBis();
					VerfuegungZeitabschnitt abschnitt = createAbwesenheitZeitAbschnitte(volltarifStart, volltarifEnd);
					resultlist.add(abschnitt);
				}
			}
		}
		return resultlist;
	}

	/**
	 * Es werden 2 Zeitabschnitte erstellt: [START_PERIODE, START_VOLLTARIF - 1Tag] und [START_VOLLTARIF, ENDE_PERIODE]
	 */
	private VerfuegungZeitabschnitt createAbwesenheitZeitAbschnitte(@Nonnull LocalDate volltarifStart, @Nonnull LocalDate volltarifEnd) {

		final VerfuegungZeitabschnitt zeitabschnitt2 = new VerfuegungZeitabschnitt(
			new DateRange(volltarifStart, volltarifEnd));
		zeitabschnitt2.setLongAbwesenheit(true);
		return zeitabschnitt2;
	}

	@NotNull
	private LocalDate calculateStartVolltarif(@Nonnull Abwesenheit abwesenheit) {
		return abwesenheit.getGueltigkeit().getGueltigAb().plusDays(abwesenheitDaysLimit);
	}

	/**
	 * True wenn die Gueltigkeit der Abwesenheit laenger als 30 Tage ist
	 */
	@SuppressWarnings("BooleanMethodNameMustStartWithQuestion")
	@NotNull
	private boolean exceedsAbwesenheitTimeLimit(@Nonnull Abwesenheit abwesenheit) {
		return (abwesenheit.getGueltigkeit().getDays()) > abwesenheitDaysLimit;
	}
}
