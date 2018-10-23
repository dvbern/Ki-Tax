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

package ch.dvbern.ebegu.rules.anlageverzeichnis;

import java.util.List;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import ch.dvbern.ebegu.entities.Betreuung;
import ch.dvbern.ebegu.entities.DokumentGrund;
import ch.dvbern.ebegu.entities.ErweiterteBetreuungContainer;
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.enums.DokumentGrundPersonType;
import ch.dvbern.ebegu.enums.DokumentGrundTyp;
import ch.dvbern.ebegu.enums.DokumentTyp;

/**
 * Dokumente für Betreuungen:
 * <p>
 * Bestätigung über den ausserordentlichen Betreuungsaufwand:
 * Notwendig, wenn die Frage "Hat Ihr Kind besondere Bedürfnisse und einen darin
 * begründeten ausserordentlichen Betreuungsaufwand?" mit Ja beantwortet wird.
 **/
public class BetreuungDokumente extends AbstractDokumente<Betreuung, Object> {

	@Override
	public void getAllDokumente(@Nonnull Gesuch gesuch, @Nonnull Set<DokumentGrund> anlageVerzeichnis) {

		final List<Betreuung> allBetreuungen = gesuch.extractAllBetreuungen();

		if (allBetreuungen.isEmpty()) {
			return;
		}

		allBetreuungen.forEach(betreuung ->
			add(getDokumentBetreuungsaufwand(betreuung), anlageVerzeichnis)
		);
	}

	@Nullable
	private DokumentGrund getDokumentBetreuungsaufwand(@Nonnull Betreuung betreuung) {
		return getDokument(
			DokumentTyp.BESTAETIGUNG_AUSSERORDENTLICHER_BETREUUNGSAUFWAND,
			betreuung,
			null,
			DokumentGrundPersonType.KIND,
			betreuung.getBetreuungNummer(),
			DokumentGrundTyp.ERWEITERTE_BETREUUNG);
	}

	@SuppressWarnings("ParameterNameDiffersFromOverriddenParameter")
	@Override
	public boolean isDokumentNeeded(@Nonnull DokumentTyp dokumentTyp, @Nullable Betreuung betreuung) {
		if (betreuung == null) {
			return false;
		}

		switch (dokumentTyp) {
		case BESTAETIGUNG_AUSSERORDENTLICHER_BETREUUNGSAUFWAND:
			ErweiterteBetreuungContainer erwBetContainer = betreuung.getErweiterteBetreuungContainer();
			return erwBetContainer.getErweiterteBetreuungJA() != null
				&& erwBetContainer.getErweiterteBetreuungJA().getErweiterteBeduerfnisse();
		default:
			return false;
		}
	}

}
