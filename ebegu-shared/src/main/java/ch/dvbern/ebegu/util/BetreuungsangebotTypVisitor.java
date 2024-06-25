package ch.dvbern.ebegu.util;

public interface BetreuungsangebotTypVisitor<T> {

	T visitKita();
	T visitTagesfamilien();
	T visitMittagstisch();
	T visitTagesschule();

	T visitFerieninsel();

}
