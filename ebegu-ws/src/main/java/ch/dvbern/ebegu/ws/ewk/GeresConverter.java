/*
 * Copyright (C) 2020 DV Bern AG, Switzerland
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
 */

package ch.dvbern.ebegu.ws.ewk;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import ch.bedag.geres.schemas._20180101.geresresidentinforesponse.BaseDeliveryType;
import ch.dvbern.ebegu.dto.personensuche.EWKAdresse;
import ch.dvbern.ebegu.dto.personensuche.EWKBeziehung;
import ch.dvbern.ebegu.dto.personensuche.EWKPerson;
import ch.dvbern.ebegu.dto.personensuche.EWKResultat;
import ch.dvbern.ebegu.enums.Geschlecht;
import ch.ech.xmlns.ech_0010_f._5.AddressInformationType;
import ch.ech.xmlns.ech_0010_f._5.SwissAddressInformationType;
import ch.ech.xmlns.ech_0011_f._8.DwellingAddressType;
import ch.ech.xmlns.ech_0020_f._3.BaseDeliveryPersonType;
import ch.ech.xmlns.ech_0020_f._3.EventBaseDelivery;
import ch.ech.xmlns.ech_0020_f._3.ReportingMunicipalityType;
import ch.ech.xmlns.ech_0021_f._7.ParentalRelationshipType;
import ch.ech.xmlns.ech_0044_f._4.PersonIdentificationType;

/**
 * Konverter zwischen EWK-Objekten und unseren DTOs
 */
public final class GeresConverter {

	private GeresConverter() {
		// Util-Class, should not be initialized
	}

	/**
	 * @param totalNumberOfResults total number of persons that were matched by the search criteria
	 * @param numberOfDeliveredResults number of persons that were actually returned
	 * @param baseDelivery list containing the matched persons
	 */
	public static EWKResultat convertFromGeresFastResult(Integer totalNumberOfResults, Integer numberOfDeliveredResults, BaseDeliveryType baseDelivery) {
		final EWKResultat ewkResultat = convertFromGeresFullResult(baseDelivery);
		return ewkResultat;
	}

	public static EWKResultat convertFromGeresFullResult(BaseDeliveryType baseDelivery) {
		EWKResultat ewkResultat = new EWKResultat();
		if (!baseDelivery.getMessages().isEmpty()) {

			final List<EWKPerson> persons = baseDelivery.getMessages().stream()
				.map(GeresConverter::convertFromGeres)
				.collect(Collectors.toList());

			ewkResultat.setPersonen(persons);
		}
		return ewkResultat;

	}

	private static EWKPerson convertFromGeres(EventBaseDelivery message) {
		final BaseDeliveryPersonType baseDeliveryPerson = message.getBaseDeliveryPerson();
		EWKPerson ewkPerson = convertFromGeres(baseDeliveryPerson);
		ReportingMunicipalityType mainResicence = message.getHasMainResidence();
		if (mainResicence != null) {
			ewkPerson.setZuzugsdatum(mainResicence.getArrivalDate());
			ewkPerson.setWegzugsdatum(mainResicence.getDepartureDate());
			ewkPerson.setAdresse(convertFromGeres(mainResicence.getDwellingAddress()));
		}
		List<EWKBeziehung> beziehungen = new ArrayList<>();
		//Eltern der Person
		final List<EWKBeziehung> parents =
			message.getBaseDeliveryPerson().getParentalRelationship().stream().map(GeresConverter::createFromGeres).collect(Collectors.toList());
		beziehungen.addAll(parents);
		ewkPerson.setBeziehungen(beziehungen);
		return ewkPerson;
	}

	@Nullable
	private static EWKBeziehung createFromGeres(@Nonnull ParentalRelationshipType parentalRelationshipType) {
		final PersonIdentificationType personIdentificationPartner = parentalRelationshipType.getPartner().getPersonIdentification();
		if (personIdentificationPartner == null) {
			return null;
		}
		EWKBeziehung ewkBeziehung = new EWKBeziehung();
		ewkBeziehung.setBeziehungstyp("EWK_BEZIEHUNG_" + parentalRelationshipType.getTypeOfRelationship());
		ewkBeziehung.setPersonID(personIdentificationPartner.getLocalPersonId().getPersonId());
		ewkBeziehung.setNachname(personIdentificationPartner.getOfficialName());
		ewkBeziehung.setVorname(personIdentificationPartner.getFirstName());
		ewkBeziehung.setGeburtsdatum(personIdentificationPartner.getDateOfBirth().getYearMonthDay());
		ewkBeziehung.setGeschlecht(convertGeschlechtFromEWK(personIdentificationPartner.getSex()));
		if (parentalRelationshipType.getPartner().getAddress() != null) {
			EWKAdresse ewkAdresse = convertFromGeres(parentalRelationshipType.getPartner().getAddress().getAddressInformation());
			ewkBeziehung.setAdresse(ewkAdresse);
		}
		return ewkBeziehung;
	}

	private static EWKAdresse convertFromGeres(DwellingAddressType dwellingAddressType) {
		SwissAddressInformationType addressInformationType = dwellingAddressType.getAddress();
		EWKAdresse ewkAdresse = new EWKAdresse();
		ewkAdresse.setGebaeudeId(dwellingAddressType.getEGID());
		ewkAdresse.setWohnungsId(dwellingAddressType.getEWID());
		ewkAdresse.setAdresszusatz1(addressInformationType.getAddressLine1());
		ewkAdresse.setAdresszusatz2(addressInformationType.getAddressLine2());
		ewkAdresse.setHausnummer(addressInformationType.getHouseNumber());
		ewkAdresse.setWohnungsnummer(addressInformationType.getDwellingNumber());
		ewkAdresse.setStrasse(addressInformationType.getStreet());
		ewkAdresse.setPostleitzahl(String.valueOf(addressInformationType.getSwissZipCode()));
		ewkAdresse.setOrt(addressInformationType.getTown());
		ewkAdresse.setGebiet(addressInformationType.getLocality());
		return ewkAdresse;
	}

	private static EWKAdresse convertFromGeres(@Nonnull AddressInformationType addressInformationType) {
		EWKAdresse ewkAdresse = new EWKAdresse();
		ewkAdresse.setAdresszusatz1(addressInformationType.getAddressLine1());
		ewkAdresse.setAdresszusatz2(addressInformationType.getAddressLine2());
		ewkAdresse.setHausnummer(addressInformationType.getHouseNumber());
		ewkAdresse.setWohnungsnummer(addressInformationType.getDwellingNumber());
		ewkAdresse.setStrasse(addressInformationType.getStreet());
		ewkAdresse.setPostleitzahl(String.valueOf(addressInformationType.getSwissZipCode()));
		ewkAdresse.setOrt(addressInformationType.getTown());
		ewkAdresse.setGebiet(addressInformationType.getLocality());
		return ewkAdresse;
	}

	private static EWKPerson convertFromGeres(BaseDeliveryPersonType baseDeliveryPerson) {

		EWKPerson ewkPerson = new EWKPerson();
		ewkPerson.setPersonID(baseDeliveryPerson.getPersonIdentification().getLocalPersonId().getPersonId());
		ewkPerson.setNachname(baseDeliveryPerson.getNameInfo().getNameData().getOfficialName());
		ewkPerson.setVorname(baseDeliveryPerson.getNameInfo().getNameData().getFirstName());
		ewkPerson.setGeburtsdatum(baseDeliveryPerson.getBirthInfo().getBirthData().getDateOfBirth().getYearMonthDay());
		ewkPerson.setZivilstand("EWK_ZIVILSTAND_" + baseDeliveryPerson.getMaritalInfo().getMaritalData().getMaritalStatus());
		ewkPerson.setZivilstandsdatum(baseDeliveryPerson.getMaritalInfo().getMaritalData().getDateOfMaritalStatus());
		ewkPerson.setGeschlecht(convertGeschlechtFromEWK(baseDeliveryPerson.getPersonIdentification().getSex()));
		return ewkPerson;
	}

	private static Geschlecht convertGeschlechtFromEWK(String geschlecht) {
		return geschlecht.equals("2") ? Geschlecht.WEIBLICH : Geschlecht.MAENNLICH;
	}

}
