/*
 * Ki-Tax: System for the management of external childcare subsidies
 * Copyright (C) 2017 City of Bern Switzerland
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package ch.dvbern.ebegu.entities;

import java.time.LocalDateTime;
import java.util.Objects;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.ForeignKey;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.PrePersist;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import ch.dvbern.ebegu.enums.UserRole;
import org.apache.commons.lang3.builder.ToStringBuilder;

import static ch.dvbern.ebegu.util.Constants.DB_DEFAULT_MAX_LENGTH;

@Entity
@Table(indexes = {
	@Index(columnList = "benutzer_id", name = "IX_authorisierter_benutzer"),
	@Index(columnList = "authToken,benutzer_id", name = "IX_authorisierter_benutzer_token")
})
public class AuthorisierterBenutzer extends AbstractMutableEntity {

	private static final long serialVersionUID = 6372688971724279665L;

	@Column(nullable = false, updatable = false)
	private final LocalDateTime firstLogin = LocalDateTime.now();

	@Column(nullable = false)
	private LocalDateTime lastLogin = LocalDateTime.now();

	@ManyToOne(optional = false, fetch = FetchType.LAZY)
	@JoinColumn(foreignKey = @ForeignKey(name = "FK_authorisierter_benutzer_benutzer_id"))
	private Benutzer benutzer = null;

	/**
	 * Dies entspricht dem token aus dem cookie
	 */
	@NotNull
	@Column(updatable = false)
	@Size(min = 1, max = DB_DEFAULT_MAX_LENGTH)
	private String authToken = null;

	/**
	 * Wiederholung von Benutzer.username damit wir nicht joinen muessen
	 */
	@NotNull
	@Column(nullable = false)
	@Size(min = 1, max = DB_DEFAULT_MAX_LENGTH)
	private String username = null;

	/**
	 * Wiederholung von benutzer.role damit wir nicht joinen muessen
	 */
	@NotNull
	@Enumerated(EnumType.STRING)
	@Column(updatable = false, nullable = false)
	private UserRole role;

	@Nullable
	@Column(nullable = true)
	@Size(min = 1, max = DB_DEFAULT_MAX_LENGTH)
	private String sessionIndex;

	@Nullable
	@Column(nullable = true)
	@Size(min = 1, max = DB_DEFAULT_MAX_LENGTH)
	private String samlNameId;

	@Nullable
	@Column(nullable = true)
	@Size(min = 1, max = DB_DEFAULT_MAX_LENGTH)
	private String samlSPEntityID;

	@Nullable
	@Column(nullable = true)
	@Size(min = 1, max = DB_DEFAULT_MAX_LENGTH)
	private String samlIDPEntityID;

	@PrePersist
	protected void prePersist() {
		lastLogin = LocalDateTime.now();
	}

	public Benutzer getBenutzer() {
		return benutzer;
	}

	public void setBenutzer(Benutzer benutzer) {
		this.benutzer = benutzer;
	}

	@Nonnull
	public String getAuthToken() {
		return authToken;
	}

	public void setAuthToken(@Nonnull final String authToken) {
		this.authToken = authToken;
	}

	@Nonnull
	public LocalDateTime getFirstLogin() {
		return firstLogin;
	}

	@Nonnull
	public LocalDateTime getLastLogin() {
		return lastLogin;
	}

	public void setLastLogin(@Nonnull final LocalDateTime lastLogin) {
		this.lastLogin = lastLogin;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public UserRole getRole() {
		return role;
	}

	public void setRole(UserRole role) {
		this.role = role;
	}

	@Nullable
	public String getSessionIndex() {
		return sessionIndex;
	}

	public void setSessionIndex(@Nullable String sessionIndex) {
		this.sessionIndex = sessionIndex;
	}

	@Nullable
	public String getSamlNameId() {
		return samlNameId;
	}

	public void setSamlNameId(@Nullable String samlNameId) {
		this.samlNameId = samlNameId;
	}

	@Nullable
	public String getSamlSPEntityID() {
		return samlSPEntityID;
	}

	public void setSamlSPEntityID(@Nullable String samlSPEntityID) {
		this.samlSPEntityID = samlSPEntityID;
	}

	@Nullable
	public String getSamlIDPEntityID() {
		return samlIDPEntityID;
	}

	public void setSamlIDPEntityID(@Nullable String samlIDPEntityID) {
		this.samlIDPEntityID = samlIDPEntityID;
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this)
			.append("username", username)
			.append("role", role)
			.append("sessionIndex", sessionIndex)
			.toString();
	}

	@SuppressWarnings({ "OverlyComplexBooleanExpression", "OverlyComplexMethod" })
	@Override
	public boolean isSame(AbstractEntity other) {
		//noinspection ObjectEquality
		if (this == other) {
			return true;
		}
		if (other == null || !getClass().equals(other.getClass())) {
			return false;
		}
		if (!(other instanceof AuthorisierterBenutzer)) {
			return false;
		}
		final AuthorisierterBenutzer otherAuthorisierterBenutzer = (AuthorisierterBenutzer) other;
		return Objects.equals(getFirstLogin(), otherAuthorisierterBenutzer.getFirstLogin()) &&
			Objects.equals(getLastLogin(), otherAuthorisierterBenutzer.getLastLogin()) &&
			Objects.equals(getAuthToken(), otherAuthorisierterBenutzer.getAuthToken()) &&
			Objects.equals(getUsername(), otherAuthorisierterBenutzer.getUsername()) &&
			getRole() == otherAuthorisierterBenutzer.getRole() &&
			Objects.equals(getSessionIndex(), otherAuthorisierterBenutzer.getSessionIndex()) &&
			Objects.equals(getSamlNameId(), otherAuthorisierterBenutzer.getSamlNameId()) &&
			Objects.equals(getSamlIDPEntityID(), otherAuthorisierterBenutzer.getSamlIDPEntityID()) &&
			Objects.equals(getSamlSPEntityID(), otherAuthorisierterBenutzer.getSamlSPEntityID());
	}
}
