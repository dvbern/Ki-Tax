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

	@Nonnull
	private String institution;
	@Nonnull
	private RueckforderungStatus status;
	@Nonnull
	private BetreuungsangebotTyp betreuungsangebotTyp;
	@Nullable
	private String traegerschaft;
	@Nullable
	private String email;
	@Nullable
	private String adresseOrganisation;
	@Nullable
	private String adresseStrasse;
	@Nullable
	private String adresseHausnummer;
	@Nullable
	private String adressePlz;
	@Nullable
	private String adresseOrt;
	@Nullable
	private String telefon;
	@Nullable
	private BigDecimal stufe1InstitutionKostenuebernahmeAnzahlTage;
	@Nullable
	private BigDecimal stufe1InstitutionKostenuebernahmeAnzahlStunden;
	@Nullable
	private BigDecimal stufe1InstitutionKostenuebernahmeBetreuung;
	@Nullable
	private BigDecimal stufe1KantonKostenuebernahmeAnzahlTage;
	@Nullable
	private BigDecimal stufe1KantonKostenuebernahmeAnzahlStunden;
	@Nullable
	private BigDecimal stufe1KantonKostenuebernahmeBetreuung;
	@Nullable
	private BigDecimal stufe1FreigabeBetrag;
	@Nullable
	private LocalDateTime stufe1FreigabeDatum;
	@Nullable
	private LocalDateTime stufe1FreigabeAusbezahltAm;
	@Nullable
	private String stufe1ZahlungJetztAusgeloest;
	@Nullable
	private RueckforderungInstitutionTyp institutionTyp;
	@Nullable
	private BigDecimal stufe2InstitutionKostenuebernahmeAnzahlTage;
	@Nullable
	private BigDecimal stufe2InstitutionKostenuebernahmeAnzahlStunden;
	@Nullable
	private BigDecimal stufe2InstitutionKostenuebernahmeBetreuung;
	@Nullable
	private BigDecimal stufe2KantonKostenuebernahmeAnzahlTage;
	@Nullable
	private BigDecimal stufe2KantonKostenuebernahmeAnzahlStunden;
	@Nullable
	private BigDecimal stufe2KantonKostenuebernahmeBetreuung;
	@Nullable
	private BigDecimal betragEntgangeneElternbeitraege;
	@Nullable
	private BigDecimal betragEntgangeneElternbeitraegeNichtAngeboteneEinheiten;
	@Nullable
	private BigDecimal rueckerstattungNichtAngeboteneBetreuungstage;
	@Nullable
	private BigDecimal kurzarbeitBetrag;
	@Nullable
	private BigDecimal coronaErwerbsersatzBetrag;
	@Nullable
	private BigDecimal stufe2VerfuegungBetrag;
	@Nullable
	private LocalDateTime stufe2VerfuegungDatum;
	@Nullable
	private LocalDateTime stufe2VerfuegungAusbezahltAm;
	@Nullable
	private String stufe2ZahlungJetztAusgeloest;
	@Nullable
	private BigDecimal beschwerdeBetrag;
	@Nullable
	private LocalDateTime beschwerdeAusbezahltAm;
	@Nullable
	private String beschwerdeZahlungJetztAusgeloest;
	@Nullable
	private String iban;
	@Nullable
	private String kontoinhaber;
	@Nullable
	private String auszahlungOrganisation;
	@Nullable
	private String auszahlungStrasse;
	@Nullable
	private String auszahlungHausnummer;
	@Nullable
	private String auszahlungPlz;
	@Nullable
	private String auszahlungOrt;

	@Nonnull
	public String getInstitution() {
		return institution;
	}

	public void setInstitution(@Nonnull String institution) {
		this.institution = institution;
	}

	public @Nonnull
	RueckforderungStatus getStatus() {
		return status;
	}

	public void setStatus(@Nonnull RueckforderungStatus status) {
		this.status = status;
	}

	public @Nonnull
	BetreuungsangebotTyp getBetreuungsangebotTyp() {
		return betreuungsangebotTyp;
	}

	public void setBetreuungsangebotTyp(@Nonnull BetreuungsangebotTyp betreuungsangebotTyp) {
		this.betreuungsangebotTyp = betreuungsangebotTyp;
	}

	@Nullable
	public String getTraegerschaft() {
		return traegerschaft;
	}

	public void setTraegerschaft(@Nullable String traegerschaft) {
		this.traegerschaft = traegerschaft;
	}

	@Nullable
	public String getEmail() {
		return email;
	}

	public void setEmail(@Nullable String email) {
		this.email = email;
	}

	@Nullable
	public String getAdresseOrganisation() {
		return adresseOrganisation;
	}

	public void setAdresseOrganisation(@Nullable String adresseOrganisation) {
		this.adresseOrganisation = adresseOrganisation;
	}

	@Nullable
	public String getAdresseStrasse() {
		return adresseStrasse;
	}

	public void setAdresseStrasse(@Nullable String adresseStrasse) {
		this.adresseStrasse = adresseStrasse;
	}

	@Nullable
	public String getAdresseHausnummer() {
		return adresseHausnummer;
	}

	public void setAdresseHausnummer(@Nullable String adresseHausnummer) {
		this.adresseHausnummer = adresseHausnummer;
	}

	@Nullable
	public String getAdressePlz() {
		return adressePlz;
	}

	public void setAdressePlz(@Nullable String adressePlz) {
		this.adressePlz = adressePlz;
	}

	@Nullable
	public String getAdresseOrt() {
		return adresseOrt;
	}

	public void setAdresseOrt(@Nullable String adresseOrt) {
		this.adresseOrt = adresseOrt;
	}

	@Nullable
	public String getTelefon() {
		return telefon;
	}

	public void setTelefon(@Nullable String telefon) {
		this.telefon = telefon;
	}

	@Nullable
	public BigDecimal getStufe1InstitutionKostenuebernahmeAnzahlTage() {
		return stufe1InstitutionKostenuebernahmeAnzahlTage;
	}

	public void setStufe1InstitutionKostenuebernahmeAnzahlTage(
		@Nullable BigDecimal stufe1InstitutionKostenuebernahmeAnzahlTage) {
		this.stufe1InstitutionKostenuebernahmeAnzahlTage = stufe1InstitutionKostenuebernahmeAnzahlTage;
	}

	@Nullable
	public BigDecimal getStufe1InstitutionKostenuebernahmeAnzahlStunden() {
		return stufe1InstitutionKostenuebernahmeAnzahlStunden;
	}

	public void setStufe1InstitutionKostenuebernahmeAnzahlStunden(
		@Nullable BigDecimal stufe1InstitutionKostenuebernahmeAnzahlStunden) {
		this.stufe1InstitutionKostenuebernahmeAnzahlStunden = stufe1InstitutionKostenuebernahmeAnzahlStunden;
	}

	@Nullable
	public BigDecimal getStufe1InstitutionKostenuebernahmeBetreuung() {
		return stufe1InstitutionKostenuebernahmeBetreuung;
	}

	public void setStufe1InstitutionKostenuebernahmeBetreuung(
		@Nullable BigDecimal stufe1InstitutionKostenuebernahmeBetreuung) {
		this.stufe1InstitutionKostenuebernahmeBetreuung = stufe1InstitutionKostenuebernahmeBetreuung;
	}

	@Nullable
	public BigDecimal getStufe1KantonKostenuebernahmeAnzahlTage() {
		return stufe1KantonKostenuebernahmeAnzahlTage;
	}

	public void setStufe1KantonKostenuebernahmeAnzahlTage(@Nullable BigDecimal stufe1KantonKostenuebernahmeAnzahlTage) {
		this.stufe1KantonKostenuebernahmeAnzahlTage = stufe1KantonKostenuebernahmeAnzahlTage;
	}

	@Nullable
	public BigDecimal getStufe1KantonKostenuebernahmeAnzahlStunden() {
		return stufe1KantonKostenuebernahmeAnzahlStunden;
	}

	public void setStufe1KantonKostenuebernahmeAnzahlStunden(
		@Nullable BigDecimal stufe1KantonKostenuebernahmeAnzahlStunden) {
		this.stufe1KantonKostenuebernahmeAnzahlStunden = stufe1KantonKostenuebernahmeAnzahlStunden;
	}

	@Nullable
	public BigDecimal getStufe1KantonKostenuebernahmeBetreuung() {
		return stufe1KantonKostenuebernahmeBetreuung;
	}

	public void setStufe1KantonKostenuebernahmeBetreuung(@Nullable BigDecimal stufe1KantonKostenuebernahmeBetreuung) {
		this.stufe1KantonKostenuebernahmeBetreuung = stufe1KantonKostenuebernahmeBetreuung;
	}

	@Nullable
	public BigDecimal getStufe1FreigabeBetrag() {
		return stufe1FreigabeBetrag;
	}

	public void setStufe1FreigabeBetrag(@Nullable BigDecimal stufe1FreigabeBetrag) {
		this.stufe1FreigabeBetrag = stufe1FreigabeBetrag;
	}

	@Nullable
	public LocalDateTime getStufe1FreigabeDatum() {
		return stufe1FreigabeDatum;
	}

	public void setStufe1FreigabeDatum(@Nullable LocalDateTime stufe1FreigabeDatum) {
		this.stufe1FreigabeDatum = stufe1FreigabeDatum;
	}

	@Nullable
	public LocalDateTime getStufe1FreigabeAusbezahltAm() {
		return stufe1FreigabeAusbezahltAm;
	}

	public void setStufe1FreigabeAusbezahltAm(@Nullable LocalDateTime stufe1FreigabeAusbezahltAm) {
		this.stufe1FreigabeAusbezahltAm = stufe1FreigabeAusbezahltAm;
	}

	@Nullable
	public BigDecimal getStufe2InstitutionKostenuebernahmeAnzahlTage() {
		return stufe2InstitutionKostenuebernahmeAnzahlTage;
	}

	public void setStufe2InstitutionKostenuebernahmeAnzahlTage(
		@Nullable BigDecimal stufe2InstitutionKostenuebernahmeAnzahlTage) {
		this.stufe2InstitutionKostenuebernahmeAnzahlTage = stufe2InstitutionKostenuebernahmeAnzahlTage;
	}

	@Nullable
	public BigDecimal getStufe2InstitutionKostenuebernahmeAnzahlStunden() {
		return stufe2InstitutionKostenuebernahmeAnzahlStunden;
	}

	public void setStufe2InstitutionKostenuebernahmeAnzahlStunden(
		@Nullable BigDecimal stufe2InstitutionKostenuebernahmeAnzahlStunden) {
		this.stufe2InstitutionKostenuebernahmeAnzahlStunden = stufe2InstitutionKostenuebernahmeAnzahlStunden;
	}

	@Nullable
	public BigDecimal getStufe2InstitutionKostenuebernahmeBetreuung() {
		return stufe2InstitutionKostenuebernahmeBetreuung;
	}

	public void setStufe2InstitutionKostenuebernahmeBetreuung(
		@Nullable BigDecimal stufe2InstitutionKostenuebernahmeBetreuung) {
		this.stufe2InstitutionKostenuebernahmeBetreuung = stufe2InstitutionKostenuebernahmeBetreuung;
	}

	@Nullable
	public BigDecimal getStufe2KantonKostenuebernahmeAnzahlTage() {
		return stufe2KantonKostenuebernahmeAnzahlTage;
	}

	public void setStufe2KantonKostenuebernahmeAnzahlTage(@Nullable BigDecimal stufe2KantonKostenuebernahmeAnzahlTage) {
		this.stufe2KantonKostenuebernahmeAnzahlTage = stufe2KantonKostenuebernahmeAnzahlTage;
	}

	@Nullable
	public BigDecimal getStufe2KantonKostenuebernahmeAnzahlStunden() {
		return stufe2KantonKostenuebernahmeAnzahlStunden;
	}

	public void setStufe2KantonKostenuebernahmeAnzahlStunden(
		@Nullable BigDecimal stufe2KantonKostenuebernahmeAnzahlStunden) {
		this.stufe2KantonKostenuebernahmeAnzahlStunden = stufe2KantonKostenuebernahmeAnzahlStunden;
	}

	@Nullable
	public BigDecimal getStufe2KantonKostenuebernahmeBetreuung() {
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

	public void setBetragEntgangeneElternbeitraegeNichtAngeboteneEinheiten(
		@Nullable BigDecimal betragEntgangeneElternbeitraegeNichtAngeboteneEinheiten) {
		this.betragEntgangeneElternbeitraegeNichtAngeboteneEinheiten =
			betragEntgangeneElternbeitraegeNichtAngeboteneEinheiten;
	}

	@Nullable
	public BigDecimal getRueckerstattungNichtAngeboteneBetreuungstage() {
		return rueckerstattungNichtAngeboteneBetreuungstage;
	}

	public void setRueckerstattungNichtAngeboteneBetreuungstage(
		@Nullable BigDecimal rueckerstattungNichtAngeboteneBetreuungstage) {
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

	@Nullable
	public BigDecimal getStufe2VerfuegungBetrag() {
		return stufe2VerfuegungBetrag;
	}

	public void setStufe2VerfuegungBetrag(@Nullable BigDecimal stufe2VerfuegungBetrag) {
		this.stufe2VerfuegungBetrag = stufe2VerfuegungBetrag;
	}

	@Nullable
	public LocalDateTime getStufe2VerfuegungDatum() {
		return stufe2VerfuegungDatum;
	}

	public void setStufe2VerfuegungDatum(@Nullable LocalDateTime stufe2VerfuegungDatum) {
		this.stufe2VerfuegungDatum = stufe2VerfuegungDatum;
	}

	@Nullable
	public LocalDateTime getStufe2VerfuegungAusbezahltAm() {
		return stufe2VerfuegungAusbezahltAm;
	}

	public void setStufe2VerfuegungAusbezahltAm(@Nullable LocalDateTime stufe2VerfuegungAusbezahltAm) {
		this.stufe2VerfuegungAusbezahltAm = stufe2VerfuegungAusbezahltAm;
	}

	@Nullable
	public String getIban() {
		return iban;
	}

	public void setIban(@Nullable String iban) {
		this.iban = iban;
	}

	@Nullable
	public String getKontoinhaber() {
		return kontoinhaber;
	}

	public void setKontoinhaber(@Nullable String kontoinhaber) {
		this.kontoinhaber = kontoinhaber;
	}

	@Nullable
	public String getAuszahlungOrganisation() {
		return auszahlungOrganisation;
	}

	public void setAuszahlungOrganisation(@Nullable String auszahlungOrganisation) {
		this.auszahlungOrganisation = auszahlungOrganisation;
	}

	@Nullable
	public String getAuszahlungStrasse() {
		return auszahlungStrasse;
	}

	public void setAuszahlungStrasse(@Nullable String auszahlungStrasse) {
		this.auszahlungStrasse = auszahlungStrasse;
	}

	@Nullable
	public String getAuszahlungHausnummer() {
		return auszahlungHausnummer;
	}

	public void setAuszahlungHausnummer(@Nullable String auszahlungHausnummer) {
		this.auszahlungHausnummer = auszahlungHausnummer;
	}

	@Nullable
	public String getAuszahlungPlz() {
		return auszahlungPlz;
	}

	public void setAuszahlungPlz(@Nullable String auszahlungPlz) {
		this.auszahlungPlz = auszahlungPlz;
	}

	@Nullable
	public String getAuszahlungOrt() {
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
