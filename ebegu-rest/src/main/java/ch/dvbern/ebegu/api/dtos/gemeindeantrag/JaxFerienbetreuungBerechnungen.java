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

import ch.dvbern.ebegu.api.dtos.JaxAbstractDTO;

import javax.annotation.Nullable;
import java.math.BigDecimal;

public class JaxFerienbetreuungBerechnungen extends JaxAbstractDTO {

	private static final long serialVersionUID = 5946998195854570129L;

	@Nullable
	private BigDecimal totalKosten;

	@Nullable
	private BigDecimal betreuungstageKinderDieserGemeindeMinusSonderschueler;

	@Nullable
	private BigDecimal betreuungstageKinderAndererGemeindeMinusSonderschueler;

	@Nullable
	private BigDecimal totalKantonsbeitrag;

	@Nullable
	private BigDecimal totalEinnahmen;

	@Nullable
	private BigDecimal beitragKinderAnbietendenGemeinde;

	@Nullable
	private BigDecimal beteiligungAnbietendenGemeinde;

	@Nullable
	private Boolean beteiligungZuTief;

	public JaxFerienbetreuungBerechnungen() {
	}

	@Nullable
	public BigDecimal getTotalKosten() {
		return totalKosten;
	}

	public void setTotalKosten(@Nullable BigDecimal totalKosten) {
		this.totalKosten = totalKosten;
	}

	@Nullable
	public BigDecimal getBetreuungstageKinderDieserGemeindeMinusSonderschueler() {
		return betreuungstageKinderDieserGemeindeMinusSonderschueler;
	}

	public void setBetreuungstageKinderDieserGemeindeMinusSonderschueler(
			@Nullable BigDecimal betreuungstageKinderDieserGemeindeMinusSonderschueler) {
		this.betreuungstageKinderDieserGemeindeMinusSonderschueler =
				betreuungstageKinderDieserGemeindeMinusSonderschueler;
	}

	@Nullable
	public BigDecimal getBetreuungstageKinderAndererGemeindeMinusSonderschueler() {
		return betreuungstageKinderAndererGemeindeMinusSonderschueler;
	}

	public void setBetreuungstageKinderAndererGemeindeMinusSonderschueler(
			@Nullable BigDecimal betreuungstageKinderAndererGemeindeMinusSonderschueler) {
		this.betreuungstageKinderAndererGemeindeMinusSonderschueler =
				betreuungstageKinderAndererGemeindeMinusSonderschueler;
	}

	@Nullable
	public BigDecimal getTotalKantonsbeitrag() {
		return totalKantonsbeitrag;
	}

	public void setTotalKantonsbeitrag(@Nullable BigDecimal totalKantonsbeitrag) {
		this.totalKantonsbeitrag = totalKantonsbeitrag;
	}

	@Nullable
	public BigDecimal getTotalEinnahmen() {
		return totalEinnahmen;
	}

	public void setTotalEinnahmen(@Nullable BigDecimal totalEinnahmen) {
		this.totalEinnahmen = totalEinnahmen;
	}

	@Nullable
	public BigDecimal getBeitragKinderAnbietendenGemeinde() {
		return beitragKinderAnbietendenGemeinde;
	}

	public void setBeitragKinderAnbietendenGemeinde(@Nullable BigDecimal beitragKinderAnbietendenGemeinde) {
		this.beitragKinderAnbietendenGemeinde = beitragKinderAnbietendenGemeinde;
	}

	@Nullable
	public BigDecimal getBeteiligungAnbietendenGemeinde() {
		return beteiligungAnbietendenGemeinde;
	}

	public void setBeteiligungAnbietendenGemeinde(@Nullable BigDecimal beteiligungAnbietendenGemeinde) {
		this.beteiligungAnbietendenGemeinde = beteiligungAnbietendenGemeinde;
	}

	@Nullable
	public Boolean getBeteiligungZuTief() {
		return beteiligungZuTief;
	}

	public void setBeteiligungZuTief(@Nullable Boolean beteiligungZuTief) {
		this.beteiligungZuTief = beteiligungZuTief;
	}
}
