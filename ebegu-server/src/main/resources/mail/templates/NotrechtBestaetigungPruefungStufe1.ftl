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
	<title>kiBon <#if configuration.isDevmode>Testsystem / Système de test</#if> – Corona-Finanzierung für Kitas und TFO: Zahlung
		freigegeben</title>

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
		<li>Die Kommunikation mit den Eltern (alle Rundschreiben, Rundmails, interne Sprach-regelungen) bezüglich der
			Betreuung zwischen dem 17. März und 16. Mai 2020.
		</li>
		<li>Einsatzpläne für die Zeit vom 17. März bis 16. Mai, aus denen ersichtlich ist, welches Personal
			einsatzbereit war.
		</li>
	</ul>
	<p>
		Stichprobenartig können auch die Elternverträge und Rechnungen für die betroffene Periode angefordert werden.
		Eltern können kontaktiert werden um die Angaben zur Kommunika-tion zu plausibilisieren.
	</p>
	<p>
		Sie können zudem noch Angaben, die Sie während der ersten Stufe gemacht haben, korri-gieren.
	</p>
	<p>
		Nach der Freigabe der Stufe 2 findet die detaillierte Prüfung der Angaben statt. Das Resultat der Prüfung ist
		eine
		anfechtbare Verfügung, welche die definitiven Beträge festlegt. Sie enthält auch Angaben darüber, ob der Kanton
		allenfalls Rückforderungen stellt oder eine Nachzahlung tätigt muss.
	</p>
	<p>
		Abteilung Familie Kanton Bern
		info.fam@be.ch
		031 633 78 91
	</p>
    <#if configuration.isDevmode>
		<p>
			<b>Hierbei handelt es sich um eine Nachricht von einem Testsystem. Dieses Testsystem wird für Schulungen
				verwendet. Über dieses System abgehandelte Gesuche verfügen über keine Zahlungsberechtigung!</b><br><br>
		</p>
    </#if>
</div>

</body>

</html>