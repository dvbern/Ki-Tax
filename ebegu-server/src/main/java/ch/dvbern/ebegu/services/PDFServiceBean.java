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
import java.io.InputStream;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.security.RolesAllowed;
import javax.ejb.Local;
import javax.ejb.Stateless;
import javax.inject.Inject;

import ch.dvbern.ebegu.entities.Betreuung;
import ch.dvbern.ebegu.entities.DokumentGrund;
import ch.dvbern.ebegu.entities.GemeindeStammdaten;
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.entities.Mahnung;
import ch.dvbern.ebegu.entities.Verfuegung;
import ch.dvbern.ebegu.enums.BetreuungsangebotTyp;
import ch.dvbern.ebegu.enums.Betreuungsstatus;
import ch.dvbern.ebegu.enums.EbeguVorlageKey;
import ch.dvbern.ebegu.enums.ErrorCodeEnum;
import ch.dvbern.ebegu.errors.EbeguEntityNotFoundException;
import ch.dvbern.ebegu.errors.MergeDocException;
import ch.dvbern.ebegu.pdfgenerator.BegleitschreibenPdfGenerator;
import ch.dvbern.ebegu.pdfgenerator.ErsteMahnungPdfGenerator;
import ch.dvbern.ebegu.pdfgenerator.FinanzielleSituationPdfGenerator;
import ch.dvbern.ebegu.pdfgenerator.FreigabequittungPdfGenerator;
import ch.dvbern.ebegu.pdfgenerator.KibonPdfGenerator;
import ch.dvbern.ebegu.pdfgenerator.MahnungPdfGenerator;
import ch.dvbern.ebegu.pdfgenerator.ZweiteMahnungPdfGenerator;
import ch.dvbern.ebegu.rules.anlageverzeichnis.DokumentenverzeichnisEvaluator;
import ch.dvbern.ebegu.types.DateRange;
import ch.dvbern.ebegu.util.DokumenteUtil;
import ch.dvbern.ebegu.util.EbeguUtil;
import ch.dvbern.ebegu.vorlagen.GeneratePDFDocumentHelper;
import ch.dvbern.ebegu.vorlagen.nichteintreten.NichteintretenPrintImpl;
import ch.dvbern.ebegu.vorlagen.nichteintreten.NichteintretenPrintMergeSource;
import ch.dvbern.ebegu.vorlagen.verfuegung.VerfuegungPrintImpl;
import ch.dvbern.ebegu.vorlagen.verfuegung.VerfuegungPrintMergeSource;
import ch.dvbern.lib.invoicegenerator.errors.InvoiceGeneratorException;
import com.google.common.io.ByteStreams;

import static ch.dvbern.ebegu.enums.UserRoleName.ADMIN_BG;
import static ch.dvbern.ebegu.enums.UserRoleName.ADMIN_GEMEINDE;
import static ch.dvbern.ebegu.enums.UserRoleName.ADMIN_MANDANT;
import static ch.dvbern.ebegu.enums.UserRoleName.ADMIN_TS;
import static ch.dvbern.ebegu.enums.UserRoleName.GESUCHSTELLER;
import static ch.dvbern.ebegu.enums.UserRoleName.REVISOR;
import static ch.dvbern.ebegu.enums.UserRoleName.SACHBEARBEITER_BG;
import static ch.dvbern.ebegu.enums.UserRoleName.SACHBEARBEITER_GEMEINDE;
import static ch.dvbern.ebegu.enums.UserRoleName.SACHBEARBEITER_MANDANT;
import static ch.dvbern.ebegu.enums.UserRoleName.SACHBEARBEITER_TS;
import static ch.dvbern.ebegu.enums.UserRoleName.SUPER_ADMIN;

@SuppressWarnings("UnstableApiUsage")
@Stateless
@Local(PDFService.class)
public class PDFServiceBean extends AbstractPrintService implements PDFService {

	private static final Objects[] OBJECTARRAY = {};
	public static final byte[] BYTES = new byte[0];

	@Inject
	private DokumentGrundService dokumentGrundService;

	@Inject
	private DokumentenverzeichnisEvaluator dokumentenverzeichnisEvaluator;

	@Inject
	private GemeindeService gemeindeService;

	@Inject
	private Authorizer authorizer;

	@Nonnull
	@Override
	@RolesAllowed({ ADMIN_BG, SUPER_ADMIN, SACHBEARBEITER_BG, ADMIN_GEMEINDE, SACHBEARBEITER_GEMEINDE, SACHBEARBEITER_TS, ADMIN_TS,
		REVISOR, ADMIN_MANDANT, SACHBEARBEITER_MANDANT})
	public byte[] generateNichteintreten(Betreuung betreuung, boolean writeProtected) throws
		MergeDocException {

		EbeguVorlageKey vorlageKey;

		BetreuungsangebotTyp angebotTyp = betreuung.getBetreuungsangebotTyp();

		if (angebotTyp == BetreuungsangebotTyp.KITA || angebotTyp == BetreuungsangebotTyp.TAGESFAMILIEN) {
			vorlageKey = EbeguVorlageKey.VORLAGE_NICHT_EINTRETENSVERFUEGUNG;
		} else if (angebotTyp == BetreuungsangebotTyp.TAGESSCHULE) {
			vorlageKey = EbeguVorlageKey.VORLAGE_INFOSCHREIBEN_MAXIMALTARIF;
		} else {
			throw new MergeDocException("generateNichteintreten()",
				"Unexpected Betreuung Type", null, OBJECTARRAY);
		}

		try {
			Objects.requireNonNull(betreuung, "Das Argument 'betreuung' darf nicht leer sein");
			final DateRange gueltigkeit = betreuung.extractGesuch().getGesuchsperiode().getGueltigkeit();
			InputStream is = getVorlageStream(gueltigkeit.getGueltigAb(), gueltigkeit.getGueltigBis(), vorlageKey);
			Objects.requireNonNull(is, "Vorlage '" + vorlageKey.name() + "' nicht gefunden");
			byte[] bytes = new GeneratePDFDocumentHelper().generatePDFDocument(
				ByteStreams.toByteArray(is), new NichteintretenPrintMergeSource(new NichteintretenPrintImpl(betreuung)),
				writeProtected);
			is.close();
			return bytes;
		} catch (IOException e) {
			throw new MergeDocException("generateNichteintreten()",
				"Bei der Generierung der Nichteintreten ist ein Fehler aufgetreten", e, OBJECTARRAY);
		}
	}

	@Nonnull
	@Override
	@RolesAllowed({ ADMIN_BG, SUPER_ADMIN, SACHBEARBEITER_BG, ADMIN_GEMEINDE, SACHBEARBEITER_GEMEINDE, SACHBEARBEITER_TS, ADMIN_TS,
		REVISOR, ADMIN_MANDANT, SACHBEARBEITER_MANDANT})
	public byte[] generateMahnung(Mahnung mahnung, Optional<Mahnung> vorgaengerMahnungOptional, boolean writeProtected) throws MergeDocException {
		Objects.requireNonNull(mahnung, "Das Argument 'mahnung' darf nicht leer sein");
		GemeindeStammdaten stammdaten = getGemeindeStammdaten(mahnung.getGesuch());

		MahnungPdfGenerator pdfGenerator;
		switch (mahnung.getMahnungTyp()) {
		case ERSTE_MAHNUNG:
			pdfGenerator = new ErsteMahnungPdfGenerator(mahnung, stammdaten, !writeProtected);
			break;
		case ZWEITE_MAHNUNG:
			Mahnung vorgaengerMahnung = vorgaengerMahnungOptional.orElseThrow(() -> new EbeguEntityNotFoundException("",
				ErrorCodeEnum.ERROR_ENTITY_NOT_FOUND, mahnung.getId()));
			pdfGenerator = new ZweiteMahnungPdfGenerator(mahnung, vorgaengerMahnung, stammdaten, !writeProtected);
			break;
		default:
			throw new MergeDocException("generateMahnung()", "Unexpected Mahnung Type", null, OBJECTARRAY);
		}
		return generateDokument(pdfGenerator);
	}

	@Override
	@Nonnull
	@RolesAllowed({ ADMIN_BG, SUPER_ADMIN, SACHBEARBEITER_BG, ADMIN_GEMEINDE, SACHBEARBEITER_GEMEINDE, GESUCHSTELLER, SACHBEARBEITER_TS, ADMIN_TS,
		REVISOR, ADMIN_MANDANT, SACHBEARBEITER_MANDANT})
	public byte[] generateFreigabequittung(@Nonnull Gesuch gesuch, boolean writeProtected) throws MergeDocException {
		Objects.requireNonNull(gesuch, "Das Argument 'gesuch' darf nicht leer sein");

		GemeindeStammdaten stammdaten = getGemeindeStammdaten(gesuch);
		final List<DokumentGrund> benoetigteUnterlagen = calculateListOfDokumentGrunds(gesuch);

		FreigabequittungPdfGenerator pdfGenerator = new FreigabequittungPdfGenerator(gesuch, stammdaten, !writeProtected, benoetigteUnterlagen);
		return generateDokument(pdfGenerator);
	}

	@Override
	@Nonnull
	@RolesAllowed({ ADMIN_BG, SUPER_ADMIN, SACHBEARBEITER_BG, ADMIN_GEMEINDE, SACHBEARBEITER_GEMEINDE, SACHBEARBEITER_TS, ADMIN_TS,
		REVISOR, ADMIN_MANDANT, SACHBEARBEITER_MANDANT})
	public byte[] generateBegleitschreiben(@Nonnull Gesuch gesuch, boolean writeProtected) throws MergeDocException {
		Objects.requireNonNull(gesuch, "Das Argument 'gesuch' darf nicht leer sein");
		authorizer.checkReadAuthorization(gesuch);

		GemeindeStammdaten stammdaten = getGemeindeStammdaten(gesuch);

		BegleitschreibenPdfGenerator pdfGenerator = new BegleitschreibenPdfGenerator(gesuch, stammdaten, !writeProtected);
		return generateDokument(pdfGenerator);
	}

	@Nonnull
	@Override
	@RolesAllowed({ ADMIN_BG, SUPER_ADMIN, SACHBEARBEITER_BG, ADMIN_GEMEINDE, SACHBEARBEITER_GEMEINDE, ADMIN_TS, SACHBEARBEITER_TS, GESUCHSTELLER,
		REVISOR, ADMIN_MANDANT, SACHBEARBEITER_MANDANT})
	public byte[] generateFinanzielleSituation(@Nonnull Gesuch gesuch, @Nullable Verfuegung famGroessenVerfuegung,
		boolean writeProtected) throws MergeDocException {

		Objects.requireNonNull(gesuch, "Das Argument 'gesuch' darf nicht leer sein");

		if (EbeguUtil.isFinanzielleSituationRequired(gesuch)) {

			if (!gesuch.hasOnlyBetreuungenOfSchulamt()) {
				// Bei nur Schulamt prüfen wir die Berechtigung nicht, damit das JA solche Gesuche schliessen kann. Der UseCase ist, dass zuerst ein zweites
				// Angebot vorhanden war, dieses aber durch das JA gelöscht wurde.		authorizer.checkReadAuthorizationFinSit(gesuch);
				authorizer.checkReadAuthorizationFinSit(gesuch);
			}

			GemeindeStammdaten stammdaten = getGemeindeStammdaten(gesuch);
			FinanzielleSituationPdfGenerator pdfGenerator = new FinanzielleSituationPdfGenerator(gesuch, stammdaten, !writeProtected);
			return generateDokument(pdfGenerator);
		}
		return BYTES;
	}

	@Nonnull
	@Override
	@RolesAllowed({ ADMIN_BG, SUPER_ADMIN, SACHBEARBEITER_BG, ADMIN_GEMEINDE, SACHBEARBEITER_GEMEINDE, SACHBEARBEITER_TS, ADMIN_TS,
		REVISOR, ADMIN_MANDANT, SACHBEARBEITER_MANDANT})
	public byte[] generateVerfuegungForBetreuung(Betreuung betreuung,
		@Nullable LocalDate letzteVerfuegungDatum, boolean writeProtected) throws MergeDocException {

		final DateRange gueltigkeit = betreuung.extractGesuchsperiode().getGueltigkeit();
		EbeguVorlageKey vorlageFromBetreuungsangebottyp = getVorlageFromBetreuungsangebottyp(betreuung);
		InputStream is = getVorlageStream(gueltigkeit.getGueltigAb(), gueltigkeit.getGueltigBis(), vorlageFromBetreuungsangebottyp);
		Objects.requireNonNull(is, "Vorlage fuer die Verfuegung nicht gefunden");
		authorizer.checkReadAuthorization(betreuung);
		try {
			byte[] bytes = new GeneratePDFDocumentHelper().generatePDFDocument(
				ByteStreams.toByteArray(is), new VerfuegungPrintMergeSource(new VerfuegungPrintImpl(betreuung, letzteVerfuegungDatum)),
				writeProtected);
			is.close();
			return bytes;
		} catch (IOException e) {
			throw new MergeDocException("printVerfuegungen()",
				"Bei der Generierung der Verfuegungsmustervorlage ist ein Fehler aufgetreten", e, OBJECTARRAY);
		}
	}

	@Nonnull
	private EbeguVorlageKey getVorlageFromBetreuungsangebottyp(final Betreuung betreuung) {
		BetreuungsangebotTyp betreuungsangebotTyp = betreuung.getBetreuungsangebotTyp();
		Objects.requireNonNull(betreuungsangebotTyp);
		if (Betreuungsstatus.NICHT_EINGETRETEN == betreuung.getBetreuungsstatus()) {
			if (betreuungsangebotTyp.isAngebotJugendamtKleinkind()) {
				return EbeguVorlageKey.VORLAGE_NICHT_EINTRETENSVERFUEGUNG;
			}
			return EbeguVorlageKey.VORLAGE_INFOSCHREIBEN_MAXIMALTARIF;
		}
		switch (betreuungsangebotTyp) {
		case TAGESFAMILIEN:
			return EbeguVorlageKey.VORLAGE_VERFUEGUNG_TAGESFAMILIEN;
		case KITA:
		default:
			return EbeguVorlageKey.VORLAGE_VERFUEGUNG_KITA;
		}
	}

	/**
	 * In dieser Methode werden alle DokumentGrunds vom Gesuch einer Liste hinzugefuegt. Die die bereits existieren und die
	 * die noch nicht hochgeladen wurden
	 */
	@Nonnull
	private List<DokumentGrund> calculateListOfDokumentGrunds(@Nonnull Gesuch gesuch) {
		List<DokumentGrund> dokumentGrundsMerged = new ArrayList<>(DokumenteUtil
			.mergeNeededAndPersisted(dokumentenverzeichnisEvaluator.calculate(gesuch),
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
	private byte[] generateDokument(@Nonnull KibonPdfGenerator pdfGenerator) throws MergeDocException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try {
			pdfGenerator.generate(baos);
			return baos.toByteArray();
		} catch (InvoiceGeneratorException e) {
			throw new MergeDocException("generateDokument()",
				"Bei der Generierung des Dokuments ist ein Fehler aufgetreten", e, OBJECTARRAY);
		}
	}
}
