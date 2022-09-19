/*
 * Copyright (C) 2022 DV Bern AG, Switzerland
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

package ch.dvbern.ebegu.lastenausgleich;

import java.util.Collection;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import ch.dvbern.ebegu.entities.Gemeinde;
import ch.dvbern.ebegu.entities.Lastenausgleich;
import ch.dvbern.ebegu.entities.LastenausgleichDetail;
import ch.dvbern.ebegu.entities.LastenausgleichGrundlagen;
import ch.dvbern.ebegu.entities.VerfuegungZeitabschnitt;
import ch.dvbern.ebegu.services.VerfuegungService;

public abstract class AbstractLastenausgleichRechner {

	private final VerfuegungService verfuegungService;

	AbstractLastenausgleichRechner(
		@Nonnull VerfuegungService verfuegungService
	) {
		this.verfuegungService = verfuegungService;
	}

	@Nullable
	public abstract LastenausgleichDetail createLastenausgleichDetail(
		@Nonnull Gemeinde gemeinde,
		@Nonnull Lastenausgleich lastenausgleich,
		@Nonnull LastenausgleichGrundlagen grundlagen
	);

	@Nonnull
	protected Collection<VerfuegungZeitabschnitt> getZeitabschnitte(@Nonnull Gemeinde gemeinde, int jahr) {
		return verfuegungService.findZeitabschnitteByYear(jahr, gemeinde);
	}

	@Nonnull
	public LastenausgleichDetail createLastenausgleichDetailKorrektur(
		@Nonnull LastenausgleichDetail detail
	) {
		detail.setTotalBelegungenMitSelbstbehalt(detail.getTotalBelegungenMitSelbstbehalt().negate());
		detail.setTotalAnrechenbar(detail.getTotalAnrechenbar().negate());
		detail.setTotalBetragGutscheineMitSelbstbehalt(detail.getTotalBetragGutscheineMitSelbstbehalt().negate());
		detail.setSelbstbehaltGemeinde(detail.getSelbstbehaltGemeinde().negate());
		detail.setBetragLastenausgleich(detail.getBetragLastenausgleich().negate());
		detail.setKorrektur(true);
		detail.setTotalBelegungenOhneSelbstbehalt(detail.getTotalBelegungenOhneSelbstbehalt().negate());
		detail.setTotalBetragGutscheineOhneSelbstbehalt(detail.getTotalBetragGutscheineOhneSelbstbehalt().negate());
		detail.setKostenFuerSelbstbehalt(detail.getKostenFuerSelbstbehalt().negate());
		return detail;
	}

	@Nonnull
	public abstract String logLastenausgleichRechnerType(int jahr);
}
