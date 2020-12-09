/*
 * Copyright (C) 2020 DV Bern AG, Switzerland
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

import javax.annotation.Nonnull;
import javax.validation.constraints.NotNull;

import ch.dvbern.ebegu.api.dtos.JaxAbstractDTO;
import ch.dvbern.ebegu.api.dtos.JaxGemeinde;
import ch.dvbern.ebegu.api.dtos.JaxGesuchsperiode;
import ch.dvbern.ebegu.enums.gemeindeantrag.GemeindeAntragTyp;

public class JaxGemeindeAntrag extends JaxAbstractDTO {

	private static final long serialVersionUID = 4099969051581833190L;

	@NotNull @Nonnull
	private GemeindeAntragTyp gemeindeAntragTyp;

	@NotNull @Nonnull
	private JaxGemeinde gemeinde;

	@NotNull @Nonnull
	private JaxGesuchsperiode gesuchsperiode;

	@NotNull @Nonnull
	private String statusString;

	private boolean antragAbgeschlossen;

	@Nonnull
	public GemeindeAntragTyp getGemeindeAntragTyp() {
		return gemeindeAntragTyp;
	}

	public void setGemeindeAntragTyp(@Nonnull GemeindeAntragTyp gemeindeAntragTyp) {
		this.gemeindeAntragTyp = gemeindeAntragTyp;
	}

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

	@Nonnull
	public String getStatusString() {
		return statusString;
	}

	public void setStatusString(@Nonnull String statusString) {
		this.statusString = statusString;
	}

	public boolean isAntragAbgeschlossen() {
		return antragAbgeschlossen;
	}

	public void setAntragAbgeschlossen(boolean antragAbgeschlossen) {
		this.antragAbgeschlossen = antragAbgeschlossen;
	}
}
