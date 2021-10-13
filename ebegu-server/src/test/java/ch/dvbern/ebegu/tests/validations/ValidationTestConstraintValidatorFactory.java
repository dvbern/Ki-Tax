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

package ch.dvbern.ebegu.tests.validations;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorFactory;
import javax.validation.Validation;

import ch.dvbern.ebegu.services.EinstellungService;
import ch.dvbern.ebegu.tests.services.EinstellungDummyServiceBean;
import ch.dvbern.ebegu.validators.CheckBetreuungspensumValidator;
import ch.dvbern.ebegu.validators.CheckFachstellenValidator;
import ch.dvbern.ebegu.validators.CheckPensumFachstelleValidator;

/**
 * This class helps us test our ConstraintValidators without actually starting a CDI container.
 * Since we are using services inside the validators we need a way to initialize the Validator with a dummy.
 * This Factory allows us to initialize the Validator ourself, giving us the oppurtunity to use a DummyService for the validotr
 */
public class ValidationTestConstraintValidatorFactory implements ConstraintValidatorFactory {


	public ValidationTestConstraintValidatorFactory() {
	}

	@Override
	public <T extends ConstraintValidator<?, ?>> T getInstance(Class<T> key) {
		if (key.equals(CheckBetreuungspensumValidator.class)) {
			//Mock Service for Parameters
			EinstellungService dummyEinstellungenService = new EinstellungDummyServiceBean();
			//noinspection unchecked,ConstantConditions Der DummyService laesst null zu, in den Tests ist es immer null
			return (T) new CheckBetreuungspensumValidator(dummyEinstellungenService, null);
		}
		if (key.equals(CheckPensumFachstelleValidator.class)) {
			//Mock Service for Parameters
			EinstellungService dummyEinstellungenService = new EinstellungDummyServiceBean();
			//noinspection unchecked,ConstantConditions Der DummyService laesst null zu, in den Tests ist es immer null
			return (T) new CheckPensumFachstelleValidator(dummyEinstellungenService, null);
		}
		if (key.equals(CheckFachstellenValidator.class)) {
			//Mock Service for Parameters
			EinstellungService dummyEinstellungenService = new EinstellungDummyServiceBean();
			//noinspection unchecked,ConstantConditions Der DummyService laesst null zu, in den Tests ist es immer null
			return (T) new CheckFachstellenValidator(dummyEinstellungenService);
		}
		ConstraintValidatorFactory delegate = Validation.byDefaultProvider().configure().getDefaultConstraintValidatorFactory();
		return delegate.getInstance(key);
	}

	@Override
	public void releaseInstance(ConstraintValidator<?, ?> instance) {
		//nothing to do
	}
}
