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

package ch.dvbern.ebegu.entities;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.ForeignKey;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import ch.dvbern.ebegu.enums.BetreuungsangebotTyp;
import ch.dvbern.ebegu.util.Constants;
import ch.dvbern.ebegu.util.EbeguUtil;
import ch.dvbern.oss.lib.beanvalidation.embeddables.IBAN;
import org.hibernate.envers.Audited;

import static ch.dvbern.ebegu.util.Constants.DB_DEFAULT_MAX_LENGTH;

/**
 * Entitaet zum Speichern von InstitutionStammdaten in der Datenbank.
 */
@Audited
@Entity
@Table(
	uniqueConstraints = {
		@UniqueConstraint(columnNames = "adresse_id", name = "UK_institution_stammdaten_adresse_id"),
		@UniqueConstraint(columnNames = "institution_id", name = "UK_institution_stammdaten_institution_id")
	},
	indexes = {
		@Index(name = "IX_institution_stammdaten_gueltig_ab", columnList = "gueltigAb"),
		@Index(name = "IX_institution_stammdaten_gueltig_bis", columnList = "gueltigBis")
	}
)
public class InstitutionStammdaten extends AbstractDateRangedEntity {

	private static final long serialVersionUID = -8403411439882700618L;

	@Column(nullable = false)
	@Nonnull
	@Enumerated(EnumType.STRING)
	private @NotNull BetreuungsangebotTyp betreuungsangebotTyp;

	@JoinColumn(foreignKey = @ForeignKey(name = "FK_institution_stammdaten_institution_id"), nullable = false)
	@OneToOne(optional = false)
	private @NotNull Institution institution;

	@Column(nullable = false)
	private @NotNull
	@Pattern(regexp = Constants.REGEX_EMAIL, message = "{validator.constraints.Email.message}")
	@Size(min = 5, max = DB_DEFAULT_MAX_LENGTH)
	String mail;

	@Column(nullable = true, length = Constants.DB_DEFAULT_MAX_LENGTH)
	@Nullable
	private @Pattern(regexp = Constants.REGEX_TELEFON, message = "{validator.constraints.phonenumber.message}")
	String telefon;

	@Column(nullable = true)
	@Nullable
	private @Size(max = DB_DEFAULT_MAX_LENGTH) String webseite;

	@Column(nullable = true)
	@Nullable
	private @Size(max = DB_DEFAULT_MAX_LENGTH) String oeffnungsAbweichungen;

	@ElementCollection(targetClass = DayOfWeek.class)
	@JoinTable(
		name = "institutionStammdatenOeffnungszeit",
		joinColumns = @JoinColumn(name = "insitutionStammdaten")
	)
	@Column(nullable = false)
	@Enumerated(EnumType.STRING)
	private Collection<DayOfWeek> oeffnungszeiten;

	@Column(nullable = false)
	@Nullable
	private LocalTime offenVon;

	@Column(nullable = false)
	@Nullable
	private LocalTime offenBis;

	@JoinColumn(foreignKey = @ForeignKey(name = "FK_institution_stammdaten_adresse_id"), nullable = false)
	@OneToOne(optional = false, cascade = CascadeType.ALL, orphanRemoval = true)
	@Nonnull
	private @NotNull Adresse adresse = new Adresse();

	@OneToMany(cascade = CascadeType.REMOVE, orphanRemoval = true, mappedBy = "institutionStammdaten", fetch = FetchType.LAZY)
	private List<Betreuungsstandort> betreuungsstandorte = new ArrayList<Betreuungsstandort>();

	@Column(nullable = false)
	private boolean mehrereBetreuungsstandorte = false;

	@Column(nullable = false)
	private boolean sendMailWennOffenePendenzen = true;

	@Nullable
	@OneToOne(optional = true, cascade = CascadeType.ALL, orphanRemoval = true)
	@JoinColumn(foreignKey = @ForeignKey(name = "FK_inst_stammdaten_inst_stammdaten_bg_id"), nullable = true)
	private InstitutionStammdatenBetreuungsgutscheine institutionStammdatenBetreuungsgutscheine;

	@Nullable
	@OneToOne(optional = true, cascade = CascadeType.ALL, orphanRemoval = true)
	@JoinColumn(foreignKey = @ForeignKey(name = "FK_inst_stammdaten_inst_stammdaten_tagesschule_id"), nullable = true)
	private InstitutionStammdatenTagesschule institutionStammdatenTagesschule;

	@Nullable
	@OneToOne(optional = true, cascade = CascadeType.ALL, orphanRemoval = true)
	@JoinColumn(foreignKey = @ForeignKey(name = "FK_inst_stammdaten_inst_stammdaten_ferieninsel_id"), nullable = true)
	private InstitutionStammdatenFerieninsel institutionStammdatenFerieninsel;

	public InstitutionStammdaten() {
	}

	public InstitutionStammdaten(@Nonnull Institution institution) {
		this.institution = institution;
	}

	@Nonnull
	public BetreuungsangebotTyp getBetreuungsangebotTyp() {
		return betreuungsangebotTyp;
	}

	public void setBetreuungsangebotTyp(@Nonnull BetreuungsangebotTyp betreuungsangebotTyp) {
		this.betreuungsangebotTyp = betreuungsangebotTyp;
	}

	@Nonnull
	public Institution getInstitution() {
		return institution;
	}

	public void setInstitution(@Nonnull Institution institution) {
		this.institution = institution;
	}

	@Nonnull
	public Adresse getAdresse() {
		return adresse;
	}

	public void setAdresse(@Nonnull Adresse adresse) {
		this.adresse = adresse;
	}

	public List<Betreuungsstandort> getBetreuungsstandorte() {
		return betreuungsstandorte;
	}

	public void setBetreuungsstandorte(List<Betreuungsstandort> betreuungsstandorte) {
		this.betreuungsstandorte = betreuungsstandorte;
	}

	public boolean hasMehrereBetreuungsstandorte() {
		return mehrereBetreuungsstandorte;
	}

	public void setMehrereBetreuungsstandorte(boolean mehrereBetreuungsstandorte) {
		this.mehrereBetreuungsstandorte = mehrereBetreuungsstandorte;
	}

	@Nullable
	public InstitutionStammdatenBetreuungsgutscheine getInstitutionStammdatenBetreuungsgutscheine() {
		return institutionStammdatenBetreuungsgutscheine;
	}

	public void setInstitutionStammdatenBetreuungsgutscheine(
		@Nullable InstitutionStammdatenBetreuungsgutscheine institutionStammdatenBetreuungsgutscheine) {
		this.institutionStammdatenBetreuungsgutscheine = institutionStammdatenBetreuungsgutscheine;
	}

	@Nullable
	public InstitutionStammdatenTagesschule getInstitutionStammdatenTagesschule() {
		return institutionStammdatenTagesschule;
	}

	public void setInstitutionStammdatenTagesschule(
		@Nullable InstitutionStammdatenTagesschule institutionStammdatenTagesschule) {
		this.institutionStammdatenTagesschule = institutionStammdatenTagesschule;
	}

	@Nullable
	public InstitutionStammdatenFerieninsel getInstitutionStammdatenFerieninsel() {
		return institutionStammdatenFerieninsel;
	}

	public void setInstitutionStammdatenFerieninsel(
		@Nullable InstitutionStammdatenFerieninsel institutionStammdatenFerieninsel) {
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
	public String getOeffnungsAbweichungen() {
		return oeffnungsAbweichungen;
	}

	public void setOeffnungsAbweichungen(@Nullable String oeffnungszeiten) {
		this.oeffnungsAbweichungen = oeffnungszeiten;
	}

	public Collection<DayOfWeek> getOeffnungszeiten() {
		return oeffnungszeiten;
	}

	public void setOeffnungszeiten(Collection<DayOfWeek> oeffnungszeiten) {
		this.oeffnungszeiten = oeffnungszeiten;
	}

	@Nullable
	public LocalTime getOffenVon() {
		return offenVon;
	}

	public void setOffenVon(@Nullable LocalTime offenVon) {
		this.offenVon = offenVon;
	}

	@Nullable
	public LocalTime getOffenBis() {
		return offenBis;
	}

	public void setOffenBis(@Nullable LocalTime offenBis) {
		this.offenBis = offenBis;
	}

	public boolean getSendMailWennOffenePendenzen() {
		return sendMailWennOffenePendenzen;
	}

	public void setSendMailWennOffenePendenzen(boolean sendMailWennOffenePendenzen) {
		this.sendMailWennOffenePendenzen = sendMailWennOffenePendenzen;
	}

	/**
	 * Returns true when today is contained in the Gueltigkeit range
	 */
	public boolean isActive() {
		return getGueltigkeit().contains(LocalDate.now());
	}

	/**
	 * If the Institutionstammdaten isActive() it sets the Institutionstammdaten.gueltigkeit.bis to the day of
	 * yesterday.
	 * If it is already inactive there is no need to set it inactive again.
	 */
	public void setInactive() {
		if (isActive()) {
			getGueltigkeit().setGueltigBis(LocalDate.now().minusDays(1));
		}
	}

	@Override
	public boolean isSame(AbstractEntity other) {
		//noinspection ObjectEquality
		if (this == other) {
			return true;
		}
		if (other == null || !getClass().equals(other.getClass())) {
			return false;
		}
		if (!super.isSame(other)) {
			return false;
		}
		if (!(other instanceof InstitutionStammdaten)) {
			return false;
		}
		final InstitutionStammdaten otherInstStammdaten = (InstitutionStammdaten) other;
		return EbeguUtil.isSame(getInstitution(), otherInstStammdaten.getInstitution()) &&
			getBetreuungsangebotTyp() == otherInstStammdaten.getBetreuungsangebotTyp() &&
			EbeguUtil.isSame(getAdresse(), otherInstStammdaten.getAdresse()) &&
			EbeguUtil.isSame(
				getInstitutionStammdatenBetreuungsgutscheine(),
				otherInstStammdaten.getInstitutionStammdatenBetreuungsgutscheine()) &&
			EbeguUtil.isSame(
				getInstitutionStammdatenTagesschule(),
				otherInstStammdaten.getInstitutionStammdatenTagesschule()) &&
			EbeguUtil.isSame(
				getInstitutionStammdatenFerieninsel(),
				otherInstStammdaten.getInstitutionStammdatenFerieninsel());
	}

	@Nullable
	public String extractKontoinhaber() {
		if (getInstitutionStammdatenBetreuungsgutscheine() != null) {
			return getInstitutionStammdatenBetreuungsgutscheine().getKontoinhaber();
		}
		return null;
	}

	@Nullable
	public Adresse extractAdresseKontoinhaber() {
		if (getInstitutionStammdatenBetreuungsgutscheine() != null) {
			return getInstitutionStammdatenBetreuungsgutscheine().getAdresseKontoinhaber();
		}
		return null;
	}

	@Nullable
	public IBAN extractIban() {
		if (getInstitutionStammdatenBetreuungsgutscheine() != null) {
			return getInstitutionStammdatenBetreuungsgutscheine().getIban();
		}
		return null;
	}

	public boolean isTagesschuleActivatable() {
		final InstitutionStammdatenTagesschule stammdaten = this.getInstitutionStammdatenTagesschule();
		return stammdaten != null && !stammdaten.extractAllModulTagesschuleGroup().isEmpty();
	}

	@Override
	public String getMessageForAccessException() {
		return "bgNummer: " + this.getBetreuungsangebotTyp()
			+ ", institution: " + this.getInstitution().getMessageForAccessException();
	}
}
