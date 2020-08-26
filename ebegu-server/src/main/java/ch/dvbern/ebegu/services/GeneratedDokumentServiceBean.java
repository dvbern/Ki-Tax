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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;

import javax.activation.MimeTypeParseException;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.ejb.Local;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import ch.dvbern.ebegu.config.EbeguConfiguration;
import ch.dvbern.ebegu.entities.AbstractAnmeldung;
import ch.dvbern.ebegu.entities.AbstractEntity;
import ch.dvbern.ebegu.entities.AbstractEntity_;
import ch.dvbern.ebegu.entities.Adresse;
import ch.dvbern.ebegu.entities.AnmeldungTagesschule;
import ch.dvbern.ebegu.entities.Betreuung;
import ch.dvbern.ebegu.entities.FileMetadata_;
import ch.dvbern.ebegu.entities.GemeindeStammdaten;
import ch.dvbern.ebegu.entities.GeneratedDokument;
import ch.dvbern.ebegu.entities.GeneratedDokument_;
import ch.dvbern.ebegu.entities.GeneratedGeneralDokument;
import ch.dvbern.ebegu.entities.GeneratedNotrechtDokument;
import ch.dvbern.ebegu.entities.GeneratedNotrechtDokument_;
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.entities.Gesuchsperiode;
import ch.dvbern.ebegu.entities.InstitutionStammdaten;
import ch.dvbern.ebegu.entities.Mahnung;
import ch.dvbern.ebegu.entities.Pain001Dokument;
import ch.dvbern.ebegu.entities.Pain001Dokument_;
import ch.dvbern.ebegu.entities.RueckforderungFormular;
import ch.dvbern.ebegu.entities.Verfuegung;
import ch.dvbern.ebegu.entities.WriteProtectedDokument;
import ch.dvbern.ebegu.entities.Zahlungsauftrag;
import ch.dvbern.ebegu.enums.AntragStatus;
import ch.dvbern.ebegu.enums.ApplicationPropertyKey;
import ch.dvbern.ebegu.enums.Betreuungsstatus;
import ch.dvbern.ebegu.enums.ErrorCodeEnum;
import ch.dvbern.ebegu.enums.FinSitStatus;
import ch.dvbern.ebegu.enums.GeneratedDokumentTyp;
import ch.dvbern.ebegu.enums.Sprache;
import ch.dvbern.ebegu.enums.ZahlungauftragStatus;
import ch.dvbern.ebegu.errors.EbeguEntityNotFoundException;
import ch.dvbern.ebegu.errors.EbeguRuntimeException;
import ch.dvbern.ebegu.errors.KibonLogLevel;
import ch.dvbern.ebegu.errors.MergeDocException;
import ch.dvbern.ebegu.pdfgenerator.PdfUtil;
import ch.dvbern.ebegu.persistence.CriteriaQueryHelper;
import ch.dvbern.ebegu.rules.BetreuungsgutscheinEvaluator;
import ch.dvbern.ebegu.rules.Rule;
import ch.dvbern.ebegu.util.Constants;
import ch.dvbern.ebegu.util.DokumenteUtil;
import ch.dvbern.ebegu.util.EbeguUtil;
import ch.dvbern.ebegu.util.KitaxUebergangsloesungParameter;
import ch.dvbern.ebegu.util.ServerMessageUtil;
import ch.dvbern.ebegu.util.UploadFileInfo;
import ch.dvbern.lib.cdipersistence.Persistence;
import ch.dvbern.oss.lib.beanvalidation.embeddables.IBAN;
import ch.dvbern.oss.lib.iso20022.pain001.v00103ch02.AuszahlungDTO;
import ch.dvbern.oss.lib.iso20022.pain001.v00103ch02.Pain001DTO;
import ch.dvbern.oss.lib.iso20022.pain001.v00103ch02.Pain001Service;
import com.lowagie.text.DocumentException;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Service fuer GeneratedDokument
 */
@SuppressWarnings("InstanceMethodNamingConvention")
@Stateless
@Local(GeneratedDokumentService.class)
public class GeneratedDokumentServiceBean extends AbstractBaseService implements GeneratedDokumentService {

	private static final Logger LOGGER = LoggerFactory.getLogger(GeneratedDokumentServiceBean.class.getSimpleName());
	public static final byte[] EMPTY_BYTES = new byte[0];

	@Inject
	private Persistence persistence;

	@Inject
	private FileSaverService fileSaverService;

	@Inject
	private FinanzielleSituationService finanzielleSituationService;

	@Inject
	private EbeguConfiguration ebeguConfiguration;

	@Inject
	private PDFService pdfService;

	@Inject
	private VerfuegungService verfuegungService;

	@Inject
	private GesuchService gesuchService;

	@Inject
	private MahnungService mahnungService;

	@Inject
	private ApplicationPropertyService applicationPropertyService;

	@Inject
	private GemeindeService gemeindeService;

	@Inject
	private RulesService rulesService;

	@Inject
	private Authorizer authorizer;

	@Inject
	private CriteriaQueryHelper criteriaQueryHelper;

	@Inject
	private Pain001Service pain001Service;

	@Inject
	private BetreuungService betreuungService;


	@Override
	@Nonnull
	public WriteProtectedDokument saveDokument(@Nonnull WriteProtectedDokument dokument) {
		Objects.requireNonNull(dokument);
		return persistence.merge(dokument);
	}

	@Override
	@Nullable
	public WriteProtectedDokument findGeneratedDokument(@Nonnull String id, @Nonnull String filename) {

		final CriteriaBuilder cb = persistence.getCriteriaBuilder();
		final CriteriaQuery<GeneratedDokument> query = cb.createQuery(GeneratedDokument.class);
		Root<GeneratedDokument> root = query.from(GeneratedDokument.class);

		Predicate predGesuch = cb.equal(root.get(GeneratedDokument_.gesuch).get(AbstractEntity_.id), id);
		Predicate predFileName = cb.equal(root.get(FileMetadata_.filename), filename);

		query.where(predGesuch, predFileName);
		return persistence.getCriteriaSingleResult(query);
	}

	@Nullable
	@Override
	public Pain001Dokument findPain001Dokument(@Nonnull String zahlungsauftragId, @Nonnull String filename) {

		final CriteriaBuilder cb = persistence.getCriteriaBuilder();
		final CriteriaQuery<Pain001Dokument> query = cb.createQuery(Pain001Dokument.class);
		Root<Pain001Dokument> root = query.from(Pain001Dokument.class);

		Predicate predZahlungsauftrag =
			cb.equal(root.get(Pain001Dokument_.zahlungsauftrag).get(AbstractEntity_.id), zahlungsauftragId);
		Predicate predFileName = cb.equal(root.get(FileMetadata_.filename), filename);

		query.where(predZahlungsauftrag, predFileName);
		return persistence.getCriteriaSingleResult(query);
	}

	@Override
	@Nullable
	public WriteProtectedDokument findGeneratedNotrechtDokument(@Nonnull String id, @Nonnull String filename) {

		final CriteriaBuilder cb = persistence.getCriteriaBuilder();
		final CriteriaQuery<GeneratedNotrechtDokument> query = cb.createQuery(GeneratedNotrechtDokument.class);
		Root<GeneratedNotrechtDokument> root = query.from(GeneratedNotrechtDokument.class);

		Predicate predGesuch = cb.equal(root.get(GeneratedNotrechtDokument_.rueckforderungFormular).get(AbstractEntity_.id), id);
		Predicate predFileName = cb.equal(root.get(FileMetadata_.filename), filename);

		query.where(predGesuch, predFileName);
		return persistence.getCriteriaSingleResult(query);
	}


	/**
	 * Sucht ein WriteProtectedDokument mit demselben Namen und Pfad und vom selben Gesuch. Wen das Dokument
	 * existiert, wird dieses gelöscht und mit dem Neuen ersetzt. Wenn es nicht existiert, ein neues wird erstellt.
	 */
	@Nonnull
	@Override
	@SuppressFBWarnings("BC_UNCONFIRMED_CAST")
	public WriteProtectedDokument saveGeneratedDokumentInDB(
		@Nonnull byte[] data,
		@Nonnull GeneratedDokumentTyp dokumentTyp,
		@Nonnull AbstractEntity entity,
		@Nonnull String fileName,
		boolean writeProtected) throws MimeTypeParseException {

		WriteProtectedDokument writeProtectedDokument;
		String filePathToRemove = null;
		if (entity instanceof Gesuch) {
			writeProtectedDokument = this.findGeneratedDokument(entity.getId(), fileName);
			if (writeProtectedDokument == null) {
				writeProtectedDokument = new GeneratedDokument();
			} else {
				//Die Datei wird am Ende geloscht, um unvollstaenige Daten zu vermeiden falls was kaputt geht
				filePathToRemove = writeProtectedDokument.getFilepfad();
			}
			((GeneratedDokument) writeProtectedDokument).setGesuch((Gesuch) entity);
		}
		else if (entity instanceof RueckforderungFormular){
			writeProtectedDokument = this.findGeneratedNotrechtDokument(entity.getId(), fileName);
			if (writeProtectedDokument == null) {
				writeProtectedDokument = new GeneratedNotrechtDokument();
			} else {
				//Die Datei wird am Ende geloscht, um unvollstaenige Daten zu vermeiden falls was kaputt geht
				filePathToRemove = writeProtectedDokument.getFilepfad();
			}
			((GeneratedNotrechtDokument) writeProtectedDokument).setRueckforderungFormular((RueckforderungFormular) entity);
		} else { // case of pain001
			writeProtectedDokument = this.findPain001Dokument(entity.getId(), fileName);
			if (writeProtectedDokument == null) {
				writeProtectedDokument = new Pain001Dokument();
			} else {
				//Die Datei wird am Ende geloscht, um unvollstaenige Daten zu vermeiden falls was kaputt geht
				filePathToRemove = writeProtectedDokument.getFilepfad();
			}
			Pain001Dokument.class.cast(writeProtectedDokument).setZahlungsauftrag((Zahlungsauftrag) entity);
		}

		final UploadFileInfo savedDokument = fileSaverService.save(data,
			fileName, entity.getId());

		writeProtectedDokument.setFilename(savedDokument.getFilename());
		writeProtectedDokument.setFilepfad(savedDokument.getPath());
		writeProtectedDokument.setFilesize(savedDokument.getSizeString());
		writeProtectedDokument.setTyp(dokumentTyp);
		writeProtectedDokument.setWriteProtected(writeProtected);

		WriteProtectedDokument returnDocument = this.saveDokument(writeProtectedDokument);

		if (filePathToRemove != null) {
			fileSaverService.remove(filePathToRemove);
		}

		return returnDocument;
	}

	@Nonnull
	@Override
	public WriteProtectedDokument getFinSitDokumentAccessTokenGeneratedDokument(
		@Nonnull final Gesuch gesuch,
		@Nonnull Boolean forceCreation) throws MimeTypeParseException, MergeDocException {

		if (!EbeguUtil.isFinanzielleSituationRequired(gesuch)) {
			throw new EbeguRuntimeException(
				"getFinSitDokumentAccessTokenGeneratedDokument",
				ErrorCodeEnum.ERROR_FIN_SIT_IS_NOT_REQUIRED);
		}

		final GemeindeStammdaten stammdaten = gemeindeService.getGemeindeStammdatenByGemeindeId(gesuch.extractGemeinde().getId()).orElseThrow(
			() -> new EbeguEntityNotFoundException("uploadLogo", ErrorCodeEnum.ERROR_ENTITY_NOT_FOUND, gesuch.extractGemeinde().getId()));
		final Sprache sprache = EbeguUtil.extractKorrespondenzsprache(gesuch, stammdaten);

		final String fileNameForGeneratedDokumentTyp = DokumenteUtil.getFileNameForGeneratedDokumentTyp(
			GeneratedDokumentTyp.FINANZIELLE_SITUATION, gesuch.getJahrFallAndGemeindenummer(),
			sprache.getLocale()
		);

		WriteProtectedDokument documentIfExistsAndIsWriteProtected =
			getDocumentIfExistsAndIsWriteProtected(gesuch.getId(), fileNameForGeneratedDokumentTyp, forceCreation);
		if (documentIfExistsAndIsWriteProtected != null) {
			return documentIfExistsAndIsWriteProtected;
		}
		WriteProtectedDokument persistedDokument = null;
		if (!forceCreation && gesuch.getStatus().isAnyStatusOfVerfuegtOrVefuegen()) {
			persistedDokument = getExistingGeneratedDokument(gesuch.getId(),
				GeneratedDokumentTyp.FINANZIELLE_SITUATION,
				fileNameForGeneratedDokumentTyp);
		}
		if (!gesuch.getStatus().isAnyStatusOfVerfuegtOrVefuegen() || persistedDokument == null) {
			//  persistedDokument == null:  Wenn das Dokument nicht geladen werden konnte, heisst es dass es nicht
			// existiert und wir muessen es trotzdem erstellen
			if (!gesuch.hasOnlyBetreuungenOfSchulamt()) {
				// Bei nur Schulamt prüfen wir die Berechtigung nicht, damit das JA solche Gesuche schliessen kann.
				// Der UseCase ist, dass zuerst ein zweites Angebot vorhanden war, dieses aber durch das JA gelöscht wurde.
				authorizer.checkReadAuthorizationFinSit(gesuch);
			}
			finanzielleSituationService.calculateFinanzDaten(gesuch);

			// Die Betreuungen mit ihren Vorgängern initialisieren, damit der MutationsMerger funktioniert!
			verfuegungService.initializeVorgaengerVerfuegungen(gesuch);

			final BetreuungsgutscheinEvaluator evaluator = initEvaluator(gesuch, sprache.getLocale());
			final Verfuegung famGroessenVerfuegung = evaluator.evaluateFamiliensituation(gesuch, sprache.getLocale());
			boolean writeProtectPDF = forceCreation;
			byte[] data = pdfService.generateFinanzielleSituation(gesuch,
				famGroessenVerfuegung,
				writeProtectPDF,
				sprache.getLocale());
			// FINANZIELLE_SITUATION in einem Zustand isAnyStatusOfVerfuegt oder Verfügen, soll das Dokument
			// schreibgeschützt sein!
			persistedDokument = saveGeneratedDokumentInDB(data, GeneratedDokumentTyp.FINANZIELLE_SITUATION, gesuch,
				fileNameForGeneratedDokumentTyp, writeProtectPDF);

		}
		return persistedDokument;
	}

	@Nonnull
	@Override
	public WriteProtectedDokument getBegleitschreibenDokument(
		@Nonnull final Gesuch gesuch,
		@Nonnull Boolean forceCreation
	) throws MimeTypeParseException, MergeDocException {

		final Sprache sprache = EbeguUtil.extractKorrespondenzsprache(gesuch, gemeindeService);

		final String fileNameForGeneratedDokumentTyp = DokumenteUtil.getFileNameForGeneratedDokumentTyp(
			GeneratedDokumentTyp.BEGLEITSCHREIBEN, gesuch.getJahrFallAndGemeindenummer(),
			sprache.getLocale()
		);

		// Das Begleitschreiben wird per Definition immer erst nach dem Verfügen erstellt, da die Verfügungen bzw.
		// Nicht-Eintretensverfügungen als Anhang im Brief erwähnt werden! Sollte das Gesuch ein Status von
		// Verfuegt haben, wird es als writeprotected gespeichert
		WriteProtectedDokument document =
			getDocumentIfExistsAndIsWriteProtected(gesuch.getId(), fileNameForGeneratedDokumentTyp, forceCreation);
		if (document == null) {

			boolean writeProtectPDF = gesuch.getStatus().isAnyStatusOfVerfuegt();
			boolean addWatermark = !gesuch.getStatus().isAnyStatusOfVerfuegt();
			byte[] data = pdfService.generateBegleitschreiben(gesuch, !addWatermark, sprache.getLocale());
			document = saveGeneratedDokumentInDB(data, GeneratedDokumentTyp.BEGLEITSCHREIBEN, gesuch,
				fileNameForGeneratedDokumentTyp, writeProtectPDF);
		}
		return document;
	}

	@Nonnull
	@Override
	public WriteProtectedDokument getKompletteKorrespondenz(@Nonnull final Gesuch gesuch)
		throws MimeTypeParseException, MergeDocException {

		List<InputStream> docsToMerge = new ArrayList<>();

		addBegleitschreibenDoc(gesuch, docsToMerge);

		addBetreuungenDoc(gesuch, docsToMerge);

		addFinanzielleSituationDoc(gesuch, docsToMerge);

		addErlaeuterungenDoc(gesuch, docsToMerge);

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try {
			PdfUtil.doMerge(docsToMerge, baos, true);
		} catch (DocumentException | IOException e) {
			throw new MergeDocException("getKompletteKorrespondenz", "Dokumente konnten nicht gemergt werden", e);
		}

		WriteProtectedDokument document =
			saveGeneratedDokumentInDB(baos.toByteArray(), GeneratedDokumentTyp.KOMPLETTE_KORRESPONDEZ, gesuch,
				"KompletteKorrespondenz.pdf", false);

		return document;
	}

	private void addFinanzielleSituationDoc(Gesuch gesuch, List<InputStream> docsToMerge) throws MergeDocException {
		if (EbeguUtil.isFinanzielleSituationRequired(gesuch) && gesuch.getFinSitStatus() == FinSitStatus.AKZEPTIERT) {
			byte[] finanzielleSituation = readFileIfExists(GeneratedDokumentTyp.FINANZIELLE_SITUATION,
				gesuch.getJahrFallAndGemeindenummer(),
				gesuch);
			if (finanzielleSituation.length > 0) {
				docsToMerge.add(new ByteArrayInputStream(finanzielleSituation));
			}
		}
	}

	private void addErlaeuterungenDoc(Gesuch gesuch, List<InputStream> docsToMerge) {
		if (EbeguUtil.isErlaeuterungenZurVerfuegungRequired(gesuch)) {
			final Sprache sprache = EbeguUtil.extractKorrespondenzsprache(gesuch, gemeindeService);
			Gesuchsperiode gesuchsperiode = gesuch.getGesuchsperiode();

			byte[] content = gesuchsperiode.getVerfuegungErlaeuterungWithSprache(sprache);

			if (content.length != 0) {
				docsToMerge.add(new ByteArrayInputStream(content));
			}
		}
	}

	private void addBetreuungenDoc(Gesuch gesuch, List<InputStream> docsToMerge) throws MergeDocException {
		for (Betreuung betreuung : gesuch.extractAllBetreuungen()) {
			// Verfuegt
			if (betreuung.getBetreuungsstatus() == Betreuungsstatus.VERFUEGT) {
				byte[] verfuegung = readFileIfExists(GeneratedDokumentTyp.VERFUEGUNG, betreuung.getBGNummer(), gesuch);
				if (verfuegung.length > 0) {
					docsToMerge.add(new ByteArrayInputStream(verfuegung));
				}
			} else if (betreuung.getBetreuungsstatus() == Betreuungsstatus.NICHT_EINGETRETEN) {
				byte[] nichtEintreten =
					readFileIfExists(GeneratedDokumentTyp.NICHTEINTRETEN, betreuung.getBGNummer(), gesuch);
				if (nichtEintreten.length > 0) {
					docsToMerge.add(new ByteArrayInputStream(nichtEintreten));
				}
			}
		}
	}

	private void addBegleitschreibenDoc(Gesuch gesuch, List<InputStream> docsToMerge) throws MergeDocException {
		byte[] begleitschreiben =
			readFileIfExists(GeneratedDokumentTyp.BEGLEITSCHREIBEN, gesuch.getJahrFallAndGemeindenummer(), gesuch);
		if (begleitschreiben.length > 0) {
			docsToMerge.add(new ByteArrayInputStream(begleitschreiben));
		}
	}

	@Nonnull
	private byte[] readFileIfExists(GeneratedDokumentTyp dokumentTyp, String identification, Gesuch gesuch)
		throws MergeDocException {
		final Sprache sprache = EbeguUtil.extractKorrespondenzsprache(gesuch, gemeindeService);
		final String filename =
			DokumenteUtil.getFileNameForGeneratedDokumentTyp(dokumentTyp, identification, sprache.getLocale());
		WriteProtectedDokument dokument = getDocumentIfExistsAndIsWriteProtected(gesuch.getId(), filename, false);
		if (dokument != null) {
			Path filePath = Paths.get(dokument.getFilepfad());
			try {
				return Files.readAllBytes(filePath);
			} catch (Exception e) {
				throw new MergeDocException("readFileIfExists", dokumentTyp + " kann nicht gelesen werden", e);
			}
		}
		return EMPTY_BYTES;
	}

	@Nonnull
	@Override
	public WriteProtectedDokument getFreigabequittungAccessTokenGeneratedDokument(
		@Nonnull final Gesuch gesuch,
		@Nonnull Boolean forceCreation
	) throws MimeTypeParseException, MergeDocException {

		final Sprache sprache = EbeguUtil.extractKorrespondenzsprache(gesuch, gemeindeService);

		final String fileNameForGeneratedDokumentTyp = DokumenteUtil.getFileNameForGeneratedDokumentTyp(
			GeneratedDokumentTyp.FREIGABEQUITTUNG, gesuch.getJahrFallAndGemeindenummer(),
			sprache.getLocale()
		);

		WriteProtectedDokument documentIfExistsAndIsWriteProtected =
			getDocumentIfExistsAndIsWriteProtected(gesuch.getId(), fileNameForGeneratedDokumentTyp, forceCreation);
		if (documentIfExistsAndIsWriteProtected != null) {
			return documentIfExistsAndIsWriteProtected;
		}

		WriteProtectedDokument persistedDokument = null;
		if (!forceCreation) {
			persistedDokument = getExistingGeneratedDokument(gesuch.getId(),
				GeneratedDokumentTyp.FREIGABEQUITTUNG,
				fileNameForGeneratedDokumentTyp);
		}
		if (persistedDokument == null || forceCreation) {

			authorizer.checkReadAuthorizationFinSit(gesuch);

			if (!gesuch.getStatus().inBearbeitung() && !forceCreation) {
				LOGGER.error(
					"{} für Gesuch {} nicht gefunden.",
					GeneratedDokumentTyp.FREIGABEQUITTUNG.name(),
					gesuch.getJahrFallAndGemeindenummer());
			}

			gesuchService.antragFreigabequittungErstellen(gesuch, AntragStatus.FREIGABEQUITTUNG);
			byte[] data = pdfService.generateFreigabequittung(
				gesuch,
				true,
				EbeguUtil.extractKorrespondenzsprache(gesuch, gemeindeService).getLocale()
			);

			// Freigabequittung soll wird nur einmal produziert und soll deswegen immer schreibgeschützt sein!
			persistedDokument = saveGeneratedDokumentInDB(data, GeneratedDokumentTyp.FREIGABEQUITTUNG, gesuch,
				fileNameForGeneratedDokumentTyp, true);
		}

		return persistedDokument;
	}

	@Nullable
	private WriteProtectedDokument getExistingGeneratedDokument(
		String id,
		GeneratedDokumentTyp dokumentTyp,
		String fileNameForGeneratedDokumentTyp) {
		final WriteProtectedDokument persistedDokument;
		if (dokumentTyp == GeneratedDokumentTyp.PAIN001) {
			persistedDokument = findPain001Dokument(id, fileNameForGeneratedDokumentTyp);
		} else {
			persistedDokument = findGeneratedDokument(id, fileNameForGeneratedDokumentTyp);
		}

		if (persistedDokument == null) {
			String expectedFilepath = ebeguConfiguration.getDocumentFilePath() + '/' + id;
			LOGGER.error("Das Dokument vom Typ: {} fuer Antragnummer {} konnte unter dem Pfad {} " +
					"nicht gefunden  werden obwohl es existieren muesste. Wird neu generiert!", dokumentTyp,
				id, expectedFilepath);
		}

		if (persistedDokument != null && !Files.exists(Paths.get(persistedDokument.getFilepfad()))) {
			if (persistedDokument.isWriteProtected()) {
				// Super-GAU: Das Entity ist da, aber das referenzierte File auf dem Filesystem ist nicht vorhanden!
				// Wir geben trotzdem das (verwaiste) Entity zurück, da sonst ein neues File (auf dem Filesystem)
				// erstellt
				// wird, das Entity aber nicht geupdated werden darf. So wird dem Client ein ungültiger Download-Link
				// zurückgegeben und der Benutzer erhält eine entsprechende Fehlermeldung.
				LOGGER.error(
					"Die schreibgeschützte Datei {} konnte nicht gefunden werden!",
					persistedDokument.getFilepfad());
				return persistedDokument; // Neu hier das (verwaiste) Entity zuereckgegeben, damit wenigstens nicht
				// noch ein neues Dokument auf dem Filesystem erstellt wird. Ergibt Fehler im Download auf Client
			}
			// GAU:Das Entity ist da, aber das File auf dem Filesystem nicht. Das File darf aber grundsätzlich neu
			// erstellt werden, es ist noch nicht writeprotected. Wir geben also null zurück.
			LOGGER.error(
				"Die Datei {} konnte nicht gefunden werden. Wir geben null zurück, damit sie neu erstellt wird.",
				persistedDokument.getFilepfad());
			return null;
		}
		return persistedDokument;
	}

	@Nonnull
	private Optional<WriteProtectedDokument> getMaybeExistingGeneratedDokument(
		String gesuchId,
		String fileNameForGeneratedDokumentTyp) {
		final WriteProtectedDokument persistedDokument =
			findGeneratedDokument(gesuchId, fileNameForGeneratedDokumentTyp);
		return Optional.ofNullable(persistedDokument);
	}

	private Optional<WriteProtectedDokument> getMaybeExistingPain001Document(
		String zahlungsauftragId,
		String fileNameForGeneratedDokumentTyp) {
		final WriteProtectedDokument persistedDokument =
			findPain001Dokument(zahlungsauftragId, fileNameForGeneratedDokumentTyp);
		return Optional.ofNullable(persistedDokument);
	}

	@Nonnull
	private Optional<WriteProtectedDokument> getMaybeExistingGeneratedNotrechtDokument(
		String rueckforderungFormularId,
		String fileNameForGeneratedDokumentTyp) {
		final WriteProtectedDokument persistedDokument =
			findGeneratedNotrechtDokument(rueckforderungFormularId, fileNameForGeneratedDokumentTyp);
		return Optional.ofNullable(persistedDokument);
	}

	@SuppressWarnings("Duplicates")
	@Nullable
	private WriteProtectedDokument getDocumentIfExistsAndIsWriteProtected(
		String gesuchId,
		String fileNameForGeneratedDokumentTyp,
		@Nonnull Boolean forceCreation) {
		Optional<WriteProtectedDokument> optionalDokument =
			getMaybeExistingGeneratedDokument(gesuchId, fileNameForGeneratedDokumentTyp);
		if (optionalDokument.isPresent() && optionalDokument.get().isWriteProtected()) {
			if (forceCreation) {
				// Dies ist ein Zustand, der eigentlich gar nicht vorkommen dürfte: Wir wollen explizit das Dokument
				// neu
				// erstellen (forceCreate), es ist aber writeProtected.
				// Wir vermuten/hoffen, dass dies nur bei sehr schnellem Doppelklick vorkommen kann. Da der Benutzer
				// mit
				// einer eventuellen Fehlermeldung sowieso nichts anfangen könnte, geben wir das bereits vorhandene
				// Dokument zurück, loggen aber den Vorfall.
				LOGGER.error(
					"Achtung, es wurde versucht, ein Dokument mit WriteProtection neu zu erstellen. "
						+ "PersistedDokument-ID: {}",
					optionalDokument.get()
						.getId());
			}
			return optionalDokument.get();
		}
		return null;
	}

	@Nullable
	private WriteProtectedDokument getNotrechtDocumentIfExistsAndIsWriteProtected(
		String rueckforderungFormularId,
		String fileNameForGeneratedDokumentTyp,
		@Nonnull Boolean forceCreation) {
		Optional<WriteProtectedDokument> optionalDokument =
			getMaybeExistingGeneratedNotrechtDokument(rueckforderungFormularId, fileNameForGeneratedDokumentTyp);
		if (optionalDokument.isPresent() && optionalDokument.get().isWriteProtected()) {
			if (forceCreation) {
				LOGGER.error(
					"Achtung, es wurde versucht, ein Dokument mit WriteProtection neu zu erstellen. "
						+ "PersistedDokument-ID: {}",
					optionalDokument.get()
						.getId());
			}
			return optionalDokument.get();
		}
		return null;
	}

	@SuppressWarnings("Duplicates")
	@Nullable
	private WriteProtectedDokument getPain001DocumentIfExistsAndIsWriteProtected(
		String zahlungsauftragId,
		String fileNameForGeneratedDokumentTyp,
		@Nonnull Boolean forceCreation) {
		Optional<WriteProtectedDokument> optionalDokument =
			getMaybeExistingPain001Document(zahlungsauftragId, fileNameForGeneratedDokumentTyp);
		if (optionalDokument.isPresent() && optionalDokument.get().isWriteProtected()) {
			if (forceCreation) {
				// Dies ist ein Zustand, der eigentlich gar nicht vorkommen dürfte: Wir wollen explizit das Dokument
				// neu erstellen (forceCreate), es ist aber writeProtected. Wir vermuten/hoffen, dass dies nur bei
				// sehr schnellem Doppelklick vorkommen kann. Da der Benutzer mit einer eventuellen Fehlermeldung
				// sowieso nichts anfangen könnte, geben wir das bereits vorhandene Dokument zurück, loggen aber den
				// Vorfall.
				LOGGER.error(
					"Achtung, es wurde versucht, ein Dokument mit WriteProtection neu zu erstellen. "
						+ "PersistedDokument-ID: {}",
					optionalDokument.get()
						.getId());
			}
			return optionalDokument.get();
		}
		return null;
	}

	@Nonnull
	private BetreuungsgutscheinEvaluator initEvaluator(@Nonnull Gesuch gesuch, @Nonnull Locale locale) {
		KitaxUebergangsloesungParameter kitaxParameter = loadKitaxUebergangsloesungParameter();
		List<Rule> rules =
			rulesService.getRulesForGesuchsperiode(gesuch.extractGemeinde(), gesuch.getGesuchsperiode(), kitaxParameter, locale);
		Boolean enableDebugOutput = applicationPropertyService.findApplicationPropertyAsBoolean(
			ApplicationPropertyKey.EVALUATOR_DEBUG_ENABLED,
			true);
		BetreuungsgutscheinEvaluator bgEvaluator = new BetreuungsgutscheinEvaluator(rules, enableDebugOutput);
		loadCalculatorParameters(gesuch.extractGemeinde(), gesuch.getGesuchsperiode());
		return bgEvaluator;
	}

	@Nonnull
	@Override
	public WriteProtectedDokument getVerfuegungDokumentAccessTokenGeneratedDokument(
		@Nonnull final Gesuch gesuch,
		@Nonnull Betreuung betreuung,
		@Nonnull String manuelleBemerkungen,
		@Nonnull Boolean forceCreation
	) throws MimeTypeParseException, MergeDocException {

		final Sprache sprache = EbeguUtil.extractKorrespondenzsprache(gesuch, gemeindeService);
		String bgNummer = betreuung.getBGNummer();
		String fileNameForGeneratedDokumentTyp = DokumenteUtil
			.getFileNameForGeneratedDokumentTyp(GeneratedDokumentTyp.VERFUEGUNG, bgNummer, sprache.getLocale());

		WriteProtectedDokument documentIfExistsAndIsWriteProtected =
			getDocumentIfExistsAndIsWriteProtected(gesuch.getId(), fileNameForGeneratedDokumentTyp, forceCreation);
		if (documentIfExistsAndIsWriteProtected != null) {
			return documentIfExistsAndIsWriteProtected;
		}

		WriteProtectedDokument persistedDokument = null;

		if (!forceCreation && Betreuungsstatus.VERFUEGT == betreuung.getBetreuungsstatus()) {
			persistedDokument = findGeneratedDokument(gesuch.getId(), fileNameForGeneratedDokumentTyp);
			if (persistedDokument == null) {
				String expectedFilepath = ebeguConfiguration.getDocumentFilePath() + '/' + gesuch.getId();
				LOGGER.error(
					"Das Dokument vom Typ: {} fuer Betreuungsnummer {} konnte unter dem Pfad {} " +
						"nicht gefunden  werden obwohl es existieren muesste. Wird neu generiert!",
					GeneratedDokumentTyp.VERFUEGUNG.name(),
					bgNummer,
					expectedFilepath);
			}
		}
		// Wenn die Betreuung nicht verfuegt ist oder das Dokument nicht geladen werden konnte, heisst es dass es
		// nicht existiert und wir muessen es erstellen
		// (Der Status wird auf Verfuegt gesetzt, BEVOR das Dokument erstellt wird!)
		if (Betreuungsstatus.VERFUEGT != betreuung.getBetreuungsstatus() || persistedDokument == null) {
			finanzielleSituationService.calculateFinanzDaten(gesuch);
			Gesuch gesuchWithVerfuegung = verfuegungService.calculateVerfuegung(gesuch);

			Betreuung matchedBetreuung = gesuchWithVerfuegung.extractBetreuungById(betreuung.getId());
			if (matchedBetreuung != null) {
				if (!manuelleBemerkungen.isEmpty()) {
					Validate.notNull(matchedBetreuung.getVerfuegungOrVerfuegungPreview());
					matchedBetreuung.getVerfuegungOrVerfuegungPreview().setManuelleBemerkungen(manuelleBemerkungen);
				}

				Optional<LocalDate> optVorherigeVerfuegungDate =
					verfuegungService.findVorgaengerVerfuegungDate(betreuung);
				LocalDate letztesVerfDatum = optVorherigeVerfuegungDate.orElse(null);
				boolean writeProtectPDF = Betreuungsstatus.VERFUEGT == betreuung.getBetreuungsstatus();
				final byte[] verfuegungsPDF = pdfService
					.generateVerfuegungForBetreuung(
						matchedBetreuung,
						letztesVerfDatum,
						writeProtectPDF,
						sprache.getLocale());

				final String fileNameForDocTyp =
					DokumenteUtil.getFileNameForGeneratedDokumentTyp(GeneratedDokumentTyp.VERFUEGUNG,
						matchedBetreuung.getBGNummer(), sprache.getLocale());

				// Wenn die Betreuung im Zustand Verfügt ist, soll das Dokument als schreibgeschützt gespeichert
				// werden.
				persistedDokument = saveGeneratedDokumentInDB(verfuegungsPDF, GeneratedDokumentTyp.VERFUEGUNG,
					gesuch, fileNameForDocTyp, writeProtectPDF);
			} else {
				throw new EbeguEntityNotFoundException("getVerfuegungDokumentAccessTokenGeneratedDokument",
					ErrorCodeEnum.ERROR_ENTITY_NOT_FOUND, "Betreuung not found: " + betreuung.getId());
			}
		}
		return persistedDokument;
	}

	@Nonnull
	@Override
	public WriteProtectedDokument getMahnungDokumentAccessTokenGeneratedDokument(
		@Nonnull Mahnung mahnung,
		@Nonnull Boolean createWriteProtected
	) throws MimeTypeParseException, MergeDocException {

		Gesuch gesuch = mahnung.getGesuch();
		final Sprache sprache = EbeguUtil.extractKorrespondenzsprache(gesuch, gemeindeService);
		Mahnung mahnungDB = persistence.find(Mahnung.class, mahnung.getId());

		final String fileNameForGeneratedDokumentTyp = DokumenteUtil.getFileNameForGeneratedDokumentTyp(
			GeneratedDokumentTyp.MAHNUNG,
			mahnungDB == null || mahnungDB.getTimestampErstellt() == null
				? "ENTWURF"
				: Constants.FILENAME_DATE_TIME_FORMATTER.format(mahnungDB.getTimestampErstellt()),
			sprache.getLocale()
		);

		// Das Dokument muss solange neu erstellt werden, bis die Mahnung ausgelöst war
		boolean regenerateFile = (mahnungDB == null);
		// Wenn wir es nicht neu generieren wollen, suchen wir erst gar nicht nach einem evt. schon existierenden
		// file. Grund: Der Entwurf wird immer unter demselben Namen gespeichert, so dass wir im Status ENTWURF
		// sehr wahrscheinlich bereits ein altes File finden.
		if (!regenerateFile) {
			WriteProtectedDokument documentIfExistsAndIsWriteProtected =
				getDocumentIfExistsAndIsWriteProtected(gesuch.getId(),
					fileNameForGeneratedDokumentTyp, false);
			if (documentIfExistsAndIsWriteProtected != null) {
				return documentIfExistsAndIsWriteProtected;
			}
		}

		WriteProtectedDokument persistedDokument =
			findGeneratedDokument(gesuch.getId(), fileNameForGeneratedDokumentTyp);
		if (persistedDokument == null || regenerateFile) {

			GeneratedDokumentTyp dokumentTyp = GeneratedDokumentTyp.MAHNUNG;

			Optional<Mahnung> vorgaengerMahnung;

			if (mahnung.hasVorgaenger()) {
				Objects.requireNonNull(mahnung.getVorgaengerId());
				vorgaengerMahnung = mahnungService.findMahnung(mahnung.getVorgaengerId());
			} else {
				vorgaengerMahnung = mahnungService.findAktiveErstMahnung(gesuch);
			}

			byte[] data =
				pdfService.generateMahnung(mahnung, vorgaengerMahnung, createWriteProtected, sprache.getLocale());

			persistedDokument = saveGeneratedDokumentInDB(data, dokumentTyp, gesuch,
				fileNameForGeneratedDokumentTyp, createWriteProtected);
		}
		return persistedDokument;

	}

	@Nonnull
	@Override
	public WriteProtectedDokument getNichteintretenDokumentAccessTokenGeneratedDokument(
		@Nonnull Betreuung betreuung,
		@Nonnull Boolean forceCreation) throws MimeTypeParseException, MergeDocException {

		Gesuch gesuch = betreuung.extractGesuch();
		GeneratedDokumentTyp dokumentTyp = GeneratedDokumentTyp.NICHTEINTRETEN;
		final Sprache sprache = EbeguUtil.extractKorrespondenzsprache(gesuch, gemeindeService);

		final String fileNameForGeneratedDokumentTyp = DokumenteUtil
			.getFileNameForGeneratedDokumentTyp(dokumentTyp, betreuung.getBGNummer(), sprache.getLocale());

		WriteProtectedDokument documentIfExistsAndIsWriteProtected =
			getDocumentIfExistsAndIsWriteProtected(gesuch.getId(), fileNameForGeneratedDokumentTyp, forceCreation);
		if (documentIfExistsAndIsWriteProtected != null) {
			return documentIfExistsAndIsWriteProtected;
		}

		WriteProtectedDokument persistedDokument = null;

		if (!forceCreation && Betreuungsstatus.NICHT_EINGETRETEN == betreuung.getBetreuungsstatus()) {
			persistedDokument =
				getExistingGeneratedDokument(gesuch.getId(), dokumentTyp, fileNameForGeneratedDokumentTyp);
		}

		if (Betreuungsstatus.NICHT_EINGETRETEN != betreuung.getBetreuungsstatus() || persistedDokument == null) {
			verfuegungService.initializeVorgaengerVerfuegungen(gesuch);
			// persistedDokument == null:  Wenn das Dokument nicht geladen werden konnte, heisst es dass es nicht
			// existiert und wir muessen es trotzdem erstellen

			boolean writeProtectPDF = forceCreation || gesuch.getStatus().isAnyStatusOfVerfuegt();
			byte[] data = pdfService.generateNichteintreten(betreuung, writeProtectPDF, sprache.getLocale());

			// Wenn in einem Zustand isAnyStatusOfVerfuegt, soll das Dokument schreibgeschützt sein!
			persistedDokument = saveGeneratedDokumentInDB(data, dokumentTyp, gesuch,
				fileNameForGeneratedDokumentTyp, writeProtectPDF);
		}
		return persistedDokument;

	}

	@Nonnull
	@Override
	public WriteProtectedDokument getPain001DokumentAccessTokenGeneratedDokument(
		@Nonnull Zahlungsauftrag zahlungsauftrag,
		@Nonnull Boolean forceCreation
	) throws MimeTypeParseException {

		WriteProtectedDokument persistedDokument = null;

		GeneratedDokumentTyp dokumentTyp = GeneratedDokumentTyp.PAIN001;

		final GemeindeStammdaten stammdaten = gemeindeService.getGemeindeStammdatenByGemeindeId(zahlungsauftrag.getGemeinde().getId()).orElseThrow(
			() -> new EbeguEntityNotFoundException("getPain001DokumentAccessTokenGeneratedDokument",
				ErrorCodeEnum.ERROR_ENTITY_NOT_FOUND, zahlungsauftrag.getGemeinde().getId()));

		// Wenn die Zahlungsinformationen nicht komplett ausgefuellt sind, fahren wir hier nicht weiter.
		if (!stammdaten.isZahlungsinformationValid()) {
			throw new EbeguRuntimeException(KibonLogLevel.INFO,
				"getPain001DokumentAccessTokenGeneratedDokument",
				ErrorCodeEnum.ERROR_ZAHLUNGSINFORMATIONEN_INCOMPLETE,
				zahlungsauftrag.getGemeinde().getName());
		}

		Sprache korrespondenzsprache = EbeguUtil.extractGemeindeSprachen(stammdaten).get(0);

		final String fileNameForGeneratedDokumentTyp = DokumenteUtil
			.getFileNameForGeneratedDokumentTyp(dokumentTyp, zahlungsauftrag.getFilename(), korrespondenzsprache.getLocale());

		if (!forceCreation && ZahlungauftragStatus.ENTWURF != zahlungsauftrag.getStatus()) {
			persistedDokument = getPain001DocumentIfExistsAndIsWriteProtected(
				zahlungsauftrag.getId(), fileNameForGeneratedDokumentTyp, forceCreation);
		}

		if (ZahlungauftragStatus.ENTWURF == zahlungsauftrag.getStatus() || persistedDokument == null) {

			// persistedDokument == null:  Wenn das Dokument nicht geladen werden konnte, heisst es dass es nicht
			// existiert und wir muessen es trotzdem erstellen

			boolean writeProtectPDF = forceCreation || ZahlungauftragStatus.ENTWURF != zahlungsauftrag.getStatus();

			byte[] data = pain001Service.getPainFileContent(wrapZahlungsauftrag(zahlungsauftrag, stammdaten, korrespondenzsprache.getLocale()));

			// Wenn nicht Entwurf, soll das Dokument schreibgeschützt sein!
			persistedDokument = saveGeneratedDokumentInDB(data, dokumentTyp, zahlungsauftrag,
				fileNameForGeneratedDokumentTyp, writeProtectPDF);
		}
		return persistedDokument;

	}

	private Pain001DTO wrapZahlungsauftrag(
		@Nonnull Zahlungsauftrag zahlungsauftrag,
		@Nonnull GemeindeStammdaten gemeindeStammdaten,
		@Nonnull Locale locale
	) {
		Pain001DTO pain001DTO = new Pain001DTO();

		// Wenn die Zahlungsinformationen nicht komplett ausgefuellt sind, fahren wir hier nicht weiter.
		if (!gemeindeStammdaten.isZahlungsinformationValid()) {
			throw new EbeguRuntimeException(KibonLogLevel.INFO,
				"wrapZahlungsauftrag",
				ErrorCodeEnum.ERROR_ZAHLUNGSINFORMATIONEN_INCOMPLETE,
				zahlungsauftrag.getGemeinde().getName());
		}

		pain001DTO.setAuszahlungsDatum(zahlungsauftrag.getDatumFaellig());
		pain001DTO.setGenerierungsDatum(zahlungsauftrag.getDatumGeneriert());

		String debitorName = gemeindeStammdaten.getKontoinhaber();
		String debitorBic = gemeindeStammdaten.getBic();
		IBAN ibanGemeinde = gemeindeStammdaten.getIban();
		Objects.requireNonNull(ibanGemeinde, "Keine IBAN fuer Gemeinde " + gemeindeStammdaten.getGemeinde().getName());
		String debitorIban = ibanToUnformattedString(ibanGemeinde);
		String debitorIbanGebuehren = applicationPropertyService.findApplicationPropertyAsString(ApplicationPropertyKey.DEBTOR_IBAN_GEBUEHREN);

		pain001DTO.setSchuldnerName(debitorName);
		pain001DTO.setSchuldnerIBAN(debitorIban);
		pain001DTO.setSchuldnerBIC(debitorBic);
		pain001DTO.setSchuldnerIBANGebuehren(debitorIbanGebuehren == null ? pain001DTO.getSchuldnerIBAN() : debitorIbanGebuehren);
		pain001DTO.setSoftwareName("kiBon");
		// we use the currentTimeMillis so that it is always different
		pain001DTO.setMsgId("kiBon" + Long.toString(System.currentTimeMillis()));

		pain001DTO.setAuszahlungen(new ArrayList<>());
		zahlungsauftrag.getZahlungen().stream()
			.filter(zahlung -> zahlung.getBetragTotalZahlung().signum() == 1)
			.forEach(zahlung -> {
				InstitutionStammdaten institutionStammdaten = zahlung.getInstitutionStammdaten();
				AuszahlungDTO auszahlungDTO = new AuszahlungDTO();
				auszahlungDTO.setBetragTotalZahlung(zahlung.getBetragTotalZahlung());
				String kontoinhaber = StringUtils.isNotEmpty(institutionStammdaten.extractKontoinhaber())
					? institutionStammdaten.extractKontoinhaber() : institutionStammdaten.getInstitution().getName();

				Adresse adresseKontoinhaber = institutionStammdaten.extractAdresseKontoinhaber() != null
					? institutionStammdaten.extractAdresseKontoinhaber() : institutionStammdaten.getAdresse();
				Objects.requireNonNull(adresseKontoinhaber);
				auszahlungDTO.setZahlungsempfaegerName(kontoinhaber);
				auszahlungDTO.setZahlungsempfaegerStrasse(adresseKontoinhaber.getStrasse());
				auszahlungDTO.setZahlungsempfaegerHausnummer(adresseKontoinhaber.getHausnummer());
				auszahlungDTO.setZahlungsempfaegerPlz(adresseKontoinhaber.getPlz());
				auszahlungDTO.setZahlungsempfaegerOrt(adresseKontoinhaber.getOrt());
				auszahlungDTO.setZahlungsempfaegerLand(adresseKontoinhaber.getLand().toString());
				IBAN ibanInstitution = institutionStammdaten.extractIban();
				Objects.requireNonNull(ibanInstitution, "Keine IBAN fuer Institution " + institutionStammdaten.getInstitution().getName());
				auszahlungDTO.setZahlungsempfaegerIBAN(ibanToUnformattedString(ibanInstitution));
				auszahlungDTO.setZahlungsempfaegerBankClearingNumber(ibanInstitution.extractClearingNumberWithoutLeadingZeros());
				String monat = zahlungsauftrag.getDatumFaellig().format(DateTimeFormatter.ofPattern("MMM yyyy", locale));
				String zahlungstext = ServerMessageUtil.getMessage("ZahlungstextPainFile", locale,
					gemeindeStammdaten.getGemeinde().getName(),
					institutionStammdaten.getInstitution().getName(),
					monat);
				auszahlungDTO.setZahlungText(zahlungstext);

				// Wenn Empfänger und Auszahler dasselbe Konto sind, soll es nicht ins PAIN File. Dies ist z.B. Gemeinde-Kitas der Fall.
				if (!debitorIban.equals(auszahlungDTO.getZahlungsempfaegerIBAN())) {
					pain001DTO.getAuszahlungen().add(auszahlungDTO);
				}
			});

		return pain001DTO;
	}

	@Nonnull
	protected String ibanToUnformattedString(@Nonnull IBAN iban) {
		Objects.requireNonNull(iban);
		return EbeguUtil.removeWhiteSpaces(iban.getIban());
	}

	@Override
	public void removeAllGeneratedDokumenteFromGesuch(@Nonnull Gesuch gesuch) {
		LOGGER.info(
			"Searching GeneratedDokuments of Gesuch: {} / {}",
			gesuch.getFall().getFallNummer(),
			gesuch.getGesuchsperiode().getGesuchsperiodeString());
		Collection<GeneratedDokument> genDokFromGesuch = findGeneratedDokumentsFromGesuch(gesuch);
		for (GeneratedDokument generatedDokument : genDokFromGesuch) {
			LOGGER.info("Deleting Dokument: {}", generatedDokument.getId());
			persistence.remove(GeneratedDokument.class, generatedDokument.getId());
		}
	}

	@Override
	public void removeFreigabequittungFromGesuch(@Nonnull Gesuch gesuch) {
		Objects.requireNonNull(gesuch);

		Optional<WriteProtectedDokument> document = getFreigabequittungFromGesuch(gesuch);

		if (document.isPresent()) {
			persistence.remove(GeneratedDokument.class, document.get().getId());
			fileSaverService.remove(document.get().getFilepfad());
		}
	}

	@Nonnull
	private Optional<WriteProtectedDokument> getFreigabequittungFromGesuch(@Nonnull Gesuch gesuch) {
		final Sprache sprache = EbeguUtil.extractKorrespondenzsprache(gesuch, gemeindeService);

		final String fileNameForGeneratedDokumentTyp = DokumenteUtil.getFileNameForGeneratedDokumentTyp(
			GeneratedDokumentTyp.FREIGABEQUITTUNG, gesuch.getJahrFallAndGemeindenummer(),
			sprache.getLocale()
		);

		return getMaybeExistingGeneratedDokument(gesuch.getId(), fileNameForGeneratedDokumentTyp);
	}

	@Nonnull
	@Override
	public Collection<GeneratedDokument> findGeneratedDokumentsFromGesuch(@Nonnull Gesuch gesuch) {
		Objects.requireNonNull(gesuch);
		this.authorizer.checkReadAuthorization(gesuch);
		return criteriaQueryHelper.getEntitiesByAttribute(GeneratedDokument.class, gesuch, GeneratedDokument_.gesuch);
	}

	@Nonnull
	@Override
	public WriteProtectedDokument getAnmeldeBestaetigungDokumentAccessTokenGeneratedDokument(
		@Nonnull final Gesuch gesuch,
		@Nonnull AbstractAnmeldung abstractAnmeldung,
		@Nonnull Boolean mitTarif,
		@Nonnull Boolean forceCreation
	) throws MimeTypeParseException, MergeDocException {

		final Sprache sprache = EbeguUtil.extractKorrespondenzsprache(gesuch, gemeindeService);
		String bgNummer = abstractAnmeldung.getBGNummer();
		String fileNameForGeneratedDokumentTyp = "";
		if(mitTarif) {
			fileNameForGeneratedDokumentTyp = DokumenteUtil
				.getFileNameForGeneratedDokumentTyp(GeneratedDokumentTyp.ANMELDEBESTAETIGUNGMITTARIF, bgNummer, sprache.getLocale());
		}
		else {
			fileNameForGeneratedDokumentTyp = DokumenteUtil
				.getFileNameForGeneratedDokumentTyp(GeneratedDokumentTyp.ANMELDEBESTAETIGUNGOHNETARIF, bgNummer,
					sprache.getLocale());
		}

		WriteProtectedDokument documentIfExistsAndIsWriteProtected =
			getDocumentIfExistsAndIsWriteProtected(gesuch.getId(), fileNameForGeneratedDokumentTyp, forceCreation);
		if (documentIfExistsAndIsWriteProtected != null) {
			return documentIfExistsAndIsWriteProtected;
		}

		WriteProtectedDokument persistedDokument = null;

		if (!forceCreation && Betreuungsstatus.SCHULAMT_ANMELDUNG_UEBERNOMMEN == abstractAnmeldung.getBetreuungsstatus()
		|| Betreuungsstatus.SCHULAMT_ANMELDUNG_AUSGELOEST == abstractAnmeldung.getBetreuungsstatus()) {
			persistedDokument = findGeneratedDokument(gesuch.getId(), fileNameForGeneratedDokumentTyp);
			if (persistedDokument == null) {
				String expectedFilepath = ebeguConfiguration.getDocumentFilePath() + '/' + gesuch.getId();
				LOGGER.error(
					"Das Dokument vom Typ: {} fuer Betreuungsnummer {} konnte unter dem Pfad {} " +
						"nicht gefunden  werden obwohl es existieren muesste. Wird neu generiert!",
					 mitTarif ? GeneratedDokumentTyp.ANMELDEBESTAETIGUNGMITTARIF.name() :
						 GeneratedDokumentTyp.ANMELDEBESTAETIGUNGOHNETARIF.name(),
					bgNummer,
					expectedFilepath);
			}
		}

		if (persistedDokument == null) {
			AnmeldungTagesschule anmeldungTagesschule =
				betreuungService.findAnmeldungTagesschule(abstractAnmeldung.getId()).orElseThrow(() -> new EbeguEntityNotFoundException(
					"generateAnmeldebestaetigungDokument",
					ErrorCodeEnum.ERROR_ENTITY_NOT_FOUND,
					abstractAnmeldung.getId()));
			byte[] data = pdfService.generateAnmeldebestaetigungFuerTagesschule(
				anmeldungTagesschule, mitTarif, true, sprache.getLocale());
			persistedDokument = saveGeneratedDokumentInDB(data, mitTarif ? GeneratedDokumentTyp.ANMELDEBESTAETIGUNGMITTARIF :
					GeneratedDokumentTyp.ANMELDEBESTAETIGUNGOHNETARIF,
				gesuch, fileNameForGeneratedDokumentTyp, forceCreation);
		}
		return persistedDokument;
	}

	@Nonnull
	@Override
	public WriteProtectedDokument getRueckforderungProvVerfuegungAccessTokenGeneratedDokument(
		@Nonnull RueckforderungFormular rueckforderungFormular
	) throws MimeTypeParseException, MergeDocException {

		String fileNameForGeneratedDokumentTyp = DokumenteUtil
				.getFileNameForGeneratedDokumentTyp(GeneratedDokumentTyp.NOTRECHT_PROVISORISCHE_VERFUEGUNG,
					rueckforderungFormular.getId(), rueckforderungFormular.getKorrespondenzSprache().getLocale());

		WriteProtectedDokument documentIfExistsAndIsWriteProtected =
			getNotrechtDocumentIfExistsAndIsWriteProtected(rueckforderungFormular.getId(),
				fileNameForGeneratedDokumentTyp,
				false);
		if (documentIfExistsAndIsWriteProtected != null) {
			return documentIfExistsAndIsWriteProtected;
		}

		WriteProtectedDokument persistedDokument = null;
		byte[] data = pdfService.generateProvisorischeVerfuegungRuckforderungformular(rueckforderungFormular, true);
		persistedDokument = saveGeneratedDokumentInDB(data,  GeneratedDokumentTyp.NOTRECHT_PROVISORISCHE_VERFUEGUNG,
			rueckforderungFormular, fileNameForGeneratedDokumentTyp, true);

		return persistedDokument;
	}

	@Nonnull
	@Override
	public WriteProtectedDokument getRueckforderungDefinitiveVerfuegungAccessTokenGeneratedDokument(
		@Nonnull RueckforderungFormular rueckforderungFormular,
		@Nullable String auftragIdentifier
	) throws MimeTypeParseException, MergeDocException {

		String fileNameForGeneratedDokumentTyp = DokumenteUtil
			.getFileNameForGeneratedDokumentTyp(GeneratedDokumentTyp.NOTRECHT_DEFINITIVE_VERFUEGUNG,
				rueckforderungFormular.getId(), rueckforderungFormular.getKorrespondenzSprache().getLocale());

		WriteProtectedDokument documentIfExistsAndIsWriteProtected =
			getNotrechtDocumentIfExistsAndIsWriteProtected(rueckforderungFormular.getId(),
				fileNameForGeneratedDokumentTyp,
				false);
		if (documentIfExistsAndIsWriteProtected != null) {
			return documentIfExistsAndIsWriteProtected;
		}

		WriteProtectedDokument persistedDokument = null;
		byte[] data = pdfService.generateDefinitiveVerfuegungRuckforderungformular(rueckforderungFormular, true);
		persistedDokument = saveGeneratedDokumentInDB(data,  GeneratedDokumentTyp.NOTRECHT_DEFINITIVE_VERFUEGUNG,
			rueckforderungFormular, fileNameForGeneratedDokumentTyp, false);

		if (auftragIdentifier != null) {
			// Bitzli ein Hack: Wir wollen fuer das Zip das byte[] direkt haben, schreiben es daher (transient) hier rein
			// Dies ist aber nur notwendig, wenn wir ueberhaupt an einer Massenverfuegung sind (also einen
			// auftragIdentifier haben)
			((GeneratedNotrechtDokument)persistedDokument).setContent(data);
		}

		return persistedDokument;
	}

	@Nonnull
	@Override
	public WriteProtectedDokument generateMassenVerfuegungenAccessTokenGeneratedDocument(
		@Nonnull byte[] content,
		@Nonnull String auftragIdentifier
	) throws MimeTypeParseException {

		// Der Name des ZipFiles ist der auftragIdentifier.
		UploadFileInfo savedDokument = fileSaverService.saveZipFile(content, auftragIdentifier);

		GeneratedGeneralDokument writeProtectedDokument = new GeneratedGeneralDokument();
		writeProtectedDokument.setIdentifier(auftragIdentifier);
		writeProtectedDokument.setFilename(savedDokument.getFilename());
		writeProtectedDokument.setFilepfad(savedDokument.getPath());
		writeProtectedDokument.setFilesize(savedDokument.getSizeString());
		writeProtectedDokument.setTyp(GeneratedDokumentTyp.NOTRECHT_MASSENVERFUEGUNG);
		writeProtectedDokument.setWriteProtected(true);

		return this.saveDokument(writeProtectedDokument);
	}
}
