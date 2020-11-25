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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

import javax.annotation.Nonnull;
import javax.ejb.Local;
import javax.ejb.Stateless;
import javax.inject.Inject;

import ch.dvbern.ebegu.entities.Gemeinde;
import ch.dvbern.ebegu.entities.Gesuchsperiode;
import ch.dvbern.ebegu.entities.gemeindeantrag.GemeindeAntrag;
import ch.dvbern.ebegu.entities.gemeindeantrag.LastenausgleichTagesschuleAngabenGemeindeContainer;
import ch.dvbern.ebegu.entities.gemeindeantrag.LastenausgleichTagesschuleAngabenInstitutionContainer;
import ch.dvbern.ebegu.enums.gemeindeantrag.LastenausgleichTagesschuleAngabenGemeindeStatus;
import ch.dvbern.ebegu.services.AbstractBaseService;
import ch.dvbern.ebegu.services.Authorizer;
import ch.dvbern.ebegu.services.GemeindeService;
import ch.dvbern.lib.cdipersistence.Persistence;
import com.google.common.base.Preconditions;

/**
 * Service fuer den Lastenausgleich der Tagesschulen
 */
@Stateless
@Local(LastenausgleichTagesschuleAngabenGemeindeService.class)
public class LastenausgleichTagesschuleAngabenGemeindeServiceBean extends AbstractBaseService implements LastenausgleichTagesschuleAngabenGemeindeService {

	@Inject
	private Persistence persistence;

	@Inject
	private Authorizer authorizer;

	@Inject
	private GemeindeService gemeindeService;

	@Inject
	private LastenausgleichTagesschuleAngabenInstitutionService angabenInstitutionService;

	@Inject
	private LastenausgleichTagesschuleAngabenGemeindeStatusHistoryService historyService;


	@Override
	@Nonnull
	public List<? extends GemeindeAntrag> createLastenausgleichTagesschuleGemeinde(
		@Nonnull Gesuchsperiode gesuchsperiode
	) {
		Objects.requireNonNull(gesuchsperiode);

		List<GemeindeAntrag> result = new ArrayList<>();
		final Collection<Gemeinde> aktiveGemeinden = gemeindeService.getAktiveGemeinden();
		for (Gemeinde gemeinde : aktiveGemeinden) {
			LastenausgleichTagesschuleAngabenGemeindeContainer fallContainer = new LastenausgleichTagesschuleAngabenGemeindeContainer();
			fallContainer.setGesuchsperiode(gesuchsperiode);
			fallContainer.setGemeinde(gemeinde);
			fallContainer.setStatus(LastenausgleichTagesschuleAngabenGemeindeStatus.NEU);
			fallContainer.setAngabenKorrektur(null); 	// Wird erst mit den Daten initialisiert, da alles zwingend
			fallContainer.setAngabenDeklaration(null); 	// Wird bei Freigabe rueberkopiert
			final LastenausgleichTagesschuleAngabenGemeindeContainer saved = saveLastenausgleichTagesschuleGemeinde(fallContainer, true);
			angabenInstitutionService.createLastenausgleichTagesschuleInstitution(saved);
			result.add(saved);
		}
		return result;
	}

	@Override
	@Nonnull
	public LastenausgleichTagesschuleAngabenGemeindeContainer saveLastenausgleichTagesschuleGemeinde(
		@Nonnull LastenausgleichTagesschuleAngabenGemeindeContainer fallContainer
	) {
		return saveLastenausgleichTagesschuleGemeinde(fallContainer, false);
	}

	@Override
	@Nonnull
	public LastenausgleichTagesschuleAngabenGemeindeContainer saveLastenausgleichTagesschuleGemeinde(
		@Nonnull LastenausgleichTagesschuleAngabenGemeindeContainer fallContainer,
		boolean saveInStatusHistory
	) {
		Objects.requireNonNull(fallContainer);
		authorizer.checkWriteAuthorization(fallContainer);

		final LastenausgleichTagesschuleAngabenGemeindeContainer saved = persistence.merge(fallContainer);
		if (saveInStatusHistory) {
			historyService.saveLastenausgleichTagesschuleStatusChange(saved);
		}
		return saved;
	}

	@Override
	@Nonnull
	public LastenausgleichTagesschuleAngabenGemeindeContainer lastenausgleichTagesschuleGemeindeEinreichen(
		@Nonnull LastenausgleichTagesschuleAngabenGemeindeContainer fallContainer
	) {
		Objects.requireNonNull(fallContainer);
		authorizer.checkWriteAuthorization(fallContainer);

		// Nur moeglich, wenn noch nicht freigegeben und ueberhaupt Daten zum kopieren vorhanden
		Preconditions.checkState(
			fallContainer.getStatus() == LastenausgleichTagesschuleAngabenGemeindeStatus.IN_BEARBEITUNG_GEMEINDE,
			"LastenausgleichAngabenGemeinde muss im Status OFFEN sein");
		Preconditions.checkArgument(
			fallContainer.getAngabenInstitutionContainers()
				.stream()
				.allMatch(LastenausgleichTagesschuleAngabenInstitutionContainer::isAntragAbgeschlossen),
			"Alle LastenausgleichAngabenInstitution muessen abgeschlossen sein");
		Objects.requireNonNull(fallContainer.getAngabenKorrektur());

		fallContainer.copyForFreigabe();
		fallContainer.setStatus(LastenausgleichTagesschuleAngabenGemeindeStatus.IN_PRUEFUNG_KANTON);
		return saveLastenausgleichTagesschuleGemeinde(fallContainer, true);
	}
}


