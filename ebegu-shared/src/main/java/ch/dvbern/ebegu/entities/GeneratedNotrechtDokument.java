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

import java.util.Arrays;

import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.ForeignKey;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.hibernate.envers.Audited;

/**
 * Entitaet zum Speichern von GeneratedNotrechtDokument in der Datenbank.
 */
@Audited
@Entity
@EntityListeners(WriteProtectedDokumentListener.class)
public class GeneratedNotrechtDokument extends WriteProtectedDokument {

	private static final long serialVersionUID = -895840426576485097L;

	@NotNull
	@ManyToOne(optional = false)
	@JoinColumn(foreignKey = @ForeignKey(name = "FK_generated_dokument_rueckforderung_formular_id"), nullable = false)
	private RueckforderungFormular rueckforderungFormular;

	@Transient
	private byte[] content;

	public GeneratedNotrechtDokument() {
	}

	public RueckforderungFormular getRueckforderungFormular() {
		return rueckforderungFormular;
	}

	public void setRueckforderungFormular(RueckforderungFormular rueckforderungFormular) {
		this.rueckforderungFormular = rueckforderungFormular;
	}

	public byte[] getContent() {
		return Arrays.copyOf(content, content.length);
	}

	public void setContent(byte[] content) {
		this.content = Arrays.copyOf(content, content.length);
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this)
			.appendSuper(super.toString())
			.append("rueckforderungFormular", rueckforderungFormular)
			.toString();
	}
}
