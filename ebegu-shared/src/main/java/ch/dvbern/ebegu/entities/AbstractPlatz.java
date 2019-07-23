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
import javax.persistence.Column;
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

import ch.dvbern.ebegu.enums.AntragCopyType;
import ch.dvbern.ebegu.enums.BetreuungsangebotTyp;
import ch.dvbern.ebegu.util.ServerMessageUtil;
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
public abstract class AbstractPlatz extends AbstractMutableEntity implements Comparable<AbstractPlatz> {

	private static final long serialVersionUID = -9037857320548372570L;

	@NotNull
	@ManyToOne(optional = false)
	@JoinColumn(foreignKey = @ForeignKey(name = "FK_platz_kind_id"), nullable = false)
	private KindContainer kind;

	@NotNull
	@ManyToOne(optional = false)
	@JoinColumn(foreignKey = @ForeignKey(name = "FK_platz_institution_stammdaten_id"), nullable = false)
	private InstitutionStammdaten institutionStammdaten;

	@NotNull
	@Min(1)
	@Column(nullable = false)
	private Integer betreuungNummer = 1;

	@Column(nullable = false)
	private boolean gueltig = false;


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
			target.setKind(targetKindContainer);
			target.setInstitutionStammdaten(this.getInstitutionStammdaten());
			target.setBetreuungNummer(this.getBetreuungNummer());
			target.setGueltig(false);
			break;
		case ERNEUERUNG:
		case MUTATION_NEUES_DOSSIER:
		case ERNEUERUNG_NEUES_DOSSIER:
			break;
		}
		return target;
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
	public String getInstitutionAndBetreuungsangebottyp(@Nonnull Locale locale) {
		String angebot = ServerMessageUtil
			.translateEnumValue(getBetreuungsangebotTyp(), locale);
		return getInstitutionStammdaten().getInstitution().getName() + " (" + angebot + ')';
	}
}
