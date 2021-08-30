/*
 * Copyright (C) 2018 DV Bern AG, Switzerland
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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.ForeignKey;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.validation.constraints.NotNull;

import ch.dvbern.ebegu.enums.EinstellungKey;
import ch.dvbern.ebegu.util.Constants;
import ch.dvbern.ebegu.util.DateUtil;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.hibernate.envers.Audited;

/**
 * Entitaet zum Speichern von zeitabhängigen Einstellungen auf Stufe System, Mandant oder Gemeinde in Ki-Tax
 */
@Audited
@Entity
public class Einstellung extends AbstractEntity implements HasMandant {

	private static final long serialVersionUID = 8704632842261673111L;

	@NotNull
	@Column(name="einstellung_key", nullable = false, updatable = false, length = Constants.DB_DEFAULT_MAX_LENGTH)
	@Enumerated(EnumType.STRING)
	private EinstellungKey key;

	@NotNull
	@Column(nullable = false, length = Constants.DB_DEFAULT_MAX_LENGTH)
	private String value;

	@Nullable
	@ManyToOne(optional = true)
	@JoinColumn(updatable = false, foreignKey = @ForeignKey(name = "FK_einstellung_mandant_id"))
	private Mandant mandant;

	@Nullable
	@ManyToOne(optional = true)
	@JoinColumn(updatable = false, foreignKey = @ForeignKey(name = "FK_einstellung_gemeinde_id"))
	private Gemeinde gemeinde;

	@NotNull
	@Nonnull
	@ManyToOne(optional = false)
	@JoinColumn(updatable = false, foreignKey = @ForeignKey(name = "FK_einstellung_gesuchsperiode_id"))
	private Gesuchsperiode gesuchsperiode;

	public Einstellung() {
	}

	public Einstellung(@Nonnull EinstellungKey key, @Nonnull String value, @Nonnull Gesuchsperiode gesuchsperiode) {
		this.key = key;
		this.value = value;
		this.gesuchsperiode = gesuchsperiode;
	}

	public Einstellung(@Nonnull EinstellungKey key, @Nonnull String value, @Nonnull Gesuchsperiode gesuchsperiode,
			@Nullable Mandant mandant, @Nullable Gemeinde gemeinde) {
		this(key, value, gesuchsperiode);
		this.gemeinde = gemeinde;
		if (gemeinde != null) {
			this.mandant = gemeinde.getMandant();
		} else {
			this.mandant = mandant;
		}
	}

	public EinstellungKey getKey() {
		return key;
	}

	public void setKey(EinstellungKey key) {
		this.key = key;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	@Nullable
	public Mandant getMandant() {
		return mandant;
	}

	public void setMandant(@Nullable Mandant mandant) {
		this.mandant = mandant;
	}

	@Nullable
	public Gemeinde getGemeinde() {
		return gemeinde;
	}

	public void setGemeinde(@Nullable Gemeinde gemeinde) {
		this.gemeinde = gemeinde;
	}

	@Nonnull
	public Gesuchsperiode getGesuchsperiode() {
		return gesuchsperiode;
	}

	public void setGesuchsperiode(@Nonnull Gesuchsperiode gesuchsperiode) {
		this.gesuchsperiode = gesuchsperiode;
	}

	/**
	 * Erstellt eine Kopie der Einstellung für eine neue Gesuchsperiode
	 */
	public Einstellung copyGesuchsperiode(@Nonnull Gesuchsperiode newGesuchsperiode) {
		return new Einstellung(this.getKey(), this.getValue(), newGesuchsperiode, this.getMandant(), this.getGemeinde());
	}

	/**
	 * Erstellt eine mandant-spezifische Kopie der Einstellung
	 */
	public Einstellung copyForMandant(@Nonnull Mandant newMandant) {
		return new Einstellung(this.getKey(), this.getValue(), this.getGesuchsperiode(), newMandant, null);
	}

	/**
	 * Erstellt eine gemeinde-spezifische Kopie der Einstellung
	 */
	public Einstellung copyForGemeinde(@Nonnull Gemeinde newGemeinde) {
		return new Einstellung(this.getKey(), this.getValue(), this.getGesuchsperiode(), newGemeinde.getMandant(), newGemeinde);
	}

	public BigDecimal getValueAsBigDecimal() {
		return new BigDecimal(value);
	}

	public Integer getValueAsInteger() {
		return Integer.valueOf(value);
	}

	public LocalDate getValueAsDate() {
		return DateUtil.parseStringToDate(value);
	}

	public Boolean getValueAsBoolean() {
		return Boolean.parseBoolean(value);
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
		if (!(other instanceof Einstellung)) {
			return false;
		}
		final Einstellung otherEinstellung = (Einstellung) other;
		return getKey() == otherEinstellung.getKey() &&
			Objects.equals(getValue(), otherEinstellung.getValue()) &&
			Objects.equals(getGesuchsperiode(), otherEinstellung.getGesuchsperiode()) &&
			Objects.equals(getMandant(), otherEinstellung.getMandant()) &&
			Objects.equals(getGemeinde(), otherEinstellung.getGemeinde());
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this)
			.append("key", key)
			.append("value", value)
			.append("mandant", mandant != null ? mandant.getName() : "null")
			.append("gemeinde", gemeinde != null ? gemeinde.getName() : "null")
			.append("gesuchsperiode", gesuchsperiode.getGesuchsperiodeString())
			.toString();
	}
}
