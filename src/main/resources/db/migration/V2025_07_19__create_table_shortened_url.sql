CREATE TABLE public.shortened_url (
    id BIGINT CONSTRAINT shortened_url__pk__id PRIMARY KEY,
    url VARCHAR(256) CONSTRAINT shortened_url__not_null__url NOT NULL,
    shortened VARCHAR(8) CONSTRAINT shortened_url__not_null__shortened NOT NULL,
    CONSTRAINT shortened_url__unique__url UNIQUE (url),
    CONSTRAINT shortened_url__unique__shortened UNIQUE (shortened)
);

CREATE SEQUENCE shortened_url__seq__id
    INCREMENT BY 1
    OWNED BY public.shortened_url.id;

ALTER TABLE public.shortened_url
    ALTER COLUMN id SET DEFAULT nextval('shortened_url__seq__id')