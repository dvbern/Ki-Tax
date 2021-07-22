/*
 * Copyright (C) 2021 DV Bern AG, Switzerland
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

package ch.dvbern.ebegu.outbox.anmeldung;

import java.time.LocalDate;
import java.util.Iterator;
import java.util.Objects;

import javax.annotation.Nonnull;

import ch.dvbern.ebegu.entities.Adresse;
import ch.dvbern.ebegu.entities.AdresseTyp;
import ch.dvbern.ebegu.entities.AnmeldungTagesschule;
import ch.dvbern.ebegu.entities.BelegungTagesschuleModul;
import ch.dvbern.ebegu.entities.Betreuung;
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.entities.Gesuchsteller;
import ch.dvbern.ebegu.entities.Kind;
import ch.dvbern.ebegu.enums.BetreuungsangebotTyp;
import ch.dvbern.ebegu.outbox.ExportedEvent;
import ch.dvbern.ebegu.test.TestDataUtil;
import ch.dvbern.kibon.exchange.commons.tagesschulen.AbholungTagesschule;
import ch.dvbern.kibon.exchange.commons.tagesschulen.ModulAuswahlDTO;
import ch.dvbern.kibon.exchange.commons.tagesschulen.TagesschuleAnmeldungDetailsDTO;
import ch.dvbern.kibon.exchange.commons.tagesschulen.TagesschuleAnmeldungEventDTO;
import ch.dvbern.kibon.exchange.commons.tagesschulen.TagesschuleAnmeldungStatus;
import ch.dvbern.kibon.exchange.commons.types.AdresseDTO;
import ch.dvbern.kibon.exchange.commons.types.Geschlecht;
import ch.dvbern.kibon.exchange.commons.types.Gesuchsperiode;
import ch.dvbern.kibon.exchange.commons.types.GesuchstellerDTO;
import ch.dvbern.kibon.exchange.commons.types.Intervall;
import ch.dvbern.kibon.exchange.commons.types.KindDTO;
import ch.dvbern.kibon.exchange.commons.util.AvroConverter;
import com.spotify.hamcrest.pojo.IsPojo;
import org.junit.Test;

import static com.spotify.hamcrest.pojo.IsPojo.pojo;
import static java.util.Objects.requireNonNull;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.is;


public class AnmeldungTagesschuleEventConverterTest {

	private final AnmeldungTagesschuleEventConverter converter = new AnmeldungTagesschuleEventConverter();

	@Test
	public void testAnmeldungTagesschuleAddedEvent() {
		Betreuung betreuung = TestDataUtil.createGesuchWithBetreuungspensum(false);
		Gesuch gesuch = betreuung.extractGesuch();
		gesuch.setFreigabeDatum(LocalDate.now());
		TestDataUtil.createDefaultAdressenForGS(gesuch, false);
		assert gesuch.getGesuchsteller1() != null;
		gesuch.getGesuchsteller1().setGesuchstellerJA(TestDataUtil.createDefaultGesuchsteller());
		betreuung.getInstitutionStammdaten().setBetreuungsangebotTyp(BetreuungsangebotTyp.TAGESSCHULE);
		AnmeldungTagesschule anmeldungTagesschule =
			TestDataUtil.createAnmeldungTagesschuleWithModules(betreuung.getKind(), betreuung.extractGesuchsperiode());
		AnmeldungTagesschuleEvent anmeldungTagesschuleAddedEvent = converter.of(anmeldungTagesschule);

		assertThat(anmeldungTagesschuleAddedEvent, is(pojo(ExportedEvent.class)
			.where(ExportedEvent::getAggregateId, is(anmeldungTagesschule.getBGNummer()))
			.where(ExportedEvent::getAggregateType, is("Anmeldung"))
			.where(ExportedEvent::getType, is("AnmeldungTagesschule")))
		);

		//noinspection deprecation
		TagesschuleAnmeldungEventDTO specificRecord = AvroConverter.fromAvroBinary(
			anmeldungTagesschuleAddedEvent.getSchema(),
			anmeldungTagesschuleAddedEvent.getPayload());

		assertThat(specificRecord, is(pojo(TagesschuleAnmeldungEventDTO.class)
			.where(
				TagesschuleAnmeldungEventDTO::getInstitutionId,
				is(anmeldungTagesschule.getInstitutionStammdaten().getInstitution().getId()))
			.where(
				TagesschuleAnmeldungEventDTO::getStatus,
				is(TagesschuleAnmeldungStatus.SCHULAMT_ANMELDUNG_AUSGELOEST))
			.where(TagesschuleAnmeldungEventDTO::getVersion, is((int) anmeldungTagesschule.getVersion()))
			.where(TagesschuleAnmeldungEventDTO::getFreigegebenAm, is(gesuch.getFreigabeDatum()))
			.where(TagesschuleAnmeldungEventDTO::getAntragstellendePerson, matchesAntragstellendePerson(gesuch))
		.where(TagesschuleAnmeldungEventDTO::getKind, matchesKind(anmeldungTagesschule.getKind().getKindJA()))
			.where(TagesschuleAnmeldungEventDTO::getGesuchsperiode, pojo(Gesuchsperiode.class)
				.where(Gesuchsperiode::getId, is(gesuch.getGesuchsperiode().getId()))
				.where(Gesuchsperiode::getGueltigAb, is(gesuch.getGesuchsperiode().getGueltigkeit().getGueltigAb()))
				.where(Gesuchsperiode::getGueltigBis, is(gesuch.getGesuchsperiode().getGueltigkeit().getGueltigBis())))
			.where(TagesschuleAnmeldungEventDTO::getAnmeldungsDetails, matchesAnmeldungDetails(anmeldungTagesschule))
		));

	}

	@Nonnull
	private IsPojo<TagesschuleAnmeldungDetailsDTO> matchesAnmeldungDetails(AnmeldungTagesschule anmeldungTagesschule) {
		assert anmeldungTagesschule.getBelegungTagesschule() != null;
		assert anmeldungTagesschule.getBelegungTagesschule().getAbholungTagesschule() != null;
		Iterator<BelegungTagesschuleModul>
			iterator = anmeldungTagesschule.getBelegungTagesschule().getBelegungTagesschuleModule().iterator();
		return pojo(TagesschuleAnmeldungDetailsDTO.class)
			.where(TagesschuleAnmeldungDetailsDTO::getRefnr, is(anmeldungTagesschule.getBGNummer()))
			.where(TagesschuleAnmeldungDetailsDTO::getBemerkung, is(anmeldungTagesschule.getBelegungTagesschule().getBemerkung()))
			.where(TagesschuleAnmeldungDetailsDTO::getEintrittsdatum, is(anmeldungTagesschule.getBelegungTagesschule().getEintrittsdatum()))
			.where(TagesschuleAnmeldungDetailsDTO::getPlanKlasse, is(anmeldungTagesschule.getBelegungTagesschule().getPlanKlasse()))
			.where(TagesschuleAnmeldungDetailsDTO::getAbweichungZweitesSemester, is(anmeldungTagesschule.getBelegungTagesschule().isAbweichungZweitesSemester()))
			.where(TagesschuleAnmeldungDetailsDTO::getAbholung, is(AbholungTagesschule.ALLEINE_NACH_HAUSE))
			.where(TagesschuleAnmeldungDetailsDTO::getModulSelection, contains(
				matchesModulAuswahlDTO(iterator.next()),
				matchesModulAuswahlDTO(iterator.next()),
				matchesModulAuswahlDTO(iterator.next()),
				matchesModulAuswahlDTO(iterator.next())
				)
			);
	}

	private IsPojo<ModulAuswahlDTO> matchesModulAuswahlDTO(BelegungTagesschuleModul belegungTagesschuleModul) {
		return pojo(ModulAuswahlDTO.class)
			.where(ModulAuswahlDTO::getModulId, is(belegungTagesschuleModul.getModulTagesschule().getId()))
			.where(ModulAuswahlDTO::getIntervall, is(Intervall.valueOf(belegungTagesschuleModul.getIntervall().name())))
			.where(ModulAuswahlDTO::getWeekday, is(belegungTagesschuleModul.getModulTagesschule().getWochentag().getValue()));
	}

	@Nonnull
	private IsPojo<GesuchstellerDTO> matchesAntragstellendePerson(@Nonnull Gesuch gesuch) {
		assert gesuch.getGesuchsteller1() != null;
		Gesuchsteller gesuchsteller = gesuch.getGesuchsteller1().getGesuchstellerJA();
		Adresse adresse = requireNonNull(gesuch.getGesuchsteller1()
			.getAdressen()
			.stream()
			.filter(gesuchstellerAdresseContainer -> Objects
				.equals(gesuchstellerAdresseContainer.extractAdresseTyp(), AdresseTyp.WOHNADRESSE))
			.findFirst()
			.get()).getGesuchstellerAdresseJA();
		assert adresse != null;
		return pojo(GesuchstellerDTO.class)
			.where(GesuchstellerDTO::getNachname, is(gesuchsteller.getNachname()))
			.where(GesuchstellerDTO::getVorname, is(gesuchsteller.getVorname()))
			.where(GesuchstellerDTO::getEmail, is(gesuchsteller.getMail()))
			.where(GesuchstellerDTO::getGeburtsdatum, is(gesuchsteller.getGeburtsdatum()))
			.where(GesuchstellerDTO::getGeschlecht, is(Geschlecht.MAENNLICH))
			.where(GesuchstellerDTO::getAdresse, pojo(AdresseDTO.class)
				.where(AdresseDTO::getOrt, is(adresse.getOrt()))
				.where(AdresseDTO::getLand, is(adresse.getLand().name()))
				.where(AdresseDTO::getStrasse, is(adresse.getStrasse()))
				.where(AdresseDTO::getHausnummer, is(adresse.getHausnummer()))
				.where(AdresseDTO::getAdresszusatz, is(adresse.getZusatzzeile()))
				.where(AdresseDTO::getPlz, is(adresse.getPlz()))
			);
	}

	@Nonnull
	private IsPojo<KindDTO> matchesKind(Kind kindJA) {
		return pojo(KindDTO.class)
			.where(KindDTO::getNachname, is(kindJA.getNachname()))
			.where(KindDTO::getVorname, is(kindJA.getVorname()))
			.where(KindDTO::getGeburtsdatum, is(kindJA.getGeburtsdatum()))
			.where(KindDTO::getGeschlecht, is(Geschlecht.WEIBLICH))
		;
	}
}
