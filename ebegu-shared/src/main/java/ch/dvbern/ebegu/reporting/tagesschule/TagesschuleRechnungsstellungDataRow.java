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

package ch.dvbern.ebegu.reporting.tagesschule;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import ch.dvbern.ebegu.entities.AnmeldungTagesschule;
import ch.dvbern.ebegu.entities.BGCalculationResult;
import ch.dvbern.ebegu.entities.Familiensituation;
import ch.dvbern.ebegu.entities.FamiliensituationContainer;
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.entities.Gesuchsteller;
import ch.dvbern.ebegu.entities.GesuchstellerAdresse;
import ch.dvbern.ebegu.entities.GesuchstellerContainer;
import ch.dvbern.ebegu.entities.Kind;
import ch.dvbern.ebegu.entities.KindContainer;
import ch.dvbern.ebegu.entities.TSCalculationResult;
import ch.dvbern.ebegu.entities.VerfuegungZeitabschnitt;
import ch.dvbern.ebegu.enums.FinSitStatus;
import ch.dvbern.ebegu.enums.reporting.ErklaerungEinkommen;
import ch.dvbern.ebegu.util.MathUtil;
import org.apache.commons.lang.builder.CompareToBuilder;

/**
 * DTO fuer die TagesschuleStatistik
 */
public class TagesschuleRechnungsstellungDataRow implements Comparable<TagesschuleRechnungsstellungDataRow> {

	@Nullable
	private String tagesschule;
	@Nullable
	private String nachnameKind;
	@Nullable
	private String vornameKind;
	@Nullable
	private LocalDate geburtsdatumKind;
	@Nullable
	private String referenznummer;

	@Nullable
	private String rechnungsadresseVorname;
	@Nullable
	private String rechnungsadresseNachname;
	@Nullable
	private String rechnungsadresseOrganisation;
	@Nullable
	private String rechnungsadresseStrasse;
	@Nullable
	private String rechnungsadresseHausnummer;
	@Nullable
	private String rechnungsadressePlz;
	@Nullable
	private String rechnungsadresseOrt;

	@Nullable
	private LocalDate datumAb;
	@Nullable
	private BigDecimal massgebendesEinkommenVorFamAbzug;
	@Nullable
	private BigDecimal famGroesse;
	@Nullable
	private BigDecimal massgebendesEinkommenNachFamAbzug;
	@Nullable
	private Boolean ekvVorhanden;
	@Nullable
	private Boolean ekvAnnuliert;
	@Nullable
	private ErklaerungEinkommen erklaerungEinkommen;
	@Nullable
	private LocalDate eintrittsdatum;
	@Nullable
	private BigDecimal gebuehrProStundeMitBetreuung;
	@Nullable
	private BigDecimal gebuehrProStundeOhneBetreuung;

	@Nullable
	public String getTagesschule() {
		return tagesschule;
	}

	public void setTagesschule(@Nullable String tagesschule) {
		this.tagesschule = tagesschule;
	}

	@Nullable
	public String getNachnameKind() {
		return nachnameKind;
	}

	public void setNachnameKind(@Nullable String nachnameKind) {
		this.nachnameKind = nachnameKind;
	}

	@Nullable
	public String getVornameKind() {
		return vornameKind;
	}

	public void setVornameKind(@Nullable String vornameKind) {
		this.vornameKind = vornameKind;
	}

	@Nullable
	public LocalDate getGeburtsdatumKind() {
		return geburtsdatumKind;
	}

	public void setGeburtsdatumKind(@Nullable LocalDate geburtsdatumKind) {
		this.geburtsdatumKind = geburtsdatumKind;
	}

	@Nullable
	public String getReferenznummer() {
		return referenznummer;
	}

	public void setReferenznummer(@Nullable String referenznummer) {
		this.referenznummer = referenznummer;
	}

	@Nullable
	public String getRechnungsadresseVorname() {
		return rechnungsadresseVorname;
	}

	public void setRechnungsadresseVorname(@Nullable String rechnungsadresseVorname) {
		this.rechnungsadresseVorname = rechnungsadresseVorname;
	}

	@Nullable
	public String getRechnungsadresseNachname() {
		return rechnungsadresseNachname;
	}

	public void setRechnungsadresseNachname(@Nullable String rechnungsadresseNachname) {
		this.rechnungsadresseNachname = rechnungsadresseNachname;
	}

	@Nullable
	public String getRechnungsadresseStrasse() {
		return rechnungsadresseStrasse;
	}

	public void setRechnungsadresseStrasse(@Nullable String rechnungsadresseStrasse) {
		this.rechnungsadresseStrasse = rechnungsadresseStrasse;
	}

	@Nullable
	public String getRechnungsadresseHausnummer() {
		return rechnungsadresseHausnummer;
	}

	public void setRechnungsadresseHausnummer(@Nullable String rechnungsadresseHausnummer) {
		this.rechnungsadresseHausnummer = rechnungsadresseHausnummer;
	}

	@Nullable
	public String getRechnungsadressePlz() {
		return rechnungsadressePlz;
	}

	public void setRechnungsadressePlz(@Nullable String rechnungsadressePlz) {
		this.rechnungsadressePlz = rechnungsadressePlz;
	}

	@Nullable
	public String getRechnungsadresseOrt() {
		return rechnungsadresseOrt;
	}

	public void setRechnungsadresseOrt(@Nullable String rechnungsadresseOrt) {
		this.rechnungsadresseOrt = rechnungsadresseOrt;
	}

	@Nullable
	public LocalDate getDatumAb() {
		return datumAb;
	}

	public void setDatumAb(@Nullable LocalDate datumAb) {
		this.datumAb = datumAb;
	}

	@Nullable
	public BigDecimal getMassgebendesEinkommenVorFamAbzug() {
		return massgebendesEinkommenVorFamAbzug;
	}

	public void setMassgebendesEinkommenVorFamAbzug(@Nullable BigDecimal massgebendesEinkommenVorFamAbzug) {
		this.massgebendesEinkommenVorFamAbzug = massgebendesEinkommenVorFamAbzug;
	}

	@Nullable
	public BigDecimal getFamGroesse() {
		return famGroesse;
	}

	public void setFamGroesse(@Nullable BigDecimal famGroesse) {
		this.famGroesse = famGroesse;
	}

	@Nullable
	public BigDecimal getMassgebendesEinkommenNachFamAbzug() {
		return massgebendesEinkommenNachFamAbzug;
	}

	public void setMassgebendesEinkommenNachFamAbzug(@Nullable BigDecimal massgebendesEinkommenNachFamAbzug) {
		this.massgebendesEinkommenNachFamAbzug = massgebendesEinkommenNachFamAbzug;
	}

	@Nullable
	public ErklaerungEinkommen getErklaerungEinkommen() {
		return erklaerungEinkommen;
	}

	public void setErklaerungEinkommen(@Nullable ErklaerungEinkommen erklaerungEinkommen) {
		this.erklaerungEinkommen = erklaerungEinkommen;
	}

	@Nullable
	public LocalDate getEintrittsdatum() {
		return eintrittsdatum;
	}

	public void setEintrittsdatum(@Nullable LocalDate eintrittsdatum) {
		this.eintrittsdatum = eintrittsdatum;
	}

	@Nullable
	public BigDecimal getGebuehrProStundeMitBetreuung() {
		return gebuehrProStundeMitBetreuung;
	}

	public void setGebuehrProStundeMitBetreuung(@Nullable BigDecimal gebuehrProStundeMitBetreuung) {
		this.gebuehrProStundeMitBetreuung = gebuehrProStundeMitBetreuung;
	}

	@Nullable
	public BigDecimal getGebuehrProStundeOhneBetreuung() {
		return gebuehrProStundeOhneBetreuung;
	}

	public void setGebuehrProStundeOhneBetreuung(@Nullable BigDecimal gebuehrProStundeOhneBetreuung) {
		this.gebuehrProStundeOhneBetreuung = gebuehrProStundeOhneBetreuung;
	}

	@Nullable
	public Boolean getEkvVorhanden() {
		return ekvVorhanden;
	}

	public void setEkvVorhanden(@Nullable Boolean ekvVorhanden) {
		this.ekvVorhanden = ekvVorhanden;
	}

	@Nullable
	public Boolean getEkvAnnuliert() {
		return ekvAnnuliert;
	}

	public void setEkvAnnuliert(@Nullable Boolean ekvAnnuliert) {
		this.ekvAnnuliert = ekvAnnuliert;
	}

	@Nullable
	public String getRechnungsadresseOrganisation() {
		return rechnungsadresseOrganisation;
	}

	public void setRechnungsadresseOrganisation(@Nullable String rechnungsadresseOrganisation) {
		this.rechnungsadresseOrganisation = rechnungsadresseOrganisation;
	}

	@Nonnull
	public static Collection<TagesschuleRechnungsstellungDataRow> createRows(
		@Nonnull VerfuegungZeitabschnitt zeitabschnitt,
		@Nonnull LocalDate stichtag
	) {
		// Der Zeitabschnitt einer Tagesschule-Verfuegung enthaelt mehrere Monate!
		Collection<TagesschuleRechnungsstellungDataRow> dataRows = new ArrayList<>();
		LocalDate monatsStart = zeitabschnitt.getGueltigkeit().getGueltigAb();
		LocalDate monatsEnd = zeitabschnitt.getGueltigkeit().getGueltigBis();
		while (!monatsStart.isAfter(stichtag) && monatsStart.isBefore(monatsEnd)) {
			dataRows.add(createRow(zeitabschnitt, monatsStart));
			monatsStart = monatsStart.plusMonths(1);
		}
		return dataRows;
	}

	@Nonnull
	private static TagesschuleRechnungsstellungDataRow createRow(
		@Nonnull VerfuegungZeitabschnitt zeitabschnitt,
		@Nonnull LocalDate monatsStart
	) {
		TagesschuleRechnungsstellungDataRow dataRow = new TagesschuleRechnungsstellungDataRow();
		final AnmeldungTagesschule anmeldungTagesschule = zeitabschnitt.getVerfuegung().getAnmeldungTagesschule();
		if (anmeldungTagesschule != null) {
			dataRow.tagesschule = anmeldungTagesschule.getInstitutionStammdaten().getInstitution().getName();
			dataRow.referenznummer = anmeldungTagesschule.getBGNummer();
			final KindContainer kindContainer = anmeldungTagesschule.getKind();
			final Kind kind = kindContainer.getKindJA();
			if (kind != null) {
				dataRow.nachnameKind = kind.getNachname();
				dataRow.vornameKind = kind.getVorname();
				dataRow.geburtsdatumKind = kind.getGeburtsdatum();
			}
			final GesuchstellerContainer gsContainer = kindContainer.getGesuch().getGesuchsteller1();
			if (gsContainer != null) {
				final Gesuchsteller gesuchsteller = gsContainer.getGesuchstellerJA();
				if (gesuchsteller != null) {
					dataRow.rechnungsadresseVorname = gesuchsteller.getVorname();
					dataRow.rechnungsadresseNachname = gesuchsteller.getNachname();
				}
				final GesuchstellerAdresse adresse = gsContainer.extractEffectiveRechnungsAdresse(LocalDate.now());
				if (adresse != null) {
					dataRow.rechnungsadresseOrganisation = adresse.getOrganisation();
					dataRow.rechnungsadresseStrasse = adresse.getStrasse();
					dataRow.rechnungsadresseHausnummer = adresse.getHausnummer();
					dataRow.rechnungsadressePlz = adresse.getPlz();
					dataRow.rechnungsadresseOrt = adresse.getOrt();
				}
			}
			if (anmeldungTagesschule.getBelegungTagesschule() != null) {
				dataRow.eintrittsdatum = anmeldungTagesschule.getBelegungTagesschule().getEintrittsdatum();
			}
		}
		dataRow.datumAb = monatsStart;
		final BGCalculationResult bgCalculationResult = zeitabschnitt.getRelevantBgCalculationResult();
		dataRow.massgebendesEinkommenVorFamAbzug = bgCalculationResult.getMassgebendesEinkommenVorAbzugFamgr();
		dataRow.famGroesse = bgCalculationResult.getFamGroesse();

		dataRow.massgebendesEinkommenNachFamAbzug =
			MathUtil.minimum(bgCalculationResult.getMassgebendesEinkommen(), BigDecimal.ZERO);
		dataRow.ekvVorhanden = getEkvVorhandenValue(anmeldungTagesschule, monatsStart);
		dataRow.ekvAnnuliert = getEkvAnnuliertValue(anmeldungTagesschule, monatsStart);
		dataRow.erklaerungEinkommen = getErklaerungEinkommen(anmeldungTagesschule);

		final TSCalculationResult tsMitBetreuung =
			bgCalculationResult.getTsCalculationResultMitPaedagogischerBetreuung();
		if (tsMitBetreuung != null) {
			dataRow.gebuehrProStundeMitBetreuung = tsMitBetreuung.getGebuehrProStunde();
		}
		final TSCalculationResult tsOhneBetreuung =
			bgCalculationResult.getTsCalculationResultOhnePaedagogischerBetreuung();
		if (tsOhneBetreuung != null) {
			dataRow.gebuehrProStundeOhneBetreuung = tsOhneBetreuung.getGebuehrProStunde();
		}
		return dataRow;
	}

	private static Boolean getEkvVorhandenValue(@Nullable AnmeldungTagesschule anmeldungTagesschule, @Nonnull LocalDate monatStart) {
		if (anmeldungTagesschule == null) {
			return false;
		}
		Gesuch gesuch = anmeldungTagesschule.getKind().getGesuch();
		if (gesuch.getEinkommensverschlechterungInfoContainer() == null) {
			return false;
		}
		if (gesuch.getGesuchsperiode().getGueltigkeit().getGueltigAb().getYear() == monatStart.getYear()){
			return gesuch.getEinkommensverschlechterungInfoContainer()
				.getEinkommensverschlechterungInfoJA()
				.getEkvFuerBasisJahrPlus1();
		}
		return gesuch.getEinkommensverschlechterungInfoContainer()
			.getEinkommensverschlechterungInfoJA()
			.getEkvFuerBasisJahrPlus2();

	}

	private static Boolean getEkvAnnuliertValue(@Nullable AnmeldungTagesschule anmeldungTagesschule, @Nonnull LocalDate monatStart) {
		if (anmeldungTagesschule == null) {
			return false;
		}
		Gesuch gesuch = anmeldungTagesschule.getKind().getGesuch();
		if (gesuch.getEinkommensverschlechterungInfoContainer() == null) {
			return false;
		}
		if (gesuch.getGesuchsperiode().getGueltigkeit().getGueltigAb().getYear() == monatStart.getYear()){
			return gesuch.getEinkommensverschlechterungInfoContainer()
				.getEinkommensverschlechterungInfoJA()
				.getEkvBasisJahrPlus1Annulliert();
		}
		return gesuch.getEinkommensverschlechterungInfoContainer()
			.getEinkommensverschlechterungInfoJA()
			.getEkvBasisJahrPlus2Annulliert();

	}

	// massgebendes Einkommen nach Familienabzug kann aus verschiedenen Gründen kleiner oder gleich 0 sein
	// 1) Finanzielle Situation wurde nicht akzeptiert
	// 2) Sozialhilfebezüger
	// 3) Einkommen wurde nicht deklariert
	// 4) Einkommen ist effektiv kleiner als Familienabzug
	private static ErklaerungEinkommen getErklaerungEinkommen(@Nullable AnmeldungTagesschule anmeldungTagesschule) {

		if (anmeldungTagesschule == null) {
			return ErklaerungEinkommen.KEINE_ERKLAERUNG;
		}
		Gesuch gesuch = anmeldungTagesschule.getKind().getGesuch();

		// 1)
		if (gesuch.getFinSitStatus() != null && gesuch.getFinSitStatus() == FinSitStatus.ABGELEHNT) {
			return ErklaerungEinkommen.FINANZIELLE_SITUATION_NICHT_AKZEPTIERT;
		}

		FamiliensituationContainer familiensituationContainer = gesuch.getFamiliensituationContainer();
		if (familiensituationContainer == null) {
			return ErklaerungEinkommen.KEINE_ERKLAERUNG;
		}
		Familiensituation familiensituationJA = familiensituationContainer.getFamiliensituationJA();
		if (familiensituationJA == null) {
			return ErklaerungEinkommen.KEINE_ERKLAERUNG;
		}

		// 2)
		if (familiensituationJA.getSozialhilfeBezueger() != null && familiensituationJA.getSozialhilfeBezueger()) {
			return ErklaerungEinkommen.SOZIALHILFEBEZUEGER;
		}
		// 3)
		if (familiensituationJA.getVerguenstigungGewuenscht() != null
			&& !familiensituationJA.getVerguenstigungGewuenscht()) {
			return ErklaerungEinkommen.KEIN_EINKOMMEN_DEKLARIERT;
		}
		// 4)
		return ErklaerungEinkommen.KEINE_ERKLAERUNG;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof TagesschuleRechnungsstellungDataRow)) {
			return false;
		}

		TagesschuleRechnungsstellungDataRow that = (TagesschuleRechnungsstellungDataRow) o;
		return Objects.equals(getReferenznummer(), that.getReferenznummer())
			&& Objects.equals(getDatumAb(), that.getDatumAb());
	}

	@Override
	public int compareTo(@Nonnull TagesschuleRechnungsstellungDataRow o) {
		if (this.equals(o)) {
			return 0;
		}
		CompareToBuilder builder = new CompareToBuilder();
		builder.append(this.getReferenznummer(), o.getReferenznummer());
		builder.append(this.getDatumAb(), o.getDatumAb());
		return builder.toComparison();
	}

	@Override
	public int hashCode() {
		return Objects.hash(referenznummer, datumAb);
	}
}
