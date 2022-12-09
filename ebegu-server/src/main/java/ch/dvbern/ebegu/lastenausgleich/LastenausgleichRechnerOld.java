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
import java.util.Objects;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import ch.dvbern.ebegu.entities.Gemeinde;
import ch.dvbern.ebegu.entities.Lastenausgleich;
import ch.dvbern.ebegu.entities.LastenausgleichDetail;
import ch.dvbern.ebegu.entities.LastenausgleichDetailZeitabschnitt;
import ch.dvbern.ebegu.entities.LastenausgleichGrundlagen;
import ch.dvbern.ebegu.services.VerfuegungService;
import ch.dvbern.ebegu.util.MathUtil;

public class LastenausgleichRechnerOld extends AbstractLastenausgleichRechner {

	public LastenausgleichRechnerOld(@Nonnull VerfuegungService verfuegungService) {
		super(verfuegungService);
	}

	@Override
	@Nullable
	public LastenausgleichDetail createLastenausgleichDetail(@Nonnull Gemeinde gemeinde, @Nonnull Lastenausgleich lastenausgleich, @Nonnull LastenausgleichGrundlagen grundlagen) {
		abschnitteProGemeindeUndJahr = getZeitabschnitte(gemeinde, grundlagen.getJahr());
		if (abschnitteProGemeindeUndJahr.isEmpty()) {
			return null;
		}
		calculateTotals();

		// Mit alter Berechnung dürfen diese beiden Werte nicht NULL sein
		Objects.requireNonNull(grundlagen.getSelbstbehaltPro100ProzentPlatz());
		Objects.requireNonNull(grundlagen.getKostenPro100ProzentPlatz());

		// Selbstbehalt Gemeinde = Total Belegung * Kosten pro 100% Platz * 20%
		BigDecimal totalBelegung = MathUtil.EXACT.divide(totalBelegungInProzent, MathUtil.EXACT.from(100));
		BigDecimal selbstbehaltGemeinde =
			MathUtil.EXACT.multiplyNullSafe(totalBelegung, grundlagen.getSelbstbehaltPro100ProzentPlatz());
		// Eingabe Lastenausgleich = Total Gutscheine - Selbstbehalt Gemeinde
		BigDecimal eingabeLastenausgleich = MathUtil.EXACT.subtractNullSafe(totalGutscheine, selbstbehaltGemeinde);

		// Total anrechenbar = total belegung * Kosten pro 100% Platz
		BigDecimal totalAnrechenbar =
			MathUtil.EXACT.multiplyNullSafe(totalBelegung, grundlagen.getKostenPro100ProzentPlatz());

		// Ohne Selbstbehalt Gemeinde Kosten = Total Belegung ohne Selbstbehalt * Selbstbehalt pro 100% Platz
		BigDecimal totalBelegungOhneSelbstbehalt =
			MathUtil.EXACT.divide(totalBelegungOhneSelbstbeahltInProzent, MathUtil.EXACT.from(100));
		BigDecimal kostenOhneSelbstbehaltGemeinde = MathUtil.EXACT.multiplyNullSafe(
			totalBelegungOhneSelbstbehalt,
			grundlagen.getSelbstbehaltPro100ProzentPlatz());

		LastenausgleichDetail detail = new LastenausgleichDetail();
		detail.setJahr(grundlagen.getJahr());
		detail.setGemeinde(gemeinde);
		detail.setTotalBelegungenMitSelbstbehalt(MathUtil.toTwoKommastelle(totalBelegungInProzent));
		detail.setTotalAnrechenbar(MathUtil.toTwoKommastelle(totalAnrechenbar));
		detail.setTotalBetragGutscheineMitSelbstbehalt(MathUtil.toTwoKommastelle(totalGutscheine));
		detail.setSelbstbehaltGemeinde(MathUtil.toTwoKommastelle(selbstbehaltGemeinde));
		detail.setBetragLastenausgleich(MathUtil.toTwoKommastelle(eingabeLastenausgleich));
		detail.setLastenausgleich(lastenausgleich);
		detail.setKorrektur(lastenausgleich.getJahr().compareTo(grundlagen.getJahr()) != 0);
		detail.setTotalBelegungenOhneSelbstbehalt(MathUtil.toTwoKommastelle(totalBelegungOhneSelbstbeahltInProzent));
		detail.setTotalBetragGutscheineOhneSelbstbehalt(MathUtil.toTwoKommastelle(totalGutscheineOhneSelbstbeahlt));
		detail.setKostenFuerSelbstbehalt(MathUtil.toTwoKommastelle(kostenOhneSelbstbehaltGemeinde));
		var detailZeitabschnitte = abschnitteProGemeindeUndJahr
			.stream()
			.map(a -> new LastenausgleichDetailZeitabschnitt(a, detail))
			.collect(Collectors.toList());
		detail.setLastenausgleichDetailZeitabschnitte(detailZeitabschnitte);

		return detail;
	}

	@Override
	public void logLastenausgleichRechnerType(int jahr, StringBuilder sb) {
		sb.append("Lastenausgleichrechner mit Selbstbehalt pro 100% Platz für Jahr ");
		sb.append(jahr);
		sb.append('\n');
	}
}
