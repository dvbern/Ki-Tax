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

package ch.dvbern.ebegu.reporting.tagesschule;

import java.time.DayOfWeek;
import java.util.ArrayList;
import java.util.List;

import ch.dvbern.ebegu.entities.ModulTagesschuleGroup;

/**
 * DTO fuer die TagesschuleStatistik für eine RepeatCol Gruppe (Wochentag und die ModulGruppen, die
 * an diesem Wochentag ein Modul anbieten).
 */
public class TagesschuleRepeatColGroup {

	private DayOfWeek wochentag;
	private List<ModulTagesschuleGroup> modulTagesschuleList;
	private String repeatColName;

	public TagesschuleRepeatColGroup(DayOfWeek wochentag, String repeatColName) {
		this.wochentag = wochentag;
		this.repeatColName = repeatColName;
		this.modulTagesschuleList = new ArrayList<>();
	}

	public DayOfWeek getWochentag() {
		return wochentag;
	}

	public void setWochentag(DayOfWeek wochentag) {
		this.wochentag = wochentag;
	}

	public List<ModulTagesschuleGroup> getModulTagesschuleList() {
		return modulTagesschuleList;
	}

	public void setModulTagesschuleList(List<ModulTagesschuleGroup> modulTagesschuleList) {
		this.modulTagesschuleList = modulTagesschuleList;
	}

	public void appendModulGroup(ModulTagesschuleGroup modulTagesschuleGroup) {
		this.modulTagesschuleList.add(modulTagesschuleGroup);
	}

	public String getRepeatColName() {
		return repeatColName;
	}

	public void setRepeatColName(String repeatColName) {
		this.repeatColName = repeatColName;
	}
}
