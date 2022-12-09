/*
 * Copyright (C) 2021 DV Bern AG, Switzerland
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package ch.dvbern.ebegu.services.gemeindeantrag;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Optional;

import ch.dvbern.ebegu.docxmerger.lats.LatsDocxDTO;
import ch.dvbern.ebegu.entities.Adresse;
import ch.dvbern.ebegu.entities.Einstellung;
import ch.dvbern.ebegu.entities.Gemeinde;
import ch.dvbern.ebegu.entities.GemeindeStammdaten;
import ch.dvbern.ebegu.entities.GemeindeStammdatenKorrespondenz;
import ch.dvbern.ebegu.entities.Gesuchsperiode;
import ch.dvbern.ebegu.entities.Mandant;
import ch.dvbern.ebegu.entities.gemeindeantrag.LastenausgleichTagesschuleAngabenGemeinde;
import ch.dvbern.ebegu.entities.gemeindeantrag.LastenausgleichTagesschuleAngabenGemeindeContainer;
import ch.dvbern.ebegu.enums.EinstellungKey;
import ch.dvbern.ebegu.enums.Sprache;
import ch.dvbern.ebegu.mocks.PrincipalBeanMock;
import ch.dvbern.ebegu.services.EinstellungService;
import ch.dvbern.ebegu.services.GemeindeService;
import ch.dvbern.ebegu.services.GesuchsperiodeService;
import ch.dvbern.ebegu.types.DateRange;
import ch.dvbern.ebegu.util.mandant.MandantIdentifier;
import org.easymock.EasyMockExtension;
import org.easymock.EasyMockSupport;
import org.easymock.Mock;
import org.easymock.TestSubject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.easymock.EasyMock.expect;

@ExtendWith(EasyMockExtension.class)
public class LastenausgleichTagesschuleDokumentServiceBeanTest extends EasyMockSupport {

	@TestSubject
	private final LastenausgleichTagesschuleDokumentServiceBean serviceBean = new LastenausgleichTagesschuleDokumentServiceBean();

	@Mock()
	private GemeindeService gemeindeServiceMock;

	@Mock()
	private GesuchsperiodeService gesuchsperiodeServiceMock;

	@Mock()
	private EinstellungService einstellungServiceMock;

	private static LastenausgleichTagesschuleAngabenGemeindeContainer container;
	private static Gesuchsperiode gesuchsperiodeOfPrognose = createGesuchsperiode();

	@BeforeAll()
	public static void beforeAll() {
		container = createContainer();
	}

	@BeforeEach()
	public void beforeEach() {
		createGemeindeServiceMock();
		createGesuchsperiodeServiceMock();
		createEinstellungServiceMock();
		replayAll();
		serviceBean.principalBean = new PrincipalBeanMock();
	}

	@Test
	public void testCalculations() {
		LatsDocxDTO dto = serviceBean.toLatsDocxDTO(container, new BigDecimal("2000"), Sprache.DEUTSCH);

		Assertions.assertEquals(new BigDecimal("1000"), dto.getElterngebuehrenProg().setScale(0, RoundingMode.CEILING));
		Assertions.assertEquals(new BigDecimal("20700"), dto.getNormlohnkostenTotalProg().setScale(0, RoundingMode.CEILING));
		Assertions.assertEquals(new BigDecimal("19700"), dto.getLastenausgleichsberechtigterBetragProg().setScale(0 , RoundingMode.CEILING));
		Assertions.assertEquals(new BigDecimal("9850"), dto.getErsteRateProg().setScale(0, RoundingMode.CEILING));
		Assertions.assertEquals(new BigDecimal("7000"), dto.getZweiteRate().setScale(0, RoundingMode.CEILING));
		Assertions.assertEquals(new BigDecimal("16850"), dto.getAuszahlungTotal().setScale(0, RoundingMode.CEILING));
	}

	private void createGemeindeServiceMock() {
		expect(gemeindeServiceMock.getGemeindeStammdatenByGemeindeId(
			container.getGemeinde().getId()
		))
			.andReturn(Optional.of(createGemeindeStammdaten()))
			.anyTimes();

	}

	private void createGesuchsperiodeServiceMock() {
		expect(gesuchsperiodeServiceMock.getNachfolgendeGesuchsperiode(
			container.getGesuchsperiode()
		))
			.andReturn(Optional.of(gesuchsperiodeOfPrognose))
			.anyTimes();

	}
	private void createEinstellungServiceMock() {
		expect(einstellungServiceMock.findEinstellung(
			EinstellungKey.LATS_LOHNNORMKOSTEN,
			container.getGemeinde(),
			container.getGesuchsperiode()
		))
			.andReturn(new Einstellung(EinstellungKey.LATS_LOHNNORMKOSTEN, "10.35", gesuchsperiodeOfPrognose))
			.anyTimes();

		expect(einstellungServiceMock.findEinstellung(
			EinstellungKey.LATS_LOHNNORMKOSTEN_LESS_THAN_50,
			container.getGemeinde(),
			container.getGesuchsperiode()
		))
			.andReturn(new Einstellung(EinstellungKey.LATS_LOHNNORMKOSTEN_LESS_THAN_50, "5.35", gesuchsperiodeOfPrognose))
			.anyTimes();

		expect(einstellungServiceMock.findEinstellung(
			EinstellungKey.LATS_LOHNNORMKOSTEN,
			container.getGemeinde(),
			gesuchsperiodeOfPrognose
		))
			.andReturn(new Einstellung(EinstellungKey.LATS_LOHNNORMKOSTEN, "10.35", gesuchsperiodeOfPrognose))
			.anyTimes();

		expect(einstellungServiceMock.findEinstellung(
			EinstellungKey.LATS_LOHNNORMKOSTEN_LESS_THAN_50,
			container.getGemeinde(),
			gesuchsperiodeOfPrognose
		))
			.andReturn(new Einstellung(EinstellungKey.LATS_LOHNNORMKOSTEN_LESS_THAN_50, "5.35", gesuchsperiodeOfPrognose))
			.anyTimes();

	}

	private static LastenausgleichTagesschuleAngabenGemeindeContainer createContainer() {
		LastenausgleichTagesschuleAngabenGemeindeContainer container = new LastenausgleichTagesschuleAngabenGemeindeContainer();
		LastenausgleichTagesschuleAngabenGemeinde korrektur = new LastenausgleichTagesschuleAngabenGemeinde();
		Gemeinde gemeinde = new Gemeinde();
		Mandant mandant = new Mandant();
		mandant.setMandantIdentifier(MandantIdentifier.BERN);
		mandant.setName("Kanton Bern");
		gemeinde.setMandant(mandant);

		korrektur.setLastenausgleichberechtigteBetreuungsstunden(new BigDecimal("1000"));
		korrektur.setNormlohnkostenBetreuungBerechnet(new BigDecimal("10000"));
		korrektur.setEinnahmenElterngebuehren(new BigDecimal("500"));
		korrektur.setLastenausgleichsberechtigerBetrag(new BigDecimal("10000"));
		korrektur.setErsteRateAusbezahlt(new BigDecimal("3000"));
		korrektur.setDavonStundenZuNormlohnMehrAls50ProzentAusgebildete(new BigDecimal("1000"));

		Gesuchsperiode gesuchsperiode = createGesuchsperiode();

		container.setAngabenKorrektur(korrektur);
		container.setGemeinde(gemeinde);
		container.setGesuchsperiode(gesuchsperiode);

		return container;
	}

	private static Gesuchsperiode createGesuchsperiode() {
		Gesuchsperiode gesuchsperiode = new Gesuchsperiode();
		gesuchsperiode.setGueltigkeit(new DateRange());
		return gesuchsperiode;
	}

	private GemeindeStammdaten createGemeindeStammdaten() {
		GemeindeStammdaten stammdaten = new GemeindeStammdaten();
		Adresse adresse = new Adresse();
		stammdaten.setAdresse(adresse);

		Gemeinde gemeinde = new Gemeinde();
		gemeinde.setBfsNummer(99999L);
		stammdaten.setGemeinde(gemeinde);

		GemeindeStammdatenKorrespondenz gemeindeStammdatenKorrespondenz = new GemeindeStammdatenKorrespondenz();
		stammdaten.setGemeindeStammdatenKorrespondenz(gemeindeStammdatenKorrespondenz);

		return stammdaten;
	}
}
