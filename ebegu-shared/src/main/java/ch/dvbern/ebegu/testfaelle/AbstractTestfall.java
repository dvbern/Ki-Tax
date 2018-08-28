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

package ch.dvbern.ebegu.testfaelle;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.ArrayList;
import java.util.Collection;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import ch.dvbern.ebegu.entities.AdresseTyp;
import ch.dvbern.ebegu.entities.Benutzer;
import ch.dvbern.ebegu.entities.Betreuung;
import ch.dvbern.ebegu.entities.Betreuungspensum;
import ch.dvbern.ebegu.entities.BetreuungspensumContainer;
import ch.dvbern.ebegu.entities.Dossier;
import ch.dvbern.ebegu.entities.Einkommensverschlechterung;
import ch.dvbern.ebegu.entities.EinkommensverschlechterungContainer;
import ch.dvbern.ebegu.entities.EinkommensverschlechterungInfo;
import ch.dvbern.ebegu.entities.EinkommensverschlechterungInfoContainer;
import ch.dvbern.ebegu.entities.Erwerbspensum;
import ch.dvbern.ebegu.entities.ErwerbspensumContainer;
import ch.dvbern.ebegu.entities.Fall;
import ch.dvbern.ebegu.entities.Familiensituation;
import ch.dvbern.ebegu.entities.FamiliensituationContainer;
import ch.dvbern.ebegu.entities.FinanzielleSituation;
import ch.dvbern.ebegu.entities.FinanzielleSituationContainer;
import ch.dvbern.ebegu.entities.Gemeinde;
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.entities.Gesuchsperiode;
import ch.dvbern.ebegu.entities.Gesuchsteller;
import ch.dvbern.ebegu.entities.GesuchstellerAdresse;
import ch.dvbern.ebegu.entities.GesuchstellerAdresseContainer;
import ch.dvbern.ebegu.entities.GesuchstellerContainer;
import ch.dvbern.ebegu.entities.InstitutionStammdaten;
import ch.dvbern.ebegu.entities.Kind;
import ch.dvbern.ebegu.entities.KindContainer;
import ch.dvbern.ebegu.enums.AntragStatus;
import ch.dvbern.ebegu.enums.BetreuungsangebotTyp;
import ch.dvbern.ebegu.enums.Betreuungsstatus;
import ch.dvbern.ebegu.enums.EinschulungTyp;
import ch.dvbern.ebegu.enums.EnumFamilienstatus;
import ch.dvbern.ebegu.enums.EnumGesuchstellerKardinalitaet;
import ch.dvbern.ebegu.enums.Geschlecht;
import ch.dvbern.ebegu.enums.Kinderabzug;
import ch.dvbern.ebegu.enums.Land;
import ch.dvbern.ebegu.enums.Taetigkeit;
import ch.dvbern.ebegu.enums.Zuschlagsgrund;
import ch.dvbern.ebegu.types.DateRange;
import ch.dvbern.ebegu.util.Constants;
import org.apache.commons.lang.StringUtils;

/**
 * Superklasse für Testfaelle des JA
 * <p>
 * Um alles mit den Services durchfuehren zu koennen, muss man zuerst den Fall erstellen, dann
 * das Gesuch erstellen und dann das Gesuch ausfuellen und updaten. Nur so werden alle WizardSteps
 * erstellt und es gibt kein Problem mit den Verknuepfungen zwischen Entities
 * Der richtige Prozess findet man in TestfaelleService#createAndSaveGesuch()
 */
public abstract class AbstractTestfall {

	public static final String ID_MANDANT_KANTON_BERN = "e3736eb8-6eef-40ef-9e52-96ab48d8f220";

	public static final String ID_INSTITUTION_WEISSENSTEIN = "ab353df1-47ca-4618-b849-2265cf1c356a";
	public static final String ID_INSTITUTION_STAMMDATEN_WEISSENSTEIN_KITA = "945e3eef-8f43-43d2-a684-4aa61089684b";
	public static final String ID_INSTITUTION_STAMMDATEN_WEISSENSTEIN_TAGI = "3304040a-3eb7-426c-a838-51981df87cec";

	public static final String ID_INSTITUTION_BRUENNEN = "1b6f476f-e0f5-4380-9ef6-836d688853a3";
	public static final String ID_INSTITUTION_STAMMDATEN_BRUENNEN_KITA = "9a0eb656-b6b7-4613-8f55-4e0e4720455e";

	public static final String ID_INSTITUTION_BERN = "f7abc530-5d1d-4f1c-a198-9039232974a0";
	public static final String ID_INSTITUTION_STAMMDATEN_BERN_TAGESSCULHE = "199ac4a1-448f-4d4c-b3a6-5aee21f89613";

	public static final String ID_INSTITUTION_GUARDA = "cb248ea4-df29-496e-ad00-1decb180859e";
	public static final String ID_INSTITUTION_STAMMDATEN_GUARDA_FERIENINSEL = "9d8ff34f-8856-4dd3-ade2-2469aadac0ed";

	protected final Gesuchsperiode gesuchsperiode;
	protected final Collection<InstitutionStammdaten> institutionStammdatenList;

	protected String fixId = null;
	protected Fall fall = null;
	protected Gemeinde gemeinde = null;
	protected Dossier dossier = null;
	protected Gesuch gesuch = null;
	protected final boolean betreuungenBestaetigt;

	public AbstractTestfall(Gesuchsperiode gesuchsperiode, Collection<InstitutionStammdaten> institutionStammdatenList,
		boolean betreuungenBestaetigt) {
		this.gesuchsperiode = gesuchsperiode;
		this.institutionStammdatenList = institutionStammdatenList;
		this.betreuungenBestaetigt = betreuungenBestaetigt;
	}

	public AbstractTestfall(Gesuchsperiode gesuchsperiode, Collection<InstitutionStammdaten> institutionStammdatenList,
		boolean betreuungenBestaetigt, Gemeinde gemeinde) {
		this(gesuchsperiode, institutionStammdatenList, betreuungenBestaetigt);
		this.gemeinde = gemeinde;
	}


	public abstract Gesuch fillInGesuch();

	public abstract String getNachname();

	public abstract String getVorname();

	public Fall createFall(@Nullable Benutzer verantwortlicher) {
		fall = new Fall();
		fall.setTimestampErstellt(LocalDateTime.now().minusDays(7));
		createDossier(fall, verantwortlicher);
		return fall;
	}

	public Fall createFall() {
		fall = new Fall();
		createDossier(fall);
		return fall;
	}

	private Dossier createDossier(@Nonnull Fall fallParam, @Nullable Benutzer verantwortlicher) {
		dossier = createDossier(fallParam);
		dossier.setVerantwortlicherBG(verantwortlicher);
		dossier.setTimestampErstellt(LocalDateTime.now().minusDays(7));
		return dossier;
	}

	private Dossier createDossier(@Nonnull Fall fallParam) {
		dossier = new Dossier();
		dossier.setFall(fallParam);
		if (gemeinde != null) {
			dossier.setGemeinde(gemeinde);
		} else {
			dossier.setGemeinde(createGemeinde());
		}
		return dossier;
	}

	private Gemeinde createGemeinde() {
		Gemeinde testGemeinde = new Gemeinde();
		testGemeinde.setEnabled(true);
		testGemeinde.setName("Testgemeinde");
		return testGemeinde;
	}

	public void createGesuch(@Nullable LocalDate eingangsdatum, AntragStatus status) {
		createGesuch(eingangsdatum);
		gesuch.setStatus(status);
	}

	public void createGesuch(@Nullable LocalDate eingangsdatum) {
		// Fall
		if (fall == null) {
			fall = createFall();
		}
		// Gesuch
		gesuch = new Gesuch();
		if (StringUtils.isNotEmpty(fixId)) {
			gesuch.setId(fixId);
		}
		gesuch.setGesuchsperiode(gesuchsperiode);
		gesuch.setDossier(dossier);
		gesuch.setEingangsdatum(eingangsdatum);
		if (eingangsdatum != null) {
			gesuch.setStatus(AntragStatus.IN_BEARBEITUNG_JA);
		} else {
			gesuch.setStatus(AntragStatus.IN_BEARBEITUNG_GS);
		}
	}

	private void setFinSitFieldsOfFamiliensituation(@Nonnull Familiensituation familiensituation) {
		if (gesuchsperiode.hasTagesschulenAnmeldung()) {
			// by default verguenstigung gewuenscht
			familiensituation.setSozialhilfeBezueger(false);
			familiensituation.setVerguenstigungGewuenscht(true);
		}
	}

	protected Gesuch createAlleinerziehend() {
		// Familiensituation
		Familiensituation familiensituation = new Familiensituation();
		familiensituation.setFamilienstatus(EnumFamilienstatus.ALLEINERZIEHEND);
		familiensituation.setGesuchstellerKardinalitaet(EnumGesuchstellerKardinalitaet.ALLEINE);
		FamiliensituationContainer familiensituationContainer = new FamiliensituationContainer();
		familiensituationContainer.setFamiliensituationJA(familiensituation);
		setFinSitFieldsOfFamiliensituation(familiensituation);
		gesuch.setFamiliensituationContainer(familiensituationContainer);
		return gesuch;
	}

	protected Gesuch createVerheiratet() {
		// Familiensituation
		Familiensituation familiensituation = new Familiensituation();
		familiensituation.setFamilienstatus(EnumFamilienstatus.VERHEIRATET);
		familiensituation.setGesuchstellerKardinalitaet(EnumGesuchstellerKardinalitaet.ZU_ZWEIT);
		familiensituation.setGemeinsameSteuererklaerung(Boolean.TRUE);
		FamiliensituationContainer familiensituationContainer = new FamiliensituationContainer();
		familiensituationContainer.setFamiliensituationJA(familiensituation);
		setFinSitFieldsOfFamiliensituation(familiensituation);
		gesuch.setFamiliensituationContainer(familiensituationContainer);
		return gesuch;
	}

	protected GesuchstellerContainer createGesuchstellerContainer() {
		return createGesuchstellerContainer(getNachname(), getVorname());
	}

	protected GesuchstellerContainer createGesuchstellerContainer(String name, String vorname) {
		GesuchstellerContainer gesuchstellerCont = new GesuchstellerContainer();
		gesuchstellerCont.setAdressen(new ArrayList<>());
		gesuchstellerCont.setGesuchstellerJA(createGesuchsteller(name, vorname));
		gesuchstellerCont.getAdressen().add(createWohnadresseContainer(gesuchstellerCont));
		return gesuchstellerCont;
	}

	protected Gesuchsteller createGesuchsteller(String name, String vorname) {
		Gesuchsteller gesuchsteller = new Gesuchsteller();
		gesuchsteller.setGeschlecht(Geschlecht.WEIBLICH);
		gesuchsteller.setNachname(name);
		gesuchsteller.setVorname(vorname);
		gesuchsteller.setGeburtsdatum(LocalDate.of(1980, Month.MARCH, 25));
		gesuchsteller.setDiplomatenstatus(false);
		gesuchsteller.setMail("test@example.com");
		gesuchsteller.setMobile("079 000 00 00");
		return gesuchsteller;
	}

	protected Gesuchsteller createGesuchsteller() {
		return createGesuchsteller(getNachname(), getVorname());
	}

	protected GesuchstellerAdresseContainer createWohnadresseContainer(GesuchstellerContainer gesuchstellerCont) {
		GesuchstellerAdresseContainer wohnadresseCont = new GesuchstellerAdresseContainer();
		wohnadresseCont.setGesuchstellerContainer(gesuchstellerCont);
		wohnadresseCont.setGesuchstellerAdresseJA(createWohnadresse());
		return wohnadresseCont;
	}

	protected GesuchstellerAdresse createWohnadresse() {
		GesuchstellerAdresse wohnadresse = new GesuchstellerAdresse();
		wohnadresse.setStrasse("Testweg");
		wohnadresse.setHausnummer("10");
		wohnadresse.setPlz("3000");
		wohnadresse.setOrt("Bern");
		wohnadresse.setLand(Land.CH);
		wohnadresse.setAdresseTyp(AdresseTyp.WOHNADRESSE);
		wohnadresse.setGueltigkeit(new DateRange(Constants.START_OF_TIME, Constants.END_OF_TIME));
		return wohnadresse;
	}

	protected ErwerbspensumContainer createErwerbspensum(int prozent, int zuschlagsprozent) {
		ErwerbspensumContainer erwerbspensumContainer = new ErwerbspensumContainer();
		Erwerbspensum erwerbspensum = new Erwerbspensum();
		erwerbspensum.setGueltigkeit(gesuchsperiode.getGueltigkeit());
		erwerbspensum.setTaetigkeit(Taetigkeit.ANGESTELLT);
		erwerbspensum.setPensum(prozent);
		if (zuschlagsprozent > 0) {
			erwerbspensum.setZuschlagZuErwerbspensum(true);
			erwerbspensum.setZuschlagsprozent(zuschlagsprozent);
			erwerbspensum.setZuschlagsgrund(Zuschlagsgrund.LANGER_ARBWEITSWEG);
		}
		erwerbspensumContainer.setErwerbspensumJA(erwerbspensum);
		return erwerbspensumContainer;
	}

	protected KindContainer createKind(Geschlecht geschlecht, String name, String vorname, LocalDate geburtsdatum, Kinderabzug kinderabzug, boolean betreuung) {
		Kind kind = new Kind();
		kind.setGeschlecht(geschlecht);
		kind.setNachname(name);
		kind.setVorname(vorname);
		kind.setGeburtsdatum(geburtsdatum);
		kind.setKinderabzug(kinderabzug);
		kind.setFamilienErgaenzendeBetreuung(betreuung);
		if (betreuung) {
			kind.setMutterspracheDeutsch(Boolean.TRUE);
			kind.setEinschulungTyp(EinschulungTyp.VORSCHULALTER);
		}
		KindContainer kindContainer = new KindContainer();
		kindContainer.setKindJA(kind);
		return kindContainer;
	}

	protected Betreuung createBetreuung(BetreuungsangebotTyp betreuungsangebotTyp, String institutionsId) {
		return createBetreuung(betreuungsangebotTyp, institutionsId, false);
	}

	protected Betreuung createBetreuung(BetreuungsangebotTyp betreuungsangebotTyp, String institutionsId, boolean bestaetigt) {
		Betreuung betreuung = new Betreuung();
		betreuung.setInstitutionStammdaten(createInstitutionStammdaten(betreuungsangebotTyp, institutionsId));
		if (!bestaetigt) {
			betreuung.setBetreuungsstatus(Betreuungsstatus.WARTEN);
		} else {
			betreuung.setBetreuungsstatus(Betreuungsstatus.BESTAETIGT);
			betreuung.setDatumBestaetigung(LocalDate.now());
		}
		betreuung.setVertrag(Boolean.TRUE);
		return betreuung;
	}

	@Nonnull
	protected InstitutionStammdaten createInstitutionStammdaten(BetreuungsangebotTyp betreuungsangebotTyp, String institutionsId) {
		for (InstitutionStammdaten institutionStammdaten : institutionStammdatenList) {
			if (institutionStammdaten.getBetreuungsangebotTyp() == betreuungsangebotTyp && institutionStammdaten.getInstitution().getId().equals(institutionsId)) {
				return institutionStammdaten;
			}
		}
		throw new IllegalStateException("Institutionsstammdaten sind nicht vorhanden: " + institutionsId);
	}

	protected BetreuungspensumContainer createBetreuungspensum(int pensum) {
		BetreuungspensumContainer betreuungspensumContainer = new BetreuungspensumContainer();
		Betreuungspensum betreuungspensum = new Betreuungspensum();
		betreuungspensumContainer.setBetreuungspensumJA(betreuungspensum);
		betreuungspensum.setGueltigkeit(gesuchsperiode.getGueltigkeit());
		betreuungspensum.setPensum(pensum);
		return betreuungspensumContainer;
	}

	protected BetreuungspensumContainer createBetreuungspensum(int pensum, LocalDate datumVon, LocalDate datumBis) {
		BetreuungspensumContainer betreuungspensumContainer = new BetreuungspensumContainer();
		Betreuungspensum betreuungspensum = new Betreuungspensum();
		betreuungspensumContainer.setBetreuungspensumJA(betreuungspensum);
		betreuungspensum.setGueltigkeit(new DateRange(datumVon, datumBis));
		betreuungspensum.setPensum(pensum);
		return betreuungspensumContainer;
	}

	protected FinanzielleSituationContainer createFinanzielleSituationContainer() {
		FinanzielleSituationContainer finanzielleSituationContainer = new FinanzielleSituationContainer();
		FinanzielleSituation finanzielleSituation = new FinanzielleSituation();
		finanzielleSituation.setSteuerveranlagungErhalten(true);
		finanzielleSituation.setSteuererklaerungAusgefuellt(true);
		finanzielleSituationContainer.setJahr(gesuchsperiode.getGueltigkeit().getGueltigAb().getYear() - 1);
		finanzielleSituationContainer.setFinanzielleSituationJA(finanzielleSituation);
		return finanzielleSituationContainer;
	}

	protected EinkommensverschlechterungContainer createEinkommensverschlechterungContainer(Gesuch gesuch, LocalDate stichtagEKV1, LocalDate stichtagEKV2) {
		EinkommensverschlechterungContainer ekvContainer = createEinkommensverschlechterungContainer(stichtagEKV1 != null, stichtagEKV2 != null);
		EinkommensverschlechterungInfoContainer infoContainer = createEinkommensverschlechterungInfoContainer(stichtagEKV1, stichtagEKV2);
		gesuch.setEinkommensverschlechterungInfoContainer(infoContainer);
		infoContainer.setGesuch(gesuch);
		return ekvContainer;
	}

	@Nonnull
	protected EinkommensverschlechterungContainer createEinkommensverschlechterungContainer(boolean erstesJahr, boolean zweitesJahr) {
		EinkommensverschlechterungContainer ekvContainer = new EinkommensverschlechterungContainer();
		if (erstesJahr) {
			Einkommensverschlechterung ekv1 = new Einkommensverschlechterung();
			ekv1.setSteuerveranlagungErhalten(true);
			ekv1.setSteuererklaerungAusgefuellt(true);
			ekvContainer.setEkvJABasisJahrPlus1(ekv1);
		}
		if (zweitesJahr) {
			Einkommensverschlechterung ekv2 = new Einkommensverschlechterung();
			ekv2.setSteuerveranlagungErhalten(true);
			ekv2.setSteuererklaerungAusgefuellt(true);
			ekvContainer.setEkvJABasisJahrPlus2(ekv2);
		}
		return ekvContainer;
	}

	protected void createEmptyEKVInfoContainer(@Nonnull Gesuch gesuch) {
		EinkommensverschlechterungInfoContainer ekvInfoContainer = new EinkommensverschlechterungInfoContainer();
		ekvInfoContainer.setGesuch(gesuch);
		gesuch.setEinkommensverschlechterungInfoContainer(ekvInfoContainer);
		EinkommensverschlechterungInfo ekvInfoJA = new EinkommensverschlechterungInfo();
		ekvInfoJA.setEinkommensverschlechterung(false);
		ekvInfoJA.setEkvFuerBasisJahrPlus1(false);
		ekvInfoJA.setEkvFuerBasisJahrPlus2(false);
		ekvInfoContainer.setEinkommensverschlechterungInfoJA(ekvInfoJA);
	}

	protected EinkommensverschlechterungInfoContainer createEinkommensverschlechterungInfoContainer(
			@Nullable LocalDate stichtagEKV1, @Nullable LocalDate stichtagEKV2) {
		EinkommensverschlechterungInfoContainer infoContainer = new EinkommensverschlechterungInfoContainer();
		EinkommensverschlechterungInfo info = new EinkommensverschlechterungInfo();
		info.setEkvFuerBasisJahrPlus1(stichtagEKV1 != null);
		if (info.getEkvFuerBasisJahrPlus1()) {
			info.setStichtagFuerBasisJahrPlus1(stichtagEKV1);
			info.setGrundFuerBasisJahrPlus1("Test");

		}
		info.setEkvFuerBasisJahrPlus2(stichtagEKV2 != null);
		if (info.getEkvFuerBasisJahrPlus2()) {
			info.setStichtagFuerBasisJahrPlus2(stichtagEKV2);
			info.setGrundFuerBasisJahrPlus2("Test");

		}
		info.setEinkommensverschlechterung(info.getEkvFuerBasisJahrPlus1() || info.getEkvFuerBasisJahrPlus2());
		infoContainer.setEinkommensverschlechterungInfoJA(info);
		return infoContainer;
	}

	public Fall getFall() {
		return fall;
	}

	public void setFall(Fall fall) {
		this.fall = fall;
	}

	public Dossier getDossier() {
		return dossier;
	}

	public void setDossier(Dossier dossier) {
		this.dossier = dossier;
	}

	public Gesuch getGesuch() {
		return gesuch;
	}

	public String getFixId() {
		return fixId;
	}

	public void setFixId(String fixId) {
		this.fixId = fixId;
	}
}
