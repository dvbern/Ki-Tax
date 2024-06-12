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
 */

package ch.dvbern.ebegu.services.kind;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import ch.dvbern.ebegu.entities.Betreuung;
import ch.dvbern.ebegu.entities.Betreuungspensum;
import ch.dvbern.ebegu.entities.BetreuungspensumContainer;
import ch.dvbern.ebegu.entities.Einstellung;
import ch.dvbern.ebegu.entities.ErwerbspensumContainer;
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.entities.Gesuchsperiode;
import ch.dvbern.ebegu.entities.GesuchstellerContainer;
import ch.dvbern.ebegu.entities.InstitutionStammdaten;
import ch.dvbern.ebegu.entities.Kind;
import ch.dvbern.ebegu.entities.KindContainer;
import ch.dvbern.ebegu.enums.EinschulungTyp;
import ch.dvbern.ebegu.enums.EinstellungKey;
import ch.dvbern.ebegu.enums.KinderabzugTyp;
import ch.dvbern.ebegu.enums.betreuung.BetreuungsangebotTyp;
import ch.dvbern.ebegu.enums.betreuung.Betreuungsstatus;
import ch.dvbern.ebegu.services.BetreuungService;
import ch.dvbern.ebegu.services.EinstellungService;
import ch.dvbern.ebegu.services.GesuchstellerService;
import org.easymock.EasyMockExtension;
import org.easymock.EasyMockSupport;
import org.easymock.Mock;
import org.easymock.TestSubject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.EnumSource.Mode;

import static org.easymock.EasyMock.expect;

@ExtendWith(EasyMockExtension.class)
class KindServiceHandlerTest extends EasyMockSupport {

	@TestSubject
	private final KindServiceHandler kindServiceHandler = new KindServiceHandler();
	@Mock
	private EinstellungService einstellungService;
	@Mock
	private BetreuungService betreuungService;

	@Mock
	private GesuchstellerService gesuchstellerService;

	@ParameterizedTest
	@EnumSource(value = KinderabzugTyp.class,
		names = {"SCHWYZ"},
		mode = Mode.EXCLUDE)
	void keinBetreuungsstatusResetOnKindSaveKinderabzugTypNichtSchwyz(KinderabzugTyp kinderabzugTyp) {
		KindContainer kindContainer = prepareKindContainer(EinschulungTyp.PRIMARSTUFE, false);
		Einstellung kinderabzugTypEinstellung = new Einstellung();
		kinderabzugTypEinstellung.setValue(kinderabzugTyp.name());
		expect(einstellungService.getEinstellungByMandant(
			EinstellungKey.KINDERABZUG_TYP,
			kindContainer.getGesuch().getGesuchsperiode())).andReturn(Optional.of(kinderabzugTypEinstellung)).once();
		replayAll();
		kindServiceHandler.resetKindBetreuungenStatusOnKindSave(kindContainer, createKindContainerEinschulungsTyp(EinschulungTyp.VORSCHULALTER));
		Assertions.assertEquals(kindContainer.getBetreuungen().stream().filter(betreuung -> Betreuungsstatus.BESTAETIGT.equals(
			betreuung.getBetreuungsstatus())).collect(Collectors.toList()).size(), kindContainer.getBetreuungen().size());
		verifyAll();
	}

	private KindContainer createKindContainerEinschulungsTyp(EinschulungTyp einschulungTyp) {
		KindContainer dbKindContainer = new KindContainer();
		dbKindContainer.setKindJA(new Kind());
		dbKindContainer.getKindJA().setEinschulungTyp(einschulungTyp);
		return dbKindContainer;
	}

	@ParameterizedTest
	@EnumSource(value = EinschulungTyp.class,
		names = {"PRIMARSTUFE", "SEKUNDAR_UND_HOEHER_STUFE"},
		mode = Mode.INCLUDE)
	void keinBetreuungsstatusResetOnKindSaveMitEinschulungAenderung_von_SCHULSTUFE_to_VORSCHULALTER(EinschulungTyp einschulungTyp) {
		KindContainer kindContainer = prepareKindContainer(EinschulungTyp.VORSCHULALTER, false);
		Einstellung kinderabzugTypEinstellung = new Einstellung();
		kinderabzugTypEinstellung.setValue("SCHWYZ");
		expect(einstellungService.getEinstellungByMandant(
			EinstellungKey.KINDERABZUG_TYP,
			kindContainer.getGesuch().getGesuchsperiode())).andReturn(Optional.of(kinderabzugTypEinstellung)).once();
		replayAll();
		kindServiceHandler.resetKindBetreuungenStatusOnKindSave(kindContainer, createKindContainerEinschulungsTyp(einschulungTyp));
		Assertions.assertEquals(kindContainer.getBetreuungen().stream().filter(betreuung -> Betreuungsstatus.BESTAETIGT.equals(
			betreuung.getBetreuungsstatus())).collect(Collectors.toList()).size(), kindContainer.getBetreuungen().size());
		verifyAll();
	}

	@ParameterizedTest
	@EnumSource(value = EinschulungTyp.class,
		names = {"PRIMARSTUFE", "SEKUNDAR_UND_HOEHER_STUFE"},
		mode = Mode.INCLUDE)
	void resetKindBetreuungenStatusOnKindSaveMitEinschulungAenderung_von_VORSCHULALTER_to_SCHULSTUFE(EinschulungTyp einschulungTyp) {
		KindContainer kindContainer = prepareKindContainer(einschulungTyp, false);
		Einstellung kinderabzugTyp = new Einstellung();
		kinderabzugTyp.setValue("SCHWYZ");
		expect(einstellungService.getEinstellungByMandant(
			EinstellungKey.KINDERABZUG_TYP,
			kindContainer.getGesuch().getGesuchsperiode())).andReturn(Optional.of(kinderabzugTyp)).once();
		expect(betreuungService.saveBetreuung(kindContainer.getBetreuungen().stream().findFirst().get(), false, null)).andReturn(
			kindContainer.getBetreuungen().stream().findFirst().get()).once();
		replayAll();
		kindServiceHandler.resetKindBetreuungenStatusOnKindSave(kindContainer, createKindContainerEinschulungsTyp(EinschulungTyp.VORSCHULALTER));
		Assertions.assertEquals(kindContainer.getBetreuungen().stream().filter(betreuung -> Betreuungsstatus.WARTEN.equals(
			betreuung.getBetreuungsstatus())).collect(Collectors.toList()).size(), kindContainer.getBetreuungen().size());
		verifyAll();
	}

	@Test
	void keinBetreuungsstatusResetOnKindSaveKeineEinschulungAenderung() {
		KindContainer kindContainer = prepareKindContainer(EinschulungTyp.VORSCHULALTER, false);
		Einstellung kinderabzugTyp = new Einstellung();
		kinderabzugTyp.setValue("SCHWYZ");
		expect(einstellungService.getEinstellungByMandant(
			EinstellungKey.KINDERABZUG_TYP,
			kindContainer.getGesuch().getGesuchsperiode())).andReturn(Optional.of(kinderabzugTyp)).once();
		replayAll();
		kindServiceHandler.resetKindBetreuungenStatusOnKindSave(kindContainer, createKindContainerEinschulungsTyp(EinschulungTyp.VORSCHULALTER));
		List<Betreuung> bestaetigteBetreuungen = kindContainer.getBetreuungen().stream().filter(betreuung -> Betreuungsstatus.BESTAETIGT.equals(
			betreuung.getBetreuungsstatus())).collect(Collectors.toList());
		Assertions.assertEquals(bestaetigteBetreuungen.size(), kindContainer.getBetreuungen().size());
		verifyAll();
	}

	@ParameterizedTest
	@EnumSource(value = KinderabzugTyp.class,
		names = {"SCHWYZ"},
		mode = Mode.EXCLUDE)
	void keinResetKindBetreuungenpensenFragenOnKindSaveKinderabzugTypNichtSchwyz(KinderabzugTyp kinderabzugTyp) {
		KindContainer kindContainer = prepareKindContainer(EinschulungTyp.PRIMARSTUFE, false);
		Einstellung kinderabzugTypEinstellung = new Einstellung();
		kinderabzugTypEinstellung.setValue(kinderabzugTyp.name());
		expect(einstellungService.getEinstellungByMandant(
			EinstellungKey.KINDERABZUG_TYP,
			kindContainer.getGesuch().getGesuchsperiode())).andReturn(Optional.of(kinderabzugTypEinstellung)).once();
		replayAll();
		kindServiceHandler.resetKindBetreuungenDatenOnKindSave(kindContainer, createKindContainerEinschulungsTyp(EinschulungTyp.VORSCHULALTER));
		Assertions.assertNotNull(kindContainer.getBetreuungen()
			.stream()
			.findFirst()
			.get()
			.getBetreuungspensumContainers()
			.stream()
			.findFirst()
			.get()
			.getBetreuungspensumJA()
			.getBetreuungInFerienzeit());
		verifyAll();
	}

	@Test
	void resetKindBetreuungenpensenFragenOnKindSaveMitEinschulungAenderung_von_SCHULSTUFE_to_VORSCHULALTER() {
		KindContainer kindContainer = prepareKindContainer(EinschulungTyp.VORSCHULALTER, false);
		Einstellung kinderabzugTyp = new Einstellung();
		kinderabzugTyp.setValue("SCHWYZ");
		expect(einstellungService.getEinstellungByMandant(
			EinstellungKey.KINDERABZUG_TYP,
			kindContainer.getGesuch().getGesuchsperiode())).andReturn(Optional.of(kinderabzugTyp)).once();
		replayAll();
		kindServiceHandler.resetKindBetreuungenDatenOnKindSave(kindContainer, createKindContainerEinschulungsTyp(EinschulungTyp.PRIMARSTUFE));
		verifyAll();
		Assertions.assertNull(kindContainer.getBetreuungen()
			.stream()
			.findFirst()
			.get()
			.getBetreuungspensumContainers()
			.stream()
			.findFirst()
			.get()
			.getBetreuungspensumJA()
			.getBetreuungInFerienzeit());
	}

	@Test
	void keinResetVonKindBetreuungenpensenFragenOnKindSaveMitEinschulungAenderung_von_VORSCHULALTER_to_PRIMARSTUFE() {
		KindContainer kindContainer = prepareKindContainer(EinschulungTyp.PRIMARSTUFE, false);
		Einstellung kinderabzugTyp = new Einstellung();
		kinderabzugTyp.setValue("SCHWYZ");
		expect(einstellungService.getEinstellungByMandant(
			EinstellungKey.KINDERABZUG_TYP,
			kindContainer.getGesuch().getGesuchsperiode())).andReturn(Optional.of(kinderabzugTyp)).once();
		replayAll();
		kindServiceHandler.resetKindBetreuungenDatenOnKindSave(kindContainer, createKindContainerEinschulungsTyp(EinschulungTyp.VORSCHULALTER));
		verifyAll();
		Assertions.assertNotNull(kindContainer.getBetreuungen()
			.stream()
			.findFirst()
			.get()
			.getBetreuungspensumContainers()
			.stream()
			.findFirst()
			.get()
			.getBetreuungspensumJA()
			.getBetreuungInFerienzeit());
	}

	@Test
	void keinResetVonGesuchDataOnKindSaveWhenGemeinsam() {
		KindContainer kindContainer = prepareKindContainer(EinschulungTyp.VORSCHULALTER, true);
		Einstellung abhaengigkeitAmspruchBeschaeftigungpensumEinstellung = new Einstellung();
		abhaengigkeitAmspruchBeschaeftigungpensumEinstellung.setValue("SCHWYZ");
		expect(einstellungService.getEinstellungByMandant(
			EinstellungKey.ABHAENGIGKEIT_ANSPRUCH_BESCHAEFTIGUNGPENSUM,
			kindContainer.getGesuch().getGesuchsperiode())).andReturn(Optional.of(
			abhaengigkeitAmspruchBeschaeftigungpensumEinstellung)).once();
		replayAll();
		kindServiceHandler.resetGesuchDataOnKindSave(kindContainer);
		verifyAll();
		Assertions.assertEquals(1, kindContainer.getGesuch().getGesuchsteller2().getErwerbspensenContainers().size());
	}

	@Test
	void resetVonGesuchDataOnKindSaveWhenNichtGemeinsam() {
		KindContainer kindContainer = prepareKindContainer(EinschulungTyp.VORSCHULALTER, false);
		Einstellung abhaengigkeitAmspruchBeschaeftigungpensumEinstellung = new Einstellung();
		abhaengigkeitAmspruchBeschaeftigungpensumEinstellung.setValue("SCHWYZ");
		expect(einstellungService.getEinstellungByMandant(
			EinstellungKey.ABHAENGIGKEIT_ANSPRUCH_BESCHAEFTIGUNGPENSUM,
			kindContainer.getGesuch().getGesuchsperiode())).andReturn(Optional.of(
			abhaengigkeitAmspruchBeschaeftigungpensumEinstellung)).once();
		expect(gesuchstellerService.saveGesuchsteller(
			kindContainer.getGesuch().getGesuchsteller2(),
			kindContainer.getGesuch(),
			2,
			false)).andReturn(
			kindContainer.getGesuch().getGesuchsteller2()).once();
		replayAll();
		kindServiceHandler.resetGesuchDataOnKindSave(kindContainer);
		verifyAll();
		Assertions.assertEquals(0, kindContainer.getGesuch().getGesuchsteller2().getErwerbspensenContainers().size());
	}

	private KindContainer prepareKindContainer(EinschulungTyp einschulungTyp, boolean gemeinsam) {
		// Kind
		KindContainer kindContainer = new KindContainer();
		Kind kind = new Kind();
		kind.setGemeinsamesGesuch(gemeinsam);
		kind.setEinschulungTyp(einschulungTyp);
		kindContainer.setKindJA(kind);
		kindContainer.setBetreuungen(new HashSet<>());
		// Gesuch
		Gesuch gesuch = new Gesuch();
		gesuch.setGesuchsperiode(new Gesuchsperiode());
		gesuch.getKindContainers().add(kindContainer);
		kindContainer.setGesuch(gesuch);
		// Betreuung
		Betreuung betreuung = new Betreuung();
		betreuung.setBetreuungsstatus(Betreuungsstatus.BESTAETIGT);
		betreuung.setBetreuungspensumContainers(new HashSet<>());
		BetreuungspensumContainer betreuungspensumContainer = new BetreuungspensumContainer();
		Betreuungspensum betreuungspensum = new Betreuungspensum();
		betreuungspensum.setBetreuungInFerienzeit(true);
		betreuungspensumContainer.setBetreuungspensumJA(betreuungspensum);
		betreuung.getBetreuungspensumContainers().add(betreuungspensumContainer);
		betreuung.setInstitutionStammdaten(new InstitutionStammdaten());
		betreuung.getInstitutionStammdaten().setBetreuungsangebotTyp(BetreuungsangebotTyp.KITA);
		kindContainer.getBetreuungen().add(betreuung);
		// Gesuchsteller 2
		GesuchstellerContainer gesuchstellerContainer = new GesuchstellerContainer();
		gesuchstellerContainer.setErwerbspensenContainers(new HashSet<>());
		gesuchstellerContainer.getErwerbspensenContainers().add(new ErwerbspensumContainer());
		kindContainer.getGesuch().setGesuchsteller2(gesuchstellerContainer);
		return kindContainer;
	}
}
