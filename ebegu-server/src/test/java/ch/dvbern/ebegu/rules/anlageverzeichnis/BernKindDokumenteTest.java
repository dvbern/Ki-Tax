package ch.dvbern.ebegu.rules.anlageverzeichnis;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import ch.dvbern.ebegu.entities.DokumentGrund;
import ch.dvbern.ebegu.entities.Dossier;
import ch.dvbern.ebegu.entities.Fall;
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.entities.Kind;
import ch.dvbern.ebegu.entities.KindContainer;
import ch.dvbern.ebegu.entities.Mandant;
import ch.dvbern.ebegu.entities.PensumFachstelle;
import ch.dvbern.ebegu.types.DateRange;
import ch.dvbern.ebegu.util.mandant.MandantIdentifier;
import org.junit.Test;

import static ch.dvbern.ebegu.util.Constants.END_OF_TIME;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class BernKindDokumenteTest {

	BernKindDokumente bernKindDokumente = new BernKindDokumente();
	@Test
	public void getPensumFachstelleTag_test() {
		Gesuch gesuch = new Gesuch();
		gesuch.setDossier(new Dossier());
		gesuch.getDossier().setFall(new Fall());
		gesuch.getDossier().getFall().setMandant(new Mandant());
		gesuch.getDossier().getFall().getMandant().setMandantIdentifier(MandantIdentifier.BERN);
		KindContainer kindContainer = new KindContainer();
		kindContainer.setKindJA(new Kind());
		PensumFachstelle pensumFachstelle = new PensumFachstelle();
		pensumFachstelle.setGueltigkeit(new DateRange());
		pensumFachstelle.getGueltigkeit().setGueltigAb(LocalDate.of(2023,8,1));
		pensumFachstelle.getGueltigkeit().setGueltigBis(LocalDate.of(2024,7,31));
		kindContainer.getKindJA().addPensumFachstelle(pensumFachstelle);
		gesuch.addKindContainer(kindContainer);
		Set<DokumentGrund> dokumentGrundSet = new HashSet<>();
		bernKindDokumente.getAllDokumente(gesuch, dokumentGrundSet, Locale.GERMAN);
		assertThat(dokumentGrundSet.size(), is(1));
		DokumentGrund dokumentGrund = dokumentGrundSet.stream().findFirst().get();
		assertThat(dokumentGrund.getTag(), is("01.08.2023 - 31.07.2024"));

		dokumentGrundSet.clear();
		pensumFachstelle.getGueltigkeit().setGueltigBis(END_OF_TIME);
		kindContainer.getKindJA().setPensumFachstelle(new HashSet<>());
		kindContainer.getKindJA().addPensumFachstelle(pensumFachstelle);
		gesuch.setKindContainers(new HashSet<>());
		gesuch.addKindContainer(kindContainer);
		bernKindDokumente.getAllDokumente(gesuch, dokumentGrundSet, Locale.GERMAN);
		assertThat(dokumentGrundSet.size(), is(1));
		dokumentGrund = dokumentGrundSet.stream().findFirst().get();
		assertThat(dokumentGrund.getTag(), is("ab 01.08.2023"));
	}









}
