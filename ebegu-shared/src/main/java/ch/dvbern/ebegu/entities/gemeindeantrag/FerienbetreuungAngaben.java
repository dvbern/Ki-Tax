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
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ForeignKey;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.OneToOne;
import javax.validation.Valid;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import ch.dvbern.ebegu.entities.AbstractEntity;
import ch.dvbern.ebegu.entities.Adresse;
import ch.dvbern.ebegu.entities.Auszahlungsdaten;
import ch.dvbern.ebegu.entities.Gemeinde;
import ch.dvbern.ebegu.util.Constants;
import org.hibernate.envers.Audited;

import static ch.dvbern.ebegu.util.Constants.DB_DEFAULT_MAX_LENGTH;

@Entity
@Audited
public class FerienbetreuungAngaben extends AbstractEntity {

	private static final long serialVersionUID = -4376690435594903597L;

	// STAMMDATEN GEMEINDE

	@Nonnull
	@ManyToMany
	@JoinTable(
		name = "ferienbetreuung_am_angebot_beteiligte_gemeinden",
		joinColumns = @JoinColumn(name = "ferienbetreuung_angaben_id", nullable = false),
		inverseJoinColumns = @JoinColumn(name = "gemeinde_id", nullable = false),
		foreignKey = @ForeignKey(name = "FK_ferienbetreuung_angaben_gemeinde_ferienbetreuung_angaben_id"),
		inverseForeignKey = @ForeignKey(name = "FK_ferienbetreuung_angaben_gemeinde_ferienbetreuung_angaben_id"),
		indexes = {
			@Index(name = "IX_ferienbetreuung_am_angebot_beteiligte_gemeinden_angaben_id", columnList = "ferienbetreuung_angaben_id"),
			@Index(name = "IX_ferienbetreuung_am_angebot_beteiligte_gemeinden_gemeinde_id", columnList = "gemeinde_id"),
		}
	)
	private Set<Gemeinde> amAngebotBeteiligteGemeinden = new HashSet<>();

	@Nullable
	@Size(max = DB_DEFAULT_MAX_LENGTH)
	@Column()
	private String traegerschaft;

	@Nullable
	@OneToOne(optional = true, cascade = CascadeType.ALL, orphanRemoval = true)
	@JoinColumn(foreignKey = @ForeignKey(name = "FK_ferienbetreuung_stammdaten_adresse_id"), nullable = false)
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

	// ANGEBOT

	@Nullable
	@Size(max = DB_DEFAULT_MAX_LENGTH)
	@Column()
	private String angebot;

	@Nullable
	@Size(max = DB_DEFAULT_MAX_LENGTH)
	@Column()
	private String angebotKontaktpersonVorname;

	@Nullable
	@Size(max = DB_DEFAULT_MAX_LENGTH)
	@Column()
	private String angebotKontaktpersonNachname;

	@Nullable
	@OneToOne(optional = true, cascade = CascadeType.ALL, orphanRemoval = true)
	@JoinColumn(foreignKey = @ForeignKey(name = "FK_ferienbetreuung_angebot_adresse_id"))
	private Adresse angebotAdresse;

	@Nullable
	@Column()
	private BigDecimal anzahlFerienwochenHerbstferien;

	@Nullable
	@Column()
	private BigDecimal anzahlFerienwochenWinterferien;

	@Nullable
	@Column()
	private BigDecimal anzahlFerienwochenFruehlingsferien;

	@Nullable
	@Column()
	private BigDecimal anzahlFerienwochenSommerferien;

	@Nullable
	@Column()
	private BigDecimal anzahlTage;

	@Nullable
	@Column()
	private BigDecimal anzahlStundenProBetreuungstag;

	@Size(max = Constants.DB_TEXTAREA_LENGTH)
	@Nullable
	@Column(length = Constants.DB_TEXTAREA_LENGTH)
	private String bemerkungenOeffnungszeiten;

	@Nonnull
	@ManyToMany
	@JoinTable(
		name = "ferienbetreuung_finanziell_beteiligte_gemeinden",
		joinColumns = @JoinColumn(name = "ferienbetreuung_angaben_id", nullable = false),
		inverseJoinColumns = @JoinColumn(name = "gemeinde_id", nullable = false),
		foreignKey = @ForeignKey(name = "FK_ferienbetreuung_angaben_gemeinde_ferienbetreuung_angaben_id_2"),
		inverseForeignKey = @ForeignKey(name = "FK_ferienbetreuung_angaben_gemeinde_ferienbetreuung_angaben_id_2"),
		indexes = {
			@Index(name = "IX_ferienbetreuung_finanziell_beteiligte_gemeinden_angaben_id", columnList = "ferienbetreuung_angaben_id"),
			@Index(name = "IX_ferienbetreuung_finanziell_beteiligte_gemeinden_gemeinde_id", columnList = "gemeinde_id"),
		}
	)
	private Set<Gemeinde> finanziellBeteiligteGemeinden = new HashSet<>(); // Gibt es weitere Gemeinden, die sich finanziell am Angebot beteiligen, ohne an der Trägerschaft beteiligt zu sein? Welche?

	@Nullable
	@Column()
	private Boolean gemeindeFuehrtAngebotSelber;

	@Nullable
	@Column()
	private Boolean gemeindeBeauftragtExterneAnbieter;

	@Nullable
	@Column()
	private Boolean angebotVereineUndPrivateIntegriert;

	@Size(max = Constants.DB_TEXTAREA_LENGTH)
	@Nullable
	@Column(length = Constants.DB_TEXTAREA_LENGTH)
	private String bemerkungenKooperation;

	@Nullable
	@Column()
	private Boolean leitungDurchPersonMitAusbildung;

	@Nullable
	@Column()
	private BigDecimal aufwandBetreuungspersonal;

	@Nullable
	@Column()
	private BigDecimal zusaetzlicherAufwandLeitungAdmin;

	@Size(max = Constants.DB_TEXTAREA_LENGTH)
	@Nullable
	@Column(length = Constants.DB_TEXTAREA_LENGTH)
	private String bemerkungenPersonal;

	@Nullable
	@Column()
	private Boolean fixerTarifKinderDerGemeinde;

	@Nullable
	@Column()
	private Boolean einkommensabhaengigerTarifKinderDerGemeinde;

	@Nullable
	@Column()
	private Boolean tagesschuleTarifGiltFuerFerienbetreuung;

	@Nullable
	@Column()
	private Boolean ferienbetreuungTarifWirdAusTagesschuleTarifAbgeleitet;

	@Nullable
	@Column()
	private Boolean kinderAusAnderenGemeindenZahlenAnderenTarif;

	@Size(max = Constants.DB_TEXTAREA_LENGTH)
	@Nullable
	@Column(length = Constants.DB_TEXTAREA_LENGTH)
	private String bemerkungenTarifsystem;

	// NUTZUNG

	@Nullable
	@Column()
	private BigDecimal anzahlBetreuungstageKinderBern;

	@Nullable
	@Column()
	private BigDecimal betreuungstageKinderDieserGemeinde;

	@Nullable
	@Column()
	private BigDecimal betreuungstageKinderDieserGemeindeSonderschueler;

	@Nullable
	@Column()
	private BigDecimal davonBetreuungstageKinderAndererGemeinden;

	@Nullable
	@Column()
	private BigDecimal davonBetreuungstageKinderAndererGemeindenSonderschueler;

	@Nullable
	@Column()
	private BigDecimal anzahlBetreuteKinder;

	@Nullable
	@Column()
	private BigDecimal anzahlBetreuteKinderSonderschueler;

	@Nullable
	@Column(name = "anzahl_betreute_kinder_1_zyklus")
	private BigDecimal anzahlBetreuteKinder1Zyklus;

	@Nullable
	@Column(name = "anzahl_betreute_kinder_2_zyklus")
	private BigDecimal anzahlBetreuteKinder2Zyklus;

	@Nullable
	@Column(name = "anzahl_betreute_kinder_3_zyklus")
	private BigDecimal anzahlBetreuteKinder3Zyklus;

	// KOSTEN UND EINNAHMEN

	@Nullable
	@Column()
	private BigDecimal personalkosten;

	@Nullable
	@Column()
	private BigDecimal personalkostenLeitungAdmin;

	@Nullable
	@Column()
	private BigDecimal sachkosten;

	@Nullable
	@Column()
	private BigDecimal verpflegungskosten;

	@Nullable
	@Column()
	private BigDecimal weitereKosten;

	@Size(max = Constants.DB_TEXTAREA_LENGTH)
	@Nullable
	@Column(length = Constants.DB_TEXTAREA_LENGTH)
	private String bemerkungenKosten;

	@Nullable
	@Column()
	private BigDecimal elterngebuehren;

	@Nullable
	@Column()
	private BigDecimal weitereEinnahmen;

	// RESULTATE

	@Nullable
	@Column()
	private BigDecimal kantonsbeitrag;

	@Nullable
	@Column()
	private BigDecimal gemeindebeitrag;

	@Nonnull
	public Set<Gemeinde> getAmAngebotBeteiligteGemeinden() {
		return amAngebotBeteiligteGemeinden;
	}

	public void setAmAngebotBeteiligteGemeinden(@Nonnull Set<Gemeinde> amAngebotBeteiligteGemeinden) {
		this.amAngebotBeteiligteGemeinden = amAngebotBeteiligteGemeinden;
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
	public String getAngebot() {
		return angebot;
	}

	public void setAngebot(@Nullable String angebot) {
		this.angebot = angebot;
	}

	@Nullable
	public String getAngebotKontaktpersonVorname() {
		return angebotKontaktpersonVorname;
	}

	public void setAngebotKontaktpersonVorname(@Nullable String angebotKontaktpersonVorname) {
		this.angebotKontaktpersonVorname = angebotKontaktpersonVorname;
	}

	@Nullable
	public String getAngebotKontaktpersonNachname() {
		return angebotKontaktpersonNachname;
	}

	public void setAngebotKontaktpersonNachname(@Nullable String angebotKontaktpersonNachname) {
		this.angebotKontaktpersonNachname = angebotKontaktpersonNachname;
	}

	@Nullable
	public Adresse getAngebotAdresse() {
		return angebotAdresse;
	}

	public void setAngebotAdresse(@Nullable Adresse angebotAdresse) {
		this.angebotAdresse = angebotAdresse;
	}

	@Nullable
	public BigDecimal getAnzahlFerienwochenHerbstferien() {
		return anzahlFerienwochenHerbstferien;
	}

	public void setAnzahlFerienwochenHerbstferien(@Nullable BigDecimal anzahlFerienwochenHerbstferien) {
		this.anzahlFerienwochenHerbstferien = anzahlFerienwochenHerbstferien;
	}

	@Nullable
	public BigDecimal getAnzahlFerienwochenWinterferien() {
		return anzahlFerienwochenWinterferien;
	}

	public void setAnzahlFerienwochenWinterferien(@Nullable BigDecimal anzahlFerienwochenWinterferien) {
		this.anzahlFerienwochenWinterferien = anzahlFerienwochenWinterferien;
	}

	@Nullable
	public BigDecimal getAnzahlFerienwochenFruehlingsferien() {
		return anzahlFerienwochenFruehlingsferien;
	}

	public void setAnzahlFerienwochenFruehlingsferien(@Nullable BigDecimal anzahlFerienwochenFruehlingsferien) {
		this.anzahlFerienwochenFruehlingsferien = anzahlFerienwochenFruehlingsferien;
	}

	@Nullable
	public BigDecimal getAnzahlFerienwochenSommerferien() {
		return anzahlFerienwochenSommerferien;
	}

	public void setAnzahlFerienwochenSommerferien(@Nullable BigDecimal anzahlFerienwochenSommerferien) {
		this.anzahlFerienwochenSommerferien = anzahlFerienwochenSommerferien;
	}

	@Nullable
	public BigDecimal getAnzahlTage() {
		return anzahlTage;
	}

	public void setAnzahlTage(@Nullable BigDecimal anzahlTage) {
		this.anzahlTage = anzahlTage;
	}

	@Nullable
	public BigDecimal getAnzahlStundenProBetreuungstag() {
		return anzahlStundenProBetreuungstag;
	}

	public void setAnzahlStundenProBetreuungstag(@Nullable BigDecimal anzahlStundenProBetreuungstag) {
		this.anzahlStundenProBetreuungstag = anzahlStundenProBetreuungstag;
	}

	@Nullable
	public String getBemerkungenOeffnungszeiten() {
		return bemerkungenOeffnungszeiten;
	}

	public void setBemerkungenOeffnungszeiten(@Nullable String bemerkungenOeffnungszeiten) {
		this.bemerkungenOeffnungszeiten = bemerkungenOeffnungszeiten;
	}

	@Nonnull
	public Set<Gemeinde> getFinanziellBeteiligteGemeinden() {
		return finanziellBeteiligteGemeinden;
	}

	public void setFinanziellBeteiligteGemeinden(@Nonnull Set<Gemeinde> finanziellBeteiligteGemeinden) {
		this.finanziellBeteiligteGemeinden = finanziellBeteiligteGemeinden;
	}

	@Nullable
	public Boolean getGemeindeFuehrtAngebotSelber() {
		return gemeindeFuehrtAngebotSelber;
	}

	public void setGemeindeFuehrtAngebotSelber(@Nullable Boolean gemeindeFuehrtAngebotSelber) {
		this.gemeindeFuehrtAngebotSelber = gemeindeFuehrtAngebotSelber;
	}

	@Nullable
	public Boolean getGemeindeBeauftragtExterneAnbieter() {
		return gemeindeBeauftragtExterneAnbieter;
	}

	public void setGemeindeBeauftragtExterneAnbieter(@Nullable Boolean gemeindeBeauftragtExterneAnbieter) {
		this.gemeindeBeauftragtExterneAnbieter = gemeindeBeauftragtExterneAnbieter;
	}

	@Nullable
	public Boolean getAngebotVereineUndPrivateIntegriert() {
		return angebotVereineUndPrivateIntegriert;
	}

	public void setAngebotVereineUndPrivateIntegriert(@Nullable Boolean angebotVereineUndPrivateIntegriert) {
		this.angebotVereineUndPrivateIntegriert = angebotVereineUndPrivateIntegriert;
	}

	@Nullable
	public String getBemerkungenKooperation() {
		return bemerkungenKooperation;
	}

	public void setBemerkungenKooperation(@Nullable String bemerkungenKooperation) {
		this.bemerkungenKooperation = bemerkungenKooperation;
	}

	@Nullable
	public Boolean getLeitungDurchPersonMitAusbildung() {
		return leitungDurchPersonMitAusbildung;
	}

	public void setLeitungDurchPersonMitAusbildung(@Nullable Boolean leitungDurchPersonMitAusbildung) {
		this.leitungDurchPersonMitAusbildung = leitungDurchPersonMitAusbildung;
	}

	@Nullable
	public BigDecimal getAufwandBetreuungspersonal() {
		return aufwandBetreuungspersonal;
	}

	public void setAufwandBetreuungspersonal(@Nullable BigDecimal aufwandBetreuungspersonal) {
		this.aufwandBetreuungspersonal = aufwandBetreuungspersonal;
	}

	@Nullable
	public BigDecimal getZusaetzlicherAufwandLeitungAdmin() {
		return zusaetzlicherAufwandLeitungAdmin;
	}

	public void setZusaetzlicherAufwandLeitungAdmin(@Nullable BigDecimal zusaetzlicherAufwandLeitungAdmin) {
		this.zusaetzlicherAufwandLeitungAdmin = zusaetzlicherAufwandLeitungAdmin;
	}

	@Nullable
	public String getBemerkungenPersonal() {
		return bemerkungenPersonal;
	}

	public void setBemerkungenPersonal(@Nullable String bemerkungenPersonal) {
		this.bemerkungenPersonal = bemerkungenPersonal;
	}

	@Nullable
	public Boolean getFixerTarifKinderDerGemeinde() {
		return fixerTarifKinderDerGemeinde;
	}

	public void setFixerTarifKinderDerGemeinde(@Nullable Boolean fixerTarifKinderDerGemeinde) {
		this.fixerTarifKinderDerGemeinde = fixerTarifKinderDerGemeinde;
	}

	@Nullable
	public Boolean getEinkommensabhaengigerTarifKinderDerGemeinde() {
		return einkommensabhaengigerTarifKinderDerGemeinde;
	}

	public void setEinkommensabhaengigerTarifKinderDerGemeinde(@Nullable Boolean einkommensabhaengigerTarifKinderDerGemeinde) {
		this.einkommensabhaengigerTarifKinderDerGemeinde = einkommensabhaengigerTarifKinderDerGemeinde;
	}

	@Nullable
	public Boolean getTagesschuleTarifGiltFuerFerienbetreuung() {
		return tagesschuleTarifGiltFuerFerienbetreuung;
	}

	public void setTagesschuleTarifGiltFuerFerienbetreuung(@Nullable Boolean tagesschuleTarifGiltFuerFerienbetreuung) {
		this.tagesschuleTarifGiltFuerFerienbetreuung = tagesschuleTarifGiltFuerFerienbetreuung;
	}

	@Nullable
	public Boolean getFerienbetreuungTarifWirdAusTagesschuleTarifAbgeleitet() {
		return ferienbetreuungTarifWirdAusTagesschuleTarifAbgeleitet;
	}

	public void setFerienbetreuungTarifWirdAusTagesschuleTarifAbgeleitet(@Nullable Boolean ferienbetreuungTarifWirdAusTagesschuleTarifAbgeleitet) {
		this.ferienbetreuungTarifWirdAusTagesschuleTarifAbgeleitet = ferienbetreuungTarifWirdAusTagesschuleTarifAbgeleitet;
	}

	@Nullable
	public Boolean getKinderAusAnderenGemeindenZahlenAnderenTarif() {
		return kinderAusAnderenGemeindenZahlenAnderenTarif;
	}

	public void setKinderAusAnderenGemeindenZahlenAnderenTarif(@Nullable Boolean kinderAusAnderenGemeindenZahlenAnderenTarif) {
		this.kinderAusAnderenGemeindenZahlenAnderenTarif = kinderAusAnderenGemeindenZahlenAnderenTarif;
	}

	@Nullable
	public String getBemerkungenTarifsystem() {
		return bemerkungenTarifsystem;
	}

	public void setBemerkungenTarifsystem(@Nullable String bemerkungenTarifsystem) {
		this.bemerkungenTarifsystem = bemerkungenTarifsystem;
	}

	@Nullable
	public BigDecimal getAnzahlBetreuungstageKinderBern() {
		return anzahlBetreuungstageKinderBern;
	}

	public void setAnzahlBetreuungstageKinderBern(@Nullable BigDecimal anzahlBetreuungstageKinderBern) {
		this.anzahlBetreuungstageKinderBern = anzahlBetreuungstageKinderBern;
	}

	@Nullable
	public BigDecimal getBetreuungstageKinderDieserGemeinde() {
		return betreuungstageKinderDieserGemeinde;
	}

	public void setBetreuungstageKinderDieserGemeinde(@Nullable BigDecimal betreuungstageKinderDieserGemeinde) {
		this.betreuungstageKinderDieserGemeinde = betreuungstageKinderDieserGemeinde;
	}

	@Nullable
	public BigDecimal getBetreuungstageKinderDieserGemeindeSonderschueler() {
		return betreuungstageKinderDieserGemeindeSonderschueler;
	}

	public void setBetreuungstageKinderDieserGemeindeSonderschueler(@Nullable BigDecimal betreuungstageKinderDieserGemeindeSonderschueler) {
		this.betreuungstageKinderDieserGemeindeSonderschueler = betreuungstageKinderDieserGemeindeSonderschueler;
	}

	@Nullable
	public BigDecimal getDavonBetreuungstageKinderAndererGemeinden() {
		return davonBetreuungstageKinderAndererGemeinden;
	}

	public void setDavonBetreuungstageKinderAndererGemeinden(@Nullable BigDecimal davonBetreuungstageKinderAndererGemeinden) {
		this.davonBetreuungstageKinderAndererGemeinden = davonBetreuungstageKinderAndererGemeinden;
	}

	@Nullable
	public BigDecimal getDavonBetreuungstageKinderAndererGemeindenSonderschueler() {
		return davonBetreuungstageKinderAndererGemeindenSonderschueler;
	}

	public void setDavonBetreuungstageKinderAndererGemeindenSonderschueler(@Nullable BigDecimal davonBetreuungstageKinderAndererGemeindenSonderschueler) {
		this.davonBetreuungstageKinderAndererGemeindenSonderschueler = davonBetreuungstageKinderAndererGemeindenSonderschueler;
	}

	@Nullable
	public BigDecimal getAnzahlBetreuteKinder() {
		return anzahlBetreuteKinder;
	}

	public void setAnzahlBetreuteKinder(@Nullable BigDecimal anzahlBetreuteKinder) {
		this.anzahlBetreuteKinder = anzahlBetreuteKinder;
	}

	@Nullable
	public BigDecimal getAnzahlBetreuteKinderSonderschueler() {
		return anzahlBetreuteKinderSonderschueler;
	}

	public void setAnzahlBetreuteKinderSonderschueler(@Nullable BigDecimal anzahlBetreuteKinderSonderschueler) {
		this.anzahlBetreuteKinderSonderschueler = anzahlBetreuteKinderSonderschueler;
	}

	@Nullable
	public BigDecimal getAnzahlBetreuteKinder1Zyklus() {
		return anzahlBetreuteKinder1Zyklus;
	}

	public void setAnzahlBetreuteKinder1Zyklus(@Nullable BigDecimal anzahlBetreuteKinder1Zyklus) {
		this.anzahlBetreuteKinder1Zyklus = anzahlBetreuteKinder1Zyklus;
	}

	@Nullable
	public BigDecimal getAnzahlBetreuteKinder2Zyklus() {
		return anzahlBetreuteKinder2Zyklus;
	}

	public void setAnzahlBetreuteKinder2Zyklus(@Nullable BigDecimal anzahlBetreuteKinder2Zyklus) {
		this.anzahlBetreuteKinder2Zyklus = anzahlBetreuteKinder2Zyklus;
	}

	@Nullable
	public BigDecimal getAnzahlBetreuteKinder3Zyklus() {
		return anzahlBetreuteKinder3Zyklus;
	}

	public void setAnzahlBetreuteKinder3Zyklus(@Nullable BigDecimal anzahlBetreuteKinder3Zyklus) {
		this.anzahlBetreuteKinder3Zyklus = anzahlBetreuteKinder3Zyklus;
	}

	@Nullable
	public BigDecimal getPersonalkosten() {
		return personalkosten;
	}

	public void setPersonalkosten(@Nullable BigDecimal personalkosten) {
		this.personalkosten = personalkosten;
	}

	@Nullable
	public BigDecimal getPersonalkostenLeitungAdmin() {
		return personalkostenLeitungAdmin;
	}

	public void setPersonalkostenLeitungAdmin(@Nullable BigDecimal personalkostenLeitungAdmin) {
		this.personalkostenLeitungAdmin = personalkostenLeitungAdmin;
	}

	@Nullable
	public BigDecimal getSachkosten() {
		return sachkosten;
	}

	public void setSachkosten(@Nullable BigDecimal sachkosten) {
		this.sachkosten = sachkosten;
	}

	@Nullable
	public BigDecimal getVerpflegungskosten() {
		return verpflegungskosten;
	}

	public void setVerpflegungskosten(@Nullable BigDecimal verpflegungskosten) {
		this.verpflegungskosten = verpflegungskosten;
	}

	@Nullable
	public BigDecimal getWeitereKosten() {
		return weitereKosten;
	}

	public void setWeitereKosten(@Nullable BigDecimal weitereKosten) {
		this.weitereKosten = weitereKosten;
	}

	@Nullable
	public String getBemerkungenKosten() {
		return bemerkungenKosten;
	}

	public void setBemerkungenKosten(@Nullable String bemerkungenKosten) {
		this.bemerkungenKosten = bemerkungenKosten;
	}

	@Nullable
	public BigDecimal getElterngebuehren() {
		return elterngebuehren;
	}

	public void setElterngebuehren(@Nullable BigDecimal elterngebuehren) {
		this.elterngebuehren = elterngebuehren;
	}

	@Nullable
	public BigDecimal getWeitereEinnahmen() {
		return weitereEinnahmen;
	}

	public void setWeitereEinnahmen(@Nullable BigDecimal weitereEinnahmen) {
		this.weitereEinnahmen = weitereEinnahmen;
	}

	@Nullable
	public BigDecimal getKantonsbeitrag() {
		return kantonsbeitrag;
	}

	public void setKantonsbeitrag(@Nullable BigDecimal kantonsbeitrag) {
		this.kantonsbeitrag = kantonsbeitrag;
	}

	@Nullable
	public BigDecimal getGemeindebeitrag() {
		return gemeindebeitrag;
	}

	public void setGemeindebeitrag(@Nullable BigDecimal gemeindebeitrag) {
		this.gemeindebeitrag = gemeindebeitrag;
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
			this.auszahlungsdaten,
			this.angebot,
			this.angebotKontaktpersonVorname,
			this.angebotKontaktpersonNachname,
			this.angebotAdresse,
			this.anzahlFerienwochenHerbstferien,
			this.anzahlFerienwochenWinterferien,
			this.anzahlFerienwochenFruehlingsferien,
			this.anzahlFerienwochenSommerferien,
			this.anzahlTage,
			this.anzahlStundenProBetreuungstag,
			this.leitungDurchPersonMitAusbildung,
			this.anzahlBetreuungstageKinderBern,
			this.betreuungstageKinderDieserGemeinde,
			this.davonBetreuungstageKinderAndererGemeinden,
			this.personalkosten,
			this.sachkosten,
			this.verpflegungskosten,
			this.elterngebuehren,
			this.weitereEinnahmen,
			this.kantonsbeitrag,
			this.gemeindebeitrag
		);
		return nonNullObj.stream()
			.anyMatch(Objects::isNull);
	}
}
