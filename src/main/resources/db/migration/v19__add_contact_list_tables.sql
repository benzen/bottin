create table list (
  id serial,
  name text,
  CONSTRAINT "list_primary_key" PRIMARY KEY ("id")
);

create table list_members (
  list_id bigint references list(id),
  contact_id bigint references contact(id),
  index int
);
