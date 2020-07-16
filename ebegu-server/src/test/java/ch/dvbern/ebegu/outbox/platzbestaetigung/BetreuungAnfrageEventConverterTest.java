/*
 * Copyright (C) 2020 DV Bern AG, Switzerland
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

package ch.dvbern.ebegu.outbox.platzbestaetigung;

import javax.annotation.Nonnull;

import ch.dvbern.ebegu.entities.Betreuung;
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.entities.Gesuchsteller;
import ch.dvbern.ebegu.entities.GesuchstellerContainer;
import ch.dvbern.ebegu.entities.Kind;
import ch.dvbern.ebegu.outbox.ExportedEvent;
import ch.dvbern.ebegu.test.TestDataUtil;
import ch.dvbern.kibon.exchange.commons.platzbestaetigung.BetreuungAnfrageEventDTO;
import ch.dvbern.kibon.exchange.commons.platzbestaetigung.GesuchstellerDTO;
import ch.dvbern.kibon.exchange.commons.platzbestaetigung.KindDTO;
import ch.dvbern.kibon.exchange.commons.types.BetreuungsangebotTyp;
import ch.dvbern.kibon.exchange.commons.util.AvroConverter;
import org.junit.Test;

import static com.spotify.hamcrest.pojo.IsPojo.pojo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class BetreuungAnfrageEventConverterTest {

	@Nonnull
	private final BetreuungAnfrageEventConverter converter = new BetreuungAnfrageEventConverter();

	@Test
	public void testCreatedEvent() {
		//die default Betreuung hat keinen Gesuch und keinen Gesuchsteller
		Betreuung kitaBetreuung = TestDataUtil.createDefaultBetreuung();
		Gesuchsteller gesuchsteller = TestDataUtil.createDefaultGesuchsteller();
		GesuchstellerContainer gesuchstellerContainer = new GesuchstellerContainer();
		gesuchstellerContainer.setGesuchstellerJA(gesuchsteller);
		Gesuch gesuch = new Gesuch();
		gesuch.setGesuchsteller1(gesuchstellerContainer);
		gesuch.setGesuchsperiode(TestDataUtil.createGesuchsperiode1718());
		kitaBetreuung.getKind().setGesuch(gesuch);
		Kind kind = kitaBetreuung.getKind().getKindJA();

		BetreuungAnfrageAddedEvent event = converter.of(kitaBetreuung);

		assertThat(event, is(pojo(ExportedEvent.class)
			.where(ExportedEvent::getAggregateId, is(kitaBetreuung.getId()))
			.where(ExportedEvent::getAggregateType, is("BetreuungAnfrage"))
			.where(ExportedEvent::getType, is("BetreuungAnfrageAdded")))
		);

		//noinspection deprecation
		BetreuungAnfrageEventDTO specificRecord = AvroConverter.fromAvroBinary(event.getSchema(), event.getPayload());

		assertThat(specificRecord, is(pojo(BetreuungAnfrageEventDTO.class)
			.where(BetreuungAnfrageEventDTO::getRefnr, is(kitaBetreuung.getBetreuungNummer().toString()))
			.where(BetreuungAnfrageEventDTO::getInstitutionId,
				is(kitaBetreuung.getInstitutionStammdaten().getInstitution().getId()))
			.where(BetreuungAnfrageEventDTO::getPeriodeVon,
				is(kitaBetreuung.extractGesuchsperiode().getGueltigkeit().getGueltigAb()))
			.where(BetreuungAnfrageEventDTO::getPeriodeBis,
				is(kitaBetreuung.extractGesuchsperiode().getGueltigkeit().getGueltigBis()))
			.where(BetreuungAnfrageEventDTO::getBetreuungsArt, is(BetreuungsangebotTyp.KITA))
			.where(BetreuungAnfrageEventDTO::getAbgelehntVonGesuchsteller, is(false))
			.where(BetreuungAnfrageEventDTO::getKind, is(pojo(KindDTO.class)
				.where(KindDTO::getNachname, is(kind.getNachname()))
				.where(KindDTO::getVorname, is(kind.getVorname()))
				.where(KindDTO::getGeburtsdatum, is(kind.getGeburtsdatum()))
			))
			.where(BetreuungAnfrageEventDTO::getGesuchsteller, is(pojo(GesuchstellerDTO.class)
				.where(GesuchstellerDTO::getNachname, is(gesuchsteller.getNachname()))
				.where(GesuchstellerDTO::getVorname, is(gesuchsteller.getVorname()))
				.where(GesuchstellerDTO::getEmail, is(gesuchsteller.getMail()))
			))
		));
	}

}
