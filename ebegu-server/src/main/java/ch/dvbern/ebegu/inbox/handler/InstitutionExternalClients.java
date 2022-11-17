package ch.dvbern.ebegu.inbox.handler;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import ch.dvbern.ebegu.entities.InstitutionExternalClient;

public class InstitutionExternalClients {

	@Nullable
	private final InstitutionExternalClient relevantClient;

	@Nonnull
	private final Collection<InstitutionExternalClient> other;

	public InstitutionExternalClients(
		@Nullable InstitutionExternalClient relevantClient,
		@Nonnull Collection<InstitutionExternalClient> other) {
		this.relevantClient = relevantClient;
		this.other = other;
	}

	public InstitutionExternalClients() {
		this.relevantClient = null;
		this.other = Collections.emptyList();
	}

	@Nonnull
	public Optional<InstitutionExternalClient> getRelevantClient() {
		return Optional.ofNullable(relevantClient);
	}

	@Nonnull
	public Collection<InstitutionExternalClient> getOther() {
		return other;
	}
}
