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

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.ForeignKey;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import ch.dvbern.ebegu.enums.betreuung.BetreuungsangebotTyp;
import ch.dvbern.ebegu.enums.ZahlungStatus;
import ch.dvbern.ebegu.errors.EbeguEntityNotFoundException;
import ch.dvbern.ebegu.util.Constants;
import ch.dvbern.ebegu.util.MathUtil;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.apache.commons.lang3.builder.CompareToBuilder;
import org.hibernate.annotations.Type;
import org.hibernate.envers.Audited;

import static ch.dvbern.ebegu.util.Constants.DB_DEFAULT_MAX_LENGTH;

/**
 * Entitaet zum Speichern von Zahlungen (=Auftrag fuer 1 Kita) in der Datenbank.
 */
@Audited
@Entity
public class Zahlung extends AbstractMutableEntity implements Comparable<Zahlung> {

	private static final long serialVersionUID = 8975199813240034719L;

	@NotNull
	@ManyToOne(optional = false)
	@JoinColumn(foreignKey = @ForeignKey(name = "FK_Zahlung_zahlungsauftrag_id"), nullable = false)
	private Zahlungsauftrag zahlungsauftrag;

	@NotNull @Nonnull
	@Column(nullable = false, length = 16)
	@Size(min = Constants.UUID_LENGTH, max = Constants.UUID_LENGTH)
	@Type( type = "string-uuid-binary" )
	private String empfaengerId;	// Kann fuer verschiedene Zahlungslauftypen etwas unterschiedliches bedeuten

	@NotNull @Nonnull
	@Column(nullable = false)
	@Size(min = 1, max = DB_DEFAULT_MAX_LENGTH)
	private String empfaengerName; 	// Kann fuer verschiedene Zahlungslauftypen etwas unterschiedliches bedeuten

	@Nullable
	@Column(nullable = true, name = "empfaenger_2_name")
	@Size(min = 1, max = DB_DEFAULT_MAX_LENGTH)
	private String empfaenger2Name; 	// Kann fuer verschiedene Zahlungslauftypen etwas unterschiedliches bedeuten

	@NotNull @Nonnull
	@Column(nullable = false)
	@Enumerated(EnumType.STRING)
	private  BetreuungsangebotTyp betreuungsangebotTyp;

	@Nullable
	@Column(nullable = true)
	private String traegerschaftName;

	@NotNull @Nonnull
	@ManyToOne(optional = false, cascade = CascadeType.ALL)
	@JoinColumn(foreignKey = @ForeignKey(name = "FK_zahlung_auszahlungsdaten_id"), nullable = false)
	private Auszahlungsdaten auszahlungsdaten;

	@NotNull
	@Column(nullable = false)
	@Enumerated(EnumType.STRING)
	private ZahlungStatus status;

	@Nonnull
	@Valid
	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true, mappedBy = "zahlung")
	private List<Zahlungsposition> zahlungspositionen = new ArrayList<>();

	@Nonnull
	private BigDecimal betragTotalZahlung;

	public Zahlungsauftrag getZahlungsauftrag() {
		return zahlungsauftrag;
	}

	public void setZahlungsauftrag(Zahlungsauftrag zahlungsauftrag) {
		this.zahlungsauftrag = zahlungsauftrag;
	}

	@Nonnull
	public String getEmpfaengerId() {
		return empfaengerId;
	}

	public void setEmpfaengerId(@Nonnull String institutionId) {
		this.empfaengerId = institutionId;
	}

	@Nonnull
	public String getEmpfaengerName() {
		return empfaengerName;
	}

	public void setEmpfaengerName(@Nonnull String institutionName) {
		this.empfaengerName = institutionName;
	}

	@Nullable
	public String getEmpfaenger2Name() {
		return empfaenger2Name;
	}

	public void setEmpfaenger2Name(@Nonnull String empfaenger2Name) {
		this.empfaenger2Name = empfaenger2Name;
	}

	@Nonnull
	public BetreuungsangebotTyp getBetreuungsangebotTyp() {
		return betreuungsangebotTyp;
	}

	public void setBetreuungsangebotTyp(@Nonnull BetreuungsangebotTyp betreuungsangebotTyp) {
		this.betreuungsangebotTyp = betreuungsangebotTyp;
	}

	@Nullable
	public String getTraegerschaftName() {
		return traegerschaftName;
	}

	public void setTraegerschaftName(@Nullable String traegerschaftName) {
		this.traegerschaftName = traegerschaftName;
	}

	@Nonnull
	public Auszahlungsdaten getAuszahlungsdaten() {
		return auszahlungsdaten;
	}

	public void setAuszahlungsdaten(@Nonnull Auszahlungsdaten auszahlungsdaten) {
		this.auszahlungsdaten = auszahlungsdaten;
	}

	public ZahlungStatus getStatus() {
		return status;
	}

	public void setStatus(ZahlungStatus status) {
		this.status = status;
	}

	@Nonnull
	public List<Zahlungsposition> getZahlungspositionen() {
		return zahlungspositionen;
	}

	public void setZahlungspositionen(@Nonnull List<Zahlungsposition> zahlungspositionen) {
		this.zahlungspositionen = zahlungspositionen;
	}

	@Nonnull
	public BigDecimal getBetragTotalZahlung() {
		return betragTotalZahlung;
	}

	public void setBetragTotalZahlung(@Nonnull BigDecimal betragTotalZahlung) {
		this.betragTotalZahlung = betragTotalZahlung;
	}

	@Override
	public int compareTo(@Nonnull Zahlung o) {
		CompareToBuilder builder = new CompareToBuilder();
		builder.append(this.getEmpfaengerName(), o.getEmpfaengerName());
		builder.append(this.getZahlungsauftrag().getDatumFaellig(), o.getZahlungsauftrag().getDatumFaellig());
		return builder.toComparison();
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
		final Zahlung otherZahlung = (Zahlung) other;
		return getStatus() == otherZahlung.getStatus() &&
			Objects.equals(getEmpfaengerId(), otherZahlung.getEmpfaengerId()) &&
			MathUtil.isSame(getBetragTotalZahlung(), otherZahlung.getBetragTotalZahlung());
	}

	@Nonnull
	public Institution extractInstitution() {
		final Optional<Zahlungsposition> firstZahlungsposition = getZahlungspositionen().stream().findFirst();
		if (firstZahlungsposition.isPresent()) {
			final AbstractPlatz platz =
				firstZahlungsposition.get().getVerfuegungZeitabschnitt().getVerfuegung().getPlatz();
			return platz.getInstitutionStammdaten().getInstitution();
		}
		throw new EbeguEntityNotFoundException("extractInstitution", "No Institution found for Zahlung " + getId());
	}
}
