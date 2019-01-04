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

import java.util.Objects;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.ForeignKey;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import ch.dvbern.ebegu.enums.AntragCopyType;
import ch.dvbern.ebegu.enums.EinschulungTyp;
import ch.dvbern.ebegu.enums.Kinderabzug;
import ch.dvbern.ebegu.util.EbeguUtil;
import org.hibernate.envers.Audited;

/**
 * Entity fuer Kinder.
 */
@Audited
@Entity
@Table(
	indexes = @Index(columnList = "geburtsdatum", name = "IX_kind_geburtsdatum")
)
public class Kind extends AbstractPersonEntity {

	private static final long serialVersionUID = -9032257320578372570L;

	@NotNull
	@Column(nullable = false)
	@Enumerated(EnumType.STRING)
	private Kinderabzug kinderabzug;

	@Column(nullable = false)
	@NotNull
	private Boolean familienErgaenzendeBetreuung = false;

	@Column(nullable = true)
	@Nullable
	private Boolean sprichtAmtssprache;

	@Column(nullable = true)
	@Nullable
	@Enumerated(EnumType.STRING)
	private EinschulungTyp einschulungTyp;

	@Valid
	@Nullable
	@OneToOne(optional = true, cascade = CascadeType.ALL, orphanRemoval = true)
	@JoinColumn(foreignKey = @ForeignKey(name = "FK_kind_pensum_fachstelle_id"), nullable = true)
	private PensumFachstelle pensumFachstelle;

	@Valid
	@Nullable
	@OneToOne(optional = true, cascade = CascadeType.ALL, orphanRemoval = true)
	@JoinColumn(foreignKey = @ForeignKey(name = "FK_kind_pensum_ausserordentlicheranspruch_id"), nullable = true)
	private PensumAusserordentlicherAnspruch pensumAusserordentlicherAnspruch;


	public Kind() {
	}

	public Kinderabzug getKinderabzug() {
		return kinderabzug;
	}

	public void setKinderabzug(Kinderabzug kinderabzug) {
		this.kinderabzug = kinderabzug;
	}

	public Boolean getFamilienErgaenzendeBetreuung() {
		return familienErgaenzendeBetreuung;
	}

	public void setFamilienErgaenzendeBetreuung(Boolean familienErgaenzendeBetreuung) {
		this.familienErgaenzendeBetreuung = familienErgaenzendeBetreuung;
	}

	@Nullable
	public Boolean getSprichtAmtssprache() {
		return sprichtAmtssprache;
	}

	public void setSprichtAmtssprache(@Nullable Boolean sprichtAmtssprache) {
		this.sprichtAmtssprache = sprichtAmtssprache;
	}

	@Nullable
	public EinschulungTyp getEinschulungTyp() {
		return einschulungTyp;
	}

	public void setEinschulungTyp(@Nullable EinschulungTyp einschulungTyp) {
		this.einschulungTyp = einschulungTyp;
	}

	@Nullable
	public PensumFachstelle getPensumFachstelle() {
		return pensumFachstelle;
	}

	public void setPensumFachstelle(@Nullable PensumFachstelle pensumFachstelle) {
		this.pensumFachstelle = pensumFachstelle;
	}

	@Nullable
	public PensumAusserordentlicherAnspruch getPensumAusserordentlicherAnspruch() {
		return pensumAusserordentlicherAnspruch;
	}

	public void setPensumAusserordentlicherAnspruch(
		@Nullable PensumAusserordentlicherAnspruch pensumAusserordentlicherAnspruch) {

		this.pensumAusserordentlicherAnspruch = pensumAusserordentlicherAnspruch;
	}

	@Nonnull
	public Kind copyKind(
		@Nonnull Kind target,
		@Nonnull AntragCopyType copyType,
		@Nonnull Gesuchsperiode gesuchsperiode) {
		super.copyAbstractPersonEntity(target, copyType);
		target.setKinderabzug(this.getKinderabzug());
		target.setFamilienErgaenzendeBetreuung(this.getFamilienErgaenzendeBetreuung());
		target.setSprichtAmtssprache(this.getSprichtAmtssprache());

		switch (copyType) {
		case MUTATION:
			target.setEinschulungTyp(this.getEinschulungTyp());
			copyFachstelle(target, copyType);
			copyAusserordentlicherAnspruch(target, copyType);
			break;
		case MUTATION_NEUES_DOSSIER:
			target.setEinschulungTyp(this.getEinschulungTyp());
			copyFachstelleIfStillValid(target, copyType, gesuchsperiode);
			copyAusserordentlicherAnspruchIfStillValid(target, copyType, gesuchsperiode);
			break;
		case ERNEUERUNG:
		case ERNEUERUNG_NEUES_DOSSIER:
			copyFachstelleIfStillValid(target, copyType, gesuchsperiode);
			copyAusserordentlicherAnspruchIfStillValid(target, copyType, gesuchsperiode);
			break;
		}
		return target;
	}

	private void copyFachstelle(@Nonnull Kind target, @Nonnull AntragCopyType copyType) {
		if (this.getPensumFachstelle() != null) {
			target.setPensumFachstelle(this.getPensumFachstelle()
				.copyPensumFachstelle(new PensumFachstelle(), copyType));
		}
	}

	private void copyFachstelleIfStillValid(
		@Nonnull Kind target,
		@Nonnull AntragCopyType copyType,
		@Nonnull Gesuchsperiode gesuchsperiode) {
		// Fachstelle nur kopieren, wenn sie noch gueltig ist
		if (this.getPensumFachstelle() != null && !this.getPensumFachstelle()
			.getGueltigkeit()
			.endsBefore(gesuchsperiode.getGueltigkeit().getGueltigAb())) {
			target.setPensumFachstelle(this.getPensumFachstelle()
				.copyPensumFachstelle(new PensumFachstelle(), copyType));
		}
	}

	private void copyAusserordentlicherAnspruch(@Nonnull Kind target, @Nonnull AntragCopyType copyType) {
		if (this.getPensumAusserordentlicherAnspruch() != null) {
			target.setPensumAusserordentlicherAnspruch(this.getPensumAusserordentlicherAnspruch()
				.copyPensumAusserordentlicherAnspruch(new PensumAusserordentlicherAnspruch(), copyType));
		}
	}

	private void copyAusserordentlicherAnspruchIfStillValid(
		@Nonnull Kind target,
		@Nonnull AntragCopyType copyType,
		@Nonnull Gesuchsperiode gesuchsperiode) {
		// Anspruch nur kopieren, wenn er noch gueltig ist
		if (this.getPensumAusserordentlicherAnspruch() != null && !this.getPensumAusserordentlicherAnspruch()
			.getGueltigkeit()
			.endsBefore(gesuchsperiode.getGueltigkeit().getGueltigAb())) {
			target.setPensumAusserordentlicherAnspruch(this.getPensumAusserordentlicherAnspruch()
				.copyPensumAusserordentlicherAnspruch(new PensumAusserordentlicherAnspruch(), copyType));
		}
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
		if (!super.isSame(other)) {
			return false;
		}
		if (!(other instanceof Kind)) {
			return false;
		}
		final Kind otherKind = (Kind) other;
		return getKinderabzug() == otherKind.getKinderabzug() &&
			Objects.equals(getFamilienErgaenzendeBetreuung(), otherKind.getFamilienErgaenzendeBetreuung()) &&
			Objects.equals(getSprichtAmtssprache(), otherKind.getSprichtAmtssprache()) &&
			Objects.equals(getEinschulungTyp(), otherKind.getEinschulungTyp()) &&
			EbeguUtil.isSameObject(getPensumFachstelle(), otherKind.getPensumFachstelle()) &&
			EbeguUtil.isSameObject(getPensumAusserordentlicherAnspruch(),
				otherKind.getPensumAusserordentlicherAnspruch());
	}
}
