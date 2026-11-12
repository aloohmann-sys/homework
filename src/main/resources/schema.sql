drop table if exists notification_dispatches;

create table notification_dispatches (
    id bigint primary key,
    notification_type varchar(32) not null,
    recipient varchar(255) not null,
    payload clob,
    status varchar(1) not null,
    failure_count integer not null,
    dispatch_count integer not null,
    is_blocked varchar(1) not null,
    message varchar(255),
    created_at timestamp not null,
    updated_at timestamp not null
);

comment on table notification_dispatches is 'Notification dispatch attempts and outcomes';
comment on column notification_dispatches.id is 'Primary key for the notification dispatch';
comment on column notification_dispatches.notification_type is 'Notification channel type';
comment on column notification_dispatches.recipient is 'Target destination for the notification';
comment on column notification_dispatches.payload is 'Notification payload content';
comment on column notification_dispatches.status is 'Current notification state (N=NEW, S=SENT, E=ERR, U=UNKNOWN, R=REJECTED)';
comment on column notification_dispatches.failure_count is 'Consecutive dispatch failure counter';
comment on column notification_dispatches.dispatch_count is 'Total dispatch attempt counter';
comment on column notification_dispatches.is_blocked is 'Whether the recipient is blocked (Y/N)';
comment on column notification_dispatches.message is 'Last message or error detail';
comment on column notification_dispatches.created_at is 'Dispatch creation timestamp';
comment on column notification_dispatches.updated_at is 'Last update timestamp';

create index ix_notification_dispatches_recipient on notification_dispatches (recipient);
create index ix_notification_dispatches_status on notification_dispatches (status);
