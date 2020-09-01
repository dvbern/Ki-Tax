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

package ch.dvbern.ebegu.entities;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.persistence.CascadeType;
import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.ForeignKey;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import ch.dvbern.oss.lib.beanvalidation.embeddables.IBAN;
import org.apache.commons.lang.builder.CompareToBuilder;
import org.hibernate.envers.Audited;

import static ch.dvbern.ebegu.util.Constants.DB_DEFAULT_MAX_LENGTH;
import static ch.dvbern.ebegu.util.Constants.DB_TEXTAREA_LENGTH;

/**
 * Entitaet zum Speichern von InstitutionStammdatenTagesschule in der Datenbank.
 * Note: this class has a natural ordering that is inconsistent with equals.
 */
@Audited
@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = "auszahlungsdaten_id", name = "UK_institution_stammdaten_bg_auszahlungsdaten_id"))
public class InstitutionStammdatenBetreuungsgutscheine extends AbstractEntity implements Comparable<InstitutionStammdatenBetreuungsgutscheine> {

	private static final long serialVersionUID = -5937387773922925929L;

	@Nullable
	@OneToOne(optional = true, cascade = CascadeType.ALL, orphanRemoval = true)
	@JoinColumn(foreignKey = @ForeignKey(name = "FK_institution_stammdaten_bg_auszahlungsdaten_id"), nullable = true)
	private Auszahlungsdaten auszahlungsdaten;

	@NotNull
	@Column(nullable = false)
	private boolean alterskategorieBaby = false;

	@NotNull
	@Column(nullable = false)
	private boolean alterskategorieVorschule = false;

	@NotNull
	@Column(nullable = false)
	private boolean alterskategorieKindergarten = false;

	@NotNull
	@Column(nullable = false)
	private boolean alterskategorieSchule = false;

	@NotNull
	@Column(nullable = false)
	private boolean subventioniertePlaetze = false;

	@Nullable
	@Column(nullable = true)
	private BigDecimal anzahlPlaetze;

	@Nullable
	@Column(nullable = true)
	private BigDecimal anzahlPlaetzeFirmen;

	@Nullable
	@Column(nullable = true)
	private BigDecimal tarifProHauptmahlzeit;

	@Nullable
	@Column(nullable = true)
	private BigDecimal tarifProNebenmahlzeit;

	@ElementCollection(targetClass = DayOfWeek.class, fetch = FetchType.EAGER)
	@CollectionTable(
		name = "institutionStammdatenBetreuungsgutscheineOeffnungstag",
		joinColumns = @JoinColumn(name = "insitutionStammdatenBetreuungsgutscheine")
	)
	@Column(nullable = true)
	@Enumerated(EnumType.STRING)
	@Nonnull
	private Set<DayOfWeek> oeffnungstage = EnumSet.noneOf(DayOfWeek.class);

	@Column(nullable = true)
	@Nullable
	private LocalTime offenVon;

	@Column(nullable = true)
	@Nullable
	private LocalTime offenBis;

	@Column(nullable = true)
	@Nullable
	private @Size(max = DB_TEXTAREA_LENGTH) String oeffnungsAbweichungen;

	@OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, mappedBy = "institutionStammdatenBetreuungsgutscheine", fetch = FetchType.EAGER)
	@Nonnull
	private Set<Betreuungsstandort> betreuungsstandorte = new HashSet<>();

	public InstitutionStammdatenBetreuungsgutscheine() {
	}

	@Nullable
	public Auszahlungsdaten getAuszahlungsdaten() {
		return auszahlungsdaten;
	}

	public void setAuszahlungsdaten(@Nullable Auszahlungsdaten auszahlungsdaten) {
		this.auszahlungsdaten = auszahlungsdaten;
	}

	@Nullable
	public IBAN getIban() {
		if (auszahlungsdaten != null) {
			return auszahlungsdaten.getIban();
		}
		return null;
	}

	public void setIban(IBAN iban) {
		if (auszahlungsdaten == null) {
			auszahlungsdaten = new Auszahlungsdaten();
		}
		auszahlungsdaten.setIban(iban);
	}

	@Nullable
	public String getKontoinhaber() {
		if (auszahlungsdaten != null) {
			return auszahlungsdaten.getKontoinhaber();
		}
		return null;
	}

	public void setKontoinhaber(@Nullable String kontoinhaber) {
		if (auszahlungsdaten == null) {
			auszahlungsdaten = new Auszahlungsdaten();
		}
		auszahlungsdaten.setKontoinhaber(kontoinhaber);
	}

	@Nullable
	public Adresse getAdresseKontoinhaber() {
		if (auszahlungsdaten != null) {
			return auszahlungsdaten.getAdresseKontoinhaber();
		}
		return null;
	}

	public void setAdresseKontoinhaber(@Nullable Adresse adresseKontoinhaber) {
		if (auszahlungsdaten == null) {
			auszahlungsdaten = new Auszahlungsdaten();
		}
		auszahlungsdaten.setAdresseKontoinhaber(adresseKontoinhaber);
	}

	public boolean getAlterskategorieBaby() {
		return alterskategorieBaby;
	}

	public void setAlterskategorieBaby(boolean alterskategorieBaby) {
		this.alterskategorieBaby = alterskategorieBaby;
	}

	public boolean getAlterskategorieVorschule() {
		return alterskategorieVorschule;
	}

	public void setAlterskategorieVorschule(boolean alterskategorieVorschule) {
		this.alterskategorieVorschule = alterskategorieVorschule;
	}

	public boolean getAlterskategorieKindergarten() {
		return alterskategorieKindergarten;
	}

	public void setAlterskategorieKindergarten(boolean alterskategorieKindergarten) {
		this.alterskategorieKindergarten = alterskategorieKindergarten;
	}

	public boolean getAlterskategorieSchule() {
		return alterskategorieSchule;
	}

	public void setAlterskategorieSchule(boolean alterskategorieSchule) {
		this.alterskategorieSchule = alterskategorieSchule;
	}

	public boolean getSubventioniertePlaetze() {
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

	@Nonnull
	public Set<DayOfWeek> getOeffnungsTage() {
		return oeffnungstage;
	}

	public void setOeffnungsTage(@Nonnull Set<DayOfWeek> oeffnungstage) {
		this.oeffnungstage = oeffnungstage;
	}

	@Nullable
	public String getOeffnungsAbweichungen() {
		return oeffnungsAbweichungen;
	}

	public void setOeffnungsAbweichungen(@Nullable String oeffnungsAbweichungen) {
		this.oeffnungsAbweichungen = oeffnungsAbweichungen;
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

	@Nonnull
	public Set<Betreuungsstandort> getBetreuungsstandorte() {
		return betreuungsstandorte;
	}

	public void setBetreuungsstandorte(@Nonnull Set<Betreuungsstandort> betreuungsstandorte) {
		this.betreuungsstandorte = betreuungsstandorte;
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
		if (!(other instanceof InstitutionStammdatenBetreuungsgutscheine)) {
			return false;
		}
		final InstitutionStammdatenBetreuungsgutscheine otherInstStammdaten = (InstitutionStammdatenBetreuungsgutscheine) other;
		return Objects.equals(getAuszahlungsdaten(), otherInstStammdaten.getAuszahlungsdaten());
	}

	@Override
	public int compareTo(InstitutionStammdatenBetreuungsgutscheine o) {
		CompareToBuilder builder = new CompareToBuilder();
		builder.append(this.getId(), o.getId());
		return builder.toComparison();
	}
}
