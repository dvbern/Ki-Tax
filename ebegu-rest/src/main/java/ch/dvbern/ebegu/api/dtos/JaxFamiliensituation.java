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

package ch.dvbern.ebegu.api.dtos;

import java.time.LocalDate;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import ch.dvbern.ebegu.enums.EnumFamilienstatus;
import ch.dvbern.ebegu.enums.EnumGesuchstellerKardinalitaet;
import ch.dvbern.lib.date.converters.LocalDateXMLConverter;

/**
 * DTO fuer Familiensituationen
 */
@XmlRootElement(name = "familiensituation")
@XmlAccessorType(XmlAccessType.FIELD)
public class JaxFamiliensituation extends JaxAbstractDTO {

	private static final long serialVersionUID = -1297019741664130597L;

	@NotNull
	private EnumFamilienstatus familienstatus;

	@NotNull
	private JaxGesuch gesuch;

	@Nullable
	private Boolean gemeinsameSteuererklaerung;

	@Nullable
	private Boolean sozialhilfeBezueger;

	@Nullable
	private Boolean verguenstigungGewuenscht;

	@Nullable
	@XmlJavaTypeAdapter(LocalDateXMLConverter.class)
	private LocalDate aenderungPer = null;

	@Nullable
	@XmlJavaTypeAdapter(LocalDateXMLConverter.class)
	private LocalDate startKonkubinat = null;

	private boolean keineMahlzeitenverguenstigungBeantragt;

	@Nullable
	private String ibanMahlzeiten;

	@Nullable
	private String kontoinhaberMahlzeiten;

	private boolean abweichendeZahlungsadresseMahlzeiten;

	@Nullable
	private JaxAdresse zahlungsadresseMahlzeiten;

	@Nullable
	private String ibanInfoma;

	@Nullable
	private String kontoinhaberInfoma;

	private boolean abweichendeZahlungsadresseInfoma;

	@Nullable
	private JaxAdresse zahlungsadresseInfoma;

	@Nullable
	private String infomaKreditorennummer;

	@Nullable
	private String infomaBankcode;


	@Nullable
	private EnumGesuchstellerKardinalitaet gesuchstellerKardinalitaet;

	@Nonnull
	private boolean fkjvFamSit;

	@Nonnull
	private Integer minDauerKonkubinat;

	public static long getSerialVersionUID() {
		return serialVersionUID;
	}

	@Nonnull
	public EnumFamilienstatus getFamilienstatus() {
		return familienstatus;
	}

	public void setFamilienstatus(@Nonnull EnumFamilienstatus familienstatus) {
		this.familienstatus = familienstatus;
	}

	@Nonnull
	public JaxGesuch getGesuch() {
		return gesuch;
	}

	public void setGesuch(@Nonnull JaxGesuch gesuch) {
		this.gesuch = gesuch;
	}

	@Nullable
	public Boolean getGemeinsameSteuererklaerung() {
		return gemeinsameSteuererklaerung;
	}

	public void setGemeinsameSteuererklaerung(@Nullable Boolean gemeinsameSteuererklaerung) {
		this.gemeinsameSteuererklaerung = gemeinsameSteuererklaerung;
	}

	@Nullable
	public LocalDate getAenderungPer() {
		return aenderungPer;
	}

	public void setAenderungPer(@Nullable LocalDate aenderungPer) {
		this.aenderungPer = aenderungPer;
	}

	@Nullable
	public LocalDate getStartKonkubinat() {
		return startKonkubinat;
	}

	public void setStartKonkubinat(@Nullable LocalDate startKonkubinat) {
		this.startKonkubinat = startKonkubinat;
	}

	@Nullable
	public Boolean getSozialhilfeBezueger() {
		return sozialhilfeBezueger;
	}

	public void setSozialhilfeBezueger(@Nullable Boolean sozialhilfeBezueger) {
		this.sozialhilfeBezueger = sozialhilfeBezueger;
	}

	@Nullable
	public Boolean getVerguenstigungGewuenscht() {
		return verguenstigungGewuenscht;
	}

	public void setVerguenstigungGewuenscht(@Nullable Boolean verguenstigungGewuenscht) {
		this.verguenstigungGewuenscht = verguenstigungGewuenscht;
	}

	public boolean isKeineMahlzeitenverguenstigungBeantragt() {
		return keineMahlzeitenverguenstigungBeantragt;
	}

	public void setKeineMahlzeitenverguenstigungBeantragt(boolean keineMahlzeitenverguenstigungBeantragt) {
		this.keineMahlzeitenverguenstigungBeantragt = keineMahlzeitenverguenstigungBeantragt;
	}

	@Nullable
	public String getIbanMahlzeiten() {
		return ibanMahlzeiten;
	}

	public void setIbanMahlzeiten(@Nullable String iban) {
		this.ibanMahlzeiten = iban;
	}

	@Nullable
	public String getKontoinhaberMahlzeiten() {
		return kontoinhaberMahlzeiten;
	}

	public void setKontoinhaberMahlzeiten(@Nullable String kontoinhaber) {
		this.kontoinhaberMahlzeiten = kontoinhaber;
	}

	public boolean isAbweichendeZahlungsadresseMahlzeiten() {
		return abweichendeZahlungsadresseMahlzeiten;
	}

	public void setAbweichendeZahlungsadresseMahlzeiten(boolean abweichendeZahlungsadresse) {
		this.abweichendeZahlungsadresseMahlzeiten = abweichendeZahlungsadresse;
	}

	@Nullable
	public JaxAdresse getZahlungsadresseMahlzeiten() {
		return zahlungsadresseMahlzeiten;
	}

	public void setZahlungsadresseMahlzeiten(@Nullable JaxAdresse zahlungsadresse) {
		this.zahlungsadresseMahlzeiten = zahlungsadresse;
	}

	@Nullable
	public String getIbanInfoma() {
		return ibanInfoma;
	}

	public void setIbanInfoma(@Nullable String ibanInfoma) {
		this.ibanInfoma = ibanInfoma;
	}

	@Nullable
	public String getKontoinhaberInfoma() {
		return kontoinhaberInfoma;
	}

	public void setKontoinhaberInfoma(@Nullable String kontoinhaberInfoma) {
		this.kontoinhaberInfoma = kontoinhaberInfoma;
	}

	public boolean isAbweichendeZahlungsadresseInfoma() {
		return abweichendeZahlungsadresseInfoma;
	}

	public void setAbweichendeZahlungsadresseInfoma(boolean abweichendeZahlungsadresseInfoma) {
		this.abweichendeZahlungsadresseInfoma = abweichendeZahlungsadresseInfoma;
	}

	@Nullable
	public JaxAdresse getZahlungsadresseInfoma() {
		return zahlungsadresseInfoma;
	}

	public void setZahlungsadresseInfoma(@Nullable JaxAdresse zahlungsadresseInfoma) {
		this.zahlungsadresseInfoma = zahlungsadresseInfoma;
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

	@Nullable
	public EnumGesuchstellerKardinalitaet getGesuchstellerKardinalitaet() {
		return gesuchstellerKardinalitaet;
	}

	public void setGesuchstellerKardinalitaet(@Nullable EnumGesuchstellerKardinalitaet gesuchstellerKardinalitaet) {
		this.gesuchstellerKardinalitaet = gesuchstellerKardinalitaet;
	}

	public boolean isFkjvFamSit() {
		return fkjvFamSit;
	}

	public void setFkjvFamSit(boolean fkjvFamSit) {
		this.fkjvFamSit = fkjvFamSit;
	}

	public Integer getMinDauerKonkubinat() {
		return minDauerKonkubinat;
	}

	public void setMinDauerKonkubinat(Integer minDauerKonkubinat) {
		this.minDauerKonkubinat = minDauerKonkubinat;
	}
}
