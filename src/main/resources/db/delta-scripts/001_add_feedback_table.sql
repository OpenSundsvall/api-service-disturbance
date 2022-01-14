create table feedback (
   id bigint not null auto_increment,
    created datetime(6),
    recipient_id varchar(255) not null,
    primary key (id)
) engine=InnoDB;

create index recipient_id_index on feedback (recipient_id);

-- Necessary line in order to document the change. 
INSERT INTO disturbance.schema_history (schema_version,comment,applied) VALUES ('001','Added feedback table', NOW());
