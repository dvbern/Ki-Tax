/*
 * Copyright (C) 2021 DV Bern AG, Switzerland
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

package ch.dvbern.ebegu.api.dtos;

import java.math.BigDecimal;

import javax.annotation.Nullable;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

@XmlAccessorType(XmlAccessType.FIELD)
public class JaxFinanzielleSituationSelbstdeklaration extends JaxAbstractDTO {

	private static final long serialVersionUID = -403916876154757656L;

	@Nullable
	private BigDecimal einkunftErwerb;

	@Nullable
	private BigDecimal einkunftVersicherung;

	@Nullable
	private BigDecimal einkunftWertschriften;

	@Nullable
	private BigDecimal einkunftUnterhaltsbeitragKinder;

	@Nullable
	private BigDecimal einkunftUeberige;

	@Nullable
	private BigDecimal einkunftLiegenschaften;

	@Nullable
	private BigDecimal abzugBerufsauslagen;

	@Nullable
	private BigDecimal abzugUnterhaltsbeitragKinder;

	@Nullable
	private BigDecimal abzugSaeule3A;

	@Nullable
	private BigDecimal abzugVersicherungspraemien;

	@Nullable
	private BigDecimal abzugKrankheitsUnfallKosten;

	@Nullable
	private BigDecimal sonderabzugErwerbstaetigkeitEhegatten;

	@Nullable
	private BigDecimal abzugKinderVorschule;

	@Nullable
	private BigDecimal abzugKinderSchule;

	@Nullable
	private BigDecimal abzugEigenbetreuung;

	@Nullable
	private BigDecimal abzugFremdbetreuung;

	@Nullable
	private BigDecimal abzugErwerbsunfaehigePersonen;

	@Nullable
	private BigDecimal vermoegen;

	@Nullable
	private BigDecimal abzugSteuerfreierBetragErwachsene;

	@Nullable
	private BigDecimal abzugSteuerfreierBetragKinder;

	@Nullable
	public BigDecimal getEinkunftErwerb() {
		return einkunftErwerb;
	}

	public void setEinkunftErwerb(@Nullable BigDecimal einkunftErwerb) {
		this.einkunftErwerb = einkunftErwerb;
	}

	@Nullable
	public BigDecimal getEinkunftVersicherung() {
		return einkunftVersicherung;
	}

	public void setEinkunftVersicherung(@Nullable BigDecimal einkunftVersicherung) {
		this.einkunftVersicherung = einkunftVersicherung;
	}

	@Nullable
	public BigDecimal getEinkunftWertschriften() {
		return einkunftWertschriften;
	}

	public void setEinkunftWertschriften(@Nullable BigDecimal einkunftWertschriften) {
		this.einkunftWertschriften = einkunftWertschriften;
	}

	@Nullable
	public BigDecimal getEinkunftUnterhaltsbeitragKinder() {
		return einkunftUnterhaltsbeitragKinder;
	}

	public void setEinkunftUnterhaltsbeitragKinder(@Nullable BigDecimal einkunftUnterhaltsbeitragKinder) {
		this.einkunftUnterhaltsbeitragKinder = einkunftUnterhaltsbeitragKinder;
	}

	@Nullable
	public BigDecimal getEinkunftUeberige() {
		return einkunftUeberige;
	}

	public void setEinkunftUeberige(@Nullable BigDecimal einkunftUeberige) {
		this.einkunftUeberige = einkunftUeberige;
	}

	@Nullable
	public BigDecimal getEinkunftLiegenschaften() {
		return einkunftLiegenschaften;
	}

	public void setEinkunftLiegenschaften(@Nullable BigDecimal einkunftLiegenschaften) {
		this.einkunftLiegenschaften = einkunftLiegenschaften;
	}

	@Nullable
	public BigDecimal getAbzugBerufsauslagen() {
		return abzugBerufsauslagen;
	}

	public void setAbzugBerufsauslagen(@Nullable BigDecimal abzugBerufsauslagen) {
		this.abzugBerufsauslagen = abzugBerufsauslagen;
	}

	@Nullable
	public BigDecimal getAbzugUnterhaltsbeitragKinder() {
		return abzugUnterhaltsbeitragKinder;
	}

	public void setAbzugUnterhaltsbeitragKinder(@Nullable BigDecimal abzugUnterhaltsbeitragKinder) {
		this.abzugUnterhaltsbeitragKinder = abzugUnterhaltsbeitragKinder;
	}

	@Nullable
	public BigDecimal getAbzugSaeule3A() {
		return abzugSaeule3A;
	}

	public void setAbzugSaeule3A(@Nullable BigDecimal abzugSaeule3A) {
		this.abzugSaeule3A = abzugSaeule3A;
	}

	@Nullable
	public BigDecimal getAbzugVersicherungspraemien() {
		return abzugVersicherungspraemien;
	}

	public void setAbzugVersicherungspraemien(@Nullable BigDecimal abzugVersicherungspraemien) {
		this.abzugVersicherungspraemien = abzugVersicherungspraemien;
	}

	@Nullable
	public BigDecimal getAbzugKrankheitsUnfallKosten() {
		return abzugKrankheitsUnfallKosten;
	}

	public void setAbzugKrankheitsUnfallKosten(@Nullable BigDecimal abzugKrankheitsUnfallKosten) {
		this.abzugKrankheitsUnfallKosten = abzugKrankheitsUnfallKosten;
	}

	@Nullable
	public BigDecimal getSonderabzugErwerbstaetigkeitEhegatten() {
		return sonderabzugErwerbstaetigkeitEhegatten;
	}

	public void setSonderabzugErwerbstaetigkeitEhegatten(@Nullable BigDecimal sonderabzugErwerbstaetigkeitEhegatten) {
		this.sonderabzugErwerbstaetigkeitEhegatten = sonderabzugErwerbstaetigkeitEhegatten;
	}

	@Nullable
	public BigDecimal getAbzugKinderVorschule() {
		return abzugKinderVorschule;
	}

	public void setAbzugKinderVorschule(@Nullable BigDecimal abzugKinderVorschule) {
		this.abzugKinderVorschule = abzugKinderVorschule;
	}

	@Nullable
	public BigDecimal getAbzugKinderSchule() {
		return abzugKinderSchule;
	}

	public void setAbzugKinderSchule(@Nullable BigDecimal abzugKinderSchule) {
		this.abzugKinderSchule = abzugKinderSchule;
	}

	@Nullable
	public BigDecimal getAbzugEigenbetreuung() {
		return abzugEigenbetreuung;
	}

	public void setAbzugEigenbetreuung(@Nullable BigDecimal abzugEigenbetreuung) {
		this.abzugEigenbetreuung = abzugEigenbetreuung;
	}

	@Nullable
	public BigDecimal getAbzugFremdbetreuung() {
		return abzugFremdbetreuung;
	}

	public void setAbzugFremdbetreuung(@Nullable BigDecimal abzugFremdbetreuung) {
		this.abzugFremdbetreuung = abzugFremdbetreuung;
	}

	@Nullable
	public BigDecimal getAbzugErwerbsunfaehigePersonen() {
		return abzugErwerbsunfaehigePersonen;
	}

	public void setAbzugErwerbsunfaehigePersonen(@Nullable BigDecimal abzugErwerbsunfaehigePersonen) {
		this.abzugErwerbsunfaehigePersonen = abzugErwerbsunfaehigePersonen;
	}

	@Nullable
	public BigDecimal getVermoegen() {
		return vermoegen;
	}

	public void setVermoegen(@Nullable BigDecimal vermoegen) {
		this.vermoegen = vermoegen;
	}

	@Nullable
	public BigDecimal getAbzugSteuerfreierBetragErwachsene() {
		return abzugSteuerfreierBetragErwachsene;
	}

	public void setAbzugSteuerfreierBetragErwachsene(@Nullable BigDecimal abzugSteuerfreierBetragErwachsene) {
		this.abzugSteuerfreierBetragErwachsene = abzugSteuerfreierBetragErwachsene;
	}

	@Nullable
	public BigDecimal getAbzugSteuerfreierBetragKinder() {
		return abzugSteuerfreierBetragKinder;
	}

	public void setAbzugSteuerfreierBetragKinder(@Nullable BigDecimal abzugSteuerfreierBetragKinder) {
		this.abzugSteuerfreierBetragKinder = abzugSteuerfreierBetragKinder;
	}
}
