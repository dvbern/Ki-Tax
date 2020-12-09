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

package ch.dvbern.ebegu.entities.gemeindeantrag;

import java.time.LocalDateTime;
import java.util.Objects;

import javax.annotation.Nonnull;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.ForeignKey;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.validation.constraints.NotNull;

import ch.dvbern.ebegu.entities.AbstractEntity;
import ch.dvbern.ebegu.entities.Benutzer;
import ch.dvbern.ebegu.enums.gemeindeantrag.LastenausgleichTagesschuleAngabenGemeindeStatus;
import org.hibernate.envers.Audited;

@Audited
@Entity
public class LastenausgleichTagesschuleAngabenGemeindeStatusHistory extends AbstractEntity {

	private static final long serialVersionUID = 7772645713958975926L;

	@NotNull @Nonnull
	@ManyToOne(optional = false)
	@JoinColumn(foreignKey = @ForeignKey(name = "FK_lats_statushistory_fall_id"), nullable = false)
	private LastenausgleichTagesschuleAngabenGemeindeContainer angabenGemeindeContainer;

	@NotNull @Nonnull
	@ManyToOne(optional = false)
	@JoinColumn(foreignKey = @ForeignKey(name = "FK_lats_statushistory_benutzer_id"), nullable = false)
	private Benutzer benutzer;

	@NotNull @Nonnull
	@Column(nullable = false)
	private LocalDateTime timestampVon;

	@Column(nullable = true)
	private LocalDateTime timestampBis;

	@NotNull @Nonnull
	@Column(nullable = false)
	@Enumerated(EnumType.STRING)
	private LastenausgleichTagesschuleAngabenGemeindeStatus status;

	@Nonnull
	public LastenausgleichTagesschuleAngabenGemeindeContainer getAngabenGemeindeContainer() {
		return angabenGemeindeContainer;
	}

	public void setAngabenGemeindeContainer(@Nonnull LastenausgleichTagesschuleAngabenGemeindeContainer fall) {
		this.angabenGemeindeContainer = fall;
	}

	@Nonnull
	public Benutzer getBenutzer() {
		return benutzer;
	}

	public void setBenutzer(@Nonnull Benutzer benutzer) {
		this.benutzer = benutzer;
	}

	@Nonnull
	public LocalDateTime getTimestampVon() {
		return timestampVon;
	}

	public void setTimestampVon(@Nonnull LocalDateTime timestampVon) {
		this.timestampVon = timestampVon;
	}

	public LocalDateTime getTimestampBis() {
		return timestampBis;
	}

	public void setTimestampBis(LocalDateTime timestampBis) {
		this.timestampBis = timestampBis;
	}

	@Nonnull
	public LastenausgleichTagesschuleAngabenGemeindeStatus getStatus() {
		return status;
	}

	public void setStatus(@Nonnull LastenausgleichTagesschuleAngabenGemeindeStatus status) {
		this.status = status;
	}

	@Override
	public boolean isSame(AbstractEntity other) {
		//noinspection ObjectEquality
		if (this == other) {
			return true;
		}
		if (!(other instanceof LastenausgleichTagesschuleAngabenGemeindeStatusHistory)) {
			return false;
		}
		if (!super.equals(other)) {
			return false;
		}
		LastenausgleichTagesschuleAngabenGemeindeStatusHistory that = (LastenausgleichTagesschuleAngabenGemeindeStatusHistory) other;
		return getAngabenGemeindeContainer().equals(that.getAngabenGemeindeContainer()) &&
			getBenutzer().equals(that.getBenutzer()) &&
			getTimestampVon().equals(that.getTimestampVon()) &&
			Objects.equals(getTimestampBis(), that.getTimestampBis()) &&
			getStatus() == that.getStatus();
	}
}
