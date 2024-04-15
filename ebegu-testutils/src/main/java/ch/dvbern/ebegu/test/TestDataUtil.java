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

package ch.dvbern.ebegu.test;

import ch.dvbern.ebegu.dto.suchfilter.smarttable.*;
import ch.dvbern.ebegu.entities.*;
import ch.dvbern.ebegu.entities.gemeindeantrag.LastenausgleichTagesschuleAngabenGemeinde;
import ch.dvbern.ebegu.entities.gemeindeantrag.LastenausgleichTagesschuleAngabenGemeindeContainer;
import ch.dvbern.ebegu.entities.gemeindeantrag.LastenausgleichTagesschuleAngabenInstitution;
import ch.dvbern.ebegu.entities.sozialdienst.Sozialdienst;
import ch.dvbern.ebegu.entities.sozialdienst.SozialdienstFall;
import ch.dvbern.ebegu.entities.sozialdienst.SozialdienstStammdaten;
import ch.dvbern.ebegu.enums.*;
import ch.dvbern.ebegu.enums.gemeindeantrag.LastenausgleichTagesschuleAngabenGemeindeFormularStatus;
import ch.dvbern.ebegu.enums.gemeindeantrag.LastenausgleichTagesschuleAngabenGemeindeStatus;
import ch.dvbern.ebegu.finanzielleSituationRechner.AbstractFinanzielleSituationRechner;
import ch.dvbern.ebegu.services.BetreuungService;
import ch.dvbern.ebegu.services.FallService;
import ch.dvbern.ebegu.services.GesuchService;
import ch.dvbern.ebegu.services.InstitutionService;
import ch.dvbern.ebegu.test.util.TestDataInstitutionStammdatenBuilder;
import ch.dvbern.ebegu.testfaelle.*;
import ch.dvbern.ebegu.testfaelle.institutionStammdatenBuilder.InstitutionStammdatenBuilder;
import ch.dvbern.ebegu.types.DateRange;
import ch.dvbern.ebegu.util.Constants;
import ch.dvbern.ebegu.util.KitaxUebergangsloesungParameter;
import ch.dvbern.ebegu.util.MathUtil;
import ch.dvbern.ebegu.util.mandant.MandantIdentifier;
import ch.dvbern.lib.cdipersistence.Persistence;
import ch.dvbern.oss.lib.beanvalidation.embeddables.IBAN;
import com.google.errorprone.annotations.CanIgnoreReturnValue;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.math.BigDecimal;
import java.time.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

import static ch.dvbern.ebegu.enums.EinstellungKey.*;
import static ch.dvbern.ebegu.util.Constants.*;

/**
 * comments homa
 */
@SuppressWarnings("PMD.NcssTypeCount")
@CanIgnoreReturnValue
public final class TestDataUtil {

	private static final String iban = "CH39 0900 0000 3066 3817 2";
	public static final String TESTMAIL = "mail@example.com";

	public static final int ABWESENHEIT_DAYS_LIMIT = 30;
	public static final int PERIODE_JAHR_0 = 2016;
	public static final int PERIODE_JAHR_1 = 2017;
	public static final int PERIODE_JAHR_2 = 2018;

	public static final LocalDate START_PERIODE = LocalDate.of(PERIODE_JAHR_1, Month.AUGUST, 1);
	public static final LocalDate ENDE_PERIODE = LocalDate.of(PERIODE_JAHR_2, Month.JULY, 31);

	public static final String TEST_STRASSE = "Nussbaumstrasse";

	public static final String GEMEINDE_PARIS_ID = "4c453263-f992-48af-86b5-dc04cd7e8bb8";
	public static final String GEMEINDE_LONDON_ID = "4c453263-f992-48af-86b5-dc04cd7e8777";

	public static final AtomicLong SEQUENCE = new AtomicLong();

	private TestDataUtil() {
	}

	public static GesuchstellerAdresseContainer createDefaultGesuchstellerAdresseContainer(@Nonnull GesuchstellerContainer gsContainer) {
		final GesuchstellerAdresseContainer gsAdressCont = new GesuchstellerAdresseContainer();
		gsAdressCont.setGesuchstellerContainer(gsContainer);
		gsAdressCont.setGesuchstellerAdresseJA(createDefaultGesuchstellerAdresse());
		return gsAdressCont;
	}

	public static GesuchstellerAdresseContainer createDefaultGesuchstellerAdresseContainerGS(@Nonnull GesuchstellerContainer gsContainer) {
		final GesuchstellerAdresseContainer gsAdressCont = new GesuchstellerAdresseContainer();
		gsAdressCont.setGesuchstellerContainer(gsContainer);
		gsAdressCont.setGesuchstellerAdresseGS(createDefaultGesuchstellerAdresse());
		return gsAdressCont;
	}

	public static GesuchstellerAdresse createDefaultGesuchstellerAdresse() {
		GesuchstellerAdresse gesuchstellerAdresse = new GesuchstellerAdresse();
		gesuchstellerAdresse.setStrasse(TEST_STRASSE);
		gesuchstellerAdresse.setHausnummer("21");
		gesuchstellerAdresse.setZusatzzeile("c/o Uwe Untermieter");
		gesuchstellerAdresse.setPlz("3006");
		gesuchstellerAdresse.setOrt("Bern");
		gesuchstellerAdresse.setGueltigkeit(new DateRange(START_OF_TIME, Constants.END_OF_TIME));
		gesuchstellerAdresse.setAdresseTyp(AdresseTyp.WOHNADRESSE);
		return gesuchstellerAdresse;
	}

	public static Adresse createDefaultAdresse() {
		Adresse adresse = new Adresse();
		adresse.setOrganisation("Jugendamt");
		adresse.setStrasse("Nussbaumstrasse");
		adresse.setHausnummer("21");
		adresse.setZusatzzeile("c/o Uwe Untermieter");
		adresse.setPlz("3006");
		adresse.setOrt("Bern");
		adresse.setGueltigkeit(new DateRange(LocalDate.now(), Constants.END_OF_TIME));
		return adresse;
	}

	public static GesuchstellerContainer createDefaultGesuchstellerContainer() {
		final GesuchstellerContainer gesuchstellerContainer = new GesuchstellerContainer();
		gesuchstellerContainer.addAdresse(createDefaultGesuchstellerAdresseContainer(gesuchstellerContainer));
		gesuchstellerContainer.setGesuchstellerJA(createDefaultGesuchsteller());
		return gesuchstellerContainer;
	}

	public static Gesuchsteller createDefaultGesuchsteller() {
		Gesuchsteller gesuchsteller = new Gesuchsteller();
		gesuchsteller.setGeburtsdatum(LocalDate.of(1984, 12, 12));
		gesuchsteller.setVorname("Tim");
		gesuchsteller.setNachname("Tester");
		gesuchsteller.setGeschlecht(Geschlecht.MAENNLICH);
		gesuchsteller.setMail("tim.tester@example.com");
		gesuchsteller.setMobile("076 309 30 58");
		gesuchsteller.setTelefon("031 378 24 24");
		return gesuchsteller;
	}

	public static EinkommensverschlechterungContainer createDefaultEinkommensverschlechterungsContainer() {
		EinkommensverschlechterungContainer einkommensverschlechterungContainer =
			new EinkommensverschlechterungContainer();

		einkommensverschlechterungContainer.setEkvGSBasisJahrPlus1(createDefaultEinkommensverschlechterung());

		final Einkommensverschlechterung ekvGSBasisJahrPlus2 = createDefaultEinkommensverschlechterung();
		ekvGSBasisJahrPlus2.setNettolohn(BigDecimal.valueOf(2));
		einkommensverschlechterungContainer.setEkvGSBasisJahrPlus2(ekvGSBasisJahrPlus2);

		final Einkommensverschlechterung ekvJABasisJahrPlus1 = createDefaultEinkommensverschlechterung();
		ekvJABasisJahrPlus1.setNettolohn(BigDecimal.valueOf(3));
		einkommensverschlechterungContainer.setEkvJABasisJahrPlus1(ekvJABasisJahrPlus1);

		final Einkommensverschlechterung ekvJABasisJahrPlus2 = createDefaultEinkommensverschlechterung();
		ekvJABasisJahrPlus2.setNettolohn(BigDecimal.valueOf(4));
		einkommensverschlechterungContainer.setEkvJABasisJahrPlus2(ekvJABasisJahrPlus2);

		return einkommensverschlechterungContainer;
	}

	public static Einkommensverschlechterung createDefaultEinkommensverschlechterung() {
		Einkommensverschlechterung einkommensverschlechterung = new Einkommensverschlechterung();
		einkommensverschlechterung.setNettolohn(MathUtil.DEFAULT.from(BigDecimal.ONE));
		return einkommensverschlechterung;
	}

	public static FamiliensituationContainer createDefaultFamiliensituationContainer() {
		FamiliensituationContainer familiensituationContainer = new FamiliensituationContainer();
		familiensituationContainer.setFamiliensituationJA(createDefaultFamiliensituation());
		return familiensituationContainer;
	}

	public static Familiensituation createDefaultFamiliensituation() {
		Familiensituation familiensituation = new Familiensituation();
		familiensituation.setFamilienstatus(EnumFamilienstatus.ALLEINERZIEHEND);
		// by default verguenstigung gewuenscht
		familiensituation.setSozialhilfeBezueger(false);
		familiensituation.setVerguenstigungGewuenscht(true);
		familiensituation.setGemeinsameSteuererklaerung(Boolean.TRUE);
		return familiensituation;
	}

	public static Gesuch createDefaultGesuch() {
		return createDefaultGesuch(AntragStatus.IN_BEARBEITUNG_JA);
	}

	public static Gesuch createDefaultGesuch(@Nullable AntragStatus status) {
		Gesuch gesuch = new Gesuch();
		gesuch.setGesuchsperiode(createGesuchsperiode1718());
		gesuch.setDossier(createDefaultDossier());
		gesuch.setEingangsdatum(LocalDate.now());
		gesuch.setFamiliensituationContainer(createDefaultFamiliensituationContainer());
		if (status != null) {
			gesuch.setStatus(status);
		} else {
			gesuch.setStatus(AntragStatus.IN_BEARBEITUNG_JA);
		}
		return gesuch;
	}

	public static Fall createDefaultFall() {
		var fall = new Fall();
		fall.setMandant(getMandantKantonBern());
		return fall;
	}

	public static Fall createDefaultFall(Mandant mandant) {
		var fall = new Fall();
		fall.setMandant(mandant);
		return fall;
	}

	public static Dossier createDefaultDossier() {
		Dossier dossier = new Dossier();
		dossier.setFall(createDefaultFall());
		dossier.setGemeinde(createGemeindeLondon());
		return dossier;
	}

	public static Dossier createDefaultDossier(Mandant mandant) {
		Dossier dossier = new Dossier();
		dossier.setFall(createDefaultFall(mandant));
		dossier.setGemeinde(createGemeindeLondon(mandant));
		return dossier;
	}

	public static Mandant createDefaultMandant() {
		Mandant mandant = new Mandant();
		mandant.setId(UUID.randomUUID().toString());
		mandant.setName("Mandant1");
		return mandant;
	}

	public static Mandant getMandantKantonBern() {
		Mandant mandant = new Mandant();
		mandant.setId(AbstractTestfall.ID_MANDANT_KANTON_BERN);
		mandant.setMandantIdentifier(MandantIdentifier.BERN);
		mandant.setName("Kanton Bern");
		return mandant;
	}

	@Nonnull
	public static Mandant getMandantKantonBernAndPersist(@Nonnull Persistence persistence) {
		Mandant mandant = persistence.find(Mandant.class, AbstractTestfall.ID_MANDANT_KANTON_BERN);
		if (mandant == null) {
			mandant = getMandantKantonBern();
			return persistence.persist(mandant);
		}
		return mandant;
	}

	public static Mandant persistMandantIfNecessary(@Nonnull Mandant mandant, @Nonnull Persistence persistence) {
		Mandant mandantFromDB = persistence.find(Mandant.class, mandant.getId());
		if (mandantFromDB == null) {
			return persistence.persist(mandant);
		}
		return mandantFromDB;
	}

	@Nonnull
	public static Mandant getMandantKantonLuzernAndPersist(@Nonnull Persistence persistence) {
		Mandant mandant = persistence.find(Mandant.class, AbstractTestfall.ID_MANDANT_KANTON_LUZERN);
		if (mandant == null) {
			mandant = getMandantLuzern();
			return persistence.persist(mandant);
		}
		return mandant;
	}

	@Nonnull
	public static Mandant getMandantLuzern() {
		Mandant mandant;
		mandant = new Mandant();
		mandant.setId(AbstractTestfall.ID_MANDANT_KANTON_LUZERN);
		mandant.setMandantIdentifier(MandantIdentifier.LUZERN);
		mandant.setName("Stadt Luzern");
		return mandant;
	}

	@Nonnull
	public static Mandant getMandantSchwyz() {
		Mandant mandant;
		mandant = new Mandant();
		mandant.setId(AbstractTestfall.ID_MANDANT_KANTON_SCHWYZ);
		mandant.setMandantIdentifier(MandantIdentifier.SCHWYZ);
		mandant.setName("Schwyz");
		return mandant;
	}

	public static Gemeinde getTestGemeinde(Persistence persistence) {
		Gemeinde gemeinde = persistence.find(Gemeinde.class, GEMEINDE_PARIS_ID);
		if (gemeinde == null) {
			gemeinde = new Gemeinde();
			gemeinde.setId(GEMEINDE_PARIS_ID);
			gemeinde.setName("Testgemeinde");
			gemeinde.setBfsNummer(SEQUENCE.incrementAndGet());
			gemeinde.setStatus(GemeindeStatus.AKTIV);
			gemeinde.setMandant(getMandantKantonBernAndPersist(persistence));
			gemeinde.setAngebotBG(true);
			gemeinde.setBetreuungsgutscheineStartdatum(LocalDate.of(2016, 1, 1));
			gemeinde.setTagesschulanmeldungenStartdatum(LocalDate.of(2020, 8, 1));
			gemeinde.setFerieninselanmeldungenStartdatum(LocalDate.of(2020, 8, 1));
			return persistence.persist(gemeinde);
		}
		return gemeinde;
	}

	@Nonnull
	public static Gemeinde getGemeindeParis(@Nonnull Persistence persistence) {
		GemeindeStammdaten stammdatenParis = getGemeindeStammdatenParis(persistence);
		return stammdatenParis.getGemeinde();
	}

	@Nonnull
	public static Gemeinde getGemeindeLondon(@Nonnull Persistence persistence) {
		GemeindeStammdaten stammdatenLondon = getGemeindeStammdatenLondon(persistence);
		return stammdatenLondon.getGemeinde();
	}

	public static GemeindeStammdaten getGemeindeStammdatenParis(@Nonnull Persistence persistence) {
		Gemeinde gemeinde = persistence.find(Gemeinde.class, GEMEINDE_PARIS_ID);
		if (gemeinde == null) {
			gemeinde = createGemeindeParis();
			Objects.requireNonNull(gemeinde.getMandant());
			TestDataUtil.persistMandantIfNecessary(gemeinde.getMandant(), persistence);
			gemeinde = persistence.persist(gemeinde);
		}
		GemeindeStammdaten stammdaten = persistence.find(GemeindeStammdaten.class, GEMEINDE_PARIS_ID);
		if (stammdaten == null) {
			stammdaten = createGemeindeStammdaten(gemeinde);
			stammdaten.setId(GEMEINDE_PARIS_ID);
			stammdaten = persistence.merge(stammdaten);
		}
		return stammdaten;
	}

	public static GemeindeStammdaten getGemeindeStammdatenLondon(@Nonnull Persistence persistence) {
		Gemeinde gemeinde = persistence.find(Gemeinde.class, GEMEINDE_LONDON_ID);
		if (gemeinde == null) {
			gemeinde = createGemeindeLondon();
			Objects.requireNonNull(gemeinde.getMandant());
			TestDataUtil.persistMandantIfNecessary(gemeinde.getMandant(), persistence);
			gemeinde = persistence.persist(gemeinde);
		}
		GemeindeStammdaten stammdaten = persistence.find(GemeindeStammdaten.class, GEMEINDE_LONDON_ID);
		if (stammdaten == null) {
			stammdaten = createGemeindeStammdaten(gemeinde);
			stammdaten.setId(GEMEINDE_LONDON_ID);
			stammdaten = persistence.merge(stammdaten);
		}
		return stammdaten;
	}

	@Nonnull
	public static Gemeinde createGemeindeParis() {
		Gemeinde gemeinde = new Gemeinde();
		gemeinde.setId(GEMEINDE_PARIS_ID);
		gemeinde.setName("Paris");
		gemeinde.setStatus(GemeindeStatus.AKTIV);
		gemeinde.setGemeindeNummer(1);
		gemeinde.setBfsNummer(99998L);
		gemeinde.setMandant(getMandantKantonBern());
		gemeinde.setAngebotBG(true);
		gemeinde.setBetreuungsgutscheineStartdatum(LocalDate.of(2016, 1, 1));
		gemeinde.setTagesschulanmeldungenStartdatum(LocalDate.of(2020, 8, 1));
		gemeinde.setFerieninselanmeldungenStartdatum(LocalDate.of(2020, 8, 1));
		GemeindeStammdaten stammdaten = createGemeindeStammdaten(gemeinde);
		stammdaten.setId(GEMEINDE_PARIS_ID);
		return gemeinde;
	}

	@Nonnull
	public static Gemeinde createGemeindeLondon() {
		Gemeinde gemeinde = new Gemeinde();
		gemeinde.setId(GEMEINDE_LONDON_ID);
		gemeinde.setName("London");
		gemeinde.setStatus(GemeindeStatus.AKTIV);
		gemeinde.setGemeindeNummer(2);
		gemeinde.setBfsNummer(99999L);
		gemeinde.setMandant(getMandantKantonBern());
		gemeinde.setAngebotBG(true);
		gemeinde.setBetreuungsgutscheineStartdatum(LocalDate.of(2016, 1, 1));
		gemeinde.setTagesschulanmeldungenStartdatum(LocalDate.of(2020, 8, 1));
		gemeinde.setFerieninselanmeldungenStartdatum(LocalDate.of(2020, 8, 1));
		GemeindeStammdaten stammdaten = createGemeindeStammdaten(gemeinde);
		stammdaten.setId(GEMEINDE_LONDON_ID);
		return gemeinde;
	}

	@Nonnull
	public static Gemeinde createGemeindeLondon(Mandant mandant) {
		Gemeinde gemeinde = new Gemeinde();
		gemeinde.setId(GEMEINDE_LONDON_ID);
		gemeinde.setName("London");
		gemeinde.setStatus(GemeindeStatus.AKTIV);
		gemeinde.setGemeindeNummer(2);
		gemeinde.setBfsNummer(99999L);
		gemeinde.setMandant(mandant);
		gemeinde.setAngebotBG(true);
		gemeinde.setBetreuungsgutscheineStartdatum(LocalDate.of(2016, 1, 1));
		gemeinde.setTagesschulanmeldungenStartdatum(LocalDate.of(2020, 8, 1));
		gemeinde.setFerieninselanmeldungenStartdatum(LocalDate.of(2020, 8, 1));
		GemeindeStammdaten stammdaten = createGemeindeStammdaten(gemeinde);
		stammdaten.setId(GEMEINDE_LONDON_ID);
		return gemeinde;
	}

	public static Fachstelle createDefaultFachstelle() {
		Fachstelle fachstelle = new Fachstelle();
		fachstelle.setName(FachstelleName.DIENST_ZENTRUM_HOEREN_SPRACHE);
		fachstelle.setFachstelleAnspruch(true);
		fachstelle.setFachstelleErweiterteBetreuung(false);
		fachstelle.setMandant(getMandantKantonBern());
		return fachstelle;
	}

	public static FinanzielleSituationContainer createFinanzielleSituationContainer() {
		FinanzielleSituationContainer container = new FinanzielleSituationContainer();
		container.setJahr(LocalDate.now().minusYears(1).getYear());
		return container;
	}

	public static FinanzielleSituation createDefaultFinanzielleSituation() {
		FinanzielleSituation finanzielleSituation = new FinanzielleSituation();
		finanzielleSituation.setSteuerveranlagungErhalten(Boolean.FALSE);
		finanzielleSituation.setSteuererklaerungAusgefuellt(Boolean.TRUE);
		finanzielleSituation.setNettolohn(BigDecimal.valueOf(100000));
		finanzielleSituation.setBruttovermoegen(BigDecimal.ZERO);
		finanzielleSituation.setErhalteneAlimente(BigDecimal.ZERO);
		finanzielleSituation.setErsatzeinkommen(BigDecimal.ZERO);
		finanzielleSituation.setFamilienzulage(BigDecimal.ZERO);
		finanzielleSituation.setGeleisteteAlimente(BigDecimal.ZERO);
		finanzielleSituation.setSchulden(BigDecimal.ZERO);
		return finanzielleSituation;
	}

	public static Traegerschaft createDefaultTraegerschaft() {
		Traegerschaft traegerschaft = new Traegerschaft();
		traegerschaft.setName("Traegerschaft" + UUID.randomUUID().toString());
		traegerschaft.setMandant(getMandantKantonBern());
		return traegerschaft;
	}

	public static Traegerschaft createDefaultTraegerschaft(Mandant mandant) {
		Traegerschaft traegerschaft = new Traegerschaft();
		traegerschaft.setName("Traegerschaft" + UUID.randomUUID().toString());
		traegerschaft.setMandant(mandant);
		return traegerschaft;
	}

	public static Institution createDefaultInstitution() {
		Institution institution = new Institution();
		institution.setName("Institution1");
		institution.setMandant(getMandantKantonBern());
		institution.setTraegerschaft(createDefaultTraegerschaft(institution.getMandant()));
		return institution;
	}

	public static Institution createDefaultInstitution(Mandant mandant) {
		Institution institution = new Institution();
		institution.setName("Institution1");
		institution.setMandant(mandant);
		institution.setTraegerschaft(createDefaultTraegerschaft(institution.getMandant()));
		return institution;
	}

	public static InstitutionStammdaten createDefaultInstitutionStammdaten() {
		return createInstitutionStammdaten(
			UUID.randomUUID().toString(),
			"Testinstitution",
			BetreuungsangebotTyp.KITA);
	}

	public static InstitutionStammdaten createDefaultInstitutionStammdaten(Mandant mandant) {
		return createInstitutionStammdaten(
			UUID.randomUUID().toString(),
			"Testinstitution",
			BetreuungsangebotTyp.KITA,
			mandant);
	}

	public static InstitutionStammdaten createInstitutionStammdatenKitaWeissenstein() {
		return createInstitutionStammdaten(
			AbstractTestfall.ID_INSTITUTION_STAMMDATEN_WEISSENSTEIN_KITA,
			"Kita Aaregg",
			BetreuungsangebotTyp.KITA);
	}

	public static InstitutionStammdaten createInstitutionStammdatenTagesfamilien() {
		return createInstitutionStammdaten(
			AbstractTestfall.ID_INSTITUTION_STAMMDATEN_TAGESFAMILIEN,
			"Tagesfamilien",
			BetreuungsangebotTyp.TAGESFAMILIEN);
	}

	public static InstitutionStammdaten createInstitutionStammdatenKitaBruennen() {
		return createInstitutionStammdaten(
			AbstractTestfall.ID_INSTITUTION_STAMMDATEN_BRUENNEN_KITA,
			"Kita Brünnen",
			BetreuungsangebotTyp.KITA);
	}

	public static InstitutionStammdaten createInstitutionStammdatenTagesschuleBern(Gesuchsperiode gesuchsperiode) {
		return createTagesschuleInstitutionStammdaten(
			AbstractTestfall.ID_INSTITUTION_STAMMDATEN_BERN_TAGESSCULHE,
			"Tagesschule Bern", gesuchsperiode
			);
	}

	private static InstitutionStammdaten createTagesschuleInstitutionStammdaten(@Nonnull String id,
		@Nonnull String name,@Nonnull Gesuchsperiode gesuchsperiode) {
		InstitutionStammdaten instStammdaten = new InstitutionStammdaten();
		instStammdaten.setId(id);
		instStammdaten.setMail(TESTMAIL);
		instStammdaten.setGueltigkeit(Constants.DEFAULT_GUELTIGKEIT);
		instStammdaten.setBetreuungsangebotTyp(BetreuungsangebotTyp.TAGESSCHULE);
		instStammdaten.setInstitution(createDefaultInstitution());
		instStammdaten.getInstitution().setName(name);
		instStammdaten.setAdresse(createDefaultAdresse());

		InstitutionStammdatenTagesschule institutionStammdatenTagesschule = new InstitutionStammdatenTagesschule();
		institutionStammdatenTagesschule.setGemeinde(createGemeindeParis());

		//modul Tagesschule Group Vormittag
		TextRessource vormittagText = new TextRessource();
		vormittagText.setTextDeutsch("Vormittag");
		vormittagText.setTextFranzoesisch("Matin");
		ModulTagesschuleGroup vormittag = createModulTagesschuleGroup(vormittagText ,
			ModulTagesschuleIntervall.WOECHENTLICH,
			"4y69g9PhD9mXguXcPAwlFinnfo6RTdyuzWuX", "3.00", 8, 0, 12,0);

		//modul Tagesschule Group Nachmittag
		TextRessource nachmittagText = new TextRessource();
		nachmittagText.setTextDeutsch("Nachmittag");
		nachmittagText.setTextFranzoesisch("Après-midi");
		ModulTagesschuleGroup nachmittag = createModulTagesschuleGroup(nachmittagText, ModulTagesschuleIntervall.WOECHENTLICH,
			"4y69g9PhD9mXguXcPAwlFinnfo6RTdyuzWuY", null, 13, 0, 16,0);

		//modul Tagesschule Group Mittag
		TextRessource mittagText = new TextRessource();
		mittagText.setTextDeutsch("Mittag");
		mittagText.setTextFranzoesisch("Midi");
		ModulTagesschuleGroup mittag = createModulTagesschuleGroup(mittagText, ModulTagesschuleIntervall.WOECHENTLICH,
			"4y69g9PhD9mXguXcPAwlFinnfo6RTdyuzWuZ", "10.00", 12, 0, 13,0);

		//Einstellungen Tagesschule
		EinstellungenTagesschule einstellungenTagesschule = new EinstellungenTagesschule();
		einstellungenTagesschule.setGesuchsperiode(gesuchsperiode);
		einstellungenTagesschule.setModulTagesschuleTyp(ModulTagesschuleTyp.DYNAMISCH);
		einstellungenTagesschule.setInstitutionStammdatenTagesschule(institutionStammdatenTagesschule);

		vormittag.setEinstellungenTagesschule(einstellungenTagesschule);
		nachmittag.setEinstellungenTagesschule(einstellungenTagesschule);
		mittag.setEinstellungenTagesschule(einstellungenTagesschule);

		Set<ModulTagesschuleGroup> modulTSGroupSet = new TreeSet<>();
		modulTSGroupSet.add(vormittag);
		modulTSGroupSet.add(mittag);
		modulTSGroupSet.add(nachmittag);

		einstellungenTagesschule.setModulTagesschuleGroups(modulTSGroupSet);

		Set<EinstellungenTagesschule> einstellungenTagesschuleSet = new TreeSet<>();
		einstellungenTagesschuleSet.add(einstellungenTagesschule);
		institutionStammdatenTagesschule.setEinstellungenTagesschule(einstellungenTagesschuleSet);

		instStammdaten.setInstitutionStammdatenTagesschule(institutionStammdatenTagesschule);

		return instStammdaten;
	}

	private static ModulTagesschuleGroup createModulTagesschuleGroup(@Nonnull TextRessource bezeichnung,
		@Nonnull ModulTagesschuleIntervall intervall, @Nonnull String identifier,@Nullable String verpflegungskosten, @Nonnull int startHour,
		@Nonnull int startMinute, @Nonnull int stopHour, @Nonnull int stopMinute){
		ModulTagesschuleGroup mtg = new ModulTagesschuleGroup();
		mtg.setBezeichnung(bezeichnung);
		mtg.setIntervall(intervall);
		mtg.setIdentifier(identifier);
		mtg.setReihenfolge(0);
		mtg.setModulTagesschuleName(ModulTagesschuleName.DYNAMISCH);
		if(verpflegungskosten != null) {
			mtg.setVerpflegungskosten(new BigDecimal(verpflegungskosten));
		}
		mtg.setWirdPaedagogischBetreut(false);
		mtg.setZeitVon(LocalTime.of(startHour,startMinute));
		mtg.setZeitBis(LocalTime.of(stopHour,stopMinute));

		//modul Tagesschule
		Set<ModulTagesschule> modulTSSet = new TreeSet<>();

		ModulTagesschule monday = new ModulTagesschule();
		monday.setWochentag(DayOfWeek.MONDAY);
		monday.setModulTagesschuleGroup(mtg);
		modulTSSet.add(monday);

		ModulTagesschule tuesday = new ModulTagesschule();
		tuesday.setWochentag(DayOfWeek.TUESDAY);
		tuesday.setModulTagesschuleGroup(mtg);
		modulTSSet.add(tuesday);

		ModulTagesschule thursday = new ModulTagesschule();
		thursday.setWochentag(DayOfWeek.THURSDAY);
		thursday.setModulTagesschuleGroup(mtg);
		modulTSSet.add(thursday);

		ModulTagesschule friday = new ModulTagesschule();
		friday.setWochentag(DayOfWeek.FRIDAY);
		friday.setModulTagesschuleGroup(mtg);
		modulTSSet.add(friday);

		mtg.setModule(modulTSSet);
		return mtg;
	}

	public static InstitutionStammdaten createInstitutionStammdatenFerieninselGuarda() {
		return createInstitutionStammdaten(
			AbstractTestfall.ID_INSTITUTION_STAMMDATEN_GUARDA_FERIENINSEL,
			"Ferieninsel Guarda",
			BetreuungsangebotTyp.FERIENINSEL);
	}

	private static InstitutionStammdaten createInstitutionStammdaten(@Nonnull String id, @Nonnull String name, @Nonnull BetreuungsangebotTyp angebotTyp) {
		InstitutionStammdaten instStammdaten = new InstitutionStammdaten();
		instStammdaten.setId(id);
		instStammdaten.setMail(TESTMAIL);
		instStammdaten.setGueltigkeit(Constants.DEFAULT_GUELTIGKEIT);
		instStammdaten.setBetreuungsangebotTyp(angebotTyp);
		instStammdaten.setInstitution(createDefaultInstitution());
		instStammdaten.getInstitution().setName(name);
		instStammdaten.setAdresse(createDefaultAdresse());
		InstitutionStammdatenBetreuungsgutscheine institutionStammdatenBetreuungsgutscheine = new InstitutionStammdatenBetreuungsgutscheine();
		institutionStammdatenBetreuungsgutscheine.setAnzahlPlaetze(BigDecimal.TEN);
		Auszahlungsdaten auszahlungsdaten = new Auszahlungsdaten();
		auszahlungsdaten.setIban(new IBAN(iban));
		auszahlungsdaten.setKontoinhaber("Kontoinhaber " + name);
		institutionStammdatenBetreuungsgutscheine.setAuszahlungsdaten(auszahlungsdaten);
		instStammdaten.setInstitutionStammdatenBetreuungsgutscheine(institutionStammdatenBetreuungsgutscheine);
		return instStammdaten;
	}

	private static InstitutionStammdaten createInstitutionStammdaten(@Nonnull String id, @Nonnull String name, @Nonnull BetreuungsangebotTyp angebotTyp, Mandant mandant) {
		InstitutionStammdaten instStammdaten = new InstitutionStammdaten();
		instStammdaten.setId(id);
		instStammdaten.setMail(TESTMAIL);
		instStammdaten.setGueltigkeit(Constants.DEFAULT_GUELTIGKEIT);
		instStammdaten.setBetreuungsangebotTyp(angebotTyp);
		instStammdaten.setInstitution(createDefaultInstitution(mandant));
		instStammdaten.getInstitution().setName(name);
		instStammdaten.setAdresse(createDefaultAdresse());
		InstitutionStammdatenBetreuungsgutscheine institutionStammdatenBetreuungsgutscheine = new InstitutionStammdatenBetreuungsgutscheine();
		institutionStammdatenBetreuungsgutscheine.setAnzahlPlaetze(BigDecimal.TEN);
		Auszahlungsdaten auszahlungsdaten = new Auszahlungsdaten();
		auszahlungsdaten.setIban(new IBAN(iban));
		auszahlungsdaten.setKontoinhaber("Kontoinhaber " + name);
		institutionStammdatenBetreuungsgutscheine.setAuszahlungsdaten(auszahlungsdaten);
		instStammdaten.setInstitutionStammdatenBetreuungsgutscheine(institutionStammdatenBetreuungsgutscheine);
		return instStammdaten;
	}

	public static Collection<InstitutionStammdaten> saveInstitutionsstammdatenForTestfaelle(@Nonnull Persistence persistence, Gesuchsperiode gesuchsperiode) {
		final InstitutionStammdaten institutionStammdatenKitaAaregg = createInstitutionStammdatenKitaWeissenstein();
		final InstitutionStammdaten institutionStammdatenKitaBruennen = createInstitutionStammdatenKitaBruennen();
		final InstitutionStammdaten institutionStammdatenTagesfamilien = createInstitutionStammdatenTagesfamilien();
		final InstitutionStammdaten institutionStammdatenTagesschuleBruennen = createInstitutionStammdatenTagesschuleBern(gesuchsperiode);
		final InstitutionStammdaten institutionStammdatenFerieninselBruennen = createInstitutionStammdatenFerieninselGuarda();
		final Mandant mandant = getMandantKantonBern();

		institutionStammdatenKitaAaregg.getInstitution().setMandant(mandant);
		institutionStammdatenKitaBruennen.getInstitution().setMandant(mandant);
		institutionStammdatenTagesfamilien.getInstitution().setMandant(mandant);
		institutionStammdatenTagesschuleBruennen.getInstitution().setMandant(mandant);
		institutionStammdatenFerieninselBruennen.getInstitution().setMandant(mandant);

		Collection<InstitutionStammdaten> list = new ArrayList<>();
		list.add(saveInstitutionStammdatenIfNecessary(persistence, institutionStammdatenKitaAaregg));
		list.add(saveInstitutionStammdatenIfNecessary(persistence, institutionStammdatenTagesfamilien));
		list.add(saveInstitutionStammdatenIfNecessary(persistence, institutionStammdatenKitaBruennen));
		list.add(saveInstitutionStammdatenIfNecessary(persistence, institutionStammdatenTagesschuleBruennen));
		list.add(saveInstitutionStammdatenIfNecessary(persistence, institutionStammdatenFerieninselBruennen));
		return list;
	}

	@Nonnull
	public static InstitutionStammdaten saveInstitutionStammdatenIfNecessary(@Nonnull Persistence persistence, @Nonnull InstitutionStammdaten institutionStammdaten) {
		Institution institution = saveInstitutionIfNecessary(persistence, institutionStammdaten.getInstitution());
		InstitutionStammdaten found = persistence.find(InstitutionStammdaten.class, institutionStammdaten.getId());
		if (found == null) {
			institutionStammdaten.setInstitution(institution);
			return persistence.merge(institutionStammdaten);
		}
		return found;
	}

	public static void saveInstitutionStammdatenTagesschule(@Nonnull Persistence persistence,
		@Nonnull InstitutionStammdatenTagesschule institutionStammdatenTagesschule) {
		InstitutionStammdatenTagesschule foundStammdatenTagesschule =
			persistence.find(InstitutionStammdatenTagesschule.class, institutionStammdatenTagesschule.getId());
		if(foundStammdatenTagesschule == null){
			persistence.merge(institutionStammdatenTagesschule);
		}
	}

	@Nonnull
	private static Institution saveInstitutionIfNecessary(@Nonnull Persistence persistence, @Nonnull Institution institution) {
		saveTraegerschaftIfNecessary(persistence, institution.getTraegerschaft());
		saveMandantIfNecessary(persistence, institution.getMandant());
		Institution found = persistence.find(Institution.class, institution.getId());
		if (found == null) {
			found = persistEntity(persistence, institution);
		}
		return found;
	}

	private static void saveTraegerschaftIfNecessary(@Nonnull Persistence persistence, @Nullable Traegerschaft traegerschaft) {
		if (traegerschaft != null) {
			saveMandantIfNecessary(persistence, traegerschaft.getMandant());
			Traegerschaft found = persistence.find(Traegerschaft.class, traegerschaft.getId());
			if (found == null) {
				persistEntity(persistence, traegerschaft);
			}
		}
	}

	public static void saveMandantIfNecessary(@Nonnull Persistence persistence, @Nullable Mandant mandant) {
		if (mandant != null) {
			Mandant found = persistence.find(Mandant.class, mandant.getId());
			if (found == null) {
				persistEntity(persistence, mandant);
			}
		}
	}

	private static Kind createDefaultKind(boolean addFachstelle) {
		Kind kind = new Kind();
		kind.setNachname("Kind_Mustermann");
		kind.setVorname("Kind_Max");
		kind.setGeburtsdatum(LocalDate.of(2010, 12, 12));
		kind.setGeschlecht(Geschlecht.WEIBLICH);
		kind.setKinderabzugErstesHalbjahr(Kinderabzug.GANZER_ABZUG);
		kind.setKinderabzugZweitesHalbjahr(Kinderabzug.GANZER_ABZUG);
		if (addFachstelle) {
			createDefaultPensumFachstelle(kind);
		}
		kind.setFamilienErgaenzendeBetreuung(true);
		kind.setSprichtAmtssprache(true);
		kind.setEinschulungTyp(EinschulungTyp.VORSCHULALTER);
		kind.setTimestampErstellt(LocalDateTime.now());
		return kind;
	}

	public static PensumFachstelle createDefaultPensumFachstelle(@Nullable Kind kind) {
		PensumFachstelle pensumFachstelle = new PensumFachstelle();
		pensumFachstelle.setPensum(50);
		pensumFachstelle.setGueltigkeit(new DateRange(LocalDate.now(), Constants.END_OF_TIME));
		pensumFachstelle.setFachstelle(createDefaultFachstelle());
		pensumFachstelle.setIntegrationTyp(IntegrationTyp.SOZIALE_INTEGRATION);
		pensumFachstelle.setKind(Objects.requireNonNullElseGet(kind, () -> createDefaultKind(false)));
		pensumFachstelle.getKind().getPensumFachstelle().add(pensumFachstelle);
		return pensumFachstelle;
	}

	public static PensumAusserordentlicherAnspruch createAusserordentlicherAnspruch(int anspruch) {
		PensumAusserordentlicherAnspruch pensum = new PensumAusserordentlicherAnspruch();
		pensum.setPensum(anspruch);
		pensum.setBegruendung("Test");
		pensum.setGueltigkeit(new DateRange(Constants.START_OF_TIME, Constants.END_OF_TIME));
		return pensum;
	}

	public static KindContainer createDefaultKindContainer() {
		KindContainer kindContainer = new KindContainer();
		Kind defaultKindGS = createDefaultKind(true);
		defaultKindGS.setNachname("GS_Kind");
		kindContainer.setKindGS(defaultKindGS);
		Kind defaultKindJA = createDefaultKind(true);
		defaultKindJA.setNachname("JA_Kind");
		kindContainer.setKindJA(defaultKindJA);
		return kindContainer;
	}

	public static KindContainer createKindContainerWithoutFachstelle() {
		KindContainer kindContainer = new KindContainer();
		Kind defaultKindGS = createDefaultKind(false);
		defaultKindGS.setNachname("GS_Kind");
		kindContainer.setKindGS(defaultKindGS);
		Kind defaultKindJA = createDefaultKind(false);
		defaultKindJA.setNachname("JA_Kind");
		kindContainer.setKindJA(defaultKindJA);
		return kindContainer;
	}

	public static ErwerbspensumContainer createErwerbspensumContainer() {
		ErwerbspensumContainer epCont = new ErwerbspensumContainer();
		epCont.setErwerbspensumGS(createErwerbspensumData());
		Erwerbspensum epKorrigiertJA = createErwerbspensumData();
		epKorrigiertJA.setTaetigkeit(Taetigkeit.RAV);
		epCont.setErwerbspensumJA(epKorrigiertJA);
		return epCont;
	}

	public static ErwerbspensumContainer createErwerbspensum(LocalDate von, LocalDate bis, int pensum) {
		return createErwerbspensum(von, bis, pensum, Taetigkeit.ANGESTELLT);
	}

	public static ErwerbspensumContainer createErwerbspensum(LocalDate von, LocalDate bis, int pensum, @Nonnull Taetigkeit taetigkeit) {
		ErwerbspensumContainer erwerbspensumContainer = new ErwerbspensumContainer();
		Erwerbspensum erwerbspensum = new Erwerbspensum();
		erwerbspensum.setTaetigkeit(taetigkeit);
		erwerbspensum.setPensum(pensum);
		erwerbspensum.setGueltigkeit(new DateRange(von, bis));
		erwerbspensumContainer.setErwerbspensumJA(erwerbspensum);
		return erwerbspensumContainer;
	}

	public static ErwerbspensumContainer createErwerbspensum(int pensum, @Nonnull Taetigkeit taetigkeit) {
		ErwerbspensumContainer erwerbspensumContainer = new ErwerbspensumContainer();
		Erwerbspensum erwerbspensum = new Erwerbspensum();
		erwerbspensum.setTaetigkeit(taetigkeit);
		erwerbspensum.setPensum(pensum);
		erwerbspensum.setGueltigkeit(Constants.DEFAULT_GUELTIGKEIT);
		erwerbspensumContainer.setErwerbspensumJA(erwerbspensum);
		return erwerbspensumContainer;
	}

	public static Erwerbspensum createErwerbspensumData() {
		Erwerbspensum ep = new Erwerbspensum();
		ep.setTaetigkeit(Taetigkeit.ANGESTELLT);
		ep.setPensum(50);
		return ep;
	}

	public static void addUnbezahlterUrlaubToErwerbspensum(Erwerbspensum erwerbspensum, LocalDate von, LocalDate bis) {
		UnbezahlterUrlaub urlaub = new UnbezahlterUrlaub();
		urlaub.setGueltigkeit(new DateRange(von, bis));
		erwerbspensum.setUnbezahlterUrlaub(urlaub);
	}

	public static AnmeldungTagesschule createAnmeldungTagesschuleWithModules(KindContainer kind, Gesuchsperiode gesuchsperiode) {
		AnmeldungTagesschule anmeldung = new AnmeldungTagesschule();
		anmeldung.setInstitutionStammdaten(createInstitutionStammdatenTagesschuleBern(gesuchsperiode));
		anmeldung.setBetreuungsstatus(Betreuungsstatus.SCHULAMT_ANMELDUNG_AUSGELOEST);
		anmeldung.setKind(kind);
		anmeldung.setBelegungTagesschule(createDefaultBelegungTagesschule(true, false));
		kind.getAnmeldungenTagesschule().add(anmeldung);
		return anmeldung;
	}

	public static AnmeldungTagesschule createKesbAnmeldungTagesschuleWithModules(KindContainer kind, Gesuchsperiode gesuchsperiode) {
		AnmeldungTagesschule anmeldung = new AnmeldungTagesschule();
		anmeldung.setInstitutionStammdaten(createInstitutionStammdatenTagesschuleBern(gesuchsperiode));
		anmeldung.setBetreuungsstatus(Betreuungsstatus.SCHULAMT_ANMELDUNG_AUSGELOEST);
		anmeldung.setKind(kind);
		anmeldung.setBelegungTagesschule(createDefaultBelegungTagesschule(true, true));
		kind.getAnmeldungenTagesschule().add(anmeldung);
		return anmeldung;
	}

	public static AnmeldungTagesschule createAnmeldungTagesschule(KindContainer kind, Gesuchsperiode gesuchsperiode) {
		AnmeldungTagesschule anmeldung = new AnmeldungTagesschule();
		anmeldung.setInstitutionStammdaten(createInstitutionStammdatenTagesschuleBern(gesuchsperiode));
		anmeldung.setBetreuungsstatus(Betreuungsstatus.SCHULAMT_ANMELDUNG_AUSGELOEST);
		anmeldung.setKind(kind);
		anmeldung.setBelegungTagesschule(createDefaultBelegungTagesschule(false, false));
		return anmeldung;
	}

	public static AnmeldungFerieninsel createAnmeldungFerieninsel(KindContainer kind) {
		AnmeldungFerieninsel betreuung = new AnmeldungFerieninsel();
		betreuung.setInstitutionStammdaten(createInstitutionStammdatenFerieninselGuarda());
		betreuung.setBetreuungsstatus(Betreuungsstatus.SCHULAMT_ANMELDUNG_AUSGELOEST);
		betreuung.setKind(kind);
		betreuung.setBelegungFerieninsel(createDefaultBelegungFerieninsel());
		return betreuung;
	}

	public static AnmeldungFerieninsel createDefaultAnmeldungFerieninsel() {
		AnmeldungFerieninsel betreuung = new AnmeldungFerieninsel();
		betreuung.setInstitutionStammdaten(createInstitutionStammdatenFerieninselGuarda());
		betreuung.setBetreuungsstatus(Betreuungsstatus.SCHULAMT_ANMELDUNG_AUSGELOEST);
		betreuung.setKind(createDefaultKindContainer());
		betreuung.setBelegungFerieninsel(createDefaultBelegungFerieninsel());
		return betreuung;
	}

	public static Betreuung createDefaultBetreuung() {
		Betreuung betreuung = new Betreuung();
		betreuung.setInstitutionStammdaten(createDefaultInstitutionStammdaten());
		betreuung.setBetreuungsstatus(Betreuungsstatus.BESTAETIGT);
		betreuung.setBetreuungspensumContainers(new TreeSet<>());
		betreuung.setAbwesenheitContainers(new HashSet<>());
		betreuung.setKind(createDefaultKindContainer());
		ErweiterteBetreuungContainer erweitContainer = TestDataUtil.createDefaultErweiterteBetreuungContainer();
		erweitContainer.setBetreuung(betreuung);
		betreuung.setErweiterteBetreuungContainer(erweitContainer);
		return betreuung;
	}

	public static Betreuung createDefaultBetreuungOhneBetreuungPensum(KindContainer kindContainer) {
		Betreuung betreuung = new Betreuung();
		betreuung.setInstitutionStammdaten(createDefaultInstitutionStammdaten());
		betreuung.setBetreuungsstatus(Betreuungsstatus.BESTAETIGT);
		betreuung.setBetreuungspensumContainers(new TreeSet<>());
		betreuung.setAbwesenheitContainers(new HashSet<>());
		betreuung.setKind(kindContainer);
		ErweiterteBetreuungContainer erweitContainer = TestDataUtil.createDefaultErweiterteBetreuungContainer();
		erweitContainer.setBetreuung(betreuung);
		betreuung.setErweiterteBetreuungContainer(erweitContainer);
		return betreuung;
	}

	public static Betreuung createDefaultBetreuung(int betreuungspensum, LocalDate von, LocalDate bis) {
		Betreuung betreuung = new Betreuung();
		betreuung.setInstitutionStammdaten(createDefaultInstitutionStammdaten());
		betreuung.setBetreuungsstatus(Betreuungsstatus.BESTAETIGT);
		betreuung.setBetreuungspensumContainers(new TreeSet<>());
		final BetreuungspensumContainer betPensContainer = TestDataUtil.createBetPensContainer(betreuung);
		final Betreuungspensum pensum = createBetreuungspensum();
		pensum.setPensum(BigDecimal.valueOf(betreuungspensum));
		pensum.setMonatlicheBetreuungskosten(BigDecimal.valueOf(2000));
		pensum.setGueltigkeit(new DateRange(von, bis));
		betPensContainer.setBetreuungspensumJA(pensum);
		betreuung.getBetreuungspensumContainers().add(betPensContainer);
		betreuung.setAbwesenheitContainers(new HashSet<>());
		betreuung.setKind(createDefaultKindContainer());
		ErweiterteBetreuungContainer erweitContainer = TestDataUtil.createDefaultErweiterteBetreuungContainer();
		erweitContainer.setBetreuung(betreuung);
		betreuung.setErweiterteBetreuungContainer(erweitContainer);
		return betreuung;
	}

	public static BelegungTagesschule createDefaultBelegungTagesschule(
			boolean withModulBelegung,
			boolean isKesbPlatzierung) {
		final BelegungTagesschule belegungTagesschule = new BelegungTagesschule();
		belegungTagesschule.setBemerkung("Dies ist eine Bemerkung!");
		belegungTagesschule.setEintrittsdatum(LocalDate.now());
		belegungTagesschule.setKeineKesbPlatzierung(!isKesbPlatzierung);
		if (withModulBelegung) {
			belegungTagesschule.setBelegungTagesschuleModule(new TreeSet<>());
			belegungTagesschule.setAbholungTagesschule(AbholungTagesschule.ALLEINE_NACH_HAUSE);
			Set<ModulTagesschule> modulTagesschuleSet = createDefaultModuleTagesschuleSet(true,
				LocalTime.of(12,0), LocalTime.of(14,0), "Mittag", "Midi");
			modulTagesschuleSet.forEach(
				modulTagesschule -> {
					BelegungTagesschuleModul belegungTagesschuleModul = new BelegungTagesschuleModul();
					belegungTagesschuleModul.setModulTagesschule(modulTagesschule);
					belegungTagesschuleModul.setBelegungTagesschule(belegungTagesschule);
					belegungTagesschuleModul.setIntervall(BelegungTagesschuleModulIntervall.WOECHENTLICH);
					belegungTagesschule.getBelegungTagesschuleModule().add(belegungTagesschuleModul);
				}
			);

			Set<ModulTagesschule> modulTagesschuleSetOhneBetreuung = createDefaultModuleTagesschuleSet(false,
				LocalTime.of(7,0), LocalTime.of(8,0), "Frühmorgens", "Matin");
			modulTagesschuleSetOhneBetreuung.forEach(
				modulTagesschule -> {
					BelegungTagesschuleModul belegungTagesschuleModul = new BelegungTagesschuleModul();
					belegungTagesschuleModul.setModulTagesschule(modulTagesschule);
					belegungTagesschuleModul.setIntervall(BelegungTagesschuleModulIntervall.ALLE_ZWEI_WOCHEN);
					belegungTagesschuleModul.setBelegungTagesschule(belegungTagesschule);
					belegungTagesschule.getBelegungTagesschuleModule().add(belegungTagesschuleModul);
				}
			);
		}
		return belegungTagesschule;
	}

	private static Set<ModulTagesschule> createDefaultModuleTagesschuleSet(boolean wirdPedagogischBetreut, LocalTime von, LocalTime bis, String bezeichnungDeutsch, String bezeichnungFranzösisch){
		ModulTagesschuleGroup modulTagesschuleGroupPedagogischBetreut = new ModulTagesschuleGroup();
		modulTagesschuleGroupPedagogischBetreut.setZeitVon(von);
		modulTagesschuleGroupPedagogischBetreut.setZeitBis(bis);
		modulTagesschuleGroupPedagogischBetreut.setVerpflegungskosten(MathUtil.DEFAULT.from(10));
		modulTagesschuleGroupPedagogischBetreut.setWirdPaedagogischBetreut(wirdPedagogischBetreut);
		TextRessource vormittagText = new TextRessource();
		vormittagText.setTextDeutsch(bezeichnungDeutsch);
		vormittagText.setTextFranzoesisch(bezeichnungFranzösisch);
		modulTagesschuleGroupPedagogischBetreut.setBezeichnung(vormittagText);

		ModulTagesschule modulTagesschuleMonday = new ModulTagesschule();
		modulTagesschuleMonday.setModulTagesschuleGroup(modulTagesschuleGroupPedagogischBetreut);
		modulTagesschuleMonday.setWochentag(DayOfWeek.MONDAY);

		ModulTagesschule modulTagesschuleFriday= new ModulTagesschule();
		modulTagesschuleFriday.setModulTagesschuleGroup(modulTagesschuleGroupPedagogischBetreut);
		modulTagesschuleFriday.setWochentag(DayOfWeek.FRIDAY);

		Set<ModulTagesschule> modulTagesschuleSet = new TreeSet<>();
		modulTagesschuleSet.add(modulTagesschuleMonday);
		modulTagesschuleSet.add(modulTagesschuleFriday);

		modulTagesschuleGroupPedagogischBetreut.setModule(modulTagesschuleSet);

		return modulTagesschuleSet;
	}

	public static BetreuungspensumContainer createBetPensContainer(Betreuung betreuung) {
		BetreuungspensumContainer container = new BetreuungspensumContainer();
		container.setBetreuung(betreuung);
		container.setBetreuungspensumGS(TestDataUtil.createBetreuungspensum());
		container.setBetreuungspensumJA(TestDataUtil.createBetreuungspensum());
		return container;
	}

	private static Betreuungspensum createBetreuungspensum() {
		Betreuungspensum betreuungspensum = new Betreuungspensum();
		betreuungspensum.setPensum(BigDecimal.valueOf(80));
		return betreuungspensum;
	}

	public static Gesuchsperiode createGesuchsperiode1718() {
		return createGesuchsperiodeXXYY(2017, 2018);
	}

	public static Gesuchsperiode createGesuchsperiode1718(Mandant mandant) {
		return createGesuchsperiodeXXYY(2017, 2018, mandant);
	}

	public static Gesuchsperiode createAndPersistGesuchsperiode1718(Persistence persistence) {
		return createGesuchsperiodeXXYYAndPersist(2017, 2018, persistence);
	}

	public static Gesuchsperiode createAndPersistCustomGesuchsperiode(Persistence persistence, int yearFrom, int yearTo) {
		return createGesuchsperiodeXXYYAndPersist(yearFrom, yearTo, persistence);
	}

	public static Gesuchsperiode createGesuchsperiode1617() {
		return createGesuchsperiodeXXYY(2016, 2017);
	}

	public static Gesuchsperiode createGesuchsperiode1617AndPersist(Persistence persistence) {
		return createGesuchsperiodeXXYYAndPersist(2016, 2017, persistence);
	}

	@Nonnull
	public static Gesuchsperiode createGesuchsperiodeXXYYAndPersist(int yearFrom, int yearTo, Persistence persistence) {
		Gesuchsperiode gesuchsperiode = new Gesuchsperiode();
		gesuchsperiode.setStatus(GesuchsperiodeStatus.AKTIV);
		gesuchsperiode.setMandant(getMandantKantonBernAndPersist(persistence));
		gesuchsperiode.setGueltigkeit(new DateRange(LocalDate.of(yearFrom, Month.AUGUST, 1), LocalDate.of(yearTo,
			Month.JULY, 31)));
		return persistence.persist(gesuchsperiode);
	}

	@Nonnull
	public static Gesuchsperiode createGesuchsperiodeXXYY(int yearFrom, int yearTo) {
		Gesuchsperiode gesuchsperiode = new Gesuchsperiode();
		gesuchsperiode.setStatus(GesuchsperiodeStatus.AKTIV);
		gesuchsperiode.setMandant(getMandantKantonBern());
		gesuchsperiode.setGueltigkeit(new DateRange(LocalDate.of(yearFrom, Month.AUGUST, 1), LocalDate.of(yearTo,
			Month.JULY, 31)));
		return gesuchsperiode;
	}

	@Nonnull
	public static Gesuchsperiode createGesuchsperiodeXXYY(int yearFrom, int yearTo, Mandant mandant) {
		Gesuchsperiode gesuchsperiode = new Gesuchsperiode();
		gesuchsperiode.setStatus(GesuchsperiodeStatus.AKTIV);
		gesuchsperiode.setMandant(mandant);
		gesuchsperiode.setGueltigkeit(new DateRange(LocalDate.of(yearFrom, Month.AUGUST, 1), LocalDate.of(yearTo,
			Month.JULY, 31)));
		return gesuchsperiode;
	}

	public static Gesuchsperiode createCustomGesuchsperiode(int firstYear, int secondYear) {
		Gesuchsperiode gesuchsperiode = new Gesuchsperiode();
		gesuchsperiode.setMandant(TestDataUtil.getMandantKantonBern());
		gesuchsperiode.setStatus(GesuchsperiodeStatus.AKTIV);
		gesuchsperiode.setGueltigkeit(new DateRange(
			LocalDate.of(firstYear, Month.AUGUST, 1),
			LocalDate.of(secondYear, Month.JULY, 31)));
		return gesuchsperiode;
	}

	public static Einstellung createDefaultEinstellung(EinstellungKey key, @Nonnull Gesuchsperiode gesuchsperiode) {
		Einstellung instStammdaten = new Einstellung();
		instStammdaten.setKey(key);
		instStammdaten.setValue("1");
		instStammdaten.setGesuchsperiode(gesuchsperiode);
		return instStammdaten;
	}

	public static EinkommensverschlechterungInfoContainer createDefaultEinkommensverschlechterungsInfoContainer(Gesuch gesuch) {
		final EinkommensverschlechterungInfoContainer einkommensverschlechterungInfoContainer =
			new EinkommensverschlechterungInfoContainer();
		einkommensverschlechterungInfoContainer.setEinkommensverschlechterungInfoJA(
			createDefaultEinkommensverschlechterungsInfo());
		einkommensverschlechterungInfoContainer.setGesuch(gesuch);
		gesuch.setEinkommensverschlechterungInfoContainer(einkommensverschlechterungInfoContainer);

		return einkommensverschlechterungInfoContainer;
	}

	public static EinkommensverschlechterungInfo createDefaultEinkommensverschlechterungsInfo() {
		final EinkommensverschlechterungInfo einkommensverschlechterungInfo = new EinkommensverschlechterungInfo();
		einkommensverschlechterungInfo.setEinkommensverschlechterung(true);
		einkommensverschlechterungInfo.setEkvFuerBasisJahrPlus1(true);
		einkommensverschlechterungInfo.setEkvFuerBasisJahrPlus2(false);
		return einkommensverschlechterungInfo;
	}

	public static EinkommensverschlechterungInfoContainer createEinkommensverschlechterungsInfoContainerOhneVerschlechterung(Gesuch gesuch) {
		final EinkommensverschlechterungInfoContainer einkommensverschlechterungInfoContainer =
			new EinkommensverschlechterungInfoContainer();
		einkommensverschlechterungInfoContainer.setEinkommensverschlechterungInfoJA(
			createEinkommensverschlechterungsInfoOhneVerschlechterung());
		einkommensverschlechterungInfoContainer.setGesuch(gesuch);
		gesuch.setEinkommensverschlechterungInfoContainer(einkommensverschlechterungInfoContainer);

		return einkommensverschlechterungInfoContainer;
	}

	public static EinkommensverschlechterungInfo createEinkommensverschlechterungsInfoOhneVerschlechterung() {
		final EinkommensverschlechterungInfo einkommensverschlechterungInfo = new EinkommensverschlechterungInfo();
		einkommensverschlechterungInfo.setEinkommensverschlechterung(false);
		einkommensverschlechterungInfo.setEkvFuerBasisJahrPlus1(false);
		einkommensverschlechterungInfo.setEkvFuerBasisJahrPlus2(false);
		return einkommensverschlechterungInfo;
	}

	public static GesuchstellerContainer createDefaultGesuchstellerWithEinkommensverschlechterung() {
		final GesuchstellerContainer gesuchsteller = createDefaultGesuchstellerContainer();
		gesuchsteller.setEinkommensverschlechterungContainer(createDefaultEinkommensverschlechterungsContainer());
		return gesuchsteller;
	}

	public static Benutzer createDefaultBenutzer() {
		Benutzer user = new Benutzer();
		user.setUsername("jula_" + UUID.randomUUID());
		user.setNachname("Iglesias");
		user.setVorname("Julio");
		user.setEmail("julio.iglesias@example.com");
		user.setMandant(getMandantKantonBern());
		Berechtigung berechtigung = new Berechtigung();
		berechtigung.setRole(UserRole.ADMIN_BG);
		berechtigung.setBenutzer(user);
		user.getBerechtigungen().add(berechtigung);
		return user;
	}

	public static Benutzer createBenutzerSCH() {
		final Benutzer defaultBenutzer = TestDataUtil.createDefaultBenutzer();
		defaultBenutzer.setRole(UserRole.SACHBEARBEITER_TS);
		return defaultBenutzer;
	}

	@SuppressWarnings("ConstantConditions")
	public static Betreuung createGesuchWithBetreuungspensum(boolean zweiGesuchsteller) {
		Gesuch gesuch = new Gesuch();
		gesuch.setGesuchsperiode(TestDataUtil.createGesuchsperiode1718());
		gesuch.setDossier(createDefaultDossier());
		gesuch.setFamiliensituationContainer(createDefaultFamiliensituationContainer());
		if (zweiGesuchsteller) {
			gesuch.extractFamiliensituation().setFamilienstatus(EnumFamilienstatus.VERHEIRATET);
		} else {
			gesuch.extractFamiliensituation().setFamilienstatus(EnumFamilienstatus.ALLEINERZIEHEND);
		}
		gesuch.setGesuchsteller1(TestDataUtil.createDefaultGesuchstellerContainer());
		gesuch.getGesuchsteller1().setFinanzielleSituationContainer(new FinanzielleSituationContainer());
		gesuch.getGesuchsteller1()
			.getFinanzielleSituationContainer()
			.setFinanzielleSituationJA(new FinanzielleSituation());
		if (zweiGesuchsteller) {
			gesuch.setGesuchsteller2(TestDataUtil.createDefaultGesuchstellerContainer());
			gesuch.getGesuchsteller2().setFinanzielleSituationContainer(new FinanzielleSituationContainer());
			gesuch.getGesuchsteller2()
				.getFinanzielleSituationContainer()
				.setFinanzielleSituationJA(new FinanzielleSituation());
		}
		Betreuung betreuung = new Betreuung();
		betreuung.setBetreuungsstatus(Betreuungsstatus.BESTAETIGT);
		KindContainer kindContainer = createDefaultKindContainer();
		kindContainer.getBetreuungen().add(betreuung);
		betreuung.setKind(kindContainer);
		betreuung.getKind().getKindJA().setEinschulungTyp(EinschulungTyp.VORSCHULALTER);
		betreuung.getKind().setGesuch(gesuch);
		gesuch.getKindContainers().add(betreuung.getKind());
		betreuung.setInstitutionStammdaten(createDefaultInstitutionStammdaten());
		betreuung.setErweiterteBetreuungContainer(TestDataUtil.createDefaultErweiterteBetreuungContainer());
		return betreuung;
	}

	@SuppressWarnings("ConstantConditions")
	public static Betreuung createGesuchWithBetreuungspensum(boolean zweiGesuchsteller, Mandant mandant) {
		Gesuch gesuch = new Gesuch();
		gesuch.setGesuchsperiode(TestDataUtil.createGesuchsperiode1718(mandant));
		gesuch.setDossier(createDefaultDossier(mandant));
		gesuch.setFamiliensituationContainer(createDefaultFamiliensituationContainer());
		if (zweiGesuchsteller) {
			gesuch.extractFamiliensituation().setFamilienstatus(EnumFamilienstatus.VERHEIRATET);
		} else {
			gesuch.extractFamiliensituation().setFamilienstatus(EnumFamilienstatus.ALLEINERZIEHEND);
		}
		gesuch.setGesuchsteller1(new GesuchstellerContainer());
		gesuch.getGesuchsteller1().addAdresse(TestDataUtil.createDefaultGesuchstellerAdresseContainer(gesuch.getGesuchsteller1()));
		gesuch.getGesuchsteller1().setFinanzielleSituationContainer(new FinanzielleSituationContainer());
		gesuch.getGesuchsteller1()
			.getFinanzielleSituationContainer()
			.setFinanzielleSituationJA(new FinanzielleSituation());
		if (zweiGesuchsteller) {
			gesuch.setGesuchsteller2(new GesuchstellerContainer());
			gesuch.getGesuchsteller2().setFinanzielleSituationContainer(new FinanzielleSituationContainer());
			gesuch.getGesuchsteller2()
				.getFinanzielleSituationContainer()
				.setFinanzielleSituationJA(new FinanzielleSituation());
		}
		Betreuung betreuung = new Betreuung();
		betreuung.setBetreuungsstatus(Betreuungsstatus.BESTAETIGT);
		KindContainer kindContainer = createDefaultKindContainer();
		kindContainer.getBetreuungen().add(betreuung);
		betreuung.setKind(kindContainer);
		betreuung.getKind().getKindJA().setEinschulungTyp(EinschulungTyp.VORSCHULALTER);
		betreuung.getKind().setGesuch(gesuch);
		gesuch.getKindContainers().add(betreuung.getKind());
		betreuung.setInstitutionStammdaten(createDefaultInstitutionStammdaten(mandant));
		betreuung.setErweiterteBetreuungContainer(TestDataUtil.createDefaultErweiterteBetreuungContainer());
		return betreuung;
	}

	@SuppressWarnings("ConstantConditions")
	public static AnmeldungTagesschule createGesuchWithAnmeldungTagesschule() {
		Gesuch gesuch = new Gesuch();
		gesuch.setGesuchsperiode(TestDataUtil.createGesuchsperiode1718());
		gesuch.setDossier(createDefaultDossier());
		gesuch.setFamiliensituationContainer(createDefaultFamiliensituationContainer());
		gesuch.extractFamiliensituation().setFamilienstatus(EnumFamilienstatus.ALLEINERZIEHEND);
		gesuch.setGesuchsteller1(new GesuchstellerContainer());
		gesuch.getGesuchsteller1().setFinanzielleSituationContainer(new FinanzielleSituationContainer());
		gesuch.getGesuchsteller1()
			.getFinanzielleSituationContainer()
			.setFinanzielleSituationJA(new FinanzielleSituation());
		final KindContainer kindContainer = createDefaultKindContainer();
		kindContainer.setGesuch(gesuch);
		gesuch.getKindContainers().add(kindContainer);
		final AnmeldungTagesschule anmeldung = createAnmeldungTagesschuleWithModules(kindContainer, gesuch.getGesuchsperiode());
		anmeldung.setKind(kindContainer);
		kindContainer.getAnmeldungenTagesschule().add(anmeldung);
		return anmeldung;
	}

	public static void calculateFinanzDaten(Gesuch gesuch,
		AbstractFinanzielleSituationRechner finanzielleSituationRechner) {
		if (gesuch.getGesuchsperiode() == null) {
			gesuch.setGesuchsperiode(createGesuchsperiode1718());
		}
		finanzielleSituationRechner.calculateFinanzDaten(gesuch, BigDecimal.valueOf(20));
	}

	public static Gesuch createTestgesuchDagmar(AbstractFinanzielleSituationRechner finanzielleSituationRechner) {
		Gesuchsperiode gesuchsperiode = TestDataUtil.createGesuchsperiode1718();
		InstitutionStammdatenBuilder institutionStammdatenBuilder = TestDataUtil.getInstititutionStammdatenBuilder(gesuchsperiode);
		Testfall01_WaeltiDagmar testfall = new Testfall01_WaeltiDagmar(gesuchsperiode, institutionStammdatenBuilder);
		testfall.createGesuch(LocalDate.of(1980, Month.MARCH, 25));
		Gesuch gesuch = testfall.fillInGesuch();
		TestDataUtil.calculateFinanzDaten(gesuch, finanzielleSituationRechner);
		gesuch.setGesuchsperiode(TestDataUtil.createGesuchsperiode1718());
		return gesuch;
	}

	public static Gesuch createTestfall11_SchulamtOnly(){
		Gesuchsperiode gesuchsperiode = TestDataUtil.createGesuchsperiode1718();
		InstitutionStammdatenBuilder institutionStammdatenBuilder = TestDataUtil.getInstititutionStammdatenBuilder(gesuchsperiode);
		Testfall11_SchulamtOnly testfall = new Testfall11_SchulamtOnly(gesuchsperiode,
				institutionStammdatenBuilder);
		testfall.createGesuch(LocalDate.of(2017, Month.MARCH, 25));
		Gesuch gesuch = testfall.fillInGesuch();
		gesuch.setGesuchsperiode(gesuchsperiode);
		return gesuch;
	}

	private static InstitutionStammdatenBuilder getInstititutionStammdatenBuilder(Gesuchsperiode gesuchsperiode) {
		return new TestDataInstitutionStammdatenBuilder(gesuchsperiode);
	}

	/**
	 * Hilfsmethode die den Testfall Waelti Dagmar erstellt und speichert
	 */
	public static Gesuch createAndPersistTestfall11_SchulamtOnly(
		@Nonnull Persistence persistence, @Nullable LocalDate eingangsdatum,
		@Nullable AntragStatus status, @Nonnull Gesuchsperiode gesuchsperiode) {
		InstitutionStammdatenBuilder institutionStammdatenBuilder = TestDataUtil.getInstititutionStammdatenBuilder(gesuchsperiode);
		Testfall11_SchulamtOnly testfall = new Testfall11_SchulamtOnly(gesuchsperiode,
				institutionStammdatenBuilder);
		testfall.createGesuch(LocalDate.of(1980, Month.MARCH, 25));
		return persistAllEntities(persistence, eingangsdatum, testfall, status);
	}

	public static Gesuch createTestgesuchYvonneFeuz(AbstractFinanzielleSituationRechner finanzielleSituationRechner) {
		final Gesuchsperiode gesuchsperiode1718 = TestDataUtil.createGesuchsperiode1718();
		InstitutionStammdatenBuilder institutionStammdatenBuilder = TestDataUtil.getInstititutionStammdatenBuilder(gesuchsperiode1718);
		Testfall02_FeutzYvonne testfall =
			new Testfall02_FeutzYvonne(gesuchsperiode1718, institutionStammdatenBuilder);
		testfall.createGesuch(LocalDate.of(1980, Month.MARCH, 25));
		Gesuch gesuch = testfall.fillInGesuch();
		TestDataUtil.calculateFinanzDaten(gesuch, finanzielleSituationRechner);
		gesuch.setGesuchsperiode(gesuchsperiode1718);
		return gesuch;
	}

	public static Gesuch createTestgesuchLauraWalther(@Nonnull Gesuchsperiode gesuchsperiode, AbstractFinanzielleSituationRechner finanzielleSituationRechner) {
		List<InstitutionStammdaten> insttStammdaten = new ArrayList<>();
		insttStammdaten.add(TestDataUtil.createInstitutionStammdatenKitaWeissenstein());
		Testfall04_WaltherLaura testfall =
			new Testfall04_WaltherLaura(gesuchsperiode, getInstititutionStammdatenBuilder(gesuchsperiode));
		testfall.createGesuch(LocalDate.of(1980, Month.MARCH, 25));
		Gesuch gesuch = testfall.fillInGesuch();
		TestDataUtil.calculateFinanzDaten(gesuch, finanzielleSituationRechner);
		gesuch.setGesuchsperiode(gesuchsperiode);
		return gesuch;
	}

	public static void setFinanzielleSituation(Gesuch gesuch, BigDecimal einkommen) {
		Objects.requireNonNull(gesuch.getGesuchsteller1());
		gesuch.getGesuchsteller1().setFinanzielleSituationContainer(new FinanzielleSituationContainer());
		Objects.requireNonNull(gesuch.getGesuchsteller1().getFinanzielleSituationContainer());
		gesuch.getGesuchsteller1()
			.getFinanzielleSituationContainer()
			.setFinanzielleSituationJA(new FinanzielleSituation());
		gesuch.getGesuchsteller1()
			.getFinanzielleSituationContainer()
			.getFinanzielleSituationJA()
			.setNettolohn(einkommen);
	}

	public static void setEinkommensverschlechterung(
		Gesuch gesuch,
		GesuchstellerContainer gesuchsteller,
		BigDecimal einkommen,
		boolean basisJahrPlus1) {
		if (gesuchsteller.getEinkommensverschlechterungContainer() == null) {
			gesuchsteller.setEinkommensverschlechterungContainer(new EinkommensverschlechterungContainer());
		}
		if (gesuch.extractEinkommensverschlechterungInfo() == null) {
			gesuch.setEinkommensverschlechterungInfoContainer(new EinkommensverschlechterungInfoContainer());
			EinkommensverschlechterungInfo einkommensverschlechterungInfo =
				gesuch.extractEinkommensverschlechterungInfo();
			Objects.requireNonNull(einkommensverschlechterungInfo);
			einkommensverschlechterungInfo.setEinkommensverschlechterung(true);
			einkommensverschlechterungInfo.setEkvFuerBasisJahrPlus1(false);
			einkommensverschlechterungInfo.setEkvFuerBasisJahrPlus2(false);
		}
		if (basisJahrPlus1) {
			gesuchsteller.getEinkommensverschlechterungContainer()
				.setEkvJABasisJahrPlus1(new Einkommensverschlechterung());
			gesuchsteller.getEinkommensverschlechterungContainer().getEkvJABasisJahrPlus1().setNettolohn(einkommen);
			EinkommensverschlechterungInfo einkommensverschlechterungInfo =
				gesuch.extractEinkommensverschlechterungInfo();
			Objects.requireNonNull(einkommensverschlechterungInfo);
			einkommensverschlechterungInfo.setEkvFuerBasisJahrPlus1(true);
			einkommensverschlechterungInfo.setEinkommensverschlechterung(true);
		} else {
			gesuchsteller.getEinkommensverschlechterungContainer()
				.setEkvJABasisJahrPlus2(new Einkommensverschlechterung());
			gesuchsteller.getEinkommensverschlechterungContainer().getEkvJABasisJahrPlus2().setNettolohn(einkommen);
			EinkommensverschlechterungInfo einkommensverschlechterungInfo =
				gesuch.extractEinkommensverschlechterungInfo();
			Objects.requireNonNull(einkommensverschlechterungInfo);
			einkommensverschlechterungInfo.setEkvFuerBasisJahrPlus2(true);
			einkommensverschlechterungInfo.setEinkommensverschlechterung(true);
		}
	}

	public static DokumentGrund createDefaultDokumentGrund() {
		DokumentGrund dokumentGrund = new DokumentGrund();
		dokumentGrund.setDokumentGrundTyp(DokumentGrundTyp.EINKOMMENSVERSCHLECHTERUNG);
		dokumentGrund.setTag("tag");
		dokumentGrund.setDokumentTyp(DokumentTyp.JAHRESLOHNAUSWEISE);
		dokumentGrund.setDokumente(new HashSet<>());
		final Dokument dokument = new Dokument();
		dokument.setDokumentGrund(dokumentGrund);
		dokument.setFilename("testdokument");
		dokument.setFilepfad("testpfad/");
		dokument.setFilesize("123456");
		dokument.setTimestampUpload(LocalDateTime.now());
		Objects.requireNonNull(dokumentGrund.getDokumente());
		dokumentGrund.getDokumente().add(dokument);
		return dokumentGrund;
	}

	/**
	 * Hilfsmethode die den Testfall Waelti Dagmar erstellt und speichert
	 */
	private static Gesuch createAndPersistWaeltiDagmarGesuch(
		InstitutionService instService, Persistence persistence,
		@Nullable LocalDate eingangsdatum, @Nullable AntragStatus status) {

		return createAndPersistWaeltiDagmarGesuch(
			instService,
			persistence,
			eingangsdatum,
			status,
			TestDataUtil.createGesuchsperiode1718());
	}

	/**
	 * Hilfsmethode die den Testfall Waelti Dagmar erstellt und speichert
	 */
	public static Gesuch createAndPersistWaeltiDagmarGesuch(
		@Nonnull InstitutionService instService, @Nonnull Persistence persistence, @Nullable LocalDate eingangsdatum,
		@Nullable AntragStatus status, @Nonnull Gesuchsperiode gesuchsperiode) {

		instService.getAllInstitutionen(gesuchsperiode.getMandant());
		List<InstitutionStammdaten> institutionStammdatenList = new ArrayList<>();
		institutionStammdatenList.add(TestDataUtil.createInstitutionStammdatenKitaWeissenstein());
		institutionStammdatenList.add(TestDataUtil.createInstitutionStammdatenKitaBruennen());
		institutionStammdatenList.forEach(institutionStammdaten -> {
			institutionStammdaten.getInstitution().setMandant(gesuchsperiode.getMandant());
		});
		Testfall01_WaeltiDagmar testfall = new Testfall01_WaeltiDagmar(gesuchsperiode, new TestDataInstitutionStammdatenBuilder(gesuchsperiode));

		return persistAllEntities(persistence, eingangsdatum, testfall, status);
	}

	public static Gesuch createAndPersistWaeltiDagmarGesuch(
		InstitutionService instService,
		Persistence persistence,
		@Nullable LocalDate eingangsdatum) {

		return createAndPersistWaeltiDagmarGesuch(instService, persistence, eingangsdatum, null);
	}

	private static void ensureFachstelleAndInstitutionsExist(Persistence persistence, Gesuch gesuch) {
		for (KindContainer kindContainer : gesuch.getKindContainers()) {
			for (Betreuung betreuung : kindContainer.getBetreuungen()) {
				InstitutionStammdaten institutionStammdaten = betreuung.getInstitutionStammdaten();
				saveInstitutionStammdatenIfNecessary(persistence, institutionStammdaten);
				if (!betreuung.getKind().getKindJA().getPensumFachstelle().isEmpty()) {
					for (PensumFachstelle pensumFachstelle : betreuung.getKind().getKindJA().getPensumFachstelle()) {
						persistence.merge(pensumFachstelle.getFachstelle());
					}
				}
			}
			for(AnmeldungTagesschule anmeldungTagesschule: kindContainer.getAnmeldungenTagesschule()){
				InstitutionStammdaten institutionStammdaten = anmeldungTagesschule.getInstitutionStammdaten();
				saveInstitutionStammdatenIfNecessary(persistence, institutionStammdaten);
				InstitutionStammdatenTagesschule institutionStammdatenTagesschule =
					institutionStammdaten.getInstitutionStammdatenTagesschule();
				Objects.requireNonNull(institutionStammdatenTagesschule);
				saveInstitutionStammdatenTagesschule(persistence, institutionStammdatenTagesschule);
			}
			for(AnmeldungFerieninsel anmeldungFerieninsel: kindContainer.getAnmeldungenFerieninsel()){
				InstitutionStammdaten institutionStammdaten = anmeldungFerieninsel.getInstitutionStammdaten();
				saveInstitutionStammdatenIfNecessary(persistence, institutionStammdaten);
			}
		}
	}

	public static Gesuch createAndPersistFeutzYvonneGesuch(
		Persistence persistence,
		@Nullable LocalDate eingangsdatum,
		Gesuchsperiode gesuchsperiode) {

		Collection<InstitutionStammdaten> institutionStammdatenList =
			saveInstitutionsstammdatenForTestfaelle(persistence, gesuchsperiode);
		Testfall02_FeutzYvonne testfall = new Testfall02_FeutzYvonne(gesuchsperiode, new TestDataInstitutionStammdatenBuilder(gesuchsperiode, institutionStammdatenList));
		return persistAllEntities(persistence, eingangsdatum, testfall, null);
	}

	public static Gesuch createAndPersistBeckerNoraGesuch(
		 Persistence persistence, @Nullable LocalDate eingangsdatum,
		@Nullable AntragStatus status, @Nonnull Gesuchsperiode gesuchsperiode) {

		Testfall06_BeckerNora testfall = new Testfall06_BeckerNora(gesuchsperiode, getInstititutionStammdatenBuilder(gesuchsperiode));
		return persistAllEntities(persistence, eingangsdatum, testfall, status);
	}

	public static Gesuch createAndPersistASIV12(
		InstitutionService instService,
		Persistence persistence,
		@Nullable LocalDate eingangsdatum,
		AntragStatus status,
		@Nonnull Gesuchsperiode gesuchsperiode
	) {
		instService.getAllInstitutionen(gesuchsperiode.getMandant());
		List<InstitutionStammdaten> institutionStammdatenList = new ArrayList<>();
		institutionStammdatenList.add(TestDataUtil.createInstitutionStammdatenTagesschuleBern(gesuchsperiode));
		institutionStammdatenList.add(TestDataUtil.createInstitutionStammdatenKitaWeissenstein());
		TestFall12_Mischgesuch testfall = new TestFall12_Mischgesuch(gesuchsperiode,
				new TestDataInstitutionStammdatenBuilder(gesuchsperiode));

		if (status != null) {
			return persistAllEntities(persistence, eingangsdatum, testfall, status);
		}
		return persistAllEntities(persistence, eingangsdatum, testfall, null);
	}

	public static Institution createAndPersistDefaultInstitution(Persistence persistence) {
		Institution inst = createDefaultInstitution();
		TestDataUtil.saveMandantIfNecessary(persistence, inst.getMandant());
		persistence.merge(inst.getTraegerschaft());
		return persistence.merge(inst);
	}

	private static Gesuch persistAllEntities(
		@Nonnull Persistence persistence,
		@Nullable LocalDate eingangsdatum,
		@Nonnull AbstractTestfall testfall,
		@Nullable AntragStatus status) {
		Benutzer verantwortlicher = createAndPersistBenutzer(persistence);
		testfall.createFall(verantwortlicher);
		if (status != null) {
			testfall.createGesuch(eingangsdatum, status);
		} else {
			testfall.createGesuch(eingangsdatum);
		}
		testfall.getDossier().setGemeinde(getGemeindeParis(persistence));
		persistence.persist(testfall.getGesuch().getFall());
		persistence.persist(testfall.getGesuch().getDossier());
		persistEntity(persistence, testfall.getGesuch().getGesuchsperiode());
		persistence.persist(testfall.getGesuch());
		Gesuch gesuch = testfall.fillInGesuch();
		ensureFachstelleAndInstitutionsExist(persistence, gesuch);
		gesuch = persistEntity(persistence, gesuch);
		return gesuch;
	}

	private static <T extends AbstractEntity> T persistEntity(Persistence persistence, @Nonnull T entity) {
		if (entity.isNew()) {
			return persistence.persist(entity);
		}
		return persistence.merge(entity);
	}

	@Nonnull
	private static Benutzer createAndPersistBenutzer(Persistence persistence, Gemeinde persistedGemeinde) {
		Benutzer verantwortlicher = TestDataUtil.createDefaultBenutzer();
		verantwortlicher.getBerechtigungen().iterator().next().getGemeindeList().add(persistedGemeinde);
		TestDataUtil.persistMandantIfNecessary(verantwortlicher.getMandant(), persistence);
		persistence.persist(verantwortlicher);
		return verantwortlicher;
	}

	@Nonnull
	private static Benutzer createAndPersistBenutzer(Persistence persistence) {
		Benutzer verantwortlicher = TestDataUtil.createDefaultBenutzer();
		TestDataUtil.persistMandantIfNecessary(verantwortlicher.getMandant(), persistence);
		verantwortlicher.getBerechtigungen().iterator().next().getGemeindeList().add(getGemeindeParis(persistence));
		persistence.persist(verantwortlicher);
		return verantwortlicher;
	}

	public static void persistEntities(Gesuch gesuch, Persistence persistence) {
		Benutzer verantwortlicher = createAndPersistBenutzer(persistence);

		Gemeinde testGemeinde = getTestGemeinde(persistence);
		gesuch.getDossier().setGemeinde(testGemeinde);

		gesuch.getDossier().setVerantwortlicherBG(verantwortlicher);
		persistence.persist(gesuch.getFall());
		persistence.persist(gesuch.getDossier());
		gesuch.setGesuchsteller1(TestDataUtil.createDefaultGesuchstellerContainer());
		TestDataUtil.saveMandantIfNecessary(persistence, gesuch.getGesuchsperiode().getMandant());
		persistence.persist(gesuch.getGesuchsperiode());

		Set<KindContainer> kindContainers = new TreeSet<>();
		Betreuung betreuung = TestDataUtil.createDefaultBetreuung();
		KindContainer kind = betreuung.getKind();

		Set<Betreuung> betreuungen = new TreeSet<>();
		betreuungen.add(betreuung);
		kind.setBetreuungen(betreuungen);

		Objects.requireNonNull(kind.getKindGS());
		for (PensumFachstelle pensumFachstelle : kind.getKindGS().getPensumFachstelle()) {
			Objects.requireNonNull(pensumFachstelle.getFachstelle());
			TestDataUtil.saveMandantIfNecessary(persistence, pensumFachstelle.getFachstelle().getMandant());
			persistence.persist(pensumFachstelle.getFachstelle());
		}
		for (PensumFachstelle pensumFachstelle : kind.getKindJA().getPensumFachstelle()) {
			Objects.requireNonNull(pensumFachstelle.getFachstelle());
			TestDataUtil.saveMandantIfNecessary(persistence, pensumFachstelle.getFachstelle().getMandant());
			persistence.persist(pensumFachstelle.getFachstelle());
		}
		kind.setGesuch(gesuch);
		kindContainers.add(kind);
		gesuch.setKindContainers(kindContainers);

		saveInstitutionStammdatenIfNecessary(persistence, betreuung.getInstitutionStammdaten());
		TestDataUtil.prepareParameters(gesuch.getGesuchsperiode(), persistence);
		persistence.persist(gesuch);
	}

	public static Gesuch createAndPersistGesuch(
		@Nonnull Persistence persistence,
		@Nullable Gemeinde gemeinde,
		@Nullable AntragStatus status,
		@Nullable Gesuchsperiode gesuchsperiode
	) {
		Gesuch gesuch = TestDataUtil.createDefaultGesuch(status);
		if (gesuchsperiode != null) {
			gesuch.setGesuchsperiode(gesuchsperiode);
		}
		Benutzer benutzer = null;
		if (gemeinde != null) {
			benutzer = createAndPersistBenutzer(persistence, gemeinde);
			gesuch.getDossier().setGemeinde(gemeinde);
		} else {
			benutzer = createAndPersistBenutzer(persistence);
			gesuch.getDossier().setGemeinde(getTestGemeinde(persistence));
		}
		gesuch.getDossier().setVerantwortlicherBG(benutzer);
		persistence.persist(gesuch.getFall());

		persistence.persist(gesuch.getDossier());
		if (gesuch.getGesuchsperiode().isNew()) {
			persistence.persist(gesuch.getGesuchsperiode());
		} else {
			persistence.merge(gesuch.getGesuchsperiode());
		}
		persistence.persist(gesuch);
		GesuchstellerContainer gs = createDefaultGesuchstellerContainer();
		persistence.persist(gs);
		gesuch.setGesuchsteller1(gs);
		persistence.merge(gesuch);
		return gesuch;
	}

	public static Gesuch createAndPersistGesuch(Persistence persistence, Gemeinde gemeinde) {
		Gesuch gesuch = TestDataUtil.createDefaultGesuch();
		saveMandantIfNecessary(persistence, gesuch.getGesuchsperiode().getMandant());
		Benutzer benutzer = createAndPersistBenutzer(persistence, gemeinde);
		gesuch.getDossier().setGemeinde(gemeinde);
		gesuch.getDossier().setVerantwortlicherBG(benutzer);
		persistence.persist(gesuch.getFall());

		persistence.persist(gesuch.getDossier());
		persistence.persist(gesuch.getGesuchsperiode());
		persistence.persist(gesuch);
		GesuchstellerContainer gs = createDefaultGesuchstellerContainer();
		persistence.persist(gs);
		return gesuch;
	}

	public static Gesuch createAndPersistGesuch(Persistence persistence) {
		Gesuch gesuch = TestDataUtil.createDefaultGesuch();
		saveMandantIfNecessary(persistence, gesuch.getGesuchsperiode().getMandant());
		Benutzer benutzer = createAndPersistBenutzer(persistence);
		gesuch.getDossier().setGemeinde(getTestGemeinde(persistence));
		gesuch.getDossier().setVerantwortlicherBG(benutzer);
		gesuch.getGesuchsperiode().setMandant(getMandantKantonBernAndPersist(persistence));
		persistence.persist(gesuch.getFall());

		persistence.persist(gesuch.getDossier());
		persistence.persist(gesuch.getGesuchsperiode());
		persistence.persist(gesuch);
		GesuchstellerContainer gs = createDefaultGesuchstellerContainer();
		persistence.persist(gs);
		return gesuch;
	}

	public static Gesuch createAndPersistGesuch(Persistence persistence, AntragStatus status) {
		Gesuch gesuch = TestDataUtil.createDefaultGesuch(status);
		gesuch.getDossier().setGemeinde(getTestGemeinde(persistence));
		gesuch.getGesuchsperiode().setMandant(getMandantKantonBernAndPersist(persistence));
		persistence.persist(gesuch.getFall());
		persistence.persist(gesuch.getDossier());
		persistence.persist(gesuch.getGesuchsperiode());
		persistence.persist(gesuch);
		return gesuch;
	}

	public static Dossier createAndPersistDossierAndFall(Persistence persistence) {
		final Fall fall = persistence.persist(TestDataUtil.createDefaultFall());
		Dossier dossier = new Dossier();
		dossier.setFall(fall);
		dossier.setGemeinde(getTestGemeinde(persistence));
		dossier = persistence.persist(dossier);
		return dossier;
	}

	public static WizardStep createWizardStepObject(
		Gesuch gesuch,
		WizardStepName wizardStepName,
		WizardStepStatus stepStatus) {
		final WizardStep jaxWizardStep = new WizardStep();
		jaxWizardStep.setGesuch(gesuch);
		jaxWizardStep.setWizardStepName(wizardStepName);
		jaxWizardStep.setWizardStepStatus(stepStatus);
		jaxWizardStep.setBemerkungen("");
		return jaxWizardStep;
	}

	@SuppressWarnings({"PMD.AvoidDuplicateLiterals", "PMD.NcssMethodCount"})
	public static void prepareParameters(Gesuchsperiode gesuchsperiode, Persistence persistence) {
		saveEinstellung(
			PARAM_PAUSCHALABZUG_PRO_PERSON_FAMILIENGROESSE_3,
			PAUSCHALABZUG_PRO_PERSON_FAMILIENGROESSE_3_FUER_TESTS,
			gesuchsperiode,
			persistence);
		saveEinstellung(
			PARAM_PAUSCHALABZUG_PRO_PERSON_FAMILIENGROESSE_4,
			PAUSCHALABZUG_PRO_PERSON_FAMILIENGROESSE_4_FUER_TESTS,
			gesuchsperiode,
			persistence);
		saveEinstellung(
			PARAM_PAUSCHALABZUG_PRO_PERSON_FAMILIENGROESSE_5,
			PAUSCHALABZUG_PRO_PERSON_FAMILIENGROESSE_5_FUER_TESTS,
			gesuchsperiode,
			persistence);
		saveEinstellung(
			PARAM_PAUSCHALABZUG_PRO_PERSON_FAMILIENGROESSE_6,
			PAUSCHALABZUG_PRO_PERSON_FAMILIENGROESSE_6_FUER_TESTS,
			gesuchsperiode,
			persistence);
		saveEinstellung(PARAM_GRENZWERT_EINKOMMENSVERSCHLECHTERUNG, "20", gesuchsperiode, persistence);
		saveEinstellung(PARAM_PENSUM_KITA_MIN, "0", gesuchsperiode, persistence);
		saveEinstellung(PARAM_PENSUM_TAGESELTERN_MIN, "0", gesuchsperiode, persistence);
		saveEinstellung(PARAM_PENSUM_TAGESSCHULE_MIN, "0", gesuchsperiode, persistence);
		saveEinstellung(GEMEINDE_KONTINGENTIERUNG_ENABLED, "false", gesuchsperiode, persistence);
		saveEinstellung(GEMEINDE_BG_BIS_UND_MIT_SCHULSTUFE, EinschulungTyp.VORSCHULALTER.name(), gesuchsperiode, persistence);
		saveEinstellung(ANGEBOT_SCHULSTUFE, BetreuungsangebotTyp.KITA.name(), gesuchsperiode, persistence);
		saveEinstellung(PARAM_MAX_TAGE_ABWESENHEIT, "30", gesuchsperiode, persistence);
		saveEinstellung(MAX_VERGUENSTIGUNG_VORSCHULE_BABY_PRO_TG, "150", gesuchsperiode, persistence);
		saveEinstellung(MAX_VERGUENSTIGUNG_VORSCHULE_KIND_PRO_TG, "100", gesuchsperiode, persistence);
		saveEinstellung(MAX_VERGUENSTIGUNG_KINDERGARTEN_PRO_TG, "75", gesuchsperiode, persistence);
		saveEinstellung(MAX_VERGUENSTIGUNG_VORSCHULE_BABY_PRO_STD, "11.90", gesuchsperiode, persistence);
		saveEinstellung(MAX_VERGUENSTIGUNG_VORSCHULE_KIND_PRO_STD, "8.50", gesuchsperiode, persistence);
		saveEinstellung(MAX_VERGUENSTIGUNG_KINDERGARTEN_PRO_STD, "8.50", gesuchsperiode, persistence);
		saveEinstellung(MAX_VERGUENSTIGUNG_PRIMAR_PRO_STD, "8.50", gesuchsperiode, persistence);
		saveEinstellung(MAX_MASSGEBENDES_EINKOMMEN, "160000", gesuchsperiode, persistence);
		saveEinstellung(MIN_MASSGEBENDES_EINKOMMEN, "43000", gesuchsperiode, persistence);
		saveEinstellung(OEFFNUNGSTAGE_KITA, "240", gesuchsperiode, persistence);
		saveEinstellung(OEFFNUNGSTAGE_TFO, "240", gesuchsperiode, persistence);
		saveEinstellung(OEFFNUNGSSTUNDEN_TFO, "11", gesuchsperiode, persistence);
		saveEinstellung(ZUSCHLAG_BEHINDERUNG_PRO_TG, "50", gesuchsperiode, persistence);
		saveEinstellung(ZUSCHLAG_BEHINDERUNG_PRO_STD, "4.25", gesuchsperiode, persistence);
		saveEinstellung(MIN_VERGUENSTIGUNG_PRO_TG, "7", gesuchsperiode, persistence);
		saveEinstellung(MIN_VERGUENSTIGUNG_PRO_STD, "0.70", gesuchsperiode, persistence);
		saveEinstellung(MIN_ERWERBSPENSUM_NICHT_EINGESCHULT, "20", gesuchsperiode, persistence);
		saveEinstellung(MIN_ERWERBSPENSUM_EINGESCHULT, "40", gesuchsperiode, persistence);
		saveEinstellung(ERWERBSPENSUM_ZUSCHLAG, "20", gesuchsperiode, persistence);
		saveEinstellung(FACHSTELLE_MIN_PENSUM_SOZIALE_INTEGRATION, "20", gesuchsperiode, persistence);
		saveEinstellung(FACHSTELLE_MAX_PENSUM_SOZIALE_INTEGRATION, "60", gesuchsperiode, persistence);
		saveEinstellung(FACHSTELLE_MIN_PENSUM_SPRACHLICHE_INTEGRATION, "40", gesuchsperiode, persistence);
		saveEinstellung(FACHSTELLE_MAX_PENSUM_SPRACHLICHE_INTEGRATION, "40", gesuchsperiode, persistence);
		saveEinstellung(GEMEINDE_FERIENINSEL_ANMELDUNGEN_DATUM_AB,
			Constants.DATE_FORMATTER.format(gesuchsperiode.getGueltigkeit().getGueltigAb()), gesuchsperiode, persistence);
		saveEinstellung(GEMEINDE_TAGESSCHULE_ANMELDUNGEN_DATUM_AB,
			Constants.DATE_FORMATTER.format(gesuchsperiode.getGueltigkeit().getGueltigAb()), gesuchsperiode, persistence);
		saveEinstellung(GEMEINDE_TAGESSCHULE_ERSTER_SCHULTAG,
			Constants.DATE_FORMATTER.format(gesuchsperiode.getGueltigkeit().getGueltigAb()), gesuchsperiode, persistence);
		saveEinstellung(GEMEINDE_TAGESSCHULE_TAGIS_ENABLED, "false", gesuchsperiode, persistence);
		saveEinstellung(GEMEINDE_TAGESSCHULE_ZUSAETZLICHE_ANGABEN_ZUR_ANMELDUNG, "false", gesuchsperiode, persistence);
		saveEinstellung(MAX_TARIF_MIT_PAEDAGOGISCHER_BETREUUNG, "12.24", gesuchsperiode, persistence);
		saveEinstellung(MAX_TARIF_OHNE_PAEDAGOGISCHER_BETREUUNG, "6.11", gesuchsperiode, persistence);
		saveEinstellung(MIN_TARIF, "0.78", gesuchsperiode, persistence);
		saveEinstellung(GEMEINDE_ZUSAETZLICHER_GUTSCHEIN_ENABLED, "false", gesuchsperiode, persistence);
		saveEinstellung(GEMEINDE_ZUSAETZLICHER_GUTSCHEIN_BETRAG_KITA, "0.00", gesuchsperiode, persistence);
		saveEinstellung(GEMEINDE_ZUSAETZLICHER_GUTSCHEIN_BETRAG_TFO, "0.00", gesuchsperiode, persistence);
		saveEinstellung(GEMEINDE_ZUSAETZLICHER_GUTSCHEIN_BIS_UND_MIT_SCHULSTUFE_KITA, EinschulungTyp.VORSCHULALTER.name(), gesuchsperiode, persistence);
		saveEinstellung(GEMEINDE_ZUSAETZLICHER_GUTSCHEIN_BIS_UND_MIT_SCHULSTUFE_TFO, EinschulungTyp.VORSCHULALTER.name(), gesuchsperiode, persistence);
		saveEinstellung(GEMEINDE_ZUSAETZLICHER_BABYBEITRAG_ENABLED, "false", gesuchsperiode, persistence);
		saveEinstellung(GEMEINDE_ZUSAETZLICHER_BABYBEITRAG_BETRAG_KITA, "0.00", gesuchsperiode, persistence);
		saveEinstellung(GEMEINDE_ZUSAETZLICHER_BABYBEITRAG_BETRAG_TFO, "0.00", gesuchsperiode, persistence);
		saveEinstellung(GEMEINDE_ZUSAETZLICHER_ANSPRUCH_FREIWILLIGENARBEIT_ENABLED, "false", gesuchsperiode, persistence);
		saveEinstellung(GEMEINDE_ZUSAETZLICHER_ANSPRUCH_FREIWILLIGENARBEIT_MAXPROZENT, "0", gesuchsperiode, persistence);
		saveEinstellung(GEMEINDE_MAHLZEITENVERGUENSTIGUNG_ENABLED, "true", gesuchsperiode, persistence);
		saveEinstellung(GEMEINDE_MAHLZEITENVERGUENSTIGUNG_EINKOMMENSSTUFE_1_VERGUENSTIGUNG_MAHLZEIT, "6",
			gesuchsperiode, persistence);
		saveEinstellung(GEMEINDE_MAHLZEITENVERGUENSTIGUNG_EINKOMMENSSTUFE_1_MAX_EINKOMMEN, "50000", gesuchsperiode,
			persistence);
		saveEinstellung(GEMEINDE_MAHLZEITENVERGUENSTIGUNG_EINKOMMENSSTUFE_2_VERGUENSTIGUNG_MAHLZEIT, "3",
			gesuchsperiode, persistence);
		saveEinstellung(GEMEINDE_MAHLZEITENVERGUENSTIGUNG_EINKOMMENSSTUFE_2_MAX_EINKOMMEN, "70000", gesuchsperiode,
			persistence);
		saveEinstellung(GEMEINDE_MAHLZEITENVERGUENSTIGUNG_EINKOMMENSSTUFE_3_VERGUENSTIGUNG_MAHLZEIT, "0",
			gesuchsperiode, persistence);
		saveEinstellung(GEMEINDE_MAHLZEITENVERGUENSTIGUNG_FUER_SOZIALHILFEBEZUEGER_ENABLED, "true", gesuchsperiode,
			persistence);
		saveEinstellung(GEMEINDE_SCHNITTSTELLE_KITAX_ENABLED, "false", gesuchsperiode,
			persistence);
		saveEinstellung(GEMEINDE_MIN_ERWERBSPENSUM_NICHT_EINGESCHULT, "20", gesuchsperiode, persistence);
		saveEinstellung(GEMEINDE_MIN_ERWERBSPENSUM_EINGESCHULT, "40", gesuchsperiode, persistence);
		saveEinstellung(GEMEINDE_MAHLZEITENVERGUENSTIGUNG_MINIMALER_ELTERNBEITRAG_MAHLZEIT, "2",
			gesuchsperiode, persistence);
		saveEinstellung(GEMEINDE_PAUSCHALBETRAG_HOHE_EINKOMMENSKLASSEN_AKTIVIERT, "false", gesuchsperiode, persistence);
		saveEinstellung(GEMEINDE_PAUSCHALBETRAG_HOHE_EINKOMMENSKLASSEN_BETRAG_KITA, "0", gesuchsperiode, persistence);
		saveEinstellung(GEMEINDE_PAUSCHALBETRAG_HOHE_EINKOMMENSKLASSEN_BETRAG_TFO, "0", gesuchsperiode, persistence);
		saveEinstellung(GEMEINDE_PAUSCHALBETRAG_HOHE_EINKOMMENSKLASSEN_BETRAG_TFO_AB_PRIMARSCHULE, "0", gesuchsperiode, persistence);
		saveEinstellung(GEMEINDE_PAUSCHALBETRAG_HOHE_EINKOMMENSKLASSEN_MAX_MASSGEBENDEN_EINKOMMEN_FUER_BERECHNUNG, "160000", gesuchsperiode, persistence);
		saveEinstellung(GEMEINDE_KEIN_GUTSCHEIN_FUER_SOZIALHILFE_EMPFAENGER, "false", gesuchsperiode, persistence);
		saveEinstellung(LATS_LOHNNORMKOSTEN, "10.39",	gesuchsperiode, persistence);
		saveEinstellung(LATS_LOHNNORMKOSTEN_LESS_THAN_50, "5.2",	gesuchsperiode, persistence);
		String stichtag = gesuchsperiode.getGueltigkeit().getGueltigAb().getYear() + "-09-15";
		saveEinstellung(LATS_STICHTAG, stichtag, gesuchsperiode, persistence);
		saveEinstellung(EINGEWOEHNUNG_TYP, EingewoehnungTyp.KEINE.toString(), gesuchsperiode, persistence);
		saveEinstellung(FKJV_MAX_DIFFERENZ_BESCHAEFTIGUNGSPENSUM, "100", gesuchsperiode, persistence);
		saveEinstellung(FKJV_SOZIALE_INTEGRATION_BIS_SCHULSTUFE, "VORSCHULALTER", gesuchsperiode, persistence);
		saveEinstellung(SPRACHLICHE_INTEGRATION_BIS_SCHULSTUFE, "VORSCHULALTER", gesuchsperiode, persistence);
		saveEinstellung(FKJV_PAUSCHALE_BEI_ANSPRUCH, "false", gesuchsperiode, persistence);
		saveEinstellung(FKJV_EINKOMMENSVERSCHLECHTERUNG_BIS_CHF, "null", gesuchsperiode, persistence);
		saveEinstellung(FKJV_PAUSCHALE_RUECKWIRKEND, "false", gesuchsperiode, persistence);
		saveEinstellung(ANSPRUCH_MONATSWEISE, "false", gesuchsperiode, persistence);
		saveEinstellung(SCHNITTSTELLE_STEUERN_AKTIV, "false", gesuchsperiode, persistence);
		saveEinstellung(FERIENBETREUUNG_CHF_PAUSCHALBETRAG, "30",gesuchsperiode, persistence);
		saveEinstellung(FERIENBETREUUNG_CHF_PAUSCHALBETRAG_SONDERSCHUELER, "60",gesuchsperiode, persistence);
		saveEinstellung(FKJV_FAMILIENSITUATION_NEU, "false", gesuchsperiode, persistence);
		saveEinstellung(MINIMALDAUER_KONKUBINAT, "5", gesuchsperiode, persistence);
		saveEinstellung(FINANZIELLE_SITUATION_TYP, "BERN", gesuchsperiode, persistence);
		saveEinstellung(KITAPLUS_ZUSCHLAG_AKTIVIERT, "false", gesuchsperiode, persistence);
		saveEinstellung(GEMEINDESPEZIFISCHE_BG_KONFIGURATIONEN, "false", gesuchsperiode, persistence);
		saveEinstellung(ABHAENGIGKEIT_ANSPRUCH_BESCHAEFTIGUNGPENSUM,
			AnspruchBeschaeftigungAbhaengigkeitTyp.ABHAENGING.name(), gesuchsperiode, persistence);
		saveEinstellung(KINDERABZUG_TYP, "ASIV", gesuchsperiode, persistence);
		saveEinstellung(FKJV_MAX_PENSUM_AUSSERORDENTLICHER_ANSPRUCH, "100", gesuchsperiode, persistence);
		saveEinstellung(AUSSERORDENTLICHER_ANSPRUCH_RULE, "ASIV", gesuchsperiode, persistence);
		saveEinstellung(KESB_PLATZIERUNG_DEAKTIVIEREN, "false", gesuchsperiode, persistence);
		saveEinstellung(BESONDERE_BEDUERFNISSE_LUZERN, "false", gesuchsperiode, persistence);
		saveEinstellung(GESCHWISTERNBONUS_AKTIVIERT, "false", gesuchsperiode, persistence);
		saveEinstellung(DAUER_BABYTARIF, "12", gesuchsperiode, persistence);
		saveEinstellung(FKJV_TEXTE, "false", gesuchsperiode, persistence);
		saveEinstellung(DIPLOMATENSTATUS_DEAKTIVIERT, "false", gesuchsperiode, persistence);
		saveEinstellung(ZEMIS_DISABLED, "false", gesuchsperiode, persistence);
		saveEinstellung(SPRACHE_AMTSPRACHE_DISABLED, "false", gesuchsperiode, persistence);
		saveEinstellung(FREIGABE_QUITTUNG_EINLESEN_REQUIRED, "true", gesuchsperiode, persistence);
		saveEinstellung(UNBEZAHLTER_URLAUB_AKTIV, "true", gesuchsperiode, persistence);
		saveEinstellung(FACHSTELLEN_TYP, "BERN", gesuchsperiode, persistence);
		saveEinstellung(AUSWEIS_NACHWEIS_REQUIRED, "false", gesuchsperiode, persistence);
		saveEinstellung(PENSUM_ANZEIGE_TYP, "ZEITEINHEIT_UND_PROZENT", gesuchsperiode, persistence);
		saveEinstellung(VERFUEGUNG_EINGESCHRIEBEN_VERSENDEN_AKTIVIERT, "true", gesuchsperiode, persistence);
		saveEinstellung(ABWESENHEIT_AKTIV, "true", gesuchsperiode, persistence);
		saveEinstellung(BEGRUENDUNG_MUTATION_AKTIVIERT, "false", gesuchsperiode, persistence);
		saveEinstellung(VERFUEGUNG_EXPORT_ENABLED, "false", gesuchsperiode, persistence);
		saveEinstellung(ZAHLUNGSANGABEN_ANTRAGSTELLER_REQUIRED, "false", gesuchsperiode, persistence);
		saveEinstellung(VERANLAGUNG_MIN_UNTERSCHIED_MASSGEBENDESEINK, "0", gesuchsperiode,persistence);
		saveEinstellung(ANSPRUCH_AB_X_MONATEN, "0", gesuchsperiode,persistence);
		saveEinstellung(KITA_STUNDEN_PRO_TAG, "10", gesuchsperiode, persistence);
		saveEinstellung(ZUSATZLICHE_FELDER_ERSATZEINKOMMEN, "false", gesuchsperiode, persistence);
		saveEinstellung(SPRACHFOERDERUNG_BESTAETIGEN, "false", gesuchsperiode, persistence);
		saveEinstellung(GESUCH_BEENDEN_BEI_TAUSCH_GS2, "false", gesuchsperiode, persistence);
	}

	public static void saveEinstellung(
		EinstellungKey key,
		String value,
		Gesuchsperiode gesuchsperiode,
		Persistence persistence
	) {
		Einstellung einstellung = new Einstellung(key, value, gesuchsperiode);
		persistence.persist(einstellung);
	}

	public static void prepareEinstellung(
		EinstellungKey key,
		String value,
		Gesuchsperiode gesuchsperiode,
		Map<EinstellungKey, Einstellung> einstellungMap
	) {
		Einstellung einstellung = new Einstellung(key, value, gesuchsperiode);
		einstellungMap.put(key, einstellung);
	}

	public static void createStammdatenDefaultVerantwortlicheParis(
		@Nonnull Persistence persistence,
		@Nonnull Benutzer verantwortlicherTS,
		@Nonnull Benutzer verantwortlicherBG) {

		GemeindeStammdaten stammdatenParis = getGemeindeStammdatenParis(persistence);
		stammdatenParis.setDefaultBenutzer(verantwortlicherBG);
		stammdatenParis.setDefaultBenutzerBG(verantwortlicherBG);
		stammdatenParis.setDefaultBenutzerTS(verantwortlicherTS);
		persistence.merge(stammdatenParis);
	}

	public static GemeindeStammdaten createGemeindeWithStammdaten() {
		return createGemeindeStammdaten(createGemeindeParis());
	}

	public static GemeindeStammdaten createGemeindeStammdaten(@Nonnull Gemeinde gemeinde) {
		GemeindeStammdaten stammdaten = new GemeindeStammdaten();
		stammdaten.setAdresse(createDefaultAdresse());
		stammdaten.setGemeinde(gemeinde);
		stammdaten.setKorrespondenzsprache(KorrespondenzSpracheTyp.DE);
		stammdaten.setMail("info@bern.ch");
		stammdaten.setTelefon("031 123 12 12");
		stammdaten.setWebseite("www.bern.ch");
		stammdaten.setIban(new IBAN("CH93 0076 2011 6238 5295 7"));
		stammdaten.setBic("BIC123");
		stammdaten.setKontoinhaber("Inhaber");
		stammdaten.setGemeindeStammdatenKorrespondenz(createGemeindeStammdatenKorrespondenz());
		return stammdaten;
	}

	public static GemeindeStammdatenKorrespondenz createGemeindeStammdatenKorrespondenz() {
		GemeindeStammdatenKorrespondenz config = new GemeindeStammdatenKorrespondenz();
		config.setSenderAddressSpacingLeft(20);
		config.setSenderAddressSpacingTop(47);
		config.setReceiverAddressSpacingLeft(123);
		config.setReceiverAddressSpacingTop(47);
		config.setLogoContent(new byte[0]);
		config.setLogoWidth(null);
		config.setLogoSpacingLeft(20);
		config.setLogoSpacingTop(15);
		return config;
	}

	public static GemeindeStammdaten createGemeindeStammdaten(@Nonnull Gemeinde gemeinde, @Nonnull Persistence persistence) {
		GemeindeStammdaten gemeindeStammdaten = createGemeindeStammdaten(gemeinde);
		if (gemeinde.isNew()) {
			persistence.persist(gemeinde);
		}
		return persistence.merge(gemeindeStammdaten);
	}

	public static Benutzer createBenutzerWithDefaultGemeinde(
		UserRole role, String userName,
		@Nullable Traegerschaft traegerschaft,
		@Nullable Institution institution,
		@Nonnull Mandant mandant,
		@Nonnull Persistence persistence,
		@Nullable String name,
		@Nullable String vorname) {
		Benutzer benutzer = createBenutzer(role, userName, traegerschaft, institution, mandant, name, vorname);
		if (role.isRoleGemeindeabhaengig()) {
			benutzer.getBerechtigungen().iterator().next().getGemeindeList().add(getGemeindeParis(persistence));
		}
		return benutzer;
	}

	public static Benutzer createBenutzerWithDefaultGemeinde(
		UserRole role, String userName,
		@Nullable Traegerschaft traegerschaft,
		@Nullable Institution institution,
		@Nonnull Mandant mandant,
		@Nonnull Persistence persistence,
		@Nullable String name,
		@Nullable String vorname,
		@Nonnull String userId) {
		Benutzer benutzer = createBenutzer(role, userName, traegerschaft, institution, mandant, name, vorname);
		benutzer.setId(userId);
		if (role.isRoleGemeindeabhaengig()) {
			benutzer.getBerechtigungen().iterator().next().getGemeindeList().add(getGemeindeParis(persistence));
		}
		return persistence.persist(benutzer);
	}

	public static Benutzer createBenutzer(
		UserRole role,
		String userName,
		@Nullable Traegerschaft traegerschaft,
		@Nullable Institution institution,
		@Nonnull Mandant mandant,
		@Nullable String nachname,
		@Nullable String vorname
	) {
		final Benutzer benutzer = new Benutzer();
		benutzer.setUsername(userName);
		benutzer.setNachname(nachname != null ? nachname : Constants.ANONYMOUS_USER_USERNAME);
		benutzer.setVorname(vorname != null ? vorname : Constants.ANONYMOUS_USER_USERNAME);
		benutzer.setEmail("e@e");
		Berechtigung berechtigung = new Berechtigung();
		berechtigung.setTraegerschaft(traegerschaft);
		berechtigung.setInstitution(institution);
		berechtigung.setRole(role);
		berechtigung.setBenutzer(benutzer);
		benutzer.getBerechtigungen().add(berechtigung);
		benutzer.setMandant(mandant);
		return benutzer;
	}

	public static Benutzer createAndPersistJABenutzer(Persistence persistence, Mandant mandant) {
		final Benutzer benutzer =
			TestDataUtil.createBenutzerWithDefaultGemeinde(UserRole.SACHBEARBEITER_BG, UUID.randomUUID().toString(),
				null, null, mandant, persistence, null, null);
		persistence.persist(benutzer);
		return benutzer;
	}

	public static Benutzer createAndPersistTraegerschaftBenutzer(Persistence persistence) {
		final Mandant mandant = TestDataUtil.getMandantKantonBernAndPersist(persistence);
		final Traegerschaft traegerschaft = TestDataUtil.createDefaultTraegerschaft(mandant);
		persistence.persist(traegerschaft);
		final Benutzer benutzer = TestDataUtil.createBenutzerWithDefaultGemeinde(
			UserRole.SACHBEARBEITER_TRAEGERSCHAFT,
			UUID.randomUUID().toString(),
			traegerschaft,
			null,
			mandant,
			persistence, null, null);
		persistence.persist(benutzer);
		return benutzer;
	}

	public static GeneratedDokument createGeneratedDokument(final Gesuch gesuch) {
		final GeneratedDokument dokument = new GeneratedDokument();
		dokument.setGesuch(gesuch);
		dokument.setTyp(GeneratedDokumentTyp.VERFUEGUNG);
		dokument.setFilepfad("pfad/to/document/doc.pdf");
		dokument.setFilename("name.pdf");
		dokument.setFilesize("32");
		return dokument;
	}

	public static Benutzer createDummySuperAdmin(
		Persistence persistence,
		@Nullable Mandant mandant,
		@Nullable String name,
		@Nullable String vorname
	) {
		//machmal brauchen wir einen dummy admin in der DB
		if (mandant == null) {
			mandant = TestDataUtil.getMandantKantonBernAndPersist(persistence);
		}
		final Benutzer benutzer = TestDataUtil.createBenutzerWithDefaultGemeinde(UserRole.SUPER_ADMIN, "superadmin",
			null, null, mandant, persistence, name, vorname);
		persistence.merge(benutzer);
		return benutzer;
	}

	public static Benutzer createDummySuperAdmin(Persistence persistence, Mandant mandant, String name, String vorname, String id) {
		//machmal brauchen wir einen dummy admin in der DB
		Benutzer potentiallyExisting = persistence.find(Benutzer.class, id);
		if (potentiallyExisting != null) {
			return potentiallyExisting;
		}
		if (mandant == null) {
			mandant = TestDataUtil.getMandantKantonBernAndPersist(persistence);
		}
		final Benutzer benutzer = TestDataUtil.createBenutzerWithDefaultGemeinde(UserRole.SUPER_ADMIN, "superadmin",
				null, null, mandant, persistence, name, vorname, id);
		persistence.merge(benutzer);
		return benutzer;
	}



	public static AntragTableFilterDTO createAntragTableFilterDTO() {
		AntragTableFilterDTO filterDTO = new AntragTableFilterDTO();
		filterDTO.setSort(new SortDTO());
		filterDTO.setSearch(new AntragSearchDTO());
		filterDTO.setPagination(new PaginationDTO());
		filterDTO.getPagination().setStart(0);
		filterDTO.getPagination().setNumber(10);
		return filterDTO;
	}

	public static Mahnung createMahnung(MahnungTyp typ, Gesuch gesuch) {
		return createMahnung(typ, gesuch, LocalDate.now().plusWeeks(2), 3);
	}

	public static Mahnung createMahnung(MahnungTyp typ, Gesuch gesuch, LocalDate firstAblauf, int numberOfDocuments) {
		Mahnung mahnung = new Mahnung();
		mahnung.setMahnungTyp(typ);
		mahnung.setTimestampAbgeschlossen(null);
		List<String> bemerkungen = new ArrayList<>();
		for (int i = 0; i < numberOfDocuments; i++) {
			bemerkungen.add("Test Dokument " + (i + 1));
		}
		mahnung.setBemerkungen(String.join("\n", bemerkungen));
		mahnung.setDatumFristablauf(firstAblauf);
		mahnung.setTimestampErstellt(LocalDateTime.now());
		mahnung.setUserMutiert("Hans Muster");
		mahnung.setGesuch(gesuch);
		return mahnung;
	}

	public static AbwesenheitContainer createShortAbwesenheitContainer(final Gesuchsperiode gesuchsperiode) {
		final AbwesenheitContainer abwesenheitContainer = new AbwesenheitContainer();
		abwesenheitContainer.setAbwesenheitJA(createShortAbwesenheit(gesuchsperiode));
		return abwesenheitContainer;
	}

	private static Abwesenheit createShortAbwesenheit(final Gesuchsperiode gesuchsperiode) {
		final Abwesenheit abwesenheit = new Abwesenheit();
		abwesenheit.setGueltigkeit(new DateRange(
			gesuchsperiode.getGueltigkeit().getGueltigAb().plusMonths(1),
			gesuchsperiode.getGueltigkeit().getGueltigAb().plusMonths(1).plusDays(ABWESENHEIT_DAYS_LIMIT - 1)));
		return abwesenheit;
	}

	public static AbwesenheitContainer createLongAbwesenheitContainer(final Gesuchsperiode gesuchsperiode) {
		final AbwesenheitContainer abwesenheitContainer = new AbwesenheitContainer();
		abwesenheitContainer.setAbwesenheitJA(createLongAbwesenheit(gesuchsperiode));
		return abwesenheitContainer;
	}

	private static Abwesenheit createLongAbwesenheit(final Gesuchsperiode gesuchsperiode) {
		final Abwesenheit abwesenheit = new Abwesenheit();
		abwesenheit.setGueltigkeit(new DateRange(
			gesuchsperiode.getGueltigkeit().getGueltigAb().plusMonths(1),
			gesuchsperiode.getGueltigkeit().getGueltigAb().plusMonths(1).plusDays(ABWESENHEIT_DAYS_LIMIT)));
		return abwesenheit;
	}

	public static Gesuch createGesuch(Dossier dossier, Gesuchsperiode periodeToUpdate, AntragStatus status) {
		Gesuch gesuch = new Gesuch();
		gesuch.setDossier(dossier);
		gesuch.setGesuchsperiode(periodeToUpdate);
		gesuch.setStatus(status);
		return gesuch;
	}

	public static Gesuch createMutation(Dossier dossier, Gesuchsperiode periodeToUpdate, AntragStatus status, int laufnummer) {
		Gesuch gesuch = new Gesuch();
		gesuch.setDossier(dossier);
		gesuch.setGesuchsperiode(periodeToUpdate);
		gesuch.setStatus(status);
		gesuch.setTyp(AntragTyp.MUTATION);
		gesuch.setLaufnummer(laufnummer);
		return gesuch;
	}

	@SuppressWarnings("MagicNumber")
	public static Betreuungsmitteilung createBetreuungmitteilung(
		Dossier dossier, Benutzer empfaenger, MitteilungTeilnehmerTyp empfaengerTyp,
		Benutzer sender, MitteilungTeilnehmerTyp senderTyp) {
		final Betreuungsmitteilung mitteilung = new Betreuungsmitteilung();
		fillOutMitteilung(dossier, empfaenger, empfaengerTyp, sender, senderTyp, mitteilung);

		Set<BetreuungsmitteilungPensum> betPensen = new HashSet<>();

		BetreuungsmitteilungPensum pensum = new BetreuungsmitteilungPensum();
		pensum.setBetreuungsmitteilung(mitteilung);
		pensum.setGueltigkeit(new DateRange(Constants.START_OF_TIME, Constants.END_OF_TIME));
		pensum.setPensum(MathUtil.DEFAULT.from(30));

		betPensen.add(pensum);
		mitteilung.setBetreuungspensen(betPensen);

		return mitteilung;
	}

	public static Mitteilung createMitteilung(
		Dossier dossier, Benutzer empfaenger, MitteilungTeilnehmerTyp empfaengerTyp,
		Benutzer sender, MitteilungTeilnehmerTyp senderTyp) {
		Mitteilung mitteilung = new Mitteilung();
		fillOutMitteilung(dossier, empfaenger, empfaengerTyp, sender, senderTyp, mitteilung);
		return mitteilung;
	}

	public static Mitteilung createMitteilungForInstitution(
		Dossier dossier, Benutzer empfaenger, MitteilungTeilnehmerTyp empfaengerTyp,
		Benutzer sender, MitteilungTeilnehmerTyp senderTyp, Institution institution) {
		Mitteilung mitteilung = new Mitteilung();
		fillOutMitteilung(dossier, empfaenger, empfaengerTyp, sender, senderTyp, mitteilung);
		mitteilung.setInstitution(institution);
		return mitteilung;
	}

	private static void fillOutMitteilung(
		Dossier dossier,
		Benutzer empfaenger,
		MitteilungTeilnehmerTyp empfaengerTyp,
		Benutzer sender,
		MitteilungTeilnehmerTyp
			senderTyp,
		Mitteilung mitteilung) {
		mitteilung.setDossier(dossier);
		mitteilung.setEmpfaenger(empfaenger);
		mitteilung.setSender(sender);
		mitteilung.setMitteilungStatus(MitteilungStatus.NEU);
		mitteilung.setSubject("Subject");
		mitteilung.setEmpfaengerTyp(empfaengerTyp);
		mitteilung.setSenderTyp(senderTyp);
		mitteilung.setMessage("Message");
	}

	public static MitteilungTableFilterDTO createMitteilungTableFilterDTO() {
		MitteilungTableFilterDTO filterDTO = new MitteilungTableFilterDTO();
		filterDTO.setSearch(new MitteilungSearchDTO());
		filterDTO.getSearch().getPredicateObject().setMessageTypes(new MessageTypes[]{MessageTypes.MITTEILUNG, MessageTypes.BETREUUNGSMITTEILUNG});
		return filterDTO;
	}

	public static Betreuung persistBetreuung(
		BetreuungService betreuungService,
		Persistence persistence,
		@Nullable Gesuchsperiode gesuchsperiode
	) {
		Betreuung betreuung = TestDataUtil.createDefaultBetreuung();
		for (BetreuungspensumContainer container : betreuung.getBetreuungspensumContainers()) {
			persistence.persist(container);
		}
		for (AbwesenheitContainer abwesenheit : betreuung.getAbwesenheitContainers()) {
			persistence.persist(abwesenheit);
		}
		saveInstitutionStammdatenIfNecessary(persistence, betreuung.getInstitutionStammdaten());
		Objects.requireNonNull(betreuung.getKind().getKindGS());

		for (PensumFachstelle pensumFachstelle : betreuung.getKind().getKindGS().getPensumFachstelle()) {
			Objects.requireNonNull(pensumFachstelle.getFachstelle());
			TestDataUtil.saveMandantIfNecessary(persistence, pensumFachstelle.getFachstelle().getMandant());
			persistence.persist(pensumFachstelle.getFachstelle());
		}
		for (PensumFachstelle pensumFachstelle : betreuung.getKind().getKindJA().getPensumFachstelle()) {
			Objects.requireNonNull(pensumFachstelle.getFachstelle());
			TestDataUtil.saveMandantIfNecessary(persistence, pensumFachstelle.getFachstelle().getMandant());
			persistence.persist(pensumFachstelle.getFachstelle());
		}

		Gesuch gesuch = TestDataUtil.createAndPersistGesuch(persistence, null, null, gesuchsperiode);
		betreuung.getKind().setGesuch(gesuch);
		persistence.persist(betreuung.getKind());

		betreuung.setBetreuungsstatus(Betreuungsstatus.WARTEN);
		final Betreuung savedBetreuung = betreuungService.saveBetreuung(betreuung, false, null);

		return savedBetreuung;

	}

	/**
	 * Verfuegt das uebergebene Gesuch. Dies muss in Status IN_BEARBEITUNG_JA uebergeben werden.
	 */
	public static Gesuch gesuchVerfuegen(@Nonnull Gesuch gesuch, @Nonnull GesuchService gesuchService) {
		gesuch.setStatus(AntragStatus.GEPRUEFT);
		final Gesuch gesuchToVerfuegt = gesuchService.updateGesuch(gesuch, true, null);
		gesuchToVerfuegt.setStatus(AntragStatus.VERFUEGEN);
		final Gesuch verfuegenGesuch = gesuchService.updateGesuch(gesuchToVerfuegt, true, null);
		verfuegenGesuch.setStatus(AntragStatus.VERFUEGT);
		return gesuchService.updateGesuch(verfuegenGesuch, true, null);
	}

	public static Gesuch persistNewCompleteGesuchInStatus(
		@Nonnull AntragStatus status, @Nonnull Persistence persistence,
		@Nonnull GesuchService gesuchService, @Nonnull Gesuchsperiode gesuchsperiode) {

		final Gesuch gesuch = TestDataUtil.createDefaultGesuch();
		gesuch.getDossier().setGemeinde(getTestGemeinde(persistence));
		gesuch.setStatus(status);
		gesuch.setGesuchsperiode(persistEntity(persistence, gesuchsperiode));
		gesuch.getDossier().setFall(persistence.persist(gesuch.getDossier().getFall()));
		gesuch.setDossier(persistence.persist(gesuch.getDossier()));
		GesuchstellerContainer gesuchsteller1 = TestDataUtil.createDefaultGesuchstellerContainer();
		gesuch.setGesuchsteller1(gesuchsteller1);
		Objects.requireNonNull(gesuch.getGesuchsteller1());
		gesuch.getGesuchsteller1().setFinanzielleSituationContainer(TestDataUtil.createFinanzielleSituationContainer());
		Objects.requireNonNull(gesuch.getGesuchsteller1().getFinanzielleSituationContainer());
		gesuch.getGesuchsteller1()
			.getFinanzielleSituationContainer()
			.setFinanzielleSituationJA(TestDataUtil.createDefaultFinanzielleSituation());
		TestDataUtil.createEinkommensverschlechterungsInfoContainerOhneVerschlechterung(gesuch);

		Gesuch createdGesuch = gesuchService.createGesuch(gesuch);

		//Kind und kindContainer und Betreuung sonst gewisse Status sind nicht erlaubt
		Betreuung betreuung = TestDataUtil.createDefaultBetreuung();

		saveInstitutionStammdatenIfNecessary(persistence, betreuung.getInstitutionStammdaten());
		Objects.requireNonNull(betreuung.getKind().getKindGS());
		//
		betreuung.getKind().getKindGS().getPensumFachstelle().clear();
		betreuung.getKind().getKindJA().getPensumFachstelle().clear();
	/*	for (PensumFachstelle pensumFachstelle : betreuung.getKind().getKindGS().getPensumFachstelle()) {
			Objects.requireNonNull(pensumFachstelle.getFachstelle());
			TestDataUtil.saveMandantIfNecessary(persistence, pensumFachstelle.getFachstelle().getMandant());
			persistence.persist(pensumFachstelle.getFachstelle());
			pensumFachstelle.setKind(betreuung.getKind().getKindGS());
		}
		for (PensumFachstelle pensumFachstelle : betreuung.getKind().getKindJA().getPensumFachstelle()) {
			Objects.requireNonNull(pensumFachstelle.getFachstelle());
			TestDataUtil.saveMandantIfNecessary(persistence, pensumFachstelle.getFachstelle().getMandant());
			persistence.persist(pensumFachstelle.getFachstelle());
			pensumFachstelle.setKind(betreuung.getKind().getKindJA());
		}*/

		KindContainer kindContainer = betreuung.getKind();
		kindContainer.getBetreuungen().add(betreuung);
		kindContainer.setGesuch(gesuch);

		persistence.persist(kindContainer);

		gesuch.setKindContainers(new HashSet<>());
		gesuch.getKindContainers().add(kindContainer);

		// Achtung: im createGesuch wird die Eingangsart und der Status aufgrund des eingeloggten Benutzers nochmals neu berechnet!
		createdGesuch.setStatus(status);
		return persistence.merge(createdGesuch);
	}

	public static Gesuch persistNewGesuchInStatus(
		@Nonnull AntragStatus status, @Nonnull Persistence persistence,
		@Nonnull GesuchService gesuchService, @Nonnull Gesuchsperiode gesuchsperiode) {

		final Gesuch gesuch = TestDataUtil.createDefaultGesuch();
		gesuch.getDossier().setGemeinde(getTestGemeinde(persistence));
		gesuch.setStatus(status);
		gesuch.setGesuchsperiode(persistEntity(persistence, gesuchsperiode));
		gesuch.getDossier().setFall(persistence.persist(gesuch.getDossier().getFall()));
		gesuch.setDossier(persistence.persist(gesuch.getDossier()));
		GesuchstellerContainer gesuchsteller1 = TestDataUtil.createDefaultGesuchstellerContainer();
		gesuch.setGesuchsteller1(gesuchsteller1);
		Objects.requireNonNull(gesuch.getGesuchsteller1());
		gesuch.getGesuchsteller1().setFinanzielleSituationContainer(TestDataUtil.createFinanzielleSituationContainer());
		Objects.requireNonNull(gesuch.getGesuchsteller1().getFinanzielleSituationContainer());
		gesuch.getGesuchsteller1()
			.getFinanzielleSituationContainer()
			.setFinanzielleSituationJA(TestDataUtil.createDefaultFinanzielleSituation());
		Gesuch createdGesuch = gesuchService.createGesuch(gesuch);
		// Achtung: im createGesuch wird die Eingangsart und der Status aufgrund des eingeloggten Benutzers nochmals neu berechnet!
		createdGesuch.setStatus(status);
		return persistence.merge(createdGesuch);
	}

	public static Gesuch persistNewGesuchInStatus(
		@Nonnull AntragStatus status, @Nonnull Persistence persistence,
		@Nonnull GesuchService gesuchService) {

		final Gesuch gesuch = TestDataUtil.createDefaultGesuch();
		gesuch.getDossier().setGemeinde(getTestGemeinde(persistence));
		gesuch.setStatus(status);
		gesuch.setGesuchsperiode(persistence.persist(gesuch.getGesuchsperiode()));
		gesuch.getDossier().setFall(persistence.persist(gesuch.getDossier().getFall()));
		gesuch.setDossier(persistence.persist(gesuch.getDossier()));
		GesuchstellerContainer gesuchsteller1 = TestDataUtil.createDefaultGesuchstellerContainer();
		gesuch.setGesuchsteller1(gesuchsteller1);
		Objects.requireNonNull(gesuch.getGesuchsteller1());
		gesuch.getGesuchsteller1().setFinanzielleSituationContainer(TestDataUtil.createFinanzielleSituationContainer());
		Objects.requireNonNull(gesuch.getGesuchsteller1().getFinanzielleSituationContainer());
		gesuch.getGesuchsteller1()
			.getFinanzielleSituationContainer()
			.setFinanzielleSituationJA(TestDataUtil.createDefaultFinanzielleSituation());
		Gesuch createdGesuch = gesuchService.createGesuch(gesuch);
		// Achtung: im createGesuch wird die Eingangsart und der Status aufgrund des eingeloggten Benutzers nochmals neu berechnet!
		createdGesuch.setStatus(status);
		return persistence.merge(createdGesuch);
	}

	@Nonnull
	public static AntragTableFilterDTO createDefaultAntragTableFilterDTO() {
		AntragTableFilterDTO antragSearch = new AntragTableFilterDTO();
		PaginationDTO pagination = new PaginationDTO();
		pagination.setNumber(20);
		pagination.setStart(0);
		pagination.setNumberOfPages(1);
		antragSearch.setPagination(pagination);
		AntragSearchDTO searchDTO = new AntragSearchDTO();
		AntragPredicateObjectDTO predicateObj = new AntragPredicateObjectDTO();
		searchDTO.setPredicateObject(predicateObj);
		antragSearch.setSearch(searchDTO);
		return antragSearch;
	}

	public static BelegungFerieninsel createDefaultBelegungFerieninsel() {
		BelegungFerieninsel belegungFerieninsel = new BelegungFerieninsel();
		belegungFerieninsel.setFerienname(Ferienname.SOMMERFERIEN);
		belegungFerieninsel.setTage(new ArrayList<>());
		belegungFerieninsel.getTage().add(createBelegungFerieninselTag(LocalDate.now().plusMonths(3)));
		belegungFerieninsel.setTageMorgenmodul(new ArrayList<>());
		belegungFerieninsel.getTageMorgenmodul().add(createBelegungFerieninselTag(LocalDate.now().plusMonths(3)));
		return belegungFerieninsel;
	}

	public static BelegungFerieninselTag createBelegungFerieninselTag(LocalDate date) {
		BelegungFerieninselTag tag = new BelegungFerieninselTag();
		tag.setTag(date);
		return tag;
	}

	public static VerfuegungZeitabschnitt createDefaultZeitabschnitt(Verfuegung verfuegung) {
		VerfuegungZeitabschnitt zeitabschnitt = new VerfuegungZeitabschnitt();
		zeitabschnitt.setVerfuegung(verfuegung);
		zeitabschnitt.setBetreuungspensumProzentForAsivAndGemeinde(BigDecimal.valueOf(10));
		zeitabschnitt.setAnspruchspensumProzentForAsivAndGemeinde(50);
		zeitabschnitt.setEinkommensjahrForAsivAndGemeinde(PERIODE_JAHR_1);
		zeitabschnitt.setZuSpaetEingereichtForAsivAndGemeinde(false);
		return zeitabschnitt;
	}

	public static ErweiterteBetreuungContainer createDefaultErweiterteBetreuungContainer() {
		ErweiterteBetreuungContainer erwBetContainer = new ErweiterteBetreuungContainer();
		ErweiterteBetreuung erwBet = new ErweiterteBetreuung();
		erwBet.setErweiterteBeduerfnisse(false);
		erwBet.setKeineKesbPlatzierung(true);
		erwBet.setBetreuungInGemeinde(true);
		erwBetContainer.setErweiterteBetreuungJA(erwBet);
		return erwBetContainer;
	}

	/**
	 * This method must be called before creating a Zahlung for the a new mutation. This is needed because to check abschnitte that have alreadz
	 * been paied we take the las Betreuung that has been paid using the TimestampVerfuegt form Gesuch and the datumGeneriert of the zahlung
	 */
	public static Gesuch correctTimestampVerfuegt(Gesuch gesuch, LocalDateTime date, Persistence persistence) {
		gesuch.setTimestampVerfuegt(date.minusDays(1));
		return persistence.merge(gesuch);
	}

	public static void initVorgaengerVerfuegungenWithNULL(@Nonnull Gesuch gesuch) {
		gesuch.getKindContainers().stream()
			.flatMap(k -> k.getBetreuungen().stream())
			.forEach(b -> b.initVorgaengerVerfuegungen(null, null));
	}

	@Nonnull
	public static KitaxUebergangsloesungParameter geKitaxUebergangsloesungParameter() {
		Collection<KitaxUebergangsloesungInstitutionOeffnungszeiten> collection = new ArrayList<>();
		collection.add(createKitaxOeffnungszeiten("Kita Brünnen"));
		collection.add(createKitaxOeffnungszeiten("Kita Weissenstein"));
		collection.add(createKitaxOeffnungszeiten("Kita Aaregg"));
		collection.add(createKitaxOeffnungszeiten("Testinstitution"));
		// Fuer Tests gehen wir im Allgemeinen davon aus, dass Bern (Paris) bereits in der Vergangenheit zu ASIV gewechselt hat
		KitaxUebergangsloesungParameter parameter = new KitaxUebergangsloesungParameter(
			LocalDate.of(2000, Month.JANUARY, 1),
			true,
			collection);
		return parameter;
	}

	@Nonnull
	private static KitaxUebergangsloesungInstitutionOeffnungszeiten createKitaxOeffnungszeiten(@Nonnull String name) {
		KitaxUebergangsloesungInstitutionOeffnungszeiten oeffnungszeiten = new KitaxUebergangsloesungInstitutionOeffnungszeiten();
		oeffnungszeiten.setOeffnungstage(MathUtil.DEFAULT.from(240));
		oeffnungszeiten.setOeffnungsstunden(MathUtil.DEFAULT.from(11.5));
		oeffnungszeiten.setNameKibon(name);
		oeffnungszeiten.setNameKitax(name);
		return oeffnungszeiten;
	}

	public static LastenausgleichTagesschuleAngabenGemeindeContainer createLastenausgleichTagesschuleAngabenGemeindeContainer(
		@Nonnull Gesuchsperiode gesuchsperiode,
		@Nonnull Gemeinde gemeinde
	) {
		LastenausgleichTagesschuleAngabenGemeindeContainer cnt = new LastenausgleichTagesschuleAngabenGemeindeContainer();
		LastenausgleichTagesschuleAngabenGemeinde angabenDeklaration = new LastenausgleichTagesschuleAngabenGemeinde();
		angabenDeklaration.setStatus(LastenausgleichTagesschuleAngabenGemeindeFormularStatus.IN_BEARBEITUNG);
		cnt.setStatus(LastenausgleichTagesschuleAngabenGemeindeStatus.NEU);
		cnt.setGesuchsperiode(gesuchsperiode);
		cnt.setAngabenDeklaration(angabenDeklaration);
		cnt.setGemeinde(gemeinde);
		return cnt;
	}

	public static LastenausgleichTagesschuleAngabenGemeinde createLastenausgleichTagesschuleAngabenGemeinde() {
		LastenausgleichTagesschuleAngabenGemeinde angabenGemeinde = new LastenausgleichTagesschuleAngabenGemeinde();
		// A: Allgemeine Angaben
		angabenGemeinde.setBedarfBeiElternAbgeklaert(true);
		angabenGemeinde.setAngebotFuerFerienbetreuungVorhanden(true);
		angabenGemeinde.setAngebotVerfuegbarFuerAlleSchulstufen(true);
		angabenGemeinde.setBegruendungWennAngebotNichtVerfuegbarFuerAlleSchulstufen(null);
		// B: Abrechnung
		angabenGemeinde.setGeleisteteBetreuungsstundenOhneBesondereBeduerfnisse(BigDecimal.TEN);
		angabenGemeinde.setGeleisteteBetreuungsstundenBesondereBeduerfnisse(BigDecimal.TEN);
		angabenGemeinde.setDavonStundenZuNormlohnMehrAls50ProzentAusgebildete(BigDecimal.TEN);
		angabenGemeinde.setDavonStundenZuNormlohnWenigerAls50ProzentAusgebildete(BigDecimal.TEN);
		angabenGemeinde.setEinnahmenElterngebuehren(BigDecimal.TEN);
		angabenGemeinde.setGeleisteteBetreuungsstundenBesondereVolksschulangebot(BigDecimal.ZERO);
		// C: Kostenbeteiligung Gemeinde
		angabenGemeinde.setGesamtKostenTagesschule(BigDecimal.TEN);
		angabenGemeinde.setEinnnahmenVerpflegung(BigDecimal.TEN);
		angabenGemeinde.setEinnahmenSubventionenDritter(BigDecimal.TEN);
		// D: Angaben zu weiteren Kosten und Ertraegen
		angabenGemeinde.setBemerkungenWeitereKostenUndErtraege("Meine Bemerkungen");
		// E: Kontrollfragen
		angabenGemeinde.setBetreuungsstundenDokumentiertUndUeberprueft(true);
		angabenGemeinde.setElterngebuehrenGemaessVerordnungBerechnet(true);
		angabenGemeinde.setEinkommenElternBelegt(true);
		angabenGemeinde.setMaximalTarif(true);
		angabenGemeinde.setMindestens50ProzentBetreuungszeitDurchAusgebildetesPersonal(true);
		angabenGemeinde.setAusbildungenMitarbeitendeBelegt(true);
		// Bemerkungen
		angabenGemeinde.setBemerkungen(null);
		angabenGemeinde.setStatus(LastenausgleichTagesschuleAngabenGemeindeFormularStatus.ABGESCHLOSSEN);
		return angabenGemeinde;
	}

	public static LastenausgleichTagesschuleAngabenInstitution createLastenausgleichTagesschuleAngabenInstitution() {
		LastenausgleichTagesschuleAngabenInstitution angabenInstitution = new LastenausgleichTagesschuleAngabenInstitution();
		// A: Informationen zur Tagesschule
		angabenInstitution.setLehrbetrieb(true);
		// B: Quantitative Angaben
		angabenInstitution.setAnzahlEingeschriebeneKinder(BigDecimal.TEN);
		angabenInstitution.setAnzahlEingeschriebeneKinderKindergarten(BigDecimal.TEN);
		angabenInstitution.setAnzahlEingeschriebeneKinderSekundarstufe(BigDecimal.TEN);
		angabenInstitution.setAnzahlEingeschriebeneKinderPrimarstufe(BigDecimal.TEN);
		angabenInstitution.setAnzahlEingeschriebeneKinderMitBesonderenBeduerfnissen(BigDecimal.TEN);
		angabenInstitution.setAnzahlEingeschriebeneKinderVolksschulangebot(BigDecimal.ZERO);
		angabenInstitution.setAnzahlEingeschriebeneKinderBasisstufe(BigDecimal.ZERO);
		angabenInstitution.setDurchschnittKinderProTagFruehbetreuung(BigDecimal.TEN);
		angabenInstitution.setDurchschnittKinderProTagMittag(BigDecimal.TEN);
		angabenInstitution.setDurchschnittKinderProTagNachmittag1(BigDecimal.TEN);
		angabenInstitution.setDurchschnittKinderProTagNachmittag2(BigDecimal.TEN);
		// C: Qualitative Vorgaben der Tagesschuleverordnung
		angabenInstitution.setSchuleAufBasisOrganisatorischesKonzept(true);
		angabenInstitution.setSchuleAufBasisPaedagogischesKonzept(true);
		angabenInstitution.setRaeumlicheVoraussetzungenEingehalten(true);
		angabenInstitution.setBetreuungsverhaeltnisEingehalten(true);
		angabenInstitution.setErnaehrungsGrundsaetzeEingehalten(true);
		// Bemerkungen
		angabenInstitution.setBemerkungen(null);
		angabenInstitution.setOeffnungszeiten("[]");
		return angabenInstitution;
	}

	public static Fall addSozialdienstToFall(Persistence persistence, FallService fallService, Fall fall) {
		SozialdienstStammdaten sozialdienstStammdaten = createDefaultSozialdienstStammdaten(fall);
		persistence.persist(sozialdienstStammdaten);
		return fallService.saveFall(fall);
	}

	public static SozialdienstStammdaten createDefaultSozialdienstStammdaten(@Nonnull Fall fall) {
		SozialdienstFall sozialdienstFall = new SozialdienstFall();
		sozialdienstFall.setName("SozialName");
		sozialdienstFall.setVorname("SozialVorname");
		sozialdienstFall.setGeburtsdatum(LocalDate.now());
		sozialdienstFall.setStatus(SozialdienstFallStatus.AKTIV);
		Adresse adresse = new Adresse();
		adresse.setGemeinde("Biel");
		adresse.setLand(Land.CH);
		adresse.setOrt("Biel");
		adresse.setPlz("2500");
		adresse.setStrasse("Bielerseestrasse");
		sozialdienstFall.setAdresse(adresse);
		Sozialdienst sozialdienst = new Sozialdienst();
		sozialdienst.setName("Sozialdienst Biel");
		sozialdienst.setStatus(SozialdienstStatus.AKTIV);
		assert fall.getMandant() != null;
		sozialdienst.setMandant(fall.getMandant());
		sozialdienstFall.setSozialdienst(sozialdienst);
		fall.setSozialdienstFall(sozialdienstFall);
		SozialdienstStammdaten sozialdienstStammdaten = new SozialdienstStammdaten();
		sozialdienstStammdaten.setSozialdienst(sozialdienst);
		sozialdienstStammdaten.setAdresse(adresse);
		sozialdienstStammdaten.setMail("sozialmail@mailbucket.dvbern.ch");
		sozialdienstStammdaten.setTelefon("078 818 82 84");
		sozialdienstStammdaten.setWebseite("");
		return sozialdienstStammdaten;
	}

	public static void persistFachstelle(@Nonnull Persistence persistence, @Nonnull Fachstelle fachstelle) {
		saveMandantIfNecessary(persistence, fachstelle.getMandant());
		persistence.persist(fachstelle);
	}

	public static void addSecondGesuchsteller(@Nonnull Gesuch gesuch) {
		final Familiensituation familiensituation = gesuch.extractFamiliensituation();
		Objects.requireNonNull(familiensituation);
		familiensituation.setFamilienstatus(EnumFamilienstatus.VERHEIRATET);
		final GesuchstellerContainer gs2 = TestDataUtil.createDefaultGesuchstellerContainer();
		final FinanzielleSituationContainer finsitGs2 = new FinanzielleSituationContainer();
		gs2.setFinanzielleSituationContainer(finsitGs2);
		finsitGs2.setFinanzielleSituationJA(new FinanzielleSituation());
		gesuch.setGesuchsteller2(gs2);
	}

	public static Mandant createMandant(MandantIdentifier mandantIdentifier) {
		Mandant mandant = new Mandant();
		mandant.setMandantIdentifier(mandantIdentifier);
		mandant.setName("Mandant");
		mandant.setActivated(true);
		return mandant;
	}
}
