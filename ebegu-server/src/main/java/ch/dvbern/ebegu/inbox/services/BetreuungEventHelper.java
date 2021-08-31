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

import java.util.Collection;
import java.util.Optional;

import javax.annotation.Nonnull;
import javax.ejb.Stateless;
import javax.inject.Inject;

import ch.dvbern.ebegu.entities.AbstractPlatz;
import ch.dvbern.ebegu.entities.Benutzer;
import ch.dvbern.ebegu.entities.Institution;
import ch.dvbern.ebegu.entities.InstitutionExternalClient;
import ch.dvbern.ebegu.errors.EbeguEntityNotFoundException;
import ch.dvbern.ebegu.inbox.handler.Processing;
import ch.dvbern.ebegu.services.BenutzerService;
import ch.dvbern.ebegu.services.ExternalClientService;

import static ch.dvbern.ebegu.enums.ErrorCodeEnum.ERROR_ENTITY_NOT_FOUND;
import static org.apache.commons.lang3.StringUtils.EMPTY;

@Stateless
public class BetreuungEventHelper {

	static final String TECHNICAL_BENUTZER_ID = "88888888-2222-2222-2222-222222222222";

	@Inject
	private BenutzerService benutzerService;

	@Inject
	private ExternalClientService externalClientService;

	@Nonnull
	public Benutzer getMutationsmeldungBenutzer() {
		return benutzerService.findBenutzerById(TECHNICAL_BENUTZER_ID)
			.orElseThrow(() -> new EbeguEntityNotFoundException(EMPTY, ERROR_ENTITY_NOT_FOUND, TECHNICAL_BENUTZER_ID));
	}

	@Nonnull
	public Processing clientNotFoundFailure(@Nonnull String clientName, @Nonnull AbstractPlatz platz) {
		Institution institution = platz.getInstitutionStammdaten().getInstitution();

		return clientNotFoundFailure(clientName, institution);
	}

	@Nonnull
	private Processing clientNotFoundFailure(@Nonnull String clientName, @Nonnull Institution institution) {
		return Processing.failure(String.format(
			"Kein InstitutionExternalClient Namens >>%s<< ist der Institution %s/%s zugewiesen",
			clientName,
			institution.getName(),
			institution.getId()));
	}

	@Nonnull
	public Optional<InstitutionExternalClient> getExternalClient(
		@Nonnull String clientName,
		@Nonnull AbstractPlatz platz) {

		Institution institution = platz.getInstitutionStammdaten().getInstitution();
		return getExternalClientForInstitution(clientName, institution);
	}

	@Nonnull
	private Optional<InstitutionExternalClient> getExternalClientForInstitution(
		@Nonnull String clientName,
		@Nonnull Institution institution) {

		Collection<InstitutionExternalClient> institutionExternalClients =
			externalClientService.getInstitutionExternalClientForInstitution(institution);

		return institutionExternalClients.stream()
			.filter(iec -> iec.getExternalClient().getClientName().equals(clientName))
			.findAny();
	}
}
