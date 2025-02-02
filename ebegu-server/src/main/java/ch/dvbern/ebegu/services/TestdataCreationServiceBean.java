/*
 * Ki-Tax: System for the management of external childcare subsidies
 * Copyright (C) 2018 City of Bern Switzerland
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

import ch.dvbern.ebegu.entities.*;
import ch.dvbern.ebegu.enums.*;
import ch.dvbern.ebegu.enums.betreuung.BetreuungsangebotTyp;
import ch.dvbern.ebegu.persistence.CriteriaQueryHelper;
import ch.dvbern.ebegu.testfaelle.*;
import ch.dvbern.ebegu.testfaelle.institutionStammdatenBuilder.InstitutionStammdatenBuilder;
import ch.dvbern.ebegu.util.Constants;
import ch.dvbern.ebegu.util.TestfallName;
import ch.dvbern.ebegu.util.testdata.AnmeldungConfig;
import ch.dvbern.ebegu.util.testdata.ErstgesuchConfig;
import ch.dvbern.ebegu.util.testdata.MutationConfig;
import ch.dvbern.ebegu.util.testdata.TestdataSetupConfig;
import ch.dvbern.lib.cdipersistence.Persistence;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.PostConstruct;
import javax.ejb.Local;
import javax.ejb.Stateless;
import javax.inject.Inject;

import java.time.LocalDate;
import java.util.*;

import static ch.dvbern.ebegu.enums.EinstellungKey.*;
import static ch.dvbern.ebegu.util.Constants.*;

/**
 * Service fuer erstellen und mutieren von Testfällen
 */
@Stateless
@Local(TestdataCreationService.class)
public class TestdataCreationServiceBean extends AbstractBaseService implements TestdataCreationService {

	@Inject
	private GesuchsperiodeService gesuchsperiodeService;
	@Inject
	private InstitutionStammdatenService institutionStammdatenService;
	@Inject
	private GesuchService gesuchService;
	@Inject
	private TestfaelleService testfaelleService;
	@Inject
	private BetreuungService betreuungService;
	@Inject
	private Persistence persistence;
	@Inject
	private CriteriaQueryHelper criteriaQueryHelper;

	private InstitutionStammdatenBuilderVisitor testfallDependenciesVisitor;

	@PostConstruct
	public void createFactory() {
		testfallDependenciesVisitor = new InstitutionStammdatenBuilderVisitor(institutionStammdatenService);
	}

	@Override
	public void setupTestdata(@Nonnull TestdataSetupConfig config) {
		Mandant mandant = getMandant(config);
		Gesuchsperiode gesuchsperiode = getGesuchsperiode(config, null);
		insertInstitutionsstammdatenForTestfaelle(config, mandant);
		insertParametersForTestfaelle(gesuchsperiode);
	}

	@Override
	public Gesuch createErstgesuch(@Nonnull ErstgesuchConfig config, Mandant mandant) {
		Gesuchsperiode gesuchsperiode = getGesuchsperiode(null, config);
		Gemeinde gemeinde = getGemeinde(null, config);
		AbstractTestfall testfall = createTestfall(mandant, config, gesuchsperiode, gemeinde);
		Gesuch gesuch = testfaelleService.createAndSaveGesuch(testfall, config.isVerfuegt(), null);
		if (config.isVerfuegt()) {
			gesuch.setTimestampVerfuegt(config.getTimestampVerfuegt());
		}
		return gesuch;
	}

	@Override
	public Gesuch createMutation(@Nonnull MutationConfig config, @Nonnull Gesuch vorgaengerAntrag) {
		Gesuch mutation = testfaelleService.antragMutieren(vorgaengerAntrag, config.getEingangsdatum());
		if (config.getErwerbspensum() != null) {
			Objects.requireNonNull(mutation.getGesuchsteller1());
			Set<ErwerbspensumContainer> erwerbspensenContainersNotEmpty =
				mutation.getGesuchsteller1().getErwerbspensenContainersNotEmpty();
			for (ErwerbspensumContainer erwerbspensumContainer : erwerbspensenContainersNotEmpty) {
				Objects.requireNonNull(erwerbspensumContainer.getErwerbspensumJA());
				erwerbspensumContainer.getErwerbspensumJA().setPensum(config.getErwerbspensum());
			}
		}
		mutation = gesuchService.updateGesuch(mutation, false, null);
		testfaelleService.gesuchVerfuegenUndSpeichern( //verfuegung durchfuehren
			config.isVerfuegt(),
			mutation,
			true,
			config.isIgnorierenInZahlungslauf());
		if (config.isVerfuegt()) {
			mutation.setTimestampVerfuegt(config.getTimestampVerfuegt());
		}
		return mutation;
	}

	@Override
	public Gesuch addAnmeldung(@Nonnull AnmeldungConfig config, @Nonnull Gesuch gesuchToAdd) {
		List<Betreuung> betreuungs = gesuchToAdd.extractAllBetreuungen();
		KindContainer firstKind = betreuungs.iterator().next().getKind();
		InstitutionStammdaten institutionStammdaten = getInstitutionStammdaten(config);
		institutionStammdaten.setBetreuungsangebotTyp(BetreuungsangebotTyp.TAGESSCHULE);
		AnmeldungTagesschule anmeldung = new AnmeldungTagesschule();
		anmeldung.setKind(firstKind);
		anmeldung.setInstitutionStammdaten(institutionStammdaten);
		anmeldung.setBetreuungsstatus(config.getBetreuungsstatus());
		firstKind.getAnmeldungenTagesschule().add(anmeldung);
		anmeldung.setBelegungTagesschule(new BelegungTagesschule());
		Objects.requireNonNull(anmeldung.getBelegungTagesschule());
		anmeldung.getBelegungTagesschule().setEintrittsdatum(LocalDate.now());
		betreuungService.saveAnmeldungTagesschule(anmeldung);
		return persistence.find(Gesuch.class, gesuchToAdd.getId());
	}

	@Nonnull
	private AbstractTestfall createTestfall(
		@Nonnull Mandant mandant,
		@Nonnull ErstgesuchConfig config,
		@Nonnull Gesuchsperiode gesuchsperiode,
		Gemeinde gemeinde) {
		TestfallName fallid = config.getTestfallName();
		boolean betreuungenBestaetigt = config.isBetreuungenBestaetigt();

		if (gesuchsperiode == null) {
			throw new IllegalStateException("Keine Gesuchsperiode vorhanden");
		}

		InstitutionStammdatenBuilder institutionStammdatenBuilder = testfallDependenciesVisitor.process(mandant);

		if (TestfallName.WAELTI_DAGMAR == fallid) {
			return new Testfall01_WaeltiDagmar(
				gesuchsperiode,
				betreuungenBestaetigt,
				gemeinde, institutionStammdatenBuilder);
		}
		if (TestfallName.FEUTZ_IVONNE == fallid) {
			return new Testfall02_FeutzYvonne(
				gesuchsperiode,
				betreuungenBestaetigt,
				gemeinde, institutionStammdatenBuilder);
		}
		if (TestfallName.PERREIRA_MARCIA == fallid) {
			return new Testfall03_PerreiraMarcia(
				gesuchsperiode,
				betreuungenBestaetigt,
				gemeinde, institutionStammdatenBuilder);
		}
		if (TestfallName.WALTHER_LAURA == fallid) {
			return new Testfall04_WaltherLaura(
				gesuchsperiode,
				betreuungenBestaetigt,
				gemeinde, institutionStammdatenBuilder);
		}
		if (TestfallName.LUETHI_MERET == fallid) {
			return new Testfall05_LuethiMeret(
				gesuchsperiode,
				betreuungenBestaetigt,
				gemeinde, institutionStammdatenBuilder);
		}
		if (TestfallName.BECKER_NORA == fallid) {
			return new Testfall06_BeckerNora(
				gesuchsperiode,
				betreuungenBestaetigt,
				gemeinde, institutionStammdatenBuilder);
		}
		if (TestfallName.MEIER_MERET == fallid) {
			return new Testfall07_MeierMeret(
				gesuchsperiode,
				betreuungenBestaetigt,
				gemeinde, institutionStammdatenBuilder);
		}
		if (TestfallName.UMZUG_AUS == fallid) {
			return new Testfall08_UmzugAusInAusBern(
				gesuchsperiode,
				betreuungenBestaetigt,
				gemeinde, institutionStammdatenBuilder);
		}
		if (TestfallName.UMZUG_VOR == fallid) {
			return new Testfall10_UmzugVorGesuchsperiode(
				gesuchsperiode,
				betreuungenBestaetigt,
				gemeinde, institutionStammdatenBuilder);
		}
		if (TestfallName.ABWESENHEIT == fallid) {
			return new Testfall09_Abwesenheit(
				gesuchsperiode,
				betreuungenBestaetigt,
				gemeinde, institutionStammdatenBuilder);
		}
		if (TestfallName.ASIV1 == fallid) {
			return new Testfall_ASIV_01(gesuchsperiode, betreuungenBestaetigt, gemeinde, institutionStammdatenBuilder);
		}
		if (TestfallName.ASIV2 == fallid) {
			return new Testfall_ASIV_02(gesuchsperiode, betreuungenBestaetigt, gemeinde, institutionStammdatenBuilder);
		}
		if (TestfallName.ASIV3 == fallid) {
			return new Testfall_ASIV_03(gesuchsperiode, betreuungenBestaetigt, gemeinde, institutionStammdatenBuilder);
		}
		if (TestfallName.ASIV4 == fallid) {
			return new Testfall_ASIV_04(gesuchsperiode, betreuungenBestaetigt, gemeinde, institutionStammdatenBuilder);
		}
		if (TestfallName.ASIV5 == fallid) {
			return new Testfall_ASIV_05(gesuchsperiode, betreuungenBestaetigt, gemeinde, institutionStammdatenBuilder);
		}
		if (TestfallName.ASIV6 == fallid) {
			return new Testfall_ASIV_06(gesuchsperiode, betreuungenBestaetigt, gemeinde, institutionStammdatenBuilder);
		}
		if (TestfallName.ASIV7 == fallid) {
			return new Testfall_ASIV_07(gesuchsperiode, betreuungenBestaetigt, gemeinde, institutionStammdatenBuilder);
		}
		if (TestfallName.ASIV8 == fallid) {
			return new Testfall_ASIV_08(gesuchsperiode, betreuungenBestaetigt, gemeinde, institutionStammdatenBuilder);
		}
		if (TestfallName.ASIV9 == fallid) {
			return new Testfall_ASIV_09(gesuchsperiode, betreuungenBestaetigt, gemeinde, institutionStammdatenBuilder);
		}
		throw new IllegalStateException("Unbekannter Testfall: " + fallid);
	}

	@Nonnull
	private Mandant getMandant(@Nonnull TestdataSetupConfig config) {
		// Vorrang hat der konfigurierte Mandant
		if (config.getMandant() != null) {
			Mandant mandant = persistence.find(Mandant.class, config.getMandant().getId());
			if (mandant == null) {
				return persistence.persist(config.getMandant());
			}
			return mandant;
		}
		// Kein Mandant konfiguriert
		return getFirstMandant();
	}

	@Nonnull
	private Mandant getFirstMandant() {
		Collection<Mandant> all = criteriaQueryHelper.getAll(Mandant.class);
		if (all == null || all.size() != 1) {
			throw new IllegalStateException("Mandant not set up correctly");
		}
		Mandant mandant = all.iterator().next();
		return mandant;
	}

	@Nonnull
	private Gemeinde getGemeinde(
		@Nullable TestdataSetupConfig setupConfig,
		@Nullable ErstgesuchConfig erstgesuchConfig) {

		// Vorrang hat die Konfig des aktuellen Gesuchs
		if (erstgesuchConfig != null && erstgesuchConfig.getGemeinde() != null) {
			return saveGemeindeIfNeeded(erstgesuchConfig.getGemeinde());
		}
		// Zweite Prio hat die allgemeine Konfig des Tests
		if (setupConfig != null && setupConfig.getGemeinde() != null) {
			return saveGemeindeIfNeeded(setupConfig.getGemeinde());
		}
		// Wir nehmen was da ist
		return saveGemeindeIfNeeded(getGemeindeParis());
	}

	@Nonnull
	private Gemeinde getGemeindeParis() {
		Gemeinde paris = persistence.find(Gemeinde.class, "4c453263-f992-48af-86b5-dc04cd7e8bb8");
		if (paris == null) {
			throw new IllegalStateException("Gemeinde Paris not found");
		}
		return paris;
	}

	@Nonnull
	private Gesuchsperiode getGesuchsperiode(
		@Nullable TestdataSetupConfig setupConfig,
		@Nullable ErstgesuchConfig erstgesuchConfig) {

		// Vorrang hat die Konfig des aktuellen Gesuchs
		if (erstgesuchConfig != null && erstgesuchConfig.getGesuchsperiode() != null) {
			return saveGesuchsperiodeIfNeeded(erstgesuchConfig.getGesuchsperiode());
		}
		// Zweite Prio hat die allgemeine Konfig des Tests
		if (setupConfig != null && setupConfig.getGesuchsperiode() != null) {
			return saveGesuchsperiodeIfNeeded(setupConfig.getGesuchsperiode());
		}
		// Wir nehmen was da ist
		return getNeuesteGesuchsperiode();
	}

	@Nonnull
	private Gesuchsperiode getNeuesteGesuchsperiode() {
		Collection<Gesuchsperiode> allActiveGesuchsperioden = gesuchsperiodeService.getAllActiveGesuchsperioden();
		if (allActiveGesuchsperioden.isEmpty()) {
			throw new IllegalStateException("Keine Gesuchsperiode definiert");
		}
		return allActiveGesuchsperioden.iterator().next();
	}

	@Nonnull
	private Gesuchsperiode saveGesuchsperiodeIfNeeded(@Nonnull Gesuchsperiode gesuchsperiode) {
		Gesuchsperiode persistedGP = persistence.find(Gesuchsperiode.class, gesuchsperiode.getId());
		if (persistedGP == null) {
			return persistence.persist(gesuchsperiode);
		}
		return gesuchsperiode;
	}

	@Nonnull
	private Gemeinde saveGemeindeIfNeeded(@Nonnull Gemeinde gemeinde) {
		Gemeinde persistedGemeinde = persistence.find(Gemeinde.class, gemeinde.getId());
		if (persistedGemeinde == null) {
			return persistence.persist(gemeinde);
		}
		return gemeinde;
	}

	private void insertInstitutionsstammdatenForTestfaelle(@Nonnull TestdataSetupConfig config, @Nonnull Mandant mandant) {
		final InstitutionStammdaten institutionStammdatenKitaAaregg = config.getKitaWeissenstein();
		final InstitutionStammdaten institutionStammdatenKitaBruennen = config.getKitaBruennen();
		final InstitutionStammdaten institutionStammdatenTagesfamilien = config.getTagesfamilien();
		final InstitutionStammdaten institutionStammdatenTagesschuleBruennen = config.getTagesschuleBruennen();
		final InstitutionStammdaten institutionStammdatenFerieninselBruennen = config.getFerieninselBruennen();

		if (institutionStammdatenKitaAaregg != null) {
			institutionStammdatenKitaAaregg.getInstitution().setMandant(mandant);
			saveInstitutionStammdatenIfNecessary(institutionStammdatenKitaAaregg);
		}
		if (institutionStammdatenKitaBruennen != null) {
			institutionStammdatenKitaBruennen.getInstitution().setMandant(mandant);
			saveInstitutionStammdatenIfNecessary(institutionStammdatenKitaBruennen);
		}
		if (institutionStammdatenTagesfamilien != null) {
			institutionStammdatenTagesfamilien.getInstitution().setMandant(mandant);
			saveInstitutionStammdatenIfNecessary(institutionStammdatenTagesfamilien);
		}
		if (institutionStammdatenTagesschuleBruennen != null) {
			institutionStammdatenTagesschuleBruennen.getInstitution().setMandant(mandant);
			saveInstitutionStammdatenIfNecessary(institutionStammdatenTagesschuleBruennen);
		}
		if (institutionStammdatenFerieninselBruennen != null) {
			institutionStammdatenFerieninselBruennen.getInstitution().setMandant(mandant);
			saveInstitutionStammdatenIfNecessary(institutionStammdatenFerieninselBruennen);
		}
	}

	private void saveInstitutionStammdatenIfNecessary(@Nullable InstitutionStammdaten institutionStammdaten) {
		if (institutionStammdaten != null) {
			saveInstitutionIfNecessary(institutionStammdaten.getInstitution());
			Optional<InstitutionStammdaten> optionalStammdaten = institutionStammdatenService
				.findInstitutionStammdaten(
					institutionStammdaten.getId());
			if (!optionalStammdaten.isPresent()) {
				institutionStammdatenService.saveInstitutionStammdaten(institutionStammdaten);
			}
		}
	}

	private void saveInstitutionIfNecessary(@Nullable Institution institution) {
		if (institution != null) {
			saveTraegerschaftIfNecessary(institution.getTraegerschaft(), institution.getMandant());
			Institution found = persistence.find(Institution.class, institution.getId());
			if (found == null) {
				persistence.persist(institution);
			}
		}
	}

	private void saveTraegerschaftIfNecessary(@Nullable Traegerschaft traegerschaft, Mandant mandant) {
		if (traegerschaft != null) {
			traegerschaft.setMandant(mandant);
			Traegerschaft found = persistence.find(Traegerschaft.class, traegerschaft.getId());
			if (found == null) {
				persistence.persist(traegerschaft);
			}
		}
	}

	@Override
	@SuppressWarnings({ "PMD.AvoidDuplicateLiterals", "PMD.NcssMethodCount" })
	public void insertParametersForTestfaelle(@Nonnull Gesuchsperiode gesuchsperiode) {
		saveEinstellung(
			PARAM_PAUSCHALABZUG_PRO_PERSON_FAMILIENGROESSE_3,
			PAUSCHALABZUG_PRO_PERSON_FAMILIENGROESSE_3_FUER_TESTS,
			gesuchsperiode);
		saveEinstellung(
			PARAM_PAUSCHALABZUG_PRO_PERSON_FAMILIENGROESSE_4,
			PAUSCHALABZUG_PRO_PERSON_FAMILIENGROESSE_4_FUER_TESTS,
			gesuchsperiode);
		saveEinstellung(
			PARAM_PAUSCHALABZUG_PRO_PERSON_FAMILIENGROESSE_5,
			PAUSCHALABZUG_PRO_PERSON_FAMILIENGROESSE_5_FUER_TESTS,
			gesuchsperiode);
		saveEinstellung(
			PARAM_PAUSCHALABZUG_PRO_PERSON_FAMILIENGROESSE_6,
			PAUSCHALABZUG_PRO_PERSON_FAMILIENGROESSE_6_FUER_TESTS,
			gesuchsperiode);
		saveEinstellung(PARAM_GRENZWERT_EINKOMMENSVERSCHLECHTERUNG, "20", gesuchsperiode);
		saveEinstellung(PARAM_PENSUM_KITA_MIN, "0", gesuchsperiode);
		saveEinstellung(PARAM_PENSUM_TAGESELTERN_MIN, "0", gesuchsperiode);
		saveEinstellung(PARAM_PENSUM_TAGESSCHULE_MIN, "0", gesuchsperiode);
		saveEinstellung(GEMEINDE_KONTINGENTIERUNG_ENABLED, "false", gesuchsperiode);
		saveEinstellung(GEMEINDE_BG_BIS_UND_MIT_SCHULSTUFE, EinschulungTyp.VORSCHULALTER.name(), gesuchsperiode);
		saveEinstellung(ANGEBOT_SCHULSTUFE, BetreuungsangebotTyp.KITA.name(), gesuchsperiode);
		saveEinstellung(PARAM_MAX_TAGE_ABWESENHEIT, "30", gesuchsperiode);
		saveEinstellung(MAX_VERGUENSTIGUNG_VORSCHULE_BABY_PRO_TG, "150", gesuchsperiode);
		saveEinstellung(MAX_VERGUENSTIGUNG_VORSCHULE_KIND_PRO_TG, "100", gesuchsperiode);
		saveEinstellung(MAX_VERGUENSTIGUNG_KINDERGARTEN_PRO_TG, "75", gesuchsperiode);
		saveEinstellung(MAX_VERGUENSTIGUNG_VORSCHULE_BABY_PRO_STD, "12.75", gesuchsperiode);
		saveEinstellung(MAX_VERGUENSTIGUNG_VORSCHULE_KIND_PRO_STD, "8.50", gesuchsperiode);
		saveEinstellung(MAX_VERGUENSTIGUNG_KINDERGARTEN_PRO_STD, "8.50", gesuchsperiode);
		saveEinstellung(MAX_VERGUENSTIGUNG_PRIMAR_PRO_STD, "8.50", gesuchsperiode);
		saveEinstellung(MAX_MASSGEBENDES_EINKOMMEN, "160000", gesuchsperiode);
		saveEinstellung(MIN_MASSGEBENDES_EINKOMMEN, "43000", gesuchsperiode);
		saveEinstellung(OEFFNUNGSTAGE_KITA, "240", gesuchsperiode);
		saveEinstellung(OEFFNUNGSTAGE_TFO, "240", gesuchsperiode);
		saveEinstellung(OEFFNUNGSSTUNDEN_TFO, "11", gesuchsperiode);
		saveEinstellung(ZUSCHLAG_BEHINDERUNG_PRO_TG, "50", gesuchsperiode);
		saveEinstellung(ZUSCHLAG_BEHINDERUNG_PRO_STD, "4.25", gesuchsperiode);
		saveEinstellung(MIN_VERGUENSTIGUNG_PRO_TG, "7", gesuchsperiode);
		saveEinstellung(MIN_VERGUENSTIGUNG_PRO_STD, "0.70", gesuchsperiode);
		saveEinstellung(MIN_ERWERBSPENSUM_NICHT_EINGESCHULT, "20", gesuchsperiode);
		saveEinstellung(MIN_ERWERBSPENSUM_EINGESCHULT, "40", gesuchsperiode);
		saveEinstellung(ERWERBSPENSUM_ZUSCHLAG, "20", gesuchsperiode);
		saveEinstellung(FACHSTELLE_MIN_PENSUM_SOZIALE_INTEGRATION, "20", gesuchsperiode);
		saveEinstellung(FACHSTELLE_MAX_PENSUM_SOZIALE_INTEGRATION, "60", gesuchsperiode);
		saveEinstellung(FACHSTELLE_MIN_PENSUM_SPRACHLICHE_INTEGRATION, "40", gesuchsperiode);
		saveEinstellung(FACHSTELLE_MAX_PENSUM_SPRACHLICHE_INTEGRATION, "40", gesuchsperiode);
		saveEinstellung(GEMEINDE_FERIENINSEL_ANMELDUNGEN_DATUM_AB,
			Constants.DATE_FORMATTER.format(gesuchsperiode.getGueltigkeit().getGueltigAb()), gesuchsperiode);
		saveEinstellung(GEMEINDE_TAGESSCHULE_ANMELDUNGEN_DATUM_AB,
			Constants.DATE_FORMATTER.format(gesuchsperiode.getGueltigkeit().getGueltigAb()), gesuchsperiode);
		saveEinstellung(GEMEINDE_TAGESSCHULE_ERSTER_SCHULTAG,
			Constants.DATE_FORMATTER.format(gesuchsperiode.getGueltigkeit().getGueltigAb()), gesuchsperiode);
		saveEinstellung(GEMEINDE_TAGESSCHULE_TAGIS_ENABLED, "false", gesuchsperiode);
		saveEinstellung(MAX_TARIF_MIT_PAEDAGOGISCHER_BETREUUNG, "12.24", gesuchsperiode);
		saveEinstellung(MAX_TARIF_OHNE_PAEDAGOGISCHER_BETREUUNG, "6.11", gesuchsperiode);
		saveEinstellung(MIN_TARIF, "0.78", gesuchsperiode);
		saveEinstellung(GEMEINDE_ZUSAETZLICHER_GUTSCHEIN_ENABLED, "false", gesuchsperiode);
		saveEinstellung(GEMEINDE_TAGESSCHULE_ZUSAETZLICHE_ANGABEN_ZUR_ANMELDUNG, "false", gesuchsperiode);
		saveEinstellung(GEMEINDE_ZUSAETZLICHER_GUTSCHEIN_BETRAG_KITA, "0.00", gesuchsperiode);
		saveEinstellung(GEMEINDE_ZUSAETZLICHER_GUTSCHEIN_BETRAG_TFO, "0.00", gesuchsperiode);
		saveEinstellung(GEMEINDE_ZUSAETZLICHER_GUTSCHEIN_LINEAR_KITA_MAX, "", gesuchsperiode);
		saveEinstellung(GEMEINDE_ZUSAETZLICHER_GUTSCHEIN_LINEAR_TFO_MAX, "", gesuchsperiode);
		saveEinstellung(GEMEINDE_ZUSAETZLICHER_GUTSCHEIN_MIN_MASSGEBENDES_EINKOMMEN, "", gesuchsperiode);
		saveEinstellung(GEMEINDE_ZUSAETZLICHER_GUTSCHEIN_MAX_MASSGEBENDES_EINKOMMEN, "", gesuchsperiode);
		saveEinstellung(
			GEMEINDE_ZUSAETZLICHER_GUTSCHEIN_BIS_UND_MIT_SCHULSTUFE_KITA,
			EinschulungTyp.VORSCHULALTER.name(),
			gesuchsperiode);
		saveEinstellung(
			GEMEINDE_ZUSAETZLICHER_GUTSCHEIN_BIS_UND_MIT_SCHULSTUFE_TFO,
			EinschulungTyp.VORSCHULALTER.name(),
			gesuchsperiode);
		saveEinstellung(GEMEINDE_ZUSAETZLICHER_BABYBEITRAG_ENABLED, "false", gesuchsperiode);
		saveEinstellung(GEMEINDE_ZUSAETZLICHER_BABYBEITRAG_BETRAG_KITA, "0.00", gesuchsperiode);
		saveEinstellung(GEMEINDE_ZUSAETZLICHER_BABYBEITRAG_BETRAG_TFO, "0.00", gesuchsperiode);
		saveEinstellung(GEMEINDE_ZUSAETZLICHER_ANSPRUCH_FREIWILLIGENARBEIT_ENABLED, "false", gesuchsperiode);
		saveEinstellung(GEMEINDE_ZUSAETZLICHER_ANSPRUCH_FREIWILLIGENARBEIT_MAXPROZENT, "0", gesuchsperiode);
		saveEinstellung(GEMEINDE_MAHLZEITENVERGUENSTIGUNG_ENABLED, "true", gesuchsperiode);
		saveEinstellung(GEMEINDE_MAHLZEITENVERGUENSTIGUNG_EINKOMMENSSTUFE_1_VERGUENSTIGUNG_MAHLZEIT, "6", gesuchsperiode);
		saveEinstellung(GEMEINDE_MAHLZEITENVERGUENSTIGUNG_EINKOMMENSSTUFE_1_MAX_EINKOMMEN, "50000", gesuchsperiode);
		saveEinstellung(GEMEINDE_MAHLZEITENVERGUENSTIGUNG_EINKOMMENSSTUFE_2_VERGUENSTIGUNG_MAHLZEIT, "3", gesuchsperiode);
		saveEinstellung(GEMEINDE_MAHLZEITENVERGUENSTIGUNG_EINKOMMENSSTUFE_2_MAX_EINKOMMEN, "70000", gesuchsperiode);
		saveEinstellung(GEMEINDE_MAHLZEITENVERGUENSTIGUNG_EINKOMMENSSTUFE_3_VERGUENSTIGUNG_MAHLZEIT, "0", gesuchsperiode);
		saveEinstellung(GEMEINDE_MAHLZEITENVERGUENSTIGUNG_FUER_SOZIALHILFEBEZUEGER_ENABLED, "true", gesuchsperiode);
		saveEinstellung(GEMEINDE_MAHLZEITENVERGUENSTIGUNG_MINIMALER_ELTERNBEITRAG_MAHLZEIT, "2", gesuchsperiode);
		saveEinstellung(GEMEINDE_SCHNITTSTELLE_KITAX_ENABLED, "false", gesuchsperiode);
		saveEinstellung(GEMEINDE_MIN_ERWERBSPENSUM_NICHT_EINGESCHULT, "20", gesuchsperiode);
		saveEinstellung(GEMEINDE_MIN_ERWERBSPENSUM_EINGESCHULT, "40", gesuchsperiode);
		saveEinstellung(GEMEINDE_PAUSCHALBETRAG_HOHE_EINKOMMENSKLASSEN_AKTIVIERT, "false", gesuchsperiode);
		saveEinstellung(GEMEINDE_PAUSCHALBETRAG_HOHE_EINKOMMENSKLASSEN_BETRAG_KITA, "0", gesuchsperiode);
		saveEinstellung(GEMEINDE_PAUSCHALBETRAG_HOHE_EINKOMMENSKLASSEN_BETRAG_TFO, "0", gesuchsperiode);
		saveEinstellung(GEMEINDE_PAUSCHALBETRAG_HOHE_EINKOMMENSKLASSEN_BETRAG_TFO_AB_PRIMARSCHULE, "0", gesuchsperiode);
		saveEinstellung(
			GEMEINDE_PAUSCHALBETRAG_HOHE_EINKOMMENSKLASSEN_MAX_MASSGEBENDEN_EINKOMMEN_FUER_BERECHNUNG,
			"160000",
			gesuchsperiode);
		saveEinstellung(GEMEINDE_KEIN_GUTSCHEIN_FUER_SOZIALHILFE_EMPFAENGER, "false", gesuchsperiode);
		saveEinstellung(LATS_LOHNNORMKOSTEN, "10.39", gesuchsperiode);
		saveEinstellung(LATS_LOHNNORMKOSTEN_LESS_THAN_50, "5.2", gesuchsperiode);
		String stichtag = gesuchsperiode.getGueltigkeit().getGueltigAb().getYear() + "-09-15";
		saveEinstellung(LATS_STICHTAG, stichtag, gesuchsperiode);
		saveEinstellung(EINGEWOEHNUNG_TYP, EingewoehnungTyp.KEINE.toString(), gesuchsperiode);
		saveEinstellung(FKJV_MAX_DIFFERENZ_BESCHAEFTIGUNGSPENSUM, "100", gesuchsperiode);
		saveEinstellung(FKJV_SOZIALE_INTEGRATION_BIS_SCHULSTUFE, "VORSCHULALTER", gesuchsperiode);
		saveEinstellung(SPRACHLICHE_INTEGRATION_BIS_SCHULSTUFE, "VORSCHULALTER", gesuchsperiode);
		saveEinstellung(FKJV_PAUSCHALE_BEI_ANSPRUCH, "false", gesuchsperiode);
		saveEinstellung(FKJV_EINKOMMENSVERSCHLECHTERUNG_BIS_CHF, "null", gesuchsperiode);
		saveEinstellung(FKJV_PAUSCHALE_RUECKWIRKEND, "false", gesuchsperiode);
		saveEinstellung(ANSPRUCH_MONATSWEISE, "false", gesuchsperiode);
		saveEinstellung(SCHNITTSTELLE_STEUERN_AKTIV, "false", gesuchsperiode);
		saveEinstellung(FERIENBETREUUNG_CHF_PAUSCHALBETRAG, "30", gesuchsperiode);
		saveEinstellung(FERIENBETREUUNG_CHF_PAUSCHALBETRAG_SONDERSCHUELER, "60", gesuchsperiode);
		saveEinstellung(FKJV_FAMILIENSITUATION_NEU, "false", gesuchsperiode);
		saveEinstellung(MINIMALDAUER_KONKUBINAT, "5", gesuchsperiode);
		saveEinstellung(FINANZIELLE_SITUATION_TYP, "BERN", gesuchsperiode);
		saveEinstellung(KITAPLUS_ZUSCHLAG_AKTIVIERT, "false", gesuchsperiode);
		saveEinstellung(GEMEINDESPEZIFISCHE_BG_KONFIGURATIONEN, "false", gesuchsperiode);
		saveEinstellung(ABHAENGIGKEIT_ANSPRUCH_BESCHAEFTIGUNGPENSUM,
			AnspruchBeschaeftigungAbhaengigkeitTyp.ABHAENGING.name(), gesuchsperiode);
		saveEinstellung(KINDERABZUG_TYP, "ASIV", gesuchsperiode);
		saveEinstellung(FKJV_MAX_PENSUM_AUSSERORDENTLICHER_ANSPRUCH, "100", gesuchsperiode);
		saveEinstellung(AUSSERORDENTLICHER_ANSPRUCH_RULE, "ASIV", gesuchsperiode);
		saveEinstellung(KESB_PLATZIERUNG_DEAKTIVIEREN, "false", gesuchsperiode);
		saveEinstellung(BESONDERE_BEDUERFNISSE_LUZERN, "false", gesuchsperiode);
		saveEinstellung(GESCHWISTERNBONUS_TYP, "NONE", gesuchsperiode);
		saveEinstellung(DAUER_BABYTARIF, "12", gesuchsperiode);
		saveEinstellung(FKJV_TEXTE, "false", gesuchsperiode);
		saveEinstellung(DIPLOMATENSTATUS_DEAKTIVIERT, "false", gesuchsperiode);
		saveEinstellung(ZEMIS_DISABLED, "false", gesuchsperiode);
		saveEinstellung(SPRACHE_AMTSPRACHE_DISABLED, "false", gesuchsperiode);
		saveEinstellung(FREIGABE_QUITTUNG_EINLESEN_REQUIRED, "true", gesuchsperiode);
		saveEinstellung(UNBEZAHLTER_URLAUB_AKTIV, "true", gesuchsperiode);
		saveEinstellung(FACHSTELLEN_TYP, "BERN", gesuchsperiode);
		saveEinstellung(AUSWEIS_NACHWEIS_REQUIRED, "false", gesuchsperiode);
		saveEinstellung(PENSUM_ANZEIGE_TYP, "ZEITEINHEIT_UND_PROZENT", gesuchsperiode);
		saveEinstellung(VERFUEGUNG_EINGESCHRIEBEN_VERSENDEN_AKTIVIERT, "true", gesuchsperiode);
		saveEinstellung(ABWESENHEIT_AKTIV, "true", gesuchsperiode);
		saveEinstellung(BEGRUENDUNG_MUTATION_AKTIVIERT, "false", gesuchsperiode);
		saveEinstellung(VERFUEGUNG_EXPORT_ENABLED, "false", gesuchsperiode);
		saveEinstellung(ZAHLUNGSANGABEN_ANTRAGSTELLER_REQUIRED, "false", gesuchsperiode);
		saveEinstellung(VERANLAGUNG_MIN_UNTERSCHIED_MASSGEBENDESEINK, "0", gesuchsperiode);
		saveEinstellung(ANSPRUCH_AB_X_MONATEN, "0", gesuchsperiode);
		saveEinstellung(KITA_STUNDEN_PRO_TAG, "10", gesuchsperiode);
		saveEinstellung(ZUSATZLICHE_FELDER_ERSATZEINKOMMEN, "false", gesuchsperiode);
		saveEinstellung(SPRACHFOERDERUNG_BESTAETIGEN, "false", gesuchsperiode);
		saveEinstellung(GESUCH_BEENDEN_BEI_TAUSCH_GS2, "false", gesuchsperiode);
		saveEinstellung(SCHULERGAENZENDE_BETREUUNGEN, "false", gesuchsperiode);
		saveEinstellung(WEGZEIT_ERWERBSPENSUM, "false", gesuchsperiode);
		saveEinstellung(ANWESENHEITSTAGE_PRO_MONAT_AKTIVIERT, "false", gesuchsperiode);
		saveEinstellung(SOZIALVERSICHERUNGSNUMMER_PERIODE, "false", gesuchsperiode);
		saveEinstellung(HOEHERE_BEITRAEGE_BEEINTRAECHTIGUNG_AKTIVIERT, "false", gesuchsperiode);
	}

	public void saveEinstellung(EinstellungKey key, String value, Gesuchsperiode gesuchsperiode) {
		Einstellung einstellungen = new Einstellung(key, value, gesuchsperiode);
		persistence.persist(einstellungen);
	}

	private InstitutionStammdaten getInstitutionStammdaten(@Nonnull AnmeldungConfig config) {
		if (config.getInstitutionStammdaten() != null) {
			return persistence.merge(config.getInstitutionStammdaten());
		}
		Collection<InstitutionStammdaten> institutionen =
			criteriaQueryHelper.getEntitiesByAttribute(InstitutionStammdaten.class,
				config.getBetreuungsangebotTyp(), InstitutionStammdaten_.betreuungsangebotTyp);
		if (institutionen.isEmpty()) {
			throw new IllegalStateException("Keine Institution mit Typ "
				+ config.getBetreuungsangebotTyp()
				+ " gefunden");
		}
		return institutionen.iterator().next();
	}
}
