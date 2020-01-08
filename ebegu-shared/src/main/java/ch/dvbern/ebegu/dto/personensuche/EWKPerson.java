
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

package ch.dvbern.ebegu.dto.personensuche;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import ch.dvbern.ebegu.entities.Gesuchsperiode;
import ch.dvbern.ebegu.enums.Geschlecht;
import ch.dvbern.lib.date.converters.LocalDateXMLConverter;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.apache.commons.lang3.builder.CompareToBuilder;
import org.jetbrains.annotations.NotNull;

/**
 * DTO für Personen aus dem EWK
 * Note: this class has a natural ordering that is inconsistent with equals.
 */
@XmlRootElement(name = "ewkPerson")
@XmlAccessorType(XmlAccessType.FIELD)
@SuppressFBWarnings("EQ_COMPARETO_USE_OBJECT_EQUALS")
public class EWKPerson implements Serializable, Comparable<EWKPerson> {

	private static final long serialVersionUID = -3920969107353572301L;

	private String personID;

	private String nachname;

	private String vorname;

	@XmlJavaTypeAdapter(LocalDateXMLConverter.class)
	private LocalDate geburtsdatum;

	@XmlJavaTypeAdapter(LocalDateXMLConverter.class)
	private LocalDate zuzugsdatum;

	@XmlJavaTypeAdapter(LocalDateXMLConverter.class)
	private LocalDate wegzugsdatum;

	private String zivilstand;

	@XmlJavaTypeAdapter(LocalDateXMLConverter.class)
	private LocalDate zivilstandsdatum;

	private Geschlecht geschlecht;

	private EWKAdresse adresse;

	private List<EWKBeziehung> beziehungen = new ArrayList<>();

	private boolean kind;

	private boolean gesuchsteller;

	private boolean haushalt;

	private boolean nichtGefunden;

	public EWKPerson() {
	}

	public String getPersonID() {
		return personID;
	}

	public void setPersonID(String personID) {
		this.personID = personID;
	}

	public String getNachname() {
		return nachname;
	}

	public void setNachname(String nachname) {
		this.nachname = nachname;
	}

	public String getVorname() {
		return vorname;
	}

	public void setVorname(String vorname) {
		this.vorname = vorname;
	}

	public LocalDate getGeburtsdatum() {
		return geburtsdatum;
	}

	public void setGeburtsdatum(LocalDate geburtsdatum) {
		this.geburtsdatum = geburtsdatum;
	}

	public LocalDate getZuzugsdatum() {
		return zuzugsdatum;
	}

	public void setZuzugsdatum(LocalDate zuzugsdatum) {
		this.zuzugsdatum = zuzugsdatum;
	}

	public LocalDate getWegzugsdatum() {
		return wegzugsdatum;
	}

	public void setWegzugsdatum(LocalDate wegzugsdatum) {
		this.wegzugsdatum = wegzugsdatum;
	}

	public String getZivilstand() {
		return zivilstand;
	}

	public void setZivilstand(String zivilstand) {
		this.zivilstand = zivilstand;
	}

	public LocalDate getZivilstandsdatum() {
		return zivilstandsdatum;
	}

	public void setZivilstandsdatum(LocalDate zivilstandsdatum) {
		this.zivilstandsdatum = zivilstandsdatum;
	}

	public Geschlecht getGeschlecht() {
		return geschlecht;
	}

	public void setGeschlecht(Geschlecht geschlecht) {
		this.geschlecht = geschlecht;
	}

	public EWKAdresse getAdresse() {
		return adresse;
	}

	public void setAdresse(EWKAdresse adresse) {
		this.adresse = adresse;
	}

	public List<EWKBeziehung> getBeziehungen() {
		return beziehungen;
	}

	public void setBeziehungen(List<EWKBeziehung> beziehungen) {
		this.beziehungen = beziehungen;
	}

	public boolean isKind() {
		return kind;
	}

	public void setKind(boolean kind) {
		this.kind = kind;
	}

	public boolean isGesuchsteller() {
		return gesuchsteller;
	}

	public void setGesuchsteller(boolean gesuchsteller) {
		this.gesuchsteller = gesuchsteller;
	}

	public boolean isHaushalt() {
		return haushalt;
	}

	public void setHaushalt(boolean haushalt) {
		this.haushalt = haushalt;
	}

	public boolean isNichtGefunden() {
		return nichtGefunden;
	}

	public void setNichtGefunden(boolean nichtGefunden) {
		this.nichtGefunden = nichtGefunden;
	}

	public boolean isWohnsitzInPeriode(Gesuchsperiode gesuchsperiode) {
		return (getZuzugsdatum() == null || getZuzugsdatum().isBefore(gesuchsperiode.getGueltigkeit().getGueltigBis()))
			&& (getWegzugsdatum() == null || getWegzugsdatum().isAfter(gesuchsperiode.getGueltigkeit().getGueltigAb()));
	}

	@Override
	public int compareTo(@NotNull EWKPerson other) {
		CompareToBuilder builder = new CompareToBuilder();
		builder.append(other.gesuchsteller, this.gesuchsteller);
		builder.append(other.kind, this.kind);
		builder.append(other.haushalt, this.haushalt);
		builder.append(other.nachname, this.nachname);
		builder.append(other.vorname, this.vorname);
		return builder.toComparison();
	}
}
