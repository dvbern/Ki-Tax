<#-- @ftlvariable name="eingeladener" type="ch.dvbern.ebegu.entities.Benutzer" -->
<#-- @ftlvariable name="configuration" type="ch.dvbern.ebegu.config.EbeguConfiguration" -->
<#-- @ftlvariable name="templateConfiguration" type="ch.dvbern.ebegu.mail.MailTemplateConfiguration" -->
<#-- @ftlvariable name="contentDE" type="java.lang.String" -->
<#-- @ftlvariable name="contentFR" type="java.lang.String" -->
<#-- @ftlvariable name="footerDE" type="java.lang.String" -->
<#-- @ftlvariable name="footerFR" type="java.lang.String" -->
<#-- @ftlvariable name="acceptLink" type="java.lang.String" -->
<#-- @ftlvariable name="acceptExpire" type="java.lang.String" -->
From: ${configuration.senderAddress}
To: ${eingeladener.email}
Subject: <@base64Header>kiBon <#if configuration.isDevmode>Testsystem</#if> – Einladung</@base64Header>
Content-Type: text/html;charset=utf-8

<html>
<head>
${templateConfiguration.mailCss}
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
    <meta name="viewport" content="width=device-width, initial-scale=1.0"/>
    <title>kiBon <#if configuration.isDevmode>Testsystem</#if> – Einladung</title>

</head>

<body>

<div>
    <p>
		${contentDE}
    </p>
	<ol type="1">
		<li>Klick auf untenstehenden Link <a href="${acceptLink}">«Registrierung starten»</a>. Sie werden auf kiBon weitergeleitet.</li>
		<li>Klick auf «Anmelden». Sie werden zur Anmeldemaske weitergeleitet. </li>
		<li>Klick auf Registrieren.</li>
		<li>Geben Sie Ihre Daten ein. Wichtig: Benützen Sie die E-Mail-Adresse, auf die Sie diese Nachricht erhalten haben. Die Mobiltelefonnummer wird für die 2-Faktor-Authentifizierung benötigt.</li>
		<li>Verifizieren Sie Ihre E-Mail-Adresse.</li>
		<li>Klick auf «Registrierung abschliessen». </li>
	</ol>
    <p>
		Wichtig: Diese Einladung ist nur bis zum <strong>${acceptExpire}</strong> gültig.
    </p>
	<p>
	<table cellspacing="0" cellpadding="0" width="100%">
		<tr>
			<td>
				<table border="0" class="mobile-button" cellspacing="0" cellpadding="0">
					<tr>
						<td align="center" bgcolor="#d50025" style="background-color: #d50025; margin: auto; max-width: 600px; -webkit-border-radius: 2px; -moz-border-radius: 2px; border-radius: 2px; padding: 10px 15px; " width="100%">
							<!--[if mso]>&nbsp;<![endif]-->
							<a href="${acceptLink}" target="_blank" style="color: #ffffff; font-weight:normal; text-align:center; background-color: #d50025; text-decoration: none; border: none; -webkit-border-radius: 2px; -moz-border-radius: 2px; border-radius: 2px; display: inline-block;">
								<span style="color: #ffffff; font-weight:normal; line-height:1.5em; text-align:center;">Registrierung starten</span>
							</a>
							<!--[if mso]>&nbsp;<![endif]-->
						</td>
					</tr>
				</table>
			</td>
		</tr>
	</table>
	</p>
    <p>
		${footerDE}
    </p>
</div>

</body>

</html>
