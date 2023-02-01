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

import javax.annotation.Nullable;
import javax.persistence.Entity;
import javax.persistence.ForeignKey;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.envers.Audited;

@Audited
@Entity
@Table
@Getter
@Setter
public class VeranlagungEventLog extends AbstractEntity {

	private static final long serialVersionUID = -6291354522204281488L;

	@Nullable
	@ManyToOne(optional = false)
	@JoinColumn(foreignKey = @ForeignKey(name = "FK_veranlagungsevent_log_antrag_id"), nullable = true)
	private Gesuch gesuch;

	@NotNull
	private Integer zpvNummer;

	@NotNull
	private LocalDate geburtsdatum;

	@NotNull
	private Integer gesuchsperiodeBeginnJahr;

	private String result;

	public VeranlagungEventLog(
			@Nullable Gesuch gesuch,
			Integer zpvNummer,
			LocalDate geburtsdatum,
			Integer gesuchsperiodeBeginnJahr) {
		this.gesuch = gesuch;
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
		return Objects.equals(this.gesuch, veranlagungEventLog.gesuch) &&
				Objects.equals(geburtsdatum, veranlagungEventLog.geburtsdatum) &&
				Objects.equals(zpvNummer, veranlagungEventLog.zpvNummer) &&
				Objects.equals(gesuchsperiodeBeginnJahr, veranlagungEventLog.gesuchsperiodeBeginnJahr) &&
				Objects.equals(result, veranlagungEventLog.result);

	}
}
