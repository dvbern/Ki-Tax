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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;

import javax.annotation.Nonnull;
import javax.ejb.Stateless;
import javax.inject.Inject;

import ch.dvbern.ebegu.entities.AbstractPlatz;
import ch.dvbern.ebegu.entities.Benutzer;
import ch.dvbern.ebegu.entities.Betreuung;
import ch.dvbern.ebegu.entities.Institution;
import ch.dvbern.ebegu.entities.InstitutionExternalClient;
import ch.dvbern.ebegu.entities.Mandant;
import ch.dvbern.ebegu.errors.EbeguEntityNotFoundException;
import ch.dvbern.ebegu.inbox.handler.InstitutionExternalClients;
import ch.dvbern.ebegu.inbox.handler.Processing;
import ch.dvbern.ebegu.inbox.util.TechnicalUserConfigurationVisitor;
import ch.dvbern.ebegu.services.BenutzerService;
import ch.dvbern.ebegu.services.ExternalClientService;

import static ch.dvbern.ebegu.enums.ErrorCodeEnum.ERROR_ENTITY_NOT_FOUND;
import static org.apache.commons.lang3.StringUtils.EMPTY;

@Stateless
public class BetreuungEventHelper {

	private static final TechnicalUserConfigurationVisitor USER_CONFIG_VISITOR = new TechnicalUserConfigurationVisitor();

	@Inject
	private BenutzerService benutzerService;

	@Inject
	private ExternalClientService externalClientService;

	@Nonnull
	public Benutzer getMutationsmeldungBenutzer(Betreuung betreuung) {
		Mandant mandant = betreuung.extractGesuch().extractMandant();
		String technicalUserID = USER_CONFIG_VISITOR.process(mandant.getMandantIdentifier()).getBetreuungMitteilungUser();
		return benutzerService.findBenutzerById(technicalUserID)
			.orElseThrow(() -> new EbeguEntityNotFoundException(EMPTY, ERROR_ENTITY_NOT_FOUND, technicalUserID));
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
	public InstitutionExternalClients getExternalClients(
		@Nonnull String relevantClientName,
		@Nonnull AbstractPlatz platz) {

		Institution institution = platz.getInstitutionStammdaten().getInstitution();
		return getExternalClientForInstitution(relevantClientName, institution);
	}

	@Nonnull
	private InstitutionExternalClients getExternalClientForInstitution(
		@Nonnull String relevantClientName,
		@Nonnull Institution institution) {

		Collection<InstitutionExternalClient> institutionExternalClients =
			externalClientService.getInstitutionExternalClientForInstitution(institution);

		Optional<InstitutionExternalClient> relevantClient = institutionExternalClients.stream()
			.filter(iec -> iec.getExternalClient().getClientName().equals(relevantClientName))
			.findAny();

		ArrayList<InstitutionExternalClient> otherClients = new ArrayList<>(institutionExternalClients);
		relevantClient.ifPresent(otherClients::remove);

		return new InstitutionExternalClients(relevantClient.orElse(null), otherClients);
	}
}
