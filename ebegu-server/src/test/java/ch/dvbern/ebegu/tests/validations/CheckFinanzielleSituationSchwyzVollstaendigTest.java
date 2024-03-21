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

package ch.dvbern.ebegu.tests.validations;

import java.math.BigDecimal;
import java.util.Optional;

import javax.validation.ConstraintValidatorContext;

import ch.dvbern.ebegu.entities.FinanzielleSituation;
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.enums.FinanzielleSituationTyp;
import ch.dvbern.ebegu.services.GesuchService;
import ch.dvbern.ebegu.validators.CheckBerechtigungGemeindeValidator;
import ch.dvbern.ebegu.validators.finsit.CheckFinanzielleSituationCompleteSchwyzValidator;
import org.easymock.EasyMockExtension;
import org.easymock.EasyMockSupport;
import org.easymock.Mock;
import org.easymock.TestSubject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.easymock.EasyMock.anyString;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

/**
 * Tests fuer {@link CheckBerechtigungGemeindeValidator}
 */
@ExtendWith(EasyMockExtension.class)
class CheckFinanzielleSituationSchwyzVollstaendigTest extends EasyMockSupport {

	@TestSubject
	private final CheckFinanzielleSituationCompleteSchwyzValidator validator = new CheckFinanzielleSituationCompleteSchwyzValidator();

	@Mock
	private GesuchService gesuchService;

	@Mock
	private ConstraintValidatorContext context;



	@Test
	void emptyFinSitShouldNotBeValid() {
		FinanzielleSituation finanzielleSituation = new FinanzielleSituation();
		setupGesuchMock();
		replayAll();

		assertThat(validator.isValid(finanzielleSituation, context), is(false));
	}

	@Test
	void nettolohnMissingShouldNotBeValid() {
		FinanzielleSituation finanzielleSituation = new FinanzielleSituation();
		setupGesuchMock();
		finanzielleSituation.setAbzuegeLiegenschaft(BigDecimal.ZERO);
		finanzielleSituation.setEinkaeufeVorsorge(BigDecimal.ZERO);
		finanzielleSituation.setSteuerbaresVermoegen(BigDecimal.valueOf(110000));
		replayAll();

		assertThat(validator.isValid(finanzielleSituation, context), is(false));
	}

	@Test
	void abzuegeLiegenschaftenMissingShouldNotBeValid() {
		FinanzielleSituation finanzielleSituation = new FinanzielleSituation();
		setupGesuchMock();
		finanzielleSituation.setSteuerbaresEinkommen(BigDecimal.valueOf(87000));
		finanzielleSituation.setEinkaeufeVorsorge(BigDecimal.ZERO);
		finanzielleSituation.setSteuerbaresVermoegen(BigDecimal.valueOf(110000));
		replayAll();

		assertThat(validator.isValid(finanzielleSituation, context), is(false));
	}

	@Test
	void einkaeufeVorsorgeMissingShouldNotBeValid() {
		FinanzielleSituation finanzielleSituation = new FinanzielleSituation();
		setupGesuchMock();
		finanzielleSituation.setSteuerbaresEinkommen(BigDecimal.valueOf(87000));
		finanzielleSituation.setAbzuegeLiegenschaft(BigDecimal.ZERO);
		finanzielleSituation.setSteuerbaresVermoegen(BigDecimal.valueOf(110000));
		replayAll();

		assertThat(validator.isValid(finanzielleSituation, context), is(false));
	}

	@Test
	void setSteuerbaresVermoegenMissingShouldNotBeValid() {
		FinanzielleSituation finanzielleSituation = new FinanzielleSituation();
		setupGesuchMock();
		finanzielleSituation.setSteuerbaresEinkommen(BigDecimal.valueOf(87000));
		finanzielleSituation.setAbzuegeLiegenschaft(BigDecimal.ZERO);
		finanzielleSituation.setEinkaeufeVorsorge(BigDecimal.ZERO);
		replayAll();

		assertThat(validator.isValid(finanzielleSituation, context), is(false));
	}

	@Test
	void bruttolohnShouldBeValid() {
		FinanzielleSituation finanzielleSituation = new FinanzielleSituation();
		setupGesuchMock();
		finanzielleSituation.setBruttoLohn(BigDecimal.valueOf(87000));
		replayAll();

		assertThat(validator.isValid(finanzielleSituation, context), is(true));
	}

	@Test
	void quellsteuerValuesFilledInShouldBeValid() {
		FinanzielleSituation finanzielleSituation = new FinanzielleSituation();
		setupGesuchMock();
		finanzielleSituation.setSteuerbaresEinkommen(BigDecimal.valueOf(87000));
		finanzielleSituation.setAbzuegeLiegenschaft(BigDecimal.ZERO);
		finanzielleSituation.setEinkaeufeVorsorge(BigDecimal.ZERO);
		finanzielleSituation.setSteuerbaresVermoegen(BigDecimal.valueOf(110000));
		replayAll();

		assertThat(validator.isValid(finanzielleSituation, context), is(true));
	}

	private void setupGesuchMock() {
		Gesuch gesuch = new Gesuch();
		gesuch.setFinSitTyp(FinanzielleSituationTyp.SCHWYZ);
		expect(gesuchService.findGesuchForFinSit(anyString())).andReturn(Optional.of(gesuch));
	}

}
