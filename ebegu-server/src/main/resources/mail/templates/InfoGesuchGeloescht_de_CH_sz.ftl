<#-- @ftlvariable name="gesuch" type="ch.dvbern.ebegu.entities.Gesuch" -->
<#-- @ftlvariable name="senderFullName" type="java.lang.String" -->
<#-- @ftlvariable name="templateConfiguration" type="ch.dvbern.ebegu.mail.MailTemplateConfiguration" -->
<#-- @ftlvariable name="configuration" type="ch.dvbern.ebegu.config.EbeguConfiguration" -->
<#-- @ftlvariable name="empfaengerMail" type="java.lang.String" -->
<#-- @ftlvariable name="gesuchsteller" type="ch.dvbern.ebegu.entities.Gesuchsteller" -->
<#-- @ftlvariable name="isSozialdienst" type="java.lang.Boolean" -->
<#-- @ftlvariable name="gruss" type="java.lang.Boolean" -->
<#-- @ftlvariable name="fall" type="ch.dvbern.ebegu.entities.Fall"-->
<#-- @ftlvariable name="gesuchsperiode" type="ch.dvbern.ebegu.entities.Gesuchsperiode"-->
<#assign datumGeloescht = .now>
From: ${configuration.senderAddress}
To: <@base64Header>${senderFullName}</@base64Header> <${empfaengerMail}>
Subject: ${fall.getPaddedFallnummer()}, ${gesuchsperiode.getGesuchsperiodeString()}, <@base64Header>kiBon <#if configuration.isDevmode>Testsystem</#if> – <#if isSozialdienst>Der Antrag für ${gesuchsteller.fullName}<#else>Ihr Antrag</#if> wurde gelöscht</@base64Header>
Content-Type: text/html;charset=utf-8

<html>
<head>
${templateConfiguration.mailCss}
	<meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
	<meta name="viewport" content="width=device-width, initial-scale=1.0"/>
	<title>kiBon <#if configuration.isDevmode>Testsystem</#if> – <#if isSozialdienst>Der Antrag für ${gesuchsteller.fullName}<#else>Ihr Antrag</#if> wurde gelöscht</title>

</head>

<body>

<div>
	<p>
		Guten Tag
	</p>
	<p>
		Sie haben <#if isSozialdienst>den Antrag für ${gesuchsteller.fullName}<#else>Ihren Antrag</#if> auf <a href="www.kibon.ch">www.kibon.ch</a> bearbeitet, diesen aber nicht
		freigegeben. Wir hatten Sie diesbezüglich bereits informiert.
	</p>
	<p>
		Ihre Angaben wurden am ${datumGeloescht?date} automatisch gelöscht.
	</p>
	<p>
		Freundliche Grüsse <br/>
		${gruss}
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
