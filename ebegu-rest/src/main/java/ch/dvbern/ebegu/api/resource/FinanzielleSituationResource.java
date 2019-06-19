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

import java.util.Objects;
import java.util.Optional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.Resource;
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
import ch.dvbern.ebegu.entities.Familiensituation;
import ch.dvbern.ebegu.entities.FinanzielleSituationContainer;
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.entities.GesuchstellerContainer;
import ch.dvbern.ebegu.enums.ErrorCodeEnum;
import ch.dvbern.ebegu.errors.EbeguEntityNotFoundException;
import ch.dvbern.ebegu.services.FinanzielleSituationService;
import ch.dvbern.ebegu.services.GesuchstellerService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

/**
 * REST Resource fuer FinanzielleSituation
 */
@Path("finanzielleSituation")
@Stateless
@Api(description = "Resource für die finanzielle Situation")
public class FinanzielleSituationResource {

	@Inject
	private FinanzielleSituationService finanzielleSituationService;
	@Inject
	private GesuchstellerService gesuchstellerService;

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
	public JaxFinanzielleSituationContainer saveFinanzielleSituation(
		@Nonnull @NotNull @PathParam("gesuchId") JaxId jaxGesuchId,
		@Nonnull @NotNull @PathParam("gesuchstellerId") JaxId jaxGesuchstellerId,
		@Nonnull @NotNull @Valid JaxFinanzielleSituationContainer jaxFinanzielleSituationContainer,
		@Context UriInfo uriInfo,
		@Context HttpServletResponse response) {

		Objects.requireNonNull(jaxGesuchId);
		Objects.requireNonNull(jaxGesuchstellerId);
		Objects.requireNonNull(jaxFinanzielleSituationContainer);

		String gesuchId = converter.toEntityId(jaxGesuchId);
		String gesuchstellerId = converter.toEntityId(jaxGesuchstellerId);
		Objects.requireNonNull(gesuchId);
		Objects.requireNonNull(gesuchstellerId);

		GesuchstellerContainer gesuchsteller = gesuchstellerService.findGesuchsteller(gesuchstellerId).orElseThrow(()
			-> new EbeguEntityNotFoundException("saveFinanzielleSituation", ErrorCodeEnum.ERROR_ENTITY_NOT_FOUND, "GesuchstellerId invalid: " + gesuchstellerId));
		FinanzielleSituationContainer convertedFinSitCont = converter.finanzielleSituationContainerToStorableEntity(jaxFinanzielleSituationContainer,
			gesuchsteller.getFinanzielleSituationContainer());

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
	public JaxGesuch saveFinanzielleSituationStart(
		@Nonnull @NotNull @Valid JaxGesuch gesuchJAXP,
		@Context UriInfo uriInfo,
		@Context HttpServletResponse response) {

		Objects.requireNonNull(gesuchJAXP.getId());
		JaxFamiliensituationContainer jaxFamiliensituationContainer = gesuchJAXP.getFamiliensituationContainer();
		Objects.requireNonNull(jaxFamiliensituationContainer);
		JaxFamiliensituation familiensituationJA = jaxFamiliensituationContainer.getFamiliensituationJA();
		Objects.requireNonNull(familiensituationJA);
		// Bei FinanzielleSituationStart arbeiten wir immer mit GS1: Wenn Sie gemeinsame Stek haben, werden die Fragen zu Veranlagung und Stek von GS1 genommen!
		JaxGesuchstellerContainer gesuchsteller1 = gesuchJAXP.getGesuchsteller1();
		Objects.requireNonNull(gesuchsteller1);
		JaxFinanzielleSituationContainer jaxFinanzielleSituationContainer = gesuchsteller1.getFinanzielleSituationContainer();
		Objects.requireNonNull(jaxFinanzielleSituationContainer);

		String gesuchId = gesuchJAXP.getId();
		String gesuchstellerId = gesuchsteller1.getId();
		Boolean sozialhilfeBezueger = familiensituationJA.getSozialhilfeBezueger();
		Boolean gemeinsameSteuererklaerung = familiensituationJA.getGemeinsameSteuererklaerung();

		Objects.requireNonNull(gesuchstellerId);
		Objects.requireNonNull(sozialhilfeBezueger);
		Objects.requireNonNull(gemeinsameSteuererklaerung);

		GesuchstellerContainer gesuchsteller = gesuchstellerService.findGesuchsteller(gesuchstellerId).orElseThrow(()
			-> new EbeguEntityNotFoundException("saveFinanzielleSituation", ErrorCodeEnum.ERROR_ENTITY_NOT_FOUND, "GesuchstellerId invalid: " + gesuchstellerId));
		FinanzielleSituationContainer convertedFinSitCont = converter.finanzielleSituationContainerToStorableEntity(jaxFinanzielleSituationContainer,
			gesuchsteller.getFinanzielleSituationContainer());

		Gesuch persistedGesuch = this.finanzielleSituationService.saveFinanzielleSituationStart(convertedFinSitCont,
			sozialhilfeBezueger, gemeinsameSteuererklaerung, gesuchId);
		return converter.gesuchToJAX(persistedGesuch);
	}

	@ApiOperation(value = "Berechnet die FinanzielleSituation fuer das uebergebene Gesuch. Die Berechnung wird " +
		"nicht gespeichert.", response = FinanzielleSituationResultateDTO.class)
	@Nullable
	@POST
	@Path("/calculate")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
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
	public Response calculateFinanzielleSituation(
		@Nonnull @NotNull @Valid JaxFinanzModel jaxFinSitModel,
		@Context UriInfo uriInfo,
		@Context HttpServletResponse response) {

		Gesuch gesuch = new Gesuch();
		gesuch.initFamiliensituationContainer();
		Familiensituation familiensituation = gesuch.extractFamiliensituation();
		Objects.requireNonNull(familiensituation);
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
	public JaxFinanzielleSituationContainer findFinanzielleSituation(
		@Nonnull @NotNull @PathParam("finanzielleSituationId") JaxId finanzielleSituationId) {

		Objects.requireNonNull(finanzielleSituationId.getId());
		String finanzielleSituationID = converter.toEntityId(finanzielleSituationId);
		Optional<FinanzielleSituationContainer> optional = finanzielleSituationService.findFinanzielleSituation(finanzielleSituationID);

		if (!optional.isPresent()) {
			return null;
		}
		FinanzielleSituationContainer finanzielleSituationToReturn = optional.get();
		return converter.finanzielleSituationContainerToJAX(finanzielleSituationToReturn);
	}
}
