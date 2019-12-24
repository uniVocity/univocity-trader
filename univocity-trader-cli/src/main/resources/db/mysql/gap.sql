# Used to store known gaps in history that can't be retrieved from the exchange.
# Delete rows from table if you want to try filling the gaps again.

CREATE TABLE gap
(
	symbol     VARCHAR(32)     NOT NULL,
	open_time  BIGINT          NOT NULL,
	close_time BIGINT          NOT NULL,
	
	CONSTRAINT gap_symbol_time_uq UNIQUE (symbol, open_time, close_time)
);