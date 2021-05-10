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

import java.math.BigDecimal;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ForeignKey;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;

import ch.dvbern.ebegu.entities.AbstractEntity;
import org.hibernate.envers.Audited;

@Entity
@Audited
public class FerienbetreuungAngaben extends AbstractEntity {

	private static final long serialVersionUID = -4376690435594903597L;

	@Nonnull
	@OneToOne(cascade = CascadeType.MERGE, orphanRemoval = true,fetch = FetchType.EAGER)
	@JoinColumn(foreignKey = @ForeignKey(name = "FK_ferienbetreuung_stammdaten_ferienbetreuung"), nullable = false)
	private FerienbetreuungAngabenStammdaten ferienbetreuungAngabenStammdaten = new FerienbetreuungAngabenStammdaten();

	@Nonnull
	@OneToOne(cascade = CascadeType.MERGE, orphanRemoval = true, fetch = FetchType.EAGER)
	@JoinColumn(foreignKey = @ForeignKey(name = "FK_ferienbetreuung_angebot_ferienbetreuung"), nullable = false)
	private FerienbetreuungAngabenAngebot ferienbetreuungAngabenAngebot = new FerienbetreuungAngabenAngebot();

	@Nonnull
	@OneToOne(cascade = CascadeType.MERGE, orphanRemoval = true, fetch = FetchType.EAGER)
	@JoinColumn(foreignKey = @ForeignKey(name = "FK_ferienbetreuung_nutzung_ferienbetreuung"), nullable = false)
	private FerienbetreuungAngabenNutzung ferienbetreuungAngabenNutzung = new FerienbetreuungAngabenNutzung();

	@Nonnull
	@OneToOne(cascade = CascadeType.MERGE, orphanRemoval = true, fetch = FetchType.EAGER)
	@JoinColumn(foreignKey = @ForeignKey(name = "FK_ferienbetreuung_kosten_einnahmen_ferienbetreuung"), nullable = false)
	private FerienbetreuungAngabenKostenEinnahmen ferienbetreuungAngabenKostenEinnahmen = new FerienbetreuungAngabenKostenEinnahmen();

	@Nullable
	@Column()
	private BigDecimal kantonsbeitrag;

	@Nullable
	@Column()
	private BigDecimal gemeindebeitrag;

	public FerienbetreuungAngaben() {}

	public FerienbetreuungAngaben(FerienbetreuungAngaben angabenToCopy) {
		this.ferienbetreuungAngabenStammdaten = new FerienbetreuungAngabenStammdaten(angabenToCopy.ferienbetreuungAngabenStammdaten);
		this.ferienbetreuungAngabenAngebot = new FerienbetreuungAngabenAngebot(angabenToCopy.ferienbetreuungAngabenAngebot);
		this.ferienbetreuungAngabenNutzung = new FerienbetreuungAngabenNutzung(angabenToCopy.ferienbetreuungAngabenNutzung);
		this.ferienbetreuungAngabenKostenEinnahmen = new FerienbetreuungAngabenKostenEinnahmen(angabenToCopy.ferienbetreuungAngabenKostenEinnahmen);
	}

	@Nonnull
	public FerienbetreuungAngabenStammdaten getFerienbetreuungAngabenStammdaten() {
		return ferienbetreuungAngabenStammdaten;
	}

	public void setFerienbetreuungAngabenStammdaten(@Nonnull FerienbetreuungAngabenStammdaten ferienbetreuungAngabenStammdaten) {
		this.ferienbetreuungAngabenStammdaten = ferienbetreuungAngabenStammdaten;
	}

	@Nonnull
	public FerienbetreuungAngabenAngebot getFerienbetreuungAngabenAngebot() {
		return ferienbetreuungAngabenAngebot;
	}

	public void setFerienbetreuungAngabenAngebot(@Nonnull FerienbetreuungAngabenAngebot ferienbetreuungAngabenAngebot) {
		this.ferienbetreuungAngabenAngebot = ferienbetreuungAngabenAngebot;
	}

	@Nonnull
	public FerienbetreuungAngabenNutzung getFerienbetreuungAngabenNutzung() {
		return ferienbetreuungAngabenNutzung;
	}

	public void setFerienbetreuungAngabenNutzung(@Nonnull FerienbetreuungAngabenNutzung ferienbetreuungAngabenNutzung) {
		this.ferienbetreuungAngabenNutzung = ferienbetreuungAngabenNutzung;
	}

	@Nonnull
	public FerienbetreuungAngabenKostenEinnahmen getFerienbetreuungAngabenKostenEinnahmen() {
		return ferienbetreuungAngabenKostenEinnahmen;
	}

	public void setFerienbetreuungAngabenKostenEinnahmen(@Nonnull FerienbetreuungAngabenKostenEinnahmen ferienbetreuungAngabenKostenEinnahmen) {
		this.ferienbetreuungAngabenKostenEinnahmen = ferienbetreuungAngabenKostenEinnahmen;
	}

	@Nullable
	public BigDecimal getKantonsbeitrag() {
		return kantonsbeitrag;
	}

	public void setKantonsbeitrag(@Nullable BigDecimal kantonsbeitrag) {
		this.kantonsbeitrag = kantonsbeitrag;
	}

	@Nullable
	public BigDecimal getGemeindebeitrag() {
		return gemeindebeitrag;
	}

	public void setGemeindebeitrag(@Nullable BigDecimal gemeindebeitrag) {
		this.gemeindebeitrag = gemeindebeitrag;
	}

	@Override
	public boolean isSame(AbstractEntity other) {
		return getId().equals(other.getId());
	}

	public boolean isReadyForFreigeben() {
		return ferienbetreuungAngabenStammdaten.isReadyForFreigeben()
			&& ferienbetreuungAngabenAngebot.isReadyForFreigeben()
			&& ferienbetreuungAngabenNutzung.isReadyForFreigeben()
			&& ferienbetreuungAngabenKostenEinnahmen.isReadyForFreigeben();
	}

}
