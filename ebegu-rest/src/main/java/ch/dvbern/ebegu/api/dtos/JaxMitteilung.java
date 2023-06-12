/*
 * Copyright (C) 2023 DV Bern AG, Switzerland
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

package ch.dvbern.ebegu.api.dtos;

import ch.dvbern.ebegu.api.dtos.finanziellesituation.JaxFinanzielleSituation;
import ch.dvbern.ebegu.enums.MitteilungStatus;
import ch.dvbern.ebegu.enums.MitteilungTeilnehmerTyp;
import ch.dvbern.ebegu.enums.MitteilungTyp;
import ch.dvbern.ebegu.util.Constants;
import ch.dvbern.lib.date.converters.LocalDateTimeXMLConverter;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.time.LocalDateTime;

/**
 * DTO fuer Stammdaten der Mitteilungen
 */
@XmlRootElement(name = "mitteilung")
@XmlAccessorType(XmlAccessType.FIELD)
public class JaxMitteilung extends JaxAbstractDTO {

	private static final long serialVersionUID = -1297771341674137397L;

	@Nullable
	private JaxDossier dossier;

	@Nullable
	private JaxBetreuung betreuung;

	@Nullable
	private JaxFinanzielleSituation finanzielleSituation;

	@Nullable
	private MitteilungTeilnehmerTyp senderTyp;

	@Nullable
	private MitteilungTeilnehmerTyp empfaengerTyp;

	@Nullable
	private JaxBenutzer sender;

	@Nullable
	private JaxBenutzer empfaenger;

	@Size(min = 0, max = Constants.DB_DEFAULT_MAX_LENGTH)
	@Nullable
	private String subject;

	@Size(min = 0, max = Constants.DB_TEXTAREA_LENGTH)
	@Nullable
	private String message;

	@NotNull
	private MitteilungStatus mitteilungStatus;

	@Nullable
	@XmlJavaTypeAdapter(LocalDateTimeXMLConverter.class)
	private LocalDateTime sentDatum;

	@Nullable
	private JaxInstitution institution;

	@Nullable
	private MitteilungTyp mitteilungTyp;

	@Nullable
	public JaxDossier getDossier() {
		return dossier;
	}

	public void setDossier(@Nullable JaxDossier dossier) {
		this.dossier = dossier;
	}

	@Nullable
	public JaxBetreuung getBetreuung() {
		return betreuung;
	}

	public void setBetreuung(@Nullable JaxBetreuung betreuung) {
		this.betreuung = betreuung;
	}

	@Nullable
	public JaxFinanzielleSituation getFinanzielleSituation() {
		return finanzielleSituation;
	}

	public void setFinanzielleSituation(@Nullable JaxFinanzielleSituation finanzielleSituation) {
		this.finanzielleSituation = finanzielleSituation;
	}

	@Nullable
	public MitteilungTeilnehmerTyp getSenderTyp() {
		return senderTyp;
	}

	public void setSenderTyp(@Nullable MitteilungTeilnehmerTyp senderTyp) {
		this.senderTyp = senderTyp;
	}

	@Nullable
	public MitteilungTeilnehmerTyp getEmpfaengerTyp() {
		return empfaengerTyp;
	}

	public void setEmpfaengerTyp(@Nullable MitteilungTeilnehmerTyp empfaengerTyp) {
		this.empfaengerTyp = empfaengerTyp;
	}

	@Nullable
	public JaxBenutzer getSender() {
		return sender;
	}

	public void setSender(@Nullable JaxBenutzer sender) {
		this.sender = sender;
	}

	@Nullable
	public JaxBenutzer getEmpfaenger() {
		return empfaenger;
	}

	public void setEmpfaenger(@Nullable JaxBenutzer empfaenger) {
		this.empfaenger = empfaenger;
	}

	@Nullable
	public String getSubject() {
		return subject;
	}

	public void setSubject(@Nullable String subject) {
		this.subject = subject;
	}

	@Nullable
	public String getMessage() {
		return message;
	}

	public void setMessage(@Nullable String message) {
		this.message = message;
	}

	public MitteilungStatus getMitteilungStatus() {
		return mitteilungStatus;
	}

	public void setMitteilungStatus(MitteilungStatus mitteilungStatus) {
		this.mitteilungStatus = mitteilungStatus;
	}

	@Nullable
	public LocalDateTime getSentDatum() {
		return sentDatum;
	}

	public void setSentDatum(@Nullable LocalDateTime sentDatum) {
		this.sentDatum = sentDatum;
	}

	@Nullable
	public JaxInstitution getInstitution() {
		return institution;
	}

	public void setInstitution(@Nullable JaxInstitution institution) {
		this.institution = institution;
	}

	@Nullable
	public MitteilungTyp getMitteilungTyp() {
		return mitteilungTyp;
	}

	public void setMitteilungTyp(@Nullable MitteilungTyp mitteilungTyp) {
		this.mitteilungTyp = mitteilungTyp;
	}
}
