package ch.dvbern.ebegu.entities;

import javax.inject.Inject;
import javax.persistence.PostPersist;
import javax.persistence.PostRemove;
import javax.persistence.PostUpdate;

import ch.dvbern.ebegu.services.AlleFaelleViewService;

public class AlleFaelleGesuchListener {

	@Inject
	private AlleFaelleViewService alleFaelleViewService;

	@PostPersist
	public void createGesuchInAlleFaelleView(Gesuch gesuch) {
		alleFaelleViewService.createViewForFullGesuch(gesuch);
	}

	@PostUpdate
	public void updateGesuchInAlleFaelleView(Gesuch gesuch) {
		alleFaelleViewService.updateViewForGesuch(gesuch);
	}

	@PostRemove
	public void removeGesuchInAlleFaelleView(Gesuch gesuch) {
		alleFaelleViewService.removeViewForGesuch(gesuch);
	}

}
