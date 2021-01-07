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
import java.util.concurrent.atomic.AtomicBoolean;
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
import ch.dvbern.ebegu.dto.suchfilter.lucene.Searchable;
import ch.dvbern.ebegu.enums.AntragCopyType;
import ch.dvbern.ebegu.enums.AntragStatus;
import ch.dvbern.ebegu.enums.AntragTyp;
import ch.dvbern.ebegu.enums.Betreuungsstatus;
import ch.dvbern.ebegu.enums.DokumentGrundPersonType;
import ch.dvbern.ebegu.enums.Eingangsart;
import ch.dvbern.ebegu.enums.FinSitStatus;
import ch.dvbern.ebegu.enums.GesuchBetreuungenStatus;
import ch.dvbern.ebegu.enums.GesuchTypFromAngebotTyp;
import ch.dvbern.ebegu.util.Constants;
import ch.dvbern.ebegu.validationgroups.AntragCompleteValidationGroup;
import ch.dvbern.ebegu.validationgroups.GesuchstellerSaveValidationGroup;
import ch.dvbern.ebegu.validators.CheckEmailGesuchsteller;
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
@CheckEmailGesuchsteller(groups = GesuchstellerSaveValidationGroup.class)
@Entity
@Indexed
@Analyzer(definition = "EBEGUGermanAnalyzer")
@EntityListeners({ GesuchStatusListener.class , GesuchGueltigListener.class})
@Table(
	uniqueConstraints = @UniqueConstraint(columnNames = { "dossier_id", "gesuchsperiode_id", "gueltig" }, name = "UK_gueltiges_gesuch"),
	indexes = @Index(name = "IX_gesuch_timestamp_erstellt", columnList = "timestampErstellt")
)
public class Gesuch extends AbstractMutableEntity implements Searchable {

	private static final long serialVersionUID = -8403487439884700618L;

	@NotNull
	@ManyToOne(optional = false)
	@JoinColumn(foreignKey = @ForeignKey(name = "FK_gesuch_dossier_id"))
	@IndexedEmbedded
	private Dossier dossier;

	@NotNull
	@ManyToOne(optional = false)
	@JoinColumn(foreignKey = @ForeignKey(name = "FK_antrag_gesuchsperiode_id"))
	private Gesuchsperiode gesuchsperiode;

	@Nullable
	@Column(nullable = true)
	private LocalDate eingangsdatum;

	@Nullable
	@Column(nullable = true)
	private LocalDate regelnGueltigAb; //Alternatives Eingangsdatum für Regelwerk

	@Nullable
	@Column(nullable = true)
	private LocalDate freigabeDatum;

	@Nullable
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
	private boolean verfuegungEingeschrieben = false;

	@Enumerated(EnumType.STRING)
	@Column(nullable = true)
	@Nullable
	private FinSitStatus finSitStatus;

	@Column(nullable = false)
	private boolean gesperrtWegenBeschwerde = false;

	@Nullable
	@Column(nullable = true)
	private LocalDate datumGewarntNichtFreigegeben;

	@Nullable
	@Column(nullable = true)
	private LocalDate datumGewarntFehlendeQuittung;

	@Nullable
	@Column(nullable = true)
	private LocalDateTime timestampVerfuegt;

	// Es muss nullable sein koennen, damit man ein UNIQUE_KEY machen kann
	@Nullable
	@Column(nullable = true)
	private Boolean gueltig = null;

	@NotNull @Nonnull
	@Column(nullable = false)
	// jedesmal wenn der Gesuchsteller das Gesuch zurück zieht, wird dieses Feld um 1 erhöht, damit wir beim
	// einscannen der Freigabequittung wissen, ob es sich um die aktuelle Freigabequittung handelt.
	private Integer anzahlGesuchZurueckgezogen = 0;


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
		final Familiensituation familiensituation = extractFamiliensituation();
		if (familiensituation != null
			&& familiensituation.hasSecondGesuchsteller(gesuchsperiode.getGueltigkeit().getGueltigBis())) {
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
		return dossier.getFall();
	}

	public Dossier getDossier() {
		return dossier;
	}

	public void setDossier(Dossier dossier) {
		this.dossier = dossier;
	}

	public Gesuchsperiode getGesuchsperiode() {
		return gesuchsperiode;
	}

	public void setGesuchsperiode(Gesuchsperiode gesuchsperiode) {
		this.gesuchsperiode = gesuchsperiode;
	}

	@Nullable
	public LocalDate getEingangsdatum() {
		return eingangsdatum;
	}

	public void setEingangsdatum(@Nullable LocalDate eingangsdatum) {
		this.eingangsdatum = eingangsdatum;
	}

	@Nullable
	public LocalDate getRegelnGueltigAb() { return regelnGueltigAb; }

	public void setRegelnGueltigAb(@Nullable LocalDate regelnGueltigAb) { this.regelnGueltigAb = regelnGueltigAb; }

	@Nullable
	public LocalDate getEingangsdatumSTV() {
		return eingangsdatumSTV;
	}

	public void setEingangsdatumSTV(@Nullable LocalDate eingangsdatumSTV) {
		this.eingangsdatumSTV = eingangsdatumSTV;
	}

	@Nullable
	public LocalDate getFreigabeDatum() {
		return freigabeDatum;
	}

	public void setFreigabeDatum(@Nullable LocalDate freigabeDatum) {
		this.freigabeDatum = freigabeDatum;
	}

	@Nonnull
	public AntragStatus getStatus() {
		return status;
	}

	public void setStatus(@Nonnull AntragStatus status) {
		this.status = status;
	}

	public AntragTyp getTyp() {
		return typ;
	}

	public void setTyp(AntragTyp typ) {
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

	public boolean isVerfuegungEingeschrieben() {
		return verfuegungEingeschrieben;
	}

	public void setVerfuegungEingeschrieben(boolean verfuegungEingeschrieben) {
		this.verfuegungEingeschrieben = verfuegungEingeschrieben;
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

	@Nullable
	public LocalDate getDatumGewarntNichtFreigegeben() {
		return datumGewarntNichtFreigegeben;
	}

	public void setDatumGewarntNichtFreigegeben(@Nullable LocalDate datumGewarntNichtFreigegeben) {
		this.datumGewarntNichtFreigegeben = datumGewarntNichtFreigegeben;
	}

	@Nullable
	public LocalDate getDatumGewarntFehlendeQuittung() {
		return datumGewarntFehlendeQuittung;
	}

	public void setDatumGewarntFehlendeQuittung(@Nullable LocalDate datumGewarntFehlendeQuittung) {
		this.datumGewarntFehlendeQuittung = datumGewarntFehlendeQuittung;
	}

	@Nullable
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

	@Nonnull
	public Integer getAnzahlGesuchZurueckgezogen() {
		return anzahlGesuchZurueckgezogen;
	}

	public void setAnzahlGesuchZurueckgezogen(@Nonnull Integer anzahlGesuchZurueckgezogen) {
		this.anzahlGesuchZurueckgezogen = anzahlGesuchZurueckgezogen;
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
			&& Objects.equals(this.getDossier(), otherAntrag.getDossier())
			&& Objects.equals(this.getGesuchsperiode(), otherAntrag.getGesuchsperiode());
	}

	/**
	 * Gibt das Startjahr der Gesuchsperiode (zweistellig) gefolgt von Fall-Nummer als String zurück.
	 * Achtung, entspricht NICHT der Antragsnummer! (siehe Antrag.laufnummer)
	 */
	@Nonnull
	public String getJahrFallAndGemeindenummer() {
		if (getGesuchsperiode() == null) {
			return "-";
		}
		return Integer.toString(getGesuchsperiode().getGueltigkeit().getGueltigAb().getYear()).substring(2)
			+ '.' + getFall().getPaddedFallnummer() + '.' + getDossier().getGemeinde().getPaddedGemeindeNummer();
	}

	@Transient
	@Nonnull
	public List<Betreuung> extractAllBetreuungen() {
		final List<Betreuung> list = new ArrayList<>();
		for (final KindContainer kind : getKindContainers()) {
			list.addAll(kind.getBetreuungen());
		}
		return list;
	}

	@Transient
	@Nonnull
	public List<AbstractAnmeldung> extractAllAnmeldungen() {
		final List<AbstractAnmeldung> list = new ArrayList<>();
		for (final KindContainer kind : getKindContainers()) {
			list.addAll(kind.getAnmeldungenTagesschule());
			list.addAll(kind.getAnmeldungenFerieninsel());
		}
		return list;
	}

	@Transient
	@Nonnull
	public List<AnmeldungTagesschule> extractAllAnmeldungenTagesschule() {
		final List<AnmeldungTagesschule> list = new ArrayList<>();
		for (final KindContainer kind : getKindContainers()) {
			list.addAll(kind.getAnmeldungenTagesschule());
		}
		return list;
	}

	@Transient
	@Nonnull
	public List<AbstractPlatz> extractAllPlaetze() {
		final List<AbstractPlatz> list = new ArrayList<>();
		for (final KindContainer kind : getKindContainers()) {
			list.addAll(kind.getBetreuungen());
			list.addAll(kind.getAnmeldungenTagesschule());
			list.addAll(kind.getAnmeldungenFerieninsel());
		}
		return list;
	}

	@Transient
	@Nonnull
	public List<Kind> extractAllKinderWithAngebot() {
		final List<Kind> list = new ArrayList<>();
		for (final KindContainer kind : getKindContainers()) {
			if (kind.getKindJA().getFamilienErgaenzendeBetreuung()) {
				list.add(kind.getKindJA());
			}
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
		Familiensituation familiensituation = extractFamiliensituation();

		String bothFamiliennamen = (this.getGesuchsteller1() != null ? this.getGesuchsteller1().extractFullName() : "");

		if (familiensituation != null && familiensituation.hasSecondGesuchsteller(getGesuchsperiode().getGueltigkeit().getGueltigBis())) {
			bothFamiliennamen += this.getGesuchsteller2() != null ? ", " + this.getGesuchsteller2().extractFullName() : "";
		}

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
		boolean hasBetreuungsKitaTagesfamillie = kindContainers.stream()
			.flatMap(kindContainer -> kindContainer.getBetreuungen().stream())
			.anyMatch(betreuung -> betreuung.getInstitutionStammdaten().getInstitution().equals(institution));

		boolean hasBetreuungsTagesschule = kindContainers.stream()
			.flatMap(kindContainer -> kindContainer.getAnmeldungenTagesschule().stream())
			.anyMatch(anmeldungTagesschule -> anmeldungTagesschule.getInstitutionStammdaten().getInstitution().equals(institution));

		boolean hasBetreuungsFerienInselt = kindContainers.stream()
			.flatMap(kindContainer -> kindContainer.getAnmeldungenFerieninsel().stream())
			.anyMatch(anmeldungFerieninsel -> anmeldungFerieninsel.getInstitutionStammdaten().getInstitution().equals(institution));

		return hasBetreuungsKitaTagesfamillie || hasBetreuungsTagesschule || hasBetreuungsFerienInselt;
	}

	/**
	 * @return false wenn es ein kind gibt dass eine nicht schulamt betreuung hat,
	 * wenn es kein kind oder betr gibt wird false zurueckgegeben
	 */
	@Transient
	public boolean hasOnlyBetreuungenOfSchulamt() {
		//noinspection SimplifyStreamApiCallChains
		List<Betreuung> allBetreuungen = kindContainers.stream().flatMap(kindContainer -> kindContainer.getBetreuungen().stream())
			.collect(Collectors.toList());

		List<AnmeldungTagesschule> anmeldungTagesschules =
			kindContainers.stream().flatMap(kindContainer -> kindContainer.getAnmeldungenTagesschule().stream())
			.collect(Collectors.toList());

		List<AnmeldungFerieninsel> anmeldungFerienInsel =
			kindContainers.stream().flatMap(kindContainer -> kindContainer.getAnmeldungenFerieninsel().stream())
				.collect(Collectors.toList());

		return allBetreuungen.isEmpty() && (!anmeldungTagesschules.isEmpty() || !anmeldungFerienInsel.isEmpty());
	}

	/**
	 * @return false wenn es ein kind gibt dass eine nicht jugendamt betreuung hat,
	 * wenn es kein kind oder betr gibt wird false zurueckgegeben
	 */
	@Transient
	public boolean hasOnlyBetreuungenOfJugendamt() {
		//noinspection SimplifyStreamApiCallChains
		List<Betreuung> allBetreuungen = kindContainers.stream().flatMap(kindContainer -> kindContainer.getBetreuungen().stream())
			.collect(Collectors.toList());

		List<AnmeldungTagesschule> anmeldungTagesschules =
			kindContainers.stream().flatMap(kindContainer -> kindContainer.getAnmeldungenTagesschule().stream())
				.collect(Collectors.toList());

		List<AnmeldungFerieninsel> anmeldungFerienInsel =
			kindContainers.stream().flatMap(kindContainer -> kindContainer.getAnmeldungenFerieninsel().stream())
				.collect(Collectors.toList());

		return !allBetreuungen.isEmpty() && anmeldungTagesschules.isEmpty() && anmeldungFerienInsel.isEmpty();
	}

	@Transient
	public boolean areAllBetreuungenBestaetigt() {
		List<Betreuung> betreuungs = extractAllBetreuungen();
		for (Betreuung betreuung : betreuungs) {
			if (Betreuungsstatus.WARTEN == betreuung.getBetreuungsstatus() ||
				Betreuungsstatus.ABGEWIESEN == betreuung.getBetreuungsstatus() ||
				Betreuungsstatus.UNBEKANNTE_INSTITUTION == betreuung.getBetreuungsstatus()) {
				return false;
			}
		}
		return true;
	}

	@Transient
	public boolean hasBetreuungOfJugendamt() {
		List<Betreuung> allBetreuungen = kindContainers.stream().flatMap(kindContainer -> kindContainer.getBetreuungen().stream())
			.collect(Collectors.toList());
		return !allBetreuungen.isEmpty();
	}

	@Transient
	public boolean hasBetreuungOfSchulamt() {
		List<AnmeldungTagesschule> anmeldungTagesschules =
			kindContainers.stream().flatMap(kindContainer -> kindContainer.getAnmeldungenTagesschule().stream())
				.collect(Collectors.toList());

		List<AnmeldungFerieninsel> anmeldungFerienInsel =
			kindContainers.stream().flatMap(kindContainer -> kindContainer.getAnmeldungenFerieninsel().stream())
				.collect(Collectors.toList());
		return !anmeldungTagesschules.isEmpty() ||!anmeldungFerienInsel.isEmpty();
	}

	@Nullable
	@Transient
	public LocalDate getRegelStartDatum() {
		if (null != getRegelnGueltigAb()) {
			return getRegelnGueltigAb();
		}
		if (getEingangsdatum() == null
			&& getEingangsart() == Eingangsart.ONLINE) {
			// damit die prov. Berechnung korrekt funktioniert, wird als default das heutige Datum gesetzt
			// falls es ein Online Gesuch ist. If it doesn't have any prov. Berechnung too
			return LocalDate.now();
		}
		return getEingangsdatum();
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
	public Gesuch copyGesuch(@Nonnull Gesuch target, @Nonnull AntragCopyType copyType, @Nonnull Eingangsart targetEingangsart, @Nonnull AntragTyp targetTyp,
			@Nonnull Dossier targetDossier, @Nonnull Gesuchsperiode targetGesuchsperiode) {
		super.copyAbstractEntity(target, copyType);
		target.setEingangsart(targetEingangsart);
		target.setTyp(targetTyp);
		target.setEingangsdatum(null);
		target.setRegelnGueltigAb(null);
		target.setDossier(targetDossier);
		target.setGesuchsperiode(targetGesuchsperiode);
		target.setStatus(targetEingangsart == Eingangsart.PAPIER ? AntragStatus.IN_BEARBEITUNG_JA : AntragStatus.IN_BEARBEITUNG_GS);

		target.setAntragStatusHistories(new LinkedHashSet<>());
		target.setGesperrtWegenBeschwerde(false);
		target.setGeprueftSTV(false);
		target.setDatumGewarntNichtFreigegeben(null);
		target.setDatumGewarntFehlendeQuittung(null);
		target.setTimestampVerfuegt(null);
		// null instead of false because of UK_Constraint UK_gueltiges_gesuch
		target.setGueltig(null);
		target.setDokumenteHochgeladen(false);

		copyFamiliensituation(target, copyType, this.isMutation());
		copyGesuchsteller1(target, copyType);

		copyKindContainer(target, copyType, target.getRegelStartDatum() != null ? target.getRegelStartDatum() : LocalDate.now());

		switch (copyType) {
		case MUTATION:
			target.setLaufnummer(this.getLaufnummer() + 1);
			copyGesuchsteller2(target, copyType);
			copyEinkommensverschlechterungInfoContainer(target, copyType);
			copyDokumentGruende(target, copyType);

			if (getFamiliensituationContainer() != null
				&& getFamiliensituationContainer().getFamiliensituationJA() != null
				&& Boolean.FALSE.equals(getFamiliensituationContainer().getFamiliensituationJA().getVerguenstigungGewuenscht())) {
				target.setFinSitStatus(FinSitStatus.AKZEPTIERT);
			}
			break;
		case ERNEUERUNG:
		case ERNEUERUNG_NEUES_DOSSIER:
			target.setLaufnummer(0); // Wir fangen für die neue Periode wieder mit 0 an
			copyGesuchsteller2IfStillNeeded(target, copyType);
			break;
		case MUTATION_NEUES_DOSSIER:
			target.setLaufnummer(0); // Wir fangen für das neue Dossier wieder mit 0 an
			copyGesuchsteller2(target, copyType);
			copyEinkommensverschlechterungInfoContainer(target, copyType);
			copyDokumentGruende(target, copyType);
			if (getFamiliensituationContainer() != null
				&& getFamiliensituationContainer().getFamiliensituationJA() != null
				&& Boolean.FALSE.equals(getFamiliensituationContainer().getFamiliensituationJA().getVerguenstigungGewuenscht())) {
				target.setFinSitStatus(FinSitStatus.AKZEPTIERT);
			}
			break;
		}
		return target;
	}

	private void copyFamiliensituation(@Nonnull Gesuch target, @Nonnull AntragCopyType copyType, boolean sourceGesuchIsMutation) {
		if (this.getFamiliensituationContainer() != null) {
			target.setFamiliensituationContainer(this.getFamiliensituationContainer().copyFamiliensituationContainer(new FamiliensituationContainer(),
				copyType, sourceGesuchIsMutation));
		}
	}

	private void copyEinkommensverschlechterungInfoContainer(@Nonnull Gesuch target, @Nonnull AntragCopyType copyType) {
		if (this.getEinkommensverschlechterungInfoContainer() != null) {
			target.setEinkommensverschlechterungInfoContainer(this.getEinkommensverschlechterungInfoContainer()
				.copyEinkommensverschlechterungInfoContainer (new EinkommensverschlechterungInfoContainer(), copyType, target));
		}
	}

	private void copyGesuchsteller1(@Nonnull Gesuch target, @Nonnull AntragCopyType copyType) {
		if (this.getGesuchsteller1() != null) {
			target.setGesuchsteller1(this.getGesuchsteller1().copyGesuchstellerContainer(new GesuchstellerContainer(), copyType));
		}
	}

	private void copyGesuchsteller2(@Nonnull Gesuch target, @Nonnull AntragCopyType copyType) {
		if (this.getGesuchsteller2() != null && this.hasSecondGesuchstellerAtAnyTimeOfGesuchsperiode()) {
			target.setGesuchsteller2(this.getGesuchsteller2().copyGesuchstellerContainer(new GesuchstellerContainer(), copyType));
		}
	}

	private void copyGesuchsteller2IfStillNeeded(@Nonnull Gesuch target, @Nonnull AntragCopyType copyType) {
		// Den zweiten GS nur kopieren, wenn er laut aktuellem Zivilstand noch benoetigt wird
		if (this.getGesuchsteller2() != null && target.hasSecondGesuchstellerAtEndOfGesuchsperiode()) {
			target.setGesuchsteller2(this.getGesuchsteller2().copyGesuchstellerContainer(new GesuchstellerContainer(), copyType));
		}
	}

	private void copyKindContainer(
		@Nonnull Gesuch target,
		@Nonnull AntragCopyType copyType,
		@Nonnull LocalDate regelStartDatum) {
		this.getKindContainers().forEach(
			kindContainer -> target.addKindContainer(kindContainer.copyKindContainer(new KindContainer(), copyType, target, target.getGesuchsperiode(), regelStartDatum))
		);
	}

	private void copyDokumentGruende(@Nonnull Gesuch target, @Nonnull AntragCopyType copyType) {
		if (this.getDokumentGrunds() != null) {
			target.setDokumentGrunds(new HashSet<>());
			this.getDokumentGrunds().forEach(
				dokumentGrund -> {
					if (!isDokumentOfSecondGesuchstellerButHasNoSecondGesuchsteller(target, dokumentGrund)) {
						target.addDokumentGrund(dokumentGrund.copyDokumentGrund(new DokumentGrund(), copyType));
					}
				}
			);
		}
	}

	private boolean isDokumentOfSecondGesuchstellerButHasNoSecondGesuchsteller(@Nonnull Gesuch target, @Nonnull DokumentGrund dokumentGrund) {
		boolean hasSecondGS = target.hasSecondGesuchstellerAtAnyTimeOfGesuchsperiode();
		if (!hasSecondGS) {
			boolean isDokumentOfSecondGS = dokumentGrund.getPersonType() == DokumentGrundPersonType.GESUCHSTELLER
				&& dokumentGrund.getPersonNumber() != null && 2 == dokumentGrund.getPersonNumber();
			return isDokumentOfSecondGS;
		}
		return false;
	}

	@Nonnull
	public Gesuch copyForMutation(
			@Nonnull Gesuch mutation, @Nonnull Eingangsart eingangsartOfTarget) {
		return this.copyGesuch(mutation, AntragCopyType.MUTATION, eingangsartOfTarget, AntragTyp.MUTATION, this.getDossier(), this.getGesuchsperiode());
	}

	@Nonnull
	public Gesuch copyForErneuerung(
			@Nonnull Gesuch folgegesuch, @Nonnull Gesuchsperiode gesuchsperiodeOfTarget, @Nonnull Eingangsart eingangsartOfTarget) {
		return this.copyGesuch(folgegesuch, AntragCopyType.ERNEUERUNG, eingangsartOfTarget, AntragTyp.ERNEUERUNGSGESUCH, this.getDossier(), gesuchsperiodeOfTarget);
	}

	@Nonnull
	public Gesuch copyForErneuerungsgesuchNeuesDossier(
			@Nonnull Gesuch target, @Nonnull Eingangsart eingangsartOfTarget,
			@Nonnull Dossier dossierOfTarget, @Nonnull Gesuchsperiode gesuchsperiodeOfTarget) {
		return this.copyGesuch(target, AntragCopyType.ERNEUERUNG_NEUES_DOSSIER, eingangsartOfTarget, AntragTyp.ERSTGESUCH, dossierOfTarget, gesuchsperiodeOfTarget);
	}

	@Nonnull
	public Gesuch copyForMutationNeuesDossier(
			@Nonnull Gesuch target, @Nonnull Eingangsart eingangsartOfTarget, @Nonnull Dossier dossierOfTarget) {
		return this.copyGesuch(target, AntragCopyType.MUTATION_NEUES_DOSSIER, eingangsartOfTarget, AntragTyp.ERSTGESUCH, dossierOfTarget, this.getGesuchsperiode());
	}

	@Nonnull
	@Override
	public String getSearchResultId() {
		return getId();
	}

	@Nonnull
	@Override
	public String getSearchResultSummary() {
		return getJahrFallAndGemeindenummer();
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

	@Nullable
	@Override
	public String getOwningDossierId() {
		return getDossier().getId();
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
		if (eingangsdatum != null) {
			return Constants.DATE_FORMATTER.format(eingangsdatum);
		}
		return "";
	}

	public String getFreigabedatumFormated() {
		if (freigabeDatum != null) {
			return Constants.DATE_FORMATTER.format(freigabeDatum);
		}
		return "";
	}

	@Nonnull
	public Optional<Gesuchsteller> extractGesuchsteller1() {
		return Optional.ofNullable(this.gesuchsteller1)
			.map(GesuchstellerContainer::getGesuchstellerJA);
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

	@Nonnull
	public Gemeinde extractGemeinde() {
		return getDossier().getGemeinde();
	}

	public AntragStatus getPreStatus() {
		return preStatus;
	}

	public void setPreStatus(AntragStatus preStatus) {
		this.preStatus = preStatus;
	}

	/**
	 * This method will go through all Betreuungen of the Gesuch and check all of them to know which kind (BetreuungsangebotTyp)
	 * of betreuungen they are. An enum will be returned with the result
	 */
	public GesuchTypFromAngebotTyp calculateGesuchTypFromAngebotTyp() {
		AtomicBoolean hasSCHAngebote = new AtomicBoolean(false);
		AtomicBoolean hasBGAngebote = new AtomicBoolean(false);
		kindContainers.stream()
			.filter(kindContainer -> !kindContainer.getBetreuungen().isEmpty())
			.flatMap(kindContainer -> kindContainer.getBetreuungen().stream())
			.collect(Collectors.toList())
			.forEach(betreuung -> {
				if (betreuung.isAngebotSchulamt()) {
					hasSCHAngebote.set(true);
				} else {
					hasBGAngebote.set(true);
				}
			});
		if (hasSCHAngebote.get() && hasBGAngebote.get()) {
			return GesuchTypFromAngebotTyp.MISCH_GESUCH;
		}
		if (hasSCHAngebote.get()) {
			return GesuchTypFromAngebotTyp.TS_GESUCH;
		}
		if (hasBGAngebote.get()) {
			return GesuchTypFromAngebotTyp.BG_GESUCH;
		}

		return GesuchTypFromAngebotTyp.NO_ANGEBOT_GESUCH;
	}

	@Nullable
	public Betreuung getFirstBetreuung() {
		return extractAllBetreuungen().stream()
			.findFirst()
			.orElse(null);
	}

	@Nullable
	public AbstractPlatz getFirstBetreuungOrAnmeldungTagesschule() {
		return extractAllPlaetze().stream()
			.filter(platz -> platz.getBetreuungsangebotTyp().isBerechnetesAngebot())
			.findFirst()
			.orElse(null);
	}

	public boolean hasSecondGesuchstellerAtEndOfGesuchsperiode() {
		Familiensituation familiensituation = extractFamiliensituation();
		return familiensituation != null
			&& familiensituation.hasSecondGesuchsteller(getGesuchsperiode().getGueltigkeit().getGueltigBis());
	}

	public boolean hasSecondGesuchstellerAtAnyTimeOfGesuchsperiode() {
		return hasSecondGesuchstellerAtAnyTimeOfGesuchsperiode(extractFamiliensituation()) ||
			hasSecondGesuchstellerAtAnyTimeOfGesuchsperiode(extractFamiliensituationErstgesuch());
	}

	private boolean hasSecondGesuchstellerAtAnyTimeOfGesuchsperiode(@Nullable final Familiensituation familiensituation) {
		return familiensituation != null
			&& (familiensituation.hasSecondGesuchsteller(getGesuchsperiode().getGueltigkeit().getGueltigAb())
			|| familiensituation.hasSecondGesuchsteller(getGesuchsperiode().getGueltigkeit().getGueltigBis()));
	}

	public static Gesuch createMutation(@Nonnull Dossier dossier, @Nonnull Gesuchsperiode gesuchsperiode, @Nullable LocalDate eingangsdatum) {
		Gesuch mutation = new Gesuch();
		mutation.setTyp(AntragTyp.MUTATION);
		mutation.setDossier(dossier);
		mutation.setGesuchsperiode(gesuchsperiode);
		mutation.setEingangsdatum(eingangsdatum);
		return mutation;
	}

	public static Gesuch createErneuerung(@Nonnull Dossier dossier, @Nonnull Gesuchsperiode gesuchsperiode, @Nullable LocalDate eingangsdatum) {
		Gesuch erneuerung = new Gesuch();
		erneuerung.setTyp(AntragTyp.ERNEUERUNGSGESUCH);
		erneuerung.setDossier(dossier);
		erneuerung.setGesuchsperiode(gesuchsperiode);
		erneuerung.setEingangsdatum(eingangsdatum);
		return erneuerung;
	}

	@Override
	public String getMessageForAccessException() {
		return "eingangsart: " + this.getEingangsart()
			+ ", status: " + this.getStatus()
			+ ", fallNummer: " + this.getJahrFallAndGemeindenummer();
	}

	@Nullable
	public Benutzer getVerantwortlicherAccordingToBetreuungen() {
		if (hasOnlyBetreuungenOfSchulamt()) {
			return getDossier().getVerantwortlicherTS();
		}
		return getDossier().getVerantwortlicherBG();
	}
}
