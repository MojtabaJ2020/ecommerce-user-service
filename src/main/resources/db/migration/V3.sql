CREATE SEQUENCE IF NOT EXISTS public.verification_token_seq
    INCREMENT 50
    START 1
    MINVALUE 1
    MAXVALUE 9223372036854775807
    CACHE 1;

ALTER SEQUENCE public.verification_token_seq
    OWNER TO postgres;


CREATE TABLE IF NOT EXISTS public.verification_token
(
    id bigint NOT NULL DEFAULT nextval('verification_token_seq'::regclass),
    token text COLLATE pg_catalog."default" NOT NULL,
    expires_at timestamp without time zone NOT NULL,
    user_id bigint,
    CONSTRAINT verification_token_pkey PRIMARY KEY (id),
    CONSTRAINT users_to_verification_token_fkey FOREIGN KEY (user_id)
        REFERENCES public.users (id) MATCH SIMPLE
        ON UPDATE CASCADE
        ON DELETE CASCADE
        NOT VALID
)

TABLESPACE pg_default;

ALTER TABLE IF EXISTS public.verification_token
    OWNER to postgres;


CREATE INDEX IF NOT EXISTS "fki_users_to_verification_token_fkey"
    ON public.verification_token USING btree
    (user_id ASC NULLS LAST)
    TABLESPACE pg_default;