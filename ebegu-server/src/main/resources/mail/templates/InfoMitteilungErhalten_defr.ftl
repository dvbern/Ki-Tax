<#-- @ftlvariable name="mitteilung" type="ch.dvbern.ebegu.entities.Mitteilung" -->
<#-- @ftlvariable name="templateConfiguration" type="ch.dvbern.ebegu.mail.MailTemplateConfiguration" -->
<#-- @ftlvariable name="configuration" type="ch.dvbern.ebegu.config.EbeguConfiguration" -->
<#-- @ftlvariable name="empfaengerMail" type="java.lang.String" -->
From: ${configuration.senderAddress}
To: <@base64Header>${mitteilung.empfaenger.fullName}</@base64Header> <${empfaengerMail}>
Subject: <@base64Header>kiBon <#if configuration.isDevmode>Testsystem / Système de test</#if> - Neue Nachricht der Gemeinde ${mitteilung.dossier.gemeinde.name} / Nouveau message de la commune ${mitteilung.dossier.gemeinde.name}</@base64Header>
Content-Type: text/html;charset=utf-8

<html>
<head>
${templateConfiguration.mailCss}
	<meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
	<meta name="viewport" content="width=device-width, initial-scale=1.0"/>
	<title>kiBon <#if configuration.isDevmode>Testsystem / Système de test</#if> - Neue Nachricht der Gemeinde ${mitteilung.dossier.gemeinde.name} / Nouveau message de la commune ${mitteilung.dossier.gemeinde.name}</title>

</head>

<body>

<div>
	<p>
		Sehr geehrte Familie
	</p>
	<p>
		Die Gemeinde ${mitteilung.dossier.gemeinde.name} hat Ihnen eine
		<a href="<#if configuration.clientUsingHTTPS>https://<#else>http://</#if>${configuration.hostname}/mitteilungen/${mitteilung.dossier.fall.id}/${mitteilung.dossier.id}/">Nachricht</a>
		geschrieben.
	</p>
	<p>
		Freundliche Grüsse <br/>
		Ihre Gemeinde ${mitteilung.dossier.gemeinde.name}
	</p>
	<p>
		<#if configuration.isDevmode>
		<b>Hierbei handelt es sich um eine Nachricht von einem Testsystem. Dieses Testsystem wird für Schulungen verwendet. Über dieses System abgehandelte Gesuche verfügen über keine Zahlungsberechtigung!</b><br><br>
		</#if>
		Dies ist eine automatisch versendete E-Mail. Bitte antworten Sie nicht auf diese Nachricht.
	</p>

	<hr>

	<p>
		Chère famille,
	</p>
	<p>
		Votre commune vous a envoyé un <a href="<#if configuration
	.clientUsingHTTPS>https://<#else>http://</#if>${configuration.hostname}/mitteilungen/${mitteilung.dossier.fall.id}/${mitteilung.dossier.id}/">message</a>.
	</p>
	<p>
		Nous vous présentons nos meilleures salutations.<br/>
		Votre commune ${mitteilung.dossier.gemeinde.name}
	</p>
	<p>
		<#if configuration.isDevmode>
		<b>Le présent message est envoyé par un système test utilisé pour les tutoriels. Les demandes via ce système ne donnent pas droit à un versement.</b><br><br>
		</#if>
		Merci de ne pas répondre à ce message automatique.
	</p>
</div>

</body>

</html>
