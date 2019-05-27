<#-- @ftlvariable name="institutionStammdaten" type="ch.dvbern.ebegu.entities.InstitutionStammdaten" -->
<#-- @ftlvariable name="configuration" type="ch.dvbern.ebegu.config.EbeguConfiguration" -->
<#-- @ftlvariable name="templateConfiguration" type="ch.dvbern.ebegu.mail.MailTemplateConfiguration" -->
From: ${configuration.senderAddress}
To: ${institutionStammdaten.mail}
Subject: <@base64Header>kiBon <#if configuration.isDevmode>Testsystem / Système de test</#if> – Offene Pendenzen (${institutionStammdaten.institution.name}) / Confirmation de places en attente (${institutionStammdaten.institution.name})</@base64Header>
Content-Type: text/html;charset=utf-8

<html>
<head>
${templateConfiguration.mailCss}
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
    <meta name="viewport" content="width=device-width, initial-scale=1.0"/>
    <title>kiBon <#if configuration.isDevmode>Testsystem / Système de test</#if> – Offene Pendenzen (${institutionStammdaten.institution.name}) / Confirmation de places en attente (${institutionStammdaten.institution.name})</title>

</head>

<body>

<div>
	<p>
		Guten Tag
	</p>
	<p>
		Wir möchten Sie darüber informieren, dass für Ihre Institution ${institutionStammdaten.institution.name} in kiBon Pendenzen offen sind. <br>
		Sie können diese
		<a href="<#if configuration.clientUsingHTTPS>https://<#else>http://</#if>${configuration.hostname}/pendenzenBetreuungen">hier</a>
		einsehen.
	</p>
	<p>
		<#if configuration.isDevmode>
		<b>Hierbei handelt es sich um eine Nachricht von einem Testsystem. Dieses Testsystem wird für Schulungen verwendet. Über dieses System abgehandelte Gesuche verfügen über keine Zahlungsberechtigung!</b><br><br>
		</#if>
		Dies ist eine automatisch versendete E-Mail. Bitte antworten Sie nicht auf diese Nachricht.
	</p>

	<hr>

	<p>
		Bonjour,
	</p>
	<p>
		Nous vous informons que des places sont en attente de confirmation pour l'institution ${institutionStammdaten.institution.name} dans kiBon.
		Vous pouvez les consulter
		<a href="<#if configuration.clientUsingHTTPS>https://<#else>http://</#if>${configuration.hostname}/pendenzenBetreuungen">ici</a>.
	</p>
	<p>
		<#if configuration.isDevmode>
		<b>Hierbei handelt es sich um eine Nachricht von einem Testsystem. Dieses Testsystem wird für Schulungen verwendet. Über dieses System abgehandelte Gesuche verfügen über keine Zahlungsberechtigung!</b><br><br>
		</#if>
		Merci de ne pas répondre à ce message automatique.
	</p>

</div>

</body>

</html>
