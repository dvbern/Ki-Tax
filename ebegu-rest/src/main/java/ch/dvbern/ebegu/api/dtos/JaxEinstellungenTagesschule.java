/*
 * Copyright (C) 2019 DV Bern AG, Switzerland
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
import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

import ch.dvbern.ebegu.enums.ModulTagesschuleTyp;


@XmlAccessorType(XmlAccessType.FIELD)
public class JaxEinstellungenTagesschule extends JaxAbstractDTO {

	private static final long serialVersionUID = -1513774591239298994L;

	@NotNull @Nonnull
	private JaxGesuchsperiode gesuchsperiode;

	@NotNull @Nonnull
	private List<JaxModulTagesschuleGroup> modulTagesschuleGroups = new ArrayList<>();

	@NotNull @Nonnull
	private ModulTagesschuleTyp modulTagesschuleTyp = ModulTagesschuleTyp.DYNAMISCH;

	@Nullable
	private String erlaeuterung;

	private boolean tagis = false;

	public JaxGesuchsperiode getGesuchsperiode() {
		return gesuchsperiode;
	}

	public void setGesuchsperiode(JaxGesuchsperiode gesuchsperiode) {
		this.gesuchsperiode = gesuchsperiode;
	}

	public List<JaxModulTagesschuleGroup> getModulTagesschuleGroups() {
		return modulTagesschuleGroups;
	}

	public void setModulTagesschuleGroups(List<JaxModulTagesschuleGroup> modulTagesschuleGroups) {
		this.modulTagesschuleGroups = modulTagesschuleGroups;
	}

	public ModulTagesschuleTyp getModulTagesschuleTyp() {
		return modulTagesschuleTyp;
	}

	public void setModulTagesschuleTyp(ModulTagesschuleTyp modulTagesschuleTyp) {
		this.modulTagesschuleTyp = modulTagesschuleTyp;
	}

	@Nullable
	public String getErlaeuterung() {
		return erlaeuterung;
	}

	public void setErlaeuterung(@Nullable String erlaeuterung) {
		this.erlaeuterung = erlaeuterung;
	}

	public boolean isTagis() {
		return tagis;
	}

	public void setTagis(boolean tagis) {
		this.tagis = tagis;
	}
}
