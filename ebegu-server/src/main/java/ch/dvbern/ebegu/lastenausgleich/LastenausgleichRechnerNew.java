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

public class LastenausgleichRechnerNew extends AbstractLastenausgleichRechner {

	public LastenausgleichRechnerNew(@Nonnull VerfuegungService verfuegungService) {
		super(verfuegungService);
	}

	@Override
	@Nullable
	public LastenausgleichDetail createLastenausgleichDetail(
		@Nonnull Gemeinde gemeinde,
		@Nonnull Lastenausgleich lastenausgleich,
		@Nonnull LastenausgleichGrundlagen grundlagen
	) {
		abschnitteProGemeindeUndJahr =
			getZeitabschnitte(gemeinde, grundlagen.getJahr());
		if (abschnitteProGemeindeUndJahr.isEmpty()) {
			return null;
		}
		calculateTotals();

		// Eingabe Lastenausgleich = Total Belegung * 80%
		BigDecimal eingabeLastenausgleich =
			MathUtil.EXACT.multiplyNullSafe(totalGutscheine, BigDecimal.valueOf(0.8));

		// Selbstbehalt Gemeinde = Total Belegung * 20%
		BigDecimal selbstbehaltGemeinde =
			MathUtil.EXACT.subtractNullSafe(totalGutscheine, eingabeLastenausgleich);

		// Total anrechenbar => in neuer Berechnung ist total Anrechenbar das gleiche wie total Gutscheine
		BigDecimal totalAnrechenbar = totalBelegungInProzent;

		// Ohne Selbstbehalt Gemeinde Kosten = Total Gutscheine ohne Selbstbehalt * 0.2
		BigDecimal kostenOhneSelbstbehaltGemeinde = MathUtil.EXACT.multiplyNullSafe(
			totalGutscheineOhneSelbstbeahlt,
			BigDecimal.valueOf(0.2));

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
		sb.append("Lastenausgleichrechner ohne Selbstbehalt für Jahr ");
		sb.append(jahr);
		sb.append('\n');
	}
}
