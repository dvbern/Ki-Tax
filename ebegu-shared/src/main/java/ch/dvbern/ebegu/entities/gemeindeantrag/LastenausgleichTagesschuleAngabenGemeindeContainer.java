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

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Objects;
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
import ch.dvbern.ebegu.entities.Gemeinde;
import ch.dvbern.ebegu.entities.Gesuchsperiode;
import ch.dvbern.ebegu.enums.gemeindeantrag.GemeindeAntragTyp;
import ch.dvbern.ebegu.enums.gemeindeantrag.LastenausgleichTagesschuleAngabenGemeindeFormularStatus;
import ch.dvbern.ebegu.enums.gemeindeantrag.LastenausgleichTagesschuleAngabenGemeindeStatus;
import ch.dvbern.ebegu.enums.gemeindeantrag.LastenausgleichTagesschuleAngabenInstitutionStatus;
import ch.dvbern.ebegu.validators.CheckLastenausgleichTagesschuleAngabenGemeinde;
import com.google.common.base.Preconditions;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.hibernate.envers.Audited;

import static ch.dvbern.ebegu.util.Constants.DB_TEXTAREA_LENGTH;

@Audited
@Entity
@CheckLastenausgleichTagesschuleAngabenGemeinde
public class LastenausgleichTagesschuleAngabenGemeindeContainer extends AbstractEntity implements GemeindeAntrag {

	private static final long serialVersionUID = -149964317716679424L;

	@NotNull
	@Nonnull
	@Column(nullable = false)
	@Enumerated(EnumType.STRING)
	private LastenausgleichTagesschuleAngabenGemeindeStatus status;

	@NotNull
	@Nonnull
	@ManyToOne(optional = false)
	@JoinColumn(foreignKey = @ForeignKey(name = "FK_lats_fall_container_gemeinde_id"), nullable = false)
	private Gemeinde gemeinde;

	@NotNull
	@Nonnull
	@ManyToOne(optional = false)
	@JoinColumn(foreignKey = @ForeignKey(name = "FK_lats_fall_container_gesuchsperiode_id"), nullable = false)
	private Gesuchsperiode gesuchsperiode;

	@Nullable // Muss leider nullable sein, da der Container schon "leer" gespeichert werden muss
	@Column(nullable = true)
	private Boolean alleAngabenInKibonErfasst;

	@Nullable
	@Size(max = DB_TEXTAREA_LENGTH)
	@Column(nullable = true)
	private String internerKommentar;

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

	@Nullable
	@Column(nullable = true)
	private BigDecimal betreuungsstundenPrognose;

	@Nullable
	@Column(nullable = true)
	private String bemerkungenBetreuungsstundenPrognose;

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
	public String getInternerKommentar() {
		return internerKommentar;
	}

	public void setInternerKommentar(@Nullable String internerKommentar) {
		this.internerKommentar = internerKommentar;
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

	public void setAngabenInstitutionContainers(
		@Nonnull Set<LastenausgleichTagesschuleAngabenInstitutionContainer> angabenInstitutionContainers) {
		this.angabenInstitutionContainers = angabenInstitutionContainers;
	}

	@Override
	@SuppressWarnings("PMD.CompareObjectsWithEquals")
	@SuppressFBWarnings("BC_UNCONFIRMED_CAST")
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
		LastenausgleichTagesschuleAngabenGemeindeContainer that =
			(LastenausgleichTagesschuleAngabenGemeindeContainer) other;
		return getGemeinde().equals(that.getGemeinde()) &&
			getGesuchsperiode().equals(that.getGesuchsperiode());
	}

	public void copyForFreigabe() {
		// Nur moeglich, wenn noch nicht freigegeben und ueberhaupt Daten zum kopieren vorhanden
		// Wir kopieren nicht, wenn Kanton bereits Daten erfasst hat
		// falls der Antrag zurück an die Gemeinde gegeben wurde, werden durch die Gemeinde direkt die
		// angabenkorrektur bearbeitet. In diesem Fall muss nicht kopiert werden.
		if (status == LastenausgleichTagesschuleAngabenGemeindeStatus.IN_BEARBEITUNG_GEMEINDE) {
			angabenKorrektur = new LastenausgleichTagesschuleAngabenGemeinde(angabenDeklaration);
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
		return status == LastenausgleichTagesschuleAngabenGemeindeStatus.ABGESCHLOSSEN;
	}

	public boolean isReadyForFreigabe() {
		return status == LastenausgleichTagesschuleAngabenGemeindeStatus.IN_BEARBEITUNG_GEMEINDE &&
			isAngabenDeklarationAbgeschlossen() &&
			allInstitutionenGeprueft();
	}

	@CanIgnoreReturnValue
	public boolean addLastenausgleichTagesschuleAngabenInstitutionContainer(
		@Nonnull final LastenausgleichTagesschuleAngabenInstitutionContainer institutionContainerToAdd) {
		institutionContainerToAdd.setAngabenGemeinde(this);
		return !angabenInstitutionContainers.contains(institutionContainerToAdd)
			&& angabenInstitutionContainers.add(institutionContainerToAdd);
	}

	public boolean allInstitutionenGeprueft() {
		for (LastenausgleichTagesschuleAngabenInstitutionContainer institutionContainer :
			this.getAngabenInstitutionContainers()) {
			if (institutionContainer.getStatus() != LastenausgleichTagesschuleAngabenInstitutionStatus.GEPRUEFT) {
				return false;
			}
		}
		return true;
	}

	public boolean angabenDeklarationComplete() {
		if (Objects.isNull(getAlleAngabenInKibonErfasst())) {
			return false;
		}
		if (Objects.isNull(getAngabenDeklaration())) {
			return false;
		}
		LastenausgleichTagesschuleAngabenGemeinde deklaration = getAngabenDeklaration();
		return angabenComplete(deklaration);
	}

	public boolean angabenKorrekturComplete() {
		if (Objects.isNull(getAlleAngabenInKibonErfasst())) {
			return false;
		}
		if (Objects.isNull(getAngabenKorrektur())) {
			return false;
		}
		LastenausgleichTagesschuleAngabenGemeinde korrektur = getAngabenKorrektur();
		return angabenComplete(korrektur);
	}

	private boolean angabenComplete(LastenausgleichTagesschuleAngabenGemeinde deklaration) {
		if (Objects.isNull(deklaration.getBedarfBeiElternAbgeklaert())) {
			return false;
		}
		if (Objects.isNull(deklaration.getAngebotFuerFerienbetreuungVorhanden())) {
			return false;
		}
		if (Objects.isNull(deklaration.getAngebotVerfuegbarFuerAlleSchulstufen())) {
			return false;
		}
		if (!deklaration.getAngebotVerfuegbarFuerAlleSchulstufen()
			&& Objects.isNull(deklaration.getBegruendungWennAngebotNichtVerfuegbarFuerAlleSchulstufen())) {
			return false;
		}
		if (Objects.isNull(deklaration.getGeleisteteBetreuungsstundenBesondereBeduerfnisse())) {
			return false;
		}
		if (Objects.isNull(deklaration.getGeleisteteBetreuungsstundenOhneBesondereBeduerfnisse())) {
			return false;
		}
		if (Objects.isNull(deklaration.getDavonStundenZuNormlohnMehrAls50ProzentAusgebildete())) {
			return false;
		}
		if (Objects.isNull(deklaration.getDavonStundenZuNormlohnWenigerAls50ProzentAusgebildete())) {
			return false;
		}
		if (Objects.isNull(deklaration.getEinnahmenElterngebuehren())) {
			return false;
		}
		if (Objects.isNull(deklaration.getBetreuungsstundenDokumentiertUndUeberprueft())) {
			return false;
		}
		if (Objects.isNull(deklaration.getElterngebuehrenGemaessVerordnungBerechnet())) {
			return false;
		}
		if (Objects.isNull(deklaration.getEinkommenElternBelegt())) {
			return false;
		}
		if (Objects.isNull(deklaration.getMaximalTarif())) {
			return false;
		}
		if (Objects.isNull(deklaration.getMindestens50ProzentBetreuungszeitDurchAusgebildetesPersonal())) {
			return false;
		}
		if (Objects.isNull(deklaration.getAusbildungenMitarbeitendeBelegt())) {
			return false;
		}
		return true;
	}

	public boolean plausibilisierungTagesschulenStundenHoldsForDeklaration() {
		LastenausgleichTagesschuleAngabenGemeinde formular;
		if (isAtLeastInBearbeitungKantonOrZuerueckgegeben()) {
			formular = getAngabenKorrektur();
		} else {
			formular = getAngabenDeklaration();
		}
		Preconditions.checkState(formular != null, "angabenDeklaration must not be null");
		Preconditions.checkState(formular.getGeleisteteBetreuungsstundenBesondereBeduerfnisse()
			!= null, "angabenDeklaration incomplete");
		Preconditions.checkState(formular.getGeleisteteBetreuungsstundenOhneBesondereBeduerfnisse()
			!= null, "angabenDeklaration incomplete");

		BigDecimal sumTagesschulen = getAngabenInstitutionContainers().stream()
			.map(container -> {
				// we should not be here if there are tagesschule formulare that are not geprueft
				Preconditions.checkArgument(
					container.getAngabenKorrektur() != null && container.isAntragAbgeschlossen(),
					"angabenDeklaration Tagesschulen incomplete"
				);
				return container.getAngabenKorrektur().getBetreuungsstundenEinschliesslichBesondereBeduerfnisse();

			}).reduce(BigDecimal.ZERO, BigDecimal::add);

		return formular.getGeleisteteBetreuungsstundenBesondereBeduerfnisse()
			.add(formular.getGeleisteteBetreuungsstundenOhneBesondereBeduerfnisse())
			.compareTo(sumTagesschulen) == 0;
	}

	public boolean isAtLeastInBearbeitungKanton() {
		return status == LastenausgleichTagesschuleAngabenGemeindeStatus.IN_PRUEFUNG_KANTON ||
			status == LastenausgleichTagesschuleAngabenGemeindeStatus.GEPRUEFT ||
			status == LastenausgleichTagesschuleAngabenGemeindeStatus.ABGESCHLOSSEN ||
			status == LastenausgleichTagesschuleAngabenGemeindeStatus.ZWEITPRUEFUNG;
	}

	public boolean isAtLeastInBearbeitungKantonOrZuerueckgegeben() {
		return isAtLeastInBearbeitungKanton()
			|| status == LastenausgleichTagesschuleAngabenGemeindeStatus.ZURUECK_AN_GEMEINDE;
	}

	public boolean isAngabenDeklarationAbgeschlossen() {
		return angabenDeklaration != null
			&& angabenDeklaration.getStatus() == LastenausgleichTagesschuleAngabenGemeindeFormularStatus.ABGESCHLOSSEN;
	}

	public boolean isAngabenKorrekturAbgeschlossen() {
		return angabenKorrektur != null
			&& angabenKorrektur.getStatus() == LastenausgleichTagesschuleAngabenGemeindeFormularStatus.ABGESCHLOSSEN;
	}

	public boolean isInBearbeitungGemeinde() {
		return status == LastenausgleichTagesschuleAngabenGemeindeStatus.IN_BEARBEITUNG_GEMEINDE
			|| status == LastenausgleichTagesschuleAngabenGemeindeStatus.ZURUECK_AN_GEMEINDE;
	}

	public boolean isAntragGeprueft() {
		return status == LastenausgleichTagesschuleAngabenGemeindeStatus.GEPRUEFT;
	}

	public boolean isInPruefungKanton() {
		return status == LastenausgleichTagesschuleAngabenGemeindeStatus.IN_PRUEFUNG_KANTON ||
			status == LastenausgleichTagesschuleAngabenGemeindeStatus.ZWEITPRUEFUNG;
	}

	public boolean isInStatusNeu() {
		return status == LastenausgleichTagesschuleAngabenGemeindeStatus.NEU;
	}

	public boolean isInZweitpruefung() {
		return status == LastenausgleichTagesschuleAngabenGemeindeStatus.ZWEITPRUEFUNG;
	}

	@Nullable
	public BigDecimal getBetreuungsstundenPrognose() {
		return betreuungsstundenPrognose;
	}

	public void setBetreuungsstundenPrognose(@Nullable BigDecimal betreuungsstundenPrognose) {
		this.betreuungsstundenPrognose = betreuungsstundenPrognose;
	}

	public boolean isAtLeastInPruefungKantonOrZurueckgegeben() {
		return this.status.atLeastInPruefungKantonOrZurueckgegeben();
	}

	@Nullable
	public String getBemerkungenBetreuungsstundenPrognose() {
		return bemerkungenBetreuungsstundenPrognose;
	}

	public void setBemerkungenBetreuungsstundenPrognose(@Nullable String bemerkungenBetreuungsstundenPrognose) {
		this.bemerkungenBetreuungsstundenPrognose = bemerkungenBetreuungsstundenPrognose;
	}
}
