package ch.dvbern.ebegu.api.converter;

import ch.dvbern.ebegu.api.dtos.*;
import ch.dvbern.ebegu.entities.*;
import ch.dvbern.ebegu.enums.ErrorCodeEnum;
import ch.dvbern.ebegu.errors.EbeguEntityNotFoundException;
import ch.dvbern.ebegu.services.*;
import ch.dvbern.ebegu.types.DateRange;
import ch.dvbern.ebegu.util.Constants;
import ch.dvbern.lib.beanvalidation.embeddables.IBAN;
import ch.dvbern.lib.date.DateConvertUtils;
import org.apache.commons.lang3.Validate;
import org.hibernate.envers.DefaultRevisionEntity;
import org.hibernate.envers.RevisionType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import java.time.LocalDate;
import java.util.Objects;
import java.util.Optional;

import static com.google.common.base.Preconditions.checkNotNull;


@Dependent
@SuppressWarnings({"PMD.NcssTypeCount", "unused"})
public class JaxBConverter {

	@Inject
	private GesuchstellerService gesuchstellerService;
	@Inject
	private AdresseService adresseService;
	@Inject
	private FinanzielleSituationService finanzielleSituationService;
	@Inject
	private FallService fallService;
	@Inject
	private MandantService mandantService;
	@Inject
	private TraegerschaftService traegerschaftService;
	@Inject
	private InstitutionService institutionService;

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
	public JaxId toJaxId(@Nonnull final AbstractEntity entity) {
		return new JaxId(entity.getId());
	}

	@Nonnull
	public JaxId toJaxId(@Nonnull final JaxAbstractDTO entity) {
		return new JaxId(entity.getId());
	}

	@Nonnull
	private <T extends JaxAbstractDTO> T convertAbstractFieldsToJAX(@Nonnull final AbstractEntity abstEntity, T jaxDTOToConvertTo) {
		jaxDTOToConvertTo.setTimestampErstellt(abstEntity.getTimestampErstellt());
		jaxDTOToConvertTo.setTimestampMutiert(abstEntity.getTimestampMutiert());
		jaxDTOToConvertTo.setId(checkNotNull(abstEntity.getId()));
		return jaxDTOToConvertTo;
	}

	@Nonnull
	private <T extends AbstractEntity> T convertAbstractFieldsToEntity(JaxAbstractDTO jaxToConvert, @Nonnull final T abstEntityToConvertTo) {
		if (jaxToConvert.getId() != null) {
			abstEntityToConvertTo.setId(jaxToConvert.getId());
			//ACHTUNG hier timestamp erstellt und mutiert NICHT  konvertieren da diese immer auf dem server gesetzt werden muessen
		}

		return abstEntityToConvertTo;
	}

	@Nonnull
	public JaxApplicationProperties applicationPropertyToJAX(@Nonnull final ApplicationProperty applicationProperty) {
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
	public PersonenAdresse adresseToEntity(@Nonnull JaxAdresse jaxAdresse, @Nonnull final PersonenAdresse personenAdresse) {
		Validate.notNull(personenAdresse);
		Validate.notNull(jaxAdresse);
		convertAbstractFieldsToEntity(jaxAdresse, personenAdresse);
		personenAdresse.setStrasse(jaxAdresse.getStrasse());
		personenAdresse.setHausnummer(jaxAdresse.getHausnummer());
		personenAdresse.setZusatzzeile(jaxAdresse.getZusatzzeile());
		personenAdresse.setPlz(jaxAdresse.getPlz());
		personenAdresse.setOrt(jaxAdresse.getOrt());
		personenAdresse.setGemeinde(jaxAdresse.getGemeinde());
		personenAdresse.setLand(jaxAdresse.getLand());
		personenAdresse.setGueltigkeit(convertDateRange(jaxAdresse));
		//adresse gilt per default von start of time an
		personenAdresse.getGueltigkeit().setGueltigAb(jaxAdresse.getGueltigAb() == null ? Constants.START_OF_TIME : jaxAdresse.getGueltigAb());
		personenAdresse.setAdresseTyp(jaxAdresse.getAdresseTyp());

		return personenAdresse;
	}

	/**
	 * Checks fields gueltigAb and gueltigBis from given object and returns the corresponding DateRange object
	 * If gueltigAb is null then current date is set instead
	 * If gueltigBis is null then end_of_time is set instead
	 *
	 * @param jaxAbstractDateRangedDTO JaxObject extending abstract class JaxAbstractDateRangedDTO
	 * @return DateRange object created with the given data
	 */
	private DateRange convertDateRange(JaxAbstractDateRangedDTO jaxAbstractDateRangedDTO) {
		LocalDate dateAb = jaxAbstractDateRangedDTO.getGueltigAb() == null ? LocalDate.now() : jaxAbstractDateRangedDTO.getGueltigAb();
		LocalDate dateBis = jaxAbstractDateRangedDTO.getGueltigBis() == null ? Constants.END_OF_TIME : jaxAbstractDateRangedDTO.getGueltigBis();
		return new DateRange(dateAb, dateBis);
	}

	@Nonnull
	public JaxAdresse adresseToJAX(@Nonnull final PersonenAdresse personenAdresse) {
		JaxAdresse jaxAdresse = new JaxAdresse();
		convertAbstractFieldsToJAX(personenAdresse, jaxAdresse);
		jaxAdresse.setStrasse(personenAdresse.getStrasse());
		jaxAdresse.setHausnummer(personenAdresse.getHausnummer());
		jaxAdresse.setZusatzzeile(personenAdresse.getZusatzzeile());
		jaxAdresse.setPlz(personenAdresse.getPlz());
		jaxAdresse.setOrt(personenAdresse.getOrt());
		jaxAdresse.setGemeinde(personenAdresse.getGemeinde());
		jaxAdresse.setLand(personenAdresse.getLand());
		jaxAdresse.setGueltigAb(personenAdresse.getGueltigkeit().getGueltigAb());
		jaxAdresse.setGueltigBis(personenAdresse.getGueltigkeit().getGueltigBis());
		jaxAdresse.setAdresseTyp(personenAdresse.getAdresseTyp());
		return jaxAdresse;
	}

	@Nonnull
	public JaxEnversRevision enversRevisionToJAX(@Nonnull final DefaultRevisionEntity revisionEntity,
												 @Nonnull final AbstractEntity abstractEntity, RevisionType accessType) {

		JaxEnversRevision jaxEnversRevision = new JaxEnversRevision();
		if (abstractEntity instanceof ApplicationProperty) {
			jaxEnversRevision.setEntity(applicationPropertyToJAX((ApplicationProperty) abstractEntity));
		}
		jaxEnversRevision.setRev(revisionEntity.getId());
		jaxEnversRevision.setRevTimeStamp(DateConvertUtils.asLocalDateTime(revisionEntity.getRevisionDate()));
		jaxEnversRevision.setAccessType(accessType);
		return jaxEnversRevision;
	}

	public Gesuchsteller gesuchstellerToEntity(@Nonnull JaxGesuchsteller gesuchstellerJAXP, @Nonnull Gesuchsteller gesuchsteller) {
		Validate.notNull(gesuchsteller);
		Validate.notNull(gesuchstellerJAXP);
		Validate.notNull(gesuchstellerJAXP.getWohnAdresse(),"Wohnadresse muss gesetzt sein");
		convertAbstractFieldsToEntity(gesuchstellerJAXP, gesuchsteller);
		gesuchsteller.setNachname(gesuchstellerJAXP.getNachname());
		gesuchsteller.setVorname(gesuchstellerJAXP.getVorname());
		gesuchsteller.setGeburtsdatum(gesuchstellerJAXP.getGeburtsdatum());
		gesuchsteller.setGeschlecht(gesuchstellerJAXP.getGeschlecht());
		gesuchsteller.setMail(gesuchstellerJAXP.getMail());
		gesuchsteller.setTelefon(gesuchstellerJAXP.getTelefon());
		gesuchsteller.setMobile(gesuchstellerJAXP.getMobile());
		gesuchsteller.setTelefonAusland(gesuchstellerJAXP.getTelefonAusland());
		gesuchsteller.setZpvNumber(gesuchstellerJAXP.getZpvNumber());

		//Relationen
		//Wir fuehren derzeit immer maximal  eine alternative Korrespondenzadressse -> diese updaten wenn vorhanden
		if (gesuchstellerJAXP.getAlternativeAdresse() != null) {
			PersonenAdresse currentAltAdr = adresseService.getKorrespondenzAdr(gesuchsteller.getId()).orElse(new PersonenAdresse());
			PersonenAdresse altAddrToMerge = adresseToEntity(gesuchstellerJAXP.getAlternativeAdresse(), currentAltAdr);
			gesuchsteller.addAdresse(altAddrToMerge);
		}
		// Umzug und Wohnadresse
		PersonenAdresse umzugAddr = null;
		if (gesuchstellerJAXP.getUmzugAdresse() != null) {
			umzugAddr = toStoreableAddresse(gesuchstellerJAXP.getUmzugAdresse());
			gesuchsteller.addAdresse(umzugAddr);
		}
		//Wohnadresse (abh von Umzug noch datum setzten)
		PersonenAdresse wohnAddrToMerge = toStoreableAddresse(gesuchstellerJAXP.getWohnAdresse());
		if (umzugAddr != null) {
			wohnAddrToMerge.getGueltigkeit().endOnDayBefore(umzugAddr.getGueltigkeit());
		}
		// Finanzielle Situation
		gesuchsteller.addAdresse(wohnAddrToMerge);
		if (gesuchstellerJAXP.getFinanzielleSituationContainer() != null) {
			gesuchsteller.setFinanzielleSituationContainer(finanzielleSituationContainerToStorableEntity(gesuchstellerJAXP.getFinanzielleSituationContainer()));
		}
		return gesuchsteller;
	}

	@Nonnull
	private PersonenAdresse toStoreableAddresse(@Nonnull JaxAdresse adresseToPrepareForSaving) {
		PersonenAdresse adrToMergeWith = new PersonenAdresse();
		if (adresseToPrepareForSaving.getId() != null) {

			Optional<PersonenAdresse> altAdr = adresseService.findAdresse(adresseToPrepareForSaving.getId());
			//wenn schon vorhanden updaten
			if (altAdr.isPresent()) {
				adrToMergeWith = altAdr.get();
			}
		}
		return adresseToEntity(adresseToPrepareForSaving, adrToMergeWith);
	}

	public JaxGesuchsteller gesuchstellerToJAX(@Nonnull Gesuchsteller persistedGesuchsteller) {
		Validate.isTrue(!persistedGesuchsteller.isNew(), "Gesuchsteller kann nicht nach REST transformiert werden weil sie noch " +
			"nicht persistiert wurde; Grund dafuer ist, dass wir die aktuelle Wohnadresse aus der Datenbank lesen wollen");
		JaxGesuchsteller jaxGesuchsteller = new JaxGesuchsteller();
		convertAbstractFieldsToJAX(persistedGesuchsteller, jaxGesuchsteller);
		jaxGesuchsteller.setNachname(persistedGesuchsteller.getNachname());
		jaxGesuchsteller.setVorname(persistedGesuchsteller.getVorname());
		jaxGesuchsteller.setGeburtsdatum(persistedGesuchsteller.getGeburtsdatum());
		jaxGesuchsteller.setGeschlecht(persistedGesuchsteller.getGeschlecht());
		jaxGesuchsteller.setMail(persistedGesuchsteller.getMail());
		jaxGesuchsteller.setTelefon(persistedGesuchsteller.getTelefon());
		jaxGesuchsteller.setMobile(persistedGesuchsteller.getMobile());
		jaxGesuchsteller.setTelefonAusland(persistedGesuchsteller.getTelefonAusland());
		jaxGesuchsteller.setZpvNumber(persistedGesuchsteller.getZpvNumber());
		//relationen laden
		Optional<PersonenAdresse> altAdr = adresseService.getKorrespondenzAdr(persistedGesuchsteller.getId());
		altAdr.ifPresent(adresse -> jaxGesuchsteller.setAlternativeAdresse(adresseToJAX(adresse)));
		PersonenAdresse currentWohnadr = adresseService.getCurrentWohnadresse(persistedGesuchsteller.getId());
		jaxGesuchsteller.setWohnAdresse(adresseToJAX(currentWohnadr));

		//wenn heute gueltige Adresse von der Adresse divergiert die bis End of Time gilt dann wurde ein Umzug angegeben
		Optional<PersonenAdresse> maybeUmzugadresse = adresseService.getNewestWohnadresse(persistedGesuchsteller.getId());
		maybeUmzugadresse.filter(umzugAdresse -> !currentWohnadr.equals(umzugAdresse))
			.ifPresent(umzugAdr -> jaxGesuchsteller.setUmzugAdresse(adresseToJAX(umzugAdr)));
		// Finanzielle Situation
		Optional<FinanzielleSituationContainer> finanzielleSituationForGesuchsteller = finanzielleSituationService.findFinanzielleSituationForGesuchsteller(persistedGesuchsteller);
		if (finanzielleSituationForGesuchsteller.isPresent()) {
			JaxFinanzielleSituationContainer jaxFinanzielleSituationContainer = finanzielleSituationContainerToJAX(finanzielleSituationForGesuchsteller.get());
			jaxGesuchsteller.setFinanzielleSituationContainer(jaxFinanzielleSituationContainer);
		}
		return jaxGesuchsteller;
	}

	public Familiensituation familiensituationToEntity(@Nonnull JaxFamilienSituation familiensituationJAXP, @Nonnull Familiensituation familiensituation) {
		Validate.notNull(familiensituation);
		Validate.notNull(familiensituationJAXP);
		convertAbstractFieldsToEntity(familiensituationJAXP, familiensituation);
		familiensituation.setFamilienstatus(familiensituationJAXP.getFamilienstatus());
		familiensituation.setGesuchstellerKardinalitaet(familiensituationJAXP.getGesuchstellerKardinalitaet());
		familiensituation.setBemerkungen(familiensituationJAXP.getBemerkungen());
		familiensituation.setGesuch(this.gesuchToEntity(familiensituationJAXP.getGesuch(), new Gesuch())); //todo imanol sollte Gesuch nicht aus der DB geholt werden?
		return familiensituation;
	}

	public JaxFamilienSituation familiensituationToJAX(@Nonnull Familiensituation persistedFamiliensituation) {
		JaxFamilienSituation jaxFamiliensituation = new JaxFamilienSituation();
		convertAbstractFieldsToJAX(persistedFamiliensituation, jaxFamiliensituation);
		jaxFamiliensituation.setFamilienstatus(persistedFamiliensituation.getFamilienstatus());
		jaxFamiliensituation.setGesuchstellerKardinalitaet(persistedFamiliensituation.getGesuchstellerKardinalitaet());
		jaxFamiliensituation.setBemerkungen(persistedFamiliensituation.getBemerkungen());
		jaxFamiliensituation.setGesuch(this.gesuchToJAX(persistedFamiliensituation.getGesuch()));
		return jaxFamiliensituation;
	}

	public Fall fallToEntity(@Nonnull JaxFall fallJAXP, @Nonnull Fall fall) {
		Validate.notNull(fall);
		Validate.notNull(fallJAXP);
		convertAbstractFieldsToEntity(fallJAXP, fall);
		return fall;
	}

	public JaxFall fallToJAX(@Nonnull Fall persistedFall) {
		JaxFall jaxFall = new JaxFall();
		convertAbstractFieldsToJAX(persistedFall, jaxFall);
		return jaxFall;
	}

	public Gesuch gesuchToEntity(@Nonnull JaxGesuch gesuchJAXP, @Nonnull Gesuch gesuch) {
		Validate.notNull(gesuch);
		Validate.notNull(gesuchJAXP);
		convertAbstractFieldsToEntity(gesuchJAXP, gesuch);

		Optional<Fall> fallFromDB = fallService.findFall(gesuchJAXP.getFall().getId());
		if (fallFromDB.isPresent()) {
			gesuch.setFall(this.fallToEntity(gesuchJAXP.getFall(), fallFromDB.get()));
		} else {
			throw new EbeguEntityNotFoundException("gesuchToEntity", ErrorCodeEnum.ERROR_ENTITY_NOT_FOUND, gesuchJAXP.getFall());
		}
		if (gesuchJAXP.getGesuchsteller1() != null && gesuchJAXP.getGesuchsteller1().getId() != null) {
			Optional<Gesuchsteller> gesuchsteller1 = gesuchstellerService.findGesuchsteller(gesuchJAXP.getGesuchsteller1().getId());
			if (gesuchsteller1.isPresent()) {
				gesuch.setGesuchsteller1(gesuchstellerToEntity(gesuchJAXP.getGesuchsteller1(), gesuchsteller1.get()));
			} else {
				throw new EbeguEntityNotFoundException("gesuchToEntity", ErrorCodeEnum.ERROR_ENTITY_NOT_FOUND, gesuchJAXP.getGesuchsteller1());
			}
		}
		if (gesuchJAXP.getGesuchsteller2() != null && gesuchJAXP.getGesuchsteller2().getId() != null) {
			Optional<Gesuchsteller> gesuchsteller2 = gesuchstellerService.findGesuchsteller(gesuchJAXP.getGesuchsteller2().getId());
			if (gesuchsteller2.isPresent()){
				gesuch.setGesuchsteller2(gesuchstellerToEntity(gesuchJAXP.getGesuchsteller2(), gesuchsteller2.get()));
			} else {
				throw new EbeguEntityNotFoundException("gesuchToEntity", ErrorCodeEnum.ERROR_ENTITY_NOT_FOUND, gesuchJAXP.getGesuchsteller2().getId());
			}
		}
		return gesuch;
	}

	public JaxGesuch gesuchToJAX(@Nonnull Gesuch persistedGesuch) {
		JaxGesuch jaxGesuch = new JaxGesuch();
		convertAbstractFieldsToJAX(persistedGesuch, jaxGesuch);
		jaxGesuch.setFall(this.fallToJAX(persistedGesuch.getFall()));
		if(persistedGesuch.getGesuchsteller1() != null) {
			jaxGesuch.setGesuchsteller1(this.gesuchstellerToJAX(persistedGesuch.getGesuchsteller1()));
		}
		if(persistedGesuch.getGesuchsteller2() != null) {
			jaxGesuch.setGesuchsteller2(this.gesuchstellerToJAX(persistedGesuch.getGesuchsteller2()));
		}
		return jaxGesuch;
	}

	public JaxMandant mandantToJAX(@Nonnull Mandant persistedMandant) {
		JaxMandant jaxMandant = new JaxMandant();
		convertAbstractFieldsToJAX(persistedMandant, jaxMandant);
		jaxMandant.setName(persistedMandant.getName());
		return jaxMandant;
	}

	public JaxTraegerschaft traegerschaftToJAX(Traegerschaft persistedTraegerschaft) {
		JaxTraegerschaft jaxTraegerschaft = new JaxTraegerschaft();
		convertAbstractFieldsToJAX(persistedTraegerschaft, jaxTraegerschaft);
		jaxTraegerschaft.setName(persistedTraegerschaft.getName());
		return jaxTraegerschaft;
	}

	public Mandant mandantToEntity(JaxMandant mandantJAXP, Mandant mandant) {
		Validate.notNull(mandant);
		Validate.notNull(mandantJAXP);
		convertAbstractFieldsToEntity(mandantJAXP, mandant);
		mandant.setName(mandantJAXP.getName());
		return mandant;
	}

	public Traegerschaft traegerschaftToEntity(@Nonnull JaxTraegerschaft traegerschaftJAXP, @Nonnull Traegerschaft traegerschaft) {
		Validate.notNull(traegerschaft);
		Validate.notNull(traegerschaftJAXP);
		convertAbstractFieldsToEntity(traegerschaftJAXP, traegerschaft);
		traegerschaft.setName(traegerschaftJAXP.getName());
		return traegerschaft;
	}

	public Fachstelle fachstelleToEntity(JaxFachstelle fachstelleJAXP, Fachstelle fachstelle) {
		Validate.notNull(fachstelleJAXP);
		Validate.notNull(fachstelle);
		convertAbstractFieldsToEntity(fachstelleJAXP, fachstelle);
		fachstelle.setName(fachstelleJAXP.getName());
		fachstelle.setBeschreibung(fachstelleJAXP.getBeschreibung());
		fachstelle.setBehinderungsbestaetigung(fachstelleJAXP.isBehinderungsbestaetigung());
		return fachstelle;
	}

	public JaxFachstelle fachstelleToJAX(@Nonnull Fachstelle persistedFachstelle) {
		JaxFachstelle jaxFachstelle = new JaxFachstelle();
		convertAbstractFieldsToJAX(persistedFachstelle, jaxFachstelle);
		jaxFachstelle.setName(persistedFachstelle.getName());
		jaxFachstelle.setBeschreibung(persistedFachstelle.getBeschreibung());
		jaxFachstelle.setBehinderungsbestaetigung(persistedFachstelle.isBehinderungsbestaetigung());
		return jaxFachstelle;
	}


	public JaxInstitution institutionToJAX(Institution persistedInstitution) {
		JaxInstitution jaxInstitution = new JaxInstitution();
		convertAbstractFieldsToJAX(persistedInstitution, jaxInstitution);
		jaxInstitution.setName(persistedInstitution.getName());
		jaxInstitution.setMandant(mandantToJAX(persistedInstitution.getMandant()));
		jaxInstitution.setTraegerschaft(traegerschaftToJAX(persistedInstitution.getTraegerschaft()));
		return jaxInstitution;
	}

	public Institution institutionToEntity(JaxInstitution institutionJAXP, Institution institution) {
		Validate.notNull(institutionJAXP);
		Validate.notNull(institution);
		convertAbstractFieldsToEntity(institutionJAXP, institution);
		institution.setName(institutionJAXP.getName());

		if (institutionJAXP.getMandant().getId() != null) {
			Optional<Mandant> mandantFromDB = mandantService.findMandant(institutionJAXP.getMandant().getId());
			if (mandantFromDB.isPresent()) {
				institution.setMandant(mandantToEntity(institutionJAXP.getMandant(), mandantFromDB.get()));
			} else {
				throw new EbeguEntityNotFoundException("institutionToEntity -> mandant", ErrorCodeEnum.ERROR_ENTITY_NOT_FOUND, institutionJAXP.getMandant().getId());
			}

		} else {
			//todo homa ebegu 82 review wie reagieren wir hier
			throw new EbeguEntityNotFoundException("institutionToEntity -> mandant", ErrorCodeEnum.ERROR_ENTITY_NOT_FOUND);
//			institution.setMandant(mandantToEntity(institutionJAXP.getMandant(), new Mandant()));
		}

		if (institutionJAXP.getTraegerschaft().getId() != null) {
			Optional<Traegerschaft> traegerschaftFromDB = traegerschaftService.findTraegerschaft(institutionJAXP.getTraegerschaft().getId());
			if (traegerschaftFromDB.isPresent()) {
				institution.setTraegerschaft(traegerschaftToEntity(institutionJAXP.getTraegerschaft(), traegerschaftFromDB.get()));
			} else {
				throw new EbeguEntityNotFoundException("institutionToEntity -> traegerschaft", ErrorCodeEnum.ERROR_ENTITY_NOT_FOUND, institutionJAXP.getTraegerschaft().getId());
			}
		} else {
			//todo homa ebegu 82 review wie reagieren wir hier
			throw new EbeguEntityNotFoundException("institutionToEntity -> traegerschaft", ErrorCodeEnum.ERROR_ENTITY_NOT_FOUND);
//			institution.setTraegerschaft(traegerschaftToEntity(institutionJAXP.getTraegerschaft(), new Traegerschaft()));
		}
		return institution;
	}

	public JaxInstitutionStammdaten institutionStammdatenToJAX(InstitutionStammdaten persistedInstStammdaten) {
		JaxInstitutionStammdaten jaxInstStammdaten = new JaxInstitutionStammdaten();
		convertAbstractFieldsToJAX(persistedInstStammdaten, jaxInstStammdaten);
		jaxInstStammdaten.setOeffnungstage(persistedInstStammdaten.getOeffnungstage());
		jaxInstStammdaten.setOeffnungsstunden(persistedInstStammdaten.getOeffnungsstunden());
		jaxInstStammdaten.setIban(persistedInstStammdaten.getIban().getIban());
		jaxInstStammdaten.setBetreuungsangebotTyp(persistedInstStammdaten.getBetreuungsangebotTyp());
		jaxInstStammdaten.setGueltigAb(persistedInstStammdaten.getGueltigkeit().getGueltigAb());
		jaxInstStammdaten.setGueltigBis(persistedInstStammdaten.getGueltigkeit().getGueltigBis());
		jaxInstStammdaten.setInstitution(institutionToJAX(persistedInstStammdaten.getInstitution()));
		return jaxInstStammdaten;
	}

	public InstitutionStammdaten institutionStammdatenToEntity(JaxInstitutionStammdaten institutionStammdatenJAXP, InstitutionStammdaten institutionStammdaten) {
		Validate.notNull(institutionStammdatenJAXP);
		Validate.notNull(institutionStammdaten);

		convertAbstractFieldsToEntity(institutionStammdatenJAXP, institutionStammdaten);

		institutionStammdaten.setOeffnungstage(institutionStammdatenJAXP.getOeffnungstage());
		institutionStammdaten.setOeffnungsstunden(institutionStammdatenJAXP.getOeffnungsstunden());
		institutionStammdaten.setIban(new IBAN(institutionStammdatenJAXP.getIban()));
		institutionStammdaten.setBetreuungsangebotTyp(institutionStammdatenJAXP.getBetreuungsangebotTyp());
		institutionStammdaten.setGueltigkeit(convertDateRange(institutionStammdatenJAXP));

		Optional<Institution> institutionFromDB = institutionService.findInstitution(institutionStammdatenJAXP.getInstitution().getId());
		if (institutionFromDB.isPresent()) {
			institutionStammdaten.setInstitution(institutionToEntity(institutionStammdatenJAXP.getInstitution(), institutionFromDB.get()));
		} else {
			throw new EbeguEntityNotFoundException("institutionStammdatenToEntity", ErrorCodeEnum.ERROR_ENTITY_NOT_FOUND, institutionStammdatenJAXP.getInstitution().getId());
		}

		return institutionStammdaten;

	}

	public FinanzielleSituationContainer finanzielleSituationContainerToStorableEntity(@Nonnull JaxFinanzielleSituationContainer containerJAX) {
		Validate.notNull(containerJAX);
		FinanzielleSituationContainer containerToMergeWith = new FinanzielleSituationContainer();
		if (containerJAX.getId() != null) {
			Optional<FinanzielleSituationContainer> existingFSC = finanzielleSituationService.findFinanzielleSituation(containerJAX.getId());
			if (existingFSC.isPresent()) {
				containerToMergeWith = existingFSC.get();
			}
		}
		FinanzielleSituationContainer mergedContainer = finanzielleSituationContainerToEntity(containerJAX, containerToMergeWith);
		return mergedContainer;
	}

	private FinanzielleSituationContainer finanzielleSituationContainerToEntity(@Nonnull JaxFinanzielleSituationContainer containerJAX,
																			   @Nonnull FinanzielleSituationContainer container) {
		Validate.notNull(container);
		Validate.notNull(containerJAX);
		convertAbstractFieldsToEntity(containerJAX, container);
		container.setJahr(containerJAX.getJahr());
		if (containerJAX.getFinanzielleSituationGS() != null && container.getFinanzielleSituationGS() != null) {
			container.setFinanzielleSituationGS(finanzielleSituationToEntity(containerJAX.getFinanzielleSituationGS(), container.getFinanzielleSituationGS()));
		}
		if (containerJAX.getFinanzielleSituationJA() != null && container.getFinanzielleSituationJA() != null) {
			container.setFinanzielleSituationJA(finanzielleSituationToEntity(containerJAX.getFinanzielleSituationJA(), container.getFinanzielleSituationJA()));
		}
		if (containerJAX.getFinanzielleSituationSV() != null && container.getFinanzielleSituationSV() != null) {
			container.setFinanzielleSituationSV(finanzielleSituationToEntity(containerJAX.getFinanzielleSituationSV(), container.getFinanzielleSituationSV()));
		}
		return container;
	}

	public JaxFinanzielleSituationContainer finanzielleSituationContainerToJAX(FinanzielleSituationContainer persistedFinanzielleSituation) {
		JaxFinanzielleSituationContainer jaxPerson = new JaxFinanzielleSituationContainer();
		convertAbstractFieldsToJAX(persistedFinanzielleSituation, jaxPerson);
		jaxPerson.setJahr(persistedFinanzielleSituation.getJahr());
		jaxPerson.setFinanzielleSituationGS(finanzielleSituationToJAX(persistedFinanzielleSituation.getFinanzielleSituationGS()));
		jaxPerson.setFinanzielleSituationJA(finanzielleSituationToJAX(persistedFinanzielleSituation.getFinanzielleSituationJA()));
		jaxPerson.setFinanzielleSituationSV(finanzielleSituationToJAX(persistedFinanzielleSituation.getFinanzielleSituationSV()));
		return jaxPerson;
	}

	private FinanzielleSituation finanzielleSituationToEntity(@Nonnull JaxFinanzielleSituation finanzielleSituationJAXP, @Nonnull FinanzielleSituation finanzielleSituation) {
		Validate.notNull(finanzielleSituation);
		Validate.notNull(finanzielleSituationJAXP);
		convertAbstractFieldsToEntity(finanzielleSituationJAXP, finanzielleSituation);
		finanzielleSituation.setSteuerveranlagungErhalten(finanzielleSituationJAXP.getSteuerveranlagungErhalten());
		finanzielleSituation.setSteuererklaerungAusgefuellt(finanzielleSituationJAXP.getSteuererklaerungAusgefuellt());
		finanzielleSituation.setNettolohn(finanzielleSituationJAXP.getNettolohn());
		finanzielleSituation.setFamilienzulage(finanzielleSituationJAXP.getFamilienzulage());
		finanzielleSituation.setErsatzeinkommen(finanzielleSituationJAXP.getErsatzeinkommen());
		finanzielleSituation.setErhalteneAlimente(finanzielleSituationJAXP.getErhalteneAlimente());
		finanzielleSituation.setBruttovermoegen(finanzielleSituationJAXP.getBruttovermoegen());
		finanzielleSituation.setSchulden(finanzielleSituationJAXP.getSchulden());
		finanzielleSituation.setSelbstaendig(finanzielleSituationJAXP.getSelbstaendig());
		finanzielleSituation.setGeschaeftsgewinnBasisjahrMinus2(finanzielleSituationJAXP.getGeschaeftsgewinnBasisjahrMinus2());
		finanzielleSituation.setGeschaeftsgewinnBasisjahrMinus1(finanzielleSituationJAXP.getGeschaeftsgewinnBasisjahrMinus1());
		finanzielleSituation.setGeschaeftsgewinnBasisjahr(finanzielleSituationJAXP.getGeschaeftsgewinnBasisjahr());
		finanzielleSituation.setGeleisteteAlimente(finanzielleSituationJAXP.getGeleisteteAlimente());
		return finanzielleSituation;
	}

	private JaxFinanzielleSituation finanzielleSituationToJAX(@Nullable FinanzielleSituation persistedFinanzielleSituation) {
		if (persistedFinanzielleSituation != null) {
			JaxFinanzielleSituation jaxPerson = new JaxFinanzielleSituation();
			convertAbstractFieldsToJAX(persistedFinanzielleSituation, jaxPerson);
			jaxPerson.setSteuerveranlagungErhalten(persistedFinanzielleSituation.getSteuerveranlagungErhalten());
			jaxPerson.setSteuererklaerungAusgefuellt(persistedFinanzielleSituation.getSteuererklaerungAusgefuellt());
			jaxPerson.setNettolohn(persistedFinanzielleSituation.getNettolohn());
			jaxPerson.setFamilienzulage(persistedFinanzielleSituation.getFamilienzulage());
			jaxPerson.setErsatzeinkommen(persistedFinanzielleSituation.getErsatzeinkommen());
			jaxPerson.setErhalteneAlimente(persistedFinanzielleSituation.getErhalteneAlimente());
			jaxPerson.setBruttovermoegen(persistedFinanzielleSituation.getBruttovermoegen());
			jaxPerson.setSchulden(persistedFinanzielleSituation.getSchulden());
			jaxPerson.setSelbstaendig(persistedFinanzielleSituation.getSelbstaendig());
			jaxPerson.setGeschaeftsgewinnBasisjahrMinus2(persistedFinanzielleSituation.getGeschaeftsgewinnBasisjahrMinus2());
			jaxPerson.setGeschaeftsgewinnBasisjahrMinus1(persistedFinanzielleSituation.getGeschaeftsgewinnBasisjahrMinus1());
			jaxPerson.setGeschaeftsgewinnBasisjahr(persistedFinanzielleSituation.getGeschaeftsgewinnBasisjahr());
			jaxPerson.setGeleisteteAlimente(persistedFinanzielleSituation.getGeleisteteAlimente());
			return jaxPerson;
		}
		return null;
	}
}
