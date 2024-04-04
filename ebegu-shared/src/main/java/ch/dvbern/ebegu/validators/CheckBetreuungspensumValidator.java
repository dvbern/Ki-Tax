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

package ch.dvbern.ebegu.validators;

import java.math.BigDecimal;
import java.text.MessageFormat;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceUnit;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import ch.dvbern.ebegu.entities.Betreuung;
import ch.dvbern.ebegu.entities.Betreuungspensum;
import ch.dvbern.ebegu.entities.BetreuungspensumContainer;
import ch.dvbern.ebegu.entities.Gemeinde;
import ch.dvbern.ebegu.entities.Gesuchsperiode;
import ch.dvbern.ebegu.services.EinstellungService;
import ch.dvbern.ebegu.util.BetreuungUtil;
import ch.dvbern.ebegu.util.ValidationMessageUtil;

/**
 * Validator for Betreuungspensen, checks that the entered betreuungspensum is bigger than the minimum
 * that is allowed for the Betreungstyp for a given date
 */
public class CheckBetreuungspensumValidator implements ConstraintValidator<CheckBetreuungspensum, Betreuung> {

	@SuppressWarnings("CdiInjectionPointsInspection")
	@Inject
	private EinstellungService einstellungService;

	// We need to pass to EinstellungService a new EntityManager to avoid errors like ConcurrentModificatinoException.
	// So we create it here and pass it to the methods of EinstellungService we need to call.
	// http://stackoverflow.com/questions/18267269/correct-way-to-do-an-entitymanager-query-during-hibernate-validation
	@PersistenceUnit(unitName = "ebeguPersistenceUnit")
	private EntityManagerFactory entityManagerFactory;

	public CheckBetreuungspensumValidator() {
	}

	/**
	 * Constructor fuer tests damit service reingegeben werden kann
	 *
	 * @param service service zum testen
	 */
	public CheckBetreuungspensumValidator(
		@Nonnull EinstellungService service,
		@Nonnull EntityManagerFactory entityManagerFactory
	) {
		this.einstellungService = service;
		this.entityManagerFactory = entityManagerFactory;
	}

	@Override
	public boolean isValid(@Nonnull Betreuung betreuung, ConstraintValidatorContext context) {

		if (betreuung.getBetreuungsstatus().isSchulamt()) {
			// Keine Betreuungspensen
			return true;
		}

		final EntityManager em = createEntityManager();
		int index = 0;
		for (BetreuungspensumContainer betPenContainer : betreuung.getBetreuungspensumContainers()) {
			Gesuchsperiode gesuchsperiode = betPenContainer.extractGesuchsperiode();
			Gemeinde gemeinde = betreuung.extractGesuch().getDossier().getGemeinde();
			BigDecimal betreuungsangebotTypMinValue = BetreuungUtil.getMinValueFromBetreuungsangebotTyp(
				gesuchsperiode, gemeinde, betreuung.getBetreuungsangebotTyp(), einstellungService, em);

			if (validateBetreuungspensum(
				betPenContainer.getBetreuungspensumGS(),
				betreuungsangebotTypMinValue,
				index,
				"GS",
				context)
				&&
				validateBetreuungspensum(
					betPenContainer.getBetreuungspensumJA(),
					betreuungsangebotTypMinValue,
					index,
					"JA",
					context)
			) {
				index++;
			} else {

				closeEntityManager(em);
				return false;
			}
		}
		closeEntityManager(em);
		return true;
	}

	@Nullable
	private EntityManager createEntityManager() {
		if (entityManagerFactory != null) {
			return entityManagerFactory.createEntityManager(); // creates a new EntityManager
		}
		return null;
	}

	private void closeEntityManager(@Nullable EntityManager em) {
		if (em != null) {
			em.close();
		}
	}

	/**
	 * With the given the pensumMin it checks if the introduced pensum is in the permitted range. Case not a
	 * ConstraintValidator will be created with a message and a path indicating which object threw the error.
	 * False will be returned in the explained case.
	 * In case the value for pensum is right, nothing will be done and true will be returned.
	 *
	 * @param betreuungspensum the betreuungspensum to check
	 * @param pensumMin the minimum permitted value for pensum
	 * @param index the index of the Betreuungspensum inside the betreuungspensum container
	 * @param containerPostfix JA or GS
	 * @param context the context
	 * @return true if the value resides inside the permitted range. False otherwise
	 */
	private boolean validateBetreuungspensum(
		@Nullable Betreuungspensum betreuungspensum,
		BigDecimal pensumMin,
		int index,
		String containerPostfix,
		ConstraintValidatorContext context) {

		if (betreuungspensum == null) {
			return true;
		}

		// Es waere moeglich, die Messages mit der Klasse HibernateConstraintValidatorContext zu erzeugen. Das waere
		// aber Hibernate-abhaengig. wuerde es Sinn machen??
		if (!betreuungspensum.getNichtEingetreten() && betreuungspensum.getPensum().compareTo(pensumMin) < 0) {
			String message = ValidationMessageUtil.getMessage("invalid_betreuungspensum");
			message = MessageFormat.format(message, betreuungspensum.getPensum(), pensumMin);

			context.disableDefaultConstraintViolation();
			context.buildConstraintViolationWithTemplate(message)
				.addPropertyNode("betreuungspensumContainers["
					+ index
					+ "].betreuungspensum"
					+ containerPostfix
					+ ".pensum")
				.addConstraintViolation();

			return false;
		}
		return true;
	}
}
