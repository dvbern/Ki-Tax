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

import java.util.Objects;
import java.util.Optional;

import javax.annotation.Nonnull;
import javax.ejb.Local;
import javax.ejb.Stateless;
import javax.inject.Inject;

import ch.dvbern.ebegu.entities.gemeindeantrag.FerienbetreuungDokument;
import ch.dvbern.ebegu.services.AbstractBaseService;
import ch.dvbern.ebegu.services.Authorizer;
import ch.dvbern.lib.cdipersistence.Persistence;

/**
 * Service fuer die Dokumente der Ferienbetreuungen
 */
@Stateless
@Local(FerienbetreuungDokumentService.class)
public class FerienbetreuungDokumentServiceBean extends AbstractBaseService
	implements FerienbetreuungDokumentService {

	public static final String ID_MUSS_GESETZT_SEIN = "id muss gesetzt sein";

	@Inject
	private Persistence persistence;

	@Inject
	private Authorizer authorizer;

	@Nonnull
	@Override
	public FerienbetreuungDokument saveDokument(@Nonnull FerienbetreuungDokument ferienbetreuungDokument) {
		Objects.requireNonNull(ferienbetreuungDokument);
		authorizer.checkWriteAuthorization(ferienbetreuungDokument.getFerienbetreuungAngabenContainer());

		return persistence.merge(ferienbetreuungDokument);
	}

	@Nonnull
	@Override
	public Optional<FerienbetreuungDokument> findDokument(@Nonnull String dokumentId) {
		Objects.requireNonNull(dokumentId, ID_MUSS_GESETZT_SEIN);
		FerienbetreuungDokument dokument = persistence.find(FerienbetreuungDokument.class, dokumentId);
		authorizer.checkReadAuthorization(dokument.getFerienbetreuungAngabenContainer());
		return Optional.ofNullable(dokument);
	}

	@Override
	public void removeDokument(@Nonnull FerienbetreuungDokument dokument) {
		authorizer.checkWriteAuthorization(dokument.getFerienbetreuungAngabenContainer());
		persistence.remove(dokument);
	}
}


