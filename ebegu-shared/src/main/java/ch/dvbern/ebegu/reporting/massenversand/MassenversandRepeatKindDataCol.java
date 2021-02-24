/*
 * Ki-Tax: System for the management of external childcare subsidies
 * Copyright (C) 2017 City of Bern Switzerland
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package ch.dvbern.ebegu.reporting.massenversand;

import java.time.LocalDate;

import javax.annotation.Nonnull;

import com.google.common.base.Strings;

/**
 * DTO fuer ein Kind einer Familie eines Massenversands
 */
public class MassenversandRepeatKindDataCol {

	private String kindName;
	private String kindVorname;
	private LocalDate kindGeburtsdatum;
	private String kindInstitutionKita;
	private String kindInstitutionTagesfamilie;
	private String kindInstitutionTagesschule;
	private String kindInstitutionFerieninsel;
	private String kindInstitutionenWeitere;

	public String getKindName() {
		return kindName;
	}

	public void setKindName(String kindName) {
		this.kindName = kindName;
	}

	public String getKindVorname() {
		return kindVorname;
	}

	public void setKindVorname(String kindVorname) {
		this.kindVorname = kindVorname;
	}

	public LocalDate getKindGeburtsdatum() {
		return kindGeburtsdatum;
	}

	public void setKindGeburtsdatum(LocalDate kindGeburtsdatum) {
		this.kindGeburtsdatum = kindGeburtsdatum;
	}

	public String getKindInstitutionKita() {
		return kindInstitutionKita;
	}

	public void setKindInstitutionKita(String kindInstitutionKita) {
		this.kindInstitutionKita = kindInstitutionKita;
	}

	public String getKindInstitutionTagesfamilie() {
		return kindInstitutionTagesfamilie;
	}

	public void setKindInstitutionTagesfamilie(String kindInstitutionTagesfamilie) {
		this.kindInstitutionTagesfamilie = kindInstitutionTagesfamilie;
	}

	public String getKindInstitutionTagesschule() {
		return kindInstitutionTagesschule;
	}

	public void setKindInstitutionTagesschule(String kindInstitutionTagesschule) {
		this.kindInstitutionTagesschule = kindInstitutionTagesschule;
	}

	public String getKindInstitutionFerieninsel() {
		return kindInstitutionFerieninsel;
	}

	public void setKindInstitutionFerieninsel(String kindInstitutionFerieninsel) {
		this.kindInstitutionFerieninsel = kindInstitutionFerieninsel;
	}

	public String getKindInstitutionenWeitere() {
		return kindInstitutionenWeitere;
	}

	public void setKindInstitutionenWeitere(String kindInstitutionenWeitere) {
		this.kindInstitutionenWeitere = kindInstitutionenWeitere;
	}

	private void addKindInstitutionenWeitere(@Nonnull String instName) {
		setKindInstitutionenWeitere(
			Strings.isNullOrEmpty(getKindInstitutionenWeitere())
				? instName
				: getKindInstitutionenWeitere() + ", " + instName
		);
	}

	public void setKindInstitutionKitaOrWeitere(@Nonnull String instName) {
		if (Strings.isNullOrEmpty(getKindInstitutionKita())) {
			setKindInstitutionKita(instName);
		} else {
			addKindInstitutionenWeitere(instName);
		}
	}

	public void setKindInstitutionTagesfamilieOrWeitere(@Nonnull String instName) {
		if (Strings.isNullOrEmpty(getKindInstitutionTagesfamilie())) {
			setKindInstitutionTagesfamilie(instName);
		} else {
			addKindInstitutionenWeitere(instName);
		}
	}

	public void setKindInstitutionTagesschuleOrWeitere(@Nonnull String instName) {
		if (Strings.isNullOrEmpty(getKindInstitutionTagesschule())) {
			setKindInstitutionTagesschule(instName);
		} else {
			addKindInstitutionenWeitere(instName);
		}
	}

	public void setKindInstitutionFerieninselOrWeitere(@Nonnull String instName) {
		if (Strings.isNullOrEmpty(getKindInstitutionFerieninsel())) {
			setKindInstitutionFerieninsel(instName);
		} else {
			addKindInstitutionenWeitere(instName);
		}
	}
}
