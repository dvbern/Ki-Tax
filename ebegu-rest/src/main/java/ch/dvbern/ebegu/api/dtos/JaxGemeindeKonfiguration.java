/*
 * AGPL File-Header
 *
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

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;

import ch.dvbern.ebegu.enums.EinschulungTyp;

public class JaxGemeindeKonfiguration {
	@NotNull
	private String gesuchsperiodeName;
	@NotNull
	private JaxGesuchsperiode gesuchsperiode;
	@Nullable
	private boolean konfigKontingentierung;
	@Nullable
	private EinschulungTyp konfigBeguBisUndMitSchulstufe;
	@NotNull
	private Map<String, String> konfigiration = new HashMap<>();

	public String getGesuchsperiodeName() {
		return gesuchsperiodeName;
	}

	public void setGesuchsperiodeName(String gesuchsperiodeName) {
		this.gesuchsperiodeName = gesuchsperiodeName;
	}

	public JaxGesuchsperiode getGesuchsperiode() {
		return gesuchsperiode;
	}

	public void setGesuchsperiode(JaxGesuchsperiode gesuchsperiode) {
		this.gesuchsperiode = gesuchsperiode;
	}

	@Nullable
	public boolean isKonfigKontingentierung() {
		return konfigKontingentierung;
	}

	public void setKonfigKontingentierung(@Nullable boolean konfigKontingentierung) {
		this.konfigKontingentierung = konfigKontingentierung;
	}

	@Nullable
	public EinschulungTyp getKonfigBeguBisUndMitSchulstufe() {
		return konfigBeguBisUndMitSchulstufe;
	}

	public void setKonfigBeguBisUndMitSchulstufe(@Nullable EinschulungTyp konfigBeguBisUndMitSchulstufe) {
		this.konfigBeguBisUndMitSchulstufe = konfigBeguBisUndMitSchulstufe;
	}

	public Map<String, String> getKonfigiration() {
		return konfigiration;
	}

	public void setKonfigiration(Map<String, String> konfigiration) {
		this.konfigiration = konfigiration;
	}
}
