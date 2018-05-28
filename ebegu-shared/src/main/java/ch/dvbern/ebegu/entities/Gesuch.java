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
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.ForeignKey;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.OrderBy;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;
import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import ch.dvbern.ebegu.dto.FinanzDatenDTO;
import ch.dvbern.ebegu.dto.suchfilter.lucene.EBEGUGermanAnalyzer;
import ch.dvbern.ebegu.dto.suchfilter.lucene.Searchable;
import ch.dvbern.ebegu.enums.AntragStatus;
import ch.dvbern.ebegu.enums.AntragTyp;
import ch.dvbern.ebegu.enums.Betreuungsstatus;
import ch.dvbern.ebegu.enums.Eingangsart;
import ch.dvbern.ebegu.enums.FinSitStatus;
import ch.dvbern.ebegu.enums.GesuchBetreuungenStatus;
import ch.dvbern.ebegu.util.Constants;
import ch.dvbern.ebegu.validationgroups.AntragCompleteValidationGroup;
import ch.dvbern.ebegu.validators.CheckGesuchComplete;
import org.hibernate.envers.Audited;
import org.hibernate.search.annotations.Analyzer;
import org.hibernate.search.annotations.Indexed;
import org.hibernate.search.annotations.IndexedEmbedded;

/**
 * Entitaet zum Speichern von Gesuch in der Datenbank.
 */
@Audited
@CheckGesuchComplete(groups = AntragCompleteValidationGroup.class)
@Entity
@Indexed
@Analyzer(impl = EBEGUGermanAnalyzer.class)
@EntityListeners({ GesuchStatusListener.class , GesuchGueltigListener.class})
@Table(
	uniqueConstraints = @UniqueConstraint(columnNames = { "fall_id", "gesuchsperiode_id", "gueltig" }, name = "UK_gueltiges_gesuch"),
	indexes = @Index(name = "IX_gesuch_timestamp_erstellt", columnList = "timestampErstellt")
)
public class Gesuch extends AbstractEntity implements Searchable {

	private static final long serialVersionUID = -8403487439884700618L;

	@NotNull
	@ManyToOne(optional = false)
	@JoinColumn(foreignKey = @ForeignKey(name = "FK_gesuch_fall_id"))
	@IndexedEmbedded
	private Fall fall;

	@NotNull
	@ManyToOne(optional = false)
	@JoinColumn(foreignKey = @ForeignKey(name = "FK_antrag_gesuchsperiode_id"))
	private Gesuchsperiode gesuchsperiode;

	@Column(nullable = true)
	private LocalDate eingangsdatum;

	@Column(nullable = true)
	private LocalDate freigabeDatum;

	@Column(nullable = true)
	private LocalDate eingangsdatumSTV;

	@NotNull
	@Column(nullable = false)
	@Enumerated(EnumType.STRING)
	private AntragStatus status;

	@NotNull
	@Column(nullable = false)
	private Boolean dokumenteHochgeladen = false;

	@NotNull
	@Column(nullable = false)
	@Enumerated(EnumType.STRING)
	private AntragTyp typ = AntragTyp.ERSTGESUCH;

	@NotNull
	@Column(nullable = false)
	@Enumerated(EnumType.STRING)
	private Eingangsart eingangsart = Eingangsart.PAPIER;

	@NotNull
	@Column(nullable = false)
	@Enumerated(EnumType.STRING)
	private GesuchBetreuungenStatus gesuchBetreuungenStatus = GesuchBetreuungenStatus.ALLE_BESTAETIGT;

	@Valid
	@Nullable
	@OneToOne(optional = true, cascade = CascadeType.ALL, orphanRemoval = true)
	@JoinColumn(foreignKey = @ForeignKey(name = "FK_gesuch_gesuchsteller_container1_id"), nullable = true)
	private GesuchstellerContainer gesuchsteller1;

	@Valid
	@Nullable
	@OneToOne(optional = true, cascade = CascadeType.ALL, orphanRemoval = true)
	@JoinColumn(foreignKey = @ForeignKey(name = "FK_gesuch_gesuchsteller_container2_id"), nullable = true)
	private GesuchstellerContainer gesuchsteller2;

	@Valid
	@OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, mappedBy = "gesuch")
	@OrderBy("kindNummer")
	private Set<KindContainer> kindContainers = new LinkedHashSet<>();

	@OneToMany(cascade = CascadeType.REMOVE, orphanRemoval = true, mappedBy = "gesuch", fetch = FetchType.LAZY)
	@OrderBy("timestampVon")
	private Set<AntragStatusHistory> antragStatusHistories = new LinkedHashSet<>();

	@Valid
	@Nullable
	@OneToOne(cascade = CascadeType.ALL, orphanRemoval = true, optional = true)
	@JoinColumn(foreignKey = @ForeignKey(name = "FK_gesuch_familiensituation_container_id"))
	private FamiliensituationContainer familiensituationContainer;

	@Valid
	@Nullable
	@OneToOne(cascade = CascadeType.ALL, orphanRemoval = true, optional = true)
	@JoinColumn(foreignKey = @ForeignKey(name = "FK_gesuch_einkommensverschlechterungInfoContainer_id"))
	private EinkommensverschlechterungInfoContainer einkommensverschlechterungInfoContainer;

	@Transient
	private FinanzDatenDTO finanzDatenDTO_alleine;

	@Transient
	private FinanzDatenDTO finanzDatenDTO_zuZweit;

	@Transient
	private AntragStatus preStatus;

	@Size(max = Constants.DB_TEXTAREA_LENGTH)
	@Nullable
	@Column(nullable = true, length = Constants.DB_TEXTAREA_LENGTH)
	private String bemerkungen;

	// Hier werden die Bemerkungen gespeichert, die das JA fuer die STV eintraegt
	@Size(max = Constants.DB_TEXTAREA_LENGTH)
	@Nullable
	@Column(nullable = true, length = Constants.DB_TEXTAREA_LENGTH)
	private String bemerkungenSTV;

	// Hier werden die Bemerkungen gespeichert, die die STV eingibt
	@Size(max = Constants.DB_TEXTAREA_LENGTH)
	@Nullable
	@Column(nullable = true, length = Constants.DB_TEXTAREA_LENGTH)
	private String bemerkungenPruefungSTV;

	@Nullable
	@Valid
	@OneToMany(cascade = CascadeType.PERSIST, mappedBy = "gesuch")
	private Set<DokumentGrund> dokumentGrunds;

	@NotNull
	@Min(0)
	@Column(nullable = false)
	private int laufnummer = 0;

	@Column(nullable = false)
	private boolean geprueftSTV = false;

	@Column(nullable = false)
	private boolean hasFSDokument = true;

	@Enumerated(EnumType.STRING)
	@Column(nullable = true)
	@Nullable
	private FinSitStatus finSitStatus;

	@Column(nullable = false)
	private boolean gesperrtWegenBeschwerde = false;

	@Column(nullable = true)
	private LocalDate datumGewarntNichtFreigegeben;

	@Column(nullable = true)
	private LocalDate datumGewarntFehlendeQuittung;

	@Column(nullable = true)
	private LocalDateTime timestampVerfuegt;

	// Es muss nullable sein koennen, damit man ein UNIQUE_KEY machen kann
	@Column(nullable = true)
	private Boolean gueltig = null;

	public Gesuch() {
	}

	@Nullable
	public GesuchstellerContainer getGesuchsteller1() {
		return gesuchsteller1;
	}

	public void setGesuchsteller1(@Nullable GesuchstellerContainer gesuchsteller1) {
		this.gesuchsteller1 = gesuchsteller1;
	}

	@Nullable
	public GesuchstellerContainer getGesuchsteller2() {
		return gesuchsteller2;
	}

	public void setGesuchsteller2(@Nullable GesuchstellerContainer gesuchsteller2) {
		this.gesuchsteller2 = gesuchsteller2;
	}

	public Set<KindContainer> getKindContainers() {
		return kindContainers;
	}

	public void setKindContainers(final Set<KindContainer> kindContainers) {
		this.kindContainers = kindContainers;
	}

	@Nullable
	public FamiliensituationContainer getFamiliensituationContainer() {
		return familiensituationContainer;
	}

	public void setFamiliensituationContainer(@Nullable FamiliensituationContainer familiensituationContainer) {
		this.familiensituationContainer = familiensituationContainer;
	}

	public Set<AntragStatusHistory> getAntragStatusHistories() {
		return antragStatusHistories;
	}

	public void setAntragStatusHistories(Set<AntragStatusHistory> antragStatusHistories) {
		this.antragStatusHistories = antragStatusHistories;
	}

	@Nullable
	public EinkommensverschlechterungInfo extractEinkommensverschlechterungInfo() {
		if (einkommensverschlechterungInfoContainer != null) {
			return einkommensverschlechterungInfoContainer.getEinkommensverschlechterungInfoJA();
		}
		return null;
	}

	public boolean addKindContainer(@NotNull final KindContainer kindContainer) {
		kindContainer.setGesuch(this);
		return this.kindContainers.add(kindContainer);
	}

	public boolean addDokumentGrund(@NotNull final DokumentGrund dokumentGrund) {
		dokumentGrund.setGesuch(this);

		if (this.dokumentGrunds == null) {
			this.dokumentGrunds = new HashSet<>();
		}

		return this.dokumentGrunds.add(dokumentGrund);
	}

	public FinanzDatenDTO getFinanzDatenDTO() {
		if (extractFamiliensituation() != null && extractFamiliensituation().hasSecondGesuchsteller()) {
			return finanzDatenDTO_zuZweit;
		}
		return finanzDatenDTO_alleine;
	}

	@Nullable
	public String getBemerkungen() {
		return bemerkungen;
	}

	public void setBemerkungen(@Nullable String bemerkungen) {
		this.bemerkungen = bemerkungen;
	}

	@Nullable
	public String getBemerkungenSTV() {
		return bemerkungenSTV;
	}

	public void setBemerkungenSTV(@Nullable String bemerkungenSTV) {
		this.bemerkungenSTV = bemerkungenSTV;
	}

	@Nullable
	public String getBemerkungenPruefungSTV() {
		return bemerkungenPruefungSTV;
	}

	public void setBemerkungenPruefungSTV(@Nullable String bemerkungenPruefungSTV) {
		this.bemerkungenPruefungSTV = bemerkungenPruefungSTV;
	}

	public Fall getFall() {
		return fall;
	}

	public final void setFall(Fall fall) {
		this.fall = fall;
	}

	public Gesuchsperiode getGesuchsperiode() {
		return gesuchsperiode;
	}

	public final void setGesuchsperiode(Gesuchsperiode gesuchsperiode) {
		this.gesuchsperiode = gesuchsperiode;
	}

	public LocalDate getEingangsdatum() {
		return eingangsdatum;
	}

	public final void setEingangsdatum(@Nullable LocalDate eingangsdatum) {
		this.eingangsdatum = eingangsdatum;
	}

	public LocalDate getEingangsdatumSTV() {
		return eingangsdatumSTV;
	}

	public void setEingangsdatumSTV(LocalDate eingangsdatumSTV) {
		this.eingangsdatumSTV = eingangsdatumSTV;
	}

	@Nullable
	public LocalDate getFreigabeDatum() {
		return freigabeDatum;
	}

	public void setFreigabeDatum(@Nullable LocalDate freigabeDatum) {
		this.freigabeDatum = freigabeDatum;
	}

	public AntragStatus getStatus() {
		return status;
	}

	public final void setStatus(AntragStatus status) {
		this.status = status;
	}

	public AntragTyp getTyp() {
		return typ;
	}

	public final void setTyp(AntragTyp typ) {
		this.typ = typ;
	}

	public Eingangsart getEingangsart() {
		return eingangsart;
	}

	public void setEingangsart(Eingangsart eingangsart) {
		this.eingangsart = eingangsart;
	}

	public GesuchBetreuungenStatus getGesuchBetreuungenStatus() {
		return gesuchBetreuungenStatus;
	}

	public void setGesuchBetreuungenStatus(GesuchBetreuungenStatus gesuchBetreuungenStatus) {
		this.gesuchBetreuungenStatus = gesuchBetreuungenStatus;
	}

	@Nullable
	public Set<DokumentGrund> getDokumentGrunds() {
		return dokumentGrunds;
	}

	public void setDokumentGrunds(@Nullable Set<DokumentGrund> dokumentGrunds) {
		this.dokumentGrunds = dokumentGrunds;
	}

	public int getLaufnummer() {
		return laufnummer;
	}

	public void setLaufnummer(int laufnummer) {
		this.laufnummer = laufnummer;
	}

	public boolean isGeprueftSTV() {
		return geprueftSTV;
	}

	public void setGeprueftSTV(boolean geprueftSTV) {
		this.geprueftSTV = geprueftSTV;
	}

	public boolean isHasFSDokument() {
		return hasFSDokument;
	}

	public void setHasFSDokument(boolean hasFSDokument) {
		this.hasFSDokument = hasFSDokument;
	}

	public boolean isGesperrtWegenBeschwerde() {
		return gesperrtWegenBeschwerde;
	}

	public void setGesperrtWegenBeschwerde(boolean gesperrtWegenBeschwerde) {
		this.gesperrtWegenBeschwerde = gesperrtWegenBeschwerde;
	}

	@Nullable
	public EinkommensverschlechterungInfoContainer getEinkommensverschlechterungInfoContainer() {
		return einkommensverschlechterungInfoContainer;
	}

	public void setEinkommensverschlechterungInfoContainer(@Nullable EinkommensverschlechterungInfoContainer einkommensverschlechterungInfoContainer) {
		this.einkommensverschlechterungInfoContainer = einkommensverschlechterungInfoContainer;
	}

	public FinanzDatenDTO getFinanzDatenDTO_alleine() {
		return finanzDatenDTO_alleine;
	}

	public void setFinanzDatenDTO_alleine(FinanzDatenDTO finanzDatenDTO_alleine) {
		this.finanzDatenDTO_alleine = finanzDatenDTO_alleine;
	}

	public FinanzDatenDTO getFinanzDatenDTO_zuZweit() {
		return finanzDatenDTO_zuZweit;
	}

	public void setFinanzDatenDTO_zuZweit(FinanzDatenDTO finanzDatenDTO_zuZweit) {
		this.finanzDatenDTO_zuZweit = finanzDatenDTO_zuZweit;
	}

	public LocalDate getDatumGewarntNichtFreigegeben() {
		return datumGewarntNichtFreigegeben;
	}

	public void setDatumGewarntNichtFreigegeben(@Nullable LocalDate datumGewarntNichtFreigegeben) {
		this.datumGewarntNichtFreigegeben = datumGewarntNichtFreigegeben;
	}

	public LocalDate getDatumGewarntFehlendeQuittung() {
		return datumGewarntFehlendeQuittung;
	}

	public void setDatumGewarntFehlendeQuittung(@Nullable LocalDate datumGewarntFehlendeQuittung) {
		this.datumGewarntFehlendeQuittung = datumGewarntFehlendeQuittung;
	}

	public LocalDateTime getTimestampVerfuegt() {
		return timestampVerfuegt;
	}

	public void setTimestampVerfuegt(@Nullable LocalDateTime datumVerfuegt) {
		this.timestampVerfuegt = datumVerfuegt;
	}

	@Nullable
	public Boolean getGueltig() {
		return gueltig;
	}

	public void setGueltig(@Nullable Boolean gueltig) {
		this.gueltig = gueltig;
	}

	/**
	 * @return boolean as false or true (if null return false) Use this function instead of getGueltig() for client.
	 */
	@Transient
	public boolean isGueltig() {
		return this.gueltig != null && this.gueltig;
	}

	public Boolean getDokumenteHochgeladen() {
		return dokumenteHochgeladen;
	}

	public void setDokumenteHochgeladen(Boolean dokumenteHochgeladen) {
		this.dokumenteHochgeladen = dokumenteHochgeladen;
	}

	@Nullable
	public FinSitStatus getFinSitStatus() {
		return finSitStatus;
	}

	public void setFinSitStatus(@Nullable FinSitStatus finSitStatus) {
		this.finSitStatus = finSitStatus;
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
		if (!(other instanceof Gesuch)) {
			return false;
		}
		final Gesuch otherAntrag = (Gesuch) other;
		return Objects.equals(this.getEingangsdatum(), otherAntrag.getEingangsdatum())
			&& Objects.equals(this.getFall(), otherAntrag.getFall())
			&& Objects.equals(this.getGesuchsperiode(), otherAntrag.getGesuchsperiode());
	}

	/**
	 * Gibt das Startjahr der Gesuchsperiode (zweistellig) gefolgt von Fall-Nummer als String zurück.
	 * Achtung, entspricht NICHT der Antragsnummer! (siehe Antrag.laufnummer)
	 */
	public String getJahrAndFallnummer() {
		if (getGesuchsperiode() == null) {
			return "-";
		}
		return Integer.toString(getGesuchsperiode().getGueltigkeit().getGueltigAb().getYear()).substring(2)
			+ '.' + getFall().getPaddedFallnummer();
	}

	@Transient
	public List<Betreuung> extractAllBetreuungen() {
		final List<Betreuung> list = new ArrayList<>();
		for (final KindContainer kind : getKindContainers()) {
			list.addAll(kind.getBetreuungen());
		}
		return list;
	}

	@Transient
	public List<AbwesenheitContainer> extractAllAbwesenheiten() {
		final List<AbwesenheitContainer> list = new ArrayList<>();
		for (final KindContainer kind : getKindContainers()) {
			for (final Betreuung betreuung : kind.getBetreuungen()) {
				list.addAll(betreuung.getAbwesenheitContainers());
			}
		}
		return list;
	}

	@Nullable
	@Transient
	public Betreuung extractBetreuungById(String betreuungId) {
		for (KindContainer kind : getKindContainers()) {
			for (Betreuung betreuung : kind.getBetreuungen()) {
				if (betreuung.getId().equals(betreuungId)) {
					return betreuung;
				}
			}
		}
		return null;
	}

	/**
	 * @return Den Familiennamen beider Gesuchsteller falls es 2 gibt, sonst Familiennamen von GS1
	 */
	@Transient
	public String extractFamiliennamenString() {
		String bothFamiliennamen = (this.getGesuchsteller1() != null ? this.getGesuchsteller1().extractNachname() : "");
		bothFamiliennamen += this.getGesuchsteller2() != null ? ", " + this.getGesuchsteller2().extractNachname() : "";
		return bothFamiliennamen;
	}

	@Transient
	public String extractFullnamesString() {
		String bothFamiliennamen = (this.getGesuchsteller1() != null ? this.getGesuchsteller1().extractFullName() : "");
		bothFamiliennamen += this.getGesuchsteller2() != null ? ", " + this.getGesuchsteller2().extractFullName() : "";
		return bothFamiliennamen;
	}

	@Transient
	public boolean isMutation() {
		return this.typ == AntragTyp.MUTATION;
	}

	@Transient
	public boolean hasBetreuungOfInstitution(@Nullable final Institution institution) {
		if (institution == null) {
			return false;
		}
		return kindContainers.stream()
			.flatMap(kindContainer -> kindContainer.getBetreuungen().stream())
			.anyMatch(betreuung -> betreuung.getInstitutionStammdaten().getInstitution().equals(institution));

	}

	/**
	 * @return false wenn es ein kind gibt dass eine nicht schulamt betreuung hat, wenn es kein kind oder betr gibt wird false zurueckgegeben
	 */
	@Transient
	public boolean hasOnlyBetreuungenOfSchulamt() {
		//noinspection SimplifyStreamApiCallChains
		List<Betreuung> allBetreuungen = kindContainers.stream().flatMap(kindContainer -> kindContainer.getBetreuungen().stream())
			.collect(Collectors.toList());
		return !allBetreuungen.isEmpty() && allBetreuungen.stream().allMatch(betreuung -> betreuung.getBetreuungsangebotTyp().isSchulamt());
	}

	@Transient
	public boolean areAllBetreuungenBestaetigt() {
		List<Betreuung> betreuungs = extractAllBetreuungen();
		for (Betreuung betreuung : betreuungs) {
			if (Betreuungsstatus.WARTEN == betreuung.getBetreuungsstatus() ||
				Betreuungsstatus.ABGEWIESEN == betreuung.getBetreuungsstatus()) {
				return false;
			}
		}
		return true;
	}

	@Transient
	public boolean hasBetreuungOfJugendamt() {
		return kindContainers.stream()
			.flatMap(kindContainer -> kindContainer.getBetreuungen().stream())
			.anyMatch(betreuung -> {
				if (betreuung.getBetreuungsangebotTyp() != null) {
					return betreuung.getBetreuungsangebotTyp().isJugendamt();
				}
				return false;
			});
	}

	@Transient
	public boolean hasBetreuungOfSchulamt() {
		return kindContainers.stream()
			.flatMap(kindContainer -> kindContainer.getBetreuungen().stream())
			.anyMatch(betreuung -> {
				if (betreuung.getBetreuungsangebotTyp() != null) {
					return betreuung.getBetreuungsangebotTyp().isSchulamt();
				}
				return false;
			});
	}

	@Nullable
	public Familiensituation extractFamiliensituation() {
		if (familiensituationContainer != null) {
			return familiensituationContainer.extractFamiliensituation();
		}
		return null;
	}

	@Nullable
	public Familiensituation extractFamiliensituationErstgesuch() {
		if (familiensituationContainer != null) {
			return familiensituationContainer.getFamiliensituationErstgesuch();
		}
		return null;
	}

	public void initFamiliensituationContainer() {
		familiensituationContainer = new FamiliensituationContainer();
		familiensituationContainer.setFamiliensituationJA(new Familiensituation());
	}

	@Nonnull
	public Gesuch copyForMutation(@Nonnull Gesuch mutation, @Nonnull Eingangsart eingangsart) {
		super.copyForMutation(mutation);
		mutation.setEingangsart(eingangsart);
		mutation.setFall(this.getFall());
		mutation.setGesuchsperiode(this.getGesuchsperiode());
		mutation.setEingangsdatum(null);
		mutation.setStatus(eingangsart == Eingangsart.PAPIER ? AntragStatus.IN_BEARBEITUNG_JA : AntragStatus.IN_BEARBEITUNG_GS);
		mutation.setTyp(AntragTyp.MUTATION);
		mutation.setLaufnummer(this.getLaufnummer() + 1);

		if (this.getGesuchsteller1() != null) {
			mutation.setGesuchsteller1(this.getGesuchsteller1().copyForMutation(new GesuchstellerContainer()));
		}
		if (this.getGesuchsteller2() != null) {
			mutation.setGesuchsteller2(this.getGesuchsteller2().copyForMutation(new GesuchstellerContainer()));
		}
		for (KindContainer kindContainer : this.getKindContainers()) {
			mutation.addKindContainer(kindContainer.copyForMutation(new KindContainer(), mutation, eingangsart));
		}
		mutation.setAntragStatusHistories(new LinkedHashSet<>());

		if (this.getFamiliensituationContainer() != null) {
			mutation.setFamiliensituationContainer(this.getFamiliensituationContainer().copyForMutation(new FamiliensituationContainer(), this.isMutation()));
		}

		if (this.getEinkommensverschlechterungInfoContainer() != null) {
			mutation.setEinkommensverschlechterungInfoContainer(this.getEinkommensverschlechterungInfoContainer().copyForMutation(new EinkommensverschlechterungInfoContainer(), mutation));
		}

		if (this.getDokumentGrunds() != null) {
			mutation.setDokumentGrunds(new HashSet<>());
			for (DokumentGrund dokumentGrund : this.getDokumentGrunds()) {
				mutation.addDokumentGrund(dokumentGrund.copyForMutation(new DokumentGrund()));
			}
		}
		mutation.setGesperrtWegenBeschwerde(false);
		mutation.setGeprueftSTV(false);
		mutation.setDatumGewarntNichtFreigegeben(null);
		mutation.setDatumGewarntFehlendeQuittung(null);
		mutation.setTimestampVerfuegt(null);
		mutation.setGueltig(false);
		mutation.setDokumenteHochgeladen(false);
		return mutation;
	}

	@Nonnull
	public Gesuch copyForErneuerung(@Nonnull Gesuch folgegesuch, @Nonnull Gesuchsperiode gesuchsperiode, @Nonnull Eingangsart eingangsart) {
		super.copyForErneuerung(folgegesuch);
		folgegesuch.setEingangsart(eingangsart);
		folgegesuch.setFall(this.getFall());
		folgegesuch.setGesuchsperiode(gesuchsperiode);
		folgegesuch.setEingangsdatum(null);
		folgegesuch.setStatus(eingangsart == Eingangsart.PAPIER ? AntragStatus.IN_BEARBEITUNG_JA : AntragStatus.IN_BEARBEITUNG_GS);
		folgegesuch.setTyp(AntragTyp.ERNEUERUNGSGESUCH);
		folgegesuch.setLaufnummer(0); // Wir fangen für die neue Periode wieder mit 0 an

		// Zuerst die Familiensituation kopieren, damit wir beim Kopieren der GS wissen, ob GS2 kopiert werden muss
		if (this.getFamiliensituationContainer() != null) {
			folgegesuch.setFamiliensituationContainer(this.getFamiliensituationContainer().copyForErneuerung(new FamiliensituationContainer()));
		}

		if (this.getGesuchsteller1() != null) {
			folgegesuch.setGesuchsteller1(this.getGesuchsteller1().copyForErneuerung(new GesuchstellerContainer(), gesuchsperiode));
		}
		// Den zweiten GS nur kopieren, wenn er laut aktuellem Zivilstand noch benoetigt wird
		if (this.getGesuchsteller2() != null && folgegesuch.getFamiliensituationContainer().getFamiliensituationJA() != null
			&& folgegesuch.getFamiliensituationContainer().getFamiliensituationJA().hasSecondGesuchsteller()) {
			folgegesuch.setGesuchsteller2(this.getGesuchsteller2().copyForErneuerung(new GesuchstellerContainer(), gesuchsperiode));
		}
		for (KindContainer kindContainer : this.getKindContainers()) {
			folgegesuch.addKindContainer(kindContainer.copyForErneuerung(new KindContainer(), folgegesuch));
		}
		folgegesuch.setGesperrtWegenBeschwerde(false);
		folgegesuch.setGeprueftSTV(false);
		folgegesuch.setDatumGewarntNichtFreigegeben(null);
		folgegesuch.setDatumGewarntFehlendeQuittung(null);
		folgegesuch.setTimestampVerfuegt(null);
		folgegesuch.setGueltig(false);
		folgegesuch.setDokumenteHochgeladen(false);
		return folgegesuch;
	}

	@Nonnull
	@Override
	public String getSearchResultId() {
		return getId();
	}

	@Nonnull
	@Override
	public String getSearchResultSummary() {
		return getJahrAndFallnummer();
	}

	@Nullable
	@Override
	public String getSearchResultAdditionalInformation() {
		return toString();
	}

	@Override
	public String getOwningGesuchId() {
		return getId();
	}

	@Override
	public String getOwningFallId() {
		return getFall().getId();
	}

	@Nonnull
	public Optional<Betreuung> extractBetreuungsFromBetreuungNummer(@NotNull Integer kindNummer, @NotNull Integer betreuungNummer) {
		final List<Betreuung> allBetreuungen = extractAllBetreuungen();
		for (final Betreuung betreuung : allBetreuungen) {
			if (betreuung.getBetreuungNummer().equals(betreuungNummer) && betreuung.getKind().getKindNummer().equals(kindNummer)) {
				return Optional.of(betreuung);
			}
		}
		return Optional.empty();
	}

	public String getEingangsdatumFormated() {
		return Constants.DATE_FORMATTER.format(eingangsdatum);
	}

	public String getFreigabedatumFormated() {
		if (freigabeDatum != null) {
			return Constants.DATE_FORMATTER.format(freigabeDatum);
		}
		return "";
	}

	@Nullable
	public Gesuchsteller extractGesuchsteller1() {
		if (this.getGesuchsteller1() != null) {
			return this.getGesuchsteller1().getGesuchstellerJA();
		}
		return null;
	}

	@Nullable
	public KindContainer extractKindFromKindNumber(Integer kindNumber) {
		if (this.kindContainers != null && kindNumber > 0) {
			for (KindContainer kindContainer : this.kindContainers) {
				if (Objects.equals(kindContainer.getKindNummer(), kindNumber)) {
					return kindContainer;
				}
			}
		}
		return null;
	}

	public AntragStatus getPreStatus() {
		return preStatus;
	}

	public void setPreStatus(AntragStatus preStatus) {
		this.preStatus = preStatus;
	}


}
