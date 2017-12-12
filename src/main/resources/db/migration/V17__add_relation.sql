create table relation (
  "left" integer,
  "right" integer,
  role text
);

alter table relation ADD CONSTRAINT fk_left_contact_id
   FOREIGN KEY ("left")
   REFERENCES contact(id);

alter table relation ADD CONSTRAINT fk_right_contact_id
   FOREIGN KEY ("right")
   REFERENCES contact(id);
