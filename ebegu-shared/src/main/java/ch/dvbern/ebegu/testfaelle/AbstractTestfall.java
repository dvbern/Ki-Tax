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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import ch.dvbern.ebegu.entities.AdresseTyp;
import ch.dvbern.ebegu.entities.AnmeldungTagesschule;
import ch.dvbern.ebegu.entities.BelegungTagesschule;
import ch.dvbern.ebegu.entities.BelegungTagesschuleModul;
import ch.dvbern.ebegu.entities.Benutzer;
import ch.dvbern.ebegu.entities.Betreuung;
import ch.dvbern.ebegu.entities.Betreuungspensum;
import ch.dvbern.ebegu.entities.BetreuungspensumContainer;
import ch.dvbern.ebegu.entities.Dossier;
import ch.dvbern.ebegu.entities.Einkommensverschlechterung;
import ch.dvbern.ebegu.entities.EinkommensverschlechterungContainer;
import ch.dvbern.ebegu.entities.EinkommensverschlechterungInfo;
import ch.dvbern.ebegu.entities.EinkommensverschlechterungInfoContainer;
import ch.dvbern.ebegu.entities.EinstellungenTagesschule;
import ch.dvbern.ebegu.entities.ErweiterteBetreuung;
import ch.dvbern.ebegu.entities.ErweiterteBetreuungContainer;
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
import ch.dvbern.ebegu.entities.ModulTagesschule;
import ch.dvbern.ebegu.entities.ModulTagesschuleGroup;
import ch.dvbern.ebegu.enums.AbholungTagesschule;
import ch.dvbern.ebegu.enums.AntragStatus;
import ch.dvbern.ebegu.enums.BelegungTagesschuleModulIntervall;
import ch.dvbern.ebegu.enums.Betreuungsstatus;
import ch.dvbern.ebegu.enums.EinschulungTyp;
import ch.dvbern.ebegu.enums.EnumFamilienstatus;
import ch.dvbern.ebegu.enums.GemeindeStatus;
import ch.dvbern.ebegu.enums.Geschlecht;
import ch.dvbern.ebegu.enums.Kinderabzug;
import ch.dvbern.ebegu.enums.Land;
import ch.dvbern.ebegu.enums.Sprache;
import ch.dvbern.ebegu.enums.Taetigkeit;
import ch.dvbern.ebegu.types.DateRange;
import ch.dvbern.ebegu.util.Constants;
import ch.dvbern.ebegu.util.MathUtil;
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

	public static final String ID_INSTITUTION_STAMMDATEN_WEISSENSTEIN_KITA = "945e3eef-8f43-43d2-a684-4aa61089684b";
	public static final String ID_INSTITUTION_STAMMDATEN_TAGESFAMILIEN = "6b7beb6e-6cf3-49d6-84c0-5818d9215ecd";
	public static final String ID_INSTITUTION_STAMMDATEN_BRUENNEN_KITA = "9a0eb656-b6b7-4613-8f55-4e0e4720455e";
	public static final String ID_INSTITUTION_STAMMDATEN_BERN_TAGESSCULHE = "199ac4a1-448f-4d4c-b3a6-5aee21f89613";
	public static final String ID_INSTITUTION_STAMMDATEN_GUARDA_FERIENINSEL = "9d8ff34f-8856-4dd3-ade2-2469aadac0ed";

	protected final Gesuchsperiode gesuchsperiode;
	protected final Collection<InstitutionStammdaten> institutionStammdatenList;

	protected String fixId = null;
	protected Fall fall = null;
	protected Gemeinde gemeinde = null;
	protected Dossier dossier = null;
	protected Gesuch gesuch = null;
	protected final boolean betreuungenBestaetigt;

	protected AbstractTestfall(
		Gesuchsperiode gesuchsperiode,
		Collection<InstitutionStammdaten> institutionStammdatenList,
		boolean betreuungenBestaetigt
	) {
		this.gesuchsperiode = gesuchsperiode;
		this.institutionStammdatenList = institutionStammdatenList;
		this.betreuungenBestaetigt = betreuungenBestaetigt;
	}

	protected AbstractTestfall(
		Gesuchsperiode gesuchsperiode,
		Collection<InstitutionStammdaten> institutionStammdatenList,
		boolean betreuungenBestaetigt, Gemeinde gemeinde
	) {
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

	private void createDossier(@Nonnull Fall fallParam, @Nullable Benutzer verantwortlicher) {
		dossier = createDossier(fallParam);
		dossier.setVerantwortlicherBG(verantwortlicher);
		dossier.setTimestampErstellt(LocalDateTime.now().minusDays(7));
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
		testGemeinde.setStatus(GemeindeStatus.AKTIV);
		testGemeinde.setName("Testgemeinde");
		testGemeinde.setBfsNummer(1L);
		testGemeinde.setBetreuungsgutscheineStartdatum(LocalDate.of(2016, 1, 1));
		testGemeinde.setTagesschulanmeldungenStartdatum(LocalDate.of(2018,8,1));
		testGemeinde.setFerieninselanmeldungenStartdatum(LocalDate.of(2018,8,1));
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
		//noinspection VariableNotUsedInsideIf
		if (eingangsdatum != null) {
			gesuch.setStatus(AntragStatus.IN_BEARBEITUNG_JA);
		} else {
			gesuch.setStatus(AntragStatus.IN_BEARBEITUNG_GS);
		}
	}

	private void setFinSitFieldsOfFamiliensituation(@Nonnull Familiensituation familiensituation) {
		// by default verguenstigung gewuenscht
		familiensituation.setSozialhilfeBezueger(false);
		familiensituation.setVerguenstigungGewuenscht(true);
	}

	protected Gesuch createAlleinerziehend() {
		// Familiensituation
		Familiensituation familiensituation = new Familiensituation();
		familiensituation.setFamilienstatus(EnumFamilienstatus.ALLEINERZIEHEND);
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
		gesuchsteller.setMail("test@mailbucket.dvbern.ch");
		gesuchsteller.setMobile("079 000 00 00");
		gesuchsteller.setKorrespondenzSprache(Sprache.DEUTSCH);
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

	protected ErwerbspensumContainer createErwerbspensum(int prozent) {
		ErwerbspensumContainer erwerbspensumContainer = new ErwerbspensumContainer();
		Erwerbspensum erwerbspensum = new Erwerbspensum();
		erwerbspensum.setGueltigkeit(gesuchsperiode.getGueltigkeit());
		erwerbspensum.setTaetigkeit(Taetigkeit.ANGESTELLT);
		erwerbspensum.setPensum(prozent);
		erwerbspensumContainer.setErwerbspensumJA(erwerbspensum);
		return erwerbspensumContainer;
	}

	protected KindContainer createKind(
		Geschlecht geschlecht,
		String name,
		String vorname,
		LocalDate geburtsdatum,
		Kinderabzug kinderabzug,
		boolean betreuung) {

		Kind kind = new Kind();
		kind.setGeschlecht(geschlecht);
		kind.setNachname(name);
		kind.setVorname(vorname);
		kind.setGeburtsdatum(geburtsdatum);
		kind.setKinderabzugErstesHalbjahr(kinderabzug);
		kind.setKinderabzugZweitesHalbjahr(kinderabzug);
		kind.setFamilienErgaenzendeBetreuung(betreuung);
		if (betreuung) {
			kind.setSprichtAmtssprache(Boolean.TRUE);
			kind.setEinschulungTyp(EinschulungTyp.VORSCHULALTER);
		}
		KindContainer kindContainer = new KindContainer();
		kindContainer.setKindJA(kind);
		return kindContainer;
	}

	protected Betreuung createBetreuung(String institutionStammdatenId, boolean bestaetigt) {
		Betreuung betreuung = new Betreuung();
		betreuung.setInstitutionStammdaten(createInstitutionStammdaten(institutionStammdatenId));
		if (!bestaetigt) {
			betreuung.setBetreuungsstatus(Betreuungsstatus.WARTEN);
		} else {
			betreuung.setBetreuungsstatus(Betreuungsstatus.BESTAETIGT);
			betreuung.setDatumBestaetigung(LocalDate.now());
		}
		betreuung.setVertrag(Boolean.TRUE);

		// default ErweiterteBetreuung
		ErweiterteBetreuungContainer erwBedContainer = new ErweiterteBetreuungContainer();
		erwBedContainer.setBetreuung(betreuung);
		ErweiterteBetreuung erwBed = new ErweiterteBetreuung();
		erwBed.setErweiterteBeduerfnisse(false);
		erwBed.setKeineKesbPlatzierung(true);
		erwBed.setBetreuungInGemeinde(Boolean.TRUE);
		erwBedContainer.setErweiterteBetreuungJA(erwBed);
		betreuung.setErweiterteBetreuungContainer(erwBedContainer);

		// normalerweise kümmern uns die Vorgänger in den Tests nicht. Rufe init hier auf, damit man in tests ohne
		// exception auf die Vorgänger zugreifen kann. Benötigt man Vorgänger kann man die init Methode nochmals
		// ausfüühren.
		betreuung.initVorgaengerVerfuegungen(null, null);

		return betreuung;
	}

	protected AnmeldungTagesschule createTagesschuleAnmeldung(String institutionStammdatenId){
		AnmeldungTagesschule anmeldungTagesschule = new AnmeldungTagesschule();
		anmeldungTagesschule.setInstitutionStammdaten(createInstitutionStammdaten(institutionStammdatenId));
		anmeldungTagesschule.setBetreuungsstatus(Betreuungsstatus.BESTAETIGT);

		BelegungTagesschule belegungTagesschule = new BelegungTagesschule();
		belegungTagesschule.setEintrittsdatum(LocalDate.of(2017, Month.AUGUST, 1));
		belegungTagesschule.setAbholungTagesschule(AbholungTagesschule.ALLEINE_NACH_HAUSE);
		belegungTagesschule.setBemerkung("Mein Kind isst kein Fleisch");

		assert anmeldungTagesschule.getInstitutionStammdaten().getInstitutionStammdatenTagesschule() != null;
		Set<EinstellungenTagesschule> einstellungenTagesschuleSet =
			anmeldungTagesschule.getInstitutionStammdaten().getInstitutionStammdatenTagesschule().getEinstellungenTagesschule();

		EinstellungenTagesschule einstellungenTagesschule = einstellungenTagesschuleSet.iterator().next();
		Set<ModulTagesschuleGroup> modulTagesschuleGroupSet =  einstellungenTagesschule.getModulTagesschuleGroups();

		Iterator<ModulTagesschuleGroup> modTSGroupIterator = modulTagesschuleGroupSet.iterator();

		ModulTagesschuleGroup modulTagesschuleGroup = modTSGroupIterator.next();
		Set<ModulTagesschule> modulTagesschuleSet = modulTagesschuleGroup.getModule();

		Set<BelegungTagesschuleModul> belegungTagesschuleModulSet = new TreeSet();
		Iterator<ModulTagesschule> modulTagesschuleIterator = modulTagesschuleSet.iterator();
		for(int i = 0; i < 3; i++){
			BelegungTagesschuleModul belegungTagesschuleModul = new BelegungTagesschuleModul();
			belegungTagesschuleModul.setBelegungTagesschule(belegungTagesschule);
			belegungTagesschuleModul.setModulTagesschule(modulTagesschuleIterator.next());
			belegungTagesschuleModul.setIntervall(BelegungTagesschuleModulIntervall.WOECHENTLICH);
			belegungTagesschuleModulSet.add(belegungTagesschuleModul);
		}

		modulTagesschuleGroup = modTSGroupIterator.next();
		modulTagesschuleSet = modulTagesschuleGroup.getModule();
		modulTagesschuleIterator = modulTagesschuleSet.iterator();
		for(int i = 0; i < 3; i++){
			BelegungTagesschuleModul belegungTagesschuleModul = new BelegungTagesschuleModul();
			belegungTagesschuleModul.setBelegungTagesschule(belegungTagesschule);
			belegungTagesschuleModul.setModulTagesschule(modulTagesschuleIterator.next());
			belegungTagesschuleModul.setIntervall(BelegungTagesschuleModulIntervall.WOECHENTLICH);
			belegungTagesschuleModulSet.add(belegungTagesschuleModul);
		}

		belegungTagesschule.setBelegungTagesschuleModule(belegungTagesschuleModulSet);

		anmeldungTagesschule.setBelegungTagesschule(belegungTagesschule);
		return anmeldungTagesschule;
	}

	@Nonnull
	protected InstitutionStammdaten createInstitutionStammdaten(String institutionsStammdatenId) {
		for (InstitutionStammdaten institutionStammdaten : institutionStammdatenList) {
			if (institutionStammdaten.getId().equals(institutionsStammdatenId)) {
				return institutionStammdaten;
			}
		}
		throw new IllegalStateException("Institutionsstammdaten sind nicht vorhanden: " + institutionsStammdatenId);
	}

	@Deprecated
	protected BetreuungspensumContainer createBetreuungspensum(Integer pensum) {
		return createBetreuungspensum(BigDecimal.valueOf(pensum));
	}

	protected BetreuungspensumContainer createBetreuungspensum(BigDecimal pensum) {
		BetreuungspensumContainer betreuungspensumContainer = new BetreuungspensumContainer();
		Betreuungspensum betreuungspensum = new Betreuungspensum();
		betreuungspensumContainer.setBetreuungspensumJA(betreuungspensum);
		betreuungspensum.setGueltigkeit(gesuchsperiode.getGueltigkeit());
		betreuungspensum.setPensum(pensum);
		betreuungspensum.setMonatlicheBetreuungskosten(MathUtil.DEFAULT.from(2000));
		return betreuungspensumContainer;
	}

	@Deprecated
	protected BetreuungspensumContainer createBetreuungspensum(
		Integer pensum, LocalDate datumVon,
		LocalDate datumBis) {

		return createBetreuungspensum(BigDecimal.valueOf(pensum), datumVon, datumBis);
	}

	protected BetreuungspensumContainer createBetreuungspensum(
		BigDecimal pensum,
		LocalDate datumVon,
		LocalDate datumBis) {

		BetreuungspensumContainer betreuungspensumContainer = new BetreuungspensumContainer();
		Betreuungspensum betreuungspensum = new Betreuungspensum();
		betreuungspensumContainer.setBetreuungspensumJA(betreuungspensum);
		betreuungspensum.setGueltigkeit(new DateRange(datumVon, datumBis));
		betreuungspensum.setPensum(pensum);
		betreuungspensum.setMonatlicheBetreuungskosten(MathUtil.DEFAULT.from(2000));

		return betreuungspensumContainer;
	}

	protected FinanzielleSituationContainer createFinanzielleSituationContainer() {
		FinanzielleSituationContainer finanzielleSituationContainer = new FinanzielleSituationContainer();
		FinanzielleSituation finanzielleSituation = new FinanzielleSituation();
		finanzielleSituation.setSteuerveranlagungErhalten(true);
		finanzielleSituation.setSteuererklaerungAusgefuellt(true);

		setFinSitDefaultValues(finanzielleSituation);

		finanzielleSituationContainer.setJahr(gesuchsperiode.getGueltigkeit().getGueltigAb().getYear() - 1);
		finanzielleSituationContainer.setFinanzielleSituationJA(finanzielleSituation);

		return finanzielleSituationContainer;
	}

	/**
	 * Schreibt in alle Felder der finanziellenSituation, die nicht Null sein dürfen, eine 0. Diese kann später in den
	 * Testfällen überschrieben werden.
	 */
	private void setFinSitDefaultValues(@Nonnull FinanzielleSituation finanzielleSituation) {
		finanzielleSituation.setFamilienzulage(BigDecimal.ZERO);
		finanzielleSituation.setErsatzeinkommen(BigDecimal.ZERO);
		finanzielleSituation.setErhalteneAlimente(BigDecimal.ZERO);
		finanzielleSituation.setGeleisteteAlimente(BigDecimal.ZERO);
		finanzielleSituation.setNettolohn(BigDecimal.ZERO);
		finanzielleSituation.setBruttovermoegen(BigDecimal.ZERO);
		finanzielleSituation.setSchulden(BigDecimal.ZERO);
	}

	protected EinkommensverschlechterungContainer createEinkommensverschlechterungContainer(
		Gesuch gesuch,
		boolean hasEKV1,
		boolean hasEKV2) {

		EinkommensverschlechterungContainer ekvContainer = createEinkommensverschlechterungContainer(hasEKV1, hasEKV2);
		EinkommensverschlechterungInfoContainer infoContainer =
			createEinkommensverschlechterungInfoContainer(hasEKV1, hasEKV2);
		gesuch.setEinkommensverschlechterungInfoContainer(infoContainer);
		infoContainer.setGesuch(gesuch);

		return ekvContainer;
	}

	@Nonnull
	protected EinkommensverschlechterungContainer createEinkommensverschlechterungContainer(
		boolean erstesJahr,
		boolean zweitesJahr) {

		EinkommensverschlechterungContainer ekvContainer = new EinkommensverschlechterungContainer();

		if (erstesJahr) {
			Einkommensverschlechterung ekv1 = new Einkommensverschlechterung();
			setEinkommensverschlechterungDefaultValues(ekv1);
			ekvContainer.setEkvJABasisJahrPlus1(ekv1);
		}

		if (zweitesJahr) {
			Einkommensverschlechterung ekv2 = new Einkommensverschlechterung();
			setEinkommensverschlechterungDefaultValues(ekv2);
			ekvContainer.setEkvJABasisJahrPlus2(ekv2);
		}

		return ekvContainer;
	}

	/**
	 * Schreibt in alle Felder der Einkommenverschlechterung, die nicht Null sein dürfen, eine 0. Diese kann später in
	 * den Testfällen überschrieben werden.
	 */
	private void setEinkommensverschlechterungDefaultValues(@Nonnull Einkommensverschlechterung ekv) {
		ekv.setFamilienzulage(BigDecimal.ZERO);
		ekv.setErsatzeinkommen(BigDecimal.ZERO);
		ekv.setErhalteneAlimente(BigDecimal.ZERO);
		ekv.setGeleisteteAlimente(BigDecimal.ZERO);

		ekv.setBruttovermoegen(BigDecimal.ZERO);
		ekv.setSchulden(BigDecimal.ZERO);
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
		boolean ekv1,
		boolean ekv2) {

		EinkommensverschlechterungInfoContainer infoContainer = new EinkommensverschlechterungInfoContainer();
		EinkommensverschlechterungInfo info = new EinkommensverschlechterungInfo();
		info.setEkvFuerBasisJahrPlus1(ekv1);
		info.setEkvFuerBasisJahrPlus2(ekv2);
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
