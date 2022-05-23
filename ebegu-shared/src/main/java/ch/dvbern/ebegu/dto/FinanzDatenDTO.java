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

package ch.dvbern.ebegu.dto;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * DTO für die Resultate der Berechnungen der Finanziellen Situation und eventueller Einkommensverschlechterungen.
 *
 * die Werte massgebendesEinkommenBasisjahrPlus1, massgebendesEinkommenBasisjahrPlus1 und datumVonBasisjahrPlus1 sowie
 * datumVonBasisjahrPlus2 sind nur gesetzt wenn die jeweilige Einkommensverschlechterung akzeptiert wurde
 */
public class FinanzDatenDTO implements Serializable {

	private static final long serialVersionUID = -1595385522554790800L;

	private BigDecimal massgebendesEinkBjVorAbzFamGr = BigDecimal.ZERO;
	private BigDecimal massgebendesEinkBjP1VorAbzFamGr = BigDecimal.ZERO;
	private BigDecimal massgebendesEinkBjP2VorAbzFamGr = BigDecimal.ZERO;

	// EKV wurde erfasst
	private boolean ekv1Erfasst = false;
	private boolean ekv2Erfasst = false;

	// 'accepted' bedeutet, dass die EKV mehr als den Grenzwert erreicht, z.B. > 20%
	private BigDecimal minEKV = BigDecimal.ZERO;
	private boolean ekv1Accepted = false;
	private boolean ekv2Accepted = false;

	// Das JA kann aber trotzdem die EKV manuell ablehnen, wenn z.B. Dokumente nicht eingereicht werden oder die EKV
	// vom GS im online Gesuch erfasst wurde, obwohl er sie nicht hätte erfassen sollen
	private boolean ekv1Annulliert = false;
	private boolean ekv2Annulliert = false;

	public FinanzDatenDTO(BigDecimal minEKV) {
		this.minEKV = minEKV;
	}

	public BigDecimal getMassgebendesEinkBjVorAbzFamGr() {
		return massgebendesEinkBjVorAbzFamGr;
	}

	public void setMassgebendesEinkBjVorAbzFamGr(BigDecimal massgebendesEinkBjVorAbzFamGr) {
		this.massgebendesEinkBjVorAbzFamGr = massgebendesEinkBjVorAbzFamGr;
	}

	public BigDecimal getMassgebendesEinkBjP1VorAbzFamGr() {
		return massgebendesEinkBjP1VorAbzFamGr;
	}

	public void setMassgebendesEinkBjP1VorAbzFamGr(BigDecimal massgebendesEinkBjP1VorAbzFamGr) {
		this.massgebendesEinkBjP1VorAbzFamGr = massgebendesEinkBjP1VorAbzFamGr;
	}

	public BigDecimal getMassgebendesEinkBjP2VorAbzFamGr() {
		return massgebendesEinkBjP2VorAbzFamGr;
	}

	public void setMassgebendesEinkBjP2VorAbzFamGr(BigDecimal massgebendesEinkBjP2VorAbzFamGr) {
		this.massgebendesEinkBjP2VorAbzFamGr = massgebendesEinkBjP2VorAbzFamGr;
	}

	public boolean isEkv1Erfasst() {
		return ekv1Erfasst;
	}

	public void setEkv1Erfasst(boolean ekv1Erfasst) {
		this.ekv1Erfasst = ekv1Erfasst;
	}

	public boolean isEkv2Erfasst() {
		return ekv2Erfasst;
	}

	public void setEkv2Erfasst(boolean ekv2Erfasst) {
		this.ekv2Erfasst = ekv2Erfasst;
	}

	public BigDecimal getMinEKV() {
		return minEKV;
	}

	public void setMinEKV(BigDecimal minEKV) {
		this.minEKV = minEKV;
	}

	public boolean isEkv1Accepted() {
		return ekv1Accepted;
	}

	public void setEkv1Accepted(boolean ekv1Accepted) {
		this.ekv1Accepted = ekv1Accepted;
	}

	public boolean isEkv2Accepted() {
		return ekv2Accepted;
	}

	public void setEkv2Accepted(boolean ekv2Accepted) {
		this.ekv2Accepted = ekv2Accepted;
	}

	public boolean isEkv1Annulliert() {
		return ekv1Annulliert;
	}

	public void setEkv1Annulliert(boolean ekv1Annulliert) {
		this.ekv1Annulliert = ekv1Annulliert;
	}

	public boolean isEkv2Annulliert() {
		return ekv2Annulliert;
	}

	public void setEkv2Annulliert(boolean ekv2Annulliert) {
		this.ekv2Annulliert = ekv2Annulliert;
	}

	public boolean isEkv1AcceptedAndNotAnnuliert() {
		return ekv1Accepted && !ekv1Annulliert;
	}

	public boolean isEkv2AcceptedAndNotAnnuliert() {
		return ekv2Accepted && !ekv2Annulliert;
	}
}
