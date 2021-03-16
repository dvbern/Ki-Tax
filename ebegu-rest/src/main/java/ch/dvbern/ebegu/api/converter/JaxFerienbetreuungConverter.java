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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package ch.dvbern.ebegu.api.converter;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;

import ch.dvbern.ebegu.api.dtos.JaxGemeinde;
import ch.dvbern.ebegu.api.dtos.gemeindeantrag.JaxFerienbetreuungAngaben;
import ch.dvbern.ebegu.api.dtos.gemeindeantrag.JaxFerienbetreuungAngabenAngebot;
import ch.dvbern.ebegu.api.dtos.gemeindeantrag.JaxFerienbetreuungAngabenContainer;
import ch.dvbern.ebegu.api.dtos.gemeindeantrag.JaxFerienbetreuungAngabenKostenEinnahmen;
import ch.dvbern.ebegu.api.dtos.gemeindeantrag.JaxFerienbetreuungAngabenNutzung;
import ch.dvbern.ebegu.api.dtos.gemeindeantrag.JaxFerienbetreuungAngabenStammdaten;
import ch.dvbern.ebegu.api.dtos.gemeindeantrag.JaxFerienbetreuungDokument;
import ch.dvbern.ebegu.entities.Adresse;
import ch.dvbern.ebegu.entities.Auszahlungsdaten;
import ch.dvbern.ebegu.entities.Gemeinde;
import ch.dvbern.ebegu.entities.gemeindeantrag.FerienbetreuungAngaben;
import ch.dvbern.ebegu.entities.gemeindeantrag.FerienbetreuungAngabenAngebot;
import ch.dvbern.ebegu.entities.gemeindeantrag.FerienbetreuungAngabenContainer;
import ch.dvbern.ebegu.entities.gemeindeantrag.FerienbetreuungAngabenKostenEinnahmen;
import ch.dvbern.ebegu.entities.gemeindeantrag.FerienbetreuungAngabenNutzung;
import ch.dvbern.ebegu.entities.gemeindeantrag.FerienbetreuungAngabenStammdaten;
import ch.dvbern.ebegu.entities.gemeindeantrag.FerienbetreuungDokument;
import ch.dvbern.ebegu.enums.ErrorCodeEnum;
import ch.dvbern.ebegu.errors.EbeguRuntimeException;
import ch.dvbern.ebegu.services.GemeindeService;
import ch.dvbern.oss.lib.beanvalidation.embeddables.IBAN;

@RequestScoped
public class JaxFerienbetreuungConverter extends AbstractConverter {

	@Inject
	private GemeindeService gemeindeService;

	@Nonnull
	public FerienbetreuungAngabenContainer ferienbetreuungenAngabenContainerToEntity(
		@Nonnull JaxFerienbetreuungAngabenContainer jaxContainer,
		@Nonnull FerienbetreuungAngabenContainer container
	) {
		convertAbstractFieldsToEntity(jaxContainer, container);

		// never set status, gemeinde and gesuchsperiode from client

		container.setAngabenDeklaration(ferienbetreuungenAngabenToEntity(
			jaxContainer.getAngabenDeklaration(),
			container.getAngabenDeklaration()
		));

		if (container.getAngabenKorrektur() != null && jaxContainer.getAngabenKorrektur() != null) {
			container.setAngabenKorrektur(ferienbetreuungenAngabenToEntity(
				jaxContainer.getAngabenKorrektur(),
				container.getAngabenDeklaration()
			));
		}

		container.setInternerKommentar(jaxContainer.getInternerKommentar());
		return container;
	}


	@Nonnull
	private FerienbetreuungAngaben ferienbetreuungenAngabenToEntity(
		@Nonnull JaxFerienbetreuungAngaben jaxContainer,
		@Nonnull FerienbetreuungAngaben ferienbetreuungAngaben
	) {
		convertAbstractFieldsToEntity(jaxContainer, ferienbetreuungAngaben);

		// stammdaten
		ferienbetreuungAngabenStammdatenToEntity(
			jaxContainer.getStammdaten(),
			ferienbetreuungAngaben.getFerienbetreuungAngabenStammdaten()
		);
		// angebot
		ferienbetreuungAngabenAngebotToEntity(
			jaxContainer.getAngebot(),
			ferienbetreuungAngaben.getFerienbetreuungAngabenAngebot()
		);
		// nutzung
		ferienbetreuungAngabenNutzungToEntity(
			jaxContainer.getNutzung(),
			ferienbetreuungAngaben.getFerienbetreuungAngabenNutzung()
		);
		// kosten und einnahmen
		ferienbetreuungAngabenKostenEinnahmenToEntity(
			jaxContainer.getKostenEinnahmen(),
			ferienbetreuungAngaben.getFerienbetreuungAngabenKostenEinnahmen()
		);

		// never save resultate from client

		return ferienbetreuungAngaben;

	}

	private void ferienbetreuungAngabenStammdatenToEntity(
		@Nonnull JaxFerienbetreuungAngabenStammdaten jaxStammdaten,
		@Nonnull FerienbetreuungAngabenStammdaten stammdaten
	) {
		if (jaxStammdaten.getAmAngebotBeteiligteGemeinden() != null) {
			Set<Gemeinde> gemeinden = gemeindeListToEntity(jaxStammdaten.getAmAngebotBeteiligteGemeinden());
			stammdaten.setAmAngebotBeteiligteGemeinden(gemeinden);
		} else {
			stammdaten.setAmAngebotBeteiligteGemeinden(Collections.emptySet());
		}
		stammdaten.setSeitWannFerienbetreuungen(jaxStammdaten.getSeitWannFerienbetreuungen());
		stammdaten.setTraegerschaft(jaxStammdaten.getTraegerschaft());
		if (jaxStammdaten.getStammdatenAdresse() != null) {
			if (stammdaten.getStammdatenAdresse() == null) {
				stammdaten.setStammdatenAdresse(new Adresse());
			}
			stammdaten.setStammdatenAdresse(
				adresseToEntity(jaxStammdaten.getStammdatenAdresse(), stammdaten.getStammdatenAdresse())
			);
		}
		stammdaten.setStammdatenKontaktpersonVorname(jaxStammdaten.getStammdatenKontaktpersonVorname());
		stammdaten.setStammdatenKontaktpersonNachname(jaxStammdaten.getStammdatenKontaktpersonNachname());
		stammdaten.setStammdatenKontaktpersonFunktion(jaxStammdaten.getStammdatenKontaktpersonFunktion());
		stammdaten.setStammdatenKontaktpersonTelefon(jaxStammdaten.getStammdatenKontaktpersonTelefon());
		stammdaten.setStammdatenKontaktpersonEmail(jaxStammdaten.getStammdatenKontaktpersonEmail());
		if (jaxStammdaten.getIban() != null && jaxStammdaten.getKontoinhaber() != null) {
			Auszahlungsdaten auszahlungsdaten = stammdaten.getAuszahlungsdaten();
			if (auszahlungsdaten == null) {
				auszahlungsdaten = new Auszahlungsdaten();
			}
			auszahlungsdaten.setIban(new IBAN(jaxStammdaten.getIban()));
			auszahlungsdaten.setKontoinhaber(jaxStammdaten.getKontoinhaber());
			if (jaxStammdaten.getAdresseKontoinhaber() != null) {
				Adresse adresse = auszahlungsdaten.getAdresseKontoinhaber();
				if (adresse == null) {
					adresse = new Adresse();
				}
				auszahlungsdaten.setAdresseKontoinhaber(adresseToEntity(jaxStammdaten.getAdresseKontoinhaber(), adresse));
			}
		}
		stammdaten.setVermerkAuszahlung(jaxStammdaten.getVermerkAuszahlung());
	}

	private void ferienbetreuungAngabenAngebotToEntity(
		@Nonnull JaxFerienbetreuungAngabenAngebot jaxAngebot,
		@Nonnull FerienbetreuungAngabenAngebot angebot
	) {

		angebot.setAngebot(jaxAngebot.getAngebot());
		angebot.setAngebotKontaktpersonVorname(jaxAngebot.getAngebotKontaktpersonVorname());
		angebot.setAngebotKontaktpersonNachname(jaxAngebot.getAngebotKontaktpersonNachname());
		if (jaxAngebot.getAngebotAdresse() != null) {
			if (angebot.getAngebotAdresse() == null) {
				angebot.setAngebotAdresse(new Adresse());
			}
			angebot.setAngebotAdresse(
				adresseToEntity(jaxAngebot.getAngebotAdresse(), angebot.getAngebotAdresse())
			);
		}
		angebot.setAnzahlFerienwochenHerbstferien(jaxAngebot.getAnzahlFerienwochenHerbstferien());
		angebot.setAnzahlFerienwochenWinterferien(jaxAngebot.getAnzahlFerienwochenWinterferien());
		angebot.setAnzahlFerienwochenFruehlingsferien(jaxAngebot.getAnzahlFerienwochenFruehlingsferien());
		angebot.setAnzahlFerienwochenSommerferien(jaxAngebot.getAnzahlFerienwochenSommerferien());
		angebot.setAnzahlTage(jaxAngebot.getAnzahlTage());
		angebot.setAnzahlStundenProBetreuungstag(jaxAngebot.getAnzahlStundenProBetreuungstag());
		angebot.setBetreuungErfolgtTagsueber(jaxAngebot.getBetreuungErfolgtTagsueber());
		angebot.setBemerkungenOeffnungszeiten(jaxAngebot.getBemerkungenOeffnungszeiten());

		if (jaxAngebot.getFinanziellBeteiligteGemeinden() != null) {
			Set<Gemeinde> gemeinden = gemeindeListToEntity(jaxAngebot.getFinanziellBeteiligteGemeinden());
			angebot.setFinanziellBeteiligteGemeinden(gemeinden);
		} else {
			angebot.setFinanziellBeteiligteGemeinden(Collections.emptySet());
		}

		angebot.setGemeindeFuehrtAngebotSelber(jaxAngebot.getGemeindeFuehrtAngebotSelber());
		angebot.setGemeindeBeauftragtExterneAnbieter(jaxAngebot.getGemeindeBeauftragtExterneAnbieter());
		angebot.setAngebotVereineUndPrivateIntegriert(jaxAngebot.getAngebotVereineUndPrivateIntegriert());
		angebot.setBemerkungenKooperation(jaxAngebot.getBemerkungenKooperation());
		angebot.setLeitungDurchPersonMitAusbildung(jaxAngebot.getLeitungDurchPersonMitAusbildung());
		angebot.setBetreuungDurchPersonenMitErfahrung(jaxAngebot.getBetreuungDurchPersonenMitErfahrung());
		angebot.setAnzahlKinderAngemessen(jaxAngebot.getAnzahlKinderAngemessen());
		angebot.setBetreuungsschluessel(jaxAngebot.getBetreuungsschluessel());
		angebot.setBemerkungenPersonal(jaxAngebot.getBemerkungenPersonal());
		angebot.setFixerTarifKinderDerGemeinde(jaxAngebot.getFixerTarifKinderDerGemeinde());
		angebot.setEinkommensabhaengigerTarifKinderDerGemeinde(jaxAngebot.getEinkommensabhaengigerTarifKinderDerGemeinde());
		angebot.setTagesschuleTarifGiltFuerFerienbetreuung(jaxAngebot.getTagesschuleTarifGiltFuerFerienbetreuung());
		angebot.setFerienbetreuungTarifWirdAusTagesschuleTarifAbgeleitet(jaxAngebot.getFerienbetreuungTarifWirdAusTagesschuleTarifAbgeleitet());
		angebot.setKinderAusAnderenGemeindenZahlenAnderenTarif(jaxAngebot.getKinderAusAnderenGemeindenZahlenAnderenTarif());
		angebot.setBemerkungenTarifsystem(jaxAngebot.getBemerkungenTarifsystem());
	}

	private void ferienbetreuungAngabenNutzungToEntity(
		@Nonnull JaxFerienbetreuungAngabenNutzung jaxNutzung,
		@Nonnull FerienbetreuungAngabenNutzung nutzung
	) {
		nutzung.setAnzahlBetreuungstageKinderBern(jaxNutzung.getAnzahlBetreuungstageKinderBern());
		nutzung.setBetreuungstageKinderDieserGemeinde(jaxNutzung.getBetreuungstageKinderDieserGemeinde());
		nutzung.setBetreuungstageKinderDieserGemeindeSonderschueler(jaxNutzung.getBetreuungstageKinderDieserGemeindeSonderschueler());
		nutzung.setDavonBetreuungstageKinderAndererGemeinden(jaxNutzung.getDavonBetreuungstageKinderAndererGemeinden());
		nutzung.setDavonBetreuungstageKinderAndererGemeindenSonderschueler(jaxNutzung.getDavonBetreuungstageKinderAndererGemeindenSonderschueler());
		nutzung.setAnzahlBetreuteKinder(jaxNutzung.getAnzahlBetreuteKinder());
		nutzung.setAnzahlBetreuteKinderSonderschueler(jaxNutzung.getAnzahlBetreuteKinderSonderschueler());
		nutzung.setAnzahlBetreuteKinder1Zyklus(jaxNutzung.getAnzahlBetreuteKinder1Zyklus());
		nutzung.setAnzahlBetreuteKinder2Zyklus(jaxNutzung.getAnzahlBetreuteKinder2Zyklus());
		nutzung.setAnzahlBetreuteKinder3Zyklus(jaxNutzung.getAnzahlBetreuteKinder3Zyklus());
	}

	private void ferienbetreuungAngabenKostenEinnahmenToEntity(
		@Nonnull JaxFerienbetreuungAngabenKostenEinnahmen jaxKostenEinnahmen,
		@Nonnull FerienbetreuungAngabenKostenEinnahmen kostenEinnahmen
	) {
		kostenEinnahmen.setPersonalkosten(jaxKostenEinnahmen.getPersonalkosten());
		kostenEinnahmen.setPersonalkostenLeitungAdmin(jaxKostenEinnahmen.getPersonalkostenLeitungAdmin());
		kostenEinnahmen.setSachkosten(jaxKostenEinnahmen.getSachkosten());
		kostenEinnahmen.setVerpflegungskosten(jaxKostenEinnahmen.getVerpflegungskosten());
		kostenEinnahmen.setWeitereKosten(jaxKostenEinnahmen.getWeitereKosten());
		kostenEinnahmen.setBemerkungenKosten(jaxKostenEinnahmen.getBemerkungenKosten());
		kostenEinnahmen.setElterngebuehren(jaxKostenEinnahmen.getElterngebuehren());
		kostenEinnahmen.setWeitereEinnahmen(jaxKostenEinnahmen.getWeitereEinnahmen());
	}

	@Nonnull
	public JaxFerienbetreuungAngabenContainer ferienbetreuungAngabenContainerToJax(
		@Nonnull final FerienbetreuungAngabenContainer container
	) {
		JaxFerienbetreuungAngabenContainer jaxContainer = new JaxFerienbetreuungAngabenContainer();
		convertAbstractFieldsToJAX(container, jaxContainer);

		jaxContainer.setStatus(container.getStatus());
		jaxContainer.setGemeinde(gemeindeToJAX(container.getGemeinde()));
		jaxContainer.setGesuchsperiode(gesuchsperiodeToJAX(container.getGesuchsperiode()));
		jaxContainer.setAngabenDeklaration(ferienbetreuungAngabenToJax(container.getAngabenDeklaration()));
		if (container.getAngabenKorrektur() != null) {
			jaxContainer.setAngabenKorrektur(ferienbetreuungAngabenToJax(container.getAngabenKorrektur()));
		}
		jaxContainer.setInternerKommentar(container.getInternerKommentar());

		return jaxContainer;
	}

	@Nonnull
	private JaxFerienbetreuungAngaben ferienbetreuungAngabenToJax(
		@Nonnull final FerienbetreuungAngaben ferienbetreuungAngaben
	) {
		JaxFerienbetreuungAngaben jaxFerienbetreuungAngaben = new JaxFerienbetreuungAngaben();
		convertAbstractFieldsToJAX(ferienbetreuungAngaben, jaxFerienbetreuungAngaben);

		// stammdaten
		JaxFerienbetreuungAngabenStammdaten jaxStammdaten = ferienbetreuungAngabenStammdatenToJax(
			ferienbetreuungAngaben.getFerienbetreuungAngabenStammdaten()
		);
		jaxFerienbetreuungAngaben.setStammdaten(jaxStammdaten);

		// angebot
		JaxFerienbetreuungAngabenAngebot angebot = ferienbetreuungAngabenAngebotToJax(
			ferienbetreuungAngaben.getFerienbetreuungAngabenAngebot()
		);
		jaxFerienbetreuungAngaben.setAngebot(angebot);

		// nutzung
		JaxFerienbetreuungAngabenNutzung nutzung = ferienbetreuungAngabenNutzungToJax(
			ferienbetreuungAngaben.getFerienbetreuungAngabenNutzung()
		);
		jaxFerienbetreuungAngaben.setNutzung(nutzung);

		// kosten und einnahmen
		JaxFerienbetreuungAngabenKostenEinnahmen kostenEinnahmen = ferienbetreuungAngabenKostenEinnahmenToJax(
			ferienbetreuungAngaben.getFerienbetreuungAngabenKostenEinnahmen()
		);
		jaxFerienbetreuungAngaben.setKostenEinnahmen(kostenEinnahmen);

		// resultate
		jaxFerienbetreuungAngaben.setGemeindebeitrag(ferienbetreuungAngaben.getGemeindebeitrag());
		jaxFerienbetreuungAngaben.setKantonsbeitrag(ferienbetreuungAngaben.getKantonsbeitrag());

		return jaxFerienbetreuungAngaben;

	}

	@Nonnull
	private JaxFerienbetreuungAngabenStammdaten ferienbetreuungAngabenStammdatenToJax(
		@Nonnull FerienbetreuungAngabenStammdaten stammdaten
	) {
		JaxFerienbetreuungAngabenStammdaten jaxStammdaten = new JaxFerienbetreuungAngabenStammdaten();

		List<JaxGemeinde> jaxGemeinden = gemeindeListToJax(stammdaten.getAmAngebotBeteiligteGemeinden());

		jaxStammdaten.setAmAngebotBeteiligteGemeinden(jaxGemeinden);
		jaxStammdaten.setTraegerschaft(stammdaten.getTraegerschaft());
		if (stammdaten.getStammdatenAdresse() != null) {
			jaxStammdaten.setStammdatenAdresse(adresseToJAX(stammdaten.getStammdatenAdresse()));
		}
		jaxStammdaten.setStammdatenKontaktpersonVorname(stammdaten.getStammdatenKontaktpersonVorname());
		jaxStammdaten.setStammdatenKontaktpersonNachname(stammdaten.getStammdatenKontaktpersonNachname());
		jaxStammdaten.setStammdatenKontaktpersonFunktion(stammdaten.getStammdatenKontaktpersonFunktion());
		jaxStammdaten.setStammdatenKontaktpersonTelefon(stammdaten.getStammdatenKontaktpersonTelefon());
		jaxStammdaten.setStammdatenKontaktpersonEmail(stammdaten.getStammdatenKontaktpersonEmail());
		if (stammdaten.getAuszahlungsdaten() != null) {
			jaxStammdaten.setIban(stammdaten.getAuszahlungsdaten().getIban().getIban());
			jaxStammdaten.setKontoinhaber(stammdaten.getAuszahlungsdaten().getKontoinhaber());
			if (stammdaten.getAuszahlungsdaten().getAdresseKontoinhaber() != null) {
				jaxStammdaten.setAdresseKontoinhaber(
					adresseToJAX(stammdaten.getAuszahlungsdaten().getAdresseKontoinhaber())
				);
			}
		}
		jaxStammdaten.setVermerkAuszahlung(stammdaten.getVermerkAuszahlung());

		return jaxStammdaten;
	}

	@Nonnull
	private JaxFerienbetreuungAngabenAngebot ferienbetreuungAngabenAngebotToJax(
		@Nonnull FerienbetreuungAngabenAngebot ferienbetreuungAngaben
	) {
		JaxFerienbetreuungAngabenAngebot jaxAngebot = new JaxFerienbetreuungAngabenAngebot();
		jaxAngebot.setAngebot(ferienbetreuungAngaben.getAngebot());
		jaxAngebot.setAngebotKontaktpersonVorname(ferienbetreuungAngaben.getAngebotKontaktpersonVorname());
		jaxAngebot.setAngebotKontaktpersonNachname(ferienbetreuungAngaben.getAngebotKontaktpersonNachname());
		if (ferienbetreuungAngaben.getAngebotAdresse() != null) {
			jaxAngebot.setAngebotAdresse(
				adresseToJAX(ferienbetreuungAngaben.getAngebotAdresse())
			);
		}
		jaxAngebot.setAnzahlFerienwochenHerbstferien(ferienbetreuungAngaben.getAnzahlFerienwochenHerbstferien());
		jaxAngebot.setAnzahlFerienwochenWinterferien(ferienbetreuungAngaben.getAnzahlFerienwochenWinterferien());
		jaxAngebot.setAnzahlFerienwochenFruehlingsferien(ferienbetreuungAngaben.getAnzahlFerienwochenFruehlingsferien());
		jaxAngebot.setAnzahlFerienwochenSommerferien(ferienbetreuungAngaben.getAnzahlFerienwochenSommerferien());
		jaxAngebot.setAnzahlTage(ferienbetreuungAngaben.getAnzahlTage());
		jaxAngebot.setAnzahlStundenProBetreuungstag(ferienbetreuungAngaben.getAnzahlStundenProBetreuungstag());
		jaxAngebot.setBetreuungErfolgtTagsueber(ferienbetreuungAngaben.getBetreuungErfolgtTagsueber());
		jaxAngebot.setBemerkungenOeffnungszeiten(ferienbetreuungAngaben.getBemerkungenOeffnungszeiten());

		List<JaxGemeinde> jaxGemeinden = gemeindeListToJax(ferienbetreuungAngaben.getFinanziellBeteiligteGemeinden());
		jaxAngebot.setFinanziellBeteiligteGemeinden(jaxGemeinden);

		jaxAngebot.setGemeindeFuehrtAngebotSelber(ferienbetreuungAngaben.getGemeindeFuehrtAngebotSelber());
		jaxAngebot.setGemeindeBeauftragtExterneAnbieter(ferienbetreuungAngaben.getGemeindeBeauftragtExterneAnbieter());
		jaxAngebot.setAngebotVereineUndPrivateIntegriert(ferienbetreuungAngaben.getAngebotVereineUndPrivateIntegriert());
		jaxAngebot.setBemerkungenKooperation(ferienbetreuungAngaben.getBemerkungenKooperation());
		jaxAngebot.setLeitungDurchPersonMitAusbildung(ferienbetreuungAngaben.getLeitungDurchPersonMitAusbildung());
		jaxAngebot.setBetreuungDurchPersonenMitErfahrung(ferienbetreuungAngaben.getBetreuungDurchPersonenMitErfahrung());
		jaxAngebot.setAnzahlKinderAngemessen(ferienbetreuungAngaben.getAnzahlKinderAngemessen());
		jaxAngebot.setBetreuungsschluessel(ferienbetreuungAngaben.getBetreuungsschluessel());
		jaxAngebot.setBemerkungenPersonal(ferienbetreuungAngaben.getBemerkungenPersonal());
		jaxAngebot.setFixerTarifKinderDerGemeinde(ferienbetreuungAngaben.getFixerTarifKinderDerGemeinde());
		jaxAngebot.setEinkommensabhaengigerTarifKinderDerGemeinde(ferienbetreuungAngaben.getEinkommensabhaengigerTarifKinderDerGemeinde());
		jaxAngebot.setTagesschuleTarifGiltFuerFerienbetreuung(ferienbetreuungAngaben.getTagesschuleTarifGiltFuerFerienbetreuung());
		jaxAngebot.setFerienbetreuungTarifWirdAusTagesschuleTarifAbgeleitet(ferienbetreuungAngaben.getFerienbetreuungTarifWirdAusTagesschuleTarifAbgeleitet());
		jaxAngebot.setKinderAusAnderenGemeindenZahlenAnderenTarif(ferienbetreuungAngaben.getKinderAusAnderenGemeindenZahlenAnderenTarif());
		jaxAngebot.setBemerkungenTarifsystem(ferienbetreuungAngaben.getBemerkungenTarifsystem());

		return jaxAngebot;
	}

	@Nonnull
	private JaxFerienbetreuungAngabenNutzung ferienbetreuungAngabenNutzungToJax(
		@Nonnull FerienbetreuungAngabenNutzung ferienbetreuungAngaben
	) {
		JaxFerienbetreuungAngabenNutzung jaxNutzung = new JaxFerienbetreuungAngabenNutzung();
		jaxNutzung.setAnzahlBetreuungstageKinderBern(ferienbetreuungAngaben.getAnzahlBetreuungstageKinderBern());
		jaxNutzung.setBetreuungstageKinderDieserGemeinde(ferienbetreuungAngaben.getBetreuungstageKinderDieserGemeinde());
		jaxNutzung.setBetreuungstageKinderDieserGemeindeSonderschueler(ferienbetreuungAngaben.getBetreuungstageKinderDieserGemeindeSonderschueler());
		jaxNutzung.setDavonBetreuungstageKinderAndererGemeinden(ferienbetreuungAngaben.getDavonBetreuungstageKinderAndererGemeinden());
		jaxNutzung.setDavonBetreuungstageKinderAndererGemeindenSonderschueler(ferienbetreuungAngaben.getDavonBetreuungstageKinderAndererGemeindenSonderschueler());
		jaxNutzung.setAnzahlBetreuteKinder(ferienbetreuungAngaben.getAnzahlBetreuteKinder());
		jaxNutzung.setAnzahlBetreuteKinderSonderschueler(ferienbetreuungAngaben.getAnzahlBetreuteKinderSonderschueler());
		jaxNutzung.setAnzahlBetreuteKinder1Zyklus(ferienbetreuungAngaben.getAnzahlBetreuteKinder1Zyklus());
		jaxNutzung.setAnzahlBetreuteKinder2Zyklus(ferienbetreuungAngaben.getAnzahlBetreuteKinder2Zyklus());
		jaxNutzung.setAnzahlBetreuteKinder3Zyklus(ferienbetreuungAngaben.getAnzahlBetreuteKinder3Zyklus());

		return jaxNutzung;
	}

	@Nonnull
	private JaxFerienbetreuungAngabenKostenEinnahmen ferienbetreuungAngabenKostenEinnahmenToJax(
		@Nonnull FerienbetreuungAngabenKostenEinnahmen ferienbetreuungAngaben
	) {
		JaxFerienbetreuungAngabenKostenEinnahmen jaxKostenEinnahmen = new JaxFerienbetreuungAngabenKostenEinnahmen();
		jaxKostenEinnahmen.setPersonalkosten(ferienbetreuungAngaben.getPersonalkosten());
		jaxKostenEinnahmen.setPersonalkostenLeitungAdmin(ferienbetreuungAngaben.getPersonalkostenLeitungAdmin());
		jaxKostenEinnahmen.setSachkosten(ferienbetreuungAngaben.getSachkosten());
		jaxKostenEinnahmen.setVerpflegungskosten(ferienbetreuungAngaben.getVerpflegungskosten());
		jaxKostenEinnahmen.setWeitereKosten(ferienbetreuungAngaben.getWeitereKosten());
		jaxKostenEinnahmen.setBemerkungenKosten(ferienbetreuungAngaben.getBemerkungenKosten());
		jaxKostenEinnahmen.setElterngebuehren(ferienbetreuungAngaben.getElterngebuehren());
		jaxKostenEinnahmen.setWeitereEinnahmen(ferienbetreuungAngaben.getWeitereEinnahmen());

		return jaxKostenEinnahmen;
	}

	@Nonnull
	public JaxFerienbetreuungDokument ferienbetreuungDokumentToJax(@Nonnull FerienbetreuungDokument ferienbetreuungDokument) {

		JaxFerienbetreuungDokument jaxFerienbetreuungDokument = convertAbstractVorgaengerFieldsToJAX(
			ferienbetreuungDokument,
			new JaxFerienbetreuungDokument()
		);
		convertFileToJax(ferienbetreuungDokument, jaxFerienbetreuungDokument);

		jaxFerienbetreuungDokument.setTimestampUpload(jaxFerienbetreuungDokument.getTimestampUpload());

		return jaxFerienbetreuungDokument;
	}

	@Nonnull
	private Set<Gemeinde> gemeindeListToEntity(@Nonnull List<JaxGemeinde> gemeindeList) {
		return gemeindeList
			.stream()
			.map(g -> {
				Objects.requireNonNull(g.getId());
				return gemeindeService.findGemeinde(g.getId()).orElseThrow(() -> new EbeguRuntimeException(
					"findGemeinde",
					ErrorCodeEnum.ERROR_ENTITY_NOT_FOUND,
					g.getId()));
			})
			.collect(Collectors.toSet());
	}

	@Nonnull
	private List<JaxGemeinde> gemeindeListToJax(@Nonnull Set<Gemeinde> gemeindeSet) {
		return gemeindeSet
			.stream()
			.map(this::gemeindeToJAX)
			.collect(Collectors.toList());
	}

}
