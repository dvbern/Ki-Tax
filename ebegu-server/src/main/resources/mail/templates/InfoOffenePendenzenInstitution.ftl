<#-- @ftlvariable name="institutionStammdaten" type="ch.dvbern.ebegu.entities.InstitutionStammdaten" -->
<#-- @ftlvariable name="configuration" type="ch.dvbern.ebegu.config.EbeguConfiguration" -->
<#-- @ftlvariable name="templateConfiguration" type="ch.dvbern.ebegu.mail.MailTemplateConfiguration" -->
From: ${configuration.senderAddress}
To: ${institutionStammdaten.mail}
Subject: <@base64Header>kiBon – Offene Pendenzen (${institutionStammdaten.institution.name})</@base64Header>
Content-Type: text/html;charset=utf-8

<html>
<head>
${templateConfiguration.mailCss}
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
    <meta name="viewport" content="width=device-width, initial-scale=1.0"/>
    <title>kiBon – Offene Pendenzen (${institutionStammdaten.institution.name})</title>

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
		Dies ist eine automatisch versendete E-Mail. Bitte antworten Sie nicht auf diese Nachricht.
	</p>

</div>

</body>

</html>
