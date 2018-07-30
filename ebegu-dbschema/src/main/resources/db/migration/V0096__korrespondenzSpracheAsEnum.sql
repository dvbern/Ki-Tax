DROP TABLE gesuchsteller_korrespondenz_sprachen;
DROP TABLE gesuchsteller_korrespondenz_sprachen_aud;

ALTER TABLE gesuchsteller
  ADD  korrespondenz_sprache varchar(255);

ALTER TABLE gesuchsteller_aud
  ADD  korrespondenz_sprache varchar(255);

