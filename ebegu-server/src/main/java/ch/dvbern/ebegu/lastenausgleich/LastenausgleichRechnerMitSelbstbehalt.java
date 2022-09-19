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

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import ch.dvbern.ebegu.entities.Betreuung;
import ch.dvbern.ebegu.entities.Gemeinde;
import ch.dvbern.ebegu.entities.Lastenausgleich;
import ch.dvbern.ebegu.entities.LastenausgleichDetail;
import ch.dvbern.ebegu.entities.LastenausgleichGrundlagen;
import ch.dvbern.ebegu.entities.VerfuegungZeitabschnitt;
import ch.dvbern.ebegu.services.VerfuegungService;
import ch.dvbern.ebegu.util.DateUtil;
import ch.dvbern.ebegu.util.MathUtil;

public class LastenausgleichRechnerMitSelbstbehalt extends AbstractLastenausgleichRechner {

	public LastenausgleichRechnerMitSelbstbehalt(@Nonnull VerfuegungService verfuegungService) {
		super(verfuegungService);
	}

	@Override
	@Nullable
	public LastenausgleichDetail createLastenausgleichDetail(@Nonnull Gemeinde gemeinde, @Nonnull Lastenausgleich lastenausgleich, @Nonnull LastenausgleichGrundlagen grundlagen) {
		Collection<VerfuegungZeitabschnitt> abschnitteProGemeindeUndJahr =
			getZeitabschnitte(gemeinde, grundlagen.getJahr());
		if (abschnitteProGemeindeUndJahr.isEmpty()) {
			return null;
		}

		// Total Belegung = Totals aller Pensum * AnteilDesMonats / 12
		BigDecimal totalBelegungInProzent = BigDecimal.ZERO;
		// Total Gutscheine: Totals aller aktuell gültigen Zeitabschnitte, die im Kalenderjahr liegen
		BigDecimal totalGutscheine = BigDecimal.ZERO;
		// Total Belegung = Totals aller Pensum ohne selbstbehalt * AnteilDesMonats / 12
		BigDecimal totalBelegungOhneSelbstbeahltInProzent = BigDecimal.ZERO;
		// Total Gutscheine: Totals aller aktuell gültigen Zeitabschnitte ohne Selbstbehalt, die im Kalenderjahr liegen
		BigDecimal totalGutscheineOhneSelbstbeahlt = BigDecimal.ZERO;
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

		return detail;
	}

	@Nonnull
	@Override
	public String logLastenausgleichRechnerType(int jahr) {
		return "Lastenausgleichrechner mit Selbstbehalt für Jahr " + jahr;
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
