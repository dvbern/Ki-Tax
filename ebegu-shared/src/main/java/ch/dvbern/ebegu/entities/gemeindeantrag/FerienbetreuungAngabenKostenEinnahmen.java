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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package ch.dvbern.ebegu.entities.gemeindeantrag;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import javax.annotation.Nullable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.validation.constraints.Size;

import ch.dvbern.ebegu.entities.AbstractEntity;
import ch.dvbern.ebegu.util.Constants;
import org.hibernate.envers.Audited;

@Entity
@Audited
public class FerienbetreuungAngabenKostenEinnahmen extends AbstractEntity {

	private static final long serialVersionUID = -3214159076031094829L;

	@Nullable
	@Column()
	private BigDecimal personalkosten;

	@Nullable
	@Column()
	private BigDecimal personalkostenLeitungAdmin;

	@Nullable
	@Column()
	private BigDecimal sachkosten;

	@Nullable
	@Column()
	private BigDecimal verpflegungskosten;

	@Nullable
	@Column()
	private BigDecimal weitereKosten;

	@Size(max = Constants.DB_TEXTAREA_LENGTH)
	@Nullable
	@Column(length = Constants.DB_TEXTAREA_LENGTH)
	private String bemerkungenKosten;

	@Nullable
	@Column()
	private BigDecimal elterngebuehren;

	@Nullable
	@Column()
	private BigDecimal weitereEinnahmen;

	@Nullable
	public BigDecimal getPersonalkosten() {
		return personalkosten;
	}

	public void setPersonalkosten(@Nullable BigDecimal personalkosten) {
		this.personalkosten = personalkosten;
	}

	@Nullable
	public BigDecimal getPersonalkostenLeitungAdmin() {
		return personalkostenLeitungAdmin;
	}

	public void setPersonalkostenLeitungAdmin(@Nullable BigDecimal personalkostenLeitungAdmin) {
		this.personalkostenLeitungAdmin = personalkostenLeitungAdmin;
	}

	@Nullable
	public BigDecimal getSachkosten() {
		return sachkosten;
	}

	public void setSachkosten(@Nullable BigDecimal sachkosten) {
		this.sachkosten = sachkosten;
	}

	@Nullable
	public BigDecimal getVerpflegungskosten() {
		return verpflegungskosten;
	}

	public void setVerpflegungskosten(@Nullable BigDecimal verpflegungskosten) {
		this.verpflegungskosten = verpflegungskosten;
	}

	@Nullable
	public BigDecimal getWeitereKosten() {
		return weitereKosten;
	}

	public void setWeitereKosten(@Nullable BigDecimal weitereKosten) {
		this.weitereKosten = weitereKosten;
	}

	@Nullable
	public String getBemerkungenKosten() {
		return bemerkungenKosten;
	}

	public void setBemerkungenKosten(@Nullable String bemerkungenKosten) {
		this.bemerkungenKosten = bemerkungenKosten;
	}

	@Nullable
	public BigDecimal getElterngebuehren() {
		return elterngebuehren;
	}

	public void setElterngebuehren(@Nullable BigDecimal elterngebuehren) {
		this.elterngebuehren = elterngebuehren;
	}

	@Nullable
	public BigDecimal getWeitereEinnahmen() {
		return weitereEinnahmen;
	}

	public void setWeitereEinnahmen(@Nullable BigDecimal weitereEinnahmen) {
		this.weitereEinnahmen = weitereEinnahmen;
	}

	@Override
	public boolean isSame(AbstractEntity other) {
		return getId().equals(other.getId());
	}

	public boolean isReadyForFreigeben() {
		return checkPropertiesNotNull();
	}

	private boolean checkPropertiesNotNull() {
		List<Serializable> nonNullObj = Arrays.asList(
			this.personalkosten,
			this.sachkosten,
			this.verpflegungskosten,
			this.elterngebuehren,
			this.weitereEinnahmen
		);
		return nonNullObj.stream()
			.anyMatch(Objects::isNull);
	}
}
