/*
 * Copyright (C) 2020 DV Bern AG, Switzerland
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

package ch.dvbern.ebegu.entities;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.ForeignKey;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.Transient;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import ch.dvbern.ebegu.enums.RueckforderungInstitutionTyp;
import ch.dvbern.ebegu.enums.RueckforderungStatus;
import ch.dvbern.ebegu.enums.Sprache;
import ch.dvbern.ebegu.util.MathUtil;
import org.hibernate.envers.Audited;
import org.hibernate.envers.RelationTargetAuditMode;

import static ch.dvbern.ebegu.enums.RueckforderungsConstants.einreichungsfristOeffentlichStufe2;
import static ch.dvbern.ebegu.enums.RueckforderungsConstants.einreichungsfristPrivatStufe2;

@Entity
@Audited
public class RueckforderungFormular extends AbstractEntity {

	private static final long serialVersionUID = 2265499143921501849L;

	@NotNull
	@ManyToOne(optional = false)
	@JoinColumn(foreignKey = @ForeignKey(name = "FK_rueckforderungFormular_institution_stammdaten_id"), nullable =
		false)
	private InstitutionStammdaten institutionStammdaten;

	@Nonnull
	@ManyToMany
	@JoinTable(
		joinColumns = @JoinColumn(name = "rueckforderung_formular_id", nullable = false),
		inverseJoinColumns = @JoinColumn(name = "rueckforderung_mitteilung_id", nullable = false),
		foreignKey = @ForeignKey(name = "FK_rueckforderung_formular_mitteilung_id"),
		inverseForeignKey = @ForeignKey(name = "FK_rueckforderung_formular_rueckforderung_mitteilung_formular_id"),
		indexes = {
			@Index(name = "FK_rueckforderung_formular_mitteilung_id", columnList = "rueckforderung_formular_id"),
			@Index(name = "IX_institution_external_clients_external_client_id", columnList =
				"rueckforderung_mitteilung_id"),
		}
	)
	@Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
	@NotNull
	private Set<RueckforderungMitteilung> rueckforderungMitteilungen = new HashSet<>();

	@NotNull
	@Column(nullable = false)
	@Nonnull
	@Enumerated(EnumType.STRING)
	private RueckforderungStatus status = RueckforderungStatus.NEU;

	@Nullable
	@ManyToOne(optional = true)
	@JoinColumn(foreignKey = @ForeignKey(name = "FK_rueckforderung_verantwortlicher_id"), nullable = true)
	private Benutzer verantwortlicher;

	@NotNull
	@Column(nullable = false)
	private boolean hasBeenProvisorisch = false; // Wird zur Anzeige der korrekten Confirmationmessage benoetigt

	@Nullable
	@Column(nullable = true)
	@Enumerated(EnumType.STRING)
	private RueckforderungInstitutionTyp institutionTyp = null;

	@Column(name = "stufe_1_kanton_kostenuebernahme_anzahl_stunden", nullable = true)
	@Nullable
	private BigDecimal stufe1KantonKostenuebernahmeAnzahlStunden;

	@Column(name = "stufe_1_institution_kostenuebernahme_anzahl_stunden", nullable = true)
	@Nullable
	private BigDecimal stufe1InstitutionKostenuebernahmeAnzahlStunden;

	@Column(name = "stufe_2_kanton_kostenuebernahme_anzahl_stunden", nullable = true)
	@Nullable
	private BigDecimal stufe2KantonKostenuebernahmeAnzahlStunden;

	@Column(name = "stufe_2_institution_kostenuebernahme_anzahl_stunden", nullable = true)
	@Nullable
	private BigDecimal stufe2InstitutionKostenuebernahmeAnzahlStunden;

	@Column(name = "stufe_1_kanton_kostenuebernahme_anzahl_tage", nullable = true)
	@Nullable
	private BigDecimal stufe1KantonKostenuebernahmeAnzahlTage;

	@Column(name = "stufe_1_institution_kostenuebernahme_anzahl_tage", nullable = true)
	@Nullable
	private BigDecimal stufe1InstitutionKostenuebernahmeAnzahlTage;

	@Column(name = "stufe_2_kanton_kostenuebernahme_anzahl_tage", nullable = true)
	@Nullable
	private BigDecimal stufe2KantonKostenuebernahmeAnzahlTage;

	@Column(name = "stufe_2_institution_kostenuebernahme_anzahl_tage", nullable = true)
	@Nullable
	private BigDecimal stufe2InstitutionKostenuebernahmeAnzahlTage;

	@Column(name = "stufe_1_kanton_kostenuebernahme_betreuung", nullable = true)
	@Nullable
	private BigDecimal stufe1KantonKostenuebernahmeBetreuung;

	@Column(name = "stufe_1_institution_kostenuebernahme_betreuung", nullable = true)
	@Nullable
	private BigDecimal stufe1InstitutionKostenuebernahmeBetreuung;

	@Column(name = "stufe_2_kanton_kostenuebernahme_betreuung", nullable = true)
	@Nullable
	private BigDecimal stufe2KantonKostenuebernahmeBetreuung;

	@Column(name = "stufe_2_institution_kostenuebernahme_betreuung", nullable = true)
	@Nullable
	private BigDecimal stufe2InstitutionKostenuebernahmeBetreuung;

	@Column(name = "stufe_1_freigabe_betrag", nullable = true)
	@Nullable
	private BigDecimal stufe1FreigabeBetrag;

	@Column(name = "stufe_1_freigabe_datum", nullable = true)
	@Nullable
	private LocalDateTime stufe1FreigabeDatum;

	@Column(name = "stufe_1_freigabe_ausbezahlt_am", nullable = true)
	@Nullable
	private LocalDateTime stufe1FreigabeAusbezahltAm;

	@Column(name = "stufe_2_voraussichtliche_betrag", nullable = true)
	@Nullable
	@Min(0)
	private BigDecimal stufe2VoraussichtlicheBetrag;

	@Column(name = "stufe_2_verfuegung_betrag", nullable = true)
	@Nullable
	@Min(0)
	private BigDecimal stufe2VerfuegungBetrag;

	@Column(name = "stufe_2_verfuegung_datum", nullable = true)
	@Nullable
	private LocalDateTime stufe2VerfuegungDatum;

	@Column(name = "stufe_2_verfuegung_ausbezahlt_am", nullable = true)
	@Nullable
	private LocalDateTime stufe2VerfuegungAusbezahltAm;

	@Column(name = "stufe_2_provisorisch_verfuegt_datum", nullable = true)
	@Nullable
	private LocalDateTime stufe2ProvisorischVerfuegtDatum;

	@Nullable
	@Column(nullable = true)
	private LocalDate extendedEinreichefrist = null; // Wenn null gilt die Default-Einreichefrist

	@Nullable
	@Column(nullable = true)
	private BigDecimal betragEntgangeneElternbeitraege;

	@Nullable
	@Column(nullable = true)
	private BigDecimal betragEntgangeneElternbeitraegeNichtAngeboteneEinheiten; // Kita in TAGE, TFO in STUNDEN

	@Nullable
	@Column(nullable = true)
	private BigDecimal anzahlNichtAngeboteneEinheiten; // Neu: Rueckerstattung fuer nicht angebotene Einheiten

	@Nullable
	@Column(nullable = true)
	private Boolean kurzarbeitBeantragt;

	@Nullable
	@Column(nullable = true)
	private BigDecimal kurzarbeitBetrag;

	@Nullable
	@Column(nullable = true)
	private Boolean kurzarbeitDefinitivVerfuegt;

	@Nullable
	@Size(min = 1, max = 2000)
	@Column(nullable = true)
	private String kurzarbeitKeinAntragBegruendung;

	@Nullable
	@Size(min = 1, max = 2000)
	@Column(nullable = true)
	private String kurzarbeitSonstiges;

	@Nullable
	@Column(nullable = true)
	private Boolean coronaErwerbsersatzBeantragt;

	@Nullable
	@Column(nullable = true)
	private BigDecimal coronaErwerbsersatzBetrag;

	@Nullable
	@Column(nullable = true)
	private Boolean coronaErwerbsersatzDefinitivVerfuegt;

	@Nullable
	@Size(min = 1, max = 2000)
	@Column(nullable = true)
	private String coronaErwerbsersatzKeinAntragBegruendung;

	@Nullable
	@Size(min = 1, max = 2000)
	@Column(nullable = true)
	private String coronaErwerbsersatzSonstiges;

	@Nonnull
	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private Sprache korrespondenzSprache = Sprache.DEUTSCH;

	@Nullable
	@Size(min=1, max=2000)
	@Column(nullable = true)
	private String bemerkungFuerVerfuegung;

	@Column
	private boolean uncheckedDocuments;

	@Nullable
	@Column(nullable = true)
	private BigDecimal beschwerdeBetrag;

	@Nullable
	@Size(max=2000)
	@Column(nullable = true)
	private String beschwerdeBemerkung;

	@Column(nullable = true)
	@Nullable
	private LocalDateTime beschwerdeAusbezahltAm;

	@Transient
	private boolean stufe1ZahlungJetztAusgeloest = false;

	@Transient
	private boolean stufe2ZahlungJetztAusgeloest = false;

	@Transient
	private boolean beschwerdeZahlungJetztAusgeloest = false;


	@Nonnull
	public Set<RueckforderungMitteilung> getRueckforderungMitteilungen() {
		return rueckforderungMitteilungen;
	}

	public void setRueckforderungMitteilungen(@Nonnull Set<RueckforderungMitteilung> rueckforderungMitteilungen) {
		this.rueckforderungMitteilungen = rueckforderungMitteilungen;
	}

	public void addRueckforderungMitteilung(@Nonnull RueckforderungMitteilung rueckforderungMitteilung) {
		this.rueckforderungMitteilungen.add(rueckforderungMitteilung);
	}

	@Nonnull
	public RueckforderungStatus getStatus() {
		return status;
	}

	public void setStatus(@Nonnull RueckforderungStatus status) {
		this.status = status;
	}

	public boolean isHasBeenProvisorisch() {
		return hasBeenProvisorisch;
	}

	public void setHasBeenProvisorisch(boolean hasBeenProvisorisch) {
		this.hasBeenProvisorisch = hasBeenProvisorisch;
	}

	@Nullable
	public RueckforderungInstitutionTyp getInstitutionTyp() {
		return institutionTyp;
	}

	public void setInstitutionTyp(@Nullable RueckforderungInstitutionTyp institutionTyp) {
		this.institutionTyp = institutionTyp;
	}

	public InstitutionStammdaten getInstitutionStammdaten() {
		return institutionStammdaten;
	}

	public void setInstitutionStammdaten(InstitutionStammdaten institutionStammdaten) {
		this.institutionStammdaten = institutionStammdaten;
	}

	@Nullable
	public Benutzer getVerantwortlicher() {
		return verantwortlicher;
	}

	public void setVerantwortlicher(@Nullable Benutzer verantwortlicher) {
		this.verantwortlicher = verantwortlicher;
	}

	@Nullable
	public BigDecimal getStufe1KantonKostenuebernahmeAnzahlStunden() {
		return stufe1KantonKostenuebernahmeAnzahlStunden;
	}

	public void setStufe1KantonKostenuebernahmeAnzahlStunden(@Nullable BigDecimal stufe1KantonKostenuebernahmeAnzahlStunden) {
		this.stufe1KantonKostenuebernahmeAnzahlStunden = stufe1KantonKostenuebernahmeAnzahlStunden;
	}

	@Nullable
	public BigDecimal getStufe1InstitutionKostenuebernahmeAnzahlStunden() {
		return stufe1InstitutionKostenuebernahmeAnzahlStunden;
	}

	public void setStufe1InstitutionKostenuebernahmeAnzahlStunden(@Nullable BigDecimal stufe1InstitutionKostenuebernahmeAnzahlStunden) {
		this.stufe1InstitutionKostenuebernahmeAnzahlStunden = stufe1InstitutionKostenuebernahmeAnzahlStunden;
	}

	@Nullable
	public BigDecimal getStufe2KantonKostenuebernahmeAnzahlStunden() {
		return stufe2KantonKostenuebernahmeAnzahlStunden;
	}

	public void setStufe2KantonKostenuebernahmeAnzahlStunden(@Nullable BigDecimal stufe2KantonKostenuebernahmeAnzahlStunden) {
		this.stufe2KantonKostenuebernahmeAnzahlStunden = stufe2KantonKostenuebernahmeAnzahlStunden;
	}

	@Nullable
	public BigDecimal getStufe2InstitutionKostenuebernahmeAnzahlStunden() {
		return stufe2InstitutionKostenuebernahmeAnzahlStunden;
	}

	public void setStufe2InstitutionKostenuebernahmeAnzahlStunden(@Nullable BigDecimal stufe2InstitutionKostenuebernahmeAnzahlStunden) {
		this.stufe2InstitutionKostenuebernahmeAnzahlStunden = stufe2InstitutionKostenuebernahmeAnzahlStunden;
	}

	@Nullable
	public BigDecimal getStufe1KantonKostenuebernahmeAnzahlTage() {
		return stufe1KantonKostenuebernahmeAnzahlTage;
	}

	public void setStufe1KantonKostenuebernahmeAnzahlTage(@Nullable BigDecimal stufe1KantonKostenuebernahmeAnzahlTage) {
		this.stufe1KantonKostenuebernahmeAnzahlTage = stufe1KantonKostenuebernahmeAnzahlTage;
	}

	@Nullable
	public BigDecimal getStufe1InstitutionKostenuebernahmeAnzahlTage() {
		return stufe1InstitutionKostenuebernahmeAnzahlTage;
	}

	public void setStufe1InstitutionKostenuebernahmeAnzahlTage(@Nullable BigDecimal stufe1InstitutionKostenuebernahmeAnzahlTage) {
		this.stufe1InstitutionKostenuebernahmeAnzahlTage = stufe1InstitutionKostenuebernahmeAnzahlTage;
	}

	@Nullable
	public BigDecimal getStufe2KantonKostenuebernahmeAnzahlTage() {
		return stufe2KantonKostenuebernahmeAnzahlTage;
	}

	public void setStufe2KantonKostenuebernahmeAnzahlTage(@Nullable BigDecimal stufe2KantonKostenuebernahmeAnzahlTage) {
		this.stufe2KantonKostenuebernahmeAnzahlTage = stufe2KantonKostenuebernahmeAnzahlTage;
	}

	@Nullable
	public BigDecimal getStufe2InstitutionKostenuebernahmeAnzahlTage() {
		return stufe2InstitutionKostenuebernahmeAnzahlTage;
	}

	public void setStufe2InstitutionKostenuebernahmeAnzahlTage(@Nullable BigDecimal stufe2InstitutionKostenuebernahmeAnzahlTage) {
		this.stufe2InstitutionKostenuebernahmeAnzahlTage = stufe2InstitutionKostenuebernahmeAnzahlTage;
	}

	@Nullable
	public BigDecimal getStufe1KantonKostenuebernahmeBetreuung() {
		return stufe1KantonKostenuebernahmeBetreuung;
	}

	public void setStufe1KantonKostenuebernahmeBetreuung(@Nullable BigDecimal stufe1KantonKostenuebernahmeBetreuung) {
		this.stufe1KantonKostenuebernahmeBetreuung = stufe1KantonKostenuebernahmeBetreuung;
	}

	@Nullable
	public BigDecimal getStufe1InstitutionKostenuebernahmeBetreuung() {
		return stufe1InstitutionKostenuebernahmeBetreuung;
	}

	public void setStufe1InstitutionKostenuebernahmeBetreuung(@Nullable BigDecimal stufe1InstitutionKostenuebernahmeBetreuung) {
		this.stufe1InstitutionKostenuebernahmeBetreuung = stufe1InstitutionKostenuebernahmeBetreuung;
	}

	@Nullable
	public BigDecimal getStufe2KantonKostenuebernahmeBetreuung() {
		return stufe2KantonKostenuebernahmeBetreuung;
	}

	public void setStufe2KantonKostenuebernahmeBetreuung(@Nullable BigDecimal stufe2KantonKostenuebernahmeBetreuung) {
		this.stufe2KantonKostenuebernahmeBetreuung = stufe2KantonKostenuebernahmeBetreuung;
	}

	@Nullable
	public BigDecimal getStufe2InstitutionKostenuebernahmeBetreuung() {
		return stufe2InstitutionKostenuebernahmeBetreuung;
	}

	public void setStufe2InstitutionKostenuebernahmeBetreuung(@Nullable BigDecimal stufe2InstitutionKostenuebernahmeBetreuung) {
		this.stufe2InstitutionKostenuebernahmeBetreuung = stufe2InstitutionKostenuebernahmeBetreuung;
	}

	@Nullable
	public BigDecimal getStufe1FreigabeBetrag() {
		return stufe1FreigabeBetrag;
	}

	public void setStufe1FreigabeBetrag(@Nullable BigDecimal stufe1FreigabeBetrag) {
		this.stufe1FreigabeBetrag = stufe1FreigabeBetrag;
	}

	@Nullable
	public LocalDateTime getStufe1FreigabeDatum() {
		return stufe1FreigabeDatum;
	}

	public void setStufe1FreigabeDatum(@Nullable LocalDateTime stufe1FreigabeDatum) {
		this.stufe1FreigabeDatum = stufe1FreigabeDatum;
	}

	@Nullable
	public LocalDateTime getStufe1FreigabeAusbezahltAm() {
		return stufe1FreigabeAusbezahltAm;
	}

	public void setStufe1FreigabeAusbezahltAm(@Nullable LocalDateTime stufe1FreigabeAusbezahltAm) {
		this.stufe1FreigabeAusbezahltAm = stufe1FreigabeAusbezahltAm;
	}

	@Nullable
	public BigDecimal getStufe2VerfuegungBetrag() {
		return stufe2VerfuegungBetrag;
	}

	public void setStufe2VerfuegungBetrag(@Nullable BigDecimal stufe2VerfuegungBetrag) {
		this.stufe2VerfuegungBetrag = stufe2VerfuegungBetrag;
	}

	@Nullable
	public LocalDateTime getStufe2VerfuegungDatum() {
		return stufe2VerfuegungDatum;
	}

	public void setStufe2VerfuegungDatum(@Nullable LocalDateTime stufe2VerfuegungDatum) {
		this.stufe2VerfuegungDatum = stufe2VerfuegungDatum;
	}

	@Nullable
	public LocalDateTime getStufe2VerfuegungAusbezahltAm() {
		return stufe2VerfuegungAusbezahltAm;
	}

	public void setStufe2VerfuegungAusbezahltAm(@Nullable LocalDateTime stufe2VerfuegungAusbezahltAm) {
		this.stufe2VerfuegungAusbezahltAm = stufe2VerfuegungAusbezahltAm;
	}

	@Nullable
	public LocalDateTime getStufe2ProvisorischVerfuegtDatum() {
		return stufe2ProvisorischVerfuegtDatum;
	}

	public void setStufe2ProvisorischVerfuegtDatum(@Nullable LocalDateTime stufe2ProvisorischVerfuegtDatum) {
		this.stufe2ProvisorischVerfuegtDatum = stufe2ProvisorischVerfuegtDatum;
	}

	@Nullable
	public LocalDate getExtendedEinreichefrist() {
		return extendedEinreichefrist;
	}

	public void setExtendedEinreichefrist(@Nullable LocalDate extendedEinreichefrist) {
		this.extendedEinreichefrist = extendedEinreichefrist;
	}

	public @Nullable BigDecimal getBetragEntgangeneElternbeitraege() {
		return betragEntgangeneElternbeitraege;
	}

	public void setBetragEntgangeneElternbeitraege(@Nullable BigDecimal betragEntgangeneElternbeitraege) {
		this.betragEntgangeneElternbeitraege = betragEntgangeneElternbeitraege;
	}

	public @Nullable BigDecimal getBetragEntgangeneElternbeitraegeNichtAngeboteneEinheiten() {
		return betragEntgangeneElternbeitraegeNichtAngeboteneEinheiten;
	}

	public void setBetragEntgangeneElternbeitraegeNichtAngeboteneEinheiten(@Nullable BigDecimal betragEntgangeneElternbeitraegeNichtAngeboteneEinheiten) {
		this.betragEntgangeneElternbeitraegeNichtAngeboteneEinheiten = betragEntgangeneElternbeitraegeNichtAngeboteneEinheiten;
	}

	public @Nullable BigDecimal getAnzahlNichtAngeboteneEinheiten() {
		return anzahlNichtAngeboteneEinheiten;
	}

	public void setAnzahlNichtAngeboteneEinheiten(@Nullable BigDecimal anzahlNichtAngeboteneEinheiten) {
		this.anzahlNichtAngeboteneEinheiten = anzahlNichtAngeboteneEinheiten;
	}

	public @Nullable Boolean getKurzarbeitBeantragt() {
		return kurzarbeitBeantragt;
	}

	public void setKurzarbeitBeantragt(@Nullable Boolean kurzarbeitBeantragt) {
		this.kurzarbeitBeantragt = kurzarbeitBeantragt;
	}

	public @Nullable BigDecimal getKurzarbeitBetrag() {
		return kurzarbeitBetrag;
	}

	public void setKurzarbeitBetrag(@Nullable BigDecimal kurzarbeitBetrag) {
		this.kurzarbeitBetrag = kurzarbeitBetrag;
	}

	public @Nullable Boolean getKurzarbeitDefinitivVerfuegt() {
		return kurzarbeitDefinitivVerfuegt;
	}

	public void setKurzarbeitDefinitivVerfuegt(@Nullable Boolean kurzarbeitDefinitivVerfuegt) {
		this.kurzarbeitDefinitivVerfuegt = kurzarbeitDefinitivVerfuegt;
	}

	public @Nullable String getKurzarbeitKeinAntragBegruendung() {
		return kurzarbeitKeinAntragBegruendung;
	}

	public void setKurzarbeitKeinAntragBegruendung(@Nullable String kurzarbeitKeinAntragBegruendung) {
		this.kurzarbeitKeinAntragBegruendung = kurzarbeitKeinAntragBegruendung;
	}

	public @Nullable String getKurzarbeitSonstiges() {
		return kurzarbeitSonstiges;
	}

	public void setKurzarbeitSonstiges(@Nullable String kurzarbeitSonstiges) {
		this.kurzarbeitSonstiges = kurzarbeitSonstiges;
	}

	public @Nullable Boolean getCoronaErwerbsersatzBeantragt() {
		return coronaErwerbsersatzBeantragt;
	}

	public void setCoronaErwerbsersatzBeantragt(@Nullable Boolean coronaErwerbsersatzBeantragt) {
		this.coronaErwerbsersatzBeantragt = coronaErwerbsersatzBeantragt;
	}

	public @Nullable BigDecimal getCoronaErwerbsersatzBetrag() {
		return coronaErwerbsersatzBetrag;
	}

	public void setCoronaErwerbsersatzBetrag(@Nullable BigDecimal coronaErwerbsersatzBetrag) {
		this.coronaErwerbsersatzBetrag = coronaErwerbsersatzBetrag;
	}

	public @Nullable Boolean getCoronaErwerbsersatzDefinitivVerfuegt() {
		return coronaErwerbsersatzDefinitivVerfuegt;
	}

	public void setCoronaErwerbsersatzDefinitivVerfuegt(@Nullable Boolean coronaErwerbsersatzDefinitivVerfuegt) {
		this.coronaErwerbsersatzDefinitivVerfuegt = coronaErwerbsersatzDefinitivVerfuegt;
	}

	public @Nullable String getCoronaErwerbsersatzKeinAntragBegruendung() {
		return coronaErwerbsersatzKeinAntragBegruendung;
	}

	public void setCoronaErwerbsersatzKeinAntragBegruendung(@Nullable String coronaErwerbsersatzKeinAntragBegruendung) {
		this.coronaErwerbsersatzKeinAntragBegruendung = coronaErwerbsersatzKeinAntragBegruendung;
	}

	public @Nullable String getCoronaErwerbsersatzSonstiges() {
		return coronaErwerbsersatzSonstiges;
	}

	public void setCoronaErwerbsersatzSonstiges(@Nullable String coronaErwerbsersatzSonstiges) {
		this.coronaErwerbsersatzSonstiges = coronaErwerbsersatzSonstiges;
	}

	@Nullable
	public String getBemerkungFuerVerfuegung() {
		return bemerkungFuerVerfuegung;
	}

	public void setBemerkungFuerVerfuegung(@Nullable String bemerkungFuerVerfuegung) {
		this.bemerkungFuerVerfuegung = bemerkungFuerVerfuegung;
	}

	public boolean isStufe1ZahlungJetztAusgeloest() {
		return stufe1ZahlungJetztAusgeloest;
	}

	public void setStufe1ZahlungJetztAusgeloest(boolean stufe1ZahlungJetztAusgeloest) {
		this.stufe1ZahlungJetztAusgeloest = stufe1ZahlungJetztAusgeloest;
	}

	public boolean isStufe2ZahlungJetztAusgeloest() {
		return stufe2ZahlungJetztAusgeloest;
	}

	public void setStufe2ZahlungJetztAusgeloest(boolean stufe2ZahlungJetztAusgeloest) {
		this.stufe2ZahlungJetztAusgeloest = stufe2ZahlungJetztAusgeloest;
	}

	@Nullable
	public BigDecimal getStufe2VoraussichtlicheBetrag() {
		return stufe2VoraussichtlicheBetrag;
	}

	public void setStufe2VoraussichtlicheBetrag(@Nullable BigDecimal stufe2VoraussichtlicheBetrag) {
		this.stufe2VoraussichtlicheBetrag = stufe2VoraussichtlicheBetrag;
	}

	@Nonnull
	public Sprache getKorrespondenzSprache() {
		return korrespondenzSprache;
	}

	public void setKorrespondenzSprache(@Nonnull Sprache korrespondenzSprache) {
		this.korrespondenzSprache = korrespondenzSprache;
	}

	public boolean hasUncheckedDocuments() {
		return uncheckedDocuments;
	}

	public void setUncheckedDocuments(boolean uncheckedDocuments) {
		this.uncheckedDocuments = uncheckedDocuments;
	}

	@Nullable
	public BigDecimal getBeschwerdeBetrag() {
		return beschwerdeBetrag;
	}

	public void setBeschwerdeBetrag(@Nullable BigDecimal beschwerdeBetrag) {
		this.beschwerdeBetrag = beschwerdeBetrag;
	}

	@Nullable
	public String getBeschwerdeBemerkung() {
		return beschwerdeBemerkung;
	}

	public void setBeschwerdeBemerkung(@Nullable String beschwerdeBemerkung) {
		this.beschwerdeBemerkung = beschwerdeBemerkung;
	}

	@Nullable
	public LocalDateTime getBeschwerdeAusbezahltAm() {
		return beschwerdeAusbezahltAm;
	}

	public void setBeschwerdeAusbezahltAm(@Nullable LocalDateTime beschwerdeAusbezahltAm) {
		this.beschwerdeAusbezahltAm = beschwerdeAusbezahltAm;
	}

	public boolean isBeschwerdeZahlungJetztAusgeloest() {
		return beschwerdeZahlungJetztAusgeloest;
	}

	public void setBeschwerdeZahlungJetztAusgeloest(boolean beschwerdeZahlungJetztAusgeloest) {
		this.beschwerdeZahlungJetztAusgeloest = beschwerdeZahlungJetztAusgeloest;
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
		if (!(other instanceof RueckforderungFormular)) {
			return false;
		}
		final RueckforderungFormular otherRueckforderungFormular = (RueckforderungFormular) other;
		return this.status == otherRueckforderungFormular.getStatus() &&
			this.getInstitutionStammdaten().getId().equals(otherRueckforderungFormular.getInstitutionStammdaten().getId());
	}

	private boolean isAuszuzahlenStufe1() {
		return RueckforderungStatus.GEPRUEFT_STUFE_1.ordinal() <=  status.ordinal() && stufe1FreigabeAusbezahltAm == null
			&& status != RueckforderungStatus.ABGESCHLOSSEN_OHNE_GESUCH;
	}

	private boolean isAuszuzahlenStufe2() {
		return RueckforderungStatus.VERFUEGT == status && stufe2VerfuegungAusbezahltAm == null;
	}

	// nur ausbezahlen, falls es eine Beschwerde gibt
	private boolean isAuszuzahlenBeschwerde() {
		return RueckforderungStatus.VERFUEGT == status && beschwerdeAusbezahltAm == null && beschwerdeBetrag != null;
	}

	public void handleAuszahlungIfNecessary() {
		if (isAuszuzahlenStufe1()) {
			this.stufe1FreigabeAusbezahltAm = LocalDateTime.now();
			this.stufe1ZahlungJetztAusgeloest = true;
		}
		if (isAuszuzahlenStufe2()) {
			this.stufe2VerfuegungAusbezahltAm = LocalDateTime.now();
			this.stufe2ZahlungJetztAusgeloest = true;
		}
		if (isAuszuzahlenBeschwerde()) {
			this.beschwerdeAusbezahltAm = LocalDateTime.now();
			this.beschwerdeZahlungJetztAusgeloest = true;
		}
	}

	private boolean isPrivateInstitution() {
		return RueckforderungInstitutionTyp.PRIVAT == getInstitutionTyp();
	}

	/**
	 * Berechnet den Betrag fuer die Freigabe Stufe 1
	 */
	@Nonnull
	public BigDecimal calculateFreigabeBetragStufe1() {
		BigDecimal freigabeBetrag;
		if (getInstitutionStammdaten().getBetreuungsangebotTyp().isKita()) {
			Objects.requireNonNull(getStufe1KantonKostenuebernahmeAnzahlTage());
			freigabeBetrag = getStufe1KantonKostenuebernahmeAnzahlTage();
		} else {
			Objects.requireNonNull(getStufe1KantonKostenuebernahmeAnzahlStunden());
			freigabeBetrag = getStufe1KantonKostenuebernahmeAnzahlStunden();
		}
		Objects.requireNonNull(getStufe1KantonKostenuebernahmeBetreuung());
		return MathUtil.DEFAULT.add(freigabeBetrag, getStufe1KantonKostenuebernahmeBetreuung());
	}

	/**
	 * Berechnet den Betrag fuer die Freigabe Stufe 2, je nachdem ob es eine
	 * private oder eine oeffentliche Institution ist.
	 */
	@Nonnull
	public BigDecimal calculateFreigabeBetragStufe2() {
		Objects.requireNonNull(getInstitutionTyp());

		// (1) Oeffentlich
		if (!isPrivateInstitution()) {
			BigDecimal freigabeBetrag;
			if (getInstitutionStammdaten().getBetreuungsangebotTyp().isKita()) {
				Objects.requireNonNull(getStufe2KantonKostenuebernahmeAnzahlTage());
				freigabeBetrag = getStufe2KantonKostenuebernahmeAnzahlTage();
			} else {
				Objects.requireNonNull(getStufe2KantonKostenuebernahmeAnzahlStunden());
				freigabeBetrag = getStufe2KantonKostenuebernahmeAnzahlStunden();
			}
			Objects.requireNonNull(getStufe2KantonKostenuebernahmeBetreuung());
			BigDecimal result = MathUtil.DEFAULT.add(freigabeBetrag, getStufe2KantonKostenuebernahmeBetreuung());
			result = MathUtil.minimum(result, BigDecimal.ZERO);
			return MathUtil.roundToFrankenRappen(result);
		}

		// (2) Privat
		Objects.requireNonNull(getBetragEntgangeneElternbeitraege());
		Objects.requireNonNull(getKurzarbeitBeantragt());
		Objects.requireNonNull(getCoronaErwerbsersatzBeantragt());

		// (2.1) Privat mit Kurzarbeit
		final BigDecimal coronaErwerbsersatzBetragToConsider
			= getCoronaErwerbsersatzBeantragt()
			? getCoronaErwerbsersatzBetrag()
			: BigDecimal.ZERO;
		if (getKurzarbeitBeantragt()) {
			// EntgangeBeitraege - bereits erhaltene Kurzarbeit - evtl. bereits erhaltene Corona Erwerbsersatz
			Objects.requireNonNull(getBetragEntgangeneElternbeitraege());
			Objects.requireNonNull(getKurzarbeitBetrag());
			BigDecimal result = MathUtil.DEFAULT.subtractMultiple(
				getBetragEntgangeneElternbeitraege(),
				getKurzarbeitBetrag(),
				coronaErwerbsersatzBetragToConsider);
			result = MathUtil.minimum(result, BigDecimal.ZERO);
			return MathUtil.roundToFrankenRappen(result);
		}

		// (2.2) Privat, ohne Kurzarbeit, ohne nicht angebotene Tage
		if (getAnzahlNichtAngeboteneEinheiten() == null || !MathUtil.isPositive(getAnzahlNichtAngeboteneEinheiten())) {
			// Keine nicht-angebotenen Tage
			BigDecimal result = MathUtil.DEFAULT.subtractMultiple(
				getBetragEntgangeneElternbeitraege(),
				coronaErwerbsersatzBetragToConsider);
			result = MathUtil.minimum(result, BigDecimal.ZERO);
			return MathUtil.roundToFrankenRappen(result);
		}
		// (2.3) Privat, ohne Kurzarbeit, mit nicht angebotene Tage
		BigDecimal result = MathUtil.DEFAULT.subtractMultiple(
			getBetragEntgangeneElternbeitraege(),
			getBetragEntgangeneElternbeitraegeNichtAngeboteneEinheiten(),
			coronaErwerbsersatzBetragToConsider)
			.add(getAnzahlNichtAngeboteneEinheiten());
		result = MathUtil.minimum(result, BigDecimal.ZERO);
		return MathUtil.roundToFrankenRappen(result);
	}

	/**
	 * Gibt das Frist-Datum zuruck, das tatsaechlich verwendet werden soll, also entweder das
	 * ueberschrieben oder den Default
	 */
	@Nonnull
	public LocalDate getRelevantEinreichungsfrist() {
		if (isPrivateInstitution() && getExtendedEinreichefrist() != null) {
			return getExtendedEinreichefrist();
		}
		return isPrivateInstitution() ? einreichungsfristPrivatStufe2 : einreichungsfristOeffentlichStufe2;
	}

	/**
	 * Der Prozess ist beendet, wenn entweder gar keine Kurzarbeit beantragt wurde, oder diese bereits definitiv verfuegt ist.
	 */
	public boolean isKurzarbeitProzessBeendet() {
		return getKurzarbeitBeantragt() == null
			|| !getKurzarbeitBeantragt()
			|| (getKurzarbeitBeantragt() && getKurzarbeitDefinitivVerfuegt() != null && getKurzarbeitDefinitivVerfuegt());
	}

	/**
	 * Der Prozess ist beendet, wenn entweder gar kein CoronaErwerbsersatz beantragt wurde, oder dieser bereits definitiv verfuegt ist.
	 */
	public boolean isCoronaErwerbsersatzProzessBeendet() {
		return getCoronaErwerbsersatzBeantragt() == null
			|| !getCoronaErwerbsersatzBeantragt()
			|| (getCoronaErwerbsersatzBeantragt() && getCoronaErwerbsersatzDefinitivVerfuegt() != null && getCoronaErwerbsersatzDefinitivVerfuegt());
	}
}
