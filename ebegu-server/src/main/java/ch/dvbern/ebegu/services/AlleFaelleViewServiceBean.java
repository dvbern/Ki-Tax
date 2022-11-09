/*
 * Copyright (C) 2022 DV Bern AG, Switzerland
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

package ch.dvbern.ebegu.services;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.security.PermitAll;
import javax.ejb.Local;
import javax.ejb.Stateless;
import javax.inject.Inject;

import ch.dvbern.ebegu.entities.AlleFaelleView;
import ch.dvbern.ebegu.entities.Benutzer;
import ch.dvbern.ebegu.entities.Betreuung;
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.entities.Institution;
import ch.dvbern.ebegu.entities.InstitutionStammdaten;
import ch.dvbern.ebegu.entities.KindContainer;
import ch.dvbern.lib.cdipersistence.Persistence;

@Stateless
@Local(AlleFaelleViewService.class)
@PermitAll
public class AlleFaelleViewServiceBean extends AbstractBaseService implements AlleFaelleViewService {

	@Inject
	private Persistence persistence;

	@Override
	public void updateViewWithFullGesuch(Gesuch gesuch) {
		AlleFaelleView alleFaelleView = convertGesuchToAlleFaelleView(gesuch);
		persistence.merge(alleFaelleView);
	}

	private AlleFaelleView convertGesuchToAlleFaelleView(Gesuch gesuch) {
		AlleFaelleView alleFaelleView = new AlleFaelleView();
		alleFaelleView.setAntragId(gesuch.getId());
		alleFaelleView.setMandantId(gesuch.getGesuchsperiode().getMandant().getId());
		alleFaelleView.setDossierId(gesuch.getDossier().getId());
		alleFaelleView.setFallId(gesuch.getFall().getId());
		alleFaelleView.setFallnummer(String.valueOf(gesuch.getFall().getFallNummer()));
		alleFaelleView.setBesitzerId(gesuch.getFall().getBesitzer() != null ?
			gesuch.getFall().getBesitzer().getId() :
			null);
		alleFaelleView.setBesitzerUsername(gesuch.getFall().getBesitzer() != null ?
			gesuch.getFall().getBesitzer().getUsername() :
			null);
		alleFaelleView.setGemeindeId(gesuch.getDossier().getGemeinde().getId());
		alleFaelleView.setGemeindeName(gesuch.getDossier().getGemeinde().getName());
		alleFaelleView.setAntragStatus(gesuch.getStatus());
		alleFaelleView.setAntragTyp(gesuch.getTyp());
		alleFaelleView.setEingangsart(gesuch.getEingangsart());
		alleFaelleView.setLaufnummer(gesuch.getLaufnummer());
		alleFaelleView.setFamilienName(gesuch.extractFamiliennamenString());
		alleFaelleView.setKinder(gesuch.getKindContainers().stream()
			.map(kc -> kc.getKindJA().getVorname())
			.collect(Collectors.joining(", ")));
		Objects.requireNonNull(gesuch.getTimestampMutiert());


		alleFaelleView.setAenderungsdatum(gesuch.getTimestampMutiert());
		alleFaelleView.setEingangsdatum(gesuch.getEingangsdatum());
		alleFaelleView.setEingangsdatumSTV(gesuch.getEingangsdatumSTV());
		alleFaelleView.setSozialdienst(gesuch.getFall().isSozialdienstFall());
		alleFaelleView.setSozialdienstId(gesuch.getFall().getSozialdienstFall() != null ?
			gesuch.getFall().getSozialdienstFall().getSozialdienst().getId() :
			null);
		alleFaelleView.setInternePendenz(gesuch.getInternePendenz());
		alleFaelleView.setDokumenteHochgeladen(gesuch.getDokumenteHochgeladen());
		alleFaelleView.setGesuchsperiodeId(gesuch.getGesuchsperiode().getId());
		alleFaelleView.setGesuchsperiodeString(gesuch.getGesuchsperiode().getGesuchsperiodeString());

		Benutzer verantwortlicherBG = gesuch.getDossier().getVerantwortlicherBG();
		if (verantwortlicherBG != null) {
			alleFaelleView.setVerantwortlicherBG(verantwortlicherBG.getFullName());
			alleFaelleView.setVerantwortlicherBGId(verantwortlicherBG.getId());
		}
		Benutzer verantwortlicherTS = gesuch.getDossier().getVerantwortlicherTS();
		if (verantwortlicherTS != null) {
			alleFaelleView.setVerantwortlicherTS(verantwortlicherTS.getFullName());
			alleFaelleView.setVerantwortlicherTSId(verantwortlicherTS.getId());
		}


		alleFaelleView.setInstitutionen(createInstitutionenList(gesuch.getKindContainers()));
		return alleFaelleView;
	}

	private List<Institution> createInstitutionenList(Set<KindContainer> kindContainers) {
		return kindContainers.stream()
			.flatMap(kc -> kc.getBetreuungen().stream())
			.map(Betreuung::getInstitutionStammdaten)
			.map(InstitutionStammdaten::getInstitution)
			.collect(Collectors.toList());
	}
}
