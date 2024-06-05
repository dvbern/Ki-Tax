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

package ch.dvbern.ebegu.rules.mutationsmerger;

import java.util.Locale;

import ch.dvbern.ebegu.util.mandant.MandantIdentifier;
import ch.dvbern.ebegu.util.mandant.MandantVisitor;
import com.sun.istack.NotNull;

public class MutationsMergerAnspruchHandlerVisitor implements MandantVisitor<AbstractMutationsMergerAnspruchHandler> {

	private final Locale locale;

	public MutationsMergerAnspruchHandlerVisitor(Locale locale) {
		this.locale = locale;
	}

	public AbstractMutationsMergerAnspruchHandler getAnspruchHandler(@NotNull MandantIdentifier mandant) {
		return mandant.accept(this);
	}
	@Override
	public AbstractMutationsMergerAnspruchHandler visitBern() {
		return new MutationsMergerAnspruchHandlerBern(locale);
	}

	@Override
	public AbstractMutationsMergerAnspruchHandler visitLuzern() {
		return new MutationsMergerAnspruchHandlerLuzern(locale);
	}

	@Override
	public AbstractMutationsMergerAnspruchHandler visitSolothurn() {
		return visitBern();
	}

	@Override
	public AbstractMutationsMergerAnspruchHandler visitAppenzellAusserrhoden() {
		return visitBern();
	}

	@Override
	public AbstractMutationsMergerAnspruchHandler visitSchwyz() {
		return this.visitLuzern();
	}
}
