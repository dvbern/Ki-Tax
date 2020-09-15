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

package ch.dvbern.ebegu.rules;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import javax.annotation.Nonnull;

import ch.dvbern.ebegu.entities.AbstractPlatz;
import ch.dvbern.ebegu.entities.AnmeldungTagesschule;
import ch.dvbern.ebegu.entities.BelegungTagesschuleModul;
import ch.dvbern.ebegu.entities.ModulTagesschule;
import ch.dvbern.ebegu.entities.VerfuegungZeitabschnitt;
import ch.dvbern.ebegu.enums.BelegungTagesschuleModulIntervall;
import ch.dvbern.ebegu.enums.BetreuungsangebotTyp;
import ch.dvbern.ebegu.types.DateRange;
import ch.dvbern.ebegu.util.MathUtil;
import com.google.common.collect.ImmutableList;

import static ch.dvbern.ebegu.enums.BetreuungsangebotTyp.TAGESSCHULE;

/**
 * Erstellt aus den gewählten Modulen der Tagesschule einen Zeitabschnitt
 */
public class TagesschuleBetreuungszeitAbschnittRule extends AbstractAbschnittRule {

	public TagesschuleBetreuungszeitAbschnittRule(@Nonnull DateRange validityPeriod, @Nonnull Locale locale) {
		super(RuleKey.BETREUUNGSPENSUM, RuleType.GRUNDREGEL_DATA, RuleValidity.ASIV, validityPeriod, locale);
	}

	@Override
	protected List<BetreuungsangebotTyp> getAnwendbareAngebote() {
		return ImmutableList.of(TAGESSCHULE);
	}

	@Nonnull
	@Override
	protected List<VerfuegungZeitabschnitt> createVerfuegungsZeitabschnitte(@Nonnull AbstractPlatz platz) {
		List<VerfuegungZeitabschnitt> tagesschuleAbschnitte = new ArrayList<>();
		tagesschuleAbschnitte.add(toVerfuegungZeitabschnitt((AnmeldungTagesschule) platz));
		return tagesschuleAbschnitte;
	}

	@SuppressWarnings("PMD.NcssMethodCount")
	@Nonnull
	private VerfuegungZeitabschnitt toVerfuegungZeitabschnitt(@Nonnull AnmeldungTagesschule anmeldungTagesschule){
		// Tageschulanmeldungen gelten immer fuer die ganze Gesuchsperiode
		VerfuegungZeitabschnitt zeitabschnitt = createZeitabschnittWithinValidityPeriodOfRule(anmeldungTagesschule.extractGesuchsperiode().getGueltigkeit());
		Objects.requireNonNull(anmeldungTagesschule.getBelegungTagesschule());

		long dauerProWocheInMinutenMitBetreuung = 0;
		long dauerProWocheInMinutenOhneBetreuung = 0;
		BigDecimal verpflegKostenProWocheMitBetreuung = BigDecimal.ZERO;
		BigDecimal verpflegKostenProWocheOhneBetreuung = BigDecimal.ZERO;
		Map<BigDecimal, Integer> verpflegungenProModulMitBetreuung = new HashMap<>();
		Map<BigDecimal, Integer> verpflegungenProModulOhneBetreuung = new HashMap<>();
		Map<BigDecimal, Integer> verpflegungenProModulMitBetreuungZweiWochen = new HashMap<>();
		Map<BigDecimal, Integer> verpflegungenProModulOhneBetreuungZweiWochen = new HashMap<>();

		for (BelegungTagesschuleModul belegungTagesschuleModul : anmeldungTagesschule.getBelegungTagesschule().getBelegungTagesschuleModule()) {
			ModulTagesschule modulTagesschule = belegungTagesschuleModul.getModulTagesschule();
			LocalTime von = modulTagesschule.getModulTagesschuleGroup().getZeitVon();
			LocalTime bis = modulTagesschule.getModulTagesschuleGroup().getZeitBis();
			long dauer = von.until(bis, ChronoUnit.MINUTES);
			BigDecimal verpflegungskosten = modulTagesschule.getModulTagesschuleGroup().getVerpflegungskosten();
			double scale = 1;
			if (modulTagesschule.getModulTagesschuleGroup().isWirdPaedagogischBetreut()){

				if (belegungTagesschuleModul.getIntervall() == BelegungTagesschuleModulIntervall.ALLE_ZWEI_WOCHEN) {
					scale = 0.5;
					if (verpflegungskosten != null) {
						Integer count = verpflegungenProModulMitBetreuungZweiWochen.getOrDefault(verpflegungskosten, 0);
						verpflegungenProModulMitBetreuungZweiWochen.put(verpflegungskosten, count + 1);
					}
				} else {
					if (verpflegungskosten != null) {
						Integer count = verpflegungenProModulMitBetreuung.getOrDefault(verpflegungskosten, 0);
						verpflegungenProModulMitBetreuung.put(verpflegungskosten, count + 1);
					}
				}

				dauerProWocheInMinutenMitBetreuung += dauer * scale;
				if (verpflegungskosten != null) {
					BigDecimal scaledVerpflegungskosten = MathUtil.DEFAULT.multiply(verpflegungskosten, BigDecimal.valueOf(scale));
					verpflegKostenProWocheMitBetreuung =
						MathUtil.DEFAULT.addNullSafe(verpflegKostenProWocheMitBetreuung, scaledVerpflegungskosten);
				}

			} else {
				if (belegungTagesschuleModul.getIntervall() == BelegungTagesschuleModulIntervall.ALLE_ZWEI_WOCHEN) {
					scale = 0.5;
					if (verpflegungskosten != null) {
						Integer count = verpflegungenProModulOhneBetreuungZweiWochen.getOrDefault(verpflegungskosten, 0);
						verpflegungenProModulOhneBetreuungZweiWochen.put(verpflegungskosten, count + 1);
					}
				} else {
					if (verpflegungskosten != null) {
						Integer count = verpflegungenProModulOhneBetreuung.getOrDefault(verpflegungskosten, 0);
						verpflegungenProModulOhneBetreuung.put(verpflegungskosten, count + 1);
					}
				}

				dauerProWocheInMinutenOhneBetreuung += dauer * scale;
				if (verpflegungskosten != null) {
					BigDecimal scaledVerpflegungskosten = MathUtil.DEFAULT.multiply(verpflegungskosten, BigDecimal.valueOf(scale));
					verpflegKostenProWocheOhneBetreuung =
						MathUtil.DEFAULT.addNullSafe(verpflegKostenProWocheOhneBetreuung, scaledVerpflegungskosten);
				}
			}
		}

		if (dauerProWocheInMinutenMitBetreuung > 0) {
			zeitabschnitt.setTsBetreuungszeitProWocheMitBetreuungForAsivAndGemeinde(Long.valueOf(dauerProWocheInMinutenMitBetreuung).intValue());
			zeitabschnitt.setTsVerpflegungskostenMitBetreuungForAsivAndGemeinde(verpflegKostenProWocheMitBetreuung);
			zeitabschnitt.setVerpflegungskostenUndMahlzeitenMitBetreuungForAsivAndGemeinde(verpflegungenProModulMitBetreuung);
			zeitabschnitt.setVerpflegungskostenUndMahlzeitenMitBetreuungZweiWochenForAsivAndGemeinde(verpflegungenProModulMitBetreuungZweiWochen);
		}
		if (dauerProWocheInMinutenOhneBetreuung > 0) {
			zeitabschnitt.setTsBetreuungszeitProWocheOhneBetreuungForAsivAndGemeinde(Long.valueOf(dauerProWocheInMinutenOhneBetreuung).intValue());
			zeitabschnitt.setTsVerpflegungskostenOhneBetreuungForAsivAndGemeinde(verpflegKostenProWocheOhneBetreuung);
			zeitabschnitt.setVerpflegungskostenUndMahlzeitenOhneBetreuungForAsivAndGemeinde(verpflegungenProModulOhneBetreuung);
			zeitabschnitt.setVerpflegungskostenUndMahlzeitenOhneBetreuungZweiWochenForAsivAndGemeinde(verpflegungenProModulOhneBetreuungZweiWochen);
		}

		return zeitabschnitt;
	}
}
