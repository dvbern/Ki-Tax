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
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.ForeignKey;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

import ch.dvbern.ebegu.enums.AntragCopyType;
import ch.dvbern.ebegu.enums.Taetigkeit;
import ch.dvbern.ebegu.enums.Zuschlagsgrund;
import ch.dvbern.ebegu.util.Constants;
import ch.dvbern.ebegu.util.EbeguUtil;
import ch.dvbern.ebegu.util.ServerMessageUtil;
import ch.dvbern.ebegu.validators.CheckUnbezahlterUrlaub;
import ch.dvbern.ebegu.validators.CheckZuschlagErwerbspensumZuschlagUndGrund;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.hibernate.envers.Audited;

/**
 * Erwerbspensum eines Gesuchstellers
 */
@Entity
@Audited
@CheckZuschlagErwerbspensumZuschlagUndGrund
@CheckUnbezahlterUrlaub
public class Erwerbspensum extends AbstractIntegerPensum {

	private static final long serialVersionUID = 4649639217797690323L;

	@NotNull
	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = Constants.DB_DEFAULT_MAX_LENGTH)
	private Taetigkeit taetigkeit;

	@Column(nullable = false)
	@NotNull
	private boolean zuschlagZuErwerbspensum;

	@Enumerated(EnumType.STRING)
	@Column(nullable = true)
	private Zuschlagsgrund zuschlagsgrund;

	@Min(0)
	@Max(100)
	@Column(nullable = true)
	private Integer zuschlagsprozent;

	@Column(nullable = true)
	@Nullable
	private String bezeichnung;

	@Nullable
	@OneToOne(optional = true, cascade = CascadeType.ALL, orphanRemoval = true)
	@JoinColumn(foreignKey = @ForeignKey(name = "FK_erwerbspensum_urlaub_id"), nullable = true)
	private UnbezahlterUrlaub unbezahlterUrlaub;

	public Erwerbspensum() {
	}

	public Taetigkeit getTaetigkeit() {
		return taetigkeit;
	}

	public void setTaetigkeit(Taetigkeit taetigkeit) {
		this.taetigkeit = taetigkeit;
	}

	public boolean getZuschlagZuErwerbspensum() {
		return zuschlagZuErwerbspensum;
	}

	public void setZuschlagZuErwerbspensum(boolean zuschlagZuErwerbspensum) {
		this.zuschlagZuErwerbspensum = zuschlagZuErwerbspensum;
	}

	public Integer getZuschlagsprozent() {
		return zuschlagsprozent;
	}

	public void setZuschlagsprozent(Integer zuschlagsprozent) {
		this.zuschlagsprozent = zuschlagsprozent;
	}

	public Zuschlagsgrund getZuschlagsgrund() {
		return zuschlagsgrund;
	}

	public void setZuschlagsgrund(Zuschlagsgrund zuschlagsgrund) {
		this.zuschlagsgrund = zuschlagsgrund;
	}

	@Nullable
	public UnbezahlterUrlaub getUnbezahlterUrlaub() {
		return unbezahlterUrlaub;
	}

	public void setUnbezahlterUrlaub(@Nullable UnbezahlterUrlaub unbezahlterUrlaub) {
		this.unbezahlterUrlaub = unbezahlterUrlaub;
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
		if (!super.isSame(other)) {
			return false;
		}
		if (!(other instanceof Erwerbspensum)) {
			return false;
		}
		final Erwerbspensum otherErwerbspensum = (Erwerbspensum) other;
		boolean pensumIsSame = super.isSame(otherErwerbspensum);
		boolean taetigkeitSame = Objects.equals(taetigkeit, otherErwerbspensum.getTaetigkeit());
		boolean zuschlagSame = Objects.equals(zuschlagZuErwerbspensum, otherErwerbspensum.getZuschlagZuErwerbspensum());
		boolean bezeichnungSame = EbeguUtil.isSameOrNullStrings(bezeichnung, otherErwerbspensum.getBezeichnung());
		boolean zuschlagsgrundSame = Objects.equals(zuschlagsgrund, otherErwerbspensum.getZuschlagsgrund());
		boolean zuschlagsprozentSame = Objects.equals(zuschlagsprozent, otherErwerbspensum.getZuschlagsprozent());
		boolean urlaubSame = Objects.equals(unbezahlterUrlaub, otherErwerbspensum.getUnbezahlterUrlaub());
		return pensumIsSame && taetigkeitSame && zuschlagSame && bezeichnungSame && zuschlagsgrundSame && zuschlagsprozentSame && urlaubSame;
	}

	public String getName() {
		if (bezeichnung == null || bezeichnung.isEmpty()) {
			return ServerMessageUtil.translateEnumValue(taetigkeit) + ' ' + getPensum() + '%';
		}
		return bezeichnung;
	}

	@Nullable
	public String getBezeichnung() {
		return bezeichnung;
	}

	public void setBezeichnung(@Nullable String bezeichnung) {
		this.bezeichnung = bezeichnung;
	}

	@Nonnull
	public Erwerbspensum copyErwerbspensum(@Nonnull Erwerbspensum target, @Nonnull AntragCopyType copyType) {
		super.copyAbstractPensumEntity(target, copyType);
		switch (copyType) {
		case MUTATION:
		case MUTATION_NEUES_DOSSIER:
			target.setTaetigkeit(this.getTaetigkeit());
			target.setZuschlagZuErwerbspensum(this.getZuschlagZuErwerbspensum());
			target.setZuschlagsgrund(this.getZuschlagsgrund());
			target.setZuschlagsprozent(this.getZuschlagsprozent());
			target.setBezeichnung(this.getBezeichnung());
			target.setUnbezahlterUrlaub(this.getUnbezahlterUrlaub());
			break;
		case ERNEUERUNG:
		case ERNEUERUNG_NEUES_DOSSIER:
			break;
		}
		return target;
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this)
			.append("bezeichnung", bezeichnung)
			.append("taetigkeit", taetigkeit)
			.append("zuschlagZuErwerbspensum", zuschlagZuErwerbspensum)
			.append("zuschlagsgrund", zuschlagsgrund)
			.append("zuschlagsprozent", zuschlagsprozent)
			.append("unbezahlterUrlaub", unbezahlterUrlaub)
			.toString();
	}

	public int getPensumInklZuschlag() {
		int total = getPensum();
		if (getZuschlagsprozent() != null) {
			total += getZuschlagsprozent();
		}
		return total;
	}
}
