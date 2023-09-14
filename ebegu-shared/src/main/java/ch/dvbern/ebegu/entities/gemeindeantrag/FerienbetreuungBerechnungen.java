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

package ch.dvbern.ebegu.entities.gemeindeantrag;

import ch.dvbern.ebegu.entities.AbstractEntity;
import org.hibernate.envers.Audited;

import javax.annotation.Nullable;
import javax.persistence.Column;
import javax.persistence.Entity;
import java.math.BigDecimal;

@Entity
@Audited
public class FerienbetreuungBerechnungen extends AbstractEntity {

	private static final long serialVersionUID = -4511966098955703204L;

	@Nullable
	@Column
	private BigDecimal totalKosten;

	@Nullable
	@Column
	private BigDecimal betreuungstageKinderDieserGemeindeMinusSonderschueler;

	@Nullable
	@Column
	private BigDecimal betreuungstageKinderAndererGemeindeMinusSonderschueler;

	@Nullable
	@Column
	private BigDecimal totalKantonsbeitrag;

	@Nullable
	@Column
	private BigDecimal totalEinnahmen;

	@Nullable
	@Column
	private BigDecimal beitragKinderAnbietendenGemeinde;

	@Nullable
	@Column
	private BigDecimal beteiligungAnbietendenGemeinde;

	@Nullable
	@Column
	private Boolean beteiligungZuTief;



	@Override
	public boolean isSame(AbstractEntity other) {
		return false;
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
	public Boolean getBeteiligungZuTief() {
		return beteiligungZuTief;
	}

	public void setBeteiligungZuTief(@Nullable Boolean beteiligungZuTief) {
		this.beteiligungZuTief = beteiligungZuTief;
	}

	@Nullable
	public BigDecimal getBeteiligungAnbietendenGemeinde() {
		return beteiligungAnbietendenGemeinde;
	}

	public void setBeteiligungAnbietendenGemeinde(@Nullable BigDecimal beteiligungAnbietendenGemeinde) {
		this.beteiligungAnbietendenGemeinde = beteiligungAnbietendenGemeinde;
	}

	@Nullable
	public BigDecimal getTotalKantonsbeitrag() {
		return totalKantonsbeitrag;
	}

	public void setTotalKantonsbeitrag(@Nullable BigDecimal totalKantonsbeitrag) {
		this.totalKantonsbeitrag = totalKantonsbeitrag;
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
	public BigDecimal getBetreuungstageKinderDieserGemeindeMinusSonderschueler() {
		return betreuungstageKinderDieserGemeindeMinusSonderschueler;
	}

	public void setBetreuungstageKinderDieserGemeindeMinusSonderschueler(
			@Nullable BigDecimal betreuungstageKinderDieserGemeindeMinusSonderschueler) {
		this.betreuungstageKinderDieserGemeindeMinusSonderschueler =
				betreuungstageKinderDieserGemeindeMinusSonderschueler;
	}

	@Nullable
	public BigDecimal getTotalKosten() {
		return totalKosten;
	}

	public void setTotalKosten(@Nullable BigDecimal totalKosten) {
		this.totalKosten = totalKosten;
	}
}
