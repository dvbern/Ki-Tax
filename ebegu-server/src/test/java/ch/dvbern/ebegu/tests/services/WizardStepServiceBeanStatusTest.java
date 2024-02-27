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

package ch.dvbern.ebegu.tests.services;

import java.util.List;
import java.util.Objects;

import javax.annotation.Nonnull;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import ch.dvbern.ebegu.entities.Einstellung;
import ch.dvbern.ebegu.entities.Familiensituation;
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.entities.Gesuch_;
import ch.dvbern.ebegu.entities.WizardStep;
import ch.dvbern.ebegu.entities.WizardStep_;
import ch.dvbern.ebegu.enums.AntragCopyType;
import ch.dvbern.ebegu.enums.EinstellungKey;
import ch.dvbern.ebegu.enums.EnumGesuchstellerKardinalitaet;
import ch.dvbern.ebegu.enums.WizardStepName;
import ch.dvbern.ebegu.enums.WizardStepStatus;
import ch.dvbern.ebegu.services.Authorizer;
import ch.dvbern.ebegu.services.EinstellungService;
import ch.dvbern.ebegu.services.ErwerbspensumService;
import ch.dvbern.ebegu.services.WizardStepService;
import ch.dvbern.ebegu.services.WizardStepServiceBean;
import ch.dvbern.ebegu.test.TestDataUtil;
import ch.dvbern.lib.cdipersistence.Persistence;
import org.easymock.EasyMock;
import org.easymock.EasyMockExtension;
import org.easymock.EasyMockSupport;
import org.easymock.Mock;
import org.easymock.TestSubject;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;
import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

/**
 * For all tests, we assume that any data on the gesuch is already correctly adapted
 */
@ExtendWith(EasyMockExtension.class)
public class WizardStepServiceBeanStatusTest extends EasyMockSupport {

	@TestSubject
	private final WizardStepService wizardStepService = new WizardStepServiceBean();

	@Mock
	Persistence persistence;

	@Mock
	Authorizer authorizer;

	@Mock
	ErwerbspensumService erwerbspensumService;

	@Mock
	EinstellungService einstellungService;

	@Nested
	class FamilienSituationUpdateTest {

		@Test
		void beschaeftigungspensumShouldBeOKToNOKOnKardinalitaetAlleineToZuZweit() {
			Gesuch gesuch = setupGesuchWithOneGS();
			Familiensituation familiensituation = gesuch.extractFamiliensituation();
			assertThat(familiensituation, notNullValue());
			assertThat(gesuch.getFamiliensituationContainer(), notNullValue());
			familiensituation.setGeteilteObhut(true);
			familiensituation.setGesuchstellerKardinalitaet(EnumGesuchstellerKardinalitaet.ALLEINE);

			Familiensituation newFamiliensituation =
				familiensituation.copyFamiliensituation(new Familiensituation(), AntragCopyType.MUTATION);
			newFamiliensituation.setGesuchstellerKardinalitaet(EnumGesuchstellerKardinalitaet.ZU_ZWEIT);

			gesuch.getFamiliensituationContainer().setFamiliensituationJA(newFamiliensituation);

			WizardStep beschaeftigungspensumStep =
				TestDataUtil.createWizardStepObject(gesuch, WizardStepName.ERWERBSPENSUM, WizardStepStatus.OK);
			final List<WizardStep> wizardSteps = List.of(beschaeftigungspensumStep);

			mockGetWizardSteps(wizardSteps, gesuch);
			mockSaveWizardSteps(wizardSteps);
			expect(erwerbspensumService.isErwerbspensumRequired(gesuch)).andReturn(true);
			expect(einstellungService.findEinstellung(
				EinstellungKey.GESUCH_BEENDEN_BEI_TAUSCH_GS2,
				gesuch.extractGemeinde(),
				gesuch.getGesuchsperiode())).andReturn(new Einstellung(
				EinstellungKey.GESUCH_BEENDEN_BEI_TAUSCH_GS2,
				"true",
				gesuch.getGesuchsperiode())).times(2);
			replayAll();

			wizardStepService.updateSteps(
				gesuch.getId(),
				familiensituation,
				newFamiliensituation,
				WizardStepName.FAMILIENSITUATION);

			assertThat(beschaeftigungspensumStep.getWizardStepStatus(), is(WizardStepStatus.NOK));
		}

		@Test
		void beschaeftigungspensumShouldBeNOKToNOKOnKardinalitaetAlleineToZuZweit() {
			Gesuch gesuch = setupGesuchWithOneGS();
			Familiensituation familiensituation = gesuch.extractFamiliensituation();
			assertThat(familiensituation, notNullValue());
			assertThat(gesuch.getFamiliensituationContainer(), notNullValue());
			assertThat(gesuch.getGesuchsteller1(), notNullValue());

			familiensituation.setGeteilteObhut(true);
			familiensituation.setGesuchstellerKardinalitaet(EnumGesuchstellerKardinalitaet.ALLEINE);
			gesuch.getGesuchsteller1().getErwerbspensenContainers().clear();

			Familiensituation newFamiliensituation =
				familiensituation.copyFamiliensituation(new Familiensituation(), AntragCopyType.MUTATION);
			newFamiliensituation.setGesuchstellerKardinalitaet(EnumGesuchstellerKardinalitaet.ZU_ZWEIT);

			gesuch.getFamiliensituationContainer().setFamiliensituationJA(newFamiliensituation);

			WizardStep beschaeftigungspensumStep =
				TestDataUtil.createWizardStepObject(gesuch, WizardStepName.ERWERBSPENSUM, WizardStepStatus.NOK);
			final List<WizardStep> wizardSteps = List.of(beschaeftigungspensumStep);

			mockGetWizardSteps(wizardSteps, gesuch);
			mockSaveWizardSteps(wizardSteps);
			expect(erwerbspensumService.isErwerbspensumRequired(gesuch)).andReturn(true);
			replayAll();

			wizardStepService.updateSteps(
				gesuch.getId(),
				familiensituation,
				newFamiliensituation,
				WizardStepName.FAMILIENSITUATION);

			assertThat(beschaeftigungspensumStep.getWizardStepStatus(), is(WizardStepStatus.NOK));
		}

		@Test
		void beschaeftigungspensumShouldBeOKToOKOnKardinalitaetZuZweitToAlleine() {
			Gesuch gesuch = setupGesuchWithOneGS();
			Familiensituation familiensituation = gesuch.extractFamiliensituation();
			assertThat(familiensituation, notNullValue());
			assertThat(gesuch.getFamiliensituationContainer(), notNullValue());
			familiensituation.setGeteilteObhut(true);
			familiensituation.setGesuchstellerKardinalitaet(EnumGesuchstellerKardinalitaet.ZU_ZWEIT);

			Familiensituation newFamiliensituation =
				familiensituation.copyFamiliensituation(new Familiensituation(), AntragCopyType.MUTATION);
			newFamiliensituation.setGesuchstellerKardinalitaet(EnumGesuchstellerKardinalitaet.ALLEINE);

			gesuch.getFamiliensituationContainer().setFamiliensituationJA(newFamiliensituation);

			WizardStep beschaeftigungspensumStep =
				TestDataUtil.createWizardStepObject(gesuch, WizardStepName.ERWERBSPENSUM, WizardStepStatus.OK);
			final List<WizardStep> wizardSteps = List.of(beschaeftigungspensumStep);

			mockGetWizardSteps(wizardSteps, gesuch);
			mockSaveWizardSteps(wizardSteps);
			expect(erwerbspensumService.isErwerbspensumRequired(gesuch)).andReturn(true);
			replayAll();

			wizardStepService.updateSteps(
				gesuch.getId(),
				familiensituation,
				newFamiliensituation,
				WizardStepName.FAMILIENSITUATION);

			assertThat(beschaeftigungspensumStep.getWizardStepStatus(), is(WizardStepStatus.OK));
		}

		@Test
		void beschaeftigungspensumShouldBeNOKToOKOnKardinalitaetZuZweitToAlleineIfBeschaeftigungspensumGS1Present() {
			Gesuch gesuch = setupGesuchWithOneGS();
			Familiensituation familiensituation = gesuch.extractFamiliensituation();
			assertThat(familiensituation, notNullValue());
			assertThat(gesuch.getFamiliensituationContainer(), notNullValue());
			familiensituation.setGeteilteObhut(true);
			familiensituation.setGesuchstellerKardinalitaet(EnumGesuchstellerKardinalitaet.ZU_ZWEIT);

			Familiensituation newFamiliensituation =
				familiensituation.copyFamiliensituation(new Familiensituation(), AntragCopyType.MUTATION);
			newFamiliensituation.setGesuchstellerKardinalitaet(EnumGesuchstellerKardinalitaet.ALLEINE);

			gesuch.getFamiliensituationContainer().setFamiliensituationJA(newFamiliensituation);

			WizardStep beschaeftigungspensumStep =
				TestDataUtil.createWizardStepObject(gesuch, WizardStepName.ERWERBSPENSUM, WizardStepStatus.NOK);
			final List<WizardStep> wizardSteps = List.of(beschaeftigungspensumStep);

			mockGetWizardSteps(wizardSteps, gesuch);
			mockSaveWizardSteps(wizardSteps);
			expect(erwerbspensumService.isErwerbspensumRequired(gesuch)).andReturn(true);
			replayAll();

			wizardStepService.updateSteps(
				gesuch.getId(),
				familiensituation,
				newFamiliensituation,
				WizardStepName.FAMILIENSITUATION);

			assertThat(beschaeftigungspensumStep.getWizardStepStatus(), is(WizardStepStatus.OK));
		}

		@Nonnull
		private Gesuch setupGesuchWithOneGS() {
			Gesuch gesuch = TestDataUtil.createDefaultGesuch();
			gesuch.setGesuchsteller1(TestDataUtil.createDefaultGesuchstellerContainer());
			assertThat(gesuch.getGesuchsteller1(), notNullValue());
			gesuch.getGesuchsteller1().getErwerbspensenContainers().add(TestDataUtil.createErwerbspensumContainer());
			Objects.requireNonNull(gesuch.extractFamiliensituation()).setFkjvFamSit(true);
			return gesuch;
		}

		@Test
		void beschaeftigungspensumShouldBeNOKToNOKOnKardinalitaetZuZweitToAlleineIfBeschaeftigungspensumGS1NotPresent() {
			Gesuch gesuch = TestDataUtil.createDefaultGesuch();
			gesuch.setGesuchsteller1(TestDataUtil.createDefaultGesuchstellerContainer());
			Familiensituation familiensituation = gesuch.extractFamiliensituation();
			assertThat(familiensituation, notNullValue());
			assertThat(gesuch.getFamiliensituationContainer(), notNullValue());
			assertThat(gesuch.getGesuchsteller1(), notNullValue());
			familiensituation.setGeteilteObhut(false);
			familiensituation.setGesuchstellerKardinalitaet(EnumGesuchstellerKardinalitaet.ZU_ZWEIT);
			gesuch.getGesuchsteller1().getErwerbspensenContainers().clear();

			Familiensituation newFamiliensituation =
				familiensituation.copyFamiliensituation(new Familiensituation(), AntragCopyType.MUTATION);
			newFamiliensituation.setGesuchstellerKardinalitaet(EnumGesuchstellerKardinalitaet.ALLEINE);

			gesuch.getFamiliensituationContainer().setFamiliensituationJA(newFamiliensituation);

			WizardStep beschaeftigungspensumStep =
				TestDataUtil.createWizardStepObject(gesuch, WizardStepName.ERWERBSPENSUM, WizardStepStatus.OK);
			final List<WizardStep> wizardSteps = List.of(beschaeftigungspensumStep);

			mockGetWizardSteps(wizardSteps, gesuch);
			mockSaveWizardSteps(wizardSteps);
			expect(erwerbspensumService.isErwerbspensumRequired(gesuch)).andReturn(true);
			replayAll();

			wizardStepService.updateSteps(
				gesuch.getId(),
				familiensituation,
				newFamiliensituation,
				WizardStepName.FAMILIENSITUATION);

			assertThat(beschaeftigungspensumStep.getWizardStepStatus(), is(WizardStepStatus.NOK));
		}
	}

	private void mockSaveWizardSteps(List<WizardStep> wizardSteps) {
		for (WizardStep wizardStep : wizardSteps) {
			expect(persistence.merge(wizardStep)).andReturn(wizardStep);
		}
	}

	private void mockGetWizardSteps(List<WizardStep> result, Gesuch gesuch) {
		CriteriaBuilder cb = mock(CriteriaBuilder.class);
		expect(persistence.getCriteriaBuilder()).andReturn(cb);

		CriteriaQuery<WizardStep> query = mock(CriteriaQuery.class);
		expect(cb.createQuery(WizardStep.class)).andReturn(query);

		Root<WizardStep> root = mock(Root.class);
		expect(query.from(WizardStep.class)).andReturn(root);

		Path<Gesuch> gesuchPath = mock(Path.class);
		expect(root.get(WizardStep_.gesuch)).andReturn(gesuchPath);

		Path<String> gesuchIdPath = mock(Path.class);
		expect(gesuchPath.get(Gesuch_.id)).andReturn(gesuchIdPath);

		Predicate gesuchPredicate = mock(Predicate.class);
		expect(cb.equal(gesuchPath, gesuch.getId())).andReturn(gesuchPredicate);

		expect(cb.equal(EasyMock.<Path<Gesuch>>anyObject(), EasyMock.anyString())).andReturn(gesuchPredicate);

		expect(query.where(gesuchPredicate)).andReturn(query);

		for (WizardStep wizardStep : result) {
			authorizer.checkReadAuthorization(EasyMock.anyObject(WizardStep.class));
		}
		expect(persistence.getCriteriaResults(query)).andReturn(result);
	}
}
