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
import javax.persistence.Column;
import javax.persistence.MappedSuperclass;
import javax.validation.constraints.NotNull;

import ch.dvbern.ebegu.enums.AntragCopyType;
import org.hibernate.envers.Audited;

@MappedSuperclass
@Audited
public class AbstractMahlzeitenPensum extends AbstractDecimalPensum {

	private static final long serialVersionUID = 7183887010325524679L;

	@NotNull
	@Column(nullable = false)
	private Integer monatlicheHauptmahlzeiten = 0;

	@NotNull
	@Column(nullable = false)
	private Integer monatlicheNebenmahlzeiten = 0;

	@NotNull
	@Column(nullable = false)
	private Integer tarifProHauptmahlzeit = 0;

	@NotNull
	@Column(nullable = false)
	private Integer tarifProNebenmahlzeit = 0;

	@Nonnull
	public Integer getMonatlicheHauptmahlzeiten() {
		return monatlicheHauptmahlzeiten;
	}

	public void setMonatlicheHauptmahlzeiten(@Nonnull Integer monatlicheHauptmahlzeiten) {
		this.monatlicheHauptmahlzeiten = monatlicheHauptmahlzeiten;
	}

	@Nonnull
	public Integer getMonatlicheNebenmahlzeiten() {
		return monatlicheNebenmahlzeiten;
	}

	public void setMonatlicheNebenmahlzeiten(@Nonnull Integer monatlicheNebenmahlzeiten) {
		this.monatlicheNebenmahlzeiten = monatlicheNebenmahlzeiten;
	}

	public Integer getTarifProHauptmahlzeit() {
		return tarifProHauptmahlzeit;
	}

	public void setTarifProHauptmahlzeit(Integer tarifProHauptmahlzeit) {
		this.tarifProHauptmahlzeit = tarifProHauptmahlzeit;
	}

	public Integer getTarifProNebenmahlzeit() {
		return tarifProNebenmahlzeit;
	}

	public void setTarifProNebenmahlzeit(Integer tarifProNebenmahlzeit) {
		this.tarifProNebenmahlzeit = tarifProNebenmahlzeit;
	}

	@Nonnull
	public void copyAbstractBetreuungspensumMahlzeitenEntity(@Nonnull AbstractMahlzeitenPensum target,
		@Nonnull AntragCopyType copyType) {
		super.copyAbstractBetreuungspensumEntity(target, copyType);
		switch (copyType) {
		case MUTATION:
			target.setMonatlicheHauptmahlzeiten(this.getMonatlicheHauptmahlzeiten());
			target.setMonatlicheNebenmahlzeiten(this.getMonatlicheNebenmahlzeiten());
			target.setTarifProHauptmahlzeit(this.getTarifProHauptmahlzeit());
			target.setTarifProNebenmahlzeit(this.getTarifProNebenmahlzeit());
			break;
		case ERNEUERUNG:
		case MUTATION_NEUES_DOSSIER:
		case ERNEUERUNG_NEUES_DOSSIER:
			break;
		}
	}
}
