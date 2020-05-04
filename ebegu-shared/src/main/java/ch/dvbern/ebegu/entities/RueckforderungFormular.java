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
	private BigDecimal stufe1KantonKostenuebernahmeAnzahlStunden;

	@Column(nullable = true)
	private BigDecimal stufe1InstitutionKostenuebernahmeAnzahlStunden;

	@Column(nullable = true)
	private BigDecimal stufe2KantonKostenuebernahmeAnzahlStunden;

	@Column(nullable = true)
	private BigDecimal stufe2InstitutionKostenuebernahmeAnzahlStunden;

	@Column(nullable = true)
	private BigDecimal stufe1KantonKostenuebernahmeAnzahlTage;

	@Column(nullable = true)
	private BigDecimal stufe1InstitutionKostenuebernahmeAnzahlTage;

	@Column(nullable = true)
	private BigDecimal stufe2KantonKostenuebernahmeAnzahlTage;

	@Column(nullable = true)
	private BigDecimal stufe2InstitutionKostenuebernahmeAnzahlTage;

	@Column(nullable = true)
	private BigDecimal stufe1KantonKostenuebernahmeBetreuung;

	@Column(nullable = true)
	private BigDecimal stufe1InstitutionKostenuebernahmeBetreuung;

	@Column(nullable = true)
	private BigDecimal stufe2KantonKostenuebernahmeBetreuung;

	@Column(nullable = true)
	private BigDecimal stufe2InstitutionKostenuebernahmeBetreuung;

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

	public BigDecimal getStufe1KantonKostenuebernahmeAnzahlStunden() {
		return stufe1KantonKostenuebernahmeAnzahlStunden;
	}

	public void setStufe1KantonKostenuebernahmeAnzahlStunden(BigDecimal nichtAngeboteneBetreuungStundenKantonStufe1) {
		this.stufe1KantonKostenuebernahmeAnzahlStunden = nichtAngeboteneBetreuungStundenKantonStufe1;
	}

	public BigDecimal getStufe1InstitutionKostenuebernahmeAnzahlStunden() {
		return stufe1InstitutionKostenuebernahmeAnzahlStunden;
	}

	public void setStufe1InstitutionKostenuebernahmeAnzahlStunden(BigDecimal nichtAngeboteneBetreuungStundenInstitutionStufe1) {
		this.stufe1InstitutionKostenuebernahmeAnzahlStunden = nichtAngeboteneBetreuungStundenInstitutionStufe1;
	}

	public BigDecimal getStufe2KantonKostenuebernahmeAnzahlStunden() {
		return stufe2KantonKostenuebernahmeAnzahlStunden;
	}

	public void setStufe2KantonKostenuebernahmeAnzahlStunden(BigDecimal nichtAngeboteneBetreuungStundenKantonStufe2) {
		this.stufe2KantonKostenuebernahmeAnzahlStunden = nichtAngeboteneBetreuungStundenKantonStufe2;
	}

	public BigDecimal getStufe2InstitutionKostenuebernahmeAnzahlStunden() {
		return stufe2InstitutionKostenuebernahmeAnzahlStunden;
	}

	public void setStufe2InstitutionKostenuebernahmeAnzahlStunden(BigDecimal nichtAngeboteneBetreuungStundenInstitutionStufe2) {
		this.stufe2InstitutionKostenuebernahmeAnzahlStunden = nichtAngeboteneBetreuungStundenInstitutionStufe2;
	}

	public BigDecimal getStufe1KantonKostenuebernahmeAnzahlTage() {
		return stufe1KantonKostenuebernahmeAnzahlTage;
	}

	public void setStufe1KantonKostenuebernahmeAnzahlTage(BigDecimal nichtAngeboteneBetreuungTageKantonStufe1) {
		this.stufe1KantonKostenuebernahmeAnzahlTage = nichtAngeboteneBetreuungTageKantonStufe1;
	}

	public BigDecimal getStufe1InstitutionKostenuebernahmeAnzahlTage() {
		return stufe1InstitutionKostenuebernahmeAnzahlTage;
	}

	public void setStufe1InstitutionKostenuebernahmeAnzahlTage(BigDecimal nichtAngeboteneBetreuungTageInstitutionStufe1) {
		this.stufe1InstitutionKostenuebernahmeAnzahlTage = nichtAngeboteneBetreuungTageInstitutionStufe1;
	}

	public BigDecimal getStufe2KantonKostenuebernahmeAnzahlTage() {
		return stufe2KantonKostenuebernahmeAnzahlTage;
	}

	public void setStufe2KantonKostenuebernahmeAnzahlTage(BigDecimal nichtAngeboteneBetreuungTageKantonStufe2) {
		this.stufe2KantonKostenuebernahmeAnzahlTage = nichtAngeboteneBetreuungTageKantonStufe2;
	}

	public BigDecimal getStufe2InstitutionKostenuebernahmeAnzahlTage() {
		return stufe2InstitutionKostenuebernahmeAnzahlTage;
	}

	public void setStufe2InstitutionKostenuebernahmeAnzahlTage(BigDecimal nichtAngeboteneBetreuungTageInstitutionStufe2) {
		this.stufe2InstitutionKostenuebernahmeAnzahlTage = nichtAngeboteneBetreuungTageInstitutionStufe2;
	}

	public BigDecimal getStufe1KantonKostenuebernahmeBetreuung() {
		return stufe1KantonKostenuebernahmeBetreuung;
	}

	public void setStufe1KantonKostenuebernahmeBetreuung(BigDecimal andereEntfalleneErtraegeKantonStufe1) {
		this.stufe1KantonKostenuebernahmeBetreuung = andereEntfalleneErtraegeKantonStufe1;
	}

	public BigDecimal getStufe1InstitutionKostenuebernahmeBetreuung() {
		return stufe1InstitutionKostenuebernahmeBetreuung;
	}

	public void setStufe1InstitutionKostenuebernahmeBetreuung(BigDecimal andereEntfalleneErtraegeInstitutionStufe1) {
		this.stufe1InstitutionKostenuebernahmeBetreuung = andereEntfalleneErtraegeInstitutionStufe1;
	}

	public BigDecimal getStufe2KantonKostenuebernahmeBetreuung() {
		return stufe2KantonKostenuebernahmeBetreuung;
	}

	public void setStufe2KantonKostenuebernahmeBetreuung(BigDecimal andereEntfalleneErtraegeKantonStufe2) {
		this.stufe2KantonKostenuebernahmeBetreuung = andereEntfalleneErtraegeKantonStufe2;
	}

	public BigDecimal getStufe2InstitutionKostenuebernahmeBetreuung() {
		return stufe2InstitutionKostenuebernahmeBetreuung;
	}

	public void setStufe2InstitutionKostenuebernahmeBetreuung(BigDecimal andereEntfalleneErtraegeInstitutionStufe2) {
		this.stufe2InstitutionKostenuebernahmeBetreuung = andereEntfalleneErtraegeInstitutionStufe2;
	}

	@Override
	public boolean isSame(AbstractEntity other) {
		//noinspection ObjectEquality
		if (this == other) {
			return true;
		}
		if (other == null || !getClass().equals(other.getClass())) {
			return false;
		}
		if (!(other instanceof RueckforderungFormular)) {
			return false;
		}
		final RueckforderungFormular otherRueckforderungFormular = (RueckforderungFormular) other;
		return this.status.equals(otherRueckforderungFormular.getStatus()) &&
			this.stufe1KantonKostenuebernahmeAnzahlStunden.equals(otherRueckforderungFormular.getStufe1KantonKostenuebernahmeAnzahlStunden()) &&
			this.stufe1InstitutionKostenuebernahmeAnzahlStunden.equals(otherRueckforderungFormular.getStufe1InstitutionKostenuebernahmeAnzahlStunden()) &&
			this.stufe2KantonKostenuebernahmeAnzahlStunden.equals(otherRueckforderungFormular.getStufe2KantonKostenuebernahmeAnzahlStunden()) &&
			this.stufe2InstitutionKostenuebernahmeAnzahlStunden.equals(otherRueckforderungFormular.getStufe2InstitutionKostenuebernahmeAnzahlStunden()) &&
			this.stufe1KantonKostenuebernahmeAnzahlTage.equals(otherRueckforderungFormular.getStufe1KantonKostenuebernahmeAnzahlTage()) &&
			this.stufe1InstitutionKostenuebernahmeAnzahlTage.equals(otherRueckforderungFormular.getStufe1InstitutionKostenuebernahmeAnzahlTage()) &&
			this.stufe2KantonKostenuebernahmeAnzahlTage.equals(otherRueckforderungFormular.getStufe2KantonKostenuebernahmeAnzahlTage()) &&
			this.stufe2InstitutionKostenuebernahmeAnzahlTage.equals(otherRueckforderungFormular.getStufe2InstitutionKostenuebernahmeAnzahlTage()) &&
			this.stufe1KantonKostenuebernahmeBetreuung.equals(otherRueckforderungFormular.getStufe1KantonKostenuebernahmeBetreuung()) &&
			this.stufe1InstitutionKostenuebernahmeBetreuung.equals(otherRueckforderungFormular.getStufe1InstitutionKostenuebernahmeBetreuung()) &&
			this.stufe2KantonKostenuebernahmeBetreuung.equals(otherRueckforderungFormular.getStufe2KantonKostenuebernahmeBetreuung()) &&
			this.stufe2InstitutionKostenuebernahmeBetreuung.equals(otherRueckforderungFormular.getStufe2InstitutionKostenuebernahmeBetreuung());
	}
}
