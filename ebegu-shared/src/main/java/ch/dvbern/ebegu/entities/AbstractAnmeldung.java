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

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.persistence.Column;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.MappedSuperclass;

import ch.dvbern.ebegu.enums.AnmeldungMutationZustand;
import ch.dvbern.ebegu.enums.AntragCopyType;
import ch.dvbern.ebegu.enums.Eingangsart;
import org.hibernate.envers.Audited;

/**
 * Superklasse fuer Schulamt Anmeldungen: Tagesschule oder Ferieninsel
 */
@MappedSuperclass
@Audited
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
public abstract class AbstractAnmeldung extends AbstractPlatz {

	private static final long serialVersionUID = -9037857320548372570L;

	@Nullable
	@Enumerated(EnumType.STRING)
	@Column(nullable = true)
	private AnmeldungMutationZustand anmeldungMutationZustand = AnmeldungMutationZustand.NOCH_NICHT_FREIGEGEBEN;


	public AbstractAnmeldung() {
	}


	@Nullable
	public AnmeldungMutationZustand getAnmeldungMutationZustand() {
		return anmeldungMutationZustand;
	}

	public void setAnmeldungMutationZustand(@Nullable AnmeldungMutationZustand anmeldungMutationZustand) {
		this.anmeldungMutationZustand = anmeldungMutationZustand;
	}

	public AbstractAnmeldung copyAbstractAnmeldung(
		@Nonnull AbstractAnmeldung target,
		@Nonnull AntragCopyType copyType,
		@Nonnull KindContainer targetKindContainer,
		@Nonnull Eingangsart targetEingangsart
	) {
		super.copyAbstractPlatz(target, copyType, targetKindContainer);
		switch (copyType) {
		case MUTATION:
			if (targetEingangsart == Eingangsart.ONLINE) {
				target.setAnmeldungMutationZustand(AnmeldungMutationZustand.NOCH_NICHT_FREIGEGEBEN);
				this.setAnmeldungMutationZustand(AnmeldungMutationZustand.AKTUELLE_ANMELDUNG);
			} else {
				target.setAnmeldungMutationZustand(AnmeldungMutationZustand.AKTUELLE_ANMELDUNG);
				target.setGueltig(true); // Bei Anmeldungen ist immer die neueste "gültig"
				this.setAnmeldungMutationZustand(AnmeldungMutationZustand.MUTIERT);
				this.setGueltig(false);
			}
			break;
		case ERNEUERUNG:
		case MUTATION_NEUES_DOSSIER:
		case ERNEUERUNG_NEUES_DOSSIER:
			break;
		}
		return target;
	}

	public void copyAnmeldung(@Nonnull AbstractAnmeldung betreuung) {
		if (this.getBetreuungsstatus() != betreuung.getBetreuungsstatus()) {
			this.setBetreuungsstatus(betreuung.getBetreuungsstatus());
			this.setInstitutionStammdaten(betreuung.getInstitutionStammdaten());
		}
	}
}
