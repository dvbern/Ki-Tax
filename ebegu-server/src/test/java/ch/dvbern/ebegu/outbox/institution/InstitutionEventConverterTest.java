/*
 * Copyright (C) 2019 DV Bern AG, Switzerland
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

package ch.dvbern.ebegu.outbox.institution;

import javax.annotation.Nonnull;

import ch.dvbern.ebegu.entities.Adresse;
import ch.dvbern.ebegu.entities.Institution;
import ch.dvbern.ebegu.entities.InstitutionStammdaten;
import ch.dvbern.ebegu.outbox.ExportedEvent;
import ch.dvbern.ebegu.test.TestDataUtil;
import ch.dvbern.kibon.exchange.commons.util.ObjectMapperUtil;
import com.fasterxml.jackson.databind.JsonNode;
import org.junit.Test;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.spotify.hamcrest.jackson.JsonMatchers.jsonObject;
import static com.spotify.hamcrest.jackson.JsonMatchers.jsonText;
import static com.spotify.hamcrest.pojo.IsPojo.pojo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class InstitutionEventConverterTest {

	@Nonnull
	private final InstitutionEventConverter converter = new InstitutionEventConverter();

	@Test
	public void testCreatedEvent() throws Exception {
		InstitutionStammdaten institutionStammdaten = TestDataUtil.createDefaultInstitutionStammdaten();
		Institution institution = institutionStammdaten.getInstitution();
		Adresse adresse = institutionStammdaten.getAdresse();

		InstitutionChangedEvent event = converter.of(institutionStammdaten);

		assertThat(event, is(pojo(ExportedEvent.class)
			.where(ExportedEvent::getAggregateId, is(institution.getId()))
			.where(ExportedEvent::getAggregateType, is("Institution"))
			.where(ExportedEvent::getType, is("InstitutionChanged")))
		);

		JsonNode jsonNode = ObjectMapperUtil.MAPPER.readTree(event.getPayload());

		assertThat(jsonNode, is(jsonObject()
			.where("id", is(jsonText(institution.getId())))
			.where("name", is(jsonText(institution.getName())))
			.where("traegerschaft", is(jsonText(checkNotNull(institution.getTraegerschaft()).getName())))
			.where("adresse", is(jsonObject()
				.where("strasse", is(jsonText(adresse.getStrasse())))
				.where("hausnummer", is(jsonText(adresse.getHausnummer())))
				.where("adresszusatz", is(jsonText(adresse.getZusatzzeile())))
				.where("ort", is(jsonText(adresse.getOrt())))
				.where("plz", is(jsonText(adresse.getPlz())))
				.where("land", is(jsonText(adresse.getLand().name())))
			))
		));
	}
}
