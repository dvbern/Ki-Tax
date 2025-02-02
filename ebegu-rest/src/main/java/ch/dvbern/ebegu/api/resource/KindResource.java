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
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.security.DenyAll;
import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
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
import ch.dvbern.ebegu.api.dtos.JaxKindContainer;
import ch.dvbern.ebegu.api.resource.util.ResourceHelper;
import ch.dvbern.ebegu.api.util.RestUtil;
import ch.dvbern.ebegu.authentication.PrincipalBean;
import ch.dvbern.ebegu.dto.KindDubletteDTO;
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.entities.Institution;
import ch.dvbern.ebegu.entities.KindContainer;
import ch.dvbern.ebegu.enums.AntragCopyType;
import ch.dvbern.ebegu.enums.ErrorCodeEnum;
import ch.dvbern.ebegu.errors.EbeguEntityNotFoundException;
import ch.dvbern.ebegu.services.GesuchService;
import ch.dvbern.ebegu.services.InstitutionService;
import ch.dvbern.ebegu.services.KindService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

import static ch.dvbern.ebegu.enums.UserRoleName.ADMIN_BG;
import static ch.dvbern.ebegu.enums.UserRoleName.ADMIN_GEMEINDE;
import static ch.dvbern.ebegu.enums.UserRoleName.ADMIN_INSTITUTION;
import static ch.dvbern.ebegu.enums.UserRoleName.ADMIN_MANDANT;
import static ch.dvbern.ebegu.enums.UserRoleName.ADMIN_SOZIALDIENST;
import static ch.dvbern.ebegu.enums.UserRoleName.ADMIN_TRAEGERSCHAFT;
import static ch.dvbern.ebegu.enums.UserRoleName.ADMIN_TS;
import static ch.dvbern.ebegu.enums.UserRoleName.GESUCHSTELLER;
import static ch.dvbern.ebegu.enums.UserRoleName.JURIST;
import static ch.dvbern.ebegu.enums.UserRoleName.REVISOR;
import static ch.dvbern.ebegu.enums.UserRoleName.SACHBEARBEITER_BG;
import static ch.dvbern.ebegu.enums.UserRoleName.SACHBEARBEITER_GEMEINDE;
import static ch.dvbern.ebegu.enums.UserRoleName.SACHBEARBEITER_INSTITUTION;
import static ch.dvbern.ebegu.enums.UserRoleName.SACHBEARBEITER_MANDANT;
import static ch.dvbern.ebegu.enums.UserRoleName.SACHBEARBEITER_SOZIALDIENST;
import static ch.dvbern.ebegu.enums.UserRoleName.SACHBEARBEITER_TRAEGERSCHAFT;
import static ch.dvbern.ebegu.enums.UserRoleName.SACHBEARBEITER_TS;
import static ch.dvbern.ebegu.enums.UserRoleName.SUPER_ADMIN;

/**
 * REST Resource fuer Kinder
 */
@Path("kinder")
@Stateless
@Api(description = "Resource zum Verwalten von Kindern eines Gesuchstellers")
@DenyAll // Absichtlich keine Rolle zugelassen, erzwingt, dass es für neue Methoden definiert werden muss
public class KindResource {

	@Inject
	private KindService kindService;
	@Inject
	private GesuchService gesuchService;
	@Inject
	private JaxBConverter converter;
	@Inject
	private InstitutionService institutionService;
	@Inject
	private ResourceHelper resourceHelper;
	@Inject
	private PrincipalBean principalBean;

	@ApiOperation(value = "Speichert ein Kind in der Datenbank", response = JaxKindContainer.class)
	@Nullable
	@PUT
	@Path("/{gesuchId}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed({ADMIN_BG, SUPER_ADMIN, SACHBEARBEITER_BG, ADMIN_GEMEINDE, SACHBEARBEITER_GEMEINDE, GESUCHSTELLER, SACHBEARBEITER_TS, ADMIN_TS,
		ADMIN_INSTITUTION, SACHBEARBEITER_INSTITUTION, ADMIN_TRAEGERSCHAFT, SACHBEARBEITER_TRAEGERSCHAFT,
		ADMIN_SOZIALDIENST, SACHBEARBEITER_SOZIALDIENST})
	public JaxKindContainer saveKind(
		@Nonnull @NotNull @PathParam("gesuchId") JaxId gesuchId,
		@Nonnull @NotNull @Valid JaxKindContainer kindContainerJAXP,
		@Context UriInfo uriInfo,
		@Context HttpServletResponse response) {

		Gesuch gesuch = gesuchService.findGesuch(gesuchId.getId()).orElseThrow(() -> new EbeguEntityNotFoundException("saveKind", ErrorCodeEnum.ERROR_ENTITY_NOT_FOUND, "GesuchId invalid: " + gesuchId.getId()));

		// Sicherstellen, dass das dazugehoerige Gesuch ueberhaupt noch editiert werden darf fuer meine Rolle
		resourceHelper.assertGesuchStatusForBenutzerRole(gesuch);

		KindContainer kindToMerge = new KindContainer();
		KindContainer copyOldKind = null;

		if (kindContainerJAXP.getId() != null) {
			Optional<KindContainer> optional = kindService.findKind(kindContainerJAXP.getId());
			kindToMerge = optional.orElse(new KindContainer());
			copyOldKind = kindToMerge.copyKindContainer(new KindContainer(), AntragCopyType.MUTATION, gesuch, gesuch.getGesuchsperiode(), gesuch.getRegelStartDatum());
		}
		KindContainer convertedKind = converter.kindContainerToEntity(kindContainerJAXP, kindToMerge);
		convertedKind.setGesuch(gesuch);
		KindContainer persistedKind = this.kindService.saveKind(convertedKind, copyOldKind);

		return converter.kindContainerToJAX(persistedKind);
	}

	@ApiOperation(value = "Gibt das Kind mit der uebergebenen Id zurueck", response = JaxKindContainer.class)
	@Nullable
	@GET
	@Path("/find/{kindContainerId}")
	@Consumes(MediaType.WILDCARD)
	@Produces(MediaType.APPLICATION_JSON)
	@PermitAll // Grundsaetzliche fuer alle Rollen: Datenabhaengig. -> Authorizer
	public JaxKindContainer findKind(
		@Nonnull @NotNull @PathParam("kindContainerId") JaxId kindJAXPId) {

		Objects.requireNonNull(kindJAXPId.getId());
		String kindID = converter.toEntityId(kindJAXPId);
		Optional<KindContainer> optional = kindService.findKind(kindID);

		if (!optional.isPresent()) {
			return null;
		}
		JaxKindContainer jaxKindContainer = converter.kindContainerToJAX(optional.get());

		// Es wird gecheckt ob der Benutzer zu einer Institution/Traegerschaft gehoert. Wenn ja, werden die Kinder gefilter
		// damit nur die relevanten Kinder geschickt werden
		if (principalBean.isCallerInAnyOfRole(ADMIN_TRAEGERSCHAFT, ADMIN_INSTITUTION, SACHBEARBEITER_TRAEGERSCHAFT, SACHBEARBEITER_INSTITUTION)) {
			Collection<Institution> instForCurrBenutzer = institutionService.getInstitutionenReadableForCurrentBenutzer(false);
			RestUtil.purgeSingleKindAndBetreuungenOfInstitutionen(jaxKindContainer, instForCurrBenutzer);
		}

		return jaxKindContainer;
	}

	@ApiOperation("Loescht das Kind mit der uebergebenen Id aus der Datenbank")
	@SuppressWarnings("NonBooleanMethodNameMayNotStartWithQuestion")
	@Nullable
	@DELETE
	@Path("/{kindContainerId}")
	@Consumes(MediaType.WILDCARD)
	@RolesAllowed({ADMIN_BG, SUPER_ADMIN, SACHBEARBEITER_BG, ADMIN_GEMEINDE, SACHBEARBEITER_GEMEINDE, GESUCHSTELLER, SACHBEARBEITER_TS, ADMIN_TS,
		ADMIN_SOZIALDIENST, SACHBEARBEITER_SOZIALDIENST})
	public Response removeKind(
		@Nonnull @NotNull @PathParam("kindContainerId") JaxId kindJAXPId,
		@Context HttpServletResponse response) {

		Objects.requireNonNull(kindJAXPId.getId());
		KindContainer kind = kindService.findKind(kindJAXPId.getId()).orElseThrow(() -> new EbeguEntityNotFoundException("removeKind", ErrorCodeEnum.ERROR_ENTITY_NOT_FOUND, "KindID invalid: " + kindJAXPId.getId()));

		// Sicherstellen, dass das dazugehoerige Gesuch ueberhaupt noch editiert werden darf fuer meine Rolle
		resourceHelper.assertGesuchStatusForBenutzerRole(kind.getGesuch());

		kindService.removeKind(kind);
		return Response.ok().build();
	}

	@ApiOperation(value = "Sucht in der Datenbank nach moeglichen Dubletten fuer alle Kinder des uebergebenen " +
		"Gesuchs. Als moegliche Dublette gelten alle Kinder mit demselben Namen, Vornamen und Geburtsdatum, welche " +
		"in einem anderen Fall vorkommen.", responseContainer = "Set", response = KindDubletteDTO.class)
	@Nullable
	@GET
	@Path("/dubletten/{gesuchId}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed({ADMIN_BG, SUPER_ADMIN, SACHBEARBEITER_BG, ADMIN_GEMEINDE, SACHBEARBEITER_GEMEINDE, JURIST, REVISOR, SACHBEARBEITER_TS, ADMIN_TS,
		ADMIN_MANDANT, SACHBEARBEITER_MANDANT})
	public Set<KindDubletteDTO> getKindDubletten(@Nonnull @NotNull @PathParam("gesuchId") JaxId gesuchJaxId) {
		Objects.requireNonNull(gesuchJaxId.getId());
		String gesuchId = converter.toEntityId(gesuchJaxId);
		return kindService.getKindDubletten(gesuchId);
	}
}
