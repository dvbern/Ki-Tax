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

import java.util.Set;
import java.util.TreeSet;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.ejb.Stateless;
import javax.inject.Inject;

import ch.dvbern.ebegu.entities.Betreuung;
import ch.dvbern.ebegu.entities.Einstellung;
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.entities.KindContainer;
import ch.dvbern.ebegu.enums.AnspruchBeschaeftigungAbhaengigkeitTyp;
import ch.dvbern.ebegu.enums.EinschulungTyp;
import ch.dvbern.ebegu.enums.EinstellungKey;
import ch.dvbern.ebegu.enums.KinderabzugTyp;
import ch.dvbern.ebegu.enums.betreuung.Betreuungsstatus;
import ch.dvbern.ebegu.errors.EbeguEntityNotFoundException;
import ch.dvbern.ebegu.services.BetreuungService;
import ch.dvbern.ebegu.services.EinstellungService;
import ch.dvbern.ebegu.services.GesuchstellerService;
import org.jetbrains.annotations.NotNull;

@Stateless
public class KindServiceHandler {
	@Inject
	private EinstellungService einstellungService;

	@Inject
	private GesuchstellerService gesuchstellerService;

	@Inject
	private BetreuungService betreuungService;

	public void resetKindBetreuungenStatusOnKindSave(@Nonnull KindContainer kind, @Nullable KindContainer dbKind) {
		EinschulungTyp alteEinschulungTyp = null;
		if (dbKind != null) {
			alteEinschulungTyp = dbKind.getKindJA().getEinschulungTyp();
		}
		if (wechseltKindVonVorschulalterZuSchulstufe(kind, alteEinschulungTyp) || compareHoehereBeitraegeChange(kind, dbKind)) {
			Set<Betreuung> betreuungTreeSet = new TreeSet<>();
			betreuungTreeSet.addAll(kind.getBetreuungen());
			betreuungTreeSet.forEach(betreuung -> {
				if (betreuung.isAngebotKita() || betreuung.isAngebotTagesfamilien() &&
					Betreuungsstatus.BESTAETIGT.equals(betreuung.getBetreuungsstatus())
				) {
					betreuung.setBetreuungsstatus(Betreuungsstatus.WARTEN);
					betreuung.setEventPublished(false);
					betreuungService.saveBetreuung(betreuung, false, null);
				}
			});
			kind.getBetreuungen().clear();
			kind.getBetreuungen().addAll(betreuungTreeSet);
		}
	}

	private boolean compareHoehereBeitraegeChange(KindContainer kind, KindContainer dbKind) {
		if (!iSSchwyzHoehereBeitraegeAktiviert(kind)) {
			return false;
		}
		return !dbKind.getKindJA()
			.getHoehereBeitraegeWegenBeeintraechtigungBeantragen()
			.equals(kind.getKindJA().getHoehereBeitraegeWegenBeeintraechtigungBeantragen());
	}

	public void resetGesuchDataOnKindSave(@Nonnull KindContainer kind) {
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

	public void resetKindBetreuungenDatenOnKindSave(@Nonnull KindContainer kind, @Nullable KindContainer dbKind) {
		EinschulungTyp alteEinschulungTyp = null;
		if (dbKind != null) {
			alteEinschulungTyp = dbKind.getKindJA().getEinschulungTyp();

			resetBetreuungInFerienzeit(kind, alteEinschulungTyp);
			resetBedarfsstufe(kind, dbKind);
		}
	}

	private void resetBedarfsstufe(KindContainer kind, KindContainer dbKind) {
		if (compareHoehereBeitraegeChange(kind, dbKind)) {
			kind.getBetreuungen().forEach(betreuung -> betreuung.setBedarfsstufe(null));
		}
	}

	private void resetBetreuungInFerienzeit(KindContainer kind, @Nullable EinschulungTyp alteEinschulungTyp) {
		if (wechseltKindVonSchulstufeZuVorschulalter(kind, alteEinschulungTyp)) {
			kind.getBetreuungen().forEach(betreuung -> {
				if (betreuung.isAngebotKita() || betreuung.isAngebotTagesfamilien()) {
					betreuung.getBetreuungspensumContainers().forEach(
						betreuungspensumContainer -> betreuungspensumContainer.getBetreuungspensumJA()
							.setBetreuungInFerienzeit(null)
					);
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

	private boolean iSSchwyzHoehereBeitraegeAktiviert(@NotNull KindContainer kind) {
		final Gesuch gesuch = kind.getGesuch();

		Einstellung hoehereBeitraegeAktiviert = einstellungService.getEinstellungByMandant(
				EinstellungKey.HOEHERE_BEITRAEGE_BEEINTRAECHTIGUNG_AKTIVIERT,
				gesuch.getGesuchsperiode())
			.orElseThrow(() -> new EbeguEntityNotFoundException(
				"saveKind",
				"Einstellung HOEHERE_BEITRAEGE_BEEINTRAECHTIGUNG_AKTIVIERT is missing for gesuchsperiode {}",
				gesuch.getGesuchsperiode().getId()));
		return hoehereBeitraegeAktiviert.getValueAsBoolean();
	}

	private boolean wechseltKindVonVorschulalterZuSchulstufe(
		@Nonnull KindContainer kind,
		@Nullable EinschulungTyp alteEinschulungTyp) {
		if (!isSchwyzEinschulungTypAktiviert(kind)  || alteEinschulungTyp == null) {
			return false;
		}
		return kind.getKindJA().getEinschulungTyp() != null &&
			kind.getKindJA().getEinschulungTyp().isEingeschult() &&
			!alteEinschulungTyp.isEingeschult();
	}

	private boolean wechseltKindVonSchulstufeZuVorschulalter(
		@Nonnull KindContainer kind,
		@Nullable EinschulungTyp alteEinschulungTyp) {
		if (!isSchwyzEinschulungTypAktiviert(kind) || alteEinschulungTyp == null) {
			return false;
		}
		return kind.getKindJA().getEinschulungTyp() != null &&
			!kind.getKindJA().getEinschulungTyp().isEingeschult() &&
			alteEinschulungTyp.isEingeschult();
	}
}
