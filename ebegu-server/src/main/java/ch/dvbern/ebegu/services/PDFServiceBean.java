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
import ch.dvbern.ebegu.entities.RueckforderungFormular;
import ch.dvbern.ebegu.entities.Verfuegung;
import ch.dvbern.ebegu.enums.EinstellungKey;
import ch.dvbern.ebegu.enums.ErrorCodeEnum;
import ch.dvbern.ebegu.errors.EbeguEntityNotFoundException;
import ch.dvbern.ebegu.errors.MergeDocException;
import ch.dvbern.ebegu.pdfgenerator.BegleitschreibenPdfGenerator;
import ch.dvbern.ebegu.pdfgenerator.ErsteMahnungPdfGenerator;
import ch.dvbern.ebegu.pdfgenerator.FinanzielleSituationPdfGenerator;
import ch.dvbern.ebegu.pdfgenerator.FreigabequittungPdfGenerator;
import ch.dvbern.ebegu.pdfgenerator.KibonPdfGenerator;
import ch.dvbern.ebegu.pdfgenerator.MahnungPdfGenerator;
import ch.dvbern.ebegu.pdfgenerator.MandantPdfGenerator;
import ch.dvbern.ebegu.pdfgenerator.PdfUtil;
import ch.dvbern.ebegu.pdfgenerator.RueckforderungVerfuegungPdfGenerator;
import ch.dvbern.ebegu.pdfgenerator.VerfuegungPdfGenerator;
import ch.dvbern.ebegu.pdfgenerator.VerfuegungPdfGenerator.Art;
import ch.dvbern.ebegu.pdfgenerator.AnmeldebestaetigungTSPDFGenerator;
import ch.dvbern.ebegu.pdfgenerator.ZweiteMahnungPdfGenerator;
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

		// Bei Nicht-Eintreten soll der FEBR-Erklaerungstext gar nicht erscheinen, es ist daher egal,
		// was wir mitgeben
		VerfuegungPdfGenerator pdfGenerator = new VerfuegungPdfGenerator(
			betreuung,
			stammdaten,
			Art.NICHT_EINTRETTEN,
			false, false);
		return generateDokument(pdfGenerator, !writeProtected, locale);
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
		return generateDokument(pdfGenerator, !writeProtected, locale);
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

		FreigabequittungPdfGenerator pdfGenerator = new FreigabequittungPdfGenerator(gesuch, stammdaten,
			benoetigteUnterlagen);
		return generateDokument(pdfGenerator, !writeProtected, locale);
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
		return generateDokument(pdfGenerator, !writeProtected, locale);
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
			FinanzielleSituationPdfGenerator pdfGenerator = new FinanzielleSituationPdfGenerator(
				gesuch, famGroessenVerfuegung, stammdaten, erstesEinreichungsdatum);
			return generateDokument(pdfGenerator, !writeProtected, locale);
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

		boolean stadtBernAsivConfigured = applicationPropertyService.isStadtBernAsivConfigured();

		Art art = betreuung.hasAnspruch() ? Art.NORMAL : Art.KEIN_ANSPRUCH;
		VerfuegungPdfGenerator pdfGenerator = new VerfuegungPdfGenerator(
			betreuung,
			stammdaten,
			art,
			showInfoKontingentierung,
			stadtBernAsivConfigured);
		return generateDokument(pdfGenerator, !writeProtected, locale);
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
		return generateDokument(pdfGenerator, !writeProtected, locale);
	}

	@Nonnull
	@Override
	public byte[] generateProvisorischeVerfuegungRuckforderungformular(@Nonnull RueckforderungFormular rueckforderungFormular,
		boolean writeProtected,
		@Nonnull Locale locale) throws MergeDocException {
		Objects.requireNonNull(rueckforderungFormular, "Das Argument 'rueckforderungFormular' darf nicht leer sein");

		String nameVerantwortlichePerson = ebeguConfiguration.getNotverordnungUnterschriftName();
		String unterschriftPath = ebeguConfiguration.getNotverordnungUnterschriftPath();
		RueckforderungVerfuegungPdfGenerator pdfGenerator =
			new RueckforderungVerfuegungPdfGenerator(rueckforderungFormular, true, nameVerantwortlichePerson, unterschriftPath);
		return generateDokument(pdfGenerator, !writeProtected, locale);
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
		@Nonnull Locale locale
	) throws MergeDocException {

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try {
			pdfGenerator.generate(baos);
			byte[] content = baos.toByteArray();
			if (entwurf) {
				return PdfUtil.addEntwurfWatermark(content, locale);
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
		@Nonnull Locale locale
	) throws MergeDocException {

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try {
			pdfGenerator.generate(baos);
			byte[] content = baos.toByteArray();
			if (entwurf) {
				return PdfUtil.addEntwurfWatermark(content, locale);
			}
			return content;
		} catch (InvoiceGeneratorException | IOException e) {
			throw new MergeDocException("generateDokument()",
				"Bei der Generierung des Dokuments ist ein Fehler aufgetreten", e, OBJECTARRAY);
		}
	}
}
