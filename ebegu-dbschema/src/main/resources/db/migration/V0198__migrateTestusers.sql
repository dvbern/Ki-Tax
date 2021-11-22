/*
 * Copyright (C) 2021 DV Bern AG, Switzerland
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

# these testusers had wrong email previously
UPDATE benutzer SET email = 'adrian.bernasconi.be@mailbucket.dvbern.ch' WHERE username = 'bead';
UPDATE benutzer SET email = 'adrian.huber.be@mailbucket.dvbern.ch' WHERE username = 'huad';
UPDATE benutzer SET email = 'joerg.aebischer.be@mailbucket.dvbern.ch' WHERE username = 'aejo';
UPDATE benutzer SET email = 'joerg.keller.be@mailbucket.dvbern.ch' WHERE username = 'kejo';
UPDATE benutzer SET email = 'jeanpierre.kraeuchi.be@mailbucket.dvbern.ch' WHERE username = 'krjp';
UPDATE benutzer SET email = 'julia.lori.be@mailbucket.dvbern.ch' WHERE username = 'luju';
UPDATE benutzer SET email = 'julien.odermatt.be@mailbucket.dvbern.ch' WHERE username = 'odju';
UPDATE benutzer SET email = 'julien.bucheli.be@mailbucket.dvbern.ch' WHERE username = 'buju';
UPDATE benutzer SET email = 'kurt.schmid.be@mailbucket.dvbern.ch' WHERE username = 'scku';
UPDATE benutzer SET email = 'kurt.kaelin.be@mailbucket.dvbern.ch' WHERE username = 'kaku';
UPDATE benutzer SET email = 'reto.hug.be@mailbucket.dvbern.ch' WHERE username = 'hure';
UPDATE benutzer SET email = 'rodolfo.hermann.be@mailbucket.dvbern.ch' WHERE username = 'hero';
UPDATE benutzer SET email = 'rodolfo.iten.be@mailbucket.dvbern.ch' WHERE username = 'itro';
UPDATE benutzer SET email = 'reto.werlen.be@mailbucket.dvbern.ch' WHERE username = 'were';
UPDATE benutzer SET email = 'julia.adler.be@mailbucket.dvbern.ch' WHERE username = 'adju';

# change email to email with be. prefix
UPDATE benutzer SET email = 'superuser.be@mailbucket.dvbern.ch' WHERE email = 'superuser@mailbucket.dvbern.ch';
UPDATE benutzer SET email = 'bernhard.roethlisberger.be@mailbucket.dvbern.ch' WHERE email = 'bernhard.roethlisberger@mailbucket.dvbern.ch';
UPDATE benutzer SET email = 'benno.roethlisberger.be@mailbucket.dvbern.ch' WHERE email = 'benno.roethlisberger@mailbucket.dvbern.ch';
UPDATE benutzer SET email = 'silvia.bergmann.be@mailbucket.dvbern.ch' WHERE email = 'silvia.bergmann@mailbucket.dvbern.ch';
UPDATE benutzer SET email = 'sophie.bergmann.be@mailbucket.dvbern.ch' WHERE email = 'sophie.bergmann@mailbucket.dvbern.ch';
UPDATE benutzer SET email = 'serge.gainsbourg.be@mailbucket.dvbern.ch' WHERE email = 'serge.gainsbourg@mailbucket.dvbern.ch';
UPDATE benutzer SET email = 'charlotte.gainsbourg.be@mailbucket.dvbern.ch' WHERE email = 'charlotte.gainsbourg@mailbucket.dvbern.ch';
UPDATE benutzer SET email = 'agnes.krause.be@mailbucket.dvbern.ch' WHERE email = 'agnes.krause@mailbucket.dvbern.ch';
UPDATE benutzer SET email = 'bernhard.bern.be@mailbucket.dvbern.ch' WHERE email = 'bernhard.bern@mailbucket.dvbern.ch';
UPDATE benutzer SET email = 'emma.gerber.be@mailbucket.dvbern.ch' WHERE email = 'emma.gerber@mailbucket.dvbern.ch';
UPDATE benutzer SET email = 'heinrich.mueller.be@mailbucket.dvbern.ch' WHERE email = 'heinrich.mueller@mailbucket.dvbern.ch';
UPDATE benutzer SET email = 'michael.berger.be@mailbucket.dvbern.ch' WHERE email = 'michael.berger@mailbucket.dvbern.ch';
UPDATE benutzer SET email = 'hans.zimmermann.be@mailbucket.dvbern.ch' WHERE email = 'hans.zimmermann@mailbucket.dvbern.ch';
UPDATE benutzer SET email = 'patrick.melcher.be@mailbucket.dvbern.ch' WHERE email = 'patrick.melcher@mailbucket.dvbern.ch';
UPDATE benutzer SET email = 'max.palmer.be@mailbucket.dvbern.ch' WHERE email = 'max.palmer@mailbucket.dvbern.ch';
UPDATE benutzer SET email = 'kurt.blaser.be@mailbucket.dvbern.ch' WHERE email = 'kurt.blaser@mailbucket.dvbern.ch';
UPDATE benutzer SET email = 'joerg.becker.be@mailbucket.dvbern.ch' WHERE email = 'joerg.becker@mailbucket.dvbern.ch';
UPDATE benutzer SET email = 'adrian.schuler.be@mailbucket.dvbern.ch' WHERE email = 'adrian.schuler@mailbucket.dvbern.ch';
UPDATE benutzer SET email = 'julien.schuler.be@mailbucket.dvbern.ch' WHERE email = 'julien.schuler@mailbucket.dvbern.ch';
UPDATE benutzer SET email = 'gerlinde.hofstetter.be@mailbucket.dvbern.ch' WHERE email = 'gerlinde.hofstetter@mailbucket.dvbern.ch';
UPDATE benutzer SET email = 'stefan.wirth.be@mailbucket.dvbern.ch' WHERE email = 'stefan.wirth@mailbucket.dvbern.ch';
UPDATE benutzer SET email = 'marlene.stoeckli.be@mailbucket.dvbern.ch' WHERE email = 'marlene.stoeckli@mailbucket.dvbern.ch';
UPDATE benutzer SET email = 'sarah.riesen.be@mailbucket.dvbern.ch' WHERE email = 'sarah.riesen@mailbucket.dvbern.ch';
UPDATE benutzer SET email = 'rodolfo.geldmacher.be@mailbucket.dvbern.ch' WHERE email = 'rodolfo.geldmacher@mailbucket.dvbern.ch';
UPDATE benutzer SET email = 'reto.revisor.be@mailbucket.dvbern.ch' WHERE email = 'reto.revisor@mailbucket.dvbern.ch';
UPDATE benutzer SET email = 'julia.jurist.be@mailbucket.dvbern.ch' WHERE email = 'julia.jurist@mailbucket.dvbern.ch';
UPDATE benutzer SET email = 'kurt.blaser.be@mailbucket.dvbern.ch' WHERE email = 'kurt.blaser@mailbucket.dvbern.ch';
UPDATE benutzer SET email = 'joerg.becker.be@mailbucket.dvbern.ch' WHERE email = 'joerg.becker@mailbucket.dvbern.ch';
UPDATE benutzer SET email = 'adrian.schuler.be@mailbucket.dvbern.ch' WHERE email = 'adrian.schuler@mailbucket.dvbern.ch';
UPDATE benutzer SET email = 'julien.schuler.be@mailbucket.dvbern.ch' WHERE email = 'julien.schuler@mailbucket.dvbern.ch';
UPDATE benutzer SET email = 'gerlinde.bader.be@mailbucket.dvbern.ch' WHERE email = 'gerlinde.bader@mailbucket.dvbern.ch';
UPDATE benutzer SET email = 'stefan.weibel.be@mailbucket.dvbern.ch' WHERE email = 'stefan.weibel@mailbucket.dvbern.ch';
UPDATE benutzer SET email = 'rodolfo.geldmacher.be@mailbucket.dvbern.ch' WHERE email = 'rodolfo.geldmacher@mailbucket.dvbern.ch';
UPDATE benutzer SET email = 'reto.revisor.be@mailbucket.dvbern.ch' WHERE email = 'reto.revisor@mailbucket.dvbern.ch';
UPDATE benutzer SET email = 'julia.jurist.be@mailbucket.dvbern.ch' WHERE email = 'julia.jurist@mailbucket.dvbern.ch';
UPDATE benutzer SET email = 'jordan.hefti.be@mailbucket.dvbern.ch' WHERE email = 'jordan.hefti@mailbucket.dvbern.ch';
UPDATE benutzer SET email = 'kurt.blaser.be@mailbucket.dvbern.ch' WHERE email = 'kurt.blaser@mailbucket.dvbern.ch';
UPDATE benutzer SET email = 'joerg.becker.be@mailbucket.dvbern.ch' WHERE email = 'joerg.becker@mailbucket.dvbern.ch';
UPDATE benutzer SET email = 'adrian.schuler.be@mailbucket.dvbern.ch' WHERE email = 'adrian.schuler@mailbucket.dvbern.ch';
UPDATE benutzer SET email = 'julien.schuler.be@mailbucket.dvbern.ch' WHERE email = 'julien.schuler@mailbucket.dvbern.ch';
UPDATE benutzer SET email = 'gerlinde.mayer.be@mailbucket.dvbern.ch' WHERE email = 'gerlinde.mayer@mailbucket.dvbern.ch';
UPDATE benutzer SET email = 'stefan.marti.be@mailbucket.dvbern.ch' WHERE email = 'stefan.marti@mailbucket.dvbern.ch';
UPDATE benutzer SET email = 'rodolfo.geldmacher.be@mailbucket.dvbern.ch' WHERE email = 'rodolfo.geldmacher@mailbucket.dvbern.ch';
UPDATE benutzer SET email = 'reto.revisor.be@mailbucket.dvbern.ch' WHERE email = 'reto.revisor@mailbucket.dvbern.ch';
UPDATE benutzer SET email = 'julia.jurist.be@mailbucket.dvbern.ch' WHERE email = 'julia.jurist@mailbucket.dvbern.ch';
UPDATE benutzer SET email = 'christoph.huetter.be@mailbucket.dvbern.ch' WHERE email = 'christoph.huetter@mailbucket.dvbern.ch';
UPDATE benutzer SET email = 'valentin.burgener.be@mailbucket.dvbern.ch' WHERE email = 'valentin.burgener@mailbucket.dvbern.ch';

