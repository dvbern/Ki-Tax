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

package ch.dvbern.ebegu.tests;

import java.util.Collection;
import java.util.Objects;
import java.util.Optional;

import javax.annotation.Nonnull;
import javax.inject.Inject;

import ch.dvbern.ebegu.entities.EinkommensverschlechterungInfoContainer;
import ch.dvbern.ebegu.entities.Familiensituation;
import ch.dvbern.ebegu.entities.FamiliensituationContainer;
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.enums.AntragCopyType;
import ch.dvbern.ebegu.enums.AntragTyp;
import ch.dvbern.ebegu.enums.EnumFamilienstatus;
import ch.dvbern.ebegu.services.EinkommensverschlechterungInfoService;
import ch.dvbern.ebegu.services.FamiliensituationService;
import ch.dvbern.ebegu.test.TestDataUtil;
import ch.dvbern.lib.cdipersistence.Persistence;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.persistence.UsingDataSet;
import org.jboss.arquillian.transaction.api.annotation.TransactionMode;
import org.jboss.arquillian.transaction.api.annotation.Transactional;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Arquillian Tests fuer die Klasse FamiliensituationService
 */
@RunWith(Arquillian.class)
@UsingDataSet("datasets/mandant-dataset.xml")
@Transactional(TransactionMode.DISABLED)
public class FamiliensituationServiceTest extends AbstractEbeguLoginTest {

	@Inject
	private FamiliensituationService familiensituationService;

	@Inject
	private EinkommensverschlechterungInfoService evInfoService;

	@Inject
	private Persistence persistence;

	@Test
	public void testCreateFamiliensituation() {
		Assert.assertNotNull(familiensituationService);
		insertNewFamiliensituationContainer();

		Collection<FamiliensituationContainer> allFamiliensituation = familiensituationService.getAllFamiliensituatione();
		Assert.assertEquals(1, allFamiliensituation.size());
		FamiliensituationContainer nextFamsit = allFamiliensituation.iterator().next();
		Assert.assertNotNull(nextFamsit.getFamiliensituationJA());
		Assert.assertEquals(EnumFamilienstatus.ALLEINERZIEHEND, nextFamsit.getFamiliensituationJA().getFamilienstatus());
	}

	@Test
	public void testUpdateFamiliensituationTest() {
		Optional<FamiliensituationContainer> familiensituation = createFamiliensituationContainer();

		Assert.assertTrue(familiensituation.isPresent());
		Familiensituation famSitExtracted = familiensituation.get().extractFamiliensituation();
		Assert.assertNotNull(famSitExtracted);
		famSitExtracted.setFamilienstatus(EnumFamilienstatus.KONKUBINAT);
		FamiliensituationContainer famSitUpdated = familiensituationService.saveFamiliensituation(TestDataUtil.createDefaultGesuch(),
			familiensituation.get(), null);
		Assert.assertEquals(EnumFamilienstatus.KONKUBINAT, Objects.requireNonNull(famSitUpdated.extractFamiliensituation()).getFamilienstatus());
		Optional<FamiliensituationContainer> famSitFound = familiensituationService.findFamiliensituation(famSitUpdated.getId());
		Assert.assertTrue(famSitFound.isPresent());
		Familiensituation famSitFoundExtracted = famSitFound.get().extractFamiliensituation();
		Assert.assertNotNull(famSitFoundExtracted);
		Assert.assertEquals(EnumFamilienstatus.KONKUBINAT, famSitFoundExtracted.getFamilienstatus());
	}

	@Test
	public void testRemoveFamiliensituationTest() {
		Assert.assertNotNull(familiensituationService);
		FamiliensituationContainer insertedFamiliensituation = insertNewFamiliensituationContainer();
		Assert.assertEquals(1, familiensituationService.getAllFamiliensituatione().size());

		familiensituationService.removeFamiliensituation(insertedFamiliensituation);
		Assert.assertEquals(0, familiensituationService.getAllFamiliensituatione().size());
	}

	@Test
	public void testSaveFamiliensituationMutation() {
		final Gesuch gesuch = TestDataUtil.createAndPersistGesuch(persistence);
		gesuch.setTyp(AntragTyp.MUTATION);

		final EinkommensverschlechterungInfoContainer evInfo = TestDataUtil.createDefaultEinkommensverschlechterungsInfoContainer(gesuch);
		final Optional<EinkommensverschlechterungInfoContainer> einkommensverschlechterungInfo = evInfoService.createEinkommensverschlechterungInfo(evInfo);
		gesuch.setEinkommensverschlechterungInfoContainer(einkommensverschlechterungInfo.get());

		Optional<FamiliensituationContainer> familiensituation = createFamiliensituationContainer();
		final FamiliensituationContainer newFamiliensituation = familiensituation.get().copyFamiliensituationContainer(new FamiliensituationContainer(),
			AntragCopyType.MUTATION, false);
		newFamiliensituation.extractFamiliensituation().setGemeinsameSteuererklaerung(null);

		final FamiliensituationContainer persistedFamiliensituation = familiensituationService.saveFamiliensituation(gesuch,
			newFamiliensituation, null);

		Assert.assertFalse(persistedFamiliensituation.extractFamiliensituation().getGemeinsameSteuererklaerung());
		Assert.assertFalse(gesuch.extractEinkommensverschlechterungInfo().getGemeinsameSteuererklaerung_BjP1());
		Assert.assertFalse(gesuch.extractEinkommensverschlechterungInfo().getGemeinsameSteuererklaerung_BjP2());
	}

	// HELP METHODS

	@Nonnull
	private FamiliensituationContainer insertNewFamiliensituationContainer() {
		FamiliensituationContainer familiensituationContainer = TestDataUtil.createDefaultFamiliensituationContainer();
		familiensituationService.saveFamiliensituation(TestDataUtil.createDefaultGesuch(), familiensituationContainer, null);
		return familiensituationContainer;
	}

	@Nonnull
	private Optional<FamiliensituationContainer> createFamiliensituationContainer() {
		Assert.assertNotNull(familiensituationService);
		FamiliensituationContainer insertedFamiliensituationContainer = insertNewFamiliensituationContainer();
		Optional<FamiliensituationContainer> familiensituation = familiensituationService.findFamiliensituation(insertedFamiliensituationContainer.getId());
		Assert.assertEquals(EnumFamilienstatus.ALLEINERZIEHEND, familiensituation.get().extractFamiliensituation().getFamilienstatus());
		return familiensituation;
	}

}
