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
import ch.dvbern.ebegu.api.dtos.JaxApplicationProperties;
import ch.dvbern.ebegu.api.dtos.JaxPublicAppConfig;
import ch.dvbern.ebegu.config.EbeguConfiguration;
import ch.dvbern.ebegu.entities.ApplicationProperty;
import ch.dvbern.ebegu.enums.ApplicationPropertyKey;
import ch.dvbern.ebegu.enums.ErrorCodeEnum;
import ch.dvbern.ebegu.errors.EbeguEntityNotFoundException;
import ch.dvbern.ebegu.errors.EbeguRuntimeException;
import ch.dvbern.ebegu.services.ApplicationPropertyService;
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

	private static final Logger LOG = LoggerFactory.getLogger(ApplicationPropertyResource.class.getSimpleName());

	@Nonnull
	private String readWhitelistAsString() {
		final Collection<String> whitelist = this.applicationPropertyService.readMimeTypeWhitelist();
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
	private JaxApplicationProperties getSentryEnvName() {
		Optional<ApplicationProperty> propertyFromDB = this.applicationPropertyService
			.readApplicationProperty(ApplicationPropertyKey.SENTRY_ENV);

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
	public JaxApplicationProperties getBackgroundColor() {
		Optional<ApplicationProperty> propertyFromDB =
			this.applicationPropertyService.readApplicationProperty(ApplicationPropertyKey.BACKGROUND_COLOR);
		ApplicationProperty prop =
			propertyFromDB.orElse(new ApplicationProperty(ApplicationPropertyKey.BACKGROUND_COLOR, "#FFFFFF"));
		return converter.applicationPropertyToJAX(prop);
	}

	@ApiOperation(value = "Returns all application properties",
		responseContainer = "List",
		response = JaxApplicationProperties.class)
	@Nonnull
	@GET
	@Consumes(MediaType.WILDCARD)
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed(SUPER_ADMIN)
	public List<JaxApplicationProperties> getAllApplicationProperties() {
		return applicationPropertyService.getAllApplicationProperties().stream()
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
		@Context HttpServletResponse response) {

		ApplicationProperty modifiedProperty =
			this.applicationPropertyService.saveOrUpdateApplicationProperty(Enum.valueOf(
				ApplicationPropertyKey.class,
				key), value);

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
		@Context HttpServletResponse response) {

		ApplicationProperty modifiedProperty =
			this.applicationPropertyService.saveOrUpdateApplicationProperty(Enum.valueOf(
				ApplicationPropertyKey.class,
				key), value);

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
		applicationPropertyService.removeApplicationProperty(Enum.valueOf(ApplicationPropertyKey.class, keyParam));
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
	public Response getPublicProperties(@Context HttpServletResponse response) {

		boolean devmode = ebeguConfiguration.getIsDevmode();
		final String whitelist = readWhitelistAsString();
		boolean dummyMode = ebeguConfiguration.isDummyLoginEnabled();
		String sentryEnvName = getSentryEnvName().getValue();
		String background = getBackgroundColor().getValue();
		boolean zahlungentestmode = ebeguConfiguration.getIsZahlungenTestMode();
		boolean personenSucheDisabled = ebeguConfiguration.isPersonenSucheDisabled();
		String kitaxHost = ebeguConfiguration.getKitaxHost();
		String kitaxendpoint = ebeguConfiguration.getKitaxEndpoint();

		EbeguEntityNotFoundException notFound = new EbeguEntityNotFoundException("getPublicProperties", ErrorCodeEnum.ERROR_ENTITY_NOT_FOUND);

		ApplicationProperty einreichefristOeffentlich  =
			this.applicationPropertyService.readApplicationProperty(ApplicationPropertyKey.NOTVERORDNUNG_DEFAULT_EINREICHEFRIST_OEFFENTLICH)
			.orElseThrow(() -> notFound);
		ApplicationProperty einreichefristPrivat  =
			this.applicationPropertyService.readApplicationProperty(ApplicationPropertyKey.NOTVERORDNUNG_DEFAULT_EINREICHEFRIST_PRIVAT)
			.orElseThrow(() -> notFound);
		ApplicationProperty ferienbetreuungAktiv  =
			this.applicationPropertyService.readApplicationProperty(ApplicationPropertyKey.FERIENBETREUUNG_AKTIV)
				.orElseThrow(() -> notFound);
		ApplicationProperty lastenausgleichTagesschulenAktiv  =
			this.applicationPropertyService.readApplicationProperty(ApplicationPropertyKey.LASTENAUSGLEICH_TAGESSCHULEN_AKTIV)
				.orElseThrow(() -> notFound);
		ApplicationProperty gemeindeKennzahlenAktiv  =
			this.applicationPropertyService.readApplicationProperty(ApplicationPropertyKey.GEMEINDE_KENNZAHLEN_AKTIV)
				.orElseThrow(() -> notFound);
		ApplicationProperty lastenausgleichTagesschulenAnteilZweitpruefungDe  =
			this.applicationPropertyService.readApplicationProperty(ApplicationPropertyKey.LASTENAUSGLEICH_TAGESSCHULEN_ANTEIL_ZWEITPRUEFUNG_DE)
				.orElseThrow(() -> notFound);
		ApplicationProperty lastenausgleichTagesschulenAnteilZweitpruefungFr  =
			this.applicationPropertyService.readApplicationProperty(ApplicationPropertyKey.LASTENAUSGLEICH_TAGESSCHULEN_ANTEIL_ZWEITPRUEFUNG_FR)
				.orElseThrow(() -> notFound);
		ApplicationProperty lastenausgleichTagesschulenAutoZweitpruefungDe  =
			this.applicationPropertyService.readApplicationProperty(ApplicationPropertyKey.LASTENAUSGLEICH_TAGESSCHULEN_AUTO_ZWEITPRUEFUNG_DE)
				.orElseThrow(() -> notFound);
		ApplicationProperty lastenausgleichTagesschulenAutoZweitpruefungFr  =
			this.applicationPropertyService.readApplicationProperty(ApplicationPropertyKey.LASTENAUSGLEICH_TAGESSCHULEN_AUTO_ZWEITPRUEFUNG_FR)
				.orElseThrow(() -> notFound);
		ApplicationProperty multimandantAktiv  =
			this.applicationPropertyService.readApplicationProperty(ApplicationPropertyKey.MULTIMANDANT_AKTIV)
				.orElseThrow(() -> notFound);

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
		JaxPublicAppConfig pubAppConf = new JaxPublicAppConfig(
			nodeName,
			devmode,
			whitelist,
			dummyMode,
			sentryEnvName,
			background,
			zahlungentestmode,
			personenSucheDisabled,
			kitaxHost,
			kitaxendpoint,
			einreichefristOeffentlich.getValue(),
			einreichefristPrivat.getValue(),
			ferienbetreuungAktiv.getValue().equals("true"),
			lastenausgleichTagesschulenAktiv.getValue().equals("true"),
			gemeindeKennzahlenAktiv.getValue().equals("true"),
			lastenausgleichTagesschulenAnteilZweitpruefungDeConverted,
			lastenausgleichTagesschulenAnteilZweitpruefungFrConverted,
			lastenausgleichTagesschulenAutoZweitpruefungDeConverted,
			lastenausgleichTagesschulenAutoZweitpruefungFrConverted,
			multimandantAktiv.getValue().equals("true")
		);
		return Response.ok(pubAppConf).build();
	}
}
