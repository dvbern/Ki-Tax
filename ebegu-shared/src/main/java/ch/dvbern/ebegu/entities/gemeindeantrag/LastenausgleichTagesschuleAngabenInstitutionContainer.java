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

package ch.dvbern.ebegu.entities.gemeindeantrag;

import java.util.Objects;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.ForeignKey;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import ch.dvbern.ebegu.entities.AbstractEntity;
import ch.dvbern.ebegu.entities.Gemeinde;
import ch.dvbern.ebegu.entities.Gesuchsperiode;
import ch.dvbern.ebegu.entities.Institution;
import ch.dvbern.ebegu.enums.gemeindeantrag.GemeindeAntragTyp;
import ch.dvbern.ebegu.enums.gemeindeantrag.LastenausgleichTagesschuleAngabenInstitutionStatus;
import org.hibernate.envers.Audited;

@Audited
@Entity
public class LastenausgleichTagesschuleAngabenInstitutionContainer extends AbstractEntity implements GemeindeAntrag {

	private static final long serialVersionUID = -3965299440745733592L;

	@NotNull @Nonnull
	@ManyToOne(optional = false)
	@JoinColumn(foreignKey = @ForeignKey(name = "FK_lats_institution_container_gemeinde_container_id"), nullable = false)
	private LastenausgleichTagesschuleAngabenGemeindeContainer angabenGemeinde;

	@NotNull @Nonnull
	@Column(nullable = false)
	@Enumerated(EnumType.STRING)
	private LastenausgleichTagesschuleAngabenInstitutionStatus status = LastenausgleichTagesschuleAngabenInstitutionStatus.OFFEN;

	@NotNull @Nonnull
	@ManyToOne(optional = false)
	@JoinColumn(foreignKey = @ForeignKey(name = "FK_lats_institution_container_institution_id"), nullable = false)
	private Institution institution;

	@Nullable
	@Valid
	@OneToOne(optional = true, cascade = CascadeType.ALL, orphanRemoval = true)
	@JoinColumn(foreignKey = @ForeignKey(name = "FK_lats_institution_container_institutiondeklaration_id"), nullable = true)
	private LastenausgleichTagesschuleAngabenInstitution angabenDeklaration;

	@Nullable
	@Valid
	@OneToOne(optional = true, cascade = CascadeType.ALL, orphanRemoval = true)
	@JoinColumn(foreignKey = @ForeignKey(name = "FK_lats_institution_container_institutionkorrektur_id"), nullable = true)
	private LastenausgleichTagesschuleAngabenInstitution angabenKorrektur;


	@Nonnull
	public LastenausgleichTagesschuleAngabenGemeindeContainer getAngabenGemeinde() {
		return angabenGemeinde;
	}

	public void setAngabenGemeinde(@Nonnull LastenausgleichTagesschuleAngabenGemeindeContainer angabenGemeinde) {
		this.angabenGemeinde = angabenGemeinde;
	}

	@Nonnull
	public LastenausgleichTagesschuleAngabenInstitutionStatus getStatus() {
		return status;
	}

	public void setStatus(@Nonnull LastenausgleichTagesschuleAngabenInstitutionStatus status) {
		this.status = status;
	}

	@Nonnull
	public Institution getInstitution() {
		return institution;
	}

	public void setInstitution(@Nonnull Institution institution) {
		this.institution = institution;
	}

	@Nullable
	public LastenausgleichTagesschuleAngabenInstitution getAngabenDeklaration() {
		return angabenDeklaration;
	}

	public void setAngabenDeklaration(@Nullable LastenausgleichTagesschuleAngabenInstitution angabenDeklaration) {
		this.angabenDeklaration = angabenDeklaration;
	}

	@Nullable
	public LastenausgleichTagesschuleAngabenInstitution getAngabenKorrektur() {
		return angabenKorrektur;
	}

	public void setAngabenKorrektur(@Nullable LastenausgleichTagesschuleAngabenInstitution angabenKorrektur) {
		this.angabenKorrektur = angabenKorrektur;
	}

	@Override
	public boolean isSame(AbstractEntity other) {
		//noinspection ObjectEquality
		if (this == other) {
			return true;
		}
		if (!(other instanceof LastenausgleichTagesschuleAngabenInstitutionContainer)) {
			return false;
		}
		if (!super.equals(other)) {
			return false;
		}
		LastenausgleichTagesschuleAngabenInstitutionContainer that = (LastenausgleichTagesschuleAngabenInstitutionContainer) other;
		return getInstitution().equals(that.getInstitution()) &&
			Objects.equals(getAngabenDeklaration(), that.getAngabenDeklaration()) &&
			Objects.equals(getAngabenKorrektur(), that.getAngabenKorrektur());
	}

	public void copyForFreigabe() {
		// Nur moeglich, wenn noch nicht freigegeben und ueberhaupt Daten zum kopieren vorhanden
		if (status == LastenausgleichTagesschuleAngabenInstitutionStatus.OFFEN && angabenDeklaration != null) {
			angabenKorrektur = new LastenausgleichTagesschuleAngabenInstitution(angabenDeklaration);
		}
	}

	@Nonnull
	@Override
	public GemeindeAntragTyp getGemeindeAntragTyp() {
		return GemeindeAntragTyp.LASTENAUSGLEICH_TAGESSCHULEN;
	}

	@Nonnull
	@Override
	public Gemeinde getGemeinde() {
		return getAngabenGemeinde().getGemeinde();
	}

	@Nonnull
	@Override
	public Gesuchsperiode getGesuchsperiode() {
		return getAngabenGemeinde().getGesuchsperiode();
	}

	@Nonnull
	@Override
	public String getStatusString() {
		return status.name();
	}

	@Override
	public boolean isAntragAbgeschlossen() {
		return status == LastenausgleichTagesschuleAngabenInstitutionStatus.GEPRUEFT;
	}
}
