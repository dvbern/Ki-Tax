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
import ch.dvbern.ebegu.test.TestDataUtil;
import ch.dvbern.ebegu.util.MathUtil;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

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
		instStammdaten.add(TestDataUtil.createInstitutionStammdatenTagesfamilien());
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

		assertNotNull(erstgesuch);
		assertNotNull(erstgesuch.getEingangsdatum());
		assertNotNull(erstgesuch.getFamiliensituationContainer());
		assertNotNull(erstgesuch.getFamiliensituationContainer().getFamiliensituationJA());
		assertNotNull(erstgesuch.getKindContainers());
		assertEquals(1, erstgesuch.getKindContainers().size());
		assertNotNull(erstgesuch.extractAllBetreuungen());
		assertEquals(2, erstgesuch.extractAllBetreuungen().size());
		assertNotNull(erstgesuch.getGesuchsteller1());
		assertNotNull(erstgesuch.getGesuchsteller1().getFinanzielleSituationContainer());
		assertNotNull(erstgesuch.getGesuchsteller1().getFinanzielleSituationContainer().getFinanzielleSituationJA());
		assertEquals(MathUtil.DEFAULT.from(53265), erstgesuch.getGesuchsteller1().getFinanzielleSituationContainer().getFinanzielleSituationJA().getNettolohn());
		assertNotNull(erstgesuch.getEinkommensverschlechterungInfoContainer());
		assertNotNull(erstgesuch.getEinkommensverschlechterungInfoContainer().getEinkommensverschlechterungInfoJA());
		assertNotNull(erstgesuch.getGesuchsteller1().getEinkommensverschlechterungContainer());
		assertNotNull(erstgesuch.getGesuchsteller1().getEinkommensverschlechterungContainer().getEkvJABasisJahrPlus1());
		assertNotNull(erstgesuch.getGesuchsteller1().getEinkommensverschlechterungContainer().getEkvGSBasisJahrPlus1());
		assertEquals(MathUtil.DEFAULT.from(1), erstgesuch.getGesuchsteller1().getEinkommensverschlechterungContainer().getEkvGSBasisJahrPlus1().getNettolohn());
		assertNotNull(erstgesuch.getDokumentGrunds());
		assertEquals(1, erstgesuch.getDokumentGrunds().size());

		Gesuchsperiode folgeperiode = TestDataUtil.createGesuchsperiode1718();
		Dossier folgeDossier = TestDataUtil.createDefaultDossier();
		folgeDossier.setFall(erstgesuch.getFall());

		mutation = erstgesuch.copyForMutation(new Gesuch(), Eingangsart.ONLINE, erstgesuch.getEingangsdatum());
		erneuerung = erstgesuch.copyForErneuerung(new Gesuch(), folgeperiode, Eingangsart.ONLINE, erstgesuch.getEingangsdatum());
		mutationNeuesDossier = erstgesuch.copyForMutationNeuesDossier(new Gesuch(), Eingangsart.ONLINE, folgeDossier);
		erneuerungNeuesDossier = erstgesuch.copyForErneuerungsgesuchNeuesDossier(new Gesuch(), Eingangsart.ONLINE, folgeDossier, folgeperiode, erstgesuch.getEingangsdatum());
	}

	@Test
	public void copyGesuch() {
		// VorgaengerId: Nur bei Mutation gesetzt
		assertNotNull(mutation.getVorgaengerId());
		assertNull(erneuerung.getVorgaengerId());
		assertNull(mutationNeuesDossier.getVorgaengerId());
		assertNull(erneuerungNeuesDossier.getVorgaengerId());

		// Eingangsart: immer ONLINE
		assertEquals(Eingangsart.ONLINE, mutation.getEingangsart());
		assertEquals(Eingangsart.ONLINE, erneuerung.getEingangsart());
		assertEquals(Eingangsart.ONLINE, mutationNeuesDossier.getEingangsart());
		assertEquals(Eingangsart.ONLINE, erneuerungNeuesDossier.getEingangsart());

		// Dossier
		assertEquals(erstgesuch.getDossier().getId(), mutation.getDossier().getId());
		assertEquals(erstgesuch.getDossier().getId(), erneuerung.getDossier().getId());
		assertNotEquals(erstgesuch.getDossier().getId(), mutationNeuesDossier.getDossier().getId());
		assertNotEquals(erstgesuch.getDossier().getId(), erneuerungNeuesDossier.getDossier().getId());

		// Status
		assertEquals(AntragStatus.IN_BEARBEITUNG_GS, mutation.getStatus());
		assertEquals(AntragStatus.IN_BEARBEITUNG_GS, erneuerung.getStatus());
		assertEquals(AntragStatus.IN_BEARBEITUNG_GS, mutationNeuesDossier.getStatus());
		assertEquals(AntragStatus.IN_BEARBEITUNG_GS, erneuerungNeuesDossier.getStatus());

		// Eingangsdatum / RegelnAbDatum: notnull because it is an online gesuch
		assertNull(mutation.getEingangsdatum());
		assertNotNull(mutation.getRegelStartDatum());
		assertNull(erneuerung.getEingangsdatum());
		assertNotNull(erneuerung.getRegelStartDatum());
		assertNull(mutationNeuesDossier.getEingangsdatum());
		assertNotNull(mutationNeuesDossier.getRegelStartDatum());
		assertNull(erneuerungNeuesDossier.getEingangsdatum());
		assertNotNull(erneuerungNeuesDossier.getRegelStartDatum());

		// Typ
		assertEquals(AntragTyp.MUTATION, mutation.getTyp());
		assertEquals(AntragTyp.ERNEUERUNGSGESUCH, erneuerung.getTyp());
		assertEquals(AntragTyp.ERSTGESUCH, mutationNeuesDossier.getTyp());
		assertEquals(AntragTyp.ERSTGESUCH, erneuerungNeuesDossier.getTyp());
	}

	@Test
	public void copyFamiliensituation() {
		assertNotNull(mutation.extractFamiliensituation());
		assertNotNull(erneuerung.extractFamiliensituation());
		assertNotNull(mutationNeuesDossier.extractFamiliensituation());
		assertNotNull(erneuerungNeuesDossier.extractFamiliensituation());

		assertEquals(EnumFamilienstatus.ALLEINERZIEHEND, Objects.requireNonNull(mutation.extractFamiliensituation()).getFamilienstatus());
		assertEquals(EnumFamilienstatus.ALLEINERZIEHEND, Objects.requireNonNull(erneuerung.extractFamiliensituation()).getFamilienstatus());
		assertEquals(EnumFamilienstatus.ALLEINERZIEHEND, Objects.requireNonNull(mutationNeuesDossier.extractFamiliensituation()).getFamilienstatus());
		assertEquals(EnumFamilienstatus.ALLEINERZIEHEND, Objects.requireNonNull(erneuerungNeuesDossier.extractFamiliensituation()).getFamilienstatus());
	}

	@Test
	public void copyGesuchsteller() {
		assertNotNull(mutation.getGesuchsteller1());
		assertNotNull(mutation.getGesuchsteller1().getGesuchstellerJA());
		assertEquals("Dagmar Wälti", mutation.getGesuchsteller1().getGesuchstellerJA().getFullName());

		assertNotNull(erneuerung.getGesuchsteller1());
		assertNotNull(erneuerung.getGesuchsteller1().getGesuchstellerJA());
		assertEquals("Dagmar Wälti", erneuerung.getGesuchsteller1().getGesuchstellerJA().getFullName());

		assertNotNull(mutationNeuesDossier.getGesuchsteller1());
		assertNotNull(mutationNeuesDossier.getGesuchsteller1().getGesuchstellerJA());
		assertEquals("Dagmar Wälti", mutationNeuesDossier.getGesuchsteller1().getGesuchstellerJA().getFullName());

		assertNotNull(erneuerungNeuesDossier.getGesuchsteller1());
		assertNotNull(erneuerungNeuesDossier.getGesuchsteller1().getGesuchstellerJA());
		assertEquals("Dagmar Wälti", erneuerungNeuesDossier.getGesuchsteller1().getGesuchstellerJA().getFullName());
	}

	@Test
	public void copyEinkommensverschlechterung() {
		// Mutation: Wird kopiert
		assertNotNull(mutation.getEinkommensverschlechterungInfoContainer());
		assertNotNull(mutation.getEinkommensverschlechterungInfoContainer().getEinkommensverschlechterungInfoJA());
		assertEquals(Boolean.TRUE,
			mutation.getEinkommensverschlechterungInfoContainer().getEinkommensverschlechterungInfoJA().getEkvFuerBasisJahrPlus1());
		assertNotNull(mutation.getGesuchsteller1());
		assertNotNull(mutation.getGesuchsteller1().getEinkommensverschlechterungContainer());
		assertNotNull(mutation.getGesuchsteller1().getEinkommensverschlechterungContainer().getEkvJABasisJahrPlus1());
		assertNotNull(mutation.getGesuchsteller1().getFinanzielleSituationContainer());
		assertEquals(MathUtil.DEFAULT.from(53265), mutation.getGesuchsteller1().getFinanzielleSituationContainer().getFinanzielleSituationJA().getNettolohn());

		// Erneuerung: Wird nicht kopiert
		assertNull(erneuerung.getEinkommensverschlechterungInfoContainer());
		assertNotNull(erneuerung.getGesuchsteller1());
		assertNull(erneuerung.getGesuchsteller1().getEinkommensverschlechterungContainer());

		// Neues Dossier im selben Jahr: Wird kopiert
		assertNotNull(mutationNeuesDossier.getEinkommensverschlechterungInfoContainer());
		assertNotNull(mutationNeuesDossier.getEinkommensverschlechterungInfoContainer().getEinkommensverschlechterungInfoJA());
		assertEquals(Boolean.TRUE,
			mutationNeuesDossier.getEinkommensverschlechterungInfoContainer().getEinkommensverschlechterungInfoJA().getEkvFuerBasisJahrPlus1());
		assertNotNull(mutationNeuesDossier.getGesuchsteller1());
		assertNotNull(mutationNeuesDossier.getGesuchsteller1().getEinkommensverschlechterungContainer());
		assertNotNull(mutationNeuesDossier.getGesuchsteller1().getEinkommensverschlechterungContainer().getEkvJABasisJahrPlus1());
		assertNotNull(mutationNeuesDossier.getGesuchsteller1().getFinanzielleSituationContainer());
		assertEquals(MathUtil.DEFAULT.from(53265), mutationNeuesDossier.getGesuchsteller1().getFinanzielleSituationContainer().getFinanzielleSituationJA().getNettolohn());

		// Neues Dossier im neuen Jahr: Wird nicht kopiert
		assertNull(erneuerungNeuesDossier.getEinkommensverschlechterungInfoContainer());
		assertNotNull(erneuerungNeuesDossier.getGesuchsteller1());
		assertNull(erneuerungNeuesDossier.getGesuchsteller1().getEinkommensverschlechterungContainer());
	}

	@Test
	public void copyKinder() {
		assertNotNull(mutation.getKindContainers());
		assertEquals(1, mutation.getKindContainers().size());
		Kind kindMutation = mutation.getKindContainers().iterator().next().getKindJA();
		assertNotNull(kindMutation);
		assertEquals("Simon Wälti", kindMutation.getFullName());
		assertEquals(EinschulungTyp.VORSCHULALTER, kindMutation.getEinschulungTyp());

		assertNotNull(erneuerung.getKindContainers());
		assertEquals(1, erneuerung.getKindContainers().size());
		Kind kindErneuerung = erneuerung.getKindContainers().iterator().next().getKindJA();
		assertNotNull(kindErneuerung);
		assertEquals("Simon Wälti", kindErneuerung.getFullName());
		assertNull(kindErneuerung.getEinschulungTyp()); // Wird nicht kopiert!

		assertNotNull(mutationNeuesDossier.getKindContainers());
		assertEquals(1, mutationNeuesDossier.getKindContainers().size());
		Kind kindMutationNeuesDossier = mutationNeuesDossier.getKindContainers().iterator().next().getKindJA();
		assertNotNull(kindMutationNeuesDossier);
		assertEquals("Simon Wälti", kindMutationNeuesDossier.getFullName());
		assertEquals(EinschulungTyp.VORSCHULALTER, kindMutationNeuesDossier.getEinschulungTyp());

		assertNotNull(erneuerungNeuesDossier.getKindContainers());
		assertEquals(1, erneuerungNeuesDossier.getKindContainers().size());
		Kind kindErneuerungNeuesDossier = erneuerungNeuesDossier.getKindContainers().iterator().next().getKindJA();
		assertNotNull(kindErneuerungNeuesDossier);
		assertEquals("Simon Wälti", kindErneuerungNeuesDossier.getFullName());
		assertNull(kindErneuerungNeuesDossier.getEinschulungTyp());
	}

	@Test
	public void copyBetreuungen() {
		// Betreuungen werden nur bei Mutationen in der selben Gemeinde kopiert
		assertNotNull(mutation.extractAllBetreuungen());
		assertEquals(2, mutation.extractAllBetreuungen().size());

		assertNotNull(erneuerung.extractAllBetreuungen());
		assertEquals(0, erneuerung.extractAllBetreuungen().size());

		assertNotNull(mutationNeuesDossier.extractAllBetreuungen());
		assertEquals(0, mutationNeuesDossier.extractAllBetreuungen().size());

		assertNotNull(erneuerungNeuesDossier.extractAllBetreuungen());
		assertEquals(0, erneuerungNeuesDossier.extractAllBetreuungen().size());
	}

	@Test
	public void copyDokumente() {
		// Dokumente werden nur im selben Jahr kopiert (unabhängig von Gemeinde)
		assertNotNull(mutation.getDokumentGrunds());
		assertEquals(1, mutation.getDokumentGrunds().size());

		assertNull(erneuerung.getDokumentGrunds());

		assertNotNull(mutationNeuesDossier.getDokumentGrunds());
		assertEquals(1, mutationNeuesDossier.getDokumentGrunds().size());

		assertNull(erneuerungNeuesDossier.getDokumentGrunds());
	}

	@Test
	public void copyAdressen() {
		// Mutation: Es werden alle Adressen kopiert
		assertNotNull(mutation.getGesuchsteller1());
		assertNotNull(mutation.getGesuchsteller1().getAdressen());
		assertEquals(2, mutation.getGesuchsteller1().getAdressen().size());

		// Erneuerung: Es werden nur aktive Adressen kopiert
		assertNotNull(erneuerung.getGesuchsteller1());
		assertNotNull(erneuerung.getGesuchsteller1().getAdressen());
		assertEquals(1, erneuerung.getGesuchsteller1().getAdressen().size());

		// Neue Gemeinde: Es werden nur aktive Adressen kopiert (egal welche GP)
		assertNotNull(mutationNeuesDossier.getGesuchsteller1());
		assertNotNull(mutationNeuesDossier.getGesuchsteller1().getAdressen());
		assertEquals(1, mutationNeuesDossier.getGesuchsteller1().getAdressen().size());

		assertNotNull(erneuerungNeuesDossier.getGesuchsteller1());
		assertNotNull(erneuerungNeuesDossier.getGesuchsteller1().getAdressen());
		assertEquals(1, erneuerungNeuesDossier.getGesuchsteller1().getAdressen().size());
	}

	@Test
	public void copyFachstellen() {
		// Fachstellen werden im gleichen Jahr immer kopiert, im neuen Jahr nur wenn noch aktuell (unabhängig von Gemeinde)
		KindContainer kindMutation = mutation.getKindContainers().iterator().next();
		assertNotNull(kindMutation);
		assertNotNull(kindMutation.getKindJA());
		assertNotNull(kindMutation.getKindJA().getPensumFachstelle());

		KindContainer kindErneuerung = erneuerung.getKindContainers().iterator().next();
		assertNotNull(kindErneuerung);
		assertNotNull(kindErneuerung.getKindJA());
		assertNull(kindErneuerung.getKindJA().getPensumFachstelle());

		KindContainer kindMutationNeuesDossier = mutationNeuesDossier.getKindContainers().iterator().next();
		assertNotNull(kindMutationNeuesDossier);
		assertNotNull(kindMutationNeuesDossier.getKindJA());
		assertNotNull(kindMutationNeuesDossier.getKindJA().getPensumFachstelle());

		KindContainer kindErneuerungNeuesDossier = erneuerungNeuesDossier.getKindContainers().iterator().next();
		assertNotNull(kindErneuerungNeuesDossier);
		assertNotNull(kindErneuerungNeuesDossier.getKindJA());
		assertNull(kindErneuerungNeuesDossier.getKindJA().getPensumFachstelle());
	}
}
