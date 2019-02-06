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

import java.time.LocalDate;
import java.util.Objects;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.ForeignKey;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import ch.dvbern.ebegu.enums.BetreuungsangebotTyp;
import ch.dvbern.ebegu.util.Constants;
import ch.dvbern.ebegu.util.EbeguUtil;
import ch.dvbern.oss.lib.beanvalidation.embeddables.IBAN;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
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
		@UniqueConstraint(columnNames = "adresse_kontoinhaber_id", name = "UK_institution_stammdaten_adressekontoinhaber_id"),
		@UniqueConstraint(columnNames = "institution_id", name= "UK_institution_stammdaten_institution_id")
	},
	indexes = {
		@Index(name = "IX_institution_stammdaten_gueltig_ab", columnList = "gueltigAb"),
		@Index(name = "IX_institution_stammdaten_gueltig_bis", columnList = "gueltigBis")
	}
)
public class InstitutionStammdaten extends AbstractDateRangedEntity {

	private static final long serialVersionUID = -8403411439882700618L;

	@Column(nullable = true)
	@Embedded
	@Valid
	private IBAN iban;

	@Enumerated(EnumType.STRING)
	@NotNull
	@Column(nullable = false)
	private BetreuungsangebotTyp betreuungsangebotTyp;

	@NotNull
	@OneToOne(optional = false)
	@JoinColumn(foreignKey = @ForeignKey(name = "FK_institution_stammdaten_institution_id"), nullable = false)
	private Institution institution;

	@NotNull
	@Pattern(regexp = Constants.REGEX_EMAIL, message = "{validator.constraints.Email.message}")
	@Size(min = 5, max = DB_DEFAULT_MAX_LENGTH)
	@Column(nullable = false)
	private String mail;

	@Nullable
	@Column(nullable = true, length = Constants.DB_DEFAULT_MAX_LENGTH)
	@Pattern(regexp = Constants.REGEX_TELEFON, message = "{validator.constraints.phonenumber.message}")
	private String telefon;

	@NotNull
	@OneToOne(optional = false, cascade = CascadeType.ALL, orphanRemoval = true)
	@JoinColumn(foreignKey = @ForeignKey(name = "FK_institution_stammdaten_adresse_id"), nullable = false)
	private Adresse adresse;

	@Nullable
	@Size(max = DB_DEFAULT_MAX_LENGTH)
	@Column(nullable = true)
	private String kontoinhaber;

	@Nullable
	@OneToOne(optional = true, cascade = CascadeType.ALL, orphanRemoval = true)
	@JoinColumn(foreignKey = @ForeignKey(name = "FK_institution_stammdaten_adressekontoinhaber_id"), nullable = true)
	private Adresse adresseKontoinhaber;

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

	public IBAN getIban() {
		return iban;
	}

	public void setIban(IBAN iban) {
		this.iban = iban;
	}

	@Nonnull
	public BetreuungsangebotTyp getBetreuungsangebotTyp() {
		return betreuungsangebotTyp;
	}

	public void setBetreuungsangebotTyp(@Nullable BetreuungsangebotTyp betreuungsangebotTyp) {
		this.betreuungsangebotTyp = betreuungsangebotTyp;
	}

	public Institution getInstitution() {
		return institution;
	}

	public void setInstitution(Institution institution) {
		this.institution = institution;
	}

	public Adresse getAdresse() {
		return adresse;
	}

	public void setAdresse(Adresse adresse) {
		this.adresse = adresse;
	}

	@SuppressFBWarnings("NM_CONFUSING")
	@Nullable
	public String getKontoinhaber() {
		return kontoinhaber;
	}

	@SuppressFBWarnings("NM_CONFUSING")
	public void setKontoinhaber(@Nullable String kontoinhaber) {
		this.kontoinhaber = kontoinhaber;
	}

	@Nullable
	public Adresse getAdresseKontoinhaber() {
		return adresseKontoinhaber;
	}

	public void setAdresseKontoinhaber(@Nullable Adresse adresseKontoinhaber) {
		this.adresseKontoinhaber = adresseKontoinhaber;
	}

	@Nullable
	public InstitutionStammdatenTagesschule getInstitutionStammdatenTagesschule() {
		return institutionStammdatenTagesschule;
	}

	public void setInstitutionStammdatenTagesschule(@Nullable InstitutionStammdatenTagesschule institutionStammdatenTagesschule) {
		this.institutionStammdatenTagesschule = institutionStammdatenTagesschule;
	}

	@Nullable
	public InstitutionStammdatenFerieninsel getInstitutionStammdatenFerieninsel() {
		return institutionStammdatenFerieninsel;
	}

	public void setInstitutionStammdatenFerieninsel(@Nullable InstitutionStammdatenFerieninsel institutionStammdatenFerieninsel) {
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

	/**
	 * Returns true when today is contained in the Gueltigkeit range
	 */
	public boolean isActive() {
		return getGueltigkeit().contains(LocalDate.now());
	}

	/**
	 * If the Institutionstammdaten isActive() it sets the Institutionstammdaten.gueltigkeit.bis to the day of yesterday.
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
		return EbeguUtil.isSameObject(getInstitution(), otherInstStammdaten.getInstitution()) &&
			getBetreuungsangebotTyp() == otherInstStammdaten.getBetreuungsangebotTyp() &&
			Objects.equals(getIban(), otherInstStammdaten.getIban()) &&
			EbeguUtil.isSameObject(getAdresse(), otherInstStammdaten.getAdresse());
	}
}
