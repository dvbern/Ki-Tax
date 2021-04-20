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

package ch.dvbern.ebegu.entities.sozialdienst;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Objects;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.ForeignKey;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import ch.dvbern.ebegu.entities.AbstractEntity;
import ch.dvbern.ebegu.entities.Adresse;
import ch.dvbern.ebegu.enums.SozialdienstFallStatus;
import org.hibernate.envers.Audited;

import static ch.dvbern.ebegu.util.Constants.DB_DEFAULT_MAX_LENGTH;
import static ch.dvbern.ebegu.util.Constants.TEN_MB;

@Audited
@Entity
@Table(
	uniqueConstraints =
		@UniqueConstraint(columnNames = "adresse_id", name = "UK_sozialdienst_fall_adresse_id")
)
public class SozialdienstFall extends AbstractEntity {

	private static final long serialVersionUID = -3978972308622826784L;
	private static final byte[] EMPTY_BYTE_ARRAY = new byte[0];

	@NotNull @Nonnull
	@ManyToOne(optional = false)
	@JoinColumn(foreignKey = @ForeignKey(name = "FK_sozialdienst_fall_sozialdienst_id"), nullable = false)
	private Sozialdienst sozialdienst;

	@NotNull @Nonnull
	@OneToOne(optional = false, cascade = CascadeType.ALL, orphanRemoval = true)
	@JoinColumn(foreignKey = @ForeignKey(name = "FK_sozialdienst_fall_adresse_id"), nullable = false)
	private Adresse adresse;

	@Size(min = 1, max = DB_DEFAULT_MAX_LENGTH)
	@Column(nullable = false)
	@NotNull
	@Nonnull
	private String name;

	@Size(min = 1, max = DB_DEFAULT_MAX_LENGTH)
	@Column(nullable = false)
	@NotNull
	@Nonnull
	private String vorname;

	@NotNull @Nonnull
	@Column(nullable = false)
	@Enumerated(EnumType.STRING)
	private SozialdienstFallStatus status = SozialdienstFallStatus.INAKTIV;

	@NotNull @Nonnull
	@Column(nullable = false)
	private LocalDate geburtsdatum;

	@Size(max = DB_DEFAULT_MAX_LENGTH)
	@Column(nullable = true)
	@Nullable
	private String nameGs2;

	@Size(max = DB_DEFAULT_MAX_LENGTH)
	@Column(nullable = true)
	@Nullable
	private String vornameGs2;

	@Nullable
	@Column(nullable = true)
	private LocalDate geburtsdatumGs2;

	@Nonnull
	public Sozialdienst getSozialdienst() {
		return sozialdienst;
	}

	public void setSozialdienst(@Nonnull Sozialdienst sozialdienst) {
		this.sozialdienst = sozialdienst;
	}

	@Nonnull
	public Adresse getAdresse() {
		return adresse;
	}

	public void setAdresse(@Nonnull Adresse adresse) {
		this.adresse = adresse;
	}

	@Nonnull
	public String getName() {
		return name;
	}

	public void setName(@Nonnull String name) {
		this.name = name;
	}

	@Nonnull
	public SozialdienstFallStatus getStatus() {
		return status;
	}

	public void setStatus(@Nonnull SozialdienstFallStatus status) {
		this.status = status;
	}

	@Nonnull
	public LocalDate getGeburtsdatum() {
		return geburtsdatum;
	}

	public void setGeburtsdatum(@Nonnull LocalDate geburtsdatum) {
		this.geburtsdatum = geburtsdatum;
	}

	@Nonnull
	public String getVorname() {
		return vorname;
	}

	public void setVorname(@Nonnull String vorname) {
		this.vorname = vorname;
	}

	@Override
	public boolean isSame(AbstractEntity other) {
		//noinspection ObjectEquality
		if (this == other) {
			return true;
		}
		if (!(other instanceof SozialdienstFall)){
			return false;
		}
		if (!super.equals(other)) {
			return false;
		}
		SozialdienstFall sozialdienstFall = (SozialdienstFall) other;
		return Objects.equals(this.getSozialdienst(), sozialdienstFall.getSozialdienst())
			&& Objects.equals(this.getStatus(), sozialdienstFall.getStatus())
			&& Objects.equals(this.getName(), sozialdienstFall.getName())
			&& Objects.equals(this.getVorname(), sozialdienstFall.getVorname())
			&& Objects.equals(this.getGeburtsdatum(), sozialdienstFall.getGeburtsdatum())
			&& Objects.equals(this.getAdresse(), sozialdienstFall.getAdresse())
			&& Objects.equals(this.getNameGs2(), sozialdienstFall.getNameGs2())
			&& Objects.equals(this.getVornameGs2(), sozialdienstFall.getVornameGs2())
			&& Objects.equals(this.getGeburtsdatumGs2(), sozialdienstFall.getGeburtsdatumGs2());
	}

	@Nullable
	public String getNameGs2() {
		return nameGs2;
	}

	public void setNameGs2(@Nullable String nameGs2) {
		this.nameGs2 = nameGs2;
	}

	@Nullable
	public String getVornameGs2() {
		return vornameGs2;
	}

	public void setVornameGs2(@Nullable String vornameGs2) {
		this.vornameGs2 = vornameGs2;
	}

	@Nullable
	public LocalDate getGeburtsdatumGs2() {
		return geburtsdatumGs2;
	}

	public void setGeburtsdatumGs2(@Nullable LocalDate geburtsdatumGs2) {
		this.geburtsdatumGs2 = geburtsdatumGs2;
	}
}
