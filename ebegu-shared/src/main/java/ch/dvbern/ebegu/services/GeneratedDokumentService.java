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

import ch.dvbern.ebegu.entities.*;
import ch.dvbern.ebegu.enums.GeneratedDokumentTyp;
import ch.dvbern.ebegu.errors.MergeDocException;

import javax.activation.MimeTypeParseException;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.util.Collection;

/**
 * Service zum Verwalten von GeneratedDokumenten
 */
@SuppressWarnings("InstanceMethodNamingConvention")
public interface GeneratedDokumentService {

	/**
	 * Erstellt ein neues GeneratedDokument wenn es noch nicht existiert und sonst aktualisiert das Bestehende
	 */
	@Nonnull
	WriteProtectedDokument saveDokument(@Nonnull WriteProtectedDokument dokument);

	@Nullable
	WriteProtectedDokument findGeneratedDokument(@Nonnull String gesuchId, @Nonnull String filename);

	@Nullable
	Pain001Dokument findPain001Dokument(@Nonnull String zahlungsauftragId, @Nonnull String filename);

	@Nonnull
	WriteProtectedDokument saveGeneratedDokumentInDB(@Nonnull byte[] data, @Nonnull GeneratedDokumentTyp dokumentTyp, @Nonnull AbstractEntity entity,
		@Nonnull String fileName, boolean writeProtected) throws MimeTypeParseException;

	@Nonnull
	WriteProtectedDokument getFinSitDokumentAccessTokenGeneratedDokument(@Nonnull Gesuch gesuch, @Nonnull Boolean forceCreation)
		throws MimeTypeParseException, MergeDocException;

	@Nonnull
	WriteProtectedDokument getBegleitschreibenDokument(@Nonnull Gesuch gesuch, @Nonnull Boolean forceCreation)
		throws MimeTypeParseException, MergeDocException;

	@Nonnull
	WriteProtectedDokument getKompletteKorrespondenz(@Nonnull Gesuch gesuch) throws MimeTypeParseException, MergeDocException;

	@Nonnull
	WriteProtectedDokument getFreigabequittungAccessTokenGeneratedDokument(@Nonnull Gesuch gesuch, @Nonnull Boolean forceCreation)
		throws MimeTypeParseException, MergeDocException;

	@Nonnull
	WriteProtectedDokument getVerfuegungDokumentAccessTokenGeneratedDokument(@Nonnull Gesuch gesuch, @Nonnull Betreuung betreuung,
		@Nonnull String	manuelleBemerkungen, @Nonnull Boolean forceCreation) throws MimeTypeParseException, MergeDocException, IOException;

	@Nonnull
	WriteProtectedDokument getMahnungDokumentAccessTokenGeneratedDokument(@Nonnull Mahnung mahnung, @Nonnull Boolean createWriteProtected)
		throws MimeTypeParseException, IOException, MergeDocException;

	@Nonnull
	WriteProtectedDokument getNichteintretenDokumentAccessTokenGeneratedDokument(@Nonnull Betreuung betreuung, @Nonnull Boolean forceCreation)
		throws MimeTypeParseException, IOException, MergeDocException;

	@Nonnull
	WriteProtectedDokument getPain001DokumentAccessTokenGeneratedDokument(@Nonnull Zahlungsauftrag zahlungsauftrag)
		throws MimeTypeParseException;

	@Nonnull
	WriteProtectedDokument getInfomaDokumentAccessTokenGeneratedDokument(@Nonnull Zahlungsauftrag zahlungsauftrag)
		throws MimeTypeParseException;

	void createZahlungsFiles(@Nonnull Zahlungsauftrag zahlungsauftrag) throws MimeTypeParseException;

	void removeAllGeneratedDokumenteFromGesuch(@Nonnull Gesuch gesuch);

	@Nonnull
	Collection<GeneratedDokument> findGeneratedDokumentsFromGesuch(@Nonnull Gesuch gesuch);

	/**
	 * Löscht die Freigabequittung von einem Gesuch
	 */
	void removeFreigabequittungFromGesuch(@Nonnull Gesuch gesuch);

	@Nonnull
	WriteProtectedDokument getAnmeldeBestaetigungDokumentAccessTokenGeneratedDokument(
		@Nonnull final Gesuch gesuch,
		@Nonnull AbstractAnmeldung abstractAnmeldung,
		@Nonnull Boolean mitTarif,
		@Nonnull Boolean forceCreation
	) throws MimeTypeParseException, MergeDocException;

	@Nonnull
	WriteProtectedDokument getRueckforderungProvVerfuegungAccessTokenGeneratedDokument(RueckforderungFormular rueckforderungFormular) throws MimeTypeParseException, MergeDocException;

	@Nullable
	WriteProtectedDokument findGeneratedNotrechtDokument(@Nonnull String id, @Nonnull String filename);

	/**
	 * Generiert ein AccessToken (und das File, falls nicht vorhanden) fuer eine einzelne Verfuegung.
	 * Falls ein AuftragIdentifier vorhanden ist: Wir sind in einer Massenverfuegung und geben den
	 * Inhalt des Dokuments als transientes Feld direkt im WriteProtectedDokument zurueck, um es ins
	 * Zip File zu schreiben
	 */
	@Nonnull
	WriteProtectedDokument getRueckforderungDefinitiveVerfuegungAccessTokenGeneratedDokument(
		@Nonnull RueckforderungFormular rueckforderungFormular,
		@Nullable String auftragIdentifier) throws MimeTypeParseException, MergeDocException;

	/**
	 * Erstellt eine definitive Verfuegung fuer alle Formulare im Status 'Bereit zum Verfuegen'.
	 * Die Verfuegungen werden als Zip File zurueckgegeben, der Name des Zip Files ist auftragIdentifier.
	 */
	@Nonnull
	WriteProtectedDokument generateMassenVerfuegungenAccessTokenGeneratedDocument(
		@Nonnull byte[] content,
		@Nonnull String auftragIdentifier) throws MimeTypeParseException;
}
