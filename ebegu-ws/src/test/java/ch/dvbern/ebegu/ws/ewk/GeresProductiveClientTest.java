/*
 * Copyright (C) 2024 DV Bern AG, Switzerland
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

import java.math.BigInteger;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.stream.Stream;

import javax.xml.soap.SOAPFault;

import ch.bedag.geres.schemas._20180101.geresresidentinforesponse.BaseDeliveryType;
import ch.bedag.geres.schemas._20180101.geresresidentinfoservice.InfrastructureFault;
import ch.bedag.geres.schemas._20180101.geresresidentinfoservice.InvalidArgumentsFault;
import ch.bedag.geres.schemas._20180101.geresresidentinfoservice.PermissionDeniedFault;
import ch.bedag.geres.schemas._20180101.geresresidentinfoservice.ResidentInfoParametersType;
import ch.bedag.geres.schemas._20180101.geresresidentinfoservice.ResidentInfoPortType;
import ch.dvbern.ebegu.dto.personensuche.EWKPerson;
import ch.dvbern.ebegu.dto.personensuche.EWKResultat;
import ch.dvbern.ebegu.entities.Gesuchsteller;
import ch.dvbern.ebegu.entities.PersonensucheAuditLog;
import ch.dvbern.ebegu.enums.Geschlecht;
import ch.dvbern.ebegu.errors.PersonenSucheServiceException;
import ch.dvbern.ebegu.services.PersonenSucheAuditLogService;
import ch.dvbern.ebegu.test.TestDataUtil;
import ch.ech.xmlns.ech_0011_f._8.BirthDataType;
import ch.ech.xmlns.ech_0011_f._8.MaritalDataType;
import ch.ech.xmlns.ech_0011_f._8.NameDataType;
import ch.ech.xmlns.ech_0020_f._3.BaseDeliveryPersonType;
import ch.ech.xmlns.ech_0020_f._3.BirthInfoType;
import ch.ech.xmlns.ech_0020_f._3.EventBaseDelivery;
import ch.ech.xmlns.ech_0020_f._3.MaritalInfoType;
import ch.ech.xmlns.ech_0020_f._3.NameInfoType;
import ch.ech.xmlns.ech_0044_f._4.DatePartiallyKnownType;
import ch.ech.xmlns.ech_0044_f._4.NamedPersonIdType;
import ch.ech.xmlns.ech_0044_f._4.PersonIdentificationType;
import com.sun.xml.messaging.saaj.soap.ver1_1.Fault1_1Impl;
import com.sun.xml.ws.fault.ServerSOAPFaultException;
import org.easymock.Capture;
import org.easymock.CaptureType;
import org.easymock.EasyMock;
import org.easymock.EasyMockExtension;
import org.easymock.EasyMockSupport;
import org.easymock.Mock;
import org.easymock.MockType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import static org.easymock.EasyMock.anyObject;
import static org.easymock.EasyMock.capture;
import static org.easymock.EasyMock.eq;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.reset;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsStringIgnoringCase;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.lessThan;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(EasyMockExtension.class)
class GeresProductiveClientTest extends EasyMockSupport {

	@Mock
	private ResidentInfoPortType port;

	@Mock
	private PersonenSucheAuditLogService personenSucheAuditLogService;

	private GeresProductiveClient testee;

	@BeforeEach
	public void setUp() {
		testee = new GeresProductiveClient(personenSucheAuditLogService, port);
	}

	@AfterEach
	void tearDown() {
		verifyAll();
	}

	static Stream<Arguments> bfsNummerSource() {
		return Stream.of(30L, null).map(Arguments::of);
	}

	@Nested
	class SuchePersonMitFallbackOhneVornameMitBfsNummer {
		@ParameterizedTest(name = "BFS nr: {0}")
		@DisplayName("must fall back to search without first name if first search is empty")
		@MethodSource("ch.dvbern.ebegu.ws.ewk.GeresProductiveClientTest#bfsNummerSource")
		void suchePersonMitFallbackOhneVorname(Long bfsNummer)
			throws PersonenSucheServiceException, InvalidArgumentsFault, InfrastructureFault, PermissionDeniedFault {
			// given
			Gesuchsteller gs = getGs();

			BaseDeliveryType emptyDelivery = createDelivery(gs);
			emptyDelivery.getMessages().clear();

			BaseDeliveryType delivery = createDelivery(gs);

			Capture<ResidentInfoParametersType> paramsCapture = EasyMock.newCapture(CaptureType.ALL);

			expect(port.residentInfoFull(capture(paramsCapture), anyObject(), anyObject()))
				.andReturn(emptyDelivery)
				.andReturn(delivery);
			createAuditLogCapture();
			createAuditLogCapture();
			replayAll();

			// when
			EWKResultat ewkResultat = testee.suchePersonMitFallbackOhneVorname(
				gs.getNachname(),
				gs.getVorname(),
				gs.getGeburtsdatum(),
				gs.getGeschlecht(), bfsNummer);

			// verify
			ResidentInfoParametersType parametersType0 = paramsCapture.getValues().get(0);
			assertThat(parametersType0.getFirstName(), is(gs.getVorname()));
			assertThat(parametersType0.getOfficialName(), is(gs.getNachname()));
			assertThat(parametersType0.getDateOfBirth(), is(gs.getGeburtsdatum()));
			assertThat(parametersType0.getSex(), is(1));
			assertThat(parametersType0.getMunicipalityNumber(), bfsNummer == null ? is(nullValue()) : is(bfsNummer.intValue()));

			ResidentInfoParametersType parametersType1 = paramsCapture.getValues().get(1);
			assertThat(parametersType1.getFirstName(), is(nullValue()));
			assertThat(parametersType1.getOfficialName(), is(gs.getNachname()));
			assertThat(parametersType1.getDateOfBirth(), is(gs.getGeburtsdatum()));
			assertThat(parametersType1.getSex(), is(1));
			assertThat(parametersType1.getMunicipalityNumber(), bfsNummer == null ? is(nullValue()) : is(bfsNummer.intValue()));

			assertThat(ewkResultat.getPersonen(), hasSize(1));
			assertThat(ewkResultat.getPersonen().get(0).getVorname(), is(gs.getVorname()));
			assertThat(ewkResultat.getPersonen().get(0).getNachname(), is(gs.getNachname()));
			assertThat(ewkResultat.getPersonen().get(0).getGeschlecht(), is(Geschlecht.MAENNLICH));
			assertThat(ewkResultat.getPersonen().get(0).isGefunden(), is(true));
			assertThat(ewkResultat.getPersonen().get(0).isHaushalt(), is(false));
		}
	}

	private static Gesuchsteller getGs() {
		Gesuchsteller gs = TestDataUtil.createDefaultGesuchsteller();
		gs.setSozialversicherungsnummer("756.1234.5678.97");
		return gs;
	}

	@Nested
	class SuchePersonenInHaushalt {
		@Test
		@DisplayName("must set haushalt to true")
		void suchePersonenInHaushaltMustSetHaushaltTrue()
			throws PersonenSucheServiceException, InvalidArgumentsFault, InfrastructureFault, PermissionDeniedFault {
			// given
			Gesuchsteller gs = getGs();

			BaseDeliveryType delivery = createDelivery(gs);
			Capture<ResidentInfoParametersType> paramsCapture = EasyMock.newCapture();

			expect(port.residentInfoFull(capture(paramsCapture), anyObject(), anyObject())).andReturn(delivery);
			createAuditLogCapture();
			replayAll();
			long wohnungsId = 10L;
			long gebaeudeId = 20L;

			// when
			EWKResultat ewkResultat = testee.suchePersonenInHaushalt(wohnungsId, gebaeudeId);

			// verify
			ResidentInfoParametersType parametersType = paramsCapture.getValue();
			assertThat(parametersType.getEgid(), is(gebaeudeId));
			assertThat(parametersType.getEwid(), is(wohnungsId));

			assertThat(ewkResultat.getPersonen(), hasSize(1));
			assertThat(ewkResultat.getPersonen().get(0).getVorname(), is(gs.getVorname()));
			assertThat(ewkResultat.getPersonen().get(0).getNachname(), is(gs.getNachname()));
			assertThat(ewkResultat.getPersonen().get(0).getGeschlecht(), is(Geschlecht.MAENNLICH));
			assertThat(ewkResultat.getPersonen().get(0).isGefunden(), is(true));
			assertThat(ewkResultat.getPersonen().get(0).isHaushalt(), is(true));
		}
	}

	private Capture<PersonensucheAuditLog> createAuditLogCapture() {
		Capture<PersonensucheAuditLog> auditLogCapture = EasyMock.newCapture();
		expect(personenSucheAuditLogService.savePersonenSucheAuditLog(EasyMock.capture(auditLogCapture))).andReturn(null);
		return auditLogCapture;
	}

	@Nested
	class ResidentInfoFull {
		@Test
		@DisplayName("must pass on parameters to service port, return converted object and write audit log entry")
		void happyPath() throws PersonenSucheServiceException, InvalidArgumentsFault, InfrastructureFault,
			PermissionDeniedFault {
			// given
			Gesuchsteller gs = getGs();
			Capture<PersonensucheAuditLog> auditLogCapture = createAuditLogCapture();

			BaseDeliveryType delivery = createDelivery(gs);
			ResidentInfoParametersType params = new ResidentInfoParametersType();
			LocalDate now = LocalDate.now();
			int searchmax = 10;
			expect(port.residentInfoFull(
				eq(params),
				eq(now),
				eq(searchmax))).andReturn(delivery);
			replayAll();

			// when
			EWKResultat ewkResultat = testee.residentInfoFull(params, now, searchmax);

			// verify
			assertThat(ewkResultat.getPersonen(), hasSize(1));
			assertThat(ewkResultat.getPersonen().get(0).getVorname(), is(gs.getVorname()));
			assertThat(ewkResultat.getPersonen().get(0).getNachname(), is(gs.getNachname()));
			assertThat(ewkResultat.getPersonen().get(0).getGeschlecht(), is(Geschlecht.MAENNLICH));
			assertThat(ewkResultat.getPersonen().get(0).isGefunden(), is(true));
			assertThat(ewkResultat.getPersonen().get(0).isHaushalt(), is(false));

			PersonensucheAuditLog auditLog = auditLogCapture.getValue();
			assertThat(
				auditLog.getResidentInfoParameters(),
				is("{\"dbPersonId\":null,\"personId\":null,\"personStatus\":[],\"officialName\":null,\"firstName\":null,"
					+ "\"sex\":null,\"dateOfBirth\":null,\"dateOfBirthAfter\":null,\"dateOfBirthBefore\":null,"
					+ "\"dateOfDeathAfter\":null,\"dateOfDeathBefore\":null,\"maritalStatus\":null,\"streetName\":null,"
					+ "\"houseNumber\":null,\"swissZipCode\":null,\"town\":null,\"municipalityNumber\":null,\"egid\":null,"
					+ "\"ewid\":null,\"changedSince\":null,\"originalName\":null}"));
			assertThat(auditLog.getFaultReceived(), is(nullValue()));
			assertThat(auditLog.getNumResultsReceived(), is(1L));
			assertThat(auditLog.getTotalNumberOfResults(), is(nullValue()));
			assertThat(auditLog.getValidityDate(), is(now));
			assertThat(auditLog.getTimestampSearchstart().until(LocalDateTime.now(), ChronoUnit.SECONDS), is(lessThan(10L)));
			assertThat(auditLog.getTimestampResult().until(LocalDateTime.now(), ChronoUnit.SECONDS), is(lessThan(1L)));
		}

		@ParameterizedTest
		@MethodSource("ch.dvbern.ebegu.ws.ewk.GeresProductiveClientTest#geresExceptionSource")
		@DisplayName("must wrap and write audit log in for exception type")
		void mustLogExceptions(Exception exception) throws InvalidArgumentsFault, InfrastructureFault, PermissionDeniedFault {
			// given
			Capture<PersonensucheAuditLog> auditLogCapture = createAuditLogCapture();

			ResidentInfoParametersType params = new ResidentInfoParametersType();
			LocalDate now = LocalDate.now();
			int searchmax = 10;
			expect(port.residentInfoFull(anyObject(), anyObject(), anyObject())).andThrow(exception);
			replayAll();

			// when
			PersonenSucheServiceException personenSucheServiceException =
				assertThrows(PersonenSucheServiceException.class, () -> testee.residentInfoFull(params, now, searchmax));

			// verify
			assertThat(personenSucheServiceException.getCause(), is(exception));

			PersonensucheAuditLog auditLog = auditLogCapture.getValue();
			assertThat(auditLog.getFaultReceived(), is(exception.getClass().getSimpleName()));
			assertThat(auditLog.getNumResultsReceived(), is(nullValue()));
		}
	}

	static Stream<Arguments> geresExceptionSource() {
		return Stream.of(
				new PermissionDeniedFault(
					"permissiondenied",
					new ch.bedag.geres.schemas._20180101.geresechtypes.PermissionDeniedFault()),
				new InvalidArgumentsFault(
					"invalidargs",
					new ch.bedag.geres.schemas._20180101.geresechtypes.InvalidArgumentsFault()),
				new InfrastructureFault(
					"infrafault",
					new ch.bedag.geres.schemas._20180101.geresechtypes.InfrastructureFault()))
			.map(Arguments::of);
	}

	@Nested
	class SuchePersonMitAhvNummer {
		@Test
		@DisplayName("must return person found by Geres")
		void mustReturnPerson()
			throws PersonenSucheServiceException, InvalidArgumentsFault, InfrastructureFault, PermissionDeniedFault {
			// given
			Gesuchsteller gs = getGs();

			BaseDeliveryType delivery = createDelivery(gs);

			Capture<ResidentInfoParametersType> paramsCapture = EasyMock.newCapture();
			expect(port.residentInfoFull(
				capture(paramsCapture),
				eq(LocalDate.now()),
				eq(GeresProductiveClient.SEARCH_MAX))).andReturn(delivery);
			createAuditLogCapture();
			replayAll();

			// when
			EWKPerson ewkPerson = testee.suchePersonMitAhvNummer(gs);

			// verify
			ResidentInfoParametersType parametersType = paramsCapture.getValue();
			assertThat(parametersType.getPersonId().getPersonId(), is(gs.getSozialversicherungsnummer().replace(".", "")));
			assertThat(
				parametersType.getPersonId().getPersonIdCategory(),
				is(GeresProductiveClient.ID_CATEGORY_AHV_13));

			assertThat(ewkPerson.getPersonID(), is(gs.getId()));
			assertThat(ewkPerson.getVorname(), is(gs.getVorname()));
			assertThat(ewkPerson.getNachname(), is(gs.getNachname()));
			assertThat(ewkPerson.getGeschlecht(), is(Geschlecht.MAENNLICH));
			assertThat(ewkPerson.isGefunden(), is(true));
		}

		@Test
		@DisplayName("must return 'not found' person if Geres did not find any person")
		void mustReturnNotFoundPerson()
			throws PersonenSucheServiceException, InvalidArgumentsFault, InfrastructureFault, PermissionDeniedFault {
			// given
			Gesuchsteller gs = getGs();
			Capture<ResidentInfoParametersType> paramsCapture = EasyMock.newCapture();

			ServerSOAPFaultException soapFaultException = createServerSOAPFaultException("No persons found");

			expect(port.residentInfoFull(
				capture(paramsCapture),
				eq(LocalDate.now()),
				eq(GeresProductiveClient.SEARCH_MAX))).andThrow(soapFaultException);
			createAuditLogCapture();
			replayAll();

			// when
			EWKPerson ewkPerson = testee.suchePersonMitAhvNummer(gs);

			// verify
			ResidentInfoParametersType parametersType = paramsCapture.getValue();
			assertThat(parametersType.getPersonId().getPersonId(), is(gs.getSozialversicherungsnummer().replace(".", "")));
			assertThat(
				parametersType.getPersonId().getPersonIdCategory(),
				is(GeresProductiveClient.ID_CATEGORY_AHV_13));

			assertThat(ewkPerson.getVorname(), is(gs.getVorname()));
			assertThat(ewkPerson.getNachname(), is(gs.getNachname()));
			assertThat(ewkPerson.getGeschlecht(), is(Geschlecht.MAENNLICH));
			assertThat(ewkPerson.isGefunden(), is(false));
		}

		@Test
		@DisplayName("must re-throw exception if it contains other message")
		void mustRethrowOtherExceptions() throws InvalidArgumentsFault, InfrastructureFault, PermissionDeniedFault {
			// given
			Gesuchsteller gs = getGs();
			Capture<ResidentInfoParametersType> paramsCapture = EasyMock.newCapture();
			Capture<PersonensucheAuditLog> auditLogCapture = createAuditLogCapture();
			ServerSOAPFaultException soapFaultException = createServerSOAPFaultException("something else");
			expect(port.residentInfoFull(
				capture(paramsCapture),
				eq(LocalDate.now()),
				eq(GeresProductiveClient.SEARCH_MAX))).andThrow(soapFaultException);
			replayAll();

			// when
			// verify
			assertThrows(ServerSOAPFaultException.class, () -> testee.suchePersonMitAhvNummer(gs));

			PersonensucheAuditLog auditLog = auditLogCapture.getValue();
			assertThat(auditLog.getFaultReceived(), is(nullValue()));
		}

		@Test
		@DisplayName("must throw exception if more than one person is returned")
		void mustThrowIfMoreThanOneResult() throws InvalidArgumentsFault, InfrastructureFault, PermissionDeniedFault {
			// given
			Gesuchsteller gs = getGs();
			Capture<PersonensucheAuditLog> auditLogCapture = createAuditLogCapture();

			BaseDeliveryType delivery = createDelivery(gs);
			BaseDeliveryType delivery2 = createDelivery(gs);
			delivery.getMessages().addAll(delivery2.getMessages());

			expect(port.residentInfoFull(anyObject(), anyObject(), anyObject())).andReturn(delivery);
			replayAll();

			// when
			// verify
			PersonenSucheServiceException personenSucheServiceException =
				assertThrows(PersonenSucheServiceException.class, () -> testee.suchePersonMitAhvNummer(gs));

			assertThat(personenSucheServiceException.getMessage(), containsStringIgnoringCase("more than one"));
			PersonensucheAuditLog auditLog = auditLogCapture.getValue();
			assertThat(auditLog.getFaultReceived(), is(nullValue()));
		}

		private ServerSOAPFaultException createServerSOAPFaultException(String message) {
			SOAPFault soapFault = mock(MockType.NICE, Fault1_1Impl.class);
			expect(soapFault.getFaultString()).andReturn(message);
			replay(soapFault); // ^ is called in the exception's constructor
			ServerSOAPFaultException soapFaultException = new ServerSOAPFaultException(soapFault);
			reset(soapFault);
			return soapFaultException;
		}
	}

	private static BaseDeliveryType createDelivery(Gesuchsteller gs) {
		BaseDeliveryType response = new BaseDeliveryType();
		EventBaseDelivery baseDelivery = new EventBaseDelivery();
		BaseDeliveryPersonType deliveryPerson = new BaseDeliveryPersonType();
		NameInfoType nameInfo = new NameInfoType();
		NameDataType nameData = new NameDataType();
		nameData.setFirstName(gs.getVorname());
		nameData.setOfficialName(gs.getNachname());
		nameInfo.setNameData(nameData);
		deliveryPerson.setNameInfo(nameInfo);
		BirthInfoType birthInfo = new BirthInfoType();
		BirthDataType birthData = new BirthDataType();
		DatePartiallyKnownType dateOfBirth = new DatePartiallyKnownType();
		dateOfBirth.setYearMonthDay(gs.getGeburtsdatum());
		birthData.setDateOfBirth(dateOfBirth);
		birthInfo.setBirthData(birthData);
		deliveryPerson.setBirthInfo(birthInfo);
		PersonIdentificationType personIdentification = new PersonIdentificationType();
		NamedPersonIdType namedPersonIdType = new NamedPersonIdType();
		namedPersonIdType.setPersonId(gs.getId());
		personIdentification.setLocalPersonId(namedPersonIdType);
		personIdentification.setSex(gs.getGeschlecht() == Geschlecht.WEIBLICH ? "2" : "1");
		deliveryPerson.setPersonIdentification(personIdentification);
		MaritalInfoType maritalInfo = new MaritalInfoType();
		maritalInfo.setMaritalData(new MaritalDataType());
		deliveryPerson.setMaritalInfo(maritalInfo);
		baseDelivery.setBaseDeliveryPerson(deliveryPerson);
		response.getMessages().add(baseDelivery);
		response.setNumberOfMessages(BigInteger.valueOf(response.getMessages().size()));
		return response;
	}
}
