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
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.ForeignKey;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.validation.constraints.NotNull;

import ch.dvbern.ebegu.enums.AntragCopyType;
import ch.dvbern.ebegu.enums.IntegrationTyp;
import ch.dvbern.ebegu.util.EbeguUtil;
import org.hibernate.envers.Audited;

/**
 * Entity fuer PensumFachstelle.
 */
@Audited
@Entity
public class PensumFachstelle extends AbstractIntegerPensum {

	private static final long serialVersionUID = -9132257320978374570L;

	@Nullable
	@ManyToOne(optional = true)
	@JoinColumn(foreignKey = @ForeignKey(name = "FK_pensum_fachstelle_fachstelle_id"))
	private Fachstelle fachstelle;

	@NotNull
	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private IntegrationTyp integrationTyp;


	public PensumFachstelle() {
	}

	@Nonnull
	public PensumFachstelle copyPensumFachstelle(@Nonnull PensumFachstelle target, @Nonnull AntragCopyType copyType) {
		super.copyAbstractPensumEntity(target, copyType);
		target.setFachstelle(this.getFachstelle());
		target.setIntegrationTyp(this.getIntegrationTyp());
		return target;
	}

	@Override
	public boolean isSame(AbstractEntity other) {
		//noinspection ObjectEquality
		if (this == other) {
			return true;
		}
		//noinspection SimplifiableIfStatement
		if (other == null || !getClass().equals(other.getClass())) {
			return false;
		}
		if (!super.isSame(other)) {
			return false;
		}
		if (!(other instanceof PensumFachstelle)) {
			return false;
		}
		final PensumFachstelle otherPensumFachstelle = (PensumFachstelle) other;
		return EbeguUtil.isSame(getFachstelle(), otherPensumFachstelle.getFachstelle())
			&& getIntegrationTyp() == otherPensumFachstelle.getIntegrationTyp();
	}

	@Nullable
	public Fachstelle getFachstelle() {
		return fachstelle;
	}

	public void setFachstelle(@Nullable Fachstelle fachstelle) {
		this.fachstelle = fachstelle;
	}

	@Nonnull
	public IntegrationTyp getIntegrationTyp() {
		return integrationTyp;
	}

	public void setIntegrationTyp(IntegrationTyp integrationTyp) {
		this.integrationTyp = integrationTyp;
	}
}
