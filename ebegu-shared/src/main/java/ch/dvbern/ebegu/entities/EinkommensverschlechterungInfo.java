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

import java.util.Objects;

import javax.annotation.Nonnull;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.validation.constraints.NotNull;

import ch.dvbern.ebegu.enums.AntragCopyType;
import ch.dvbern.ebegu.util.EbeguUtil;
import org.hibernate.envers.Audited;

/**
 * Entity für die Erfassung von Einkommensverschlechterungen für das Gesuch
 * Speichern der Entscheidung ob eine Einkommensverschlechterung geltend gemacht werden möchte sowie die Auswahl der
 * Jahreshälfte, Monat des Ereignisses sowie deren Grund
 *
 * @author gapa
 * @version 1.0
 */
@Audited
@Entity
public class EinkommensverschlechterungInfo extends AbstractMutableEntity {

	private static final long serialVersionUID = 3952202946246235539L;

	@NotNull
	@Column(nullable = false)
	private Boolean einkommensverschlechterung = Boolean.FALSE;

	@NotNull
	@Column(nullable = false)
	private Boolean ekvFuerBasisJahrPlus1;

	@NotNull
	@Column(nullable = false)
	private Boolean ekvFuerBasisJahrPlus2;

	@NotNull
	@Column(nullable = false)
	private Boolean ekvBasisJahrPlus1Annulliert = false;

	@NotNull
	@Column(nullable = false)
	private Boolean ekvBasisJahrPlus2Annulliert = false;

	public EinkommensverschlechterungInfo() {
	}

	public EinkommensverschlechterungInfo(EinkommensverschlechterungInfo other) {
		if (other != null) {
			this.einkommensverschlechterung = other.getEinkommensverschlechterung();
			this.ekvFuerBasisJahrPlus1 = other.getEkvFuerBasisJahrPlus1();
			this.ekvFuerBasisJahrPlus2 = other.getEkvFuerBasisJahrPlus2();
		}
	}

	public Boolean getEinkommensverschlechterung() {
		return einkommensverschlechterung;
	}

	public void setEinkommensverschlechterung(final Boolean einkommensverschlechterung) {
		this.einkommensverschlechterung = einkommensverschlechterung;
	}

	public Boolean getEkvFuerBasisJahrPlus1() {
		return ekvFuerBasisJahrPlus1;
	}

	public void setEkvFuerBasisJahrPlus1(final Boolean ekvFuerBasisJahrPlus1) {
		this.ekvFuerBasisJahrPlus1 = ekvFuerBasisJahrPlus1;
	}

	public Boolean getEkvFuerBasisJahrPlus2() {
		return ekvFuerBasisJahrPlus2;
	}

	public void setEkvFuerBasisJahrPlus2(final Boolean ekvFuerBasisJahrPlus2) {
		this.ekvFuerBasisJahrPlus2 = ekvFuerBasisJahrPlus2;
	}

	public Boolean getEkvBasisJahrPlus1Annulliert() {
		return ekvBasisJahrPlus1Annulliert;
	}

	public void setEkvBasisJahrPlus1Annulliert(Boolean ekvBasisJahrPlus1Annulliert) {
		this.ekvBasisJahrPlus1Annulliert = ekvBasisJahrPlus1Annulliert;
	}

	public Boolean getEkvBasisJahrPlus2Annulliert() {
		return ekvBasisJahrPlus2Annulliert;
	}

	public void setEkvBasisJahrPlus2Annulliert(Boolean ekvBasisJahrPlus2Annulliert) {
		this.ekvBasisJahrPlus2Annulliert = ekvBasisJahrPlus2Annulliert;
	}

	@Nonnull
	public EinkommensverschlechterungInfo copyEinkommensverschlechterungInfo(@Nonnull EinkommensverschlechterungInfo target, @Nonnull AntragCopyType copyType) {
		super.copyAbstractEntity(target, copyType);
		switch (copyType) {
		case MUTATION:
		case MUTATION_NEUES_DOSSIER:
			target.setEinkommensverschlechterung(this.getEinkommensverschlechterung());
			target.setEkvFuerBasisJahrPlus1(this.getEkvFuerBasisJahrPlus1());
			target.setEkvFuerBasisJahrPlus2(this.getEkvFuerBasisJahrPlus2());
			target.setEkvBasisJahrPlus1Annulliert(this.getEkvBasisJahrPlus1Annulliert());
			target.setEkvBasisJahrPlus2Annulliert(this.getEkvBasisJahrPlus2Annulliert());
			break;
		case ERNEUERUNG:
		case ERNEUERUNG_NEUES_DOSSIER:
			break;
		}
		return target;
	}

	@Override
	@SuppressWarnings({ "OverlyComplexBooleanExpression", "PMD.CompareObjectsWithEquals" })
	public boolean isSame(AbstractEntity other) {
		//noinspection ObjectEquality
		if (this == other) {
			return true;
		}
		if (other == null || !getClass().equals(other.getClass())) {
			return false;
		}
		final EinkommensverschlechterungInfo otherEKVInfo = (EinkommensverschlechterungInfo) other;
		// if there is no EKV (einkommensverschlechterung==false) there is no need to compare the rest
		//noinspection SimplifiableIfStatement -> for clarity sake
		if (Objects.equals(getEinkommensverschlechterung(), otherEKVInfo.getEinkommensverschlechterung())
			&& Boolean.FALSE.equals(getEinkommensverschlechterung())) {
			return true;
		}
		return isSameBasisJahrPlus1(otherEKVInfo) && isSameBasisJahrPlus2(otherEKVInfo)
			&& EbeguUtil.isSameOrNullBoolean(this.ekvBasisJahrPlus1Annulliert, otherEKVInfo.ekvBasisJahrPlus1Annulliert)
			&& EbeguUtil.isSameOrNullBoolean(this.ekvBasisJahrPlus2Annulliert, otherEKVInfo.ekvBasisJahrPlus2Annulliert);
	}

	private boolean isSameBasisJahrPlus1(EinkommensverschlechterungInfo otherEKVInfo) {
		return EbeguUtil.isSameOrNullBoolean(getEkvFuerBasisJahrPlus1(), otherEKVInfo.getEkvFuerBasisJahrPlus1());
	}

	private boolean isSameBasisJahrPlus2(EinkommensverschlechterungInfo otherEKVInfo) {
		return EbeguUtil.isSameOrNullBoolean(getEkvFuerBasisJahrPlus2(), otherEKVInfo.getEkvFuerBasisJahrPlus2());
	}
}
