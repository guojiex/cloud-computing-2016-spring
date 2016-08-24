-- Write down the SQL statements you wrote to createing optimized tables
-- and to populate those tables in this file.
-- Remember to add comments explaining why you did so.
DROP TABLE IF EXISTS part_opt ;
CREATE EXTERNAL TABLE part_opt (
    p_partkey INT, 
    p_name STRING, 
    p_mfgr STRING, 
    p_color STRING, 
    p_type STRING, 
    p_size INT, 
    p_container STRING)
PARTITIONED BY(p_category STRING,p_brand1 STRING)
ROW FORMAT DELIMITED FIELDS TERMINATED BY '|'
STORED AS PARQUET;
INSERT INTO part_opt PARTITION(p_category,p_brand1)
SELECT p_partkey, 
p_name , 
p_mfgr , 
p_color , 
p_type , 
p_size , 
p_container,
p_category, 
p_brand1  from part;

DROP TABLE IF EXISTS supplier_opt ;
CREATE EXTERNAL TABLE supplier_opt (
    s_suppkey   INT,
    s_name STRING,
    s_address STRING,
    s_nation STRING,
    s_phone STRING)
PARTITIONED BY(s_city STRING,s_region STRING)
ROW FORMAT DELIMITED FIELDS TERMINATED BY '|'
STORED AS PARQUET;
INSERT INTO supplier_opt PARTITION(s_city,s_region)
SELECT s_suppkey   ,
s_name ,
s_address,
s_nation ,
s_phone,
s_city,s_region FROM supplier;


DROP TABLE IF EXISTS customer_opt ;
CREATE EXTERNAL TABLE customer_opt(
    c_custkey INT,
    c_name STRING,
    c_address  STRING,
    c_nation STRING,
    c_region STRING,
    c_phone STRING,
    c_mktsegment STRING)
PARTITIONED BY(c_city STRING)
ROW FORMAT DELIMITED FIELDS TERMINATED BY '|'
STORED AS PARQUET;
INSERT INTO customer_opt PARTITION(c_city)
SELECT c_custkey ,
c_name ,
c_address  ,
c_nation ,
c_region ,
c_phone ,
c_mktsegment,
c_city FROM customer;



DROP TABLE IF EXISTS dwdate_opt;
CREATE EXTERNAL TABLE dwdate_opt(
    d_datekey INT,
    d_date STRING,
    d_dayofweek STRING,
    d_month STRING,
    d_yearmonthnum INT,
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
PARTITIONED BY(d_year INT,d_yearmonth STRING)
ROW FORMAT DELIMITED FIELDS TERMINATED BY '|'
STORED AS PARQUET;
INSERT INTO dwdate_opt PARTITION(d_year,d_yearmonth)
SELECT d_datekey ,
d_date ,
d_dayofweek ,
d_month ,
d_yearmonthnum ,
d_daynuminweek ,
d_daynuminmonth ,
d_daynuminyear ,
d_monthnuminyear ,
d_weeknuminyear ,
d_sellingseason ,
d_lastdayinweekfl ,
d_lastdayinmonthfl ,
d_holidayfl ,
d_weekdayfl,
d_year,d_yearmonth FROM dwdate;

DROP TABLE  IF EXISTS lineorder_opt;
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
    lo_revenue INT,
    lo_supplycost INT,
    lo_tax INT,
    lo_commitdate INT,
    lo_shipmode STRING)
PARTITIONED BY(lo_discount INT)
ROW FORMAT DELIMITED FIELDS TERMINATED BY '|'
STORED AS PARQUET;
INSERT INTO lineorder_opt PARTITION(lo_discount)
SELECT lo_orderkey ,
lo_linenumber ,
lo_custkey ,
lo_partkey ,
lo_suppkey ,
lo_orderdate ,
lo_orderpriority ,
lo_shippriority ,
lo_quantity ,
lo_extendedprice ,
lo_ordertotalprice ,
lo_revenue ,
lo_supplycost ,
lo_tax ,
lo_commitdate ,
lo_shipmode,
lo_discount FROM lineorder;

