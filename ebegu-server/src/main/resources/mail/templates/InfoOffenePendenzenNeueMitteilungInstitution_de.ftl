<#-- @ftlvariable name="institutionStammdaten" type="ch.dvbern.ebegu.entities.InstitutionStammdaten" -->
<#-- @ftlvariable name="configuration" type="ch.dvbern.ebegu.config.EbeguConfiguration" -->
<#-- @ftlvariable name="templateConfiguration" type="ch.dvbern.ebegu.mail.MailTemplateConfiguration" -->
<#-- @ftlvariable name="offenePendenzen" type="java.lang.Boolean" -->
<#-- @ftlvariable name="ungelesendeMitteilung" type="java.lang.Boolean" -->
<#-- @ftlvariable name="hostname" type="java.lang.String" -->
From: ${configuration.senderAddress}
To: ${institutionStammdaten.mail}
Subject: <@base64Header>kiBon <#if configuration.isDevmode>Testsystem / Système de test</#if> –  <#if offenePendenzen>Offene Pendenzen</#if><#if ungelesendeMitteilung && offenePendenzen> und</#if><#if ungelesendeMitteilung> neue Mitteilungen</#if> (${institutionStammdaten.institution.name}) / <#if offenePendenzen>Confirmation de places en attente</#if><#if ungelesendeMitteilung && offenePendenzen> et</#if><#if ungelesendeMitteilung> nouveau message</#if> (${institutionStammdaten.institution.name})</@base64Header>
Content-Type: text/html;charset=utf-8

<html>
<head>
${templateConfiguration.mailCss}
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
    <meta name="viewport" content="width=device-width, initial-scale=1.0"/>
    <title>kiBon <#if configuration.isDevmode>Testsystem / Système de test</#if>– <#if offenePendenzen>Offene Pendenzen</#if><#if ungelesendeMitteilung && offenePendenzen> und</#if><#if ungelesendeMitteilung> neue Mitteilungen</#if> (${institutionStammdaten.institution.name}) / <#if offenePendenzen>Confirmation de places en attente</#if><#if ungelesendeMitteilung && offenePendenzen> et</#if><#if ungelesendeMitteilung> nouveau message</#if> (${institutionStammdaten.institution.name})</title>

</head>

<body>

<div>
	<p>
		Guten Tag
	</p>
    <#if offenePendenzen>
	<p>
		Wir möchten Sie darüber informieren, dass für Ihre Institution ${institutionStammdaten.institution.name} in kiBon Pendenzen offen sind. <br>
		Sie können diese <a href="<#if configuration.clientUsingHTTPS>https://<#else>http://</#if>${hostname}/pendenzenBetreuungen">hier</a> einsehen.
	</p>
    </#if>
    <#if ungelesendeMitteilung>
	<p>
		Wir möchten Sie <#if offenePendenzen> ausserdem </#if>darüber informieren, dass Sie für Ihre Institution ${institutionStammdaten.institution.name} ungelesene Nachrichten im Posteingang haben.
		Sie können diese <a href="<#if configuration.clientUsingHTTPS>https://<#else>http://</#if>${hostname}/posteingang">hier</a> einsehen.
	</p>
	</#if>
	<p>
		<#if configuration.isDevmode>
		<b>Hierbei handelt es sich um eine Nachricht von einem Testsystem. Dieses Testsystem wird für Schulungen verwendet. Über dieses System abgehandelte Anträge verfügen über keine Zahlungsberechtigung!</b><br><br>
		</#if>
		Dies ist eine automatisch versendete E-Mail. Bitte antworten Sie nicht auf diese Nachricht.
	</p>
</div>

</body>

</html>
