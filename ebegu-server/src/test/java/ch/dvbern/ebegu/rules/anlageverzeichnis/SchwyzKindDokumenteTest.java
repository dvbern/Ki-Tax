package ch.dvbern.ebegu.rules.anlageverzeichnis;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import javax.annotation.Nonnull;

import ch.dvbern.ebegu.entities.DokumentGrund;
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.entities.Kind;
import ch.dvbern.ebegu.entities.KindContainer;
import ch.dvbern.ebegu.entities.Mandant;
import ch.dvbern.ebegu.enums.DokumentGrundTyp;
import ch.dvbern.ebegu.enums.DokumentTyp;
import ch.dvbern.ebegu.enums.EinschulungTyp;
import ch.dvbern.ebegu.enums.Kinderabzug;
import ch.dvbern.ebegu.test.TestDataUtil;
import ch.dvbern.ebegu.util.mandant.MandantIdentifier;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

class SchwyzKindDokumenteTest {

	SchwyzKindDokumente schwyzKindDokumente = new SchwyzKindDokumente();

	@Test
	void kindWithoutHoehereBeitraege_shouldNotHaveDokument() {
		Gesuch gesuch = setupGesuch();
		KindContainer kindContainer = setupKindContainer();
		gesuch.getKindContainers().add(kindContainer);
		final Kind kind = kindContainer.getKindJA();
		kind.setKinderabzugErstesHalbjahr(Kinderabzug.GANZER_ABZUG);
		kind.setFamilienErgaenzendeBetreuung(true);
		kind.setEinschulungTyp(EinschulungTyp.VORSCHULALTER);
		kind.setUnterhaltspflichtig(true);
		kind.setLebtKindAlternierend(true);
		kind.setHoehereBeitraegeWegenBeeintraechtigungBeantragen(false);

		Set<DokumentGrund> dokumentGrundSet = new HashSet<>();
		schwyzKindDokumente.getAllDokumente(gesuch, dokumentGrundSet, Locale.GERMAN);

		assertThat(dokumentGrundSet.isEmpty(), is(true));
	}

	@Test
	void kindWithHoehereBeitraegeNichtDigitalHochladen_shouldNotHaveDokument() {
		Gesuch gesuch = setupGesuch();
		KindContainer kindContainer = setupKindContainer();
		gesuch.getKindContainers().add(kindContainer);
		final Kind kind = kindContainer.getKindJA();
		kind.setKinderabzugErstesHalbjahr(Kinderabzug.GANZER_ABZUG);
		kind.setFamilienErgaenzendeBetreuung(true);
		kind.setEinschulungTyp(EinschulungTyp.VORSCHULALTER);
		kind.setUnterhaltspflichtig(true);
		kind.setLebtKindAlternierend(true);
		kind.setHoehereBeitraegeWegenBeeintraechtigungBeantragen(true);
		kind.setHoehereBeitraegeUnterlagenDigital(false);

		Set<DokumentGrund> dokumentGrundSet = new HashSet<>();
		schwyzKindDokumente.getAllDokumente(gesuch, dokumentGrundSet, Locale.GERMAN);

		assertThat(dokumentGrundSet.isEmpty(), is(true));
	}

	@Nested
	class NachweisHoehereBeitraegeBeintraechtigungTest {
		@Test
		void kindWithHoehereBeitraegeDigitalHochladen_shouldHaveNachweisHoehereBeitraegeBeeintraechtigung() {
			Gesuch gesuch = setupGesuch();
			KindContainer kindContainer = setupKindContainer();
			gesuch.getKindContainers().add(kindContainer);
			final Kind kind = kindContainer.getKindJA();
			kind.setKinderabzugErstesHalbjahr(Kinderabzug.GANZER_ABZUG);
			kind.setFamilienErgaenzendeBetreuung(true);
			kind.setEinschulungTyp(EinschulungTyp.VORSCHULALTER);
			kind.setUnterhaltspflichtig(true);
			kind.setLebtKindAlternierend(true);
			kind.setHoehereBeitraegeWegenBeeintraechtigungBeantragen(true);
			kind.setHoehereBeitraegeUnterlagenDigital(true);

			Set<DokumentGrund> dokumentGrundSet = new HashSet<>();
			schwyzKindDokumente.getAllDokumente(gesuch, dokumentGrundSet, Locale.GERMAN);

			assertThat(dokumentGrundSet.size(), is(1));
			assertThat(dokumentGrundSet.stream().findFirst().orElseThrow().getDokumentGrundTyp(), is(DokumentGrundTyp.KINDER));
			assertThat(
				dokumentGrundSet.stream().findFirst().orElseThrow().getDokumentTyp(),
				is(DokumentTyp.NACHWEIS_HOEHERE_BEITRAEGE_BEEINTRAECHTIGUNG));
		}

	}
	@Nonnull
	private static Gesuch setupGesuch() {
		Gesuch gesuch = TestDataUtil.createDefaultGesuch();
		setSchwyzMandant(gesuch);
		return gesuch;
	}

	@Nonnull
	private static KindContainer setupKindContainer() {
		KindContainer kindContainer = TestDataUtil.createDefaultKindContainer();
		kindContainer.getKindJA().setPensumFachstelle(Set.of());
		return kindContainer;
	}

	private static void setSchwyzMandant(Gesuch gesuch) {
		gesuch.getDossier().getFall().setMandant(new Mandant());
		gesuch.getDossier().getFall().getMandant().setMandantIdentifier(MandantIdentifier.SCHWYZ);
	}

}
