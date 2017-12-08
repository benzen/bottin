create table telephone (
  id serial,
  type text,
  number text,
  CONSTRAINT "telephone_primary_key" PRIMARY KEY ("id")
);

create table contact_telephones (
  contact_id bigint references contact(id),
  telephone_id bigint references telephone(id),
  "order" int
);
