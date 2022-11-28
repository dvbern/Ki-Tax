/*
 * Copyright (C) 2022 DV Bern AG, Switzerland
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

package ch.dvbern.ebegu.services;

import java.util.Optional;

import javax.annotation.Nonnull;
import javax.ejb.EJBAccessException;
import javax.ejb.EJBTransactionRolledbackException;

import ch.dvbern.ebegu.entities.Dossier;
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.entities.Gesuchsperiode;
import ch.dvbern.ebegu.entities.NeueVeranlagungsMitteilung;
import ch.dvbern.ebegu.entities.SteuerdatenResponse;
import ch.dvbern.ebegu.enums.AntragStatus;
import ch.dvbern.ebegu.enums.ErrorCodeEnum;
import ch.dvbern.ebegu.errors.EbeguRuntimeException;
import ch.dvbern.ebegu.test.TestDataUtil;
import org.easymock.EasyMockExtension;
import org.easymock.EasyMockSupport;
import org.easymock.Mock;
import org.easymock.TestSubject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.EnumSource.Mode;

import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

/**
 * Es gibt schon eine MitteilungServiceBeanTest
 * Diese ist eine leichtere Version mit Mock anstatt Arquillian die betrifft nur die bearbeitung von neue Veranlagung
 * Mitteilungen
 */
@ExtendWith(EasyMockExtension.class)
public class NeueVeranlagungMitteilungTest extends EasyMockSupport {

	private Gesuchsperiode gesuchsperiode;
	private Dossier dossier;
	private NeueVeranlagungsMitteilung neueVeranlagungsMitteilung;

	@SuppressWarnings("InstanceVariableMayNotBeInitialized")
	@Mock
	private GesuchService gesuchService;

	@SuppressWarnings("InstanceVariableMayNotBeInitialized")
	@Mock
	private Authorizer authorizer;

	@TestSubject
	private final MitteilungServiceBean mitteilungServiceBean = new MitteilungServiceBean();

	@BeforeEach
	void setUp() {
		gesuchsperiode = TestDataUtil.createGesuchsperiodeXXYY(2020, 2021);
		dossier = TestDataUtil.createDefaultDossier();

		neueVeranlagungsMitteilung = new NeueVeranlagungsMitteilung();

	}

	@ParameterizedTest
	@EnumSource(value = AntragStatus.class,
		names = { "FREIGEGEBEN", "FREIGABEQUITTUNG" },
		mode = Mode.INCLUDE)
	void antragNochNichtFreigegeben(@Nonnull AntragStatus antragStatus) {
		SteuerdatenResponse steuerdatenResponse = new SteuerdatenResponse();
		Gesuch gesuch = TestDataUtil.createGesuch(dossier, gesuchsperiode, antragStatus);
		steuerdatenResponse.setKiBonAntragId(gesuch.getId());
		neueVeranlagungsMitteilung.setSteuerdatenResponse(steuerdatenResponse);

		expect(gesuchService.findGesuch(gesuch.getId())).andReturn(Optional.of(gesuch));
		authorizer.checkReadAuthorizationMitteilung(neueVeranlagungsMitteilung);
		expectLastCall();
		replayAll();
		try {
			mitteilungServiceBean.neueVeranlagunssmitteilungBearbeiten(neueVeranlagungsMitteilung);
		} catch (EbeguRuntimeException e) {
			assertThat(e.getErrorCodeEnum(), is(ErrorCodeEnum.ERROR_NOCH_NICHT_FREIGEGEBENE_ANTRAG));
		}

		verifyAll();
	}

	@Test
	void existingOnlineAntragMutation() {
		SteuerdatenResponse steuerdatenResponse = new SteuerdatenResponse();
		Gesuch gesuch = TestDataUtil.createGesuch(dossier, gesuchsperiode, AntragStatus.VERFUEGEN);
		steuerdatenResponse.setKiBonAntragId(gesuch.getId());
		neueVeranlagungsMitteilung.setSteuerdatenResponse(steuerdatenResponse);

		expect(gesuchService.findGesuch(gesuch.getId())).andReturn(Optional.of(gesuch));
		authorizer.checkReadAuthorizationMitteilung(neueVeranlagungsMitteilung);
		expectLastCall();
		expect(gesuchService.getNeustesGesuchFuerGesuch(gesuch)).andThrow(new EJBTransactionRolledbackException(
			"",
			new EJBAccessException()));
		replayAll();
		try {
			mitteilungServiceBean.neueVeranlagunssmitteilungBearbeiten(neueVeranlagungsMitteilung);
		} catch (EbeguRuntimeException e) {
			assertThat(e.getErrorCodeEnum(), is(ErrorCodeEnum.ERROR_EXISTING_ONLINE_MUTATION));
		}

		verifyAll();
	}

	@Test
	void gesuchGesperrt() {
		SteuerdatenResponse steuerdatenResponse = new SteuerdatenResponse();
		Gesuch gesuch = TestDataUtil.createGesuch(dossier, gesuchsperiode, AntragStatus.VERFUEGEN);
		gesuch.setGesperrtWegenBeschwerde(true);
		steuerdatenResponse.setKiBonAntragId(gesuch.getId());
		neueVeranlagungsMitteilung.setSteuerdatenResponse(steuerdatenResponse);

		expect(gesuchService.findGesuch(gesuch.getId())).andReturn(Optional.of(gesuch));
		authorizer.checkReadAuthorizationMitteilung(neueVeranlagungsMitteilung);
		expectLastCall();
		expect(gesuchService.getNeustesGesuchFuerGesuch(gesuch)).andReturn(Optional.of(gesuch));
		replayAll();
		try {
			mitteilungServiceBean.neueVeranlagunssmitteilungBearbeiten(neueVeranlagungsMitteilung);
		} catch (EbeguRuntimeException e) {
			assertThat(e.getErrorCodeEnum(), is(ErrorCodeEnum.ERROR_MUTATIONSMELDUNG_FALL_GESPERRT));
		}

		verifyAll();
	}

	@Test
	void gesuchInStatusVerfuegen() {
		SteuerdatenResponse steuerdatenResponse = new SteuerdatenResponse();
		Gesuch gesuch = TestDataUtil.createGesuch(dossier, gesuchsperiode, AntragStatus.VERFUEGEN);
		steuerdatenResponse.setKiBonAntragId(gesuch.getId());
		neueVeranlagungsMitteilung.setSteuerdatenResponse(steuerdatenResponse);

		expect(gesuchService.findGesuch(gesuch.getId())).andReturn(Optional.of(gesuch));
		authorizer.checkReadAuthorizationMitteilung(neueVeranlagungsMitteilung);
		expectLastCall();
		expect(gesuchService.getNeustesGesuchFuerGesuch(gesuch)).andReturn(Optional.of(gesuch));
		replayAll();
		try {
			mitteilungServiceBean.neueVeranlagunssmitteilungBearbeiten(neueVeranlagungsMitteilung);
		} catch (EbeguRuntimeException e) {
			assertThat(e.getErrorCodeEnum(), is(ErrorCodeEnum.ERROR_MUTATIONSMELDUNG_STATUS_VERFUEGEN));
		}

		verifyAll();
	}

}
