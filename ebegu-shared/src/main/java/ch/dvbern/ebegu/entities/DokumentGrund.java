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

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.ForeignKey;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Transient;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import ch.dvbern.ebegu.enums.AntragCopyType;
import ch.dvbern.ebegu.enums.DokumentGrundPersonType;
import ch.dvbern.ebegu.enums.DokumentGrundTyp;
import ch.dvbern.ebegu.enums.DokumentTyp;
import ch.dvbern.ebegu.util.EbeguUtil;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.apache.commons.lang3.builder.CompareToBuilder;
import org.hibernate.envers.Audited;

import static ch.dvbern.ebegu.util.Constants.DB_DEFAULT_MAX_LENGTH;

/**
 * Entitaet zum Speichern von DokumentGrund in der Datenbank.
 */
@Audited
@Entity
public class DokumentGrund extends AbstractMutableEntity implements Comparable<DokumentGrund> {

	private static final long serialVersionUID = 5417585258130227434L;

	@NotNull
	@ManyToOne(optional = false)
	@JoinColumn(foreignKey = @ForeignKey(name = "FK_dokumentGrund_gesuch_id"), nullable = false)
	private Gesuch gesuch = null;

	@Enumerated(EnumType.STRING)
	@NotNull
	private DokumentGrundTyp dokumentGrundTyp = null;

	@Size(min = 1, max = DB_DEFAULT_MAX_LENGTH)
	@Column(nullable = true)
	@Nullable
	private String tag = null;

	@Enumerated(EnumType.STRING)
	@Nullable
	private DokumentGrundPersonType personType = null;

	@Nullable
	private Integer personNumber = null;

	@Enumerated(EnumType.STRING)
	@NotNull
	private DokumentTyp dokumentTyp = null;

	@Nonnull
	@Valid
	@OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, mappedBy = "dokumentGrund")
	private Set<Dokument> dokumente = new HashSet<>();

	// Marker, ob Dokument benötigt wird oder nicht. Nicht in DB
	@Transient
	private boolean needed = true;

	public DokumentGrund() {
	}

	public DokumentGrund(DokumentGrundTyp dokumentGrundTyp) {
		this.dokumentGrundTyp = dokumentGrundTyp;
		this.needed = !DokumentGrundTyp.isSonstigeOrPapiergesuch(dokumentGrundTyp);
	}

	public DokumentGrund(
		@Nonnull DokumentGrundTyp dokumentGrundTyp,
		@Nullable String tag,
		@Nullable DokumentGrundPersonType personType,
		@Nullable Integer personNumber) {

		this.dokumentGrundTyp = dokumentGrundTyp;
		this.needed = !DokumentGrundTyp.isSonstigeOrPapiergesuch(dokumentGrundTyp);
		this.tag = tag;
		this.personType = personType;
		this.personNumber = personNumber;
	}

	public DokumentGrund(@Nonnull DokumentGrundTyp dokumentGrundTyp, @Nonnull DokumentTyp dokumentTyp) {
		this(dokumentGrundTyp);
		this.dokumente = new HashSet<>();
		this.dokumentTyp = dokumentTyp;
	}

	public DokumentGrund(
		@Nonnull DokumentGrundTyp dokumentGrundTyp,
		@Nullable String tag,
		@Nullable DokumentGrundPersonType personType,
		@Nullable Integer personNumber,
		@Nonnull DokumentTyp dokumentTyp) {

		this(dokumentGrundTyp, tag, personType, personNumber);
		this.dokumente = new HashSet<>();
		this.dokumentTyp = dokumentTyp;
	}

	@Nonnull
	public Set<Dokument> getDokumente() {
		return dokumente;
	}

	public void setDokumente(@Nonnull Set<Dokument> dokumente) {
		this.dokumente = dokumente;
	}

	public Gesuch getGesuch() {
		return gesuch;
	}

	public void setGesuch(Gesuch gesuch) {
		this.gesuch = gesuch;
	}

	public DokumentGrundTyp getDokumentGrundTyp() {
		return dokumentGrundTyp;
	}

	public void setDokumentGrundTyp(DokumentGrundTyp dokumentGrundTyp) {
		this.dokumentGrundTyp = dokumentGrundTyp;
	}

	@Nullable
	public String getTag() {
		return tag;
	}

	public void setTag(@Nullable String tag) {
		this.tag = tag;
	}

	public DokumentTyp getDokumentTyp() {
		return dokumentTyp;
	}

	public void setDokumentTyp(DokumentTyp dokumentTyp) {
		this.dokumentTyp = dokumentTyp;
	}

	public boolean isNeeded() {
		return needed;
	}

	public void setNeeded(boolean needed) {
		this.needed = needed;
	}

	@Nullable
	public DokumentGrundPersonType getPersonType() {
		return personType;
	}

	public void setPersonType(@Nullable DokumentGrundPersonType personType) {
		this.personType = personType;
	}

	@Nullable
	public Integer getPersonNumber() {
		return personNumber;
	}

	public void setPersonNumber(@Nullable Integer personNumber) {
		this.personNumber = personNumber;
	}

	@Override
	public String toString() {
		return "DokumentGrund{" +
			"dokumentGrundTyp=" + dokumentGrundTyp +
			", year='" + tag + '\'' +
			", dokumente=" + dokumente +
			'}';
	}

	/**
	 * This methode compares both objects with all their attributes.
	 * WARNING! Never use it when trying to compare old DokumentGrund with new DokumentGrund.
	 * Since in the old data the fields personType and personNumber didn't exist, the comparison
	 * cannot be done with this methode.
	 */
	@Override
	public int compareTo(@Nonnull DokumentGrund o) {
		CompareToBuilder builder = new CompareToBuilder();
		builder.append(this.getDokumentGrundTyp(), o.getDokumentGrundTyp());
		builder.append(this.getDokumentTyp(), o.getDokumentTyp());
		if (this.getTag() != null && o.getTag() != null) {
			builder.append(this.getTag(), o.getTag());
		}
		if (this.getPersonNumber() != null && o.getPersonNumber() != null) {
			builder.append(this.getPersonNumber(), o.getPersonNumber());
		}
		if (this.getPersonType() != null && o.getPersonType() != null) {
			builder.append(this.getPersonType(), o.getPersonType());
		}
		return builder.toComparison();
	}

	public boolean isEmpty() {
		return getDokumente().size() <= 0;
	}

	@Nonnull
	public DokumentGrund copyDokumentGrund(@Nonnull DokumentGrund target, @Nonnull AntragCopyType copyType) {
		super.copyAbstractEntity(target, copyType);
		switch (copyType) {
		case MUTATION:
		case MUTATION_NEUES_DOSSIER:
			target.setDokumentGrundTyp(this.getDokumentGrundTyp());
			target.setTag(this.getTag());
			target.setPersonNumber(this.getPersonNumber());
			target.setPersonType(this.getPersonType());
			target.setDokumentTyp(this.getDokumentTyp());
			for (Dokument dokument : this.getDokumente()) {
				target.getDokumente().add(dokument.copyDokument(new Dokument(), copyType, target));
			}
			if (DokumentGrundTyp.isSonstigeOrPapiergesuch(this.getDokumentGrundTyp())) {
				target.setNeeded(false);
			} else {
				target.setNeeded(this.isNeeded());
			}
			break;
		case ERNEUERUNG:
		case ERNEUERUNG_NEUES_DOSSIER:
			break;
		}
		return target;
	}

	@Override
	@SuppressWarnings("PMD.CompareObjectsWithEquals")
	@SuppressFBWarnings("BC_UNCONFIRMED_CAST")
	public boolean isSame(AbstractEntity other) {
		//noinspection ObjectEquality
		if (this == other) {
			return true;
		}
		if (other == null || !getClass().equals(other.getClass())) {
			return false;
		}
		final DokumentGrund otherDokumentGrund = (DokumentGrund) other;
		return getDokumentGrundTyp() == otherDokumentGrund.getDokumentGrundTyp() &&
			Objects.equals(getTag(), otherDokumentGrund.getTag()) &&
			getPersonType() == otherDokumentGrund.getPersonType() &&
			Objects.equals(getPersonNumber(), otherDokumentGrund.getPersonNumber()) &&
			getDokumentTyp() == otherDokumentGrund.getDokumentTyp() &&
			EbeguUtil.areListsSameSize(getDokumente(), otherDokumentGrund.getDokumente());
	}
}
