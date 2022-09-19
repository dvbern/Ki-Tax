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

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import ch.dvbern.ebegu.entities.Gemeinde;
import ch.dvbern.ebegu.entities.Lastenausgleich;
import ch.dvbern.ebegu.entities.LastenausgleichDetail;
import ch.dvbern.ebegu.entities.LastenausgleichGrundlagen;
import ch.dvbern.ebegu.services.VerfuegungService;

public class LastenausgleichRechnerOhneSelbstbehalt extends AbstractLastenausgleichRechner {

	public LastenausgleichRechnerOhneSelbstbehalt(@Nonnull VerfuegungService verfuegungService) {
		super(verfuegungService);
	}

	@Override
	@Nullable
	public LastenausgleichDetail createLastenausgleichDetail(@Nonnull Gemeinde gemeinde, @Nonnull Lastenausgleich lastenausgleich, @Nonnull LastenausgleichGrundlagen grundlagen) {
		return null;
	}

	@Nonnull
	@Override
	public String logLastenausgleichRechnerType(int jahr) {
		return "Lastenausgleichrechner ohne Selbstbehalt für Jahr " + jahr;
	}
}
