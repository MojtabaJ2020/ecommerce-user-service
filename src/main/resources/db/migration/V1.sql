-- SEQUENCE: public.users_seq

-- DROP SEQUENCE IF EXISTS public.users_seq;

CREATE SEQUENCE IF NOT EXISTS public.users_seq
    INCREMENT 50
    START 2
    MINVALUE 1
    MAXVALUE 9223372036854775807
    CACHE 1;

ALTER SEQUENCE public.users_seq
    OWNER TO postgres;


-- Table: public.users

-- DROP TABLE IF EXISTS public.users;

CREATE TABLE IF NOT EXISTS public.users
(
    id bigint NOT NULL DEFAULT NEXTVAL('public.users_seq'),
    email character varying(255) NOT NULL COLLATE pg_catalog."default",
    name character varying(64) NOT NULL COLLATE pg_catalog."default",
    password character varying(64) NOT NULL COLLATE pg_catalog."default",
    picture oid,
    role character varying(255) COLLATE pg_catalog."default",
    CONSTRAINT users_pkey PRIMARY KEY (id),
    CONSTRAINT users_unique_email UNIQUE (email),
    CONSTRAINT users_unique_name UNIQUE (name)
)

TABLESPACE pg_default;

ALTER TABLE IF EXISTS public.users
    OWNER to postgres;

insert into public.users (id, email, name, password, picture, role) values (1, 'Admin@gmail.com', 'Admin', '$2a$10$eV6VMCFRlarxenMasG/41.3KF7tN6hS9milCqUUSFhYRftC4rd47a',null,'ADMIN');