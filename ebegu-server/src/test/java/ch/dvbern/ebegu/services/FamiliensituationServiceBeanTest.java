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

package ch.dvbern.ebegu.services;

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.util.List;

import javax.annotation.Nonnull;

import ch.dvbern.ebegu.entities.Einstellung;
import ch.dvbern.ebegu.entities.Familiensituation;
import ch.dvbern.ebegu.entities.FamiliensituationContainer;
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.entities.GesuchstellerContainer;
import ch.dvbern.ebegu.enums.AntragCopyType;
import ch.dvbern.ebegu.enums.EinstellungKey;
import ch.dvbern.ebegu.enums.EnumFamilienstatus;
import ch.dvbern.ebegu.enums.FinanzielleSituationTyp;
import ch.dvbern.ebegu.enums.UnterhaltsvereinbarungAnswer;
import ch.dvbern.ebegu.services.famsitchangehandler.FamSitChangeHandlerBernBean;
import org.easymock.EasyMockExtension;
import org.easymock.EasyMockSupport;
import ch.dvbern.ebegu.test.TestDataUtil;
import ch.dvbern.lib.cdipersistence.Persistence;
import org.easymock.Mock;
import org.easymock.TestSubject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.easymock.EasyMock.anyObject;
import static org.easymock.EasyMock.anyString;
import static org.easymock.EasyMock.expect;
import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

@ExtendWith(EasyMockExtension.class)
class FamiliensituationServiceBeanTest extends EasyMockSupport {

	@TestSubject
	private final FamiliensituationServiceBean familiensituationServiceBean = new FamiliensituationServiceBean();

	@Mock
	private WizardStepService wizardStepService;

	@Mock
	private EinstellungService einstellungService;

	@Mock
	private Persistence persistence;

	@Mock
	private GesuchstellerService gesuchstellerService;

	@BeforeEach
	void setup() throws NoSuchFieldException, IllegalAccessException {
		setPrivateField(familiensituationServiceBean, "famSitChangeHandler", new FamSitChangeHandlerBernBean(gesuchstellerService, einstellungService));
	}

	@Test
	void shouldNotRemoveGS2ErwerbspensumOnChangeToKonkubinatMitKind() {
		Gesuch gesuch = setupGesuch();
		Familiensituation familiensituation = setupFamiliensituation(gesuch, EnumFamilienstatus.VERHEIRATET);
		assertThat(gesuch.getFamiliensituationContainer(), notNullValue());
		assertThat(gesuch.getGesuchsteller2(), notNullValue());
		mockCalls(gesuch, "true");

		Familiensituation newFamiliensituation =
			familiensituation.copyFamiliensituation(new Familiensituation(), AntragCopyType.MUTATION);
		newFamiliensituation.setFamilienstatus(EnumFamilienstatus.KONKUBINAT);
		gesuch.getFamiliensituationContainer().setFamiliensituationJA(newFamiliensituation);

		assertThat(1, is(gesuch.getGesuchsteller2().getErwerbspensenContainers().size()));

		familiensituationServiceBean.saveFamiliensituation(gesuch, gesuch.getFamiliensituationContainer(),
				familiensituation);

		assertThat(gesuch.getGesuchsteller2(), notNullValue());
		assertThat(1, is(gesuch.getGesuchsteller2().getErwerbspensenContainers().size()));
	}

	@Test
	void shouldNotRemoveGS2ErwerbspensumOnChangeToLongKonkubinatOhneKind() {
		Gesuch gesuch = setupGesuch();
		Familiensituation familiensituation = setupFamiliensituation(gesuch, EnumFamilienstatus.VERHEIRATET);
		assertThat(gesuch.getFamiliensituationContainer(), notNullValue());
		assertThat(gesuch.getGesuchsteller2(), notNullValue());
		mockCalls(gesuch, "true");

		Familiensituation newFamiliensituation =
			familiensituation.copyFamiliensituation(new Familiensituation(), AntragCopyType.MUTATION);
		newFamiliensituation.setFamilienstatus(EnumFamilienstatus.KONKUBINAT_KEIN_KIND);
		newFamiliensituation.setStartKonkubinat(LocalDate.of(2010, 1,1));
		newFamiliensituation.setMinDauerKonkubinat(2);
		gesuch.getFamiliensituationContainer().setFamiliensituationJA(newFamiliensituation);

		assertThat(1, is(gesuch.getGesuchsteller2().getErwerbspensenContainers().size()));

		familiensituationServiceBean.saveFamiliensituation(gesuch, gesuch.getFamiliensituationContainer(),
				familiensituation);

		assertThat(gesuch.getGesuchsteller2(), notNullValue());
		assertThat(1, is(gesuch.getGesuchsteller2().getErwerbspensenContainers().size()));
	}

	@Test
	void shouldNotRemoveGS2ErwerbspensumOnChangeToShortKonkubinatOhneKindGeteilteObhut() {
		Gesuch gesuch = setupGesuch();
		Familiensituation familiensituation = setupFamiliensituation(gesuch, EnumFamilienstatus.VERHEIRATET);
		assertThat(gesuch.getFamiliensituationContainer(), notNullValue());
		assertThat(gesuch.getGesuchsteller2(), notNullValue());
		mockCalls(gesuch, "true");

		Familiensituation newFamiliensituation =
			familiensituation.copyFamiliensituation(new Familiensituation(), AntragCopyType.MUTATION);
		newFamiliensituation.setFamilienstatus(EnumFamilienstatus.KONKUBINAT_KEIN_KIND);
		newFamiliensituation.setStartKonkubinat(LocalDate.of(2016, 1,1));
		newFamiliensituation.setMinDauerKonkubinat(2);
		newFamiliensituation.setGeteilteObhut(true);
		gesuch.getFamiliensituationContainer().setFamiliensituationJA(newFamiliensituation);

		assertThat(1, is(gesuch.getGesuchsteller2().getErwerbspensenContainers().size()));

		familiensituationServiceBean.saveFamiliensituation(gesuch, gesuch.getFamiliensituationContainer(),
				familiensituation);

		assertThat(gesuch.getGesuchsteller2(), notNullValue());
		assertThat(1, is(gesuch.getGesuchsteller2().getErwerbspensenContainers().size()));
	}

	@Test
	void shouldNotRemoveGS2ErwerbspensumOnChangeToShortKonkubinatOhneKindNichtGeteilteObhutUnterhaltsvereinbarung() {
		Gesuch gesuch = setupGesuch();
		Familiensituation familiensituation = setupFamiliensituation(gesuch, EnumFamilienstatus.VERHEIRATET);
		assertThat(gesuch.getFamiliensituationContainer(), notNullValue());
		assertThat(gesuch.getGesuchsteller2(), notNullValue());
		mockCalls(gesuch, "true");

		Familiensituation newFamiliensituation =
			familiensituation.copyFamiliensituation(new Familiensituation(), AntragCopyType.MUTATION);
		newFamiliensituation.setFamilienstatus(EnumFamilienstatus.KONKUBINAT_KEIN_KIND);
		newFamiliensituation.setStartKonkubinat(LocalDate.of(2016, 1,1));
		newFamiliensituation.setMinDauerKonkubinat(2);
		newFamiliensituation.setGeteilteObhut(false);
		newFamiliensituation.setUnterhaltsvereinbarung(UnterhaltsvereinbarungAnswer.JA_UNTERHALTSVEREINBARUNG);
		gesuch.getFamiliensituationContainer().setFamiliensituationJA(newFamiliensituation);

		assertThat(1, is(gesuch.getGesuchsteller2().getErwerbspensenContainers().size()));

		familiensituationServiceBean.saveFamiliensituation(gesuch, gesuch.getFamiliensituationContainer(),
				familiensituation);

		assertThat(gesuch.getGesuchsteller2(), notNullValue());
		assertThat(1, is(gesuch.getGesuchsteller2().getErwerbspensenContainers().size()));
	}


	@Test
	void shouldRemoveGS2ErwerbspensumOnChangeToShortKonkubinatOhneKindNichtGeteilteObhutKeineUnterhaltsvereinbarung() {
		Gesuch gesuch = setupGesuch();
		Familiensituation familiensituation = setupFamiliensituation(gesuch, EnumFamilienstatus.VERHEIRATET);
		assertThat(gesuch.getFamiliensituationContainer(), notNullValue());
		assertThat(gesuch.getGesuchsteller2(), notNullValue());
		mockCalls(gesuch, "true");

		Familiensituation newFamiliensituation =
			familiensituation.copyFamiliensituation(new Familiensituation(), AntragCopyType.MUTATION);
		newFamiliensituation.setFamilienstatus(EnumFamilienstatus.KONKUBINAT_KEIN_KIND);
		newFamiliensituation.setStartKonkubinat(LocalDate.of(2016, 1,1));
		newFamiliensituation.setMinDauerKonkubinat(2);
		newFamiliensituation.setGeteilteObhut(false);
		newFamiliensituation.setUnterhaltsvereinbarung(UnterhaltsvereinbarungAnswer.NEIN_UNTERHALTSVEREINBARUNG);
		gesuch.getFamiliensituationContainer().setFamiliensituationJA(newFamiliensituation);

		assertThat(1, is(gesuch.getGesuchsteller2().getErwerbspensenContainers().size()));

		familiensituationServiceBean.saveFamiliensituation(gesuch, gesuch.getFamiliensituationContainer(),
				familiensituation);

		assertThat(gesuch.getGesuchsteller2(), notNullValue());
		assertThat(0, is(gesuch.getGesuchsteller2().getErwerbspensenContainers().size()));
	}


	@Test
	void shouldNotRemoveGS2ErwerbspensumOnChangeToShortKonkubinatOhneKindNichtGeteilteObhutKeineUnterhaltsvereinbarungEinstellungDeaktiviert() {
		Gesuch gesuch = setupGesuch();
		Familiensituation familiensituation = setupFamiliensituation(gesuch, EnumFamilienstatus.VERHEIRATET);
		assertThat(gesuch.getFamiliensituationContainer(), notNullValue());
		assertThat(gesuch.getGesuchsteller2(), notNullValue());
		mockCalls(gesuch, "false");

		Familiensituation newFamiliensituation =
			familiensituation.copyFamiliensituation(new Familiensituation(), AntragCopyType.MUTATION);
		newFamiliensituation.setFamilienstatus(EnumFamilienstatus.KONKUBINAT_KEIN_KIND);
		newFamiliensituation.setStartKonkubinat(LocalDate.of(2016, 1,1));
		newFamiliensituation.setMinDauerKonkubinat(2);
		newFamiliensituation.setGeteilteObhut(false);
		newFamiliensituation.setUnterhaltsvereinbarung(UnterhaltsvereinbarungAnswer.NEIN_UNTERHALTSVEREINBARUNG);
		gesuch.getFamiliensituationContainer().setFamiliensituationJA(newFamiliensituation);

		assertThat(1, is(gesuch.getGesuchsteller2().getErwerbspensenContainers().size()));

		familiensituationServiceBean.saveFamiliensituation(gesuch, gesuch.getFamiliensituationContainer(),
				familiensituation);

		assertThat(gesuch.getGesuchsteller2(), notNullValue());
		assertThat(1, is(gesuch.getGesuchsteller2().getErwerbspensenContainers().size()));
	}

	private void mockCalls(Gesuch gesuch, String einstellungValue) {
		assertThat(gesuch.getFamiliensituationContainer(), notNullValue());
		expect(persistence.merge(anyObject(FamiliensituationContainer.class))).andReturn(
			gesuch.getFamiliensituationContainer().copyFamiliensituationContainer(
				new FamiliensituationContainer(),
				AntragCopyType.MUTATION,
				false
			)
		);
		expect(wizardStepService.updateSteps(
			anyString(),
			anyObject(),
			anyObject(),
			anyObject())).andReturn(List.of());
		expect(einstellungService.findEinstellung(
			EinstellungKey.GESUCH_BEENDEN_BEI_TAUSCH_GS2,
			gesuch.extractGemeinde(),
			gesuch.getGesuchsperiode()
		)).andReturn(
			new Einstellung(EinstellungKey.GESUCH_BEENDEN_BEI_TAUSCH_GS2, einstellungValue, gesuch.getGesuchsperiode())
		);
		replayAll();
	}

	@Nonnull
	private static Familiensituation setupFamiliensituation(Gesuch gesuch, EnumFamilienstatus familienstatus) {
		Familiensituation familiensituation = gesuch.extractFamiliensituation();
		assertThat(familiensituation, notNullValue());
		assertThat(gesuch.getFamiliensituationContainer(), notNullValue());
		familiensituation.setFamilienstatus(familienstatus);
		return familiensituation;
	}

	//GP 17/18
	@Nonnull
	private static Gesuch setupGesuch() {
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

	private void setPrivateField(Object object, String fieldName, Object value) throws NoSuchFieldException, IllegalAccessException {
		Field field = object.getClass().getDeclaredField(fieldName);
		field.setAccessible(true);
		field.set(object, value);
	}
}
