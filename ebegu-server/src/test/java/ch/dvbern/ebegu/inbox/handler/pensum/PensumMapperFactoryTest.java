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
import java.util.function.Supplier;

import javax.annotation.Nonnull;

import ch.dvbern.ebegu.entities.AbstractBetreuungsPensum;
import ch.dvbern.ebegu.entities.AbstractMahlzeitenPensum;
import ch.dvbern.ebegu.entities.Betreuung;
import ch.dvbern.ebegu.entities.BetreuungsmitteilungPensum;
import ch.dvbern.ebegu.entities.Betreuungspensum;
import ch.dvbern.ebegu.entities.BetreuungspensumContainer;
import ch.dvbern.ebegu.entities.EingewoehnungPauschale;
import ch.dvbern.ebegu.entities.Einstellung;
import ch.dvbern.ebegu.entities.Gesuchsperiode;
import ch.dvbern.ebegu.entities.containers.PensumUtil;
import ch.dvbern.ebegu.enums.BetreuungsangebotTyp;
import ch.dvbern.ebegu.enums.EingewoehnungTyp;
import ch.dvbern.ebegu.enums.EinstellungKey;
import ch.dvbern.ebegu.enums.PensumUnits;
import ch.dvbern.ebegu.inbox.handler.ProcessingContext;
import ch.dvbern.ebegu.services.EinstellungService;
import ch.dvbern.ebegu.services.MitteilungService;
import ch.dvbern.ebegu.types.DateRange;
import ch.dvbern.ebegu.util.Constants;
import ch.dvbern.kibon.exchange.commons.platzbestaetigung.EingewoehnungDTO;
import ch.dvbern.kibon.exchange.commons.platzbestaetigung.ZeitabschnittDTO;
import com.spotify.hamcrest.pojo.IsPojo;
import org.easymock.EasyMockExtension;
import org.easymock.EasyMockSupport;
import org.easymock.Mock;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static ch.dvbern.ebegu.enums.EinstellungKey.EINGEWOEHNUNG_TYP;
import static ch.dvbern.ebegu.inbox.handler.PlatzbestaetigungTestUtil.createZeitabschnittDTO;
import static ch.dvbern.ebegu.inbox.handler.PlatzbestaetigungTestUtil.initProcessingContext;
import static com.spotify.hamcrest.pojo.IsPojo.pojo;
import static java.util.Objects.requireNonNull;
import static org.easymock.EasyMock.anyObject;
import static org.easymock.EasyMock.eq;
import static org.easymock.EasyMock.expect;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.closeTo;
import static org.hamcrest.Matchers.comparesEqualTo;
import static org.hamcrest.Matchers.is;

@ExtendWith(EasyMockExtension.class)
class PensumMapperFactoryTest extends EasyMockSupport {

	@Mock
	private EinstellungService einstellungService;

	@Mock
	private MitteilungService mitteilungService;

	private PensumMapperFactory factory;

	@BeforeEach
	void setUp() {
		// EinstellungMock is initialized too late when trying to do field initializers instead.
		factory = new PensumMapperFactory(
			new BetreuungInFerienzeitMapperFactory(mitteilungService),
			new MahlzeitVerguenstigungMapperFactory(einstellungService),
			new PensumValueMapperFactory(einstellungService),
			new EingewoehnungPauschaleMapperFactory(einstellungService)
		);
	}

	@AfterEach
	void tearDown() {
		verifyAll();
	}

	@Test
	void importHauptMahlzeitenWhenBetreuungsAngebotMittagstisch() {
		ZeitabschnittDTO z = createZeitabschnittDTO(Constants.DEFAULT_GUELTIGKEIT);
		z.setAnzahlHauptmahlzeiten(BigDecimal.valueOf(4));
		z.setTarifProHauptmahlzeiten(BigDecimal.valueOf(12.25));
		z.setAnzahlNebenmahlzeiten(BigDecimal.TEN);

		ProcessingContext ctx = initProcessingContext(z);

		BigDecimal monatlicheHauptmahlzeiten = BigDecimal.valueOf(5);
		BigDecimal tarifProHauptmahlzeit = BigDecimal.valueOf(10.5);

		Betreuung betreuung = ctx.getBetreuung();

		// setup a MITTAGSTISCH
		betreuung.getInstitutionStammdaten().setBetreuungsangebotTyp(BetreuungsangebotTyp.MITTAGSTISCH);
		BetreuungspensumContainer container = betreuung.getBetreuungspensumContainers().iterator().next();
		Betreuungspensum betreuungspensum = requireNonNull(container.getBetreuungspensumJA());
		betreuungspensum.setMonatlicheHauptmahlzeiten(monatlicheHauptmahlzeiten);
		betreuungspensum.setTarifProHauptmahlzeit(tarifProHauptmahlzeit);
		PensumUtil.transformMittagstischPensum(betreuungspensum);

		replayAll();
		PensumMapper<BetreuungsmitteilungPensum> pensumMapper = factory.createPensumMapper(ctx);
		BetreuungsmitteilungPensum actual = convert(pensumMapper, z, BetreuungsmitteilungPensum::new);

		assertThat(actual, pojo(AbstractMahlzeitenPensum.class)
			.where(AbstractMahlzeitenPensum::getMonatlicheHauptmahlzeiten, comparesEqualTo(BigDecimal.valueOf(4)))
			.where(AbstractMahlzeitenPensum::getMonatlicheNebenmahlzeiten, comparesEqualTo(BigDecimal.ZERO))
			.where(AbstractMahlzeitenPensum::getTarifProHauptmahlzeit, comparesEqualTo(BigDecimal.valueOf(12.25)))
			.where(AbstractMahlzeitenPensum::getTarifProNebenmahlzeit, comparesEqualTo(BigDecimal.ZERO))
			.where(AbstractMahlzeitenPensum::getPensum, closeTo(BigDecimal.valueOf(4 * 100 / 20.5), BigDecimal.valueOf(1.0e-8)))
			.where(AbstractMahlzeitenPensum::getMonatlicheBetreuungskosten, comparesEqualTo(BigDecimal.valueOf(4 * 12.25))));
	}

	@Test
	void defaultMapperIntegrationTest() {
		ZeitabschnittDTO z = createZeitabschnitt();

		mockEinstellungen();
		replayAll();
		PensumMapper<BetreuungsmitteilungPensum> pensumMapper = factory.createPensumMapper(initProcessingContext(z));
		BetreuungsmitteilungPensum actual = convert(pensumMapper, z, BetreuungsmitteilungPensum::new);

		assertThat(actual, matches(z));
	}

	@Test
	void platzbestaetigungIntegrationTest() {
		ZeitabschnittDTO z = createZeitabschnitt();

		mockEinstellungen();
		replayAll();
		var forPlatzbestaetigung = factory.createForPlatzbestaetigung(initProcessingContext(z));
		Betreuungspensum actual = convert(forPlatzbestaetigung, z, Betreuungspensum::new);

		assertThat(actual, matches(z));
	}

	@Test
	void betreuungsmitteilungIntegrationTest() {
		ZeitabschnittDTO z = createZeitabschnitt();

		mockEinstellungen();
		replayAll();
		var pensumMapper = factory.createForBetreuungsmitteilung(initProcessingContext(z));
		BetreuungsmitteilungPensum actual = convert(pensumMapper, z, BetreuungsmitteilungPensum::new);

		assertThat(actual, matches(z));
	}

	private <T extends AbstractBetreuungsPensum> T convert(
		PensumMapper<T> pensumMapper,
		ZeitabschnittDTO z,
		Supplier<T> entitySupplier
	) {
		T actual = entitySupplier.get();
		pensumMapper.toAbstractMahlzeitenPensum(actual, z);

		return actual;
	}

	@Nonnull
	private ZeitabschnittDTO createZeitabschnitt() {
		ZeitabschnittDTO z = createZeitabschnittDTO(Constants.DEFAULT_GUELTIGKEIT);
		z.setAnzahlHauptmahlzeiten(BigDecimal.valueOf(4));
		z.setTarifProHauptmahlzeiten(BigDecimal.valueOf(12.25));
		z.setAnzahlNebenmahlzeiten(BigDecimal.TEN);
		z.setTarifProNebenmahlzeiten(BigDecimal.valueOf(9.5));
		z.setBetreuungInFerienzeit(false);
		z.setEingewoehnung(new EingewoehnungDTO(BigDecimal.valueOf(400), LocalDate.of(2024, 1, 10), LocalDate.of(2024, 1, 12)));

		return z;
	}

	private IsPojo<AbstractBetreuungsPensum> matches(ZeitabschnittDTO z) {
		return pojo(AbstractBetreuungsPensum.class)
			.where(AbstractBetreuungsPensum::getGueltigkeit, pojo(DateRange.class)
				.where(DateRange::getGueltigAb, is(z.getVon()))
				.where(DateRange::getGueltigBis, is(z.getBis()))
			)
			.where(AbstractBetreuungsPensum::getPensum, comparesEqualTo(z.getBetreuungspensum()))
			.where(AbstractBetreuungsPensum::getUnitForDisplay, is(PensumUnits.valueOf(z.getPensumUnit().name())))
			.where(AbstractBetreuungsPensum::getMonatlicheBetreuungskosten, comparesEqualTo(z.getBetreuungskosten()))
			.where(AbstractBetreuungsPensum::getEingewoehnungPauschale, pojo(EingewoehnungPauschale.class)
				.where(EingewoehnungPauschale::getPauschale, comparesEqualTo(z.getEingewoehnung().getPauschale()))
				.where(EingewoehnungPauschale::getGueltigkeit, pojo(DateRange.class)
					.where(DateRange::getGueltigAb, is(z.getEingewoehnung().getVon()))
					.where(DateRange::getGueltigBis, is(z.getEingewoehnung().getBis()))
				))
			.where(AbstractBetreuungsPensum::getMonatlicheHauptmahlzeiten, comparesEqualTo(z.getAnzahlHauptmahlzeiten()))
			.where(AbstractBetreuungsPensum::getMonatlicheNebenmahlzeiten, comparesEqualTo(z.getAnzahlNebenmahlzeiten()))
			.where(AbstractBetreuungsPensum::getTarifProHauptmahlzeit, comparesEqualTo(z.getTarifProHauptmahlzeiten()))
			.where(AbstractBetreuungsPensum::getTarifProNebenmahlzeit, comparesEqualTo(z.getTarifProNebenmahlzeiten()))
			.where(AbstractBetreuungsPensum::getBetreuungInFerienzeit, comparesEqualTo(z.getBetreuungInFerienzeit()));
	}

	private void mockEinstellungen() {
		expect(mitteilungService.showSchulergaenzendeBetreuung(anyObject()))
			.andReturn(true)
			.anyTimes();

		expect(einstellungService.isEnabled(eq(EinstellungKey.GEMEINDE_MAHLZEITENVERGUENSTIGUNG_ENABLED), anyObject()))
			.andReturn(true)
			.anyTimes();

		expect(einstellungService.getEinstellungAsBigDecimal(eq(EinstellungKey.OEFFNUNGSTAGE_KITA), anyObject()))
			.andReturn(BigDecimal.valueOf(240))
			.anyTimes();

		expect(einstellungService.getEinstellungAsBigDecimal(eq(EinstellungKey.OEFFNUNGSTAGE_TFO), anyObject()))
			.andReturn(BigDecimal.valueOf(240))
			.anyTimes();

		expect(einstellungService.getEinstellungAsBigDecimal(eq(EinstellungKey.OEFFNUNGSSTUNDEN_TFO), anyObject()))
			.andReturn(BigDecimal.valueOf(11))
			.anyTimes();

		expect(einstellungService.findEinstellung(eq(EINGEWOEHNUNG_TYP), anyObject()))
			.andReturn(new Einstellung(EINGEWOEHNUNG_TYP, EingewoehnungTyp.PAUSCHALE.name(), mock(Gesuchsperiode.class)))
			.anyTimes();
	}
}
