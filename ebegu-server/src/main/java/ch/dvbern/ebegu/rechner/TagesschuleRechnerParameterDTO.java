/*
 * Copyright (C) 2019 DV Bern AG, Switzerland
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
import ch.dvbern.ebegu.enums.EinstellungKey;
import ch.dvbern.ebegu.enums.ErrorCodeEnum;
import ch.dvbern.ebegu.errors.EbeguEntityNotFoundException;

import static ch.dvbern.ebegu.enums.EinstellungKey.MAX_MASSGEBENDES_EINKOMMEN;
import static ch.dvbern.ebegu.enums.EinstellungKey.MIN_MASSGEBENDES_EINKOMMEN;

/**
 * Kapselung aller Parameter, welche für die Tagesschule-Berechnung aller Angebote benötigt werden.
 * Diese müssen aus den Einstellungen gelesen werden.
 */
public final class TagesschuleRechnerParameterDTO {

	private BigDecimal maxTarifMitPaedagogischerBetreuung;
	private BigDecimal maxTarifOhnePaedagogischeBetreuung;
	private BigDecimal minTarif;
	private BigDecimal maxMassgebendesEinkommen;
	private BigDecimal minMassgebendesEinkommen;


	public TagesschuleRechnerParameterDTO(Map<EinstellungKey, Einstellung> paramMap, Gesuchsperiode gesuchsperiode, Gemeinde gemeinde) {
		// TODO: Die Werte aus den Einstellungen lesen!
		this.setMaxMassgebendesEinkommen(asBigDecimal(paramMap, MAX_MASSGEBENDES_EINKOMMEN, gesuchsperiode, gemeinde));
		this.setMinMassgebendesEinkommen(asBigDecimal(paramMap, MIN_MASSGEBENDES_EINKOMMEN, gesuchsperiode, gemeinde));
	}

	public TagesschuleRechnerParameterDTO() {

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

	public BigDecimal getMaxTarifMitPaedagogischerBetreuung() {
		return maxTarifMitPaedagogischerBetreuung;
	}

	public void setMaxTarifMitPaedagogischerBetreuung(BigDecimal maxTarifMitPaedagogischerBetreuung) {
		this.maxTarifMitPaedagogischerBetreuung = maxTarifMitPaedagogischerBetreuung;
	}

	public BigDecimal getMaxTarifOhnePaedagogischeBetreuung() {
		return maxTarifOhnePaedagogischeBetreuung;
	}

	public void setMaxTarifOhnePaedagogischeBetreuung(BigDecimal maxTarifOhnePaedagogischeBetreuung) {
		this.maxTarifOhnePaedagogischeBetreuung = maxTarifOhnePaedagogischeBetreuung;
	}

	public BigDecimal getMinTarif() {
		return minTarif;
	}

	public void setMinTarif(BigDecimal minTarif) {
		this.minTarif = minTarif;
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

}
