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

import ch.dvbern.ebegu.enums.AntragCopyType;
import ch.dvbern.ebegu.enums.GruendeZusatzleistung;
import ch.dvbern.ebegu.enums.IntegrationTyp;
import ch.dvbern.ebegu.util.EbeguUtil;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.apache.commons.lang3.builder.CompareToBuilder;
import org.hibernate.envers.Audited;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.persistence.*;
import javax.validation.constraints.NotNull;

/**
 * Entity fuer PensumFachstelle.
 */
@Audited
@Entity
public class PensumFachstelle extends AbstractIntegerPensum implements Comparable<PensumFachstelle> {

	private static final long serialVersionUID = -9132257320978374570L;

	@Nullable
	@ManyToOne(optional = true)
	@JoinColumn(foreignKey = @ForeignKey(name = "FK_pensum_fachstelle_fachstelle_id"))
	private Fachstelle fachstelle;

	@NotNull
	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private IntegrationTyp integrationTyp;

	@Nullable
	@Enumerated(EnumType.STRING)
	@Column(nullable = true)
	private GruendeZusatzleistung gruendeZusatzleistung;

	@NotNull
	@ManyToOne(optional = false)
	@JoinColumn(foreignKey = @ForeignKey(name = "FK_pensum_fachstelle_kind_id"))
	private Kind kind;

	public PensumFachstelle() {
	}

	public PensumFachstelle(Kind kind) {
		this.kind = kind;
	}

	@Nonnull
	public PensumFachstelle copyPensumFachstelle(@Nonnull PensumFachstelle target, @Nonnull AntragCopyType copyType) {
		super.copyAbstractPensumEntity(target, copyType);
		target.setFachstelle(this.getFachstelle());
		target.setIntegrationTyp(this.getIntegrationTyp());
		target.setGruendeZusatzleistung(this.getGruendeZusatzleistung());
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
		//noinspection SimplifiableIfStatement
		if (other == null || !getClass().equals(other.getClass())) {
			return false;
		}
		if (!super.isSame(other)) {
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

	public Kind getKind() {
		return this.kind;
	}

	public void setKind(@Nonnull Kind kind) {
		this.kind = kind;
	}

	@Nullable
	public GruendeZusatzleistung getGruendeZusatzleistung() {
		return gruendeZusatzleistung;
	}

	public void setGruendeZusatzleistung(@Nullable GruendeZusatzleistung gruendeZusatzleistung) {
		this.gruendeZusatzleistung = gruendeZusatzleistung;
	}

	@Override
	public int compareTo(@Nonnull PensumFachstelle o) {
		CompareToBuilder builder = new CompareToBuilder();
		builder.append(this.getGueltigkeit().getGueltigAb(), o.getGueltigkeit().getGueltigAb());
		builder.append(this.getId(), o.getId());
		return builder.toComparison();
	}
}
