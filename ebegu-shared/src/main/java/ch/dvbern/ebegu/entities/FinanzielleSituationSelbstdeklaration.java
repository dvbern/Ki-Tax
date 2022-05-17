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

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.persistence.Column;
import javax.persistence.Entity;

import ch.dvbern.ebegu.enums.AntragCopyType;
import ch.dvbern.ebegu.util.MathUtil;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.hibernate.envers.Audited;

@Audited
@Entity
public class FinanzielleSituationSelbstdeklaration extends AbstractMutableEntity {

	private static final long serialVersionUID = -731322115720756445L;

	@Nullable
	@Column(nullable = true)
	private BigDecimal einkunftErwerb;

	@Nullable
	@Column(nullable = true)
	private BigDecimal einkunftVersicherung;

	@Nullable
	@Column(nullable = true)
	private BigDecimal einkunftAusgleichskassen;

	@Nullable
	@Column(nullable = true)
	private BigDecimal einkunftWertschriften;

	@Nullable
	@Column(nullable = true)
	private BigDecimal einkunftUnterhaltsbeitragSteuerpflichtige;

	@Nullable
	@Column(nullable = true)
	private BigDecimal einkunftUnterhaltsbeitragKinder;

	@Nullable
	@Column(nullable = true)
	private BigDecimal einkunftUeberige;

	@Nullable
	@Column(nullable = true)
	private BigDecimal einkunftLiegenschaften;

	@Nullable
	@Column(nullable = true)
	private BigDecimal abzugBerufsauslagen;

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
	private BigDecimal sonderabzugErwerbstaetigkeitEhegatten;

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
	@Column(nullable = true)
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
	public BigDecimal getEinkunftAusgleichskassen() {
		return einkunftAusgleichskassen;
	}

	public void setEinkunftAusgleichskassen(@Nullable BigDecimal einkunftAusgleichskassen) {
		this.einkunftAusgleichskassen = einkunftAusgleichskassen;
	}

	@Nullable
	public BigDecimal getEinkunftWertschriften() {
		return einkunftWertschriften;
	}

	public void setEinkunftWertschriften(@Nullable BigDecimal einkunftWertschriften) {
		this.einkunftWertschriften = einkunftWertschriften;
	}

	@Nullable
	public BigDecimal getEinkunftUnterhaltsbeitragSteuerpflichtige() {
		return einkunftUnterhaltsbeitragSteuerpflichtige;
	}

	public void setEinkunftUnterhaltsbeitragSteuerpflichtige(
		@Nullable BigDecimal einkunftUnterhaltsbeitragSteuerpflichtige) {
		this.einkunftUnterhaltsbeitragSteuerpflichtige = einkunftUnterhaltsbeitragSteuerpflichtige;
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

	@Override
	@SuppressWarnings("PMD.CompareObjectsWithEquals")
	@SuppressFBWarnings("BC_UNCONFIRMED_CAST")
	public boolean isSame(AbstractEntity other) {
		//noinspection ObjectEquality
		if (this == other) {
			return true;
		}
		if (other == null || !getClass().equals(other.getClass())) {
			return false;
		}
		final FinanzielleSituationSelbstdeklaration otherSelbstdeklaration = (FinanzielleSituationSelbstdeklaration) other;
		return MathUtil.isSame(getEinkunftErwerb(), otherSelbstdeklaration.getEinkunftErwerb()) &&
			MathUtil.isSame(getEinkunftVersicherung(), otherSelbstdeklaration.getEinkunftVersicherung()) &&
			MathUtil.isSame(getEinkunftAusgleichskassen(), otherSelbstdeklaration.getEinkunftAusgleichskassen()) &&
			MathUtil.isSame(getEinkunftWertschriften(), otherSelbstdeklaration.getEinkunftWertschriften()) &&
			MathUtil.isSame(getEinkunftUnterhaltsbeitragSteuerpflichtige(), otherSelbstdeklaration.getEinkunftUnterhaltsbeitragSteuerpflichtige()) &&
			MathUtil.isSame(getEinkunftUnterhaltsbeitragKinder(), otherSelbstdeklaration.getEinkunftUnterhaltsbeitragKinder()) &&
			MathUtil.isSame(getEinkunftUeberige(), otherSelbstdeklaration.getEinkunftUeberige()) &&
			MathUtil.isSame(getEinkunftLiegenschaften(), otherSelbstdeklaration.getEinkunftLiegenschaften()) &&
			MathUtil.isSame(getAbzugBerufsauslagen(), otherSelbstdeklaration.getAbzugBerufsauslagen()) &&
			MathUtil.isSame(getAbzugUnterhaltsbeitragEhepartner(), otherSelbstdeklaration.getAbzugUnterhaltsbeitragEhepartner()) &&
			MathUtil.isSame(getAbzugUnterhaltsbeitragKinder(), otherSelbstdeklaration.getAbzugUnterhaltsbeitragKinder()) &&
			MathUtil.isSame(getAbzugRentenleistungen(), otherSelbstdeklaration.getAbzugRentenleistungen()) &&
			MathUtil.isSame(getAbzugSaeule3A(), otherSelbstdeklaration.getAbzugSaeule3A()) &&
			MathUtil.isSame(getAbzugVersicherungspraemien(), otherSelbstdeklaration.getAbzugVersicherungspraemien()) &&
			MathUtil.isSame(getAbzugKrankheitsUnfallKosten(), otherSelbstdeklaration.getAbzugKrankheitsUnfallKosten()) &&
			MathUtil.isSame(getSonderabzugErwerbstaetigkeitEhegatten(), otherSelbstdeklaration.getSonderabzugErwerbstaetigkeitEhegatten()) &&
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

	public FinanzielleSituationSelbstdeklaration copySelbsteklaration(
		FinanzielleSituationSelbstdeklaration target,
		AntragCopyType copyType) {
		switch (copyType) {
		case MUTATION:
		case MUTATION_NEUES_DOSSIER:
			target.setEinkunftErwerb(this.getEinkunftErwerb());
			target.setEinkunftVersicherung(this.getEinkunftVersicherung());
			target.setEinkunftAusgleichskassen(this.getEinkunftAusgleichskassen());
			target.setEinkunftWertschriften(this.getEinkunftWertschriften());
			target.setEinkunftUnterhaltsbeitragSteuerpflichtige(this.getEinkunftUnterhaltsbeitragSteuerpflichtige());
			target.setEinkunftUnterhaltsbeitragKinder(this.getEinkunftUnterhaltsbeitragKinder());
			target.setEinkunftUeberige(this.getEinkunftUeberige());
			target.setEinkunftLiegenschaften(this.getEinkunftLiegenschaften());
			target.setAbzugBerufsauslagen(this.getAbzugBerufsauslagen());
			target.setAbzugUnterhaltsbeitragEhepartner(this.getAbzugUnterhaltsbeitragEhepartner());
			target.setAbzugUnterhaltsbeitragKinder(this.getAbzugUnterhaltsbeitragKinder());
			target.setAbzugRentenleistungen(this.getAbzugRentenleistungen());
			target.setAbzugSaeule3A(this.getAbzugSaeule3A());
			target.setAbzugVersicherungspraemien(this.getAbzugVersicherungspraemien());
			target.setAbzugKrankheitsUnfallKosten(this.getAbzugKrankheitsUnfallKosten());
			target.setSonderabzugErwerbstaetigkeitEhegatten(this.getSonderabzugErwerbstaetigkeitEhegatten());
			target.setAbzugKinderVorschule(this.getAbzugKinderVorschule());
			target.setAbzugKinderSchule(this.getAbzugKinderSchule());
			target.setAbzugKinderAuswaertigerAufenthalt(this.getAbzugKinderAuswaertigerAufenthalt());
			target.setAbzugEigenbetreuung(this.getAbzugEigenbetreuung());
			target.setAbzugErwerbsunfaehigePersonen(this.getAbzugErwerbsunfaehigePersonen());
			target.setAbzugFremdbetreuung(this.getAbzugFremdbetreuung());
			target.setAbzugSteuerfreierBetragErwachsene(this.getAbzugSteuerfreierBetragErwachsene());
			target.setAbzugSteuerfreierBetragKinder(this.getAbzugSteuerfreierBetragKinder());
			target.setVermoegen(this.getVermoegen());
			break;
		default:
			break;
		}
		return target;
	}

	@Nonnull
	public BigDecimal calculateEinkuenfte() {
		BigDecimal total = BigDecimal.ZERO;
		return MathUtil.EXACT.addNullSafe(
			total,
			einkunftErwerb,
			einkunftVersicherung,
			einkunftAusgleichskassen,
			einkunftWertschriften,
			einkunftUnterhaltsbeitragSteuerpflichtige,
			einkunftUnterhaltsbeitragKinder,
			einkunftUeberige,
			einkunftLiegenschaften
		);
	}

	@Nonnull
	public BigDecimal calculateAbzuege() {
		BigDecimal total = BigDecimal.ZERO;
		return MathUtil.EXACT.addNullSafe(
			total,
			abzugBerufsauslagen,
			abzugUnterhaltsbeitragEhepartner,
			abzugUnterhaltsbeitragKinder,
			abzugRentenleistungen,
			abzugSaeule3A,
			abzugVersicherungspraemien,
			abzugKrankheitsUnfallKosten,
			sonderabzugErwerbstaetigkeitEhegatten,
			abzugKinderVorschule,
			abzugKinderSchule,
			abzugKinderAuswaertigerAufenthalt,
			abzugEigenbetreuung,
			abzugFremdbetreuung,
			abzugErwerbsunfaehigePersonen
		);
	}

	@Nonnull
	public BigDecimal calculateVermoegen() {
		BigDecimal total = BigDecimal.ZERO;
		total = MathUtil.EXACT.addNullSafe(total, vermoegen);
		total = MathUtil.EXACT.subtractNullSafe(
			total,
			abzugSteuerfreierBetragErwachsene
		);
		return MathUtil.EXACT.subtractNullSafe(
			total,
			abzugSteuerfreierBetragKinder
		);
	}

	public boolean isVollstaendig() {
		return einkunftErwerb != null
			&& einkunftVersicherung != null
			&& einkunftAusgleichskassen != null
			&& einkunftWertschriften != null
			&& einkunftUnterhaltsbeitragSteuerpflichtige != null
			&& einkunftUnterhaltsbeitragKinder != null
			&& einkunftUeberige != null
			&& einkunftLiegenschaften != null
			&& abzugBerufsauslagen != null
			&& abzugUnterhaltsbeitragEhepartner != null
			&& abzugUnterhaltsbeitragKinder != null
			&& abzugRentenleistungen != null
			&& abzugSaeule3A != null
			&& abzugVersicherungspraemien != null
			&& abzugKrankheitsUnfallKosten != null
			&& sonderabzugErwerbstaetigkeitEhegatten != null
			&& abzugKinderVorschule != null
			&& abzugKinderSchule != null
			&& abzugKinderAuswaertigerAufenthalt != null
			&& abzugEigenbetreuung != null
			&& abzugFremdbetreuung != null
			&& abzugErwerbsunfaehigePersonen != null
			&& vermoegen != null
			&& abzugSteuerfreierBetragErwachsene != null
			&& abzugSteuerfreierBetragKinder != null;
	}
}
