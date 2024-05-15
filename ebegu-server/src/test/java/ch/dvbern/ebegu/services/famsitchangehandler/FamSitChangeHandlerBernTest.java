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

import java.util.Objects;

import javax.annotation.Nonnull;

import ch.dvbern.ebegu.entities.Familiensituation;
import ch.dvbern.ebegu.entities.FamiliensituationContainer;
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.entities.Kind;
import ch.dvbern.ebegu.entities.KindContainer;
import ch.dvbern.ebegu.enums.EnumFamilienstatus;
import ch.dvbern.ebegu.enums.FinanzielleSituationTyp;
import ch.dvbern.ebegu.enums.Geschlecht;
import ch.dvbern.ebegu.enums.Kinderabzug;
import ch.dvbern.ebegu.services.EinstellungService;
import ch.dvbern.ebegu.services.GesuchstellerService;
import ch.dvbern.ebegu.test.TestDataUtil;
import ch.dvbern.ebegu.testfaelle.dataprovider.AbstractTestfallDataProvider;
import org.easymock.EasyMockSupport;
import org.easymock.Mock;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.EnumSource.Mode;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class FamSitChangeHandlerBernTest extends EasyMockSupport {

	@Mock
	GesuchstellerService gesuchstellerService;

	@Mock
	EinstellungService einstellungService;

	SharedFamSitChangeDefaultHandler testee =
		new SharedFamSitChangeDefaultHandler(gesuchstellerService, einstellungService);

	@Nested
	class KinderabzugResetTest {

		@Test
		void shouldNotResetIfOldFamSitIfNull() {
			Gesuch gesuch = setupGesuch();
			gesuch.setFamiliensituationContainer(createFamSitContainer(EnumFamilienstatus.VERHEIRATET));
			KindContainer before = createDefaultKind();
			KindContainer kind = createDefaultKind();
			gesuch.getKindContainers().add(kind);

			testee.handlePossibleKinderabzugFragenReset(gesuch, getFamiliensituationJA(gesuch), null);

			assertThat(before.getKindJA().getInPruefung(), is(false));
			assertThat(kind.getKindJA().getInPruefung(), is(false));
		}

		@ParameterizedTest
		@EnumSource(value = EnumFamilienstatus.class, mode = Mode.MATCH_ALL)
		void shouldNotResetOnSameStatus(EnumFamilienstatus familienstatus) {
			Gesuch gesuch = setupGesuch();
			gesuch.setFamiliensituationContainer(createFamSitContainer(familienstatus));
			KindContainer before = createDefaultKind();
			KindContainer kind = createDefaultKind();
			gesuch.getKindContainers().add(kind);

			testee.handlePossibleKinderabzugFragenReset(
				gesuch,
				getFamiliensituationJA(gesuch),
				getFamiliensituationJA(createFamSitContainer(familienstatus)));

			assertThat(before.getKindJA().getInPruefung(), is(false));
			assertThat(kind.getKindJA().getInPruefung(), is(false));
		}

		@ParameterizedTest
		@EnumSource(value = EnumFamilienstatus.class, names = {"VERHEIRATET"}, mode = Mode.EXCLUDE)
		void shouldResetAndSetInPruefungInStatusChangeFromVerheiratetTo(EnumFamilienstatus newStatus) {
			Gesuch gesuch = setupGesuch();
			gesuch.setFamiliensituationContainer(createFamSitContainer(newStatus));
			KindContainer before = createDefaultKind();
			KindContainer kind = createDefaultKind();
			gesuch.getKindContainers().add(kind);

			testee.handlePossibleKinderabzugFragenReset(
				gesuch,
				getFamiliensituationJA(gesuch),
				getFamiliensituationJA(createFamSitContainer(EnumFamilienstatus.VERHEIRATET)));

			assertThat(before.getKindJA().getInPruefung(), is(false));
			assertThat(kind.getKindJA().getInPruefung(), is(true));
		}

		@ParameterizedTest
		@EnumSource(value = EnumFamilienstatus.class, names = {"KONKUBINAT"}, mode = Mode.EXCLUDE)
		void shouldResetAndSetInPruefungInStatusChangeFromKonkubinat(EnumFamilienstatus newStatus) {

			Gesuch gesuch = setupGesuch();
			gesuch.setFamiliensituationContainer(createFamSitContainer(newStatus));
			KindContainer before = createDefaultKind();
			KindContainer kind = createDefaultKind();
			gesuch.getKindContainers().add(kind);

			testee.handlePossibleKinderabzugFragenReset(
				gesuch,
				getFamiliensituationJA(gesuch),
				getFamiliensituationJA(createFamSitContainer(EnumFamilienstatus.KONKUBINAT)));

			assertThat(before.getKindJA().getInPruefung(), is(false));
			assertThat(kind.getKindJA().getInPruefung(), is(true));
		}

		@ParameterizedTest
		@EnumSource(value = EnumFamilienstatus.class, names = {"KONKUBINAT_KEIN_KIND"}, mode = Mode.EXCLUDE)
		void shouldResetAndSetInPruefungInStatusChangeFromKonkubinatKeinKind(EnumFamilienstatus newStatus) {

			Gesuch gesuch = setupGesuch();
			gesuch.setFamiliensituationContainer(createFamSitContainer(newStatus));
			KindContainer before = createDefaultKind();
			KindContainer kind = createDefaultKind();
			gesuch.getKindContainers().add(kind);

			testee.handlePossibleKinderabzugFragenReset(
				gesuch,
				getFamiliensituationJA(gesuch),
				getFamiliensituationJA(createFamSitContainer(EnumFamilienstatus.KONKUBINAT_KEIN_KIND)));

			assertThat(before.getKindJA().getInPruefung(), is(false));
			assertThat(kind.getKindJA().getInPruefung(), is(true));
		}

		@ParameterizedTest
		@EnumSource(value = EnumFamilienstatus.class, names = {"ALLEINERZIEHEND"}, mode = Mode.EXCLUDE)
		void shouldResetAndSetInPruefungInStatusChangeFromAlleinerziehend(EnumFamilienstatus newStatus) {

			Gesuch gesuch = setupGesuch();
			gesuch.setFamiliensituationContainer(createFamSitContainer(newStatus));
			KindContainer before = createDefaultKind();
			KindContainer kind = createDefaultKind();
			gesuch.getKindContainers().add(kind);

			testee.handlePossibleKinderabzugFragenReset(
				gesuch,
				getFamiliensituationJA(gesuch),
				getFamiliensituationJA(createFamSitContainer(EnumFamilienstatus.ALLEINERZIEHEND)));

			assertThat(before.getKindJA().getInPruefung(), is(false));
			assertThat(kind.getKindJA().getInPruefung(), is(true));
		}

		private KindContainer createDefaultKind() {
			KindContainer kindContainer = new KindContainer();
			Kind kind = new Kind();
			AbstractTestfallDataProvider.setRequiredKindData(
				kind,
				Geschlecht.WEIBLICH,
				"Lara",
				"Testkind",
				TestDataUtil.START_PERIODE.minusYears(5),
				false,
				Kinderabzug.GANZER_ABZUG,
				true);
			kindContainer.setKindJA(kind);
			return kindContainer;
		}
	}

	private static Gesuch setupGesuch() {
		Gesuch gesuch = new Gesuch();
		gesuch.setFinSitTyp(FinanzielleSituationTyp.BERN_FKJV);
		return gesuch;
	}

	private static Familiensituation getFamiliensituationJA(FamiliensituationContainer famSitContainer) {
		return Objects.requireNonNull(famSitContainer.getFamiliensituationJA());
	}

	private static Familiensituation getFamiliensituationJA(Gesuch gesuch) {
		return getFamiliensituationJA(Objects.requireNonNull(gesuch.getFamiliensituationContainer()));
	}

	@Nonnull
	private static FamiliensituationContainer createFamSitContainer(EnumFamilienstatus familienstatus) {
		final FamiliensituationContainer familiensituationContainer = TestDataUtil.createDefaultFamiliensituationContainer();
		Objects.requireNonNull(familiensituationContainer.getFamiliensituationJA());
		familiensituationContainer.getFamiliensituationJA().setFamilienstatus(familienstatus);
		return familiensituationContainer;
	}
}
