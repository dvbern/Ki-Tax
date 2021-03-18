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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package ch.dvbern.ebegu.entities.gemeindeantrag;

import java.io.Serializable;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.persistence.CascadeType;
import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ForeignKey;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.validation.Valid;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import ch.dvbern.ebegu.entities.AbstractEntity;
import ch.dvbern.ebegu.entities.Adresse;
import ch.dvbern.ebegu.entities.Auszahlungsdaten;
import ch.dvbern.ebegu.util.Constants;
import org.hibernate.envers.Audited;

import static ch.dvbern.ebegu.util.Constants.DB_DEFAULT_MAX_LENGTH;

@Entity
@Audited
public class FerienbetreuungAngabenStammdaten extends AbstractEntity {

	private static final long serialVersionUID = -4711352384230177665L;

	@ElementCollection(fetch = FetchType.EAGER)
	@CollectionTable(
		name = "ferienbetreuung_am_angebot_beteiligte_gemeinden",
		joinColumns = @JoinColumn(name = "ferienbetreuung_stammdaten_id")
	)
	@Column(nullable = false)
	@Nonnull
	private Set<String> amAngebotBeteiligteGemeinden = new HashSet<>();

	@Nullable
	@Size(max = DB_DEFAULT_MAX_LENGTH)
	@Column()
	private String seitWannFerienbetreuungen;

	@Nullable
	@Size(max = DB_DEFAULT_MAX_LENGTH)
	@Column()
	private String traegerschaft;

	@Nullable
	@OneToOne(optional = true, cascade = CascadeType.ALL, orphanRemoval = true)
	@JoinColumn(foreignKey = @ForeignKey(name = "FK_ferienbetreuung_stammdaten_adresse_id"))
	private Adresse stammdatenAdresse;

	@Nullable
	@Size(max = DB_DEFAULT_MAX_LENGTH)
	@Column()
	private String stammdatenKontaktpersonVorname;

	@Nullable
	@Size(max = DB_DEFAULT_MAX_LENGTH)
	@Column()
	private String stammdatenKontaktpersonNachname;

	@Nullable
	@Size(max = DB_DEFAULT_MAX_LENGTH)
	@Column()
	private String stammdatenKontaktpersonFunktion;

	@Nullable
	@Size(max = DB_DEFAULT_MAX_LENGTH)
	@Column()
	@Pattern(regexp = Constants.REGEX_TELEFON_MOBILE, message = "{error_invalid_mobilenummer}")
	private String stammdatenKontaktpersonTelefon;

	@Nullable
	@Pattern(regexp = Constants.REGEX_EMAIL, message = "{validator.constraints.Email.message}")
	@Size(max = DB_DEFAULT_MAX_LENGTH)
	@Column()
	private String stammdatenKontaktpersonEmail;

	@Nullable
	@Valid
	@OneToOne(optional = true, cascade = CascadeType.ALL, orphanRemoval = true)
	@JoinColumn(foreignKey = @ForeignKey(name = "FK_ferienbetreuung_angaben_auszahlungsdaten_id"))
	private Auszahlungsdaten auszahlungsdaten;

	@Nullable
	@Size(max = DB_DEFAULT_MAX_LENGTH)
	@Column()
	private String vermerkAuszahlung;

	@Nonnull
	public Set<String> getAmAngebotBeteiligteGemeinden() {
		return amAngebotBeteiligteGemeinden;
	}

	public void setAmAngebotBeteiligteGemeinden(@Nonnull Set<String> amAngebotBeteiligteGemeinden) {
		this.amAngebotBeteiligteGemeinden = amAngebotBeteiligteGemeinden;
	}

	@Nullable
	public String getSeitWannFerienbetreuungen() {
		return seitWannFerienbetreuungen;
	}

	public void setSeitWannFerienbetreuungen(@Nullable String seitWannFerienbetreuungen) {
		this.seitWannFerienbetreuungen = seitWannFerienbetreuungen;
	}

	@Nullable
	public String getTraegerschaft() {
		return traegerschaft;
	}

	public void setTraegerschaft(@Nullable String traegerschaft) {
		this.traegerschaft = traegerschaft;
	}

	@Nullable
	public Adresse getStammdatenAdresse() {
		return stammdatenAdresse;
	}

	public void setStammdatenAdresse(@Nullable Adresse stammdatenAdresse) {
		this.stammdatenAdresse = stammdatenAdresse;
	}

	@Nullable
	public String getStammdatenKontaktpersonVorname() {
		return stammdatenKontaktpersonVorname;
	}

	public void setStammdatenKontaktpersonVorname(@Nullable String stammdatenKontaktpersonVorname) {
		this.stammdatenKontaktpersonVorname = stammdatenKontaktpersonVorname;
	}

	@Nullable
	public String getStammdatenKontaktpersonNachname() {
		return stammdatenKontaktpersonNachname;
	}

	public void setStammdatenKontaktpersonNachname(@Nullable String stammdatenKontaktpersonNachname) {
		this.stammdatenKontaktpersonNachname = stammdatenKontaktpersonNachname;
	}

	@Nullable
	public String getStammdatenKontaktpersonFunktion() {
		return stammdatenKontaktpersonFunktion;
	}

	public void setStammdatenKontaktpersonFunktion(@Nullable String stammdatenKontaktpersonFunktion) {
		this.stammdatenKontaktpersonFunktion = stammdatenKontaktpersonFunktion;
	}

	@Nullable
	public String getStammdatenKontaktpersonTelefon() {
		return stammdatenKontaktpersonTelefon;
	}

	public void setStammdatenKontaktpersonTelefon(@Nullable String stammdatenKontaktpersonTelefon) {
		this.stammdatenKontaktpersonTelefon = stammdatenKontaktpersonTelefon;
	}

	@Nullable
	public String getStammdatenKontaktpersonEmail() {
		return stammdatenKontaktpersonEmail;
	}

	public void setStammdatenKontaktpersonEmail(@Nullable String stammdatenKontaktpersonEmail) {
		this.stammdatenKontaktpersonEmail = stammdatenKontaktpersonEmail;
	}

	@Nullable
	public Auszahlungsdaten getAuszahlungsdaten() {
		return auszahlungsdaten;
	}

	public void setAuszahlungsdaten(@Nullable Auszahlungsdaten auszahlungsdaten) {
		this.auszahlungsdaten = auszahlungsdaten;
	}

	@Nullable
	public String getVermerkAuszahlung() {
		return vermerkAuszahlung;
	}

	public void setVermerkAuszahlung(@Nullable String vermerkAuszahlung) {
		this.vermerkAuszahlung = vermerkAuszahlung;
	}

	@Override
	public boolean isSame(AbstractEntity other) {
		return getId().equals(other.getId());
	}

	public boolean isReadyForFreigeben() {
		return checkPropertiesNotNull();
	}

	private boolean checkPropertiesNotNull() {
		List<Serializable> nonNullObj = Arrays.asList(
			this.stammdatenKontaktpersonVorname,
			this.stammdatenKontaktpersonNachname,
			this.stammdatenAdresse,
			this.stammdatenKontaktpersonTelefon,
			this.stammdatenKontaktpersonEmail,
			this.auszahlungsdaten
		);
		return nonNullObj.stream()
			.anyMatch(Objects::isNull);
	}
}
