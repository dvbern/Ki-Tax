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

import java.math.BigDecimal;
import java.util.HashSet;
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
import javax.validation.constraints.NotNull;
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
		joinColumns = @JoinColumn(name = "ferienbetreuung_angaben_id", nullable = false),
		inverseJoinColumns = @JoinColumn(name = "gemeinde_id", nullable = false),
		foreignKey = @ForeignKey(name = "FK_ferienbetreuung_angaben_gemeinde_ferienbetreuung_angaben_id"),
		inverseForeignKey = @ForeignKey(name = "FK_ferienbetreuung_angaben_gemeinde_ferienbetreuung_angaben_id"),
		indexes = {
			@Index(name = "IX_ferienbetreuung_angaben_gemeinde_ferienbetreuung_angaben_id", columnList = "ferienbetreuung_angaben_id"),
			@Index(name = "IX_ferienbetreuung_angaben_gemeinde_ferienbetreuung_angaben_id", columnList = "gemeinde_id"),
		}
	)
	private Set<Gemeinde> amAngebotBeteiligteGemeinden = new HashSet<>();

	@Nullable
	@Size(max = DB_DEFAULT_MAX_LENGTH)
	@Column(nullable = true)
	private String traegerschaft;

	@NotNull
	@OneToOne(optional = false, cascade = CascadeType.ALL, orphanRemoval = true)
	@JoinColumn(foreignKey = @ForeignKey(name = "FK_ferienbetreuung_angaben_adresse_id"), nullable = false)
	private Adresse stammdatenAdresse;

	@Nonnull
	@Size(max = DB_DEFAULT_MAX_LENGTH)
	@Column(nullable = false)
	private String stammdatenKontaktpersonVorname;

	@Nonnull
	@Size(max = DB_DEFAULT_MAX_LENGTH)
	@Column(nullable = false)
	private String stammdatenKontaktpersonNachname;

	@Nullable
	@Size(max = DB_DEFAULT_MAX_LENGTH)
	@Column(nullable = true)
	private String stammdatenKontaktpersonFunktion;

	@Nonnull
	@Size(max = DB_DEFAULT_MAX_LENGTH)
	@Column(nullable = false)
	@Pattern(regexp = Constants.REGEX_TELEFON_MOBILE, message = "{error_invalid_mobilenummer}")
	private String stammdatenKontaktpersonTelefon;

	@Nonnull
	@Pattern(regexp = Constants.REGEX_EMAIL, message = "{validator.constraints.Email.message}")
	@Size(max = DB_DEFAULT_MAX_LENGTH)
	@Column(nullable = false)
	private String stammdatenKontaktpersonEmail;

	@Nullable
	@Valid
	@OneToOne(optional = true, cascade = CascadeType.ALL, orphanRemoval = true)
	@JoinColumn(foreignKey = @ForeignKey(name = "FK_ferienbetreuung_angaben_auszahlungsdaten_id"), nullable = true)
	private Auszahlungsdaten auszahlungsdaten;

	// ANGEBOT

	@Nonnull
	@Size(max = DB_DEFAULT_MAX_LENGTH)
	@Column(nullable = false)
	private String angebot;

	@Nonnull
	@Size(max = DB_DEFAULT_MAX_LENGTH)
	@Column(nullable = false)
	private String angebotKontaktpersonVorname;

	@Nonnull
	@Size(max = DB_DEFAULT_MAX_LENGTH)
	@Column(nullable = false)
	private String angebotKontaktpersonNachname;

	@NotNull
	@OneToOne(optional = false, cascade = CascadeType.ALL, orphanRemoval = true)
	@JoinColumn(foreignKey = @ForeignKey(name = "FK_ferienbetreuung_angaben_adresse_id"), nullable = false)
	private Adresse angebotAdresse;

	@Nonnull
	@Column(nullable = false)
	private BigDecimal anzahlFerienwochenHerbstferien;

	@Nonnull
	@Column(nullable = false)
	private BigDecimal anzahlFerienwochenWinterferien;

	@Nonnull
	@Column(nullable = false)
	private BigDecimal anzahlFerienwochenFruehlingsferien;

	@Nonnull
	@Column(nullable = false)
	private BigDecimal anzahlFerienwochenSommerferien;

	@Nonnull
	@Column(nullable = false)
	private BigDecimal anzahlTage;

	@Nonnull
	@Column(nullable = false)
	private BigDecimal anzahlStundenProBetreuungstag;

	@Size(max = Constants.DB_TEXTAREA_LENGTH)
	@Nullable
	@Column(nullable = true, length = Constants.DB_TEXTAREA_LENGTH)
	private String bemerkungenOeffnungszeiten;

	@Nonnull
	@ManyToMany
	@JoinTable(
		joinColumns = @JoinColumn(name = "ferienbetreuung_angaben_id", nullable = false),
		inverseJoinColumns = @JoinColumn(name = "gemeinde_id", nullable = false),
		foreignKey = @ForeignKey(name = "FK_ferienbetreuung_angaben_gemeinde_ferienbetreuung_angaben_id_2"),
		inverseForeignKey = @ForeignKey(name = "FK_ferienbetreuung_angaben_gemeinde_ferienbetreuung_angaben_id_2"),
		indexes = {
			@Index(name = "IX_ferienbetreuung_angaben_gemeinde_ferienbetreuung_angaben_id_2", columnList = "ferienbetreuung_angaben_id"),
			@Index(name = "IX_ferienbetreuung_angaben_gemeinde_ferienbetreuung_angaben_id_2", columnList = "gemeinde_id"),
		}
	)
	private Set<Gemeinde> finanziellBeteiligteGemeinden = new HashSet<>(); // Gibt es weitere Gemeinden, die sich finanziell am Angebot beteiligen, ohne an der Trägerschaft beteiligt zu sein? Welche?

	@Nullable
	@Column(nullable = true)
	private Boolean gemeindeFuehrtAngebotSelber;

	@Nullable
	@Column(nullable = true)
	private Boolean gemeindeBeauftragtExterneAnbieter;

	@Nullable
	@Column(nullable = true)
	private Boolean angebotVereineUndPrivateIntegriert;

	@Size(max = Constants.DB_TEXTAREA_LENGTH)
	@Nullable
	@Column(nullable = true, length = Constants.DB_TEXTAREA_LENGTH)
	private String bemerkungenKooperation;

	@Nonnull
	@Column(nullable = false)
	private Boolean leitungDurchPersonMitAusbildung;

	@Nonnull
	@Column(nullable = false)
	private BigDecimal aufwandBetreuungspersonal;

	@Nonnull
	@Column(nullable = false)
	private BigDecimal zusaetzlicherAufwandLeitungAdmin;

	@Size(max = Constants.DB_TEXTAREA_LENGTH)
	@Nullable
	@Column(nullable = true, length = Constants.DB_TEXTAREA_LENGTH)
	private String bemerkungenPersonal;

	@Nonnull
	@Column(nullable = false)
	private Boolean fixerTarifKinderDerGemeinde;

	@Nonnull
	@Column(nullable = false)
	private Boolean einkommensabhaengigerTarifKinderDerGemeinde;

	@Nonnull
	@Column(nullable = false)
	private Boolean tagesschuleTarifGiltFuerFerienbetreuung;

	@Nonnull
	@Column(nullable = false)
	private Boolean ferienbetreuungTarifWirdAusTagesschuleTarifAbgeleitet;

	@Nonnull
	@Column(nullable = false)
	private Boolean kinderAusAnderenGemeindenZahlenAnderenTarif;

	@Size(max = Constants.DB_TEXTAREA_LENGTH)
	@Nullable
	@Column(nullable = true, length = Constants.DB_TEXTAREA_LENGTH)
	private String bemerkungenTarifsystem;

	// NUTZUNG

	@Nonnull
	@Column(nullable = false)
	private BigDecimal anzahlBetreuungstageKinderBern;

	@Nonnull
	@Column(nullable = false)
	private BigDecimal betreuungstageKinderDieserGemeinde;

	@Nullable
	@Column(nullable = true)
	private BigDecimal betreuungstageKinderDieserGemeindeSonderschueler;

	@Nonnull
	@Column(nullable = false)
	private BigDecimal davonBetreuungstageKinderAndererGemeinden;

	@Nullable
	@Column(nullable = true)
	private BigDecimal davonBetreuungstageKinderAndererGemeindenSonderschueler;

	@Nullable
	@Column(nullable = true)
	private BigDecimal anzahlBetreuteKinder;

	@Nullable
	@Column(nullable = true)
	private BigDecimal anzahlBetreuteKinderSonderschueler;

	@Nullable
	@Column(nullable = true, name = "anzahl_betreute_kinder_1_zyklus")
	private BigDecimal anzahlBetreuteKinder1Zyklus;

	@Nullable
	@Column(nullable = true, name = "anzahl_betreute_kinder_2_zyklus")
	private BigDecimal anzahlBetreuteKinder2Zyklus;

	@Nullable
	@Column(nullable = true, name = "anzahl_betreute_kinder_3_zyklus")
	private BigDecimal anzahlBetreuteKinder3Zyklus;

	// KOSTEN UND EINNAHMEN

	@Nonnull
	@Column(nullable = false)
	private BigDecimal personalkosten;

	@Nullable
	@Column(nullable = true)
	private BigDecimal personalkostenLeitungAdmin;

	@Nonnull
	@Column(nullable = false)
	private BigDecimal sachkosten;

	@Nonnull
	@Column(nullable = false)
	private BigDecimal verpflegungskosten;

	@Nullable
	@Column(nullable = true)
	private BigDecimal weitereKosten;

	@Size(max = Constants.DB_TEXTAREA_LENGTH)
	@Nullable
	@Column(nullable = true, length = Constants.DB_TEXTAREA_LENGTH)
	private String bemerkungenKosten;

	@Nonnull
	@Column(nullable = false)
	private BigDecimal elterngebuehren;

	@Nonnull
	@Column(nullable = false)
	private BigDecimal weitereEinnahmen;

	// RESULTATE

	@Nonnull
	@Column(nullable = false)
	private BigDecimal kantonsbeitrag;

	@Nonnull
	@Column(nullable = false)
	private BigDecimal gemeindebeitrag;

	@Override
	public boolean isSame(AbstractEntity other) {
		return getId().equals(other.getId());
	}
}
