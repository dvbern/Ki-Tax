/*
 * Copyright (C) 2021 DV Bern AG, Switzerland
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

import java.time.LocalDateTime;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.validation.constraints.NotEmpty;

import com.google.common.base.Objects;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

@Entity
public class BetreuungMonitoring extends AbstractEntity {

	private static final long serialVersionUID = 4998440001747583997L;

	@Nonnull
	@Column(nullable = false, updatable = false)
	private final @NotEmpty String refNummer;

	@Nonnull
	@Column(nullable = false, updatable = false)
	private final @NotEmpty String benutzer;

	@Nonnull
	@Column(nullable = false, updatable = false)
	private final @NotEmpty String infoText;

	@Nonnull
	@Column(nullable = false, updatable = false)
	private final LocalDateTime timestamp;

	/**
	 * just for JPA
	 */
	@SuppressWarnings("ConstantConditions")
	@SuppressFBWarnings(value = "NP_STORE_INTO_NONNULL_FIELD", justification = "just for JPA")
	protected BetreuungMonitoring() {
		this.refNummer = "";
		this.benutzer = "";
		this.infoText = "";
		this.timestamp = null;
	}

	public BetreuungMonitoring(@Nonnull @NotEmpty String refNummer, @Nonnull @NotEmpty String benutzer,
		@Nonnull @NotEmpty String infoText,
		@Nonnull LocalDateTime timestamp) {
		this.refNummer = refNummer;
		this.benutzer = benutzer;
		this.infoText = infoText;
		this.timestamp = timestamp;
	}

	@Override
	public boolean isSame(AbstractEntity other) {
		return this.equals(other);
	}

	@Override
	public boolean equals(@Nullable Object o) {
		if (this == o) {
			return true;
		}

		if (o == null || !getClass().equals(o.getClass())) {
			return false;
		}

		if (!super.equals(o)) {
			return false;
		}

		BetreuungMonitoring that = (BetreuungMonitoring) o;

		return Objects.equal(getId(), that.getId());
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(
			super.hashCode(),
			getRefNummer(),
			getBenutzer(),
			getInfoText(),
			getTimestamp());
	}

	@Nonnull
	public String getRefNummer() {
		return refNummer;
	}

	@Nonnull
	public String getBenutzer() {
		return benutzer;
	}

	@Nonnull
	public String getInfoText() {
		return infoText;
	}

	@Nonnull
	public LocalDateTime getTimestamp() {
		return timestamp;
	}
}
