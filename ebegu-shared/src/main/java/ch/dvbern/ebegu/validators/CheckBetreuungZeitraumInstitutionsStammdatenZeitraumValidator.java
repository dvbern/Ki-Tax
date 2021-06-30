/*
 * Ki-Tax: System for the management of external childcare subsidies
 * Copyright (C) 2018 City of Bern Switzerland
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

import java.text.MessageFormat;
import java.time.LocalDate;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import javax.validation.constraints.NotNull;

import ch.dvbern.ebegu.entities.Betreuung;
import ch.dvbern.ebegu.entities.BetreuungspensumContainer;
import ch.dvbern.ebegu.types.DateRange;
import ch.dvbern.ebegu.util.Constants;
import ch.dvbern.ebegu.util.DateUtil;
import ch.dvbern.ebegu.util.KitaxUtil;
import ch.dvbern.ebegu.util.ValidationMessageUtil;

/**
 * Die Betreuungspensen einer Betreuung müssen innerhalb der Verfügbarkeit der Institution liegen (Zeitraum der Institutionsstammdaten)
 */
public class CheckBetreuungZeitraumInstitutionsStammdatenZeitraumValidator implements ConstraintValidator<CheckBetreuungZeitraumInstitutionsStammdatenZeitraum, Betreuung> {

	@Override
	public void initialize(CheckBetreuungZeitraumInstitutionsStammdatenZeitraum constraintAnnotation) {
		// nop
	}

	@Override
	public boolean isValid(Betreuung betreuung, ConstraintValidatorContext context) {
		DateRange institutionStammdatenDateRange = betreuung.getInstitutionStammdaten().getGueltigkeit();
		// Uns interessiert grundsaetzlich nur der Bereich innerhalb der Gesuchsperiode
		DateRange stammdatenWithinGP = DateUtil.limitToDateRange(institutionStammdatenDateRange, betreuung.extractGesuchsperiode().getGueltigkeit());

		for (BetreuungspensumContainer betreuungspensumContainer : betreuung.getBetreuungspensumContainers()) {
			DateRange pensumDateRange = betreuungspensumContainer.getBetreuungspensumJA().getGueltigkeit();
			// Uns interessiert grundsaetzlich nur der Bereich innerhalb der Gesuchsperiode
			DateRange betreuungWithinGP = DateUtil.limitToDateRange(pensumDateRange, betreuung.extractGesuchsperiode().getGueltigkeit());
			// Da wir jetzt nur noch die Gesuchsperiode betrachten, darf die Betreuung NIE ausserhalb der Stammdaten sein
			if (!stammdatenWithinGP.contains(betreuungWithinGP)) {
				// Sonderfall: Fuer die Stadt Bern nach FEBR sind teilweise auch Kitas zugelassen,
				// die mit ASIV erst spaeter beginnen, jedoch nach FEBR Gutscheine akzeptieren
				if (KitaxUtil.isGemeindeWithKitaxUebergangsloesung(betreuung.extractGemeinde())
					&& KitaxUtil.isInstitutionAcceptingFebrButNotAsiv(betreuung.getInstitutionStammdaten())) {
					return true;
				}
				setConstraintViolationMessage(institutionStammdatenDateRange, context);
				return false;
			}
		}
		return true;
	}

	private void setConstraintViolationMessage(@NotNull DateRange institutionStammdatenDateRange, @NotNull ConstraintValidatorContext context) {
		String message = ValidationMessageUtil.getMessage("invalid_betreuungszeitraum_for_institutionsstammdaten");
		message = MessageFormat.format(message, Constants.DATE_FORMATTER.format(institutionStammdatenDateRange.getGueltigAb()),
			Constants.DATE_FORMATTER.format(institutionStammdatenDateRange.getGueltigBis()));

		context.disableDefaultConstraintViolation();
		context.buildConstraintViolationWithTemplate(message)
			.addConstraintViolation();
	}
}
