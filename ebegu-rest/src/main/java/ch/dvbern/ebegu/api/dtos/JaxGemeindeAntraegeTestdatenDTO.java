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

package ch.dvbern.ebegu.api.dtos;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;

import ch.dvbern.ebegu.enums.gemeindeantrag.LastenausgleichTagesschuleAngabenGemeindeStatus;

public class JaxGemeindeAntraegeTestdatenDTO extends JaxAbstractDTO {

	private static final long serialVersionUID = 7154032563940613244L;

	@NotNull
	private JaxGesuchsperiode gesuchsperiode;

	@Nullable
	private JaxGemeinde gemeinde;

	@NotNull
	private LastenausgleichTagesschuleAngabenGemeindeStatus status;

	public JaxGesuchsperiode getGesuchsperiode() {
		return gesuchsperiode;
	}

	public void setGesuchsperiode(JaxGesuchsperiode gesuchsperiode) {
		this.gesuchsperiode = gesuchsperiode;
	}

	@Nullable
	public JaxGemeinde getGemeinde() {
		return gemeinde;
	}

	public void setGemeinde(@Nullable JaxGemeinde gemeinde) {
		this.gemeinde = gemeinde;
	}

	public LastenausgleichTagesschuleAngabenGemeindeStatus getStatus() {
		return status;
	}

	public void setStatus(LastenausgleichTagesschuleAngabenGemeindeStatus status) {
		this.status = status;
	}
}
