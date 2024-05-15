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
import ch.dvbern.ebegu.enums.EnumGesuchstellerKardinalitaet;
import ch.dvbern.ebegu.enums.Geschlecht;
import ch.dvbern.ebegu.services.EinstellungService;
import ch.dvbern.ebegu.services.FinanzielleSituationService;
import ch.dvbern.ebegu.services.GesuchstellerService;
import ch.dvbern.ebegu.test.TestDataUtil;
import ch.dvbern.ebegu.testfaelle.dataprovider.SchwyzTestfallDataProvider;
import org.easymock.EasyMockSupport;
import org.easymock.Mock;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class FamSitChangeHandlerSchwyzTest extends EasyMockSupport {

	@Mock
	GesuchstellerService gesuchstellerService;

	@Mock
	EinstellungService einstellungService;

	@Mock
	FinanzielleSituationService finanzielleSituationService;

	FamSitChangeHandlerSchwyz testee =
		new FamSitChangeHandlerSchwyz(gesuchstellerService, einstellungService, finanzielleSituationService);

	@Nested
	class KinderabzugResetTest {

		@Test
		void shouldNotsetInPruefungIfOldFamSitIsNull() {
			Gesuch gesuch = new Gesuch();
			gesuch.setFamiliensituationContainer(createFamSitContainer(EnumGesuchstellerKardinalitaet.ALLEINE));
			KindContainer kind = createDefaultKind();
			KindContainer before = createDefaultKind();
			gesuch.getKindContainers().add(kind);

			testee.handlePossibleKinderabzugFragenReset(gesuch, getFamiliensituationJA(gesuch), null);

			assertThat(before.getKindJA().getInPruefung(), is(false));
			assertThat(kind.getKindJA().getInPruefung(), is(false));
		}

		@Test
		void shouldResetOnChangeFromAlleineToZuZweit() {
			Gesuch gesuch = new Gesuch();
			gesuch.setFamiliensituationContainer(createFamSitContainer(EnumGesuchstellerKardinalitaet.ALLEINE));
			KindContainer kind = createDefaultKind();
			KindContainer before = createDefaultKind();
			gesuch.getKindContainers().add(kind);

			testee.handlePossibleKinderabzugFragenReset(
				gesuch,
				getFamiliensituationJA(gesuch),
				getFamiliensituationJA(createFamSitContainer(EnumGesuchstellerKardinalitaet.ZU_ZWEIT)));

			assertThat(before.getKindJA().getInPruefung(), is(false));
			assertThat(kind.getKindJA().getInPruefung(), is(true));
		}

		@Test
		void shouldResetOnChangeFromZuZweitToAlleine() {
			Gesuch gesuch = new Gesuch();
			gesuch.setFamiliensituationContainer(createFamSitContainer(EnumGesuchstellerKardinalitaet.ZU_ZWEIT));
			KindContainer kind = createDefaultKind();
			KindContainer copy = createDefaultKind();
			gesuch.getKindContainers().add(kind);

			testee.handlePossibleKinderabzugFragenReset(
				gesuch,
				getFamiliensituationJA(gesuch),
				getFamiliensituationJA(createFamSitContainer(EnumGesuchstellerKardinalitaet.ALLEINE)));

			assertThat(copy.getKindJA().getInPruefung(), is(false));
			assertThat(kind.getKindJA().getInPruefung(), is(true));
		}

		private KindContainer createDefaultKind() {
			KindContainer kindContainer = new KindContainer();
			Kind kind = new Kind();
			SchwyzTestfallDataProvider.setRequiredKindData(
				kind,
				Geschlecht.WEIBLICH,
				"Lara",
				"Testkind",
				TestDataUtil.START_PERIODE.minusYears(5),
				true);
			kindContainer.setKindJA(kind);
			return kindContainer;
		}

	}

	private static Familiensituation getFamiliensituationJA(FamiliensituationContainer famSitContainer) {
		return Objects.requireNonNull(famSitContainer.getFamiliensituationJA());
	}

	private static Familiensituation getFamiliensituationJA(Gesuch gesuch) {
		return getFamiliensituationJA(Objects.requireNonNull(gesuch.getFamiliensituationContainer()));
	}

	@Nonnull
	private static FamiliensituationContainer createFamSitContainer(EnumGesuchstellerKardinalitaet gesuchstellerKardinalitaet) {
		final FamiliensituationContainer familiensituationContainer = TestDataUtil.createDefaultFamiliensituationContainer();
		Objects.requireNonNull(familiensituationContainer.getFamiliensituationJA());
		familiensituationContainer.getFamiliensituationJA().setFamilienstatus(EnumFamilienstatus.SCHWYZ);
		familiensituationContainer.getFamiliensituationJA().setGesuchstellerKardinalitaet(gesuchstellerKardinalitaet);
		return familiensituationContainer;
	}


}
