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

package ch.dvbern.ebegu.api.dtos;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

/**
 * DTO fuer Daten der erweiterten Betreeung
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class JaxErweiterteBetreuung extends JaxAbstractDTO {

	private static final long serialVersionUID = 2763541593212370507L;

	@NotNull
	private Boolean erweiterteBeduerfnisse;

	private JaxFachstelle fachstelle;

	private boolean erweiterteBeduerfnisseBestaetigt;

	@NotNull
	private Boolean keineKesbPlatzierung;

	@Nullable
	private Boolean betreuungInGemeinde;

	@Nonnull
	private Boolean kitaPlusZuschlag;

	@Nullable
	public Boolean getBetreuungInGemeinde() {
		return betreuungInGemeinde;
	}

	public void setBetreuungInGemeinde(@Nullable Boolean betreuungInGemeinde) {
		this.betreuungInGemeinde = betreuungInGemeinde;
	}

	@Nonnull
	public Boolean getErweiterteBeduerfnisse() {
		return erweiterteBeduerfnisse;
	}

	public void setErweiterteBeduerfnisse(@Nonnull Boolean erweiterteBeduerfnisse) {
		this.erweiterteBeduerfnisse = erweiterteBeduerfnisse;
	}

	public JaxFachstelle getFachstelle() {
		return fachstelle;
	}

	public void setFachstelle(JaxFachstelle fachstelle) {
		this.fachstelle = fachstelle;
	}

	public boolean isErweiterteBeduerfnisseBestaetigt() {
		return erweiterteBeduerfnisseBestaetigt;
	}

	public void setErweiterteBeduerfnisseBestaetigt(boolean erweiterteBeduerfnisseBestaetigt) {
		this.erweiterteBeduerfnisseBestaetigt = erweiterteBeduerfnisseBestaetigt;
	}

	@Nonnull
	public Boolean getKeineKesbPlatzierung() {
		return keineKesbPlatzierung;
	}

	public void setKeineKesbPlatzierung(@Nonnull Boolean keineKesbPlatzierung) {
		this.keineKesbPlatzierung = keineKesbPlatzierung;
	}

	@Nonnull
	public Boolean getKitaPlusZuschlag() {
		return kitaPlusZuschlag;
	}

	public void setKitaPlusZuschlag(@Nonnull Boolean kitaPlusZuschlag) {
		this.kitaPlusZuschlag = kitaPlusZuschlag;
	}
}
