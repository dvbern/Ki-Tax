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
import java.util.Set;
import java.util.TreeSet;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.OneToMany;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import ch.dvbern.ebegu.enums.AbholungTagesschule;
import ch.dvbern.ebegu.enums.AntragCopyType;
import ch.dvbern.ebegu.util.Constants;
import org.hibernate.annotations.SortNatural;
import org.hibernate.envers.Audited;

import static ch.dvbern.ebegu.util.Constants.DB_DEFAULT_MAX_LENGTH;

/**
 * Entity for the Belegung of the Tageschulangebote in a Betreuung.
 */
@Audited
@Entity
public class BelegungTagesschule extends AbstractMutableEntity {

	private static final long serialVersionUID = -8403435739182708718L;

	@OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, mappedBy = "belegungTagesschule")
	@SortNatural
	@Nonnull
	private Set<BelegungTagesschuleModul> belegungTagesschuleModule = new TreeSet<>();

	@NotNull
	@Column(nullable = false)
	private LocalDate eintrittsdatum;

	@Size(min = 1, max = DB_DEFAULT_MAX_LENGTH)
	@Nullable
	@Column
	private String planKlasse;

	@Enumerated(EnumType.STRING)
	@Nullable
	@Column
	private AbholungTagesschule abholungTagesschule;

	@Size(max = Constants.DB_TEXTAREA_LENGTH)
	@Nullable
	@Column(nullable = true, length = Constants.DB_TEXTAREA_LENGTH)
	private String bemerkung;

	@Column(nullable = false)
	private boolean abweichungZweitesSemester = false;

	@Override
	public boolean isSame(AbstractEntity other) {
		//noinspection ObjectEquality
		if (this == other) {
			return true;
		}
		if (other == null || !getClass().equals(other.getClass())) {
			return false;
		}
		//noinspection RedundantIfStatement
		if (!(other instanceof BelegungTagesschule)) {
			return false;
		}
		return true;
	}

	@Nonnull
	public Set<BelegungTagesschuleModul> getBelegungTagesschuleModule() {
		return belegungTagesschuleModule;
	}

	public void setBelegungTagesschuleModule(@Nonnull Set<BelegungTagesschuleModul> belegungTagesschuleModule) {
		this.belegungTagesschuleModule = belegungTagesschuleModule;
	}

	@NotNull
	public LocalDate getEintrittsdatum() {
		return eintrittsdatum;
	}

	public void setEintrittsdatum(@NotNull LocalDate eintrittsdatum) {
		this.eintrittsdatum = eintrittsdatum;
	}

	@Nullable
	public String getPlanKlasse() {
		return planKlasse;
	}

	public void setPlanKlasse(@Nullable String planKlasse) {
		this.planKlasse = planKlasse;
	}

	@Nullable
	public AbholungTagesschule getAbholungTagesschule() {
		return abholungTagesschule;
	}

	public void setAbholungTagesschule(@Nullable AbholungTagesschule abholungTagesschule) {
		this.abholungTagesschule = abholungTagesschule;
	}

	@Nullable
	public String getBemerkung() {
		return bemerkung;
	}

	public void setBemerkung(@Nullable String bemerkung) {
		this.bemerkung = bemerkung;
	}

	public boolean isAbweichungZweitesSemester() {
		return abweichungZweitesSemester;
	}

	public void setAbweichungZweitesSemester(boolean abweichungZweitesSemester) {
		this.abweichungZweitesSemester = abweichungZweitesSemester;
	}

	public boolean addBelegungTagesschuleModul(final BelegungTagesschuleModul modulToAdd) {
		return !belegungTagesschuleModule.contains(modulToAdd) &&
			belegungTagesschuleModule.add(modulToAdd);
	}

	@Nonnull
	public BelegungTagesschule copyBelegungTagesschule(@Nonnull BelegungTagesschule target, @Nonnull AntragCopyType copyType) {
		super.copyAbstractEntity(target, copyType);
		switch (copyType) {
		case MUTATION:
			target.setEintrittsdatum(LocalDate.from(eintrittsdatum));
			target.setPlanKlasse(this.getPlanKlasse());
			target.setAbholungTagesschule(this.abholungTagesschule);
			target.setBemerkung(this.getBemerkung());
			target.setAbweichungZweitesSemester(this.abweichungZweitesSemester);
			copyBelegungTagesschuleModul(target, copyType);
			break;
		case ERNEUERUNG:
		case MUTATION_NEUES_DOSSIER:
		case ERNEUERUNG_NEUES_DOSSIER:
			break;
		}
		return target;
	}

	private void copyBelegungTagesschuleModul(@Nonnull BelegungTagesschule target, @Nonnull AntragCopyType copyType) {
		for (BelegungTagesschuleModul belegungTagesschuleModul : this.getBelegungTagesschuleModule()) {
			BelegungTagesschuleModul belegungTagesschuleModulCopy =
				belegungTagesschuleModul.copyBelegungTagesschuleModul(new BelegungTagesschuleModul(), copyType);
			belegungTagesschuleModulCopy.setBelegungTagesschule(target);
			target.addBelegungTagesschuleModul(belegungTagesschuleModulCopy);
		}
	}
}
