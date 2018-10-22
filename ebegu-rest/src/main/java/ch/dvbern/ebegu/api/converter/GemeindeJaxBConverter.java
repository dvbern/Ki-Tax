/*
 * Copyright (C) 2018 DV Bern AG, Switzerland
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

package ch.dvbern.ebegu.api.converter;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;

import ch.dvbern.ebegu.api.dtos.JaxEinstellung;
import ch.dvbern.ebegu.api.dtos.JaxGemeinde;
import ch.dvbern.ebegu.api.dtos.JaxGemeindeKonfiguration;
import ch.dvbern.ebegu.api.dtos.JaxGemeindeStammdaten;
import ch.dvbern.ebegu.entities.Adresse;
import ch.dvbern.ebegu.entities.Benutzer;
import ch.dvbern.ebegu.entities.Einstellung;
import ch.dvbern.ebegu.entities.Gemeinde;
import ch.dvbern.ebegu.entities.GemeindeStammdaten;
import ch.dvbern.ebegu.entities.Gesuchsperiode;
import ch.dvbern.ebegu.enums.EinschulungTyp;
import ch.dvbern.ebegu.enums.EinstellungKey;
import ch.dvbern.ebegu.enums.GemeindeStatus;
import ch.dvbern.ebegu.enums.GesuchsperiodeStatus;
import ch.dvbern.ebegu.enums.KorrespondenzSpracheTyp;
import ch.dvbern.ebegu.services.BenutzerService;
import ch.dvbern.ebegu.services.EinstellungService;
import ch.dvbern.ebegu.services.GemeindeService;
import ch.dvbern.ebegu.services.GesuchsperiodeService;
import ch.dvbern.ebegu.util.StreamsUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static java.util.Objects.requireNonNull;

@RequestScoped
public class GemeindeJaxBConverter extends AbstractConverter {

	private static final Logger LOGGER = LoggerFactory.getLogger(GemeindeJaxBConverter.class);

	@Inject
	private JaxBConverter converter;
	@Inject
	private BenutzerService benutzerService;
	@Inject
	private GemeindeService gemeindeService;
	@Inject
	private GesuchsperiodeService gesuchsperiodeService;
	@Inject
	private EinstellungService einstellungService;

	@Nonnull
	public Gemeinde gemeindeToEntity(@Nonnull final JaxGemeinde jaxGemeinde, @Nonnull final Gemeinde gemeinde) {
		requireNonNull(gemeinde);
		requireNonNull(jaxGemeinde);
		requireNonNull(jaxGemeinde.getBetreuungsgutscheineStartdatum());
		convertAbstractFieldsToEntity(jaxGemeinde, gemeinde);
		gemeinde.setName(jaxGemeinde.getName());
		gemeinde.setStatus(jaxGemeinde.getStatus());
		gemeinde.setGemeindeNummer(jaxGemeinde.getGemeindeNummer());
		gemeinde.setBfsNummer(jaxGemeinde.getBfsNummer());
		if (jaxGemeinde.getBetreuungsgutscheineStartdatum() != null) {
			gemeinde.setBetreuungsgutscheineStartdatum(jaxGemeinde.getBetreuungsgutscheineStartdatum());
		}

		return gemeinde;
	}

	public JaxGemeinde gemeindeToJAX(@Nonnull final Gemeinde persistedGemeinde) {
		final JaxGemeinde jaxGemeinde = new JaxGemeinde();
		convertAbstractFieldsToJAX(persistedGemeinde, jaxGemeinde);
		jaxGemeinde.setName(persistedGemeinde.getName());
		jaxGemeinde.setStatus(persistedGemeinde.getStatus());
		jaxGemeinde.setGemeindeNummer(persistedGemeinde.getGemeindeNummer());
		jaxGemeinde.setBfsNummer(persistedGemeinde.getBfsNummer());
		jaxGemeinde.setBetreuungsgutscheineStartdatum(persistedGemeinde.getBetreuungsgutscheineStartdatum());

		return jaxGemeinde;
	}

	@Nonnull
	public Set<Gemeinde> gemeindeListToEntity(
		@Nonnull Set<JaxGemeinde> jaxGemeindeList,
		@Nonnull Set<Gemeinde> gemeindeList) {

		final Set<Gemeinde> transformedGemeindeList = new TreeSet<>();
		for (final JaxGemeinde jaxGemeinde : jaxGemeindeList) {
			final Gemeinde gemeindeToMergeWith = gemeindeList
				.stream()
				.filter(existingGemeinde -> existingGemeinde.getId().equalsIgnoreCase(jaxGemeinde.getId()))
				.reduce(StreamsUtil.toOnlyElement())
				.orElse(new Gemeinde());
			final Gemeinde gemeindeToAdd = gemeindeToEntity(jaxGemeinde, gemeindeToMergeWith);
			final boolean added = transformedGemeindeList.add(gemeindeToAdd);
			if (!added) {
				LOGGER.warn(JaxBConverter.DROPPED_DUPLICATE_CONTAINER + "{}", gemeindeToAdd);
			}
		}

		return transformedGemeindeList;
	}

	@Nonnull
	public GemeindeStammdaten gemeindeStammdatenToEntity(@Nonnull final JaxGemeindeStammdaten jaxStammdaten, @Nonnull final GemeindeStammdaten stammdaten) {
		requireNonNull(stammdaten);
		requireNonNull(stammdaten.getAdresse());
		requireNonNull(jaxStammdaten);
		requireNonNull(jaxStammdaten.getGemeinde());
		requireNonNull(jaxStammdaten.getGemeinde().getId());
		requireNonNull(jaxStammdaten.getAdresse());

		convertAbstractFieldsToEntity(jaxStammdaten, stammdaten);

		if (jaxStammdaten.getDefaultBenutzerBG() != null) {
			benutzerService.findBenutzer(jaxStammdaten.getDefaultBenutzerBG().getUsername())
				.ifPresent(stammdaten::setDefaultBenutzerBG);
		}
		if (jaxStammdaten.getDefaultBenutzerTS() != null) {
			benutzerService.findBenutzer(jaxStammdaten.getDefaultBenutzerTS().getUsername())
				.ifPresent(stammdaten::setDefaultBenutzerTS);
		}

		// Die Gemeinde selbst ändert nicht, nur wieder von der DB lesen
		gemeindeService.findGemeinde(jaxStammdaten.getGemeinde().getId())
			.ifPresent(stammdaten::setGemeinde);

		converter.adresseToEntity(jaxStammdaten.getAdresse(), stammdaten.getAdresse());

		if (jaxStammdaten.getBeschwerdeAdresse() != null) {
			if (stammdaten.getBeschwerdeAdresse() == null) {
				stammdaten.setBeschwerdeAdresse(new Adresse());
			}
			converter.adresseToEntity(jaxStammdaten.getBeschwerdeAdresse(), stammdaten.getBeschwerdeAdresse());
		}
		stammdaten.setKeineBeschwerdeAdresse(jaxStammdaten.isKeineBeschwerdeAdresse());
		stammdaten.setMail(jaxStammdaten.getMail());
		stammdaten.setTelefon(jaxStammdaten.getTelefon());
		stammdaten.setWebseite(jaxStammdaten.getWebseite());

		if (jaxStammdaten.isKorrespondenzspracheDe() && jaxStammdaten.isKorrespondenzspracheFr()) {
			stammdaten.setKorrespondenzsprache(KorrespondenzSpracheTyp.DE_FR);
		} else if (jaxStammdaten.isKorrespondenzspracheDe()) {
			stammdaten.setKorrespondenzsprache(KorrespondenzSpracheTyp.DE);
		} else if (jaxStammdaten.isKorrespondenzspracheFr()) {
			stammdaten.setKorrespondenzsprache(KorrespondenzSpracheTyp.FR);
		} else {
			throw new IllegalArgumentException("Die Korrespondenzsprache muss gesetzt sein");
		}

		// Konfiguration
		// Die Gemeindekonfigurationen kann nur in folgenden Fällen bearbeitet werden:
		// - wenn die Gesuchsperiode im Status "Entwurf" ist
		// - wenn die Gemeinde im Status "Eingeladen" ist
		boolean eingeladen = GemeindeStatus.EINGELADEN.equals(jaxStammdaten.getGemeinde().getStatus());
		for (JaxGemeindeKonfiguration konfiguraion : jaxStammdaten.getKonfigurationsListe()) {
			if (eingeladen || GesuchsperiodeStatus.ENTWURF.equals(konfiguraion.getGesuchsperiodeStatus())) {
				saveGemeindeKonfiguration(stammdaten.getGemeinde(), konfiguraion);
			}
		}
		return stammdaten;
	}

	public JaxGemeindeStammdaten gemeindeStammdatenToJAX(@Nonnull final GemeindeStammdaten stammdaten) {
		requireNonNull(stammdaten);
		requireNonNull(stammdaten.getGemeinde());
		requireNonNull(stammdaten.getAdresse());

		final JaxGemeindeStammdaten jaxStammdaten = new JaxGemeindeStammdaten();
		convertAbstractFieldsToJAX(stammdaten, jaxStammdaten);
		Collection<Benutzer> administratoren = benutzerService.getGemeindeAdministratoren(stammdaten.getGemeinde());
		Collection<Benutzer> sachbearbeiter = benutzerService.getGemeindeSachbearbeiter(stammdaten.getGemeinde());
		jaxStammdaten.setAdministratoren(administratoren.stream().map(Benutzer::getFullName).collect(Collectors.joining(", ")));
		jaxStammdaten.setSachbearbeiter(sachbearbeiter.stream().map(Benutzer::getFullName).collect(Collectors.joining(", ")));
		jaxStammdaten.setGemeinde(gemeindeToJAX(stammdaten.getGemeinde()));
		jaxStammdaten.setAdresse(converter.adresseToJAX(stammdaten.getAdresse()));
		jaxStammdaten.setMail(stammdaten.getMail());
		jaxStammdaten.setTelefon(stammdaten.getTelefon());
		jaxStammdaten.setWebseite(stammdaten.getWebseite());
		jaxStammdaten.setKeineBeschwerdeAdresse(stammdaten.isKeineBeschwerdeAdresse());

		if (KorrespondenzSpracheTyp.DE == stammdaten.getKorrespondenzsprache()) {
			jaxStammdaten.setKorrespondenzspracheDe(true);
			jaxStammdaten.setKorrespondenzspracheFr(false);
		} else if (KorrespondenzSpracheTyp.FR == stammdaten.getKorrespondenzsprache()) {
			jaxStammdaten.setKorrespondenzspracheDe(false);
			jaxStammdaten.setKorrespondenzspracheFr(true);
		} else if (KorrespondenzSpracheTyp.DE_FR == stammdaten.getKorrespondenzsprache()) {
			jaxStammdaten.setKorrespondenzspracheDe(true);
			jaxStammdaten.setKorrespondenzspracheFr(true);
		}
		jaxStammdaten.setBenutzerListeBG(benutzerService.getBenutzerBgOrGemeinde(stammdaten.getGemeinde())
			.stream().map(converter::benutzerToJaxBenutzer).collect(Collectors.toList()));
		jaxStammdaten.setBenutzerListeTS(benutzerService.getBenutzerTsOrGemeinde(stammdaten.getGemeinde())
			.stream().map(converter::benutzerToJaxBenutzer).collect(Collectors.toList()));

		if (!stammdaten.isNew()) {
			if (stammdaten.getDefaultBenutzerBG() != null) {
				jaxStammdaten.setDefaultBenutzerBG(converter.benutzerToJaxBenutzer(stammdaten.getDefaultBenutzerBG()));
			}
			if (stammdaten.getDefaultBenutzerTS() != null) {
				jaxStammdaten.setDefaultBenutzerTS(converter.benutzerToJaxBenutzer(stammdaten.getDefaultBenutzerTS()));
			}
			if (stammdaten.getBeschwerdeAdresse() != null) {
				jaxStammdaten.setBeschwerdeAdresse(converter.adresseToJAX(stammdaten.getBeschwerdeAdresse()));
			}
		}

		// Konfiguration
		if (GemeindeStatus.EINGELADEN.equals(stammdaten.getGemeinde().getStatus())) {
			// Ist die Gemeinde noch im Status EINGELADEN, laden wir nur die Konfiguration der richtigen Gesuchsperiode
			// Die Gesuchsperiode wo das BEGU Startdatum drin liett, falls diese bereits existert,
			// falss diese nicht existiert, nehmen wir die aktuelle Gesuchsperiode
			Optional<Gesuchsperiode> gpBeguStart = gesuchsperiodeService.getGesuchsperiodeAm(stammdaten.getGemeinde().getBetreuungsgutscheineStartdatum());
			Optional<Gesuchsperiode> gpAktuell = gesuchsperiodeService.getGesuchsperiodeAm(LocalDate.now());
			Gesuchsperiode gesuchsperiode = null;
			if (gpBeguStart.isPresent()) {
				gesuchsperiode = gpBeguStart.get();
			} else if (gpAktuell.isPresent()) {
				gesuchsperiode = gpAktuell.get();
			}
			if (gesuchsperiode != null) {
				jaxStammdaten.getKonfigurationsListe().add(loadGemeindeKonfiguration(stammdaten.getGemeinde(), gesuchsperiode));
			}
		} else {
			// Ist die Gemeinde noch im Status AKTIV, laden wir die Konfigurationen aller Gesuchsperioden
			for (Gesuchsperiode gesuchsperiode : gesuchsperiodeService.getAllGesuchsperioden()) {
				jaxStammdaten.getKonfigurationsListe().add(loadGemeindeKonfiguration(stammdaten.getGemeinde(), gesuchsperiode));
			}
		}
		return jaxStammdaten;
	}

	private JaxGemeindeKonfiguration loadGemeindeKonfiguration(@Nonnull Gemeinde gemeinde, @Nonnull Gesuchsperiode gesuchsperiode) {
		JaxGemeindeKonfiguration konfiguration = new JaxGemeindeKonfiguration();
		konfiguration.setGesuchsperiodeName(gesuchsperiode.getGesuchsperiodeDisplayName());
		konfiguration.setGesuchsperiodeId(gesuchsperiode.getId());
		konfiguration.setGesuchsperiodeStatus(gesuchsperiode.getStatus());

		Map<EinstellungKey, Einstellung> konfigurationMap = einstellungService.getAllEinstellungenByGemeindeAsMap(gemeinde, gesuchsperiode);
		for (Map.Entry<EinstellungKey, Einstellung> entry : konfigurationMap.entrySet()) {
			if (EinstellungKey.BG_BIS_UND_MIT_SCHULSTUFE.equals(entry.getKey())
				|| EinstellungKey.KONTINGENTIERUNG_ENABLED.equals(entry.getKey())) { // nur gemeindespezifische Einstellungen
				konfiguration.getKonfigurationen().add(converter.einstellungToJAX(entry.getValue()));
			}
		}
		return konfiguration;
	}

	private void saveGemeindeKonfiguration(@Nonnull Gemeinde gemeinde, @Nonnull JaxGemeindeKonfiguration konfiguration) {
		if (konfiguration.getGesuchsperiodeId() != null) {
			Optional<Gesuchsperiode> gesuchsperiode = gesuchsperiodeService.findGesuchsperiode(konfiguration.getGesuchsperiodeId());
			if (gesuchsperiode.isPresent()) {
				for (JaxEinstellung jaxKonfig : konfiguration.getKonfigurationen()) {
					Einstellung einstellung = einstellungService.findEinstellung(jaxKonfig.getKey(), gemeinde, gesuchsperiode.get());
					if (!gemeinde.equals(einstellung.getGemeinde()) || !gesuchsperiode.get().equals(einstellung.getGesuchsperiode())) {
						einstellung = new Einstellung();
						einstellung.setKey(jaxKonfig.getKey());
						einstellung.setGemeinde(gemeinde);
						einstellung.setGesuchsperiode(gesuchsperiode.get());
					}
					einstellung.setValue(jaxKonfig.getValue());
					einstellungService.saveEinstellung(einstellung);
				}
			}
		}
	}
}
