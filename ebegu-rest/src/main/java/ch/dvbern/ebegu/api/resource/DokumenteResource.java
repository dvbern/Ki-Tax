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
import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.security.PermitAll;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import ch.dvbern.ebegu.api.converter.JaxBConverter;
import ch.dvbern.ebegu.api.dtos.JaxDokumentGrund;
import ch.dvbern.ebegu.api.dtos.JaxDokumente;
import ch.dvbern.ebegu.api.dtos.JaxId;
import ch.dvbern.ebegu.entities.Dokument;
import ch.dvbern.ebegu.entities.DokumentGrund;
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.enums.DokumentGrundTyp;
import ch.dvbern.ebegu.errors.EbeguEntityNotFoundException;
import ch.dvbern.ebegu.i18n.LocaleThreadLocal;
import ch.dvbern.ebegu.rules.anlageverzeichnis.DokumentenverzeichnisEvaluator;
import ch.dvbern.ebegu.services.DokumentGrundService;
import ch.dvbern.ebegu.services.DokumentService;
import ch.dvbern.ebegu.services.GesuchService;
import ch.dvbern.ebegu.util.DokumenteUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

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
		@Nonnull @NotNull @Valid @PathParam("gesuchId") JaxId gesuchId) {

		Gesuch gesuch = gesuchService.findGesuch(gesuchId.getId())
			.orElseThrow(() -> new EbeguEntityNotFoundException("getDokumente", gesuchId.getId()));

		Set<DokumentGrund> dokumentGrundsNeeded = dokumentenverzeichnisEvaluator.calculate(gesuch, LocaleThreadLocal.get());
		dokumentenverzeichnisEvaluator.addOptionalDokumentGruende(dokumentGrundsNeeded);

		Collection<DokumentGrund> persisted = dokumentGrundService.findAllDokumentGrundByGesuch(gesuch);

		Set<DokumentGrund> merged = DokumenteUtil.mergeNeededAndPersisted(dokumentGrundsNeeded, persisted);

		return converter.dokumentGruendeToJAX(merged);
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

		Gesuch gesuch = gesuchService.findGesuch(gesuchId.getId())
			.orElseThrow(() -> new EbeguEntityNotFoundException("getDokumenteByTyp", gesuchId.getId()));

		Set<DokumentGrund> dokumentGrundsNeeded = new HashSet<>();

		dokumentenverzeichnisEvaluator.addOptionalDokumentGruendeByType(dokumentGrundsNeeded, dokumentGrundTyp);

		Collection<DokumentGrund> persisted =
			dokumentGrundService.findAllDokumentGrundByGesuchAndDokumentType(gesuch, dokumentGrundTyp);

		Set<DokumentGrund> merged = DokumenteUtil.mergeNeededAndPersisted(dokumentGrundsNeeded, persisted);

		return converter.dokumentGruendeToJAX(merged);
	}

	@ApiOperation("Loescht das Dokument mit der uebergebenen Id in der Datenbank")
	@Nullable
	@DELETE
	@Path("/{dokumentId}")
	@Consumes(MediaType.WILDCARD)
	public JaxDokumentGrund removeDokument(@Nonnull @NotNull @Valid @PathParam("dokumentId") JaxId dokumentJAXPId) {
		String dokumentId = converter.toEntityId(dokumentJAXPId);

		Dokument dokument = dokumentService.findDokument(dokumentId)
			.orElseThrow(() -> new EbeguEntityNotFoundException("removeDokument", dokumentId));

		String grundId = dokument.getDokumentGrund().getId();
		DokumentGrund dokumentGrund = dokumentGrundService.findDokumentGrund(grundId)
			.orElseThrow(() -> new EbeguEntityNotFoundException("findDokumentGrund_loadDokumentGrund", grundId));

		dokumentGrund.getDokumente().remove(dokument);
		dokumentService.removeDokument(dokument);

		return converter.dokumentGrundToJax(dokumentGrund);
	}
}
