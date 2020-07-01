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

package ch.dvbern.ebegu.enums;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Month;

import ch.dvbern.ebegu.util.MathUtil;

public interface RueckforderungsConstants {

	BigDecimal entschaedigungProTagKita = MathUtil.DEFAULT.from(25);
	BigDecimal entschaedigungProStundeTfo = MathUtil.DEFAULT.from(2); // TODO (team) wieviel ist es bei TFO?

	LocalDate einreichungsfristOeffentlichStufe2 = LocalDate.of(2020, Month.JULY, 31);
	LocalDate einreichungsfristPrivatStufe2 = LocalDate.of(2020, Month.JULY, 17);

}
