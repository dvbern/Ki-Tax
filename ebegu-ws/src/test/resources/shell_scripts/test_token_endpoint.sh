#!/bin/bash
#
# Copyright © 2023 DV Bern AG, Switzerland
#
# Das vorliegende Dokument, einschliesslich aller seiner Teile, ist urheberrechtlich
# geschützt. Jede Verwertung ist ohne Zustimmung der DV Bern AG unzulässig. Dies gilt
# insbesondere für Vervielfältigungen, die Einspeicherung und Verarbeitung in
# elektronischer Form. Wird das Dokument einem Kunden im Rahmen der Projektarbeit zur
# Ansicht übergeben, ist jede weitere Verteilung durch den Kunden an Dritte untersagt.
#

export TOKEN_ENDPOINT=https://nesko-int.sv.fin.be.ch/sso/auth/realms/hades-int/protocol/openid-connect/token
export SERVICE_ACCOUNT=kibon-int
export SERVICE_ACCOUNT_SECRET=CHANGEME
curl -v -s -X POST -u $SERVICE_ACCOUNT:$SERVICE_ACCOUNT_SECRET -d grant_type=client_credentials $TOKEN_ENDPOINT
