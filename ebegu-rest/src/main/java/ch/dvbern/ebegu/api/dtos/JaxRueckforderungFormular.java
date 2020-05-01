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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package ch.dvbern.ebegu.api.dtos;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

import ch.dvbern.ebegu.entities.Institution;
import ch.dvbern.ebegu.entities.RueckforderungMitteilung;
import ch.dvbern.ebegu.enums.RueckforderungStatus;

@XmlAccessorType(XmlAccessType.FIELD)
public class JaxRueckforderungFormular extends JaxAbstractDTO {

	private static final long serialVersionUID = -7620977294104032869L;

	@Nonnull
	private Institution institution;

	@Nonnull
	private Set<RueckforderungMitteilung> rueckforderungMitteilungen = new HashSet<>();

	@Nonnull
	private RueckforderungStatus status;

	private BigDecimal nichtAngeboteneBetreuungStundenKantonStufe1;

	private BigDecimal nichtAngeboteneBetreuungStundenInstitutionStufe1;

	private BigDecimal nichtAngeboteneBetreuungStundenKantonStufe2;

	private BigDecimal nichtAngeboteneBetreuungStundenInstitutionStufe2;

	private BigDecimal nichtAngeboteneBetreuungTageKantonStufe1;

	private BigDecimal nichtAngeboteneBetreuungTageInstitutionStufe1;

	private BigDecimal nichtAngeboteneBetreuungTageKantonStufe2;

	private BigDecimal nichtAngeboteneBetreuungTageInstitutionStufe2;

	private BigDecimal andereEntfalleneErtraegeKantonStufe1;

	private BigDecimal andereEntfalleneErtraegeInstitutionStufe1;

	private BigDecimal andereEntfalleneErtraegeKantonStufe2;

	private BigDecimal andereEntfalleneErtraegeInstitutionStufe2;

	@Nonnull
	public Institution getInstitution() {
		return institution;
	}

	public void setInstitution(@Nonnull Institution institution) {
		this.institution = institution;
	}

	@Nonnull
	public Set<RueckforderungMitteilung> getRueckforderungMitteilungen() {
		return rueckforderungMitteilungen;
	}

	public void setRueckforderungMitteilungen(@Nonnull Set<RueckforderungMitteilung> rueckforderungMitteilungen) {
		this.rueckforderungMitteilungen = rueckforderungMitteilungen;
	}

	@Nonnull
	public RueckforderungStatus getStatus() {
		return status;
	}

	public void setStatus(@Nonnull RueckforderungStatus status) {
		this.status = status;
	}

	@Nullable
	public BigDecimal getNichtAngeboteneBetreuungStundenKantonStufe1() {
		return nichtAngeboteneBetreuungStundenKantonStufe1;
	}

	public void setNichtAngeboteneBetreuungStundenKantonStufe1(@Nullable BigDecimal nichtAngeboteneBetreuungStundenKantonStufe1) {
		this.nichtAngeboteneBetreuungStundenKantonStufe1 = nichtAngeboteneBetreuungStundenKantonStufe1;
	}

	@Nullable
	public BigDecimal getNichtAngeboteneBetreuungStundenInstitutionStufe1() {
		return nichtAngeboteneBetreuungStundenInstitutionStufe1;
	}

	public void setNichtAngeboteneBetreuungStundenInstitutionStufe1(@Nullable BigDecimal nichtAngeboteneBetreuungStundenInstitutionStufe1) {
		this.nichtAngeboteneBetreuungStundenInstitutionStufe1 = nichtAngeboteneBetreuungStundenInstitutionStufe1;
	}

	@Nullable
	public BigDecimal getNichtAngeboteneBetreuungStundenKantonStufe2() {
		return nichtAngeboteneBetreuungStundenKantonStufe2;
	}

	public void setNichtAngeboteneBetreuungStundenKantonStufe2(@Nullable BigDecimal nichtAngeboteneBetreuungStundenKantonStufe2) {
		this.nichtAngeboteneBetreuungStundenKantonStufe2 = nichtAngeboteneBetreuungStundenKantonStufe2;
	}

	@Nullable
	public BigDecimal getNichtAngeboteneBetreuungStundenInstitutionStufe2() {
		return nichtAngeboteneBetreuungStundenInstitutionStufe2;
	}

	public void setNichtAngeboteneBetreuungStundenInstitutionStufe2(@Nullable BigDecimal nichtAngeboteneBetreuungStundenInstitutionStufe2) {
		this.nichtAngeboteneBetreuungStundenInstitutionStufe2 = nichtAngeboteneBetreuungStundenInstitutionStufe2;
	}

	@Nullable
	public BigDecimal getNichtAngeboteneBetreuungTageKantonStufe1() {
		return nichtAngeboteneBetreuungTageKantonStufe1;
	}

	public void setNichtAngeboteneBetreuungTageKantonStufe1(@Nullable BigDecimal nichtAngeboteneBetreuungTageKantonStufe1) {
		this.nichtAngeboteneBetreuungTageKantonStufe1 = nichtAngeboteneBetreuungTageKantonStufe1;
	}

	@Nullable
	public BigDecimal getNichtAngeboteneBetreuungTageInstitutionStufe1() {
		return nichtAngeboteneBetreuungTageInstitutionStufe1;
	}

	public void setNichtAngeboteneBetreuungTageInstitutionStufe1(@Nullable BigDecimal nichtAngeboteneBetreuungTageInstitutionStufe1) {
		this.nichtAngeboteneBetreuungTageInstitutionStufe1 = nichtAngeboteneBetreuungTageInstitutionStufe1;
	}

	@Nullable
	public BigDecimal getNichtAngeboteneBetreuungTageKantonStufe2() {
		return nichtAngeboteneBetreuungTageKantonStufe2;
	}

	public void setNichtAngeboteneBetreuungTageKantonStufe2(@Nullable BigDecimal nichtAngeboteneBetreuungTageKantonStufe2) {
		this.nichtAngeboteneBetreuungTageKantonStufe2 = nichtAngeboteneBetreuungTageKantonStufe2;
	}

	@Nullable
	public BigDecimal getNichtAngeboteneBetreuungTageInstitutionStufe2() {
		return nichtAngeboteneBetreuungTageInstitutionStufe2;
	}

	public void setNichtAngeboteneBetreuungTageInstitutionStufe2(@Nullable BigDecimal nichtAngeboteneBetreuungTageInstitutionStufe2) {
		this.nichtAngeboteneBetreuungTageInstitutionStufe2 = nichtAngeboteneBetreuungTageInstitutionStufe2;
	}

	@Nullable
	public BigDecimal getAndereEntfalleneErtraegeKantonStufe1() {
		return andereEntfalleneErtraegeKantonStufe1;
	}

	public void setAndereEntfalleneErtraegeKantonStufe1(@Nullable BigDecimal andereEntfalleneErtraegeKantonStufe1) {
		this.andereEntfalleneErtraegeKantonStufe1 = andereEntfalleneErtraegeKantonStufe1;
	}

	@Nullable
	public BigDecimal getAndereEntfalleneErtraegeInstitutionStufe1() {
		return andereEntfalleneErtraegeInstitutionStufe1;
	}

	public void setAndereEntfalleneErtraegeInstitutionStufe1(@Nullable BigDecimal andereEntfalleneErtraegeInstitutionStufe1) {
		this.andereEntfalleneErtraegeInstitutionStufe1 = andereEntfalleneErtraegeInstitutionStufe1;
	}

	@Nullable
	public BigDecimal getAndereEntfalleneErtraegeKantonStufe2() {
		return andereEntfalleneErtraegeKantonStufe2;
	}

	public void setAndereEntfalleneErtraegeKantonStufe2(@Nullable BigDecimal andereEntfalleneErtraegeKantonStufe2) {
		this.andereEntfalleneErtraegeKantonStufe2 = andereEntfalleneErtraegeKantonStufe2;
	}

	@Nullable
	public BigDecimal getAndereEntfalleneErtraegeInstitutionStufe2() {
		return andereEntfalleneErtraegeInstitutionStufe2;
	}

	public void setAndereEntfalleneErtraegeInstitutionStufe2(@Nullable BigDecimal andereEntfalleneErtraegeInstitutionStufe2) {
		this.andereEntfalleneErtraegeInstitutionStufe2 = andereEntfalleneErtraegeInstitutionStufe2;
	}
}
