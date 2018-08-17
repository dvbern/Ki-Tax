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

import javax.annotation.Nonnull;
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

import ch.dvbern.ebegu.enums.ZahlungStatus;
import ch.dvbern.ebegu.util.EbeguUtil;
import ch.dvbern.ebegu.util.MathUtil;
import org.apache.commons.lang3.builder.CompareToBuilder;
import org.hibernate.envers.Audited;

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

	@NotNull
	@ManyToOne(optional = false)
	@JoinColumn(foreignKey = @ForeignKey(name = "FK_Zahlung_institutionStammdaten_id"))
	private InstitutionStammdaten institutionStammdaten;

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

	public InstitutionStammdaten getInstitutionStammdaten() {
		return institutionStammdaten;
	}

	public void setInstitutionStammdaten(InstitutionStammdaten institutionStammdaten) {
		this.institutionStammdaten = institutionStammdaten;
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
		builder.append(this.getInstitutionStammdaten().getInstitution().getName(), o.getInstitutionStammdaten().getInstitution().getName());
		builder.append(this.getZahlungsauftrag().getDatumFaellig(), o.getZahlungsauftrag().getDatumFaellig());
		return builder.toComparison();
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
		if (!(other instanceof Zahlung)) {
			return false;
		}
		final Zahlung otherZahlung = (Zahlung) other;
		return Objects.equals(getStatus(), otherZahlung.getStatus()) &&
			EbeguUtil.isSameObject(getInstitutionStammdaten(), otherZahlung.getInstitutionStammdaten()) &&
			MathUtil.isSame(getBetragTotalZahlung(), otherZahlung.getBetragTotalZahlung());
	}
}
