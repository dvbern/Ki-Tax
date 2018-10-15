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

import java.util.Objects;

import javax.annotation.Nonnull;
import javax.ejb.EJBAccessException;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;

import ch.dvbern.ebegu.api.connector.ILoginConnectorResource;
import ch.dvbern.ebegu.api.dtos.JaxExternalAuthAccessElement;
import ch.dvbern.ebegu.api.dtos.JaxExternalAuthorisierterBenutzer;
import ch.dvbern.ebegu.api.dtos.JaxExternalBenutzer;
import ch.dvbern.ebegu.api.dtos.JaxMandant;
import ch.dvbern.ebegu.api.resource.MandantResource;
import ch.dvbern.ebegu.api.util.version.VersionInfoBean;
import ch.dvbern.ebegu.authentication.AuthAccessElement;
import ch.dvbern.ebegu.config.EbeguConfiguration;
import ch.dvbern.ebegu.entities.AuthorisierterBenutzer;
import ch.dvbern.ebegu.entities.Benutzer;
import ch.dvbern.ebegu.entities.Mandant;
import ch.dvbern.ebegu.enums.BenutzerStatus;
import ch.dvbern.ebegu.enums.ErrorCodeEnum;
import ch.dvbern.ebegu.errors.EbeguEntityNotFoundException;
import ch.dvbern.ebegu.errors.LoginException;
import ch.dvbern.ebegu.services.AuthService;
import ch.dvbern.ebegu.services.BenutzerService;
import ch.dvbern.ebegu.services.MandantService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static java.util.Objects.requireNonNull;

/**
 * Service provided by KI-TAX to allow an external login module to create users and logins
 */
@SuppressWarnings({ "EjbInterceptorInspection", "EjbClassBasicInspection" })
@Stateless
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
		MandantService mandantService) {

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
			builder.append(" WARNING access is not local");
		}
		return builder.toString();

	}

	@Override
	public JaxExternalBenutzer updateOrStoreUserFromIAM(@Nonnull JaxExternalBenutzer benutzer) {
		LOG.debug("Requested url {} ", this.uriInfo.getAbsolutePath());
		LOG.debug("Requested forwared for {} ", this.request.getHeader("X-Forwarded-For"));
		checkLocalAccessOnly();

		Benutzer user = new Benutzer();
		toBenutzer(benutzer, user);

		String unusedAttr = StringUtils.join(benutzer.getCommonName(), benutzer.getTelephoneNumber(),
			benutzer.getMobile(), benutzer.getPreferredLang(), benutzer.getPostalCode(), benutzer.getState(),
			benutzer.getStreet(), benutzer.getPostalCode(), benutzer.getCountryCode(), benutzer.getCountry(),
			benutzer.getCountry(), ',');

		LOG.info(
			"The following attributes are received from the ExternalLoginModule but not yet stored {}",
			unusedAttr);

		Mandant mandant = this.mandantService.findMandant(benutzer.getMandantId())
			.orElseThrow(() -> {
				LOG.error("Mandant not found for passed id: {}", benutzer.getMandantId());
				return new EbeguEntityNotFoundException("updateOrStoreMandant", ErrorCodeEnum.ERROR_ENTITY_NOT_FOUND);
			});
		user.setMandant(mandant);

		Benutzer storedUser = benutzerService.updateOrStoreUserFromIAM(user);

		return convertBenutzerToJax(storedUser);
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
	public JaxExternalBenutzer updateUserFromIAM(@Nonnull String userId, @Nonnull JaxExternalBenutzer benutzer) {
		requireNonNull(userId);
		requireNonNull(benutzer);
		checkLocalAccessOnly();

		Benutzer existingBenutzer = benutzerService.findBenutzerById(userId)
			.orElseThrow(() -> new EbeguEntityNotFoundException(
				"Benutzer not found",
				ErrorCodeEnum.ERROR_ENTITY_NOT_FOUND));

		String persistedEmail = existingBenutzer.getEmail();
		String externalEmail = benutzer.getEmail();

		if (!persistedEmail.equals(externalEmail)) {
			throw new LoginException(ErrorCodeEnum.ERROR_EMAIL_MISMATCH, persistedEmail, externalEmail);
		}

		toBenutzer(benutzer, existingBenutzer);

		if (existingBenutzer.getStatus() == BenutzerStatus.EINGELADEN) {
			existingBenutzer.setStatus(BenutzerStatus.AKTIV);
		}

		Benutzer updatedBenutzer = benutzerService.updateOrStoreUserFromIAM(existingBenutzer);

		return convertBenutzerToJax(updatedBenutzer);
	}

	@Nonnull
	private JaxExternalBenutzer convertBenutzerToJax(@Nonnull Benutzer storedUser) {
		JaxExternalBenutzer jaxExternalBenutzer = new JaxExternalBenutzer();
		jaxExternalBenutzer.setUsername(storedUser.getUsername());
		jaxExternalBenutzer.setExternalUUID(storedUser.getExternalUUID());
		jaxExternalBenutzer.setEmail(storedUser.getEmail());
		jaxExternalBenutzer.setNachname(storedUser.getNachname());
		jaxExternalBenutzer.setVorname(storedUser.getVorname());
		jaxExternalBenutzer.setRole(storedUser.getRole().name());
		jaxExternalBenutzer.setMandantId(storedUser.getMandant().getId());
		jaxExternalBenutzer.setGesperrt(storedUser.getStatus() != BenutzerStatus.AKTIV);
		if (storedUser.getInstitution() != null) {
			jaxExternalBenutzer.setInstitutionId(storedUser.getInstitution().getId());
		}
		if (storedUser.getTraegerschaft() != null) {
			jaxExternalBenutzer.setTraegerschaftId(storedUser.getTraegerschaft().getId());
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
	public JaxExternalAuthAccessElement createLoginFromIAM(@Nonnull JaxExternalAuthorisierterBenutzer jaxExtAuthUser) {
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
		@Nonnull AuthAccessElement loginDataForCookie) {
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
