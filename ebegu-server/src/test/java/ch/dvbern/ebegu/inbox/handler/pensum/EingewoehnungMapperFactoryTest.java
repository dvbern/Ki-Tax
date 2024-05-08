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

package ch.dvbern.ebegu.inbox.handler.pensum;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.validation.ConstraintViolation;
import javax.validation.Validator;

import ch.dvbern.ebegu.entities.AbstractMahlzeitenPensum;
import ch.dvbern.ebegu.entities.BetreuungsmitteilungPensum;
import ch.dvbern.ebegu.entities.Eingewoehnung;
import ch.dvbern.ebegu.entities.Einstellung;
import ch.dvbern.ebegu.entities.Gesuchsperiode;
import ch.dvbern.ebegu.enums.EingewoehnungTyp;
import ch.dvbern.ebegu.inbox.handler.ProcessingContext;
import ch.dvbern.ebegu.services.EinstellungService;
import ch.dvbern.ebegu.types.DateRange;
import ch.dvbern.ebegu.util.Constants;
import ch.dvbern.kibon.exchange.commons.platzbestaetigung.EingewoehnungDTO;
import ch.dvbern.kibon.exchange.commons.platzbestaetigung.ZeitabschnittDTO;
import org.easymock.EasyMockExtension;
import org.easymock.EasyMockSupport;
import org.easymock.Mock;
import org.easymock.TestSubject;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static ch.dvbern.ebegu.enums.EinstellungKey.EINGEWOEHNUNG_TYP;
import static ch.dvbern.ebegu.inbox.handler.PlatzbestaetigungTestUtil.createZeitabschnittDTO;
import static ch.dvbern.ebegu.inbox.handler.PlatzbestaetigungTestUtil.initProcessingContext;
import static com.spotify.hamcrest.pojo.IsPojo.pojo;
import static org.easymock.EasyMock.anyObject;
import static org.easymock.EasyMock.eq;
import static org.easymock.EasyMock.expect;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.comparesEqualTo;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;

@ExtendWith(EasyMockExtension.class)
class EingewoehnungMapperFactoryTest extends EasyMockSupport {

	@TestSubject
	private final EingewoehnungMapperFactory factory = new EingewoehnungMapperFactory();

	@Mock
	private Validator validator;

	@Mock
	private EinstellungService einstellungService;

	@AfterEach
	void tearDown() {
		verifyAll();
	}

	@Nested
	class WhenEinstellungEnabled {

		@Test
		void importEingewoehnung() {
			ZeitabschnittDTO z = createZeitabschnittDTO(Constants.DEFAULT_GUELTIGKEIT);
			EingewoehnungDTO eingewoehnung =
				new EingewoehnungDTO(BigDecimal.valueOf(123.45), LocalDate.of(2024, 1, 1), LocalDate.of(2024, 1, 31));
			z.setEingewoehnung(eingewoehnung);

			expectEingewoehnungTyp(EingewoehnungTyp.PAUSCHALE);
			expect(validator.validate(anyObject())).andReturn(Set.of());
			BetreuungsmitteilungPensum actual = convert(z);

			assertThat(actual.getEingewoehnung(), pojo(Eingewoehnung.class)
				.where(Eingewoehnung::getKosten, comparesEqualTo(BigDecimal.valueOf(123.45)))
				.where(Eingewoehnung::getGueltigkeit, pojo(DateRange.class)
					.where(DateRange::getGueltigAb, comparesEqualTo(LocalDate.of(2024, 1, 1)))
					.where(DateRange::getGueltigBis, comparesEqualTo(LocalDate.of(2024, 1, 31)))));
		}

		@Test
		void ignoreInvalidEingewoehnung_requireHumanConfirmation() {
			ZeitabschnittDTO z = createZeitabschnittDTO(Constants.DEFAULT_GUELTIGKEIT);
			@SuppressWarnings("DataFlowIssue") EingewoehnungDTO eingewoehnung =
				new EingewoehnungDTO(null, LocalDate.of(2024, 1, 1), LocalDate.of(2024, 1, 31));
			z.setEingewoehnung(eingewoehnung);
			ProcessingContext ctx = initProcessingContext(z);

			expectEingewoehnungTyp(EingewoehnungTyp.PAUSCHALE);
			ConstraintViolation<Eingewoehnung> violation = mock(ConstraintViolation.class);
			expect(validator.validate(anyObject(Eingewoehnung.class))).andReturn(Set.of(violation));

			BetreuungsmitteilungPensum actual = convert(z, ctx);

			assertThat(actual.getEingewoehnung(), is(nullValue()));
			assertThat(ctx, pojo(ProcessingContext.class)
				.where(ProcessingContext::getHumanConfirmationMessages, containsString("Die Eingewöhnung-Daten sind ungültig: "))
				.where(ProcessingContext::isReadyForBestaetigen, is(false))
			);
		}

		@Test
		void importEingewoehnung_null() {
			ZeitabschnittDTO z = createZeitabschnittDTO(Constants.DEFAULT_GUELTIGKEIT);
			//noinspection DataFlowIssue
			z.setEingewoehnung(null);

			expectEingewoehnungTyp(EingewoehnungTyp.PAUSCHALE);
			BetreuungsmitteilungPensum actual = convert(z);

			assertThat(actual.getEingewoehnung(), is(nullValue()));
		}
	}

	@ParameterizedTest
	@EnumSource(value = EingewoehnungTyp.class, mode = EnumSource.Mode.EXCLUDE, names = "PAUSCHALE")
	void ignoresEingewoehnung(EingewoehnungTyp eingewoehnungTyp) {
		ZeitabschnittDTO z = createZeitabschnittDTO(Constants.DEFAULT_GUELTIGKEIT);
		EingewoehnungDTO eingewoehnung =
			new EingewoehnungDTO(BigDecimal.valueOf(123.45), LocalDate.of(2024, 1, 1), LocalDate.of(2024, 1, 31));
		z.setEingewoehnung(eingewoehnung);

		expectEingewoehnungTyp(eingewoehnungTyp);
		BetreuungsmitteilungPensum actual = convert(z);

		assertThat(actual.getEingewoehnung(), is(nullValue()));
	}

	@Nonnull
	private BetreuungsmitteilungPensum convert(ZeitabschnittDTO z) {
		return convert(z, initProcessingContext(z));
	}

	@Nonnull
	private BetreuungsmitteilungPensum convert(ZeitabschnittDTO z, ProcessingContext ctx) {
		replayAll();

		PensumMapper<AbstractMahlzeitenPensum> pensumMapper = factory.createForEingewoehnung(ctx);

		BetreuungsmitteilungPensum actual = new BetreuungsmitteilungPensum();
		pensumMapper.toAbstractMahlzeitenPensum(actual, z);

		return actual;
	}

	private void expectEingewoehnungTyp(EingewoehnungTyp eingewoehnungTyp) {
		expect(einstellungService.findEinstellung(eq(EINGEWOEHNUNG_TYP), anyObject()))
			.andReturn(new Einstellung(EINGEWOEHNUNG_TYP, eingewoehnungTyp.name(), mock(Gesuchsperiode.class)));
	}
}
