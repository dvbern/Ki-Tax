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

package ch.dvbern.ebegu.util;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Optional;

import javax.annotation.Nonnull;
import javax.enterprise.context.ContextNotActiveException;
import javax.enterprise.inject.spi.CDI;
import javax.persistence.PrePersist;
import javax.persistence.PreRemove;
import javax.persistence.PreUpdate;

import ch.dvbern.ebegu.authentication.PrincipalBean;
import ch.dvbern.ebegu.entities.AbstractEntity;
import ch.dvbern.ebegu.entities.Benutzer;
import ch.dvbern.ebegu.entities.Betreuung;
import ch.dvbern.ebegu.entities.Fall;
import ch.dvbern.ebegu.entities.Gemeinde;
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.entities.GesuchDeletionLog;
import ch.dvbern.ebegu.entities.KindContainer;
import ch.dvbern.ebegu.entities.Mandant;
import ch.dvbern.ebegu.entities.Verfuegung;
import ch.dvbern.ebegu.enums.ErrorCodeEnum;
import ch.dvbern.ebegu.enums.GesuchDeletionCause;
import ch.dvbern.ebegu.enums.SequenceType;
import ch.dvbern.ebegu.enums.UserRole;
import ch.dvbern.ebegu.errors.EbeguRuntimeException;
import ch.dvbern.ebegu.services.BenutzerService;
import ch.dvbern.ebegu.services.FallService;
import ch.dvbern.ebegu.services.GesuchDeletionLogService;
import ch.dvbern.ebegu.services.KindService;
import ch.dvbern.ebegu.services.MandantService;
import ch.dvbern.ebegu.services.SequenceService;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AbstractEntityListener {

	private static final Logger LOGGER = LoggerFactory.getLogger(AbstractEntityListener.class);

	private static PrincipalBean principalBean = null;
	private FallService fallService;
	private KindService kindService;
	private SequenceService sequenceService;
	private GesuchDeletionLogService deletionLogService;

	private BenutzerService benutzerService;

	@SuppressFBWarnings(value = "LI_LAZY_INIT_STATIC", justification = "Auch wenn das vlt. mehrfach initialisiert wird... das macht nix, solange am Ende was Richtiges drinsteht")
	private static PrincipalBean getPrincipalBean() {
		if (principalBean == null) {
			//FIXME: das ist nur ein Ugly Workaround, weil CDI-Injection (mal wieder) buggy ist.
			//noinspection NonThreadSafeLazyInitialization
			principalBean = CDI.current().select(PrincipalBean.class).get();
		}
		return principalBean;
	}

	@PrePersist
	protected void prePersist(@Nonnull AbstractEntity entity) {
		LocalDateTime now = LocalDateTime.now();
		entity.setTimestampErstellt(now);
		entity.setTimestampMutiert(now);
		entity.setUserErstellt(getPrincipalName());
		entity.setUserMutiert(getPrincipalName());
		if (entity instanceof KindContainer && !((KindContainer) entity).hasVorgaenger()
			&& ((KindContainer) entity).getKindNummer() <= -1) {
			// Neue Kind-Nummer: nur setzen, wenn es nicht ein "kopiertes" Kind (Mutation oder Erneuerungsgesuch) ist
			// in diesen Faellen ist dann bereits eine Nummer gesetzt und wir setzen hier keine neue
			// !entity.hasVorgaenger() ist ueberfluessig, trotzdem wird als Doppelcheck nicht entfernt.
			KindContainer kind = (KindContainer) entity;
			Optional<Fall> optFall = getFallService().findFall(kind.getGesuch().getFall().getId());
			if (optFall.isPresent()) {
				Fall fall = optFall.get();
				kind.setKindNummer(fall.getNextNumberKind());
				fall.setNextNumberKind(fall.getNextNumberKind() + 1);
			}
		} else if (entity instanceof Betreuung && !((Betreuung) entity).hasVorgaenger()) {
			// Neue Betreuungs-Nummer: nur setzen, wenn es nicht eine "kopierte" Betreuung ist
			Betreuung betreuung = (Betreuung) entity;
			Optional<KindContainer> optKind = getKindService().findKind(betreuung.getKind().getId());
			if (optKind.isPresent()) {
				KindContainer kindContainer = optKind.get();
				betreuung.setBetreuungNummer(kindContainer.getNextNumberBetreuung());
				kindContainer.setNextNumberBetreuung(kindContainer.getNextNumberBetreuung() + 1);
			}
		} else if (entity instanceof Fall) {
			Fall fall = (Fall) entity;
			Mandant mandant = getPrincipalBean().getMandant();
			Long nextFallNr = getSequenceService().createNumberTransactional(SequenceType.FALL_NUMMER, Objects.requireNonNull(mandant));
			fall.setFallNummer(nextFallNr);
			fall.setMandant(mandant);
			if (getPrincipalBean().isCallerInRole(UserRole.GESUCHSTELLER)) {
				Optional<Benutzer> benutzer = getBenutzerService().findBenutzer(getPrincipalName());
				fall.setBesitzer(benutzer.orElseThrow(() -> new EbeguRuntimeException("findBenutzer", ErrorCodeEnum.ERROR_ENTITY_NOT_FOUND, getPrincipalName())));

			}
		} else if (entity instanceof Verfuegung) {
			// Verfuegung darf erst erstellt werden, wenn die Betreuung verfuegt ist
			Verfuegung verfuegung = (Verfuegung) entity;
			if (!verfuegung.getBetreuung().getBetreuungsstatus().isGeschlossenJA()) {
				throw new IllegalStateException("Verfuegung darf nicht gespeichert werden, wenn die Betreuung nicht verfuegt ist");
			}
		}
	}

	@PreUpdate
	public void preUpdate(@Nonnull AbstractEntity entity) {
		if (!entity.isSkipPreUpdate()) {
			entity.setTimestampMutiert(LocalDateTime.now());
			entity.setUserMutiert(getPrincipalName());
			if (entity instanceof Verfuegung) {
				throw new IllegalStateException("Verfuegung darf eigentlich nur einmal erstellt werden, wenn die Betreuung verfuegt ist, und nie mehr veraendert");

			}
		}
	}

	@PreRemove
	public void preRemove(@Nonnull AbstractEntity entity) {
		if (entity instanceof Gesuch) {
			// Ueberpruefen, ob ein DeletionLog-Eintrag erstellt wurde
			Optional<GesuchDeletionLog> gesuchDeletionLogByGesuch = getGesuchDeletionLogService().findGesuchDeletionLogByGesuch(entity.getId());
			if (!gesuchDeletionLogByGesuch.isPresent()) {
				GesuchDeletionLog gesuchDeletionLog = getGesuchDeletionLogService().saveGesuchDeletionLog(
					new GesuchDeletionLog((Gesuch) entity, GesuchDeletionCause.UNBEKANNT));
				LOGGER.error("Achtung, es wurde ein Gesuch geloescht, welches noch keinen GesuchDeletionLog-Eintrag hat! Erstelle diesen. ID={}",
					gesuchDeletionLog.getId());
			}
		}
	}

	private FallService getFallService() {
		if (fallService == null) {
			//FIXME: das ist nur ein Ugly Workaround, weil CDI-Injection in Wildfly 10 nicht funktioniert.
			//noinspection NonThreadSafeLazyInitialization
			fallService = CDI.current().select(FallService.class).get();
		}
		return fallService;
	}

	private KindService getKindService() {
		if (kindService == null) {
			//FIXME: das ist nur ein Ugly Workaround, weil CDI-Injection in Wildfly 10 nicht funktioniert.
			//noinspection NonThreadSafeLazyInitialization
			kindService = CDI.current().select(KindService.class).get();
		}
		return kindService;
	}

	private BenutzerService getBenutzerService() {
		if (benutzerService == null) {
			//FIXME: das ist nur ein Ugly Workaround, weil CDI-Injection in Wildfly 10 nicht funktioniert.
			//noinspection NonThreadSafeLazyInitialization
			benutzerService = CDI.current().select(BenutzerService.class).get();
		}
		return benutzerService;
	}

	private SequenceService getSequenceService() {
		if (sequenceService == null) {
			//FIXME: das ist nur ein Ugly Workaround, weil CDI-Injection in Wildfly 10 nicht funktioniert.
			//noinspection NonThreadSafeLazyInitialization
			sequenceService = CDI.current().select(SequenceService.class).get();
		}
		return sequenceService;
	}

	private GesuchDeletionLogService getGesuchDeletionLogService() {
		if (deletionLogService == null) {
			//FIXME: das ist nur ein Ugly Workaround, weil CDI-Injection in Wildfly 10 nicht funktioniert.
			//noinspection NonThreadSafeLazyInitialization
			deletionLogService = CDI.current().select(GesuchDeletionLogService.class).get();
		}
		return deletionLogService;
	}

	private String getPrincipalName() {
		try {
			return getPrincipalBean().getPrincipal().getName();
		} catch (ContextNotActiveException e) {
			LOGGER.error("No context when persisting entity.");
			throw e;
		}
	}
}
