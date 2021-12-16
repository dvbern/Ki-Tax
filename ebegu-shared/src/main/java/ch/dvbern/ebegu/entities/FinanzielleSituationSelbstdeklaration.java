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

package ch.dvbern.ebegu.entities;

import java.math.BigDecimal;

import javax.annotation.Nullable;
import javax.persistence.Column;
import javax.persistence.Entity;

import ch.dvbern.ebegu.util.MathUtil;
import org.hibernate.envers.Audited;

@Audited
@Entity
public class FinanzielleSituationSelbstdeklaration extends AbstractMutableEntity {

	private static final long serialVersionUID = -731322115720756445L;

	@Nullable
	@Column(nullable = true)
	private BigDecimal einkuftErwerb;

	@Nullable
	@Column(nullable = true)
	private BigDecimal einkuftVersicherung;

	@Nullable
	@Column(nullable = true)
	private BigDecimal einkuftAusgleichskassen;

	@Nullable
	@Column(nullable = true)
	private BigDecimal einkuftWertschriften;

	@Nullable
	@Column(nullable = true)
	private BigDecimal einkuftUnterhaltsbeitragSteuerpflichtige;

	@Nullable
	@Column(nullable = true)
	private BigDecimal einkuftUnterhaltsbeitragKinder;

	@Nullable
	@Column(nullable = true)
	private BigDecimal einkuftUeberige;

	@Nullable
	@Column(nullable = true)
	private BigDecimal einkunftLiegenschaften;

	@Nullable
	@Column(nullable = true)
	private BigDecimal abzugBerufsauslagen;

	@Nullable
	@Column(nullable = true)
	private BigDecimal abzugSchuldzinsen;

	@Nullable
	@Column(nullable = true)
	private BigDecimal abzugUnterhaltsbeitragEhepartner;

	@Nullable
	@Column(nullable = true)
	private BigDecimal abzugUnterhaltsbeitragKinder;

	@Nullable
	@Column(nullable = true)
	private BigDecimal abzugRentenleistungen;

	@Nullable
	@Column(nullable = true)
	private BigDecimal abzugSaeule3A;

	@Nullable
	@Column(nullable = true)
	private BigDecimal abzugVersicherungspraemien;

	@Nullable
	@Column(nullable = true)
	private BigDecimal abzugKrankheitsUnfallKosten;

	@Nullable
	@Column(nullable = true)
	private BigDecimal abzugFreiweiligeZuwendungPartien;

	@Nullable
	@Column(nullable = true)
	private BigDecimal abzugKinderVorschule;

	@Nullable
	@Column(nullable = true)
	private BigDecimal abzugKinderSchule;

	@Nullable
	@Column(nullable = true)
	private BigDecimal abzugKinderAuswaertigerAufenthalt;

	@Nullable
	@Column(nullable = true)
	private BigDecimal abzugEigenbetreuung;

	@Nullable
	@Column(nullable = true)
	private BigDecimal abzugFremdbetreuung;

	@Nullable
	@Column(nullable = true)
	private BigDecimal abzugErwerbsunfaehigePersonen;

	@Nullable
	@Column(nullable = true)
	private BigDecimal vermoegen;

	@Nullable
	@Column(nullable = true)
	private BigDecimal abzugSteuerfreierBetragErwachsene;

	@Nullable
	public BigDecimal getEinkuftErwerb() {
		return einkuftErwerb;
	}

	public void setEinkuftErwerb(@Nullable BigDecimal einkuftErwerb) {
		this.einkuftErwerb = einkuftErwerb;
	}

	@Nullable
	public BigDecimal getEinkuftVersicherung() {
		return einkuftVersicherung;
	}

	public void setEinkuftVersicherung(@Nullable BigDecimal einkuftVersicherung) {
		this.einkuftVersicherung = einkuftVersicherung;
	}

	@Nullable
	public BigDecimal getEinkuftAusgleichskassen() {
		return einkuftAusgleichskassen;
	}

	public void setEinkuftAusgleichskassen(@Nullable BigDecimal einkuftAusgleichskassen) {
		this.einkuftAusgleichskassen = einkuftAusgleichskassen;
	}

	@Nullable
	public BigDecimal getEinkuftWertschriften() {
		return einkuftWertschriften;
	}

	public void setEinkuftWertschriften(@Nullable BigDecimal einkuftWertschriften) {
		this.einkuftWertschriften = einkuftWertschriften;
	}

	@Nullable
	public BigDecimal getEinkuftUnterhaltsbeitragSteuerpflichtige() {
		return einkuftUnterhaltsbeitragSteuerpflichtige;
	}

	public void setEinkuftUnterhaltsbeitragSteuerpflichtige(
		@Nullable BigDecimal einkuftUnterhaltsbeitragSteuerpflichtige) {
		this.einkuftUnterhaltsbeitragSteuerpflichtige = einkuftUnterhaltsbeitragSteuerpflichtige;
	}

	@Nullable
	public BigDecimal getEinkuftUnterhaltsbeitragKinder() {
		return einkuftUnterhaltsbeitragKinder;
	}

	public void setEinkuftUnterhaltsbeitragKinder(@Nullable BigDecimal einkuftUnterhaltsbeitragKinder) {
		this.einkuftUnterhaltsbeitragKinder = einkuftUnterhaltsbeitragKinder;
	}

	@Nullable
	public BigDecimal getEinkuftUeberige() {
		return einkuftUeberige;
	}

	public void setEinkuftUeberige(@Nullable BigDecimal einkuftUeberige) {
		this.einkuftUeberige = einkuftUeberige;
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
	public BigDecimal getAbzugSchuldzinsen() {
		return abzugSchuldzinsen;
	}

	public void setAbzugSchuldzinsen(@Nullable BigDecimal abzugSchuldzinsen) {
		this.abzugSchuldzinsen = abzugSchuldzinsen;
	}

	@Nullable
	public BigDecimal getAbzugUnterhaltsbeitragEhepartner() {
		return abzugUnterhaltsbeitragEhepartner;
	}

	public void setAbzugUnterhaltsbeitragEhepartner(@Nullable BigDecimal abzugUnterhaltsbeitragEhepartner) {
		this.abzugUnterhaltsbeitragEhepartner = abzugUnterhaltsbeitragEhepartner;
	}

	@Nullable
	public BigDecimal getAbzugUnterhaltsbeitragKinder() {
		return abzugUnterhaltsbeitragKinder;
	}

	public void setAbzugUnterhaltsbeitragKinder(@Nullable BigDecimal abzugUnterhaltsbeitragKinder) {
		this.abzugUnterhaltsbeitragKinder = abzugUnterhaltsbeitragKinder;
	}

	@Nullable
	public BigDecimal getAbzugRentenleistungen() {
		return abzugRentenleistungen;
	}

	public void setAbzugRentenleistungen(@Nullable BigDecimal abzugRentenleistungen) {
		this.abzugRentenleistungen = abzugRentenleistungen;
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
	public BigDecimal getAbzugFreiweiligeZuwendungPartien() {
		return abzugFreiweiligeZuwendungPartien;
	}

	public void setAbzugFreiweiligeZuwendungPartien(@Nullable BigDecimal abzugFreiweiligeZuwendungPartien) {
		this.abzugFreiweiligeZuwendungPartien = abzugFreiweiligeZuwendungPartien;
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
	public BigDecimal getAbzugKinderAuswaertigerAufenthalt() {
		return abzugKinderAuswaertigerAufenthalt;
	}

	public void setAbzugKinderAuswaertigerAufenthalt(@Nullable BigDecimal abzugKinderAuswaertigerAufenthalt) {
		this.abzugKinderAuswaertigerAufenthalt = abzugKinderAuswaertigerAufenthalt;
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

	@Nullable
	@Column(nullable = true)
	private BigDecimal abzugSteuerfreierBetragKinder;

	@Override
	public boolean isSame(AbstractEntity other) {
		//noinspection ObjectEquality
		if (this == other) {
			return true;
		}
		if (other == null || !getClass().equals(other.getClass())) {
			return false;
		}

		if (!(other instanceof FinanzielleSituationSelbstdeklaration)) {
			return false;
		}

		final FinanzielleSituationSelbstdeklaration otherSelbstdeklaration = (FinanzielleSituationSelbstdeklaration) other;
		return MathUtil.isSame(getEinkuftErwerb(), otherSelbstdeklaration.getEinkuftErwerb()) &&
			MathUtil.isSame(getEinkuftVersicherung(), otherSelbstdeklaration.getEinkuftVersicherung()) &&
			MathUtil.isSame(getEinkuftAusgleichskassen(), otherSelbstdeklaration.getEinkuftAusgleichskassen()) &&
			MathUtil.isSame(getEinkuftWertschriften(), otherSelbstdeklaration.getEinkuftWertschriften()) &&
			MathUtil.isSame(getEinkuftUnterhaltsbeitragSteuerpflichtige(), otherSelbstdeklaration.getEinkuftUnterhaltsbeitragSteuerpflichtige()) &&
			MathUtil.isSame(getEinkuftUnterhaltsbeitragKinder(), otherSelbstdeklaration.getEinkuftUnterhaltsbeitragKinder()) &&
			MathUtil.isSame(getEinkuftUeberige(), otherSelbstdeklaration.getEinkuftUeberige()) &&
			MathUtil.isSame(getEinkunftLiegenschaften(), otherSelbstdeklaration.getEinkunftLiegenschaften()) &&
			MathUtil.isSame(getAbzugBerufsauslagen(), otherSelbstdeklaration.getAbzugBerufsauslagen()) &&
			MathUtil.isSame(getAbzugSchuldzinsen(), otherSelbstdeklaration.getAbzugSchuldzinsen()) &&
			MathUtil.isSame(getAbzugUnterhaltsbeitragEhepartner(), otherSelbstdeklaration.getAbzugUnterhaltsbeitragEhepartner()) &&
			MathUtil.isSame(getAbzugUnterhaltsbeitragKinder(), otherSelbstdeklaration.getAbzugUnterhaltsbeitragKinder()) &&
			MathUtil.isSame(getAbzugRentenleistungen(), otherSelbstdeklaration.getAbzugRentenleistungen()) &&
			MathUtil.isSame(getAbzugSaeule3A(), otherSelbstdeklaration.getAbzugSaeule3A()) &&
			MathUtil.isSame(getAbzugVersicherungspraemien(), otherSelbstdeklaration.getAbzugVersicherungspraemien()) &&
			MathUtil.isSame(getAbzugKrankheitsUnfallKosten(), otherSelbstdeklaration.getAbzugKrankheitsUnfallKosten()) &&
			MathUtil.isSame(getAbzugFreiweiligeZuwendungPartien(), otherSelbstdeklaration.getAbzugFreiweiligeZuwendungPartien()) &&
			MathUtil.isSame(getAbzugKinderVorschule(), otherSelbstdeklaration.getAbzugKinderVorschule()) &&
			MathUtil.isSame(getAbzugKinderSchule(), otherSelbstdeklaration.getAbzugKinderSchule()) &&
			MathUtil.isSame(getAbzugKinderAuswaertigerAufenthalt(), otherSelbstdeklaration.getAbzugKinderAuswaertigerAufenthalt()) &&
			MathUtil.isSame(getAbzugEigenbetreuung(), otherSelbstdeklaration.getAbzugEigenbetreuung()) &&
			MathUtil.isSame(getAbzugFremdbetreuung(), otherSelbstdeklaration.getAbzugFremdbetreuung()) &&
			MathUtil.isSame(getAbzugErwerbsunfaehigePersonen(), otherSelbstdeklaration.getAbzugErwerbsunfaehigePersonen()) &&
			MathUtil.isSame(getAbzugSteuerfreierBetragErwachsene(), otherSelbstdeklaration.getAbzugSteuerfreierBetragErwachsene()) &&
			MathUtil.isSame(getAbzugSteuerfreierBetragKinder(), otherSelbstdeklaration.getAbzugSteuerfreierBetragKinder()) &&
			MathUtil.isSame(getVermoegen(), otherSelbstdeklaration.getVermoegen());
	}
}
