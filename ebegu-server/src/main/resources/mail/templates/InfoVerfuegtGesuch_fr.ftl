<#-- @ftlvariable name="gesuch" type="ch.dvbern.ebegu.entities.Gesuch" -->
<#-- @ftlvariable name="senderFullName" type="java.lang.String" -->
<#-- @ftlvariable name="templateConfiguration" type="ch.dvbern.ebegu.mail.MailTemplateConfiguration" -->
<#-- @ftlvariable name="configuration" type="ch.dvbern.ebegu.config.EbeguConfiguration" -->
<#-- @ftlvariable name="empfaengerMail" type="java.lang.String" -->
<#-- @ftlvariable name="gesuchsteller" type="ch.dvbern.ebegu.entities.Gesuchsteller" -->
<#-- @ftlvariable name="isSozialdienst" type="java.lang.Boolean" -->
<#-- @ftlvariable name="hostname" type="java.lang.String" -->
<#-- @ftlvariable name="gruss" type="java.lang.String" -->
<#-- @ftlvariable name="fall" type="ch.dvbern.ebegu.entities.Fall"-->
<#-- @ftlvariable name="gesuchsperiode" type="ch.dvbern.ebegu.entities.Gesuchsperiode"-->
From: ${configuration.senderAddress}
To: <@base64Header>${senderFullName}</@base64Header> <${empfaengerMail}>
Subject: ${fall.getPaddedFallnummer()}, ${gesuchsperiode.getGesuchsperiodeString()}, <@base64Header>kiBon <#if configuration.isDevmode>Système de test</#if> – <#if isSozialdienst>La demande pour ${gesuchsteller.fullName}<#else>Votre demande</#if> a été traitée</@base64Header>
Content-Type: text/html;charset=utf-8

<html>
<head>
${templateConfiguration.mailCss}
	<meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
	<meta name="viewport" content="width=device-width, initial-scale=1.0"/>
	<title>kiBon <#if configuration.isDevmode>Système de test</#if> – <#if isSozialdienst>La demande pour ${gesuchsteller.fullName}<#else>Votre demande</#if> a été traitée</title>

</head>

<body>

<div>
	<p>
		Chère famille,
	</p>
	<p>
        <#if isSozialdienst>La demande pour ${gesuchsteller.fullName}<#else>Votre demande</#if> a été examinée et le montant du bon a été calculé. Vous pouvez consulter les résultats
		<a href="<#if configuration.clientUsingHTTPS>https://<#else>http://</#if>${hostname}/gesuch/verfuegen/${gesuch.id}">ici</a>.
	</p>
	<p>
		Nous vous présentons nos meilleures salutations.<br/>
		${gruss}
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
