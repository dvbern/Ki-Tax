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

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ForeignKey;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.OrderBy;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import ch.dvbern.ebegu.errors.EbeguRuntimeException;
import ch.dvbern.ebegu.util.Constants;
import ch.dvbern.ebegu.validators.CheckVerfuegungPlatz;
import org.hibernate.envers.Audited;

/**
 * Verfuegung fuer eine einzelne Betreuung
 */
@CheckVerfuegungPlatz
@Entity
@Audited
public class Verfuegung extends AbstractMutableEntity {

	private static final long serialVersionUID = -6682874795746487562L;

	@Column(nullable = true, length = Constants.DB_TEXTAREA_LENGTH)
	@Nullable
	private @Size(max = Constants.DB_TEXTAREA_LENGTH) String generatedBemerkungen;

	@Column(nullable = true, length = Constants.DB_TEXTAREA_LENGTH)
	@Nullable
	private @Size(max = Constants.DB_TEXTAREA_LENGTH) String manuelleBemerkungen;

	@Nullable
	@JoinColumn(foreignKey = @ForeignKey(name = "FK_verfuegung_betreuung_id"), nullable = true)
	@OneToOne(optional = true, fetch = FetchType.EAGER)
	private Betreuung betreuung;

	@Nullable
	@JoinColumn(foreignKey = @ForeignKey(name = "FK_verfuegung_anmeldungTagesschule_id"), nullable = true)
	@OneToOne(optional = true, fetch = FetchType.EAGER)
	private AnmeldungTagesschule anmeldungTagesschule;

	@OrderBy("gueltigkeit ASC")
	@OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, mappedBy = "verfuegung")
	@Nonnull
	private @Valid List<VerfuegungZeitabschnitt> zeitabschnitte = new ArrayList<>();

	@Column(nullable = false) // Verwendet in Statistik "Gesuche nach Zeitraum", siehe orm.xml
	private @NotNull boolean kategorieNormal = false;

	@Column(nullable = false) // Verwendet in Statistik "Gesuche nach Zeitraum", siehe orm.xml
	private @NotNull boolean kategorieMaxEinkommen = false;

	@Column(nullable = false) // Verwendet in Statistik "Gesuche nach Zeitraum", siehe orm.xml
	private @NotNull boolean kategorieKeinPensum = false;

	@Column(nullable = false) // Verwendet in Statistik "Gesuche nach Zeitraum", siehe orm.xml
	private @NotNull boolean kategorieNichtEintreten = false;

	/**
	 * @deprecated used to convert all Verfuegungen from the database to VerfuegungVerfuegtEvents and publish them to
	 * Kafka. Thus, only required for one deployment.
	 */
	@Deprecated
	@Column(nullable = false)
	private @NotNull boolean eventPublished = true;

	public Verfuegung() {
	}

	@Nullable
	public String getGeneratedBemerkungen() {
		return generatedBemerkungen;
	}

	public void setGeneratedBemerkungen(@Nullable String autoInitialisierteBemerkungen) {
		this.generatedBemerkungen = autoInitialisierteBemerkungen;
	}

	@Nullable
	public String getManuelleBemerkungen() {
		return manuelleBemerkungen;
	}

	public void setManuelleBemerkungen(@Nullable String manuelleBemerkungen) {
		this.manuelleBemerkungen = manuelleBemerkungen;
	}

	@Nonnull
	public List<VerfuegungZeitabschnitt> getZeitabschnitte() {
		return zeitabschnitte;
	}

	public void setZeitabschnitte(@Nonnull List<VerfuegungZeitabschnitt> zeitabschnitte) {
		this.zeitabschnitte = zeitabschnitte;
		for (VerfuegungZeitabschnitt zeitabschnitt : this.zeitabschnitte) {
			zeitabschnitt.setVerfuegung(this);
		}
	}

	@Nullable
	public Betreuung getBetreuung() {
		return betreuung;
	}

	public void setBetreuung(@Nullable Betreuung betreuung) {
		this.betreuung = betreuung;
	}

	@Nullable
	public AnmeldungTagesschule getAnmeldungTagesschule() {
		return anmeldungTagesschule;
	}

	public void setAnmeldungTagesschule(@Nullable AnmeldungTagesschule anmeldungTagesschule) {
		this.anmeldungTagesschule = anmeldungTagesschule;
	}

	@Nonnull
	public AbstractPlatz getPlatz() {
		if (betreuung != null) {
			return betreuung;
		}
		if (anmeldungTagesschule != null) {
			return anmeldungTagesschule;
		}
		throw new EbeguRuntimeException("getPlatz", "Verfuegung ohne dazugehoerige Betreuung/AnmeldungTagesschule: " + getId());
	}

	public void setPlatz(@Nonnull AbstractPlatz platz) {
		if (platz instanceof Betreuung) {
			setBetreuung((Betreuung) platz);
		} else if (platz instanceof AnmeldungTagesschule) {
			setAnmeldungTagesschule((AnmeldungTagesschule) platz);
		} else {
			throw new EbeguRuntimeException("setPlatz", "Verfuegung gibts nur für Betreuung/AnmeldungTagesschule: " + getId());
		}
	}

	public boolean isKategorieNormal() {
		return kategorieNormal;
	}

	public void setKategorieNormal(boolean kategorieNormal) {
		this.kategorieNormal = kategorieNormal;
	}

	public boolean isKategorieMaxEinkommen() {
		return kategorieMaxEinkommen;
	}

	public void setKategorieMaxEinkommen(boolean kategorieMaxEinkommen) {
		this.kategorieMaxEinkommen = kategorieMaxEinkommen;
	}

	public boolean isKategorieKeinPensum() {
		return kategorieKeinPensum;
	}

	public void setKategorieKeinPensum(boolean kategorieKeinPensum) {
		this.kategorieKeinPensum = kategorieKeinPensum;
	}

	public boolean isKategorieNichtEintreten() {
		return kategorieNichtEintreten;
	}

	public void setKategorieNichtEintreten(boolean kategorieNichtEintreten) {
		this.kategorieNichtEintreten = kategorieNichtEintreten;
	}

	public boolean isEventPublished() {
		return eventPublished;
	}

	public void setEventPublished(boolean eventPublished) {
		this.eventPublished = eventPublished;
	}

	public String toString() {
		StringBuilder sb = new StringBuilder("Verfuegung");
		for (VerfuegungZeitabschnitt zeitabschnitt : zeitabschnitte) {
			sb.append('\n');
			sb.append(zeitabschnitt);
		}
		return sb.toString();
	}

	@Override
	@SuppressWarnings("PMD.CompareObjectsWithEquals")
	public boolean isSame(AbstractEntity other) {
		//noinspection ObjectEquality
		if (this == other) {
			return true;
		}
		if (other == null || !getClass().equals(other.getClass())) {
			return false;
		}
		final Verfuegung otherVerfuegung = (Verfuegung) other;
		return Objects.equals(getGeneratedBemerkungen(), otherVerfuegung.getGeneratedBemerkungen()) &&
			Objects.equals(getManuelleBemerkungen(), otherVerfuegung.getManuelleBemerkungen()) &&
			Objects.equals(isKategorieNormal(), otherVerfuegung.isKategorieNormal()) &&
			Objects.equals(isKategorieMaxEinkommen(), otherVerfuegung.isKategorieMaxEinkommen()) &&
			Objects.equals(isKategorieKeinPensum(), otherVerfuegung.isKategorieKeinPensum()) &&
			Objects.equals(isKategorieNichtEintreten(), otherVerfuegung.isKategorieNichtEintreten());
	}

}
