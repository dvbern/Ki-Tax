/*
 * Copyright (C) 2022 DV Bern AG, Switzerland
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

package ch.dvbern.ebegu.pdfgenerator;

import javax.annotation.Nonnull;
import javax.validation.constraints.NotNull;

import ch.dvbern.ebegu.entities.Betreuung;
import ch.dvbern.ebegu.entities.GemeindeStammdaten;
import ch.dvbern.ebegu.entities.Mandant;
import ch.dvbern.ebegu.enums.BetreuungspensumAnzeigeTyp;
import ch.dvbern.ebegu.pdfgenerator.AbstractVerfuegungPdfGenerator.Art;
import ch.dvbern.ebegu.util.mandant.MandantVisitor;

public class VerfuegungPdfGeneratorVisitor implements MandantVisitor<AbstractVerfuegungPdfGenerator> {

	private final Betreuung betreuung;
	private final GemeindeStammdaten stammdaten;
	private final Art art;
	private final boolean kontingentierungEnabledAndEntwurf;
	private final boolean stadtBernAsivConfigured;
	private final boolean isFKJVTexte;
	private final BetreuungspensumAnzeigeTyp betreuungspensumAnzeigeTyp;

	public VerfuegungPdfGeneratorVisitor(
		@Nonnull Betreuung betreuung,
		@Nonnull GemeindeStammdaten stammdaten,
		@Nonnull Art art,
		boolean kontingentierungEnabledAndEntwurf,
		boolean stadtBernAsivConfigured,
		boolean isFKJVTexte,
		BetreuungspensumAnzeigeTyp betreuungspensumAnzeigeTyp
	) {
		this.betreuung = betreuung;
		this.stammdaten = stammdaten;
		this.art = art;
		this.kontingentierungEnabledAndEntwurf = kontingentierungEnabledAndEntwurf;
		this.stadtBernAsivConfigured = stadtBernAsivConfigured;
		this.isFKJVTexte = isFKJVTexte;
		this.betreuungspensumAnzeigeTyp = betreuungspensumAnzeigeTyp;
	}

	public AbstractVerfuegungPdfGenerator getVerfuegungPdfGeneratorForMandant(@NotNull Mandant mandant) {
		return mandant.getMandantIdentifier().accept(this);
	}

	@Override
	public AbstractVerfuegungPdfGenerator visitBern() {
		return new VerfuegungPdfGeneratorBern(betreuung, stammdaten, art, kontingentierungEnabledAndEntwurf, stadtBernAsivConfigured, isFKJVTexte, betreuungspensumAnzeigeTyp);
	}

	@Override
	public AbstractVerfuegungPdfGenerator visitLuzern() {
		return new VerfuegungPdfGeneratorLuzern(betreuung, stammdaten, art, kontingentierungEnabledAndEntwurf, stadtBernAsivConfigured, isFKJVTexte, betreuungspensumAnzeigeTyp);
	}

	@Override
	public AbstractVerfuegungPdfGenerator visitSolothurn() {
		return new VerfuegungPdfGeneratorSolothurn(betreuung, stammdaten, art, kontingentierungEnabledAndEntwurf, stadtBernAsivConfigured, isFKJVTexte, betreuungspensumAnzeigeTyp);
	}

	@Override
	public AbstractVerfuegungPdfGenerator visitAppenzellAusserrhoden() {
		return new VerfuegungPdfGeneratorAppenzell(betreuung, stammdaten, art, kontingentierungEnabledAndEntwurf, stadtBernAsivConfigured, isFKJVTexte, betreuungspensumAnzeigeTyp);
	}

	@Override
	public AbstractVerfuegungPdfGenerator visitSchwyz() {
		return new VerfuegungPdfGeneratorSolothurn(betreuung, stammdaten, art, kontingentierungEnabledAndEntwurf, stadtBernAsivConfigured, isFKJVTexte, betreuungspensumAnzeigeTyp);
	}
}
