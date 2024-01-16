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

import ch.dvbern.ebegu.entities.GemeindeStammdaten;
import ch.dvbern.ebegu.entities.Mahnung;
import ch.dvbern.ebegu.entities.Mandant;
import ch.dvbern.ebegu.util.mandant.MandantVisitor;

import javax.validation.constraints.NotNull;

public class ErsteMahnungPdfGeneratorVisitor implements MandantVisitor<AbstractErsteMahnungPdfGenerator> {

	private final GemeindeStammdaten stammdaten;
	private final Mahnung mahnung;

	public ErsteMahnungPdfGeneratorVisitor(Mahnung mahnung, GemeindeStammdaten stammdaten) {
		this.stammdaten = stammdaten;
		this.mahnung = mahnung;
	}

	public AbstractErsteMahnungPdfGenerator getErsteMahnungPdfGeneratorForMandant(@NotNull Mandant mandant) {
		return mandant.getMandantIdentifier().accept(this);
	}

	@Override
	public AbstractErsteMahnungPdfGenerator visitBern() {
		return new ErsteMahnungPdfGenerator(mahnung, stammdaten);
	}

	@Override
	public AbstractErsteMahnungPdfGenerator visitLuzern() {
		return new ErsteMahnungPdfGenerator(mahnung, stammdaten);
	}

	@Override
	public AbstractErsteMahnungPdfGenerator visitSolothurn() {
		return new ErsteMahnungPdfGenerator(mahnung, stammdaten);
	}

	@Override
	public AbstractErsteMahnungPdfGenerator visitAppenzellAusserrhoden() {
		return new ErsteMahnungPdfGeneratorAppenzell(mahnung, stammdaten);
	}

	@Override
	public AbstractErsteMahnungPdfGenerator visitSchwyz() {
		return new ErsteMahnungPdfGenerator(mahnung, stammdaten);
	}
}
