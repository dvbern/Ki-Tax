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
import javax.persistence.AssociationOverride;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ForeignKey;
import javax.persistence.JoinColumn;
import javax.validation.constraints.NotNull;

import ch.dvbern.ebegu.enums.AntragCopyType;
import ch.dvbern.ebegu.types.DateRange;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.apache.commons.lang3.builder.CompareToBuilder;
import org.hibernate.envers.Audited;

/**
 * Entity fuer Betreuungspensen.
 * Note: this class has a natural ordering that is inconsistent with equals.
 */
@SuppressWarnings("ComparableImplementedButEqualsNotOverridden")
@Audited
@Entity
@AssociationOverride(name = "eingewoehnung",
	joinColumns = @JoinColumn(name = "eingewoehnung_id"), foreignKey = @ForeignKey(name = "FK_betreuungspensum_eingewoehnung_id"))
public class Betreuungspensum extends AbstractBetreuungsPensum implements Comparable<Betreuungspensum> {

	private static final long serialVersionUID = -9032857320571372370L;

	@NotNull
	@Column(nullable = false)
	private Boolean nichtEingetreten = false;

	public Betreuungspensum() {
	}

	@SuppressWarnings("PMD.ConstructorCallsOverridableMethod")
	public Betreuungspensum(BetreuungsmitteilungPensum betPensumMitteilung) {
		this.setGueltigkeit(new DateRange(betPensumMitteilung.getGueltigkeit()));
		this.setPensum(betPensumMitteilung.getPensum());
		this.setUnitForDisplay(betPensumMitteilung.getUnitForDisplay());
		this.setMonatlicheBetreuungskosten(betPensumMitteilung.getMonatlicheBetreuungskosten());
		this.setNichtEingetreten(false); //can not be set through BetreuungsmitteilungPensum
		this.setMonatlicheHauptmahlzeiten(betPensumMitteilung.getMonatlicheHauptmahlzeiten());
		this.setMonatlicheNebenmahlzeiten(betPensumMitteilung.getMonatlicheNebenmahlzeiten());
		this.setTarifProHauptmahlzeit(betPensumMitteilung.getTarifProHauptmahlzeit());
		this.setTarifProNebenmahlzeit(betPensumMitteilung.getTarifProNebenmahlzeit());
		this.setStuendlicheVollkosten(betPensumMitteilung.getStuendlicheVollkosten());
		this.setBetreuungInFerienzeit(betPensumMitteilung.getBetreuungInFerienzeit());
		this.setBetreuteTage(betPensumMitteilung.getBetreuteTage());

		if (betPensumMitteilung.getEingewoehnung() != null) {
			Eingewoehnung eingewoehnung = new Eingewoehnung();
			eingewoehnung.setKosten(betPensumMitteilung.getEingewoehnung().getKosten());
			eingewoehnung.setGueltigkeit(betPensumMitteilung.getEingewoehnung().getGueltigkeit());
			this.setEingewoehnung(eingewoehnung);
		}
	}

	public Betreuungspensum(DateRange gueltigkeit) {
		this.setGueltigkeit(gueltigkeit);
	}

	@Nonnull
	public Boolean getNichtEingetreten() {
		return nichtEingetreten;
	}

	public void setNichtEingetreten(@Nonnull Boolean nichtEingetreten) {
		this.nichtEingetreten = nichtEingetreten;
	}

	@Override
	public int compareTo(Betreuungspensum o) {
		CompareToBuilder builder = new CompareToBuilder();
		builder.append(this.getGueltigkeit(), o.getGueltigkeit());
		builder.append(this.getId(), o.getId());
		return builder.toComparison();
	}

	@Nonnull
	public Betreuungspensum copyBetreuungspensum(@Nonnull Betreuungspensum target, @Nonnull AntragCopyType copyType) {
		super.copyAbstractBetreuungspensumMahlzeitenEntity(target, copyType);
		switch (copyType) {
		case MUTATION:
			target.setNichtEingetreten(this.getNichtEingetreten());
			target.setBetreuungInFerienzeit(this.getBetreuungInFerienzeit());
			break;
		case ERNEUERUNG:
		case ERNEUERUNG_AR_2023:
		case MUTATION_NEUES_DOSSIER:
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
		if (!super.isSame(other)) {
			return false;
		}
		final Betreuungspensum otherBetreuungspensum = (Betreuungspensum) other;
		return Objects.equals(getNichtEingetreten(), otherBetreuungspensum.getNichtEingetreten())
			&& Objects.equals(getBetreuungInFerienzeit(), otherBetreuungspensum.getBetreuungInFerienzeit())
			&& this.getUnitForDisplay() == otherBetreuungspensum.getUnitForDisplay();
	}
}
