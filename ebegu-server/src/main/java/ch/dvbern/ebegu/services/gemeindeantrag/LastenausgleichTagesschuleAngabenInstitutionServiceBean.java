/*
 * Copyright (C) 2020 DV Bern AG, Switzerland
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

package ch.dvbern.ebegu.services.gemeindeantrag;

import java.util.Collection;
import java.util.Objects;

import javax.annotation.Nonnull;
import javax.ejb.Local;
import javax.ejb.Stateless;
import javax.inject.Inject;

import ch.dvbern.ebegu.entities.InstitutionStammdaten;
import ch.dvbern.ebegu.entities.gemeindeantrag.LastenausgleichTagesschuleAngabenGemeindeContainer;
import ch.dvbern.ebegu.entities.gemeindeantrag.LastenausgleichTagesschuleAngabenInstitutionContainer;
import ch.dvbern.ebegu.enums.gemeindeantrag.LastenausgleichTagesschuleAngabenInstitutionStatus;
import ch.dvbern.ebegu.services.AbstractBaseService;
import ch.dvbern.ebegu.services.Authorizer;
import ch.dvbern.ebegu.services.InstitutionStammdatenService;
import ch.dvbern.lib.cdipersistence.Persistence;
import com.google.common.base.Preconditions;

/**
 * Service fuer den Lastenausgleich der Tagesschulen, Formulare der Institutionen
 */
@Stateless
@Local(LastenausgleichTagesschuleAngabenInstitutionService.class)
public class LastenausgleichTagesschuleAngabenInstitutionServiceBean extends AbstractBaseService implements LastenausgleichTagesschuleAngabenInstitutionService {

	@Inject
	private Persistence persistence;

	@Inject
	private Authorizer authorizer;

	@Inject
	private InstitutionStammdatenService institutionStammdatenService;

	@Override
	public void createLastenausgleichTagesschuleInstitution(
		@Nonnull LastenausgleichTagesschuleAngabenGemeindeContainer gemeindeContainer
	) {
		Objects.requireNonNull(gemeindeContainer);

		final Collection<InstitutionStammdaten> institutionStammdatenList =
			institutionStammdatenService.getAllTagesschulenForGesuchsperiodeAndGemeinde(
				gemeindeContainer.getGesuchsperiode(),
				gemeindeContainer.getGemeinde());
		for (InstitutionStammdaten institutionStammdaten : institutionStammdatenList) {
			LastenausgleichTagesschuleAngabenInstitutionContainer institutionContainer = new LastenausgleichTagesschuleAngabenInstitutionContainer();
			institutionContainer.setInstitution(institutionStammdaten.getInstitution());
			institutionContainer.setStatus(LastenausgleichTagesschuleAngabenInstitutionStatus.OFFEN);
			institutionContainer.setAngabenKorrektur(null);		// Wird erst mit den Daten initialisiert, da alles zwingend
			institutionContainer.setAngabenDeklaration(null);	// Wird bei Freigabe rueber kopiert
			institutionContainer.setAngabenGemeinde(gemeindeContainer);

			final LastenausgleichTagesschuleAngabenInstitutionContainer saved =
				saveLastenausgleichTagesschuleInstitution(institutionContainer);

			gemeindeContainer.addLastenausgleichTagesschuleAngabenInstitutionContainer(saved);
		}
	}

	@Override
	@Nonnull
	public LastenausgleichTagesschuleAngabenInstitutionContainer saveLastenausgleichTagesschuleInstitution(
		@Nonnull LastenausgleichTagesschuleAngabenInstitutionContainer institutionContainer
	) {
		Objects.requireNonNull(institutionContainer);
		authorizer.checkWriteAuthorization(institutionContainer);

		return persistence.merge(institutionContainer);
	}

	@Override
	@Nonnull
	public LastenausgleichTagesschuleAngabenInstitutionContainer lastenausgleichTagesschuleInstitutionFreigeben(
		@Nonnull LastenausgleichTagesschuleAngabenInstitutionContainer institutionContainer
	) {
		Objects.requireNonNull(institutionContainer);
		authorizer.checkWriteAuthorization(institutionContainer);

		// Nur moeglich, wenn noch nicht freigegeben
		Preconditions.checkState(
			institutionContainer.getStatus() == LastenausgleichTagesschuleAngabenInstitutionStatus.OFFEN,
			"LastenausgleichAngabenInstitution muss im Status OFFEN sein");

		Objects.requireNonNull(institutionContainer.getAngabenKorrektur());

		institutionContainer.copyForFreigabe();
		institutionContainer.setStatus(LastenausgleichTagesschuleAngabenInstitutionStatus.IN_PRUEFUNG_GEMEINDE);
		return persistence.merge(institutionContainer);
	}
}


