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

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Month;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import ch.dvbern.ebegu.dto.suchfilter.smarttable.AntragPredicateObjectDTO;
import ch.dvbern.ebegu.dto.suchfilter.smarttable.AntragSearchDTO;
import ch.dvbern.ebegu.dto.suchfilter.smarttable.AntragTableFilterDTO;
import ch.dvbern.ebegu.dto.suchfilter.smarttable.MitteilungSearchDTO;
import ch.dvbern.ebegu.dto.suchfilter.smarttable.MitteilungTableFilterDTO;
import ch.dvbern.ebegu.dto.suchfilter.smarttable.PaginationDTO;
import ch.dvbern.ebegu.dto.suchfilter.smarttable.SortDTO;
import ch.dvbern.ebegu.entities.AbstractEntity;
import ch.dvbern.ebegu.entities.Abwesenheit;
import ch.dvbern.ebegu.entities.AbwesenheitContainer;
import ch.dvbern.ebegu.entities.Adresse;
import ch.dvbern.ebegu.entities.AdresseTyp;
import ch.dvbern.ebegu.entities.AnmeldungFerieninsel;
import ch.dvbern.ebegu.entities.AnmeldungTagesschule;
import ch.dvbern.ebegu.entities.BelegungFerieninsel;
import ch.dvbern.ebegu.entities.BelegungFerieninselTag;
import ch.dvbern.ebegu.entities.BelegungTagesschule;
import ch.dvbern.ebegu.entities.BelegungTagesschuleModul;
import ch.dvbern.ebegu.entities.Benutzer;
import ch.dvbern.ebegu.entities.Berechtigung;
import ch.dvbern.ebegu.entities.Betreuung;
import ch.dvbern.ebegu.entities.Betreuungsmitteilung;
import ch.dvbern.ebegu.entities.BetreuungsmitteilungPensum;
import ch.dvbern.ebegu.entities.Betreuungspensum;
import ch.dvbern.ebegu.entities.BetreuungspensumContainer;
import ch.dvbern.ebegu.entities.Dokument;
import ch.dvbern.ebegu.entities.DokumentGrund;
import ch.dvbern.ebegu.entities.Dossier;
import ch.dvbern.ebegu.entities.Einkommensverschlechterung;
import ch.dvbern.ebegu.entities.EinkommensverschlechterungContainer;
import ch.dvbern.ebegu.entities.EinkommensverschlechterungInfo;
import ch.dvbern.ebegu.entities.EinkommensverschlechterungInfoContainer;
import ch.dvbern.ebegu.entities.Einstellung;
import ch.dvbern.ebegu.entities.EinstellungenTagesschule;
import ch.dvbern.ebegu.entities.ErweiterteBetreuung;
import ch.dvbern.ebegu.entities.ErweiterteBetreuungContainer;
import ch.dvbern.ebegu.entities.Erwerbspensum;
import ch.dvbern.ebegu.entities.ErwerbspensumContainer;
import ch.dvbern.ebegu.entities.Fachstelle;
import ch.dvbern.ebegu.entities.Fall;
import ch.dvbern.ebegu.entities.Familiensituation;
import ch.dvbern.ebegu.entities.FamiliensituationContainer;
import ch.dvbern.ebegu.entities.FinanzielleSituation;
import ch.dvbern.ebegu.entities.FinanzielleSituationContainer;
import ch.dvbern.ebegu.entities.Gemeinde;
import ch.dvbern.ebegu.entities.GemeindeStammdaten;
import ch.dvbern.ebegu.entities.GeneratedDokument;
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.entities.Gesuchsperiode;
import ch.dvbern.ebegu.entities.Gesuchsteller;
import ch.dvbern.ebegu.entities.GesuchstellerAdresse;
import ch.dvbern.ebegu.entities.GesuchstellerAdresseContainer;
import ch.dvbern.ebegu.entities.GesuchstellerContainer;
import ch.dvbern.ebegu.entities.Institution;
import ch.dvbern.ebegu.entities.InstitutionStammdaten;
import ch.dvbern.ebegu.entities.InstitutionStammdatenBetreuungsgutscheine;
import ch.dvbern.ebegu.entities.InstitutionStammdatenTagesschule;
import ch.dvbern.ebegu.entities.Kind;
import ch.dvbern.ebegu.entities.KindContainer;
import ch.dvbern.ebegu.entities.KitaxUebergangsloesungInstitutionOeffnungszeiten;
import ch.dvbern.ebegu.entities.Mahnung;
import ch.dvbern.ebegu.entities.Mandant;
import ch.dvbern.ebegu.entities.Mitteilung;
import ch.dvbern.ebegu.entities.ModulTagesschule;
import ch.dvbern.ebegu.entities.ModulTagesschuleGroup;
import ch.dvbern.ebegu.entities.PensumAusserordentlicherAnspruch;
import ch.dvbern.ebegu.entities.PensumFachstelle;
import ch.dvbern.ebegu.entities.TextRessource;
import ch.dvbern.ebegu.entities.Traegerschaft;
import ch.dvbern.ebegu.entities.UnbezahlterUrlaub;
import ch.dvbern.ebegu.entities.Verfuegung;
import ch.dvbern.ebegu.entities.VerfuegungZeitabschnitt;
import ch.dvbern.ebegu.entities.WizardStep;
import ch.dvbern.ebegu.enums.AntragStatus;
import ch.dvbern.ebegu.enums.BelegungTagesschuleModulIntervall;
import ch.dvbern.ebegu.enums.BetreuungsangebotTyp;
import ch.dvbern.ebegu.enums.Betreuungsstatus;
import ch.dvbern.ebegu.enums.DokumentGrundTyp;
import ch.dvbern.ebegu.enums.DokumentTyp;
import ch.dvbern.ebegu.enums.EinschulungTyp;
import ch.dvbern.ebegu.enums.EinstellungKey;
import ch.dvbern.ebegu.enums.EnumFamilienstatus;
import ch.dvbern.ebegu.enums.FachstelleName;
import ch.dvbern.ebegu.enums.Ferienname;
import ch.dvbern.ebegu.enums.GemeindeStatus;
import ch.dvbern.ebegu.enums.GeneratedDokumentTyp;
import ch.dvbern.ebegu.enums.Geschlecht;
import ch.dvbern.ebegu.enums.GesuchsperiodeStatus;
import ch.dvbern.ebegu.enums.IntegrationTyp;
import ch.dvbern.ebegu.enums.Kinderabzug;
import ch.dvbern.ebegu.enums.KorrespondenzSpracheTyp;
import ch.dvbern.ebegu.enums.MahnungTyp;
import ch.dvbern.ebegu.enums.MitteilungStatus;
import ch.dvbern.ebegu.enums.MitteilungTeilnehmerTyp;
import ch.dvbern.ebegu.enums.ModulTagesschuleIntervall;
import ch.dvbern.ebegu.enums.ModulTagesschuleName;
import ch.dvbern.ebegu.enums.ModulTagesschuleTyp;
import ch.dvbern.ebegu.enums.Taetigkeit;
import ch.dvbern.ebegu.enums.UserRole;
import ch.dvbern.ebegu.enums.WizardStepName;
import ch.dvbern.ebegu.enums.WizardStepStatus;
import ch.dvbern.ebegu.services.BetreuungService;
import ch.dvbern.ebegu.services.GesuchService;
import ch.dvbern.ebegu.services.InstitutionService;
import ch.dvbern.ebegu.testfaelle.AbstractTestfall;
import ch.dvbern.ebegu.testfaelle.TestFall12_Mischgesuch;
import ch.dvbern.ebegu.testfaelle.Testfall01_WaeltiDagmar;
import ch.dvbern.ebegu.testfaelle.Testfall02_FeutzYvonne;
import ch.dvbern.ebegu.testfaelle.Testfall04_WaltherLaura;
import ch.dvbern.ebegu.testfaelle.Testfall06_BeckerNora;
import ch.dvbern.ebegu.testfaelle.Testfall11_SchulamtOnly;
import ch.dvbern.ebegu.types.DateRange;
import ch.dvbern.ebegu.util.Constants;
import ch.dvbern.ebegu.util.FinanzielleSituationRechner;
import ch.dvbern.ebegu.util.KitaxUebergangsloesungParameter;
import ch.dvbern.ebegu.util.MathUtil;
import ch.dvbern.lib.cdipersistence.Persistence;
import ch.dvbern.oss.lib.beanvalidation.embeddables.IBAN;

import static ch.dvbern.ebegu.enums.EinstellungKey.ERWERBSPENSUM_ZUSCHLAG;
import static ch.dvbern.ebegu.enums.EinstellungKey.FACHSTELLE_MAX_PENSUM_SOZIALE_INTEGRATION;
import static ch.dvbern.ebegu.enums.EinstellungKey.FACHSTELLE_MAX_PENSUM_SPRACHLICHE_INTEGRATION;
import static ch.dvbern.ebegu.enums.EinstellungKey.FACHSTELLE_MIN_PENSUM_SOZIALE_INTEGRATION;
import static ch.dvbern.ebegu.enums.EinstellungKey.FACHSTELLE_MIN_PENSUM_SPRACHLICHE_INTEGRATION;
import static ch.dvbern.ebegu.enums.EinstellungKey.GEMEINDE_BG_BIS_UND_MIT_SCHULSTUFE;
import static ch.dvbern.ebegu.enums.EinstellungKey.GEMEINDE_FERIENINSEL_ANMELDUNGEN_DATUM_AB;
import static ch.dvbern.ebegu.enums.EinstellungKey.GEMEINDE_KONTINGENTIERUNG_ENABLED;
import static ch.dvbern.ebegu.enums.EinstellungKey.GEMEINDE_MAHLZEITENVERGUENSTIGUNG_EINKOMMENSSTUFE_1_MAX_EINKOMMEN;
import static ch.dvbern.ebegu.enums.EinstellungKey.GEMEINDE_MAHLZEITENVERGUENSTIGUNG_EINKOMMENSSTUFE_1_VERGUENSTIGUNG_HAUPTMAHLZEIT;
import static ch.dvbern.ebegu.enums.EinstellungKey.GEMEINDE_MAHLZEITENVERGUENSTIGUNG_EINKOMMENSSTUFE_1_VERGUENSTIGUNG_NEBENMAHLZEIT;
import static ch.dvbern.ebegu.enums.EinstellungKey.GEMEINDE_MAHLZEITENVERGUENSTIGUNG_EINKOMMENSSTUFE_2_MAX_EINKOMMEN;
import static ch.dvbern.ebegu.enums.EinstellungKey.GEMEINDE_MAHLZEITENVERGUENSTIGUNG_EINKOMMENSSTUFE_2_VERGUENSTIGUNG_HAUPTMAHLZEIT;
import static ch.dvbern.ebegu.enums.EinstellungKey.GEMEINDE_MAHLZEITENVERGUENSTIGUNG_EINKOMMENSSTUFE_2_VERGUENSTIGUNG_NEBENMAHLZEIT;
import static ch.dvbern.ebegu.enums.EinstellungKey.GEMEINDE_MAHLZEITENVERGUENSTIGUNG_EINKOMMENSSTUFE_3_VERGUENSTIGUNG_HAUPTMAHLZEIT;
import static ch.dvbern.ebegu.enums.EinstellungKey.GEMEINDE_MAHLZEITENVERGUENSTIGUNG_EINKOMMENSSTUFE_3_VERGUENSTIGUNG_NEBENMAHLZEIT;
import static ch.dvbern.ebegu.enums.EinstellungKey.GEMEINDE_MAHLZEITENVERGUENSTIGUNG_ENABLED;
import static ch.dvbern.ebegu.enums.EinstellungKey.GEMEINDE_MAHLZEITENVERGUENSTIGUNG_FUER_SOZIALHILFEBEZUEGER_ENABLED;
import static ch.dvbern.ebegu.enums.EinstellungKey.GEMEINDE_MAHLZEITENVERGUENSTIGUNG_MINIMALER_ELTERNBEITRAG_HAUPTMAHLZEIT;
import static ch.dvbern.ebegu.enums.EinstellungKey.GEMEINDE_MAHLZEITENVERGUENSTIGUNG_MINIMALER_ELTERNBEITRAG_NEBENMAHLZEIT;
import static ch.dvbern.ebegu.enums.EinstellungKey.GEMEINDE_MIN_ERWERBSPENSUM_EINGESCHULT;
import static ch.dvbern.ebegu.enums.EinstellungKey.GEMEINDE_MIN_ERWERBSPENSUM_NICHT_EINGESCHULT;
import static ch.dvbern.ebegu.enums.EinstellungKey.GEMEINDE_SCHNITTSTELLE_KITAX_ENABLED;
import static ch.dvbern.ebegu.enums.EinstellungKey.GEMEINDE_TAGESSCHULE_ANMELDUNGEN_DATUM_AB;
import static ch.dvbern.ebegu.enums.EinstellungKey.GEMEINDE_TAGESSCHULE_ERSTER_SCHULTAG;
import static ch.dvbern.ebegu.enums.EinstellungKey.GEMEINDE_TAGESSCHULE_TAGIS_ENABLED;
import static ch.dvbern.ebegu.enums.EinstellungKey.GEMEINDE_ZUSAETZLICHER_ANSPRUCH_FREIWILLIGENARBEIT_ENABLED;
import static ch.dvbern.ebegu.enums.EinstellungKey.GEMEINDE_ZUSAETZLICHER_ANSPRUCH_FREIWILLIGENARBEIT_MAXPROZENT;
import static ch.dvbern.ebegu.enums.EinstellungKey.GEMEINDE_ZUSAETZLICHER_BABYBEITRAG_BETRAG_KITA;
import static ch.dvbern.ebegu.enums.EinstellungKey.GEMEINDE_ZUSAETZLICHER_BABYBEITRAG_BETRAG_TFO;
import static ch.dvbern.ebegu.enums.EinstellungKey.GEMEINDE_ZUSAETZLICHER_BABYBEITRAG_ENABLED;
import static ch.dvbern.ebegu.enums.EinstellungKey.GEMEINDE_ZUSAETZLICHER_GUTSCHEIN_BETRAG_KITA;
import static ch.dvbern.ebegu.enums.EinstellungKey.GEMEINDE_ZUSAETZLICHER_GUTSCHEIN_BETRAG_TFO;
import static ch.dvbern.ebegu.enums.EinstellungKey.GEMEINDE_ZUSAETZLICHER_GUTSCHEIN_BIS_UND_MIT_SCHULSTUFE_KITA;
import static ch.dvbern.ebegu.enums.EinstellungKey.GEMEINDE_ZUSAETZLICHER_GUTSCHEIN_BIS_UND_MIT_SCHULSTUFE_TFO;
import static ch.dvbern.ebegu.enums.EinstellungKey.GEMEINDE_ZUSAETZLICHER_GUTSCHEIN_ENABLED;
import static ch.dvbern.ebegu.enums.EinstellungKey.MAX_MASSGEBENDES_EINKOMMEN;
import static ch.dvbern.ebegu.enums.EinstellungKey.MAX_TARIF_MIT_PAEDAGOGISCHER_BETREUUNG;
import static ch.dvbern.ebegu.enums.EinstellungKey.MAX_TARIF_OHNE_PAEDAGOGISCHER_BETREUUNG;
import static ch.dvbern.ebegu.enums.EinstellungKey.MAX_VERGUENSTIGUNG_SCHULE_PRO_STD;
import static ch.dvbern.ebegu.enums.EinstellungKey.MAX_VERGUENSTIGUNG_SCHULE_PRO_TG;
import static ch.dvbern.ebegu.enums.EinstellungKey.MAX_VERGUENSTIGUNG_VORSCHULE_BABY_PRO_STD;
import static ch.dvbern.ebegu.enums.EinstellungKey.MAX_VERGUENSTIGUNG_VORSCHULE_BABY_PRO_TG;
import static ch.dvbern.ebegu.enums.EinstellungKey.MAX_VERGUENSTIGUNG_VORSCHULE_KIND_PRO_STD;
import static ch.dvbern.ebegu.enums.EinstellungKey.MAX_VERGUENSTIGUNG_VORSCHULE_KIND_PRO_TG;
import static ch.dvbern.ebegu.enums.EinstellungKey.MIN_ERWERBSPENSUM_EINGESCHULT;
import static ch.dvbern.ebegu.enums.EinstellungKey.MIN_ERWERBSPENSUM_NICHT_EINGESCHULT;
import static ch.dvbern.ebegu.enums.EinstellungKey.MIN_MASSGEBENDES_EINKOMMEN;
import static ch.dvbern.ebegu.enums.EinstellungKey.MIN_TARIF;
import static ch.dvbern.ebegu.enums.EinstellungKey.MIN_VERGUENSTIGUNG_PRO_STD;
import static ch.dvbern.ebegu.enums.EinstellungKey.MIN_VERGUENSTIGUNG_PRO_TG;
import static ch.dvbern.ebegu.enums.EinstellungKey.OEFFNUNGSSTUNDEN_TFO;
import static ch.dvbern.ebegu.enums.EinstellungKey.OEFFNUNGSTAGE_KITA;
import static ch.dvbern.ebegu.enums.EinstellungKey.OEFFNUNGSTAGE_TFO;
import static ch.dvbern.ebegu.enums.EinstellungKey.PARAM_GRENZWERT_EINKOMMENSVERSCHLECHTERUNG;
import static ch.dvbern.ebegu.enums.EinstellungKey.PARAM_MAX_TAGE_ABWESENHEIT;
import static ch.dvbern.ebegu.enums.EinstellungKey.PARAM_PAUSCHALABZUG_PRO_PERSON_FAMILIENGROESSE_3;
import static ch.dvbern.ebegu.enums.EinstellungKey.PARAM_PAUSCHALABZUG_PRO_PERSON_FAMILIENGROESSE_4;
import static ch.dvbern.ebegu.enums.EinstellungKey.PARAM_PAUSCHALABZUG_PRO_PERSON_FAMILIENGROESSE_5;
import static ch.dvbern.ebegu.enums.EinstellungKey.PARAM_PAUSCHALABZUG_PRO_PERSON_FAMILIENGROESSE_6;
import static ch.dvbern.ebegu.enums.EinstellungKey.PARAM_PENSUM_KITA_MIN;
import static ch.dvbern.ebegu.enums.EinstellungKey.PARAM_PENSUM_TAGESELTERN_MIN;
import static ch.dvbern.ebegu.enums.EinstellungKey.PARAM_PENSUM_TAGESSCHULE_MIN;
import static ch.dvbern.ebegu.enums.EinstellungKey.ZUSCHLAG_BEHINDERUNG_PRO_STD;
import static ch.dvbern.ebegu.enums.EinstellungKey.ZUSCHLAG_BEHINDERUNG_PRO_TG;
import static ch.dvbern.ebegu.util.Constants.PAUSCHALABZUG_PRO_PERSON_FAMILIENGROESSE_3_FUER_TESTS;
import static ch.dvbern.ebegu.util.Constants.PAUSCHALABZUG_PRO_PERSON_FAMILIENGROESSE_4_FUER_TESTS;
import static ch.dvbern.ebegu.util.Constants.PAUSCHALABZUG_PRO_PERSON_FAMILIENGROESSE_5_FUER_TESTS;
import static ch.dvbern.ebegu.util.Constants.PAUSCHALABZUG_PRO_PERSON_FAMILIENGROESSE_6_FUER_TESTS;

/**
 * comments homa
 */
@SuppressWarnings("PMD.NcssTypeCount")
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
		gesuchstellerAdresse.setPlz("3014");
		gesuchstellerAdresse.setOrt("Bern");
		gesuchstellerAdresse.setGueltigkeit(new DateRange(LocalDate.now(), Constants.END_OF_TIME));
		gesuchstellerAdresse.setAdresseTyp(AdresseTyp.WOHNADRESSE);
		return gesuchstellerAdresse;
	}

	public static Adresse createDefaultAdresse() {
		Adresse adresse = new Adresse();
		adresse.setOrganisation("Jugendamt");
		adresse.setStrasse("Nussbaumstrasse");
		adresse.setHausnummer("21");
		adresse.setZusatzzeile("c/o Uwe Untermieter");
		adresse.setPlz("3014");
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
		return new Fall();
	}

	public static Dossier createDefaultDossier() {
		Dossier dossier = new Dossier();
		dossier.setFall(createDefaultFall());
		dossier.setGemeinde(createGemeindeParis());
		return dossier;
	}

	public static Mandant createDefaultMandant() {
		Mandant mandant = new Mandant();
		mandant.setId(UUID.randomUUID().toString());
		mandant.setName("Mandant1");
		return mandant;
	}

	@Nonnull
	public static Mandant getMandantKantonBern(@Nonnull Persistence persistence) {
		Mandant mandant = persistence.find(Mandant.class, AbstractTestfall.ID_MANDANT_KANTON_BERN);
		if (mandant == null) {
			mandant = new Mandant();
			mandant.setName("Kanton Bern");
			return persistence.persist(mandant);
		}
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
			gemeinde.setMandant(getMandantKantonBern(persistence));
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

	public static GemeindeStammdaten getGemeindeStammdatenParis(@Nonnull Persistence persistence) {
		Gemeinde gemeinde = persistence.find(Gemeinde.class, GEMEINDE_PARIS_ID);
		if (gemeinde == null) {
			gemeinde = createGemeindeParis();
			persistence.persist(gemeinde.getMandant());
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

	@Nonnull
	public static Gemeinde createGemeindeParis() {
		Gemeinde gemeinde = new Gemeinde();
		gemeinde.setId(GEMEINDE_PARIS_ID);
		gemeinde.setName("Paris");
		gemeinde.setStatus(GemeindeStatus.AKTIV);
		gemeinde.setGemeindeNummer(1);
		gemeinde.setBfsNummer(99998L);
		gemeinde.setMandant(createDefaultMandant());
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
		gemeinde.setMandant(createDefaultMandant());
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
		return traegerschaft;
	}

	public static Institution createDefaultInstitution() {
		Institution institution = new Institution();
		institution.setName("Institution1");
		institution.setMandant(createDefaultMandant());
		institution.setTraegerschaft(createDefaultTraegerschaft());
		return institution;
	}

	public static InstitutionStammdaten createDefaultInstitutionStammdaten() {
		return createInstitutionStammdaten(
			UUID.randomUUID().toString(),
			"Testinstitution",
			BetreuungsangebotTyp.KITA);
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
		institutionStammdatenBetreuungsgutscheine.setIban(new IBAN(iban));
		institutionStammdatenBetreuungsgutscheine.setAnzahlPlaetze(BigDecimal.TEN);
		instStammdaten.setInstitutionStammdatenBetreuungsgutscheine(institutionStammdatenBetreuungsgutscheine);
		return instStammdaten;
	}

	public static Collection<InstitutionStammdaten> saveInstitutionsstammdatenForTestfaelle(@Nonnull Persistence persistence, Gesuchsperiode gesuchsperiode) {
		final InstitutionStammdaten institutionStammdatenKitaAaregg = createInstitutionStammdatenKitaWeissenstein();
		final InstitutionStammdaten institutionStammdatenKitaBruennen = createInstitutionStammdatenKitaBruennen();
		final InstitutionStammdaten institutionStammdatenTagesfamilien = createInstitutionStammdatenTagesfamilien();
		final InstitutionStammdaten institutionStammdatenTagesschuleBruennen = createInstitutionStammdatenTagesschuleBern(gesuchsperiode);
		final InstitutionStammdaten institutionStammdatenFerieninselBruennen = createInstitutionStammdatenFerieninselGuarda();
		final Mandant mandant = createDefaultMandant();

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
			Traegerschaft found = persistence.find(Traegerschaft.class, traegerschaft.getId());
			if (found == null) {
				persistEntity(persistence, traegerschaft);
			}
		}
	}

	private static void saveMandantIfNecessary(@Nonnull Persistence persistence, @Nullable Mandant mandant) {
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
			kind.setPensumFachstelle(createDefaultPensumFachstelle());
		}
		kind.setFamilienErgaenzendeBetreuung(true);
		kind.setSprichtAmtssprache(true);
		kind.setEinschulungTyp(EinschulungTyp.VORSCHULALTER);
		return kind;
	}

	public static PensumFachstelle createDefaultPensumFachstelle() {
		PensumFachstelle pensumFachstelle = new PensumFachstelle();
		pensumFachstelle.setPensum(50);
		pensumFachstelle.setGueltigkeit(new DateRange(LocalDate.now(), Constants.END_OF_TIME));
		pensumFachstelle.setFachstelle(createDefaultFachstelle());
		pensumFachstelle.setIntegrationTyp(IntegrationTyp.SOZIALE_INTEGRATION);
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
		ErwerbspensumContainer erwerbspensumContainer = new ErwerbspensumContainer();
		Erwerbspensum erwerbspensum = new Erwerbspensum();
		erwerbspensum.setTaetigkeit(Taetigkeit.ANGESTELLT);
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
		anmeldung.setBelegungTagesschule(createDefaultBelegungTagesschule(true));
		kind.getAnmeldungenTagesschule().add(anmeldung);
		return anmeldung;
	}

	public static AnmeldungTagesschule createAnmeldungTagesschule(KindContainer kind, Gesuchsperiode gesuchsperiode) {
		AnmeldungTagesschule anmeldung = new AnmeldungTagesschule();
		anmeldung.setInstitutionStammdaten(createInstitutionStammdatenTagesschuleBern(gesuchsperiode));
		anmeldung.setBetreuungsstatus(Betreuungsstatus.SCHULAMT_ANMELDUNG_AUSGELOEST);
		anmeldung.setKind(kind);
		anmeldung.setBelegungTagesschule(createDefaultBelegungTagesschule(false));
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

	public static BelegungTagesschule createDefaultBelegungTagesschule(boolean withModulBelegung) {
		final BelegungTagesschule belegungTagesschule = new BelegungTagesschule();
		belegungTagesschule.setBemerkung("Dies ist eine Bemerkung!");
		belegungTagesschule.setEintrittsdatum(LocalDate.now());
		if (withModulBelegung) {
			belegungTagesschule.setBelegungTagesschuleModule(new TreeSet<>());

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

	public static Gesuchsperiode createAndPersistGesuchsperiode1718(Persistence persistence) {
		Gesuchsperiode gesuchsperiodeXXYY = createGesuchsperiodeXXYY(2017, 2018);
		return persistence.persist(gesuchsperiodeXXYY);
	}

	public static Gesuchsperiode createAndPersistCustomGesuchsperiode(Persistence persistence, int yearFrom, int yearTo) {
		Gesuchsperiode gesuchsperiodeXXYY = createGesuchsperiodeXXYY(yearFrom, yearTo);
		return persistence.persist(gesuchsperiodeXXYY);
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
		user.setMandant(createDefaultMandant());
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
		gesuch.setGesuchsteller1(new GesuchstellerContainer());
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
		betreuung.setInstitutionStammdaten(createDefaultInstitutionStammdaten());
		betreuung.setErweiterteBetreuungContainer(TestDataUtil.createDefaultErweiterteBetreuungContainer());
		return betreuung;
	}

	public static void calculateFinanzDaten(Gesuch gesuch) {
		if (gesuch.getGesuchsperiode() == null) {
			gesuch.setGesuchsperiode(createGesuchsperiode1718());
		}
		FinanzielleSituationRechner finanzielleSituationRechner = new FinanzielleSituationRechner();
		finanzielleSituationRechner.calculateFinanzDaten(gesuch, BigDecimal.valueOf(20));
	}

	public static Gesuch createTestgesuchDagmar() {
		List<InstitutionStammdaten> insttStammdaten = new ArrayList<>();
		insttStammdaten.add(TestDataUtil.createInstitutionStammdatenKitaBruennen());
		insttStammdaten.add(TestDataUtil.createInstitutionStammdatenKitaWeissenstein());
		Testfall01_WaeltiDagmar testfall =
			new Testfall01_WaeltiDagmar(TestDataUtil.createGesuchsperiode1718(), insttStammdaten);
		testfall.createGesuch(LocalDate.of(1980, Month.MARCH, 25));
		Gesuch gesuch = testfall.fillInGesuch();
		TestDataUtil.calculateFinanzDaten(gesuch);
		gesuch.setGesuchsperiode(TestDataUtil.createGesuchsperiode1718());
		return gesuch;
	}

	public static Gesuch createTestfall11_SchulamtOnly(){
		List<InstitutionStammdaten> insttStammdaten = new ArrayList<>();
		Gesuchsperiode gesuchsperiode = TestDataUtil.createGesuchsperiode1718();
		insttStammdaten.add(TestDataUtil.createInstitutionStammdatenTagesschuleBern(gesuchsperiode));
		insttStammdaten.add(TestDataUtil.createInstitutionStammdatenFerieninselGuarda());
		Testfall11_SchulamtOnly testfall = new Testfall11_SchulamtOnly(gesuchsperiode,
			insttStammdaten);
		testfall.createGesuch(LocalDate.of(2017, Month.MARCH, 25));
		Gesuch gesuch = testfall.fillInGesuch();
		gesuch.setGesuchsperiode(gesuchsperiode);
		return gesuch;
	}

	/**
	 * Hilfsmethode die den Testfall Waelti Dagmar erstellt und speichert
	 */
	public static Gesuch createAndPersistTestfall11_SchulamtOnly(
		@Nonnull Persistence persistence, @Nullable LocalDate eingangsdatum,
		@Nullable AntragStatus status, @Nonnull Gesuchsperiode gesuchsperiode) {

		List<InstitutionStammdaten> insttStammdaten = new ArrayList<>();
		insttStammdaten.add(TestDataUtil.createInstitutionStammdatenTagesschuleBern(gesuchsperiode));
		insttStammdaten.add(TestDataUtil.createInstitutionStammdatenFerieninselGuarda());
		Testfall11_SchulamtOnly testfall = new Testfall11_SchulamtOnly(gesuchsperiode,
			insttStammdaten);
		testfall.createGesuch(LocalDate.of(1980, Month.MARCH, 25));
		return persistAllEntities(persistence, eingangsdatum, testfall, status);
	}

	public static Gesuch createTestgesuchYvonneFeuz() {
		List<InstitutionStammdaten> insttStammdaten = new ArrayList<>();
		insttStammdaten.add(TestDataUtil.createInstitutionStammdatenKitaBruennen());
		insttStammdaten.add(TestDataUtil.createInstitutionStammdatenKitaWeissenstein());
		Testfall02_FeutzYvonne testfall =
			new Testfall02_FeutzYvonne(TestDataUtil.createGesuchsperiode1718(), insttStammdaten);
		testfall.createGesuch(LocalDate.of(1980, Month.MARCH, 25));
		Gesuch gesuch = testfall.fillInGesuch();
		TestDataUtil.calculateFinanzDaten(gesuch);
		gesuch.setGesuchsperiode(TestDataUtil.createGesuchsperiode1718());
		return gesuch;
	}

	public static Gesuch createTestgesuchLauraWalther(@Nonnull Gesuchsperiode gesuchsperiode) {
		List<InstitutionStammdaten> insttStammdaten = new ArrayList<>();
		insttStammdaten.add(TestDataUtil.createInstitutionStammdatenKitaWeissenstein());
		Testfall04_WaltherLaura testfall =
			new Testfall04_WaltherLaura(gesuchsperiode, insttStammdaten);
		testfall.createGesuch(LocalDate.of(1980, Month.MARCH, 25));
		Gesuch gesuch = testfall.fillInGesuch();
		TestDataUtil.calculateFinanzDaten(gesuch);
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

		instService.getAllInstitutionen();
		List<InstitutionStammdaten> institutionStammdatenList = new ArrayList<>();
		institutionStammdatenList.add(TestDataUtil.createInstitutionStammdatenKitaWeissenstein());
		institutionStammdatenList.add(TestDataUtil.createInstitutionStammdatenKitaBruennen());
		Testfall01_WaeltiDagmar testfall = new Testfall01_WaeltiDagmar(gesuchsperiode, institutionStammdatenList);

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
				if (betreuung.getKind().getKindJA().getPensumFachstelle() != null) {
					persistence.merge(betreuung.getKind().getKindJA().getPensumFachstelle().getFachstelle());
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
		Testfall02_FeutzYvonne testfall = new Testfall02_FeutzYvonne(gesuchsperiode, institutionStammdatenList);
		return persistAllEntities(persistence, eingangsdatum, testfall, null);
	}

	public static Gesuch createAndPersistBeckerNoraGesuch(
		 Persistence persistence, @Nullable LocalDate eingangsdatum,
		@Nullable AntragStatus status, @Nonnull Gesuchsperiode gesuchsperiode) {

		Collection<InstitutionStammdaten> institutionStammdatenList =
			saveInstitutionsstammdatenForTestfaelle(persistence, gesuchsperiode);
		Testfall06_BeckerNora testfall = new Testfall06_BeckerNora(gesuchsperiode, institutionStammdatenList);
		return persistAllEntities(persistence, eingangsdatum, testfall, status);
	}

	public static Gesuch createAndPersistASIV12(
		InstitutionService instService,
		Persistence persistence,
		@Nullable LocalDate eingangsdatum,
		AntragStatus status,
		@Nonnull Gesuchsperiode gesuchsperiode
	) {
		instService.getAllInstitutionen();
		List<InstitutionStammdaten> institutionStammdatenList = new ArrayList<>();
		institutionStammdatenList.add(TestDataUtil.createInstitutionStammdatenTagesschuleBern(gesuchsperiode));
		institutionStammdatenList.add(TestDataUtil.createInstitutionStammdatenKitaWeissenstein());
		TestFall12_Mischgesuch testfall = new TestFall12_Mischgesuch(gesuchsperiode,
			institutionStammdatenList);

		if (status != null) {
			return persistAllEntities(persistence, eingangsdatum, testfall, status);
		}
		return persistAllEntities(persistence, eingangsdatum, testfall, null);
	}

	public static Institution createAndPersistDefaultInstitution(Persistence persistence) {
		Institution inst = createDefaultInstitution();
		persistence.merge(inst.getMandant());
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
		persistence.persist(verantwortlicher.getMandant());
		persistence.persist(verantwortlicher);
		return verantwortlicher;
	}

	@Nonnull
	private static Benutzer createAndPersistBenutzer(Persistence persistence) {
		Benutzer verantwortlicher = TestDataUtil.createDefaultBenutzer();
		persistence.persist(verantwortlicher.getMandant());
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
		persistence.persist(gesuch.getGesuchsperiode());

		Set<KindContainer> kindContainers = new TreeSet<>();
		Betreuung betreuung = TestDataUtil.createDefaultBetreuung();
		KindContainer kind = betreuung.getKind();

		Set<Betreuung> betreuungen = new TreeSet<>();
		betreuungen.add(betreuung);
		kind.setBetreuungen(betreuungen);

		Objects.requireNonNull(kind.getKindGS());
		Objects.requireNonNull(kind.getKindGS().getPensumFachstelle());
		persistence.persist(kind.getKindGS().getPensumFachstelle().getFachstelle());
		Objects.requireNonNull(kind.getKindJA().getPensumFachstelle());
		persistence.persist(kind.getKindJA().getPensumFachstelle().getFachstelle());
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
		return gesuch;
	}

	public static Gesuch createAndPersistGesuch(Persistence persistence, Gemeinde gemeinde) {
		Gesuch gesuch = TestDataUtil.createDefaultGesuch();
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
		Benutzer benutzer = createAndPersistBenutzer(persistence);
		gesuch.getDossier().setGemeinde(getTestGemeinde(persistence));
		gesuch.getDossier().setVerantwortlicherBG(benutzer);
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
		saveEinstellung(PARAM_MAX_TAGE_ABWESENHEIT, "30", gesuchsperiode, persistence);
		saveEinstellung(MAX_VERGUENSTIGUNG_VORSCHULE_BABY_PRO_TG, "150", gesuchsperiode, persistence);
		saveEinstellung(MAX_VERGUENSTIGUNG_VORSCHULE_KIND_PRO_TG, "100", gesuchsperiode, persistence);
		saveEinstellung(MAX_VERGUENSTIGUNG_SCHULE_PRO_TG, "75", gesuchsperiode, persistence);
		saveEinstellung(MAX_VERGUENSTIGUNG_VORSCHULE_BABY_PRO_STD, "11.90", gesuchsperiode, persistence);
		saveEinstellung(MAX_VERGUENSTIGUNG_VORSCHULE_KIND_PRO_STD, "8.50", gesuchsperiode, persistence);
		saveEinstellung(MAX_VERGUENSTIGUNG_SCHULE_PRO_STD, "8.50", gesuchsperiode, persistence);
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
		saveEinstellung(GEMEINDE_MAHLZEITENVERGUENSTIGUNG_EINKOMMENSSTUFE_1_VERGUENSTIGUNG_HAUPTMAHLZEIT, "6",
			gesuchsperiode, persistence);
		saveEinstellung(GEMEINDE_MAHLZEITENVERGUENSTIGUNG_EINKOMMENSSTUFE_1_VERGUENSTIGUNG_NEBENMAHLZEIT, "3",
			gesuchsperiode, persistence);
		saveEinstellung(GEMEINDE_MAHLZEITENVERGUENSTIGUNG_EINKOMMENSSTUFE_1_MAX_EINKOMMEN, "50000", gesuchsperiode,
			persistence);
		saveEinstellung(GEMEINDE_MAHLZEITENVERGUENSTIGUNG_EINKOMMENSSTUFE_2_VERGUENSTIGUNG_HAUPTMAHLZEIT, "3",
			gesuchsperiode, persistence);
		saveEinstellung(GEMEINDE_MAHLZEITENVERGUENSTIGUNG_EINKOMMENSSTUFE_2_VERGUENSTIGUNG_NEBENMAHLZEIT, "1",
			gesuchsperiode, persistence);
		saveEinstellung(GEMEINDE_MAHLZEITENVERGUENSTIGUNG_EINKOMMENSSTUFE_2_MAX_EINKOMMEN, "70000", gesuchsperiode,
			persistence);
		saveEinstellung(GEMEINDE_MAHLZEITENVERGUENSTIGUNG_EINKOMMENSSTUFE_3_VERGUENSTIGUNG_HAUPTMAHLZEIT, "0",
			gesuchsperiode, persistence);
		saveEinstellung(GEMEINDE_MAHLZEITENVERGUENSTIGUNG_EINKOMMENSSTUFE_3_VERGUENSTIGUNG_NEBENMAHLZEIT, "0",
			gesuchsperiode, persistence);
		saveEinstellung(GEMEINDE_MAHLZEITENVERGUENSTIGUNG_FUER_SOZIALHILFEBEZUEGER_ENABLED, "true", gesuchsperiode,
			persistence);
		saveEinstellung(GEMEINDE_SCHNITTSTELLE_KITAX_ENABLED, "false", gesuchsperiode,
			persistence);
		saveEinstellung(GEMEINDE_MIN_ERWERBSPENSUM_NICHT_EINGESCHULT, "20", gesuchsperiode, persistence);
		saveEinstellung(GEMEINDE_MIN_ERWERBSPENSUM_EINGESCHULT, "40", gesuchsperiode, persistence);
		saveEinstellung(GEMEINDE_MAHLZEITENVERGUENSTIGUNG_MINIMALER_ELTERNBEITRAG_HAUPTMAHLZEIT, "2",
			gesuchsperiode, persistence);
		saveEinstellung(GEMEINDE_MAHLZEITENVERGUENSTIGUNG_MINIMALER_ELTERNBEITRAG_NEBENMAHLZEIT, "2", gesuchsperiode,
			persistence);
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
		return stammdaten;
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

	public static Benutzer createAndPersistJABenutzer(Persistence persistence) {
		final Mandant mandant = TestDataUtil.createDefaultMandant();
		persistence.persist(mandant);
		final Benutzer benutzer =
			TestDataUtil.createBenutzerWithDefaultGemeinde(UserRole.SACHBEARBEITER_BG, UUID.randomUUID().toString(),
				null, null, mandant, persistence, null, null);
		persistence.persist(benutzer);
		return benutzer;
	}

	public static Benutzer createAndPersistTraegerschaftBenutzer(Persistence persistence) {
		final Traegerschaft traegerschaft = TestDataUtil.createDefaultTraegerschaft();
		persistence.persist(traegerschaft);
		final Mandant mandant = TestDataUtil.createDefaultMandant();
		persistence.persist(mandant);
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
			mandant = TestDataUtil.createDefaultMandant();
			persistence.persist(mandant);
		}
		final Benutzer benutzer = TestDataUtil.createBenutzerWithDefaultGemeinde(UserRole.SUPER_ADMIN, "superadmin",
			null, null, mandant, persistence, name, vorname);
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
		Objects.requireNonNull(gesuch.getGesuchsteller1());
		final GesuchstellerAdresseContainer adresseGS1 = TestDataUtil.
			createDefaultGesuchstellerAdresseContainer(gesuch.getGesuchsteller1());
		Objects.requireNonNull(adresseGS1.getGesuchstellerAdresseJA());
		adresseGS1.getGesuchstellerAdresseJA().setNichtInGemeinde(false);
		adresseGS1.getGesuchstellerAdresseJA()
			.setGueltigkeit(new DateRange(Constants.START_OF_TIME, Constants.END_OF_TIME));
		adressen1.add(adresseGS1);
		Objects.requireNonNull(gesuch.getGesuchsteller1());
		gesuch.getGesuchsteller1().setAdressen(adressen1);

		if (gs2) {
			List<GesuchstellerAdresseContainer> adressen2 = new ArrayList<>();
			Objects.requireNonNull(gesuch.getGesuchsteller2());
			final GesuchstellerAdresseContainer adresseGS2 = TestDataUtil
				.createDefaultGesuchstellerAdresseContainer(gesuch.getGesuchsteller2());
			Objects.requireNonNull(adresseGS2.getGesuchstellerAdresseJA());
			adresseGS2.getGesuchstellerAdresseJA().setNichtInGemeinde(false);
			adresseGS2.getGesuchstellerAdresseJA()
				.setGueltigkeit(new DateRange(Constants.START_OF_TIME, Constants.END_OF_TIME));
			adressen2.add(adresseGS2);
			Objects.requireNonNull(gesuch.getGesuchsteller2());
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
		Objects.requireNonNull(betreuung.getKind().getKindGS().getPensumFachstelle());
		Objects.requireNonNull(betreuung.getKind().getKindJA().getPensumFachstelle());
		persistence.persist(betreuung.getKind().getKindGS().getPensumFachstelle().getFachstelle());
		persistence.persist(betreuung.getKind().getKindJA().getPensumFachstelle().getFachstelle());

		Gesuch gesuch = TestDataUtil.createAndPersistGesuch(persistence, null, null, gesuchsperiode);
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
		Objects.requireNonNull(betreuung.getKind().getKindGS().getPensumFachstelle());
		Objects.requireNonNull(betreuung.getKind().getKindJA().getPensumFachstelle());
		persistence.persist(betreuung.getKind().getKindGS().getPensumFachstelle().getFachstelle());
		persistence.persist(betreuung.getKind().getKindJA().getPensumFachstelle().getFachstelle());

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
}
