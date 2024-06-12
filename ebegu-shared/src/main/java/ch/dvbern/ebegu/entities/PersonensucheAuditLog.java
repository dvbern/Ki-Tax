/*
 * Copyright (C)  2020 DV Bern AG, Switzerland
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
 *
 */

package ch.dvbern.ebegu.entities;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;

import ch.dvbern.ebegu.util.entitylisteners.PersonenSucheAuditLogListener;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * For data-security reasons we have to log who requests what data on our {@link PersonenSucheService}
 */
@Entity
@EntityListeners(PersonenSucheAuditLogListener.class)
public class PersonensucheAuditLog implements Serializable {

	private static final long serialVersionUID = 2033061799557303703L;


	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id;

	public PersonensucheAuditLog(
		@Nonnull String calledMethod,
		@Nonnull String residentInfoParameters,
		@Nonnull LocalDate validityDate,
		@Nullable String faultReceived,
		@Nullable Long totalNumberOfResults,
		@Nullable Long numResultsReceived,
		@Nonnull LocalDateTime timestampSearchstart,
		@Nonnull LocalDateTime endTime
	) {
		this.calledMethod = calledMethod;
		this.validityDate = validityDate;
		this.residentInfoParameters = residentInfoParameters;
		this.faultReceived = faultReceived;
		this.totalNumberOfResults = totalNumberOfResults;
		this.numResultsReceived = numResultsReceived;
		this.timestampSearchstart = timestampSearchstart;
		this.timestampResult = endTime;
	}


	@NotNull
	@Column(nullable = false)
	private String calledMethod;

	// mariadb does not yet have  native json support so we store this as a string
	@Lob
	@Column(nullable = false)
	@NotNull
	private String residentInfoParameters;

	@Column(nullable = false)
	private LocalDate validityDate;

	@Nullable
	@Column(nullable = true)
	private String faultReceived;

	@Nullable
	@Column(nullable = true)
	private Long totalNumberOfResults;

	@Column(nullable = true)
	@Nullable
	private Long numResultsReceived;


	@Column(nullable = false)
	private LocalDateTime timestampSearchstart;

	@NotNull
	@Column(nullable = false)
	private LocalDateTime timestampResult;   // auto filled on persist

	@Column(nullable = false)
	private String username;

	public long getId() {
		return id;
	}

	public String getResidentInfoParameters() {
		return residentInfoParameters;
	}

	public LocalDate getValidityDate() {
		return validityDate;
	}

	@Nullable
	public String getFaultReceived() {
		return faultReceived;
	}

	@Nullable
	public Long getTotalNumberOfResults() {
		return totalNumberOfResults;
	}

	@Nullable
	public Long getNumResultsReceived() {
		return numResultsReceived;
	}

	public LocalDateTime getTimestampSearchstart() {
		return timestampSearchstart;
	}

	public LocalDateTime getTimestampResult() {
		return timestampResult;
	}

	public void setTimestampResult(LocalDateTime timestampResult) {
		this.timestampResult = timestampResult;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getUsername() {
		return username;
	}

	@Transient
	public long getRequestDurationMilis(){
		if (getTimestampSearchstart() == null || getTimestampResult() == null) {
			return -1;
		}
		return ChronoUnit.MILLIS.between(getTimestampSearchstart(), getTimestampResult());
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this)
			.append("id", id)
			.append("calledMethod", calledMethod)
			.append("numResultsReceived", numResultsReceived)
			.append("timestampResult", timestampResult)
			.append("username", username)
			.toString();
	}
}
