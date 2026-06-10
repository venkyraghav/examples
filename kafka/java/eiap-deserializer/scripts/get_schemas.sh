#!/usr/bin/env bash

# get_schema.sh
# Fetch schemas from a Confluent Schema Registry and save by schema type.
# Usage:
#   SCHEMA_REGISTRY_URL=http://localhost:8081 ./get_schema.sh
# Environment variables supported for auth (first match is used):
#   SR_USERNAME & SR_PASSWORD   - HTTP basic auth
#   SR_BEARER_TOKEN             - Authorization: Bearer <token>
#   OAUTH_URL, OAUTH_CLIENTID, OAUTH_SECRET - retrieve bearer token from Keycloak
#   SCHEMA_REGISTRY_CRT         - Schema Registry certificate
# Output directory: set OUTPUT_DIR (default ./schemas_<timestamp>)

set -euo pipefail

command -v jq >/dev/null 2>&1 || { echo "jq is required but not installed" >&2; exit 1; }

test -f ./init_get_schema.sh && source ./init_get_schema.sh
source ./init_get_schema.sh

SR_URL=${SCHEMA_REGISTRY_URL:-${SCHEMA_REGISTRY_URL:-http://localhost:8081}}
OUTPUT_DIR=${OUTPUT_DIR:-./generated/schemas_`date +"%Y%m%d%H%M%S"`}

SR_CRT_ARGS=
if [[ -n "${SCHEMA_REGISTRY_CRT}" ]]; then
        SR_CRT_ARGS="--cacert ${SCHEMA_REGISTRY_CRT}"
fi

# Authentication: prefer basic -> bearer env -> oauth client credentials
CURL_AUTH_ARGS=()
if [[ -n "${SR_USERNAME:-}" && -n "${SR_PASSWORD:-}" ]]; then
        CURL_AUTH_ARGS=( -u "${SR_USERNAME}:${SR_PASSWORD}" )
elif [[ -n "${SR_BEARER_TOKEN:-}" ]]; then
        CURL_AUTH_ARGS=( -H "Authorization: Bearer ${SR_BEARER_TOKEN}" )
elif [[ -n "${OAUTH_URL:-}" && -n "${OAUTH_CLIENTID:-}" && -n "${OAUTH_SECRET:-}" ]]; then
        echo "Obtaining OAuth token from ${OAUTH_URL}..."
        TOKEN=$( curl -sS -X POST -H "Content-Type: application/x-www-form-urlencoded" -d "client_id=${OAUTH_CLIENTID}&client_secret=${OAUTH_SECRET}&grant_type=client_credentials" ${OAUTH_URL} | jq -r '.access_token' )
        if [[ -z "${TOKEN}" || "${TOKEN}" == "null" ]]; then
                echo "Failed to obtain OAuth token" >&2; exit 1
        fi
        CURL_AUTH_ARGS=( -H "Authorization: Bearer ${TOKEN}" )
fi

mkdir -p "${OUTPUT_DIR}"
declare -A DIRS
DIRS[AVRO]="${OUTPUT_DIR}/avro"
DIRS[PROTOBUF]="${OUTPUT_DIR}/proto"
DIRS[JSON]="${OUTPUT_DIR}/jsonschema"
DIRS[JSONSCHEMA]="${OUTPUT_DIR}/jsonschema"
DIRS[DEFAULT]="${OUTPUT_DIR}/other"

for d in "${DIRS[@]}"; do mkdir -p "$d"; done


echo "Querying schema registry at ${SR_URL} ..."
subjects_json=$( curl ${SR_CRT_ARGS} -sS "${SR_URL}/subjects" "${CURL_AUTH_ARGS[@]:-}" |sed 's/^:.*://g')
if [[ -z "$subjects_json" ]]; then
        echo "No subjects returned from ${SR_URL}/subjects" >&2
        exit 1
fi

subjects=$( jq -r '.[]' <<< "$subjects_json" )
if [[ -z "$subjects" ]]; then
        echo "No subjects found"; exit 0
fi

sanitize() { echo "$1" | tr "/\n" "__" | sed 's/[^A-Za-z0-9._-]/_/g'; }

fetch_and_save() {
        local subject="$1"; local version="$2"
        local info_json
        info_json=$( curl ${SR_CRT_ARGS} -sS "${SR_URL}/subjects/${subject}/versions/${version}" "${CURL_AUTH_ARGS[@]:-}" )
        if [[ -z "$info_json" ]]; then
                echo "Failed to fetch ${subject} v${version}" >&2; return 1
        fi

        schemaId=$( jq -r '.id' <<< "$info_json" )
        schemaType=$( jq -r '.schemaType // "AVRO"' <<< "$info_json" )
        schema=$( jq -r '.schema' <<< "$info_json" )
        references=$( jq -r '.references // []' <<< "$info_json" )

        case "${schemaType^^}" in
                AVRO)
                        ext=.avsc; dir="${DIRS[AVRO]}";;
                PROTOBUF|PROTO)
                        ext=.proto; dir="${DIRS[PROTOBUF]}";;
                JSON|JSONSCHEMA)
                        ext=.json; dir="${DIRS[JSONSCHEMA]}";;
                *)
                        ext=.schema; dir="${DIRS[DEFAULT]}";;
        esac

        subj=`echo ${subject}|sed 's/^:.*://g'`
        fname="$(sanitize "${subj}")_v${version}_${schemaId}${ext}"
        outpath="${dir}/${fname}"
        echo "Writing ${outpath} (type=${schemaType})"
        printf "%s\n" "$schema" > "$outpath"

        # Handle protobuf/other references if present (each reference has name, subject, version)
        if [[ "${schemaType^^}" == "PROTOBUF" || "${schemaType^^}" == "PROTO" ]]; then
                refs_count=$( jq 'length' <<< "$references" )
                if [[ "$refs_count" -gt 0 ]]; then
                        echo "Found ${refs_count} references for ${subject} v${version}, fetching..."
                        for i in $(seq 0 $((refs_count-1))); do
                                name=$( jq -r ".[$i].name" <<< "$references" )
                                ref_subject=$( jq -r ".[$i].subject" <<< "$references" )
                                ref_version=$( jq -r ".[$i].version" <<< "$references" )
                                if [[ "$name" == "null" || "$ref_subject" == "null" || "$ref_version" == "null" ]]; then
                                        continue
                                fi
                                ref_info=$( curl ${SR_CRT_ARGS} -sS "${SR_URL}/subjects/${ref_subject}/versions/${ref_version}" "${CURL_AUTH_ARGS[@]:-}" )
                                ref_schema=$( jq -r '.schema' <<< "$ref_info" )
                                # Save referenced file using the provided name
                                ref_out="${DIRS[PROTOBUF]}/${name}"
                                echo "Writing reference ${ref_out}"
                                printf "%s\n" "$ref_schema" > "$ref_out"
                        done
                fi
        fi
}

while IFS= read -r subject; do
        if [[ -z "$subject" || "$subject" == "null" ]]; then continue; fi
        # Get versions for subject
        versions_json=$( curl ${SR_CRT_ARGS} -sS "${SR_URL}/subjects/${subject}/versions" "${CURL_AUTH_ARGS[@]:-}" )
        versions=$( jq -r '.[]' <<< "$versions_json" )
        for v in $versions; do
                fetch_and_save "$subject" "$v" || echo "Warning: failed to fetch ${subject} v${v}" >&2
        done
done <<< "$subjects"

echo "All schemas saved under ${OUTPUT_DIR}"
