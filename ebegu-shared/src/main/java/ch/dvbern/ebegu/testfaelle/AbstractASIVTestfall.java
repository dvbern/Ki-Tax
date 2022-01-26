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

package ch.dvbern.ebegu.testfaelle;

import java.time.LocalDate;

import javax.annotation.Nonnull;

import ch.dvbern.ebegu.entities.Familiensituation;
import ch.dvbern.ebegu.entities.FamiliensituationContainer;
import ch.dvbern.ebegu.entities.Gemeinde;
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.entities.Gesuchsperiode;
import ch.dvbern.ebegu.enums.AntragCopyType;
import ch.dvbern.ebegu.enums.EnumFamilienstatus;

/**
 * Superklasse für ASIV-Testfaelle
 */
public abstract class AbstractASIVTestfall extends AbstractTestfall {

	protected AbstractASIVTestfall(
			@Nonnull Gesuchsperiode gesuchsperiode,
			boolean betreuungenBestaetigt,
			@Nonnull Gemeinde gemeinde,
			InstitutionStammdatenBuilder institutionStammdatenBuilder) {
		super(gesuchsperiode, betreuungenBestaetigt, gemeinde, institutionStammdatenBuilder);
	}

	protected AbstractASIVTestfall(
			@Nonnull Gesuchsperiode gesuchsperiode,
			boolean betreuungenBestaetigt,
			InstitutionStammdatenBuilder institutionStammdatenBuilder) {
		super(gesuchsperiode, betreuungenBestaetigt, institutionStammdatenBuilder);
	}

	public abstract Gesuch createMutation(Gesuch erstgesuch);

	protected Gesuch createAlleinerziehend(Gesuch gesuch, LocalDate ereignisdatum) {
		// Familiensituation
		Familiensituation familiensituation = new Familiensituation();
		familiensituation.setFamilienstatus(EnumFamilienstatus.ALLEINERZIEHEND);
		FamiliensituationContainer familiensituationContainer = new FamiliensituationContainer();
		familiensituationContainer.setFamiliensituationJA(familiensituation);
		familiensituation.setAenderungPer(ereignisdatum);

		Familiensituation familiensituationErstgesuch = new Familiensituation();
		familiensituationErstgesuch.setFamilienstatus(EnumFamilienstatus.VERHEIRATET);
		familiensituationErstgesuch.setGemeinsameSteuererklaerung(Boolean.TRUE);
		familiensituationContainer.setFamiliensituationErstgesuch(familiensituationErstgesuch);

		gesuch.setFamiliensituationContainer(familiensituationContainer);
		return gesuch;
	}

	protected Gesuch createVerheiratet(Gesuch gesuch, LocalDate ereignisdatum) {
		// Familiensituation
		assert gesuch.getFamiliensituationContainer() != null;
		assert gesuch.getFamiliensituationContainer().getFamiliensituationJA() != null;

		Familiensituation familiensituation = gesuch.getFamiliensituationContainer()
				.getFamiliensituationJA()
				.copyFamiliensituation(new Familiensituation(), AntragCopyType.MUTATION);
		familiensituation.setSozialhilfeBezueger(gesuch.getFamiliensituationContainer()
				.getFamiliensituationJA()
				.getSozialhilfeBezueger());
		familiensituation.setFamilienstatus(EnumFamilienstatus.VERHEIRATET);
		familiensituation.setGemeinsameSteuererklaerung(gesuch.getFamiliensituationContainer()
				.getFamiliensituationJA()
				.getGemeinsameSteuererklaerung());
		FamiliensituationContainer familiensituationContainer = new FamiliensituationContainer();
		familiensituationContainer.setFamiliensituationJA(familiensituation);
		familiensituation.setAenderungPer(ereignisdatum);

		familiensituationContainer.setFamiliensituationErstgesuch(gesuch.extractFamiliensituationErstgesuch());

		gesuch.setFamiliensituationContainer(familiensituationContainer);
		return gesuch;
	}
}
