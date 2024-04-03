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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;

public class JaxGemeindeKonfiguration implements Serializable {

	private static final long serialVersionUID = 549308935162296882L;

	@Nonnull
	private String gesuchsperiodeName;
	@Nonnull
	private String gesuchsperiodeStatusName;
	@Nonnull
	private JaxGesuchsperiode gesuchsperiode;
	@Nonnull
	private List<JaxEinstellung> konfigurationen = new ArrayList<>();
	@Nonnull
	private List<JaxGemeindeStammdatenGesuchsperiodeFerieninsel> ferieninselStammdaten;

	private int erwerbspensumZuschlagMax;
	private int erwerbspensumMiminumVorschuleMax;
	private int erwerbspensumMiminumSchulkinderMax;

	@Nonnull
	public String getGesuchsperiodeName() {
		return gesuchsperiodeName;
	}

	public void setGesuchsperiodeName(@Nonnull String gesuchsperiodeName) {
		this.gesuchsperiodeName = gesuchsperiodeName;
	}

	@Nonnull
	public JaxGesuchsperiode getGesuchsperiode() {
		return gesuchsperiode;
	}

	public void setGesuchsperiode(@Nonnull JaxGesuchsperiode gesuchsperiode) {
		this.gesuchsperiode = gesuchsperiode;
	}

	@Nonnull
	public List<JaxEinstellung> getKonfigurationen() {
		return konfigurationen;
	}

	public void setKonfigurationen(@Nonnull List<JaxEinstellung> konfigurationen) {
		this.konfigurationen = konfigurationen;
	}

	public int getErwerbspensumZuschlagMax() {
		return erwerbspensumZuschlagMax;
	}

	public void setErwerbspensumZuschlagMax(int erwerbspensumZuschlagMax) {
		this.erwerbspensumZuschlagMax = erwerbspensumZuschlagMax;
	}

	@Nonnull
	public String getGesuchsperiodeStatusName() {
		return gesuchsperiodeStatusName;
	}

	public void setGesuchsperiodeStatusName(@Nonnull String gesuchsperiodeStatusName) {
		this.gesuchsperiodeStatusName = gesuchsperiodeStatusName;
	}

	@Nonnull
	public List<JaxGemeindeStammdatenGesuchsperiodeFerieninsel> getFerieninselStammdaten() {
		return ferieninselStammdaten;
	}

	public void setFerieninselStammdaten(@Nonnull List<JaxGemeindeStammdatenGesuchsperiodeFerieninsel> ferieninselStammdaten) {
		this.ferieninselStammdaten = ferieninselStammdaten;
	}

	public int getErwerbspensumMiminumVorschuleMax() {
		return erwerbspensumMiminumVorschuleMax;
	}

	public void setErwerbspensumMiminumVorschuleMax(int erwerbspensumMiminumVorschuleMax) {
		this.erwerbspensumMiminumVorschuleMax = erwerbspensumMiminumVorschuleMax;
	}

	public int getErwerbspensumMiminumSchulkinderMax() {
		return erwerbspensumMiminumSchulkinderMax;
	}

	public void setErwerbspensumMiminumSchulkinderMax(int erwerbspensumMiminumSchulkinderMax) {
		this.erwerbspensumMiminumSchulkinderMax = erwerbspensumMiminumSchulkinderMax;
	}
}
