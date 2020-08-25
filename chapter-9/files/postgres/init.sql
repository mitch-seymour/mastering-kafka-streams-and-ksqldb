
CREATE TABLE titles (
    id           integer,
    title        varchar(120),
    CONSTRAINT   id PRIMARY KEY(id)
);

INSERT INTO titles (id, title) values (1, 'Stranger Things');
INSERT INTO titles (id, title) values (2, 'Black Mirror');
INSERT INTO titles (id, title) values (3, 'The Office');
