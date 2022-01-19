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

import java.time.LocalDateTime;
import java.util.Objects;

import javax.annotation.Nullable;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.ForeignKey;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.validation.constraints.NotNull;

import ch.dvbern.ebegu.enums.SteuerdatenAnfrageStatus;
import org.apache.commons.lang3.StringUtils;

@Entity
public class SteuerdatenAnfrageLog extends AbstractEntity {

	private static final long serialVersionUID = -6447515549110267609L;

	@NotNull
	@Column(nullable = false)
	private LocalDateTime timestampSent;

	@NotNull
	@Column(nullable = false)
	@Enumerated(EnumType.STRING)
	private SteuerdatenAnfrageStatus status;

	@Nullable
	@Column(nullable = true)
	private String faultReceived;


	@NotNull
	@OneToOne(cascade = CascadeType.ALL)
	@JoinColumn(foreignKey = @ForeignKey(name = "FK_steuerdaten_anfrage_log_request"))
	private SteuerdatenRequest request;

	@Nullable
	@OneToOne(cascade = CascadeType.ALL)
	@JoinColumn(foreignKey = @ForeignKey(name = "FK_steuerdaten_anfrage_log_response"))
	private SteuerdatenResponse response;

	public SteuerdatenAnfrageLog(){}

	public SteuerdatenAnfrageLog(
		LocalDateTime timestampSent,
		SteuerdatenAnfrageStatus status,
		@Nullable String faultReceived,
		SteuerdatenRequest request,
		@Nullable SteuerdatenResponse response) {
		this.timestampSent = timestampSent;
		this.status = status;
		this.faultReceived = faultReceived;
		this.request = request;
		this.response = response;
	}

	public LocalDateTime getTimestampSent() {
		return timestampSent;
	}

	public void setTimestampSent(LocalDateTime timestampSendingRequest) {
		this.timestampSent = timestampSendingRequest;
	}

	public SteuerdatenAnfrageStatus getStatus() {
		return status;
	}

	public void setStatus(SteuerdatenAnfrageStatus status) {
		this.status = status;
	}

	@Nullable
	public String getFaultReceived() {
		return faultReceived;
	}

	public void setFaultReceived(@Nullable String faultReceived) {
		this.faultReceived = faultReceived;
	}

	public @Nullable
	SteuerdatenResponse getResponse() {
		return response;
	}

	public void setResponse(@Nullable SteuerdatenResponse response) {
		this.response = response;
	}

	public SteuerdatenRequest getRequest() {
		return request;
	}

	public void setRequest(SteuerdatenRequest request) {
		this.request = request;
	}

	@Override
	public boolean isSame(AbstractEntity other) {
		//noinspection ObjectEquality
		if (this == other) {
			return true;
		}
		if (other == null || !getClass().equals(other.getClass())) {
			return false;
		}

		if (!(other instanceof SteuerdatenAnfrageLog)) {
			return false;
		}

		final SteuerdatenAnfrageLog otherAnfrage = (SteuerdatenAnfrageLog) other;
		return StringUtils.equals(this.faultReceived, otherAnfrage.faultReceived) &&
			this.timestampSent.equals(otherAnfrage.timestampSent) &&
			Objects.equals(this.request, otherAnfrage.request) &&
			Objects.equals(this.response, otherAnfrage.response) &&
			this.status == otherAnfrage.status;
	}
}
