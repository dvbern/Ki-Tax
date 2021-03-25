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

import java.util.List;
import java.util.Optional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import ch.dvbern.ebegu.entities.Gemeinde;
import ch.dvbern.ebegu.entities.Gesuchsperiode;
import ch.dvbern.ebegu.entities.gemeindeantrag.FerienbetreuungAngabenAngebot;
import ch.dvbern.ebegu.entities.gemeindeantrag.FerienbetreuungAngabenContainer;
import ch.dvbern.ebegu.entities.gemeindeantrag.FerienbetreuungAngabenKostenEinnahmen;
import ch.dvbern.ebegu.entities.gemeindeantrag.FerienbetreuungAngabenNutzung;
import ch.dvbern.ebegu.entities.gemeindeantrag.FerienbetreuungAngabenStammdaten;

/**
 * Service fuer die Ferienbetreuungen
 */
public interface FerienbetreuungService {

	@Nonnull
	List<FerienbetreuungAngabenContainer> getFerienbetreuungAntraege(
		@Nullable String gemeinde,
		@Nullable String periode,
		@Nullable String status
	);

	@Nonnull
	Optional<FerienbetreuungAngabenContainer> findFerienbetreuungAngabenContainer(@Nonnull String containerId);

	@Nonnull
	FerienbetreuungAngabenContainer createFerienbetreuungAntrag(@Nonnull Gemeinde gemeinde, @Nonnull Gesuchsperiode gesuchsperiode);

	@Nonnull
	void saveKommentar(@Nonnull String id, @Nonnull String kommentar);

	@Nonnull
	Optional<FerienbetreuungAngabenStammdaten> findFerienbetreuungAngabenStammdaten(@Nonnull String stammdatenId);

	@Nonnull
	Optional<FerienbetreuungAngabenAngebot> findFerienbetreuungAngabenAngebot(@Nonnull String angebotId);

	@Nonnull
	Optional<FerienbetreuungAngabenNutzung> findFerienbetreuungAngabenNutzung(@Nonnull String nutzungId);

	@Nonnull
	Optional<FerienbetreuungAngabenKostenEinnahmen> findFerienbetreuungAngabenKostenEinnahmen(@Nonnull String kostenEinnahmenId);

	@Nonnull
	FerienbetreuungAngabenStammdaten saveFerienbetreuungAngabenStammdaten(@Nonnull FerienbetreuungAngabenStammdaten stammdaten);

	@Nonnull
	FerienbetreuungAngabenAngebot saveFerienbetreuungAngabenAngebot(@Nonnull FerienbetreuungAngabenAngebot angebot);

	@Nonnull
	FerienbetreuungAngabenNutzung saveFerienbetreuungAngabenNutzung(@Nonnull FerienbetreuungAngabenNutzung nutzung);

	@Nonnull
	FerienbetreuungAngabenKostenEinnahmen saveFerienbetreuungAngabenKostenEinnahmen(@Nonnull FerienbetreuungAngabenKostenEinnahmen kostenEinnahmen);

	@Nonnull
	FerienbetreuungAngabenAngebot ferienbetreuungAngebotAbschliessen(@Nonnull FerienbetreuungAngabenAngebot angebot);

	@Nonnull
	FerienbetreuungAngabenAngebot ferienbetreuungAngebotFalscheAngaben(@Nonnull FerienbetreuungAngabenAngebot angebot);
}
