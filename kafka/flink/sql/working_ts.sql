select sensor_id, sensor_location, avg(sensor_reading) as avg1 from `sensor_readings_ts` group by sensor_id, sensor_location;

SELECT
  window_start, sensor_id, sensor_location, count(sensor_reading) AS cnt
FROM TABLE(
  TUMBLE(TABLE sensor_readings_ts, DESCRIPTOR($rowtime), INTERVAL '15' MINUTE))
GROUP BY window_start, sensor_id, sensor_location;

select 
    window_start,
    sensor_id, sensor_location, 
    min(sensor_reading) as minreading, max(sensor_reading) as maxreading
from table(tumble(table sensor_readings_ts, DESCRIPTOR($rowtime), INTERVAL '15' MINUTE))
group by window_start, sensor_id, sensor_location;

select sensor_id, sensor_location, 
      sensor_reading,
      sensor_reading - lag(sensor_reading, 1) OVER (PARTITION BY sensor_id, sensor_location ORDER BY $rowtime) as sr_change
from sensor_readings_ts;

alter table `sensor_readings_ts` MODIFY WATERMARK FOR `SOURCE_WATERMARK`(); -- messagetime - retention.ms
alter table `sensor_readings_ts` MODIFY WATERMARK FOR $rowtime AS $rowtime - INTERVAL '1' MINUTE; -- messagetime - 1min
alter table `sensor_readings_ts` MODIFY WATERMARK FOR ts AS ts - INTERVAL '1' MINUTE; -- column `ts: timestamp(3)` - 1min
alter table `sensor_readings_ts` MODIFY WATERMARK FOR `PROCTIME`(); -- processing time

select ts, sensor_id, sensor_location,
      sensor_reading,
      lag(ts, 1) OVER (PARTITION BY sensor_id, sensor_location ORDER BY ts RANGE between INTERVAL '10' MINUTE PRECEDING and CURRENT ROW) as srt_1,
      lag(sensor_reading, 1) OVER (PARTITION BY sensor_id, sensor_location ORDER BY ts RANGE between INTERVAL '10' MINUTE PRECEDING and CURRENT ROW) as sr_1,
      sensor_reading - lag(sensor_reading, 1) OVER (PARTITION BY sensor_id, sensor_location ORDER BY ts RANGE between INTERVAL '10' MINUTE PRECEDING and CURRENT ROW) as reading_change
from sensor_readings_ts;

select 
    window_start,
    sensor_id, sensor_location, 
    min(sensor_reading) as minreading, max(sensor_reading) as maxreading
from table(tumble(table sensor_readings_ts, DESCRIPTOR(ts), INTERVAL '1' MINUTE))
group by window_start, sensor_id, sensor_location
;

{"sensor_id": 4,"sensor_location": "bedroom","sensor_reading": 116,"ts": 1732113055000}

with reading_change as (
  select ts, sensor_id, sensor_location,
    sensor_reading,
    lag(ts, 1) OVER (PARTITION BY sensor_id, sensor_location ORDER BY ts RANGE between INTERVAL '10' MINUTE PRECEDING and CURRENT ROW) as srt_1,
    lag(sensor_reading, 1) OVER (PARTITION BY sensor_id, sensor_location ORDER BY ts RANGE between INTERVAL '10' MINUTE PRECEDING and CURRENT ROW) as sr_1,
    sensor_reading - lag(sensor_reading, 1) OVER (PARTITION BY sensor_id, sensor_location ORDER BY ts RANGE between INTERVAL '10' MINUTE PRECEDING and CURRENT ROW) as sr_change
  from sensor_readings_ts
)
select ts, sensor_id, sensor_location, sensor_reading, srt_1, sr_1, sr_change
from reading_change
;

with all_data_set as (select * from sensor_readings_ts)
select count(*) from all_data_set where ts <= current_watermark(ts);

select count(*) as t_events,
sum(case when ts <= current_watermark(ts) then 1
else 0
end) as late_events from sensor_readings_ts;
