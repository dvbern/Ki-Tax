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
import java.net.InetAddress;
import java.net.URI;
import java.net.UnknownHostException;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.security.DenyAll;
import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.CookieParam;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import ch.dvbern.ebegu.api.AuthConstants;
import ch.dvbern.ebegu.api.converter.JaxBConverter;
import ch.dvbern.ebegu.api.dtos.JaxApplicationProperties;
import ch.dvbern.ebegu.api.dtos.JaxPublicAppConfig;
import ch.dvbern.ebegu.authentication.PrincipalBean;
import ch.dvbern.ebegu.config.EbeguConfiguration;
import ch.dvbern.ebegu.entities.ApplicationProperty;
import ch.dvbern.ebegu.entities.Mandant;
import ch.dvbern.ebegu.enums.ApplicationPropertyKey;
import ch.dvbern.ebegu.enums.ErrorCodeEnum;
import ch.dvbern.ebegu.errors.EbeguEntityNotFoundException;
import ch.dvbern.ebegu.errors.EbeguRuntimeException;
import ch.dvbern.ebegu.services.ApplicationPropertyService;
import ch.dvbern.ebegu.services.MandantService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang3.StringUtils;
import org.apache.tika.mime.MimeTypeException;
import org.apache.tika.mime.MimeTypes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static ch.dvbern.ebegu.enums.UserRoleName.ADMIN_BG;
import static ch.dvbern.ebegu.enums.UserRoleName.ADMIN_GEMEINDE;
import static ch.dvbern.ebegu.enums.UserRoleName.SUPER_ADMIN;

/**
 * Resource fuer ApplicationProperties
 */
@Path("application-properties")
@Stateless
@Api(description = "Resource zum Lesen der Applikationsproperties")
@DenyAll // Absichtlich keine Rolle zugelassen, erzwingt, dass es für neue Methoden definiert werden muss
public class ApplicationPropertyResource {

	@Inject
	private ApplicationPropertyService applicationPropertyService;

	@Inject
	private JaxBConverter converter;

	@Inject
	private EbeguConfiguration ebeguConfiguration;

	@Inject
	private PrincipalBean principalBean;

	@Inject
	private MandantService mandantService;

	private static final Logger LOG = LoggerFactory.getLogger(ApplicationPropertyResource.class.getSimpleName());

	@Nonnull
	private String readWhitelistAsString(@Nonnull Mandant mandant) {
		final Collection<String> whitelist = this.applicationPropertyService.readMimeTypeWhitelist(mandant);
		MimeTypes allTypes = MimeTypes.getDefaultMimeTypes();
		final List<String> extensions = whitelist.stream().map(mimetype -> {
			try {
				return allTypes.forName(mimetype).getExtension();
			} catch (MimeTypeException e) {
				LOG.error("Could not find extension for mime type {}", mimetype);
				return "";
			}
		}).filter(Objects::nonNull).collect(Collectors.toList());
		return StringUtils.join(extensions, ",");
	}

	@Nonnull
	private JaxApplicationProperties getSentryEnvName(Mandant mandant) {
		Optional<ApplicationProperty> propertyFromDB = this.applicationPropertyService
			.readApplicationProperty(ApplicationPropertyKey.SENTRY_ENV, mandant);

		ApplicationProperty prop = propertyFromDB.orElseGet(() -> {
			String sentryEnv = ebeguConfiguration.getSentryEnv();
			return new ApplicationProperty(ApplicationPropertyKey.SENTRY_ENV, sentryEnv);
		});
		return converter.applicationPropertyToJAX(prop);
	}

	@SuppressWarnings("NonBooleanMethodNameMayNotStartWithQuestion")
	@ApiOperation(value = "Returns background Color for the current System", response = String.class)
	@GET
	@Consumes(MediaType.WILDCARD)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/public/background")
	@PermitAll
	public JaxApplicationProperties getBackgroundColor(@CookieParam(AuthConstants.COOKIE_MANDANT) Cookie mandantCookie) {
		// getBackgroundColor muss auch erlaubt sein, wenn kein Mandant gesetzt ist. Wir brauchen dies auf der Verteiler-
		// seite der Mandanten, um herauszufinden, ob die Mandantenfähigkeit überhaupt aktiv ist
		Mandant mandant;
		if (mandantCookie == null) {
			mandant = mandantService.getMandantBern();
		} else {
			mandant = mandantService.findMandantByCookie(mandantCookie);
		}

		ApplicationProperty prop = getBackgroundColorProperty(mandant);
		return converter.applicationPropertyToJAX(prop);
	}

	@Nonnull
	private ApplicationProperty getBackgroundColorProperty(Mandant mandant) {
		Optional<ApplicationProperty> propertyFromDB =
			this.applicationPropertyService.readApplicationProperty(ApplicationPropertyKey.BACKGROUND_COLOR,
					mandant);
		ApplicationProperty prop =
			propertyFromDB.orElse(new ApplicationProperty(ApplicationPropertyKey.BACKGROUND_COLOR, "#FFFFFF"));
		return prop;
	}

	@ApiOperation(value = "Returns all application properties",
		responseContainer = "List",
		response = JaxApplicationProperties.class)
	@Nonnull
	@GET
	@Consumes(MediaType.WILDCARD)
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed(SUPER_ADMIN)
	public List<JaxApplicationProperties> getAllApplicationProperties(@CookieParam(AuthConstants.COOKIE_MANDANT) Cookie mandantCookie) {
		var mandant = mandantService.findMandantByCookie(mandantCookie);

		return applicationPropertyService.getAllApplicationProperties(mandant).stream()
			.sorted(Comparator.comparing(o -> o.getName().name()))
			.map(ap -> converter.applicationPropertyToJAX(ap))
			.collect(Collectors.toList());
	}

	@ApiOperation(value = "Create a new ApplicationProperty with the given key and value",
		response = JaxApplicationProperties.class, consumes = MediaType.TEXT_PLAIN)
	@Nullable
	@POST
	@Path("/{key}")
	@Consumes(MediaType.TEXT_PLAIN)
	@RolesAllowed(SUPER_ADMIN)
	public Response create(
		@Nonnull @NotNull @PathParam("key") String key,
		@Nonnull @NotNull String value,
		@Context UriInfo uriInfo,
		@Context HttpServletResponse response,
		@CookieParam(AuthConstants.COOKIE_MANDANT) Cookie mandantCookie) {

		var mandant = mandantService.findMandantByCookie(mandantCookie);

		ApplicationProperty modifiedProperty =
			this.applicationPropertyService.saveOrUpdateApplicationProperty(Enum.valueOf(
				ApplicationPropertyKey.class,
				key), value, mandant);

		URI uri = uriInfo.getBaseUriBuilder()
			.path(ApplicationPropertyResource.class)
			.path("/" + modifiedProperty.getName())
			.build();

		return Response.created(uri).entity(converter.applicationPropertyToJAX(modifiedProperty)).build();
	}

	@ApiOperation(value = "Aktualisiert ein bestehendes ApplicationProperty",
		response = JaxApplicationProperties.class, consumes = MediaType.TEXT_PLAIN)
	@Nullable
	@PUT
	@Path("/{key}")
	@Consumes(MediaType.TEXT_PLAIN)
	@RolesAllowed(SUPER_ADMIN)
	public JaxApplicationProperties update(
		@Nonnull @PathParam("key") String key,
		@Nonnull @NotNull String value,
		@Context UriInfo uriInfo,
		@Context HttpServletResponse response,
		@CookieParam(AuthConstants.COOKIE_MANDANT) Cookie mandantCookie
		) {
		var mandant = mandantService.findMandantByCookie(mandantCookie);

		ApplicationProperty modifiedProperty =
			this.applicationPropertyService.saveOrUpdateApplicationProperty(Enum.valueOf(
				ApplicationPropertyKey.class,
				key), value, mandant);

		return converter.applicationPropertyToJAX(modifiedProperty);
	}

	@SuppressWarnings("NonBooleanMethodNameMayNotStartWithQuestion")
	@ApiOperation("Removes an application property")
	@Nullable
	@DELETE
	@Path("/{key}")
	@Consumes(MediaType.WILDCARD)
	@RolesAllowed({ ADMIN_BG, ADMIN_GEMEINDE, SUPER_ADMIN })
	public Response remove(@Nonnull @PathParam("key") String keyParam, @Context HttpServletResponse response) {
		applicationPropertyService.removeApplicationProperty(Enum.valueOf(ApplicationPropertyKey.class, keyParam),
				principalBean.getMandant());
		return Response.ok().build();
	}

	@RolesAllowed(SUPER_ADMIN)
	@ApiOperation(value = "Gibt den Wert des Properties zurück", response = Boolean.class)
	@GET
	@Consumes(MediaType.WILDCARD)
	@Produces(MediaType.WILDCARD)
	@Path("/property/{key}")
	public Response getProperty(@Nonnull @PathParam("key") String keyParam, @Context HttpServletResponse response) {
		if (keyParam.startsWith("ebegu")) {
			return Response.ok(System.getProperty(keyParam)).build();
		}
		return Response.noContent().build();
	}

	@ApiOperation(value = "Single request to load public config", response = Boolean.class)
	@GET
	@Consumes(MediaType.WILDCARD)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/public/all")
	@PermitAll
	@SuppressWarnings("PMD.NcssMethodCount")
	public Response getPublicProperties(@Context HttpServletResponse response, @CookieParam(AuthConstants.COOKIE_MANDANT)
			Cookie mandantCookie) {

		// getPublicProperties muss auch erlaubt sein, wenn kein Mandant gesetzt ist. Wir brauchen dies auf der Verteiler-
		// seite der Mandanten, um herauszufinden, ob die Mandantenfähigkeit überhaupt aktiv ist
		Mandant mandant;
		if (mandantCookie == null) {
			mandant = mandantService.getMandantBern();
		} else {
			mandant = mandantService.findMandantByCookie(mandantCookie);
		}

		boolean devmode = ebeguConfiguration.getIsDevmode();
		final String whitelist = readWhitelistAsString(mandant);
		boolean dummyMode = ebeguConfiguration.isDummyLoginEnabled(mandant);
		String sentryEnvName = getSentryEnvName(mandant).getValue();
		String background = getBackgroundColorProperty(mandant).getValue();
		boolean zahlungentestmode = ebeguConfiguration.getIsZahlungenTestMode();
		boolean personenSucheDisabled = ebeguConfiguration.isPersonenSucheDisabled();
		String kitaxHost = ebeguConfiguration.getKitaxHost();
		String kitaxendpoint = ebeguConfiguration.getKitaxEndpoint();
		boolean multimandantEnabled = ebeguConfiguration.getMultimandantEnabled();
		boolean isEbeguKibonAnfrageTestGuiEnabled = ebeguConfiguration.getEbeguKibonAnfrageTestGuiEnabled();
		boolean testfaelleEnabled = ebeguConfiguration.isTestfaelleEnabled();

		ApplicationProperty einreichefristOeffentlich =
				getApplicationProperty(
						mandant,
						ApplicationPropertyKey.NOTVERORDNUNG_DEFAULT_EINREICHEFRIST_OEFFENTLICH);
		ApplicationProperty einreichefristPrivat =
				getApplicationProperty(
						mandant,
						ApplicationPropertyKey.NOTVERORDNUNG_DEFAULT_EINREICHEFRIST_PRIVAT);
		ApplicationProperty ferienbetreuungAktiv =
				getApplicationProperty(mandant, ApplicationPropertyKey.FERIENBETREUUNG_AKTIV);
		ApplicationProperty lastenausgleichAktiv =
				getApplicationProperty(mandant, ApplicationPropertyKey.LASTENAUSGLEICH_AKTIV);
		ApplicationProperty lastenausgleichTagesschulenAktiv =
				getApplicationProperty(mandant, ApplicationPropertyKey.LASTENAUSGLEICH_TAGESSCHULEN_AKTIV);
		ApplicationProperty gemeindeKennzahlenAktiv =
				getApplicationProperty(mandant, ApplicationPropertyKey.GEMEINDE_KENNZAHLEN_AKTIV);
		ApplicationProperty lastenausgleichTagesschulenAnteilZweitpruefungDe =
				getApplicationProperty(
						mandant,
						ApplicationPropertyKey.LASTENAUSGLEICH_TAGESSCHULEN_ANTEIL_ZWEITPRUEFUNG_DE);
		ApplicationProperty lastenausgleichTagesschulenAnteilZweitpruefungFr =
				getApplicationProperty(
						mandant,
						ApplicationPropertyKey.LASTENAUSGLEICH_TAGESSCHULEN_ANTEIL_ZWEITPRUEFUNG_FR);
		ApplicationProperty lastenausgleichTagesschulenAutoZweitpruefungDe =
				getApplicationProperty(
						mandant,
						ApplicationPropertyKey.LASTENAUSGLEICH_TAGESSCHULEN_AUTO_ZWEITPRUEFUNG_DE);
		ApplicationProperty lastenausgleichTagesschulenAutoZweitpruefungFr =
				getApplicationProperty(
						mandant,
						ApplicationPropertyKey.LASTENAUSGLEICH_TAGESSCHULEN_AUTO_ZWEITPRUEFUNG_FR);
		ApplicationProperty primaryColor =
				getApplicationProperty(mandant, ApplicationPropertyKey.PRIMARY_COLOR);
		ApplicationProperty primaryColorDark =
				getApplicationProperty(mandant, ApplicationPropertyKey.PRIMARY_COLOR_DARK);
		ApplicationProperty primaryColorLight =
				getApplicationProperty(mandant, ApplicationPropertyKey.PRIMARY_COLOR_LIGHT);
		ApplicationProperty logoFileName =
				getApplicationProperty(mandant, ApplicationPropertyKey.LOGO_FILE_NAME);
		ApplicationProperty logoFileNameWhite =
				getApplicationProperty(mandant, ApplicationPropertyKey.LOGO_WHITE_FILE_NAME);
		ApplicationProperty infomaZahlungen =
				getApplicationProperty(mandant, ApplicationPropertyKey.INFOMA_ZAHLUNGEN);
		ApplicationProperty auszahlungAnEltern =
				getApplicationProperty(mandant, ApplicationPropertyKey.AUSZAHLUNGEN_AN_ELTERN);
		ApplicationProperty frenchEnabled =
				getApplicationProperty(mandant, ApplicationPropertyKey.FRENCH_ENABLED);
		ApplicationProperty geresEnabledForMandant =
				getApplicationProperty(mandant, ApplicationPropertyKey.GERES_ENABLED_FOR_MANDANT);
		ApplicationProperty steuerschnittstelleAktivAb =
				getApplicationProperty(mandant, ApplicationPropertyKey.SCHNITTSTELLE_STEUERSYSTEME_AKTIV_AB);
		ApplicationProperty zusatzinformationenInstitution =
				getApplicationProperty(mandant, ApplicationPropertyKey.ZUSATZINFORMATIONEN_INSTITUTION);
		ApplicationProperty activatedDemoFeatures =
				getApplicationProperty(mandant, ApplicationPropertyKey.ACTIVATED_DEMO_FEATURES);
		ApplicationProperty checkboxAuszahlungInZukunft =
				getApplicationProperty(mandant, ApplicationPropertyKey.CHECKBOX_AUSZAHLEN_IN_ZUKUNFT);
		ApplicationProperty institutionenDurchGemeindenEinladen =
				getApplicationProperty(
						mandant,
						ApplicationPropertyKey.INSTITUTIONEN_DURCH_GEMEINDEN_EINLADEN);

		ApplicationProperty erlaubenInstitutionenZuWaehlen =
				getApplicationProperty(mandant, ApplicationPropertyKey.ERLAUBEN_INSTITUTIONEN_ZU_WAEHLEN);
		ApplicationProperty angebotTSEnabled =
				getApplicationProperty(mandant, ApplicationPropertyKey.ANGEBOT_TS_ENABLED);
		ApplicationProperty angebotFIEnabled =
				getApplicationProperty(mandant, ApplicationPropertyKey.ANGEBOT_FI_ENABLED);
		ApplicationProperty angebotMittagstischEnabled =
				getApplicationProperty(mandant, ApplicationPropertyKey.ANGEBOT_MITTAGSTISCH_ENABLED);
		ApplicationProperty angebotTFOEnabled =
				getApplicationProperty(mandant, ApplicationPropertyKey.ANGEBOT_TFO_ENABLED);
		ApplicationProperty abweichungenEnabled =
				getApplicationProperty(mandant, ApplicationPropertyKey.ABWEICHUNGEN_ENABLED);
		ApplicationProperty gemeindeVereinfachteKonfigAktiv =
				getApplicationProperty(mandant, ApplicationPropertyKey.GEMEINDE_VEREINFACHTE_KONFIG_AKTIV);

		String nodeName = "";
		BigDecimal lastenausgleichTagesschulenAnteilZweitpruefungDeConverted;
		BigDecimal lastenausgleichTagesschulenAnteilZweitpruefungFrConverted;
		BigDecimal lastenausgleichTagesschulenAutoZweitpruefungDeConverted;
		BigDecimal lastenausgleichTagesschulenAutoZweitpruefungFrConverted;
		try {
			nodeName = InetAddress.getLocalHost().getHostName();
			lastenausgleichTagesschulenAnteilZweitpruefungDeConverted = new BigDecimal(lastenausgleichTagesschulenAnteilZweitpruefungDe.getValue());
			lastenausgleichTagesschulenAnteilZweitpruefungFrConverted = new BigDecimal(lastenausgleichTagesschulenAnteilZweitpruefungFr.getValue());
			lastenausgleichTagesschulenAutoZweitpruefungDeConverted = new BigDecimal(lastenausgleichTagesschulenAutoZweitpruefungDe.getValue());
			lastenausgleichTagesschulenAutoZweitpruefungFrConverted = new BigDecimal(lastenausgleichTagesschulenAutoZweitpruefungFr.getValue());
		} catch (UnknownHostException e) {
			throw new EbeguRuntimeException("getHostName", "Hostname konnte nicht ermittelt werden", e);
		} catch (NumberFormatException e) {
			throw new EbeguRuntimeException("new BigDecimal()", "Fehler beim Parsen einer Einstellung", e);
		}
		JaxPublicAppConfig pubAppConf = JaxPublicAppConfig.builder()
				.currentNode(nodeName)
				.devmode(devmode)
				.whitelist(whitelist)
				.dummyMode(dummyMode)
				.sentryEnvName(sentryEnvName)
				.backgroundColor(background)
				.zahlungentestmode(zahlungentestmode)
				.personenSucheDisabled(personenSucheDisabled)
				.kitaxHost(kitaxHost)
				.kitaxEndpoint(kitaxendpoint)
				.notverordnungDefaultEinreichefristOeffentlich(einreichefristOeffentlich.getValue())
				.notverordnungDefaultEinreichefristPrivat(einreichefristPrivat.getValue())
				.lastenausgleichAktiv(stringToBool(lastenausgleichAktiv.getValue()))
				.ferienbetreuungAktiv(stringToBool(ferienbetreuungAktiv.getValue()))
				.lastenausgleichTagesschulenAktiv(stringToBool(lastenausgleichTagesschulenAktiv.getValue()))
				.gemeindeKennzahlenAktiv(stringToBool(gemeindeKennzahlenAktiv.getValue()))
				.lastenausgleichTagesschulenAnteilZweitpruefungDe(
						lastenausgleichTagesschulenAnteilZweitpruefungDeConverted)
				.lastenausgleichTagesschulenAnteilZweitpruefungFr(
						lastenausgleichTagesschulenAnteilZweitpruefungFrConverted)
				.lastenausgleichTagesschulenAutoZweitpruefungDe(lastenausgleichTagesschulenAutoZweitpruefungDeConverted)
				.lastenausgleichTagesschulenAutoZweitpruefungFr(lastenausgleichTagesschulenAutoZweitpruefungFrConverted)
				.primaryColor(primaryColor.getValue())
				.primaryColorDark(primaryColorDark.getValue())
				.primaryColorLight(primaryColorLight.getValue())
				.logoFileName(logoFileName.getValue())
				.logoFileNameWhite(logoFileNameWhite.getValue())
				.multimandantAktiviert(multimandantEnabled)
				.infomaZahlungen(stringToBool(infomaZahlungen.getValue()))
				.frenchEnabled(stringToBool(frenchEnabled.getValue()))
				.geresEnabledForMandant(stringToBool(geresEnabledForMandant.getValue()))
				.ebeguKibonAnfrageTestGuiEnabled(isEbeguKibonAnfrageTestGuiEnabled)
				.steuerschnittstelleAktivAb(steuerschnittstelleAktivAb.getValue())
				.zusatzinformationenInstitution(stringToBool(zusatzinformationenInstitution.getValue()))
				.activatedDemoFeatures(activatedDemoFeatures.getValue())
				.checkboxAuszahlungInZukunft(stringToBool(checkboxAuszahlungInZukunft.getValue()))
				.institutionenDurchGemeindenEinladen(stringToBool(institutionenDurchGemeindenEinladen.getValue()))
				.erlaubenInstitutionenZuWaehlen(stringToBool(erlaubenInstitutionenZuWaehlen.getValue()))
				.angebotTSActivated(stringToBool(angebotTSEnabled.getValue()))
				.angebotFIActivated(stringToBool(angebotFIEnabled.getValue()))
				.angebotMittagstischEnabled(stringToBool(angebotMittagstischEnabled.getValue()))
				.angebotTFOActivated(stringToBool(angebotTFOEnabled.getValue()))
				.auszahlungAnEltern(stringToBool(auszahlungAnEltern.getValue()))
				.abweichungenEnabled(stringToBool(abweichungenEnabled.getValue()))
				.gemeindeVereinfachteKonfigAktiv(stringToBool(gemeindeVereinfachteKonfigAktiv.getValue()))
				.testfaelleEnabled(testfaelleEnabled)
				.build();

		return Response.ok(pubAppConf).build();
	}

	private ApplicationProperty getApplicationProperty(
			Mandant mandant,
			ApplicationPropertyKey applicationPropertyKey) {
		return this.applicationPropertyService.readApplicationProperty(
						applicationPropertyKey,
						mandant)
				.orElseThrow(() -> new EbeguEntityNotFoundException(
						"getPublicProperties",
						ErrorCodeEnum.ERROR_ENTITY_NOT_FOUND));
	}

	private boolean stringToBool(@Nonnull String str) {
		return str.equals("true");
	}
}
