/*
 * Copyright (C) 2021 DV Bern AG, Switzerland
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package ch.dvbern.ebegu.validators;

import java.text.MessageFormat;
import java.util.Objects;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceUnit;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import ch.dvbern.ebegu.entities.Einstellung;
import ch.dvbern.ebegu.entities.Gemeinde;
import ch.dvbern.ebegu.entities.Gesuchsperiode;
import ch.dvbern.ebegu.entities.KindContainer;
import ch.dvbern.ebegu.enums.EinschulungTyp;
import ch.dvbern.ebegu.enums.EinstellungKey;
import ch.dvbern.ebegu.enums.IntegrationTyp;
import ch.dvbern.ebegu.errors.EbeguRuntimeException;
import ch.dvbern.ebegu.i18n.LocaleThreadLocal;
import ch.dvbern.ebegu.services.EinstellungService;
import ch.dvbern.ebegu.util.ServerMessageUtil;
import ch.dvbern.ebegu.util.ValidationMessageUtil;

/**
 *  Fachstellen dürfen nur im Vorschulalter gesetzt werden: Eine soziale oder sprachliche Indikation
 *  nach Artikel 34d Absatz 1 Buchstabe f ASIV liegt vor bei einem Kind, das noch nicht in die Volksschule
 *  eingetreten ist. Dies wird mit diesem Validator überprüft.
 */
public class CheckFachstellenValidator implements ConstraintValidator<CheckFachstellen, KindContainer> {

	@Inject
	private EinstellungService einstellungService;

	// We need to pass to EinstellungService a new EntityManager to avoid errors like ConcurrentModificatinoException. So we create it here
	// and pass it to the methods of EinstellungService we need to call.
	//http://stackoverflow.com/questions/18267269/correct-way-to-do-an-entitymanager-query-during-hibernate-validation
	@PersistenceUnit(unitName = "ebeguPersistenceUnit")
	private EntityManagerFactory entityManagerFactory;

	public CheckFachstellenValidator() {
	}

	public CheckFachstellenValidator(@Nonnull EinstellungService einstellungService) {
		this.einstellungService = einstellungService;
	}

	@Override
	public void initialize(CheckFachstellen constraintAnnotation) {
		// nop
	}

	@Override
	public boolean isValid(@Nonnull KindContainer kindContainer, ConstraintValidatorContext context) {
		if (kindContainer.getKindJA() == null
			|| kindContainer.getKindJA().getPensumFachstelle() == null
			|| kindContainer.getKindJA().getPensumFachstelle().getFachstelle() == null
		) {
			// Kein PensumFachstelle
			return true;
		}
		if (kindContainer.getKindJA().getPensumFachstelle().getIntegrationTyp() == IntegrationTyp.SOZIALE_INTEGRATION) {
			return validateSozialeIndikation(kindContainer, context);
		}
		return validateSprachlicheIndikation(kindContainer, context);
	}

	private boolean validateSozialeIndikation(@Nonnull KindContainer kindContainer, @Nonnull ConstraintValidatorContext context) {
		var gemeinde = kindContainer.getGesuch().extractGemeinde();
		var gesuchsperiode = kindContainer.getGesuch().getGesuchsperiode();
		Einstellung schulstufeEinstellung = findSchulstufeEinstellung(gemeinde, gesuchsperiode);
		var maxEinschulungTyp= convertEinstellungToEinschulungTyp(schulstufeEinstellung);
		Objects.requireNonNull(kindContainer.getKindJA().getEinschulungTyp());
		if (maxEinschulungTyp.ordinal() >= kindContainer.getKindJA().getEinschulungTyp().ordinal()) {
			return true;
		}
		createConstraintViolation("invalid_fachstellen_sozial", maxEinschulungTyp, context);
		return false;
	}

	private Einstellung findSchulstufeEinstellung(@Nonnull Gemeinde gemeinde, @Nonnull Gesuchsperiode gesuchsperiode) {
		var em = createEntityManager();
		Einstellung schulstufeEinstellung = this.einstellungService
			.findEinstellung(EinstellungKey.FKJV_SOZIALE_INTEGRATION_BIS_SCHULSTUFE, gemeinde, gesuchsperiode, em);
		closeEntityManager(em);
		return schulstufeEinstellung;
	}

	private EntityManager createEntityManager() {
		if (entityManagerFactory != null) {
			return entityManagerFactory.createEntityManager(); // creates a new EntityManager
		}
		return null;
	}

	private void closeEntityManager(EntityManager em) {
		if (em != null) {
			em.close();
		}
	}

	private boolean validateSprachlicheIndikation(@Nonnull KindContainer kindContainer, @Nonnull ConstraintValidatorContext context) {
		if (kindContainer.getKindJA().getEinschulungTyp() == EinschulungTyp.VORSCHULALTER) {
			return true;
		}
		createConstraintViolation("invalid_fachstellen_sprachlich", EinschulungTyp.VORSCHULALTER, context);
		return false;
	}

	@Nonnull
	private EinschulungTyp convertEinstellungToEinschulungTyp(Einstellung einstellung) {
		EinschulungTyp einschulungTyp = null;
		for (EinschulungTyp typ : EinschulungTyp.values()) {
			if (typ.name().equals(einstellung.getValue())) {
				einschulungTyp = typ;
			}
		}
		if (einschulungTyp == null) {
			throw new EbeguRuntimeException("convertEinstellungToEinschulungTyp", "einschulungtyp "
				+ einstellung.getValue()
				+ "nicht gefunden");
		}
		return einschulungTyp;
	}

	private void createConstraintViolation(
		@Nonnull String translationKey,
		@Nonnull EinschulungTyp einschulungTyp,
		@Nonnull ConstraintValidatorContext context
	) {
		if (context != null) {
			String message = ValidationMessageUtil.getMessage(translationKey);
			String einschulungTypTranslated = ServerMessageUtil.translateEnumValue(einschulungTyp, LocaleThreadLocal.get());
			message = MessageFormat.format(message, einschulungTypTranslated);

			context.disableDefaultConstraintViolation();
			context.buildConstraintViolationWithTemplate(message)
				.addPropertyNode("pensumFachstelle.pensum")
				.addConstraintViolation();
		}
	}
}
