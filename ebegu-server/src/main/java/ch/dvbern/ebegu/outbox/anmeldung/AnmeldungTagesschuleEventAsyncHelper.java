package ch.dvbern.ebegu.outbox.anmeldung;

import ch.dvbern.ebegu.entities.AnmeldungTagesschule;
import ch.dvbern.ebegu.entities.Mandant;
import ch.dvbern.ebegu.outbox.ExportedEvent;
import ch.dvbern.ebegu.services.ApplicationPropertyService;
import ch.dvbern.lib.cdipersistence.Persistence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Resource;
import javax.ejb.Asynchronous;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import javax.transaction.TransactionSynchronizationRegistry;

@Stateless
public class AnmeldungTagesschuleEventAsyncHelper {

	private static final Logger LOG = LoggerFactory.getLogger(AnmeldungTagesschuleEventAsyncHelper.class);

	@Resource
	private TransactionSynchronizationRegistry txReg;

	@Inject
	private Persistence persistence;

	@Inject
	private Event<ExportedEvent> event;

	@Inject
	private ApplicationPropertyService applicationPropertyService;

	@Inject
	private AnmeldungTagesschuleEventConverter anmeldungTagesschuleEventConverter;

	@Asynchronous
	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public void convert(String id) {
		AnmeldungTagesschule anmeldung = persistence.find(AnmeldungTagesschule.class, id);

		Mandant mandant = anmeldung.extractGesuch().extractMandant();

		if (!applicationPropertyService.isPublishSchnittstelleEventsAktiviert(mandant)) {
			return;
		}

		LOG.info(
			"Converting {} in Thread {} and Transaction {}",
			anmeldung.getBGNummer(),
			Thread.currentThread(),
			txReg.getTransactionKey());

		this.event.fire(anmeldungTagesschuleEventConverter.of(anmeldung));
		anmeldung.setEventPublished(true);
		persistence.merge(anmeldung);
	}
}
