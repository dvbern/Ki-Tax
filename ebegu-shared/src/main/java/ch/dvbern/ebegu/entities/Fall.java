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

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ForeignKey;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

import ch.dvbern.ebegu.util.Constants;
import org.apache.commons.lang.StringUtils;
import org.hibernate.envers.Audited;
import org.hibernate.search.annotations.Analyzer;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.FieldBridge;
import org.hibernate.search.annotations.Indexed;
import org.hibernate.search.annotations.IndexedEmbedded;
import org.hibernate.search.bridge.builtin.LongBridge;

/**
 * Entitaet zum Speichern von Fall in der Datenbank.
 */
@Audited
@Entity
@Table(
	uniqueConstraints = {
		@UniqueConstraint(columnNames = "fallNummer", name = "UK_fall_nummer"),
		@UniqueConstraint(columnNames = "besitzer_id", name = "UK_fall_besitzer"),
		@UniqueConstraint(columnNames = "sozialdienst_fall_id", name = "UK_fall_sozialdienst_fall_id")
	},
	indexes = {
		@Index(name = "IX_fall_fall_nummer", columnList = "fallNummer"),
		@Index(name = "IX_fall_besitzer", columnList = "besitzer_id"),
		@Index(name = "IX_fall_mandant", columnList = "mandant_id")
	}
)
@Indexed
@Analyzer(definition = "EBEGUGermanAnalyzer")
public class Fall extends AbstractMutableEntity implements HasMandant {

	private static final long serialVersionUID = -9154456879261811678L;

	@NotNull
	@Column(nullable = false)
	@Min(1)
	@Field(bridge = @FieldBridge(impl = LongBridge.class))
	private long fallNummer = 1;

	@Nullable
	@ManyToOne(optional = true)
	@JoinColumn(foreignKey = @ForeignKey(name = "FK_fall_besitzer_id"))
	@IndexedEmbedded
	private Benutzer besitzer = null; // Erfassender (im IAM eingeloggter) Gesuchsteller

	@Nullable
	@OneToOne(optional = true, cascade = CascadeType.ALL, orphanRemoval = true)
	@JoinColumn(foreignKey = @ForeignKey(name = "FK_fall_sozialdienst_fall_id"), nullable = true)
	private SozialdienstFall sozialdienstFall;

	/**
	 * nextNumberKind ist die Nummer, die das naechste Kind bekommen wird. Aus diesem Grund ist es by default 1
	 * Dieses Feld darf nicht mit der Anzahl der Kinder verwechselt werden, da sie sehr unterschiedlich sein koennen falls mehrere Kinder geloescht wurden
	 */
	@NotNull
	@Min(1)
	@Column(nullable = false)
	private Integer nextNumberKind = 1;

	@NotNull
	@ManyToOne(optional = false)
	@JoinColumn(foreignKey = @ForeignKey(name = "FK_fall_mandant_id"))
	private Mandant mandant;

	public long getFallNummer() {
		return fallNummer;
	}

	public void setFallNummer(long fallNummer) {
		this.fallNummer = fallNummer;
	}

	@Nullable
	public Benutzer getBesitzer() {
		return besitzer;
	}

	public void setBesitzer(@Nullable Benutzer besitzer) {
		this.besitzer = besitzer;
	}

	public Integer getNextNumberKind() {
		return nextNumberKind;
	}

	public void setNextNumberKind(Integer nextNumberKind) {
		this.nextNumberKind = nextNumberKind;
	}

	@Override
	public Mandant getMandant() {
		return mandant;
	}

	public void setMandant(Mandant mandant) {
		this.mandant = mandant;
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
		if (!(other instanceof Fall)) {
			return false;
		}
		final Fall otherFall = (Fall) other;
		return Objects.equals(getFallNummer(), otherFall.getFallNummer());
	}

	@Transient
	public String getPaddedFallnummer() {
		return StringUtils.leftPad(String.valueOf(this.getFallNummer()), Constants.FALLNUMMER_LENGTH, '0');
	}

	@Override
	public String getMessageForAccessException() {
		return "fallNummer: " + this.getFallNummer()
			+ ", besitzer: " + (this.getBesitzer() != null ? this.getBesitzer().getUsername() : "null");
	}

	@Nullable
	public SozialdienstFall getSozialdienstFall() {
		return sozialdienstFall;
	}

	public void setSozialdienstFall(@Nullable SozialdienstFall sozialdienstFall) {
		this.sozialdienstFall = sozialdienstFall;
	}
}
