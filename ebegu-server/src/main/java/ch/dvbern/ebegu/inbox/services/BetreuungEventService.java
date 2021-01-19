/*
 * Copyright (C) 2021 DV Bern AG, Switzerland
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

package ch.dvbern.ebegu.inbox.services;

import javax.annotation.Nonnull;
import javax.ejb.Stateless;
import javax.inject.Inject;

import ch.dvbern.ebegu.entities.Benutzer;
import ch.dvbern.ebegu.errors.EbeguEntityNotFoundException;
import ch.dvbern.ebegu.services.BenutzerService;

import static ch.dvbern.ebegu.enums.ErrorCodeEnum.ERROR_ENTITY_NOT_FOUND;
import static org.apache.commons.lang3.StringUtils.EMPTY;

@Stateless
public class BetreuungEventService {

	static final String TECHNICAL_BENUTZER_ID = "88888888-2222-2222-2222-222222222222";

	@Inject
	private BenutzerService benutzerService;


	@Nonnull
	public Benutzer getMutationsmeldungBenutzer() {
		return benutzerService.findBenutzerById(TECHNICAL_BENUTZER_ID)
			.orElseThrow(() -> new EbeguEntityNotFoundException(EMPTY, ERROR_ENTITY_NOT_FOUND, TECHNICAL_BENUTZER_ID));
	}

}
