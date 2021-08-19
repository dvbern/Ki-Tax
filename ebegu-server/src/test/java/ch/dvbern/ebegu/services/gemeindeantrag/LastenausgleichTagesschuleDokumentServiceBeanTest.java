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
import java.util.Optional;

import ch.dvbern.ebegu.authentication.PrincipalBean;
import ch.dvbern.ebegu.docxmerger.lats.LatsDocxDTO;
import ch.dvbern.ebegu.entities.Adresse;
import ch.dvbern.ebegu.entities.Benutzer;
import ch.dvbern.ebegu.entities.Gemeinde;
import ch.dvbern.ebegu.entities.GemeindeStammdaten;
import ch.dvbern.ebegu.entities.gemeindeantrag.LastenausgleichTagesschuleAngabenGemeinde;
import ch.dvbern.ebegu.entities.gemeindeantrag.LastenausgleichTagesschuleAngabenGemeindeContainer;
import ch.dvbern.ebegu.enums.Sprache;
import ch.dvbern.ebegu.services.GemeindeService;
import org.easymock.EasyMockExtension;
import org.easymock.EasyMockSupport;
import org.easymock.Mock;
import org.easymock.TestSubject;
import org.junit.Assert;
import org.junit.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.easymock.EasyMock.expect;

@ExtendWith(EasyMockExtension.class)
public class LastenausgleichTagesschuleDokumentServiceBeanTest extends EasyMockSupport {

	@TestSubject
	private final LastenausgleichTagesschuleDokumentServiceBean serviceBean = new LastenausgleichTagesschuleDokumentServiceBean();

	@Mock
	private GemeindeService gemeindeServiceMock;

	@Mock
	private PrincipalBean principalMock;

	@Test
	public void testCalculations() {
		createGemeindeServiceMock();
		createPrincipalMock();
		replayAll();

		LatsDocxDTO dto = serviceBean.toLatsDocxDTO(createContainer(), new BigDecimal("2000"), Sprache.DEUTSCH);

		Assert.assertEquals(new BigDecimal("1000"), dto.getElterngebuehrenProg());
	}

	private void createGemeindeServiceMock() {
		expect(gemeindeServiceMock.getGemeindeStammdatenByGemeindeId(
			"abcd"
		)).andReturn(Optional.of(createGemeindeStammdaten()));

	}

	private void createPrincipalMock() {
		expect(principalMock.getBenutzer()).andReturn(createBenutzer());
	}

	private LastenausgleichTagesschuleAngabenGemeindeContainer createContainer() {
		LastenausgleichTagesschuleAngabenGemeindeContainer container = new LastenausgleichTagesschuleAngabenGemeindeContainer();
		LastenausgleichTagesschuleAngabenGemeinde korrektur = new LastenausgleichTagesschuleAngabenGemeinde();
		Gemeinde gemeinde = new Gemeinde();

		korrektur.setLastenausgleichberechtigteBetreuungsstunden(new BigDecimal("1000"));
		korrektur.setNormlohnkostenBetreuungBerechnet(new BigDecimal("10"));
		korrektur.setEinnahmenElterngebuehren(new BigDecimal("500"));

		container.setAngabenKorrektur(korrektur);
		container.setGemeinde(gemeinde);

		return container;
	}

	private GemeindeStammdaten createGemeindeStammdaten() {
		GemeindeStammdaten stammdaten = new GemeindeStammdaten();
		Adresse adresse = new Adresse();
		stammdaten.setAdresse(adresse);
		return stammdaten;
	}

	private Benutzer createBenutzer() {
		return new Benutzer();
	}
}
