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

import java.util.Objects;

import javax.annotation.Nullable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.ForeignKey;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import ch.dvbern.ebegu.util.Constants;
import ch.dvbern.ebegu.enums.ApplicationPropertyKey;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.envers.Audited;

import static ch.dvbern.ebegu.util.Constants.DB_DEFAULT_MAX_LENGTH;
import static ch.dvbern.ebegu.util.Constants.DB_TEXTAREA_LENGTH;

/**
 * Entitaet zum Speichern von diversen Applikationsproperties in der Datenbank.
 */
@Audited
@Entity
@Table(
	uniqueConstraints = @UniqueConstraint(columnNames = {"name", "mandant_id"}, name = "UK_application_property_name")
)
public class ApplicationProperty extends AbstractMutableEntity implements HasMandant {

	private static final long serialVersionUID = -7687645920282879260L;
	@NotNull
	@Column(nullable = false, length = DB_DEFAULT_MAX_LENGTH)
	@Enumerated(EnumType.STRING)
	private ApplicationPropertyKey name;

	@Size(max = DB_TEXTAREA_LENGTH)
	@NotNull
	@Column(nullable = false, length = DB_TEXTAREA_LENGTH)
	private String value;


	@NotNull
	@ManyToOne(optional = false)
	@JoinColumn(foreignKey = @ForeignKey(name = "FK_application_property_mandant_id"))
	private Mandant mandant;

	@Nullable
	@Column(nullable = false, length = DB_TEXTAREA_LENGTH)
	private String erklaerung;

	public ApplicationProperty() {
	}

	public ApplicationProperty(final ApplicationPropertyKey key, final String value) {
		this.name = key;
		this.value = value;
	}

	public ApplicationProperty(ApplicationPropertyKey key, String value, Mandant mandant) {
		this(key, value);
		this.mandant = mandant;
	}

	@Override
	@SuppressWarnings("PMD.CompareObjectsWithEquals")
	@SuppressFBWarnings("BC_UNCONFIRMED_CAST")
	public boolean isSame(AbstractEntity other) {
		//noinspection ObjectEquality
		if (this == other) {
			return true;
		}
		if (other == null || !getClass().equals(other.getClass())) {
			return false;
		}
		final ApplicationProperty otherApplicationProperty = (ApplicationProperty) other;
		return getName() == otherApplicationProperty.getName() &&
			Objects.equals(getValue(), otherApplicationProperty.getValue()) &&
			StringUtils.equals(getErklaerung(), otherApplicationProperty.getErklaerung());
	}

	public ApplicationPropertyKey getName() {
		return name;
	}

	public void setName(final ApplicationPropertyKey name) {
		this.name = name;
	}

	public String getValue() {
		return value;
	}

	public void setValue(final String value) {
		this.value = value;
	}

	@NotNull
	@Override
	public Mandant getMandant() {
		return mandant;
	}

	@Override
	public void setMandant(Mandant mandant) {
		this.mandant = mandant;
	}

	@Nullable
	public String getErklaerung() {
		return erklaerung;
	}

	public void setErklaerung(@Nullable final String erklaerung) {
		this.erklaerung = erklaerung;
	}
}
