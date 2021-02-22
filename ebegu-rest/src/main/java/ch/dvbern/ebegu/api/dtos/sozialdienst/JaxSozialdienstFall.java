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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package ch.dvbern.ebegu.api.dtos.sozialdienst;

import java.time.LocalDate;

import javax.annotation.Nonnull;
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import ch.dvbern.ebegu.api.dtos.JaxAbstractDTO;
import ch.dvbern.ebegu.api.dtos.JaxAdresse;
import ch.dvbern.ebegu.enums.SozialdienstFallStatus;
import ch.dvbern.lib.date.converters.LocalDateXMLConverter;

@XmlAccessorType(XmlAccessType.FIELD)
public class JaxSozialdienstFall extends JaxAbstractDTO {

	private static final long serialVersionUID = 2183212900252723745L;

	@NotNull
	private String name;

	@Nonnull
	private SozialdienstFallStatus status;

	@NotNull
	@XmlJavaTypeAdapter(LocalDateXMLConverter.class)
	private LocalDate geburtsdatum;

	@NotNull
	private JaxAdresse adresse;

	@NotNull
	private JaxSozialdienst sozialdienst;

	public JaxAdresse getAdresse() {
		return adresse;
	}

	public void setAdresse(JaxAdresse adresse) {
		this.adresse = adresse;
	}

	@Nonnull
	public SozialdienstFallStatus getStatus() {
		return status;
	}

	public void setStatus(@Nonnull SozialdienstFallStatus status) {
		this.status = status;
	}

	public LocalDate getGeburtsdatum() {
		return geburtsdatum;
	}

	public void setGeburtsdatum(LocalDate geburtsdatum) {
		this.geburtsdatum = geburtsdatum;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public JaxSozialdienst getSozialdienst() {
		return sozialdienst;
	}

	public void setSozialdienst(JaxSozialdienst jaxSozialdienst) {
		this.sozialdienst = jaxSozialdienst;
	}
}
