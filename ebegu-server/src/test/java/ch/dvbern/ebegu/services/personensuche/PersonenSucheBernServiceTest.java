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
 *
 */

package ch.dvbern.ebegu.services.personensuche;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import ch.dvbern.ebegu.dto.personensuche.EWKPerson;
import ch.dvbern.ebegu.dto.personensuche.EWKResultat;
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.finanzielleSituationRechner.FinanzielleSituationBernRechner;
import ch.dvbern.ebegu.test.TestDataUtil;
import ch.dvbern.ebegu.ws.ewk.GeresClient;
import org.easymock.EasyMockExtension;
import org.easymock.EasyMockSupport;
import org.easymock.Mock;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.easymock.EasyMock.expect;

@ExtendWith(EasyMockExtension.class)
class PersonenSucheBernServiceTest extends EasyMockSupport {

	private PersonenSucheBernService personenSucheService;

	@Mock
	private GeresClient geresClient;

	@BeforeEach
	void setUp() {
		personenSucheService = new PersonenSucheBernService(geresClient);
	}

	@Test
	void suchePersonByGesuch() throws Exception {
		// given
		Gesuch gesuch = TestDataUtil.createTestgesuchDagmar(new FinanzielleSituationBernRechner());
		var gs1 = Objects.requireNonNull(Objects.requireNonNull(gesuch.getGesuchsteller1()).getGesuchstellerJA());
		var kind1 = Objects.requireNonNull(Objects.requireNonNull(gesuch.getKindContainers().stream().collect(Collectors.toList()).get(0).getKindJA()));
		gesuch.getDossier().getGemeinde().setBfsNummer(351L);
		Assertions.assertNotNull(gesuch.getDossier().getGemeinde().getBfsNummer(), "bfs nummer muss gesetzt sein fuer suche");

		EWKResultat value = new EWKResultat();
		value.getPersonen().add(GeresTestUtil.ewkPersonFromEntity(gs1));
		expect(geresClient.suchePersonMitFallbackOhneVorname(gs1.getNachname(), gs1.getVorname(), gs1.getGeburtsdatum(), gs1.getGeschlecht(), 351L)).andReturn(
			value);
		expect(geresClient.suchePersonMitFallbackOhneVorname(gs1.getNachname(), gs1.getVorname(), gs1.getGeburtsdatum(), gs1.getGeschlecht())).andReturn(new EWKResultat());
		expect(geresClient.suchePersonMitFallbackOhneVorname(kind1.getNachname(), kind1.getVorname(), kind1.getGeburtsdatum(), kind1.getGeschlecht())).andReturn(new EWKResultat());

		replayAll();

		// when
		EWKResultat ewkResultat = personenSucheService.suchePersonen(gesuch);

		// verify
		Assertions.assertNotNull(ewkResultat);
		Assertions.assertEquals(2, ewkResultat.getPersonen().size()); // gs1 und gs2 sowie ein kind existieren im gesuch, wir erwarten IMMer fuer alle eine Antwort

		//found GS
		final List<EWKPerson> gesuchstellerRes = ewkResultat.getPersonen().stream().filter(EWKPerson::isGesuchsteller).collect(Collectors.toList());
		Assertions.assertEquals(1, gesuchstellerRes.size());

		//found kind
		final List<EWKPerson> kinderResults = ewkResultat.getPersonen().stream().filter(EWKPerson::isKind).collect(Collectors.toList());
		Assertions.assertEquals(1, kinderResults.size(), "Should find exactly the one Kind in the Gesuch");
		EWKPerson kindResult = kinderResults.get(0);
		Assertions.assertEquals(LocalDate.of(2014,4,13), kindResult.getGeburtsdatum());
		Assertions.assertEquals("Simon", kindResult.getVorname());
		Assertions.assertEquals("Wälti", kindResult.getNachname());
	}



}
