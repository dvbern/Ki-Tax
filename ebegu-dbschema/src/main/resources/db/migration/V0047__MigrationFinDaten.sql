UPDATE einkommensverschlechterung SET bruttovermoegen = 0.00 where bruttovermoegen is null;
UPDATE einkommensverschlechterung SET erhaltene_alimente = 0.00 where erhaltene_alimente is null;
UPDATE einkommensverschlechterung SET ersatzeinkommen = 0.00 where ersatzeinkommen is null;
UPDATE einkommensverschlechterung SET familienzulage = 0.00 where familienzulage is null;
UPDATE einkommensverschlechterung SET geleistete_alimente = 0.00 where geleistete_alimente is null;
UPDATE einkommensverschlechterung SET nettolohn = 0.00 where nettolohn is null;
UPDATE einkommensverschlechterung SET schulden = 0.00 where schulden is null;

UPDATE finanzielle_situation fs SET bruttovermoegen = 0.00 where bruttovermoegen is null and (select famsit
.sozialhilfe_bezueger from familiensituation famsit, familiensituation_container famsitcontainer, gesuch gs, gesuchsteller_container gsc, finanzielle_situation_container fsc
where famsit.id = famsitcontainer.familiensituationja_id and famsitcontainer.id = gs.familiensituation_container_id and (gs.gesuchsteller1_id = gsc.id or gs.gesuchsteller2_id = gsc.id)
and gsc.id = fsc.gesuchsteller_container_id and fsc.finanzielle_situationja_id = fs.id) = false;

UPDATE finanzielle_situation fs SET erhaltene_alimente = 0.00 where erhaltene_alimente is null and (select famsit
.sozialhilfe_bezueger from familiensituation famsit, familiensituation_container famsitcontainer, gesuch gs, gesuchsteller_container gsc, finanzielle_situation_container fsc
where famsit.id = famsitcontainer.familiensituationja_id and famsitcontainer.id = gs.familiensituation_container_id and (gs.gesuchsteller1_id = gsc.id or gs.gesuchsteller2_id = gsc.id)
and gsc.id = fsc.gesuchsteller_container_id and fsc.finanzielle_situationja_id = fs.id) = false;

UPDATE finanzielle_situation fs SET ersatzeinkommen = 0.00 where ersatzeinkommen is null and (select famsit
.sozialhilfe_bezueger from familiensituation famsit, familiensituation_container famsitcontainer, gesuch gs, gesuchsteller_container gsc, finanzielle_situation_container fsc
where famsit.id = famsitcontainer.familiensituationja_id and famsitcontainer.id = gs.familiensituation_container_id and (gs.gesuchsteller1_id = gsc.id or gs.gesuchsteller2_id = gsc.id)
and gsc.id = fsc.gesuchsteller_container_id and fsc.finanzielle_situationja_id = fs.id) = false;

UPDATE finanzielle_situation fs SET familienzulage = 0.00 where familienzulage is null and (select famsit
.sozialhilfe_bezueger from familiensituation famsit, familiensituation_container famsitcontainer, gesuch gs, gesuchsteller_container gsc, finanzielle_situation_container fsc
where famsit.id = famsitcontainer.familiensituationja_id and famsitcontainer.id = gs.familiensituation_container_id and (gs.gesuchsteller1_id = gsc.id or gs.gesuchsteller2_id = gsc.id)
and gsc.id = fsc.gesuchsteller_container_id and fsc.finanzielle_situationja_id = fs.id) = false;

UPDATE finanzielle_situation fs SET geleistete_alimente = 0.00 where geleistete_alimente is null and (select famsit
.sozialhilfe_bezueger from familiensituation famsit, familiensituation_container famsitcontainer, gesuch gs, gesuchsteller_container gsc, finanzielle_situation_container fsc
where famsit.id = famsitcontainer.familiensituationja_id and famsitcontainer.id = gs.familiensituation_container_id and (gs.gesuchsteller1_id = gsc.id or gs.gesuchsteller2_id = gsc.id)
and gsc.id = fsc.gesuchsteller_container_id and fsc.finanzielle_situationja_id = fs.id) = false;

UPDATE finanzielle_situation fs SET nettolohn = 0.00 where nettolohn is null and (select famsit
.sozialhilfe_bezueger from familiensituation famsit, familiensituation_container famsitcontainer, gesuch gs, gesuchsteller_container gsc, finanzielle_situation_container fsc
where famsit.id = famsitcontainer.familiensituationja_id and famsitcontainer.id = gs.familiensituation_container_id and (gs.gesuchsteller1_id = gsc.id or gs.gesuchsteller2_id = gsc.id)
and gsc.id = fsc.gesuchsteller_container_id and fsc.finanzielle_situationja_id = fs.id) = false;

UPDATE finanzielle_situation fs SET schulden = 0.00 where schulden is null and (select famsit
.sozialhilfe_bezueger from familiensituation famsit, familiensituation_container famsitcontainer, gesuch gs, gesuchsteller_container gsc, finanzielle_situation_container fsc
where famsit.id = famsitcontainer.familiensituationja_id and famsitcontainer.id = gs.familiensituation_container_id and (gs.gesuchsteller1_id = gsc.id or gs.gesuchsteller2_id = gsc.id)
and gsc.id = fsc.gesuchsteller_container_id and fsc.finanzielle_situationja_id = fs.id) = false;