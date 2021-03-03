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

	@Nonnull
	private String name;

	@Nonnull
	private String vorname;

	@Nonnull
	private SozialdienstFallStatus status = SozialdienstFallStatus.INAKTIV;

	@Nonnull
	@XmlJavaTypeAdapter(LocalDateXMLConverter.class)
	private LocalDate geburtsdatum;

	@Nonnull
	private JaxAdresse adresse;

	@Nonnull
	private JaxSozialdienst sozialdienst;

	@Nonnull
	public String getName() {
		return name;
	}

	public void setName(@Nonnull String name) {
		this.name = name;
	}

	@Nonnull
	public SozialdienstFallStatus getStatus() {
		return status;
	}

	public void setStatus(@Nonnull SozialdienstFallStatus status) {
		this.status = status;
	}

	@Nonnull
	public LocalDate getGeburtsdatum() {
		return geburtsdatum;
	}

	public void setGeburtsdatum(@Nonnull LocalDate geburtsdatum) {
		this.geburtsdatum = geburtsdatum;
	}

	@Nonnull
	public JaxAdresse getAdresse() {
		return adresse;
	}

	public void setAdresse(@Nonnull JaxAdresse adresse) {
		this.adresse = adresse;
	}

	@Nonnull
	public JaxSozialdienst getSozialdienst() {
		return sozialdienst;
	}

	public void setSozialdienst(@Nonnull JaxSozialdienst sozialdienst) {
		this.sozialdienst = sozialdienst;
	}

	@Nonnull
	public String getVorname() {
		return vorname;
	}

	public void setVorname(@Nonnull String vorname) {
		this.vorname = vorname;
	}
}
