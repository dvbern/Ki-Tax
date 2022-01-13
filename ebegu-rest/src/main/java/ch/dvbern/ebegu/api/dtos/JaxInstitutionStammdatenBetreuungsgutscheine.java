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

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * DTO fuer InstitutionStammdatenBetreuungsgutscheine
 */
@XmlRootElement(name = "institutionStammdaten")
@XmlAccessorType(XmlAccessType.FIELD)
public class JaxInstitutionStammdatenBetreuungsgutscheine extends JaxAbstractDTO {

	private static final long serialVersionUID = 1881996153584255198L;

	@Nullable
	private String iban;
	@Nullable
	private String kontoinhaber;
	@Nullable
	private JaxAdresse adresseKontoinhaber;

	private boolean alterskategorieBaby;

	private boolean alterskategorieVorschule;

	private boolean alterskategorieKindergarten;

	private boolean alterskategorieSchule;

	private boolean subventioniertePlaetze;

	@Nullable
	private BigDecimal tarifProHauptmahlzeit;

	@Nullable
	private BigDecimal tarifProNebenmahlzeit;

	@Nullable
	private BigDecimal anzahlPlaetze = BigDecimal.ZERO;
	@Nullable
	private BigDecimal anzahlPlaetzeFirmen;

	@Nullable
	private String oeffnungsAbweichungen;

	@Nonnull
	private Set<DayOfWeek> oeffnungstage = EnumSet.noneOf(DayOfWeek.class);

	@Nullable
	private String offenVon;

	@Nullable
	private String offenBis;

	@Nullable
	private String alternativeEmailFamilienportal;

	@Nullable
	private Integer oeffnungstageProJahr;

	@Nullable
	private BigDecimal auslastungInstitutionen;

	@Nullable
	private BigDecimal anzahlKinderWarteliste;

	@Nullable
	private BigDecimal summePensumWarteliste;

	@Nullable
	private BigDecimal dauerWarteliste;

	private boolean fruehEroeffnung;

	private boolean spaetEroeffnung;

	private boolean wochenendeEroeffnung;

	private boolean uebernachtungMoeglich;

	@Nullable
	private String infomaKreditorennummer;

	@Nullable
	private String infomaBankcode;


	@Nonnull
	private Set<JaxBetreuungsstandort> betreuungsstandorte = new HashSet<>();

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
	public JaxAdresse getAdresseKontoinhaber() {
		return adresseKontoinhaber;
	}

	public void setAdresseKontoinhaber(@Nullable JaxAdresse adresseKontoinhaber) {
		this.adresseKontoinhaber = adresseKontoinhaber;
	}

	public boolean isAlterskategorieBaby() {
		return alterskategorieBaby;
	}

	public void setAlterskategorieBaby(boolean alterskategorieBaby) {
		this.alterskategorieBaby = alterskategorieBaby;
	}

	public boolean isAlterskategorieVorschule() {
		return alterskategorieVorschule;
	}

	public void setAlterskategorieVorschule(boolean alterskategorieVorschule) {
		this.alterskategorieVorschule = alterskategorieVorschule;
	}

	public boolean isAlterskategorieKindergarten() {
		return alterskategorieKindergarten;
	}

	public void setAlterskategorieKindergarten(boolean alterskategorieKindergarten) {
		this.alterskategorieKindergarten = alterskategorieKindergarten;
	}

	public boolean isAlterskategorieSchule() {
		return alterskategorieSchule;
	}

	public void setAlterskategorieSchule(boolean alterskategorieSchule) {
		this.alterskategorieSchule = alterskategorieSchule;
	}

	public boolean isSubventioniertePlaetze() {
		return subventioniertePlaetze;
	}

	public void setSubventioniertePlaetze(boolean subventioniertePlaetze) {
		this.subventioniertePlaetze = subventioniertePlaetze;
	}

	@Nullable
	public BigDecimal getAnzahlPlaetze() {
		return anzahlPlaetze;
	}

	public void setAnzahlPlaetze(@Nullable BigDecimal anzahlPlaetze) {
		this.anzahlPlaetze = anzahlPlaetze;
	}

	@Nullable
	public BigDecimal getAnzahlPlaetzeFirmen() {
		return anzahlPlaetzeFirmen;
	}

	public void setAnzahlPlaetzeFirmen(@Nullable BigDecimal anzahlPlaetzeFirmen) {
		this.anzahlPlaetzeFirmen = anzahlPlaetzeFirmen;
	}

	@Nullable
	public BigDecimal getTarifProHauptmahlzeit() {
		return tarifProHauptmahlzeit;
	}

	public void setTarifProHauptmahlzeit(@Nullable BigDecimal tarifProHauptmahlzeit) {
		this.tarifProHauptmahlzeit = tarifProHauptmahlzeit;
	}

	@Nullable
	public BigDecimal getTarifProNebenmahlzeit() {
		return tarifProNebenmahlzeit;
	}

	public void setTarifProNebenmahlzeit(@Nullable BigDecimal tarifProNebenmahlzeit) {
		this.tarifProNebenmahlzeit = tarifProNebenmahlzeit;
	}

	@Nullable
	public String getOeffnungsAbweichungen() {
		return oeffnungsAbweichungen;
	}

	public void setOeffnungsAbweichungen(@Nullable String oeffnungsAbweichungen) {
		this.oeffnungsAbweichungen = oeffnungsAbweichungen;
	}

	@Nonnull
	public Set<DayOfWeek> getOeffnungstage() {
		return oeffnungstage;
	}

	public void setOeffnungstage(@Nonnull Set<DayOfWeek> oeffnungstage) {
		this.oeffnungstage = oeffnungstage;
	}

	@Nullable
	public String getOffenVon() {
		return offenVon;
	}

	public void setOffenVon(@Nullable String offenVon) {
		this.offenVon = offenVon;
	}

	@Nullable
	public String getOffenBis() {
		return offenBis;
	}

	public void setOffenBis(@Nullable String offenBis) {
		this.offenBis = offenBis;
	}

	@Nullable
	public String getAlternativeEmailFamilienportal() {
		return alternativeEmailFamilienportal;
	}

	public void setAlternativeEmailFamilienportal(@Nullable String alternativeEmailFamilienportal) {
		this.alternativeEmailFamilienportal = alternativeEmailFamilienportal;
	}

	@Nonnull
	public Set<JaxBetreuungsstandort> getBetreuungsstandorte() {
		return betreuungsstandorte;
	}

	public void setBetreuungsstandorte(@Nonnull Set<JaxBetreuungsstandort> betreuungsstandorte) {
		this.betreuungsstandorte = betreuungsstandorte;
	}

	@Nullable
	public Integer getOeffnungstageProJahr() {
		return oeffnungstageProJahr;
	}

	public void setOeffnungstageProJahr(@Nullable Integer oeffnungstageProJahr) {
		this.oeffnungstageProJahr = oeffnungstageProJahr;
	}

	@Nullable
	public BigDecimal getAuslastungInstitutionen() {
		return auslastungInstitutionen;
	}

	public void setAuslastungInstitutionen(@Nullable BigDecimal auslastungInstitutionen) {
		this.auslastungInstitutionen = auslastungInstitutionen;
	}

	@Nullable
	public BigDecimal getAnzahlKinderWarteliste() {
		return anzahlKinderWarteliste;
	}

	public void setAnzahlKinderWarteliste(@Nullable BigDecimal anzahlKinderWarteliste) {
		this.anzahlKinderWarteliste = anzahlKinderWarteliste;
	}

	@Nullable
	public BigDecimal getSummePensumWarteliste() {
		return summePensumWarteliste;
	}

	public void setSummePensumWarteliste(@Nullable BigDecimal summePensumWarteliste) {
		this.summePensumWarteliste = summePensumWarteliste;
	}

	@Nullable
	public BigDecimal getDauerWarteliste() {
		return dauerWarteliste;
	}

	public void setDauerWarteliste(@Nullable BigDecimal dauerWarteliste) {
		this.dauerWarteliste = dauerWarteliste;
	}

	public boolean isFruehEroeffnung() {
		return fruehEroeffnung;
	}

	public void setFruehEroeffnung(boolean fruehEroeffnung) {
		this.fruehEroeffnung = fruehEroeffnung;
	}

	public boolean isSpaetEroeffnung() {
		return spaetEroeffnung;
	}

	public void setSpaetEroeffnung(boolean spaetEroeffnung) {
		this.spaetEroeffnung = spaetEroeffnung;
	}

	public boolean isWochenendeEroeffnung() {
		return wochenendeEroeffnung;
	}

	public void setWochenendeEroeffnung(boolean wochenendeEroeffnung) {
		this.wochenendeEroeffnung = wochenendeEroeffnung;
	}

	public boolean isUebernachtungMoeglich() {
		return uebernachtungMoeglich;
	}

	public void setUebernachtungMoeglich(boolean uebernachtungMoeglich) {
		this.uebernachtungMoeglich = uebernachtungMoeglich;
	}

	@Nullable
	public String getInfomaKreditorennummer() {
		return infomaKreditorennummer;
	}

	public void setInfomaKreditorennummer(@Nullable String infomaKreditorennummer) {
		this.infomaKreditorennummer = infomaKreditorennummer;
	}

	@Nullable
	public String getInfomaBankcode() {
		return infomaBankcode;
	}

	public void setInfomaBankcode(@Nullable String infomaBankcode) {
		this.infomaBankcode = infomaBankcode;
	}
}
