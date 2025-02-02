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

package ch.dvbern.ebegu.pdfgenerator.finanzielleSituation;

import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import ch.dvbern.ebegu.entities.Mandant;
import ch.dvbern.ebegu.pdfgenerator.PdfUtil;
import ch.dvbern.ebegu.util.ServerMessageUtil;
import com.google.errorprone.annotations.CanIgnoreReturnValue;

public class FinanzielleSituationRow {

	private static final String URSPRUENGLICH = "PdfGeneration_FinSit_Urspruenglich";
	public static final String LINE_FEED_SPACE = "\n ";

	@Nonnull
	private String label;

	@Nonnull
	private String supertext;

	@Nonnull
	private String gs1;

	@Nonnull
	private String gs1Urspruenglich;

	@Nullable
	private String gs2;

	@Nonnull
	private String gs2Urspruenglich;

	private boolean bold = false;


	public FinanzielleSituationRow(@Nonnull String label, @Nonnull String gs1) {
		this.label = label;
		this.gs1 = gs1;
	}

	public FinanzielleSituationRow(@Nonnull String label, @Nullable BigDecimal gs1) {
		this.label = label;
		this.gs1 = PdfUtil.printBigDecimal(gs1);
	}

	@CanIgnoreReturnValue
	public FinanzielleSituationRow withFooter(String footer, List<String> footers) {
		if (!footers.contains(footer)) {
			footers.add(footer);
		}
		setSupertext(" " + (footers.indexOf(footer) + 1));

		return this;
	}

	@CanIgnoreReturnValue
	public FinanzielleSituationRow bold() {
		return bold(true);
	}

	@CanIgnoreReturnValue
	public FinanzielleSituationRow bold(boolean printBold) {
		this.bold = printBold;
		return this;
	}

	public boolean isBold() {
		return bold;
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
	public String getGs2Urspruenglich() {
		return gs2Urspruenglich;
	}

	@Nullable
	public String getGs1Urspruenglich() {
		return gs1Urspruenglich;
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

	public void setGs1Urspruenglich(@Nullable BigDecimal gs1Urspruenglich, Locale locale, Mandant mandant) {
		this.gs1Urspruenglich = gs1Urspruenglich == null
			? LINE_FEED_SPACE + ServerMessageUtil.getMessage(URSPRUENGLICH, locale, mandant) + " -"
			: LINE_FEED_SPACE + ServerMessageUtil.getMessage(URSPRUENGLICH, locale, mandant) + PdfUtil.printBigDecimal(gs1Urspruenglich);
	}

	public void setGs2Urspruenglich(@Nullable BigDecimal gs2Urspruenglich, Locale locale, Mandant mandant) {
		this.gs2Urspruenglich = gs2Urspruenglich == null
			? LINE_FEED_SPACE + ServerMessageUtil.getMessage(URSPRUENGLICH, locale, mandant) + " -"
			: LINE_FEED_SPACE + ServerMessageUtil.getMessage(URSPRUENGLICH, locale, mandant) + PdfUtil.printBigDecimal(gs2Urspruenglich);
	}
}
