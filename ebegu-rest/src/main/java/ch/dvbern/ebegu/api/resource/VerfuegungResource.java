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
import java.util.stream.Collectors;

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
import ch.dvbern.ebegu.api.dtos.JaxBetreuung;
import ch.dvbern.ebegu.api.dtos.JaxId;
import ch.dvbern.ebegu.api.dtos.JaxKindContainer;
import ch.dvbern.ebegu.api.dtos.JaxVerfuegung;
import ch.dvbern.ebegu.api.resource.util.ResourceHelper;
import ch.dvbern.ebegu.api.util.RestUtil;
import ch.dvbern.ebegu.authentication.PrincipalBean;
import ch.dvbern.ebegu.entities.AbstractAnmeldung;
import ch.dvbern.ebegu.entities.AnmeldungFerieninsel;
import ch.dvbern.ebegu.entities.AnmeldungTagesschule;
import ch.dvbern.ebegu.entities.Betreuung;
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.entities.Institution;
import ch.dvbern.ebegu.entities.Verfuegung;
import ch.dvbern.ebegu.enums.Betreuungsstatus;
import ch.dvbern.ebegu.enums.ErrorCodeEnum;
import ch.dvbern.ebegu.errors.EbeguEntityNotFoundException;
import ch.dvbern.ebegu.services.BetreuungService;
import ch.dvbern.ebegu.services.GesuchService;
import ch.dvbern.ebegu.services.InstitutionService;
import ch.dvbern.ebegu.services.VerfuegungService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

import static ch.dvbern.ebegu.enums.UserRoleName.ADMIN_BG;
import static ch.dvbern.ebegu.enums.UserRoleName.ADMIN_GEMEINDE;
import static ch.dvbern.ebegu.enums.UserRoleName.ADMIN_INSTITUTION;
import static ch.dvbern.ebegu.enums.UserRoleName.ADMIN_TRAEGERSCHAFT;
import static ch.dvbern.ebegu.enums.UserRoleName.ADMIN_TS;
import static ch.dvbern.ebegu.enums.UserRoleName.SACHBEARBEITER_BG;
import static ch.dvbern.ebegu.enums.UserRoleName.SACHBEARBEITER_GEMEINDE;
import static ch.dvbern.ebegu.enums.UserRoleName.SACHBEARBEITER_INSTITUTION;
import static ch.dvbern.ebegu.enums.UserRoleName.SACHBEARBEITER_TRAEGERSCHAFT;
import static ch.dvbern.ebegu.enums.UserRoleName.SACHBEARBEITER_TS;
import static ch.dvbern.ebegu.enums.UserRoleName.SUPER_ADMIN;

/**
 * REST Resource fuer Verfügungen
 */
@Path("verfuegung")
@Stateless
@Api("Resource für Verfügungen, inkl. Berechnung der Vergünstigung")
@DenyAll // Absichtlich keine Rolle zugelassen, erzwingt, dass es für neue Methoden definiert werden muss
public class VerfuegungResource {

	@Inject
	private VerfuegungService verfuegungService;

	@Inject
	private GesuchService gesuchService;

	@Inject
	private BetreuungService betreuungService;

	@Inject
	private InstitutionService institutionService;

	@Inject
	private ResourceHelper resourceHelper;

	@Inject
	private JaxBConverter converter;

	@Inject
	private PrincipalBean principalBean;


	@ApiOperation(value = "Calculates the Verfuegung of the Gesuch with the given id, does nothing if the Gesuch " +
		"does not exists. Note: Nothing is stored in the Database",
		responseContainer = "Set", response = JaxKindContainer.class)
	@Nullable
	@GET
	@Path("/calculate/{gesuchId}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@PermitAll // Grundsaetzliche fuer alle Rollen: Datenabhaengig. -> Authorizer
	public Response calculateVerfuegung(
		@Nonnull @NotNull @PathParam("gesuchId") JaxId gesuchstellerId,
		@Context UriInfo uriInfo,
		@Context HttpServletResponse response) {

		Optional<Gesuch> gesuchOptional = gesuchService.findGesuch(gesuchstellerId.getId());

		if (!gesuchOptional.isPresent()) {
			return null;
		}
		Gesuch gesuch = gesuchOptional.get();
		Gesuch gesuchWithCalcVerfuegung = verfuegungService.calculateVerfuegung(gesuch);

		// wir muessen nur die kind container mappen nicht das ganze gesuch
		Set<JaxKindContainer> kindContainers = gesuchWithCalcVerfuegung.getKindContainers().stream()
				.map(kindContainer -> converter.kindContainerToJAX(kindContainer))
				.collect(Collectors.toSet());
		// Es wird gecheckt ob der Benutzer zu einer Institution/Traegerschaft gehoert. Wenn ja, werden die Kinder
		// gefiltert, damit nur die relevanten Kinder geschickt werden
		if (principalBean.isCallerInAnyOfRole(
			ADMIN_TRAEGERSCHAFT,
			SACHBEARBEITER_TRAEGERSCHAFT,
			ADMIN_INSTITUTION,
			SACHBEARBEITER_INSTITUTION)) {
			Collection<Institution> instForCurrBenutzer =
				institutionService.getInstitutionenReadableForCurrentBenutzer(false);
			RestUtil.purgeKinderAndBetreuungenOfInstitutionen(kindContainers, instForCurrBenutzer);
		}
		return Response.ok(kindContainers).build();
	}

	@ApiOperation(value = "Generiert eine Verfuegung und speichert diese in der Datenbank", response = JaxVerfuegung.class)
	@Nullable
	@PUT
	@Path("/verfuegen/{gesuchId}/{betreuungId}/{ignorieren}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed({ SUPER_ADMIN, ADMIN_GEMEINDE, SACHBEARBEITER_GEMEINDE, ADMIN_BG, SACHBEARBEITER_BG, ADMIN_TS, SACHBEARBEITER_TS })
	public JaxVerfuegung saveVerfuegung(
		@Nonnull @NotNull @PathParam("gesuchId") JaxId gesuchJaxId,
		@Nonnull @NotNull @PathParam("betreuungId") JaxId betreuungJaxId,
		@Nonnull @NotNull @PathParam("ignorieren") Boolean ignorieren,
		@Nullable String verfuegungManuelleBemerkungen
	) {
		String gesuchId = converter.toEntityId(gesuchJaxId);
		String betreuungId = converter.toEntityId(betreuungJaxId);

		Verfuegung persistedVerfuegung = this.verfuegungService.verfuegen(gesuchId, betreuungId, verfuegungManuelleBemerkungen, ignorieren, true);
		return converter.verfuegungToJax(persistedVerfuegung);
	}

	@ApiOperation("Schliesst eine Betreuung ab, ohne sie zu verfuegen")
	@Nullable
	@POST
	@Path("/schliessenOhneVerfuegen/{betreuungId}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed({ SUPER_ADMIN, ADMIN_GEMEINDE, SACHBEARBEITER_GEMEINDE, ADMIN_BG, SACHBEARBEITER_BG, ADMIN_TS, SACHBEARBEITER_TS })
	public Response verfuegungSchliessenOhneVerfuegen(
		@Nonnull @NotNull @PathParam("betreuungId") JaxId betreuungId) {

		Betreuung betreuung = betreuungService.findBetreuung(betreuungId.getId())
			.orElseThrow(() -> new EbeguEntityNotFoundException(
				"verfuegungSchliessenOhneVerfuegen",
				ErrorCodeEnum.ERROR_ENTITY_NOT_FOUND,
				"BetreuungID invalid: " + betreuungId.getId()));


		betreuungService.schliessenOhneVerfuegen(betreuung);

		return Response.ok().build();
	}

	@ApiOperation(value = "Erstellt eine Nichteintretens-Verfuegung", response = JaxVerfuegung.class)
	@Nullable
	@GET
	@Path("/nichtEintreten/{gesuchId}/{betreuungId}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed({ SUPER_ADMIN, ADMIN_GEMEINDE, SACHBEARBEITER_GEMEINDE, ADMIN_BG, SACHBEARBEITER_BG, ADMIN_TS, SACHBEARBEITER_TS })
	public JaxVerfuegung schliessenNichtEintreten(
		@Nonnull @NotNull @PathParam("gesuchId") JaxId gesuchJaxId,
		@Nonnull @NotNull @PathParam("betreuungId") JaxId betreuungJaxId
	) {
		String gesuchId = converter.toEntityId(gesuchJaxId);
		String betreuungId = converter.toEntityId(betreuungJaxId);

		Verfuegung persistedVerfuegung = this.verfuegungService.nichtEintreten(gesuchId, betreuungId);
		return converter.verfuegungToJax(persistedVerfuegung);
	}

	@ApiOperation(value = "Schulamt-Anmeldung wird durch die Institution bestätigt und die Finanzielle Situation ist "
		+ "geprueft", response = JaxBetreuung.class)
	@Nonnull
	@PUT
	@Path("/anmeldung/uebernehmen")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed({ SUPER_ADMIN, ADMIN_TRAEGERSCHAFT, SACHBEARBEITER_TRAEGERSCHAFT, ADMIN_INSTITUTION,
		SACHBEARBEITER_INSTITUTION, SACHBEARBEITER_TS, ADMIN_TS, ADMIN_GEMEINDE, SACHBEARBEITER_GEMEINDE })
	public JaxBetreuung anmeldungUebernehmen(
		@Nonnull @NotNull @Valid JaxBetreuung betreuungJAXP,
		@Context UriInfo uriInfo,
		@Context HttpServletResponse response
	) {
		Objects.requireNonNull(betreuungJAXP.getId());
		Objects.requireNonNull(betreuungJAXP.getKindId());

		// Sicherstellen, dass der Status des Server-Objektes genau dem erwarteten Status entspricht
		resourceHelper.assertBetreuungStatusEqual(betreuungJAXP.getId(),
			Betreuungsstatus.SCHULAMT_ANMELDUNG_AUSGELOEST, Betreuungsstatus.SCHULAMT_MODULE_AKZEPTIERT);

		AbstractAnmeldung convertedBetreuung = converter.platzToStoreableEntity(betreuungJAXP);
		// Sicherstellen, dass das dazugehoerige Gesuch ueberhaupt noch editiert werden darf fuer meine Rolle
		resourceHelper.assertGesuchStatusForBenutzerRole(convertedBetreuung.getKind().getGesuch(), convertedBetreuung);

		if (convertedBetreuung.getBetreuungsangebotTyp().isTagesschule()) {
			AnmeldungTagesschule convertedAnmeldungTagesschule = (AnmeldungTagesschule) convertedBetreuung;
			AnmeldungTagesschule persistedBetreuung =
				this.verfuegungService.anmeldungTagesschuleUebernehmen(convertedAnmeldungTagesschule);
			return converter.platzToJAX(persistedBetreuung);
		} else {
			AnmeldungFerieninsel convertedAnmeldungFerieninsel = (AnmeldungFerieninsel) convertedBetreuung;
			AnmeldungFerieninsel persistedBetreuung =
				this.verfuegungService.anmeldungFerieninselUebernehmen(convertedAnmeldungFerieninsel);
			return converter.platzToJAX(persistedBetreuung);
		}
	}
}

