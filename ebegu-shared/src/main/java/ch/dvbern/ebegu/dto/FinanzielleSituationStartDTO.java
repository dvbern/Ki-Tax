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

import java.time.LocalDate;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import ch.dvbern.ebegu.entities.Adresse;

public class FinanzielleSituationStartDTO {

	final private @Nonnull
	Boolean sozialhilfeBezueger;
	final private @Nullable
	String zustaendigeAmtsstelle;
	final private @Nullable
	String nameBetreuer;
	final private @Nullable
	Boolean gemeinsameSteuererklaerung;
	final private Boolean verguenstigungGewuenscht;
	final private boolean keineMahlzeitenverguenstigungGewuenscht;
	final private @Nullable
	String iban;
	final private @Nullable
	String kontoinhaber;
	final private boolean abweichendeZahlungsadresse;
	final private @Nullable
	Adresse zahlungsadresse;
	final private @Nullable
	String infomaKreditorennummer;
	final private @Nullable
	String infomaBankcode;
	final private @Nullable
	LocalDate finSitAenderungGueltigAbDatum;

	final private boolean auszahlungAusserhalbVonKibon;

	public FinanzielleSituationStartDTO(
		@Nonnull Boolean sozialhilfeBezueger,
		@Nullable String zustaendigeAmtsstelle,
		@Nullable String nameBetreuer,
		@Nullable Boolean gemeinsameSteuererklaerung,
		Boolean verguenstigungGewuenscht,
		boolean keineMahlzeitenverguenstigungGewuenscht,
		@Nullable String iban,
		@Nullable String kontoinhaber,
		boolean abweichendeZahlungsadresse,
		@Nullable Adresse zahlungsadresse,
		@Nullable String infomaKreditorennummer,
		@Nullable String infomaBankcode,
		@Nullable LocalDate finSitAenderungGueltigAbDatum,
		boolean auszahlungAusserhalbVonKibon) {
		this.sozialhilfeBezueger = sozialhilfeBezueger;
		this.zustaendigeAmtsstelle = zustaendigeAmtsstelle;
		this.nameBetreuer = nameBetreuer;
		this.gemeinsameSteuererklaerung = gemeinsameSteuererklaerung;
		this.verguenstigungGewuenscht = verguenstigungGewuenscht;
		this.keineMahlzeitenverguenstigungGewuenscht = keineMahlzeitenverguenstigungGewuenscht;
		this.iban = iban;
		this.kontoinhaber = kontoinhaber;
		this.abweichendeZahlungsadresse = abweichendeZahlungsadresse;
		this.zahlungsadresse = zahlungsadresse;
		this.infomaKreditorennummer = infomaKreditorennummer;
		this.infomaBankcode = infomaBankcode;
		this.finSitAenderungGueltigAbDatum = finSitAenderungGueltigAbDatum;
		this.auszahlungAusserhalbVonKibon = auszahlungAusserhalbVonKibon;
	}

	@Nonnull
	public Boolean getSozialhilfeBezueger() {
		return sozialhilfeBezueger;
	}

	@Nullable
	public String getZustaendigeAmtsstelle() {
		return zustaendigeAmtsstelle;
	}

	@Nullable
	public String getNameBetreuer() {
		return nameBetreuer;
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
	public String getInfomaKreditorennummer() {
		return infomaKreditorennummer;
	}

	@Nullable
	public String getInfomaBankcode() {
		return infomaBankcode;
	}

	@Nullable
	public LocalDate getFinSitAenderungGueltigAbDatum() {
		return finSitAenderungGueltigAbDatum;
	}

	public boolean isAuszahlungAusserhalbVonKibon() {
		return auszahlungAusserhalbVonKibon;
	}
}
