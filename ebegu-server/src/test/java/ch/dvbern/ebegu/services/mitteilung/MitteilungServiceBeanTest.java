package ch.dvbern.ebegu.services.mitteilung;

import java.util.Optional;

import javax.persistence.EntityManager;

import ch.dvbern.ebegu.entities.Betreuung;
import ch.dvbern.ebegu.entities.Betreuungsmitteilung;
import ch.dvbern.ebegu.entities.Dossier;
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.entities.Gesuchsperiode;
import ch.dvbern.ebegu.entities.KindContainer;
import ch.dvbern.ebegu.enums.AntragStatus;
import ch.dvbern.ebegu.enums.Eingangsart;
import ch.dvbern.ebegu.enums.FinSitStatus;
import ch.dvbern.ebegu.services.Authorizer;
import ch.dvbern.ebegu.services.GesuchService;
import ch.dvbern.ebegu.services.VerfuegungService;
import ch.dvbern.ebegu.test.TestDataUtil;
import ch.dvbern.lib.cdipersistence.Persistence;
import org.easymock.EasyMockExtension;
import org.easymock.EasyMockSupport;
import org.easymock.Mock;
import org.easymock.TestSubject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.easymock.EasyMock.anyObject;
import static org.easymock.EasyMock.anyString;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;

@ExtendWith(EasyMockExtension.class)
public class MitteilungServiceBeanTest extends EasyMockSupport {

	@SuppressWarnings("InstanceVariableMayNotBeInitialized")
	@Mock
	private GesuchService gesuchService;

	@SuppressWarnings("InstanceVariableMayNotBeInitialized")
	@Mock
	private Authorizer authorizer;

	@SuppressWarnings("InstanceVariableMayNotBeInitialized")
	@Mock
	private VerfuegungService verfuegungService;

	@Mock
	private Persistence persistence;

	@Mock
	private EntityManager entityManager;

	@TestSubject
	private MitteilungServiceBean mitteilungServiceBean = new MitteilungServiceBean();

	@Test
	public void testMutationVerfuegtWennFinSitAccepted() {
		Gesuchsperiode gesuchsperiode = TestDataUtil.createGesuchsperiodeXXYY(2020, 2021);
		Dossier dossier = TestDataUtil.createDefaultDossier();
		Gesuch gesuch = TestDataUtil.createGesuch(dossier, gesuchsperiode, AntragStatus.VERFUEGT);
		gesuch.setEingangsart(Eingangsart.ONLINE);
		gesuch.setFinSitStatus(FinSitStatus.ABGELEHNT);
		Betreuungsmitteilung betreuungsmitteilung = new Betreuungsmitteilung();
		betreuungsmitteilung.setBetreuung(new Betreuung());
		betreuungsmitteilung.getBetreuung().setKind(new KindContainer());
		betreuungsmitteilung.getBetreuung().getKind().setGesuch(gesuch);
		Gesuch mutation = TestDataUtil.createGesuch(dossier, gesuchsperiode, AntragStatus.IN_BEARBEITUNG_JA);
		mutation.setNewlyCreatedMutation(true);
		mutation.setVorgaengerId("UUID");
		expectDoApplyMitteilung(gesuch, mutation, betreuungsmitteilung);
		verfuegungService.gesuchAutomatischVerfuegen(mutation);
		expectLastCall();
		replayAll();
		mitteilungServiceBean.applyBetreuungsmitteilungIfPossible(betreuungsmitteilung);
	}
	@Test
	public void testMutationNotVerfuegtWennFinSitRejected() {
		Gesuchsperiode gesuchsperiode = TestDataUtil.createGesuchsperiodeXXYY(2020, 2021);
		Dossier dossier = TestDataUtil.createDefaultDossier();
		Gesuch gesuch = TestDataUtil.createGesuch(dossier, gesuchsperiode, AntragStatus.VERFUEGT);
		gesuch.setEingangsart(Eingangsart.ONLINE);
		gesuch.setFinSitStatus(FinSitStatus.ABGELEHNT);
		Betreuungsmitteilung betreuungsmitteilung = new Betreuungsmitteilung();
		betreuungsmitteilung.setBetreuung(new Betreuung());
		betreuungsmitteilung.getBetreuung().setKind(new KindContainer());
		betreuungsmitteilung.getBetreuung().getKind().setGesuch(gesuch);
		Gesuch mutation = TestDataUtil.createGesuch(dossier, gesuchsperiode, AntragStatus.IN_BEARBEITUNG_JA);
		mutation.setNewlyCreatedMutation(true);
		mutation.setVorgaengerId("UUID");
		expectDoApplyMitteilung(gesuch, mutation, betreuungsmitteilung);
		replayAll();
		mitteilungServiceBean.applyBetreuungsmitteilungIfPossible(betreuungsmitteilung);
		Assertions.assertEquals(AntragStatus.IN_BEARBEITUNG_JA, mutation.getStatus());
	}

	private void expectDoApplyMitteilung(Gesuch gesuch, Gesuch mutation, Betreuungsmitteilung mitteilung) {
		authorizer.checkReadAuthorizationMitteilung(mitteilung);
		expectLastCall();
		expect(gesuchService.getNeustesGesuchFuerGesuch(gesuch)).andReturn(Optional.of(gesuch));
		authorizer.checkWriteAuthorization(gesuch);
		expectLastCall();
		expect(gesuchService.createGesuch(anyObject())).andReturn(mutation);
		authorizer.checkWriteAuthorization(mutation);
		expectLastCall();
		authorizer.checkWriteAuthorization(mutation);
		expectLastCall();
		authorizer.checkReadAuthorizationMitteilung(mitteilung);
		expectLastCall();
		expect(gesuchService.updateGesuch(mutation, false, null)).andReturn(mutation);
		expect(persistence.getEntityManager()).andReturn(entityManager);
		entityManager.flush();
		expectLastCall();
		expect(gesuchService.findErstgesuchForGesuch(mutation)).andReturn(gesuch);
		expect(gesuchService.findVorgaengerGesuchNotIgnoriert(anyString())).andReturn(gesuch);
	}
}
