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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package ch.dvbern.ebegu.api.resource;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import ch.dvbern.ebegu.api.converter.JaxBConverter;
import ch.dvbern.ebegu.api.dtos.JaxId;
import ch.dvbern.ebegu.api.dtos.JaxRueckforderungDokument;
import ch.dvbern.ebegu.api.dtos.JaxRueckforderungFormular;
import ch.dvbern.ebegu.api.dtos.JaxRueckforderungMitteilung;
import ch.dvbern.ebegu.api.dtos.JaxRueckforderungMitteilungRequestParams;
import ch.dvbern.ebegu.authentication.PrincipalBean;
import ch.dvbern.ebegu.entities.RueckforderungDokument;
import ch.dvbern.ebegu.entities.RueckforderungFormular;
import ch.dvbern.ebegu.entities.RueckforderungMitteilung;
import ch.dvbern.ebegu.enums.ErrorCodeEnum;
import ch.dvbern.ebegu.enums.RueckforderungStatus;
import ch.dvbern.ebegu.enums.UserRole;
import ch.dvbern.ebegu.errors.EbeguEntityNotFoundException;
import ch.dvbern.ebegu.errors.EbeguRuntimeException;
import ch.dvbern.ebegu.services.RueckforderungDokumentService;
import ch.dvbern.ebegu.services.RueckforderungFormularService;
import ch.dvbern.ebegu.services.RueckforderungMitteilungService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

import static java.util.Objects.requireNonNull;

@Path("notrecht")
@Stateless
@Api(description = "Resource zum Verwalten von Rueckforderungsformularen für das Notrecht")
public class NotrechtResource {

	@Inject
	private RueckforderungFormularService rueckforderungFormularService;

	@Inject
	private RueckforderungMitteilungService rueckforderungMitteilungService;

	@Inject
	private JaxBConverter converter;

	@Inject
	private PrincipalBean principalBean;

	@Inject
	private RueckforderungDokumentService rueckforderungDokumentService;

	@ApiOperation(value = "Erstellt leere Rückforderungsformulare für alle Kitas & TFOs die in kiBon existieren "
		+ "und bisher kein Rückforderungsformular haben", responseContainer = "List", response =
		JaxRueckforderungFormular.class)
	@Nullable
	@POST
	@Path("/initialize")
	@Consumes(MediaType.WILDCARD)
	@Produces(MediaType.APPLICATION_JSON)
	public List<JaxRueckforderungFormular> initializeRueckforderungFormulare() {

		List<RueckforderungFormular> createdFormulare =
			rueckforderungFormularService.initializeRueckforderungFormulare();

		return converter.rueckforderungFormularListToJax(createdFormulare);
	}

	@ApiOperation(value = "Gibt alle Rückforderungsformulare zurück, die der aktuelle Benutzer lesen kann",
		responseContainer = "List", response = JaxRueckforderungFormular.class)
	@GET
	@Path("/currentuser")
	@Consumes(MediaType.WILDCARD)
	@Produces(MediaType.APPLICATION_JSON)
	public List<JaxRueckforderungFormular> getRueckforderungFormulareForCurrentUser() {

		List<RueckforderungFormular> formulareCurrentBenutzer =
			rueckforderungFormularService.getRueckforderungFormulareForCurrentBenutzer();

		return converter.rueckforderungFormularListToJax(formulareCurrentBenutzer);
	}

	@ApiOperation(value = "Gibt zurück, ob der aktuelle eingeloggte Benutzer mindestens ein Rückforderungformular "
		+ "sehen kann",
		responseContainer = "List", response = JaxRueckforderungFormular.class)
	@GET
	@Path("/currentuser/hasformular")
	@Consumes(MediaType.WILDCARD)
	@Produces(MediaType.APPLICATION_JSON)
	public boolean currentUserHasFormular() {

		List<RueckforderungFormular> formulareCurrentBenutzer =
			rueckforderungFormularService.getRueckforderungFormulareForCurrentBenutzer();

		return formulareCurrentBenutzer.size() > 0;
	}

	@ApiOperation(value = "Updates a RueckforderungFormular in the database", response =
		JaxRueckforderungFormular.class)
	@Nullable
	@PUT
	@Path("/update")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public JaxRueckforderungFormular update(
		@Nonnull @NotNull JaxRueckforderungFormular rueckforderungFormularJAXP,
		@Context UriInfo uriInfo,
		@Context HttpServletResponse response) {

		Objects.requireNonNull(rueckforderungFormularJAXP.getId());

		RueckforderungFormular rueckforderungFormularFromDB =
			rueckforderungFormularService.findRueckforderungFormular(rueckforderungFormularJAXP.getId())
				.orElseThrow(() -> new EbeguEntityNotFoundException("update", ErrorCodeEnum.ERROR_ENTITY_NOT_FOUND,
					rueckforderungFormularJAXP.getId()));

		RueckforderungFormular rueckforderungFormularToMerge =
			converter.rueckforderungFormularToEntity(rueckforderungFormularJAXP,
				rueckforderungFormularFromDB);

		if (!checkStatusErlaubtFuerRole(rueckforderungFormularToMerge)) {
			throw new EbeguRuntimeException("update", "Action not allowed for this user");
		}

		RueckforderungFormular modifiedRueckforderungFormular =
			this.rueckforderungFormularService.save(rueckforderungFormularToMerge);

		return converter.rueckforderungFormularToJax(modifiedRueckforderungFormular);
	}

	@ApiOperation(value = "Updates a RueckforderungFormular in the database", response =
		JaxRueckforderungFormular.class)
	@Nullable
	@PUT
	@Path("/updateWithStatusChange")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public JaxRueckforderungFormular updateAndChangeStatusIfNecessary(
		@Nonnull @NotNull JaxRueckforderungFormular rueckforderungFormularJAXP,
		@Context UriInfo uriInfo,
		@Context HttpServletResponse response) {
		Objects.requireNonNull(rueckforderungFormularJAXP.getId());

		RueckforderungFormular rueckforderungFormularFromDB =
			rueckforderungFormularService.findRueckforderungFormular(rueckforderungFormularJAXP.getId())
				.orElseThrow(() -> new EbeguEntityNotFoundException("updateAndChangeStatusIfNecessary", ErrorCodeEnum.ERROR_ENTITY_NOT_FOUND,
					rueckforderungFormularJAXP.getId()));

		RueckforderungFormular rueckforderungFormularToMerge =
			converter.rueckforderungFormularToEntity(rueckforderungFormularJAXP,
				rueckforderungFormularFromDB);

		if (!checkStatusErlaubtFuerRole(rueckforderungFormularToMerge)) {
			throw new EbeguRuntimeException("updateAndChangeStatusIfNecessary", "Action not allowed for this user");
		}

		rueckforderungFormularToMerge = this.rueckforderungFormularService.save(rueckforderungFormularToMerge);
		RueckforderungFormular modifiedRueckforderungFormular =
			this.rueckforderungFormularService.saveAndChangeStatusIfNecessary(rueckforderungFormularToMerge);

		return converter.rueckforderungFormularToJax(modifiedRueckforderungFormular);
	}

	@ApiOperation(value = "Sucht den Benutzer mit dem uebergebenen Username in der Datenbank.",
		response = JaxRueckforderungFormular.class)
	@Nullable
	@GET
	@Path("/{rueckforderungFormId}")
	@Consumes(MediaType.WILDCARD)
	@Produces(MediaType.APPLICATION_JSON)
	public JaxRueckforderungFormular findRueckforderungFormular(
		@Nonnull @NotNull @PathParam("rueckforderungFormId") JaxId rueckforderungFormJaxId) {

		Objects.requireNonNull(rueckforderungFormJaxId.getId());
		String rueckforderungFormId = converter.toEntityId(rueckforderungFormJaxId);
		Optional<RueckforderungFormular> rueckforderungFormularOptional =
			rueckforderungFormularService.findRueckforderungFormular(rueckforderungFormId);

		if (!rueckforderungFormularOptional.isPresent()) {
			return null;
		}
		RueckforderungFormular rueckforderungFormularToReturn = rueckforderungFormularOptional.get();
		final JaxRueckforderungFormular jaxRueckforderungFormular =
			converter.rueckforderungFormularToJax(rueckforderungFormularToReturn);
		return jaxRueckforderungFormular;
	}

	private boolean checkStatusErlaubtFuerRole(@Nonnull RueckforderungFormular rueckforderungFormular) {
		if (principalBean.isCallerInAnyOfRole(UserRole.getInstitutionTraegerschaftRoles())
			&& RueckforderungStatus.isStatusForInstitutionAuthorized(rueckforderungFormular.getStatus())) {
			return true;
		}
		return principalBean.isCallerInAnyOfRole(UserRole.SACHBEARBEITER_MANDANT, UserRole.ADMIN_MANDANT,
			UserRole.SUPER_ADMIN)
			&& RueckforderungStatus.isStatusForKantonAuthorized(rueckforderungFormular.getStatus());
	}

	@ApiOperation("Sendet eine Nachricht an alle Besitzer von Rückforderungsformularen mit gewünschtem Status")
	@POST
	@Path("/mitteilung")
	@Consumes(MediaType.WILDCARD)
	public void sendMessage(
		@Nonnull @NotNull JaxRueckforderungMitteilungRequestParams data,
		@Context UriInfo uriInfo,
		@Context HttpServletResponse response
	) {
		final JaxRueckforderungMitteilung jaxRueckforderungMitteilung = data.getMitteilung();
		List<RueckforderungStatus> statusList = data.getStatusList();
		RueckforderungMitteilung rueckforderungMitteilung =
			converter.rueckforderungMitteilungToEntity(jaxRueckforderungMitteilung, new RueckforderungMitteilung());
		rueckforderungMitteilungService.sendMitteilung(rueckforderungMitteilung, statusList);
	}

	@ApiOperation("Sendet eine Nachricht an alle Besitzer von Rückforderungsformularen mit gewünschtem Status")
	@POST
	@Path("/einladung")
	@Consumes(MediaType.WILDCARD)
	public void sendEinladung(
		@Nonnull @NotNull JaxRueckforderungMitteilung jaxRueckforderungMitteilung,
		@Context UriInfo uriInfo,
		@Context HttpServletResponse response) {
		RueckforderungMitteilung rueckforderungMitteilung =
			converter.rueckforderungMitteilungToEntity(jaxRueckforderungMitteilung, new RueckforderungMitteilung());
		rueckforderungMitteilungService.sendEinladung(rueckforderungMitteilung);
	}

	@ApiOperation("Aktiviert der Phase 2 und setzt die entsprechende Status fuer die Ruckforderungsformular "
		+ "die schon mit Phase 1 durch sind")
	@Nullable
	@POST
	@Path("/initializePhase2")
	public Response initializePhase2() {
		rueckforderungFormularService.initializePhase2();
		return Response.ok().build();
	}

	@ApiOperation("Setzt den Status des Formulars zurueck auf 'In Bearbeitung Institution Phase 2'")
	@Nonnull
	@POST
	@Path("/resetStatus")
	@Consumes(MediaType.WILDCARD)
	@Produces(MediaType.APPLICATION_JSON)
	public JaxRueckforderungFormular resetStatusToInBearbeitungInstitutionPhase2(
		@Nonnull @NotNull String formularId,
		@Context UriInfo uriInfo,
		@Context HttpServletResponse response
	) {
		Objects.requireNonNull(formularId);
		RueckforderungFormular formular = rueckforderungFormularService.resetStatusToInBearbeitungInstitutionPhase2(formularId);
		return converter.rueckforderungFormularToJax(formular);
	}

	@ApiOperation(value = "Gibt alle Rückforderungsdokumente zurück, die die aktuelle RueckforderungForm",
		responseContainer = "List", response = JaxRueckforderungFormular.class)
	@GET
	@Path("/dokumente/{rueckforderungFormId}")
	@Consumes(MediaType.WILDCARD)
	@Produces(MediaType.APPLICATION_JSON)
	public List<JaxRueckforderungDokument> getRueckforderungFormulareDokumente(
		@Nonnull @NotNull @PathParam("rueckforderungFormId") JaxId rueckforderungFormJaxId
	) {
		Objects.requireNonNull(rueckforderungFormJaxId.getId());
		String rueckforderungFormId = converter.toEntityId(rueckforderungFormJaxId);
		List<RueckforderungDokument> rueckforderungDokumente =
			rueckforderungDokumentService.findDokumente(rueckforderungFormId);

		return converter.rueckforderungDokumentListToJax(rueckforderungDokumente);
	}

	@ApiOperation("Loescht das Dokument mit der uebergebenen Id in der Datenbank")
	@Nullable
	@DELETE
	@Path("/{rueckforderungDokumentId}")
	@Consumes(MediaType.WILDCARD)
	public Response removeRueckforderungDokument(
		@Nonnull @NotNull @PathParam("rueckforderungDokumentId") JaxId rueckforderungDokumentJAXPId,
		@Context HttpServletResponse response) {

		requireNonNull(rueckforderungDokumentJAXPId.getId());
		String dokumentId = converter.toEntityId(rueckforderungDokumentJAXPId);

		RueckforderungDokument rueckforderungDokument =
			rueckforderungDokumentService.findDokument(dokumentId).orElseThrow(() -> new EbeguEntityNotFoundException(
			"removeRueckforderungDokument",
			ErrorCodeEnum.ERROR_ENTITY_NOT_FOUND, dokumentId));

		rueckforderungDokumentService.removeDokument(rueckforderungDokument);

		return Response.ok().build();
	}
}
