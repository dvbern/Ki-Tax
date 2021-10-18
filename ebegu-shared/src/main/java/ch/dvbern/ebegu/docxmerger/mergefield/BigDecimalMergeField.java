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

package ch.dvbern.ebegu.docxmerger.mergefield;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class BigDecimalMergeField extends AbstractMergeField<BigDecimal> {

	private final int scale;
	private final DecimalFormat formater;

	public BigDecimalMergeField(@Nonnull String name, @Nullable BigDecimal value, @Nonnull String format) {
		this(name, value , 2, format);
	}

	public BigDecimalMergeField(@Nonnull String name, @Nullable BigDecimal value,  @Nonnull Integer precision, @Nonnull String format) {
		super(name, value);
		this.scale = precision;

		DecimalFormatSymbols symbols = new DecimalFormatSymbols();
		symbols.setGroupingSeparator('\'');
		this.formater = new DecimalFormat(format, symbols);
	}

	@Override
	@Nonnull
	public String getConvertedValue() {
		if (getValue() == null) {
			return "";
		}
		return formater.format(this.getValue().setScale(this.scale, RoundingMode.HALF_UP));
	}
}
