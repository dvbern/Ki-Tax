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

package ch.dvbern.ebegu.rechner;

import java.math.BigDecimal;
import java.util.Map;

import javax.annotation.Nonnull;

import ch.dvbern.ebegu.entities.Einstellung;
import ch.dvbern.ebegu.entities.Gemeinde;
import ch.dvbern.ebegu.entities.Gesuchsperiode;
import ch.dvbern.ebegu.enums.EinschulungTyp;
import ch.dvbern.ebegu.enums.EinstellungKey;
import ch.dvbern.ebegu.enums.ErrorCodeEnum;
import ch.dvbern.ebegu.errors.EbeguEntityNotFoundException;

import static ch.dvbern.ebegu.enums.EinstellungKey.GEMEINDE_ZUSAETZLICHER_BABYBEITRAG_BETRAG_KITA;
import static ch.dvbern.ebegu.enums.EinstellungKey.GEMEINDE_ZUSAETZLICHER_BABYBEITRAG_BETRAG_TFO;
import static ch.dvbern.ebegu.enums.EinstellungKey.GEMEINDE_ZUSAETZLICHER_BABYBEITRAG_ENABLED;
import static ch.dvbern.ebegu.enums.EinstellungKey.GEMEINDE_ZUSAETZLICHER_GUTSCHEIN_BETRAG_KITA;
import static ch.dvbern.ebegu.enums.EinstellungKey.GEMEINDE_ZUSAETZLICHER_GUTSCHEIN_BETRAG_TFO;
import static ch.dvbern.ebegu.enums.EinstellungKey.GEMEINDE_ZUSAETZLICHER_GUTSCHEIN_BIS_UND_MIT_SCHULSTUFE_KITA;
import static ch.dvbern.ebegu.enums.EinstellungKey.GEMEINDE_ZUSAETZLICHER_GUTSCHEIN_BIS_UND_MIT_SCHULSTUFE_TFO;
import static ch.dvbern.ebegu.enums.EinstellungKey.GEMEINDE_ZUSAETZLICHER_GUTSCHEIN_ENABLED;

/**
 * Kapselung aller Parameter, welche für die BG-Berechnung aller Angebote benötigt werden.
 * Diese müssen aus den Einstellungen gelesen werden.
 */
public final class BGRechnerParameterGemeindeDTO {

	// (1) Zusaetzlicher Gutschein der Gemeinde
	private Boolean gemeindeZusaetzlicherGutscheinEnabled;
	// Betrag des zusätzlichen Beitrags zum Gutschein
	private BigDecimal gemeindeZusaetzlicherGutscheinBetragKita;
	private BigDecimal gemeindeZusaetzlicherGutscheinBetragTfo;
	// Zusaetzlichen Gutschein anbieten bis und mit
	private EinschulungTyp gemeindeZusaetzlicherGutscheinBisUndMitSchulstufeKita;
	private EinschulungTyp gemeindeZusaetzlicherGutscheinBisUndMitSchulstufeTfo;

	// (2) Zusaetzlicher Baby-Gutschein
	private Boolean gemeindeZusaetzlicherBabyGutscheinEnabled;
	// Betrag des zusätzlichen Gutscheins für Babies
	private BigDecimal gemeindeZusaetzlicherBabyGutscheinBetragKita;
	private BigDecimal gemeindeZusaetzlicherBabyGutscheinBetragTfo;



	public BGRechnerParameterGemeindeDTO(Map<EinstellungKey, Einstellung> paramMap, Gesuchsperiode gesuchsperiode, Gemeinde gemeinde) {
		// (1) Zusaetzlicher Gutschein der Gemeinde
		this.setGemeindeZusaetzlicherGutscheinEnabled(asBoolean(paramMap, GEMEINDE_ZUSAETZLICHER_GUTSCHEIN_ENABLED, gesuchsperiode, gemeinde));
		this.setGemeindeZusaetzlicherGutscheinBetragKita(asBigDecimal(paramMap, GEMEINDE_ZUSAETZLICHER_GUTSCHEIN_BETRAG_KITA, gesuchsperiode, gemeinde));
		this.setGemeindeZusaetzlicherGutscheinBetragTfo(asBigDecimal(paramMap, GEMEINDE_ZUSAETZLICHER_GUTSCHEIN_BETRAG_TFO, gesuchsperiode, gemeinde));
		this.setGemeindeZusaetzlicherGutscheinBisUndMitSchulstufeKita(asSchulstufe(paramMap, GEMEINDE_ZUSAETZLICHER_GUTSCHEIN_BIS_UND_MIT_SCHULSTUFE_KITA, gesuchsperiode, gemeinde));
		this.setGemeindeZusaetzlicherGutscheinBisUndMitSchulstufeTfo(asSchulstufe(paramMap, GEMEINDE_ZUSAETZLICHER_GUTSCHEIN_BIS_UND_MIT_SCHULSTUFE_TFO, gesuchsperiode, gemeinde));
		// (2) Zusaetzlicher Baby-Gutschein
		this.setGemeindeZusaetzlicherBabyGutscheinEnabled(asBoolean(paramMap, GEMEINDE_ZUSAETZLICHER_BABYBEITRAG_ENABLED, gesuchsperiode, gemeinde));
		this.setGemeindeZusaetzlicherBabyGutscheinBetragKita(asBigDecimal(paramMap, GEMEINDE_ZUSAETZLICHER_BABYBEITRAG_BETRAG_KITA, gesuchsperiode, gemeinde));
		this.setGemeindeZusaetzlicherBabyGutscheinBetragTfo(asBigDecimal(paramMap, GEMEINDE_ZUSAETZLICHER_BABYBEITRAG_BETRAG_TFO, gesuchsperiode, gemeinde));
	}

	public BGRechnerParameterGemeindeDTO() {

	}

	private BigDecimal asBigDecimal(
		@Nonnull Map<EinstellungKey, Einstellung> paramMap,
		@Nonnull EinstellungKey paramKey,
		@Nonnull Gesuchsperiode gesuchsperiode,
		@Nonnull Gemeinde gemeinde) {

		Einstellung param = paramMap.get(paramKey);
		if (param == null) {
			String message = "Required calculator parameter '" + paramKey + "' could not be loaded for the given Gemeinde '" + gemeinde.getName() + "', Gesuchsperiode "
				+ '\'' + gesuchsperiode + '\'';
			throw new EbeguEntityNotFoundException("loadCalculatorParameters", message, ErrorCodeEnum.ERROR_PARAMETER_NOT_FOUND, paramKey);
		}
		return param.getValueAsBigDecimal();
	}

	private Boolean asBoolean(
		@Nonnull Map<EinstellungKey, Einstellung> paramMap,
		@Nonnull EinstellungKey paramKey,
		@Nonnull Gesuchsperiode gesuchsperiode,
		@Nonnull Gemeinde gemeinde) {

		Einstellung param = paramMap.get(paramKey);
		if (param == null) {
			String message = "Required calculator parameter '" + paramKey + "' could not be loaded for the given Gemeinde '" + gemeinde.getName() + "', Gesuchsperiode "
				+ '\'' + gesuchsperiode + '\'';
			throw new EbeguEntityNotFoundException("loadCalculatorParameters", message, ErrorCodeEnum.ERROR_PARAMETER_NOT_FOUND, paramKey);
		}
		return param.getValueAsBoolean();
	}

	private EinschulungTyp asSchulstufe(
		@Nonnull Map<EinstellungKey, Einstellung> paramMap,
		@Nonnull EinstellungKey paramKey,
		@Nonnull Gesuchsperiode gesuchsperiode,
		@Nonnull Gemeinde gemeinde) {

		Einstellung param = paramMap.get(paramKey);
		if (param == null) {
			String message = "Required calculator parameter '" + paramKey + "' could not be loaded for the given Gemeinde '" + gemeinde.getName() + "', Gesuchsperiode "
				+ '\'' + gesuchsperiode + '\'';
			throw new EbeguEntityNotFoundException("loadCalculatorParameters", message, ErrorCodeEnum.ERROR_PARAMETER_NOT_FOUND, paramKey);
		}
		return EinschulungTyp.valueOf(param.getValue());
	}

	public Boolean getGemeindeZusaetzlicherGutscheinEnabled() {
		return gemeindeZusaetzlicherGutscheinEnabled;
	}

	public void setGemeindeZusaetzlicherGutscheinEnabled(Boolean gemeindeZusaetzlicherGutscheinEnabled) {
		this.gemeindeZusaetzlicherGutscheinEnabled = gemeindeZusaetzlicherGutscheinEnabled;
	}

	public BigDecimal getGemeindeZusaetzlicherGutscheinBetragKita() {
		return gemeindeZusaetzlicherGutscheinBetragKita;
	}

	public void setGemeindeZusaetzlicherGutscheinBetragKita(BigDecimal gemeindeZusaetzlicherGutscheinBetragKita) {
		this.gemeindeZusaetzlicherGutscheinBetragKita = gemeindeZusaetzlicherGutscheinBetragKita;
	}

	public BigDecimal getGemeindeZusaetzlicherGutscheinBetragTfo() {
		return gemeindeZusaetzlicherGutscheinBetragTfo;
	}

	public void setGemeindeZusaetzlicherGutscheinBetragTfo(BigDecimal gemeindeZusaetzlicherGutscheinBetragTfo) {
		this.gemeindeZusaetzlicherGutscheinBetragTfo = gemeindeZusaetzlicherGutscheinBetragTfo;
	}

	public EinschulungTyp getGemeindeZusaetzlicherGutscheinBisUndMitSchulstufeKita() {
		return gemeindeZusaetzlicherGutscheinBisUndMitSchulstufeKita;
	}

	public void setGemeindeZusaetzlicherGutscheinBisUndMitSchulstufeKita(EinschulungTyp gemeindeZusaetzlicherGutscheinBisUndMitSchulstufeKita) {
		this.gemeindeZusaetzlicherGutscheinBisUndMitSchulstufeKita = gemeindeZusaetzlicherGutscheinBisUndMitSchulstufeKita;
	}

	public EinschulungTyp getGemeindeZusaetzlicherGutscheinBisUndMitSchulstufeTfo() {
		return gemeindeZusaetzlicherGutscheinBisUndMitSchulstufeTfo;
	}

	public void setGemeindeZusaetzlicherGutscheinBisUndMitSchulstufeTfo(EinschulungTyp gemeindeZusaetzlicherGutscheinBisUndMitSchulstufeTfo) {
		this.gemeindeZusaetzlicherGutscheinBisUndMitSchulstufeTfo = gemeindeZusaetzlicherGutscheinBisUndMitSchulstufeTfo;
	}

	public Boolean getGemeindeZusaetzlicherBabyGutscheinEnabled() {
		return gemeindeZusaetzlicherBabyGutscheinEnabled;
	}

	public void setGemeindeZusaetzlicherBabyGutscheinEnabled(Boolean gemeindeZusaetzlicherBabyGutscheinEnabled) {
		this.gemeindeZusaetzlicherBabyGutscheinEnabled = gemeindeZusaetzlicherBabyGutscheinEnabled;
	}

	public BigDecimal getGemeindeZusaetzlicherBabyGutscheinBetragKita() {
		return gemeindeZusaetzlicherBabyGutscheinBetragKita;
	}

	public void setGemeindeZusaetzlicherBabyGutscheinBetragKita(BigDecimal gemeindeZusaetzlicherBabyGutscheinBetragKita) {
		this.gemeindeZusaetzlicherBabyGutscheinBetragKita = gemeindeZusaetzlicherBabyGutscheinBetragKita;
	}

	public BigDecimal getGemeindeZusaetzlicherBabyGutscheinBetragTfo() {
		return gemeindeZusaetzlicherBabyGutscheinBetragTfo;
	}

	public void setGemeindeZusaetzlicherBabyGutscheinBetragTfo(BigDecimal gemeindeZusaetzlicherBabyGutscheinBetragTfo) {
		this.gemeindeZusaetzlicherBabyGutscheinBetragTfo = gemeindeZusaetzlicherBabyGutscheinBetragTfo;
	}
}
