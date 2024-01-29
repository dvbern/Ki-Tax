package ch.dvbern.ebegu.api.resource.authentication;

import ch.dvbern.ebegu.entities.Mandant;
import ch.dvbern.ebegu.enums.UserRole;
import ch.dvbern.ebegu.util.mandant.MandantVisitor;

public class VerfuegungDownloadAuthenticatorVisitor implements MandantVisitor<Boolean> {

	private final UserRole role;

	public VerfuegungDownloadAuthenticatorVisitor(UserRole role) {
		this.role = role;
	}

	public Boolean isUserAllowed(Mandant mandant) {
		return mandant.getMandantIdentifier().accept(this);
	}

	@Override
	public Boolean visitBern() {
		return Boolean.TRUE;
	}

	@Override
	public Boolean visitLuzern() {
		return !this.role.isInstitutionRole();
	}

	@Override
	public Boolean visitSolothurn() {
		return Boolean.TRUE;
	}

	@Override
	public Boolean visitAppenzellAusserrhoden() {
		return Boolean.TRUE;
	}

	@Override
	public Boolean visitSchwyz() {
		return this.visitSolothurn();
	}
}
