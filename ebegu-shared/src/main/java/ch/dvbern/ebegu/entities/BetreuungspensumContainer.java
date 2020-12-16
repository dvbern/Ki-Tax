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
import javax.annotation.Nullable;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.ForeignKey;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Transient;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import ch.dvbern.ebegu.enums.AntragCopyType;
import ch.dvbern.ebegu.types.DateRange;
import ch.dvbern.ebegu.util.EbeguUtil;
import ch.dvbern.ebegu.util.Gueltigkeit;
import org.apache.commons.lang.builder.CompareToBuilder;
import org.hibernate.envers.Audited;

/**
 * Container-Entity für die Betreuungspensen: Diese muss für jeden Benutzertyp (GS, JA) einzeln geführt werden,
 * damit die Veränderungen / Korrekturen angezeigt werden können.
 */
@Audited
@Entity
public class BetreuungspensumContainer extends AbstractMutableEntity
	implements Gueltigkeit, Comparable<BetreuungspensumContainer> {

	private static final long serialVersionUID = -6784987861150035840L;

	@NotNull
	@ManyToOne(optional = false)
	@JoinColumn(foreignKey = @ForeignKey(name = "FK_betreuungspensum_container_betreuung_id"), nullable = false)
	private Betreuung betreuung;

	@Nullable
	@Valid
	@OneToOne(optional = true, cascade = CascadeType.ALL, orphanRemoval = true)
	@JoinColumn(foreignKey = @ForeignKey(name = "FK_betreuungspensum_container_betreuungspensum_gs"))
	private Betreuungspensum betreuungspensumGS;

	@Valid
	@OneToOne(optional = true, cascade = CascadeType.ALL, orphanRemoval = true)
	@JoinColumn(foreignKey = @ForeignKey(name = "FK_betreuungspensum_container_betreuungspensum_ja"))
	private Betreuungspensum betreuungspensumJA;

	public BetreuungspensumContainer() {
	}

	public Betreuung getBetreuung() {
		return this.betreuung;
	}

	public void setBetreuung(Betreuung betreuung) {
		this.betreuung = betreuung;
	}

	@Nullable
	public Betreuungspensum getBetreuungspensumGS() {
		return betreuungspensumGS;
	}

	public void setBetreuungspensumGS(@Nullable Betreuungspensum betreuungspensumGS) {
		this.betreuungspensumGS = betreuungspensumGS;
	}

	public Betreuungspensum getBetreuungspensumJA() {
		return betreuungspensumJA;
	}

	public void setBetreuungspensumJA(Betreuungspensum betreuungspensumJA) {
		this.betreuungspensumJA = betreuungspensumJA;
	}

	@Nonnull
	@Override
	public DateRange getGueltigkeit() {
		return betreuungspensumJA.getGueltigkeit();
	}

	@SuppressWarnings({ "OverlyComplexBooleanExpression" })
	@Override
	public boolean isSame(AbstractEntity other) {
		//noinspection ObjectEquality
		if (this == other) {
			return true;
		}
		if (other == null || !getClass().equals(other.getClass())) {
			return false;
		}
		if (!(other instanceof BetreuungspensumContainer)) {
			return false;
		}
		final BetreuungspensumContainer otherBetreuungspensumContainer = (BetreuungspensumContainer) other;

		return EbeguUtil.isSame(getBetreuungspensumJA(), otherBetreuungspensumContainer.getBetreuungspensumJA());
	}

	/**
	 * @return geht durch die internen Datenstrukturen hoch bis zur Gesuchsperiode und gibt diese zureuck
	 * @throws IllegalArgumentException wenn einer der benoetigten Pfade null ist
	 */
	@Transient
	public Gesuchsperiode extractGesuchsperiode() {
		Objects.requireNonNull(this.getBetreuung(), "Cannot extract Gesuchsperiode because Betreuung is null");
		Objects.requireNonNull(this.getBetreuung().getKind(), "Cannot extract Gesuchsperiode because Kind is null");
		Objects.requireNonNull(this.getBetreuung().getKind().getGesuch(), "Cannot extract Gesuchsperiode because Gesuch is null");
		return this.getBetreuung().getKind().getGesuch().getGesuchsperiode();
	}

	@Transient
	public Gesuch extractGesuch() {
		return this.getBetreuung().getKind().getGesuch();
	}

	@Override
	public int compareTo(BetreuungspensumContainer o) {
		CompareToBuilder builder = new CompareToBuilder();
		builder.append(this.getBetreuungspensumJA(), o.getBetreuungspensumJA());
		builder.append(this.getBetreuungspensumJA().getId(), o.getBetreuungspensumJA().getId());
		return builder.toComparison();
	}

	@Nonnull
	public BetreuungspensumContainer copyWithPensumJA() {
		return copyBetreuungspensumContainer(new BetreuungspensumContainer(), AntragCopyType.MUTATION, betreuung);
	}

	@Nonnull
	public BetreuungspensumContainer copyBetreuungspensumContainer(
			@Nonnull BetreuungspensumContainer target, @Nonnull AntragCopyType copyType, @Nonnull Betreuung targetBetreuung) {
		super.copyAbstractEntity(target, copyType);
		switch (copyType) {
		case MUTATION:
			target.setBetreuung(targetBetreuung);
			target.setBetreuungspensumGS(null);
			target.setBetreuungspensumJA(this.getBetreuungspensumJA().copyBetreuungspensum(new Betreuungspensum(), copyType));
			break;
		case ERNEUERUNG:
		case MUTATION_NEUES_DOSSIER:
		case ERNEUERUNG_NEUES_DOSSIER:
			break;
		}
		return target;
	}
}
