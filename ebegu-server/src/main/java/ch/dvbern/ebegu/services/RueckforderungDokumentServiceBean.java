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

package ch.dvbern.ebegu.services;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Optional;

import javax.annotation.Nonnull;
import javax.ejb.Local;
import javax.ejb.Stateless;
import javax.inject.Inject;

import ch.dvbern.ebegu.entities.RueckforderungDokument;
import ch.dvbern.lib.cdipersistence.Persistence;

@Stateless
@Local(RueckforderungDokumentService.class)
public class RueckforderungDokumentServiceBean extends AbstractBaseService implements RueckforderungDokumentService {

	@Inject
	private Persistence persistence;

	@Override
	@Nonnull
	public Optional<RueckforderungDokument> findDokument(@Nonnull String key) {
		Objects.requireNonNull(key, "id muss gesetzt sein");
		RueckforderungDokument doc = persistence.find(RueckforderungDokument.class, key);
		if (doc == null) {
			return Optional.empty();
		}
		return Optional.of(doc);
	}

	@Override
	public void removeDokument(@Nonnull RueckforderungDokument dokument) {
		persistence.remove(dokument);
	}

	@Nonnull
	@Override
	public RueckforderungDokument saveDokumentGrund(@Nonnull RueckforderungDokument rueckforderungDokument) {
		Objects.requireNonNull(rueckforderungDokument);

		rueckforderungDokument.setTimestampUpload(LocalDateTime.now());

		final RueckforderungDokument mergedRueckforderungDokument = persistence.merge(rueckforderungDokument);

		return mergedRueckforderungDokument;
	}
}
