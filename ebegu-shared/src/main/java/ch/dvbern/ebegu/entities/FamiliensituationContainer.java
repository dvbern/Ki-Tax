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
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.ForeignKey;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.validation.Valid;

import ch.dvbern.ebegu.enums.AntragCopyType;
import ch.dvbern.ebegu.util.EbeguUtil;
import ch.dvbern.ebegu.validationgroups.AntragCompleteValidationGroup;
import ch.dvbern.ebegu.validators.CheckFamiliensituationContainerComplete;
import org.hibernate.envers.Audited;

/**
 * Entitaet zum Speichern von FamiliensituationContainer in der Datenbank.
 */
@CheckFamiliensituationContainerComplete(groups = AntragCompleteValidationGroup.class)
@Audited
@Entity
public class FamiliensituationContainer extends AbstractMutableEntity {

	private static final long serialVersionUID = 6696130722316500745L;

	@Valid
	@Nullable
	@OneToOne(cascade = CascadeType.ALL, orphanRemoval = true, optional = true)
	@JoinColumn(foreignKey = @ForeignKey(name = "FK_familiensituation_container_familiensituation_JA_id"))
	private Familiensituation familiensituationJA;

	@Valid
	@Nullable
	@OneToOne(cascade = CascadeType.ALL, orphanRemoval = true, optional = true)
	@JoinColumn(foreignKey = @ForeignKey(name = "FK_familiensituation_container_familiensituation_GS_id"))
	private Familiensituation familiensituationGS;

	@Valid
	@Nullable
	@OneToOne(cascade = CascadeType.ALL, orphanRemoval = true, optional = true)
	@JoinColumn(foreignKey = @ForeignKey(name = "FK_familiensituation_container_familiensituation_erstgesuch_id"))
	private Familiensituation familiensituationErstgesuch;

	public FamiliensituationContainer() {
	}

	@Nonnull
	public FamiliensituationContainer copyFamiliensituationContainer(
			@Nonnull FamiliensituationContainer target, @Nonnull AntragCopyType copyType, boolean sourceGesuchIsMutation) {
		super.copyAbstractEntity(target, copyType);
		target.setFamiliensituationGS(null);
		Objects.requireNonNull(getFamiliensituationJA());
		target.setFamiliensituationJA(getFamiliensituationJA().copyFamiliensituation(new Familiensituation(), copyType));

		switch (copyType) {
		case MUTATION:
			target.setFamiliensituationJA(this.getFamiliensituationJA().copyFamiliensituation(new Familiensituation(), copyType));
			// Falls das zu kopierende Gesuch bereits eine Mutation war, muss die FamiliensituationErstgesuch auch gesetzt werden
			if (sourceGesuchIsMutation) {
				Objects.requireNonNull(this.getFamiliensituationErstgesuch());
				target.setFamiliensituationErstgesuch(this.getFamiliensituationErstgesuch().copyFamiliensituation(new Familiensituation(), copyType));
			} else {
				target.setFamiliensituationErstgesuch(this.getFamiliensituationJA().copyFamiliensituation(new Familiensituation(), copyType));
			}
			break;
		case ERNEUERUNG:
		case MUTATION_NEUES_DOSSIER:
		case ERNEUERUNG_NEUES_DOSSIER:
			target.setFamiliensituationJA(this.getFamiliensituationJA().copyFamiliensituation(new Familiensituation(), copyType));
			break;
		}
		return target;
	}

	@Nullable
	public Familiensituation getFamiliensituationJA() {
		return familiensituationJA;
	}

	public void setFamiliensituationJA(@Nullable Familiensituation familiensituationJA) {
		this.familiensituationJA = familiensituationJA;
	}

	@Nullable
	public Familiensituation getFamiliensituationGS() {
		return familiensituationGS;
	}

	public void setFamiliensituationGS(@Nullable Familiensituation familiensituationGS) {
		this.familiensituationGS = familiensituationGS;
	}

	@Nullable
	public Familiensituation getFamiliensituationErstgesuch() {
		return familiensituationErstgesuch;
	}

	public void setFamiliensituationErstgesuch(@Nullable Familiensituation familiensituationErstgesuch) {
		this.familiensituationErstgesuch = familiensituationErstgesuch;
	}

	@Nullable
	public Familiensituation extractFamiliensituation() {
		return familiensituationJA;
	}

	@Nonnull
	public Familiensituation getFamiliensituationAm(LocalDate stichtag) {
		Objects.requireNonNull(getFamiliensituationJA());
		if (getFamiliensituationJA().getAenderungPer() == null || getFamiliensituationJA().getAenderungPer().isBefore(stichtag)) {
			return getFamiliensituationJA();
		}
		Objects.requireNonNull(getFamiliensituationErstgesuch());
		return getFamiliensituationErstgesuch();
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
		if (!(other instanceof FamiliensituationContainer)) {
			return false;
		}
		final FamiliensituationContainer otherFamSitContainer = (FamiliensituationContainer) other;
		return EbeguUtil.isSameObject(getFamiliensituationJA(), otherFamSitContainer.getFamiliensituationJA());
	}
}
