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

package ch.dvbern.ebegu.services;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.ejb.Local;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import ch.dvbern.ebegu.entities.AbstractEntity_;
import ch.dvbern.ebegu.entities.Adresse;
import ch.dvbern.ebegu.entities.Auszahlungsdaten;
import ch.dvbern.ebegu.entities.Benutzer;
import ch.dvbern.ebegu.entities.Berechtigung;
import ch.dvbern.ebegu.entities.Betreuung;
import ch.dvbern.ebegu.entities.Betreuung_;
import ch.dvbern.ebegu.entities.Fall;
import ch.dvbern.ebegu.entities.Gemeinde;
import ch.dvbern.ebegu.entities.GemeindeStammdaten;
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.entities.Gesuchsperiode;
import ch.dvbern.ebegu.entities.Institution;
import ch.dvbern.ebegu.entities.InstitutionStammdaten;
import ch.dvbern.ebegu.entities.InstitutionStammdatenBetreuungsgutscheine;
import ch.dvbern.ebegu.entities.Mandant;
import ch.dvbern.ebegu.entities.Traegerschaft;
import ch.dvbern.ebegu.enums.BetreuungsangebotTyp;
import ch.dvbern.ebegu.enums.Betreuungsstatus;
import ch.dvbern.ebegu.enums.Eingangsart;
import ch.dvbern.ebegu.enums.ErrorCodeEnum;
import ch.dvbern.ebegu.enums.GemeindeStatus;
import ch.dvbern.ebegu.enums.GesuchDeletionCause;
import ch.dvbern.ebegu.enums.InstitutionStatus;
import ch.dvbern.ebegu.enums.KorrespondenzSpracheTyp;
import ch.dvbern.ebegu.enums.UserRole;
import ch.dvbern.ebegu.enums.WizardStepName;
import ch.dvbern.ebegu.errors.EbeguEntityNotFoundException;
import ch.dvbern.ebegu.errors.EbeguRuntimeException;
import ch.dvbern.ebegu.testfaelle.AbstractTestfall;
import ch.dvbern.ebegu.testfaelle.Testfall01_WaeltiDagmar;
import ch.dvbern.ebegu.testfaelle.Testfall02_FeutzYvonne;
import ch.dvbern.ebegu.testfaelle.Testfall03_PerreiraMarcia;
import ch.dvbern.ebegu.testfaelle.Testfall04_WaltherLaura;
import ch.dvbern.ebegu.testfaelle.Testfall05_LuethiMeret;
import ch.dvbern.ebegu.testfaelle.Testfall06_BeckerNora;
import ch.dvbern.ebegu.testfaelle.Testfall07_MeierMeret;
import ch.dvbern.ebegu.types.DateRange;
import ch.dvbern.ebegu.util.Constants;
import ch.dvbern.ebegu.util.FreigabeCopyUtil;
import ch.dvbern.lib.cdipersistence.Persistence;
import ch.dvbern.oss.lib.beanvalidation.embeddables.IBAN;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Service fuer erstellen und mutieren von Schulungsdaten
 */
@SuppressWarnings({ "DLS_DEAD_LOCAL_STORE", "DM_CONVERT_CASE", "EI_EXPOSE_REP", "ConstantNamingConvention",
	"SpringAutowiredFieldsWarningInspection" })
@Stateless
@Local(SchulungService.class)
public class SchulungServiceBean extends AbstractBaseService implements SchulungService {

	private static final Logger LOG = LoggerFactory.getLogger(SchulungServiceBean.class);
	private static final Random RANDOM = new Random();
	private static final Pattern XX = Pattern.compile("XX");
	private static final String EXAMPLE_COM = "@example.com";

	private static final String GEMEINDE_TUTORIAL_ID = "11111111-1111-4444-4444-111111111111";
	private static final String GEMEINDE_STAMMDATEN_TUTORIAL_ID = "11111111-1111-4444-4444-111111111112";

	private static final String TRAEGERSCHAFT_FISCH_ID = "11111111-1111-1111-1111-111111111111";

	private static final String INSTITUTION_FORELLE_ID = "22222222-1111-1111-1111-111111111111";
	private static final String INSTITUTION_HECHT_ID = "22222222-1111-1111-1111-222222222222";
	private static final String INSTITUTION_LACHS_ID = "22222222-1111-1111-1111-333333333333";
	private static final String INSTITUTION_TUTORIAL_ID = "22222222-1111-1111-1111-444444444444";

	private static final String KITA_FORELLE_ID = "33333333-1111-1111-1111-111111111111";
	private static final String TAGESELTERN_FORELLE_ID = "33333333-1111-1111-2222-111111111111";
	private static final String KITA_HECHT_ID = "33333333-1111-1111-1111-222222222222";
	private static final String KITA_TUTORIAL_ID = "33333333-1111-1111-1111-444444444444";
	private static final String KITA_BRUENNEN_STAMMDATEN_ID = "9a0eb656-b6b7-4613-8f55-4e0e4720455e";

	private static final String GESUCH_ID = "44444444-1111-1111-1111-1111111111XX";

	private static final String BENUTZER_TUTORIAL_GEMEINDE_USERNAME = "tust";
	private static final String BENUTZER_FISCH_USERNAME = "sch20";
	private static final String BENUTZER_FORELLE_USERNAME = "sch21";
	private static final String BENUTZER_FISCH_NAME = "Fisch";
	private static final String BENUTZER_FISCH_VORNAME = "Fritz";
	private static final String BENUTZER_FORELLE_NAME = "Forelle";
	private static final String BENUTZER_FORELLE_VORNAME = "Franz";
	private static final String BENUTZER_TUTORIAL_GEMEINDE_NAME = "Tutorial";
	private static final String BENUTZER_TUTORIAL_GEMEINDE_VORNAME = "Gerlinde";

	private static final String GESUCHSTELLER_VORNAME = "Sandra";
	private static final String[] GESUCHSTELLER_LIST = { "Huber",
		"Müller",
		"Gerber",
		"Antonelli",
		"Schüpbach",
		"Kovac",
		"Ackermann",
		"Keller",
		"Wyttenbach",
		"Rindlisbacher",
		"Dubois",
		"Menet",
		"Burri",
		"Schmid",
		"Rodriguez",
		"Nussbaum" };


	@Inject
	private GesuchService gesuchService;

	@Inject
	private MandantService mandantService;

	@Inject
	private TraegerschaftService traegerschaftService;

	@Inject
	private InstitutionService institutionService;

	@Inject
	private InstitutionStammdatenService institutionStammdatenService;

	@Inject
	private BenutzerService benutzerService;

	@Inject
	private TestfaelleService testfaelleService;

	@Inject
	private GesuchsperiodeService gesuchsperiodeService;

	@Inject
	private FallService fallService;

	@Inject
	private WizardStepService wizardStepService;

	@Inject
	private GemeindeService gemeindeService;

	@Inject
	private Persistence persistence;

	@Override
	public void resetSchulungsdaten() {
		LOG.info("Lösche Schulungsdaten... ");
		deleteSchulungsdaten();
		LOG.info("Erstelle Schulungsdaten...");
		createSchulungsdaten();
		LOG.info("... beendet");
	}

	@Override
	public void deleteSchulungsdaten() {

		removeFaelleForSuche();

		for (int i = 0; i < GESUCHSTELLER_LIST.length; i++) {
			removeGesucheFallAndBenutzer(i + 1);
		}

		// Bevor die Testinstitutionen geloescht werden, muss sichergestellt sein, dass diese von keinen "normalen"
		// Testfaellen verwendet werden -> auf Kita Brünnen umbiegen
		Optional<InstitutionStammdaten> institutionStammdatenOptional = institutionStammdatenService.findInstitutionStammdaten(KITA_BRUENNEN_STAMMDATEN_ID);
		if (institutionStammdatenOptional.isPresent()) {
			InstitutionStammdaten institutionStammdaten = institutionStammdatenOptional.get();
			assertInstitutionNotUsedInNormalenGesuchen(KITA_FORELLE_ID, institutionStammdaten);
			assertInstitutionNotUsedInNormalenGesuchen(TAGESELTERN_FORELLE_ID, institutionStammdaten);
			assertInstitutionNotUsedInNormalenGesuchen(KITA_HECHT_ID, institutionStammdaten);
		}

		removeBenutzer(BENUTZER_FISCH_USERNAME);
		removeBenutzer(BENUTZER_FORELLE_USERNAME);

		if (institutionService.findInstitution(INSTITUTION_FORELLE_ID, true).isPresent()) {
			institutionService.removeInstitution(INSTITUTION_FORELLE_ID);
		}
		if (institutionService.findInstitution(INSTITUTION_HECHT_ID, true).isPresent()) {
			institutionService.removeInstitution(INSTITUTION_HECHT_ID);
		}
		if (institutionService.findInstitution(INSTITUTION_LACHS_ID, true).isPresent()) {
			institutionService.removeInstitution(INSTITUTION_LACHS_ID);
		}
		if (traegerschaftService.findTraegerschaft(TRAEGERSCHAFT_FISCH_ID).isPresent()) {
			traegerschaftService.removeTraegerschaft(TRAEGERSCHAFT_FISCH_ID);
		}
	}

	@Override
	public void createSchulungsdaten() {
		// TODO wir sollten fuer die Schulung auch eine Gemeinde auswaehlen (sollte bei der Umsetzung von Schulung geaendert werden)
		Gemeinde gemeinde = gemeindeService.getAktiveGemeinden().stream().findFirst()
			.orElseThrow(() -> new EbeguEntityNotFoundException("createSchulungsdaten", ErrorCodeEnum.ERROR_ENTITY_NOT_FOUND));
		Traegerschaft traegerschaftFisch = createTraegerschaft(TRAEGERSCHAFT_FISCH_ID, "Fisch");

		Institution institutionForelle = createInstitution(INSTITUTION_FORELLE_ID, "Forelle", traegerschaftFisch);
		Institution institutionHecht = createInstitution(INSTITUTION_HECHT_ID, "Hecht", traegerschaftFisch);
		Institution institutionLachs = createInstitution(INSTITUTION_LACHS_ID, "Lachs", traegerschaftFisch);

		InstitutionStammdaten kitaForelle = createInstitutionStammdaten(
			KITA_FORELLE_ID,
			institutionForelle,
			BetreuungsangebotTyp.KITA,
			institutionForelle.getName() + EXAMPLE_COM);
		InstitutionStammdaten tageselternForelle = createInstitutionStammdaten(
			TAGESELTERN_FORELLE_ID,
			institutionLachs,
			BetreuungsangebotTyp.TAGESFAMILIEN,
			institutionLachs.getName() + EXAMPLE_COM);
		InstitutionStammdaten kitaHecht = createInstitutionStammdaten(
			KITA_HECHT_ID,
			institutionHecht,
			BetreuungsangebotTyp.KITA,
			institutionHecht.getName() + EXAMPLE_COM);

		List<InstitutionStammdaten> institutionenForSchulung = new LinkedList<>();
		institutionenForSchulung.add(kitaForelle);
		institutionenForSchulung.add(tageselternForelle);
		institutionenForSchulung.add(kitaHecht);

		createBenutzer(BENUTZER_FISCH_NAME, BENUTZER_FISCH_VORNAME, traegerschaftFisch, null, null, BENUTZER_FISCH_USERNAME);
		createBenutzer(BENUTZER_FORELLE_NAME, BENUTZER_FORELLE_VORNAME, null, institutionForelle, null, BENUTZER_FORELLE_USERNAME);

		for (int i = 0; i < GESUCHSTELLER_LIST.length; i++) {
			createGesuchsteller(GESUCHSTELLER_LIST[i], getUsername(i + 1));
		}
		createFaelleForSuche(institutionenForSchulung, gemeinde);
	}

	@Override
	public void createTutorialdaten() {
		LOG.info("Erstelle Tutorialdaten...");

		Gemeinde gemeinde = createGemeindeTutorial();
		GemeindeStammdaten gemeindeStammdaten = createGemeindeStammdatenTutorial(gemeinde);

		Institution institutionTutorial = createInstitution(INSTITUTION_TUTORIAL_ID, "Kita kiBon", null);
		createInstitutionStammdaten(
			KITA_TUTORIAL_ID,
			institutionTutorial,
			BetreuungsangebotTyp.KITA,
			"kita.kibon" + EXAMPLE_COM);

		final Benutzer gemeindeBenutzer = createBenutzer(
			BENUTZER_TUTORIAL_GEMEINDE_NAME, BENUTZER_TUTORIAL_GEMEINDE_VORNAME,
			null,
			null,
			Stream.of(gemeinde).collect(Collectors.toSet()),
			BENUTZER_TUTORIAL_GEMEINDE_USERNAME
		);

		setUserAsDefaultVerantwortlicher(gemeindeStammdaten, gemeindeBenutzer);

		LOG.info("... beendet");
	}

	private void setUserAsDefaultVerantwortlicher(
		@Nonnull GemeindeStammdaten gemeindeStammdaten,
		@Nonnull Benutzer gemeindeBenutzer
	) {
		gemeindeStammdaten.setDefaultBenutzerBG(gemeindeBenutzer);
		gemeindeStammdaten.setDefaultBenutzerTS(gemeindeBenutzer);
		gemeindeService.saveGemeindeStammdaten(gemeindeStammdaten);
	}

	private Gemeinde createGemeindeTutorial() {
		Mandant mandant = mandantService.getFirst();
		Gemeinde gemeinde = new Gemeinde();
		gemeinde.setId(GEMEINDE_TUTORIAL_ID);
		gemeinde.setBfsNummer(1L);
		gemeinde.setMandant(mandant);
		gemeinde.setBetreuungsgutscheineStartdatum(Constants.START_OF_TIME);
		gemeinde.setTagesschulanmeldungenStartdatum(LocalDate.of(2018,8,1));
		gemeinde.setFerieninselanmeldungenStartdatum(LocalDate.of(2018,8,1));
		gemeinde.setName("Gemeinde kiBon");
		gemeinde.setStatus(GemeindeStatus.AKTIV);
		gemeinde.setBfsNummer(5555L); // this BFS-number cannot exist
		gemeinde.setGemeindeNummer(5555); // this number cannot exist

		return gemeindeService.createGemeinde(gemeinde);
	}

	private GemeindeStammdaten createGemeindeStammdatenTutorial(@Nonnull Gemeinde gemeinde) {
		GemeindeStammdaten stammdaten = new GemeindeStammdaten();
		stammdaten.setId(GEMEINDE_STAMMDATEN_TUTORIAL_ID);
		stammdaten.setGemeinde(gemeinde);
		stammdaten.setKontoinhaber("Tutorial");
		stammdaten.setBic("XXXXCH22");
		stammdaten.setIban(new IBAN("CH9300762011623852957"));
		stammdaten.setAdresse(createAdresse(stammdaten.getId()));
		stammdaten.setKorrespondenzsprache(KorrespondenzSpracheTyp.DE_FR);
		stammdaten.setMail("gemeinde@example.com");
		stammdaten.setTelefon("0789256896");
		stammdaten.setWebseite("www.tutorialgemeinde.ch");

		try {
			final InputStream logo = SchulungServiceBean.class.getResourceAsStream("/schulung/logo-kibon-bern.png");
			final byte[] gemeindeLogo = IOUtils.toByteArray(logo);
			stammdaten.setLogoContent(gemeindeLogo);
		} catch (IOException e) {
			LOG.info("Logo for Tutorial couldnot be added to Gemeinde");
		}

		stammdaten.setBeschwerdeAdresse(null);

		return gemeindeService.saveGemeindeStammdaten(stammdaten);
	}

	@Override
	@Nonnull
	public String[] getSchulungBenutzer() {
		//noinspection SuspiciousArrayCast
		String[] clone = (String[]) ArrayUtils.clone(GESUCHSTELLER_LIST);
		List<String> list = Arrays.asList(clone);
		Collections.sort(list);
		return list.toArray(new String[list.size()]);
	}

	@SuppressWarnings("SameParameterValue")
	@Nonnull
	private Traegerschaft createTraegerschaft(@Nonnull String id, @Nonnull String name) {
		Traegerschaft traegerschaft = new Traegerschaft();
		traegerschaft.setId(id);
		traegerschaft.setName(name);
		return traegerschaftService.saveTraegerschaft(traegerschaft);
	}

	@Nonnull
	private Institution createInstitution(
		@Nonnull String id,
		@Nonnull String name,
		@Nullable Traegerschaft traegerschaft
	) {

		Mandant mandant = mandantService.getFirst();
		Institution institution = new Institution();
		institution.setId(id);
		institution.setName(name);
		institution.setMandant(mandant);
		institution.setStatus(InstitutionStatus.AKTIV);
		if (traegerschaft != null) {
			institution.setTraegerschaft(traegerschaft);
		}
		return institutionService.createInstitution(institution);
	}

	@SuppressWarnings("MagicNumber")
	@Nonnull
	private InstitutionStammdaten createInstitutionStammdaten(
		@Nonnull String id,
		@Nonnull Institution institution,
		@Nonnull BetreuungsangebotTyp betreuungsangebotTyp,
		@Nonnull String email
	) {
		InstitutionStammdaten instStammdaten = new InstitutionStammdaten();
		instStammdaten.setId(id);
		instStammdaten.setGueltigkeit(Constants.DEFAULT_GUELTIGKEIT);
		instStammdaten.setBetreuungsangebotTyp(betreuungsangebotTyp);
		instStammdaten.setAdresse(createAdresse(id));
		instStammdaten.setInstitution(institution);
		instStammdaten.setMail(email);
		InstitutionStammdatenBetreuungsgutscheine institutionStammdatenBetreuungsgutscheine = new InstitutionStammdatenBetreuungsgutscheine();
		institutionStammdatenBetreuungsgutscheine.setAnzahlPlaetze(BigDecimal.TEN);
		Auszahlungsdaten auszahlungsdaten = new Auszahlungsdaten();
		auszahlungsdaten.setIban(new IBAN("CH39 0900 0000 3066 3817 2"));
		auszahlungsdaten.setKontoinhaber("DvBern");
		institutionStammdatenBetreuungsgutscheine.setAuszahlungsdaten(auszahlungsdaten);
		instStammdaten.setInstitutionStammdatenBetreuungsgutscheine(institutionStammdatenBetreuungsgutscheine);
		return institutionStammdatenService.saveInstitutionStammdaten(instStammdaten);
	}

	@Nonnull
	private Adresse createAdresse(@Nonnull String id) {
		Adresse adresse = new Adresse();
		adresse.setId(id);
		adresse.setOrganisation("DvBern");
		adresse.setStrasse("Nussbaumstrasse");
		adresse.setHausnummer("21");
		adresse.setPlz("3014");
		adresse.setOrt("Bern");
		adresse.setGueltigkeit(new DateRange(LocalDate.now(), Constants.END_OF_TIME));
		return adresse;
	}

	private void createGesuchsteller(@Nonnull String name, @Nonnull String username) {
		Mandant mandant = mandantService.getFirst();
		Benutzer benutzer = new Benutzer();
		benutzer.setVorname(GESUCHSTELLER_VORNAME);
		benutzer.setNachname(name);
		Berechtigung berechtigung = new Berechtigung();
		berechtigung.setRole(UserRole.GESUCHSTELLER);
		berechtigung.setBenutzer(benutzer);
		benutzer.getBerechtigungen().add(berechtigung);
		benutzer.setEmail(GESUCHSTELLER_VORNAME.toLowerCase(Locale.GERMAN) + '.' + name.toLowerCase(Locale.GERMAN) + EXAMPLE_COM);
		benutzer.setUsername(username);
		benutzer.setMandant(mandant);
		benutzerService.saveBenutzer(benutzer);
	}

	@Nonnull
	private Benutzer createBenutzer(
		@Nonnull String name,
		@Nonnull String vorname,
		@Nullable Traegerschaft traegerschaft,
		@Nullable Institution institution,
		@Nullable Set<Gemeinde> gemeinden,
		@Nonnull String username
	) {

		Mandant mandant = mandantService.getFirst();
		Benutzer benutzer = new Benutzer();
		benutzer.setVorname(vorname);
		benutzer.setNachname(name);
		Berechtigung berechtigung = new Berechtigung();
		berechtigung.setBenutzer(benutzer);

		if (traegerschaft != null) {
			berechtigung.setRole(UserRole.SACHBEARBEITER_TRAEGERSCHAFT);
			berechtigung.setTraegerschaft(traegerschaft);
		}
		if (institution != null) {
			berechtigung.setRole(UserRole.SACHBEARBEITER_INSTITUTION);
			berechtigung.setInstitution(institution);
		}
		if (gemeinden != null && !gemeinden.isEmpty()) {
			berechtigung.setRole(UserRole.ADMIN_BG);
			berechtigung.setGemeindeList(gemeinden);
		}
		benutzer.getBerechtigungen().add(berechtigung);
		benutzer.setEmail(vorname.toLowerCase(Locale.GERMAN) + '.' + name.toLowerCase(Locale.GERMAN) + EXAMPLE_COM);
		benutzer.setUsername(username);
		benutzer.setMandant(mandant);
		return benutzerService.saveBenutzer(benutzer);
	}

	@Nonnull
	@SuppressWarnings("DM_CONVERT_CASE")
	private String getUsername(int position) {
		return "sch" + String.format("%02d", position);
	}

	private void removeGesucheFallAndBenutzer(int position) {
		testfaelleService.removeGesucheOfGS(getUsername(position));
		removeBenutzer(getUsername(position));
	}

	private void createFaelleForSuche(@Nonnull List<InstitutionStammdaten> institutionenForSchulung, @Nonnull Gemeinde gemeinde) {
		Gesuchsperiode gesuchsperiode = gesuchsperiodeService.getAllActiveGesuchsperioden().iterator().next();
		List<InstitutionStammdaten> institutionenForTestfall = testfaelleService.getInstitutionsstammdatenForTestfaelle();

		createFall(Testfall01_WaeltiDagmar.class, gesuchsperiode, institutionenForTestfall, gemeinde, "01", null, null, institutionenForSchulung, true);
		createFall(Testfall02_FeutzYvonne.class, gesuchsperiode, institutionenForTestfall, gemeinde, "02", null, null, institutionenForSchulung);
		createFall(Testfall03_PerreiraMarcia.class, gesuchsperiode, institutionenForTestfall, gemeinde, "03", null, null, institutionenForSchulung);
		createFall(Testfall04_WaltherLaura.class, gesuchsperiode, institutionenForTestfall, gemeinde, "04", null, null, institutionenForSchulung);
		createFall(Testfall05_LuethiMeret.class, gesuchsperiode, institutionenForTestfall, gemeinde, "05", null, null, institutionenForSchulung);
		createFall(Testfall06_BeckerNora.class, gesuchsperiode, institutionenForTestfall, gemeinde, "06", null, null, institutionenForSchulung);
		createFall(Testfall07_MeierMeret.class, gesuchsperiode, institutionenForTestfall, gemeinde, "07", null, null, institutionenForSchulung);

		createFall(Testfall01_WaeltiDagmar.class, gesuchsperiode, institutionenForTestfall, gemeinde, "08", "Gerber", "Milena", institutionenForSchulung);
		createFall(Testfall02_FeutzYvonne.class, gesuchsperiode, institutionenForTestfall, gemeinde, "09", "Bernasconi", "Claudia", institutionenForSchulung);
		createFall(Testfall03_PerreiraMarcia.class, gesuchsperiode, institutionenForTestfall, gemeinde, "10", "Odermatt", "Yasmin", institutionenForSchulung);
		createFall(Testfall04_WaltherLaura.class, gesuchsperiode, institutionenForTestfall, gemeinde, "11", "Hefti", "Sarah", institutionenForSchulung);
		createFall(Testfall05_LuethiMeret.class, gesuchsperiode, institutionenForTestfall, gemeinde, "12", "Schmid", "Natalie", institutionenForSchulung);
		createFall(Testfall06_BeckerNora.class, gesuchsperiode, institutionenForTestfall, gemeinde, "13", "Kälin", "Judith", institutionenForSchulung);
		createFall(Testfall07_MeierMeret.class, gesuchsperiode, institutionenForTestfall, gemeinde, "14", "Werlen", "Franziska", institutionenForSchulung);

		createFall(Testfall01_WaeltiDagmar.class, gesuchsperiode, institutionenForTestfall, gemeinde, "15", "Iten", "Joy", institutionenForSchulung);
		createFall(Testfall02_FeutzYvonne.class, gesuchsperiode, institutionenForTestfall, gemeinde, "16", "Keller", "Birgit", institutionenForSchulung);
		createFall(Testfall03_PerreiraMarcia.class, gesuchsperiode, institutionenForTestfall, gemeinde, "17", "Hofer", "Melanie", institutionenForSchulung);
		createFall(Testfall04_WaltherLaura.class, gesuchsperiode, institutionenForTestfall, gemeinde, "18", "Steiner", "Stefanie", institutionenForSchulung);
		createFall(Testfall05_LuethiMeret.class, gesuchsperiode, institutionenForTestfall, gemeinde, "19", "Widmer", "Ursula", institutionenForSchulung);
		createFall(Testfall06_BeckerNora.class, gesuchsperiode, institutionenForTestfall, gemeinde, "20", "Graf", "Anna", institutionenForSchulung);
		createFall(Testfall07_MeierMeret.class, gesuchsperiode, institutionenForTestfall, gemeinde, "21", "Zimmermann", "Katrin", institutionenForSchulung);

		createFall(Testfall02_FeutzYvonne.class, gesuchsperiode, institutionenForTestfall, gemeinde, "22", "Hofstetter", "Anneliese", institutionenForSchulung);
		createFall(Testfall03_PerreiraMarcia.class, gesuchsperiode, institutionenForTestfall, gemeinde, "23", "Arnold", "Madeleine", institutionenForSchulung);
		createFall(Testfall04_WaltherLaura.class, gesuchsperiode, institutionenForTestfall, gemeinde, "24", "Schneebeli", "Janine", institutionenForSchulung);
		createFall(Testfall05_LuethiMeret.class, gesuchsperiode, institutionenForTestfall, gemeinde, "25", "Weber", "Marianne", institutionenForSchulung);
	}

	@SuppressFBWarnings("REC_CATCH_EXCEPTION")
	private void createFall(@Nonnull Class<? extends AbstractTestfall> classTestfall,
		@Nonnull Gesuchsperiode gesuchsperiode, @Nonnull List<InstitutionStammdaten> institutionenForTestfall, @Nonnull Gemeinde gemeinde,
		@Nonnull String id, @Nullable String nachname, @Nullable String vorname,
		@Nonnull List<InstitutionStammdaten> institutionenForSchulung, boolean noRandom) {

		@SuppressWarnings("DuplicateBooleanBranch")  // Damit VERFUEGT nicht zu haeufig...
			boolean verfuegen = RANDOM.nextBoolean() && RANDOM.nextBoolean();
		if (noRandom) {
			verfuegen = true;
		}
		AbstractTestfall testfall = null;
		try {
			testfall = classTestfall.getConstructor(Gesuchsperiode.class, Collection.class, Boolean.TYPE, Gemeinde.class).newInstance(gesuchsperiode,
				institutionenForTestfall, verfuegen, gemeinde);
			testfall.setFixId(XX.matcher(GESUCH_ID).replaceAll(id));
			Gesuch gesuch = createFallForSuche(testfall, nachname, vorname, institutionenForSchulung, verfuegen, noRandom);
			FreigabeCopyUtil.copyForFreigabe(gesuch);
			gesuchService.updateGesuch(gesuch, false, null);
		} catch (Exception e) {
			throw new EbeguRuntimeException("createFall", "Could not create Testfall " + classTestfall.getSimpleName(), e,
				classTestfall.getSimpleName());
		}
	}

	private void createFall(@Nonnull Class<? extends AbstractTestfall> classTestfall,
		@Nonnull Gesuchsperiode gesuchsperiode, @Nonnull List<InstitutionStammdaten> institutionenForTestfall, @Nonnull Gemeinde gemeinde,
		@Nonnull String id, @Nullable String nachname, @Nullable String vorname,
		@Nonnull List<InstitutionStammdaten> institutionenForSchulung) {

		createFall(classTestfall, gesuchsperiode, institutionenForTestfall, gemeinde, id, nachname, vorname, institutionenForSchulung, false);
	}

	@SuppressWarnings("ConstantConditions")
	private Gesuch createFallForSuche(@Nonnull AbstractTestfall testfall, @Nullable String nachname,
		@Nullable String vorname, @Nonnull List<InstitutionStammdaten> institutionenForSchulung,
		boolean verfuegen, boolean noRandom) {

		Gesuch gesuch = createFallForSuche(testfall, institutionenForSchulung, verfuegen, noRandom);
		if (StringUtils.isNotEmpty(nachname)) {
			gesuch.getGesuchsteller1().getGesuchstellerJA().setNachname(nachname);
		}
		if (StringUtils.isNotEmpty(vorname)) {
			gesuch.getGesuchsteller1().getGesuchstellerJA().setVorname(vorname);
		}
		return gesuchService.updateGesuch(gesuch, false, null);
	}

	@Nonnull
	private Gesuch createFallForSuche(@Nonnull AbstractTestfall testfall,
		@Nonnull List<InstitutionStammdaten> institutionenForSchulung, boolean verfuegen, boolean noRandom) {

		Gesuch gesuch = testfaelleService.createAndSaveGesuch(testfall, verfuegen, null);
		gesuch.setEingangsdatum(LocalDate.now());

		// Gesuch entweder online oder papier
		boolean online = RANDOM.nextBoolean();
		Eingangsart eingangsart = online ? Eingangsart.ONLINE : Eingangsart.PAPIER;
		gesuch.setEingangsart(eingangsart);

		// Institutionen anpassen
		List<Betreuung> betreuungList = gesuch.extractAllBetreuungen();
		for (Betreuung betreuung : betreuungList) {
			if (noRandom) {
				if (betreuung.getBetreuungNummer() == 1) {
					InstitutionStammdaten institutionStammdaten = institutionenForSchulung.get(2);
					betreuung.setInstitutionStammdaten(institutionStammdaten);
				} else {
					InstitutionStammdaten institutionStammdaten = institutionenForSchulung.get(0);
					betreuung.setInstitutionStammdaten(institutionStammdaten);
				}
				betreuung.setBetreuungsstatus(Betreuungsstatus.VERFUEGT);
			} else {
				InstitutionStammdaten institutionStammdaten = institutionenForSchulung.get(RANDOM.nextInt(institutionenForSchulung.size()));
				betreuung.setInstitutionStammdaten(institutionStammdaten);
				if (verfuegen) {
					betreuung.setBetreuungsstatus(Betreuungsstatus.VERFUEGT);
				} else {
					// Etwas haeufiger WARTEN als BESTAETIGT/ABGELEHNT
					Betreuungsstatus[] statussis = { Betreuungsstatus.WARTEN, Betreuungsstatus.WARTEN, Betreuungsstatus.WARTEN, Betreuungsstatus.BESTAETIGT, Betreuungsstatus.ABGEWIESEN };
					Betreuungsstatus status = Collections.unmodifiableList(Arrays.asList(statussis)).get(RANDOM.nextInt(statussis.length));
					betreuung.setBetreuungsstatus(status);
					if (Betreuungsstatus.ABGEWIESEN == status) {
						betreuung.setGrundAblehnung("Abgelehnt");
					}
				}
			}
		}
		Gesuch savedGesuch = gesuchService.updateGesuch(gesuch, false, null);
		if (verfuegen) {
			wizardStepService.updateSteps(savedGesuch.getId(), null, null, WizardStepName.VERFUEGEN);
		}
		return savedGesuch;
	}

	private void removeBenutzer(@Nonnull String username) {
		if (benutzerService.findBenutzer(username).isPresent()) {
			benutzerService.removeBenutzer(username);
		}
	}

	@SuppressWarnings("MagicNumber")
	private void removeFaelleForSuche() {
		int anzahlFaelle = 25;
		for (int i = 1; i <= anzahlFaelle; i++) {
			String id = XX.matcher(GESUCH_ID).replaceAll(StringUtils.leftPad(String.valueOf(i), 2, "0"));
			Optional<Gesuch> gesuchOptional = gesuchService.findGesuch(id);
			if (gesuchOptional.isPresent()) {
				final Optional<Fall> fall = fallService.findFall(gesuchOptional.get().getFall().getId());
				// Fall und seine abhaengigen Gesuche loeschen
				fall.ifPresent(fall1 -> fallService.removeFall(fall1, GesuchDeletionCause.USER));
			}
		}
	}

	private void assertInstitutionNotUsedInNormalenGesuchen(@Nonnull String institutionId,
		@Nonnull InstitutionStammdaten toReplace) {
		final CriteriaBuilder cb = persistence.getCriteriaBuilder();
		final CriteriaQuery<Betreuung> query = cb.createQuery(Betreuung.class);
		Root<Betreuung> root = query.from(Betreuung.class);
		Join<Betreuung, InstitutionStammdaten> join = root.join(Betreuung_.institutionStammdaten, JoinType.LEFT);

		query.select(root);
		Predicate idPred = cb.equal(join.get(AbstractEntity_.id), institutionId);
		query.where(idPred);
		List<Betreuung> criteriaResults = persistence.getCriteriaResults(query);
		for (Betreuung betreuung : criteriaResults) {
			betreuung.setInstitutionStammdaten(toReplace);
			persistence.merge(betreuung);
		}
	}
}
