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
 */

package ch.dvbern.ebegu.validators.dateranges;

import java.text.MessageFormat;
import java.util.List;

import javax.annotation.Nonnull;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import ch.dvbern.ebegu.entities.AbstractMahlzeitenPensum;
import ch.dvbern.ebegu.entities.Betreuung;
import ch.dvbern.ebegu.entities.containers.BetreuungAndPensumContainer;
import ch.dvbern.ebegu.types.DateRange;
import ch.dvbern.ebegu.util.Constants;
import ch.dvbern.ebegu.util.DateUtil;
import ch.dvbern.ebegu.util.KitaxUtil;
import ch.dvbern.ebegu.util.ValidationMessageUtil;

/**
 * Die Betreuungspensen eines {@link BetreuungAndPensumContainer} müssen innerhalb der Verfügbarkeit der Institution liegen
 * (Zeitraum der Institutionsstammdaten)
 */
public class CheckBetreuungZeitraumInstitutionsStammdatenZeitraumValidator
	implements ConstraintValidator<CheckBetreuungZeitraumInstitutionsStammdatenZeitraum, BetreuungAndPensumContainer> {

	@Override
	public boolean isValid(BetreuungAndPensumContainer container, ConstraintValidatorContext context) {
		return container.findBetreuung()
			.map(betreuung -> {
				if (hasPensenGueltigkeitWithinInstitutionStammdatenGueltigkeit(betreuung, container.getForJA())) {
					return true;
				}

				setConstraintViolationMessage(betreuung.getInstitutionStammdaten().getGueltigkeit(), context);

				return false;
			})
			.orElse(true);
	}

	private boolean hasPensenGueltigkeitWithinInstitutionStammdatenGueltigkeit(
		@Nonnull Betreuung betreuung,
		@Nonnull List<? extends AbstractMahlzeitenPensum> container
	) {
		DateRange institutionStammdatenDateRange = betreuung.getInstitutionStammdaten().getGueltigkeit();
		DateRange gesuchsperiode = betreuung.extractGesuchsperiode().getGueltigkeit();
		// Uns interessiert grundsaetzlich nur der Bereich innerhalb der Gesuchsperiode
		DateRange stammdatenWithinGP = DateUtil.limitToDateRange(institutionStammdatenDateRange, gesuchsperiode);

		for (AbstractMahlzeitenPensum pensum : container) {
			DateRange pensumDateRange = pensum.getGueltigkeit();
			// Uns interessiert grundsaetzlich nur der Bereich innerhalb der Gesuchsperiode
			DateRange betreuungWithinGP = DateUtil.limitToDateRange(pensumDateRange, gesuchsperiode);
			// Da wir jetzt nur noch die Gesuchsperiode betrachten, darf die Betreuung NIE ausserhalb der Stammdaten sein
			if (!stammdatenWithinGP.contains(betreuungWithinGP)) {
				// Sonderfall: Fuer die Stadt Bern nach FEBR sind teilweise auch Kitas zugelassen,
				// die mit ASIV erst spaeter beginnen, jedoch nach FEBR Gutscheine akzeptieren
				return KitaxUtil.isGemeindeWithKitaxUebergangsloesung(betreuung.extractGemeinde())
					&& KitaxUtil.isInstitutionAcceptingFebrButNotAsiv(betreuung.getInstitutionStammdaten());
			}
		}
		return true;
	}

	private void setConstraintViolationMessage(@Nonnull DateRange institutionStammdatenDateRange, @Nonnull ConstraintValidatorContext context) {
		String message = ValidationMessageUtil.getMessage("invalid_betreuungszeitraum_for_institutionsstammdaten");
		message = MessageFormat.format(message, Constants.DATE_FORMATTER.format(institutionStammdatenDateRange.getGueltigAb()),
			Constants.DATE_FORMATTER.format(institutionStammdatenDateRange.getGueltigBis()));

		context.disableDefaultConstraintViolation();
		context.buildConstraintViolationWithTemplate(message)
			.addConstraintViolation();
	}
}
