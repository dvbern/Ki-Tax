package ch.dvbern.ebegu.rules.anlageverzeichnis;

import ch.dvbern.ebegu.entities.AbstractFinanzielleSituation;
import ch.dvbern.ebegu.entities.DokumentGrund;
import ch.dvbern.ebegu.entities.Familiensituation;
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.enums.DokumentTyp;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Locale;
import java.util.Set;

public class SchwyzEinkommensverschlechterungDokumente extends AbstractDokumente<AbstractFinanzielleSituation, Familiensituation>  {
	@Override
	public void getAllDokumente(@Nonnull Gesuch gesuch, @Nonnull Set<DokumentGrund> anlageVerzeichnis, @Nonnull Locale locale) {
		// Wird in KIBON-3420 implementiert
	}

	@Override
	public boolean isDokumentNeeded(@Nonnull DokumentTyp dokumentTyp, @Nullable AbstractFinanzielleSituation dataForDocument) {
		return false;
	}
}
