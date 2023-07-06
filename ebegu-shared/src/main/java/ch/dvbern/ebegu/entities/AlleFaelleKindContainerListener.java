package ch.dvbern.ebegu.entities;

import javax.inject.Inject;
import javax.persistence.PostPersist;

import ch.dvbern.ebegu.services.AlleFaelleViewService;

public class AlleFaelleKindContainerListener {

	@Inject
	private AlleFaelleViewService alleFaelleViewService;

	@PostPersist
	public void createKindInAlleFaelleView(KindContainer kindContainer) {
		if (!alleFaelleViewService.isNeueAlleFaelleViewActivated()) {
			return;
		}

		alleFaelleViewService.createKindInView(kindContainer.getKindJA(), kindContainer.getGesuch());
	}

}
