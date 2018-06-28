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

import ch.dvbern.ebegu.entities.EbeguParameter;
import ch.dvbern.ebegu.entities.Gesuchsperiode;
import ch.dvbern.ebegu.entities.Mandant;
import ch.dvbern.ebegu.enums.EbeguParameterKey;
import ch.dvbern.ebegu.enums.ErrorCodeEnum;
import ch.dvbern.ebegu.errors.EbeguEntityNotFoundException;

import static ch.dvbern.ebegu.enums.EbeguParameterKey.PARAM_ABGELTUNG_PRO_TAG_KANTON;
import static ch.dvbern.ebegu.enums.EbeguParameterKey.PARAM_ANZAHL_TAGE_KANTON;
import static ch.dvbern.ebegu.enums.EbeguParameterKey.PARAM_ANZAL_TAGE_MAX_KITA;
import static ch.dvbern.ebegu.enums.EbeguParameterKey.PARAM_BABY_ALTER_IN_MONATEN;
import static ch.dvbern.ebegu.enums.EbeguParameterKey.PARAM_BABY_FAKTOR;
import static ch.dvbern.ebegu.enums.EbeguParameterKey.PARAM_KOSTEN_PRO_STUNDE_MAX;
import static ch.dvbern.ebegu.enums.EbeguParameterKey.PARAM_KOSTEN_PRO_STUNDE_MAX_TAGESELTERN;
import static ch.dvbern.ebegu.enums.EbeguParameterKey.PARAM_KOSTEN_PRO_STUNDE_MIN;
import static ch.dvbern.ebegu.enums.EbeguParameterKey.PARAM_MASSGEBENDES_EINKOMMEN_MAX;
import static ch.dvbern.ebegu.enums.EbeguParameterKey.PARAM_MASSGEBENDES_EINKOMMEN_MIN;
import static ch.dvbern.ebegu.enums.EbeguParameterKey.PARAM_STUNDEN_PRO_TAG_MAX_KITA;
import static ch.dvbern.ebegu.enums.EbeguParameterKey.PARAM_STUNDEN_PRO_TAG_TAGI;

/**
 * Kapselung aller Parameter, welche für die BG-Berechnung aller Angebote benötigt werden.
 * Diese müssen aus den EbeguParametern gelesen werden.
 */
public final class BGRechnerParameterDTO {

	private BigDecimal beitragKantonProTag;        // PARAM_ABGELTUNG_PRO_TAG_KANTON

	private BigDecimal beitragStadtProTagJahr1;            // PARAM_FIXBETRAG_STADT_PRO_TAG_KITA
	private BigDecimal beitragStadtProTagJahr2;            // PARAM_FIXBETRAG_STADT_PRO_TAG_KITA

	private BigDecimal anzahlTageTagi;                // PARAM_ANZAHL_TAGE_KANTON
	private BigDecimal anzahlTageMaximal;            // PARAM_ANZAL_TAGE_MAX_KITA

	private BigDecimal anzahlStundenProTagTagi;    // PARAM_STUNDEN_PRO_TAG_TAGI
	private BigDecimal anzahlStundenProTagMaximal;    // PARAM_STUNDEN_PRO_TAG_MAX_KITA

	private BigDecimal kostenProStundeMaximalKitaTagi; // PARAM_KOSTEN_PRO_STUNDE_MAX
	private BigDecimal kostenProStundeMaximalTageseltern; // PARAM_KOSTEN_PRO_STUNDE_MAX_TAGESELTERN
	private BigDecimal kostenProStundeMinimal;        // PARAM_KOSTEN_PRO_STUNDE_MIN

	private BigDecimal massgebendesEinkommenMaximal; // PARAM_MASSGEBENDES_EINKOMMEN_MIN
	private BigDecimal massgebendesEinkommenMinimal; // PARAM_MASSGEBENDES_EINKOMMEN_MAX

	private BigDecimal babyFaktor;                    // PARAM_BABY_FAKTOR
	private int babyAlterInMonaten;                    // PARAM_BABY_ALTER_IN_MONATEN

	public BGRechnerParameterDTO(Map<EbeguParameterKey, EbeguParameter> paramMap, Gesuchsperiode gesuchsperiode, Mandant mandant) {

		this.setBeitragKantonProTag(asBigDecimal(paramMap, PARAM_ABGELTUNG_PRO_TAG_KANTON, gesuchsperiode, mandant));
		this.setAnzahlTageMaximal(asBigDecimal(paramMap, PARAM_ANZAL_TAGE_MAX_KITA, gesuchsperiode, mandant));
		this.setAnzahlStundenProTagMaximal(asBigDecimal(paramMap, PARAM_STUNDEN_PRO_TAG_MAX_KITA, gesuchsperiode, mandant));
		this.setKostenProStundeMaximalKitaTagi(asBigDecimal(paramMap, PARAM_KOSTEN_PRO_STUNDE_MAX, gesuchsperiode, mandant));
		this.setKostenProStundeMinimal(asBigDecimal(paramMap, PARAM_KOSTEN_PRO_STUNDE_MIN, gesuchsperiode, mandant));
		this.setMassgebendesEinkommenMaximal(asBigDecimal(paramMap, PARAM_MASSGEBENDES_EINKOMMEN_MAX, gesuchsperiode, mandant));
		this.setMassgebendesEinkommenMinimal(asBigDecimal(paramMap, PARAM_MASSGEBENDES_EINKOMMEN_MIN, gesuchsperiode, mandant));
		this.setAnzahlTageTagi(asBigDecimal(paramMap, PARAM_ANZAHL_TAGE_KANTON, gesuchsperiode, mandant));
		this.setAnzahlStundenProTagTagi(asBigDecimal(paramMap, PARAM_STUNDEN_PRO_TAG_TAGI, gesuchsperiode, mandant));
		this.setKostenProStundeMaximalTageseltern(asBigDecimal(paramMap, PARAM_KOSTEN_PRO_STUNDE_MAX_TAGESELTERN, gesuchsperiode, mandant));
		this.setBabyAlterInMonaten(asInteger(paramMap, PARAM_BABY_ALTER_IN_MONATEN, gesuchsperiode, mandant));
		this.setBabyFaktor(asBigDecimal(paramMap, PARAM_BABY_FAKTOR, gesuchsperiode, mandant));
	}

	public BGRechnerParameterDTO() {

	}

	private int asInteger(Map<EbeguParameterKey, EbeguParameter> paramMap, EbeguParameterKey paramKey, Gesuchsperiode gesuchsperiode, Mandant mandant) {
		EbeguParameter param = paramMap.get(paramKey);
		if (param == null) {
			String message = "Required calculator parameter '" + paramKey + "' could not be loaded for the given Mandant '" + mandant + "', Gesuchsperiode '" + gesuchsperiode + "'";
			throw new EbeguEntityNotFoundException("loadCalculatorParameters", message, ErrorCodeEnum.ERROR_PARAMETER_NOT_FOUND, paramKey);
		}
		return param.getValueAsInteger();
	}

	private BigDecimal asBigDecimal(Map<EbeguParameterKey, EbeguParameter> paramMap, EbeguParameterKey paramKey, Gesuchsperiode gesuchsperiode, Mandant mandant) {
		EbeguParameter param = paramMap.get(paramKey);
		if (param == null) {
			String message = "Required calculator parameter '" + paramKey + "' could not be loaded for the given Mandant '" + mandant + "', Gesuchsperiode "
				+ '\'' + gesuchsperiode + '\'';
			throw new EbeguEntityNotFoundException("loadCalculatorParameters", message, ErrorCodeEnum.ERROR_PARAMETER_NOT_FOUND, paramKey);
		}
		return param.getValueAsBigDecimal();
	}

	public BigDecimal getBeitragKantonProTag() {
		return beitragKantonProTag;
	}

	public void setBeitragKantonProTag(BigDecimal beitragKantonProTag) {
		this.beitragKantonProTag = beitragKantonProTag;
	}

	public BigDecimal getBeitragStadtProTagJahr1() {
		return beitragStadtProTagJahr1;
	}

	public void setBeitragStadtProTagJahr1(BigDecimal beitragStadtProTagJahr1) {
		this.beitragStadtProTagJahr1 = beitragStadtProTagJahr1;
	}

	public BigDecimal getBeitragStadtProTagJahr2() {
		return beitragStadtProTagJahr2;
	}

	public void setBeitragStadtProTagJahr2(BigDecimal beitragStadtProTagJahr2) {
		this.beitragStadtProTagJahr2 = beitragStadtProTagJahr2;
	}

	public BigDecimal getAnzahlTageMaximal() {
		return anzahlTageMaximal;
	}

	public void setAnzahlTageMaximal(BigDecimal anzahlTageMaximal) {
		this.anzahlTageMaximal = anzahlTageMaximal;
	}

	public BigDecimal getAnzahlStundenProTagMaximal() {
		return anzahlStundenProTagMaximal;
	}

	public void setAnzahlStundenProTagMaximal(BigDecimal anzahlStundenProTagMaximal) {
		this.anzahlStundenProTagMaximal = anzahlStundenProTagMaximal;
	}

	public BigDecimal getKostenProStundeMaximalKitaTagi() {
		return kostenProStundeMaximalKitaTagi;
	}

	public void setKostenProStundeMaximalKitaTagi(BigDecimal kostenProStundeMaximalKitaTagi) {
		this.kostenProStundeMaximalKitaTagi = kostenProStundeMaximalKitaTagi;
	}

	public BigDecimal getKostenProStundeMinimal() {
		return kostenProStundeMinimal;
	}

	public void setKostenProStundeMinimal(BigDecimal kostenProStundeMinimal) {
		this.kostenProStundeMinimal = kostenProStundeMinimal;
	}

	public BigDecimal getMassgebendesEinkommenMaximal() {
		return massgebendesEinkommenMaximal;
	}

	public void setMassgebendesEinkommenMaximal(BigDecimal massgebendesEinkommenMaximal) {
		this.massgebendesEinkommenMaximal = massgebendesEinkommenMaximal;
	}

	public BigDecimal getMassgebendesEinkommenMinimal() {
		return massgebendesEinkommenMinimal;
	}

	public void setMassgebendesEinkommenMinimal(BigDecimal massgebendesEinkommenMinimal) {
		this.massgebendesEinkommenMinimal = massgebendesEinkommenMinimal;
	}

	public BigDecimal getAnzahlTageTagi() {
		return anzahlTageTagi;
	}

	public void setAnzahlTageTagi(BigDecimal anzahlTageTagi) {
		this.anzahlTageTagi = anzahlTageTagi;
	}

	public BigDecimal getAnzahlStundenProTagTagi() {
		return anzahlStundenProTagTagi;
	}

	public void setAnzahlStundenProTagTagi(BigDecimal anzahlStundenProTagTagi) {
		this.anzahlStundenProTagTagi = anzahlStundenProTagTagi;
	}

	public BigDecimal getKostenProStundeMaximalTageseltern() {
		return kostenProStundeMaximalTageseltern;
	}

	public void setKostenProStundeMaximalTageseltern(BigDecimal kostenProStundeMaximalTageseltern) {
		this.kostenProStundeMaximalTageseltern = kostenProStundeMaximalTageseltern;
	}

	public BigDecimal getBabyFaktor() {
		return babyFaktor;
	}

	public void setBabyFaktor(BigDecimal babyFaktor) {
		this.babyFaktor = babyFaktor;
	}

	public int getBabyAlterInMonaten() {
		return babyAlterInMonaten;
	}

	public void setBabyAlterInMonaten(int babyAlterInMonaten) {
		this.babyAlterInMonaten = babyAlterInMonaten;
	}
}
