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

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import ch.dvbern.ebegu.enums.BetreuungsangebotTyp;

@XmlRootElement(name = "institutionStammdatenSummary")
@XmlAccessorType(XmlAccessType.FIELD)
public abstract class JaxAbstractInstitutionStammdaten extends JaxAbstractDateRangedDTO {

	private static final long serialVersionUID = -1893677808322218626L;

	@Nonnull
	private BetreuungsangebotTyp betreuungsangebotTyp;
	@NotNull
	private JaxInstitution institution;
	@Nullable
	private JaxInstitutionStammdatenTagesschule institutionStammdatenTagesschule;
	@Nullable
	private JaxInstitutionStammdatenFerieninsel institutionStammdatenFerieninsel;
	@NotNull
	private String mail;
	@Nullable
	private String telefon;
	@Nullable
	private String webseite;
	@Nullable
	private String oeffnungszeiten;
	@NotNull
	private JaxAdresse adresse;
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
	private BigDecimal anzahlPlaetze = BigDecimal.ZERO;
	@Nullable
	private BigDecimal anzahlPlaetzeFirmen;

	private boolean sendMailWennOffenePendenzen = true;


	@Nullable
	public String getIban() {
		return iban;
	}

	public void setIban(@Nullable String iban) {
		this.iban = iban;
	}

	@Nonnull
	public BetreuungsangebotTyp getBetreuungsangebotTyp() {
		return betreuungsangebotTyp;
	}

	public void setBetreuungsangebotTyp(@Nonnull BetreuungsangebotTyp betreuungsangebotTyp) {
		this.betreuungsangebotTyp = betreuungsangebotTyp;
	}

	public JaxInstitution getInstitution() {
		return institution;
	}

	public void setInstitution(JaxInstitution institution) {
		this.institution = institution;
	}

	public JaxAdresse getAdresse() {
		return adresse;
	}

	public void setAdresse(JaxAdresse adresse) {
		this.adresse = adresse;
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

	@Nullable
	public JaxInstitutionStammdatenTagesschule getInstitutionStammdatenTagesschule() {
		return institutionStammdatenTagesschule;
	}

	public void setInstitutionStammdatenTagesschule(@Nullable JaxInstitutionStammdatenTagesschule institutionStammdatenTagesschule) {
		this.institutionStammdatenTagesschule = institutionStammdatenTagesschule;
	}

	@Nullable
	public JaxInstitutionStammdatenFerieninsel getInstitutionStammdatenFerieninsel() {
		return institutionStammdatenFerieninsel;
	}

	public void setInstitutionStammdatenFerieninsel(@Nullable JaxInstitutionStammdatenFerieninsel institutionStammdatenFerieninsel) {
		this.institutionStammdatenFerieninsel = institutionStammdatenFerieninsel;
	}

	public String getMail() {
		return mail;
	}

	public void setMail(String mail) {
		this.mail = mail;
	}

	@Nullable
	public String getTelefon() {
		return telefon;
	}

	public void setTelefon(@Nullable String telefon) {
		this.telefon = telefon;
	}

	@Nullable
	public String getWebseite() {
		return webseite;
	}

	public void setWebseite(@Nullable String webseite) {
		this.webseite = webseite;
	}

	@Nullable
	public String getOeffnungszeiten() {
		return oeffnungszeiten;
	}

	public void setOeffnungszeiten(@Nullable String oeffnungszeiten) {
		this.oeffnungszeiten = oeffnungszeiten;
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

	public BigDecimal getAnzahlPlaetze() {
		return anzahlPlaetze;
	}

	public void setAnzahlPlaetze(BigDecimal anzahlPlaetze) {
		this.anzahlPlaetze = anzahlPlaetze;
	}

	@Nullable
	public BigDecimal getAnzahlPlaetzeFirmen() {
		return anzahlPlaetzeFirmen;
	}

	public void setAnzahlPlaetzeFirmen(@Nullable BigDecimal anzahlPlaetzeFirmen) {
		this.anzahlPlaetzeFirmen = anzahlPlaetzeFirmen;
	}

	public boolean isSendMailWennOffenePendenzen() {
		return sendMailWennOffenePendenzen;
	}

	public void setSendMailWennOffenePendenzen(boolean sendMailWennOffenePendenzen) {
		this.sendMailWennOffenePendenzen = sendMailWennOffenePendenzen;
	}
}
