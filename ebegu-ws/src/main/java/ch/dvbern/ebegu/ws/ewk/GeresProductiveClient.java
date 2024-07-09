/*
 * Copyright (C)  2020 DV Bern AG, Switzerland
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 *
 */
package ch.dvbern.ebegu.ws.ewk;

import ch.bedag.geres.schemas._20180101.geresechtypes.FaultBase;
import ch.bedag.geres.schemas._20180101.geresechtypes.IdType;
import ch.bedag.geres.schemas._20180101.geresechtypes.TestResponse;
import ch.bedag.geres.schemas._20180101.geresresidentinforesponse.BaseDeliveryType;
import ch.bedag.geres.schemas._20180101.geresresidentinfoservice.InfrastructureFault;
import ch.bedag.geres.schemas._20180101.geresresidentinfoservice.InvalidArgumentsFault;
import ch.bedag.geres.schemas._20180101.geresresidentinfoservice.PermissionDeniedFault;
import ch.bedag.geres.schemas._20180101.geresresidentinfoservice.ResidentInfoParametersType;
import ch.bedag.geres.schemas._20180101.geresresidentinfoservice.ResidentInfoPortType;
import ch.dvbern.ebegu.dto.personensuche.EWKPerson;
import ch.dvbern.ebegu.dto.personensuche.EWKResultat;
import ch.dvbern.ebegu.entities.Gemeinde;
import ch.dvbern.ebegu.entities.Gesuchsteller;
import ch.dvbern.ebegu.entities.PersonensucheAuditLog;
import ch.dvbern.ebegu.enums.Geschlecht;
import ch.dvbern.ebegu.errors.PersonenSucheServiceException;
import ch.dvbern.ebegu.services.PersonenSucheAuditLogService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.xml.ws.fault.ServerSOAPFaultException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

@RequiredArgsConstructor
public class GeresProductiveClient implements GeresClient {

	public static final String METHOD_NAME = "GeresWebService#residentInfoFull";
	private static final Logger LOGGER = LoggerFactory.getLogger(GeresProductiveClient.class.getSimpleName());
	static final Integer SEARCH_MAX = 100;
	static final String ID_CATEGORY_AHV_13 = "CH.VN";

	private final PersonenSucheAuditLogService personenSucheAuditLogService;

	private final ResidentInfoPortType port;

	@Override
	public String test() throws PersonenSucheServiceException {
		try {
			final TestResponse test = port.test();
			return test.getTestResponse();
		} catch (InfrastructureFault infrastructureFault) {
			String msg = createExceptionMessage("GeresWebService#test", infrastructureFault,infrastructureFault.getFaultInfo());
			throw new PersonenSucheServiceException("test", msg, infrastructureFault);
		} catch (PermissionDeniedFault permissionDeniedFault) {
			String msg = createExceptionMessage("GeresWebService#test",
				permissionDeniedFault, permissionDeniedFault.getFaultInfo());
			throw new PersonenSucheServiceException("test", msg, permissionDeniedFault);
		}

	}

	EWKResultat residentInfoFull(ResidentInfoParametersType residentInfoParameters, LocalDate validityDate, Integer searchMax)
		throws PersonenSucheServiceException {
		LocalDateTime startDate = LocalDateTime.now();
		BaseDeliveryType baseDeliveryType = null;
		Exception exceptionReceived = null;

		try {

			baseDeliveryType = port.residentInfoFull(residentInfoParameters, validityDate, searchMax);
			return GeresConverter.convertFromGeresFullResult(baseDeliveryType);
		}
		// cannot use multi-catch because there is no common base to use getFaultInfo() from
		catch (InvalidArgumentsFault invalidArgumentsFault) {
			String msg = createExceptionMessage(METHOD_NAME, invalidArgumentsFault, invalidArgumentsFault.getFaultInfo());
			exceptionReceived = invalidArgumentsFault;
			throw new PersonenSucheServiceException(METHOD_NAME, msg, invalidArgumentsFault);
		} catch (InfrastructureFault infrastructureFault) {
			String msg = createExceptionMessage(METHOD_NAME, infrastructureFault, infrastructureFault.getFaultInfo());
			exceptionReceived = infrastructureFault;
			throw new PersonenSucheServiceException(METHOD_NAME, msg, infrastructureFault);
		} catch (PermissionDeniedFault permissionDeniedFault) {
			String msg = createExceptionMessage(METHOD_NAME, permissionDeniedFault, permissionDeniedFault.getFaultInfo());
			exceptionReceived = permissionDeniedFault;
			throw new PersonenSucheServiceException(METHOD_NAME, msg, permissionDeniedFault);
		} finally {
			writeAuditLogForGeresCall(residentInfoParameters, validityDate, startDate, exceptionReceived, baseDeliveryType);
		}

	}

	@SuppressWarnings("PMD.UnusedPrivateMethod")
	private String createExceptionMessage(String methodName, Exception exception, FaultBase faultInfo) {
		return String.format(
			"Call to %s failed with %s '%s', user-message '%s', technical-message '%s', error-code: '%s'",
			methodName,
			exception.getClass().getSimpleName(),
			exception.getMessage(),
			faultInfo.getUserMessage(),
			faultInfo.getTechnicalMessage(),
			faultInfo.getErrorCode());
	}

	private void writeAuditLogForGeresCall(
		ResidentInfoParametersType residentInfoParameters,
		LocalDate validityDate,
		LocalDateTime startTime,
		@Nullable Exception exceptionReceived,
		@Nullable BaseDeliveryType baseDeliveryType
		) {

		PersonensucheAuditLog personensucheAuditLogEntry = new PersonensucheAuditLog(
			METHOD_NAME,
			convertSearchParamForLog(residentInfoParameters),
			validityDate,
			exceptionReceived != null ? exceptionReceived.getClass().getSimpleName() : null,
			null,
			baseDeliveryType != null ? baseDeliveryType.getNumberOfMessages().longValue() : null,
			startTime,
			LocalDateTime.now()
		);
		final PersonensucheAuditLog logEntry = personenSucheAuditLogService.savePersonenSucheAuditLog(personensucheAuditLogEntry);
		LOGGER.trace("Webservice call to Geres complete: Auditinfo {}", logEntry);
	}

	private String convertSearchParamForLog(ResidentInfoParametersType residentInfoParameters) {
		ObjectMapper objectMapper = new ObjectMapper();

		try {
			return objectMapper.writeValueAsString(residentInfoParameters);
		} catch (JsonProcessingException e) {
			return residentInfoParameters.getFirstName() + ' ' + DateTimeFormatter.ISO_DATE.format(residentInfoParameters.getDateOfBirth());
		}
	}

	@Nonnull
	@Override
	public EWKResultat suchePersonMitFallbackOhneVorname(@Nonnull String name, @Nonnull String vorname, @Nonnull LocalDate geburtsdatum, @Nonnull Geschlecht geschlecht, Long bfsNummer) throws PersonenSucheServiceException {
		EWKResultat ewkResultat = suchePerson(name, vorname, geburtsdatum, geschlecht, bfsNummer);
		if (ewkResultat.getPersonen().isEmpty()) {
			ewkResultat = suchePerson(name, null, geburtsdatum, geschlecht, bfsNummer);
		}
		return ewkResultat;
	}

	@Nonnull
	@Override
	public EWKResultat suchePersonMitFallbackOhneVorname(@Nonnull String name, @Nonnull String vorname, @Nonnull LocalDate geburtsdatum, @Nonnull Geschlecht geschlecht) throws PersonenSucheServiceException {
		return suchePersonMitFallbackOhneVorname(name, vorname, geburtsdatum, geschlecht, null);
	}

	@Nonnull
	@Override
	public EWKResultat suchePersonenInHaushalt(Long wohnungsId, Long gebaeudeId) throws PersonenSucheServiceException {
		ResidentInfoParametersType parameters = new ResidentInfoParametersType();
		parameters.setEgid(gebaeudeId);
		parameters.setEwid(wohnungsId);
		LocalDate validityDate = LocalDate.now();
		final EWKResultat ewkResultat = this.residentInfoFull(parameters, validityDate, SEARCH_MAX);
		ewkResultat.getPersonen().forEach(person -> person.setHaushalt(true));
		return ewkResultat;
	}

	@Nonnull
	private EWKResultat suchePerson(@Nonnull String name, String vorname, @Nonnull LocalDate geburtsdatum, @Nonnull Geschlecht geschlecht, Long bfsNummer) throws PersonenSucheServiceException {
		ResidentInfoParametersType parameters = new ResidentInfoParametersType();
		if (vorname != null) {
			parameters.setFirstName(vorname);
		}
		if (bfsNummer != null) {
			parameters.setMunicipalityNumber(bfsNummer.intValue());
		}
		parameters.setOfficialName(name);
		parameters.setDateOfBirth(geburtsdatum);
		parameters.setSex(Geschlecht.MAENNLICH == geschlecht ? 1 : 2);
		LocalDate validityDate = LocalDate.now();
		return this.residentInfoFull(parameters, validityDate, SEARCH_MAX);
	}

	@Override
	@Nonnull
	public EWKPerson suchePersonMitAhvNummerInGemeinde(
		Gesuchsteller gesuchsteller,
		Gemeinde gemeinde)
		throws PersonenSucheServiceException {
		Objects.requireNonNull(gesuchsteller.getSozialversicherungsnummer(), "VN cannot be null");
		try {
			ResidentInfoParametersType parameters = new ResidentInfoParametersType();
			IdType idType = new IdType();
			idType.setPersonIdCategory(ID_CATEGORY_AHV_13);
			idType.setPersonId(removeDots(gesuchsteller.getSozialversicherungsnummer()));
			parameters.setPersonId(idType);
			parameters.setMunicipalityNumber(gemeinde.getBfsNummer().intValue());
			LocalDate validityDate = LocalDate.now();
			EWKResultat ewkResultat = this.residentInfoFull(parameters, validityDate, SEARCH_MAX);
			throwIfMoreThanOnePersonReturned(gesuchsteller, ewkResultat);
			return ewkResultat.getPersonen().get(0);
		} catch (ServerSOAPFaultException e) {
			if (noPersonWasFound(e)) {
				return GeresUtil.createNotFoundPerson(gesuchsteller);
			}
			throw e;
		}
	}

	private static String removeDots(String vn) {
		return vn.replace(".", "");
	}

	// Because we are searching by ID it should never return more than one person, but who knows.
	private void throwIfMoreThanOnePersonReturned(Gesuchsteller gesuchsteller, EWKResultat ewkResultat) throws PersonenSucheServiceException {
		if (ewkResultat.getPersonen().size() > 1) {
			throw new PersonenSucheServiceException(
				"suchePersonMitAhvNummer",
				String.format("More than one person found for AHV Nr of GS %s", gesuchsteller.getId()));
		}
	}

	/*
	 * When searching for an ID Geres throws an exception in case there is no match.
	 *
	 * Full message is "Client received SOAP Fault from server: No persons found with the provided ID on validity date: YYYY-MM-DD for person CH.VN/xxxxxxxxxxxxx Please see the server log to find more detail regarding exact cause of the failure."
	 */
	private boolean noPersonWasFound(ServerSOAPFaultException e) {
		return e.getMessage().contains("No persons found");
	}
}
