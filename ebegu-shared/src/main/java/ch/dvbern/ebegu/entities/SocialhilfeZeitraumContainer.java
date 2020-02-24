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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package ch.dvbern.ebegu.entities;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.ForeignKey;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import ch.dvbern.ebegu.enums.AntragCopyType;
import ch.dvbern.ebegu.util.EbeguUtil;
import org.hibernate.envers.Audited;

/**
 * Container-Entity für die Socialhilfe Zeiträume: Diese muss für die  Benutzertypen (GS, JA) einzeln geführt werden,
 * damit die Veränderungen / Korrekturen angezeigt werden können.
 */
@Audited
@Entity
public class SocialhilfeZeitraumContainer extends AbstractMutableEntity {

	private static final long serialVersionUID = -9132257320978372422L;

	@NotNull
	@ManyToOne(optional = false)
	@JoinColumn(foreignKey = @ForeignKey(name = "FK_socialhilfe_zeitraum_container_familliensituation_id"))
	private FamiliensituationContainer familiensituationContainer;

	@Nullable
	@Valid
	@OneToOne(optional = true, cascade = CascadeType.ALL, orphanRemoval = true)
	@JoinColumn(foreignKey = @ForeignKey(name = "FK_socialhilfe_zeitraum_container_socialhilfezeitraumgs_id"))
	private SocialhilfeZeitraum socialhilfeZeitraumGS;

	@Nullable
	@Valid
	@OneToOne(optional = true, cascade = CascadeType.ALL, orphanRemoval = true)
	@JoinColumn(foreignKey = @ForeignKey(name = "FK_socialhilfe_zeitraum_container_socialhilfezeitraumja_id"))
	private SocialhilfeZeitraum socialhilfeZeitraumJA;


	@Override
	public boolean isSame(AbstractEntity other) {
		//noinspection ObjectEquality
		if (this == other) {
			return true;
		}
		if (other == null || !getClass().equals(other.getClass())) {
			return false;
		}
		if (!(other instanceof SocialhilfeZeitraumContainer)) {
			return false;
		}
		final SocialhilfeZeitraumContainer otherSocialhilfeZeitraumContainer = (SocialhilfeZeitraumContainer) other;
		return EbeguUtil.isSameObject(getSocialhilfeZeitraumJA(), otherSocialhilfeZeitraumContainer.getSocialhilfeZeitraumJA());
	}

	@Nonnull
	public SocialhilfeZeitraumContainer copySocialhilfeZeitraumContainer(
		@Nonnull SocialhilfeZeitraumContainer target, @Nonnull AntragCopyType copyType,
		@Nonnull FamiliensituationContainer targetFamiliensituationContainer) {
		super.copyAbstractEntity(target, copyType);
		switch (copyType) {
		case MUTATION:
		case MUTATION_NEUES_DOSSIER:
			target.setFamiliensituationContainer(targetFamiliensituationContainer);
			target.setSocialhilfeZeitraumGS(null);
			if (this.getSocialhilfeZeitraumJA() != null) {
				target.setSocialhilfeZeitraumJA(this.getSocialhilfeZeitraumJA().copySocialhilfeZeitraum(new SocialhilfeZeitraum(), copyType));
			}
			break;
		case ERNEUERUNG:
		case ERNEUERUNG_NEUES_DOSSIER:
			break;
		}
		return target;
	}

	public FamiliensituationContainer getFamiliensituationContainer() {
		return familiensituationContainer;
	}

	public void setFamiliensituationContainer(FamiliensituationContainer familiensituationContainer) {
		this.familiensituationContainer = familiensituationContainer;
	}

	@Nullable
	public SocialhilfeZeitraum getSocialhilfeZeitraumGS() {
		return socialhilfeZeitraumGS;
	}

	public void setSocialhilfeZeitraumGS(@Nullable SocialhilfeZeitraum socialhilfeZeitraumGS) {
		this.socialhilfeZeitraumGS = socialhilfeZeitraumGS;
	}

	@Nullable
	public SocialhilfeZeitraum getSocialhilfeZeitraumJA() {
		return socialhilfeZeitraumJA;
	}

	public void setSocialhilfeZeitraumJA(@Nullable SocialhilfeZeitraum socialhilfeZeitraumJA) {
		this.socialhilfeZeitraumJA = socialhilfeZeitraumJA;
	}
}
