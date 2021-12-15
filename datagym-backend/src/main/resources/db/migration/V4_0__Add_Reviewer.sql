create table reviewer (id varchar(255) not null, timestamp bigint not null, user_id varchar(255) not null, project_id varchar(255) not null, primary key (id));
alter table reviewer add constraint fk_reviewer_project foreign key (project_id) references project (id);
