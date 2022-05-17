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

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.ForeignKey;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Transient;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import ch.dvbern.ebegu.enums.AntragCopyType;
import ch.dvbern.ebegu.types.DateRange;
import ch.dvbern.ebegu.util.EbeguUtil;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.hibernate.envers.Audited;

/**
 * Container-Entity für die GesuchstellerAdressen
 */
@Audited
@Entity
public class GesuchstellerAdresseContainer extends AbstractMutableEntity {

	private static final long serialVersionUID = -3084333639027795652L;

	@NotNull
	@ManyToOne(optional = false)
	@JoinColumn(foreignKey = @ForeignKey(name = "FK_gesuchstelleradresse_container_gesuchstellerContainer_id"))
	private GesuchstellerContainer gesuchstellerContainer;

	@Nullable
	@Valid
	@OneToOne(optional = true, cascade = CascadeType.ALL, orphanRemoval = true)
	@JoinColumn(foreignKey = @ForeignKey(name = "FK_gesuchstelleradresse_container_gesuchstellergs_id"))
	private GesuchstellerAdresse gesuchstellerAdresseGS;

	@Valid
	@OneToOne(optional = true, cascade = CascadeType.ALL, orphanRemoval = true)
	@JoinColumn(foreignKey = @ForeignKey(name = "FK_gesuchstelleradresse_container_gesuchstellerja_id"))
	private GesuchstellerAdresse gesuchstellerAdresseJA;

	public GesuchstellerAdresseContainer() {
	}

	public GesuchstellerContainer getGesuchstellerContainer() {
		return gesuchstellerContainer;
	}

	public void setGesuchstellerContainer(GesuchstellerContainer gesuchstellerContainer) {
		this.gesuchstellerContainer = gesuchstellerContainer;
	}

	@Nullable
	public GesuchstellerAdresse getGesuchstellerAdresseGS() {
		return gesuchstellerAdresseGS;
	}

	public void setGesuchstellerAdresseGS(@Nullable GesuchstellerAdresse gesuchstellerAdresseGS) {
		this.gesuchstellerAdresseGS = gesuchstellerAdresseGS;
	}

	@Nullable
	public GesuchstellerAdresse getGesuchstellerAdresseJA() {
		return gesuchstellerAdresseJA;
	}

	public void setGesuchstellerAdresseJA(@Nullable GesuchstellerAdresse gesuchstellerAdresseJA) {
		this.gesuchstellerAdresseJA = gesuchstellerAdresseJA;
	}

	/**
	 * Fragt nach dem Wert der AdresseJA, welcher eigentlich der geltende Wert ist
	 */
	@Transient
	public boolean extractIsKorrespondenzAdresse() {
		return this.gesuchstellerAdresseJA != null && this.gesuchstellerAdresseJA.isKorrespondenzAdresse()
			|| this.gesuchstellerAdresseJA == null && this.gesuchstellerAdresseGS != null && this.gesuchstellerAdresseGS.isKorrespondenzAdresse();
	}

	/**
	 * Fragt nach dem Wert der AdresseJA, welcher eigentlich der geltende Wert ist
	 */
	@Transient
	public boolean extractIsRechnungsAdresse() {
		return this.gesuchstellerAdresseJA != null && this.gesuchstellerAdresseJA.isRechnungsAdresse()
			|| this.gesuchstellerAdresseJA == null && this.gesuchstellerAdresseGS != null && this.gesuchstellerAdresseGS.isRechnungsAdresse();
	}

	/**
	 * Extracts the value of nichtInGemeinde von gesuchstellerAdresseJA
	 */
	@Transient
	public boolean extractIsNichtInGemeinde() {
		return this.gesuchstellerAdresseJA != null && this.gesuchstellerAdresseJA.isNichtInGemeinde();
	}

	/**
	 * Extracts the Gueltigkeit von gesuchstellerAdresseJA
	 */
	@Nullable
	public DateRange extractGueltigkeit() {
		return this.gesuchstellerAdresseJA != null ? this.gesuchstellerAdresseJA.getGueltigkeit() : null;
	}

	/**
	 * Extracts the AdresseTyp von gesuchstellerAdresseJA
	 */
	@Nullable
	public AdresseTyp extractAdresseTyp() {
		return this.gesuchstellerAdresseJA != null ? this.gesuchstellerAdresseJA.getAdresseTyp() : null;
	}

	/**
	 * Extracts the Hausnummer von gesuchstellerAdresseJA
	 */
	@Nullable
	public String extractHausnummer() {
		return this.gesuchstellerAdresseJA != null ? this.gesuchstellerAdresseJA.getHausnummer() : null;
	}

	/**
	 * Extracts the Strasse von gesuchstellerAdresseJA
	 */
	@Nullable
	public String extractStrasse() {
		return this.gesuchstellerAdresseJA != null ? this.gesuchstellerAdresseJA.getStrasse() : null;
	}

	/**
	 * Extracts the Zusatzzeile von gesuchstellerAdresseJA
	 */
	@Nullable
	public String extractZusatzzeile() {
		return this.gesuchstellerAdresseJA != null ? this.gesuchstellerAdresseJA.getZusatzzeile() : null;
	}

	/**
	 * Extracts the PLZ von gesuchstellerAdresseJA
	 */
	@Nullable
	public String extractPlz() {
		return this.gesuchstellerAdresseJA != null ? this.gesuchstellerAdresseJA.getPlz() : null;
	}

	/**
	 * Extracts the Ort von gesuchstellerAdresseJA
	 */
	@Nullable
	public String extractOrt() {
		return this.gesuchstellerAdresseJA != null ? this.gesuchstellerAdresseJA.getOrt() : null;
	}

	/**
	 * Extracts the Land von gesuchstellerAdresseJA
	 */
	@Nullable
	public String extractLand() {
		return this.gesuchstellerAdresseJA != null ? this.gesuchstellerAdresseJA.getLand().name(): null;
	}

	/**
	 * Extracts the Gemeinde von gesuchstellerAdresseJA
	 */
	@Nullable
	public String extractGemeinde() {
		return this.gesuchstellerAdresseJA != null ? this.gesuchstellerAdresseJA.getGemeinde(): null;
	}


	@Nullable
	public String extractOrganisation() {
		return this.gesuchstellerAdresseJA != null ? this.gesuchstellerAdresseJA.getOrganisation() : null;
	}

	@Nonnull
	public GesuchstellerAdresseContainer copyGesuchstellerAdresseContainer(@Nonnull GesuchstellerAdresseContainer target, @Nonnull AntragCopyType copyType, @Nonnull GesuchstellerContainer gsContainer) {
		super.copyAbstractEntity(target, copyType);
		target.setGesuchstellerContainer(gsContainer);
		target.setGesuchstellerAdresseGS(null);
		if (this.getGesuchstellerAdresseJA() != null) {
			target.setGesuchstellerAdresseJA(this.getGesuchstellerAdresseJA().copyGesuchstellerAdresse(new GesuchstellerAdresse(), copyType));
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
		final GesuchstellerAdresseContainer otherAdresseContainer = (GesuchstellerAdresseContainer) other;
		return EbeguUtil.isSame(getGesuchstellerAdresseJA(), otherAdresseContainer.getGesuchstellerAdresseJA());
	}
}
