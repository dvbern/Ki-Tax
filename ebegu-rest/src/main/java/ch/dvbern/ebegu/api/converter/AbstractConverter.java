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

import ch.dvbern.ebegu.api.dtos.*;
import ch.dvbern.ebegu.authentication.PrincipalBean;
import ch.dvbern.ebegu.entities.*;
import ch.dvbern.ebegu.types.DateRange;
import ch.dvbern.ebegu.util.Constants;
import com.google.errorprone.annotations.CanIgnoreReturnValue;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import java.time.LocalDate;
import java.util.Objects;

import static com.google.common.base.Preconditions.checkNotNull;
import static java.util.Objects.requireNonNull;

@RequestScoped
public class AbstractConverter {

	@Inject
	private PrincipalBean principalBean;

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

	protected <T extends JaxAbstractDTO> void convertAbstractFieldsToJAX(
		@Nonnull final AbstractEntity abstEntity,
		final T jaxDTOToConvertTo) {

		jaxDTOToConvertTo.setTimestampErstellt(abstEntity.getTimestampErstellt());
		jaxDTOToConvertTo.setTimestampMutiert(abstEntity.getTimestampMutiert());
		jaxDTOToConvertTo.setId(checkNotNull(abstEntity.getId()));
		jaxDTOToConvertTo.setVersion(abstEntity.getVersion());
	}

	protected <T extends HasMandant> void convertMandantFieldsToEntity(
			@Nonnull final T abstEntityToConvertTo) {
		abstEntityToConvertTo.setMandant(getPrincipalBean().getMandant());
	}

	@Nonnull
	@CanIgnoreReturnValue
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
	@CanIgnoreReturnValue
	protected <T extends JaxAbstractDTO> T convertAbstractVorgaengerFieldsToJAX(
		@Nonnull final AbstractMutableEntity abstEntity,
		final T jaxDTOToConvertTo) {

		convertAbstractFieldsToJAX(abstEntity, jaxDTOToConvertTo);
		jaxDTOToConvertTo.setVorgaengerId(abstEntity.getVorgaengerId());

		return jaxDTOToConvertTo;
	}

	@Nonnull
	@CanIgnoreReturnValue
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
	@CanIgnoreReturnValue
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
		JaxAbstractMahlzeitenPensumDTO jaxPensum,
		AbstractMahlzeitenPensum pensumEntity) {

		convertAbstractDateRangedFieldsToEntity(jaxPensum, pensumEntity);
		pensumEntity.setMonatlicheBetreuungskosten(jaxPensum.getMonatlicheBetreuungskosten());
		pensumEntity.setUnitForDisplay(jaxPensum.getUnitForDisplay());
		pensumEntity.setPensum(jaxPensum.getPensum());
		pensumEntity.setStuendlicheVollkosten(jaxPensum.getStuendlicheVollkosten());

		if (jaxPensum.getEingewoehnungPauschale() != null) {
			EingewoehnungPauschale eingewoehnungPauschale = pensumEntity.getEingewoehnungPauschale() != null ?
				pensumEntity.getEingewoehnungPauschale() : new EingewoehnungPauschale();
			pensumEntity.setEingewoehnungPauschale(
				convertEingewoehnungspauschaleToEntity(jaxPensum.getEingewoehnungPauschale(), eingewoehnungPauschale));
		} else {
			pensumEntity.setEingewoehnungPauschale(null);
		}
	}

	private EingewoehnungPauschale convertEingewoehnungspauschaleToEntity(
		JaxEingewoehnungPauschale jaxEingewoehnungPauschale,
		EingewoehnungPauschale eingewoehnungPauschale) {

		convertAbstractDateRangedFieldsToEntity(jaxEingewoehnungPauschale, eingewoehnungPauschale);
		eingewoehnungPauschale.setPauschale(jaxEingewoehnungPauschale.getPauschale());
		return eingewoehnungPauschale;
	}

	protected void convertAbstractPensumFieldsToJAX(
		final AbstractIntegerPensum pensum,
		final JaxAbstractIntegerPensumDTO jaxPensum) {

		convertAbstractDateRangedFieldsToJAX(pensum, jaxPensum);
		jaxPensum.setPensum(pensum.getPensum());
	}

	protected void convertAbstractPensumFieldsToJAX(
		BetreuungsmitteilungPensum pensum,
		JaxBetreuungsmitteilungPensum jaxPensum) {

		convertAbstractDateRangedFieldsToJAX(pensum, jaxPensum);

		jaxPensum.setPensum(pensum.getPensum());
		jaxPensum.setUnitForDisplay(pensum.getUnitForDisplay());
		jaxPensum.setMonatlicheBetreuungskosten(pensum.getMonatlicheBetreuungskosten());
		jaxPensum.setStuendlicheVollkosten(pensum.getStuendlicheVollkosten());
	}

	protected void convertAbstractPensumFieldsToJAX(
		AbstractMahlzeitenPensum pensum,
		JaxAbstractMahlzeitenPensumDTO jaxPensum) {

		convertAbstractDateRangedFieldsToJAX(pensum, jaxPensum);
		jaxPensum.setPensum(pensum.getPensum());
		jaxPensum.setUnitForDisplay(pensum.getUnitForDisplay());
		jaxPensum.setMonatlicheBetreuungskosten(pensum.getMonatlicheBetreuungskosten());
		jaxPensum.setStuendlicheVollkosten(pensum.getStuendlicheVollkosten());
		if (pensum.getEingewoehnungPauschale() != null) {
			jaxPensum.setEingewoehnungPauschale(
				eingewoehnungPauschaleToJax(pensum.getEingewoehnungPauschale(), new JaxEingewoehnungPauschale()));
		}
	}

	@Nonnull
	protected JaxEingewoehnungPauschale eingewoehnungPauschaleToJax(
		EingewoehnungPauschale eingewoehnungPauschale,
		JaxEingewoehnungPauschale jaxEingewoehnungPauschale) {

		convertAbstractDateRangedFieldsToJAX(eingewoehnungPauschale, jaxEingewoehnungPauschale);
		jaxEingewoehnungPauschale.setPauschale(eingewoehnungPauschale.getPauschale());
		return jaxEingewoehnungPauschale;
	}

	protected JaxFile convertFileToJax(FileMetadata fileMetadata, JaxFile jaxFile) {
		jaxFile.setFilename(fileMetadata.getFilename());
		jaxFile.setFilepfad(fileMetadata.getFilepfad());
		jaxFile.setFilesize(fileMetadata.getFilesize());
		return jaxFile;
	}

	protected FileMetadata convertFileToEnity(JaxFile jaxFile, FileMetadata fileMetadata) {
		requireNonNull(fileMetadata);
		requireNonNull(jaxFile);
		fileMetadata.setFilename(jaxFile.getFilename());
		fileMetadata.setFilepfad(jaxFile.getFilepfad());
		fileMetadata.setFilesize(jaxFile.getFilesize());
		return fileMetadata;
	}

	@Nonnull
	@CanIgnoreReturnValue
	public Adresse adresseToEntity(@Nonnull final JaxAdresse jaxAdresse, @Nonnull final Adresse adresse) {
		requireNonNull(adresse);
		requireNonNull(jaxAdresse);
		convertAbstractDateRangedFieldsToEntity(jaxAdresse, adresse);
		adresse.setStrasse(jaxAdresse.getStrasse());
		adresse.setHausnummer(jaxAdresse.getHausnummer());
		adresse.setZusatzzeile(jaxAdresse.getZusatzzeile());
		adresse.setPlz(jaxAdresse.getPlz());
		adresse.setOrt(jaxAdresse.getOrt());
		// Gemeinde ist read-only und wird nicht gesetzt
		adresse.setLand(jaxAdresse.getLand());
		adresse.setOrganisation(jaxAdresse.getOrganisation());
		//adresse gilt per default von start of time an
		adresse.getGueltigkeit().setGueltigAb(jaxAdresse.getGueltigAb() == null ?
			Constants.START_OF_TIME :
			jaxAdresse.getGueltigAb());

		return adresse;
	}

	@Nonnull
	public JaxAdresse adresseToJAX(@Nonnull final Adresse adresse) {
		final JaxAdresse jaxAdresse = new JaxAdresse();
		convertAbstractDateRangedFieldsToJAX(adresse, jaxAdresse);
		jaxAdresse.setStrasse(adresse.getStrasse());
		jaxAdresse.setHausnummer(adresse.getHausnummer());
		jaxAdresse.setZusatzzeile(adresse.getZusatzzeile());
		jaxAdresse.setPlz(adresse.getPlz());
		jaxAdresse.setOrt(adresse.getOrt());
		jaxAdresse.setGemeinde(adresse.getGemeinde());
		jaxAdresse.setBfsNummer(adresse.getBfsNummer());
		jaxAdresse.setLand(adresse.getLand());
		jaxAdresse.setOrganisation(adresse.getOrganisation());
		return jaxAdresse;
	}

	@Nonnull
	public Gemeinde gemeindeToEntity(@Nonnull final JaxGemeinde jaxGemeinde, @Nonnull final Gemeinde gemeinde) {
		requireNonNull(gemeinde);
		requireNonNull(jaxGemeinde);
		requireNonNull(jaxGemeinde.getBetreuungsgutscheineStartdatum());
		requireNonNull(jaxGemeinde.getTagesschulanmeldungenStartdatum());
		requireNonNull(jaxGemeinde.getFerieninselanmeldungenStartdatum());
		convertAbstractFieldsToEntity(jaxGemeinde, gemeinde);
		convertMandantFieldsToEntity(gemeinde);
		gemeinde.setName(jaxGemeinde.getName());
		gemeinde.setStatus(jaxGemeinde.getStatus());
		gemeinde.setGemeindeNummer(jaxGemeinde.getGemeindeNummer());
		gemeinde.setBfsNummer(jaxGemeinde.getBfsNummer());
		gemeinde.setBetreuungsgutscheineStartdatum(jaxGemeinde.getBetreuungsgutscheineStartdatum());
		gemeinde.setTagesschulanmeldungenStartdatum(jaxGemeinde.getTagesschulanmeldungenStartdatum());
		gemeinde.setFerieninselanmeldungenStartdatum(jaxGemeinde.getFerieninselanmeldungenStartdatum());
		gemeinde.setGueltigBis(jaxGemeinde.getGueltigBis());
		gemeinde.setAngebotBG(jaxGemeinde.isAngebotBG());
		gemeinde.setAngebotBGTFO(jaxGemeinde.isAngebotBGTFO());
		gemeinde.setAngebotTS(jaxGemeinde.isAngebotTS());
		gemeinde.setAngebotFI(jaxGemeinde.isAngebotFI());
		gemeinde.setBesondereVolksschule(jaxGemeinde.isBesondereVolksschule());
		gemeinde.setNurLats(jaxGemeinde.isNurLats());
		gemeinde.setInfomaZahlungen(jaxGemeinde.getInfomaZahlungen());
		return gemeinde;
	}

	public JaxGemeinde gemeindeToJAX(@Nonnull final Gemeinde persistedGemeinde) {
		final JaxGemeinde jaxGemeinde = new JaxGemeinde();
		convertAbstractFieldsToJAX(persistedGemeinde, jaxGemeinde);
		jaxGemeinde.setKey(persistedGemeinde.getId());
		jaxGemeinde.setName(persistedGemeinde.getName());
		jaxGemeinde.setStatus(persistedGemeinde.getStatus());
		jaxGemeinde.setGemeindeNummer(persistedGemeinde.getGemeindeNummer());
		jaxGemeinde.setBfsNummer(persistedGemeinde.getBfsNummer());
		jaxGemeinde.setBetreuungsgutscheineStartdatum(persistedGemeinde.getBetreuungsgutscheineStartdatum());
		jaxGemeinde.setTagesschulanmeldungenStartdatum(persistedGemeinde.getTagesschulanmeldungenStartdatum());
		jaxGemeinde.setFerieninselanmeldungenStartdatum(persistedGemeinde.getFerieninselanmeldungenStartdatum());
		jaxGemeinde.setGueltigBis(persistedGemeinde.getGueltigBis());
		jaxGemeinde.setAngebotBG(persistedGemeinde.isAngebotBG());
		jaxGemeinde.setAngebotBGTFO(persistedGemeinde.isAngebotBGTFO());
		jaxGemeinde.setAngebotTS(persistedGemeinde.isAngebotTS());
		jaxGemeinde.setAngebotFI(persistedGemeinde.isAngebotFI());
		jaxGemeinde.setBesondereVolksschule(persistedGemeinde.isBesondereVolksschule());
		jaxGemeinde.setNurLats(persistedGemeinde.isNurLats());
		jaxGemeinde.setInfomaZahlungen(persistedGemeinde.getInfomaZahlungen());
		return jaxGemeinde;
	}

	@Nonnull
	public JaxBfsGemeinde gemeindeBfsToJax(@Nonnull final BfsGemeinde bfsGemeinde) {
		// Aktuell brauchen wir nur den Namen und die BFS-Nummer
		JaxBfsGemeinde jaxBfsGemeinde = new JaxBfsGemeinde();
		jaxBfsGemeinde.setName(bfsGemeinde.getName());
		jaxBfsGemeinde.setBfsNummer(bfsGemeinde.getBfsNummer());
		return jaxBfsGemeinde;
	}

	@Nonnull
	public JaxGesuchsperiode gesuchsperiodeToJAX(@Nonnull Gesuchsperiode persistedGesuchsperiode) {

		JaxGesuchsperiode jaxGesuchsperiode = new JaxGesuchsperiode();
		convertAbstractDateRangedFieldsToJAX(persistedGesuchsperiode, jaxGesuchsperiode);
		jaxGesuchsperiode.setStatus(persistedGesuchsperiode.getStatus());

		return jaxGesuchsperiode;
	}

	@Nonnull
	public Gesuchsperiode gesuchsperiodeToEntity(
		@Nonnull JaxGesuchsperiode jaxGesuchsperiode,
		@Nonnull Gesuchsperiode gesuchsperiode) {

		convertAbstractDateRangedFieldsToEntity(jaxGesuchsperiode, gesuchsperiode);
		convertMandantFieldsToEntity(gesuchsperiode);
		gesuchsperiode.setStatus(jaxGesuchsperiode.getStatus());

		return gesuchsperiode;
	}

	public TextRessource textRessourceToEntity(
		@Nonnull final JaxTextRessource textRessourceJAX,
		@Nullable TextRessource textRessource) {
		requireNonNull(textRessourceJAX);

		if (textRessource == null) {
			textRessource = new TextRessource();
		}

		convertAbstractFieldsToEntity(textRessourceJAX, textRessource);

		textRessource.setTextDeutsch(textRessourceJAX.getTextDeutsch());
		textRessource.setTextFranzoesisch(textRessourceJAX.getTextFranzoesisch());

		return textRessource;
	}

	public JaxTextRessource textRessourceToJAX(@Nonnull final TextRessource textRessource) {
		Objects.requireNonNull(textRessource);

		final JaxTextRessource jaxTextRessource = new JaxTextRessource();

		convertAbstractFieldsToJAX(textRessource, jaxTextRessource);

		jaxTextRessource.setTextDeutsch(textRessource.getTextDeutsch());
		jaxTextRessource.setTextFranzoesisch(textRessource.getTextFranzoesisch());

		return jaxTextRessource;
	}

	public JaxInstitution institutionToJAX(final Institution persistedInstitution) {
		final JaxInstitution jaxInstitution = new JaxInstitution();
		convertAbstractVorgaengerFieldsToJAX(persistedInstitution, jaxInstitution);
		jaxInstitution.setName(persistedInstitution.getName());
		Objects.requireNonNull(persistedInstitution.getMandant());
		jaxInstitution.setMandant(mandantToJAX(persistedInstitution.getMandant()));
		jaxInstitution.setStatus(persistedInstitution.getStatus());
		jaxInstitution.setStammdatenCheckRequired(persistedInstitution.isStammdatenCheckRequired());
		if (persistedInstitution.getTraegerschaft() != null) {
			jaxInstitution.setTraegerschaft(traegerschaftLightToJAX(persistedInstitution.getTraegerschaft()));
		}
		return jaxInstitution;
	}

	public JaxMandant mandantToJAX(@Nonnull final Mandant persistedMandant) {
		final JaxMandant jaxMandant = new JaxMandant();
		convertAbstractVorgaengerFieldsToJAX(persistedMandant, jaxMandant);
		jaxMandant.setName(persistedMandant.getName());
		jaxMandant.setMandantIdentifier(persistedMandant.getMandantIdentifier());
		return jaxMandant;
	}

	public Mandant mandantToEntity(final JaxMandant mandantJAXP, final Mandant mandant) {
		requireNonNull(mandant);
		requireNonNull(mandantJAXP);
		convertAbstractVorgaengerFieldsToEntity(mandantJAXP, mandant);
		mandant.setName(mandantJAXP.getName());
		return mandant;
	}

	/**
	 * Diese Methode verwenden ausser wenn man der Institution Count und InstitutionNamen benoetigt
	 */
	public JaxTraegerschaft traegerschaftLightToJAX(final Traegerschaft persistedTraegerschaft) {
		final JaxTraegerschaft jaxTraegerschaft = new JaxTraegerschaft();
		convertAbstractVorgaengerFieldsToJAX(persistedTraegerschaft, jaxTraegerschaft);
		jaxTraegerschaft.setName(persistedTraegerschaft.getName());
		jaxTraegerschaft.setActive(persistedTraegerschaft.getActive());
		return jaxTraegerschaft;
	}

	@Nonnull
	public JaxEinstellung einstellungToJAX(@Nonnull final Einstellung einstellung) {
		final JaxEinstellung jaxEinstellung = new JaxEinstellung();
		convertAbstractFieldsToJAX(einstellung, jaxEinstellung);
		jaxEinstellung.setKey(einstellung.getKey());
		jaxEinstellung.setValue(einstellung.getValue());
		jaxEinstellung.setErklaerung(einstellung.getErklaerung());
		jaxEinstellung.setGemeindeId(null == einstellung.getGemeinde() ? null : einstellung.getGemeinde().getId());
		jaxEinstellung.setGesuchsperiodeId(einstellung.getGesuchsperiode().getId());
		// Mandant wird aktuell nicht gemappt
		return jaxEinstellung;
	}

	protected PrincipalBean getPrincipalBean() {
		return principalBean;
	}
}
