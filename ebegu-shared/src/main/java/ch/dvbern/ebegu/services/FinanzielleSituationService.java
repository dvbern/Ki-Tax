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

package ch.dvbern.ebegu.services;

import java.util.Optional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import ch.dvbern.ebegu.dto.FinanzielleSituationResultateDTO;
import ch.dvbern.ebegu.dto.FinanzielleSituationStartDTO;
import ch.dvbern.ebegu.dto.JaxFinanzielleSituationAufteilungDTO;
import ch.dvbern.ebegu.entities.AbstractFinanzielleSituation;
import ch.dvbern.ebegu.entities.FinanzielleSituation;
import ch.dvbern.ebegu.entities.FinanzielleSituationContainer;
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.entities.GesuchstellerContainer;
import ch.dvbern.ebegu.entities.NeueVeranlagungsMitteilung;

/**
 * Service zum Verwalten von Finanziellen Situationen
 */
public interface FinanzielleSituationService {

	/**
	 * Speichert die allgemeinen Fragen der Finanziellen Situation.
	 * Diese werden auf der Familiensituation (bei gemeinsamer Stek) bzw. auf der
	 * Finanziellen Situation des GS1 gespeichert (bei getrennter Stek)
	 */
	@Nonnull
	Gesuch saveFinanzielleSituationStart(
		@Nonnull FinanzielleSituationContainer finanzielleSituation,
		@Nonnull FinanzielleSituationStartDTO finSitStartDTO,
		@Nonnull String gesuchId);

	/**
	 * Speichert die Finanzielle Situation für einen Gesuchsteller
	 */
	@Nonnull
	FinanzielleSituationContainer saveFinanzielleSituation(
		@Nonnull FinanzielleSituationContainer finanzielleSituation,
		@Nonnull String gesuchId);

	/**
	 * @param id PK (id) der FinanzielleSituation
	 * @return FinanzielleSituation mit dem gegebenen key oder null falls nicht vorhanden
	 */
	@Nonnull
	Optional<FinanzielleSituationContainer> findFinanzielleSituation(@Nonnull String id);

	/**
	 * Berechnet die Finanzielle Situation beider Gesuchsteller
	 */
	@Nonnull
	FinanzielleSituationResultateDTO calculateResultate(@Nonnull Gesuch gesuch);

	/**
	 * Berechnet die Finanzdaten für die Verfügung, d.h. inklusive allfälliger Einkommensverschlechterungen
	 * Das Resultat wird direkt dem Gesuch angehängt
	 */
	void calculateFinanzDaten(@Nonnull Gesuch gesuch);

	/**
	 * speichert einfach die Datenstand ohne weitere Arbeit
	 *
	 * @param finanzielleSituation
	 */
	@Nonnull
	FinanzielleSituationContainer saveFinanzielleSituationTemp(FinanzielleSituationContainer finanzielleSituation);

	void setValuesFromAufteilungDTO(@Nonnull FinanzielleSituation finSitGs1, @Nonnull FinanzielleSituation finSitGs2, @Nonnull JaxFinanzielleSituationAufteilungDTO dto);

	@Nullable
    FinanzielleSituation findFinanzielleSituationForNeueVeranlagungsMitteilung(@Nonnull
		NeueVeranlagungsMitteilung persistedMitteilung);

	void resetCompleteSchwyzFinSitData(
		AbstractFinanzielleSituation finSit,
		GesuchstellerContainer gesuchstellerContainer);
}
