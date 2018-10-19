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

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.security.RolesAllowed;
import javax.ejb.Local;
import javax.ejb.Stateless;
import javax.inject.Inject;

import ch.dvbern.ebegu.entities.Betreuung;
import ch.dvbern.ebegu.entities.Einstellung;
import ch.dvbern.ebegu.entities.ErwerbspensumContainer;
import ch.dvbern.ebegu.entities.Gemeinde;
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.entities.Gesuchsperiode;
import ch.dvbern.ebegu.entities.InstitutionStammdaten;
import ch.dvbern.ebegu.entities.InstitutionStammdaten_;
import ch.dvbern.ebegu.entities.KindContainer;
import ch.dvbern.ebegu.entities.Mandant;
import ch.dvbern.ebegu.entities.Traegerschaft;
import ch.dvbern.ebegu.enums.EinschulungTyp;
import ch.dvbern.ebegu.enums.EinstellungKey;
import ch.dvbern.ebegu.errors.EbeguEntityNotFoundException;
import ch.dvbern.ebegu.persistence.CriteriaQueryHelper;
import ch.dvbern.ebegu.testfaelle.AbstractTestfall;
import ch.dvbern.ebegu.testfaelle.Testfall01_WaeltiDagmar;
import ch.dvbern.ebegu.testfaelle.Testfall02_FeutzYvonne;
import ch.dvbern.ebegu.testfaelle.Testfall03_PerreiraMarcia;
import ch.dvbern.ebegu.testfaelle.Testfall04_WaltherLaura;
import ch.dvbern.ebegu.testfaelle.Testfall05_LuethiMeret;
import ch.dvbern.ebegu.testfaelle.Testfall06_BeckerNora;
import ch.dvbern.ebegu.testfaelle.Testfall07_MeierMeret;
import ch.dvbern.ebegu.testfaelle.Testfall08_UmzugAusInAusBern;
import ch.dvbern.ebegu.testfaelle.Testfall09_Abwesenheit;
import ch.dvbern.ebegu.testfaelle.Testfall10_UmzugVorGesuchsperiode;
import ch.dvbern.ebegu.testfaelle.Testfall_ASIV_01;
import ch.dvbern.ebegu.testfaelle.Testfall_ASIV_02;
import ch.dvbern.ebegu.testfaelle.Testfall_ASIV_03;
import ch.dvbern.ebegu.testfaelle.Testfall_ASIV_04;
import ch.dvbern.ebegu.testfaelle.Testfall_ASIV_05;
import ch.dvbern.ebegu.testfaelle.Testfall_ASIV_06;
import ch.dvbern.ebegu.testfaelle.Testfall_ASIV_07;
import ch.dvbern.ebegu.testfaelle.Testfall_ASIV_08;
import ch.dvbern.ebegu.testfaelle.Testfall_ASIV_09;
import ch.dvbern.ebegu.util.TestfallName;
import ch.dvbern.ebegu.util.testdata.AnmeldungConfig;
import ch.dvbern.ebegu.util.testdata.ErstgesuchConfig;
import ch.dvbern.ebegu.util.testdata.MutationConfig;
import ch.dvbern.ebegu.util.testdata.TestdataSetupConfig;
import ch.dvbern.lib.cdipersistence.Persistence;

import static ch.dvbern.ebegu.enums.EinstellungKey.BG_BIS_UND_MIT_SCHULSTUFE;
import static ch.dvbern.ebegu.enums.EinstellungKey.KONTINGENTIERUNG_ENABLED;
import static ch.dvbern.ebegu.enums.EinstellungKey.MAX_MASSGEBENDES_EINKOMMEN;
import static ch.dvbern.ebegu.enums.EinstellungKey.MAX_VERGUENSTIGUNG_SCHULE_PRO_STD;
import static ch.dvbern.ebegu.enums.EinstellungKey.MAX_VERGUENSTIGUNG_SCHULE_PRO_TG;
import static ch.dvbern.ebegu.enums.EinstellungKey.MAX_VERGUENSTIGUNG_VORSCHULE_BABY_PRO_STD;
import static ch.dvbern.ebegu.enums.EinstellungKey.MAX_VERGUENSTIGUNG_VORSCHULE_BABY_PRO_TG;
import static ch.dvbern.ebegu.enums.EinstellungKey.MAX_VERGUENSTIGUNG_VORSCHULE_KIND_PRO_STD;
import static ch.dvbern.ebegu.enums.EinstellungKey.MAX_VERGUENSTIGUNG_VORSCHULE_KIND_PRO_TG;
import static ch.dvbern.ebegu.enums.EinstellungKey.MIN_MASSGEBENDES_EINKOMMEN;
import static ch.dvbern.ebegu.enums.EinstellungKey.MIN_VERGUENSTIGUNG_PRO_STD;
import static ch.dvbern.ebegu.enums.EinstellungKey.MIN_VERGUENSTIGUNG_PRO_TG;
import static ch.dvbern.ebegu.enums.EinstellungKey.OEFFNUNGSSTUNDEN_TFO;
import static ch.dvbern.ebegu.enums.EinstellungKey.OEFFNUNGSTAGE_KITA;
import static ch.dvbern.ebegu.enums.EinstellungKey.OEFFNUNGSTAGE_TFO;
import static ch.dvbern.ebegu.enums.EinstellungKey.PARAM_GRENZWERT_EINKOMMENSVERSCHLECHTERUNG;
import static ch.dvbern.ebegu.enums.EinstellungKey.PARAM_MAXIMALER_ZUSCHLAG_ERWERBSPENSUM;
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
import static ch.dvbern.ebegu.enums.UserRoleName.ADMIN_BG;
import static ch.dvbern.ebegu.enums.UserRoleName.ADMIN_GEMEINDE;
import static ch.dvbern.ebegu.enums.UserRoleName.SUPER_ADMIN;

/**
 * Service fuer erstellen und mutieren von Testfällen
 */
@Stateless
@Local(TestdataCreationService.class)
@RolesAllowed({ ADMIN_BG, ADMIN_GEMEINDE, SUPER_ADMIN })
public class TestdataCreationServiceBean extends AbstractBaseService implements TestdataCreationService {

	@Inject
	private GesuchsperiodeService gesuchsperiodeService;
	@Inject
	private InstitutionStammdatenService institutionStammdatenService;
	@Inject
	private InstitutionService institutionService;
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


	@Override
	public void setupTestdata(@Nonnull TestdataSetupConfig config) {
		Mandant mandant = getMandant(config);
		Gesuchsperiode gesuchsperiode = getGesuchsperiode(config, null);
		insertInstitutionsstammdatenForTestfaelle(config, mandant, gesuchsperiode);
		insertParametersForTestfaelle(gesuchsperiode);
	}

	@Override
	public Gesuch createErstgesuch(@Nonnull ErstgesuchConfig config) {
		Gesuchsperiode gesuchsperiode = getGesuchsperiode(null, config);
		Gemeinde gemeinde = getGemeinde(null, config);
		AbstractTestfall testfall = createTestfall(config, gesuchsperiode, gemeinde);
		Gesuch gesuch = testfaelleService.createAndSaveGesuch(testfall, true, null);
		if (config.isVerfuegt()) {
			gesuch.setTimestampVerfuegt(config.getTimestampVerfuegt());
		}
		return gesuch;
	}

	@Override
	public Gesuch createMutation(@Nonnull MutationConfig config, @Nonnull Gesuch vorgaengerAntrag) {
		Gesuch mutation = gesuchService.antragMutieren(vorgaengerAntrag.getId(), config.getEingangsdatum())
			.orElseThrow(() -> new EbeguEntityNotFoundException("antragMutieren", ""));
		if (config.getErwerbspensum() != null) {
			Objects.requireNonNull(mutation.getGesuchsteller1());
			Set<ErwerbspensumContainer> erwerbspensenContainersNotEmpty = mutation.getGesuchsteller1().getErwerbspensenContainersNotEmpty();
			for (ErwerbspensumContainer erwerbspensumContainer : erwerbspensenContainersNotEmpty) {
				Objects.requireNonNull(erwerbspensumContainer.getErwerbspensumJA());
				erwerbspensumContainer.getErwerbspensumJA().setPensum(config.getErwerbspensum());
			}
		}
		mutation = gesuchService.createGesuch(mutation);
		testfaelleService.gesuchVerfuegenUndSpeichern(config.isVerfuegt(), mutation, true, config.isIgnorierenInZahlungslauf());
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
		Betreuung betreuung = new Betreuung();
		betreuung.setKind(firstKind);
		betreuung.setInstitutionStammdaten(institutionStammdaten);
		betreuung.setBetreuungsstatus(config.getBetreuungsstatus());
		betreuungService.saveBetreuung(betreuung, false);
		return persistence.find(Gesuch.class, gesuchToAdd.getId());
	}

	@Nonnull
	private AbstractTestfall createTestfall(@Nonnull ErstgesuchConfig config, @Nonnull Gesuchsperiode gesuchsperiode, Gemeinde gemeinde) {
		TestfallName fallid = config.getTestfallName();
		boolean betreuungenBestaetigt = config.isBetreuungenBestaetigt();

		if (gesuchsperiode == null) {
			throw new IllegalStateException("Keine Gesuchsperiode vorhanden");
		}
		List<InstitutionStammdaten> institutionStammdatenList = testfaelleService.getInstitutionsstammdatenForTestfaelle();

		if (TestfallName.WAELTI_DAGMAR == fallid) {
			return new Testfall01_WaeltiDagmar(gesuchsperiode, institutionStammdatenList, betreuungenBestaetigt, gemeinde);
		}
		if (TestfallName.FEUTZ_IVONNE == fallid) {
			return new Testfall02_FeutzYvonne(gesuchsperiode, institutionStammdatenList, betreuungenBestaetigt, gemeinde);
		}
		if (TestfallName.PERREIRA_MARCIA == fallid) {
			return new Testfall03_PerreiraMarcia(gesuchsperiode, institutionStammdatenList, betreuungenBestaetigt, gemeinde);
		}
		if (TestfallName.WALTHER_LAURA == fallid) {
			return new Testfall04_WaltherLaura(gesuchsperiode, institutionStammdatenList, betreuungenBestaetigt, gemeinde);
		}
		if (TestfallName.LUETHI_MERET == fallid) {
			return new Testfall05_LuethiMeret(gesuchsperiode, institutionStammdatenList, betreuungenBestaetigt, gemeinde);
		}
		if (TestfallName.BECKER_NORA == fallid) {
			return new Testfall06_BeckerNora(gesuchsperiode, institutionStammdatenList, betreuungenBestaetigt, gemeinde);
		}
		if (TestfallName.MEIER_MERET == fallid) {
			return new Testfall07_MeierMeret(gesuchsperiode, institutionStammdatenList, betreuungenBestaetigt, gemeinde);
		}
		if (TestfallName.UMZUG_AUS == fallid) {
			return new Testfall08_UmzugAusInAusBern(gesuchsperiode, institutionStammdatenList, betreuungenBestaetigt, gemeinde);
		}
		if (TestfallName.UMZUG_VOR == fallid) {
			return new Testfall10_UmzugVorGesuchsperiode(gesuchsperiode, institutionStammdatenList, betreuungenBestaetigt, gemeinde);
		}
		if (TestfallName.ABWESENHEIT == fallid) {
			return new Testfall09_Abwesenheit(gesuchsperiode, institutionStammdatenList, betreuungenBestaetigt, gemeinde);
		}
		if (TestfallName.ASIV1 == fallid) {
			return new Testfall_ASIV_01(gesuchsperiode, institutionStammdatenList, betreuungenBestaetigt, gemeinde);
		}
		if (TestfallName.ASIV2 == fallid) {
			return new Testfall_ASIV_02(gesuchsperiode, institutionStammdatenList, betreuungenBestaetigt, gemeinde);
		}
		if (TestfallName.ASIV3 == fallid) {
			return new Testfall_ASIV_03(gesuchsperiode, institutionStammdatenList, betreuungenBestaetigt, gemeinde);
		}
		if (TestfallName.ASIV4 == fallid) {
			return new Testfall_ASIV_04(gesuchsperiode, institutionStammdatenList, betreuungenBestaetigt, gemeinde);
		}
		if (TestfallName.ASIV5 == fallid) {
			return new Testfall_ASIV_05(gesuchsperiode, institutionStammdatenList, betreuungenBestaetigt, gemeinde);
		}
		if (TestfallName.ASIV6 == fallid) {
			return new Testfall_ASIV_06(gesuchsperiode, institutionStammdatenList, betreuungenBestaetigt, gemeinde);
		}
		if (TestfallName.ASIV7 == fallid) {
			return new Testfall_ASIV_07(gesuchsperiode, institutionStammdatenList, betreuungenBestaetigt, gemeinde);
		}
		if (TestfallName.ASIV8 == fallid) {
			return new Testfall_ASIV_08(gesuchsperiode, institutionStammdatenList, betreuungenBestaetigt, gemeinde);
		}
		if (TestfallName.ASIV9 == fallid) {
			return new Testfall_ASIV_09(gesuchsperiode, institutionStammdatenList, betreuungenBestaetigt, gemeinde);
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
	private Gemeinde getGemeinde(@Nullable TestdataSetupConfig setupConfig, @Nullable ErstgesuchConfig erstgesuchConfig) {
		// Vorrang hat die Konfig des aktuellen Gesuchs
		if (erstgesuchConfig != null && erstgesuchConfig.getGemeinde() != null) {
			return saveGemeindeIfNeeded(erstgesuchConfig.getGemeinde());
		}
		// Zweite Prio hat die allgemeine Konfig des Tests
		if (setupConfig != null && setupConfig.getGemeinde() != null) {
			return saveGemeindeIfNeeded(setupConfig.getGemeinde());
		}
		// Wir nehmen was da ist
		return saveGemeindeIfNeeded(getGemeindeBern());
	}

	@Nonnull
	private Gemeinde getGemeindeBern() {
		Gemeinde bern = persistence.find(Gemeinde.class, "4c453263-f992-48af-86b5-dc04cd7e8bb8");
		if (bern == null) {
			throw new IllegalStateException("Gemeinde Bern not found");
		}
		return bern;
	}

	@Nonnull
	private Gesuchsperiode getGesuchsperiode(@Nullable TestdataSetupConfig setupConfig, @Nullable ErstgesuchConfig erstgesuchConfig) {
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

	private void insertInstitutionsstammdatenForTestfaelle(@Nonnull TestdataSetupConfig config, @Nonnull Mandant mandant, @Nonnull Gesuchsperiode gesuchsperiode) {
		final InstitutionStammdaten institutionStammdatenKitaAaregg = config.getKitaWeissenstein();
		final InstitutionStammdaten institutionStammdatenKitaBruennen = config.getKitaBruennen();
		final InstitutionStammdaten institutionStammdatenTagiAaregg = config.getTagiWeissenstein();
		final InstitutionStammdaten institutionStammdatenTagesschuleBruennen = config.getTagesschuleBruennen();
		final InstitutionStammdaten institutionStammdatenFerieninselBruennen = config.getFerieninselBruennen();

		Traegerschaft traegerschaftAaregg = institutionStammdatenKitaAaregg.getInstitution().getTraegerschaft();
		traegerschaftAaregg = persistence.persist(traegerschaftAaregg);

		Traegerschaft traegerschaftBruennen = institutionStammdatenKitaBruennen.getInstitution().getTraegerschaft();
		traegerschaftBruennen = persistence.persist(traegerschaftBruennen);

		institutionStammdatenKitaAaregg.getInstitution().setMandant(mandant);
		institutionStammdatenKitaAaregg.getInstitution().setTraegerschaft(traegerschaftAaregg);
		institutionStammdatenKitaBruennen.getInstitution().setMandant(mandant);
		institutionStammdatenKitaBruennen.getInstitution().setTraegerschaft(traegerschaftBruennen);
		institutionStammdatenTagiAaregg.getInstitution().setMandant(mandant);
		institutionStammdatenTagiAaregg.getInstitution().setTraegerschaft(traegerschaftAaregg);

		institutionService.createInstitution(institutionStammdatenKitaAaregg.getInstitution());
		saveInstitutionStammdatenIfNecessary(institutionStammdatenKitaAaregg, gesuchsperiode);
		saveInstitutionStammdatenIfNecessary(institutionStammdatenTagiAaregg, gesuchsperiode);

		institutionService.createInstitution(institutionStammdatenKitaBruennen.getInstitution());
		saveInstitutionStammdatenIfNecessary(institutionStammdatenKitaBruennen, gesuchsperiode);
		saveInstitutionStammdatenIfNecessary(institutionStammdatenTagesschuleBruennen, gesuchsperiode);
		saveInstitutionStammdatenIfNecessary(institutionStammdatenFerieninselBruennen, gesuchsperiode);
	}

	private void saveInstitutionStammdatenIfNecessary(@Nullable InstitutionStammdaten institutionStammdaten, @Nonnull Gesuchsperiode gesuchsperiode) {
		if (institutionStammdaten != null) {
			Collection<InstitutionStammdaten> existing = institutionStammdatenService
				.getAllInstitutionStammdatenByInstitutionAndGesuchsperiode(
				institutionStammdaten.getInstitution().getId(), institutionStammdaten.getBetreuungsangebotTyp(), gesuchsperiode);
			if (existing.isEmpty()) {
				institutionStammdatenService.saveInstitutionStammdaten(institutionStammdaten);
			}
		}
	}

	@Override
	public void insertParametersForTestfaelle(@Nonnull Gesuchsperiode gesuchsperiode) {
		saveEinstellung(PARAM_PAUSCHALABZUG_PRO_PERSON_FAMILIENGROESSE_3, "3760", gesuchsperiode);
		saveEinstellung(PARAM_PAUSCHALABZUG_PRO_PERSON_FAMILIENGROESSE_4, "5900", gesuchsperiode);
		saveEinstellung(PARAM_PAUSCHALABZUG_PRO_PERSON_FAMILIENGROESSE_5, "6970", gesuchsperiode);
		saveEinstellung(PARAM_PAUSCHALABZUG_PRO_PERSON_FAMILIENGROESSE_6, "7500", gesuchsperiode);
		saveEinstellung(PARAM_GRENZWERT_EINKOMMENSVERSCHLECHTERUNG, "20", gesuchsperiode);
		saveEinstellung(PARAM_MAXIMALER_ZUSCHLAG_ERWERBSPENSUM, "20", gesuchsperiode);
		saveEinstellung(PARAM_PENSUM_KITA_MIN, "0", gesuchsperiode);
		saveEinstellung(PARAM_PENSUM_TAGESELTERN_MIN, "0", gesuchsperiode);
		saveEinstellung(PARAM_PENSUM_TAGESSCHULE_MIN, "0", gesuchsperiode);
		saveEinstellung(KONTINGENTIERUNG_ENABLED, "false", gesuchsperiode);
		saveEinstellung(BG_BIS_UND_MIT_SCHULSTUFE, EinschulungTyp.VORSCHULALTER.name(), gesuchsperiode);
		saveEinstellung(PARAM_MAX_TAGE_ABWESENHEIT, "30", gesuchsperiode);
		saveEinstellung(MAX_VERGUENSTIGUNG_VORSCHULE_BABY_PRO_TG,"140", gesuchsperiode);
		saveEinstellung(MAX_VERGUENSTIGUNG_VORSCHULE_KIND_PRO_TG, "100", gesuchsperiode);
		saveEinstellung(MAX_VERGUENSTIGUNG_SCHULE_PRO_TG, "75", gesuchsperiode);
		saveEinstellung(MAX_VERGUENSTIGUNG_VORSCHULE_BABY_PRO_STD, "11.90", gesuchsperiode);
		saveEinstellung(MAX_VERGUENSTIGUNG_VORSCHULE_KIND_PRO_STD, "8.50", gesuchsperiode);
		saveEinstellung(MAX_VERGUENSTIGUNG_SCHULE_PRO_STD, "8.50", gesuchsperiode);
		saveEinstellung(MAX_MASSGEBENDES_EINKOMMEN, "160000", gesuchsperiode);
		saveEinstellung(MIN_MASSGEBENDES_EINKOMMEN, "43000", gesuchsperiode);
		saveEinstellung(OEFFNUNGSTAGE_KITA, "240", gesuchsperiode);
		saveEinstellung(OEFFNUNGSTAGE_TFO, "240", gesuchsperiode);
		saveEinstellung(OEFFNUNGSSTUNDEN_TFO, "11", gesuchsperiode);
		saveEinstellung(ZUSCHLAG_BEHINDERUNG_PRO_TG, "50", gesuchsperiode);
		saveEinstellung(ZUSCHLAG_BEHINDERUNG_PRO_STD, "4.25", gesuchsperiode);
		saveEinstellung(MIN_VERGUENSTIGUNG_PRO_TG, "7", gesuchsperiode);
		saveEinstellung(MIN_VERGUENSTIGUNG_PRO_STD,"0.70", gesuchsperiode);
	}

	public void saveEinstellung(EinstellungKey key, String value, Gesuchsperiode gesuchsperiode) {
		Einstellung einstellungen = new Einstellung(key, value, gesuchsperiode);
		persistence.persist(einstellungen);
	}

	private InstitutionStammdaten getInstitutionStammdaten(@Nonnull AnmeldungConfig config) {
		if (config.getInstitutionStammdaten() != null) {
			return persistence.merge(config.getInstitutionStammdaten());
		}
		Collection<InstitutionStammdaten> institutionen = criteriaQueryHelper.getEntitiesByAttribute(InstitutionStammdaten.class,
			config.getBetreuungsangebotTyp(), InstitutionStammdaten_.betreuungsangebotTyp);
		if (institutionen.isEmpty()) {
			throw new IllegalStateException("Keine Institution mit Typ " + config.getBetreuungsangebotTyp() + " gefunden");
		}
		return institutionen.iterator().next();
	}
}
