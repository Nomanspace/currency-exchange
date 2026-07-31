CREATE TABLE ExchangeRates ( 
	id SERIAL PRIMARY KEY,
	BaseCurrencyId INT,
	TargetCurrencyId INT,
	ExchangeRate DECIMAL(6),
	UNIQUE (BaseCurrencyId, TargetCurrencyId),
	FOREIGN KEY (BaseCurrencyId) REFERENCES currencies(id),
	FOREIGN KEY (TargetCurrencyId) REFERENCES currencies(id)
);