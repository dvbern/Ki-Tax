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
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.ForeignKey;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import ch.dvbern.ebegu.enums.AntragCopyType;
import ch.dvbern.ebegu.enums.EnumAbholungTagesschule;
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

	@NotNull
	@Valid
	@SortNatural
	@ManyToMany
	// es darf nicht cascadeAll sein, da sonst die Module geloescht werden, wenn die Belegung geloescht wird, obwohl das Modul eigentlich zur Institutione gehoert
	@JoinTable(
		joinColumns = @JoinColumn(name = "belegung_tagesschule_id", nullable = false),
		inverseJoinColumns = @JoinColumn(name = "module_tagesschule_id", nullable = false),
		foreignKey = @ForeignKey(name = "FK_belegung_tagesschule_belegung_tagesschule_id"),
		inverseForeignKey = @ForeignKey(name = "FK_belegung_tagesschule_module_tagesschule_id"),
		indexes = {
			@Index(name = "IX_belegung_tagesschule_belegung_tagesschule_id", columnList = "belegung_tagesschule_id"),
			@Index(name = "IX_belegung_tagesschule_module_tagesschule_id", columnList = "module_tagesschule_id"),
		}
	)
	private Set<ModulTagesschule> moduleTagesschule = new TreeSet<>();

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
	private EnumAbholungTagesschule abholungTagesschule;

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

	@NotNull
	public Set<ModulTagesschule> getModuleTagesschule() {
		return moduleTagesschule;
	}

	public void setModuleTagesschule(@NotNull Set<ModulTagesschule> module) {
		this.moduleTagesschule = module;
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
	public EnumAbholungTagesschule getAbholungTagesschule() {
		return abholungTagesschule;
	}

	public void setAbholungTagesschule(@Nullable EnumAbholungTagesschule abholungTagesschule) {
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
			// Don't copy them, because it's a ManyToMany relation
			target.getModuleTagesschule().clear();
			target.getModuleTagesschule().addAll(moduleTagesschule);
			break;
		case ERNEUERUNG:
		case MUTATION_NEUES_DOSSIER:
		case ERNEUERUNG_NEUES_DOSSIER:
			break;
		}
		return target;
	}
}
