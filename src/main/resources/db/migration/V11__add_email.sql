create table email (
  id serial,
  contact_id integer,
  index integer,
  type text,
  address text,
  CONSTRAINT "email_primary_key" PRIMARY KEY ("id")
);
alter table email ADD CONSTRAINT fk_contact_id
   FOREIGN KEY (contact_id)
   REFERENCES contact(id);
