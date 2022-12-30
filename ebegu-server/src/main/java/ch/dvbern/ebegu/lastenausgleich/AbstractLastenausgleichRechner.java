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

import java.math.BigDecimal;
import java.util.Collection;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import ch.dvbern.ebegu.entities.Betreuung;
import ch.dvbern.ebegu.entities.Gemeinde;
import ch.dvbern.ebegu.entities.Lastenausgleich;
import ch.dvbern.ebegu.entities.LastenausgleichDetail;
import ch.dvbern.ebegu.entities.LastenausgleichDetailZeitabschnitt;
import ch.dvbern.ebegu.entities.LastenausgleichGrundlagen;
import ch.dvbern.ebegu.entities.VerfuegungZeitabschnitt;
import ch.dvbern.ebegu.services.VerfuegungService;
import ch.dvbern.ebegu.util.DateUtil;
import ch.dvbern.ebegu.util.MathUtil;

public abstract class AbstractLastenausgleichRechner {

	private final VerfuegungService verfuegungService;

	protected Collection<VerfuegungZeitabschnitt> abschnitteProGemeindeUndJahr;
	// Total Belegung = Totals aller Pensum * AnteilDesMonats / 12
	protected BigDecimal totalBelegungInProzent = BigDecimal.ZERO;
	// Total Gutscheine: Totals aller aktuell gültigen Zeitabschnitte, die im Kalenderjahr liegen
	protected BigDecimal totalGutscheine = BigDecimal.ZERO;
	// Total Belegung = Totals aller Pensum ohne selbstbehalt * AnteilDesMonats / 12
	protected BigDecimal totalBelegungOhneSelbstbeahltInProzent = BigDecimal.ZERO;
	// Total Gutscheine: Totals aller aktuell gültigen Zeitabschnitte ohne Selbstbehalt, die im Kalenderjahr liegen
	protected BigDecimal totalGutscheineOhneSelbstbeahlt = BigDecimal.ZERO;

	AbstractLastenausgleichRechner(@Nonnull VerfuegungService verfuegungService) {
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

	protected void setLastenausgleichDetailZeitabschnitte(LastenausgleichDetail detail) {
		var detailZeitabschnitte = abschnitteProGemeindeUndJahr
			.stream()
			.map(a -> new LastenausgleichDetailZeitabschnitt(a, detail))
			.collect(Collectors.toList());
		detail.setLastenausgleichDetailZeitabschnitte(detailZeitabschnitte);
	}

	public abstract void logLastenausgleichRechnerType(int jahr, StringBuilder sb);

	protected void calculateTotals() {
		for (VerfuegungZeitabschnitt abschnitt : abschnitteProGemeindeUndJahr) {
			BigDecimal anteilKalenderjahr = getAnteilKalenderjahr(abschnitt);
			BigDecimal gutschein = abschnitt.getBgCalculationResultAsiv().getVerguenstigung();
			Betreuung betreuung = abschnitt.getVerfuegung().getBetreuung();
			if (betreuung != null
				&& betreuung.getKind().getKeinSelbstbehaltDurchGemeinde() != null
				&& betreuung.getKind().getKeinSelbstbehaltDurchGemeinde()) {
				totalBelegungOhneSelbstbeahltInProzent =
					MathUtil.EXACT.addNullSafe(totalBelegungOhneSelbstbeahltInProzent, anteilKalenderjahr);
				totalGutscheineOhneSelbstbeahlt =
					MathUtil.EXACT.addNullSafe(totalGutscheineOhneSelbstbeahlt, gutschein);
			} else {
				totalBelegungInProzent = MathUtil.EXACT.addNullSafe(totalBelegungInProzent, anteilKalenderjahr);
				totalGutscheine = MathUtil.EXACT.addNullSafe(totalGutscheine, gutschein);
			}

		}
	}

	@Nonnull
	private BigDecimal getAnteilKalenderjahr(@Nonnull VerfuegungZeitabschnitt zeitabschnitt) {
		// Pensum * AnteilDesMonats / 12. Beispiel 80% ganzer Monat = 6.67% AnteilKalenderjahr
		BigDecimal anteilMonat = DateUtil.calculateAnteilMonatInklWeekend(
			zeitabschnitt.getGueltigkeit().getGueltigAb(),
			zeitabschnitt.getGueltigkeit().getGueltigBis());
		BigDecimal pensum = zeitabschnitt.getBgCalculationResultAsiv().getBgPensumProzent();
		BigDecimal pensumAnteilMonat = MathUtil.EXACT.multiplyNullSafe(anteilMonat, pensum);
		return MathUtil.EXACT.divide(pensumAnteilMonat, MathUtil.EXACT.from(12d));
	}
}
