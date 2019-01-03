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

package ch.dvbern.ebegu.pdfgenerator;

import java.math.BigDecimal;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class FinanzielleSituationRow {

	@Nonnull
	private String label;

	@Nonnull
	private String supertext;

	@Nonnull
	private String gs1;

	@Nonnull
	// @ToDo: Dies ist nur zum Testen. Sollte mit dem richtigen Wert gesetzt werden.
	private String gs1ValueOfGS = "1'000";

	@Nullable
	private String gs2;

	@Nonnull
	private String gs2ValueOfGS;


	public FinanzielleSituationRow(@Nonnull String label, @Nonnull String gs1) {
		this.label = label;
		this.gs1 = gs1;
	}

	public FinanzielleSituationRow(@Nonnull String label, @Nullable BigDecimal gs1) {
		this.label = label;
		this.gs1 = PdfUtil.printBigDecimal(gs1);
	}

	@Nonnull
	public String getLabel() {
		return label;
	}

	@Nonnull
	public String getGs1() {
		return gs1;
	}

	@Nullable
	public String getGs2ValueOfGS() {
		return gs2ValueOfGS;
	}

	@Nonnull
	public String getGs1ValueOfGS() {
		return gs1ValueOfGS;
	}

	@Nullable
	public String getGs2() {
		return gs2;
	}

	@Nullable
	public String getSupertext() {
		return supertext;
	}

	public void setSupertext(@Nonnull String supertext) {
		this.supertext = supertext;
	}

	public void setGs2(@Nullable String gs2) {
		this.gs2 = gs2;
	}

	public void setGs1(@Nullable BigDecimal gs1) {
		this.gs1 = PdfUtil.printBigDecimal(gs1);
	}

	public void setGs2(@Nullable BigDecimal gs2) {
		this.gs2 = PdfUtil.printBigDecimal(gs2);
	}
}
