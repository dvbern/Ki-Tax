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

package ch.dvbern.ebegu.tests.validations;

import java.math.BigDecimal;
import java.util.Set;

import javax.annotation.Nonnull;

import ch.dvbern.ebegu.entities.Betreuung;
import ch.dvbern.ebegu.entities.Betreuungspensum;
import ch.dvbern.ebegu.entities.BetreuungspensumContainer;
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.enums.BetreuungsangebotTyp;
import ch.dvbern.ebegu.test.TestDataUtil;
import ch.dvbern.ebegu.validators.betreuungspensum.CheckMittagstischPensum;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;

import static ch.dvbern.ebegu.tests.util.validation.ViolationMatchers.violatesAnnotation;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.not;

public class CheckMittagstischPensumValidatorTest extends AbstractValidatorTest {

	@ParameterizedTest
	@EnumSource(value = BetreuungsangebotTyp.class, names = "MITTAGSTISCH", mode = EnumSource.Mode.EXCLUDE)
	void pensumAndMahlzeitenCanBeArbitrary(BetreuungsangebotTyp betreuungsangebotTyp) {
		Betreuungspensum pensumGS =
			withMahlzeitenAndBetreuungspensum(BigDecimal.valueOf(4), BigDecimal.valueOf(12.25), BigDecimal.valueOf(75));
		Betreuungspensum pensumJA =
			withMahlzeitenAndBetreuungspensum(BigDecimal.valueOf(5), BigDecimal.valueOf(13), BigDecimal.valueOf(80));

		Betreuung betreuung = createBetreuung(betreuungsangebotTyp, pensumGS, pensumJA);

		assertThat(validate(betreuung), not(violatesAnnotation(CheckMittagstischPensum.class)));
	}

	@Nested
	class WhenBetreuungsangebotMittagstisch {

		@ParameterizedTest
		@CsvSource({
			"20.5, 100",
			"10.25, 50",
			"4.1, 20",
			"2.05, 10",
			"1, 4.8775",
			"1, 4.878",
			"1, 4.879",
			"0, 0"
		})
		void shouldPassWhenPensumIsDerived(BigDecimal anzahlMahlzeiten, BigDecimal pensum) {
			Betreuungspensum pensumGS =
				withMahlzeitenAndBetreuungspensum(anzahlMahlzeiten, BigDecimal.valueOf(12.25), pensum);
			Betreuungspensum pensumJA =
				withMahlzeitenAndBetreuungspensum(anzahlMahlzeiten, BigDecimal.valueOf(13), pensum);

			Betreuung betreuung = createBetreuung(BetreuungsangebotTyp.MITTAGSTISCH, pensumGS, pensumJA);

			assertThat(validate(betreuung), not(violatesAnnotation(CheckMittagstischPensum.class)));
		}

		@ParameterizedTest
		@CsvSource("1, 2, 10, 20.5, 100")
		void shouldFailWhenPensumMismatchesMahlzeiten(BigDecimal anzahlMahlzeiten) {
			Betreuungspensum pensumGS =
				withMahlzeitenAndBetreuungspensum(anzahlMahlzeiten, BigDecimal.valueOf(12.25), BigDecimal.ZERO);
			Betreuungspensum pensumJA =
				withMahlzeitenAndBetreuungspensum(anzahlMahlzeiten, BigDecimal.valueOf(13), BigDecimal.ZERO);

			Betreuung betreuung = createBetreuung(BetreuungsangebotTyp.MITTAGSTISCH, pensumGS, pensumJA);

			assertThat(validate(betreuung), violatesAnnotation(CheckMittagstischPensum.class));
		}
	}

	@Nonnull
	private Betreuungspensum withMahlzeitenAndBetreuungspensum(
		BigDecimal anzahlMahlzeiten,
		BigDecimal tarifProMahlzeit,
		BigDecimal pensum
	) {
		var result = TestDataUtil.createBetreuungspensumMittagstisch(anzahlMahlzeiten, tarifProMahlzeit);
		result.setPensum(pensum);

		return result;
	}

	@Nonnull
	private Betreuung createBetreuung(
		@Nonnull BetreuungsangebotTyp betreuungsangebotTyp,
		@Nonnull Betreuungspensum pensumGS,
		@Nonnull Betreuungspensum pensumJA
	) {
		Gesuch gesuch = TestDataUtil.createDefaultGesuch();
		gesuch.setGesuchsperiode(TestDataUtil.createGesuchsperiode1718());

		Betreuung betreuung = TestDataUtil.createDefaultBetreuung();
		betreuung.getKind().setGesuch(gesuch);
		betreuung.getInstitutionStammdaten().setBetreuungsangebotTyp(betreuungsangebotTyp);

		BetreuungspensumContainer betPensContainer = TestDataUtil.createBetPensContainer(betreuung);
		betPensContainer.setBetreuungspensumGS(pensumGS);
		betPensContainer.setBetreuungspensumJA(pensumJA);

		betreuung.setBetreuungspensumContainers(Set.of(betPensContainer));

		return betreuung;
	}
}
