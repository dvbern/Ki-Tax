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

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import ch.dvbern.ebegu.entities.Kind;

/**
 * Stellt sicher, dass die Angaben zum Kinderabzug korrekt sind
 */
public class CheckKinderabzugValidator implements ConstraintValidator<CheckKinderabzug, Kind> {

	@Override
	public void initialize(CheckKinderabzug constraintAnnotation) {
		//nop
	}

	@Override
	public boolean isValid(@Nonnull Kind kind, @Nullable ConstraintValidatorContext context) {
		if (!validatePflegekind(kind)) {
			return false;
		}
		if (!validateObhutAlternierend(kind)) {
			return false;
		}
		if (!validateKindErstausbildung(kind)) {
			return false;
		}
		if (!validateKindAbzugTypSchwyz(kind)) {
			return false;
		}
		return true;
	}

	private boolean validatePflegekind(@Nonnull Kind kind) {
		if (kind.getPflegekind()) {
			if (kind.getPflegeEntschaedigungErhalten() == null) {
				return false;
			}
			if (kind.getObhutAlternierendAusueben() != null) {
				return false;
			}
			if (kind.getInErstausbildung() != null) {
				return false;
			}
		}
		return true;
	}

	private boolean validateObhutAlternierend(@Nonnull Kind kind) {
		if (kind.getObhutAlternierendAusueben() != null) {
			if (kind.getPflegekind()) {
				return false;
			}
			if (kind.getInErstausbildung() != null) {
				return false;
			}
			if (kind.getAlimenteErhalten() != null) {
				return false;
			}
			if (kind.getAlimenteBezahlen() != null) {
				return false;
			}
		}
		return true;
	}

	private boolean validateKindErstausbildung(@Nonnull Kind kind) {
		if (kind.getInErstausbildung() != null) {
			if (kind.getObhutAlternierendAusueben() != null) {
				return false;
			}
			if (kind.getPflegekind()) {
				return false;
			}
			if (!kind.getInErstausbildung()) {
				if (kind.getAlimenteErhalten() != null) {
					return false;
				}
				return kind.getAlimenteBezahlen() == null;
			}
			if (kind.getAlimenteBezahlen() != null) {
				return kind.getAlimenteErhalten() == null;
			}
			if (kind.getAlimenteErhalten() != null) {
				return kind.getAlimenteBezahlen() == null;
			}
		}
		return true;
	}

	private boolean validateKindAbzugTypSchwyz(@Nonnull Kind kind) {
		if (kind.getUnterhaltspflichtig() != null) {
			if (Boolean.TRUE.equals(kind.getUnterhaltspflichtig())) {
				if (kind.getLebtKindAlternierend() == null) {
					return false;
				}
			} else if (kind.getLebtKindAlternierend() != null) {
				return false;
			}
		}
		return true;
	}
}
