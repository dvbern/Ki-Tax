/*
 * Copyright © 2018 DV Bern AG, Switzerland
 *
 * Das vorliegende Dokument, einschliesslich aller seiner Teile, ist urheberrechtlich
 * geschützt. Jede Verwertung ist ohne Zustimmung der DV Bern AG unzulässig. Dies gilt
 * insbesondere für Vervielfältigungen, die Einspeicherung und Verarbeitung in
 * elektronischer Form. Wird das Dokument einem Kunden im Rahmen der Projektarbeit zur
 * Ansicht übergeben, ist jede weitere Verteilung durch den Kunden an Dritte untersagt.
 */

package ch.dvbern.ebegu.errors;

import javax.annotation.Nonnull;
import javax.ejb.ApplicationException;

@ApplicationException(rollback = true)
public class ConnectorException extends Exception {

	private static final long serialVersionUID = 6434204197010381768L;

	public ConnectorException(@Nonnull String message) {
		super(message);
	}
}
