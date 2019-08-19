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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import javax.validation.Validation;
import javax.validation.Validator;
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
import ch.dvbern.ebegu.api.dtos.JaxAnmeldungDTO;
import ch.dvbern.ebegu.api.dtos.JaxBetreuung;
import ch.dvbern.ebegu.api.dtos.JaxBetreuungspensumAbweichung;
import ch.dvbern.ebegu.api.dtos.JaxId;
import ch.dvbern.ebegu.api.resource.util.BetreuungUtil;
import ch.dvbern.ebegu.api.resource.util.ResourceHelper;
import ch.dvbern.ebegu.entities.AnmeldungFerieninsel;
import ch.dvbern.ebegu.entities.AnmeldungTagesschule;
import ch.dvbern.ebegu.entities.Betreuung;
import ch.dvbern.ebegu.entities.BetreuungspensumAbweichung;
import ch.dvbern.ebegu.entities.Dossier;
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.entities.KindContainer;
import ch.dvbern.ebegu.enums.BetreuungsangebotTyp;
import ch.dvbern.ebegu.enums.Betreuungsstatus;
import ch.dvbern.ebegu.enums.ErrorCodeEnum;
import ch.dvbern.ebegu.errors.EbeguEntityNotFoundException;
import ch.dvbern.ebegu.errors.EbeguRuntimeException;
import ch.dvbern.ebegu.errors.KibonLogLevel;
import ch.dvbern.ebegu.services.BetreuungService;
import ch.dvbern.ebegu.services.DossierService;
import ch.dvbern.ebegu.services.GesuchService;
import ch.dvbern.ebegu.services.KindService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang.Validate;

/**
 * REST Resource fuer Betreuungen. Betreuung = ein Kind in einem Betreuungsangebot bei einer Institution.
 */
@Path("betreuungen")
@Stateless
@Api(description = "Resource zum Verwalten von Betreuungen (Ein Betreuungsangebot für ein Kind)")
public class BetreuungResource {

	public static final String KIND_CONTAINER_ID_INVALID = "KindContainerId invalid: ";

	@Inject
	private BetreuungService betreuungService;
	@Inject
	private KindService kindService;
	@Inject
	private DossierService dossierService;
	@Inject
	private JaxBConverter converter;
	@Inject
	private ResourceHelper resourceHelper;
	@Inject
	private GesuchService gesuchService;

	@ApiOperation(value = "Speichert eine Betreuung in der Datenbank", response = JaxBetreuung.class)
	@Nonnull
	@PUT
	@Path("/betreuung/{abwesenheit}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public JaxBetreuung saveBetreuung(
		@Nonnull @NotNull @Valid JaxBetreuung betreuungJAXP,
		@Nonnull @NotNull @PathParam("abwesenheit") Boolean abwesenheit,
		@Context UriInfo uriInfo,
		@Context HttpServletResponse response) {

		Objects.requireNonNull(betreuungJAXP.getKindId());

		Optional<KindContainer> kind = kindService.findKind(betreuungJAXP.getKindId());
		if (kind.isPresent()) {
			KindContainer kindContainer = kind.get();
			BetreuungsangebotTyp betreuungsangebotTyp =
				betreuungJAXP.getInstitutionStammdaten().getBetreuungsangebotTyp();
			switch (betreuungsangebotTyp) {
			case TAGESSCHULE:
				return savePlatzAnmeldungTagesschule(betreuungJAXP, kindContainer, abwesenheit);
			case FERIENINSEL:
				return savePlatzAnmeldungFerieninsel(betreuungJAXP, kindContainer, abwesenheit);
			default:
				return savePlatzBetreuung(betreuungJAXP, kindContainer, abwesenheit);
			}
		}
		throw new EbeguEntityNotFoundException("saveBetreuung", ErrorCodeEnum.ERROR_ENTITY_NOT_FOUND,
			KIND_CONTAINER_ID_INVALID + betreuungJAXP.getKindId());
	}

	private JaxBetreuung savePlatzBetreuung(@Nonnull JaxBetreuung betreuungJAXP, @Nonnull KindContainer kindContainer,
		Boolean abwesenheit) {
		if (BetreuungUtil.hasDuplicateBetreuung(betreuungJAXP, kindContainer.getBetreuungen())) {
			throw new EbeguRuntimeException(KibonLogLevel.NONE, "savePlatzBetreuung",
				ErrorCodeEnum.ERROR_DUPLICATE_BETREUUNG);
		}
		Betreuung convertedBetreuung = converter.betreuungToStoreableEntity(betreuungJAXP);
		resourceHelper.assertGesuchStatusForBenutzerRole(kindContainer.getGesuch(), convertedBetreuung);

		convertedBetreuung.setKind(kindContainer);

		Betreuung persistedBetreuung = this.betreuungService.saveBetreuung(convertedBetreuung, abwesenheit);
		return converter.betreuungToJAX(persistedBetreuung);
	}

	private JaxBetreuung savePlatzAnmeldungTagesschule(@Nonnull JaxBetreuung betreuungJAXP,
		@Nonnull KindContainer kindContainer, Boolean abwesenheit) {
		if (BetreuungUtil.hasDuplicateAnmeldungTagesschule(betreuungJAXP, kindContainer.getAnmeldungenTagesschule())) {
			throw new EbeguRuntimeException(KibonLogLevel.NONE, "savePlatzAnmeldungTagesschule",
				ErrorCodeEnum.ERROR_DUPLICATE_BETREUUNG);
		}
		AnmeldungTagesschule convertedAnmeldungTagesschule =
			converter.anmeldungTagesschuleToStoreableEntity(betreuungJAXP);
		resourceHelper.assertGesuchStatusForBenutzerRole(kindContainer.getGesuch(), convertedAnmeldungTagesschule);

		convertedAnmeldungTagesschule.setKind(kindContainer);
		if (convertedAnmeldungTagesschule.isKeineDetailinformationen()) {
			// eine Anmeldung ohne Detailinformationen muss immer als Uebernommen gespeichert werden
			convertedAnmeldungTagesschule.setBetreuungsstatus(Betreuungsstatus.SCHULAMT_ANMELDUNG_UEBERNOMMEN);
		}

		AnmeldungTagesschule persistedAnmeldungTagesschule =
			this.betreuungService.saveAnmeldungTagesschule(convertedAnmeldungTagesschule, abwesenheit);
		return converter.anmeldungTagesschuleToJAX(persistedAnmeldungTagesschule);
	}

	private JaxBetreuung savePlatzAnmeldungFerieninsel(@Nonnull JaxBetreuung betreuungJAXP,
		@Nonnull KindContainer kindContainer, Boolean abwesenheit) {
		if (BetreuungUtil.hasDuplicateAnmeldungFerieninsel(betreuungJAXP, kindContainer.getAnmeldungenFerieninsel())) {
			throw new EbeguRuntimeException(KibonLogLevel.NONE, "savePlatzAnmeldungFerieninsel",
				ErrorCodeEnum.ERROR_DUPLICATE_BETREUUNG);
		}
		AnmeldungFerieninsel convertedAnmeldungFerieninsel =
			converter.anmeldungFerieninselToStoreableEntity(betreuungJAXP);
		resourceHelper.assertGesuchStatusForBenutzerRole(kindContainer.getGesuch(), convertedAnmeldungFerieninsel);

		convertedAnmeldungFerieninsel.setKind(kindContainer);

		AnmeldungFerieninsel persistedAnmeldungFerieninsel =
			this.betreuungService.saveAnmeldungFerieninsel(convertedAnmeldungFerieninsel, abwesenheit);
		return converter.anmeldungFerieninselToJAX(persistedAnmeldungFerieninsel);
	}

	@ApiOperation(value = "Speichert eine Abwesenheit in der Datenbank.", responseContainer = "List", response =
		JaxBetreuung.class)
	@Nonnull
	@PUT
	@Path("/all/{abwesenheit}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public List<JaxBetreuung> saveAbwesenheiten(
		List<JaxBetreuung> betreuungenJAXP,
		@Nonnull @PathParam("abwesenheit") Boolean abwesenheit,
		@Context UriInfo uriInfo,
		@Context HttpServletResponse response) {
		Validate.notNull(abwesenheit, "abwesenheit may not be null");
		Validate.notNull(betreuungenJAXP, "betreuungenJAXP may not be null");
		Validator validator = Validation.byDefaultProvider().configure().buildValidatorFactory().getValidator();
		betreuungenJAXP.forEach(jaxBetreuung -> validator.validate(jaxBetreuung));

		if (!betreuungenJAXP.isEmpty()) {
			final String gesuchId = betreuungenJAXP.get(0).getGesuchId();
			if (gesuchId != null) {
				final Optional<Gesuch> gesuchOpt = gesuchService.findGesuch(gesuchId);
				final Gesuch gesuch = gesuchOpt.orElseThrow(() -> new EbeguRuntimeException("saveAbwesenheiten",
					ErrorCodeEnum.ERROR_ENTITY_NOT_FOUND, gesuchId));
				resourceHelper.assertGesuchStatusForBenutzerRole(gesuch);
			}
		}

		List<JaxBetreuung> resultBetreuungen = new ArrayList<>();
		betreuungenJAXP.forEach(betreuungJAXP -> {
			Betreuung convertedBetreuung = converter.betreuungToStoreableEntity(betreuungJAXP);
			Betreuung persistedBetreuung = this.betreuungService.saveBetreuung(convertedBetreuung, abwesenheit);

			resultBetreuungen.add(converter.betreuungToJAX(persistedBetreuung));
		});
		return resultBetreuungen;
	}

	@ApiOperation(value = "Betreuungsplatzanfrage wird durch die Institution abgelehnt", response = JaxBetreuung.class)
	@Nonnull
	@PUT
	@Path("/abweisen")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public JaxBetreuung betreuungPlatzAbweisen(
		@Nonnull @NotNull @Valid JaxBetreuung betreuungJAXP,
		@Context UriInfo uriInfo,
		@Context HttpServletResponse response) {

		Objects.requireNonNull(betreuungJAXP.getId());
		Objects.requireNonNull(betreuungJAXP.getKindId());

		// Sicherstellen, dass der Status des Server-Objektes genau dem erwarteten Status entspricht
		resourceHelper.assertBetreuungStatusEqual(betreuungJAXP.getId(), Betreuungsstatus.WARTEN);

		Betreuung convertedBetreuung = converter.betreuungToStoreableEntity(betreuungJAXP);

		// Sicherstellen, dass das dazugehoerige Gesuch ueberhaupt noch editiert werden darf fuer meine Rolle
		resourceHelper.assertGesuchStatusForBenutzerRole(convertedBetreuung.getKind().getGesuch());
		Betreuung persistedBetreuung = this.betreuungService.betreuungPlatzAbweisen(convertedBetreuung);

		return converter.betreuungToJAX(persistedBetreuung);
	}

	@ApiOperation(value = "Betreuungsplatzanfrage wird durch die Institution bestätigt", response = JaxBetreuung.class)
	@Nonnull
	@PUT
	@Path("/bestaetigen")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public JaxBetreuung betreuungPlatzBestaetigen(
		@Nonnull @NotNull @Valid JaxBetreuung betreuungJAXP,
		@Context UriInfo uriInfo,
		@Context HttpServletResponse response) {

		Objects.requireNonNull(betreuungJAXP.getId());
		Objects.requireNonNull(betreuungJAXP.getKindId());

		// Sicherstellen, dass der Status des Server-Objektes genau dem erwarteten Status entspricht
		resourceHelper.assertBetreuungStatusEqual(betreuungJAXP.getId(), Betreuungsstatus.WARTEN);

		Betreuung convertedBetreuung = converter.betreuungToStoreableEntity(betreuungJAXP);

		// Sicherstellen, dass das dazugehoerige Gesuch ueberhaupt noch editiert werden darf fuer meine Rolle
		resourceHelper.assertGesuchStatusForBenutzerRole(convertedBetreuung.getKind().getGesuch());
		Betreuung persistedBetreuung = this.betreuungService.betreuungPlatzBestaetigen(convertedBetreuung);

		return converter.betreuungToJAX(persistedBetreuung);
	}

	@ApiOperation(value = "Schulamt-Anmeldung wird durch die Institution bestätigt", response = JaxBetreuung.class)
	@Nonnull
	@PUT
	@Path("/schulamt/uebernehmen")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public JaxBetreuung anmeldungSchulamtUebernehmen(@Nonnull @NotNull @Valid JaxBetreuung betreuungJAXP,
		@Context UriInfo uriInfo,
		@Context HttpServletResponse response) {

		Objects.requireNonNull(betreuungJAXP.getId());
		Objects.requireNonNull(betreuungJAXP.getKindId());

		// Sicherstellen, dass der Status des Server-Objektes genau dem erwarteten Status entspricht
		resourceHelper.assertBetreuungStatusEqual(betreuungJAXP.getId(),
			Betreuungsstatus.SCHULAMT_ANMELDUNG_AUSGELOEST);


		Betreuung convertedBetreuung = converter.betreuungToStoreableEntity(betreuungJAXP);
		// Sicherstellen, dass das dazugehoerige Gesuch ueberhaupt noch editiert werden darf fuer meine Rolle
		resourceHelper.assertGesuchStatusForBenutzerRole(convertedBetreuung.getKind().getGesuch(), convertedBetreuung);
		Betreuung persistedBetreuung = this.betreuungService.anmeldungSchulamtUebernehmen(convertedBetreuung);

		return converter.betreuungToJAX(persistedBetreuung);
	}

	@ApiOperation(value = "Schulamt-Anmeldung wird durch die Institution abgelehnt", response = JaxBetreuung.class)
	@Nonnull
	@PUT
	@Path("/schulamt/ablehnen")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public JaxBetreuung anmeldungSchulamtAblehnen(@Nonnull @NotNull @Valid JaxBetreuung betreuungJAXP,
		@Context UriInfo uriInfo,
		@Context HttpServletResponse response) {

		Objects.requireNonNull(betreuungJAXP.getId());
		Objects.requireNonNull(betreuungJAXP.getKindId());

		// Sicherstellen, dass der Status des Server-Objektes genau dem erwarteten Status entspricht
		// Anmeldungen ablehnen kann man entweder im Status SCHULAMT_ANMELDUNG_AUSGELOEST oder
		// SCHULAMT_FALSCHE_INSTITUTION
		resourceHelper.assertBetreuungStatusEqual(betreuungJAXP.getId(),
		Betreuungsstatus.SCHULAMT_ANMELDUNG_AUSGELOEST, Betreuungsstatus.SCHULAMT_FALSCHE_INSTITUTION);

		Betreuung convertedBetreuung = converter.betreuungToStoreableEntity(betreuungJAXP);
		// Sicherstellen, dass das dazugehoerige Gesuch ueberhaupt noch editiert werden darf fuer meine Rolle
		resourceHelper.assertGesuchStatusForBenutzerRole(convertedBetreuung.getKind().getGesuch(), convertedBetreuung);
		Betreuung persistedBetreuung = this.betreuungService.anmeldungSchulamtAblehnen(convertedBetreuung);

		return converter.betreuungToJAX(persistedBetreuung);
	}

	@ApiOperation(value = "Schulamt-Anmeldung fuer falsche Institution gestellt", response = JaxBetreuung.class)
	@Nonnull
	@PUT
	@Path("/schulamt/falscheInstitution")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public JaxBetreuung anmeldungSchulamtFalscheInstitution(@Nonnull @NotNull @Valid JaxBetreuung betreuungJAXP,
		@Context UriInfo uriInfo,
		@Context HttpServletResponse response) {

		Objects.requireNonNull(betreuungJAXP.getId());
		Objects.requireNonNull(betreuungJAXP.getKindId());

		// Sicherstellen, dass der Status des Server-Objektes genau dem erwarteten Status entspricht
		resourceHelper.assertBetreuungStatusEqual(betreuungJAXP.getId(),
		Betreuungsstatus.SCHULAMT_ANMELDUNG_AUSGELOEST);

		Betreuung convertedBetreuung = converter.betreuungToStoreableEntity(betreuungJAXP);
		// Sicherstellen, dass das dazugehoerige Gesuch ueberhaupt noch editiert werden darf fuer meine Rolle
		resourceHelper.assertGesuchStatusForBenutzerRole(convertedBetreuung.getKind().getGesuch(), convertedBetreuung);
		Betreuung persistedBetreuung =
			this.betreuungService.anmeldungSchulamtFalscheInstitution(convertedBetreuung);

		return converter.betreuungToJAX(persistedBetreuung);
	}

	@ApiOperation(value = "Sucht die Betreuung mit der übergebenen Id in der Datenbank. Dabei wird geprüft, ob der " +
		"eingeloggte Benutzer für die gesuchte Betreuung berechtigt ist", response = JaxBetreuung.class)
	@Nullable
	@GET
	@Path("/{betreuungId}")
	@Consumes(MediaType.WILDCARD)
	@Produces(MediaType.APPLICATION_JSON)
	public JaxBetreuung findBetreuung(
		@Nonnull @NotNull @PathParam("betreuungId") JaxId betreuungJAXPId) {
		Objects.requireNonNull(betreuungJAXPId.getId());
		String id = converter.toEntityId(betreuungJAXPId);
		Optional<Betreuung> fallOptional = betreuungService.findBetreuung(id);

		if (!fallOptional.isPresent()) {
			return null;
		}
		Betreuung betreuungToReturn = fallOptional.get();
		return converter.betreuungToJAX(betreuungToReturn);
	}

	@ApiOperation("Löscht die Betreuung mit der übergebenen Id in der Datenbank. Dabei wird geprüft, ob der " +
		"eingeloggte Benutzer für die gesuchte Betreuung berechtigt ist")
	@Nullable
	@DELETE
	@Path("/{betreuungId}")
	@Consumes(MediaType.WILDCARD)
	@SuppressWarnings("NonBooleanMethodNameMayNotStartWithQuestion")
	public Response removeBetreuung(
		@Nonnull @NotNull @PathParam("betreuungId") JaxId betreuungJAXPId,
		@Context HttpServletResponse response) {

		Objects.requireNonNull(betreuungJAXPId.getId());
		Optional<Betreuung> betreuung = betreuungService.findBetreuung(betreuungJAXPId.getId());

		if (betreuung.isPresent()) {
			// Sicherstellen, dass das dazugehoerige Gesuch ueberhaupt noch editiert werden darf fuer meine Rolle
			resourceHelper.assertGesuchStatusForBenutzerRole(betreuung.get().extractGesuch());
			betreuungService.removeBetreuung(converter.toEntityId(betreuungJAXPId));
			return Response.ok().build();
		}
		throw new EbeguEntityNotFoundException("removeBetreuung", ErrorCodeEnum.ERROR_ENTITY_NOT_FOUND, "BetreuungID "
			+ "invalid: " + betreuungJAXPId.getId());
	}

	@ApiOperation(value = "Sucht alle verfügten Betreuungen aus allen Gesuchsperioden, welche zum übergebenen Dossier" +
		"vorhanden sind. Es werden nur diejenigen Betreuungen zurückgegeben, für welche der eingeloggte Benutzer " +
		"berechtigt ist.", responseContainer = "Collection", response = JaxBetreuung.class)
	@Nullable
	@GET
	@Path("/alleBetreuungen/{dossierId}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response findAllBetreuungenWithVerfuegungFromFall(
		@Nonnull @NotNull @PathParam("dossierId") JaxId jaxDossierId,
		@Context UriInfo uriInfo,
		@Context HttpServletResponse response) {

		Optional<Dossier> dossierOptional = dossierService.findDossier(converter.toEntityId(jaxDossierId));
		if (!dossierOptional.isPresent()) {
			return null;
		}
		Dossier dossier = dossierOptional.get();

		Collection<Betreuung> betreuungCollection =
			betreuungService.findAllBetreuungenWithVerfuegungForDossier(dossier);
		Collection<JaxBetreuung> jaxBetreuungList = converter.betreuungListToJax(betreuungCollection);

		return Response.ok(jaxBetreuungList).build();
	}

	@ApiOperation(value = "Erstelle eine Schulamt Anmeldung vom GS-Dashboard in der Datenbank", response =
		JaxBetreuung.class)
	@Nonnull
	@PUT
	@Path("/anmeldung/create/")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response createAnmeldung(
		@Nonnull @NotNull @Valid JaxAnmeldungDTO jaxAnmeldungDTO,
		@Context UriInfo uriInfo,
		@Context HttpServletResponse response) {

		Optional<KindContainer> kind = kindService.findKind(jaxAnmeldungDTO.getKindContainerId());
		if (kind.isPresent()) {
			if (BetreuungUtil.hasDuplicateAnmeldungTagesschule(jaxAnmeldungDTO.getBetreuung(),
				kind.get().getAnmeldungenTagesschule())) {
				throw new EbeguRuntimeException("createAnmeldung", ErrorCodeEnum.ERROR_DUPLICATE_BETREUUNG);
			}
			if (BetreuungUtil.hasDuplicateAnmeldungFerieninsel(jaxAnmeldungDTO.getBetreuung(),
				kind.get().getAnmeldungenFerieninsel())) {
				throw new EbeguRuntimeException("createAnmeldung", ErrorCodeEnum.ERROR_DUPLICATE_BETREUUNG);
			}

			if (jaxAnmeldungDTO.getAdditionalKindQuestions() && !kind.get().getKindJA().getFamilienErgaenzendeBetreuung()) {
				kind.get().getKindJA().setFamilienErgaenzendeBetreuung(true);
				kind.get().getKindJA().setEinschulungTyp(jaxAnmeldungDTO.getEinschulungTyp());
				kind.get().getKindJA().setSprichtAmtssprache(jaxAnmeldungDTO.getSprichtAmtssprache());
				kindService.saveKind(kind.get());
			}

			Betreuung convertedBetreuung = converter.betreuungToStoreableEntity(jaxAnmeldungDTO.getBetreuung());
			resourceHelper.assertGesuchStatusForBenutzerRole(kind.get().getGesuch(), convertedBetreuung);
			convertedBetreuung.setKind(kind.get());
			this.betreuungService.saveBetreuung(convertedBetreuung, false);

			return Response.ok().build();
		}
		throw new EbeguEntityNotFoundException("createAnmeldung", ErrorCodeEnum.ERROR_ENTITY_NOT_FOUND,
			KIND_CONTAINER_ID_INVALID + jaxAnmeldungDTO
				.getKindContainerId());
	}

	@ApiOperation(value = "Speichert die Abweichungen einer Betreuung in der Datenbank", response = Collection.class)
	@PUT
	@Path("/betreuung/abweichungen/{betreuungId}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Collection<JaxBetreuungspensumAbweichung> saveBetreuungspensumAbweichungen(
		@Nonnull @NotNull @PathParam("betreuungId") JaxId betreuungId,
		@Nonnull @NotNull @Valid JaxBetreuung betreuungJax,
		@Context UriInfo uriInfo,
		@Context HttpServletResponse response) {

		Objects.requireNonNull(betreuungJax);
		Objects.requireNonNull(betreuungId);
		String id = converter.toEntityId(betreuungId);
		Optional<Betreuung> betreuungOptional = betreuungService.findBetreuung(id);

		if (!betreuungOptional.isPresent()) {
			return null;
		}

		Betreuung betreuung = betreuungOptional.get();
		Set<BetreuungspensumAbweichung> toStore = converter.betreuungspensumAbweichungenToEntity(betreuungJax.getBetreuungspensumAbweichungen(),
			betreuung.getBetreuungspensumAbweichungen());

		converter.setBetreuungInbetreuungsAbweichungen(toStore, betreuung);
		betreuung.setBetreuungspensumAbweichungen(toStore);
		betreuungService.saveBetreuung(betreuung, false);
		return converter.betreuungspensumAbweichungenToJax(betreuung);
	}

	@ApiOperation(value = "Gibt für jeden Monat in einer Gesuchsperiode eine BetreuungspensumAbweichung zurück.",
		response =
		Collection.class)
	@GET
	@Path("/betreuung/abweichungen/{betreuungId}/")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Collection<JaxBetreuungspensumAbweichung> findBetreuungspensumAbweichungen(
		@Nonnull @NotNull @PathParam("betreuungId") JaxId betreuungId,
		@Context UriInfo uriInfo,
		@Context HttpServletResponse response) {

		Objects.requireNonNull(betreuungId);
		Optional<Betreuung> betreuungOptional = betreuungService.findBetreuung(betreuungId.getId());

		if (!betreuungOptional.isPresent()) {
			return null;
		}

		Betreuung betreuung = betreuungOptional.get();
		return converter.betreuungspensumAbweichungenToJax(betreuung);
	}
}
