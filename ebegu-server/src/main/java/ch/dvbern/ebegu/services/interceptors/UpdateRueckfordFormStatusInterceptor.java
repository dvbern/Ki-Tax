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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package ch.dvbern.ebegu.services.interceptors;

import javax.inject.Inject;
import javax.interceptor.AroundInvoke;
import javax.interceptor.InvocationContext;

import ch.dvbern.ebegu.authentication.PrincipalBean;
import ch.dvbern.ebegu.config.EbeguConfiguration;
import ch.dvbern.ebegu.entities.RueckforderungFormular;
import ch.dvbern.ebegu.enums.RueckforderungStatus;
import ch.dvbern.ebegu.enums.UserRole;
import ch.dvbern.ebegu.services.RueckforderungFormularService;
import ch.dvbern.lib.cdipersistence.Persistence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static ch.dvbern.ebegu.enums.UserRole.ADMIN_INSTITUTION;
import static ch.dvbern.ebegu.enums.UserRole.SACHBEARBEITER_INSTITUTION;

public class UpdateRueckfordFormStatusInterceptor {

	private static final Logger LOG =
		LoggerFactory.getLogger(UpdateRueckfordFormStatusInterceptor.class.getSimpleName());

	private static final UserRole[] INSTITUTION_ROLEN = { ADMIN_INSTITUTION, SACHBEARBEITER_INSTITUTION };

	@Inject
	private PrincipalBean principalBean;

	@Inject
	private Persistence persistence;

	@Inject
	private RueckforderungFormularService rueckforderungFormularService;

	@Inject
	private EbeguConfiguration configuration;

	@SuppressWarnings("PMD.SignatureDeclareThrowsException")
	@AroundInvoke
	public Object maybeChangeGesuchstatusToInBearbeitung(InvocationContext ctx) throws Exception {

		if (ctx.getParameters() != null && ctx.getParameters().length != 0) {
			String rueckforderungFormularID = ctx.getParameters()[0] instanceof String ?
				(String) ctx.getParameters()[0] : null;
			if (rueckforderungFormularID != null) {
				RueckforderungFormular rueckforderungFormular = persistence.find(RueckforderungFormular.class,
					rueckforderungFormularID);
				if (rueckforderungFormular == null) {
					LOG.info("RueckforderungFormular mit ID {} wurde nicht in der DB gefunden",
						rueckforderungFormularID);
				} else {
					if (principalBean.isCallerInAnyOfRole(INSTITUTION_ROLEN) && RueckforderungStatus.EINGELADEN == rueckforderungFormular.getStatus()) {
						changeRueckforderungFormularStatus(rueckforderungFormular,
							RueckforderungStatus.IN_BEARBEITUNG_INSTITUTION_STUFE_1);
					}
				}
			}
		}
		return ctx.proceed();
	}

	private void changeRueckforderungFormularStatus(RueckforderungFormular rueckforderungFormular,
		RueckforderungStatus newStatus) {
		rueckforderungFormular.setStatus(newStatus);
		rueckforderungFormularService.save(rueckforderungFormular);

		if (configuration.getIsDevmode() || LOG.isDebugEnabled()) {
			LOG.info("RueckforderungFormular wurde in den Status {} gesetzt. ID {}", newStatus,
				rueckforderungFormular.getId());
		}
	}
}
