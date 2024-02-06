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

package ch.dvbern.ebegu.services.util;

import java.time.LocalDate;

import ch.dvbern.ebegu.entities.Familiensituation;
import ch.dvbern.ebegu.entities.Gesuchsperiode;
import ch.dvbern.ebegu.enums.EnumFamilienstatus;
import ch.dvbern.ebegu.enums.UnterhaltsvereinbarungAnswer;
import ch.dvbern.ebegu.types.DateRange;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class ErwerbspensumHelperTest {

	private static final Gesuchsperiode GESUCHSPERIODE = new Gesuchsperiode();

	@BeforeAll
	public static void setup() {
		GESUCHSPERIODE.setGueltigkeit(new DateRange(LocalDate.of(2024, 8, 1), LocalDate.of(2025, 7, 31)));
	}

	@Test
	public void verheiratetPensumGS2ShouldNotBeOmittable() {
		Familiensituation familiensituation = new Familiensituation();
		familiensituation.setFamilienstatus(EnumFamilienstatus.VERHEIRATET);

		assertThat(ErwerbspensumHelper.isKonkubinatOhneKindAndGS2ErwerbspensumOmittable(familiensituation, GESUCHSPERIODE), is(false));
	}

	// we don't check for the specific alleinstehend cases here, this helper function is only responsible for the konkubinat cases
	@Test
	public void alleinerziehendPensumGS2ShouldNotBeOmittable() {
		Familiensituation familiensituation = new Familiensituation();
		familiensituation.setFamilienstatus(EnumFamilienstatus.ALLEINERZIEHEND);

		assertThat(ErwerbspensumHelper.isKonkubinatOhneKindAndGS2ErwerbspensumOmittable(familiensituation, GESUCHSPERIODE), is(false));
	}

	@Test
	public void konkubinatMitKindPensumGS2ShouldNotBeOmittable() {
		Familiensituation familiensituation = new Familiensituation();
		familiensituation.setFamilienstatus(EnumFamilienstatus.KONKUBINAT);

		assertThat(ErwerbspensumHelper.isKonkubinatOhneKindAndGS2ErwerbspensumOmittable(familiensituation, GESUCHSPERIODE), is(false));
	}

	// Konkubinatpartner as a whole not required, therefore we don't have the special case
	@Test
	public void shortKonkubinatOhneKindPensumGS2ShouldBeOmittable() {
		Familiensituation familiensituation = new Familiensituation();
		familiensituation.setFamilienstatus(EnumFamilienstatus.KONKUBINAT_KEIN_KIND);
		familiensituation.setMinDauerKonkubinat(2);
		familiensituation.setStartKonkubinat(LocalDate.of(2025, 1,1));

		assertThat(ErwerbspensumHelper.isKonkubinatOhneKindAndGS2ErwerbspensumOmittable(familiensituation, GESUCHSPERIODE), is(false));
	}

	@Test
	public void longKonkubinatOhneKindPensumGS2ShouldNotBeOmittable() {
		Familiensituation familiensituation = new Familiensituation();
		familiensituation.setFamilienstatus(EnumFamilienstatus.KONKUBINAT_KEIN_KIND);
		familiensituation.setMinDauerKonkubinat(2);
		familiensituation.setStartKonkubinat(LocalDate.of(2020, 1,1));

		assertThat(ErwerbspensumHelper.isKonkubinatOhneKindAndGS2ErwerbspensumOmittable(familiensituation, GESUCHSPERIODE), is(false));
	}

	@Test
	public void konkubinatBecomingXJaehrigDuringGPOhneKindPensumGS2ShouldBeOmittable() {
		Familiensituation familiensituation = new Familiensituation();
		familiensituation.setFamilienstatus(EnumFamilienstatus.KONKUBINAT_KEIN_KIND);
		familiensituation.setMinDauerKonkubinat(2);
		familiensituation.setStartKonkubinat(LocalDate.of(2023, 1,1));
		familiensituation.setGeteilteObhut(false);
		familiensituation.setUnterhaltsvereinbarung(UnterhaltsvereinbarungAnswer.NEIN_UNTERHALTSVEREINBARUNG);

		assertThat(ErwerbspensumHelper.isKonkubinatOhneKindAndGS2ErwerbspensumOmittable(familiensituation, GESUCHSPERIODE), is(true));
	}
}
