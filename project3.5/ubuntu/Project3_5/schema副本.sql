-- Write down the SQL statements you wrote to createing optimized tables
-- and to populate those tables in this file.
-- Remember to add comments explaining why you did so.
CREATE EXTERNAL TABLE part_opt_ori (
	p_partkey INT, 
	p_name STRING, 
	p_mfgr STRING, 
	p_category STRING, 
	p_brand1 STRING, 
	p_color STRING, 
	p_type STRING, 
	p_size INT, 
	p_container STRING)
ROW FORMAT DELIMITED FIELDS TERMINATED BY '|'
LOCATION '/data/part/';
CREATE EXTERNAL TABLE part_opt(
	p_partkey INT, 
	p_name STRING, 
	p_mfgr STRING, 
	p_category STRING, 
	p_brand1 STRING, 
	p_color STRING, 
	p_type STRING, 
	p_size INT, 
	p_container STRING)
STORED AS PARQUET;
insert into part_opt select * from part_opt_ori;

CREATE EXTERNAL TABLE supplier_opt1 (
	s_suppkey   INT,
	s_name STRING,
	s_address STRING,
	s_city STRING,
	s_nation STRING,
	s_region STRING,
	s_phone STRING)
ROW FORMAT DELIMITED FIELDS TERMINATED BY '|'
LOCATION '/data/supplier/';
CREATE EXTERNAL TABLE supplier_opt (
	s_suppkey   INT,
	s_name STRING,
	s_address STRING,
	s_city STRING,
	s_nation STRING,
	s_region STRING,
	s_phone STRING)
STORED AS PARQUET;
insert into  supplier_opt  select * from supplier_opt1;


CREATE EXTERNAL TABLE customer_opt1(
	c_custkey INT,
	c_name STRING,
	c_address  STRING,
	c_city STRING,
	c_nation STRING,
	c_region STRING,
	c_phone STRING,
	c_mktsegment STRING)
ROW FORMAT DELIMITED FIELDS TERMINATED BY '|'
LOCATION '/data/customer/';
CREATE EXTERNAL TABLE customer_opt(
	c_custkey INT,
	c_name STRING,
	c_address  STRING,
	c_city STRING,
	c_nation STRING,
	c_region STRING,
	c_phone STRING,
	c_mktsegment STRING)
STORED AS PARQUET;
insert into  customer_opt select * from customer_opt1;


CREATE EXTERNAL TABLE dwdate_opt1(
	d_datekey INT,
	d_date STRING,
	d_dayofweek STRING,
	d_month STRING,
	d_year INT,
	d_yearmonthnum INT,
	d_yearmonth STRING,
	d_daynuminweek INT,
	d_daynuminmonth INT,
	d_daynuminyear INT,
	d_monthnuminyear INT,
	d_weeknuminyear INT,
	d_sellingseason STRING,
	d_lastdayinweekfl STRING,
	d_lastdayinmonthfl STRING,
	d_holidayfl STRING,
	d_weekdayfl STRING)
ROW FORMAT DELIMITED FIELDS TERMINATED BY '|'
LOCATION '/data/dwdate/';
CREATE EXTERNAL TABLE dwdate_opt(
	d_datekey INT,
	d_date STRING,
	d_dayofweek STRING,
	d_month STRING,
	d_year INT,
	d_yearmonthnum INT,
	d_yearmonth STRING,
	d_daynuminweek INT,
	d_daynuminmonth INT,
	d_daynuminyear INT,
	d_monthnuminyear INT,
	d_weeknuminyear INT,
	d_sellingseason STRING,
	d_lastdayinweekfl STRING,
	d_lastdayinmonthfl STRING,
	d_holidayfl STRING,
	d_weekdayfl STRING)
STORED AS PARQUET;
insert into   dwdate_opt select * from  dwdate_opt1;

CREATE EXTERNAL TABLE lineorder_opt1(
	lo_orderkey INT,
	lo_linenumber INT,
	lo_custkey INT,
	lo_partkey INT,
	lo_suppkey INT,
	lo_orderdate INT,
	lo_orderpriority STRING,
	lo_shippriority STRING,
	lo_quantity INT,
	lo_extendedprice INT,
	lo_ordertotalprice INT,
	lo_discount INT,
	lo_revenue INT,
	lo_supplycost INT,
	lo_tax INT,
	lo_commitdate INT,
	lo_shipmode STRING)
ROW FORMAT DELIMITED FIELDS TERMINATED BY '|'
LOCATION '/data/lineorder/';
CREATE EXTERNAL TABLE lineorder_opt(
	lo_orderkey INT,
	lo_linenumber INT,
	lo_custkey INT,
	lo_partkey INT,
	lo_suppkey INT,
	lo_orderdate INT,
	lo_orderpriority STRING,
	lo_shippriority STRING,
	lo_quantity INT,
	lo_extendedprice INT,
	lo_ordertotalprice INT,
	lo_discount INT,
	lo_revenue INT,
	lo_supplycost INT,
	lo_tax INT,
	lo_commitdate INT,
	lo_shipmode STRING)
STORED AS PARQUET;
insert into lineorder_opt select * from  lineorder_opt1;
