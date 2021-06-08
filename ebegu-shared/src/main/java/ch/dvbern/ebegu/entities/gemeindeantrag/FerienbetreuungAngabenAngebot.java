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
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.persistence.CascadeType;
import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.ForeignKey;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.validation.constraints.Size;

import ch.dvbern.ebegu.entities.AbstractEntity;
import ch.dvbern.ebegu.entities.Adresse;
import ch.dvbern.ebegu.enums.AntragCopyType;
import ch.dvbern.ebegu.enums.KinderAusAnderenGemeindenZahlenAnderenTarifAnswer;
import ch.dvbern.ebegu.enums.gemeindeantrag.FerienbetreuungFormularStatus;
import ch.dvbern.ebegu.util.Constants;
import org.hibernate.envers.Audited;

import static ch.dvbern.ebegu.util.Constants.DB_DEFAULT_MAX_LENGTH;

@Entity
@Audited
public class FerienbetreuungAngabenAngebot extends AbstractEntity {

	private static final long serialVersionUID = -5754760882297428231L;

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
	private BigDecimal anzahlFerienwochenSportferien;

	@Nullable
	@Column()
	private BigDecimal anzahlFerienwochenSommerferien;

	@Nullable
	@Column()
	private BigDecimal anzahlTage;

	@Size(max = Constants.DB_TEXTAREA_LENGTH)
	@Nullable
	@Column(length = Constants.DB_TEXTAREA_LENGTH)
	private String bemerkungenAnzahlFerienwochen;

	@Nullable
	@Column()
	private BigDecimal anzahlStundenProBetreuungstag;

	@Nullable
	@Column()
	private Boolean betreuungErfolgtTagsueber;

	@Size(max = Constants.DB_TEXTAREA_LENGTH)
	@Nullable
	@Column(length = Constants.DB_TEXTAREA_LENGTH)
	private String bemerkungenOeffnungszeiten;

	@Nonnull
	@Column(nullable = false)
	@Enumerated(EnumType.STRING)
	private FerienbetreuungFormularStatus status = FerienbetreuungFormularStatus.IN_BEARBEITUNG_GEMEINDE;;

	@ElementCollection(fetch = FetchType.EAGER)
	@CollectionTable(
		name = "ferienbetreuung_finanziell_beteiligte_gemeinden",
		joinColumns = @JoinColumn(name = "ferienbetreuung_angebot_id")
	)
	@Column(nullable = false)
	@Nonnull
	private Set<String> finanziellBeteiligteGemeinden = new TreeSet<>();

	@Nullable
	@Column()
	private Boolean gemeindeFuehrtAngebotSelber;

	@Nullable
	@Column()
	private Boolean gemeindeFuehrtAngebotInKooperation;

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
	private Boolean betreuungDurchPersonenMitErfahrung;

	@Nullable
	@Column()
	private Boolean anzahlKinderAngemessen;

	@Nullable
	@Column()
	private String betreuungsschluessel;

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
	@Enumerated(EnumType.STRING)
	private KinderAusAnderenGemeindenZahlenAnderenTarifAnswer kinderAusAnderenGemeindenZahlenAnderenTarif;

	@Size(max = Constants.DB_TEXTAREA_LENGTH)
	@Nullable
	@Column(length = Constants.DB_TEXTAREA_LENGTH)
	private String bemerkungenTarifsystem;

	public FerienbetreuungAngabenAngebot() {}

	public FerienbetreuungAngabenAngebot(FerienbetreuungAngabenAngebot toCopy) {
		this.angebot = toCopy.angebot;
		this.angebotKontaktpersonVorname = toCopy.angebotKontaktpersonVorname;
		this.angebotKontaktpersonNachname = toCopy.angebotKontaktpersonNachname;
		if(toCopy.angebotAdresse != null) {
			this.angebotAdresse = toCopy.angebotAdresse.copyAdresse(new Adresse(), AntragCopyType.MUTATION);
		}

		this.anzahlFerienwochenFruehlingsferien = toCopy.anzahlFerienwochenFruehlingsferien;
		this.anzahlFerienwochenSommerferien = toCopy.anzahlFerienwochenSommerferien;
		this.anzahlFerienwochenSportferien = toCopy.anzahlFerienwochenSportferien;
		this.anzahlFerienwochenHerbstferien = toCopy.anzahlFerienwochenHerbstferien;
		this.anzahlFerienwochenWinterferien = toCopy.anzahlFerienwochenWinterferien;
		this.anzahlTage = toCopy.anzahlTage;
		this.bemerkungenAnzahlFerienwochen = toCopy.bemerkungenAnzahlFerienwochen;

		this.finanziellBeteiligteGemeinden = new TreeSet<>(toCopy.finanziellBeteiligteGemeinden);

		this.gemeindeFuehrtAngebotSelber = toCopy.gemeindeFuehrtAngebotSelber;
		this.gemeindeFuehrtAngebotInKooperation = toCopy.gemeindeFuehrtAngebotInKooperation;
		this.gemeindeBeauftragtExterneAnbieter = toCopy.gemeindeBeauftragtExterneAnbieter;
		this.angebotVereineUndPrivateIntegriert = toCopy.angebotVereineUndPrivateIntegriert;
		this.bemerkungenKooperation = toCopy.bemerkungenKooperation;

		this.anzahlStundenProBetreuungstag = toCopy.anzahlStundenProBetreuungstag;
		this.betreuungErfolgtTagsueber = toCopy.betreuungErfolgtTagsueber;

		this.leitungDurchPersonMitAusbildung = toCopy.leitungDurchPersonMitAusbildung;
		this.betreuungDurchPersonenMitErfahrung = toCopy.betreuungDurchPersonenMitErfahrung;
		this.anzahlKinderAngemessen = toCopy.anzahlKinderAngemessen;
		this.betreuungsschluessel = toCopy.betreuungsschluessel;
		this.bemerkungenPersonal = toCopy.bemerkungenPersonal;

		this.fixerTarifKinderDerGemeinde = toCopy.fixerTarifKinderDerGemeinde;
		this.einkommensabhaengigerTarifKinderDerGemeinde = toCopy.einkommensabhaengigerTarifKinderDerGemeinde;
		this.tagesschuleTarifGiltFuerFerienbetreuung = toCopy.tagesschuleTarifGiltFuerFerienbetreuung;
		this.ferienbetreuungTarifWirdAusTagesschuleTarifAbgeleitet = toCopy.ferienbetreuungTarifWirdAusTagesschuleTarifAbgeleitet;
		this.kinderAusAnderenGemeindenZahlenAnderenTarif = toCopy.kinderAusAnderenGemeindenZahlenAnderenTarif;

		this.bemerkungenTarifsystem = toCopy.bemerkungenTarifsystem;
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
	public String getBemerkungenAnzahlFerienwochen() {
		return bemerkungenAnzahlFerienwochen;
	}

	public void setBemerkungenAnzahlFerienwochen(@Nullable String bemerkungenAnzahlFerienwochen) {
		this.bemerkungenAnzahlFerienwochen = bemerkungenAnzahlFerienwochen;
	}

	@Nullable
	public BigDecimal getAnzahlStundenProBetreuungstag() {
		return anzahlStundenProBetreuungstag;
	}

	public void setAnzahlStundenProBetreuungstag(@Nullable BigDecimal anzahlStundenProBetreuungstag) {
		this.anzahlStundenProBetreuungstag = anzahlStundenProBetreuungstag;
	}

	@Nullable
	public Boolean getBetreuungErfolgtTagsueber() {
		return betreuungErfolgtTagsueber;
	}

	public void setBetreuungErfolgtTagsueber(@Nullable Boolean betreuungErfolgtTagsueber) {
		this.betreuungErfolgtTagsueber = betreuungErfolgtTagsueber;
	}

	@Nullable
	public String getBemerkungenOeffnungszeiten() {
		return bemerkungenOeffnungszeiten;
	}

	public void setBemerkungenOeffnungszeiten(@Nullable String bemerkungenOeffnungszeiten) {
		this.bemerkungenOeffnungszeiten = bemerkungenOeffnungszeiten;
	}

	@Nonnull
	public Set<String> getFinanziellBeteiligteGemeinden() {
		return finanziellBeteiligteGemeinden;
	}

	public void setFinanziellBeteiligteGemeinden(@Nonnull Set<String> finanziellBeteiligteGemeinden) {
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
	public Boolean getBetreuungDurchPersonenMitErfahrung() {
		return betreuungDurchPersonenMitErfahrung;
	}

	public void setBetreuungDurchPersonenMitErfahrung(@Nullable Boolean betreuungDurchPersonenMitErfahrung) {
		this.betreuungDurchPersonenMitErfahrung = betreuungDurchPersonenMitErfahrung;
	}

	@Nullable
	public Boolean getAnzahlKinderAngemessen() {
		return anzahlKinderAngemessen;
	}

	public void setAnzahlKinderAngemessen(@Nullable Boolean anzahlKinderAngemessen) {
		this.anzahlKinderAngemessen = anzahlKinderAngemessen;
	}

	@Nullable
	public String getBetreuungsschluessel() {
		return betreuungsschluessel;
	}

	public void setBetreuungsschluessel(@Nullable String betreuungsschluessel) {
		this.betreuungsschluessel = betreuungsschluessel;
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
	public KinderAusAnderenGemeindenZahlenAnderenTarifAnswer getKinderAusAnderenGemeindenZahlenAnderenTarif() {
		return kinderAusAnderenGemeindenZahlenAnderenTarif;
	}

	public void setKinderAusAnderenGemeindenZahlenAnderenTarif(
		@Nullable KinderAusAnderenGemeindenZahlenAnderenTarifAnswer kinderAusAnderenGemeindenZahlenAnderenTarif
	) {
		this.kinderAusAnderenGemeindenZahlenAnderenTarif = kinderAusAnderenGemeindenZahlenAnderenTarif;
	}

	@Nullable
	public String getBemerkungenTarifsystem() {
		return bemerkungenTarifsystem;
	}

	public void setBemerkungenTarifsystem(@Nullable String bemerkungenTarifsystem) {
		this.bemerkungenTarifsystem = bemerkungenTarifsystem;
	}

	@Override
	public boolean isSame(AbstractEntity other) {
		return getId().equals(other.getId());
	}

	public boolean isReadyForFreigeben() {
		return checkPropertiesNotNull() && status == FerienbetreuungFormularStatus.ABGESCHLOSSEN;
	}

	public boolean isReadyForAbschluss() {
		return checkPropertiesNotNull();
	}

	private boolean checkPropertiesNotNull() {
		List<Serializable> nonNullObj = Arrays.asList(
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
			this.betreuungErfolgtTagsueber,
			this.leitungDurchPersonMitAusbildung,
			this.betreuungDurchPersonenMitErfahrung,
			this.anzahlKinderAngemessen,
			this.betreuungsschluessel
		);
		return nonNullObj.stream()
			.noneMatch(Objects::isNull);
	}

	@Nonnull
	public FerienbetreuungFormularStatus getStatus() {
		return status;
	}

	public void setStatus(@Nonnull FerienbetreuungFormularStatus status) {
		this.status = status;
	}

	public boolean isAbgeschlossen() {
		return status == FerienbetreuungFormularStatus.ABGESCHLOSSEN;
	}

	@Nullable
	public BigDecimal getAnzahlFerienwochenSportferien() {
		return anzahlFerienwochenSportferien;
	}

	public void setAnzahlFerienwochenSportferien(@Nullable BigDecimal anzahlFerienwochenSportferien) {
		this.anzahlFerienwochenSportferien = anzahlFerienwochenSportferien;
	}

	@Nullable
	public Boolean getGemeindeFuehrtAngebotInKooperation() {
		return gemeindeFuehrtAngebotInKooperation;
	}

	public void setGemeindeFuehrtAngebotInKooperation(@Nullable Boolean gemeindeFuehrtAngebotInKooperation) {
		this.gemeindeFuehrtAngebotInKooperation = gemeindeFuehrtAngebotInKooperation;
	}
}
