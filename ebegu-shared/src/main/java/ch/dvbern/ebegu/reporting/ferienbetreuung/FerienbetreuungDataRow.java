/*
 * Copyright (C) 2021 DV Bern AG, Switzerland
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

package ch.dvbern.ebegu.reporting.ferienbetreuung;

import java.math.BigDecimal;
import java.time.LocalDate;

import ch.dvbern.ebegu.enums.KinderAusAnderenGemeindenZahlenAnderenTarifAnswer;
import ch.dvbern.ebegu.enums.gemeindeantrag.FerienbetreuungAngabenStatus;
import org.jetbrains.annotations.Nullable;

public class FerienbetreuungDataRow {

	private String periode = null;
	private FerienbetreuungAngabenStatus status = null;

	private @Nullable String traegerschaft = null;
	private String weitereGemeinden = null;
	private @Nullable LocalDate seitWannFerienbetreuungen = null;

	private String gemeinde = null;
	private Long bfsNummerGemeinde = null;
	private @Nullable String gemeindeAnschrift = null;
	private String gemeindeStrasse = null;
	private @Nullable String geimeindeHausnummer = null;
	private @Nullable String gemeindeZusatz = null;
	private String gemeindePlz = null;
	private String gemeindeOrt = null;

	private @Nullable String stammdatenKontaktpersonVorname = null;
	private @Nullable String stammdatenKontaktpersonName = null;
	private @Nullable String stammdatenKontaktpersonFunktion = null;
	private @Nullable String stammdatenKontaktpersonTelefon = null;
	private @Nullable String stammdatenKontaktpersonEmail = null;
	private String kontoInhaber = null;
	private String kontoStrasse = null;
	private @Nullable String kontoHausnummer = null;
	private @Nullable String kontoZusatz = null;
	private String kontoPlz = null;
	private String kontoOrt = null;
	private String iban = null;
	private @Nullable String kontoVermerk = null;

	private @Nullable String angebot = null;
	private @Nullable String angebotKontaktpersonVorname = null;
	private @Nullable String angebotKontaktpersonNachname = null;
	private String angebotKontaktpersonStrasse = null;
	private @Nullable String angebotKontaktpersonHausnummer = null;
	private @Nullable String angebotKontaktpersonZusatz = null;
	private String angebotKontaktpersonPlz = null;
	private String angebotKontaktpersonOrt = null;

	private @Nullable BigDecimal anzahlFerienwochenHerbstferien = null;
	private @Nullable BigDecimal anzahlFerienwochenWinterferien = null;
	private @Nullable BigDecimal anzahlFerienwochenSportferien = null;
	private @Nullable BigDecimal anzahlFerienwochenFruehlingsferien = null;
	private @Nullable BigDecimal anzahlFerienwochenSommerferien = null;
	private @Nullable BigDecimal anzahlTageGesamt = null;
	private @Nullable String bemerkungAnzahlFerienwochen = null;
	private @Nullable BigDecimal anzahlStundenProBetreuungstag = null;
	private @Nullable Boolean betreuungErfolgtTagsueber = null;
	private @Nullable String bemerkungOeffnungszeiten = null;

	private String finanziellBeteiligteGemeinden = null;
	private @Nullable Boolean gemeindeFuehrtAngebotSelber = null;
	private @Nullable Boolean gemeindeFuehrtAngebotInKooperation = null;
	private @Nullable Boolean gemeindeBeauftragtExterneAnbieter = null;
	private @Nullable Boolean angebotVereineUndPrivateIntegriert = null;
	private @Nullable String bemerkungenKooperation = null;
	private @Nullable Boolean leitungDurchPersonMitAusbildung = null;
	private @Nullable Boolean betreuungDurchPersonenMitErfahrung = null;
	private @Nullable Boolean anzahlKinderAngemessen = null;
	private @Nullable String betreuungsschluessel = null;
	private @Nullable String bemerkungenPersonal = null;
	private @Nullable Boolean fixerTarifKinderDerGemeinde = null;
	private @Nullable Boolean einkommensabhaengigerTarifKinderDerGemeinde = null;
	private @Nullable Boolean tagesschuleTarifGiltFuerFerienbetreuung = null;
	private @Nullable Boolean ferienbetreuungTarifWirdAusTagesschuleTarifAbgeleitet = null;
	private @Nullable KinderAusAnderenGemeindenZahlenAnderenTarifAnswer kinderAusAnderenGemeindenZahlenAnderenTarif = null;
	private @Nullable String bemerkungenTarifsystem = null;

	private @Nullable BigDecimal anzahlBetreuungstageKinderBern = null;
	private @Nullable BigDecimal betreuungstageKinderDieserGemeinde = null;
	private @Nullable BigDecimal betreuungstageKinderDieserGemeindeSonderschueler = null;
	private @Nullable BigDecimal davonBetreuungstageKinderAndererGemeinden = null;
	private @Nullable BigDecimal davonBetreuungstageKinderAndererGemeindenSonderschueler = null;
	private @Nullable BigDecimal anzahlBetreuteKinder = null;
	private @Nullable BigDecimal anzahlBetreuteKinderSonderschueler = null;
	private @Nullable BigDecimal anzahlBetreuteKinder1Zyklus = null;
	private @Nullable BigDecimal anzahlBetreuteKinder2Zyklus = null;
	private @Nullable BigDecimal anzahlBetreuteKinder3Zyklus = null;

	private @Nullable BigDecimal personalkosten = null;
	private @Nullable BigDecimal personalkostenLeitungAdmin = null;
	private @Nullable BigDecimal sachkosten = null;
	private @Nullable BigDecimal verpflegungskosten = null;
	private @Nullable BigDecimal weitereKosten = null;
	private @Nullable String bemerkungenKosten = null;
	private @Nullable BigDecimal elterngebuehren = null;
	private @Nullable BigDecimal weitereEinnahmen = null;

	private BigDecimal totalKantonsbeitrag = null;
	private BigDecimal beitragKinderAnbietendenGemeinde = null;
	private BigDecimal beteiligungAnbietendenGemeinde =  null;

	private @Nullable String kommentar = null;

	public String getGemeinde() {
		return gemeinde;
	}

	public void setGemeinde(String gemeinde) {
		this.gemeinde = gemeinde;
	}

	public Long getBfsNummerGemeinde() {
		return bfsNummerGemeinde;
	}

	public void setBfsNummerGemeinde(Long bfsNummerGemeinde) {
		this.bfsNummerGemeinde = bfsNummerGemeinde;
	}

	public String getPeriode() {
		return periode;
	}

	public void setPeriode(String periode) {
		this.periode = periode;
	}

	public FerienbetreuungAngabenStatus getStatus() {
		return status;
	}

	public void setStatus(FerienbetreuungAngabenStatus status) {
		this.status = status;
	}

	public @Nullable String getTraegerschaft() {
		return traegerschaft;
	}

	public void setTraegerschaft(@Nullable String traegerschaft) {
		this.traegerschaft = traegerschaft;
	}

	public String getWeitereGemeinden() {
		return weitereGemeinden;
	}

	public void setWeitereGemeinden(String weitereGemeinden) {
		this.weitereGemeinden = weitereGemeinden;
	}

	public @Nullable LocalDate getSeitWannFerienbetreuungen() {
		return seitWannFerienbetreuungen;
	}

	public void setSeitWannFerienbetreuungen(@Nullable LocalDate seitWannFerienbetreuungen) {
		this.seitWannFerienbetreuungen = seitWannFerienbetreuungen;
	}

	public String getGemeindeStrasse() {
		return gemeindeStrasse;
	}

	public void setGemeindeStrasse(String gemeindeStrasse) {
		this.gemeindeStrasse = gemeindeStrasse;
	}

	public @Nullable String getGeimeindeHausnummer() {
		return geimeindeHausnummer;
	}

	public void setGeimeindeHausnummer(@Nullable String geimeindeHausnummer) {
		this.geimeindeHausnummer = geimeindeHausnummer;
	}

	public @Nullable String getGemeindeZusatz() {
		return gemeindeZusatz;
	}

	public void setGemeindeZusatz(@Nullable String gemeindeZusatz) {
		this.gemeindeZusatz = gemeindeZusatz;
	}

	public String getGemeindePlz() {
		return gemeindePlz;
	}

	public void setGemeindePlz(String gemeindePLZ) {
		this.gemeindePlz = gemeindePLZ;
	}

	public String getGemeindeOrt() {
		return gemeindeOrt;
	}

	public void setGemeindeOrt(String gemeindeOrt) {
		this.gemeindeOrt = gemeindeOrt;
	}

	public @Nullable String getStammdatenKontaktpersonVorname() {
		return stammdatenKontaktpersonVorname;
	}

	public void setStammdatenKontaktpersonVorname(@Nullable String stammdatenKontaktPersonPersonVorname) {
		this.stammdatenKontaktpersonVorname = stammdatenKontaktPersonPersonVorname;
	}

	public @Nullable String getStammdatenKontaktpersonName() {
		return stammdatenKontaktpersonName;
	}

	public void setStammdatenKontaktpersonName(@Nullable String stammdatenKontaktpersonName) {
		this.stammdatenKontaktpersonName = stammdatenKontaktpersonName;
	}

	public @Nullable String getStammdatenKontaktpersonFunktion() {
		return stammdatenKontaktpersonFunktion;
	}

	public void setStammdatenKontaktpersonFunktion(@Nullable String stammdatenKontaktpersonFunktion) {
		this.stammdatenKontaktpersonFunktion = stammdatenKontaktpersonFunktion;
	}

	public @Nullable String getStammdatenKontaktpersonTelefon() {
		return stammdatenKontaktpersonTelefon;
	}

	public void setStammdatenKontaktpersonTelefon(@Nullable String stammdatenKontaktpersonTelefon) {
		this.stammdatenKontaktpersonTelefon = stammdatenKontaktpersonTelefon;
	}

	public @Nullable String getStammdatenKontaktpersonEmail() {
		return stammdatenKontaktpersonEmail;
	}

	public void setStammdatenKontaktpersonEmail(@Nullable String stammdatenKontaktPersonMail) {
		this.stammdatenKontaktpersonEmail = stammdatenKontaktPersonMail;
	}

	public String getKontoInhaber() {
		return kontoInhaber;
	}

	public void setKontoInhaber(String kontoInhaber) {
		this.kontoInhaber = kontoInhaber;
	}

	public String getKontoStrasse() {
		return kontoStrasse;
	}

	public void setKontoStrasse(String kontoStrasse) {
		this.kontoStrasse = kontoStrasse;
	}

	public @Nullable String getKontoHausnummer() {
		return kontoHausnummer;
	}

	public void setKontoHausnummer(@Nullable String kontoNummer) {
		this.kontoHausnummer = kontoNummer;
	}

	public @Nullable String getKontoZusatz() {
		return kontoZusatz;
	}

	public void setKontoZusatz(@Nullable String kontoZusatz) {
		this.kontoZusatz = kontoZusatz;
	}

	public String getKontoPlz() {
		return kontoPlz;
	}

	public void setKontoPlz(String kontoPLZ) {
		this.kontoPlz = kontoPLZ;
	}

	public String getKontoOrt() {
		return kontoOrt;
	}

	public void setKontoOrt(String kontoOrt) {
		this.kontoOrt = kontoOrt;
	}

	public String getIban() {
		return iban;
	}

	public void setIban(String iban) {
		this.iban = iban;
	}

	public @Nullable String getKontoVermerk() {
		return kontoVermerk;
	}

	public void setKontoVermerk(@Nullable String kontoVermerk) {
		this.kontoVermerk = kontoVermerk;
	}

	public @Nullable String getAngebot() {
		return angebot;
	}

	public void setAngebot(@Nullable String angebot) {
		this.angebot = angebot;
	}

	public @Nullable String getAngebotKontaktpersonVorname() {
		return angebotKontaktpersonVorname;
	}

	public void setAngebotKontaktpersonVorname(@Nullable String angebotKontaktPersonVorname) {
		this.angebotKontaktpersonVorname = angebotKontaktPersonVorname;
	}

	public @Nullable String getAngebotKontaktpersonNachname() {
		return angebotKontaktpersonNachname;
	}

	public void setAngebotKontaktpersonNachname(@Nullable String angebotKontaktPersonName) {
		this.angebotKontaktpersonNachname = angebotKontaktPersonName;
	}

	public String getAngebotKontaktpersonStrasse() {
		return angebotKontaktpersonStrasse;
	}

	public void setAngebotKontaktpersonStrasse(String angebotKontaktPersonStrasse) {
		this.angebotKontaktpersonStrasse = angebotKontaktPersonStrasse;
	}

	public @Nullable String getAngebotKontaktpersonHausnummer() {
		return angebotKontaktpersonHausnummer;
	}

	public void setAngebotKontaktpersonHausnummer(@Nullable String angebotKontaktPersonNummer) {
		this.angebotKontaktpersonHausnummer = angebotKontaktPersonNummer;
	}

	public @Nullable String getAngebotKontaktpersonZusatz() {
		return angebotKontaktpersonZusatz;
	}

	public void setAngebotKontaktpersonZusatz(@Nullable String angebotKontaktPersonZusatz) {
		this.angebotKontaktpersonZusatz = angebotKontaktPersonZusatz;
	}

	public String getAngebotKontaktpersonPlz() {
		return angebotKontaktpersonPlz;
	}

	public void setAngebotKontaktpersonPlz(String angebotKontaktPersonPLZ) {
		this.angebotKontaktpersonPlz = angebotKontaktPersonPLZ;
	}

	public String getAngebotKontaktpersonOrt() {
		return angebotKontaktpersonOrt;
	}

	public void setAngebotKontaktpersonOrt(String angebotKontaktPersonOrt) {
		this.angebotKontaktpersonOrt = angebotKontaktPersonOrt;
	}

	public @Nullable BigDecimal getAnzahlFerienwochenHerbstferien() {
		return anzahlFerienwochenHerbstferien;
	}

	public void setAnzahlFerienwochenHerbstferien(@Nullable BigDecimal anzahlFerienwochenHerbstferien) {
		this.anzahlFerienwochenHerbstferien = anzahlFerienwochenHerbstferien;
	}

	public @Nullable BigDecimal getAnzahlFerienwochenWinterferien() {
		return anzahlFerienwochenWinterferien;
	}

	public void setAnzahlFerienwochenWinterferien(@Nullable BigDecimal anzahlFerienwochenWinterferien) {
		this.anzahlFerienwochenWinterferien = anzahlFerienwochenWinterferien;
	}

	public @Nullable BigDecimal getAnzahlFerienwochenSportferien() {
		return anzahlFerienwochenSportferien;
	}

	public void setAnzahlFerienwochenSportferien(@Nullable BigDecimal anzahlFerienwochenSportferien) {
		this.anzahlFerienwochenSportferien = anzahlFerienwochenSportferien;
	}

	public @Nullable BigDecimal getAnzahlFerienwochenFruehlingsferien() {
		return anzahlFerienwochenFruehlingsferien;
	}

	public void setAnzahlFerienwochenFruehlingsferien(@Nullable BigDecimal anzahlFerienwochenFruehlingsferien) {
		this.anzahlFerienwochenFruehlingsferien = anzahlFerienwochenFruehlingsferien;
	}

	public @Nullable BigDecimal getAnzahlFerienwochenSommerferien() {
		return anzahlFerienwochenSommerferien;
	}

	public void setAnzahlFerienwochenSommerferien(@Nullable BigDecimal anzahlFerienwochenSommerferien) {
		this.anzahlFerienwochenSommerferien = anzahlFerienwochenSommerferien;
	}

	public @Nullable BigDecimal getAnzahlTageGesamt() {
		return anzahlTageGesamt;
	}

	public void setAnzahlTageGesamt(@Nullable BigDecimal anzahlTageGesamt) {
		this.anzahlTageGesamt = anzahlTageGesamt;
	}

	public @Nullable String getBemerkungAnzahlFerienwochen() {
		return bemerkungAnzahlFerienwochen;
	}

	public void setBemerkungAnzahlFerienwochen(@Nullable String bemerkungAnzahlFerienwochen) {
		this.bemerkungAnzahlFerienwochen = bemerkungAnzahlFerienwochen;
	}

	public @Nullable BigDecimal getAnzahlStundenProBetreuungstag() {
		return anzahlStundenProBetreuungstag;
	}

	public void setAnzahlStundenProBetreuungstag(@Nullable BigDecimal anzahlStundenProBetreuungstag) {
		this.anzahlStundenProBetreuungstag = anzahlStundenProBetreuungstag;
	}

	public @Nullable Boolean getBetreuungErfolgtTagsueber() {
		return betreuungErfolgtTagsueber;
	}

	public void setBetreuungErfolgtTagsueber(@Nullable Boolean betreuungErfolgtTagsueber) {
		this.betreuungErfolgtTagsueber = betreuungErfolgtTagsueber;
	}

	public @Nullable String getBemerkungOeffnungszeiten() {
		return bemerkungOeffnungszeiten;
	}

	public void setBemerkungOeffnungszeiten(@Nullable String bemerkungOeffnungszeiten) {
		this.bemerkungOeffnungszeiten = bemerkungOeffnungszeiten;
	}

	public String getFinanziellBeteiligteGemeinden() {
		return finanziellBeteiligteGemeinden;
	}

	public void setFinanziellBeteiligteGemeinden(String finanziellBeteiligteGemeinden) {
		this.finanziellBeteiligteGemeinden = finanziellBeteiligteGemeinden;
	}

	public @Nullable Boolean getGemeindeFuehrtAngebotSelber() {
		return gemeindeFuehrtAngebotSelber;
	}

	public void setGemeindeFuehrtAngebotSelber(@Nullable Boolean gemeindeFuehrtAngebotSelber) {
		this.gemeindeFuehrtAngebotSelber = gemeindeFuehrtAngebotSelber;
	}

	public @Nullable Boolean getGemeindeFuehrtAngebotInKooperation() {
		return gemeindeFuehrtAngebotInKooperation;
	}

	public void setGemeindeFuehrtAngebotInKooperation(@Nullable Boolean gemeindeFuehrtAngebotInKooperation) {
		this.gemeindeFuehrtAngebotInKooperation = gemeindeFuehrtAngebotInKooperation;
	}

	public @Nullable Boolean getGemeindeBeauftragtExterneAnbieter() {
		return gemeindeBeauftragtExterneAnbieter;
	}

	public void setGemeindeBeauftragtExterneAnbieter(@Nullable Boolean gemeindeBeauftragtExterneAnbieter) {
		this.gemeindeBeauftragtExterneAnbieter = gemeindeBeauftragtExterneAnbieter;
	}

	public @Nullable Boolean getAngebotVereineUndPrivateIntegriert() {
		return angebotVereineUndPrivateIntegriert;
	}

	public void setAngebotVereineUndPrivateIntegriert(@Nullable Boolean angebotVereineUndPrivateIntegriert) {
		this.angebotVereineUndPrivateIntegriert = angebotVereineUndPrivateIntegriert;
	}

	public @Nullable String getBemerkungenKooperation() {
		return bemerkungenKooperation;
	}

	public void setBemerkungenKooperation(@Nullable String bemerkungenKooperation) {
		this.bemerkungenKooperation = bemerkungenKooperation;
	}

	public @Nullable Boolean getLeitungDurchPersonMitAusbildung() {
		return leitungDurchPersonMitAusbildung;
	}

	public void setLeitungDurchPersonMitAusbildung(@Nullable Boolean leitungDurchPersonMitAusbildung) {
		this.leitungDurchPersonMitAusbildung = leitungDurchPersonMitAusbildung;
	}

	public @Nullable Boolean getBetreuungDurchPersonenMitErfahrung() {
		return betreuungDurchPersonenMitErfahrung;
	}

	public void setBetreuungDurchPersonenMitErfahrung(@Nullable Boolean betreuungDurchPersonenMitErfahrung) {
		this.betreuungDurchPersonenMitErfahrung = betreuungDurchPersonenMitErfahrung;
	}

	public @Nullable Boolean getAnzahlKinderAngemessen() {
		return anzahlKinderAngemessen;
	}

	public void setAnzahlKinderAngemessen(@Nullable Boolean anzahlKinderAngemessen) {
		this.anzahlKinderAngemessen = anzahlKinderAngemessen;
	}

	public @Nullable String getBetreuungsschluessel() {
		return betreuungsschluessel;
	}

	public void setBetreuungsschluessel(@Nullable String betreuungsschluessel) {
		this.betreuungsschluessel = betreuungsschluessel;
	}

	public @Nullable String getBemerkungenPersonal() {
		return bemerkungenPersonal;
	}

	public void setBemerkungenPersonal(@Nullable String bemerkungenPersonal) {
		this.bemerkungenPersonal = bemerkungenPersonal;
	}

	public @Nullable Boolean getFixerTarifKinderDerGemeinde() {
		return fixerTarifKinderDerGemeinde;
	}

	public void setFixerTarifKinderDerGemeinde(@Nullable Boolean fixerTarifKinderDerGemeinde) {
		this.fixerTarifKinderDerGemeinde = fixerTarifKinderDerGemeinde;
	}

	public @Nullable Boolean getEinkommensabhaengigerTarifKinderDerGemeinde() {
		return einkommensabhaengigerTarifKinderDerGemeinde;
	}

	public void setEinkommensabhaengigerTarifKinderDerGemeinde(@Nullable Boolean einkommensabhaengigerTarifKinderDerGemeinde) {
		this.einkommensabhaengigerTarifKinderDerGemeinde = einkommensabhaengigerTarifKinderDerGemeinde;
	}

	public @Nullable Boolean getTagesschuleTarifGiltFuerFerienbetreuung() {
		return tagesschuleTarifGiltFuerFerienbetreuung;
	}

	public void setTagesschuleTarifGiltFuerFerienbetreuung(@Nullable Boolean tagesschuleTarifGiltFuerFerienbetreuung) {
		this.tagesschuleTarifGiltFuerFerienbetreuung = tagesschuleTarifGiltFuerFerienbetreuung;
	}

	public @Nullable Boolean getFerienbetreuungTarifWirdAusTagesschuleTarifAbgeleitet() {
		return ferienbetreuungTarifWirdAusTagesschuleTarifAbgeleitet;
	}

	public void setFerienbetreuungTarifWirdAusTagesschuleTarifAbgeleitet(@Nullable Boolean ferienbetreuungTarifWirdAusTagesschuleTarifAbgeleitet) {
		this.ferienbetreuungTarifWirdAusTagesschuleTarifAbgeleitet =
			ferienbetreuungTarifWirdAusTagesschuleTarifAbgeleitet;
	}

	public @Nullable KinderAusAnderenGemeindenZahlenAnderenTarifAnswer getKinderAusAnderenGemeindenZahlenAnderenTarif() {
		return kinderAusAnderenGemeindenZahlenAnderenTarif;
	}

	public void setKinderAusAnderenGemeindenZahlenAnderenTarif(@Nullable KinderAusAnderenGemeindenZahlenAnderenTarifAnswer kinderAusAnderenGemeindenZahlenAnderenTarif) {
		this.kinderAusAnderenGemeindenZahlenAnderenTarif = kinderAusAnderenGemeindenZahlenAnderenTarif;
	}

	public @Nullable String getBemerkungenTarifsystem() {
		return bemerkungenTarifsystem;
	}

	public void setBemerkungenTarifsystem(@Nullable String bemerkungenTarifsystem) {
		this.bemerkungenTarifsystem = bemerkungenTarifsystem;
	}

	public @Nullable BigDecimal getAnzahlBetreuungstageKinderBern() {
		return anzahlBetreuungstageKinderBern;
	}

	public void setAnzahlBetreuungstageKinderBern(@Nullable BigDecimal anzahlBetreuungstageKinderBern) {
		this.anzahlBetreuungstageKinderBern = anzahlBetreuungstageKinderBern;
	}

	public @Nullable BigDecimal getBetreuungstageKinderDieserGemeinde() {
		return betreuungstageKinderDieserGemeinde;
	}

	public void setBetreuungstageKinderDieserGemeinde(@Nullable BigDecimal betreuungstageKinderDieserGemeinde) {
		this.betreuungstageKinderDieserGemeinde = betreuungstageKinderDieserGemeinde;
	}

	public @Nullable BigDecimal getBetreuungstageKinderDieserGemeindeSonderschueler() {
		return betreuungstageKinderDieserGemeindeSonderschueler;
	}

	public void setBetreuungstageKinderDieserGemeindeSonderschueler(@Nullable BigDecimal betreuungstageKinderDieserGemeindeSonderschueler) {
		this.betreuungstageKinderDieserGemeindeSonderschueler = betreuungstageKinderDieserGemeindeSonderschueler;
	}

	public @Nullable BigDecimal getDavonBetreuungstageKinderAndererGemeinden() {
		return davonBetreuungstageKinderAndererGemeinden;
	}

	public void setDavonBetreuungstageKinderAndererGemeinden(@Nullable BigDecimal davonBetreuungstageKinderAndererGemeinden) {
		this.davonBetreuungstageKinderAndererGemeinden = davonBetreuungstageKinderAndererGemeinden;
	}

	public @Nullable BigDecimal getDavonBetreuungstageKinderAndererGemeindenSonderschueler() {
		return davonBetreuungstageKinderAndererGemeindenSonderschueler;
	}

	public void setDavonBetreuungstageKinderAndererGemeindenSonderschueler(@Nullable BigDecimal davonBetreuungstageKinderAndererGemeindenSonderschueler) {
		this.davonBetreuungstageKinderAndererGemeindenSonderschueler =
			davonBetreuungstageKinderAndererGemeindenSonderschueler;
	}

	public @Nullable BigDecimal getAnzahlBetreuteKinder() {
		return anzahlBetreuteKinder;
	}

	public void setAnzahlBetreuteKinder(@Nullable BigDecimal anzahlBetreuteKinder) {
		this.anzahlBetreuteKinder = anzahlBetreuteKinder;
	}

	public @Nullable BigDecimal getAnzahlBetreuteKinderSonderschueler() {
		return anzahlBetreuteKinderSonderschueler;
	}

	public void setAnzahlBetreuteKinderSonderschueler(@Nullable BigDecimal anzahlBetreuteKinderSonderschueler) {
		this.anzahlBetreuteKinderSonderschueler = anzahlBetreuteKinderSonderschueler;
	}

	public @Nullable BigDecimal getAnzahlBetreuteKinder1Zyklus() {
		return anzahlBetreuteKinder1Zyklus;
	}

	public void setAnzahlBetreuteKinder1Zyklus(@Nullable BigDecimal anzahlBetreuteKinder1Zyklus) {
		this.anzahlBetreuteKinder1Zyklus = anzahlBetreuteKinder1Zyklus;
	}

	public @Nullable BigDecimal getAnzahlBetreuteKinder2Zyklus() {
		return anzahlBetreuteKinder2Zyklus;
	}

	public void setAnzahlBetreuteKinder2Zyklus(@Nullable BigDecimal anzahlBetreuteKinder2Zyklus) {
		this.anzahlBetreuteKinder2Zyklus = anzahlBetreuteKinder2Zyklus;
	}

	public @Nullable BigDecimal getAnzahlBetreuteKinder3Zyklus() {
		return anzahlBetreuteKinder3Zyklus;
	}

	public void setAnzahlBetreuteKinder3Zyklus(@Nullable BigDecimal anzahlBetreuteKinder3Zyklus) {
		this.anzahlBetreuteKinder3Zyklus = anzahlBetreuteKinder3Zyklus;
	}

	public @Nullable BigDecimal getPersonalkosten() {
		return personalkosten;
	}

	public void setPersonalkosten(@Nullable BigDecimal personalkosten) {
		this.personalkosten = personalkosten;
	}

	public @Nullable BigDecimal getPersonalkostenLeitungAdmin() {
		return personalkostenLeitungAdmin;
	}

	public void setPersonalkostenLeitungAdmin(@Nullable BigDecimal personalkostenLeitungAdmin) {
		this.personalkostenLeitungAdmin = personalkostenLeitungAdmin;
	}

	public @Nullable BigDecimal getSachkosten() {
		return sachkosten;
	}

	public void setSachkosten(@Nullable BigDecimal sachkosten) {
		this.sachkosten = sachkosten;
	}

	public @Nullable BigDecimal getVerpflegungskosten() {
		return verpflegungskosten;
	}

	public void setVerpflegungskosten(@Nullable BigDecimal verpflegungskosten) {
		this.verpflegungskosten = verpflegungskosten;
	}

	public @Nullable BigDecimal getWeitereKosten() {
		return weitereKosten;
	}

	public void setWeitereKosten(@Nullable BigDecimal weitereKosten) {
		this.weitereKosten = weitereKosten;
	}

	public @Nullable String getBemerkungenKosten() {
		return bemerkungenKosten;
	}

	public void setBemerkungenKosten(@Nullable String bemerkungenKosten) {
		this.bemerkungenKosten = bemerkungenKosten;
	}

	public @Nullable BigDecimal getElterngebuehren() {
		return elterngebuehren;
	}

	public void setElterngebuehren(@Nullable BigDecimal elterngebuehren) {
		this.elterngebuehren = elterngebuehren;
	}

	public @Nullable BigDecimal getWeitereEinnahmen() {
		return weitereEinnahmen;
	}

	public void setWeitereEinnahmen(@Nullable BigDecimal weitereEinnahmen) {
		this.weitereEinnahmen = weitereEinnahmen;
	}

	public BigDecimal getTotalKantonsbeitrag() {
		return totalKantonsbeitrag;
	}

	public void setTotalKantonsbeitrag(BigDecimal totalKantonsbeitrag) {
		this.totalKantonsbeitrag = totalKantonsbeitrag;
	}

	public BigDecimal getBeteiligungAnbietendenGemeinde() {
		return beteiligungAnbietendenGemeinde;
	}

	public void setBeteiligungAnbietendenGemeinde(BigDecimal beteiligungAnbietendenGemeinde) {
		this.beteiligungAnbietendenGemeinde = beteiligungAnbietendenGemeinde;
	}

	public @Nullable String getGemeindeAnschrift() {
		return gemeindeAnschrift;
	}

	public void setGemeindeAnschrift(@Nullable String gemeindeAnschrift) {
		this.gemeindeAnschrift = gemeindeAnschrift;
	}

	public @Nullable String getKommentar() {
		return kommentar;
	}

	public void setKommentar(@Nullable String kommentar) {
		this.kommentar = kommentar;
	}

	public BigDecimal getBeitragKinderAnbietendenGemeinde() {
		return beitragKinderAnbietendenGemeinde;
	}

	public void setBeitragKinderAnbietendenGemeinde(BigDecimal beitragKinderAnbietendenGemeinde) {
		this.beitragKinderAnbietendenGemeinde = beitragKinderAnbietendenGemeinde;
	}
}
