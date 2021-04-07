<#-- @ftlvariable name="gesuch" type="ch.dvbern.ebegu.entities.Gesuch" -->
<#-- @ftlvariable name="senderFullName" type="java.lang.String" -->
<#-- @ftlvariable name="templateConfiguration" type="ch.dvbern.ebegu.mail.MailTemplateConfiguration" -->
<#-- @ftlvariable name="configuration" type="ch.dvbern.ebegu.config.EbeguConfiguration" -->
<#-- @ftlvariable name="empfaengerMail" type="java.lang.String" -->
<#-- @ftlvariable name="gesuchsteller" type="ch.dvbern.ebegu.entities.Gesuchsteller" -->
<#-- @ftlvariable name="isSozialdienst" type="java.lang.Boolean" -->
From: ${configuration.senderAddress}
To: <@base64Header>${senderFullName}</@base64Header> <${empfaengerMail}>
Subject: <@base64Header>kiBon <#if configuration.isDevmode>Testsystem</#if> – <#if isSozialdienst>Antrag für ${gesuchsteller.fullName}<#else>Antrag</#if> kann freigegeben werden</@base64Header>
Content-Type: text/html;charset=utf-8

<html>
<head>
${templateConfiguration.mailCss}
	<meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
	<meta name="viewport" content="width=device-width, initial-scale=1.0"/>
	<title>kiBon <#if configuration.isDevmode>Testsystem</#if> – <#if isSozialdienst>Antrag für ${gesuchsteller.fullName}<#else>Antrag</#if> kann freigegeben werden</title>

</head>

<body>

<div>
	<p>
		Sehr geehrte Familie
	</p>
	<p>
		Für sämtliche Betreuungsangebote wurden die Plätze bestätigt. <#if isSozialdienst>Der Antrag für ${gesuchsteller.fullName}<#else>Der Antrag</#if> kann
		<a href="<#if configuration.clientUsingHTTPS>https://<#else>http://</#if>${configuration.hostname}/gesuch/freigabe/${gesuch.id}">hier</a>
		freigegeben werden.
	<p>
		Freundliche Grüsse <br/>
		Ihre Gemeinde ${gesuch.dossier.gemeinde.name}
	</p>

	<p>
		<#if configuration.isDevmode>
		<b>Hierbei handelt es sich um eine Nachricht von einem Testsystem. Dieses Testsystem wird für Schulungen verwendet. Über dieses System abgehandelte Anträge verfügen über keine Zahlungsberechtigung!</b><br><br>
		</#if>
		Dies ist eine automatisch versendete E-Mail. Bitte antworten Sie nicht auf diese Nachricht.
	</p>
</div>

</body>

</html>
