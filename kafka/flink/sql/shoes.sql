/*
****************************************************
    Purpose: FlinkDemo_JnJ
    Author: Venky Narayanan <vnarayanan@confluent.io>
    Date: Nov 15, 2024
****************************************************
    Pre-reqs: Using datagen create the following avro data streams 
        1. `shoes`
        2. `shoe_orders`
        3. `shoe_customers`
        4. `shoe_clickstreams`
****************************************************
*/

// Not supported in Confluent Cloud UI
// SET 'client.statement-name' = 'FlinkDemo_JnJ';
  
select * from shoes limit 10;

select * from shoe_orders limit 10;

select * from shoe_orders where product_id='2ae26388-fc97-4170-a3a4-c8b02b31a803' limit 1;

create table shoe_orders_ts (
  order_id int, product_id string, customer_id string, ts timestamp(3), 
  watermark for ts as ts - INTERVAL '4' SECOND,
  primary key(order_id) NOT ENFORCED
  ) with ('value.fields-include' = 'all')
  AS SELECT order_id, product_id, customer_id, ts FROM shoe_orders;

select * from `shoe_orders_ts` join `shoes` on `id`=`product_id`;

create table shoe_orders_ts (
  order_id int, product_id string, customer_id string, sale_price int, ts timestamp(3), 
  watermark for ts as ts - INTERVAL '4' SECOND,
  primary key(order_id) NOT ENFORCED
  ) with ('value.fields-include' = 'all');

insert into shoe_orders_ts
SELECT order_id, product_id, customer_id, sale_price, ts FROM shoe_orders so join shoes s on id=product_id;

with shoe_orders_shoes as (
    select order_id, product_id, customer_id, cast(null as int) as sale_price, cast(ts as timestamp(3)) as ts from shoe_orders
    union all
    select cast(null as string) as order_id, id as product_id, cast(null as string) as customer_id, sale_price, cast($rowtime as timestamp(3)) as ts from shoes
)
select
  last_value(order_id) OVER w AS order_id,
  last_value(product_id) OVER w AS product_id,
  last_value(customer_id) OVER w AS customer_id,
  last_value(sale_price) OVER w AS sale_price,
  max(ts) over w as rowtime      -- Track the latest event timestamp
from shoe_orders_shoes
-- Define window for tracking latest values per customer
window w as (
  partition by product_id             -- Group all events by customer
  order by ts                   -- Order by event timestamp
  rows between unbounded preceding     -- Consider all previous events
    AND CURRENT ROW                    -- up to the current one
)

create table `lowvalue_shoe_orders` (order_id int not null, customer_id string not null, product_id string not null, `sale_price` int not null, ts timestamp(3), 
  watermark for ts as ts - INTERVAL '4' SECOND,
  primary key(order_id) NOT ENFORCED) distributed into 1 buckets;

insert into lowvalue_shoe_orders (order_id, customer_id, product_id, sale_price, ts) select order_id, customer_id, product_id, sale_price, ts from `shoe_orders_ts` join `shoes` on `id`=`product_id` where sale_price < 5000;

describe extended `shoe_clickstreams`;

select product_id, user_id, view_time, ts, $rowtime, current_watermark($rowtime) as wm from shoe_clickstreams order by $rowtime;

-- This query combines order and click data, tracking the latest values
-- for each customer's interactions across both datasets

-- First, combine order data and clickstream data into a single structure
-- Note: Fields not present in one source are filled with NULL
WITH combined_data AS (
  -- Orders data with empty clickstreams fields
SELECT
    customer_id,
    order_id,
    so.product_id,
    sale_price,
    CAST(NULL AS STRING) AS page_url,        -- Click-specific fields set to NULL
    CAST(NULL AS INT) AS view_time,
    $rowtime
  FROM shoe_orders_ts so join shoes s on so.product_id = s.id
  UNION ALL
  -- Click data with empty order-related fields
  SELECT
    user_id AS customer_id,             -- Normalize user_id to match customer_id
    CAST(NULL AS INT) AS order_id,   -- Order-specific fields set to NULL
    CAST(NULL AS STRING) AS product_id, -- for click records
    CAST(NULL AS DOUBLE) AS sale_price,
    page_url,
    view_time,
    $rowtime
  FROM shoe_clickstreams
)
-- For each customer, maintain the latest value for each field
-- using window functions over the combined dataset
SELECT
  LAST_VALUE(customer_id) OVER w AS customer_id,
  LAST_VALUE(order_id) OVER w AS order_id,
  LAST_VALUE(product_id) OVER w AS product_id,
  LAST_VALUE(price) OVER w AS price,
  LAST_VALUE(url) OVER w AS url,
  LAST_VALUE(user_agent) OVER w AS user_agent,
  LAST_VALUE(view_time) OVER w AS view_time,
  MAX($rowtime) OVER w AS rowtime      -- Track the latest event timestamp
FROM combined_data
-- Define window for tracking latest values per customer
WINDOW w AS (
  PARTITION BY customer_id             -- Group all events by customer
  ORDER BY $rowtime                    -- Order by event timestamp
  ROWS BETWEEN UNBOUNDED PRECEDING     -- Consider all previous events
    AND CURRENT ROW                    -- up to the current one
)