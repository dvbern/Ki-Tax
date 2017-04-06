/*
 * Copyright (c) 2012 DV Bern AG, Switzerland
 *
 * Das vorliegende Dokument, einschliesslich aller seiner Teile, ist urheberrechtlich
 * geschuetzt. Jede Verwertung ist ohne Zustimmung der DV Bern AG unzulaessig. Dies gilt
 * insbesondere fuer Vervielfaeltigungen, die Einspeicherung und Verarbeitung in
 * elektronischer Form. Wird das Dokument einem Kunden im Rahmen der Projektarbeit zur
 * Ansicht uebergeben ist jede weitere Verteilung durch den Kunden an Dritte untersagt.
 */

package ch.dvbern.ebegu.errors;


import ch.dvbern.ebegu.enums.ErrorCodeEnum;

/**
 * Exception, welche geworfen wird wenn beim Aufruf des EWK-Service ein Fehler passiert
 */
public class PersonenSucheServiceException extends EbeguException {

	private static final long serialVersionUID = 5438097529958118878L;


	public PersonenSucheServiceException(final String methodname, final String message) {
		super(methodname, message, ErrorCodeEnum.ERROR_PERSONENSUCHE_TECHNICAL);
	}

	public PersonenSucheServiceException(final String methodname, final String message, final Throwable cause) {
		super(methodname, message, ErrorCodeEnum.ERROR_PERSONENSUCHE_TECHNICAL, cause);
	}
}