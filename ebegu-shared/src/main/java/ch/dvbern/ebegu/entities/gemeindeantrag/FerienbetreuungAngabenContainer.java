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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package ch.dvbern.ebegu.entities.gemeindeantrag;

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
import javax.validation.constraints.Size;

import ch.dvbern.ebegu.entities.AbstractEntity;
import ch.dvbern.ebegu.entities.DokumentGrund;
import ch.dvbern.ebegu.entities.Gemeinde;
import ch.dvbern.ebegu.entities.Gesuchsperiode;
import ch.dvbern.ebegu.enums.gemeindeantrag.FerienbetreuungAngabenStatus;
import ch.dvbern.ebegu.enums.gemeindeantrag.GemeindeAntragTyp;
import ch.dvbern.ebegu.enums.gemeindeantrag.LastenausgleichTagesschuleAngabenGemeindeStatus;
import org.hibernate.envers.Audited;

import static ch.dvbern.ebegu.util.Constants.DB_TEXTAREA_LENGTH;

@Entity
@Audited
public class FerienbetreuungAngabenContainer extends AbstractEntity implements GemeindeAntrag {

	private static final long serialVersionUID = -3872331984799085800L;

	@NotNull
	@Nonnull
	@Column(nullable = false)
	@Enumerated(EnumType.STRING)
	private FerienbetreuungAngabenStatus status;

	@NotNull
	@Nonnull
	@ManyToOne(optional = false)
	@JoinColumn(foreignKey = @ForeignKey(name = "FK_ferienbetreuung_container_gemeinde_id"), nullable = false)
	private Gemeinde gemeinde;

	@NotNull
	@Nonnull
	@ManyToOne(optional = false)
	@JoinColumn(foreignKey = @ForeignKey(name = "FK_ferienbetreuung_container_gesuchsperiode_id"), nullable = false)
	private Gesuchsperiode gesuchsperiode;

	@Nonnull
	@Valid
	@OneToOne(optional = false, cascade = CascadeType.ALL, orphanRemoval = true)
	@JoinColumn(foreignKey = @ForeignKey(name = "FK_ferienbetreuung_container_deklaration_id"), nullable = true)
	private FerienbetreuungAngaben angabenDeklaration;

	@Nullable
	@Valid
	@OneToOne(optional = true, cascade = CascadeType.ALL, orphanRemoval = true)
	@JoinColumn(foreignKey = @ForeignKey(name = "FK_ferienbetreuung_container_korrektur_id"), nullable = true)
	private FerienbetreuungAngaben angabenKorrektur;

	@Nullable
	@Size(max = DB_TEXTAREA_LENGTH)
	@Column(nullable = true)
	private String internerKommentar;

	@Nullable
	@Valid
	@OneToMany(mappedBy = "ferienbetreuungAngabenContainer")
	private Set<FerienbetreuungDokument> dokumente;

	@Nonnull
	public FerienbetreuungAngabenStatus getStatus() {
		return status;
	}

	public void setStatus(@Nonnull FerienbetreuungAngabenStatus status) {
		this.status = status;
	}

	@Nonnull
	@Override
	public GemeindeAntragTyp getGemeindeAntragTyp() {
		return GemeindeAntragTyp.FERIENBETREUUNG;
	}

	@Nonnull
	public Gemeinde getGemeinde() {
		return gemeinde;
	}

	public void setGemeinde(@Nonnull Gemeinde gemeinde) {
		this.gemeinde = gemeinde;
	}

	@Nonnull
	public Gesuchsperiode getGesuchsperiode() {
		return gesuchsperiode;
	}

	@Nonnull
	@Override
	public String getStatusString() {
		return status.toString();
	}

	@Override
	public boolean isAntragAbgeschlossen() {
		return status == FerienbetreuungAngabenStatus.VERFUEGT
			|| status == FerienbetreuungAngabenStatus.ABGELEHNT;
	}

	public void setGesuchsperiode(@Nonnull Gesuchsperiode gesuchsperiode) {
		this.gesuchsperiode = gesuchsperiode;
	}

	@Nonnull
	public FerienbetreuungAngaben getAngabenDeklaration() {
		return angabenDeklaration;
	}

	public void setAngabenDeklaration(@Nonnull FerienbetreuungAngaben angabenDeklaration) {
		this.angabenDeklaration = angabenDeklaration;
	}

	@Nullable
	public FerienbetreuungAngaben getAngabenKorrektur() {
		return angabenKorrektur;
	}

	public void setAngabenKorrektur(@Nullable FerienbetreuungAngaben angabenKorrektur) {
		this.angabenKorrektur = angabenKorrektur;
	}

	@Nullable
	public String getInternerKommentar() {
		return internerKommentar;
	}

	public void setInternerKommentar(@Nullable String internerKommentar) {
		this.internerKommentar = internerKommentar;
	}

	@Override
	public boolean isSame(AbstractEntity other) {
		return getId().equals(other.getId());
	}

	public boolean isAtLeastInPruefungKanton() {
		return status == FerienbetreuungAngabenStatus.IN_PRUEFUNG_KANTON ||
			status == FerienbetreuungAngabenStatus.GEPRUEFT ||
			status == FerienbetreuungAngabenStatus.VERFUEGT ||
			status == FerienbetreuungAngabenStatus.ABGELEHNT;
	}

	public boolean isReadyForGeprueft() {
		if(getAngabenKorrektur() == null) {
			return false;
		}

		return getAngabenKorrektur().getFerienbetreuungAngabenStammdaten().isAbgeschlossen() &&
			getAngabenKorrektur().getFerienbetreuungAngabenAngebot().isAbgeschlossen() &&
			getAngabenKorrektur().getFerienbetreuungAngabenNutzung().isAbgeschlossen() &&
			getAngabenKorrektur().getFerienbetreuungAngabenKostenEinnahmen().isAbgeschlossen();
	}

	@Nullable
	public Set<FerienbetreuungDokument> getDokumente() {
		return dokumente;
	}

	public void setDokumente(@Nullable Set<FerienbetreuungDokument> dokumente) {
		this.dokumente = dokumente;
	}

	public boolean isInPruefungKanton() {
		return status == FerienbetreuungAngabenStatus.IN_PRUEFUNG_KANTON;
	}

	public boolean isGeprueft() {
		return status == FerienbetreuungAngabenStatus.GEPRUEFT ||
			status == FerienbetreuungAngabenStatus.VERFUEGT ||
			status == FerienbetreuungAngabenStatus.ABGELEHNT;
	}

	public void copyForFreigabe() {
		// Nur moeglich, wenn noch nicht freigegeben und ueberhaupt Daten zum kopieren vorhanden
		if (status == FerienbetreuungAngabenStatus.IN_BEARBEITUNG_GEMEINDE && angabenKorrektur == null) {
			angabenKorrektur = new FerienbetreuungAngaben(angabenDeklaration);
		}
	}

	public boolean isInBearbeitungGemeinde() {
		return status == FerienbetreuungAngabenStatus.IN_BEARBEITUNG_GEMEINDE;
	}
}
