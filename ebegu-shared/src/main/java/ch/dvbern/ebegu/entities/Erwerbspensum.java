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

import java.util.Locale;
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
import javax.validation.constraints.NotNull;

import ch.dvbern.ebegu.enums.AntragCopyType;
import ch.dvbern.ebegu.enums.Taetigkeit;
import ch.dvbern.ebegu.util.Constants;
import ch.dvbern.ebegu.util.EbeguUtil;
import ch.dvbern.ebegu.util.ServerMessageUtil;
import ch.dvbern.ebegu.validators.CheckUnbezahlterUrlaub;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.hibernate.envers.Audited;

/**
 * Erwerbspensum eines Gesuchstellers
 */
@Entity
@Audited
@CheckUnbezahlterUrlaub
public class Erwerbspensum extends AbstractIntegerPensum {

	private static final long serialVersionUID = 4649639217797690323L;

	@NotNull
	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = Constants.DB_DEFAULT_MAX_LENGTH)
	private Taetigkeit taetigkeit;

	@Column(nullable = true)
	@Nullable
	private String bezeichnung;

	@Nullable
	@OneToOne(optional = true, cascade = CascadeType.ALL, orphanRemoval = true)
	@JoinColumn(foreignKey = @ForeignKey(name = "FK_erwerbspensum_urlaub_id"), nullable = true)
	private UnbezahlterUrlaub unbezahlterUrlaub;

	@Column(nullable = true)
	@Nullable
	private Boolean unregelmaessigeArbeitszeiten;


	public Erwerbspensum() {
	}


	public Taetigkeit getTaetigkeit() {
		return taetigkeit;
	}

	public void setTaetigkeit(Taetigkeit taetigkeit) {
		this.taetigkeit = taetigkeit;
	}

	@Nullable
	public String getBezeichnung() {
		return bezeichnung;
	}

	public void setBezeichnung(@Nullable String bezeichnung) {
		this.bezeichnung = bezeichnung;
	}

	@Nullable
	public UnbezahlterUrlaub getUnbezahlterUrlaub() {
		return unbezahlterUrlaub;
	}

	public void setUnbezahlterUrlaub(@Nullable UnbezahlterUrlaub unbezahlterUrlaub) {
		this.unbezahlterUrlaub = unbezahlterUrlaub;
	}

	@Nullable
	public Boolean getUnregelmaessigeArbeitszeiten() {
		return unregelmaessigeArbeitszeiten;
	}

	public void setUnregelmaessigeArbeitszeiten(@Nullable Boolean unregelmaessigeArbeitszeiten) {
		this.unregelmaessigeArbeitszeiten = unregelmaessigeArbeitszeiten;
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
		boolean taetigkeitSame = taetigkeit == otherErwerbspensum.getTaetigkeit();
		boolean bezeichnungSame = EbeguUtil.isSameOrNullStrings(bezeichnung, otherErwerbspensum.getBezeichnung());
		boolean urlaubSame = Objects.equals(unbezahlterUrlaub, otherErwerbspensum.getUnbezahlterUrlaub());
		return pensumIsSame && taetigkeitSame && bezeichnungSame && urlaubSame;
	}

	public String getName(
			@Nonnull Locale locale,
			Mandant mandant) {
		if (bezeichnung == null || bezeichnung.isEmpty()) {
			return ServerMessageUtil.translateEnumValue(taetigkeit, locale, mandant) + ' ' + getPensum() + '%';
		}
		return bezeichnung;
	}


	@Nonnull
	public Erwerbspensum copyErwerbspensum(@Nonnull Erwerbspensum target, @Nonnull AntragCopyType copyType) {
		super.copyAbstractPensumEntity(target, copyType);
		switch (copyType) {
		case MUTATION:
		case MUTATION_NEUES_DOSSIER:
			target.setTaetigkeit(this.getTaetigkeit());
			target.setBezeichnung(this.getBezeichnung());
			copyUnbezahlterUrlaub(target, copyType);
			break;
		case ERNEUERUNG:
		case ERNEUERUNG_NEUES_DOSSIER:
			break;
		}
		return target;
	}

	private void copyUnbezahlterUrlaub(@Nonnull Erwerbspensum target, @Nonnull AntragCopyType copyType) {
		if (this.getUnbezahlterUrlaub() != null) {
			target.setUnbezahlterUrlaub(this.getUnbezahlterUrlaub().copyUnbezahlterUrlaub(new UnbezahlterUrlaub(), copyType));
		}
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this)
			.append("bezeichnung", bezeichnung)
			.append("taetigkeit", taetigkeit)
			.append("unbezahlterUrlaub", unbezahlterUrlaub)
			.toString();
	}
}
