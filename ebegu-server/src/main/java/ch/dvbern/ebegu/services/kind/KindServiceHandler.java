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

package ch.dvbern.ebegu.services.kind;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.ejb.Stateless;
import javax.inject.Inject;

import ch.dvbern.ebegu.entities.Einstellung;
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.entities.KindContainer;
import ch.dvbern.ebegu.enums.AnspruchBeschaeftigungAbhaengigkeitTyp;
import ch.dvbern.ebegu.enums.Betreuungsstatus;
import ch.dvbern.ebegu.enums.EinstellungKey;
import ch.dvbern.ebegu.enums.KinderabzugTyp;
import ch.dvbern.ebegu.errors.EbeguEntityNotFoundException;
import ch.dvbern.ebegu.services.BetreuungService;
import ch.dvbern.ebegu.services.EinstellungService;
import ch.dvbern.ebegu.services.GesuchstellerService;

@Stateless
public class KindServiceHandler {
	@Inject
	private EinstellungService einstellungService;

	@Inject
	private GesuchstellerService gesuchstellerService;

	@Inject
	private BetreuungService betreuungService;

	protected void resetKindBetreuungenStatusOnKindSave(@Nonnull KindContainer kind, @Nullable KindContainer oldKind) {
		if (!isSchwyzEinschulungTypAktiviert(kind) || oldKind == null) {
			return; //Betreuungstatus muss nur wenn der KinderabzugTyp = SCHWYZ resetet werden
		}
		if (kind.getKindJA().getEinschulungTyp() != null &&
			!kind.getKindJA().getEinschulungTyp().isEingeschult() &&
			oldKind.getKindJA().getEinschulungTyp() != null &&
			oldKind.getKindJA().getEinschulungTyp().isEingeschult()
		) {
			kind.getBetreuungen().forEach(betreuung -> {
				if (betreuung.isAngebotKita() || betreuung.isAngebotTagesfamilien() &&
					Betreuungsstatus.BESTAETIGT.equals(betreuung.getBetreuungsstatus())
				) {
					betreuung.setBetreuungsstatus(Betreuungsstatus.WARTEN);
					betreuung.setEventPublished(false);
					betreuungService.saveBetreuung(betreuung, false, null);
				}
			});
		}
	}

	protected void resetGesuchDataOnKindSave(@Nonnull KindContainer kind) {
		final Gesuch gesuch = kind.getGesuch();

		Einstellung anspruchBeschaeftigungTyp = einstellungService.getEinstellungByMandant(
				EinstellungKey.ABHAENGIGKEIT_ANSPRUCH_BESCHAEFTIGUNGPENSUM,
				gesuch
					.getGesuchsperiode())
			.orElseThrow(() -> new EbeguEntityNotFoundException(
				"saveKind",
				"Einstellung ABHAENGIGKEIT_ANSPRUCH_BESCHAEFTIGUNGPENSUM is missing for gesuchsperiode {}",
				gesuch.getGesuchsperiode().getId()));

		if (AnspruchBeschaeftigungAbhaengigkeitTyp.valueOf(anspruchBeschaeftigungTyp.getValue())
			== AnspruchBeschaeftigungAbhaengigkeitTyp.SCHWYZ && gesuch.getGesuchsteller2() != null) {
			boolean hasKindWithUnterhaltspflichtGS2 = gesuch.getKindContainers()
				.stream()
				.map(KindContainer::getKindJA)
				.anyMatch(kindJA -> Boolean.TRUE.equals(kindJA.getGemeinsamesGesuch()));
			if (!hasKindWithUnterhaltspflichtGS2 && !gesuch.getGesuchsteller2().getErwerbspensenContainers().isEmpty()) {
				gesuch.getGesuchsteller2().getErwerbspensenContainers().clear();
				gesuchstellerService.saveGesuchsteller(gesuch.getGesuchsteller2(), gesuch, 2, false);
			}
		}
	}

	public void resetKindBetreuungenDatenOnKindSave(@Nonnull KindContainer kind, @Nullable KindContainer oldKind) {
		if (!isSchwyzEinschulungTypAktiviert(kind) || oldKind == null) {
			return; //Betreuungstatus muss nur wenn der KinderabzugTyp = SCHWYZ resetet werden
		}
		if (kind.getKindJA().getEinschulungTyp() != null &&
			kind.getKindJA().getEinschulungTyp().isEingeschult() &&
			oldKind.getKindJA().getEinschulungTyp() != null &&
			!oldKind.getKindJA().getEinschulungTyp().isEingeschult()
		) {
			kind.getBetreuungen().forEach(betreuung -> {
				if (betreuung.isAngebotKita() || betreuung.isAngebotTagesfamilien()) {
					betreuung.getBetreuungspensumContainers().forEach(
						betreuungspensumContainer -> betreuungspensumContainer.getBetreuungspensumJA()
							.setBetreuungInFerienzeit(null)
					);
					betreuungService.saveBetreuung(betreuung, false, null);
				}
			});
		}
	}

	private boolean isSchwyzEinschulungTypAktiviert(KindContainer kind) {
		final Gesuch gesuch = kind.getGesuch();

		Einstellung kinderabzugTyp = einstellungService.getEinstellungByMandant(
				EinstellungKey.KINDERABZUG_TYP,
				gesuch
					.getGesuchsperiode())
			.orElseThrow(() -> new EbeguEntityNotFoundException(
				"saveKind",
				"Einstellung KINDERABZUG_TYP is missing for gesuchsperiode {}",
				gesuch.getGesuchsperiode().getId()));
		return KinderabzugTyp.SCHWYZ.equals(KinderabzugTyp.valueOf(kinderabzugTyp.getValue()));
	}
}
