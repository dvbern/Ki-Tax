package ch.dvbern.ebegu.rules.anlageverzeichnis;

import ch.dvbern.ebegu.entities.DokumentGrund;
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.enums.DokumentGrundTyp;
import ch.dvbern.ebegu.enums.DokumentTyp;

import javax.ejb.Stateless;
import java.util.HashSet;
import java.util.Set;

@Stateless
public class DokumentenverzeichnisEvaluator {

	private AbstractDokumente familiensituationDokumente = new FamiliensituationDokumente();
	private AbstractDokumente kindAnlagen = new KindDokumente();
	private AbstractDokumente erwerbspensumDokumente = new ErwerbspensumDokumente();
	private AbstractDokumente finanzielleSituationDokumente = new FinanzielleSituationDokumente();
	private AbstractDokumente einkommensverschlechterungDokumente = new EinkommensverschlechterungDokumente();

	public Set<DokumentGrund> calculate(Gesuch gesuch) {

		Set<DokumentGrund> anlageVerzeichnis = new HashSet<>();

		if (gesuch != null) {
			familiensituationDokumente.getAllDokumente(gesuch, anlageVerzeichnis);
			kindAnlagen.getAllDokumente(gesuch, anlageVerzeichnis);
			erwerbspensumDokumente.getAllDokumente(gesuch, anlageVerzeichnis);
			finanzielleSituationDokumente.getAllDokumente(gesuch, anlageVerzeichnis);
			einkommensverschlechterungDokumente.getAllDokumente(gesuch, anlageVerzeichnis);
		}

		return anlageVerzeichnis;
	}

	public void addSonstige(Set<DokumentGrund> dokumentGrunds) {
		DokumentGrund dokumentGrund = new DokumentGrund(DokumentGrundTyp.SONSTIGE_NACHWEISE, DokumentTyp.DIV);
		dokumentGrund.setNeeded(false);
		dokumentGrunds.add(dokumentGrund);
	}

	public void addPapiergesuch(Set<DokumentGrund> dokumentGrunds, Gesuch gesuch) {
		DokumentGrund dokumentGrund = new DokumentGrund(DokumentGrundTyp.PAPIERGESUCH, DokumentTyp.ORIGINAL_PAPIERGESUCH);
		dokumentGrund.setNeeded(false);
		dokumentGrunds.add(dokumentGrund);
	}
}