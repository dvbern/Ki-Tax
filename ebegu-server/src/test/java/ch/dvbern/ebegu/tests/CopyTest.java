/*
 * Ki-Tax: System for the management of external childcare subsidies
 * Copyright (C) 2018 City of Bern Switzerland
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package ch.dvbern.ebegu.tests;

import java.time.LocalDate;
import java.time.Month;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;
import java.util.TreeSet;

import ch.dvbern.ebegu.entities.Dossier;
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.entities.Gesuchsperiode;
import ch.dvbern.ebegu.entities.GesuchstellerAdresseContainer;
import ch.dvbern.ebegu.entities.InstitutionStammdaten;
import ch.dvbern.ebegu.entities.Kind;
import ch.dvbern.ebegu.entities.KindContainer;
import ch.dvbern.ebegu.enums.AntragStatus;
import ch.dvbern.ebegu.enums.AntragTyp;
import ch.dvbern.ebegu.enums.Eingangsart;
import ch.dvbern.ebegu.enums.EinschulungTyp;
import ch.dvbern.ebegu.enums.EnumFamilienstatus;
import ch.dvbern.ebegu.testfaelle.Testfall01_WaeltiDagmar;
import ch.dvbern.ebegu.tets.TestDataUtil;
import ch.dvbern.ebegu.util.MathUtil;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Test copy entities for Mutation, Erneuerung, neues Dossier
 */
public class CopyTest {

	private Gesuch erstgesuch;

	private Gesuch mutation;
	private Gesuch erneuerung;
	private Gesuch mutationNeuesDossier;
	private Gesuch erneuerungNeuesDossier;

	@Before
	public void setUp() {
		// Komplettes Gesuch aufsetzen
		Collection<InstitutionStammdaten> instStammdaten = new ArrayList<>();
		instStammdaten.add(TestDataUtil.createInstitutionStammdatenKitaBruennen());
		instStammdaten.add(TestDataUtil.createInstitutionStammdatenKitaWeissenstein());
		instStammdaten.add(TestDataUtil.createInstitutionStammdatenTagiWeissenstein());
		Gesuchsperiode gesuchsperiode = TestDataUtil.createGesuchsperiode1617();
		Testfall01_WaeltiDagmar testfall01_waeltiDagmar = new Testfall01_WaeltiDagmar(gesuchsperiode, instStammdaten);
		testfall01_waeltiDagmar.createGesuch(LocalDate.now());
		testfall01_waeltiDagmar.fillInGesuch();
		erstgesuch = testfall01_waeltiDagmar.getGesuch();
		Objects.requireNonNull(erstgesuch.getFamiliensituationContainer()).setFamiliensituationErstgesuch(erstgesuch.getFamiliensituationContainer().getFamiliensituationJA());
		TestDataUtil.createDefaultEinkommensverschlechterungsInfoContainer(erstgesuch);
		assert erstgesuch.getGesuchsteller1() != null;
		erstgesuch.getGesuchsteller1().setEinkommensverschlechterungContainer(TestDataUtil.createDefaultEinkommensverschlechterungsContainer());
		erstgesuch.setDokumentGrunds(new TreeSet<>());
		assert erstgesuch.getDokumentGrunds() != null;
		erstgesuch.getDokumentGrunds().add(TestDataUtil.createDefaultDokumentGrund());

		// Sonderfälle: Ablaufende Adressen und Fachstellen
		LocalDate datumAblauf = LocalDate.of(2017, Month.JANUARY, 15);
		// 2 Adressen, eine davon läuft während Gesuchsperiode ab
		GesuchstellerAdresseContainer adresseAblaufend = TestDataUtil.createDefaultGesuchstellerAdresseContainer(erstgesuch.getGesuchsteller1());
		Objects.requireNonNull(adresseAblaufend.extractGueltigkeit()).setGueltigBis(datumAblauf.minusDays(1));
		GesuchstellerAdresseContainer adresseZukuenftig = TestDataUtil.createDefaultGesuchstellerAdresseContainer(erstgesuch.getGesuchsteller1());
		Objects.requireNonNull(adresseZukuenftig.extractGueltigkeit()).setGueltigAb(datumAblauf);
		erstgesuch.getGesuchsteller1().getAdressen().clear();
		erstgesuch.getGesuchsteller1().getAdressen().add(adresseAblaufend);
		erstgesuch.getGesuchsteller1().getAdressen().add(adresseZukuenftig);
		// Fachstelle, welche während der Gesuchsperiode abläuft
		Kind kind = erstgesuch.getKindContainers().iterator().next().getKindJA();
		kind.setPensumFachstelle(TestDataUtil.createDefaultPensumFachstelle());
		Objects.requireNonNull(kind.getPensumFachstelle()).getGueltigkeit().setGueltigBis(datumAblauf);

		Assert.assertNotNull(erstgesuch);
		Assert.assertNotNull(erstgesuch.getEingangsdatum());
		Assert.assertNotNull(erstgesuch.getFamiliensituationContainer());
		Assert.assertNotNull(erstgesuch.getFamiliensituationContainer().getFamiliensituationJA());
		Assert.assertNotNull(erstgesuch.getKindContainers());
		Assert.assertEquals(1, erstgesuch.getKindContainers().size());
		Assert.assertNotNull(erstgesuch.extractAllBetreuungen());
		Assert.assertEquals(2, erstgesuch.extractAllBetreuungen().size());
		Assert.assertNotNull(erstgesuch.getGesuchsteller1());
		Assert.assertNotNull(erstgesuch.getGesuchsteller1().getFinanzielleSituationContainer());
		Assert.assertNotNull(erstgesuch.getGesuchsteller1().getFinanzielleSituationContainer().getFinanzielleSituationJA());
		Assert.assertEquals(MathUtil.DEFAULT.from(53265), erstgesuch.getGesuchsteller1().getFinanzielleSituationContainer().getFinanzielleSituationJA().getNettolohn());
		Assert.assertNotNull(erstgesuch.getEinkommensverschlechterungInfoContainer());
		Assert.assertNotNull(erstgesuch.getEinkommensverschlechterungInfoContainer().getEinkommensverschlechterungInfoJA());
		Assert.assertNotNull(erstgesuch.getGesuchsteller1().getEinkommensverschlechterungContainer());
		Assert.assertNotNull(erstgesuch.getGesuchsteller1().getEinkommensverschlechterungContainer().getEkvJABasisJahrPlus1());
		Assert.assertEquals(MathUtil.DEFAULT.from(1), erstgesuch.getGesuchsteller1().getEinkommensverschlechterungContainer().getEkvGSBasisJahrPlus1().getNettolohn());
		Assert.assertNotNull(erstgesuch.getDokumentGrunds());
		Assert.assertEquals(1, erstgesuch.getDokumentGrunds().size());

		Gesuchsperiode folgeperiode = TestDataUtil.createGesuchsperiode1718();
		Dossier folgeDossier = TestDataUtil.createDefaultDossier();
		folgeDossier.setFall(erstgesuch.getFall());

		mutation = erstgesuch.copyForMutation(new Gesuch(), Eingangsart.ONLINE);
		erneuerung = erstgesuch.copyForErneuerung(new Gesuch(), folgeperiode, Eingangsart.ONLINE);
		mutationNeuesDossier = erstgesuch.copyForMutationNeuesDossier(new Gesuch(), Eingangsart.ONLINE, AntragTyp.ERSTGESUCH, folgeDossier);
		erneuerungNeuesDossier = erstgesuch.copyForErneuerungsgesuchNeuesDossier(new Gesuch(), Eingangsart.ONLINE, AntragTyp.ERSTGESUCH, folgeDossier, folgeperiode);
	}

	@Test
	public void copyGesuch() {
		// VorgaengerId: Nur bei Mutation gesetzt
		Assert.assertNotNull(mutation.getVorgaengerId());
		Assert.assertNull(erneuerung.getVorgaengerId());
		Assert.assertNull(mutationNeuesDossier.getVorgaengerId());
		Assert.assertNull(erneuerungNeuesDossier.getVorgaengerId());

		// Eingangsart: immer ONLINE
		Assert.assertEquals(Eingangsart.ONLINE, mutation.getEingangsart());
		Assert.assertEquals(Eingangsart.ONLINE, erneuerung.getEingangsart());
		Assert.assertEquals(Eingangsart.ONLINE, mutationNeuesDossier.getEingangsart());
		Assert.assertEquals(Eingangsart.ONLINE, erneuerungNeuesDossier.getEingangsart());

		// Dossier
		Assert.assertEquals(erstgesuch.getDossier().getId(), mutation.getDossier().getId());
		Assert.assertEquals(erstgesuch.getDossier().getId(), erneuerung.getDossier().getId());
		Assert.assertNotEquals(erstgesuch.getDossier().getId(), mutationNeuesDossier.getDossier().getId());
		Assert.assertNotEquals(erstgesuch.getDossier().getId(), erneuerungNeuesDossier.getDossier().getId());

		// Status
		Assert.assertEquals(AntragStatus.IN_BEARBEITUNG_GS, mutation.getStatus());
		Assert.assertEquals(AntragStatus.IN_BEARBEITUNG_GS, erneuerung.getStatus());
		Assert.assertEquals(AntragStatus.IN_BEARBEITUNG_GS, mutationNeuesDossier.getStatus());
		Assert.assertEquals(AntragStatus.IN_BEARBEITUNG_GS, erneuerungNeuesDossier.getStatus());

		// Eingangsdatum / RegelnAbDatum
		Assert.assertNull(mutation.getEingangsdatum());
		Assert.assertNull(mutation.getRegelStartDatum());
		Assert.assertNull(erneuerung.getEingangsdatum());
		Assert.assertNull(erneuerung.getRegelStartDatum());
		Assert.assertNull(mutationNeuesDossier.getEingangsdatum());
		Assert.assertNull(mutationNeuesDossier.getRegelStartDatum());
		Assert.assertNull(erneuerungNeuesDossier.getEingangsdatum());
		Assert.assertNull(erneuerungNeuesDossier.getRegelStartDatum());

		// Typ
		Assert.assertEquals(AntragTyp.MUTATION, mutation.getTyp());
		Assert.assertEquals(AntragTyp.ERNEUERUNGSGESUCH, erneuerung.getTyp());
		Assert.assertEquals(AntragTyp.ERSTGESUCH, mutationNeuesDossier.getTyp());
		Assert.assertEquals(AntragTyp.ERSTGESUCH, erneuerungNeuesDossier.getTyp());
	}

	@Test
	public void copyFamiliensituation() {
		Assert.assertNotNull(mutation.extractFamiliensituation());
		Assert.assertNotNull(erneuerung.extractFamiliensituation());
		Assert.assertNotNull(mutationNeuesDossier.extractFamiliensituation());
		Assert.assertNotNull(erneuerungNeuesDossier.extractFamiliensituation());

		Assert.assertEquals(EnumFamilienstatus.ALLEINERZIEHEND, Objects.requireNonNull(mutation.extractFamiliensituation()).getFamilienstatus());
		Assert.assertEquals(EnumFamilienstatus.ALLEINERZIEHEND, Objects.requireNonNull(erneuerung.extractFamiliensituation()).getFamilienstatus());
		Assert.assertEquals(EnumFamilienstatus.ALLEINERZIEHEND, Objects.requireNonNull(mutationNeuesDossier.extractFamiliensituation()).getFamilienstatus());
		Assert.assertEquals(EnumFamilienstatus.ALLEINERZIEHEND, Objects.requireNonNull(erneuerungNeuesDossier.extractFamiliensituation()).getFamilienstatus());
	}

	@Test
	public void copyGesuchsteller() {
		Assert.assertNotNull(mutation.getGesuchsteller1());
		Assert.assertNotNull(mutation.getGesuchsteller1().getGesuchstellerJA());
		Assert.assertEquals("Dagmar Wälti", mutation.getGesuchsteller1().getGesuchstellerJA().getFullName());

		Assert.assertNotNull(erneuerung.getGesuchsteller1());
		Assert.assertNotNull(erneuerung.getGesuchsteller1().getGesuchstellerJA());
		Assert.assertEquals("Dagmar Wälti", erneuerung.getGesuchsteller1().getGesuchstellerJA().getFullName());

		Assert.assertNotNull(mutationNeuesDossier.getGesuchsteller1());
		Assert.assertNotNull(mutationNeuesDossier.getGesuchsteller1().getGesuchstellerJA());
		Assert.assertEquals("Dagmar Wälti", mutationNeuesDossier.getGesuchsteller1().getGesuchstellerJA().getFullName());

		Assert.assertNotNull(erneuerungNeuesDossier.getGesuchsteller1());
		Assert.assertNotNull(erneuerungNeuesDossier.getGesuchsteller1().getGesuchstellerJA());
		Assert.assertEquals("Dagmar Wälti", erneuerungNeuesDossier.getGesuchsteller1().getGesuchstellerJA().getFullName());
	}

	@Test
	public void copyEinkommensverschlechterung() {
		Assert.assertNotNull(mutation.getEinkommensverschlechterungInfoContainer());
		Assert.assertNotNull(mutation.getEinkommensverschlechterungInfoContainer().getEinkommensverschlechterungInfoJA());
		Assert.assertEquals(Boolean.TRUE,
			mutation.getEinkommensverschlechterungInfoContainer().getEinkommensverschlechterungInfoJA().getEkvFuerBasisJahrPlus1());

		Assert.assertNotNull(mutation.getGesuchsteller1());
		Assert.assertNotNull(mutation.getGesuchsteller1().getEinkommensverschlechterungContainer());
		Assert.assertNotNull(mutation.getGesuchsteller1().getEinkommensverschlechterungContainer().getEkvJABasisJahrPlus1());
		Assert.assertNotNull(mutation.getGesuchsteller1().getFinanzielleSituationContainer());
		Assert.assertEquals(MathUtil.DEFAULT.from(53265), mutation.getGesuchsteller1().getFinanzielleSituationContainer().getFinanzielleSituationJA().getNettolohn());

		Assert.assertNull(erneuerung.getEinkommensverschlechterungInfoContainer());
		Assert.assertNotNull(erneuerung.getGesuchsteller1());
		Assert.assertNull(erneuerung.getGesuchsteller1().getEinkommensverschlechterungContainer());

		Assert.assertNotNull(mutationNeuesDossier.getEinkommensverschlechterungInfoContainer());
		Assert.assertNotNull(mutationNeuesDossier.getEinkommensverschlechterungInfoContainer().getEinkommensverschlechterungInfoJA());
		Assert.assertEquals(Boolean.TRUE,
			mutationNeuesDossier.getEinkommensverschlechterungInfoContainer().getEinkommensverschlechterungInfoJA().getEkvFuerBasisJahrPlus1());

		Assert.assertNotNull(mutationNeuesDossier.getGesuchsteller1());
		Assert.assertNotNull(mutationNeuesDossier.getGesuchsteller1().getEinkommensverschlechterungContainer());
		Assert.assertNotNull(mutationNeuesDossier.getGesuchsteller1().getEinkommensverschlechterungContainer().getEkvJABasisJahrPlus1());
		Assert.assertNotNull(mutationNeuesDossier.getGesuchsteller1().getFinanzielleSituationContainer());
		Assert.assertEquals(MathUtil.DEFAULT.from(53265), mutationNeuesDossier.getGesuchsteller1().getFinanzielleSituationContainer().getFinanzielleSituationJA().getNettolohn());

		Assert.assertNull(erneuerungNeuesDossier.getEinkommensverschlechterungInfoContainer());
		Assert.assertNotNull(erneuerungNeuesDossier.getGesuchsteller1());
		Assert.assertNull(erneuerungNeuesDossier.getGesuchsteller1().getEinkommensverschlechterungContainer());
	}

	@Test
	public void copyKinder() {
		Assert.assertNotNull(mutation.getKindContainers());
		Assert.assertEquals(1, mutation.getKindContainers().size());
		Kind kindMutation = mutation.getKindContainers().iterator().next().getKindJA();
		Assert.assertNotNull(kindMutation);
		Assert.assertEquals("Simon Wälti", kindMutation.getFullName());
		Assert.assertEquals(EinschulungTyp.KLASSE1, kindMutation.getEinschulungTyp());

		Assert.assertNotNull(erneuerung.getKindContainers());
		Assert.assertEquals(1, erneuerung.getKindContainers().size());
		Kind kindErneuerung = erneuerung.getKindContainers().iterator().next().getKindJA();
		Assert.assertNotNull(kindErneuerung);
		Assert.assertEquals("Simon Wälti", kindErneuerung.getFullName());
		Assert.assertNull(kindErneuerung.getEinschulungTyp()); // Wird nicht kopiert!

		Assert.assertNotNull(mutationNeuesDossier.getKindContainers());
		Assert.assertEquals(1, mutationNeuesDossier.getKindContainers().size());
		Kind kindMutationNeuesDossier = mutationNeuesDossier.getKindContainers().iterator().next().getKindJA();
		Assert.assertNotNull(kindMutationNeuesDossier);
		Assert.assertEquals("Simon Wälti", kindMutationNeuesDossier.getFullName());
		Assert.assertEquals(EinschulungTyp.KLASSE1, kindMutationNeuesDossier.getEinschulungTyp());

		Assert.assertNotNull(erneuerungNeuesDossier.getKindContainers());
		Assert.assertEquals(1, erneuerungNeuesDossier.getKindContainers().size());
		Kind kindErneuerungNeuesDossier = erneuerungNeuesDossier.getKindContainers().iterator().next().getKindJA();
		Assert.assertNotNull(kindErneuerungNeuesDossier);
		Assert.assertEquals("Simon Wälti", kindErneuerungNeuesDossier.getFullName());
		Assert.assertNull(kindErneuerungNeuesDossier.getEinschulungTyp()); // Wird nicht kopiert!
	}

	@Test
	public void copyBetreuungen() {
		Assert.assertNotNull(mutation.extractAllBetreuungen());
		Assert.assertEquals(2, mutation.extractAllBetreuungen().size());

		Assert.assertNotNull(erneuerung.extractAllBetreuungen());
		Assert.assertEquals(0, erneuerung.extractAllBetreuungen().size());

		Assert.assertNotNull(mutationNeuesDossier.extractAllBetreuungen());
		Assert.assertEquals(0, mutationNeuesDossier.extractAllBetreuungen().size());

		Assert.assertNotNull(erneuerungNeuesDossier.extractAllBetreuungen());
		Assert.assertEquals(0, erneuerungNeuesDossier.extractAllBetreuungen().size());
	}

	@Test
	public void copyDokumente() {
		Assert.assertNotNull(mutation.getDokumentGrunds());
		Assert.assertEquals(1, mutation.getDokumentGrunds().size());

		Assert.assertNull(erneuerung.getDokumentGrunds());

		Assert.assertNotNull(mutationNeuesDossier.getDokumentGrunds());
		Assert.assertEquals(1, mutationNeuesDossier.getDokumentGrunds().size());

		Assert.assertNull(erneuerungNeuesDossier.getDokumentGrunds());
	}

	@Test
	public void copyAdressen() {
		Assert.assertNotNull(mutation.getGesuchsteller1());
		Assert.assertNotNull(mutation.getGesuchsteller1().getAdressen());
		Assert.assertEquals(2, mutation.getGesuchsteller1().getAdressen().size());

		Assert.assertNotNull(erneuerung.getGesuchsteller1());
		Assert.assertNotNull(erneuerung.getGesuchsteller1().getAdressen());
		Assert.assertEquals(1, erneuerung.getGesuchsteller1().getAdressen().size());

		Assert.assertNotNull(mutationNeuesDossier.getGesuchsteller1());
		Assert.assertNotNull(mutationNeuesDossier.getGesuchsteller1().getAdressen());
		Assert.assertEquals(1, mutationNeuesDossier.getGesuchsteller1().getAdressen().size());

		Assert.assertNotNull(erneuerungNeuesDossier.getGesuchsteller1());
		Assert.assertNotNull(erneuerungNeuesDossier.getGesuchsteller1().getAdressen());
		Assert.assertEquals(1, erneuerungNeuesDossier.getGesuchsteller1().getAdressen().size());
	}

	@Test
	public void copyFachstellen() {
		KindContainer kindMutation = mutation.getKindContainers().iterator().next();
		Assert.assertNotNull(kindMutation);
		Assert.assertNotNull(kindMutation.getKindJA());
		Assert.assertNotNull(kindMutation.getKindJA().getPensumFachstelle());

		KindContainer kindErneuerung = erneuerung.getKindContainers().iterator().next();
		Assert.assertNotNull(kindErneuerung);
		Assert.assertNotNull(kindErneuerung.getKindJA());
		Assert.assertNull(kindErneuerung.getKindJA().getPensumFachstelle());

		KindContainer kindMutationNeuesDossier = mutationNeuesDossier.getKindContainers().iterator().next();
		Assert.assertNotNull(kindMutationNeuesDossier);
		Assert.assertNotNull(kindMutationNeuesDossier.getKindJA());
		Assert.assertNotNull(kindMutationNeuesDossier.getKindJA().getPensumFachstelle());

		KindContainer kindErneuerungNeuesDossier = erneuerungNeuesDossier.getKindContainers().iterator().next();
		Assert.assertNotNull(kindErneuerungNeuesDossier);
		Assert.assertNotNull(kindErneuerungNeuesDossier.getKindJA());
		Assert.assertNull(kindErneuerungNeuesDossier.getKindJA().getPensumFachstelle());
	}
}
