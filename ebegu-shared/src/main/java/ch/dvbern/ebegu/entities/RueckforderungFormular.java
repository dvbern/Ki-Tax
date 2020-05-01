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

package ch.dvbern.ebegu.entities;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ForeignKey;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.validation.constraints.NotNull;

import ch.dvbern.ebegu.enums.RueckforderungStatus;
import org.hibernate.envers.Audited;


@Entity
@Audited
public class RueckforderungFormular extends AbstractEntity {

	private static final long serialVersionUID = 2265499143921501849L;

	@NotNull
	@ManyToOne(optional = false)
	@JoinColumn(foreignKey = @ForeignKey(name = "FK_rueckforderungFormular_institution_id"), nullable = false)
	private Institution institution;

	@Nonnull
	@ManyToMany
	@JoinTable(
		joinColumns = @JoinColumn(name = "rueckforderung_formular_id", nullable = false),
		inverseJoinColumns = @JoinColumn(name = "rueckforderung_mitteilung_id", nullable = false),
		foreignKey = @ForeignKey(name = "FK_rueckforderung_formular_mitteilung_id"),
		inverseForeignKey = @ForeignKey(name = "FK_rueckforderung_formular_rueckforderung_mitteilung_formular_id"),
		indexes = {
			@Index(name = "FK_rueckforderung_formular_mitteilung_id", columnList = "rueckforderung_formular_id"),
			@Index(name = "IX_institution_external_clients_external_client_id", columnList = "rueckforderung_mitteilung_id"),
		}
	)
	private @NotNull
	Set<RueckforderungMitteilung> rueckforderungMitteilungen = new HashSet<>();

	@NotNull
	@Column(nullable = false)
	@Nonnull
	private RueckforderungStatus status;

	@Column(nullable = true)
	private BigDecimal nichtAngeboteneBetreuungStundenKantonStufe1;

	@Column(nullable = true)
	private BigDecimal nichtAngeboteneBetreuungStundenInstitutionStufe1;

	@Column(nullable = true)
	private BigDecimal nichtAngeboteneBetreuungStundenKantonStufe2;

	@Column(nullable = true)
	private BigDecimal nichtAngeboteneBetreuungStundenInstitutionStufe2;

	@Column(nullable = true)
	private BigDecimal nichtAngeboteneBetreuungTageKantonStufe1;

	@Column(nullable = true)
	private BigDecimal nichtAngeboteneBetreuungTageInstitutionStufe1;

	@Column(nullable = true)
	private BigDecimal nichtAngeboteneBetreuungTageKantonStufe2;

	@Column(nullable = true)
	private BigDecimal nichtAngeboteneBetreuungTageInstitutionStufe2;

	@Column(nullable = true)
	private BigDecimal andereEntfalleneErtraegeKantonStufe1;

	@Column(nullable = true)
	private BigDecimal andereEntfalleneErtraegeInstitutionStufe1;

	@Column(nullable = true)
	private BigDecimal andereEntfalleneErtraegeKantonStufe2;

	@Column(nullable = true)
	private BigDecimal andereEntfalleneErtraegeInstitutionStufe2;

	public Institution getInstitution() {
		return institution;
	}

	public void setInstitution(Institution institution) {
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

	public BigDecimal getNichtAngeboteneBetreuungStundenKantonStufe1() {
		return nichtAngeboteneBetreuungStundenKantonStufe1;
	}

	public void setNichtAngeboteneBetreuungStundenKantonStufe1(BigDecimal nichtAngeboteneBetreuungStundenKantonStufe1) {
		this.nichtAngeboteneBetreuungStundenKantonStufe1 = nichtAngeboteneBetreuungStundenKantonStufe1;
	}

	public BigDecimal getNichtAngeboteneBetreuungStundenInstitutionStufe1() {
		return nichtAngeboteneBetreuungStundenInstitutionStufe1;
	}

	public void setNichtAngeboteneBetreuungStundenInstitutionStufe1(BigDecimal nichtAngeboteneBetreuungStundenInstitutionStufe1) {
		this.nichtAngeboteneBetreuungStundenInstitutionStufe1 = nichtAngeboteneBetreuungStundenInstitutionStufe1;
	}

	public BigDecimal getNichtAngeboteneBetreuungStundenKantonStufe2() {
		return nichtAngeboteneBetreuungStundenKantonStufe2;
	}

	public void setNichtAngeboteneBetreuungStundenKantonStufe2(BigDecimal nichtAngeboteneBetreuungStundenKantonStufe2) {
		this.nichtAngeboteneBetreuungStundenKantonStufe2 = nichtAngeboteneBetreuungStundenKantonStufe2;
	}

	public BigDecimal getNichtAngeboteneBetreuungStundenInstitutionStufe2() {
		return nichtAngeboteneBetreuungStundenInstitutionStufe2;
	}

	public void setNichtAngeboteneBetreuungStundenInstitutionStufe2(BigDecimal nichtAngeboteneBetreuungStundenInstitutionStufe2) {
		this.nichtAngeboteneBetreuungStundenInstitutionStufe2 = nichtAngeboteneBetreuungStundenInstitutionStufe2;
	}

	public BigDecimal getNichtAngeboteneBetreuungTageKantonStufe1() {
		return nichtAngeboteneBetreuungTageKantonStufe1;
	}

	public void setNichtAngeboteneBetreuungTageKantonStufe1(BigDecimal nichtAngeboteneBetreuungTageKantonStufe1) {
		this.nichtAngeboteneBetreuungTageKantonStufe1 = nichtAngeboteneBetreuungTageKantonStufe1;
	}

	public BigDecimal getNichtAngeboteneBetreuungTageInstitutionStufe1() {
		return nichtAngeboteneBetreuungTageInstitutionStufe1;
	}

	public void setNichtAngeboteneBetreuungTageInstitutionStufe1(BigDecimal nichtAngeboteneBetreuungTageInstitutionStufe1) {
		this.nichtAngeboteneBetreuungTageInstitutionStufe1 = nichtAngeboteneBetreuungTageInstitutionStufe1;
	}

	public BigDecimal getNichtAngeboteneBetreuungTageKantonStufe2() {
		return nichtAngeboteneBetreuungTageKantonStufe2;
	}

	public void setNichtAngeboteneBetreuungTageKantonStufe2(BigDecimal nichtAngeboteneBetreuungTageKantonStufe2) {
		this.nichtAngeboteneBetreuungTageKantonStufe2 = nichtAngeboteneBetreuungTageKantonStufe2;
	}

	public BigDecimal getNichtAngeboteneBetreuungTageInstitutionStufe2() {
		return nichtAngeboteneBetreuungTageInstitutionStufe2;
	}

	public void setNichtAngeboteneBetreuungTageInstitutionStufe2(BigDecimal nichtAngeboteneBetreuungTageInstitutionStufe2) {
		this.nichtAngeboteneBetreuungTageInstitutionStufe2 = nichtAngeboteneBetreuungTageInstitutionStufe2;
	}

	public BigDecimal getAndereEntfalleneErtraegeKantonStufe1() {
		return andereEntfalleneErtraegeKantonStufe1;
	}

	public void setAndereEntfalleneErtraegeKantonStufe1(BigDecimal andereEntfalleneErtraegeKantonStufe1) {
		this.andereEntfalleneErtraegeKantonStufe1 = andereEntfalleneErtraegeKantonStufe1;
	}

	public BigDecimal getAndereEntfalleneErtraegeInstitutionStufe1() {
		return andereEntfalleneErtraegeInstitutionStufe1;
	}

	public void setAndereEntfalleneErtraegeInstitutionStufe1(BigDecimal andereEntfalleneErtraegeInstitutionStufe1) {
		this.andereEntfalleneErtraegeInstitutionStufe1 = andereEntfalleneErtraegeInstitutionStufe1;
	}

	public BigDecimal getAndereEntfalleneErtraegeKantonStufe2() {
		return andereEntfalleneErtraegeKantonStufe2;
	}

	public void setAndereEntfalleneErtraegeKantonStufe2(BigDecimal andereEntfalleneErtraegeKantonStufe2) {
		this.andereEntfalleneErtraegeKantonStufe2 = andereEntfalleneErtraegeKantonStufe2;
	}

	public BigDecimal getAndereEntfalleneErtraegeInstitutionStufe2() {
		return andereEntfalleneErtraegeInstitutionStufe2;
	}

	public void setAndereEntfalleneErtraegeInstitutionStufe2(BigDecimal andereEntfalleneErtraegeInstitutionStufe2) {
		this.andereEntfalleneErtraegeInstitutionStufe2 = andereEntfalleneErtraegeInstitutionStufe2;
	}

	@Override
	public boolean isSame(AbstractEntity other) {
		return false;
	}
}
