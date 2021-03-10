CREATE TABLE gap
(
	symbol     VARCHAR(32)     NOT NULL,
	open_time  BIGINT          NOT NULL,
	close_time BIGINT          NOT NULL,
	
	CONSTRAINT gap_symbol_time_uq UNIQUE (symbol, open_time, close_time)
);