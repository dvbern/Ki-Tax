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

import java.time.LocalDate;
import java.time.Month;
import java.util.Collection;
import java.util.Optional;

import javax.inject.Inject;

import ch.dvbern.ebegu.entities.EbeguParameter;
import ch.dvbern.ebegu.entities.Gesuchsperiode;
import ch.dvbern.ebegu.enums.EbeguParameterKey;
import ch.dvbern.ebegu.services.EbeguParameterService;
import ch.dvbern.ebegu.tets.TestDataUtil;
import ch.dvbern.ebegu.types.DateRange;
import ch.dvbern.ebegu.util.Constants;
import ch.dvbern.lib.cdipersistence.Persistence;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.persistence.UsingDataSet;
import org.jboss.arquillian.transaction.api.annotation.TransactionMode;
import org.jboss.arquillian.transaction.api.annotation.Transactional;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import static ch.dvbern.ebegu.enums.EbeguParameterKey.PARAM_FIXBETRAG_STADT_PRO_TAG_KITA;

/**
 * Testet den EbeguParameterService.
 */
@RunWith(Arquillian.class)
@UsingDataSet("datasets/empty.xml")
@Transactional(TransactionMode.DISABLED)
public class EbeguParameterServiceTest extends AbstractEbeguLoginTest {

	@Inject
	private EbeguParameterService parameterService;

	@Inject
	private Persistence persistence;

	private final EbeguParameterKey PARAM_KEY = EbeguParameterKey.PARAM_ANZAL_TAGE_MAX_KITA;

	@Test
	public void createEbeguParameterTest() {
		Assert.assertNotNull(parameterService);
		EbeguParameter insertedEbeguParameter = insertEbeguParameter();

		Collection<EbeguParameter> allEbeguParameter = parameterService.getAllEbeguParameter();
		Assert.assertEquals(1, allEbeguParameter.size());
		EbeguParameter nextEbeguParameter = allEbeguParameter.iterator().next();
		Assert.assertEquals(insertedEbeguParameter.getName(), nextEbeguParameter.getName());
		Assert.assertEquals(insertedEbeguParameter.getValue(), nextEbeguParameter.getValue());
	}

	@Test
	public void updateEbeguParameterTest() {
		Assert.assertNotNull(parameterService);
		EbeguParameter insertedEbeguParameter = insertEbeguParameter();

		Optional<EbeguParameter> ebeguParameterOptional = parameterService.findEbeguParameter(insertedEbeguParameter.getId());
		Assert.assertTrue(ebeguParameterOptional.isPresent());
		EbeguParameter persistedInstStammdaten = ebeguParameterOptional.get();
		Assert.assertEquals(insertedEbeguParameter.getValue(), persistedInstStammdaten.getValue());

		persistedInstStammdaten.setValue("Mein Test Wert");
		EbeguParameter updatedEbeguParameter = parameterService.saveEbeguParameter(persistedInstStammdaten);
		Assert.assertEquals(persistedInstStammdaten.getValue(), updatedEbeguParameter.getValue());
	}

	@Test
	public void getAllEbeguParameterByDateTest() {
		Assert.assertNotNull(parameterService);
		insertEbeguParameter();
		Collection<EbeguParameter> allEbeguParameterByDate = parameterService.getAllEbeguParameterByDate(LocalDate.now());
		Assert.assertEquals(1, allEbeguParameterByDate.size());
	}

	@Test
	public void saveEbeguParameter() throws Exception {
		// Noch keine Params
		Collection<EbeguParameter> allParameter = parameterService.getAllEbeguParameter();
		Optional<EbeguParameter> currentParameterOptional = parameterService.getEbeguParameterByKeyAndDate(PARAM_KEY, LocalDate.now());

		Assert.assertTrue(allParameter.isEmpty());
		Assert.assertFalse(currentParameterOptional.isPresent());

		EbeguParameter param1 = TestDataUtil.createDefaultEbeguParameter(EbeguParameterKey.PARAM_ANZAL_TAGE_MAX_KITA);
		parameterService.saveEbeguParameter(param1);

		allParameter = parameterService.getAllEbeguParameter();
		currentParameterOptional = parameterService.getEbeguParameterByKeyAndDate(PARAM_KEY, LocalDate.now());

		Assert.assertFalse(allParameter.isEmpty());
		Assert.assertTrue(currentParameterOptional.isPresent());

		EbeguParameter currentParameter = currentParameterOptional.get();
		Assert.assertEquals(Constants.START_OF_TIME, currentParameter.getGueltigkeit().getGueltigAb());
		Assert.assertEquals(Constants.END_OF_TIME, currentParameter.getGueltigkeit().getGueltigBis());

	}

	@Test
	public void getEbeguParameterByGesuchsperiode() throws Exception {
		Collection<EbeguParameter> allParameter = parameterService.getAllEbeguParameter();
		Assert.assertTrue(allParameter.isEmpty());

		Gesuchsperiode gesuchsperiode = TestDataUtil.createDefaultGesuchsperiode();
		gesuchsperiode.setGueltigkeit(new DateRange(LocalDate.of(2015, Month.AUGUST, 1), LocalDate.of(2016, Month.JULY, 31)));

		EbeguParameter parameter = TestDataUtil.createDefaultEbeguParameter(EbeguParameterKey.PARAM_ANZAL_TAGE_MAX_KITA);
		parameter.setGueltigkeit(gesuchsperiode.getGueltigkeit());
		parameterService.saveEbeguParameter(parameter);

		gesuchsperiode = TestDataUtil.createDefaultGesuchsperiode();
		gesuchsperiode.setGueltigkeit(new DateRange(LocalDate.of(2016, Month.AUGUST, 1), LocalDate.of(2017, Month.JULY, 31)));
		parameterService.getEbeguParameterByGesuchsperiode(gesuchsperiode);

		allParameter = parameterService.getAllEbeguParameter();
		Assert.assertFalse(allParameter.isEmpty());
		Assert.assertEquals(2, allParameter.size());
	}

	@Test
	public void getEbeguParameterByJahr() throws Exception {
		Collection<EbeguParameter> allParameter = parameterService.getAllEbeguParameter();
		Assert.assertTrue(allParameter.isEmpty());

		EbeguParameter parameter = TestDataUtil.createDefaultEbeguParameter(PARAM_FIXBETRAG_STADT_PRO_TAG_KITA);
		parameter.setGueltigkeit(new DateRange(2015));
		parameterService.saveEbeguParameter(parameter);

		parameterService.getEbeguParametersByJahr(2016);

		allParameter = parameterService.getAllEbeguParameter();
		Assert.assertFalse(allParameter.isEmpty());
		Assert.assertEquals(2, allParameter.size());
	}

	@Test
	public void getEbeguParameterByKeyAndDate() throws Exception {
		EbeguParameter param1 = TestDataUtil.createDefaultEbeguParameter(EbeguParameterKey.PARAM_ANZAL_TAGE_MAX_KITA);
		parameterService.saveEbeguParameter(param1);

		Optional<EbeguParameter> optional = parameterService.getEbeguParameterByKeyAndDate(PARAM_KEY, LocalDate.now());
		Assert.assertTrue(optional.isPresent());
	}

	private EbeguParameter insertEbeguParameter() {
		EbeguParameter ebeguParameter = TestDataUtil.createDefaultEbeguParameter(EbeguParameterKey.PARAM_ANZAL_TAGE_MAX_KITA);
		return parameterService.saveEbeguParameter(ebeguParameter);
	}
}
