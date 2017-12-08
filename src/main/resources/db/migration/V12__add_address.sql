create table address (
  id serial,
  unit text,
  street text,
  locality text,
  regionCode text,
  pobox text,
  countryCode text,
  deliveryInfo text,
  index int,
  contact_id int,
  CONSTRAINT "address_primary_key" PRIMARY KEY ("id")
);

alter table address ADD CONSTRAINT fk_contact_id
   FOREIGN KEY (contact_id)
   REFERENCES contact(id);
