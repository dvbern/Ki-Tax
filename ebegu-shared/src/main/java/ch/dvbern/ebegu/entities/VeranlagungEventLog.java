/*
 * Copyright (C) 2022 DV Bern AG, Switzerland
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

import java.time.LocalDate;
import java.util.Objects;

import javax.persistence.Column;
import javax.persistence.Entity;

import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import lombok.Getter;
import lombok.Setter;

@Entity
@Table
@Getter
@Setter
public class VeranlagungEventLog extends AbstractEntity {

	private static final long serialVersionUID = -6291354522204281488L;

	@NotNull
	@Column(nullable = false)
	private String antragId;

	@NotNull
	private Integer zpvNummer;

	@NotNull
	private LocalDate geburtsdatum;

	@NotNull
	private Integer gesuchsperiodeBeginnJahr;

	private String result;

	public VeranlagungEventLog(
		String antragId,
		Integer zpvNummer,
		LocalDate geburtsdatum,
		Integer gesuchsperiodeBeginnJahr) {
		this.antragId = antragId;
		this.zpvNummer = zpvNummer;
		this.geburtsdatum = geburtsdatum;
		this.gesuchsperiodeBeginnJahr = gesuchsperiodeBeginnJahr;
	}

	public VeranlagungEventLog() {

	}

	@Override
	public boolean isSame(AbstractEntity other) {
		//noinspection ObjectEquality
		if (this == other) {
			return true;
		}
		if (!(other instanceof VeranlagungEventLog)) {
			return false;
		}
		if (!super.equals(other)) {
			return false;
		}
		VeranlagungEventLog veranlagungEventLog = (VeranlagungEventLog) other;
		return Objects.equals(this.antragId, veranlagungEventLog.antragId) &&
				Objects.equals(geburtsdatum, veranlagungEventLog.geburtsdatum) &&
				Objects.equals(zpvNummer, veranlagungEventLog.zpvNummer) &&
				Objects.equals(gesuchsperiodeBeginnJahr, veranlagungEventLog.gesuchsperiodeBeginnJahr) &&
				Objects.equals(result, veranlagungEventLog.result);

	}
}
