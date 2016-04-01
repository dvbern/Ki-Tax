package ch.dvbern.ebegu.api.converter;

import ch.dvbern.ebegu.api.dtos.*;
import ch.dvbern.ebegu.entities.AbstractEntity;
import ch.dvbern.ebegu.entities.Adresse;
import ch.dvbern.ebegu.entities.ApplicationProperty;
import ch.dvbern.ebegu.entities.Person;
import ch.dvbern.ebegu.util.Constants;
import ch.dvbern.lib.date.DateConvertUtils;
import org.apache.commons.lang3.Validate;
import org.hibernate.envers.DefaultRevisionEntity;
import org.hibernate.envers.RevisionType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.enterprise.context.Dependent;
import java.time.LocalDate;
import java.util.Objects;

import static com.google.common.base.Preconditions.checkNotNull;


@Dependent
@SuppressWarnings({"PMD.NcssTypeCount", "unused"})
public class JaxBConverter {

	private static final Logger LOG = LoggerFactory.getLogger(JaxBConverter.class);


	@Nonnull
	public String toResourceId(@Nonnull final AbstractEntity entity) {
		return String.valueOf(Objects.requireNonNull(entity.getId()));
	}

	@Nonnull
	public String toEntityId(@Nonnull final JaxId resourceId) {
		// TODO wahrscheinlich besser manuell auf NULL pruefen und gegebenenfalls eine IllegalArgumentException werfen
		return Objects.requireNonNull(resourceId.getId());
	}

	@Nonnull
	public String toEntityId(@Nonnull final JaxAbstractDTO resource) {
		return toEntityId(Objects.requireNonNull(resource.getId()));
	}

	@Nonnull
	private <T extends JaxAbstractDTO> T convertAbstractFieldsToJAX(@Nonnull final AbstractEntity abstEntity, T jaxDTOToConvertTo) {
		jaxDTOToConvertTo.setTimestampErstellt(abstEntity.getTimestampErstellt());
		jaxDTOToConvertTo.setTimestampMutiert(abstEntity.getTimestampMutiert());
		jaxDTOToConvertTo.setId(checkNotNull(new JaxId(abstEntity.getId())));
		return jaxDTOToConvertTo;
	}

	@Nonnull
	private <T extends AbstractEntity> T convertAbstractFieldsToEntity(JaxAbstractDTO jaxToConvert, @Nonnull final T abstEntityToConvertTo) {
		if (jaxToConvert.getId() != null) {
			abstEntityToConvertTo.setId(toEntityId(jaxToConvert));
		}

		return abstEntityToConvertTo;
	}

	@Nonnull
	public JaxApplicationProperties applicationPropertieToJAX(@Nonnull final ApplicationProperty applicationProperty) {
		JaxApplicationProperties jaxProperty = new JaxApplicationProperties();
		convertAbstractFieldsToJAX(applicationProperty, jaxProperty);
		jaxProperty.setName(applicationProperty.getName());
		jaxProperty.setValue(applicationProperty.getValue());
		return jaxProperty;
	}

	@Nonnull
	public ApplicationProperty applicationPropertieToEntity(JaxApplicationProperties jaxAP, @Nonnull final ApplicationProperty applicationProperty) {
		Validate.notNull(applicationProperty);
		Validate.notNull(jaxAP);
		convertAbstractFieldsToEntity(jaxAP, applicationProperty);
		applicationProperty.setName(jaxAP.getName());
		applicationProperty.setValue(jaxAP.getValue());

		return applicationProperty;
	}

	@Nonnull
	public Adresse adresseToEntity(@Nonnull JaxAdresse jaxAdresse, @Nonnull final Adresse adresse) {
		Validate.notNull(adresse);
		Validate.notNull(jaxAdresse);
		convertAbstractFieldsToEntity(jaxAdresse, adresse);
		adresse.setStrasse(jaxAdresse.getStrasse());
		adresse.setHausnummer(jaxAdresse.getHausnummer());
		adresse.setPlz(jaxAdresse.getPlz());
		adresse.setOrt(jaxAdresse.getOrt());
		adresse.setGemeinde(jaxAdresse.getGemeinde());
		adresse.setGueltigAb(jaxAdresse.getGueltigAb() == null ? LocalDate.now() : jaxAdresse.getGueltigAb());
		adresse.setGueltigBis(jaxAdresse.getGueltigBis() == null ? Constants.END_OF_TIME : jaxAdresse.getGueltigBis());
		adresse.setAdresseTyp(jaxAdresse.getAdresseTyp());

		return adresse;
	}

	@Nonnull
	public JaxAdresse adresseToJAX(@Nonnull final Adresse adresse) {
		JaxAdresse jaxAdresse = new JaxAdresse();
		convertAbstractFieldsToJAX(adresse, jaxAdresse);
		jaxAdresse.setStrasse(adresse.getStrasse());
		jaxAdresse.setHausnummer(adresse.getHausnummer());
		jaxAdresse.setPlz(adresse.getPlz());
		jaxAdresse.setOrt(adresse.getOrt());
		jaxAdresse.setGemeinde(adresse.getGemeinde());
		jaxAdresse.setGueltigAb(adresse.getGueltigAb());
		jaxAdresse.setGueltigBis(adresse.getGueltigBis());
		jaxAdresse.setAdresseTyp(adresse.getAdresseTyp());
		return jaxAdresse;
	}

	@Nonnull
	public JaxEnversRevision enversRevisionToJAX(@Nonnull final DefaultRevisionEntity revisionEntity,
												 @Nonnull final AbstractEntity abstractEntity, RevisionType accessType) {

		JaxEnversRevision jaxEnversRevision = new JaxEnversRevision();
		if (abstractEntity instanceof ApplicationProperty) {
			jaxEnversRevision.setEntity(applicationPropertieToJAX((ApplicationProperty) abstractEntity));
		}
		jaxEnversRevision.setRev(revisionEntity.getId());
		jaxEnversRevision.setRevTimeStamp(DateConvertUtils.asLocalDateTime(revisionEntity.getRevisionDate()));
		jaxEnversRevision.setAccessType(accessType);
		return jaxEnversRevision;
	}

	public Person personToEntity(@Nonnull JaxPerson personJAXP, @Nonnull Person person) {
		Validate.notNull(person);
		Validate.notNull(personJAXP);
		convertAbstractFieldsToEntity(personJAXP, person);
		person.setNachname(personJAXP.getNachname());
		person.setVorname(personJAXP.getVorname());
		person.setGeburtsdatum(personJAXP.getGeburtsdatum());
		person.setGeschlecht(personJAXP.getGeschlecht());
		person.setMail(personJAXP.getMail());
		person.setTelefon(personJAXP.getTelefon());
		person.setMobile(personJAXP.getMobile());
		person.setTelefonAusland(personJAXP.getTelefonAusland());
		person.setZpvNumber(personJAXP.getZpvNumber());

		return person;
	}

	public JaxPerson personToJAX(@Nonnull Person persistedPerson) {
		JaxPerson jaxPerson = new JaxPerson();
		convertAbstractFieldsToJAX(persistedPerson, jaxPerson);
		jaxPerson.setNachname(persistedPerson.getNachname());
		jaxPerson.setVorname(persistedPerson.getVorname());
		jaxPerson.setGeburtsdatum(persistedPerson.getGeburtsdatum());
		jaxPerson.setGeschlecht(persistedPerson.getGeschlecht());
		jaxPerson.setMail(persistedPerson.getMail());
		jaxPerson.setTelefon(persistedPerson.getTelefon());
		jaxPerson.setMobile(persistedPerson.getMobile());
		jaxPerson.setTelefonAusland(persistedPerson.getTelefonAusland());
		jaxPerson.setZpvNumber(persistedPerson.getZpvNumber());
		return jaxPerson;
	}
}
