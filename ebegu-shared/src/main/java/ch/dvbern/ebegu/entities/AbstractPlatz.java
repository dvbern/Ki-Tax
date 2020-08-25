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

import java.util.Locale;
import java.util.Objects;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.persistence.Column;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.ForeignKey;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MappedSuperclass;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

import ch.dvbern.ebegu.dto.suchfilter.lucene.Searchable;
import ch.dvbern.ebegu.enums.AntragCopyType;
import ch.dvbern.ebegu.enums.BetreuungsangebotTyp;
import ch.dvbern.ebegu.enums.Betreuungsstatus;
import ch.dvbern.ebegu.util.ServerMessageUtil;
import ch.dvbern.ebegu.validators.CheckPlatzAndAngebottyp;
import com.google.common.base.Preconditions;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.apache.commons.lang3.builder.CompareToBuilder;
import org.hibernate.envers.Audited;

/**
 * Superklasse fuer alle Betreuungen / Anmeldungen
 */
@MappedSuperclass
@Audited
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
@Table(
	uniqueConstraints =
	@UniqueConstraint(columnNames = { "betreuungNummer", "kind_id" }, name = "UK_platz_kind_betreuung_nummer")
)
@CheckPlatzAndAngebottyp
public abstract class AbstractPlatz extends AbstractMutableEntity implements Comparable<AbstractPlatz>, Searchable {

	private static final long serialVersionUID = -9037857320548372570L;

	@NotNull
	@ManyToOne(optional = false)
	@JoinColumn(foreignKey = @ForeignKey(name = "FK_platz_kind_id"), nullable = false)
	private KindContainer kind;

	@NotNull
	@ManyToOne(optional = false)
	@JoinColumn(foreignKey = @ForeignKey(name = "FK_platz_institution_stammdaten_id"), nullable = false)
	private InstitutionStammdaten institutionStammdaten;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	@NotNull
	private Betreuungsstatus betreuungsstatus;

	@NotNull
	@Min(1)
	@Column(nullable = false)
	private Integer betreuungNummer = 1;

	@Column(nullable = false)
	private boolean gueltig = false;

	/**
	 * It will always contain the vorganegerVerfuegung, regardless it has been paid or not
	 */
	@Transient
	@Nullable
	private Verfuegung vorgaengerVerfuegung;

	@Transient
	private boolean vorgaengerInitialized = false;


	protected AbstractPlatz() {
	}

	@Nonnull
	public KindContainer getKind() {
		return kind;
	}

	public void setKind(@Nonnull KindContainer kind) {
		this.kind = kind;
	}

	@Nonnull
	public InstitutionStammdaten getInstitutionStammdaten() {
		return institutionStammdaten;
	}

	public void setInstitutionStammdaten(@Nonnull InstitutionStammdaten institutionStammdaten) {
		this.institutionStammdaten = institutionStammdaten;
	}

	@NotNull
	public Betreuungsstatus getBetreuungsstatus() {
		return betreuungsstatus;
	}

	public void setBetreuungsstatus(@NotNull Betreuungsstatus betreuungsstatus) {
		this.betreuungsstatus = betreuungsstatus;
	}

	@Nonnull
	public Integer getBetreuungNummer() {
		return betreuungNummer;
	}

	public void setBetreuungNummer(@Nonnull Integer betreuungNummer) {
		this.betreuungNummer = betreuungNummer;
	}

	public boolean isGueltig() {
		return gueltig;
	}

	public void setGueltig(boolean gueltig) {
		this.gueltig = gueltig;
	}

	@Nullable
	public abstract Verfuegung getVerfuegung();

	public abstract void setVerfuegung(@Nullable Verfuegung verfuegung);

	@Nullable
	public abstract Verfuegung getVerfuegungPreview();

	public abstract void setVerfuegungPreview(@Nullable Verfuegung verfuegung);


	/**
	 *
	 * @return wenn der Status der Betreuung so ist dass eine definitive Verfuegung vorhanden ist
	 * gibt diese zurueck.
	 * Ansonsten wird der im verfuegungPreview gespeicherte werd zurueck gegeben
	 */
	@Nullable
	public Verfuegung getVerfuegungOrVerfuegungPreview() {
		if (getBetreuungsstatus().isAnyStatusOfVerfuegt()) {
			return getVerfuegung();
		}
		return getVerfuegungPreview();
	}

	public void initVorgaengerVerfuegungen(
		@Nullable Verfuegung vorgaenger,
		@Nullable  Verfuegung vorgaengerAusbezahlt
	) {
		this.vorgaengerVerfuegung = vorgaenger;
		this.vorgaengerInitialized = true;
	}

	/**
	 * @return die Verfuegung oder ausbezahlte Vorgaengerverfuegung dieser Betreuung
	 */
	@Nullable
	public Verfuegung getVerfuegungOrVorgaengerAusbezahlteVerfuegung() {
		if (getVerfuegung() != null) {
			return getVerfuegung();
		}
		return null;
	}

	@Nullable
	public Verfuegung getVorgaengerVerfuegung() {
		checkVorgaengerInitialized();
		return vorgaengerVerfuegung;
	}

	@SuppressWarnings("NonBooleanMethodNameMayNotStartWithQuestion")
	protected void checkVorgaengerInitialized() {
		Preconditions.checkState(
			vorgaengerInitialized,
			"must initialize transient fields of %s via VerfuegungService#initializeVorgaengerVerfuegungen",
			this);
	}

	/**
	 * Erstellt die BG-Nummer als zusammengesetzten String aus Jahr, FallId, KindId und BetreuungsNummer
	 */
	@Transient
	@SuppressFBWarnings("NM_CONFUSING")
	public String getBGNummer() {
		// some users like Institutionen don't have access to the Kind, so it must be proved that getKind() doesn't return null
		if (getKind().getGesuch() != null) {
			String kindNumberAsString = String.valueOf(getKind().getKindNummer());
			String betreuung = String.valueOf(getBetreuungNummer());
			return getKind().getGesuch().getJahrFallAndGemeindenummer() + '.' + kindNumberAsString + '.' + betreuung;
		}
		return "";
	}

	@Override
	public int compareTo(@Nonnull AbstractPlatz other) {
		CompareToBuilder compareToBuilder = new CompareToBuilder();
		compareToBuilder.append(this.getBetreuungNummer(), other.getBetreuungNummer());
		compareToBuilder.append(this.getId(), other.getId());
		return compareToBuilder.toComparison();
	}

	@Nonnull
	public AbstractPlatz copyAbstractPlatz(@Nonnull AbstractPlatz target, @Nonnull AntragCopyType copyType, @Nonnull KindContainer targetKindContainer) {
		super.copyAbstractEntity(target, copyType);

		switch (copyType) {
		case MUTATION:
			// Bereits verfuegte Betreuungen werden als BESTAETIGT kopiert, alle anderen behalten ihren Status
			if (this.getBetreuungsstatus().isGeschlossenJA()) {
				// Falls sämtliche Betreuungspensum-Container dieser Betreuung ein effektives Pensum von 0 haben, handelt es sich um die
				// Verfügung eines stornierten Platzes. Wir übernehmen diesen als "STORNIERT"
				if (hasAnyNonZeroPensum()) {
					target.setBetreuungsstatus(Betreuungsstatus.BESTAETIGT);
				} else {
					target.setBetreuungsstatus(Betreuungsstatus.STORNIERT);
				}
			} else {
				target.setBetreuungsstatus(this.getBetreuungsstatus());
			}
			target.setKind(targetKindContainer);
			target.setInstitutionStammdaten(this.getInstitutionStammdaten());
			target.setBetreuungNummer(this.getBetreuungNummer());
			break;
		case ERNEUERUNG:
		case MUTATION_NEUES_DOSSIER:
		case ERNEUERUNG_NEUES_DOSSIER:
			break;
		}
		return target;
	}

	protected boolean hasAnyNonZeroPensum() {
		return true;
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
		if (!(other instanceof AbstractPlatz)) {
			return false;
		}
		final AbstractPlatz otherBetreuung = (AbstractPlatz) other;
		return this.getInstitutionStammdaten().isSame(otherBetreuung.getInstitutionStammdaten());
	}

	@Nonnull
	@Transient
	public BetreuungsangebotTyp getBetreuungsangebotTyp() {
		return getInstitutionStammdaten().getBetreuungsangebotTyp();
	}

	@Transient
	public Gesuchsperiode extractGesuchsperiode() {
		Objects.requireNonNull(this.getKind(), "Can not extract Gesuchsperiode because Kind is null");
		Objects.requireNonNull(this.getKind().getGesuch(), "Can not extract Gesuchsperiode because Gesuch is null");
		return this.getKind().getGesuch().getGesuchsperiode();
	}

	@Transient
	public Gesuch extractGesuch() {
		Objects.requireNonNull(this.getKind(), "Can not extract Gesuch because Kind is null");
		return this.getKind().getGesuch();
	}

	@Nonnull
	@Transient
	public Gemeinde extractGemeinde() {
		return this.extractGesuch().extractGemeinde();
	}

	@Nonnull
	public String getInstitutionAndBetreuungsangebottyp(@Nonnull Locale locale) {
		String angebot = ServerMessageUtil
			.translateEnumValue(getBetreuungsangebotTyp(), locale);
		return getInstitutionStammdaten().getInstitution().getName() + " (" + angebot + ')';
	}

	@Nonnull
	@Override
	public String getSearchResultId() {
		return getId();
	}

	@Nonnull
	@Override
	public String getSearchResultSummary() {
		return getKind().getSearchResultSummary() + ' ' + getBGNummer();
	}

	@Nullable
	@Override
	public String getSearchResultAdditionalInformation() {
		return toString();
	}

	@Override
	public String getOwningGesuchId() {
		return extractGesuch().getId();
	}

	@Override
	public String getOwningFallId() {
		return extractGesuch().getFall().getId();
	}

	@Nullable
	@Override
	public String getOwningDossierId() {
		return extractGesuch().getDossier().getId();
	}
}
