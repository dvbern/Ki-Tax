/*
 * Copyright (C) 2019 DV Bern AG, Switzerland
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

package ch.dvbern.ebegu.api.dtos;

import java.math.BigDecimal;

import javax.annotation.Nonnull;
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

/**
 * DTO fuer ein Detail eines Lastenausgleichs (Daten pro Gemeinde und Jahr)
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class JaxLastenausgleichDetail extends JaxAbstractDTO {

	private static final long serialVersionUID = 1492463839000754511L;

	@NotNull @Nonnull
	private Integer jahr = 0;

	@NotNull @Nonnull
	private String gemeindeName;

	@NotNull @Nonnull
	private Long gemeindeBfsNummer = 0L;

	@NotNull @Nonnull
	private BigDecimal totalBelegungen = BigDecimal.ZERO;

	@NotNull @Nonnull
	private BigDecimal totalBetragGutscheine = BigDecimal.ZERO;

	@NotNull @Nonnull
	private BigDecimal selbstbehaltGemeinde = BigDecimal.ZERO;

	@NotNull @Nonnull
	private BigDecimal betragLastenausgleich = BigDecimal.ZERO;

	private boolean korrektur = false;


	@Nonnull
	public Integer getJahr() {
		return jahr;
	}

	public void setJahr(@Nonnull Integer jahr) {
		this.jahr = jahr;
	}

	@Nonnull
	public String getGemeindeName() {
		return gemeindeName;
	}

	public void setGemeindeName(@Nonnull String gemeindeName) {
		this.gemeindeName = gemeindeName;
	}

	@Nonnull
	public Long getGemeindeBfsNummer() {
		return gemeindeBfsNummer;
	}

	public void setGemeindeBfsNummer(@Nonnull Long gemeindeBfsNummer) {
		this.gemeindeBfsNummer = gemeindeBfsNummer;
	}

	@Nonnull
	public BigDecimal getTotalBelegungen() {
		return totalBelegungen;
	}

	public void setTotalBelegungen(@Nonnull BigDecimal totalBelegungen) {
		this.totalBelegungen = totalBelegungen;
	}

	@Nonnull
	public BigDecimal getTotalBetragGutscheine() {
		return totalBetragGutscheine;
	}

	public void setTotalBetragGutscheine(@Nonnull BigDecimal totalBetragGutscheine) {
		this.totalBetragGutscheine = totalBetragGutscheine;
	}

	@Nonnull
	public BigDecimal getSelbstbehaltGemeinde() {
		return selbstbehaltGemeinde;
	}

	public void setSelbstbehaltGemeinde(@Nonnull BigDecimal selbstbehaltGemeinde) {
		this.selbstbehaltGemeinde = selbstbehaltGemeinde;
	}

	@Nonnull
	public BigDecimal getBetragLastenausgleich() {
		return betragLastenausgleich;
	}

	public void setBetragLastenausgleich(@Nonnull BigDecimal betragLastenausgleich) {
		this.betragLastenausgleich = betragLastenausgleich;
	}

	public boolean isKorrektur() {
		return korrektur;
	}

	public void setKorrektur(boolean korrektur) {
		this.korrektur = korrektur;
	}
}
