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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;

import javax.activation.MimeTypeParseException;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.ejb.Local;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.NonUniqueResultException;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.ParameterExpression;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import ch.dvbern.ebegu.entities.AbstractDateRangedEntity_;
import ch.dvbern.ebegu.entities.EbeguVorlage;
import ch.dvbern.ebegu.entities.EbeguVorlage_;
import ch.dvbern.ebegu.entities.Vorlage;
import ch.dvbern.ebegu.enums.BetreuungsangebotTyp;
import ch.dvbern.ebegu.enums.EbeguVorlageKey;
import ch.dvbern.ebegu.enums.ErrorCodeEnum;
import ch.dvbern.ebegu.errors.EbeguEntityNotFoundException;
import ch.dvbern.ebegu.errors.EbeguRuntimeException;
import ch.dvbern.ebegu.persistence.CriteriaQueryHelper;
import ch.dvbern.ebegu.types.DateRange_;
import ch.dvbern.ebegu.util.Constants;
import ch.dvbern.ebegu.util.ServerMessageUtil;
import ch.dvbern.ebegu.util.UploadFileInfo;
import ch.dvbern.lib.cdipersistence.Persistence;
import org.apache.commons.io.IOUtils;

import static ch.dvbern.ebegu.enums.UserRoleName.ADMIN_BG;
import static ch.dvbern.ebegu.enums.UserRoleName.ADMIN_GEMEINDE;
import static ch.dvbern.ebegu.enums.UserRoleName.SUPER_ADMIN;

/**
 * Service fuer EbeguVorlage
 */
@Stateless
@Local(EbeguVorlageService.class)
public class EbeguVorlageServiceBean extends AbstractBaseService implements EbeguVorlageService {

	@Inject
	private Persistence persistence;

	@Inject
	private FileSaverService fileSaverService;

	@Inject
	private CriteriaQueryHelper criteriaQueryHelper;

	@Nonnull
	@Override
	@RolesAllowed({ ADMIN_BG, ADMIN_GEMEINDE, SUPER_ADMIN })
	public EbeguVorlage saveEbeguVorlage(@Nonnull EbeguVorlage ebeguVorlage) {
		Objects.requireNonNull(ebeguVorlage);
		return persistence.merge(ebeguVorlage);
	}

	@Override
	@Nonnull
	@PermitAll
	public Optional<EbeguVorlage> getEbeguVorlageByDatesAndKey(@Nonnull LocalDate abDate, @Nonnull LocalDate bisDate, @Nonnull EbeguVorlageKey ebeguVorlageKey) {
		return getEbeguVorlageByDatesAndKey(abDate, bisDate, ebeguVorlageKey, persistence.getEntityManager());
	}

	@Override
	@Nonnull
	@PermitAll
	public Optional<EbeguVorlage> getEbeguVorlageByDatesAndKey(@Nonnull LocalDate abDate, @Nonnull LocalDate bisDate, @Nonnull EbeguVorlageKey ebeguVorlageKey, final EntityManager em) {
		final CriteriaBuilder cb = em.getCriteriaBuilder();
		final CriteriaQuery<EbeguVorlage> query = cb.createQuery(EbeguVorlage.class);
		Root<EbeguVorlage> root = query.from(EbeguVorlage.class);
		query.select(root);

		ParameterExpression<EbeguVorlageKey> keyParam = cb.parameter(EbeguVorlageKey.class, "key");
		Predicate keyPredicate = cb.equal(root.get(EbeguVorlage_.name), keyParam);

		ParameterExpression<LocalDate> dateAbParam = cb.parameter(LocalDate.class, "dateAb");
		Predicate dateAbPredicate = cb.equal(root.get(AbstractDateRangedEntity_.gueltigkeit).get(DateRange_.gueltigAb), dateAbParam);

		ParameterExpression<LocalDate> dateBisParam = cb.parameter(LocalDate.class, "dateBis");
		Predicate dateBisPredicate = cb.equal(root.get(AbstractDateRangedEntity_.gueltigkeit).get(DateRange_.gueltigBis), dateBisParam);

		query.where(keyPredicate, dateAbPredicate, dateBisPredicate);
		TypedQuery<EbeguVorlage> q = em.createQuery(query);
		q.setParameter(dateAbParam, abDate);
		q.setParameter(dateBisParam, bisDate);
		q.setParameter(keyParam, ebeguVorlageKey);
		List<EbeguVorlage> resultList = q.getResultList();
		EbeguVorlage paramOrNull = null;
		if (resultList.size() == 1) {
			paramOrNull = resultList.get(0);
		} else if (resultList.size() > 1) {
			throw new NonUniqueResultException();
		}
		return Optional.ofNullable(paramOrNull);
	}

	@Override
	@Nullable
	@PermitAll
	public EbeguVorlage updateEbeguVorlage(@Nonnull EbeguVorlage ebeguVorlage) {
		Objects.requireNonNull(ebeguVorlage);
		return persistence.merge(ebeguVorlage);
	}

	@Override
	@RolesAllowed({ ADMIN_BG, ADMIN_GEMEINDE, SUPER_ADMIN })
	public void removeVorlage(@Nonnull String id) {
		Objects.requireNonNull(id);
		Optional<EbeguVorlage> ebeguVorlage = findById(id);
		EbeguVorlage ebeguVorlageEntity = ebeguVorlage.orElseThrow(() -> new EbeguEntityNotFoundException
			("removeEbeguVorlage", ErrorCodeEnum.ERROR_ENTITY_NOT_FOUND, id));

		if (ebeguVorlageEntity.getVorlage() != null) {
			fileSaverService.remove(ebeguVorlageEntity.getVorlage().getFilepfad());
		}
		ebeguVorlageEntity.setVorlage(null);
		updateEbeguVorlage(ebeguVorlageEntity);
	}

	@Nonnull
	@Override
	@PermitAll
	public Optional<EbeguVorlage> findById(@Nonnull final String id) {
		Objects.requireNonNull(id, "id muss gesetzt sein");
		EbeguVorlage a = persistence.find(EbeguVorlage.class, id);
		return Optional.ofNullable(a);
	}

	@Nonnull
	@Override
	@PermitAll
	public Collection<EbeguVorlage> getALLEbeguVorlageByDate(@Nonnull LocalDate date) {
		return new ArrayList<>(criteriaQueryHelper.getAllInInterval(EbeguVorlage.class, date));
	}

	@Override
	@PermitAll
	@Nullable
	public Vorlage getVorlageNotrechtOeffentlicheInstitutionen(@Nonnull String language, @Nonnull BetreuungsangebotTyp angebotTyp) {
		EbeguVorlageKey key = EbeguVorlageKey.getNotrechtVorlageOeffentlicheInstitutionen(language, angebotTyp);
		return getVorlage(language, key);
	}

	@Override
	@PermitAll
	@Nullable
	public Vorlage getVorlageNotrechtPrivateInstitutionen(@Nonnull String language, @Nonnull BetreuungsangebotTyp angebotTyp) {
		EbeguVorlageKey key = EbeguVorlageKey.getNotrechtVorlagePrivateInstitutionen(language, angebotTyp);
		return getVorlage(language, key);
	}

	@Nullable
	private Vorlage getVorlage(@Nonnull String language, @Nullable EbeguVorlageKey key) {
		if (key == null) {
			return null;
		}
		try {
			Vorlage vorlage = new Vorlage();
			vorlage.setFilesize("10");
			final String filename = ServerMessageUtil.getMessage(key.name(), new Locale(language)) + ".xlsx";
			vorlage.setFilename(filename);
			// Das Defaultfile lesen und im Filesystem ablegen
			InputStream is = EbeguVorlageServiceBean.class.getResourceAsStream(key.getDefaultVorlagePath());
			byte[] bytes = IOUtils.toByteArray(is);
			String folder = Constants.TEMP_NOTVERORDNUNG;
			UploadFileInfo notrechtVorlage = fileSaverService.save(bytes, vorlage.getFilename(), folder);
			vorlage.setFilepfad(notrechtVorlage.getPathWithoutFileName() + File.separator + notrechtVorlage.getActualFilename());
			return vorlage;
		} catch (IOException | MimeTypeParseException e) {
			throw new EbeguRuntimeException("getVorlageNotrecht", "Could not create Vorlage Notrecht", e);
		}
	}
}
