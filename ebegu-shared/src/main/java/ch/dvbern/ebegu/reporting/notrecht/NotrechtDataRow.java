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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package ch.dvbern.ebegu.reporting.notrecht;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import ch.dvbern.ebegu.enums.BetreuungsangebotTyp;
import ch.dvbern.ebegu.enums.RueckforderungInstitutionTyp;
import ch.dvbern.ebegu.enums.RueckforderungStatus;

/**
 * DTO fuer die Statistik Notrecht
 */
public class NotrechtDataRow {

	private @Nonnull String institution;
	private @Nonnull RueckforderungStatus status;
	private @Nonnull BetreuungsangebotTyp betreuungsangebotTyp;
	private @Nullable String traegerschaft;
	private @Nullable String email;
	private @Nullable String adresseOrganisation;
	private @Nullable String adresseStrasse;
	private @Nullable String adresseHausnummer;
	private @Nullable String adressePlz;
	private @Nullable String adresseOrt;
	private @Nullable String telefon;
	private @Nullable BigDecimal stufe1InstitutionKostenuebernahmeAnzahlTage;
	private @Nullable BigDecimal stufe1InstitutionKostenuebernahmeAnzahlStunden;
	private @Nullable BigDecimal stufe1InstitutionKostenuebernahmeBetreuung;
	private @Nullable BigDecimal stufe1KantonKostenuebernahmeAnzahlTage;
	private @Nullable BigDecimal stufe1KantonKostenuebernahmeAnzahlStunden;
	private @Nullable BigDecimal stufe1KantonKostenuebernahmeBetreuung;
	private @Nullable BigDecimal stufe1FreigabeBetrag;
	private @Nullable LocalDateTime stufe1FreigabeDatum;
	private @Nullable LocalDateTime stufe1FreigabeAusbezahltAm;
	private @Nullable String stufe1ZahlungJetztAusgeloest;
	private @Nullable RueckforderungInstitutionTyp institutionTyp;
	private @Nullable BigDecimal stufe2InstitutionKostenuebernahmeAnzahlTage;
	private @Nullable BigDecimal stufe2InstitutionKostenuebernahmeAnzahlStunden;
	private @Nullable BigDecimal stufe2InstitutionKostenuebernahmeBetreuung;
	private @Nullable BigDecimal stufe2KantonKostenuebernahmeAnzahlTage;
	private @Nullable BigDecimal stufe2KantonKostenuebernahmeAnzahlStunden;
	private @Nullable BigDecimal stufe2KantonKostenuebernahmeBetreuung;
	private @Nullable BigDecimal betragEntgangeneElternbeitraege;
	private @Nullable BigDecimal betragEntgangeneElternbeitraegeNichtAngeboteneEinheiten;
	private @Nullable BigDecimal rueckerstattungNichtAngeboteneBetreuungstage;
	private @Nullable BigDecimal kurzarbeitBetrag;
	private @Nullable BigDecimal coronaErwerbsersatzBetrag;
	private @Nullable BigDecimal stufe2VerfuegungBetrag;
	private @Nullable LocalDateTime stufe2VerfuegungDatum;
	private @Nullable LocalDateTime stufe2VerfuegungAusbezahltAm;
	private @Nullable String stufe2ZahlungJetztAusgeloest;
	private @Nullable BigDecimal beschwerdeBetrag;
	private @Nullable LocalDateTime beschwerdeAusbezahltAm;
	private @Nullable String beschwerdeZahlungJetztAusgeloest;
	private @Nullable String iban;
	private @Nullable String kontoinhaber;
	private @Nullable String auszahlungOrganisation;
	private @Nullable String auszahlungStrasse;
	private @Nullable String auszahlungHausnummer;
	private @Nullable String auszahlungPlz;
	private @Nullable String auszahlungOrt;

	public @Nonnull String getInstitution() {
		return institution;
	}

	public void setInstitution(@Nonnull String institution) {
		this.institution = institution;
	}

	public @Nonnull RueckforderungStatus getStatus() {
		return status;
	}

	public void setStatus(@Nonnull RueckforderungStatus status) {
		this.status = status;
	}

	public @Nonnull BetreuungsangebotTyp getBetreuungsangebotTyp() {
		return betreuungsangebotTyp;
	}

	public void setBetreuungsangebotTyp(@Nonnull BetreuungsangebotTyp betreuungsangebotTyp) {
		this.betreuungsangebotTyp = betreuungsangebotTyp;
	}

	public @Nullable String getTraegerschaft() {
		return traegerschaft;
	}

	public void setTraegerschaft(@Nullable String traegerschaft) {
		this.traegerschaft = traegerschaft;
	}

	public @Nullable String getEmail() {
		return email;
	}

	public void setEmail(@Nullable String email) {
		this.email = email;
	}

	public @Nullable String getAdresseOrganisation() {
		return adresseOrganisation;
	}

	public void setAdresseOrganisation(@Nullable String adresseOrganisation) {
		this.adresseOrganisation = adresseOrganisation;
	}

	public @Nullable String getAdresseStrasse() {
		return adresseStrasse;
	}

	public void setAdresseStrasse(@Nullable String adresseStrasse) {
		this.adresseStrasse = adresseStrasse;
	}

	public @Nullable String getAdresseHausnummer() {
		return adresseHausnummer;
	}

	public void setAdresseHausnummer(@Nullable String adresseHausnummer) {
		this.adresseHausnummer = adresseHausnummer;
	}

	public @Nullable String getAdressePlz() {
		return adressePlz;
	}

	public void setAdressePlz(@Nullable String adressePlz) {
		this.adressePlz = adressePlz;
	}

	public @Nullable String getAdresseOrt() {
		return adresseOrt;
	}

	public void setAdresseOrt(@Nullable String adresseOrt) {
		this.adresseOrt = adresseOrt;
	}

	public @Nullable String getTelefon() {
		return telefon;
	}

	public void setTelefon(@Nullable String telefon) {
		this.telefon = telefon;
	}

	public @Nullable BigDecimal getStufe1InstitutionKostenuebernahmeAnzahlTage() {
		return stufe1InstitutionKostenuebernahmeAnzahlTage;
	}

	public void setStufe1InstitutionKostenuebernahmeAnzahlTage(@Nullable BigDecimal stufe1InstitutionKostenuebernahmeAnzahlTage) {
		this.stufe1InstitutionKostenuebernahmeAnzahlTage = stufe1InstitutionKostenuebernahmeAnzahlTage;
	}

	public @Nullable BigDecimal getStufe1InstitutionKostenuebernahmeAnzahlStunden() {
		return stufe1InstitutionKostenuebernahmeAnzahlStunden;
	}

	public void setStufe1InstitutionKostenuebernahmeAnzahlStunden(@Nullable BigDecimal stufe1InstitutionKostenuebernahmeAnzahlStunden) {
		this.stufe1InstitutionKostenuebernahmeAnzahlStunden = stufe1InstitutionKostenuebernahmeAnzahlStunden;
	}

	public @Nullable BigDecimal getStufe1InstitutionKostenuebernahmeBetreuung() {
		return stufe1InstitutionKostenuebernahmeBetreuung;
	}

	public void setStufe1InstitutionKostenuebernahmeBetreuung(@Nullable BigDecimal stufe1InstitutionKostenuebernahmeBetreuung) {
		this.stufe1InstitutionKostenuebernahmeBetreuung = stufe1InstitutionKostenuebernahmeBetreuung;
	}

	public @Nullable BigDecimal getStufe1KantonKostenuebernahmeAnzahlTage() {
		return stufe1KantonKostenuebernahmeAnzahlTage;
	}

	public void setStufe1KantonKostenuebernahmeAnzahlTage(@Nullable BigDecimal stufe1KantonKostenuebernahmeAnzahlTage) {
		this.stufe1KantonKostenuebernahmeAnzahlTage = stufe1KantonKostenuebernahmeAnzahlTage;
	}

	public @Nullable BigDecimal getStufe1KantonKostenuebernahmeAnzahlStunden() {
		return stufe1KantonKostenuebernahmeAnzahlStunden;
	}

	public void setStufe1KantonKostenuebernahmeAnzahlStunden(@Nullable BigDecimal stufe1KantonKostenuebernahmeAnzahlStunden) {
		this.stufe1KantonKostenuebernahmeAnzahlStunden = stufe1KantonKostenuebernahmeAnzahlStunden;
	}

	public @Nullable BigDecimal getStufe1KantonKostenuebernahmeBetreuung() {
		return stufe1KantonKostenuebernahmeBetreuung;
	}

	public void setStufe1KantonKostenuebernahmeBetreuung(@Nullable BigDecimal stufe1KantonKostenuebernahmeBetreuung) {
		this.stufe1KantonKostenuebernahmeBetreuung = stufe1KantonKostenuebernahmeBetreuung;
	}

	public @Nullable BigDecimal getStufe1FreigabeBetrag() {
		return stufe1FreigabeBetrag;
	}

	public void setStufe1FreigabeBetrag(@Nullable BigDecimal stufe1FreigabeBetrag) {
		this.stufe1FreigabeBetrag = stufe1FreigabeBetrag;
	}

	public @Nullable LocalDateTime getStufe1FreigabeDatum() {
		return stufe1FreigabeDatum;
	}

	public void setStufe1FreigabeDatum(@Nullable LocalDateTime stufe1FreigabeDatum) {
		this.stufe1FreigabeDatum = stufe1FreigabeDatum;
	}

	public @Nullable LocalDateTime getStufe1FreigabeAusbezahltAm() {
		return stufe1FreigabeAusbezahltAm;
	}

	public void setStufe1FreigabeAusbezahltAm(@Nullable LocalDateTime stufe1FreigabeAusbezahltAm) {
		this.stufe1FreigabeAusbezahltAm = stufe1FreigabeAusbezahltAm;
	}

	public @Nullable BigDecimal getStufe2InstitutionKostenuebernahmeAnzahlTage() {
		return stufe2InstitutionKostenuebernahmeAnzahlTage;
	}

	public void setStufe2InstitutionKostenuebernahmeAnzahlTage(@Nullable BigDecimal stufe2InstitutionKostenuebernahmeAnzahlTage) {
		this.stufe2InstitutionKostenuebernahmeAnzahlTage = stufe2InstitutionKostenuebernahmeAnzahlTage;
	}

	public @Nullable BigDecimal getStufe2InstitutionKostenuebernahmeAnzahlStunden() {
		return stufe2InstitutionKostenuebernahmeAnzahlStunden;
	}

	public void setStufe2InstitutionKostenuebernahmeAnzahlStunden(@Nullable BigDecimal stufe2InstitutionKostenuebernahmeAnzahlStunden) {
		this.stufe2InstitutionKostenuebernahmeAnzahlStunden = stufe2InstitutionKostenuebernahmeAnzahlStunden;
	}

	public @Nullable BigDecimal getStufe2InstitutionKostenuebernahmeBetreuung() {
		return stufe2InstitutionKostenuebernahmeBetreuung;
	}

	public void setStufe2InstitutionKostenuebernahmeBetreuung(@Nullable BigDecimal stufe2InstitutionKostenuebernahmeBetreuung) {
		this.stufe2InstitutionKostenuebernahmeBetreuung = stufe2InstitutionKostenuebernahmeBetreuung;
	}

	public @Nullable BigDecimal getStufe2KantonKostenuebernahmeAnzahlTage() {
		return stufe2KantonKostenuebernahmeAnzahlTage;
	}

	public void setStufe2KantonKostenuebernahmeAnzahlTage(@Nullable BigDecimal stufe2KantonKostenuebernahmeAnzahlTage) {
		this.stufe2KantonKostenuebernahmeAnzahlTage = stufe2KantonKostenuebernahmeAnzahlTage;
	}

	public @Nullable BigDecimal getStufe2KantonKostenuebernahmeAnzahlStunden() {
		return stufe2KantonKostenuebernahmeAnzahlStunden;
	}

	public void setStufe2KantonKostenuebernahmeAnzahlStunden(@Nullable BigDecimal stufe2KantonKostenuebernahmeAnzahlStunden) {
		this.stufe2KantonKostenuebernahmeAnzahlStunden = stufe2KantonKostenuebernahmeAnzahlStunden;
	}

	public @Nullable BigDecimal getStufe2KantonKostenuebernahmeBetreuung() {
		return stufe2KantonKostenuebernahmeBetreuung;
	}

	public void setStufe2KantonKostenuebernahmeBetreuung(@Nullable BigDecimal stufe2KantonKostenuebernahmeBetreuung) {
		this.stufe2KantonKostenuebernahmeBetreuung = stufe2KantonKostenuebernahmeBetreuung;
	}

	@Nullable
	public BigDecimal getBetragEntgangeneElternbeitraege() {
		return betragEntgangeneElternbeitraege;
	}

	public void setBetragEntgangeneElternbeitraege(@Nullable BigDecimal betragEntgangeneElternbeitraege) {
		this.betragEntgangeneElternbeitraege = betragEntgangeneElternbeitraege;
	}

	@Nullable
	public BigDecimal getBetragEntgangeneElternbeitraegeNichtAngeboteneEinheiten() {
		return betragEntgangeneElternbeitraegeNichtAngeboteneEinheiten;
	}

	public void setBetragEntgangeneElternbeitraegeNichtAngeboteneEinheiten(@Nullable BigDecimal betragEntgangeneElternbeitraegeNichtAngeboteneEinheiten) {
		this.betragEntgangeneElternbeitraegeNichtAngeboteneEinheiten = betragEntgangeneElternbeitraegeNichtAngeboteneEinheiten;
	}

	@Nullable
	public BigDecimal getRueckerstattungNichtAngeboteneBetreuungstage() {
		return rueckerstattungNichtAngeboteneBetreuungstage;
	}

	public void setRueckerstattungNichtAngeboteneBetreuungstage(@Nullable BigDecimal rueckerstattungNichtAngeboteneBetreuungstage) {
		this.rueckerstattungNichtAngeboteneBetreuungstage = rueckerstattungNichtAngeboteneBetreuungstage;
	}

	@Nullable
	public BigDecimal getKurzarbeitBetrag() {
		return kurzarbeitBetrag;
	}

	public void setKurzarbeitBetrag(@Nullable BigDecimal kurzarbeitBetrag) {
		this.kurzarbeitBetrag = kurzarbeitBetrag;
	}

	@Nullable
	public BigDecimal getCoronaErwerbsersatzBetrag() {
		return coronaErwerbsersatzBetrag;
	}

	public void setCoronaErwerbsersatzBetrag(@Nullable BigDecimal coronaErwerbsersatzBetrag) {
		this.coronaErwerbsersatzBetrag = coronaErwerbsersatzBetrag;
	}

	public @Nullable BigDecimal getStufe2VerfuegungBetrag() {
		return stufe2VerfuegungBetrag;
	}

	public void setStufe2VerfuegungBetrag(@Nullable BigDecimal stufe2VerfuegungBetrag) {
		this.stufe2VerfuegungBetrag = stufe2VerfuegungBetrag;
	}

	public @Nullable LocalDateTime getStufe2VerfuegungDatum() {
		return stufe2VerfuegungDatum;
	}

	public void setStufe2VerfuegungDatum(@Nullable LocalDateTime stufe2VerfuegungDatum) {
		this.stufe2VerfuegungDatum = stufe2VerfuegungDatum;
	}

	public @Nullable LocalDateTime getStufe2VerfuegungAusbezahltAm() {
		return stufe2VerfuegungAusbezahltAm;
	}

	public void setStufe2VerfuegungAusbezahltAm(@Nullable LocalDateTime stufe2VerfuegungAusbezahltAm) {
		this.stufe2VerfuegungAusbezahltAm = stufe2VerfuegungAusbezahltAm;
	}

	public @Nullable String getIban() {
		return iban;
	}

	public void setIban(@Nullable String iban) {
		this.iban = iban;
	}

	public @Nullable String getKontoinhaber() {
		return kontoinhaber;
	}

	public void setKontoinhaber(@Nullable String kontoinhaber) {
		this.kontoinhaber = kontoinhaber;
	}

	public @Nullable String getAuszahlungOrganisation() {
		return auszahlungOrganisation;
	}

	public void setAuszahlungOrganisation(@Nullable String auszahlungOrganisation) {
		this.auszahlungOrganisation = auszahlungOrganisation;
	}

	public @Nullable String getAuszahlungStrasse() {
		return auszahlungStrasse;
	}

	public void setAuszahlungStrasse(@Nullable String auszahlungStrasse) {
		this.auszahlungStrasse = auszahlungStrasse;
	}

	public @Nullable String getAuszahlungHausnummer() {
		return auszahlungHausnummer;
	}

	public void setAuszahlungHausnummer(@Nullable String auszahlungHausnummer) {
		this.auszahlungHausnummer = auszahlungHausnummer;
	}

	public @Nullable String getAuszahlungPlz() {
		return auszahlungPlz;
	}

	public void setAuszahlungPlz(@Nullable String auszahlungPlz) {
		this.auszahlungPlz = auszahlungPlz;
	}

	public @Nullable String getAuszahlungOrt() {
		return auszahlungOrt;
	}

	public void setAuszahlungOrt(@Nullable String auszahlungOrt) {
		this.auszahlungOrt = auszahlungOrt;
	}

	@Nullable
	public String getStufe1ZahlungJetztAusgeloest() {
		return stufe1ZahlungJetztAusgeloest;
	}

	public void setStufe1ZahlungJetztAusgeloest(@Nullable String stufe1ZahlungJetztAusgeloest) {
		this.stufe1ZahlungJetztAusgeloest = stufe1ZahlungJetztAusgeloest;
	}

	@Nullable
	public RueckforderungInstitutionTyp getInstitutionTyp() {
		return institutionTyp;
	}

	public void setInstitutionTyp(@Nullable RueckforderungInstitutionTyp institutionTyp) {
		this.institutionTyp = institutionTyp;
	}

	@Nullable
	public String getStufe2ZahlungJetztAusgeloest() {
		return stufe2ZahlungJetztAusgeloest;
	}

	public void setStufe2ZahlungJetztAusgeloest(@Nullable String stufe2ZahlungJetztAusgeloest) {
		this.stufe2ZahlungJetztAusgeloest = stufe2ZahlungJetztAusgeloest;
	}

	@Nullable
	public BigDecimal getBeschwerdeBetrag() {
		return beschwerdeBetrag;
	}

	public void setBeschwerdeBetrag(@Nullable BigDecimal beschwerdeBetrag) {
		this.beschwerdeBetrag = beschwerdeBetrag;
	}

	@Nullable
	public LocalDateTime getBeschwerdeAusbezahltAm() {
		return beschwerdeAusbezahltAm;
	}

	public void setBeschwerdeAusbezahltAm(@Nullable LocalDateTime beschwerdeAusbezahltAm) {
		this.beschwerdeAusbezahltAm = beschwerdeAusbezahltAm;
	}

	@Nullable
	public String getBeschwerdeZahlungJetztAusgeloest() {
		return beschwerdeZahlungJetztAusgeloest;
	}

	public void setBeschwerdeZahlungJetztAusgeloest(@Nullable String beschwerdeZahlungJetztAusgeloest) {
		this.beschwerdeZahlungJetztAusgeloest = beschwerdeZahlungJetztAusgeloest;
	}
}
