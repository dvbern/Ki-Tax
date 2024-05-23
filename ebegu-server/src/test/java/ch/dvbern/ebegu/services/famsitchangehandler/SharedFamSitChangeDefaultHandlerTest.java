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

package ch.dvbern.ebegu.services.famsitchangehandler;

import java.time.LocalDate;
import java.util.Objects;
import java.util.stream.Stream;

import javax.annotation.Nonnull;

import ch.dvbern.ebegu.entities.Familiensituation;
import ch.dvbern.ebegu.entities.FamiliensituationContainer;
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.entities.GesuchstellerContainer;
import ch.dvbern.ebegu.enums.AntragStatus;
import ch.dvbern.ebegu.enums.EnumFamilienstatus;
import ch.dvbern.ebegu.enums.EnumGesuchstellerKardinalitaet;
import ch.dvbern.ebegu.enums.FinanzielleSituationTyp;
import ch.dvbern.ebegu.enums.UnterhaltsvereinbarungAnswer;
import ch.dvbern.ebegu.services.EinstellungService;
import ch.dvbern.ebegu.services.GesuchstellerService;
import ch.dvbern.ebegu.test.TestDataUtil;
import ch.dvbern.ebegu.util.Constants;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.easymock.EasyMockExtension;
import org.easymock.EasyMockSupport;
import org.easymock.Mock;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;

@ExtendWith(EasyMockExtension.class)
class SharedFamSitChangeDefaultHandlerTest extends EasyMockSupport {

	@Mock
	private EinstellungService einstellungService;

	@Mock
	private GesuchstellerService gesuchstellerService;

	private SharedFamSitChangeDefaultHandler handler = null;

	@BeforeEach
	void setupTestee() {
		handler = new SharedFamSitChangeDefaultHandler(gesuchstellerService, einstellungService);
	}

	@TestInstance(Lifecycle.PER_CLASS)
	@Nested
	class ErstantragTest {

		LocalDate eingangsdatum = TestDataUtil.START_PERIODE.minusMonths(1);

		@ParameterizedTest(name = "to {0}")
		@MethodSource("provideFamiliensituationWithGS2NotToRemove")
		@DisplayName("GS2 should not be removed on change from Verheiratet")
		void testParameterizedChangeShouldNotRemove(FamSitChangeTestConfiguration configuration) {
			Gesuch gesuch = setupGesuchWith2GS();
			getFamiliensituationContainerNullSafe(gesuch).setFamiliensituationJA(configuration.newFamiliensituation);

			handler.removeGS2DataOnChangeFrom2To1GS(
				gesuch,
				configuration.newFamiliensituation,
				getFamiliensituationContainerNullSafe(gesuch),
				configuration.oldFamiliensituation);

			assertThat(gesuch.getGesuchsteller2(), notNullValue());
		}

		@ParameterizedTest(name = "{0}")
		@MethodSource("provideFamiliensituationWithGS2ToRemove")
		@DisplayName("GS2 should be removed on change from")
		void testParameterizedChangeShouldRemove(FamSitChangeTestConfiguration configuration) {
			Gesuch gesuch = setupGesuchWith2GS();
			getFamiliensituationContainerNullSafe(gesuch).setFamiliensituationJA(configuration.newFamiliensituation);

			handler.removeGS2DataOnChangeFrom2To1GS(
				gesuch,
				configuration.newFamiliensituation,
				getFamiliensituationContainerNullSafe(gesuch),
				configuration.oldFamiliensituation);

			assertThat(gesuch.getGesuchsteller2(), nullValue());
		}

		private Stream<Arguments> provideFamiliensituationWithGS2NotToRemove() {
			return Stream.of(
				FamSitChangeTestConfiguration.of("Verheiratet", setupFamiliensituation(EnumFamilienstatus.VERHEIRATET)),
				FamSitChangeTestConfiguration.of(
					"Alleinerziehend mit geteilter Obhut zu zweit",
					setupAlleinerziehenddGeteilteObhut(eingangsdatum, EnumGesuchstellerKardinalitaet.ZU_ZWEIT)),
				FamSitChangeTestConfiguration.of(
					"Konkubinat ohne Kind mit geteilter Obhut zu zweit",
					setupKonkubinatOhneKindGeteilteObhut(eingangsdatum, EnumGesuchstellerKardinalitaet.ZU_ZWEIT)),
				FamSitChangeTestConfiguration.of(
					"Alleinerziehend alleinige Obhut ohne Unterhaltsvereinbarung",
					setupAlleinerziehendNichtGeteilteObhut(
						eingangsdatum,
						UnterhaltsvereinbarungAnswer.NEIN_UNTERHALTSVEREINBARUNG)),
				FamSitChangeTestConfiguration.of(
					"Konkubinat ohne Kind alleinige Obhut ohne Unterhaltsvereinbarung",
					setupKonkubinatOhneKindNichtGeteilteObhut(
						eingangsdatum,
						UnterhaltsvereinbarungAnswer.NEIN_UNTERHALTSVEREINBARUNG))
			).flatMap(config -> Stream.of(
				FamSitChangeTestConfiguration.builder()
					.name('[' + config.name + "] to Konkubinat mit Kind")
					.oldFamiliensituation(config.oldFamiliensituation)
					.newFamiliensituation(setupFamiliensituation(EnumFamilienstatus.KONKUBINAT))
					.build(),
				FamSitChangeTestConfiguration.builder()
					.name('[' + config.name + "]  to Konkubinat ohne Kind mit geteilter Obhut und gemeinsamen Gesuch")
					.oldFamiliensituation(config.oldFamiliensituation)
					.newFamiliensituation(setupKonkubinatOhneKindGeteilteObhut(
						eingangsdatum,
						EnumGesuchstellerKardinalitaet.ZU_ZWEIT))
					.build(),
				FamSitChangeTestConfiguration.builder()
					.name('[' + config.name + "]  to Konkubinat ohne Kind ohne geteilte Obhut ohne Unterhaltsvereinbarung")
					.oldFamiliensituation(config.oldFamiliensituation)
					.newFamiliensituation(setupKonkubinatOhneKindNichtGeteilteObhut(
						eingangsdatum,
						UnterhaltsvereinbarungAnswer.NEIN_UNTERHALTSVEREINBARUNG))
					.build(),
				FamSitChangeTestConfiguration.builder()
					.name('[' + config.name + "]  to Alleinerziehend mit geteilter Obhut und gemeinsamen Gesuch")
					.oldFamiliensituation(config.oldFamiliensituation)
					.newFamiliensituation(setupAlleinerziehenddGeteilteObhut(
						eingangsdatum,
						EnumGesuchstellerKardinalitaet.ZU_ZWEIT))
					.build(),
				FamSitChangeTestConfiguration.builder()
					.name('[' + config.name + "]  to Alleinerziehend ohne geteilte Obhut ohne Unterhaltsvereinbarung")
					.oldFamiliensituation(config.oldFamiliensituation)
					.newFamiliensituation(setupAlleinerziehendNichtGeteilteObhut(
						eingangsdatum,
						UnterhaltsvereinbarungAnswer.NEIN_UNTERHALTSVEREINBARUNG))
					.build(),
				FamSitChangeTestConfiguration.builder()
					.name('[' + config.name + "]  to Long Konkubinat Ohne Kind")
					.oldFamiliensituation(config.oldFamiliensituation)
					.newFamiliensituation(setupLongKonkubinat())
					.build()
			)).map(Arguments::of);
		}

		private Stream<Arguments> provideFamiliensituationWithGS2ToRemove() {
			return Stream.of(
					FamSitChangeTestConfiguration.of("Verheiratet", setupFamiliensituation(EnumFamilienstatus.VERHEIRATET)),
					FamSitChangeTestConfiguration.of(
						"Alleinerziehend mit geteilter Obhut zu zweit",
						setupAlleinerziehenddGeteilteObhut(eingangsdatum, EnumGesuchstellerKardinalitaet.ZU_ZWEIT)),
					FamSitChangeTestConfiguration.of(
						"Konkubinat ohne Kind mit geteilter Obhut zu zweit",
						setupKonkubinatOhneKindGeteilteObhut(eingangsdatum, EnumGesuchstellerKardinalitaet.ZU_ZWEIT)),
					FamSitChangeTestConfiguration.of(
						"Alleinerziehend alleinige Obhut ohne Unterhaltsvereinbarung",
						setupAlleinerziehendNichtGeteilteObhut(
							eingangsdatum,
							UnterhaltsvereinbarungAnswer.NEIN_UNTERHALTSVEREINBARUNG)),
					FamSitChangeTestConfiguration.of(
						"Konkubinat ohne Kind alleinige Obhut ohne Unterhaltsvereinbarung",
						setupKonkubinatOhneKindNichtGeteilteObhut(
							eingangsdatum,
							UnterhaltsvereinbarungAnswer.NEIN_UNTERHALTSVEREINBARUNG))
				).flatMap(this::getFamSitWithGS2ToRemoveTestConfigurations)
				.map(Arguments::of);
		}

		@Nonnull
		private Stream<FamSitChangeTestConfiguration> getFamSitWithGS2ToRemoveTestConfigurations(FamSitChangeTestConfiguration oldConfig) {
			return Stream.of(
				FamSitChangeTestConfiguration.builder()
					.name('[' + oldConfig.name + "] to Konkubinat ohne Kind mit geteilter Obhut und alleinigem Gesuch")
					.oldFamiliensituation(oldConfig.oldFamiliensituation)
					.newFamiliensituation(setupKonkubinatOhneKindGeteilteObhut(
						eingangsdatum,
						EnumGesuchstellerKardinalitaet.ALLEINE))
					.build(),
				FamSitChangeTestConfiguration.builder()
					.name('[' + oldConfig.name + "]  to Konkubinat ohne Kind ohne geteilte Obhut mit Unterhaltsvereinbarung")
					.oldFamiliensituation(oldConfig.oldFamiliensituation)
					.newFamiliensituation(setupKonkubinatOhneKindNichtGeteilteObhut(
						eingangsdatum,
						UnterhaltsvereinbarungAnswer.JA_UNTERHALTSVEREINBARUNG))
					.build(),
				FamSitChangeTestConfiguration.builder()
					.name('['
						+ oldConfig.name
						+ "]  to Konkubinat ohne Kind ohne geteilte Obhut mit Unterhaltsvereinbarung nicht möglich")
					.oldFamiliensituation(oldConfig.oldFamiliensituation)
					.newFamiliensituation(setupKonkubinatOhneKindNichtGeteilteObhut(
						eingangsdatum,
						UnterhaltsvereinbarungAnswer.UNTERHALTSVEREINBARUNG_NICHT_MOEGLICH))
					.build(),
				FamSitChangeTestConfiguration.builder()
					.name('[' + oldConfig.name + "]  to Alleinerziehend mit geteilter Obhut und alleinigem Gesuch")
					.oldFamiliensituation(oldConfig.oldFamiliensituation)
					.newFamiliensituation(setupAlleinerziehenddGeteilteObhut(
						eingangsdatum,
						EnumGesuchstellerKardinalitaet.ALLEINE))
					.build(),
				FamSitChangeTestConfiguration.builder()
					.name('[' + oldConfig.name + "]  to Alleinerziehend ohne geteilte Obhut mit Unterhaltsvereinbarung")
					.oldFamiliensituation(oldConfig.oldFamiliensituation)
					.newFamiliensituation(setupAlleinerziehendNichtGeteilteObhut(
						eingangsdatum,
						UnterhaltsvereinbarungAnswer.JA_UNTERHALTSVEREINBARUNG))
					.build(),
				FamSitChangeTestConfiguration.builder()
					.name('['
						+ oldConfig.name
						+ "]  to Alleinerziehend ohne geteilte Obhut mit Unterhaltsvereinbarung nicht möglich")
					.oldFamiliensituation(oldConfig.oldFamiliensituation)
					.newFamiliensituation(setupAlleinerziehendNichtGeteilteObhut(
						eingangsdatum,
						UnterhaltsvereinbarungAnswer.UNTERHALTSVEREINBARUNG_NICHT_MOEGLICH))
					.build()
			);
		}
		@Nonnull
		private Gesuch setupGesuchWith2GS() {
			Gesuch gesuch = TestDataUtil.createDefaultGesuch();
			final GesuchstellerContainer gs1Container = TestDataUtil.createDefaultGesuchstellerContainer();
			gs1Container.getErwerbspensenContainers().add(TestDataUtil.createErwerbspensumContainer());
			final GesuchstellerContainer gs2Container = TestDataUtil.createDefaultGesuchstellerContainer();
			gs2Container.getErwerbspensenContainers().add(TestDataUtil.createErwerbspensumContainer());
			gesuch.setGesuchsteller1(gs1Container);
			gesuch.setGesuchsteller2(gs2Container);
			gesuch.setFinSitTyp(FinanzielleSituationTyp.BERN_FKJV);
			return gesuch;
		}
	}

	@TestInstance(Lifecycle.PER_CLASS)
	@Nested
	class MutationDuringGPTest {

		private final LocalDate eingangsdatum = TestDataUtil.START_PERIODE.plusMonths(1);

		@ParameterizedTest(name = "to {0}")
		@MethodSource("provideFamiliensituationWithGS2NotToRemove")
		@DisplayName("GS2 should not be removed on change from")
		void testParameterizedChangeShouldNotRemove(FamSitChangeTestConfiguration configuration) {
			Gesuch gesuch = setupMutationWith2GS();
			getFamiliensituationContainerNullSafe(gesuch).setFamiliensituationJA(configuration.newFamiliensituation);

			handler.removeGS2DataOnChangeFrom2To1GS(
				gesuch,
				configuration.newFamiliensituation,
				getFamiliensituationContainerNullSafe(gesuch),
				configuration.oldFamiliensituation);

			assertThat(gesuch.getGesuchsteller2(), notNullValue());
		}

		private Stream<Arguments> provideFamiliensituationWithGS2NotToRemove() {
			return Stream.of(
				FamSitChangeTestConfiguration.of("Verheiratet", setupFamiliensituation(EnumFamilienstatus.VERHEIRATET)),
				FamSitChangeTestConfiguration.of(
					"Alleinerziehend mit geteilter Obhut zu zweit",
					setupAlleinerziehenddGeteilteObhut(eingangsdatum, EnumGesuchstellerKardinalitaet.ZU_ZWEIT)),
				FamSitChangeTestConfiguration.of(
					"Konkubinat ohne Kind mit geteilter Obhut zu zweit",
					setupKonkubinatOhneKindGeteilteObhut(eingangsdatum, EnumGesuchstellerKardinalitaet.ZU_ZWEIT)),
				FamSitChangeTestConfiguration.of(
					"Alleinerziehend alleinige Obhut ohne Unterhaltsvereinbarung",
					setupAlleinerziehendNichtGeteilteObhut(
						eingangsdatum,
						UnterhaltsvereinbarungAnswer.NEIN_UNTERHALTSVEREINBARUNG)),
				FamSitChangeTestConfiguration.of(
					"Konkubinat ohne Kind alleinige Obhut ohne Unterhaltsvereinbarung",
					setupKonkubinatOhneKindNichtGeteilteObhut(
						eingangsdatum,
						UnterhaltsvereinbarungAnswer.NEIN_UNTERHALTSVEREINBARUNG))
			).flatMap(config -> Stream.of(
				FamSitChangeTestConfiguration.builder()
					.name('[' + config.name + "] to Konkubinat ohne Kind mit geteilter Obhut und alleinigem Gesuch")
					.oldFamiliensituation(config.oldFamiliensituation)
					.newFamiliensituation(setupKonkubinatOhneKindGeteilteObhut(
						eingangsdatum,
						EnumGesuchstellerKardinalitaet.ALLEINE))
					.build(),
				FamSitChangeTestConfiguration.builder()
					.name('[' + config.name + "]  to Konkubinat ohne Kind ohne geteilte Obhut mit Unterhaltsvereinbarung")
					.oldFamiliensituation(config.oldFamiliensituation)
					.newFamiliensituation(setupKonkubinatOhneKindNichtGeteilteObhut(
						eingangsdatum,
						UnterhaltsvereinbarungAnswer.JA_UNTERHALTSVEREINBARUNG))
					.build(),
				FamSitChangeTestConfiguration.builder()
					.name('['
						+ config.name
						+ "]  to Konkubinat ohne Kind ohne geteilte Obhut mit Unterhaltsvereinbarung nicht möglich")
					.oldFamiliensituation(config.oldFamiliensituation)
					.newFamiliensituation(setupKonkubinatOhneKindNichtGeteilteObhut(
						eingangsdatum,
						UnterhaltsvereinbarungAnswer.UNTERHALTSVEREINBARUNG_NICHT_MOEGLICH))
					.build(),
				FamSitChangeTestConfiguration.builder()
					.name('[' + config.name + "]  to Alleinerziehend mit geteilter Obhut und alleinigem Gesuch")
					.oldFamiliensituation(config.oldFamiliensituation)
					.newFamiliensituation(setupAlleinerziehenddGeteilteObhut(
						eingangsdatum,
						EnumGesuchstellerKardinalitaet.ALLEINE))
					.build(),
				FamSitChangeTestConfiguration.builder()
					.name('[' + config.name + "]  to Alleinerziehend ohne geteilte Obhut mit Unterhaltsvereinbarung")
					.oldFamiliensituation(config.oldFamiliensituation)
					.newFamiliensituation(setupAlleinerziehendNichtGeteilteObhut(
						eingangsdatum,
						UnterhaltsvereinbarungAnswer.JA_UNTERHALTSVEREINBARUNG))
					.build(),
				FamSitChangeTestConfiguration.builder()
					.name('['
						+ config.name
						+ "]  to Alleinerziehend ohne geteilte Obhut mit Unterhaltsvereinbarung nicht möglich")
					.oldFamiliensituation(config.oldFamiliensituation)
					.newFamiliensituation(setupAlleinerziehendNichtGeteilteObhut(
						eingangsdatum,
						UnterhaltsvereinbarungAnswer.UNTERHALTSVEREINBARUNG_NICHT_MOEGLICH))
					.build(),
				FamSitChangeTestConfiguration.builder()
					.name('[' + config.name + "]  to Konkubinat ohne Kind mit geteilter Obhut und gemeinsamen Gesuch")
					.oldFamiliensituation(config.oldFamiliensituation)
					.newFamiliensituation(setupKonkubinatOhneKindGeteilteObhut(
						eingangsdatum,
						EnumGesuchstellerKardinalitaet.ZU_ZWEIT))
					.build(),
				FamSitChangeTestConfiguration.builder()
					.name('[' + config.name + "]  to Konkubinat ohne Kind ohne geteilte Obhut ohne Unterhaltsvereinbarung")
					.oldFamiliensituation(config.oldFamiliensituation)
					.newFamiliensituation(setupKonkubinatOhneKindNichtGeteilteObhut(
						eingangsdatum,
						UnterhaltsvereinbarungAnswer.NEIN_UNTERHALTSVEREINBARUNG))
					.build(),
				FamSitChangeTestConfiguration.builder()
					.name('[' + config.name + "]  to Alleinerziehend mit geteilter Obhut und gemeinsamen Gesuch")
					.oldFamiliensituation(config.oldFamiliensituation)
					.newFamiliensituation(setupAlleinerziehenddGeteilteObhut(
						eingangsdatum,
						EnumGesuchstellerKardinalitaet.ZU_ZWEIT))
					.build(),
				FamSitChangeTestConfiguration.builder()
					.name('[' + config.name + "]  to Alleinerziehend ohne geteilte Obhut ohne Unterhaltsvereinbarung")
					.oldFamiliensituation(config.oldFamiliensituation)
					.newFamiliensituation(setupAlleinerziehendNichtGeteilteObhut(
						eingangsdatum,
						UnterhaltsvereinbarungAnswer.NEIN_UNTERHALTSVEREINBARUNG))
					.build(),
				FamSitChangeTestConfiguration.builder()
					.name('[' + config.name + "] to Konkubinat mit Kind")
					.oldFamiliensituation(config.oldFamiliensituation)
					.newFamiliensituation(setupFamiliensituation(EnumFamilienstatus.KONKUBINAT))
					.build(),
				FamSitChangeTestConfiguration.builder()
					.name('[' + config.name + "]  to Long Konkubinat Ohne Kind")
					.oldFamiliensituation(config.oldFamiliensituation)
					.newFamiliensituation(setupLongKonkubinat())
					.build()
			)).map(Arguments::of);
		}

		@Nonnull
		private Gesuch setupMutationWith2GS() {
			Gesuch gesuch = TestDataUtil.createDefaultGesuch();
			Gesuch mutation =
				TestDataUtil.createMutation(gesuch.getDossier(), gesuch.getGesuchsperiode(), AntragStatus.IN_BEARBEITUNG_GS, 1);
			mutation.setFamiliensituationContainer(new FamiliensituationContainer());
			mutation.setEingangsdatum(gesuch.getGesuchsperiode().getGueltigkeit().getGueltigAb().plusMonths(1));
			mutation.setRegelnGueltigAb(mutation.getEingangsdatum());
			final GesuchstellerContainer gs1Container = TestDataUtil.createDefaultGesuchstellerContainer();
			gs1Container.getErwerbspensenContainers().add(TestDataUtil.createErwerbspensumContainer());
			final GesuchstellerContainer gs2Container = TestDataUtil.createDefaultGesuchstellerContainer();
			gs2Container.getErwerbspensenContainers().add(TestDataUtil.createErwerbspensumContainer());
			mutation.setGesuchsteller1(gs1Container);
			mutation.setGesuchsteller2(gs2Container);
			mutation.setFinSitTyp(FinanzielleSituationTyp.BERN_FKJV);
			return mutation;
		}
	}

	@TestInstance(Lifecycle.PER_CLASS)
	@Nested
	class MutationBeforeGPTest {

		private final LocalDate eingangsdatum = TestDataUtil.START_PERIODE.minusMonths(1);

		@ParameterizedTest(name = "to {0}")
		@MethodSource("provideFamiliensituationWithGS2NotToRemove")
		@DisplayName("GS2 should not be removed on change from Verheiratet")
		void testParameterizedChangeShouldNotRemove(FamSitChangeTestConfiguration configuration) {
			Gesuch gesuch = setupMutationWith2GS();
			getFamiliensituationContainerNullSafe(gesuch).setFamiliensituationJA(configuration.newFamiliensituation);

			handler.removeGS2DataOnChangeFrom2To1GS(
				gesuch,
				configuration.newFamiliensituation,
				getFamiliensituationContainerNullSafe(gesuch),
				configuration.oldFamiliensituation);

			assertThat(gesuch.getGesuchsteller2(), notNullValue());
		}

		@ParameterizedTest(name = "{0}")
		@MethodSource("provideFamiliensituationWithGS2ToRemove")
		@DisplayName("GS2 should be removed on change from")
		void testParameterizedChangeShouldRemove(FamSitChangeTestConfiguration configuration) {
			Gesuch gesuch = setupMutationWith2GS();
			getFamiliensituationContainerNullSafe(gesuch).setFamiliensituationJA(configuration.newFamiliensituation);

			handler.removeGS2DataOnChangeFrom2To1GS(
				gesuch,
				configuration.newFamiliensituation,
				getFamiliensituationContainerNullSafe(gesuch),
				configuration.oldFamiliensituation);

			assertThat(gesuch.getGesuchsteller2(), nullValue());
		}

		private Stream<Arguments> provideFamiliensituationWithGS2NotToRemove() {
			return Stream.of(
				FamSitChangeTestConfiguration.of("Verheiratet", setupFamiliensituation(EnumFamilienstatus.VERHEIRATET)),
				FamSitChangeTestConfiguration.of(
					"Alleinerziehend mit geteilter Obhut zu zweit",
					setupAlleinerziehenddGeteilteObhut(eingangsdatum, EnumGesuchstellerKardinalitaet.ZU_ZWEIT)),
				FamSitChangeTestConfiguration.of(
					"Konkubinat ohne Kind mit geteilter Obhut zu zweit",
					setupKonkubinatOhneKindGeteilteObhut(eingangsdatum, EnumGesuchstellerKardinalitaet.ZU_ZWEIT)),
				FamSitChangeTestConfiguration.of(
					"Alleinerziehend alleinige Obhut ohne Unterhaltsvereinbarung",
					setupAlleinerziehendNichtGeteilteObhut(
						eingangsdatum,
						UnterhaltsvereinbarungAnswer.NEIN_UNTERHALTSVEREINBARUNG)),
				FamSitChangeTestConfiguration.of(
					"Konkubinat ohne Kind alleinige Obhut ohne Unterhaltsvereinbarung",
					setupKonkubinatOhneKindNichtGeteilteObhut(
						eingangsdatum,
						UnterhaltsvereinbarungAnswer.NEIN_UNTERHALTSVEREINBARUNG))
			).flatMap(config -> Stream.of(
				FamSitChangeTestConfiguration.builder()
					.name('[' + config.name + "] to Konkubinat mit Kind")
					.oldFamiliensituation(config.oldFamiliensituation)
					.newFamiliensituation(setupFamiliensituation(EnumFamilienstatus.KONKUBINAT))
					.build()
			)).map(Arguments::of);
		}

		private Stream<Arguments> provideFamiliensituationWithGS2ToRemove() {
			return Stream.of(
					FamSitChangeTestConfiguration.of("Verheiratet", setupFamiliensituation(EnumFamilienstatus.VERHEIRATET)),
					FamSitChangeTestConfiguration.of(
						"Alleinerziehend mit geteilter Obhut zu zweit",
						setupAlleinerziehenddGeteilteObhut(eingangsdatum, EnumGesuchstellerKardinalitaet.ZU_ZWEIT)),
					FamSitChangeTestConfiguration.of(
						"Konkubinat ohne Kind mit geteilter Obhut zu zweit",
						setupKonkubinatOhneKindGeteilteObhut(eingangsdatum, EnumGesuchstellerKardinalitaet.ZU_ZWEIT)),
					FamSitChangeTestConfiguration.of(
						"Alleinerziehend alleinige Obhut ohne Unterhaltsvereinbarung",
						setupAlleinerziehendNichtGeteilteObhut(
							eingangsdatum,
							UnterhaltsvereinbarungAnswer.NEIN_UNTERHALTSVEREINBARUNG)),
					FamSitChangeTestConfiguration.of(
						"Konkubinat ohne Kind alleinige Obhut ohne Unterhaltsvereinbarung",
						setupKonkubinatOhneKindNichtGeteilteObhut(
							eingangsdatum,
							UnterhaltsvereinbarungAnswer.NEIN_UNTERHALTSVEREINBARUNG))
				).flatMap(this::getFamSitWithGS2ToRemoveTestConfigurations)
				.map(Arguments::of);
		}

		@Nonnull
		private Stream<FamSitChangeTestConfiguration> getFamSitWithGS2ToRemoveTestConfigurations(FamSitChangeTestConfiguration config) {
			return Stream.of(
				FamSitChangeTestConfiguration.builder()
					.name('[' + config.name + "] to Konkubinat ohne Kind mit geteilter Obhut und alleinigem Gesuch")
					.oldFamiliensituation(config.oldFamiliensituation)
					.newFamiliensituation(setupKonkubinatOhneKindGeteilteObhut(
						eingangsdatum,
						EnumGesuchstellerKardinalitaet.ALLEINE))
					.build(),
				FamSitChangeTestConfiguration.builder()
					.name('[' + config.name + "]  to Konkubinat ohne Kind ohne geteilte Obhut mit Unterhaltsvereinbarung")
					.oldFamiliensituation(config.oldFamiliensituation)
					.newFamiliensituation(setupKonkubinatOhneKindNichtGeteilteObhut(
						eingangsdatum,
						UnterhaltsvereinbarungAnswer.JA_UNTERHALTSVEREINBARUNG))
					.build(),
				FamSitChangeTestConfiguration.builder()
					.name('['
						+ config.name
						+ "]  to Konkubinat ohne Kind ohne geteilte Obhut mit Unterhaltsvereinbarung nicht möglich")
					.oldFamiliensituation(config.oldFamiliensituation)
					.newFamiliensituation(setupKonkubinatOhneKindNichtGeteilteObhut(
						eingangsdatum,
						UnterhaltsvereinbarungAnswer.UNTERHALTSVEREINBARUNG_NICHT_MOEGLICH))
					.build(),
				FamSitChangeTestConfiguration.builder()
					.name('[' + config.name + "]  to Alleinerziehend mit geteilter Obhut und alleinigem Gesuch")
					.oldFamiliensituation(config.oldFamiliensituation)
					.newFamiliensituation(setupAlleinerziehenddGeteilteObhut(
						eingangsdatum,
						EnumGesuchstellerKardinalitaet.ALLEINE))
					.build(),
				FamSitChangeTestConfiguration.builder()
					.name('[' + config.name + "]  to Alleinerziehend ohne geteilte Obhut mit Unterhaltsvereinbarung")
					.oldFamiliensituation(config.oldFamiliensituation)
					.newFamiliensituation(setupAlleinerziehendNichtGeteilteObhut(
						eingangsdatum,
						UnterhaltsvereinbarungAnswer.JA_UNTERHALTSVEREINBARUNG))
					.build(),
				FamSitChangeTestConfiguration.builder()
					.name('['
						+ config.name
						+ "]  to Alleinerziehend ohne geteilte Obhut mit Unterhaltsvereinbarung nicht möglich")
					.oldFamiliensituation(config.oldFamiliensituation)
					.newFamiliensituation(setupAlleinerziehendNichtGeteilteObhut(
						eingangsdatum,
						UnterhaltsvereinbarungAnswer.UNTERHALTSVEREINBARUNG_NICHT_MOEGLICH))
					.build(),
				FamSitChangeTestConfiguration.builder()
					.name('[' + config.name + "]  to Konkubinat ohne Kind mit geteilter Obhut und gemeinsamen Gesuch")
					.oldFamiliensituation(config.oldFamiliensituation)
					.newFamiliensituation(setupKonkubinatOhneKindGeteilteObhut(
						eingangsdatum,
						EnumGesuchstellerKardinalitaet.ZU_ZWEIT))
					.build(),
				FamSitChangeTestConfiguration.builder()
					.name('[' + config.name + "]  to Konkubinat ohne Kind ohne geteilte Obhut ohne Unterhaltsvereinbarung")
					.oldFamiliensituation(config.oldFamiliensituation)
					.newFamiliensituation(setupKonkubinatOhneKindNichtGeteilteObhut(
						eingangsdatum,
						UnterhaltsvereinbarungAnswer.NEIN_UNTERHALTSVEREINBARUNG))
					.build(),
				FamSitChangeTestConfiguration.builder()
					.name('[' + config.name + "]  to Alleinerziehend mit geteilter Obhut und gemeinsamen Gesuch")
					.oldFamiliensituation(config.oldFamiliensituation)
					.newFamiliensituation(setupAlleinerziehenddGeteilteObhut(
						eingangsdatum,
						EnumGesuchstellerKardinalitaet.ZU_ZWEIT))
					.build(),
				FamSitChangeTestConfiguration.builder()
					.name('[' + config.name + "]  to Alleinerziehend ohne geteilte Obhut ohne Unterhaltsvereinbarung")
					.oldFamiliensituation(config.oldFamiliensituation)
					.newFamiliensituation(setupAlleinerziehendNichtGeteilteObhut(
						eingangsdatum,
						UnterhaltsvereinbarungAnswer.NEIN_UNTERHALTSVEREINBARUNG)).build(),
				FamSitChangeTestConfiguration.builder()
					.name('[' + config.name + "]  to Long Konkubinat Ohne Kind")
					.oldFamiliensituation(config.oldFamiliensituation)
					.newFamiliensituation(setupLongKonkubinat())
					.build()
			);
		}

		@Nonnull
		private Gesuch setupMutationWith2GS() {
			Gesuch gesuch = TestDataUtil.createDefaultGesuch();
			Gesuch mutation =
				TestDataUtil.createMutation(gesuch.getDossier(), gesuch.getGesuchsperiode(), AntragStatus.IN_BEARBEITUNG_GS, 1);
			mutation.setFamiliensituationContainer(new FamiliensituationContainer());
			mutation.setEingangsdatum(eingangsdatum);
			mutation.setRegelnGueltigAb(mutation.getEingangsdatum());
			final GesuchstellerContainer gs1Container = TestDataUtil.createDefaultGesuchstellerContainer();
			gs1Container.getErwerbspensenContainers().add(TestDataUtil.createErwerbspensumContainer());
			final GesuchstellerContainer gs2Container = TestDataUtil.createDefaultGesuchstellerContainer();
			gs2Container.getErwerbspensenContainers().add(TestDataUtil.createErwerbspensumContainer());
			mutation.setGesuchsteller1(gs1Container);
			mutation.setGesuchsteller2(gs2Container);
			mutation.setFinSitTyp(FinanzielleSituationTyp.BERN_FKJV);
			return mutation;
		}
	}

	@Nonnull
	private static Familiensituation setupKonkubinatOhneKindGeteilteObhut(
		LocalDate konkubinatStart,
		EnumGesuchstellerKardinalitaet gesuchstellerKardinalitaet) {
		final Familiensituation newFamiliensituation = setupFamiliensituation(EnumFamilienstatus.KONKUBINAT_KEIN_KIND);
		newFamiliensituation.setStartKonkubinat(konkubinatStart);
		newFamiliensituation.setAenderungPer(konkubinatStart);
		newFamiliensituation.setGeteilteObhut(true);
		newFamiliensituation.setGesuchstellerKardinalitaet(gesuchstellerKardinalitaet);
		return newFamiliensituation;
	}

	@Nonnull
	private static Familiensituation setupKonkubinatOhneKindNichtGeteilteObhut(
		LocalDate konkubinatStart,
		UnterhaltsvereinbarungAnswer unterhaltsvereinbarung) {
		final Familiensituation newFamiliensituation = setupFamiliensituation(EnumFamilienstatus.KONKUBINAT_KEIN_KIND);
		newFamiliensituation.setStartKonkubinat(konkubinatStart);
		newFamiliensituation.setAenderungPer(konkubinatStart);
		newFamiliensituation.setGeteilteObhut(false);
		newFamiliensituation.setUnterhaltsvereinbarung(unterhaltsvereinbarung);
		return newFamiliensituation;
	}

	@Nonnull
	private static Familiensituation setupAlleinerziehenddGeteilteObhut(
		LocalDate konkubinatStart,
		EnumGesuchstellerKardinalitaet gesuchstellerKardinalitaet) {
		final Familiensituation newFamiliensituation = setupFamiliensituation(EnumFamilienstatus.KONKUBINAT_KEIN_KIND);
		newFamiliensituation.setStartKonkubinat(konkubinatStart);
		newFamiliensituation.setAenderungPer(konkubinatStart);
		newFamiliensituation.setGeteilteObhut(true);
		newFamiliensituation.setGesuchstellerKardinalitaet(gesuchstellerKardinalitaet);
		return newFamiliensituation;
	}

	@Nonnull
	private static Familiensituation setupAlleinerziehendNichtGeteilteObhut(
		LocalDate konkubinatStart,
		UnterhaltsvereinbarungAnswer unterhaltsvereinbarung) {
		final Familiensituation newFamiliensituation = setupFamiliensituation(EnumFamilienstatus.KONKUBINAT_KEIN_KIND);
		newFamiliensituation.setStartKonkubinat(konkubinatStart);
		newFamiliensituation.setAenderungPer(konkubinatStart);
		newFamiliensituation.setGeteilteObhut(false);
		newFamiliensituation.setUnterhaltsvereinbarung(unterhaltsvereinbarung);
		return newFamiliensituation;
	}

	@Nonnull
	private static FamiliensituationContainer getFamiliensituationContainerNullSafe(Gesuch gesuch) {
		return Objects.requireNonNull(gesuch.getFamiliensituationContainer());
	}

	@Nonnull
	private static Familiensituation setupFamiliensituation(EnumFamilienstatus familienstatus) {
		Familiensituation familiensituation = new Familiensituation();
		familiensituation.setFkjvFamSit(true);
		familiensituation.setFamilienstatus(familienstatus);
		return familiensituation;
	}

	private static Familiensituation setupLongKonkubinat() {
		Familiensituation familiensituation = new Familiensituation();
		familiensituation.setFkjvFamSit(true);
		familiensituation.setFamilienstatus(EnumFamilienstatus.KONKUBINAT_KEIN_KIND);
		familiensituation.setStartKonkubinat(Constants.START_OF_TIME);
		familiensituation.setAenderungPer(Constants.START_OF_TIME);
		return familiensituation;
	}


	@Getter
	@Setter
	@Builder
	private static class FamSitChangeTestConfiguration {
		Familiensituation oldFamiliensituation;
		Familiensituation newFamiliensituation;
		String name;

		@Override
		public String toString() {
			return this.name;
		}

		static FamSitChangeTestConfiguration of(String name, Familiensituation oldFamiliensituation) {
			return FamSitChangeTestConfiguration.builder().name(name).oldFamiliensituation(oldFamiliensituation).build();
		}
	}


}
