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

package ch.dvbern.ebegu.dto.neskovanp;

import java.math.BigDecimal;
import java.time.LocalDate;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import ch.dvbern.lib.date.converters.LocalDateXMLConverter;

/**
 * DTO für die Steuerdaten Response von der nesko np Schnittstelle
 */
@XmlRootElement(name = "steuerdatenResponse")
@XmlAccessorType(XmlAccessType.FIELD)
public class SteuerdatenResponse {

	private static final long serialVersionUID = 7702L;

	private int zpvNrAntragsteller;

	@XmlJavaTypeAdapter(LocalDateXMLConverter.class)
	private LocalDate geburtsdatumAntragsteller;

	private String kiBonAntragID;

	private int beginnGesuchsperiode;

	private int zpvNrDossiertraeger;

	@XmlJavaTypeAdapter(LocalDateXMLConverter.class)
	private LocalDate geburtsdatumDossiertraeger;

	private Integer zpvNrPartner;

	@XmlJavaTypeAdapter(LocalDateXMLConverter.class)
	private LocalDate geburtsdatumPartner;

	private int fallId;

	@XmlJavaTypeAdapter(LocalDateXMLConverter.class)
	private LocalDate antwortdatum;

	private boolean synchroneAntwort;

	private Veranlagungsstand veranlagungsstand;

	private boolean unterjaehrigerFall;

	private BigDecimal erwerbseinkommenUnselbstaendigkeitDossiertraeger;

	private BigDecimal erwerbseinkommenUnselbstaendigkeitPartner;

	private BigDecimal steuerpflichtigesErsatzeinkommenDossiertraeger;

	private BigDecimal steuerpflichtigesErsatzeinkommenPartner;

	private BigDecimal erhalteneUnterhaltsbeitraegeDossiertraeger;

	private BigDecimal erhalteneUnterhaltsbeitraegePartner;

	private BigDecimal ausgewiesenerGeschaeftsertragDossiertraeger;

	private BigDecimal ausgewiesenerGeschaeftsertragPartner;

	private BigDecimal ausgewiesenerGeschaeftsertragVorperiodeDossiertraeger;

	private BigDecimal ausgewiesenerGeschaeftsertragVorperiodePartner;

	private BigDecimal ausgewiesenerGeschaeftsertragVorperiode2Dossiertraeger;

	private BigDecimal ausgewiesenerGeschaeftsertragVorperiode2Partner;

	private BigDecimal weitereSteuerbareEinkuenfteDossiertraeger;

	private BigDecimal weitereSteuerbareEinkuenftePartner;

	private BigDecimal bruttoertraegeAusVermoegenOhneLiegenschaftenUndOhneEGME;

	private BigDecimal bruttoertraegeAusLiegenschaften;

	private BigDecimal nettoertraegeAusEGMEDossiertraeger;

	private BigDecimal nettoertraegeAusEGMEPartner;

	private BigDecimal geleisteteUnterhaltsbeitraege;

	private BigDecimal schuldzinsen;

	private BigDecimal gewinnungskostenBeweglichesVermoegen;

	private BigDecimal liegenschaftsAbzuege;

	private BigDecimal nettovermoegen;

	public int getZpvNrAntragsteller() {
		return zpvNrAntragsteller;
	}

	public void setZpvNrAntragsteller(int zpvNrAntragsteller) {
		this.zpvNrAntragsteller = zpvNrAntragsteller;
	}

	public LocalDate getGeburtsdatumAntragsteller() {
		return geburtsdatumAntragsteller;
	}

	public void setGeburtsdatumAntragsteller(LocalDate geburtsdatumAntragsteller) {
		this.geburtsdatumAntragsteller = geburtsdatumAntragsteller;
	}

	public String getKiBonAntragID() {
		return kiBonAntragID;
	}

	public void setKiBonAntragID(String kiBonAntragID) {
		this.kiBonAntragID = kiBonAntragID;
	}

	public int getBeginnGesuchsperiode() {
		return beginnGesuchsperiode;
	}

	public void setBeginnGesuchsperiode(int beginnGesuchsperiode) {
		this.beginnGesuchsperiode = beginnGesuchsperiode;
	}

	public int getZpvNrDossiertraeger() {
		return zpvNrDossiertraeger;
	}

	public void setZpvNrDossiertraeger(int zpvNrDossiertraeger) {
		this.zpvNrDossiertraeger = zpvNrDossiertraeger;
	}

	public LocalDate getGeburtsdatumDossiertraeger() {
		return geburtsdatumDossiertraeger;
	}

	public void setGeburtsdatumDossiertraeger(LocalDate geburtsdatumDossiertraeger) {
		this.geburtsdatumDossiertraeger = geburtsdatumDossiertraeger;
	}

	public Integer getZpvNrPartner() {
		return zpvNrPartner;
	}

	public void setZpvNrPartner(Integer zpvNrPartner) {
		this.zpvNrPartner = zpvNrPartner;
	}

	public LocalDate getGeburtsdatumPartner() {
		return geburtsdatumPartner;
	}

	public void setGeburtsdatumPartner(LocalDate geburtsdatumPartner) {
		this.geburtsdatumPartner = geburtsdatumPartner;
	}

	public int getFallId() {
		return fallId;
	}

	public void setFallId(int fallId) {
		this.fallId = fallId;
	}

	public LocalDate getAntwortdatum() {
		return antwortdatum;
	}

	public void setAntwortdatum(LocalDate antwortdatum) {
		this.antwortdatum = antwortdatum;
	}

	public boolean isSynchroneAntwort() {
		return synchroneAntwort;
	}

	public void setSynchroneAntwort(boolean synchroneAntwort) {
		this.synchroneAntwort = synchroneAntwort;
	}

	public Veranlagungsstand getVeranlagungsstand() {
		return veranlagungsstand;
	}

	public void setVeranlagungsstand(Veranlagungsstand veranlagungsstand) {
		this.veranlagungsstand = veranlagungsstand;
	}

	public boolean isUnterjaehrigerFall() {
		return unterjaehrigerFall;
	}

	public void setUnterjaehrigerFall(boolean unterjaehrigerFall) {
		this.unterjaehrigerFall = unterjaehrigerFall;
	}

	public BigDecimal getErwerbseinkommenUnselbstaendigkeitDossiertraeger() {
		return erwerbseinkommenUnselbstaendigkeitDossiertraeger;
	}

	public void setErwerbseinkommenUnselbstaendigkeitDossiertraeger(BigDecimal erwerbseinkommenUnselbstaendigkeitDossiertraeger) {
		this.erwerbseinkommenUnselbstaendigkeitDossiertraeger = erwerbseinkommenUnselbstaendigkeitDossiertraeger;
	}

	public BigDecimal getErwerbseinkommenUnselbstaendigkeitPartner() {
		return erwerbseinkommenUnselbstaendigkeitPartner;
	}

	public void setErwerbseinkommenUnselbstaendigkeitPartner(BigDecimal erwerbseinkommenUnselbstaendigkeitPartner) {
		this.erwerbseinkommenUnselbstaendigkeitPartner = erwerbseinkommenUnselbstaendigkeitPartner;
	}

	public BigDecimal getSteuerpflichtigesErsatzeinkommenDossiertraeger() {
		return steuerpflichtigesErsatzeinkommenDossiertraeger;
	}

	public void setSteuerpflichtigesErsatzeinkommenDossiertraeger(BigDecimal steuerpflichtigesErsatzeinkommenDossiertraeger) {
		this.steuerpflichtigesErsatzeinkommenDossiertraeger = steuerpflichtigesErsatzeinkommenDossiertraeger;
	}

	public BigDecimal getSteuerpflichtigesErsatzeinkommenPartner() {
		return steuerpflichtigesErsatzeinkommenPartner;
	}

	public void setSteuerpflichtigesErsatzeinkommenPartner(BigDecimal steuerpflichtigesErsatzeinkommenPartner) {
		this.steuerpflichtigesErsatzeinkommenPartner = steuerpflichtigesErsatzeinkommenPartner;
	}

	public BigDecimal getErhalteneUnterhaltsbeitraegeDossiertraeger() {
		return erhalteneUnterhaltsbeitraegeDossiertraeger;
	}

	public void setErhalteneUnterhaltsbeitraegeDossiertraeger(BigDecimal erhalteneUnterhaltsbeitraegeDossiertraeger) {
		this.erhalteneUnterhaltsbeitraegeDossiertraeger = erhalteneUnterhaltsbeitraegeDossiertraeger;
	}

	public BigDecimal getErhalteneUnterhaltsbeitraegePartner() {
		return erhalteneUnterhaltsbeitraegePartner;
	}

	public void setErhalteneUnterhaltsbeitraegePartner(BigDecimal erhalteneUnterhaltsbeitraegePartner) {
		this.erhalteneUnterhaltsbeitraegePartner = erhalteneUnterhaltsbeitraegePartner;
	}

	public BigDecimal getAusgewiesenerGeschaeftsertragDossiertraeger() {
		return ausgewiesenerGeschaeftsertragDossiertraeger;
	}

	public void setAusgewiesenerGeschaeftsertragDossiertraeger(BigDecimal ausgewiesenerGeschaeftsertragDossiertraeger) {
		this.ausgewiesenerGeschaeftsertragDossiertraeger = ausgewiesenerGeschaeftsertragDossiertraeger;
	}

	public BigDecimal getAusgewiesenerGeschaeftsertragPartner() {
		return ausgewiesenerGeschaeftsertragPartner;
	}

	public void setAusgewiesenerGeschaeftsertragPartner(BigDecimal ausgewiesenerGeschaeftsertragPartner) {
		this.ausgewiesenerGeschaeftsertragPartner = ausgewiesenerGeschaeftsertragPartner;
	}

	public BigDecimal getAusgewiesenerGeschaeftsertragVorperiodeDossiertraeger() {
		return ausgewiesenerGeschaeftsertragVorperiodeDossiertraeger;
	}

	public void setAusgewiesenerGeschaeftsertragVorperiodeDossiertraeger(BigDecimal ausgewiesenerGeschaeftsertragVorperiodeDossiertraeger) {
		this.ausgewiesenerGeschaeftsertragVorperiodeDossiertraeger =
			ausgewiesenerGeschaeftsertragVorperiodeDossiertraeger;
	}

	public BigDecimal getAusgewiesenerGeschaeftsertragVorperiodePartner() {
		return ausgewiesenerGeschaeftsertragVorperiodePartner;
	}

	public void setAusgewiesenerGeschaeftsertragVorperiodePartner(BigDecimal ausgewiesenerGeschaeftsertragVorperiodePartner) {
		this.ausgewiesenerGeschaeftsertragVorperiodePartner = ausgewiesenerGeschaeftsertragVorperiodePartner;
	}

	public BigDecimal getAusgewiesenerGeschaeftsertragVorperiode2Dossiertraeger() {
		return ausgewiesenerGeschaeftsertragVorperiode2Dossiertraeger;
	}

	public void setAusgewiesenerGeschaeftsertragVorperiode2Dossiertraeger(BigDecimal ausgewiesenerGeschaeftsertragVorperiode2Dossiertraeger) {
		this.ausgewiesenerGeschaeftsertragVorperiode2Dossiertraeger =
			ausgewiesenerGeschaeftsertragVorperiode2Dossiertraeger;
	}

	public BigDecimal getAusgewiesenerGeschaeftsertragVorperiode2Partner() {
		return ausgewiesenerGeschaeftsertragVorperiode2Partner;
	}

	public void setAusgewiesenerGeschaeftsertragVorperiode2Partner(BigDecimal ausgewiesenerGeschaeftsertragVorperiode2Partner) {
		this.ausgewiesenerGeschaeftsertragVorperiode2Partner = ausgewiesenerGeschaeftsertragVorperiode2Partner;
	}

	public BigDecimal getWeitereSteuerbareEinkuenfteDossiertraeger() {
		return weitereSteuerbareEinkuenfteDossiertraeger;
	}

	public void setWeitereSteuerbareEinkuenfteDossiertraeger(BigDecimal weitereSteuerbareEinkuenfteDossiertraeger) {
		this.weitereSteuerbareEinkuenfteDossiertraeger = weitereSteuerbareEinkuenfteDossiertraeger;
	}

	public BigDecimal getWeitereSteuerbareEinkuenftePartner() {
		return weitereSteuerbareEinkuenftePartner;
	}

	public void setWeitereSteuerbareEinkuenftePartner(BigDecimal weitereSteuerbareEinkuenftePartner) {
		this.weitereSteuerbareEinkuenftePartner = weitereSteuerbareEinkuenftePartner;
	}

	public BigDecimal getBruttoertraegeAusVermoegenOhneLiegenschaftenUndOhneEGME() {
		return bruttoertraegeAusVermoegenOhneLiegenschaftenUndOhneEGME;
	}

	public void setBruttoertraegeAusVermoegenOhneLiegenschaftenUndOhneEGME(BigDecimal bruttoertraegeAusVermoegenOhneLiegenschaftenUndOhneEGME) {
		this.bruttoertraegeAusVermoegenOhneLiegenschaftenUndOhneEGME =
			bruttoertraegeAusVermoegenOhneLiegenschaftenUndOhneEGME;
	}

	public BigDecimal getBruttoertraegeAusLiegenschaften() {
		return bruttoertraegeAusLiegenschaften;
	}

	public void setBruttoertraegeAusLiegenschaften(BigDecimal bruttoertraegeAusLiegenschaften) {
		this.bruttoertraegeAusLiegenschaften = bruttoertraegeAusLiegenschaften;
	}

	public BigDecimal getNettoertraegeAusEGMEDossiertraeger() {
		return nettoertraegeAusEGMEDossiertraeger;
	}

	public void setNettoertraegeAusEGMEDossiertraeger(BigDecimal nettoertraegeAusEGMEDossiertraeger) {
		this.nettoertraegeAusEGMEDossiertraeger = nettoertraegeAusEGMEDossiertraeger;
	}

	public BigDecimal getNettoertraegeAusEGMEPartner() {
		return nettoertraegeAusEGMEPartner;
	}

	public void setNettoertraegeAusEGMEPartner(BigDecimal nettoertraegeAusEGMEPartner) {
		this.nettoertraegeAusEGMEPartner = nettoertraegeAusEGMEPartner;
	}

	public BigDecimal getGeleisteteUnterhaltsbeitraege() {
		return geleisteteUnterhaltsbeitraege;
	}

	public void setGeleisteteUnterhaltsbeitraege(BigDecimal geleisteteUnterhaltsbeitraege) {
		this.geleisteteUnterhaltsbeitraege = geleisteteUnterhaltsbeitraege;
	}

	public BigDecimal getSchuldzinsen() {
		return schuldzinsen;
	}

	public void setSchuldzinsen(BigDecimal schuldzinsen) {
		this.schuldzinsen = schuldzinsen;
	}

	public BigDecimal getGewinnungskostenBeweglichesVermoegen() {
		return gewinnungskostenBeweglichesVermoegen;
	}

	public void setGewinnungskostenBeweglichesVermoegen(BigDecimal gewinnungskostenBeweglichesVermoegen) {
		this.gewinnungskostenBeweglichesVermoegen = gewinnungskostenBeweglichesVermoegen;
	}

	public BigDecimal getLiegenschaftsAbzuege() {
		return liegenschaftsAbzuege;
	}

	public void setLiegenschaftsAbzuege(BigDecimal liegenschaftsAbzuege) {
		this.liegenschaftsAbzuege = liegenschaftsAbzuege;
	}

	public BigDecimal getNettovermoegen() {
		return nettovermoegen;
	}

	public void setNettovermoegen(BigDecimal nettovermoegen) {
		this.nettovermoegen = nettovermoegen;
	}

}
