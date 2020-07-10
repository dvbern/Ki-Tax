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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

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

import ch.dvbern.ebegu.enums.ZahlungauftragStatus;
import ch.dvbern.ebegu.util.Constants;
import ch.dvbern.ebegu.util.MathUtil;
import org.hibernate.envers.Audited;

/**
 * Entitaet zum Speichern von einem Zahlungsauftrag in der Datenbank.
 */
@Audited
@Entity
public class Zahlungsauftrag extends AbstractDateRangedEntity {

	private static final long serialVersionUID = 5758088668232796741L;

	@NotNull
	@Column(nullable = false)
	private LocalDate datumFaellig; // Nur benoetigt fuer die Information an Postfinance -> ISO File

	@NotNull
	@Column(nullable = false)
	private LocalDateTime datumGeneriert; // Zeitpunkt, an welchem der Auftrag erstellt wurde, d.h. bis hierhin wurden Mutationen beruecksichtigt

	@NotNull
	@Column(nullable = false)
	@Enumerated(EnumType.STRING)
	private ZahlungauftragStatus status = ZahlungauftragStatus.ENTWURF;

	@NotNull
	@Size(max = Constants.DB_DEFAULT_MAX_LENGTH)
	@Column(nullable = false, length = Constants.DB_DEFAULT_MAX_LENGTH)
	private String beschrieb;

	@Nullable
	@Size(max = Constants.DB_TEXTAREA_LENGTH)
	@Column(nullable = true, length = Constants.DB_TEXTAREA_LENGTH)
	private String result;

	@NotNull
	@ManyToOne(optional = false)
	@JoinColumn(foreignKey = @ForeignKey(name = "FK_zahlungsauftrag_gemeinde_id"))
	private Gemeinde gemeinde;

	@Nonnull
	@Valid
	@OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY, mappedBy = "zahlungsauftrag")
	private List<Zahlung> zahlungen = new ArrayList<>();

	@Nonnull
	private BigDecimal betragTotalAuftrag;

	@Nonnull
	private Boolean hasNegativeZahlungen = false;

	public LocalDate getDatumFaellig() {
		return datumFaellig;
	}

	public void setDatumFaellig(LocalDate datumFaellig) {
		this.datumFaellig = datumFaellig;
	}

	public LocalDateTime getDatumGeneriert() {
		return datumGeneriert;
	}

	public void setDatumGeneriert(LocalDateTime datumGeneriert) {
		this.datumGeneriert = datumGeneriert;
	}

	public ZahlungauftragStatus getStatus() {
		return status;
	}

	public void setStatus(ZahlungauftragStatus status) {
		this.status = status;
	}

	public String getBeschrieb() {
		return beschrieb;
	}

	public void setBeschrieb(String beschrieb) {
		this.beschrieb = beschrieb;
	}

	@Nullable
	public String getResult() {
		return result;
	}

	public void setResult(@Nullable String result) {
		this.result = result;
	}

	@Nonnull
	public List<Zahlung> getZahlungen() {
		return zahlungen;
	}

	public void setZahlungen(@Nonnull List<Zahlung> zahlungen) {
		this.zahlungen = zahlungen;
	}

	@Nonnull
	public BigDecimal getBetragTotalAuftrag() {
		return betragTotalAuftrag;
	}

	public void setBetragTotalAuftrag(@Nonnull BigDecimal betragTotalAuftrag) {
		this.betragTotalAuftrag = betragTotalAuftrag;
	}

	@NotNull
	public Gemeinde getGemeinde() {
		return gemeinde;
	}

	public void setGemeinde(@NotNull Gemeinde gemeinde) {
		this.gemeinde = gemeinde;
	}

	@Nonnull
	public Boolean getHasNegativeZahlungen() {
		return hasNegativeZahlungen;
	}

	public void setHasNegativeZahlungen(@Nonnull Boolean hasNegativeZahlungen) {
		this.hasNegativeZahlungen = hasNegativeZahlungen;
	}

	public String getFilename() {
		return "Zahlungen_" + getGemeinde().getName() + '_' + Constants.SQL_DATE_FORMAT.format(getDatumGeneriert());
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
		if (!super.isSame(other)) {
			return false;
		}
		if (!(other instanceof Zahlungsauftrag)) {
			return false;
		}
		final Zahlungsauftrag otherZahlungsauftrag = (Zahlungsauftrag) other;
		return Objects.equals(getDatumFaellig(), otherZahlungsauftrag.getDatumFaellig()) &&
			Objects.equals(getDatumGeneriert(), otherZahlungsauftrag.getDatumGeneriert()) &&
			getStatus() == otherZahlungsauftrag.getStatus() &&
			Objects.equals(getBeschrieb(), otherZahlungsauftrag.getBeschrieb()) &&
			MathUtil.isSame(getBetragTotalAuftrag(), otherZahlungsauftrag.getBetragTotalAuftrag()) &&
			Objects.equals(getGemeinde(), otherZahlungsauftrag.getGemeinde());
	}
}
