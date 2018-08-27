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
import java.util.Objects;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;

import ch.dvbern.ebegu.enums.AntragCopyType;
import ch.dvbern.ebegu.enums.EnumFamilienstatus;
import ch.dvbern.ebegu.enums.EnumGesuchstellerKardinalitaet;
import ch.dvbern.ebegu.util.EbeguUtil;
import org.hibernate.envers.Audited;

/**
 * Entitaet zum Speichern von Familiensituation in der Datenbank.
 */
@Audited
@Entity
public class Familiensituation extends AbstractMutableEntity {

	private static final long serialVersionUID = -6534582356181164632L;

	@Enumerated(EnumType.STRING)
	@NotNull
	@Column(nullable = false)
	private EnumFamilienstatus familienstatus;

	@Enumerated(EnumType.STRING)
	@Nullable
	@Column(nullable = true)
	private EnumGesuchstellerKardinalitaet gesuchstellerKardinalitaet;

	@Column(nullable = true)
	private Boolean gemeinsameSteuererklaerung;

	// Diese beiden Felder werden nicht immer eingegeben, deswegen Boolean und nicht boolean, damit sie auch null sein duerfen
	@Nullable
	@Column(nullable = true)
	private Boolean sozialhilfeBezueger;

	@Nullable
	@Column(nullable = true)
	private Boolean verguenstigungGewuenscht;

	@Column(nullable = true)
	private LocalDate aenderungPer;

	public Familiensituation() {
	}

	public Familiensituation(Familiensituation that) {
		if (that != null) {
			this.familienstatus = that.getFamilienstatus();
			this.gemeinsameSteuererklaerung = that.getGemeinsameSteuererklaerung();
			this.gesuchstellerKardinalitaet = that.getGesuchstellerKardinalitaet();
			this.aenderungPer = that.getAenderungPer();
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
	public EnumGesuchstellerKardinalitaet getGesuchstellerKardinalitaet() {
		return gesuchstellerKardinalitaet;
	}

	public void setGesuchstellerKardinalitaet(@Nullable EnumGesuchstellerKardinalitaet gesuchstellerKardinalitaet) {
		this.gesuchstellerKardinalitaet = gesuchstellerKardinalitaet;
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

	@Transient
	public boolean hasSecondGesuchsteller() {
		if (this.familienstatus != null) {
			switch (this.familienstatus) {
			case ALLEINERZIEHEND:
			case WENIGER_FUENF_JAHRE:
				return EnumGesuchstellerKardinalitaet.ZU_ZWEIT == this.getGesuchstellerKardinalitaet();
			case VERHEIRATET:
			case KONKUBINAT:
			case LAENGER_FUENF_JAHRE:
				return true;
			}
		}
		return false;
	}

	@Nonnull
	public Familiensituation copyFamiliensituation(@Nonnull Familiensituation target, @Nonnull AntragCopyType copyType) {
		super.copyAbstractEntity(target, copyType);
		target.setFamilienstatus(this.getFamilienstatus());
		target.setGemeinsameSteuererklaerung(this.getGemeinsameSteuererklaerung());
		target.setGesuchstellerKardinalitaet(this.getGesuchstellerKardinalitaet());
		target.setSozialhilfeBezueger(this.getSozialhilfeBezueger());
		target.setVerguenstigungGewuenscht(this.getVerguenstigungGewuenscht());
		switch (copyType) {
		case MUTATION:
			target.setAenderungPer(this.getAenderungPer());
			break;
		case ERNEUERUNG:
		case MUTATION_NEUES_DOSSIER:
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
			Objects.equals(getFamilienstatus(), otherFamiliensituation.getFamilienstatus()) &&
			Objects.equals(getGesuchstellerKardinalitaet(), otherFamiliensituation.getGesuchstellerKardinalitaet()) &&
			EbeguUtil.isSameOrNullBoolean(getGemeinsameSteuererklaerung(), otherFamiliensituation.getGemeinsameSteuererklaerung()) &&
			Objects.equals(getSozialhilfeBezueger(), otherFamiliensituation.getSozialhilfeBezueger()) &&
			Objects.equals(getVerguenstigungGewuenscht(), otherFamiliensituation.getVerguenstigungGewuenscht());

	}
}
