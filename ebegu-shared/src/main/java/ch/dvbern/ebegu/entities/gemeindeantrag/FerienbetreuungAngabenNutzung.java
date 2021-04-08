/*
 * Copyright (C) 2021 DV Bern AG, Switzerland
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package ch.dvbern.ebegu.entities.gemeindeantrag;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;

import ch.dvbern.ebegu.entities.AbstractEntity;
import ch.dvbern.ebegu.enums.gemeindeantrag.FerienbetreuungFormularStatus;
import org.hibernate.envers.Audited;

@Entity
@Audited
public class FerienbetreuungAngabenNutzung extends AbstractEntity {

	private static final long serialVersionUID = 3169971542443386003L;

	@Nullable
	@Column()
	private BigDecimal anzahlBetreuungstageKinderBern;

	@Nullable
	@Column()
	private BigDecimal betreuungstageKinderDieserGemeinde;

	@Nullable
	@Column()
	private BigDecimal betreuungstageKinderDieserGemeindeSonderschueler;

	@Nullable
	@Column()
	private BigDecimal davonBetreuungstageKinderAndererGemeinden;

	@Nullable
	@Column()
	private BigDecimal davonBetreuungstageKinderAndererGemeindenSonderschueler;

	@Nullable
	@Column()
	private BigDecimal anzahlBetreuteKinder;

	@Nullable
	@Column()
	private BigDecimal anzahlBetreuteKinderSonderschueler;

	@Nullable
	@Column(name = "anzahl_betreute_kinder_1_zyklus")
	private BigDecimal anzahlBetreuteKinder1Zyklus;

	@Nullable
	@Column(name = "anzahl_betreute_kinder_2_zyklus")
	private BigDecimal anzahlBetreuteKinder2Zyklus;

	@Nullable
	@Column(name = "anzahl_betreute_kinder_3_zyklus")
	private BigDecimal anzahlBetreuteKinder3Zyklus;

	@Nonnull
	@Column()
	@Enumerated(EnumType.STRING)
	private FerienbetreuungFormularStatus status = FerienbetreuungFormularStatus.IN_BEARBEITUNG_GEMEINDE;;

	@Nullable
	public BigDecimal getAnzahlBetreuungstageKinderBern() {
		return anzahlBetreuungstageKinderBern;
	}

	public void setAnzahlBetreuungstageKinderBern(@Nullable BigDecimal anzahlBetreuungstageKinderBern) {
		this.anzahlBetreuungstageKinderBern = anzahlBetreuungstageKinderBern;
	}

	@Nullable
	public BigDecimal getBetreuungstageKinderDieserGemeinde() {
		return betreuungstageKinderDieserGemeinde;
	}

	public void setBetreuungstageKinderDieserGemeinde(@Nullable BigDecimal betreuungstageKinderDieserGemeinde) {
		this.betreuungstageKinderDieserGemeinde = betreuungstageKinderDieserGemeinde;
	}

	@Nullable
	public BigDecimal getBetreuungstageKinderDieserGemeindeSonderschueler() {
		return betreuungstageKinderDieserGemeindeSonderschueler;
	}

	public void setBetreuungstageKinderDieserGemeindeSonderschueler(@Nullable BigDecimal betreuungstageKinderDieserGemeindeSonderschueler) {
		this.betreuungstageKinderDieserGemeindeSonderschueler = betreuungstageKinderDieserGemeindeSonderschueler;
	}

	@Nullable
	public BigDecimal getDavonBetreuungstageKinderAndererGemeinden() {
		return davonBetreuungstageKinderAndererGemeinden;
	}

	public void setDavonBetreuungstageKinderAndererGemeinden(@Nullable BigDecimal davonBetreuungstageKinderAndererGemeinden) {
		this.davonBetreuungstageKinderAndererGemeinden = davonBetreuungstageKinderAndererGemeinden;
	}

	@Nullable
	public BigDecimal getDavonBetreuungstageKinderAndererGemeindenSonderschueler() {
		return davonBetreuungstageKinderAndererGemeindenSonderschueler;
	}

	public void setDavonBetreuungstageKinderAndererGemeindenSonderschueler(@Nullable BigDecimal davonBetreuungstageKinderAndererGemeindenSonderschueler) {
		this.davonBetreuungstageKinderAndererGemeindenSonderschueler = davonBetreuungstageKinderAndererGemeindenSonderschueler;
	}

	@Nullable
	public BigDecimal getAnzahlBetreuteKinder() {
		return anzahlBetreuteKinder;
	}

	public void setAnzahlBetreuteKinder(@Nullable BigDecimal anzahlBetreuteKinder) {
		this.anzahlBetreuteKinder = anzahlBetreuteKinder;
	}

	@Nullable
	public BigDecimal getAnzahlBetreuteKinderSonderschueler() {
		return anzahlBetreuteKinderSonderschueler;
	}

	public void setAnzahlBetreuteKinderSonderschueler(@Nullable BigDecimal anzahlBetreuteKinderSonderschueler) {
		this.anzahlBetreuteKinderSonderschueler = anzahlBetreuteKinderSonderschueler;
	}

	@Nullable
	public BigDecimal getAnzahlBetreuteKinder1Zyklus() {
		return anzahlBetreuteKinder1Zyklus;
	}

	public void setAnzahlBetreuteKinder1Zyklus(@Nullable BigDecimal anzahlBetreuteKinder1Zyklus) {
		this.anzahlBetreuteKinder1Zyklus = anzahlBetreuteKinder1Zyklus;
	}

	@Nullable
	public BigDecimal getAnzahlBetreuteKinder2Zyklus() {
		return anzahlBetreuteKinder2Zyklus;
	}

	public void setAnzahlBetreuteKinder2Zyklus(@Nullable BigDecimal anzahlBetreuteKinder2Zyklus) {
		this.anzahlBetreuteKinder2Zyklus = anzahlBetreuteKinder2Zyklus;
	}

	@Nullable
	public BigDecimal getAnzahlBetreuteKinder3Zyklus() {
		return anzahlBetreuteKinder3Zyklus;
	}

	public void setAnzahlBetreuteKinder3Zyklus(@Nullable BigDecimal anzahlBetreuteKinder3Zyklus) {
		this.anzahlBetreuteKinder3Zyklus = anzahlBetreuteKinder3Zyklus;
	}

	@Override
	public boolean isSame(AbstractEntity other) {
		return getId().equals(other.getId());
	}

	public boolean isReadyForFreigeben() {
		return checkPropertiesNotNull();
	}

	private boolean checkPropertiesNotNull() {
		List<Serializable> nonNullObj = Arrays.asList(
			this.anzahlBetreuungstageKinderBern,
			this.betreuungstageKinderDieserGemeinde,
			this.davonBetreuungstageKinderAndererGemeinden
		);
		return nonNullObj.stream()
			.noneMatch(Objects::isNull);
	}

	@Nonnull
	public FerienbetreuungFormularStatus getStatus() {
		return status;
	}

	public void setStatus(@Nonnull FerienbetreuungFormularStatus status) {
		this.status = status;
	}

	public boolean isAbgeschlossen() {
		return status == FerienbetreuungFormularStatus.ABGESCHLOSSEN;
	}

	public boolean isGeprueft() {
		return status == FerienbetreuungFormularStatus.GEPRUEFT;
	}
}
