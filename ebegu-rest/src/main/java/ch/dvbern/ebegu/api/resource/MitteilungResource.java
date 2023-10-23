/*
 * Copyright (C) 2023 DV Bern AG, Switzerland
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

package ch.dvbern.ebegu.api.resource;

import ch.dvbern.ebegu.api.converter.JaxBConverter;
import ch.dvbern.ebegu.api.dtos.*;
import ch.dvbern.ebegu.dto.suchfilter.smarttable.MitteilungTableFilterDTO;
import ch.dvbern.ebegu.dto.suchfilter.smarttable.PaginationDTO;
import ch.dvbern.ebegu.entities.*;
import ch.dvbern.ebegu.enums.ErrorCodeEnum;
import ch.dvbern.ebegu.errors.EbeguEntityNotFoundException;
import ch.dvbern.ebegu.errors.EbeguRuntimeException;
import ch.dvbern.ebegu.errors.KibonLogLevel;
import ch.dvbern.ebegu.i18n.LocaleThreadLocal;
import ch.dvbern.ebegu.services.BenutzerService;
import ch.dvbern.ebegu.services.BetreuungService;
import ch.dvbern.ebegu.services.DossierService;
import ch.dvbern.ebegu.services.MitteilungService;
import ch.dvbern.ebegu.util.MitteilungUtil;
import ch.dvbern.ebegu.util.MonitoringUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.security.DenyAll;
import javax.annotation.security.RolesAllowed;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.util.*;
import java.util.stream.Collectors;

import static ch.dvbern.ebegu.enums.UserRoleName.*;

/**
 * Resource fuer Mitteilung
 */
@Path("mitteilungen")
@Stateless
@Api(description = "Resource zum Verwalten von Mitteilungen (In-System Nachrichten)")
@DenyAll // Absichtlich keine Rolle zugelassen, erzwingt, dass es für neue Methoden definiert werden muss
public class MitteilungResource {

	public static final String FALL_ID_INVALID = "FallID invalid: ";
	public static final String DOSSIER_ID_INVALID = "DossierId invalid: ";

	@Inject
	private MitteilungService mitteilungService;

	@Inject
	private DossierService dossierService;

	@Inject
	private BetreuungService betreuungService;

	@Inject
	private JaxBConverter converter;

	@Inject
	private BenutzerService benutzerService;

	@ApiOperation(value = "Speichert eine Mitteilung", response = JaxMitteilung.class)
	@Nullable
	@PUT
	@Path("/send")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed({ SUPER_ADMIN, ADMIN_BG, SACHBEARBEITER_BG, ADMIN_GEMEINDE, SACHBEARBEITER_GEMEINDE, GESUCHSTELLER,
		ADMIN_INSTITUTION, SACHBEARBEITER_INSTITUTION, ADMIN_TRAEGERSCHAFT, SACHBEARBEITER_TRAEGERSCHAFT,
		SACHBEARBEITER_TS, ADMIN_TS, ADMIN_SOZIALDIENST, SACHBEARBEITER_SOZIALDIENST })
	public JaxMitteilung sendMitteilung(
		@Nonnull @NotNull @Valid JaxMitteilung mitteilungJAXP,
		@Context UriInfo uriInfo,
		@Context HttpServletResponse response) {

		Mitteilung mitteilung = readAndConvertMitteilung(mitteilungJAXP);
		Mitteilung persistedMitteilung = this.mitteilungService.sendMitteilung(mitteilung);
		return converter.mitteilungToJAX(persistedMitteilung, new JaxMitteilung());
	}

	@ApiOperation(value = "Speichert eine BetreuungsMitteilung", response = JaxBetreuungsmitteilung.class)
	@Nullable
	@PUT
	@Path("/sendbetreuungsmitteilung")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed({ SUPER_ADMIN, ADMIN_INSTITUTION, SACHBEARBEITER_INSTITUTION, ADMIN_TRAEGERSCHAFT,
		SACHBEARBEITER_TRAEGERSCHAFT })
	public JaxBetreuungsmitteilung sendBetreuungsmitteilung(
		@Nonnull @NotNull @Valid JaxBetreuungsmitteilung mitteilungJAXP,
		@Context UriInfo uriInfo,
		@Context HttpServletResponse response) {

		Objects.requireNonNull(mitteilungJAXP);
		Objects.requireNonNull(mitteilungJAXP.getBetreuung());
		Objects.requireNonNull(mitteilungJAXP.getBetreuung().getId());

		Betreuung betreuung =
			betreuungService.findBetreuung(mitteilungJAXP.getBetreuung().getId())
				.orElseThrow(() -> new EbeguEntityNotFoundException(
					"sendBetreuungsmitteilung",
					ErrorCodeEnum.ERROR_ENTITY_NOT_FOUND,
					mitteilungJAXP.getBetreuung()
				));

		// we need to check if Betreuung was storniert and has an other one for the same institution whos not
		if (!mitteilungService.isBetreuungGueltigForMutation(betreuung)) {
			throw new EbeguRuntimeException(
				KibonLogLevel.WARN,
				"sendBetreuungsmitteilung",
				ErrorCodeEnum.ERROR_BETREUUNG_STORNIERT_UND_UNGUELTIG);
		}

		// we first clear all the Mutationsmeldungen for the current Betreuung
		mitteilungService.removeOffeneBetreuungsmitteilungenForBetreuung(betreuung);

		Betreuungsmitteilung betreuungsmitteilung =
			converter.betreuungsmitteilungToEntity(mitteilungJAXP, new Betreuungsmitteilung());

		final Locale locale = LocaleThreadLocal.get();

		final Benutzer currentBenutzer = benutzerService.getCurrentBenutzer()
			.orElseThrow(() -> new EbeguEntityNotFoundException(
				"sendBetreuungsmitteilung",
				ErrorCodeEnum.ERROR_ENTITY_NOT_FOUND));

		MitteilungUtil.initializeBetreuungsmitteilung(betreuungsmitteilung, betreuung, currentBenutzer, locale);

		betreuungsmitteilung.setMessage(mitteilungService.createNachrichtForMutationsmeldung(betreuungsmitteilung,
			betreuungsmitteilung.getBetreuungspensen()));

		Betreuungsmitteilung persistedMitteilung =
			this.mitteilungService.sendBetreuungsmitteilung(betreuungsmitteilung);
		return converter.betreuungsmitteilungToJAX(persistedMitteilung);
	}

	@ApiOperation(value = "Uebernimmt eine Betreuungsmitteilung in eine Mutation. Falls aktuell keine Mutation offen"
		+ " " +
		"ist, wird eine neue erstellt. Falls eine Mutation im Status VERFUEGEN vorhanden ist, oder die Mutation im " +
		"Status BESCHWERDE ist, wird ein Fehler zurueckgegeben", response = JaxId.class)
	@Nullable
	@PUT
	@Path("/applybetreuungsmitteilung/{betreuungsmitteilungId}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed({ SUPER_ADMIN, ADMIN_BG, SACHBEARBEITER_BG, ADMIN_GEMEINDE, SACHBEARBEITER_GEMEINDE })
	public JaxId applyBetreuungsmitteilung(
		@Nonnull @NotNull @PathParam("betreuungsmitteilungId") JaxId betreuungsmitteilungId,
		@Context UriInfo uriInfo,
		@Context HttpServletResponse response) {

		final Optional<Betreuungsmitteilung> mitteilung =
			mitteilungService.findBetreuungsmitteilung(betreuungsmitteilungId.getId());
		if (mitteilung.isEmpty()) {
			throw new EbeguEntityNotFoundException("applyBetreuungsmitteilung", ErrorCodeEnum.ERROR_ENTITY_NOT_FOUND,
				"BetreuungsmitteilungID invalid: " + betreuungsmitteilungId.getId());
		}
		final Gesuch mutiertesGesuch = this.mitteilungService.applyBetreuungsmitteilung(mitteilung.get());
		return converter.toJaxId(mutiertesGesuch);
	}
	@ApiOperation(value = "Uebernimmt eine Betreuungsmitteilung in eine Mutation. Falls aktuell keine Mutation offen "
		+ "ist, wird eine neue erstellt. Falls eine Mutation im Status VERFUEGEN vorhanden ist, oder die Mutation im "
		+ "Status BESCHWERDE ist, wird der Fehler auf der Betreuungsmitteilung gespeichert und kein Fehler geworfen",
		response = JaxMitteilungSearchresultDTO.class)
	@Nonnull
	@POST
	@Path("/applybetreuungsmitteilungsilently")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed({ SUPER_ADMIN, ADMIN_BG, SACHBEARBEITER_BG, ADMIN_GEMEINDE, SACHBEARBEITER_GEMEINDE, GESUCHSTELLER,
		ADMIN_INSTITUTION, SACHBEARBEITER_INSTITUTION, ADMIN_TRAEGERSCHAFT, SACHBEARBEITER_TRAEGERSCHAFT, ADMIN_TS,
		SACHBEARBEITER_TS, ADMIN_SOZIALDIENST, SACHBEARBEITER_SOZIALDIENST })
	public JaxBetreuungsmitteilung applyBetreuungsmitteilungSilenty(
		@Nonnull @NotNull JaxBetreuungsmitteilung jaxBetreuungsmitteilung,
		@Context UriInfo uriInfo,
		@Context HttpServletResponse response) {

		Objects.requireNonNull(jaxBetreuungsmitteilung.getId());
		final Betreuungsmitteilung betreuungsmitteilung =
			mitteilungService.findBetreuungsmitteilung(jaxBetreuungsmitteilung.getId())
				.orElseThrow(() -> new EbeguEntityNotFoundException(
					"applyBetreuungsmitteilung",
					ErrorCodeEnum.ERROR_ENTITY_NOT_FOUND,
					"JaxBetreuungsmitteilungId is invalid: " + jaxBetreuungsmitteilung.getId()));

		String errorMessage = mitteilungService.applyBetreuungsmitteilungIfPossible(betreuungsmitteilung);
		Betreuungsmitteilung betreuungsmitteilungUpdated =
			mitteilungService.findAndRefreshBetreuungsmitteilung(betreuungsmitteilung.getId())
				.orElseThrow(() -> new EbeguEntityNotFoundException(
					"applyBetreuungsmitteilung",
					ErrorCodeEnum.ERROR_ENTITY_NOT_FOUND,
					"JaxBetreuungsmitteilungId is invalid after Update: " + betreuungsmitteilung.getId()));
		betreuungsmitteilungUpdated.setErrorMessage(errorMessage);

		return converter.betreuungsmitteilungToJAX(betreuungsmitteilungUpdated);
	}

	@ApiOperation(value = "Markiert eine Mitteilung als gelesen", response = JaxMitteilung.class)
	@Nullable
	@PUT
	@Path("/setgelesen/{mitteilungId}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed({ SUPER_ADMIN, ADMIN_BG, SACHBEARBEITER_BG, ADMIN_GEMEINDE, SACHBEARBEITER_GEMEINDE,
		SACHBEARBEITER_TS, ADMIN_TS })
	public JaxMitteilung setMitteilungGelesen(
		@Nonnull @NotNull @PathParam("mitteilungId") JaxId mitteilungId,
		@Context UriInfo uriInfo,
		@Context HttpServletResponse response) {

		final Mitteilung mitteilung = mitteilungService.setMitteilungGelesen(mitteilungId.getId());

		return converter.mitteilungToJAX(mitteilung, new JaxMitteilung());
	}

	@ApiOperation(value = "Markiert eine Mitteilung als erledigt", response = JaxMitteilung.class)
	@Nullable
	@PUT
	@Path("/seterledigt/{mitteilungId}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed({ SUPER_ADMIN, ADMIN_BG, SACHBEARBEITER_BG, ADMIN_GEMEINDE, SACHBEARBEITER_GEMEINDE,
		SACHBEARBEITER_TS, ADMIN_TS })
	public JaxMitteilung setMitteilungErledigt(
		@Nonnull @NotNull @PathParam("mitteilungId") JaxId mitteilungId,
		@Context UriInfo uriInfo,
		@Context HttpServletResponse response) {

		final Mitteilung mitteilung = mitteilungService.setMitteilungErledigt(mitteilungId.getId());

		return converter.mitteilungToJAX(mitteilung, new JaxMitteilung());
	}

	@ApiOperation(value = "Markiert eine Mitteilung als ungelesen", response = JaxMitteilung.class)
	@Nullable
	@PUT
	@Path("/setneu/{mitteilungId}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed({ SUPER_ADMIN })
	public JaxMitteilung setMitteilungUngelesen(
		@Nonnull @NotNull @PathParam("mitteilungId") JaxId mitteilungId,
		@Context UriInfo uriInfo,
		@Context HttpServletResponse response) {

		final Mitteilung mitteilung = mitteilungService.setMitteilungUngelesen(mitteilungId.getId());

		return converter.mitteilungToJAX(mitteilung, new JaxMitteilung());
	}

	@ApiOperation(value = "Markiert eine Mitteilung als ignoriert", response = JaxMitteilung.class)
	@Nullable
	@PUT
	@Path("/setignoriert/{mitteilungId}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed({ SUPER_ADMIN, SUPER_ADMIN, ADMIN_BG, SACHBEARBEITER_BG, ADMIN_GEMEINDE, SACHBEARBEITER_GEMEINDE,
			SACHBEARBEITER_TS, ADMIN_TS })
	public JaxMitteilung setMitteilungIgnoriert(
		@Nonnull @NotNull @PathParam("mitteilungId") JaxId mitteilungId,
		@Context UriInfo uriInfo,
		@Context HttpServletResponse response) {

		final Mitteilung mitteilung = mitteilungService.setMitteilungIgnoriert(mitteilungId.getId());

		return converter.mitteilungToJAX(mitteilung, new JaxMitteilung());
	}

	@ApiOperation(value = "Gibt die Mitteilung mit der uebergebenen Id zurueck", response = JaxMitteilung.class)
	@Nullable
	@GET
	@Path("/seterledigt/{mitteilungId}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed({ SUPER_ADMIN, ADMIN_BG, SACHBEARBEITER_BG, ADMIN_GEMEINDE, SACHBEARBEITER_GEMEINDE, GESUCHSTELLER,
		ADMIN_INSTITUTION, SACHBEARBEITER_INSTITUTION, ADMIN_TRAEGERSCHAFT, SACHBEARBEITER_TRAEGERSCHAFT,
		SACHBEARBEITER_TS, ADMIN_TS, ADMIN_SOZIALDIENST, SACHBEARBEITER_SOZIALDIENST })
	public JaxMitteilung findMitteilung(
		@Nonnull @NotNull @PathParam("mitteilungId") JaxId mitteilungId,
		@Context UriInfo uriInfo,
		@Context HttpServletResponse response) {

		Objects.requireNonNull(mitteilungId.getId());
		String mitteilungID = converter.toEntityId(mitteilungId);
		Optional<Mitteilung> optional = mitteilungService.findMitteilung(mitteilungID);

		return optional.map(mitteilung -> converter.mitteilungToJAX(mitteilung, new JaxMitteilung())).orElse(null);
	}

	@ApiOperation(value = "Gibt die neueste Betreuungsmitteilung fuer die uebergebene Betreuung zurueck",
		response = JaxMitteilung.class)
	@Nullable
	@GET
	@Path("/newestBetreuunsmitteilung/{betreuungId}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed({ SUPER_ADMIN, ADMIN_BG, SACHBEARBEITER_BG, ADMIN_GEMEINDE, SACHBEARBEITER_GEMEINDE, GESUCHSTELLER,
		ADMIN_INSTITUTION, SACHBEARBEITER_INSTITUTION, ADMIN_TRAEGERSCHAFT, SACHBEARBEITER_TRAEGERSCHAFT, JURIST,
		REVISOR, ADMIN_TS, SACHBEARBEITER_TS, ADMIN_MANDANT, SACHBEARBEITER_MANDANT, ADMIN_SOZIALDIENST,
		SACHBEARBEITER_SOZIALDIENST })
	public JaxMitteilung findNewestBetreuunsmitteilung(
		@Nonnull @NotNull @PathParam("betreuungId") JaxId jaxBetreuungId,
		@Context UriInfo uriInfo,
		@Context HttpServletResponse response) {

		Objects.requireNonNull(jaxBetreuungId.getId());
		String betreuungId = converter.toEntityId(jaxBetreuungId);
		Optional<Betreuung> optional = betreuungService.findBetreuung(betreuungId);

		if (optional.isEmpty()) {
			throw new EbeguEntityNotFoundException(
				"findNewestBetreuunsmitteilung",
				ErrorCodeEnum.ERROR_ENTITY_NOT_FOUND,
				betreuungId);
		}

		Optional<Betreuungsmitteilung> optBetMitteilung =
			mitteilungService.findNewestBetreuungsmitteilung(betreuungId);
		return optBetMitteilung.map(mitteilung -> converter.betreuungsmitteilungToJAX(mitteilung)).orElse(null);
	}

	@ApiOperation(value = "Gibt einen Wrapper mit der Liste aller Mitteilungen zurueck, welche fuer den eingeloggten"
		+ " " +
		"Benutzer fuer das uebergebene Dossier vorhanden sind", response = JaxMitteilungen.class)
	@Nullable
	@GET
	@Path("/forrole/dossier/{dossierId}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed({ SUPER_ADMIN, ADMIN_BG, SACHBEARBEITER_BG, ADMIN_GEMEINDE, SACHBEARBEITER_GEMEINDE, GESUCHSTELLER,
		ADMIN_INSTITUTION, SACHBEARBEITER_INSTITUTION, ADMIN_TRAEGERSCHAFT, SACHBEARBEITER_TRAEGERSCHAFT, JURIST,
		REVISOR, ADMIN_TS, SACHBEARBEITER_TS, ADMIN_MANDANT, SACHBEARBEITER_MANDANT, ADMIN_SOZIALDIENST,
		SACHBEARBEITER_SOZIALDIENST })
	public JaxMitteilungen getMitteilungenOfDossierForCurrentRolle(
		@Nonnull @NotNull @PathParam("dossierId") JaxId jaxDossierId,
		@Context UriInfo uriInfo,
		@Context HttpServletResponse response) {

		Objects.requireNonNull(jaxDossierId.getId());
		String dossierId = converter.toEntityId(jaxDossierId);
		Optional<Dossier> dossier = dossierService.findDossier(dossierId);
		if (dossier.isPresent()) {
			final Collection<JaxMitteilung> convertedMitteilungen = new ArrayList<>();
			final Collection<Mitteilung> mitteilungen =
				mitteilungService.getMitteilungenForCurrentRolle(dossier.get());
			mitteilungen.forEach(mitteilung -> {
				if (mitteilung instanceof Betreuungsmitteilung) {
					convertedMitteilungen.add(converter.betreuungsmitteilungToJAX((Betreuungsmitteilung) mitteilung));
				} else {
					convertedMitteilungen.add(converter.mitteilungToJAX(mitteilung, new JaxMitteilung()));
				}
			});
			return new JaxMitteilungen(convertedMitteilungen); // We wrap the list to avoid loosing subtypes attributes
		}
		throw new EbeguEntityNotFoundException(
			"getMitteilungenForCurrentRolle",
			ErrorCodeEnum.ERROR_ENTITY_NOT_FOUND,
			FALL_ID_INVALID + jaxDossierId.getId());
	}

	@ApiOperation(value = "Gibt einen Wrapper mit der Liste aller Mitteilungen zurueck, welche fuer den eingeloggten"
		+ " " +
		"Benutzer fuer die uebergebene Betreuung vorhanden sind", response = JaxMitteilungen.class)
	@Nullable
	@GET
	@Path("/forrole/betreuung/{betreuungId}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed({ SUPER_ADMIN, ADMIN_BG, SACHBEARBEITER_BG, ADMIN_GEMEINDE, SACHBEARBEITER_GEMEINDE, GESUCHSTELLER,
		ADMIN_INSTITUTION, SACHBEARBEITER_INSTITUTION, ADMIN_TRAEGERSCHAFT, SACHBEARBEITER_TRAEGERSCHAFT, JURIST,
		REVISOR, ADMIN_TS, SACHBEARBEITER_TS, ADMIN_MANDANT, SACHBEARBEITER_MANDANT, ADMIN_SOZIALDIENST,
		SACHBEARBEITER_SOZIALDIENST })
	public JaxMitteilungen getMitteilungenForCurrentRolleForBetreuung(
		@Nonnull @NotNull @PathParam("betreuungId") JaxId betreuungId,
		@Context UriInfo uriInfo,
		@Context HttpServletResponse response) {

		Objects.requireNonNull(betreuungId.getId());
		String id = converter.toEntityId(betreuungId);
		Optional<Betreuung> betreuung = betreuungService.findBetreuung(id);
		if (betreuung.isPresent()) {
			final Collection<Mitteilung> mitteilungen =
				mitteilungService.getMitteilungenForCurrentRolle(betreuung.get());
			return new JaxMitteilungen(mitteilungen.stream().map(mitteilung ->
				converter.mitteilungToJAX(mitteilung, new JaxMitteilung())).collect(Collectors.toList()));
		}
		throw new EbeguEntityNotFoundException(
			"getMitteilungenForCurrentRolleForBetreuung",
			ErrorCodeEnum.ERROR_ENTITY_NOT_FOUND,
			"BetreuungID invalid: " + betreuungId.getId());
	}

	@ApiOperation(value = "Ermittelt die Anzahl neuer Mitteilungen im Posteingang des eingeloggten Benutzers",
		response = Integer.class)
	@Nullable
	@GET
	@Path("/amountnewforuser/notokenrefresh")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed({ SUPER_ADMIN, ADMIN_BG, SACHBEARBEITER_BG, ADMIN_GEMEINDE, SACHBEARBEITER_GEMEINDE, GESUCHSTELLER,
		ADMIN_INSTITUTION, ADMIN_MANDANT, SACHBEARBEITER_MANDANT, SACHBEARBEITER_INSTITUTION, ADMIN_TRAEGERSCHAFT,
		SACHBEARBEITER_TRAEGERSCHAFT, SACHBEARBEITER_TS, ADMIN_TS, ADMIN_SOZIALDIENST, SACHBEARBEITER_SOZIALDIENST })
	public Integer getAmountNewMitteilungenForCurrentBenutzer(
		@Context UriInfo uriInfo,
		@Context HttpServletResponse response) {

		return mitteilungService.getAmountNewMitteilungenForCurrentBenutzer().intValue();
	}

	@ApiOperation(value = "Loescht die Mitteilung mit der uebergebenen Id aus der Datenbank", response = Void.class)
	@SuppressWarnings("NonBooleanMethodNameMayNotStartWithQuestion")
	@Nullable
	@DELETE
	@Path("/{mitteilungId}")
	@Consumes(MediaType.WILDCARD)
	@RolesAllowed({ SUPER_ADMIN, ADMIN_BG, SACHBEARBEITER_BG, ADMIN_GEMEINDE, SACHBEARBEITER_GEMEINDE, ADMIN_TS,
		SACHBEARBEITER_TS, GESUCHSTELLER, ADMIN_INSTITUTION, SACHBEARBEITER_INSTITUTION, ADMIN_TRAEGERSCHAFT,
		SACHBEARBEITER_TRAEGERSCHAFT, ADMIN_SOZIALDIENST, SACHBEARBEITER_SOZIALDIENST })
	public Response removeMitteilung(
		@Nonnull @NotNull @PathParam("mitteilungId") JaxId mitteilungJAXPId,
		@Context HttpServletResponse response) {

		Objects.requireNonNull(mitteilungJAXPId.getId());
		Optional<Mitteilung> mitteilung = mitteilungService.findMitteilung(mitteilungJAXPId.getId());
		if (mitteilung.isPresent()) {
			mitteilungService.removeMitteilung(mitteilung.get());
			return Response.ok().build();
		}
		throw new EbeguEntityNotFoundException(
			"removeMitteilung",
			ErrorCodeEnum.ERROR_ENTITY_NOT_FOUND,
			"MitteilungID invalid: " + mitteilungJAXPId.getId());
	}

	@ApiOperation(value = "Setzt alle Mitteilungen des Dossiers mit der uebergebenen Id auf gelesen",
		response = JaxMitteilungen.class)
	@Nullable
	@PUT
	@Path("/setallgelesen/{dossierId}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed({ SUPER_ADMIN, ADMIN_BG, SACHBEARBEITER_BG, ADMIN_GEMEINDE, SACHBEARBEITER_GEMEINDE, GESUCHSTELLER,
		ADMIN_INSTITUTION, SACHBEARBEITER_INSTITUTION, ADMIN_TRAEGERSCHAFT, SACHBEARBEITER_TRAEGERSCHAFT,
		SACHBEARBEITER_TS, ADMIN_TS, ADMIN_SOZIALDIENST, SACHBEARBEITER_SOZIALDIENST })
	public JaxMitteilungen setAllNewMitteilungenOfDossierGelesen(
		@Nonnull @NotNull @PathParam("dossierId") JaxId jaxDossierId,
		@Context UriInfo uriInfo,
		@Context HttpServletResponse response) {

		Objects.requireNonNull(jaxDossierId.getId());
		String dossierId = converter.toEntityId(jaxDossierId);
		Optional<Dossier> dossier = dossierService.findDossier(dossierId);
		if (dossier.isPresent()) {
			final Collection<Mitteilung> mitteilungen =
				mitteilungService.setAllNewMitteilungenOfDossierGelesen(dossier.get());
			Collection<JaxMitteilung> convertedMitteilungen = new ArrayList<>();
			final Iterator<Mitteilung> iterator = mitteilungen.iterator();
			//noinspection WhileLoopReplaceableByForEach
			while (iterator.hasNext()) {
				convertedMitteilungen.add(converter.mitteilungToJAX(iterator.next(), new JaxMitteilung()));
			}
			return new JaxMitteilungen(convertedMitteilungen);
		}
		throw new EbeguEntityNotFoundException(
			"setAllNewMitteilungenOfDossierGelesen",
			ErrorCodeEnum.ERROR_ENTITY_NOT_FOUND,
			FALL_ID_INVALID + jaxDossierId.getId());
	}

	private Mitteilung readAndConvertMitteilung(@Nonnull JaxMitteilung mitteilungJAXP) {
		Mitteilung mitteilung = new Mitteilung();
		if (mitteilungJAXP.getId() != null) {
			final Optional<Mitteilung> optMitteilung = mitteilungService.findMitteilung(mitteilungJAXP.getId());
			mitteilung = optMitteilung.orElse(new Mitteilung());
		}

		return converter.mitteilungToEntity(mitteilungJAXP, mitteilung);
	}

	@ApiOperation(value = "Ermittelt die Anzahl neuer Mitteilungen fuer das uebergebene Dossier aller Benutzer in der "
		+ "Rolle des eingeloggten Benutzers",
		response = Integer.class)
	@Nullable
	@GET
	@Path("/amountnew/dossier/{dossierId}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed({ SUPER_ADMIN, ADMIN_BG, SACHBEARBEITER_BG, ADMIN_GEMEINDE, SACHBEARBEITER_GEMEINDE, GESUCHSTELLER,
		ADMIN_INSTITUTION, SACHBEARBEITER_INSTITUTION, ADMIN_TRAEGERSCHAFT, SACHBEARBEITER_TRAEGERSCHAFT, JURIST,
		REVISOR, ADMIN_TS, SACHBEARBEITER_TS, ADMIN_MANDANT, SACHBEARBEITER_MANDANT, ADMIN_SOZIALDIENST,
		SACHBEARBEITER_SOZIALDIENST })
	public Integer getAmountNewMitteilungenOfDossierForCurrentRolle(
		@Nonnull @NotNull @PathParam("dossierId") JaxId jaxDossierId,
		@Context UriInfo uriInfo,
		@Context HttpServletResponse response) {

		Objects.requireNonNull(jaxDossierId.getId());
		String dossierId = converter.toEntityId(jaxDossierId);
		Optional<Dossier> dossier = dossierService.findDossier(dossierId);
		if (dossier.isPresent()) {
			return mitteilungService.getNewMitteilungenOfDossierForCurrentRolle(dossier.get()).size();
		}
		throw new EbeguEntityNotFoundException(
			"getMitteilungenForCurrentRolle",
			ErrorCodeEnum.ERROR_ENTITY_NOT_FOUND,
			DOSSIER_ID_INVALID + jaxDossierId.getId());
	}

	@ApiOperation(value = "Weiterleiten der Mitteilung", response = JaxMitteilung.class)
	@Nonnull
	@GET
	@Path("/weiterleiten/{mitteilungId}/{userName}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed({ SUPER_ADMIN, ADMIN_BG, SACHBEARBEITER_BG, ADMIN_TS, SACHBEARBEITER_TS, ADMIN_GEMEINDE,
		SACHBEARBEITER_GEMEINDE })
	public JaxMitteilung mitteilungWeiterleiten(
		@Nonnull @NotNull @PathParam("mitteilungId") JaxId mitteilungJaxId,
		@Nonnull @NotNull @PathParam("userName") String username,
		@Context UriInfo uriInfo,
		@Context HttpServletResponse response
	) {
		Objects.requireNonNull(mitteilungJaxId.getId());
		String mitteilungId = converter.toEntityId(mitteilungJaxId);
		Mitteilung mitteilung = mitteilungService.mitteilungWeiterleiten(mitteilungId, username);
		return converter.mitteilungToJAX(mitteilung, new JaxMitteilung());
	}

	@ApiOperation(value = "Sucht Mitteilungen mit den uebergebenen Suchkriterien/Filtern",
		response = JaxMitteilungSearchresultDTO.class)
	@Nonnull
	@POST
	@Path("/search/{includeClosed}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed({ SUPER_ADMIN, ADMIN_BG, SACHBEARBEITER_BG, ADMIN_GEMEINDE, SACHBEARBEITER_GEMEINDE, GESUCHSTELLER,
		ADMIN_INSTITUTION, SACHBEARBEITER_INSTITUTION, ADMIN_TRAEGERSCHAFT, SACHBEARBEITER_TRAEGERSCHAFT, ADMIN_TS,
		SACHBEARBEITER_TS, ADMIN_SOZIALDIENST, SACHBEARBEITER_SOZIALDIENST })
	public Response searchMitteilungen(
		@Nonnull @PathParam("includeClosed") String includeClosed,
		@Nonnull @NotNull MitteilungTableFilterDTO tableFilterDTO,
		@Context UriInfo uriInfo,
		@Context HttpServletResponse response) {

		return MonitoringUtil.monitor(GesuchResource.class, "searchMitteilungen", () -> {
			Pair<Long, List<Mitteilung>> searchResultPair = mitteilungService
				.searchMitteilungen(tableFilterDTO, Boolean.valueOf(includeClosed));
			List<Mitteilung> foundMitteilungen = searchResultPair.getRight();

			List<JaxMitteilung> convertedMitteilungen = foundMitteilungen.stream().map(mitteilung ->
				converter.mitteilungToJAX(mitteilung, new JaxMitteilung())).collect(Collectors.toList());

			JaxMitteilungSearchresultDTO resultDTO = new JaxMitteilungSearchresultDTO();
			resultDTO.setMitteilungDTOs(convertedMitteilungen);
			PaginationDTO pagination = tableFilterDTO.getPagination();
			pagination.setTotalItemCount(searchResultPair.getLeft());
			resultDTO.setPaginationDTO(pagination);

			return Response.ok(resultDTO).build();
		});
	}

	@ApiOperation(value = "Wandelt BetreuungspensumAbweichungen in eine Mutationsmeldung um und sendet diese an die "
		+ "Gemeinde",
		response = JaxBetreuung.class)
	@Nullable
	@PUT
	@Path("/betreuung/abweichungenfreigeben/{betreuungId}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed({ SUPER_ADMIN, ADMIN_INSTITUTION, SACHBEARBEITER_INSTITUTION, ADMIN_TRAEGERSCHAFT,
		SACHBEARBEITER_TRAEGERSCHAFT })
	public List<JaxBetreuungspensumAbweichung> createMutationsmeldungFromBetreuungspensumAbweichungen(
		@Nonnull @NotNull @PathParam("betreuungId") JaxId jaxBetreuungId,
		@Nonnull @NotNull @Valid JaxBetreuungsmitteilung mitteilungJAXP,
		@Context UriInfo uriInfo,
		@Context HttpServletResponse response) {

		Objects.requireNonNull(mitteilungJAXP);
		Objects.requireNonNull(mitteilungJAXP);

		Optional<Betreuung> betreuungOpt = betreuungService.findBetreuung(converter.toEntityId(jaxBetreuungId));

		if (betreuungOpt.isEmpty()) {
			return null;
		}

		Betreuung betreuung = betreuungOpt.get();

		mitteilungService.removeOffeneBetreuungsmitteilungenForBetreuung(betreuung);

		if (betreuung.getBetreuungspensumAbweichungen() == null) {
			return null;
		}
		Betreuungsmitteilung mitteilung =
			converter.betreuungsmitteilungToEntity(mitteilungJAXP, new Betreuungsmitteilung());

		mitteilungService.createMutationsmeldungAbweichungen(mitteilung, betreuung);
		return converter.betreuungspensumAbweichungenToJax(betreuung);
	}

	@ApiOperation(value = "Uebernimmt eine neue Veranlagung Mitteilung in eine Mutation. Falls aktuell keine Mutation "
		+ "offen ist wird eine neue erstellt. Wenn den aktuellen Gesuch Status erlaubt die FinSit noch zu anpassen,"
		+ "dann wird die neue FinSit direkt angepasst." +
		"  Falls eine Mutation im Status VERFUEGEN vorhanden ist, oder die Mutation im " +
		"Status BESCHWERDE ist, wird ein Fehler zurueckgegeben", response = JaxId.class)
	@Nullable
	@PUT
	@Path("/neueVeranlagungsmitteilungBearbeiten/{neueveranlagungsmitteilungId}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed({ SUPER_ADMIN, ADMIN_BG, SACHBEARBEITER_BG, ADMIN_GEMEINDE, SACHBEARBEITER_GEMEINDE, ADMIN_TS, SACHBEARBEITER_TS })
	public JaxId neueVeranlagungsmitteilungBearbeiten(
		@Nonnull @NotNull @PathParam("neueveranlagungsmitteilungId") JaxId neueVeranlagungsmitteilungId,
		@Context UriInfo uriInfo,
		@Context HttpServletResponse response) {

		final Optional<NeueVeranlagungsMitteilung> mitteilung =
			mitteilungService.findVeranlagungsMitteilungById(neueVeranlagungsmitteilungId.getId());
		if (mitteilung.isEmpty()) {
			throw new EbeguEntityNotFoundException("neueVeranlagungsmitteilungBearbeiten", ErrorCodeEnum.ERROR_ENTITY_NOT_FOUND,
				"NeueVeranlagungsmitteilungId invalid: " + neueVeranlagungsmitteilungId.getId());
		}
		final Gesuch mutiertesGesuch = this.mitteilungService.neueVeranlagungssmitteilungBearbeiten(mitteilung.get());
		return converter.toJaxId(mutiertesGesuch);
	}
}
