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

package ch.dvbern.ebegu.api.dtos.gemeindeantrag;

import java.math.BigDecimal;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import ch.dvbern.ebegu.api.dtos.JaxAbstractDTO;

public class JaxFerienbetreuungAngaben extends JaxAbstractDTO {

	private static final long serialVersionUID = 7756772754919885791L;

	@Nonnull
	JaxFerienbetreuungAngabenStammdaten stammdaten;

	@Nonnull
	JaxFerienbetreuungAngabenAngebot angebot;

	@Nonnull
	JaxFerienbetreuungAngabenNutzung nutzung;

	@Nonnull
	JaxFerienbetreuungAngabenKostenEinnahmen kostenEinnahmen;

	@Nullable
	JaxFerienbetreuungBerechnungen berechnungen;

	@Nullable
	private BigDecimal kantonsbeitrag;

	@Nullable
	private BigDecimal gemeindebeitrag;

	@Nonnull
	public JaxFerienbetreuungAngabenStammdaten getStammdaten() {
		return stammdaten;
	}

	public void setStammdaten(@Nonnull JaxFerienbetreuungAngabenStammdaten stammdaten) {
		this.stammdaten = stammdaten;
	}

	@Nonnull
	public JaxFerienbetreuungAngabenAngebot getAngebot() {
		return angebot;
	}

	public void setAngebot(@Nonnull JaxFerienbetreuungAngabenAngebot angebot) {
		this.angebot = angebot;
	}

	@Nonnull
	public JaxFerienbetreuungAngabenNutzung getNutzung() {
		return nutzung;
	}

	public void setNutzung(@Nonnull JaxFerienbetreuungAngabenNutzung nutzung) {
		this.nutzung = nutzung;
	}

	@Nonnull
	public JaxFerienbetreuungAngabenKostenEinnahmen getKostenEinnahmen() {
		return kostenEinnahmen;
	}

	public void setKostenEinnahmen(@Nonnull JaxFerienbetreuungAngabenKostenEinnahmen kostenEinnahmen) {
		this.kostenEinnahmen = kostenEinnahmen;
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

	@Nullable
	public JaxFerienbetreuungBerechnungen getBerechnungen() {
		return berechnungen;
	}

	public void setBerechnungen(@Nullable JaxFerienbetreuungBerechnungen berechnungen) {
		this.berechnungen = berechnungen;
	}
}
