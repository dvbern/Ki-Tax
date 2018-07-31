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
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
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
	indexes = {
		@Index(columnList = "geburtsdatum", name = "IX_kind_geburtsdatum")
	})
public class Kind extends AbstractPersonEntity {

	private static final long serialVersionUID = -9032257320578372570L;

	@Max(100)
	@Min(0)
	@Nullable
	@Column(nullable = true)
	private Integer wohnhaftImGleichenHaushalt;

	@NotNull
	@Column(nullable = false)
	@Enumerated(EnumType.STRING)
	private Kinderabzug kinderabzug;

	@Column(nullable = false)
	@NotNull
	private Boolean familienErgaenzendeBetreuung = false;

	@Column(nullable = true)
	@Nullable
	private Boolean mutterspracheDeutsch;

	@Column(nullable = true)
	@Nullable
	@Enumerated(EnumType.STRING)
	private EinschulungTyp einschulungTyp;

	@Valid
	@Nullable
	@OneToOne(optional = true, cascade = CascadeType.ALL, orphanRemoval = true)
	@JoinColumn(foreignKey = @ForeignKey(name = "FK_kind_pensum_fachstelle_id"), nullable = true)
	private PensumFachstelle pensumFachstelle;

	public Kind() {
	}

	@Nullable
	public Integer getWohnhaftImGleichenHaushalt() {
		return wohnhaftImGleichenHaushalt;
	}

	public void setWohnhaftImGleichenHaushalt(@Nullable Integer wohnhaftImGleichenHaushalt) {
		this.wohnhaftImGleichenHaushalt = wohnhaftImGleichenHaushalt;
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
	public Boolean getMutterspracheDeutsch() {
		return mutterspracheDeutsch;
	}

	public void setMutterspracheDeutsch(@Nullable Boolean mutterspracheDeutsch) {
		this.mutterspracheDeutsch = mutterspracheDeutsch;
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



	public Kind copyKind(@Nonnull Kind target, @Nonnull AntragCopyType copyType, @Nonnull Gesuchsperiode gesuchsperiode) {
		super.copyAbstractPersonEntity(target, copyType);
		target.setWohnhaftImGleichenHaushalt(this.getWohnhaftImGleichenHaushalt());
		target.setKinderabzug(this.getKinderabzug());
		target.setFamilienErgaenzendeBetreuung(this.getFamilienErgaenzendeBetreuung());
		target.setMutterspracheDeutsch(this.getMutterspracheDeutsch());

		switch (copyType) {
		case MUTATION:
			target.setEinschulungTyp(this.getEinschulungTyp());
			copyFachstelle(target, copyType);
			break;
		case MUTATION_NEUES_DOSSIER:
			target.setEinschulungTyp(this.getEinschulungTyp());
			copyFachstelleIfStillValid(target, copyType, gesuchsperiode);
			break;
		case ERNEUERUNG:
			copyFachstelleIfStillValid(target, copyType, gesuchsperiode);
			break;
		}
		return target;
	}

	private void copyFachstelle(@Nonnull Kind target, @Nonnull AntragCopyType copyType) {
		if (this.getPensumFachstelle() != null) {
			target.setPensumFachstelle(this.getPensumFachstelle().copyForMutation(new PensumFachstelle()));
		}
	}

	private void copyFachstelleIfStillValid(@Nonnull Kind target, @Nonnull AntragCopyType copyType, @Nonnull Gesuchsperiode gesuchsperiode) {
		if (this.getPensumFachstelle() != null) {
			// Fachstelle nur kopieren, wenn sie noch gueltig ist
			if (!this.getPensumFachstelle().getGueltigkeit().endsBefore(gesuchsperiode.getGueltigkeit().getGueltigAb())) {
				target.setPensumFachstelle(this.getPensumFachstelle().copyForErneuerung(new PensumFachstelle()));
			}
		}
	}


//	@Nonnull
//	public Kind copyForMutation(@Nonnull Kind mutation) {
//		if (this.getPensumFachstelle() != null) {
//			mutation.setPensumFachstelle(this.getPensumFachstelle().copyForMutation(new PensumFachstelle()));
//		}
////		mutation.setEinschulungTyp(this.getEinschulungTyp());
////		return copyForMutationOrErneuerung(mutation);
//	}
//
//	@SuppressWarnings("PMD.CollapsibleIfStatements")
//	@Nonnull
//	public Kind copyForErneuerung(@Nonnull Kind folgegesuchKind, @Nonnull Gesuchsperiode gesuchsperiodeFolgegesuch) {
//		if (this.getPensumFachstelle() != null) {
//			// Fachstelle nur kopieren, wenn sie noch gueltig ist
//			if (!this.getPensumFachstelle().getGueltigkeit().endsBefore(gesuchsperiodeFolgegesuch.getGueltigkeit().getGueltigAb())) {
//				folgegesuchKind.setPensumFachstelle(this.getPensumFachstelle().copyForErneuerung(new PensumFachstelle()));
//			}
//		}
//		// Beim Erneuerungsgesuch wird der EinschulungTyp NICHT kopiert
////		return copyForMutationOrErneuerung(folgegesuchKind);
//	}

//	@Nonnull
//	private Kind copyForMutationOrErneuerung(@Nonnull Kind mutation) {
//		mutation.setWohnhaftImGleichenHaushalt(this.getWohnhaftImGleichenHaushalt());
//		mutation.setKinderabzug(this.getKinderabzug());
//		mutation.setFamilienErgaenzendeBetreuung(this.getFamilienErgaenzendeBetreuung());
//		mutation.setMutterspracheDeutsch(this.getMutterspracheDeutsch());
//		return mutation;
//	}

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
		return Objects.equals(getWohnhaftImGleichenHaushalt(), otherKind.getWohnhaftImGleichenHaushalt()) &&
			getKinderabzug() == otherKind.getKinderabzug() &&
			Objects.equals(getFamilienErgaenzendeBetreuung(), otherKind.getFamilienErgaenzendeBetreuung()) &&
			Objects.equals(getMutterspracheDeutsch(), otherKind.getMutterspracheDeutsch()) &&
			Objects.equals(getEinschulungTyp(), otherKind.getEinschulungTyp()) &&
			EbeguUtil.isSameObject(getPensumFachstelle(), otherKind.getPensumFachstelle());
	}
}
