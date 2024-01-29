/*
 * Copyright (C) 2021 DV Bern AG, Switzerland
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

package ch.dvbern.ebegu.testfaelle;

import ch.dvbern.ebegu.entities.Mandant;
import ch.dvbern.ebegu.services.InstitutionStammdatenService;
import ch.dvbern.ebegu.testfaelle.institutionStammdatenBuilder.InstitutionStammdatenBuilder;
import ch.dvbern.ebegu.testfaelle.institutionStammdatenBuilder.InstitutionStammdatenBuilderAr;
import ch.dvbern.ebegu.testfaelle.institutionStammdatenBuilder.InstitutionStammdatenBuilderBe;
import ch.dvbern.ebegu.testfaelle.institutionStammdatenBuilder.InstitutionStammdatenBuilderLu;
import ch.dvbern.ebegu.testfaelle.institutionStammdatenBuilder.InstitutionStammdatenBuilderSo;
import ch.dvbern.ebegu.testfaelle.institutionStammdatenBuilder.InstitutionStammdatenBuilderSz;
import ch.dvbern.ebegu.util.mandant.MandantVisitor;
import org.jetbrains.annotations.NotNull;

public final class InstitutionStammdatenBuilderVisitor implements MandantVisitor<InstitutionStammdatenBuilder> {

	private final InstitutionStammdatenService institutionStammdatenService;

	public InstitutionStammdatenBuilderVisitor(InstitutionStammdatenService institutionStammdatenService) {
		this.institutionStammdatenService = institutionStammdatenService;
	}

	public InstitutionStammdatenBuilder process(@NotNull Mandant mandant) {
		return mandant.getMandantIdentifier().accept(this);
	}

	@Override
	public @NotNull InstitutionStammdatenBuilder visitBern() {
		return new InstitutionStammdatenBuilderBe(institutionStammdatenService);
	}

	@Override
	public InstitutionStammdatenBuilder visitLuzern() {
		return new InstitutionStammdatenBuilderLu(institutionStammdatenService);
	}

	@Override
	public InstitutionStammdatenBuilder visitSolothurn() {
		return new InstitutionStammdatenBuilderSo(institutionStammdatenService);
	}

	@Override
	public InstitutionStammdatenBuilder visitAppenzellAusserrhoden() {
		return new InstitutionStammdatenBuilderAr(institutionStammdatenService);
	}

	// TODO
	@Override
	public InstitutionStammdatenBuilder visitSchwyz() {
		return new InstitutionStammdatenBuilderSz(institutionStammdatenService);
	}
}
