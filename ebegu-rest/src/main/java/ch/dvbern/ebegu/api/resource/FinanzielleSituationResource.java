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

import java.math.BigDecimal;
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
import ch.dvbern.ebegu.authentication.PrincipalBean;
import ch.dvbern.ebegu.dto.FinanzielleSituationResultateDTO;
import ch.dvbern.ebegu.dto.FinanzielleSituationStartDTO;
import ch.dvbern.ebegu.dto.JaxFinanzielleSituationAufteilungDTO;
import ch.dvbern.ebegu.entities.Adresse;
import ch.dvbern.ebegu.entities.Benutzer;
import ch.dvbern.ebegu.entities.Familiensituation;
import ch.dvbern.ebegu.entities.FamiliensituationContainer;
import ch.dvbern.ebegu.entities.FinanzielleSituation;
import ch.dvbern.ebegu.entities.FinanzielleSituationContainer;
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.entities.GesuchstellerContainer;
import ch.dvbern.ebegu.entities.SteuerdatenResponse;
import ch.dvbern.ebegu.enums.ErrorCodeEnum;
import ch.dvbern.ebegu.enums.FinanzielleSituationTyp;
import ch.dvbern.ebegu.enums.SteuerdatenAnfrageStatus;
import ch.dvbern.ebegu.errors.EbeguEntityNotFoundException;
import ch.dvbern.ebegu.errors.KiBonAnfrageServiceException;
import ch.dvbern.ebegu.services.FamiliensituationService;
import ch.dvbern.ebegu.services.FinanzielleSituationService;
import ch.dvbern.ebegu.services.GesuchService;
import ch.dvbern.ebegu.services.GesuchstellerService;
import ch.dvbern.ebegu.services.KibonAnfrageService;
import ch.dvbern.ebegu.util.MathUtil;
import com.google.common.base.Strings;
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

	protected static final MathUtil GANZZAHL = MathUtil.GANZZAHL;

	@Inject
	private FinanzielleSituationService finanzielleSituationService;
	@Inject
	private GesuchstellerService gesuchstellerService;

	@Inject
	private FamiliensituationService familiensituationService;

	@Inject
	private KibonAnfrageService kibonAnfrageService;

	@Inject
	private GesuchService gesuchService;

	@Inject
	private PrincipalBean principalBean;

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

		GesuchstellerContainer gesuchsteller =  findGesuchstellerById(gesuchstellerId, "saveFinanzielleSituation");

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

		if (familiensituationJA.isAbweichendeZahlungsadresseMahlzeiten()) {
			requireNonNull(familiensituationJA.getZahlungsadresseMahlzeiten());
		}

		if (familiensituationJA.isKeineMahlzeitenverguenstigungBeantragt()) {
			familiensituationJA.setIbanMahlzeiten(null);
			familiensituationJA.setKontoinhaberMahlzeiten(null);
			familiensituationJA.setAbweichendeZahlungsadresseMahlzeiten(false);
			familiensituationJA.setZahlungsadresseMahlzeiten(null);
		}

		Adresse storedAdresseMahlzeit = new Adresse();
		Adresse storedAdresseInfoma = new Adresse();
		if (jaxFamiliensituationContainer.getId() != null) {
			Optional<FamiliensituationContainer> storedFamSitContOptional =
				familiensituationService.findFamiliensituation(jaxFamiliensituationContainer.getId());

			if (storedFamSitContOptional.isPresent()) {
				Familiensituation storedFamSit = storedFamSitContOptional.get().getFamiliensituationJA();
				if (storedFamSit != null
					&& storedFamSit.getAuszahlungsdatenMahlzeiten() != null
					&& storedFamSit.getAuszahlungsdatenMahlzeiten().getAdresseKontoinhaber() != null) {
					storedAdresseMahlzeit = storedFamSit.getAuszahlungsdatenMahlzeiten().getAdresseKontoinhaber();
				}
				if (storedFamSit != null
					&& storedFamSit.getAuszahlungsdatenInfoma() != null
					&& storedFamSit.getAuszahlungsdatenInfoma().getAdresseKontoinhaber() != null) {
					storedAdresseInfoma = storedFamSit.getAuszahlungsdatenInfoma().getAdresseKontoinhaber();
				}
			}
		}

		Boolean sozialhilfeBezueger = familiensituationJA.getSozialhilfeBezueger();
		Boolean gemeinsameSteuererklaerung = familiensituationJA.getGemeinsameSteuererklaerung();
		Boolean verguenstigungGewuenscht = familiensituationJA.getVerguenstigungGewuenscht();

		if (gesuchJAXP.getFinSitTyp().equals(FinanzielleSituationTyp.BERN)
			|| gesuchJAXP.getFinSitTyp().equals(FinanzielleSituationTyp.BERN_FKJV)
			|| gesuchJAXP.getFinSitTyp().equals(FinanzielleSituationTyp.SOLOTHURN)) {
			requireNonNull(sozialhilfeBezueger);
			requireNonNull(gemeinsameSteuererklaerung);

			if (sozialhilfeBezueger.equals(Boolean.TRUE)) {
				// Sozialhilfebezueger bekommen immer eine Verguenstigung
				verguenstigungGewuenscht = Boolean.TRUE;
			} else {
				requireNonNull(verguenstigungGewuenscht);
			}
		} else {
			sozialhilfeBezueger = false;
			verguenstigungGewuenscht = Boolean.TRUE;
		}

		FinanzielleSituationStartDTO finSitStartDTO = new FinanzielleSituationStartDTO(
			sozialhilfeBezueger,
			gemeinsameSteuererklaerung,
			verguenstigungGewuenscht,
			familiensituationJA.isKeineMahlzeitenverguenstigungBeantragt(),
			familiensituationJA.getIbanMahlzeiten(),
			familiensituationJA.getKontoinhaberMahlzeiten(),
			familiensituationJA.isAbweichendeZahlungsadresseMahlzeiten(),
			familiensituationJA.getZahlungsadresseMahlzeiten() == null ? null :
				converter.adresseToEntity(familiensituationJA.getZahlungsadresseMahlzeiten(), storedAdresseMahlzeit),
			familiensituationJA.getIbanInfoma(),
			familiensituationJA.getKontoinhaberInfoma(),
			familiensituationJA.isAbweichendeZahlungsadresseInfoma(),
			familiensituationJA.getZahlungsadresseInfoma() == null ? null :
				converter.adresseToEntity(familiensituationJA.getZahlungsadresseInfoma(), storedAdresseInfoma),
			familiensituationJA.getInfomaKreditorennummer(),
			familiensituationJA.getInfomaBankcode()
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
		}
		if (jaxFinSitModel.getFinanzielleSituationContainerGS2() != null) {
			gesuch.setGesuchsteller2(new GesuchstellerContainer());
			//noinspection ConstantConditions
			gesuch.getGesuchsteller2().setFinanzielleSituationContainer(
				converter.finanzielleSituationContainerToEntity(
					jaxFinSitModel.getFinanzielleSituationContainerGS2(),
					new FinanzielleSituationContainer()));
		}

		FinanzielleSituationResultateDTO finanzielleSituationResultateDTO =
			finanzielleSituationService.calculateResultate(gesuch);
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
	@Path("/kibonanfrage/{kibonAnfrageId}/{gesuchstellerId}/{isGemeinsam}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed({GESUCHSTELLER })
	@TransactionAttribute(TransactionAttributeType.NEVER)
	public JaxFinanzielleSituationContainer updateFinSitMitSteuerdaten(
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

		assert gesuch.getFamiliensituationContainer() != null;
		assert gesuch.getFamiliensituationContainer().getFamiliensituationJA() != null;

		//FinSit Suchen, Feldern updaten
		GesuchstellerContainer gesuchsteller = findGesuchstellerById(jaxGesuchstellerId.getId(), "updateFinSitMitSteuerdaten");

		FinanzielleSituationContainer convertedFinSitCont = converter.finanzielleSituationContainerToStorableEntity(
			jaxFinanzielleSituationContainer,
			gesuchsteller.getFinanzielleSituationContainer());
		convertedFinSitCont.setGesuchsteller(gesuchsteller);

		// if GS1 has no ZPV-Nummer, we can abort early
		Benutzer benutzer = principalBean.getBenutzer();
		if (gesuch.getEingangsart().isPapierGesuch() || Strings.isNullOrEmpty(benutzer.getZpvNummer())) {
			updateFinSitSteuerdatenAbfrageStatusFailed(convertedFinSitCont.getFinanzielleSituationJA(), SteuerdatenAnfrageStatus.FAILED_KEINE_ZPV_NUMMER);

			FinanzielleSituationContainer persistedFinSit =
					this.finanzielleSituationService.saveFinanzielleSituationTemp(convertedFinSitCont);
			return converter.finanzielleSituationContainerToJAX(persistedFinSit);
		}

		boolean hasTwoAntragStellende = gesuch.getGesuchsteller2() != null;
		// TODO: find out where hasTwoAntragStellende can be false and steuererklaerungGemeinsam true
		final boolean steuererklaerungGemeinsam = Boolean.TRUE.equals(gesuch.getFamiliensituationContainer()
				.getFamiliensituationJA()
				.getGemeinsameSteuererklaerung());

		if (hasTwoAntragStellende && steuererklaerungGemeinsam) {
				// init finSitGS2 START
				FinanzielleSituationContainer finSitGS2Cont =  gesuch.getGesuchsteller2().getFinanzielleSituationContainer() != null ?
						gesuch.getGesuchsteller2().getFinanzielleSituationContainer() :
						new FinanzielleSituationContainer();
				if (finSitGS2Cont.getFinanzielleSituationJA() == null) {
					finSitGS2Cont.setFinanzielleSituationJA(new FinanzielleSituation());
				}
				finSitGS2Cont.setJahr(convertedFinSitCont.getJahr());
				finSitGS2Cont.getFinanzielleSituationJA().setSteuerdatenZugriff(true);
				finSitGS2Cont.getFinanzielleSituationJA().setSteuererklaerungAusgefuellt(convertedFinSitCont.getFinanzielleSituationJA().getSteuererklaerungAusgefuellt());
				finSitGS2Cont.getFinanzielleSituationJA().setSteuerveranlagungErhalten(convertedFinSitCont.getFinanzielleSituationJA().getSteuerveranlagungErhalten());
				finSitGS2Cont.setGesuchsteller(gesuch.getGesuchsteller2());
				// init finSitGS2 END

				try {
					// try gemeinsame Steuererklärung anfrage
					SteuerdatenResponse steuerdatenResponse = kibonAnfrageService.getSteuerDaten(
							Integer.valueOf(benutzer.getZpvNummer()),
							gesuchsteller.getGesuchstellerJA().getGeburtsdatum(),
							kibonAnfrageId.getId(),
							gesuch.getGesuchsperiode().getBasisJahr());
					handleSteuerdatenGemeinsamResponse(convertedFinSitCont.getFinanzielleSituationJA(), finSitGS2Cont.getFinanzielleSituationJA(), steuerdatenResponse, gesuch);
					this.finanzielleSituationService.saveFinanzielleSituationTemp(finSitGS2Cont);
				} catch (KiBonAnfrageServiceException e) {
					updateFinSitSteuerdatenAbfrageStatusFailed(
							convertedFinSitCont.getFinanzielleSituationJA(),
							SteuerdatenAnfrageStatus.FAILED);
					updateFinSitSteuerdatenAbfrageStatusFailed(
							finSitGS2Cont.getFinanzielleSituationJA(),
							SteuerdatenAnfrageStatus.FAILED);
			}

		} else {
			// anfrage single GS
			final boolean isGS2 = gesuchsteller.equals(gesuch.getGesuchsteller2());
			String zpvNummer = isGS2 ? gesuchsteller.getGesuchstellerJA().getZpvNummer() : benutzer.getZpvNummer();
			if (zpvNummer != null) {
				try {
					SteuerdatenResponse steuerdatenResponseGS1 = kibonAnfrageService.getSteuerDaten(
							Integer.valueOf(zpvNummer),
							gesuchsteller.getGesuchstellerJA().getGeburtsdatum(),
							kibonAnfrageId.getId(),
							gesuch.getGesuchsperiode().getBasisJahr());
					handleSteuerdatenResponse(convertedFinSitCont.getFinanzielleSituationJA(), steuerdatenResponseGS1);
				} catch (KiBonAnfrageServiceException e) {
					updateFinSitSteuerdatenAbfrageStatusFailed(
							convertedFinSitCont.getFinanzielleSituationJA(),
							SteuerdatenAnfrageStatus.FAILED);
				}
			} else {
				updateFinSitSteuerdatenAbfrageStatusFailed(convertedFinSitCont.getFinanzielleSituationJA(),
						isGS2 ?
								SteuerdatenAnfrageStatus.FAILED_KEINE_ZPV_NUMMER_GS2 :
								SteuerdatenAnfrageStatus.FAILED_KEINE_ZPV_NUMMER);
			}
		}
		//und zusendlich speichern und zuruckgeben, GS2 speichern wir wo nötig
		FinanzielleSituationContainer persistedFinSit =
			this.finanzielleSituationService.saveFinanzielleSituationTemp(convertedFinSitCont);
		return converter.finanzielleSituationContainerToJAX(persistedFinSit);
	}

	/**
	 * handle steuerdatenResponse for single GS
	 */
	private void handleSteuerdatenResponse(
			FinanzielleSituation finSit,
			SteuerdatenResponse steuerdatenResponse) {
		if (steuerdatenResponse.getUnterjaehrigerFall() != null && steuerdatenResponse.getUnterjaehrigerFall()) {
			updateFinSitSteuerdatenAbfrageStatusFailed(
					finSit,
					SteuerdatenAnfrageStatus.FAILED_UNTERJAEHRIGER_FALL);
		} else if (steuerdatenResponse.getZpvNrPartner() != null) {
			updateFinSitSteuerdatenAbfrageStatusFailed(
					finSit,
					SteuerdatenAnfrageStatus.FAILED_PARTNER_NICHT_GEMEINSAM);
		} else {
			updateFinSitSteuerdatenAbfrageStatusOk(finSit, steuerdatenResponse);
		}
	}

	private void updateFinSitSteuerdatenAbfrageStatusOk(
			FinanzielleSituation finSit,
			SteuerdatenResponse steuerdatenResponse) {
		assert steuerdatenResponse.getZpvNrPartner() == null;

		if (steuerdatenResponse.getVeranlagungsstand() != null) {
			finSit.setSteuerdatenAbfrageStatus(SteuerdatenAnfrageStatus.valueOf(steuerdatenResponse.getVeranlagungsstand()
							.name()));
		}
		// Pflichtfeldern wenn null muessen zu 0 gesetzt werden, Sie sind nicht editierbar im Formular
		finSit.setNettolohn(steuerdatenResponse.getErwerbseinkommenUnselbstaendigkeitDossiertraeger() != null ?
						steuerdatenResponse.getErwerbseinkommenUnselbstaendigkeitDossiertraeger() :
						BigDecimal.ZERO);
		finSit.setFamilienzulage(steuerdatenResponse.getWeitereSteuerbareEinkuenfteDossiertraeger() != null ?
						steuerdatenResponse.getWeitereSteuerbareEinkuenfteDossiertraeger() :
						BigDecimal.ZERO);
		finSit.setErsatzeinkommen(steuerdatenResponse.getSteuerpflichtigesErsatzeinkommenDossiertraeger() != null ?
						steuerdatenResponse.getSteuerpflichtigesErsatzeinkommenDossiertraeger() :
						BigDecimal.ZERO);
		finSit.setErhalteneAlimente(steuerdatenResponse.getErhalteneUnterhaltsbeitraegeDossiertraeger() != null ?
						steuerdatenResponse.getErhalteneUnterhaltsbeitraegeDossiertraeger() :
						BigDecimal.ZERO);
		finSit.setNettoertraegeErbengemeinschaft(steuerdatenResponse.getNettoertraegeAusEgmeDossiertraeger() != null ?
						steuerdatenResponse.getNettoertraegeAusEgmeDossiertraeger() :
						BigDecimal.ZERO);
		finSit.setGeleisteteAlimente(steuerdatenResponse.getGeleisteteUnterhaltsbeitraege() != null ?
						steuerdatenResponse.getGeleisteteUnterhaltsbeitraege() :
						BigDecimal.ZERO);
		finSit.setAbzugSchuldzinsen(steuerdatenResponse.getSchuldzinsen() != null ? steuerdatenResponse.getSchuldzinsen() : BigDecimal.ZERO);


		// Die Geschaeftsgewinn Feldern muessen unbedingt null bleiben wenn null wegen die Berechnung
		finSit.setGeschaeftsgewinnBasisjahr(steuerdatenResponse.getAusgewiesenerGeschaeftsertragDossiertraeger());
		finSit.setGeschaeftsgewinnBasisjahrMinus1(steuerdatenResponse.getAusgewiesenerGeschaeftsertragVorperiodeDossiertraeger());
		finSit.setGeschaeftsgewinnBasisjahrMinus2(steuerdatenResponse.getAusgewiesenerGeschaeftsertragVorperiode2Dossiertraeger());

		// Berechnete Feldern - diese können null bleiben als Sie sind editierbar im Formular
		BigDecimal bruttertraegeVermogenTotal =
				GANZZAHL.addNullSafe(steuerdatenResponse.getBruttoertraegeAusLiegenschaften() != null ?
						steuerdatenResponse.getBruttoertraegeAusLiegenschaften() :
						BigDecimal.ZERO, steuerdatenResponse.getBruttoertraegeAusVermoegenOhneLiegenschaftenUndOhneEgme());

		finSit.setBruttoertraegeVermoegen(bruttertraegeVermogenTotal);
		BigDecimal gewinnungskostenTotal =
				GANZZAHL.addNullSafe(steuerdatenResponse.getGewinnungskostenBeweglichesVermoegen() != null ?
						steuerdatenResponse.getGewinnungskostenBeweglichesVermoegen() :
						BigDecimal.ONE, steuerdatenResponse.getLiegenschaftsAbzuege());
		finSit.setGewinnungskosten(gewinnungskostenTotal);
		finSit.setNettoVermoegen(steuerdatenResponse.getNettovermoegen());
	}

	private void updateFinSitSteuerdatenAbfrageStatusFailed(
			FinanzielleSituation finSitGS1,
			SteuerdatenAnfrageStatus steuerdatenAnfrageStatus) {
		finSitGS1.setSteuerdatenAbfrageStatus(steuerdatenAnfrageStatus);
	}

	private void handleSteuerdatenGemeinsamResponse(
			FinanzielleSituation convertedFinSitCont,
			FinanzielleSituation finSitGS2,
			SteuerdatenResponse steuerdatenResponse,
			Gesuch gesuch) {
		if (steuerdatenResponse.getUnterjaehrigerFall() != null && steuerdatenResponse.getUnterjaehrigerFall()) {
			updateFinSitSteuerdatenAbfrageGemeinsamStatusFailed(
					convertedFinSitCont,
					finSitGS2,
					SteuerdatenAnfrageStatus.FAILED_UNTERJAEHRIGER_FALL);
		} else if (steuerdatenResponse.getZpvNrPartner() == null) {
			updateFinSitSteuerdatenAbfrageGemeinsamStatusFailed(
					convertedFinSitCont,
					finSitGS2,
					SteuerdatenAnfrageStatus.FAILED_KEIN_PARTNER_GEMEINSAM);
		} else if (requireNonNull(gesuch.getGesuchsteller2()).getGesuchstellerJA().getGeburtsdatum().compareTo(
				requireNonNull(steuerdatenResponse.getGeburtsdatumPartner())) != 0) {
			updateFinSitSteuerdatenAbfrageGemeinsamStatusFailed(
					convertedFinSitCont,
					finSitGS2,
					SteuerdatenAnfrageStatus.FAILED_GEBURTSDATUM);
		} else {
			updateFinSitSteuerdatenAbfrageGemeinsamStatusOk(convertedFinSitCont, finSitGS2, steuerdatenResponse);
		}
	}

	private void updateFinSitSteuerdatenAbfrageGemeinsamStatusFailed(
			@Nonnull FinanzielleSituation finSitGS1,
			@Nonnull FinanzielleSituation finSitGS2,
			SteuerdatenAnfrageStatus steuerdatenAnfrageStatus) {
		finSitGS1.setSteuerdatenAbfrageStatus(steuerdatenAnfrageStatus);
		finSitGS2.setSteuerdatenAbfrageStatus(steuerdatenAnfrageStatus);

	}

	private void updateFinSitSteuerdatenAbfrageGemeinsamStatusOk(
			@Nonnull FinanzielleSituation convertedFinSitCont,
			@Nonnull FinanzielleSituation finSitGS2,
			SteuerdatenResponse steuerdatenResponse) {
		assert steuerdatenResponse.getZpvNrPartner() != null;

		if (steuerdatenResponse.getVeranlagungsstand() != null) {
			convertedFinSitCont.setSteuerdatenAbfrageStatus(SteuerdatenAnfrageStatus.valueOf(steuerdatenResponse.getVeranlagungsstand()
							.name()));
				finSitGS2.setSteuerdatenAbfrageStatus(SteuerdatenAnfrageStatus.valueOf(steuerdatenResponse.getVeranlagungsstand()
								.name()));
		}
		// Pflichtfeldern wenn null muessen zu 0 gesetzt werden, Sie sind nicht editierbar im Formular
		convertedFinSitCont.setNettolohn(steuerdatenResponse.getErwerbseinkommenUnselbstaendigkeitDossiertraeger() != null ?
						steuerdatenResponse.getErwerbseinkommenUnselbstaendigkeitDossiertraeger() :
						BigDecimal.ZERO);
		convertedFinSitCont.setFamilienzulage(steuerdatenResponse.getWeitereSteuerbareEinkuenfteDossiertraeger() != null ?
						steuerdatenResponse.getWeitereSteuerbareEinkuenfteDossiertraeger() :
						BigDecimal.ZERO);
		convertedFinSitCont.setErsatzeinkommen(steuerdatenResponse.getSteuerpflichtigesErsatzeinkommenDossiertraeger() != null ?
						steuerdatenResponse.getSteuerpflichtigesErsatzeinkommenDossiertraeger() :
						BigDecimal.ZERO);
		convertedFinSitCont.setErhalteneAlimente(steuerdatenResponse.getErhalteneUnterhaltsbeitraegeDossiertraeger() != null ?
						steuerdatenResponse.getErhalteneUnterhaltsbeitraegeDossiertraeger() :
						BigDecimal.ZERO);
		convertedFinSitCont.setNettoertraegeErbengemeinschaft(steuerdatenResponse.getNettoertraegeAusEgmeDossiertraeger() != null ?
						steuerdatenResponse.getNettoertraegeAusEgmeDossiertraeger() :
						BigDecimal.ZERO);

		// Die Geschaeftsgewinn Feldern muessen unbedingt null bleiben wenn null wegen die Berechnung
		convertedFinSitCont.setGeschaeftsgewinnBasisjahr(steuerdatenResponse.getAusgewiesenerGeschaeftsertragDossiertraeger());
		convertedFinSitCont.setGeschaeftsgewinnBasisjahrMinus1(steuerdatenResponse.getAusgewiesenerGeschaeftsertragVorperiodeDossiertraeger());
		convertedFinSitCont.setGeschaeftsgewinnBasisjahrMinus2(steuerdatenResponse.getAusgewiesenerGeschaeftsertragVorperiode2Dossiertraeger());

		// Berechnete Feldern - diese können null bleiben als Sie sind editierbar im Formular
		BigDecimal bruttertraegeVermogenTotal =
				GANZZAHL.addNullSafe(
						steuerdatenResponse.getBruttoertraegeAusLiegenschaften() != null ?
								steuerdatenResponse.getBruttoertraegeAusLiegenschaften() :
								BigDecimal.ZERO,
						steuerdatenResponse.getBruttoertraegeAusVermoegenOhneLiegenschaftenUndOhneEgme());
		convertedFinSitCont.setBruttoertraegeVermoegen(GANZZAHL.divide(bruttertraegeVermogenTotal, new BigDecimal(2)));
		convertedFinSitCont.setAbzugSchuldzinsen(GANZZAHL.divide(steuerdatenResponse.getSchuldzinsen() != null ?
						steuerdatenResponse.getSchuldzinsen() :
						BigDecimal.ZERO, new BigDecimal(2)));
		BigDecimal gewinnungskostenTotal =
				GANZZAHL.addNullSafe(steuerdatenResponse.getGewinnungskostenBeweglichesVermoegen() != null ?
						steuerdatenResponse.getGewinnungskostenBeweglichesVermoegen() :
						BigDecimal.ONE, steuerdatenResponse.getLiegenschaftsAbzuege());
		convertedFinSitCont.setGewinnungskosten(GANZZAHL.divide(gewinnungskostenTotal, new BigDecimal(2)));
		convertedFinSitCont.setGeleisteteAlimente(GANZZAHL.divide(steuerdatenResponse.getGeleisteteUnterhaltsbeitraege() != null ?
						steuerdatenResponse.getGeleisteteUnterhaltsbeitraege() :
						BigDecimal.ZERO, new BigDecimal(2)));
		convertedFinSitCont.setNettoVermoegen(GANZZAHL.divide(steuerdatenResponse.getNettovermoegen() != null ?
						steuerdatenResponse.getNettovermoegen() :
						BigDecimal.ZERO, new BigDecimal(2)));

		finSitGS2.setNettolohn(steuerdatenResponse.getErwerbseinkommenUnselbstaendigkeitPartner() != null ?
						steuerdatenResponse.getErwerbseinkommenUnselbstaendigkeitPartner() :
						BigDecimal.ZERO);
		finSitGS2.setFamilienzulage(steuerdatenResponse.getWeitereSteuerbareEinkuenftePartner() != null ?
						steuerdatenResponse.getWeitereSteuerbareEinkuenftePartner() :
						BigDecimal.ZERO);
		finSitGS2.setErsatzeinkommen(steuerdatenResponse.getSteuerpflichtigesErsatzeinkommenPartner() != null ?
						steuerdatenResponse.getSteuerpflichtigesErsatzeinkommenPartner() :
						BigDecimal.ZERO);
		finSitGS2.setErhalteneAlimente(steuerdatenResponse.getErhalteneUnterhaltsbeitraegePartner() != null ?
						steuerdatenResponse.getErhalteneUnterhaltsbeitraegePartner() :
						BigDecimal.ZERO);
		finSitGS2.setNettoertraegeErbengemeinschaft(steuerdatenResponse.getNettoertraegeAusEgmePartner() != null ?
						steuerdatenResponse.getNettoertraegeAusEgmePartner() :
						BigDecimal.ZERO);

		finSitGS2.setGeschaeftsgewinnBasisjahr(steuerdatenResponse.getAusgewiesenerGeschaeftsertragPartner());
		finSitGS2.setGeschaeftsgewinnBasisjahrMinus1(steuerdatenResponse.getAusgewiesenerGeschaeftsertragVorperiodePartner());
		finSitGS2.setGeschaeftsgewinnBasisjahrMinus2(steuerdatenResponse.getAusgewiesenerGeschaeftsertragVorperiode2Partner());

		finSitGS2.setBruttoertraegeVermoegen(GANZZAHL.divide(bruttertraegeVermogenTotal, new BigDecimal(2)));
		finSitGS2.setAbzugSchuldzinsen(GANZZAHL.divide(steuerdatenResponse.getSchuldzinsen() != null ?
						steuerdatenResponse.getSchuldzinsen() :
						BigDecimal.ZERO, new BigDecimal(2)));
		finSitGS2.setGewinnungskosten(GANZZAHL.divide(gewinnungskostenTotal, new BigDecimal(2)));
		finSitGS2.setGeleisteteAlimente(GANZZAHL.divide(steuerdatenResponse.getGeleisteteUnterhaltsbeitraege() != null ?
						steuerdatenResponse.getGeleisteteUnterhaltsbeitraege() :
						BigDecimal.ZERO, new BigDecimal(2)));
		finSitGS2.setNettoVermoegen(GANZZAHL.divide(steuerdatenResponse.getNettovermoegen() != null ?
						steuerdatenResponse.getNettovermoegen() :
						BigDecimal.ZERO, new BigDecimal(2)));
	}

	@ApiOperation(value = "reset die FinSit Status und Nettovermoegen falls gesetzt"
		+ "Ergebniss",
		response = SteuerdatenResponse.class)
	@Nullable
	@PUT
	@Path("/kibonanfrage/reset/{kibonAnfrageId}/{gesuchstellerId}/{isGemeinsam}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed({GESUCHSTELLER })
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
		GesuchstellerContainer gesuchsteller = findGesuchstellerById(jaxGesuchstellerId.getId(), "resetFinSitSteuerdaten");

		FinanzielleSituationContainer convertedFinSitCont = converter.finanzielleSituationContainerToStorableEntity(
			jaxFinanzielleSituationContainer,
			gesuchsteller.getFinanzielleSituationContainer());
		convertedFinSitCont.setGesuchsteller(gesuchsteller);

		// reset SteuerdatenAbfrageStatus und NettoVermoegen
		convertedFinSitCont.getFinanzielleSituationJA().setSteuerdatenAbfrageStatus(null);
		convertedFinSitCont.getFinanzielleSituationJA().setNettoVermoegen(null);
		convertedFinSitCont.getFinanzielleSituationJA().setSteuerdatenZugriff(false);

		// auch fuer GS2 wenn gemeinsam
		if (isGemeinsam && gesuch.getGesuchsteller2() != null && gesuch.getGesuchsteller2().getFinanzielleSituationContainer() != null
			&& gesuch.getGesuchsteller2().getFinanzielleSituationContainer().getFinanzielleSituationJA() != null) {
			FinanzielleSituationContainer finSitGS2 = gesuch.getGesuchsteller2().getFinanzielleSituationContainer();
			finSitGS2.getFinanzielleSituationJA().setSteuerdatenZugriff(false);
			finSitGS2.getFinanzielleSituationJA().setSteuerdatenAbfrageStatus(null);
			finSitGS2.getFinanzielleSituationJA().setNettoVermoegen(null);
			this.finanzielleSituationService.saveFinanzielleSituationTemp(finSitGS2);
		}

		//und zusendlich speichern und zuruckgeben
		FinanzielleSituationContainer persistedFinSit =
			this.finanzielleSituationService.saveFinanzielleSituationTemp(convertedFinSitCont);
		return converter.finanzielleSituationContainerToJAX(persistedFinSit);
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

		this.finanzielleSituationService.saveFinanzielleSituation(gesuch.getGesuchsteller1().getFinanzielleSituationContainer(), gesuchId.getId());
		this.finanzielleSituationService.saveFinanzielleSituation(gesuch.getGesuchsteller2().getFinanzielleSituationContainer(), gesuchId.getId());

		return Response.ok().build();
	}

	private GesuchstellerContainer findGesuchstellerById(@Nonnull String gesuchstellerId, @Nonnull String methodeName) {
		return gesuchstellerService.findGesuchsteller(gesuchstellerId).orElseThrow(()
			-> new EbeguEntityNotFoundException(
			methodeName,
			ErrorCodeEnum.ERROR_ENTITY_NOT_FOUND,
			"GesuchstellerId invalid: " + gesuchstellerId));
	}
}
