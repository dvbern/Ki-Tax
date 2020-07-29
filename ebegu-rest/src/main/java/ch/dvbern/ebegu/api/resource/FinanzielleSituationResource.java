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

import java.util.Optional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.Resource;
import javax.annotation.security.DenyAll;
import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.ejb.EJBContext;
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
import ch.dvbern.ebegu.api.dtos.JaxFamiliensituation;
import ch.dvbern.ebegu.api.dtos.JaxFamiliensituationContainer;
import ch.dvbern.ebegu.api.dtos.JaxFinanzModel;
import ch.dvbern.ebegu.api.dtos.JaxFinanzielleSituationContainer;
import ch.dvbern.ebegu.api.dtos.JaxGesuch;
import ch.dvbern.ebegu.api.dtos.JaxGesuchstellerContainer;
import ch.dvbern.ebegu.api.dtos.JaxId;
import ch.dvbern.ebegu.dto.FinanzielleSituationResultateDTO;
import ch.dvbern.ebegu.entities.Adresse;
import ch.dvbern.ebegu.entities.Familiensituation;
import ch.dvbern.ebegu.entities.FamiliensituationContainer;
import ch.dvbern.ebegu.entities.FinanzielleSituationContainer;
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.entities.GesuchstellerContainer;
import ch.dvbern.ebegu.enums.ErrorCodeEnum;
import ch.dvbern.ebegu.errors.EbeguEntityNotFoundException;
import ch.dvbern.ebegu.services.FamiliensituationService;
import ch.dvbern.ebegu.services.FinanzielleSituationService;
import ch.dvbern.ebegu.services.GesuchstellerService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

import static ch.dvbern.ebegu.enums.UserRoleName.ADMIN_BG;
import static ch.dvbern.ebegu.enums.UserRoleName.ADMIN_GEMEINDE;
import static ch.dvbern.ebegu.enums.UserRoleName.ADMIN_MANDANT;
import static ch.dvbern.ebegu.enums.UserRoleName.ADMIN_TS;
import static ch.dvbern.ebegu.enums.UserRoleName.GESUCHSTELLER;
import static ch.dvbern.ebegu.enums.UserRoleName.JURIST;
import static ch.dvbern.ebegu.enums.UserRoleName.REVISOR;
import static ch.dvbern.ebegu.enums.UserRoleName.SACHBEARBEITER_BG;
import static ch.dvbern.ebegu.enums.UserRoleName.SACHBEARBEITER_GEMEINDE;
import static ch.dvbern.ebegu.enums.UserRoleName.SACHBEARBEITER_MANDANT;
import static ch.dvbern.ebegu.enums.UserRoleName.SACHBEARBEITER_TS;
import static ch.dvbern.ebegu.enums.UserRoleName.STEUERAMT;
import static ch.dvbern.ebegu.enums.UserRoleName.SUPER_ADMIN;
import static java.util.Objects.requireNonNull;

/**
 * REST Resource fuer FinanzielleSituation
 */
@Path("finanzielleSituation")
@Stateless
@Api(description = "Resource für die finanzielle Situation")
@DenyAll // Absichtlich keine Rolle zugelassen, erzwingt, dass es für neue Methoden definiert werden muss
public class FinanzielleSituationResource {

	@Inject
	private FinanzielleSituationService finanzielleSituationService;
	@Inject
	private GesuchstellerService gesuchstellerService;

	@Inject
	private FamiliensituationService familiensituationService;

	@SuppressWarnings("CdiInjectionPointsInspection")
	@Inject
	private JaxBConverter converter;

	@Resource
	private EJBContext context;    //fuer rollback


	@ApiOperation(value = "Create a new JaxFinanzielleSituationContainer in the database. The transfer object also has a " +
		"relation to FinanzielleSituation, it is stored in the database as well.", response = JaxFinanzielleSituationContainer.class)
	@Nullable
	@PUT
	@Path("/finanzielleSituation/{gesuchId}/{gesuchstellerId}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed({ SUPER_ADMIN, ADMIN_BG, SACHBEARBEITER_BG, ADMIN_GEMEINDE, SACHBEARBEITER_GEMEINDE, GESUCHSTELLER, SACHBEARBEITER_TS, ADMIN_TS })
	public JaxFinanzielleSituationContainer saveFinanzielleSituation(
		@Nonnull @NotNull @PathParam("gesuchId") JaxId jaxGesuchId,
		@Nonnull @NotNull @PathParam("gesuchstellerId") JaxId jaxGesuchstellerId,
		@Nonnull @NotNull @Valid JaxFinanzielleSituationContainer jaxFinanzielleSituationContainer,
		@Context UriInfo uriInfo,
		@Context HttpServletResponse response) {

		requireNonNull(jaxGesuchId);
		requireNonNull(jaxGesuchstellerId);
		requireNonNull(jaxFinanzielleSituationContainer);

		String gesuchId = converter.toEntityId(jaxGesuchId);
		String gesuchstellerId = converter.toEntityId(jaxGesuchstellerId);
		requireNonNull(gesuchId);
		requireNonNull(gesuchstellerId);

		GesuchstellerContainer gesuchsteller = gesuchstellerService.findGesuchsteller(gesuchstellerId).orElseThrow(()
			-> new EbeguEntityNotFoundException("saveFinanzielleSituation", ErrorCodeEnum.ERROR_ENTITY_NOT_FOUND, "GesuchstellerId invalid: " + gesuchstellerId));
		FinanzielleSituationContainer convertedFinSitCont = converter.finanzielleSituationContainerToStorableEntity(jaxFinanzielleSituationContainer,
			gesuchsteller.getFinanzielleSituationContainer());

		convertedFinSitCont.setGesuchsteller(gesuchsteller);

		FinanzielleSituationContainer persistedFinSit =
			this.finanzielleSituationService.saveFinanzielleSituation(convertedFinSitCont, gesuchId);

		return converter.finanzielleSituationContainerToJAX(persistedFinSit);
	}

	@ApiOperation(value = "Updates all required Data for the finanzielle Situation in Gesuch", response = JaxFinanzielleSituationContainer.class)
	@Nullable
	@PUT
	@Path("/finanzielleSituationStart")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed({ SUPER_ADMIN, ADMIN_BG, SACHBEARBEITER_BG, ADMIN_GEMEINDE, SACHBEARBEITER_GEMEINDE, GESUCHSTELLER, SACHBEARBEITER_TS, ADMIN_TS })
	public JaxGesuch saveFinanzielleSituationStart(
		@Nonnull @NotNull @Valid JaxGesuch gesuchJAXP,
		@Context UriInfo uriInfo,
		@Context HttpServletResponse response) {

		requireNonNull(gesuchJAXP.getId());
		JaxFamiliensituationContainer jaxFamiliensituationContainer = gesuchJAXP.getFamiliensituationContainer();
		requireNonNull(jaxFamiliensituationContainer);
		JaxFamiliensituation familiensituationJA = jaxFamiliensituationContainer.getFamiliensituationJA();
		requireNonNull(familiensituationJA);
		// Bei FinanzielleSituationStart arbeiten wir immer mit GS1: Wenn Sie gemeinsame Stek haben, werden die Fragen zu Veranlagung und Stek von GS1 genommen!
		JaxGesuchstellerContainer gesuchsteller1 = gesuchJAXP.getGesuchsteller1();
		requireNonNull(gesuchsteller1);
		JaxFinanzielleSituationContainer jaxFinanzielleSituationContainer = gesuchsteller1.getFinanzielleSituationContainer();
		requireNonNull(jaxFinanzielleSituationContainer);

		String gesuchId = gesuchJAXP.getId();
		String gesuchstellerId = gesuchsteller1.getId();
		Boolean sozialhilfeBezueger = familiensituationJA.getSozialhilfeBezueger();
		Boolean gemeinsameSteuererklaerung = familiensituationJA.getGemeinsameSteuererklaerung();
		Boolean verguenstigungGewuenscht = familiensituationJA.getVerguenstigungGewuenscht();

		requireNonNull(gesuchstellerId);
		requireNonNull(sozialhilfeBezueger);
		requireNonNull(gemeinsameSteuererklaerung);
		if (sozialhilfeBezueger.equals(Boolean.TRUE)) {
			// Sozialhilfebezueger bekommen immer eine Verguenstigung
			verguenstigungGewuenscht = Boolean.TRUE;
		} else {
			requireNonNull(verguenstigungGewuenscht);
		}

		GesuchstellerContainer gesuchsteller = gesuchstellerService.findGesuchsteller(gesuchstellerId).orElseThrow(()
			-> new EbeguEntityNotFoundException("saveFinanzielleSituation", ErrorCodeEnum.ERROR_ENTITY_NOT_FOUND, "GesuchstellerId invalid: " + gesuchstellerId));
		FinanzielleSituationContainer convertedFinSitCont = converter.finanzielleSituationContainerToStorableEntity(jaxFinanzielleSituationContainer,
			gesuchsteller.getFinanzielleSituationContainer());
		convertedFinSitCont.setGesuchsteller(gesuchsteller);

		if (familiensituationJA.isAbweichendeZahlungsadresse()) {
			requireNonNull(familiensituationJA.getZahlungsadresse());
		}

		if (familiensituationJA.isKeineMahlzeitenverguenstigungBeantragt()) {
			familiensituationJA.setIban(null);
			familiensituationJA.setKontoinhaber(null);
			familiensituationJA.setAbweichendeZahlungsadresse(false);
			familiensituationJA.setZahlungsadresse(null);
		}

		Adresse storedAdresse = new Adresse();
		if (jaxFamiliensituationContainer.getId() != null) {
			Optional<FamiliensituationContainer> storedFamSitContOptional =
				familiensituationService.findFamiliensituation(jaxFamiliensituationContainer.getId());

			if (storedFamSitContOptional.isPresent()) {
				Familiensituation storedFamSit = storedFamSitContOptional.get().getFamiliensituationJA();
				if (storedFamSit != null && storedFamSit.getZahlungsadresse() != null) {
					storedAdresse = storedFamSit.getZahlungsadresse();
				}
			}
		}

		Gesuch persistedGesuch = this.finanzielleSituationService.saveFinanzielleSituationStart(
			convertedFinSitCont,
			sozialhilfeBezueger,
			gemeinsameSteuererklaerung,
			verguenstigungGewuenscht,
			familiensituationJA.isKeineMahlzeitenverguenstigungBeantragt(),
			familiensituationJA.getIban(),
			familiensituationJA.getKontoinhaber(),
			familiensituationJA.isAbweichendeZahlungsadresse(),
			familiensituationJA.getZahlungsadresse() == null ? null :
				converter.adresseToEntity(familiensituationJA.getZahlungsadresse(), storedAdresse),
			gesuchId);
		return converter.gesuchToJAX(persistedGesuch);
	}

	@ApiOperation(value = "Berechnet die FinanzielleSituation fuer das uebergebene Gesuch. Die Berechnung wird " +
		"nicht gespeichert.", response = FinanzielleSituationResultateDTO.class)
	@Nullable
	@POST
	@Path("/calculate")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@PermitAll // Grundsaetzliche fuer alle Rollen: Datenabhaengig. -> Authorizer
	public Response calculateFinanzielleSituation(
		@Nonnull @NotNull @Valid JaxGesuch gesuchJAXP,
		@Context UriInfo uriInfo,
		@Context HttpServletResponse response) {

		Gesuch gesuch = converter.gesuchToEntity(gesuchJAXP, new Gesuch()); // nur konvertieren, nicht mergen mit Gesuch von DB!
		FinanzielleSituationResultateDTO finanzielleSituationResultateDTO = finanzielleSituationService.calculateResultate(gesuch);

		return Response.ok(finanzielleSituationResultateDTO).build();
	}

	/**
	 * Finanzielle Situation wird hier im gegensatz zur /calculate mehtode nur als DTO mitgegeben statt als ganzes gesuch
	 */
	@ApiOperation(value = "Berechnet die FinanzielleSituation fuer das Gesuch mit der uebergebenen Id. Die Berechnung " +
		"nicht gespeichert. Die FinanzielleSituation wird hier im Gegensatz zur /calculate mehtode nur als DTO " +
		"mitgegeben statt als ganzes Gesuch", response = FinanzielleSituationResultateDTO.class)
	@Nullable
	@POST
	@Path("/calculateTemp")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@PermitAll // Grundsaetzliche fuer alle Rollen: Datenabhaengig. -> Authorizer
	public Response calculateFinanzielleSituation(
		@Nonnull @NotNull @Valid JaxFinanzModel jaxFinSitModel,
		@Context UriInfo uriInfo,
		@Context HttpServletResponse response) {

		Gesuch gesuch = new Gesuch();
		gesuch.initFamiliensituationContainer();
		Familiensituation familiensituation = gesuch.extractFamiliensituation();
		requireNonNull(familiensituation);
		familiensituation.setGemeinsameSteuererklaerung(jaxFinSitModel.isGemeinsameSteuererklaerung());
		if (jaxFinSitModel.getFinanzielleSituationContainerGS1() != null) {
			gesuch.setGesuchsteller1(new GesuchstellerContainer());
			//noinspection ConstantConditions
			gesuch.getGesuchsteller1().setFinanzielleSituationContainer(
				converter.finanzielleSituationContainerToEntity(jaxFinSitModel.getFinanzielleSituationContainerGS1(), new FinanzielleSituationContainer()));
		}
		if (jaxFinSitModel.getFinanzielleSituationContainerGS2() != null) {
			gesuch.setGesuchsteller2(new GesuchstellerContainer());
			//noinspection ConstantConditions
			gesuch.getGesuchsteller2().setFinanzielleSituationContainer(
				converter.finanzielleSituationContainerToEntity(jaxFinSitModel.getFinanzielleSituationContainerGS2(), new FinanzielleSituationContainer()));
		}

		FinanzielleSituationResultateDTO finanzielleSituationResultateDTO = finanzielleSituationService.calculateResultate(gesuch);
		// Wir wollen nur neu berechnen. Das Gesuch soll auf keinen Fall neu gespeichert werden
		context.setRollbackOnly();
		return Response.ok(finanzielleSituationResultateDTO).build();
	}

	@ApiOperation(value = "Sucht die FinanzielleSituation mit der uebergebenen Id in der Datenbank",
		response = JaxFinanzielleSituationContainer.class)
	@Nullable
	@GET
	@Path("/{finanzielleSituationId}")
	@Consumes(MediaType.WILDCARD)
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed({ SUPER_ADMIN, ADMIN_BG, SACHBEARBEITER_BG, ADMIN_GEMEINDE, SACHBEARBEITER_GEMEINDE, JURIST, REVISOR, GESUCHSTELLER, STEUERAMT,
		ADMIN_TS, SACHBEARBEITER_TS, ADMIN_MANDANT, SACHBEARBEITER_MANDANT })
	public JaxFinanzielleSituationContainer findFinanzielleSituation(
		@Nonnull @NotNull @PathParam("finanzielleSituationId") JaxId finanzielleSituationId) {

		requireNonNull(finanzielleSituationId.getId());
		String finanzielleSituationID = converter.toEntityId(finanzielleSituationId);
		Optional<FinanzielleSituationContainer> optional = finanzielleSituationService.findFinanzielleSituation(finanzielleSituationID);

		if (!optional.isPresent()) {
			return null;
		}
		FinanzielleSituationContainer finanzielleSituationToReturn = optional.get();
		return converter.finanzielleSituationContainerToJAX(finanzielleSituationToReturn);
	}
}
