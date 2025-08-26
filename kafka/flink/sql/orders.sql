-- shipped orders
select o.`$rowtime`, s.`$rowtime`, o.order_id, o.product_id, s.shipment_id, s.status from orders o join shipments s on o.order_id = s.order_id where status = 'shipped';

-- more than 10 orders from the same customer
select customer_id, count(*) as cnt from orders group by customer_id having count(*) > 10

-- windowed orders
select * from 
table(
    tumble(
        table orders, DESCRIPTOR($rowtime), interval '10' MINUTES
        )
    )
;

-- customers sending more than 2 orders in the same hour
with order_by_hours as (
select order_id, product_id, customer_id, $rowtime as ts, window_start from 
table(
    tumble(
        table orders, DESCRIPTOR($rowtime), interval '10' MINUTES
        )
    )
)
select customer_id, count(*) from order_by_hours group by customer_id having count(*) > 2;
