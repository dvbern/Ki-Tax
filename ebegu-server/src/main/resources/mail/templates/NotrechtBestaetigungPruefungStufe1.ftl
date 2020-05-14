<#-- @ftlvariable name="institutionStammdaten" type="ch.dvbern.ebegu.entities.InstitutionStammdaten" -->
<#-- @ftlvariable name="configuration" type="ch.dvbern.ebegu.config.EbeguConfiguration" -->
<#-- @ftlvariable name="templateConfiguration" type="ch.dvbern.ebegu.mail.MailTemplateConfiguration" -->
<#-- @ftlvariable name="betrag" type="java.lang.String" -->
From: ${configuration.senderAddress}
To: ${institutionStammdaten.mail}
Subject: <@base64Header>kiBon <#if configuration.isDevmode>Testsystem / Système de test</#if> – Corona-Finanzierung
	für Kitas und TFO: Zahlung freigegeben</@base64Header>
Content-Type: text/html;charset=utf-8

<html>
<head>
    ${templateConfiguration.mailCss}
	<meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
	<meta name="viewport" content="width=device-width, initial-scale=1.0"/>
	<title>kiBon <#if configuration.isDevmode>Testsystem / Système de test</#if> – Corona-Finanzierung für Kitas und
		TFO: Zahlung
		freigegeben / Corona - financement pour les crèches et les parents de jour: Versement libéré </title>

</head>

<body>

<div>
	<p>
		Sehr geehrte Dame, sehr geehrter Herr
	</p>
	<p>
		Sie werden in den nächsten Tagen die folgende Zahlung erhalten:
	</p>
	<p>
        <#if institutionStammdaten.betreuungsangebotTyp.isKita()>
			Für Stunden/ Plätze, welche nicht mehr angeboten wurden: CHF ${betrag}
        </#if>
        <#if institutionStammdaten.betreuungsangebotTyp.isTagesfamilien()>
			Für Elterngebühren für die Betreuung für coronabedingte Abwesenheiten: CHF ${betrag}
        </#if>
	</p>
	<p>
		Hier können Sie sehen, ob im Vergleich zu Ihrer Eingabe Änderungen vorgenommen wurden. Die definitive
		Abrechnung mit Begründung von Abweichungen zur Eingabe erfolgt nach der detaillierten Prüfung Ihres Gesuchs in
		der Stufe 2.
	</p>
	<p>
		<b>
			Bitte beachten Sie: Die zweite Stufe müssen Sie ebenfalls bis spätestens 31. Juli 2020 freigeben. Ansonsten
			verwirkt Ihr Anspruch auf Leistungen und der Kanton wird die geleistete Zahlung zurückfordern.
		</b>
	</p>
	<p>
		Für die definitive Abrechnung müssen Sie die folgenden Dokumente hochladen:
	</p>
	<ul>
		<li>Entweder das ausgefüllte Excel-Formular oder sonstige Dokumente, aus denen die im Excel-Formular enthaltenen
			Angaben ersichtlich sind.
		</li>
		<li>Die Kommunikation mit den Eltern (alle Rundschreiben, Rundmails, interne Sprachregelungen) bezüglich der
			Betreuung zwischen dem 17. März und 16. Mai 2020.
		</li>
		<li>Einsatzpläne für die Zeit vom 17. März bis 16. Mai, aus denen ersichtlich ist, welches Personal
			einsatzbereit war.
		</li>
	</ul>
	<p>
		Stichprobenartig können auch die Elternverträge und Rechnungen für die betroffene Periode angefordert werden.
		Eltern können kontaktiert werden um die Angaben zur Kommunikation zu plausibilisieren.
	</p>
	<p>
		Sie können zudem noch Angaben, die Sie während der ersten Stufe gemacht haben, korrigieren.
	</p>
	<p>
		Nach der Freigabe der Stufe 2 findet die detaillierte Prüfung der Angaben statt. Das Resultat der Prüfung ist
		eine anfechtbare Verfügung, welche die definitiven Beträge festlegt. Sie enthält auch Angaben darüber, ob der
		Kanton allenfalls Rückforderungen stellt oder eine Nachzahlung tätigt muss.
	</p>
	<p>
		Abteilung Familie Kanton Bern
		info.fam@be.ch
		031 633 78 91
	</p>
	<p>
		Den aktuellen Stand Ihrer Rückforderungsformulare finden Sie unter <a
				href="<#if configuration.clientUsingHTTPS>https://<#else>http://</#if>${configuration.hostname}/corona-finanzierung/list">https://kibon.ch/corona-finanzierung/list</a>
	</p>
    <#if configuration.isDevmode>
		<p>
			<b>Hierbei handelt es sich um eine Nachricht von einem Testsystem. Dieses Testsystem wird für Schulungen
				verwendet. Über dieses System abgehandelte Gesuche verfügen über keine Zahlungsberechtigung!</b><br><br>
		</p>
    </#if>
</div>

<div>
	<p>
		Chère madame, cher monsieur,
	</p>
	<p>
		vous allez recevoir dans les prochains jour le versement suivant:
	</p>
	<p>
        <#if institutionStammdaten.betreuungsangebotTyp.isKita()>
			Pour les heures/places qui n'ont plus pu être offerte:    CHF    ${betrag}
        </#if>
        <#if institutionStammdaten.betreuungsangebotTyp.isTagesfamilien()>
			Pour les tarifs pour la garde des enfants liés aux absences dues au corona:    CHF    ${betrag}
        </#if>
	</p>
	<p>
		Vous pouvez voir ici, si il y a eu des changements en comparaison avec vos données entrées. La facturation
		définitive avec les raisons des écarts par rapport aux données entrées aura lieu après l'examen détaillé de
		votre demande lors de l'étape 2.
	</p>
	<p>
		<b>
			Remarque: vous devez également complèter la deuxième étape avant le 31 juillet 2020.
			Sinon le droit aux prestations est perdu et la canton récupèrera le versement effectué.
		</b>
	</p>
	<p>
		Pour la facturation final, vous devez nous fournir les documents suivants:
	</p>
	<ul>
		<li>Sois le forumlaire excel rempli, sois d'autres documents ou les données demandées dans le
			formulaire excel sont visible.
		</li>
		<li>Les communications avec les parents (tous les courriers à plusieurs destinataires, e-mail groupé,
			règles de languages interne) concernant la garde entre le 17 mars et le 16 mai 2020.
		</li>
		<li>Les plans d'horaires du 17 mars ou 16 mai, ou l'on peut voir quelle personne était opérationelle.
		</li>
	</ul>
	<p>
		Les contracts avec les parents ainsi que des factures pour la période concernée peuvent vous
		être demandés spontanément.
		Les parents peuvent être contacter afin de vérifier les données concernant les communications.
	</p>
	<p>
		Vous pouvez également corriger toutes les informations que vous avez fournies lors de la première étape.
	</p>
	<p>
		Il y aura une vérification détaillée des données après la libération de l'étape 2. Le résultat de la
		vérification est une décision contestable, qui fixe les montants définitifs. Elle contiendra aussi des
		informations indiquant si le canton effectue une demande de restitution ou si un versement supplémentaire
		doit être effectué.
	</p>
	<p>
		Abteilung Familie Kanton Bern
		info.fam@be.ch
		031 633 78 91
	</p>
	<p>
		Vous pouvez consulter l'état actuel de votre formulaire de demande de restitution sous <a
				href="<#if configuration.clientUsingHTTPS>https://<#else>http://</#if>${configuration.hostname}/corona-finanzierung/list">https://kibon.ch/corona-finanzierung/list</a>
	</p>
    <#if configuration.isDevmode>
		<p>
			<b>Le présent message est envoyé par un système test utilisé pour les tutoriels. Les demandes via ce système
				ne donnent pas droit à un versement.</b><br><br>
		</p>
    </#if>
</div>

</body>

</html>