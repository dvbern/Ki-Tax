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

package ch.dvbern.ebegu.test.data;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import javax.xml.bind.annotation.XmlElement;

import ch.dvbern.ebegu.entities.VerfuegungZeitabschnitt;
import ch.dvbern.ebegu.entities.VerfuegungZeitabschnittBemerkung;
import org.apache.commons.lang.StringUtils;

import static java.math.BigDecimal.ZERO;

/**
 * XML Klasse zum speichern der Verfügungszeitabschnitt daten in XML file
 */
public class VerfuegungZeitabschnittData {

	private String gueltigAb;

	private String gueltigBis;

	private BigDecimal betreuungspensumProzent;

	private int anspruchberechtigtesPensum;

	private BigDecimal vollkosten = ZERO;

	private BigDecimal elternbeitrag = ZERO;

	private BigDecimal abzugFamGroesse = null;

	private BigDecimal famGroesse = null;

	private List<String> bemerkungenList = Collections.emptyList();

	public VerfuegungZeitabschnittData() {

	}

	public VerfuegungZeitabschnittData(VerfuegungZeitabschnitt verfuegungZeitabschnitt) {
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");

		this.gueltigAb = verfuegungZeitabschnitt.getGueltigkeit().getGueltigAb().format(formatter);
		this.gueltigBis = verfuegungZeitabschnitt.getGueltigkeit().getGueltigBis().format(formatter);
		this.abzugFamGroesse = verfuegungZeitabschnitt.getAbzugFamGroesse();
		this.elternbeitrag = verfuegungZeitabschnitt.getElternbeitrag();
		this.anspruchberechtigtesPensum = verfuegungZeitabschnitt.getAnspruchberechtigtesPensum();
		this.bemerkungenList = verfuegungZeitabschnitt.getVerfuegungZeitabschnittBemerkungList().stream()
			.map(VerfuegungZeitabschnittBemerkung::getBemerkung)
			.collect(Collectors.toList());
		this.betreuungspensumProzent = verfuegungZeitabschnitt.getBetreuungspensumProzent();
		this.famGroesse = verfuegungZeitabschnitt.getFamGroesse();
		this.vollkosten = verfuegungZeitabschnitt.getVollkosten();
	}

	public BigDecimal getBetreuungspensumProzent() {
		return betreuungspensumProzent;
	}

	public void setBetreuungspensumProzent(BigDecimal betreuungspensumProzent) {
		this.betreuungspensumProzent = betreuungspensumProzent;
	}

	public int getAnspruchberechtigtesPensum() {
		return anspruchberechtigtesPensum;
	}

	public void setAnspruchberechtigtesPensum(int anspruchberechtigtesPensum) {
		this.anspruchberechtigtesPensum = anspruchberechtigtesPensum;
	}

	public BigDecimal getVollkosten() {
		return vollkosten;
	}

	public void setVollkosten(BigDecimal vollkosten) {
		this.vollkosten = vollkosten;
	}

	public BigDecimal getElternbeitrag() {
		return elternbeitrag;
	}

	public void setElternbeitrag(BigDecimal elternbeitrag) {
		this.elternbeitrag = elternbeitrag;
	}

	public BigDecimal getAbzugFamGroesse() {
		return abzugFamGroesse;
	}

	public void setAbzugFamGroesse(BigDecimal abzugFamGroesse) {
		this.abzugFamGroesse = abzugFamGroesse;
	}

	public BigDecimal getFamGroesse() {
		return famGroesse;
	}

	public void setFamGroesse(BigDecimal famGroesse) {
		this.famGroesse = famGroesse;
	}

	public List<String> getBemerkungenList() {
		return bemerkungenList;
	}

	public void setBemerkungenList(List<String> bemerkungenList) {
		this.bemerkungenList = bemerkungenList;
	}

	@XmlElement(name="bemerkungen")
	public void setBemerkungen(String bemerkungen) {
		if(StringUtils.isNotEmpty(bemerkungen)) {
			this.bemerkungenList = Arrays.asList(bemerkungen.split("\n"));
		}
	}

	public String getGueltigAb() {
		return gueltigAb;
	}

	public void setGueltigAb(String gueltigAb) {
		this.gueltigAb = gueltigAb;
	}

	public String getGueltigBis() {
		return gueltigBis;
	}

	public void setGueltigBis(String gueltigBis) {
		this.gueltigBis = gueltigBis;
	}
}
