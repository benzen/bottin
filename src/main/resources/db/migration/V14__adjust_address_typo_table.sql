alter table address drop column regionCode;
alter table address drop column countryCode;
alter table address drop column deliveryInfo;

alter table address add column region_code text;
alter table address add column country_code text;
alter table address add column delivery_info text;
