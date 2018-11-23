delete
from institution_stammdaten
where id not in('945e3eef-8f43-43d2-a684-4aa61089684b',
				'6b7beb6e-6cf3-49d6-84c0-5818d9215ecd',
				'9a0eb656-b6b7-4613-8f55-4e0e4720455e',
				'199ac4a1-448f-4d4c-b3a6-5aee21f89613',
				'9d8ff34f-8856-4dd3-ade2-2469aadac0ed');
delete
from institution_stammdaten_aud
where id not in('945e3eef-8f43-43d2-a684-4aa61089684b',
				'6b7beb6e-6cf3-49d6-84c0-5818d9215ecd',
				'9a0eb656-b6b7-4613-8f55-4e0e4720455e',
				'199ac4a1-448f-4d4c-b3a6-5aee21f89613',
				'9d8ff34f-8856-4dd3-ade2-2469aadac0ed');

delete
from institution
where id not in (
				select institution_id from institution_stammdaten);
delete
from institution_aud
where id in (
			select institution_id from institution_stammdaten);

# all institutions must now be activated and some fields must be filled out in the stammdaten
update institution set status = 'AKTIV';
update institution_stammdaten set mail='mail@example.com';

update adresse a
inner join institution_stammdaten isd on a.id = isd.adresse_id
inner join institution i on isd.institution_id = i.id
set organisation = i.name;


