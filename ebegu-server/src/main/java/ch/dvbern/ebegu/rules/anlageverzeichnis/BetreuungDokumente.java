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

import java.util.HashSet;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import ch.dvbern.ebegu.entities.Betreuung;
import ch.dvbern.ebegu.entities.DokumentGrund;
import ch.dvbern.ebegu.entities.ErweiterteBetreuungContainer;
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.entities.Kind;
import ch.dvbern.ebegu.entities.KindContainer;
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

		final Set<KindContainer> kindContainers = gesuch.getKindContainers();

		if (kindContainers == null || kindContainers.isEmpty()) {
			return;
		}

		for (KindContainer kindContainer : kindContainers) {
			for (Betreuung betreuung : kindContainer.getBetreuungen()) {
				add(getDokument(betreuung, DokumentTyp.BESTAETIGUNG_AUSSERORDENTLICHER_BETREUUNGSAUFWAND), anlageVerzeichnis);
			}
		}
	}

	@Nullable
	private DokumentGrund getDokument(@Nonnull Betreuung betreuung, DokumentTyp dokumentTyp) {
		return getDokument(
			dokumentTyp,
			betreuung,
			null,
			DokumentGrundPersonType.KIND,
			betreuung.getBetreuungNummer(),
			DokumentGrundTyp.ERWEITERTE_BETREUUNG);
	}

	@SuppressWarnings("ParameterNameDiffersFromOverriddenParameter")
	@Override
	public boolean isDokumentNeeded(@Nonnull DokumentTyp dokumentTyp, @Nullable Betreuung betreuung) {
		if (betreuung != null) {
			switch (dokumentTyp) {
			case BESTAETIGUNG_AUSSERORDENTLICHER_BETREUUNGSAUFWAND:
				ErweiterteBetreuungContainer erwBetContainer = betreuung.getErweiterteBetreuungContainer();
				return erwBetContainer != null && erwBetContainer.getErweiterteBetreuungJA() != null
					&& erwBetContainer.getErweiterteBetreuungJA().getErweiterteBeduerfnisse();
			default:
				return false;
			}
		}
		return false;
	}

}
