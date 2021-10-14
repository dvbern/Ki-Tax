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

package ch.dvbern.ebegu.reporting.gemeinden;

import java.math.BigDecimal;
import java.math.BigInteger;

import javax.annotation.Nullable;

public class GemeindenDatenDataRow {

	@Nullable
	private String gesuchsperiode;

	@Nullable
	private String limitierungKita;

	@Nullable
	private Boolean kontingentierung;

	@Nullable
	private Boolean nachfrageErfuellt;

	@Nullable
	private BigDecimal nachfrageAnzahl;

	@Nullable
	private BigDecimal nachfrageDauer;

	@Nullable
	private String limitierungTfo;

	@Nullable
	private BigDecimal erwerbspensumZuschlag;

	@Nullable
	public String getGesuchsperiode() {
		return gesuchsperiode;
	}

	public void setGesuchsperiode(@Nullable String gesuchsperiode) {
		this.gesuchsperiode = gesuchsperiode;
	}

	@Nullable
	public String getLimitierungKita() {
		return limitierungKita;
	}

	public void setLimitierungKita(@Nullable String limitierungKita) {
		this.limitierungKita = limitierungKita;
	}

	@Nullable
	public Boolean getKontingentierung() {
		return kontingentierung;
	}

	public void setKontingentierung(@Nullable Boolean kontingentierung) {
		this.kontingentierung = kontingentierung;
	}

	@Nullable
	public Boolean getNachfrageErfuellt() {
		return nachfrageErfuellt;
	}

	public void setNachfrageErfuellt(@Nullable Boolean nachfrageErfuellt) {
		this.nachfrageErfuellt = nachfrageErfuellt;
	}

	@Nullable
	public BigDecimal getNachfrageAnzahl() {
		return nachfrageAnzahl;
	}

	public void setNachfrageAnzahl(@Nullable BigDecimal nachfrageAnzahl) {
		this.nachfrageAnzahl = nachfrageAnzahl;
	}

	public void setNachfrageAnzahl(@Nullable BigInteger nachfrageAnzahl) {
		if (nachfrageAnzahl == null) {
			this.nachfrageAnzahl = null;
		} else {
			this.nachfrageAnzahl = new BigDecimal(nachfrageAnzahl);
		}
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

	public void setLimitierungTfo(@Nullable String limitierungTfo) {
		this.limitierungTfo = limitierungTfo;
	}

	@Nullable
	public BigDecimal getErwerbspensumZuschlag() {
		return erwerbspensumZuschlag;
	}

	public void setErwerbspensumZuschlag(@Nullable BigDecimal erwerbspensumZuschlag) {
		this.erwerbspensumZuschlag = erwerbspensumZuschlag;
	}
}
