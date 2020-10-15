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

package ch.dvbern.ebegu.rechner;

import java.math.BigDecimal;
import java.util.Map;

import javax.annotation.Nonnull;

import ch.dvbern.ebegu.entities.Einstellung;
import ch.dvbern.ebegu.entities.Gemeinde;
import ch.dvbern.ebegu.entities.Gesuchsperiode;
import ch.dvbern.ebegu.enums.EinstellungKey;
import ch.dvbern.ebegu.enums.ErrorCodeEnum;
import ch.dvbern.ebegu.errors.EbeguEntityNotFoundException;

import static ch.dvbern.ebegu.enums.EinstellungKey.MAX_MASSGEBENDES_EINKOMMEN;
import static ch.dvbern.ebegu.enums.EinstellungKey.MAX_TARIF_MIT_PAEDAGOGISCHER_BETREUUNG;
import static ch.dvbern.ebegu.enums.EinstellungKey.MAX_TARIF_OHNE_PAEDAGOGISCHER_BETREUUNG;
import static ch.dvbern.ebegu.enums.EinstellungKey.MAX_VERGUENSTIGUNG_SCHULE_PRO_STD;
import static ch.dvbern.ebegu.enums.EinstellungKey.MAX_VERGUENSTIGUNG_SCHULE_PRO_TG;
import static ch.dvbern.ebegu.enums.EinstellungKey.MAX_VERGUENSTIGUNG_VORSCHULE_BABY_PRO_STD;
import static ch.dvbern.ebegu.enums.EinstellungKey.MAX_VERGUENSTIGUNG_VORSCHULE_BABY_PRO_TG;
import static ch.dvbern.ebegu.enums.EinstellungKey.MAX_VERGUENSTIGUNG_VORSCHULE_KIND_PRO_STD;
import static ch.dvbern.ebegu.enums.EinstellungKey.MAX_VERGUENSTIGUNG_VORSCHULE_KIND_PRO_TG;
import static ch.dvbern.ebegu.enums.EinstellungKey.MIN_MASSGEBENDES_EINKOMMEN;
import static ch.dvbern.ebegu.enums.EinstellungKey.MIN_TARIF;
import static ch.dvbern.ebegu.enums.EinstellungKey.MIN_VERGUENSTIGUNG_PRO_STD;
import static ch.dvbern.ebegu.enums.EinstellungKey.MIN_VERGUENSTIGUNG_PRO_TG;
import static ch.dvbern.ebegu.enums.EinstellungKey.OEFFNUNGSSTUNDEN_TFO;
import static ch.dvbern.ebegu.enums.EinstellungKey.OEFFNUNGSTAGE_KITA;
import static ch.dvbern.ebegu.enums.EinstellungKey.OEFFNUNGSTAGE_TFO;
import static ch.dvbern.ebegu.enums.EinstellungKey.ZUSCHLAG_BEHINDERUNG_PRO_STD;
import static ch.dvbern.ebegu.enums.EinstellungKey.ZUSCHLAG_BEHINDERUNG_PRO_TG;

/**
 * Kapselung aller Parameter, welche für die BG-Berechnung aller Angebote benötigt werden.
 * Diese müssen aus den Einstellungen gelesen werden.
 */
public final class BGRechnerParameterDTO {

	private BigDecimal maxVerguenstigungVorschuleBabyProTg;
	private BigDecimal maxVerguenstigungVorschuleKindProTg;
	private BigDecimal maxVerguenstigungSchuleKindProTg;

	private BigDecimal maxVerguenstigungVorschuleBabyProStd;
	private BigDecimal maxVerguenstigungVorschuleKindProStd;
	private BigDecimal maxVerguenstigungSchuleKindProStd;

	private BigDecimal maxMassgebendesEinkommen;
	private BigDecimal minMassgebendesEinkommen;

	private BigDecimal oeffnungstageKita;
	private BigDecimal oeffnungstageTFO;
	private BigDecimal oeffnungsstundenTFO;

	private BigDecimal zuschlagBehinderungProTg;
	private BigDecimal zuschlagBehinderungProStd;

	private BigDecimal minVerguenstigungProTg;
	private BigDecimal minVerguenstigungProStd;

	private BigDecimal maxTarifTagesschuleMitPaedagogischerBetreuung;
	private BigDecimal maxTarifTagesschuleOhnePaedagogischerBetreuung;
	private BigDecimal minTarifTagesschule;

	private Boolean mahlzeitenverguenstigungEnabled;

	private BGRechnerParameterGemeindeDTO gemeindeParameter = new BGRechnerParameterGemeindeDTO();


	public BGRechnerParameterDTO(Map<EinstellungKey, Einstellung> paramMap, Gesuchsperiode gesuchsperiode, Gemeinde gemeinde) {
		this.setMaxVerguenstigungVorschuleBabyProTg(asBigDecimal(paramMap, MAX_VERGUENSTIGUNG_VORSCHULE_BABY_PRO_TG, gesuchsperiode, gemeinde));
		this.setMaxVerguenstigungVorschuleKindProTg(asBigDecimal(paramMap, MAX_VERGUENSTIGUNG_VORSCHULE_KIND_PRO_TG, gesuchsperiode, gemeinde));
		this.setMaxVerguenstigungSchuleKindProTg(asBigDecimal(paramMap, MAX_VERGUENSTIGUNG_SCHULE_PRO_TG, gesuchsperiode, gemeinde));
		this.setMaxVerguenstigungVorschuleBabyProStd(asBigDecimal(paramMap, MAX_VERGUENSTIGUNG_VORSCHULE_BABY_PRO_STD, gesuchsperiode, gemeinde));
		this.setMaxVerguenstigungVorschuleKindProStd(asBigDecimal(paramMap, MAX_VERGUENSTIGUNG_VORSCHULE_KIND_PRO_STD, gesuchsperiode, gemeinde));
		this.setMaxVerguenstigungSchuleKindProStd(asBigDecimal(paramMap, MAX_VERGUENSTIGUNG_SCHULE_PRO_STD, gesuchsperiode, gemeinde));
		this.setMaxMassgebendesEinkommen(asBigDecimal(paramMap, MAX_MASSGEBENDES_EINKOMMEN, gesuchsperiode, gemeinde));
		this.setMinMassgebendesEinkommen(asBigDecimal(paramMap, MIN_MASSGEBENDES_EINKOMMEN, gesuchsperiode, gemeinde));
		this.setOeffnungstageKita(asBigDecimal(paramMap, OEFFNUNGSTAGE_KITA, gesuchsperiode, gemeinde));
		this.setOeffnungstageTFO(asBigDecimal(paramMap, OEFFNUNGSTAGE_TFO, gesuchsperiode, gemeinde));
		this.setOeffnungsstundenTFO(asBigDecimal(paramMap, OEFFNUNGSSTUNDEN_TFO, gesuchsperiode, gemeinde));
		this.setZuschlagBehinderungProTg(asBigDecimal(paramMap, ZUSCHLAG_BEHINDERUNG_PRO_TG, gesuchsperiode, gemeinde));
		this.setZuschlagBehinderungProStd(asBigDecimal(paramMap, ZUSCHLAG_BEHINDERUNG_PRO_STD, gesuchsperiode, gemeinde));
		this.setMinVerguenstigungProTg(asBigDecimal(paramMap, MIN_VERGUENSTIGUNG_PRO_TG, gesuchsperiode, gemeinde));
		this.setMinVerguenstigungProStd(asBigDecimal(paramMap, MIN_VERGUENSTIGUNG_PRO_STD, gesuchsperiode, gemeinde));
		this.setMaxTarifTagesschuleMitPaedagogischerBetreuung(asBigDecimal(paramMap, MAX_TARIF_MIT_PAEDAGOGISCHER_BETREUUNG, gesuchsperiode, gemeinde));
		this.setMaxTarifTagesschuleOhnePaedagogischerBetreuung(asBigDecimal(paramMap, MAX_TARIF_OHNE_PAEDAGOGISCHER_BETREUUNG, gesuchsperiode, gemeinde));
		this.setMinTarifTagesschule(asBigDecimal(paramMap, MIN_TARIF, gesuchsperiode, gemeinde));
		this.setMahlzeitenverguenstigungEnabled(asBoolean(paramMap, EinstellungKey.GEMEINDE_MAHLZEITENVERGUENSTIGUNG_ENABLED, gesuchsperiode, gemeinde));
		this.setGemeindeParameter(new BGRechnerParameterGemeindeDTO(paramMap, gesuchsperiode, gemeinde));
	}

	public BGRechnerParameterDTO() {

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

	public BigDecimal getMaxVerguenstigungVorschuleBabyProTg() {
		return maxVerguenstigungVorschuleBabyProTg;
	}

	public void setMaxVerguenstigungVorschuleBabyProTg(BigDecimal maxVerguenstigungVorschuleBabyProTg) {
		this.maxVerguenstigungVorschuleBabyProTg = maxVerguenstigungVorschuleBabyProTg;
	}

	public BigDecimal getMaxVerguenstigungVorschuleKindProTg() {
		return maxVerguenstigungVorschuleKindProTg;
	}

	public void setMaxVerguenstigungVorschuleKindProTg(BigDecimal maxVerguenstigungVorschuleKindProTg) {
		this.maxVerguenstigungVorschuleKindProTg = maxVerguenstigungVorschuleKindProTg;
	}

	public BigDecimal getMaxVerguenstigungSchuleKindProTg() {
		return maxVerguenstigungSchuleKindProTg;
	}

	public void setMaxVerguenstigungSchuleKindProTg(BigDecimal maxVerguenstigungSchuleKindProTg) {
		this.maxVerguenstigungSchuleKindProTg = maxVerguenstigungSchuleKindProTg;
	}

	public BigDecimal getMaxVerguenstigungVorschuleBabyProStd() {
		return maxVerguenstigungVorschuleBabyProStd;
	}

	public void setMaxVerguenstigungVorschuleBabyProStd(BigDecimal maxVerguenstigungVorschuleBabyProStd) {
		this.maxVerguenstigungVorschuleBabyProStd = maxVerguenstigungVorschuleBabyProStd;
	}

	public BigDecimal getMaxVerguenstigungVorschuleKindProStd() {
		return maxVerguenstigungVorschuleKindProStd;
	}

	public void setMaxVerguenstigungVorschuleKindProStd(BigDecimal maxVerguenstigungVorschuleKindProStd) {
		this.maxVerguenstigungVorschuleKindProStd = maxVerguenstigungVorschuleKindProStd;
	}

	public BigDecimal getMaxVerguenstigungSchuleKindProStd() {
		return maxVerguenstigungSchuleKindProStd;
	}

	public void setMaxVerguenstigungSchuleKindProStd(BigDecimal maxVerguenstigungSchuleKindProStd) {
		this.maxVerguenstigungSchuleKindProStd = maxVerguenstigungSchuleKindProStd;
	}

	public BigDecimal getMaxMassgebendesEinkommen() {
		return maxMassgebendesEinkommen;
	}

	public void setMaxMassgebendesEinkommen(BigDecimal maxMassgebendesEinkommen) {
		this.maxMassgebendesEinkommen = maxMassgebendesEinkommen;
	}

	public BigDecimal getMinMassgebendesEinkommen() {
		return minMassgebendesEinkommen;
	}

	public void setMinMassgebendesEinkommen(BigDecimal minMassgebendesEinkommen) {
		this.minMassgebendesEinkommen = minMassgebendesEinkommen;
	}

	public BigDecimal getOeffnungstageKita() {
		return oeffnungstageKita;
	}

	public void setOeffnungstageKita(BigDecimal oeffnungstageKita) {
		this.oeffnungstageKita = oeffnungstageKita;
	}

	public BigDecimal getOeffnungstageTFO() {
		return oeffnungstageTFO;
	}

	public void setOeffnungstageTFO(BigDecimal oeffnungstageTFO) {
		this.oeffnungstageTFO = oeffnungstageTFO;
	}

	public BigDecimal getOeffnungsstundenTFO() {
		return oeffnungsstundenTFO;
	}

	public void setOeffnungsstundenTFO(BigDecimal oeffnungsstundenTFO) {
		this.oeffnungsstundenTFO = oeffnungsstundenTFO;
	}

	public BigDecimal getZuschlagBehinderungProTg() {
		return zuschlagBehinderungProTg;
	}

	public void setZuschlagBehinderungProTg(BigDecimal zuschlagBehinderungProTg) {
		this.zuschlagBehinderungProTg = zuschlagBehinderungProTg;
	}

	public BigDecimal getZuschlagBehinderungProStd() {
		return zuschlagBehinderungProStd;
	}

	public void setZuschlagBehinderungProStd(BigDecimal zuschlagBehinderungProStd) {
		this.zuschlagBehinderungProStd = zuschlagBehinderungProStd;
	}

	public BigDecimal getMinVerguenstigungProTg() {
		return minVerguenstigungProTg;
	}

	public void setMinVerguenstigungProTg(BigDecimal minVerguenstigungProTg) {
		this.minVerguenstigungProTg = minVerguenstigungProTg;
	}

	public BigDecimal getMinVerguenstigungProStd() {
		return minVerguenstigungProStd;
	}

	public void setMinVerguenstigungProStd(BigDecimal minVerguenstigungProStd) {
		this.minVerguenstigungProStd = minVerguenstigungProStd;
	}

	public BigDecimal getMaxTarifTagesschuleMitPaedagogischerBetreuung() {
		return maxTarifTagesschuleMitPaedagogischerBetreuung;
	}

	public void setMaxTarifTagesschuleMitPaedagogischerBetreuung(BigDecimal maxTarifTagesschuleMitPaedagogischerBetreuung) {
		this.maxTarifTagesschuleMitPaedagogischerBetreuung = maxTarifTagesschuleMitPaedagogischerBetreuung;
	}

	public BigDecimal getMaxTarifTagesschuleOhnePaedagogischerBetreuung() {
		return maxTarifTagesschuleOhnePaedagogischerBetreuung;
	}

	public void setMaxTarifTagesschuleOhnePaedagogischerBetreuung(BigDecimal maxTarifTagesschuleOhnePaedagogischerBetreuung) {
		this.maxTarifTagesschuleOhnePaedagogischerBetreuung = maxTarifTagesschuleOhnePaedagogischerBetreuung;
	}

	public BigDecimal getMinTarifTagesschule() {
		return minTarifTagesschule;
	}

	public void setMinTarifTagesschule(BigDecimal minTarifTagesschule) {
		this.minTarifTagesschule = minTarifTagesschule;
	}

	public Boolean getMahlzeitenverguenstigungEnabled() {
		return mahlzeitenverguenstigungEnabled;
	}

	public void setMahlzeitenverguenstigungEnabled(Boolean mahlzeitenverguenstigungEnabled) {
		this.mahlzeitenverguenstigungEnabled = mahlzeitenverguenstigungEnabled;
	}

	public BGRechnerParameterGemeindeDTO getGemeindeParameter() {
		return gemeindeParameter;
	}

	public void setGemeindeParameter(BGRechnerParameterGemeindeDTO gemeindeParameter) {
		this.gemeindeParameter = gemeindeParameter;
	}
}
