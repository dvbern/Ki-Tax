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

package ch.dvbern.ebegu.api.dtos.finanziellesituation;

import java.math.BigDecimal;

import javax.annotation.Nullable;

import ch.dvbern.ebegu.api.dtos.JaxAbstractDTO;

public class JaxFinSitZusatzangabenAppenzell extends JaxAbstractDTO {

	private static final long serialVersionUID = -6899661072522033574L;

	@Nullable
	private BigDecimal saeule3a;

	@Nullable
	private BigDecimal saeule3aNichtBvg;

	@Nullable
	private BigDecimal beruflicheVorsorge;

	@Nullable
	private BigDecimal liegenschaftsaufwand;

	@Nullable
	private BigDecimal einkuenfteBgsa;

	@Nullable
	private BigDecimal vorjahresverluste;

	@Nullable
	private BigDecimal politischeParteiSpende;

	@Nullable
	private BigDecimal leistungAnJuristischePersonen;
	@Nullable
	private BigDecimal steuerbaresEinkommen;

	@Nullable
	private BigDecimal steuerbaresVermoegen;

	@Nullable
	private JaxFinSitZusatzangabenAppenzell zusatzangabenPartner;

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

	public void setSaeule3aNichtBvg(@Nullable BigDecimal saeule3aNichtBvg) {
		this.saeule3aNichtBvg = saeule3aNichtBvg;
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

	public void setEinkuenfteBgsa(@Nullable BigDecimal einkuenfteBgsa) {
		this.einkuenfteBgsa = einkuenfteBgsa;
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

	@Nullable
	public JaxFinSitZusatzangabenAppenzell getZusatzangabenPartner() {
		return zusatzangabenPartner;
	}

	public void setZusatzangabenPartner(@Nullable JaxFinSitZusatzangabenAppenzell zusatzangabenPartner) {
		this.zusatzangabenPartner = zusatzangabenPartner;
	}

	@Nullable
	public BigDecimal getSteuerbaresEinkommen() {
		return steuerbaresEinkommen;
	}

	public void setSteuerbaresEinkommen(@Nullable BigDecimal steuerbaresEinkommen) {
		this.steuerbaresEinkommen = steuerbaresEinkommen;
	}

	@Nullable
	public BigDecimal getSteuerbaresVermoegen() {
		return steuerbaresVermoegen;
	}

	public void setSteuerbaresVermoegen(@Nullable BigDecimal steuerbaresVermoegen) {
		this.steuerbaresVermoegen = steuerbaresVermoegen;
	}
}
