/*
 * Copyright (C) 2018 DV Bern AG, Switzerland
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

package ch.dvbern.ebegu.validation;

import java.util.Locale;

import javax.validation.MessageInterpolator;
import javax.validation.Validation;

import ch.dvbern.ebegu.i18n.LocaleThreadLocal;

/**
 * Der normale MessageInterpolator verwendet das defaultLocale der VM und ubersetzt die Texte daher nicht korrekt.
 * Wird eine Validierung von Hand angestossen soll dieser Validator verwendet werden dem mitgegeben werden kann in
 * welchem
 * Locale die Texte erstellt werden sollen
 */
public class LocaleAwareMessageInterpolator implements MessageInterpolator {

	private MessageInterpolator delegate;

	public LocaleAwareMessageInterpolator() {
		// CDI
		this.delegate = Validation.byDefaultProvider().configure().getDefaultMessageInterpolator();
	}

	public LocaleAwareMessageInterpolator(MessageInterpolator delegate) {
		this.delegate = delegate;
	}

	@Override
	public String interpolate(String message, MessageInterpolator.Context context) {
		return delegate.interpolate(message, context, LocaleThreadLocal.get());
	}

	@Override
	public String interpolate(String message, MessageInterpolator.Context context, Locale locale) {
		return delegate.interpolate(message, context, locale);
	}
}
