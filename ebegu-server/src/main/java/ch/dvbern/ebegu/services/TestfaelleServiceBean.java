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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Month;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.activation.MimeType;
import javax.activation.MimeTypeParseException;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.PostConstruct;
import javax.ejb.Local;
import javax.ejb.Stateless;
import javax.inject.Inject;

import ch.dvbern.ebegu.authentication.PrincipalBean;
import ch.dvbern.ebegu.config.EbeguConfiguration;
import ch.dvbern.ebegu.einladung.Einladung;
import ch.dvbern.ebegu.entities.Adresse;
import ch.dvbern.ebegu.entities.AdresseTyp;
import ch.dvbern.ebegu.entities.AnmeldungTagesschule;
import ch.dvbern.ebegu.entities.Benutzer;
import ch.dvbern.ebegu.entities.Betreuung;
import ch.dvbern.ebegu.entities.Dossier;
import ch.dvbern.ebegu.entities.Einstellung;
import ch.dvbern.ebegu.entities.Erwerbspensum;
import ch.dvbern.ebegu.entities.ErwerbspensumContainer;
import ch.dvbern.ebegu.entities.Fall;
import ch.dvbern.ebegu.entities.Familiensituation;
import ch.dvbern.ebegu.entities.FamiliensituationContainer;
import ch.dvbern.ebegu.entities.Gemeinde;
import ch.dvbern.ebegu.entities.GemeindeStammdaten;
import ch.dvbern.ebegu.entities.GemeindeStammdatenKorrespondenz;
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.entities.Gesuchsperiode;
import ch.dvbern.ebegu.entities.Gesuchsteller;
import ch.dvbern.ebegu.entities.GesuchstellerAdresse;
import ch.dvbern.ebegu.entities.GesuchstellerAdresseContainer;
import ch.dvbern.ebegu.entities.GesuchstellerContainer;
import ch.dvbern.ebegu.entities.InstitutionStammdaten;
import ch.dvbern.ebegu.entities.Mandant;
import ch.dvbern.ebegu.entities.Mitteilung;
import ch.dvbern.ebegu.entities.WizardStep;
import ch.dvbern.ebegu.entities.gemeindeantrag.FerienbetreuungAngabenContainer;
import ch.dvbern.ebegu.entities.gemeindeantrag.LastenausgleichTagesschuleAngabenGemeindeContainer;
import ch.dvbern.ebegu.entities.sozialdienst.Sozialdienst;
import ch.dvbern.ebegu.entities.sozialdienst.SozialdienstFallDokument;
import ch.dvbern.ebegu.enums.AntragStatus;
import ch.dvbern.ebegu.enums.Betreuungsstatus;
import ch.dvbern.ebegu.enums.Eingangsart;
import ch.dvbern.ebegu.enums.EinstellungKey;
import ch.dvbern.ebegu.enums.EnumFamilienstatus;
import ch.dvbern.ebegu.enums.ErrorCodeEnum;
import ch.dvbern.ebegu.enums.FinSitStatus;
import ch.dvbern.ebegu.enums.FinanzielleSituationTyp;
import ch.dvbern.ebegu.enums.Geschlecht;
import ch.dvbern.ebegu.enums.GesuchDeletionCause;
import ch.dvbern.ebegu.enums.KorrespondenzSpracheTyp;
import ch.dvbern.ebegu.enums.Sprache;
import ch.dvbern.ebegu.enums.Taetigkeit;
import ch.dvbern.ebegu.enums.WizardStepName;
import ch.dvbern.ebegu.enums.WizardStepStatus;
import ch.dvbern.ebegu.enums.gemeindeantrag.FerienbetreuungAngabenStatus;
import ch.dvbern.ebegu.enums.gemeindeantrag.LastenausgleichTagesschuleAngabenGemeindeStatus;
import ch.dvbern.ebegu.errors.EbeguEntityNotFoundException;
import ch.dvbern.ebegu.errors.EbeguRuntimeException;
import ch.dvbern.ebegu.errors.MailException;
import ch.dvbern.ebegu.errors.MergeDocException;
import ch.dvbern.ebegu.services.gemeindeantrag.FerienbetreuungService;
import ch.dvbern.ebegu.services.gemeindeantrag.LastenausgleichTagesschuleAngabenGemeindeService;
import ch.dvbern.ebegu.services.gemeindeantrag.LastenausgleichTagesschuleAngabenGemeindeStatusHistoryService;
import ch.dvbern.ebegu.testfaelle.AbstractASIVTestfall;
import ch.dvbern.ebegu.testfaelle.AbstractTestfall;
import ch.dvbern.ebegu.testfaelle.InstitutionStammdatenBuilder;
import ch.dvbern.ebegu.testfaelle.MandantSozialdienstVisitor;
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
import ch.dvbern.ebegu.testfaelle.Testfall11_SchulamtOnly;
import ch.dvbern.ebegu.testfaelle.InstitutionStammdatenBuilderVisitor;
import ch.dvbern.ebegu.testfaelle.Testfall_ASIV_01;
import ch.dvbern.ebegu.testfaelle.Testfall_ASIV_02;
import ch.dvbern.ebegu.testfaelle.Testfall_ASIV_03;
import ch.dvbern.ebegu.testfaelle.Testfall_ASIV_04;
import ch.dvbern.ebegu.testfaelle.Testfall_ASIV_05;
import ch.dvbern.ebegu.testfaelle.Testfall_ASIV_06;
import ch.dvbern.ebegu.testfaelle.Testfall_ASIV_07;
import ch.dvbern.ebegu.testfaelle.Testfall_ASIV_08;
import ch.dvbern.ebegu.testfaelle.Testfall_ASIV_09;
import ch.dvbern.ebegu.testfaelle.Testfall_ASIV_10;
import ch.dvbern.ebegu.testfaelle.Testfall_Sozialdienst;
import ch.dvbern.ebegu.testfaelle.testantraege.Testantrag_FB;
import ch.dvbern.ebegu.testfaelle.testantraege.Testantrag_LATS;
import ch.dvbern.ebegu.types.DateRange;
import ch.dvbern.ebegu.util.Constants;
import ch.dvbern.ebegu.util.EbeguUtil;
import ch.dvbern.ebegu.util.FreigabeCopyUtil;
import ch.dvbern.ebegu.util.UploadFileInfo;
import ch.dvbern.oss.lib.beanvalidation.embeddables.IBAN;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static java.util.Objects.requireNonNull;

/**
 * Service fuer erstellen und mutieren von Testfällen
 */
@Stateless
@Local(TestfaelleService.class)
public class TestfaelleServiceBean extends AbstractBaseService implements TestfaelleService {

	private static final Logger LOG = LoggerFactory.getLogger(TestfaelleServiceBean.class);

	@Inject
	private GesuchsperiodeService gesuchsperiodeService;
	@Inject
	private InstitutionStammdatenService institutionStammdatenService;
	@Inject
	private GesuchService gesuchService;
	@Inject
	private FallService fallService;
	@Inject
	private BenutzerService benutzerService;
	@Inject
	private FamiliensituationService familiensituationService;
	@Inject
	private GesuchstellerService gesuchstellerService;
	@Inject
	private KindService kindService;
	@Inject
	private BetreuungService betreuungService;
	@Inject
	private ErwerbspensumService erwerbspensumService;
	@Inject
	private FinanzielleSituationService finanzielleSituationService;
	@Inject
	private EinkommensverschlechterungInfoService einkommensverschlechterungInfoService;
	@Inject
	private EinkommensverschlechterungService einkommensverschlechterungService;
	@Inject
	private WizardStepService wizardStepService;
	@Inject
	private VerfuegungService verfuegungService;
	@Inject
	private GeneratedDokumentService genDokServiceBean;
	@Inject
	private DossierService dossierService;
	@Inject
	private GemeindeService gemeindeService;
	@Inject
	private MailService mailService;
	@Inject
	private EbeguConfiguration configuration;
	@Inject
	private TestfaelleService testfaelleService;
	@Inject
	private SozialdienstService sozialdienstService;
	@Inject
	private LastenausgleichTagesschuleAngabenGemeindeService latsService;
	@Inject
	private LastenausgleichTagesschuleAngabenGemeindeStatusHistoryService historyService;
	@Inject
	private FerienbetreuungService ferienbetreuungService;
	@Inject
	private FileSaverService fileSaverService;
	@Inject
	private SozialdienstFallDokumentService sozialdienstFallDokumentService;
	@Inject
	private EinstellungService einstellungService;
	@Inject
	private PrincipalBean principalBean;

	private InstitutionStammdatenBuilderVisitor testfallDependenciesVisitor;
	private MandantSozialdienstVisitor mandantSozialdienstVisitor;

	@PostConstruct
	public void createFactory(){
		testfallDependenciesVisitor = new InstitutionStammdatenBuilderVisitor(institutionStammdatenService);
		mandantSozialdienstVisitor = new MandantSozialdienstVisitor(sozialdienstService);
	}

	@Override
	@Nonnull
	public StringBuilder createAndSaveTestfaelle(@Nonnull Mandant mandant, @Nonnull String fallid, boolean betreuungenBestaetigt, boolean verfuegen,
			@Nullable String gesuchsPeriodeId, @Nonnull String gemeindeId) {
		return createAndSaveTestfaelle(mandant, fallid, 1, betreuungenBestaetigt, verfuegen, null, gesuchsPeriodeId, gemeindeId);
	}

	@Nonnull
	@SuppressWarnings({ "PMD.NcssMethodCount", "PMD.AvoidDuplicateLiterals" })
	public StringBuilder createAndSaveTestfaelle(
			@Nonnull Mandant mandant,
			@Nonnull String fallid,
			@Nullable Integer iterationCount,
			boolean betreuungenBestaetigt,
			boolean verfuegen,
			@Nullable Benutzer besitzer,
			@Nullable String gesuchsPeriodeId,
			@Nonnull String gemeindeId) {

		iterationCount = (iterationCount == null || iterationCount == 0) ? 1 : iterationCount;

		Gesuchsperiode gesuchsperiode;
		if (StringUtils.isNotEmpty(gesuchsPeriodeId)) {
			gesuchsperiode = gesuchsperiodeService.findGesuchsperiode(gesuchsPeriodeId).orElseThrow(() -> new EbeguEntityNotFoundException("createAndSaveTestfaelle",
				ErrorCodeEnum.ERROR_ENTITY_NOT_FOUND, gesuchsPeriodeId));
		} else {
			gesuchsperiode = getNeuesteGesuchsperiode();
		}

		Gemeinde gemeinde = gemeindeService.findGemeinde(gemeindeId).orElseThrow(() -> new EbeguEntityNotFoundException("createAndSaveTestfaelle",
			ErrorCodeEnum.ERROR_ENTITY_NOT_FOUND, gemeindeId));

		InstitutionStammdatenBuilder institutionStammdatenBuilder = testfallDependenciesVisitor.process(mandant);

		StringBuilder responseString = new StringBuilder();
		for (int i = 0; i < iterationCount; i++) {

			if (WAELTI_DAGMAR.equals(fallid)) {
				final Gesuch gesuch = createAndSaveGesuch(new Testfall01_WaeltiDagmar(gesuchsperiode,
								betreuungenBestaetigt, gemeinde, institutionStammdatenBuilder),
					verfuegen, besitzer);
				responseString.append("Fall Dagmar Waelti erstellt, Fallnummer: '").append(gesuch.getFall().getFallNummer()).append("', AntragID: ").append(gesuch.getId());
			} else if (FEUTZ_IVONNE.equals(fallid)) {
				final Gesuch gesuch = createAndSaveGesuch(new Testfall02_FeutzYvonne(gesuchsperiode,
						betreuungenBestaetigt, gemeinde, institutionStammdatenBuilder), verfuegen, besitzer);
				responseString.append("Fall Yvonne Feutz erstellt, Fallnummer: '").append(gesuch.getFall().getFallNummer()).append("', AntragID: ").append(gesuch.getId());
			} else if (PERREIRA_MARCIA.equals(fallid)) {
				final Gesuch gesuch = createAndSaveGesuch(new Testfall03_PerreiraMarcia(gesuchsperiode,
						betreuungenBestaetigt, gemeinde, institutionStammdatenBuilder), verfuegen, besitzer);
				responseString.append("Fall Marcia Perreira erstellt, Fallnummer: '").append(gesuch.getFall().getFallNummer()).append("', AntragID: ").append(gesuch.getId());
			} else if (WALTHER_LAURA.equals(fallid)) {
				final Gesuch gesuch = createAndSaveGesuch(new Testfall04_WaltherLaura(gesuchsperiode,
						betreuungenBestaetigt, gemeinde, institutionStammdatenBuilder), verfuegen, besitzer);
				responseString.append("Fall Laura Walther erstellt, Fallnummer: '").append(gesuch.getFall().getFallNummer()).append("', AntragID: ").append(gesuch.getId());
			} else if (LUETHI_MERET.equals(fallid)) {
				final Gesuch gesuch = createAndSaveGesuch(new Testfall05_LuethiMeret(gesuchsperiode,
						betreuungenBestaetigt, gemeinde, institutionStammdatenBuilder), verfuegen, besitzer);
				responseString.append("Fall Meret Luethi erstellt, Fallnummer: '").append(gesuch.getFall().getFallNummer()).append("', AntragID: ").append(gesuch.getId());
			} else if (BECKER_NORA.equals(fallid)) {
				final Gesuch gesuch = createAndSaveGesuch(new Testfall06_BeckerNora(gesuchsperiode,
						betreuungenBestaetigt, gemeinde, institutionStammdatenBuilder), verfuegen, besitzer);
				responseString.append("Fall Nora Becker erstellt, Fallnummer: '").append(gesuch.getFall().getFallNummer()).append("', AntragID: ").append(gesuch.getId());
			} else if (MEIER_MERET.equals(fallid)) {
				final Gesuch gesuch = createAndSaveGesuch(new Testfall07_MeierMeret(gesuchsperiode,
						betreuungenBestaetigt, gemeinde, institutionStammdatenBuilder), verfuegen, besitzer);
				responseString.append("Fall Meier Meret erstellt, Fallnummer: '").append(gesuch.getFall().getFallNummer()).append("', AntragID: ").append(gesuch.getId());
			} else if (UMZUG_AUS_IN_AUS_BERN.equals(fallid)) {
				final Gesuch gesuch = createAndSaveGesuch(new Testfall08_UmzugAusInAusBern(gesuchsperiode,
						betreuungenBestaetigt, gemeinde, institutionStammdatenBuilder), verfuegen, besitzer);
				responseString.append("Fall Umzug Aus-In-Aus Bern Fallnummer: '").append(gesuch.getFall().getFallNummer()).append("', AntragID: ").append(gesuch.getId());
			} else if (UMZUG_VOR_GESUCHSPERIODE.equals(fallid)) {
				final Gesuch gesuch = createAndSaveGesuch(new Testfall10_UmzugVorGesuchsperiode(gesuchsperiode,
						betreuungenBestaetigt, gemeinde, institutionStammdatenBuilder), verfuegen, besitzer);
				responseString.append("Fall Umzug Vor Gesuchsperiode Fallnummer: '").append(gesuch.getFall().getFallNummer()).append("', AntragID: ").append(gesuch.getId());
			} else if (ABWESENHEIT.equals(fallid)) {
				final Gesuch gesuch = createAndSaveGesuch(new Testfall09_Abwesenheit(gesuchsperiode,
						betreuungenBestaetigt, gemeinde, institutionStammdatenBuilder), verfuegen, besitzer);
				responseString.append("Fall Abwesenheit Fallnummer: ").append(gesuch.getFall().getFallNummer()).append("', AntragID: ").append(gesuch.getId());
			} else if (SCHULAMT_ONLY.equals(fallid)) {
				final Gesuch gesuch = createAndSaveGesuch(new Testfall11_SchulamtOnly(gesuchsperiode,
						betreuungenBestaetigt, gemeinde,
						institutionStammdatenBuilder), verfuegen, besitzer);
				responseString.append("Fall Schulamt Only Fallnummer: ").append(gesuch.getFall().getFallNummer()).append("', AntragID: ").append(gesuch.getId());
			} else if (ASIV1.equals(fallid)) {
				final Gesuch gesuch = createAndSaveAsivGesuch(new Testfall_ASIV_01(gesuchsperiode, true, gemeinde, institutionStammdatenBuilder), verfuegen, besitzer);
				responseString.append("Fall ASIV 1 Fallnummer: ").append(gesuch.getFall().getFallNummer()).append("', AntragID: ").append(gesuch.getId());
			} else if (ASIV2.equals(fallid)) {
				final Gesuch gesuch = createAndSaveAsivGesuch(new Testfall_ASIV_02(gesuchsperiode, true, gemeinde, institutionStammdatenBuilder), verfuegen, besitzer);
				responseString.append("Fall ASIV 2 Fallnummer: ").append(gesuch.getFall().getFallNummer()).append("', AntragID: ").append(gesuch.getId());
			} else if (ASIV3.equals(fallid)) {
				final Gesuch gesuch = createAndSaveAsivGesuch(new Testfall_ASIV_03(gesuchsperiode, true, gemeinde, institutionStammdatenBuilder), verfuegen, besitzer);
				responseString.append("Fall ASIV 3 Fallnummer: ").append(gesuch.getFall().getFallNummer()).append("', AntragID: ").append(gesuch.getId());
			} else if (ASIV4.equals(fallid)) {
				final Gesuch gesuch = createAndSaveAsivGesuch(new Testfall_ASIV_04(gesuchsperiode, true, gemeinde, institutionStammdatenBuilder), verfuegen, besitzer);
				responseString.append("Fall ASIV 4 Fallnummer: ").append(gesuch.getFall().getFallNummer()).append("', AntragID: ").append(gesuch.getId());
			} else if (ASIV5.equals(fallid)) {
				final Gesuch gesuch = createAndSaveAsivGesuch(new Testfall_ASIV_05(gesuchsperiode, true, gemeinde, institutionStammdatenBuilder), verfuegen, besitzer);
				responseString.append("Fall ASIV 5 Fallnummer: ").append(gesuch.getFall().getFallNummer()).append("', AntragID: ").append(gesuch.getId());
			} else if (ASIV6.equals(fallid)) {
				final Gesuch gesuch = createAndSaveAsivGesuch(new Testfall_ASIV_06(gesuchsperiode, true, gemeinde, institutionStammdatenBuilder), verfuegen, besitzer);
				responseString.append("Fall ASIV 6 Fallnummer: ").append(gesuch.getFall().getFallNummer()).append("', AntragID: ").append(gesuch.getId());
			} else if (ASIV7.equals(fallid)) {
				final Gesuch gesuch = createAndSaveAsivGesuch(new Testfall_ASIV_07(gesuchsperiode, true, gemeinde, institutionStammdatenBuilder), verfuegen, besitzer);
				responseString.append("Fall ASIV 7 Fallnummer: ").append(gesuch.getFall().getFallNummer()).append("', AntragID: ").append(gesuch.getId());
			} else if (ASIV8.equals(fallid)) {
				final Gesuch gesuch = createAndSaveAsivGesuch(new Testfall_ASIV_08(gesuchsperiode, true, gemeinde, institutionStammdatenBuilder), verfuegen, besitzer);
				responseString.append("Fall ASIV 8 Fallnummer: ").append(gesuch.getFall().getFallNummer()).append("', AntragID: ").append(gesuch.getId());
			} else if (ASIV9.equals(fallid)) {
				final Gesuch gesuch = createAndSaveAsivGesuch(new Testfall_ASIV_09(gesuchsperiode,
						true, gemeinde,
						institutionStammdatenBuilder), verfuegen, besitzer);
				responseString.append("Fall ASIV 9 Fallnummer: ").append(gesuch.getFall().getFallNummer()).append("', AntragID: ").append(gesuch.getId());
			} else if (ASIV10.equals(fallid)) {
				final Gesuch gesuch = createAndSaveAsivGesuch(new Testfall_ASIV_10(gesuchsperiode,
						true, gemeinde,
						institutionStammdatenBuilder), verfuegen, besitzer);
				responseString.append("Fall ASIV 10 Fallnummer: ").append(gesuch.getFall().getFallNummer()).append("', AntragID: ").append(gesuch.getId());
			} else if (SOZIALDIENST.equals(fallid)) {
				Sozialdienst sozialdienst = mandantSozialdienstVisitor.process(mandant);
				Testfall_Sozialdienst testfallSozialdienst = new Testfall_Sozialdienst(
					gesuchsperiode,
						betreuungenBestaetigt,
					gemeinde,
					sozialdienst,
						institutionStammdatenBuilder);
				final Gesuch gesuch = createAndSaveGesuch(testfallSozialdienst, verfuegen, null);
				createAndSaveSozialdienstFallDokument(gesuch.getFall());
				responseString.append("Fall Sozialdienst Fallnummer: ").append(gesuch.getFall().getFallNummer()).append("', AntragID: ").append(gesuch.getId());
			} else if ("all".equals(fallid)) {
				createAndSaveGesuch(new Testfall01_WaeltiDagmar(gesuchsperiode, betreuungenBestaetigt, gemeinde, institutionStammdatenBuilder), verfuegen, besitzer);
				createAndSaveGesuch(new Testfall02_FeutzYvonne(gesuchsperiode, betreuungenBestaetigt, gemeinde, institutionStammdatenBuilder), verfuegen, besitzer);
				createAndSaveGesuch(new Testfall03_PerreiraMarcia(gesuchsperiode, betreuungenBestaetigt, gemeinde, institutionStammdatenBuilder), verfuegen, besitzer);
				createAndSaveGesuch(new Testfall04_WaltherLaura(gesuchsperiode, betreuungenBestaetigt, gemeinde, institutionStammdatenBuilder), verfuegen, besitzer);
				createAndSaveGesuch(new Testfall05_LuethiMeret(gesuchsperiode, betreuungenBestaetigt, gemeinde, institutionStammdatenBuilder), verfuegen, besitzer);
				createAndSaveGesuch(new Testfall06_BeckerNora(gesuchsperiode, betreuungenBestaetigt, gemeinde, institutionStammdatenBuilder), verfuegen, besitzer);
				createAndSaveGesuch(new Testfall07_MeierMeret(gesuchsperiode, betreuungenBestaetigt, gemeinde, institutionStammdatenBuilder), verfuegen, besitzer);
				createAndSaveGesuch(new Testfall08_UmzugAusInAusBern(gesuchsperiode, betreuungenBestaetigt, gemeinde, institutionStammdatenBuilder), verfuegen, besitzer);
				createAndSaveGesuch(new Testfall09_Abwesenheit(gesuchsperiode, betreuungenBestaetigt, gemeinde, institutionStammdatenBuilder), verfuegen, besitzer);
				createAndSaveGesuch(new Testfall10_UmzugVorGesuchsperiode(gesuchsperiode,
						betreuungenBestaetigt, gemeinde, institutionStammdatenBuilder), verfuegen, besitzer);
				createAndSaveGesuch(new Testfall11_SchulamtOnly(gesuchsperiode,
						betreuungenBestaetigt, gemeinde,
						institutionStammdatenBuilder), verfuegen, besitzer);
				createAndSaveAsivGesuch(new Testfall_ASIV_01(gesuchsperiode, true, gemeinde, institutionStammdatenBuilder), verfuegen, besitzer);
				createAndSaveAsivGesuch(new Testfall_ASIV_02(gesuchsperiode, true, gemeinde, institutionStammdatenBuilder), verfuegen, besitzer);
				createAndSaveAsivGesuch(new Testfall_ASIV_03(gesuchsperiode, true, gemeinde, institutionStammdatenBuilder), verfuegen, besitzer);
				createAndSaveAsivGesuch(new Testfall_ASIV_04(gesuchsperiode, true, gemeinde, institutionStammdatenBuilder), verfuegen, besitzer);
				createAndSaveAsivGesuch(new Testfall_ASIV_05(gesuchsperiode, true, gemeinde, institutionStammdatenBuilder), verfuegen, besitzer);
				createAndSaveAsivGesuch(new Testfall_ASIV_06(gesuchsperiode, true, gemeinde, institutionStammdatenBuilder), verfuegen, besitzer);
				createAndSaveAsivGesuch(new Testfall_ASIV_07(gesuchsperiode, true, gemeinde, institutionStammdatenBuilder), verfuegen, besitzer);
				createAndSaveAsivGesuch(new Testfall_ASIV_08(gesuchsperiode, true, gemeinde, institutionStammdatenBuilder), verfuegen, besitzer);
				createAndSaveAsivGesuch(new Testfall_ASIV_09(gesuchsperiode,
						true, gemeinde,
						institutionStammdatenBuilder), verfuegen, besitzer);
				createAndSaveAsivGesuch(new Testfall_ASIV_10(gesuchsperiode,
						true, gemeinde,
						institutionStammdatenBuilder), verfuegen, besitzer);
				createAndSaveGesuch(new Testfall_Sozialdienst(gesuchsperiode,
						betreuungenBestaetigt, gemeinde, mandantSozialdienstVisitor.process(mandant),
						institutionStammdatenBuilder), verfuegen, null);
				responseString.append("Testfaelle 1-11, ASIV-Testfaelle 1-10 und Sozialdiensttestfall erstellt");
			} else {
				responseString.append("Usage: /Nummer des Testfalls an die URL anhaengen. Bisher umgesetzt: 1-11. "
					+ "'/all' erstellt alle Testfaelle");
			}
		}
		return responseString;
	}

	@Override
	@Nonnull
	public StringBuilder createAndSaveAsOnlineGesuch(@Nonnull String fallid, boolean betreuungenBestaetigt, boolean verfuegen, @Nonnull String username,
			@Nullable String gesuchsPeriodeId, @Nonnull String gemeindeId, @Nonnull Mandant mandant) {
		removeGesucheOfGS(username, mandant);
		Benutzer benutzer = benutzerService.findBenutzer(username, mandant).orElse(benutzerService.getCurrentBenutzer().orElse(null));
		return this.createAndSaveTestfaelle(mandant, fallid, 1, betreuungenBestaetigt, verfuegen, benutzer, gesuchsPeriodeId, gemeindeId);
	}

	@Nonnull
	@Override
	@SuppressWarnings("PMD.NcssMethodCount")
	public Gesuch createAndSaveTestfaelle(
			@Nonnull String fallid, boolean betreuungenBestaetigt, boolean verfuegen, @Nonnull String gemeindeId,
			@Nonnull Gesuchsperiode gesuchsperiode, Mandant mandant) {
		Gemeinde gemeinde = gemeindeService.findGemeinde(gemeindeId).orElseThrow(() -> new EbeguEntityNotFoundException("createAndSaveTestfaelle",
			ErrorCodeEnum.ERROR_ENTITY_NOT_FOUND, gemeindeId));

		InstitutionStammdatenBuilder institutionStammdatenBuilder =  testfallDependenciesVisitor.process(mandant);;

		if (WAELTI_DAGMAR.equals(fallid)) {
			return createAndSaveGesuch(new Testfall01_WaeltiDagmar(gesuchsperiode, betreuungenBestaetigt, gemeinde, institutionStammdatenBuilder), verfuegen, null);
		}
		if (FEUTZ_IVONNE.equals(fallid)) {
			return createAndSaveGesuch(new Testfall02_FeutzYvonne(gesuchsperiode, betreuungenBestaetigt, gemeinde, institutionStammdatenBuilder), verfuegen, null);
		}
		if (PERREIRA_MARCIA.equals(fallid)) {
			return createAndSaveGesuch(new Testfall03_PerreiraMarcia(gesuchsperiode, betreuungenBestaetigt, gemeinde, institutionStammdatenBuilder), verfuegen, null);
		}
		if (WALTHER_LAURA.equals(fallid)) {
			return createAndSaveGesuch(new Testfall04_WaltherLaura(gesuchsperiode, betreuungenBestaetigt, gemeinde, institutionStammdatenBuilder), verfuegen, null);
		}
		if (LUETHI_MERET.equals(fallid)) {
			return createAndSaveGesuch(new Testfall05_LuethiMeret(gesuchsperiode, betreuungenBestaetigt, gemeinde, institutionStammdatenBuilder), verfuegen, null);
		}
		if (BECKER_NORA.equals(fallid)) {
			return createAndSaveGesuch(new Testfall06_BeckerNora(gesuchsperiode, betreuungenBestaetigt, gemeinde, institutionStammdatenBuilder), verfuegen, null);
		}
		if (MEIER_MERET.equals(fallid)) {
			return createAndSaveGesuch(new Testfall07_MeierMeret(gesuchsperiode, betreuungenBestaetigt, gemeinde, institutionStammdatenBuilder), verfuegen, null);
		}
		if (UMZUG_AUS_IN_AUS_BERN.equals(fallid)) {
			return createAndSaveGesuch(new Testfall08_UmzugAusInAusBern(gesuchsperiode,
					betreuungenBestaetigt, gemeinde, institutionStammdatenBuilder), verfuegen, null);
		}
		if (ABWESENHEIT.equals(fallid)) {
			return createAndSaveGesuch(new Testfall09_Abwesenheit(gesuchsperiode, betreuungenBestaetigt, gemeinde, institutionStammdatenBuilder), verfuegen, null);
		}
		if (UMZUG_VOR_GESUCHSPERIODE.equals(fallid)) {
			return createAndSaveGesuch(new Testfall10_UmzugVorGesuchsperiode(gesuchsperiode,
					betreuungenBestaetigt, gemeinde, institutionStammdatenBuilder), verfuegen, null);
		}
		if (SCHULAMT_ONLY.equals(fallid)) {
			return createAndSaveGesuch(new Testfall11_SchulamtOnly(gesuchsperiode,
					betreuungenBestaetigt, gemeinde,
					institutionStammdatenBuilder), verfuegen, null);
		}
		if (ASIV1.equals(fallid)) {
			return createAndSaveAsivGesuch(new Testfall_ASIV_01(gesuchsperiode, true, gemeinde, institutionStammdatenBuilder), verfuegen, null);
		}
		if (ASIV2.equals(fallid)) {
			return createAndSaveAsivGesuch(new Testfall_ASIV_02(gesuchsperiode, true, gemeinde, institutionStammdatenBuilder), verfuegen, null);
		}
		if (ASIV3.equals(fallid)) {
			return createAndSaveAsivGesuch(new Testfall_ASIV_03(gesuchsperiode, true, gemeinde, institutionStammdatenBuilder), verfuegen, null);
		}
		if (ASIV4.equals(fallid)) {
			return createAndSaveAsivGesuch(new Testfall_ASIV_04(gesuchsperiode, true, gemeinde, institutionStammdatenBuilder), verfuegen, null);
		}
		if (ASIV5.equals(fallid)) {
			return createAndSaveAsivGesuch(new Testfall_ASIV_05(gesuchsperiode, true, gemeinde, institutionStammdatenBuilder), verfuegen, null);
		}
		if (ASIV6.equals(fallid)) {
			return createAndSaveAsivGesuch(new Testfall_ASIV_06(gesuchsperiode, true, gemeinde, institutionStammdatenBuilder), verfuegen, null);
		}
		if (ASIV7.equals(fallid)) {
			return createAndSaveAsivGesuch(new Testfall_ASIV_07(gesuchsperiode, true, gemeinde, institutionStammdatenBuilder), verfuegen, null);
		}
		if (ASIV8.equals(fallid)) {
			return createAndSaveAsivGesuch(new Testfall_ASIV_08(gesuchsperiode, true, gemeinde, institutionStammdatenBuilder), verfuegen, null);
		}
		if (ASIV9.equals(fallid)) {
			return createAndSaveAsivGesuch(new Testfall_ASIV_09(gesuchsperiode,
					true, gemeinde,
					institutionStammdatenBuilder), verfuegen, null);
		}
		if (ASIV10.equals(fallid)) {
			return createAndSaveAsivGesuch(new Testfall_ASIV_10(gesuchsperiode,
					true, gemeinde,
					institutionStammdatenBuilder), verfuegen, null);
		}
		if (SOZIALDIENST.equals(fallid)) {
			return createAndSaveGesuch(new Testfall_Sozialdienst(gesuchsperiode,
					betreuungenBestaetigt, gemeinde, mandantSozialdienstVisitor.process(mandant),
					institutionStammdatenBuilder), verfuegen, null);
		}
		throw new IllegalArgumentException("Unbekannter Testfall: " + fallid);
	}

	@Override
	public void removeGesucheOfGS(@Nonnull String username, @Nonnull Mandant mandant) {
		Benutzer benutzer = benutzerService.findBenutzer(username, mandant).orElse(null);
		Optional<Fall> existingFall = fallService.findFallByBesitzer(benutzer);
		existingFall.ifPresent(fall -> fallService.removeFall(fall, GesuchDeletionCause.USER));
	}

	@Override
	@Nonnull
	public Gesuch mutierenHeirat(@Nonnull String dossierId, @Nonnull String gesuchsperiodeId,
		@Nonnull LocalDate eingangsdatum, @Nonnull LocalDate aenderungPer, boolean verfuegen) {

		requireNonNull(eingangsdatum);
		requireNonNull(gesuchsperiodeId);
		requireNonNull(dossierId);
		requireNonNull(aenderungPer);

		Familiensituation newFamsit = getFamiliensituationZuZweit(aenderungPer);
		Familiensituation oldFamsit = getFamiliensituationAlleine(null);

		Dossier dossier = dossierService.findDossier(dossierId).orElseThrow(() -> new EbeguEntityNotFoundException(
			"mutierenHeirat", "dossier konnte nicht geladen werden", dossierId));
		Gesuchsperiode gesuchsperiode = gesuchsperiodeService.findGesuchsperiode(gesuchsperiodeId).orElseThrow(() -> new EbeguEntityNotFoundException(
			"mutierenHeirat", "gesuchsperiode konnte nicht geladen werden", gesuchsperiodeId));

		Gesuch mutation = Gesuch.createMutation(dossier, gesuchsperiode, eingangsdatum);
		mutation = gesuchService.createGesuch(mutation);

		final FamiliensituationContainer familiensituationContainer = mutation.getFamiliensituationContainer();
		requireNonNull(familiensituationContainer, "Familiensituation muss gesetzt sein");
		familiensituationContainer.setFamiliensituationErstgesuch(oldFamsit);
		familiensituationContainer.setFamiliensituationJA(newFamsit);

		familiensituationService.saveFamiliensituation(mutation, familiensituationContainer, oldFamsit);
		requireNonNull(mutation.getGesuchsteller1(), "Gesuchsteller 1 muss gesetzt sein");
		final GesuchstellerContainer gesuchsteller2 = gesuchstellerService
			.saveGesuchsteller(createGesuchstellerHeirat(mutation.getGesuchsteller1()), mutation, 2, false);

		mutation.setGesuchsteller2(gesuchsteller2);
		gesuchService.updateGesuch(mutation, false);
		gesuchVerfuegenUndSpeichern(verfuegen, mutation, true, false);
		return mutation;
	}

	@Override
	@Nonnull
	public Gesuch mutierenFinSit(@Nonnull String dossierId, @Nonnull String gesuchsperiodeId, @Nonnull LocalDate eingangsdatum,
		@Nonnull LocalDate aenderungPer, boolean verfuegen, BigDecimal nettoLohn, boolean ignorieren) {

		requireNonNull(eingangsdatum);
		requireNonNull(gesuchsperiodeId);
		requireNonNull(dossierId);
		requireNonNull(aenderungPer);

		Dossier dossier = dossierService.findDossier(dossierId).orElseThrow(() -> new EbeguEntityNotFoundException(
			"mutierenFinSit", "dossier konnte nicht geladen werden", dossierId));
		Gesuchsperiode gesuchsperiode = gesuchsperiodeService.findGesuchsperiode(gesuchsperiodeId).orElseThrow(() -> new EbeguEntityNotFoundException(
			"mutierenFinSit", "gesuchsperiode konnte nicht geladen werden", gesuchsperiodeId));

		Gesuch mutation = Gesuch.createMutation(dossier, gesuchsperiode, eingangsdatum);
		mutation = gesuchService.createGesuch(mutation);

		requireNonNull(mutation.getGesuchsteller1(), "GS1 muss gesetzt sein");
		requireNonNull(mutation.getGesuchsteller1().getFinanzielleSituationContainer(), "FinSit vom GS1 muss gesetzt sein");
		mutation.getGesuchsteller1().getFinanzielleSituationContainer().getFinanzielleSituationJA().setNettolohn(nettoLohn);

		gesuchstellerService.saveGesuchsteller(mutation.getGesuchsteller1(), mutation, 1, false);
		gesuchService.updateGesuch(mutation, false);
		gesuchVerfuegenUndSpeichern(verfuegen, mutation, true, ignorieren);
		return mutation;
	}

	@Override
	@Nullable
	public Gesuch mutierenScheidung(@Nonnull String dossierId, @Nonnull String gesuchsperiodeId,
		@Nonnull LocalDate eingangsdatum, @Nonnull LocalDate aenderungPer, boolean verfuegen) {

		requireNonNull(eingangsdatum);
		requireNonNull(gesuchsperiodeId);
		requireNonNull(dossierId);
		requireNonNull(aenderungPer);

		Familiensituation oldFamsit = getFamiliensituationZuZweit(null);
		Familiensituation newFamsit = getFamiliensituationAlleine(aenderungPer);

		Dossier dossier = dossierService.findDossier(dossierId).orElseThrow(() -> new EbeguEntityNotFoundException(
			"mutierenScheidung", "dossier konnte nicht geladen werden", dossierId));
		Gesuchsperiode gesuchsperiode = gesuchsperiodeService.findGesuchsperiode(gesuchsperiodeId).orElseThrow(() -> new EbeguEntityNotFoundException(
			"mutierenScheidung", "gesuchsperiode konnte nicht geladen werden", gesuchsperiodeId));

		Gesuch mutation = Gesuch.createMutation(dossier, gesuchsperiode, eingangsdatum);
		mutation = gesuchService.createGesuch(mutation);

		final FamiliensituationContainer familiensituationContainer = mutation.getFamiliensituationContainer();
		requireNonNull(familiensituationContainer, "Familiensituation muss gesetzt sein");
		familiensituationContainer.setFamiliensituationErstgesuch(oldFamsit);
		familiensituationContainer.setFamiliensituationJA(newFamsit);

		familiensituationService.saveFamiliensituation(mutation, familiensituationContainer, oldFamsit);
		gesuchService.updateGesuch(mutation, false);
		gesuchVerfuegenUndSpeichern(verfuegen, mutation, true, false);
		return mutation;
	}

	@Nonnull
	private Gesuchsperiode getNeuesteGesuchsperiode() {
		Collection<Gesuchsperiode> allActiveGesuchsperioden = gesuchsperiodeService.getAllActiveGesuchsperioden();
		return allActiveGesuchsperioden.iterator().next();
	}

	@Override
	@Nonnull
	public List<InstitutionStammdaten> getInstitutionsstammdatenForTestfaelle(Mandant mandant) {
		InstitutionStammdatenBuilder stammdatenBuilder =  testfallDependenciesVisitor.process(mandant);;
		return stammdatenBuilder.buildStammdaten();
	}

	/**
	 * Diese Methode ist etwas lang und haesslich aber das ist weil wir versuchen, den ganzen Prozess zu simulieren. D.h. wir speichern
	 * alle Objekte hintereinander, um die entsprechenden Services auszufuehren, damit die interne Logik auch durchgefuehrt wird.
	 * Nachteil ist, dass man vor allem die WizardSteps vorbereiten muss, damit der Prozess so laeuft wie auf dem web browser.
	 * <p>
	 * Am Ende der Methode und zur Sicherheit, updaten wir das Gesuch ein letztes Mal, um uns zu vergewissern, dass alle Daten gespeichert wurden.
	 * <p>
	 * Die Methode geht davon aus, dass die Daten nur eingetragen wurden und noch keine Betreuung bzw. Verfuegung bearbeitet ist.
	 * Aus diesem Grund, bleibt das Gesuch mit Status IN_BEARBEITUNG_JA
	 *
	 * @param fromTestfall testfall
	 * @param besitzer wenn der besitzer gesetzt ist wird der fall diesem besitzer zugeordnet
	 */
	@Override
	@Nonnull
	@SuppressWarnings("PMD.NcssMethodCount")
	public Gesuch createAndSaveGesuch(
		@Nonnull AbstractTestfall fromTestfall,
		boolean verfuegen,
		@Nullable Benutzer besitzer
	) {
		final List<Gesuch> gesuche = gesuchService.findGesuchByGSName(fromTestfall.getNachname(), fromTestfall.getVorname());
		if (!gesuche.isEmpty()) {
			fromTestfall.setFall(gesuche.iterator().next().getFall());
		}

		final Optional<Benutzer> currentBenutzer = benutzerService.getCurrentBenutzer();
		Optional<Fall> fallByBesitzer = fallService.findFallByBesitzer(besitzer); //fall kann schon existieren
		Fall fall;
		Dossier dossier = null;
		if (!fallByBesitzer.isPresent()) {
			boolean nichtFreigegebenesOnlineGesuch = besitzer != null && !verfuegen;
			if (!nichtFreigegebenesOnlineGesuch && currentBenutzer.isPresent()) {
				// Wir setzen den aktuellen Benutzer als Verantwortliche Person, aber nur,
				// wenn es nicht ein nicht-freigegebenes OnlineGesuch ist
				fall = fromTestfall.createFall(currentBenutzer.get());
			} else {
				fall = fromTestfall.createFall();
			}
			dossier = fromTestfall.getDossier();
		} else {
			// Fall ist schon vorhanden
			fall = fallByBesitzer.get();
			fall.setNextNumberKind(1); //reset
			// Dossier ist möglicherweise auch schon vorhanden
			Collection<Dossier> dossiersByFall = dossierService.findDossiersByFall(fall.getId());
			if (dossiersByFall.isEmpty()) {
				dossier = new Dossier();
				dossier.setFall(fall);
			} else if (dossiersByFall.size() == 1) {
				dossier = dossiersByFall.iterator().next();
			} else {
				throw new IllegalStateException("Fall hat mehrere Dossiers. Dieser Zustand darf bei Testfaellen nicht vorkommen");
			}
		}
		if (besitzer != null) {
			fall.setBesitzer(besitzer);
		}
		Objects.requireNonNull(principalBean.getMandant());
		fall.setMandant(principalBean.getMandant());

		final Fall persistedFall = fallService.saveFall(fall);
		fromTestfall.setFall(persistedFall); // dies wird gebraucht, weil fallService.saveFall ein merge macht.

		final Dossier persistedDossier = dossierService.saveDossier(dossier);
		fromTestfall.setDossier(persistedDossier);

		fromTestfall.createGesuch(LocalDate.of(2016, Month.FEBRUARY, 15));
		gesuchService.createGesuch(fromTestfall.getGesuch());
		Gesuch gesuch = fromTestfall.fillInGesuch();

		//noinspection VariableNotUsedInsideIf Muss so sein
		if (besitzer != null) {
			gesuch.setStatus(AntragStatus.IN_BEARBEITUNG_GS);
			gesuch.setEingangsart(Eingangsart.ONLINE);
		} else if (fall.getSozialdienstFall() != null) {
			gesuch.setStatus(AntragStatus.IN_BEARBEITUNG_SOZIALDIENST);
			setWizardStepForSozialdienst(gesuch);
			gesuch.setEingangsart(Eingangsart.ONLINE);
		} else {
			gesuch.setEingangsart(Eingangsart.PAPIER);
		}

		Einstellung neuFamiSit = einstellungService.findEinstellung(EinstellungKey.FKJV_FAMILIENSITUATION_NEU, gesuch.extractGemeinde(), gesuch.getGesuchsperiode());
		Einstellung dauerKonkubinat = einstellungService.findEinstellung(EinstellungKey.MINIMALDAUER_KONKUBINAT, gesuch.extractGemeinde(), gesuch.getGesuchsperiode());
		requireNonNull(gesuch.extractFamiliensituation()).setFkjvFamSit(neuFamiSit.getValueAsBoolean());
		requireNonNull(gesuch.extractFamiliensituation()).setMinDauerKonkubinat(dauerKonkubinat.getValueAsInteger());

		gesuchVerfuegenUndSpeichern(verfuegen, gesuch, false, false);

		return gesuch;

	}

	private void setWizardStepForSozialdienst(@Nonnull Gesuch gesuch) {
		WizardStep gesuchStep = wizardStepService.findWizardStepFromGesuch(gesuch.getId(), WizardStepName.GESUCH_ERSTELLEN);
		gesuchStep.setWizardStepStatus(WizardStepStatus.OK);
		gesuchStep.setVerfuegbar(true);
		wizardStepService.saveWizardStep(gesuchStep);
	}

	@Nonnull
	public Gesuch createAndSaveAsivGesuch(@Nonnull AbstractASIVTestfall fromTestfall, boolean verfuegen,
		@Nullable Benutzer besitzer) {
		final Gesuch erstgesuch = createAndSaveGesuch(fromTestfall, true, besitzer);
		// Mutation
		Gesuch mutation = testfaelleService.antragMutieren(erstgesuch, LocalDate.of(2016, Month.MARCH, 1));
		mutation = fromTestfall.createMutation(mutation);
		gesuchService.updateGesuch(mutation, false, null);
		requireNonNull(mutation.getFamiliensituationContainer(), "Familiensituation muss gesetzt sein!");
		familiensituationService.saveFamiliensituation(mutation, mutation.getFamiliensituationContainer(), null);
		gesuchVerfuegenUndSpeichern(verfuegen, mutation, true, false);
		setWizardStepOkayAndVerfuegbar(wizardStepService.findWizardStepFromGesuch(mutation.getId(), WizardStepName.GESUCHSTELLER).getId());
		setWizardStepOkayAndVerfuegbar(wizardStepService.findWizardStepFromGesuch(mutation.getId(),
				getFinSitWizardStepName(mutation)).getId());
		setWizardStepOkayAndVerfuegbar(wizardStepService.findWizardStepFromGesuch(mutation.getId(), wizardStepService.getEKVWizardStepNameForGesuch(mutation)).getId());
		return mutation;
	}

	private WizardStepName getFinSitWizardStepName(Gesuch mutation) {
		if (mutation.getFinSitTyp().equals(FinanzielleSituationTyp.LUZERN)) {
			return WizardStepName.FINANZIELLE_SITUATION_LUZERN;
		}
		if (mutation.getFinSitTyp().equals(FinanzielleSituationTyp.SOLOTHURN)) {
			return WizardStepName.FINANZIELLE_SITUATION_SOLOTHURN;
		}
		return WizardStepName.FINANZIELLE_SITUATION;
	}

	private void setWizardStepOkayAndVerfuegbar(@Nonnull String wizardStepId) {
		Optional<WizardStep> wizardStep = wizardStepService.findWizardStep(wizardStepId);
		if (wizardStep.isPresent()) {
			wizardStep.get().setVerfuegbar(true);
			wizardStep.get().setWizardStepStatus(WizardStepStatus.OK);
		}
	}

	@Override
	public void gesuchVerfuegenUndSpeichern(boolean verfuegen, @Nonnull Gesuch gesuch, boolean mutation, boolean ignorierenInZahlungslauf) {
		final List<WizardStep> wizardStepsFromGesuch = wizardStepService.findWizardStepsFromGesuch(gesuch.getId());

		if (!mutation) {
			saveFamiliensituation(gesuch, wizardStepsFromGesuch);
			saveGesuchsteller(gesuch, wizardStepsFromGesuch);
			saveKinder(gesuch, wizardStepsFromGesuch);
			saveBetreuungen(gesuch, wizardStepsFromGesuch);
			saveErwerbspensen(gesuch, wizardStepsFromGesuch);
			saveFinanzielleSituation(gesuch, wizardStepsFromGesuch);
			saveEinkommensverschlechterung(gesuch, wizardStepsFromGesuch);

			gesuchService.updateGesuch(gesuch, false, null); // just save all other objects before updating dokumente and verfuegungen
			saveDokumente(wizardStepsFromGesuch);
			saveVerfuegungen(gesuch, wizardStepsFromGesuch);
		}

		if (verfuegen) {
			Optional<GemeindeStammdaten> stammdaten =
				gemeindeService.getGemeindeStammdatenByGemeindeId(gesuch.extractGemeinde().getId());
			if (!stammdaten.isPresent()) {
				createStammdatenForGemeinde(gesuch.extractGemeinde());
			}
			FreigabeCopyUtil.copyForFreigabe(gesuch);
			gesuch.setFinSitStatus(FinSitStatus.AKZEPTIERT);

			gesuch.getKindContainers().stream().flatMap(kindContainer -> kindContainer.getBetreuungen().stream())
				.forEach(betreuung -> verfuegungService.verfuegen(
					gesuch.getId(),
					betreuung.getId(),
					null,
					ignorierenInZahlungslauf,
					ignorierenInZahlungslauf,
					false)
			);
			if (EbeguUtil.isFinanzielleSituationRequired(gesuch)) {
				generateDokFinSituation(gesuch); // the finSit document must be explicitly generated
			}
		}
	}

	private void createStammdatenForGemeinde(@Nonnull Gemeinde gemeinde) {
		GemeindeStammdaten stammdaten = new GemeindeStammdaten();
		stammdaten.setGemeinde(gemeinde);
		stammdaten.setMail("testgemeinde@mailbucket.dvbern.ch");
		stammdaten.setAdresse(new Adresse());
		stammdaten.getAdresse().setOrt("Bern");
		stammdaten.getAdresse().setPlz("3000");
		stammdaten.getAdresse().setStrasse("Nussbaumstrasse");
		stammdaten.setIban(new IBAN("CH93 0076 2011 6238 5295 7"));
		stammdaten.setBic("BIC123");
		stammdaten.setKontoinhaber("Inhaber");
		GemeindeStammdatenKorrespondenz gemeindeStammdatenKorrespondenz = new GemeindeStammdatenKorrespondenz();
		stammdaten.setGemeindeStammdatenKorrespondenz(gemeindeStammdatenKorrespondenz);
		gemeindeService.saveGemeindeStammdaten(stammdaten);
	}

	/**
	 * Creates the document for the finanzielle Situation.
	 */
	private void generateDokFinSituation(@Nonnull Gesuch gesuch) {
		try {
			genDokServiceBean.getFinSitDokumentAccessTokenGeneratedDokument(gesuch, true);
		} catch (MimeTypeParseException | MergeDocException e) {
			LOG.error("Dokument FinSit konnte nicht erstellt werden", e);
		}
	}

	private void saveVerfuegungen(@Nonnull Gesuch gesuch, @Nonnull List<WizardStep> wizardStepsFromGesuch) {
		if (!gesuch.getStatus().isAnyStatusOfVerfuegt()) {
			setWizardStepInStatus(wizardStepsFromGesuch, WizardStepName.VERFUEGEN, WizardStepStatus.WARTEN);
		} else {
			setWizardStepInStatus(wizardStepsFromGesuch, WizardStepName.VERFUEGEN, WizardStepStatus.OK);
		}
		setWizardStepVerfuegbar(wizardStepsFromGesuch, WizardStepName.VERFUEGEN);
	}

	private void saveDokumente(@Nonnull List<WizardStep> wizardStepsFromGesuch) {
		setWizardStepInStatus(wizardStepsFromGesuch, WizardStepName.DOKUMENTE, WizardStepStatus.IN_BEARBEITUNG);
		setWizardStepVerfuegbar(wizardStepsFromGesuch, WizardStepName.DOKUMENTE);
	}

	private void saveEinkommensverschlechterung(@Nonnull Gesuch gesuch,
		@Nonnull List<WizardStep> wizardStepsFromGesuch) {
		var ekvStepName = wizardStepService.getEKVWizardStepNameForGesuch(gesuch);
		if (gesuch.getEinkommensverschlechterungInfoContainer() != null) {
			setWizardStepInStatus(wizardStepsFromGesuch, ekvStepName, WizardStepStatus.IN_BEARBEITUNG);
			einkommensverschlechterungInfoService.createEinkommensverschlechterungInfo(gesuch.getEinkommensverschlechterungInfoContainer());
		}
		if (gesuch.getGesuchsteller1() != null && gesuch.getGesuchsteller1().getEinkommensverschlechterungContainer() != null) {
			einkommensverschlechterungService.saveEinkommensverschlechterungContainer(gesuch.getGesuchsteller1().getEinkommensverschlechterungContainer(), gesuch);
		}
		if (gesuch.getGesuchsteller2() != null && gesuch.getGesuchsteller2().getEinkommensverschlechterungContainer() != null) {
			einkommensverschlechterungService.saveEinkommensverschlechterungContainer(gesuch.getGesuchsteller2().getEinkommensverschlechterungContainer(), gesuch);
		}
		setWizardStepInStatus(wizardStepsFromGesuch, ekvStepName, WizardStepStatus.OK);
		setWizardStepVerfuegbar(wizardStepsFromGesuch, ekvStepName);
	}

	private void saveFinanzielleSituation(@Nonnull Gesuch gesuch, @Nonnull List<WizardStep> wizardStepsFromGesuch) {
		var finSitStepName = wizardStepService.getFinSitWizardStepNameForGesuch(gesuch);
		if (gesuch.getGesuchsteller1() != null && gesuch.getGesuchsteller1().getFinanzielleSituationContainer() != null) {
			setWizardStepInStatus(wizardStepsFromGesuch, finSitStepName, WizardStepStatus.IN_BEARBEITUNG);
			finanzielleSituationService.saveFinanzielleSituation(
				gesuch.getGesuchsteller1().getFinanzielleSituationContainer(),
				gesuch.getId());
		}
		if (gesuch.getGesuchsteller2() != null && gesuch.getGesuchsteller2().getFinanzielleSituationContainer() != null) {
			finanzielleSituationService.saveFinanzielleSituation(
				gesuch.getGesuchsteller2().getFinanzielleSituationContainer(),
				gesuch.getId());
		}
		setWizardStepInStatus(wizardStepsFromGesuch, finSitStepName, WizardStepStatus.OK);
		setWizardStepVerfuegbar(wizardStepsFromGesuch, finSitStepName);
	}

	private void saveErwerbspensen(@Nonnull Gesuch gesuch, @Nonnull List<WizardStep> wizardStepsFromGesuch) {
		if (gesuch.getGesuchsteller1() != null) {
			setWizardStepInStatus(wizardStepsFromGesuch, WizardStepName.ERWERBSPENSUM, WizardStepStatus.IN_BEARBEITUNG);
			gesuch.getGesuchsteller1().getErwerbspensenContainers()
				.forEach(erwerbspensumContainer -> erwerbspensumService.saveErwerbspensum(erwerbspensumContainer, gesuch));
		}
		if (gesuch.getGesuchsteller2() != null) {
			gesuch.getGesuchsteller2().getErwerbspensenContainers()
				.forEach(erwerbspensumContainer -> erwerbspensumService.saveErwerbspensum(erwerbspensumContainer, gesuch));
		}
		setWizardStepVerfuegbar(wizardStepsFromGesuch, WizardStepName.ERWERBSPENSUM);
	}

	private void saveBetreuungen(@Nonnull Gesuch gesuch, @Nonnull List<WizardStep> wizardStepsFromGesuch) {
		setWizardStepInStatus(wizardStepsFromGesuch, WizardStepName.BETREUUNG, WizardStepStatus.IN_BEARBEITUNG);
		final List<Betreuung> allBetreuungen = gesuch.extractAllBetreuungen();
		allBetreuungen.forEach(betreuung -> betreuungService.saveBetreuung(betreuung, false, null));
		setWizardStepVerfuegbar(wizardStepsFromGesuch, WizardStepName.BETREUUNG);
	}

	private void saveKinder(@Nonnull Gesuch gesuch, @Nonnull List<WizardStep> wizardStepsFromGesuch) {
		setWizardStepInStatus(wizardStepsFromGesuch, WizardStepName.KINDER, WizardStepStatus.IN_BEARBEITUNG);
		gesuch.getKindContainers().forEach(kindContainer -> kindService.saveKind(kindContainer));
		setWizardStepVerfuegbar(wizardStepsFromGesuch, WizardStepName.KINDER);
	}

	private void saveGesuchsteller(@Nonnull Gesuch gesuch, @Nonnull List<WizardStep> wizardStepsFromGesuch) {
		if (gesuch.getGesuchsteller1() != null) {
			setWizardStepInStatus(wizardStepsFromGesuch, WizardStepName.GESUCHSTELLER, WizardStepStatus.IN_BEARBEITUNG);
			gesuchstellerService.saveGesuchsteller(gesuch.getGesuchsteller1(), gesuch, 1, false);
		}
		if (gesuch.getGesuchsteller2() != null) {
			gesuchstellerService.saveGesuchsteller(gesuch.getGesuchsteller2(), gesuch, 2, false);
		}
		setWizardStepVerfuegbar(wizardStepsFromGesuch, WizardStepName.GESUCHSTELLER);
		// Umzug wird by default OK und verfuegbar, da es nicht notwendig ist, einen Umzug einzutragen
		setWizardStepVerfuegbar(wizardStepsFromGesuch, WizardStepName.UMZUG);
		setWizardStepInStatus(wizardStepsFromGesuch, WizardStepName.UMZUG, WizardStepStatus.OK);
	}

	private void saveFamiliensituation(@Nonnull Gesuch gesuch, @Nonnull List<WizardStep> wizardStepsFromGesuch) {
		if (gesuch.extractFamiliensituation() != null) {
			setWizardStepInStatus(wizardStepsFromGesuch, WizardStepName.FAMILIENSITUATION, WizardStepStatus.IN_BEARBEITUNG);
			requireNonNull(gesuch.getFamiliensituationContainer(), "FamiliensituationContainer muss gesetzt sein");
			familiensituationService.saveFamiliensituation(gesuch, gesuch.getFamiliensituationContainer(), null);
			setWizardStepVerfuegbar(wizardStepsFromGesuch, WizardStepName.FAMILIENSITUATION);
		}
	}

	private void setWizardStepInStatus(@Nonnull List<WizardStep> wizardSteps, @Nonnull WizardStepName stepName,
		@Nonnull WizardStepStatus status) {
		final WizardStep wizardStep = getWizardStepByName(wizardSteps, stepName);
		if (wizardStep != null) {
			wizardStep.setWizardStepStatus(status);
			wizardStepService.saveWizardStep(wizardStep);
		}
	}

	private void setWizardStepVerfuegbar(@Nonnull List<WizardStep> wizardSteps, @Nonnull WizardStepName stepName) {
		final WizardStep wizardStep = getWizardStepByName(wizardSteps, stepName);
		if (wizardStep != null) {
			wizardStep.setVerfuegbar(true);
			wizardStepService.saveWizardStep(wizardStep);
		}
	}

	@Nullable
	private WizardStep getWizardStepByName(@Nonnull List<WizardStep> wizardSteps, @Nonnull WizardStepName stepName) {
		for (WizardStep wizardStep : wizardSteps) {
			if (stepName == wizardStep.getWizardStepName()) {
				return wizardStep;
			}
		}
		return null;
	}

	@Nonnull
	private GesuchstellerContainer createGesuchstellerHeirat(@Nonnull GesuchstellerContainer gesuchsteller1) {
		GesuchstellerContainer gesuchsteller2 = new GesuchstellerContainer();
		Gesuchsteller gesuchsteller = new Gesuchsteller();
		gesuchsteller.setGeburtsdatum(LocalDate.of(1984, 12, 12));
		gesuchsteller.setVorname("Tim");
		gesuchsteller.setNachname(gesuchsteller1.extractNachname());
		gesuchsteller.setGeschlecht(Geschlecht.MAENNLICH);
		gesuchsteller.setMail("tim.tester@example.com");
		gesuchsteller.setMobile("076 309 30 58");
		gesuchsteller.setTelefon("031 378 24 24");

		gesuchsteller2.setGesuchstellerJA(gesuchsteller);
		gesuchsteller2.addAdresse(createGesuchstellerAdresseHeirat(gesuchsteller2));

		final ErwerbspensumContainer erwerbspensumContainer = createErwerbspensumContainer();
		erwerbspensumContainer.setGesuchsteller(gesuchsteller2);
		gesuchsteller2.getErwerbspensenContainers().add(erwerbspensumContainer);

		return gesuchsteller2;
	}

	@Nonnull
	private GesuchstellerAdresseContainer createGesuchstellerAdresseHeirat(@Nonnull GesuchstellerContainer gsCont) {
		GesuchstellerAdresseContainer gsAdresseContainer = new GesuchstellerAdresseContainer();

		GesuchstellerAdresse gesuchstellerAdresse = new GesuchstellerAdresse();
		gesuchstellerAdresse.setStrasse("Nussbaumstrasse");
		gesuchstellerAdresse.setHausnummer("21");
		gesuchstellerAdresse.setZusatzzeile("c/o Uwe Untermieter");
		gesuchstellerAdresse.setPlz("3014");
		gesuchstellerAdresse.setOrt("Bern");
		gesuchstellerAdresse.setGueltigkeit(new DateRange(Constants.START_OF_TIME, Constants.END_OF_TIME));
		gesuchstellerAdresse.setAdresseTyp(AdresseTyp.WOHNADRESSE);

		gsAdresseContainer.setGesuchstellerContainer(gsCont);
		gsAdresseContainer.setGesuchstellerAdresseJA(gesuchstellerAdresse);

		return gsAdresseContainer;
	}

	@Nonnull
	private ErwerbspensumContainer createErwerbspensumContainer() {
		ErwerbspensumContainer epCont = new ErwerbspensumContainer();
		epCont.setErwerbspensumGS(createErwerbspensumData());
		Erwerbspensum epKorrigiertJA = createErwerbspensumData();
		epKorrigiertJA.setTaetigkeit(Taetigkeit.ANGESTELLT);
		epCont.setErwerbspensumJA(epKorrigiertJA);
		return epCont;
	}

	@Nonnull
	private Erwerbspensum createErwerbspensumData() {
		Erwerbspensum ep = new Erwerbspensum();
		ep.setTaetigkeit(Taetigkeit.ANGESTELLT);
		ep.setPensum(90);
		ep.setGueltigkeit(new DateRange(Constants.START_OF_TIME, Constants.END_OF_TIME));
		return ep;
	}

	@Nonnull
	private Familiensituation getFamiliensituationZuZweit(@Nullable LocalDate aenderungPer) {
		Familiensituation famsit = new Familiensituation();
		famsit.setFamilienstatus(EnumFamilienstatus.VERHEIRATET);
		famsit.setGemeinsameSteuererklaerung(true);
		famsit.setAenderungPer(aenderungPer);
		famsit.setSozialhilfeBezueger(false);
		famsit.setVerguenstigungGewuenscht(true);
		return famsit;
	}

	@Nonnull
	private Familiensituation getFamiliensituationAlleine(@Nullable LocalDate aenderungPer) {
		Familiensituation famsit = new Familiensituation();
		famsit.setFamilienstatus(EnumFamilienstatus.ALLEINERZIEHEND);
		famsit.setAenderungPer(aenderungPer);
		famsit.setSozialhilfeBezueger(false);
		famsit.setVerguenstigungGewuenscht(true);
		return famsit;
	}

	@Override
	public void testAllMails(@Nonnull String mailadresse, Mandant mandant) {
		// in order to send test mails we must run in dev mode
		if(!configuration.getIsDevmode()) {
			throw new EbeguRuntimeException("testAllMails", "Testmails dürfen nur in Dev Mode versendet werden");
		}
		Gesuchsperiode gesuchsperiode = gesuchsperiodeService.findNewestGesuchsperiode(mandant).orElseThrow(() -> new IllegalArgumentException());
		Gemeinde gemeinde = gemeindeService.getAktiveGemeinden(mandant).stream().findFirst().orElseThrow(() -> new IllegalArgumentException());
		GemeindeStammdaten gemeindeStammdaten = gemeindeService.getGemeindeStammdatenByGemeindeId(gemeinde.getId()).orElseThrow(() -> new IllegalArgumentException());
		Benutzer besitzer = benutzerService.getCurrentBenutzer().orElseThrow(() -> new IllegalStateException());
		InstitutionStammdatenBuilder institutionStammdatenBuilder = testfallDependenciesVisitor.process(mandant);
		final Gesuch gesuch = createAndSaveGesuch(new Testfall01_WaeltiDagmar(gesuchsperiode, true, gemeinde, institutionStammdatenBuilder),
			true, null);
		requireNonNull(gesuch.getGesuchsteller1());
		Betreuung firstBetreuung = gesuch.getFirstBetreuung();
		requireNonNull(firstBetreuung);

		String oldAdresseUser = besitzer.getEmail();
		String oldAdresseInstitution = firstBetreuung.getInstitutionStammdaten().getMail();
		IBAN oldIBAN = gemeindeStammdaten.getIban();
		KorrespondenzSpracheTyp oldKorrespondenzSpracheTyp = gemeindeStammdaten.getKorrespondenzsprache();

		besitzer.setEmail(mailadresse);
		firstBetreuung.getInstitutionStammdaten().setMail(mailadresse);

		gesuch.getGesuchsteller1().getGesuchstellerJA().setMail(mailadresse);
		gesuch.getFall().setBesitzer(besitzer);
		gesuch.setEingangsart(Eingangsart.ONLINE);

		Mitteilung mitteilung = new Mitteilung();
		mitteilung.setEmpfaenger(besitzer);
		mitteilung.setBetreuung(firstBetreuung);
		mitteilung.setDossier(gesuch.getDossier());

		Einladung einladung = Einladung.forMitarbeiter(besitzer);
		firstBetreuung.setBetreuungsstatus(Betreuungsstatus.BESTAETIGT);

		gemeindeStammdaten.setIban(new IBAN("CH82 0900 0000 1001 5000 6"));
		gemeindeStammdaten.setKorrespondenzsprache(KorrespondenzSpracheTyp.DE_FR);

		try {
			// Sprachabhängige Mails in beiden Sprachen schicken
			testAllMailsInSprache(Sprache.DEUTSCH, gesuch, firstBetreuung, gesuchsperiode, mailadresse);
			testAllMailsInSprache(Sprache.FRANZOESISCH, gesuch, firstBetreuung, gesuchsperiode, mailadresse);
			// Sprachunabhängige Mails
			mailService.sendInfoOffenePendenzenNeuMitteilungInstitution(firstBetreuung.getInstitutionStammdaten(), true,false);
			mailService.sendBenutzerEinladung(besitzer, einladung);
			mailService.sendInfoMitteilungErhalten(mitteilung);
			LOG.info("Es sollten 31 Mails verschickt worden sein an {}", mailadresse);
		} catch (MailException e) {
			LOG.error("Could not send Mails", e);
		} finally {
			besitzer.setEmail(oldAdresseUser);
			gesuch.getFall().setBesitzer(null);
			firstBetreuung.getInstitutionStammdaten().setMail(oldAdresseInstitution);
			gemeindeStammdaten.setIban(oldIBAN);
			gemeindeStammdaten.setKorrespondenzsprache(oldKorrespondenzSpracheTyp);
		}
	}

	private void testAllMailsInSprache(@Nonnull Sprache sprache, @Nonnull Gesuch gesuch, @Nonnull Betreuung firstBetreuung,
		@Nonnull Gesuchsperiode gesuchsperiode, @Nonnull String mailadresse) throws MailException {

		requireNonNull(gesuch.getGesuchsteller1());
		gesuch.getGesuchsteller1().getGesuchstellerJA().setKorrespondenzSprache(sprache);
		Locale locale = sprache == Sprache.FRANZOESISCH ? Locale.FRENCH : Locale.GERMAN;

		mailService.sendInfoBetreuungenBestaetigt(gesuch);
		mailService.sendInfoBetreuungAbgelehnt(firstBetreuung);
		mailService.sendInfoVerfuegtGesuch(gesuch);
		mailService.sendInfoVerfuegtMutation(gesuch);
		mailService.sendInfoMahnung(gesuch);
		mailService.sendWarnungGesuchNichtFreigegeben(gesuch, 10);
		mailService.sendWarnungFreigabequittungFehlt(gesuch, 10);
		mailService.sendInfoGesuchGeloescht(gesuch);
		mailService.sendInfoFreischaltungGesuchsperiode(gesuchsperiode, gesuch);
		mailService.sendInfoBetreuungGeloescht(gesuch.extractAllBetreuungen());
		mailService.sendInfoBetreuungVerfuegt(firstBetreuung);
		mailService.sendInfoStatistikGeneriert(mailadresse, "www.kibon.ch", locale,
				requireNonNull(gesuch.getFall().getMandant()));

		AnmeldungTagesschule anmeldung = new AnmeldungTagesschule();
		anmeldung.setId(firstBetreuung.getId());
		anmeldung.setInstitutionStammdaten(firstBetreuung.getInstitutionStammdaten());
		anmeldung.setKind(firstBetreuung.getKind());

		mailService.sendInfoSchulamtAnmeldungTagesschuleAkzeptiert(anmeldung);
		mailService.sendInfoSchulamtAnmeldungTagesschuleUebernommen(anmeldung);
		mailService.sendInfoSchulamtAnmeldungAbgelehnt(anmeldung);
	}

	@Nonnull
	@Override
	public Gesuch antragErneuern(@Nonnull Gesuch gesuch, @Nonnull Gesuchsperiode gesuchsperiode, @Nullable LocalDate eingangsdatum) {
		Gesuch erneuerung = Gesuch.createErneuerung(gesuch.getDossier(), gesuchsperiode, eingangsdatum);
		return gesuchService.createGesuch(erneuerung);
	}

	@Nonnull
	@Override
	public Gesuch antragMutieren(@Nonnull Gesuch antrag, @Nullable LocalDate eingangsdatum) {
		Gesuch mutation = Gesuch.createMutation(antrag.getDossier(), antrag.getGesuchsperiode(), eingangsdatum);
		return gesuchService.createGesuch(mutation);
	}

	@Nonnull
	@Override
	public Collection<LastenausgleichTagesschuleAngabenGemeindeContainer> createAndSaveLATSTestdaten(
		@Nonnull String gesuchsperiodeId,
		@Nullable String gemeindeId,
		@Nonnull LastenausgleichTagesschuleAngabenGemeindeStatus status) {
		Gesuchsperiode gesuchsperiode = gesuchsperiodeService.findGesuchsperiode(gesuchsperiodeId)
			.orElseThrow(() -> new IllegalArgumentException());
		if (gemeindeId != null) {
			Gemeinde gemeinde =
				gemeindeService.findGemeinde(gemeindeId).orElseThrow(() -> new IllegalArgumentException());
			Einstellung normlohnkosten = einstellungService.findEinstellung(EinstellungKey.LATS_LOHNNORMKOSTEN, gemeinde, gesuchsperiode);
			Einstellung normlohnkostenLessThen50 = einstellungService.findEinstellung(EinstellungKey.LATS_LOHNNORMKOSTEN_LESS_THAN_50, gemeinde, gesuchsperiode);
			return Collections.singleton(createAndSaveLATSTestdatenForGemeinde(status, gesuchsperiode, gemeinde, normlohnkosten, normlohnkostenLessThen50));
		}

		Collection<Gemeinde> allGemeinden = gemeindeService.getAktiveGemeinden();
		// we need normlohnkosten only for first gemeinde, since this value is always identical for one gesuchsperiode
		Gemeinde firstGemeinde = allGemeinden.iterator().next();
		Einstellung normlohnkosten = einstellungService.findEinstellung(EinstellungKey.LATS_LOHNNORMKOSTEN, firstGemeinde, gesuchsperiode);
		Einstellung normlohnkostenLessThen50 = einstellungService.findEinstellung(EinstellungKey.LATS_LOHNNORMKOSTEN_LESS_THAN_50, firstGemeinde, gesuchsperiode);

		return allGemeinden
			.stream()
			.map(gemeinde -> createAndSaveLATSTestdatenForGemeinde(status, gesuchsperiode, gemeinde, normlohnkosten, normlohnkostenLessThen50))
			.collect(Collectors.toSet());
	}

	@Nonnull
	@Override
	public FerienbetreuungAngabenContainer createAndSaveFerienbetreuungTestdaten(
		@Nonnull String gesuchsperiodeId,
		@Nonnull String gemeindeId,
		@Nonnull FerienbetreuungAngabenStatus status) {
		Gesuchsperiode gesuchsperiode = gesuchsperiodeService.findGesuchsperiode(gesuchsperiodeId)
			.orElseThrow(() -> new IllegalArgumentException());
			Gemeinde gemeinde =
				gemeindeService.findGemeinde(gemeindeId).orElseThrow(() -> new IllegalArgumentException());
		FerienbetreuungAngabenContainer testantrag = (new Testantrag_FB(gesuchsperiode, gemeinde, status)).getContainer();
		ferienbetreuungService.deleteFerienbetreuungAntragIfExists(gemeinde, gesuchsperiode);
		return ferienbetreuungService.saveFerienbetreuungAngabenContainer(testantrag);
	}

	@Nonnull
	private LastenausgleichTagesschuleAngabenGemeindeContainer createAndSaveLATSTestdatenForGemeinde(
		@Nonnull LastenausgleichTagesschuleAngabenGemeindeStatus status,
		@Nonnull Gesuchsperiode gesuchsperiode,
		@Nonnull Gemeinde gemeinde,
		@Nonnull Einstellung normlohnkosten,
		@Nonnull Einstellung normlohnkostenLessThen50
	) {
		final Collection<InstitutionStammdaten> allTagesschulenForGesuchsperiodeAndGemeinde =
			institutionStammdatenService.getAllTagesschulenForGesuchsperiodeAndGemeinde(gesuchsperiode, gemeinde);
		LastenausgleichTagesschuleAngabenGemeindeContainer testantrag = (new Testantrag_LATS(
			gemeinde,
			gesuchsperiode,
			allTagesschulenForGesuchsperiodeAndGemeinde,
			status,
			normlohnkosten,
			normlohnkostenLessThen50
		).getContainer());
		latsService.deleteAntragIfExists(gemeinde, gesuchsperiode);
		LastenausgleichTagesschuleAngabenGemeindeContainer savedAntrag = latsService.saveLastenausgleichTagesschuleGemeinde(testantrag);
		historyService.saveLastenausgleichTagesschuleStatusChange(savedAntrag);
		return savedAntrag;
	}


	private void createAndSaveSozialdienstFallDokument(@Nonnull Fall fall) {
		requireNonNull(fall.getSozialdienstFall());
		try{
			UploadFileInfo fileInfo = new UploadFileInfo("vollmacht.pdf", new MimeType("text/pdf"));
			fileInfo.setFilename("vollmacht.pdf");

			fileInfo.setBytes(fallService.generateVollmachtDokument(fall.getId(), Sprache.DEUTSCH));

			fileSaverService.save(fileInfo, fall.getSozialdienstFall().getId());

			SozialdienstFallDokument sozialdienstFallDokument = new SozialdienstFallDokument();
			sozialdienstFallDokument.setSozialdienstFall(fall.getSozialdienstFall());
			sozialdienstFallDokument.setFilepfad(fileInfo.getPath());
			sozialdienstFallDokument.setFilename(fileInfo.getFilename());
			sozialdienstFallDokument.setFilesize(fileInfo.getSizeString());

			sozialdienstFallDokumentService.saveVollmachtDokument(sozialdienstFallDokument);
		}
		catch (MimeTypeParseException | MergeDocException e) {
			LOG.error("Could not save Sozialdienst Vollmacht Dokument");
		}
	}

}
