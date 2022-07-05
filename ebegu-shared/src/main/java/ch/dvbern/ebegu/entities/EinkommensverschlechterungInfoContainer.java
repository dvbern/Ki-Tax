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

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.ForeignKey;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import ch.dvbern.ebegu.enums.AntragCopyType;
import ch.dvbern.ebegu.util.EbeguUtil;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
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
public class EinkommensverschlechterungInfoContainer extends AbstractMutableEntity {

	private static final long serialVersionUID = 7458803905310712257L;

	@Nullable
	@Valid
	@OneToOne(optional = true, cascade = CascadeType.ALL, orphanRemoval = true)
	@JoinColumn(foreignKey = @ForeignKey(name = "FK_ekvinfocontainer_einkommensverschlechterunginfogs_id"), nullable = true)
	private EinkommensverschlechterungInfo einkommensverschlechterungInfoGS;

	@Valid
	@Nonnull
	@OneToOne(optional = true, cascade = CascadeType.ALL, orphanRemoval = true)
	@JoinColumn(foreignKey = @ForeignKey(name = "FK_ekvinfocontainer_einkommensverschlechterunginfoja_id"), nullable = true)
	private EinkommensverschlechterungInfo einkommensverschlechterungInfoJA = new EinkommensverschlechterungInfo();

	@NotNull
	@Valid
	@OneToOne(optional = false, mappedBy = "einkommensverschlechterungInfoContainer")
	private Gesuch gesuch;

	public EinkommensverschlechterungInfoContainer() {
	}

	public EinkommensverschlechterungInfoContainer(EinkommensverschlechterungInfoContainer other) {
		if (other != null) {
			if (other.getEinkommensverschlechterungInfoGS() != null) {
				this.einkommensverschlechterungInfoGS = new EinkommensverschlechterungInfo(other.getEinkommensverschlechterungInfoGS());
			}
			this.einkommensverschlechterungInfoJA = new EinkommensverschlechterungInfo(other.getEinkommensverschlechterungInfoJA());
			this.gesuch = other.getGesuch();
		}
	}

	@Nullable
	public EinkommensverschlechterungInfo getEinkommensverschlechterungInfoGS() {
		return einkommensverschlechterungInfoGS;
	}

	public void setEinkommensverschlechterungInfoGS(@Nullable EinkommensverschlechterungInfo einkommensverschlechterungInfoGS) {
		this.einkommensverschlechterungInfoGS = einkommensverschlechterungInfoGS;
	}

	@Nonnull
	public EinkommensverschlechterungInfo getEinkommensverschlechterungInfoJA() {
		return einkommensverschlechterungInfoJA;
	}

	public void setEinkommensverschlechterungInfoJA(@Nonnull EinkommensverschlechterungInfo einkommensverschlechterungInfoJA) {
		this.einkommensverschlechterungInfoJA = einkommensverschlechterungInfoJA;
	}

	public Gesuch getGesuch() {
		return gesuch;
	}

	public void setGesuch(Gesuch gesuch) {
		this.gesuch = gesuch;

		if (gesuch != null &&
			(gesuch.getEinkommensverschlechterungInfoContainer() == null || !gesuch.getEinkommensverschlechterungInfoContainer().equals(this))) {
			gesuch.setEinkommensverschlechterungInfoContainer(this);
		}
	}

	@Nonnull
	public EinkommensverschlechterungInfoContainer copyEinkommensverschlechterungInfoContainer(
			@Nonnull EinkommensverschlechterungInfoContainer target, @Nonnull AntragCopyType copyType, @Nonnull Gesuch targetGesuch) {
		super.copyAbstractEntity(target, copyType);
		switch (copyType) {
		case MUTATION:
		case MUTATION_NEUES_DOSSIER:
			target.setGesuch(targetGesuch);
			target.setEinkommensverschlechterungInfoGS(null);
			target.setEinkommensverschlechterungInfoJA(getEinkommensverschlechterungInfoJA().copyEinkommensverschlechterungInfo(
				new EinkommensverschlechterungInfo(), copyType));
			break;
		case ERNEUERUNG:
		case ERNEUERUNG_NEUES_DOSSIER:
			break;
		}
		return target;
	}

	@Override
	@SuppressWarnings("PMD.CompareObjectsWithEquals")
	@SuppressFBWarnings("BC_UNCONFIRMED_CAST")
	public boolean isSame(AbstractEntity other) {
		//noinspection ObjectEquality
		if (this == other) {
			return true;
		}
		if (other == null || !getClass().equals(other.getClass())) {
			return false;
		}
		final EinkommensverschlechterungInfoContainer otherEKVInfoContainer = (EinkommensverschlechterungInfoContainer) other;
		return EbeguUtil.isSame(getEinkommensverschlechterungInfoJA(), otherEKVInfoContainer.getEinkommensverschlechterungInfoJA());
	}
}
