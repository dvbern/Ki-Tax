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

package ch.dvbern.ebegu.api.resource;

import java.util.Collection;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.security.PermitAll;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import ch.dvbern.ebegu.api.converter.JaxBConverter;
import ch.dvbern.ebegu.api.dtos.JaxDokumentGrund;
import ch.dvbern.ebegu.api.dtos.JaxDokumente;
import ch.dvbern.ebegu.api.dtos.JaxId;
import ch.dvbern.ebegu.entities.Dokument;
import ch.dvbern.ebegu.entities.DokumentGrund;
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.enums.DokumentGrundTyp;
import ch.dvbern.ebegu.enums.ErrorCodeEnum;
import ch.dvbern.ebegu.errors.EbeguEntityNotFoundException;
import ch.dvbern.ebegu.i18n.LocaleThreadLocal;
import ch.dvbern.ebegu.rules.anlageverzeichnis.DokumentenverzeichnisEvaluator;
import ch.dvbern.ebegu.services.DokumentGrundService;
import ch.dvbern.ebegu.services.DokumentService;
import ch.dvbern.ebegu.services.GesuchService;
import ch.dvbern.ebegu.util.DokumenteUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

import static java.util.Objects.requireNonNull;

/**
 * REST Resource fuer Dokumente
 */
@Path("dokumente")
@Stateless
@Api(description = "Resource für die Verwaltung von Dokumenten")
@PermitAll // Grundsaetzliche fuer alle Rollen: Datenabhaengig. -> Authorizer
public class DokumenteResource {

	@SuppressWarnings("CdiInjectionPointsInspection")
	@Inject
	private JaxBConverter converter;

	@Inject
	private GesuchService gesuchService;

	@Inject
	private DokumentenverzeichnisEvaluator dokumentenverzeichnisEvaluator;

	@Inject
	private DokumentGrundService dokumentGrundService;

	@Inject
	private DokumentService dokumentService;


	@ApiOperation(value = "Gibt alle Dokumentgruende zurück, welche zum uebergebenen Gesuch vorhanden sind.",
		response = JaxDokumente.class)
	@Nullable
	@GET
	@Path("/{gesuchId}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public JaxDokumente getDokumente(
		@Nonnull @NotNull @PathParam("gesuchId") JaxId gesuchId) {

		Optional<Gesuch> gesuch = gesuchService.findGesuch(gesuchId.getId());
		if (gesuch.isPresent()) {
			final Set<DokumentGrund> dokumentGrundsNeeded = dokumentenverzeichnisEvaluator
				.calculate(gesuch.get(), LocaleThreadLocal.get());
			dokumentenverzeichnisEvaluator.addOptionalDokumentGruende(dokumentGrundsNeeded);
			final Collection<DokumentGrund> persistedDokumentGrund = dokumentGrundService.findAllDokumentGrundByGesuch(gesuch.get());
			final Set<DokumentGrund> dokumentGrundsMerged = DokumenteUtil.mergeNeededAndPersisted(dokumentGrundsNeeded, persistedDokumentGrund);
			return converter.dokumentGruendeToJAX(dokumentGrundsMerged);
		}
		throw new EbeguEntityNotFoundException("getDokumente", ErrorCodeEnum.ERROR_ENTITY_NOT_FOUND, "GesuchId invalid: " + gesuchId.getId());
	}

	@ApiOperation(value = "Gibt alle Dokumentegruende eines bestimmten Typs zurück, die zu einem Gesuch vorhanden sind",
		response = JaxDokumente.class)
	@Nullable
	@GET
	@Path("/byTyp/{gesuchId}/{dokumentGrundTyp}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public JaxDokumente getDokumenteByTyp(
		@Nonnull @NotNull @PathParam("gesuchId") JaxId gesuchId,
		@Nonnull @NotNull @PathParam("dokumentGrundTyp") DokumentGrundTyp dokumentGrundTyp) {

		Optional<Gesuch> gesuch = gesuchService.findGesuch(gesuchId.getId());
		if (gesuch.isPresent()) {
			final Set<DokumentGrund> dokumentGrundsNeeded = new HashSet<>();

			dokumentenverzeichnisEvaluator.addOptionalDokumentGruendeByType(dokumentGrundsNeeded, dokumentGrundTyp);

			Collection<DokumentGrund> persistedDokumentGrund = dokumentGrundService
				.findAllDokumentGrundByGesuchAndDokumentType(gesuch.get(), dokumentGrundTyp);

			final Set<DokumentGrund> dokumentGrundsMerged = DokumenteUtil
				.mergeNeededAndPersisted(dokumentGrundsNeeded, persistedDokumentGrund);

			return converter.dokumentGruendeToJAX(dokumentGrundsMerged);
		}
		throw new EbeguEntityNotFoundException("getDokumenteByTyp", ErrorCodeEnum.ERROR_ENTITY_NOT_FOUND, "GesuchId invalid: " + gesuchId.getId());
	}

	@ApiOperation("Loescht das Dokument mit der uebergebenen Id in der Datenbank")
	@Nullable
	@DELETE
	@Path("/{dokumentId}")
	@Consumes(MediaType.WILDCARD)
	public JaxDokumentGrund removeDokument(
		@Nonnull @NotNull @PathParam("dokumentId") JaxId dokumentJAXPId,
		@Context HttpServletResponse response) {

		requireNonNull(dokumentJAXPId.getId());
		String dokumentId = converter.toEntityId(dokumentJAXPId);

		Dokument dokument = dokumentService.findDokument(dokumentId).orElseThrow(() -> new EbeguEntityNotFoundException("removeDokument",
			ErrorCodeEnum.ERROR_ENTITY_NOT_FOUND, dokumentId));

		DokumentGrund dokumentGrund = dokumentGrundService.findDokumentGrund(dokument.getDokumentGrund().getId())
			.orElseThrow(() ->
				new EbeguEntityNotFoundException(
					"findDokumentGrund_loadDokumentGrund",
					ErrorCodeEnum.ERROR_ENTITY_NOT_FOUND,
					dokument.getDokumentGrund().getId()
				));

		dokumentGrund.getDokumente().remove(dokument);
		dokumentService.removeDokument(dokument);

		return converter.dokumentGrundToJax(dokumentGrund);
	}
}
