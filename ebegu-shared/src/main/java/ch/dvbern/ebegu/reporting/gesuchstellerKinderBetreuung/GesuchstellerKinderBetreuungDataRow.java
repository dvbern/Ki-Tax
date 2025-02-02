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

package ch.dvbern.ebegu.reporting.gesuchstellerKinderBetreuung;

import java.math.BigDecimal;
import java.time.LocalDate;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import ch.dvbern.ebegu.enums.betreuung.BetreuungsangebotTyp;
import ch.dvbern.ebegu.enums.EinschulungTyp;
import ch.dvbern.ebegu.enums.EnumFamilienstatus;

public class GesuchstellerKinderBetreuungDataRow {

	@Nullable private String referenzNummer;
	@Nullable private String institution;
	@Nullable private BetreuungsangebotTyp betreuungsTyp;
	@Nullable private String periode;
	@Nullable private String gesuchStatus;

	@Nullable private LocalDate eingangsdatum;
	@Nullable private LocalDate verfuegungsdatum;
	@Nullable private Integer fallId;
	@Nullable private String gemeinde;

	@Nullable private String gs1Name;
	@Nullable private String gs1Vorname;
	@Nullable private String gs1Strasse;
	@Nullable private String gs1Hausnummer;
	@Nullable private String gs1Zusatzzeile;
	@Nullable private String gs1Plz;
	@Nullable private String gs1Ort;
	@Nullable private Boolean gs1Diplomatenstatus;
	@Nonnull private Integer gs1EwpAngestellt = 0;
	@Nonnull private Integer gs1EwpAusbildung = 0;
	@Nonnull private Integer gs1EwpSelbstaendig = 0;
	@Nonnull private Integer gs1EwpRav = 0;
	@Nonnull private Integer gs1EwpGesundhtl = 0;
	@Nonnull private Integer gs1EwpIntegration = 0;
	@Nonnull private Integer gs1EwpFreiwillig = 0;


	@Nullable private String gs2Name;
	@Nullable private String gs2Vorname;
	@Nullable private String gs2Strasse;
	@Nullable private String gs2Hausnummer;
	@Nullable private String gs2Zusatzzeile;
	@Nullable private String gs2Plz;
	@Nullable private String gs2Ort;
	@Nullable private Boolean gs2Diplomatenstatus;
	@Nonnull private Integer gs2EwpAngestellt = 0;
	@Nonnull private Integer gs2EwpAusbildung = 0;
	@Nonnull private Integer gs2EwpSelbstaendig = 0;
	@Nonnull private Integer gs2EwpRav = 0;
	@Nonnull private Integer gs2EwpGesundhtl = 0;
	@Nonnull private Integer gs2EwpIntegration = 0;
	@Nonnull private Integer gs2EwpFreiwillig = 0;

	@Nullable private EnumFamilienstatus familiensituation;
	@Nullable private BigDecimal familiengroesse;

	@Nullable private BigDecimal massgEinkVorFamilienabzug;
	@Nullable private BigDecimal familienabzug;
	@Nullable private BigDecimal massgEink;
	@Nullable private Integer einkommensjahr;
	@Nullable private Boolean ekvVorhandenBasisJahr1;
	@Nullable private Boolean ekvVorhandenBasisJahr2;
	@Nullable private Boolean ekvAnnuliertBasisJahr1;
	@Nullable private Boolean ekvAnnuliertBasisJahr2;
	@Nullable private Boolean stvGeprueft;
	@Nullable private Boolean veranlagt;
	@Nullable private Boolean mzvBeantragt;
	@Nullable private Boolean sozialhilfeBezueger;

	@Nullable private String kindName;
	@Nullable private String kindVorname;
	@Nullable private LocalDate kindGeburtsdatum;
	@Nullable private String kindFachstelle;
	@Nullable private String kindIntegration;
	@Nullable private Boolean kindErwBeduerfnisse;
	@Nullable private Boolean kindSprichtAmtssprache;
	@Nullable private EinschulungTyp kindEinschulungTyp;
	@Nullable private Boolean keinPlatzImSchulhort;

	private LocalDate zeitabschnittVon;
	private LocalDate zeitabschnittBis;
	@Nullable private String betreuungsStatus;
	@Nullable private BigDecimal betreuungsPensum;

	@Nullable private BigDecimal anspruchsPensumKanton;
	@Nullable private BigDecimal anspruchsPensumGemeinde;
	@Nullable private BigDecimal anspruchsPensumTotal;

	@Nullable private BigDecimal bgPensumKanton;
	@Nullable private BigDecimal bgPensumGemeinde;
	@Nullable private BigDecimal bgPensumTotal;

	@Nullable private BigDecimal bgStunden;
	@Nullable private String bgPensumZeiteinheit;

	@Nullable private BigDecimal vollkosten;
	@Nullable private BigDecimal elternbeitrag;
	@Nullable private BigDecimal verguenstigungKanton;
	@Nullable private BigDecimal verguenstigungGemeinde;
	@Nullable private BigDecimal verguenstigungTotal;

	@Nullable private BigDecimal ausserordentlicherAnspruch;

	@Nullable private String iban;
	@Nullable private String kontoinhaber;

	private boolean showBgSensitiveData = true;

	public GesuchstellerKinderBetreuungDataRow() {
	}

	@Nullable
	public String getReferenzNummer() {
		return referenzNummer;
	}

	public void setReferenzNummer(@Nullable String referenzNummer) {
		this.referenzNummer = referenzNummer;
	}

	@Nullable
	public String getInstitution() {
		return institution;
	}

	public void setInstitution(@Nullable String institution) {
		this.institution = institution;
	}

	@Nullable
	public BetreuungsangebotTyp getBetreuungsTyp() {
		return betreuungsTyp;
	}

	public void setBetreuungsTyp(@Nullable BetreuungsangebotTyp betreuungsTyp) {
		this.betreuungsTyp = betreuungsTyp;
	}

	@Nullable
	public String getPeriode() {
		return periode;
	}

	public void setPeriode(@Nullable String periode) {
		this.periode = periode;
	}

	@Nullable
	public String getGesuchStatus() {
		return gesuchStatus;
	}

	public void setGesuchStatus(@Nullable String gesuchStatus) {
		this.gesuchStatus = gesuchStatus;
	}

	@Nullable
	public LocalDate getEingangsdatum() {
		return eingangsdatum;
	}

	public void setEingangsdatum(@Nullable LocalDate eingangsdatum) {
		this.eingangsdatum = eingangsdatum;
	}

	@Nullable
	public LocalDate getVerfuegungsdatum() {
		return verfuegungsdatum;
	}

	public void setVerfuegungsdatum(@Nullable LocalDate verfuegungsdatum) {
		this.verfuegungsdatum = verfuegungsdatum;
	}

	@Nullable
	public String getGemeinde() {
		return gemeinde;
	}

	public void setGemeinde(@Nullable String gemeinde) {
		this.gemeinde = gemeinde;
	}

	@Nullable
	public Integer getFallId() {
		return fallId;
	}

	public void setFallId(@Nullable Integer fallId) {
		this.fallId = fallId;
	}

	@Nullable
	public String getGs1Name() {
		return gs1Name;
	}

	public void setGs1Name(@Nullable String gs1Name) {
		this.gs1Name = gs1Name;
	}

	@Nullable
	public String getGs1Vorname() {
		return gs1Vorname;
	}

	public void setGs1Vorname(@Nullable String gs1Vorname) {
		this.gs1Vorname = gs1Vorname;
	}

	@Nullable
	public String getGs1Strasse() {
		return gs1Strasse;
	}

	public void setGs1Strasse(@Nullable String gs1Strasse) {
		this.gs1Strasse = gs1Strasse;
	}

	@Nullable
	public String getGs1Hausnummer() {
		return gs1Hausnummer;
	}

	public void setGs1Hausnummer(@Nullable String gs1Hausnummer) {
		this.gs1Hausnummer = gs1Hausnummer;
	}

	@Nullable
	public String getGs1Zusatzzeile() {
		return gs1Zusatzzeile;
	}

	public void setGs1Zusatzzeile(@Nullable String gs1Zusatzzeile) {
		this.gs1Zusatzzeile = gs1Zusatzzeile;
	}

	@Nullable
	public String getGs1Plz() {
		return gs1Plz;
	}

	public void setGs1Plz(@Nullable String gs1Plz) {
		this.gs1Plz = gs1Plz;
	}

	@Nullable
	public String getGs1Ort() {
		return gs1Ort;
	}

	public void setGs1Ort(@Nullable String gs1Ort) {
		this.gs1Ort = gs1Ort;
	}

	@Nullable
	public Boolean getGs1Diplomatenstatus() {
		return gs1Diplomatenstatus;
	}

	public void setGs1Diplomatenstatus(@Nullable Boolean gs1Diplomatenstatus) {
		this.gs1Diplomatenstatus = gs1Diplomatenstatus;
	}

	@Nonnull
	public Integer getGs1EwpAngestellt() {
		return gs1EwpAngestellt;
	}

	public void setGs1EwpAngestellt(@Nonnull Integer gs1EwpAngestellt) {
		this.gs1EwpAngestellt = gs1EwpAngestellt;
	}

	@Nonnull
	public Integer getGs1EwpAusbildung() {
		return gs1EwpAusbildung;
	}

	public void setGs1EwpAusbildung(@Nonnull Integer gs1EwpAusbildung) {
		this.gs1EwpAusbildung = gs1EwpAusbildung;
	}

	@Nonnull
	public Integer getGs1EwpSelbstaendig() {
		return gs1EwpSelbstaendig;
	}

	public void setGs1EwpSelbstaendig(@Nonnull Integer gs1EwpSelbstaendig) {
		this.gs1EwpSelbstaendig = gs1EwpSelbstaendig;
	}

	@Nonnull
	public Integer getGs1EwpRav() {
		return gs1EwpRav;
	}

	public void setGs1EwpRav(@Nonnull Integer gs1EwpRav) {
		this.gs1EwpRav = gs1EwpRav;
	}

	@Nonnull
	public Integer getGs1EwpGesundhtl() {
		return gs1EwpGesundhtl;
	}

	public void setGs1EwpGesundhtl(@Nonnull Integer gs1EwpGesundhtl) {
		this.gs1EwpGesundhtl = gs1EwpGesundhtl;
	}

	@Nonnull
	public Integer getGs1EwpFreiwillig() {
		return gs1EwpFreiwillig;
	}

	public void setGs1EwpFreiwillig(@Nonnull Integer gs1EwpFreiwillig) {
		this.gs1EwpFreiwillig = gs1EwpFreiwillig;
	}

	@Nullable
	public String getGs2Name() {
		return gs2Name;
	}

	public void setGs2Name(@Nullable String gs2Name) {
		this.gs2Name = gs2Name;
	}

	@Nullable
	public String getGs2Vorname() {
		return gs2Vorname;
	}

	public void setGs2Vorname(@Nullable String gs2Vorname) {
		this.gs2Vorname = gs2Vorname;
	}

	@Nullable
	public String getGs2Strasse() {
		return gs2Strasse;
	}

	public void setGs2Strasse(@Nullable String gs2Strasse) {
		this.gs2Strasse = gs2Strasse;
	}

	@Nullable
	public String getGs2Hausnummer() {
		return gs2Hausnummer;
	}

	public void setGs2Hausnummer(@Nullable String gs2Hausnummer) {
		this.gs2Hausnummer = gs2Hausnummer;
	}

	@Nullable
	public String getGs2Zusatzzeile() {
		return gs2Zusatzzeile;
	}

	public void setGs2Zusatzzeile(@Nullable String gs2Zusatzzeile) {
		this.gs2Zusatzzeile = gs2Zusatzzeile;
	}

	@Nullable
	public String getGs2Plz() {
		return gs2Plz;
	}

	public void setGs2Plz(@Nullable String gs2Plz) {
		this.gs2Plz = gs2Plz;
	}

	@Nullable
	public String getGs2Ort() {
		return gs2Ort;
	}

	public void setGs2Ort(@Nullable String gs2Ort) {
		this.gs2Ort = gs2Ort;
	}

	@Nullable
	public Boolean getGs2Diplomatenstatus() {
		return gs2Diplomatenstatus;
	}

	public void setGs2Diplomatenstatus(@Nullable Boolean gs2Diplomatenstatus) {
		this.gs2Diplomatenstatus = gs2Diplomatenstatus;
	}

	@Nonnull
	public Integer getGs2EwpAngestellt() {
		return gs2EwpAngestellt;
	}

	public void setGs2EwpAngestellt(@Nonnull Integer gs2EwpAngestellt) {
		this.gs2EwpAngestellt = gs2EwpAngestellt;
	}

	@Nonnull
	public Integer getGs2EwpAusbildung() {
		return gs2EwpAusbildung;
	}

	public void setGs2EwpAusbildung(@Nonnull Integer gs2EwpAusbildung) {
		this.gs2EwpAusbildung = gs2EwpAusbildung;
	}

	@Nonnull
	public Integer getGs2EwpSelbstaendig() {
		return gs2EwpSelbstaendig;
	}

	public void setGs2EwpSelbstaendig(@Nonnull Integer gs2EwpSelbstaendig) {
		this.gs2EwpSelbstaendig = gs2EwpSelbstaendig;
	}

	@Nonnull
	public Integer getGs2EwpRav() {
		return gs2EwpRav;
	}

	public void setGs2EwpRav(@Nonnull Integer gs2EwpRav) {
		this.gs2EwpRav = gs2EwpRav;
	}

	@Nonnull
	public Integer getGs2EwpGesundhtl() {
		return gs2EwpGesundhtl;
	}

	public void setGs2EwpGesundhtl(@Nonnull Integer gs2EwpGesundhtl) {
		this.gs2EwpGesundhtl = gs2EwpGesundhtl;
	}

	@Nullable
	public EnumFamilienstatus getFamiliensituation() {
		return familiensituation;
	}

	public void setFamiliensituation(@Nullable EnumFamilienstatus familiensituation) {
		this.familiensituation = familiensituation;
	}

	@Nullable
	public BigDecimal getFamiliengroesse() {
		return familiengroesse;
	}

	public void setFamiliengroesse(@Nullable BigDecimal familiengroesse) {
		this.familiengroesse = familiengroesse;
	}

	@Nullable
	public BigDecimal getMassgEinkVorFamilienabzug() {
		return massgEinkVorFamilienabzug;
	}

	public void setMassgEinkVorFamilienabzug(@Nullable BigDecimal massgEinkVorFamilienabzug) {
		this.massgEinkVorFamilienabzug = massgEinkVorFamilienabzug;
	}

	@Nullable
	public BigDecimal getFamilienabzug() {
		return familienabzug;
	}

	public void setFamilienabzug(@Nullable BigDecimal familienabzug) {
		this.familienabzug = familienabzug;
	}

	@Nullable
	public BigDecimal getMassgEink() {
		return massgEink;
	}

	public void setMassgEink(@Nullable BigDecimal massgEink) {
		this.massgEink = massgEink;
	}

	@Nullable
	public Integer getEinkommensjahr() {
		return einkommensjahr;
	}

	public void setEinkommensjahr(@Nullable Integer einkommensjahr) {
		this.einkommensjahr = einkommensjahr;
	}

	@Nullable
	public Boolean getStvGeprueft() {
		return stvGeprueft;
	}

	public void setStvGeprueft(@Nullable Boolean stvGeprueft) {
		this.stvGeprueft = stvGeprueft;
	}

	@Nullable
	public Boolean getVeranlagt() {
		return veranlagt;
	}

	public void setVeranlagt(@Nullable Boolean veranlagt) {
		this.veranlagt = veranlagt;
	}

	@Nullable
	public Boolean isSozialhilfeBezueger() {
		return sozialhilfeBezueger;
	}

	public void setSozialhilfeBezueger(@Nullable Boolean sozialhilfeBezueger) {
		this.sozialhilfeBezueger = sozialhilfeBezueger;
	}

	@Nullable
	public String getKindName() {
		return kindName;
	}

	public void setKindName(@Nullable String kindName) {
		this.kindName = kindName;
	}

	@Nullable
	public String getKindVorname() {
		return kindVorname;
	}

	public void setKindVorname(@Nullable String kindVorname) {
		this.kindVorname = kindVorname;
	}

	@Nullable
	public LocalDate getKindGeburtsdatum() {
		return kindGeburtsdatum;
	}

	public void setKindGeburtsdatum(@Nullable LocalDate kindGeburtsdatum) {
		this.kindGeburtsdatum = kindGeburtsdatum;
	}

	@Nullable
	public String getKindFachstelle() {
		return kindFachstelle;
	}

	public void setKindFachstelle(@Nullable String kindFachstelle) {
		this.kindFachstelle = kindFachstelle;
	}

	@Nullable
	public String getKindIntegration() {
		return kindIntegration;
	}

	public void setKindIntegration(@Nullable String kindIntegration) {
		this.kindIntegration = kindIntegration;
	}

	@Nullable
	public Boolean getKindErwBeduerfnisse() {
		return kindErwBeduerfnisse;
	}

	public void setKindErwBeduerfnisse(@Nullable Boolean kindErwBeduerfnisse) {
		this.kindErwBeduerfnisse = kindErwBeduerfnisse;
	}

	@Nullable
	public Boolean getKindSprichtAmtssprache() {
		return kindSprichtAmtssprache;
	}

	public void setKindSprichtAmtssprache(@Nullable Boolean kindSprichtAmtssprache) {
		this.kindSprichtAmtssprache = kindSprichtAmtssprache;
	}

	@Nullable
	public EinschulungTyp getKindEinschulungTyp() { return kindEinschulungTyp; }

	public void setKindEinschulungTyp(@Nullable EinschulungTyp kindEinschulungTyp) { this.kindEinschulungTyp = kindEinschulungTyp; }

	@Nullable
	public Boolean getKeinPlatzImSchulhort() {
		return keinPlatzImSchulhort;
	}

	public void setKeinPlatzImSchulhort(@Nullable Boolean keinPlatzImSchulhort) {
		this.keinPlatzImSchulhort = keinPlatzImSchulhort;
	}

	public LocalDate getZeitabschnittVon() {
		return zeitabschnittVon;
	}

	public void setZeitabschnittVon(LocalDate zeitabschnittVon) {
		this.zeitabschnittVon = zeitabschnittVon;
	}

	public LocalDate getZeitabschnittBis() {
		return zeitabschnittBis;
	}

	public void setZeitabschnittBis(LocalDate zeitabschnittBis) {
		this.zeitabschnittBis = zeitabschnittBis;
	}

	@Nullable
	public String getBetreuungsStatus() {
		return betreuungsStatus;
	}

	public void setBetreuungsStatus(@Nullable String betreuungsStatus) {
		this.betreuungsStatus = betreuungsStatus;
	}

	@Nullable
	public BigDecimal getBetreuungspensum() {
		return betreuungsPensum;
	}

	public void setBetreuungspensum(@Nullable BigDecimal betreuungsPensum) {
		this.betreuungsPensum = betreuungsPensum;
	}

	@Nullable
	public BigDecimal getAnspruchsPensumKanton() {
		return anspruchsPensumKanton;
	}

	public void setAnspruchsPensumKanton(@Nullable BigDecimal anspruchsPensumKanton) {
		this.anspruchsPensumKanton = anspruchsPensumKanton;
	}

	@Nullable
	public BigDecimal getAnspruchsPensumGemeinde() {
		return anspruchsPensumGemeinde;
	}

	public void setAnspruchsPensumGemeinde(@Nullable BigDecimal anspruchsPensumGemeinde) {
		this.anspruchsPensumGemeinde = anspruchsPensumGemeinde;
	}

	@Nullable
	public BigDecimal getAnspruchsPensumTotal() {
		return anspruchsPensumTotal;
	}

	public void setAnspruchsPensumTotal(@Nullable BigDecimal anspruchsPensumTotal) {
		this.anspruchsPensumTotal = anspruchsPensumTotal;
	}

	@Nullable
	public BigDecimal getBgPensumKanton() {
		return bgPensumKanton;
	}

	public void setBgPensumKanton(@Nullable BigDecimal bgPensumKanton) {
		this.bgPensumKanton = bgPensumKanton;
	}

	@Nullable
	public BigDecimal getBgPensumGemeinde() {
		return bgPensumGemeinde;
	}

	public void setBgPensumGemeinde(@Nullable BigDecimal bgPensumGemeinde) {
		this.bgPensumGemeinde = bgPensumGemeinde;
	}

	@Nullable
	public BigDecimal getBgPensumTotal() {
		return bgPensumTotal;
	}

	public void setBgPensumTotal(@Nullable BigDecimal bgPensumTotal) {
		this.bgPensumTotal = bgPensumTotal;
	}

	@Nullable
	public BigDecimal getBgStunden() {
		return bgStunden;
	}

	public void setBgStunden(@Nullable BigDecimal bgStunden) {
		this.bgStunden = bgStunden;
	}

	@Nullable
	public String getBgPensumZeiteinheit() {
		return bgPensumZeiteinheit;
	}

	public void setBgPensumZeiteinheit(@Nullable String bgPensumZeiteinheit) {
		this.bgPensumZeiteinheit = bgPensumZeiteinheit;
	}

	@Nullable
	public BigDecimal getVollkosten() {
		return vollkosten;
	}

	public void setVollkosten(@Nullable BigDecimal vollkosten) {
		this.vollkosten = vollkosten;
	}

	@Nullable
	public BigDecimal getElternbeitrag() {
		return elternbeitrag;
	}

	public void setElternbeitrag(@Nullable BigDecimal elternbeitrag) {
		this.elternbeitrag = elternbeitrag;
	}

	@Nullable
	public BigDecimal getVerguenstigungKanton() {
		return verguenstigungKanton;
	}

	public void setVerguenstigungKanton(@Nullable BigDecimal verguenstigungKanton) {
		this.verguenstigungKanton = verguenstigungKanton;
	}

	@Nullable
	public BigDecimal getVerguenstigungGemeinde() {
		return verguenstigungGemeinde;
	}

	public void setVerguenstigungGemeinde(@Nullable BigDecimal verguenstigungGemeinde) {
		this.verguenstigungGemeinde = verguenstigungGemeinde;
	}

	@Nullable
	public BigDecimal getVerguenstigungTotal() {
		return verguenstigungTotal;
	}

	public void setVerguenstigungTotal(@Nullable BigDecimal verguenstigungTotal) {
		this.verguenstigungTotal = verguenstigungTotal;
	}

	@Nullable
	public BigDecimal getAusserordentlicherAnspruch() {
		return ausserordentlicherAnspruch;
	}

	public void setAusserordentlicherAnspruch(@Nullable BigDecimal ausserordentlicherAnspruch) {
		this.ausserordentlicherAnspruch = ausserordentlicherAnspruch;
	}

	@Nonnull
	public Integer getGs1EwpIntegration() {
		return gs1EwpIntegration;
	}

	public void setGs1EwpIntegration(@Nonnull Integer gs1EwpIntegration) {
		this.gs1EwpIntegration = gs1EwpIntegration;
	}

	@Nonnull
	public Integer getGs2EwpIntegration() {
		return gs2EwpIntegration;
	}

	public void setGs2EwpIntegration(@Nonnull Integer gs2EwpIntegration) {
		this.gs2EwpIntegration = gs2EwpIntegration;
	}

	@Nonnull
	public Integer getGs2EwpFreiwillig() {
		return gs2EwpFreiwillig;
	}

	public void setGs2EwpFreiwillig(@Nonnull Integer gs2EwpFreiwillig) {
		this.gs2EwpFreiwillig = gs2EwpFreiwillig;
	}

	@Nullable
	public Boolean getEkvVorhandenBasisJahr1() {
		return ekvVorhandenBasisJahr1;
	}

	public void setEkvVorhandenBasisJahr1(@Nullable Boolean ekvVorhandenBasisJahr1) {
		this.ekvVorhandenBasisJahr1 = ekvVorhandenBasisJahr1;
	}

	@Nullable
	public Boolean getEkvVorhandenBasisJahr2() {
		return ekvVorhandenBasisJahr2;
	}

	public void setEkvVorhandenBasisJahr2(@Nullable Boolean ekvVorhandenBasisJahr2) {
		this.ekvVorhandenBasisJahr2 = ekvVorhandenBasisJahr2;
	}

	@Nullable
	public Boolean getEkvAnnuliertBasisJahr1() {
		return ekvAnnuliertBasisJahr1;
	}

	public void setEkvAnnuliertBasisJahr1(@Nullable Boolean ekvAnnuliertBasisJahr1) {
		this.ekvAnnuliertBasisJahr1 = ekvAnnuliertBasisJahr1;
	}

	@Nullable
	public Boolean getEkvAnnuliertBasisJahr2() {
		return ekvAnnuliertBasisJahr2;
	}

	public void setEkvAnnuliertBasisJahr2(@Nullable Boolean ekvAnnuliertBasisJahr2) {
		this.ekvAnnuliertBasisJahr2 = ekvAnnuliertBasisJahr2;
	}

	@Nullable
	public Boolean getMzvBeantragt() {
		return mzvBeantragt;
	}

	public void setMzvBeantragt(@Nullable Boolean mzvBeantragt) {
		this.mzvBeantragt = mzvBeantragt;
	}

	@Nullable
	public String getIban() {
		return iban;
	}

	public void setIban(@Nullable String iban) {
		this.iban = iban;
	}

	@Nullable
	public String getKontoinhaber() {
		return kontoinhaber;
	}

	public void setKontoinhaber(@Nullable String kontoinhaber) {
		this.kontoinhaber = kontoinhaber;
	}

	public boolean isShowBgSensitiveData() {
		return showBgSensitiveData;
	}

	public void setShowBgSensitiveData(boolean showBgSensitiveData) {
		this.showBgSensitiveData = showBgSensitiveData;
	}
}
