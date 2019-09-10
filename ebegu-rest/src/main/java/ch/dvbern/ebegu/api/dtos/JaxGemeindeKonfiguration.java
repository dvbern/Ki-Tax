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

public class JaxGemeindeKonfiguration {
	@NotNull
	private String gesuchsperiodeName;
	@Nonnull
	private JaxGesuchsperiode gesuchsperiode;
	@NotNull
	private List<JaxEinstellung> konfigurationen = new ArrayList<>();

	private int erwerbspensumZuschlagMandant;


	public String getGesuchsperiodeName() {
		return gesuchsperiodeName;
	}

	public void setGesuchsperiodeName(String gesuchsperiodeName) {
		this.gesuchsperiodeName = gesuchsperiodeName;
	}

	@Nonnull
	public JaxGesuchsperiode getGesuchsperiode() {
		return gesuchsperiode;
	}

	public void setGesuchsperiode(@Nonnull JaxGesuchsperiode gesuchsperiode) {
		this.gesuchsperiode = gesuchsperiode;
	}

	public List<JaxEinstellung> getKonfigurationen() {
		return konfigurationen;
	}

	public void setKonfigurationen(List<JaxEinstellung> konfigurationen) {
		this.konfigurationen = konfigurationen;
	}

	public int getErwerbspensumZuschlagMandant() {
		return erwerbspensumZuschlagMandant;
	}

	public void setErwerbspensumZuschlagMandant(int erwerbspensumZuschlagMandant) {
		this.erwerbspensumZuschlagMandant = erwerbspensumZuschlagMandant;
	}
}
