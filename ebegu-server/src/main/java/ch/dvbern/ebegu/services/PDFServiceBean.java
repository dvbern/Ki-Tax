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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.ejb.Local;
import javax.ejb.Stateless;
import javax.inject.Inject;

import ch.dvbern.ebegu.config.EbeguConfiguration;
import ch.dvbern.ebegu.entities.AnmeldungTagesschule;
import ch.dvbern.ebegu.entities.Betreuung;
import ch.dvbern.ebegu.entities.DokumentGrund;
import ch.dvbern.ebegu.entities.Einstellung;
import ch.dvbern.ebegu.entities.GemeindeStammdaten;
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.entities.Mahnung;
import ch.dvbern.ebegu.entities.Mandant;
import ch.dvbern.ebegu.entities.RueckforderungFormular;
import ch.dvbern.ebegu.entities.Verfuegung;
import ch.dvbern.ebegu.entities.gemeindeantrag.FerienbetreuungAngabenContainer;
import ch.dvbern.ebegu.entities.gemeindeantrag.LastenausgleichTagesschuleAngabenGemeindeContainer;
import ch.dvbern.ebegu.entities.sozialdienst.SozialdienstFall;
import ch.dvbern.ebegu.enums.EinstellungKey;
import ch.dvbern.ebegu.enums.ErrorCodeEnum;
import ch.dvbern.ebegu.enums.RueckforderungInstitutionTyp;
import ch.dvbern.ebegu.enums.Sprache;
import ch.dvbern.ebegu.errors.EbeguEntityNotFoundException;
import ch.dvbern.ebegu.errors.MergeDocException;
import ch.dvbern.ebegu.finanzielleSituationRechner.FinanzielleSituationRechnerFactory;
import ch.dvbern.ebegu.i18n.LocaleThreadLocal;
import ch.dvbern.ebegu.pdfgenerator.AbstractVerfuegungPdfGenerator;
import ch.dvbern.ebegu.pdfgenerator.AbstractVerfuegungPdfGenerator.Art;
import ch.dvbern.ebegu.pdfgenerator.AnmeldebestaetigungTSPDFGenerator;
import ch.dvbern.ebegu.pdfgenerator.BegleitschreibenPdfGenerator;
import ch.dvbern.ebegu.pdfgenerator.DokumentAnFamilieGenerator;
import ch.dvbern.ebegu.pdfgenerator.ErsteMahnungPdfGenerator;
import ch.dvbern.ebegu.pdfgenerator.FerienbetreuungReportPdfGenerator;
import ch.dvbern.ebegu.pdfgenerator.FreigabequittungPdfQuittungVisitor;
import ch.dvbern.ebegu.pdfgenerator.KibonPdfGenerator;
import ch.dvbern.ebegu.pdfgenerator.LATSReportPdfGenerator;
import ch.dvbern.ebegu.pdfgenerator.MahnungPdfGenerator;
import ch.dvbern.ebegu.pdfgenerator.MandantPdfGenerator;
import ch.dvbern.ebegu.pdfgenerator.MusterPdfGenerator;
import ch.dvbern.ebegu.pdfgenerator.PdfUtil;
import ch.dvbern.ebegu.pdfgenerator.RueckforderungPrivatDefinitivVerfuegungPdfGenerator;
import ch.dvbern.ebegu.pdfgenerator.RueckforderungPrivateVerfuegungPdfGenerator;
import ch.dvbern.ebegu.pdfgenerator.RueckforderungProvVerfuegungPdfGenerator;
import ch.dvbern.ebegu.pdfgenerator.RueckforderungPublicVerfuegungPdfGenerator;
import ch.dvbern.ebegu.pdfgenerator.VerfuegungPdfGeneratorVisitor;
import ch.dvbern.ebegu.pdfgenerator.VollmachtPdfGenerator;
import ch.dvbern.ebegu.pdfgenerator.ZweiteMahnungPdfGenerator;
import ch.dvbern.ebegu.pdfgenerator.finanzielleSituation.FinanzielleSituationPdfGeneratorFactory;
import ch.dvbern.ebegu.rules.anlageverzeichnis.DokumentenverzeichnisEvaluator;
import ch.dvbern.ebegu.util.DokumenteUtil;
import ch.dvbern.ebegu.util.EbeguUtil;
import ch.dvbern.lib.invoicegenerator.errors.InvoiceGeneratorException;

@Stateless
@Local(PDFService.class)
public class PDFServiceBean implements PDFService {

	private static final Objects[] OBJECTARRAY = {};
	public static final byte[] BYTES = new byte[0];

	@Inject
	private DokumentGrundService dokumentGrundService;

	@Inject
	private DokumentenverzeichnisEvaluator dokumentenverzeichnisEvaluator;

	@Inject
	private GemeindeService gemeindeService;

	@Inject
	private DossierService dossierService;

	@Inject
	private EinstellungService einstellungService;

	@Inject
	private ApplicationPropertyService applicationPropertyService;

	@Inject
	private EbeguConfiguration ebeguConfiguration;

	@Inject
	private Authorizer authorizer;

	@Nonnull
	@Override
	public byte[] generateNichteintreten(
		Betreuung betreuung,
		boolean writeProtected,
		@Nonnull Locale locale
	) throws MergeDocException {

		Objects.requireNonNull(betreuung, "Das Argument 'betreuung' darf nicht leer sein");
		GemeindeStammdaten stammdaten = getGemeindeStammdaten(betreuung.extractGesuch());

		Mandant mandant = stammdaten.getGemeinde().getMandant();
		assert mandant != null;

		boolean isFKJVTexte = getEinstellungFKJVTexte(betreuung);

		// Bei Nicht-Eintreten soll der FEBR-Erklaerungstext gar nicht erscheinen, es ist daher egal,
		// was wir mitgeben
		VerfuegungPdfGeneratorVisitor verfuegungPdfGeneratorVisitor = new VerfuegungPdfGeneratorVisitor(
			betreuung,
			stammdaten,
			Art.NICHT_EINTRETTEN,
			false, false, isFKJVTexte);
		AbstractVerfuegungPdfGenerator pdfGenerator =
			verfuegungPdfGeneratorVisitor.getVerfuegungPdfGeneratorForMandant(mandant);
		return generateDokument(pdfGenerator, !writeProtected, locale, mandant);
	}

	private boolean getEinstellungFKJVTexte(@Nonnull Betreuung betreuung) {
		return einstellungService.findEinstellung(
			EinstellungKey.FKJV_TEXTE,
			betreuung.extractGesuch().extractGemeinde(),
			betreuung.extractGesuchsperiode()
		).getValueAsBoolean();
	}

	@Nonnull
	@Override
	public byte[] generateMahnung(
		Mahnung mahnung,
		Optional<Mahnung> vorgaengerMahnungOptional,
		boolean writeProtected,
		@Nonnull Locale locale
	) throws MergeDocException {

		Objects.requireNonNull(mahnung, "Das Argument 'mahnung' darf nicht leer sein");
		GemeindeStammdaten stammdaten = getGemeindeStammdaten(mahnung.getGesuch());

		MahnungPdfGenerator pdfGenerator;
		switch (mahnung.getMahnungTyp()) {
		case ERSTE_MAHNUNG:
			pdfGenerator = new ErsteMahnungPdfGenerator(mahnung, stammdaten);
			break;
		case ZWEITE_MAHNUNG:
			Mahnung vorgaengerMahnung = vorgaengerMahnungOptional.orElseThrow(() -> new EbeguEntityNotFoundException("generateMahnung",
				ErrorCodeEnum.ERROR_ENTITY_NOT_FOUND, mahnung.getId()));
			pdfGenerator = new ZweiteMahnungPdfGenerator(mahnung, vorgaengerMahnung, stammdaten);
			break;
		default:
			throw new MergeDocException("generateMahnung()", "Unexpected Mahnung Type", null, OBJECTARRAY);
		}
		return generateDokument(pdfGenerator, !writeProtected, locale, stammdaten.getGemeinde().getMandant());
	}

	@Override
	@Nonnull
	public byte[] generateFreigabequittung(
		@Nonnull Gesuch gesuch,
		boolean writeProtected,
		@Nonnull Locale locale
	) throws MergeDocException {

		Objects.requireNonNull(gesuch, "Das Argument 'gesuch' darf nicht leer sein");

		GemeindeStammdaten stammdaten = getGemeindeStammdaten(gesuch);
		final List<DokumentGrund> benoetigteUnterlagen = calculateListOfDokumentGrunds(gesuch, locale);

		Mandant mandant = stammdaten.getGemeinde().getMandant();

		FreigabequittungPdfQuittungVisitor pdfGeneratorVisitor = new FreigabequittungPdfQuittungVisitor(gesuch, stammdaten,
			benoetigteUnterlagen);
		return generateDokument(pdfGeneratorVisitor.getFreigabequittungPdfGeneratorForMandant(mandant), !writeProtected, locale, mandant);
	}

	@Override
	@Nonnull
	public byte[] generateBegleitschreiben(
		@Nonnull Gesuch gesuch,
		boolean writeProtected,
		@Nonnull Locale locale
	) throws MergeDocException {

		Objects.requireNonNull(gesuch, "Das Argument 'gesuch' darf nicht leer sein");
		authorizer.checkReadAuthorization(gesuch);

		GemeindeStammdaten stammdaten = getGemeindeStammdaten(gesuch);

		BegleitschreibenPdfGenerator pdfGenerator = new BegleitschreibenPdfGenerator(gesuch, stammdaten);
		return generateDokument(pdfGenerator, !writeProtected, locale, stammdaten.getGemeinde().getMandant());
	}

	@Nonnull
	@Override
	public byte[] generateFinanzielleSituation(
		@Nonnull Gesuch gesuch,
		@Nonnull Verfuegung famGroessenVerfuegung,
		boolean writeProtected,
		@Nonnull Locale locale
	) throws MergeDocException {

		Objects.requireNonNull(gesuch, "Das Argument 'gesuch' darf nicht leer sein");

		if (EbeguUtil.isFinanzielleSituationRequired(gesuch)) {

			if (!gesuch.hasOnlyBetreuungenOfSchulamt()) {
				// Bei nur Schulamt prüfen wir die Berechtigung nicht, damit das JA solche Gesuche schliessen kann. Der UseCase ist,
				// dass zuerst ein zweites Angebot vorhanden war, dieses aber durch das JA gelöscht wurde.
				authorizer.checkReadAuthorizationFinSit(gesuch);
			}

			// Im Dokument der Finanziellen Situation werden nur die Zeitabschnitte dargestellt, die nach dem
			// ersten Einreichungsdatum aller Gesuche dieses Dossiers liegen
			LocalDate erstesEinreichungsdatum =
				dossierService.getErstesEinreichungsdatum(gesuch.getDossier(), gesuch.getGesuchsperiode());

			GemeindeStammdaten stammdaten = getGemeindeStammdaten(gesuch);

			DokumentAnFamilieGenerator pdfGenerator = FinanzielleSituationPdfGeneratorFactory.getGenerator(
				gesuch,
				famGroessenVerfuegung,
				stammdaten,
				erstesEinreichungsdatum,
				FinanzielleSituationRechnerFactory.getRechner(gesuch)
			);
			return generateDokument(pdfGenerator, !writeProtected, locale, stammdaten.getGemeinde().getMandant());
		}
		return BYTES;
	}

	@Nonnull
	@Override
	public byte[] generateVerfuegungForBetreuung(
		@Nonnull Betreuung betreuung,
		@Nullable LocalDate letzteVerfuegungDatum,
		boolean writeProtected,
		@Nonnull Locale locale
	) throws MergeDocException {

		Objects.requireNonNull(betreuung, "Das Argument 'betreuung' darf nicht leer sein");
		GemeindeStammdaten stammdaten = getGemeindeStammdaten(betreuung.extractGesuch());

		// Falls die Gemeinde Kontingentierung eingeschaltet hat *und* es sich um einen Entwurf handelt
		// wird auf der Verfügung ein Vermerk zur Kontingentierung gedruckt
		boolean showInfoKontingentierung = false;
		if (!writeProtected) {
			Einstellung einstellungKontingentierung = einstellungService.findEinstellung(
				EinstellungKey.GEMEINDE_KONTINGENTIERUNG_ENABLED,
				betreuung.extractGesuch().extractGemeinde(),
				betreuung.extractGesuchsperiode());
			showInfoKontingentierung = einstellungKontingentierung.getValueAsBoolean();
		}

		boolean stadtBernAsivConfigured = applicationPropertyService.isStadtBernAsivConfigured(betreuung.extractGesuch().extractGemeinde().getMandant());
		boolean isFKJVTexte = getEinstellungFKJVTexte(betreuung);

		Art art = evaluateArt(betreuung);

		Mandant mandant = stammdaten.getGemeinde().getMandant();
		assert mandant != null;

		VerfuegungPdfGeneratorVisitor verfuegungPdfGeneratorVisitor = new VerfuegungPdfGeneratorVisitor(
			betreuung,
			stammdaten,
			art,
			showInfoKontingentierung,
			stadtBernAsivConfigured,
			isFKJVTexte);
		AbstractVerfuegungPdfGenerator pdfGenerator =
			verfuegungPdfGeneratorVisitor.getVerfuegungPdfGeneratorForMandant(mandant);

		return generateDokument(pdfGenerator, !writeProtected, locale, mandant);
	}

	private Art evaluateArt(Betreuung betreuung) {
		if (betreuung.hasAnspruch()) {
			return Art.NORMAL;
		}

		return betreuung.isAngebotTagesfamilien() ? Art.KEIN_ANSCHRUCH_TFO : Art.KEIN_ANSPRUCH;
	}

	@Nonnull
	@Override
	public byte[] generateAnmeldebestaetigungFuerTagesschule(
		@Nonnull AnmeldungTagesschule anmeldungTagesschule,
		@Nonnull boolean mitTarif,
		boolean writeProtected,
		@Nonnull Locale locale
	) throws MergeDocException {

		Objects.requireNonNull(anmeldungTagesschule, "Das Argument 'anmeldungTagesschule' darf nicht leer sein");
		Gesuch gesuch = anmeldungTagesschule.extractGesuch();
		GemeindeStammdaten stammdaten = getGemeindeStammdaten(gesuch);

		AnmeldebestaetigungTSPDFGenerator.Art art = mitTarif ? AnmeldebestaetigungTSPDFGenerator.Art.MIT_TARIF :
			AnmeldebestaetigungTSPDFGenerator.Art.OHNE_TARIF;

		Einstellung mahlzeitenverguenstigungEnabled = einstellungService.findEinstellung(
			EinstellungKey.GEMEINDE_MAHLZEITENVERGUENSTIGUNG_ENABLED,
			anmeldungTagesschule.extractGesuch().extractGemeinde(),
			anmeldungTagesschule.extractGesuchsperiode());

		AnmeldebestaetigungTSPDFGenerator pdfGenerator = new AnmeldebestaetigungTSPDFGenerator(gesuch,
			stammdaten, art , anmeldungTagesschule, mahlzeitenverguenstigungEnabled.getValueAsBoolean());
		return generateDokument(pdfGenerator, !writeProtected, locale, stammdaten.getGemeinde().getMandant());
	}

	@Nonnull
	@Override
	public byte[] generateProvisorischeVerfuegungRuckforderungformular(
		@Nonnull RueckforderungFormular rueckforderungFormular, boolean writeProtected
	) throws MergeDocException {

		Objects.requireNonNull(rueckforderungFormular, "Das Argument 'rueckforderungFormular' darf nicht leer sein");

		String nameVerantwortlichePerson = ebeguConfiguration.getNotverordnungUnterschriftName();
		String unterschriftPath = ebeguConfiguration.getNotverordnungUnterschriftPath();
		RueckforderungProvVerfuegungPdfGenerator pdfGenerator =
			new RueckforderungProvVerfuegungPdfGenerator(rueckforderungFormular, nameVerantwortlichePerson, unterschriftPath);
		return generateDokument(pdfGenerator, !writeProtected, rueckforderungFormular.getKorrespondenzSprache().getLocale(),
				Objects.requireNonNull(rueckforderungFormular.getInstitutionStammdaten().getInstitution()
						.getMandant()));
	}

	@Nonnull
	@Override
	public byte[] generateDefinitiveVerfuegungRuckforderungformular(
		@Nonnull RueckforderungFormular rueckforderungFormular, boolean writeProtected
	) throws MergeDocException {

		Objects.requireNonNull(rueckforderungFormular, "Das Argument 'rueckforderungFormular' darf nicht leer sein");

		String nameVerantwortlichePerson = ebeguConfiguration.getNotverordnungUnterschriftName();
		MandantPdfGenerator pdfGenerator = null;
		if (rueckforderungFormular.getInstitutionTyp() == RueckforderungInstitutionTyp.PRIVAT) {
			// is institution private
			if (rueckforderungFormular.isHasBeenProvisorisch()) {
				pdfGenerator = new RueckforderungPrivatDefinitivVerfuegungPdfGenerator(
					rueckforderungFormular, nameVerantwortlichePerson);
			} else {
				pdfGenerator = new RueckforderungPrivateVerfuegungPdfGenerator(
					rueckforderungFormular, nameVerantwortlichePerson);
			}
		} else {
			// is instition public
			pdfGenerator = new RueckforderungPublicVerfuegungPdfGenerator(
				rueckforderungFormular, nameVerantwortlichePerson);
		}
		return generateDokument(pdfGenerator, !writeProtected, rueckforderungFormular.getKorrespondenzSprache().getLocale(),
				Objects.requireNonNull(rueckforderungFormular.getInstitutionStammdaten().getInstitution()
						.getMandant()));
	}

	@Override
	@Nonnull
	public byte[] generateMusterdokument(
		@Nonnull GemeindeStammdaten gemeindeStammdaten
	) throws MergeDocException {
		Objects.requireNonNull(gemeindeStammdaten, "Das Argument 'gemeindeStammdaten' darf nicht leer sein");
		authorizer.checkReadAuthorization(gemeindeStammdaten.getGemeinde());

		MusterPdfGenerator pdfGenerator = new MusterPdfGenerator(gemeindeStammdaten);
		return generateDokument(pdfGenerator, false, LocaleThreadLocal.get(), gemeindeStammdaten.getGemeinde().getMandant());
	}

	/**
	 * In dieser Methode werden alle DokumentGrunds vom Gesuch einer Liste hinzugefuegt. Die die bereits existieren und die
	 * die noch nicht hochgeladen wurden
	 */
	@Nonnull
	private List<DokumentGrund> calculateListOfDokumentGrunds(@Nonnull Gesuch gesuch, @Nonnull Locale locale) {
		List<DokumentGrund> dokumentGrundsMerged = new ArrayList<>(DokumenteUtil
			.mergeNeededAndPersisted(
				dokumentenverzeichnisEvaluator.calculate(gesuch, locale),
				dokumentGrundService.findAllDokumentGrundByGesuch(gesuch)));
		Collections.sort(dokumentGrundsMerged);
		return dokumentGrundsMerged;
	}

	@Nonnull
	private GemeindeStammdaten getGemeindeStammdaten(@Nonnull Gesuch gesuch) {
		String gemeindeId = gesuch.extractGemeinde().getId();
		GemeindeStammdaten stammdaten = gemeindeService.getGemeindeStammdatenByGemeindeId(gemeindeId)
			.orElseThrow(() -> new EbeguEntityNotFoundException(
				"getGemeindeStammdaten", ErrorCodeEnum.ERROR_ENTITY_NOT_FOUND, gemeindeId));
		return stammdaten;
	}

	@Nonnull
	private byte[] generateDokument(
			@Nonnull KibonPdfGenerator pdfGenerator,
			boolean entwurf,
			@Nonnull Locale locale,
			Mandant mandant) throws MergeDocException {

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try {
			pdfGenerator.generate(baos);
			byte[] content = baos.toByteArray();
			if (entwurf) {
				return PdfUtil.addEntwurfWatermark(content, locale, mandant);
			}
			return content;
		} catch (InvoiceGeneratorException | IOException e) {
			throw new MergeDocException("generateDokument()",
				"Bei der Generierung des Dokuments ist ein Fehler aufgetreten", e, OBJECTARRAY);
		}
	}

	@Nonnull
	private byte[] generateDokument(
		@Nonnull MandantPdfGenerator pdfGenerator,
		boolean entwurf,
		@Nonnull Locale locale,
		@Nonnull Mandant mandant
	) throws MergeDocException {

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try {
			pdfGenerator.generate(baos);
			byte[] content = baos.toByteArray();
			if (entwurf) {
				return PdfUtil.addEntwurfWatermark(content, locale, mandant);
			}
			return content;
		} catch (InvoiceGeneratorException | IOException e) {
			throw new MergeDocException("generateDokument()",
				"Bei der Generierung des Dokuments ist ein Fehler aufgetreten", e, OBJECTARRAY);
		}
	}

	@Nonnull
	@Override
	public byte[] generateVollmachtSozialdienst(
		@Nonnull SozialdienstFall sozialdienstFall,
		@Nonnull Sprache sprache
	) throws MergeDocException {

		Objects.requireNonNull(sozialdienstFall, "Das Argument 'sozialdienstFall' darf nicht leer sein");

		VollmachtPdfGenerator pdfGenerator = new VollmachtPdfGenerator(sprache, sozialdienstFall);

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try {
			pdfGenerator.generate(baos);
			byte[] content = baos.toByteArray();
			return content;
		} catch (InvoiceGeneratorException e) {
			throw new MergeDocException("generateDokument()",
				"Bei der Generierung des Dokuments ist ein Fehler aufgetreten", e, OBJECTARRAY);
		}
	}


	@Override
	@Nonnull
	public byte[] generateFerienbetreuungReport(
			@Nonnull FerienbetreuungAngabenContainer ferienbetreuung,
			@Nonnull GemeindeStammdaten gemeindeStammdaten,
			@Nonnull Sprache sprache
	) throws MergeDocException {

		Objects.requireNonNull(ferienbetreuung, "Das Argument 'ferienbetreuung' darf nicht leer sein");

		FerienbetreuungReportPdfGenerator pdfGenerator = new FerienbetreuungReportPdfGenerator(ferienbetreuung, sprache);
		return generateDokument(pdfGenerator, false, sprache.getLocale(), gemeindeStammdaten.getGemeinde().getMandant());
	}

	@Nonnull
	@Override
	public byte[] generateLATSReport(
		@Nonnull LastenausgleichTagesschuleAngabenGemeindeContainer container,
		@Nonnull Sprache sprache,
		@Nonnull Einstellung lohnnormkosten,
		@Nonnull Einstellung lohnnormkostenLessThan50
	) throws MergeDocException {
		Objects.requireNonNull(container, "Das Argument 'container' darf nicht leer sein");

		LATSReportPdfGenerator pdfGenerator = new LATSReportPdfGenerator(container, lohnnormkosten, lohnnormkostenLessThan50, sprache);
		return generateDokument(pdfGenerator, false, sprache.getLocale(), container.getGemeinde().getMandant());
	}
}
