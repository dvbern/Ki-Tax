/*
 * Copyright (C) 2021 DV Bern AG,
 *  Switzerland
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation,
 *  either version 3 of the
 * License,
 *  or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 *
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not,
 *  see <https://www.gnu.org/licenses/>.
 */

package ch.dvbern.ebegu.dto;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import ch.dvbern.ebegu.entities.Adresse;

public class FinanzielleSituationStartDTO {

	final private @Nonnull Boolean sozialhilfebezueger;
	final private @Nullable Boolean gemeinsameSteuererklaerung;
	final private Boolean verguenstigungGewuenscht;
	final private boolean keineMahlzeitenverguenstigungGewuenscht;
	final private @Nullable String iban;
	final private @Nullable String kontoinhaber;
	final private boolean abweichendeZahlungsadresse;
	final private @Nullable Adresse zahlungsadresse;
	final private @Nullable String ibanInfoma;
	final private @Nullable String kontoinhaberInfoma;
	final private boolean abweichendeZahlungsadresseInfoma;
	final private @Nullable Adresse zahlungsadresseInfoma;
	final private @Nullable String infomaKreditorennummer;
	final private @Nullable String infomaBankcode;
	final private @Nullable Boolean auszahlungAnEltern;

	public FinanzielleSituationStartDTO(
		@Nonnull Boolean sozialhilfebezueger,
		@Nullable Boolean gemeinsameSteuererklaerung,
		Boolean verguenstigungGewuenscht,
		boolean keineMahlzeitenverguenstigungGewuenscht,
		@Nullable String iban,
		@Nullable String kontoinhaber,
		boolean abweichendeZahlungsadresse,
		@Nullable Adresse zahlungsadresse,
		@Nullable String ibanInfoma,
		@Nullable String kontoinhaberInfoma,
		boolean abweichendeZahlungsadresseInfoma,
		@Nullable Adresse zahlungsadresseInfoma,
		@Nullable String infomaKreditorennummer,
		@Nullable String infomaBankcode,
		@Nullable Boolean auszahlungAnEltern
	) {
		this.sozialhilfebezueger = sozialhilfebezueger;
		this.gemeinsameSteuererklaerung = gemeinsameSteuererklaerung;
		this.verguenstigungGewuenscht = verguenstigungGewuenscht;
		this.keineMahlzeitenverguenstigungGewuenscht = keineMahlzeitenverguenstigungGewuenscht;
		this.iban = iban;
		this.kontoinhaber = kontoinhaber;
		this.abweichendeZahlungsadresse = abweichendeZahlungsadresse;
		this.zahlungsadresse = zahlungsadresse;
		this.ibanInfoma = ibanInfoma;
		this.kontoinhaberInfoma = kontoinhaberInfoma;
		this.abweichendeZahlungsadresseInfoma = abweichendeZahlungsadresseInfoma;
		this.zahlungsadresseInfoma = zahlungsadresseInfoma;
		this.infomaKreditorennummer = infomaKreditorennummer;
		this.infomaBankcode = infomaBankcode;
		this.auszahlungAnEltern = auszahlungAnEltern;
	}

	@Nonnull
	public Boolean getSozialhilfebezueger() {
		return sozialhilfebezueger;
	}

	@Nullable
	public Boolean getGemeinsameSteuererklaerung() {
		return gemeinsameSteuererklaerung;
	}

	public Boolean getVerguenstigungGewuenscht() {
		return verguenstigungGewuenscht;
	}

	public boolean isKeineMahlzeitenverguenstigungGewuenscht() {
		return keineMahlzeitenverguenstigungGewuenscht;
	}

	@Nullable
	public String getIban() {
		return iban;
	}

	@Nullable
	public String getKontoinhaber() {
		return kontoinhaber;
	}

	public boolean isAbweichendeZahlungsadresse() {
		return abweichendeZahlungsadresse;
	}

	@Nullable
	public Adresse getZahlungsadresse() {
		return zahlungsadresse;
	}

	@Nullable
	public String getIbanInfoma() {
		return ibanInfoma;
	}

	@Nullable
	public String getKontoinhaberInfoma() {
		return kontoinhaberInfoma;
	}

	public boolean isAbweichendeZahlungsadresseInfoma() {
		return abweichendeZahlungsadresseInfoma;
	}

	@Nullable
	public Adresse getZahlungsadresseInfoma() {
		return zahlungsadresseInfoma;
	}

	@Nullable
	public String getInfomaKreditorennummer() {
		return infomaKreditorennummer;
	}

	@Nullable
	public String getInfomaBankcode() {
		return infomaBankcode;
	}

	@Nullable
	public Boolean getAuszahlungAnEltern() {
		return auszahlungAnEltern;
	}
}
