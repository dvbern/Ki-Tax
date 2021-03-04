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

package ch.dvbern.ebegu.api.dtos.gemeindeantrag;

import java.math.BigDecimal;

import javax.annotation.Nullable;

import ch.dvbern.ebegu.api.dtos.JaxAbstractDTO;

public class JaxFerienbetreuungAngabenNutzung extends JaxAbstractDTO {

	private static final long serialVersionUID = -7564745550708581548L;

	@Nullable
	private BigDecimal anzahlBetreuungstageKinderBern;

	@Nullable
	private BigDecimal betreuungstageKinderDieserGemeinde;

	@Nullable
	private BigDecimal betreuungstageKinderDieserGemeindeSonderschueler;

	@Nullable
	private BigDecimal davonBetreuungstageKinderAndererGemeinden;

	@Nullable
	private BigDecimal davonBetreuungstageKinderAndererGemeindenSonderschueler;

	@Nullable
	private BigDecimal anzahlBetreuteKinder;

	@Nullable
	private BigDecimal anzahlBetreuteKinderSonderschueler;

	@Nullable
	private BigDecimal anzahlBetreuteKinder1Zyklus;

	@Nullable
	private BigDecimal anzahlBetreuteKinder2Zyklus;

	@Nullable
	private BigDecimal anzahlBetreuteKinder3Zyklus;

	@Nullable
	public BigDecimal getAnzahlBetreuungstageKinderBern() {
		return anzahlBetreuungstageKinderBern;
	}

	public void setAnzahlBetreuungstageKinderBern(@Nullable BigDecimal anzahlBetreuungstageKinderBern) {
		this.anzahlBetreuungstageKinderBern = anzahlBetreuungstageKinderBern;
	}

	@Nullable
	public BigDecimal getBetreuungstageKinderDieserGemeinde() {
		return betreuungstageKinderDieserGemeinde;
	}

	public void setBetreuungstageKinderDieserGemeinde(@Nullable BigDecimal betreuungstageKinderDieserGemeinde) {
		this.betreuungstageKinderDieserGemeinde = betreuungstageKinderDieserGemeinde;
	}

	@Nullable
	public BigDecimal getBetreuungstageKinderDieserGemeindeSonderschueler() {
		return betreuungstageKinderDieserGemeindeSonderschueler;
	}

	public void setBetreuungstageKinderDieserGemeindeSonderschueler(@Nullable BigDecimal betreuungstageKinderDieserGemeindeSonderschueler) {
		this.betreuungstageKinderDieserGemeindeSonderschueler = betreuungstageKinderDieserGemeindeSonderschueler;
	}

	@Nullable
	public BigDecimal getDavonBetreuungstageKinderAndererGemeinden() {
		return davonBetreuungstageKinderAndererGemeinden;
	}

	public void setDavonBetreuungstageKinderAndererGemeinden(@Nullable BigDecimal davonBetreuungstageKinderAndererGemeinden) {
		this.davonBetreuungstageKinderAndererGemeinden = davonBetreuungstageKinderAndererGemeinden;
	}

	@Nullable
	public BigDecimal getDavonBetreuungstageKinderAndererGemeindenSonderschueler() {
		return davonBetreuungstageKinderAndererGemeindenSonderschueler;
	}

	public void setDavonBetreuungstageKinderAndererGemeindenSonderschueler(@Nullable BigDecimal davonBetreuungstageKinderAndererGemeindenSonderschueler) {
		this.davonBetreuungstageKinderAndererGemeindenSonderschueler = davonBetreuungstageKinderAndererGemeindenSonderschueler;
	}

	@Nullable
	public BigDecimal getAnzahlBetreuteKinder() {
		return anzahlBetreuteKinder;
	}

	public void setAnzahlBetreuteKinder(@Nullable BigDecimal anzahlBetreuteKinder) {
		this.anzahlBetreuteKinder = anzahlBetreuteKinder;
	}

	@Nullable
	public BigDecimal getAnzahlBetreuteKinderSonderschueler() {
		return anzahlBetreuteKinderSonderschueler;
	}

	public void setAnzahlBetreuteKinderSonderschueler(@Nullable BigDecimal anzahlBetreuteKinderSonderschueler) {
		this.anzahlBetreuteKinderSonderschueler = anzahlBetreuteKinderSonderschueler;
	}

	@Nullable
	public BigDecimal getAnzahlBetreuteKinder1Zyklus() {
		return anzahlBetreuteKinder1Zyklus;
	}

	public void setAnzahlBetreuteKinder1Zyklus(@Nullable BigDecimal anzahlBetreuteKinder1Zyklus) {
		this.anzahlBetreuteKinder1Zyklus = anzahlBetreuteKinder1Zyklus;
	}

	@Nullable
	public BigDecimal getAnzahlBetreuteKinder2Zyklus() {
		return anzahlBetreuteKinder2Zyklus;
	}

	public void setAnzahlBetreuteKinder2Zyklus(@Nullable BigDecimal anzahlBetreuteKinder2Zyklus) {
		this.anzahlBetreuteKinder2Zyklus = anzahlBetreuteKinder2Zyklus;
	}

	@Nullable
	public BigDecimal getAnzahlBetreuteKinder3Zyklus() {
		return anzahlBetreuteKinder3Zyklus;
	}

	public void setAnzahlBetreuteKinder3Zyklus(@Nullable BigDecimal anzahlBetreuteKinder3Zyklus) {
		this.anzahlBetreuteKinder3Zyklus = anzahlBetreuteKinder3Zyklus;
	}
}
