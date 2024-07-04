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

package ch.dvbern.ebegu.rules;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import ch.dvbern.ebegu.entities.Betreuung;
import ch.dvbern.ebegu.entities.Einstellung;
import ch.dvbern.ebegu.entities.ErwerbspensumContainer;
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.entities.GesuchstellerContainer;
import ch.dvbern.ebegu.entities.VerfuegungZeitabschnitt;
import ch.dvbern.ebegu.enums.AnspruchBeschaeftigungAbhaengigkeitTyp;
import ch.dvbern.ebegu.enums.EinstellungKey;
import ch.dvbern.ebegu.enums.Taetigkeit;
import ch.dvbern.ebegu.test.TestDataUtil;
import org.easymock.EasyMockSupport;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static ch.dvbern.ebegu.enums.EinstellungKey.ABHAENGIGKEIT_ANSPRUCH_BESCHAEFTIGUNGPENSUM;
import static ch.dvbern.ebegu.enums.EinstellungKey.ERWERBSPENSUM_ZUSCHLAG;
import static ch.dvbern.ebegu.enums.EinstellungKey.MIN_ERWERBSPENSUM_NICHT_EINGESCHULT;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

class ErwerbspensumSchwyzCalcRuleTest extends EasyMockSupport {

	private static Map<EinstellungKey, Einstellung> einstellungenMap;

	@BeforeAll
	public static void setUp() {
		einstellungenMap = EbeguRuleTestsHelper.getAllEinstellungen(TestDataUtil.createGesuchsperiode1718());
		einstellungenMap.get(ABHAENGIGKEIT_ANSPRUCH_BESCHAEFTIGUNGPENSUM)
			.setValue(AnspruchBeschaeftigungAbhaengigkeitTyp.SCHWYZ.name());
		einstellungenMap.get(MIN_ERWERBSPENSUM_NICHT_EINGESCHULT).setValue(String.valueOf(20));
		einstellungenMap.get(ERWERBSPENSUM_ZUSCHLAG).setValue(String.valueOf(0));
	}

	@Nested
	class OneGSTest {

		@Test
		void keinErwerbspensumShouldNotHaveAnspruch() {
			Betreuung betreuung = TestDataUtil.createGesuchWithBetreuungspensum(false);
			List<VerfuegungZeitabschnitt> result = EbeguRuleTestsHelper.calculate(betreuung, einstellungenMap);
			assertThat(result.get(0).getAnspruchberechtigtesPensum(), is(0));
		}

		@ParameterizedTest
		@ValueSource(ints = { 0, 10, 19 })
		void totalErwerbspensumOfXLessThanMinShouldHaveNoAnspruch(int anspruch) {
			Betreuung betreuung = TestDataUtil.createGesuchWithBetreuungspensum(false);
			Gesuch gesuch = betreuung.extractGesuch();
			final GesuchstellerContainer gesuchsteller = gesuch.getGesuchsteller1();
			Objects.requireNonNull(gesuchsteller);
			createErwerbspensum(gesuchsteller, anspruch);

			List<VerfuegungZeitabschnitt> result = EbeguRuleTestsHelper.calculate(betreuung, einstellungenMap);

			assertThat(result.get(0).getAnspruchberechtigtesPensum(), is(0));
		}

		@ParameterizedTest
		@ValueSource(ints = { 20, 40, 100 })
		void totalErwerbspensumOfXBiggerEqualMinShouldHaveXAnspruch(int anspruch) {
			Betreuung betreuung = TestDataUtil.createGesuchWithBetreuungspensum(false);
			Gesuch gesuch = betreuung.extractGesuch();
			final GesuchstellerContainer gesuchsteller = gesuch.getGesuchsteller1();
			Objects.requireNonNull(gesuchsteller);
			createErwerbspensum(gesuchsteller, anspruch);

			List<VerfuegungZeitabschnitt> result = EbeguRuleTestsHelper.calculate(betreuung, einstellungenMap);

			assertThat(result.get(0).getAnspruchberechtigtesPensum(), is(anspruch));
		}

	}

	private static void createErwerbspensum(GesuchstellerContainer gesuchsteller, int pensum) {
		final ErwerbspensumContainer erwerbspensumContainer = TestDataUtil.createErwerbspensum(pensum, Taetigkeit.ANGESTELLT);
		erwerbspensumContainer.setGesuchsteller(gesuchsteller);
		gesuchsteller.addErwerbspensumContainer(erwerbspensumContainer);
	}

}
