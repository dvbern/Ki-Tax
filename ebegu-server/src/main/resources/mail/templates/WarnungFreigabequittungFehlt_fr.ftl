<#-- @ftlvariable name="gesuch" type="ch.dvbern.ebegu.entities.Gesuch" -->
<#-- @ftlvariable name="gesuchsteller" type="ch.dvbern.ebegu.entities.Gesuchsteller" -->
<#-- @ftlvariable name="anzahlTage" type="java.lang.String" -->
<#-- @ftlvariable name="datumLoeschung" type="java.lang.String" -->
<#-- @ftlvariable name="adresse" type="java.lang.String" -->
<#-- @ftlvariable name="templateConfiguration" type="ch.dvbern.ebegu.mail.MailTemplateConfiguration" -->
<#-- @ftlvariable name="configuration" type="ch.dvbern.ebegu.config.EbeguConfiguration" -->
From: ${configuration.senderAddress}
To: " ${gesuchsteller.fullName} <${gesuchsteller.mail}>
Subject: <@base64Header>kiBon <#if configuration.isDevmode>Système de test</#if> – Confirmation des données à remettre</@base64Header>
Content-Type: text/html;charset=utf-8

<html>
<head>
${templateConfiguration.mailCss}
	<meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
	<meta name="viewport" content="width=device-width, initial-scale=1.0"/>
	<title>kiBon <#if configuration.isDevmode>Système de test</#if> – Confirmation des données à remettre</title>

</head>

<body>

<div>
	<p>
		Bonjour,
	</p>
	<p>
		Vous avez déposé une demande via kiBon pour laquelle vos données n'ont pas encore été confirmées.
		Le formulaire, qui peut être téléchargé
		<a href="<#if configuration.clientUsingHTTPS>https://<#else>http://</#if>${configuration.hostname}/gesuch/freigabe/${gesuch.id}">ici</a>, est à remettre dûment signé et au plus vite par courrier postal à ${adresse} faute de quoi votre demande
		sera considérée comme non valable. Elle ne pourra pas être traitée et sera automatiquement supprimée le ${datumLoeschung}.
	</p>
	<p>
		Veuillez noter que le bon de garde est émis pour le mois suivant le dépôt de la demande, à condition que celle-ci soit assortie de tous les documents
		requis, et pour le début de la prise en charge dans le cadre de la nouvelle période.
	</p>
	<p>
		Nous vous présentons nos salutations les meilleures.<br/>
		Votre commune
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
