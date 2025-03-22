-- id sensors that had a reading changes less than 10
with reading_change as (
    select $rowtime, sensor_location, sensor_id, 
        sensor_reading,
        sensor_reading - lag(sensor_reading, 1) OVER (PARTITION BY sensor_location, sensor_id ORDER BY $rowtime) as reading_change
    from sensor_readings
)
select $rowtime, 
    sensor_location, sensor_id, 
    reading_change
from reading_change where reading_change < 10;

-- find min and max by sensor location and id with window
select 
    window_start, window_end, 
    sensor_location, sensor_id, 
    min(sensor_reading) as minreading, max(sensor_reading) as maxreading
from table(tumble(table sensor_readings, DESCRIPTOR($rowtime), INTERVAL '1' hours))
group by window_start, window_end, sensor_location, sensor_id;

-- change watermark strategy
alter table `sensor_readings` MODIFY WATERMARK FOR $rowtime AS $rowtime - INTERVAL '15' MINUTE;

-- watermark updates
select CURRENT_WATERMARK($rowtime), order_id, product_id, customer_id, $rowtime as ts, window_start from 
table(
    tumble(
        table orders, DESCRIPTOR($rowtime), interval '1' hours
        )
    )

-- low and high  sensor readings
SELECT mr.*
FROM sensor_readings
    MATCH_RECOGNIZE (
        PARTITION BY sensor_id, sensor_location
        ORDER BY $rowtime
        MEASURES
            $rowtime as ts,
            LAST(LOW.sensor_reading) AS low_sensor_reading,
            LAST(HIGH.sensor_reading) AS high_sensor_reading
        ONE ROW PER MATCH
        AFTER MATCH SKIP TO NEXT ROW
        PATTERN (START_ROW LOW+ HIGH)
        DEFINE
            LOW AS
                (LAST(LOW.sensor_reading, 1) IS NULL AND LOW.sensor_reading < START_ROW.sensor_reading) OR
                    sensor_reading < LAST(sensor_reading, 1),
            HIGH AS
                HIGH.sensor_reading > LAST(LOW.sensor_reading, 1)
    ) mr;
