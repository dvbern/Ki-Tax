package ch.dvbern.ebegu.services;

import ch.dvbern.ebegu.cdi.Dummy;
import ch.dvbern.ebegu.config.EbeguConfiguration;
import ch.dvbern.ebegu.dto.personensuche.EWKPerson;
import ch.dvbern.ebegu.dto.personensuche.EWKResultat;
import ch.dvbern.ebegu.entities.Gesuchsteller;
import ch.dvbern.ebegu.enums.Geschlecht;
import ch.dvbern.ebegu.errors.EbeguRuntimeException;
import ch.dvbern.ebegu.errors.PersonenSucheServiceBusinessException;
import ch.dvbern.ebegu.errors.PersonenSucheServiceException;
import ch.dvbern.ebegu.ws.ewk.IEWKWebService;
import ch.dvbern.lib.cdipersistence.Persistence;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.Validate;

import javax.annotation.Nonnull;
import javax.annotation.PostConstruct;
import javax.annotation.security.RolesAllowed;
import javax.ejb.Local;
import javax.ejb.Stateless;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Default;
import javax.enterprise.inject.Instance;
import javax.enterprise.util.AnnotationLiteral;
import javax.inject.Inject;
import java.time.LocalDate;

import static ch.dvbern.ebegu.enums.UserRoleName.*;

/**
 * Service fuer die Personensuche
 */
@Stateless
@Local(PersonenSucheService.class)
@RolesAllowed({SUPER_ADMIN, ADMIN, SACHBEARBEITER_JA})
public class PersonenSucheServiceBean extends AbstractBaseService implements PersonenSucheService {

	@Inject
	@Any
	//wir entscheiden programmatisch ob wir den dummy brauchen, daher hier mal alle injecten und dann im postconstruct entscheiden
	private Instance<IEWKWebService> serviceInstance;

	private IEWKWebService ewkService;

	@Inject
	private EbeguConfiguration config;

	@Inject
	private Persistence<Gesuchsteller> persistence;


	@SuppressWarnings(value = {"PMD.UnusedPrivateMethod"})
	@SuppressFBWarnings(value = {"SIC_INNER_SHOULD_BE_STATIC_ANON"})
	@PostConstruct
	private void resolveService() {
		if (config.isPersonenSucheDisabled()) {
			ewkService = serviceInstance.select(new AnnotationLiteral<Dummy>() {}).get();
		} else {
			ewkService = serviceInstance.select(new AnnotationLiteral<Default>() {}).get();
		}
	}

	@Override
	@Nonnull
	public EWKResultat suchePerson(@Nonnull Gesuchsteller gesuchsteller) throws PersonenSucheServiceException, PersonenSucheServiceBusinessException {
		Validate.notNull(gesuchsteller, "Gesuchsteller muss gesetzt sein");
		Validate.isTrue(gesuchsteller.isNew(), "Gesuchsteller muss zuerst gespeichert werden!");
		if (StringUtils.isNotEmpty(gesuchsteller.getEwkPersonId())) {
			return suchePerson(gesuchsteller.getEwkPersonId());
		} else {
			return suchePerson(gesuchsteller.getNachname(), gesuchsteller.getGeburtsdatum(), gesuchsteller.getGeschlecht());
		}
	}

	@Override
	@Nonnull
	public Gesuchsteller selectPerson(@Nonnull Gesuchsteller gesuchsteller, @Nonnull EWKPerson ewkPerson) {
		Validate.notNull(gesuchsteller, "Gesuchsteller muss gesetzt sein");
		Validate.notNull(ewkPerson, "ewkPerson muss gesetzt sein");
		Validate.isTrue(gesuchsteller.isNew(), "Gesuchsteller muss zuerst gespeichert werden!");
		if (StringUtils.isNotEmpty(gesuchsteller.getEwkPersonId()) && !gesuchsteller.getEwkPersonId().equals(ewkPerson.getPersonID())) {
			throw new EbeguRuntimeException("selectPerson", "Die Person " + gesuchsteller.getId() + " ist schon mit der Person " + gesuchsteller.getEwkPersonId() + " verknüpft");
		}
		gesuchsteller.setEwkPersonId(ewkPerson.getPersonID());
		return persistence.merge(gesuchsteller);
	}

	@Override
	@Nonnull
	public EWKResultat suchePerson(@Nonnull String id) throws PersonenSucheServiceException, PersonenSucheServiceBusinessException {
		return ewkService.suchePerson(id);
	}

	@Override
	@Nonnull
	public EWKResultat suchePerson(@Nonnull String name, @Nonnull String vorname, @Nonnull LocalDate geburtsdatum, @Nonnull Geschlecht geschlecht) throws PersonenSucheServiceException, PersonenSucheServiceBusinessException {
		return ewkService.suchePerson(name, vorname, geburtsdatum, geschlecht);
	}

	@Override
	@Nonnull
	public EWKResultat suchePerson(@Nonnull String name, @Nonnull LocalDate geburtsdatum, @Nonnull Geschlecht geschlecht) throws PersonenSucheServiceException, PersonenSucheServiceBusinessException {
		return ewkService.suchePerson(name, geburtsdatum, geschlecht);
	}
}
