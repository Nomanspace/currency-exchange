CREATE TABLE Currencies
(
    id       SERIAL PRIMARY KEY,
    code     VARCHAR(10) NOT NULL ,
    fullname VARCHAR(255) NOT NULL ,
    sign     VARCHAR(10) NOT NULL ,
    UNIQUE(code)
);