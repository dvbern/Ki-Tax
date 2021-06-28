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

import java.time.LocalDateTime;
import java.util.Objects;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.ForeignKey;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import ch.dvbern.ebegu.enums.MitteilungStatus;
import ch.dvbern.ebegu.enums.MitteilungTeilnehmerTyp;
import ch.dvbern.ebegu.util.EbeguUtil;
import ch.dvbern.ebegu.validators.CheckMitteilungCompleteness;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.hibernate.envers.Audited;

import static ch.dvbern.ebegu.util.Constants.DB_DEFAULT_MAX_LENGTH;
import static ch.dvbern.ebegu.util.Constants.DB_TEXTAREA_LENGTH;

/**
 * Entitaet zum Speichern von Mitteilungen in der Datenbank.
 */
@Audited
@Entity
@CheckMitteilungCompleteness
public class Mitteilung extends AbstractMutableEntity {

	private static final long serialVersionUID = 489324250198016526L;

	@NotNull @Nonnull
	@ManyToOne(optional = false)
	@JoinColumn(foreignKey = @ForeignKey(name = "FK_mitteilung_dossier_id"))
	private Dossier dossier;

	@Nullable
	@ManyToOne(optional = true)
	@JoinColumn(foreignKey = @ForeignKey(name = "FK_mitteilung_betreuung_id"))
	private Betreuung betreuung;

	@NotNull @Nonnull
	@Column(nullable = false)
	@Enumerated(EnumType.STRING)
	private MitteilungTeilnehmerTyp senderTyp;

	@NotNull @Nonnull
	@Column(nullable = false)
	@Enumerated(EnumType.STRING)
	private MitteilungTeilnehmerTyp empfaengerTyp;

	@NotNull @Nonnull
	@ManyToOne(optional = false)
	@JoinColumn(foreignKey = @ForeignKey(name = "FK_Mitteilung_sender"))
	private Benutzer sender;

	@Nullable
	@ManyToOne(optional = true)
	@JoinColumn(foreignKey = @ForeignKey(name = "FK_Mitteilung_empfaenger"))
	private Benutzer empfaenger;

	@Size(min = 0, max = DB_DEFAULT_MAX_LENGTH)
	@Column(nullable = true)
	@Nullable
	private String subject;

	@Size(min = 0, max = DB_TEXTAREA_LENGTH)
	@Column(nullable = true, length = DB_TEXTAREA_LENGTH)
	@Nullable
	private String message;

	@NotNull @Nonnull
	@Column(nullable = false)
	@Enumerated(EnumType.STRING)
	private MitteilungStatus mitteilungStatus;

	@Nullable
	@Column(nullable = true)
	private LocalDateTime sentDatum;

	@Nullable
	@ManyToOne(optional = true)
	@JoinColumn(foreignKey = @ForeignKey(name = "FK_mitteilung_institution_id"))
	private Institution institution;

	@NotNull
	public Fall getFall() {
		return dossier.getFall();
	}

	@Nonnull
	public Dossier getDossier() {
		return dossier;
	}

	public void setDossier(@Nonnull Dossier dossier) {
		this.dossier = dossier;
	}

	@Nullable
	public Betreuung getBetreuung() {
		return betreuung;
	}

	public void setBetreuung(@Nullable Betreuung betreuung) {
		this.betreuung = betreuung;
	}

	@Nonnull
	public MitteilungTeilnehmerTyp getSenderTyp() {
		return senderTyp;
	}

	public void setSenderTyp(@Nonnull MitteilungTeilnehmerTyp senderTyp) {
		this.senderTyp = senderTyp;
	}

	@Nonnull
	public MitteilungTeilnehmerTyp getEmpfaengerTyp() {
		return empfaengerTyp;
	}

	public void setEmpfaengerTyp(@Nonnull MitteilungTeilnehmerTyp empfaengerTyp) {
		this.empfaengerTyp = empfaengerTyp;
	}

	@Nonnull
	public Benutzer getSender() {
		return sender;
	}

	public void setSender(@Nonnull Benutzer sender) {
		this.sender = sender;
	}

	@Nullable
	public Benutzer getEmpfaenger() {
		return empfaenger;
	}

	public void setEmpfaenger(@Nullable Benutzer empfaenger) {
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

	@Nonnull
	public MitteilungStatus getMitteilungStatus() {
		return mitteilungStatus;
	}

	public void setMitteilungStatus(@Nonnull MitteilungStatus mitteilungStatus) {
		this.mitteilungStatus = mitteilungStatus;
	}

	@Nullable
	public LocalDateTime getSentDatum() {
		return sentDatum;
	}

	public void setSentDatum(@Nullable LocalDateTime sentDatum) {
		this.sentDatum = sentDatum;
	}

	@SuppressWarnings({ "OverlyComplexBooleanExpression", "OverlyComplexMethod" })
	@Override
	public boolean isSame(AbstractEntity other) {
		//noinspection ObjectEquality
		if (this == other) {
			return true;
		}
		if (other == null || !getClass().equals(other.getClass())) {
			return false;
		}
		if (!(other instanceof Mitteilung)) {
			return false;
		}
		final Mitteilung otherMitteilung = (Mitteilung) other;
		return EbeguUtil.isSame(getBetreuung(), otherMitteilung.getBetreuung()) &&
			Objects.equals(getSender().getId(), otherMitteilung.getSender().getId()) &&
			getSenderTyp() == otherMitteilung.getSenderTyp() &&
			Objects.equals(getSentDatum(), otherMitteilung.getSentDatum()) &&
			EbeguUtil.isSame(getEmpfaenger(), otherMitteilung.getEmpfaenger()) &&
			getEmpfaengerTyp() == otherMitteilung.getEmpfaengerTyp() &&
			Objects.equals(getSubject(), otherMitteilung.getSubject()) &&
			Objects.equals(getMessage(), otherMitteilung.getMessage()) &&
			getMitteilungStatus() == otherMitteilung.getMitteilungStatus();
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this)
			.append("senderTyp", senderTyp)
			.append("empfaengerTyp", empfaengerTyp)
			.append("sender", sender)
			.append("empfaenger", empfaenger)
			.append("mitteilungStatus", mitteilungStatus)
			.toString();
	}

	@Nullable
	public Institution getInstitution() {
		return institution;
	}

	public void setInstitution(@Nullable Institution institution) {
		this.institution = institution;
	}
}
