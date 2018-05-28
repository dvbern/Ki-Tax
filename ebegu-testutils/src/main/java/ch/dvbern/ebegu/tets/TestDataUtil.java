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

package ch.dvbern.ebegu.tets;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import ch.dvbern.ebegu.dto.suchfilter.smarttable.AntragPredicateObjectDTO;
import ch.dvbern.ebegu.dto.suchfilter.smarttable.AntragSearchDTO;
import ch.dvbern.ebegu.dto.suchfilter.smarttable.AntragTableFilterDTO;
import ch.dvbern.ebegu.dto.suchfilter.smarttable.MitteilungSearchDTO;
import ch.dvbern.ebegu.dto.suchfilter.smarttable.MitteilungTableFilterDTO;
import ch.dvbern.ebegu.dto.suchfilter.smarttable.PaginationDTO;
import ch.dvbern.ebegu.dto.suchfilter.smarttable.SortDTO;
import ch.dvbern.ebegu.entities.AbstractFinanzielleSituation;
import ch.dvbern.ebegu.entities.Abwesenheit;
import ch.dvbern.ebegu.entities.AbwesenheitContainer;
import ch.dvbern.ebegu.entities.Adresse;
import ch.dvbern.ebegu.entities.AdresseTyp;
import ch.dvbern.ebegu.entities.ApplicationProperty;
import ch.dvbern.ebegu.entities.BelegungFerieninsel;
import ch.dvbern.ebegu.entities.BelegungFerieninselTag;
import ch.dvbern.ebegu.entities.BelegungTagesschule;
import ch.dvbern.ebegu.entities.Benutzer;
import ch.dvbern.ebegu.entities.Berechtigung;
import ch.dvbern.ebegu.entities.Betreuung;
import ch.dvbern.ebegu.entities.Betreuungsmitteilung;
import ch.dvbern.ebegu.entities.BetreuungsmitteilungPensum;
import ch.dvbern.ebegu.entities.Betreuungspensum;
import ch.dvbern.ebegu.entities.BetreuungspensumContainer;
import ch.dvbern.ebegu.entities.Dokument;
import ch.dvbern.ebegu.entities.DokumentGrund;
import ch.dvbern.ebegu.entities.EbeguParameter;
import ch.dvbern.ebegu.entities.Einkommensverschlechterung;
import ch.dvbern.ebegu.entities.EinkommensverschlechterungContainer;
import ch.dvbern.ebegu.entities.EinkommensverschlechterungInfo;
import ch.dvbern.ebegu.entities.EinkommensverschlechterungInfoContainer;
import ch.dvbern.ebegu.entities.Erwerbspensum;
import ch.dvbern.ebegu.entities.ErwerbspensumContainer;
import ch.dvbern.ebegu.entities.Fachstelle;
import ch.dvbern.ebegu.entities.Fall;
import ch.dvbern.ebegu.entities.Familiensituation;
import ch.dvbern.ebegu.entities.FamiliensituationContainer;
import ch.dvbern.ebegu.entities.FerieninselStammdaten;
import ch.dvbern.ebegu.entities.FerieninselZeitraum;
import ch.dvbern.ebegu.entities.FinanzielleSituation;
import ch.dvbern.ebegu.entities.FinanzielleSituationContainer;
import ch.dvbern.ebegu.entities.GeneratedDokument;
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.entities.Gesuchsperiode;
import ch.dvbern.ebegu.entities.Gesuchsteller;
import ch.dvbern.ebegu.entities.GesuchstellerAdresse;
import ch.dvbern.ebegu.entities.GesuchstellerAdresseContainer;
import ch.dvbern.ebegu.entities.GesuchstellerContainer;
import ch.dvbern.ebegu.entities.Institution;
import ch.dvbern.ebegu.entities.InstitutionStammdaten;
import ch.dvbern.ebegu.entities.Kind;
import ch.dvbern.ebegu.entities.KindContainer;
import ch.dvbern.ebegu.entities.Mahnung;
import ch.dvbern.ebegu.entities.Mandant;
import ch.dvbern.ebegu.entities.Mitteilung;
import ch.dvbern.ebegu.entities.PensumFachstelle;
import ch.dvbern.ebegu.entities.Traegerschaft;
import ch.dvbern.ebegu.entities.Verfuegung;
import ch.dvbern.ebegu.entities.VerfuegungZeitabschnitt;
import ch.dvbern.ebegu.entities.WizardStep;
import ch.dvbern.ebegu.enums.AntragStatus;
import ch.dvbern.ebegu.enums.ApplicationPropertyKey;
import ch.dvbern.ebegu.enums.BetreuungsangebotTyp;
import ch.dvbern.ebegu.enums.Betreuungsstatus;
import ch.dvbern.ebegu.enums.DokumentGrundTyp;
import ch.dvbern.ebegu.enums.DokumentTyp;
import ch.dvbern.ebegu.enums.EbeguParameterKey;
import ch.dvbern.ebegu.enums.Eingangsart;
import ch.dvbern.ebegu.enums.EnumFamilienstatus;
import ch.dvbern.ebegu.enums.EnumGesuchstellerKardinalitaet;
import ch.dvbern.ebegu.enums.Ferienname;
import ch.dvbern.ebegu.enums.GeneratedDokumentTyp;
import ch.dvbern.ebegu.enums.Geschlecht;
import ch.dvbern.ebegu.enums.GesuchsperiodeStatus;
import ch.dvbern.ebegu.enums.Kinderabzug;
import ch.dvbern.ebegu.enums.MahnungTyp;
import ch.dvbern.ebegu.enums.MitteilungStatus;
import ch.dvbern.ebegu.enums.MitteilungTeilnehmerTyp;
import ch.dvbern.ebegu.enums.Taetigkeit;
import ch.dvbern.ebegu.enums.UserRole;
import ch.dvbern.ebegu.enums.WizardStepName;
import ch.dvbern.ebegu.enums.WizardStepStatus;
import ch.dvbern.ebegu.enums.Zuschlagsgrund;
import ch.dvbern.ebegu.services.BetreuungService;
import ch.dvbern.ebegu.services.EbeguParameterService;
import ch.dvbern.ebegu.services.GesuchService;
import ch.dvbern.ebegu.services.InstitutionService;
import ch.dvbern.ebegu.testfaelle.AbstractTestfall;
import ch.dvbern.ebegu.testfaelle.Testfall01_WaeltiDagmar;
import ch.dvbern.ebegu.testfaelle.Testfall02_FeutzYvonne;
import ch.dvbern.ebegu.testfaelle.Testfall06_BeckerNora;
import ch.dvbern.ebegu.types.DateRange;
import ch.dvbern.ebegu.util.Constants;
import ch.dvbern.ebegu.util.FinanzielleSituationRechner;
import ch.dvbern.lib.cdipersistence.Persistence;
import ch.dvbern.oss.lib.beanvalidation.embeddables.IBAN;

import static ch.dvbern.ebegu.enums.EbeguParameterKey.PARAM_ABGELTUNG_PRO_TAG_KANTON;
import static ch.dvbern.ebegu.enums.EbeguParameterKey.PARAM_ANZAHL_TAGE_KANTON;
import static ch.dvbern.ebegu.enums.EbeguParameterKey.PARAM_ANZAL_TAGE_MAX_KITA;
import static ch.dvbern.ebegu.enums.EbeguParameterKey.PARAM_BABY_ALTER_IN_MONATEN;
import static ch.dvbern.ebegu.enums.EbeguParameterKey.PARAM_BABY_FAKTOR;
import static ch.dvbern.ebegu.enums.EbeguParameterKey.PARAM_FIXBETRAG_STADT_PRO_TAG_KITA;
import static ch.dvbern.ebegu.enums.EbeguParameterKey.PARAM_GRENZWERT_EINKOMMENSVERSCHLECHTERUNG;
import static ch.dvbern.ebegu.enums.EbeguParameterKey.PARAM_KOSTEN_PRO_STUNDE_MAX;
import static ch.dvbern.ebegu.enums.EbeguParameterKey.PARAM_KOSTEN_PRO_STUNDE_MAX_TAGESELTERN;
import static ch.dvbern.ebegu.enums.EbeguParameterKey.PARAM_KOSTEN_PRO_STUNDE_MIN;
import static ch.dvbern.ebegu.enums.EbeguParameterKey.PARAM_MASSGEBENDES_EINKOMMEN_MAX;
import static ch.dvbern.ebegu.enums.EbeguParameterKey.PARAM_MASSGEBENDES_EINKOMMEN_MIN;
import static ch.dvbern.ebegu.enums.EbeguParameterKey.PARAM_MAXIMALER_ZUSCHLAG_ERWERBSPENSUM;
import static ch.dvbern.ebegu.enums.EbeguParameterKey.PARAM_PAUSCHALABZUG_PRO_PERSON_FAMILIENGROESSE_3;
import static ch.dvbern.ebegu.enums.EbeguParameterKey.PARAM_PAUSCHALABZUG_PRO_PERSON_FAMILIENGROESSE_4;
import static ch.dvbern.ebegu.enums.EbeguParameterKey.PARAM_PAUSCHALABZUG_PRO_PERSON_FAMILIENGROESSE_5;
import static ch.dvbern.ebegu.enums.EbeguParameterKey.PARAM_PAUSCHALABZUG_PRO_PERSON_FAMILIENGROESSE_6;
import static ch.dvbern.ebegu.enums.EbeguParameterKey.PARAM_STUNDEN_PRO_TAG_MAX_KITA;
import static ch.dvbern.ebegu.enums.EbeguParameterKey.PARAM_STUNDEN_PRO_TAG_TAGI;

/**
 * comments homa
 */
@SuppressWarnings("PMD.NcssTypeCount")
public final class TestDataUtil {

	private static final String iban = "CH39 0900 0000 3066 3817 2";

	public static final int PERIODE_JAHR_0 = 2016;
	public static final int PERIODE_JAHR_1 = 2017;
	public static final int PERIODE_JAHR_2 = 2018;

	public static final LocalDate START_PERIODE = LocalDate.of(PERIODE_JAHR_1, Month.AUGUST, 1);
	public static final LocalDate ENDE_PERIODE = LocalDate.of(PERIODE_JAHR_2, Month.JULY, 31);

	public static final LocalDate STICHTAG_EKV_1 = LocalDate.of(PERIODE_JAHR_1, Month.SEPTEMBER, 1);
	public static final LocalDate STICHTAG_EKV_1_GUELTIG = STICHTAG_EKV_1.plusMonths(1);
	public static final LocalDate STICHTAG_EKV_2 = LocalDate.of(PERIODE_JAHR_2, Month.APRIL, 1);
	public static final LocalDate STICHTAG_EKV_2_GUELTIG = STICHTAG_EKV_2.plusMonths(1);

	private TestDataUtil() {
	}

	public static GesuchstellerAdresseContainer createDefaultGesuchstellerAdresseContainer(GesuchstellerContainer gsContainer) {
		final GesuchstellerAdresseContainer gsAdressCont = new GesuchstellerAdresseContainer();
		gsAdressCont.setGesuchstellerContainer(gsContainer);
		gsAdressCont.setGesuchstellerAdresseJA(createDefaultGesuchstellerAdresse());
		return gsAdressCont;
	}

	public static GesuchstellerAdresse createDefaultGesuchstellerAdresse() {
		GesuchstellerAdresse gesuchstellerAdresse = new GesuchstellerAdresse();
		gesuchstellerAdresse.setStrasse("Nussbaumstrasse");
		gesuchstellerAdresse.setHausnummer("21");
		gesuchstellerAdresse.setZusatzzeile("c/o Uwe Untermieter");
		gesuchstellerAdresse.setPlz("3014");
		gesuchstellerAdresse.setOrt("Bern");
		gesuchstellerAdresse.setGueltigkeit(new DateRange(LocalDate.now(), Constants.END_OF_TIME));
		gesuchstellerAdresse.setAdresseTyp(AdresseTyp.WOHNADRESSE);
		return gesuchstellerAdresse;
	}

	public static Adresse createDefaultAdresse() {
		Adresse adresse = new Adresse();
		adresse.setStrasse("Nussbaumstrasse");
		adresse.setHausnummer("21");
		adresse.setZusatzzeile("c/o Uwe Untermieter");
		adresse.setPlz("3014");
		adresse.setOrt("Bern");
		adresse.setGueltigkeit(new DateRange(LocalDate.now(), Constants.END_OF_TIME));
		return adresse;
	}

	public static GesuchstellerContainer createDefaultGesuchstellerContainer(Gesuch gesuch) {
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
		EinkommensverschlechterungContainer einkommensverschlechterungContainer = new EinkommensverschlechterungContainer();

		einkommensverschlechterungContainer.setEkvGSBasisJahrPlus1(createDefaultEinkommensverschlechterung());

		final Einkommensverschlechterung ekvGSBasisJahrPlus2 = createDefaultEinkommensverschlechterung();
		ekvGSBasisJahrPlus2.setNettolohnJan(BigDecimal.valueOf(2));
		einkommensverschlechterungContainer.setEkvGSBasisJahrPlus2(ekvGSBasisJahrPlus2);

		final Einkommensverschlechterung ekvJABasisJahrPlus1 = createDefaultEinkommensverschlechterung();
		ekvJABasisJahrPlus1.setNettolohnJan(BigDecimal.valueOf(3));
		einkommensverschlechterungContainer.setEkvJABasisJahrPlus1(ekvJABasisJahrPlus1);

		final Einkommensverschlechterung ekvJABasisJahrPlus2 = createDefaultEinkommensverschlechterung();
		ekvJABasisJahrPlus2.setNettolohnJan(BigDecimal.valueOf(4));
		einkommensverschlechterungContainer.setEkvJABasisJahrPlus2(ekvJABasisJahrPlus2);

		return einkommensverschlechterungContainer;
	}

	public static Einkommensverschlechterung createDefaultEinkommensverschlechterung() {
		Einkommensverschlechterung einkommensverschlechterung = new Einkommensverschlechterung();
		createDefaultAbstractFinanzielleSituation(einkommensverschlechterung);
		einkommensverschlechterung.setNettolohnJan(BigDecimal.ONE);
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
		familiensituation.setGesuchstellerKardinalitaet(EnumGesuchstellerKardinalitaet.ALLEINE);
		familiensituation.setGemeinsameSteuererklaerung(Boolean.TRUE);
		return familiensituation;
	}

	public static Gesuch createDefaultGesuch() {
		return createDefaultGesuch(AntragStatus.IN_BEARBEITUNG_JA);
	}

	public static Gesuch createDefaultGesuch(AntragStatus status) {
		Gesuch gesuch = new Gesuch();
		gesuch.setGesuchsperiode(createDefaultGesuchsperiode());
		gesuch.setFall(createDefaultFall());
		gesuch.setEingangsdatum(LocalDate.now());
		gesuch.setFamiliensituationContainer(createDefaultFamiliensituationContainer());
		gesuch.setStatus(status);
		return gesuch;
	}

	public static Fall createDefaultFall() {
		return new Fall();
	}

	public static Mandant createDefaultMandant() {
		Mandant mandant = new Mandant();
		mandant.setName("Mandant1");
		return mandant;
	}

	public static Fachstelle createDefaultFachstelle() {
		Fachstelle fachstelle = new Fachstelle();
		fachstelle.setName("Fachstelle1");
		fachstelle.setBeschreibung("Kinder Fachstelle");
		fachstelle.setBehinderungsbestaetigung(true);
		return fachstelle;
	}

	public static FinanzielleSituationContainer createFinanzielleSituationContainer() {
		FinanzielleSituationContainer container = new FinanzielleSituationContainer();
		container.setJahr(LocalDate.now().minusYears(1).getYear());
		return container;
	}

	public static FinanzielleSituation createDefaultFinanzielleSituation() {

		FinanzielleSituation finanzielleSituation = new FinanzielleSituation();
		createDefaultAbstractFinanzielleSituation(finanzielleSituation);
		finanzielleSituation.setNettolohn(BigDecimal.valueOf(100000));
		return finanzielleSituation;
	}

	public static void createDefaultAbstractFinanzielleSituation(AbstractFinanzielleSituation abstractFinanzielleSituation) {
		abstractFinanzielleSituation.setSteuerveranlagungErhalten(Boolean.FALSE);
		abstractFinanzielleSituation.setSteuererklaerungAusgefuellt(Boolean.TRUE);
	}

	public static Traegerschaft createDefaultTraegerschaft() {
		Traegerschaft traegerschaft = new Traegerschaft();
		traegerschaft.setName("Traegerschaft1");
		traegerschaft.setMail("traegerschaft@example.com");
		return traegerschaft;
	}

	public static Institution createDefaultInstitution() {
		Institution institution = new Institution();
		institution.setName("Institution1");
		institution.setMandant(createDefaultMandant());
		institution.setTraegerschaft(createDefaultTraegerschaft());
		institution.setMail("institution@example.com");
		return institution;
	}

	public static InstitutionStammdaten createDefaultInstitutionStammdaten() {
		InstitutionStammdaten instStammdaten = new InstitutionStammdaten();
		instStammdaten.setIban(new IBAN(iban));
		instStammdaten.setOeffnungsstunden(BigDecimal.valueOf(24));
		instStammdaten.setOeffnungstage(BigDecimal.valueOf(365));
		instStammdaten.setGueltigkeit(new DateRange(Constants.GESUCHSPERIODE_17_18));
		instStammdaten.setBetreuungsangebotTyp(BetreuungsangebotTyp.KITA);
		instStammdaten.setInstitution(createDefaultInstitution());
		instStammdaten.setAdresse(createDefaultAdresse());
		return instStammdaten;
	}

	public static InstitutionStammdaten createInstitutionStammdatenTagesschuleForInstitution(@Nonnull Institution institution) {
		InstitutionStammdaten instStammdaten = new InstitutionStammdaten();
		instStammdaten.setIban(new IBAN(iban));
		instStammdaten.setOeffnungsstunden(BigDecimal.valueOf(24));
		instStammdaten.setOeffnungstage(BigDecimal.valueOf(365));
		instStammdaten.setGueltigkeit(new DateRange());
		instStammdaten.setBetreuungsangebotTyp(BetreuungsangebotTyp.TAGESSCHULE);
		instStammdaten.setInstitution(institution);
		instStammdaten.setAdresse(createDefaultAdresse());
		return instStammdaten;
	}

	public static InstitutionStammdaten createInstitutionStammdatenFerieninselForInstitution(@Nonnull Institution institution) {
		InstitutionStammdaten instStammdaten = new InstitutionStammdaten();
		instStammdaten.setIban(new IBAN(iban));
		instStammdaten.setOeffnungsstunden(BigDecimal.valueOf(24));
		instStammdaten.setOeffnungstage(BigDecimal.valueOf(365));
		instStammdaten.setGueltigkeit(new DateRange());
		instStammdaten.setBetreuungsangebotTyp(BetreuungsangebotTyp.FERIENINSEL);
		instStammdaten.setInstitution(institution);
		instStammdaten.setAdresse(createDefaultAdresse());
		return instStammdaten;
	}

	public static InstitutionStammdaten createInstitutionStammdatenKitaWeissenstein() {
		InstitutionStammdaten instStammdaten = new InstitutionStammdaten();
		instStammdaten.setId(AbstractTestfall.ID_INSTITUTION_STAMMDATEN_WEISSENSTEIN_KITA);
		instStammdaten.setIban(new IBAN(iban));
		instStammdaten.setOeffnungsstunden(BigDecimal.valueOf(11.50));
		instStammdaten.setOeffnungstage(BigDecimal.valueOf(240));
		instStammdaten.setGueltigkeit(Constants.DEFAULT_GUELTIGKEIT);
		instStammdaten.setBetreuungsangebotTyp(BetreuungsangebotTyp.KITA);
		instStammdaten.setInstitution(createDefaultInstitution());
		instStammdaten.getInstitution().setId(AbstractTestfall.ID_INSTITUTION_WEISSENSTEIN);
		instStammdaten.getInstitution().setName("Kita Aaregg");
		instStammdaten.setAdresse(createDefaultAdresse());
		return instStammdaten;
	}

	public static InstitutionStammdaten createInstitutionStammdatenTagiWeissenstein() {
		InstitutionStammdaten instStammdaten = new InstitutionStammdaten();
		instStammdaten.setId(AbstractTestfall.ID_INSTITUTION_STAMMDATEN_WEISSENSTEIN_TAGI);
		instStammdaten.setIban(new IBAN(iban));
		instStammdaten.setOeffnungsstunden(BigDecimal.valueOf(9));
		instStammdaten.setOeffnungstage(BigDecimal.valueOf(244));
		instStammdaten.setGueltigkeit(Constants.DEFAULT_GUELTIGKEIT);
		instStammdaten.setBetreuungsangebotTyp(BetreuungsangebotTyp.TAGI);
		instStammdaten.setInstitution(createDefaultInstitution());
		instStammdaten.getInstitution().setId(AbstractTestfall.ID_INSTITUTION_WEISSENSTEIN);
		instStammdaten.getInstitution().setName("Tagi & Kita Aaregg");
		instStammdaten.setAdresse(createDefaultAdresse());
		return instStammdaten;
	}

	public static InstitutionStammdaten createInstitutionStammdatenKitaBruennen() {
		InstitutionStammdaten instStammdaten = new InstitutionStammdaten();
		instStammdaten.setId(AbstractTestfall.ID_INSTITUTION_STAMMDATEN_BRUENNEN_KITA);
		instStammdaten.setIban(new IBAN(iban));
		instStammdaten.setOeffnungsstunden(BigDecimal.valueOf(11.50));
		instStammdaten.setOeffnungstage(BigDecimal.valueOf(240));
		instStammdaten.setGueltigkeit(Constants.DEFAULT_GUELTIGKEIT);
		instStammdaten.setBetreuungsangebotTyp(BetreuungsangebotTyp.KITA);
		instStammdaten.setInstitution(createDefaultInstitution());
		instStammdaten.getInstitution().setId(AbstractTestfall.ID_INSTITUTION_BRUENNEN);
		instStammdaten.getInstitution().setName("Kita Brünnen");
		instStammdaten.setAdresse(createDefaultAdresse());
		return instStammdaten;
	}

	public static InstitutionStammdaten createInstitutionStammdatenTagesschuleBern() {
		InstitutionStammdaten instStammdaten = new InstitutionStammdaten();
		instStammdaten.setId(AbstractTestfall.ID_INSTITUTION_STAMMDATEN_BERN_TAGESSCULHE);
		instStammdaten.setIban(new IBAN(iban));
		instStammdaten.setOeffnungsstunden(BigDecimal.valueOf(9));
		instStammdaten.setOeffnungstage(BigDecimal.valueOf(240));
		instStammdaten.setGueltigkeit(Constants.DEFAULT_GUELTIGKEIT);
		instStammdaten.setBetreuungsangebotTyp(BetreuungsangebotTyp.TAGESSCHULE);
		instStammdaten.setInstitution(createDefaultInstitution());
		instStammdaten.getInstitution().setId(AbstractTestfall.ID_INSTITUTION_BERN);
		instStammdaten.getInstitution().setName("Tagesschule Bern");
		instStammdaten.setAdresse(createDefaultAdresse());
		return instStammdaten;
	}

	public static InstitutionStammdaten createInstitutionStammdatenFerieninselGuarda() {
		InstitutionStammdaten instStammdaten = new InstitutionStammdaten();
		instStammdaten.setId(AbstractTestfall.ID_INSTITUTION_STAMMDATEN_GUARDA_FERIENINSEL);
		instStammdaten.setIban(new IBAN(iban));
		instStammdaten.setOeffnungsstunden(BigDecimal.valueOf(9));
		instStammdaten.setOeffnungstage(BigDecimal.valueOf(120));
		instStammdaten.setGueltigkeit(Constants.DEFAULT_GUELTIGKEIT);
		instStammdaten.setBetreuungsangebotTyp(BetreuungsangebotTyp.FERIENINSEL);
		instStammdaten.setInstitution(createDefaultInstitution());
		instStammdaten.getInstitution().setId(AbstractTestfall.ID_INSTITUTION_GUARDA);
		instStammdaten.getInstitution().setName("Ferieninsel Guarda");
		instStammdaten.setAdresse(createDefaultAdresse());
		return instStammdaten;
	}

	private static Kind createDefaultKind(boolean addFachstelle) {
		Kind kind = new Kind();
		kind.setNachname("Kind_Mustermann");
		kind.setVorname("Kind_Max");
		kind.setGeburtsdatum(LocalDate.of(2010, 12, 12));
		kind.setGeschlecht(Geschlecht.WEIBLICH);
		kind.setKinderabzug(Kinderabzug.GANZER_ABZUG);
		if (addFachstelle) {
			kind.setPensumFachstelle(createDefaultPensumFachstelle());
		}
		kind.setFamilienErgaenzendeBetreuung(true);
		kind.setMutterspracheDeutsch(true);
		kind.setEinschulung(true);
		return kind;
	}

	public static PensumFachstelle createDefaultPensumFachstelle() {
		PensumFachstelle pensumFachstelle = new PensumFachstelle();
		pensumFachstelle.setPensum(50);
		pensumFachstelle.setGueltigkeit(new DateRange(LocalDate.now(), Constants.END_OF_TIME));
		pensumFachstelle.setFachstelle(createDefaultFachstelle());
		return pensumFachstelle;
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

	public static ErwerbspensumContainer createErwerbspensum(LocalDate von, LocalDate bis, int pensum, int zuschlag) {
		ErwerbspensumContainer erwerbspensumContainer = new ErwerbspensumContainer();
		Erwerbspensum erwerbspensum = new Erwerbspensum();
		erwerbspensum.setPensum(pensum);
		erwerbspensum.setZuschlagsprozent(zuschlag);
		erwerbspensum.setGueltigkeit(new DateRange(von, bis));
		erwerbspensumContainer.setErwerbspensumJA(erwerbspensum);
		return erwerbspensumContainer;
	}

	public static Erwerbspensum createErwerbspensumData() {
		Erwerbspensum ep = new Erwerbspensum();
		ep.setTaetigkeit(Taetigkeit.ANGESTELLT);
		ep.setPensum(50);
		ep.setZuschlagZuErwerbspensum(true);
		ep.setZuschlagsgrund(Zuschlagsgrund.LANGER_ARBWEITSWEG);
		ep.setZuschlagsprozent(10);
		return ep;
	}

	public static Betreuung createAnmeldungTagesschule(KindContainer kind) {
		Betreuung betreuung = new Betreuung();
		betreuung.setInstitutionStammdaten(createInstitutionStammdatenTagesschuleBern());
		betreuung.setBetreuungsstatus(Betreuungsstatus.SCHULAMT_ANMELDUNG_AUSGELOEST);
		betreuung.setBetreuungspensumContainers(new TreeSet<>());
		betreuung.setAbwesenheitContainers(new HashSet<>());
		betreuung.setKind(kind);
		betreuung.setBelegungTagesschule(createDefaultBelegungTagesschule());
		return betreuung;
	}

	public static Betreuung createDefaultBetreuung() {
		Betreuung betreuung = new Betreuung();
		betreuung.setInstitutionStammdaten(createDefaultInstitutionStammdaten());
		betreuung.setBetreuungsstatus(Betreuungsstatus.BESTAETIGT);
		betreuung.setBetreuungspensumContainers(new TreeSet<>());
		betreuung.setAbwesenheitContainers(new HashSet<>());
		betreuung.setKind(createDefaultKindContainer());
		return betreuung;
	}

	public static BelegungTagesschule createDefaultBelegungTagesschule() {
		final BelegungTagesschule belegungTagesschule = new BelegungTagesschule();
		belegungTagesschule.setEintrittsdatum(LocalDate.now());
		return belegungTagesschule;
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
		betreuungspensum.setPensum(80);
		return betreuungspensum;
	}

	public static Gesuchsperiode createDefaultGesuchsperiode() {
		return createCurrentGesuchsperiode();
	}

	public static Gesuchsperiode createCurrentGesuchsperiode() {
		Gesuchsperiode gesuchsperiode = new Gesuchsperiode();
		gesuchsperiode.setStatus(GesuchsperiodeStatus.AKTIV);

		boolean isSecondHalbjahr = LocalDate.now().isAfter(LocalDate.of(LocalDate.now().getYear(), Month.JULY, 31));
		int startyear = isSecondHalbjahr ? LocalDate.now().getYear() : LocalDate.now().getYear() - 1;
		LocalDate start = LocalDate.of(startyear, Month.AUGUST, 1);
		LocalDate end = LocalDate.of(startyear + 1, Month.JULY, 31);
		gesuchsperiode.setGueltigkeit(new DateRange(start, end));
		return gesuchsperiode;
	}

	public static Gesuchsperiode createGesuchsperiode1718() {
		return createGesuchsperiodeXXYY(2017, 2018);
	}

	public static Gesuchsperiode createGesuchsperiode1617() {
		return createGesuchsperiodeXXYY(2016, 2017);
	}

	@Nonnull
	private static Gesuchsperiode createGesuchsperiodeXXYY(int yearFrom, int yearTo) {
		Gesuchsperiode gesuchsperiode = new Gesuchsperiode();
		gesuchsperiode.setStatus(GesuchsperiodeStatus.AKTIV);
		gesuchsperiode.setGueltigkeit(new DateRange(LocalDate.of(yearFrom, Month.AUGUST, 1), LocalDate.of(yearTo,
			Month.JULY, 31)));
		return gesuchsperiode;
	}

	public static Gesuchsperiode createCustomGesuchsperiode(int firstYear, int secondYear) {
		Gesuchsperiode gesuchsperiode = new Gesuchsperiode();
		gesuchsperiode.setStatus(GesuchsperiodeStatus.AKTIV);
		gesuchsperiode.setGueltigkeit(new DateRange(LocalDate.of(firstYear, Month.AUGUST, 1), LocalDate.of(secondYear, Month.JULY, 31)));
		return gesuchsperiode;
	}

	public static EbeguParameter createDefaultEbeguParameter(EbeguParameterKey key) {
		EbeguParameter instStammdaten = new EbeguParameter();
		instStammdaten.setName(key);
		instStammdaten.setValue("1");
		instStammdaten.setGueltigkeit(new DateRange(Constants.START_OF_TIME, Constants.END_OF_TIME));
		return instStammdaten;
	}

	public static List<EbeguParameter> createAllEbeguParameters() {
		final List<EbeguParameter> list = new ArrayList<>();
		list.add(createDefaultEbeguParameter(EbeguParameterKey.PARAM_ABGELTUNG_PRO_TAG_KANTON));
		list.add(createDefaultEbeguParameter(EbeguParameterKey.PARAM_ANZAHL_TAGE_KANTON));
		list.add(createDefaultEbeguParameter(EbeguParameterKey.PARAM_ANZAL_TAGE_MAX_KITA));
		list.add(createDefaultEbeguParameter(EbeguParameterKey.PARAM_BABY_ALTER_IN_MONATEN));
		list.add(createDefaultEbeguParameter(EbeguParameterKey.PARAM_BABY_FAKTOR));
		list.add(createDefaultEbeguParameter(EbeguParameterKey.PARAM_FIXBETRAG_STADT_PRO_TAG_KITA));
		list.add(createDefaultEbeguParameter(EbeguParameterKey.PARAM_GRENZWERT_EINKOMMENSVERSCHLECHTERUNG));
		list.add(createDefaultEbeguParameter(EbeguParameterKey.PARAM_STUNDEN_PRO_TAG_TAGI));
		list.add(createDefaultEbeguParameter(EbeguParameterKey.PARAM_MAX_TAGE_ABWESENHEIT));
		list.add(createDefaultEbeguParameter(EbeguParameterKey.PARAM_MASSGEBENDES_EINKOMMEN_MIN));
		list.add(createDefaultEbeguParameter(EbeguParameterKey.PARAM_MASSGEBENDES_EINKOMMEN_MAX));
		list.add(createDefaultEbeguParameter(EbeguParameterKey.PARAM_STUNDEN_PRO_TAG_MAX_KITA));
		list.add(createDefaultEbeguParameter(EbeguParameterKey.PARAM_PENSUM_TAGI_MIN));
		list.add(createDefaultEbeguParameter(EbeguParameterKey.PARAM_PENSUM_TAGESSCHULE_MIN));
		list.add(createDefaultEbeguParameter(EbeguParameterKey.PARAM_PENSUM_TAGESELTERN_MIN));
		list.add(createDefaultEbeguParameter(EbeguParameterKey.PARAM_PENSUM_KITA_MIN));
		list.add(createDefaultEbeguParameter(EbeguParameterKey.PARAM_PAUSCHALABZUG_PRO_PERSON_FAMILIENGROESSE_3));
		list.add(createDefaultEbeguParameter(EbeguParameterKey.PARAM_PAUSCHALABZUG_PRO_PERSON_FAMILIENGROESSE_4));
		list.add(createDefaultEbeguParameter(EbeguParameterKey.PARAM_PAUSCHALABZUG_PRO_PERSON_FAMILIENGROESSE_5));
		list.add(createDefaultEbeguParameter(EbeguParameterKey.PARAM_PAUSCHALABZUG_PRO_PERSON_FAMILIENGROESSE_6));
		list.add(createDefaultEbeguParameter(EbeguParameterKey.PARAM_KOSTEN_PRO_STUNDE_MIN));
		list.add(createDefaultEbeguParameter(EbeguParameterKey.PARAM_KOSTEN_PRO_STUNDE_MAX));
		list.add(createDefaultEbeguParameter(EbeguParameterKey.PARAM_KOSTEN_PRO_STUNDE_MAX_TAGESELTERN));
		list.add(createDefaultEbeguParameter(EbeguParameterKey.PARAM_MAXIMALER_ZUSCHLAG_ERWERBSPENSUM));
		return list;
	}

	public static List<EbeguParameter> createAndPersistAllEbeguParameters(EbeguParameterService parameterService) {
		final List<EbeguParameter> allEbeguParameters = createAllEbeguParameters();
		allEbeguParameters.forEach(parameterService::saveEbeguParameter);
		return allEbeguParameters;
	}

	public static EinkommensverschlechterungInfoContainer createDefaultEinkommensverschlechterungsInfoContainer(Gesuch gesuch) {
		final EinkommensverschlechterungInfoContainer einkommensverschlechterungInfoContainer = new EinkommensverschlechterungInfoContainer();
		einkommensverschlechterungInfoContainer.setEinkommensverschlechterungInfoJA(createDefaultEinkommensverschlechterungsInfo());
		einkommensverschlechterungInfoContainer.setGesuch(gesuch);
		gesuch.setEinkommensverschlechterungInfoContainer(einkommensverschlechterungInfoContainer);

		return einkommensverschlechterungInfoContainer;
	}

	public static EinkommensverschlechterungInfo createDefaultEinkommensverschlechterungsInfo() {
		final EinkommensverschlechterungInfo einkommensverschlechterungInfo = new EinkommensverschlechterungInfo();
		einkommensverschlechterungInfo.setEinkommensverschlechterung(true);
		einkommensverschlechterungInfo.setEkvFuerBasisJahrPlus1(true);
		einkommensverschlechterungInfo.setStichtagFuerBasisJahrPlus1(LocalDate.now());
		einkommensverschlechterungInfo.setGrundFuerBasisJahrPlus1("Grund fuer basis Jahr Plus 1");
		einkommensverschlechterungInfo.setEkvFuerBasisJahrPlus2(false);
		return einkommensverschlechterungInfo;
	}

	public static Gesuch createDefaultEinkommensverschlechterungsGesuch() {
		Gesuch gesuch = createDefaultGesuch();
		gesuch.setEinkommensverschlechterungInfoContainer(createDefaultEinkommensverschlechterungsInfoContainer(gesuch));
		return gesuch;
	}

	public static GesuchstellerContainer createDefaultGesuchstellerWithEinkommensverschlechterung() {
		final Gesuch gesuch = TestDataUtil.createDefaultGesuch();
		final GesuchstellerContainer gesuchsteller = createDefaultGesuchstellerContainer(gesuch);
		gesuchsteller.setEinkommensverschlechterungContainer(createDefaultEinkommensverschlechterungsContainer());
		return gesuchsteller;
	}

	public static Benutzer createDefaultBenutzer() {
		Benutzer user = new Benutzer();
		user.setUsername("jula_" + UUID.randomUUID());
		user.setNachname("Iglesias");
		user.setVorname("Julio");
		user.setEmail("julio.iglesias@example.com");
		user.setMandant(createDefaultMandant());
		Berechtigung berechtigung = new Berechtigung();
		berechtigung.setRole(UserRole.ADMIN);
		berechtigung.setBenutzer(user);
		user.getBerechtigungen().add(berechtigung);
		return user;
	}

	public static Benutzer createBenutzerSCH() {
		final Benutzer defaultBenutzer = TestDataUtil.createDefaultBenutzer();
		defaultBenutzer.setRole(UserRole.SCHULAMT);
		return defaultBenutzer;
	}

	@SuppressWarnings("ConstantConditions")
	public static Betreuung createGesuchWithBetreuungspensum(boolean zweiGesuchsteller) {
		Gesuch gesuch = new Gesuch();
		gesuch.setGesuchsperiode(TestDataUtil.createGesuchsperiode1718());
		gesuch.setFamiliensituationContainer(createDefaultFamiliensituationContainer());
		gesuch.extractFamiliensituation().setFamilienstatus(EnumFamilienstatus.ALLEINERZIEHEND);
		if (zweiGesuchsteller) {
			gesuch.extractFamiliensituation().setGesuchstellerKardinalitaet(EnumGesuchstellerKardinalitaet.ZU_ZWEIT);
		} else {
			gesuch.extractFamiliensituation().setGesuchstellerKardinalitaet(EnumGesuchstellerKardinalitaet.ALLEINE);
		}
		gesuch.setGesuchsteller1(new GesuchstellerContainer());
		gesuch.getGesuchsteller1().setFinanzielleSituationContainer(new FinanzielleSituationContainer());
		gesuch.getGesuchsteller1().getFinanzielleSituationContainer().setFinanzielleSituationJA(new FinanzielleSituation());
		if (zweiGesuchsteller) {
			gesuch.setGesuchsteller2(new GesuchstellerContainer());
			gesuch.getGesuchsteller2().setFinanzielleSituationContainer(new FinanzielleSituationContainer());
			gesuch.getGesuchsteller2().getFinanzielleSituationContainer().setFinanzielleSituationJA(new FinanzielleSituation());
		}
		Betreuung betreuung = new Betreuung();
		betreuung.setKind(new KindContainer());
		betreuung.getKind().setKindJA(new Kind());
		betreuung.getKind().setGesuch(gesuch);
		betreuung.setInstitutionStammdaten(createDefaultInstitutionStammdaten());
		return betreuung;
	}

	public static void calculateFinanzDaten(Gesuch gesuch) {
		if (gesuch.getGesuchsperiode() == null) {
			gesuch.setGesuchsperiode(createGesuchsperiode1718());
		}
		FinanzielleSituationRechner finanzielleSituationRechner = new FinanzielleSituationRechner();
		finanzielleSituationRechner.calculateFinanzDaten(gesuch, BigDecimal.valueOf(0.80));
	}

	public static Gesuch createTestgesuchDagmar() {
		List<InstitutionStammdaten> insttStammdaten = new ArrayList<>();
		insttStammdaten.add(TestDataUtil.createDefaultInstitutionStammdaten());
		Testfall01_WaeltiDagmar testfall = new Testfall01_WaeltiDagmar(TestDataUtil.createGesuchsperiode1718(), insttStammdaten);
		testfall.createFall(null);
		testfall.createGesuch(LocalDate.of(1980, Month.MARCH, 25));
		Gesuch gesuch = testfall.fillInGesuch();
		TestDataUtil.calculateFinanzDaten(gesuch);
		gesuch.setGesuchsperiode(TestDataUtil.createGesuchsperiode1718());
		return gesuch;
	}

	public static void setFinanzielleSituation(Gesuch gesuch, BigDecimal einkommen) {
		gesuch.getGesuchsteller1().setFinanzielleSituationContainer(new FinanzielleSituationContainer());
		gesuch.getGesuchsteller1().getFinanzielleSituationContainer().setFinanzielleSituationJA(new FinanzielleSituation());
		gesuch.getGesuchsteller1().getFinanzielleSituationContainer().getFinanzielleSituationJA().setNettolohn(einkommen);
	}

	public static void setEinkommensverschlechterung(Gesuch gesuch, GesuchstellerContainer gesuchsteller, BigDecimal einkommen, boolean basisJahrPlus1) {
		if (gesuchsteller.getEinkommensverschlechterungContainer() == null) {
			gesuchsteller.setEinkommensverschlechterungContainer(new EinkommensverschlechterungContainer());
		}
		if (gesuch.extractEinkommensverschlechterungInfo() == null) {
			gesuch.setEinkommensverschlechterungInfoContainer(new EinkommensverschlechterungInfoContainer());
			gesuch.extractEinkommensverschlechterungInfo().setEinkommensverschlechterung(true);
			gesuch.extractEinkommensverschlechterungInfo().setEkvFuerBasisJahrPlus1(false);
			gesuch.extractEinkommensverschlechterungInfo().setEkvFuerBasisJahrPlus2(false);
		}
		if (basisJahrPlus1) {
			gesuchsteller.getEinkommensverschlechterungContainer().setEkvJABasisJahrPlus1(new Einkommensverschlechterung());
			gesuchsteller.getEinkommensverschlechterungContainer().getEkvJABasisJahrPlus1().setNettolohnAug(einkommen);
			gesuch.extractEinkommensverschlechterungInfo().setEkvFuerBasisJahrPlus1(true);
			;
			gesuch.extractEinkommensverschlechterungInfo().setStichtagFuerBasisJahrPlus1(STICHTAG_EKV_1);
			gesuch.extractEinkommensverschlechterungInfo().setEinkommensverschlechterung(true);
		} else {
			gesuchsteller.getEinkommensverschlechterungContainer().setEkvJABasisJahrPlus2(new Einkommensverschlechterung());
			gesuchsteller.getEinkommensverschlechterungContainer().getEkvJABasisJahrPlus2().setNettolohnAug(einkommen);
			gesuch.extractEinkommensverschlechterungInfo().setEkvFuerBasisJahrPlus2(true);
			gesuch.extractEinkommensverschlechterungInfo().setStichtagFuerBasisJahrPlus2(STICHTAG_EKV_2);
			gesuch.extractEinkommensverschlechterungInfo().setEinkommensverschlechterung(true);
		}
	}

	public static DokumentGrund createDefaultDokumentGrund() {

		DokumentGrund dokumentGrund = new DokumentGrund();
		dokumentGrund.setDokumentGrundTyp(DokumentGrundTyp.EINKOMMENSVERSCHLECHTERUNG);
		dokumentGrund.setTag("tag");
		dokumentGrund.setFullName("Hugo");
		dokumentGrund.setDokumentTyp(DokumentTyp.JAHRESLOHNAUSWEISE);
		dokumentGrund.setDokumente(new HashSet<>());
		final Dokument dokument = new Dokument();
		dokument.setDokumentGrund(dokumentGrund);
		dokument.setFilename("testdokument");
		dokument.setFilepfad("testpfad/");
		dokument.setFilesize("123456");
		dokument.setTimestampUpload(LocalDateTime.now());
		dokumentGrund.getDokumente().add(dokument);
		return dokumentGrund;
	}

	/**
	 * Hilfsmethode die den Testfall Waelti Dagmar erstellt und speichert
	 */
	public static Gesuch createAndPersistWaeltiDagmarGesuch(InstitutionService instService, Persistence persistence, @Nullable LocalDate eingangsdatum, AntragStatus status) {
		instService.getAllInstitutionen();
		List<InstitutionStammdaten> institutionStammdatenList = new ArrayList<>();
		institutionStammdatenList.add(TestDataUtil.createInstitutionStammdatenKitaWeissenstein());
		institutionStammdatenList.add(TestDataUtil.createInstitutionStammdatenKitaBruennen());
		Testfall01_WaeltiDagmar testfall = new Testfall01_WaeltiDagmar(TestDataUtil.createGesuchsperiode1718(), institutionStammdatenList);

		if (status != null) {
			return persistAllEntities(persistence, eingangsdatum, testfall, status);
		} else {
			return persistAllEntities(persistence, eingangsdatum, testfall);
		}
	}

	public static Gesuch createAndPersistWaeltiDagmarGesuch(InstitutionService instService, Persistence persistence, @Nullable LocalDate eingangsdatum) {
		return createAndPersistWaeltiDagmarGesuch(instService, persistence, eingangsdatum, null);
	}

	private static void ensureFachstelleAndInstitutionsExist(Persistence persistence, Gesuch gesuch) {
		for (KindContainer kindContainer : gesuch.getKindContainers()) {
			for (Betreuung betreuung : kindContainer.getBetreuungen()) {
				persistence.merge(betreuung.getInstitutionStammdaten().getInstitution().getTraegerschaft());
				persistence.merge(betreuung.getInstitutionStammdaten().getInstitution().getMandant());
				if (persistence.find(Institution.class, betreuung.getInstitutionStammdaten().getInstitution().getId()) == null) {
					persistence.merge(betreuung.getInstitutionStammdaten().getInstitution());
				}
				if (persistence.find(InstitutionStammdaten.class, betreuung.getInstitutionStammdaten().getId()) == null) {
					persistence.merge(betreuung.getInstitutionStammdaten());
				}
				if (betreuung.getKind().getKindJA().getPensumFachstelle() != null) {
					persistence.merge(betreuung.getKind().getKindJA().getPensumFachstelle().getFachstelle());
				}
			}
		}
	}

	public static Gesuch createAndPersistFeutzYvonneGesuch(InstitutionService instService, Persistence persistence, LocalDate eingangsdatum, AntragStatus status) {
		instService.getAllInstitutionen();
		List<InstitutionStammdaten> institutionStammdatenList = new ArrayList<>();
		institutionStammdatenList.add(TestDataUtil.createInstitutionStammdatenTagiWeissenstein());
		institutionStammdatenList.add(TestDataUtil.createInstitutionStammdatenKitaWeissenstein());
		Testfall02_FeutzYvonne testfall = new Testfall02_FeutzYvonne(TestDataUtil.createGesuchsperiode1718(), institutionStammdatenList);

		return persistAllEntities(persistence, eingangsdatum, testfall, status);
	}

	public static Gesuch createAndPersistFeutzYvonneGesuch(InstitutionService instService, Persistence persistence, LocalDate eingangsdatum) {
		instService.getAllInstitutionen();
		List<InstitutionStammdaten> institutionStammdatenList = new ArrayList<>();
		institutionStammdatenList.add(TestDataUtil.createInstitutionStammdatenTagiWeissenstein());
		institutionStammdatenList.add(TestDataUtil.createInstitutionStammdatenKitaWeissenstein());
		Testfall02_FeutzYvonne testfall = new Testfall02_FeutzYvonne(TestDataUtil.createGesuchsperiode1718(), institutionStammdatenList);

		return persistAllEntities(persistence, eingangsdatum, testfall);
	}

	public static Gesuch createAndPersistBeckerNoraGesuch(InstitutionService instService, Persistence persistence, @Nullable LocalDate eingangsdatum, AntragStatus status) {
		instService.getAllInstitutionen();
		List<InstitutionStammdaten> institutionStammdatenList = new ArrayList<>();
		institutionStammdatenList.add(TestDataUtil.createInstitutionStammdatenTagiWeissenstein());
		institutionStammdatenList.add(TestDataUtil.createInstitutionStammdatenKitaWeissenstein());
		Testfall06_BeckerNora testfall = new Testfall06_BeckerNora(TestDataUtil.createGesuchsperiode1718(), institutionStammdatenList);

		if (status != null) {
			return persistAllEntities(persistence, eingangsdatum, testfall, status);
		} else {
			return persistAllEntities(persistence, eingangsdatum, testfall);
		}
	}

	public static Gesuch createAndPersistBeckerNoraGesuch(InstitutionService instService, Persistence persistence, @Nullable LocalDate eingangsdatum) {
		return createAndPersistBeckerNoraGesuch(instService, persistence, eingangsdatum, null);
	}

	public static Institution createAndPersistDefaultInstitution(Persistence persistence) {
		Institution inst = createDefaultInstitution();
		persistence.merge(inst.getMandant());
		persistence.merge(inst.getTraegerschaft());
		return persistence.merge(inst);

	}

	private static Gesuch persistAllEntities(Persistence persistence, @Nullable LocalDate eingangsdatum, AbstractTestfall testfall, AntragStatus status) {
		Benutzer verantwortlicher = createAndPersistBenutzer(persistence);
		testfall.createFall(verantwortlicher);
		testfall.createGesuch(eingangsdatum, status);
		persistence.persist(testfall.getGesuch().getFall());
		persistence.persist(testfall.getGesuch().getGesuchsperiode());
		persistence.persist(testfall.getGesuch());
		Gesuch gesuch = testfall.fillInGesuch();
		ensureFachstelleAndInstitutionsExist(persistence, gesuch);
		gesuch = persistence.merge(gesuch);
		return gesuch;
	}

	@Nonnull
	private static Benutzer createAndPersistBenutzer(Persistence persistence) {
		Benutzer verantwortlicher = TestDataUtil.createDefaultBenutzer();
		persistence.persist(verantwortlicher.getMandant());
		persistence.persist(verantwortlicher);
		return verantwortlicher;
	}

	private static Gesuch persistAllEntities(Persistence persistence, @Nullable LocalDate eingangsdatum, AbstractTestfall testfall) {
		Benutzer verantwortlicher = createAndPersistBenutzer(persistence);
		testfall.createFall(verantwortlicher);
		testfall.createGesuch(eingangsdatum);
		persistence.persist(testfall.getGesuch().getFall());
		persistence.persist(testfall.getGesuch().getGesuchsperiode());
		persistence.persist(testfall.getGesuch());
		Gesuch gesuch = testfall.fillInGesuch();
		ensureFachstelleAndInstitutionsExist(persistence, gesuch);
		gesuch = persistence.merge(gesuch);
		return gesuch;
	}

	public static void persistEntities(Gesuch gesuch, Persistence persistence) {
		Benutzer verantwortlicher = createAndPersistBenutzer(persistence);

		gesuch.getFall().setVerantwortlicher(verantwortlicher);
		persistence.persist(gesuch.getFall());
		gesuch.setGesuchsteller1(TestDataUtil.createDefaultGesuchstellerContainer(gesuch));
		persistence.persist(gesuch.getGesuchsperiode());

		Set<KindContainer> kindContainers = new TreeSet<>();
		Betreuung betreuung = TestDataUtil.createDefaultBetreuung();
		KindContainer kind = betreuung.getKind();

		Set<Betreuung> betreuungen = new TreeSet<>();
		betreuungen.add(betreuung);
		kind.setBetreuungen(betreuungen);

		persistence.persist(kind.getKindGS().getPensumFachstelle().getFachstelle());
		persistence.persist(kind.getKindJA().getPensumFachstelle().getFachstelle());
		kind.setGesuch(gesuch);
		kindContainers.add(kind);
		gesuch.setKindContainers(kindContainers);

		persistence.persist(betreuung.getInstitutionStammdaten().getInstitution().getTraegerschaft());
		persistence.persist(betreuung.getInstitutionStammdaten().getInstitution().getMandant());
		persistence.persist(betreuung.getInstitutionStammdaten().getInstitution());
		persistence.persist(betreuung.getInstitutionStammdaten());

		persistence.persist(gesuch);
	}

	public static Gesuch createAndPersistGesuch(Persistence persistence) {
		Gesuch gesuch = TestDataUtil.createDefaultGesuch();
		persistence.persist(gesuch.getFall());
		persistence.persist(gesuch.getGesuchsperiode());
		persistence.persist(gesuch);
		return gesuch;
	}

	public static Gesuch createAndPersistGesuch(Persistence persistence, AntragStatus status) {
		Gesuch gesuch = TestDataUtil.createDefaultGesuch(status);
		persistence.persist(gesuch.getFall());
		persistence.persist(gesuch.getGesuchsperiode());
		persistence.persist(gesuch);
		return gesuch;
	}

	public static WizardStep createWizardStepObject(Gesuch gesuch, WizardStepName wizardStepName, WizardStepStatus stepStatus) {
		final WizardStep jaxWizardStep = new WizardStep();
		jaxWizardStep.setGesuch(gesuch);
		jaxWizardStep.setWizardStepName(wizardStepName);
		jaxWizardStep.setWizardStepStatus(stepStatus);
		jaxWizardStep.setBemerkungen("");
		return jaxWizardStep;
	}

	public static void prepareParameters(DateRange gueltigkeit, Persistence persistence) {

		LocalDate year1Start = LocalDate.of(gueltigkeit.getGueltigAb().getYear(), Month.JANUARY, 1);
		LocalDate year1End = LocalDate.of(gueltigkeit.getGueltigAb().getYear(), Month.DECEMBER, 31);
		saveParameter(PARAM_ABGELTUNG_PRO_TAG_KANTON, "107.19", gueltigkeit, persistence);
		saveParameter(PARAM_FIXBETRAG_STADT_PRO_TAG_KITA, "7", new DateRange(year1Start, year1End), persistence);
		saveParameter(PARAM_FIXBETRAG_STADT_PRO_TAG_KITA, "7", new DateRange(year1Start.plusYears(1), year1End.plusYears(1)), persistence);
		saveParameter(PARAM_ANZAL_TAGE_MAX_KITA, "244", gueltigkeit, persistence);
		saveParameter(PARAM_STUNDEN_PRO_TAG_MAX_KITA, "11.5", gueltigkeit, persistence);
		saveParameter(PARAM_KOSTEN_PRO_STUNDE_MAX, "11.91", gueltigkeit, persistence);
		saveParameter(PARAM_KOSTEN_PRO_STUNDE_MIN, "0.75", gueltigkeit, persistence);
		saveParameter(PARAM_MASSGEBENDES_EINKOMMEN_MAX, "158690", gueltigkeit, persistence);
		saveParameter(PARAM_MASSGEBENDES_EINKOMMEN_MIN, "42540", gueltigkeit, persistence);
		saveParameter(PARAM_ANZAHL_TAGE_KANTON, "240", gueltigkeit, persistence);
		saveParameter(PARAM_STUNDEN_PRO_TAG_TAGI, "7", gueltigkeit, persistence);
		saveParameter(PARAM_KOSTEN_PRO_STUNDE_MAX_TAGESELTERN, "9.16", gueltigkeit, persistence);
		saveParameter(PARAM_BABY_ALTER_IN_MONATEN, "12", gueltigkeit, persistence);  //waere eigentlich int
		saveParameter(PARAM_BABY_FAKTOR, "1.5", gueltigkeit, persistence);
		saveParameter(PARAM_PAUSCHALABZUG_PRO_PERSON_FAMILIENGROESSE_3, "3760", gueltigkeit, persistence);
		saveParameter(PARAM_PAUSCHALABZUG_PRO_PERSON_FAMILIENGROESSE_4, "5900", gueltigkeit, persistence);
		saveParameter(PARAM_PAUSCHALABZUG_PRO_PERSON_FAMILIENGROESSE_5, "6970", gueltigkeit, persistence);
		saveParameter(PARAM_PAUSCHALABZUG_PRO_PERSON_FAMILIENGROESSE_6, "7500", gueltigkeit, persistence);
		saveParameter(PARAM_GRENZWERT_EINKOMMENSVERSCHLECHTERUNG, "20", gueltigkeit, persistence);
		saveParameter(PARAM_MAXIMALER_ZUSCHLAG_ERWERBSPENSUM, "20", gueltigkeit, persistence);

	}

	public static void saveParameter(EbeguParameterKey key, String value, DateRange gueltigkeit, Persistence persistence) {
		EbeguParameter ebeguParameter = new EbeguParameter(key, value, gueltigkeit);
		persistence.persist(ebeguParameter);

	}

	public static void prepareApplicationProperties(Persistence persistence) {

		saveParameter(ApplicationPropertyKey.DEBTOR_NAME, "Direktion für Bildung, Soziales und Sport der Stadt Bern", persistence);
		saveParameter(ApplicationPropertyKey.DEBTOR_IBAN, "CH4808704020071690000", persistence);
		saveParameter(ApplicationPropertyKey.DEBTOR_BIC, "POFICHBEXXX", persistence);
		saveParameter(ApplicationPropertyKey.DEBTOR_IBAN_GEBUEHREN, "CH4808704020071700000", persistence);
	}

	public static void saveParameter(ApplicationPropertyKey key, String value, Persistence persistence) {
		ApplicationProperty applicationProperty = new ApplicationProperty(key, value);
		persistence.persist(applicationProperty);

	}

	public static Benutzer createBenutzer(UserRole role, String userName, @Nullable Traegerschaft traegerschaft, @Nullable Institution institution, Mandant mandant) {
		final Benutzer benutzer = new Benutzer();
		benutzer.setUsername(userName);
		benutzer.setNachname("anonymous");
		benutzer.setVorname("anonymous");
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

	public static Benutzer createAndPersistJABenutzer(Persistence persistence) {
		final Mandant mandant = TestDataUtil.createDefaultMandant();
		persistence.persist(mandant);
		final Benutzer benutzer = TestDataUtil.createBenutzer(UserRole.SACHBEARBEITER_JA, UUID.randomUUID().toString(), null, null, mandant);
		persistence.persist(benutzer);
		return benutzer;
	}

	public static Benutzer createAndPersistTraegerschaftBenutzer(Persistence persistence) {
		final Traegerschaft traegerschaft = TestDataUtil.createDefaultTraegerschaft();
		persistence.persist(traegerschaft);
		final Mandant mandant = TestDataUtil.createDefaultMandant();
		persistence.persist(mandant);
		final Benutzer benutzer = TestDataUtil.createBenutzer(UserRole.SACHBEARBEITER_TRAEGERSCHAFT, UUID.randomUUID().toString(), traegerschaft, null, mandant);
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

	public static Benutzer createDummySuperAdmin(Persistence persistence, @Nullable Mandant mandant) {
		//machmal brauchen wir einen dummy admin in der DB
		if (mandant == null) {
			mandant = TestDataUtil.createDefaultMandant();
			persistence.persist(mandant);
		}
		final Benutzer benutzer = TestDataUtil.createBenutzer(UserRole.SUPER_ADMIN, "superadmin", null, null, mandant);
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

	public static void createDefaultAdressenForGS(final Gesuch gesuch, final boolean gs2) {
		List<GesuchstellerAdresseContainer> adressen1 = new ArrayList<>();
		final GesuchstellerAdresseContainer adresseGS1 = TestDataUtil.createDefaultGesuchstellerAdresseContainer(gesuch.getGesuchsteller1());
		adresseGS1.getGesuchstellerAdresseJA().setNichtInGemeinde(false);
		adresseGS1.getGesuchstellerAdresseJA().setGueltigkeit(new DateRange(Constants.START_OF_TIME, Constants.END_OF_TIME));
		adressen1.add(adresseGS1);
		gesuch.getGesuchsteller1().setAdressen(adressen1);

		if (gs2) {
			List<GesuchstellerAdresseContainer> adressen2 = new ArrayList<>();
			final GesuchstellerAdresseContainer adresseGS2 = TestDataUtil.createDefaultGesuchstellerAdresseContainer(gesuch.getGesuchsteller2());
			adresseGS2.getGesuchstellerAdresseJA().setNichtInGemeinde(false);
			adresseGS2.getGesuchstellerAdresseJA().setGueltigkeit(new DateRange(Constants.START_OF_TIME, Constants.END_OF_TIME));
			adressen2.add(adresseGS2);
			gesuch.getGesuchsteller2().setAdressen(adressen2);
		}
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
		mahnung.setBemerkungen(bemerkungen.stream().collect(Collectors.joining("\n")));
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
		abwesenheit.setGueltigkeit(new DateRange(gesuchsperiode.getGueltigkeit().getGueltigAb().plusMonths(1),
			gesuchsperiode.getGueltigkeit().getGueltigAb().plusMonths(1).plusDays(Constants.ABWESENHEIT_DAYS_LIMIT - 1)));
		return abwesenheit;
	}

	public static AbwesenheitContainer createLongAbwesenheitContainer(final Gesuchsperiode gesuchsperiode) {
		final AbwesenheitContainer abwesenheitContainer = new AbwesenheitContainer();
		abwesenheitContainer.setAbwesenheitJA(createLongAbwesenheit(gesuchsperiode));
		return abwesenheitContainer;
	}

	private static Abwesenheit createLongAbwesenheit(final Gesuchsperiode gesuchsperiode) {
		final Abwesenheit abwesenheit = new Abwesenheit();
		abwesenheit.setGueltigkeit(new DateRange(gesuchsperiode.getGueltigkeit().getGueltigAb().plusMonths(1),
			gesuchsperiode.getGueltigkeit().getGueltigAb().plusMonths(1).plusDays(Constants.ABWESENHEIT_DAYS_LIMIT)));
		return abwesenheit;
	}

	public static Gesuch createGesuch(Fall fall, Gesuchsperiode periodeToUpdate, AntragStatus status) {
		Gesuch gesuch = new Gesuch();
		gesuch.setFall(fall);
		gesuch.setGesuchsperiode(periodeToUpdate);
		gesuch.setStatus(status);
		return gesuch;
	}

	@SuppressWarnings("MagicNumber")
	public static Betreuungsmitteilung createBetreuungmitteilung(Fall fall, Benutzer empfaenger, MitteilungTeilnehmerTyp empfaengerTyp,
		Benutzer sender, MitteilungTeilnehmerTyp senderTyp) {
		final Betreuungsmitteilung mitteilung = new Betreuungsmitteilung();
		fillOutMitteilung(fall, empfaenger, empfaengerTyp, sender, senderTyp, mitteilung);

		Set<BetreuungsmitteilungPensum> betPensen = new HashSet<>();

		BetreuungsmitteilungPensum pensum = new BetreuungsmitteilungPensum();
		pensum.setBetreuungsmitteilung(mitteilung);
		pensum.setGueltigkeit(new DateRange(Constants.START_OF_TIME, Constants.END_OF_TIME));
		pensum.setPensum(30);

		betPensen.add(pensum);
		mitteilung.setBetreuungspensen(betPensen);

		return mitteilung;
	}

	public static Mitteilung createMitteilung(Fall fall, Benutzer empfaenger, MitteilungTeilnehmerTyp empfaengerTyp,
		Benutzer sender, MitteilungTeilnehmerTyp senderTyp) {
		Mitteilung mitteilung = new Mitteilung();
		fillOutMitteilung(fall, empfaenger, empfaengerTyp, sender, senderTyp, mitteilung);
		return mitteilung;
	}

	private static void fillOutMitteilung(Fall fall, Benutzer empfaenger, MitteilungTeilnehmerTyp empfaengerTyp, Benutzer sender, MitteilungTeilnehmerTyp senderTyp, Mitteilung mitteilung) {
		mitteilung.setFall(fall);
		mitteilung.setEmpfaenger(empfaenger);
		mitteilung.setSender(sender);
		mitteilung.setMitteilungStatus(MitteilungStatus.ENTWURF);
		mitteilung.setSubject("Subject");
		mitteilung.setEmpfaengerTyp(empfaengerTyp);
		mitteilung.setSenderTyp(senderTyp);
		mitteilung.setMessage("Message");
	}

	public static MitteilungTableFilterDTO createMitteilungTableFilterDTO() {
		MitteilungTableFilterDTO filterDTO = new MitteilungTableFilterDTO();
		filterDTO.setSearch(new MitteilungSearchDTO());
		return filterDTO;
	}

	public static Betreuung persistBetreuung(BetreuungService betreuungService, Persistence persistence) {
		Betreuung betreuung = TestDataUtil.createDefaultBetreuung();
		for (BetreuungspensumContainer container : betreuung.getBetreuungspensumContainers()) {
			persistence.persist(container);
		}
		for (AbwesenheitContainer abwesenheit : betreuung.getAbwesenheitContainers()) {
			persistence.persist(abwesenheit);
		}
		persistence.persist(betreuung.getInstitutionStammdaten().getInstitution().getTraegerschaft());
		persistence.persist(betreuung.getInstitutionStammdaten().getInstitution().getMandant());
		persistence.persist(betreuung.getInstitutionStammdaten().getInstitution());
		persistence.persist(betreuung.getInstitutionStammdaten());
		persistence.persist(betreuung.getKind().getKindGS().getPensumFachstelle().getFachstelle());
		persistence.persist(betreuung.getKind().getKindJA().getPensumFachstelle().getFachstelle());

		Gesuch gesuch = TestDataUtil.createAndPersistGesuch(persistence);
		betreuung.getKind().setGesuch(gesuch);
		persistence.persist(betreuung.getKind());

		betreuung.setBetreuungsstatus(Betreuungsstatus.WARTEN);
		final Betreuung savedBetreuung = betreuungService.saveBetreuung(betreuung, false);

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

	public static Gesuch persistNewGesuchInStatus(@Nonnull AntragStatus status, @Nonnull Persistence persistence, @Nonnull GesuchService gesuchService) {
		final Gesuch gesuch = TestDataUtil.createDefaultGesuch();
		gesuch.setEingangsart(Eingangsart.PAPIER);
		gesuch.setStatus(status);
		gesuch.setGesuchsperiode(persistence.persist(gesuch.getGesuchsperiode()));
		gesuch.setFall(persistence.persist(gesuch.getFall()));
		gesuch.setGesuchsteller1(TestDataUtil.createDefaultGesuchstellerContainer(gesuch));
		gesuch.getGesuchsteller1().setFinanzielleSituationContainer(TestDataUtil.createFinanzielleSituationContainer());
		gesuch.getGesuchsteller1().getFinanzielleSituationContainer().setFinanzielleSituationJA(TestDataUtil.createDefaultFinanzielleSituation());
		gesuchService.createGesuch(gesuch);
		return gesuch;
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

	@Nonnull
	public static FerieninselStammdaten createDefaultFerieninselStammdaten(@Nonnull Gesuchsperiode gesuchsperiode) {
		FerieninselStammdaten stammdaten = new FerieninselStammdaten();
		stammdaten.setFerienname(Ferienname.SOMMERFERIEN);
		stammdaten.setAnmeldeschluss(LocalDate.now().plusMonths(1));
		List<FerieninselZeitraum> zeitraumList = new ArrayList<>();
		FerieninselZeitraum zeitraum = new FerieninselZeitraum();
		zeitraum.setGueltigkeit(new DateRange(LocalDate.now().plusMonths(2), LocalDate.now().plusMonths(3)));
		zeitraumList.add(zeitraum);
		stammdaten.setZeitraumList(zeitraumList);
		stammdaten.setGesuchsperiode(gesuchsperiode);
		return stammdaten;
	}

	public static BelegungFerieninsel createDefaultBelegungFerieninsel() {
		BelegungFerieninsel belegungFerieninsel = new BelegungFerieninsel();
		belegungFerieninsel.setFerienname(Ferienname.SOMMERFERIEN);
		belegungFerieninsel.setTage(new ArrayList<>());
		belegungFerieninsel.getTage().add(createBelegungFerieninselTag(LocalDate.now().plusMonths(3)));
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
		zeitabschnitt.setBetreuungspensum(10);
		zeitabschnitt.setAnspruchberechtigtesPensum(50);
		zeitabschnitt.setEinkommensjahr(PERIODE_JAHR_1);
		zeitabschnitt.setZuSpaetEingereicht(false);
		return zeitabschnitt;
	}
}
