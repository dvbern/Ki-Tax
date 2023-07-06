package ch.dvbern.ebegu.entities;

import javax.inject.Inject;
import javax.persistence.PostRemove;
import javax.persistence.PostUpdate;

import ch.dvbern.ebegu.services.AlleFaelleViewService;

public class AlleFaelleKindListener {

	@Inject
	private AlleFaelleViewService alleFaelleViewService;

	//PostPersist wird hier nicht verwendet, weil wir zum Erstellen des Kindes in der View die GesuchId brauchen
	//das Erstellen der Kinder wird über den AlleFelleKindContainerListener gemacht

	@PostUpdate
	public void updateKindInAlleFaelleView(Kind kind) {
		if (!alleFaelleViewService.isNeueAlleFaelleViewActivated()) {
			return;
		}

		alleFaelleViewService.updateKindInView(kind);
	}

	@PostRemove
	public void removeKindFromAlleFaelleView(Kind kind) {
		if (!alleFaelleViewService.isNeueAlleFaelleViewActivated()) {
			return;
		}

		alleFaelleViewService.removeKindInView(kind);
	}
}
