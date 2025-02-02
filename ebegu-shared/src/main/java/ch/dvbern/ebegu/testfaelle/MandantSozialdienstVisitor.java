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
import ch.dvbern.ebegu.entities.sozialdienst.Sozialdienst;
import ch.dvbern.ebegu.enums.ErrorCodeEnum;
import ch.dvbern.ebegu.errors.EbeguEntityNotFoundException;
import ch.dvbern.ebegu.services.SozialdienstService;
import ch.dvbern.ebegu.util.mandant.MandantVisitor;
import org.jetbrains.annotations.NotNull;

public final class MandantSozialdienstVisitor implements MandantVisitor<Sozialdienst> {

	private static final String ID_BERNER_SOZIALDIENST = "f44a68f2-dda2-4bf2-936a-68e20264b620";
	private static final String ID_LUZERNER_SOZIALDIENST = "7049ec48-30ab-11ec-a86f-b89a2ae4a038";
	private static final String ID_SOLOTHURNER_SOZIALDIENST = "1b1b4208-5394-11ec-98e8-f4390979fa3e";
	private static final String ID_APPENZELL_AUSSERRHODENER_SOZIALDIENST = "1653a0c7-39ab-11ed-a63d-b05cda43de9c";
	// TODO
	private static final String ID_SCHWYZ_SOZIALDIENST = "TBD";


	private final SozialdienstService sozialdienstService;

	public MandantSozialdienstVisitor(SozialdienstService sozialdienstService) {
		this.sozialdienstService = sozialdienstService;
	}

	public Sozialdienst process(@NotNull Mandant mandant) {
		return mandant.getMandantIdentifier().accept(this);
	}

	@Override
	public Sozialdienst visitBern() {
			return sozialdienstService.findSozialdienst(ID_BERNER_SOZIALDIENST)
					.orElseThrow(() -> new EbeguEntityNotFoundException("getBernerSozialdienst", ErrorCodeEnum.ERROR_ENTITY_NOT_FOUND));
	}

	@Override
	public Sozialdienst visitLuzern() {
		return sozialdienstService.findSozialdienst(ID_LUZERNER_SOZIALDIENST)
				.orElseThrow(() -> new EbeguEntityNotFoundException("getLuzernerSozialdienst", ErrorCodeEnum.ERROR_ENTITY_NOT_FOUND));
	}

	@Override
	public Sozialdienst visitSolothurn() {
		return sozialdienstService.findSozialdienst(ID_SOLOTHURNER_SOZIALDIENST)
				.orElseThrow(() -> new EbeguEntityNotFoundException("getSolothurnerSozialdienst", ErrorCodeEnum.ERROR_ENTITY_NOT_FOUND));
	}

	@Override
	public Sozialdienst visitAppenzellAusserrhoden() {
		return sozialdienstService.findSozialdienst(ID_APPENZELL_AUSSERRHODENER_SOZIALDIENST)
				.orElseThrow(() -> new EbeguEntityNotFoundException("getAppenzellAusserrhodenerSozialdienst", ErrorCodeEnum.ERROR_ENTITY_NOT_FOUND));
	}

	@Override
	public Sozialdienst visitSchwyz() {
		return sozialdienstService.findSozialdienst(ID_SCHWYZ_SOZIALDIENST)
			.orElseThrow(() -> new EbeguEntityNotFoundException("getSchwyzerSozialdienst", ErrorCodeEnum.ERROR_ENTITY_NOT_FOUND));
	}
}
