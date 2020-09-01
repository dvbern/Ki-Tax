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
import javax.validation.constraints.Size;

import ch.dvbern.ebegu.enums.AntragCopyType;
import ch.dvbern.oss.lib.beanvalidation.embeddables.IBAN;
import org.hibernate.envers.Audited;

import static ch.dvbern.ebegu.util.Constants.DB_DEFAULT_MAX_LENGTH;

/**
 * Entitaet zum Speichern von Zahlungsinformationen in der Datenbank.
 */
@Audited
@Entity
public class Auszahlungsdaten extends AbstractEntity {

	private static final long serialVersionUID = 1991251126987562205L;

	@Nonnull
	@Column(nullable = false)
	@Embedded
	@Valid
	private IBAN iban;

	@Nonnull
	@Size(max = DB_DEFAULT_MAX_LENGTH)
	@Column(nullable = false)
	private String kontoinhaber;

	@Nullable
	@OneToOne(optional = true, cascade = CascadeType.ALL, orphanRemoval = true)
	@JoinColumn(foreignKey = @ForeignKey(name = "FK_auszahlungsdaten_adressekontoinhaber_id"), nullable = true)
	private Adresse adresseKontoinhaber;


	@Nonnull
	public IBAN getIban() {
		return iban;
	}

	public void setIban(@Nonnull IBAN iban) {
		this.iban = iban;
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
	public boolean isSame(AbstractEntity other) {
		//noinspection ObjectEquality
		if (this == other) {
			return true;
		}
		if (other == null || !getClass().equals(other.getClass())) {
			return false;
		}
		if (!(other instanceof Auszahlungsdaten)) {
			return false;
		}
		final Auszahlungsdaten otherZahlung = (Auszahlungsdaten) other;
		return Objects.equals(getIban(), otherZahlung.getIban()) &&
			Objects.equals(getKontoinhaber(), otherZahlung.getKontoinhaber()) &&
			Objects.equals(getAdresseKontoinhaber(), otherZahlung.getAdresseKontoinhaber());
	}

	@Nonnull
	public Auszahlungsdaten copyAuszahlungsdaten(@Nonnull Auszahlungsdaten target, @Nonnull AntragCopyType copyType) {
		switch (copyType) {
		case MUTATION:
		case MUTATION_NEUES_DOSSIER:
			target.setIban(this.getIban());
			target.setKontoinhaber(this.getKontoinhaber());
			if (this.getAdresseKontoinhaber() != null) {
				target.setAdresseKontoinhaber(this.getAdresseKontoinhaber().copyAdresse(new Adresse(), copyType));
			}
			break;
		case ERNEUERUNG:
		case ERNEUERUNG_NEUES_DOSSIER:
			break;
		}
		return target;
	}
}
