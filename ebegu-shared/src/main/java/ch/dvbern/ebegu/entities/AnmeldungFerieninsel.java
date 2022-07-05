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

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.persistence.AssociationOverride;
import javax.persistence.AssociationOverrides;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ForeignKey;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import ch.dvbern.ebegu.enums.AntragCopyType;
import ch.dvbern.ebegu.enums.Eingangsart;
import ch.dvbern.ebegu.validators.CheckPlatzAndAngebottyp;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.apache.commons.lang.NotImplementedException;
import org.hibernate.envers.Audited;

/**
 * Anmeldung für Ferieninsel
 */
@Audited
@Entity
@CheckPlatzAndAngebottyp
// Der ForeignKey-Name wird leider nicht richtig generiert, muss von Hand angepasst werden!
@AssociationOverrides({
	@AssociationOverride(name="kind", joinColumns=@JoinColumn(name="kind_id"), foreignKey = @ForeignKey(name = "FK_anmeldung_ferieninsel_kind_id")),
	@AssociationOverride(name="institutionStammdaten", joinColumns=@JoinColumn(name="institutionStammdaten_id"), foreignKey = @ForeignKey(name = "FK_anmeldung_ferieninsel_institution_stammdaten_id"))
})

@Table(
	uniqueConstraints =
	@UniqueConstraint(columnNames = { "betreuungNummer", "kind_id" }, name = "UK_anmeldung_ferieninsel_kind_betreuung_nummer")
)
public class AnmeldungFerieninsel extends AbstractAnmeldung {

	private static final long serialVersionUID = -9037857320548372570L;

	@Nullable
	@OneToOne(optional = true, cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
	@JoinColumn(foreignKey = @ForeignKey(name = "FK_anmeldung_ferieninsel_belegung_ferieninsel_id"), nullable = true)
	private BelegungFerieninsel belegungFerieninsel;


	public AnmeldungFerieninsel() {
	}


	@Nullable
	public BelegungFerieninsel getBelegungFerieninsel() {
		return belegungFerieninsel;
	}

	public void setBelegungFerieninsel(@Nullable BelegungFerieninsel belegungFerieninsel) {
		this.belegungFerieninsel = belegungFerieninsel;
	}

	@Nullable
	@Override
	public Verfuegung getVerfuegung() {
		throw new NotImplementedException("Ferieninseln werden aktuell nicht verfügt");
	}

	@Override
	public void setVerfuegung(@Nullable Verfuegung verfuegung) {
		throw new NotImplementedException("Ferieninseln werden aktuell nicht verfügt");
	}

	@Nullable
	@Override
	public Verfuegung getVerfuegungPreview() {
		throw new NotImplementedException("Ferieninseln werden aktuell nicht berechnet");
	}

	@Override
	public void setVerfuegungPreview(@Nullable Verfuegung verfuegung) {
		throw new NotImplementedException("Ferieninseln werden aktuell nicht berechnet");
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
		if (!super.isSame(other)) {
			return false;
		}
		final AnmeldungFerieninsel otherBetreuung = (AnmeldungFerieninsel) other;
		return Objects.equals(this.getBelegungFerieninsel(), otherBetreuung.getBelegungFerieninsel());
	}

	@Nonnull
	public AnmeldungFerieninsel copyAnmeldungFerieninsel(
		@Nonnull AnmeldungFerieninsel target,
		@Nonnull AntragCopyType copyType,
		@Nonnull KindContainer targetKindContainer,
		@Nonnull Eingangsart targetEingangsart
	) {
		super.copyAbstractAnmeldung(target, copyType, targetKindContainer, targetEingangsart);
		switch (copyType) {
		case MUTATION:
			if (belegungFerieninsel != null) {
				target.setBelegungFerieninsel(belegungFerieninsel.copyBelegungFerieninsel(new BelegungFerieninsel(), copyType));
			}
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
		if (this.getBetreuungsstatus() != betreuung.getBetreuungsstatus() && betreuung instanceof AnmeldungFerieninsel) {
			AnmeldungFerieninsel that = (AnmeldungFerieninsel) betreuung;
			if (that.getBelegungFerieninsel() != null) {
				this.setBelegungFerieninsel(that.getBelegungFerieninsel().copyBelegungFerieninsel(new BelegungFerieninsel(), AntragCopyType.MUTATION));
			}
		}
	}
}
