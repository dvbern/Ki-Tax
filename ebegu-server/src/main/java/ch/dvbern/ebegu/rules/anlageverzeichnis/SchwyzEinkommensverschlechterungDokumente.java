package ch.dvbern.ebegu.rules.anlageverzeichnis;

import ch.dvbern.ebegu.entities.AbstractFinanzielleSituation;
import ch.dvbern.ebegu.entities.DokumentGrund;
import ch.dvbern.ebegu.entities.Einkommensverschlechterung;
import ch.dvbern.ebegu.entities.Familiensituation;
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.entities.GesuchstellerContainer;
import ch.dvbern.ebegu.enums.DokumentGrundPersonType;
import ch.dvbern.ebegu.enums.DokumentGrundTyp;
import ch.dvbern.ebegu.enums.DokumentTyp;
import ch.dvbern.ebegu.errors.EbeguRuntimeException;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import java.util.Locale;
import java.util.Objects;
import java.util.Set;

public class SchwyzEinkommensverschlechterungDokumente
	extends AbstractDokumente<AbstractFinanzielleSituation, Familiensituation> {
	@Override
	public void getAllDokumente(@Nonnull Gesuch gesuch, @Nonnull Set<DokumentGrund> anlageVerzeichnis, @Nonnull Locale locale) {
		final GesuchstellerContainer gesuchsteller1 = gesuch.getGesuchsteller1();
		if (gesuchsteller1 == null || gesuch.getEinkommensverschlechterungInfoContainer() == null) {
			return;
		}

		final Boolean gesuchHasEKV = gesuch.getEinkommensverschlechterungInfoContainer()
			.getEinkommensverschlechterungInfoJA()
			.getEinkommensverschlechterung();

		if (Boolean.FALSE.equals(gesuchHasEKV) || gesuchsteller1.getEinkommensverschlechterungContainer() == null) {
			return;
		}

		Familiensituation familiensituation = gesuch.extractFamiliensituation();
		Objects.requireNonNull(familiensituation);

		Einkommensverschlechterung ekv = gesuchsteller1.getEinkommensverschlechterungContainer().getEkvJABasisJahrPlus1();

		getAllDokumenteForGS(gesuch, anlageVerzeichnis, ekv, 1);

		if (gesuch.getGesuchsteller2() != null && Boolean.FALSE.equals(familiensituation.getGemeinsameSteuererklaerung())) {
			if (gesuch.getGesuchsteller2().getEinkommensverschlechterungContainer() == null) {
				return;
			}
			Einkommensverschlechterung ekvGS2 =
				gesuch.getGesuchsteller2().getEinkommensverschlechterungContainer().getEkvJABasisJahrPlus1();
			getAllDokumenteForGS(gesuch, anlageVerzeichnis, ekvGS2, 2);
		}
	}

	private void getAllDokumenteForGS(
		@Nonnull Gesuch gesuch,
		@Nonnull Set<DokumentGrund> anlageVerzeichnis,
		Einkommensverschlechterung ekv,
		int gesuchstellerNummer) {
		add(getDokument(DokumentTyp.NACHWEIS_NETTOLOHN, ekv, gesuch, gesuchstellerNummer), anlageVerzeichnis);
		add(getDokument(DokumentTyp.NACHWEIS_EINKAEUFE_VORSORGE, ekv, gesuch, gesuchstellerNummer), anlageVerzeichnis);
		add(getDokument(DokumentTyp.NACHWEIS_ABZUEGE_LIEGENSCHAFT, ekv, gesuch, gesuchstellerNummer), anlageVerzeichnis);
		add(getDokument(DokumentTyp.NACHWEIS_VERMOEGEN, ekv, gesuch, gesuchstellerNummer), anlageVerzeichnis);
		add(getDokument(DokumentTyp.NACHWEIS_BRUTTOLOHN, ekv, gesuch, gesuchstellerNummer), anlageVerzeichnis);
	}

	@Nullable
	private DokumentGrund getDokument(
		DokumentTyp dokumentTyp,
		Einkommensverschlechterung einkommensverschlechterung,
		Gesuch gesuch,
		int gesuchstellerNummer) {
		return getDokument(
			dokumentTyp,
			einkommensverschlechterung,
			gesuch.extractFamiliensituation(),
			String.valueOf(gesuch.getGesuchsperiode().getBasisJahrPlus1()),
			DokumentGrundPersonType.GESUCHSTELLER,
			gesuchstellerNummer,
			DokumentGrundTyp.EINKOMMENSVERSCHLECHTERUNG,
			gesuch.getGesuchsperiode().getGueltigkeit().getGueltigBis()
		);
	}

	@Override
	public boolean isDokumentNeeded(@Nonnull DokumentTyp dokumentTyp, @Nullable AbstractFinanzielleSituation dataForDocument) {
		if (dataForDocument == null) {
			return false;
		}
		switch (dokumentTyp) {
		case NACHWEIS_NETTOLOHN:
			return dataForDocument.getSteuerbaresEinkommen() != null;
		case NACHWEIS_EINKAEUFE_VORSORGE:
			return dataForDocument.getEinkaeufeVorsorge() != null;
		case NACHWEIS_ABZUEGE_LIEGENSCHAFT:
			return dataForDocument.getAbzuegeLiegenschaft() != null;
		case NACHWEIS_VERMOEGEN:
			return dataForDocument.getSteuerbaresVermoegen() != null;
		case NACHWEIS_BRUTTOLOHN:
			if (!(dataForDocument instanceof Einkommensverschlechterung)) {
				throw new EbeguRuntimeException(
					"isDokumentNeeded",
					"This class must only be used with Einkommensverschlechterunt");
			}
			return ((Einkommensverschlechterung) dataForDocument).getBruttolohnAbrechnung1() != null;
		default:
			return false;
		}
	}
}
