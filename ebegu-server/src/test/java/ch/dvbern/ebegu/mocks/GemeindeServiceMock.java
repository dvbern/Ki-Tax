/*
 * Copyright (C) 2018 DV Bern AG, Switzerland
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

package ch.dvbern.ebegu.mocks;

import java.util.Optional;

import javax.annotation.Nonnull;

import ch.dvbern.ebegu.entities.Gemeinde;
import ch.dvbern.ebegu.entities.GemeindeStammdaten;
import ch.dvbern.ebegu.entities.Mandant;
import ch.dvbern.ebegu.services.GemeindeServiceBean;
import ch.dvbern.ebegu.test.TestDataUtil;
import ch.dvbern.ebegu.util.mandant.MandantIdentifier;

public class GemeindeServiceMock extends GemeindeServiceBean {

	@Nonnull
	@Override
	public Optional<GemeindeStammdaten> getGemeindeStammdatenByGemeindeId(
		@Nonnull String gemeindeId
	) {
		GemeindeStammdaten gemeindeWithStammdaten = TestDataUtil.createGemeindeWithStammdaten();
		// Aktuell wird als Adresse immer Jugendamt verwendet
		gemeindeWithStammdaten.getAdresse().setOrganisation("Jugendamt");
		return Optional.of(gemeindeWithStammdaten);
	}

	@Nonnull
	@Override
	public Optional<Gemeinde> findGemeinde(@Nonnull String id) {
		Gemeinde gemeinde = new Gemeinde();
		Mandant mandant = new Mandant();
		mandant.setMandantIdentifier(MandantIdentifier.BERN);
		gemeinde.setMandant(mandant);
		return Optional.of(gemeinde);
	}
}
