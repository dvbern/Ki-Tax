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

package ch.dvbern.ebegu.services.gemeindeantrag;

import ch.dvbern.ebegu.entities.Benutzer;
import ch.dvbern.ebegu.entities.Gemeinde;
import ch.dvbern.ebegu.entities.Gesuchsperiode;
import ch.dvbern.ebegu.entities.gemeindeantrag.*;
import ch.dvbern.ebegu.enums.Sprache;
import ch.dvbern.ebegu.errors.MergeDocException;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * Service fuer die Ferienbetreuungen
 */
public interface FerienbetreuungService {

	@Nonnull
	Collection<FerienbetreuungAngabenContainer> getAllFerienbetreuungAntraege();

	@Nonnull
	List<FerienbetreuungAngabenContainer> getFerienbetreuungAntraege(
		@Nullable String gemeinde,
		@Nullable String periode,
		@Nullable String status,
		@Nullable String timestampMutiert,
		@Nullable Benutzer verantwortlicher
	);

	@Nonnull
	Optional<FerienbetreuungAngabenContainer> findFerienbetreuungAngabenContainer(@Nonnull String containerId);

	@Nonnull
	FerienbetreuungAngabenContainer saveFerienbetreuungAngabenContainer(@Nonnull FerienbetreuungAngabenContainer container);

	@Nonnull
	FerienbetreuungAngabenContainer createFerienbetreuungAntrag(@Nonnull Gemeinde gemeinde, @Nonnull Gesuchsperiode gesuchsperiode);

	void saveKommentar(@Nonnull String id, @Nonnull String kommentar);

	void saveVerantwortlicher(@Nonnull String containerId, @Nullable String username);

	@Nonnull
	Optional<FerienbetreuungAngabenStammdaten> findFerienbetreuungAngabenStammdaten(@Nonnull String stammdatenId);

	@Nonnull
	Optional<FerienbetreuungAngabenAngebot> findFerienbetreuungAngabenAngebot(@Nonnull String angebotId);

	@Nonnull
	Optional<FerienbetreuungAngabenNutzung> findFerienbetreuungAngabenNutzung(@Nonnull String nutzungId);

	@Nonnull
	Optional<FerienbetreuungAngabenKostenEinnahmen> findFerienbetreuungAngabenKostenEinnahmen(@Nonnull String kostenEinnahmenId);

	@Nonnull
	Optional<FerienbetreuungBerechnungen> findFerienbetreuungBerechnung(@Nonnull String berechnungId);

	@Nonnull
	FerienbetreuungAngabenStammdaten saveFerienbetreuungAngabenStammdaten(@Nonnull FerienbetreuungAngabenStammdaten stammdaten);

	@Nonnull
	FerienbetreuungAngabenAngebot saveFerienbetreuungAngabenAngebot(@Nonnull FerienbetreuungAngabenAngebot angebot);

	@Nonnull
	FerienbetreuungAngabenNutzung saveFerienbetreuungAngabenNutzung(@Nonnull FerienbetreuungAngabenNutzung nutzung);

	@Nonnull
	FerienbetreuungAngabenKostenEinnahmen saveFerienbetreuungAngabenKostenEinnahmen(@Nonnull FerienbetreuungAngabenKostenEinnahmen kostenEinnahmen);

	@Nonnull
	FerienbetreuungBerechnungen saveFerienbetreuungBerechnungen(@Nonnull FerienbetreuungBerechnungen berechnungen);

	@Nonnull
	FerienbetreuungAngabenAngebot ferienbetreuungAngebotAbschliessen(@Nonnull FerienbetreuungAngabenAngebot angebot);

	@Nonnull
	FerienbetreuungAngabenAngebot ferienbetreuungAngebotFalscheAngaben(@Nonnull FerienbetreuungAngabenAngebot angebot);

	@Nonnull
	FerienbetreuungAngabenNutzung ferienbetreuungAngabenNutzungAbschliessen(@Nonnull FerienbetreuungAngabenNutzung nutzung);

	@Nonnull
	FerienbetreuungAngabenNutzung ferienbetreuungAngabenNutzungFalscheAngaben(@Nonnull FerienbetreuungAngabenNutzung nutzung);

	@Nonnull
	FerienbetreuungAngabenKostenEinnahmen ferienbetreuungAngabenKostenEinnahmenAbschliessen(
		@Nonnull FerienbetreuungAngabenKostenEinnahmen kostenEinnahmen);

	@Nonnull
	FerienbetreuungAngabenKostenEinnahmen ferienbetreuungAngabenKostenEinnahmenFalscheAngaben(
		@Nonnull FerienbetreuungAngabenKostenEinnahmen kostenEinnahmen);

	@Nonnull
	FerienbetreuungAngabenStammdaten ferienbetreuungAngabenStammdatenAbschliessen(
		@Nonnull FerienbetreuungAngabenStammdaten stammdaten);

	@Nonnull
	FerienbetreuungAngabenStammdaten ferienbetreuungAngabenStammdatenFalscheAngaben(
		@Nonnull FerienbetreuungAngabenStammdaten stammdaten);

	@Nonnull
	FerienbetreuungAngabenContainer ferienbetreuungAngabenFreigeben(
		@Nonnull FerienbetreuungAngabenContainer container);

	@Nonnull
	FerienbetreuungAngabenContainer ferienbetreuungAngabenGeprueft(
		@Nonnull FerienbetreuungAngabenContainer container);

	@Nonnull FerienbetreuungAngabenContainer ferienbetreuungAngabenZurueckAnGemeinde(
		@Nonnull FerienbetreuungAngabenContainer container);

	void deleteFerienbetreuungAntragIfExists(@Nonnull Gemeinde gemeinde, @Nonnull Gesuchsperiode gesuchsperiode);

	void deleteAntragIfExistsAndIsNotAbgeschlossen(
			@Nonnull Gemeinde gemeinde,
			@Nonnull Gesuchsperiode gesuchsperiode);

	@Nonnull FerienbetreuungAngabenContainer antragAbschliessen(@Nonnull FerienbetreuungAngabenContainer container);

	@Nonnull FerienbetreuungAngabenContainer zurueckAnKanton(@Nonnull FerienbetreuungAngabenContainer container);

	Optional<FerienbetreuungAngabenContainer> findFerienbetreuungAngabenVorgaengerContainer(@Nonnull FerienbetreuungAngabenContainer container);

	byte[] generateFerienbetreuungReportDokument(@Nonnull FerienbetreuungAngabenContainer container, @Nonnull Sprache sprache)
			throws MergeDocException;
}
