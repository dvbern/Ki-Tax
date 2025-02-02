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

import ch.dvbern.ebegu.enums.EnumFamilienstatus;
import ch.dvbern.ebegu.enums.EnumGesuchstellerKardinalitaet;
import ch.dvbern.ebegu.enums.UnterhaltsvereinbarungAnswer;
import ch.dvbern.ebegu.validators.iban.CheckIBANString;
import ch.dvbern.lib.date.converters.LocalDateXMLConverter;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.time.LocalDate;

/**
 * DTO fuer Familiensituationen
 */
@XmlRootElement(name = "familiensituation")
@XmlAccessorType(XmlAccessType.FIELD)
public class JaxFamiliensituation extends JaxAbstractDTO {

	private static final long serialVersionUID = 6551372166765101992L;

	@NotNull
	private EnumFamilienstatus familienstatus;

	@Nullable
	private Boolean gemeinsameSteuererklaerung;

	@Nullable
	private Boolean sozialhilfeBezueger;

	@Nullable
	private Boolean partnerIdentischMitVorgesuch;

	@Nullable
	private String zustaendigeAmtsstelle;

	@Nullable
	private String nameBetreuer;

	@Nullable
	private Boolean verguenstigungGewuenscht;

	@Nullable
	@XmlJavaTypeAdapter(LocalDateXMLConverter.class)
	private LocalDate aenderungPer = null;

	@Nullable
	@XmlJavaTypeAdapter(LocalDateXMLConverter.class)
	private LocalDate startKonkubinat = null;

	private boolean keineMahlzeitenverguenstigungBeantragt;

	private boolean keineMahlzeitenverguenstigungBeantragtEditable;

	@Nullable
	@CheckIBANString
	private String iban;

	@Nullable
	private String kontoinhaber;

	private boolean abweichendeZahlungsadresse;

	@Nullable
	private JaxAdresse zahlungsadresse;

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

	@Nullable
	private UnterhaltsvereinbarungAnswer unterhaltsvereinbarung;

	@Nullable
	private String unterhaltsvereinbarungBemerkung;

	@Nullable
	private Boolean geteilteObhut;

	@Nullable
	private Boolean gemeinsamerHaushaltMitObhutsberechtigterPerson;

	@Nullable
	private Boolean gemeinsamerHaushaltMitPartner;

	private boolean auszahlungAusserhalbVonKibon;

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

	public void setPartnerIdentischMitVorgesuch(@Nullable Boolean identischerPartner) {
		this.partnerIdentischMitVorgesuch = identischerPartner;
	}

	public Boolean getPartnerIdentischMitVorgesuch(){
		return partnerIdentischMitVorgesuch;
	}

	@Nullable
	public String getZustaendigeAmtsstelle() {
		return zustaendigeAmtsstelle;
	}

	public void setZustaendigeAmtsstelle(@Nullable String zustaendigeAmtsstelle) {
		this.zustaendigeAmtsstelle = zustaendigeAmtsstelle;
	}

	@Nullable
	public String getNameBetreuer() {
		return nameBetreuer;
	}

	public void setNameBetreuer(@Nullable String nameBetreuer) {
		this.nameBetreuer = nameBetreuer;
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

	public boolean isKeineMahlzeitenverguenstigungBeantragtEditable() {
		return keineMahlzeitenverguenstigungBeantragtEditable;
	}

	public void setKeineMahlzeitenverguenstigungBeantragtEditable(boolean keineMahlzeitenverguenstigungBeantragtEditable) {
		this.keineMahlzeitenverguenstigungBeantragtEditable = keineMahlzeitenverguenstigungBeantragtEditable;
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

	public boolean isAbweichendeZahlungsadresse() {
		return abweichendeZahlungsadresse;
	}

	public void setAbweichendeZahlungsadresse(boolean abweichendeZahlungsadresse) {
		this.abweichendeZahlungsadresse = abweichendeZahlungsadresse;
	}

	@Nullable
	public JaxAdresse getZahlungsadresse() {
		return zahlungsadresse;
	}

	public void setZahlungsadresse(@Nullable JaxAdresse zahlungsadresse) {
		this.zahlungsadresse = zahlungsadresse;
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

	@Nullable
	public UnterhaltsvereinbarungAnswer getUnterhaltsvereinbarung() {
		return unterhaltsvereinbarung;
	}

	public void setUnterhaltsvereinbarung(@Nullable UnterhaltsvereinbarungAnswer unterhaltsvereinbarung) {
		this.unterhaltsvereinbarung = unterhaltsvereinbarung;
	}

	@Nullable
	public String getUnterhaltsvereinbarungBemerkung() {
		return unterhaltsvereinbarungBemerkung;
	}

	public void setUnterhaltsvereinbarungBemerkung(@Nullable String unterhaltsvereinbarungBemerkung) {
		this.unterhaltsvereinbarungBemerkung = unterhaltsvereinbarungBemerkung;
	}

	@Nullable
	public Boolean getGeteilteObhut() {
		return geteilteObhut;
	}

	public void setGeteilteObhut(@Nullable Boolean geteilteObhut) {
		this.geteilteObhut = geteilteObhut;
	}

	@Nullable
	public Boolean getGemeinsamerHaushaltMitObhutsberechtigterPerson() {
		return gemeinsamerHaushaltMitObhutsberechtigterPerson;
	}

	public void setGemeinsamerHaushaltMitObhutsberechtigterPerson(
		@Nullable Boolean gemeinsamerHaushaltMitObhutsberechtigterPerson) {
		this.gemeinsamerHaushaltMitObhutsberechtigterPerson = gemeinsamerHaushaltMitObhutsberechtigterPerson;
	}

	@Nullable
	public Boolean getGemeinsamerHaushaltMitPartner() {
		return gemeinsamerHaushaltMitPartner;
	}

	public void setGemeinsamerHaushaltMitPartner(@Nullable Boolean gemeinsamerHaushaltMitPartner) {
		this.gemeinsamerHaushaltMitPartner = gemeinsamerHaushaltMitPartner;
	}

	public boolean isAuszahlungAusserhalbVonKibon() {
		return auszahlungAusserhalbVonKibon;
	}

	public void setAuszahlungAusserhalbVonKibon(boolean auszahlungAusserhalbVonKibon) {
		this.auszahlungAusserhalbVonKibon = auszahlungAusserhalbVonKibon;
	}
}
