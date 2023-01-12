/*
 * Copyright (C) 2023 DV Bern AG, Switzerland
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
import java.util.Objects;

import javax.annotation.Nullable;
import javax.persistence.Column;
import javax.persistence.Entity;

import ch.dvbern.ebegu.enums.AntragCopyType;
import org.hibernate.envers.Audited;

@Audited
@Entity
public class FinanzielleVerhaeltnisse extends AbstractMutableEntity {

	private static final long serialVersionUID = -8876223011487726148L;

	@Nullable
	@Column(nullable = true)
	private BigDecimal saeule3a;

	@Nullable
	@Column(nullable = true)
	private BigDecimal saeule3aNichtBvg;

	@Nullable
	@Column(nullable = true)
	private BigDecimal beruflicheVorsorge;

	@Nullable
	@Column(nullable = true)
	private BigDecimal liegenschaftsaufwand;

	@Nullable
	@Column(nullable = true)
	private BigDecimal einkuenfteBgsa;

	@Nullable
	@Column(nullable = true)
	private BigDecimal vorjahresverluste;

	@Nullable
	@Column(nullable = true)
	private BigDecimal politischeParteiSpende;

	@Nullable
	@Column(nullable = true)
	private BigDecimal leistungAnJuristischePersonen;

	public FinanzielleVerhaeltnisse() {
	}

	@Nullable
	public BigDecimal getSaeule3a() {
		return saeule3a;
	}

	public void setSaeule3a(@Nullable BigDecimal saeule3a) {
		this.saeule3a = saeule3a;
	}

	@Nullable
	public BigDecimal getSaeule3aNichtBvg() {
		return saeule3aNichtBvg;
	}

	public void setSaeule3aNichtBvg(@Nullable BigDecimal saeule3aNichtBVG) {
		this.saeule3aNichtBvg = saeule3aNichtBVG;
	}

	@Nullable
	public BigDecimal getBeruflicheVorsorge() {
		return beruflicheVorsorge;
	}

	public void setBeruflicheVorsorge(@Nullable BigDecimal beruflicheVorsorge) {
		this.beruflicheVorsorge = beruflicheVorsorge;
	}

	@Nullable
	public BigDecimal getLiegenschaftsaufwand() {
		return liegenschaftsaufwand;
	}

	public void setLiegenschaftsaufwand(@Nullable BigDecimal liegenschaftsaufwand) {
		this.liegenschaftsaufwand = liegenschaftsaufwand;
	}

	@Nullable
	public BigDecimal getEinkuenfteBgsa() {
		return einkuenfteBgsa;
	}

	public void setEinkuenfteBgsa(@Nullable BigDecimal einkuenfteBGSA) {
		this.einkuenfteBgsa = einkuenfteBGSA;
	}

	@Nullable
	public BigDecimal getVorjahresverluste() {
		return vorjahresverluste;
	}

	public void setVorjahresverluste(@Nullable BigDecimal vorjahresverluste) {
		this.vorjahresverluste = vorjahresverluste;
	}

	@Nullable
	public BigDecimal getPolitischeParteiSpende() {
		return politischeParteiSpende;
	}

	public void setPolitischeParteiSpende(@Nullable BigDecimal politischeParteiSpende) {
		this.politischeParteiSpende = politischeParteiSpende;
	}

	@Nullable
	public BigDecimal getLeistungAnJuristischePersonen() {
		return leistungAnJuristischePersonen;
	}

	public void setLeistungAnJuristischePersonen(@Nullable BigDecimal leistungAnJuristischePersonen) {
		this.leistungAnJuristischePersonen = leistungAnJuristischePersonen;
	}

	@Override
	public boolean isSame(AbstractEntity other) {
		return this.equals(other);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		if (!super.equals(o)) {
			return false;
		}
		FinanzielleVerhaeltnisse that = (FinanzielleVerhaeltnisse) o;
		return Objects.equals(getSaeule3a(), that.getSaeule3a())
			&& Objects.equals(
			getSaeule3aNichtBvg(),
			that.getSaeule3aNichtBvg())
			&& Objects.equals(
			getBeruflicheVorsorge(),
			that.getBeruflicheVorsorge())
			&& Objects.equals(
			getLiegenschaftsaufwand(),
			that.getLiegenschaftsaufwand())
			&& Objects.equals(getEinkuenfteBgsa(), that.getEinkuenfteBgsa())
			&& Objects.equals(getVorjahresverluste(), that.getVorjahresverluste())
			&& Objects.equals(getPolitischeParteiSpende(), that.getPolitischeParteiSpende())
			&& Objects.equals(getLeistungAnJuristischePersonen(), that.getLeistungAnJuristischePersonen());
	}

	@Override
	public int hashCode() {
		return Objects.hash(
			super.hashCode(),
			getSaeule3a(),
			getSaeule3aNichtBvg(),
			getBeruflicheVorsorge(),
			getLiegenschaftsaufwand(),
			getEinkuenfteBgsa(),
			getVorjahresverluste(),
			getPolitischeParteiSpende(),
			getLeistungAnJuristischePersonen());
	}

	public FinanzielleVerhaeltnisse copyFinanzielleVerhaeltnisse(
		FinanzielleVerhaeltnisse target,
		AntragCopyType copyType) {
		switch (copyType) {
		case MUTATION:
		case MUTATION_NEUES_DOSSIER:
			target.setSaeule3a(this.getSaeule3a());
			target.setSaeule3aNichtBvg(this.getSaeule3aNichtBvg());
			target.setBeruflicheVorsorge(this.getBeruflicheVorsorge());
			target.setLiegenschaftsaufwand(this.getLiegenschaftsaufwand());
			target.setEinkuenfteBgsa(this.getEinkuenfteBgsa());
			target.setVorjahresverluste(this.getVorjahresverluste());
			target.setPolitischeParteiSpende(this.getPolitischeParteiSpende());
			target.setLeistungAnJuristischePersonen(this.getLeistungAnJuristischePersonen());
			break;
		default:
			break;
		}
		return target;
	}
}
