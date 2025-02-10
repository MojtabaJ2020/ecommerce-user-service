CREATE SEQUENCE IF NOT EXISTS public.refresh_token_seq
    INCREMENT 50
    START 1
    MINVALUE 1
    MAXVALUE 9223372036854775807
    CACHE 1;

ALTER SEQUENCE public.refresh_token_seq
    OWNER TO postgres;


-- Table: public.refresh_token

-- DROP TABLE IF EXISTS public.refresh_token;

CREATE TABLE IF NOT EXISTS public.refresh_token
(
    id bigint NOT NULL DEFAULT nextval('refresh_token_seq'::regclass),
    provider character varying(64) COLLATE pg_catalog."default" NOT NULL,
    token text COLLATE pg_catalog."default" NOT NULL,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    expires_at timestamp without time zone NOT NULL,
    revoked boolean DEFAULT false,
    last_used_at timestamp without time zone,
    ip_address character varying(64) COLLATE pg_catalog."default",
    user_id bigint,
    CONSTRAINT refresh_token_pkey PRIMARY KEY (id),
    CONSTRAINT refresh_token_unique_token UNIQUE (token),
    CONSTRAINT users_to_refresh_token_fkey FOREIGN KEY (user_id)
        REFERENCES public.users (id) MATCH SIMPLE
        ON UPDATE CASCADE
        ON DELETE CASCADE
        NOT VALID
)

TABLESPACE pg_default;

ALTER TABLE IF EXISTS public.refresh_token
    OWNER to postgres;
-- Index: fki_FK_users_to_refresh_token

-- DROP INDEX IF EXISTS public."fki_FK_users_to_refresh_token";

CREATE INDEX IF NOT EXISTS "fki_users_to_refresh_token_fkey"
    ON public.refresh_token USING btree
    (user_id ASC NULLS LAST)
    TABLESPACE pg_default;