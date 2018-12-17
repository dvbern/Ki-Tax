/*
 * Ki-Tax: System for the management of external childcare subsidies
 * Copyright (C) 2017 City of Bern Switzerland
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

package ch.dvbern.ebegu.services;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import javax.activation.MimeTypeParseException;
import javax.annotation.Nonnull;
import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.ejb.Local;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import ch.dvbern.ebegu.entities.AbstractEntity_;
import ch.dvbern.ebegu.entities.DokumentGrund;
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.entities.Mahnung;
import ch.dvbern.ebegu.entities.Mahnung_;
import ch.dvbern.ebegu.enums.AntragStatus;
import ch.dvbern.ebegu.enums.ErrorCodeEnum;
import ch.dvbern.ebegu.enums.MahnungTyp;
import ch.dvbern.ebegu.errors.EbeguRuntimeException;
import ch.dvbern.ebegu.errors.MergeDocException;
import ch.dvbern.ebegu.pdfgenerator.KibonPrintUtil;
import ch.dvbern.ebegu.rules.anlageverzeichnis.DokumentenverzeichnisEvaluator;
import ch.dvbern.ebegu.util.DokumenteUtil;
import ch.dvbern.lib.cdipersistence.Persistence;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static ch.dvbern.ebegu.enums.UserRoleName.ADMIN_BG;
import static ch.dvbern.ebegu.enums.UserRoleName.ADMIN_GEMEINDE;
import static ch.dvbern.ebegu.enums.UserRoleName.ADMIN_TS;
import static ch.dvbern.ebegu.enums.UserRoleName.SACHBEARBEITER_BG;
import static ch.dvbern.ebegu.enums.UserRoleName.SACHBEARBEITER_GEMEINDE;
import static ch.dvbern.ebegu.enums.UserRoleName.SACHBEARBEITER_TS;
import static ch.dvbern.ebegu.enums.UserRoleName.SUPER_ADMIN;

/**
 * Service fuer Mahnungen
 */
@SuppressWarnings("OverlyBroadCatchBlock")
@Stateless
@Local(MahnungService.class)
@PermitAll
public class MahnungServiceBean extends AbstractBaseService implements MahnungService {

	private static final Logger LOG = LoggerFactory.getLogger(MahnungServiceBean.class.getSimpleName());

	@Inject
	private Persistence persistence;

	@Inject
	private DokumentGrundService dokumentGrundService;

	@Inject
	private DokumentenverzeichnisEvaluator dokumentenverzeichnisEvaluator;

	@Inject
	private GesuchService gesuchService;

	@Inject
	private Authorizer authorizer;

	@Inject
	private MailService mailService;

	@Inject
	private GeneratedDokumentService generatedDokumentService;

	@Override
	@Nonnull
	@RolesAllowed({ ADMIN_BG, SUPER_ADMIN, SACHBEARBEITER_BG, ADMIN_GEMEINDE, SACHBEARBEITER_GEMEINDE, SACHBEARBEITER_TS, ADMIN_TS })
	public Mahnung createMahnung(@Nonnull Mahnung mahnung) {
		Objects.requireNonNull(mahnung);
		// Sicherstellen, dass keine offene Mahnung desselben Typs schon existiert
		assertNoOpenMahnungOfType(mahnung.getGesuch(), mahnung.getMahnungTyp());
		if (MahnungTyp.ZWEITE_MAHNUNG == mahnung.getMahnungTyp()) {
			// Die Erst-Mahnung suchen und verknuepfen, wird im Dokument gebraucht
			Optional<Mahnung> erstMahnung = findAktiveErstMahnung(mahnung.getGesuch());
			if (erstMahnung.isPresent()) {
				mahnung.setVorgaengerId(erstMahnung.get().getId());
			} else {
				throw new EbeguRuntimeException("createMahnung", "Zweitmahnung erstellt ohne aktive Erstmahnung! " + mahnung.getId(), mahnung.getId());
			}
		}
		Mahnung persistedMahnung = persistence.persist(mahnung);
		Gesuch gesuch = persistedMahnung.getGesuch();
		gesuch.setDokumenteHochgeladen(Boolean.FALSE);
		// Das Mahnungsdokument drucken
		try {
			generatedDokumentService.getMahnungDokumentAccessTokenGeneratedDokument(mahnung, true);
		} catch (MimeTypeParseException | IOException | MergeDocException e) {
			throw new EbeguRuntimeException("createMahnung", "Mahnung-Dokument konnte nicht erstellt werden " +
				mahnung.getId(), e, mahnung.getId());
		}
		// Mail senden
		try {
			mailService.sendInfoMahnung(gesuch);
		} catch (Exception e) {
			LOG.error("Mail InfoMahnung konnte nicht verschickt werden fuer Gesuch {}", gesuch.getId(), e);
		}
		return persistedMahnung;
	}

	@Override
	@Nonnull
	@PermitAll
	public Optional<Mahnung> findMahnung(@Nonnull String mahnungId) {
		Objects.requireNonNull(mahnungId, "mahnungId muss gesetzt sein");
		Mahnung mahnung = persistence.find(Mahnung.class, mahnungId);
		if (mahnung != null) {
			authorizer.checkReadAuthorization(mahnung.getGesuch());
		}
		return Optional.ofNullable(mahnung);
	}

	@Override
	@Nonnull
	@PermitAll
	public Collection<Mahnung> findMahnungenForGesuch(@Nonnull Gesuch gesuch) {
		authorizer.checkReadAuthorization(gesuch);
		final CriteriaBuilder cb = persistence.getCriteriaBuilder();
		final CriteriaQuery<Mahnung> query = cb.createQuery(Mahnung.class);
		Root<Mahnung> root = query.from(Mahnung.class);

		Predicate prediateGesuch = cb.equal(root.get(Mahnung_.gesuch), gesuch);
		query.where(prediateGesuch);
		query.orderBy(cb.asc(root.get(AbstractEntity_.timestampErstellt)));
		return persistence.getCriteriaResults(query);
	}

	@Override
	@Nonnull
	@RolesAllowed({ ADMIN_BG, SUPER_ADMIN, SACHBEARBEITER_BG, ADMIN_GEMEINDE, SACHBEARBEITER_GEMEINDE, SACHBEARBEITER_TS, ADMIN_TS })
	public Gesuch mahnlaufBeenden(@Nonnull Gesuch gesuch) {
		gesuch.setStatus(AntragStatus.IN_BEARBEITUNG_JA);
		gesuch.setDokumenteHochgeladen(Boolean.FALSE);
		gesuch = gesuchService.updateGesuch(gesuch, true, null);
		// Alle Mahnungen auf erledigt stellen
		Collection<Mahnung> mahnungenForGesuch = findMahnungenForGesuch(gesuch);
		for (Mahnung mahnung : mahnungenForGesuch) {
			mahnung.setTimestampAbgeschlossen(LocalDateTime.now());
			persistence.persist(mahnung);
		}
		return gesuch;
	}

	@Override
	@Nonnull
	@PermitAll
	public String getInitialeBemerkungen(@Nonnull Gesuch gesuch) {
		authorizer.checkReadAuthorization(gesuch);
		List<DokumentGrund> dokumentGrundsMerged = new ArrayList<>(DokumenteUtil
			.mergeNeededAndPersisted(dokumentenverzeichnisEvaluator.calculate(gesuch),
				dokumentGrundService.findAllDokumentGrundByGesuch(gesuch)));
		Collections.sort(dokumentGrundsMerged);

		StringBuilder bemerkungenBuilder = new StringBuilder();
		for (DokumentGrund dokumentGrund : dokumentGrundsMerged) {
			String dokumentData = KibonPrintUtil.getDokumentAsTextIfNeeded(dokumentGrund, gesuch);
			if (StringUtils.isNotEmpty(dokumentData)) {
				bemerkungenBuilder.append(dokumentData);
				bemerkungenBuilder.append('\n');
			}
		}
		return bemerkungenBuilder.toString();
	}

	@Override
	@RolesAllowed({ ADMIN_BG, ADMIN_GEMEINDE, SUPER_ADMIN })
	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public void fristAblaufTimer() {
		// Es muessen alle ueberprueft werden, die noch aktiv sind und deren Ablaufdatum < NOW liegt
		final CriteriaBuilder cb = persistence.getCriteriaBuilder();
		final CriteriaQuery<Mahnung> query = cb.createQuery(Mahnung.class);
		Root<Mahnung> root = query.from(Mahnung.class);
		query.distinct(true);

		Predicate predicateAktiv = cb.isNull(root.get(Mahnung_.timestampAbgeschlossen));
		Predicate predicateNochNichtAbgelaufenMarkiert = cb.isFalse(root.get(Mahnung_.abgelaufen));
		Predicate predicateAbgelaufen = cb.lessThan(root.get(Mahnung_.datumFristablauf), LocalDate.now());
		query.where(predicateAktiv, predicateNochNichtAbgelaufenMarkiert, predicateAbgelaufen);

		List<Mahnung> gesucheMitAbgelaufenenMahnungen = persistence.getCriteriaResults(query);
		for (Mahnung mahnung : gesucheMitAbgelaufenenMahnungen) {
			final Gesuch gesuch = mahnung.getGesuch();
			if (AntragStatus.ERSTE_MAHNUNG == gesuch.getStatus()) {
				gesuch.setStatus(AntragStatus.ERSTE_MAHNUNG_ABGELAUFEN);
				gesuchService.updateGesuch(gesuch, true, null);
			} else if (AntragStatus.ZWEITE_MAHNUNG == gesuch.getStatus()) {
				gesuch.setStatus(AntragStatus.ZWEITE_MAHNUNG_ABGELAUFEN);
				gesuchService.updateGesuch(gesuch, true, null);
			}
			mahnung.setAbgelaufen(true);
			persistence.merge(mahnung);
		}
	}

	@Override
	@Nonnull
	@PermitAll
	public Optional<Mahnung> findAktiveErstMahnung(Gesuch gesuch) {
		authorizer.checkReadAuthorization(gesuch);
		final CriteriaQuery<Mahnung> query = createQueryNotAbgeschlosseneMahnung(gesuch, MahnungTyp.ERSTE_MAHNUNG);
		// Wirft eine NonUnique-Exception, falls mehrere aktive ErstMahnungen!
		Mahnung aktiveErstMahnung = persistence.getCriteriaSingleResult(query);
		return Optional.ofNullable(aktiveErstMahnung);
	}

	@Override
	@RolesAllowed({ SUPER_ADMIN, ADMIN_BG, ADMIN_GEMEINDE })
	public void removeAllMahnungenFromGesuch(Gesuch gesuch) {
		Collection<Mahnung> mahnungenFromGesuch = findMahnungenForGesuch(gesuch);
		for (Mahnung mahnung : mahnungenFromGesuch) {
			persistence.remove(Mahnung.class, mahnung.getId());
		}
	}

	private void assertNoOpenMahnungOfType(@Nonnull Gesuch gesuch, @Nonnull MahnungTyp mahnungTyp) {
		authorizer.checkReadAuthorization(gesuch);
		final CriteriaQuery<Mahnung> query = createQueryNotAbgeschlosseneMahnung(gesuch, mahnungTyp);
		// Wirft eine NonUnique-Exception, falls mehrere aktive ErstMahnungen!
		List<Mahnung> criteriaResults = persistence.getCriteriaResults(query);
		if (!criteriaResults.isEmpty()) {
			throw new EbeguRuntimeException("assertNoOpenMahnungOfType", ErrorCodeEnum.ERROR_EXISTING_MAHNUNG);
		}
	}

	@Nonnull
	private CriteriaQuery<Mahnung> createQueryNotAbgeschlosseneMahnung(@Nonnull Gesuch gesuch, @Nonnull MahnungTyp mahnungTyp) {
		final CriteriaBuilder cb = persistence.getCriteriaBuilder();
		final CriteriaQuery<Mahnung> query = cb.createQuery(Mahnung.class);
		Root<Mahnung> root = query.from(Mahnung.class);
		query.select(root);
		Predicate predicateTyp = cb.equal(root.get(Mahnung_.mahnungTyp), mahnungTyp);
		Predicate predicateAktiv = cb.isNull(root.get(Mahnung_.timestampAbgeschlossen));
		Predicate predicateGesuch = cb.equal(root.get(Mahnung_.gesuch), gesuch);
		query.where(predicateTyp, predicateAktiv, predicateGesuch);
		return query;
	}
}
