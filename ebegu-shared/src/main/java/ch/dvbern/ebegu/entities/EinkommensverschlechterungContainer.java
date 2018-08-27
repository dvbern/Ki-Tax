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
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import ch.dvbern.ebegu.enums.AntragCopyType;
import ch.dvbern.ebegu.util.EbeguUtil;
import org.hibernate.envers.Audited;

/**
 * Container-Entity für dieEinkommensverschlechterung: Diese muss für jeden
 * Benutzertyp (GS, JA) sowie für beide Halbjahre (Basisjahr + 1 und Basisjahr +2 ) einzeln geführt werden,
 * damit die Veränderungen Korrekturen angezeigt werden können.
 *
 * @author gapa
 * @version 1.0
 */
@Audited
@Entity
@Table(
	uniqueConstraints = @UniqueConstraint(columnNames = "gesuchsteller_container_id", name = "UK_einkommensverschlechterungcontainer_gesuchsteller")
)
public class EinkommensverschlechterungContainer extends AbstractMutableEntity {

	private static final long serialVersionUID = -2685774428336265818L;

	@NotNull
	@OneToOne(optional = false)
	@JoinColumn(foreignKey = @ForeignKey(name = "FK_einkommensverschlechterungcontainer_gesuchstellerContainer_id"), nullable = false)
	private GesuchstellerContainer gesuchstellerContainer;

	@Nullable
	@Valid
	@OneToOne(optional = true, cascade = CascadeType.ALL, orphanRemoval = true)
	@JoinColumn(foreignKey = @ForeignKey(name = "FK_einkommensverschlechterungcontainer_ekvGSBasisJahrPlus1_id"), nullable = true)
	private Einkommensverschlechterung ekvGSBasisJahrPlus1;

	@Nullable
	@Valid
	@OneToOne(optional = true, cascade = CascadeType.ALL, orphanRemoval = true)
	@JoinColumn(foreignKey = @ForeignKey(name = "FK_einkommensverschlechterungcontainer_ekvGSBasisJahrPlus2_id"), nullable = true)
	private Einkommensverschlechterung ekvGSBasisJahrPlus2;

	@Valid
	@OneToOne(optional = true, cascade = CascadeType.ALL, orphanRemoval = true)
	@JoinColumn(foreignKey = @ForeignKey(name = "FK_einkommensverschlechterungcontainer_ekvJABasisJahrPlus1_id"), nullable = true)
	private Einkommensverschlechterung ekvJABasisJahrPlus1;

	@Valid
	@OneToOne(optional = true, cascade = CascadeType.ALL, orphanRemoval = true)
	@JoinColumn(foreignKey = @ForeignKey(name = "FK_einkommensverschlechterungcontainer_ekvJABasisJahrPlus2_id"), nullable = true)
	private Einkommensverschlechterung ekvJABasisJahrPlus2;

	public EinkommensverschlechterungContainer() {
	}

	public Einkommensverschlechterung getEkvJABasisJahrPlus2() {
		return ekvJABasisJahrPlus2;
	}

	public void setEkvJABasisJahrPlus2(final Einkommensverschlechterung ekvJABasisJahrPlus2) {
		this.ekvJABasisJahrPlus2 = ekvJABasisJahrPlus2;
	}

	public Einkommensverschlechterung getEkvJABasisJahrPlus1() {
		return ekvJABasisJahrPlus1;
	}

	public void setEkvJABasisJahrPlus1(final Einkommensverschlechterung ekvJABasisJahrPlus1) {
		this.ekvJABasisJahrPlus1 = ekvJABasisJahrPlus1;
	}

	@Nullable
	public Einkommensverschlechterung getEkvGSBasisJahrPlus2() {
		return ekvGSBasisJahrPlus2;
	}

	public void setEkvGSBasisJahrPlus2(@Nullable final Einkommensverschlechterung ekvGSBasisJahrPlus2) {
		this.ekvGSBasisJahrPlus2 = ekvGSBasisJahrPlus2;
	}

	@Nullable
	public Einkommensverschlechterung getEkvGSBasisJahrPlus1() {
		return ekvGSBasisJahrPlus1;
	}

	public void setEkvGSBasisJahrPlus1(@Nullable final Einkommensverschlechterung ekvGSBasisJahrPlus1) {
		this.ekvGSBasisJahrPlus1 = ekvGSBasisJahrPlus1;
	}

	public GesuchstellerContainer getGesuchsteller() {
		return gesuchstellerContainer;
	}

	public void setGesuchsteller(GesuchstellerContainer gesuchsteller) {
		this.gesuchstellerContainer = gesuchsteller;
	}

	@Nonnull
	public EinkommensverschlechterungContainer copyEinkommensverschlechterungContainer(@Nonnull EinkommensverschlechterungContainer target,
			@Nonnull AntragCopyType copyType, @Nonnull GesuchstellerContainer targetGesuchstellerContainer) {

		super.copyAbstractEntity(target, copyType);
		switch (copyType) {
		case MUTATION:
		case MUTATION_NEUES_DOSSIER:
			target.setGesuchsteller(targetGesuchstellerContainer);
			target.setEkvGSBasisJahrPlus1(null);
			target.setEkvGSBasisJahrPlus2(null);
			if (this.getEkvJABasisJahrPlus1() != null) {
				target.setEkvJABasisJahrPlus1(this.getEkvJABasisJahrPlus1().copyEinkommensverschlechterung(new Einkommensverschlechterung(), copyType));
			}
			if (this.getEkvJABasisJahrPlus2() != null) {
				target.setEkvJABasisJahrPlus2(this.getEkvJABasisJahrPlus2().copyEinkommensverschlechterung(new Einkommensverschlechterung(), copyType));
			}
			break;
		case ERNEUERUNG:
		case ERNEUERUNG_NEUES_DOSSIER:
			break;
		}
		return target;
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
		if (!(other instanceof EinkommensverschlechterungContainer)) {
			return false;
		}
		final EinkommensverschlechterungContainer otherEKVContainer = (EinkommensverschlechterungContainer) other;
		return EbeguUtil.isSameObject(getEkvGSBasisJahrPlus1(), otherEKVContainer.getEkvGSBasisJahrPlus1()) &&
			EbeguUtil.isSameObject(getEkvGSBasisJahrPlus2(), otherEKVContainer.getEkvGSBasisJahrPlus2()) &&
			EbeguUtil.isSameObject(getEkvJABasisJahrPlus1(), otherEKVContainer.getEkvJABasisJahrPlus1()) &&
			EbeguUtil.isSameObject(getEkvJABasisJahrPlus2(), otherEKVContainer.getEkvJABasisJahrPlus2());
	}
}
