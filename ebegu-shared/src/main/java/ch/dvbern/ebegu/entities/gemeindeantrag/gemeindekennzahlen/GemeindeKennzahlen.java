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

package ch.dvbern.ebegu.entities.gemeindeantrag.gemeindekennzahlen;

import java.math.BigDecimal;
import java.math.BigInteger;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.ForeignKey;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import ch.dvbern.ebegu.entities.AbstractEntity;
import ch.dvbern.ebegu.entities.Gemeinde;
import ch.dvbern.ebegu.entities.Gesuchsperiode;
import ch.dvbern.ebegu.entities.gemeindeantrag.GemeindeAntrag;
import ch.dvbern.ebegu.enums.gemeindeantrag.GemeindeAntragTyp;
import org.hibernate.envers.Audited;

@Audited
@Entity
public class GemeindeKennzahlen extends AbstractEntity implements GemeindeAntrag {

	private static final long serialVersionUID = 8854741977608451344L;


	@Nonnull
	@Column(nullable = false)
	@Enumerated(EnumType.STRING)
	private GemeindeKennzahlenStatus status;

	@Nonnull
	@ManyToOne(optional = false)
	@JoinColumn(foreignKey = @ForeignKey(name = "FK_gemeinde_kennzahlen_gemeinde_id"), nullable = false)
	private Gemeinde gemeinde;

	@Nonnull
	@ManyToOne(optional = false)
	@JoinColumn(foreignKey = @ForeignKey(name = "FK_gemeinde_kennzahlen_gesuchsperiode_id"), nullable = false)
	private Gesuchsperiode gesuchsperiode;

	@Nullable
	@Column
	private Boolean nachfrageErfuellt;

	@Nullable
	@Column
	private Boolean gemeindeKontingentiert;

	@Nullable
	@Column
	private BigInteger nachfrageAnzahl;

	@Nullable
	@Column
	private BigDecimal nachfrageDauer;

	@Nullable
	@Column
	private String limitierungTfo;
	

	@Override
	public boolean isSame(AbstractEntity other) {
		return false;
	}

	@Nonnull
	@Override
	public GemeindeAntragTyp getGemeindeAntragTyp() {
		return GemeindeAntragTyp.GEMEINDE_KENNZAHLEN;
	}

	@Nonnull
	@Override
	public Gemeinde getGemeinde() {
		return gemeinde;
	}

	@Nonnull
	@Override
	public Gesuchsperiode getGesuchsperiode() {
		return gesuchsperiode;
	}

	@Nonnull
	@Override
	public String getStatusString() {
		return status.toString();
	}

	@Override
	public boolean isAntragAbgeschlossen() {
		return status == GemeindeKennzahlenStatus.ABGESCHLOSSEN;
	}

	@Nonnull
	public GemeindeKennzahlenStatus getStatus() {
		return status;
	}

	public void setStatus(@Nonnull GemeindeKennzahlenStatus status) {
		this.status = status;
	}

	public void setGemeinde(@Nonnull Gemeinde gemeinde) {
		this.gemeinde = gemeinde;
	}

	public void setGesuchsperiode(@Nonnull Gesuchsperiode gesuchsperiode) {
		this.gesuchsperiode = gesuchsperiode;
	}

	@Nullable
	public Boolean getNachfrageErfuellt() {
		return nachfrageErfuellt;
	}

	public void setNachfrageErfuellt(@Nullable Boolean nachfrageErfuellt) {
		this.nachfrageErfuellt = nachfrageErfuellt;
	}

	@Nullable
	public BigInteger getNachfrageAnzahl() {
		return nachfrageAnzahl;
	}

	public void setNachfrageAnzahl(@Nullable BigInteger nachfrageAnzahl) {
		this.nachfrageAnzahl = nachfrageAnzahl;
	}

	@Nullable
	public BigDecimal getNachfrageDauer() {
		return nachfrageDauer;
	}

	public void setNachfrageDauer(@Nullable BigDecimal nachfrageDauer) {
		this.nachfrageDauer = nachfrageDauer;
	}

	@Nullable
	public String getLimitierungTfo() {
		return limitierungTfo;
	}

	public void setLimitierungTfo(@Nullable String welcheKostenlenkungsmassnahmen) {
		this.limitierungTfo = welcheKostenlenkungsmassnahmen;
	}

	@Nullable
	public Boolean getGemeindeKontingentiert() {
		return gemeindeKontingentiert;
	}

	public void setGemeindeKontingentiert(@Nullable Boolean gemeindeKontingentiert) {
		this.gemeindeKontingentiert = gemeindeKontingentiert;
	}
}
