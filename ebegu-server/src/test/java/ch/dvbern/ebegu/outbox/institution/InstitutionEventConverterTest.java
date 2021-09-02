/*
 * Copyright (C) 2019 DV Bern AG, Switzerland
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

package ch.dvbern.ebegu.outbox.institution;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.EnumSet;

import javax.annotation.Nonnull;

import ch.dvbern.ebegu.entities.Adresse;
import ch.dvbern.ebegu.entities.Betreuungsstandort;
import ch.dvbern.ebegu.entities.EinstellungenTagesschule;
import ch.dvbern.ebegu.entities.Institution;
import ch.dvbern.ebegu.entities.InstitutionStammdaten;
import ch.dvbern.ebegu.entities.InstitutionStammdatenBetreuungsgutscheine;
import ch.dvbern.ebegu.entities.InstitutionStammdatenTagesschule;
import ch.dvbern.ebegu.entities.KontaktAngaben;
import ch.dvbern.ebegu.entities.ModulTagesschuleGroup;
import ch.dvbern.ebegu.outbox.ExportedEvent;
import ch.dvbern.ebegu.test.TestDataUtil;
import ch.dvbern.ebegu.types.DateRange;
import ch.dvbern.ebegu.util.Constants;
import ch.dvbern.kibon.exchange.commons.institution.AltersKategorie;
import ch.dvbern.kibon.exchange.commons.institution.GemeindeDTO;
import ch.dvbern.kibon.exchange.commons.institution.InstitutionEventDTO;
import ch.dvbern.kibon.exchange.commons.institution.InstitutionStatus;
import ch.dvbern.kibon.exchange.commons.institution.KontaktAngabenDTO;
import ch.dvbern.kibon.exchange.commons.tagesschulen.ModulDTO;
import ch.dvbern.kibon.exchange.commons.tagesschulen.TagesschuleModuleDTO;
import ch.dvbern.kibon.exchange.commons.types.BetreuungsangebotTyp;
import ch.dvbern.kibon.exchange.commons.types.Wochentag;
import ch.dvbern.kibon.exchange.commons.util.AvroConverter;
import ch.dvbern.kibon.exchange.commons.util.TimeConverter;
import com.spotify.hamcrest.pojo.IsPojo;
import org.hamcrest.Matcher;
import org.junit.jupiter.api.Test;

import static com.spotify.hamcrest.pojo.IsPojo.pojo;
import static java.util.Objects.requireNonNull;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.comparesEqualTo;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;

public class InstitutionEventConverterTest {

	@Nonnull
	private final InstitutionEventConverter converter = new InstitutionEventConverter();

	@Test
	public void testChangedEvent() {
		InstitutionStammdaten institutionStammdaten = TestDataUtil.createDefaultInstitutionStammdaten();
		Institution institution = institutionStammdaten.getInstitution();
		institutionStammdaten.getGueltigkeit().setGueltigBis(Constants.END_OF_TIME);

		InstitutionStammdatenBetreuungsgutscheine bgStammdaten =
			requireNonNull(institutionStammdaten.getInstitutionStammdatenBetreuungsgutscheine());

		Betreuungsstandort betreuungsstandort = createBetreuungsstandort();
		bgStammdaten.getBetreuungsstandorte().add(betreuungsstandort);

		bgStammdaten.setOeffnungsTage(EnumSet.of(DayOfWeek.THURSDAY, DayOfWeek.MONDAY));
		bgStammdaten.setOffenVon(LocalTime.of(7, 0));
		bgStammdaten.setOffenBis(LocalTime.of(18, 0));
		bgStammdaten.setOeffnungsAbweichungen("Freitag bieten wir auf Wunsch auch eine Betreuung an.");
		bgStammdaten.setAlterskategorieBaby(true);
		bgStammdaten.setAlterskategorieVorschule(true);
		bgStammdaten.setSubventioniertePlaetze(false);
		bgStammdaten.setAnzahlPlaetze(BigDecimal.TEN);
		bgStammdaten.setAnzahlPlaetzeFirmen(BigDecimal.ONE);

		InstitutionChangedEvent event = converter.of(institutionStammdaten);

		assertThat(event, is(pojo(ExportedEvent.class)
			.where(ExportedEvent::getAggregateId, is(institution.getId()))
			.where(ExportedEvent::getAggregateType, is("Institution"))
			.where(ExportedEvent::getType, is("InstitutionChanged")))
		);

		//noinspection deprecation
		InstitutionEventDTO specificRecord = AvroConverter.fromAvroBinary(event.getSchema(), event.getPayload());

		assertThat(specificRecord, is(pojo(InstitutionEventDTO.class)
			.where(InstitutionEventDTO::getId, is(institution.getId()))
			.where(InstitutionEventDTO::getName, is(institution.getName()))
			.where(InstitutionEventDTO::getTraegerschaft, is(requireNonNull(institution.getTraegerschaft()).getName()))
			.where(InstitutionEventDTO::getStatus, is(InstitutionStatus.valueOf(institution.getStatus().name())))
			.where(
				InstitutionEventDTO::getBetreuungsGutscheineAb,
				is(institutionStammdaten.getGueltigkeit().getGueltigAb()))
			// END_OF_TIME shall be converted to NULL
			.where(InstitutionEventDTO::getBetreuungsGutscheineBis, nullValue())
			.where(
				InstitutionEventDTO::getBetreuungsArt,
				is(BetreuungsangebotTyp.valueOf(institutionStammdaten.getBetreuungsangebotTyp().name())))
			.where(InstitutionEventDTO::getAdresse, matchesKontaktAngaben(institutionStammdaten))
			.where(InstitutionEventDTO::getBetreuungsAdressen, contains(
				// implicitly, the institution address is also a betreuungs address
				matchesKontaktAngaben(institutionStammdaten),
				matchesKontaktAngaben(betreuungsstandort)
			))
			.where(InstitutionEventDTO::getOeffnungsTage, contains(Wochentag.MONDAY, Wochentag.THURSDAY))
			.where(InstitutionEventDTO::getOffenVon, is(TimeConverter.serialize(bgStammdaten.getOffenVon())))
			.where(InstitutionEventDTO::getOffenBis, is(TimeConverter.serialize(bgStammdaten.getOffenBis())))
			.where(InstitutionEventDTO::getOeffnungsAbweichungen, is(bgStammdaten.getOeffnungsAbweichungen()))
			.where(InstitutionEventDTO::getAltersKategorien, containsInAnyOrder(
				AltersKategorie.BABY,
				AltersKategorie.VORSCHULE)
			)
			.where(InstitutionEventDTO::getSubventioniertePlaetze, is(bgStammdaten.getSubventioniertePlaetze()))
			.where(InstitutionEventDTO::getAnzahlPlaetze, comparesEqualTo(bgStammdaten.getAnzahlPlaetze()))
			.where(InstitutionEventDTO::getAnzahlPlaetzeFirmen, comparesEqualTo(bgStammdaten.getAnzahlPlaetzeFirmen()))
			.where(InstitutionEventDTO::getTimestampMutiert, is(notNullValue()))
		));
	}

	@Test
	public void testDeletedEvent() {
		InstitutionStammdaten institutionStammdaten = TestDataUtil.createDefaultInstitutionStammdaten();
		Institution institution = institutionStammdaten.getInstitution();
		InstitutionChangedEvent event = converter.deleteEvent(institutionStammdaten);

		assertThat(event, is(pojo(ExportedEvent.class)
			.where(ExportedEvent::getAggregateId, is(institution.getId()))
			.where(ExportedEvent::getAggregateType, is("Institution"))
			.where(ExportedEvent::getType, is("InstitutionChanged")))
		);

		//noinspection deprecation
		InstitutionEventDTO specificRecord = AvroConverter.fromAvroBinary(event.getSchema(), event.getPayload());

		assertThat(specificRecord, is(pojo(InstitutionEventDTO.class)
			.where(InstitutionEventDTO::getStatus, is(InstitutionStatus.DELETED))));
	}

	@Nonnull
	private IsPojo<KontaktAngabenDTO> matchesKontaktAngaben(@Nonnull KontaktAngaben kontaktAngaben) {
		Adresse adresse = kontaktAngaben.getAdresse();

		return pojo(KontaktAngabenDTO.class)
			.where(KontaktAngabenDTO::getAnschrift, is(adresse.getOrganisation()))
			.where(KontaktAngabenDTO::getStrasse, is(adresse.getStrasse()))
			.where(KontaktAngabenDTO::getHausnummer, is(adresse.getHausnummer()))
			.where(KontaktAngabenDTO::getAdresszusatz, is(adresse.getZusatzzeile()))
			.where(KontaktAngabenDTO::getPlz, is(adresse.getPlz()))
			.where(KontaktAngabenDTO::getOrt, is(adresse.getOrt()))
			.where(KontaktAngabenDTO::getLand, is(adresse.getLand().name()))
			.where(KontaktAngabenDTO::getGemeinde, pojo(GemeindeDTO.class)
				.where(GemeindeDTO::getName, is(adresse.getGemeinde()))
				.where(GemeindeDTO::getBfsNummer, is(nullValue()))
			)
			.where(KontaktAngabenDTO::getEmail, is(kontaktAngaben.getMail()))
			.where(KontaktAngabenDTO::getTelefon, is(kontaktAngaben.getTelefon()))
			.where(KontaktAngabenDTO::getWebseite, is(kontaktAngaben.getWebseite()));
	}

	@Nonnull
	private Betreuungsstandort createBetreuungsstandort() {
		Betreuungsstandort betreuungsstandort = new Betreuungsstandort();
		betreuungsstandort.setAdresse(TestDataUtil.createDefaultAdresse());

		betreuungsstandort.setMail("hallo@kibon.ch");
		betreuungsstandort.setTelefon("031 111 11 11");
		betreuungsstandort.setWebseite("https://www.kibon.ch");

		return betreuungsstandort;
	}

	@Test
	public void testTagesschuleChangedEvent() {
		InstitutionStammdaten institutionStammdaten =
			TestDataUtil.createInstitutionStammdatenTagesschuleBern(TestDataUtil.createGesuchsperiode1718());
		Institution institution = institutionStammdaten.getInstitution();
		InstitutionChangedEvent event = converter.of(institutionStammdaten);

		assertThat(event, is(pojo(ExportedEvent.class)
			.where(ExportedEvent::getAggregateId, is(institution.getId()))
			.where(ExportedEvent::getAggregateType, is("Institution"))
			.where(ExportedEvent::getType, is("InstitutionChanged")))
		);

		//noinspection deprecation
		InstitutionEventDTO specificRecord = AvroConverter.fromAvroBinary(event.getSchema(), event.getPayload());

		assertThat(specificRecord, is(pojo(InstitutionEventDTO.class)
			.where(InstitutionEventDTO::getId, is(institution.getId()))
			.where(InstitutionEventDTO::getName, is(institution.getName()))
			.where(InstitutionEventDTO::getStatus, is(InstitutionStatus.valueOf(institution.getStatus().name())))
			.where(InstitutionEventDTO::getTagesschuleModule, contains(
				matchesTagesschuleModule(requireNonNull(institutionStammdaten.getInstitutionStammdatenTagesschule()))
			))
		));
	}

	@Nonnull
	private Matcher<TagesschuleModuleDTO>[] matchesTagesschuleModule(
		@Nonnull InstitutionStammdatenTagesschule stammdaten) {
		//noinspection unchecked
		return stammdaten.getEinstellungenTagesschule()
			.stream()
			.map(this::moduleMatcher)
			.toArray(Matcher[]::new);
	}

	@Nonnull
	private Matcher<TagesschuleModuleDTO> moduleMatcher(@Nonnull EinstellungenTagesschule einstellungen) {
		DateRange gueltigkeit = einstellungen.getGesuchsperiode().getGueltigkeit();

		return pojo(TagesschuleModuleDTO.class)
			.where(TagesschuleModuleDTO::getPeriodeVon, is(gueltigkeit.getGueltigAb()))
			.where(TagesschuleModuleDTO::getPeriodeBis, is(gueltigkeit.getGueltigBis()))
			.where(TagesschuleModuleDTO::getModule, containsInAnyOrder(
				moduleForEinstellung(einstellungen)
			));
	}

	@Nonnull
	private Matcher<ModulDTO>[] moduleForEinstellung(@Nonnull EinstellungenTagesschule einstellungen) {
		//noinspection unchecked
		return einstellungen.getModulTagesschuleGroups().stream()
			.map(this::isModul)
			.toArray(Matcher[]::new);
	}

	@Nonnull
	private Matcher<ModulDTO> isModul(@Nonnull ModulTagesschuleGroup group) {
		return pojo(ModulDTO.class)
			.where(ModulDTO::getId, is(group.getId()));
	}
}
