/*
 * Copyright (C) 2024 DV Bern AG, Switzerland
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package ch.dvbern.ebegu.pdfgenerator.verfuegung;

import javax.annotation.Nonnull;
import javax.validation.constraints.NotNull;

import ch.dvbern.ebegu.entities.Betreuung;
import ch.dvbern.ebegu.entities.GemeindeStammdaten;
import ch.dvbern.ebegu.entities.Mandant;
import ch.dvbern.ebegu.enums.betreuung.BetreuungspensumAnzeigeTyp;
import ch.dvbern.ebegu.pdfgenerator.verfuegung.AbstractVerfuegungPdfGenerator.Art;
import ch.dvbern.ebegu.util.mandant.MandantVisitor;

public class VerfuegungPdfGeneratorVisitor implements MandantVisitor<AbstractVerfuegungPdfGenerator> {

	private final Betreuung betreuung;
	private final GemeindeStammdaten stammdaten;
	private final Art art;
	private final VerfuegungPdfGeneratorKonfiguration verfuegungPdfGeneratorKonfiguration;

	public VerfuegungPdfGeneratorVisitor(
		@Nonnull Betreuung betreuung,
		@Nonnull GemeindeStammdaten stammdaten,
		@Nonnull Art art,
		VerfuegungPdfGeneratorKonfiguration verfuegungPdfGeneratorKonfiguration
	) {
		this.betreuung = betreuung;
		this.stammdaten = stammdaten;
		this.art = art;
		this.verfuegungPdfGeneratorKonfiguration = verfuegungPdfGeneratorKonfiguration;
	}

	public AbstractVerfuegungPdfGenerator getVerfuegungPdfGeneratorForMandant(@NotNull Mandant mandant) {
		return mandant.getMandantIdentifier().accept(this);
	}

	@Override
	public AbstractVerfuegungPdfGenerator visitBern() {
		return new VerfuegungPdfGeneratorBern(betreuung, stammdaten, art, verfuegungPdfGeneratorKonfiguration);
	}

	@Override
	public AbstractVerfuegungPdfGenerator visitLuzern() {
		return new VerfuegungPdfGeneratorLuzern(betreuung, stammdaten, art, verfuegungPdfGeneratorKonfiguration);
	}

	@Override
	public AbstractVerfuegungPdfGenerator visitSolothurn() {
		return new VerfuegungPdfGeneratorSolothurn(betreuung, stammdaten, art, verfuegungPdfGeneratorKonfiguration);
	}

	@Override
	public AbstractVerfuegungPdfGenerator visitAppenzellAusserrhoden() {
		return new VerfuegungPdfGeneratorAppenzell(betreuung, stammdaten, art, verfuegungPdfGeneratorKonfiguration);
	}

	@Override
	public AbstractVerfuegungPdfGenerator visitSchwyz() {
		return new VerfuegungPdfGeneratorSchwyz(betreuung, stammdaten, art, verfuegungPdfGeneratorKonfiguration);
	}
}
