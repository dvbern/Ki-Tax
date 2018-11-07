/*
 * Copyright (C) 2018 DV Bern AG, Switzerland
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
package ch.dvbern.ebegu.api.converter;

import java.util.Optional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;

import ch.dvbern.ebegu.api.dtos.JaxPensumFachstelle;
import ch.dvbern.ebegu.entities.Fachstelle;
import ch.dvbern.ebegu.entities.PensumFachstelle;
import ch.dvbern.ebegu.enums.ErrorCodeEnum;
import ch.dvbern.ebegu.errors.EbeguEntityNotFoundException;
import ch.dvbern.ebegu.services.FachstelleService;
import ch.dvbern.ebegu.services.PensumFachstelleService;

import static java.util.Objects.requireNonNull;

@RequestScoped
public class PensumFachstelleJaxBConverter extends AbstractConverter {

	@Inject
	private JaxBConverter converter;
	@Inject
	private PensumFachstelleService pensumFachstelleService;
	@Inject
	private FachstelleService fachstelleService;


	@Nullable
	public JaxPensumFachstelle pensumFachstelleToJax(@Nullable final PensumFachstelle persistedPensumFachstelle) {
		if (persistedPensumFachstelle == null) {
			return null;
		}
		final JaxPensumFachstelle jaxPensumFachstelle = new JaxPensumFachstelle();
		convertAbstractPensumFieldsToJAX(persistedPensumFachstelle, jaxPensumFachstelle);
		jaxPensumFachstelle.setFachstelle(converter.fachstelleToJAX(persistedPensumFachstelle.getFachstelle()));
		jaxPensumFachstelle.setIntegrationTyp(persistedPensumFachstelle.getIntegrationTyp());
		return jaxPensumFachstelle;
	}

	public PensumFachstelle pensumFachstelleToEntity(
		final JaxPensumFachstelle pensumFachstelleJAXP,
		final PensumFachstelle pensumFachstelle
	) {
		requireNonNull(pensumFachstelleJAXP.getFachstelle(), "Fachstelle muss existieren");
		requireNonNull(
			pensumFachstelleJAXP.getFachstelle().getId(),
			"Fachstelle muss bereits gespeichert sein");
		convertAbstractPensumFieldsToEntity(pensumFachstelleJAXP, pensumFachstelle);

		final Optional<Fachstelle> fachstelleFromDB =
			fachstelleService.findFachstelle(pensumFachstelleJAXP.getFachstelle().getId());
		if (fachstelleFromDB.isPresent()) {
			// Fachstelle darf nicht vom Client ueberschrieben werden
			pensumFachstelle.setFachstelle(fachstelleFromDB.get());
		} else {
			throw new EbeguEntityNotFoundException(
				"pensumFachstelleToEntity",
				ErrorCodeEnum.ERROR_ENTITY_NOT_FOUND,
				pensumFachstelleJAXP.getFachstelle()
					.getId());
		}
		pensumFachstelle.setIntegrationTyp(pensumFachstelleJAXP.getIntegrationTyp());

		return pensumFachstelle;
	}

	public PensumFachstelle toStorablePensumFachstelle(@Nonnull final JaxPensumFachstelle pensumFsToSave) {
		PensumFachstelle pensumToMergeWith = new PensumFachstelle();
		if (pensumFsToSave.getId() != null) {
			final Optional<PensumFachstelle> pensumFachstelleOpt =
				pensumFachstelleService.findPensumFachstelle(pensumFsToSave.getId());
			if (pensumFachstelleOpt.isPresent()) {
				pensumToMergeWith = pensumFachstelleOpt.get();
			} else {
				throw new EbeguEntityNotFoundException(
					"toStorablePensumFachstelle",
					ErrorCodeEnum.ERROR_ENTITY_NOT_FOUND,
					pensumFsToSave.getId());
			}
		}
		return pensumFachstelleToEntity(pensumFsToSave, pensumToMergeWith);
	}
}
