ALTER TABLE familiensituation DROP behinderungszuschlag_fuer_mind_ein_kind_einmal_beantragt;
ALTER TABLE familiensituation_aud DROP behinderungszuschlag_fuer_mind_ein_kind_einmal_beantragt;

ALTER TABLE familiensituation CHANGE antrag_nur_fuer_behinderungszuschlag verguenstigung_gewuenscht BIT(1) NULL DEFAULT NULL;
ALTER TABLE familiensituation_aud CHANGE antrag_nur_fuer_behinderungszuschlag verguenstigung_gewuenscht BIT(1) NULL DEFAULT NULL;

UPDATE familiensituation SET verguenstigung_gewuenscht = NOT verguenstigung_gewuenscht WHERE
      verguenstigung_gewuenscht IS NOT NULL;
