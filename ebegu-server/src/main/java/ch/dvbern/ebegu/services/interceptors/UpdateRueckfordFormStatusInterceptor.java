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
import ch.dvbern.ebegu.entities.RueckforderungFormular;
import ch.dvbern.ebegu.enums.RueckforderungStatus;
import ch.dvbern.ebegu.enums.UserRole;
import ch.dvbern.ebegu.services.RueckforderungFormularService;
import ch.dvbern.lib.cdipersistence.Persistence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UpdateRueckfordFormStatusInterceptor {

	private static final Logger LOG =
		LoggerFactory.getLogger(UpdateRueckfordFormStatusInterceptor.class.getSimpleName());

	@Inject
	private PrincipalBean principalBean;

	@Inject
	private Persistence persistence;

	@Inject
	private RueckforderungFormularService rueckforderungFormularService;


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
					if (principalBean.isCallerInAnyOfRole(UserRole.getInstitutionTraegerschaftRoles())
							&& RueckforderungStatus.EINGELADEN == rueckforderungFormular.getStatus()) {
						// Beim Speichern wird automatisch der richtige Status gesetzt
						rueckforderungFormularService.save(rueckforderungFormular);
					}
				}
			}
		}
		return ctx.proceed();
	}
}
