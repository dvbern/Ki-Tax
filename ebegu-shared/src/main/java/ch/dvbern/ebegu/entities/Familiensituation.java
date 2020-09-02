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

package ch.dvbern.ebegu.entities;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.ForeignKey;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import ch.dvbern.ebegu.enums.AntragCopyType;
import ch.dvbern.ebegu.enums.EnumFamilienstatus;
import ch.dvbern.ebegu.util.EbeguUtil;
import org.hibernate.envers.Audited;

/**
 * Entitaet zum Speichern von Familiensituation in der Datenbank.
 */
@Audited
@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = "auszahlungsdaten_id", name = "UK_familiensituation_auszahlungsdaten_id"))
public class Familiensituation extends AbstractMutableEntity {

	private static final long serialVersionUID = -6534582356181164632L;

	@Enumerated(EnumType.STRING)
	@NotNull
	@Column(nullable = false)
	private EnumFamilienstatus familienstatus;

	@Nullable
	@Column(nullable = true)
	private Boolean gemeinsameSteuererklaerung;

	// Diese beiden Felder werden nicht immer eingegeben, deswegen Boolean und nicht boolean, damit sie auch null sein duerfen
	@Nullable
	@Column(nullable = true)
	private Boolean sozialhilfeBezueger;

	@Nullable
	@Column(nullable = true)
	private Boolean verguenstigungGewuenscht;

	@Nullable
	@Column(nullable = true)
	private LocalDate aenderungPer;

	@Nullable
	@Column(nullable = true)
	private LocalDate startKonkubinat;

	@Column(nullable = false)
	private boolean keineMahlzeitenverguenstigungBeantragt;

	@Nullable
	@Valid
	@OneToOne(optional = true, cascade = CascadeType.ALL, orphanRemoval = true)
	@JoinColumn(foreignKey = @ForeignKey(name = "FK_familiensituation_auszahlungsdaten_id"), nullable = true)
	private Auszahlungsdaten auszahlungsdaten;

	@Column(nullable = false)
	private boolean abweichendeZahlungsadresse;


	public Familiensituation() {
	}

	public Familiensituation(Familiensituation that) {
		if (that != null) {
			this.familienstatus = that.getFamilienstatus();
			this.gemeinsameSteuererklaerung = that.getGemeinsameSteuererklaerung();
			this.aenderungPer = that.getAenderungPer();
			this.startKonkubinat = that.getStartKonkubinat();
			this.sozialhilfeBezueger = that.getSozialhilfeBezueger();
			this.verguenstigungGewuenscht = that.getVerguenstigungGewuenscht();
		}
	}

	@Nonnull
	public EnumFamilienstatus getFamilienstatus() {
		return familienstatus;
	}

	public void setFamilienstatus(@Nonnull EnumFamilienstatus familienstatus) {
		this.familienstatus = familienstatus;
	}

	@Nullable
	public Boolean getGemeinsameSteuererklaerung() {
		return gemeinsameSteuererklaerung;
	}

	public void setGemeinsameSteuererklaerung(@Nullable Boolean gemeinsameSteuererklaerung) {
		this.gemeinsameSteuererklaerung = gemeinsameSteuererklaerung;
	}

	@Nullable
	public LocalDate getAenderungPer() {
		return aenderungPer;
	}

	public void setAenderungPer(@Nullable LocalDate aenderungPer) {
		this.aenderungPer = aenderungPer;
	}

	@Nullable
	public LocalDate getStartKonkubinat() {
		return startKonkubinat;
	}

	public void setStartKonkubinat(@Nullable LocalDate startKonkubinat) {
		this.startKonkubinat = startKonkubinat;
	}

	@Nullable
	public Boolean getSozialhilfeBezueger() {
		return sozialhilfeBezueger;
	}

	public void setSozialhilfeBezueger(@Nullable Boolean sozialhilfeBezueger) {
		this.sozialhilfeBezueger = sozialhilfeBezueger;
	}

	@Nullable
	public Boolean getVerguenstigungGewuenscht() {
		return verguenstigungGewuenscht;
	}

	public void setVerguenstigungGewuenscht(@Nullable Boolean verguenstigungGewuenscht) {
		this.verguenstigungGewuenscht = verguenstigungGewuenscht;
	}

	public boolean isKeineMahlzeitenverguenstigungBeantragt() {
		return keineMahlzeitenverguenstigungBeantragt;
	}

	public void setKeineMahlzeitenverguenstigungBeantragt(boolean keineMahlzeitenverguenstigungBeantragt) {
		this.keineMahlzeitenverguenstigungBeantragt = keineMahlzeitenverguenstigungBeantragt;
	}

	@Nullable
	public Auszahlungsdaten getAuszahlungsdaten() {
		return auszahlungsdaten;
	}

	public void setAuszahlungsdaten(@Nullable Auszahlungsdaten auszahlungsdaten) {
		this.auszahlungsdaten = auszahlungsdaten;
	}

	public boolean isAbweichendeZahlungsadresse() {
		return abweichendeZahlungsadresse;
	}

	public void setAbweichendeZahlungsadresse(boolean abweichendeZahlungsadresse) {
		this.abweichendeZahlungsadresse = abweichendeZahlungsadresse;
	}

	@Transient
	public boolean hasSecondGesuchsteller(LocalDate referenzdatum) {
		if (this.familienstatus != null) {
			switch (this.familienstatus) {
			case ALLEINERZIEHEND:
				return false;
			case VERHEIRATET:
			case KONKUBINAT:
				return true;
			case KONKUBINAT_KEIN_KIND:
				// a konkubinat is considered to be "long" and therefore requires a 2nd Gesuchsteller
				// when it started 5 years before the given date. Since the rule applies one month after
				// this five years (as it is with all other rules) we need to substract one month too.
				return this.startKonkubinat == null ||
					!this.startKonkubinat.isAfter(referenzdatum
						.minus(5, ChronoUnit.YEARS)
						.minus(1, ChronoUnit.MONTHS));
			}
		}
		return false;
	}

	@Nonnull
	public Familiensituation copyFamiliensituation(@Nonnull Familiensituation target, @Nonnull AntragCopyType copyType) {
		super.copyAbstractEntity(target, copyType);
		target.setFamilienstatus(this.getFamilienstatus());
		target.setStartKonkubinat(this.getStartKonkubinat());
		switch (copyType) {
		case MUTATION:
			target.setAenderungPer(this.getAenderungPer());
			target.setVerguenstigungGewuenscht(this.getVerguenstigungGewuenscht());
			target.setGemeinsameSteuererklaerung(this.getGemeinsameSteuererklaerung());
			target.setSozialhilfeBezueger(this.getSozialhilfeBezueger());
			target.setKeineMahlzeitenverguenstigungBeantragt(this.isKeineMahlzeitenverguenstigungBeantragt());
			if (this.getAuszahlungsdaten() != null) {
				target.setAuszahlungsdaten(this.getAuszahlungsdaten().copyAuszahlungsdaten(new Auszahlungsdaten(), copyType));
			}
			target.setAbweichendeZahlungsadresse(this.isAbweichendeZahlungsadresse());
			break;
		case MUTATION_NEUES_DOSSIER:
			target.setVerguenstigungGewuenscht(this.getVerguenstigungGewuenscht());
			target.setGemeinsameSteuererklaerung(this.getGemeinsameSteuererklaerung());
			target.setSozialhilfeBezueger(this.getSozialhilfeBezueger());
			target.setKeineMahlzeitenverguenstigungBeantragt(this.isKeineMahlzeitenverguenstigungBeantragt());
			if (this.getAuszahlungsdaten() != null) {
				target.setAuszahlungsdaten(this.getAuszahlungsdaten().copyAuszahlungsdaten(new Auszahlungsdaten(), copyType));
			}
			target.setAbweichendeZahlungsadresse(this.isAbweichendeZahlungsadresse());
			break;
		case ERNEUERUNG:
		case ERNEUERUNG_NEUES_DOSSIER:
			break;
		}
		return target;
	}

	@Override
	public boolean isSame(@Nullable AbstractEntity other) {
		//noinspection ObjectEquality
		if (this == other) {
			return true;
		}
		if (other == null || !getClass().equals(other.getClass())) {
			return false;
		}
		if (!(other instanceof Familiensituation)) {
			return false;
		}
		final Familiensituation otherFamiliensituation = (Familiensituation) other;
		return Objects.equals(getAenderungPer(), otherFamiliensituation.getAenderungPer()) &&
			getFamilienstatus() == otherFamiliensituation.getFamilienstatus() &&
			EbeguUtil.isSameOrNullBoolean(getGemeinsameSteuererklaerung(), otherFamiliensituation.getGemeinsameSteuererklaerung()) &&
			Objects.equals(getSozialhilfeBezueger(), otherFamiliensituation.getSozialhilfeBezueger()) &&
			Objects.equals(getVerguenstigungGewuenscht(), otherFamiliensituation.getVerguenstigungGewuenscht()) &&
			Objects.equals(getStartKonkubinat(), otherFamiliensituation.getStartKonkubinat());
	}
}
