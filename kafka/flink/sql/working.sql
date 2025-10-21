select sensor_id, sensor_location, avg(sensor_reading) as avg1 from `sensor_readings` group by sensor_id, sensor_location;

SELECT
  window_start, sensor_id, sensor_location, count(sensor_reading) AS cnt
FROM TABLE(
  TUMBLE(TABLE sensor_readings, DESCRIPTOR($rowtime), INTERVAL '15' MINUTE))
GROUP BY window_start, sensor_id, sensor_location;

select 
    window_start,
    sensor_id, sensor_location, 
    min(sensor_reading) as minreading, max(sensor_reading) as maxreading
from table(tumble(table sensor_readings, DESCRIPTOR($rowtime), INTERVAL '15' MINUTE))
group by window_start, sensor_id, sensor_location;

-- not yet
select sensor_id, sensor_location, 
      sensor_reading,
      sensor_reading - lag(sensor_reading, 1) OVER (PARTITION BY sensor_id, sensor_location ORDER BY $rowtime) as reading_change
from sensor_readings;

alter table `sensor_readings` MODIFY WATERMARK FOR $rowtime AS $rowtime - INTERVAL '1' MINUTE;

select $rowtime, sensor_id, sensor_location,
      sensor_reading,
      lag($rowtime, 1) OVER (PARTITION BY sensor_id, sensor_location ORDER BY $rowtime) as srt_1,
      lag(sensor_reading, 1) OVER (PARTITION BY sensor_id, sensor_location ORDER BY $rowtime) as sr_1,
      sensor_reading - lag(sensor_reading, 1) OVER (PARTITION BY sensor_id, sensor_location ORDER BY $rowtime) as reading_change
from sensor_readings;

with reading_change as (
  select $rowtime, sensor_id, sensor_location,
    sensor_reading,
    lag($rowtime, 1) OVER (PARTITION BY sensor_id, sensor_location ORDER BY $rowtime) as srt_1,
    lag(sensor_reading, 1) OVER (PARTITION BY sensor_id, sensor_location ORDER BY $rowtime) as sr_1,
    sensor_reading - lag(sensor_reading, 1) OVER (PARTITION BY sensor_id, sensor_location ORDER BY $rowtime) as sr_change
  from sensor_readings
)
select $rowtime, 
    sensor_id, sensor_location, sensor_reading, srt_1, sr_1, sr_change
from reading_change where sr_change > 1
;

-- not working
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

