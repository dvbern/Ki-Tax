/*
 * Ki-Tax: System for the management of external childcare subsidies
 * Copyright (C) 2017 City of Bern Switzerland
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package ch.dvbern.ebegu.vorlagen.finanziellesituation;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import ch.dvbern.ebegu.entities.AbstractFinanzielleSituation;
import ch.dvbern.ebegu.util.FinanzielleSituationRechner;
import ch.dvbern.ebegu.util.MathUtil;
import ch.dvbern.ebegu.util.ServerMessageUtil;

import static java.util.Objects.requireNonNull;

/**
 * DTO fuer die Finazdaten die im Print gebraucht werden, implementiert den FinanzielleSituationPrint
 */
public abstract class FinanzDatenPrintImpl implements FinanzDatenPrint {

	public static final int DEFAULT_WERT = 123456;

	@Nullable
	private final FinanzSituationPrintGesuchsteller fsGesuchsteller1;

	@Nullable
	private final FinanzSituationPrintGesuchsteller fsGesuchsteller2;

	/**
	 * Konstruktor
	 *
	 * @param fsGesuchsteller1 {@link AbstractFinanzielleSituation}
	 * @param fsGesuchsteller2 {@link AbstractFinanzielleSituation}
	 */
	protected FinanzDatenPrintImpl(
		@Nullable FinanzSituationPrintGesuchsteller fsGesuchsteller1,
		@Nullable FinanzSituationPrintGesuchsteller fsGesuchsteller2) {

		this.fsGesuchsteller1 = fsGesuchsteller1;
		this.fsGesuchsteller2 = fsGesuchsteller2;

	}

	@Nullable
	@Override
	public final BigDecimal getNettolohnG1() {

		return requireNonNull(this.getFinanzSituationGS1()).getNettolohn();

	}

	@Nullable
	@Override
	public final BigDecimal getNettolohnG2() {
		return fsGesuchsteller2 != null ? requireNonNull(this.getFinanzSituationGS2()).getNettolohn() : null;

	}

	@Override
	public final BigDecimal getFamilienzulagenG1() {

		return requireNonNull(this.getFinanzSituationGS1()).getFamilienzulage();

	}

	@Nullable
	@Override
	public final BigDecimal getFamilienzulagenG2() {
		return this.getFinanzSituationGS2() != null ? this.getFinanzSituationGS2().getFamilienzulage() : null;

	}

	@Override
	public final BigDecimal getErsatzeinkommenG1() {

		return requireNonNull(this.getFinanzSituationGS1()).getErsatzeinkommen();
	}

	@Nullable
	@Override
	public final BigDecimal getErsatzeinkommenG2() {
		return this.getFinanzSituationGS2() != null ? this.getFinanzSituationGS2().getErsatzeinkommen() : null;

	}

	@Override
	public final BigDecimal getUnterhaltsbeitraegeG1() {
		return requireNonNull(this.getFinanzSituationGS1()).getErhalteneAlimente();
	}

	@Nullable
	@Override
	public final BigDecimal getGeleisteteUnterhaltsbeitraegeG1() {
		return this.getFinanzSituationGS1() != null ? this.getFinanzSituationGS1().getGeleisteteAlimente() : null;
	}

	@Nullable
	@Override
	public final BigDecimal getGeleisteteUnterhaltsbeitraegeG2() {
		return this.getFinanzSituationGS2() != null ? this.getFinanzSituationGS2().getGeleisteteAlimente() : null;
	}

	@Nullable
	@Override
	public final BigDecimal getUnterhaltsbeitraegeG2() {
		return this.getFinanzSituationGS2() != null ? this.getFinanzSituationGS2().getErhalteneAlimente() : null;
	}

	@Nullable
	@Override
	public BigDecimal getGeschaeftsgewinnG1() {
		if (fsGesuchsteller1 != null) {
			return FinanzielleSituationRechner.calcGeschaeftsgewinnDurchschnitt(fsGesuchsteller1.getFinanzielleSituation());
		}
		return null;
	}

	@Nullable
	@Override
	public BigDecimal getGeschaeftsgewinnG2() {
		if (fsGesuchsteller2 != null) {
			return FinanzielleSituationRechner.calcGeschaeftsgewinnDurchschnitt(fsGesuchsteller2.getFinanzielleSituation());
		}
		return null;
	}

	@Nullable
	@Override
	public final BigDecimal getZwischentotalEinkuenfteG1() {

		BigDecimal nettolohn = getNettolohnG1() != null ? getNettolohnG1() : BigDecimal.ZERO;
		BigDecimal familienzulagen = getFamilienzulagenG1() != null ? getFamilienzulagenG1() : BigDecimal.ZERO;
		BigDecimal ersatzeinkommen = getErsatzeinkommenG1() != null ? getErsatzeinkommenG1() : BigDecimal.ZERO;
		BigDecimal unterhaltsbeitraege = getUnterhaltsbeitraegeG1() != null ?
			getUnterhaltsbeitraegeG1() :
			BigDecimal.ZERO;
		BigDecimal geschaeftsgewinn = getGeschaeftsgewinnG1() != null ? getGeschaeftsgewinnG1() : BigDecimal.ZERO;
		return MathUtil.EXACT.add(nettolohn, familienzulagen, ersatzeinkommen, unterhaltsbeitraege, geschaeftsgewinn);
	}

	@Nullable
	@Override
	public final BigDecimal getZwischentotalEinkuenfteG2() {

		BigDecimal nettolohn = getNettolohnG2() != null ? getNettolohnG2() : BigDecimal.ZERO;
		BigDecimal familienzulagen = getFamilienzulagenG2() != null ? getFamilienzulagenG2() : BigDecimal.ZERO;
		BigDecimal ersatzeinkommen = getErsatzeinkommenG2() != null ? getErsatzeinkommenG2() : BigDecimal.ZERO;
		BigDecimal unterhaltsbeitraege = getUnterhaltsbeitraegeG2() != null ?
			getUnterhaltsbeitraegeG2() :
			BigDecimal.ZERO;
		BigDecimal geschaeftsgewinn = getGeschaeftsgewinnG2() != null ? getGeschaeftsgewinnG2() : BigDecimal.ZERO;
		return MathUtil.EXACT.add(nettolohn, familienzulagen, ersatzeinkommen, unterhaltsbeitraege, geschaeftsgewinn);
	}

	@Override
	public final BigDecimal getTotalEinkuenfte() {

		BigDecimal total1 = getZwischentotalEinkuenfteG1() != null ? getZwischentotalEinkuenfteG1() : BigDecimal.ZERO;
		BigDecimal total2 = getZwischentotalEinkuenfteG2() != null ? getZwischentotalEinkuenfteG2() : BigDecimal.ZERO;

		return MathUtil.EXACT.add(total1, total2);
	}

	@Override
	public final BigDecimal getBruttovermoegenG1() {
		return requireNonNull(this.getFinanzSituationGS1()).getBruttovermoegen();
	}

	@Nullable
	@Override
	public final BigDecimal getBruttovermoegenG2() {
		if (this.getFinanzSituationGS2() != null) {
			return this.getFinanzSituationGS2().getBruttovermoegen();
		}
		return null;
	}

	@Override
	public final BigDecimal getSchuldenG1() {
		return requireNonNull(this.getFinanzSituationGS1()).getSchulden();
	}

	@Nullable
	@Override
	public final BigDecimal getSchuldenG2() {
		if (fsGesuchsteller2 != null && this.getFinanzSituationGS2() != null) {
			return this.getFinanzSituationGS2().getSchulden();
		}
		return null;
	}

	@Override
	public final BigDecimal getZwischentotalNettovermoegenBeiderGesuchsteller1() {
		BigDecimal bruttovermoegen = getBruttovermoegenG1() != null ? getBruttovermoegenG1() : BigDecimal.ZERO;
		BigDecimal schulden = getSchuldenG1() != null ? getSchuldenG1() : BigDecimal.ZERO;

		return MathUtil.EXACT.subtract(bruttovermoegen, schulden);
	}

	@Override
	public final BigDecimal getZwischentotalNettovermoegenBeiderGesuchsteller2() {
		BigDecimal bruttovermoegen = getBruttovermoegenG2() != null ? getBruttovermoegenG2() : BigDecimal.ZERO;
		BigDecimal schulden = getSchuldenG2() != null ? getSchuldenG2() : BigDecimal.ZERO;

		return MathUtil.EXACT.subtract(bruttovermoegen, schulden);

	}

	@Override
	public final BigDecimal getZwischentotalNettovermoegenInsgesamt() {
		BigDecimal nettovermoegenG1 = getZwischentotalNettovermoegenBeiderGesuchsteller1() != null ?
			getZwischentotalNettovermoegenBeiderGesuchsteller1() : BigDecimal.ZERO;
		BigDecimal nettovermoegenG2 = getZwischentotalNettovermoegenBeiderGesuchsteller2() != null ?
			getZwischentotalNettovermoegenBeiderGesuchsteller2() : BigDecimal.ZERO;

		return MathUtil.EXACT.add(nettovermoegenG1, nettovermoegenG2);
	}

	//das ist eigentlich der nettovermoegensabzug
	@Override
	@Nonnull
	public final BigDecimal getNettovermoegen() {
		return FinanzielleSituationRechner.calcVermoegen5Prozent(this.getFinanzSituationGS1(),
			this.getFinanzSituationGS2());
	}

	@Override
	public final BigDecimal getTotalAbzuege() {

		BigDecimal unterhaltGS1 = getGeleisteteUnterhaltsbeitraegeG1() != null ?
			getGeleisteteUnterhaltsbeitraegeG1() :
			BigDecimal.ZERO;
		BigDecimal unterhaltGS2 = getGeleisteteUnterhaltsbeitraegeG2() != null ?
			getGeleisteteUnterhaltsbeitraegeG2() :
			BigDecimal.ZERO;

		return MathUtil.EXACT.add(unterhaltGS1, unterhaltGS2);
	}

	@Override
	public final BigDecimal getZusammenzugTotaleinkuenfte() {
		return getTotalEinkuenfte();
	}

	@Override
	public final BigDecimal getZusammenzugNettovermoegen() {
		return getNettovermoegen();
	}

	@Override
	public final BigDecimal getZusammenzugTotalAbzuege() {
		return getTotalAbzuege();
	}

	@Override
	public final BigDecimal getMassgebendesEinkommen() {
		BigDecimal totalEinkuenfte = getZusammenzugTotaleinkuenfte();
		BigDecimal vermoegenszuschlag = getZusammenzugNettovermoegen();
		BigDecimal abzug = getZusammenzugTotalAbzuege();
		BigDecimal totalEinkommen = MathUtil.EXACT.add(totalEinkuenfte, vermoegenszuschlag);

		return MathUtil.EXACT.subtract(totalEinkommen, abzug);
	}

	@Nullable
	protected AbstractFinanzielleSituation getFinanzSituationGS1() {
		if (fsGesuchsteller1 != null) {
			return this.fsGesuchsteller1.getFinanzielleSituation();
		}
		return null;
	}

	@Nullable
	protected AbstractFinanzielleSituation getFinanzSituationGS2() {
		if (fsGesuchsteller2 != null) {
			return this.fsGesuchsteller2.getFinanzielleSituation();
		}
		return null;
	}

	@Override
	public String getDateCreate() {
		final String datePattern = ServerMessageUtil.getMessage("date_pattern");
		LocalDate date = LocalDate.now();
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern(datePattern);

		return date.format(formatter);
	}

	@Nullable
	public FinanzSituationPrintGesuchsteller getFsGesuchsteller1() {
		return fsGesuchsteller1;
	}

	@Nullable
	public FinanzSituationPrintGesuchsteller getFsGesuchsteller2() {
		return fsGesuchsteller2;
	}
}
