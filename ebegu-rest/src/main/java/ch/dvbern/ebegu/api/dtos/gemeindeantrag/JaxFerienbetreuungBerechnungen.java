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

package ch.dvbern.ebegu.api.dtos.gemeindeantrag;

import java.math.BigDecimal;

import javax.annotation.Nonnull;

import ch.dvbern.ebegu.api.dtos.JaxAbstractDTO;

public class JaxFerienbetreuungBerechnungen extends JaxAbstractDTO {

	@Nonnull
	private BigDecimal totalKosten;

	@Nonnull
	private BigDecimal betreuungstageKinderDieserGemeindeMinusSonderschueler;

	@Nonnull
	private BigDecimal betreuungstageKinderAndererGemeindeMinusSonderschueler;

	@Nonnull
	private BigDecimal totalKantonsbeitrag;

	@Nonnull
	private BigDecimal totalEinnahmen;

	@Nonnull
	private BigDecimal beitragKinderAnbietendenGemeinde;

	@Nonnull
	private BigDecimal beteiligungAnbietendenGemeinde;

	@Nonnull
	private Boolean beteiligungZuTief;

	public JaxFerienbetreuungBerechnungen() {
	}

	@Nonnull
	public BigDecimal getTotalKosten() {
		return totalKosten;
	}

	public void setTotalKosten(@Nonnull BigDecimal totalKosten) {
		this.totalKosten = totalKosten;
	}

	@Nonnull
	public BigDecimal getBetreuungstageKinderDieserGemeindeMinusSonderschueler() {
		return betreuungstageKinderDieserGemeindeMinusSonderschueler;
	}

	public void setBetreuungstageKinderDieserGemeindeMinusSonderschueler(
			@Nonnull BigDecimal betreuungstageKinderDieserGemeindeMinusSonderschueler) {
		this.betreuungstageKinderDieserGemeindeMinusSonderschueler =
				betreuungstageKinderDieserGemeindeMinusSonderschueler;
	}

	@Nonnull
	public BigDecimal getBetreuungstageKinderAndererGemeindeMinusSonderschueler() {
		return betreuungstageKinderAndererGemeindeMinusSonderschueler;
	}

	public void setBetreuungstageKinderAndererGemeindeMinusSonderschueler(
			@Nonnull BigDecimal betreuungstageKinderAndererGemeindeMinusSonderschueler) {
		this.betreuungstageKinderAndererGemeindeMinusSonderschueler =
				betreuungstageKinderAndererGemeindeMinusSonderschueler;
	}

	@Nonnull
	public BigDecimal getTotalKantonsbeitrag() {
		return totalKantonsbeitrag;
	}

	public void setTotalKantonsbeitrag(@Nonnull BigDecimal totalKantonsbeitrag) {
		this.totalKantonsbeitrag = totalKantonsbeitrag;
	}

	@Nonnull
	public BigDecimal getTotalEinnahmen() {
		return totalEinnahmen;
	}

	public void setTotalEinnahmen(@Nonnull BigDecimal totalEinnahmen) {
		this.totalEinnahmen = totalEinnahmen;
	}

	@Nonnull
	public BigDecimal getBeitragKinderAnbietendenGemeinde() {
		return beitragKinderAnbietendenGemeinde;
	}

	public void setBeitragKinderAnbietendenGemeinde(@Nonnull BigDecimal beitragKinderAnbietendenGemeinde) {
		this.beitragKinderAnbietendenGemeinde = beitragKinderAnbietendenGemeinde;
	}

	@Nonnull
	public BigDecimal getBeteiligungAnbietendenGemeinde() {
		return beteiligungAnbietendenGemeinde;
	}

	public void setBeteiligungAnbietendenGemeinde(@Nonnull BigDecimal beteiligungAnbietendenGemeinde) {
		this.beteiligungAnbietendenGemeinde = beteiligungAnbietendenGemeinde;
	}

	@Nonnull
	public Boolean getBeteiligungZuTief() {
		return beteiligungZuTief;
	}

	public void setBeteiligungZuTief(@Nonnull Boolean beteiligungZuTief) {
		this.beteiligungZuTief = beteiligungZuTief;
	}
}
