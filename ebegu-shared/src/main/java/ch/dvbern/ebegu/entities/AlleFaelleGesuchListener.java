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
		if (!alleFaelleViewService.isNeueAlleFaelleViewActivated()) {
			return;
		}

		alleFaelleViewService.createViewForFullGesuch(gesuch);
	}

	@PostUpdate
	public void updateGesuchInAlleFaelleView(Gesuch gesuch) {
		if (!alleFaelleViewService.isNeueAlleFaelleViewActivated()) {
			return;
		}

		alleFaelleViewService.updateViewForGesuch(gesuch);
	}

	@PostRemove
	public void removeGesuchInAlleFaelleView(Gesuch gesuch) {
		if (!alleFaelleViewService.isNeueAlleFaelleViewActivated()) {
			return;
		}

		alleFaelleViewService.removeViewForGesuch(gesuch);
	}

}
