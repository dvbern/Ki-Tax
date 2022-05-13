/*
 * Copyright (C) 2018 DV Bern AG, Switzerland
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

import java.util.Objects;

import javax.annotation.Nonnull;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import ch.dvbern.ebegu.enums.AntragCopyType;
import org.hibernate.envers.Audited;

import static ch.dvbern.ebegu.util.Constants.DB_TEXTAREA_LENGTH;

/**
 * Entity fuer das Pensum eines ausserordentlichen Anspruchs.
 */
@Audited
@Entity
public class PensumAusserordentlicherAnspruch extends AbstractIntegerPensum {

	private static final long serialVersionUID = -9132257320978374570L;

	@Size(min = 1, max = DB_TEXTAREA_LENGTH)
	@Column(nullable = false)
	@NotNull
	private String begruendung;

	public PensumAusserordentlicherAnspruch() {
	}

	public String getBegruendung() {
		return begruendung;
	}

	public void setBegruendung(String begruendung) {
		this.begruendung = begruendung;
	}

	@Nonnull
	public PensumAusserordentlicherAnspruch copyPensumAusserordentlicherAnspruch(
		@Nonnull PensumAusserordentlicherAnspruch target,
		@Nonnull AntragCopyType copyType
	) {
		super.copyAbstractPensumEntity(target, copyType);
		target.setBegruendung(this.getBegruendung());
		return target;
	}

	@Override
	@SuppressWarnings("PMD.CompareObjectsWithEquals")
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
		final PensumAusserordentlicherAnspruch otherPensumFachstelle = (PensumAusserordentlicherAnspruch) other;
		return Objects.equals(this.getBegruendung(), otherPensumFachstelle.getBegruendung());
	}
}
