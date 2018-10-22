/*
 * Copyright © 2018 DV Bern AG, Switzerland
 *
 * Das vorliegende Dokument, einschliesslich aller seiner Teile, ist urheberrechtlich
 * geschützt. Jede Verwertung ist ohne Zustimmung der DV Bern AG unzulässig. Dies gilt
 * insbesondere für Vervielfältigungen, die Einspeicherung und Verarbeitung in
 * elektronischer Form. Wird das Dokument einem Kunden im Rahmen der Projektarbeit zur
 * Ansicht übergeben, ist jede weitere Verteilung durch den Kunden an Dritte untersagt.
 */

package ch.dvbern.ebegu.api.errors;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.Response.Status.Family;
import javax.ws.rs.core.Response.StatusType;
import javax.ws.rs.ext.Provider;

import ch.dvbern.ebegu.errors.ConnectorException;

@Provider
public class ConnectorExceptionMapper extends AbstractEbeguExceptionMapper<ConnectorException> {

	private class ConnectorStatusType implements StatusType {

		private final Family family = Status.CONFLICT.getFamily();
		private final int statusCode = Status.CONFLICT.getStatusCode();
		private static final String reasonPhrase = "connector error";

		@Override
		public int getStatusCode() {
			return this.statusCode;
		}

		@Override
		public Family getFamily() {
			return this.family;
		}

		@Override
		public String getReasonPhrase() {
			return this.reasonPhrase;
		}
	}

	@Override
	public Response toResponse(@Nonnull ConnectorException e) {
		// FIXME: connector does not receive the message
		return Response.status(new ConnectorStatusType())
			.entity(e.getMessage())
			.type(MediaType.TEXT_PLAIN_TYPE)
			.build();
	}

	@Nullable
	@Override
	protected Response buildViolationReportResponse(ConnectorException exception, Status status) {
		return null;
	}
}
