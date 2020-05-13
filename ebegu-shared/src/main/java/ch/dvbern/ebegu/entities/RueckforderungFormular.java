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
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.ForeignKey;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.validation.constraints.NotNull;

import ch.dvbern.ebegu.enums.RueckforderungStatus;
import org.hibernate.envers.Audited;
import org.hibernate.envers.RelationTargetAuditMode;

@Entity
@Audited
public class RueckforderungFormular extends AbstractEntity {

	private static final long serialVersionUID = 2265499143921501849L;

	@NotNull
	@ManyToOne(optional = false)
	@JoinColumn(foreignKey = @ForeignKey(name = "FK_rueckforderungFormular_institution_stammdaten_id"), nullable =
		false)
	private InstitutionStammdaten institutionStammdaten;

	@Nonnull
	@ManyToMany
	@JoinTable(
		joinColumns = @JoinColumn(name = "rueckforderung_formular_id", nullable = false),
		inverseJoinColumns = @JoinColumn(name = "rueckforderung_mitteilung_id", nullable = false),
		foreignKey = @ForeignKey(name = "FK_rueckforderung_formular_mitteilung_id"),
		inverseForeignKey = @ForeignKey(name = "FK_rueckforderung_formular_rueckforderung_mitteilung_formular_id"),
		indexes = {
			@Index(name = "FK_rueckforderung_formular_mitteilung_id", columnList = "rueckforderung_formular_id"),
			@Index(name = "IX_institution_external_clients_external_client_id", columnList =
				"rueckforderung_mitteilung_id"),
		}
	)

	@Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
	@NotNull
	private Set<RueckforderungMitteilung> rueckforderungMitteilungen = new HashSet<>();

	@NotNull
	@Column(nullable = false)
	@Nonnull
	@Enumerated(EnumType.STRING)
	private RueckforderungStatus status = RueckforderungStatus.NEU;

	@Column(name = "stufe_1_kanton_kostenuebernahme_anzahl_stunden", nullable = true)
	@Nullable
	private BigDecimal stufe1KantonKostenuebernahmeAnzahlStunden;

	@Column(name = "stufe_1_institution_kostenuebernahme_anzahl_stunden", nullable = true)
	@Nullable
	private BigDecimal stufe1InstitutionKostenuebernahmeAnzahlStunden;

	@Column(name = "stufe_2_kanton_kostenuebernahme_anzahl_stunden", nullable = true)
	@Nullable
	private BigDecimal stufe2KantonKostenuebernahmeAnzahlStunden;

	@Column(name = "stufe_2_institution_kostenuebernahme_anzahl_stunden", nullable = true)
	@Nullable
	private BigDecimal stufe2InstitutionKostenuebernahmeAnzahlStunden;

	@Column(name = "stufe_1_kanton_kostenuebernahme_anzahl_tage", nullable = true)
	@Nullable
	private BigDecimal stufe1KantonKostenuebernahmeAnzahlTage;

	@Column(name = "stufe_1_institution_kostenuebernahme_anzahl_tage", nullable = true)
	@Nullable
	private BigDecimal stufe1InstitutionKostenuebernahmeAnzahlTage;

	@Column(name = "stufe_2_kanton_kostenuebernahme_anzahl_tage", nullable = true)
	@Nullable
	private BigDecimal stufe2KantonKostenuebernahmeAnzahlTage;

	@Column(name = "stufe_2_institution_kostenuebernahme_anzahl_tage", nullable = true)
	@Nullable
	private BigDecimal stufe2InstitutionKostenuebernahmeAnzahlTage;

	@Column(name = "stufe_1_kanton_kostenuebernahme_betreuung", nullable = true)
	@Nullable
	private BigDecimal stufe1KantonKostenuebernahmeBetreuung;

	@Column(name = "stufe_1_institution_kostenuebernahme_betreuung", nullable = true)
	@Nullable
	private BigDecimal stufe1InstitutionKostenuebernahmeBetreuung;

	@Column(name = "stufe_2_kanton_kostenuebernahme_betreuung", nullable = true)
	@Nullable
	private BigDecimal stufe2KantonKostenuebernahmeBetreuung;

	@Column(name = "stufe_2_institution_kostenuebernahme_betreuung", nullable = true)
	@Nullable
	private BigDecimal stufe2InstitutionKostenuebernahmeBetreuung;

	@Column(name = "stufe_1_freigabe_betrag", nullable = true)
	@Nullable
	private BigDecimal stufe1FreigabeBetrag;

	@Column(name = "stufe_1_freigabe_datum", nullable = true)
	@Nullable
	private LocalDateTime stufe1FreigabeDatum;

	@Column(name = "stufe_1_freigabe_ausbezahlt_am", nullable = true)
	@Nullable
	private LocalDateTime stufe1FreigabeAusbezahltAm;

	@Column(name = "stufe_2_verfuegung_betrag", nullable = true)
	@Nullable
	private BigDecimal stufe2VerfuegungBetrag;

	@Column(name = "stufe_2_verfuegung_datum", nullable = true)
	@Nullable
	private LocalDateTime stufe2VerfuegungDatum;

	@Column(name = "stufe_2_verfuegung_ausbezahlt_am", nullable = true)
	@Nullable
	private LocalDateTime stufe2VerfuegungAusbezahltAm;

	@Nonnull
	public Set<RueckforderungMitteilung> getRueckforderungMitteilungen() {
		return rueckforderungMitteilungen;
	}

	public void setRueckforderungMitteilungen(@Nonnull Set<RueckforderungMitteilung> rueckforderungMitteilungen) {
		this.rueckforderungMitteilungen = rueckforderungMitteilungen;
	}

	public void addRueckforderungMitteilung(@Nonnull RueckforderungMitteilung rueckforderungMitteilung) {
		this.rueckforderungMitteilungen.add(rueckforderungMitteilung);
	}

	@Nonnull
	public RueckforderungStatus getStatus() {
		return status;
	}

	public void setStatus(@Nonnull RueckforderungStatus status) {
		this.status = status;
	}

	public InstitutionStammdaten getInstitutionStammdaten() {
		return institutionStammdaten;
	}

	public void setInstitutionStammdaten(InstitutionStammdaten institutionStammdaten) {
		this.institutionStammdaten = institutionStammdaten;
	}

	@Nullable
	public BigDecimal getStufe1KantonKostenuebernahmeAnzahlStunden() {
		return stufe1KantonKostenuebernahmeAnzahlStunden;
	}

	public void setStufe1KantonKostenuebernahmeAnzahlStunden(@Nullable BigDecimal stufe1KantonKostenuebernahmeAnzahlStunden) {
		this.stufe1KantonKostenuebernahmeAnzahlStunden = stufe1KantonKostenuebernahmeAnzahlStunden;
	}

	@Nullable
	public BigDecimal getStufe1InstitutionKostenuebernahmeAnzahlStunden() {
		return stufe1InstitutionKostenuebernahmeAnzahlStunden;
	}

	public void setStufe1InstitutionKostenuebernahmeAnzahlStunden(@Nullable BigDecimal stufe1InstitutionKostenuebernahmeAnzahlStunden) {
		this.stufe1InstitutionKostenuebernahmeAnzahlStunden = stufe1InstitutionKostenuebernahmeAnzahlStunden;
	}

	@Nullable
	public BigDecimal getStufe2KantonKostenuebernahmeAnzahlStunden() {
		return stufe2KantonKostenuebernahmeAnzahlStunden;
	}

	public void setStufe2KantonKostenuebernahmeAnzahlStunden(@Nullable BigDecimal stufe2KantonKostenuebernahmeAnzahlStunden) {
		this.stufe2KantonKostenuebernahmeAnzahlStunden = stufe2KantonKostenuebernahmeAnzahlStunden;
	}

	@Nullable
	public BigDecimal getStufe2InstitutionKostenuebernahmeAnzahlStunden() {
		return stufe2InstitutionKostenuebernahmeAnzahlStunden;
	}

	public void setStufe2InstitutionKostenuebernahmeAnzahlStunden(@Nullable BigDecimal stufe2InstitutionKostenuebernahmeAnzahlStunden) {
		this.stufe2InstitutionKostenuebernahmeAnzahlStunden = stufe2InstitutionKostenuebernahmeAnzahlStunden;
	}

	@Nullable
	public BigDecimal getStufe1KantonKostenuebernahmeAnzahlTage() {
		return stufe1KantonKostenuebernahmeAnzahlTage;
	}

	public void setStufe1KantonKostenuebernahmeAnzahlTage(@Nullable BigDecimal stufe1KantonKostenuebernahmeAnzahlTage) {
		this.stufe1KantonKostenuebernahmeAnzahlTage = stufe1KantonKostenuebernahmeAnzahlTage;
	}

	@Nullable
	public BigDecimal getStufe1InstitutionKostenuebernahmeAnzahlTage() {
		return stufe1InstitutionKostenuebernahmeAnzahlTage;
	}

	public void setStufe1InstitutionKostenuebernahmeAnzahlTage(@Nullable BigDecimal stufe1InstitutionKostenuebernahmeAnzahlTage) {
		this.stufe1InstitutionKostenuebernahmeAnzahlTage = stufe1InstitutionKostenuebernahmeAnzahlTage;
	}

	@Nullable
	public BigDecimal getStufe2KantonKostenuebernahmeAnzahlTage() {
		return stufe2KantonKostenuebernahmeAnzahlTage;
	}

	public void setStufe2KantonKostenuebernahmeAnzahlTage(@Nullable BigDecimal stufe2KantonKostenuebernahmeAnzahlTage) {
		this.stufe2KantonKostenuebernahmeAnzahlTage = stufe2KantonKostenuebernahmeAnzahlTage;
	}

	@Nullable
	public BigDecimal getStufe2InstitutionKostenuebernahmeAnzahlTage() {
		return stufe2InstitutionKostenuebernahmeAnzahlTage;
	}

	public void setStufe2InstitutionKostenuebernahmeAnzahlTage(@Nullable BigDecimal stufe2InstitutionKostenuebernahmeAnzahlTage) {
		this.stufe2InstitutionKostenuebernahmeAnzahlTage = stufe2InstitutionKostenuebernahmeAnzahlTage;
	}

	@Nullable
	public BigDecimal getStufe1KantonKostenuebernahmeBetreuung() {
		return stufe1KantonKostenuebernahmeBetreuung;
	}

	public void setStufe1KantonKostenuebernahmeBetreuung(@Nullable BigDecimal stufe1KantonKostenuebernahmeBetreuung) {
		this.stufe1KantonKostenuebernahmeBetreuung = stufe1KantonKostenuebernahmeBetreuung;
	}

	@Nullable
	public BigDecimal getStufe1InstitutionKostenuebernahmeBetreuung() {
		return stufe1InstitutionKostenuebernahmeBetreuung;
	}

	public void setStufe1InstitutionKostenuebernahmeBetreuung(@Nullable BigDecimal stufe1InstitutionKostenuebernahmeBetreuung) {
		this.stufe1InstitutionKostenuebernahmeBetreuung = stufe1InstitutionKostenuebernahmeBetreuung;
	}

	@Nullable
	public BigDecimal getStufe2KantonKostenuebernahmeBetreuung() {
		return stufe2KantonKostenuebernahmeBetreuung;
	}

	public void setStufe2KantonKostenuebernahmeBetreuung(@Nullable BigDecimal stufe2KantonKostenuebernahmeBetreuung) {
		this.stufe2KantonKostenuebernahmeBetreuung = stufe2KantonKostenuebernahmeBetreuung;
	}

	@Nullable
	public BigDecimal getStufe2InstitutionKostenuebernahmeBetreuung() {
		return stufe2InstitutionKostenuebernahmeBetreuung;
	}

	public void setStufe2InstitutionKostenuebernahmeBetreuung(@Nullable BigDecimal stufe2InstitutionKostenuebernahmeBetreuung) {
		this.stufe2InstitutionKostenuebernahmeBetreuung = stufe2InstitutionKostenuebernahmeBetreuung;
	}

	@Nullable
	public BigDecimal getStufe1FreigabeBetrag() {
		return stufe1FreigabeBetrag;
	}

	public void setStufe1FreigabeBetrag(@Nullable BigDecimal stufe1FreigabeBetrag) {
		this.stufe1FreigabeBetrag = stufe1FreigabeBetrag;
	}

	@Nullable
	public LocalDateTime getStufe1FreigabeDatum() {
		return stufe1FreigabeDatum;
	}

	public void setStufe1FreigabeDatum(@Nullable LocalDateTime stufe1FreigabeDatum) {
		this.stufe1FreigabeDatum = stufe1FreigabeDatum;
	}

	@Nullable
	public LocalDateTime getStufe1FreigabeAusbezahltAm() {
		return stufe1FreigabeAusbezahltAm;
	}

	public void setStufe1FreigabeAusbezahltAm(@Nullable LocalDateTime stufe1FreigabeAusbezahltAm) {
		this.stufe1FreigabeAusbezahltAm = stufe1FreigabeAusbezahltAm;
	}

	@Nullable
	public BigDecimal getStufe2VerfuegungBetrag() {
		return stufe2VerfuegungBetrag;
	}

	public void setStufe2VerfuegungBetrag(@Nullable BigDecimal stufe2VerfuegungBetrag) {
		this.stufe2VerfuegungBetrag = stufe2VerfuegungBetrag;
	}

	@Nullable
	public LocalDateTime getStufe2VerfuegungDatum() {
		return stufe2VerfuegungDatum;
	}

	public void setStufe2VerfuegungDatum(@Nullable LocalDateTime stufe2VerfuegungDatum) {
		this.stufe2VerfuegungDatum = stufe2VerfuegungDatum;
	}

	@Nullable
	public LocalDateTime getStufe2VerfuegungAusbezahltAm() {
		return stufe2VerfuegungAusbezahltAm;
	}

	public void setStufe2VerfuegungAusbezahltAm(@Nullable LocalDateTime stufe2VerfuegungAusbezahltAm) {
		this.stufe2VerfuegungAusbezahltAm = stufe2VerfuegungAusbezahltAm;
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
			this.getInstitutionStammdaten().getId().equals(otherRueckforderungFormular.getInstitutionStammdaten().getId());
	}

	private boolean isAuszuzahlenStufe1() {
		return RueckforderungStatus.GEPRUEFT_STUFE_1.ordinal() <=  status.ordinal() && stufe1FreigabeAusbezahltAm == null
			&& !status.equals(RueckforderungStatus.ABGESCHLOSSEN_OHNE_GESUCH);
	}

	private boolean isAuszuzahlenStufe2() {
		return RueckforderungStatus.VERFUEGT == status && stufe2VerfuegungAusbezahltAm == null;
	}

	public void handleAuszahlungIfNecessary() {
		if (isAuszuzahlenStufe1()) {
			this.stufe1FreigabeAusbezahltAm = LocalDateTime.now();
		}
		if (isAuszuzahlenStufe2()) {
			this.stufe2VerfuegungAusbezahltAm = LocalDateTime.now();
		}
	}
}
