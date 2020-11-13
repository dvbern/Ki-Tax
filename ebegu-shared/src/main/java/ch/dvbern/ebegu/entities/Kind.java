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
import javax.validation.constraints.Pattern;
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
import ch.dvbern.ebegu.util.Constants;
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

	@Column(nullable = true)
	@Nullable
	@Enumerated(EnumType.STRING)
	private Kinderabzug kinderabzugErstesHalbjahr;

	@Column(nullable = true)
	@Nullable
	@Enumerated(EnumType.STRING)
	private Kinderabzug kinderabzugZweitesHalbjahr;

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

	@Column(nullable = true)
	@Nullable
	private Boolean ausAsylwesen;

	@Column(nullable = true)
	@Nullable
	@Pattern(regexp = Constants.REGEX_ZEMIS, message = "{validator.constraints.zemis.message}")
	private String zemisNummer;

	public Kind() {
	}

	@Nullable
	public Kinderabzug getKinderabzugErstesHalbjahr() {
		return kinderabzugErstesHalbjahr;
	}

	public void setKinderabzugErstesHalbjahr(@Nullable Kinderabzug kinderabzugErstesHalbjahr) {
		this.kinderabzugErstesHalbjahr = kinderabzugErstesHalbjahr;
	}

	@Nullable
	public Kinderabzug getKinderabzugZweitesHalbjahr() {
		return kinderabzugZweitesHalbjahr;
	}

	public void setKinderabzugZweitesHalbjahr(@Nullable Kinderabzug kinderabzugZweitesHalbjahr) {
		this.kinderabzugZweitesHalbjahr = kinderabzugZweitesHalbjahr;
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

	@Nullable
	public Boolean getAusAsylwesen() {
		return ausAsylwesen;
	}

	public void setAusAsylwesen(@Nullable Boolean ausAsylwesen) {
		this.ausAsylwesen = ausAsylwesen;
	}

	@Nullable
	public String getZemisNummer() {
		return zemisNummer;
	}

	public void setZemisNummer(@Nullable String zemisNummer) {
		this.zemisNummer = zemisNummer;
	}

	@Nonnull
	public Kind copyKind(
		@Nonnull Kind target,
		@Nonnull AntragCopyType copyType,
		@Nonnull Gesuchsperiode gesuchsperiode) {
		super.copyAbstractPersonEntity(target, copyType);
		target.setFamilienErgaenzendeBetreuung(this.getFamilienErgaenzendeBetreuung());
		target.setSprichtAmtssprache(this.getSprichtAmtssprache());
		target.setAusAsylwesen(this.getAusAsylwesen());
		target.setZemisNummer(this.getZemisNummer());

		switch (copyType) {
		case MUTATION:
			target.setEinschulungTyp(this.getEinschulungTyp());
			target.setKinderabzugErstesHalbjahr(this.getKinderabzugErstesHalbjahr());
			target.setKinderabzugZweitesHalbjahr(this.getKinderabzugZweitesHalbjahr());
			copyFachstelle(target, copyType);
			copyAusserordentlicherAnspruch(target, copyType);
			break;
		case MUTATION_NEUES_DOSSIER:
			target.setEinschulungTyp(this.getEinschulungTyp());
			target.setKinderabzugErstesHalbjahr(this.getKinderabzugErstesHalbjahr());
			target.setKinderabzugZweitesHalbjahr(this.getKinderabzugZweitesHalbjahr());
			copyFachstelleIfStillValid(target, copyType, gesuchsperiode);
			// Ausserordentlicher Anspruch wird nicht kopiert, auch wenn er noch gueltig waere.
			// Dieser liegt ja in der Kompetenz der Gemeinde und kann nicht uebernommen werden
			break;
		case ERNEUERUNG:
		case ERNEUERUNG_NEUES_DOSSIER:
			// Ausserordentlicher Anspruch wird nicht kopiert, auch wenn er noch gueltig waere.
			// Dieser muss immer neu beantragt werden!
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
		return getKinderabzugErstesHalbjahr() == otherKind.getKinderabzugErstesHalbjahr() &&
			getKinderabzugZweitesHalbjahr() == otherKind.getKinderabzugZweitesHalbjahr() &&
			Objects.equals(getFamilienErgaenzendeBetreuung(), otherKind.getFamilienErgaenzendeBetreuung()) &&
			Objects.equals(getSprichtAmtssprache(), otherKind.getSprichtAmtssprache()) &&
			getEinschulungTyp() == otherKind.getEinschulungTyp() &&
			EbeguUtil.isSame(getPensumFachstelle(), otherKind.getPensumFachstelle()) &&
			EbeguUtil.isSame(getPensumAusserordentlicherAnspruch(),
				otherKind.getPensumAusserordentlicherAnspruch());
	}

	public boolean isGeprueft() {
		return kinderabzugErstesHalbjahr != null;
	}
}
