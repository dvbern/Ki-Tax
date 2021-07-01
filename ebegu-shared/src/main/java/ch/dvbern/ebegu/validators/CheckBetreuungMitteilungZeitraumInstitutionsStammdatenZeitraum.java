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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package ch.dvbern.ebegu.validators;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import javax.validation.Constraint;
import javax.validation.Payload;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Die Betreuungspensen einer BetreuungMitteilung müssen innerhalb der Verfügbarkeit der Institution liegen (Zeitraum
 * der Institutionsstammdaten)
 */
@Target({ TYPE, ANNOTATION_TYPE })
@Retention(RUNTIME)
@Constraint(validatedBy = CheckBetreuungMitteilungZeitraumInstitutionsStammdatenZeitraumValidator.class)
@Documented
public @interface CheckBetreuungMitteilungZeitraumInstitutionsStammdatenZeitraum {

	String message() default "{invalid_betreuungszeitraum_for_institutionsstammdaten}";

	Class<?>[] groups() default {};

	Class<? extends Payload>[] payload() default {};

}
