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

package ch.dvbern.ebegu.validators.betreuungspensum;

import java.math.BigDecimal;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import ch.dvbern.ebegu.entities.AbstractMahlzeitenPensum;
import ch.dvbern.ebegu.entities.Betreuung;
import ch.dvbern.ebegu.entities.containers.BetreuungAndPensumContainer;
import ch.dvbern.ebegu.enums.betreuung.BetreuungsangebotTyp;
import ch.dvbern.ebegu.util.BetreuungUtil;
import ch.dvbern.ebegu.util.MathUtil;

/**
 * Validator for Betreuungspensen, for {@link BetreuungsangebotTyp#MITTAGSTISCH} checks that each
 * {@link AbstractMahlzeitenPensum#getPensum} is derived from {@link AbstractMahlzeitenPensum#getMonatlicheHauptmahlzeiten}
 * and that {@link AbstractMahlzeitenPensum#getMonatlicheBetreuungskosten} matches
 * {@link AbstractMahlzeitenPensum#getTarifProHauptmahlzeit}.
 */
public class CheckMittagstischPensumValidator
	implements ConstraintValidator<CheckMittagstischPensum, BetreuungAndPensumContainer> {

	@Override
	public boolean isValid(BetreuungAndPensumContainer container, ConstraintValidatorContext context) {
		return container.findBetreuung()
			.filter(Betreuung::isAngebotMittagstisch)
			.map(b -> container.getBetreuungenJA().stream().allMatch(this::isValid))
			.orElse(true);
	}

	private boolean isValid(AbstractMahlzeitenPensum pensum) {
		return hasValidMittagstischPensum(pensum) && hasValidKosten(pensum);
	}

	private boolean hasValidMittagstischPensum(AbstractMahlzeitenPensum pensum) {
		BigDecimal derivedPensum = BetreuungUtil.derivePensumMittagstisch(pensum);

		return MathUtil.isClose(derivedPensum, pensum.getPensum(), BigDecimal.valueOf(0.001));
	}

	private boolean hasValidKosten(AbstractMahlzeitenPensum pensum) {
		BigDecimal derivedKosten = BetreuungUtil.deriveKostenMittagstisch(pensum);
		BigDecimal roundedKosten = MathUtil.toTwoKommastelle(pensum.getMonatlicheBetreuungskosten());

		return MathUtil.isSame(derivedKosten, roundedKosten);
	}
}
