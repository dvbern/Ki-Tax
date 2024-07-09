/*
 * Copyright (C) 2024 DV Bern AG, Switzerland
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 *
 */

package ch.dvbern.ebegu.services.personensuche;

import ch.dvbern.ebegu.dto.personensuche.EWKAdresse;
import ch.dvbern.ebegu.dto.personensuche.EWKPerson;
import ch.dvbern.ebegu.dto.personensuche.EWKResultat;
import ch.dvbern.ebegu.entities.AdresseTyp;
import ch.dvbern.ebegu.entities.Gemeinde;
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.entities.Gesuchsperiode;
import ch.dvbern.ebegu.entities.Gesuchsteller;
import ch.dvbern.ebegu.entities.GesuchstellerAdresse;
import ch.dvbern.ebegu.entities.GesuchstellerAdresseContainer;
import ch.dvbern.ebegu.entities.Kind;
import ch.dvbern.ebegu.entities.Mandant;
import ch.dvbern.ebegu.errors.PersonenSucheServiceBusinessException;
import ch.dvbern.ebegu.errors.PersonenSucheServiceException;
import ch.dvbern.ebegu.test.TestDataUtil;
import ch.dvbern.ebegu.test.util.TestDataInstitutionStammdatenBuilder;
import ch.dvbern.ebegu.testfaelle.Testfall01_WaeltiDagmar;
import ch.dvbern.ebegu.testfaelle.Testfall02_FeutzYvonne;
import ch.dvbern.ebegu.util.mandant.MandantIdentifier;
import ch.dvbern.ebegu.ws.ewk.GeresClient;
import ch.dvbern.ebegu.ws.ewk.GeresUtil;
import org.easymock.EasyMockExtension;
import org.easymock.EasyMockSupport;
import org.easymock.Mock;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.ArrayList;
import java.util.Objects;

import static org.easymock.EasyMock.expect;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.is;

@ExtendWith(EasyMockExtension.class)
class PersonenSucheSchwyzServiceTest extends EasyMockSupport {

	private static final Long GEBAEUDE_ID = 1000L;
	private static final Long WOHNUNGS_ID = 2002L;
	@Mock
	private GeresClient geresClient;

	private PersonenSucheSchwyzService testee;

	@BeforeEach
	void setUp() {
		testee = new PersonenSucheSchwyzService(geresClient);
	}

	@AfterEach
	void tearDown() {
		verifyAll();
	}

	@Nested
	@DisplayName("Suche mit einem GS")
	class MitEinGs {
		@Test
		@DisplayName("muss GS1 mit AHV Nr, und Kind im Haushalt finden")
		void suchePersonenMitGs1Gefunden() throws PersonenSucheServiceBusinessException,
			PersonenSucheServiceException {
			// given
			Mandant mandant = new Mandant();
			mandant.setMandantIdentifier(MandantIdentifier.SCHWYZ);

			Gesuchsperiode gesuchsperiode = TestDataUtil.createGesuchsperiode1718(mandant);
			Gemeinde gemeinde = TestDataUtil.createGemeindeLondon(mandant);

			TestDataInstitutionStammdatenBuilder institution =
				new TestDataInstitutionStammdatenBuilder(gesuchsperiode);
			var testFall = new Testfall01_WaeltiDagmar(gesuchsperiode, true, gemeinde, institution);
			testFall.createGesuch(gesuchsperiode.getDatumAktiviert());
			testFall.fillInGesuch();

			Gesuch gesuch = testFall.getGesuch();

			Gesuchsteller gs1 = gesuch.extractGesuchsteller1().orElseThrow();
			Kind kind = gesuch.getKindContainers().stream().findFirst().orElseThrow().getKindJA();

			EWKPerson ewkGs1 = GeresTestUtil.ewkPersonFromEntity(gs1);
			ewkGs1.setAdresse(getGs1Adresse(gesuch));

			expect(geresClient.suchePersonMitAhvNummerInGemeinde(gs1, gemeinde)).andReturn(ewkGs1);
			EWKResultat personenInHaushalt = new EWKResultat();
			personenInHaushalt.getPersonen().add(GeresTestUtil.ewkPersonFromEntity(kind));
			personenInHaushalt.getPersonen()
				.add(GeresTestUtil.ewkPersonFromEntity(gs1)); // GS1 must still only appear once in result
			expect(geresClient.suchePersonenInHaushalt(WOHNUNGS_ID, GEBAEUDE_ID)).andReturn(personenInHaushalt);
			replayAll();

			// when
			EWKResultat ewkResultat = testee.suchePersonen(gesuch);

			// verify
			assertThat(
				ewkResultat.getPersonen(),
				contains(
					allOf(
						hasProperty("vorname", is(gs1.getVorname())),
						hasProperty("personID", is(gs1.getId())),
						hasProperty("nichtGefunden", equalTo(false)),
						hasProperty("gesuchsteller", equalTo(true)),
						hasProperty("kind", equalTo(false))),
					allOf(
						hasProperty("vorname", is(kind.getVorname())),
						hasProperty("personID", is(kind.getId())),
						hasProperty("nichtGefunden", equalTo(false)),
						hasProperty("gesuchsteller", equalTo(false)),
						hasProperty("kind", equalTo(true)))
				));

		}

		@Test
		@DisplayName("muss GS1 und Kind als 'nicht gefunden' zurückgeben")
		void suchePersonenMitNurGs1NichtGefunden()
			throws PersonenSucheServiceBusinessException, PersonenSucheServiceException {
			// given
			Mandant mandant = new Mandant();
			mandant.setMandantIdentifier(MandantIdentifier.SCHWYZ);

			Gesuchsperiode gesuchsperiode = TestDataUtil.createGesuchsperiode1718(mandant);
			Gemeinde gemeinde = TestDataUtil.createGemeindeLondon(mandant);

			TestDataInstitutionStammdatenBuilder institution =
				new TestDataInstitutionStammdatenBuilder(gesuchsperiode);
			var testFall = new Testfall01_WaeltiDagmar(gesuchsperiode, true, gemeinde, institution);
			testFall.createGesuch(gesuchsperiode.getDatumAktiviert());
			testFall.fillInGesuch();

			Gesuch gesuch = testFall.getGesuch();

			Gesuchsteller gs1 = gesuch.extractGesuchsteller1().orElseThrow();
			Kind kind = gesuch.getKindContainers().stream().findFirst().orElseThrow().getKindJA();

			EWKPerson ewkGsPerson = GeresUtil.createNotFoundPerson(gs1);
			expect(geresClient.suchePersonMitAhvNummerInGemeinde(gs1, gemeinde)).andReturn(ewkGsPerson);
			replayAll();

			// when
			EWKResultat ewkResultat = testee.suchePersonen(gesuch);

			// verify
			assertThat(
				ewkResultat.getPersonen(),
				contains(
					allOf(
						hasProperty("vorname", equalTo(gs1.getVorname())),
						hasProperty("nachname", equalTo(gs1.getNachname())),
						hasProperty("nichtGefunden", equalTo(true)),
						hasProperty("gesuchsteller", equalTo(true)),
						hasProperty("kind", equalTo(false))),
					allOf(
						hasProperty("vorname", equalTo(kind.getVorname())),
						hasProperty("nachname", equalTo(kind.getNachname())),
						hasProperty("nichtGefunden", equalTo(true)),
						hasProperty("gesuchsteller", equalTo(false)),
						hasProperty("kind", equalTo(true)))
				)
			);
		}

	}

	private static EWKAdresse getGs1Adresse(Gesuch gesuch) {
		GesuchstellerAdresseContainer adressen = Objects.requireNonNull(gesuch.getGesuchsteller1())
			.getAdressen()
			.stream()
			.filter(gesuchstellerAdresseContainer -> gesuchstellerAdresseContainer.extractAdresseTyp()
				== AdresseTyp.WOHNADRESSE)
			.findFirst()
			.orElseThrow();
		GesuchstellerAdresse adresseJA = Objects.requireNonNull(adressen.getGesuchstellerAdresseJA());
		return GeresTestUtil.ewkAdresseFromEntity(adresseJA, GEBAEUDE_ID, WOHNUNGS_ID);
	}

	@Nested
	@DisplayName("Suche mit zwei GS")
	class MitZweiGs {
		@Test
		@DisplayName("muss GS2 im Haushalt finden, aber Kinder nicht")
		void sucheMit2GsGs2InHaushalt() throws PersonenSucheServiceBusinessException, PersonenSucheServiceException {
			// given
			Mandant mandant = new Mandant();
			mandant.setMandantIdentifier(MandantIdentifier.SCHWYZ);

			Gesuchsperiode gesuchsperiode = TestDataUtil.createGesuchsperiode1718(mandant);
			Gemeinde gemeinde = TestDataUtil.createGemeindeLondon(mandant);

			TestDataInstitutionStammdatenBuilder institution =
				new TestDataInstitutionStammdatenBuilder(gesuchsperiode);
			var testFall = new Testfall02_FeutzYvonne(gesuchsperiode, true, gemeinde, institution);
			testFall.createGesuch(gesuchsperiode.getDatumAktiviert());
			testFall.fillInGesuch();

			Gesuch gesuch = testFall.getGesuch();

			Gesuchsteller gs1 = gesuch.extractGesuchsteller1().orElseThrow();
			EWKPerson ewkGs1 = GeresTestUtil.ewkPersonFromEntity(gs1);
			ewkGs1.setAdresse(getGs1Adresse(gesuch));

			expect(geresClient.suchePersonMitAhvNummerInGemeinde(gs1, gemeinde)).andReturn(ewkGs1);

			Gesuchsteller gs2 = gesuch.extractGesuchsteller2().orElseThrow();
			EWKResultat ewkResultatGs2 = new EWKResultat();
			ewkResultatGs2.getPersonen().add(GeresTestUtil.ewkPersonFromEntity(gs2));
			expect(geresClient.suchePersonenInHaushalt(WOHNUNGS_ID, GEBAEUDE_ID)).andReturn(ewkResultatGs2);

			var kinder = new ArrayList<>(gesuch.getKindContainers());
			Kind kind1 = kinder.get(0).getKindJA();
			Kind kind2 = kinder.get(1).getKindJA();

			replayAll();

			// when
			EWKResultat ewkResultat = testee.suchePersonen(gesuch);

			// verify
			assertThat(
				ewkResultat.getPersonen(),
				contains(
					allOf(
						hasProperty("vorname", is(gs1.getVorname())),
						hasProperty("nachname", is(gs1.getNachname())),
						hasProperty("personID", is(gs1.getId())),
						hasProperty("nichtGefunden", equalTo(false)),
						hasProperty("gesuchsteller", equalTo(true)),
						hasProperty("kind", equalTo(false))),
					allOf(
						hasProperty("vorname", is(kind1.getVorname())),
						hasProperty("nachname", is(kind1.getNachname())),
						hasProperty("nichtGefunden", equalTo(true)),
						hasProperty("kind", equalTo(true))),
					allOf(
						hasProperty("vorname", is(kind2.getVorname())),
						hasProperty("nachname", is(kind2.getNachname())),
						hasProperty("nichtGefunden", equalTo(true)),
						hasProperty("kind", equalTo(true))),
					allOf(
						hasProperty("vorname", is(gs2.getVorname())),
						hasProperty("nachname", is(gs2.getNachname())),
						hasProperty("personID", is(gs2.getId())),
						hasProperty("nichtGefunden", equalTo(false)),
						hasProperty("kind", equalTo(false)))
				));

		}

		@Test
		@DisplayName(
			"muss GS2 per AHV Nr suchen falls nicht im Haushalt gefunden, ein Kind im Haushalt, eines gar nicht")
		void sucheMit2GsGs2NichtInHaushalt()
			throws PersonenSucheServiceBusinessException, PersonenSucheServiceException {
			// given
			Mandant mandant = new Mandant();
			mandant.setMandantIdentifier(MandantIdentifier.SCHWYZ);

			Gesuchsperiode gesuchsperiode = TestDataUtil.createGesuchsperiode1718(mandant);
			Gemeinde gemeinde = TestDataUtil.createGemeindeLondon(mandant);

			TestDataInstitutionStammdatenBuilder institution =
				new TestDataInstitutionStammdatenBuilder(gesuchsperiode);
			var testFall = new Testfall02_FeutzYvonne(gesuchsperiode, true, gemeinde, institution);
			testFall.createGesuch(gesuchsperiode.getDatumAktiviert());
			testFall.fillInGesuch();

			Gesuch gesuch = testFall.getGesuch();

			Gesuchsteller gs1 = gesuch.extractGesuchsteller1().orElseThrow();
			EWKPerson ewkGs1 = GeresTestUtil.ewkPersonFromEntity(gs1);
			ewkGs1.setAdresse(getGs1Adresse(gesuch));

			expect(geresClient.suchePersonMitAhvNummerInGemeinde(gs1, gemeinde)).andReturn(ewkGs1);

			Gesuchsteller gs2 = gesuch.extractGesuchsteller2().orElseThrow();

			EWKPerson ewkGs2 = GeresTestUtil.ewkPersonFromEntity(gs2);
			expect(geresClient.suchePersonMitAhvNummerInGemeinde(gs2, gemeinde)).andReturn(ewkGs2);

			var kinder = new ArrayList<>(gesuch.getKindContainers());
			Kind kind1 = kinder.get(0).getKindJA();
			Kind kind2 = kinder.get(1).getKindJA();

			EWKResultat resultat = new EWKResultat();
			resultat.getPersonen().add(GeresTestUtil.ewkPersonFromEntity(kind2));
			expect(geresClient.suchePersonenInHaushalt(WOHNUNGS_ID, GEBAEUDE_ID)).andReturn(resultat);

			replayAll();

			// when
			EWKResultat ewkResultat = testee.suchePersonen(gesuch);

			// verify
			assertThat(
				ewkResultat.getPersonen(),
				contains(
					allOf(
						hasProperty("vorname", is(gs1.getVorname())),
						hasProperty("nachname", is(gs1.getNachname())),
						hasProperty("personID", is(gs1.getId())),
						hasProperty("nichtGefunden", equalTo(false)),
						hasProperty("gesuchsteller", equalTo(true)),
						hasProperty("kind", equalTo(false))),
					allOf(
						hasProperty("vorname", is(kind1.getVorname())),
						hasProperty("nachname", is(kind1.getNachname())),
						hasProperty("nichtGefunden", equalTo(true)),
						hasProperty("gesuchsteller", equalTo(false)),
						hasProperty("kind", equalTo(true))),
					allOf(
						hasProperty("vorname", is(kind2.getVorname())),
						hasProperty("nachname", is(kind2.getNachname())),
						hasProperty("personID", is(kind2.getId())),
						hasProperty("nichtGefunden", equalTo(false)),
						hasProperty("gesuchsteller", equalTo(false)),
						hasProperty("kind", equalTo(true))),
					allOf(
						hasProperty("vorname", is(gs2.getVorname())),
						hasProperty("nachname", is(gs2.getNachname())),
						hasProperty("personID", is(gs2.getId())),
						hasProperty("nichtGefunden", equalTo(false)),
						hasProperty("gesuchsteller", equalTo(false)),
						hasProperty("kind", equalTo(false)))
				));

		}
	}
}

