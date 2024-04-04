/*
 * librdkafka - Apache Kafka C library
 *
 * Copyright (c) 2017-2022, Magnus Edenhill
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */

/**
 * Simple Apache Kafka producer
 * using the Kafka driver from librdkafka
 * (https://github.com/confluentinc/librdkafka)
 */

#include <stdio.h>
#include <signal.h>
#include <string.h>
#include <getopt.h>
#include <sys/time.h>
#include <ctype.h>

#include <unistd.h>
#include <stdlib.h>
#include <syslog.h>
#include <errno.h>

/* Typical include path would be <librdkafka/rdkafka.h>, but this program
 * is builtin from within the librdkafka source tree and thus differs. */
#include <librdkafka/rdkafka.h>

static volatile sig_atomic_t run = 1;

#define DEFAULT_TPS 1
#define DEFAULT_RECLEN 10
#define DEFAULT_RECNUM 1000
#define DEFAULT_DEBUG "conf"
#define DEFAULT_BROKER "localhost:9092"
#define DEFAULT_CLIENTID "rdkafka_producer"

#define DEBUG "DEBUG"
#define INFO "INFO"
#define ERROR "ERROR"
#define WARN "WARN"
#define MAIN "main: main"

#define log_debug(func,buf) log(DEBUG,func,buf)
#define log_error(func,buf) log(ERROR,func,buf)
#define log_info(func,buf) log(INFO,func,buf)
#define log_warn(func,buf) log(WARN,func,buf)

#define CONF_BROKER "bootstrap.servers"
#define CONF_CLIENTID "client.id"
#define CONF_DEBUG "debug"

static char *brokers = DEFAULT_BROKER;
static int opt;
static const char *debug = DEFAULT_DEBUG;
static int do_conf_dump = 0;
static rd_kafka_conf_t *conf;
static char errstr[512];
static rd_kafka_t *rk;    /* Producer instance handle */
static char buf[512];     /* Message value temporary buffer */
static const char *topic; /* Argument: topic to produce to */
static char *client_id = DEFAULT_CLIENTID;
static long recordlen = DEFAULT_RECLEN;
static int recordnum = DEFAULT_RECNUM;
static int tps = DEFAULT_TPS;
static char msg_buf[1024];

static void log(const char* prefix, const char *func, const char *buf)
{
    struct timeval tv;
    gettimeofday(&tv, NULL);
    if (strcmp(prefix, ERROR) || strcmp(prefix, WARN)) {
        fprintf(stderr, "%u.%03u %s: %s: %s\n", (int)tv.tv_sec, (int)(tv.tv_usec / 1000), prefix, (func?func:"main"), buf);
        return;
    }
    fprintf(stdout, "%u.%03u %s: %s: %s\n", (int)tv.tv_sec, (int)(tv.tv_usec / 1000), prefix, (func?func:"main"), buf);
}

/**
 * @brief Signal termination of program
 */
static void stop(int sig)
{
    run = 0;
    fclose(stdin); /* abort fgets() */
}

/**
 * @brief Message delivery report callback.
 *
 * This callback is called exactly once per message, indicating if
 * the message was succesfully delivered
 * (rkmessage->err == RD_KAFKA_RESP_ERR_NO_ERROR) or permanently
 * failed delivery (rkmessage->err != RD_KAFKA_RESP_ERR_NO_ERROR).
 *
 * The callback is triggered from rd_kafka_poll() and executes on
 * the application's thread.
 */
static void dr_msg_cb(rd_kafka_t *rk, const rd_kafka_message_t *rkmessage, void *opaque)
{
    if (rkmessage->err) {
        sprintf(msg_buf, "Message delivery failed: %s", rd_kafka_err2str(rkmessage->err));
        log_error(MAIN, msg_buf);
    }
    else {
        sprintf(msg_buf, "Message delivered (%zd bytes, partition %" PRId32 ", offset %" PRId64 ")",
                rkmessage->len, rkmessage->partition, rkmessage->offset);
        log_info(MAIN, msg_buf);
    }

    /* The rkmessage is destroyed automatically by librdkafka */
}

static void sig_usr1(int sig)
{
    rd_kafka_dump(stdout, rk);
}

static void usage(char *progname)
{
    fprintf(stderr,
            "Usage: %s [options] <topic[:part]> <topic[:part]>..\n"
            "\n"
            "librdkafka version %s (0x%08x)\n"
            "\n"
            " Options:\n"
            "  -s <record-len>   Size of each record (%d bytes)\n"
            "  -n <num records>  Number of records (%d records)\n"
            "  -T <TPS>          Throughput (%d tps)\n"
            "  -t <topic>        Topic\n"
            "  -b <brokers>      Broker address (%s)\n"
            "  -c <client-id>    Client ID (%s)\n"
            "  -d [facs..]       Enable debugging contexts: (%s)\n"
            "                    %s\n"
            "  -X <prop=name>    Set arbitrary librdkafka "
            "configuration property\n"
            "               Use '-X list' to see the full list\n"
            "               of supported properties.\n"
            "\n",
            progname, rd_kafka_version_str(), rd_kafka_version(),
            DEFAULT_RECLEN, DEFAULT_RECNUM, DEFAULT_TPS, DEFAULT_BROKER, DEFAULT_CLIENTID, DEFAULT_DEBUG, rd_kafka_get_debug_contexts());
    exit(1);
}

static long current_timestamp() {
    struct timeval te; 
    gettimeofday(&te, NULL); // get current time
    long milliseconds = te.tv_sec*1000LL + te.tv_usec/1000; // calculate milliseconds
    return milliseconds;
}

static void confdump()
{
    const char **arr;
    size_t cnt;
    int pass;

    for (pass = 0; pass < 2; pass++)
    {
        if (pass == 0)
        {
            arr = rd_kafka_conf_dump(conf, &cnt);
            log_info(MAIN, "# Global config");
        }
        else
        {
            rd_kafka_topic_conf_t *topic_conf =
                rd_kafka_conf_get_default_topic_conf(conf);
            if (topic_conf)
            {
                log_info(MAIN, "# Topic config");
                arr = rd_kafka_topic_conf_dump(
                    topic_conf, &cnt);
            }
            else
            {
                arr = NULL;
            }
        }

        if (!arr)
            continue;

        int i;
        for (i = 0; i < (int)cnt; i += 2) {
            sprintf(msg_buf, "%s = %s", arr[i], arr[i + 1]);
            log_info(MAIN, msg_buf);
        }

        rd_kafka_conf_dump_free(arr, cnt);
    }

    exit(0);
}

/**
 * Kafka logger callback (optional)
 */
static void logger(const rd_kafka_t *rk, int level, const char *fac, const char *buf)
{
    char prefix[256];
    sprintf(prefix, "RDKAFKA-%i-%s", level, fac);

    log(prefix, rd_kafka_name(rk), buf);
}

static void err_cb(rd_kafka_t *rk, int err, const char *reason, void *opaque)
{
    if (err == RD_KAFKA_RESP_ERR__FATAL)
    {
        char errstr[512];
        err = rd_kafka_fatal_error(rk, errstr, sizeof(errstr));
        sprintf(msg_buf, "FATAL ERROR CALLBACK: %s: %s", rd_kafka_err2str(err), errstr);
        log_error(rd_kafka_name(rk), msg_buf);
    }
    else
    {
        sprintf(msg_buf, "ERROR CALLBACK: %s: %s", rd_kafka_err2str(err), reason);
        log_error(rd_kafka_name(rk), msg_buf);
    }
}

static void parseArgs(int argc, char **argv)
{
    while ((opt = getopt(argc, argv, "n:T:s:t:c:b:d:X:")) != -1)
    {
        switch (opt)
        {
        case 's':
            recordlen = atol(optarg);
            if (recordlen < 1 || recordlen > 1000000) {
                log_error(MAIN, "Record length should be between 1 and 1000000");
                usage(argv[0]);
            }
            break;
        case 'n':
            recordnum = atoi(optarg);
            if (recordnum < 1 || recordnum > 1000000) {
                log_error(MAIN, "Record numbers should be between 1 and 1000000");
                usage(argv[0]);
            }
            break;
        case 'T':
            tps = atoi(optarg);
            if (tps < 1 || tps > 1000) {
                log_error(MAIN, "TPS should be between 1 and 1000");
                usage(argv[0]);
            }
            break;
        case 't':
            topic = optarg;
            break;
        case 'b':
            brokers = optarg;
            break;
        case 'c':
            client_id = optarg;
            break;
        case 'd':
            debug = optarg;
            break;
        case 'X':
        {
            char *name, *val;
            rd_kafka_conf_res_t res;

            if (!strcmp(optarg, "list") || !strcmp(optarg, "help"))
            {
                rd_kafka_conf_properties_show(stdout);
                exit(0);
            }

            if (!strcmp(optarg, "dump"))
            {
                do_conf_dump = 1;
                continue;
            }

            name = optarg;
            if (!(val = strchr(name, '=')))
            {
                sprintf(msg_buf, "Expected  -X property=value, not %s", name);
                log_error(MAIN, msg_buf);
                exit(1);
            }

            *val = '\0';
            val++;

            res = rd_kafka_conf_set(conf, name, val, errstr, sizeof(errstr));

            if (res != RD_KAFKA_CONF_OK)
            {
                log_error(MAIN, errstr);
                exit(1);
            }
        }
        break;

        default:
            usage(argv[0]);
        }
    }
}

static char *rand_string(char *str, size_t size)
{
    const char charset[] = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
    if (size) {
        --size;
        for (size_t n = 0; n < size; n++) {
            int key = rand() % (int) (sizeof charset - 1);
            str[n] = charset[key];
        }
        str[size] = '\0';
    }
    return str;
}

int main(int argc, char **argv)
{

    signal(SIGINT, stop);
    signal(SIGUSR1, sig_usr1);

    /* Kafka configuration */
    conf = rd_kafka_conf_new();

    /* Set logger */
    rd_kafka_conf_set_log_cb(conf, logger);
    rd_kafka_conf_set_error_cb(conf, err_cb);

    parseArgs(argc, argv);

    sprintf(msg_buf, "debug = %s", debug);
    log_info(MAIN, msg_buf);

    if (debug && rd_kafka_conf_set(conf, CONF_DEBUG, debug, errstr,
                                   sizeof(errstr)) != RD_KAFKA_CONF_OK)
    {
        sprintf(msg_buf, "Debug configuration failed: %s: %s", errstr, debug);
        log_error(MAIN, msg_buf);
        exit(1);
    }

    if (do_conf_dump)
        confdump();

    /* Set bootstrap broker(s) as a comma-separated list of
     * host or host:port (default port 9092).
     * librdkafka will use the bootstrap brokers to acquire the full
     * set of brokers from the cluster. */
    if (rd_kafka_conf_set(conf, CONF_BROKER, brokers, errstr,
                          sizeof(errstr)) != RD_KAFKA_CONF_OK)
    {
        log_error(MAIN, errstr);
        exit(1);
    }

    if (rd_kafka_conf_set(conf, CONF_CLIENTID, client_id, errstr, sizeof(errstr)) != RD_KAFKA_CONF_OK)
    {
        log_error(MAIN, errstr);
        exit(1);
    }

    /* Set the delivery report callback.
     * This callback will be called once per message to inform
     * the application if delivery succeeded or failed.
     * See dr_msg_cb() above.
     * The callback is only triggered from rd_kafka_poll() and
     * rd_kafka_flush(). */
    rd_kafka_conf_set_dr_msg_cb(conf, dr_msg_cb);

    /*
     * Create producer instance.
     *
     * NOTE: rd_kafka_new() takes ownership of the conf object
     *       and the application must not reference it again after
     *       this call.
     */
    rk = rd_kafka_new(RD_KAFKA_PRODUCER, conf, errstr, sizeof(errstr));
    if (!rk)
    {
        sprintf(msg_buf, "Failed to create new producer: %s", errstr);
        log_error(MAIN, msg_buf);
        return 1;
    }

    /* Signal handler for clean shutdown */
    signal(SIGINT, stop);
    /*
    ** tps to sleep time
    **--------------------------------------------------
    **  tps   |   sec    |   ms        |  micros       |
    **--------------------------------------------------
    **   1    |    1     |   1000      |  1000000      |
    **   2    |    1/2   |   1000/2    |  1000000/2    |
    **   10   |    1/10  |   1000/10   |  1000000/10   |
    **   0.5  |    1/0.5 |   1000/0.5  |  1000000/0.5  |
    **   0.25 |    1/0.2 |   1000/0.25 |  1000000/0.25 |
    **--------------------------------------------------
    */

    char *str = malloc(recordlen);
    int iter = -1;
    
    long last = current_timestamp();
    while (run && rand_string(str, recordlen))
    {
        rd_kafka_resp_err_t err;

        iter++;
        if (iter == recordnum) 
            run = 0;
        
        {
            useconds_t sleepfor = 1000000/tps;
            sprintf(msg_buf, "Published %d messages. Sleeping for %d microsecs, Curr %zd Last %zd ...", iter, sleepfor, current_timestamp(), last);
            // log_debug(MAIN, msg_buf);
            usleep(sleepfor); // 2 milli or 2000 micro secs
            // log_debug(MAIN, "Done Sleeping");
        }

        long curr = current_timestamp();
        if (curr-last > 10000)
        {
            last = curr;
            // log(DEBUG, MAIN, "Polling now");
            /* Empty line: only serve delivery reports */
            rd_kafka_poll(rk, 0 /*non-blocking */);
        }

    retry:
        err = rd_kafka_producev(rk, RD_KAFKA_V_TOPIC(topic), RD_KAFKA_V_MSGFLAGS(RD_KAFKA_MSG_F_COPY),
                                RD_KAFKA_V_VALUE(buf, recordlen), RD_KAFKA_V_OPAQUE(NULL), RD_KAFKA_V_END);

        if (err)
        {
            /*
             * Failed to *enqueue* message for producing.
             */
            sprintf(msg_buf,
                    "Failed to produce to topic %s: %s", topic,
                    rd_kafka_err2str(err));
            log_error(MAIN, msg_buf);

            if (err == RD_KAFKA_RESP_ERR__QUEUE_FULL)
            {
                /* If the internal queue is full, wait for
                 * messages to be delivered and then retry.
                 * The internal queue represents both
                 * messages to be sent and messages that have
                 * been sent or failed, awaiting their
                 * delivery report callback to be called.
                 *
                 * The internal queue is limited by the
                 * configuration property
                 * queue.buffering.max.messages and
                 * queue.buffering.max.kbytes */
                rd_kafka_poll(rk,
                              1000 /*block for max 1000ms*/);
                goto retry;
            }
        }
        else
        {
            sprintf(msg_buf, "Enqueued message (%zd bytes) for topic %s", recordlen, topic);
            log_info(MAIN, msg_buf);
        }

        /* A producer application should continually serve
         * the delivery report queue by calling rd_kafka_poll()
         * at frequent intervals.
         * Either put the poll call in your main loop, or in a
         * dedicated thread, or call it after every
         * rd_kafka_produce() call.
         * Just make sure that rd_kafka_poll() is still called
         * during periods where you are not producing any messages
         * to make sure previously produced messages have their
         * delivery report callback served (and any other callbacks
         * you register). */
        // rd_kafka_poll(rk, 0 /*non-blocking*/);
    }

    /* Wait for final messages to be delivered or fail.
     * rd_kafka_flush() is an abstraction over rd_kafka_poll() which
     * waits for all messages to be delivered. */
    log_info(MAIN, "Flushing final messages...");
    rd_kafka_flush(rk, 10 * 1000 /* wait for max 10 seconds */);

    /* If the output queue is still not empty there is an issue
     * with producing messages to the clusters. */
    if (rd_kafka_outq_len(rk) > 0) {
        sprintf(msg_buf, "%d message(s) were not delivered", rd_kafka_outq_len(rk));
        log_error(MAIN, msg_buf);
    }

    /* Destroy the producer instance */
    rd_kafka_destroy(rk);

    return 0;
}