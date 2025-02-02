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

import ch.dvbern.ebegu.entities.gemeindeantrag.FerienbetreuungDokument;
import ch.dvbern.ebegu.enums.Sprache;

/**
 * Service fuer die Dokumente Ferienbetreuungen
 */
public interface FerienbetreuungDokumentService {

	@Nonnull FerienbetreuungDokument saveDokument(@Nonnull FerienbetreuungDokument ferienbetreuungDokument);

	@Nonnull Optional<FerienbetreuungDokument> findDokument(@Nonnull String dokumentId);

	void removeDokument(@Nonnull FerienbetreuungDokument dokument);

	@Nonnull List<FerienbetreuungDokument> findDokumente(@Nonnull String ferienbetreuungContainerId);

	byte[] createDocx(@Nonnull String containerId, @Nonnull Sprache sprache);
}
