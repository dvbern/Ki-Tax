/*
 * Copyright (C) 2018 DV Bern AG, Switzerland
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

package ch.dvbern.ebegu.api.converter;

import java.time.LocalDate;
import java.util.Objects;

import javax.annotation.Nonnull;
import javax.enterprise.context.RequestScoped;

import ch.dvbern.ebegu.api.dtos.JaxAbstractDTO;
import ch.dvbern.ebegu.api.dtos.JaxAbstractDateRangedDTO;
import ch.dvbern.ebegu.api.dtos.JaxAbstractIntegerPensumDTO;
import ch.dvbern.ebegu.api.dtos.JaxAbstractPersonDTO;
import ch.dvbern.ebegu.api.dtos.JaxBetreuungsmitteilungPensum;
import ch.dvbern.ebegu.api.dtos.JaxBetreuungspensum;
import ch.dvbern.ebegu.api.dtos.JaxBetreuungspensumAbweichung;
import ch.dvbern.ebegu.api.dtos.JaxId;
import ch.dvbern.ebegu.entities.AbstractDateRangedEntity;
import ch.dvbern.ebegu.entities.AbstractEntity;
import ch.dvbern.ebegu.entities.AbstractIntegerPensum;
import ch.dvbern.ebegu.entities.AbstractMutableEntity;
import ch.dvbern.ebegu.entities.AbstractPersonEntity;
import ch.dvbern.ebegu.entities.BetreuungsmitteilungPensum;
import ch.dvbern.ebegu.entities.Betreuungspensum;
import ch.dvbern.ebegu.entities.BetreuungspensumAbweichung;
import ch.dvbern.ebegu.types.DateRange;
import ch.dvbern.ebegu.util.Constants;

import static com.google.common.base.Preconditions.checkNotNull;

@RequestScoped
public class AbstractConverter {

	@Nonnull
	public String toResourceId(@Nonnull final AbstractEntity entity) {
		return Objects.requireNonNull(entity.getId());
	}

	@Nonnull
	public String toEntityId(@Nonnull final JaxId resourceId) {
		return Objects.requireNonNull(resourceId.getId());
	}

	@Nonnull
	public JaxId toJaxId(@Nonnull final AbstractEntity entity) {
		return new JaxId(entity.getId());
	}

	@Nonnull
	public JaxId toJaxId(@Nonnull final JaxAbstractDTO entity) {
		Objects.requireNonNull(entity);
		Objects.requireNonNull(entity.getId());
		return new JaxId(entity.getId());
	}

	@Nonnull
	protected <T extends JaxAbstractDTO> T convertAbstractFieldsToJAX(
		@Nonnull final AbstractEntity abstEntity,
		final T jaxDTOToConvertTo) {

		jaxDTOToConvertTo.setTimestampErstellt(abstEntity.getTimestampErstellt());
		jaxDTOToConvertTo.setTimestampMutiert(abstEntity.getTimestampMutiert());
		jaxDTOToConvertTo.setId(checkNotNull(abstEntity.getId()));
		jaxDTOToConvertTo.setVersion(abstEntity.getVersion());

		return jaxDTOToConvertTo;
	}

	@Nonnull
	protected <T extends AbstractEntity> T convertAbstractFieldsToEntity(
		final JaxAbstractDTO jaxToConvert,
		@Nonnull final T abstEntityToConvertTo) {

		if (jaxToConvert.getId() != null) {
			abstEntityToConvertTo.setId(jaxToConvert.getId());
			abstEntityToConvertTo.setVersion(jaxToConvert.getVersion());
			//ACHTUNG hier timestamp erstellt und mutiert NICHT konvertieren da diese immer auf dem server gesetzt
			// werden muessen
		}

		return abstEntityToConvertTo;
	}

	@Nonnull
	protected <T extends JaxAbstractDTO> T convertAbstractVorgaengerFieldsToJAX(
		@Nonnull final AbstractMutableEntity abstEntity,
		final T jaxDTOToConvertTo) {

		convertAbstractFieldsToJAX(abstEntity, jaxDTOToConvertTo);
		jaxDTOToConvertTo.setVorgaengerId(abstEntity.getVorgaengerId());

		return jaxDTOToConvertTo;
	}

	@Nonnull
	protected <T extends AbstractMutableEntity> T convertAbstractVorgaengerFieldsToEntity(
		final JaxAbstractDTO jaxToConvert,
		@Nonnull final T abstEntityToConvertTo) {

		convertAbstractFieldsToEntity(jaxToConvert, abstEntityToConvertTo);
		abstEntityToConvertTo.setVorgaengerId(jaxToConvert.getVorgaengerId());

		return abstEntityToConvertTo;
	}

	/**
	 * Converts all person related fields from Jax to Entity
	 *
	 * @param personEntityJAXP das objekt als Jax
	 * @param personEntity das object als Entity
	 */
	protected void convertAbstractPersonFieldsToEntity(
		final JaxAbstractPersonDTO personEntityJAXP,
		final AbstractPersonEntity personEntity) {

		convertAbstractVorgaengerFieldsToEntity(personEntityJAXP, personEntity);
		personEntity.setNachname(personEntityJAXP.getNachname());
		personEntity.setVorname(personEntityJAXP.getVorname());
		personEntity.setGeburtsdatum(personEntityJAXP.getGeburtsdatum());
		personEntity.setGeschlecht(personEntityJAXP.getGeschlecht());
	}

	/**
	 * Converts all person related fields from Entity to Jax
	 *
	 * @param personEntity das object als Entity
	 * @param personEntityJAXP das objekt als Jax
	 */
	protected void convertAbstractPersonFieldsToJAX(
		final AbstractPersonEntity personEntity,
		final JaxAbstractPersonDTO personEntityJAXP) {

		convertAbstractVorgaengerFieldsToJAX(personEntity, personEntityJAXP);
		personEntityJAXP.setNachname(personEntity.getNachname());
		personEntityJAXP.setVorname(personEntity.getVorname());
		personEntityJAXP.setGeburtsdatum(personEntity.getGeburtsdatum());
		personEntityJAXP.setGeschlecht(personEntity.getGeschlecht());
	}

	/**
	 * Checks fields gueltigAb and gueltigBis from given object and stores the corresponding DateRange object in the
	 * given Jax Object
	 * If gueltigAb is null then current date is set instead
	 * If gueltigBis is null then end_of_time is set instead
	 *
	 * @param dateRangedJAXP AbstractDateRanged jax where to take the date from
	 * @param dateRangedEntity AbstractDateRanged entity where to store the date into
	 */
	@Nonnull
	protected AbstractDateRangedEntity convertAbstractDateRangedFieldsToEntity(
		final JaxAbstractDateRangedDTO dateRangedJAXP, final AbstractDateRangedEntity
		dateRangedEntity) {
		convertAbstractVorgaengerFieldsToEntity(dateRangedJAXP, dateRangedEntity);
		final LocalDate dateAb =
			dateRangedJAXP.getGueltigAb() == null ? LocalDate.now() : dateRangedJAXP.getGueltigAb();
		final LocalDate dateBis =
			dateRangedJAXP.getGueltigBis() == null ? Constants.END_OF_TIME : dateRangedJAXP.getGueltigBis();
		dateRangedEntity.setGueltigkeit(new DateRange(dateAb, dateBis));
		return dateRangedEntity;
	}

	/***
	 * Konvertiert eine DateRange fuer den Client. Wenn das DatumBis {@link Constants#END_OF_TIME} entspricht wird es
	 * NICHT konvertiert
	 */
	protected void convertAbstractDateRangedFieldsToJAX(
		@Nonnull final AbstractDateRangedEntity dateRangedEntity,
		@Nonnull final JaxAbstractDateRangedDTO jaxDateRanged) {

		Objects.requireNonNull(dateRangedEntity.getGueltigkeit());
		convertAbstractVorgaengerFieldsToJAX(dateRangedEntity, jaxDateRanged);
		jaxDateRanged.setGueltigAb(dateRangedEntity.getGueltigkeit().getGueltigAb());
		if (Constants.END_OF_TIME.equals(dateRangedEntity.getGueltigkeit().getGueltigBis())) {
			jaxDateRanged.setGueltigBis(null); // end of time gueltigkeit wird nicht an client geschickt
		} else {
			jaxDateRanged.setGueltigBis(dateRangedEntity.getGueltigkeit().getGueltigBis());
		}
	}

	protected void convertAbstractPensumFieldsToEntity(
		final JaxAbstractIntegerPensumDTO jaxPensum,
		final AbstractIntegerPensum pensumEntity) {

		convertAbstractDateRangedFieldsToEntity(jaxPensum, pensumEntity);
		pensumEntity.setPensum(jaxPensum.getPensum());
	}

	protected void convertAbstractPensumFieldsToEntity(
		JaxBetreuungspensum jaxPensum,
		Betreuungspensum pensumEntity) {

		convertAbstractDateRangedFieldsToEntity(jaxPensum, pensumEntity);
		pensumEntity.setPensum(jaxPensum.getPensum());
		pensumEntity.setUnitForDisplay(jaxPensum.getUnitForDisplay());
	}

	protected void convertAbstractPensumFieldsToEntity(
		JaxBetreuungspensumAbweichung jaxPensum,
		BetreuungspensumAbweichung pensumEntity) {

		convertAbstractDateRangedFieldsToEntity(jaxPensum, pensumEntity);
		pensumEntity.setMonatlicheBetreuungskosten(jaxPensum.getMonatlicheBetreuungskosten());
		pensumEntity.setUnitForDisplay(jaxPensum.getUnitForDisplay());
		pensumEntity.setPensum(jaxPensum.getPensum());
	}

	protected void convertAbstractPensumFieldsToEntity(
		JaxBetreuungsmitteilungPensum jaxPensum,
		BetreuungsmitteilungPensum pensumEntity) {

		convertAbstractDateRangedFieldsToEntity(jaxPensum, pensumEntity);
		pensumEntity.setPensum(jaxPensum.getPensum());
		pensumEntity.setMonatlicheBetreuungskosten(jaxPensum.getMonatlicheBetreuungskosten());
		pensumEntity.setUnitForDisplay(jaxPensum.getUnitForDisplay());
	}

	protected void convertAbstractPensumFieldsToJAX(
		final AbstractIntegerPensum pensum,
		final JaxAbstractIntegerPensumDTO jaxPensum) {

		convertAbstractDateRangedFieldsToJAX(pensum, jaxPensum);
		jaxPensum.setPensum(pensum.getPensum());
	}

	protected void convertAbstractPensumFieldsToJAX(
		final BetreuungsmitteilungPensum pensum,
		final JaxBetreuungspensum jaxPensum) {

		convertAbstractDateRangedFieldsToJAX(pensum, jaxPensum);
		jaxPensum.setPensum(pensum.getPensum());
		jaxPensum.setUnitForDisplay(pensum.getUnitForDisplay());
		jaxPensum.setMonatlicheBetreuungskosten(pensum.getMonatlicheBetreuungskosten());
	}

	protected void convertAbstractPensumFieldsToJAX(
		BetreuungsmitteilungPensum pensum,
		JaxBetreuungsmitteilungPensum jaxPensum) {

		convertAbstractDateRangedFieldsToJAX(pensum, jaxPensum);

		jaxPensum.setPensum(pensum.getPensum());
		jaxPensum.setUnitForDisplay(pensum.getUnitForDisplay());
		jaxPensum.setMonatlicheBetreuungskosten(pensum.getMonatlicheBetreuungskosten());
	}

	protected void convertAbstractPensumFieldsToJAX(
		Betreuungspensum pensum,
		JaxBetreuungspensum jaxPensum) {

		convertAbstractDateRangedFieldsToJAX(pensum, jaxPensum);
		jaxPensum.setPensum(pensum.getPensum());
		jaxPensum.setUnitForDisplay(pensum.getUnitForDisplay());
		jaxPensum.setMonatlicheBetreuungskosten(pensum.getMonatlicheBetreuungskosten());
	}

	protected void convertAbstractPensumFieldsToJAX(
		BetreuungspensumAbweichung pensum,
		JaxBetreuungspensumAbweichung jaxPensum) {

		convertAbstractDateRangedFieldsToJAX(pensum, jaxPensum);

		jaxPensum.setPensum(pensum.getPensum());
		jaxPensum.setUnitForDisplay(pensum.getUnitForDisplay());
		jaxPensum.setMonatlicheBetreuungskosten(pensum.getMonatlicheBetreuungskosten());
	}

}
