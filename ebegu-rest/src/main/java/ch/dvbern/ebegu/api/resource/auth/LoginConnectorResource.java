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

package ch.dvbern.ebegu.api.resource.auth;

import java.util.Optional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.ejb.EJBAccessException;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;

import ch.dvbern.ebegu.api.connector.ILoginConnectorResource;
import ch.dvbern.ebegu.api.dtos.JaxBenutzerResponseWrapper;
import ch.dvbern.ebegu.api.dtos.JaxExternalAuthAccessElement;
import ch.dvbern.ebegu.api.dtos.JaxExternalAuthorisierterBenutzer;
import ch.dvbern.ebegu.api.dtos.JaxExternalBenutzer;
import ch.dvbern.ebegu.api.dtos.JaxMandant;
import ch.dvbern.ebegu.api.resource.MandantResource;
import ch.dvbern.ebegu.api.util.version.VersionInfoBean;
import ch.dvbern.ebegu.authentication.AuthAccessElement;
import ch.dvbern.ebegu.config.EbeguConfiguration;
import ch.dvbern.ebegu.einladung.Einladung;
import ch.dvbern.ebegu.entities.AuthorisierterBenutzer;
import ch.dvbern.ebegu.entities.Benutzer;
import ch.dvbern.ebegu.entities.Mandant;
import ch.dvbern.ebegu.enums.BenutzerStatus;
import ch.dvbern.ebegu.enums.ErrorCodeEnum;
import ch.dvbern.ebegu.enums.UserRole;
import ch.dvbern.ebegu.errors.EbeguEntityNotFoundException;
import ch.dvbern.ebegu.i18n.LocaleThreadLocal;
import ch.dvbern.ebegu.services.AuthService;
import ch.dvbern.ebegu.services.BenutzerService;
import ch.dvbern.ebegu.services.MandantService;
import ch.dvbern.ebegu.util.ServerMessageUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static ch.dvbern.ebegu.enums.ErrorCodeEnum.ERROR_EMAIL_MISMATCH;
import static ch.dvbern.ebegu.enums.ErrorCodeEnum.ERROR_ENTITY_NOT_FOUND;
import static ch.dvbern.ebegu.enums.ErrorCodeEnum.ERROR_PENDING_INVITATION;
import static java.util.Objects.requireNonNull;

/**
 * Service provided by KI-TAX to allow an external login module to create users and logins
 */
@SuppressWarnings({ "EjbInterceptorInspection", "EjbClassBasicInspection" })
@Stateless
@Path("/connector")
public class LoginConnectorResource implements ILoginConnectorResource {

	private static final Logger LOG = LoggerFactory.getLogger(LoginConnectorResource.class.getSimpleName());

	private final BenutzerService benutzerService;
	private final AuthService authService;

	private final VersionInfoBean versionInfoBean;

	private final MandantResource mandantResource;
	private final MandantService mandantService;
	private final LocalhostChecker localhostChecker;
	private final EbeguConfiguration configuration;

	@Context
	private HttpServletRequest request;

	@Context
	private UriInfo uriInfo;

	@Inject
	public LoginConnectorResource(
		VersionInfoBean versionInfoBean,
		LocalhostChecker localhostChecker,
		EbeguConfiguration configuration,
		BenutzerService benutzerService,
		AuthService authService,
		MandantResource mandantResource,
		MandantService mandantService
	) {

		this.configuration = configuration;
		this.versionInfoBean = versionInfoBean;
		this.localhostChecker = localhostChecker;
		this.benutzerService = benutzerService;
		this.authService = authService;
		this.mandantResource = mandantResource;
		this.mandantService = mandantService;
	}

	@Override
	public String getHeartBeat() {
		StringBuilder builder = new StringBuilder();
		if (versionInfoBean != null && versionInfoBean.getVersionInfo().isPresent()) {
			builder.append("Version: ");
			builder.append(versionInfoBean.getVersionInfo().get().getVersion());

		} else {
			builder.append("unknown Version");
		}
		final boolean isAccessedLocally = localhostChecker.isAddressLocalhost(request.getRemoteAddr());
		if (!isAccessedLocally) {
			builder
				.append(" WARNING access is not local. Remote Address is: ")
				.append(request.getRemoteAddr());
		}
		return builder.toString();

	}

	@Override
	public JaxBenutzerResponseWrapper isBenutzerGesperrt(@Nonnull String benutzerId) {
		Benutzer benutzer = benutzerService.findBenutzerById(benutzerId).orElseThrow(() -> {
			LOG.error("Benutzer not found for passed id: {}", benutzerId);
			return new EbeguEntityNotFoundException("isBenutzerGesperrt", ErrorCodeEnum.ERROR_ENTITY_NOT_FOUND);
		});
		JaxBenutzerResponseWrapper responseWrapper = new JaxBenutzerResponseWrapper();
		responseWrapper.setStoredBenutzerGesperrt(benutzer.getStatus() == BenutzerStatus.GESPERRT);
		return responseWrapper;
	}

	@Override
	public JaxBenutzerResponseWrapper updateOrStoreBenutzer(@Nonnull JaxExternalBenutzer externalBenutzer) {
		LOG.debug("Requested url {} ", this.uriInfo.getAbsolutePath());
		LOG.debug("Requested forwared for {} ", this.request.getHeader("X-Forwarded-For"));
		checkLocalAccessOnly();

		Benutzer benutzer = new Benutzer();
		toBenutzer(externalBenutzer, benutzer);

		String unusedAttr = StringUtils.join(
			externalBenutzer.getCommonName(),
			externalBenutzer.getTelephoneNumber(),
			externalBenutzer.getMobile(),
			externalBenutzer.getPreferredLang(),
			externalBenutzer.getPostalCode(),
			externalBenutzer.getState(),
			externalBenutzer.getStreet(),
			externalBenutzer.getPostalCode(),
			externalBenutzer.getCountryCode(),
			externalBenutzer.getCountry(),
			externalBenutzer.getCountry(),
			',');

		LOG.info(
			"The following attributes are received from the ExternalLoginModule but not yet stored {}",
			unusedAttr);

		Mandant mandant = this.mandantService.findMandant(externalBenutzer.getMandantId())
			.orElseThrow(() -> {
				LOG.error("Mandant not found for passed id: {}", externalBenutzer.getMandantId());
				return new EbeguEntityNotFoundException("updateOrStoreMandant", ErrorCodeEnum.ERROR_ENTITY_NOT_FOUND);
			});
		benutzer.setMandant(mandant);

		Benutzer storedUser;


		Optional<Benutzer> invitedUserOpt = benutzerService.findUserWithInvitationByEmail(benutzer);
		// wenn der Benutzer eingeladen ist, muss er die Einladung akzeptieren
		if(invitedUserOpt.isPresent()) {
			final Benutzer presentUser = invitedUserOpt.get();
			String url = benutzerService.createInvitationLink(presentUser, Einladung.forRolle(presentUser));
			externalBenutzer.setInvitationLink(url);
			externalBenutzer.setInvitationPending(true);
			String rolleIst = ServerMessageUtil.translateEnumValue(UserRole.GESUCHSTELLER, LocaleThreadLocal.get());
			String rolleSoll = ServerMessageUtil.translateEnumValue(presentUser.getRole(), LocaleThreadLocal.get());
			String msg = ServerMessageUtil.translateEnumValue(ERROR_PENDING_INVITATION, LocaleThreadLocal.get(), rolleIst, rolleSoll);
			return convertBenutzerResponseWrapperToJax(externalBenutzer, msg);
		}

		storedUser = benutzerService.updateOrStoreUserFromIAM(benutzer);

		return convertBenutzerResponseWrapperToJax(convertBenutzerToJax(storedUser), null);
	}

	private void toBenutzer(@Nonnull JaxExternalBenutzer jaxExternalBenutzer, @Nonnull Benutzer benutzer) {
		benutzer.setUsername(jaxExternalBenutzer.getUsername());
		benutzer.setExternalUUID(jaxExternalBenutzer.getExternalUUID());
		benutzer.setEmail(jaxExternalBenutzer.getEmail());
		benutzer.setNachname(jaxExternalBenutzer.getNachname());
		benutzer.setVorname(jaxExternalBenutzer.getVorname());
	}

	@Nonnull
	@Override
	public JaxBenutzerResponseWrapper updateBenutzer(
		@Nonnull String benutzerId,
		@Nonnull JaxExternalBenutzer externalBenutzer
	) {
		requireNonNull(benutzerId);
		requireNonNull(externalBenutzer);
		requireNonNull(externalBenutzer.getExternalUUID());
		checkLocalAccessOnly();

		Benutzer existingBenutzer = benutzerService.findBenutzerById(benutzerId).orElse(null);

		//if user not exists return error msg to connector
		if (existingBenutzer == null) {
			return convertBenutzerResponseWrapperToJax(
				externalBenutzer,
				ServerMessageUtil.translateEnumValue(ERROR_ENTITY_NOT_FOUND, LocaleThreadLocal.get())
			);
		}

		String persistedEmail = existingBenutzer.getEmail();
		String externalEmail = externalBenutzer.getEmail();

		if (!persistedEmail.equalsIgnoreCase(externalEmail)) {
			String msg = ServerMessageUtil.translateEnumValue(
				ERROR_EMAIL_MISMATCH,
				LocaleThreadLocal.get(),
				persistedEmail,
				externalEmail
			);

			//return the message to connector and stop process
			return convertBenutzerResponseWrapperToJax(externalBenutzer, msg);
		}

		// Überprüfen, ob die external ID schon besetzt ist
		Optional<Benutzer> existingBenutzerWithExternalUuidOptional =
			benutzerService.findBenutzerByExternalUUID(externalBenutzer.getExternalUUID());
		if (existingBenutzerWithExternalUuidOptional.isPresent()) {
			Benutzer duplicatedBenutzer = existingBenutzerWithExternalUuidOptional.get();
			benutzerService.deleteExternalUUIDInNewTransaction(duplicatedBenutzer.getId());
			String bemerkung =
				"ExternalUUID uebernommen von Benutzer: username=" + duplicatedBenutzer.getUsername()
					+ " externalUUID= " + duplicatedBenutzer.getExternalUUID() + ". Bei diesem wurde die externalUUID gelöscht" ;
			LOG.info(bemerkung);
			existingBenutzer.addBemerkung(bemerkung);
		}

		//external uuid setzen
		toBenutzer(externalBenutzer, existingBenutzer);

		if (existingBenutzer.getStatus() == BenutzerStatus.EINGELADEN) {
			existingBenutzer.setStatus(BenutzerStatus.AKTIV);
		}

		Benutzer updatedBenutzer = benutzerService.updateOrStoreUserFromIAM(existingBenutzer);
		return convertBenutzerResponseWrapperToJax(convertBenutzerToJax(updatedBenutzer), null);
	}

	private JaxBenutzerResponseWrapper convertBenutzerResponseWrapperToJax(
		@NotNull JaxExternalBenutzer benutzer,
		@Nullable String msg) {
		JaxBenutzerResponseWrapper wrapper = new JaxBenutzerResponseWrapper();
		wrapper.setBenutzer(benutzer);
		wrapper.setErrorMessage(msg);

		return wrapper;
	}

	@Nonnull
	private JaxExternalBenutzer convertBenutzerToJax(@Nonnull Benutzer benutzer) {
		JaxExternalBenutzer jaxExternalBenutzer = new JaxExternalBenutzer();
		jaxExternalBenutzer.setUsername(benutzer.getUsername());
		jaxExternalBenutzer.setExternalUUID(benutzer.getExternalUUID());
		jaxExternalBenutzer.setEmail(benutzer.getEmail());
		jaxExternalBenutzer.setNachname(benutzer.getNachname());
		jaxExternalBenutzer.setVorname(benutzer.getVorname());
		jaxExternalBenutzer.setRole(benutzer.getRole().name());
		jaxExternalBenutzer.setMandantId(benutzer.getMandant().getId());
		jaxExternalBenutzer.setGesperrt(benutzer.getStatus() != BenutzerStatus.AKTIV);
		if (benutzer.getInstitution() != null) {
			jaxExternalBenutzer.setInstitutionId(benutzer.getInstitution().getId());
		}
		if (benutzer.getTraegerschaft() != null) {
			jaxExternalBenutzer.setTraegerschaftId(benutzer.getTraegerschaft().getId());
		}
		return jaxExternalBenutzer;
	}

	@Nonnull
	@Override
	public String getMandant() {
		checkLocalAccessOnly();
		final JaxMandant first = mandantResource.getFirst();
		if (first.getId() == null) {
			String message = "error while loading mandant";
			throw new EbeguEntityNotFoundException("getFirst", message, ErrorCodeEnum.ERROR_ENTITY_NOT_FOUND);
		}

		return first.getId();
	}

	@Override
	public JaxExternalAuthAccessElement createLogin(@Nonnull JaxExternalAuthorisierterBenutzer jaxExtAuthUser) {
		requireNonNull(jaxExtAuthUser, "Passed JaxExternalAuthorisierterBenutzer may not be null");

		LOG.debug("ExternalLogin System is creating Authorization for user {}", jaxExtAuthUser.getUsername());
		LOG.debug("Requested url {} ", this.uriInfo.getAbsolutePath());

		checkLocalAccessOnly();

		AuthorisierterBenutzer authUser = convertExternalLogin(jaxExtAuthUser);
		AuthAccessElement loginDataForCookie = this.authService.createLoginFromIAM(authUser);
		return convertToJaxExternalAuthAccessElement(loginDataForCookie);
	}

	@Nonnull
	private JaxExternalAuthAccessElement convertToJaxExternalAuthAccessElement(
		@Nonnull AuthAccessElement loginDataForCookie
	) {
		requireNonNull(loginDataForCookie, "login data to convert may not be null");
		return new JaxExternalAuthAccessElement(
			loginDataForCookie.getAuthId(),
			loginDataForCookie.getAuthToken(),
			loginDataForCookie.getXsrfToken(),
			loginDataForCookie.getNachname(),
			loginDataForCookie.getVorname(),
			loginDataForCookie.getEmail(),
			loginDataForCookie.getRole().name()
		);
	}

	/**
	 * currently we allow requests to this services only from localhost
	 */
	@SuppressWarnings("NonBooleanMethodNameMayNotStartWithQuestion")
	private void checkLocalAccessOnly() {
		if (!this.configuration.isRemoteLoginConnectorAllowed()) {
			boolean isLocallyAccessed = this.localhostChecker.isAddressLocalhost(request.getRemoteAddr());
			if (!isLocallyAccessed) {
				throw new EJBAccessException("This Service may only be called from localhost but was accessed from  "
					+ request.getRemoteAddr());
			}
		}
	}

	@Nonnull
	private AuthorisierterBenutzer convertExternalLogin(JaxExternalAuthorisierterBenutzer jaxExtAuthBen) {
		AuthorisierterBenutzer authUser = new AuthorisierterBenutzer();
		authUser.setUsername(jaxExtAuthBen.getUsername());
		authUser.setAuthToken(jaxExtAuthBen.getAuthToken());
		authUser.setLastLogin(jaxExtAuthBen.getLastLogin());
		authUser.setSamlIDPEntityID(jaxExtAuthBen.getSamlIDPEntityID());
		authUser.setSamlSPEntityID(jaxExtAuthBen.getSamlSPEntityID());
		authUser.setSamlNameId(jaxExtAuthBen.getSamlNameId());
		authUser.setSessionIndex(jaxExtAuthBen.getSessionIndex());
		return authUser;
	}
}
