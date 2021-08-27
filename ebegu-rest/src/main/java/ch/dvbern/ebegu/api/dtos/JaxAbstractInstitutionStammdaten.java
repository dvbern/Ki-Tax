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

	@NotNull @Nonnull
	private BetreuungsangebotTyp betreuungsangebotTyp;
	@NotNull @Nonnull
	private JaxInstitution institution;
	@Nullable
	private JaxInstitutionStammdatenBetreuungsgutscheine institutionStammdatenBetreuungsgutscheine;
	@Nullable
	private JaxInstitutionStammdatenTagesschule institutionStammdatenTagesschule;
	@Nullable
	private JaxInstitutionStammdatenFerieninsel institutionStammdatenFerieninsel;
	@NotNull @Nonnull
	private String mail;
	@Nullable
	private String telefon;
	@Nullable
	private String webseite;
	// Wird nur noch read-only verwendet, um die Daten-Migration durch die Institutions-Admins zu vereinfachen
	@Nullable
	private String oeffnungszeiten;
	@NotNull @Nonnull
	private JaxAdresse adresse;
	@Nullable
	private String grundSchliessung;
	@Nullable
	private String erinnerungMail;

	private boolean sendMailWennOffenePendenzen = true;


	@Nonnull
	public BetreuungsangebotTyp getBetreuungsangebotTyp() {
		return betreuungsangebotTyp;
	}

	public void setBetreuungsangebotTyp(@Nonnull BetreuungsangebotTyp betreuungsangebotTyp) {
		this.betreuungsangebotTyp = betreuungsangebotTyp;
	}

	@Nonnull
	public JaxInstitution getInstitution() {
		return institution;
	}

	public void setInstitution(@Nonnull JaxInstitution institution) {
		this.institution = institution;
	}

	@Nonnull
	public JaxAdresse getAdresse() {
		return adresse;
	}

	public void setAdresse(@Nonnull JaxAdresse adresse) {
		this.adresse = adresse;
	}

	@Nullable
	public JaxInstitutionStammdatenBetreuungsgutscheine getInstitutionStammdatenBetreuungsgutscheine() {
		return institutionStammdatenBetreuungsgutscheine;
	}

	public void setInstitutionStammdatenBetreuungsgutscheine(@Nullable JaxInstitutionStammdatenBetreuungsgutscheine institutionStammdatenBetreuungsgutscheine) {
		this.institutionStammdatenBetreuungsgutscheine = institutionStammdatenBetreuungsgutscheine;
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

	@Nonnull
	public String getMail() {
		return mail;
	}

	public void setMail(@Nonnull String mail) {
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

	public boolean isSendMailWennOffenePendenzen() {
		return sendMailWennOffenePendenzen;
	}

	public void setSendMailWennOffenePendenzen(boolean sendMailWennOffenePendenzen) {
		this.sendMailWennOffenePendenzen = sendMailWennOffenePendenzen;
	}

	@Nullable
	public String getGrundSchliessung() {
		return grundSchliessung;
	}

	public void setGrundSchliessung(@Nullable String grundSchliessung) {
		this.grundSchliessung = grundSchliessung;
	}

	@Nullable
	public String getErinnerungMail() {
		return erinnerungMail;
	}

	public void setErinnerungMail(@Nullable String erinnerungMail) {
		this.erinnerungMail = erinnerungMail;
	}
}
