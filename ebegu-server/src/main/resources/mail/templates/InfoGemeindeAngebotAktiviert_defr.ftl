<#-- @ftlvariable name="templateConfiguration" type="ch.dvbern.ebegu.mail.MailTemplateConfiguration" -->
<#-- @ftlvariable name="configuration" type="ch.dvbern.ebegu.config.EbeguConfiguration" -->
<#-- @ftlvariable name="empfaengerMail" type="java.lang.String" -->
<#-- @ftlvariable name="gemeinde" type="ch.dvbern.ebegu.entities.Gemeinde" -->
<#-- @ftlvariable name="angebotNameDe" type="java.lang.String" -->
<#-- @ftlvariable name="angebotNameFr" type="java.lang.String" -->
<#-- @ftlvariable name="hostname" type="java.lang.String" -->
From: ${configuration.senderAddress}
To: ${empfaengerMail}
Subject: <@base64Header>kiBon <#if configuration.isDevmode>Testsystem</#if> – Angebot ${angebotNameDe} wurde aktiviert</@base64Header>
Content-Type: text/html;charset=utf-8

<html>
<head>
${templateConfiguration.mailCss}
	<meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
	<meta name="viewport" content="width=device-width, initial-scale=1.0"/>
	<title>kiBon <#if configuration.isDevmode>Testsystem</#if> – Angebot ${angebotNameDe} wurde aktiviert</title>

</head>

<body>

<div>
	<p>Das Angebot ${angebotNameDe} wurde für die Gemeinde ${gemeinde.name} aktiviert.</p>
	<p>Vervollständigen Sie das Profil für ${angebotNameDe} <a href="<#if configuration.clientUsingHTTPS>https://<#else>http://</#if>${hostname}/gemeinde/edit/${gemeinde.id}/0">hier</a>.</p>
</div>

<hr>

<div>
	<p>L'offre ${angebotNameFr} a été activée pour la commune ${gemeinde.name}.</p>
	<p>Veuillez compléter le profile pour ${angebotNameFr} <a href="<#if configuration.clientUsingHTTPS>https://<#else>http://</#if>${hostname}/gemeinde/edit/${gemeinde.id}/0">ici</a>.</p>
</div>

</body>

</html>
