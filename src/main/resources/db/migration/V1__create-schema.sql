create table contact (
  id serial,
  type text,
  name text,
  prenom text,
  notes text,
  CONSTRAINT "contact_primary_key" PRIMARY KEY ("id")
);
