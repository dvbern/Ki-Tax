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
import javax.persistence.Column;
import javax.persistence.MappedSuperclass;

import ch.dvbern.ebegu.enums.AntragCopyType;
import org.hibernate.envers.Audited;

@MappedSuperclass
@Audited
public class AbstractMahlzeitenPensum extends AbstractDecimalPensum {

	private static final long serialVersionUID = 7183887010325524679L;

	@Nullable
	@Column(nullable = true)
	private Integer monatlicheHauptmahlzeiten;

	@Nullable
	@Column(nullable = true)
	private Integer monatlicheNebenmahlzeiten;

	@Nullable
	public Integer getMonatlicheHauptmahlzeiten() {
		return monatlicheHauptmahlzeiten;
	}

	public void setMonatlicheHauptmahlzeiten(@Nullable Integer monatlicheHauptmahlzeiten) {
		this.monatlicheHauptmahlzeiten = monatlicheHauptmahlzeiten;
	}

	@Nullable
	public Integer getMonatlicheNebenmahlzeiten() {
		return monatlicheNebenmahlzeiten;
	}

	public void setMonatlicheNebenmahlzeiten(@Nullable Integer monatlicheNebenmahlzeiten) {
		this.monatlicheNebenmahlzeiten = monatlicheNebenmahlzeiten;
	}

	@Nonnull
	public void copyAbstractBetreuungspensumMahlzeitenEntity(@Nonnull AbstractMahlzeitenPensum target,
		@Nonnull AntragCopyType copyType) {
		super.copyAbstractBetreuungspensumEntity(target, copyType);
		switch (copyType) {
		case MUTATION:
			target.setMonatlicheHauptmahlzeiten(this.getMonatlicheHauptmahlzeiten());
			target.setMonatlicheNebenmahlzeiten(this.getMonatlicheNebenmahlzeiten());
			break;
		case ERNEUERUNG:
		case MUTATION_NEUES_DOSSIER:
		case ERNEUERUNG_NEUES_DOSSIER:
			break;
		}
	}
}
