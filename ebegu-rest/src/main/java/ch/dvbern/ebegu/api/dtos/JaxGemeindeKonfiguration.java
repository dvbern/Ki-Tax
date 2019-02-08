/*
 * Copyright (C) 2018 DV Bern AG, Switzerland
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

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;
import javax.validation.constraints.NotNull;

import ch.dvbern.ebegu.enums.GesuchsperiodeStatus;

public class JaxGemeindeKonfiguration {
	@NotNull
	private String gesuchsperiodeName;
	@NotNull
	private String gesuchsperiodeId;
	@NotNull
	private GesuchsperiodeStatus gesuchsperiodeStatus;
	@NotNull
	private List<JaxEinstellung> konfigurationen = new ArrayList<>();


	public String getGesuchsperiodeName() {
		return gesuchsperiodeName;
	}

	public void setGesuchsperiodeName(String gesuchsperiodeName) {
		this.gesuchsperiodeName = gesuchsperiodeName;
	}

	public String getGesuchsperiodeId() {
		return gesuchsperiodeId;
	}

	public void setGesuchsperiodeId(String gesuchsperiodeId) {
		this.gesuchsperiodeId = gesuchsperiodeId;
	}

	@Nonnull
	public GesuchsperiodeStatus getGesuchsperiodeStatus() {
		return gesuchsperiodeStatus;
	}

	public void setGesuchsperiodeStatus(@Nonnull GesuchsperiodeStatus gesuchsperiodeStatus) {
		this.gesuchsperiodeStatus = gesuchsperiodeStatus;
	}

	public List<JaxEinstellung> getKonfigurationen() {
		return konfigurationen;
	}

	public void setKonfigurationen(List<JaxEinstellung> konfigurationen) {
		this.konfigurationen = konfigurationen;
	}

}
