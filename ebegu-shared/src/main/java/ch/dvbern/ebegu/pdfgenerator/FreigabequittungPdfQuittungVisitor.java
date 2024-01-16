/*
 * Copyright (C) 2023 DV Bern AG, Switzerland
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package ch.dvbern.ebegu.pdfgenerator;

import java.util.List;

import javax.annotation.Nonnull;
import javax.validation.constraints.NotNull;

import ch.dvbern.ebegu.entities.DokumentGrund;
import ch.dvbern.ebegu.entities.GemeindeStammdaten;
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.entities.Mandant;
import ch.dvbern.ebegu.util.mandant.MandantVisitor;

public class FreigabequittungPdfQuittungVisitor implements MandantVisitor<AbstractFreigabequittungPdfGenerator> {

	private final Gesuch gesuch;
	private final GemeindeStammdaten stammdaten;
	private final List<DokumentGrund> benoetigteUnterlagen;

	public FreigabequittungPdfQuittungVisitor(
		@Nonnull Gesuch gesuch,
		@Nonnull GemeindeStammdaten stammdaten,
		@Nonnull List<DokumentGrund> benoetigteUnterlagen) {
		this.gesuch = gesuch;
		this.stammdaten = stammdaten;
		this.benoetigteUnterlagen = benoetigteUnterlagen;
	}

	public AbstractFreigabequittungPdfGenerator getFreigabequittungPdfGeneratorForMandant(@NotNull Mandant mandant) {
		return mandant.getMandantIdentifier().accept(this);
	}

	@Override
	public AbstractFreigabequittungPdfGenerator visitBern() {
		return new FreigabequittungPdfGeneratorBern(gesuch, stammdaten, benoetigteUnterlagen);
	}

	@Override
	public AbstractFreigabequittungPdfGenerator visitLuzern() {
		return new FreigabequittungPdfGeneratorLuzern(gesuch, stammdaten, benoetigteUnterlagen);
	}

	@Override
	public AbstractFreigabequittungPdfGenerator visitSolothurn() {
		return new FreigabequittungPdfGeneratorBern(gesuch, stammdaten, benoetigteUnterlagen);
	}

	@Override
	public AbstractFreigabequittungPdfGenerator visitAppenzellAusserrhoden() {
		return new FreigabequittungPdfGeneratorAppenzell(gesuch, stammdaten, benoetigteUnterlagen);
	}

	@Override
	public AbstractFreigabequittungPdfGenerator visitSchwyz() {
		return new FreigabequittungPdfGeneratorBern(gesuch, stammdaten, benoetigteUnterlagen);
	}
}
