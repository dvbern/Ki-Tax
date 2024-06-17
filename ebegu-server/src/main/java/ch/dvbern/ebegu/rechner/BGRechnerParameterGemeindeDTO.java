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

import ch.dvbern.ebegu.entities.Einstellung;
import ch.dvbern.ebegu.entities.Gemeinde;
import ch.dvbern.ebegu.entities.Gesuchsperiode;
import ch.dvbern.ebegu.enums.EinschulungTyp;
import ch.dvbern.ebegu.enums.EinstellungKey;
import ch.dvbern.ebegu.enums.ErrorCodeEnum;
import ch.dvbern.ebegu.enums.gemeindekonfiguration.GemeindeZusaetzlicherGutscheinTyp;
import ch.dvbern.ebegu.errors.EbeguEntityNotFoundException;

import javax.annotation.Nonnull;
import java.math.BigDecimal;
import java.util.Map;

import static ch.dvbern.ebegu.enums.EinstellungKey.GEMEINDE_PAUSCHALBETRAG_HOHE_EINKOMMENSKLASSEN_AKTIVIERT;
import static ch.dvbern.ebegu.enums.EinstellungKey.GEMEINDE_PAUSCHALBETRAG_HOHE_EINKOMMENSKLASSEN_BETRAG_KITA;
import static ch.dvbern.ebegu.enums.EinstellungKey.GEMEINDE_PAUSCHALBETRAG_HOHE_EINKOMMENSKLASSEN_BETRAG_TFO;
import static ch.dvbern.ebegu.enums.EinstellungKey.GEMEINDE_PAUSCHALBETRAG_HOHE_EINKOMMENSKLASSEN_BETRAG_TFO_AB_PRIMARSCHULE;
import static ch.dvbern.ebegu.enums.EinstellungKey.GEMEINDE_PAUSCHALBETRAG_HOHE_EINKOMMENSKLASSEN_MAX_MASSGEBENDEN_EINKOMMEN_FUER_BERECHNUNG;
import static ch.dvbern.ebegu.enums.EinstellungKey.GEMEINDE_ZUSAETZLICHER_BABYBEITRAG_BETRAG_KITA;
import static ch.dvbern.ebegu.enums.EinstellungKey.GEMEINDE_ZUSAETZLICHER_BABYBEITRAG_BETRAG_TFO;
import static ch.dvbern.ebegu.enums.EinstellungKey.GEMEINDE_ZUSAETZLICHER_BABYBEITRAG_ENABLED;
import static ch.dvbern.ebegu.enums.EinstellungKey.GEMEINDE_ZUSAETZLICHER_GUTSCHEIN_BETRAG_KITA;
import static ch.dvbern.ebegu.enums.EinstellungKey.GEMEINDE_ZUSAETZLICHER_GUTSCHEIN_BETRAG_TFO;
import static ch.dvbern.ebegu.enums.EinstellungKey.GEMEINDE_ZUSAETZLICHER_GUTSCHEIN_BIS_UND_MIT_SCHULSTUFE_KITA;
import static ch.dvbern.ebegu.enums.EinstellungKey.GEMEINDE_ZUSAETZLICHER_GUTSCHEIN_BIS_UND_MIT_SCHULSTUFE_TFO;
import static ch.dvbern.ebegu.enums.EinstellungKey.GEMEINDE_ZUSAETZLICHER_GUTSCHEIN_ENABLED;
import static ch.dvbern.ebegu.enums.EinstellungKey.GEMEINDE_ZUSAETZLICHER_GUTSCHEIN_LINEAR_KITA_MAX;
import static ch.dvbern.ebegu.enums.EinstellungKey.GEMEINDE_ZUSAETZLICHER_GUTSCHEIN_LINEAR_TFO_MAX;
import static ch.dvbern.ebegu.enums.EinstellungKey.GEMEINDE_ZUSAETZLICHER_GUTSCHEIN_MAX_MASSGEBENDES_EINKOMMEN;
import static ch.dvbern.ebegu.enums.EinstellungKey.GEMEINDE_ZUSAETZLICHER_GUTSCHEIN_MIN_MASSGEBENDES_EINKOMMEN;
import static ch.dvbern.ebegu.enums.EinstellungKey.GEMEINDE_ZUSAETZLICHER_GUTSCHEIN_TYP;

/**
 * Kapselung aller Parameter, welche für die BG-Berechnung aller Angebote benötigt werden.
 * Diese müssen aus den Einstellungen gelesen werden.
 */
public final class BGRechnerParameterGemeindeDTO {

	// (1) Zusaetzlicher Gutschein der Gemeinde
	private Boolean gemeindeZusaetzlicherGutscheinEnabled;
	private GemeindeZusaetzlicherGutscheinTyp gemeindeZusaetzlicherGutscheinTyp;
	// Betrag des zusätzlichen Beitrags zum Gutschein
	private BigDecimal gemeindeZusaetzlicherGutscheinBetragKita;
	private BigDecimal gemeindeZusaetzlicherGutscheinBetragTfo;
	private BigDecimal gemeindeZusaetzlicherGutscheinLinearKitaMax;
	private BigDecimal gemeindeZusaetzlicherGutscheinMaxMassgebendesEinkommen;
	private BigDecimal gemeindeZusaetzlicherGutscheinMinMassgebendesEinkommen;
	private EinschulungTyp gemeindeZusaetzlicherGutscheinBisUndMitSchulstufeKita;
	// Zusaetzlichen Gutschein anbieten bis und mit
	private BigDecimal gemeindeZusaetzlicherGutscheinLinearTfoMax;
	private EinschulungTyp gemeindeZusaetzlicherGutscheinBisUndMitSchulstufeTfo;

	// (2) Zusaetzlicher Baby-Gutschein
	private Boolean gemeindeZusaetzlicherBabyGutscheinEnabled;
	// Betrag des zusätzlichen Gutscheins für Babies
	private BigDecimal gemeindeZusaetzlicherBabyGutscheinBetragKita;
	private BigDecimal gemeindeZusaetzlicherBabyGutscheinBetragTfo;

	// (3) Minimal Betrag-Gutschein
	private Boolean gemeindePauschalbetragEnabled = false;
	private BigDecimal gemeindePauschalbetragKita;
	private BigDecimal gemeindePauschalbetragTfo;
	private BigDecimal gemeindePauschalbetragTfoPrimarschule;
	private BigDecimal gemeindePauschalbetragMaxMassgebendenEinkommenFuerBerechnung;

	public BGRechnerParameterGemeindeDTO(Map<EinstellungKey, Einstellung> paramMap, Gesuchsperiode gesuchsperiode, Gemeinde gemeinde) {
		// (1) Zusaetzlicher Gutschein der Gemeinde
		this.setGemeindeZusaetzlicherGutscheinEnabled(asBoolean(paramMap, GEMEINDE_ZUSAETZLICHER_GUTSCHEIN_ENABLED, gesuchsperiode, gemeinde));
		this.gemeindeZusaetzlicherGutscheinTyp = getGemeindeZusaetzlicherGutscheinTyp(paramMap, gesuchsperiode, gemeinde);
		this.gemeindeZusaetzlicherGutscheinLinearKitaMax = asBigDecimal(paramMap, GEMEINDE_ZUSAETZLICHER_GUTSCHEIN_LINEAR_KITA_MAX, gesuchsperiode, gemeinde);
		this.gemeindeZusaetzlicherGutscheinLinearTfoMax = asBigDecimal(paramMap, GEMEINDE_ZUSAETZLICHER_GUTSCHEIN_LINEAR_TFO_MAX, gesuchsperiode, gemeinde);
		this.gemeindeZusaetzlicherGutscheinMinMassgebendesEinkommen = asBigDecimal(paramMap, GEMEINDE_ZUSAETZLICHER_GUTSCHEIN_MIN_MASSGEBENDES_EINKOMMEN, gesuchsperiode, gemeinde);
		this.gemeindeZusaetzlicherGutscheinMaxMassgebendesEinkommen = asBigDecimal(paramMap, GEMEINDE_ZUSAETZLICHER_GUTSCHEIN_MAX_MASSGEBENDES_EINKOMMEN, gesuchsperiode, gemeinde);
		this.setGemeindeZusaetzlicherGutscheinBetragKita(asBigDecimal(paramMap, GEMEINDE_ZUSAETZLICHER_GUTSCHEIN_BETRAG_KITA, gesuchsperiode, gemeinde));
		this.setGemeindeZusaetzlicherGutscheinBetragTfo(asBigDecimal(paramMap, GEMEINDE_ZUSAETZLICHER_GUTSCHEIN_BETRAG_TFO, gesuchsperiode, gemeinde));
		this.setGemeindeZusaetzlicherGutscheinBisUndMitSchulstufeKita(asSchulstufe(paramMap, GEMEINDE_ZUSAETZLICHER_GUTSCHEIN_BIS_UND_MIT_SCHULSTUFE_KITA, gesuchsperiode, gemeinde));
		this.setGemeindeZusaetzlicherGutscheinBisUndMitSchulstufeTfo(asSchulstufe(paramMap, GEMEINDE_ZUSAETZLICHER_GUTSCHEIN_BIS_UND_MIT_SCHULSTUFE_TFO, gesuchsperiode, gemeinde));
		// (2) Zusaetzlicher Baby-Gutschein
		this.setGemeindeZusaetzlicherBabyGutscheinEnabled(asBoolean(paramMap, GEMEINDE_ZUSAETZLICHER_BABYBEITRAG_ENABLED, gesuchsperiode, gemeinde));
		this.setGemeindeZusaetzlicherBabyGutscheinBetragKita(asBigDecimal(paramMap, GEMEINDE_ZUSAETZLICHER_BABYBEITRAG_BETRAG_KITA, gesuchsperiode, gemeinde));
		this.setGemeindeZusaetzlicherBabyGutscheinBetragTfo(asBigDecimal(paramMap, GEMEINDE_ZUSAETZLICHER_BABYBEITRAG_BETRAG_TFO, gesuchsperiode, gemeinde));
		// (3) Minimal Betrag-Gutschein
		this.setGemeindePauschalbetragEnabled(asBoolean(paramMap, GEMEINDE_PAUSCHALBETRAG_HOHE_EINKOMMENSKLASSEN_AKTIVIERT, gesuchsperiode, gemeinde));
		this.setGemeindePauschalbetragKita(asBigDecimal(paramMap, GEMEINDE_PAUSCHALBETRAG_HOHE_EINKOMMENSKLASSEN_BETRAG_KITA, gesuchsperiode, gemeinde));
		this.setGemeindePauschalbetragTfo(asBigDecimal(paramMap, GEMEINDE_PAUSCHALBETRAG_HOHE_EINKOMMENSKLASSEN_BETRAG_TFO, gesuchsperiode, gemeinde));
		this.setGemeindePauschalbetragTfoPrimarschule(asBigDecimal(paramMap, GEMEINDE_PAUSCHALBETRAG_HOHE_EINKOMMENSKLASSEN_BETRAG_TFO_AB_PRIMARSCHULE, gesuchsperiode, gemeinde));

		this.setGemeindePauschalbetragMaxMassgebendenEinkommenFuerBerechnung(asBigDecimal(paramMap,
			GEMEINDE_PAUSCHALBETRAG_HOHE_EINKOMMENSKLASSEN_MAX_MASSGEBENDEN_EINKOMMEN_FUER_BERECHNUNG, gesuchsperiode, gemeinde));
	}

	private GemeindeZusaetzlicherGutscheinTyp getGemeindeZusaetzlicherGutscheinTyp(
		Map<EinstellungKey, Einstellung> paramMap,
		Gesuchsperiode gesuchsperiode,
		Gemeinde gemeinde) {
		Einstellung param = paramMap.get(GEMEINDE_ZUSAETZLICHER_GUTSCHEIN_TYP);
		if (param == null) {
			throwParamNotFoundException(GEMEINDE_ZUSAETZLICHER_GUTSCHEIN_TYP, gesuchsperiode, gemeinde);
		}
		return GemeindeZusaetzlicherGutscheinTyp.valueOf(param.getValue());
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
			throwParamNotFoundException(paramKey, gesuchsperiode, gemeinde);
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
			throwParamNotFoundException(paramKey, gesuchsperiode, gemeinde);
		}
		return param.getValueAsBoolean();
	}

	private static void throwParamNotFoundException(
		@Nonnull EinstellungKey paramKey,
		@Nonnull Gesuchsperiode gesuchsperiode,
		@Nonnull Gemeinde gemeinde) {
		String message = "Required calculator parameter '" + paramKey
			+ "' could not be loaded for the given Gemeinde '" + gemeinde.getName() + "', Gesuchsperiode "
			+ '\'' + gesuchsperiode
			+ '\'';
		throw new EbeguEntityNotFoundException("loadCalculatorParameters", message, ErrorCodeEnum.ERROR_PARAMETER_NOT_FOUND,
			paramKey);
	}

	private EinschulungTyp asSchulstufe(
		@Nonnull Map<EinstellungKey, Einstellung> paramMap,
		@Nonnull EinstellungKey paramKey,
		@Nonnull Gesuchsperiode gesuchsperiode,
		@Nonnull Gemeinde gemeinde) {

		Einstellung param = paramMap.get(paramKey);
		if (param == null) {
			throwParamNotFoundException(paramKey, gesuchsperiode, gemeinde);
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

	public Boolean getGemeindePauschalbetragEnabled() {
		return gemeindePauschalbetragEnabled;
	}

	public void setGemeindePauschalbetragEnabled(Boolean gemeindePauschalbetragEnabled) {
		this.gemeindePauschalbetragEnabled = gemeindePauschalbetragEnabled;
	}

	public BigDecimal getGemeindePauschalbetragKita() {
		return gemeindePauschalbetragKita;
	}

	public void setGemeindePauschalbetragKita(BigDecimal gemeindePauschalbetragKita) {
		this.gemeindePauschalbetragKita = gemeindePauschalbetragKita;
	}

	public BigDecimal getGemeindePauschalbetragTfo() {
		return gemeindePauschalbetragTfo;
	}

	public void setGemeindePauschalbetragTfo(BigDecimal gemeindePauschalbetragTfo) {
		this.gemeindePauschalbetragTfo = gemeindePauschalbetragTfo;
	}

	public BigDecimal getGemeindePauschalbetragMaxMassgebendenEinkommenFuerBerechnung() {
		return gemeindePauschalbetragMaxMassgebendenEinkommenFuerBerechnung;
	}

	public void setGemeindePauschalbetragMaxMassgebendenEinkommenFuerBerechnung(BigDecimal gemeindePauschalbetragMaxMassgebendenEinkommenFuerBerechnung) {
		this.gemeindePauschalbetragMaxMassgebendenEinkommenFuerBerechnung =
			gemeindePauschalbetragMaxMassgebendenEinkommenFuerBerechnung;
	}

	public BigDecimal getGemeindePauschalbetragTfoPrimarschule() {
		return gemeindePauschalbetragTfoPrimarschule;
	}

	public void setGemeindePauschalbetragTfoPrimarschule(BigDecimal gemeindePauschalbetragTfoPrimarschule) {
		this.gemeindePauschalbetragTfoPrimarschule = gemeindePauschalbetragTfoPrimarschule;
	}

	public GemeindeZusaetzlicherGutscheinTyp getGemeindeZusaetzlicherGutscheinTyp() {
		return gemeindeZusaetzlicherGutscheinTyp;
	}

	public void setGemeindeZusaetzlicherGutscheinTyp(GemeindeZusaetzlicherGutscheinTyp gemeindeZusaetzlicherGutscheinTyp) {
		this.gemeindeZusaetzlicherGutscheinTyp = gemeindeZusaetzlicherGutscheinTyp;
	}

	public BigDecimal getGemeindeZusaetzlicherGutscheinLinearTfoMax() {
		return gemeindeZusaetzlicherGutscheinLinearTfoMax;
	}

	public BigDecimal getGemeindeZusaetzlicherGutscheinLinearKitaMax() {
		return gemeindeZusaetzlicherGutscheinLinearKitaMax;
	}

	public BigDecimal getGemeindeZusaetzlicherGutscheinMinMassgebendesEinkommen() {
		return gemeindeZusaetzlicherGutscheinMinMassgebendesEinkommen;
	}

	public void setGemeindeZusaetzlicherGutscheinLinearKitaMax(BigDecimal gemeindeZusaetzlicherGutscheinLinearKitaMax) {
		this.gemeindeZusaetzlicherGutscheinLinearKitaMax = gemeindeZusaetzlicherGutscheinLinearKitaMax;
	}

	public void setGemeindeZusaetzlicherGutscheinLinearTfoMax(BigDecimal gemeindeZusaetzlicherGutscheinLinearTfoMax) {
		this.gemeindeZusaetzlicherGutscheinLinearTfoMax = gemeindeZusaetzlicherGutscheinLinearTfoMax;
	}

	public void setGemeindeZusaetzlicherGutscheinMinMassgebendesEinkommen(BigDecimal gemeindeZusaetzlicherGutscheinMinMassgebendesEinkommen) {
		this.gemeindeZusaetzlicherGutscheinMinMassgebendesEinkommen =
				gemeindeZusaetzlicherGutscheinMinMassgebendesEinkommen;
	}

	public void setGemeindeZusaetzlicherGutscheinMaxMassgebendesEinkommen(BigDecimal gemeindeZusaetzlicherGutscheinMaxMassgebendesEinkommen) {
		this.gemeindeZusaetzlicherGutscheinMaxMassgebendesEinkommen =
				gemeindeZusaetzlicherGutscheinMaxMassgebendesEinkommen;
	}

	public BigDecimal getGemeindeZusaetzlicherGutscheinMaxMassgebendesEinkommen() {
		return gemeindeZusaetzlicherGutscheinMaxMassgebendesEinkommen;
	}

}
