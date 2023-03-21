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

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
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
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import ch.dvbern.ebegu.enums.AntragCopyType;
import ch.dvbern.ebegu.enums.EnumFamilienstatus;
import ch.dvbern.ebegu.enums.EnumGesuchstellerKardinalitaet;
import ch.dvbern.ebegu.enums.UnterhaltsvereinbarungAnswer;
import ch.dvbern.ebegu.util.EbeguUtil;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.hibernate.envers.Audited;

import static ch.dvbern.ebegu.util.Constants.DB_DEFAULT_MAX_LENGTH;
import static ch.dvbern.ebegu.util.Constants.DB_TEXTAREA_LENGTH;

/**
 * Entitaet zum Speichern von Familiensituation in der Datenbank.
 */
@Audited
@Entity
@Table(uniqueConstraints = {
	@UniqueConstraint(columnNames = "auszahlungsdaten_id",
		name = "UK_familiensituation_auszahlungsdaten_id")
})
public class Familiensituation extends AbstractMutableEntity {

	private static final long serialVersionUID = -6534582356181164632L;

	@Enumerated(EnumType.STRING)
	@NotNull
	@Column(nullable = false)
	private EnumFamilienstatus familienstatus;

	@Nullable
	@Column(nullable = true)
	private Boolean gemeinsameSteuererklaerung;

	// Diese beiden Felder werden nicht immer eingegeben, deswegen Boolean und nicht boolean, damit sie auch null sein
	// duerfen
	@Nullable
	@Column(nullable = true)
	private Boolean sozialhilfeBezueger;

	@Nullable
	@Column(nullable = true)
	private Boolean partnerIdentischMitVorgesuch;

	@Nullable
	@Size(max = DB_DEFAULT_MAX_LENGTH)
	private String zustaendigeAmtsstelle;

	@Nullable
	@Size(max = DB_DEFAULT_MAX_LENGTH)
	private String nameBetreuer;

	@Nullable
	@Column(nullable = true)
	private Boolean verguenstigungGewuenscht;

	@Nullable
	@Column(nullable = true)
	private LocalDate aenderungPer;

	@Nullable
	@Column(nullable = true)
	private LocalDate startKonkubinat;

	@Column(nullable = false)
	private boolean keineMahlzeitenverguenstigungBeantragt;

	@Column(nullable = false)
	private boolean keineMahlzeitenverguenstigungBeantragtEditable = true;

	@Nullable
	@Valid
	@OneToOne(optional = true, cascade = CascadeType.ALL, orphanRemoval = true)
	@JoinColumn(foreignKey = @ForeignKey(name = "FK_familiensituation_auszahlungsdaten_id"), nullable = true)
	private Auszahlungsdaten auszahlungsdaten;

	@Column(nullable = false)
	private boolean abweichendeZahlungsadresse;

	@Enumerated(value = EnumType.STRING)
	@Nullable
	@Column(nullable = true)
	private EnumGesuchstellerKardinalitaet gesuchstellerKardinalitaet;

	@Nonnull
	@Column(nullable = false)
	private boolean fkjvFamSit = false;

	@Nonnull
	@Column(nullable = false)
	private Integer minDauerKonkubinat = 5;

	@Nullable
	@Column(nullable = true)
	@Enumerated(EnumType.STRING)
	private UnterhaltsvereinbarungAnswer unterhaltsvereinbarung;

	@Nullable
	@Column(nullable = true)
	@Size(max = DB_TEXTAREA_LENGTH)
	private String unterhaltsvereinbarungBemerkung;

	@Nullable
	@Column(nullable = true)
	private Boolean geteilteObhut;

	@Nullable
	@Column(nullable = true)
	private Boolean gemeinsamerHaushaltMitObhutsberechtigterPerson;

	@Nullable
	@Column(nullable = true)
	private Boolean gemeinsamerHaushaltMitPartner;


	public Familiensituation() {
	}

	public Familiensituation(Familiensituation that) {
		if (that != null) {
			this.familienstatus = that.getFamilienstatus();
			this.gemeinsameSteuererklaerung = that.getGemeinsameSteuererklaerung();
			this.aenderungPer = that.getAenderungPer();
			this.startKonkubinat = that.getStartKonkubinat();
			this.sozialhilfeBezueger = that.getSozialhilfeBezueger();
			this.verguenstigungGewuenscht = that.getVerguenstigungGewuenscht();
			this.partnerIdentischMitVorgesuch = that.getPartnerIdentischMitVorgesuch();
		}
	}

	@Nonnull
	public EnumFamilienstatus getFamilienstatus() {
		return familienstatus;
	}

	public void setFamilienstatus(@Nonnull EnumFamilienstatus familienstatus) {
		this.familienstatus = familienstatus;
	}

	@Nullable
	public Boolean getGemeinsameSteuererklaerung() {
		return gemeinsameSteuererklaerung;
	}

	public void setGemeinsameSteuererklaerung(@Nullable Boolean gemeinsameSteuererklaerung) {
		this.gemeinsameSteuererklaerung = gemeinsameSteuererklaerung;
	}

	@Nullable
	public LocalDate getAenderungPer() {
		return aenderungPer;
	}

	public void setAenderungPer(@Nullable LocalDate aenderungPer) {
		this.aenderungPer = aenderungPer;
	}

	@Nullable
	public LocalDate getStartKonkubinat() {
		return startKonkubinat;
	}

	public void setStartKonkubinat(@Nullable LocalDate startKonkubinat) {
		this.startKonkubinat = startKonkubinat;
	}

	@Nullable
	public Boolean getSozialhilfeBezueger() {
		return sozialhilfeBezueger;
	}

	public void setSozialhilfeBezueger(@Nullable Boolean sozialhilfeBezueger) {
		this.sozialhilfeBezueger = sozialhilfeBezueger;
	}

	@Nullable
	public Boolean getPartnerIdentischMitVorgesuch() {
		return partnerIdentischMitVorgesuch;
	}

	public void setPartnerIdentischMitVorgesuch(@Nullable Boolean partnerIdentischMitVorgesuch){
		this.partnerIdentischMitVorgesuch = partnerIdentischMitVorgesuch;
	}

	@Nullable
	public String getZustaendigeAmtsstelle() {
		return zustaendigeAmtsstelle;
	}

	public void setZustaendigeAmtsstelle(@Nullable String zustaendigeAmtsstelle) {
		this.zustaendigeAmtsstelle = zustaendigeAmtsstelle;
	}

	@Nullable
	public String getNameBetreuer() {
		return nameBetreuer;
	}

	public void setNameBetreuer(@Nullable String nameBetreuer) {
		this.nameBetreuer = nameBetreuer;
	}

	@Nullable
	public Boolean getVerguenstigungGewuenscht() {
		return verguenstigungGewuenscht;
	}

	public void setVerguenstigungGewuenscht(@Nullable Boolean verguenstigungGewuenscht) {
		this.verguenstigungGewuenscht = verguenstigungGewuenscht;
	}

	public boolean isKeineMahlzeitenverguenstigungBeantragt() {
		return keineMahlzeitenverguenstigungBeantragt;
	}

	public void setKeineMahlzeitenverguenstigungBeantragt(boolean keineMahlzeitenverguenstigungBeantragt) {
		this.keineMahlzeitenverguenstigungBeantragt = keineMahlzeitenverguenstigungBeantragt;
	}

	public boolean isKeineMahlzeitenverguenstigungBeantragtEditable() {
		return keineMahlzeitenverguenstigungBeantragtEditable;
	}

	public void setKeineMahlzeitenverguenstigungBeantragtEditable(boolean keineMahlzeitenverguenstigungBeantragtEditable) {
		this.keineMahlzeitenverguenstigungBeantragtEditable = keineMahlzeitenverguenstigungBeantragtEditable;
	}

	@Nullable
	public Auszahlungsdaten getAuszahlungsdaten() {
		return auszahlungsdaten;
	}

	public void setAuszahlungsdaten(@Nullable Auszahlungsdaten auszahlungsdaten) {
		this.auszahlungsdaten = auszahlungsdaten;
	}

	public boolean isAbweichendeZahlungsadresse() {
		return abweichendeZahlungsadresse;
	}

	public void setAbweichendeZahlungsadresse(boolean abweichendeZahlungsadresse) {
		this.abweichendeZahlungsadresse = abweichendeZahlungsadresse;
	}

	@Nullable
	public EnumGesuchstellerKardinalitaet getGesuchstellerKardinalitaet() {
		return gesuchstellerKardinalitaet;
	}

	public void setGesuchstellerKardinalitaet(@Nullable EnumGesuchstellerKardinalitaet gesuchstellerKardinalitaet) {
		this.gesuchstellerKardinalitaet = gesuchstellerKardinalitaet;
	}

	public boolean isFkjvFamSit() {
		return fkjvFamSit;
	}

	public void setFkjvFamSit(boolean fkjvFamSit) {
		this.fkjvFamSit = fkjvFamSit;
	}
	@Nonnull
	public LocalDate getStartKonkubinatPlusMindauer( @Nonnull LocalDate startKonkubinat){
		return startKonkubinat
				.plusYears(this.minDauerKonkubinat);
	}

	@Nonnull
	public LocalDate getStartKonkubinatPlusMindauerEndOfMonth( @Nonnull LocalDate startKonkubinat){
		LocalDate startKonkubinatPlusMindauer = getStartKonkubinatPlusMindauer(startKonkubinat);
		return startKonkubinatPlusMindauer.with(TemporalAdjusters.lastDayOfMonth());
	}

	@Nonnull
	public Integer getMinDauerKonkubinat() {
		return minDauerKonkubinat;
	}

	public void setMinDauerKonkubinat(@Nonnull Integer minDauerKonkubinat) {
		this.minDauerKonkubinat = minDauerKonkubinat;
	}

	public void setUnterhaltsvereinbarung(@Nullable UnterhaltsvereinbarungAnswer unterhaltsvereinbarung) {
		this.unterhaltsvereinbarung = unterhaltsvereinbarung;
	}

	@Nullable
	public String getUnterhaltsvereinbarungBemerkung() {
		return unterhaltsvereinbarungBemerkung;
	}

	public void setUnterhaltsvereinbarungBemerkung(@Nullable String unterhaltsvereinbarungBemerkung) {
		this.unterhaltsvereinbarungBemerkung = unterhaltsvereinbarungBemerkung;
	}

	@Nullable
	public Boolean getGeteilteObhut() {
		return geteilteObhut;
	}

	public void setGeteilteObhut(@Nullable Boolean geteilteObhut) {
		this.geteilteObhut = geteilteObhut;
	}

	@Nullable
	public UnterhaltsvereinbarungAnswer getUnterhaltsvereinbarung() {
		return unterhaltsvereinbarung;
	}

	@Nullable
	public Boolean getGemeinsamerHaushaltMitObhutsberechtigterPerson() {
		return gemeinsamerHaushaltMitObhutsberechtigterPerson;
	}

	public void setGemeinsamerHaushaltMitObhutsberechtigterPerson(
		@Nullable Boolean gemeinsamerHaushaltMitObhutsBerechtigterPerson) {
		this.gemeinsamerHaushaltMitObhutsberechtigterPerson = gemeinsamerHaushaltMitObhutsBerechtigterPerson;
	}

	@Nullable
	public Boolean getGemeinsamerHaushaltMitPartner() {
		return gemeinsamerHaushaltMitPartner;
	}

	public void setGemeinsamerHaushaltMitPartner(@Nullable Boolean gemeinsamerHaushaltMitPartner) {
		this.gemeinsamerHaushaltMitPartner = gemeinsamerHaushaltMitPartner;
	}

	@Transient
	public boolean hasSecondGesuchsteller(LocalDate referenzdatum) {
		if (this.familienstatus != null) {
			switch (this.familienstatus) {
			case APPENZELL:
				return this.hasSecondGesuchstellerAppenzell();
			case ALLEINERZIEHEND:
				if (!this.isFkjvFamSit()) {
					return false;
				}
				return this.hasSecondGesuchstellerFKJV();
			case PFLEGEFAMILIE:
				return this.gesuchstellerKardinalitaet != null && this.gesuchstellerKardinalitaet.equals(
					EnumGesuchstellerKardinalitaet.ZU_ZWEIT);
			case VERHEIRATET:
			case KONKUBINAT:
				return true;
			case KONKUBINAT_KEIN_KIND:
				// a konkubinat is considered to be "long" and therefore requires a 2nd Gesuchsteller
				// when it started x years before the given date. Since the rule applies one month after
				// this five years (as it is with all other rules) we need to substract one month too.
				var dateMinusX = referenzdatum
					.minus(this.getMinDauerKonkubinat(), ChronoUnit.YEARS)
					.minus(1, ChronoUnit.MONTHS);
				if (this.startKonkubinat == null ||
					!this.startKonkubinat.isAfter(dateMinusX)) {
					return true;
				};
				if (isFkjvFamSit()) {
					return this.hasSecondGesuchstellerFKJV();
				}
			}
		}
		return false;
	}

	private boolean hasSecondGesuchstellerAppenzell() {
		if (this.geteilteObhut == null ||
			this.gemeinsamerHaushaltMitObhutsberechtigterPerson == null) {
			return false;
		}

		return this.geteilteObhut && this.gemeinsamerHaushaltMitObhutsberechtigterPerson;
	}

	private boolean hasSecondGesuchstellerFKJV() {
		if (this.geteilteObhut != null && this.geteilteObhut) {
			return this.gesuchstellerKardinalitaet == EnumGesuchstellerKardinalitaet.ZU_ZWEIT;
		}

		return this.unterhaltsvereinbarung != null && this.unterhaltsvereinbarung == UnterhaltsvereinbarungAnswer.NEIN_UNTERHALTSVEREINBARUNG;
	}

	@Nonnull
	public Familiensituation copyFamiliensituation(
		@Nonnull Familiensituation target,
		@Nonnull AntragCopyType copyType) {
		super.copyAbstractEntity(target, copyType);
		target.setFamilienstatus(this.getFamilienstatus());
		target.setStartKonkubinat(this.getStartKonkubinat());
		target.setGesuchstellerKardinalitaet(this.getGesuchstellerKardinalitaet());
		target.setFkjvFamSit(this.fkjvFamSit);
		target.setMinDauerKonkubinat(this.minDauerKonkubinat);
		target.setGeteilteObhut(this.getGeteilteObhut());
		target.setUnterhaltsvereinbarung(this.unterhaltsvereinbarung);
		target.setUnterhaltsvereinbarungBemerkung(this.unterhaltsvereinbarungBemerkung);
		target.setGemeinsamerHaushaltMitPartner(this.getGemeinsamerHaushaltMitPartner());
		target.setGemeinsamerHaushaltMitObhutsberechtigterPerson(this.getGemeinsamerHaushaltMitObhutsberechtigterPerson());
		switch (copyType) {
		case MUTATION:
			target.setAenderungPer(this.getAenderungPer());
			target.setVerguenstigungGewuenscht(this.getVerguenstigungGewuenscht());
			target.setGemeinsameSteuererklaerung(this.getGemeinsameSteuererklaerung());
			target.setSozialhilfeBezueger(this.getSozialhilfeBezueger());
			target.setZustaendigeAmtsstelle(this.getZustaendigeAmtsstelle());
			target.setNameBetreuer(this.getNameBetreuer());
			target.setKeineMahlzeitenverguenstigungBeantragt(this.isKeineMahlzeitenverguenstigungBeantragt());
			// Wenn es mal beantragt war, kann es in einer Mutation nicht mehr nicht-beantragt werden
			target.setKeineMahlzeitenverguenstigungBeantragtEditable(this.isKeineMahlzeitenverguenstigungBeantragt());
			if (this.getAuszahlungsdaten() != null) {
				target.setAuszahlungsdaten(this.getAuszahlungsdaten()
					.copyAuszahlungsdaten(new Auszahlungsdaten(), copyType));
			}
			target.setAbweichendeZahlungsadresse(this.isAbweichendeZahlungsadresse());
			target.setVerguenstigungGewuenscht(this.verguenstigungGewuenscht);
			target.setPartnerIdentischMitVorgesuch(this.partnerIdentischMitVorgesuch);
			break;
		case MUTATION_NEUES_DOSSIER:
			target.setVerguenstigungGewuenscht(this.getVerguenstigungGewuenscht());
			target.setGemeinsameSteuererklaerung(this.getGemeinsameSteuererklaerung());
			target.setSozialhilfeBezueger(this.getSozialhilfeBezueger());
			target.setZustaendigeAmtsstelle(this.getZustaendigeAmtsstelle());
			target.setNameBetreuer(this.getNameBetreuer());
			target.setKeineMahlzeitenverguenstigungBeantragt(this.isKeineMahlzeitenverguenstigungBeantragt());
			if (this.getAuszahlungsdaten() != null) {
				target.setAuszahlungsdaten(this.getAuszahlungsdaten()
					.copyAuszahlungsdaten(new Auszahlungsdaten(), copyType));
			}
			target.setAbweichendeZahlungsadresse(this.isAbweichendeZahlungsadresse());
			target.setPartnerIdentischMitVorgesuch(this.partnerIdentischMitVorgesuch);
			break;
		case ERNEUERUNG:
		case ERNEUERUNG_NEUES_DOSSIER:
			if (this.getAuszahlungsdaten() != null) {
				target.setAuszahlungsdaten(this.getAuszahlungsdaten().copyAuszahlungsdaten(new Auszahlungsdaten(), copyType));
			}
			target.setAbweichendeZahlungsadresse(this.isAbweichendeZahlungsadresse());
			break;
		}
		return target;
	}

	@Override
	@SuppressWarnings("PMD.CompareObjectsWithEquals")
	@SuppressFBWarnings("BC_UNCONFIRMED_CAST")
	public boolean isSame(@Nullable AbstractEntity other) {
		//noinspection ObjectEquality
		if (this == other) {
			return true;
		}
		if (other == null || !getClass().equals(other.getClass())) {
			return false;
		}
		final Familiensituation otherFamiliensituation = (Familiensituation) other;
		return Objects.equals(getAenderungPer(), otherFamiliensituation.getAenderungPer()) &&
			getFamilienstatus() == otherFamiliensituation.getFamilienstatus() &&
			EbeguUtil.isSameOrNullBoolean(
				getGemeinsameSteuererklaerung(),
				otherFamiliensituation.getGemeinsameSteuererklaerung()) &&
			Objects.equals(getSozialhilfeBezueger(), otherFamiliensituation.getSozialhilfeBezueger()) &&
			Objects.equals(getVerguenstigungGewuenscht(), otherFamiliensituation.getVerguenstigungGewuenscht()) &&
			Objects.equals(getStartKonkubinat(), otherFamiliensituation.getStartKonkubinat()) &&
			Objects.equals(getGesuchstellerKardinalitaet(), otherFamiliensituation.getGesuchstellerKardinalitaet()) &&
			Objects.equals(getGeteilteObhut(), otherFamiliensituation.getGeteilteObhut()) &&
			Objects.equals(getUnterhaltsvereinbarung(), otherFamiliensituation.getUnterhaltsvereinbarung()) &&
			Objects.equals(getGemeinsamerHaushaltMitPartner(), otherFamiliensituation.getGemeinsamerHaushaltMitPartner()) &&
			Objects.equals(getGemeinsamerHaushaltMitObhutsberechtigterPerson(),
				otherFamiliensituation.getGemeinsamerHaushaltMitObhutsberechtigterPerson());
	}
}
