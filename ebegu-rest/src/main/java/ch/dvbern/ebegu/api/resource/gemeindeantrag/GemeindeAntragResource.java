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

package ch.dvbern.ebegu.api.resource.gemeindeantrag;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.security.DenyAll;
import javax.annotation.security.RolesAllowed;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;

import ch.dvbern.ebegu.api.converter.JaxBConverter;
import ch.dvbern.ebegu.api.dtos.JaxId;
import ch.dvbern.ebegu.api.dtos.gemeindeantrag.JaxGemeindeAntrag;
import ch.dvbern.ebegu.api.dtos.gemeindeantrag.JaxLastenausgleichTagesschuleAngabenInstitutionContainer;
import ch.dvbern.ebegu.authentication.PrincipalBean;
import ch.dvbern.ebegu.config.EbeguConfiguration;
import ch.dvbern.ebegu.entities.Gemeinde;
import ch.dvbern.ebegu.entities.Gesuchsperiode;
import ch.dvbern.ebegu.entities.gemeindeantrag.GemeindeAntrag;
import ch.dvbern.ebegu.enums.ErrorCodeEnum;
import ch.dvbern.ebegu.enums.UserRole;
import ch.dvbern.ebegu.enums.gemeindeantrag.GemeindeAntragTyp;
import ch.dvbern.ebegu.errors.EbeguEntityNotFoundException;
import ch.dvbern.ebegu.errors.EbeguRuntimeException;
import ch.dvbern.ebegu.errors.EntityExistsException;
import ch.dvbern.ebegu.services.Authorizer;
import ch.dvbern.ebegu.services.GemeindeService;
import ch.dvbern.ebegu.services.GesuchsperiodeService;
import ch.dvbern.ebegu.services.InstitutionService;
import ch.dvbern.ebegu.services.gemeindeantrag.GemeindeAntragService;
import ch.dvbern.ebegu.services.gemeindeantrag.LastenausgleichTagesschuleAngabenInstitutionService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

import static ch.dvbern.ebegu.enums.UserRoleName.ADMIN_BG;
import static ch.dvbern.ebegu.enums.UserRoleName.ADMIN_FERIENBETREUUNG;
import static ch.dvbern.ebegu.enums.UserRoleName.ADMIN_GEMEINDE;
import static ch.dvbern.ebegu.enums.UserRoleName.ADMIN_INSTITUTION;
import static ch.dvbern.ebegu.enums.UserRoleName.ADMIN_MANDANT;
import static ch.dvbern.ebegu.enums.UserRoleName.ADMIN_TRAEGERSCHAFT;
import static ch.dvbern.ebegu.enums.UserRoleName.ADMIN_TS;
import static ch.dvbern.ebegu.enums.UserRoleName.SACHBEARBEITER_BG;
import static ch.dvbern.ebegu.enums.UserRoleName.SACHBEARBEITER_FERIENBETREUUNG;
import static ch.dvbern.ebegu.enums.UserRoleName.SACHBEARBEITER_GEMEINDE;
import static ch.dvbern.ebegu.enums.UserRoleName.SACHBEARBEITER_INSTITUTION;
import static ch.dvbern.ebegu.enums.UserRoleName.SACHBEARBEITER_MANDANT;
import static ch.dvbern.ebegu.enums.UserRoleName.SACHBEARBEITER_TRAEGERSCHAFT;
import static ch.dvbern.ebegu.enums.UserRoleName.SACHBEARBEITER_TS;
import static ch.dvbern.ebegu.enums.UserRoleName.SUPER_ADMIN;

/**
 * REST Resource fuer Gemeindeantraege
 */
@Path("gemeindeantrag")
@Stateless
@Api(description = "Resource fuer Gemeindeantraege")
@DenyAll // Absichtlich keine Rolle zugelassen, erzwingt, dass es für neue Methoden definiert werden muss
public class GemeindeAntragResource {

	@Inject
	private GemeindeAntragService gemeindeAntragService;

	@Inject
	private GesuchsperiodeService gesuchsperiodeService;

	@Inject
	private GemeindeService gemeindeService;

	@Inject
	private LastenausgleichTagesschuleAngabenInstitutionService angabenInstitutionService;

	@Inject
	private JaxBConverter converter;

	@Inject
	private PrincipalBean principal;

	@Inject
	private Authorizer authorizer;

	@Inject
	private InstitutionService institutionService;

	@Inject
	private EbeguConfiguration configuration;

	@ApiOperation(
		"Erstellt fuer jede aktive Gemeinde einen Gemeindeantrag des gewuenschten Typs fuer die gewuenschte Periode")
	@POST
	@Path("/createAllAntraege/{gemeindeAntragTyp}/gesuchsperiode/{gesuchsperiodeId}")
	@RolesAllowed({ SUPER_ADMIN, ADMIN_MANDANT, SACHBEARBEITER_MANDANT })
	public List<JaxGemeindeAntrag> createAllGemeindeAntraege(
		@Nonnull @Valid @PathParam("gemeindeAntragTyp") GemeindeAntragTyp gemeindeAntragTyp,
		@Nonnull @Valid @PathParam("gesuchsperiodeId") JaxId gesuchsperiodeJaxId,
		@Context HttpServletRequest request,
		@Context UriInfo uriInfo
	) {
		Objects.requireNonNull(gesuchsperiodeJaxId.getId());
		Objects.requireNonNull(gemeindeAntragTyp);

		String gesuchsperiodeId = converter.toEntityId(gesuchsperiodeJaxId);
		Gesuchsperiode gesuchsperiode = gesuchsperiodeService.findGesuchsperiode(gesuchsperiodeId).
			orElseThrow(() -> new EbeguEntityNotFoundException(
				"createAllGemeindeAntraege",
				ErrorCodeEnum.ERROR_ENTITY_NOT_FOUND,
				gesuchsperiodeId));

		try {
			final List<GemeindeAntrag> gemeindeAntragList =
				gemeindeAntragService.createAllGemeindeAntraege(gesuchsperiode, gemeindeAntragTyp);
			return converter.gemeindeAntragListToJax(gemeindeAntragList);
		} catch (EntityExistsException e) {
			throw new WebApplicationException(e, Status.CONFLICT);
		}
	}

	@ApiOperation(
		"Löscht alle Anträge des gewuenschten Typs fuer die gewuenschte Periode")
	@DELETE
	@Path("/deleteAntraege/{gemeindeAntragTyp}/gesuchsperiode/{gesuchsperiodeId}")
	@RolesAllowed({ SUPER_ADMIN })
	public void deleteGemeindeAntraege(
		@Nonnull @Valid @PathParam("gemeindeAntragTyp") GemeindeAntragTyp gemeindeAntragTyp,
		@Nonnull @Valid @PathParam("gesuchsperiodeId") JaxId gesuchsperiodeJaxId,
		@Context HttpServletRequest request,
		@Context UriInfo uriInfo
	) {

		if (!configuration.getIsDevmode()) {
			throw new EbeguRuntimeException("deleteGemeindeAntraege", "deleteGemeindeAntrage ist nur im Devmode möglich");
		}

		Objects.requireNonNull(gesuchsperiodeJaxId.getId());
		Objects.requireNonNull(gemeindeAntragTyp);

		String gesuchsperiodeId = converter.toEntityId(gesuchsperiodeJaxId);
		Gesuchsperiode gesuchsperiode = gesuchsperiodeService.findGesuchsperiode(gesuchsperiodeId).
			orElseThrow(() -> new EbeguEntityNotFoundException(
				"deleteGemeindeAntraege",
				ErrorCodeEnum.ERROR_ENTITY_NOT_FOUND,
				gesuchsperiodeId));

		gemeindeAntragService.deleteGemeindeAntraege(gesuchsperiode, gemeindeAntragTyp);
	}

	@ApiOperation(
		"Erstellt fuer die gewählte Gemeinde einen Gemeindeantrag des gewuenschten Typs fuer die gewuenschte Periode")
	@POST
	@Path("/create/{gemeindeAntragTyp}/gesuchsperiode/{gesuchsperiodeId}/gemeinde/{gemeindeId}")
	@RolesAllowed({ SUPER_ADMIN, ADMIN_MANDANT, SACHBEARBEITER_MANDANT, ADMIN_GEMEINDE, SACHBEARBEITER_GEMEINDE,
		ADMIN_BG, SACHBEARBEITER_BG, ADMIN_TS, SACHBEARBEITER_TS, ADMIN_FERIENBETREUUNG, SACHBEARBEITER_FERIENBETREUUNG })
	public JaxGemeindeAntrag createGemeindeAntrag(
		@Nonnull @Valid @PathParam("gemeindeAntragTyp") GemeindeAntragTyp gemeindeAntragTyp,
		@Nonnull @Valid @PathParam("gesuchsperiodeId") JaxId gesuchsperiodeJaxId,
		@Nonnull @Valid @PathParam("gemeindeId") JaxId gemeindeJaxId,
		@Context HttpServletRequest request,
		@Context UriInfo uriInfo
	) {
		Objects.requireNonNull(gesuchsperiodeJaxId.getId());
		Objects.requireNonNull(gemeindeAntragTyp);
		Objects.requireNonNull(gemeindeJaxId.getId());

		String gesuchsperiodeId = converter.toEntityId(gesuchsperiodeJaxId);
		Gesuchsperiode gesuchsperiode = gesuchsperiodeService.findGesuchsperiode(gesuchsperiodeId).
			orElseThrow(() -> new EbeguEntityNotFoundException(
				"createGemeindeAntrag",
				ErrorCodeEnum.ERROR_ENTITY_NOT_FOUND,
				gesuchsperiodeId));

		String gemeindeId = converter.toEntityId(gemeindeJaxId);
		Gemeinde gemeinde = gemeindeService.findGemeinde(gemeindeId).
			orElseThrow(() -> new EbeguEntityNotFoundException(
				"createGemeindeAntrag",
				ErrorCodeEnum.ERROR_ENTITY_NOT_FOUND,
				gemeindeId));

		try {
			final GemeindeAntrag gemeindeAntrag =
				gemeindeAntragService.createGemeindeAntrag(gemeinde, gesuchsperiode, gemeindeAntragTyp);
			return converter.gemeindeAntragToJax(gemeindeAntrag);
		} catch (EntityExistsException e) {
			throw new WebApplicationException(e, Status.CONFLICT);
		}
	}

	@ApiOperation("Gibt alle Gemeindeanträge zurück, die die Benutzerin sehen kann")
	@GET
	@Path("")
	@RolesAllowed({ SUPER_ADMIN, ADMIN_MANDANT, SACHBEARBEITER_MANDANT, ADMIN_GEMEINDE, SACHBEARBEITER_GEMEINDE, ADMIN_TS, SACHBEARBEITER_TS,
		SACHBEARBEITER_INSTITUTION, ADMIN_INSTITUTION, ADMIN_TRAEGERSCHAFT, SACHBEARBEITER_TRAEGERSCHAFT, ADMIN_BG, SACHBEARBEITER_BG,
		SACHBEARBEITER_FERIENBETREUUNG, ADMIN_FERIENBETREUUNG})
	public List<JaxGemeindeAntrag> getAllGemeindeAntraege(
		@Nullable @QueryParam("gemeinde") String gemeinde,
		@Nullable @QueryParam("periode") String periode,
		@Nullable @QueryParam("typ") String typ,
		@Nullable @QueryParam("status") String status,
		@Nullable @QueryParam("timestampMutiert") String timestampMutiert
	) {
		return converter.gemeindeAntragListToJax(
			(List<GemeindeAntrag>) gemeindeAntragService.getGemeindeAntraege(gemeinde, periode, typ, status, timestampMutiert));
	}

	@ApiOperation("Gibt alle Tagesschuleanträge des Gemeinde-Antrags zurück, die für die Benutzerin sichtbar sind")
	@GET
	@Path("{gemeindeAntragId}/tagesschulenantraege")
	@RolesAllowed({ SUPER_ADMIN, ADMIN_MANDANT, SACHBEARBEITER_MANDANT, ADMIN_INSTITUTION, SACHBEARBEITER_INSTITUTION, ADMIN_GEMEINDE,
		ADMIN_TS, SACHBEARBEITER_TS, SACHBEARBEITER_GEMEINDE })
	public List<JaxLastenausgleichTagesschuleAngabenInstitutionContainer> getTagesschuleAntraegeFuerGemeinedAntrag(
		@Nonnull @Valid @PathParam("gemeindeAntragId") String gemeindeAntragId
	) {
		authorizer.checkReadAuthorizationLATSGemeindeAntrag(gemeindeAntragId);

		return angabenInstitutionService.findLastenausgleichTagesschuleAngabenInstitutionByGemeindeAntragId(
			gemeindeAntragId)
			.stream()
			// does user belong to institution or is mandant
			.filter(lastenausgleichTagesschuleAngabenInstitutionContainer -> principal.isCallerInAnyOfRole(UserRole.getMandantSuperadminRoles())
				|| institutionService.getInstitutionenReadableForCurrentBenutzer(
				false).stream()
				.anyMatch(institution -> institution.getId()
					.equals(lastenausgleichTagesschuleAngabenInstitutionContainer.getInstitution().getId())))
			.filter(lastenausgleichTagesschuleAngabenInstitutionContainer -> {
				switch (lastenausgleichTagesschuleAngabenInstitutionContainer.getStatus()) {
				case OFFEN:
				case IN_PRUEFUNG_GEMEINDE:
					return principal.isCallerInAnyOfRole(
						UserRole.SUPER_ADMIN,
						UserRole.ADMIN_GEMEINDE,
						UserRole.SACHBEARBEITER_GEMEINDE,
						UserRole.ADMIN_INSTITUTION,
						UserRole.SACHBEARBEITER_INSTITUTION);
				}
				return true;
			})
			.map(l -> converter.lastenausgleichTagesschuleAngabenInstitutionContainerToJax(l))
			.collect(Collectors.toList());

	}
}
