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

import java.util.Objects;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.persistence.AssociationOverride;
import javax.persistence.AssociationOverrides;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ForeignKey;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;
import javax.validation.Valid;

import ch.dvbern.ebegu.enums.AntragCopyType;
import ch.dvbern.ebegu.enums.Betreuungsstatus;
import ch.dvbern.ebegu.enums.Eingangsart;
import ch.dvbern.ebegu.validators.CheckPlatzAndAngebottyp;
import org.hibernate.envers.Audited;

/**
 * Anmeldung für Tagesschule
 */
@Audited
@Entity
@CheckPlatzAndAngebottyp
// Der ForeignKey-Name wird leider nicht richtig generiert, muss von Hand angepasst werden!
@AssociationOverrides({
	@AssociationOverride(name = "kind", joinColumns = @JoinColumn(name = "kind_id"), foreignKey = @ForeignKey(name = "FK_anmeldung_tagesschule_kind_id")),
	@AssociationOverride(name="institutionStammdaten", joinColumns=@JoinColumn(name="institutionStammdaten_id"), foreignKey = @ForeignKey(name = "FK_anmeldung_tagesschule_institution_stammdaten_id"))
})
@Table(
	uniqueConstraints =
	@UniqueConstraint(columnNames = { "betreuungNummer", "kind_id" }, name = "UK_anmeldung_tagesschule_kind_betreuung_nummer")
)
public class AnmeldungTagesschule extends AbstractAnmeldung {

	private static final long serialVersionUID = -9037857320548372570L;

	@Nullable
	@OneToOne(optional = true, cascade = CascadeType.ALL, orphanRemoval = true)
	@JoinColumn(foreignKey = @ForeignKey(name = "FK_anmeldung_tagesschule_belegung_tagesschule_id"), nullable = true)
	private BelegungTagesschule belegungTagesschule;

	@Column(nullable = false)
	private boolean keineDetailinformationen = false;

	@Nullable
	@Valid
	@OneToOne(optional = true, cascade = CascadeType.REMOVE, orphanRemoval = true, mappedBy = "anmeldungTagesschule")
	private Verfuegung verfuegung;

	@Transient
	@Nullable
	private Verfuegung verfuegungPreview;


	public AnmeldungTagesschule() {
	}


	@Nullable
	public BelegungTagesschule getBelegungTagesschule() {
		return belegungTagesschule;
	}

	public void setBelegungTagesschule(@Nullable BelegungTagesschule belegungTagesschule) {
		this.belegungTagesschule = belegungTagesschule;
	}

	public boolean isKeineDetailinformationen() {
		return keineDetailinformationen;
	}

	public void setKeineDetailinformationen(boolean keineDetailinformationen) {
		this.keineDetailinformationen = keineDetailinformationen;
	}

	@Override
	@Nullable
	public Verfuegung getVerfuegung() {
		return verfuegung;
	}

	@Override
	public void setVerfuegung(@Nullable Verfuegung verfuegung) {
		this.verfuegung = verfuegung;
	}

	@Override
	@Nullable
	public Verfuegung getVerfuegungPreview() {
		return verfuegungPreview;
	}

	@Override
	public void setVerfuegungPreview(@Nullable Verfuegung verfuegungPreview) {
		this.verfuegungPreview = verfuegungPreview;
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
		if (!(other instanceof AnmeldungTagesschule)) {
			return false;
		}
		final AnmeldungTagesschule otherBetreuung = (AnmeldungTagesschule) other;
		return Objects.equals(this.getBelegungTagesschule(), otherBetreuung.getBelegungTagesschule());
	}

	@Nonnull
	public AnmeldungTagesschule copyAnmeldungTagesschule(
		@Nonnull AnmeldungTagesschule target,
		@Nonnull AntragCopyType copyType,
		@Nonnull KindContainer targetKindContainer,
		@Nonnull Eingangsart targetEingangsart
	) {
		super.copyAbstractAnmeldung(target, copyType, targetKindContainer, targetEingangsart);
		switch (copyType) {
		case MUTATION:
			if (belegungTagesschule != null) {
				target.setBelegungTagesschule(belegungTagesschule.copyBelegungTagesschule(new BelegungTagesschule(), copyType));
			}
			target.setKeineDetailinformationen(this.isKeineDetailinformationen());
			if (target.isKeineDetailinformationen()) {
				// eine Anmeldung ohne Detailinformationen muss immer als Uebernommen gespeichert werden
				target.setBetreuungsstatus(Betreuungsstatus.SCHULAMT_ANMELDUNG_UEBERNOMMEN);
			}
			target.setVerfuegung(null);
			break;
		case ERNEUERUNG:
		case MUTATION_NEUES_DOSSIER:
		case ERNEUERUNG_NEUES_DOSSIER:
			break;
		}
		return target;
	}

	@Override
	public void copyAnmeldung(@Nonnull AbstractAnmeldung betreuung) {
		super.copyAnmeldung(betreuung);
		if (this.getBetreuungsstatus() != betreuung.getBetreuungsstatus() && betreuung instanceof AnmeldungTagesschule) {
			AnmeldungTagesschule that = (AnmeldungTagesschule) betreuung;
			this.setKeineDetailinformationen(that.isKeineDetailinformationen());
			if (that.getBelegungTagesschule() != null) {
				this.setBelegungTagesschule(that.getBelegungTagesschule().copyBelegungTagesschule(new BelegungTagesschule(), AntragCopyType.MUTATION));
			}
		}
	}

	public boolean isTagesschuleTagi() {
		// Bei Keine-Detailinformationen gehen wir davon aus, dass es eine normale Tagesschule ist
		if (isKeineDetailinformationen()) {
			return false;
		}
		final InstitutionStammdatenTagesschule stammdatenTagesschule = this.getInstitutionStammdaten().getInstitutionStammdatenTagesschule();
		Objects.requireNonNull(stammdatenTagesschule);
		final Set<EinstellungenTagesschule> einstellungenTagesschule = stammdatenTagesschule.getEinstellungenTagesschule();
		for (EinstellungenTagesschule einstellung : einstellungenTagesschule) {
			if (einstellung.getGesuchsperiode().equals(this.extractGesuchsperiode())) {
				return einstellung.isTagi();
			}
		}
		return false;
	}
}
