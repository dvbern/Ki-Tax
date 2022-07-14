/*
 * Copyright (C) 2020 DV Bern AG, Switzerland
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

package ch.dvbern.ebegu.entities;

import java.util.Objects;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.ForeignKey;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import ch.dvbern.ebegu.enums.AntragCopyType;
import ch.dvbern.ebegu.validators.CheckAnyAuszahlungskontoSet;
import ch.dvbern.ebegu.validators.CheckIBANUppercase;
import ch.dvbern.oss.lib.beanvalidation.embeddables.IBAN;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.apache.commons.lang.StringUtils;
import org.hibernate.envers.Audited;

import static ch.dvbern.ebegu.util.Constants.DB_DEFAULT_MAX_LENGTH;

/**
 * Entitaet zum Speichern von Zahlungsinformationen in der Datenbank.
 */
@Audited
@Entity
@CheckAnyAuszahlungskontoSet
public class Auszahlungsdaten extends AbstractEntity {

	private static final long serialVersionUID = 1991251126987562205L;

	@Nullable
	@Column(nullable = true)
	@Embedded
	@CheckIBANUppercase
	@Valid
	private IBAN iban;

	@Nullable
	@Column(nullable = true, length = DB_DEFAULT_MAX_LENGTH)
	@Size(max = DB_DEFAULT_MAX_LENGTH)
	private String infomaKreditorennummer;

	@Nullable
	@Column(nullable = true, length = DB_DEFAULT_MAX_LENGTH)
	@Size(max = DB_DEFAULT_MAX_LENGTH)
	private String infomaBankcode;

	@NotNull
	@Nonnull
	@Size(max = DB_DEFAULT_MAX_LENGTH)
	@Column(nullable = false)
	private String kontoinhaber;

	@Nullable
	@OneToOne(optional = true, cascade = CascadeType.ALL, orphanRemoval = true)
	@JoinColumn(foreignKey = @ForeignKey(name = "FK_auszahlungsdaten_adressekontoinhaber_id"), nullable = true)
	private Adresse adresseKontoinhaber;

	@Nullable
	public IBAN getIban() {
		return iban;
	}

	public void setIban(@Nullable IBAN iban) {
		this.iban = iban;
	}

	@Nullable
	public String getInfomaKreditorennummer() {
		return infomaKreditorennummer;
	}

	public void setInfomaKreditorennummer(@Nullable String infomaKontonummer) {
		this.infomaKreditorennummer = infomaKontonummer;
	}

	@Nullable
	public String getInfomaBankcode() {
		return infomaBankcode;
	}

	public void setInfomaBankcode(@Nullable String infomaBankcode) {
		this.infomaBankcode = infomaBankcode;
	}

	@Nonnull
	public String getKontoinhaber() {
		return kontoinhaber;
	}

	public void setKontoinhaber(@Nonnull String kontoinhaber) {
		this.kontoinhaber = kontoinhaber;
	}

	@Nullable
	public Adresse getAdresseKontoinhaber() {
		return adresseKontoinhaber;
	}

	public void setAdresseKontoinhaber(@Nullable Adresse adresseKontoinhaber) {
		this.adresseKontoinhaber = adresseKontoinhaber;
	}

	@Override
	@SuppressWarnings("PMD.CompareObjectsWithEquals")
	@SuppressFBWarnings("BC_UNCONFIRMED_CAST")
	public boolean isSame(AbstractEntity other) {
		//noinspection ObjectEquality
		if (this == other) {
			return true;
		}
		if (other == null || !getClass().equals(other.getClass())) {
			return false;
		}
		final Auszahlungsdaten otherZahlung = (Auszahlungsdaten) other;
		return Objects.equals(getIban(), otherZahlung.getIban()) &&
			Objects.equals(getInfomaKreditorennummer(), otherZahlung.getInfomaKreditorennummer()) &&
			Objects.equals(getInfomaBankcode(), otherZahlung.getInfomaBankcode()) &&
			Objects.equals(getKontoinhaber(), otherZahlung.getKontoinhaber()) &&
			Objects.equals(getAdresseKontoinhaber(), otherZahlung.getAdresseKontoinhaber());
	}

	@Nonnull
	public Auszahlungsdaten copyAuszahlungsdaten(@Nonnull Auszahlungsdaten target, @Nonnull AntragCopyType copyType) {
		target.setIban(this.getIban());
		target.setKontoinhaber(this.getKontoinhaber());
		if (this.getAdresseKontoinhaber() != null) {
			target.setAdresseKontoinhaber(this.getAdresseKontoinhaber().copyAdresse(new Adresse(), copyType));
		}
		target.setInfomaKreditorennummer(this.getInfomaKreditorennummer());
		target.setInfomaBankcode(this.getInfomaBankcode());
		return target;
	}

	public boolean isZahlungsinformationValid() {
		return StringUtils.isNotEmpty(kontoinhaber)
			&& StringUtils.isNotEmpty(getIbanOrInfomaKreditorennummer());
	}

	@Nullable
	public String extractIbanAsString() {
		return iban != null ? iban.getIban() : null;
	}

	@Nonnull
	public String getIbanOrInfomaKreditorennummer() {
		String kontonr = extractIbanAsString();
		if (StringUtils.isNotEmpty(kontonr)) {
			return kontonr;
		}
		kontonr = getInfomaKreditorennummer();
		Objects.requireNonNull(kontonr);
		return kontonr;
	}
}
