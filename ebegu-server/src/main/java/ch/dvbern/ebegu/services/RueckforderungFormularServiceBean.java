/*
 * Copyright (C) 2020 DV Bern AG, Switzerland
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

package ch.dvbern.ebegu.services;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.activation.MimeTypeParseException;
import javax.annotation.Nonnull;
import javax.ejb.Local;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.interceptor.Interceptors;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.ParameterExpression;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import ch.dvbern.ebegu.authentication.PrincipalBean;
import ch.dvbern.ebegu.entities.Benutzer;
import ch.dvbern.ebegu.entities.GeneratedNotrechtDokument;
import ch.dvbern.ebegu.entities.Institution;
import ch.dvbern.ebegu.entities.InstitutionStammdaten;
import ch.dvbern.ebegu.entities.RueckforderungFormular;
import ch.dvbern.ebegu.entities.RueckforderungFormular_;
import ch.dvbern.ebegu.entities.RueckforderungMitteilung;
import ch.dvbern.ebegu.entities.WriteProtectedDokument;
import ch.dvbern.ebegu.enums.ApplicationPropertyKey;
import ch.dvbern.ebegu.enums.BetreuungsangebotTyp;
import ch.dvbern.ebegu.enums.ErrorCodeEnum;
import ch.dvbern.ebegu.enums.RueckforderungInstitutionTyp;
import ch.dvbern.ebegu.enums.RueckforderungStatus;
import ch.dvbern.ebegu.enums.UserRole;
import ch.dvbern.ebegu.errors.EbeguEntityNotFoundException;
import ch.dvbern.ebegu.errors.EbeguRuntimeException;
import ch.dvbern.ebegu.errors.MailException;
import ch.dvbern.ebegu.errors.MergeDocException;
import ch.dvbern.ebegu.persistence.CriteriaQueryHelper;
import ch.dvbern.ebegu.services.interceptors.UpdateRueckfordFormStatusInterceptor;
import ch.dvbern.ebegu.services.util.ZipCreator;
import ch.dvbern.ebegu.util.EbeguUtil;
import ch.dvbern.lib.cdipersistence.Persistence;
import org.apache.commons.lang.StringUtils;

@Stateless
@Local(RueckforderungFormularService.class)
public class RueckforderungFormularServiceBean extends AbstractBaseService implements RueckforderungFormularService {

	@Inject
	private Persistence persistence;

	@Inject
	private InstitutionStammdatenService institutionStammdatenService;

	@Inject
	private CriteriaQueryHelper criteriaQueryHelper;

	@Inject
	private InstitutionService institutionService;

	@Inject
	private ApplicationPropertyService applicationPropertyService;

	@Inject
	private MailService mailService;

	@Inject
	private PrincipalBean principalBean;

	@Inject
	private Authorizer authorizer;

	@Inject
	private GeneratedDokumentService generatedDokumentService;

	@Nonnull
	@Override
	public List<RueckforderungFormular> initializeRueckforderungFormulare() {

		Collection<InstitutionStammdaten> institutionenStammdatenCollection = institutionStammdatenService.getAllInstitutionStammdaten();
		Collection<RueckforderungFormular> rueckforderungFormularCollection = getAllRueckforderungFormulare();

		List<RueckforderungFormular> rueckforderungFormulare = new ArrayList<>();
		for (InstitutionStammdaten institutionStammdaten : institutionenStammdatenCollection) {
			// neues Formular erstellen falls es sich un eine kita oder TFO handelt und noch kein Formular existiert
			if ((institutionStammdaten.getBetreuungsangebotTyp() == BetreuungsangebotTyp.KITA ||
				institutionStammdaten.getBetreuungsangebotTyp() == BetreuungsangebotTyp.TAGESFAMILIEN) &&
				!isFormularExisting(institutionStammdaten, rueckforderungFormularCollection)
				&& institutionStammdaten.getInstitutionStammdatenBetreuungsgutscheine() != null
				&& institutionStammdaten.getInstitutionStammdatenBetreuungsgutscheine().getIban() != null) {

				RueckforderungFormular formular = new RueckforderungFormular();
				formular.setInstitutionStammdaten(institutionStammdaten);
				formular.setStatus(RueckforderungStatus.NEU);
				rueckforderungFormulare.add(createRueckforderungFormular(formular));
			}
		}
		return rueckforderungFormulare;
	}

	/**
	 * Falls in der Liste der Rückforderungsformulare die Institution bereits existiert, wird true zurückgegeben
	 */
	private boolean isFormularExisting(@Nonnull InstitutionStammdaten stammdaten,
		@Nonnull Collection<RueckforderungFormular> rueckforderungFormularCollection
	) {
		List<RueckforderungFormular> filteredFormulare = rueckforderungFormularCollection
			.stream()
			.filter(formular -> formular.getInstitutionStammdaten().getId().equals(stammdaten.getId()))
			.collect(Collectors.toList());
		return !filteredFormulare.isEmpty();
	}

	@Nonnull
	@Override
	public RueckforderungFormular createRueckforderungFormular(@Nonnull RueckforderungFormular rueckforderungFormular) {
		authorizer.checkWriteAuthorization(rueckforderungFormular);
		return persistence.persist(rueckforderungFormular);
	}

	@Nonnull
	private Collection<RueckforderungFormular> getAllRueckforderungFormulare(){
		return criteriaQueryHelper.getAll(RueckforderungFormular.class);
	}

	@Nonnull
	@Override
	public List<RueckforderungFormular> getRueckforderungFormulareForCurrentBenutzer() {
		Collection<RueckforderungFormular> allRueckforderungFormulare = getAllRueckforderungFormulare();
		Benutzer currentBenutzer = principalBean.getBenutzer();
		if (currentBenutzer.getRole().isRoleMandant() || currentBenutzer.getRole().isSuperadmin()){
			return new ArrayList<>(allRueckforderungFormulare);
		}
		Collection<Institution> institutionenCurrentBenutzer =
			institutionService.getInstitutionenEditableForCurrentBenutzer(false);

		return allRueckforderungFormulare.stream().filter(formular -> {
			for (Institution institution : institutionenCurrentBenutzer) {
				if (institution.getId().equals(formular.getInstitutionStammdaten().getInstitution().getId())) {
					return true;
				}
			}
			return false;
		}).collect(Collectors.toList());
	}

	@Nonnull
	@Override
	@Interceptors(UpdateRueckfordFormStatusInterceptor.class)
	public Optional<RueckforderungFormular> findRueckforderungFormular(@Nonnull String id) {
		Objects.requireNonNull(id, "id muss gesetzt sein");
		RueckforderungFormular rueckforderungFormular = persistence.find(RueckforderungFormular.class, id);
		authorizer.checkReadAuthorization(rueckforderungFormular);
		return Optional.ofNullable(rueckforderungFormular);
	}

	@Nonnull
	@Override
	public RueckforderungFormular save(@Nonnull RueckforderungFormular rueckforderungFormular) {
		Objects.requireNonNull(rueckforderungFormular);
		authorizer.checkWriteAuthorization(rueckforderungFormular);
		final RueckforderungFormular mergedRueckforderungFormular = persistence.merge(rueckforderungFormular);
		return mergedRueckforderungFormular;
	}

	private RueckforderungFormular saveWithoutAuthCheck(@Nonnull RueckforderungFormular rueckforderungFormular) {
		Objects.requireNonNull(rueckforderungFormular);
		final RueckforderungFormular mergedRueckforderungFormular = persistence.merge(rueckforderungFormular);
		return mergedRueckforderungFormular;
	}

	@Nonnull
	@Override
	public RueckforderungFormular saveAndChangeStatusIfNecessary(@Nonnull RueckforderungFormular rueckforderungFormular) {
		Objects.requireNonNull(rueckforderungFormular);
		authorizer.checkWriteAuthorization(rueckforderungFormular);
		changeStatusAndCopyFields(rueckforderungFormular);
		return saveWithoutAuthCheck(rueckforderungFormular);
	}

	@Nonnull
	@Override
	public Collection<RueckforderungFormular> getRueckforderungFormulareByStatus(@Nonnull List<RueckforderungStatus> status) {
		Objects.requireNonNull(status.get(0), "Mindestens ein Status muss angegeben werden");
		final CriteriaBuilder cb = persistence.getCriteriaBuilder();
		final CriteriaQuery<RueckforderungFormular> query = cb.createQuery(RueckforderungFormular.class);

		final Root<RueckforderungFormular> root = query.from(RueckforderungFormular.class);

		Predicate predicateStatus = root.get(RueckforderungFormular_.status).in(status);
		query.where(predicateStatus);
		return persistence.getCriteriaResults(query);
	}

	@Nonnull
	@Override
	public RueckforderungFormular addMitteilung(
		@Nonnull RueckforderungFormular formular,
		@Nonnull RueckforderungMitteilung mitteilung
	) {
		authorizer.checkReadAuthorization(formular);
		formular.addRueckforderungMitteilung(mitteilung);
		return persistence.persist(formular);
	}

	@Override
	public void initializePhase2() {
		//set Application Properties zu true
		applicationPropertyService.saveOrUpdateApplicationProperty(ApplicationPropertyKey.KANTON_NOTVERORDNUNG_PHASE_2_AKTIV, "true");
		//get alle Ruckforderungsformular, check status and changed if needed
		ArrayList<RueckforderungStatus> statusGeprueftStufe1 = new ArrayList<>();
		statusGeprueftStufe1.add(RueckforderungStatus.GEPRUEFT_STUFE_1);
		Collection<RueckforderungFormular> formulareWithStatusGeprueftStufe1 =
			getRueckforderungFormulareByStatus(statusGeprueftStufe1);
		for (RueckforderungFormular formular : formulareWithStatusGeprueftStufe1) {
			authorizer.checkWriteAuthorization(formular);
			saveAndChangeStatusIfNecessary(formular);
		}
	}

	@Nonnull
	@Override
	public RueckforderungFormular resetStatusToInBearbeitungInstitutionPhase2(@Nonnull String id) {
		final RueckforderungFormular rueckforderungFormular = findRueckforderungFormular(id)
			.orElseThrow(() -> new EbeguEntityNotFoundException(
				"resetStatusToInBearbeitungInstitutionPhase2",
				ErrorCodeEnum.ERROR_ENTITY_NOT_FOUND,
				"Rueckfordungsformular invalid: " + id));
		authorizer.checkWriteAuthorization(rueckforderungFormular);
		rueckforderungFormular.setStatus(RueckforderungStatus.IN_BEARBEITUNG_INSTITUTION_STUFE_2);
		return saveWithoutAuthCheck(rueckforderungFormular);
	}

	@Nonnull
	@Override
	public RueckforderungFormular resetStatusToInPruefungKantonPhase2(@Nonnull String id) {
		final RueckforderungFormular rueckforderungFormular = findRueckforderungFormular(id)
			.orElseThrow(() -> new EbeguEntityNotFoundException(
				"resetStatusToInBearbeitungKantonPhase2",
				ErrorCodeEnum.ERROR_ENTITY_NOT_FOUND,
				"Rueckfordungsformular invalid: " + id));
		authorizer.checkWriteAuthorization(rueckforderungFormular);
		rueckforderungFormular.setStatus(RueckforderungStatus.IN_PRUEFUNG_KANTON_STUFE_2);
		return saveWithoutAuthCheck(rueckforderungFormular);
	}

	@Override
	@Nonnull
	public RueckforderungFormular provisorischeVerfuegung(RueckforderungFormular formular) {
		//Set status to Verfuegt Provisorisch
		formular.setStatus(RueckforderungStatus.VERFUEGT_PROVISORISCH);
		formular.setStufe2VoraussichtlicheBetrag(formular.calculateFreigabeBetragStufe2());
		formular.setHasBeenProvisorisch(true);
		final RueckforderungFormular persistedRueckforderungFormular = persistence.merge(formular);
		try {
			//Inform Institution das der PRovisorsische Verfuegung wurde generiert
			mailService.sendInfoRueckforderungProvisorischVerfuegt(persistedRueckforderungFormular);
		} catch (MailException e) {
			logExceptionAccordingToEnvironment(e,
				"Mail InfoRueckforderungProvisorischVerfuegt konnte nicht verschickt werden fuer RueckforderungFormular",
				persistedRueckforderungFormular.getId());
		}
		generateProvisorischeVerfuegungDokument(persistedRueckforderungFormular);

		return persistedRueckforderungFormular;
	}

	@Nonnull
	@Override
	public byte[] massenVerfuegungDefinitiv(@Nonnull String auftragIdentifier) {
		final Collection<RueckforderungFormular> toVerfuegen = getFormulareZuVerfuegen();
		// Eigentliches Verfuegen (inkl. Generierung der Verfuegung)
		try {
			ZipCreator zipCreator = new ZipCreator();
			for (RueckforderungFormular rueckforderungFormular : toVerfuegen) {
				byte[] content = definitivVerfuegen(rueckforderungFormular, auftragIdentifier);
				// Als Dateinamen innerhalb des Zips nehmen wir den Namen der Institution:
				String zipEntryName = EbeguUtil.toFilename(rueckforderungFormular.getInstitutionStammdaten().getInstitution().getName() + ".pdf");
				zipCreator.append(new ByteArrayInputStream(content), zipEntryName);
			}
			return zipCreator.create();
		} catch (IOException ioe) {
			throw new EbeguRuntimeException(
				"definitivVerfuegen", "Could not create Zip File for Auftrag", ioe, auftragIdentifier);
		}
	}

	@Nonnull
	private Collection<RueckforderungFormular> getFormulareZuVerfuegen() {
		final CriteriaBuilder cb = persistence.getCriteriaBuilder();
		final CriteriaQuery<RueckforderungFormular> query = cb.createQuery(RueckforderungFormular.class);
		Root<RueckforderungFormular> root = query.from(RueckforderungFormular.class);

		ParameterExpression<RueckforderungStatus> statusParam = cb.parameter(RueckforderungStatus.class, "status");
		ParameterExpression<RueckforderungInstitutionTyp> institutionTypParam = cb.parameter(RueckforderungInstitutionTyp.class, "institutionTyp");
		ParameterExpression<Boolean> hasBeenProvisorischParam = cb.parameter(Boolean.class, "hasBeenProvisorisch");

		// Alle im Status BEREIT_ZUM_VERFUEGEN, die Typ PRIVAT sind und nie eine Provisorische Verfuegung hatten
		Predicate statusPredicate = cb.equal(root.get(RueckforderungFormular_.status), statusParam);
		Predicate privatPredicate = cb.equal(root.get(RueckforderungFormular_.institutionTyp), institutionTypParam);
		Predicate hasNotBeenProvisorischParam = cb.equal(root.get(RueckforderungFormular_.hasBeenProvisorisch), hasBeenProvisorischParam);

		query.where(statusPredicate, privatPredicate, hasNotBeenProvisorischParam);

		TypedQuery<RueckforderungFormular> q = persistence.getEntityManager().createQuery(query);
		q.setParameter(statusParam, RueckforderungStatus.BEREIT_ZUM_VERFUEGEN);
		q.setParameter(institutionTypParam, RueckforderungInstitutionTyp.PRIVAT);
		q.setParameter(hasBeenProvisorischParam, Boolean.FALSE);
		return q.getResultList();
	}

	@Nonnull
	private byte[] definitivVerfuegen(@Nonnull RueckforderungFormular formular, @Nonnull String auftragIdentifier) {
		if (formular.getStatus() != RueckforderungStatus.BEREIT_ZUM_VERFUEGEN) {
			throw new IllegalArgumentException("falscher status");
		}
		formular.setStatus(RueckforderungStatus.VERFUEGT);
		final RueckforderungFormular persistedRueckforderungFormular = persistence.merge(formular);
		// Bei der definitiven Verfuegung wird kein E-Mail versandt
		final byte[] bytes = generateDefinitiveVerfuegungDokument(persistedRueckforderungFormular, auftragIdentifier);
		return bytes;
	}

	@SuppressWarnings("PMD.NcssMethodCount")
	private void changeStatusAndCopyFields(@Nonnull RueckforderungFormular rueckforderungFormular) {
		authorizer.checkWriteAuthorization(rueckforderungFormular);
		switch (rueckforderungFormular.getStatus()) {
		case EINGELADEN: {
			if (principalBean.isCallerInAnyOfRole(UserRole.getInstitutionTraegerschaftRoles())) {
				rueckforderungFormular.setStatus(RueckforderungStatus.IN_BEARBEITUNG_INSTITUTION_STUFE_1);
			}
			break;
		}
		case IN_BEARBEITUNG_INSTITUTION_STUFE_1: {
			if (principalBean.isCallerInAnyOfRole(UserRole.getInstitutionTraegerschaftRoles())) {
				rueckforderungFormular.setStatus(RueckforderungStatus.IN_PRUEFUNG_KANTON_STUFE_1);
				rueckforderungFormular.setStufe1KantonKostenuebernahmeAnzahlStunden(rueckforderungFormular.getStufe1InstitutionKostenuebernahmeAnzahlStunden());
				rueckforderungFormular.setStufe1KantonKostenuebernahmeAnzahlTage(rueckforderungFormular.getStufe1InstitutionKostenuebernahmeAnzahlTage());
				rueckforderungFormular.setStufe1KantonKostenuebernahmeBetreuung(rueckforderungFormular.getStufe1InstitutionKostenuebernahmeBetreuung());
			}
			break;
		}
		case IN_BEARBEITUNG_INSTITUTION_STUFE_2: {
			if (principalBean.isCallerInAnyOfRole(UserRole.getInstitutionTraegerschaftRoles())) {
				RueckforderungStatus nextStatus = RueckforderungStatus.IN_PRUEFUNG_KANTON_STUFE_2;
				rueckforderungFormular.setStatus(nextStatus);
				rueckforderungFormular.setStufe2KantonKostenuebernahmeAnzahlStunden(rueckforderungFormular.getStufe2InstitutionKostenuebernahmeAnzahlStunden());
				rueckforderungFormular.setStufe2KantonKostenuebernahmeAnzahlTage(rueckforderungFormular.getStufe2InstitutionKostenuebernahmeAnzahlTage());
				rueckforderungFormular.setStufe2KantonKostenuebernahmeBetreuung(rueckforderungFormular.getStufe2InstitutionKostenuebernahmeBetreuung());
			}
			break;
		}
		case IN_PRUEFUNG_KANTON_STUFE_1: {
			if (principalBean.isCallerInAnyOfRole(UserRole.getMandantSuperadminRoles())) {
				rueckforderungFormular.setStatus(RueckforderungStatus.GEPRUEFT_STUFE_1);
				// Zahlungen ausloesen
				rueckforderungFormular.setStufe1FreigabeBetrag(rueckforderungFormular.calculateFreigabeBetragStufe1());
				rueckforderungFormular.setStufe1FreigabeDatum(LocalDateTime.now());
				// Bestaetigung schicken
				createBestaetigungStufe1Geprueft(rueckforderungFormular);

				// Falls unterdessen die Phase zwei bereits aktiviert wurde, wollen wir mit "geprueft" der Phase zwei direkt in die Bearbeitung
				// Institution Phase 2 wechseln, da wir sonst auf "geprueft" blockiert bleiben
				if (applicationPropertyService.isKantonNotverordnungPhase2Aktiviert()
					&& principalBean.isCallerInAnyOfRole(UserRole.getMandantSuperadminRoles())) {
					// Direkt zum naechsten Status wechseln. In der Audit-Tabelle wird nur der neue Status sein
					// Finde ich aber okay, da es auch nur 1 Benutzeraktion war, die von Status IN_PRUEFUNG_KANTON_STUFE_1
					// zu IN_BEARBEITUNG_INSTITUTION_STUFE_2 gefuehrt hat.
					rueckforderungFormular.setStatus(RueckforderungStatus.IN_BEARBEITUNG_INSTITUTION_STUFE_2);
					rueckforderungFormular.setStufe2InstitutionKostenuebernahmeAnzahlStunden(rueckforderungFormular.getStufe1KantonKostenuebernahmeAnzahlStunden());
					rueckforderungFormular.setStufe2InstitutionKostenuebernahmeAnzahlTage(rueckforderungFormular.getStufe1KantonKostenuebernahmeAnzahlTage());
					rueckforderungFormular.setStufe2InstitutionKostenuebernahmeBetreuung(rueckforderungFormular.getStufe1KantonKostenuebernahmeBetreuung());
				}
			}
			break;
		}
		case GEPRUEFT_STUFE_1: {
			if (applicationPropertyService.isKantonNotverordnungPhase2Aktiviert()
					&& principalBean.isCallerInAnyOfRole(UserRole.getMandantSuperadminRoles())) {
				rueckforderungFormular.setStatus(RueckforderungStatus.IN_BEARBEITUNG_INSTITUTION_STUFE_2);
				rueckforderungFormular.setStufe2InstitutionKostenuebernahmeAnzahlStunden(rueckforderungFormular.getStufe1KantonKostenuebernahmeAnzahlStunden());
				rueckforderungFormular.setStufe2InstitutionKostenuebernahmeAnzahlTage(rueckforderungFormular.getStufe1KantonKostenuebernahmeAnzahlTage());
				rueckforderungFormular.setStufe2InstitutionKostenuebernahmeBetreuung(rueckforderungFormular.getStufe1KantonKostenuebernahmeBetreuung());
			}
			break;
		}
		case IN_PRUEFUNG_KANTON_STUFE_2:
			rueckforderungFormular.setStufe2VoraussichtlicheBetrag(rueckforderungFormular.calculateFreigabeBetragStufe2());
		case VERFUEGT_PROVISORISCH:
			if(principalBean.isCallerInAnyOfRole(UserRole.getMandantSuperadminRoles())){
				rueckforderungFormular.setStatus(RueckforderungStatus.BEREIT_ZUM_VERFUEGEN);
			}
			break;
		default:
			break;
		}
	}

	private void createBestaetigungStufe1Geprueft(@Nonnull RueckforderungFormular modifiedRueckforderungFormular) {
		try {
			// Als Hack, weil im Nachhinein die Anforderung kam, das Mail auch noch als RueckforderungsMitteilung zu
			// speichern, wird hier der generierte HTML-Inhalt des Mails zurueckgegeben
			final String mailText = mailService.sendNotrechtBestaetigungPruefungStufe1(modifiedRueckforderungFormular);
			if (mailText != null) {
				// Wir wollen nur den body speichern
				String content = StringUtils.substringBetween(mailText, "<body>", "</body>");
				// remove any newlines or tabs (leading or trailing whitespace doesn't matter)
				content = content.replaceAll("(\\t|\\n)", "");
				// boil down remaining whitespace to a single space
				content = content.replaceAll("\\s+", " ");
				content = content.trim();

				final String betreff = "Corona-Finanzierung für Kitas und  TFO: Zahlung freigegeben / "
					+ "Corona - financement pour les crèches et les parents de jour: Versement libéré";
				RueckforderungMitteilung mitteilung = new RueckforderungMitteilung();
				mitteilung.setBetreff(betreff);
				mitteilung.setInhalt(content);
				mitteilung.setAbsender(principalBean.getBenutzer());
				mitteilung.setSendeDatum(LocalDateTime.now());
				mitteilung = persistence.persist(mitteilung);
				addMitteilung(modifiedRueckforderungFormular, mitteilung);
			}
		} catch (Exception e) {
			throw new EbeguRuntimeException("update",
				"BestaetigungEmail koennte nicht als Mitteilung gespeichert werden fuer RueckforderungFormular: " + modifiedRueckforderungFormular.getId(), e);
		}
	}

	/**
	 * Generiert das Provisoriche Verfuegung Dokument einer Ruckforderungformular.
	 */
	private void generateProvisorischeVerfuegungDokument(@Nonnull RueckforderungFormular rueckforderungFormular) {
		try {
			//noinspection ResultOfMethodCallIgnored
			generatedDokumentService.getRueckforderungProvVerfuegungAccessTokenGeneratedDokument(rueckforderungFormular);
		} catch (MimeTypeParseException | MergeDocException e) {
			throw new EbeguRuntimeException(
				"ProvisorischeVerfuegungDokument",
				"ProvisorischeVerfuegung-Dokument konnte nicht erstellt werden"
					+ rueckforderungFormular.getId(), e);
		}
	}

	/**
	 * Generiert das definitive Verfuegung Dokument einer Ruckforderungformular.
	 */
	private byte[] generateDefinitiveVerfuegungDokument(
		@Nonnull RueckforderungFormular rueckforderungFormular,
		@Nonnull String auftragIdentifier
	) {
		try {
			//noinspection ResultOfMethodCallIgnored
			final WriteProtectedDokument dokument =
				generatedDokumentService.getRueckforderungDefinitiveVerfuegungAccessTokenGeneratedDokument(
				rueckforderungFormular, auftragIdentifier);
			return ((GeneratedNotrechtDokument)dokument).getContent();
		} catch (MimeTypeParseException | MergeDocException e) {
			throw new EbeguRuntimeException(
				"DefinitiveVerfuegungDokument",
				"DefinitiveVerfuegung-Dokument konnte nicht erstellt werden"
					+ rueckforderungFormular.getId(), e);
		}
	}
}
