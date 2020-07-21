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

import java.net.URI;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

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
import ch.dvbern.ebegu.api.dtos.JaxAlwaysEditableProperties;
import ch.dvbern.ebegu.api.dtos.JaxAntragSearchresultDTO;
import ch.dvbern.ebegu.api.dtos.JaxGesuch;
import ch.dvbern.ebegu.api.dtos.JaxId;
import ch.dvbern.ebegu.api.resource.util.ResourceHelper;
import ch.dvbern.ebegu.api.util.RestUtil;
import ch.dvbern.ebegu.authentication.PrincipalBean;
import ch.dvbern.ebegu.dto.JaxAntragDTO;
import ch.dvbern.ebegu.dto.personensuche.EWKResultat;
import ch.dvbern.ebegu.entities.Dossier;
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.entities.Gesuchsperiode;
import ch.dvbern.ebegu.entities.Institution;
import ch.dvbern.ebegu.enums.AntragStatus;
import ch.dvbern.ebegu.enums.AntragStatusDTO;
import ch.dvbern.ebegu.enums.ErrorCodeEnum;
import ch.dvbern.ebegu.enums.FinSitStatus;
import ch.dvbern.ebegu.enums.GesuchBetreuungenStatus;
import ch.dvbern.ebegu.enums.UserRole;
import ch.dvbern.ebegu.errors.EbeguEntityNotFoundException;
import ch.dvbern.ebegu.errors.EbeguException;
import ch.dvbern.ebegu.errors.EbeguRuntimeException;
import ch.dvbern.ebegu.services.DossierService;
import ch.dvbern.ebegu.services.GesuchService;
import ch.dvbern.ebegu.services.GesuchsperiodeService;
import ch.dvbern.ebegu.services.InstitutionService;
import ch.dvbern.ebegu.services.PensumAusserordentlicherAnspruchService;
import ch.dvbern.ebegu.services.PersonenSucheService;
import ch.dvbern.ebegu.util.AntragStatusConverterUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static ch.dvbern.ebegu.enums.UserRoleName.ADMIN_BG;
import static ch.dvbern.ebegu.enums.UserRoleName.ADMIN_GEMEINDE;
import static ch.dvbern.ebegu.enums.UserRoleName.ADMIN_INSTITUTION;
import static ch.dvbern.ebegu.enums.UserRoleName.ADMIN_MANDANT;
import static ch.dvbern.ebegu.enums.UserRoleName.ADMIN_TRAEGERSCHAFT;
import static ch.dvbern.ebegu.enums.UserRoleName.ADMIN_TS;
import static ch.dvbern.ebegu.enums.UserRoleName.GESUCHSTELLER;
import static ch.dvbern.ebegu.enums.UserRoleName.JURIST;
import static ch.dvbern.ebegu.enums.UserRoleName.REVISOR;
import static ch.dvbern.ebegu.enums.UserRoleName.SACHBEARBEITER_BG;
import static ch.dvbern.ebegu.enums.UserRoleName.SACHBEARBEITER_GEMEINDE;
import static ch.dvbern.ebegu.enums.UserRoleName.SACHBEARBEITER_INSTITUTION;
import static ch.dvbern.ebegu.enums.UserRoleName.SACHBEARBEITER_MANDANT;
import static ch.dvbern.ebegu.enums.UserRoleName.SACHBEARBEITER_TRAEGERSCHAFT;
import static ch.dvbern.ebegu.enums.UserRoleName.SACHBEARBEITER_TS;
import static ch.dvbern.ebegu.enums.UserRoleName.STEUERAMT;
import static ch.dvbern.ebegu.enums.UserRoleName.SUPER_ADMIN;

/**
 * Resource fuer Gesuch
 */
@Path("gesuche")
@Stateless
@Api(description = "Resource für Anträge (Erstgesuch oder Mutation)")
@DenyAll // Absichtlich keine Rolle zugelassen, erzwingt, dass es für neue Methoden definiert werden muss
public class GesuchResource {

	private static final Logger LOG = LoggerFactory.getLogger(GesuchResource.class);

	public static final String GESUCH_ID_INVALID = "GesuchId invalid: ";

	@Inject
	private GesuchService gesuchService;

	@Inject
	private GesuchsperiodeService gesuchsperiodeService;

	@Inject
	private InstitutionService institutionService;

	@Inject
	private DossierService dossierService;

	@Inject
	private PensumAusserordentlicherAnspruchService ausserordentlicherAnspruchService;

	@Inject
	private PrincipalBean principalBean;

	@Inject
	private JaxBConverter converter;

	@Inject
	private ResourceHelper resourceHelper;

	@Inject
	private PersonenSucheService personenSucheService;

	@ApiOperation(value = "Creates a new Antrag in the database. The transfer object also has a relation to " +
		"Familiensituation which is stored in the database as well.", response = JaxGesuch.class)
	@Nonnull
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed({ SUPER_ADMIN, ADMIN_BG, SACHBEARBEITER_BG, ADMIN_GEMEINDE, SACHBEARBEITER_GEMEINDE, GESUCHSTELLER,
		SACHBEARBEITER_TS, ADMIN_TS })
	public Response create(
		@Nonnull @NotNull JaxGesuch gesuchJAXP,
		@Context UriInfo uriInfo,
		@Context HttpServletResponse response) {

		Gesuch convertedGesuch = converter.gesuchToEntity(gesuchJAXP, new Gesuch());
		Gesuch persistedGesuch = this.gesuchService.createGesuch(convertedGesuch);

		URI uri = uriInfo.getBaseUriBuilder()
			.path(GesuchResource.class)
			.path('/' + persistedGesuch.getId())
			.build();

		JaxGesuch jaxGesuch = converter.gesuchToJAX(persistedGesuch);
		return Response.created(uri).entity(jaxGesuch).build();
	}

	@ApiOperation(value = "Updates a Antrag in the database", response = JaxGesuch.class)
	@Nullable
	@PUT
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed({SUPER_ADMIN, ADMIN_BG, SACHBEARBEITER_BG, ADMIN_GEMEINDE, SACHBEARBEITER_GEMEINDE, JURIST, REVISOR, ADMIN_TS, SACHBEARBEITER_TS,  STEUERAMT,
		GESUCHSTELLER, ADMIN_MANDANT, SACHBEARBEITER_MANDANT})
	public JaxGesuch update(
		@Nonnull @NotNull JaxGesuch gesuchJAXP,
		@Context UriInfo uriInfo,
		@Context HttpServletResponse response) {

		Objects.requireNonNull(gesuchJAXP.getId());
		Gesuch gesuchFromDB = gesuchService.findGesuch(gesuchJAXP.getId())
			.orElseThrow(() -> new EbeguEntityNotFoundException("update", ErrorCodeEnum.ERROR_ENTITY_NOT_FOUND,
				gesuchJAXP.getId()));

		//only if status has changed: Muss ermittelt werden, BEVOR wir mergen!
		final boolean saveInStatusHistory =
			gesuchFromDB.getStatus() != AntragStatusConverterUtil.convertStatusToEntity(gesuchJAXP.getStatus());
		Gesuch gesuchToMerge = converter.gesuchToEntity(gesuchJAXP, gesuchFromDB);
		Gesuch modifiedGesuch = this.gesuchService.updateGesuch(gesuchToMerge, saveInStatusHistory, null);
		return converter.gesuchToJAX(modifiedGesuch);
	}

	@ApiOperation(value = "Gibt den Antrag mit der uebergebenen Id zurueck. Dabei wird geprueft, ob der eingeloggte " +
		"Benutzer ueberhaupt fuer das Gesuch berechtigt ist.", response = JaxGesuch.class)
	@Nullable
	@GET
	@Path("/{gesuchId}")
	@Consumes(MediaType.WILDCARD)
	@Produces(MediaType.APPLICATION_JSON)
	@PermitAll // Grundsaetzliche fuer alle Rollen: Datenabhaengig. -> Authorizer
	public JaxGesuch findGesuch(
		@Nonnull @NotNull @PathParam("gesuchId") JaxId gesuchJAXPId) {
		Objects.requireNonNull(gesuchJAXPId.getId());
		String gesuchID = converter.toEntityId(gesuchJAXPId);
		Optional<Gesuch> gesuchOptional = gesuchService.findGesuch(gesuchID);

		if (!gesuchOptional.isPresent()) {
			return null;
		}
		Gesuch gesuchToReturn = gesuchOptional.get();
		final JaxGesuch jaxGesuch = converter.gesuchToJAX(gesuchToReturn);
		return jaxGesuch;
	}

	@ApiOperation(value = "Sucht eine Person im EWK nach Name, Vorname, Geburtsdatum und Geschlecht.",
		response = EWKResultat.class)
	@Nullable
	@GET
	@Path("/ewk/searchgesuch/{gesuchId}")
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed({ SUPER_ADMIN, ADMIN_BG, SACHBEARBEITER_BG, ADMIN_GEMEINDE, SACHBEARBEITER_GEMEINDE, ADMIN_TS, SACHBEARBEITER_TS, JURIST, REVISOR, ADMIN_MANDANT, SACHBEARBEITER_MANDANT })
	public EWKResultat suchePersonenByGesuch(
		@Nonnull @NotNull @PathParam("gesuchId") JaxId gesuchJAXPId) throws EbeguException {
		Objects.requireNonNull(gesuchJAXPId.getId());
		String gesuchID = converter.toEntityId(gesuchJAXPId);
		Optional<Gesuch> gesuchOptional = gesuchService.findGesuch(gesuchID);

		if (!gesuchOptional.isPresent()) {
			LOG.trace("Tried to trigger Search in Personensuche for non-exisiting gesuchId");
			return null;
		}
		Gesuch gesuch = gesuchOptional.get();
		return personenSucheService.suchePersonen(gesuch);
	}


	/**
	 * Da beim Einscannen Gesuche eingelesen werden die noch im Status Freigabequittung sind brauchen
	 * wir hier eine separate Methode um das Lesen der noetigen Informationen dieser Gesuche zuzulassen
	 * Wenn kein Gesuch gefunden wird wird null zurueckgegeben.
	 *
	 * @param gesuchJAXPId gesuchID des Gesuchs im Status Freigabequittung oder hoeher
	 * @return DTO mit den relevanten Informationen zum Gesuch
	 */
	@ApiOperation(value = "Gibt den Antrag mit der uebergebenen Id zurueck. Da beim Einscannen Gesuche eingelesen " +
		"werden die noch im Status Freigabequittung sind brauchen wir hier eine separate Methode um das Lesen der " +
		"noetigen Informationen dieser Gesuche zuzulassen.", response = JaxGesuch.class)
	@Nullable
	@GET
	@Path("/freigabe/{gesuchId}/{anzZurueckgezogen}")
	@Consumes(MediaType.WILDCARD)
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed({ ADMIN_BG, SUPER_ADMIN, SACHBEARBEITER_BG, ADMIN_GEMEINDE, SACHBEARBEITER_GEMEINDE, ADMIN_TS,
		SACHBEARBEITER_TS, GESUCHSTELLER })
	public JaxAntragDTO findGesuchForFreigabe(
		@Nonnull @NotNull @PathParam("gesuchId") JaxId gesuchJAXPId,
		@Nonnull @NotNull @PathParam("anzZurueckgezogen") String anzZurueckgezogen
	) {
		Objects.requireNonNull(gesuchJAXPId.getId());
		String gesuchID = converter.toEntityId(gesuchJAXPId);
		Integer zuruckgezogenAsInt = Integer.valueOf(anzZurueckgezogen);
		Gesuch gesuchToReturn = gesuchService.findGesuchForFreigabe(gesuchID, zuruckgezogenAsInt, true);

		JaxAntragDTO jaxAntragDTO = converter.gesuchToAntragDTO(gesuchToReturn,
			principalBean.discoverMostPrivilegedRole());

		jaxAntragDTO.setFamilienName(gesuchToReturn.extractFullnamesString()); //hier volle Namen beider GS
		return jaxAntragDTO;
	}

	/**
	 * Methode findGesuch fuer Benutzer mit Rolle SACHBEARBEITER_INSTITUTION, SACHBEARBEITER_TRAEGERSCHAFT oder
	 * SACHBEARBEITER_TS / ADMIN_TS. Das ganze Gesuch wird gefilter
	 * sodass nur die relevanten Daten zum Client geschickt werden.
	 *
	 * @param gesuchJAXPId ID des Gesuchs
	 * @return filtriertes Gesuch mit nur den relevanten Daten
	 */
	@ApiOperation(value = "Gibt den Antrag mit der uebergebenen Id zurueck. Methode fuer Benutzer mit Rolle " +
		"SACHBEARBEITER_INSTITUTION oder SACHBEARBEITER_TRAEGERSCHAFT. Das ganze Gesuch wird gefiltert so dass nur " +
		"die relevanten Daten zum Client geschickt werden.", response = JaxGesuch.class)
	@Nullable
	@GET
	@Path("/institution/{gesuchId}")
	@Consumes(MediaType.WILDCARD)
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed({SUPER_ADMIN, ADMIN_TRAEGERSCHAFT, SACHBEARBEITER_TRAEGERSCHAFT, ADMIN_INSTITUTION, SACHBEARBEITER_INSTITUTION})
	public JaxGesuch findGesuchForInstitution(
		@Nonnull @NotNull @PathParam("gesuchId") JaxId gesuchJAXPId) {

		final JaxGesuch completeGesuch = findGesuch(gesuchJAXPId);

		UserRole role = principalBean.discoverMostPrivilegedRole();
		if (role != null) {
			Collection<Institution> instForCurrBenutzer =
				institutionService.getInstitutionenReadableForCurrentBenutzer(false);
			return cleanGesuchForInstitutionTraegerschaft(completeGesuch, instForCurrBenutzer);
		}
		return null; // aus sicherheitsgruenden geben wir null zurueck wenn etwas nicht stimmmt
	}

	/**
	 * Nimmt das uebergebene Gesuch und entfernt alle Daten die fuer die Rollen SACHBEARBEITER_INSTITUTION oder
	 * SACHBEARBEITER_TRAEGERSCHAFT nicht
	 * relevant sind. Dieses Gesuch wird zurueckgeliefert
	 */
	@Nullable
	private JaxGesuch cleanGesuchForInstitutionTraegerschaft(@Nullable final JaxGesuch completeGesuch,
		final Collection<Institution> userInstitutionen) {
		if (completeGesuch != null) {
			//clean EKV
			completeGesuch.setEinkommensverschlechterungInfoContainer(null);

			//clean GS -> FinSit
			if (completeGesuch.getGesuchsteller1() != null) {
				completeGesuch.getGesuchsteller1().setEinkommensverschlechterungContainer(null);
				completeGesuch.getGesuchsteller1().setErwerbspensenContainers(null);
				completeGesuch.getGesuchsteller1().setFinanzielleSituationContainer(null);
			}
			if (completeGesuch.getGesuchsteller2() != null) {
				completeGesuch.getGesuchsteller2().setEinkommensverschlechterungContainer(null);
				completeGesuch.getGesuchsteller2().setErwerbspensenContainers(null);
				completeGesuch.getGesuchsteller2().setFinanzielleSituationContainer(null);
			}

			RestUtil.purgeKinderAndBetreuungenOfInstitutionen(completeGesuch.getKindContainers(), userInstitutionen);
		}
		return completeGesuch;
	}

	@ApiOperation("Aktualisiert die Bemerkungen fuer ein Gesuch.")
	@Nullable
	@PUT
	@Path("/bemerkung/{gesuchId}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed({SUPER_ADMIN, ADMIN_BG, SACHBEARBEITER_BG, ADMIN_GEMEINDE, SACHBEARBEITER_GEMEINDE, JURIST, REVISOR, ADMIN_TS, SACHBEARBEITER_TS,  STEUERAMT,
		GESUCHSTELLER, ADMIN_MANDANT, SACHBEARBEITER_MANDANT})
	public Response updateBemerkung(
		@Nonnull @NotNull @PathParam("gesuchId") JaxId gesuchJAXPId,
		@Nonnull @NotNull String bemerkung,
		@Context UriInfo uriInfo,
		@Context HttpServletResponse response) {

		Objects.requireNonNull(gesuchJAXPId.getId());
		Optional<Gesuch> gesuchOptional = gesuchService.findGesuch(converter.toEntityId(gesuchJAXPId));

		if (gesuchOptional.isPresent()) {
			gesuchOptional.get().setBemerkungen(bemerkung);

			gesuchService.updateGesuch(gesuchOptional.get(), false, null);

			return Response.ok().build();
		}
		throw new EbeguEntityNotFoundException("updateBemerkung", ErrorCodeEnum.ERROR_ENTITY_NOT_FOUND,
			GESUCH_ID_INVALID + gesuchJAXPId.getId());
	}

	@ApiOperation("Aktualisiert die Bemerkungen der Steuerbüro der Gemeinde fuer ein Gesuch.")
	@Nullable
	@PUT
	@Path("/bemerkungPruefungSTV/{gesuchId}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed({SUPER_ADMIN, ADMIN_BG, SACHBEARBEITER_BG, ADMIN_GEMEINDE, SACHBEARBEITER_GEMEINDE, JURIST, REVISOR, ADMIN_TS, SACHBEARBEITER_TS,  STEUERAMT,
		GESUCHSTELLER, ADMIN_MANDANT, SACHBEARBEITER_MANDANT})
	public Response updateBemerkungPruefungSTV(
		@Nonnull @NotNull @PathParam("gesuchId") JaxId gesuchJAXPId,
		@Nonnull @NotNull String bemerkungPruefungSTV,
		@Context UriInfo uriInfo,
		@Context HttpServletResponse response) {

		Objects.requireNonNull(gesuchJAXPId.getId());
		Optional<Gesuch> gesuchOptional = gesuchService.findGesuch(converter.toEntityId(gesuchJAXPId));

		if (gesuchOptional.isPresent()) {
			gesuchOptional.get().setBemerkungenPruefungSTV(bemerkungPruefungSTV);

			gesuchService.updateGesuch(gesuchOptional.get(), false, null);

			return Response.ok().build();
		}
		throw new EbeguEntityNotFoundException("updateBemerkungPruefungSTV", ErrorCodeEnum.ERROR_ENTITY_NOT_FOUND,
			GESUCH_ID_INVALID + gesuchJAXPId.getId());
	}

	@ApiOperation("Aktualisiert den Status eines Gesuchs")
	@Nullable
	@PUT
	@Path("/status/{gesuchId}/{statusDTO}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed({SUPER_ADMIN, ADMIN_BG, SACHBEARBEITER_BG, ADMIN_GEMEINDE, SACHBEARBEITER_GEMEINDE, JURIST, REVISOR, ADMIN_TS, SACHBEARBEITER_TS,  STEUERAMT,
		GESUCHSTELLER, ADMIN_MANDANT, SACHBEARBEITER_MANDANT})
	public Response updateStatus(
		@Nonnull @NotNull @PathParam("gesuchId") JaxId gesuchJAXPId,
		@Nonnull @NotNull @PathParam("statusDTO") AntragStatusDTO statusDTO) {

		Objects.requireNonNull(gesuchJAXPId.getId());
		Objects.requireNonNull(statusDTO);
		Optional<Gesuch> gesuchOptional = gesuchService.findGesuch(converter.toEntityId(gesuchJAXPId));

		if (gesuchOptional.isPresent()) {
			if (gesuchOptional.get().getStatus() != AntragStatusConverterUtil.convertStatusToEntity(statusDTO)) {
				//only if status has changed
				gesuchOptional.get().setStatus(AntragStatusConverterUtil.convertStatusToEntity(statusDTO));
				gesuchService.updateGesuch(gesuchOptional.get(), true, null);
			}
			return Response.ok().build();
		}
		String message = String.format("Could not update Status because the Gesuch with ID %s could not be read",
			gesuchJAXPId.getId());
		throw new EbeguEntityNotFoundException("updateStatus", message, ErrorCodeEnum.ERROR_ENTITY_NOT_FOUND,
			GESUCH_ID_INVALID + gesuchJAXPId.getId());
	}

	@ApiOperation(value = "Gibt alle Antraege (Gesuche und Mutationen) eines Falls zurueck",
		responseContainer = "List", response = JaxAntragDTO.class)
	@Nonnull
	@GET
	@Path("/dossier/{dossierId}")
	@Consumes(MediaType.WILDCARD)
	@Produces(MediaType.APPLICATION_JSON)
	@PermitAll // Grundsaetzliche fuer alle Rollen: Datenabhaengig. -> Authorizer
	public List<JaxAntragDTO> getAllAntragDTOForDossier(
		@Nonnull @NotNull @PathParam("dossierId") JaxId dossierJAXPId) {
		Objects.requireNonNull(dossierJAXPId.getId());
		return gesuchService.getAllAntragDTOForDossier(converter.toEntityId(dossierJAXPId));
	}

	@ApiOperation(value = "Gibt den Antrag frei und bereitet ihn vor für die Bearbeitung durch das Jugendamt",
		response = JaxGesuch.class)
	@Nullable
	@POST
	@Path("/freigeben/{antragId}/JA/{usernameJA}/SCH/{usernameSCH}")
	@Consumes(MediaType.WILDCARD)
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed({ ADMIN_BG, SUPER_ADMIN, SACHBEARBEITER_BG, ADMIN_GEMEINDE, SACHBEARBEITER_GEMEINDE, ADMIN_TS,
		SACHBEARBEITER_TS, GESUCHSTELLER })
	public Response antragFreigeben(
		@Nonnull @NotNull @PathParam("antragId") JaxId antragJaxId,
		@Nullable @PathParam("usernameJA") String usernameJA,
		@Nullable @PathParam("usernameSCH") String usernameSCH,
		@Context UriInfo uriInfo,
		@Context HttpServletResponse response) {

		// Sicherstellen, dass der Status des Client-Objektes genau dem des Servers entspricht
		resourceHelper.assertGesuchStatusForFreigabe(antragJaxId.getId());

		Objects.requireNonNull(antragJaxId.getId());

		final String antragId = converter.toEntityId(antragJaxId);

		Gesuch gesuch = gesuchService.antragFreigeben(antragId, usernameJA, usernameSCH);
		return Response.ok(converter.gesuchToJAX(gesuch)).build();
	}

	@ApiOperation(value = "Zieht einen freigegebenen Online Antrag zurück und versetzt ihn in den Status In "
		+ "Bearbeitung Gesuchsteller",
		response = JaxGesuch.class)
	@Nullable
	@POST
	@Path("/zurueckziehen/{antragId}")
	@Consumes(MediaType.WILDCARD)
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed({ SUPER_ADMIN, GESUCHSTELLER })
	public Response antraZurueckziehen(
		@Nonnull @NotNull @PathParam("antragId") JaxId antragJaxId,
		@Context UriInfo uriInfo,
		@Context HttpServletResponse response) {

		// Sicherstellen, dass der Status des Client-Objektes genau dem des Servers entspricht
		resourceHelper.assertGesuchStatusForFreigabe(antragJaxId.getId());

		Objects.requireNonNull(antragJaxId.getId());

		final String antragId = converter.toEntityId(antragJaxId);

		Gesuch gesuch = gesuchService.antragZurueckziehen(antragId);
		return Response.ok(converter.gesuchToJAX(gesuch)).build();
	}

	@ApiOperation(value = "Setzt das gegebene Gesuch als Beschwerde haengig und bei allen Gesuchen der Periode das " +
		"Flag gesperrtWegenBeschwerde auf true", response = JaxGesuch.class)
	@Nullable
	@POST
	@Path("/setBeschwerde/{antragId}")
	@Consumes(MediaType.WILDCARD)
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed({ ADMIN_BG, SUPER_ADMIN, SACHBEARBEITER_BG, ADMIN_GEMEINDE, SACHBEARBEITER_GEMEINDE,
		SACHBEARBEITER_TS, ADMIN_TS })
	public Response setBeschwerdeHaengig(
		@Nonnull @NotNull @PathParam("antragId") JaxId antragJaxId,
		@Context UriInfo uriInfo,
		@Context HttpServletResponse response) {

		Objects.requireNonNull(antragJaxId.getId());
		final String antragId = converter.toEntityId(antragJaxId);
		Optional<Gesuch> gesuch = gesuchService.findGesuch(antragId);

		resourceHelper.assertGesuchStatusEqual(antragId, AntragStatusDTO.VERFUEGT, AntragStatusDTO.PRUEFUNG_STV,
			AntragStatusDTO.IN_BEARBEITUNG_STV, AntragStatusDTO.GEPRUEFT_STV, AntragStatusDTO.KEIN_ANGEBOT,
			AntragStatusDTO.NUR_SCHULAMT);

		if (gesuch.isPresent()) {
			Gesuch persistedGesuch = gesuchService.setBeschwerdeHaengigForPeriode(gesuch.get());
			return Response.ok(converter.gesuchToJAX(persistedGesuch)).build();
		}
		throw new EbeguEntityNotFoundException("setBeschwerdeHaengig", ErrorCodeEnum.ERROR_ENTITY_NOT_FOUND,
			GESUCH_ID_INVALID + antragJaxId.getId());
	}

	@ApiOperation(value = "Setzt das gegebene Gesuch als Abgeschossen (Status NUR_SCHULAMT)", response =
		JaxGesuch.class)
	@Nullable
	@POST
	@Path("/setAbschliessen/{antragId}")
	@Consumes(MediaType.WILDCARD)
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed({ ADMIN_BG, SUPER_ADMIN, SACHBEARBEITER_BG, ADMIN_GEMEINDE, SACHBEARBEITER_GEMEINDE,
		SACHBEARBEITER_TS, ADMIN_TS })
	public Response setAbschliessen(
		@Nonnull @NotNull @PathParam("antragId") JaxId antragJaxId,
		@Context UriInfo uriInfo,
		@Context HttpServletResponse response) {

		Objects.requireNonNull(antragJaxId.getId());
		final String antragId = converter.toEntityId(antragJaxId);
		Optional<Gesuch> gesuch = gesuchService.findGesuch(antragId);

		if (gesuch.isPresent()) {
			resourceHelper.assertGesuchStatusEqual(antragId, AntragStatusDTO.IN_BEARBEITUNG_JA,
				AntragStatusDTO.GEPRUEFT);
			Gesuch persistedGesuch = gesuchService.setAbschliessen(gesuch.get());
			final JaxGesuch jaxGesuch = converter.gesuchToJAX(persistedGesuch);
			return Response.ok(jaxGesuch).build();
		}
		throw new EbeguEntityNotFoundException("setAbschliessen", ErrorCodeEnum.ERROR_ENTITY_NOT_FOUND,
			GESUCH_ID_INVALID + antragJaxId.getId());
	}

	@ApiOperation(value = "Setzt das gegebene Gesuch als PRUEFUNG_STV", response = JaxGesuch.class)
	@Nullable
	@POST
	@Path("/sendToSTV/{antragId}")
	@Consumes(MediaType.WILDCARD)
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed({ ADMIN_BG, SUPER_ADMIN, SACHBEARBEITER_BG, ADMIN_GEMEINDE, SACHBEARBEITER_GEMEINDE,
		SACHBEARBEITER_TS, ADMIN_TS })
	public Response sendGesuchToSTV(
		@Nonnull @NotNull @PathParam("antragId") JaxId antragJaxId,
		@Nullable String bemerkungen,
		@Context UriInfo uriInfo,
		@Context HttpServletResponse response) {

		// Sicherstellen, dass der Status des Client-Objektes genau dem des Servers entspricht
		resourceHelper.assertGesuchStatusEqual(antragJaxId.getId(), AntragStatusDTO.VERFUEGT,
			AntragStatusDTO.NUR_SCHULAMT);

		Objects.requireNonNull(antragJaxId.getId());
		final String antragId = converter.toEntityId(antragJaxId);
		Optional<Gesuch> gesuchOptional = gesuchService.findGesuch(antragId);

		if (!gesuchOptional.isPresent()) {
			throw new EbeguEntityNotFoundException("sendGesuchToSTV", ErrorCodeEnum.ERROR_ENTITY_NOT_FOUND,
				GESUCH_ID_INVALID + antragJaxId.getId());
		}
		Gesuch gesuch = gesuchOptional.get();
		Gesuch persistedGesuch = gesuchService.sendGesuchToSTV(gesuch, bemerkungen);
		return Response.ok(converter.gesuchToJAX(persistedGesuch)).build();
	}

	@ApiOperation(value = "Setzt das gegebene Gesuch als GEPRUEFT_STV und das Flag geprueftSTV als true",
		response = JaxGesuch.class)
	@Nullable
	@POST
	@Path("/freigebenSTV/{antragId}")
	@Consumes(MediaType.WILDCARD)
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed({ STEUERAMT, SUPER_ADMIN})
	public Response gesuchBySTVFreigeben(
		@Nonnull @NotNull @PathParam("antragId") JaxId antragJaxId,
		@Context UriInfo uriInfo,
		@Context HttpServletResponse response) {

		// Sicherstellen, dass der Status des Client-Objektes genau dem des Servers entspricht
		resourceHelper.assertGesuchStatusEqual(antragJaxId.getId(), AntragStatusDTO.IN_BEARBEITUNG_STV);

		Objects.requireNonNull(antragJaxId.getId());
		final String antragId = converter.toEntityId(antragJaxId);
		Optional<Gesuch> gesuch = gesuchService.findGesuch(antragId);

		if (!gesuch.isPresent()) {
			throw new EbeguEntityNotFoundException("gesuchBySTVFreigeben", ErrorCodeEnum.ERROR_ENTITY_NOT_FOUND,
				GESUCH_ID_INVALID + antragJaxId.getId());
		}

		Gesuch persistedGesuch = gesuchService.gesuchBySTVFreigeben(gesuch.get());
		return Response.ok(converter.gesuchToJAX(persistedGesuch)).build();

	}

	@ApiOperation(value = "Setzt das gegebene Gesuch als VERFUEGT",
		response = JaxGesuch.class)
	@Nullable
	@POST
	@Path("/stvPruefungAbschliessen/{antragId}")
	@Consumes(MediaType.WILDCARD)
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed({ ADMIN_BG, SUPER_ADMIN, SACHBEARBEITER_BG, ADMIN_GEMEINDE, SACHBEARBEITER_GEMEINDE,
		SACHBEARBEITER_TS, ADMIN_TS })
	public Response stvPruefungAbschliessen(
		@Nonnull @NotNull @PathParam("antragId") JaxId antragJaxId,
		@Context UriInfo uriInfo,
		@Context HttpServletResponse response) {

		// Sicherstellen, dass der Status des Client-Objektes genau dem des Servers entspricht
		resourceHelper.assertGesuchStatusEqual(antragJaxId.getId(), AntragStatusDTO.GEPRUEFT_STV, AntragStatusDTO.PRUEFUNG_STV);

		Objects.requireNonNull(antragJaxId.getId());
		final String antragId = converter.toEntityId(antragJaxId);
		Optional<Gesuch> gesuchOptional = gesuchService.findGesuch(antragId);

		Gesuch gesuch = gesuchOptional.orElseThrow(() -> new EbeguEntityNotFoundException("stvPruefungAbschliessen",
			ErrorCodeEnum.ERROR_ENTITY_NOT_FOUND, GESUCH_ID_INVALID + antragJaxId.getId()));

		if (AntragStatus.GEPRUEFT_STV != gesuch.getStatus() && AntragStatus.PRUEFUNG_STV != gesuch.getStatus()) {
			// Wir vergewissern uns dass das Gesuch im Status IN_BEARBEITUNG_STV ist, da sonst kann es nicht fuer das
			// JA freigegeben werden
			throw new EbeguRuntimeException("stvPruefungAbschliessen",
				ErrorCodeEnum.ERROR_ONLY_IN_PRUEFUNG_GEPRUEFT_STV_ALLOWED, "Status ist: " + gesuch.getStatus());
		}

		Gesuch persistedGesuch = gesuchService.stvPruefungAbschliessen(gesuch);
		return Response.ok(converter.gesuchToJAX(persistedGesuch)).build();

	}

	@ApiOperation(value = "Setzt das gegebene Gesuch als VERFUEGT und bei allen Gescuhen der Periode den Flag " +
		"gesperrtWegenBeschwerde auf false", response = JaxGesuch.class)
	@SuppressWarnings("NonBooleanMethodNameMayNotStartWithQuestion")
	@Nullable
	@POST
	@Path("/removeBeschwerde/{antragId}")
	@Consumes(MediaType.WILDCARD)
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed({ ADMIN_BG, SUPER_ADMIN, SACHBEARBEITER_BG, ADMIN_GEMEINDE, SACHBEARBEITER_GEMEINDE,
		SACHBEARBEITER_TS, ADMIN_TS })
	public Response removeBeschwerdeHaengig(
		@Nonnull @NotNull @PathParam("antragId") JaxId antragJaxId,
		@Context UriInfo uriInfo,
		@Context HttpServletResponse response) {

		// Sicherstellen, dass der Status des Client-Objektes genau dem des Servers entspricht
		resourceHelper.assertGesuchStatusEqual(antragJaxId.getId(), AntragStatusDTO.BESCHWERDE_HAENGIG);

		Objects.requireNonNull(antragJaxId.getId());
		final String antragId = converter.toEntityId(antragJaxId);
		Optional<Gesuch> gesuch = gesuchService.findGesuch(antragId);

		if (gesuch.isPresent()) {
			Gesuch persistedGesuch = gesuchService.removeBeschwerdeHaengigForPeriode(gesuch.get());
			return Response.ok(converter.gesuchToJAX(persistedGesuch)).build();
		}
		throw new EbeguEntityNotFoundException("removeBeschwerdeHaengig", ErrorCodeEnum.ERROR_ENTITY_NOT_FOUND,
			GESUCH_ID_INVALID + antragJaxId.getId());
	}

	@ApiOperation("Loescht eine online Mutation")
	@SuppressWarnings("NonBooleanMethodNameMayNotStartWithQuestion")
	@DELETE
	@Path("/removeOnlineMutation/{dossierId}/{gesuchsperiodeId}")
	@Consumes(MediaType.WILDCARD)
	@RolesAllowed({ ADMIN_BG, ADMIN_GEMEINDE, ADMIN_TS, SUPER_ADMIN })
	public Response removeOnlineMutation(
		@Nonnull @NotNull @PathParam("dossierId") JaxId dossierId,
		@Nonnull @NotNull @PathParam("gesuchsperiodeId") JaxId gesuchsperiodeId,
		@Context HttpServletResponse response) {

		Objects.requireNonNull(dossierId.getId());
		Objects.requireNonNull(gesuchsperiodeId.getId());
		Optional<Dossier> dossier = dossierService.findDossier(dossierId.getId());
		if (!dossier.isPresent()) {
			throw new EbeguEntityNotFoundException("removeOnlineMutation", ErrorCodeEnum.ERROR_ENTITY_NOT_FOUND,
				"Dossier_ID invalid " + dossierId.getId());
		}
		Optional<Gesuchsperiode> gesuchsperiode = gesuchsperiodeService.findGesuchsperiode(gesuchsperiodeId.getId());
		if (!gesuchsperiode.isPresent()) {
			throw new EbeguEntityNotFoundException("removeOnlineMutation", ErrorCodeEnum.ERROR_ENTITY_NOT_FOUND,
				"Gesuchsperiode_ID invalid " + gesuchsperiodeId.getId());
		}
		gesuchService.removeOnlineMutation(dossier.get(), gesuchsperiode.get());

		return Response.ok().build();
	}

	@ApiOperation("Loescht ein online Erneuerungsgesuch")
	@SuppressWarnings("NonBooleanMethodNameMayNotStartWithQuestion")
	@DELETE
	@Path("/removeOnlineFolgegesuch/{dossierId}/{gesuchsperiodeId}")
	@Consumes(MediaType.WILDCARD)
	@RolesAllowed({ ADMIN_BG, ADMIN_GEMEINDE, ADMIN_TS, SUPER_ADMIN })
	public Response removeOnlineFolgegesuch(
		@Nonnull @NotNull @PathParam("dossierId") JaxId dossierJAXPId,
		@Nonnull @NotNull @PathParam("gesuchsperiodeId") JaxId gesuchsperiodeJAXPId,
		@Context HttpServletResponse response) {

		Objects.requireNonNull(dossierJAXPId.getId());
		Objects.requireNonNull(gesuchsperiodeJAXPId.getId());

		Optional<Dossier> dossier = dossierService.findDossier(dossierJAXPId.getId());
		if (!dossier.isPresent()) {
			throw new EbeguEntityNotFoundException("removeOnlineFolgegesuch", ErrorCodeEnum.ERROR_ENTITY_NOT_FOUND,
				"Dossier_ID invalid " + dossierJAXPId.getId());
		}
		Optional<Gesuchsperiode> gesuchsperiode =
			gesuchsperiodeService.findGesuchsperiode(gesuchsperiodeJAXPId.getId());
		if (!gesuchsperiode.isPresent()) {
			throw new EbeguEntityNotFoundException("removeOnlineFolgegesuch", ErrorCodeEnum.ERROR_ENTITY_NOT_FOUND,
				"GesuchsperiodeId invalid: " + gesuchsperiodeJAXPId.getId());
		}
		gesuchService.removeOnlineFolgegesuch(dossier.get(), gesuchsperiode.get());

		return Response.ok().build();
	}

	@DELETE
	@Path("/removeAntrag/{gesuchId}")
	@Consumes(MediaType.WILDCARD)
	@RolesAllowed({ GESUCHSTELLER, ADMIN_BG, ADMIN_GEMEINDE, ADMIN_TS, SUPER_ADMIN })
	public Response removeAntrag(
		@Nonnull @NotNull @PathParam("gesuchId") JaxId gesuchJaxId,
		@Context HttpServletResponse response) {

		Objects.requireNonNull(gesuchJaxId.getId());

		Gesuch gesuch = gesuchService.findGesuch(gesuchJaxId.getId(), true).orElseThrow(()
			-> new EbeguEntityNotFoundException("removeAntrag", ErrorCodeEnum.ERROR_ENTITY_NOT_FOUND, "GesuchId "
			+ "invalid: " + gesuchJaxId.getId()));

		gesuchService.removeAntrag(gesuch);
		return Response.ok().build();
	}

	@DELETE
	@Path("/removeAntragForced/{gesuchId}")
	@Consumes(MediaType.WILDCARD)
	@RolesAllowed(SUPER_ADMIN)
	public Response removeAntragForced(
		@Nonnull @NotNull @PathParam("gesuchId") JaxId gesuchJaxId,
		@Context HttpServletResponse response) {

		Objects.requireNonNull(gesuchJaxId.getId());

		Gesuch gesuch = gesuchService.findGesuch(gesuchJaxId.getId(), true).orElseThrow(()
			-> new EbeguEntityNotFoundException("removeAntragForced", ErrorCodeEnum.ERROR_ENTITY_NOT_FOUND, "GesuchId "
			+ "invalid: " + gesuchJaxId.getId()));

		gesuchService.removeAntragForced(gesuch);
		return Response.ok().build();
	}

	@ApiOperation(value = "Schliesst ein Gesuch ab, das kein Angebot hat", response = JaxGesuch.class)
	@Nullable
	@POST
	@Path("/closeWithoutAngebot/{antragId}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed({ SUPER_ADMIN, ADMIN_BG, SACHBEARBEITER_BG, ADMIN_GEMEINDE, SACHBEARBEITER_GEMEINDE })
	public Response closeWithoutAngebot(
		@Nonnull @NotNull @PathParam("antragId") JaxId antragJaxId,
		@Context UriInfo uriInfo,
		@Context HttpServletResponse response) {

		// Sicherstellen, dass der Status des Client-Objektes genau dem des Servers entspricht
		resourceHelper.assertGesuchStatusEqual(antragJaxId.getId(), AntragStatusDTO.GEPRUEFT);

		Objects.requireNonNull(antragJaxId.getId());
		final String antragId = converter.toEntityId(antragJaxId);
		Optional<Gesuch> gesuchOptional = gesuchService.findGesuch(antragId);
		if (!gesuchOptional.isPresent()) {
			throw new EbeguEntityNotFoundException("closeWithoutAngebot", ErrorCodeEnum.ERROR_ENTITY_NOT_FOUND,
				GESUCH_ID_INVALID + antragId);
		}

		Gesuch closedGesuch = gesuchService.closeWithoutAngebot(gesuchOptional.get());

		return Response.ok(converter.gesuchToJAX(closedGesuch)).build();
	}

	@ApiOperation(value = "Aendert den Status des Gesuchs auf VERFUEGEN. Sollte es nur Schulangebote geben, dann " +
		"wechselt der Status auf NUR_SCHULAMT", response = JaxGesuch.class)
	@Nullable
	@POST
	@Path("/verfuegenStarten/{antragId}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed({ SUPER_ADMIN, ADMIN_BG, SACHBEARBEITER_BG, ADMIN_GEMEINDE, SACHBEARBEITER_GEMEINDE })
	public Response verfuegenStarten(
		@Nonnull @NotNull @PathParam("antragId") JaxId antragJaxId,
		@Context UriInfo uriInfo,
		@Context HttpServletResponse response) {

		// Sicherstellen, dass der Status des Client-Objektes genau dem des Servers entspricht
		resourceHelper.assertGesuchStatusEqual(antragJaxId.getId(), AntragStatusDTO.GEPRUEFT);

		Objects.requireNonNull(antragJaxId.getId());
		final String antragId = converter.toEntityId(antragJaxId);

		final Gesuch gesuch = gesuchService.findGesuch(antragId).orElseThrow(() ->
			new EbeguEntityNotFoundException("verfuegenStarten", ErrorCodeEnum.ERROR_ENTITY_NOT_FOUND,
				GESUCH_ID_INVALID + antragId)
		);
		Gesuch closedGesuch = gesuchService.verfuegenStarten(gesuch);

		return Response.ok(converter.gesuchToJAX(closedGesuch)).build();
	}

	@ApiOperation(value = "Ermittelt den Gesamtstatus aller Betreuungen des Gesuchs mit der uebergebenen Id.",
		response = GesuchBetreuungenStatus.class)
	@Nullable
	@GET
	@Path("/gesuchBetreuungenStatus/{gesuchId}")
	@Consumes(MediaType.WILDCARD)
	@Produces(MediaType.APPLICATION_JSON)
	@PermitAll // Grundsaetzliche fuer alle Rollen: Datenabhaengig. -> Authorizer
	public Response findGesuchBetreuungenStatus(
		@Nonnull @NotNull @PathParam("gesuchId") JaxId gesuchJAXPId) {

		Objects.requireNonNull(gesuchJAXPId.getId());
		Optional<Gesuch> gesuchOptional = gesuchService.findGesuch(converter.toEntityId(gesuchJAXPId));
		if (!gesuchOptional.isPresent()) {
			throw new EbeguEntityNotFoundException("findGesuchBetreuungenStatus", ErrorCodeEnum
				.ERROR_ENTITY_NOT_FOUND, GESUCH_ID_INVALID + gesuchJAXPId.getId());
		}
		Gesuch gesuchToReturn = gesuchOptional.get();
		return Response.ok(gesuchToReturn.getGesuchBetreuungenStatus()).build();
	}

	@ApiOperation(value = "verfuegt das gegebene Gesuch. Funktioniert nur bei Gesuchen, bei denen alle Betreuungen "
		+ "verfügt sind, der Status"
		+ " vom Gesuch aber noch nicht als VERFUEGT gesetzt wurde.", response = JaxAntragSearchresultDTO.class)
	@Nonnull
	@POST
	@Path("/gesuchVerfuegen/{gesuchId}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed({ SUPER_ADMIN, ADMIN_BG, SACHBEARBEITER_BG, ADMIN_GEMEINDE, SACHBEARBEITER_GEMEINDE })
	public Response gesuchVerfuegen(
		@Nonnull @NotNull @PathParam("gesuchId") JaxId gesuchJAXPId) {

		Objects.requireNonNull(gesuchJAXPId.getId());
		Optional<Gesuch> gesuchOptional = gesuchService.findGesuch(converter.toEntityId(gesuchJAXPId));
		if (!gesuchOptional.isPresent()) {
			throw new EbeguEntityNotFoundException("gesuchVerfuegen", ErrorCodeEnum
				.ERROR_ENTITY_NOT_FOUND, GESUCH_ID_INVALID + gesuchJAXPId.getId());
		}
		gesuchService.gesuchVerfuegen(gesuchOptional.get());
		return Response.ok().build();
	}

	@ApiOperation(value = "Aendert den FinSitStatus im Gesuch", response = JaxGesuch.class)
	@Nonnull
	@POST
	@Path("/changeFinSitStatus/{antragId}/{finSitStatus}")
	@Consumes(MediaType.WILDCARD)
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed({ ADMIN_BG, SUPER_ADMIN, SACHBEARBEITER_BG, ADMIN_GEMEINDE, SACHBEARBEITER_GEMEINDE,
		SACHBEARBEITER_TS, ADMIN_TS })
	public Response changeFinSitStatus(
		@Nonnull @NotNull @PathParam("antragId") JaxId antragJaxId,
		@Nonnull @NotNull @PathParam("finSitStatus") FinSitStatus finSitStatus,
		@Context UriInfo uriInfo,
		@Context HttpServletResponse response) {

		Objects.requireNonNull(antragJaxId.getId());
		final String antragId = converter.toEntityId(antragJaxId);

		if (gesuchService.changeFinSitStatus(antragId, finSitStatus) == 1) {
			return Response.ok().build();
		}
		throw new EbeguEntityNotFoundException("changeFinSitStatus", ErrorCodeEnum
			.ERROR_ENTITY_NOT_FOUND, GESUCH_ID_INVALID + antragJaxId.getId());

	}

	@SuppressWarnings("NonBooleanMethodNameMayNotStartWithQuestion")
	@ApiOperation(value = "Ermittelt ob das uebergebene Gesuch das neuestes dieses Falls und Jahres ist.", response =
		Boolean.class)
	@Nullable
	@GET
	@Path("/newest/{gesuchId}")
	@Consumes(MediaType.WILDCARD)
	@Produces(MediaType.APPLICATION_JSON)
	@PermitAll // Grundsaetzliche fuer alle Rollen: Datenabhaengig. -> Authorizer
	public Response isNeuestesGesuch(@Nonnull @NotNull @PathParam("gesuchId") JaxId gesuchJAXPId) {
		Objects.requireNonNull(gesuchJAXPId.getId());
		Gesuch gesuch = gesuchService.findGesuch(converter.toEntityId(gesuchJAXPId))
			.orElseThrow(() -> new EbeguEntityNotFoundException("isNeuestesGesuch",
				ErrorCodeEnum.ERROR_ENTITY_NOT_FOUND, gesuchJAXPId.getId()));
		Boolean neustesGesuch = gesuchService.isNeustesGesuch(gesuch);
		return Response.ok(neustesGesuch).build();
	}

	@ApiOperation(value = "Gibt die ID des neuesten Gesuchs dieses Falls und Jahres zurueck. Wenn es noch keinen Fall,"
		+ " kein Gesuch oder keine Gesuchsperiode "
		+ "gibt, wird null zurueckgegeben", response = String.class)
	@Nonnull
	@GET
	@Path("/newestid/gesuchsperiode/{gesuchsperiodeId}/dossier/{dossierId}")
	@Consumes(MediaType.WILDCARD)
	@Produces(MediaType.TEXT_PLAIN)
	@PermitAll // Grundsaetzliche fuer alle Rollen: Datenabhaengig. -> Authorizer
	public Response getIdOfNewestGesuch(@Nonnull @NotNull @PathParam("gesuchsperiodeId") JaxId gesuchsperiodeJaxId,
		@Nonnull @NotNull @PathParam("dossierId") JaxId dossierJaxId) {

		Objects.requireNonNull(dossierJaxId.getId());
		Objects.requireNonNull(gesuchsperiodeJaxId.getId());

		Optional<Dossier> dossier = dossierService.findDossier(dossierJaxId.getId());
		Optional<Gesuchsperiode> gesuchsperiode =
			gesuchsperiodeService.findGesuchsperiode(gesuchsperiodeJaxId.getId());

		if (!dossier.isPresent()) {
			throw new EbeguEntityNotFoundException("getIdOfNewestGesuch", ErrorCodeEnum.ERROR_ENTITY_NOT_FOUND,
				dossierJaxId.getId());
		}
		if (!gesuchsperiode.isPresent()) {
			throw new EbeguEntityNotFoundException("getIdOfNewestGesuch", ErrorCodeEnum.ERROR_ENTITY_NOT_FOUND,
				gesuchsperiodeJaxId.getId());
		}

		Optional<String> idOfNeuestesGesuch =
			gesuchService.getIdOfNeuestesGesuchForDossierAndGesuchsperiode(gesuchsperiode.get(), dossier.get());
		if (idOfNeuestesGesuch.isPresent()) {
			return Response.ok(idOfNeuestesGesuch.get()).build();
		}
		return Response.ok().build();
	}

	@ApiOperation(value = "Gibt die ID des neuesten Gesuchs dieses Dossiers zurueck. Wenn es noch keinen Fall, kein "
		+ "Gesuch oder keine Gesuchsperiode "
		+ "gibt, wird null zurueckgegeben", response = String.class)
	@Nonnull
	@GET
	@Path("/newestid/fall/{fallId}")
	@Consumes(MediaType.WILDCARD)
	@Produces(MediaType.TEXT_PLAIN)
	@PermitAll // Grundsaetzliche fuer alle Rollen: Datenabhaengig. -> Authorizer
	public Response getIdOfNewestGesuchForDossier(
		@Nonnull @NotNull @PathParam("fallId") JaxId dossierJaxId) {

		Objects.requireNonNull(dossierJaxId.getId());

		Optional<Dossier> dossier = dossierService.findDossier(dossierJaxId.getId());

		if (!dossier.isPresent()) {
			throw new EbeguEntityNotFoundException("getIdOfNewestGesuchForDossier",
				ErrorCodeEnum.ERROR_ENTITY_NOT_FOUND, dossierJaxId.getId());
		}

		Optional<String> idOfNeuestesGesuch = gesuchService.getIdOfNeuestesGesuchForDossier(dossier.get());
		if (idOfNeuestesGesuch.isPresent()) {
			return Response.ok(idOfNeuestesGesuch.get()).build();
		}
		return Response.ok().build();
	}

	@SuppressWarnings("NonBooleanMethodNameMayNotStartWithQuestion")
	@ApiOperation(value = "Ermittelt ob fuer das uebergebene Gesuch ein ausserordentlicher Anspruch moeglich ist",
		response = Boolean.class)
	@Nullable
	@GET
	@Path("/ausserordentlicheranspruchpossible/{gesuchId}")
	@Consumes(MediaType.WILDCARD)
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed({ ADMIN_BG, SUPER_ADMIN, SACHBEARBEITER_BG, ADMIN_GEMEINDE, SACHBEARBEITER_GEMEINDE, GESUCHSTELLER,
		SACHBEARBEITER_TS, ADMIN_TS, ADMIN_MANDANT, SACHBEARBEITER_MANDANT, REVISOR, JURIST })
	public Response isAusserordentlicherAnspruchPossible(@Nonnull @NotNull @PathParam("gesuchId") JaxId gesuchJAXPId) {
		Objects.requireNonNull(gesuchJAXPId.getId());
		Gesuch gesuch = gesuchService.findGesuch(converter.toEntityId(gesuchJAXPId))
			.orElseThrow(() -> new EbeguEntityNotFoundException("isAusserordentlicherAnspruchPossible",
				ErrorCodeEnum.ERROR_ENTITY_NOT_FOUND, gesuchJAXPId.getId()));

		Boolean possible = ausserordentlicherAnspruchService.isAusserordentlicherAnspruchPossible(gesuch);
		return Response.ok(possible).build();
	}

	@ApiOperation(value = "Gibt alle Antraege (Gesuche und Mutationen) eines Falls zurueck",
		responseContainer = "List", response = JaxAntragDTO.class)
	@Nonnull
	@GET
	@Path("/massenversand/{gesuchId}")
	@Consumes(MediaType.WILDCARD)
	@Produces(MediaType.WILDCARD)
	@PermitAll // Grundsaetzliche fuer alle Rollen: Datenabhaengig. -> Authorizer
	public List<String> getMassenversandTexteForGesuch(@Nonnull @NotNull @PathParam("gesuchId") JaxId gesuchIdJax) {
		Validate.notNull(gesuchIdJax.getId());
		return gesuchService.getMassenversandTexteForGesuch(converter.toEntityId(gesuchIdJax));
	}

	@ApiOperation(value = "Setzt das gegebene Gesuch in den Status KEIN_KONTINGENT", response = JaxGesuch.class)
	@Nullable
	@POST
	@Path("/setKeinKontingent/{antragId}")
	@Consumes(MediaType.WILDCARD)
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed({ SUPER_ADMIN, ADMIN_BG, SACHBEARBEITER_BG, ADMIN_GEMEINDE, SACHBEARBEITER_GEMEINDE,
		SACHBEARBEITER_TS, ADMIN_TS })
	public Response setKeinKontingent(
		@Nonnull @NotNull @PathParam("antragId") JaxId antragJaxId,
		@Context UriInfo uriInfo,
		@Context HttpServletResponse response) {

		Objects.requireNonNull(antragJaxId.getId());
		final String antragId = converter.toEntityId(antragJaxId);
		Optional<Gesuch> gesuch = gesuchService.findGesuch(antragId);

		if (gesuch.isPresent()) {
			resourceHelper.assertGesuchStatus(gesuch.get(), AntragStatusDTO.GEPRUEFT);
			Gesuch persistedGesuch = gesuchService.setKeinKontingent(gesuch.get());
			return Response.ok(converter.gesuchToJAX(persistedGesuch)).build();
		}
		throw new EbeguEntityNotFoundException("setKeinKontingent",
			ErrorCodeEnum.ERROR_ENTITY_NOT_FOUND, GESUCH_ID_INVALID + antragJaxId.getId());
	}

	@ApiOperation(value = "Setzt und speichert Properties auf dem Gesuch, welche immer bearbeitet werden dürfen",
		response = JaxAlwaysEditableProperties.class)
	@PUT
	@Path("/updateAlwaysEditableProperties")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed({SUPER_ADMIN, ADMIN_BG, SACHBEARBEITER_BG, ADMIN_GEMEINDE, SACHBEARBEITER_GEMEINDE, JURIST, REVISOR, ADMIN_TS, SACHBEARBEITER_TS,  STEUERAMT,
		GESUCHSTELLER, ADMIN_MANDANT, SACHBEARBEITER_MANDANT})
	public JaxGesuch updateAlwaysEditableProperties(
		@Nonnull @NotNull @Valid JaxAlwaysEditableProperties properties,
		@Context UriInfo uriInfo,
		@Context HttpServletResponse response) {

		Objects.requireNonNull(properties.getGesuchId().getId());
		final String antragId = converter.toEntityId(properties.getGesuchId());
		Gesuch gesuch = gesuchService.findGesuch(antragId).orElseThrow( () -> new EbeguEntityNotFoundException(
			"updateAlwaysEditableProperties", ErrorCodeEnum.ERROR_ENTITY_NOT_FOUND, antragId));

		converter.alwaysEditablePropertiesToGesuch(properties,gesuch);
		gesuchService.updateGesuch(gesuch, false);

		return converter.gesuchToJAX(gesuch);
	}
}
