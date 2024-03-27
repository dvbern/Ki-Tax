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

package ch.dvbern.ebegu.api.converter.gemeinde;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;

import ch.dvbern.ebegu.api.converter.AbstractConverter;
import ch.dvbern.ebegu.api.converter.JaxBenutzerConverter;
import ch.dvbern.ebegu.api.dtos.JaxAbstractDTO;
import ch.dvbern.ebegu.api.dtos.JaxAbstractGemeindeStammdaten;
import ch.dvbern.ebegu.api.dtos.JaxFerieninselZeitraum;
import ch.dvbern.ebegu.api.dtos.JaxGemeindeKonfiguration;
import ch.dvbern.ebegu.api.dtos.JaxGemeindeStammdaten;
import ch.dvbern.ebegu.api.dtos.JaxGemeindeStammdatenGesuchsperiodeFerieninsel;
import ch.dvbern.ebegu.api.dtos.JaxGemeindeStammdatenKorrespondenz;
import ch.dvbern.ebegu.api.dtos.JaxGemeindeStammdatenLite;
import ch.dvbern.ebegu.entities.Adresse;
import ch.dvbern.ebegu.entities.Benutzer;
import ch.dvbern.ebegu.entities.Einstellung;
import ch.dvbern.ebegu.entities.Gemeinde;
import ch.dvbern.ebegu.entities.GemeindeStammdaten;
import ch.dvbern.ebegu.entities.GemeindeStammdatenGesuchsperiodeFerieninsel;
import ch.dvbern.ebegu.entities.GemeindeStammdatenGesuchsperiodeFerieninselZeitraum;
import ch.dvbern.ebegu.entities.Gesuchsperiode;
import ch.dvbern.ebegu.entities.Institution;
import ch.dvbern.ebegu.entities.Mandant;
import ch.dvbern.ebegu.entities.TextRessource;
import ch.dvbern.ebegu.enums.EinstellungKey;
import ch.dvbern.ebegu.enums.KorrespondenzSpracheTyp;
import ch.dvbern.ebegu.i18n.LocaleThreadLocal;
import ch.dvbern.ebegu.services.BenutzerService;
import ch.dvbern.ebegu.services.EinstellungService;
import ch.dvbern.ebegu.services.FerieninselStammdatenService;
import ch.dvbern.ebegu.services.GemeindeService;
import ch.dvbern.ebegu.services.GesuchsperiodeService;
import ch.dvbern.ebegu.services.InstitutionService;
import ch.dvbern.oss.lib.beanvalidation.embeddables.IBAN;

import static java.util.Objects.requireNonNull;

@RequestScoped
public class JaxGemeindeStammdatenConverter extends AbstractConverter {

	@Inject
	private GemeindeService gemeindeService;
	@Inject
	private BenutzerService benutzerService;
	@Inject
	private InstitutionService institutionService;
	@Inject
	private GesuchsperiodeService gesuchsperiodeService;
	@Inject
	private JaxBenutzerConverter jaxBenutzerConverter;
	@Inject
	private EinstellungService einstellungService;
	@Inject
	private FerieninselStammdatenService ferieninselStammdatenService;

	@SuppressWarnings("PMD.NcssMethodCount")
	@Nonnull
	public GemeindeStammdaten gemeindeStammdatenToEntity(
		@Nonnull final JaxGemeindeStammdaten jaxStammdaten,
		@Nonnull final GemeindeStammdaten stammdaten
	) {
		requireNonNull(stammdaten);
		requireNonNull(stammdaten.getAdresse());
		requireNonNull(jaxStammdaten);
		requireNonNull(jaxStammdaten.getGemeinde());
		requireNonNull(jaxStammdaten.getGemeinde().getId());
		requireNonNull(jaxStammdaten.getAdresse());
		requireNonNull(jaxStammdaten.getStandardRechtsmittelbelehrung());
		requireNonNull(jaxStammdaten.getBenachrichtigungBgEmailAuto());
		requireNonNull(jaxStammdaten.getBenachrichtigungTsEmailAuto());
		requireNonNull(jaxStammdaten.getStandardDokSignature());

		convertAbstractFieldsToEntity(jaxStammdaten, stammdaten);

		// Die Gemeinde selbst ändert nicht, nur wieder von der DB lesen
		gemeindeService.findGemeinde(jaxStammdaten.getGemeinde().getId())
			.ifPresent(stammdaten::setGemeinde);

		Mandant mandant = stammdaten.getGemeinde().getMandant();

		if (jaxStammdaten.getDefaultBenutzerBG() != null) {
			benutzerService.findBenutzer(jaxStammdaten.getDefaultBenutzerBG().getUsername(), mandant)
				.ifPresent(stammdaten::setDefaultBenutzerBG);
		}
		if (jaxStammdaten.getDefaultBenutzerTS() != null) {
			benutzerService.findBenutzer(jaxStammdaten.getDefaultBenutzerTS().getUsername(), mandant)
				.ifPresent(stammdaten::setDefaultBenutzerTS);
		}
		if (jaxStammdaten.getDefaultBenutzer() != null) {
			benutzerService.findBenutzer(jaxStammdaten.getDefaultBenutzer().getUsername(), mandant)
				.ifPresent(stammdaten::setDefaultBenutzer);
		}

		if (jaxStammdaten.getGemeindeAusgabestelle() != null) {
			Objects.requireNonNull(jaxStammdaten.getGemeindeAusgabestelle().getId());
			gemeindeService.findGemeinde(jaxStammdaten.getGemeindeAusgabestelle().getId())
				.ifPresent(stammdaten::setGemeindeAusgabestelle);
		} else {
			stammdaten.setGemeindeAusgabestelle(null);
		}

		gemeindeStammdatenAdressenToEntity(jaxStammdaten, stammdaten);
		stammdaten.setMail(jaxStammdaten.getMail());
		stammdaten.setTelefon(jaxStammdaten.getTelefon());
		stammdaten.setWebseite(jaxStammdaten.getWebseite());
		stammdaten.setHasAltGemeindeKontakt(jaxStammdaten.getHasAltGemeindeKontakt());
		stammdaten.setAltGemeindeKontaktText(jaxStammdaten.getAltGemeindeKontaktText());

		if (jaxStammdaten.isKorrespondenzspracheDe() && jaxStammdaten.isKorrespondenzspracheFr()) {
			stammdaten.setKorrespondenzsprache(KorrespondenzSpracheTyp.DE_FR);
		} else if (jaxStammdaten.isKorrespondenzspracheDe()) {
			stammdaten.setKorrespondenzsprache(KorrespondenzSpracheTyp.DE);
		} else if (jaxStammdaten.isKorrespondenzspracheFr()) {
			stammdaten.setKorrespondenzsprache(KorrespondenzSpracheTyp.FR);
		} else {
			throw new IllegalArgumentException("Die Korrespondenzsprache muss gesetzt sein");
		}

		stammdaten.setKontoinhaber(jaxStammdaten.getKontoinhaber());
		stammdaten.setBic(jaxStammdaten.getBic());
		if (jaxStammdaten.getIban() != null) {
			stammdaten.setIban(new IBAN(jaxStammdaten.getIban()));
		}

		stammdaten.setStandardRechtsmittelbelehrung(jaxStammdaten.getStandardRechtsmittelbelehrung());
		stammdaten.setBenachrichtigungBgEmailAuto(jaxStammdaten.getBenachrichtigungBgEmailAuto());
		stammdaten.setBenachrichtigungTsEmailAuto(jaxStammdaten.getBenachrichtigungTsEmailAuto());
		stammdaten.setStandardDokSignature(jaxStammdaten.getStandardDokSignature());

		stammdaten.setStandardDokTitle(jaxStammdaten.getStandardDokTitle());
		stammdaten.setStandardDokUnterschriftTitel(jaxStammdaten.getStandardDokUnterschriftTitel());
		stammdaten.setStandardDokUnterschriftName(jaxStammdaten.getStandardDokUnterschriftName());
		stammdaten.setStandardDokUnterschriftTitel2(jaxStammdaten.getStandardDokUnterschriftTitel2());
		stammdaten.setStandardDokUnterschriftName2(jaxStammdaten.getStandardDokUnterschriftName2());
		stammdaten.setTsVerantwortlicherNachVerfuegungBenachrichtigen(jaxStammdaten.getTsVerantwortlicherNachVerfuegungBenachrichtigen());

		stammdaten.setHasZusatzTextVerfuegung(jaxStammdaten.getHasZusatzTextVerfuegung());
		stammdaten.setZusatzTextVerfuegung(jaxStammdaten.getZusatzTextVerfuegung());
		stammdaten.setHasZusatzTextFreigabequittung(jaxStammdaten.getHasZusatzTextFreigabequittung());
		stammdaten.setZusatzTextFreigabequittung(jaxStammdaten.getZusatzTextFreigabequittung());

		stammdaten.setBgEmail(jaxStammdaten.getBgEmail());
		stammdaten.setBgTelefon(jaxStammdaten.getBgTelefon());
		stammdaten.setTsEmail(jaxStammdaten.getTsEmail());
		stammdaten.setTsTelefon(jaxStammdaten.getTsTelefon());
		stammdaten.setEmailBeiGesuchsperiodeOeffnung(jaxStammdaten.getEmailBeiGesuchsperiodeOeffnung());

		if (jaxStammdaten.getRechtsmittelbelehrung() != null) {
			if (stammdaten.getRechtsmittelbelehrung() == null) {
				stammdaten.setRechtsmittelbelehrung(new TextRessource());
			}
			stammdaten.setRechtsmittelbelehrung(textRessourceToEntity(
				jaxStammdaten.getRechtsmittelbelehrung(),
				stammdaten.getRechtsmittelbelehrung()));
		}

		stammdaten.setUsernameScolaris(jaxStammdaten.getUsernameScolaris());
		stammdaten.setGutscheinSelberAusgestellt(jaxStammdaten.getGutscheinSelberAusgestellt());

		jaxStammdaten.getGemeindeStammdatenKorrespondenz().apply(stammdaten.getGemeindeStammdatenKorrespondenz());

		stammdaten.setAlleBgInstitutionenZugelassen(jaxStammdaten.getAlleBgInstitutionenZugelassen());
		if (jaxStammdaten.getAlleBgInstitutionenZugelassen()) {
			stammdaten.setZugelasseneBgInstitutionen(new ArrayList<>());
		} else {
			var ids = jaxStammdaten.getZugelasseneBgInstitutionen()
				.stream()
				.map(JaxAbstractDTO::getId)
				.collect(Collectors.toList());
			List<Institution> institutionen = institutionService.findAllInstitutionen(ids);
			stammdaten.setZugelasseneBgInstitutionen(institutionen);
		}

		return stammdaten;
	}

	private void gemeindeStammdatenAdressenToEntity(
		@Nonnull JaxGemeindeStammdaten jaxStammdaten,
		@Nonnull GemeindeStammdaten stammdaten
	) {
		adresseToEntity(jaxStammdaten.getAdresse(), stammdaten.getAdresse());

		if (jaxStammdaten.getBgAdresse() != null) {
			if (stammdaten.getBgAdresse() == null) {
				stammdaten.setBgAdresse(new Adresse());
			}
			adresseToEntity(jaxStammdaten.getBgAdresse(), stammdaten.getBgAdresse());
		} else {
			stammdaten.setBgAdresse(null);
		}

		if (jaxStammdaten.getTsAdresse() != null) {
			if (stammdaten.getTsAdresse() == null) {
				stammdaten.setTsAdresse(new Adresse());
			}
			adresseToEntity(jaxStammdaten.getTsAdresse(), stammdaten.getTsAdresse());
		} else {
			stammdaten.setTsAdresse(null);
		}

		if (jaxStammdaten.getBeschwerdeAdresse() != null) {
			if (stammdaten.getBeschwerdeAdresse() == null) {
				stammdaten.setBeschwerdeAdresse(new Adresse());
			}
			adresseToEntity(jaxStammdaten.getBeschwerdeAdresse(), stammdaten.getBeschwerdeAdresse());
		} else {
			stammdaten.setBeschwerdeAdresse(null);
		}
	}

	@SuppressWarnings("PMD.NcssMethodCount")
	public JaxGemeindeStammdaten gemeindeStammdatenToJAX(@Nonnull final GemeindeStammdaten stammdaten) {
		requireNonNull(stammdaten);
		requireNonNull(stammdaten.getGemeinde());
		requireNonNull(stammdaten.getAdresse());
		final JaxGemeindeStammdaten jaxStammdaten = new JaxGemeindeStammdaten();
		abstractGemeindeStammdatenToJax(jaxStammdaten, stammdaten);
		Collection<Benutzer> administratoren = benutzerService.getGemeindeAdministratoren(stammdaten.getGemeinde());
		Collection<Benutzer> sachbearbeiter = benutzerService.getGemeindeSachbearbeiter(stammdaten.getGemeinde());
		jaxStammdaten.setAdministratoren(administratoren.stream()
			.map(Benutzer::getFullName)
			.collect(Collectors.joining(", ")));
		jaxStammdaten.setSachbearbeiter(sachbearbeiter.stream()
			.map(Benutzer::getFullName)
			.collect(Collectors.joining(", ")));
		jaxStammdaten.setGemeinde(gemeindeToJAX(stammdaten.getGemeinde()));
		gemeindeStammdatenToJAXSetKorrespondenzsprache(jaxStammdaten, stammdaten);
		gemeindeStammdatenToJAXSetDefaultBenutzer(jaxStammdaten, stammdaten);
		gemeindeStammdatenZusaetzlicheAdressenToJax(jaxStammdaten, stammdaten);
		jaxStammdaten.setBgTelefon(stammdaten.getBgTelefon());
		jaxStammdaten.setBgEmail(stammdaten.getBgEmail());
		jaxStammdaten.setTsTelefon(stammdaten.getTsTelefon());
		jaxStammdaten.setTsEmail(stammdaten.getTsEmail());
		jaxStammdaten.setEmailBeiGesuchsperiodeOeffnung(stammdaten.getEmailBeiGesuchsperiodeOeffnung());
		jaxStammdaten.setHasZusatzTextVerfuegung(stammdaten.getHasZusatzTextVerfuegung());
		jaxStammdaten.setZusatzTextVerfuegung(stammdaten.getZusatzTextVerfuegung());
		jaxStammdaten.setHasZusatzTextFreigabequittung(stammdaten.getHasZusatzTextFreigabequittung());
		jaxStammdaten.setZusatzTextFreigabequittung(stammdaten.getZusatzTextFreigabequittung());
		jaxStammdaten.setKontoinhaber(stammdaten.getKontoinhaber());
		jaxStammdaten.setBic(stammdaten.getBic());
		if (stammdaten.getIban() != null) {
			jaxStammdaten.setIban(stammdaten.getIban().getIban());
		}

		jaxStammdaten.setStandardRechtsmittelbelehrung(stammdaten.getStandardRechtsmittelbelehrung());
		jaxStammdaten.setBenachrichtigungBgEmailAuto(stammdaten.getBenachrichtigungBgEmailAuto());
		jaxStammdaten.setBenachrichtigungTsEmailAuto(stammdaten.getBenachrichtigungTsEmailAuto());
		jaxStammdaten.setStandardDokSignature(stammdaten.getStandardDokSignature());

		jaxStammdaten.setStandardDokTitle(stammdaten.getStandardDokTitle());
		jaxStammdaten.setStandardDokUnterschriftTitel(stammdaten.getStandardDokUnterschriftTitel());
		jaxStammdaten.setStandardDokUnterschriftName(stammdaten.getStandardDokUnterschriftName());
		jaxStammdaten.setStandardDokUnterschriftTitel2(stammdaten.getStandardDokUnterschriftTitel2());
		jaxStammdaten.setStandardDokUnterschriftName2(stammdaten.getStandardDokUnterschriftName2());
		jaxStammdaten.setTsVerantwortlicherNachVerfuegungBenachrichtigen(stammdaten.getTsVerantwortlicherNachVerfuegungBenachrichtigen());

		if (stammdaten.getRechtsmittelbelehrung() != null) {
			jaxStammdaten.setRechtsmittelbelehrung(textRessourceToJAX(stammdaten.getRechtsmittelbelehrung()));
		}

		jaxStammdaten.setUsernameScolaris(stammdaten.getUsernameScolaris());

		jaxStammdaten.setGutscheinSelberAusgestellt(stammdaten.getGutscheinSelberAusgestellt());
		if (stammdaten.getGemeindeAusgabestelle() != null) {
			jaxStammdaten.setGemeindeAusgabestelle(gemeindeToJAX(stammdaten.getGemeindeAusgabestelle()));
		}

		jaxStammdaten.setGemeindeStammdatenKorrespondenz(JaxGemeindeStammdatenKorrespondenz.from(stammdaten.getGemeindeStammdatenKorrespondenz()));
		convertAbstractFieldsToJAX(
			stammdaten.getGemeindeStammdatenKorrespondenz(),
			jaxStammdaten.getGemeindeStammdatenKorrespondenz());

		jaxStammdaten.setAlleBgInstitutionenZugelassen(stammdaten.getAlleBgInstitutionenZugelassen());
		var jaxInstitutionen = stammdaten.getZugelasseneBgInstitutionen()
			.stream()
			.map(this::institutionToJAX)
			.collect(Collectors.toList());
		jaxStammdaten.setZugelasseneBgInstitutionen(jaxInstitutionen);

		return jaxStammdaten;
	}

	public JaxGemeindeStammdatenLite gemeindeStammdatenLiteToJAX(@Nonnull final GemeindeStammdaten stammdaten) {
		requireNonNull(stammdaten);
		requireNonNull(stammdaten.getGemeinde());
		requireNonNull(stammdaten.getAdresse());
		final JaxGemeindeStammdatenLite jaxStammdaten = new JaxGemeindeStammdatenLite();
		abstractGemeindeStammdatenToJax(jaxStammdaten, stammdaten);
		jaxStammdaten.setGemeindeName(stammdaten.getGemeinde().getName());
		return jaxStammdaten;
	}

	@SuppressWarnings("PMD.UnusedPrivateMethod") // Die Methode ist verwendet
	private void abstractGemeindeStammdatenToJax(
		@Nonnull JaxAbstractGemeindeStammdaten jaxStammdaten,
		@Nonnull GemeindeStammdaten stammdaten) {
		convertAbstractFieldsToJAX(stammdaten, jaxStammdaten);
		jaxStammdaten.setMail(stammdaten.getMail());
		jaxStammdaten.setTelefon(stammdaten.getTelefon());
		jaxStammdaten.setWebseite(stammdaten.getWebseite());
		gemeindeStammdatenToJAXSetKorrespondenzsprache(jaxStammdaten, stammdaten);
		jaxStammdaten.setAdresse(adresseToJAX(stammdaten.getAdresse()));
		jaxStammdaten.setHasAltGemeindeKontakt(stammdaten.getHasAltGemeindeKontakt());
		jaxStammdaten.setAltGemeindeKontaktText(stammdaten.getAltGemeindeKontaktText());
		// Konfiguration: Wir laden die Gesuchsperioden, die vor dem Ende der Gemeinde-Gültigkeit liegen
		Objects.requireNonNull(stammdaten.getGemeinde().getMandant());
		List<Gesuchsperiode> gueltigeGesuchsperiodenForGemeinde =
			gesuchsperiodeService.getAllGesuchsperioden(stammdaten.getGemeinde().getMandant())
				.stream()
				.filter(gesuchsperiode -> gesuchsperiode.getMandant().equals(stammdaten.getGemeinde().getMandant()))
				.filter(gesuchsperiode -> stammdaten.getGemeinde()
					.getGueltigBis()
					.isAfter(gesuchsperiode.getGueltigkeit().getGueltigAb()))
				.collect(Collectors.toList());

		for (Gesuchsperiode gesuchsperiode : gueltigeGesuchsperiodenForGemeinde) {
			jaxStammdaten.getKonfigurationsListe().add(loadGemeindeKonfiguration(
				stammdaten.getGemeinde(),
				gesuchsperiode));
		}
	}

	private void gemeindeStammdatenToJAXSetDefaultBenutzer(
		@Nonnull JaxGemeindeStammdaten jaxStammdaten,
		@Nonnull GemeindeStammdaten stammdaten
	) {
		jaxStammdaten.setBenutzerListeBG(benutzerService.getBenutzerBgOrGemeinde(stammdaten.getGemeinde())
			.stream().map(benutzer -> jaxBenutzerConverter.benutzerToJaxBenutzer(benutzer)).collect(Collectors.toList()));
		jaxStammdaten.setBenutzerListeTS(benutzerService.getBenutzerTsOrGemeinde(stammdaten.getGemeinde())
			.stream().map(benutzer -> jaxBenutzerConverter.benutzerToJaxBenutzer(benutzer)).collect(Collectors.toList()));

		if (!stammdaten.isNew()) {
			if (stammdaten.getDefaultBenutzerBG() != null) {
				jaxStammdaten.setDefaultBenutzerBG(jaxBenutzerConverter.benutzerToJaxBenutzer(stammdaten.getDefaultBenutzerBG()));
			}
			if (stammdaten.getDefaultBenutzerTS() != null) {
				jaxStammdaten.setDefaultBenutzerTS(jaxBenutzerConverter.benutzerToJaxBenutzer(stammdaten.getDefaultBenutzerTS()));
			}
			if (stammdaten.getDefaultBenutzer() != null) {
				jaxStammdaten.setDefaultBenutzer(jaxBenutzerConverter.benutzerToJaxBenutzer(stammdaten.getDefaultBenutzer()));
			}
		}
	}

	private void gemeindeStammdatenZusaetzlicheAdressenToJax(
		@Nonnull JaxGemeindeStammdaten jaxStammdaten,
		@Nonnull GemeindeStammdaten stammdaten
	) {
		if (stammdaten.getBeschwerdeAdresse() != null) {
			jaxStammdaten.setBeschwerdeAdresse(adresseToJAX(stammdaten.getBeschwerdeAdresse()));
		}
		if (stammdaten.getBgAdresse() != null) {
			jaxStammdaten.setBgAdresse(adresseToJAX(stammdaten.getBgAdresse()));
		}
		if (stammdaten.getTsAdresse() != null) {
			jaxStammdaten.setTsAdresse(adresseToJAX(stammdaten.getTsAdresse()));
		}
	}

	private void gemeindeStammdatenToJAXSetKorrespondenzsprache(
		@Nonnull JaxAbstractGemeindeStammdaten jaxStammdaten,
		@Nonnull GemeindeStammdaten stammdaten
	) {
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
	}

	private JaxGemeindeKonfiguration loadGemeindeKonfiguration(
		@Nonnull Gemeinde gemeinde,
		@Nonnull Gesuchsperiode gesuchsperiode) {
		JaxGemeindeKonfiguration konfiguration = new JaxGemeindeKonfiguration();
		konfiguration.setGesuchsperiodeName(gesuchsperiode.getGesuchsperiodeDisplayName(LocaleThreadLocal.get()));
		konfiguration.setGesuchsperiodeStatusName(gesuchsperiode.getGesuchsperiodeStatusName(LocaleThreadLocal.get()));
		konfiguration.setGesuchsperiode(gesuchsperiodeToJAX(gesuchsperiode));
		Map<EinstellungKey, Einstellung> gemeindeKonfigurationMap = einstellungService
			.getGemeindeEinstellungenActiveForMandantOnlyAsMap(gemeinde, gesuchsperiode);
		konfiguration.getKonfigurationen().addAll(gemeindeKonfigurationMap.entrySet().stream()
			.map(x -> einstellungToJAX(x.getValue()))
			.collect(Collectors.toList()));

		Optional<Einstellung> erwerbspensumZuschlagMax =
			einstellungService.getEinstellungByMandant(EinstellungKey.ERWERBSPENSUM_ZUSCHLAG, gesuchsperiode);
		if (erwerbspensumZuschlagMax.isPresent()) {
			konfiguration.setErwerbspensumZuschlagMax(
				erwerbspensumZuschlagMax.get().getValueAsInteger()
			);
		}
		Optional<Einstellung> erwerbspensumMiminumVorschuleMax =
			einstellungService.getEinstellungByMandant(
				EinstellungKey.GEMEINDE_MIN_ERWERBSPENSUM_NICHT_EINGESCHULT,
				gesuchsperiode);
		if (erwerbspensumMiminumVorschuleMax.isPresent()) {
			konfiguration.setErwerbspensumMiminumVorschuleMax(
				erwerbspensumMiminumVorschuleMax.get().getValueAsInteger()
			);
		}
		Optional<Einstellung> erwerbspensumMiminumSchulkinderMax =
			einstellungService.getEinstellungByMandant(
				EinstellungKey.GEMEINDE_MIN_ERWERBSPENSUM_EINGESCHULT,
				gesuchsperiode);
		if (erwerbspensumMiminumSchulkinderMax.isPresent()) {
			konfiguration.setErwerbspensumMiminumSchulkinderMax(
				erwerbspensumMiminumSchulkinderMax.get().getValueAsInteger()
			);
		}

		List<JaxGemeindeStammdatenGesuchsperiodeFerieninsel> ferieninselStammdaten =
			ferieninselStammdatenService.findGesuchsperiodeFerieninselByGemeindeAndPeriode(
					gemeinde.getId(),
					gesuchsperiode.getId())
				.stream()
				.map(this::ferieninselStammdatenToJAX)
				.collect(Collectors.toList());

		konfiguration.setFerieninselStammdaten(ferieninselStammdaten);

		return konfiguration;
	}

	@Nonnull
	public JaxGemeindeStammdatenGesuchsperiodeFerieninsel ferieninselStammdatenToJAX(
		@Nonnull GemeindeStammdatenGesuchsperiodeFerieninsel persistedFerieninselStammdaten) {

		final JaxGemeindeStammdatenGesuchsperiodeFerieninsel jaxGemeindeStammdatenGesuchsperiodeFerieninsel =
			new JaxGemeindeStammdatenGesuchsperiodeFerieninsel();

		convertAbstractVorgaengerFieldsToJAX(
			persistedFerieninselStammdaten,
			jaxGemeindeStammdatenGesuchsperiodeFerieninsel);
		jaxGemeindeStammdatenGesuchsperiodeFerieninsel.setFerienname(persistedFerieninselStammdaten.getFerienname());
		jaxGemeindeStammdatenGesuchsperiodeFerieninsel.setAnmeldeschluss(persistedFerieninselStammdaten.getAnmeldeschluss());
		jaxGemeindeStammdatenGesuchsperiodeFerieninsel.setFerienActive(persistedFerieninselStammdaten.isFerienActive());
		for (GemeindeStammdatenGesuchsperiodeFerieninselZeitraum ferieninselZeitraum :
			persistedFerieninselStammdaten.getZeitraumList()) {
			JaxFerieninselZeitraum jaxFerieninselZeitraum = new JaxFerieninselZeitraum();
			convertAbstractDateRangedFieldsToJAX(ferieninselZeitraum, jaxFerieninselZeitraum);
			jaxGemeindeStammdatenGesuchsperiodeFerieninsel.getZeitraumList().add(jaxFerieninselZeitraum);
		}
		return jaxGemeindeStammdatenGesuchsperiodeFerieninsel;
	}
}
