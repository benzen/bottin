drop table contact_telephones;
alter table telephone add column index int;
alter table telephone add column contact_id int;
alter table telephone ADD CONSTRAINT fk_contact_id
   FOREIGN KEY (contact_id)
   REFERENCES contact(id);
