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

import java.util.HashSet;
import java.util.Set;

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
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import ch.dvbern.ebegu.entities.AbstractEntity;
import ch.dvbern.ebegu.entities.Gemeinde;
import ch.dvbern.ebegu.entities.Gesuchsperiode;
import ch.dvbern.ebegu.enums.gemeindeantrag.GemeindeAntragTyp;
import ch.dvbern.ebegu.enums.gemeindeantrag.LastenausgleichTagesschuleAngabenGemeindeStatus;
import ch.dvbern.ebegu.validators.CheckLastenausgleichTagesschuleAngabenGemeinde;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import org.hibernate.envers.Audited;

@Audited
@Entity
@CheckLastenausgleichTagesschuleAngabenGemeinde
public class LastenausgleichTagesschuleAngabenGemeindeContainer extends AbstractEntity implements GemeindeAntrag {

	private static final long serialVersionUID = -149964317716679424L;

	@NotNull @Nonnull
	@Column(nullable = false)
	@Enumerated(EnumType.STRING)
	private LastenausgleichTagesschuleAngabenGemeindeStatus status;

	@NotNull @Nonnull
	@ManyToOne(optional = false)
	@JoinColumn(foreignKey = @ForeignKey(name = "FK_lats_fall_container_gemeinde_id"), nullable = false)
	private Gemeinde gemeinde;

	@NotNull @Nonnull
	@ManyToOne(optional = false)
	@JoinColumn(foreignKey = @ForeignKey(name = "FK_lats_fall_container_gesuchsperiode_id"), nullable = false)
	private Gesuchsperiode gesuchsperiode;

	@Nullable // Muss leider nullable sein, da der Container schon "leer" gespeichert werden muss
	@Column(nullable = true)
	private Boolean alleAngabenInKibonErfasst;

	@Nullable
	@Valid
	@OneToOne(optional = true, cascade = CascadeType.ALL, orphanRemoval = true)
	@JoinColumn(foreignKey = @ForeignKey(name = "FK_lats_fall_container_falldeklaration_id"), nullable = true)
	private LastenausgleichTagesschuleAngabenGemeinde angabenDeklaration;

	@Nullable
	@Valid
	@OneToOne(optional = true, cascade = CascadeType.ALL, orphanRemoval = true)
	@JoinColumn(foreignKey = @ForeignKey(name = "FK_lats_fall_container_fallkorrektur_id"), nullable = true)
	private LastenausgleichTagesschuleAngabenGemeinde angabenKorrektur;

	@Nonnull
	@Valid
	@OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, mappedBy = "angabenGemeinde")
	private Set<LastenausgleichTagesschuleAngabenInstitutionContainer> angabenInstitutionContainers = new HashSet<>();


	@Nonnull
	public LastenausgleichTagesschuleAngabenGemeindeStatus getStatus() {
		return status;
	}

	public void setStatus(@Nonnull LastenausgleichTagesschuleAngabenGemeindeStatus status) {
		this.status = status;
	}

	@Override
	@Nonnull
	public Gemeinde getGemeinde() {
		return gemeinde;
	}

	public void setGemeinde(@Nonnull Gemeinde gemeinde) {
		this.gemeinde = gemeinde;
	}

	@Override
	@Nonnull
	public Gesuchsperiode getGesuchsperiode() {
		return gesuchsperiode;
	}

	public void setGesuchsperiode(@Nonnull Gesuchsperiode gesuchsperiode) {
		this.gesuchsperiode = gesuchsperiode;
	}

	@Nullable
	public Boolean getAlleAngabenInKibonErfasst() {
		return alleAngabenInKibonErfasst;
	}

	public void setAlleAngabenInKibonErfasst(@Nullable Boolean alleAngabenInKibonErfasst) {
		this.alleAngabenInKibonErfasst = alleAngabenInKibonErfasst;
	}

	@Nullable
	public LastenausgleichTagesschuleAngabenGemeinde getAngabenDeklaration() {
		return angabenDeklaration;
	}

	public void setAngabenDeklaration(@Nullable LastenausgleichTagesschuleAngabenGemeinde fallDeklaration) {
		this.angabenDeklaration = fallDeklaration;
	}

	@Nullable
	public LastenausgleichTagesschuleAngabenGemeinde getAngabenKorrektur() {
		return angabenKorrektur;
	}

	public void setAngabenKorrektur(@Nullable LastenausgleichTagesschuleAngabenGemeinde fallKorrektur) {
		this.angabenKorrektur = fallKorrektur;
	}

	@Nonnull
	public Set<LastenausgleichTagesschuleAngabenInstitutionContainer> getAngabenInstitutionContainers() {
		return angabenInstitutionContainers;
	}

	public void setAngabenInstitutionContainers(@Nonnull Set<LastenausgleichTagesschuleAngabenInstitutionContainer> angabenInstitutionContainers) {
		this.angabenInstitutionContainers = angabenInstitutionContainers;
	}

	@Override
	public boolean isSame(AbstractEntity other) {
		//noinspection ObjectEquality
		if (this == other) {
			return true;
		}
		if (!(other instanceof LastenausgleichTagesschuleAngabenGemeindeContainer)) {
			return false;
		}
		if (!super.equals(other)) {
			return false;
		}
		LastenausgleichTagesschuleAngabenGemeindeContainer that = (LastenausgleichTagesschuleAngabenGemeindeContainer) other;
		return getGemeinde().equals(that.getGemeinde()) &&
			getGesuchsperiode().equals(that.getGesuchsperiode());
	}

	public void copyForFreigabe() {
		// Nur moeglich, wenn noch nicht freigegeben und ueberhaupt Daten zum kopieren vorhanden
		if (status == LastenausgleichTagesschuleAngabenGemeindeStatus.IN_BEARBEITUNG_GEMEINDE && angabenKorrektur != null) {
			angabenDeklaration = new LastenausgleichTagesschuleAngabenGemeinde(angabenKorrektur);
		}
	}

	@Nonnull
	@Override
	public String getStatusString() {
		return status.toString();
	}

	@Nonnull
	@Override
	public GemeindeAntragTyp getGemeindeAntragTyp() {
		return GemeindeAntragTyp.LASTENAUSGLEICH_TAGESSCHULEN;
	}

	@Override
	public boolean isAntragAbgeschlossen() {
		return status == LastenausgleichTagesschuleAngabenGemeindeStatus.VERFUEGT;
	}

	@CanIgnoreReturnValue
	public boolean addLastenausgleichTagesschuleAngabenInstitutionContainer(@Nonnull final LastenausgleichTagesschuleAngabenInstitutionContainer institutionContainerToAdd) {
		institutionContainerToAdd.setAngabenGemeinde(this);
		return !angabenInstitutionContainers.contains(institutionContainerToAdd)
			&& angabenInstitutionContainers.add(institutionContainerToAdd);
	}
}
