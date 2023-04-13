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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package ch.dvbern.ebegu.api.resource;

import java.time.LocalDate;
import java.util.Objects;
import java.util.Optional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.Resource;
import javax.annotation.security.DenyAll;
import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.ejb.EJBContext;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
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
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import ch.dvbern.ebegu.api.converter.JaxBConverter;
import ch.dvbern.ebegu.api.dtos.JaxFamiliensituation;
import ch.dvbern.ebegu.api.dtos.JaxFamiliensituationContainer;
import ch.dvbern.ebegu.api.dtos.JaxGesuch;
import ch.dvbern.ebegu.api.dtos.JaxGesuchstellerContainer;
import ch.dvbern.ebegu.api.dtos.JaxId;
import ch.dvbern.ebegu.api.dtos.finanziellesituation.JaxFinanzModel;
import ch.dvbern.ebegu.api.dtos.finanziellesituation.JaxFinanzielleSituationContainer;
import ch.dvbern.ebegu.dto.FinanzielleSituationResultateDTO;
import ch.dvbern.ebegu.dto.FinanzielleSituationStartDTO;
import ch.dvbern.ebegu.dto.JaxFinanzielleSituationAufteilungDTO;
import ch.dvbern.ebegu.entities.Adresse;
import ch.dvbern.ebegu.entities.Einstellung;
import ch.dvbern.ebegu.entities.Familiensituation;
import ch.dvbern.ebegu.entities.FamiliensituationContainer;
import ch.dvbern.ebegu.entities.FinanzielleSituationContainer;
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.entities.GesuchstellerContainer;
import ch.dvbern.ebegu.entities.SteuerdatenResponse;
import ch.dvbern.ebegu.enums.EinstellungKey;
import ch.dvbern.ebegu.enums.ErrorCodeEnum;
import ch.dvbern.ebegu.enums.FinanzielleSituationTyp;
import ch.dvbern.ebegu.enums.GesuchstellerTyp;
import ch.dvbern.ebegu.errors.EbeguEntityNotFoundException;
import ch.dvbern.ebegu.nesko.handler.KibonAnfrageContext;
import ch.dvbern.ebegu.nesko.handler.KibonAnfrageHandler;
import ch.dvbern.ebegu.services.EinstellungService;
import ch.dvbern.ebegu.services.FamiliensituationService;
import ch.dvbern.ebegu.services.FinanzielleSituationService;
import ch.dvbern.ebegu.services.GesuchService;
import ch.dvbern.ebegu.services.GesuchstellerService;
import ch.dvbern.ebegu.util.Constants;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

import static ch.dvbern.ebegu.enums.UserRoleName.ADMIN_BG;
import static ch.dvbern.ebegu.enums.UserRoleName.ADMIN_GEMEINDE;
import static ch.dvbern.ebegu.enums.UserRoleName.ADMIN_MANDANT;
import static ch.dvbern.ebegu.enums.UserRoleName.ADMIN_SOZIALDIENST;
import static ch.dvbern.ebegu.enums.UserRoleName.ADMIN_TS;
import static ch.dvbern.ebegu.enums.UserRoleName.GESUCHSTELLER;
import static ch.dvbern.ebegu.enums.UserRoleName.JURIST;
import static ch.dvbern.ebegu.enums.UserRoleName.REVISOR;
import static ch.dvbern.ebegu.enums.UserRoleName.SACHBEARBEITER_BG;
import static ch.dvbern.ebegu.enums.UserRoleName.SACHBEARBEITER_GEMEINDE;
import static ch.dvbern.ebegu.enums.UserRoleName.SACHBEARBEITER_MANDANT;
import static ch.dvbern.ebegu.enums.UserRoleName.SACHBEARBEITER_SOZIALDIENST;
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

	@Inject
	private GesuchService gesuchService;

	@Inject
	private KibonAnfrageHandler kibonAnfrageHandler;

	@Inject
	private EinstellungService einstellungService;

	@SuppressWarnings("CdiInjectionPointsInspection")
	@Inject
	private JaxBConverter converter;

	@Resource
	private EJBContext context;    //fuer rollback

	@ApiOperation(value =
		"Create a new JaxFinanzielleSituationContainer in the database. The transfer object also has a "
			+
			"relation to FinanzielleSituation, it is stored in the database as well.",
		response = JaxFinanzielleSituationContainer.class)
	@Nullable
	@PUT
	@Path("/finanzielleSituation/{gesuchId}/{gesuchstellerId}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed({ SUPER_ADMIN, ADMIN_BG, SACHBEARBEITER_BG, ADMIN_GEMEINDE, SACHBEARBEITER_GEMEINDE, GESUCHSTELLER,
		SACHBEARBEITER_TS, ADMIN_TS, ADMIN_SOZIALDIENST, SACHBEARBEITER_SOZIALDIENST })
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

		GesuchstellerContainer gesuchsteller = findGesuchstellerById(gesuchstellerId, "saveFinanzielleSituation");

		FinanzielleSituationContainer convertedFinSitCont = converter.finanzielleSituationContainerToStorableEntity(
			jaxFinanzielleSituationContainer,
			gesuchsteller.getFinanzielleSituationContainer());

		convertedFinSitCont.setGesuchsteller(gesuchsteller);

		FinanzielleSituationContainer persistedFinSit =
			this.finanzielleSituationService.saveFinanzielleSituation(convertedFinSitCont, gesuchId);

		return converter.finanzielleSituationContainerToJAX(persistedFinSit);
	}

	@ApiOperation(value = "Updates all required Data for the finanzielle Situation in Gesuch",
		response = JaxFinanzielleSituationContainer.class)
	@Nullable
	@PUT
	@Path("/finanzielleSituationStart")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed({ SUPER_ADMIN, ADMIN_BG, SACHBEARBEITER_BG, ADMIN_GEMEINDE, SACHBEARBEITER_GEMEINDE, GESUCHSTELLER,
		SACHBEARBEITER_TS, ADMIN_TS, ADMIN_SOZIALDIENST, SACHBEARBEITER_SOZIALDIENST })
	@SuppressWarnings("PMD.NcssMethodCount")
	public JaxGesuch saveFinanzielleSituationStart(
		@Nonnull @NotNull @Valid JaxGesuch gesuchJAXP,
		@Context UriInfo uriInfo,
		@Context HttpServletResponse response) {

		requireNonNull(gesuchJAXP.getId());
		JaxFamiliensituationContainer jaxFamiliensituationContainer = gesuchJAXP.getFamiliensituationContainer();
		requireNonNull(jaxFamiliensituationContainer);
		JaxFamiliensituation familiensituationJA = jaxFamiliensituationContainer.getFamiliensituationJA();
		requireNonNull(familiensituationJA);
		// Bei FinanzielleSituationStart arbeiten wir immer mit GS1: Wenn Sie gemeinsame Stek haben, werden die Fragen
		// zu Veranlagung und Stek von GS1 genommen!
		JaxGesuchstellerContainer gesuchsteller1 = gesuchJAXP.getGesuchsteller1();
		requireNonNull(gesuchsteller1);
		JaxFinanzielleSituationContainer jaxFinanzielleSituationContainer =
			gesuchsteller1.getFinanzielleSituationContainer();
		requireNonNull(jaxFinanzielleSituationContainer);

		String gesuchId = gesuchJAXP.getId();
		String gesuchstellerId = gesuchsteller1.getId();

		requireNonNull(gesuchstellerId);

		GesuchstellerContainer gesuchsteller = findGesuchstellerById(gesuchstellerId, "saveFinanzielleSituationStart");

		FinanzielleSituationContainer convertedFinSitCont = converter.finanzielleSituationContainerToStorableEntity(
			jaxFinanzielleSituationContainer,
			gesuchsteller.getFinanzielleSituationContainer());
		convertedFinSitCont.setGesuchsteller(gesuchsteller);

		if (familiensituationJA.isAbweichendeZahlungsadresse()) {
			requireNonNull(familiensituationJA.getZahlungsadresse());
		}

		Gesuch gesuch = gesuchService
			.findGesuch(gesuchId)
			.orElseThrow(() -> new EbeguEntityNotFoundException(
				"saveFinanzielleSituationStart",
				ErrorCodeEnum.ERROR_ENTITY_NOT_FOUND,
				gesuchId));

		final Einstellung einstellung = einstellungService.findEinstellung(
			EinstellungKey.GEMEINDE_MAHLZEITENVERGUENSTIGUNG_ENABLED,
			gesuch.extractGemeinde(),
			gesuch.getGesuchsperiode());
		boolean mahlzeitenverguenstigungEnabled = einstellung.getValueAsBoolean();

		if (!mahlzeitenverguenstigungEnabled) {
			familiensituationJA.setKeineMahlzeitenverguenstigungBeantragt(true);
		}

		Adresse storedAdresse = new Adresse();
		if (jaxFamiliensituationContainer.getId() != null) {
			Optional<FamiliensituationContainer> storedFamSitContOptional =
				familiensituationService.findFamiliensituation(jaxFamiliensituationContainer.getId());

			if (storedFamSitContOptional.isPresent()) {
				Familiensituation storedFamSit = storedFamSitContOptional.get().getFamiliensituationJA();
				if (storedFamSit != null
					&& storedFamSit.getAuszahlungsdaten() != null
					&& storedFamSit.getAuszahlungsdaten().getAdresseKontoinhaber() != null) {
					storedAdresse = storedFamSit.getAuszahlungsdaten().getAdresseKontoinhaber();
				}
			}
		}

		Boolean sozialhilfeBezueger = familiensituationJA.getSozialhilfeBezueger();
		Boolean gemeinsameSteuererklaerung = familiensituationJA.getGemeinsameSteuererklaerung();
		Boolean verguenstigungGewuenscht = familiensituationJA.getVerguenstigungGewuenscht();

		requireNonNull(sozialhilfeBezueger);
		requireNonNull(gemeinsameSteuererklaerung);

		if (gesuchJAXP.getFinSitTyp().equals(FinanzielleSituationTyp.BERN)
			|| gesuchJAXP.getFinSitTyp().equals(FinanzielleSituationTyp.BERN_FKJV)
			|| gesuchJAXP.getFinSitTyp().equals(FinanzielleSituationTyp.SOLOTHURN)) {

			if (sozialhilfeBezueger.equals(Boolean.TRUE)) {
				// Sozialhilfebezueger bekommen immer eine Verguenstigung
				verguenstigungGewuenscht = Boolean.TRUE;
			} else {
				requireNonNull(verguenstigungGewuenscht);
			}
		} else {
			verguenstigungGewuenscht = Boolean.TRUE;
		}

		FinanzielleSituationStartDTO finSitStartDTO = new FinanzielleSituationStartDTO(
			sozialhilfeBezueger,
			familiensituationJA.getZustaendigeAmtsstelle(),
			familiensituationJA.getNameBetreuer(),
			gemeinsameSteuererklaerung,
			verguenstigungGewuenscht,
			familiensituationJA.isKeineMahlzeitenverguenstigungBeantragt(),
			familiensituationJA.getIban(),
			familiensituationJA.getKontoinhaber(),
			familiensituationJA.isAbweichendeZahlungsadresse(),
			familiensituationJA.getZahlungsadresse() == null ? null :
				converter.adresseToEntity(familiensituationJA.getZahlungsadresse(), storedAdresse),
			familiensituationJA.getInfomaKreditorennummer(),
			familiensituationJA.getInfomaBankcode(),
			gesuchJAXP.getFinSitAenderungGueltigAbDatum()
		);

		Gesuch persistedGesuch = this.finanzielleSituationService.saveFinanzielleSituationStart(
			convertedFinSitCont,
			finSitStartDTO,
			gesuchId
		);

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

		Gesuch gesuch =
			converter.gesuchToEntity(gesuchJAXP, new Gesuch()); // nur konvertieren, nicht mergen mit Gesuch von DB!
		FinanzielleSituationResultateDTO finanzielleSituationResultateDTO =
			finanzielleSituationService.calculateResultate(gesuch);

		return Response.ok(finanzielleSituationResultateDTO).build();
	}

	/**
	 * Finanzielle Situation wird hier im gegensatz zur /calculate mehtode nur als DTO mitgegeben statt als ganzes
	 * gesuch
	 */
	@ApiOperation(value = "Berechnet die FinanzielleSituation fuer das Gesuch mit der uebergebenen Id. Die Berechnung "
		+
		"nicht gespeichert. Die FinanzielleSituation wird hier im Gegensatz zur /calculate mehtode nur als DTO "
		+
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
		if (jaxFinSitModel.getFinanzielleSituationTyp() != null) {
			gesuch.setFinSitTyp(jaxFinSitModel.getFinanzielleSituationTyp());
		} else {
			gesuch.setFinSitTyp(FinanzielleSituationTyp.BERN);
		}
		Familiensituation familiensituation = gesuch.extractFamiliensituation();
		requireNonNull(familiensituation);
		familiensituation.setGemeinsameSteuererklaerung(jaxFinSitModel.isGemeinsameSteuererklaerung());
		if (jaxFinSitModel.getFinanzielleSituationContainerGS1() != null) {
			gesuch.setGesuchsteller1(new GesuchstellerContainer());
			//noinspection ConstantConditions
			gesuch.getGesuchsteller1().setFinanzielleSituationContainer(
				converter.finanzielleSituationContainerToEntity(
					jaxFinSitModel.getFinanzielleSituationContainerGS1(),
					new FinanzielleSituationContainer()));
			setFinSitAbfrageStatus(gesuch.getGesuchsteller1(), jaxFinSitModel.getFinanzielleSituationContainerGS1());
		}
		if (jaxFinSitModel.getFinanzielleSituationContainerGS2() != null) {
			gesuch.setGesuchsteller2(new GesuchstellerContainer());
			//noinspection ConstantConditions
			gesuch.getGesuchsteller2().setFinanzielleSituationContainer(
				converter.finanzielleSituationContainerToEntity(
					jaxFinSitModel.getFinanzielleSituationContainerGS2(),
					new FinanzielleSituationContainer()));
			setFinSitAbfrageStatus(gesuch.getGesuchsteller2(), jaxFinSitModel.getFinanzielleSituationContainerGS2());
		}

		FinanzielleSituationResultateDTO finanzielleSituationResultateDTO =
			finanzielleSituationService.calculateResultate(gesuch);
		// Wir wollen nur neu berechnen. Das Gesuch soll auf keinen Fall neu gespeichert werden
		context.setRollbackOnly();
		return Response.ok(finanzielleSituationResultateDTO).build();
	}

	private void setFinSitAbfrageStatus(
		@Nonnull GesuchstellerContainer gesuchstellerContainer,
		@Nonnull JaxFinanzielleSituationContainer finanzielleSituationContainer) {
		if (finanzielleSituationContainer.getId() != null) {
			Optional<FinanzielleSituationContainer> finSitCont =
				finanzielleSituationService.findFinanzielleSituation(finanzielleSituationContainer.getId());
			if (finSitCont.isPresent()) {
				assert gesuchstellerContainer.getFinanzielleSituationContainer() != null;
				gesuchstellerContainer.getFinanzielleSituationContainer()
					.getFinanzielleSituationJA()
					.setSteuerdatenAbfrageStatus(finSitCont.get()
						.getFinanzielleSituationJA()
						.getSteuerdatenAbfrageStatus());
			}
		}
	}

	@ApiOperation(value = "Sucht die FinanzielleSituation mit der uebergebenen Id in der Datenbank",
		response = JaxFinanzielleSituationContainer.class)
	@Nullable
	@GET
	@Path("/{finanzielleSituationId}")
	@Consumes(MediaType.WILDCARD)
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed({ SUPER_ADMIN, ADMIN_BG, SACHBEARBEITER_BG, ADMIN_GEMEINDE, SACHBEARBEITER_GEMEINDE, JURIST, REVISOR,
		GESUCHSTELLER, STEUERAMT,
		ADMIN_TS, SACHBEARBEITER_TS, ADMIN_MANDANT, SACHBEARBEITER_MANDANT, ADMIN_SOZIALDIENST,
		SACHBEARBEITER_SOZIALDIENST })
	public JaxFinanzielleSituationContainer findFinanzielleSituation(
		@Nonnull @NotNull @PathParam("finanzielleSituationId") JaxId finanzielleSituationId) {

		requireNonNull(finanzielleSituationId.getId());
		String finanzielleSituationID = converter.toEntityId(finanzielleSituationId);
		Optional<FinanzielleSituationContainer> optional =
			finanzielleSituationService.findFinanzielleSituation(finanzielleSituationID);

		if (!optional.isPresent()) {
			return null;
		}
		FinanzielleSituationContainer finanzielleSituationToReturn = optional.get();
		return converter.finanzielleSituationContainerToJAX(finanzielleSituationToReturn);
	}

	@ApiOperation(value = "Setzt die schon beantworte Fragen im Backend und update die FinSitDaten gemaess die Anruf "
		+ "Ergebniss",
		response = SteuerdatenResponse.class)
	@Nullable
	@PUT
	@Path("/kibonanfrage/{gesuchId}/{gesuchstellerNumber}/{isGemeinsam}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed({ GESUCHSTELLER, ADMIN_BG, SACHBEARBEITER_BG, ADMIN_TS, SACHBEARBEITER_TS, ADMIN_GEMEINDE,
		SACHBEARBEITER_GEMEINDE, SACHBEARBEITER_GEMEINDE, SUPER_ADMIN})
	@TransactionAttribute(TransactionAttributeType.NEVER)
	public JaxFinanzielleSituationContainer updateFinSitMitSteuerdaten(
		@Nonnull @NotNull @PathParam("gesuchstellerNumber") int gesuchstellerNumber,
		@Nonnull @NotNull @PathParam("gesuchId") JaxId gesuchId,
		@Nonnull @NotNull @PathParam("isGemeinsam") boolean isGemeinsam,
		@Context UriInfo uriInfo,
		@Context HttpServletResponse response
	) {

		Objects.requireNonNull(gesuchId.getId());

		//Antrag suchen
		Gesuch gesuch = gesuchService.findGesuch(gesuchId.getId()).orElseThrow(()
			-> new EbeguEntityNotFoundException(
			"getSteuerdatenBeiAntragId",
			ErrorCodeEnum.ERROR_ENTITY_NOT_FOUND,
			"Gesuch ID invalid: " + gesuchId.getId()));

		GesuchstellerTyp gesuchstellerTyp = GesuchstellerTyp.getGesuchstellerTypByNummer(gesuchstellerNumber);
		initFinanzielleSituationContainerForSteuerdatenRequest(gesuch, isGemeinsam, gesuchstellerTyp);

		KibonAnfrageContext kibonAnfrageContext =
				kibonAnfrageHandler.handleKibonAnfrage(gesuch, gesuchstellerTyp);

		if (isGemeinsam) {
			this.gesuchstellerService.saveGesuchsteller(requireNonNull(gesuch.getGesuchsteller2()), gesuch, 2, false);
			this.finanzielleSituationService.
					saveFinanzielleSituationTemp(kibonAnfrageContext.getFinSitCont(GesuchstellerTyp.GESUCHSTELLER_2));
		}
		gesuchstellerService.saveGesuchsteller(
				kibonAnfrageContext.getGesuchstellerContainerToUse(),
				gesuch,
				gesuchstellerNumber,
				false);
		FinanzielleSituationContainer persistedFinSit = this.finanzielleSituationService.saveFinanzielleSituationTemp(
				kibonAnfrageContext.getFinanzielleSituationContainerToUse());
		return converter.finanzielleSituationContainerToJAX(persistedFinSit);
	}

	private void initFinanzielleSituationContainerForSteuerdatenRequest(
			Gesuch gesuch,
			boolean isGemeinsam,
			GesuchstellerTyp gesuchstellerTyp) {

		GesuchstellerContainer gesuchstellerToInitialse = gesuchstellerTyp == GesuchstellerTyp.GESUCHSTELLER_1 ?
			 gesuch.getGesuchsteller1() : gesuch.getGesuchsteller2();

		Objects.requireNonNull(gesuchstellerToInitialse);
		Objects.requireNonNull(gesuchstellerToInitialse.getFinanzielleSituationContainer());
		gesuchstellerToInitialse.getFinanzielleSituationContainer().getFinanzielleSituationJA().setSteuerdatenZugriff(true);

		Objects.requireNonNull(gesuch.getFamiliensituationContainer());
		Objects.requireNonNull(gesuch.getFamiliensituationContainer().getFamiliensituationJA());
		gesuch.getFamiliensituationContainer().getFamiliensituationJA().setGemeinsameSteuererklaerung(isGemeinsam);
	}

	@ApiOperation(value = "reset die FinSit Status und Nettovermoegen falls gesetzt"
		+ "Ergebniss",
		response = SteuerdatenResponse.class)
	@Nullable
	@PUT
	@Path("/kibonanfrage/reset/{kibonAnfrageId}/{gesuchstellerId}/{isGemeinsam}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed({ GESUCHSTELLER, SUPER_ADMIN })
	@TransactionAttribute(TransactionAttributeType.NEVER)
	public JaxFinanzielleSituationContainer resetFinSitSteuerdaten(
		@Nonnull @NotNull @PathParam("kibonAnfrageId") JaxId kibonAnfrageId,
		@Nonnull @NotNull @PathParam("gesuchstellerId") JaxId jaxGesuchstellerId,
		@Nonnull @NotNull @PathParam("isGemeinsam") boolean isGemeinsam,
		@Nonnull @NotNull @Valid JaxFinanzielleSituationContainer jaxFinanzielleSituationContainer,
		@Context UriInfo uriInfo,
		@Context HttpServletResponse response
	) {
		Objects.requireNonNull(kibonAnfrageId.getId());
		Objects.requireNonNull(jaxGesuchstellerId.getId());
		//Antrag suchen
		Gesuch gesuch = gesuchService.findGesuch(kibonAnfrageId.getId()).orElseThrow(()
			-> new EbeguEntityNotFoundException(
			"getSteuerdatenBeiAntragId",
			ErrorCodeEnum.ERROR_ENTITY_NOT_FOUND,
			"Gesuch ID invalid: " + kibonAnfrageId.getId()));

		//FinSit Suchen, Feldern updaten
		GesuchstellerContainer gesuchsteller =
			findGesuchstellerById(jaxGesuchstellerId.getId(), "resetFinSitSteuerdaten");

		FinanzielleSituationContainer convertedFinSitCont = converter.finanzielleSituationContainerToStorableEntity(
			jaxFinanzielleSituationContainer,
			gesuchsteller.getFinanzielleSituationContainer());
		convertedFinSitCont.setGesuchsteller(gesuchsteller);

		// reset SteuerdatenAbfrageStatus und NettoVermoegen
		convertedFinSitCont.getFinanzielleSituationJA().setSteuerdatenAbfrageStatus(null);
		convertedFinSitCont.getFinanzielleSituationJA().setSteuerdatenAbfrageTimestamp(null);
		convertedFinSitCont.getFinanzielleSituationJA().setNettoVermoegen(null);

		// auch fuer GS2 wenn gemeinsam
		if (isGemeinsam
			&& gesuch.getGesuchsteller2() != null
			&& gesuch.getGesuchsteller2().getFinanzielleSituationContainer() != null
			&& gesuch.getGesuchsteller2().getFinanzielleSituationContainer().getFinanzielleSituationJA() != null) {
			FinanzielleSituationContainer finSitGS2 = gesuch.getGesuchsteller2().getFinanzielleSituationContainer();
			finSitGS2.getFinanzielleSituationJA().setSteuerdatenAbfrageStatus(null);
			finSitGS2.getFinanzielleSituationJA().setSteuerdatenAbfrageTimestamp(null);
			finSitGS2.getFinanzielleSituationJA().setNettoVermoegen(null);
			finSitGS2.getFinanzielleSituationJA()
				.setSteuerdatenZugriff(convertedFinSitCont.getFinanzielleSituationJA().getSteuerdatenZugriff());
			this.finanzielleSituationService.saveFinanzielleSituationTemp(finSitGS2);
		}

		//und zusendlich speichern und zuruckgeben
		FinanzielleSituationContainer persistedFinSit =
			this.finanzielleSituationService.saveFinanzielleSituationTemp(convertedFinSitCont);
		return converter.finanzielleSituationContainerToJAX(persistedFinSit);
	}

	@ApiOperation(value = "reset die FinSit Status und Nettovermoegen falls gesetzt"
		+ "Ergebniss",
		response = SteuerdatenResponse.class)
	@GET
	@Path("/geburtsdatum-matches-steuerabfrage/{containerId}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed({ SUPER_ADMIN, ADMIN_BG, SACHBEARBEITER_BG, ADMIN_GEMEINDE, SACHBEARBEITER_GEMEINDE, GESUCHSTELLER,
		SACHBEARBEITER_TS, ADMIN_TS, ADMIN_SOZIALDIENST, SACHBEARBEITER_SOZIALDIENST, ADMIN_MANDANT,
		SACHBEARBEITER_MANDANT })
	@TransactionAttribute(TransactionAttributeType.NEVER)
	public boolean doesGeburtsdatumMatchSteuerabfrage(
		@Nonnull @NotNull @PathParam("containerId") JaxId jaxContainerId,
		@Nonnull @NotNull @QueryParam("geburtsdatum") String geburtsdatum,
		@Context UriInfo uriInfo,
		@Context HttpServletResponse response
	) {
		//Antrag suchen
		FinanzielleSituationContainer finSitContainer =
			findFinanzielleSituationWithResponse(jaxContainerId);

		LocalDate datumSteuerdatenAnfrage = requireNonNull(finSitContainer.getFinanzielleSituationJA()
			.getSteuerdatenResponse()).getGeburtsdatumAntragsteller();

		LocalDate formattedGeburtsdatum = LocalDate.parse(geburtsdatum, Constants.DATE_FORMATTER);

		boolean isMatching = Objects.equals(datumSteuerdatenAnfrage, formattedGeburtsdatum);

		if (!isMatching
			&& finSitContainer.getFinanzielleSituationJA().getSteuerdatenResponse().getGeburtsdatumPartner() != null) {
			isMatching = Objects.equals(finSitContainer.getFinanzielleSituationJA()
				.getSteuerdatenResponse()
				.getGeburtsdatumPartner(), formattedGeburtsdatum);
		}

		return isMatching;
	}

	@Nonnull
	private FinanzielleSituationContainer findFinanzielleSituationWithResponse(JaxId jaxContainerId) {
		Objects.requireNonNull(jaxContainerId.getId());

		FinanzielleSituationContainer finSitContainer =
			finanzielleSituationService.findFinanzielleSituation(jaxContainerId.getId()).orElseThrow(()
				-> new EbeguEntityNotFoundException(
				"findFinanzielleSituationWithResponse",
				ErrorCodeEnum.ERROR_ENTITY_NOT_FOUND,
				"FinSit ID invalid: " + jaxContainerId.getId()));

		while (finSitContainer.getFinanzielleSituationJA().getSteuerdatenResponse() == null) {
			finSitContainer = findFinanzielleSituationVorgaenger(finSitContainer).orElseThrow(() ->
				new EbeguEntityNotFoundException(
					"Keine FinSit mit SteuerdatenResponse gefunden",
					ErrorCodeEnum.ERROR_ENTITY_NOT_FOUND
				));
		}

		return finSitContainer;
	}

	private Optional<FinanzielleSituationContainer> findFinanzielleSituationVorgaenger(FinanzielleSituationContainer finSitContainer) {
		if (finSitContainer.getVorgaengerId() == null) {
			return Optional.empty();
		}

		return finanzielleSituationService.findFinanzielleSituation(finSitContainer.getVorgaengerId());
	}

	@ApiOperation(value = "",
		response = JaxFinanzielleSituationContainer.class)
	@Nullable
	@PUT
	@Path("/updateFromAufteilung/{gesuchId}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed({ SUPER_ADMIN, ADMIN_BG, SACHBEARBEITER_BG, ADMIN_GEMEINDE, SACHBEARBEITER_GEMEINDE, GESUCHSTELLER,
		SACHBEARBEITER_TS, ADMIN_TS, ADMIN_SOZIALDIENST, SACHBEARBEITER_SOZIALDIENST })
	@TransactionAttribute(TransactionAttributeType.NEVER)
	public Response updateFinSitFromAufteilung(
		@Nonnull @NotNull @PathParam("gesuchId") JaxId gesuchId,
		@Nonnull @NotNull @Valid JaxFinanzielleSituationAufteilungDTO jaxFinanzielleSituationAufteilungDTO,
		@Context UriInfo uriInfo,
		@Context HttpServletResponse response
	) {
		Objects.requireNonNull(jaxFinanzielleSituationAufteilungDTO);
		Objects.requireNonNull(gesuchId.getId());

		Gesuch gesuch = gesuchService.findGesuch(gesuchId.getId(), true)
			.orElseThrow(()
				-> new EbeguEntityNotFoundException(
				"updateFinSitFromAufteilung",
				ErrorCodeEnum.ERROR_ENTITY_NOT_FOUND,
				"GesuchId invalid: " + gesuchId)
			);

		Objects.requireNonNull(gesuch.getGesuchsteller1());
		Objects.requireNonNull(gesuch.getGesuchsteller2());
		Objects.requireNonNull(gesuch.getGesuchsteller1().getFinanzielleSituationContainer());
		Objects.requireNonNull(gesuch.getGesuchsteller2().getFinanzielleSituationContainer());

		this.finanzielleSituationService.setValuesFromAufteilungDTO(
			gesuch.getGesuchsteller1().getFinanzielleSituationContainer().getFinanzielleSituationJA(),
			gesuch.getGesuchsteller2().getFinanzielleSituationContainer().getFinanzielleSituationJA(),
			jaxFinanzielleSituationAufteilungDTO
		);

		this.finanzielleSituationService.saveFinanzielleSituation(gesuch.getGesuchsteller1()
			.getFinanzielleSituationContainer(), gesuchId.getId());
		this.finanzielleSituationService.saveFinanzielleSituation(gesuch.getGesuchsteller2()
			.getFinanzielleSituationContainer(), gesuchId.getId());

		return Response.ok().build();
	}

	private GesuchstellerContainer findGesuchstellerById(
		@Nonnull String gesuchstellerId,
		@Nonnull String methodeName) {
		return gesuchstellerService.findGesuchsteller(gesuchstellerId).orElseThrow(()
			-> new EbeguEntityNotFoundException(
			methodeName,
			ErrorCodeEnum.ERROR_ENTITY_NOT_FOUND,
			"GesuchstellerId invalid: " + gesuchstellerId));
	}
}
