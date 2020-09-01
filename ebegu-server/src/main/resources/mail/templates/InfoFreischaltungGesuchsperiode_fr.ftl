<#-- @ftlvariable name="gesuch" type="ch.dvbern.ebegu.entities.Gesuch" -->
<#-- @ftlvariable name="gesuchsteller" type="ch.dvbern.ebegu.entities.Gesuchsteller" -->
<#-- @ftlvariable name="templateConfiguration" type="ch.dvbern.ebegu.mail.MailTemplateConfiguration" -->
<#-- @ftlvariable name="configuration" type="ch.dvbern.ebegu.config.EbeguConfiguration" -->
<#-- @ftlvariable name="empfaengerMail" type="java.lang.String" -->
<#-- @ftlvariable name="startDatum" type="java.lang.String" -->
<#-- @ftlvariable name="gesuchsperiode" type="ch.dvbern.ebegu.entities.Gesuchsperiode" -->
From: ${configuration.senderAddress}
To: <@base64Header>${gesuchsteller.fullName}</@base64Header> <${empfaengerMail}>
Subject: <@base64Header>kiBon <#if configuration.isDevmode>Système de test</#if> – Activation de la nouvelle période couverte par la demande</@base64Header>
Content-Type: text/html;charset=utf-8

<html>
<head>
${templateConfiguration.mailCss}
	<meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
	<meta name="viewport" content="width=device-width, initial-scale=1.0"/>
	<title>kiBon <#if configuration.isDevmode>Système de test</#if> – Activation de la nouvelle période couverte par la demande</title>
</head>

<body>

<div>
	<p>
		Bonjour,
	</p>
	<p>
		Nous avons le plaisir de vous informer que la période couverte par la demande ${gesuchsperiode.gesuchsperiodeString} est dès à présent active dans kiBon
		pour la saisie de vos données. <br>
		Vous pouvez saisir une nouvelle demande <a href="https://www.kibon.ch/">ici</a>.
	</p>
	<p>
		Veuillez noter que le bon de garde est émis pour le mois suivant le dépôt de la demande, à condition qu'elle soit assortie de tous les documents requis,
		et pour le début de la prise en charge dans le cadre de la nouvelle période.
	</p>
	<p>
		Si vous ne souhaitez pas déposer de demande pour la période ${gesuchsperiode.gesuchsperiodeString}, vous n'avez plus rien à entreprendre.
	<p>
		Nous vous présentons nos meilleures salutations.<br/>
		Votre commune ${gesuch.dossier.gemeinde.name}
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
