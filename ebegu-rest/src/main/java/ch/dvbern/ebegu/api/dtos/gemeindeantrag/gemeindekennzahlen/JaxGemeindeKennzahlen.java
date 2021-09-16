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

package ch.dvbern.ebegu.api.dtos.gemeindeantrag.gemeindekennzahlen;

import java.math.BigDecimal;
import java.math.BigInteger;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import ch.dvbern.ebegu.api.dtos.JaxAbstractDTO;
import ch.dvbern.ebegu.api.dtos.JaxGemeinde;
import ch.dvbern.ebegu.api.dtos.JaxGesuchsperiode;
import ch.dvbern.ebegu.entities.gemeindeantrag.gemeindekennzahlen.GemeindeKennzahlenStatus;

public class JaxGemeindeKennzahlen extends JaxAbstractDTO {

	private static final long serialVersionUID = 5815815180036113378L;

	@Nonnull
	private JaxGemeinde gemeinde;

	@Nonnull
	private JaxGesuchsperiode gesuchsperiode;

	@Nonnull
	private GemeindeKennzahlenStatus status;

	@Nullable
	private Boolean nachfrageErfuellt;

	@Nullable
	private BigInteger nachfrageAnzahl;

	@Nullable
	private BigDecimal nachfrageDauer;

	@Nullable
	private Boolean kostenlenkungAndere;

	@Nullable
	private String welcheKostenlenkungsmassnahmen;

	@Nonnull
	public JaxGemeinde getGemeinde() {
		return gemeinde;
	}

	public void setGemeinde(@Nonnull JaxGemeinde gemeinde) {
		this.gemeinde = gemeinde;
	}

	@Nonnull
	public JaxGesuchsperiode getGesuchsperiode() {
		return gesuchsperiode;
	}

	public void setGesuchsperiode(@Nonnull JaxGesuchsperiode gesuchsperiode) {
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
	public Boolean getKostenlenkungAndere() {
		return kostenlenkungAndere;
	}

	public void setKostenlenkungAndere(@Nullable Boolean kostenlenkungAndere) {
		this.kostenlenkungAndere = kostenlenkungAndere;
	}

	@Nullable
	public String getWelcheKostenlenkungsmassnahmen() {
		return welcheKostenlenkungsmassnahmen;
	}

	public void setWelcheKostenlenkungsmassnahmen(@Nullable String welcheKostenlenkungsmassnahmen) {
		this.welcheKostenlenkungsmassnahmen = welcheKostenlenkungsmassnahmen;
	}

	@Nonnull
	public GemeindeKennzahlenStatus getStatus() {
		return status;
	}

	public void setStatus(@Nonnull GemeindeKennzahlenStatus status) {
		this.status = status;
	}
}
