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
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.ForeignKey;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.UniqueConstraint;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import ch.dvbern.ebegu.enums.Ferienname;
import ch.dvbern.ebegu.validators.CheckFerieninselStammdatenDatesOverlapping;
import org.hibernate.annotations.SortNatural;
import org.hibernate.envers.Audited;

/**
 * Entity for the Basedata of a Ferieninsel
 */
@Audited
@Entity
@CheckFerieninselStammdatenDatesOverlapping
public class GemeindeStammdatenGesuchsperiodeFerieninsel extends AbstractMutableEntity {

	private static final long serialVersionUID = 6703477164293147908L;

	@NotNull
	@Column(nullable = false)
	@Enumerated(EnumType.STRING)
	private Ferienname ferienname;


	@NotNull
	@Valid
	@SortNatural
	@OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
	@JoinTable(
		name = "gemeinde_stammdaten_gp_fi_gemeinde_stammdaten_gp_fi_zeitraum",
		joinColumns = @JoinColumn(name = "ferieninsel_stammdaten_id", nullable = false),
		inverseJoinColumns = @JoinColumn(name = "zeitraum_id", nullable = false),
		foreignKey = @ForeignKey(name = "FK_ferieninsel_stammdaten_ferieninsel_stammdaten_id"),
		inverseForeignKey = @ForeignKey(name = "FK_ferieninsel_stammdaten_ferieninsel_zeitraum_id"),
		uniqueConstraints = @UniqueConstraint(columnNames = "zeitraum_id", name = "UK_ferieninsel_stammdaten_zeitraum_id"),
		indexes = {
			@Index(name = "IX_ferieninsel_stammdaten_ferieninsel_stammdaten_id", columnList = "ferieninsel_stammdaten_id"),
			@Index(name = "IX_ferieninsel_stammdaten_zeitraum_id", columnList = "zeitraum_id"),
		})
	private List<GemeindeStammdatenGesuchsperiodeFerieninselZeitraum> zeitraumList = new ArrayList<>();

	@NotNull
	@Column(nullable = false)
	private LocalDate anmeldeschluss;

	//TODO FERIENINSEL: Gesuchsperiode entfernen, ist schon auf dem GemeindeStammdatenGesuchsperiode
	@NotNull
	@ManyToOne(optional = false)
	@JoinColumn(foreignKey = @ForeignKey(name = "FK_ferieninsel_stammdaten_gesuchsperiodeId"))
	private Gesuchsperiode gesuchsperiode;

	public Ferienname getFerienname() {
		return ferienname;
	}

	public void setFerienname(Ferienname ferienname) {
		this.ferienname = ferienname;
	}

	public List<GemeindeStammdatenGesuchsperiodeFerieninselZeitraum> getZeitraumList() {
		return zeitraumList;
	}

	public void setZeitraumList(List<GemeindeStammdatenGesuchsperiodeFerieninselZeitraum> zeitraumList) {
		this.zeitraumList = zeitraumList;
	}

	public LocalDate getAnmeldeschluss() {
		return anmeldeschluss;
	}

	public void setAnmeldeschluss(LocalDate anmeldeschluss) {
		this.anmeldeschluss = anmeldeschluss;
	}

	public Gesuchsperiode getGesuchsperiode() {
		return gesuchsperiode;
	}

	public void setGesuchsperiode(Gesuchsperiode gesuchsperiode) {
		this.gesuchsperiode = gesuchsperiode;
	}

	@Override
	public boolean isSame(AbstractEntity other) {
		if (this == other) {
			return true;
		}
		if (other == null || !getClass().equals(other.getClass())) {
			return false;
		}
		if (!(other instanceof GemeindeStammdatenGesuchsperiodeFerieninsel)) {
			return false;
		}
		final GemeindeStammdatenGesuchsperiodeFerieninsel otherFerieninselStammdaten = (GemeindeStammdatenGesuchsperiodeFerieninsel) other;
		return Objects.equals(getFerienname(), otherFerieninselStammdaten.getFerienname()) &&
			Objects.equals(getGesuchsperiode(), otherFerieninselStammdaten.getGesuchsperiode());
	}
}
