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

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.validation.constraints.NotNull;

import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * Entitaet zum Speichern eines Allgemeinen Dokumentes (Ohne direkte Verlinkung im Modell) in der
 * Datenbank. Das Dokument hat einen Identifier.
 */
@Entity
@EntityListeners(WriteProtectedDokumentListener.class)
public class GeneratedGeneralDokument extends WriteProtectedDokument {

	private static final long serialVersionUID = 9124834795106980991L;

	@NotNull
	@Column(nullable = false)
	private String identifier;

	public GeneratedGeneralDokument() {
	}

	public String getIdentifier() {
		return identifier;
	}

	public void setIdentifier(String identifier) {
		this.identifier = identifier;
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this)
			.appendSuper(super.toString())
			.append("identifier", identifier)
			.toString();
	}
}
